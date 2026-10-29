package app.smartbike.plataforma

import app.smartbike.dominio.IdentidadDispositivo
import app.smartbike.dominio.NombreAnunciado
import app.smartbike.protocolo.NotificacionGatt
import app.smartbike.protocolo.ServidorGatt
import kotlinx.coroutines.flow.StateFlow

/**
 * Cómo está el transporte hacia la plataforma: sin publicar, anunciado esperando a la plataforma, anunciado pero sin
 * responder en la red (design.md del change `comprobar-anuncio-zwift`, decisión 2) o con la plataforma conectada.
 */
sealed interface EstadoTransporte {
    data object Parado : EstadoTransporte
    data object Esperando : EstadoTransporte
    data object NoVisible : EstadoTransporte

    /** Solo por Bluetooth: apagado o sin permiso, así que no se puede anunciar (change `ftms-bluetooth`). */
    data object SinBluetooth : EstadoTransporte

    /** Solo por Bluetooth: el móvil no puede anunciarse como periférico (NF3). */
    data object NoDisponible : EstadoTransporte
    data class Conectado(val direccion: String) : EstadoTransporte
}

/**
 * Lo común a los transportes hacia la plataforma (arquitectura.md § 4.3; design.md del change `puente-zwift-wifi`, decisión 3):
 * publica el [ServidorGatt], atiende a un cliente cada vez y le envía notificaciones. Hoy DirCon por wifi y el servidor
 * GATT nativo del mando por Bluetooth.
 */
interface TransporteDircon {
    val estado: StateFlow<EstadoTransporte>

    /**
     * Publica [servidor] con [identidad], anunciado como [nombre] (change `simular-kickr-bike`, decisión 3), y atiende a
     * la plataforma hasta [parar]. El servidor solo se toca desde el hilo de control, también en sus manejadores de escritura.
     */
    fun publicar(servidor: ServidorGatt, identidad: IdentidadDispositivo, nombre: String = NombreAnunciado.NORMAL)

    fun parar()

    /** Envía [n] al cliente, si hay; no espera a que salga. */
    fun notificar(n: NotificacionGatt)
}

/** El anuncio en la red local; con `NsdManager` en el móvil y un doble en los tests. */
interface Anunciador {
    /** Pide el anuncio y espera a que el sistema lo dé por registrado; `false` si falla o no contesta a tiempo. */
    suspend fun anunciar(nombre: String, tipo: String, puerto: Int, registros: Map<String, String>): Boolean
    fun retirar()
}
