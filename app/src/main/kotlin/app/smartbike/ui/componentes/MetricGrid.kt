package app.smartbike.ui.componentes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.smartbike.ui.tema.SqTema

/**
 * Un dato de la rejilla: [valor] `null` se muestra como «—» (sin dato, p. ej. sin Neo). Con [texto], el valor es una
 * palabra y no una cifra (`sq-metric-val text`): letra normal, que puede ocupar dos líneas. [plantilla] es su valor más
 * largo, con el que [MetricGrid] mide la cifra para que no cambie de tamaño con lo que marque.
 */
data class Dato(
    val etiqueta: String,
    val valor: String?,
    val unidad: String? = null,
    val texto: Boolean = false,
    val plantilla: String? = null,
    /** Color de la cifra: la pendiente en subida o bajada. */
    val tono: TonoDato = TonoDato.Neutro,
)

/** Subida y bajada se pintan en `climb` y `descent` (solo con más de 0,5 % de pendiente); el signo ya las distingue sin el color. */
enum class TonoDato { Neutro, Subida, Bajada }

/**
 * Dato con su etiqueta (`sq-metric`): etiqueta en mayúsculas y cifra con su unidad, alineadas por la línea base. La
 * unidad lleva su propio interlineado para no alargar el valor (`.sq-metric-val .sq-unit`): así mide lo mismo con
 * unidad que sin ella. [descripcionSinDato] es lo que lee el lector de pantalla en lugar de «—». Con [centrado], los
 * dos centrados en horizontal, como en la rejilla de Rodaje.
 */
@Composable
fun MetricTile(
    dato: Dato,
    tamanoCifra: TextUnit,
    modifier: Modifier = Modifier,
    color: Color = when (dato.tono) {
        TonoDato.Subida -> SqTema.colores.climb
        TonoDato.Bajada -> SqTema.colores.descent
        TonoDato.Neutro -> SqTema.colores.ink
    },
    descripcionSinDato: String = "",
    centrado: Boolean = false,
) {
    val c = SqTema.colores
    Column(
        modifier,
        verticalArrangement = Arrangement.spacedBy(SqTema.espacios.space1),
        horizontalAlignment = if (centrado) Alignment.CenterHorizontally else Alignment.Start,
    ) {
        Label(dato.etiqueta)
        Row(horizontalArrangement = Arrangement.spacedBy(SqTema.espacios.space1)) {
            val estilo = estiloCifra(tamanoCifra)
            if (dato.valor == null) {
                Text("—", color = c.inkMuted, style = estilo, softWrap = false, modifier = Modifier.alignByBaseline().semantics { contentDescription = descripcionSinDato })
            } else if (dato.texto) {
                Text(dato.valor, color = color, style = SqTema.tipografia.body.copy(fontWeight = FontWeight(600), lineHeight = 32.sp))
            } else {
                Text(dato.valor, color = color, style = estilo, softWrap = false, modifier = Modifier.alignByBaseline())
                dato.unidad?.let { Text(it, color = c.inkMuted, style = ESTILO_UNIDAD, softWrap = false, modifier = Modifier.alignByBaseline()) }
            }
        }
    }
}

/** La unidad, con interlineado ajustado a su tamaño: alineada por la línea base, no baja de la de la cifra. */
private val ESTILO_UNIDAD = SqTema.tipografia.body.copy(
    fontSize = 16.sp,
    fontWeight = FontWeight(600),
    lineHeight = 16.sp,
    lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.Both, LineHeightStyle.Mode.Tight),
)

private fun estiloCifra(tamano: TextUnit): TextStyle = SqTema.tipografia.metricXl.copy(fontSize = tamano, lineHeight = tamano)

/** Columnas y filas de la rejilla por número de datos (README de `MetricGrid`). */
fun distribucionRejilla(n: Int, vertical: Boolean): Pair<Int, Int> {
    val columnas = if (vertical) (if (n == 1) 1 else 2) else (if (n <= 3) 1 else 2)
    return columnas to (n + columnas - 1) / columnas
}

/**
 * Cifra de la rejilla y lo mínimo que necesita con ella: el alto de sus filas en vertical o el ancho de sus columnas en
 * horizontal. Lo que sobre del área hasta ese mínimo es lo que puede ceder a los botones − y + sin bajar la cifra.
 */
data class MedidaRejilla(val cifra: TextUnit, val minimo: Dp)

/**
 * Los datos tal como cuentan para medir la rejilla: un valor que no es más largo que su plantilla se cambia por ella,
 * que ya mide lo más ancho que puede marcar. Así la medida solo se repite cuando cambia algo que la mueve (etiqueta,
 * unidad, plantilla, un valor sin dato o más largo que su plantilla), no con cada dato nuevo del rodaje.
 */
fun datosParaMedir(datos: List<Dato>): List<Dato> = datos.map { d ->
    val cubierto = d.valor != null && d.plantilla != null && d.valor.length <= d.plantilla.length
    if (cubierto) d.copy(valor = d.plantilla) else d
}

/** Separaciones de la rejilla: `space-2` entre filas y `space-6` entre columnas. */
private val SEPARACION_FILAS = 8.dp
private val SEPARACION_COLUMNAS = 24.dp

/** Tope y paso con los que se busca la cifra, y el mínimo. */
private val TOPE_CIFRA = 144.sp
private val PASO_CIFRA = 2.sp
private val MINIMO_CIFRA = 16.sp

