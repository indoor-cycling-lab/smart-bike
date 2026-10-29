package app.smartbike.ui.sesiones

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.smartbike.SmartBikeApp
import app.smartbike.datos.EstadoGrabacion
import app.smartbike.datos.RepositorioSesiones
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Cada cuánto se vuelve a leer la carpeta mientras se graba, para que el tamaño de la activa crezca a la vista. */
private const val MUESTREO_MS = 2_000L

/**
 * Vuelve a leer con [leer] cuando cambia el estado de la grabación o [refresco], y cada [MUESTREO_MS] mientras se graba
 * (design.md del change `registro-sesiones`, decisión 6). La lectura va en IO.
 */
@OptIn(ExperimentalCoroutinesApi::class)
fun <T> refrescando(grabacion: Flow<EstadoGrabacion>, refresco: Flow<Int> = MutableStateFlow(0), leer: () -> T): Flow<T> = combine(grabacion, refresco) { g, _ -> g }
    .transformLatest { g ->
        emit(leer())
        while (g is EstadoGrabacion.Grabando) {
            delay(MUESTREO_MS)
            emit(leer())
        }
    }
    .flowOn(Dispatchers.IO)

/** Sesiones grabadas: la lista y su borrado, con confirmación en línea (spec `session-recording`). */
class SesionesViewModel(
    private val repositorio: RepositorioSesiones,
    grabacion: Flow<EstadoGrabacion>,
) : ViewModel() {

    private val refresco = MutableStateFlow(0)
    private val confirmacion = MutableStateFlow<Confirmacion>(Confirmacion.Ninguna)
    private val _sinApp = MutableStateFlow(false)

    /** Lo último que se intentó abrir no lo atiende ninguna app. */
    val sinApp: StateFlow<Boolean> = _sinApp

    val estado: StateFlow<SesionesUiState> = combine(refrescando(grabacion, refresco) { repositorio.listar() }, grabacion, confirmacion) { l, g, c ->
        SesionesUiState(l, g, c)
    }.let { flujo -> flujo.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SesionesUiState()) }

    fun pedirBorrar(nombre: String) {
        confirmacion.value = estado.value.pedirBorrar(nombre).confirmacion
    }

    fun pedirBorrarTodas() {
        confirmacion.value = estado.value.pedirBorrarTodas().confirmacion
    }

    fun cancelar() {
        confirmacion.value = Confirmacion.Ninguna
    }

    /** Borra lo que pidió la confirmación; la que se graba nunca se borra. */
    fun confirmar() {
        val pedido = confirmacion.value
        confirmacion.value = Confirmacion.Ninguna
        viewModelScope.launch(Dispatchers.IO) {
            when (pedido) {
                is Confirmacion.Una -> repositorio.borrar(pedido.nombre)
                Confirmacion.Todas -> repositorio.borrarTodas()
                Confirmacion.Ninguna -> Unit
            }
            refresco.update { it + 1 }
        }
    }

    /** El archivo de [nombre] para abrirlo o compartirlo, si sigue ahí. */
    fun archivo(nombre: String): File? = repositorio.archivo(nombre)

    fun avisarSinApp(sin: Boolean) {
        _sinApp.value = sin
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val graph = (this[APPLICATION_KEY] as SmartBikeApp).graph
                SesionesViewModel(graph.sesionesGrabadas, graph.grabador.estado)
            }
        }
    }
}
