package app.smartbike.ui.tema

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.content.res.Resources
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import app.smartbike.dominio.IdiomaApp
import java.util.Locale

/** El `Locale` del idioma de la interfaz: lo que formatea los números y las fechas (spec `app-language`, «Formatos según el idioma»). */
val IdiomaApp.locale: Locale get() = Locale.forLanguageTag(codigo)

/** El `Locale` del idioma de la composición, para formatear números y fechas. */
@Composable
fun localeActual(): Locale = LocalConfiguration.current.locales[0]

/** Un contexto con los recursos del [idioma], para los textos que se leen fuera de la composición (la notificación). */
fun Context.conIdioma(idioma: IdiomaApp): Context = createConfigurationContext(Configuration(resources.configuration).apply { setLocale(idioma.locale) })

/**
 * Un contexto que envuelve al [base] (la actividad se sigue encontrando a través de él) y lee los recursos del
 * contexto del idioma: `stringResource` y `pluralStringResource` usan `LocalContext.current.resources`.
 */
private class ContextoConIdioma(base: Context, private val localizado: Context) : ContextWrapper(base) {
    override fun getResources(): Resources = localizado.resources
}

/**
 * La app entera con el idioma elegido en Ajustes (spec `app-language`, design.md del change `idioma-es-en`, decisión 3).
 * Mientras [idioma] es `null` (la configuración aún no se ha leído) no compone nada, como [SqTemaElegido]: así ninguna
 * pantalla sale en un idioma para cambiar al otro al instante. Al cambiarlo, la composición se rehace con los textos
 * nuevos sin recrear la actividad.
 */
@Composable
fun IdiomaElegido(idioma: IdiomaApp?, content: @Composable () -> Unit) {
    if (idioma == null) return
    val base = LocalContext.current
    val contexto = remember(base, idioma) { ContextoConIdioma(base, base.conIdioma(idioma)) }
    CompositionLocalProvider(
        LocalContext provides contexto,
        LocalConfiguration provides contexto.resources.configuration,
        content = content,
    )
}
