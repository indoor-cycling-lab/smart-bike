package app.smartbike.ui.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import app.smartbike.ui.tema.SqTema

/**
 * Potencia objetivo en ERG (zona `sq-ride__erg` de `RideScreen`): ocupa el lugar de la marcha, centrada en su columna.
 * La cifra en `erg` (`sq-erg-hero`), la potencia que pide Zwift debajo (`sq-target`) y, si las palancas la han
 * cambiado, el ajuste (`sq-badge-erg`, `erg` sobre `erg-soft`). En ERG manual, sin [deZwift] ni [ajuste], no hay línea.
 */
@Composable
fun ErgTarget(
    etiqueta: String,
    vatios: Int,
    deZwift: String?,
    ajuste: String?,
    descripcion: String,
    modifier: Modifier = Modifier,
) {
    val c = SqTema.colores
    Column(modifier, verticalArrangement = Arrangement.spacedBy(SqTema.espacios.space3), horizontalAlignment = Alignment.CenterHorizontally) {
        Label(etiqueta)
        Row(
            Modifier.semantics(mergeDescendants = true) { contentDescription = descripcion },
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(SqTema.espacios.space2),
        ) {
            Text(
                vatios.toString(),
                color = c.erg,
                style = SqTema.tipografia.gearHero.copy(fontSize = 152.sp, lineHeight = 136.sp, letterSpacing = (-0.02).em),
                softWrap = false,
                modifier = Modifier.clearAndSetSemantics {},
            )
            Text(
                "W",
                color = c.inkMuted,
                style = TextStyle(fontFamily = SqTema.familias.numeric, fontSize = 24.sp, fontWeight = FontWeight(700)),
                modifier = Modifier.padding(bottom = SqTema.espacios.space3).clearAndSetSemantics {},
            )
        }
        if (deZwift == null && ajuste == null) return@Column
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(SqTema.espacios.space3)) {
            deZwift?.let { Text(it, color = c.erg, style = TextStyle(fontFamily = SqTema.familias.numeric, fontSize = 20.sp, fontWeight = FontWeight(600))) }
            ajuste?.let {
                Text(
                    it,
                    color = c.erg,
                    style = SqTema.tipografia.body.copy(fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight(600)),
                    modifier = Modifier
                        .background(c.ergSoft, RoundedCornerShape(SqTema.radios.radiusSm))
                        .padding(horizontal = SqTema.espacios.space2, vertical = 2.dp),
                )
            }
        }
    }
}
