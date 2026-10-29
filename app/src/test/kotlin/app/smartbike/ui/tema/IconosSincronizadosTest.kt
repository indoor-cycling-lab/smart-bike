package app.smartbike.ui.tema

import java.io.File
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Los iconos de res/drawable (ic_*.xml) se generan a partir de design-system/icons.svg con generar.py, que Gradle
 * no ejecuta. Este test falla si se cambió icons.svg sin regenerar o si se editó un icono a mano.
 */
class IconosSincronizadosTest {

    private fun propiedad(nombre: String) = File(checkNotNull(System.getProperty(nombre)) { "Falta la propiedad $nombre (app/build.gradle.kts)" })

    @Test
    fun iconos_de_la_app_coinciden_con_icons_svg() {
        val svg = propiedad("icons.svg").readText().replace(Regex("<!--.*?-->", RegexOption.DOT_MATCHES_ALL), "")
        val fuente = Regex("""<symbol id="i-([a-z0-9_]+)"[^>]*>\s*<path d="([^"]+)"\s*/>""").findAll(svg)
            .associate { it.groupValues[1] to it.groupValues[2] }

        val app = propiedad("drawables").listFiles { f -> f.name.startsWith("ic_") && f.extension == "xml" }.orEmpty()
            .associate { f ->
                f.nameWithoutExtension.removePrefix("ic_") to
                    Regex("""android:pathData="([^"]+)"""").find(f.readText())?.groupValues?.get(1)
            }

        val fallos = buildList {
            (fuente.keys - app.keys).sorted().forEach { add("falta el icono $it en res/drawable") }
            (app.keys - fuente.keys).sorted().forEach { add("el icono $it no está en icons.svg") }
            fuente.keys.intersect(app.keys).sorted().forEach { if (fuente[it] != app[it]) add("el icono $it no coincide con icons.svg") }
        }
        assertTrue(fuente.isNotEmpty()) { "No se ha encontrado ningún icono en icons.svg" }
        assertTrue(fallos.isEmpty()) {
            "Los iconos de la app no coinciden con design-system/icons.svg:\n" +
                fallos.joinToString("\n") { "  - $it" } +
                "\nEjecuta python3 scripts/design-system/generar.py"
        }
    }
}
