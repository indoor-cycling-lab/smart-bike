package app.smartbike.ui.sesiones

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

/**
 * Abrir y compartir una sesión grabada con otra app (design.md del change `registro-sesiones`, decisión 7): texto
 * plano, con la `Uri` del `FileProvider` y permiso de lectura temporal solo para ese archivo. Sin permisos nuevos.
 */
object CompartirSesion {
    private const val TIPO = "text/plain"

    private fun uri(context: Context, archivo: File) = FileProvider.getUriForFile(context, "${context.packageName}.sesiones", archivo)

    fun intentAbrir(context: Context, archivo: File): Intent {
        val uri = uri(context, archivo)
        return Intent(Intent.ACTION_VIEW).setDataAndType(uri, TIPO)
            .apply { clipData = ClipData.newRawUri(archivo.name, uri) }
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    fun intentCompartir(context: Context, archivo: File): Intent {
        val uri = uri(context, archivo)
        val envio = Intent(Intent.ACTION_SEND).setType(TIPO)
            .putExtra(Intent.EXTRA_STREAM, uri)
            .apply { clipData = ClipData.newRawUri(archivo.name, uri) }
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        return Intent.createChooser(envio, null).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    /** Lanza [intent]; devuelve `false` si ninguna app lo atiende, para avisarlo sin cerrar la app. */
    fun lanzar(context: Context, intent: Intent): Boolean = try {
        context.startActivity(intent)
        true
    } catch (_: ActivityNotFoundException) {
        false
    }
}
