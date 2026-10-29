package app.smartbike.plataforma

import app.smartbike.dominio.IdentidadDispositivo
import app.smartbike.protocolo.CaracteristicaGatt
import app.smartbike.protocolo.DirconCodec.uuid16
import app.smartbike.protocolo.NotificacionGatt
import app.smartbike.protocolo.PropiedadGatt
import app.smartbike.protocolo.ServicioGatt
import app.smartbike.protocolo.ServidorGatt
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Las condiciones del periférico del modo «Rodillo por Bluetooth (FTMS)»: spec `ftms-bluetooth`, «Requisitos del móvil»,
 * y design.md del change `ftms-bluetooth`, decisión 4.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TransporteBluetoothTest {
    private val servidorGatt = ServidorGatt(listOf(ServicioGatt(uuid16(0x1826), listOf(CaracteristicaGatt(uuid16(0x2AD2), setOf(PropiedadGatt.Notificar))))))
    private val identidad = IdentidadDispositivo("02:11:22:33:44:55", "0123456789")
    private val host = TransporteDePrueba()
    private var periferico = true

    private fun TestScope.transporte(bt: BluetoothDePrueba = BluetoothDePrueba()) = TransporteBluetooth(backgroundScope, host, bt) { periferico }

    @Test
    fun con_todo_en_orden_publica_el_servidor_una_sola_vez_y_el_estado_es_el_del_servidor() = runTest {
        val t = transporte()
        runCurrent()
        assertEquals(EstadoTransporte.Parado, t.estado.value)
        t.publicar(servidorGatt, identidad, "Smart Bike")
        t.publicar(servidorGatt, identidad, "Smart Bike")
        runCurrent()
        assertEquals(1, host.publicaciones)
        assertEquals(EstadoTransporte.Esperando, t.estado.value)
        host.estado.value = EstadoTransporte.Conectado("AA:BB")
        runCurrent()
        assertEquals(EstadoTransporte.Conectado("AA:BB"), t.estado.value)
    }

    @Test
    fun apagar_y_encender_el_bluetooth_retira_y_vuelve_a_anunciar_sin_tocar_nada() = runTest {
        val bt = BluetoothDePrueba()
        val t = transporte(bt)
        t.publicar(servidorGatt, identidad, "Smart Bike")
        runCurrent()
        bt.encendido.value = false
        runCurrent()
        assertEquals(1, host.paradas)
        assertEquals(EstadoTransporte.SinBluetooth, t.estado.value)
        bt.encendido.value = true
        runCurrent()
        assertEquals(2, host.publicaciones)
        assertEquals(EstadoTransporte.Esperando, t.estado.value)
    }

    @Test
    fun sin_permiso_de_anunciar_o_de_conectar_no_publica_y_al_concederlo_si() = runTest {
        val bt = BluetoothDePrueba(permisoAnunciar = false)
        val t = transporte(bt)
        t.publicar(servidorGatt, identidad, "Smart Bike")
        runCurrent()
        assertEquals(0, host.publicaciones)
        assertEquals(EstadoTransporte.SinBluetooth, t.estado.value)
        bt.permisoAnunciar.value = true
        bt.permiso.value = false
        runCurrent()
        assertEquals(0, host.publicaciones)
        bt.permiso.value = true
        runCurrent()
        assertEquals(1, host.publicaciones)
    }

    @Test
    fun un_movil_sin_modo_periferico_da_no_disponible_y_no_publica() = runTest {
        periferico = false
        val t = transporte()
        t.publicar(servidorGatt, identidad, "Smart Bike")
        runCurrent()
        assertEquals(0, host.publicaciones)
        assertEquals(EstadoTransporte.NoDisponible, t.estado.value)
    }

    @Test
    fun con_el_bluetooth_apagado_manda_sin_bluetooth_aunque_el_periferico_no_se_sepa() = runTest {
        periferico = false // Android no lo sabe con el Bluetooth apagado
        val t = transporte(BluetoothDePrueba(encendido = false))
        t.publicar(servidorGatt, identidad, "Smart Bike")
        runCurrent()
        assertEquals(EstadoTransporte.SinBluetooth, t.estado.value)
    }

    @Test
    fun parar_retira_el_servidor_y_notificar_va_al_servidor() = runTest {
        val t = transporte()
        t.publicar(servidorGatt, identidad, "Smart Bike")
        runCurrent()
        val n = NotificacionGatt(uuid16(0x2AD2), byteArrayOf(1, 2))
        t.notificar(n)
        assertEquals(listOf(n), host.enviadas)
        t.parar()
        runCurrent()
        assertEquals(1, host.paradas)
        assertEquals(EstadoTransporte.Parado, t.estado.value)
        t.parar()
        assertEquals(1, host.paradas, "parar dos veces no repite")
        assertTrue(host.enviadas.size == 1)
    }
}
