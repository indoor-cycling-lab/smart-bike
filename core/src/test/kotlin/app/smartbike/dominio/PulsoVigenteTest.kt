package app.smartbike.dominio

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/** Spec `heart-rate`, «Pulso válido» y «Pérdida del pulsómetro». */
class PulsoVigenteTest {
    private var t = 0L
    private val p = PulsoVigente({ t })

    @Test
    fun sin_lecturas_no_hay_pulso() = assertNull(p.valor())

    @Test
    fun una_lectura_normal_es_el_pulso() {
        assertTrue(p.registrar(132))
        assertEquals(132, p.valor())
    }

    @Test
    fun cero_255_y_negativos_se_ignoran_y_mantienen_el_anterior() {
        p.registrar(120)
        t = 1_000
        assertFalse(p.registrar(0))
        assertFalse(p.registrar(255))
        assertFalse(p.registrar(-1))
        assertEquals(120, p.valor())
    }

    @Test
    fun los_limites_valen() {
        assertTrue(p.registrar(1))
        assertTrue(p.registrar(250))
        assertEquals(250, p.valor())
    }

    @Test
    fun caduca_a_los_5_s_y_vuelve_con_la_siguiente_lectura_valida() {
        p.registrar(120)
        t = 4_999
        assertEquals(120, p.valor())
        t = 5_000
        assertNull(p.valor())
        p.registrar(125)
        assertEquals(125, p.valor())
    }

    @Test
    fun una_lectura_ignorada_no_alarga_la_vigencia() {
        p.registrar(120)
        t = 4_000
        p.registrar(0)
        t = 5_000
        assertNull(p.valor())
    }

    @Test
    fun perder_lo_pone_a_null_al_instante() {
        p.registrar(120)
        p.perder()
        assertNull(p.valor())
        p.registrar(121)
        assertEquals(121, p.valor())
    }
}
