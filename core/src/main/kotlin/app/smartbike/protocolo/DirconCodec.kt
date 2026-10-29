package app.smartbike.protocolo

import java.nio.ByteBuffer
import java.util.UUID

/**
 * Mensajes DirCon (*Direct Connect* de Wahoo): los mismos servicios GATT sobre TCP en la red local
 * (arquitectura.md § 7.3). Cada mensaje es una cabecera de 6 bytes (versión, identificador, número de secuencia,
 * código de respuesta y longitud del contenido en `uint16` *big endian*) seguida del contenido. Los UUID van siempre
 * en 128 bits.
 *
 * **A validar** con Zwift en PC (design.md del change `puente-zwift-wifi`, decisión 2): no hay especificación
 * pública, así que los identificadores, los códigos y las propiedades salen de la descripción del protocolo.
 */
object DirconCodec {
    const val VERSION = 1
    const val CABECERA = 6
    const val CONTENIDO_MAX = 65_535

    const val DESCUBRIR_SERVICIOS = 0x01
    const val DESCUBRIR_CARACTERISTICAS = 0x02
    const val LEER = 0x03
    const val ESCRIBIR = 0x04
    const val NOTIFICACIONES = 0x05
    const val NOTIFICACION = 0x06

    /** Códigos de respuesta. */
    const val EXITO = 0
    const val MENSAJE_DESCONOCIDO = 1
    const val ERROR_INESPERADO = 2
    const val SERVICIO_NO_ENCONTRADO = 3
    const val CARACTERISTICA_NO_ENCONTRADA = 4
    const val OPERACION_NO_ADMITIDA = 5

    /** Propiedades de una característica en el descubrimiento. */
    const val PROP_LEER = 0x01
    const val PROP_ESCRIBIR = 0x02
    const val PROP_NOTIFICAR = 0x04

    class Mensaje(val tipo: Int, val secuencia: Int, val codigo: Int, val contenido: ByteArray) {
        override fun toString() = "Mensaje(tipo=$tipo, sec=$secuencia, cod=$codigo, ${contenido.joinToString("") { "%02x".format(it) }})"
    }

    /** Lo que pide el cliente (Zwift), ya interpretado; [secuencia] se devuelve en la respuesta. */
    sealed interface Peticion {
        val secuencia: Int

        data class DescubrirServicios(override val secuencia: Int) : Peticion
        data class DescubrirCaracteristicas(override val secuencia: Int, val servicio: UUID) : Peticion
        data class Leer(override val secuencia: Int, val caracteristica: UUID) : Peticion
        class Escribir(override val secuencia: Int, val caracteristica: UUID, val valor: ByteArray) : Peticion
        data class Suscribir(override val secuencia: Int, val caracteristica: UUID, val activar: Boolean) : Peticion

        /** Un tipo que no se conoce o un contenido que no se entiende: se responde con [codigo]. */
        data class Desconocida(override val secuencia: Int, val tipo: Int, val codigo: Int) : Peticion
    }

    /** Una característica en la respuesta al descubrimiento, con las propiedades `PROP_*`. */
    data class Caracteristica(val uuid: UUID, val propiedades: Int)

    /** Mitad baja de la base de Bluetooth `0000xxxx-0000-1000-8000-00805f9b34fb`. */
    private val BASE_BLUETOOTH = UUID.fromString("00000000-0000-1000-8000-00805f9b34fb").leastSignificantBits

    /** UUID de 128 bits de un UUID corto de Bluetooth (`0x1826`). */
    fun uuid16(corto: Int): UUID = UUID((corto.toLong() and 0xFFFF) shl 32 or 0x1000, BASE_BLUETOOTH)

    /** El UUID corto (`2ad9`) si es de la base de Bluetooth, o el completo; para el registro. */
    fun nombreCorto(u: UUID): String {
        val alto = u.mostSignificantBits
        val esCorto = u.leastSignificantBits == BASE_BLUETOOTH && alto ushr 48 == 0L && alto and 0xFFFF_FFFFL == 0x1000L
        return if (esCorto) "%04x".format(alto ushr 32) else u.toString()
    }

