package app.smartbike.dominio

import app.smartbike.protocolo.SquareButton
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder

/**
 * Lo que hace la app ante un gesto o un botón de la pantalla (arquitectura.md § 5.3): marchas, platos, ERG, pantallas y
 * funciones de Zwift. Los nombres serializados son estables: se guardan en `configuracion.json`.
 */
@Serializable
sealed interface Accion {
    @Serializable
    @SerialName("marcha-mas")
    data class MarchaMas(val n: Int) : Accion

    @Serializable
    @SerialName("marcha-menos")
    data class MarchaMenos(val n: Int) : Accion

    @Serializable
    @SerialName("marcha-mas-dura")
    data object MarchaMasDura : Accion

    @Serializable
    @SerialName("marcha-mas-ligera")
    data object MarchaMasLigera : Accion

    @Serializable
    @SerialName("marcha-referencia")
    data object MarchaReferencia : Accion

    @Serializable
    @SerialName("fijar-referencia")
    data object FijarReferencia : Accion

    /** Con platos, al plato siguiente más grande (design.md del change `transmisiones-de-marca`, decisión 4). */
    @Serializable
    @SerialName("plato-mas")
    data object PlatoMas : Accion

    @Serializable
    @SerialName("plato-menos")
    data object PlatoMenos : Accion

    /** En ERG, [AjusteErg.PASO_W] más; en SIM no hace nada (design.md del change `erg-manual`, decisión 3). */
    @Serializable
    @SerialName("erg-mas")
    data object ErgMas : Accion

    @Serializable
    @SerialName("erg-menos")
    data object ErgMenos : Accion

    /** De SIM a ERG manual y vuelta; en ERG de Zwift no hace nada (spec `manual-erg`). */
    @Serializable
    @SerialName("alternar-erg")
    data object AlternarErgManual : Accion

    /**
     * A la pestaña anterior de la barra inferior, en orden circular; no cambia el estado de la sesión (design.md del
     * change `rodaje-completo-y-ajustes`, decisión 4).
     */
    @Serializable
    @SerialName("pantalla-anterior")
    data object PantallaAnterior : Accion

    @Serializable
    @SerialName("pantalla-siguiente")
    data object PantallaSiguiente : Accion

    /** Pasa al siguiente modelo de resistencia: simple, por tabla, físico y vuelta (spec `resistance-models`). */
    @Serializable
    @SerialName("siguiente-modelo")
    data object SiguienteModelo : Accion

    /** Pasa el ventilador de manual a automático y al revés (spec `fan-control`); no cambia la marcha. */
    @Serializable
    @SerialName("ventilador-alternar")
    data object VentiladorAlternar : Accion

    @Serializable
    @SerialName("ventilador-mas")
    data object VentiladorMas : Accion

    @Serializable
    @SerialName("ventilador-menos")
    data object VentiladorMenos : Accion

    /**
     * Una función del mando emulado de la plataforma: con «pulsar», mientras se mantiene el botón (design.md del change
     * `mando-zwift-emulado`, decisiones 1 y 3). No es repetible: la plataforma ya trata el botón mantenido. Conserva el
     * nombre serializado «zwift» de las configuraciones guardadas.
     */
    @Serializable
    @SerialName("zwift")
    data class Mando(val funcion: FuncionMando) : Accion

    /** Al mantener su botón con «pulsar», se repite (spec `button-gestures`, «Autorrepetición al mantener»). */
    val repetible: Boolean get() = this is MarchaMas || this is MarchaMenos || this == ErgMas || this == ErgMenos

