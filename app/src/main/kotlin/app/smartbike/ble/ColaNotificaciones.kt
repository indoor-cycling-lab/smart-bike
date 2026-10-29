package app.smartbike.ble

import app.smartbike.protocolo.NotificacionGatt
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Notificaciones en serie por dispositivo (arquitectura.md § 7.4; design.md del change `endurecer-dircon-ble`,
 * decisión 5). Android solo admite una en vuelo por conexión, así que cada dispositivo tiene su cola y su confirmación
 * ([confirmar], `onNotificationSent`): cada notificación espera a la anterior de *ese* dispositivo, como mucho
 * [esperaMs], y uno lento no retrasa a los demás. Solo reciben las notificaciones los [destinos] que estén [suscrito]s
 * a la característica. [enviar] dice si Android la aceptó y [alSalir], si salió hacia alguno. Si se acumulan, se pierden
 * las más antiguas.
 */
class ColaNotificaciones<D : Any>(
    private val ambito: CoroutineScope,
    private val destinos: () -> Collection<D>,
    private val suscrito: (D, UUID) -> Boolean,
    private val enviar: (D, NotificacionGatt) -> Boolean,
    private val alSalir: (NotificacionGatt) -> Unit,
    private val esperaMs: Long = ESPERA_ENVIO_MS,
) {
    private class Cola(val salida: Channel<NotificacionGatt>, val confirmada: Channel<Unit>)

    private val colas = HashMap<D, Cola>()
    private var cerrada = false

    fun encolar(n: NotificacionGatt) {
        for (d in destinos()) {
            if (suscrito(d, n.caracteristica)) colaDe(d)?.salida?.trySend(n)
        }
    }

    /** [d] ha confirmado la última notificación enviada. */
    fun confirmar(d: D) {
        synchronized(this) { colas[d] }?.confirmada?.trySend(Unit)
    }

    /** [d] ya no es destino: su cola se cierra y lo pendiente se descarta. */
    fun quitar(d: D) {
        synchronized(this) { colas.remove(d) }?.salida?.cancel()
    }

    /** No sale nada más; lo pendiente se descarta. */
    fun cerrar() {
        val todas = synchronized(this) {
            cerrada = true
            colas.values.toList().also { colas.clear() }
        }
        todas.forEach { it.salida.cancel() }
    }

    private fun colaDe(d: D): Cola? = synchronized(this) {
        if (cerrada) return null
        colas.getOrPut(d) {
            val c = Cola(Channel(CAPACIDAD, BufferOverflow.DROP_OLDEST), Channel(Channel.CONFLATED))
            ambito.launch {
                for (n in c.salida) {
                    c.confirmada.tryReceive()
                    if (enviar(d, n)) {
                        withTimeoutOrNull(esperaMs) { c.confirmada.receive() }
                        alSalir(n)
                    }
                }
            }
            c
        }
    }

    companion object {
        const val CAPACIDAD = 64
        const val ESPERA_ENVIO_MS = 500L
    }
}
