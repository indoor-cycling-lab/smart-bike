package app.smartbike.protocolo

/**
 * Medida de frecuencia cardiaca estándar (`0x2A37`; design.md del change `pulso-zwift`, decisión 2). Se lee el pulso de lo
 * que da la banda y se escribe uno propio, siempre igual, para Zwift: no se reenvía el paquete tal cual.
 */
object PulsoCodec {
    private const val BANDERA_16_BITS = 0x01

    /** Los bpm del paquete, en formato de 8 o de 16 bits según el bit 0 de las banderas; `null` si es demasiado corto. */
    fun decodificar(bytes: ByteArray): Int? {
        if (bytes.isEmpty()) return null
        return if (bytes[0].toInt() and BANDERA_16_BITS == 0) {
            if (bytes.size < 2) null else bytes[1].toInt() and 0xFF
        } else {
            if (bytes.size < 3) null else (bytes[1].toInt() and 0xFF) or ((bytes[2].toInt() and 0xFF) shl 8)
        }
    }

    /** Formato de 8 bits, sin contacto, sin energía y sin intervalos RR. */
    fun medida(bpm: Int): ByteArray = byteArrayOf(0x00, bpm.toByte())
}
