// Generado por scripts/design-system/generar.py a partir de design-system/tokens.json. No editar a mano:
// cambia tokens.json y ejecuta python3 scripts/design-system/generar.py. TokensSincronizadosTest
// falla si este fichero no coincide con tokens.json.
package app.smartbike.ui.tema

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import app.smartbike.R

/** Colores de un tema. Los valores de cada tema están en [ColoresRodaje] y [ColoresClaro]. */
data class SqColores(
    /** Fondo de todas las pantallas. Rodaje es el tema por defecto: cuarto de entrenamiento con poca luz. */
    val bg: Color,
    /** Paneles y filas de ajustes sobre bg. */
    val surface: Color,
    /** Estado pulsado, pistas de barras y teclas de botón del Square. */
    val surface2: Color,
    /** Separadores finos. Solo decorativo: no delimita controles. */
    val line: Color,
    /** Borde de controles (botones secundarios, steppers, chips). ≥3:1 sobre bg y surface en ambos temas. */
    val lineStrong: Color,
    /** Texto y cifras principales sobre bg, surface y surface-2. */
    val ink: Color,
    /** Etiquetas de métricas, unidades y texto secundario sobre bg, surface y surface-2. También pendiente llana (−0,5 % a 0,5 %) en Diagnóstico. */
    val inkMuted: Color,
    /** Color de la marcha: el número de marcha, el piñón activo y los cambios. Como texto, sobre bg y surface. Es el único color de marca: solo para la marcha, la acción principal y lo elegido o activo (foco, pestaña, opción, tecla pulsada, captura, capa App). */
    val gear: Color,
    /** Relleno de la marcha activa en la escalera de marchas y del botón principal. */
    val gearFill: Color,
    /** Texto e iconos sobre gear-fill (10:1). Nunca blanco. */
    val onGearFill: Color,
    /** Fondo tenue detrás de texto gear: aviso de tope, etiqueta de capa App. */
    val gearSoft: Color,
    /** Pendiente positiva (subida) en texto y flechas, sobre bg y surface. En Diagnóstico y en el dato opcional «Pendiente» de la rejilla de Rodaje. */
    val climb: Color,
    /** Pendiente negativa (bajada) en texto y flechas, sobre bg y surface. En Diagnóstico y en el dato opcional «Pendiente» de la rejilla de Rodaje. */
    val descent: Color,
    /** Modo SIM (pendiente) en la insignia de modo, sobre bg y surface. */
    val sim: Color,
    /** Modo ERG (potencia fija, manual o de Zwift) en la insignia de modo y la potencia objetivo, sobre bg y surface. */
    val erg: Color,
    /** Fondo tenue detrás de texto erg: ajuste de las palancas sobre la potencia de Zwift en ERG. */
    val ergSoft: Color,
    /** Dispositivo conectado. Siempre acompañado de punto lleno y palabra; nunca solo color. */
    val ok: Color,
    /** Desconectado, error o cadencia peligrosamente baja en ERG. Siempre con icono o palabra: no se distingue de ok solo por luminosidad. */
    val danger: Color,
    /** Anillo de foco (teclado, mando D-pad): 3px sólido con 2px de separación en bg. */
    val focus: Color,
) {
    /** Cada color por su nombre en tokens.json, leído de las propiedades. */
    fun porNombre(): Map<String, Color> = linkedMapOf(
        "bg" to bg,
        "surface" to surface,
        "surface-2" to surface2,
        "line" to line,
        "line-strong" to lineStrong,
        "ink" to ink,
        "ink-muted" to inkMuted,
        "gear" to gear,
        "gear-fill" to gearFill,
        "on-gear-fill" to onGearFill,
        "gear-soft" to gearSoft,
        "climb" to climb,
        "descent" to descent,
        "sim" to sim,
        "erg" to erg,
        "erg-soft" to ergSoft,
        "ok" to ok,
        "danger" to danger,
        "focus" to focus,
    )
}

/** Tema Rodaje (oscuro). */
val ColoresRodaje = SqColores(
    bg = Color(0xFF0F1216),
    surface = Color(0xFF181C22),
    surface2 = Color(0xFF222831),
    line = Color(0xFF2D343E),
    lineStrong = Color(0xFF5F6976),
    ink = Color(0xFFEEF2F6),
    inkMuted = Color(0xFF9AA5B1),
    gear = Color(0xFFFFB21F),
    gearFill = Color(0xFFFFB21F),
    onGearFill = Color(0xFF0F1216),
    gearSoft = Color(0xFF3A2A0C),
    climb = Color(0xFFFF7D5C),
    descent = Color(0xFF5CB0FF),
    sim = Color(0xFF4FD1C5),
    erg = Color(0xFFC09BFF),
    ergSoft = Color(0xFF2F2B40),
    ok = Color(0xFF4ADE80),
    danger = Color(0xFFFF6B6B),
    focus = Color(0xFFFFB21F),
)

