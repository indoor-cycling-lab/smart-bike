package app.smartbike.ui.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.smartbike.R
import app.smartbike.ui.tema.SqTema

/**
 * Zona de captura (`sq-capture`, design-system/components/CapturePanel): borde discontinuo `gear` mientras escucha, con
 * [texto] arriba, las [teclas] detectadas pulsadas a 48 dp (o [esperando] si no hay ninguna) y lo [detectado] debajo.
 * Sin [escuchando] (Square sin conexión, `is-off`), borde continuo `line` y solo el texto, en `ink-muted`.
 */
@Composable
fun CapturePanel(
    texto: String,
    modifier: Modifier = Modifier,
    escuchando: Boolean = true,
    teclas: List<String> = emptyList(),
    esperando: String? = null,
    detectado: String? = null,
) {
    val c = SqTema.colores
    val forma = RoundedCornerShape(SqTema.radios.radiusMd)
    val radio = SqTema.radios.radiusMd
    Column(
        modifier
            .fillMaxWidth()
            .background(c.surface, forma)
            .then(
                if (escuchando) {
                    Modifier.drawBehind {
                        val ancho = 2.dp.toPx()
                        drawRoundRect(
                            color = c.gear,
                            style = Stroke(width = ancho, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 4.dp.toPx()))),
                            cornerRadius = CornerRadius(radio.toPx()),
                            topLeft = Offset(ancho / 2, ancho / 2),
                            size = Size(size.width - ancho, size.height - ancho),
                        )
                    }
                } else {
                    Modifier.border(2.dp, c.line, forma)
                },
            )
            .padding(horizontal = SqTema.espacios.space4, vertical = SqTema.espacios.space8)
            .semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SqTema.espacios.space4),
    ) {
        Text(texto, style = SqTema.tipografia.body.copy(textAlign = TextAlign.Center), color = if (escuchando) c.ink else c.inkMuted)
        if (!escuchando) return@Column
        Row(Modifier.heightIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(SqTema.espacios.space2)) {
            if (teclas.isEmpty()) {
                esperando?.let { Caption(it) }
            } else {
                teclas.forEachIndexed { i, t ->
                    if (i > 0) Text("+", color = c.inkMuted, style = SqTema.tipografia.body.copy(fontWeight = FontWeight(600)))
                    TeclaGrande(t)
                }
            }
        }
        detectado?.let { Caption(it) }
    }
}

/** `SquareKey` pulsada a 48 dp, como en `.sq-capture .sq-key`. */
@Composable
private fun TeclaGrande(texto: String) {
    val c = SqTema.colores
    val ancha = texto.length > 1
    Box(
        Modifier
            .height(48.dp)
            .widthIn(min = 48.dp)
            .background(c.gearFill, RoundedCornerShape(SqTema.radios.radiusMd))
            .padding(horizontal = SqTema.espacios.space3),
        contentAlignment = Alignment.Center,
    ) {
        val simbolo = SimboloTecla.de(texto)
        if (simbolo != null) {
            Icon(painterResource(simbolo.icono), contentDescription = stringResource(simbolo.descripcion), modifier = Modifier.size(28.dp), tint = c.onGearFill)
        } else {
            Text(
                texto,
                color = c.onGearFill,
                style = if (ancha) {
                    SqTema.tipografia.body.copy(fontSize = 15.sp, fontWeight = FontWeight(600), lineHeight = 15.sp)
                } else {
                    SqTema.tipografia.body.copy(fontFamily = SqTema.familias.numeric, fontSize = 24.sp, fontWeight = FontWeight(700), lineHeight = 24.sp)
                },
                softWrap = false,
            )
        }
    }
}

/**
 * Una entrada que ya tiene la acción (`sq-entry`), con sus teclas, el gesto y el botón de quitar (`close`,
 * `touch-min`) que se anuncia como [descripcionQuitar].
 */
@Composable
fun EntradaAccion(entrada: EntradaUi, descripcionQuitar: String, alQuitar: () -> Unit, modifier: Modifier = Modifier) {
    val c = SqTema.colores
    Row(
        modifier
            .fillMaxWidth()
            .heightIn(min = SqTema.tamanos.touchMin)
            .background(c.surface)
            .padding(start = SqTema.espacios.space4),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SqTema.espacios.space2),
    ) {
        EntradaMapa(entrada, Modifier.weight(1f).padding(vertical = SqTema.espacios.space2))
        Box(
            Modifier
                .size(SqTema.tamanos.touchMin)
                .clickable(role = Role.Button, onClickLabel = descripcionQuitar, onClick = alQuitar),
            contentAlignment = Alignment.Center,
        ) {
            Icon(painterResource(R.drawable.ic_close), contentDescription = descripcionQuitar, tint = c.inkMuted)
        }
    }
}