    companion object {
        const val SALTO = 3

        /**
         * Las acciones de la app que se pueden asignar con la lineal, en el orden de la pantalla de Botones (spec
         * `button-mapping`). Las funciones de Zwift van aparte, en [ZWIFT].
         */
        val ASIGNABLES: List<Accion> by lazy {
            listOf(
                MarchaMas(1), MarchaMenos(1), MarchaMas(SALTO), MarchaMenos(SALTO),
                MarchaMasDura, MarchaMasLigera, MarchaReferencia, FijarReferencia,
                ErgMas, ErgMenos, AlternarErgManual, PantallaAnterior, PantallaSiguiente, SiguienteModelo,
                VentiladorAlternar, VentiladorMas, VentiladorMenos,
            )
        }

        /**
         * Todas las acciones asignables con alguna transmisión, para identificarlas por su posición. Las de ERG, las de
         * pantalla y las funciones de Zwift van al final, en el orden en que llegaron, para no mover las posiciones
         * anteriores. Perezosas, como [ASIGNABLES]: un `data object` que se inicializa antes que la interfaz la vería a
         * medio construir.
         */
        val TODAS: List<Accion> by lazy {
            ASIGNABLES.filterNot { it in ERG || it in PANTALLA || it == SiguienteModelo || it in VENTILADOR } + listOf(PlatoMas, PlatoMenos) + ERG + PANTALLA + ZWIFT + SiguienteModelo + VENTILADOR
        }

        /** Las funciones de Zwift, en el orden de su grupo en Botones; asignables con todas las transmisiones. */
        val ZWIFT: List<Accion> by lazy { FuncionZwift.entries.map(::Mando) }

        private val ERG: List<Accion> get() = listOf(ErgMas, ErgMenos, AlternarErgManual)

        /** Las acciones del ventilador, sin botón por defecto y no repetibles (design.md de `ventilador-headwind`, decisión 5). */
        val VENTILADOR: List<Accion> get() = listOf(VentiladorAlternar, VentiladorMas, VentiladorMenos)

        private val PANTALLA: List<Accion> get() = listOf(PantallaAnterior, PantallaSiguiente)

        /** Con platos, además, las de plato tras «−3 marchas»; con todas, las funciones de Zwift al final. */
        fun asignables(transmision: Transmision): List<Accion> = when (transmision) {
            is Transmision.Lineal -> ASIGNABLES
            is Transmision.Platos -> ASIGNABLES.take(4) + listOf(PlatoMas, PlatoMenos) + ASIGNABLES.drop(4)
        } + ZWIFT
    }
}

/** Una entrada con un gesto hace una acción. */
@Serializable
data class Regla(val entrada: Entrada, val gesto: TipoGesto, val accion: Accion)

/**
 * Qué hace cada entrada y gesto (design.md del change `gestos-y-asignacion`, decisión 4). Las reglas guardadas son las
 * de las palancas sin invertir; con [invertirPalancas], todo lo que se consulta o se cambia desde fuera habla de los
 * botones físicos, y la entrada de una palanca se refleja al otro lado.
 *
 * Cada entrada y gesto tiene como mucho una acción, y «pulsar» excluye «corta», «larga» y «doble» en la misma entrada.
 * Una combinación sin reglas hace lo del botón solo: el Campagnolo de un lado no cambia lo que hace la palanca del otro
 * (spec `virtual-gears`, «Modificador del otro lado»).
 */
