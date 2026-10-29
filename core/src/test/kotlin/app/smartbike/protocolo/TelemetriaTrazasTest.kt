package app.smartbike.protocolo

import app.smartbike.trazas.Traza
import kotlin.math.abs
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Contrasta la telemetría estándar del Neo real con su página FE-C 0x19 (Trainer Data), que el Neo envía por `fec2`
 * sin pedirla: potencia instantánea en los bytes 6–7 (12 bits) y cadencia instantánea en el byte 2 de la página.
 */
class TelemetriaTrazasTest {

    private class Fec19(val tMs: Long, val cadencia: Int, val potencia: Int)

    private val traza = Traza.leer("neo-erg-200w.txt").filter { it.origen == "NEO" && it.sentido == "RX" }
    private val fec19 = traza.filter { it.caracteristica == "fec2" && it.datos.size == 13 && it.datos[4].toInt() == 0x19 }.map {
        val d = it.datos
        Fec19(it.tMs, d[6].toInt() and 0xFF, (d[9].toInt() and 0xFF) or ((d[10].toInt() and 0x0F) shl 8))
    }

    /** La página 0x19 más cercana en el tiempo a [tMs]. */
    private fun cercana(tMs: Long) = fec19.minBy { abs(it.tMs - tMs) }

    @Test
    fun potencia_coincide_con_la_pagina_0x19() {
        val potencias = traza.filter { it.caracteristica == "2a63" }.map { it.tMs to TelemetriaCodec.potencia(it.datos)!! }
        assertTrue(potencias.count { it.second > 0 } > 20, "La traza debe tener pedaleo")
        // La 0x19 llega a ~4 Hz y la 0x2A63 a ~1 Hz: se compara con la más cercana, con margen para la variación.
        val dentro = potencias.filter { it.second > 0 }.count { (t, w) -> abs(w - cercana(t).potencia) <= maxOf(15, w / 8) }
        val total = potencias.count { it.second > 0 }
        assertTrue(dentro >= total * 0.9, "Potencia: $dentro de $total dentro del margen")
    }

    @Test
    fun cadencia_coincide_con_la_pagina_0x19() {
        val calculo = CalculoCadencia()
        val cadencias = traza.filter { it.caracteristica == "2a5b" }.mapNotNull { l ->
            TelemetriaCodec.biela(l.datos)?.let { l.tMs to calculo.biela(it, l.tMs) }
        }
        val pedaleando = cadencias.filter { it.second > 0 }
        assertTrue(pedaleando.size > 20, "La traza debe tener pedaleo")
        val dentro = pedaleando.count { (t, rpm) -> abs(rpm - cercana(t).cadencia) <= 8 }
        assertTrue(dentro >= pedaleando.size * 0.8, "Cadencia: $dentro de ${pedaleando.size} dentro de ±8 rpm")
    }
}
