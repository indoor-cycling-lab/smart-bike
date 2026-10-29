package app.smartbike.dominio

import app.smartbike.dominio.ComandoRodillo.Pendiente
import app.smartbike.dominio.ComandoRodillo.Potencia
import kotlin.math.abs
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/** Escenarios de la spec `resistance-models` sobre `ControlRodillo`: dificultad, protecciones y cambio de modelo. */
class ControlRodilloModelosTest {
    private val simple = ModeloSimple(1.0, 12)
    private val fisico = ModeloFisico(75.0, 8.0)
    private fun m(p: Int) = EstadoMarcha.Numerada(p, 24)

    private fun conectado(modelo: ModeloResistencia, marcha: Int = 12, dificultad: Int = 100, minima: Int = 30) = ControlRodillo(modelo, Terreno(0.0), ModoBajadas.LA_MARCHA_NO_CREA_BAJADAS, dificultad, minima)
        .also { it.neoListo(m(marcha)) }

    /** Alimenta la cadencia cada 250 ms durante [ms] y devuelve el último comando enviado. */
    private fun ControlRodillo.pedalear(rpm: Double, marcha: EstadoMarcha, desde: Long, ms: Long): Long {
        var t = desde
        while (t < desde + ms) {
            t += 250
            alCambiarCadencia(rpm, marcha, t)
        }
        return t
    }

    // Dificultad

    @Test
    fun dificultad_150_con_el_simple() {
        assertEquals(Pendiente(7.5), conectado(simple, 17, 150).ultimoEnviado)
    }

    @Test
    fun dificultad_100_no_escala() {
        assertEquals(Pendiente(5.0), conectado(simple, 17).ultimoEnviado)
    }

    @Test
    fun dificultad_en_caliente_envia_la_pendiente_nueva() {
        val c = conectado(simple, 17)
        assertEquals(Pendiente(5.5), c.cambiarDificultad(110, m(17)))
    }

    @Test
    fun dificultad_sin_efecto_en_erg() {
        val c = conectado(simple, 12, 150)
        c.potenciaPlataforma(230)
        assertNull(c.cambiarDificultad(50, m(12)))
        assertEquals(Potencia(230), c.ultimoEnviado)
    }

    @Test
    fun dificultad_50_con_el_fisico_da_la_mitad_con_minima_0() {
        val a = conectado(fisico, 12, 100, minima = 0)
        a.pedalear(90.0, m(12), 0, 4000)
        val b = conectado(fisico, 12, 50, minima = 0)
        b.pedalear(90.0, m(12), 0, 4000)
        assertEquals((a.ultimoEnviado as Potencia).vatios / 2.0, (b.ultimoEnviado as Potencia).vatios.toDouble(), 2.0)
    }

    // Protecciones del físico

    @Test
    fun bajada_con_cadencia_normal_da_la_potencia_minima() {
        val c = ControlRodillo(fisico, Terreno(-5.0), potenciaMinimaW = 30).also { it.neoListo(m(1)) }
        c.pedalear(90.0, m(1), 0, 6000)
        assertEquals(Potencia(30), c.ultimoEnviado)
    }

    @Test
    fun cadencia_baja_alivia_hasta_la_minima_a_cero() {
        val c = conectado(fisico, 18)
        val t = c.pedalear(80.0, m(18), 0, 5000)
        val normal = (c.ultimoEnviado as Potencia).vatios
        c.pedalear(20.0, m(18), t, 3000)
        val baja = (c.ultimoEnviado as Potencia).vatios
        assertTrue(baja < normal)
        c.pedalear(0.0, m(18), t + 3000, 15000)
        assertEquals(Potencia(30), c.ultimoEnviado)
    }

    @Test
    fun cambio_gradual_con_la_cadencia() {
        val c = conectado(fisico, 18)
        val t = c.pedalear(60.0, m(18), 0, 5000)
        val antes = (c.ultimoEnviado as Potencia).vatios
        c.pedalear(100.0, m(18), t, 500)
        val medio = (c.ultimoEnviado as Potencia).vatios
        val tope = fisico.potenciaW(Terreno(0.0), 100.0, m(18))
        assertTrue(medio > antes && medio < tope - 20, "$antes -> $medio, tope $tope")
        c.pedalear(100.0, m(18), t + 500, 8000)
        assertEquals(tope, (c.ultimoEnviado as Potencia).vatios.toDouble(), 5.0)
    }

    @Test
    fun cambio_de_marcha_inmediato_sin_rampa() {
        val c = conectado(fisico, 12)
        val t = c.pedalear(90.0, m(12), 0, 6000)
        val esperado = (fisico.potenciaW(Terreno(0.0), 90.0, m(18))).let { Math.round(it).toInt() }
        val enviado = c.alCambiarMarcha(m(18), t) as Potencia
        assertEquals(esperado.toDouble(), enviado.vatios.toDouble(), 3.0)
    }

    @Test
    fun sin_reenvios_por_menos_de_2_w_ni_mas_de_4_hz() {
        val c = conectado(fisico, 12)
        var t = c.pedalear(90.0, m(12), 0, 8000)
        var envios = 0
        repeat(40) { i ->
            t += 50
            if (c.alCambiarCadencia(90.0 + (i % 2) * 0.05, m(12), t) != null) envios++
        }
        assertEquals(0, envios)
    }

    @Test
    fun pendiente_con_zigzag_no_oscila() {
        val c = conectado(fisico, 12)
        var t = 0L
        c.terrenoPlataforma(Terreno(4.0), m(12), t)
        t = c.pedalear(90.0, m(12), t, 10000)
        val enviados = mutableListOf<Int>()
        repeat(40) { i ->
            t += 250
            if (i % 8 == 0) c.terrenoPlataforma(Terreno(4.0 + if ((i / 8) % 2 == 0) 0.05 else -0.05), m(12), t)
            (c.alCambiarCadencia(90.0, m(12), t) as? Potencia)?.let { enviados += it.vatios }
        }
        assertTrue(enviados.isEmpty() || enviados.max() - enviados.min() <= 2, "$enviados")
    }

    @Test
    fun el_fisico_no_envia_sin_neo_y_envia_al_conectar() {
        val c = ControlRodillo(fisico, Terreno(0.0))
        assertNull(c.alCambiarCadencia(90.0, m(12), 250))
        assertTrue(c.neoListo(m(12)) is Potencia)
    }

    // Cambio de modelo

    @Test
    fun cambiar_de_modelo_rodando() {
        val c = conectado(simple, 17)
        assertNull(c.cambiarModelo(ModeloTabla(ModeloSimple(1.0, 12).tablaPorDefecto()), m(17)))
        val r = c.cambiarModelo(fisico, m(17))
        assertTrue(r is Potencia)
        assertTrue(c.cambiarModelo(simple, m(17)) is Pendiente)
    }

    @Test
    fun en_erg_el_cambio_de_modelo_no_envia() {
        val c = conectado(simple, 12)
        c.alternarErgManual(200, m(12))
        assertNull(c.cambiarModelo(fisico, m(12)))
        assertEquals(Potencia(200), c.ultimoEnviado)
        assertNull(c.alCambiarCadencia(90.0, m(12), 250))
    }

    @Test
    fun el_fisico_en_cadencia_cero_pide_la_minima() {
        val c = conectado(fisico, 12)
        assertEquals(Potencia(30), c.ultimoEnviado)
        assertTrue(abs(c.cadenciaSuaveRpm) < 1e-9)
    }
}
