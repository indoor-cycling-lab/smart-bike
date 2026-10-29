package app.smartbike.ui.botones

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.smartbike.SmartBikeApp
import app.smartbike.datos.AlmacenConfiguracion
import app.smartbike.dominio.Asignacion
import app.smartbike.dominio.Configuracion
import app.smartbike.dominio.ConfiguracionBotones
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Botones: lee y cambia la asignación de la transmisión elegida y la sección `botones` de `configuracion.json`; la
 * `Sesion` aplica los cambios al guardarse.
 */
class BotonesViewModel(private val ajustes: AlmacenConfiguracion, squareConfigurado: Flow<Boolean> = flowOf(true)) : ViewModel() {

    private val confirmando = MutableStateFlow(false)

    val estado: StateFlow<BotonesUiState> = combine(ajustes.configuracion, confirmando, squareConfigurado) { c, conf, sq -> estadoDeBotones(c, conf, sq) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BotonesUiState())

    fun invertirPalancas(si: Boolean) = cambiarConfiguracion { it.conAsignacion(it.asignacion.copy(invertirPalancas = si)) }

    fun autorrepeticion(si: Boolean) = cambiar { it.copy(autorrepeticion = si) }

    fun tiempo(t: Tiempo, pasos: Int) = cambiar { it.copy(tiempos = t.cambiar(it.tiempos, pasos)) }

    fun pedirRestaurar() {
        confirmando.value = true
    }

    fun cancelarRestaurar() {
        confirmando.value = false
    }

    /**
     * La asignación de fábrica de la transmisión elegida, sin inversión, y los tiempos y la autorrepetición por defecto;
     * las demás transmisiones no cambian (spec `button-mapping`, «Restaurar»).
     */
    fun restaurar() {
        confirmando.value = false
        cambiarConfiguracion { it.conAsignacion(Asignacion.porDefecto(it.transmisionElegida)).copy(botones = ConfiguracionBotones()) }
    }

    private fun cambiar(cambio: (ConfiguracionBotones) -> ConfiguracionBotones) = cambiarConfiguracion { it.copy(botones = cambio(it.botones)) }

    private fun cambiarConfiguracion(cambio: (Configuracion) -> Configuracion) {
        viewModelScope.launch { ajustes.actualizarConfiguracion(cambio) }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val graph = (this[APPLICATION_KEY] as SmartBikeApp).graph
                BotonesViewModel(graph.almacen, graph.iniciarSesion().estado.map { it.squareConfigurado != null })
            }
        }
    }
}
