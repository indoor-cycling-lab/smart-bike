package app.smartbike.ui.rodaje

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import app.smartbike.dominio.Accion
import app.smartbike.dominio.EnlaceGuia
import app.smartbike.dominio.ModoConexion
import app.smartbike.dominio.PasosGuia
import app.smartbike.ui.componentes.AccionesGuia
import app.smartbike.ui.createComposeRuleEs
import app.smartbike.ui.tema.SqTema
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** Las maquetas `guia-*` de rodaje.html y rodaje-vertical.html, y la spec `getting-started-guide`. */
class GuiaInicioTest {
    @get:Rule
    val compose = createComposeRuleEs()

    private fun pasos(rodillo: EnlaceGuia, controlador: EnlaceGuia = EnlaceGuia.NO_CONFIGURADO, plataforma: Boolean = false) = PasosGuia.de(rodillo, controlador, plataforma)

    private fun guia(
        pasos: PasosGuia = pasos(EnlaceGuia.NO_CONFIGURADO),
        pagina: Int = 1,
        abierta: Boolean = true,
        modo: ModoConexion = ModoConexion.FTMS_WIFI,
        rodillo: String? = null,
        controlador: String? = null,
    ) = GuiaUi(pagina, abierta, pasos, rodillo, controlador, modo, "Smart Bike", "OnePlus 8")

    private fun mostrar(g: GuiaUi, acciones: AccionesGuia = AccionesGuia(), ancho: Int = 360, alto: Int = 640) {
        compose.setContent {
            SqTema {
                Box(Modifier.requiredSize(ancho.dp, alto.dp)) {
                    Rodaje(
                        RodajeUiState(guia = g),
                        abrirDispositivos = acciones.abrirDispositivos,
                        abrirAjustes = acciones.abrirAjustes,
                        abrirConexion = acciones.abrirConexion,
                        guia = acciones,
                        animar = false,
                    )
                }
            }
        }
    }

    @Test
    fun pagina_1_dice_que_es_la_app_y_sus_tres_piezas() {
        mostrar(guia())
        compose.onNodeWithText("Smart Bike").assertIsDisplayed()
        compose.onNodeWithText("Conecta tu rodillo, un controlador y tu app de ciclismo indoor.").assertIsDisplayed()
        compose.onNodeWithText("Da la resistencia y mide tu potencia y tu cadencia.").assertIsDisplayed()
        compose.onNodeWithText("Sus botones cambian de marcha.").assertIsDisplayed()
        compose.onNodeWithText("Recibe tu potencia y te envía la pendiente.").assertIsDisplayed()
        compose.onAllNodesWithText("Opcional").assertCountEquals(2)
        compose.onNodeWithText("1 de 4").assertIsDisplayed()
        compose.onNodeWithText("Siguiente").assertIsDisplayed()
        assertEquals(0, compose.onAllNodesWithText("Atrás").fetchSemanticsNodes().size)
    }

    @Test
    fun siguiente_y_atras_cambian_de_pagina_con_el_indicador() {
        var pagina by mutableIntStateOf(1)
        compose.setContent {
            SqTema {
                Box(Modifier.requiredSize(360.dp, 640.dp)) {
                    Rodaje(RodajeUiState(guia = guia(pagina = pagina)), abrirDispositivos = {}, guia = AccionesGuia(irA = { pagina = it }), animar = false)
                }
            }
        }
        compose.onNodeWithText("1 de 4").assertIsDisplayed()
        compose.onNodeWithText("Siguiente").performClick()
        compose.onNodeWithText("2 de 4").assertIsDisplayed()
        compose.onNodeWithText("Siguiente").performClick()
        compose.onNodeWithText("3 de 4").assertIsDisplayed()
        compose.onNodeWithText("Siguiente").performClick()
        compose.onNodeWithText("4 de 4").assertIsDisplayed()
        compose.onNodeWithText("Atrás").performClick()
        compose.onNodeWithText("3 de 4").assertIsDisplayed()
        compose.onNodeWithText("Atrás").performClick()
        compose.onNodeWithText("2 de 4").assertIsDisplayed()
        compose.onNodeWithText("Atrás").performClick()
        compose.onNodeWithText("1 de 4").assertIsDisplayed()
    }

