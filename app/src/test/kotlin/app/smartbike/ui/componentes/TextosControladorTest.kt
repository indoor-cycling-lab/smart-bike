package app.smartbike.ui.componentes

import java.io.File
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Los nombres del controlador (spec `device-connection`, `platform-settings` y `ride-screen`) viven en `strings.xml`:
 * el Square es «Controlador» en Dispositivos y en la barra de Rodaje, y lo que la app publica es «Controlador Zwift».
 */
class TextosControladorTest {

    private val textos: Map<String, String> by lazy {
        val xml = File("src/main/res/values/strings.xml").readText()
        Regex("""<string name="([a-z0-9_]+)">(.*?)</string>""", RegexOption.DOT_MATCHES_ALL).findAll(xml)
            .associate { it.groupValues[1] to it.groupValues[2] }
    }

    private fun texto(clave: String) = checkNotNull(textos[clave]) { "Falta la cadena $clave" }

    @Test
    fun el_square_es_controlador_en_dispositivos_y_en_la_barra() {
        assertEquals("Controlador", texto("servicio_controlador"))
        assertEquals("controlador", texto("tipo_controlador"))
        assertEquals("Controlador", texto("enlace_controlador_square"))
    }

    @Test
    fun el_servicio_publicado_es_controlador_zwift() {
        assertEquals("Controlador Zwift", texto("servicio_controlador_zwift"))
    }

    @Test
    fun rodaje_dice_controlador_donde_decia_mando() {
        assertEquals("Conectado · sin controlador", texto("publicado_conectado_sin_mando"))
        assertEquals("Conectado · sin controlador ni pulso", texto("publicado_conectado_sin_mando_ni_pulso"))
        assertEquals("controlador, %1\$s", texto("publicado_descripcion_mando"))
        assertEquals("controlador", texto("enlace_controlador"))
    }

    @Test
    fun ya_no_quedan_las_cadenas_con_el_nombre_antiguo() {
        listOf("servicio_mando", "tipo_mando", "enlace_mando").forEach { assertFalse(it in textos, "sigue $it") }
        assertTrue("controlador" in texto("dispositivos_intro"))
    }
}
