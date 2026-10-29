package app.smartbike.ui.dispositivos

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import app.smartbike.sesion.Sesion
import app.smartbike.ui.createComposeRuleEs
import app.smartbike.ui.tema.SqTema
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/** Estados de maquetas/dispositivos.html y escenarios de la spec `device-connection`. */
class DispositivosTest {
    @get:Rule
    val compose = createComposeRuleEs()

    private fun mostrar(e: DispositivosUiState, acciones: AccionesDispositivos = AccionesDispositivos()) {
        compose.setContent { SqTema { Dispositivos(e, acciones) } }
    }

    private fun texto(t: String) = compose.onNodeWithText(t).performScrollTo()

    /** Un botón por su descripción accesible, que nombra el tipo («Buscar rodillo»); el texto visible no. */
    private fun boton(descripcion: String) = compose.onNode(hasContentDescription(descripcion), useUnmergedTree = true).performScrollTo()

    /** El botón entero, en el árbol fusionado: para saber si está habilitado (la descripción la lleva su texto). */
    private fun botonEntero(descripcion: String) = compose.onNode(hasContentDescription(descripcion)).performScrollTo()

    private fun noHay(descripcion: String) = compose.onNode(hasContentDescription(descripcion), useUnmergedTree = true).assertDoesNotExist()

    private val conectados = DispositivosUiState(
        neo = FilaDispositivoUi(EstadoFila.Conectado, "Tacx Neo 12345"),
        square = FilaDispositivoUi(EstadoFila.Conectado, "Elite Square"),
    )

    @Test
    fun primer_uso_solo_buscar_en_cada_seccion() {
        // #estado=primer-uso: cada sección con «Ninguno configurado» y su «Buscar», sin «Buscar dispositivos».
        var rodillo = 0
        var mando = 0
        mostrar(DispositivosUiState(), AccionesDispositivos(buscarRodillo = { rodillo++ }, buscarMando = { mando++ }))
        boton("Buscar rodillo").performClick()
        boton("Buscar controlador").performClick()
        boton("Buscar pulsómetro")
        boton("Buscar ventilador")
        noHay("Olvidar rodillo")
        compose.onNodeWithText("Buscar dispositivos").assertDoesNotExist()
        assertEquals(1, rodillo)
        assertEquals(1, mando)
    }

    @Test
    fun conectado_solo_olvidar() {
        // Spec `device-connection`, «Botones según el estado»: configurado y conectado, solo «Olvidar».
        val olvidados = mutableListOf<Sesion.Dispositivo>()
        mostrar(conectados, AccionesDispositivos(olvidar = { olvidados += it }))
        texto("Tacx Neo 12345")
        texto("Elite Square")
        noHay("Buscar rodillo")
        noHay("Reconectar rodillo")
        boton("Olvidar controlador").performClick()
        boton("Olvidar rodillo").performClick()
        assertEquals(listOf(Sesion.Dispositivo.Square, Sesion.Dispositivo.Neo), olvidados)
    }

    @Test
    fun configurado_sin_conexion_reconectar_y_olvidar() {
        // #estado=square-reconectando.
        val reconectados = mutableListOf<Sesion.Dispositivo>()
        mostrar(conectados.copy(square = FilaDispositivoUi(EstadoFila.Conectando, "Elite Square")), AccionesDispositivos(reconectar = { reconectados += it }))
        boton("Reconectar controlador").performClick()
        boton("Olvidar controlador")
        noHay("Buscar controlador")
        texto("Pulsa cualquier botón para despertarlo. La app lo intenta sola; si lo acabas de despertar, toca «Reconectar» para no esperar.")
        assertEquals(listOf(Sesion.Dispositivo.Square), reconectados)
    }

    @Test
    fun buscando_uno_los_demas_siguen_disponibles() {
        mostrar(DispositivosUiState(neo = FilaDispositivoUi(EstadoFila.Buscando)))
        botonEntero("Buscar rodillo").assertIsNotEnabled()
        botonEntero("Buscar controlador").assertIsEnabled()
    }

    @Test
    fun los_botones_dicen_solo_la_accion() {
        mostrar(DispositivosUiState())
        boton("Buscar pulsómetro")
        compose.onNodeWithText("Buscar pulsómetro").assertDoesNotExist()
    }

    @Test
    fun pulsometro_conectado_con_su_ayuda() {
        mostrar(conectados.copy(pulsometro = FilaDispositivoUi(EstadoFila.Conectado, "COOSPO HW9")))
        texto("COOSPO HW9")
        texto("La app publica tu pulso: en tu app de entrenamiento, elige Smart Bike también como pulsómetro. Si la banda ya está conectada a otro dispositivo, puede que no admita otra conexión.")
        boton("Olvidar pulsómetro")
    }

    @Test
    fun bateria_baja_aviso_informativo_bajo_el_controlador() {
        // Spec `device-connection`, «Batería baja del Square en Dispositivos»; estado `square-bateria-baja` de la maqueta.
        mostrar(conectados.copy(bateriaSquareBajaPct = 15))
        texto("Batería del Square baja (15 %). Revisa la pila antes de rodar.")
        // El controlador sigue conectado y con sus botones.
        boton("Olvidar controlador")
    }

    @Test
    fun sin_bateria_baja_no_hay_aviso_de_bateria() {
        mostrar(conectados)
        compose.onNodeWithText("Batería del Square baja", substring = true).assertDoesNotExist()
    }
}
