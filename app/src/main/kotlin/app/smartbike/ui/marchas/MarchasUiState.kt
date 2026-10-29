package app.smartbike.ui.marchas

import app.smartbike.dominio.Configuracion
import app.smartbike.dominio.Desarrollos
import app.smartbike.dominio.EstadoMarcha
import app.smartbike.dominio.ModoCambio
import app.smartbike.dominio.Transmision

/**
 * Pantalla de Marchas (maquetas/marchas.html, estados `lineal`, `shimano` y `shimano-manual`): la transmisión elegida,
 * su modo y su referencia, y «Recordar la última marcha» (spec `settings-screen`; design.md del change
 * `transmisiones-de-marca`, decisión 8).
 */
data class MarchasUiState(
    val transmisiones: List<Transmision> = TRANSMISIONES,
    val transmision: Transmision = Transmision.Lineal(),
    val modo: ModoCambio = ModoCambio.AUTOMATICO,
    val referencia: EstadoMarcha = Transmision.Lineal().marcha(12),
    val recordarUltimaMarcha: Boolean = true,
) {
    val puedeAligerar: Boolean get() = referenciaSiguiente(transmision, referencia, dura = false) != null
    val puedeEndurecer: Boolean get() = referenciaSiguiente(transmision, referencia, dura = true) != null

    companion object {
        /** La lineal y los desarrollos de fábrica, en el orden de la pantalla. */
        val TRANSMISIONES: List<Transmision> = listOf(Transmision.Lineal()) + Desarrollos.TODOS
    }
}

fun estadoDeMarchas(c: Configuracion): MarchasUiState {
    val t = c.transmisionElegida
    return MarchasUiState(
        transmisiones = listOf(Transmision.Lineal(c.lineal.marchas)) + Desarrollos.TODOS,
        transmision = t,
        modo = c.modo,
        referencia = c.referencia,
        recordarUltimaMarcha = c.recordarUltimaMarcha,
    )
}

/**
 * La referencia siguiente en ese sentido, o `null` en el extremo: con la lineal, la marcha de al lado; con platos, la
 * combinación siguiente de las 24 ordenadas por relación, para poder elegir cualquiera, como con □ larga.
 */
fun referenciaSiguiente(t: Transmision, ref: EstadoMarcha, dura: Boolean): EstadoMarcha? = when (t) {
    is Transmision.Lineal -> (ref as? EstadoMarcha.Numerada)?.let { m ->
        (if (dura) m.posicion + 1 else m.posicion - 1).takeIf { it in 1..t.marchas }?.let { t.marcha(it) }
    }

    is Transmision.Platos -> {
        val i = t.ordenadas.indexOf(ref)
        if (i < 0) null else t.ordenadas.getOrNull(if (dura) i + 1 else i - 1)
    }
}
