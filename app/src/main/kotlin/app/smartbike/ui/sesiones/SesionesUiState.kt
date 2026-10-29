package app.smartbike.ui.sesiones

import app.smartbike.datos.EstadoGrabacion
import app.smartbike.datos.EstadoSesion
import app.smartbike.datos.SesionGrabada

/** Lo que pide confirmar la pantalla antes de borrar (maquetas/sesiones.html): nada, una sesión o todas. */
sealed interface Confirmacion {
    data object Ninguna : Confirmacion
    data class Una(val nombre: String) : Confirmacion
    data object Todas : Confirmacion
}

/**
 * Pantalla Sesiones grabadas (maquetas/sesiones.html; spec `session-recording`): el estado de la grabación, la lista de
 * sesiones, la más reciente primero, y la confirmación de borrar.
 */
data class SesionesUiState(
    val sesiones: List<SesionGrabada> = emptyList(),
    val grabacion: EstadoGrabacion = EstadoGrabacion.Apagada,
    val confirmacion: Confirmacion = Confirmacion.Ninguna,
) {
    val vacia get() = sesiones.isEmpty()

    /** Lo que «Borrar todas» borraría: todo menos la que se graba. */
    val borrables get() = sesiones.filter { it.borrable }

    /** «Borrar todas» solo se ofrece si hay algo que borrar. */
    val puedeBorrarTodas get() = borrables.isNotEmpty()

    /** Pide confirmar el borrado de [nombre]; la que se graba no se ofrece. */
    fun pedirBorrar(nombre: String) = if (sesiones.any { it.nombre == nombre && it.borrable }) copy(confirmacion = Confirmacion.Una(nombre)) else this

    fun pedirBorrarTodas() = if (puedeBorrarTodas) copy(confirmacion = Confirmacion.Todas) else this

    fun cancelar() = copy(confirmacion = Confirmacion.Ninguna)
}

/** Lo que dice la fila de una sesión además de su tamaño: nada si está cerrada. */
fun SesionGrabada.motivo(): EstadoSesion? = estado.takeIf { it != EstadoSesion.CERRADA }
