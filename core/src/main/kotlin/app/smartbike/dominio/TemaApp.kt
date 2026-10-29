package app.smartbike.dominio

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * El tema que elige el usuario en Ajustes > Pantalla, para toda la app (design.md del change `acceso-ajustes-y-tema`,
 * decisión 5). «Oscuro» es el tema `rodaje` del sistema de diseño. Los nombres serializados son estables.
 */
@Serializable
enum class TemaApp {
    @SerialName("oscuro")
    OSCURO,

    @SerialName("claro")
    CLARO,
}
