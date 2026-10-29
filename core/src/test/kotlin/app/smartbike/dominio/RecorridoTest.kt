package app.smartbike.dominio

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class RecorridoTest {
    @Test
    fun diez_minutos_a_24_kmh_son_unos_4_km() {
        val r = Recorrido()
        var t = 0L
        repeat(2400) {
            t += 250
            r.muestra(t, 24 / 3.6)
        }
        assertEquals(4.0, r.distanciaM / 1000, 0.01)
    }

    @Test
    fun parado_no_avanza() {
        val r = Recorrido()
        r.muestra(0, 6.0)
        r.muestra(1000, 0.0)
        r.muestra(2000, 0.0)
        assertEquals(0.0, r.distanciaM, 0.0)
    }

    @Test
    fun sin_velocidad_conserva_y_no_integra_el_hueco() {
        val r = Recorrido()
        r.muestra(0, 6.0)
        r.muestra(1000, 6.0)
        r.muestra(1500, null)
        r.sinVelocidad(60_000)
        r.muestra(61_000, 6.0)
        assertEquals(12.0, r.distanciaM, 1e-9)
    }

    @Test
    fun un_hueco_largo_se_acota_a_2_s() {
        val r = Recorrido()
        r.muestra(0, 5.0)
        r.muestra(30_000, 5.0)
        assertEquals(10.0, r.distanciaM, 1e-9)
    }
}
