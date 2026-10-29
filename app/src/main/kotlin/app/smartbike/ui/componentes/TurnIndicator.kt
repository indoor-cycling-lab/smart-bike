package app.smartbike.ui.componentes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import app.smartbike.R
import app.smartbike.dominio.LadoGiro
import app.smartbike.ui.tema.SqTema

/**
 * Aviso de giro (`sq-turn`, spec `ride-screen` «Aviso de giro»): una flecha de 56 dp a cada lado de [contenido] (la marcha),
 * la de la izquierda con el giro a la izquierda y la de la derecha con el giro a la derecha. El sitio de las dos flechas
 * está siempre reservado (invisibles, no quitadas), así que al aparecer no se mueve nada. Se ve por la forma y la posición,
 * no por el color, y la flecha encendida se anuncia al lector de pantalla. La derecha es la izquierda reflejada.
 */
@Composable
fun TurnIndicator(giro: Set<LadoGiro>, modifier: Modifier = Modifier, contenido: @Composable () -> Unit) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(SqTema.espacios.space4), verticalAlignment = Alignment.CenterVertically) {
        Flecha(LadoGiro.IZQUIERDA in giro, derecha = false)
        contenido()
        Flecha(LadoGiro.DERECHA in giro, derecha = true)
    }
}

@Composable
private fun Flecha(encendida: Boolean, derecha: Boolean) {
    val descripcion = stringResource(if (derecha) R.string.girando_derecha else R.string.girando_izquierda)
    Box(
        Modifier
            .size(TAMANO)
            .testTag(if (derecha) ETIQUETA_DERECHA else ETIQUETA_IZQUIERDA)
            .alpha(if (encendida) 1f else 0f)
            .then(
                if (encendida) {
                    Modifier.semantics {
                        contentDescription = descripcion
                        liveRegion = LiveRegionMode.Polite
                    }
                } else {
                    Modifier.clearAndSetSemantics { }
                },
            ),
    ) {
        Icon(
            painterResource(R.drawable.ic_arrow_back),
            contentDescription = null,
            tint = SqTema.colores.gear,
            modifier = Modifier.size(TAMANO).graphicsLayer(scaleX = if (derecha) -1f else 1f),
        )
    }
}

private val TAMANO = 56.dp
const val ETIQUETA_IZQUIERDA = "giro-izquierda"
const val ETIQUETA_DERECHA = "giro-derecha"
