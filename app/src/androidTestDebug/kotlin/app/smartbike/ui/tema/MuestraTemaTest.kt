package app.smartbike.ui.tema

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performSemanticsAction
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class MuestraTemaTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun muestra_cada_color_y_estilo_en_los_dos_temas() {
        compose.setContent { MuestraTema() }

        for (nombre in ColoresRodaje.porNombre().keys) {
            assertEquals("color $nombre", 2, compose.onAllNodesWithText(nombre, useUnmergedTree = true).fetchSemanticsNodes().size)
        }
        for (nombre in SqTipografia.porNombre().keys) {
            for (tema in Tema.entries) {
                assertEquals("estilo $nombre ($tema)", 1, compose.onAllNodesWithTag(etiquetaEjemplo(tema, nombre)).fetchSemanticsNodes().size)
            }
            assertEquals(
                "etiqueta de $nombre",
                2,
                compose.onAllNodesWithText("$nombre · ", substring = true).fetchSemanticsNodes().size,
            )
        }
    }

    @Test
    fun gear_hero_se_alcanza_desplazando() {
        compose.setContent { MuestraTema() }

        for (tema in Tema.entries) {
            val ejemplo = compose.onNodeWithTag(etiquetaEjemplo(tema, "gear-hero"))
            bajarHasta(ejemplo)
            ejemplo.assertIsDisplayed()
        }
    }

    /**
     * Desplaza la columna de la muestra hasta que el nodo quede entero a la vista. performScrollTo y
     * performScrollToNode solo mueven el contenedor desplazable más cercano, que aquí es el horizontal de cada ejemplo.
     */
    private fun bajarHasta(nodo: SemanticsNodeInteraction) {
        val muestra = compose.onNodeWithTag("muestra")
        val alto = muestra.fetchSemanticsNode().boundsInRoot.height
        repeat(100) {
            val visible = nodo.fetchSemanticsNode().boundsInRoot
            if (visible.height >= nodo.fetchSemanticsNode().size.height) return
            muestra.performSemanticsAction(SemanticsActions.ScrollBy) { it(0f, alto / 2) }
            compose.waitForIdle()
        }
        throw AssertionError("No se llegó al nodo desplazando la muestra")
    }
}
