package app.smartbike.ui.rodaje

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.smartbike.AppGraph
import app.smartbike.SmartBikeApp
import app.smartbike.dominio.Accion
import app.smartbike.dominio.Configuracion
import app.smartbike.dominio.EstadoMarcha
import app.smartbike.dominio.ResultadoAjuste
import app.smartbike.dominio.TopeMarcha
import app.smartbike.sesion.EventoRodaje
import app.smartbike.sesion.Sesion
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RodajeViewModel(
    private val sesion: Sesion,
    private val terminarSesion: () -> Unit,
    private val configuracion: Flow<Configuracion> = flowOf(Configuracion()),
    private val nombreMovil: () -> String? = { null },
) : ViewModel() {

    private val tope = MutableStateFlow<TopeMarcha?>(null)
    private val aviso = MutableStateFlow<AvisoUi?>(null)
    private var borrarTope: Job? = null
    private var borrarAviso: Job? = null

    init {
        // El aviso y el tope son eventos, no estado: duran 1,5 s y uno nuevo sustituye al anterior (arquitectura.md § 9.2).
        viewModelScope.launch {
            sesion.eventos.collect { e ->
                when (e) {
                    is EventoRodaje.Cambio -> avisar(
                        if (e.cambio.cambiaPlato) {
                            AvisoUi.Plato((e.cambio.nueva as EstadoMarcha.PlatoPinon).plato)
                        } else {
                            AvisoUi.Marcha(e.accion, e.cambio.pasos)
                        },
                    )

                    is EventoRodaje.Tope -> {
                        aviso.value = null
                        tope.value = e.tope
                        borrarTope?.cancel()
                        borrarTope = launch {
                            delay(DURACION_AVISO_MS)
                            tope.value = null
                        }
                    }

                    is EventoRodaje.Ajuste -> avisar(
                        when (val r = e.resultado) {
                            is ResultadoAjuste.Cambiado -> AvisoUi.Vatios(r.diferenciaW)
                            else -> e.entradaW?.let { AvisoUi.VueltaA(it) } ?: AvisoUi.PotenciaZwift
                        },
                    )

                    is EventoRodaje.TopePotencia -> avisar(AvisoUi.TopePotencia(e.potenciaW))

                    is EventoRodaje.ModoErg -> avisar(AvisoUi.ModoErg(e.potenciaW.takeIf { e.activo }, e.libre))

                    EventoRodaje.ErgPlataformaActivo -> avisar(AvisoUi.EntrenamientoZwift)

                    is EventoRodaje.ReferenciaFijada -> avisar(AvisoUi.Referencia(e.marcha))

                    // Cambiar de pantalla lo resuelve MainActivity, sin aviso.
                    is EventoRodaje.Pantalla -> Unit

                    is EventoRodaje.ModeloCambiado -> avisar(AvisoUi.Modelo(e.modelo))

                    is EventoRodaje.Ventilador -> avisar(AvisoUi.Ventilador(e.aviso))
                }
            }
        }
    }

    private fun avisar(a: AvisoUi) {
        tope.value = null
        aviso.value = a
        borrarAviso?.cancel()
        borrarAviso = viewModelScope.launch {
            delay(DURACION_AVISO_MS)
            aviso.value = null
        }
    }

    private val confirmacionSalida = ConfirmacionSalida(viewModelScope, terminarSesion)

    val estado: StateFlow<RodajeUiState> = combine(
        combine(sesion.estado, sesion.guia) { e, g -> e to g },
        tope,
        aviso,
        configuracion,
        confirmacionSalida.estado,
    ) { (e, g), t, a, c, salida ->
        estadoDeRodaje(e, t, a, c, salida, g, nombreMovil())
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), estadoDeRodaje(sesion.estado.value, null, null))

    /** La tira del pie reabre la hoja de la guía en la página en la que estaba. */
    fun abrirGuia() = sesion.guia { it.abrir() }

    fun minimizarGuia() = sesion.guia { it.minimizar() }

    /** La equis de la guía: la oculta hasta que se vuelva a abrir la app. */
    fun cerrarGuia() = sesion.guia { it.cerrar() }

    /** «No volver a mostrar»: la oculta y lo recuerda. */
    fun noMostrarGuia() = sesion.noMostrarGuia()

    fun paginaGuia(pagina: Int) = sesion.guia { it.irA(pagina) }

    /** Botones − y + de la pantalla: el mismo camino que las palancas (spec `ride-screen`, «Botones − y + en Rodaje»). */
    fun mas() = sesion.accion(Accion.MarchaMas(1))

    fun menos() = sesion.accion(Accion.MarchaMenos(1))

    /** El indicador de modo tocable: la acción «Alternar SIM ⇄ ERG» del Square, sin lógica de modo propia (spec `ride-screen`, «Cambiar el modo tocándolo»). */
    fun alternarModo() = sesion.accion(Accion.AlternarErgManual)

    val notificacionesPedidas: StateFlow<Boolean> = sesion.notificacionesPedidas

    fun notificacionesPedidas() = sesion.notificacionesPedidas()

    /** Atrás desde Rodaje: el primero pide confirmación y un segundo en 3 s termina la sesión (spec `ride-session`). */
    fun atras() = confirmacionSalida.atras()

    /** Al salir de Rodaje: no queda un primer Atrás armado. */
    fun cancelarSalida() = confirmacionSalida.cancelar()

    companion object {
        const val DURACION_AVISO_MS = 1_500L

        val Factory = viewModelFactory {
            initializer {
                val graph: AppGraph = (this[APPLICATION_KEY] as SmartBikeApp).graph
                RodajeViewModel(graph.iniciarSesion(), graph::terminarSesion, graph.almacen.configuracion, graph::nombreBluetooth)
            }
        }
    }
}
