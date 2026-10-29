package app.smartbike.plataforma.zwift

import app.smartbike.datos.RegistroSesion
import app.smartbike.dominio.FuncionZwift
import app.smartbike.dominio.IdentidadDispositivo
import app.smartbike.dominio.ModoConexion
import app.smartbike.dominio.Terreno
import app.smartbike.plataforma.EstadoMando
import app.smartbike.plataforma.EstadoTransporte
import app.smartbike.plataforma.ReceptorGrabador
import app.smartbike.plataforma.TelemetriaPlataforma
import app.smartbike.plataforma.TransporteDePrueba
import app.smartbike.plataforma.UltimoDePlataforma
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
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/** Spec `zwift-ride` («Modo Zwift Ride») y `zwift-controller`; design.md del change `mando-zwift-dircon`, decisiones 1 a 4 y 7. */
@OptIn(ExperimentalCoroutinesApi::class)
class ConexionZwiftRideTest {
    private val registro = RegistroSesion(relojMonotonico = { 0L }, relojPared = { 0L })
    private val transporte = TransporteDePrueba()
    private val receptor = ReceptorGrabador()
    private val identidad = IdentidadDispositivo("02:11:22:33:44:55", "0123456789")
    private val rideOn = MandoZwiftCodec.RIDE_ON + byteArrayOf(0x02, 0x04)

    private fun bytes(h: String) = h.split(" ").map { it.toInt(16).toByte() }.toByteArray()
    private fun hex(b: ByteArray) = b.joinToString(" ") { "%02x".format(it) }

    private val ConexionZwiftRide.m get() = mando

    private fun TestScope.conexion() = ConexionZwiftRide(backgroundScope, registro, transporte, "0.0-test", receptor).also { runCurrent() }

    /** Zwift se conecta, se suscribe como lo hizo en la prueba y saluda al mando. */
    private fun TestScope.zwiftSaluda(c: ConexionZwiftRide) {
        c.m.activar(true)
        c.publicar(identidad)
        val s = checkNotNull(transporte.servidor)
        transporte.estado.value = EstadoTransporte.Conectado("192.168.0.10")
        listOf(uuid16(FtmsCodec.INDOOR_BIKE_DATA), uuid16(FtmsCodec.CP_MEDIDA), ASYNC, SYNC_TX).forEach { s.suscribir(it, true) }
        s.escribir(SYNC_RX, rideOn)
        runCurrent()
        transporte.enviadas.clear()
    }

    private fun TestScope.escribir(h: String): List<String> {
        val r = checkNotNull(transporte.servidor).escribir(SYNC_RX, bytes(h)) as ResultadoGatt.Ok
        runCurrent()
        return r.valor.map { hex(it.valor) }
    }

    @Test
    fun el_rodillo_ofrece_ftms_potencia_pulso_informacion_y_el_mando() = runTest {
        val c = conexion()
        c.publicar(identidad)
        assertEquals(ModoConexion.ZWIFT_RIDE, c.modo)
        assertEquals(
            listOf(0x1826, 0x1818, 0x180D, 0x180A, 0xFC82).map(::uuid16),
            checkNotNull(transporte.servidor).descubrirServicios(),
        )
        assertEquals(identidad, transporte.identidad)
    }

    private fun leerSerie(): String = String((checkNotNull(transporte.servidor).leer(uuid16(FtmsCodec.SERIE)) as ResultadoGatt.Ok).valor, Charsets.UTF_8)

    @Test
    fun zwift_lee_la_serie_de_la_identidad_publicada_sin_neo() = runTest {
        val c = conexion()
        c.publicar(identidad)
        assertEquals("0123456789", leerSerie())
        c.publicar(IdentidadDispositivo("02:aa:bb:cc:4F:2A", "9876543210"))
        assertEquals("9876543210", leerSerie())
    }

    @Test
    fun simulando_un_kickr_la_serie_es_la_de_esa_identidad() = runTest {
        val c = ConexionZwiftRide(backgroundScope, registro, transporte, "0.0-test", receptor, simularKickr = true).also { runCurrent() }
        c.publicar(IdentidadDispositivo("02:aa:bb:cc:4F:2A", "5555555555"))
        assertEquals("5555555555", leerSerie())
    }

