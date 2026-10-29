package app.smartbike.dominio

import app.smartbike.dominio.ComandoRodillo.Pendiente
import app.smartbike.dominio.ComandoRodillo.Potencia
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/** Escenarios de la spec `resistance-models`: «Modelo por tabla», «Modelo físico» y «Velocidad y distancia calculadas». */
class ModelosTablaFisicoTest {
    private val llano = Terreno(0.0)
    private fun m(p: Int) = EstadoMarcha.Numerada(p, 24)

    private fun tablaDe(t: Transmision) = ModeloSimple(1.0, 12, t).tablaPorDefecto()

    @Test
    fun tabla_sin_editar_igual_al_simple_en_la_lineal() {
        val simple = ModeloSimple(1.0, 12)
        val tabla = ModeloTabla(tablaDe(Transmision.Lineal()))
        (1..24).forEach { assertEquals(simple.comando(llano, 0.0, m(it)), tabla.comando(llano, 0.0, m(it)), "marcha $it") }
    }

    @Test
    fun tabla_sin_editar_igual_al_simple_en_la_shimano() {
        val t = Desarrollos.SHIMANO_105
        val simple = ModeloSimple(1.0, 12, t)
        val tabla = ModeloTabla(tablaDe(t), t)
        t.ordenadas.forEach { assertEquals(simple.comando(llano, 0.0, it), tabla.comando(llano, 0.0, it), "$it") }
    }

    @Test
    fun una_marcha_editada_solo_cambia_esa() {
        val base = tablaDe(Transmision.Lineal())
        val editada = base.toMutableList().also { it[16] = 7.0 }
        val tabla = ModeloTabla(editada)
        assertEquals(Pendiente(7.0), tabla.comando(llano, 0.0, m(17)))
        assertEquals(Pendiente(4.0), tabla.comando(llano, 0.0, m(16)))
    }

    @Test
    fun con_zwift_suma_el_ajuste_de_la_marcha() {
        val tabla = ModeloTabla(List(24) { 2.0 })
        assertEquals(Pendiente(5.0), tabla.comando(Terreno(3.0), 0.0, m(5)))
    }

    private val fisico = ModeloFisico(75.0, 8.0)
    private fun w(terreno: Terreno, rpm: Double, marcha: EstadoMarcha) = fisico.potenciaW(terreno, rpm, marcha)

    @Test
    fun marcha_mas_dura_sube_la_potencia_en_las_24() {
        val p = (1..24).map { w(llano, 90.0, m(it)) }
        p.zipWithNext().forEach { (a, b) -> assertTrue(b > a, "$a < $b") }
    }

    @Test
    fun cadencia_y_pendiente_suben_la_potencia() {
        assertTrue(w(llano, 90.0, m(12)) > w(llano, 70.0, m(12)))
        assertTrue(w(Terreno(6.0), 90.0, m(12)) > w(llano, 90.0, m(12)))
    }

    @Test
    fun pesos_pesan_en_subida_y_poco_en_llano() {
        val pesado = ModeloFisico(75.0, 12.0)
        val subida = Terreno(6.0)
        assertTrue(pesado.potenciaW(subida, 90.0, m(12)) - fisico.potenciaW(subida, 90.0, m(12)) > 10.0)
        assertTrue(pesado.potenciaW(llano, 90.0, m(12)) - fisico.potenciaW(llano, 90.0, m(12)) < 5.0)
    }

    @Test
    fun bajada_pronunciada_no_da_potencia_negativa() {
        assertEquals(0.0, w(Terreno(-8.0), 60.0, m(1)), 0.0)
    }

    @Test
    fun velocidad_con_relacion_2_a_90_rpm() {
        val s = EstadoMarcha.PlatoPinon(36, 18) // relación 2,0
        assertEquals(22.6, fisico.velocidadMs(90.0, s) * 3.6, 0.05)
    }

    @Test
    fun la_lineal_reparte_entre_1_y_la_relacion_de_la_shimano() {
        assertEquals(1.0, fisico.relacion(m(1)), 1e-9)
        assertEquals(52.0 / 11.0, fisico.relacion(m(24)), 1e-9)
    }

    @Test
    fun comando_es_una_potencia_redondeada() {
        val c = fisico.comando(llano, 90.0, m(12)) as Potencia
        assertEquals(w(llano, 90.0, m(12)).let { Math.round(it).toInt() }, c.vatios)
    }
}
