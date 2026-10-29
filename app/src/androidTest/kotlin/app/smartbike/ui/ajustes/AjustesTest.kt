package app.smartbike.ui.ajustes

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.dp
import app.smartbike.datos.EstadoGrabacion
import app.smartbike.datos.ResumenSesiones
import app.smartbike.dominio.Configuracion
import app.smartbike.dominio.DatoRodaje
import app.smartbike.dominio.Desarrollos
import app.smartbike.dominio.IdiomaApp
import app.smartbike.dominio.ModoBajadas
import app.smartbike.dominio.ModoCambio
import app.smartbike.dominio.MotivoImportacion
import app.smartbike.dominio.OrientacionApp
import app.smartbike.dominio.TemaApp
import app.smartbike.dominio.TipoModelo
import app.smartbike.ui.createComposeRuleEs
import app.smartbike.ui.navegacion.Pestana
import app.smartbike.ui.tema.SqTema
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** maquetas/ajustes.html y escenarios de la spec `settings-screen`. */
class AjustesTest {
    @get:Rule
    val compose = createComposeRuleEs()

    private fun mostrar(estado: AjustesUiState = AjustesUiState(), acciones: AccionesAjustes = AccionesAjustes()) {
        compose.setContent { SqTema { Ajustes(estado, acciones) } }
    }

    private fun texto(t: String) = compose.onNodeWithText(t).performScrollTo()

    @Test
    fun como_pestana_con_los_valores_por_defecto() {
        // Spec `settings-screen`, «Abrir Ajustes» y «Valores por defecto».
        mostrar()
        assertEquals(0, compose.onAllNodesWithContentDescription("Volver a Diagnóstico").fetchSemanticsNodes().size)
        texto("Marchas")
        texto("Diagnóstico")
        texto("Potencia y cadencia")
        texto("Botones − y + en pantalla").assertIsOff()
        texto("Cambiar de modo tocando").assertIsOff()
        texto("Toca el indicador de modo para pasar de Libre o SIM a ERG manual y volver. No cambia el modo en un entrenamiento de la app")
        texto("Mostrar la acción").assertIsOn()
        texto("Vibrar").assertIsOn()
        compose.onNodeWithText("75 kg", substring = true).performScrollTo()
        texto("Rodillo")
        texto("La marcha no crea bajadas").assertIsSelected()
        texto("Sin protección").assertIsNotSelected()
    }

    @Test
    fun modelo_de_resistencia_con_sus_ajustes() {
        // Spec `settings-screen`, «Modelo de resistencia», «Dificultad» y «Peso de la bici».
        var elegido: TipoModelo? = null
        mostrar(acciones = AccionesAjustes(modelo = { elegido = it }))
        texto("MODELO DE RESISTENCIA")
        texto("Simple").assertIsSelected()
        texto("Por tabla").assertIsNotSelected()
        texto("Físico").assertIsNotSelected()
        texto("Dificultad")
        compose.onNodeWithText("100 %", substring = true).performScrollTo()
        texto("Peso de la bici")
        compose.onNodeWithText("8,0 kg", substring = true).performScrollTo()
        // Sin el físico, no hay potencia mínima ni tabla.
        assertEquals(0, compose.onAllNodesWithText("Potencia mínima").fetchSemanticsNodes().size)
        assertEquals(0, compose.onAllNodesWithText("Tabla de marchas").fetchSemanticsNodes().size)
        texto("Calcula la potencia con tu cadencia, la marcha y los pesos").performClick()
        assertEquals(TipoModelo.FISICO, elegido)
    }

    @Test
    fun con_el_fisico_potencia_minima_y_sin_bajadas() {
        mostrar(AjustesUiState(modelo = TipoModelo.FISICO))
        texto("Potencia mínima")
        compose.onNodeWithText("30 W", substring = true).performScrollTo()
        assertEquals(0, compose.onAllNodesWithText("La marcha no crea bajadas").fetchSemanticsNodes().size)
    }