    @Test
    fun un_set_con_pendiente_llega_como_terreno_y_se_contesta() = runTest {
        val c = conexion()
        zwiftSaluda(c)
        val r = escribir("3f 08 01 10 01 22 05 28 c7 02 38 00") // −1,64 %
        assertEquals(listOf("40 08 00 10 01"), r)
        assertEquals(listOf(Terreno(-1.64)), receptor.terrenos)
        assertTrue(receptor.ultimo is UltimoDePlataforma.Simulacion)
    }

    @Test
    fun los_set_a_trozos_se_juntan_y_la_relacion_sola_no_cambia_el_terreno() = runTest {
        val c = conexion()
        zwiftSaluda(c)
        escribir("3f 08 01 10 00 22 0c 40 ec 27 50 d3 06 58 9c 4a 60 90 03") // CW, masas y Crr: sin pendiente
        escribir("3f 08 00 10 03 22 03 28 b1 02") // −1,53 %
        escribir("3f 08 01 10 02 22 04 38 a0 83 01") // relación 1,68
        assertEquals(listOf(Terreno(-1.53, crr = 0.004, cw = 0.51)), receptor.terrenos)
        assertEquals(listOf("ultimo Simulacion", "sin cambio", "ultimo Simulacion", "terreno Terreno(pendientePct=-1.53, crr=0.004, cw=0.51, vientoMs=null)", "ultimo Simulacion", "sin cambio"), receptor.eventos)
    }

    @Test
    fun un_set_con_erg_llega_como_potencia() = runTest {
        val c = conexion()
        zwiftSaluda(c)
        assertEquals(listOf("40 08 00 10 05"), escribir("3f 08 01 10 05 22 03 10 e6 01"))
        assertEquals(listOf(230), receptor.potencias)
        assertTrue(receptor.terrenos.isEmpty())
    }

    @Test
    fun un_set_ilegible_no_llega_ni_se_contesta() = runTest {
        val c = conexion()
        zwiftSaluda(c)
        assertTrue(escribir("3f 08 01 10 00 22 0c 40").isEmpty())
        assertTrue(receptor.eventos.isEmpty())
    }

    @Test
    fun los_datos_de_pedaleo_salen_solo_tras_el_handshake_y_con_los_cinco_campos() = runTest {
        val c = conexion()
        c.publicar(identidad)
        val s = checkNotNull(transporte.servidor)
        s.suscribir(ASYNC, true)
        s.suscribir(uuid16(FtmsCodec.INDOOR_BIKE_DATA), true)
        c.telemetria(TelemetriaPlataforma(120, 80, null, 0.0))
        assertTrue(transporte.enviadasPor(ASYNC).isEmpty(), "nada antes del handshake")
        s.escribir(SYNC_RX, rideOn)
        transporte.enviadas.clear()
        c.telemetria(TelemetriaPlataforma(120, 80, null, 0.0))
        assertEquals(listOf("03 08 78 10 50 18 00 20 78 28 00"), transporte.enviadasPor(ASYNC))
        // Con Neo perdido, la `Sesion` manda 0.
        transporte.enviadas.clear()
        c.telemetria(TelemetriaPlataforma(0, 0, null, 0.0))
        assertEquals(listOf("03 08 00 10 00 18 00 20 00 28 00"), transporte.enviadasPor(ASYNC))
    }

    @Test
    fun los_comandos_de_ftms_se_contestan_y_no_cambian_nada() = runTest {
        val c = conexion()
        zwiftSaluda(c)
        val control = uuid16(FtmsCodec.PUNTO_CONTROL)
        checkNotNull(transporte.servidor).suscribir(control, true)
        val r = checkNotNull(transporte.servidor).escribir(control, bytes("11 00 00 90 01 28 33")) as ResultadoGatt.Ok
        assertTrue(r.valor.isNotEmpty())
        assertTrue(receptor.eventos.isEmpty())
    }

