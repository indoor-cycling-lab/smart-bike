package app.smartbike.datos

import app.smartbike.dominio.IdiomaApp
import kotlinx.coroutines.flow.first

/**
 * Fija el idioma del primer arranque (spec `app-language`, «Idioma del primer arranque»): si la configuración no trae
 * idioma —instalación limpia o ajustes guardados antes del change `idioma-es-en`—, guarda el del móvil, [lenguaje]
 * (`es` o cualquier otro). Con uno ya elegido no hace nada: la app no vuelve a seguir al móvil.
 */
suspend fun fijarIdiomaInicial(ajustes: AlmacenConfiguracion, lenguaje: String) {
    if (ajustes.configuracion.first().idioma != null) return
    ajustes.actualizarConfiguracion { it.copy(idioma = it.idioma ?: IdiomaApp.deLenguaje(lenguaje)) }
}
