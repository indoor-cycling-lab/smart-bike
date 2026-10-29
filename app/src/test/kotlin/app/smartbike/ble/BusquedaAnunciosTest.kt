package app.smartbike.ble

import java.util.UUID
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/** Design.md del change `pulso-zwift`, decisión 5: la búsqueda por servicio, aparte de la del Neo y el Square. */
@OptIn(ExperimentalCoroutinesApi::class)
class BusquedaAnunciosTest {
    private val pulso = CriterioAnuncio.servicio(PulsoClient.CLAVE, PulsoClient.SERVICIO)
    private val neo = CriterioAnuncio.prefijo("Tacx Neo")
    private val square = CriterioAnuncio.prefijo("SQUARE")
    private val servicioOtro = UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb")

    /** Un escáner falso: emite los anuncios y se queda esperando, como el real. */
    private fun escaner(vararg a: AnuncioVisto): Flow<AnuncioVisto> = flow {
        a.forEach { emit(it) }
        awaitCancellation()
    }

    private val banda = AnuncioVisto("HW9-1234", "CC:CC", setOf(PulsoClient.SERVICIO))
    private val otro = AnuncioVisto("Auriculares", "EE:EE", setOf(servicioOtro))

    @Test
    fun encuentra_un_anuncio_con_el_servicio_de_pulso() = runTest {
        val r = resolverBusqueda(escaner(banda), listOf(pulso), 30_000).toList()
        assertEquals(listOf<ResultadoBusqueda>(ResultadoBusqueda.Encontrado(PulsoClient.CLAVE, "CC:CC", "HW9-1234")), r)
        assertEquals(0, currentTime)
    }

    @Test
    fun ignora_un_anuncio_sin_el_servicio_y_vence_a_los_30_s() = runTest {
        val r = resolverBusqueda(escaner(otro), listOf(pulso), 30_000).toList()
        assertEquals(listOf<ResultadoBusqueda>(ResultadoBusqueda.NoEncontrado(PulsoClient.CLAVE)), r)
        assertEquals(30_000, currentTime)
    }

    @Test
    fun se_queda_con_el_primero_que_encuentra() = runTest {
        val segunda = AnuncioVisto("HW9-9999", "DD:DD", setOf(PulsoClient.SERVICIO))
        val r = resolverBusqueda(escaner(otro, banda, segunda), listOf(pulso), 30_000).toList()
        assertEquals("CC:CC", (r.single() as ResultadoBusqueda.Encontrado).direccion)
    }

    @Test
    fun el_criterio_de_servicio_no_se_mezcla_con_el_neo_y_el_square() = runTest {
        val neoAnunciado = AnuncioVisto("Tacx Neo 12345", "AA:AA", emptySet())
        // Buscando el pulso, un Neo no vale; buscando el Neo y el Square, una banda no vale.
        assertEquals(
            listOf<ResultadoBusqueda>(ResultadoBusqueda.NoEncontrado(PulsoClient.CLAVE)),
            resolverBusqueda(escaner(neoAnunciado), listOf(pulso), 30_000).toList(),
        )
        val r = resolverBusqueda(escaner(banda, neoAnunciado), listOf(neo, square), 30_000).toList()
        assertEquals(
            listOf<ResultadoBusqueda>(ResultadoBusqueda.Encontrado("Tacx Neo", "AA:AA", "Tacx Neo 12345"), ResultadoBusqueda.NoEncontrado("SQUARE")),
            r,
        )
    }

    @Test
    fun por_prefijo_no_distingue_mayusculas_y_da_un_resultado_por_criterio() = runTest {
        val sq = AnuncioVisto("square 1234", "BB:BB", emptySet())
        val r = resolverBusqueda(escaner(sq), listOf(neo, square), 30_000).toList()
        assertEquals(ResultadoBusqueda.Encontrado("SQUARE", "BB:BB", "square 1234"), r.first())
        assertEquals(ResultadoBusqueda.NoEncontrado("Tacx Neo"), r.last())
    }

    @Test
    fun un_anuncio_sin_nombre_se_devuelve_con_su_direccion() = runTest {
        val r = resolverBusqueda(escaner(AnuncioVisto(null, "CC:CC", setOf(PulsoClient.SERVICIO))), listOf(pulso), 30_000).toList()
        assertEquals(ResultadoBusqueda.Encontrado(PulsoClient.CLAVE, "CC:CC", "CC:CC"), r.single())
    }
}
