package app.smartbike.ui.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.smartbike.ui.tema.SqTema
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Ajuste numérico con − y + (`sq-step`). Mantener un botón repite. En un límite, el botón que lo superaría se
 * deshabilita: atenuado, sin respuesta y anunciado como no disponible; el valor no se atenúa.
 */
@Composable
fun Stepper(
    nombre: String,
    valor: String,
    unidad: String,
    alRestar: () -> Unit,
    alSumar: () -> Unit,
    descripcionRestar: String,
    descripcionSumar: String,
    modifier: Modifier = Modifier,
    puedeRestar: Boolean = true,
    puedeSumar: Boolean = true,
    ayuda: String? = null,
) {
    val c = SqTema.colores
    Row(
        modifier
            .fillMaxWidth()
            .background(c.surface)
            .padding(horizontal = SqTema.espacios.space4, vertical = SqTema.espacios.space3),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SqTema.espacios.space4),
    ) {
        Column(Modifier.weight(1f)) {
            Text(nombre, style = SqTema.tipografia.body)
            ayuda?.let { Caption(it) }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(SqTema.espacios.space2)) {
            BotonPaso("−", descripcionRestar, puedeRestar, alRestar)
            Text(
                buildAnnotatedString {
                    append(valor)
                    withStyle(SpanStyle(fontSize = 14.sp, fontFamily = SqTema.familias.sans, color = c.inkMuted)) { append(" $unidad") }
                },
                style = SqTema.tipografia.metricMd.copy(textAlign = TextAlign.Center),
                modifier = Modifier.widthIn(min = 88.dp),
            )
            BotonPaso("+", descripcionSumar, puedeSumar, alSumar)
        }
    }
}

@Composable
internal fun BotonPaso(glifo: String, descripcion: String, habilitado: Boolean, accion: () -> Unit) {
    val c = SqTema.colores
    val forma = RoundedCornerShape(SqTema.radios.radiusMd)
    val accionActual by rememberUpdatedState(accion)
    val habilitadoActual by rememberUpdatedState(habilitado)
    Box(
        Modifier
            .size(SqTema.tamanos.touchMin)
            .alpha(if (habilitado) 1f else DESHABILITADO)
            .background(c.bg, forma)
            .border(1.dp, c.lineStrong, forma)
            .semantics {
                role = Role.Button
                contentDescription = descripcion
                if (!habilitado) disabled()
                onClick {
                    if (habilitadoActual) accionActual()
                    habilitadoActual
                }
            }
            .pointerInput(Unit) {
                // Pulsar ejecuta una vez al levantar; mantener repite tras REPETIR_TRAS_MS cada REPETIR_CADA_MS (como las
                // palancas). Si el gesto acaba en desplazamiento antes de repetir (la lista lo consume), no hace nada.
                coroutineScope {
                    awaitEachGesture {
                        awaitFirstDown()
                        if (!habilitadoActual) return@awaitEachGesture
                        var repitio = false
                        val repeticion = launch {
                            delay(REPETIR_TRAS_MS)
                            repitio = true
                            while (habilitadoActual) {
                                accionActual()
                                delay(REPETIR_CADA_MS)
                            }
                        }
                        val arriba = waitForUpOrCancellation()
                        repeticion.cancel()
                        if (arriba != null && !repitio && habilitadoActual) accionActual()
                    }
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(glifo, style = SqTema.tipografia.body.copy(fontSize = 22.sp, fontWeight = FontWeight(600), lineHeight = 22.sp), color = c.ink)
    }
}

private const val REPETIR_TRAS_MS = 400L
private const val REPETIR_CADA_MS = 100L
