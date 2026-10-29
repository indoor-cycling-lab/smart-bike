package app.smartbike.dominio

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

class NombreAnunciadoTest {
    private val a = IdentidadDispositivo("50:50:25:6C:66:9C", "1")
    private val b = IdentidadDispositivo("02:11:22:33:4F:2A", "2")

    @Test
    fun sin_simular_es_square_shift() {
        assertEquals("Smart Bike", NombreAnunciado.de(a, simularKickr = false))
    }

    @Test
    fun simulando_lleva_los_cuatro_ultimos_hexadecimales_de_la_mac_en_mayusculas() {
        assertEquals("KICKR BIKE PRO 669C", NombreAnunciado.de(a, simularKickr = true))
        assertEquals("KICKR BIKE PRO 4F2A", NombreAnunciado.de(b, simularKickr = true))
        assertEquals("KICKR BIKE PRO 669C", NombreAnunciado.de(IdentidadDispositivo("50:50:25:6c:66:9c", "1"), simularKickr = true))
    }

    @Test
    fun la_misma_mac_da_el_mismo_nombre_y_macs_distintas_dan_nombres_distintos() {
        assertEquals(NombreAnunciado.de(a, true), NombreAnunciado.de(a.copy(serie = "otra"), true))
        assertNotEquals(NombreAnunciado.de(a, true), NombreAnunciado.de(b, true))
    }

    @Test
    fun una_mac_con_formato_inesperado_da_un_sufijo_valido() {
        assertEquals("KICKR BIKE PRO 0000", NombreAnunciado.de(IdentidadDispositivo("", "1"), true))
        assertEquals("KICKR BIKE PRO 00AB", NombreAnunciado.de(IdentidadDispositivo("zz-AB", "1"), true))
        assertEquals("KICKR BIKE PRO 4567", NombreAnunciado.de(IdentidadDispositivo("1234567", "1"), true))
    }
}
