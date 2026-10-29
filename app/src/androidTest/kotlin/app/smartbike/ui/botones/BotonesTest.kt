package app.smartbike.ui.botones

import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import app.smartbike.dominio.Accion
import app.smartbike.dominio.Asignacion
import app.smartbike.dominio.Configuracion
import app.smartbike.dominio.ConfiguracionBotones
import app.smartbike.dominio.Desarrollos
import app.smartbike.dominio.Entrada
import app.smartbike.dominio.FuncionZwift
import app.smartbike.dominio.TiemposGestos
import app.smartbike.dominio.TipoGesto
import app.smartbike.protocolo.SquareButton
import app.smartbike.ui.createComposeRuleEs
import app.smartbike.ui.navegacion.Pestana
import app.smartbike.ui.tema.SqTema
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** Un test por estado de maquetas/botones.html. */
class BotonesTest {
    @get:Rule
    val compose = createComposeRuleEs()

    private fun mostrar(c: Configuracion = Configuracion(), confirmando: Boolean = false, acciones: AccionesBotones = AccionesBotones(), squareConfigurado: Boolean = true) {
        compose.setContent { SqTema { Botones(estadoDeBotones(c, confirmando, squareConfigurado), acciones) } }
    }

    private fun texto(t: String) = compose.onNodeWithText(t).performScrollTo()

    private fun arriba(t: String) = compose.onNodeWithText(t).fetchSemanticsNode().positionInRoot.y

    @Test
    fun grupos_en_orden() {
        mostrar()
        assertTrue(arriba("Acciones de la app") < arriba("Funciones de Zwift"))
        assertTrue(arriba("Funciones de Zwift") < arriba("Gestos"))
    }

    @Test
    fun lista() {
        var asignada: Accion? = null
        mostrar(acciones = AccionesBotones(asignar = { asignada = it }))
        texto("Funciones de Zwift")
        texto("Llegan a tu app de entrenamiento por el controlador Zwift de la app, por Bluetooth, como con un controlador de Zwift. Toca una función para cambiar su botón.")
        texto("Seleccionar")
        texto("Freno izquierdo")
        // Spec `button-mapping`, «Funciones de Zwift»: tocar «Ride On» abre Asignar acción.
        texto("Ride On").performClick()
        assertEquals(Accion.Mando(FuncionZwift.Z), asignada)
        // Sin la fila de las palancas, que cambian las marchas de la app.
        compose.onNodeWithText("Cambiar de marcha · lo hace la app").assertDoesNotExist()
        texto("Acciones de la app")
        texto("Volver a la marcha de referencia")
        texto("Fijar la marcha de referencia").performClick()
        assertEquals(Accion.FijarReferencia, asignada)
        texto("+3 marchas")
        compose.onNodeWithText("Invertir palancas").performScrollTo().assertIsOff()
        compose.onNodeWithText("Repetir al mantener").performScrollTo().assertIsOn()
        compose.onAllNodesWithText("800 ms", substring = true)[0].performScrollTo()
        compose.onNodeWithText("250 ms", substring = true).performScrollTo()
        texto("Restaurar asignación por defecto")
    }

    @Test
    fun acciones_de_erg() {
        // Spec `button-mapping`, «Acciones de ERG» y «ERG por defecto».
        var asignada: Accion? = null
        mostrar(acciones = AccionesBotones(asignar = { asignada = it }))
        texto("+10 W")
        texto("−10 W")
        texto("Alternar SIM ⇄ ERG").performClick()
        assertEquals(Accion.AlternarErgManual, asignada)
        // El orden lo prueba `BotonesUiStateTest`; aquí, que las dos de vatios, «Siguiente modelo» y las tres del ventilador
        // (change `ventilador-headwind`) salen sin botón.
        compose.onAllNodesWithText("Sin botón").assertCountEquals(6)
    }

