package app.smartbike.ble

import app.smartbike.plataforma.zwift.MandoZwift
import app.smartbike.protocolo.FtmsCodec
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AnuncioTest {
    @Test
    fun el_mando_sigue_anunciando_un_solo_servicio_con_los_datos_de_fabricante() {
        val a = Anuncio(MandoZwift.SERVICIO_ANUNCIO, MandoZwift.FABRICANTE_ZWIFT, MandoZwift.DATOS_FABRICANTE)
        assertEquals(listOf(MandoZwift.SERVICIO_ANUNCIO), a.servicios16)
        assertEquals(MandoZwift.FABRICANTE_ZWIFT, a.fabricante)
        assertTrue(a.nombreDelMovil)
    }

    @Test
    fun el_rodillo_por_bluetooth_anuncia_ftms_y_cycling_power_con_el_nombre_del_movil() {
        val a = Anuncio(listOf(FtmsCodec.SERVICIO_FTMS, FtmsCodec.SERVICIO_POTENCIA))
        assertEquals(listOf(0x1826, 0x1818), a.servicios16)
        assertNull(a.fabricante)
        assertTrue(a.nombreDelMovil)
    }
}
