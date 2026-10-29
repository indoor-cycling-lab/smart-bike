package app.smartbike.ui.diagnostico

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.smartbike.SmartBikeApp
import app.smartbike.datos.EntradaRegistro
import app.smartbike.datos.RegistroSesion
import app.smartbike.dominio.ComandoRodillo
import app.smartbike.sesion.Sesion
import app.smartbike.ui.NOMBRE_APP_ENTRENAMIENTO
import app.smartbike.ui.componentes.FilaHex
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DiagnosticoViewModel(
    private val sesion: Sesion,
    private val registro: RegistroSesion,
    /** El nombre Bluetooth del móvil, con el que se elige el mando en Zwift. */
    private val nombreMovil: () -> String? = { null },
) : ViewModel() {

    private val control = MutableStateFlow(ControlManual())
    private val pausado = MutableStateFlow(false)

    /** Entrada más reciente al tocar «Limpiar»: el volcado solo muestra las posteriores. */
    private val limpiadoHasta = MutableStateFlow<EntradaRegistro?>(null)

    private val trafico = MutableStateFlow(emptyList<FilaHex>())

    /** «Tráfico en directo»: desmarcada al abrir Diagnóstico; sobrevive al giro, no a salir de la pantalla. */
    private val traficoVisible = MutableStateFlow(false)

    init {
        // Las filas se forman fuera del hilo principal para no restarle fluidez a la pantalla.
        viewModelScope.launch {
            filasDelVolcado(registro, traficoVisible, pausado, limpiadoHasta)
                .flowOn(Dispatchers.Default)
                .collect { trafico.value = it }
        }
    }

    val estado: StateFlow<DiagnosticoUiState> =
        // Con el nombre Bluetooth en el combine, «Nombre anunciado» se vuelve a leer al renombrarse o recuperarse el móvil.
        combine(sesion.estado, control, trafico, combine(pausado, traficoVisible, sesion.nombreBluetooth) { p, v, _ -> p to v }) { c, ctl, filas, (p, v) ->
            estadoDePantalla(c, ctl, filas, p, v, nombreMovil())
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DiagnosticoUiState())

    fun pendiente(delta: Int) = control.update { it.pendiente(delta) }

    fun potencia(delta: Int) = control.update { it.potencia(delta) }

    fun enviarPendiente() = sesion.controlManual(ComandoRodillo.Pendiente(control.value.pendiente))

    fun enviarPotencia() = sesion.controlManual(ComandoRodillo.Potencia(control.value.potencia))

    fun alternarPausa() = pausado.update { !it }

    /** Al ocultar el tráfico se anula la pausa: al volver a mostrarlo, se ve en directo. */
    fun alternarTrafico() {
        if (traficoVisible.value) pausado.value = false
        traficoVisible.update { !it }
    }

    fun limpiar() {
        limpiadoHasta.value = registro.ultimas(1).firstOrNull()
        trafico.value = emptyList()
    }

    /** Al salir de Diagnóstico (no al girar el móvil), el Neo vuelve a la marcha (spec `trainer-manual-control`). */
    override fun onCleared() = sesion.salirDiagnostico()

    companion object {
        const val FILAS_TRAFICO = 200
        const val PERIODO_TRAFICO_MS = 100L

        val Factory = viewModelFactory {
            initializer {
                val graph = (this[APPLICATION_KEY] as SmartBikeApp).graph
                DiagnosticoViewModel(graph.iniciarSesion(), graph.registro, graph::nombreBluetooth)
            }
        }
    }
}

/**
 * Filas del volcado. Con el tráfico oculto, una lista vacía, sin suscribirse al registro ni formar filas. Visible, las
 * últimas [DiagnosticoViewModel.FILAS_TRAFICO] entradas posteriores a [limpiadoHasta], como mucho una vez cada
 * [periodoMs] (design.md, decisión 6); la primera, al mostrarlo, ya trae las llegadas mientras estaba oculto. En pausa,
 * la lista no cambia.
 */
@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
internal fun filasDelVolcado(
    registro: RegistroSesion,
    visible: Flow<Boolean>,
    pausado: Flow<Boolean>,
    limpiadoHasta: Flow<EntradaRegistro?>,
    periodoMs: Long = DiagnosticoViewModel.PERIODO_TRAFICO_MS,
    nombrePlataforma: Flow<String> = flowOf(NOMBRE_APP_ENTRENAMIENTO),
): Flow<List<FilaHex>> = visible.distinctUntilChanged().flatMapLatest { v ->
    if (!v) {
        flowOf(emptyList())
    } else {
        combine(registro.cambios.sample(periodoMs).onStart { emit(0) }, pausado, limpiadoHasta, nombrePlataforma) { _, p, hasta, nombre -> Triple(p, hasta, nombre) }
            .filterNot { (p, _, _) -> p }
            .map { (_, hasta, nombre) ->
                filasTrafico(registro.ultimas(DiagnosticoViewModel.FILAS_TRAFICO).takeWhile { it !== hasta }, nombrePlataforma = nombre)
            }
    }
}
