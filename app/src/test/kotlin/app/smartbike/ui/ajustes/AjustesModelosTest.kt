package app.smartbike.ui.ajustes

import app.smartbike.dominio.Configuracion
import app.smartbike.dominio.TipoModelo
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/** Spec `settings-screen`: modelo, dificultad, potencia mínima y peso de la bici. */
class AjustesModelosTest {
    @Test
    fun valores_por_defecto() {
        val e = estadoDeAjustes(Configuracion())
        assertEquals(TipoModelo.SIMPLE, e.modelo)
        assertEquals(100, e.dificultadPct)
        assertEquals(30, e.potenciaMinimaW)
        assertEquals(8.0, e.pesoBiciKg, 0.0)
    }

    @Test
    fun que_se_muestra_con_cada_modelo() {
        val simple = estadoDeAjustes(Configuracion())
        assertTrue(simple.muestraBajadas && !simple.muestraTabla && !simple.muestraPotenciaMinima)
        val tabla = estadoDeAjustes(Configuracion(modelo = TipoModelo.TABLA))
        assertTrue(tabla.muestraBajadas && tabla.muestraTabla && !tabla.muestraPotenciaMinima)
        val fisico = estadoDeAjustes(Configuracion(modelo = TipoModelo.FISICO))
        assertTrue(!fisico.muestraBajadas && !fisico.muestraTabla && fisico.muestraPotenciaMinima)
    }

    @Test
    fun dificultad_de_5_en_5_y_en_sus_limites() {
        assertEquals(105, dificultadMas(100, 1))
        assertEquals(150, dificultadMas(150, 1))
        assertEquals(50, dificultadMas(50, -1))
        assertFalse(AjustesUiState(dificultadPct = 150).puedeSubirDificultad)
        assertTrue(AjustesUiState(dificultadPct = 150).puedeBajarDificultad)
    }

    @Test
    fun potencia_minima_de_5_en_5_y_en_sus_limites() {
        assertEquals(35, potenciaMinimaMas(30, 1))
        assertEquals(0, potenciaMinimaMas(0, -1))
        assertEquals(150, potenciaMinimaMas(150, 1))
        assertFalse(AjustesUiState(potenciaMinimaW = 0).puedeBajarMinima)
    }

    @Test
    fun peso_de_la_bici_de_medio_kilo_y_en_sus_limites() {
        assertEquals(8.5, pesoBiciMas(8.0, 1), 0.0)
        assertEquals(5.0, pesoBiciMas(5.0, -1), 0.0)
        assertEquals(25.0, pesoBiciMas(25.0, 1), 0.0)
        assertFalse(AjustesUiState(pesoBiciKg = 5.0).puedeBajarPesoBici)
        assertTrue(AjustesUiState(pesoBiciKg = 5.0).puedeSubirPesoBici)
    }
}
