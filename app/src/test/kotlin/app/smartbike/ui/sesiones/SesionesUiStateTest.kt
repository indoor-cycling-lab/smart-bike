package app.smartbike.ui.sesiones

import app.smartbike.datos.EstadoGrabacion
import app.smartbike.datos.EstadoSesion
import app.smartbike.datos.SesionGrabada
import java.time.LocalDateTime
import java.util.Locale
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/** Spec `session-recording`: lista, confirmaciones y textos de la pantalla Sesiones grabadas. */
class SesionesUiStateTest {
    private val es = Locale.forLanguageTag("es")
    private val en = Locale.forLanguageTag("en")

    private fun s(nombre: String, bytes: Long, estado: EstadoSesion = EstadoSesion.CERRADA) = SesionGrabada(nombre, null, bytes, estado)

    private val grabando = s("sesion-20261029-184207.txt", 12_000, EstadoSesion.GRABANDO)
    private val cerrada1 = s("sesion-20261028-071500.txt", 18_000)
    private val cerrada2 = s("sesion-20261026-190300.txt", 11_000)
    private val lista = SesionesUiState(listOf(grabando, cerrada1, cerrada2), EstadoGrabacion.Grabando(grabando.nombre))

    @Test
    fun la_que_se_graba_no_es_borrable_y_borrar_todas_la_respeta() {
        assertFalse(grabando.borrable)
        assertEquals(listOf(cerrada1, cerrada2), lista.borrables)
        assertTrue(lista.puedeBorrarTodas)
    }

    @Test
    fun la_que_se_graba_no_pide_confirmacion_de_borrado() {
        assertEquals(lista, lista.pedirBorrar(grabando.nombre))
    }

    @Test
    fun pedir_borrar_una_y_cancelar() {
        val c = lista.pedirBorrar(cerrada1.nombre)
        assertEquals(Confirmacion.Una(cerrada1.nombre), c.confirmacion)
        assertEquals(Confirmacion.Ninguna, c.cancelar().confirmacion)
        assertEquals(3, c.cancelar().sesiones.size)
    }

    @Test
    fun pedir_borrar_todas() {
        assertEquals(Confirmacion.Todas, lista.pedirBorrarTodas().confirmacion)
    }

    @Test
    fun sin_nada_que_borrar_no_se_ofrece_borrar_todas() {
        val solo = SesionesUiState(listOf(grabando))
        assertFalse(solo.puedeBorrarTodas)
        assertEquals(solo, solo.pedirBorrarTodas())
        assertTrue(SesionesUiState().vacia)
        assertFalse(SesionesUiState().puedeBorrarTodas)
    }

    @Test
    fun el_motivo_solo_aparece_si_no_esta_cerrada() {
        assertNull(cerrada1.motivo())
        assertEquals(EstadoSesion.DETENIDA_POR_ESPACIO, s("x.txt", 1, EstadoSesion.DETENIDA_POR_ESPACIO).motivo())
    }

    @Test
    fun la_fecha_lleva_dia_mes_abreviado_ano_y_hora() {
        assertEquals("29 oct 2026 · 18:42", fechaSesion(LocalDateTime.of(2026, 10, 29, 18, 42, 7), "x", es))
        assertEquals("5 ene 2027 · 07:05", fechaSesion(LocalDateTime.of(2027, 1, 5, 7, 5), "x", es))
        assertEquals("rara.txt", fechaSesion(null, "rara.txt", es))
    }

    @Test
    fun el_tamano_se_dice_en_b_kb_o_mb() {
        assertEquals("0 B", tamanoLegible(0, es))
        assertEquals("512 B", tamanoLegible(512, es))
        assertEquals("1 KB", tamanoLegible(1_000, es))
        assertEquals("2 KB", tamanoLegible(1_001, es))
        assertEquals("41 KB", tamanoLegible(41_000, es))
        assertEquals("1,2 MB", tamanoLegible(1_200_000, es))
    }

    @Test
    fun fecha_y_tamano_en_ingles() {
        assertEquals("29 Oct 2026 · 18:42", fechaSesion(LocalDateTime.of(2026, 10, 29, 18, 42, 7), "x", en))
        assertEquals("5 Jan 2027 · 07:05", fechaSesion(LocalDateTime.of(2027, 1, 5, 7, 5), "x", en))
        assertEquals("rara.txt", fechaSesion(null, "rara.txt", en))
        assertEquals("512 B", tamanoLegible(512, en))
        assertEquals("41 KB", tamanoLegible(41_000, en))
        assertEquals("1.2 MB", tamanoLegible(1_200_000, en))
    }
}
