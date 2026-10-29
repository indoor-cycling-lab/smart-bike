package app.smartbike.protocolo

import java.io.ByteArrayOutputStream

/** Botones de un mando Zwift Ride (arquitectura.md § 7.4). `_I` y `_D`: mando izquierdo y derecho. */
enum class BotonMando {
    IZQUIERDA,
    ARRIBA,
    DERECHA,
    ABAJO,
    A,
    B,
    Y,
    Z,
    CAMBIO_SUBIR_I,
    CAMBIO_BAJAR_I,
    POWERUP_I,
    ENCENDIDO_I,
    CAMBIO_SUBIR_D,
    CAMBIO_BAJAR_D,
    POWERUP_D,
    ENCENDIDO_D,
}

/**
 * Valor de cada botón en `ButtonMap`. Las fuentes (el `.proto` y el enum escrito a mano) coinciden hasta `Y` y no a
 * partir de `Z`; la app usa la del enum escrito a mano, que es la que funcionó con Zwift (design.md del change
 * `prototipo-mando-zwift`, decisión 2).
 */
enum class TablaBotones(private val desdeZ: List<Int>) {
    ENUM_MANUAL(listOf(0x80, 0x100, 0x200, 0x400, 0x800, 0x1000, 0x2000, 0x4000, 0x8000)),
    ;

    fun valor(b: BotonMando): Int = if (b.ordinal < Z) 1 shl b.ordinal else desdeZ[b.ordinal - Z]

    private companion object {
        val Z = BotonMando.Z.ordinal
    }
}

/** Cómo se dice a Zwift que se responde al handshake. */
enum class RespuestaHandshake {
    /** `RideOn` solo. */
    PELADO,
}

/** Cómo se responde a la petición de información de Zwift (`00 08 <id>`), cuyo formato no está publicado. */
enum class RespuestaInfo {
    /** `3C 08 <id>`: solo el identificador pedido, sin datos. */
    VACIA,

    /**
     * Las páginas con los campos del protobuf del protocolo: a la 0 (`PAGE_DEV_INFO`), una `DevInfoPage` completa de un
     * Ride izquierdo (producto 8, «Zwift SF2»); a la 16 (`PAGE_CLIENT_SERVER_CONFIGURATION`), notificaciones 0; a las
     * demás, solo el identificador. Por DirCon no hay anuncio BLE: esta página es lo único que identifica al mando
     * (design.md del change `mando-zwift-emulado`, decisión 0).
     */
    PAGINA_RIDE,
}

/**
 * Tramas del servicio de mando Zwift Ride (arquitectura.md § 7.4; design.md del change `prototipo-mando-zwift`,
 * decisión 2): handshake `RideOn` y `RideKeyPadStatus` con la máscara invertida. Protobuf codificado a mano.
 */
object MandoZwiftCodec {
    /** `Opcode.CONTROLLER_NOTIFICATION`. */
    const val NOTIFICACION_MANDO = 0x23

    val RIDE_ON = "RideOn".toByteArray(Charsets.US_ASCII)

    fun esHandshake(valor: ByteArray): Boolean = valor.size >= RIDE_ON.size && RIDE_ON.indices.all { valor[it] == RIDE_ON[it] }

    fun respuestaHandshake(variante: RespuestaHandshake): ByteArray = when (variante) {
        RespuestaHandshake.PELADO -> RIDE_ON.copyOf()
    }

    /** Opcode de la petición de información y de su respuesta. */
    const val PETICION_INFO = 0x00
    const val RESPUESTA_INFO = 0x3C

    /** Nombre con el que se anuncia un Zwift Ride real. */
    const val NOMBRE_RIDE = "Zwift SF2"

    /** El identificador pedido si [valor] es una petición de información (`00 08 <varint>`), o `null`. */
    fun peticionInfo(valor: ByteArray): Long? {
        if (valor.size < 3 || valor[0].toInt() != PETICION_INFO || valor[1].toInt() != 0x08) return null
        var v = 0L
        var desplazamiento = 0
        for (i in 2 until valor.size) {
            v = v or ((valor[i].toLong() and 0x7F) shl desplazamiento)
            if (valor[i].toInt() and 0x80 == 0) return if (i == valor.size - 1) v else null
            desplazamiento += 7
            if (desplazamiento > 63) return null
        }
        return null
    }

    /** Respuesta a la petición de información de [id]. */
    fun respuestaInfo(variante: RespuestaInfo, id: Long): ByteArray {
        val out = ByteArrayOutputStream()
        out.write(RESPUESTA_INFO)
        out.write(0x08)
        varint(out, id)
        when (variante) {
            RespuestaInfo.PAGINA_RIDE -> when (id) {
                PAGINA_DISPOSITIVO -> {
                    val pagina = paginaDispositivo(NOMBRE_RIDE, PRODUCTO_RIDE_IZQUIERDO)
                    // `DevInfo`: la página va en su campo 1.
                    conLongitud(out, 2, ByteArrayOutputStream().apply { conLongitud(this, 1, pagina) }.toByteArray())
                }

                // `ClientServerCfgPage` con `notifications` (campo 1) a 0.
                PAGINA_CLIENTE_SERVIDOR -> conLongitud(out, 2, byteArrayOf(0x08, 0x00))

                PAGINA_ENTRADAS -> conLongitud(out, 2, paginaEntradas())

                // Observada en la captura de BikeControl; se desconoce su significado.
                PAGINA_771 -> conLongitud(
                    out,
                    2,
                    ByteArrayOutputStream().apply {
                        conLongitud(this, 1, byteArrayOf(0x08, 0x00, 0x10, 0x64, 0x18, 0x00, 0x20, 0x00))
                    }.toByteArray(),
                )

                else -> Unit
            }

            RespuestaInfo.VACIA -> Unit
        }
        return out.toByteArray()
    }

