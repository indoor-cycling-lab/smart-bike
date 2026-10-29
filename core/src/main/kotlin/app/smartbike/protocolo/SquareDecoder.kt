package app.smartbike.protocolo

/**
 * Notificaciones de botones del Elite Square (arquitectura.md § 7.1).
 *
 * - `347b0045-…` (la que se usa): 11 bytes; el byte 0 va a cero y, desde el *nibble* alto del byte 1, hay un
 *   contador de 4 bits por botón ([SquareButton.contador]) que sube uno al pulsar y otro al soltar: impar = pulsado.
 *   Al ser contadores, no se pierde ninguna pulsación aunque ocurra entera entre dos notificaciones.
 * - `347b0043-…`: bytes 3–6, *big endian*, un bit por botón ([SquareButton.mascara]). Solo da el estado del momento y
 *   pierde las pulsaciones rápidas; se conserva para comparar en las trazas.
 */
object SquareDecoder {
    private const val LONGITUD_CONTADORES = 11
    private const val LONGITUD_MASCARA = 7

    /** Contadores de `0x45` indexados por `SquareButton.ordinal`, o `null` si la trama es demasiado corta. */
    fun contadores(datos: ByteArray): IntArray? {
        if (datos.size < LONGITUD_CONTADORES) return null
        return IntArray(SquareButton.entries.size) { i ->
            val nibble = SquareButton.entries[i].contador + 2 // los dos primeros nibbles son el byte 0
            val b = datos[nibble / 2].toInt() and 0xFF
            if (nibble % 2 == 0) b shr 4 else b and 0x0F
        }
    }

    /** Botones pulsados según la paridad de los contadores. */
    fun mascara(contadores: IntArray): UInt = SquareButton.entries.fold(0u) { acc, b -> if (contadores[b.ordinal] % 2 == 1) acc or b.mascara else acc }

    /** Máscara de `0x43`, o `null` si la trama es demasiado corta para contenerla. */
    fun mascara(datos: ByteArray): UInt? {
        if (datos.size < LONGITUD_MASCARA) return null
        return (3..6).fold(0u) { acc, i -> (acc shl 8) or (datos[i].toUInt() and 0xFFu) }
    }
}
