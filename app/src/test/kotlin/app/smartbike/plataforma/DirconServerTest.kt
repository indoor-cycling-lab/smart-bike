package app.smartbike.plataforma

import app.smartbike.datos.RegistroSesion
import app.smartbike.dominio.IdentidadDispositivo
import app.smartbike.protocolo.CaracteristicaGatt
import app.smartbike.protocolo.DirconCodec
import app.smartbike.protocolo.NotificacionGatt
import app.smartbike.protocolo.PropiedadGatt
import app.smartbike.protocolo.ServicioGatt
import app.smartbike.protocolo.ServidorGatt
import java.net.Socket
import java.net.SocketTimeoutException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DirconServerTest {
    private val ambito = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val hilo = Dispatchers.Default.limitedParallelism(1)
    private val registro = RegistroSesion(relojMonotonico = { 0L }, relojPared = { 0L })

    private class AnunciadorFalso : Anunciador {
        var anuncio: Triple<String, Int, Map<String, String>>? = null
        var retiradas = 0
        var anuncios = 0
        override suspend fun anunciar(nombre: String, tipo: String, puerto: Int, registros: Map<String, String>): Boolean {
            anuncio = Triple("$nombre $tipo", puerto, registros)
            anuncios++
            return true
        }
        override fun retirar() {
            retiradas++
        }
    }

    private val anunciador = AnunciadorFalso()

    /** Devuelve los [resultados] en orden y después repite el último; cuenta las comprobaciones. */
    private class SondaFalsa(vararg resultados: ResultadoSonda) : SondaAnuncio {
        private val pendientes = ArrayDeque(resultados.toList())

        @Volatile var ultimo: ResultadoSonda = resultados.last()

        @Volatile var comprobaciones = 0

        @Volatile var instanciaComprobada: String? = null
        override suspend fun comprobar(instancia: String, tipo: String, puerto: Int): ResultadoSonda {
            comprobaciones++
            instanciaComprobada = instancia
            return (pendientes.removeFirstOrNull() ?: ultimo).also { ultimo = it }
        }
    }

    private val responde = ResultadoSonda.Responde(2, "multicast")
    private val sin = ResultadoSonda.SinRespuesta
    private val ftms = DirconCodec.uuid16(0x1826)
    private val feature = DirconCodec.uuid16(0x2ACC)
    private val datos = DirconCodec.uuid16(0x2AD2)
    private val gatt = ServidorGatt(
        listOf(
            ServicioGatt(
                ftms,
                listOf(
                    CaracteristicaGatt(feature, setOf(PropiedadGatt.Leer), leer = { byteArrayOf(0x02, 0x40, 0, 0) }),
                    CaracteristicaGatt(datos, setOf(PropiedadGatt.Notificar)),
                ),
            ),
        ),
    )
    private val servidor = DirconServer(ambito, hilo, registro, anunciador, { "127.0.0.1" }, puertos = listOf(0), ajustarSocket = { null })

    @AfterEach
    fun cerrar() {
        servidor.parar()
        ambito.cancel()
    }

    private fun hex(b: ByteArray) = b.joinToString(" ") { "%02x".format(it) }

    private class ClientePrueba(puerto: Int) {
        val socket = Socket("127.0.0.1", puerto).apply { soTimeout = 3_000 }
        private val lector = DirconCodec.Lector()
        private val pendientes = ArrayDeque<DirconCodec.Mensaje>()

        fun enviar(m: DirconCodec.Mensaje) = socket.getOutputStream().write(DirconCodec.codificar(m))

        fun recibir(): DirconCodec.Mensaje {
            val buf = ByteArray(512)
            while (pendientes.isEmpty()) {
                val n = socket.getInputStream().read(buf)
                check(n >= 0) { "Conexión cerrada" }
                pendientes += lector.anadir(buf, n)
            }
            return pendientes.removeFirst()
        }

        /**
         * Si el servidor cierra la conexión antes de [limiteMs]. Descarta lo que llegue antes del cierre y no se queda
         * en una sola lectura: en el runner de GitHub, más lento, el cierre puede tardar más que el `soTimeout`.
         */
        fun cerrado(limiteMs: Long = 10_000): Boolean {
            val fin = System.nanoTime() + limiteMs * 1_000_000
            while (System.nanoTime() < fin) {
                try {
                    if (socket.getInputStream().read() < 0) return true
                } catch (_: SocketTimeoutException) {
                    // Sigue esperando hasta el límite.
                }
            }
            return false
        }
    }

    private fun mensaje(tipo: Int, sec: Int, contenido: ByteArray = ByteArray(0)) = DirconCodec.Mensaje(tipo, sec, 0, contenido)

    private fun publicado(): Int = runBlocking {
        withContext(hilo) { servidor.publicar(gatt, IdentidadDispositivo("02:11:22:33:44:55", "0123456789")) }
        withTimeout(3_000) { servidor.estado.first { it == EstadoTransporte.Esperando } }
        checkNotNull(servidor.puerto)
    }

    private fun esperar(e: EstadoTransporte) = runBlocking { withTimeout(3_000) { servidor.estado.first { it == e } } }

    @Test
    fun anuncia_con_los_registros() {
        val puerto = publicado()
        val (nombre, p, txt) = checkNotNull(anunciador.anuncio)
        assertEquals("Smart Bike _wahoo-fitness-tnp._tcp", nombre)
        assertEquals(puerto, p)
        assertEquals(mapOf("ble-service-uuids" to "0x1826", "mac-address" to "02:11:22:33:44:55", "serial-number" to "0123456789"), txt)
    }

    @Test
    fun uuid_cortos_con_prefijo() {
        val fc82 = ServicioGatt(java.util.UUID.fromString("0000fc82-0000-1000-8000-00805f9b34fb"), emptyList())
        val conPrefijo = DirconServer.registros(ServidorGatt(gatt.servicios + fc82), IdentidadDispositivo("02:11:22:33:44:55", "0123456789"))
        assertEquals("0x1826,0xFC82", conPrefijo["ble-service-uuids"])
    }

    @Test
    fun anuncia_con_otro_nombre_y_servicios_extra() = runBlocking {
        val mando = java.util.UUID.fromString("00000001-19ca-4651-86e5-fa29dcdd09d1")
        val conMando = ServidorGatt(gatt.servicios + ServicioGatt(mando, emptyList()))
        val otro = DirconServer(
            ambito,
            hilo,
            registro,
            anunciador,
            { "127.0.0.1" },
            puertos = listOf(0),
            ajustarSocket = { null },
            serviciosExtra = listOf("0xFC82"),
        )
        try {
            withContext(hilo) { otro.publicar(conMando, IdentidadDispositivo("02:11:22:33:44:55", "0123456789"), "Smart Bike Mando") }
            withTimeout(3_000) { otro.estado.first { it == EstadoTransporte.Esperando } }
            val (nombre, _, txt) = checkNotNull(anunciador.anuncio)
            assertEquals("Smart Bike Mando _wahoo-fitness-tnp._tcp", nombre)
            assertEquals("0x1826,00000001-19CA-4651-86E5-FA29DCDD09D1,0xFC82", txt["ble-service-uuids"])
        } finally {
            otro.parar()
        }
    }

    @Test
    fun descubrir_leer_suscribir_y_notificar() {
        val c = ClientePrueba(publicado())
        esperar(EstadoTransporte.Conectado("127.0.0.1"))

        c.enviar(mensaje(DirconCodec.DESCUBRIR_SERVICIOS, 1))
        val s = c.recibir()
        assertEquals(1, s.secuencia)
        assertEquals(ftms, DirconCodec.uuid(s.contenido, 0))

        c.enviar(mensaje(DirconCodec.LEER, 2, DirconCodec.bytes(feature)))
        assertEquals("02 40 00 00", hex(c.recibir().contenido.copyOfRange(16, 20)))

        c.enviar(mensaje(DirconCodec.NOTIFICACIONES, 3, DirconCodec.bytes(datos) + byteArrayOf(1)))
        assertEquals(DirconCodec.EXITO, c.recibir().codigo)

        runBlocking(hilo) { servidor.notificar(checkNotNull(gatt.notificar(datos, byteArrayOf(0x44, 0)))) }
        val n = c.recibir()
        assertEquals(DirconCodec.NOTIFICACION, n.tipo)
        assertEquals("44 00", hex(n.contenido.copyOfRange(16, 18)))
    }

    @Test
    fun el_dircon_contesta_la_serie_el_firmware_y_el_hardware_y_los_anota() {
        val puente = PuenteFtms("1.2.3", registro = registro) { 0 }
        val identidad = IdentidadDispositivo("02:11:22:33:44:55", "0123456789")
        puente.fijarIdentidad(identidad)
        runBlocking {
            withContext(hilo) { servidor.publicar(puente.servidor, identidad) }
            withTimeout(3_000) { servidor.estado.first { it == EstadoTransporte.Esperando } }
        }
        val c = ClientePrueba(checkNotNull(servidor.puerto))
        esperar(EstadoTransporte.Conectado("127.0.0.1"))
        for ((id, valor) in listOf(0x2A25 to "0123456789", 0x2A26 to "1.2.3", 0x2A27 to "1.0")) {
            c.enviar(mensaje(DirconCodec.LEER, id, DirconCodec.bytes(DirconCodec.uuid16(id))))
            val r = c.recibir()
            assertEquals(DirconCodec.EXITO, r.codigo, "0x${id.toString(16)}")
            assertEquals(valor, String(r.contenido.copyOfRange(16, r.contenido.size), Charsets.UTF_8))
        }
        val textos = sucesos()
        assertTrue(textos.any { "serie" in it && "0123456789" in it }, "$textos")
    }

    @Test
    fun el_pulso_llega_como_notificacion_dircon_sin_tocar_la_potencia() {
        val puente = PuenteFtms("0.0-test") { 0 }
        runBlocking {
            withContext(hilo) { servidor.publicar(puente.servidor, IdentidadDispositivo("02:11:22:33:44:55", "0123456789")) }
            withTimeout(3_000) { servidor.estado.first { it == EstadoTransporte.Esperando } }
        }
        val c = ClientePrueba(checkNotNull(servidor.puerto))
        esperar(EstadoTransporte.Conectado("127.0.0.1"))

        c.enviar(mensaje(DirconCodec.DESCUBRIR_SERVICIOS, 1))
        val servicios = c.recibir().contenido.toList().chunked(16).map { it.toByteArray() }.map { DirconCodec.uuid(it, 0) }
        assertTrue(DirconCodec.uuid16(0x180D) in servicios)

        val pulso = DirconCodec.uuid16(0x2A37)
        val potencia = DirconCodec.uuid16(0x2A63)
        c.enviar(mensaje(DirconCodec.NOTIFICACIONES, 2, DirconCodec.bytes(pulso) + byteArrayOf(1)))
        assertEquals(DirconCodec.EXITO, c.recibir().codigo)
        c.enviar(mensaje(DirconCodec.NOTIFICACIONES, 3, DirconCodec.bytes(potencia) + byteArrayOf(1)))
        assertEquals(DirconCodec.EXITO, c.recibir().codigo)

        runBlocking(hilo) {
            servidor.notificar(checkNotNull(puente.pulso(132)))
            puente.telemetria(TelemetriaPlataforma(200, 90, null, 25.0)).forEach { servidor.notificar(it) }
        }
        val n = c.recibir()
        assertEquals(DirconCodec.NOTIFICACION, n.tipo)
        assertEquals(pulso, DirconCodec.uuid(n.contenido, 0))
        assertEquals("00 84", hex(n.contenido.copyOfRange(16, 18)))
        assertEquals(potencia, DirconCodec.uuid(c.recibir().contenido, 0))
    }

    @Test
    fun error_si_no_existe() {
        val c = ClientePrueba(publicado())
        c.enviar(mensaje(DirconCodec.LEER, 4, DirconCodec.bytes(ftms)))
        assertEquals(DirconCodec.CARACTERISTICA_NO_ENCONTRADA, c.recibir().codigo)
    }

    @Test
    fun un_cliente_nuevo_sustituye_al_anterior_y_empieza_sin_suscripciones() {
        val puerto = publicado()
        val c1 = ClientePrueba(puerto)
        c1.enviar(mensaje(DirconCodec.NOTIFICACIONES, 1, DirconCodec.bytes(datos) + byteArrayOf(1)))
        c1.recibir()
        val c2 = ClientePrueba(puerto)
        assertTrue(c1.cerrado())
        // El estado ya decía «Conectado 127.0.0.1» por c1: la respuesta a c2 es la que asegura que el servidor ya
        // atiende a c2 y ha borrado las suscripciones de c1 (lo hace antes de leer su primer mensaje).
        c2.enviar(mensaje(DirconCodec.DESCUBRIR_SERVICIOS, 9))
        assertEquals(9, c2.recibir().secuencia)
        esperar(EstadoTransporte.Conectado("127.0.0.1"))
        runBlocking(hilo) { assertNull(gatt.notificar(datos, byteArrayOf(1))) }
    }

    @Test
    fun cierre_del_cliente_vuelve_a_esperar_y_parar_retira_el_anuncio() {
        val c = ClientePrueba(publicado())
        esperar(EstadoTransporte.Conectado("127.0.0.1"))
        c.socket.close()
        esperar(EstadoTransporte.Esperando)
        runBlocking(hilo) { servidor.parar() }
        esperar(EstadoTransporte.Parado)
        assertEquals(1, anunciador.retiradas)
    }

    @Test
    fun publicar_otra_vez_tras_parar() {
        publicado()
        runBlocking(hilo) { servidor.parar() }
        esperar(EstadoTransporte.Parado)
        val c = ClientePrueba(publicado())
        c.enviar(mensaje(DirconCodec.DESCUBRIR_SERVICIOS, 5))
        assertEquals(5, c.recibir().secuencia)
    }

    private var ahora = 0L

    private var ipCliente = "127.0.0.1"

    private fun servidorConReloj(direccion: () -> String? = { "127.0.0.1" }) = DirconServer(
        ambito,
        hilo,
        registro,
        anunciador,
        direccion,
        puertos = listOf(0),
        ajustarSocket = { null },
        reloj = { ahora },
        ipDe = { ipCliente },
    )

    private fun publicarEn(s: DirconServer): Int = runBlocking {
        withContext(hilo) { s.publicar(gatt, IdentidadDispositivo("02:11:22:33:44:55", "0123456789")) }
        withTimeout(3_000) { s.estado.first { it == EstadoTransporte.Esperando } }
        checkNotNull(s.puerto)
    }

    @Test
    fun escucha_en_la_direccion_de_la_wifi_y_lo_anota() {
        val s = servidorConReloj()
        try {
            publicarEn(s)
            assertTrue(sucesos().any { it.startsWith("Anunciado") && "127.0.0.1:${s.puerto}" in it }, "${sucesos()}")
            assertEquals("127.0.0.1", s.direccionEscucha)
        } finally {
            runBlocking(hilo) { s.parar() }
        }
    }

    @Test
    fun sin_direccion_de_wifi_no_se_abre() {
        val s = servidorConReloj({ null })
        runBlocking(hilo) { s.publicar(gatt, IdentidadDispositivo("02:11:22:33:44:55", "0123456789")) }
        Thread.sleep(200)
        assertNull(s.puerto)
        assertEquals(EstadoTransporte.Parado, s.estado.value)
        assertEquals(0, anunciador.anuncios)
    }

    @Test
    fun publicar_con_otra_direccion_cierra_y_vuelve_a_abrir() {
        var direccion: String? = "127.0.0.1"
        val s = servidorConReloj({ direccion })
        try {
            val primero = publicarEn(s)
            assertEquals(1, anunciador.anuncios)
            direccion = "0.0.0.0"
            runBlocking(hilo) { s.publicar(gatt, IdentidadDispositivo("02:11:22:33:44:55", "0123456789")) }
            runBlocking { withTimeout(3_000) { while (anunciador.anuncios < 2) kotlinx.coroutines.delay(10) } }
            assertEquals("0.0.0.0", s.direccionEscucha)
            assertEquals(1, anunciador.retiradas)
            assertTrue(primero > 0)
        } finally {
            runBlocking(hilo) { s.parar() }
        }
    }

    @Test
    fun el_segundo_cliente_de_la_misma_ip_sustituye_al_primero() {
        val s = servidorConReloj()
        try {
            val puerto = publicarEn(s)
            val c1 = ClientePrueba(puerto)
            c1.enviar(mensaje(DirconCodec.DESCUBRIR_SERVICIOS, 1))
            c1.recibir()
            val c2 = ClientePrueba(puerto)
            assertTrue(c1.cerrado())
            c2.enviar(mensaje(DirconCodec.DESCUBRIR_SERVICIOS, 2))
            assertEquals(2, c2.recibir().secuencia)
        } finally {
            runBlocking(hilo) { s.parar() }
        }
    }

    @Test
    fun la_politica_rechaza_a_otra_ip_mientras_el_actual_envia_y_la_acepta_tras_el_silencio() {
        val s = servidorConReloj()
        try {
            val puerto = publicarEn(s)
            ipCliente = "10.0.0.1"
            val c1 = ClientePrueba(puerto)
            c1.enviar(mensaje(DirconCodec.DESCUBRIR_SERVICIOS, 1))
            c1.recibir()
            ipCliente = "10.0.0.2"
            ahora += 5_000
            val c2 = ClientePrueba(puerto)
            assertTrue(c2.cerrado(), "el segundo debe quedar rechazado")
            runBlocking { withTimeout(3_000) { while (sucesos().none { it.startsWith("DirCon: rechazado 10.0.0.2") }) kotlinx.coroutines.delay(10) } }
            c1.enviar(mensaje(DirconCodec.DESCUBRIR_SERVICIOS, 3))
            assertEquals(3, c1.recibir().secuencia)
            ahora += 15_000
            val c3 = ClientePrueba(puerto)
            assertTrue(c1.cerrado())
            c3.enviar(mensaje(DirconCodec.DESCUBRIR_SERVICIOS, 4))
            assertEquals(4, c3.recibir().secuencia)
        } finally {
            runBlocking(hilo) { s.parar() }
        }
    }

    @Test
    fun el_vigilante_cierra_el_socket_si_una_escritura_se_bloquea() {
        val liberar = java.util.concurrent.CountDownLatch(1)
        val s = DirconServer(
            ambito,
            hilo,
            registro,
            anunciador,
            { "127.0.0.1" },
            puertos = listOf(0),
            ajustarSocket = { null },
            reloj = { System.nanoTime() / 1_000_000 },
            salidaDe = { socket ->
                object : java.io.OutputStream() {
                    override fun write(b: Int) = write(byteArrayOf(b.toByte()))
                    override fun write(b: ByteArray, off: Int, len: Int) {
                        // Bloqueado hasta que el servidor cierra el socket, como una escritura real sin acuse.
                        while (!socket.isClosed) Thread.sleep(10)
                        liberar.countDown()
                        throw java.io.IOException("Socket cerrado")
                    }
                }
            },
            escrituraMaxMs = 200,
            periodoVigilanteMs = 50,
        )
        try {
            val puerto = publicarEn(s)
            val c = ClientePrueba(puerto)
            esperarDe(s, EstadoTransporte.Conectado("127.0.0.1"))
            c.enviar(mensaje(DirconCodec.DESCUBRIR_SERVICIOS, 1))
            assertTrue(liberar.await(3, java.util.concurrent.TimeUnit.SECONDS), "el socket debería cerrarse")
            runBlocking { withTimeout(3_000) { while (sucesos().none { it.startsWith("Plataforma desconectada") }) kotlinx.coroutines.delay(10) } }
            assertTrue(sucesos().any { "escritura bloqueada" in it })
        } finally {
            runBlocking(hilo) { s.parar() }
        }
    }

    private fun esperarDe(s: DirconServer, e: EstadoTransporte) = runBlocking { withTimeout(3_000) { s.estado.first { it == e } } }

    @Test
    fun una_rafaga_de_200_mensajes_desconecta_y_la_secuencia_de_zwift_no() {
        val s = servidorConReloj()
        try {
            val puerto = publicarEn(s)
            val zwift = ClientePrueba(puerto)
            repeat(30) { zwift.enviar(mensaje(DirconCodec.DESCUBRIR_SERVICIOS, it)) }
            repeat(30) { zwift.recibir() }
            assertTrue(sucesos().none { "exceso de mensajes" in it })
            val ruidoso = ClientePrueba(puerto)
            val paquete = DirconCodec.codificar(mensaje(DirconCodec.DESCUBRIR_SERVICIOS, 1))
            ruidoso.socket.getOutputStream().write(ByteArray(0).let { paquete.let { p -> ByteArray(p.size * 200) { i -> p[i % p.size] } } })
            runBlocking { withTimeout(3_000) { while (sucesos().none { "exceso de mensajes" in it }) kotlinx.coroutines.delay(10) } }
        } finally {
            runBlocking(hilo) { s.parar() }
        }
    }

    @Test
    fun un_servidor_gatt_que_lanza_al_escribir_responde_error_inesperado_y_la_conexion_sigue() {
        val malo = ServidorGatt(
            listOf(
                ServicioGatt(
                    ftms,
                    listOf(CaracteristicaGatt(datos, setOf(PropiedadGatt.Escribir), alEscribir = { error("fallo") })),
                ),
            ),
        )
        val s = servidorConReloj()
        try {
            runBlocking {
                withContext(hilo) { s.publicar(malo, IdentidadDispositivo("02:11:22:33:44:55", "0123456789")) }
                withTimeout(3_000) { s.estado.first { it == EstadoTransporte.Esperando } }
            }
            val c = ClientePrueba(checkNotNull(s.puerto))
            c.enviar(mensaje(DirconCodec.ESCRIBIR, 1, DirconCodec.bytes(datos) + byteArrayOf(1)))
            val r = c.recibir()
            assertEquals(DirconCodec.ERROR_INESPERADO, r.codigo)
            c.enviar(mensaje(DirconCodec.DESCUBRIR_SERVICIOS, 2))
            assertEquals(2, c.recibir().secuencia)
        } finally {
            runBlocking(hilo) { s.parar() }
        }
    }

    private fun conSonda(sonda: SondaAnuncio, prueba: DirconServer.() -> Unit) {
        val s = DirconServer(ambito, hilo, registro, anunciador, { "127.0.0.1" }, puertos = listOf(0), ajustarSocket = { null }, sonda = sonda, periodoComprobacionMs = 100)
        try {
            runBlocking(hilo) { s.publicar(gatt, IdentidadDispositivo("02:11:22:33:44:55", "0123456789")) }
            s.prueba()
        } finally {
            runBlocking(hilo) { s.parar() }
        }
    }

    private fun DirconServer.esperarEstado(e: EstadoTransporte) = runBlocking { withTimeout(3_000) { estado.first { it == e } } }

    private fun sucesos() = registro.ultimas(1000).map { it.texto }

    private fun esperarSucesos(texto: String, n: Int = 1) = runBlocking {
        withTimeout(3_000) { while (sucesos().count { it.startsWith(texto) } < n) kotlinx.coroutines.delay(10) }
    }

    @Test
    fun anuncio_que_responde_sigue_esperando() = conSonda(SondaFalsa(responde)) {
        esperarSucesos("Anuncio: responde en 2 ms (multicast)", 2)
        assertEquals(EstadoTransporte.Esperando, estado.value)
        assertEquals(1, anunciador.anuncios)
    }

    @Test
    fun basta_con_volver_a_anunciarse() = conSonda(SondaFalsa(sin, responde)) {
        esperarSucesos("Anuncio: responde")
        assertEquals(EstadoTransporte.Esperando, estado.value)
        assertEquals(2, anunciador.anuncios)
        assertTrue("Anuncio: sin respuesta, se vuelve a anunciar" in sucesos())
        assertTrue(sucesos().none { it.contains("no visible") })
    }

    @Test
    fun sin_respuesta_tras_volver_a_anunciarse_no_es_visible() = conSonda(SondaFalsa(sin)) {
        esperarEstado(EstadoTransporte.NoVisible)
        assertEquals(2, anunciador.anuncios)
        assertTrue("Anuncio: sin respuesta, plataforma no visible" in sucesos())
        // Ya no visible, no se vuelve a anunciar en cada comprobación.
        esperarSucesos("Anuncio: sigue sin respuesta", 2)
        assertEquals(2, anunciador.anuncios)
        assertEquals(EstadoTransporte.NoVisible, estado.value)
    }

    @Test
    fun vuelve_a_esperando_al_responder() = conSonda(SondaFalsa(sin, sin, sin, responde)) {
        esperarEstado(EstadoTransporte.NoVisible)
        esperarEstado(EstadoTransporte.Esperando)
    }

    @Test
    fun deja_de_responder_mas_tarde() = conSonda(SondaFalsa(responde, sin)) {
        esperarSucesos("Anuncio: responde")
        esperarEstado(EstadoTransporte.NoVisible)
        assertEquals(2, anunciador.anuncios)
    }

    @Test
    fun con_cliente_no_se_comprueba() {
        val sonda = SondaFalsa(responde)
        conSonda(sonda) {
            esperarSucesos("Anuncio: responde")
            val c = ClientePrueba(checkNotNull(puerto))
            esperarEstado(EstadoTransporte.Conectado("127.0.0.1"))
            Thread.sleep(150)
            val antes = sonda.comprobaciones
            Thread.sleep(400)
            assertEquals(antes, sonda.comprobaciones)
            c.socket.close()
            esperarEstado(EstadoTransporte.Esperando)
            runBlocking { withTimeout(3_000) { while (sonda.comprobaciones == antes) kotlinx.coroutines.delay(10) } }
        }
    }

    @Test
    fun con_cliente_no_pasa_a_no_visible() {
        val sonda = SondaFalsa(responde)
        conSonda(sonda) {
            esperarSucesos("Anuncio: responde")
            ClientePrueba(checkNotNull(puerto))
            esperarEstado(EstadoTransporte.Conectado("127.0.0.1"))
            sonda.ultimo = sin
            Thread.sleep(400)
            assertEquals(EstadoTransporte.Conectado("127.0.0.1"), estado.value)
        }
    }

    @Test
    fun parar_corta_la_comprobacion() {
        val sonda = SondaFalsa(responde)
        conSonda(sonda) {
            esperarSucesos("Anuncio: responde")
            runBlocking(hilo) { parar() }
            esperarEstado(EstadoTransporte.Parado)
            val antes = sonda.comprobaciones
            Thread.sleep(400)
            assertEquals(antes, sonda.comprobaciones)
        }
    }

    @Test
    fun anuncia_con_el_nombre_dado_y_la_sonda_busca_ese_nombre() = runBlocking {
        val sonda = SondaFalsa(responde)
        val otro = DirconServer(ambito, hilo, registro, anunciador, { "127.0.0.1" }, puertos = listOf(0), ajustarSocket = { null }, sonda = sonda)
        try {
            withContext(hilo) { otro.publicar(gatt, IdentidadDispositivo("02:11:22:33:66:9C", "0123456789"), "KICKR BIKE PRO 669C") }
            withTimeout(3_000) { otro.estado.first { it == EstadoTransporte.Esperando } }
            val (nombre, _, txt) = checkNotNull(anunciador.anuncio)
            assertEquals("KICKR BIKE PRO 669C _wahoo-fitness-tnp._tcp", nombre)
            assertEquals("02:11:22:33:66:9C", txt["mac-address"])
            assertTrue(sucesos().any { "Anunciado «KICKR BIKE PRO 669C»" in it })
            withTimeout(3_000) { while (sonda.instanciaComprobada == null) kotlinx.coroutines.delay(10) }
            assertEquals("KICKR BIKE PRO 669C", sonda.instanciaComprobada)
        } finally {
            otro.parar()
        }
    }
}
