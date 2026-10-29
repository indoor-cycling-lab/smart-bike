package app.smartbike.ui.ajustes

import app.smartbike.datos.AlmacenConfiguracion
import app.smartbike.datos.EstadoGrabacion
import app.smartbike.datos.ResumenSesiones
import app.smartbike.dominio.Configuracion
import app.smartbike.dominio.ConfiguracionVentilador
import app.smartbike.dominio.Desarrollos
import app.smartbike.dominio.DispositivoConfigurado
import app.smartbike.dominio.EstadoMarcha
import app.smartbike.dominio.EstadoVentilador
import app.smartbike.dominio.ModoBajadas
import app.smartbike.dominio.ModoCambio
import app.smartbike.dominio.ModoConexion
import app.smartbike.dominio.ModoVentilador
import app.smartbike.dominio.OrientacionApp
import app.smartbike.dominio.TemaApp
import app.smartbike.sesion.EstadoDispositivo
import app.smartbike.sesion.EstadoRodaje
import app.smartbike.sesion.Simulacion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/** Escenarios de la spec `settings-screen` sobre el estado de Ajustes. */
@OptIn(ExperimentalCoroutinesApi::class)
class AjustesUiStateTest {

    private class AjustesFalsos : AlmacenConfiguracion {
        override val configuracion = MutableStateFlow(Configuracion())
        override suspend fun actualizarConfiguracion(cambio: (Configuracion) -> Configuracion) {
            configuracion.value = cambio(configuracion.value)
        }
    }

    private val dispatcher = StandardTestDispatcher()

    @BeforeEach
    fun antes() = Dispatchers.setMain(dispatcher)

    @AfterEach
    fun despues() = Dispatchers.resetMain()

    @Test
    fun valor_por_defecto() = runTest(dispatcher) {
        val vm = AjustesViewModel(AjustesFalsos())
        backgroundScope.launch { vm.estado.collect {} }
        runCurrent()
        assertEquals(ModoBajadas.LA_MARCHA_NO_CREA_BAJADAS, vm.estado.value.bajadas)
    }

    @Test
    fun orientacion_por_defecto_y_elegida() = runTest(dispatcher) {
        // Spec `settings-screen`, «Orientación en Ajustes»: «Según el móvil» por defecto; al elegir «Horizontal», se guarda.
        val ajustes = AjustesFalsos()
        val vm = AjustesViewModel(ajustes)
        backgroundScope.launch { vm.estado.collect {} }
        runCurrent()
        assertEquals(OrientacionApp.SEGUN_MOVIL, vm.estado.value.orientacion)
        vm.orientacion(OrientacionApp.HORIZONTAL)
        runCurrent()
        assertEquals(OrientacionApp.HORIZONTAL, ajustes.configuracion.value.orientacion)
        assertEquals(OrientacionApp.HORIZONTAL, vm.estado.value.orientacion)
    }

    @Test
    fun tema_por_defecto_y_elegido() = runTest(dispatcher) {
        // Spec `settings-screen`, «Tema en Ajustes»: «Oscuro» por defecto; al elegir «Claro», se guarda.
        val ajustes = AjustesFalsos()
        val vm = AjustesViewModel(ajustes)
        backgroundScope.launch { vm.estado.collect {} }
        runCurrent()
        assertEquals(TemaApp.OSCURO, vm.estado.value.tema)
        vm.tema(TemaApp.CLARO)
        runCurrent()
        assertEquals(TemaApp.CLARO, ajustes.configuracion.value.tema)
        assertEquals(TemaApp.CLARO, vm.estado.value.tema)
    }

    @Test
    fun elegir_otra_opcion_la_guarda() = runTest(dispatcher) {
        val ajustes = AjustesFalsos()
        val vm = AjustesViewModel(ajustes)
        backgroundScope.launch { vm.estado.collect {} }
        vm.bajadas(ModoBajadas.NUNCA_EMPUJAR)
        runCurrent()
        assertEquals(ModoBajadas.NUNCA_EMPUJAR, ajustes.configuracion.value.bajadas)
        assertEquals(ModoBajadas.NUNCA_EMPUJAR, vm.estado.value.bajadas)
    }

    @Test
    fun entrada_de_marchas_con_la_shimano() = runTest(dispatcher) {
        val ajustes = AjustesFalsos()
        ajustes.configuracion.value = Configuracion(transmision = Desarrollos.SHIMANO_105.id)
        val vm = AjustesViewModel(ajustes)
        backgroundScope.launch { vm.estado.collect {} }
        runCurrent()
        assertEquals(Desarrollos.SHIMANO_105, vm.estado.value.transmision)
        assertEquals(ModoCambio.AUTOMATICO, vm.estado.value.modoCambio)
    }

