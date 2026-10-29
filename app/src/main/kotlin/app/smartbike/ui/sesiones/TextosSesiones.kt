package app.smartbike.ui.sesiones

import java.time.LocalDateTime
import java.util.Locale
import kotlin.math.ceil

private val MESES_ES = listOf("ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dic")
private val MESES_EN = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

/**
 * «29 oct 2026 · 18:42» o «29 Oct 2026 · 18:42» según el [locale]; sin fecha (un nombre que no la lleva), el nombre del
 * archivo. Los meses son fijos por idioma: los del sistema cambian de abreviatura entre versiones de Android.
 */
fun fechaSesion(inicio: LocalDateTime?, nombre: String, locale: Locale): String {
    val meses = if (locale.language == "es") MESES_ES else MESES_EN
    return inicio?.let { "%d %s %d · %02d:%02d".format(it.dayOfMonth, meses[it.monthValue - 1], it.year, it.hour, it.minute) } ?: nombre
}

/** El tamaño de un archivo: «512 B», «41 KB» o «1,2 MB» («1.2 MB» en inglés). Un archivo con datos nunca dice «0 KB». */
fun tamanoLegible(bytes: Long, locale: Locale): String = when {
    bytes < 1_000 -> "$bytes B"
    bytes < 1_000_000 -> "${ceil(bytes / 1_000.0).toLong()} KB"
    else -> "%.1f MB".format(locale, bytes / 1_000_000.0)
}
