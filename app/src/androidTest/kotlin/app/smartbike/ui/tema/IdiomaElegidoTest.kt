package app.smartbike.ui.tema

import android.app.Activity
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import app.smartbike.R
import app.smartbike.dominio.IdiomaApp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test

/** Spec `app-language`: los textos de la composición siguen al idioma elegido y la actividad se sigue encontrando. */
class IdiomaElegidoTest {
    @get:Rule
    val compose = createComposeRule()

    @Composable
    private fun Muestra() {
        androidx.compose.foundation.layout.Column {
            Text(stringResource(R.string.ajustes))
            Text(pluralStringResource(R.plurals.accion_mas_marchas, 1, 1))
            Text(pluralStringResource(R.plurals.accion_mas_marchas, 3, 3))
        }
    }

    @Test
    fun los_textos_siguen_al_idioma_y_cambian_al_momento() {
        var idioma by mutableStateOf<IdiomaApp?>(IdiomaApp.ES)
        compose.setContent { IdiomaElegido(idioma) { Muestra() } }
        compose.onNodeWithText("Ajustes").assertExists()
        compose.onNodeWithText("+1 marcha").assertExists()
        compose.onNodeWithText("+3 marchas").assertExists()
        idioma = IdiomaApp.EN
        compose.onNodeWithText("Settings").assertExists()
        compose.onNodeWithText("+1 gear").assertExists()
        compose.onNodeWithText("+3 gears").assertExists()
    }

    @Test
    fun el_contexto_del_servicio_lee_los_textos_del_idioma() {
        // Spec `app-language`, «Notificación de la sesión»: la notificación se construye con `conIdioma`.
        val base = androidx.test.core.app.ApplicationProvider.getApplicationContext<android.content.Context>()
        val es = base.conIdioma(IdiomaApp.ES)
        val en = base.conIdioma(IdiomaApp.EN)
        assertEquals("Sesión", es.getString(R.string.canal_sesion))
        assertEquals("Terminar", es.getString(R.string.terminar))
        assertNotEquals(es.getString(R.string.terminar), en.getString(R.string.terminar))
        assertNotEquals(es.getString(R.string.canal_sesion_descripcion), en.getString(R.string.canal_sesion_descripcion))
    }

    @Test
    fun sin_idioma_leido_no_compone_nada() {
        compose.setContent { IdiomaElegido(null) { Text("visible") } }
        compose.onNodeWithText("visible").assertDoesNotExist()
    }

    @Test
    fun la_actividad_se_sigue_encontrando() {
        var actividad: Activity? = null
        compose.setContent {
            IdiomaElegido(IdiomaApp.EN) {
                var c = LocalContext.current
                while (c is android.content.ContextWrapper && c !is Activity) c = c.baseContext
                actividad = c as? Activity
            }
        }
        compose.waitForIdle()
        assertNotNull(actividad)
    }
}
