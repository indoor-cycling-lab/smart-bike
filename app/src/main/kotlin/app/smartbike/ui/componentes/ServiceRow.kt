package app.smartbike.ui.componentes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import app.smartbike.R
import app.smartbike.ui.tema.SqTema

/**
 * Un servicio que la app publica en Conexión (`ServiceRow`): su icono, su nombre, por dónde va, su estado en palabra y
 * forma y una ayuda debajo. Con [alTocar], la fila entera es un enlace (a Dispositivos, cuando el servicio no se ofrece):
 * [noSeOfrece] lo atenúa y añade la flecha.
 */
@Composable
fun ServiceRow(
    enlace: Enlace,
    nombre: String,
    via: String,
    estado: EstadoChip,
    textoEstado: String,
    ayuda: String?,
    modifier: Modifier = Modifier,
    noSeOfrece: Boolean = false,
    alTocar: (() -> Unit)? = null,
) {
    val c = SqTema.colores
    val tocable = if (alTocar != null) Modifier.heightIn(min = SqTema.tamanos.touchMin).clickable(role = Role.Button, onClick = alTocar) else Modifier
    SettingsItemApilado(modifier.then(tocable)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(SqTema.espacios.space3), verticalAlignment = Alignment.CenterVertically) {
            IconoEnlace(enlace)
            Column(Modifier.weight(1f)) {
                Text(nombre, style = SqTema.tipografia.body.copy(fontWeight = FontWeight(600)), color = if (noSeOfrece) c.inkMuted else c.ink)
                Caption(via)
            }
            ConnectionChip(estado, textoEstado, atenuado = noSeOfrece)
            if (alTocar != null) Icon(painterResource(R.drawable.ic_chevron_right), contentDescription = null, tint = c.inkMuted)
        }
        if (ayuda != null) Text(ayuda, style = SqTema.tipografia.body)
    }
}
