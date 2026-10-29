package app.smartbike.ui.tema

import java.io.File
import java.nio.file.Files
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * El icono de la app, su capa monocroma y la splash (res/drawable/identidad_*.xml y res/mipmap-anydpi/ic_launcher*.xml)
 * se generan desde design-system/identidad con generar.py, que Gradle no ejecuta. Este test regenera esos recursos en un
 * directorio temporal y falla, con el nombre del recurso, si alguno del repositorio difiere.
 */
class IdentidadSincronizadaTest {

    private fun propiedad(nombre: String) = File(checkNotNull(System.getProperty(nombre)) { "Falta la propiedad $nombre (app/build.gradle.kts)" })

    @Test
    fun recursos_de_identidad_coinciden_con_la_fuente() {
        val temporal = Files.createTempDirectory("identidad").toFile()
        try {
            val proceso = ProcessBuilder("python3", propiedad("generador").absolutePath, "--identidad-a", temporal.absolutePath)
                .redirectErrorStream(true).start()
            val salida = proceso.inputStream.bufferedReader().readText()
            assertEquals(0, proceso.waitFor()) { "No se pudo regenerar la identidad:\n$salida" }

            val esperados = temporal.walkTopDown().filter { it.isFile }.associateBy { it.relativeTo(temporal).invariantSeparatorsPath }
            assertTrue(esperados.isNotEmpty()) { "El generador no ha producido ningún recurso de identidad" }
            val res = propiedad("res")
            val fallos = esperados.mapNotNull { (ruta, generado) ->
                val actual = File(res, ruta)
                when {
                    !actual.exists() -> "falta $ruta"
                    actual.readText() != generado.readText() -> "$ruta no coincide con design-system/identidad"
                    else -> null
                }
            }
            assertTrue(fallos.isEmpty()) {
                "Los recursos de identidad no coinciden con su fuente:\n" +
                    fallos.joinToString("\n") { "  - $it" } +
                    "\nEjecuta python3 scripts/design-system/generar.py"
            }
        } finally {
            temporal.deleteRecursively()
        }
    }
}
