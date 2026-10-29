package app.smartbike.dominio

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Definición de la transmisión (F5.7): la lineal de N marchas numeradas o una de platos y piñones. El desarrollo es un
 * dato, para que añadir otra marca no cambie el código (design.md del change `transmisiones-de-marca`, decisión 1).
 */
sealed interface Transmision {
    /** Lo que identifica la transmisión en la configuración guardada. */
    val id: String

    data class Lineal(val marchas: Int = 24) : Transmision {
        init {
            require(marchas >= 2) { "Una transmisión necesita al menos 2 marchas: $marchas" }
        }

        override val id: String get() = ID

        fun marcha(posicion: Int) = EstadoMarcha.Numerada(posicion, marchas)

        companion object {
            const val ID = "lineal"
        }
    }

    /** Platos y piñones en dientes, de menor a mayor, y los puntos del modo automático. */
    data class Platos(
        override val id: String,
        val nombre: String,
        val platos: List<Int>,
        val pinones: List<Int>,
        val sincronizado: Sincronizado,
        /** La marcha de referencia mientras el usuario no fije otra. */
        val referencia: EstadoMarcha.PlatoPinon,
    ) : Transmision {
        init {
            require(platos.isNotEmpty() && platos == platos.sorted().distinct()) { "Platos de menor a mayor: $platos" }
            require(pinones.size >= 2 && pinones == pinones.sorted().distinct()) { "Piñones de menor a mayor: $pinones" }
            require(sincronizado.subirDesdePinon in pinones && sincronizado.bajarDesdePinon in pinones) {
                "Puntos de cambio fuera del cassette: $sincronizado"
            }
            require(contiene(referencia)) { "Referencia fuera de la transmisión: $referencia" }
        }

        val masLigera: EstadoMarcha.PlatoPinon get() = EstadoMarcha.PlatoPinon(platos.first(), pinones.last())
        val masDura: EstadoMarcha.PlatoPinon get() = EstadoMarcha.PlatoPinon(platos.last(), pinones.first())

        /** Las combinaciones de la más ligera a la más dura; a igual relación, primero el plato pequeño. */
        val ordenadas: List<EstadoMarcha.PlatoPinon> by lazy {
            platos.flatMap { p -> pinones.map { q -> EstadoMarcha.PlatoPinon(p, q) } }
                .sortedWith(compareBy({ it.relacion }, { it.plato }))
        }

        fun contiene(m: EstadoMarcha): Boolean = m is EstadoMarcha.PlatoPinon && m.plato in platos && m.pinon in pinones
    }
}

/**
 * Puntos del modo automático (como el Synchro Shift de Shimano): en el plato pequeño, endurecer desde el piñón
 * [subirDesdePinon] o uno más pequeño pasa al plato grande; en el grande, aligerar desde el [bajarDesdePinon] o uno
 * más grande pasa al pequeño. Al cambiar de plato, el piñón se mueve [compensacion] posiciones para compensar.
 * Validados por el usuario al rodar (decisión 10).
 */
data class Sincronizado(val subirDesdePinon: Int, val bajarDesdePinon: Int, val compensacion: Int)

/** Desarrollos de fábrica (decisión 5 de `funcional.md`): por ahora, solo el del usuario. */
object Desarrollos {
    val SHIMANO_105 = Transmision.Platos(
        id = "shimano-105",
        nombre = "Shimano 105",
        platos = listOf(36, 52),
        pinones = listOf(11, 12, 13, 14, 15, 17, 19, 21, 24, 28, 32, 36),
        sincronizado = Sincronizado(subirDesdePinon = 15, bajarDesdePinon = 24, compensacion = 2),
        referencia = EstadoMarcha.PlatoPinon(36, 17),
    )

    val TODOS: List<Transmision.Platos> = listOf(SHIMANO_105)

    fun porId(id: String): Transmision.Platos? = TODOS.firstOrNull { it.id == id }
}

/** Cómo se cambia con una transmisión de platos (F5.8). Con la lineal no se usa. */
@Serializable
enum class ModoCambio {
    /** La derecha recorre una secuencia que cambia de plato sola, como el Di2 en Synchro Shift. */
    @SerialName("automatico")
    AUTOMATICO,

    /** La derecha cambia el piñón y la izquierda el plato, como los mandos. */
    @SerialName("manual")
    MANUAL,
}

/** Marcha puesta: un número de 1 (la más ligera) a [Numerada.total], o un plato y un piñón en dientes (decisión 2). */
sealed interface EstadoMarcha {
    data class Numerada(val posicion: Int, val total: Int) : EstadoMarcha {
        init {
            require(posicion in 1..total) { "Marcha $posicion fuera de 1..$total" }
        }
    }

    @Serializable
    data class PlatoPinon(val plato: Int, val pinon: Int) : EstadoMarcha {
        val relacion: Double get() = plato.toDouble() / pinon

        /** Como se escribe en los registros y en Diagnóstico: `36×17`. */
        override fun toString() = "$plato×$pinon"
    }
}