/**
 * La mayor cifra, hasta 144 sp, con la que la etiqueta y el valor de cada dato caben en su celda de un área de [ancho] ×
 * [alto], medidos como los pinta [MetricTile] con la escala de letra del móvil. Cada dato se mide con su valor y con su
 * plantilla, así que la cifra no cambia mientras se rueda. Con 2 columnas y un número impar de datos, el último ocupa la
 * fila entera.
 */
fun medirRejilla(datos: List<Dato>, ancho: Dp, alto: Dp, vertical: Boolean, medidor: TextMeasurer, densidad: Density): MedidaRejilla {
    val (columnas, filas) = distribucionRejilla(datos.size, vertical)
    val espacio = with(densidad) { 4.dp.toPx() }
    val etiqueta = medidor.measure("A", SqTema.tipografia.label, density = densidad).size.height
    val altoCelda = with(densidad) { ((alto - SEPARACION_FILAS * (filas - 1)) / filas).toPx() }
    val anchoCelda = with(densidad) { ((ancho - SEPARACION_COLUMNAS * (columnas - 1)) / columnas).toPx() }
    val anchoFila = with(densidad) { ancho.toPx() }
    fun ancha(i: Int) = columnas == 2 && datos.size % 2 == 1 && i == datos.lastIndex

    /** Ancho de un dato (etiqueta o cifra con su unidad, lo mayor) y alto de su cifra, con la plantilla y con el valor. */
    fun medir(d: Dato, cifra: TextStyle): Pair<Float, Int> {
        val textos = listOfNotNull(d.valor ?: "—", d.plantilla)
        val unidad = d.unidad?.let { medidor.measure(it, ESTILO_UNIDAD, softWrap = false, density = densidad).size.width + espacio } ?: 0f
        val tamanos = textos.map { medidor.measure(it, cifra, softWrap = false, density = densidad).size }
        val anchoCifra = tamanos.maxOf { it.width } + unidad
        val anchoEtiqueta = medidor.measure(d.etiqueta.uppercase(), SqTema.tipografia.label, softWrap = false, density = densidad).size.width
        return maxOf(anchoCifra, anchoEtiqueta.toFloat()) to tamanos.maxOf { it.height }
    }

    var tamano = TOPE_CIFRA
    while (true) {
        val cifra = estiloCifra(tamano)
        val medidas = datos.map { medir(it, cifra) }
        val cabe = medidas.withIndex().all { (i, m) ->
            m.first <= (if (ancha(i)) anchoFila else anchoCelda) && etiqueta + espacio + m.second <= altoCelda
        }
        if (cabe || tamano.value <= MINIMO_CIFRA.value) {
            val minimo = with(densidad) {
                if (vertical) {
                    (filas * (etiqueta + espacio + medidas.maxOf { it.second })).toDp() + SEPARACION_FILAS * (filas - 1)
                } else {
                    val normales = medidas.filterIndexed { i, _ -> !ancha(i) }.maxOfOrNull { it.first } ?: 0f
                    val anchas = medidas.filterIndexed { i, _ -> ancha(i) }.maxOfOrNull { it.first } ?: 0f
                    maxOf((columnas * normales).toDp() + SEPARACION_COLUMNAS * (columnas - 1), anchas.toDp())
                }
            }
            return MedidaRejilla(if (cabe) tamano else MINIMO_CIFRA, minimo)
        }
        tamano = (tamano.value - PASO_CIFRA.value).sp
    }
}

/**
 * Zona de datos de la pantalla de rodaje (`sq-mgrid`): de 1 a 6 datos sin desplazamiento, con la distribución de
 * `design-system/components/MetricGrid/README.md`. Cada dato va centrado en su celda (`.sq-ride .sq-mgrid .sq-metric`)
 * y, con 2 columnas y un número impar de datos, el último ocupa la fila entera, centrado.
 *
 * La cifra es [cifra] si llega (Rodaje la calcula con [medirRejilla] para repartir el sitio con los botones); si no, la
 * que da [medirRejilla] con el tamaño de la rejilla.
 */
@Composable
fun MetricGrid(
    datos: List<Dato>,
    modifier: Modifier = Modifier,
    descripcionSinDato: String = "",
    vertical: Boolean = false,
    cifra: TextUnit? = null,
) {
    require(datos.size in 1..6) { "MetricGrid admite de 1 a 6 datos: ${datos.size}" }
    val (columnas, _) = distribucionRejilla(datos.size, vertical)
    val medidor = rememberTextMeasurer()
    val densidad = LocalDensity.current
    BoxWithConstraints(modifier.fillMaxSize()) {
        val aMedir = datosParaMedir(datos)
        val tamano = cifra ?: remember(aMedir, maxWidth, maxHeight, densidad, vertical) {
            medirRejilla(aMedir, maxWidth, maxHeight, vertical, medidor, densidad).cifra
        }
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(SEPARACION_FILAS)) {
            datos.chunked(columnas).forEach { fila ->
                // La última fila con un solo dato en 2 columnas lo pone en todo el ancho, sin hueco a su derecha.
                Row(Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(SEPARACION_COLUMNAS)) {
                    fila.forEach { d ->
                        Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                            MetricTile(d, tamano, descripcionSinDato = descripcionSinDato, centrado = true)
                        }
                    }
                }
            }
        }
    }
}
