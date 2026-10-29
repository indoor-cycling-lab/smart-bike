package app.smartbike.protocolo

import java.io.ByteArrayOutputStream

/**
 * Datos de pedaleo del rodillo en el protocolo de Zwift: `TRAINER_NOTIF` (`0x03`) con `TrainerNotification`, cuyos cinco
 * primeros campos son obligatorios: potencia (1), cadencia (2), velocidad de la bici (3), potencia media (4) y velocidad
 * de la rueda (5). Un rodillo con marchas virtuales los manda por el servicio `FC82` en lugar de por FTMS (design.md del
 * change `mando-zwift-dircon`, decisión 0; a validar, también las unidades de las dos velocidades).
 */
object PedaleoZwiftCodec {
    const val OPCODE_DATOS_RODILLO = 0x03

    /** `03` + los cinco campos varint; la potencia media, si no se da, es la instantánea. */
    fun datos(
        vatios: Int,
        cadenciaRpm: Int,
        velocidadBici: Int = 0,
        potenciaMediaW: Int = vatios,
        velocidadRueda: Int = 0,
    ): ByteArray = ByteArrayOutputStream().apply {
        write(OPCODE_DATOS_RODILLO)
        campo(this, 1, vatios)
        campo(this, 2, cadenciaRpm)
        campo(this, 3, velocidadBici)
        campo(this, 4, potenciaMediaW)
        campo(this, 5, velocidadRueda)
    }.toByteArray()

    private fun campo(out: ByteArrayOutputStream, numero: Int, valor: Int) {
        out.write(numero shl 3)
        var v = valor.toLong().coerceAtLeast(0)
        do {
            var b = (v and 0x7F).toInt()
            v = v ushr 7
            if (v != 0L) b = b or 0x80
            out.write(b)
        } while (v != 0L)
    }
}
