package app.smartbike.ui.ajustes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.smartbike.SmartBikeApp
import app.smartbike.datos.AlmacenConfiguracion
import app.smartbike.datos.CopiaConfiguracion
import app.smartbike.datos.EstadoGrabacion
import app.smartbike.datos.ResumenSesiones
import app.smartbike.dominio.Accion
import app.smartbike.dominio.Configuracion
import app.smartbike.dominio.Identidad
import app.smartbike.dominio.IdiomaApp
import app.smartbike.dominio.ModoBajadas
import app.smartbike.dominio.ModoConexion
import app.smartbike.dominio.ModoVentilador
import app.smartbike.dominio.OrientacionApp
import app.smartbike.dominio.ResultadoImportacion
import app.smartbike.dominio.TemaApp
import app.smartbike.dominio.TipoModelo
import app.smartbike.plataforma.EstadoMando
import app.smartbike.sesion.Sesion
import app.smartbike.ui.sesiones.refrescando
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Ajustes: lee y cambia `configuracion.json`; la `Sesion` aplica los cambios al guardarse. Del estado del [mando] solo
 * le importa si el móvil puede hacer de mando.
 * También exporta e importa la configuración con [copia] (design.md del change `exportar-bateria-y-giro`, decisión 4): un
 * archivo válido queda pendiente de confirmar y solo al confirmarlo sustituye los ajustes.
 */