/** Tema Claro. */
val ColoresClaro = SqColores(
    bg = Color(0xFFF2F3F5),
    surface = Color(0xFFFFFFFF),
    surface2 = Color(0xFFE7E9ED),
    line = Color(0xFFD5D9DF),
    lineStrong = Color(0xFF7C8591),
    ink = Color(0xFF12161B),
    inkMuted = Color(0xFF56606B),
    gear = Color(0xFF8F5400),
    gearFill = Color(0xFFFFB21F),
    onGearFill = Color(0xFF0F1216),
    gearSoft = Color(0xFFFFEFD2),
    climb = Color(0xFFB23A1C),
    descent = Color(0xFF1F5FAE),
    sim = Color(0xFF0F766E),
    erg = Color(0xFF6A3FC0),
    ergSoft = Color(0xFFDAD3EB),
    ok = Color(0xFF1E7A43),
    danger = Color(0xFFC22A2A),
    focus = Color(0xFF8F5400),
)

/** Familias tipográficas, empaquetadas en res/font. */
object SqFamilias {
    /** Barlow Condensed */
    val numeric = FontFamily(Font(R.font.barlow_condensed_bold, FontWeight(700)), Font(R.font.barlow_condensed_medium, FontWeight(500)), Font(R.font.barlow_condensed_semibold, FontWeight(600)))
    /** Barlow */
    val sans = FontFamily(Font(R.font.barlow_regular, FontWeight(400)), Font(R.font.barlow_semibold, FontWeight(600)))
    /** JetBrains Mono */
    val mono = FontFamily(Font(R.font.jet_brains_mono_regular, FontWeight(400)))

    fun porNombre(): Map<String, FontFamily> = linkedMapOf(
        "numeric" to numeric,
        "sans" to sans,
        "mono" to mono,
    )
}

// Interlineado repartido arriba y abajo y sin relleno de fuente, como en CSS: la caja de una línea mide su
// interlineado. Si es mayor que el alto natural de la fuente, Trim.None reparte el espacio que sobra; si es
// menor (las cifras grandes), Compose no encoge la línea salvo con Trim.Both y Mode.Tight.
private val InterlineadoCss = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.None, LineHeightStyle.Mode.Fixed)
private val InterlineadoCssAjustado = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.Both, LineHeightStyle.Mode.Tight)
private val SinRellenoDeFuente = PlatformTextStyle(includeFontPadding = false)

/** Estilos tipográficos. La mayúscula de `label` la aplica quien lo usa, como text-transform en CSS. */
object SqTipografia {
    /** Número de marcha en la pantalla de rodaje con transmisión lineal. Uno por pantalla. */
    val gearHero = TextStyle(
        fontFamily = SqFamilias.numeric,
        fontWeight = FontWeight(700),
        fontSize = 168.sp,
        lineHeight = 152.sp,
        letterSpacing = (-0.02).em,
        fontFeatureSettings = "tnum",
        lineHeightStyle = InterlineadoCssAjustado,
        platformStyle = SinRellenoDeFuente,
    )
    /** Plato × piñón en la pantalla de rodaje con transmisión física. Sustituye a gear-hero; el signo × va en metric-xl y ink-muted. */
    val gearRatioHero = TextStyle(
        fontFamily = SqFamilias.numeric,
        fontWeight = FontWeight(700),
        fontSize = 136.sp,
        lineHeight = 120.sp,
        letterSpacing = (-0.02).em,
        fontFeatureSettings = "tnum",
        lineHeightStyle = InterlineadoCssAjustado,
        platformStyle = SinRellenoDeFuente,
    )
    /** Potencia en la pantalla de rodaje. */
    val metricXl = TextStyle(
        fontFamily = SqFamilias.numeric,
        fontWeight = FontWeight(600),
        fontSize = 72.sp,
        lineHeight = 72.sp,
        fontFeatureSettings = "tnum",
        lineHeightStyle = InterlineadoCssAjustado,
        platformStyle = SinRellenoDeFuente,
    )
    /** Cadencia y potencia objetivo; en Diagnóstico, pendientes. */
    val metricLg = TextStyle(
        fontFamily = SqFamilias.numeric,
        fontWeight = FontWeight(600),
        fontSize = 44.sp,
        lineHeight = 48.sp,
        fontFeatureSettings = "tnum",
        lineHeightStyle = InterlineadoCssAjustado,
        platformStyle = SinRellenoDeFuente,
    )
    /** Relación de marcha, valores de steppers en ajustes. */
    val metricMd = TextStyle(
        fontFamily = SqFamilias.numeric,
        fontWeight = FontWeight(600),
        fontSize = 28.sp,
        lineHeight = 32.sp,
        fontFeatureSettings = "tnum",
        lineHeightStyle = InterlineadoCssAjustado,
        platformStyle = SinRellenoDeFuente,
    )
    /** Título de pantalla de ajustes. */
    val title = TextStyle(
        fontFamily = SqFamilias.sans,
        fontWeight = FontWeight(600),
        fontSize = 24.sp,
        lineHeight = 32.sp,
        lineHeightStyle = InterlineadoCss,
        platformStyle = SinRellenoDeFuente,
    )
    /** Encabezado de sección. */
    val heading = TextStyle(
        fontFamily = SqFamilias.sans,
        fontWeight = FontWeight(600),
        fontSize = 18.sp,
        lineHeight = 24.sp,
        lineHeightStyle = InterlineadoCss,
        platformStyle = SinRellenoDeFuente,
    )
    /** Texto de ajustes y descripciones. */
    val body = TextStyle(
        fontFamily = SqFamilias.sans,
        fontWeight = FontWeight(400),
        fontSize = 16.sp,
        lineHeight = 24.sp,
        lineHeightStyle = InterlineadoCss,
        platformStyle = SinRellenoDeFuente,
    )
    /** Etiqueta de métrica, en mayúsculas, color ink-muted. */
    val label = TextStyle(
        fontFamily = SqFamilias.sans,
        fontWeight = FontWeight(600),
        fontSize = 13.sp,
        lineHeight = 16.sp,
        letterSpacing = (0.08).em,
        lineHeightStyle = InterlineadoCss,
        platformStyle = SinRellenoDeFuente,
    )
    /** Metadatos y ayudas bajo un control. */
    val caption = TextStyle(
        fontFamily = SqFamilias.sans,
        fontWeight = FontWeight(400),
        fontSize = 13.sp,
        lineHeight = 18.sp,
        lineHeightStyle = InterlineadoCss,
        platformStyle = SinRellenoDeFuente,
    )
    /** Volcados hexadecimales y UUID en la pantalla de diagnóstico. */
    val mono = TextStyle(
        fontFamily = SqFamilias.mono,
        fontWeight = FontWeight(400),
        fontSize = 13.sp,
        lineHeight = 20.sp,
        lineHeightStyle = InterlineadoCss,
        platformStyle = SinRellenoDeFuente,
    )

