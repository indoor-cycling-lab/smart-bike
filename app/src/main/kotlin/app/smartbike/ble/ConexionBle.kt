package app.smartbike.ble

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Estado del enlace con un dispositivo central (arquitectura.md § 8). La reconexión la decide la `Sesion`.
 * `Perdida`: se cortó sin que la app lo pidiera. `Fallo`: no se pudo conectar o le falta un servicio.
 */
enum class EstadoEnlace { Desconectado, Conectando, Descubriendo, Listo, Perdida, Fallo }

/** Notificación recibida, con la hora monotónica de llegada. [caracteristica] es la etiqueta corta (`0x43`, `2a63`). */
class Notificacion(val caracteristica: String, val datos: ByteArray, val tMs: Long)

/** Lo común a los clientes del Neo y del Square, detrás de una interfaz para probar sin Bluetooth. */
interface ConexionBle {
    val estado: StateFlow<EstadoEnlace>
    val notificaciones: Flow<Notificacion>

    /** Conecta con el dispositivo de esa dirección y vuelve cuando el enlace está `Listo` o ha fallado. */
    suspend fun conectar(direccion: String): Boolean

    /**
     * Espera, sin límite de tiempo, a que el dispositivo de esa dirección se anuncie y conecta con él (`autoConnect`
     * de Android: sin buscar y también con la pantalla apagada). Vuelve con `true` cuando el enlace está `Listo`, o con
     * `false` si falla. Se cancela con [desconectar] o cancelando a quien espera.
     */
    suspend fun conectarEnSegundoPlano(direccion: String): Boolean

    /** Desconecta a petición de la app: el estado pasa a `Desconectado`, no a `Perdida`. */
    suspend fun desconectar()

    /**
     * Cancela la conexión pendiente y vacía la cola de peticiones del cliente, sin cerrar su gestor (design.md del change
     * `endurecer-dircon-ble`, decisión 6). Es el último recurso cuando [desconectar] no termina. No se usa `close()`:
     * deja el gestor de Nordic inservible.
     */
    fun cancelarConexionPendiente()
}

interface ConexionSquare : ConexionBle {
    /**
     * Los niveles de batería (0–100 %) que cuentan: las lecturas periódicas, no la de justo después de conectar (spec
     * `square-battery`).
     */
    val nivelBateria: Flow<Int>
}

/** El pulsómetro: sus notificaciones son la medida de frecuencia cardiaca (`2A37`). */
interface ConexionPulso : ConexionBle

/** El ventilador: sus notificaciones son el control point (`FD`/`FE`); las órdenes se escriben sin respuesta. */
interface ConexionVentilador : ConexionBle {
    /** Escribe una orden en el control point; si no hay enlace `Listo`, no hace nada ni la encola. */
    fun enviar(orden: ByteArray)
}

interface ConexionNeo : ConexionBle {
    /**
     * Encola un comando FE-C; si hay otro esperando, lo sustituye (arquitectura.md § 6.2). [alConfirmar] se llama al
     * terminar la escritura (con `false` si no se confirmó a tiempo); no se llama si otro comando lo sustituye antes.
     */
    fun enviar(comando: ByteArray, alConfirmar: ((Boolean) -> Unit)? = null)

    /** Como [enviar], pero espera a que el Neo confirme la escritura, como mucho [limiteMs]. */
    suspend fun enviarYEsperar(comando: ByteArray, limiteMs: Long): Boolean
}
