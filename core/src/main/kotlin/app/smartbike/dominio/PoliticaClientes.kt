package app.smartbike.dominio

/**
 * Qué hace DirCon cuando llega un cliente y ya hay otro (spec `device-connection`; design.md del change
 * `endurecer-dircon-ble`, decisión 2). El nuevo sustituye al actual si no hay actual, si viene de la misma dirección
 * (Zwift que vuelve tras un corte) o si el actual lleva [SILENCIO_SUSTITUIBLE_MS] sin enviar nada; si no, se rechaza.
 */
object PoliticaClientes {
    const val SILENCIO_SUSTITUIBLE_MS = 15_000L

    /** El cliente conectado: su dirección y el instante (ms monotónicos) de su último mensaje. */
    data class Actual(val direccion: String, val ultimoMensajeMs: Long)

    enum class Decision { Sustituir, Rechazar }

    fun decidir(actual: Actual?, direccionNuevo: String, ahoraMs: Long): Decision = when {
        actual == null -> Decision.Sustituir
        actual.direccion == direccionNuevo -> Decision.Sustituir
        ahoraMs - actual.ultimoMensajeMs >= SILENCIO_SUSTITUIBLE_MS -> Decision.Sustituir
        else -> Decision.Rechazar
    }
}
