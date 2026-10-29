package app.smartbike.datos

import app.smartbike.dominio.ArchivoSesion
import app.smartbike.dominio.Configuracion
import java.io.File
import java.io.IOException
import java.io.Writer
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/** El espacio del volumen de datos del usuario, en bytes. */
class Espacio(val libreBytes: Long, val totalBytes: Long)

/** Lo que muestran la sección «Depuración» y la pantalla de sesiones sobre la grabación. */
sealed interface EstadoGrabacion {
    data object Apagada : EstadoGrabacion

    data class Grabando(val archivo: String) : EstadoGrabacion

    data class DetenidaPorEspacio(val archivo: String) : EstadoGrabacion

    /** [archivo] es `null` si ni siquiera se pudo crear. */
    data class DetenidaPorError(val archivo: String?) : EstadoGrabacion

    /** Se activó con el espacio ya por debajo del mínimo: no se creó ningún archivo. */
    data object SinEspacio : EstadoGrabacion
}

/**
 * Graba los sucesos del [registro] en un archivo de texto de [directorio] (F13.4; design.md del change
 * `registro-sesiones`, decisiones 2 a 5). El hilo que emite un suceso solo lo formatea y lo mete en una cola; una
 * corrutina de [ambito] (de IO) lo escribe, vacía el archivo como mucho cada [intervaloVaciadoMs] y comprueba el
 * espacio cada [intervaloEspacioMs]. Un fallo no se propaga: detiene la grabación y se anota en el registro.
 */
