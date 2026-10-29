package app.smartbike.dominio

import app.smartbike.dominio.ModoBajadas.LA_MARCHA_NO_CREA_BAJADAS
import app.smartbike.dominio.ModoBajadas.NUNCA_EMPUJAR
import app.smartbike.dominio.ModoBajadas.SIN_PROTECCION
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

/** Tabla de la spec `trainer-resistance`, «Protección en bajadas»: terreno + (marcha − 12) %, con el suelo del modo. */
class ModoBajadasTest {
    private fun enviada(modo: ModoBajadas, terreno: Double, marcha: Int) = modo.limitar(terreno + (marcha - 12), terreno)

    @Test
    fun suelos() {
        assertNull(SIN_PROTECCION.suelo(-7.0))
        assertEquals(0.0, LA_MARCHA_NO_CREA_BAJADAS.suelo(4.0))
        assertEquals(0.0, LA_MARCHA_NO_CREA_BAJADAS.suelo(0.0))
        assertEquals(-7.0, LA_MARCHA_NO_CREA_BAJADAS.suelo(-7.0))
        assertEquals(0.0, NUNCA_EMPUJAR.suelo(-7.0))
    }

    @Test
    fun la_marcha_no_crea_bajadas() {
        assertEquals(0.0, enviada(LA_MARCHA_NO_CREA_BAJADAS, 0.0, 1))
        assertEquals(0.0, enviada(LA_MARCHA_NO_CREA_BAJADAS, 4.0, 1))
        assertEquals(-7.0, enviada(LA_MARCHA_NO_CREA_BAJADAS, -7.0, 1))
        assertEquals(-7.0, enviada(LA_MARCHA_NO_CREA_BAJADAS, -7.0, 12))
        assertEquals(5.0, enviada(LA_MARCHA_NO_CREA_BAJADAS, -7.0, 24))
        assertEquals(12.0, enviada(LA_MARCHA_NO_CREA_BAJADAS, 0.0, 24))
    }

    @Test
    fun nunca_empujar() {
        assertEquals(0.0, enviada(NUNCA_EMPUJAR, -7.0, 12))
        assertEquals(0.0, enviada(NUNCA_EMPUJAR, -7.0, 1))
        assertEquals(3.0, enviada(NUNCA_EMPUJAR, -7.0, 22))
        assertEquals(0.0, enviada(NUNCA_EMPUJAR, 0.0, 1))
    }

    @Test
    fun sin_proteccion() {
        assertEquals(-18.0, enviada(SIN_PROTECCION, -7.0, 1))
        assertEquals(-11.0, enviada(SIN_PROTECCION, 0.0, 1))
    }
}
