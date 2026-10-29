package app.smartbike.protocolo

import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.roundToInt

/**
 * Tramas FTMS (Fitness Machine, `0x1826`) y Cycling Power (`0x1818`) que la app ofrece a Zwift (arquitectura.md § 7.3;
 * design.md del change `puente-zwift-wifi`, decisión 4). Todo en *little endian*.
 */
object FtmsCodec {
    const val SERVICIO_FTMS = 0x1826
    const val SERVICIO_POTENCIA = 0x1818
    const val SERVICIO_INFORMACION = 0x180A
    const val FEATURE = 0x2ACC
    const val INDOOR_BIKE_DATA = 0x2AD2
    const val TRAINING_STATUS = 0x2AD3
    const val RANGO_POTENCIA = 0x2AD8
    const val PUNTO_CONTROL = 0x2AD9
    const val STATUS = 0x2ADA
    const val CP_MEDIDA = 0x2A63
    const val CP_FEATURE = 0x2A65
    const val CP_UBICACION = 0x2A5D
    const val FABRICANTE = 0x2A29
    const val MODELO = 0x2A24
    const val VERSION_SOFTWARE = 0x2A28
    const val SERIE = 0x2A25
    const val VERSION_FIRMWARE = 0x2A26
    const val VERSION_HARDWARE = 0x2A27

    const val POTENCIA_MAX_RANGO = 2000

    /** Resultados de la respuesta del punto de control. */
    const val EXITO = 0x01
    const val NO_ADMITIDO = 0x02
    const val PARAMETRO_INVALIDO = 0x03

    /** Lo que pide Zwift por el punto de control (`0x2AD9`). */
    sealed interface Comando {
        val opcode: Int

        data object PedirControl : Comando {
            override val opcode = 0x00
        }
        data object Reiniciar : Comando {
            override val opcode = 0x01
        }
        data class PotenciaObjetivo(val vatios: Int) : Comando {
            override val opcode = 0x05
        }
        data object Iniciar : Comando {
            override val opcode = 0x07
        }
        data class Parar(val pausa: Boolean) : Comando {
            override val opcode = 0x08
        }

        /** Indoor Bike Simulation: viento en m/s, pendiente en %, rodadura y coeficiente de viento en kg/m. */
        data class Simulacion(val vientoMs: Double, val pendientePct: Double, val crr: Double, val cw: Double) : Comando {
            override val opcode = 0x11
        }

        data class NoAdmitido(override val opcode: Int) : Comando
        data class Invalido(override val opcode: Int) : Comando
    }

    /** Fitness Machine Feature: cadencia y potencia medidas; objetivo de potencia y simulación en interior. */
    fun feature(): ByteArray = le {
        putInt(1 shl 1 or (1 shl 14))
        putInt(1 shl 3 or (1 shl 13))
    }

    /** Supported Power Range: 0–[POTENCIA_MAX_RANGO] W en pasos de 1 W. */
    fun rangoPotencia(): ByteArray = le {
        putShort(0)
        putShort(POTENCIA_MAX_RANGO.toShort())
        putShort(1)
    }

    /** Training Status: sin cadena, en reposo. */
    fun trainingStatus(): ByteArray = byteArrayOf(0x00, 0x01)

    /** Cycling Power Feature: datos de biela. */
    fun cpFeature(): ByteArray = le { putInt(1 shl 3) }

    /** Sensor Location: buje trasero. */
    fun cpUbicacion(): ByteArray = byteArrayOf(0x0D)

    /** Indoor Bike Data con velocidad (0,01 km/h), cadencia (0,5 rpm) y potencia (W) instantáneas. */
    fun indoorBikeData(velocidadKmh: Double, cadenciaRpm: Int, vatios: Int): ByteArray = le {
        putShort(0x0044)
        putShort((velocidadKmh * 100).roundToInt().coerceIn(0, 0xFFFF).toShort())
        putShort((cadenciaRpm * 2).coerceIn(0, 0xFFFF).toShort())
        putShort(vatios.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort())
    }

    /** Cycling Power Measurement con la potencia y, si los hay, los datos de biela tal como llegan del Neo. */
    fun cyclingPower(vatios: Int, biela: TelemetriaCodec.Biela?): ByteArray = le {
        putShort(if (biela != null) 0x20 else 0x00)
        putShort(vatios.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort())
        if (biela != null) {
            putShort(biela.revoluciones.toShort())
            putShort(biela.hora1024.toShort())
        }
    }

    fun comando(datos: ByteArray): Comando {
        if (datos.isEmpty()) return Comando.Invalido(-1)
        val op = datos[0].toInt() and 0xFF
        val b = ByteBuffer.wrap(datos).order(ByteOrder.LITTLE_ENDIAN)
        return when (op) {
            0x00 -> Comando.PedirControl

            0x01 -> Comando.Reiniciar

            0x05 -> if (datos.size >= 3) Comando.PotenciaObjetivo(b.getShort(1).toInt()) else Comando.Invalido(op)

            0x07 -> Comando.Iniciar

            0x08 -> Comando.Parar(pausa = datos.size >= 2 && datos[1].toInt() == 0x02)

            0x11 -> if (datos.size >= 7) {
                Comando.Simulacion(
                    vientoMs = b.getShort(1) / 1000.0,
                    pendientePct = b.getShort(3) / 100.0,
                    crr = (datos[5].toInt() and 0xFF) / 10_000.0,
                    cw = (datos[6].toInt() and 0xFF) / 100.0,
                )
            } else {
                Comando.Invalido(op)
            }

            else -> Comando.NoAdmitido(op)
        }
    }

    /** Respuesta del punto de control: `80 <opcode> <resultado>`. */
    fun respuesta(opcode: Int, resultado: Int): ByteArray = byteArrayOf(0x80.toByte(), opcode.toByte(), resultado.toByte())

    private inline fun le(cuerpo: ByteBuffer.() -> Unit): ByteArray {
        val b = ByteBuffer.allocate(16).order(ByteOrder.LITTLE_ENDIAN)
        b.cuerpo()
        return b.array().copyOf(b.position())
    }
}