@Serializable
data class Asignacion(
    @Serializable(with = ReglasSerializer::class) val reglas: List<Regla>,
    val invertirPalancas: Boolean = false,
) {
    private fun guardada(entrada: Entrada) = if (invertirPalancas && entrada.esPalanca) entrada.reflejada() else entrada

    /** Las reglas que se aplican a una entrada física. */
    private fun aplicables(entrada: Entrada): List<Regla> {
        val e = guardada(entrada)
        val propias = reglas.filter { it.entrada == e }
        if (propias.isNotEmpty() || e.modificador == null) return propias
        return reglas.filter { it.entrada == Entrada(e.boton) }
    }

    fun accion(entrada: Entrada, gesto: TipoGesto): Accion? {
        val tipo = if (gesto == TipoGesto.Repetir) TipoGesto.Pulsar else gesto
        return aplicables(entrada).firstOrNull { it.gesto == tipo }?.accion
    }

    fun accion(gesto: Gesto): Accion? = accion(gesto.entrada, gesto.tipo)

    /** Los gestos con acción de una entrada física. */
    fun gestos(entrada: Entrada): Set<TipoGesto> = aplicables(entrada).mapTo(mutableSetOf()) { it.gesto }

    /** Las reglas de [accion] con sus entradas físicas, en el orden en que se añadieron. */
    fun reglasDe(accion: Accion): List<Regla> = reglas.filter { it.accion == accion }.map { it.copy(entrada = guardada(it.entrada)) }

    /**
     * Las reglas de otras acciones que se perderían al asignar [gesto] de una entrada física a [accion]: la del mismo
     * gesto y las que excluye (spec `button-mapping`, «Aviso de entrada en uso»).
     */
    fun enConflicto(entrada: Entrada, gesto: TipoGesto, accion: Accion): List<Regla> {
        val e = guardada(entrada)
        return reglas.filter { it.entrada == e && it.accion != accion && excluye(it.gesto, gesto) }.map { it.copy(entrada = entrada) }
    }

    /** Añade la regla y quita las que choquen con ella. */
    fun asignar(entrada: Entrada, gesto: TipoGesto, accion: Accion): Asignacion {
        require(gesto != TipoGesto.Repetir) { "«Repetir» no se asigna" }
        val e = guardada(entrada)
        val quedan = reglas.filterNot { it.entrada == e && excluye(it.gesto, gesto) }
        return copy(reglas = quedan + Regla(e, gesto, accion))
    }

    /** Añade [regla] (con su entrada guardada) solo si no está ya y no choca con ninguna otra (migraciones). */
    fun conReglaSiLibre(regla: Regla): Asignacion {
        if (regla in reglas) return this
        val ocupada = reglas.any { it.entrada == regla.entrada && excluye(it.gesto, regla.gesto) }
        return if (ocupada) this else copy(reglas = reglas + regla)
    }

    /** Quita la regla de una entrada física. */
    fun quitar(entrada: Entrada, gesto: TipoGesto): Asignacion {
        val e = guardada(entrada)
        return copy(reglas = reglas.filterNot { it.entrada == e && it.gesto == gesto })
    }

    companion object {
        /** El mismo gesto, o «pulsar» frente a «corta», «larga» o «doble». */
        private fun excluye(a: TipoGesto, b: TipoGesto) = a == b || (a == TipoGesto.Pulsar) != (b == TipoGesto.Pulsar)

        /**
         * △ larga alterna SIM ⇄ ERG en todas las transmisiones; «+10 W» y «−10 W» quedan sin botón, porque en ERG las
         * palancas ya ajustan la potencia (design.md del change `erg-manual`, decisión 5).
         */
        val ALTERNAR_ERG = Regla(Entrada(SquareButton.Triangulo), TipoGesto.Larga, Accion.AlternarErgManual)

        /**
         * Campag. I + cruceta ← / → cambian de pantalla en todas las transmisiones (anexo A.2 de `funcional.md`; design.md
         * del change `rodaje-completo-y-ajustes`, decisión 4). «Pulsar», sin espera.
         */
        val PANTALLA_ANTERIOR = Regla(Entrada(SquareButton.CrucetaIzquierda, SquareButton.CampagnoloIzquierdo), TipoGesto.Pulsar, Accion.PantallaAnterior)
        val PANTALLA_SIGUIENTE = Regla(Entrada(SquareButton.CrucetaDerecha, SquareButton.CampagnoloIzquierdo), TipoGesto.Pulsar, Accion.PantallaSiguiente)

        /**
         * Las funciones de Zwift en los botones de un mando oficial, con «pulsar», en todas las transmisiones (anexo A.1 de
         * `funcional.md`; design.md del change `mando-zwift-emulado`, decisión 7). No chocan con las de pantalla, que
         * llevan Campag. I.
         */
        val ZWIFT_POR_DEFECTO: List<Regla> = FuncionZwift.entries.map { Regla(Entrada(it.boton), TipoGesto.Pulsar, Accion.Mando(it)) }

        /** Anexo A de `funcional.md`, con las acciones que ya existen (spec `button-mapping`, «Asignación por defecto»). */
        val POR_DEFECTO = Asignacion(
            listOf(
                Regla(Entrada(SquareButton.PalancaDerecha1), TipoGesto.Pulsar, Accion.MarchaMas(1)),
                Regla(Entrada(SquareButton.PalancaDerecha2), TipoGesto.Pulsar, Accion.MarchaMas(1)),
                Regla(Entrada(SquareButton.PalancaIzquierda1), TipoGesto.Pulsar, Accion.MarchaMenos(1)),
                Regla(Entrada(SquareButton.PalancaIzquierda2), TipoGesto.Pulsar, Accion.MarchaMenos(1)),
                Regla(Entrada(SquareButton.PalancaDerecha1, SquareButton.CampagnoloDerecho), TipoGesto.Pulsar, Accion.MarchaMas(Accion.SALTO)),
                Regla(Entrada(SquareButton.PalancaIzquierda1, SquareButton.CampagnoloIzquierdo), TipoGesto.Pulsar, Accion.MarchaMenos(Accion.SALTO)),
                Regla(Entrada(SquareButton.PalancaDerecha2, SquareButton.CampagnoloDerecho), TipoGesto.Pulsar, Accion.MarchaMasDura),
                Regla(Entrada(SquareButton.PalancaIzquierda2, SquareButton.CampagnoloIzquierdo), TipoGesto.Pulsar, Accion.MarchaMasLigera),
                Regla(Entrada(SquareButton.Cuadrado), TipoGesto.Corta, Accion.MarchaReferencia),
                Regla(Entrada(SquareButton.Cuadrado), TipoGesto.Larga, Accion.FijarReferencia),
                ALTERNAR_ERG,
                PANTALLA_ANTERIOR,
                PANTALLA_SIGUIENTE,
            ) + ZWIFT_POR_DEFECTO,
        )

        /**
         * Con platos, como los mandos Shimano: la palanca grande derecha (D1) al piñón más grande, la pequeña (D2) al más
         * pequeño, y la izquierda al plato (design.md del change `transmisiones-de-marca`, decisión 4), validado con el STI
         * del usuario.
         */
        val POR_DEFECTO_PLATOS = Asignacion(
            listOf(
                Regla(Entrada(SquareButton.PalancaDerecha1), TipoGesto.Pulsar, Accion.MarchaMenos(1)),
                Regla(Entrada(SquareButton.PalancaDerecha2), TipoGesto.Pulsar, Accion.MarchaMas(1)),
                Regla(Entrada(SquareButton.PalancaIzquierda1), TipoGesto.Pulsar, Accion.PlatoMas),
                Regla(Entrada(SquareButton.PalancaIzquierda2), TipoGesto.Pulsar, Accion.PlatoMenos),
                Regla(Entrada(SquareButton.PalancaDerecha1, SquareButton.CampagnoloDerecho), TipoGesto.Pulsar, Accion.MarchaMenos(Accion.SALTO)),
                Regla(Entrada(SquareButton.PalancaDerecha2, SquareButton.CampagnoloDerecho), TipoGesto.Pulsar, Accion.MarchaMas(Accion.SALTO)),
                Regla(Entrada(SquareButton.PalancaIzquierda1, SquareButton.CampagnoloIzquierdo), TipoGesto.Pulsar, Accion.MarchaMasDura),
                Regla(Entrada(SquareButton.PalancaIzquierda2, SquareButton.CampagnoloIzquierdo), TipoGesto.Pulsar, Accion.MarchaMasLigera),
                Regla(Entrada(SquareButton.Cuadrado), TipoGesto.Corta, Accion.MarchaReferencia),
                Regla(Entrada(SquareButton.Cuadrado), TipoGesto.Larga, Accion.FijarReferencia),
                ALTERNAR_ERG,
                PANTALLA_ANTERIOR,
                PANTALLA_SIGUIENTE,
            ) + ZWIFT_POR_DEFECTO,
        )

        /** La de fábrica de cada transmisión. */
        fun porDefecto(transmision: Transmision): Asignacion = when (transmision) {
            is Transmision.Lineal -> POR_DEFECTO
            is Transmision.Platos -> POR_DEFECTO_PLATOS
        }
    }
}

/**
 * Lista de reglas que, al leerla, descarta las que no se entienden (una acción de una versión futura) sin invalidar el
 * resto (decisión 4).
 */
object ReglasSerializer : KSerializer<List<Regla>> {
    private val lista = ListSerializer(Regla.serializer())
    override val descriptor: SerialDescriptor = lista.descriptor

    override fun serialize(encoder: Encoder, value: List<Regla>) = lista.serialize(encoder, value)

    override fun deserialize(decoder: Decoder): List<Regla> {
        val json = decoder as? JsonDecoder ?: return lista.deserialize(decoder)
        val elementos = json.decodeJsonElement() as? JsonArray ?: return emptyList()
        return elementos.mapNotNull { runCatching { json.json.decodeFromJsonElement(Regla.serializer(), it) }.getOrNull() }
    }
}