    /** Identificadores de las páginas que pide Zwift. */
    const val PAGINA_DISPOSITIVO = 0L
    const val PAGINA_CLIENTE_SERVIDOR = 16L
    const val PAGINA_ENTRADAS = 1024L
    const val PAGINA_771 = 771L

    /** Botones que declara la página 1024 observada (`0x11ff`). */
    private const val BOTONES_DECLARADOS = 0x11ffL

    /** Producto en la `DevInfoPage`: el mismo número que el tipo de los datos de fabricante del anuncio BLE. */
    const val PRODUCTO_RIDE_IZQUIERDO = 0x08

    /** Identificador propio del mando emulado (no es la dirección de ningún mando real). */
    const val UID_MANDO = "0253514D4102"

    /**
     * `DevInfoPage`: versión de protocolo 515 (1), firmware `00 00 01 01` (2), nombre (3), serie `<producto>-<UID>`
     * (6), hardware `A.0` (7), capacidades {tipo 2, 1} (8), fabricante 1 (9), producto (10) y UID (11).
     */
    private fun paginaDispositivo(nombre: String, producto: Int): ByteArray = ByteArrayOutputStream().apply {
        write(0x08)
        varint(this, VERSION_PROTOCOLO)
        conLongitud(this, 2, byteArrayOf(0x00, 0x00, 0x01, 0x01))
        conLongitud(this, 3, nombre.toByteArray(Charsets.US_ASCII))
        conLongitud(this, 6, "%02X-%s".format(producto, UID_MANDO).toByteArray(Charsets.US_ASCII))
        conLongitud(this, 7, "A.0".toByteArray(Charsets.US_ASCII))
        conLongitud(this, 8, byteArrayOf(0x08, 0x02, 0x10, 0x01))
        write(0x48)
        varint(this, 1)
        write(0x50)
        varint(this, producto.toLong())
        conLongitud(this, 11, UID_MANDO.toByteArray(Charsets.US_ASCII))
    }.toByteArray()

    private const val VERSION_PROTOCOLO = 515L

    /**
     * Página 1024, como la de BikeControl: un grupo (campo 1) con los botones declarados (1) y ninguna palanca
     * analógica (2, a 0). Zwift sigue atendiendo el freno y el giro por palancas con ella (design.md del change
     * `completar-protocolo-dircon-zwift`, decisión 1).
     */
    private fun paginaEntradas(): ByteArray = ByteArrayOutputStream().apply {
        conLongitud(
            this,
            1,
            ByteArrayOutputStream().apply {
                write(0x08)
                varint(this, BOTONES_DECLARADOS)
                write(0x10)
                varint(this, 0)
            }.toByteArray(),
        )
    }.toByteArray()

    /**
     * `0x23` + `RideKeyPadStatus` con [pulsados] a 0 en `ButtonMap` (lógica invertida) y, detrás, las [palancas]
     * (`Location` → `AnalogValue`, `sint32`) en `AnalogPaddles` (campo 3), una pulsación por palanca en orden de
     * ubicación. El giro y el freno van como palancas analógicas, no en `ButtonMap`: Zwift se queda con el último valor
     * de cada una hasta recibir otro.
     */
    fun botones(pulsados: Set<BotonMando>, tabla: TablaBotones, palancas: Map<Int, Int> = emptyMap()): ByteArray {
        val mascara = pulsados.fold(0) { m, b -> m or tabla.valor(b) }
        val out = ByteArrayOutputStream()
        out.write(NOTIFICACION_MANDO)
        out.write(0x08)
        varint(out, mascara.inv().toLong() and 0xFFFFFFFFL)
        palancas.toSortedMap().forEach { (ubicacion, valor) -> conLongitud(out, 3, pulsacion(ubicacion, valor)) }
        return out.toByteArray()
    }

    private fun pulsacion(ubicacion: Int, valor: Int): ByteArray = ByteArrayOutputStream().apply {
        write(0x08)
        varint(this, ubicacion.toLong())
        write(0x10)
        varint(this, zigzag(valor))
    }.toByteArray()

    private fun conLongitud(out: ByteArrayOutputStream, campo: Int, contenido: ByteArray) {
        out.write(campo shl 3 or 2)
        varint(out, contenido.size.toLong())
        out.write(contenido)
    }

    private fun zigzag(n: Int): Long = ((n shl 1) xor (n shr 31)).toLong() and 0xFFFFFFFFL

    private fun varint(out: ByteArrayOutputStream, valor: Long) {
        var v = valor
        do {
            var b = (v and 0x7F).toInt()
            v = v ushr 7
            if (v != 0L) b = b or 0x80
            out.write(b)
        } while (v != 0L)
    }
}
