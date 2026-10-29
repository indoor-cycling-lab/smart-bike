package app.smartbike.ui.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import app.smartbike.ui.tema.SqTema

/**
 * Modo del Neo (`sq-mode`): contorno y texto en `ink` con [libre] (sin app que envíe pendiente), en `sim` o, con [erg], en `erg`, sobre `bg`. [origen] es quién manda en
 * ERG («ZWIFT»), en `ink-muted` (`sq-mode small`). El modo se lee por la palabra, no solo por el color.
 *
 * Con [alPulsar] es la variante tocable (`button.sq-mode`): relleno tintado con el color del modo, borde de 3 dp y relleno más intenso al pulsar; sigue midiendo 32 dp de
 * alto, pero su objetivo táctil llega a `touch-min` y [descripcion] dice la acción para accesibilidad.
 */
@Composable
fun ModeBadge(
    texto: String,
    modifier: Modifier = Modifier,
    erg: Boolean = false,
    origen: String? = null,
    libre: Boolean = false,
    alPulsar: (() -> Unit)? = null,
    descripcion: String? = null,
) {
    val c = SqTema.colores
    val color = if (erg) {
        c.erg
    } else if (libre) {
        c.ink
    } else {
        c.sim
    }
    val forma = RoundedCornerShape(SqTema.radios.radiusSm)
    if (alPulsar == null) {
        Etiqueta(texto, origen, color, c.bg, 2.dp, modifier)
        return
    }
    val interaccion = remember { MutableInteractionSource() }
    val pulsado by interaccion.collectIsPressedAsState()
    Box(
        modifier
            .heightIn(min = SqTema.tamanos.touchMin)
            .clip(forma)
            .clickable(interactionSource = interaccion, indication = null, role = Role.Button, onClick = alPulsar)
            .semantics { descripcion?.let { contentDescription = it } },
        contentAlignment = Alignment.Center,
    ) {
        Etiqueta(texto, origen, color, color.copy(alpha = if (pulsado) 0.28f else 0.14f).compositeOver(c.bg), 3.dp)
    }
}

@Composable
private fun Etiqueta(texto: String, origen: String?, color: Color, fondo: Color, borde: Dp, modifier: Modifier = Modifier) {
    val forma = RoundedCornerShape(SqTema.radios.radiusSm)
    Row(
        modifier.height(32.dp).background(fondo, forma).border(borde, color, forma).padding(horizontal = SqTema.espacios.space3),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SqTema.espacios.space2),
    ) {
        Text(texto, color = color, style = TextStyle(fontFamily = SqTema.familias.numeric, fontSize = 18.sp, fontWeight = FontWeight(700), letterSpacing = 0.06.em))
        origen?.let {
            Text(it, color = SqTema.colores.inkMuted, style = TextStyle(fontFamily = SqTema.familias.sans, fontSize = 12.sp, fontWeight = FontWeight(600), letterSpacing = 0.04.em))
        }
    }
}
