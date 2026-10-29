package app.smartbike.ui.tabla

import app.smartbike.dominio.Configuracion
import app.smartbike.dominio.Desarrollos
import app.smartbike.dominio.EstadoMarcha
import app.smartbike.dominio.Transmision
import java.util.Locale
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/** Spec `settings-screen`, «Pantalla Tabla de marchas». */
class TablaUiStateTest {
    private val es = Locale.forLanguageTag("es")
    private val en = Locale.forLanguageTag("en")

    private val lineal = Transmision.Lineal()

    @Test
    fun marchas_de_la_lineal() {
        val e = estadoDeTabla(Configuracion(), EstadoMarcha.Numerada(17, 24))
        assertEquals(24, e.filas.size)
        assertEquals(-11.0, e.filas.first().ajustePct, 1e-9)
        assertEquals(12.0, e.filas.last().ajustePct, 1e-9)
        assertEquals(16, e.indicePuesta)
        assertEquals(1, e.filas.count { it.puesta })
    }

    @Test
    fun marchas_de_la_shimano_por_relacion() {
        val c = Configuracion(transmision = Desarrollos.SHIMANO_105.id)
        val e = estadoDeTabla(c, EstadoMarcha.PlatoPinon(36, 17))
        assertEquals(24, e.filas.size)
        assertEquals("36×36", nombreMarcha(e.filas.first().marcha))
        assertEquals("52×11", nombreMarcha(e.filas.last().marcha))
        assertTrue(e.filas.zipWithNext().all { (a, b) -> a.ajustePct <= b.ajustePct })
        assertEquals("36×17", nombreMarcha(e.filas[e.indicePuesta].marcha))
    }

    @Test
    fun pasos_de_una_decima_y_limites() {
        assertEquals(5.1, ajusteMas(5.0, 1), 1e-9)
        assertEquals(-0.4, ajusteMas(-0.39, 0), 1e-9)
        assertEquals(20.0, ajusteMas(20.0, 1), 1e-9)
        assertEquals(-20.0, ajusteMas(-20.0, -1), 1e-9)
        assertFalse(FilaTabla(EstadoMarcha.Numerada(1, 24), 20.0, false).puedeSubir)
        assertTrue(FilaTabla(EstadoMarcha.Numerada(1, 24), 20.0, false).puedeBajar)
    }

    @Test
    fun editar_una_marcha_solo_cambia_esa_y_restablecer_la_devuelve() {
        val c = Configuracion()
        val editada = ajustarFila(c.tabla(lineal), 16, 1)
        val e = estadoDeTabla(c.conTabla(lineal, editada), null)
        assertEquals(5.1, e.filas[16].ajustePct, 1e-9)
        assertEquals(c.tabla(lineal)[15], e.filas[15].ajustePct, 1e-9)
        assertEquals(5.0, estadoDeTabla(c.conTabla(lineal, editada).sinTabla(lineal), null).filas[16].ajustePct, 1e-9)
    }

    @Test
    fun ajuste_con_signo() {
        assertEquals("+3,2", ajusteTexto(3.2, es))
        assertEquals("−1,5", ajusteTexto(-1.5, es))
        assertEquals("0,0", ajusteTexto(0.0, es))
        assertEquals("+3.2", ajusteTexto(3.2, en))
        assertEquals("−1.5", ajusteTexto(-1.5, en))
        assertEquals("0.0", ajusteTexto(0.0, en))
    }
}
