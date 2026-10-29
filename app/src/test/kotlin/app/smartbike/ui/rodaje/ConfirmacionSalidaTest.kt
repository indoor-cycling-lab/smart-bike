package app.smartbike.ui.rodaje

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ConfirmacionSalidaTest {
    private var terminadas = 0

    private fun TestScope.salida() = ConfirmacionSalida(backgroundScope) { terminadas++ }

    @Test
    fun el_primer_atras_pide_confirmacion_sin_terminar() = runTest {
        val s = salida()
        s.atras()
        assertEquals(SalidaUi.Confirmar, s.estado.value)
        assertEquals(0, terminadas)
    }

    @Test
    fun el_segundo_atras_a_tiempo_termina_una_vez() = runTest {
        val s = salida()
        s.atras()
        advanceTimeBy(2_900)
        s.atras()
        assertEquals(SalidaUi.Cerrando, s.estado.value)
        assertEquals(1, terminadas)
        advanceTimeBy(10_000)
        assertEquals(SalidaUi.Cerrando, s.estado.value)
    }

    @Test
    fun el_aviso_desaparece_a_los_3_s_y_el_segundo_atras_tarde_vuelve_a_pedir() = runTest {
        val s = salida()
        s.atras()
        advanceTimeBy(3_001)
        assertEquals(SalidaUi.Ninguna, s.estado.value)
        s.atras()
        assertEquals(SalidaUi.Confirmar, s.estado.value)
        assertEquals(0, terminadas)
    }

    @Test
    fun atras_mientras_se_cierra_no_hace_nada() = runTest {
        val s = salida()
        s.atras()
        s.atras()
        s.atras()
        assertEquals(SalidaUi.Cerrando, s.estado.value)
        assertEquals(1, terminadas)
    }

    @Test
    fun cancelar_limpia_la_confirmacion_y_el_siguiente_atras_vuelve_a_pedir() = runTest {
        val s = salida()
        s.atras()
        s.cancelar()
        assertEquals(SalidaUi.Ninguna, s.estado.value)
        s.atras()
        assertEquals(SalidaUi.Confirmar, s.estado.value)
        assertEquals(0, terminadas)
    }

    @Test
    fun cancelar_no_interrumpe_el_cierre() = runTest {
        val s = salida()
        s.atras()
        s.atras()
        s.cancelar()
        assertEquals(SalidaUi.Cerrando, s.estado.value)
    }
}
