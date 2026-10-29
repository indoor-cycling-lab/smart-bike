package app.smartbike.ui.rodaje

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Alto de Rodaje en vertical por debajo del cual la pantalla es «baja» (spec `ride-screen`, «Pantalla baja en vertical»). */
val ALTO_PANTALLA_BAJA = 760.dp

/** Con la pantalla baja y los botones − y + activados, los datos que muestra la rejilla. */
const val DATOS_CON_BOTONES = 4

fun esPantallaBaja(alto: Dp): Boolean = alto < ALTO_PANTALLA_BAJA

/** Los datos que Rodaje pinta y los que oculta, en el orden elegido. */
data class RecorteDatos<T>(val visibles: List<T>, val ocultos: List<T>)

/**
 * Con la pantalla baja y los botones activados, solo los primeros [DATOS_CON_BOTONES] datos; en cualquier otro caso, todos
 * (spec `ride-screen`, «Datos con botones en pantalla baja»). No cambia los datos elegidos: solo lo que se pinta.
 */
fun <T> recorteDatos(alto: Dp, botones: Boolean, datos: List<T>): RecorteDatos<T> = if (botones && esPantallaBaja(alto)) {
    RecorteDatos(datos.take(DATOS_CON_BOTONES), datos.drop(DATOS_CON_BOTONES))
} else {
    RecorteDatos(datos, emptyList())
}
