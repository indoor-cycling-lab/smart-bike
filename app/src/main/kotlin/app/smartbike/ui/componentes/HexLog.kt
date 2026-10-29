package app.smartbike.ui.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.smartbike.ui.tema.SqTema

/**
 * Una fila del volcado. [cambiados] marca los bytes (por índice) que cambian respecto a la trama anterior de la misma
 * característica. [enviada]: lo envía la app («→ Neo»), con el origen en color de marca.
 */
@Immutable
data class FilaHex(
    val hora: String,
    val origen: String,
    val caracteristica: String,
    val contenido: String,
    val enviada: Boolean = false,
    val cambiados: Set<Int> = emptySet(),
    val esHex: Boolean = true,
)

/**
 * Registro en crudo de lo que entra y sale por Bluetooth (`sq-hex`), la más reciente arriba. Alto limitado a
 * [FILAS_VISIBLES] filas; el resto se ve desplazando dentro del bloque, no la pantalla. Es una lista perezosa: con
 * cientos de filas que cambian 10 veces por segundo, solo se componen las visibles.
 */
@Composable
fun HexLog(filas: List<FilaHex>, vacio: String, descripcion: String, modifier: Modifier = Modifier) {
    val c = SqTema.colores
    val forma = RoundedCornerShape(SqTema.radios.radiusMd)
    val estilo = SqTema.tipografia.mono.copy(fontSize = 13.sp, lineHeight = 20.sp)
    Box(
        modifier
            .fillMaxWidth()
            .background(c.surface, forma)
            .border(1.dp, c.line, forma)
            .semantics { contentDescription = descripcion }
            .horizontalScroll(rememberScrollState()),
    ) {
        LazyColumn(
            Modifier.heightIn(max = altoMaximo()),
            contentPadding = PaddingValues(horizontal = SqTema.espacios.space4, vertical = SqTema.espacios.space3),
        ) {
            if (filas.isEmpty()) item { Text(vacio, style = estilo, color = c.inkMuted) }
            items(filas) { f ->
                Row {
                    // Columnas fijas en caracteres de la fuente mono: 12ch, 6ch, 6ch (components.css).
                    Text(f.hora, style = estilo, color = c.inkMuted, modifier = Modifier.width(ancho(12)))
                    Spacer(Modifier.width(SqTema.espacios.space3))
                    Text(f.origen, style = estilo, color = if (f.enviada) c.gear else c.inkMuted, modifier = Modifier.width(ancho(7)))
                    Spacer(Modifier.width(SqTema.espacios.space3))
                    Text(f.caracteristica, style = estilo, color = c.inkMuted, modifier = Modifier.width(ancho(6)))
                    Spacer(Modifier.width(SqTema.espacios.space3))
                    Text(contenido(f), style = estilo, color = c.ink, softWrap = false)
                }
            }
        }
    }
}

@Composable
private fun contenido(f: FilaHex) = buildAnnotatedString {
    if (!f.esHex || f.cambiados.isEmpty()) {
        append(f.contenido)
        return@buildAnnotatedString
    }
    f.contenido.split(" ").forEachIndexed { i, b ->
        if (i > 0) append(" ")
        if (i in f.cambiados) withStyle(SpanStyle(color = SqTema.colores.gear)) { append(b) } else append(b)
    }
}

// Ancho de un carácter de JetBrains Mono a 13 sp: 0,6 em. En sp, para que crezca con el tamaño de letra del sistema.
@Composable
private fun ancho(caracteres: Int) = with(LocalDensity.current) { (caracteres * 13 * 0.6).sp.toDp() }

// Alto máximo: FILAS_VISIBLES líneas de 20 sp más el relleno vertical (max-height de .sq-hex en components.css).
@Composable
private fun altoMaximo() = with(LocalDensity.current) { (FILAS_VISIBLES * 20).sp.toDp() } + SqTema.espacios.space3 * 2

private const val FILAS_VISIBLES = 12
