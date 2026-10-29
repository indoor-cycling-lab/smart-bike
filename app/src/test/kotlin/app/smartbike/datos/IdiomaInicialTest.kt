package app.smartbike.datos

import app.smartbike.dominio.Configuracion
import app.smartbike.dominio.IdiomaApp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/** Spec `app-language`, «Idioma del primer arranque». */
class IdiomaInicialTest {
    private class AlmacenFalso(inicial: Configuracion = Configuracion()) : AlmacenConfiguracion {
        val actual = MutableStateFlow(inicial)
        var escrituras = 0
        override val configuracion: Flow<Configuracion> = actual
        override suspend fun actualizarConfiguracion(cambio: (Configuracion) -> Configuracion) {
            escrituras++
            actual.value = cambio(actual.value)
        }
    }

    @Test
    fun instalacion_limpia_con_el_movil_en_espanol() = runTest {
        val a = AlmacenFalso()
        fijarIdiomaInicial(a, "es")
        assertEquals(IdiomaApp.ES, a.actual.value.idioma)
    }

    @Test
    fun instalacion_limpia_con_el_movil_en_frances() = runTest {
        val a = AlmacenFalso()
        fijarIdiomaInicial(a, "fr")
        assertEquals(IdiomaApp.EN, a.actual.value.idioma)
    }

    @Test
    fun ajustes_anteriores_sin_idioma_conservan_lo_demas() = runTest {
        val a = AlmacenFalso(Configuracion(pesoCiclistaKg = 90))
        fijarIdiomaInicial(a, "en")
        assertEquals(IdiomaApp.EN, a.actual.value.idioma)
        assertEquals(90, a.actual.value.pesoCiclistaKg)
    }

    @Test
    fun con_idioma_elegido_no_sigue_al_movil() = runTest {
        val a = AlmacenFalso(Configuracion(idioma = IdiomaApp.ES))
        fijarIdiomaInicial(a, "en")
        assertEquals(IdiomaApp.ES, a.actual.value.idioma)
        assertEquals(0, a.escrituras)
    }
}
