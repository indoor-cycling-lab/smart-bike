package app.smartbike.ui.botones

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.smartbike.SmartBikeApp
import app.smartbike.datos.AlmacenConfiguracion
import app.smartbike.dominio.Accion
import app.smartbike.dominio.Regla
import app.smartbike.dominio.TipoGesto
import app.smartbike.sesion.EstadoCaptura
import app.smartbike.sesion.Sesion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Asignar acción: mientras está abierta, la `Sesion` captura las entradas del Square en lugar de ejecutar sus acciones
 * (design.md del change `gestos-y-asignacion`, decisión 7). Nada se guarda hasta «Guardar».
 */
class AsignarAccionViewModel(
    private val accion: Accion,
    private val sesion: Sesion,
    private val ajustes: AlmacenConfiguracion,
) : ViewModel() {

    private val quitadas = MutableStateFlow(emptyList<Regla>())
    private val gesto = MutableStateFlow(TipoGesto.Pulsar)

    init {
        sesion.empezarCaptura()
        viewModelScope.launch {
            // Un Campagnolo solo no admite «pulsar»: se propone «corta».
            sesion.captura.collect { c ->
                val e = (c as? EstadoCaptura.Capturada)?.entrada ?: return@collect
                if (e.boton.esModificador && e.modificador == null) gesto.update { if (it == TipoGesto.Pulsar) TipoGesto.Corta else it }
            }
        }
    }

    val estado: StateFlow<AsignarAccionUiState> =
        combine(ajustes.configuracion, quitadas, sesion.captura, gesto) { c, q, captura, g ->
            estadoDeAsignar(accion, c.asignacion, q, captura ?: EstadoCaptura.Esperando, g)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AsignarAccionUiState(accion))

    fun quitar(r: Regla) = quitadas.update { it + r }

    fun elegirGesto(g: TipoGesto) {
        if (g !in estado.value.gestosDeshabilitados) gesto.value = g
    }

    /** Guarda en `configuracion.json` y, al terminar, llama a [hecho] para volver a Botones. */
    fun guardar(hecho: () -> Unit) {
        val e = estado.value
        if (!e.puedeGuardar) return
        viewModelScope.launch {
            ajustes.actualizarConfiguracion {
                // En la asignación de la transmisión elegida (spec `button-mapping`, «Una asignación por transmisión»).
                it.conAsignacion(asignacionAlGuardar(it.asignacion, accion, quitadas.value, e.capturada, e.gesto))
            }
            hecho()
        }
    }

    override fun onCleared() = sesion.terminarCaptura()

    companion object {
        fun factory(accion: Accion) = viewModelFactory {
            initializer {
                val graph = (this[APPLICATION_KEY] as SmartBikeApp).graph
                AsignarAccionViewModel(accion, graph.iniciarSesion(), graph.almacen)
            }
        }
    }
}
