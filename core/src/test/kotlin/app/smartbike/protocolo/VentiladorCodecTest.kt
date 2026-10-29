package app.smartbike.protocolo

import app.smartbike.dominio.ConfiguracionVentilador
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

/** Design.md del change `ventilador-headwind`, decisión 2. El protocolo está **a validar** con el ventilador real. */
class VentiladorCodecTest {
    private fun bytes(vararg v: Int) = ByteArray(v.size) { v[it].toByte() }

    @Test
    fun codifica_las_ordenes() {
        assertArrayEquals(bytes(0x02, 0x1E), VentiladorCodec.velocidad(30))
        assertArrayEquals(bytes(0x04, 0x04), VentiladorCodec.modo(VentiladorCodec.MODO_MANUAL))
        assertArrayEquals(bytes(0x04, 0x02), VentiladorCodec.modo(VentiladorCodec.MODO_PULSO))
        assertArrayEquals(bytes(0x01), VentiladorCodec.leerVelocidad())
        assertArrayEquals(bytes(0x03), VentiladorCodec.leerModo())
        assertArrayEquals(bytes(0x05), VentiladorCodec.leerConfiguracion())
    }

    @Test
    fun la_velocidad_se_recorta_a_0_100() {
        assertArrayEquals(bytes(0x02, 100), VentiladorCodec.velocidad(150))
        assertArrayEquals(bytes(0x02, 0), VentiladorCodec.velocidad(-5))
    }

    @Test
    fun escribir_la_configuracion_antepone_el_codigo() {
        val c = ConfiguracionVentilador.desde(bytes(120, 135, 150, 165, 1, 2, 3, 4, 5))!!
        assertArrayEquals(bytes(0x06, 120, 135, 150, 165, 1, 2, 3, 4, 5), VentiladorCodec.escribirConfiguracion(c))
    }

    @Test
    fun decodifica_el_estado() = assertEquals(NotificacionVentilador.Estado(velocidad = 0x19, modo = 4), VentiladorCodec.decodificar(bytes(0xFD, 0x01, 0x19, 0x04)))

    @Test
    fun decodifica_las_respuestas() {
        assertEquals(NotificacionVentilador.Modo(correcto = true, modo = 2), VentiladorCodec.decodificar(bytes(0xFE, 0x03, 0x01, 0x02)))
        assertEquals(NotificacionVentilador.Velocidad(correcto = true, velocidad = 40), VentiladorCodec.decodificar(bytes(0xFE, 0x01, 0x01, 40)))
        assertEquals(NotificacionVentilador.Confirmacion(orden = 2, correcto = true), VentiladorCodec.decodificar(bytes(0xFE, 0x02, 0x01)))
        assertEquals(NotificacionVentilador.Confirmacion(orden = 6, correcto = false), VentiladorCodec.decodificar(bytes(0xFE, 0x06, 0x02)))
    }

    @Test
    fun la_configuracion_de_9_y_de_18_bytes_llega_entera() {
        val nueve = bytes(120, 135, 150, 165, 10, 20, 30, 40, 50)
        val dieciocho = ByteArray(18) { (100 + it).toByte() }
        assertEquals(NotificacionVentilador.Configuracion(true, nueve), VentiladorCodec.decodificar(bytes(0xFE, 0x05, 0x01) + nueve))
        assertEquals(NotificacionVentilador.Configuracion(true, dieciocho), VentiladorCodec.decodificar(bytes(0xFE, 0x05, 0x01) + dieciocho))
    }

    @Test
    fun un_paquete_vacio_corto_o_desconocido_es_null() {
        assertNull(VentiladorCodec.decodificar(bytes()))
        assertNull(VentiladorCodec.decodificar(bytes(0xFD)))
        assertNull(VentiladorCodec.decodificar(bytes(0xFD, 0x01, 0x19)))
        assertNull(VentiladorCodec.decodificar(bytes(0xFE, 0x03, 0x01)))
        assertNull(VentiladorCodec.decodificar(bytes(0xAA, 0x01, 0x01, 0x01)))
        assertNull(VentiladorCodec.decodificar(bytes(0xFE, 0x77, 0x01)))
    }
}
