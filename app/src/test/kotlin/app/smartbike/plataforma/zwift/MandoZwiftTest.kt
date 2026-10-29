package app.smartbike.plataforma.zwift

import app.smartbike.datos.RegistroSesion
import app.smartbike.dominio.FuncionZwift
import app.smartbike.dominio.IdentidadDispositivo
import app.smartbike.plataforma.EstadoMando
import app.smartbike.plataforma.EstadoTransporte
import app.smartbike.plataforma.TransporteDircon
import app.smartbike.plataforma.zwift.MandoZwift.Companion.ASYNC
import app.smartbike.plataforma.zwift.MandoZwift.Companion.SYNC_RX
import app.smartbike.plataforma.zwift.MandoZwift.Companion.SYNC_TX
import app.smartbike.protocolo.DirconCodec
import app.smartbike.protocolo.MandoZwiftCodec
import app.smartbike.protocolo.NotificacionGatt
import app.smartbike.protocolo.PropiedadGatt
import app.smartbike.protocolo.RespuestaInfo
import app.smartbike.protocolo.ResultadoGatt
import app.smartbike.protocolo.ServidorGatt
import java.util.UUID
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/** Spec `zwift-controller`; design.md del change `mando-zwift-emulado`, decisión 5. */
@OptIn(ExperimentalCoroutinesApi::class)
class MandoZwiftTest {
    private val registro = RegistroSesion(relojMonotonico = { 0L }, relojPared = { 0L })
    private val rideOn = MandoZwiftCodec.RIDE_ON + byteArrayOf(0x02, 0x04)

    /** Las dos palancas a 0, que van en cada trama de botones (Zwift conserva el último valor). */
    private val ceros = "1a 04 08 00 10 00 1a 04 08 01 10 00"
    private val reposo = "23 08 ff ff ff ff 0f $ceros"

    private fun hex(b: ByteArray) = b.joinToString(" ") { "%02x".format(it) }

    private class TransporteDePrueba : TransporteDircon {
        override val estado = MutableStateFlow<EstadoTransporte>(EstadoTransporte.Parado)
        var servidor: ServidorGatt? = null
        var publicaciones = 0
        var paradas = 0
        val enviadas = mutableListOf<NotificacionGatt>()
        override fun publicar(servidor: ServidorGatt, identidad: IdentidadDispositivo, nombre: String) {
            this.servidor = servidor
            publicaciones++
            estado.value = EstadoTransporte.Esperando
        }
        override fun parar() {
            paradas++
            estado.value = EstadoTransporte.Parado
        }
        override fun notificar(n: NotificacionGatt) {
            enviadas += n
        }
    }

    private val transporte = TransporteDePrueba()

    private fun enviadasPor(c: UUID) = transporte.enviadas.filter { it.caracteristica == c }.map { hex(it.valor) }

    private fun TestScope.mando() = MandoZwift(backgroundScope, registro, transporte).also {
        runCurrent()
        it.publicar()
        runCurrent()
    }

    /** Zwift se conecta como lo hace el transporte: usa el servidor y se suscribe a ASYNC y SYNC_TX. */
    private fun TestScope.conectar(m: MandoZwift) {
        transporte.estado.value = EstadoTransporte.Conectado("AA:BB")
        listOf(ASYNC, SYNC_TX).forEach { m.servidor.suscribir(it, true) }
        runCurrent()
    }

    private fun MandoZwift.handshake(): List<NotificacionGatt> = (servidor.escribir(SYNC_RX, rideOn) as ResultadoGatt.Ok).valor

    private fun TestScope.saludado(): MandoZwift = mando().also {
        conectar(it)
        it.handshake()
    }

    @Test
    fun publica_una_vez_el_mando_la_informacion_y_la_bateria() = runTest {
        val m = mando()
        m.publicar()
        // Publicarlo en el transporte es cosa de quien lo usa, no del mando.
        assertEquals(0, transporte.publicaciones)
        assertEquals(EstadoMando.Esperando, m.estado.value)
        assertEquals(listOf(MandoZwift.SERVICIO, MandoZwift.INFORMACION, MandoZwift.BATERIA), m.servidor.descubrirServicios())
        assertEquals(5, (m.servidor.descubrirCaracteristicas(MandoZwift.SERVICIO) as ResultadoGatt.Ok).valor.size)
        assertEquals("64", hex((m.servidor.leer(MandoZwift.NIVEL_BATERIA) as ResultadoGatt.Ok).valor))
    }

