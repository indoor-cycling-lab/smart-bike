package app.smartbike.ble

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface EstadoBluetooth {
    val encendido: StateFlow<Boolean>

    /** Si la app tiene `BLUETOOTH_CONNECT` (siempre en Android 11 o anterior). */
    val permiso: StateFlow<Boolean>

    /** Si la app tiene `BLUETOOTH_ADVERTISE`, para anunciarse como mando de Zwift (siempre en Android 11 o anterior). */
    val permisoAnunciar: StateFlow<Boolean>
}

/**
 * Sigue si el Bluetooth del móvil está encendido, con `ACTION_STATE_CHANGED`, y si hay permiso para conectar y para
 * anunciarse, que se vuelve a mirar con [comprobarPermiso] (no hay aviso del sistema al concederlo).
 */
class AdaptadorBluetooth(context: Context) : EstadoBluetooth {
    private val appContext = context.applicationContext
    private val adaptador = (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
    private val _encendido = MutableStateFlow(adaptador?.isEnabled == true)
    override val encendido: StateFlow<Boolean> = _encendido.asStateFlow()

    private val _permiso = MutableStateFlow(tienePermiso())
    override val permiso: StateFlow<Boolean> = _permiso.asStateFlow()

    private val _permisoAnunciar = MutableStateFlow(tiene(Manifest.permission.BLUETOOTH_ADVERTISE))
    override val permisoAnunciar: StateFlow<Boolean> = _permisoAnunciar.asStateFlow()

    /** El nombre Bluetooth del móvil, con el que Zwift ve el mando; `null` sin permiso o sin Bluetooth. */
    @SuppressLint("MissingPermission") // Sin BLUETOOTH_CONNECT, Android lanza SecurityException.
    fun nombre(): String? = try {
        adaptador?.name
    } catch (_: SecurityException) {
        null
    }

    /** Vuelve a mirar los dos permisos; devuelve si hay permiso para conectar. */
    fun comprobarPermiso(): Boolean {
        _permisoAnunciar.value = tiene(Manifest.permission.BLUETOOTH_ADVERTISE)
        return tienePermiso().also { _permiso.value = it }
    }

    private fun tienePermiso() = tiene(Manifest.permission.BLUETOOTH_CONNECT)

    private fun tiene(permiso: String) = Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
        ContextCompat.checkSelfPermission(appContext, permiso) == PackageManager.PERMISSION_GRANTED

    init {
        context.applicationContext.registerReceiver(
            object : BroadcastReceiver() {
                override fun onReceive(c: Context, intent: Intent) {
                    val estado = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                    _encendido.value = estado == BluetoothAdapter.STATE_ON
                }
            },
            IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED),
        )
    }
}
