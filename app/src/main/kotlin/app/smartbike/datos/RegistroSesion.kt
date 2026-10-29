package app.smartbike.datos

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class Origen { Square, Neo, Pulso, Ventilador, Plataforma, App }

/** `Rx`: trama recibida; `Tx`: trama enviada; `Evt`: suceso de la app (conexión, envío manual…). */
enum class Sentido { Rx, Tx, Evt }

/**
 * Una entrada del registro. [tMs] es monotónico (no salta con la hora del sistema) y sirve para medir; [horaMs] es
 * la hora de reloj, solo para mostrarla. [caracteristica] es el UUID corto (`0x43`, `fec3`, `2a63`) o `-`.
 */
class EntradaRegistro(
    val tMs: Long,
    val horaMs: Long,
    val origen: Origen,
    val sentido: Sentido,
    val caracteristica: String,
    val datos: ByteArray = ByteArray(0),
    val texto: String = "",
) {
    /**
     * Línea de traza: `<tMs> <ORIGEN> <SENTIDO> <característica> <hex o texto>`, p. ej.
     * `183402117 SQUARE RX 0x43 0a1b2c80020000ff`. Es el formato que leen los tests de `:core` (`Traza.leer`).
     */
    fun linea(): String {
        val contenido = if (sentido == Sentido.Evt) texto else datos.joinToString("") { "%02x".format(it) }
        return "$tMs ${origen.name.uppercase()} ${sentido.name.uppercase()} $caracteristica $contenido"
    }
}

/**
 * Registro circular en memoria de tramas y sucesos de la sesión (F13.1, arquitectura.md § 11). Se puede escribir
 * desde cualquier hilo. Cada entrada se copia también a [espejo] (en depuración, el registro del sistema).
 */
class RegistroSesion(
    private val capacidad: Int = CAPACIDAD,
    private val relojMonotonico: () -> Long,
    private val relojPared: () -> Long,
    private val espejo: ((String) -> Unit)? = null,
) {
    private val entradas = ArrayDeque<EntradaRegistro>(capacidad)
    private val _cambios = MutableStateFlow(0L)

    /** Crece con cada entrada nueva o al limpiar; la UI lo muestrea para pedir [ultimas]. */
    val cambios: StateFlow<Long> = _cambios.asStateFlow()

    /**
     * Recibe cada suceso (`Evt`) una sola vez, junto al [espejo], y ninguna trama. Se asigna y se quita en caliente
     * desde cualquier hilo; sin él, el coste es leer un campo (design.md del change `registro-sesiones`, decisión 1).
     */
    @Volatile
    var sumidero: ((EntradaRegistro) -> Unit)? = null

    fun trama(origen: Origen, sentido: Sentido, caracteristica: String, datos: ByteArray) = anadir(EntradaRegistro(relojMonotonico(), relojPared(), origen, sentido, caracteristica, datos.copyOf()))

    fun suceso(origen: Origen, texto: String) = anadir(EntradaRegistro(relojMonotonico(), relojPared(), origen, Sentido.Evt, "-", texto = texto))

    /** Las [n] entradas más recientes, la más reciente primero. */
    fun ultimas(n: Int): List<EntradaRegistro> = synchronized(entradas) {
        entradas.takeLast(n).asReversed().toList()
    }

    val tamano: Int get() = synchronized(entradas) { entradas.size }

    private fun anadir(e: EntradaRegistro) {
        synchronized(entradas) {
            if (entradas.size == capacidad) entradas.removeFirst()
            entradas.addLast(e)
        }
        _cambios.update { it + 1 }
        espejo?.invoke(e.linea())
        if (e.sentido == Sentido.Evt) sumidero?.invoke(e)
    }

    companion object {
        const val CAPACIDAD = 10_000
    }
}
