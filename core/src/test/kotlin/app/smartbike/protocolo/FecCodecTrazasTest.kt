package app.smartbike.protocolo

import app.smartbike.trazas.Traza
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/** Trazas reales del Tacx Neo 1: el checksum y las tramas que se le enviaron. */
class FecCodecTrazasTest {

    private val trazas = listOf("neo-pendiente-xor.txt", "neo-erg-200w.txt").flatMap { Traza.leer(it) }

    @Test
    fun las_paginas_fec_del_neo_llevan_checksum_xor() {
        val paginas = trazas.filter { it.origen == "NEO" && it.sentido == "RX" && it.caracteristica == "fec2" }
        assertTrue(paginas.size > 50) { "solo ${paginas.size} páginas FE-C" }
        for (p in paginas) {
            val xor = p.datos.dropLast(1).fold(0) { acc, b -> acc xor (b.toInt() and 0xFF) }
            assertEquals(xor, p.datos.last().toInt() and 0xFF, "página en t=${p.tMs}")
        }
    }

    @Test
    fun lo_enviado_al_neo_es_lo_que_codifica_fec_codec() {
        val enviadas = trazas.filter { it.sentido == "TX" && it.caracteristica == "fec3" }.map { it.datos.toList() }.toSet()
        val esperadas = setOf(FecCodec.pendiente(0.0), FecCodec.pendiente(10.0), FecCodec.potencia(200)).map { it.toList() }.toSet()
        assertEquals(esperadas, enviadas)
    }
}
