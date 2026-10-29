package app.smartbike.ui.datos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.smartbike.SmartBikeApp
import app.smartbike.datos.AlmacenConfiguracion
import app.smartbike.dominio.DatoRodaje
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Datos en pantalla: cambia `datos` en `configuracion.json`; Rodaje lo lee al momento. */
class DatosViewModel(private val ajustes: AlmacenConfiguracion) : ViewModel() {

    val estado: StateFlow<DatosUiState> = ajustes.configuracion.map(::estadoDeDatos)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DatosUiState())

    fun alternar(d: DatoRodaje) {
        viewModelScope.launch { ajustes.actualizarConfiguracion { it.copy(datos = alternarDato(it.datos, d)) } }
    }

    fun mover(d: DatoRodaje, pasos: Int) {
        viewModelScope.launch { ajustes.actualizarConfiguracion { it.copy(datos = moverDato(it.datos, d, pasos)) } }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { DatosViewModel((this[APPLICATION_KEY] as SmartBikeApp).graph.almacen) }
        }
    }
}
