package app.smartbike.protocolo

import kotlin.math.roundToInt

/**
 * Mensajes ANT+ FE-C que se escriben en la característica `6e40fec3-…` del Tacx Neo:
 * `A4 09 4E 05` + página + 7 bytes de datos + checksum (XOR de todos los bytes), 13 bytes en total.
 *
 * El checksum de la página 0x33 se validó con el Neo 1 real: aplica la pendiente con el XOR estándar de ANT, y sus
 * propias páginas FE-C usan el mismo (arquitectura.md § 7.2).
 */
object FecCodec {
    const val PAGINA_POTENCIA = 0x31
    const val PAGINA_PENDIENTE = 0x33
    const val PENDIENTE_MIN = -200.0
    const val PENDIENTE_MAX = 200.0
    const val POTENCIA_MAX = 4000

    /** Página 0x33 (Track Resistance): pendiente en % con resolución de 0,01 %; rodadura por defecto. */
    fun pendiente(pct: Double): ByteArray {
        require(pct in PENDIENTE_MIN..PENDIENTE_MAX) { "Pendiente fuera del rango FE-C: $pct %" }
        val g = ((pct + 200) * 100).roundToInt()
        return mensaje(PAGINA_PENDIENTE, 0xFF, 0xFF, 0xFF, 0xFF, g and 0xFF, g shr 8, 0xFF)
    }

    /** Página 0x31 (Target Power): potencia en W con resolución de 0,25 W. */
    fun potencia(vatios: Int): ByteArray {
        require(vatios in 0..POTENCIA_MAX) { "Potencia fuera del rango FE-C: $vatios W" }
        val p = vatios * 4
        return mensaje(PAGINA_POTENCIA, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, p and 0xFF, p shr 8)
    }

    private fun mensaje(pagina: Int, vararg datos: Int): ByteArray {
        val m = intArrayOf(0xA4, 0x09, 0x4E, 0x05, pagina, *datos)
        val chk = m.fold(0) { acc, b -> acc xor b }
        return ByteArray(m.size + 1) { if (it < m.size) m[it].toByte() else chk.toByte() }
    }
}
