package app.smartbike.protocolo

import java.util.UUID

/** Lo que admite una característica. Las indicaciones van por DirCon como notificaciones. */
enum class PropiedadGatt { Leer, Escribir, Notificar, Indicar }

enum class ErrorGatt { ServicioNoEncontrado, CaracteristicaNoEncontrada, OperacionNoAdmitida }

/** Valor que hay que enviar al cliente por una característica a la que está suscrito. */
class NotificacionGatt(val caracteristica: UUID, val valor: ByteArray)

sealed interface ResultadoGatt<out T> {
    data class Ok<T>(val valor: T) : ResultadoGatt<T>
    data class Error(val motivo: ErrorGatt) : ResultadoGatt<Nothing>
}

/**
 * Una característica que se ofrece a Zwift. [leer] da su valor actual; [alEscribir] atiende una escritura y devuelve
 * lo que hay que notificar en respuesta (por ejemplo, la respuesta del punto de control FTMS).
 */
class CaracteristicaGatt(
    val uuid: UUID,
    val propiedades: Set<PropiedadGatt>,
    val leer: (() -> ByteArray)? = null,
    val alEscribir: ((ByteArray) -> List<NotificacionGatt>)? = null,
)

class ServicioGatt(val uuid: UUID, val caracteristicas: List<CaracteristicaGatt>)

/**
 * Servicios y características que la app ofrece a Zwift, común a DirCon y a Bluetooth (arquitectura.md § 4.3; design.md
 * del change `puente-zwift-wifi`, decisión 3): quién atiende cada lectura o escritura y a qué está suscrito el cliente.
 * No sabe de transportes: devuelve resultados y notificaciones, y el transporte los envía. Un solo cliente a la vez.
 */
class ServidorGatt(val servicios: List<ServicioGatt>) {
    private val porUuid = servicios.flatMap { it.caracteristicas }.associateBy { it.uuid }
    private val suscritas = mutableSetOf<UUID>()

    fun descubrirServicios(): List<UUID> = servicios.map { it.uuid }

    fun descubrirCaracteristicas(servicio: UUID): ResultadoGatt<List<CaracteristicaGatt>> = servicios.firstOrNull { it.uuid == servicio }?.let { ResultadoGatt.Ok(it.caracteristicas) }
        ?: ResultadoGatt.Error(ErrorGatt.ServicioNoEncontrado)

    fun leer(caracteristica: UUID): ResultadoGatt<ByteArray> {
        val c = porUuid[caracteristica] ?: return ResultadoGatt.Error(ErrorGatt.CaracteristicaNoEncontrada)
        val leer = c.leer
        if (PropiedadGatt.Leer !in c.propiedades || leer == null) return ResultadoGatt.Error(ErrorGatt.OperacionNoAdmitida)
        return ResultadoGatt.Ok(leer())
    }

    /** Atiende una escritura; las notificaciones que devuelve son solo las de características suscritas. */
    fun escribir(caracteristica: UUID, valor: ByteArray): ResultadoGatt<List<NotificacionGatt>> {
        val c = porUuid[caracteristica] ?: return ResultadoGatt.Error(ErrorGatt.CaracteristicaNoEncontrada)
        val alEscribir = c.alEscribir
        if (PropiedadGatt.Escribir !in c.propiedades || alEscribir == null) {
            return ResultadoGatt.Error(ErrorGatt.OperacionNoAdmitida)
        }
        return ResultadoGatt.Ok(alEscribir(valor).filter { it.caracteristica in suscritas })
    }

    fun suscribir(caracteristica: UUID, activar: Boolean): ResultadoGatt<Unit> {
        val c = porUuid[caracteristica] ?: return ResultadoGatt.Error(ErrorGatt.CaracteristicaNoEncontrada)
        if (PropiedadGatt.Notificar !in c.propiedades && PropiedadGatt.Indicar !in c.propiedades) {
            return ResultadoGatt.Error(ErrorGatt.OperacionNoAdmitida)
        }
        if (activar) suscritas += caracteristica else suscritas -= caracteristica
        return ResultadoGatt.Ok(Unit)
    }

    fun suscrita(caracteristica: UUID): Boolean = caracteristica in suscritas

    /** La notificación de [valor] por [caracteristica], o `null` si el cliente no está suscrito. */
    fun notificar(caracteristica: UUID, valor: ByteArray): NotificacionGatt? = if (caracteristica in suscritas) NotificacionGatt(caracteristica, valor) else null

    /** El cliente se ha ido: el siguiente empieza sin suscripciones. */
    fun clientePerdido() = suscritas.clear()
}