    @Test
    fun por_tabla_abre_la_tabla_de_marchas() {
        var abierta = false
        mostrar(AjustesUiState(modelo = TipoModelo.TABLA), AccionesAjustes(abrirTabla = { abierta = true }))
        texto("Tabla de marchas").performClick()
        assertTrue(abierta)
        texto("La marcha no crea bajadas").assertIsSelected()
    }

    @Test
    fun dificultad_en_su_limite() {
        mostrar(AjustesUiState(dificultadPct = 150))
        compose.onNodeWithContentDescription("Subir dificultad").performScrollTo().assertIsNotEnabled()
        compose.onNodeWithContentDescription("Bajar dificultad").performScrollTo().assertIsEnabled()
    }

    @Test
    fun seccion_pantalla_con_el_tema() {
        // Spec `settings-screen`, «Ajustes como pestaña» (orden) y «Tema en Ajustes».
        var elegido: TemaApp? = null
        mostrar(acciones = AccionesAjustes(tema = { elegido = it }))
        // Posiciones en la columna desplazable, sin desplazarla: todas se miden en el mismo sistema.
        val y = { t: String -> compose.onNodeWithText(t).fetchSemanticsNode().positionInRoot.y }
        val alRodar = y("Vibrar")
        val pantalla = y("Pantalla")
        texto("TEMA")
        texto("Oscuro").assertIsSelected()
        texto("Claro").assertIsNotSelected()
        val ciclista = y("Ciclista")
        assertTrue(alRodar < pantalla && pantalla < ciclista)
        texto("Para leer con luz de día").performClick()
        assertEquals(TemaApp.CLARO, elegido)
    }

    @Test
    fun idioma_encima_del_tema() {
        // Spec `settings-screen`, «Idioma en Ajustes».
        var elegido: IdiomaApp? = null
        mostrar(AjustesUiState(idioma = IdiomaApp.ES), AccionesAjustes(idioma = { elegido = it }))
        val y = { t: String -> compose.onNodeWithText(t).fetchSemanticsNode().positionInRoot.y }
        val idioma = y("IDIOMA · LANGUAGE")
        val tema = y("TEMA")
        assertTrue(idioma < tema)
        texto("Español").assertIsSelected()
        texto("English").assertIsNotSelected()
        texto("English").performClick()
        assertEquals(IdiomaApp.EN, elegido)
    }

    @Test
    fun orientacion_debajo_del_tema() {
        // Spec `settings-screen`, «Orientación en Ajustes».
        var elegida: OrientacionApp? = null
        mostrar(acciones = AccionesAjustes(orientacion = { elegida = it }))
        val y = { t: String -> compose.onNodeWithText(t).fetchSemanticsNode().positionInRoot.y }
        val tema = y("TEMA")
        val orientacion = y("ORIENTACIÓN")
        val ciclista = y("Ciclista")
        assertTrue(tema < orientacion && orientacion < ciclista)
        texto("Según el móvil").assertIsSelected()
        texto("Vertical").assertIsNotSelected()
        texto("Horizontal").assertIsNotSelected()
        texto("Siempre en horizontal, aunque gires el móvil").performClick()
        assertEquals(OrientacionApp.HORIZONTAL, elegida)
    }

    @Test
    fun horizontal_elegida() {
        mostrar(AjustesUiState(orientacion = OrientacionApp.HORIZONTAL))
        texto("Horizontal").assertIsSelected()
        texto("Según el móvil").assertIsNotSelected()
    }

    @Test
    fun sin_seccion_zwift_y_con_la_entrada_de_botones() {
        // Spec `settings-screen`, «Ajustes como pestaña»: Botones entre Marchas y Diagnóstico, y lo de Zwift está en Plataformas.
        var abierta = 0
        mostrar(acciones = AccionesAjustes(abrirBotones = { abierta++ }))
        val y = { t: String -> compose.onNodeWithText(t).fetchSemanticsNode().positionInRoot.y }
        assertTrue(y("Marchas") < y("Botones") && y("Botones") < y("Diagnóstico"))
        texto("Lineal · asignación, gestos y tiempos")
        compose.onNodeWithText("Zwift").assertDoesNotExist()
        compose.onNodeWithText("Mando de Zwift").assertDoesNotExist()
        compose.onNodeWithText("Simular un KICKR BIKE").assertDoesNotExist()
        compose.onNodeWithText("Botones").performClick()
        assertEquals(1, abierta)
    }

