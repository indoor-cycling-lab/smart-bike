package app.smartbike.ui.rodaje

import androidx.compose.ui.unit.dp
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PantallaBajaTest {
    private val seis = listOf("potencia", "cadencia", "tiempo", "w/kg", "media", "maxima")

    @Test
    fun el_umbral_es_760_dp() {
        assertTrue(esPantallaBaja(640.dp))
        assertTrue(esPantallaBaja(759.dp))
        assertFalse(esPantallaBaja(760.dp))
        assertFalse(esPantallaBaja(844.dp))
    }

    @Test
    fun con_botones_en_pantalla_baja_solo_los_cuatro_primeros() {
        val r = recorteDatos(640.dp, botones = true, datos = seis)
        assertEquals(listOf("potencia", "cadencia", "tiempo", "w/kg"), r.visibles)
        assertEquals(listOf("media", "maxima"), r.ocultos)
    }

    @Test
    fun con_cinco_datos_se_oculta_el_quinto() {
        val r = recorteDatos(720.dp, botones = true, datos = seis.take(5))
        assertEquals(seis.take(4), r.visibles)
        assertEquals(listOf("media"), r.ocultos)
    }

    @Test
    fun con_cuatro_datos_o_menos_no_se_oculta_nada() {
        for (n in 1..4) {
            val r = recorteDatos(640.dp, botones = true, datos = seis.take(n))
            assertEquals(seis.take(n), r.visibles)
            assertTrue(r.ocultos.isEmpty())
        }
    }

    @Test
    fun sin_botones_se_ven_los_seis() {
        val r = recorteDatos(640.dp, botones = false, datos = seis)
        assertEquals(seis, r.visibles)
        assertTrue(r.ocultos.isEmpty())
    }

    @Test
    fun en_pantalla_alta_se_ven_los_seis_aunque_haya_botones() {
        assertEquals(seis, recorteDatos(760.dp, botones = true, datos = seis).visibles)
        assertEquals(seis, recorteDatos(844.dp, botones = true, datos = seis).visibles)
    }

    @Test
    fun no_cambia_los_datos_elegidos() {
        val elegidos = seis.toList()
        recorteDatos(640.dp, botones = true, datos = elegidos)
        assertEquals(seis, elegidos)
    }
}