    @Test
    fun sin_publicar_esta_desactivado() = runTest {
        val m = MandoZwift(backgroundScope, registro, transporte)
        assertEquals(EstadoMando.Desactivado, m.estado.value)
        assertEquals(0, transporte.publicaciones)
    }

    @Test
    fun handshake_y_respuesta() = runTest {
        val m = mando()
        conectar(m)
        assertEquals(EstadoMando.Esperando, m.estado.value)
        val respuesta = m.handshake()
        assertEquals(listOf("52 69 64 65 4f 6e"), respuesta.map { hex(it.valor) })
        assertEquals(SYNC_TX, respuesta.single().caracteristica)
        assertEquals(EstadoMando.Conectado, m.estado.value)
        assertEquals("52 69 64 65 4f 6e", hex((m.servidor.leer(SYNC_TX) as ResultadoGatt.Ok).valor))
    }

    @Test
    fun otra_escritura_no_hace_handshake() = runTest {
        val m = mando()
        conectar(m)
        assertTrue((m.servidor.escribir(SYNC_RX, byteArrayOf(0x41, 0x08, 0x05)) as ResultadoGatt.Ok).valor.isEmpty())
        assertEquals(EstadoMando.Esperando, m.estado.value)
    }

    @Test
    fun peticion_de_informacion_con_las_paginas_del_ride() = runTest {
        val m = saludado()
        for (id in listOf(0L, 16L, 1024L, 771L)) {
            val peticion = byteArrayOf(0x00, 0x08) + varint(id)
            val r = (m.servidor.escribir(SYNC_RX, peticion) as ResultadoGatt.Ok).valor
            assertEquals(listOf(hex(MandoZwiftCodec.respuestaInfo(RespuestaInfo.PAGINA_RIDE, id)!!)), r.map { hex(it.valor) }, "página $id")
            assertTrue(r.all { it.caracteristica == SYNC_TX })
        }
    }

    private fun respuestaA(m: MandoZwift, id: Long): String {
        val r = (m.servidor.escribir(SYNC_RX, byteArrayOf(0x00, 0x08) + varint(id)) as ResultadoGatt.Ok).valor
        return hex(r.single().valor)
    }

    @Test
    fun las_paginas_1024_y_771_se_contestan_con_datos() = runTest {
        val m = saludado()
        assertEquals("3c 08 80 08 12 07 0a 05 08 ff 23 10 00", respuestaA(m, 1024))
        assertEquals("3c 08 83 06 12 0a 0a 08 08 00 10 64 18 00 20 00", respuestaA(m, 771))
    }

    @Test
    fun el_registro_anota_si_la_respuesta_llevo_datos() = runTest {
        val m = saludado()
        respuestaA(m, 1024)
        respuestaA(m, 7)
        val textos = registro.ultimas(100).map { it.texto }
        assertTrue(textos.any { "información 1024 respondida con datos" in it }, "$textos")
        assertTrue(textos.any { "información 7 respondida solo con el identificador" in it }, "$textos")
    }

    @Test
    fun nada_antes_del_handshake() = runTest {
        val m = mando()
        conectar(m)
        m.pulsar(FuncionZwift.ABAJO)
        advanceTimeBy(20_000)
        runCurrent()
        assertEquals(setOf(FuncionZwift.ABAJO), m.pulsadas)
        assertTrue(transporte.enviadas.isEmpty())
    }

    @Test
    fun cada_cambio_sale_al_instante_por_async() = runTest {
        val m = saludado()
        m.pulsar(FuncionZwift.DERECHA)
        m.pulsar(FuncionZwift.A)
        m.soltar(FuncionZwift.A)
        m.pulsar(FuncionZwift.FRENO_DERECHO)
        m.soltar(FuncionZwift.FRENO_DERECHO)
        m.soltar(FuncionZwift.DERECHA)
        assertEquals(
            listOf(
                "23 08 fb ff ff ff 0f $ceros",
                "23 08 eb ff ff ff 0f $ceros",
                "23 08 fb ff ff ff 0f $ceros",
                "23 08 fb ff ff ff 0f 1a 04 08 00 10 00 1a 05 08 01 10 c7 01",
                "23 08 fb ff ff ff 0f $ceros",
                reposo,
            ),
            enviadasPor(ASYNC),
        )
    }