    fun codificar(m: Mensaje): ByteArray {
        require(m.contenido.size <= 0xFFFF) { "Contenido demasiado largo: ${m.contenido.size}" }
        val b = ByteBuffer.allocate(CABECERA + m.contenido.size)
        b.put(VERSION.toByte()).put(m.tipo.toByte()).put(m.secuencia.toByte()).put(m.codigo.toByte())
        b.putShort(m.contenido.size.toShort()).put(m.contenido)
        return b.array()
    }

    /** Interpreta un mensaje recibido del cliente. */
    fun peticion(m: Mensaje): Peticion {
        val c = m.contenido
        val s = m.secuencia
        return when (m.tipo) {
            DESCUBRIR_SERVICIOS -> Peticion.DescubrirServicios(s)

            DESCUBRIR_CARACTERISTICAS ->
                if (c.size >= 16) Peticion.DescubrirCaracteristicas(s, uuid(c, 0)) else malformada(m)

            LEER -> if (c.size >= 16) Peticion.Leer(s, uuid(c, 0)) else malformada(m)

            ESCRIBIR -> if (c.size >= 16) Peticion.Escribir(s, uuid(c, 0), c.copyOfRange(16, c.size)) else malformada(m)

            NOTIFICACIONES -> if (c.size >= 17) Peticion.Suscribir(s, uuid(c, 0), c[16].toInt() != 0) else malformada(m)

            else -> Peticion.Desconocida(s, m.tipo, MENSAJE_DESCONOCIDO)
        }
    }

    private fun malformada(m: Mensaje) = Peticion.Desconocida(m.secuencia, m.tipo, ERROR_INESPERADO)

    fun respuestaServicios(secuencia: Int, servicios: List<UUID>) = Mensaje(DESCUBRIR_SERVICIOS, secuencia, EXITO, uuids(servicios))

    fun respuestaCaracteristicas(secuencia: Int, servicio: UUID, caracteristicas: List<Caracteristica>): Mensaje {
        val b = ByteBuffer.allocate(16 + 17 * caracteristicas.size)
        b.put(bytes(servicio))
        caracteristicas.forEach { b.put(bytes(it.uuid)).put(it.propiedades.toByte()) }
        return Mensaje(DESCUBRIR_CARACTERISTICAS, secuencia, EXITO, b.array())
    }

    fun respuestaLectura(secuencia: Int, caracteristica: UUID, valor: ByteArray) = Mensaje(LEER, secuencia, EXITO, bytes(caracteristica) + valor)

    fun respuestaEscritura(secuencia: Int, caracteristica: UUID) = Mensaje(ESCRIBIR, secuencia, EXITO, bytes(caracteristica))

    fun respuestaSuscripcion(secuencia: Int, caracteristica: UUID) = Mensaje(NOTIFICACIONES, secuencia, EXITO, bytes(caracteristica))

    /** Respuesta de error a una petición de [tipo], con su secuencia y sin contenido. */
    fun error(tipo: Int, secuencia: Int, codigo: Int) = Mensaje(tipo, secuencia, codigo, ByteArray(0))

    /** Notificación espontánea del servidor; DirCon no distingue indicaciones de notificaciones. */
    fun notificacion(caracteristica: UUID, valor: ByteArray) = Mensaje(NOTIFICACION, 0, EXITO, bytes(caracteristica) + valor)

    /** Respuesta a [p] según [servidor] y, si es una escritura, lo que hay que notificar después de responder. */
    class Atencion(val respuesta: Mensaje, val notificaciones: List<NotificacionGatt> = emptyList())

