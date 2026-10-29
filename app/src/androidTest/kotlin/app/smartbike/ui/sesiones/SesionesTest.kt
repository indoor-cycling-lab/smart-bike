package app.smartbike.ui.sesiones

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.dp
import app.smartbike.datos.EstadoGrabacion
import app.smartbike.datos.EstadoSesion
import app.smartbike.datos.SesionGrabada
import app.smartbike.ui.createComposeRuleEs
import app.smartbike.ui.tema.SqTema
import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/** maquetas/sesiones.html y escenarios de la spec `session-recording`: lista, abrir, compartir y borrar. */
class SesionesTest {
    @get:Rule
    val compose = createComposeRuleEs()

    private fun sesion(dia: Int, hora: Int, bytes: Long, estado: EstadoSesion = EstadoSesion.CERRADA) = SesionGrabada("sesion-202610%02d-%02d0000.txt".format(dia, hora), LocalDateTime.of(2026, 10, dia, hora, 0), bytes, estado)

    private val grabando = sesion(29, 18, 12_000, EstadoSesion.GRABANDO)
    private val cerrada = sesion(28, 7, 18_000)
    private val otra = sesion(26, 19, 11_000)

    private fun mostrar(estado: SesionesUiState, acciones: AccionesSesiones = AccionesSesiones(), sinApp: Boolean = false) {
        compose.setContent { SqTema { Sesiones(estado, acciones, sinApp) } }
    }

    private fun texto(t: String) = compose.onNodeWithText(t).performScrollTo()

    @Test
    fun lista_con_una_sesion_grabandose() {
        mostrar(SesionesUiState(listOf(grabando, cerrada, otra), EstadoGrabacion.Grabando(grabando.nombre)))
        texto("29 oct 2026 · 18:00")
        texto("Grabando · 12 KB")
        texto("18 KB")
        texto("11 KB")
        texto("Borrar todas")
        // La que se graba no ofrece «Borrar»; las cerradas, sí.
        compose.onAllNodesWithContentDescription("Borrar la sesión del 29 oct 2026 · 18:00").assertCountEquals(0)
        compose.onNodeWithContentDescription("Borrar la sesión del 28 oct 2026 · 07:00").assertExists()
    }

    @Test
    fun la_ayuda_avisa_de_direcciones_y_nombres() {
        mostrar(SesionesUiState(listOf(cerrada)))
        compose.onNodeWithText("direcciones de red y nombres de dispositivos", substring = true).assertIsDisplayed()
    }

    @Test
    fun sin_sesiones_dice_como_grabar_y_no_ofrece_borrar_todas() {
        mostrar(SesionesUiState())
        texto("No hay sesiones grabadas")
        compose.onNodeWithText("Activa «Grabar sesiones»", substring = true).assertExists()
        compose.onAllNodesWithText("Borrar todas").assertCountEquals(0)
    }

    @Test
    fun detenida_por_poco_espacio_y_por_un_error() {
        mostrar(
            SesionesUiState(
                listOf(sesion(29, 18, 8_000, EstadoSesion.DETENIDA_POR_ESPACIO), cerrada),
                EstadoGrabacion.DetenidaPorEspacio("sesion-20261029-180000.txt"),
            ),
        )
        texto("Detenida por poco espacio · 8 KB")
    }

    @Test
    fun detenida_por_un_error() {
        mostrar(SesionesUiState(listOf(sesion(29, 18, 5_000, EstadoSesion.DETENIDA_POR_ERROR)), EstadoGrabacion.DetenidaPorError("a.txt")))
        texto("Detenida por un error · 5 KB")
    }

    @Test
    fun abrir_compartir_y_borrar_llaman_a_su_accion() {
        var abierta: String? = null
        var compartida: String? = null
        var borrar: String? = null
        mostrar(
            SesionesUiState(listOf(cerrada)),
            AccionesSesiones(abrir = { abierta = it.nombre }, compartir = { compartida = it.nombre }, pedirBorrar = { borrar = it.nombre }),
        )
        compose.onNodeWithContentDescription("Abrir la sesión del 28 oct 2026 · 07:00").performScrollTo().performClick()
        compose.onNodeWithContentDescription("Compartir la sesión del 28 oct 2026 · 07:00").performScrollTo().performClick()
        compose.onNodeWithContentDescription("Borrar la sesión del 28 oct 2026 · 07:00").performScrollTo().performClick()
        assertEquals(cerrada.nombre, abierta)
        assertEquals(cerrada.nombre, compartida)
        assertEquals(cerrada.nombre, borrar)
    }

    @Test
    fun las_acciones_miden_al_menos_48_dp() {
        mostrar(SesionesUiState(listOf(cerrada)))
        for (accion in listOf("Abrir", "Compartir", "Borrar")) {
            compose.onNodeWithContentDescription("$accion la sesión del 28 oct 2026 · 07:00").performScrollTo().assertHeightIsAtLeast(48.dp)
        }
    }

    @Test
    fun confirmar_el_borrado_de_una_con_borrar_y_cancelar() {
        var confirmada = false
        var cancelada = false
        mostrar(
            SesionesUiState(listOf(grabando, cerrada), EstadoGrabacion.Grabando(grabando.nombre), Confirmacion.Una(cerrada.nombre)),
            AccionesSesiones(confirmar = { confirmada = true }, cancelar = { cancelada = true }),
        )
        texto("Se borrará la sesión del 28 oct 2026 · 07:00 (18 KB). No se puede deshacer.")
        texto("Cancelar").performClick()
        assertEquals(true, cancelada)
        texto("Borrar").performClick()
        assertEquals(true, confirmada)
    }

    @Test
    fun confirmar_borrar_todas_respeta_la_que_se_graba() {
        mostrar(SesionesUiState(listOf(grabando, cerrada, otra), EstadoGrabacion.Grabando(grabando.nombre), Confirmacion.Todas))
        texto("Se borrarán las 2 sesiones cerradas (29 KB). La que se está grabando se conserva. No se puede deshacer.")
        compose.onAllNodesWithText("Borrar todas").assertCountEquals(0)
    }

    @Test
    fun confirmar_borrar_todas_con_una_sola_cerrada_y_sin_grabacion() {
        mostrar(SesionesUiState(listOf(cerrada), confirmacion = Confirmacion.Todas))
        texto("Se borrará la sesión cerrada (18 KB). No se puede deshacer.")
    }

    @Test
    fun avisa_si_ninguna_app_lo_abre() {
        mostrar(SesionesUiState(listOf(cerrada)), sinApp = true)
        compose.onNodeWithText("No hay ninguna app que pueda abrir este archivo.").assertIsDisplayed()
    }
}
