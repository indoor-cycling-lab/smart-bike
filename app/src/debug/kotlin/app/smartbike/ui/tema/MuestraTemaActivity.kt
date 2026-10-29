package app.smartbike.ui.tema

import android.graphics.Color as ColorAndroid
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp

/**
 * Muestra del tema, solo en depuración: reproduce las secciones «Color» y «Tipografía» de la guía de estilo
 * (design-system/styleguide/) en los dos temas para comparar la app con la guía en el móvil.
 *
 *     adb shell am start -n app.smartbike.debug/app.smartbike.ui.tema.MuestraTemaActivity
 */
class MuestraTemaActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(ColorAndroid.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(ColorAndroid.TRANSPARENT),
        )
        super.onCreate(savedInstanceState)
        setContent { MuestraTema() }
    }
}

@Composable
fun MuestraTema() {
    SqTema(modifier = Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                // Fuera del desplazamiento: el contenido no pasa por debajo de las barras del sistema.
                .safeDrawingPadding()
                .testTag("muestra")
                .verticalScroll(rememberScrollState()),
        ) {
            for ((tema, etiqueta) in listOf(Tema.Rodaje to "Rodaje", Tema.Claro to "Claro")) {
                SqTema(tema, Modifier.fillMaxWidth()) { SeccionTema(tema, etiqueta) }
            }
        }
    }
}

@Composable
private fun SeccionTema(tema: Tema, etiqueta: String) {
    val colores = SqTema.colores
    val espacios = SqTema.espacios
    Column(
        Modifier.padding(horizontal = espacios.space4, vertical = espacios.space6),
        verticalArrangement = Arrangement.spacedBy(espacios.space4),
    ) {
        Text(etiqueta.uppercase(), style = SqTema.tipografia.label, color = colores.inkMuted)

        Text("Color", style = SqTema.tipografia.title)
        Column(verticalArrangement = Arrangement.spacedBy(espacios.space3)) {
            for ((nombre, color) in colores.porNombre()) MuestraColor(nombre, color)
        }

        Text("Tipografía", style = SqTema.tipografia.title)
        for ((nombre, estilo) in SqTema.tipografia.porNombre()) {
            MuestraEstilo(tema, nombre, estilo, SqTema.tipografia.ejemplos.getValue(nombre))
        }
    }
}

@Composable
private fun MuestraColor(nombre: String, color: Color) {
    val colores = SqTema.colores
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(SqTema.tamanos.touchMin)
                .border(1.dp, colores.line, RoundedCornerShape(SqTema.radios.radiusMd)) // como .sg-chip en la guía
                .background(color, RoundedCornerShape(SqTema.radios.radiusMd)),
        )
        Column(Modifier.padding(start = SqTema.espacios.space3)) {
            Text(nombre, style = SqTema.tipografia.mono)
            Text("#%06X".format(color.toArgb() and 0xFFFFFF), style = SqTema.tipografia.caption, color = colores.inkMuted)
        }
    }
}

@Composable
private fun MuestraEstilo(tema: Tema, nombre: String, estilo: TextStyle, ejemplo: String) {
    val colores = SqTema.colores
    Column(
        Modifier
            .fillMaxWidth()
            .padding(vertical = SqTema.espacios.space2),
        verticalArrangement = Arrangement.spacedBy(SqTema.espacios.space1),
    ) {
        Text(
            "$nombre · ${estilo.fontSize.entero()}/${estilo.lineHeight.entero()}",
            style = SqTema.tipografia.mono,
            color = colores.inkMuted,
        )
        // Los estilos grandes no caben en vertical: se desplazan en horizontal sin reducirlos.
        Box(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
            Text(ejemplo, style = estilo, softWrap = false, modifier = Modifier.testTag(etiquetaEjemplo(tema, nombre)))
        }
    }
}

/** Etiqueta de test del ejemplo de un estilo en un tema: `ejemplo-rodaje-gear-hero`. */
fun etiquetaEjemplo(tema: Tema, nombre: String) = "ejemplo-${tema.name.lowercase()}-$nombre"

private fun TextUnit.entero() = value.toInt().toString()
