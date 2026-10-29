package app.smartbike.ui.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.smartbike.ui.navegacion.Pestana
import app.smartbike.ui.tema.SqTema

/**
 * Navegación entre las cuatro pestañas (`sq-nav`): icono y texto siempre, la actual en `gear` y anunciada como
 * elegida, así no depende solo del color. Solo en Dispositivos, Botones y Ajustes.
 */
@Composable
fun BottomNav(actual: Pestana, alElegir: (Pestana) -> Unit, modifier: Modifier = Modifier) {
    val c = SqTema.colores
    Column(modifier.fillMaxWidth().background(c.surface).navigationBarsPadding()) {
        HorizontalDivider(thickness = 1.dp, color = c.line)
        Row(Modifier.fillMaxWidth().selectableGroup()) {
            Pestana.entries.forEach { p ->
                val elegida = p == actual
                val color = if (elegida) c.gear else c.inkMuted
                Column(
                    Modifier
                        .weight(1f)
                        .heightIn(min = 56.dp)
                        .selectable(elegida, role = Role.Tab, onClick = { if (!elegida) alElegir(p) })
                        .padding(top = 10.dp, bottom = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(SqTema.espacios.space1, Alignment.CenterVertically),
                ) {
                    Icon(painterResource(p.icono), contentDescription = null, tint = color)
                    Text(
                        stringResource(p.nombre),
                        style = SqTema.tipografia.caption.copy(fontSize = 12.sp, fontWeight = FontWeight(600)),
                        color = color,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}
