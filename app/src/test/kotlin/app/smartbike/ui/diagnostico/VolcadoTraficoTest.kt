package app.smartbike.ui.diagnostico

import app.smartbike.datos.EntradaRegistro
import app.smartbike.datos.Origen
import app.smartbike.datos.RegistroSesion
import app.smartbike.datos.Sentido
import app.smartbike.ui.componentes.FilaHex
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/** «Tráfico en directo» (spec `diagnostics`): sin la casilla no se forman filas; al marcarla salen las recientes. */
@OptIn(ExperimentalCoroutinesApi::class)
class VolcadoTraficoTest {

    private val registro = RegistroSesion(relojMonotonico = { 0L }, relojPared = { 0L })
    private val visible = MutableStateFlow(false)
    private val pausado = MutableStateFlow(false)
    private val limpiadoHasta = MutableStateFlow<EntradaRegistro?>(null)

    private fun pulsar(byte: Int) = registro.trama(Origen.Square, Sentido.Rx, "0x43", byteArrayOf(byte.toByte()))

    /** Recoge el volcado en segundo plano y devuelve la última lista emitida. */
    private fun TestScope.volcado(): () -> List<FilaHex>? {
        var ultima: List<FilaHex>? = null
        backgroundScope.launch { filasDelVolcado(registro, visible, pausado, limpiadoHasta).collect { ultima = it } }
        runCurrent()
        return { ultima }
    }

    private fun TestScope.esperar() {
        advanceTimeBy(DiagnosticoViewModel.PERIODO_TRAFICO_MS + 1)
        runCurrent()
    }

    @Test
    fun `con el tráfico oculto no hay filas aunque lleguen tramas`() = runTest {
        val ultima = volcado()
        pulsar(1)
        pulsar(2)
        esperar()
        assertEquals(emptyList<FilaHex>(), ultima())
    }

    @Test
    fun `al mostrarlo salen también las tramas llegadas mientras estaba oculto`() = runTest {
        val ultima = volcado()
        pulsar(1)
        pulsar(2)
        visible.value = true
        runCurrent()
        assertEquals(listOf("02", "01"), ultima()!!.map { it.contenido })
        pulsar(3)
        esperar()
        assertEquals(listOf("03", "02", "01"), ultima()!!.map { it.contenido })
    }

    @Test
    fun `en pausa la lista no cambia y al ocultarlo queda vacía`() = runTest {
        val ultima = volcado()
        visible.value = true
        pulsar(1)
        esperar()
        pausado.value = true
        pulsar(2)
        esperar()
        assertEquals(listOf("01"), ultima()!!.map { it.contenido })
        visible.value = false
        runCurrent()
        assertTrue(ultima()!!.isEmpty())
    }
}
