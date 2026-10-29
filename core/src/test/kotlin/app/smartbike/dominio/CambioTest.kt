package app.smartbike.dominio

import app.smartbike.dominio.ResultadoCambio.Cambiada
import app.smartbike.dominio.ResultadoCambio.ReferenciaFijada
import app.smartbike.dominio.ResultadoCambio.SinCambio
import app.smartbike.dominio.ResultadoCambio.Tope
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class CambioTest {

    @Nested
    inner class Lineal {
        private val cambio = Cambio(Transmision.Lineal(24))
        private fun m(p: Int) = cambio.marcha(p)
        private fun aplicar(desde: Int, a: Accion, referencia: Int = 12) = cambio.aplicar(m(desde), a, m(referencia))
        private fun cambiada(de: Int, a: Int) = Cambiada(m(de), m(a), a - de)

        @Test
        fun subir_y_bajar_una() {
            assertEquals(cambiada(14, 15), aplicar(14, Accion.MarchaMas(1)))
            assertEquals(cambiada(14, 13), aplicar(14, Accion.MarchaMenos(1)))
        }

        @Test
        fun saltos_y_extremos() {
            assertEquals(cambiada(14, 17), aplicar(14, Accion.MarchaMas(3)))
            assertEquals(cambiada(14, 11), aplicar(14, Accion.MarchaMenos(3)))
            assertEquals(cambiada(14, 24), aplicar(14, Accion.MarchaMasDura))
            assertEquals(cambiada(14, 1), aplicar(14, Accion.MarchaMasLigera))
        }

        @Test
        fun referencia() {
            assertEquals(cambiada(20, 12), aplicar(20, Accion.MarchaReferencia))
            assertEquals(SinCambio, aplicar(12, Accion.MarchaReferencia))
            assertEquals(cambiada(20, 15), aplicar(20, Accion.MarchaReferencia, referencia = 15))
        }

        @Test
        fun fijar_la_referencia_no_cambia_la_marcha() {
            assertEquals(ReferenciaFijada(m(15)), aplicar(15, Accion.FijarReferencia))
            assertEquals(ReferenciaFijada(m(24)), aplicar(24, Accion.FijarReferencia))
        }

        @Test
        fun topes() {
            assertEquals(Tope(TopeMarcha.MasDura), aplicar(24, Accion.MarchaMas(1)))
            assertEquals(Tope(TopeMarcha.MasDura), aplicar(24, Accion.MarchaMas(3)))
            assertEquals(Tope(TopeMarcha.MasDura), aplicar(24, Accion.MarchaMasDura))
            assertEquals(Tope(TopeMarcha.MasLigera), aplicar(1, Accion.MarchaMenos(1)))
            assertEquals(Tope(TopeMarcha.MasLigera), aplicar(1, Accion.MarchaMasLigera))
        }

        @Test
        fun salto_recortado_por_el_extremo_no_es_tope() {
            val r = aplicar(23, Accion.MarchaMas(3))
            assertEquals(cambiada(23, 24), r)
            assertEquals(1, (r as Cambiada).pasos)
        }

        @Test
        fun desde_un_extremo_se_puede_ir_al_otro_sentido() {
            assertEquals(cambiada(24, 23), aplicar(24, Accion.MarchaMenos(1)))
            assertEquals(cambiada(1, 2), aplicar(1, Accion.MarchaMas(1)))
        }

        @Test
        fun el_plato_no_hace_nada() {
            assertEquals(SinCambio, aplicar(14, Accion.PlatoMas))
            assertEquals(SinCambio, aplicar(14, Accion.PlatoMenos))
        }
    }

    /** Escenarios de la spec `virtual-gears` con la Shimano 105 52/36 · 11–36. */
    @Nested
    inner class Shimano {
        private val t = Desarrollos.SHIMANO_105
        private val automatico = Cambio(t, ModoCambio.AUTOMATICO)
        private val manual = Cambio(t, ModoCambio.MANUAL)
        private fun m(plato: Int, pinon: Int) = EstadoMarcha.PlatoPinon(plato, pinon)
        private val ref = m(36, 17)

        /** La marcha a la que lleva la acción, o falla si no cambia. */
        private fun Cambio.a(desde: EstadoMarcha.PlatoPinon, accion: Accion) = (aplicar(desde, accion, ref) as Cambiada).nueva as EstadoMarcha.PlatoPinon

        @Test
        fun subir_de_plato() {
            assertEquals(m(52, 19), automatico.a(m(36, 15), Accion.MarchaMas(1)))
        }

        @Test
        fun antes_del_punto_de_subida() {
            assertEquals(m(36, 15), automatico.a(m(36, 17), Accion.MarchaMas(1)))
        }

        @Test
        fun bajar_de_plato() {
            assertEquals(m(36, 19), automatico.a(m(52, 24), Accion.MarchaMenos(1)))
        }

        @Test
        fun histeresis() {
            val una = automatico.a(m(52, 19), Accion.MarchaMenos(1))
            assertEquals(m(52, 21), una)
            assertEquals(m(52, 24), automatico.a(una, Accion.MarchaMenos(1)))
        }

        private fun recorrido(cambio: Cambio, desde: EstadoMarcha.PlatoPinon, accion: Accion): List<EstadoMarcha.PlatoPinon> {
            val marchas = mutableListOf(desde)
            while (true) {
                val r = cambio.aplicar(marchas.last(), accion, ref)
                if (r !is Cambiada) return marchas
                marchas += r.nueva as EstadoMarcha.PlatoPinon
            }
        }

        @Test
        fun recorrido_completo_subiendo() {
            val subida = recorrido(automatico, t.masLigera, Accion.MarchaMas(1))
            assertEquals(
                listOf(
                    m(36, 36), m(36, 32), m(36, 28), m(36, 24), m(36, 21), m(36, 19), m(36, 17), m(36, 15),
                    m(52, 19), m(52, 17), m(52, 15), m(52, 14), m(52, 13), m(52, 12), m(52, 11),
                ),
                subida,
            )
            // Cada marcha es más dura que la anterior.
            subida.zipWithNext().forEach { (a, b) -> assertTrue(b.relacion > a.relacion, "$a → $b") }
        }

        @Test
        fun recorrido_completo_bajando() {
            val bajada = recorrido(automatico, t.masDura, Accion.MarchaMenos(1))
            assertEquals(
                listOf(
                    m(52, 11), m(52, 12), m(52, 13), m(52, 14), m(52, 15), m(52, 17), m(52, 19), m(52, 21), m(52, 24),
                    m(36, 19), m(36, 21), m(36, 24), m(36, 28), m(36, 32), m(36, 36),
                ),
                bajada,
            )
            bajada.zipWithNext().forEach { (a, b) -> assertTrue(b.relacion < a.relacion, "$a → $b") }
        }

        @Test
        fun desde_una_marcha_cruzada() {
            assertEquals(m(36, 21), automatico.a(m(52, 28), Accion.MarchaMenos(1)))
            assertEquals(m(52, 15), automatico.a(m(36, 13), Accion.MarchaMas(1)))
        }

        @Test
        fun plato_a_mano_en_automatico() {
            assertEquals(m(52, 21), automatico.a(m(36, 17), Accion.PlatoMas))
            assertEquals(m(36, 14), automatico.a(m(52, 17), Accion.PlatoMenos))
            // Compensación recortada al cassette.
            assertEquals(m(52, 36), automatico.a(m(36, 32), Accion.PlatoMas))
        }

        @Test
        fun manual() {
            assertEquals(m(36, 14), manual.a(m(36, 15), Accion.MarchaMas(1)))
            assertEquals(m(52, 17), manual.a(m(36, 17), Accion.PlatoMas))
            assertEquals(m(52, 32), manual.a(m(52, 28), Accion.MarchaMenos(1)))
        }

        @Test
        fun saltos_y_extremos() {
            assertEquals(m(36, 15), manual.a(m(36, 21), Accion.MarchaMas(3)))
            assertEquals(m(52, 17), automatico.a(m(36, 17), Accion.MarchaMas(3)))
            for (c in listOf(manual, automatico)) {
                assertEquals(m(52, 11), c.a(m(36, 24), Accion.MarchaMasDura))
                assertEquals(m(36, 36), c.a(m(52, 13), Accion.MarchaMasLigera))
            }
        }

        @Test
        fun salto_recortado_en_manual_no_es_tope() {
            val r = manual.aplicar(m(52, 12), Accion.MarchaMas(3), ref)
            assertEquals(Cambiada(m(52, 12), m(52, 11), 1), r)
        }

        @Test
        fun topes() {
            assertEquals(Tope(TopeMarcha.MasDura), automatico.aplicar(m(52, 11), Accion.MarchaMas(1), ref))
            assertEquals(Tope(TopeMarcha.MasLigera), automatico.aplicar(m(36, 36), Accion.MarchaMenos(1), ref))
            assertEquals(Tope(TopeMarcha.Pinon(11)), manual.aplicar(m(36, 11), Accion.MarchaMas(1), ref))
            assertEquals(Tope(TopeMarcha.Pinon(36)), manual.aplicar(m(52, 36), Accion.MarchaMenos(1), ref))
            assertEquals(Tope(TopeMarcha.MasDura), manual.aplicar(m(52, 11), Accion.MarchaMas(3), ref))
            for (c in listOf(manual, automatico)) {
                assertEquals(Tope(TopeMarcha.Plato(52)), c.aplicar(m(52, 17), Accion.PlatoMas, ref))
                assertEquals(Tope(TopeMarcha.Plato(36)), c.aplicar(m(36, 17), Accion.PlatoMenos, ref))
                assertEquals(Tope(TopeMarcha.MasDura), c.aplicar(m(52, 11), Accion.MarchaMasDura, ref))
            }
        }

        @Test
        fun referencia() {
            assertEquals(m(36, 17), automatico.a(m(52, 13), Accion.MarchaReferencia))
            assertEquals(SinCambio, automatico.aplicar(m(36, 17), Accion.MarchaReferencia, ref))
            assertEquals(ReferenciaFijada(m(52, 19)), automatico.aplicar(m(52, 19), Accion.FijarReferencia, ref))
        }

        @Test
        fun cambiar_de_modo_no_cambia_la_marcha() {
            // La marcha la guarda quien llama; el modo nuevo solo cuenta en la acción siguiente.
            assertEquals(m(36, 12), manual.a(m(36, 13), Accion.MarchaMas(1)))
            assertEquals(m(52, 15), automatico.a(m(36, 13), Accion.MarchaMas(1)))
        }

        @Test
        fun saltos_de_tres_en_la_zona_de_histeresis() {
            // Subir 3 y bajar 3 desde la 36×15 vuelve por la histéresis: cambia de plato una vez en cada sentido.
            val arriba = automatico.a(m(36, 15), Accion.MarchaMas(3))
            assertEquals(m(52, 15), arriba)
            val abajo = automatico.a(arriba, Accion.MarchaMenos(3))
            assertEquals(m(52, 21), abajo)
            assertEquals(m(36, 21), automatico.a(abajo, Accion.MarchaMenos(3)))
        }

        @Test
        fun pasos_y_cambio_de_plato() {
            val pinon = automatico.aplicar(m(52, 17), Accion.MarchaMenos(1), ref) as Cambiada
            assertEquals(-1, pinon.pasos)
            assertEquals(false, pinon.cambiaPlato)
            val plato = automatico.aplicar(m(36, 15), Accion.MarchaMas(1), ref) as Cambiada
            assertTrue(plato.cambiaPlato)
            assertTrue(plato.pasos > 0)
        }
    }
}
