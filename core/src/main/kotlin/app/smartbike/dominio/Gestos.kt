package app.smartbike.dominio

import app.smartbike.protocolo.SquareButton
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Un botón del Square, solo o con el Campagnolo que ya estaba pulsado al pulsarlo (design.md del change
 * `gestos-y-asignacion`, decisión 1). Un Campagnolo solo es una entrada sin modificador.
 */
@Serializable
data class Entrada(val boton: SquareButton, val modificador: SquareButton? = null) {
    init {
        require(modificador == null || (modificador.esModificador && modificador != boton)) { "Modificador no válido: $modificador" }
    }

    /** La misma entrada en la palanca del otro lado, con el Campagnolo de su lado («Invertir palancas»). */
    fun reflejada(): Entrada {
        val otra = REFLEJO[boton] ?: return this
        return Entrada(otra, modificador?.let { REFLEJO.getValue(it) })
    }

    val esPalanca: Boolean get() = boton in PALANCAS

    companion object {
        val PALANCAS = setOf(
            SquareButton.PalancaDerecha1,
            SquareButton.PalancaDerecha2,
            SquareButton.PalancaIzquierda1,
            SquareButton.PalancaIzquierda2,
        )

        private val REFLEJO = mapOf(
            SquareButton.PalancaDerecha1 to SquareButton.PalancaIzquierda1,
            SquareButton.PalancaIzquierda1 to SquareButton.PalancaDerecha1,
            SquareButton.PalancaDerecha2 to SquareButton.PalancaIzquierda2,
            SquareButton.PalancaIzquierda2 to SquareButton.PalancaDerecha2,
            SquareButton.CampagnoloDerecho to SquareButton.CampagnoloIzquierdo,
            SquareButton.CampagnoloIzquierdo to SquareButton.CampagnoloDerecho,
        )
    }
}

/** Gestos sobre una entrada (spec `button-gestures`). [Repetir] no se asigna: repite la acción de [Pulsar]. */
@Serializable
enum class TipoGesto {
    @SerialName("pulsar")
    Pulsar,

    @SerialName("corta")
    Corta,

    @SerialName("larga")
    Larga,

    @SerialName("doble")
    Doble,

    @SerialName("repetir")
    Repetir,
    ;

    companion object {
        /** Los que se eligen al asignar, en el orden de la pantalla. */
        val ASIGNABLES = listOf(Pulsar, Corta, Larga, Doble)
    }
}

/** Gesto reconocido, con la hora (monotónica) en que se reconoció. */
data class Gesto(val entrada: Entrada, val tipo: TipoGesto, val tMs: Long) {
    val boton: SquareButton get() = entrada.boton
}

/**
 * Tiempos de los gestos en ms (F3.5), con sus límites y pasos de [PASO_MS]. La larga y el retardo de repetición son de
 * 800 ms porque, medida con las notificaciones del Square, una pulsación normal llega a durar ~650 ms (design.md del
 * change `gestos-y-asignacion`, riesgos).
 */
@Serializable
data class TiemposGestos(
    val largaMs: Int = 800,
    val dobleMs: Int = 300,
    val retardoRepeticionMs: Int = 800,
    val ritmoRepeticionMs: Int = 250,
) {
    /** Los mismos tiempos dentro de sus límites, para un documento guardado a mano o de otra versión. */
    fun normalizados() = TiemposGestos(
        largaMs.coerceIn(LARGA),
        dobleMs.coerceIn(DOBLE),
        retardoRepeticionMs.coerceIn(RETARDO),
        ritmoRepeticionMs.coerceIn(RITMO),
    )

    companion object {
        const val PASO_MS = 50
        val LARGA = 300..1500
        val DOBLE = 150..600
        val RETARDO = 300..1000
        val RITMO = 100..1000
    }
}

/**
 * Eventos de botón → gestos (design.md del change `gestos-y-asignacion`, decisiones 1–3). Puro y sin reloj: la hora
 * llega con cada evento y con [vencer], que la `Sesion` llama en [proximoVencimiento].
 *
 * Solo hace esperar a lo ambiguo, según los gestos que la [asignacion] da a cada entrada: con «pulsar», el gesto sale
 * al pulsar; con «corta» sin «doble», al soltar; con «doble», al pasar la ventana. Una entrada sin reglas da «pulsar»
 * al instante (para Diagnóstico), salvo un Campagnolo, que nunca da «pulsar» y da «corta» al soltarlo.
 */