    @Test
    fun la_misma_funcion_por_dos_entradas_se_envia_una_vez() = runTest {
        val m = saludado()
        m.pulsar(FuncionZwift.Z)
        m.pulsar(FuncionZwift.Z)
        m.soltar(FuncionZwift.Z)
        assertEquals(listOf("23 08 ff fe ff ff 0f $ceros"), enviadasPor(ASYNC))
        m.soltar(FuncionZwift.Z)
        assertEquals(reposo, enviadasPor(ASYNC).last())
    }

    @Test
    fun keepalive_con_el_estado_actual_por_async() = runTest {
        val m = saludado()
        advanceTimeBy(4_000)
        m.pulsar(FuncionZwift.GIRAR_IZQUIERDA)
        advanceTimeBy(4_000)
        runCurrent()
        assertEquals(1, enviadasPor(ASYNC).size)
        advanceTimeBy(1_001)
        runCurrent()
        val giro = "23 08 ff ff ff ff 0f 1a 05 08 00 10 c7 01 1a 04 08 01 10 00"
        assertEquals(listOf(giro, giro), enviadasPor(ASYNC))
        assertTrue(enviadasPor(SYNC_TX).isEmpty())
    }

    @Test
    fun diez_minutos_sin_tocar_nada() = runTest {
        val m = saludado()
        advanceTimeBy(10 * 60_000L + 1)
        runCurrent()
        val keepalives = enviadasPor(ASYNC)
        assertEquals(120, keepalives.size)
        assertTrue(keepalives.all { it == reposo })
        transporte.enviadas.clear()
        m.pulsar(FuncionZwift.A)
        assertEquals(listOf("23 08 ef ff ff ff 0f $ceros"), enviadasPor(ASYNC))
    }

    @Test
    fun sin_suscripcion_no_envia() = runTest {
        val m = mando()
        transporte.estado.value = EstadoTransporte.Conectado("AA:BB")
        runCurrent()
        m.handshake()
        m.pulsar(FuncionZwift.A)
        assertTrue(transporte.enviadas.isEmpty())
    }

    @Test
    fun cliente_perdido_suelta_todo_y_olvida_el_handshake() = runTest {
        val m = saludado()
        m.pulsar(FuncionZwift.FRENO_IZQUIERDO)
        m.servidor.clientePerdido()
        transporte.estado.value = EstadoTransporte.Esperando
        runCurrent()
        assertEquals(EstadoMando.Esperando, m.estado.value)
        assertTrue(m.pulsadas.isEmpty())
        transporte.enviadas.clear()
        advanceTimeBy(20_000)
        runCurrent()
        assertTrue(transporte.enviadas.isEmpty(), "sin handshake no hay keepalive")
        // El siguiente cliente vuelve a hacer el handshake y no hay nada pulsado.
        conectar(m)
        m.handshake()
        advanceTimeBy(5_001)
        runCurrent()
        assertEquals(listOf(reposo), enviadasPor(ASYNC))
    }

    @Test
    fun soltar_todo() = runTest {
        val m = saludado()
        m.pulsar(FuncionZwift.GIRAR_DERECHA)
        m.pulsar(FuncionZwift.B)
        m.soltarTodo()
        assertTrue(m.pulsadas.isEmpty())
        assertEquals(reposo, enviadasPor(ASYNC).last())
    }

    @Test
    fun parar_detiene_el_keepalive_sin_tocar_el_transporte() = runTest {
        val m = saludado()
        m.pulsar(FuncionZwift.Y)
        m.parar()
        assertEquals(EstadoMando.Desactivado, m.estado.value)
        assertEquals(0, transporte.paradas)
        assertTrue(m.pulsadas.isEmpty())
        transporte.enviadas.clear()
        advanceTimeBy(20_000)
        runCurrent()
        assertTrue(transporte.enviadas.isEmpty())
        // Se puede volver a publicar.
        m.publicar()
        assertEquals(EstadoMando.Esperando, m.estado.value)
    }

    private fun varint(v: Long): ByteArray {
        val out = mutableListOf<Byte>()
        var x = v
        do {
            var b = (x and 0x7F).toInt()
            x = x ushr 7
            if (x != 0L) b = b or 0x80
            out += b.toByte()
        } while (x != 0L)
        return out.toByteArray()
    }

    // --- Dentro del rodillo (change `mando-zwift-dircon`) ---

    private fun bytes(h: String) = h.split(" ").map { it.toInt(16).toByte() }.toByteArray()

