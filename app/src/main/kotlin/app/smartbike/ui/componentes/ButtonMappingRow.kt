package app.smartbike.ui.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import app.smartbike.R
import app.smartbike.ui.tema.SqTema

/** Quién resuelve una función de los botones: Zwift con el Square emparejado o la app (`sq-layer`). */
enum class Capa { Zwift, App }

/**
 * Una entrada en una fila de asignación: [teclas] unidas por [separador] («+» en una combinación, «/» entre botones
 * equivalentes) y el [gesto] detrás.
 */
data class EntradaUi(val teclas: List<String>, val gesto: String, val separador: String = "+")

/**
 * Fila de asignación apilada (`sq-map sq-map--stack`, design-system/components/ButtonMappingRow): la acción arriba y
 * sus entradas debajo, una por línea, o [sinBoton] si no tiene ninguna. Con [alTocar] es un enlace con chevron; sin él,
 * solo informativa (`is-info`), sin estado táctil.
 */
@Composable
fun ButtonMappingRow(
    accion: String,
    entradas: List<EntradaUi>,
    capa: Capa,
    modifier: Modifier = Modifier,
    sinBoton: String? = null,
    alTocar: (() -> Unit)? = null,
) {
    val c = SqTema.colores
    Row(
        modifier
            .fillMaxWidth()
            .heightIn(min = SqTema.tamanos.touchMin)
            .background(c.surface)
            .then(if (alTocar != null) Modifier.clickable(role = Role.Button, onClick = alTocar) else Modifier)
            .semantics(mergeDescendants = true) {}
            .padding(horizontal = SqTema.espacios.space4, vertical = SqTema.espacios.space3),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SqTema.espacios.space3),
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(SqTema.espacios.space2)) {
            Text(accion, style = SqTema.tipografia.body.copy(fontWeight = FontWeight(600)), color = c.ink)
            if (entradas.isEmpty()) {
                sinBoton?.let { Text(it, style = SqTema.tipografia.body, color = c.inkMuted) }
            }
            entradas.forEach { EntradaMapa(it) }
        }
        CapaInsignia(capa)
        if (alTocar != null) {
            Icon(
                painterResource(R.drawable.ic_chevron_right),
                contentDescription = null,
                tint = c.inkMuted,
                modifier = Modifier.offset(x = SqTema.espacios.space2),
            )
        }
    }
}

/** Teclas de una entrada sin partir (`.combo`) y el gesto detrás; si no cabe el gesto, pasa a la línea siguiente. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EntradaMapa(entrada: EntradaUi, modifier: Modifier = Modifier, teclaGrande: Boolean = false) {
    val c = SqTema.colores
    FlowRow(
        modifier,
        horizontalArrangement = Arrangement.spacedBy(SqTema.espacios.space2),
        verticalArrangement = Arrangement.spacedBy(SqTema.espacios.space2),
        itemVerticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(SqTema.espacios.space2)) {
            entrada.teclas.forEachIndexed { i, t ->
                if (i > 0) Text(entrada.separador, color = c.inkMuted, style = SqTema.tipografia.body.copy(fontWeight = FontWeight(600)))
                SquareKey(t, ancha = t.length > 1)
            }
        }
        Text(entrada.gesto, color = c.inkMuted, style = SqTema.tipografia.body.copy(fontSize = 13.sp, lineHeight = 18.sp))
    }
}

/** Etiqueta de capa (`sq-layer`): ZWIFT en `surface-2`/`ink`, APP en `gear-soft`/`gear`. */
@Composable
fun CapaInsignia(capa: Capa, modifier: Modifier = Modifier) {
    val c = SqTema.colores
    Box(
        modifier
            .height(22.dp)
            .background(if (capa == Capa.App) c.gearSoft else c.surface2, RoundedCornerShape(SqTema.radios.radiusSm))
            .padding(horizontal = SqTema.espacios.space2),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            if (capa == Capa.App) "APP" else "ZWIFT",
            color = if (capa == Capa.App) c.gear else c.ink,
            style = SqTema.tipografia.body.copy(fontSize = 12.sp, lineHeight = 12.sp, fontWeight = FontWeight(600), letterSpacing = 0.04.em),
        )
    }
}