    @Test
    fun valores_por_defecto_de_al_rodar_y_ciclista() {
        // Spec `settings-screen`, «Valores por defecto».
        val e = estadoDeAjustes(Configuracion())
        assertEquals(Configuracion.DATOS_POR_DEFECTO, e.datos)
        assertEquals(false, e.botonesEnPantalla)
        assertEquals(false, e.modoTactil)
        assertEquals(true, e.mostrarAccion)
        assertEquals(true, e.vibracion)
        assertEquals(75, e.pesoCiclistaKg)
    }

    @Test
    fun casillas_y_peso_se_guardan() = runTest(dispatcher) {
        val ajustes = AjustesFalsos()
        val vm = AjustesViewModel(ajustes)
        backgroundScope.launch { vm.estado.collect {} }
        vm.botonesEnPantalla(true)
        vm.modoTactil(true)
        vm.mostrarAccion(false)
        vm.vibracion(false)
        vm.peso(1)
        runCurrent()
        val c = ajustes.configuracion.value
        assertEquals(true, c.botonesEnPantalla)
        assertEquals(true, c.modoTactil)
        assertEquals(false, c.mostrarAccion)
        assertEquals(false, c.vibracion)
        // «Cambiar el peso»: de 75 a 76 kg.
        assertEquals(76, c.pesoCiclistaKg)
    }

    @Test
    fun extremos_del_peso() {
        // Spec `settings-screen`, «Extremo».
        val arriba = AjustesUiState(pesoCiclistaKg = 150)
        assertEquals(false, arriba.puedeSubirPeso)
        assertEquals(true, arriba.puedeBajarPeso)
        val abajo = AjustesUiState(pesoCiclistaKg = 40)
        assertEquals(false, abajo.puedeBajarPeso)
    }

    // --- Ventilador (spec `settings-screen`, «Ventilador en Ajustes»; change `ventilador-headwind`) ---

    private fun rodajeConVentilador(e: EstadoDispositivo, estado: EstadoVentilador?, configurado: Boolean = true) = EstadoRodaje(marcha = EstadoMarcha.Numerada(1, 24), referencia = EstadoMarcha.Numerada(1, 24), simulacion = Simulacion(0.0, 0.0, null), ventilador = e, ventiladorEstado = estado, ventiladorConfigurado = DispositivoConfigurado("DD:DD", "HEADWIND 4F2A").takeIf { configurado })

    private fun config(vararg u: Int) = ConfiguracionVentilador.desde(ByteArray(9) { if (it < 4) u[it].toByte() else 7 })!!

    private val manual40 get() = EstadoVentilador(ModoVentilador.Manual, 40, 40, config(120, 135, 150, 165))

    @Test
    fun sin_ventilador_configurado_no_hay_seccion() {
        assertNull(ventiladorDeAjustes(rodajeConVentilador(EstadoDispositivo.Conectado, manual40, configurado = false)))
        assertNull(estadoDeAjustes(Configuracion()).ventilador)
    }

    @Test
    fun con_ventilador_conectado_en_manual_todo_se_puede_cambiar() {
        val v = checkNotNull(ventiladorDeAjustes(rodajeConVentilador(EstadoDispositivo.Conectado, manual40)))
        assertEquals(ModoVentilador.Manual, v.modo)
        assertEquals(40, v.velocidad)
        assertEquals(listOf(120, 135, 150, 165), v.umbrales)
        assertTrue(v.puedeCambiarModo && v.puedeBajarVelocidad && v.puedeSubirVelocidad)
        assertTrue((1..4).all { v.puedeBajarUmbral(it) && v.puedeSubirUmbral(it) })
    }

    @Test
    fun en_automatico_la_velocidad_esta_deshabilitada() {
        val v = checkNotNull(ventiladorDeAjustes(rodajeConVentilador(EstadoDispositivo.Conectado, manual40.copy(modo = ModoVentilador.Pulso, velocidad = 25))))
        assertFalse(v.puedeBajarVelocidad || v.puedeSubirVelocidad)
        // Se ve la última velocidad manual, no la que el ventilador decide.
        assertEquals(40, v.velocidad)
        assertTrue(v.puedeBajarUmbral(1))
    }

    @Test
    fun la_velocidad_en_los_extremos_deshabilita_solo_ese_lado() {
        val alto = checkNotNull(ventiladorDeAjustes(rodajeConVentilador(EstadoDispositivo.Conectado, manual40.copy(velocidad = 100, velocidadManual = 100))))
        assertFalse(alto.puedeSubirVelocidad)
        assertTrue(alto.puedeBajarVelocidad)
        val bajo = checkNotNull(ventiladorDeAjustes(rodajeConVentilador(EstadoDispositivo.Conectado, manual40.copy(velocidad = 0, velocidadManual = 0))))
        assertFalse(bajo.puedeBajarVelocidad)
    }

