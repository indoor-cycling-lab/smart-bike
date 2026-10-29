package app.smartbike.dominio

import app.smartbike.dominio.TipoGesto.Corta
import app.smartbike.dominio.TipoGesto.Doble
import app.smartbike.dominio.TipoGesto.Larga
import app.smartbike.dominio.TipoGesto.Pulsar
import app.smartbike.dominio.TipoGesto.Repetir
import app.smartbike.protocolo.SquareButton
import app.smartbike.protocolo.SquareButton.A
import app.smartbike.protocolo.SquareButton.CampagnoloDerecho
import app.smartbike.protocolo.SquareButton.CampagnoloIzquierdo
import app.smartbike.protocolo.SquareButton.Cuadrado
import app.smartbike.protocolo.SquareButton.PalancaDerecha1
import app.smartbike.protocolo.SquareButton.PalancaDerecha2
import app.smartbike.protocolo.SquareButton.PalancaIzquierda1
import app.smartbike.protocolo.SquareButton.Triangulo
import app.smartbike.protocolo.SquareButton.Y
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

/**
 * Escenarios de la spec `button-gestures` con un reloj virtual: [pulsar] y [soltar] llevan la máscara de botones
 * pulsados como la daría `EventosBoton`, y [hasta] hace lo que haría el temporizador de la `Sesion`.
 */
class ReconocedorGestosTest {

    /** Con los tiempos que nombran los escenarios de la spec: larga y retardo de 500 ms. */
    private var reconocedor = ReconocedorGestos(tiempos = TiemposGestos(largaMs = 500, retardoRepeticionMs = 500))
    private var mascara = 0u
    private val gestos = mutableListOf<Gesto>()

    private fun pulsar(b: SquareButton, t: Long) {
        mascara = mascara or b.mascara
        gestos += reconocedor.procesar(EventoBoton.Pulsado(b, t, mascara))
    }

    private fun soltar(b: SquareButton, t: Long) {
        mascara = mascara and b.mascara.inv()
        gestos += reconocedor.procesar(EventoBoton.Soltado(b, t))
    }

    /** Vence los temporizadores uno a uno, en su hora, hasta [t]. */
    private fun hasta(t: Long) {
        while (true) {
            val v = reconocedor.proximoVencimiento() ?: return
            if (v > t) return
            gestos += reconocedor.vencer(v)
        }
    }

    private fun g(b: SquareButton, tipo: TipoGesto, t: Long, m: SquareButton? = null) = Gesto(Entrada(b, m), tipo, t)

    private fun asignar(b: SquareButton, tipo: TipoGesto, a: Accion) {
        reconocedor.asignacion = reconocedor.asignacion.asignar(Entrada(b), tipo, a)
    }

    @Test
    fun palanca_da_pulsar_al_instante_sin_temporizador_al_soltar() {
        pulsar(PalancaDerecha1, 10)
        assertEquals(listOf(g(PalancaDerecha1, Pulsar, 10)), gestos)
        soltar(PalancaDerecha1, 100)
        assertEquals(1, gestos.size)
        assertNull(reconocedor.proximoVencimiento())
    }

    @Test
    fun pulsacion_larga_sin_esperar_a_soltar() {
        pulsar(Cuadrado, 0)
        assertEquals(500L, reconocedor.proximoVencimiento())
        hasta(500)
        assertEquals(listOf(g(Cuadrado, Larga, 500)), gestos)
        soltar(Cuadrado, 600)
        assertEquals(listOf(g(Cuadrado, Larga, 500)), gestos)
    }

    @Test
    fun pulsacion_corta_al_soltar() {
        pulsar(Cuadrado, 0)
        hasta(200)
        soltar(Cuadrado, 200)
        assertEquals(listOf(g(Cuadrado, Corta, 200)), gestos)
        assertNull(reconocedor.proximoVencimiento())
    }

    @Test
    fun suelta_tardia_tras_el_umbral_es_larga() {
        // Sin que el temporizador haya llegado a vencer, la suelta a los 600 ms ya es larga.
        pulsar(Cuadrado, 0)
        soltar(Cuadrado, 600)
        assertEquals(listOf(g(Cuadrado, Larga, 500)), gestos)
    }

    @Test
    fun doble_pulsacion() {
        asignar(Triangulo, Corta, Accion.MarchaMas(1))
        asignar(Triangulo, Doble, Accion.MarchaMenos(1))
        pulsar(Triangulo, 0)
        soltar(Triangulo, 100)
        hasta(250)
        pulsar(Triangulo, 250)
        assertEquals(listOf(g(Triangulo, Doble, 250)), gestos)
        soltar(Triangulo, 350)
        hasta(2000)
        assertEquals(listOf(g(Triangulo, Doble, 250)), gestos)
    }