class ReconocedorGestos(
    var asignacion: Asignacion = Asignacion.POR_DEFECTO,
    var tiempos: TiemposGestos = TiemposGestos(),
    var autorrepeticion: Boolean = true,
) {
    private enum class Modo { Pulsar, Ambiguo }

    private class Pulsacion(
        val entrada: Entrada?,
        val modo: Modo,
        val tipos: Set<TipoGesto>,
        /** Hora de la larga o de la siguiente repetición; `null` si no hay ninguna pendiente. */
        var vence: Long?,
        /** Ya dio su gesto (larga o doble): no da nada al soltar. */
        var resuelta: Boolean = false,
        /** Un Campagnolo durante el que se pulsó otro botón: hizo de modificador y no da gestos propios. */
        var usada: Boolean = false,
    )

    private val pulsados = mutableMapOf<SquareButton, Pulsacion>()

    /** Cortas que esperan a la ventana de doble pulsación: entrada → hora en que vence. */
    private val esperando = linkedMapOf<Entrada, Long>()

    fun procesar(evento: EventoBoton): List<Gesto> {
        val gestos = vencer(evento.tMs).toMutableList()
        when (evento) {
            is EventoBoton.Pulsado -> pulsar(evento, gestos)
            is EventoBoton.Soltado -> soltar(evento, gestos)
        }
        return gestos
    }

    /** Gestos cuya hora ha llegado en [ahoraMs]: larga, repetición y corta tras la ventana, por orden de hora. */
    fun vencer(ahoraMs: Long): List<Gesto> {
        val gestos = mutableListOf<Gesto>()
        while (true) {
            val t = proximoVencimiento() ?: break
            if (t > ahoraMs) break
            val corta = esperando.entries.firstOrNull { it.value == t }
            if (corta != null) {
                esperando.remove(corta.key)
                gestos += Gesto(corta.key, TipoGesto.Corta, t)
                continue
            }
            val p = pulsados.values.first { it.vence == t }
            val entrada = checkNotNull(p.entrada)
            if (p.modo == Modo.Pulsar) {
                gestos += Gesto(entrada, TipoGesto.Repetir, t)
                p.vence = t + tiempos.ritmoRepeticionMs
            } else {
                gestos += Gesto(entrada, TipoGesto.Larga, t)
                p.vence = null
                p.resuelta = true
            }
        }
        return gestos
    }

    fun proximoVencimiento(): Long? = (pulsados.values.mapNotNull { it.vence } + esperando.values).minOrNull()

    /** Para la repetición de [entrada] hasta que se suelte (tope durante la repetición, decisión 8). */
    fun pararRepeticion(entrada: Entrada) {
        pulsados.values.filter { it.entrada == entrada && it.modo == Modo.Pulsar }.forEach { it.vence = null }
    }

    /** Square perdido: se olvida todo sin dar gestos, para que nada se repita ni salga una corta al perderlo. */
    fun reiniciar() {
        pulsados.clear()
        esperando.clear()
    }

    private fun pulsar(e: EventoBoton.Pulsado, gestos: MutableList<Gesto>) {
        val otros = SquareButton.pulsados(e.mascara).filter { it != e.boton }
        // Un Campagnolo mantenido mientras se pulsa otro botón hace de modificador y ya no da gestos propios.
        pulsados.forEach { (b, p) ->
            if (b.esModificador) {
                p.usada = true
                p.vence = null
            }
        }
        val entrada = entradaDe(e.boton, otros)
        if (entrada == null) {
            pulsados[e.boton] = Pulsacion(null, Modo.Ambiguo, emptySet(), vence = null, resuelta = true)
            return
        }
        val tipos = asignacion.gestos(entrada)
        val modo = if (TipoGesto.Pulsar in tipos || (tipos.isEmpty() && !e.boton.esModificador)) Modo.Pulsar else Modo.Ambiguo
        if (modo == Modo.Pulsar) {
            gestos += Gesto(entrada, TipoGesto.Pulsar, e.tMs)
            val repite = autorrepeticion && asignacion.accion(entrada, TipoGesto.Pulsar)?.repetible == true
            pulsados[e.boton] = Pulsacion(entrada, modo, tipos, vence = if (repite) e.tMs + tiempos.retardoRepeticionMs else null)
            return
        }
        val vence = esperando[entrada]
        if (vence != null && TipoGesto.Doble in tipos) {
            esperando.remove(entrada)
            gestos += Gesto(entrada, TipoGesto.Doble, e.tMs)
            pulsados[e.boton] = Pulsacion(entrada, modo, tipos, vence = null, resuelta = true)
            return
        }
        val larga = if (TipoGesto.Larga in tipos) e.tMs + tiempos.largaMs else null
        pulsados[e.boton] = Pulsacion(entrada, modo, tipos, vence = larga)
    }

    private fun soltar(e: EventoBoton.Soltado, gestos: MutableList<Gesto>) {
        val p = pulsados.remove(e.boton) ?: return
        val entrada = p.entrada ?: return
        if (p.modo == Modo.Pulsar || p.resuelta || p.usada) return
        if (TipoGesto.Doble in p.tipos) {
            esperando[entrada] = e.tMs + tiempos.dobleMs
        } else {
            gestos += Gesto(entrada, TipoGesto.Corta, e.tMs)
        }
    }

    /**
     * La entrada de un botón al pulsarlo: con el único Campagnolo que ya estaba pulsado como modificador. Con los dos
     * Campagnolo pulsados no hay entrada; un Campagnolo solo lo es si no había otro botón pulsado.
     */
    private fun entradaDe(boton: SquareButton, otros: List<SquareButton>): Entrada? {
        if (boton.esModificador) return if (otros.isEmpty()) Entrada(boton) else null
        val modificadores = otros.filter { it.esModificador }
        return when (modificadores.size) {
            0 -> Entrada(boton)
            1 -> Entrada(boton, modificadores.single())
            else -> null
        }
    }
}