    @Test
    fun las_funciones_del_mando_salen_por_async() = runTest {
        val c = conexion()
        zwiftSaluda(c)
        c.m.pulsar(FuncionZwift.ARRIBA)
        assertEquals(listOf("23 08 fd ff ff ff 0f 1a 04 08 00 10 00 1a 04 08 01 10 00"), transporte.enviadasPor(ASYNC))
        c.m.soltarTodo()
        assertEquals("23 08 ff ff ff ff 0f 1a 04 08 00 10 00 1a 04 08 01 10 00", transporte.enviadasPor(ASYNC).last())
    }

    @Test
    fun estados_del_mando() = runTest {
        val c = conexion()
        assertEquals(EstadoMando.Desactivado, c.m.estado.value)
        c.m.activar(true)
        assertEquals(EstadoMando.SinConexion, c.m.estado.value)
        c.publicar(identidad)
        assertEquals(EstadoMando.Esperando, c.m.estado.value)
        val s = checkNotNull(transporte.servidor)
        transporte.estado.value = EstadoTransporte.Conectado("192.168.0.10")
        s.suscribir(ASYNC, true)
        s.escribir(SYNC_RX, rideOn)
        runCurrent()
        assertEquals(EstadoMando.Conectado, c.m.estado.value)
        c.m.activar(false)
        assertEquals(EstadoMando.Desactivado, c.m.estado.value)
        c.m.activar(true)
        assertEquals(EstadoMando.Conectado, c.m.estado.value)
        c.retirar()
        assertEquals(EstadoMando.SinConexion, c.m.estado.value)
        assertEquals(1, transporte.paradas)
    }

    @Test
    fun zwift_perdido_suelta_todo_y_el_siguiente_empieza_sin_nada() = runTest {
        val c = conexion()
        zwiftSaluda(c)
        c.m.pulsar(FuncionZwift.A)
        transporte.estado.value = EstadoTransporte.Esperando
        runCurrent()
        assertEquals(EstadoMando.Esperando, c.m.estado.value)
        assertTrue(c.m.giro.isEmpty())
        transporte.enviadas.clear()
        val s = checkNotNull(transporte.servidor)
        transporte.estado.value = EstadoTransporte.Conectado("192.168.1.35")
        s.suscribir(ASYNC, true)
        s.suscribir(SYNC_TX, true)
        s.escribir(SYNC_RX, rideOn)
        runCurrent()
        c.m.soltarTodo()
        assertTrue(transporte.enviadasPor(ASYNC).none { it.startsWith("23 08 ef") }, "A no debe seguir pulsada")
    }

    @Test
    fun terminar_para_todo() = runTest {
        val c = conexion()
        c.m.activar(true)
        c.publicar(identidad)
        c.terminar()
        assertEquals(1, transporte.paradas)
        assertEquals(EstadoMando.SinConexion, c.m.estado.value)
    }

    // --- Simular un KICKR BIKE (change simular-kickr-bike) ---

    @Test
    fun sin_simular_se_anuncia_como_smart_bike() = runTest {
        val c = conexion()
        c.publicar(IdentidadDispositivo("02:aa:bb:cc:dd:ee", "1"))
        assertEquals("Smart Bike", transporte.nombre)
    }

    @Test
    fun simulando_usa_su_nombre_y_su_identidad_y_conserva_el_servicio_del_mando() = runTest {
        val c = ConexionZwiftRide(backgroundScope, registro, transporte, "0.0-test", receptor, simularKickr = true).also { runCurrent() }
        val a = IdentidadDispositivo("02:aa:bb:cc:4F:2A", "1")
        c.publicar(a)
        assertEquals("KICKR BIKE PRO 4F2A", transporte.nombre)
        assertEquals(a, transporte.identidad)
        assertTrue(uuid16(0xFC82) in checkNotNull(transporte.servidor).descubrirServicios())
        assertEquals(ModoConexion.ZWIFT_RIDE, c.modo)
    }
}
