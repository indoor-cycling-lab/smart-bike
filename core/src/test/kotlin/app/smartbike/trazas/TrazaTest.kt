package app.smartbike.trazas

import app.smartbike.protocolo.SquareDecoder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TrazaTest {

    private val traza = Traza.leer("ejemplo-escrita-a-mano.txt")

    @Test
    fun lee_lineas_sueltas_y_de_logcat() {
        assertEquals(7, traza.size)
        assertEquals("Square: conectado", traza.first().texto)
        assertEquals(1000L, traza[1].tMs)
        assertEquals(8, traza[1].datos.size)
        assertEquals("fec3", traza.last().caracteristica)
    }

    @Test
    fun los_bytes_se_pueden_decodificar() {
        val mascaras = traza.filter { it.caracteristica == "0x43" }.map { SquareDecoder.mascara(it.datos) }
        assertEquals(listOf(0x8000_0000u, 0x8002_0000u, 0x8002_0000u, 0x8000_0000u, 0u), mascaras)
    }
}