    @Test
    fun la_entrada_de_botones_dice_de_que_transmision_es() {
        mostrar(AjustesUiState(transmision = Desarrollos.SHIMANO_105))
        texto("Shimano 105 · asignación, gestos y tiempos")
    }

    @Test
    fun claro_elegido() {
        mostrar(AjustesUiState(tema = TemaApp.CLARO))
        texto("Claro").assertIsSelected()
        texto("Oscuro").assertIsNotSelected()
    }

    @Test
    fun la_fila_entera_elige_la_opcion() {
        var elegido: ModoBajadas? = null
        mostrar(acciones = AccionesAjustes(bajadas = { elegido = it }))
        // Tocar la ayuda también elige la opción (spec `settings-screen`, «Objetivo táctil»).
        texto("El Neo no empuja nunca, ni en las bajadas").performClick()
        assertEquals(ModoBajadas.NUNCA_EMPUJAR, elegido)
    }

    @Test
    fun entradas_de_marchas_diagnostico_y_datos() {
        // Spec `settings-screen`, «Entrada de Marchas» y «Abrir Diagnóstico».
        val abiertas = mutableListOf<String>()
        val estado = AjustesUiState(transmision = Desarrollos.SHIMANO_105, modoCambio = ModoCambio.AUTOMATICO)
        mostrar(
            estado,
            AccionesAjustes(
                abrirMarchas = { abiertas += "marchas" },
                abrirDiagnostico = { abiertas += "diagnostico" },
                abrirDatos = { abiertas += "datos" },
            ),
        )
        texto("Shimano 105 · automático")
        texto("Marchas").performClick()
        texto("Diagnóstico").performClick()
        texto("Datos en pantalla").performClick()
        assertEquals(listOf("marchas", "diagnostico", "datos"), abiertas)
    }

    @Test
    fun casillas_de_al_rodar() {
        // Spec `settings-screen`, «Activar los botones»: la fila entera cambia la casilla.
        val cambios = mutableListOf<Pair<String, Boolean>>()
        mostrar(
            acciones = AccionesAjustes(
                botonesEnPantalla = { cambios += "botones" to it },
                modoTactil = { cambios += "modo" to it },
                mostrarAccion = { cambios += "accion" to it },
                vibracion = { cambios += "vibrar" to it },
            ),
        )
        texto("Para cambiar de marcha tocando el móvil. Solo manda con el Square conectado: sin él, los botones aparecen siempre").performClick()
        texto("Cambiar de modo tocando").performClick()
        texto("Mostrar la acción").performClick()
        texto("Vibrar").performClick()
        assertEquals(listOf("botones" to true, "modo" to true, "accion" to false, "vibrar" to false), cambios)
    }

    @Test
    fun resumen_de_muchos_datos() {
        // Spec `settings-screen`, «Resumen de muchos datos».
        mostrar(AjustesUiState(datos = listOf(DatoRodaje.TIEMPO, DatoRodaje.POTENCIA, DatoRodaje.W_KG, DatoRodaje.POTENCIA_MEDIA)))
        texto("Tiempo, potencia, W/kg y potencia media")
    }

    @Test
    fun extremos_del_peso_y_sin_pendiente_sin_zwift() {
        // Spec `settings-screen`, «Extremo» del peso; «Pendiente sin Zwift» ya no existe (change `pestana-conexion`).
        val pasos = mutableListOf<String>()
        mostrar(AjustesUiState(pesoCiclistaKg = 150), AccionesAjustes(peso = { pasos += "peso $it" }))
        compose.onNodeWithContentDescription("Subir peso del ciclista").performScrollTo().assertIsNotEnabled().performClick()
        compose.onNodeWithContentDescription("Bajar peso del ciclista").assertIsEnabled().performClick()
        assertEquals(listOf("peso -1"), pasos)
        compose.onNodeWithText("Pendiente sin Zwift").assertDoesNotExist()
    }

