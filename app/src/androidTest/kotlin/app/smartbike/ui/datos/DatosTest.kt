package app.smartbike.ui.datos

import android.content.res.Configuration
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import app.smartbike.dominio.DatoRodaje
import app.smartbike.dominio.DatoRodaje.CADENCIA
import app.smartbike.dominio.DatoRodaje.POTENCIA
import app.smartbike.dominio.DatoRodaje.POTENCIA_MAXIMA
import app.smartbike.dominio.DatoRodaje.POTENCIA_MEDIA
import app.smartbike.dominio.DatoRodaje.TIEMPO
import app.smartbike.dominio.DatoRodaje.W_KG
import app.smartbike.ui.createComposeRuleEs
import app.smartbike.ui.tema.SqTema
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** Estados de maquetas/datos.html y escenarios de la spec `settings-screen`, «Pantalla Datos en pantalla». */
class DatosTest {
    @get:Rule
    val compose = createComposeRuleEs()

    private fun mostrar(elegidos: List<DatoRodaje>, acciones: AccionesDatos = AccionesDatos()) {
        compose.setContent { SqTema { Datos(DatosUiState(elegidos), acciones) } }
    }

    private fun texto(t: String) = compose.onNodeWithText(t).performScrollTo()

    /** Como [mostrar], con una ventana de [alto] dp de alto (la pantalla baja es < 760 dp). */
    private fun mostrarEnAlto(alto: Int, elegidos: List<DatoRodaje>, botones: Boolean) {
        compose.setContent {
            val configuracion = Configuration(LocalConfiguration.current).apply { screenHeightDp = alto }
            CompositionLocalProvider(LocalConfiguration provides configuracion) {
                SqTema { Datos(DatosUiState(elegidos, botones = botones), AccionesDatos()) }
            }
        }
    }

    private val seis = listOf(POTENCIA, CADENCIA, TIEMPO, W_KG, POTENCIA_MEDIA, POTENCIA_MAXIMA)
    private val aviso = "Con los botones − y + activados, en esta pantalla Rodaje muestra solo los 4 primeros datos. No se verán Potencia media y Potencia máxima."

    @Test
    fun por_defecto() {
        mostrar(listOf(POTENCIA, CADENCIA))
        texto("2 de 6")
        texto("Media de 3 s")
        texto("Tiempo").assertIsOff()
        texto("En ERG, la potencia objetivo se muestra siempre y no cuenta como dato.")
    }

    @Test
    fun anadir_un_dato() {
        val alternados = mutableListOf<DatoRodaje>()
        mostrar(listOf(POTENCIA, CADENCIA), AccionesDatos(alternar = { alternados += it }))
        texto("Mientras pedaleas").performClick()
        assertEquals(listOf(TIEMPO), alternados)
    }

    @Test
    fun al_menos_uno() {
        mostrar(listOf(POTENCIA))
        texto("1 de 6")
        texto("Hace falta al menos un dato")
        compose.onNodeWithText("Potencia").performScrollTo()
        compose.onNodeWithText("Hace falta al menos un dato").performScrollTo()
        // La fila del único dato no se puede desmarcar.
        compose.onNodeWithText("Hace falta al menos un dato").assertIsNotEnabled().assertIsOn()
    }

    @Test
    fun maximo_de_seis_entre_ocho() {
        // Spec `settings-screen`, «Todos los datos»: con seis marcados, velocidad y distancia quedan deshabilitadas.
        mostrar(DatoRodaje.entries.take(6))
        texto("6 de 6")
        compose.onAllNodesWithText("Quita uno para añadir otro").assertCountEquals(2)
        compose.onNodeWithText("Velocidad").assertIsNotEnabled()
    }

    @Test
    fun velocidad_y_distancia_con_su_ayuda() {
        mostrar(listOf(POTENCIA, CADENCIA))
        texto("La calcula el modelo físico; con otro modelo, «—»")
        texto("Mientras pedaleas, con el modelo físico")
    }

    @Test
    fun ordenar_sin_arrastrar() {
        // «Ordenar sin arrastrar»: «Subir» y «Bajar» en el asa, para el lector de pantalla.
        val movidos = mutableListOf<Pair<DatoRodaje, Int>>()
        mostrar(listOf(POTENCIA, CADENCIA), AccionesDatos(mover = { d, n -> movidos += d to n }))
        val acciones = compose.onNodeWithContentDescription("Reordenar Cadencia").performScrollTo().fetchSemanticsNode()
            .config.getOrNull(SemanticsActions.CustomActions).orEmpty()
        assertEquals(listOf("Subir"), acciones.map { it.label })
        acciones.single().action()
        assertEquals(listOf(CADENCIA to -1), movidos)
        // Sin asa en los no elegidos.
        compose.onNodeWithContentDescription("Reordenar Tiempo").assertDoesNotExist()
    }

    @Test
    fun ordenar_arrastrando() {
        // «Ordenar»: arrastrar el asa de la cadencia por encima de la potencia.
        val movidos = mutableListOf<Pair<DatoRodaje, Int>>()
        mostrar(listOf(POTENCIA, CADENCIA), AccionesDatos(mover = { d, n -> movidos += d to n }))
        compose.onNodeWithContentDescription("Reordenar Cadencia").performScrollTo().performTouchInput { swipeUp(startY = centerY, endY = centerY - height * 1.2f) }
        assertTrue(movidos.isNotEmpty())
        assertEquals(CADENCIA, movidos.single().first)
        assertTrue(movidos.single().second < 0)
    }

    @Test
    fun aviso_con_seis_datos_botones_y_pantalla_baja() {
        mostrarEnAlto(640, seis, botones = true)
        compose.onNodeWithText(aviso).assertExists()
        texto("6 de 6")
    }

    @Test
    fun aviso_con_cinco_datos_nombra_solo_el_quinto() {
        mostrarEnAlto(640, seis.take(5), botones = true)
        compose.onNodeWithText("Con los botones − y + activados, en esta pantalla Rodaje muestra solo los 4 primeros datos. No se verá Potencia media.").assertExists()
        texto("5 de 6")
    }

    @Test
    fun sin_aviso_sin_botones_con_cuatro_datos_o_en_pantalla_alta() {
        mostrarEnAlto(640, seis, botones = false)
        compose.onAllNodesWithText("Con los botones", substring = true).assertCountEquals(0)
    }

    @Test
    fun sin_aviso_con_cuatro_datos_y_botones() {
        mostrarEnAlto(640, seis.take(4), botones = true)
        compose.onAllNodesWithText("Con los botones", substring = true).assertCountEquals(0)
    }

    @Test
    fun sin_aviso_en_pantalla_alta() {
        mostrarEnAlto(844, seis, botones = true)
        compose.onAllNodesWithText("Con los botones", substring = true).assertCountEquals(0)
    }
}
