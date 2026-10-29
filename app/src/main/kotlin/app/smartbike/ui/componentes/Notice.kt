package app.smartbike.ui.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.sp
import app.smartbike.R
import app.smartbike.ui.tema.SqTema

/**
 * Aviso en línea (`sq-notice`), siempre con icono y texto. El de error se anuncia como alerta y dice qué pasa y qué
 * hacer.
 */
@Composable
fun Notice(texto: String, modifier: Modifier = Modifier, error: Boolean = false) {
    val c = SqTema.colores
    val color = if (error) c.danger else c.gear
    // is-danger: color-mix(in srgb, danger 14 %, bg).
    val fondo = if (error) c.danger.copy(alpha = 0.14f).compositeOver(c.bg) else c.gearSoft
    Row(
        modifier
            .fillMaxWidth()
            .background(fondo, RoundedCornerShape(SqTema.radios.radiusMd))
            .padding(horizontal = SqTema.espacios.space4, vertical = SqTema.espacios.space3)
            .then(if (error) Modifier.semantics { liveRegion = LiveRegionMode.Assertive } else Modifier),
        horizontalArrangement = Arrangement.spacedBy(SqTema.espacios.space3),
    ) {
        Icon(painterResource(if (error) R.drawable.ic_error else R.drawable.ic_info), contentDescription = null, tint = color)
        Text(texto, color = color, style = SqTema.tipografia.body.copy(fontSize = 14.sp, lineHeight = 20.sp))
    }
}
