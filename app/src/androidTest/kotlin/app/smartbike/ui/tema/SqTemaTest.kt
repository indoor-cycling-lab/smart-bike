package app.smartbike.ui.tema

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import app.smartbike.dominio.TemaApp
import app.smartbike.ui.createComposeRuleEs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SqTemaTest {
    @get:Rule
    val compose = createComposeRuleEs()

    private class Lectura {
        var tema: Tema? = null
        var colores: SqColores? = null
        var contenido: Color? = null
    }

    @Composable
    private fun Leer(lectura: Lectura) {
        lectura.tema = SqTema.tema
        lectura.colores = SqTema.colores
        lectura.contenido = LocalContentColor.current
    }

    @Test
    fun sin_tema_elegido_usa_rodaje() {
        val lectura = Lectura()
        compose.setContent {
            SqTema(modifier = Modifier.testTag("raiz")) {
                Box(Modifier.size(48.dp))
                Leer(lectura)
            }
        }
        compose.waitForIdle()

        assertEquals(Tema.Rodaje, lectura.tema)
        assertEquals(ColoresRodaje, lectura.colores)
        assertEquals(ColoresRodaje.ink, lectura.contenido)
        assertEquals(ColoresRodaje.bg, colorCentral("raiz"))
    }

    @Test
    fun tema_claro_anidado_cambia_solo_su_zona() {
        val fuera = Lectura()
        val dentro = Lectura()
        compose.setContent {
            SqTema {
                Column {
                    Box(Modifier.size(48.dp).testTag("rodaje")) { Leer(fuera) }
                    SqTema(Tema.Claro, Modifier.size(48.dp).testTag("claro")) { Leer(dentro) }
                }
            }
        }
        compose.waitForIdle()

        assertEquals(Tema.Rodaje, fuera.tema)
        assertEquals(ColoresRodaje.ink, fuera.contenido)
        assertEquals(ColoresRodaje.bg, colorCentral("rodaje"))

        assertEquals(Tema.Claro, dentro.tema)
        assertEquals(ColoresClaro, dentro.colores)
        assertEquals(ColoresClaro.ink, dentro.contenido)
        assertEquals(ColoresClaro.bg, colorCentral("claro"))
    }

    @Test
    fun focus_en_claro_es_gear_de_claro() {
        val dentro = Lectura()
        compose.setContent {
            SqTema { SqTema(Tema.Claro) { Leer(dentro) } }
        }
        compose.waitForIdle()

        assertEquals(ColoresClaro.gear, dentro.colores?.focus)
        assertNotEquals(ColoresRodaje.gear, dentro.colores?.focus)
    }

    private fun colorCentral(etiqueta: String): Color {
        val pixeles = compose.onNodeWithTag(etiqueta).captureToImage().toPixelMap()
        return pixeles[pixeles.width / 2, pixeles.height / 2]
    }

    /** Como `line-height` en CSS: la caja del texto mide el interlineado, también si es menor que el cuerpo. */
    @Test
    fun interlineado_como_en_css() {
        compose.setContent {
            SqTema {
                Column {
                    androidx.compose.material3.Text("14", style = SqTipografia.gearHero, modifier = Modifier.testTag("marcha"))
                    androidx.compose.material3.Text("247", style = SqTipografia.metricXl, modifier = Modifier.testTag("dato"))
                    androidx.compose.material3.Text("Cuerpo", style = SqTipografia.body, modifier = Modifier.testTag("cuerpo"))
                }
            }
        }
        compose.onNodeWithTag("marcha").assertHeightIsEqualTo(152.dp)
        compose.onNodeWithTag("dato").assertHeightIsEqualTo(72.dp)
        // Interlineado mayor que el alto natural de la fuente: también lo respeta.
        compose.onNodeWithTag("cuerpo").assertHeightIsEqualTo(SqTipografia.body.lineHeight.value.dp)
    }

    /** Spec `app-theme`: hasta leer la configuración no se pinta ninguna pantalla, ni en un tema ni en otro. */
    @Test
    fun sin_tema_leido_no_compone_nada() {
        var compuesta = false
        compose.setContent { SqTemaElegido(null) { compuesta = true } }
        compose.waitForIdle()
        assertEquals(false, compuesta)
    }

    /** Spec `app-theme`, «Girar el móvil en claro»: con el tema ya leído, la primera composición sale en claro. */
    @Test
    fun claro_elegido_desde_la_primera_composicion() {
        val vistos = mutableListOf<Tema>()
        compose.setContent {
            SqTemaElegido(TemaApp.CLARO, Modifier.testTag("raiz")) {
                Box(Modifier.size(48.dp))
                vistos += SqTema.tema
            }
        }
        compose.waitForIdle()
        assertTrue(vistos.isNotEmpty() && vistos.all { it == Tema.Claro })
        assertEquals(ColoresClaro.bg, colorCentral("raiz"))
    }

    /** Spec `settings-screen`, «Tema en Ajustes»: cambiar el tema no reinicia lo que hay dentro (la sesión, la pila). */
    @Test
    fun cambiar_de_tema_conserva_el_estado() {
        var tema by mutableStateOf(TemaApp.OSCURO)
        val lectura = Lectura()
        var contador: (() -> Int)? = null
        compose.setContent {
            SqTemaElegido(tema, Modifier.testTag("raiz")) {
                val pulsaciones = remember { intArrayOf(0) }
                contador = { pulsaciones[0] }
                pulsaciones[0]++
                Box(Modifier.size(48.dp))
                Leer(lectura)
            }
        }
        compose.waitForIdle()
        assertEquals(Tema.Rodaje, lectura.tema)
        val antes = contador!!()
        tema = TemaApp.CLARO
        compose.waitForIdle()
        assertEquals(Tema.Claro, lectura.tema)
        assertEquals(ColoresClaro.bg, colorCentral("raiz"))
        // El mismo `remember`: se ha recompuesto, no se ha vuelto a crear.
        assertTrue(contador!!() > antes)
    }
}
