package app.smartbike.ui.conexion

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import app.smartbike.dominio.AjustesConexion
import app.smartbike.dominio.Identidad
import app.smartbike.dominio.ModoConexion
import app.smartbike.ui.createComposeRuleEs
import app.smartbike.ui.navegacion.Pestana
import app.smartbike.ui.tema.SqTema
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/** Estados de maquetas/conexion.html y escenarios de la spec `platform-settings`. */
class ConexionTest {
    @get:Rule
    val compose = createComposeRuleEs()

    private fun mostrar(e: ConexionUiState, acciones: AccionesConexionPestana = AccionesConexionPestana()) {
        compose.setContent { SqTema { Conexion(e, acciones) } }
    }

    private fun texto(t: String) = compose.onNodeWithText(t).performScrollTo()

    private val esperando = ConexionUiState(
        rodillo = ServicioUi(EstadoServicio.Esperando, Via.Wifi, AyudaServicio.EmparejarRodillo),
        mando = ServicioUi(EstadoServicio.Esperando, Via.Bluetooth, AyudaServicio.MandoBluetooth),
        pulso = ServicioUi(EstadoServicio.Esperando, Via.Wifi, AyudaServicio.PulsoElegir),
        nombreMovil = "OnePlus 8",
        sinRodillo = false,
    )

    @Test
    fun estado_inicial() {
        // #estado=inicio: los tres servicios esperando, Wifi + Bluetooth elegido y recomendado, «Smart Bike».
        mostrar(esperando)
        compose.onAllNodesWithText("Esperando").assertCountEquals(3)
        texto("En la pantalla de emparejamiento de tu app de entrenamiento, elige Smart Bike como rodillo, potencia y cadencia.")
        texto("Wifi + Bluetooth").assertIsSelected()
        texto("Recomendado")
        texto("Solo Bluetooth").assertIsNotSelected()
        texto("Solo wifi (modo Zwift Ride)").assertIsNotSelected()
        texto("Smart Bike").assertIsSelected()
        // Sin elección de plataforma ni interruptor del mando.
        compose.onNodeWithText("Plataforma").assertDoesNotExist()
        compose.onNodeWithText("Mando de Zwift").assertDoesNotExist()
    }

    @Test
    fun la_compatibilidad_solo_nombra_lo_probado() {
        mostrar(esperando)
        texto("Zwift: validado")
        compose.onAllNodesWithText("Zwift: validado en parte").assertCountEquals(2)
        compose.onAllNodesWithText("Otras apps con FTMS: a validar").assertCountEquals(2)
        texto("Solo para Zwift")
        compose.onNodeWithText("MyWhoosh: validado").assertDoesNotExist()
    }

    @Test
    fun sin_rodillo_no_se_publica_nada_y_lleva_a_dispositivos() {
        // #estado=primer-uso.
        var ido: Pestana? = null
        mostrar(ConexionUiState(), AccionesConexionPestana(irA = { ido = it }))
        texto("La app no publica nada hasta que configures un rodillo en Dispositivos.")
        compose.onAllNodesWithText("No se ofrece").assertCountEquals(3)
        texto("Busca el Neo en Dispositivos para publicar el rodillo.").performClick()
        assertEquals(Pestana.Dispositivos, ido)
    }

    @Test
    fun solo_bluetooth_sin_nombre_ante_la_app() {
        mostrar(
            esperando.copy(
                conexion = AjustesConexion(ModoConexion.FTMS_BLUETOOTH),
                rodillo = ServicioUi(EstadoServicio.Esperando, Via.Bluetooth, AyudaServicio.EmparejarRodilloMovil),
                mando = ServicioUi(EstadoServicio.Esperando, Via.DentroBluetooth, AyudaServicio.MandoDentro),
            ),
        )
        texto("Solo Bluetooth").assertIsSelected()
        texto("Dentro de OnePlus 8 · por Bluetooth")
        compose.onNodeWithText("Nombre ante la app").assertDoesNotExist()
    }

    @Test
    fun elegir_el_modo_y_el_nombre() {
        var modo: ModoConexion? = null
        var nombre: Identidad? = null
        mostrar(esperando, AccionesConexionPestana(modo = { modo = it }, identidad = { nombre = it }))
        texto("Solo wifi (modo Zwift Ride)").performClick()
        assertEquals(ModoConexion.ZWIFT_RIDE, modo)
        texto("KICKR BIKE PRO").performClick()
        assertEquals(Identidad.KICKR_BIKE_PRO, nombre)
    }

    @Test
    fun pulso_enviando_sin_mostrar_bpm() {
        mostrar(esperando.copy(pulso = ServicioUi(EstadoServicio.Enviando, Via.Wifi, null)))
        texto("Enviando")
        compose.onNodeWithText("bpm", substring = true).assertDoesNotExist()
    }
}
