package app.smartbike.protocolo

import app.smartbike.protocolo.FtmsCodec.Comando
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class FtmsCodecTest {

    private fun hex(b: ByteArray) = b.joinToString(" ") { "%02x".format(it) }
    private fun bytes(h: String) = h.split(" ").map { it.toInt(16).toByte() }.toByteArray()

    @Test
    fun caracteristicas_de_lectura() {
        assertEquals("02 40 00 00 08 20 00 00", hex(FtmsCodec.feature()))
        assertEquals("00 00 d0 07 01 00", hex(FtmsCodec.rangoPotencia()))
        assertEquals("00 01", hex(FtmsCodec.trainingStatus()))
        assertEquals("08 00 00 00", hex(FtmsCodec.cpFeature()))
        assertEquals("0d", hex(FtmsCodec.cpUbicacion()))
    }

    @Test
    fun indoor_bike_data() {
        assertEquals("44 00 52 0d b6 00 f7 00", hex(FtmsCodec.indoorBikeData(34.10, 91, 247)))
        assertEquals("44 00 00 00 00 00 00 00", hex(FtmsCodec.indoorBikeData(0.0, 0, 0)))
    }

    @Test
    fun cycling_power_con_y_sin_biela() {
        assertEquals("20 00 ee 00 3c 12 8a 51", hex(FtmsCodec.cyclingPower(238, TelemetriaCodec.Biela(0x123C, 0x518A))))
        assertEquals("00 00 c8 00", hex(FtmsCodec.cyclingPower(200, null)))
    }

    @Test
    fun simulacion() {
        assertEquals(Comando.Simulacion(0.0, 4.0, 0.004, 0.51), FtmsCodec.comando(bytes("11 00 00 90 01 28 33")))
    }

    @Test
    fun simulacion_en_bajada_con_viento() {
        // Viento −1,5 m/s (0xfa24), pendiente −3,00 % (0xfed4).
        val c = FtmsCodec.comando(bytes("11 24 fa d4 fe 28 33")) as Comando.Simulacion
        assertEquals(-1.5, c.vientoMs, 1e-9)
        assertEquals(-3.0, c.pendientePct, 1e-9)
    }

    @Test
    fun potencia_objetivo() {
        assertEquals(Comando.PotenciaObjetivo(230), FtmsCodec.comando(bytes("05 e6 00")))
    }

    @Test
    fun resto_de_opcodes() {
        assertEquals(Comando.PedirControl, FtmsCodec.comando(bytes("00")))
        assertEquals(Comando.Reiniciar, FtmsCodec.comando(bytes("01")))
        assertEquals(Comando.Iniciar, FtmsCodec.comando(bytes("07")))
        assertEquals(Comando.Parar(pausa = true), FtmsCodec.comando(bytes("08 02")))
        assertEquals(Comando.NoAdmitido(0x04), FtmsCodec.comando(bytes("04 64")))
        assertEquals(Comando.Invalido(0x11), FtmsCodec.comando(bytes("11 00 00")))
        assertEquals(Comando.Invalido(0x05), FtmsCodec.comando(bytes("05")))
    }

    @Test
    fun respuesta() {
        assertEquals("80 11 01", hex(FtmsCodec.respuesta(0x11, FtmsCodec.EXITO)))
        assertEquals("80 04 02", hex(FtmsCodec.respuesta(0x04, FtmsCodec.NO_ADMITIDO)))
    }
}
