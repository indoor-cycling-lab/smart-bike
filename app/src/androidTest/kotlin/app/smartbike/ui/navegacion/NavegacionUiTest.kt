package app.smartbike.ui.navegacion

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import app.smartbike.dominio.Accion
import app.smartbike.dominio.OrientacionApp
import app.smartbike.dominio.TemaApp
import app.smartbike.ui.tema.OrientacionElegida
import app.smartbike.ui.tema.SqTema
import app.smartbike.ui.tema.SqTemaElegido
import app.smartbike.ui.tema.Tema
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** Spec `app-navigation` con `AppNavegacion` y pantallas de prueba que dicen cuál son. */
class NavegacionUiTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    private var terminada = false
    private var tema by mutableStateOf(TemaApp.OSCURO)
    private var temaVisto: Tema? = null
    private var orientacion by mutableStateOf<OrientacionApp?>(null)
    private var marcha by mutableStateOf(7)
    private val pantallaSiguiente = Channel<Boolean>(Channel.UNLIMITED)

    @Composable
    private fun Pantalla(nombre: String, vararg botones: Pair<String, () -> Unit>) {
        Column {
            Text("Pantalla $nombre")
            botones.forEach { (t, a) -> Button(onClick = a) { Text(t) } }
        }
    }

    private fun mostrar() {
        compose.setContent {
            OrientacionElegida(orientacion)
            SqTemaElegido(tema) {
                temaVisto = SqTema.tema
                AppNavegacion(
                    Pantallas(
                        rodaje = { abrirDispositivos, abrirAjustes, abrirConexion ->
                            BackHandler { terminada = true }
                            Pantalla("Rodaje", "Marcha $marcha" to {}, "chip" to abrirDispositivos, "icono de Ajustes" to abrirAjustes, "estado de la conexión" to abrirConexion)
                        },
                        dispositivos = { irA -> Pantalla("Dispositivos", "a Conexión" to { irA(Pestana.Conexion) }, "a Rodar" to { irA(Pestana.Rodar) }) },
                        conexion = { irA -> Pantalla("Conexión", "a Ajustes" to { irA(Pestana.Ajustes) }, "a Rodar" to { irA(Pestana.Rodar) }) },
                        botones = { volver, asignar -> Pantalla("Botones", "volver" to volver, "asignar" to { asignar(Accion.TODAS.first()) }) },
                        ajustes = { irA, abrir ->
                            Pantalla("Ajustes", "a Botones" to { abrir(Rutas.BOTONES) }, "a Claro" to { tema = TemaApp.CLARO }, "a la orientación actual" to { orientacion = if (esHorizontal()) OrientacionApp.HORIZONTAL else OrientacionApp.VERTICAL }, "a según el móvil" to { orientacion = OrientacionApp.SEGUN_MOVIL }, "a Marchas" to { abrir(Rutas.MARCHAS) }, "a Diagnóstico" to { abrir(Rutas.DIAGNOSTICO) }, "a Sesiones" to { abrir(Rutas.SESIONES) }, "a Dispositivos" to { irA(Pestana.Dispositivos) })
                        },
                        marchas = { volver -> Pantalla("Marchas", "volver" to volver) },
                        datos = { volver -> Pantalla("Datos", "volver" to volver) },
                        tabla = { volver -> Pantalla("Tabla", "volver" to volver) },
                        diagnostico = { volver -> Pantalla("Diagnóstico", "volver" to volver) },
                        sesiones = { volver -> Pantalla("Sesiones grabadas", "volver" to volver) },
                        asignar = { _, volver -> Pantalla("Asignar", "volver" to volver) },
                    ),
                    pantallaSiguiente.receiveAsFlow(),
                )
            }
        }
    }

    private fun esHorizontal() = compose.activity.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    private fun orientacionPedida() = compose.activity.requestedOrientation

    private fun en(nombre: String) = compose.onNodeWithText("Pantalla $nombre").assertExists()

    private fun toca(t: String) {
        compose.onNodeWithText(t).performClick()
        compose.waitForIdle()
    }

    private fun atras() {
        compose.runOnUiThread { compose.activity.onBackPressedDispatcher.onBackPressed() }
        compose.waitForIdle()
    }

    private fun square(siguiente: Boolean) {
        pantallaSiguiente.trySend(siguiente)
        compose.waitForIdle()
    }

    @Test
    fun cambiar_de_pestana_y_volver_a_rodar() {
        // «Cambiar de pestaña» y «Volver a rodar».
        mostrar()
        en("Rodaje")
        toca("chip")
        en("Dispositivos")
        toca("a Conexión")
        en("Conexión")
        toca("a Rodar")
        en("Rodaje")
    }

    @Test
    fun el_estado_de_la_conexion_abre_conexion_y_atras_vuelve_a_rodaje() {
        // Spec `platform-settings`, «Conexión desde Rodaje».
        mostrar()
        toca("estado de la conexión")
        en("Conexión")
        atras()
        en("Rodaje")
        assertFalse(terminada)
    }

    @Test
    fun botones_se_abre_desde_ajustes_con_volver_y_asignar_vuelve_a_botones() {
        // Spec `app-navigation`, «Ajustes a Botones y vuelta» y «Asignar acción».
        mostrar()
        toca("icono de Ajustes")
        toca("a Botones")
        en("Botones")
        toca("asignar")
        en("Asignar")
        toca("volver")
        en("Botones")
        atras()
        en("Ajustes")
        toca("a Botones")
        toca("volver")
        en("Ajustes")
    }

    @Test
    fun icono_de_ajustes_abre_la_pestana_y_atras_vuelve_a_rodaje() {
        // Spec `ride-screen`, «Ajustes desde Rodaje»: Ajustes como pestaña; Atrás vuelve a Rodaje sin terminar la sesión.
        mostrar()
        toca("icono de Ajustes")
        en("Ajustes")
        atras()
        en("Rodaje")
        assertFalse(terminada)
    }

    @Test
    fun cambiar_de_tema_en_ajustes_no_cambia_de_pantalla() {
        // Spec `settings-screen`, «Cambiar a claro»: la pila se conserva y Rodaje sigue debajo, en claro.
        mostrar()
        toca("icono de Ajustes")
        toca("a Claro")
        en("Ajustes")
        assertEquals(Tema.Claro, temaVisto)
        atras()
        en("Rodaje")
        assertFalse(terminada)
    }

    @Test
    fun la_orientacion_elegida_se_pide_a_la_actividad() {
        // Spec `settings-screen`, «Orientación»: la actividad pide la orientación de la configuración. Se elige la que ya
        // tiene el dispositivo para que no se recree la actividad (el giro real se prueba en el móvil).
        mostrar()
        compose.runOnUiThread { orientacion = if (esHorizontal()) OrientacionApp.HORIZONTAL else OrientacionApp.VERTICAL }
        compose.waitForIdle()
        val esperada = if (esHorizontal()) ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE else ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
        assertEquals(esperada, orientacionPedida())
        compose.runOnUiThread { orientacion = OrientacionApp.SEGUN_MOVIL }
        compose.waitForIdle()
        assertEquals(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED, orientacionPedida())
    }

    @Test
    fun cambiar_la_orientacion_en_ajustes_conserva_la_marcha_y_la_pantalla() {
        mostrar()
        marcha = 12
        toca("icono de Ajustes")
        toca("a la orientación actual")
        en("Ajustes")
        assertTrue(orientacionPedida() != ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
        atras()
        en("Rodaje")
        compose.onNodeWithText("Marcha 12").assertExists()
        toca("icono de Ajustes")
        toca("a según el móvil")
        assertEquals(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED, orientacionPedida())
        atras()
        en("Rodaje")
        assertFalse(terminada)
    }

    @Test
    fun atras_en_una_pestana_vuelve_a_rodaje() {
        mostrar()
        toca("chip")
        toca("a Conexión")
        toca("a Ajustes")
        atras()
        en("Rodaje")
        assertFalse(terminada)
    }

    @Test
    fun atras_en_una_secundaria_vuelve_a_su_origen() {
        mostrar()
        toca("chip")
        toca("a Conexión")
        toca("a Ajustes")
        toca("a Diagnóstico")
        en("Diagnóstico")
        atras()
        en("Ajustes")
        toca("a Marchas")
        toca("volver")
        en("Ajustes")
    }

    @Test
    fun ajustes_a_sesiones_grabadas_y_vuelta() {
        // «Ajustes a Sesiones grabadas y vuelta».
        mostrar()
        toca("chip")
        toca("a Conexión")
        toca("a Ajustes")
        toca("a Sesiones")
        en("Sesiones grabadas")
        toca("volver")
        en("Ajustes")
        toca("a Sesiones")
        atras()
        en("Ajustes")
    }

    @Test
    fun atras_en_rodaje_termina_sin_pasar_por_las_pestanas() {
        // «Atrás en Rodaje».
        mostrar()
        toca("chip")
        toca("a Conexión")
        toca("a Ajustes")
        toca("a Dispositivos")
        toca("a Rodar")
        atras()
        assertTrue(terminada)
    }

    @Test
    fun pantalla_siguiente_y_anterior_con_el_square() {
        // «Desde Rodaje», «Vuelta circular», «Anterior desde Rodaje» y «Desde una secundaria».
        mostrar()
        square(siguiente = true)
        en("Conexión")
        square(siguiente = true)
        en("Dispositivos")
        square(siguiente = true)
        en("Ajustes")
        square(siguiente = true)
        en("Rodaje")
        square(siguiente = false)
        en("Ajustes")
        toca("a Marchas")
        square(siguiente = false)
        en("Dispositivos")
        // Y Atrás desde ahí vuelve a Rodaje: la secundaria se cerró.
        atras()
        en("Rodaje")
        assertEquals(false, terminada)
    }
}
