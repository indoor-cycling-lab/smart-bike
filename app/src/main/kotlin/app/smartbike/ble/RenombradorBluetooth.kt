package app.smartbike.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import app.smartbike.datos.AlmacenEstado
import app.smartbike.datos.Origen
import app.smartbike.datos.RegistroSesion
import app.smartbike.dominio.EstadoGuardado
import app.smartbike.dominio.RecuperacionNombre
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull

/** Lo que `RenombradorBluetooth` necesita del adaptador, para poder probarlo (design.md de `nombre-bluetooth-propio`, decisión 2). */
interface NombreDelAdaptador {
    /** El nombre Bluetooth del móvil ahora; `null` sin permiso o sin Bluetooth. */
    fun actual(): String?

    /** El último nombre que Android ha confirmado con `ACTION_LOCAL_NAME_CHANGED`. */
    val confirmado: StateFlow<String?>

    /** Pide el cambio de nombre; `false` si Android lo rechaza. Lanza `SecurityException` sin permiso. */
    fun poner(nombre: String): Boolean
}

/** El nombre Bluetooth del móvil tal como lo ve la app (spec `bluetooth-name`). */
sealed interface EstadoNombre {
    /** El móvil conserva su nombre. */
    data object Propio : EstadoNombre

    /** El móvil se llama [nombre], el de la app; [original] es el que tenía y recuperará. */
    data class Renombrado(val nombre: String, val original: String) : EstadoNombre

    /** Se quiso renombrar y no se pudo: la app se anuncia con el nombre del móvil. */
    data class NoSePudo(val motivo: String) : EstadoNombre
}

/**
 * Cambia el nombre Bluetooth del móvil mientras la app publica por Bluetooth y se lo devuelve después (spec
 * `bluetooth-name`; design.md del change `nombre-bluetooth-propio`, decisiones 2, 4 y 6). El nombre original se guarda en
 * `estado.json` antes del primer cambio, para recuperarlo aunque la app se cierre de golpe.
 */
class RenombradorBluetooth(
    private val adaptador: NombreDelAdaptador,
    private val almacen: AlmacenEstado,
    private val registro: RegistroSesion,
) {
    private val _estado = MutableStateFlow<EstadoNombre>(EstadoNombre.Propio)
    val estado: StateFlow<EstadoNombre> = _estado.asStateFlow()
    private val candado = Mutex()

    /**
     * Pone [deseado] como nombre del móvil, o recupera el original con `null`. Devuelve si el nombre del adaptador ha
     * cambiado, para que quien anuncia vuelva a hacerlo con el nuevo.
     */
    suspend fun aplicar(deseado: String?): Boolean = candado.withLock {
        if (deseado == null) recuperar() else renombrar(deseado)
    }

    private suspend fun renombrar(deseado: String): Boolean {
        val actual = adaptador.actual()
        val antes = almacen.leer()
        if (actual == deseado) {
            _estado.value = EstadoNombre.Renombrado(deseado, antes.nombreBluetoothOriginal ?: actual)
            return false
        }
        if (actual == null) return fallo(deseado, "no se puede leer el nombre del móvil")
        // Ya renombrado por la app: el original sigue siendo el guardado; si lo cambió el usuario, el original es el actual.
        val original = antes.nombreBluetoothOriginal?.takeIf { antes.nombreBluetoothPuesto == actual } ?: actual
        almacen.actualizar { it.copy(nombreBluetoothOriginal = original, nombreBluetoothPuesto = deseado) }
        val aceptado = try {
            adaptador.poner(deseado)
        } catch (_: SecurityException) {
            return fallo(deseado, "sin permiso", antes)
        }
        if (!aceptado) return fallo(deseado, "Android lo rechaza", antes)
        if (!esperar(deseado)) return fallo(deseado, "Android no lo confirma")
        registro.suceso(Origen.Plataforma, "Bluetooth: nombre del móvil «$actual» → «$deseado»")
        _estado.value = EstadoNombre.Renombrado(deseado, original)
        return true
    }

    private suspend fun recuperar(): Boolean {
        val guardado = almacen.leer()
        val original = guardado.nombreBluetoothOriginal
        val puesto = guardado.nombreBluetoothPuesto
        if (original == null || puesto == null) {
            _estado.value = EstadoNombre.Propio
            return false
        }
        val actual = adaptador.actual()
        when (val decision = RecuperacionNombre.decidir(original, puesto, actual)) {
            RecuperacionNombre.NoTocar -> {
                registro.suceso(Origen.Plataforma, "Bluetooth: el nombre del móvil ya no es «$puesto»; no se toca")
                olvidar()
                return false
            }

            is RecuperacionNombre.Restaurar -> {
                val hecho = try {
                    adaptador.poner(decision.nombre) && esperar(decision.nombre)
                } catch (_: SecurityException) {
                    false
                }
                if (!hecho) {
                    // Se conservan los campos: se vuelve a intentar al abrir la app.
                    registro.suceso(Origen.Plataforma, "Bluetooth: no se pudo devolver el nombre «${decision.nombre}» al móvil")
                    return false
                }
                registro.suceso(Origen.Plataforma, "Bluetooth: nombre del móvil «$puesto» → «${decision.nombre}»")
                olvidar()
                return true
            }
        }
    }

    private suspend fun olvidar() {
        almacen.actualizar { it.copy(nombreBluetoothOriginal = null, nombreBluetoothPuesto = null) }
        _estado.value = EstadoNombre.Propio
    }

    private suspend fun esperar(nombre: String): Boolean = withTimeoutOrNull(ESPERA_CONFIRMACION_MS) { adaptador.confirmado.first { it == nombre } } != null

    /** [anterior], si se da, es lo que había guardado: el nombre no ha cambiado, así que se deja como estaba. */
    private suspend fun fallo(deseado: String, motivo: String, anterior: EstadoGuardado? = null): Boolean {
        if (anterior != null) {
            almacen.actualizar {
                it.copy(nombreBluetoothOriginal = anterior.nombreBluetoothOriginal, nombreBluetoothPuesto = anterior.nombreBluetoothPuesto)
            }
        }
        registro.suceso(Origen.Plataforma, "Bluetooth: no se pudo poner el nombre «$deseado» ($motivo)")
        _estado.value = EstadoNombre.NoSePudo(motivo)
        return false
    }

    companion object {
        const val ESPERA_CONFIRMACION_MS = 3_000L
    }
}

/** El adaptador del móvil: `setName` y `ACTION_LOCAL_NAME_CHANGED` como confirmación. */
@SuppressLint("MissingPermission") // Sin BLUETOOTH_CONNECT, Android lanza SecurityException, que se trata como «no se pudo».
class AdaptadorNombre(context: Context) : NombreDelAdaptador {
    private val adaptador = (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
    private val _confirmado = MutableStateFlow(actual())
    override val confirmado: StateFlow<String?> = _confirmado.asStateFlow()

    override fun actual(): String? = try {
        adaptador?.name
    } catch (_: SecurityException) {
        null
    }

    override fun poner(nombre: String): Boolean = adaptador?.setName(nombre) == true

    init {
        context.applicationContext.registerReceiver(
            object : BroadcastReceiver() {
                override fun onReceive(c: Context, intent: Intent) {
                    _confirmado.value = intent.getStringExtra(BluetoothAdapter.EXTRA_LOCAL_NAME) ?: actual()
                }
            },
            IntentFilter(BluetoothAdapter.ACTION_LOCAL_NAME_CHANGED),
        )
    }
}
