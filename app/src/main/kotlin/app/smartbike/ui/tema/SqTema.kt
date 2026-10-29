package app.smartbike.ui.tema

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier

/** Los dos temas del sistema de diseño. Rodaje (oscuro) es el predeterminado. */
enum class Tema { Rodaje, Claro }

private val LocalTema = staticCompositionLocalOf { Tema.Rodaje }
private val LocalSqColores = staticCompositionLocalOf { ColoresRodaje }

/** Acceso a los tokens desde los composables: `SqTema.colores.onGearFill`, `SqTema.tipografia.gearHero`… */
object SqTema {
    val tema: Tema
        @Composable @ReadOnlyComposable
        get() = LocalTema.current

    val colores: SqColores
        @Composable @ReadOnlyComposable
        get() = LocalSqColores.current

    val tipografia = SqTipografia
    val familias = SqFamilias
    val espacios = SqEspacios
    val radios = SqRadios
    val tamanos = SqTamanos
}

/**
 * Aplica un tema a su contenido y pinta el fondo `bg` de ese tema, como `data-theme` en la guía.
 * Se puede anidar: `SqTema(Tema.Claro) { … }` dentro de una pantalla en rodaje cambia solo esa zona.
 */
@Composable
fun SqTema(
    tema: Tema = Tema.Rodaje,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val colores = coloresDe(tema)
    CompositionLocalProvider(LocalTema provides tema, LocalSqColores provides colores) {
        // Material 3 solo como base técnica (ripple, selección de texto, indicadores): las pantallas no leen colorScheme.
        MaterialTheme(colorScheme = esquemaMaterial(tema, colores)) {
            CompositionLocalProvider(
                LocalContentColor provides colores.ink,
                LocalTextStyle provides SqTipografia.body,
            ) {
                Box(modifier.background(colores.bg)) { content() }
            }
        }
    }
}

private fun esquemaMaterial(tema: Tema, c: SqColores): ColorScheme {
    val base = if (tema == Tema.Rodaje) darkColorScheme() else lightColorScheme()
    return base.copy(
        primary = c.gear,
        onPrimary = c.onGearFill,
        primaryContainer = c.gearSoft,
        onPrimaryContainer = c.gear,
        background = c.bg,
        onBackground = c.ink,
        surface = c.surface,
        onSurface = c.ink,
        surfaceVariant = c.surface2,
        onSurfaceVariant = c.inkMuted,
        outline = c.lineStrong,
        outlineVariant = c.line,
        error = c.danger,
    )
}
