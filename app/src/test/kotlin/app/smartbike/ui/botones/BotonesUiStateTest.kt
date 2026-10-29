package app.smartbike.ui.botones

import app.smartbike.datos.AlmacenConfiguracion
import app.smartbike.dominio.Accion
import app.smartbike.dominio.AjustesConexion
import app.smartbike.dominio.Asignacion
import app.smartbike.dominio.Configuracion
import app.smartbike.dominio.ConfiguracionBotones
import app.smartbike.dominio.Desarrollos
import app.smartbike.dominio.Entrada
import app.smartbike.dominio.FuncionZwift
import app.smartbike.dominio.ModoConexion
import app.smartbike.dominio.Regla
import app.smartbike.dominio.TiemposGestos
import app.smartbike.dominio.TipoGesto
import app.smartbike.dominio.Transmision
import app.smartbike.protocolo.SquareButton
import app.smartbike.sesion.EstadoCaptura
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

@OptIn(ExperimentalCoroutinesApi::class)
class BotonesUiStateTest {

    private val defecto = Configuracion()

    @Test
    fun lista_por_defecto() {
        val e = estadoDeBotones(defecto, confirmandoRestaurar = false)
        assertEquals(Accion.ASIGNABLES, e.acciones.map { it.accion })
        assertEquals(
            listOf(Entrada(SquareButton.PalancaDerecha1), Entrada(SquareButton.PalancaDerecha2)),
            e.acciones.first().reglas.map { it.entrada },
        )
        // Spec `button-mapping`, «ERG por defecto»: △ larga alterna y «+10 W» y «−10 W» quedan sin botón.
        val alternar = e.acciones.single { it.accion == Accion.AlternarErgManual }
        assertEquals(listOf(Entrada(SquareButton.Triangulo)), alternar.reglas.map { it.entrada })
        assertEquals(listOf(TipoGesto.Larga), alternar.reglas.map { it.gesto })
        // Spec `button-mapping`, «Pantalla por defecto»: al final, con Campag. I + cruceta.
        assertEquals(listOf(Accion.PantallaAnterior, Accion.PantallaSiguiente, Accion.SiguienteModelo), e.acciones.takeLast(6).take(3).map { it.accion })
        assertEquals(listOf(Entrada(SquareButton.CrucetaDerecha, SquareButton.CampagnoloIzquierdo)), e.acciones.single { it.accion == Accion.PantallaSiguiente }.reglas.map { it.entrada })
        // Spec `button-mapping`, «Acción de modelo»: sin botón por defecto.
        assertTrue(e.acciones.single { it.accion == Accion.SiguienteModelo }.reglas.isEmpty())
        // Spec `button-mapping`, «Acciones del ventilador»: tras «Siguiente modelo», las tres sin botón.
        assertEquals(Accion.VENTILADOR, e.acciones.takeLast(3).map { it.accion })
        assertTrue(e.acciones.takeLast(3).all { it.reglas.isEmpty() })
        assertTrue(e.acciones.single { it.accion == Accion.ErgMas }.reglas.isEmpty())
        assertTrue(e.acciones.single { it.accion == Accion.ErgMenos }.reglas.isEmpty())
        assertFalse(e.invertirPalancas)
        assertTrue(e.autorrepeticion)
        assertFalse(e.confirmandoRestaurar)
    }

    @Test
    fun funciones_de_zwift_por_defecto() {
        // Spec `button-mapping`, «Zwift por defecto» y «Funciones de Zwift».
        for (c in listOf(defecto, defecto.copy(transmision = Desarrollos.SHIMANO_105.id))) {
            val e = estadoDeBotones(c, false)
            assertEquals(MandoBotones.Aparte, e.mando)
            assertEquals(Accion.ZWIFT, e.funcionesZwift.map { it.accion })
            e.funcionesZwift.forEach { f ->
                val funcion = (f.accion as Accion.Mando).funcion as FuncionZwift
                assertEquals(listOf(Entrada(funcion.boton)), f.reglas.map { it.entrada }, "$funcion")
                assertEquals(listOf(TipoGesto.Pulsar), f.reglas.map { it.gesto })
            }
            assertTrue(e.acciones.none { it.accion is Accion.Mando })
        }
    }

