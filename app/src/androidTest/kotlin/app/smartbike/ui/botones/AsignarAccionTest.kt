package app.smartbike.ui.botones

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import app.smartbike.dominio.Accion
import app.smartbike.dominio.Asignacion
import app.smartbike.dominio.Entrada
import app.smartbike.dominio.FuncionZwift
import app.smartbike.dominio.Regla
import app.smartbike.dominio.TipoGesto
import app.smartbike.protocolo.SquareButton
import app.smartbike.sesion.EstadoCaptura
import app.smartbike.ui.createComposeRuleEs
import app.smartbike.ui.tema.SqTema
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/** Un test por estado de maquetas/asignar.html, con la acción «−3 marchas». */
class AsignarAccionTest {
    @get:Rule
    val compose = createComposeRuleEs()

    private val menos3 = Accion.MarchaMenos(3)

    private fun mostrar(
        captura: EstadoCaptura,
        gesto: TipoGesto = TipoGesto.Pulsar,
        acciones: AccionesAsignar = AccionesAsignar(),
        accion: Accion = menos3,
    ) {
        compose.setContent {
            SqTema { AsignarAccion(estadoDeAsignar(accion, Asignacion.POR_DEFECTO, emptyList(), captura, gesto), acciones) }
        }
    }

    private fun texto(t: String) = compose.onNodeWithText(t).performScrollTo()

    @Test
    fun esperando() {
        val quitadas = mutableListOf<Regla>()
        mostrar(EstadoCaptura.Esperando, acciones = AccionesAsignar(quitar = { quitadas += it }))
        texto("−3 marchas")
        texto("Esperando…")
        texto("Guardar").assertIsNotEnabled()
        compose.onNodeWithContentDescription("Quitar Campag. I + Palanca I1 pulsar").performClick()
        assertEquals(listOf(Regla(Entrada(SquareButton.PalancaIzquierda1, SquareButton.CampagnoloIzquierdo), TipoGesto.Pulsar, menos3)), quitadas)
    }

    @Test
    fun sin_square() {
        mostrar(EstadoCaptura.SinSquare)
        texto("Conecta el Square para capturar un botón")
        texto("Guardar").assertIsNotEnabled()
    }

    @Test
    fun detectado() {
        var guardar = 0
        mostrar(
            EstadoCaptura.Capturada(Entrada(SquareButton.Triangulo, SquareButton.CampagnoloDerecho)),
            TipoGesto.Corta,
            AccionesAsignar(guardar = { guardar++ }),
        )
        texto("Detectado: combinación")
        texto("Corta").assertIsSelected()
        texto("Campag. D + △ corta está libre.")
        texto("Guardar").assertIsEnabled().performClick()
        assertEquals(1, guardar)
    }

    @Test
    fun en_uso() {
        mostrar(EstadoCaptura.Capturada(Entrada(SquareButton.PalancaDerecha1)))
        texto("Detectado: botón")
        texto("Palanca D1 pulsar ya hace «+1 marcha». Si guardas, dejará de hacerlo.")
    }

    @Test
    fun a_con_el_mando_activado_solo_avisa_de_que_deja_de_seleccionar() {
        // Spec `button-mapping`, «Asignar A con el mando activado».
        mostrar(EstadoCaptura.Capturada(Entrada(SquareButton.A)), accion = Accion.MarchaMas(1))
        texto("A pulsar ya hace «Seleccionar». Si guardas, dejará de hacerlo.")
        compose.onNodeWithText("Zwift también recibirá", substring = true).assertDoesNotExist()
    }

    @Test
    fun funcion_de_zwift() {
        // Estado `funcion-zwift` de la maqueta: «Ride On» con Z, Campag. D + △ capturada con «pulsar».
        mostrar(
            EstadoCaptura.Capturada(Entrada(SquareButton.Triangulo, SquareButton.CampagnoloDerecho)),
            accion = Accion.Mando(FuncionZwift.Z),
        )
        texto("Ride On")
        texto("Función de Zwift: llega a Zwift por el controlador Zwift de la app")
        texto("Pulsar: Zwift la recibe al instante y mientras se mantiene el botón, como en un controlador oficial. Corta, larga y doble le envían una pulsación breve.")
        texto("Campag. D + △ pulsar está libre.")
    }

    @Test
    fun campagnolo_solo_sin_pulsar() {
        val elegidos = mutableListOf<TipoGesto>()
        mostrar(EstadoCaptura.Capturada(Entrada(SquareButton.CampagnoloIzquierdo)), TipoGesto.Corta, AccionesAsignar(elegirGesto = { elegidos += it }))
        texto("Pulsar").assertIsNotEnabled().performClick()
        texto("Larga").performClick()
        assertEquals(listOf(TipoGesto.Larga), elegidos)
    }
}