    @Test
    fun corta_con_doble_asignada_espera_la_ventana() {
        asignar(Triangulo, Corta, Accion.MarchaMas(1))
        asignar(Triangulo, Doble, Accion.MarchaMenos(1))
        pulsar(Triangulo, 0)
        soltar(Triangulo, 100)
        hasta(399)
        assertEquals(emptyList<Gesto>(), gestos)
        hasta(400)
        assertEquals(listOf(g(Triangulo, Corta, 400)), gestos)
    }

    @Test
    fun segunda_pulsacion_fuera_de_la_ventana_son_dos_cortas() {
        asignar(Triangulo, Corta, Accion.MarchaMas(1))
        asignar(Triangulo, Doble, Accion.MarchaMenos(1))
        pulsar(Triangulo, 0)
        soltar(Triangulo, 100)
        pulsar(Triangulo, 500)
        soltar(Triangulo, 600)
        hasta(1000)
        assertEquals(listOf(g(Triangulo, Corta, 400), g(Triangulo, Corta, 900)), gestos)
    }

    @Test
    fun solo_corta_asignada_no_espera_ninguna_ventana() {
        reconocedor.asignacion = reconocedor.asignacion.quitar(Entrada(Cuadrado), Larga)
        pulsar(Cuadrado, 0)
        assertNull(reconocedor.proximoVencimiento())
        soltar(Cuadrado, 2000)
        assertEquals(listOf(g(Cuadrado, Corta, 2000)), gestos)
    }

    @Test
    fun mantener_la_palanca_repite() {
        // Spec «Mantener la palanca»: 500 ms de retardo, 250 ms de ritmo, 1100 ms mantenida.
        pulsar(PalancaDerecha1, 0)
        hasta(1100)
        soltar(PalancaDerecha1, 1100)
        hasta(5000)
        assertEquals(
            listOf(g(PalancaDerecha1, Pulsar, 0), g(PalancaDerecha1, Repetir, 500), g(PalancaDerecha1, Repetir, 750), g(PalancaDerecha1, Repetir, 1000)),
            gestos,
        )
    }

    @Test
    fun autorrepeticion_desactivada() {
        reconocedor.autorrepeticion = false
        pulsar(PalancaDerecha1, 0)
        hasta(2000)
        soltar(PalancaDerecha1, 2000)
        assertEquals(listOf(g(PalancaDerecha1, Pulsar, 0)), gestos)
    }

    @Test
    fun pulsaciones_rapidas_sin_repeticion() {
        for (i in 0 until 5) {
            pulsar(PalancaDerecha1, i * 200L)
            hasta(i * 200L + 150)
            soltar(PalancaDerecha1, i * 200L + 150)
        }
        assertEquals(5, gestos.size)
        assertEquals(setOf(Pulsar), gestos.map { it.tipo }.toSet())
    }

    @Test
    fun ir_a_un_extremo_no_se_repite() {
        pulsar(CampagnoloDerecho, 0)
        pulsar(PalancaDerecha2, 100)
        hasta(2100)
        assertEquals(listOf(g(PalancaDerecha2, Pulsar, 100, CampagnoloDerecho)), gestos)
    }

    @Test
    fun parar_la_repeticion_hasta_soltar() {
        pulsar(PalancaDerecha1, 0)
        hasta(500)
        reconocedor.pararRepeticion(Entrada(PalancaDerecha1))
        hasta(3000)
        assertEquals(listOf(g(PalancaDerecha1, Pulsar, 0), g(PalancaDerecha1, Repetir, 500)), gestos)
        soltar(PalancaDerecha1, 3000)
        pulsar(PalancaDerecha1, 3100)
        hasta(3600)
        assertEquals(g(PalancaDerecha1, Repetir, 3600), gestos.last())
    }

    @Test
    fun conexion_perdida_detiene_la_repeticion() {
        pulsar(PalancaDerecha1, 0)
        hasta(600)
        reconocedor.reiniciar()
        assertNull(reconocedor.proximoVencimiento())
        hasta(5000)
        assertEquals(2, gestos.size)
    }

    @Test
    fun conexion_perdida_no_da_una_corta() {
        pulsar(Cuadrado, 0)
        reconocedor.reiniciar()
        hasta(5000)
        assertEquals(emptyList<Gesto>(), gestos)
    }

    @Test
    fun combinacion_con_modificador() {
        pulsar(CampagnoloIzquierdo, 0)
        pulsar(Y, 100)
        assertEquals(listOf(g(Y, Pulsar, 100, CampagnoloIzquierdo)), gestos)
    }

    @Test
    fun campagnolo_pulsado_despues_no_forma_combinacion() {
        pulsar(PalancaDerecha1, 0)
        pulsar(CampagnoloDerecho, 50)
        soltar(CampagnoloDerecho, 100)
        soltar(PalancaDerecha1, 150)
        hasta(2000)
        assertEquals(listOf(g(PalancaDerecha1, Pulsar, 0)), gestos)
    }

