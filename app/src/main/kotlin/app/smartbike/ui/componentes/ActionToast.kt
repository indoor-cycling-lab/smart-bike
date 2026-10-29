package app.smartbike.ui.componentes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.smartbike.ui.tema.SqTema

/**
 * Aviso breve de la acción (`sq-toast`): solo la acción, sin botón ni gesto. Quien lo usa decide cuánto se ve
 * (1,5 s); con [texto] `null` se desvanece.
 */
@Composable
fun ActionToast(texto: String?, modifier: Modifier = Modifier) {
    val c = SqTema.colores
    val forma = RoundedCornerShape(SqTema.radios.radiusMd)
    AnimatedVisibility(texto != null, modifier, enter = fadeIn(tween(0)), exit = fadeOut(tween(200))) {
        Text(
            texto.orEmpty(),
            color = c.ink,
            style = SqTema.tipografia.body.copy(fontWeight = FontWeight(600)),
            softWrap = false,
            modifier = Modifier
                .background(c.surface, forma)
                .border(1.dp, c.line, forma)
                .padding(horizontal = SqTema.espacios.space4, vertical = SqTema.espacios.space2)
                .semantics { liveRegion = LiveRegionMode.Polite },
        )
    }
}
