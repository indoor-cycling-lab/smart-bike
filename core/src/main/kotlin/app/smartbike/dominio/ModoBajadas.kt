package app.smartbike.dominio

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Cuánto puede bajar la pendiente que se envía al Neo en modo SIM (F6.5): con pendiente negativa, su motor empuja el
 * volante (design.md del change `proteccion-bajadas`, decisión 1). Los nombres serializados son estables.
 */
@Serializable
enum class ModoBajadas {
    /** Terreno + marcha, sin límite. */
    @SerialName("sin-proteccion")
    SIN_PROTECCION,

    /** La marcha nunca deja la pendiente por debajo de la del terreno, ni por debajo de 0 % en llano o subida. */
    @SerialName("la-marcha-no-crea-bajadas")
    LA_MARCHA_NO_CREA_BAJADAS,

    /** La pendiente no baja nunca de 0 %. */
    @SerialName("nunca-empujar")
    NUNCA_EMPUJAR,

    ;

    /** Pendiente mínima que se envía al Neo con el terreno [pendienteTerrenoPct], o `null` si no hay límite. */
    fun suelo(pendienteTerrenoPct: Double): Double? = when (this) {
        SIN_PROTECCION -> null
        LA_MARCHA_NO_CREA_BAJADAS -> minOf(pendienteTerrenoPct, 0.0)
        NUNCA_EMPUJAR -> 0.0
    }

    /** [pendientePct] con el suelo de este modo aplicado. */
    fun limitar(pendientePct: Double, pendienteTerrenoPct: Double): Double = suelo(pendienteTerrenoPct)?.let { maxOf(pendientePct, it) } ?: pendientePct
}