    fun atender(servidor: ServidorGatt, p: Peticion): Atencion = when (p) {
        is Peticion.DescubrirServicios -> Atencion(respuestaServicios(p.secuencia, servidor.descubrirServicios()))

        is Peticion.DescubrirCaracteristicas -> when (val r = servidor.descubrirCaracteristicas(p.servicio)) {
            is ResultadoGatt.Ok -> Atencion(
                respuestaCaracteristicas(p.secuencia, p.servicio, r.valor.map { Caracteristica(it.uuid, propiedades(it.propiedades)) }),
            )

            is ResultadoGatt.Error -> Atencion(error(DESCUBRIR_CARACTERISTICAS, p.secuencia, codigo(r.motivo)))
        }

        is Peticion.Leer -> when (val r = servidor.leer(p.caracteristica)) {
            is ResultadoGatt.Ok -> Atencion(respuestaLectura(p.secuencia, p.caracteristica, r.valor))
            is ResultadoGatt.Error -> Atencion(error(LEER, p.secuencia, codigo(r.motivo)))
        }

        is Peticion.Escribir -> when (val r = servidor.escribir(p.caracteristica, p.valor)) {
            is ResultadoGatt.Ok -> Atencion(respuestaEscritura(p.secuencia, p.caracteristica), r.valor)
            is ResultadoGatt.Error -> Atencion(error(ESCRIBIR, p.secuencia, codigo(r.motivo)))
        }

        is Peticion.Suscribir -> when (val r = servidor.suscribir(p.caracteristica, p.activar)) {
            is ResultadoGatt.Ok -> Atencion(respuestaSuscripcion(p.secuencia, p.caracteristica))
            is ResultadoGatt.Error -> Atencion(error(NOTIFICACIONES, p.secuencia, codigo(r.motivo)))
        }

        is Peticion.Desconocida -> Atencion(error(p.tipo, p.secuencia, p.codigo))
    }

    private fun propiedades(p: Set<PropiedadGatt>): Int = (if (PropiedadGatt.Leer in p) PROP_LEER else 0) or
        (if (PropiedadGatt.Escribir in p) PROP_ESCRIBIR else 0) or
        (if (PropiedadGatt.Notificar in p || PropiedadGatt.Indicar in p) PROP_NOTIFICAR else 0)

    private fun codigo(e: ErrorGatt): Int = when (e) {
        ErrorGatt.ServicioNoEncontrado -> SERVICIO_NO_ENCONTRADO
        ErrorGatt.CaracteristicaNoEncontrada -> CARACTERISTICA_NO_ENCONTRADA
        ErrorGatt.OperacionNoAdmitida -> OPERACION_NO_ADMITIDA
    }

    fun bytes(u: UUID): ByteArray = ByteBuffer.allocate(16).putLong(u.mostSignificantBits).putLong(u.leastSignificantBits).array()

    fun uuid(b: ByteArray, desde: Int): UUID {
        val bb = ByteBuffer.wrap(b, desde, 16)
        return UUID(bb.long, bb.long)
    }

    private fun uuids(l: List<UUID>): ByteArray {
        val b = ByteBuffer.allocate(16 * l.size)
        l.forEach { b.put(bytes(it)) }
        return b.array()
    }

    /**
     * Separa en mensajes los bytes que llegan por el socket: TCP no respeta los límites de los mensajes, así que un
     * bloque puede traer medio mensaje o varios. Guarda lo que falta hasta el siguiente bloque.
     */
    class Lector {
        private val buffer = ByteArray(CABECERA + CONTENIDO_MAX)
        private var longitud = 0

        fun anadir(datos: ByteArray, n: Int = datos.size): List<Mensaje> {
            val mensajes = mutableListOf<Mensaje>()
            var origen = 0
            while (origen < n) {
                // Lo que cabe entre lo pendiente y el final del búfer; tras extraer, lo pendiente es un mensaje incompleto.
                val copiar = minOf(n - origen, buffer.size - longitud)
                datos.copyInto(buffer, longitud, origen, origen + copiar)
                longitud += copiar
                origen += copiar
                extraer(mensajes)
            }
            return mensajes
        }

        private fun extraer(mensajes: MutableList<Mensaje>) {
            var i = 0
            while (longitud - i >= CABECERA) {
                val contenido = ((buffer[i + 4].toInt() and 0xFF) shl 8) or (buffer[i + 5].toInt() and 0xFF)
                if (longitud - i < CABECERA + contenido) break
                mensajes += Mensaje(
                    tipo = buffer[i + 1].toInt() and 0xFF,
                    secuencia = buffer[i + 2].toInt() and 0xFF,
                    codigo = buffer[i + 3].toInt() and 0xFF,
                    contenido = buffer.copyOfRange(i + CABECERA, i + CABECERA + contenido),
                )
                i += CABECERA + contenido
            }
            if (i > 0) {
                buffer.copyInto(buffer, 0, i, longitud)
                longitud -= i
            }
        }
    }
}