    fun porNombre(): Map<String, TextStyle> = linkedMapOf(
        "gear-hero" to gearHero,
        "gear-ratio-hero" to gearRatioHero,
        "metric-xl" to metricXl,
        "metric-lg" to metricLg,
        "metric-md" to metricMd,
        "title" to title,
        "heading" to heading,
        "body" to body,
        "label" to label,
        "caption" to caption,
        "mono" to mono,
    )

    /** Texto de ejemplo de cada estilo, el mismo que muestra la guía. */
    val ejemplos: Map<String, String> = linkedMapOf(
        "gear-hero" to "14",
        "gear-ratio-hero" to "50×17",
        "metric-xl" to "247",
        "metric-lg" to "+6,2 %",
        "metric-md" to "50×17",
        "title" to "Asignación de botones",
        "heading" to "Funciones de Zwift",
        "body" to "Mantén pulsada la maneta para frenar en Zwift.",
        "label" to "CADENCIA",
        "caption" to "Última conexión hace 2 min",
        "mono" to "a4 09 4e 05 33 ff ff ff ff 4c 4f ff 9c",
    )
}

/** Espaciados en dp. Base de 4dp. La pantalla de rodaje usa los pasos grandes; los ajustes, los pequeños. */
object SqEspacios {
    /** Icono–texto, punto de estado–palabra. */
    val space1 = 4.dp
    /** Dentro de chips e insignias. */
    val space2 = 8.dp
    /** Entre filas relacionadas, etiqueta–cifra. */
    val space3 = 12.dp
    /** Relleno de paneles y margen lateral en ajustes. */
    val space4 = 16.dp
    /** Entre secciones de ajustes; margen lateral de la pantalla de rodaje. */
    val space6 = 24.dp
    /** Entre zonas de la pantalla de rodaje (marcha, métricas, estado). */
    val space8 = 32.dp
    /** Aire alrededor del número de marcha. */
    val space12 = 48.dp

    fun porNombre(): Map<String, Dp> = linkedMapOf(
        "space-1" to space1,
        "space-2" to space2,
        "space-3" to space3,
        "space-4" to space4,
        "space-6" to space6,
        "space-8" to space8,
        "space-12" to space12,
    )
}

/** Radios en dp. Formas de taller: esquinas firmes y discos de piñón. Nada de tarjetas muy redondeadas. */
object SqRadios {
    /** Segmentos de la escalera de marchas, insignias de modo. */
    val radiusSm = 4.dp
    /** Botones, paneles, teclas de botón del Square. */
    val radiusMd = 8.dp
    /** Chips de conexión y piñones (discos). */
    val radiusFull = 9999.dp

    fun porNombre(): Map<String, Dp> = linkedMapOf(
        "radius-sm" to radiusSm,
        "radius-md" to radiusMd,
        "radius-full" to radiusFull,
    )
}

/** Tamaños en dp. Tamaños de interacción en dp. */
object SqTamanos {
    /** Objetivo táctil mínimo en ajustes (Material). */
    val touchMin = 48.dp
    /** Botones táctiles de la pantalla de rodaje, para tocar con guantes o en marcha. */
    val touchRide = 72.dp
    /** Altura de la escalera de marchas. */
    val ladderH = 28.dp
    /** Grosor del anillo de foco. */
    val focusWidth = 3.dp

    fun porNombre(): Map<String, Dp> = linkedMapOf(
        "touch-min" to touchMin,
        "touch-ride" to touchRide,
        "ladder-h" to ladderH,
        "focus-width" to focusWidth,
    )
}
