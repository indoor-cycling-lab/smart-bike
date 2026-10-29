package app.smartbike.ui.tabla

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.smartbike.SmartBikeApp
import app.smartbike.datos.AlmacenConfiguracion
import app.smartbike.dominio.EstadoMarcha
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Tabla de marchas: cambia `tablas` en `configuracion.json`; la `Sesion` lo aplica al momento. */
class TablaViewModel(
    private val ajustes: AlmacenConfiguracion,
    puesta: Flow<EstadoMarcha?> = flowOf(null),
) : ViewModel() {

    val estado: StateFlow<TablaUiState> = combine(ajustes.configuracion, puesta, ::estadoDeTabla)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TablaUiState())

    fun ajustar(indice: Int, pasos: Int) {
        viewModelScope.launch {
            ajustes.actualizarConfiguracion {
                val t = it.transmisionElegida
                it.conTabla(t, ajustarFila(it.tabla(t), indice, pasos))
            }
        }
    }

    fun restablecer() {
        viewModelScope.launch { ajustes.actualizarConfiguracion { it.sinTabla(it.transmisionElegida) } }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val graph = (this[APPLICATION_KEY] as SmartBikeApp).graph
                TablaViewModel(graph.almacen, graph.iniciarSesion().estado.map { it.marcha })
            }
        }
    }
}