    @Test
    fun la_ultima_pagina_solo_tiene_atras() {
        mostrar(guia(pagina = 4))
        assertEquals(0, compose.onAllNodesWithText("Siguiente").fetchSemanticsNodes().size)
        assertEquals(0, compose.onAllNodesWithText("Abrir Conexión").fetchSemanticsNodes().size)
        compose.onNodeWithText("Atrás").assertIsDisplayed()
    }

    @Test
    fun pagina_2_en_el_primer_uso_todo_pendiente_sin_color_de_alerta() {
        mostrar(guia(pagina = 2))
        compose.onNodeWithText("Empareja tus dispositivos").assertIsDisplayed()
        compose.onNodeWithText("Configura la conexión").assertIsDisplayed()
        compose.onNodeWithText("Ajusta la app").assertIsDisplayed()
        compose.onAllNodesWithText("Pendiente").assertCountEquals(1)
        compose.onNodeWithText("Rodillo (necesario) y controlador (opcional)").assertIsDisplayed()
        compose.onNodeWithText("Cómo se conecta tu app de ciclismo indoor a Smart Bike. Ahora: Wifi + Bluetooth").assertIsDisplayed()
        compose.onNodeWithText("Opcional").assertIsDisplayed()
        compose.onNodeWithText("Tipo de marcha, botones, pantalla…").assertIsDisplayed()
    }

    @Test
    fun pagina_2_con_el_rodillo_conectado_dice_que_falta_el_controlador() {
        mostrar(guia(pasos(EnlaceGuia.CONECTADO), pagina = 2, rodillo = "Tacx Neo 1"))
        compose.onNodeWithText("Falta el controlador").assertIsDisplayed()
        compose.onNodeWithText("Tacx Neo 1 conectado. Controlador, opcional, sin emparejar").assertIsDisplayed()
    }

    @Test
    fun pagina_2_con_rodillo_y_controlador_dice_los_modelos_reales() {
        mostrar(guia(pasos(EnlaceGuia.CONECTADO, EnlaceGuia.CONECTADO), pagina = 2, rodillo = "Tacx Neo 1", controlador = "Elite Square"))
        compose.onNodeWithText("Hecho").assertIsDisplayed()
        compose.onNodeWithText("Tacx Neo 1 y Elite Square conectados").assertIsDisplayed()
    }

    @Test
    fun el_lector_de_pantalla_lee_el_paso_con_su_estado() {
        mostrar(guia(pasos(EnlaceGuia.CONECTADO, EnlaceGuia.CONECTADO), pagina = 2, rodillo = "Tacx Neo 1", controlador = "Elite Square"))
        compose.onNodeWithContentDescription("Empareja tus dispositivos, hecho").assertIsDisplayed()
        compose.onNodeWithContentDescription("Configura la conexión, opcional. Modo Wifi + Bluetooth").assertIsDisplayed()
    }

    @Test
    fun el_rodillo_conectando_se_lee_conectando() {
        mostrar(guia(pasos(EnlaceGuia.CONECTANDO), pagina = 2, rodillo = "Tacx Neo 1"))
        compose.onNodeWithContentDescription("Empareja tus dispositivos, conectando").assertIsDisplayed()
    }

    @Test
    fun cada_paso_lleva_a_su_pantalla() {
        var destino = ""
        val acciones = AccionesGuia(
            abrirDispositivos = { destino = "dispositivos" },
            abrirConexion = { destino = "conexion" },
            abrirAjustes = { destino = "ajustes" },
        )
        mostrar(guia(pagina = 2), acciones)
        compose.onNodeWithText("Empareja tus dispositivos").performClick()
        assertEquals("dispositivos", destino)
        compose.onNodeWithText("Configura la conexión").performClick()
        assertEquals("conexion", destino)
        compose.onNodeWithText("Ajusta la app").performClick()
        assertEquals("ajustes", destino)
    }

