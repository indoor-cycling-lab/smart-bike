package app.smartbike.dominio

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/** Spec `ride-screen`, «Potencia y cadencia» y «Datos de la sesión», con el Neo a ~1 Hz y reloj virtual. */
class DatosSesionTest {

    private val d = DatosSesion()
    private var t = 0L

    /** [segundos] muestras, una por segundo, a [w] vatios y [rpm]. */
    private fun pedalear(segundos: Int, w: Int, rpm: Int = 90) = repeat(segundos) {
        d.muestra(t, w, rpm)
        t += 1_000
    }

    @Test
    fun media_de_3_s() {
        pedalear(10, 200)
        assertEquals(200, d.resumen.potencia3sW)
        // Pasa a 300 W: sube poco a poco y llega en unos 3 s.
        val vistas = (1..5).map {
            pedalear(1, 300)
            d.resumen.potencia3sW
        }
        assertTrue(vistas.first() in 201..299, "$vistas")
        assertTrue(vistas.zipWithNext().all { (a, b) -> b >= a }, "$vistas")
        assertEquals(300, vistas.last())
    }

    @Test
    fun dejar_de_pedalear() {
        pedalear(10, 250)
        pedalear(5, 0, rpm = 0)
        assertEquals(0, d.resumen.potencia3sW)
    }

    @Test
    fun tiempo_parado() {
        // Spec «Tiempo parado»: 10 min, 2 min parado y 5 min más.
        pedalear(600, 200)
        pedalear(120, 0, rpm = 0)
        pedalear(300, 200)
        // Con una muestra por segundo, el último segundo aún no se ha cerrado.
        assertTrue(kotlin.math.abs(15 * 60_000L - d.resumen.tiempoMs) <= 1_000, "${d.resumen.tiempoMs}")
    }

    @Test
    fun media_sin_las_paradas() {
        // Spec «Media sin las paradas».
        pedalear(600, 200)
        pedalear(300, 0, rpm = 0)
        pedalear(600, 200)
        assertEquals(200, d.resumen.mediaW)
    }

    @Test
    fun maxima() {
        pedalear(60, 200)
        pedalear(5, 650)
        pedalear(60, 180)
        assertEquals(650, d.resumen.maximaW)
        assertEquals(180, d.resumen.potencia3sW)
    }

    @Test
    fun w_kg() {
        // Spec «W/kg»: 247 W con 75 kg.
        pedalear(5, 247)
        assertEquals(3.3, d.resumen.wKg(75))
        assertEquals(3.2, d.resumen.wKg(78))
    }

    @Test
    fun neo_desconectado_no_avanza() {
        // Spec «Neo desconectado»: el tiempo se queda en 20 min mientras no hay Neo.
        pedalear(20 * 60, 200)
        val antes = d.resumen
        d.pausar()
        t += 5 * 60_000 // 5 min sin Neo
        assertEquals(0, d.resumen.potencia3sW)
        assertEquals(antes.tiempoMs, d.resumen.tiempoMs)
        assertEquals(antes.maximaW, d.resumen.maximaW)
        // Al volver, solo cuenta lo que se pedalea desde entonces.
        pedalear(61, 200)
        assertEquals(antes.tiempoMs + 60_000, d.resumen.tiempoMs)
        assertEquals(200, d.resumen.mediaW)
    }

    @Test
    fun hueco_recortado() {
        // Una muestra perdida no suma más de 2 s.
        pedalear(2, 200)
        t += 10_000
        pedalear(1, 200)
        assertEquals(1_000 + DatosSesion.HUECO_MAX_MS, d.resumen.tiempoMs)
    }

    @Test
    fun sesion_nueva_empieza_de_cero() {
        val r = DatosSesion().resumen
        assertEquals(DatosSesion.Resumen(0, 0, 0, 0), r)
    }
}
