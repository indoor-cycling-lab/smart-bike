package app.smartbike.ui.marchas

import app.smartbike.datos.AlmacenConfiguracion
import app.smartbike.dominio.AjustesLineal
import app.smartbike.dominio.Configuracion
import app.smartbike.dominio.Desarrollos
import app.smartbike.dominio.EstadoMarcha
import app.smartbike.dominio.ModoCambio
import app.smartbike.dominio.Transmision
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/** Escenarios de la spec `settings-screen` sobre la pantalla de Marchas. */
@OptIn(ExperimentalCoroutinesApi::class)
class MarchasUiStateTest {

    private class AjustesFalsos : AlmacenConfiguracion {
        override val configuracion = MutableStateFlow(Configuracion())
        override suspend fun actualizarConfiguracion(cambio: (Configuracion) -> Configuracion) {
            configuracion.value = cambio(configuracion.value)
        }
    }

    private val shimano = Desarrollos.SHIMANO_105
    private fun s(plato: Int, pinon: Int) = EstadoMarcha.PlatoPinon(plato, pinon)
    private val dispatcher = StandardTestDispatcher()

    @BeforeEach
    fun antes() = Dispatchers.setMain(dispatcher)

    @AfterEach
    fun despues() = Dispatchers.resetMain()

    @Test
    fun valor_por_defecto() {
        // Estado `lineal` de la maqueta.
        val e = estadoDeMarchas(Configuracion())
        assertEquals(listOf(Transmision.Lineal(), shimano), e.transmisiones)
        assertEquals(Transmision.Lineal(), e.transmision)
        assertEquals(EstadoMarcha.Numerada(12, 24), e.referencia)
        assertTrue(e.recordarUltimaMarcha)
    }

    @Test
    fun shimano_automatico_y_manual() {
        val e = estadoDeMarchas(Configuracion(transmision = shimano.id))
        assertEquals(shimano, e.transmision)
        assertEquals(ModoCambio.AUTOMATICO, e.modo)
        assertEquals(s(36, 17), e.referencia)
        assertEquals(ModoCambio.MANUAL, estadoDeMarchas(Configuracion(transmision = shimano.id).conModo(ModoCambio.MANUAL)).modo)
    }

    @Test
    fun referencia_siguiente() {
        // Spec `settings-screen`, «Referencia lineal», «Referencia con la Shimano» y «Extremo».
        assertEquals(EstadoMarcha.Numerada(13, 24), referenciaSiguiente(Transmision.Lineal(), EstadoMarcha.Numerada(12, 24), dura = true))
        assertEquals(s(52, 24), referenciaSiguiente(shimano, s(36, 17), dura = true))
        assertEquals(s(36, 17), referenciaSiguiente(shimano, s(52, 24), dura = false))
        assertEquals(null, referenciaSiguiente(shimano, s(52, 11), dura = true))
        val extremo = estadoDeMarchas(Configuracion(transmision = shimano.id).conReferencia(s(52, 11)))
        assertFalse(extremo.puedeEndurecer)
        assertTrue(extremo.puedeAligerar)
        assertFalse(estadoDeMarchas(Configuracion(lineal = AjustesLineal(referencia = 1))).puedeAligerar)
    }

    @Test
    fun elegir_transmision_modo_referencia_y_recordar() = runTest(dispatcher) {
        val ajustes = AjustesFalsos()
        val vm = MarchasViewModel(ajustes)
        backgroundScope.launch { vm.estado.collect {} }
        vm.transmision(shimano.id)
        runCurrent()
        assertEquals(shimano, vm.estado.value.transmision)
        vm.modo(ModoCambio.MANUAL)
        vm.referencia(dura = true)
        vm.recordarUltimaMarcha(false)
        runCurrent()
        val c = ajustes.configuracion.value
        assertEquals(ModoCambio.MANUAL, c.modo)
        assertEquals(s(52, 24), c.referencia)
        assertFalse(c.recordarUltimaMarcha)
        // La lineal no cambia.
        vm.transmision(Transmision.Lineal.ID)
        runCurrent()
        assertEquals(EstadoMarcha.Numerada(12, 24), vm.estado.value.referencia)
    }
}
