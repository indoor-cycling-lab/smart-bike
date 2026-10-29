package app.smartbike.plataforma

import app.smartbike.ble.EstadoBluetooth
import app.smartbike.dominio.IdentidadDispositivo
import app.smartbike.dominio.Terreno
import app.smartbike.protocolo.NotificacionGatt
import app.smartbike.protocolo.ServidorGatt
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow

/** Un transporte hacia Zwift de mentira: guarda lo publicado y lo notificado, y deja decidir el estado. */
class TransporteDePrueba : TransporteDircon {
    override val estado = MutableStateFlow<EstadoTransporte>(EstadoTransporte.Parado)
    var servidor: ServidorGatt? = null
    var identidad: IdentidadDispositivo? = null
    var nombre: String? = null
    var publicaciones = 0
    var paradas = 0
    val enviadas = mutableListOf<NotificacionGatt>()

    override fun publicar(servidor: ServidorGatt, identidad: IdentidadDispositivo, nombre: String) {
        this.servidor = servidor
        this.identidad = identidad
        this.nombre = nombre
        publicaciones++
        estado.value = EstadoTransporte.Esperando
    }

    override fun parar() {
        paradas++
        estado.value = EstadoTransporte.Parado
    }

    override fun notificar(n: NotificacionGatt) {
        enviadas += n
    }

    fun enviadasPor(c: UUID) = enviadas.filter { it.caracteristica == c }.map { it.valor.joinToString(" ") { b -> "%02x".format(b) } }
}

/** Un receptor que anota lo que Zwift le manda a la `Sesion`. */
class ReceptorGrabador : ReceptorPlataforma {
    val eventos = mutableListOf<String>()
    var ultimo: UltimoDePlataforma? = null
    val terrenos = mutableListOf<Terreno>()
    val potencias = mutableListOf<Int>()

    override fun terreno(terreno: Terreno) {
        terrenos += terreno
        eventos += "terreno $terreno"
    }
    override fun potenciaErg(vatios: Int) {
        potencias += vatios
        eventos += "erg $vatios"
    }
    override fun sinCambio() {
        eventos += "sin cambio"
    }
    override fun ultimoDePlataforma(ultimo: UltimoDePlataforma) {
        this.ultimo = ultimo
        eventos += "ultimo ${ultimo::class.simpleName}"
    }
}

class BluetoothDePrueba(encendido: Boolean = true, permiso: Boolean = true, permisoAnunciar: Boolean = true) : EstadoBluetooth {
    override val encendido = MutableStateFlow(encendido)
    override val permiso = MutableStateFlow(permiso)
    override val permisoAnunciar = MutableStateFlow(permisoAnunciar)
}
