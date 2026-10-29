package app.smartbike.ui.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import app.smartbike.R
import app.smartbike.ui.tema.SqTema

/** Símbolos y cruceta del Square que la tecla dibuja como icono (`i-tecla_*`), con su nombre accesible. */
internal enum class SimboloTecla(val glifo: String, val icono: Int, val descripcion: Int) {
    Triangulo("△", R.drawable.ic_tecla_triangulo, R.string.tecla_triangulo),
    Circulo("○", R.drawable.ic_tecla_circulo, R.string.tecla_circulo),
    Cuadrado("□", R.drawable.ic_tecla_cuadrado, R.string.tecla_cuadrado),
    Equis("✕", R.drawable.ic_tecla_equis, R.string.tecla_equis),
    Arriba("↑", R.drawable.ic_tecla_arriba, R.string.tecla_arriba),
    Abajo("↓", R.drawable.ic_tecla_abajo, R.string.tecla_abajo),
    Izquierda("←", R.drawable.ic_tecla_izquierda, R.string.tecla_izquierda),
    Derecha("→", R.drawable.ic_tecla_derecha, R.string.tecla_derecha),
    ;

    companion object {
        fun de(texto: String): SimboloTecla? = entries.firstOrNull { it.glifo == texto }
    }
}

/**
 * Un botón del Square como tecla (`sq-key`): icono dibujado para △ ○ □ ✕ y la cruceta, letra (A B Y Z) o, si [ancha],
 * nombre corto («Palanca D1», «Freno I», «Campag. D»). Pulsada: relleno de marca.
 */
@Composable
fun SquareKey(texto: String, modifier: Modifier = Modifier, ancha: Boolean = false, pulsada: Boolean = false) {
    val c = SqTema.colores
    val forma = RoundedCornerShape(SqTema.radios.radiusMd)
    val estilo = if (ancha) {
        SqTema.tipografia.body.copy(fontSize = 13.sp, fontWeight = FontWeight(600), letterSpacing = 0.02.em, lineHeight = 13.sp)
    } else {
        SqTema.tipografia.body.copy(fontFamily = SqTema.familias.numeric, fontSize = 18.sp, fontWeight = FontWeight(700), lineHeight = 18.sp)
    }
    val simbolo = SimboloTecla.de(texto)
    val tinta = if (pulsada) c.onGearFill else c.ink
    Box(
        modifier
            .height(36.dp)
            .widthIn(min = 36.dp)
            .background(if (pulsada) c.gearFill else c.surface2, forma)
            .border(1.dp, if (pulsada) c.gearFill else c.lineStrong, forma)
            .padding(horizontal = SqTema.espacios.space2)
            .semantics(mergeDescendants = true) { selected = pulsada },
        contentAlignment = Alignment.Center,
    ) {
        if (simbolo != null) {
            Icon(painterResource(simbolo.icono), contentDescription = stringResource(simbolo.descripcion), modifier = Modifier.size(20.dp), tint = tinta)
        } else {
            Text(texto, style = estilo, color = tinta, maxLines = 1, overflow = TextOverflow.Clip, softWrap = false)
        }
    }
}

/**
 * Combinación de teclas unidas por «+», el modificador primero, con el gesto y la acción si los hay (aviso de gesto,
 * `sq-toast`). Si no cabe en una línea, pasa entera a la siguiente.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CombinacionSquare(teclas: List<String>, modifier: Modifier = Modifier, gesto: String? = null, accion: String? = null) {
    val c = SqTema.colores
    val forma = RoundedCornerShape(SqTema.radios.radiusMd)
    FlowRow(
        modifier
            .background(c.surface, forma)
            .border(1.dp, c.line, forma)
            .padding(start = SqTema.espacios.space2, end = SqTema.espacios.space4, top = SqTema.espacios.space2, bottom = SqTema.espacios.space2),
        horizontalArrangement = Arrangement.spacedBy(SqTema.espacios.space3),
        verticalArrangement = Arrangement.spacedBy(SqTema.espacios.space2),
        itemVerticalAlignment = Alignment.CenterVertically,
    ) {
        teclas.forEachIndexed { i, t ->
            if (i > 0) Text("+", color = c.inkMuted, style = SqTema.tipografia.body.copy(fontWeight = FontWeight(600)))
            SquareKey(t, ancha = true)
        }
        gesto?.let { Text(it, color = c.inkMuted, style = SqTema.tipografia.caption) }
        accion?.let { Text(it, color = c.ink, style = SqTema.tipografia.body.copy(fontWeight = FontWeight(600)), softWrap = false) }
    }
}
