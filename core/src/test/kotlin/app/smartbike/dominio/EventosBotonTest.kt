package app.smartbike.dominio

import app.smartbike.dominio.EventoBoton.Pulsado
import app.smartbike.dominio.EventoBoton.Soltado
import app.smartbike.protocolo.SquareButton
import app.smartbike.protocolo.SquareButton.CampagnoloDerecho
import app.smartbike.protocolo.SquareButton.FrenoIzquierdo
import app.smartbike.protocolo.SquareButton.PalancaDerecha1
import app.smartbike.protocolo.SquareButton.PalancaDerecha2
import app.smartbike.protocolo.SquareButton.PalancaIzquierda1
import app.smartbike.protocolo.SquareButton.PalancaIzquierda2
import app.smartbike.protocolo.SquareDecoder
import app.smartbike.trazas.Traza
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class EventosBotonTest {

    private val eventos = EventosBoton()

    /** Contadores a 2 (todo suelto) salvo los indicados. */
    private fun contadores(vararg valores: Pair<SquareButton, Int>) = IntArray(SquareButton.entries.size) { 2 }.also { c -> valores.forEach { (b, v) -> c[b.ordinal] = v } }

    private fun base() = eventos.procesar(contadores(), 0)

    @Test
    fun la_linea_base_no_da_eventos() {
        assertEquals(emptyList<EventoBoton>(), base())
    }

    @ParameterizedTest
    @EnumSource(SquareButton::class)
    fun cada_boton_por_separado(boton: SquareButton) {
        base()
        assertEquals(listOf(Pulsado(boton, 10, boton.mascara)), eventos.procesar(contadores(boton to 3), 10))
        assertEquals(listOf(Soltado(boton, 20)), eventos.procesar(contadores(boton to 4), 20))
    }

    @Test
    fun notificacion_repetida_no_produce_eventos() {
        base()
        eventos.procesar(contadores(PalancaDerecha1 to 3), 10)
        assertEquals(emptyList<EventoBoton>(), eventos.procesar(contadores(PalancaDerecha1 to 3), 30))
        assertEquals(PalancaDerecha1.mascara, eventos.mascara)
    }

    @Test
    fun pulsacion_completa_entre_dos_notificaciones() {
        base()
        assertEquals(
            listOf(Pulsado(PalancaDerecha1, 10, PalancaDerecha1.mascara), Soltado(PalancaDerecha1, 10)),
            eventos.procesar(contadores(PalancaDerecha1 to 4), 10),
        )
        assertEquals(0u, eventos.mascara)
    }

    @Test
    fun el_contador_da_la_vuelta() {
        eventos.procesar(contadores(PalancaDerecha1 to 15), 0) // 15: impar, pulsada al conectar
        assertEquals(listOf(Soltado(PalancaDerecha1, 10)), eventos.procesar(contadores(PalancaDerecha1 to 0), 10))
        assertEquals(listOf(Pulsado(PalancaDerecha1, 20, PalancaDerecha1.mascara)), eventos.procesar(contadores(PalancaDerecha1 to 1), 20))
    }

    @Test
    fun boton_pulsado_al_conectar() {
        assertEquals(
            listOf(Pulsado(FrenoIzquierdo, 0, FrenoIzquierdo.mascara)),
            eventos.procesar(contadores(FrenoIzquierdo to 5), 0),
        )
        assertEquals(listOf(Soltado(FrenoIzquierdo, 10)), eventos.procesar(contadores(FrenoIzquierdo to 6), 10))
    }

    @Test
    fun modificador_y_palanca() {
        base()
        val ambos = CampagnoloDerecho.mascara or PalancaDerecha1.mascara
        val secuencia = listOf(
            contadores(CampagnoloDerecho to 3) to 100L,
            contadores(CampagnoloDerecho to 3, PalancaDerecha1 to 3) to 200L,
            contadores(CampagnoloDerecho to 3, PalancaDerecha1 to 4) to 300L,
            contadores(CampagnoloDerecho to 4, PalancaDerecha1 to 4) to 400L,
        ).flatMap { (c, t) -> eventos.procesar(c, t) }

        assertEquals(
            listOf(
                Pulsado(CampagnoloDerecho, 100, CampagnoloDerecho.mascara),
                Pulsado(PalancaDerecha1, 200, ambos),
                Soltado(PalancaDerecha1, 300),
                Soltado(CampagnoloDerecho, 400),
            ),
            secuencia,
        )
    }

    @Test
    fun soltar_y_pulsar_en_la_misma_notificacion_suelta_primero() {
        base()
        eventos.procesar(contadores(PalancaDerecha1 to 3), 0)
        assertEquals(
            listOf(Soltado(PalancaDerecha1, 50), Pulsado(FrenoIzquierdo, 50, FrenoIzquierdo.mascara)),
            eventos.procesar(contadores(PalancaDerecha1 to 4, FrenoIzquierdo to 3), 50),
        )
    }

    @Test
    fun perdida_suelta_los_pulsados_y_olvida_la_linea_base() {
        base()
        eventos.procesar(contadores(FrenoIzquierdo to 3, CampagnoloDerecho to 3), 0)
        assertEquals(
            listOf(Soltado(FrenoIzquierdo, 500), Soltado(CampagnoloDerecho, 500)),
            eventos.perdida(500),
        )
        assertEquals(0u, eventos.mascara)
        // Tras reconectar, los contadores pueden ser cualesquiera: la primera notificación vuelve a ser línea base.
        assertEquals(emptyList<EventoBoton>(), eventos.procesar(contadores(FrenoIzquierdo to 8), 600))
    }

    /** Captura real: diez pulsaciones lo más rápidas posible en cada palanca; 0x43 solo informaba de 4 o 5. */
    @Test
    fun traza_real_de_pulsaciones_rapidas() {
        val pulsaciones = Traza.leer("square-pulsaciones-rapidas.txt")
            .filter { it.origen == "SQUARE" && it.caracteristica == "0x45" }
            .flatMap { l -> eventos.procesar(SquareDecoder.contadores(l.datos)!!, l.tMs) }
            .filterIsInstance<Pulsado>()
            .groupingBy { it.boton }.eachCount()

        assertEquals(
            mapOf(PalancaDerecha1 to 10, PalancaIzquierda1 to 10, PalancaDerecha2 to 10, PalancaIzquierda2 to 10),
            pulsaciones,
        )
    }
}
