package app.smartbike.protocolo

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ServidorGattTest {
    private val servicio = DirconCodec.uuid16(0x1826)
    private val lectura = DirconCodec.uuid16(0x2ACC)
    private val control = DirconCodec.uuid16(0x2AD9)
    private val datos = DirconCodec.uuid16(0x2AD2)
    private val escrito = mutableListOf<ByteArray>()

    private val servidor = ServidorGatt(
        listOf(
            ServicioGatt(
                servicio,
                listOf(
                    CaracteristicaGatt(lectura, setOf(PropiedadGatt.Leer), leer = { byteArrayOf(1, 2) }),
                    CaracteristicaGatt(control, setOf(PropiedadGatt.Escribir, PropiedadGatt.Indicar), alEscribir = { v ->
                        escrito += v
                        listOf(NotificacionGatt(control, byteArrayOf(0x80.toByte(), v[0], 1)))
                    }),
                    CaracteristicaGatt(datos, setOf(PropiedadGatt.Notificar)),
                ),
            ),
        ),
    )

    @Test
    fun descubrir() {
        assertEquals(listOf(servicio), servidor.descubrirServicios())
        val c = servidor.descubrirCaracteristicas(servicio) as ResultadoGatt.Ok
        assertEquals(listOf(lectura, control, datos), c.valor.map { it.uuid })
        assertEquals(ResultadoGatt.Error(ErrorGatt.ServicioNoEncontrado), servidor.descubrirCaracteristicas(datos))
    }

    @Test
    fun leer() {
        assertEquals(listOf<Byte>(1, 2), (servidor.leer(lectura) as ResultadoGatt.Ok).valor.toList())
        assertEquals(ResultadoGatt.Error(ErrorGatt.OperacionNoAdmitida), servidor.leer(datos))
        assertEquals(ResultadoGatt.Error(ErrorGatt.CaracteristicaNoEncontrada), servidor.leer(servicio))
    }

    @Test
    fun escribir_sin_suscripcion_no_notifica() {
        val r = servidor.escribir(control, byteArrayOf(0x00)) as ResultadoGatt.Ok
        assertTrue(r.valor.isEmpty())
        assertEquals(1, escrito.size)
    }

    @Test
    fun escribir_con_suscripcion_notifica_la_respuesta() {
        servidor.suscribir(control, true)
        val r = servidor.escribir(control, byteArrayOf(0x07)) as ResultadoGatt.Ok
        assertEquals(listOf<Byte>(0x80.toByte(), 0x07, 1), r.valor.single().valor.toList())
    }

    @Test
    fun escribir_donde_no_se_puede() {
        assertEquals(ResultadoGatt.Error(ErrorGatt.OperacionNoAdmitida), servidor.escribir(lectura, byteArrayOf(1)))
        assertEquals(ResultadoGatt.Error(ErrorGatt.OperacionNoAdmitida), servidor.suscribir(lectura, true))
    }

    @Test
    fun notificar_solo_si_suscrito_y_hasta_perder_el_cliente() {
        assertNull(servidor.notificar(datos, byteArrayOf(1)))
        servidor.suscribir(datos, true)
        assertTrue(servidor.suscrita(datos))
        assertEquals(datos, servidor.notificar(datos, byteArrayOf(1))?.caracteristica)
        servidor.clientePerdido()
        assertFalse(servidor.suscrita(datos))
        assertNull(servidor.notificar(datos, byteArrayOf(1)))
    }

    @Test
    fun desuscribir() {
        servidor.suscribir(datos, true)
        servidor.suscribir(datos, false)
        assertNull(servidor.notificar(datos, byteArrayOf(1)))
    }
}
