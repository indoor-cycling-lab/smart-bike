package app.smartbike.dominio

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * La orientación que elige el usuario en Ajustes > Pantalla, para toda la app (design.md del change
 * `orientacion-pantalla`, decisión 1). «Según el móvil» gira con el móvil y respeta el bloqueo de rotación del sistema.
 * Los nombres serializados son estables.
 */
@Serializable
enum class OrientacionApp {
    @SerialName("movil")
    SEGUN_MOVIL,

    @SerialName("vertical")
    VERTICAL,

    @SerialName("horizontal")
    HORIZONTAL,
}
