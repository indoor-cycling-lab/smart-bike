package app.smartbike.dominio

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * El formato del archivo de una sesión grabada (F13.4; design.md del change `registro-sesiones`, decisiones 4 y 5):
 * texto plano con una cabecera de líneas `# ` y un suceso por línea. Son funciones puras: quien escribe en disco es
 * `GrabadorSesion`.
 */
object ArchivoSesion {
    private val fechaHora = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    private val hora = DateTimeFormatter.ofPattern("HH:mm:ss.SSS")

    /**
     * La cabecera: cuándo empezó, la versión de la app y la configuración de la sesión (modo, nombre ante la app,
     * transmisión y modelo de resistencia), una línea `# ` cada dato y al final una línea en blanco.
     */
    fun cabecera(inicioMs: Long, zona: ZoneId, version: String, c: Configuracion): List<String> = listOf(
        "# Smart Bike · sesión grabada",
        "# Inicio: ${fechaHora.format(Instant.ofEpochMilli(inicioMs).atZone(zona))}",
        "# Versión de la app: $version",
        "# Modo de conexión: ${c.conexion.modo.name.lowercase().replace('_', '-')}",
        "# Nombre ante la app: ${if (c.simularKickrBike) NombreAnunciado.KICKR else NombreAnunciado.NORMAL}",
        "# Transmisión: ${c.transmisionElegida.id}",
        "# Modelo de resistencia: ${c.modelo.name.lowercase()}",
        "",
    )

    /** Un suceso: `HH:mm:ss.SSS ORIGEN texto`, con la hora de reloj. */
    fun linea(horaMs: Long, zona: ZoneId, origen: String, texto: String): String = "${hora.format(Instant.ofEpochMilli(horaMs).atZone(zona))} ${origen.uppercase()} $texto"

    /** La última línea de una grabación detenida por poco espacio libre. */
    fun paradaPorEspacio(libre: Long, total: Long, minimoPct: Int): String = "# Grabación detenida: queda ${porcentaje(libre, total)} % libre, mínimo $minimoPct %"

    /** El aviso de que la cola desbordó y se perdieron [n] sucesos. */
    fun descartados(n: Int): String = "# se descartaron $n sucesos"

    /**
     * Si queda al menos [minimoPct] % del total libre. Sin un total medible (cero o negativo) no hay espacio del que
     * fiarse: es falso.
     */
    fun hayEspacio(libre: Long, total: Long, minimoPct: Int): Boolean = total > 0 && libre * 100 >= total * minimoPct

    /** El % libre, redondeado hacia abajo, para decirlo en una línea. */
    fun porcentaje(libre: Long, total: Long): Int = if (total > 0) (libre * 100 / total).toInt() else 0

    /** Cuántas sesiones grabadas se conservan, contando la que empieza (spec `session-recording`, «Sesiones conservadas»). */
    const val SESIONES_CONSERVADAS = 20

    /**
     * Las sesiones de [nombres] que sobran, las más antiguas primero, para que al añadir una nueva queden [conservar]
     * (design.md del change `preparar-release`, decisión 6). Se ordenan por la fecha del nombre y, en el mismo segundo,
     * por su sufijo: `sesion-…-184207.txt` va antes que `sesion-…-184207-2.txt`.
     */
    fun aBorrar(nombres: List<String>, conservar: Int = SESIONES_CONSERVADAS): List<String> {
        val sobran = nombres.size - (conservar - 1)
        if (sobran <= 0) return emptyList()
        return nombres.sortedWith(compareBy({ baseDe(it) }, { sufijoDe(it) })).take(sobran)
    }

    /** `sesion-20261029-184207-2.txt` → `sesion-20261029-184207`. */
    private fun baseDe(nombre: String): String = nombre.removeSuffix(".txt").take(LARGO_BASE)

    /** `sesion-20261029-184207-2.txt` → 2; sin sufijo, 1. */
    private fun sufijoDe(nombre: String): Int = nombre.removeSuffix(".txt").drop(LARGO_BASE).removePrefix("-").toIntOrNull() ?: 1

    /** Lo que mide `sesion-AAAAMMDD-HHMMSS`. */
    private const val LARGO_BASE = 22
}
