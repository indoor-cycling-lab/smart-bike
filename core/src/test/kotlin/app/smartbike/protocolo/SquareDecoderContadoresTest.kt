package app.smartbike.protocolo

import app.smartbike.trazas.Traza
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class SquareDecoderContadoresTest {

    /** Trama de `0x45` con todos los contadores a [base] salvo el de [boton], que vale [valor]. */
    private fun trama(boton: SquareButton? = null, valor: Int = 0, base: Int = 2): ByteArray {
        val nibbles = IntArray(22) { if (it < 2) 0 else base }
        if (boton != null) nibbles[boton.contador + 2] = valor
        return ByteArray(11) { ((nibbles[it * 2] shl 4) or nibbles[it * 2 + 1]).toByte() }
    }

    @ParameterizedTest
    @EnumSource(SquareButton::class)
    fun cada_contador_en_su_posicion(boton: SquareButton) {
        val c = SquareDecoder.contadores(trama(boton, valor = 0xB))!!
        assertEquals(0xB, c[boton.ordinal])
        assertEquals(19, c.count { it == 2 })
        assertEquals(boton.mascara, SquareDecoder.mascara(c))
    }

    @Test
    fun las_veinte_posiciones_son_distintas() {
        assertEquals((0 until 20).toSet(), SquareButton.entries.map { it.contador }.toSet())
    }

    @Test
    fun contadores_pares_ninguno_pulsado() {
        assertEquals(0u, SquareDecoder.mascara(SquareDecoder.contadores(trama())!!))
    }

    @Test
    fun trama_corta() {
        assertNull(SquareDecoder.contadores(ByteArray(10)))
    }

    /**
     * Captura real con un botón cada vez y ~1 s entre botones: el Square manda 0x43 y 0x45 casi a la vez (≤ 5 ms), y
     * en cada pareja los botones pulsados según 0x45 son los de la máscara de 0x43. Aparecen los 20 botones.
     */
    @Test
    fun traza_real_de_los_veinte_botones() {
        val traza = Traza.leer("square-20-botones.txt").filter { it.origen == "SQUARE" && it.sentido == "RX" }
        var ultima43: Pair<Long, UInt>? = null
        var vistos = 0u
        var parejas = 0
        for (l in traza) {
            when (l.caracteristica) {
                "0x43" -> ultima43 = l.tMs to SquareDecoder.mascara(l.datos)!!

                "0x45" -> {
                    val m45 = SquareDecoder.mascara(SquareDecoder.contadores(l.datos)!!)
                    vistos = vistos or m45
                    val (t43, m43) = ultima43 ?: continue
                    if (l.tMs - t43 <= 5) {
                        assertEquals(m43, m45, "pareja en t=${l.tMs}")
                        parejas++
                    }
                }
            }
        }
        assertEquals(SquareButton.entries.fold(0u) { a, b -> a or b.mascara }, vistos, "los 20 botones aparecen en 0x45")
        assert(parejas > 100) { "solo $parejas parejas 0x43/0x45" }
    }
}
