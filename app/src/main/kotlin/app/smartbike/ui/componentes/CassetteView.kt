package app.smartbike.ui.componentes

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
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.smartbike.ui.tema.SqFamilias
import app.smartbike.ui.tema.SqTema

/**
 * El cassette de perfil (`sq-cass` y `sq-cog-n`): una barra por piñón, el mayor (el más ligero) a la izquierda, con
 * altura 28 % + 72 % × dientes / dientes del mayor, y el [activo] en `gear-fill` con su número en `gear`. Para el lector
 * de pantalla es una sola imagen con [descripcion] («Cassette 11 a 36, piñón 17»). No es interactivo.
 */
@Composable
fun CassetteView(pinones: List<Int>, activo: Int, descripcion: String, modifier: Modifier = Modifier, alto: Dp = 48.dp) {
    val c = SqTema.colores
    val deMayorAMenor = pinones.sortedDescending()
    val mayor = deMayorAMenor.first().toFloat()
    val numeros = TextStyle(fontFamily = SqFamilias.numeric, fontWeight = FontWeight(600), fontSize = 13.sp, lineHeight = 16.sp, textAlign = TextAlign.Center)
    Column(modifier.fillMaxWidth().clearAndSetSemantics { contentDescription = descripcion }) {
        Row(Modifier.fillMaxWidth().height(alto), horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.Bottom) {
            deMayorAMenor.forEach { d ->
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight(0.28f + 0.72f * d / mayor)
                        .background(if (d == activo) c.gearFill else c.surface2, RoundedCornerShape(SqTema.radios.radiusSm)),
                )
            }
        }
        Row(Modifier.fillMaxWidth().padding(top = SqTema.espacios.space1), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            deMayorAMenor.forEach { d ->
                Text(d.toString(), color = if (d == activo) c.gear else c.inkMuted, style = numeros, modifier = Modifier.weight(1f))
            }
        }
    }
}
