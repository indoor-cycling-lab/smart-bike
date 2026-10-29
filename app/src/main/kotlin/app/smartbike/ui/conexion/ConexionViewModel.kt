package app.smartbike.ui.conexion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.smartbike.SmartBikeApp
import app.smartbike.ble.EstadoNombre
import app.smartbike.datos.AlmacenConfiguracion
import app.smartbike.dominio.Configuracion
import app.smartbike.dominio.Identidad
import app.smartbike.dominio.ModoConexion
import app.smartbike.sesion.EstadoRodaje
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Conexión: lee y cambia el modo y el nombre de `configuracion.json`; la `Sesion` aplica los cambios al guardarse, también
 * rodando (spec `platform-settings`, «Pestaña Conexión»). [nombreMovil] da el nombre Bluetooth del móvil.
 */
class ConexionViewModel(
    private val ajustes: AlmacenConfiguracion,
    rodaje: StateFlow<EstadoRodaje>,
    nombreBluetooth: StateFlow<EstadoNombre>,
    private val nombreMovil: () -> String? = { null },
) : ViewModel() {

    val estado: StateFlow<ConexionUiState> = combine(ajustes.configuracion, rodaje, nombreBluetooth) { c, e, n -> estadoDeConexion(c, e, nombreMovil(), n) }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            estadoDeConexion(Configuracion(), rodaje.value, nombreMovil(), nombreBluetooth.value),
        )

    private fun cambiar(cambio: (Configuracion) -> Configuracion) {
        viewModelScope.launch { ajustes.actualizarConfiguracion(cambio) }
    }

    fun modo(m: ModoConexion) = cambiar { it.copy(conexion = it.conexion.copy(modo = m)) }

    fun identidad(i: Identidad) = cambiar { it.copy(conexion = it.conexion.copy(identidad = i)) }

    fun nombrePorBluetooth(activado: Boolean) = cambiar { it.copy(conexion = it.conexion.copy(nombrePorBluetooth = activado)) }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val graph = (this[APPLICATION_KEY] as SmartBikeApp).graph
                val sesion = graph.iniciarSesion()
                ConexionViewModel(graph.almacen, sesion.estado, sesion.nombreBluetooth, graph::nombreBluetooth)
            }
        }
    }
}
