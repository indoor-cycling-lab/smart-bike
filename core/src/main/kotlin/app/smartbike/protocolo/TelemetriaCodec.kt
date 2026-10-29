package app.smartbike.protocolo

/**
 * Telemetría estándar del Tacx Neo (arquitectura.md § 7.2): Cycling Power Measurement (`0x2A63`) para la potencia y
 * CSC Measurement (`0x2A5B`) para la biela, de la que sale la cadencia.
 */
object TelemetriaCodec {
    /** Revoluciones de biela acumuladas y hora del último evento de biela en 1/1024 s, los dos de 16 bits. */
    data class Biela(val revoluciones: Int, val hora1024: Int)

    /** Potencia instantánea en W (`sint16` tras las *flags*), o `null` si la trama es demasiado corta. */
    fun potencia(datos: ByteArray): Int? {
        if (datos.size < 4) return null
        return ((datos[3].toInt() shl 8) or (datos[2].toInt() and 0xFF)).toShort().toInt()
    }

    /** Datos de biela de CSC Measurement, o `null` si la trama no los trae. */
    fun biela(datos: ByteArray): Biela? {
        if (datos.isEmpty()) return null
        val flags = datos[0].toInt()
        val conRueda = flags and 0x01 != 0
        val conBiela = flags and 0x02 != 0
        if (!conBiela) return null
        val i = 1 + if (conRueda) 6 else 0
        if (datos.size < i + 4) return null
        return Biela(u16(datos, i), u16(datos, i + 2))
    }

    private fun u16(d: ByteArray, i: Int) = (d[i].toInt() and 0xFF) or ((d[i + 1].toInt() and 0xFF) shl 8)
}

/**
 * Cadencia a partir de los eventos de biela sucesivos, con desbordamiento de 16 bits. Es 0 con potencia 0 o si no hay
 * eventos de biela nuevos en [SIN_EVENTOS_MS] (por la hora de llegada de las notificaciones).
 */
class CalculoCadencia {
    private var anterior: TelemetriaCodec.Biela? = null
    private var ultimoEventoMs = 0L
    private var rpm = 0

    fun biela(b: TelemetriaCodec.Biela, tMs: Long): Int {
        val a = anterior
        anterior = b
        if (a == null) {
            ultimoEventoMs = tMs
            return rpm
        }
        val revs = (b.revoluciones - a.revoluciones) and 0xFFFF
        val tiempo = (b.hora1024 - a.hora1024) and 0xFFFF
        if (revs > 0 && tiempo > 0) {
            rpm = Math.round(revs * 60 * 1024.0 / tiempo).toInt()
            ultimoEventoMs = tMs
        } else if (tMs - ultimoEventoMs > SIN_EVENTOS_MS) {
            rpm = 0
        }
        return rpm
    }

    /** Con potencia 0 no se está pedaleando. */
    fun potencia(vatios: Int): Int {
        if (vatios == 0) rpm = 0
        return rpm
    }

    fun reiniciar() {
        anterior = null
        rpm = 0
    }

    companion object {
        const val SIN_EVENTOS_MS = 2_000L
    }
}
