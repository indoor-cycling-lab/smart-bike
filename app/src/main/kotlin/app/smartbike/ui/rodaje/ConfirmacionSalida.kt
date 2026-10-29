package app.smartbike.ui.rodaje

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/** El aviso de salida de Rodaje (spec `ride-screen`, «Confirmar la salida con Atrás»). */
enum class SalidaUi { Ninguna, Confirmar, Cerrando }

/**
 * La ventana de confirmación de Atrás (design.md del change `confirmar-salida-rodaje`, decisión 1): el primer Atrás abre
 * [VENTANA_SALIDA_MS] de espera; un segundo Atrás dentro de ella llama a [terminar] una sola vez. Aparte del `ViewModel`
 * para comprobarla con tiempo virtual.
 */
class ConfirmacionSalida(private val scope: CoroutineScope, private val terminar: () -> Unit) {
    private val _estado = MutableStateFlow(SalidaUi.Ninguna)
    val estado: StateFlow<SalidaUi> = _estado
    private var ventana: Job? = null

    fun atras() {
        when (_estado.value) {
            SalidaUi.Ninguna -> {
                _estado.value = SalidaUi.Confirmar
                ventana = scope.launch {
                    delay(VENTANA_SALIDA_MS)
                    _estado.value = SalidaUi.Ninguna
                }
            }

            SalidaUi.Confirmar -> {
                ventana?.cancel()
                _estado.value = SalidaUi.Cerrando
                terminar()
            }

            SalidaUi.Cerrando -> Unit
        }
    }

    /** Al salir de Rodaje: limpia un aviso a medias, pero no interrumpe un cierre en marcha. */
    fun cancelar() {
        if (_estado.value != SalidaUi.Confirmar) return
        ventana?.cancel()
        _estado.value = SalidaUi.Ninguna
    }

    companion object {
        const val VENTANA_SALIDA_MS = 3_000L
    }
}
