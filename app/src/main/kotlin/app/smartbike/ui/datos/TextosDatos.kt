package app.smartbike.ui.datos

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import app.smartbike.R
import app.smartbike.dominio.DatoRodaje

@StringRes
fun nombreDato(d: DatoRodaje): Int = when (d) {
    DatoRodaje.POTENCIA -> R.string.dato_potencia
    DatoRodaje.CADENCIA -> R.string.dato_cadencia
    DatoRodaje.TIEMPO -> R.string.dato_tiempo
    DatoRodaje.W_KG -> R.string.dato_w_kg
    DatoRodaje.POTENCIA_MEDIA -> R.string.dato_potencia_media
    DatoRodaje.POTENCIA_MAXIMA -> R.string.dato_potencia_maxima
    DatoRodaje.VELOCIDAD -> R.string.dato_velocidad
    DatoRodaje.DISTANCIA -> R.string.dato_distancia
    DatoRodaje.PENDIENTE -> R.string.dato_pendiente
}

@StringRes
fun ayudaDato(d: DatoRodaje): Int = when (d) {
    DatoRodaje.POTENCIA -> R.string.dato_potencia_ayuda
    DatoRodaje.CADENCIA -> R.string.dato_cadencia_ayuda
    DatoRodaje.TIEMPO -> R.string.dato_tiempo_ayuda
    DatoRodaje.W_KG -> R.string.dato_w_kg_ayuda
    DatoRodaje.POTENCIA_MEDIA -> R.string.dato_potencia_media_ayuda
    DatoRodaje.POTENCIA_MAXIMA -> R.string.dato_potencia_maxima_ayuda
    DatoRodaje.VELOCIDAD -> R.string.dato_velocidad_ayuda
    DatoRodaje.DISTANCIA -> R.string.dato_distancia_ayuda
    DatoRodaje.PENDIENTE -> R.string.dato_pendiente_ayuda
}

/**
 * El valor más largo de cada dato, con el que se mide la cifra de la rejilla de Rodaje (README de `MetricGrid`): así su
 * tamaño no depende de lo que marque y no cambia mientras se rueda. Con números tabulares, el 8 mide lo que cualquier
 * cifra.
 */
fun plantillaDato(d: DatoRodaje): String = when (d) {
    DatoRodaje.POTENCIA, DatoRodaje.POTENCIA_MEDIA, DatoRodaje.POTENCIA_MAXIMA -> "8888"
    DatoRodaje.CADENCIA -> "188"
    DatoRodaje.TIEMPO -> "8:88:88"
    DatoRodaje.W_KG -> "88,8"
    DatoRodaje.VELOCIDAD -> "88,8"
    DatoRodaje.DISTANCIA -> "888,88"
    DatoRodaje.PENDIENTE -> "−88,8"
}

/** «Potencia y cadencia», «Tiempo, potencia y W/kg»: los datos en su orden, para la entrada de Ajustes. */
@Composable
fun resumenDatos(datos: List<DatoRodaje>): String {
    val nombres = datos.mapIndexed { i, d ->
        val n = stringResource(nombreDato(d))
        // Tras el primero, en minúscula salvo las unidades («W/kg»).
        if (i == 0 || d == DatoRodaje.W_KG) n else n.replaceFirstChar { it.lowercase() }
    }
    return unirConY(nombres, stringResource(R.string.y))
}

/** «a», «a y b», «a, b y c». */
fun unirConY(partes: List<String>, y: String): String = when (partes.size) {
    0 -> ""
    1 -> partes[0]
    else -> partes.dropLast(1).joinToString(", ") + " $y " + partes.last()
}