    @Test
    fun barra_inferior_en_pantalla_pequena() {
        // Spec `settings-screen`, «Pantalla pequeña»: el contenido se desplaza y la barra sigue al pie.
        val pestanas = mutableListOf<Pestana>()
        compose.setContent {
            SqTema { Box(Modifier.requiredSize(360.dp, 640.dp)) { Ajustes(AjustesUiState(), AccionesAjustes(irA = { pestanas += it })) } }
        }
        compose.onNodeWithText("Rodar").assertIsDisplayed()
        texto("Copia de seguridad")
        compose.onNodeWithText("Dispositivos").assertIsDisplayed().performClick()
        assertEquals(listOf(Pestana.Dispositivos), pestanas)
    }

    // Spec `settings-backup` y `settings-screen`, «Copia de seguridad al final».

    @Test
    fun copia_de_seguridad_va_despues_de_rodillo_con_objetivos_de_48_dp() {
        mostrar()
        // Con «Importar configuración» a la vista (el final de la pantalla), «Rodillo» queda por encima de «Copia de seguridad».
        compose.onNodeWithText("Importar configuración").performScrollTo()
        val rodillo = compose.onNodeWithText("Rodillo").fetchSemanticsNode().positionInRoot.y
        val copia = compose.onNodeWithText("Copia de seguridad").fetchSemanticsNode().positionInRoot.y
        assertTrue("Rodillo en $rodillo, Copia de seguridad en $copia", copia > rodillo)
        val minimo = 48 * androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().targetContext.resources.displayMetrics.density
        for (fila in listOf("Exportar configuración", "Importar configuración")) {
            val alto = compose.onNodeWithText(fila).performScrollTo().fetchSemanticsNode().boundsInRoot.height
            assertTrue("$fila mide $alto px", alto >= minimo - 1)
        }
    }

    @Test
    fun exportar_e_importar_llaman_a_sus_acciones() {
        var exportar = 0
        var importar = 0
        mostrar(acciones = AccionesAjustes(exportar = { exportar++ }, importar = { importar++ }))
        texto("Exportar configuración").performClick()
        texto("Importar configuración").performClick()
        assertEquals(1, exportar)
        assertEquals(1, importar)
    }

    @Test
    fun sin_copia_no_hay_mensajes() {
        mostrar()
        texto("Copia de seguridad")
        for (t in listOf("Configuración exportada", "Configuración importada", "Importar", "Cancelar")) {
            assertEquals(t, 0, compose.onAllNodesWithText(t).fetchSemanticsNodes().size)
        }
    }

    @Test
    fun exportada_e_importada_se_dicen() {
        mostrar(AjustesUiState(copia = Copia.Exportada))
        texto("Configuración exportada").assertIsDisplayed()
    }

    @Test
    fun confirmar_la_importacion() {
        var confirmar = 0
        var cancelar = 0
        mostrar(
            AjustesUiState(copia = Copia.Pendiente(Configuracion())),
            AccionesAjustes(confirmarImportacion = { confirmar++ }, cancelarImportacion = { cancelar++ }),
        )
        texto("Se reemplazarán todos los ajustes por los del archivo. Los dispositivos no cambian.").assertIsDisplayed()
        texto("Importar").performClick()
        texto("Cancelar").performClick()
        assertEquals(1, confirmar)
        assertEquals(1, cancelar)
    }

