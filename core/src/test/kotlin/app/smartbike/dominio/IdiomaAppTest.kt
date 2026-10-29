package app.smartbike.dominio

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/** El idioma de la interfaz (spec `app-language`). */
class IdiomaAppTest {
    @Test
    fun el_movil_en_espanol_de_cualquier_region_da_espanol() {
        assertEquals(IdiomaApp.ES, IdiomaApp.deLenguaje("es"))
        assertEquals(IdiomaApp.ES, IdiomaApp.deLenguaje("es-MX"))
        assertEquals(IdiomaApp.ES, IdiomaApp.deLenguaje("es_ES"))
        assertEquals(IdiomaApp.ES, IdiomaApp.deLenguaje("ES"))
    }

    @Test
    fun cualquier_otro_idioma_da_ingles() {
        assertEquals(IdiomaApp.EN, IdiomaApp.deLenguaje("en"))
        assertEquals(IdiomaApp.EN, IdiomaApp.deLenguaje("fr"))
        assertEquals(IdiomaApp.EN, IdiomaApp.deLenguaje("ca"))
        assertEquals(IdiomaApp.EN, IdiomaApp.deLenguaje(""))
    }

    @Test
    fun ida_y_vuelta() {
        val json = Configuracion.aJson(Configuracion(idioma = IdiomaApp.EN))
        assertTrue("\"idioma\":\"en\"" in json)
        assertEquals(IdiomaApp.EN, Configuracion.deJson(json).idioma)
    }

    @Test
    fun documento_anterior_sin_idioma_se_lee_sin_decidir() {
        assertNull(Configuracion.deJson("""{"version":11,"pesoCiclistaKg":70}""").idioma)
        assertNull(Configuracion().idioma)
    }

    @Test
    fun un_idioma_que_no_se_entiende_se_importa_como_no_valido() {
        val r = Configuracion.validar("""{"version":12,"idioma":"klingon"}""")
        assertEquals(MotivoImportacion.NO_ES_CONFIGURACION, (r as ResultadoImportacion.Invalida).motivo)
    }

    @Test
    fun archivo_con_idioma_se_importa_con_el() {
        val c = (Configuracion.validar("""{"version":12,"idioma":"en"}""") as ResultadoImportacion.Valida).configuracion
        assertEquals(IdiomaApp.EN, c.idioma)
    }

    @Test
    fun un_documento_con_idioma_se_lee_en_una_version_que_no_lo_conoce() {
        // ignoreUnknownKeys: lo mismo que haría la versión anterior con la clave nueva.
        val sinConocerlo = Configuracion.deJson("""{"version":12,"idioma":"en","pesoCiclistaKg":70,"claveFutura":1}""")
        assertEquals(70, sinConocerlo.pesoCiclistaKg)
    }
}
