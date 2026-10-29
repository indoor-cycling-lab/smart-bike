package app.smartbike.ui.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.smartbike.ui.tema.SqTema

/**
 * Fila de la tabla de marchas (`sq-step--gear`): una marcha con su ajuste y un paso a paso. La [puesta] lleva una barra a
 * la izquierda y la palabra «Puesta» en [ayuda], no solo color.
 */
@Composable
fun GearStepper(
    nombre: String,
    ayuda: String,
    valor: String,
    alRestar: () -> Unit,
    alSumar: () -> Unit,
    descripcionRestar: String,
    descripcionSumar: String,
    modifier: Modifier = Modifier,
    puesta: Boolean = false,
    puedeRestar: Boolean = true,
    puedeSumar: Boolean = true,
) {
    val c = SqTema.colores
    Row(
        modifier
            .fillMaxWidth()
            .background(c.surface)
            .drawBehind { if (puesta) drawRect(c.gear, Offset.Zero, Size(4.dp.toPx(), size.height)) }
            .semantics { selected = puesta }
            .padding(horizontal = SqTema.espacios.space4, vertical = SqTema.espacios.space2),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SqTema.espacios.space4),
    ) {
        Column(Modifier.weight(1f)) {
            Text(nombre, style = SqTema.tipografia.metricMd.copy(fontSize = 22.sp, lineHeight = 28.sp))
            Caption(ayuda)
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(SqTema.espacios.space2)) {
            BotonPaso("−", descripcionRestar, puedeRestar, alRestar)
            Text(
                buildAnnotatedString {
                    append(valor)
                    withStyle(SpanStyle(fontSize = 14.sp, fontFamily = SqTema.familias.sans, color = c.inkMuted)) { append(" %") }
                },
                style = SqTema.tipografia.metricMd.copy(fontSize = 22.sp, lineHeight = 28.sp, textAlign = TextAlign.Center),
                modifier = Modifier.widthIn(min = 76.dp),
            )
            BotonPaso("+", descripcionSumar, puedeSumar, alSumar)
        }
    }
}