    @Test
    fun campagnolo_usado_como_modificador_no_da_su_corta() {
        reconocedor.asignacion = reconocedor.asignacion.asignar(Entrada(CampagnoloIzquierdo), Corta, Accion.MarchaReferencia)
        pulsar(CampagnoloIzquierdo, 0)
        pulsar(PalancaIzquierda1, 100)
        soltar(PalancaIzquierda1, 200)
        soltar(CampagnoloIzquierdo, 300)
        hasta(2000)
        assertEquals(listOf(g(PalancaIzquierda1, Pulsar, 100, CampagnoloIzquierdo)), gestos)
    }

    @Test
    fun campagnolo_usado_como_modificador_no_da_su_larga() {
        reconocedor.asignacion = reconocedor.asignacion.asignar(Entrada(CampagnoloIzquierdo), Larga, Accion.MarchaReferencia)
        pulsar(CampagnoloIzquierdo, 0)
        pulsar(PalancaIzquierda1, 100)
        soltar(PalancaIzquierda1, 200)
        hasta(2000)
        assertEquals(listOf(g(PalancaIzquierda1, Pulsar, 100, CampagnoloIzquierdo)), gestos)
    }

    @Test
    fun campagnolo_solo_da_corta_y_nunca_pulsar() {
        pulsar(CampagnoloIzquierdo, 0)
        assertEquals(emptyList<Gesto>(), gestos)
        soltar(CampagnoloIzquierdo, 150)
        assertEquals(listOf(g(CampagnoloIzquierdo, Corta, 150)), gestos)
    }

    @Test
    fun los_dos_campagnolo_no_forman_combinacion() {
        pulsar(CampagnoloIzquierdo, 0)
        pulsar(CampagnoloDerecho, 50)
        pulsar(PalancaDerecha1, 100)
        soltar(PalancaDerecha1, 200)
        soltar(CampagnoloDerecho, 300)
        soltar(CampagnoloIzquierdo, 400)
        hasta(5000)
        assertEquals(emptyList<Gesto>(), gestos)
    }

    @Test
    fun boton_sin_accion_da_pulsar_para_diagnostico() {
        pulsar(Y, 0)
        soltar(Y, 100)
        assertEquals(listOf(g(Y, Pulsar, 0)), gestos)
    }

    @Test
    fun umbral_de_larga_configurable() {
        reconocedor.tiempos = TiemposGestos(largaMs = 1000)
        pulsar(Cuadrado, 0)
        hasta(900)
        soltar(Cuadrado, 900)
        assertEquals(listOf(g(Cuadrado, Corta, 900)), gestos)
    }

    @Test
    fun pulsacion_normal_con_los_tiempos_por_defecto() {
        // Spec «Pulsación normal sin repetición»: la suelta llega 650 ms después, como en las trazas reales.
        reconocedor = ReconocedorGestos()
        pulsar(PalancaDerecha1, 0)
        hasta(650)
        soltar(PalancaDerecha1, 650)
        pulsar(Cuadrado, 1000)
        hasta(1650)
        soltar(Cuadrado, 1650)
        assertEquals(listOf(g(PalancaDerecha1, Pulsar, 0), g(Cuadrado, Corta, 1650)), gestos)
    }

    @Test
    fun cambio_de_asignacion_con_un_boton_pulsado() {
        // La pulsación en curso sigue con lo que tenía al pulsar; la siguiente usa la asignación nueva.
        pulsar(Cuadrado, 0)
        reconocedor.asignacion = reconocedor.asignacion.asignar(Entrada(Cuadrado), Pulsar, Accion.MarchaMas(1))
        soltar(Cuadrado, 100)
        pulsar(Cuadrado, 200)
        assertEquals(listOf(g(Cuadrado, Corta, 100), g(Cuadrado, Pulsar, 200)), gestos)
    }

    @Test
    fun palancas_invertidas_repiten_con_su_accion_nueva() {
        reconocedor.asignacion = reconocedor.asignacion.copy(invertirPalancas = true)
        pulsar(PalancaDerecha2, 0)
        hasta(500)
        assertEquals(listOf(g(PalancaDerecha2, Pulsar, 0), g(PalancaDerecha2, Repetir, 500)), gestos)
        assertEquals(Accion.MarchaMenos(1), reconocedor.asignacion.accion(gestos.last()))
    }

    @Test
    fun a_sin_accion_con_campagnolo() {
        pulsar(CampagnoloDerecho, 0)
        pulsar(A, 50)
        assertEquals(listOf(g(A, Pulsar, 50, CampagnoloDerecho)), gestos)
    }
}