    @Test
    fun cada_error_dice_su_motivo() {
        val casos = mapOf(
            MotivoImportacion.VERSION_MAS_NUEVA to "El archivo es de una versión más nueva de Smart Bike. Actualiza la app para importarlo.",
            MotivoImportacion.NO_ES_JSON to "El archivo no es una configuración de Smart Bike. Elige un archivo exportado desde esta app.",
            MotivoImportacion.NO_ES_CONFIGURACION to "El archivo no es una configuración de Smart Bike. Elige un archivo exportado desde esta app.",
            MotivoImportacion.VACIO to "No se pudo leer el archivo. Elige otro archivo.",
            MotivoImportacion.NO_SE_PUDO_LEER to "No se pudo leer el archivo. Elige otro archivo.",
        )
        val estado = androidx.compose.runtime.mutableStateOf(AjustesUiState())
        compose.setContent { SqTema { Ajustes(estado.value, AccionesAjustes()) } }
        for ((motivo, texto) in casos) {
            compose.runOnIdle { estado.value = AjustesUiState(copia = Copia.ErrorImportar(motivo)) }
            texto(texto).assertIsDisplayed()
        }
        compose.runOnIdle { estado.value = AjustesUiState(copia = Copia.ErrorExportar) }
        texto("No se pudo guardar el archivo. Prueba en otra ubicación.").assertIsDisplayed()
        compose.runOnIdle { estado.value = AjustesUiState(copia = Copia.ErrorSelector) }
        texto("No se pudo abrir el selector de archivos.").assertIsDisplayed()
    }

    @Test
    fun depuracion_al_final_con_los_valores_por_defecto() {
        // Spec `settings-screen`, «Valores por defecto»: grabar apagado, mínimo 10 % y ninguna sesión.
        mostrar()
        texto("Depuración")
        texto("Grabar sesiones")
        compose.onNodeWithText("Grabar sesiones").assertIsDisplayed()
        texto("Espacio libre mínimo")
        // `performScrollTo` deja la etiqueta justo en el borde inferior: el valor, centrado en una fila alta, queda fuera.
        texto("10 %").assertIsDisplayed()
        texto("Sesiones grabadas")
        compose.onNodeWithText("No hay ninguna").assertIsDisplayed()
        compose.onAllNodesWithText("Grabando la sesión actual.").assertCountEquals(0)
    }

    @Test
    fun depuracion_grabando_con_el_resumen_de_las_sesiones() {
        mostrar(AjustesUiState(grabarSesiones = true, grabacion = EstadoGrabacion.Grabando("a.txt"), sesiones = ResumenSesiones(3, 41_000)))
        texto("Grabando la sesión actual.")
        texto("3 sesiones · 41 KB")
    }

    @Test
    fun depuracion_detenida_por_poco_espacio_dice_como_reanudar() {
        mostrar(AjustesUiState(grabarSesiones = true, grabacion = EstadoGrabacion.DetenidaPorEspacio("a.txt")))
        compose.onNodeWithText("Detenida por poco espacio", substring = true).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun depuracion_acciones_y_limites_del_minimo() {
        var abierta = false
        var activada: Boolean? = null
        var pasos = 0
        mostrar(
            AjustesUiState(espacioLibreMinimoPct = 50),
            AccionesAjustes(abrirSesiones = { abierta = true }, grabarSesiones = { activada = it }, espacioLibreMinimo = { pasos += it }),
        )
        texto("Grabar sesiones").performClick()
        assertEquals(true, activada)
        compose.onNodeWithContentDescription("Subir espacio libre mínimo").performScrollTo().assertIsNotEnabled()
        compose.onNodeWithContentDescription("Bajar espacio libre mínimo").performScrollTo().performClick()
        assertEquals(-1, pasos)
        texto("Sesiones grabadas").performClick()
        assertTrue(abierta)
    }

    @Test
    fun la_entrada_guia_de_inicio_la_reabre_con_su_ayuda() {
        // Spec `settings-screen`, «Entrada "Guía de inicio" en Ajustes».
        var abierta = 0
        mostrar(acciones = AccionesAjustes(abrirGuia = { abierta++ }))
        compose.onNodeWithText("Guía de inicio").assertIsDisplayed()
        compose.onNodeWithText("Qué conectar para empezar a rodar").assertIsDisplayed()
        compose.onNodeWithText("Guía de inicio").performClick()
        assertEquals(1, abierta)
    }
}
