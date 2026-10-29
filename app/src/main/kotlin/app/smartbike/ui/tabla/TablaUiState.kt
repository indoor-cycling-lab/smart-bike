package app.smartbike.ui.tabla

import app.smartbike.dominio.Configuracion
import app.smartbike.dominio.EstadoMarcha
import app.smartbike.dominio.Transmision
import app.smartbike.dominio.escalera
import kotlin.math.roundToInt

/** Una fila de la tabla: la marcha, su ajuste (en %) y si es la puesta. */
data class FilaTabla(val marcha: EstadoMarcha, val ajustePct: Double, val puesta: Boolean) {
    val puedeBajar get() = ajustePct > Configuracion.AJUSTE_TABLA_PCT.start + 1e-9
    val puedeSubir get() = ajustePct < Configuracion.AJUSTE_TABLA_PCT.endInclusive - 1e-9
}

/**
 * Tabla de marchas (maquetas/tabla-marchas.html): la transmisión elegida con una fila por marcha, de la más ligera a la
 * más dura (spec `settings-screen`, «Pantalla Tabla de marchas»).
 */
data class TablaUiState(
    val transmision: Transmision = Transmision.Lineal(),
    val filas: List<FilaTabla> = emptyList(),
) {
    /** La fila de la marcha puesta, para abrir la lista desplazada a ella. */
    val indicePuesta: Int get() = filas.indexOfFirst { it.puesta }.coerceAtLeast(0)
}

fun estadoDeTabla(c: Configuracion, puesta: EstadoMarcha?): TablaUiState {
    val t = c.transmisionElegida
    val tabla = c.tabla(t)
    return TablaUiState(t, t.escalera().mapIndexed { i, m -> FilaTabla(m, tabla[i], m == puesta) })
}

/** Un paso de [Configuracion.PASO_TABLA_PCT] desde el valor mostrado (a una décima), en sus límites. */
fun ajusteMas(pct: Double, pasos: Int): Double = (((pct * 10).roundToInt() + pasos) / 10.0).coerceIn(Configuracion.AJUSTE_TABLA_PCT)

/** La tabla con la fila [indice] cambiada [pasos] pasos. */
fun ajustarFila(tabla: List<Double>, indice: Int, pasos: Int): List<Double> = tabla.toMutableList().also { it[indice] = ajusteMas(it[indice], pasos) }