    @Test
    fun la_pagina_3_dice_las_formas_de_rodar_con_los_nombres_del_indicador() {
        mostrar(guia(pagina = 3))
        compose.onNodeWithText("Cómo puedes usar la app").assertIsDisplayed()
        // Los nombres también salen en el indicador de Rodaje, que está detrás de la hoja: basta con que existan.
        listOf("LIBRE", "SIM", "ERG").forEach { assert(compose.onAllNodesWithText(it, substring = false).fetchSemanticsNodes().isNotEmpty()) }
        compose.onNodeWithText("Sin app de ciclismo indoor. Terreno llano: la dureza la da tu marcha.").assertIsDisplayed()
        compose.onNodeWithText("Con una app como Zwift, que envía la pendiente de la ruta. Tus marchas se suman a ella.").assertIsDisplayed()
        compose.onNodeWithText("Potencia fija, sea cual sea tu cadencia. En ERG MANUAL la fijas tú; en ERG APP la manda tu entrenamiento.").assertIsDisplayed()
        compose.onNodeWithText("SIM y ERG APP necesitan tu app conectada (página siguiente); LIBRE y ERG MANUAL, no.").assertIsDisplayed()
        compose.onNodeWithText("Siguiente").assertIsDisplayed()
    }

    @Test
    fun la_pagina_4_dice_el_modo_y_sus_pasos() {
        mostrar(guia(pagina = 4, modo = ModoConexion.FTMS_BLUETOOTH))
        compose.onNodeWithText("Opcional").assertIsDisplayed()
        compose.onNodeWithText("Según tu modo de conexión: Solo Bluetooth.").assertIsDisplayed()
        compose.onNodeWithText("Activa el Bluetooth del equipo donde corre la app. No hace falta wifi.").assertIsDisplayed()
        compose.onNodeWithText("En la app, elige el móvil (OnePlus 8, su nombre Bluetooth) como rodillo, potencia y cadencia.").assertIsDisplayed()
    }

    @Test
    fun la_pagina_4_dice_el_estado_de_la_conexion_con_la_app() {
        mostrar(guia(pasos(EnlaceGuia.NO_CONFIGURADO), pagina = 4))
        compose.onNodeWithText("Antes, empareja el rodillo").assertIsDisplayed()
    }

    @Test
    fun la_pagina_4_con_el_rodillo_dice_esperando_a_tu_app() {
        mostrar(guia(pasos(EnlaceGuia.CONECTADO, EnlaceGuia.CONECTADO), pagina = 4))
        compose.onNodeWithText("Esperando a tu app").assertIsDisplayed()
    }

    @Test
    fun la_pagina_4_con_la_app_conectada_lo_dice() {
        mostrar(guia(pasos(EnlaceGuia.CONECTADO, EnlaceGuia.CONECTADO, plataforma = true), pagina = 4))
        compose.onNodeWithText("App conectada").assertIsDisplayed()
    }

    @Test
    fun la_pagina_4_en_wifi_mas_bluetooth_y_en_solo_wifi() {
        mostrar(guia(pagina = 4, modo = ModoConexion.FTMS_WIFI))
        compose.onNodeWithText("En la app, elige «Smart Bike» como potencia, cadencia y rodillo, por wifi.").assertIsDisplayed()
    }

    @Test
    fun descartar_y_minimizar_con_objetivos_de_48_dp() {
        var minimizada = 0
        var cerrada = 0
        var noMostrar = 0
        mostrar(guia(), AccionesGuia(minimizar = { minimizada++ }, cerrar = { cerrada++ }, noMostrar = { noMostrar++ }))
        compose.onNodeWithContentDescription("Minimizar la guía").assertWidthIsAtLeast(48.dp).assertHeightIsAtLeast(48.dp).performClick()
        compose.onNodeWithContentDescription("Cerrar la guía").assertWidthIsAtLeast(48.dp).assertHeightIsAtLeast(48.dp).performClick()
        compose.onNodeWithText("No volver a mostrar").assertHeightIsAtLeast(48.dp).performClick()
        assertEquals(listOf(1, 1, 1), listOf(minimizada, cerrada, noMostrar))
    }

    @Test
    fun la_tira_minimizada_dice_lo_siguiente_por_hacer_y_reabre_la_hoja() {
        var abierta = 0
        mostrar(guia(abierta = false), AccionesGuia(abrir = { abierta++ }))
        compose.onNodeWithText("Para empezar").assertIsDisplayed()
        compose.onNodeWithText("Empareja tus dispositivos").assertIsDisplayed()
        compose.onNodeWithContentDescription("Para empezar. Empareja tus dispositivos").assertHeightIsAtLeast(48.dp).performClick()
        assertEquals(1, abierta)
        // Sin el texto de conexión: la tira ocupa su sitio (spec `ride-screen`, «Minimizada»).
        assertEquals(0, compose.onAllNodesWithText("Toca las conexiones para conectar el Neo y el Square").fetchSemanticsNodes().size)
    }

