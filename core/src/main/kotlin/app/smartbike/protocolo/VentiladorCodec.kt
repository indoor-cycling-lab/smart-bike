package app.smartbike.protocolo

import app.smartbike.dominio.ConfiguracionVentilador

/**
 * Lo que el ventilador notifica por su control point (design.md del change `ventilador-headwind`, decisión 2): `FD 01 SS MM`
 * cada ~1 s y tras cada cambio, y `FE xx ST …` como respuesta a la orden `xx`, con `ST = 01` si fue bien.
 */
sealed interface NotificacionVentilador {
    /** `FD 01 SS MM`: velocidad en % y código de modo. */
    data class Estado(val velocidad: Int, val modo: Int) : NotificacionVentilador

    /** `FE 01 ST SS`: respuesta a «leer velocidad». */
    data class Velocidad(val correcto: Boolean, val velocidad: Int) : NotificacionVentilador

    /** `FE 03 ST MM`: respuesta a «leer modo». */
    data class Modo(val correcto: Boolean, val modo: Int) : NotificacionVentilador

    /** `FE 05 ST …`: respuesta a «leer configuración», con los bytes de la configuración tal cual llegaron. */
    data class Configuracion(val correcto: Boolean, val bytes: ByteArray) : NotificacionVentilador {
        override fun equals(other: Any?) = other is Configuracion && correcto == other.correcto && bytes.contentEquals(other.bytes)
        override fun hashCode() = 31 * correcto.hashCode() + bytes.contentHashCode()
    }

    /** `FE xx ST` de una orden de escritura (`02` velocidad, `04` modo, `06` configuración). */
    data class Confirmacion(val orden: Int, val correcto: Boolean) : NotificacionVentilador
}

/** Protocolo Bluetooth del Headwind, no oficial y **a validar** con el ventilador real (design.md, decisión 2). */
object VentiladorCodec {
    const val MODO_PULSO = 2
    const val MODO_MANUAL = 4

    private const val LEER_VELOCIDAD = 0x01
    private const val FIJAR_VELOCIDAD = 0x02
    private const val LEER_MODO = 0x03
    private const val FIJAR_MODO = 0x04
    private const val LEER_CONFIGURACION = 0x05
    private const val ESCRIBIR_CONFIGURACION = 0x06
    private const val ESTADO = 0xFD
    private const val RESPUESTA = 0xFE
    private const val OK = 0x01

    fun leerVelocidad(): ByteArray = byteArrayOf(LEER_VELOCIDAD.toByte())

    fun leerModo(): ByteArray = byteArrayOf(LEER_MODO.toByte())

    fun leerConfiguracion(): ByteArray = byteArrayOf(LEER_CONFIGURACION.toByte())

    fun velocidad(porcentaje: Int): ByteArray = byteArrayOf(FIJAR_VELOCIDAD.toByte(), porcentaje.coerceIn(0, 100).toByte())

    fun modo(codigo: Int): ByteArray = byteArrayOf(FIJAR_MODO.toByte(), codigo.toByte())

    fun escribirConfiguracion(config: ConfiguracionVentilador): ByteArray = byteArrayOf(ESCRIBIR_CONFIGURACION.toByte()) + config.bytes

    /** `null` si el paquete es corto o desconocido. */
    fun decodificar(bytes: ByteArray): NotificacionVentilador? {
        if (bytes.size < 2) return null
        val b = IntArray(bytes.size) { bytes[it].toInt() and 0xFF }
        return when (b[0]) {
            ESTADO -> if (b[1] == 0x01 && b.size >= 4) NotificacionVentilador.Estado(b[2], b[3]) else null

            RESPUESTA -> {
                if (b.size < 3) return null
                val ok = b[2] == OK
                when (b[1]) {
                    LEER_VELOCIDAD -> if (b.size >= 4) NotificacionVentilador.Velocidad(ok, b[3]) else null
                    LEER_MODO -> if (b.size >= 4) NotificacionVentilador.Modo(ok, b[3]) else null
                    LEER_CONFIGURACION -> NotificacionVentilador.Configuracion(ok, bytes.copyOfRange(3, bytes.size))
                    FIJAR_VELOCIDAD, FIJAR_MODO, ESCRIBIR_CONFIGURACION -> NotificacionVentilador.Confirmacion(b[1], ok)
                    else -> null
                }
            }

            else -> null
        }
    }
}