/** Por qué no se puede cambiar: el extremo de la transmisión, o el del plato o el piñón en manual (decisión 3). */
sealed interface TopeMarcha {
    data object MasDura : TopeMarcha
    data object MasLigera : TopeMarcha
    data class Plato(val dientes: Int) : TopeMarcha
    data class Pinon(val dientes: Int) : TopeMarcha
}

sealed interface ResultadoCambio {
    /**
     * La marcha cambia; un salto que sobrepasa el extremo se recorta y también cuenta como cambio. [pasos] es lo que
     * avanzó, positivo si es más dura: posiciones en la lineal, piñones en manual y pasos de la secuencia en automático.
     */
    data class Cambiada(val anterior: EstadoMarcha, val nueva: EstadoMarcha, val pasos: Int) : ResultadoCambio {
        /** Con platos, si cambió el plato (el aviso dice «Plato N»). */
        val cambiaPlato: Boolean
            get() = anterior is EstadoMarcha.PlatoPinon && nueva is EstadoMarcha.PlatoPinon && anterior.plato != nueva.plato
    }

    /** Ya estaba en el tope en el sentido pedido (F5.4). */
    data class Tope(val tope: TopeMarcha) : ResultadoCambio

    /** La acción pide la marcha que ya está puesta (volver a la referencia estando en ella). */
    data object SinCambio : ResultadoCambio

    /** La marcha puesta pasa a ser la de referencia; la marcha no cambia. */
    data class ReferenciaFijada(val referencia: EstadoMarcha) : ResultadoCambio
}

/**
 * Aplica las acciones de marcha con topes (F5.1–F5.4, F5.8). Puro: no guarda la marcha ni la referencia, las recibe en
 * cada llamada (design.md del change `gestos-y-asignacion`, decisión 5). Con platos, el [modo] decide qué hacen las
 * acciones, con las reglas de la decisión 3 del change `transmisiones-de-marca`.
 */
class Cambio(val transmision: Transmision, val modo: ModoCambio = ModoCambio.AUTOMATICO) {
    /** Una marcha de la lineal; con platos no tiene sentido. */
    fun marcha(posicion: Int) = (transmision as Transmision.Lineal).marcha(posicion)

    fun aplicar(actual: EstadoMarcha, accion: Accion, referencia: EstadoMarcha): ResultadoCambio = when (transmision) {
        is Transmision.Lineal -> lineal(transmision, actual as EstadoMarcha.Numerada, accion, referencia)
        is Transmision.Platos -> platos(transmision, actual as EstadoMarcha.PlatoPinon, accion, referencia)
    }

    private fun lineal(t: Transmision.Lineal, actual: EstadoMarcha.Numerada, accion: Accion, referencia: EstadoMarcha): ResultadoCambio {
        val total = t.marchas
        val (destino, tope) = when (accion) {
            is Accion.MarchaMas -> actual.posicion + accion.n to TopeMarcha.MasDura

            is Accion.MarchaMenos -> actual.posicion - accion.n to TopeMarcha.MasLigera

            Accion.MarchaMasDura -> total to TopeMarcha.MasDura

            Accion.MarchaMasLigera -> 1 to TopeMarcha.MasLigera

            Accion.MarchaReferencia -> ((referencia as? EstadoMarcha.Numerada)?.posicion ?: actual.posicion).coerceIn(1, total) to null

            Accion.FijarReferencia -> return ResultadoCambio.ReferenciaFijada(actual)

            // Sin platos, las acciones de plato no hacen nada; las de ERG, las de pantalla y las funciones de Zwift no
            // cambian la marcha.
            Accion.PlatoMas, Accion.PlatoMenos, Accion.ErgMas, Accion.ErgMenos, Accion.AlternarErgManual,
            Accion.PantallaAnterior, Accion.PantallaSiguiente, Accion.SiguienteModelo, Accion.VentiladorAlternar, Accion.VentiladorMas, Accion.VentiladorMenos, is Accion.Mando,
            -> return ResultadoCambio.SinCambio
        }
        val recortado = destino.coerceIn(1, total)
        return when {
            recortado != actual.posicion -> ResultadoCambio.Cambiada(actual, t.marcha(recortado), recortado - actual.posicion)
            tope != null && (actual.posicion == 1 || actual.posicion == total) -> ResultadoCambio.Tope(tope)
            else -> ResultadoCambio.SinCambio
        }
    }

