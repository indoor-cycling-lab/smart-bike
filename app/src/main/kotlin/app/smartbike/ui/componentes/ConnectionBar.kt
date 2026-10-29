package app.smartbike.ui.componentes

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import app.smartbike.R
import app.smartbike.ui.tema.SqTema

/**
 * Los enlaces de la app y su icono, el mismo en Rodaje, Dispositivos y Diagnóstico (`design-system/README.md`,
 * Iconografía). El Square lleva el manillar; [Mando], el controlador que la app publica, emula el controlador de Zwift
 * y lleva el icono de Zwift, como la plataforma.
 */
enum class Enlace(@DrawableRes val icono: Int) {
    Neo(R.drawable.ic_bolt),
    Square(R.drawable.ic_manillar),
    Zwift(R.drawable.ic_zwift),
    Mando(R.drawable.ic_zwift),
    Pulso(R.drawable.ic_favorite),
    Ventilador(R.drawable.ic_mode_fan),
}

private val TAMANO_ICONO = 14.dp

/** Un icono de la barra: su [estado] y [descripcion], «Neo, conectado», que se une a la de los demás. */
data class EnlaceBarra(val enlace: Enlace, val estado: EstadoChip, val descripcion: String)

/**
 * Barra de conexiones de Rodaje (`sq-connbar`): un icono de 14 dp por enlace, agrupados con 4 dp entre ellos (`space-1`), y un único
 * objetivo táctil de `touch-min` de alto para todos. El estado va en la forma del icono, no solo en el color:
 * conectado, el icono en `ink`; esperando, en `ink-muted` con un hueco en la esquina y un arco que gira dentro; sin
 * conexión, en `danger` y tachado. El hueco se recorta del icono (capa fuera de pantalla con `BlendMode.Clear`) para
 * no pisar su dibujo. El ancho no depende del estado. La descripción es una sola: «Conexiones: Neo, conectado; …».
 */
@Composable
fun ConnectionBar(enlaces: List<EnlaceBarra>, alTocar: () -> Unit, modifier: Modifier = Modifier) {
    val descripcion = stringResource(R.string.conexiones_descripcion, enlaces.joinToString("; ") { it.descripcion })
    Row(
        modifier
            .semantics(mergeDescendants = true) { contentDescription = descripcion }
            .height(SqTema.tamanos.touchMin)
            .clickable(role = Role.Button, onClick = alTocar)
            .padding(horizontal = SqTema.espacios.space2),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SqTema.espacios.space1),
    ) {
        enlaces.forEach { IconoEstado(it.enlace, it.estado) }
    }
}

/** El icono de un enlace en `ink-muted`, delante de su nombre en Dispositivos y Diagnóstico; `testTag` «icono-neo», «icono-mando»… */
@Composable
fun IconoEnlace(enlace: Enlace, modifier: Modifier = Modifier) {
    Icon(painterResource(enlace.icono), contentDescription = null, tint = SqTema.colores.inkMuted, modifier = modifier.testTag(etiquetaIcono(enlace)))
}

fun etiquetaIcono(enlace: Enlace) = "icono-${enlace.name.lowercase()}"

/**
 * El tachado de «sin conexión»: un hueco diagonal de 5 unidades de esquina a esquina y, dentro, una línea de 2, en
 * unidades de un icono de 24 dp, con una capa fuera de pantalla.
 */
internal fun DrawScope.dibujarTachado(color: Color) {
    val u = size.width / 24f
    drawLine(Color.Black, Offset(-4 * u, -4 * u), Offset(28 * u, 28 * u), strokeWidth = 5 * u, blendMode = BlendMode.Clear)
    drawLine(color, Offset(1.4f * u, 1.4f * u), Offset(22.6f * u, 22.6f * u), strokeWidth = 2 * u, cap = StrokeCap.Round)
}

/**
 * El giro de 0 a 360° por segundo del estado «Esperando». Mientras existe, la animación pide fotogramas: créalo solo en
 * ese estado y léelo al dibujar, no al componer.
 */
@Composable
internal fun rememberGiro(): State<Float> = rememberInfiniteTransition(label = "giro").animateFloat(
    0f,
    360f,
    infiniteRepeatable(tween(1000, easing = LinearEasing), RepeatMode.Restart),
    label = "giro",
)

@Composable
private fun IconoEstado(enlace: Enlace, estado: EstadoChip) {
    val c = SqTema.colores
    val color = when (estado) {
        EstadoChip.Sin -> c.danger
        EstadoChip.Esperando, EstadoChip.Neutro -> c.inkMuted
        EstadoChip.Ok, EstadoChip.Baja -> c.ink
    }
    val giro = if (estado == EstadoChip.Esperando) rememberGiro() else null
    // La insignia de «batería baja» sale del icono (esquina superior izquierda): se dibuja fuera de la capa del icono para no recortarse.
    Box(
        Modifier
            .size(TAMANO_ICONO)
            .drawWithContent {
                drawContent()
                if (estado == EstadoChip.Baja) dibujarInsigniaBateria(c.ink, c.bg)
            },
    ) {
        Icon(
            painterResource(enlace.icono),
            contentDescription = null,
            tint = color,
            modifier = Modifier
                .size(TAMANO_ICONO)
                .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
                .drawWithContent {
                    drawContent()
                    // Las medidas de la marca van en unidades de un icono de 24 dp y se escalan al tamaño real.
                    val u = size.width / 24f
                    when (estado) {
                        EstadoChip.Esperando -> {
                            // Hueco circular en la esquina inferior derecha y, dentro, el arco de 270° que gira.
                            val centro = Offset(19 * u, 19 * u)
                            drawCircle(Color.Black, radius = 7 * u, center = centro, blendMode = BlendMode.Clear)
                            val trazo = 2 * u
                            val lado = 10 * u - trazo
                            drawArc(
                                color,
                                startAngle = giro?.value ?: 0f,
                                sweepAngle = 270f,
                                useCenter = false,
                                topLeft = Offset(centro.x - lado / 2, centro.y - lado / 2),
                                size = Size(lado, lado),
                                style = Stroke(trazo),
                            )
                        }

                        EstadoChip.Sin -> dibujarTachado(color)

                        else -> Unit
                    }
                },
        )
    }
}

/**
 * La insignia de «batería baja» (`sq-connbar__i.is-low`): un círculo de 11 unidades en `ink`, en la esquina superior
 * izquierda del icono y asomando por fuera, con un «!» en el color del fondo. Unidades de un icono de 24 dp.
 */
internal fun DrawScope.dibujarInsigniaBateria(color: Color, fondo: Color) {
    val u = size.width / 24f
    val lado = 11 * u
    val x0 = -3.5f * u
    val y0 = -1 * u
    drawCircle(color, radius = lado / 2, center = Offset(x0 + lado / 2, y0 + lado / 2))
    val barra = Size(1.9f * u, 4.6f * u)
    drawRect(fondo, Offset(x0 + (lado - barra.width) / 2, y0 + 0.2f * (lado - barra.height)), barra)
    drawCircle(fondo, radius = 1 * u, center = Offset(x0 + lado / 2, y0 + 0.76f * lado))
}
