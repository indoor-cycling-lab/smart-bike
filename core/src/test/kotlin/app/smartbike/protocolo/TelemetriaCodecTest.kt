package app.smartbike.protocolo

import app.smartbike.protocolo.TelemetriaCodec.Biela
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class TelemetriaCodecTest {

    private fun hex(s: String) = ByteArray(s.length / 2) { s.substring(it * 2, it * 2 + 2).toInt(16).toByte() }

    @Test
    fun potencia_del_neo_real() {
        // Tramas de la traza neo-erg-200w.txt.
        assertEquals(186, TelemetriaCodec.potencia(hex("3000ba003e0100007a345e007e2d")))
        assertEquals(0, TelemetriaCodec.potencia(hex("300000000d010000b088470000d8")))
    }

    @Test
    fun potencia_negativa_y_trama_corta() {
        assertEquals(-2, TelemetriaCodec.potencia(hex("0000feff")))
        assertNull(TelemetriaCodec.potencia(hex("3000ba")))
    }

    @Test
    fun biela_con_rueda_y_sin_rueda() {
        assertEquals(Biela(0x5e, 0x2d7e), TelemetriaCodec.biela(hex("033e010000451a5e007e2d")))
        assertEquals(Biela(0x0102, 0x0400), TelemetriaCodec.biela(hex("0202010004")))
        assertNull(TelemetriaCodec.biela(hex("010d01000060")))
        assertNull(TelemetriaCodec.biela(hex("0302")))
    }

    @Test
    fun cadencia_por_eventos() {
        val c = CalculoCadencia()
        assertEquals(0, c.biela(Biela(10, 0), 0))
        // Una vuelta en 1024/1024 s = 60 rpm.
        assertEquals(60, c.biela(Biela(11, 1024), 1000))
        // Dos vueltas en 1,5 s = 80 rpm.
        assertEquals(80, c.biela(Biela(13, 1024 + 1536), 2500))
    }

    @Test
    fun cadencia_con_desbordamiento_de_16_bits() {
        val c = CalculoCadencia()
        c.biela(Biela(0xFFFF, 0xFF00), 0)
        assertEquals(60, c.biela(Biela(0x0000, 0xFF00 + 1024 - 0x10000), 1000))
    }

    @Test
    fun cadencia_a_cero_sin_eventos_en_2_s_o_con_potencia_0() {
        val c = CalculoCadencia()
        c.biela(Biela(10, 0), 0)
        c.biela(Biela(11, 1024), 1000)
        assertEquals(60, c.biela(Biela(11, 1024), 2500))
        assertEquals(0, c.biela(Biela(11, 1024), 3100))
        c.biela(Biela(12, 2048), 4000)
        assertEquals(0, c.potencia(0))
    }
}
