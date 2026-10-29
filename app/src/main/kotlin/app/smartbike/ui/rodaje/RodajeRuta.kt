package app.smartbike.ui.rodaje

import android.Manifest
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import app.smartbike.ui.componentes.AccionesGuia

/**
 * Rodaje con su `ViewModel` y lo que depende de Android (design.md del change `marchas-sin-zwift`, decisión 9): con las
 * barras del sistema a la vista (change `quitar-barra-estado-rodaje`) y la pantalla encendida mientras se ve; Atrás dos veces seguidas termina la sesión (change `confirmar-salida-rodaje`); la primera vez pide el permiso
 * de notificaciones. La orientación es la del móvil, con su bloqueo de rotación (change `rodaje-vertical`, decisión 1).
 */
@Composable
fun RodajeRuta(abrirDispositivos: () -> Unit, abrirAjustes: () -> Unit, abrirConexion: () -> Unit, vm: RodajeViewModel = viewModel(factory = RodajeViewModel.Factory)) {
    val estado by vm.estado.collectAsStateWithLifecycle()
    val contexto = LocalContext.current
    val vista = LocalView.current

    DisposableEffect(Unit) {
        vista.keepScreenOn = true
        onDispose { vista.keepScreenOn = false }
    }

    BackHandler { vm.atras() }

    DisposableEffect(Unit) { onDispose { vm.cancelarSalida() } }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val pedidas by vm.notificacionesPedidas.collectAsStateWithLifecycle()
        val pedir = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
        LaunchedEffect(pedidas) {
            if (!pedidas) {
                vm.notificacionesPedidas()
                pedir.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // Movimiento reducido: con las animaciones del sistema desactivadas, la marcha cambia sin animación.
    val animar = remember {
        Settings.Global.getFloat(contexto.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) != 0f
    }
    Rodaje(estado, abrirDispositivos, abrirAjustes = abrirAjustes, abrirConexion = abrirConexion, animar = animar, mas = vm::mas, menos = vm::menos, alternarModo = vm::alternarModo, guia = AccionesGuia(minimizar = vm::minimizarGuia, abrir = vm::abrirGuia, cerrar = vm::cerrarGuia, noMostrar = vm::noMostrarGuia, irA = vm::paginaGuia))
}
