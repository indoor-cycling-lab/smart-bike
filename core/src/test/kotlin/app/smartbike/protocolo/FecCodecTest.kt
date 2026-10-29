package app.smartbike.protocolo

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class FecCodecTest {

    private fun hex(b: ByteArray) = b.joinToString(" ") { "%02x".format(it) }

    @ParameterizedTest
    @CsvSource(
        "0.0,   a4 09 4e 05 33 ff ff ff ff 20 4e ff 44",
        "3.0,   a4 09 4e 05 33 ff ff ff ff 4c 4f ff 29",
        "5.0,   a4 09 4e 05 33 ff ff ff ff 14 50 ff 6e",
        "-5.0,  a4 09 4e 05 33 ff ff ff ff 2c 4c ff 4a",
        "20.0,  a4 09 4e 05 33 ff ff ff ff f0 55 ff 8f",
        "-10.0, a4 09 4e 05 33 ff ff ff ff 38 4a ff 58",
        "2.5,   a4 09 4e 05 33 ff ff ff ff 1a 4f ff 7f",
    )
    fun pendiente(pct: Double, esperado: String) {
        assertEquals(esperado, hex(FecCodec.pendiente(pct)))
    }

    @Test
    fun pendiente_redondea_a_centesimas() {
        assertEquals(hex(FecCodec.pendiente(1.0)), hex(FecCodec.pendiente(1.004)))
    }

    @ParameterizedTest
    @CsvSource(
        "50,   a4 09 4e 05 31 ff ff ff ff ff c8 00 e0",
        "200,  a4 09 4e 05 31 ff ff ff ff ff 20 03 0b",
        "1000, a4 09 4e 05 31 ff ff ff ff ff a0 0f 87",
    )
    fun potencia(vatios: Int, esperado: String) {
        assertEquals(esperado, hex(FecCodec.potencia(vatios)))
    }

    @Test
    fun trece_bytes() {
        assertEquals(13, FecCodec.pendiente(0.0).size)
        assertEquals(13, FecCodec.potencia(0).size)
    }

    @Test
    fun fuera_de_rango() {
        assertThrows<IllegalArgumentException> { FecCodec.pendiente(200.5) }
        assertThrows<IllegalArgumentException> { FecCodec.pendiente(-200.5) }
        assertThrows<IllegalArgumentException> { FecCodec.potencia(-1) }
        assertThrows<IllegalArgumentException> { FecCodec.potencia(4001) }
    }
}
