package app.smartbike.ui.botones

import app.smartbike.dominio.Accion
import app.smartbike.dominio.Asignacion
import app.smartbike.dominio.Entrada
import app.smartbike.dominio.Regla
import app.smartbike.dominio.TipoGesto
import app.smartbike.sesion.EstadoCaptura

/** Avisos de Asignar acción sobre la entrada capturada (spec `button-mapping`). */
sealed interface AvisoAsignar {
    /** La entrada con su gesto no tiene acción. */
    data class Libre(val entrada: Entrada, val gesto: TipoGesto) : AvisoAsignar

    /** Ya la tiene esta misma acción: guardar no cambia nada. */
    data class YaAsignada(val entrada: Entrada, val gesto: TipoGesto) : AvisoAsignar

    /** Otras acciones perderán esa entrada al guardar (aviso de peligro). */
    data class EnUso(val perdidas: List<Regla>) : AvisoAsignar
}

/** Pantalla de Asignar acción (maquetas/asignar.html). */
data class AsignarAccionUiState(
    val accion: Accion,
    /** Las entradas físicas de la acción, sin las que se han quitado y aún no se han guardado. */
    val reglas: List<Regla> = emptyList(),
    val captura: EstadoCaptura = EstadoCaptura.SinSquare,
    val gesto: TipoGesto = TipoGesto.Pulsar,
    val avisos: List<AvisoAsignar> = emptyList(),
    val puedeGuardar: Boolean = false,
) {
    val capturada: Entrada? get() = (captura as? EstadoCaptura.Capturada)?.entrada

    /** «Pulsar» no se puede elegir para un Campagnolo solo (spec `button-gestures`). */
    val gestosDeshabilitados: Set<TipoGesto>
        get() = if (capturada?.let { it.boton.esModificador && it.modificador == null } == true) setOf(TipoGesto.Pulsar) else emptySet()
}

/**
 * Lo que muestra la pantalla: la asignación guardada [asignacion] con las reglas [quitadas] de la acción, la captura y
 * el gesto elegido. Sin interruptor del mando, no hay aviso de los botones que lee Zwift: un botón reasignado deja de
 * llegar a la app de entrenamiento (design.md del change `pestana-conexion`; spec `button-mapping`).
 */
fun estadoDeAsignar(
    accion: Accion,
    asignacion: Asignacion,
    quitadas: List<Regla>,
    captura: EstadoCaptura,
    gesto: TipoGesto,
): AsignarAccionUiState {
    val reglas = asignacion.reglasDe(accion).filterNot { it in quitadas }
    val entrada = (captura as? EstadoCaptura.Capturada)?.entrada
    val avisos = buildList {
        if (entrada == null) return@buildList
        val perdidas = asignacion.enConflicto(entrada, gesto, accion)
        val ya = asignacion.reglasDe(accion).any { it.entrada == entrada && it.gesto == gesto } && Regla(entrada, gesto, accion) !in quitadas
        when {
            perdidas.isNotEmpty() -> add(AvisoAsignar.EnUso(perdidas))
            ya -> add(AvisoAsignar.YaAsignada(entrada, gesto))
        }
        if (isEmpty()) add(AvisoAsignar.Libre(entrada, gesto))
    }
    return AsignarAccionUiState(
        accion = accion,
        reglas = reglas,
        captura = captura,
        gesto = gesto,
        avisos = avisos,
        puedeGuardar = entrada != null || quitadas.isNotEmpty(),
    )
}

/** La asignación al guardar: sin las reglas quitadas y con la entrada capturada, que se lleva las que chocan. */
fun asignacionAlGuardar(asignacion: Asignacion, accion: Accion, quitadas: List<Regla>, capturada: Entrada?, gesto: TipoGesto): Asignacion {
    val sinQuitadas = quitadas.fold(asignacion) { a, r -> a.quitar(r.entrada, r.gesto) }
    return capturada?.let { sinQuitadas.asignar(it, gesto, accion) } ?: sinQuitadas
}
