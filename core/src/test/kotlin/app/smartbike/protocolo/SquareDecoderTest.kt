package app.smartbike.protocolo

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class SquareDecoderTest {

    /** Trama de 8 bytes con la máscara en los bytes 3–6 y relleno distinto de cero alrededor. */
    private fun trama(mascara: UInt) = byteArrayOf(
        0x0A,
        0x1B,
        0x2C,
        (mascara shr 24).toByte(),
        (mascara shr 16).toByte(),
        (mascara shr 8).toByte(),
        mascara.toByte(),
        0x7F,
    )

    @ParameterizedTest
    @EnumSource(SquareButton::class)
    fun cada_boton_tiene_su_bit(boton: SquareButton) {
        val mascara = SquareDecoder.mascara(trama(boton.mascara))
        assertEquals(boton.mascara, mascara)
        assertEquals(listOf(boton), SquareButton.pulsados(mascara!!))
    }

    @Test
    fun los_veinte_bits_son_distintos() {
        val todos = SquareButton.entries.fold(0u) { acc, b -> acc or b.mascara }
        assertEquals(20, todos.countOneBits())
    }

    @Test
    fun varios_a_la_vez() {
        val mascara = SquareDecoder.mascara(byteArrayOf(0, 0, 0, 0x80.toByte(), 0x02, 0x00, 0x00))
        assertEquals(0x8002_0000u, mascara)
        assertEquals(
            listOf(SquareButton.PalancaDerecha1, SquareButton.CampagnoloDerecho),
            SquareButton.pulsados(mascara!!),
        )
    }

    @Test
    fun ninguno_pulsado() {
        assertEquals(0u, SquareDecoder.mascara(trama(0u)))
        assertEquals(emptyList<SquareButton>(), SquareButton.pulsados(0u))
    }

    @Test
    fun trama_corta() {
        assertNull(SquareDecoder.mascara(ByteArray(6)))
        assertNull(SquareDecoder.mascara(ByteArray(0)))
    }

    @Test
    fun trama_de_siete_bytes_justos() {
        assertEquals(0x0000_0001u, SquareDecoder.mascara(byteArrayOf(0, 0, 0, 0, 0, 0, 1)))
    }

    @Test
    fun bits_sin_boton_se_ignoran() {
        assertEquals(listOf(SquareButton.A), SquareButton.pulsados(0x0100_0004u))
    }
}
