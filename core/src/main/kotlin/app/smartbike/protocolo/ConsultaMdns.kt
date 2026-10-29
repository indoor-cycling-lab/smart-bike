package app.smartbike.protocolo

import java.io.ByteArrayOutputStream

/**
 * La pregunta con la que la app comprueba que su anuncio responde en la red (design.md del change
 * `comprobar-anuncio-zwift`, decisión 1): el registro SRV de su instancia, con el bit QU para que el responder conteste
 * por unicast al puerto que pregunta. Los nombres DNS no distinguen mayúsculas y la respuesta puede comprimirlos.
 */
object ConsultaMdns {
    const val TIPO_SRV = 33
    private const val CLASE_IN = 1
    private const val BIT_QU = 0x8000
    private const val BIT_RESPUESTA = 0x8000
    private const val CABECERA = 12
    private const val MAX_SALTOS = 32

    /** El nombre completo de [instancia] de [tipo] (`_servicio._tcp`) en `local`, por etiquetas. */
    fun etiquetas(instancia: String, tipo: String): List<String> = listOf(instancia) + tipo.split('.') + "local"

    /** Consulta del SRV de [instancia] de [tipo], con el bit QU. */
    fun consultaSrv(instancia: String, tipo: String): ByteArray {
        val out = ByteArrayOutputStream()
        // Identificador 0, sin indicadores y una pregunta (RFC 6762 § 18).
        out.write(byteArrayOf(0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0))
        for (e in etiquetas(instancia, tipo)) {
            val b = e.toByteArray(Charsets.UTF_8)
            require(b.size in 1..63) { "Etiqueta DNS no válida: «$e»" }
            out.write(b.size)
            out.write(b)
        }
        out.write(0)
        escribir16(out, TIPO_SRV)
        escribir16(out, BIT_QU or CLASE_IN)
        return out.toByteArray()
    }

    /**
     * El puerto del SRV de [instancia] de [tipo] en [paquete], en cualquier sección, o `null` si no es una respuesta,
     * no lo trae o está mal formada.
     */
    fun puertoSrv(paquete: ByteArray, instancia: String, tipo: String): Int? {
        val buscado = etiquetas(instancia, tipo).map { it.lowercase() }
        return try {
            if (paquete.size < CABECERA || leer16(paquete, 2) and BIT_RESPUESTA == 0) return null
            val preguntas = leer16(paquete, 4)
            val registros = leer16(paquete, 6) + leer16(paquete, 8) + leer16(paquete, 10)
            var pos = CABECERA
            repeat(preguntas) { pos = saltarNombre(paquete, pos) + 4 }
            repeat(registros) {
                val (nombre, fin) = leerNombre(paquete, pos)
                val tipoRegistro = leer16(paquete, fin)
                val longitud = leer16(paquete, fin + 8)
                val datos = fin + 10
                if (datos + longitud > paquete.size) return null
                if (tipoRegistro == TIPO_SRV && longitud >= 6 && nombre.map { it.lowercase() } == buscado) {
                    return leer16(paquete, datos + 4)
                }
                pos = datos + longitud
            }
            null
        } catch (_: IndexOutOfBoundsException) {
            null
        }
    }

    private fun leerNombre(p: ByteArray, inicio: Int): Pair<List<String>, Int> {
        val etiquetas = mutableListOf<String>()
        var pos = inicio
        var fin = -1
        var saltos = 0
        while (true) {
            val n = p[pos].toInt() and 0xFF
            when {
                n == 0 -> return etiquetas to (if (fin < 0) pos + 1 else fin)

                n and 0xC0 == 0xC0 -> {
                    if (++saltos > MAX_SALTOS) throw IndexOutOfBoundsException("Bucle de compresión")
                    if (fin < 0) fin = pos + 2
                    pos = ((n and 0x3F) shl 8) or (p[pos + 1].toInt() and 0xFF)
                }

                else -> {
                    if (pos + 1 + n > p.size) throw IndexOutOfBoundsException("Etiqueta truncada")
                    etiquetas += String(p, pos + 1, n, Charsets.UTF_8)
                    pos += 1 + n
                }
            }
        }
    }

    private fun saltarNombre(p: ByteArray, inicio: Int) = leerNombre(p, inicio).second

    private fun leer16(p: ByteArray, i: Int) = ((p[i].toInt() and 0xFF) shl 8) or (p[i + 1].toInt() and 0xFF)

    private fun escribir16(out: ByteArrayOutputStream, v: Int) {
        out.write(v shr 8 and 0xFF)
        out.write(v and 0xFF)
    }
}
