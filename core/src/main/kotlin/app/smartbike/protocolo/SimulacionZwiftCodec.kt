package app.smartbike.protocolo

import app.smartbike.dominio.SimulacionZwift
import java.io.ByteArrayOutputStream

/** Un `Set` recibido de Zwift: opciones, identificador del mensaje y, si lo trae, el `TrainerSimulationParam`. */
data class SetZwift(val opciones: Int, val idMensaje: Int, val simulacion: SimulacionZwift?)

/**
 * `Set` (`0x3F`) con `TrainerSimulationParam` y `SetResponse` (`0x40`) del protocolo de Zwift, codificados a mano a
 * partir de su descripción pública (design.md del change `mando-zwift-dircon`, decisión 2; NF6). Tolera campos
 * desconocidos y devuelve `null` ante un mensaje truncado.
 */
object SimulacionZwiftCodec {
    const val OPCODE_SET = 0x3F
    const val OPCODE_SET_RESPONSE = 0x40

    private const val CAMPO_OPCIONES = 1
    private const val CAMPO_ID = 2
    private const val CAMPO_ESTADO = 1
    private const val CAMPO_PARAMETROS = 4

    fun esSet(valor: ByteArray): Boolean = valor.isNotEmpty() && valor[0].toInt() == OPCODE_SET

    fun decodificar(valor: ByteArray): SetZwift? {
        if (!esSet(valor)) return null
        var opciones = 0
        var id = 0
        var simulacion: SimulacionZwift? = null
        val ok = campos(valor, 1, valor.size) { campo, entero, desde, hasta ->
            when (campo) {
                CAMPO_OPCIONES -> entero?.let { opciones = it.toInt() }
                CAMPO_ID -> entero?.let { id = it.toInt() }
                CAMPO_PARAMETROS -> if (entero == null) simulacion = parametros(valor, desde, hasta) ?: return null
            }
            true
        }
        return if (ok) SetZwift(opciones, id, simulacion) else null
    }

    /** `SetResponse` con `status` 0 (campo 1) y el `msgId` (campo 2), `40 08 00 10 <msgId>`; con eso Zwift no repite el `SET`. */
    fun respuesta(set: SetZwift): ByteArray = ByteArrayOutputStream().apply {
        write(OPCODE_SET_RESPONSE)
        write(CAMPO_ESTADO shl 3)
        write(0)
        write(CAMPO_ID shl 3)
        varint(this, set.idMensaje.toLong())
    }.toByteArray()

    private fun parametros(b: ByteArray, desde: Int, hasta: Int): SimulacionZwift? {
        var s = SimulacionZwift()
        val ok = campos(b, desde, hasta) { campo, v, _, _ ->
            if (v != null) {
                s = when (campo) {
                    1 -> s.copy(resistencia = v.toInt())
                    2 -> s.copy(potenciaErgW = v.toInt())
                    3 -> s.copy(ventanaPromedio = v.toInt())
                    4 -> s.copy(vientoBruto = deZigzag(v))
                    5 -> s.copy(pendientePct = deZigzag(v) / 100.0)
                    6 -> s.copy(relacionReal = v.toInt())
                    7 -> s.copy(relacionVirtual = v / 10_000.0)
                    8 -> s.copy(cw = v / 10_000.0)
                    9 -> s.copy(diametroRueda = v.toInt())
                    10 -> s.copy(masaBiciKg = v / 100.0)
                    11 -> s.copy(masaCiclistaKg = v / 100.0)
                    12 -> s.copy(crr = v / 100_000.0)
                    13 -> s.copy(areaFrontal = v.toInt())
                    14 -> s.copy(frenoElectronico = v.toInt())
                    else -> s
                }
            }
            true
        }
        return if (ok) s else null
    }

    /**
     * Recorre los campos de [b] entre [desde] y [hasta]. [alCampo] recibe el número de campo, el valor si es un varint
     * (`null` si trae longitud) y los límites del contenido. Los campos de 32 y 64 bits se saltan. Falso si se trunca.
     */
    private inline fun campos(b: ByteArray, desde: Int, hasta: Int, alCampo: (Int, Long?, Int, Int) -> Boolean): Boolean {
        var i = desde
        while (i < hasta) {
            val (clave, despuesClave) = leerVarint(b, i, hasta) ?: return false
            i = despuesClave
            val campo = (clave shr 3).toInt()
            when ((clave and 7).toInt()) {
                0 -> {
                    val (v, sig) = leerVarint(b, i, hasta) ?: return false
                    i = sig
                    if (!alCampo(campo, v, i, i)) return false
                }

                2 -> {
                    val (largo, sig) = leerVarint(b, i, hasta) ?: return false
                    if (largo < 0 || sig + largo > hasta) return false
                    if (!alCampo(campo, null, sig, sig + largo.toInt())) return false
                    i = sig + largo.toInt()
                }

                1 -> {
                    i += 8
                    if (i > hasta) return false
                }

                5 -> {
                    i += 4
                    if (i > hasta) return false
                }

                else -> return false
            }
        }
        return true
    }

    private fun leerVarint(b: ByteArray, desde: Int, hasta: Int): Pair<Long, Int>? {
        var v = 0L
        var desplazamiento = 0
        var i = desde
        while (i < hasta && desplazamiento <= 63) {
            v = v or ((b[i].toLong() and 0x7F) shl desplazamiento)
            i++
            if (b[i - 1].toInt() and 0x80 == 0) return v to i
            desplazamiento += 7
        }
        return null
    }

    private fun deZigzag(v: Long): Int = ((v ushr 1) xor -(v and 1)).toInt()

    private fun varint(out: ByteArrayOutputStream, valor: Long) {
        var v = valor
        do {
            var x = (v and 0x7F).toInt()
            v = v ushr 7
            if (v != 0L) x = x or 0x80
            out.write(x)
        } while (v != 0L)
    }
}
