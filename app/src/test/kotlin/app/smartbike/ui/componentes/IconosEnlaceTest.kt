package app.smartbike.ui.componentes

import app.smartbike.R
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

/** Spec `ui-components`, «Icono de cada enlace» y «Icono de la entrada Botones». */
class IconosEnlaceTest {

    @Test
    fun el_square_lleva_el_manillar() {
        assertEquals(R.drawable.ic_manillar, Enlace.Square.icono)
    }

    @Test
    fun el_controlador_publicado_lleva_el_icono_de_zwift() {
        assertEquals(R.drawable.ic_zwift, Enlace.Mando.icono)
        assertEquals(Enlace.Zwift.icono, Enlace.Mando.icono)
    }

    @Test
    fun los_enlaces_de_la_barra_de_rodaje_tienen_cada_uno_su_icono() {
        val iconos = listOf(Enlace.Neo, Enlace.Square, Enlace.Pulso, Enlace.Ventilador).map { it.icono }
        assertEquals(iconos.size, iconos.toSet().size)
    }

    @Test
    fun el_icono_de_botones_es_distinto_del_manillar() {
        assertNotEquals(R.drawable.ic_botones, Enlace.Square.icono)
    }
}