class GrabadorSesion(
    private val directorio: File,
    private val registro: RegistroSesion,
    private val relojPared: () -> Long,
    private val relojMonotonico: () -> Long,
    private val zona: ZoneId,
    private val version: String,
    private val espacio: () -> Espacio,
    private val ambito: CoroutineScope,
    private val intervaloVaciadoMs: Long = 1_000,
    private val intervaloEspacioMs: Long = 30_000,
    private val capacidadCola: Int = 1_000,
    private val abrir: (File) -> Writer = { it.bufferedWriter(Charsets.UTF_8) },
    private val borrar: (File) -> Boolean = { it.delete() },
) {
    private val _estado = MutableStateFlow<EstadoGrabacion>(EstadoGrabacion.Apagada)
    val estado: StateFlow<EstadoGrabacion> = _estado.asStateFlow()

    /** El espacio libre mínimo en %; se aplica al momento, también a una grabación en curso. */
    @Volatile
    var minimoPct: Int = Configuracion().espacioLibreMinimoPct

    private var cola: Channel<String>? = null

    /** Abre un archivo nuevo y empieza a recibir los sucesos del registro. Si ya graba, cierra el anterior. */
    @Synchronized
    fun iniciar(c: Configuracion) {
        cerrar()
        minimoPct = c.espacioLibreMinimoPct
        val ahora = espacio()
        if (!ArchivoSesion.hayEspacio(ahora.libreBytes, ahora.totalBytes, minimoPct)) {
            _estado.value = EstadoGrabacion.SinEspacio
            registro.suceso(Origen.App, "Grabación no iniciada: poco espacio libre")
            return
        }
        val inicio = relojPared()
        directorio.mkdirs()
        rotar()
        val archivo = nombreLibre(inicio)
        val escritor = try {
            abrir(archivo)
        } catch (e: IOException) {
            fallar(archivo.name, e)
            return
        }
        val perdidos = AtomicInteger()
        val nueva = Channel<String>(capacidadCola, BufferOverflow.DROP_OLDEST) { perdidos.incrementAndGet() }
        cola = nueva
        _estado.value = EstadoGrabacion.Grabando(archivo.name)
        registro.sumidero = { e -> nueva.trySend(ArchivoSesion.linea(e.horaMs, zona, e.origen.name, e.texto)) }
        ambito.launch { escribir(archivo.name, escritor, nueva, perdidos, ArchivoSesion.cabecera(inicio, zona, version, c)) }
    }

    /**
     * Cierra el archivo y deja de recibir sucesos. Con [olvidarParada], una parada por espacio o por error deja de
     * mostrarse (se apagó el ajuste); si no, se sigue viendo hasta la siguiente grabación.
     */
    @Synchronized
    fun detener(olvidarParada: Boolean = false) {
        cerrar()
        val e = _estado.value
        if (e is EstadoGrabacion.Grabando || olvidarParada) _estado.value = EstadoGrabacion.Apagada
    }

    private fun cerrar() {
        registro.sumidero = null
        cola?.close()
        cola = null
    }

    private fun fallar(archivo: String?, e: Exception) {
        _estado.value = EstadoGrabacion.DetenidaPorError(archivo)
        registro.suceso(Origen.App, "Grabación detenida por un error: ${e.message}")
    }

    /**
     * Borra las sesiones más antiguas para que, con la que va a empezar, queden [ArchivoSesion.SESIONES_CONSERVADAS]
     * (spec `session-recording`, «Sesiones conservadas»). La anterior ya está cerrada y la nueva aún no existe, así que
     * ninguna de la carpeta se está grabando. Un fallo se anota y no impide grabar.
     */
    private fun rotar() {
        val nombres = directorio.listFiles { f -> f.isFile && f.name.endsWith(".txt") }.orEmpty().map { it.name }
        ArchivoSesion.aBorrar(nombres).forEach { nombre ->
            val borrada = try {
                borrar(File(directorio, nombre))
            } catch (e: SecurityException) {
                false
            }
            registro.suceso(Origen.App, if (borrada) "Sesión antigua borrada: $nombre" else "No se pudo borrar la sesión antigua: $nombre")
        }
    }

    private fun nombreLibre(inicioMs: Long): File {
        val base = "sesion-" + NOMBRE.format(Instant.ofEpochMilli(inicioMs).atZone(zona))
        var f = File(directorio, "$base.txt")
        var n = 2
        while (f.exists()) f = File(directorio, "$base-${n++}.txt")
        return f
    }

    private suspend fun escribir(nombre: String, w: Writer, entrada: Channel<String>, perdidos: AtomicInteger, cabecera: List<String>) {
        var parada: EstadoGrabacion? = null
        try {
            w.use {
                cabecera.forEach { l ->
                    w.write(l)
                    w.write("\n")
                }
                w.flush()
                var proximoVaciado: Long? = null
                var proximoEspacio = relojMonotonico() + intervaloEspacioMs
                var sigue = true
                while (sigue) {
                    val limite = minOf(proximoVaciado ?: Long.MAX_VALUE, proximoEspacio)
                    val r = withTimeoutOrNull((limite - relojMonotonico()).coerceAtLeast(0)) { entrada.receiveCatching() }
                    if (r != null) {
                        if (r.isClosed) {
                            sigue = false
                        } else {
                            escribirLinea(w, perdidos, r.getOrThrow())
                            while (true) escribirLinea(w, perdidos, entrada.tryReceive().getOrNull() ?: break)
                            if (proximoVaciado == null) proximoVaciado = relojMonotonico() + intervaloVaciadoMs
                        }
                    }
                    val ahora = relojMonotonico()
                    if (proximoVaciado != null && ahora >= proximoVaciado) {
                        w.flush()
                        proximoVaciado = null
                    }
                    if (sigue && ahora >= proximoEspacio) {
                        proximoEspacio = ahora + intervaloEspacioMs
                        val e = espacio()
                        val minimo = minimoPct
                        if (!ArchivoSesion.hayEspacio(e.libreBytes, e.totalBytes, minimo)) {
                            w.write(ArchivoSesion.paradaPorEspacio(e.libreBytes, e.totalBytes, minimo) + "\n")
                            parada = EstadoGrabacion.DetenidaPorEspacio(nombre)
                            sigue = false
                        }
                    }
                }
                escribirLinea(w, perdidos, null)
            }
        } catch (e: IOException) {
            parada = EstadoGrabacion.DetenidaPorError(nombre)
        }
        if (parada != null) detenerPor(entrada, parada)
    }

    /** Escribe una línea y, antes, el aviso de lo que la cola descartó. `null` solo escribe ese aviso pendiente. */
    private fun escribirLinea(w: Writer, perdidos: AtomicInteger, linea: String?) {
        val n = perdidos.getAndSet(0)
        if (n > 0) w.write(ArchivoSesion.descartados(n) + "\n")
        if (linea != null) w.write(linea + "\n")
    }

    @Synchronized
    private fun detenerPor(entrada: Channel<String>, motivo: EstadoGrabacion) {
        // Solo si sigue siendo esta grabación: una nueva ya habría cambiado la cola.
        if (cola !== entrada) return
        cerrar()
        _estado.value = motivo
        registro.suceso(
            Origen.App,
            if (motivo is EstadoGrabacion.DetenidaPorEspacio) "Grabación detenida: poco espacio libre" else "Grabación detenida por un error",
        )
    }

    private companion object {
        val NOMBRE: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")
    }
}

/**
 * Ata el grabador al ajuste «Grabar sesiones» y a la sesión de rodaje (design.md del change `registro-sesiones`,
 * decisión 3): graba mientras haya las dos cosas. Al activarse abre un archivo nuevo; al desactivarse el ajuste o
 * terminar la sesión, lo cierra. El espacio libre mínimo se aplica al momento.
 */
fun CoroutineScope.atarGrabador(grabador: GrabadorSesion, configuracion: Flow<Configuracion>, haySesion: Flow<Boolean>): Job = launch {
    combine(configuracion, haySesion) { c, sesion -> Pair(c.grabarSesiones && sesion, c) }
        .onEach { (_, c) -> grabador.minimoPct = c.espacioLibreMinimoPct }
        .distinctUntilChangedBy { (activa, _) -> activa }
        .collect { (activa, c) -> if (activa) grabador.iniciar(c) else grabador.detener(olvidarParada = !c.grabarSesiones) }
}