    @Test
    fun modificada() {
        val a = Asignacion.POR_DEFECTO
            .quitar(Entrada(SquareButton.PalancaIzquierda2, SquareButton.CampagnoloIzquierdo), TipoGesto.Pulsar)
            .asignar(Entrada(SquareButton.Triangulo, SquareButton.CampagnoloDerecho), TipoGesto.Corta, Accion.MarchaMenos(3))
            .copy(invertirPalancas = true)
        mostrar(Configuracion().conAsignacion(a))
        // «Marcha más ligera», «+10 W», «−10 W», «Siguiente modelo» y las tres del ventilador sin botón; △ en «−3 marchas» y en «Alternar SIM ⇄ ERG».
        compose.onAllNodesWithText("Sin botón").assertCountEquals(7)
        compose.onAllNodesWithContentDescription("triángulo").assertCountEquals(2)
        // △ corta en «−3 marchas» y □ corta en «Volver a la marcha de referencia».
        compose.onAllNodesWithText("corta").assertCountEquals(2)
        compose.onNodeWithText("Invertir palancas").performScrollTo().assertIsOn()
    }

    @Test
    fun restaurar() {
        var restaurar = 0
        var cancelar = 0
        mostrar(confirmando = true, acciones = AccionesBotones(restaurar = { restaurar++ }, cancelarRestaurar = { cancelar++ }))
        texto("La asignación, las palancas invertidas y los tiempos volverán a los de fábrica.")
        texto("Cancelar").performClick()
        texto("Restaurar").performClick()
        assertEquals(1, restaurar)
        assertEquals(1, cancelar)
        assertEquals(0, compose.onAllNodesWithText("Restaurar asignación por defecto").fetchSemanticsNodes().size)
    }

    @Test
    fun como_secundaria_de_ajustes() {
        // Spec `button-mapping`, «Abrir desde Ajustes»: con volver a Ajustes y sin barra inferior.
        var volvio = 0
        val tiempos = mutableListOf<Pair<Tiempo, Int>>()
        mostrar(
            Configuracion(botones = ConfiguracionBotones(tiempos = TiemposGestos(dobleMs = 600))),
            acciones = AccionesBotones(volver = { volvio++ }, tiempo = { t, n -> tiempos += t to n }),
        )
        assertEquals(0, compose.onAllNodesWithText("Conexión").fetchSemanticsNodes().size)
        compose.onNodeWithContentDescription("Volver a Ajustes").performClick()
        assertEquals(1, volvio)
        // Spec `button-mapping`, «Acciones de pantalla».
        texto("Pantalla anterior")
        texto("Pantalla siguiente")
        // Límite: la ventana de la doble en 600 ms no sube.
        compose.onNodeWithContentDescription("Subir doble pulsación").performScrollTo().assertIsNotEnabled().performClick()
        compose.onNodeWithContentDescription("Bajar doble pulsación").assertIsEnabled().performClick()
        assertEquals(listOf(Tiempo.Doble to -1), tiempos)
    }

    @Test
    fun shimano() {
        // Estado `shimano` de la maqueta: su asignación, con las acciones de plato.
        mostrar(Configuracion(transmision = Desarrollos.SHIMANO_105.id))
        texto("Asignación de Shimano 105. Cada transmisión tiene la suya.")
        texto("Ride On")
        texto("Plato más grande")
        texto("Plato más pequeño")
    }

    @Test
    fun sin_square_configurado() {
        // Estado `sin-square` de la maqueta: las filas iguales y la ayuda de que el mando no se publica.
        mostrar(squareConfigurado = false)
        texto("Sin un Square configurado, el controlador no se publica y estas funciones no llegan a tu app de entrenamiento. Búscalo en Dispositivos; la asignación se conserva.")
        texto("Ride On")
    }

    @Test
    fun funcion_de_zwift_sin_boton() {
        // Estado `modificada` de la maqueta: «Usar powerup» sin botón.
        mostrar(Configuracion().let { it.conAsignacion(it.asignacion.quitar(Entrada(SquareButton.Y), TipoGesto.Pulsar)) })
        texto("Usar powerup")
        compose.onAllNodesWithText("Sin botón").assertCountEquals(7)
    }

    @Test
    fun lineal_dice_que_es_su_asignacion() {
        mostrar()
        texto("Asignación de la lineal. Cada transmisión tiene la suya.")
        compose.onNodeWithText("Plato más grande").assertDoesNotExist()
    }
}
