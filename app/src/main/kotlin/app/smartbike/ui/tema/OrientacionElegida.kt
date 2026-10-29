package app.smartbike.ui.tema

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import app.smartbike.dominio.OrientacionApp

/**
 * La orientación de Android de cada opción de Ajustes > Pantalla (design.md del change `orientacion-pantalla`,
 * decisión 2). Las fijas usan `SENSOR_*`: no dependen del bloqueo de rotación del sistema y, dentro de la orientación,
 * la pantalla se ve derecha en el sentido en que esté el móvil.
 */
fun OrientacionApp.comoPantalla(): Int = when (this) {
    OrientacionApp.SEGUN_MOVIL -> ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    OrientacionApp.VERTICAL -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
    OrientacionApp.HORIZONTAL -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
}

/**
 * Pide a la actividad la orientación elegida en cada cambio (decisión 3). Mientras [orientacion] es `null` (la
 * configuración aún no se ha leído) no pide nada. Si no es la actual, Android gira y recrea la actividad.
 */
@Composable
fun OrientacionElegida(orientacion: OrientacionApp?) {
    val actividad = LocalContext.current.actividad()
    LaunchedEffect(orientacion) {
        if (orientacion != null) actividad?.requestedOrientation = orientacion.comoPantalla()
    }
}

private tailrec fun Context.actividad(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.actividad()
    else -> null
}
