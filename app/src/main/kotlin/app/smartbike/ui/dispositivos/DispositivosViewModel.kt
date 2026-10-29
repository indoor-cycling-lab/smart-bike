package app.smartbike.ui.dispositivos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.smartbike.SmartBikeApp
import app.smartbike.sesion.Sesion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class DispositivosViewModel(private val sesion: Sesion) : ViewModel() {

    private val permiso = MutableStateFlow(Permiso.Desconocido)

    val estado: StateFlow<DispositivosUiState> = combine(sesion.estado, permiso) { e, p -> estadoDeDispositivos(e, p) }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            estadoDeDispositivos(sesion.estado.value, Permiso.Desconocido),
        )

    fun permisoConcedido(concedido: Boolean) {
        permiso.value = if (concedido) Permiso.Concedido else Permiso.Denegado
    }

    fun buscarRodillo() = sesion.buscarRodillo()

    fun buscarMando() = sesion.buscarMando()

    fun reconectar(d: Sesion.Dispositivo) = sesion.reconectar(d)

    fun buscarPulsometro() = sesion.buscarPulsometro()

    fun buscarVentilador() = sesion.buscarVentilador()

    fun olvidar(d: Sesion.Dispositivo) = sesion.olvidar(d)

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val graph = (this[APPLICATION_KEY] as SmartBikeApp).graph
                DispositivosViewModel(graph.iniciarSesion())
            }
        }
    }
}
