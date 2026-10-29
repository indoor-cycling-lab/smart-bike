package app.smartbike.ui.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
 * Una sesión grabada (`sq-ses`; design-system/components/SessionRow): cuándo empezó, su tamaño o estado y las acciones
 * de abrir, compartir y borrar, cada una de `touch-min`. Sin [borrar] (la sesión que se graba) se conserva el hueco, para
 * que abrir y compartir queden alineados con las demás filas.
 */
@Composable
fun SessionRow(
    nombre: String,
    ayuda: String,
    abrir: () -> Unit,
    compartir: () -> Unit,
    descripcionAbrir: String,
    descripcionCompartir: String,
    modifier: Modifier = Modifier,
    borrar: (() -> Unit)? = null,
    descripcionBorrar: String = "",
) {
    Row(
        modifier
            .fillMaxWidth()
            .heightIn(min = SqTema.tamanos.touchMin)
            .background(SqTema.colores.surface)
            .padding(start = SqTema.espacios.space4, top = SqTema.espacios.space2, bottom = SqTema.espacios.space2),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SqTema.espacios.space1),
    ) {
        Column(Modifier.weight(1f)) {
            Text(nombre, style = SqTema.tipografia.body.copy(fontWeight = FontWeight(600)))
            Caption(ayuda)
        }
        Row(Modifier.width(SqTema.tamanos.touchMin * 3)) {
            AccionSesion(R.drawable.ic_open_in_new, descripcionAbrir, abrir)
            AccionSesion(R.drawable.ic_share, descripcionCompartir, compartir)
            if (borrar != null) AccionSesion(R.drawable.ic_delete, descripcionBorrar, borrar)
        }
    }
}

@Composable
private fun AccionSesion(icono: Int, descripcion: String, alTocar: () -> Unit) {
    Box(
        Modifier.size(SqTema.tamanos.touchMin).clickable(role = Role.Button, onClickLabel = descripcion, onClick = alTocar),
        contentAlignment = Alignment.Center,
    ) {
        Icon(painterResource(icono), contentDescription = descripcion, tint = SqTema.colores.inkMuted)
    }
}
