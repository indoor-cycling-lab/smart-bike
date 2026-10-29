package app.smartbike.ui.componentes

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.smartbike.R
import app.smartbike.ui.tema.SqTema

/** Lo más ancho que puede ser la palabra de estado de un paso. */
private val ESTADO_ANCHO_MAXIMO = 88.dp

/** El estado de un paso de la guía: hecho, conectando, pendiente, o [Informativo] si el paso no tiene estado («Ajusta la app»). */
enum class EstadoPaso { Hecho, Conectando, Pendiente, Informativo }

/**
 * Fila de un paso de la guía de inicio (`sq-gstep`): punto de estado (con la forma de `ConnectionChip`, nunca en el color de
 * error), [nombre] con la marca «Opcional» si es [opcional], [ayuda] y la palabra de [estadoTexto]; toda la fila es un
 * enlace de `touch-min` con un chevron. Con [ancha] (horizontal) el estado va bajo el texto. [descripcion] es la lectura
 * completa del lector de pantalla («Empareja tus dispositivos, hecho»).
 */
@Composable
fun GuideStep(
    nombre: String,
    estado: EstadoPaso,
    descripcion: String,
    alTocar: () -> Unit,
    modifier: Modifier = Modifier,
    ayuda: String? = null,
    estadoTexto: String? = null,
    opcionalTexto: String? = null,
    ancha: Boolean = false,
) {
    val c = SqTema.colores
    val chip = when (estado) {
        EstadoPaso.Hecho -> EstadoChip.Ok
        EstadoPaso.Conectando -> EstadoChip.Esperando
        EstadoPaso.Pendiente -> EstadoChip.Neutro
        EstadoPaso.Informativo -> null
    }
    val textos: @Composable (Modifier) -> Unit = { m ->
        Column(m) {
            // El nombre y «Opcional» van seguidos y, si no caben, la marca pasa a la línea de abajo (como el texto en línea de la maqueta).
            FlowRow(horizontalArrangement = Arrangement.spacedBy(SqTema.espacios.space1), verticalArrangement = Arrangement.Center) {
                Text(nombre, color = c.ink, style = SqTema.tipografia.body.copy(fontSize = 15.sp, lineHeight = 20.sp, fontWeight = FontWeight(600)), modifier = Modifier.align(Alignment.CenterVertically))
                opcionalTexto?.let { Etiqueta(it, Modifier.align(Alignment.CenterVertically)) }
            }
            ayuda?.let { Text(it, color = c.inkMuted, style = SqTema.tipografia.body.copy(fontSize = 13.sp, lineHeight = 18.sp)) }
            if (ancha) estadoTexto?.let { Text(it, color = if (estado == EstadoPaso.Hecho) c.ink else c.inkMuted, style = SqTema.tipografia.body.copy(fontSize = 13.sp, lineHeight = 18.sp), modifier = Modifier.padding(top = SqTema.espacios.space2)) }
        }
    }
    Row(
        modifier
            .fillMaxWidth()
            .heightIn(min = SqTema.tamanos.touchMin)
            .semantics(mergeDescendants = true) { contentDescription = descripcion }
            .clickable(role = Role.Button, onClick = alTocar)
            .padding(start = SqTema.espacios.space4, end = SqTema.espacios.space2, top = SqTema.espacios.space2, bottom = SqTema.espacios.space2),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SqTema.espacios.space3),
    ) {
        // El punto ocupa su hueco aunque el paso no tenga estado, para alinear el texto con los demás.
        if (chip != null) Punto(chip) else Spacer(Modifier.size(10.dp))
        textos(Modifier.weight(1f))
        // La palabra de estado tiene un ancho máximo y pasa a dos líneas antes de apretar el nombre («Falta el controlador»).
        if (!ancha) estadoTexto?.let { Text(it, color = if (estado == EstadoPaso.Hecho) c.ink else c.inkMuted, style = SqTema.tipografia.body.copy(fontSize = 13.sp, lineHeight = 18.sp, textAlign = TextAlign.End), modifier = Modifier.widthIn(max = ESTADO_ANCHO_MAXIMO)) }
        Icon(painterResource(R.drawable.ic_chevron_right), contentDescription = null, tint = c.inkMuted, modifier = Modifier.size(20.dp))
    }
}

/** La marca «Opcional» de un paso o de una pieza (`sq-gstep__opt`): borde fino, sin color propio. */
@Composable
fun Etiqueta(texto: String, modifier: Modifier = Modifier) {
    val c = SqTema.colores
    Text(
        texto,
        color = c.inkMuted,
        style = SqTema.tipografia.body.copy(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight(600)),
        maxLines = 1,
        softWrap = false,
        modifier = modifier
            .border(1.dp, c.lineStrong, RoundedCornerShape(SqTema.radios.radiusSm))
            .padding(horizontal = SqTema.espacios.space1),
    )
}