    @Test
    fun funcion_de_zwift_sin_boton_tras_quitarla() {
        val a = Asignacion.POR_DEFECTO.quitar(Entrada(SquareButton.Y), TipoGesto.Pulsar)
        val e = estadoDeBotones(defecto.conAsignacion(a), false)
        assertTrue(e.funcionesZwift.single { it.accion == Accion.Mando(FuncionZwift.Y) }.reglas.isEmpty())
    }

    @Test
    fun sin_square_configurado() {
        // Spec `button-mapping`, «Sin el mando de Zwift»: las filas se mantienen.
        val e = estadoDeBotones(defecto, false, squareConfigurado = false)
        assertEquals(MandoBotones.SinSquare, e.mando)
        assertEquals(Accion.ZWIFT, e.funcionesZwift.map { it.accion })
    }

    @Test
    fun el_mando_va_dentro_con_solo_bluetooth_y_solo_wifi() {
        // Spec `button-mapping`, «Mando de Zwift en Zwift Ride para la asignación».
        for (m in listOf(ModoConexion.FTMS_BLUETOOTH, ModoConexion.ZWIFT_RIDE)) {
            assertEquals(MandoBotones.Dentro, estadoDeBotones(defecto.copy(conexion = AjustesConexion(m)), false).mando, m.name)
        }
        assertEquals(MandoBotones.Aparte, estadoDeBotones(defecto.copy(conexion = AjustesConexion(ModoConexion.FTMS_WIFI)), false).mando)
    }

    @Test
    fun acciones_con_la_shimano() {
        // Spec `button-mapping`, «Acciones de plato con la Shimano» y «Shimano por defecto».
        val e = estadoDeBotones(defecto.copy(transmision = Desarrollos.SHIMANO_105.id), confirmandoRestaurar = false)
        assertEquals(Desarrollos.SHIMANO_105, e.transmision)
        assertEquals(Accion.asignables(Desarrollos.SHIMANO_105) - Accion.ZWIFT.toSet(), e.acciones.map { it.accion })
        assertEquals(listOf(Entrada(SquareButton.PalancaDerecha2)), e.acciones.single { it.accion == Accion.MarchaMas(1) }.reglas.map { it.entrada })
        assertEquals(listOf(Entrada(SquareButton.PalancaIzquierda1)), e.acciones.single { it.accion == Accion.PlatoMas }.reglas.map { it.entrada })
        // Con la lineal, sin acciones de plato.
        assertTrue(estadoDeBotones(defecto, false).acciones.none { it.accion == Accion.PlatoMas || it.accion == Accion.PlatoMenos })
    }

    @Test
    fun modificada() {
        // Estado `modificada` de la maqueta: más ligera sin botón, una combinación nueva y palancas invertidas.
        val a = Asignacion.POR_DEFECTO
            .quitar(Entrada(SquareButton.PalancaIzquierda2, SquareButton.CampagnoloIzquierdo), TipoGesto.Pulsar)
            .asignar(Entrada(SquareButton.Triangulo, SquareButton.CampagnoloDerecho), TipoGesto.Corta, Accion.MarchaMenos(3))
        val e = estadoDeBotones(defecto.conAsignacion(a), false)
        assertTrue(e.acciones.single { it.accion == Accion.MarchaMasLigera }.reglas.isEmpty())
        assertEquals(2, e.acciones.single { it.accion == Accion.MarchaMenos(3) }.reglas.size)
        val inv = estadoDeBotones(defecto.conAsignacion(a.copy(invertirPalancas = true)), false)
        assertTrue(inv.invertirPalancas)
        // Con las palancas invertidas, cada acción enseña los botones físicos que la hacen.
        assertEquals(
            listOf(Entrada(SquareButton.PalancaIzquierda1), Entrada(SquareButton.PalancaIzquierda2)),
            inv.acciones.first().reglas.map { it.entrada },
        )
    }

