package app.smartbike.ui.componentes

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.smartbike.ui.tema.SqTema

/**
 * Marcha lineal en grande (`sq-gear`): el número en `gear-hero` y «Marcha · de N» al lado. En un tope, la cifra pasa a
 * `ink-muted` y aparece [tope] debajo (`sq-limit`), en un hueco que está reservado también sin tope: [reservaTope] es el
 * texto que mide ese hueco. Al cambiar, la cifra nueva entra 12 dp en la dirección del cambio
 * (arriba = más dura) en 120 ms, salvo con [animar] a `false` (movimiento reducido).
 */
@Composable
fun GearDisplay(
    marcha: Int,
    total: Int,
    etiqueta: String,
    deTotal: String,
    descripcion: String,
    modifier: Modifier = Modifier,
    tope: String? = null,
    reservaTope: String = "",
    animar: Boolean = true,
) {
    val c = SqTema.colores
    val desplazamiento = with(LocalDensity.current) { 12.dp.roundToPx() }
    // El tope va centrado bajo la cifra, como `.sq-ride__gear > .sq-col` (design.md del change `erg-manual`, decisión 6).
    Column(modifier, verticalArrangement = Arrangement.spacedBy(SqTema.espacios.space2), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(Modifier.semantics(mergeDescendants = true) { contentDescription = descripcion }, verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(SqTema.espacios.space6)) {
            AnimatedContent(
                targetState = marcha,
                transitionSpec = {
                    if (!animar) {
                        ContentTransform(fadeIn(tween(0)), fadeOut(tween(0)))
                    } else {
                        val sube = targetState > initialState
                        (slideInVertically(tween(DURACION_MS)) { if (sube) desplazamiento else -desplazamiento } + fadeIn(tween(DURACION_MS)))
                            .togetherWith(fadeOut(tween(DURACION_MS)))
                    }
                },
                label = "marcha",
            ) { m ->
                Text(m.toString(), color = if (tope != null) c.inkMuted else c.gear, style = SqTema.tipografia.gearHero, softWrap = false, modifier = Modifier.clearAndSetSemantics {})
            }
            Column(Modifier.padding(bottom = SqTema.espacios.space3).clearAndSetSemantics {}, verticalArrangement = Arrangement.spacedBy(SqTema.espacios.space2)) {
                Label(etiqueta)
                Text(deTotal, color = c.ink, style = SqTema.tipografia.metricMd.copy(fontSize = 28.sp, lineHeight = 32.sp, fontWeight = FontWeight(600)))
            }
        }
        // El hueco de la etiqueta está siempre reservado (`sq-limit is-reserva`), para que al aparecer no se mueva nada.
        Text(
            tope ?: reservaTope,
            color = c.gear,
            style = SqTema.tipografia.caption.copy(fontWeight = FontWeight(600)),
            modifier = Modifier
                .then(if (tope == null) Modifier.alpha(0f).clearAndSetSemantics {} else Modifier)
                .background(c.gearSoft, RoundedCornerShape(SqTema.radios.radiusSm))
                .padding(horizontal = SqTema.espacios.space2, vertical = 2.dp),
        )
    }
}

/**
 * Marcha física en grande (`sq-ratio-hero`): plato × piñón en `gear-ratio-hero`, con el × en `metric-xl` y
 * `ink-muted`. Como la lineal, en un tope se atenúa y muestra [tope] en un hueco reservado, y al cambiar entra 12 dp en
 * la dirección del cambio según la relación (arriba = más dura), salvo con [animar] a `false`.
 */
@Composable
fun GearDisplay(
    plato: Int,
    pinon: Int,
    descripcion: String,
    modifier: Modifier = Modifier,
    tope: String? = null,
    reservaTope: String = "",
    animar: Boolean = true,
) {
    val c = SqTema.colores
    val desplazamiento = with(LocalDensity.current) { 12.dp.roundToPx() }
    // El tope va centrado bajo la cifra, como `.sq-ride__gear > .sq-col` (design.md del change `erg-manual`, decisión 6).
    Column(modifier, verticalArrangement = Arrangement.spacedBy(SqTema.espacios.space2), horizontalAlignment = Alignment.CenterHorizontally) {
        AnimatedContent(
            targetState = plato to pinon,
            transitionSpec = {
                if (!animar) {
                    ContentTransform(fadeIn(tween(0)), fadeOut(tween(0)))
                } else {
                    val sube = targetState.first.toDouble() / targetState.second > initialState.first.toDouble() / initialState.second
                    (slideInVertically(tween(DURACION_MS)) { if (sube) desplazamiento else -desplazamiento } + fadeIn(tween(DURACION_MS)))
                        .togetherWith(fadeOut(tween(DURACION_MS)))
                }
            },
            modifier = Modifier.semantics(mergeDescendants = true) { contentDescription = descripcion },
            label = "plato-pinon",
        ) { (p, q) ->
            Row(Modifier.clearAndSetSemantics {}, verticalAlignment = Alignment.Bottom) {
                val color = if (tope != null) c.inkMuted else c.gear
                Text(p.toString(), color = color, style = SqTema.tipografia.gearRatioHero, softWrap = false)
                Text(
                    "×",
                    color = c.inkMuted,
                    style = SqTema.tipografia.metricXl,
                    softWrap = false,
                    modifier = Modifier.padding(horizontal = 2.dp, vertical = SqTema.espacios.space2),
                )
                Text(q.toString(), color = color, style = SqTema.tipografia.gearRatioHero, softWrap = false)
            }
        }
        Text(
            tope ?: reservaTope,
            color = c.gear,
            style = SqTema.tipografia.caption.copy(fontWeight = FontWeight(600)),
            modifier = Modifier
                .then(if (tope == null) Modifier.alpha(0f).clearAndSetSemantics {} else Modifier)
                .background(c.gearSoft, RoundedCornerShape(SqTema.radios.radiusSm))
                .padding(horizontal = SqTema.espacios.space2, vertical = 2.dp),
        )
    }
}

/**
 * Escalera de marchas lineal (`sq-ladder`): un segmento por marcha, la ligera a la izquierda. Las superadas en
 * `line-strong`, la actual en `gear-fill` a toda altura, el resto en `surface-2`, y la referencia con una marca
 * inferior en `ink`. No es interactiva.
 */
@Composable
fun GearLadder(total: Int, actual: Int, referencia: Int?, ligera: String, dura: String, modifier: Modifier = Modifier) {
    val c = SqTema.colores
    Column(modifier.clearAndSetSemantics {}) {
        Row(Modifier.fillMaxWidth().height(SqTema.tamanos.ladderH), horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.Bottom) {
            for (i in 1..total) {
                val forma = RoundedCornerShape(SqTema.radios.radiusSm)
                val color = when {
                    i == actual -> c.gearFill
                    i < actual -> c.lineStrong
                    else -> c.surface2
                }
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight(if (i == actual) 1f else 0.4f)
                        .background(color, forma)
                        .then(
                            if (i == referencia) {
                                Modifier.drawBehind {
                                    val alto = 3.dp.toPx()
                                    drawRect(c.ink, topLeft = Offset(0f, size.height - alto), size = size.copy(height = alto))
                                }
                            } else {
                                Modifier
                            },
                        ),
                )
            }
        }
        Row(Modifier.fillMaxWidth().padding(top = SqTema.espacios.space1), horizontalArrangement = Arrangement.SpaceBetween) {
            Caption(ligera)
            Caption(dura)
        }
    }
}

private const val DURACION_MS = 120
