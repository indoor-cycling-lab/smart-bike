package app.smartbike.ui.tema

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.smartbike.dominio.TemaApp

/** El tema del sistema de diseño de cada opción de Ajustes > Pantalla: «Oscuro» es el tema rodaje. */
fun TemaApp.comoTema(): Tema = when (this) {
    TemaApp.OSCURO -> Tema.Rodaje
    TemaApp.CLARO -> Tema.Claro
}

/** Los colores de [tema], para lo que se pinta fuera de Compose (el fondo de la ventana). */
fun coloresDe(tema: Tema): SqColores = when (tema) {
    Tema.Rodaje -> ColoresRodaje
    Tema.Claro -> ColoresClaro
}

/**
 * La app entera con el tema elegido en Ajustes (spec `app-theme`). Mientras [tema] es `null` (la configuración aún no
 * se ha leído) no compone nada: así ninguna pantalla aparece en un tema para cambiar al otro al instante (design.md del
 * change `acceso-ajustes-y-tema`, decisión 7).
 */
@Composable
fun SqTemaElegido(tema: TemaApp?, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    if (tema == null) return
    SqTema(tema.comoTema(), modifier, content)
}
