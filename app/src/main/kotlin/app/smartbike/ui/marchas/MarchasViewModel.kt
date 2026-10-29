package app.smartbike.ui.marchas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.smartbike.SmartBikeApp
import app.smartbike.datos.AlmacenConfiguracion
import app.smartbike.dominio.Configuracion
import app.smartbike.dominio.ModoCambio
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Marchas: lee y cambia `configuracion.json`; la `Sesion` aplica los cambios al guardarse, sin terminar la sesión. */
class MarchasViewModel(private val ajustes: AlmacenConfiguracion) : ViewModel() {

    val estado: StateFlow<MarchasUiState> = ajustes.configuracion.map(::estadoDeMarchas)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MarchasUiState())

    fun transmision(id: String) = cambiar { it.copy(transmision = id) }

    fun modo(m: ModoCambio) = cambiar { it.conModo(m) }

    /** Mueve la referencia de la transmisión elegida a la combinación de al lado; en el extremo, no hace nada. */
    fun referencia(dura: Boolean) = cambiar { c ->
        referenciaSiguiente(c.transmisionElegida, c.referencia, dura)?.let { c.conReferencia(it) } ?: c
    }

    fun recordarUltimaMarcha(si: Boolean) = cambiar { it.copy(recordarUltimaMarcha = si) }

    private fun cambiar(cambio: (Configuracion) -> Configuracion) {
        viewModelScope.launch { ajustes.actualizarConfiguracion(cambio) }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { MarchasViewModel((this[APPLICATION_KEY] as SmartBikeApp).graph.almacen) }
        }
    }
}
