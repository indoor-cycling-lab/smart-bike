package app.smartbike.ui.dispositivos

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import app.smartbike.sesion.Sesion

/** Permisos para buscar y conectar por Bluetooth (arquitectura.md § 12). */
private val PERMISOS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
} else {
    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
}

/**
 * Se piden con [PERMISOS], del mismo grupo («Dispositivos cercanos»), pero sin ellos solo falta el mando de Zwift, que
 * queda «Sin conexión» (design.md del change `mando-zwift-emulado`, decisión 8).
 */
private val PERMISOS_MANDO = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    arrayOf(Manifest.permission.BLUETOOTH_ADVERTISE)
} else {
    emptyArray()
}

private fun Context.tiene(permisos: Array<String>) = permisos.all { ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }

private fun Context.tienePermisos() = tiene(PERMISOS)

/** Lo que hacen los botones de las conexiones con Android: buscar con permiso, activar el Bluetooth y abrir ajustes. */
class AccionesConexion(
    val buscar: () -> Unit,
    val activarBluetooth: () -> Unit,
    val abrirAjustes: () -> Unit,
    val buscarPulsometro: () -> Unit = {},
    val buscarVentilador: () -> Unit = {},
    val buscarMando: () -> Unit = {},
    val reconectar: (Sesion.Dispositivo) -> Unit = {},
)

/**
 * Las acciones de las conexiones de Diagnóstico y Dispositivos: «Buscar dispositivos» pide el permiso si falta
 * (spec `device-connection`, «Permisos de Bluetooth»), y al volver de los ajustes de la app con el permiso concedido
 * desaparece el aviso.
 */
@Composable
fun rememberAccionesConexion(
    permisoConcedido: (Boolean) -> Unit,
    buscar: () -> Unit,
    buscarPulsometro: () -> Unit = {},
    buscarVentilador: () -> Unit = {},
    buscarMando: () -> Unit = {},
    reconectar: (Sesion.Dispositivo) -> Unit = {},
): AccionesConexion {
    val contexto = LocalContext.current
    // Lo que se iba a hacer al pedir el permiso («Buscar», «Buscar pulsómetro», «Reconectar» el Neo…), como texto para
    // que sobreviva a girar el móvil con el diálogo abierto.
    var pendiente by rememberSaveable { mutableStateOf(PENDIENTE_BUSCAR) }
    val acciones = mapOf(
        PENDIENTE_BUSCAR to buscar,
        PENDIENTE_PULSOMETRO to buscarPulsometro,
        PENDIENTE_VENTILADOR to buscarVentilador,
        PENDIENTE_MANDO to buscarMando,
    ) + Sesion.Dispositivo.entries.associate { d -> "$PENDIENTE_RECONECTAR${d.name}" to { reconectar(d) } }
    val pedirPermisos = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { r ->
        val concedidos = PERMISOS.all { r[it] ?: contexto.tiene(arrayOf(it)) }
        permisoConcedido(concedidos)
        if (concedidos) acciones[pendiente]?.invoke()
    }
    fun conPermisos(clave: String) = conPermisos(contexto, permisoConcedido, acciones.getValue(clave), { pendiente = clave }, pedirPermisos::launch)
    val activarBluetooth = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {}
    LifecycleResumeEffect(Unit) {
        if (contexto.tienePermisos()) permisoConcedido(true)
        onPauseOrDispose {}
    }
    return AccionesConexion(
        buscar = { conPermisos(PENDIENTE_BUSCAR) },
        buscarPulsometro = { conPermisos(PENDIENTE_PULSOMETRO) },
        buscarVentilador = { conPermisos(PENDIENTE_VENTILADOR) },
        buscarMando = { conPermisos(PENDIENTE_MANDO) },
        reconectar = { d -> conPermisos("$PENDIENTE_RECONECTAR${d.name}") },
        activarBluetooth = {
            try {
                activarBluetooth.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
            } catch (_: SecurityException) {
                // Sin BLUETOOTH_CONNECT no se puede pedir directamente: se abren los ajustes de Bluetooth.
                contexto.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
            }
        },
        abrirAjustes = {
            contexto.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", contexto.packageName, null)))
        },
    )
}

/** Hace [accion] si hay permisos; si no, la deja [pendiente], los pide y, al concederlos, la hace. */
private fun conPermisos(
    contexto: Context,
    permisoConcedido: (Boolean) -> Unit,
    accion: () -> Unit,
    pendiente: () -> Unit,
    pedir: (Array<String>) -> Unit,
) {
    if (contexto.tienePermisos() && contexto.tiene(PERMISOS_MANDO)) {
        permisoConcedido(true)
        accion()
    } else {
        pendiente()
        pedir(PERMISOS + PERMISOS_MANDO)
    }
}

private const val PENDIENTE_BUSCAR = "buscar"
private const val PENDIENTE_PULSOMETRO = "pulsometro"
private const val PENDIENTE_VENTILADOR = "ventilador"
private const val PENDIENTE_MANDO = "mando"
private const val PENDIENTE_RECONECTAR = "reconectar:"
