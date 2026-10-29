package app.smartbike.ui.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.smartbike.ui.tema.SqTema

/** Sección de una pantalla de ajustes (`sq-section`): título, ayuda opcional y su contenido. */
@Composable
fun SettingsSection(
    titulo: String,
    modifier: Modifier = Modifier,
    ayuda: String? = null,
    contenido: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(SqTema.espacios.space2)) {
        Text(titulo, style = SqTema.tipografia.heading, modifier = Modifier.semantics { heading() })
        ayuda?.let { Caption(it) }
        contenido()
    }
}

/** Lugar de una fila dentro de su [SettingsList], para que lo que se pinta junto al borde de la lista siga su esquina. */
internal data class PosicionFila(val primera: Boolean, val ultima: Boolean)

internal val LocalPosicionFila = compositionLocalOf { PosicionFila(primera = false, ultima = false) }

/** Lista de filas con borde y separadores (`sq-list`). Cada elemento de [filas] es una fila. */
@Composable
fun SettingsList(filas: List<@Composable () -> Unit>, modifier: Modifier = Modifier) {
    val forma = RoundedCornerShape(SqTema.radios.radiusMd)
    Column(modifier.fillMaxWidth().clip(forma).border(1.dp, SqTema.colores.line, forma)) {
        filas.forEachIndexed { i, fila ->
            if (i > 0) HorizontalDivider(thickness = 1.dp, color = SqTema.colores.line)
            CompositionLocalProvider(LocalPosicionFila provides PosicionFila(primera = i == 0, ultima = i == filas.lastIndex)) { fila() }
        }
    }
}

/** Fila de una lista (`sq-item`): icono, nombre, ayuda y lo que vaya a la derecha. Al menos `touch-min` de alto. */
@Composable
fun SettingsItem(
    nombre: String,
    modifier: Modifier = Modifier,
    icono: Int? = null,
    iconoTag: String? = null,
    ayuda: String? = null,
    final: (@Composable () -> Unit)? = null,
    ayudaUnaLinea: Boolean = false,
) {
    Row(
        modifier
            .fillMaxWidth()
            .heightIn(min = SqTema.tamanos.touchMin)
            .background(SqTema.colores.surface)
            .padding(horizontal = SqTema.espacios.space4, vertical = SqTema.espacios.space3),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SqTema.espacios.space3),
    ) {
        icono?.let {
            Icon(painterResource(it), contentDescription = null, tint = SqTema.colores.inkMuted, modifier = if (iconoTag != null) Modifier.testTag(iconoTag) else Modifier)
        }
        Column(Modifier.weight(1f)) {
            Text(nombre, style = SqTema.tipografia.body.copy(fontWeight = FontWeight(600)))
            ayuda?.let { Caption(it, maxLineas = if (ayudaUnaLinea) 1 else Int.MAX_VALUE) }
        }
        final?.invoke()
    }
}

/** Fila apilada (`sq-item--stack`): contenido libre en columna sobre el fondo de la lista. */
@Composable
fun SettingsItemApilado(modifier: Modifier = Modifier, contenido: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier
            .fillMaxWidth()
            .background(SqTema.colores.surface)
            .padding(horizontal = SqTema.espacios.space4, vertical = SqTema.espacios.space3),
        verticalArrangement = Arrangement.spacedBy(SqTema.espacios.space3),
        content = contenido,
    )
}

/** Texto de ayuda (`sq-caption`). */
@Composable
fun Caption(texto: String, modifier: Modifier = Modifier, maxLineas: Int = Int.MAX_VALUE) {
    Text(
        texto,
        style = SqTema.tipografia.caption,
        color = SqTema.colores.inkMuted,
        modifier = modifier,
        maxLines = maxLineas,
        overflow = TextOverflow.Ellipsis,
    )
}

/** Etiqueta en versalitas (`sq-label`). */
@Composable
fun Label(texto: String, modifier: Modifier = Modifier) {
    Text(texto.uppercase(), style = SqTema.tipografia.label, color = SqTema.colores.inkMuted, modifier = modifier)
}

/**
 * Fila con casilla (`sq-item` con `sq-check`): toda la fila cambia el valor, y se anuncia como casilla con su nombre.
 */
@Composable
fun SettingsItemCasilla(nombre: String, marcada: Boolean, alCambiar: (Boolean) -> Unit, modifier: Modifier = Modifier, ayuda: String? = null) {
    SettingsItem(
        nombre,
        modifier.toggleable(marcada, role = Role.Checkbox, onValueChange = alCambiar),
        ayuda = ayuda,
        final = {
            Checkbox(
                checked = marcada,
                onCheckedChange = null,
                colors = CheckboxDefaults.colors(
                    checkedColor = SqTema.colores.gearFill,
                    checkmarkColor = SqTema.colores.onGearFill,
                    uncheckedColor = SqTema.colores.lineStrong,
                ),
            )
        },
    )
}

/**
 * Control segmentado (`sq-seg`): una opción elegida, con relleno de marca. Las opciones de [deshabilitadas] se
 * atenúan y no responden.
 */
