package app.smartbike.dominio

/**
 * Lo que trae un `TrainerSimulationParam` del modo de marchas virtuales de Zwift (arquitectura.md § 7.4; design.md del
 * change `mando-zwift-dircon`, decisión 1). Cada `SET` trae solo una parte, así que todo es nulable. Las escalas son
 * las del protobuf público: pendiente en centésimas de %, relación virtual ×10 000, CW ×10 000, masas ×100, Crr ×100 000.
 * El viento y lo que el modelo no usa quedan en bruto.
 */
data class SimulacionZwift(
    val resistencia: Int? = null,
    val potenciaErgW: Int? = null,
    val ventanaPromedio: Int? = null,
    /** Viento en bruto (`sint32`): la escala está a validar. */
    val vientoBruto: Int? = null,
    val pendientePct: Double? = null,
    val relacionReal: Int? = null,
    val relacionVirtual: Double? = null,
    val cw: Double? = null,
    val diametroRueda: Int? = null,
    val masaBiciKg: Double? = null,
    val masaCiclistaKg: Double? = null,
    val crr: Double? = null,
    val areaFrontal: Int? = null,
    val frenoElectronico: Int? = null,
) {
    /** Esta simulación con los campos de [otra] por encima: lo último que mandó Zwift de cada cosa. */
    fun combinar(otra: SimulacionZwift) = SimulacionZwift(
        resistencia = otra.resistencia ?: resistencia,
        potenciaErgW = otra.potenciaErgW ?: potenciaErgW,
        ventanaPromedio = otra.ventanaPromedio ?: ventanaPromedio,
        vientoBruto = otra.vientoBruto ?: vientoBruto,
        pendientePct = otra.pendientePct ?: pendientePct,
        relacionReal = otra.relacionReal ?: relacionReal,
        relacionVirtual = otra.relacionVirtual ?: relacionVirtual,
        cw = otra.cw ?: cw,
        diametroRueda = otra.diametroRueda ?: diametroRueda,
        masaBiciKg = otra.masaBiciKg ?: masaBiciKg,
        masaCiclistaKg = otra.masaCiclistaKg ?: masaCiclistaKg,
        crr = otra.crr ?: crr,
        areaFrontal = otra.areaFrontal ?: areaFrontal,
        frenoElectronico = otra.frenoElectronico ?: frenoElectronico,
    )
}

/**
 * Junta los `SET` parciales de Zwift en un [Terreno] (design.md, decisión 2). El viento no se usa hasta validar su
 * escala. La relación de la marcha de Zwift, las masas y el resto no entran en el terreno: se guardan para Diagnóstico.
 */
class AcumuladorSimulacion {
    /** Lo último que mandó Zwift de cada campo. */
    var ultima = SimulacionZwift()
        private set

    /** Potencia de ERG pedida por el último `SET` que la traía. */
    val potenciaErgW: Int? get() = ultima.potenciaErgW

    /**
     * Aplica [s] y devuelve el terreno resultante si [s] trae pendiente, Crr o CW y ya hay pendiente; si no
     * (por ejemplo, una relación sola), `null`: no cambia lo que simula el Neo.
     */
    fun aplicar(s: SimulacionZwift): Terreno? {
        ultima = ultima.combinar(s)
        val cambiaTerreno = s.pendientePct != null || s.crr != null || s.cw != null
        val pendiente = ultima.pendientePct ?: return null
        if (!cambiaTerreno) return null
        return Terreno(pendiente, ultima.crr, ultima.cw, vientoMs = null)
    }

    fun reiniciar() {
        ultima = SimulacionZwift()
    }
}
