package app.smartbike.ui.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.smartbike.ui.tema.SqFamilias
import app.smartbike.ui.tema.SqTema

/**
 * Los platos con el activo relleno (`sq-ride__rings` y `sq-rings`): [etiqueta] («Plato») y un óvalo por plato, de
 * menor a mayor. El activo en `gear-fill` con texto `on-gear-fill`; los demás solo con borde `line-strong`, para que se
 * distinga también por el relleno. En horizontal, la etiqueta va encima y alineada a la derecha; en [vertical], al
 * lado. Con un solo plato no se muestra. No es interactivo.
 */
@Composable
fun ChainringIndicator(
    platos: List<Int>,
    activo: Int,
    etiqueta: String,
    descripcion: String,
    modifier: Modifier = Modifier,
    vertical: Boolean = false,
) {
    if (platos.size < 2) return
    val anillos = @Composable {
        Row(
            Modifier.semantics(mergeDescendants = true) { contentDescription = descripcion },
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            platos.forEach { Anillo(it, it == activo) }
        }
    }
    if (vertical) {
        Row(modifier, horizontalArrangement = Arrangement.spacedBy(SqTema.espacios.space2), verticalAlignment = Alignment.CenterVertically) {
            Label(etiqueta, Modifier.clearAndSetSemantics {})
            anillos()
        }
    } else {
        Column(modifier.padding(bottom = SqTema.espacios.space2), horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(SqTema.espacios.space2)) {
            Label(etiqueta, Modifier.clearAndSetSemantics {})
            anillos()
        }
    }
}

@Composable
private fun Anillo(dientes: Int, activo: Boolean) {
    val c = SqTema.colores
    val forma = RoundedCornerShape(SqTema.radios.radiusFull)
    Box(
        Modifier
            .widthIn(min = 44.dp)
            .height(28.dp)
            .then(if (activo) Modifier.background(c.gearFill, forma) else Modifier)
            .border(2.dp, if (activo) c.gearFill else c.lineStrong, forma)
            .padding(horizontal = SqTema.espacios.space2)
            .clearAndSetSemantics {},
        contentAlignment = Alignment.Center,
    ) {
        Text(
            dientes.toString(),
            color = if (activo) c.onGearFill else c.inkMuted,
            style = TextStyle(fontFamily = SqFamilias.numeric, fontWeight = FontWeight(700), fontSize = 16.sp, lineHeight = 16.sp),
        )
    }
}
