package app.smartbike.dominio

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * El idioma de la interfaz que elige el usuario en Ajustes > Pantalla (design.md del change `idioma-es-en`, decisión 1).
 * Los nombres serializados son estables y coinciden con el código de idioma.
 */
@Serializable
enum class IdiomaApp(val codigo: String) {
    @SerialName("es")
    ES("es"),

    @SerialName("en")
    EN("en"),
    ;

    companion object {
        /** El idioma del primer arranque: español si el del móvil lo es (de cualquier región) e inglés en cualquier otro caso. */
        fun deLenguaje(codigo: String): IdiomaApp = if (codigo.substringBefore('-').substringBefore('_').lowercase() == "es") ES else EN
    }
}