    @Test
    fun los_umbrales_pegados_deshabilitan_los_botones_del_limite_entre_zonas() {
        val estado = manual40.copy(configuracion = config(120, 121, 122, 220))
        val v = checkNotNull(ventiladorDeAjustes(rodajeConVentilador(EstadoDispositivo.Conectado, estado)))
        assertFalse(v.puedeSubirUmbral(1))
        assertFalse(v.puedeBajarUmbral(2))
        assertFalse(v.puedeSubirUmbral(2))
        assertFalse(v.puedeBajarUmbral(3))
        assertFalse(v.puedeSubirUmbral(4))
        assertTrue(v.puedeBajarUmbral(1))
        assertTrue(v.puedeBajarUmbral(4))
    }

    @Test
    fun sin_conexion_se_ve_lo_ultimo_con_todo_deshabilitado() {
        val v = checkNotNull(ventiladorDeAjustes(rodajeConVentilador(EstadoDispositivo.Reconectando, manual40)))
        assertFalse(v.conectado)
        assertEquals(40, v.velocidad)
        assertFalse(v.puedeCambiarModo || v.puedeBajarVelocidad || v.puedeSubirVelocidad)
        assertTrue((1..4).none { v.puedeBajarUmbral(it) || v.puedeSubirUmbral(it) })
    }

    @Test
    fun otro_modo_se_dice_y_no_deja_cambiar_la_velocidad() {
        val v = checkNotNull(ventiladorDeAjustes(rodajeConVentilador(EstadoDispositivo.Conectado, manual40.copy(modo = ModoVentilador.Otro))))
        assertEquals(ModoVentilador.Otro, v.modo)
        assertFalse(v.puedeBajarVelocidad)
    }

    @Test
    fun depuracion_por_defecto() = runTest(dispatcher) {
        // Spec `settings-screen`, «Valores por defecto»: grabar apagado, mínimo 10 % y ninguna sesión.
        val vm = AjustesViewModel(AjustesFalsos())
        backgroundScope.launch { vm.estado.collect {} }
        runCurrent()
        val e = vm.estado.value
        assertFalse(e.grabarSesiones)
        assertEquals(10, e.espacioLibreMinimoPct)
        assertEquals(EstadoGrabacion.Apagada, e.grabacion)
        assertEquals(ResumenSesiones(0, 0), e.sesiones)
    }

    @Test
    fun activar_la_grabacion_y_subir_el_minimo_se_guarda() = runTest(dispatcher) {
        // «Activar y guardar»: se activa, se sube el mínimo a 20 % y se reabre.
        val ajustes = AjustesFalsos()
        val vm = AjustesViewModel(ajustes)
        backgroundScope.launch { vm.estado.collect {} }
        vm.grabarSesiones(true)
        vm.espacioLibreMinimo(2)
        runCurrent()
        assertTrue(ajustes.configuracion.value.grabarSesiones)
        assertEquals(20, ajustes.configuracion.value.espacioLibreMinimoPct)
        assertEquals(20, vm.estado.value.espacioLibreMinimoPct)
    }

    @Test
    fun el_minimo_no_pasa_de_sus_limites() = runTest(dispatcher) {
        val ajustes = AjustesFalsos()
        val vm = AjustesViewModel(ajustes)
        backgroundScope.launch { vm.estado.collect {} }
        vm.espacioLibreMinimo(100)
        runCurrent()
        assertEquals(50, vm.estado.value.espacioLibreMinimoPct)
        assertFalse(vm.estado.value.puedeSubirEspacioLibre)
        assertTrue(vm.estado.value.puedeBajarEspacioLibre)
        vm.espacioLibreMinimo(-100)
        runCurrent()
        assertEquals(5, vm.estado.value.espacioLibreMinimoPct)
        assertTrue(vm.estado.value.puedeSubirEspacioLibre)
        assertFalse(vm.estado.value.puedeBajarEspacioLibre)
    }

    @Test
    fun el_estado_de_la_grabacion_y_el_resumen_llegan_a_la_pantalla() = runTest(dispatcher) {
        val grabacion = MutableStateFlow<EstadoGrabacion>(EstadoGrabacion.Grabando("sesion-20261029-184207.txt"))
        val vm = AjustesViewModel(AjustesFalsos(), grabacion = grabacion, resumenSesiones = flowOf(ResumenSesiones(3, 41_000)))
        backgroundScope.launch { vm.estado.collect {} }
        runCurrent()
        assertEquals(EstadoGrabacion.Grabando("sesion-20261029-184207.txt"), vm.estado.value.grabacion)
        assertEquals(ResumenSesiones(3, 41_000), vm.estado.value.sesiones)
        grabacion.value = EstadoGrabacion.DetenidaPorEspacio("sesion-20261029-184207.txt")
        runCurrent()
        assertEquals(EstadoGrabacion.DetenidaPorEspacio("sesion-20261029-184207.txt"), vm.estado.value.grabacion)
    }

    @Test
    fun el_paso_del_minimo_es_de_cinco() {
        assertEquals(15, espacioLibreMinimoMas(10, 1))
        assertEquals(5, espacioLibreMinimoMas(10, -1))
        assertEquals(50, espacioLibreMinimoMas(50, 1))
        assertEquals(5, espacioLibreMinimoMas(5, -1))
    }
}
