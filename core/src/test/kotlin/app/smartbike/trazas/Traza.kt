package app.smartbike.trazas

/** Una línea de traza grabada con el equipo real (formato de `RegistroSesion`, en `:app`). */
data class LineaTraza(
    val tMs: Long,
    val origen: String,
    val sentido: String,
    val caracteristica: String,
    val datos: ByteArray,
    val texto: String,
)

/**
 * Lee las trazas de `src/test/resources/trazas/`. Acepta las líneas tal cual las da `adb logcat -s SqTrama`
 * (con la cabecera de logcat delante) o solas: `<tMs> <ORIGEN> <SENTIDO> <característica> <hex o texto>`.
 * Ignora las líneas en blanco, los comentarios (`#`) y lo que no tenga ese formato.
 */
object Traza {
    private val formato = Regex("""(\d+) (SQUARE|NEO|APP) (RX|TX|EVT) (\S+) ?(.*)$""")

    fun leer(nombre: String): List<LineaTraza> {
        val recurso = checkNotNull(javaClass.getResource("/trazas/$nombre")) { "No existe la traza $nombre" }
        return parsear(recurso.readText().lineSequence())
    }

    fun parsear(lineas: Sequence<String>): List<LineaTraza> = lineas
        .filterNot { it.isBlank() || it.trimStart().startsWith("#") }
        .mapNotNull { formato.find(it) }
        .map { m ->
            val (t, origen, sentido, caracteristica, resto) = m.destructured
            val esEvento = sentido == "EVT"
            LineaTraza(
                tMs = t.toLong(),
                origen = origen,
                sentido = sentido,
                caracteristica = caracteristica,
                datos = if (esEvento) ByteArray(0) else hexABytes(resto.trim()),
                texto = if (esEvento) resto else "",
            )
        }
        .toList()

    private fun hexABytes(hex: String): ByteArray {
        require(hex.length % 2 == 0) { "Hexadecimal con longitud impar: $hex" }
        return ByteArray(hex.length / 2) { hex.substring(it * 2, it * 2 + 2).toInt(16).toByte() }
    }
}
