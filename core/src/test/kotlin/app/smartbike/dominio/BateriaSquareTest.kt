package app.smartbike.dominio

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/** Spec `square-battery`, «Batería baja». */
class BateriaSquareTest {
    @Test
    fun sin_nivel_no_es_baja() = assertFalse(BateriaSquare.baja(null))

    @Test
    fun en_el_umbral_es_baja_y_justo_por_encima_no() {
        assertTrue(BateriaSquare.baja(20))
        assertFalse(BateriaSquare.baja(21))
        assertTrue(BateriaSquare.baja(0))
        assertFalse(BateriaSquare.baja(100))
    }

    @Test
    fun una_lectura_fuera_de_0_a_100_no_vale() {
        assertNull(BateriaSquare.nivelValido(-1))
        assertNull(BateriaSquare.nivelValido(101))
        assertNull(BateriaSquare.nivelValido(150))
        assertNull(BateriaSquare.nivelValido(null))
        assertEquals(0, BateriaSquare.nivelValido(0))
        assertEquals(100, BateriaSquare.nivelValido(100))
    }

    @Test
    fun la_primera_lectura_de_cada_conexion_no_cuenta() {
        val f = FiltroBateria()
        assertNull(f.lectura(35))
        assertEquals(80, f.lectura(80))
        assertNull(f.lectura(150))
        assertNull(f.lectura(null))
        assertEquals(79, f.lectura(79))
        f.reiniciar()
        assertNull(f.lectura(0))
        assertEquals(0, f.lectura(0))
    }
}
