package app.smartbike.dominio

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class LimiteRitmoTest {
    private var t = 0L
    private val limite = LimiteRitmo({ t })

    @Test
    fun una_rafaga_de_la_capacidad_pasa_y_la_siguiente_no() {
        repeat(LimiteRitmo.CAPACIDAD) { assertTrue(limite.permitir()) }
        assertFalse(limite.permitir())
    }

    @Test
    fun al_ritmo_de_recarga_sostenido_nunca_se_agota() {
        repeat(10_000) {
            t += 20 // 50 por segundo
            assertTrue(limite.permitir())
        }
    }

    @Test
    fun se_recupera_tras_una_pausa() {
        repeat(LimiteRitmo.CAPACIDAD) { limite.permitir() }
        assertFalse(limite.permitir())
        t += 10_000
        repeat(LimiteRitmo.CAPACIDAD) { assertTrue(limite.permitir()) }
        assertFalse(limite.permitir())
    }
}
