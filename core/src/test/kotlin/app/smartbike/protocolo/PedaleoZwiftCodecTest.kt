package app.smartbike.protocolo

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Test

class PedaleoZwiftCodecTest {
    private fun bytes(h: String) = h.split(" ").map { it.toInt(16).toByte() }.toByteArray()

    @Test
    fun los_cinco_campos_obligatorios() = assertArrayEquals(bytes("03 08 c8 01 10 5a 18 00 20 c8 01 28 00"), PedaleoZwiftCodec.datos(200, 90))

    @Test
    fun potencia_media_y_velocidades() = assertArrayEquals(bytes("03 08 64 10 3c 18 e8 07 20 5a 28 d0 0f"), PedaleoZwiftCodec.datos(100, 60, 1000, 90, 2000))

    @Test
    fun parado() = assertArrayEquals(bytes("03 08 00 10 00 18 00 20 00 28 00"), PedaleoZwiftCodec.datos(0, 0))

    @Test
    fun los_negativos_van_a_cero() = assertArrayEquals(bytes("03 08 00 10 00 18 00 20 00 28 00"), PedaleoZwiftCodec.datos(-5, -1, -1, -1, -1))
}
