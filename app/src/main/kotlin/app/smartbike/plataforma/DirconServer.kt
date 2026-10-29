package app.smartbike.plataforma

import app.smartbike.datos.Origen
import app.smartbike.datos.RegistroSesion
import app.smartbike.datos.Sentido
import app.smartbike.dominio.IdentidadDispositivo
import app.smartbike.dominio.LimiteRitmo
import app.smartbike.dominio.PoliticaClientes
import app.smartbike.protocolo.DirconCodec
import app.smartbike.protocolo.DirconCodec.Peticion
import app.smartbike.protocolo.NotificacionGatt
import app.smartbike.protocolo.ServidorGatt
import java.io.IOException
import java.io.OutputStream
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.UUID
import java.util.concurrent.atomic.AtomicLong
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Transporte por wifi (DirCon): servidor TCP de un cliente y anuncio mDNS (arquitectura.md § 7.3; design.md del change
 * `puente-zwift-wifi`, decisiones 2 y 10). Lee y escribe en [io]; cada petición se atiende en [hilo], el hilo de
 * control, y un escritor único por cliente serializa respuestas y notificaciones. Un cliente nuevo sustituye al
 * anterior, para que la plataforma pueda volver tras un corte aunque el socket viejo siga medio abierto.
 *
 * Con [sonda], mientras no hay cliente comprueba cada [periodoComprobacionMs] que el anuncio responde en la red y, si
 * no, se vuelve a anunciar una vez antes de pasar a [EstadoTransporte.NoVisible] (design.md del change
 * `comprobar-anuncio-zwift`, decisiones 2 y 3).
 */
