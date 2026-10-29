package app.smartbike.dominio

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class AcumuladorSimulacionTest {
    private val ajustes = SimulacionZwift(cw = 0.51, crr = 0.004, masaBiciKg = 8.51, masaCiclistaKg = 95.0)

    @Test
    fun pendiente_sola() {
        val a = AcumuladorSimulacion()
        assertEquals(Terreno(4.0), a.aplicar(SimulacionZwift(pendientePct = 4.0)))
    }

    @Test
    fun pendiente_tras_ajustes_lleva_crr_y_cw_y_no_el_viento() {
        val a = AcumuladorSimulacion()
        assertNull(a.aplicar(ajustes))
        assertEquals(Terreno(-1.64, crr = 0.004, cw = 0.51, vientoMs = null), a.aplicar(SimulacionZwift(pendientePct = -1.64)))
    }

    @Test
    fun ajustes_tras_la_pendiente_cambian_el_terreno() {
        val a = AcumuladorSimulacion()
        a.aplicar(SimulacionZwift(pendientePct = 2.0))
        assertEquals(Terreno(2.0, crr = 0.004, cw = 0.51), a.aplicar(ajustes))
    }

    @Test
    fun relacion_sola_no_cambia_el_terreno() {
        val a = AcumuladorSimulacion()
        a.aplicar(SimulacionZwift(pendientePct = 3.0))
        assertNull(a.aplicar(SimulacionZwift(relacionVirtual = 1.68)))
        assertEquals(1.68, a.ultima.relacionVirtual!!, 1e-9)
        assertEquals(3.0, a.ultima.pendientePct!!, 1e-9)
    }

    @Test
    fun el_ultimo_valor_gana() {
        val a = AcumuladorSimulacion()
        a.aplicar(SimulacionZwift(pendientePct = 3.0))
        assertEquals(Terreno(-1.0), a.aplicar(SimulacionZwift(pendientePct = -1.0)))
    }

    @Test
    fun potencia_erg_aparte() {
        val a = AcumuladorSimulacion()
        assertNull(a.aplicar(SimulacionZwift(potenciaErgW = 200)))
        assertEquals(200, a.potenciaErgW)
    }

    @Test
    fun reiniciar_olvida_todo() {
        val a = AcumuladorSimulacion()
        a.aplicar(SimulacionZwift(pendientePct = 3.0))
        a.reiniciar()
        assertNull(a.aplicar(ajustes))
    }
}