class AjustesViewModel(
    private val ajustes: AlmacenConfiguracion,
    private val copia: CopiaConfiguracion? = null,
    ventilador: Flow<VentiladorAjustesUi?> = flowOf(null),
    private val sesion: Sesion? = null,
    grabacion: Flow<EstadoGrabacion> = flowOf(EstadoGrabacion.Apagada),
    resumenSesiones: Flow<ResumenSesiones> = flowOf(ResumenSesiones(0, 0)),
) : ViewModel() {

    private val resultadoCopia = MutableStateFlow<Copia>(Copia.Ninguna)

    val estado: StateFlow<AjustesUiState> = combine(ajustes.configuracion, resultadoCopia, ventilador, grabacion, resumenSesiones) { c, r, v, g, s -> estadoDeAjustes(c, v, g, s).copy(copia = r) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AjustesUiState())

    private fun cambiar(cambio: (Configuracion) -> Configuracion) {
        viewModelScope.launch { ajustes.actualizarConfiguracion(cambio) }
    }

    fun bajadas(modo: ModoBajadas) = cambiar { it.copy(bajadas = modo) }

    fun modelo(m: TipoModelo) = cambiar { it.copy(modelo = m) }

    fun dificultad(pasos: Int) = cambiar { it.copy(dificultadPct = dificultadMas(it.dificultadPct, pasos)) }

    fun potenciaMinima(pasos: Int) = cambiar { it.copy(potenciaMinimaW = potenciaMinimaMas(it.potenciaMinimaW, pasos)) }

    fun pesoBici(pasos: Int) = cambiar { it.copy(pesoBiciKg = pesoBiciMas(it.pesoBiciKg, pasos)) }

    fun botonesEnPantalla(activados: Boolean) = cambiar { it.copy(botonesEnPantalla = activados) }

    fun modoTactil(activado: Boolean) = cambiar { it.copy(modoTactil = activado) }

    fun mostrarAccion(activado: Boolean) = cambiar { it.copy(mostrarAccion = activado) }

    fun vibracion(activada: Boolean) = cambiar { it.copy(vibracion = activada) }

    /** Se aplica al momento a toda la app: `MainActivity` sigue el tema de la configuración (spec `settings-screen`). */
    fun tema(t: TemaApp) = cambiar { it.copy(tema = t) }

    /** Se aplica al momento a toda la app: `MainActivity` sigue el idioma de la configuración (spec `app-language`). */
    fun idioma(i: IdiomaApp) = cambiar { it.copy(idioma = i) }

    /** Se aplica al momento a toda la app: `MainActivity` sigue la orientación de la configuración (spec `settings-screen`). */
    fun orientacion(o: OrientacionApp) = cambiar { it.copy(orientacion = o) }

    /** Se aplica al momento: el grabador sigue el ajuste (design.md del change `registro-sesiones`, decisión 3). */
    fun grabarSesiones(activada: Boolean) = cambiar { it.copy(grabarSesiones = activada) }

    /** El espacio libre mínimo para grabar, de 5 en 5 % y en sus límites; se aplica al momento, también a una grabación en curso. */
    fun espacioLibreMinimo(pasos: Int) = cambiar { it.copy(espacioLibreMinimoPct = espacioLibreMinimoMas(it.espacioLibreMinimoPct, pasos)) }

    /** El peso del ciclista, de kilo en kilo y en sus límites. */
    fun peso(pasos: Int) = cambiar { it.copy(pesoCiclistaKg = (it.pesoCiclistaKg + pasos).coerceIn(Configuracion.PESO_KG)) }

    /** «Guía de inicio»: la guía vuelve a verse, abierta en la página 1, también si estaba descartada o terminada. */
    fun reabrirGuia() = sesion?.reabrirGuia()

    /** El modo del ventilador: el ventilador cambia al momento y su modo se lee de lo que él comunica. */
    fun ventiladorModo(modo: ModoVentilador) = sesion?.fijarModoVentilador(modo)

    /** Un paso de la velocidad manual del ventilador: el mismo camino y las mismas reglas que los botones del Square. */
    fun ventiladorVelocidad(pasos: Int) {
        sesion?.accion(if (pasos > 0) Accion.VentiladorMas else Accion.VentiladorMenos)
    }

    /** Un paso de 1 bpm del umbral de la [zona] (1 a 4); la `Sesion` lo escribe tras un rebote (decisión 4). */
    fun ventiladorUmbral(zona: Int, pasos: Int) {
        val u = estado.value.ventilador?.umbrales ?: return
        sesion?.cambiarUmbralVentilador(zona, u[zona - 1] + pasos)
    }

    /** Con [destino] `null` (selector cerrado) no pasa nada. */
    fun exportar(destino: String?) {
        if (destino == null || copia == null) return
        viewModelScope.launch {
            val hecho = copia.exportar(destino, ajustes.configuracion.first())
            resultadoCopia.value = if (hecho) Copia.Exportada else Copia.ErrorExportar
        }
    }

    /** Valida el archivo entero; no cambia ningún ajuste hasta [confirmarImportacion]. Con [origen] `null` no pasa nada. */
    fun elegirArchivo(origen: String?) {
        if (origen == null || copia == null) return
        viewModelScope.launch {
            resultadoCopia.value = when (val r = copia.leer(origen)) {
                is ResultadoImportacion.Valida -> Copia.Pendiente(r.configuracion)
                is ResultadoImportacion.Invalida -> Copia.ErrorImportar(r.motivo)
            }
        }
    }

    fun confirmarImportacion() {
        val pendiente = resultadoCopia.value as? Copia.Pendiente ?: return
        viewModelScope.launch {
            // Un archivo anterior al ajuste no trae idioma: la app conserva el que tiene (spec `app-language`).
            ajustes.actualizarConfiguracion { actual -> pendiente.configuracion.let { it.copy(idioma = it.idioma ?: actual.idioma) } }
            resultadoCopia.value = Copia.Importada
        }
    }

    /** El sistema no tiene selector de archivos (`ActivityNotFoundException`). */
    fun errorSelector() {
        resultadoCopia.value = Copia.ErrorSelector
    }

    /** Cancelar la confirmación o descartar el mensaje al salir de Ajustes. */
    fun descartarCopia() {
        resultadoCopia.value = Copia.Ninguna
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val graph = (this[APPLICATION_KEY] as SmartBikeApp).graph
                val sesion = graph.iniciarSesion()
                AjustesViewModel(
                    graph.almacen,
                    graph.copia,
                    sesion.estado.map(::ventiladorDeAjustes),
                    sesion,
                    graph.grabador.estado,
                    refrescando(graph.grabador.estado) { graph.sesionesGrabadas.resumen() },
                )
            }
        }
    }
}
