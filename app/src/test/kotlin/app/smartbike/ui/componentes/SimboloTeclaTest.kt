package app.smartbike.ui.componentes

import app.smartbike.R
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

/** Spec `ui-components`, «Teclas del Square»: símbolos y cruceta con icono; letras y nombres, con texto. */
class SimboloTeclaTest {

    @Test
    fun cada_glifo_de_simbolo_y_cruceta_tiene_su_icono_y_su_descripcion() {
        assertEquals(R.drawable.ic_tecla_triangulo, SimboloTecla.de("△")?.icono)
        assertEquals(R.string.tecla_triangulo, SimboloTecla.de("△")?.descripcion)
        assertEquals(R.drawable.ic_tecla_equis, SimboloTecla.de("✕")?.icono)
        assertEquals(R.string.tecla_arriba, SimboloTecla.de("↑")?.descripcion)
        assertEquals(R.string.tecla_izquierda, SimboloTecla.de("←")?.descripcion)
    }

    @Test
    fun los_ocho_simbolos_son_distintos() {
        assertEquals(8, SimboloTecla.entries.map { it.icono }.toSet().size)
        assertEquals(8, SimboloTecla.entries.map { it.descripcion }.toSet().size)
        assertEquals(8, SimboloTecla.entries.map { it.glifo }.toSet().size)
    }

    @Test
    fun letras_y_nombres_siguen_como_texto() {
        listOf("A", "B", "Y", "Z", "Palanca D1", "Freno I", "Campag. D").forEach { assertNull(SimboloTecla.de(it), it) }
    }
}