    @Test
    fun con_rodillo_y_controlador_la_tira_dice_conecta_tu_app() {
        mostrar(guia(pasos(EnlaceGuia.CONECTADO, EnlaceGuia.CONECTADO), abierta = false))
        compose.onNodeWithText("Conecta tu app de ciclismo indoor").assertIsDisplayed()
    }

    @Test
    fun con_la_hoja_abierta_la_tira_no_se_lee() {
        mostrar(guia(abierta = true))
        compose.onAllNodesWithContentDescription("Para empezar. Empareja tus dispositivos").assertCountEquals(0)
    }

    @Test
    fun sin_guia_el_pie_sigue_diciendo_lo_de_siempre() {
        compose.setContent { SqTema { Rodaje(RodajeUiState(), abrirDispositivos = {}, animar = false, modifier = Modifier.requiredSize(360.dp, 640.dp)) } }
        compose.onNodeWithText("Toca las conexiones para conectar el Neo y el Square").assertIsDisplayed()
    }

    @Test
    fun en_horizontal_la_hoja_se_ve_entera() {
        mostrar(guia(pagina = 2), ancho = 640, alto = 360)
        compose.onNodeWithText("Empareja tus dispositivos").assertIsDisplayed()
        compose.onNodeWithText("Ajusta la app").assertIsDisplayed()
        compose.onNodeWithText("No volver a mostrar").assertIsDisplayed()
        compose.onNodeWithText("Atrás").assertIsDisplayed()
    }

    @Test
    fun la_hoja_tiene_el_nombre_de_la_pagina() {
        mostrar(guia(pagina = 2))
        compose.onNode(SemanticsMatcher.expectValue(SemanticsProperties.PaneTitle, "Guía de inicio, página 2 de 4")).assertExists()
    }

    @Test
    fun abrir_y_minimizar_la_hoja_no_mueve_la_marcha() {
        var abierta by mutableStateOf(true)
        compose.setContent {
            SqTema {
                Box(Modifier.requiredSize(360.dp, 640.dp)) {
                    Rodaje(RodajeUiState(guia = guia(abierta = abierta)), abrirDispositivos = {}, animar = false)
                }
            }
        }
        val marcha = compose.onNodeWithContentDescription("Marcha 12 de 24")
        val conHoja = marcha.getUnclippedBoundsInRoot()
        compose.runOnIdle { abierta = false }
        compose.waitForIdle()
        assertEquals(conHoja, marcha.getUnclippedBoundsInRoot())
    }

    @Test
    fun el_aviso_de_accion_sustituye_a_la_tira_mientras_dura() {
        var aviso by mutableStateOf<AvisoUi?>(AvisoUi.Marcha(Accion.MarchaMas(1), 1))
        compose.setContent {
            SqTema {
                Box(Modifier.requiredSize(360.dp, 640.dp)) {
                    Rodaje(RodajeUiState(guia = guia(abierta = false), aviso = aviso), abrirDispositivos = {}, animar = false)
                }
            }
        }
        assertEquals(0, compose.onAllNodesWithContentDescription("Para empezar. Empareja tus dispositivos").fetchSemanticsNodes().size)
        compose.runOnIdle { aviso = null }
        compose.onNodeWithContentDescription("Para empezar. Empareja tus dispositivos").assertIsDisplayed()
    }

    @Test
    fun con_los_botones_la_tira_no_los_tapa() {
        compose.setContent {
            SqTema {
                Box(Modifier.requiredSize(360.dp, 720.dp)) {
                    Rodaje(RodajeUiState(botones = true, guia = guia(abierta = false)), abrirDispositivos = {}, animar = false)
                }
            }
        }
        val tira = compose.onNodeWithContentDescription("Para empezar. Empareja tus dispositivos")
        val mas = compose.onNodeWithContentDescription("Una marcha más dura")
        mas.assertIsDisplayed().assertHeightIsAtLeast(72.dp)
        tira.assertIsDisplayed()
        assertTrue(tira.getUnclippedBoundsInRoot().bottom <= mas.getUnclippedBoundsInRoot().top)
    }
}
