package app.smartbike.protocolo

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

/** Spec `heart-rate`, «Pulso válido»; design.md del change `pulso-zwift`, decisión 2. */
class PulsoCodecTest {
    private fun bytes(vararg v: Int) = ByteArray(v.size) { v[it].toByte() }

    @Test
    fun formato_de_8_bits() = assertEquals(132, PulsoCodec.decodificar(bytes(0x00, 132)))

    @Test
    fun formato_de_16_bits() = assertEquals(132, PulsoCodec.decodificar(bytes(0x01, 132, 0x00)))

    @Test
    fun formato_de_16_bits_con_byte_alto() = assertEquals(300, PulsoCodec.decodificar(bytes(0x01, 0x2C, 0x01)))

    @Test
    fun un_paquete_vacio_o_corto_no_vale() {
        assertNull(PulsoCodec.decodificar(bytes()))
        assertNull(PulsoCodec.decodificar(bytes(0x00)))
        assertNull(PulsoCodec.decodificar(bytes(0x01, 132)))
    }

    @Test
    fun los_intervalos_rr_y_la_energia_se_ignoran() {
        // 8 bits, energía (bit 3) y RR (bit 4): 0x18, bpm, energía (2), RR (2)
        assertEquals(97, PulsoCodec.decodificar(bytes(0x18, 97, 0x10, 0x00, 0xD2, 0x03)))
    }

    @Test
    fun el_byte_del_pulso_es_sin_signo() = assertEquals(200, PulsoCodec.decodificar(bytes(0x00, 200)))

    @Test
    fun la_medida_es_de_8_bits_sin_extras() = assertArrayEquals(bytes(0x00, 132), PulsoCodec.medida(132))

    @Test
    fun la_medida_hace_ida_y_vuelta() = assertEquals(132, PulsoCodec.decodificar(PulsoCodec.medida(132)))
}