    @Test
    fun limites_de_los_tiempos() {
        val e = estadoDeBotones(defecto.copy(botones = ConfiguracionBotones(TiemposGestos(dobleMs = 600, ritmoRepeticionMs = 100))), false)
        assertFalse(e.puedeSubir(Tiempo.Doble))
        assertTrue(e.puedeBajar(Tiempo.Doble))
        assertFalse(e.puedeBajar(Tiempo.Ritmo))
        assertEquals(600, Tiempo.Doble.cambiar(e.tiempos, 1).dobleMs)
        assertEquals(850, Tiempo.Larga.cambiar(TiemposGestos(), 1).largaMs)
        assertEquals(1000, Tiempo.Retardo.cambiar(TiemposGestos(), 10).retardoRepeticionMs)
    }

    // --- ViewModel ----------------------------------------------------------------------------------------------

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
    fun restaurar_con_confirmacion() = runTest(dispatcher) {
        val ajustes = AjustesFalsos()
        val vm = BotonesViewModel(ajustes)
        backgroundScope.launch { vm.estado.collect {} }
        vm.invertirPalancas(true)
        vm.tiempo(Tiempo.Ritmo, 3)
        vm.autorrepeticion(false)
        runCurrent()
        assertEquals(400, ajustes.configuracion.value.botones.tiempos.ritmoRepeticionMs)
        assertTrue(vm.estado.value.invertirPalancas)

        vm.pedirRestaurar()
        runCurrent()
        assertTrue(vm.estado.value.confirmandoRestaurar)
        vm.cancelarRestaurar()
        runCurrent()
        assertFalse(vm.estado.value.confirmandoRestaurar)
        assertTrue(ajustes.configuracion.value.asignacion.invertirPalancas)

        vm.pedirRestaurar()
        vm.restaurar()
        runCurrent()
        assertEquals(ConfiguracionBotones(), ajustes.configuracion.value.botones)
        assertFalse(vm.estado.value.confirmandoRestaurar)
    }

    @Test
    fun restaurar_solo_la_elegida() = runTest(dispatcher) {
        // Spec `button-mapping`, «Restaurar solo la elegida».
        val ajustes = AjustesFalsos()
        val vm = BotonesViewModel(ajustes)
        backgroundScope.launch { vm.estado.collect {} }
        vm.invertirPalancas(true)
        runCurrent()
        ajustes.configuracion.value = ajustes.configuracion.value.copy(transmision = Desarrollos.SHIMANO_105.id)
        vm.invertirPalancas(true)
        runCurrent()
        vm.restaurar()
        runCurrent()
        assertEquals(Asignacion.POR_DEFECTO_PLATOS, ajustes.configuracion.value.asignacion)
        assertTrue(ajustes.configuracion.value.asignacion(Transmision.Lineal()).invertirPalancas)
    }

    // --- Asignar acción -----------------------------------------------------------------------------------------

    private val a = Asignacion.POR_DEFECTO
    private val menos3 = Accion.MarchaMenos(3)

    private fun capturada(b: SquareButton, m: SquareButton? = null) = EstadoCaptura.Capturada(Entrada(b, m))

    @Test
    fun esperando_y_sin_square_no_dejan_guardar() {
        val e = estadoDeAsignar(menos3, a, emptyList(), EstadoCaptura.Esperando, TipoGesto.Pulsar)
        assertEquals(listOf(Regla(Entrada(SquareButton.PalancaIzquierda1, SquareButton.CampagnoloIzquierdo), TipoGesto.Pulsar, menos3)), e.reglas)
        assertFalse(e.puedeGuardar)
        assertTrue(e.avisos.isEmpty())
        assertFalse(estadoDeAsignar(menos3, a, emptyList(), EstadoCaptura.SinSquare, TipoGesto.Pulsar).puedeGuardar)
    }

