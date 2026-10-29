package app.smartbike.plataforma.zwift

import app.smartbike.ble.EstadoBluetooth
import app.smartbike.dominio.FuncionMando
import app.smartbike.dominio.FuncionZwift
import app.smartbike.dominio.LadoGiro
import app.smartbike.plataforma.EstadoMando
import app.smartbike.plataforma.MandoEmulado
import app.smartbike.plataforma.TransporteDircon
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * El mando de Zwift como periférico Bluetooth aparte, junto al modo «Rodillo por wifi (FTMS)» (design.md del change
 * `generalizar-plataformas`, decisión 4). Lo publica con el ajuste activado, el Bluetooth encendido, los permisos y el
 * modo periférico, y lo para si falta algo (design.md del change `mando-zwift-emulado`, decisión 6). Tiene su propio
 * transporte Bluetooth (servidor GATT y anuncio): el rodillo y el mando no comparten transporte.
 */
class MandoZwiftBluetooth(
    ambito: CoroutineScope,
    private val mando: MandoZwift,
    private val transporteMando: TransporteDircon,
    private val bluetooth: EstadoBluetooth,
    /** Si el móvil puede anunciarse como periférico Bluetooth (NF3); solo vale con el Bluetooth encendido. */
    private val perifericoDisponible: () -> Boolean,
) : MandoEmulado {
    private val _estado = MutableStateFlow(EstadoMando.Desactivado)
    override val estado: StateFlow<EstadoMando> = _estado.asStateFlow()

    private var activado = false
    private var terminado = false

    init {
        ambito.launch {
            combine(bluetooth.encendido, bluetooth.permiso, bluetooth.permisoAnunciar, mando.estado) { _, _, _, _ -> }
                .collect { actualizar() }
        }
    }

    override fun activar(activado: Boolean) {
        this.activado = activado
        actualizar()
    }

    override fun terminar() {
        terminado = true
        actualizar()
    }

    override fun pulsar(funcion: FuncionMando) {
        if (funcion is FuncionZwift) mando.pulsar(funcion)
    }

    override fun soltar(funcion: FuncionMando) {
        if (funcion is FuncionZwift) mando.soltar(funcion)
    }

    override fun soltarTodo() = mando.soltarTodo()

    override val giro: Set<LadoGiro> get() = mando.giro

    /**
     * Publica el mando con el ajuste activado, el Bluetooth encendido, los permisos y el modo periférico, y lo para si
     * falta algo; publicarlo es idempotente, así que se publica una vez mientras duren esas condiciones, no con cada
     * reconexión del Neo o del Square.
     */
    private fun actualizar() {
        val visible = when {
            !activado -> EstadoMando.Desactivado
            !bluetooth.encendido.value || !bluetooth.permiso.value || !bluetooth.permisoAnunciar.value -> EstadoMando.SinConexion
            !perifericoDisponible() -> EstadoMando.NoDisponible
            else -> null
        }
        if (visible == null && !terminado) {
            if (mando.estado.value == EstadoMando.Desactivado) {
                mando.publicar()
                transporteMando.publicar(mando.servidor, MandoZwift.IDENTIDAD)
            }
        } else if (mando.estado.value != EstadoMando.Desactivado) {
            // Zwift conserva lo último de cada palanca: se suelta todo antes de dejar de anunciar el mando.
            mando.soltarTodo()
            mando.parar()
            transporteMando.parar()
        }
        val nuevo = visible ?: mando.estado.value.takeIf { it != EstadoMando.Desactivado } ?: EstadoMando.Esperando
        if (_estado.value != nuevo) _estado.value = nuevo
    }
}
