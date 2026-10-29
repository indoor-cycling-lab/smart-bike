package app.smartbike.ui.componentes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import app.smartbike.R
import app.smartbike.ui.tema.SqTema

/** Barra superior de una pantalla: título y, en las secundarias, botón de volver (`sq-appbar`). */
@Composable
fun AppBar(titulo: String, modifier: Modifier = Modifier, volver: Pair<String, () -> Unit>? = null) {
    Row(
        modifier.heightIn(min = SqTema.tamanos.touchMin),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SqTema.espacios.space2),
    ) {
        if (volver != null) {
            val (descripcion, accion) = volver
            Box(
                Modifier
                    .offset(x = -SqTema.espacios.space3)
                    .size(SqTema.tamanos.touchMin)
                    .clip(CircleShape)
                    .clickable(onClickLabel = descripcion, onClick = accion),
                contentAlignment = Alignment.Center,
            ) {
                Icon(painterResource(R.drawable.ic_arrow_back), contentDescription = descripcion, tint = SqTema.colores.ink)
            }
        }
        Text(titulo, style = SqTema.tipografia.title, modifier = Modifier.semantics { heading() })
    }
}
