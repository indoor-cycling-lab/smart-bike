package app.smartbike.ui.componentes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.smartbike.ui.tema.SqTema

/**
 * Botón de acción (`sq-btn`). Principal: uno por pantalla, para la acción que avanza; el resto, secundarios.
 * [descripcion] es lo que anuncia TalkBack cuando [texto] va abreviado.
 */
@Composable
fun Button(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    principal: Boolean = false,
    habilitado: Boolean = true,
    descripcion: String? = null,
    relleno: Dp = SqTema.espacios.space6,
) {
    val c = SqTema.colores
    val forma = RoundedCornerShape(SqTema.radios.radiusMd)
    Box(
        modifier
            .heightIn(min = SqTema.tamanos.touchMin)
            .alpha(if (habilitado) 1f else DESHABILITADO)
            .clip(forma)
            .background(if (principal) c.gearFill else c.surface)
            .border(BorderStroke(1.dp, if (principal) c.gearFill else c.lineStrong), forma)
            .clickable(enabled = habilitado, role = Role.Button, onClick = onClick)
            .padding(horizontal = relleno, vertical = SqTema.espacios.space2),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            texto,
            modifier = if (descripcion != null) Modifier.semantics { contentDescription = descripcion } else Modifier,
            style = SqTema.tipografia.body.copy(fontWeight = FontWeight(600), textAlign = TextAlign.Center),
            color = if (principal) c.onGearFill else c.ink,
        )
    }
}

/**
 * Botón de rodaje (`sq-btn ride`): `touch-ride` (72 dp) de alto y de ancho mínimo, con el glifo en la letra de las
 * cifras. Solo en Rodaje, como respaldo táctil de las marchas (F11.8).
 */
@Composable
fun ButtonRide(glifo: String, descripcion: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val c = SqTema.colores
    val forma = RoundedCornerShape(SqTema.radios.radiusMd)
    Box(
        modifier
            .heightIn(min = SqTema.tamanos.touchRide)
            .widthIn(min = SqTema.tamanos.touchRide)
            .clip(forma)
            .background(c.surface)
            .border(BorderStroke(1.dp, c.lineStrong), forma)
            .semantics { contentDescription = descripcion }
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = SqTema.espacios.space4),
        contentAlignment = Alignment.Center,
    ) {
        Text(glifo, style = SqTema.tipografia.metricXl.copy(fontSize = 32.sp, lineHeight = 32.sp), color = c.ink, modifier = Modifier.clearAndSetSemantics {})
    }
}

/** Opacidad de los controles deshabilitados (`:disabled { opacity: .45 }`). */
internal const val DESHABILITADO = 0.45f
