package app.smartbike.dominio

import app.smartbike.protocolo.SquareButton
import app.smartbike.protocolo.SquareDecoder

/** Cambio de estado de un botón del Square, con la hora de llegada de la notificación que lo trajo. */
sealed interface EventoBoton {
    val boton: SquareButton
    val tMs: Long

    /** [mascara] son todos los botones pulsados en ese momento, incluido [boton]: sirve para las combinaciones. */
    data class Pulsado(override val boton: SquareButton, override val tMs: Long, val mascara: UInt) : EventoBoton

    data class Soltado(override val boton: SquareButton, override val tMs: Long) : EventoBoton
}

/**
 * Convierte los contadores de `0x45` del Square en eventos de pulsar y soltar (F2.1, F2.2).
 *
 * Cada contador sube uno al pulsar y otro al soltar, así que la diferencia con el anterior (módulo 16) es el número de
 * transiciones: una pulsación completa entre dos notificaciones da pulsar y soltar, y una notificación que repite los
 * contadores no da nada. La primera notificación fija la línea base. No tiene reloj propio: la hora llega con cada
 * notificación.
 */
class EventosBoton {
    private var contadores: IntArray? = null

    /** Botones pulsados según la última notificación. */
    var mascara: UInt = 0u
        private set

    fun procesar(nuevos: IntArray, tMs: Long): List<EventoBoton> {
        val anteriores = contadores
        contadores = nuevos.copyOf()
        if (anteriores == null) {
            // Línea base: los contadores impares son botones que ya estaban pulsados al conectar.
            mascara = SquareDecoder.mascara(nuevos)
            return SquareButton.pulsados(mascara).map { EventoBoton.Pulsado(it, tMs, mascara) }
        }
        val cambian = SquareButton.entries.filter { nuevos[it.ordinal] != anteriores[it.ordinal] }
        // Primero los que empiezan soltando, para que un botón que se suelta y otro que se pulsa en la misma
        // notificación nunca parezcan pulsados a la vez; dentro de cada grupo, en orden de bit.
        val (empiezanSoltando, empiezanPulsando) = cambian.partition { mascara and it.mascara != 0u }
        val eventos = mutableListOf<EventoBoton>()
        for (b in empiezanSoltando + empiezanPulsando) {
            repeat(Math.floorMod(nuevos[b.ordinal] - anteriores[b.ordinal], 16)) {
                if (mascara and b.mascara != 0u) {
                    mascara = mascara and b.mascara.inv()
                    eventos += EventoBoton.Soltado(b, tMs)
                } else {
                    mascara = mascara or b.mascara
                    eventos += EventoBoton.Pulsado(b, tMs, mascara)
                }
            }
        }
        return eventos
    }

    /**
     * Conexión perdida: suelta los botones que quedaban pulsados, para que ninguno se quede repitiendo, y olvida la
     * línea base, para que la reconexión empiece de nuevo.
     */
    fun perdida(tMs: Long): List<EventoBoton> {
        val sueltos = SquareButton.pulsados(mascara).map { EventoBoton.Soltado(it, tMs) }
        mascara = 0u
        contadores = null
        return sueltos
    }
}
