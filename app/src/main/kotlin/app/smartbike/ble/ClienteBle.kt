package app.smartbike.ble

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.util.Log
import app.smartbike.BuildConfig
import app.smartbike.datos.Origen
import app.smartbike.datos.RegistroSesion
import app.smartbike.datos.Sentido
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.ble.ConnectRequest
import no.nordicsemi.android.ble.ConnectionPriorityRequest
import no.nordicsemi.android.ble.ktx.state.ConnectionState
import no.nordicsemi.android.ble.ktx.stateAsFlow
import no.nordicsemi.android.ble.ktx.suspend

/**
 * Base de los clientes centrales sobre la biblioteca de Nordic: estado del enlace, notificaciones al registro y a un
 * flujo, y prioridad de conexión alta (arquitectura.md § 6.3). No sabe qué significan los bytes.
 */
abstract class ClienteBle(
    context: Context,
    private val origen: Origen,
    protected val registro: RegistroSesion,
    protected val ambito: CoroutineScope,
    private val reloj: () -> Long,
) : BleManager(context),
    ConexionBle {

    private val adaptador: BluetoothAdapter? =
        (context.getSystemService(Context.BLUETOOTH_SERVICE) as android.bluetooth.BluetoothManager).adapter

    private val _estado = MutableStateFlow(EstadoEnlace.Desconectado)
    override val estado: StateFlow<EstadoEnlace> = _estado.asStateFlow()

    // Los callbacks de Nordic solo copian los bytes aquí; quien escucha los procesa en su hilo (arquitectura.md § 6.1).
    private val _notificaciones = MutableSharedFlow<Notificacion>(extraBufferCapacity = 256)
    override val notificaciones: SharedFlow<Notificacion> = _notificaciones

    @Volatile private var desconexionPedida = false

    init {
        ambito.launch {
            stateAsFlow().collect { s ->
                _estado.value = when (s) {
                    ConnectionState.Connecting -> EstadoEnlace.Conectando

                    ConnectionState.Initializing -> EstadoEnlace.Descubriendo

                    ConnectionState.Ready -> EstadoEnlace.Listo

                    ConnectionState.Disconnecting -> _estado.value

                    is ConnectionState.Disconnected -> when {
                        desconexionPedida -> EstadoEnlace.Desconectado
                        _estado.value == EstadoEnlace.Listo -> EstadoEnlace.Perdida
                        s.reason == ConnectionState.Disconnected.Reason.NOT_SUPPORTED -> EstadoEnlace.Fallo
                        _estado.value == EstadoEnlace.Desconectado -> EstadoEnlace.Desconectado
                        else -> EstadoEnlace.Fallo
                    }
                }
                if (s !is ConnectionState.Disconnecting) registro.suceso(origen, "Enlace: ${_estado.value}")
            }
        }
    }

    override suspend fun conectar(direccion: String): Boolean = conectar(direccion, segundoPlano = false)

    override suspend fun conectarEnSegundoPlano(direccion: String): Boolean = conectar(direccion, segundoPlano = true)

    /**
     * Directa: 3 reintentos y 15 s, para un dispositivo despierto. En segundo plano: `autoConnect` sin límite de tiempo
     * (design.md del change `conexion-automatica-en-segundo-plano`, decisión 2). Nordic, por defecto, hace antes otra
     * conexión directa, que con el dispositivo apagado no pasaba nunca a `autoConnect` en el OnePlus 8: la directa ya
     * la ha hecho la `Sesion`, así que se pide sin ella.
     */
    private suspend fun conectar(direccion: String, segundoPlano: Boolean): Boolean {
        if (!BluetoothAdapter.checkBluetoothAddress(direccion)) {
            registro.suceso(origen, "Dirección inválida: $direccion")
            if (_estado.value != EstadoEnlace.Listo) _estado.value = EstadoEnlace.Fallo
            return false
        }
        val dispositivo = adaptador?.getRemoteDevice(direccion) ?: return false
        if (segundoPlano) esperarDesconexion() else cancelarReconexionPropia()
        desconexionPedida = false
        registro.suceso(origen, if (segundoPlano) "Conexión en segundo plano con $direccion" else "Conexión directa con $direccion")
        return try {
            val peticion = connect(dispositivo)
            conexionEnCurso = peticion
            if (segundoPlano) peticion.useAutoConnect(true, false) else peticion.retry(3, 200).useAutoConnect(false).timeout(15_000)
            peticion.suspend()
            true
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            registro.suceso(origen, "No se pudo conectar: ${e.javaClass.simpleName}")
            if (_estado.value != EstadoEnlace.Listo) _estado.value = EstadoEnlace.Fallo
            false
        } finally {
            conexionEnCurso = null
        }
    }

    /**
     * Tras una directa fallida, Nordic tarda unos milisegundos en cerrar el enlace; una petición que llega antes se
     * pierde sin fallar ni conectarse (visto en el OnePlus 8). Se espera a que quede desconectado, como mucho
     * [ESPERA_CIERRE_MS].
     */
    private suspend fun esperarDesconexion() {
        withTimeoutOrNull(ESPERA_CIERRE_MS) { stateAsFlow().first { it is ConnectionState.Disconnected } }
    }

    /**
     * Tras una conexión con `autoConnect`, la biblioteca de Nordic vuelve a conectar por su cuenta al perderla. La
     * política es de la `Sesion`: antes de la directa se cancela ese intento para no tener dos a la vez.
     */
    private suspend fun cancelarReconexionPropia() {
        if (isConnected || _estado.value == EstadoEnlace.Desconectado) return
        desconexionPedida = true
        try {
            disconnect().suspend()
        } catch (_: Exception) {
            // No había nada que cancelar.
        }
    }

    override suspend fun desconectar() {
        desconexionPedida = true
        try {
            disconnect().suspend()
        } catch (_: Exception) {
            // Ya estaba desconectado o el Bluetooth está apagado: el estado final es el mismo.
        }
        _estado.value = EstadoEnlace.Desconectado
    }

    /** La petición de conexión en curso, para poder cancelarla a la fuerza. */
    @Volatile private var conexionEnCurso: ConnectRequest? = null

    override fun cancelarConexionPendiente() {
        desconexionPedida = true
        registro.suceso(origen, "Se cancela la conexión pendiente y se vacía la cola")
        conexionEnCurso?.cancelPendingConnection()
        cancelQueue()
        _estado.value = EstadoEnlace.Desconectado
    }

    private var ultimaPeticionMs = Long.MIN_VALUE / 2
    private var intervaloMs = 0.0
    private var repeticion: Job? = null

    /**
     * Prioridad de conexión alta, sostenida mientras dure la conexión (design.md del change `marchas-sin-zwift`,
     * decisión 10, paso 2): el Square y el Neo renegocian a los pocos segundos un intervalo largo. Mientras el intervalo
     * pase de [INTERVALO_MAX_MS] se vuelve a pedir, sin límite de intentos pero con al menos [ESPERA_PRIORIDAD_MS]
     * entre peticiones: si el aviso llega antes, la petición se aplaza, porque puede no llegar otro. Con [sostener] a
     * `false` solo se pide al conectar. Cada cambio de parámetros queda en el registro.
     */
    protected fun prioridadAltaSostenida(sostener: Boolean = true) {
        setConnectionParametersListener { _, intervalo, latencia, _ ->
            // Nordic avisa desde su hilo: el estado se toca en el hilo de control, el del ámbito de la app.
            ambito.launch {
                // El intervalo llega en unidades de 1,25 ms.
                intervaloMs = intervalo * 1.25
                registro.suceso(origen, "Parámetros de conexión: intervalo $intervaloMs ms, latencia $latencia")
                if (sostener && intervaloMs > INTERVALO_MAX_MS) pedirPrioridadAlta()
            }
        }
        repeticion?.cancel()
        intervaloMs = 0.0
        ultimaPeticionMs = reloj()
        requestConnectionPriority(ConnectionPriorityRequest.CONNECTION_PRIORITY_HIGH).enqueue()
    }

    private fun pedirPrioridadAlta() {
        if (repeticion?.isActive == true) return
        repeticion = ambito.launch {
            delay((ultimaPeticionMs + ESPERA_PRIORIDAD_MS - reloj()).coerceAtLeast(0))
            if (intervaloMs <= INTERVALO_MAX_MS || !isConnected) return@launch
            ultimaPeticionMs = reloj()
            registro.suceso(origen, "Pide prioridad alta otra vez")
            requestConnectionPriority(ConnectionPriorityRequest.CONNECTION_PRIORITY_HIGH).enqueue()
            // Si Android la aplica y el dispositivo vuelve a alargar el intervalo, llegará otro aviso; si no llega
            // ninguno, se vuelve a intentar al cumplirse la espera.
            delay(ESPERA_PRIORIDAD_MS)
            if (intervaloMs > INTERVALO_MAX_MS && isConnected) pedirPrioridadAltaDespues()
        }
    }

    private fun pedirPrioridadAltaDespues() {
        repeticion = null
        pedirPrioridadAlta()
    }

    /** Suscribe [c] y manda cada notificación al registro y al flujo, con la etiqueta [etiqueta]. */
    protected fun escuchar(c: BluetoothGattCharacteristic, etiqueta: String) {
        setNotificationCallback(c).with { _, data ->
            val bytes = data.value ?: ByteArray(0)
            registro.trama(origen, Sentido.Rx, etiqueta, bytes)
            _notificaciones.tryEmit(Notificacion(etiqueta, bytes, reloj()))
        }
        enableNotifications(c).fail { _, status -> registro.suceso(origen, "No se pudo suscribir a $etiqueta ($status)") }
            .enqueue()
    }

    // En depuración, también los pasos de la conexión (`connectGatt`, `autoConnect`) para las pruebas con el equipo real;
    // en release, solo avisos y errores: los pasos llevan las direcciones de los dispositivos.
    override fun getMinLogPriority(): Int = if (BuildConfig.DEBUG) Log.DEBUG else Log.WARN

    companion object {
        const val INTERVALO_MAX_MS = 15.0
        const val ESPERA_PRIORIDAD_MS = 10_000L
        const val ESPERA_CIERRE_MS = 2_000L
    }

    override fun log(priority: Int, message: String) {
        Log.println(priority, "SqBle", "${origen.name}: $message")
    }
}
