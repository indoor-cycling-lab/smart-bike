package app.smartbike.ui.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.smartbike.ui.tema.SqTema

/**
 * Aviso de la salida de Rodaje (`sq-exit-notice`): translúcido, de varias líneas si hace falta y sin consumir toques.
 * Quien lo usa lo coloca flotando en el centro, sin mover nada; [descripcion] es lo que lee el lector de pantalla.
 */
@Composable
fun ExitNotice(texto: String, descripcion: String, modifier: Modifier = Modifier) {
    val c = SqTema.colores
    val forma = RoundedCornerShape(SqTema.radios.radiusMd)
    Text(
        texto,
        color = c.ink,
        style = SqTema.tipografia.body.copy(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight(500), textAlign = TextAlign.Center),
        modifier = modifier
            .background(c.surface.copy(alpha = 0.82f), forma)
            .border(1.dp, c.line, forma)
            .padding(horizontal = SqTema.espacios.space4, vertical = SqTema.espacios.space2)
            .semantics {
                liveRegion = LiveRegionMode.Polite
                contentDescription = descripcion
            },
    )
}
