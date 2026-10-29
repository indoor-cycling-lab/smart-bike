package app.smartbike.ui.navegacion

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.smartbike.ui.componentes.BottomNav
import app.smartbike.ui.tema.SqTema

/**
 * Pantalla de pestaña (Dispositivos, Botones, Ajustes): el contenido se desplaza y la barra inferior queda siempre al
 * pie (spec `app-navigation`, «Barra inferior»).
 */
@Composable
fun PantallaPestana(
    pestana: Pestana,
    irA: (Pestana) -> Unit,
    modifier: Modifier = Modifier,
    contenido: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier.fillMaxSize()) {
        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
                .verticalScroll(rememberScrollState())
                .padding(start = SqTema.espacios.space4, end = SqTema.espacios.space4, top = SqTema.espacios.space4, bottom = SqTema.espacios.space8),
            verticalArrangement = Arrangement.spacedBy(SqTema.espacios.space6),
            content = contenido,
        )
        BottomNav(pestana, irA)
    }
}
