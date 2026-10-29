package app.smartbike.ui.marchas

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import app.smartbike.dominio.Configuracion
import app.smartbike.dominio.Desarrollos
import app.smartbike.dominio.EstadoMarcha
import app.smartbike.dominio.ModoCambio
import app.smartbike.ui.createComposeRuleEs
import app.smartbike.ui.tema.SqTema
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** Estados `lineal`, `shimano` y `shimano-manual` de maquetas/marchas.html y escenarios de la spec `settings-screen`. */
class MarchasTest {
    @get:Rule
    val compose = createComposeRuleEs()

    private val shimano = Desarrollos.SHIMANO_105

    private fun mostrar(c: Configuracion, acciones: AccionesMarchas = AccionesMarchas()) {
        compose.setContent { SqTema { Marchas(estadoDeMarchas(c), acciones) } }
    }

    private fun texto(t: String) = compose.onNodeWithText(t).performScrollTo()

    @Test
    fun lineal() {
        var volvio = false
        mostrar(Configuracion(), AccionesMarchas(volver = { volvio = true }))
        texto("Lineal").assertIsSelected()
        texto("Shimano 105 2×12").assertIsNotSelected()
        texto("24 marchas numeradas, sin platos ni piñones")
        texto("52/36 · 11–36")
        texto("Referencia: 12")
        texto("Palancas D más dura, palancas I más ligera.")
        compose.onNodeWithText("Cómo se cambia").assertDoesNotExist()
        texto("Recordar la última marcha").assertIsOn()
        compose.onNodeWithContentDescription("Volver a Ajustes").performClick()
        assertTrue(volvio)
    }

    @Test
    fun shimano_automatico() {
        var elegida: String? = null
        mostrar(Configuracion(transmision = shimano.id), AccionesMarchas(transmision = { elegida = it }))
        texto("Shimano 105 2×12").assertIsSelected()
        compose.onNodeWithContentDescription("Plato 36").performScrollTo()
        compose.onNodeWithContentDescription("Cassette 11 a 36, piñón 17").performScrollTo()
        texto("Referencia: 36×17")
        texto("Automático").assertIsSelected()
        texto("Manual").assertIsNotSelected()
        texto("Palancas D más dura y más ligera; el plato cambia solo, como el Di2 en Synchro Shift")
        // Tocar cualquier punto de la fila elige la transmisión.
        texto("24 marchas numeradas, sin platos ni piñones").performClick()
        assertEquals("lineal", elegida)
    }

    @Test
    fun shimano_manual_y_referencia() {
        var modo: ModoCambio? = null
        var dura: Boolean? = null
        mostrar(
            Configuracion(transmision = shimano.id).conModo(ModoCambio.MANUAL),
            AccionesMarchas(modo = { modo = it }, referencia = { dura = it }),
        )
        texto("Manual").assertIsSelected()
        texto("Palancas D cambian el piñón y palancas I el plato, como tus mandos")
        texto("Automático").performClick()
        assertEquals(ModoCambio.AUTOMATICO, modo)
        compose.onNodeWithContentDescription("Referencia más dura").performScrollTo().performClick()
        assertEquals(true, dura)
    }

    @Test
    fun referencia_en_el_extremo() {
        // Spec `settings-screen`, «Extremo»: en la 52×11, el + está deshabilitado.
        mostrar(Configuracion(transmision = shimano.id).conReferencia(EstadoMarcha.PlatoPinon(52, 11)))
        compose.onNodeWithContentDescription("Referencia más dura").performScrollTo().assertIsNotEnabled()
        compose.onNodeWithContentDescription("Referencia más ligera").performScrollTo().assertIsEnabled()
    }
}
