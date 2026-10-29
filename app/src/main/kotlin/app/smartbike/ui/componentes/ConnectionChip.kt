package app.smartbike.ui.componentes

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.smartbike.ui.tema.SqTema

/** [Baja]: conectado con la batería baja (solo el Square en la barra de Rodaje); en un chip se dibuja como [Ok]. */
enum class EstadoChip { Ok, Esperando, Sin, Neutro, Baja }

/**
 * Estado de un enlace (`sq-conn`): punto lleno si está conectado, anillo girando si espera, anillo vacío con borde de
 * peligro si no hay conexión, y un guion (`sq-conn idle`) si está desactivado o no disponible, que no es ni espera ni
 * alerta. Siempre con texto: el estado no depende solo del color. Con [estadoTexto], el estado
 * va en palabra tras el nombre («Square sin conexión»); sin él, la forma basta y [descripcion] da el
 * estado a los lectores de pantalla («Square, sin conexión»). Con [alTocar] es tocable, con un objetivo de
 * `touch-min` aunque el chip mida 32 dp. Si el texto no cabe pasa a dos líneas sin salirse del chip.
 */
@Composable
fun ConnectionChip(
    estado: EstadoChip,
    texto: String,
    modifier: Modifier = Modifier,
    estadoTexto: String? = null,
    alTocar: (() -> Unit)? = null,
    descripcion: String? = null,
    /** El texto en `ink-muted`: el estado de la plataforma de Rodaje mientras solo espera (`PlatformBadge`). */
    atenuado: Boolean = false,
) {
    val conDescripcion =
        if (descripcion == null) modifier else modifier.semantics(mergeDescendants = true) { contentDescription = descripcion }
    if (alTocar != null) {
        // Objetivo táctil de touch-min aunque el chip mida 32 dp (`a.sq-conn`).
        Box(
            conDescripcion.heightIn(min = SqTema.tamanos.touchMin).clickable(role = Role.Button, onClick = alTocar),
            contentAlignment = Alignment.Center,
        ) { Chip(estado, texto, estadoTexto, Modifier, atenuado) }
    } else {
        Chip(estado, texto, estadoTexto, conDescripcion, atenuado)
    }
}

@Composable
private fun Chip(estado: EstadoChip, texto: String, estadoTexto: String?, modifier: Modifier, atenuado: Boolean = false) {
    val c = SqTema.colores
    Row(
        modifier
            .heightIn(min = 32.dp)
            .clip(CircleShape)
            .background(c.surface)
            .border(1.dp, if (estado == EstadoChip.Sin) c.danger else c.lineStrong, CircleShape)
            .padding(horizontal = SqTema.espacios.space3, vertical = SqTema.espacios.space1),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SqTema.espacios.space2),
    ) {
        Punto(estado)
        // Si no cabe junto a las conexiones pasa a dos líneas (2 × 18 + 8 = 44 dp), dentro del objetivo táctil de `touch-min`.
        Text(texto, color = if (atenuado) c.inkMuted else c.ink, style = SqTema.tipografia.body.copy(fontSize = 14.sp, lineHeight = 18.sp, fontWeight = FontWeight(600)))
        estadoTexto?.let {
            Text(it, color = if (estado == EstadoChip.Sin) c.danger else c.inkMuted, style = SqTema.tipografia.body.copy(fontSize = 14.sp))
        }
    }
}

@Composable
internal fun Punto(estado: EstadoChip) {
    val c = SqTema.colores
    val giro = if (estado == EstadoChip.Esperando) rememberGiro() else null
    Canvas(Modifier.size(10.dp)) {
        val trazo = 2.dp.toPx()
        when (estado) {
            EstadoChip.Ok, EstadoChip.Baja -> drawCircle(c.ok)

            EstadoChip.Neutro -> drawLine(
                c.inkMuted,
                Offset(0f, size.height / 2),
                Offset(size.width, size.height / 2),
                strokeWidth = trazo,
                cap = StrokeCap.Round,
            )

            EstadoChip.Sin -> drawCircle(c.danger, radius = (size.minDimension - trazo) / 2, style = Stroke(trazo))

            EstadoChip.Esperando -> drawArc(
                c.inkMuted,
                startAngle = giro?.value ?: 0f,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = Offset(trazo / 2, trazo / 2),
                size = Size(size.width - trazo, size.height - trazo),
                style = Stroke(trazo),
            )
        }
    }
}
