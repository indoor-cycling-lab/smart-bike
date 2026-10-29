package app.smartbike.plataforma.zwift

import app.smartbike.datos.RegistroSesion
import app.smartbike.dominio.FuncionZwift
import app.smartbike.dominio.IdentidadDispositivo
import app.smartbike.dominio.LadoGiro
import app.smartbike.dominio.ModoConexion
import app.smartbike.plataforma.ConexionFtms
import app.smartbike.plataforma.EstadoMando
import app.smartbike.plataforma.EstadoTransporte
import app.smartbike.plataforma.ReceptorGrabador
import app.smartbike.plataforma.TransporteDePrueba
import app.smartbike.plataforma.zwift.MandoZwift.Companion.ASYNC
import app.smartbike.plataforma.zwift.MandoZwift.Companion.SYNC_RX
import app.smartbike.plataforma.zwift.MandoZwift.Companion.SYNC_TX
import app.smartbike.protocolo.DirconCodec.uuid16
import app.smartbike.protocolo.FtmsCodec
import app.smartbike.protocolo.MandoZwiftCodec
import app.smartbike.protocolo.ResultadoGatt
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * El mando de Zwift como un servicio más del servidor del rodillo por Bluetooth: spec `ftms-bluetooth` («Mando de Zwift
 * dentro del mismo dispositivo») y design.md del change `ftms-bluetooth`, decisión 5.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MandoZwiftEnServidorTest {
    private val registro = RegistroSesion(relojMonotonico = { 0L }, relojPared = { 0L })
    private val transporte = TransporteDePrueba()
    private val identidad = IdentidadDispositivo("02:11:22:33:44:55", "0123456789")

    private fun TestScope.conexion(): Pair<ConexionFtms, MandoZwiftEnServidor> {
        val mando = MandoZwiftEnServidor(
            backgroundScope,
            MandoZwift(backgroundScope, registro, transporte, servicioDelMando = ServicioDelMando.BLUETOOTH),
            transporte.estado,
        )
        val c = ConexionFtms(ModoConexion.FTMS_BLUETOOTH, registro, transporte, "0.0-test", ReceptorGrabador(), mando = mando)
        runCurrent()
        return c to mando
    }

    /** Zwift se conecta, se suscribe al mando y manda el `RideOn`. */
    private fun TestScope.saludar() {
        val s = checkNotNull(transporte.servidor)
        transporte.estado.value = EstadoTransporte.Conectado("AA:BB")
        s.suscribir(ASYNC, true)
        s.suscribir(SYNC_TX, true)
        s.escribir(SYNC_RX, MandoZwiftCodec.RIDE_ON)
        runCurrent()
    }

    @Test
    fun el_servidor_del_rodillo_incluye_el_servicio_del_mando_con_sus_cinco_caracteristicas() = runTest {
        val (c, _) = conexion()
        c.publicar(identidad)
        val s = checkNotNull(transporte.servidor)
        assertEquals(listOf(uuid16(FtmsCodec.SERVICIO_FTMS), uuid16(FtmsCodec.SERVICIO_POTENCIA), uuid16(0x180D), uuid16(0x180A), MandoZwift.SERVICIO), s.descubrirServicios())
        val caracteristicas = (s.descubrirCaracteristicas(MandoZwift.SERVICIO) as ResultadoGatt.Ok).valor.map { it.uuid }
        assertEquals(listOf(ASYNC, SYNC_RX, SYNC_TX, MandoZwift.DESCONOCIDA_5, MandoZwift.DESCONOCIDA_6), caracteristicas)
        assertEquals(1, transporte.publicaciones, "un solo servidor y una sola publicación")
    }

    @Test
    fun el_estado_sigue_al_ajuste_a_la_publicacion_y_al_saludo_de_zwift() = runTest {
        val (c, mando) = conexion()
        assertEquals(EstadoMando.Desactivado, mando.estado.value, "hasta que la sesión lo activa")
        mando.activar(true)
        assertEquals(EstadoMando.SinConexion, mando.estado.value, "sin publicar el rodillo no hay mando")
        mando.activar(false)
        assertEquals(EstadoMando.Desactivado, mando.estado.value)
        mando.activar(true)
        c.publicar(identidad)
        runCurrent()
        assertEquals(EstadoMando.Esperando, mando.estado.value)
        saludar()
        assertEquals(EstadoMando.Conectado, mando.estado.value)
        c.retirar()
        runCurrent()
        assertEquals(EstadoMando.SinConexion, mando.estado.value)
    }

    @Test
    fun las_funciones_de_zwift_salen_por_el_rodillo_y_las_de_otra_plataforma_se_ignoran() = runTest {
        val (c, mando) = conexion()
        mando.activar(true)
        c.publicar(identidad)
        saludar()
        mando.pulsar(FuncionZwift.A)
        assertTrue(transporte.enviadasPor(ASYNC).isNotEmpty(), "la pulsación sale por el transporte del rodillo")
        assertEquals(
            setOf(LadoGiro.IZQUIERDA),
            run {
                mando.pulsar(FuncionZwift.GIRAR_IZQUIERDA)
                mando.giro
            },
        )
        mando.soltarTodo()
        assertTrue(mando.giro.isEmpty())
    }

    @Test
    fun retirar_y_terminar_paran_el_mando_con_el_rodillo() = runTest {
        val (c, mando) = conexion()
        mando.activar(true)
        c.publicar(identidad)
        runCurrent()
        c.retirar()
        runCurrent()
        assertEquals(1, transporte.paradas)
        assertFalse(mando.estado.value == EstadoMando.Esperando)
        c.terminar()
        assertEquals(2, transporte.paradas)
    }

    @Test
    fun sin_bluetooth_o_sin_modo_periferico_el_mando_sigue_al_transporte() = runTest {
        val (c, mando) = conexion()
        mando.activar(true)
        c.publicar(identidad)
        runCurrent()
        assertEquals(EstadoMando.Esperando, mando.estado.value)
        transporte.estado.value = EstadoTransporte.SinBluetooth
        runCurrent()
        assertEquals(EstadoMando.SinConexion, mando.estado.value)
        transporte.estado.value = EstadoTransporte.NoDisponible
        runCurrent()
        assertEquals(EstadoMando.NoDisponible, mando.estado.value)
        transporte.estado.value = EstadoTransporte.Esperando
        runCurrent()
        assertEquals(EstadoMando.Esperando, mando.estado.value)
    }
}
