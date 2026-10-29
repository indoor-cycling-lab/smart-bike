package app.smartbike.ui.datos

import androidx.compose.ui.unit.Dp
import app.smartbike.dominio.Configuracion
import app.smartbike.dominio.DatoRodaje
import app.smartbike.dominio.EstadoMarcha
import app.smartbike.ui.rodaje.recorteDatos

/**
 * Datos en pantalla (maquetas/datos.html): los elegidos en su orden, los demás después, y la marcha de la vista previa
 * (spec `settings-screen`, «Pantalla Datos en pantalla»).
 */
data class DatosUiState(
    val elegidos: List<DatoRodaje> = Configuracion.DATOS_POR_DEFECTO,
    val marcha: EstadoMarcha = EstadoMarcha.Numerada(12, 24),
    /** Botones − y + en pantalla: con la pantalla baja, Rodaje solo muestra los primeros datos (spec `ride-screen`). */
    val botones: Boolean = false,
) {
    /** Los nueve, con los elegidos primero en su orden. */
    val filas: List<DatoRodaje> get() = elegidos + DatoRodaje.entries.filterNot { it in elegidos }

    /** Los datos que Rodaje no muestra con la pantalla de alto [alto] y los botones elegidos; con ellos, el aviso. */
    fun ocultos(alto: Dp): List<DatoRodaje> = recorteDatos(alto, botones, elegidos).ocultos

    /** El único elegido no se puede quitar. */
    fun bloqueado(d: DatoRodaje) = elegidos == listOf(d)

    /** Con el máximo marcado, los demás no se pueden añadir hasta quitar uno. */
    fun sinSitio(d: DatoRodaje) = d !in elegidos && elegidos.size >= Configuracion.MAX_DATOS
}

fun estadoDeDatos(c: Configuracion) = DatosUiState(c.datos, c.referencia, c.botonesEnPantalla)

/** Marca o desmarca [d]: uno nuevo va al final; el último no se quita. */
fun alternarDato(datos: List<DatoRodaje>, d: DatoRodaje): List<DatoRodaje> = when {
    d !in datos -> (datos + d).take(Configuracion.MAX_DATOS)
    datos.size > 1 -> datos - d
    else -> datos
}

/** Mueve [d] [pasos] puestos (negativo, hacia arriba) entre los elegidos, sin salirse de la lista. */
fun moverDato(datos: List<DatoRodaje>, d: DatoRodaje, pasos: Int): List<DatoRodaje> {
    val i = datos.indexOf(d)
    if (i < 0) return datos
    val destino = (i + pasos).coerceIn(0, datos.lastIndex)
    if (destino == i) return datos
    return datos.toMutableList().apply {
        removeAt(i)
        add(destino, d)
    }
}
