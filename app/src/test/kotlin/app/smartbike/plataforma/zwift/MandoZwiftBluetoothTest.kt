package app.smartbike.plataforma.zwift

import app.smartbike.datos.RegistroSesion
import app.smartbike.dominio.FuncionZwift
import app.smartbike.dominio.IdentidadDispositivo
import app.smartbike.dominio.ModoConexion
import app.smartbike.plataforma.BluetoothDePrueba
import app.smartbike.plataforma.ConexionFtms
import app.smartbike.plataforma.EstadoMando
import app.smartbike.plataforma.EstadoTransporte
import app.smartbike.plataforma.ReceptorGrabador
import app.smartbike.plataforma.TransporteDePrueba
import app.smartbike.plataforma.zwift.MandoZwift.Companion.ASYNC
import app.smartbike.plataforma.zwift.MandoZwift.Companion.SYNC_RX
import app.smartbike.protocolo.MandoZwiftCodec
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * El mando de Zwift por Bluetooth, aislado: spec `zwift-controller` («Mando por Bluetooth»); se publica con el ajuste, el
 * Bluetooth, los permisos y el modo periférico (design.md del change `mando-zwift-emulado`, decisión 6).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MandoZwiftBluetoothTest {
    private val registro = RegistroSesion(relojMonotonico = { 0L }, relojPared = { 0L })
    private val ble = TransporteDePrueba()
    private val bluetooth = BluetoothDePrueba()
    private var periferico = true

    private fun TestScope.mando() = MandoZwiftBluetooth(backgroundScope, MandoZwift(backgroundScope, registro, ble), ble, bluetooth) { periferico }.also { runCurrent() }

    @Test
    fun el_mando_va_por_su_propio_transporte_bluetooth() = runTest {
        val m = mando()
        m.activar(true)
        assertEquals(1, ble.publicaciones)
        assertEquals(MandoZwift.IDENTIDAD, ble.identidad)
        assertEquals(listOf(MandoZwift.SERVICIO, MandoZwift.INFORMACION, MandoZwift.BATERIA), checkNotNull(ble.servidor).descubrirServicios())
        assertEquals(EstadoMando.Esperando, m.estado.value)
        // Idempotente: otro aviso con las mismas condiciones no lo vuelve a publicar.
        m.activar(true)
        assertEquals(1, ble.publicaciones)
    }

    @Test
    fun estados_del_mando_segun_el_ajuste_el_bluetooth_y_el_modo_periferico() = runTest {
        val m = mando()
        assertEquals(EstadoMando.Desactivado, m.estado.value)
        m.activar(true)
        assertEquals(EstadoMando.Esperando, m.estado.value)
        bluetooth.encendido.value = false
        runCurrent()
        assertEquals(EstadoMando.SinConexion, m.estado.value)
        assertEquals(1, ble.paradas)
        bluetooth.encendido.value = true
        runCurrent()
        assertEquals(EstadoMando.Esperando, m.estado.value)
        assertEquals(2, ble.publicaciones)
        m.activar(false)
        assertEquals(EstadoMando.Desactivado, m.estado.value)
    }

    @Test
    fun movil_sin_modo_periferico_no_anuncia_el_mando() = runTest {
        periferico = false
        val m = mando()
        m.activar(true)
        assertEquals(EstadoMando.NoDisponible, m.estado.value)
        assertEquals(0, ble.publicaciones)
    }

    @Test
    fun desactivar_con_un_boton_mantenido_lo_suelta_antes_de_dejar_de_anunciar() = runTest {
        val m = mando()
        m.activar(true)
        val s = checkNotNull(ble.servidor)
        ble.estado.value = EstadoTransporte.Conectado("PC")
        s.suscribir(ASYNC, true)
        s.escribir(SYNC_RX, MandoZwiftCodec.RIDE_ON + byteArrayOf(0x02, 0x04))
        runCurrent()
        m.pulsar(FuncionZwift.FRENO_IZQUIERDO)
        ble.enviadas.clear()
        m.activar(false)
        assertEquals("23 08 ff ff ff ff 0f 1a 04 08 00 10 00 1a 04 08 01 10 00", ble.enviadasPor(ASYNC).last())
        assertEquals(1, ble.paradas)
    }

    @Test
    fun terminar_para_el_mando() = runTest {
        val m = mando()
        m.activar(true)
        m.terminar()
        assertEquals(1, ble.paradas)
    }

    @Test
    fun enchufado_a_la_conexion_retirar_deja_el_mando_y_terminar_lo_para_todo() = runTest {
        val rodillo = TransporteDePrueba()
        val m = mando()
        val c = ConexionFtms(ModoConexion.FTMS_WIFI, registro, rodillo, "0.0-test", ReceptorGrabador(), mando = m)
        m.activar(true)
        c.publicar(IdentidadDispositivo("02:11:22:33:44:55", "0123456789"))
        c.retirar()
        assertEquals(1, rodillo.paradas)
        assertEquals(EstadoMando.Esperando, checkNotNull(c.mando).estado.value)
        c.terminar()
        assertEquals(2, rodillo.paradas)
        assertEquals(1, ble.paradas)
    }
}
