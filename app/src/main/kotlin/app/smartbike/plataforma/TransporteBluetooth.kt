package app.smartbike.plataforma

import app.smartbike.ble.EstadoBluetooth
import app.smartbike.dominio.IdentidadDispositivo
import app.smartbike.protocolo.NotificacionGatt
import app.smartbike.protocolo.ServidorGatt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * El transporte del modo «Rodillo por Bluetooth (FTMS)» (design.md del change `ftms-bluetooth`, decisión 4): un servidor
 * GATT ([servidor], el `GattServerHost`) que solo se publica mientras el Bluetooth está encendido, hay permisos y el móvil
 * puede anunciarse como periférico (NF3), y se retira si falta algo. Publicar es idempotente, así que se publica una vez
 * mientras duren las condiciones; al cumplirse de nuevo vuelve a anunciarse sin que nadie haga nada. Su [estado] añade
 * [EstadoTransporte.SinBluetooth] y [EstadoTransporte.NoDisponible] al del servidor.
 */
class TransporteBluetooth(
    ambito: CoroutineScope,
    private val servidor: TransporteDircon,
    private val bluetooth: EstadoBluetooth,
    /** Si el móvil puede anunciarse como periférico Bluetooth (NF3); solo vale con el Bluetooth encendido. */
    private val perifericoDisponible: () -> Boolean,
) : TransporteDircon {
    private class Pedido(val servidor: ServidorGatt, val identidad: IdentidadDispositivo, val nombre: String)

    private val _estado = MutableStateFlow<EstadoTransporte>(EstadoTransporte.Parado)
    override val estado: StateFlow<EstadoTransporte> = _estado.asStateFlow()

    private var pedido: Pedido? = null
    private var publicado = false

    init {
        ambito.launch {
            combine(bluetooth.encendido, bluetooth.permiso, bluetooth.permisoAnunciar, servidor.estado) { _, _, _, _ -> }
                .collect { actualizar() }
        }
    }

    override fun publicar(servidor: ServidorGatt, identidad: IdentidadDispositivo, nombre: String) {
        pedido = Pedido(servidor, identidad, nombre)
        actualizar()
    }

    override fun parar() {
        pedido = null
        actualizar()
    }

    override fun notificar(n: NotificacionGatt) = servidor.notificar(n)

    private fun actualizar() {
        val p = pedido
        val falta = when {
            !bluetooth.encendido.value || !bluetooth.permiso.value || !bluetooth.permisoAnunciar.value -> EstadoTransporte.SinBluetooth
            !perifericoDisponible() -> EstadoTransporte.NoDisponible
            else -> null
        }
        if (p != null && falta == null) {
            if (!publicado) {
                publicado = true
                servidor.publicar(p.servidor, p.identidad, p.nombre)
            }
        } else if (publicado) {
            publicado = false
            servidor.parar()
        }
        val nuevo = when {
            p == null -> EstadoTransporte.Parado
            falta != null -> falta
            else -> servidor.estado.value
        }
        if (_estado.value != nuevo) _estado.value = nuevo
    }
}
