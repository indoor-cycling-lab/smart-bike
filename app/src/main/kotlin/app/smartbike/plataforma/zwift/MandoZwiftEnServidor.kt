package app.smartbike.plataforma.zwift

import app.smartbike.dominio.FuncionMando
import app.smartbike.dominio.FuncionZwift
import app.smartbike.dominio.LadoGiro
import app.smartbike.plataforma.EstadoMando
import app.smartbike.plataforma.EstadoTransporte
import app.smartbike.plataforma.MandoEmulado
import app.smartbike.protocolo.NotificacionGatt
import app.smartbike.protocolo.ServicioGatt
import app.smartbike.protocolo.ServidorGatt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * El mando de Zwift como un servicio más del servidor GATT del rodillo por Bluetooth, en un solo dispositivo y un solo
 * anuncio (design.md del change `ftms-bluetooth`, decisión 5; validado con Zwift en PC en la tarea 6.6). Lo usa
 * `ConexionFtms` con [servicio] y [usarServidor]; el estado sigue al de [MandoZwift] y a si el rodillo está publicado, como
 * el mando de `ConexionZwiftRide`.
 */
class MandoZwiftEnServidor(
    ambito: CoroutineScope,
    private val mando: MandoZwift,
    /** El estado del transporte del rodillo: sin Bluetooth o sin modo periférico el mando tampoco se ve. */
    private val transporte: StateFlow<EstadoTransporte>,
) : MandoEmulado {
    private val _estado = MutableStateFlow(EstadoMando.SinConexion)
    override val estado: StateFlow<EstadoMando> = _estado.asStateFlow()

    private var activado = false
    private var publicado = false

    init {
        ambito.launch { mando.estado.collect { recalcular() } }
        ambito.launch { transporte.collect { recalcular() } }
    }

    /** El servicio del mando con sus cinco características, para añadirlo al del rodillo. */
    val servicio: ServicioGatt get() = mando.servicio

    /** El servidor del rodillo, donde Zwift se suscribe a las notificaciones del mando. */
    fun usarServidor(s: ServidorGatt) = mando.usarServidor(s)

    /** Si Zwift ha saludado al mando. */
    val saludado: Boolean get() = mando.saludado

    /** Cuántos handshakes ha habido: distingue a un cliente del siguiente. */
    val saludos: Int get() = mando.saludos

    /** Lo que llega por SYNC_RX y el mando no entiende: los `SET` si Zwift lo trata como un Zwift Ride (decisión 9 de `pestana-conexion`). */
    fun alEscrituraDesconocida(atender: (ByteArray) -> List<NotificacionGatt>?) {
        mando.alEscrituraDesconocida = atender
    }

    /** Notifica [trama] a Zwift por ASYNC. */
    fun notificarAsync(trama: ByteArray) = mando.notificarAsync(trama)

    /** El rodillo se publica o se retira: el mando empieza a esperar o se para con él. */
    fun publicado(publicado: Boolean) {
        this.publicado = publicado
        if (publicado) mando.publicar() else mando.parar()
        recalcular()
    }

    override fun activar(activado: Boolean) {
        this.activado = activado
        recalcular()
    }

    override fun pulsar(funcion: FuncionMando) {
        if (funcion is FuncionZwift) mando.pulsar(funcion)
    }

    override fun soltar(funcion: FuncionMando) {
        if (funcion is FuncionZwift) mando.soltar(funcion)
    }

    override fun soltarTodo() = mando.soltarTodo()

    override val giro: Set<LadoGiro> get() = mando.giro

    override fun terminar() = publicado(false)

    private fun recalcular() {
        val nuevo = when {
            !activado -> EstadoMando.Desactivado
            !publicado || transporte.value == EstadoTransporte.SinBluetooth -> EstadoMando.SinConexion
            transporte.value == EstadoTransporte.NoDisponible -> EstadoMando.NoDisponible
            else -> mando.estado.value.takeIf { it != EstadoMando.Desactivado } ?: EstadoMando.Esperando
        }
        if (_estado.value != nuevo) _estado.value = nuevo
    }
}
