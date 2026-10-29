package app.smartbike.ui.tabla

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToIndex
import app.smartbike.dominio.Configuracion
import app.smartbike.dominio.Desarrollos
import app.smartbike.dominio.EstadoMarcha
import app.smartbike.ui.createComposeRuleEs
import app.smartbike.ui.tema.SqTema
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/** maquetas/tabla-marchas.html y escenarios de la spec `settings-screen`, «Pantalla Tabla de marchas». */
class TablaTest {
    @get:Rule
    val compose = createComposeRuleEs()

    private fun mostrar(estado: TablaUiState, acciones: AccionesTabla = AccionesTabla()) {
        compose.setContent { SqTema { Tabla(estado, acciones) } }
    }

    @Test
    fun lineal_con_la_marcha_puesta_a_la_vista() {
        mostrar(estadoDeTabla(Configuracion(), EstadoMarcha.Numerada(17, 24)))
        compose.onNodeWithText("● Puesta · Ajuste de la marcha").assertIsDisplayed()
    }

    @Test
    fun shimano_con_nombres_y_relacion() {
        mostrar(estadoDeTabla(Configuracion(transmision = Desarrollos.SHIMANO_105.id), EstadoMarcha.PlatoPinon(36, 17)))
        compose.onNodeWithText("● Puesta · Relación 2,12").assertIsDisplayed()
        compose.onNodeWithText("36×17").assertIsDisplayed()
    }

    @Test
    fun cambiar_una_marcha_y_restablecer() {
        var pasos: Pair<Int, Int>? = null
        var restablecida = false
        mostrar(
            estadoDeTabla(Configuracion(), EstadoMarcha.Numerada(17, 24)),
            AccionesTabla(ajustar = { i, p -> pasos = i to p }, restablecer = { restablecida = true }),
        )
        compose.onNodeWithContentDescription("Subir el ajuste de la 17").performClick()
        assertEquals(16 to 1, pasos)
        // Se abre desplazada a la marcha puesta: la cabecera y «Restablecer» quedan arriba.
        compose.onNode(hasScrollAction()).performScrollToIndex(0)
        compose.onNodeWithText("Tabla de marchas").assertIsDisplayed()
        compose.onNodeWithText("Restablecer").performClick()
        assertEquals(true, restablecida)
    }

    @Test
    fun extremo_deshabilita_el_boton() {
        val c = Configuracion().let { it.conTabla(it.transmisionElegida, List(24) { 20.0 }) }
        mostrar(estadoDeTabla(c, EstadoMarcha.Numerada(17, 24)))
        compose.onNodeWithContentDescription("Subir el ajuste de la 17").assertIsNotEnabled()
        compose.onNodeWithContentDescription("Bajar el ajuste de la 17").assertIsEnabled()
    }
}
