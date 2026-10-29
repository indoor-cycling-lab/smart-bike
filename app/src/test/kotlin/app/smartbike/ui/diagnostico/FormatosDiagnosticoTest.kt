package app.smartbike.ui.diagnostico

import app.smartbike.dominio.ComandoRodillo
import java.util.Locale
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/** Spec `app-language`, «Formatos según el idioma»: los números de Diagnóstico siguen al idioma de la app. */
class FormatosDiagnosticoTest {
    private val es = Locale.forLanguageTag("es")
    private val en = Locale.forLanguageTag("en")

    @Test
    fun decimal_con_coma_o_punto_y_signo_menos_tipografico() {
        assertEquals("3,0", decimal(3.0, es))
        assertEquals("3.0", decimal(3.0, en))
        assertEquals("−3,5", decimal(-3.5, es))
        assertEquals("−3.5", decimal(-3.5, en))
        assertEquals("0.012", decimal(0.012, en, 3))
    }

    @Test
    fun con_signo_y_comandos() {
        assertEquals("+5,0", conSigno(5.0, es))
        assertEquals("+5.0", conSigno(5.0, en))
        assertEquals("−11.0", conSigno(-11.0, en))
        assertEquals("−3.5", textoComando(ComandoRodillo.Pendiente(-3.5), en))
        assertEquals("120", textoComando(ComandoRodillo.Potencia(120), en))
    }
}