    @Test
    fun combinacion_libre() {
        val e = estadoDeAsignar(menos3, a, emptyList(), capturada(SquareButton.Triangulo, SquareButton.CampagnoloDerecho), TipoGesto.Corta)
        assertEquals(listOf(AvisoAsignar.Libre(Entrada(SquareButton.Triangulo, SquareButton.CampagnoloDerecho), TipoGesto.Corta)), e.avisos)
        assertTrue(e.puedeGuardar)
        val guardada = asignacionAlGuardar(a, menos3, emptyList(), e.capturada, e.gesto)
        assertEquals(menos3, guardada.accion(Entrada(SquareButton.Triangulo, SquareButton.CampagnoloDerecho), TipoGesto.Corta))
    }

    @Test
    fun entrada_en_uso() {
        val e = estadoDeAsignar(menos3, a, emptyList(), capturada(SquareButton.PalancaDerecha1), TipoGesto.Pulsar)
        assertEquals(listOf(AvisoAsignar.EnUso(listOf(Regla(Entrada(SquareButton.PalancaDerecha1), TipoGesto.Pulsar, Accion.MarchaMas(1))))), e.avisos)
        val guardada = asignacionAlGuardar(a, menos3, emptyList(), e.capturada, e.gesto)
        assertEquals(menos3, guardada.accion(Entrada(SquareButton.PalancaDerecha1), TipoGesto.Pulsar))
    }

    @Test
    fun boton_de_zwift_libre_sin_aviso_de_zwift() {
        // Sin interruptor del mando, A libre solo dice que está libre (spec `button-mapping`).
        val sinA = a.quitar(Entrada(SquareButton.A), TipoGesto.Pulsar)
        val e = estadoDeAsignar(menos3, sinA, emptyList(), capturada(SquareButton.A), TipoGesto.Pulsar)
        assertEquals(listOf(AvisoAsignar.Libre(Entrada(SquareButton.A), TipoGesto.Pulsar)), e.avisos)
    }

    @Test
    fun campagnolo_solo_no_admite_pulsar() {
        val e = estadoDeAsignar(menos3, a, emptyList(), capturada(SquareButton.CampagnoloIzquierdo), TipoGesto.Corta)
        assertEquals(setOf(TipoGesto.Pulsar), e.gestosDeshabilitados)
        assertTrue(estadoDeAsignar(menos3, a, emptyList(), capturada(SquareButton.Triangulo), TipoGesto.Corta).gestosDeshabilitados.isEmpty())
    }

    @Test
    fun quitar_una_palanca_y_guardar() {
        val mas1 = Accion.MarchaMas(1)
        val d2 = Regla(Entrada(SquareButton.PalancaDerecha2), TipoGesto.Pulsar, mas1)
        val e = estadoDeAsignar(mas1, a, listOf(d2), EstadoCaptura.SinSquare, TipoGesto.Pulsar)
        assertEquals(listOf(Entrada(SquareButton.PalancaDerecha1)), e.reglas.map { it.entrada })
        assertTrue(e.puedeGuardar)
        val guardada = asignacionAlGuardar(a, mas1, listOf(d2), null, TipoGesto.Pulsar)
        assertEquals(null, guardada.accion(Entrada(SquareButton.PalancaDerecha2), TipoGesto.Pulsar))
        assertEquals(mas1, guardada.accion(Entrada(SquareButton.PalancaDerecha1), TipoGesto.Pulsar))
    }

    @Test
    fun ya_asignada_a_esta_accion() {
        val e = estadoDeAsignar(Accion.MarchaMas(1), a, emptyList(), capturada(SquareButton.PalancaDerecha1), TipoGesto.Pulsar)
        assertEquals(listOf(AvisoAsignar.YaAsignada(Entrada(SquareButton.PalancaDerecha1), TipoGesto.Pulsar)), e.avisos)
    }
}