class DirconServer(
    private val ambito: CoroutineScope,
    private val hilo: CoroutineDispatcher,
    private val registro: RegistroSesion,
    private val anunciador: Anunciador,
    private val direccionWifi: () -> String?,
    private val puertos: List<Int> = listOf(PUERTO, 0),
    private val io: CoroutineDispatcher = Dispatchers.IO,
    private val serviciosExtra: List<String> = emptyList(),
    private val sonda: SondaAnuncio? = null,
    private val periodoComprobacionMs: Long = PERIODO_COMPROBACION_MS,
    private val reloj: () -> Long = { System.nanoTime() / 1_000_000 },
    private val ajustarSocket: (Socket) -> String? = ::ajustarSocketDircon,
    private val salidaDe: (Socket) -> OutputStream = { it.getOutputStream() },
    private val ipDe: (Socket) -> String = { it.inetAddress?.hostAddress ?: "?" },
    private val escrituraMaxMs: Long = ESCRITURA_MAX_MS,
    private val periodoVigilanteMs: Long = PERIODO_VIGILANTE_MS,
) : TransporteDircon {
    private val _estado = MutableStateFlow<EstadoTransporte>(EstadoTransporte.Parado)
    override val estado: StateFlow<EstadoTransporte> = _estado.asStateFlow()

    private var trabajo: Job? = null

    /** Si está publicado o publicándose; solo lo tocan [publicar] y [parar], desde el hilo de control. */
    private var publicado = false

    /** La dirección de la wifi con la que se publicó; si cambia, [publicar] cierra y vuelve a abrir. */
    private var direccionPublicada: String? = null

    @Volatile private var servidorTcp: ServerSocket? = null

    @Volatile private var cliente: Cliente? = null

    /** Puerto en el que escucha, o `null` si no está publicado. */
    val puerto: Int? get() = servidorTcp?.localPort

    /** Dirección en la que escucha, o `null` si no está publicado. */
    val direccionEscucha: String? get() = servidorTcp?.inetAddress?.hostAddress

    private class Cliente(val socket: Socket, val direccion: String, val salida: Channel<ByteArray>, val trabajo: Job) {
        /** Instante (reloj del servidor) del último mensaje recibido. */
        @Volatile var ultimoMensajeMs = 0L
    }

    override fun publicar(servidor: ServidorGatt, identidad: IdentidadDispositivo, nombre: String) {
        val direccion = direccionWifi()
        if (publicado && direccion == direccionPublicada) return
        if (publicado) {
            registro.suceso(Origen.Plataforma, "DirCon: la wifi cambia a ${direccion ?: "ninguna"}, se vuelve a abrir")
            parar()
        }
        if (direccion == null) {
            registro.suceso(Origen.Plataforma, "DirCon: sin dirección de wifi, no se abre")
            return
        }
        publicado = true
        direccionPublicada = direccion
        val anterior = trabajo
        trabajo = ambito.launch(io) {
            // Tras un `parar()` reciente, el servidor anterior termina de cerrarse antes de abrir el nuevo.
            anterior?.join()
            val tcp = abrir(direccion) ?: run {
                registro.suceso(Origen.Plataforma, "No se pudo abrir el servidor DirCon")
                return@launch
            }
            servidorTcp = tcp
            try {
                // `accept` no se puede cancelar: si `parar()` llegó antes de guardar el socket, se sale aquí.
                ensureActive()
                val txt = registros(servidor, identidad, serviciosExtra)
                val anunciar = suspend { anunciador.anunciar(nombre, TIPO, tcp.localPort, txt) }
                val registrado = anunciar()
                registro.suceso(Origen.Plataforma, "Anunciado «$nombre» en $direccion:${tcp.localPort} (${identidad.mac})")
                _estado.value = EstadoTransporte.Esperando
                sonda?.let { s -> launch { comprobarAnuncio(s, nombre, tcp.localPort, registrado, anunciar) } }
                while (true) {
                    val s = tcp.accept()
                    val ip = ipDe(s)
                    val viejo = cliente
                    val actual = viejo?.let { PoliticaClientes.Actual(it.direccion, it.ultimoMensajeMs) }
                    if (PoliticaClientes.decidir(actual, ip, reloj()) == PoliticaClientes.Decision.Rechazar) {
                        registro.suceso(Origen.Plataforma, "DirCon: rechazado $ip, la plataforma de ${viejo?.direccion} sigue activa")
                        s.close()
                        continue
                    }
                    if (viejo != null) {
                        registro.suceso(Origen.Plataforma, "Otro cliente sustituye al anterior")
                        viejo.socket.close()
                        viejo.trabajo.join()
                    }
                    val salida = Channel<ByteArray>(CAPACIDAD_SALIDA, BufferOverflow.DROP_OLDEST)
                    lateinit var nuevo: Cliente
                    val t = launch(start = CoroutineStart.LAZY) { atender(s, nuevo, servidor) }
                    nuevo = Cliente(s, ip, salida, t).also { it.ultimoMensajeMs = reloj() }
                    cliente = nuevo
                    t.start()
                }
            } catch (_: IOException) {
                // `parar()` cierra el servidor y `accept` termina.
            } finally {
                tcp.close()
                servidorTcp = null
                // Primero se cierra el socket del cliente, que está bloqueado leyendo, y después se espera a que acabe.
                cliente?.socket?.close()
                withContext(NonCancellable) { cliente?.trabajo?.join() }
                anunciador.retirar()
                _estado.value = EstadoTransporte.Parado
                registro.suceso(Origen.Plataforma, "Servidor DirCon parado")
            }
        }
    }

    override fun parar() {
        if (!publicado) return
        publicado = false
        direccionPublicada = null
        trabajo?.cancel()
        servidorTcp?.close()
        cliente?.socket?.close()
    }

    /**
     * Comprueba el anuncio al registrarse y cada [periodoComprobacionMs] mientras no hay cliente. Ante un fallo estando
     * visible, se vuelve a anunciar una vez y comprueba otra vez antes de pasar a [EstadoTransporte.NoVisible].
     */
    private suspend fun comprobarAnuncio(sonda: SondaAnuncio, nombre: String, puerto: Int, registradoAlPublicar: Boolean, anunciar: suspend () -> Boolean) {
        var registrado = registradoAlPublicar
        while (true) {
            if (cliente == null) {
                val comprobar = suspend { if (registrado) sonda.comprobar(nombre, TIPO, puerto) else ResultadoSonda.SinRespuesta }
                var r = comprobar()
                val visibleAntes = _estado.value != EstadoTransporte.NoVisible
                if (r == ResultadoSonda.SinRespuesta && visibleAntes && cliente == null) {
                    registro.suceso(Origen.Plataforma, "Anuncio: sin respuesta, se vuelve a anunciar")
                    anunciador.retirar()
                    registrado = anunciar()
                    r = comprobar()
                }
                when (r) {
                    is ResultadoSonda.Responde -> {
                        registro.suceso(Origen.Plataforma, "Anuncio: responde en ${r.ms} ms (${r.via})")
                        if (cliente == null) _estado.compareAndSet(EstadoTransporte.NoVisible, EstadoTransporte.Esperando)
                    }

                    ResultadoSonda.SinRespuesta -> {
                        registro.suceso(Origen.Plataforma, if (visibleAntes) "Anuncio: sin respuesta, plataforma no visible" else "Anuncio: sigue sin respuesta")
                        if (cliente == null) _estado.compareAndSet(EstadoTransporte.Esperando, EstadoTransporte.NoVisible)
                    }
                }
            }
            delay(periodoComprobacionMs)
        }
    }

    override fun notificar(n: NotificacionGatt) {
        val c = cliente ?: return
        if (c.salida.trySend(DirconCodec.codificar(DirconCodec.notificacion(n.caracteristica, n.valor))).isSuccess) {
            registro.trama(Origen.Plataforma, Sentido.Tx, DirconCodec.nombreCorto(n.caracteristica), n.valor)
        }
    }

    private suspend fun atender(s: Socket, c: Cliente, servidor: ServidorGatt) = coroutineScope {
        val direccion = c.direccion
        val salida = c.salida
        val limite = LimiteRitmo(reloj)
        ajustarSocket(s)?.let { registro.suceso(Origen.Plataforma, "DirCon: socket sin ajustar completo ($it)") }
        // Instante en que empezó la escritura en curso, o 0 si no hay ninguna.
        val escribiendoDesde = AtomicLong(0)
        withContext(hilo) { servidor.clientePerdido() }
        registro.suceso(Origen.Plataforma, "Plataforma conectada desde $direccion")
        _estado.value = EstadoTransporte.Conectado(direccion)
        val escritor = launch {
            try {
                s.tcpNoDelay = true
                val out = salidaDe(s)
                for (b in salida) {
                    escribiendoDesde.set(reloj())
                    out.write(b)
                    out.flush()
                    escribiendoDesde.set(0)
                }
            } catch (e: IOException) {
                registro.suceso(Origen.Plataforma, "Error al escribir a la plataforma: ${e.message}")
                s.close()
            }
        }
        val vigilante = launch {
            while (true) {
                delay(periodoVigilanteMs)
                val desde = escribiendoDesde.get()
                if (desde != 0L && reloj() - desde > escrituraMaxMs) {
                    registro.suceso(Origen.Plataforma, "DirCon: escritura bloqueada más de ${escrituraMaxMs / 1000} s, se cierra")
                    s.close()
                    return@launch
                }
            }
        }
        try {
            val entrada = s.getInputStream()
            val lector = DirconCodec.Lector()
            val buf = ByteArray(TAMANO_LECTURA)
            while (true) {
                val n = entrada.read(buf)
                if (n < 0) break
                for (m in lector.anadir(buf, n)) {
                    c.ultimoMensajeMs = reloj()
                    if (!limite.permitir()) {
                        registro.suceso(Origen.Plataforma, "DirCon: $direccion desconectado por exceso de mensajes")
                        s.close()
                        return@coroutineScope
                    }
                    val p = DirconCodec.peticion(m)
                    registrar(p)
                    val a = try {
                        withContext(hilo) { DirconCodec.atender(servidor, p) }
                    } catch (e: Exception) {
                        registro.suceso(Origen.Plataforma, "DirCon: error al atender ${p::class.simpleName}: $e")
                        DirconCodec.Atencion(DirconCodec.error(m.tipo, p.secuencia, DirconCodec.ERROR_INESPERADO))
                    }
                    if (a.respuesta.codigo != DirconCodec.EXITO) {
                        registro.suceso(Origen.Plataforma, "DirCon: respuesta ${a.respuesta.tipo} con error ${a.respuesta.codigo}")
                    }
                    salida.trySend(DirconCodec.codificar(a.respuesta))
                    a.notificaciones.forEach { notificarA(salida, it) }
                }
            }
        } catch (_: IOException) {
            // Socket cerrado: por Zwift, por un cliente nuevo o por `parar()`.
        } finally {
            salida.close()
            vigilante.cancel()
            escritor.cancel()
            s.close()
            withContext(NonCancellable) {
                registro.suceso(Origen.Plataforma, "Plataforma desconectada ($direccion)")
                if (cliente?.socket === s) {
                    cliente = null
                    withContext(hilo) { servidor.clientePerdido() }
                    if (servidorTcp != null) _estado.value = EstadoTransporte.Esperando
                }
            }
        }
    }

    private fun notificarA(salida: Channel<ByteArray>, n: NotificacionGatt) {
        if (salida.trySend(DirconCodec.codificar(DirconCodec.notificacion(n.caracteristica, n.valor))).isSuccess) {
            registro.trama(Origen.Plataforma, Sentido.Tx, DirconCodec.nombreCorto(n.caracteristica), n.valor)
        }
    }

    private fun registrar(p: Peticion) {
        when (p) {
            is Peticion.Escribir -> registro.trama(Origen.Plataforma, Sentido.Rx, DirconCodec.nombreCorto(p.caracteristica), p.valor)

            is Peticion.DescubrirServicios -> registro.suceso(Origen.Plataforma, "DirCon: descubrir servicios")

            is Peticion.DescubrirCaracteristicas ->
                registro.suceso(Origen.Plataforma, "DirCon: características de ${DirconCodec.nombreCorto(p.servicio)}")

            is Peticion.Leer -> registro.suceso(Origen.Plataforma, "DirCon: leer ${DirconCodec.nombreCorto(p.caracteristica)}")

            is Peticion.Suscribir -> registro.suceso(
                Origen.Plataforma,
                "DirCon: ${if (p.activar) "suscribir" else "desuscribir"} ${DirconCodec.nombreCorto(p.caracteristica)}",
            )

            is Peticion.Desconocida -> registro.suceso(Origen.Plataforma, "DirCon: mensaje ${p.tipo} no atendido (${p.codigo})")
        }
    }

    private fun abrir(direccion: String): ServerSocket? {
        for (p in puertos) {
            try {
                return ServerSocket().apply {
                    reuseAddress = true
                    bind(InetSocketAddress(InetAddress.getByName(direccion), p))
                }
            } catch (e: IOException) {
                registro.suceso(Origen.Plataforma, "Puerto $p ocupado: ${e.message}")
            }
        }
        return null
    }

    companion object {
        const val TIPO = "_wahoo-fitness-tnp._tcp"
        const val PUERTO = 36866
        const val PERIODO_COMPROBACION_MS = 60_000L
        private const val CAPACIDAD_SALIDA = 256
        private const val TAMANO_LECTURA = 2048
        const val ESCRITURA_MAX_MS = 10_000L
        const val PERIODO_VIGILANTE_MS = 1_000L

        /**
         * Registros TXT del anuncio: servicios ofrecidos e identidad estable. Los UUID cortos van como `0x1826`; los
         * largos, como el del mando Zwift, completos y en mayúsculas (a validar). [extra] se añade al final tal cual
         * (design.md del change `prototipo-mando-zwift`, decisión 4).
         */
        fun registros(
            servidor: ServidorGatt,
            identidad: IdentidadDispositivo,
            extra: List<String> = emptyList(),
        ): Map<String, String> = mapOf(
            "ble-service-uuids" to (servidor.descubrirServicios().map { servicioAnunciado(it) } + extra).joinToString(","),
            "mac-address" to identidad.mac,
            "serial-number" to identidad.serie,
        )

        private fun servicioAnunciado(u: UUID): String {
            val corto = DirconCodec.nombreCorto(u)
            return if (corto.length == 4) "0x" + corto.uppercase() else corto.uppercase()
        }
    }
}