@Composable
fun Segmentado(
    opciones: List<String>,
    elegida: Int?,
    alElegir: (Int) -> Unit,
    modifier: Modifier = Modifier,
    deshabilitadas: Set<Int> = emptySet(),
) {
    val c = SqTema.colores
    val forma = RoundedCornerShape(SqTema.radios.radiusMd)
    Row(
        modifier.fillMaxWidth().height(IntrinsicSize.Min).clip(forma).border(1.dp, c.lineStrong, forma).selectableGroup(),
    ) {
        opciones.forEachIndexed { i, texto ->
            if (i > 0) VerticalDivider(thickness = 1.dp, color = c.lineStrong)
            val sel = i == elegida
            val habilitada = i !in deshabilitadas
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .heightIn(min = SqTema.tamanos.touchMin)
                    .alpha(if (habilitada) 1f else DESHABILITADO)
                    .background(if (sel) c.gearFill else c.surface)
                    .selectable(sel, enabled = habilitada, role = Role.RadioButton, onClick = { alElegir(i) }),
                contentAlignment = Alignment.Center,
            ) {
                Text(texto, style = SqTema.tipografia.body.copy(fontWeight = FontWeight(600)), color = if (sel) c.onGearFill else c.ink)
            }
        }
    }
}

/**
 * Opción de radio con descripción (`sq-opt`): la fila entera elige la opción, y la elegida lleva un anillo `gear`
 * además del radio marcado, así no depende solo del color. Va dentro de una [SettingsList] con `selectableGroup`. Sin
 * [ayuda] (como las opciones de idioma) la fila solo lleva el nombre.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsOpcion(
    nombre: String,
    ayuda: String?,
    elegida: Boolean,
    alElegir: () -> Unit,
    modifier: Modifier = Modifier,
    habilitada: Boolean = true,
    /** Una marca en texto tras el nombre, como «Recomendado» (`sq-tag`). */
    etiqueta: String? = null,
    /** Una segunda línea de ayuda, como el detalle técnico de un modo (`ModeList`). */
    detalle: String? = null,
    /** Etiquetas bajo la ayuda, como la compatibilidad de un modo (`sq-compat`, `ModeList`). */
    etiquetas: List<AnnotatedString> = emptyList(),
) {
    val c = SqTema.colores
    val posicion = LocalPosicionFila.current
    val radioLista = SqTema.radios.radiusMd
    Row(
        modifier
            .fillMaxWidth()
            .heightIn(min = SqTema.tamanos.touchMin)
            .alpha(if (habilitada) 1f else 0.45f)
            .background(c.surface)
            .then(if (elegida) Modifier.drawBehind { anilloInterior(c.gear, posicion, radioLista) } else Modifier)
            .selectable(elegida, enabled = habilitada, role = Role.RadioButton, onClick = alElegir)
            .padding(horizontal = SqTema.espacios.space4, vertical = SqTema.espacios.space3),
        horizontalArrangement = Arrangement.spacedBy(SqTema.espacios.space3),
    ) {
        Box(
            Modifier
                .padding(top = 2.dp)
                .size(20.dp)
                .background(c.surface, CircleShape)
                .border(2.dp, if (elegida) c.gear else c.lineStrong, CircleShape)
                .drawBehind { if (elegida) drawCircle(c.gear, radius = 4.dp.toPx()) },
        )
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(SqTema.espacios.space2)) {
                Text(nombre, Modifier.weight(1f, fill = false), style = SqTema.tipografia.body.copy(fontWeight = FontWeight(600)))
                if (etiqueta != null) Etiqueta(etiqueta)
            }
            if (ayuda != null) Caption(ayuda)
            if (detalle != null) Caption(detalle)
            if (etiquetas.isNotEmpty()) {
                FlowRow(
                    Modifier.padding(top = SqTema.espacios.space2),
                    horizontalArrangement = Arrangement.spacedBy(SqTema.espacios.space2),
                    verticalArrangement = Arrangement.spacedBy(SqTema.espacios.space1),
                ) { etiquetas.forEach { Etiqueta(it, negrita = false) } }
            }
        }
    }
}

/** Marca de texto junto a un nombre (`sq-tag`): `ink` sobre `surface-2` con borde `line-strong`; se lee por la palabra. */
@Composable
private fun Etiqueta(texto: String) = Etiqueta(AnnotatedString(texto), negrita = true)

/** Con [negrita] falso, el peso normal salvo lo que [texto] marque (la compatibilidad: «**Zwift:** validado»). */
@Composable
private fun Etiqueta(texto: AnnotatedString, negrita: Boolean) {
    val c = SqTema.colores
    val forma = RoundedCornerShape(SqTema.radios.radiusSm)
    Text(
        texto,
        Modifier.background(c.surface2, forma).border(1.dp, c.lineStrong, forma).padding(horizontal = SqTema.espacios.space2),
        style = SqTema.tipografia.caption.copy(fontWeight = FontWeight(if (negrita) 600 else 400)),
        color = c.ink,
    )
}

/**
 * Anillo de 2 dp dentro de la fila, como el `inset box-shadow` de la maqueta. El borde de la lista (1 dp) se pinta encima de las
 * filas, así que el anillo arranca 1 dp por dentro en los lados que tocan ese borde y sigue su esquina redondeada.
 */
private fun DrawScope.anilloInterior(color: Color, posicion: PosicionFila, radioLista: Dp) {
    val grosor = 2.dp.toPx()
    val borde = 1.dp.toPx()
    val radio = (radioLista.toPx() - borde).coerceAtLeast(0f)
    val izquierda = borde + grosor / 2
    val derecha = size.width - borde - grosor / 2
    val arriba = (if (posicion.primera) borde else 0f) + grosor / 2
    val abajo = size.height - (if (posicion.ultima) borde else 0f) - grosor / 2
    val arribaRadio = if (posicion.primera) CornerRadius(radio - grosor / 2) else CornerRadius.Zero
    val abajoRadio = if (posicion.ultima) CornerRadius(radio - grosor / 2) else CornerRadius.Zero
    val trazo = Path().apply {
        addRoundRect(RoundRect(izquierda, arriba, derecha, abajo, arribaRadio, arribaRadio, abajoRadio, abajoRadio))
    }
    drawPath(trazo, color, style = Stroke(grosor))
}
