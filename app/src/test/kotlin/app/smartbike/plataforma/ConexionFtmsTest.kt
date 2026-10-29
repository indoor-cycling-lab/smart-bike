package app.smartbike.plataforma

import app.smartbike.datos.RegistroSesion
import app.smartbike.dominio.Enlace
import app.smartbike.dominio.FuncionMando
import app.smartbike.dominio.IdentidadDispositivo
import app.smartbike.dominio.LadoGiro
import app.smartbike.dominio.ModoConexion
import app.smartbike.dominio.Terreno
import app.smartbike.plataforma.zwift.MandoZwift
import app.smartbike.plataforma.zwift.MandoZwift.Companion.ASYNC
import app.smartbike.plataforma.zwift.MandoZwift.Companion.SYNC_RX
import app.smartbike.plataforma.zwift.MandoZwift.Companion.SYNC_TX
import app.smartbike.plataforma.zwift.MandoZwiftEnServidor
import app.smartbike.plataforma.zwift.ServicioDelMando
import app.smartbike.protocolo.DirconCodec.uuid16
import app.smartbike.protocolo.FtmsCodec
import app.smartbike.protocolo.MandoZwiftCodec
import app.smartbike.protocolo.ResultadoGatt
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * El modo «Rodillo por wifi (FTMS)», aislado y sin mando de ninguna plataforma: spec `platform-bridge` («El modo FTMS
 * por wifi no cambia») y design.md del change `generalizar-plataformas`, decisión 4. El mando por Bluetooth de Zwift
 * tiene su propio test, `MandoZwiftBluetoothTest`.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ConexionFtmsTest {
    private val registro = RegistroSesion(relojMonotonico = { 0L }, relojPared = { 0L })
    private val rodillo = TransporteDePrueba()
    private val receptor = ReceptorGrabador()
    private val identidad = IdentidadDispositivo("02:11:22:33:44:55", "0123456789")

    private fun bytes(h: String) = h.split(" ").map { it.toInt(16).toByte() }.toByteArray()

    private fun conexion(simularKickr: Boolean = false, mando: MandoEmulado? = null, modo: ModoConexion = ModoConexion.FTMS_WIFI) = ConexionFtms(modo, registro, rodillo, "0.0-test", receptor, simularKickr, mando)

    /** Un mando de mentira: solo anota que se le ha dicho que termine. */
    private class MandoFalso : MandoEmulado {
        override val estado: StateFlow<EstadoMando> = MutableStateFlow(EstadoMando.Esperando)
        var terminado = false
        override fun activar(activado: Boolean) = Unit
        override fun pulsar(funcion: FuncionMando) = Unit
        override fun soltar(funcion: FuncionMando) = Unit
        override fun soltarTodo() = Unit
        override val giro: Set<LadoGiro> = emptySet()
        override fun terminar() {
            terminado = true
        }
    }

    @Test
    fun la_plataforma_lee_la_serie_de_la_identidad_publicada_sin_neo() {
        val c = conexion()
        c.publicar(identidad)
        val serie = { String((checkNotNull(rodillo.servidor).leer(uuid16(FtmsCodec.SERIE)) as ResultadoGatt.Ok).valor, Charsets.UTF_8) }
        assertEquals("0123456789", serie())
        c.publicar(IdentidadDispositivo("02:aa:bb:cc:4F:2A", "9876543210"))
        assertEquals("9876543210", serie())
    }

    @Test
    fun el_rodillo_ofrece_solo_ftms_potencia_pulso_e_informacion() {
        val c = conexion()
        c.publicar(identidad)
        assertEquals(ModoConexion.FTMS_WIFI, c.modo)
        assertEquals(listOf(0x1826, 0x1818, 0x180D, 0x180A).map(::uuid16), checkNotNull(rodillo.servidor).descubrirServicios())
        assertEquals(identidad, rodillo.identidad)
    }

    @Test
    fun sin_pulsometro_configurado_no_se_ofrece_el_pulso() {
        // Spec `platform-bridge`, «Pulso hacia la plataforma», «Sin pulsómetro configurado».
        val c = ConexionFtms(ModoConexion.FTMS_WIFI, registro, rodillo, "0.0-test", receptor, pulso = false)
        c.publicar(identidad)
        assertEquals(listOf(0x1826, 0x1818, 0x180A).map(::uuid16), checkNotNull(rodillo.servidor).descubrirServicios())
        assertEquals(false, c.suscritoPulso)
    }

    @Test
    fun los_comandos_de_ftms_llegan_como_terreno_y_potencia() {
        val c = conexion()
        c.publicar(identidad)
        val s = checkNotNull(rodillo.servidor)
        val control = uuid16(FtmsCodec.PUNTO_CONTROL)
        s.suscribir(control, true)
        val r = s.escribir(control, bytes("11 00 00 90 01 28 33")) as ResultadoGatt.Ok // 4,00 %
        assertEquals(listOf(Terreno(4.0, crr = 0.0051, cw = 0.51, vientoMs = 0.0)).map { it.pendientePct }, receptor.terrenos.map { it.pendientePct })
        assertTrue(r.valor.isNotEmpty(), "se contesta por indicación")
        s.escribir(control, bytes("05 e6 00"))
        assertEquals(listOf(230), receptor.potencias)
        s.escribir(control, bytes("00"))
        assertEquals("sin cambio", receptor.eventos.last())
        assertTrue(receptor.ultimo is UltimoDePlataforma.Comando)
    }

    @Test
    fun un_set_no_se_atiende_en_este_modo() {
        val c = conexion()
        c.publicar(identidad)
        // El servidor del rodillo no tiene el servicio del mando: no hay dónde escribir un `SET`.
        assertFalse(uuid16(0xFC82) in checkNotNull(rodillo.servidor).descubrirServicios())
        assertTrue(receptor.eventos.isEmpty())
    }

    @Test
    fun sin_mando_no_tiene_mando_y_retirar_y_terminar_paran_el_rodillo() {
        val c = conexion()
        assertNull(c.mando)
        c.publicar(identidad)
        c.retirar()
        assertEquals(1, rodillo.paradas)
        c.terminar()
        assertEquals(2, rodillo.paradas)
    }

    @Test
    fun terminar_tambien_termina_el_mando() {
        val mando = MandoFalso()
        val c = conexion(mando = mando)
        assertEquals(mando, c.mando)
        c.publicar(identidad)
        c.retirar()
        assertFalse(mando.terminado, "retirar el anuncio por wifi no toca el mando")
        c.terminar()
        assertTrue(mando.terminado)
    }

    // --- Simular un KICKR BIKE (change simular-kickr-bike) ---

    @Test
    fun sin_simular_se_anuncia_como_smart_bike() {
        val c = conexion()
        c.publicar(IdentidadDispositivo("02:aa:bb:cc:dd:ee", "1"))
        assertEquals("Smart Bike", rodillo.nombre)
    }

    @Test
    fun simulando_usa_su_nombre_y_no_cambia_nada_mas() {
        val c = conexion(simularKickr = true)
        val a = IdentidadDispositivo("02:aa:bb:cc:66:9C", "1")
        c.publicar(a)
        assertEquals("KICKR BIKE PRO 669C", rodillo.nombre)
        assertEquals(a, rodillo.identidad)
        assertEquals(listOf(0x1826, 0x1818, 0x180D, 0x180A).map(::uuid16), checkNotNull(rodillo.servidor).descubrirServicios())
    }

    // --- Rodillo por Bluetooth (change ftms-bluetooth, decisión 2) ---

    @Test
    fun por_bluetooth_ofrece_lo_mismo_con_el_mismo_comando_y_sin_mando() {
        val c = conexion(modo = ModoConexion.FTMS_BLUETOOTH)
        assertEquals(ModoConexion.FTMS_BLUETOOTH, c.modo)
        assertEquals(Enlace.BLUETOOTH, c.enlace)
        assertNull(c.mando)
        c.publicar(identidad)
        assertEquals(listOf(0x1826, 0x1818, 0x180D, 0x180A).map(::uuid16), checkNotNull(rodillo.servidor).descubrirServicios())
        val s = checkNotNull(rodillo.servidor)
        val control = uuid16(FtmsCodec.PUNTO_CONTROL)
        s.suscribir(control, true)
        s.escribir(control, bytes("11 00 00 90 01 28 33"))
        assertEquals(listOf(4.0), receptor.terrenos.map { it.pendientePct })
        s.escribir(control, bytes("05 c8 00"))
        assertEquals(listOf(200), receptor.potencias)
        s.escribir(control, bytes("00"))
        assertEquals("sin cambio", receptor.eventos.last())
    }

    @Test
    fun por_bluetooth_la_telemetria_y_el_pulso_salen_por_el_transporte() {
        val c = conexion(modo = ModoConexion.FTMS_BLUETOOTH)
        c.publicar(identidad)
        val s = checkNotNull(rodillo.servidor)
        s.suscribir(uuid16(FtmsCodec.INDOOR_BIKE_DATA), true)
        s.suscribir(uuid16(FtmsCodec.CP_MEDIDA), true)
        s.suscribir(uuid16(0x2A37), true)
        c.telemetria(TelemetriaPlataforma(vatios = 180, cadenciaRpm = 88, biela = null, velocidadKmh = 0.0))
        c.pulso(140)
        assertTrue(rodillo.enviadasPor(uuid16(FtmsCodec.INDOOR_BIKE_DATA)).isNotEmpty())
        assertTrue(rodillo.enviadasPor(uuid16(FtmsCodec.CP_MEDIDA)).isNotEmpty())
        assertTrue(rodillo.enviadasPor(uuid16(0x2A37)).isNotEmpty())
        assertTrue(c.suscrito)
    }

    @Test
    fun los_modos_por_wifi_llegan_por_wifi() {
        assertEquals(Enlace.WIFI, conexion().enlace)
    }

    // --- «Solo Bluetooth» con el mando dentro y Zwift con su protocolo (change pestana-conexion, decisión 9) ---

    private val rideOn = MandoZwiftCodec.RIDE_ON + byteArrayOf(0x02, 0x04)

    private fun hex(b: ByteArray) = b.joinToString(" ") { "%02x".format(it) }

    /** «Solo Bluetooth» con el mando dentro, Zwift conectado, suscrito como en la prueba del 28-10-2026 y con el mando saludado. */
    private fun TestScope.porBluetoothConMandoSaludado(): ConexionFtms {
        val mando = MandoZwiftEnServidor(backgroundScope, MandoZwift(backgroundScope, registro, rodillo, servicioDelMando = ServicioDelMando.BLUETOOTH), rodillo.estado)
        val c = conexion(mando = mando, modo = ModoConexion.FTMS_BLUETOOTH)
        mando.activar(true)
        c.publicar(identidad)
        runCurrent()
        rodillo.estado.value = EstadoTransporte.Conectado("AA:BB:CC:00:00:04")
        val s = checkNotNull(rodillo.servidor)
        listOf(uuid16(FtmsCodec.INDOOR_BIKE_DATA), uuid16(FtmsCodec.CP_MEDIDA), uuid16(FtmsCodec.PUNTO_CONTROL), ASYNC, SYNC_TX).forEach { s.suscribir(it, true) }
        s.escribir(SYNC_RX, rideOn)
        runCurrent()
        rodillo.enviadas.clear()
        return c
    }

    @Test
    fun zwift_usa_su_protocolo_por_bluetooth() = runTest {
        // Spec `ftms-bluetooth`, «Zwift como rodillo de Zwift por Bluetooth», «Zwift usa su protocolo».
        val c = porBluetoothConMandoSaludado()
        val s = checkNotNull(rodillo.servidor)
        val r = s.escribir(SYNC_RX, bytes("3f 08 01 10 01 22 05 28 90 03 38 00")) as ResultadoGatt.Ok // +2,00 %
        assertEquals(listOf("40 08 00 10 01"), r.valor.map { hex(it.valor) })
        assertEquals(listOf(2.0), receptor.terrenos.map { it.pendientePct })
        c.telemetria(TelemetriaPlataforma(vatios = 120, cadenciaRpm = 80, biela = null, velocidadKmh = 0.0))
        assertEquals(listOf("03 08 78 10 50 18 00 20 78 28 00"), rodillo.enviadasPor(ASYNC).filter { it.startsWith("03") })
        assertTrue(rodillo.enviadasPor(uuid16(FtmsCodec.CP_MEDIDA)).isNotEmpty(), "la potencia sigue saliendo también por FTMS")
    }

    @Test
    fun zwift_usa_ftms_por_bluetooth_y_no_sale_nada_por_su_protocolo() = runTest {
        // Spec `ftms-bluetooth`, «Zwift como rodillo de Zwift por Bluetooth», «Zwift usa FTMS».
        val c = porBluetoothConMandoSaludado()
        checkNotNull(rodillo.servidor).escribir(uuid16(FtmsCodec.PUNTO_CONTROL), bytes("11 00 00 90 01 28 33"))
        assertEquals(listOf(4.0), receptor.terrenos.map { it.pendientePct })
        c.telemetria(TelemetriaPlataforma(vatios = 120, cadenciaRpm = 80, biela = null, velocidadKmh = 0.0))
        assertTrue(rodillo.enviadasPor(ASYNC).none { it.startsWith("03") }, "sin pedaleo por el mando")
        assertTrue(rodillo.enviadasPor(uuid16(FtmsCodec.CP_MEDIDA)).isNotEmpty())
    }

    @Test
    fun zwift_perdido_vuelve_a_ftms_hasta_el_siguiente_set() = runTest {
        val c = porBluetoothConMandoSaludado()
        checkNotNull(rodillo.servidor).escribir(SYNC_RX, bytes("3f 08 01 10 01 22 05 28 90 03 38 00"))
        rodillo.estado.value = EstadoTransporte.Esperando
        runCurrent()
        rodillo.estado.value = EstadoTransporte.Conectado("AA:BB:CC:00:00:04")
        checkNotNull(rodillo.servidor).escribir(SYNC_RX, rideOn)
        runCurrent()
        rodillo.enviadas.clear()
        c.telemetria(TelemetriaPlataforma(vatios = 120, cadenciaRpm = 80, biela = null, velocidadKmh = 0.0))
        assertTrue(rodillo.enviadasPor(ASYNC).none { it.startsWith("03") }, "el cliente nuevo no ha mandado ningún SET")
    }
}
