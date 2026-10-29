package app.smartbike.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import app.smartbike.dominio.IdiomaApp
import app.smartbike.ui.tema.IdiomaElegido

/**
 * Una regla de Compose que pinta el contenido con la interfaz en español, sea cual sea el idioma del móvil de pruebas
 * (design.md del change `idioma-es-en`, decisión 9): los tests buscan los textos en español.
 */
fun createComposeRuleEs(): ComposeContentTestRule = EnEspanol(createComposeRule())

private class EnEspanol(private val regla: ComposeContentTestRule) : ComposeContentTestRule by regla {
    override fun setContent(composable: @Composable () -> Unit) = regla.setContent { IdiomaElegido(IdiomaApp.ES, composable) }
}