    private fun platos(t: Transmision.Platos, actual: EstadoMarcha.PlatoPinon, accion: Accion, referencia: EstadoMarcha): ResultadoCambio = when (accion) {
        is Accion.MarchaMas -> pasos(t, actual, accion.n, dura = true)

        is Accion.MarchaMenos -> pasos(t, actual, accion.n, dura = false)

        Accion.MarchaMasDura -> irA(t, actual, t.masDura, TopeMarcha.MasDura)

        Accion.MarchaMasLigera -> irA(t, actual, t.masLigera, TopeMarcha.MasLigera)

        Accion.MarchaReferencia -> irA(t, actual, referencia as? EstadoMarcha.PlatoPinon ?: actual, null)

        Accion.FijarReferencia -> ResultadoCambio.ReferenciaFijada(actual)

        Accion.PlatoMas -> plato(t, actual, mas = true)

        Accion.PlatoMenos -> plato(t, actual, mas = false)

        Accion.ErgMas, Accion.ErgMenos, Accion.AlternarErgManual, Accion.PantallaAnterior, Accion.PantallaSiguiente,
        Accion.SiguienteModelo, Accion.VentiladorAlternar, Accion.VentiladorMas, Accion.VentiladorMenos, is Accion.Mando,
        -> ResultadoCambio.SinCambio
    }

    /** [n] pasos en un sentido; si el primero ya es un tope, es un tope, y si no, se para en él sin avisar. */
    private fun pasos(t: Transmision.Platos, actual: EstadoMarcha.PlatoPinon, n: Int, dura: Boolean): ResultadoCambio {
        var marcha = actual
        repeat(n) {
            marcha = paso(t, marcha, dura)
                ?: return if (marcha == actual) ResultadoCambio.Tope(tope(t, marcha, dura)) else cambiada(t, actual, marcha)
        }
        return cambiada(t, actual, marcha)
    }

    /** Un paso: la marcha siguiente en ese sentido, o `null` si está en un tope. */
    private fun paso(t: Transmision.Platos, m: EstadoMarcha.PlatoPinon, dura: Boolean): EstadoMarcha.PlatoPinon? {
        val p = t.platos.indexOf(m.plato)
        val q = t.pinones.indexOf(m.pinon) // 0 = el piñón más pequeño (el más duro)
        val s = t.sincronizado
        if (modo == ModoCambio.AUTOMATICO) {
            if (dura && p < t.platos.lastIndex && q <= t.pinones.indexOf(s.subirDesdePinon)) {
                return EstadoMarcha.PlatoPinon(t.platos[p + 1], t.pinones[(q + s.compensacion).coerceAtMost(t.pinones.lastIndex)])
            }
            if (!dura && p > 0 && q >= t.pinones.indexOf(s.bajarDesdePinon)) {
                return EstadoMarcha.PlatoPinon(t.platos[p - 1], t.pinones[(q - s.compensacion).coerceAtLeast(0)])
            }
        }
        val siguiente = if (dura) q - 1 else q + 1
        return if (siguiente in t.pinones.indices) EstadoMarcha.PlatoPinon(m.plato, t.pinones[siguiente]) else null
    }

    /** El tope de [m] en ese sentido: el extremo absoluto, o en manual el del piñón de ese plato. */
    private fun tope(t: Transmision.Platos, m: EstadoMarcha.PlatoPinon, dura: Boolean): TopeMarcha = when {
        dura && m == t.masDura -> TopeMarcha.MasDura
        !dura && m == t.masLigera -> TopeMarcha.MasLigera
        else -> TopeMarcha.Pinon(m.pinon)
    }

    /** Cambio de plato: en manual, el mismo piñón; en automático, compensando para que la marcha no salte tanto. */
    private fun plato(t: Transmision.Platos, actual: EstadoMarcha.PlatoPinon, mas: Boolean): ResultadoCambio {
        val p = t.platos.indexOf(actual.plato)
        val destino = if (mas) p + 1 else p - 1
        if (destino !in t.platos.indices) return ResultadoCambio.Tope(TopeMarcha.Plato(actual.plato))
        val q = t.pinones.indexOf(actual.pinon)
        val compensacion = if (modo == ModoCambio.AUTOMATICO) t.sincronizado.compensacion else 0
        val nuevoQ = (if (mas) q + compensacion else q - compensacion).coerceIn(t.pinones.indices)
        return cambiada(t, actual, EstadoMarcha.PlatoPinon(t.platos[destino], t.pinones[nuevoQ]))
    }

    private fun irA(t: Transmision.Platos, actual: EstadoMarcha.PlatoPinon, destino: EstadoMarcha.PlatoPinon, tope: TopeMarcha?): ResultadoCambio = when {
        destino != actual -> cambiada(t, actual, destino)
        tope != null -> ResultadoCambio.Tope(tope)
        else -> ResultadoCambio.SinCambio
    }

    /** Con el mismo plato, los pasos son los piñones que avanzó; si cambia el plato, los puestos en [Transmision.Platos.ordenadas]. */
    private fun cambiada(t: Transmision.Platos, de: EstadoMarcha.PlatoPinon, a: EstadoMarcha.PlatoPinon): ResultadoCambio {
        if (a == de) return ResultadoCambio.SinCambio
        val pasos = if (a.plato == de.plato) {
            t.pinones.indexOf(de.pinon) - t.pinones.indexOf(a.pinon)
        } else {
            t.ordenadas.indexOf(a) - t.ordenadas.indexOf(de)
        }
        return ResultadoCambio.Cambiada(de, a, pasos)
    }
}