    private fun TestScope.mandoEnRodillo(): MandoZwift {
        val m = MandoZwift(backgroundScope, registro, transporte, servicioDelMando = ServicioDelMando.EN_RODILLO)
        // El puente monta el servicio del mando en su servidor y se lo pasa.
        m.usarServidor(ServidorGatt(listOf(m.servicio)))
        runCurrent()
        m.publicar()
        runCurrent()
        return m
    }

    @Test
    fun en_el_rodillo_el_servicio_es_fc82_con_tres_caracteristicas() = runTest {
        val m = mandoEnRodillo()
        val fc82 = DirconCodec.uuid16(0xFC82)
        assertEquals(listOf(fc82), m.servidor.descubrirServicios())
        val c = (m.servidor.descubrirCaracteristicas(fc82) as ResultadoGatt.Ok).valor
        assertEquals(listOf(ASYNC, SYNC_RX, SYNC_TX), c.map { it.uuid })
        assertEquals(setOf(PropiedadGatt.Notificar), c.first { it.uuid == SYNC_TX }.propiedades)
        assertEquals(setOf(PropiedadGatt.Escribir), c.first { it.uuid == SYNC_RX }.propiedades)
    }

    @Test
    fun el_mando_nunca_publica_ni_para_el_transporte() = runTest {
        val m = mandoEnRodillo()
        assertEquals(EstadoMando.Esperando, m.estado.value)
        m.parar()
        assertEquals(EstadoMando.Desactivado, m.estado.value)
        assertEquals(0, transporte.publicaciones)
        assertEquals(0, transporte.paradas)
    }

    @Test
    fun en_el_rodillo_handshake_informacion_y_keepalive_como_siempre() = runTest {
        val m = mandoEnRodillo()
        conectar(m)
        assertEquals(listOf("52 69 64 65 4f 6e"), m.handshake().map { hex(it.valor) })
        assertTrue(m.saludado)
        assertEquals(EstadoMando.Conectado, m.estado.value)
        val r = (m.servidor.escribir(SYNC_RX, bytes("00 08 00")) as ResultadoGatt.Ok).valor
        assertEquals(listOf(hex(MandoZwiftCodec.respuestaInfo(RespuestaInfo.PAGINA_RIDE, 0)!!)), r.map { hex(it.valor) })
        transporte.enviadas.clear()
        advanceTimeBy(MandoZwift.KEEPALIVE_MS + 1)
        runCurrent()
        assertEquals(listOf(reposo), enviadasPor(ASYNC))
    }

    @Test
    fun lo_que_el_mando_no_entiende_va_a_quien_lo_use() = runTest {
        val m = mandoEnRodillo()
        conectar(m)
        m.handshake()
        // Sin nadie que lo atienda, se ignora.
        assertTrue((m.servidor.escribir(SYNC_RX, bytes("3f 08 01")) as ResultadoGatt.Ok).valor.isEmpty())
        val recibidos = mutableListOf<String>()
        m.alEscrituraDesconocida = { v ->
            recibidos += hex(v)
            if (v[0] == 0x3f.toByte()) listOf(NotificacionGatt(SYNC_TX, byteArrayOf(0x40))) else null
        }
        val r = (m.servidor.escribir(SYNC_RX, bytes("3f 08 01")) as ResultadoGatt.Ok).valor
        assertEquals(listOf("40"), r.map { hex(it.valor) })
        assertTrue((m.servidor.escribir(SYNC_RX, bytes("41 08 05")) as ResultadoGatt.Ok).valor.isEmpty())
        assertEquals(listOf("3f 08 01", "41 08 05"), recibidos)
    }

    @Test
    fun el_handshake_y_la_informacion_no_pasan_por_el_punto_de_extension() = runTest {
        val m = mandoEnRodillo()
        conectar(m)
        var n = 0
        m.alEscrituraDesconocida = {
            n++
            null
        }
        m.handshake()
        m.servidor.escribir(SYNC_RX, bytes("00 08 00"))
        assertEquals(0, n)
    }

    @Test
    fun notificar_por_async_solo_llega_a_quien_esta_suscrito() = runTest {
        val m = mandoEnRodillo()
        m.notificarAsync(bytes("03 08 01"))
        assertTrue(transporte.enviadas.isEmpty())
        conectar(m)
        transporte.enviadas.clear()
        m.notificarAsync(bytes("03 08 01"))
        assertEquals(listOf("03 08 01"), enviadasPor(ASYNC))
    }
}
