package app.smartbike.ui.datos

import androidx.compose.ui.unit.dp
import app.smartbike.dominio.Configuracion
import app.smartbike.dominio.DatoRodaje
import app.smartbike.dominio.DatoRodaje.CADENCIA
import app.smartbike.dominio.DatoRodaje.DISTANCIA
import app.smartbike.dominio.DatoRodaje.PENDIENTE
import app.smartbike.dominio.DatoRodaje.POTENCIA
import app.smartbike.dominio.DatoRodaje.POTENCIA_MAXIMA
import app.smartbike.dominio.DatoRodaje.POTENCIA_MEDIA
import app.smartbike.dominio.DatoRodaje.TIEMPO
import app.smartbike.dominio.DatoRodaje.VELOCIDAD
import app.smartbike.dominio.DatoRodaje.W_KG
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/** Spec `settings-screen`, «Pantalla Datos en pantalla», sobre el estado y sus funciones. */
class DatosUiStateTest {

    @Test
    fun por_defecto() {
        val e = estadoDeDatos(Configuracion())
        assertEquals(listOf(POTENCIA, CADENCIA), e.elegidos)
        assertEquals(DatoRodaje.entries.size, e.filas.size)
        assertEquals(listOf(POTENCIA, CADENCIA), e.filas.take(2))
    }

    @Test
    fun anadir_un_dato_va_al_final() {
        assertEquals(listOf(POTENCIA, CADENCIA, TIEMPO), alternarDato(listOf(POTENCIA, CADENCIA), TIEMPO))
    }

    @Test
    fun quitar_un_dato() {
        assertEquals(listOf(CADENCIA), alternarDato(listOf(POTENCIA, CADENCIA), POTENCIA))
    }

    @Test
    fun al_menos_uno() {
        assertEquals(listOf(POTENCIA), alternarDato(listOf(POTENCIA), POTENCIA))
        val e = DatosUiState(listOf(POTENCIA))
        assertTrue(e.bloqueado(POTENCIA))
        assertFalse(e.bloqueado(CADENCIA))
    }

    @Test
    fun todos_los_datos() {
        val todos = listOf(POTENCIA, CADENCIA, TIEMPO, W_KG, POTENCIA_MEDIA, POTENCIA_MAXIMA)
        assertEquals(todos, todos.fold(listOf(POTENCIA)) { l, d -> if (d in l) l else alternarDato(l, d) })
    }

    @Test
    fun nueve_datos_y_maximo_de_seis() {
        val seis = listOf(POTENCIA, CADENCIA, TIEMPO, W_KG, POTENCIA_MEDIA, POTENCIA_MAXIMA)
        assertEquals(9, estadoDeDatos(Configuracion()).filas.size)
        // El séptimo no se añade; las casillas de los dos sin marcar quedan deshabilitadas con su ayuda.
        assertEquals(seis, alternarDato(seis, VELOCIDAD))
        val e = DatosUiState(seis)
        assertTrue(e.sinSitio(VELOCIDAD) && e.sinSitio(DISTANCIA) && e.sinSitio(PENDIENTE))
        assertFalse(e.sinSitio(POTENCIA))
        // Quitando uno, se puede añadir otro.
        assertFalse(DatosUiState(seis - TIEMPO).sinSitio(VELOCIDAD))
        assertEquals(seis - TIEMPO + VELOCIDAD, alternarDato(seis - TIEMPO, VELOCIDAD))
    }

    @Test
    fun ordenar() {
        // «Ordenar»: cadencia por encima de potencia; «Ordenar sin arrastrar»: un puesto cada vez.
        assertEquals(listOf(CADENCIA, POTENCIA), moverDato(listOf(POTENCIA, CADENCIA), CADENCIA, -1))
        assertEquals(listOf(CADENCIA, TIEMPO, POTENCIA), moverDato(listOf(POTENCIA, CADENCIA, TIEMPO), POTENCIA, 5))
        assertEquals(listOf(POTENCIA, CADENCIA), moverDato(listOf(POTENCIA, CADENCIA), POTENCIA, -1))
        assertEquals(listOf(POTENCIA), moverDato(listOf(POTENCIA), TIEMPO, 1))
    }

    @Test
    fun resumen_unido_con_y() {
        assertEquals("Potencia", unirConY(listOf("Potencia"), "y"))
        assertEquals("Potencia y cadencia", unirConY(listOf("Potencia", "cadencia"), "y"))
        assertEquals("Tiempo, potencia y W/kg", unirConY(listOf("Tiempo", "potencia", "W/kg"), "y"))
    }

    @Test
    fun plantillas_tan_largas_como_los_valores_reales() {
        // README de `MetricGrid`: la cifra se mide con la plantilla, que no puede ser más corta que un valor real largo.
        val largos = mapOf(
            POTENCIA to "1000",
            POTENCIA_MEDIA to "1000",
            POTENCIA_MAXIMA to "1999",
            CADENCIA to "120",
            TIEMPO to "1:12:40",
            W_KG to "12,4",
            VELOCIDAD to "99,9",
            DISTANCIA to "999,99",
            PENDIENTE to "−12,5",
        )
        DatoRodaje.entries.forEach { d ->
            val p = plantillaDato(d)
            assertTrue(p.length >= largos.getValue(d).length, "$d: «$p» más corta que «${largos.getValue(d)}»")
        }
    }

    private val seis = listOf(POTENCIA, CADENCIA, TIEMPO, W_KG, POTENCIA_MEDIA, POTENCIA_MAXIMA)

    @Test
    fun el_estado_lleva_los_botones_de_la_configuracion() {
        assertFalse(estadoDeDatos(Configuracion()).botones)
        assertTrue(estadoDeDatos(Configuracion(botonesEnPantalla = true)).botones)
    }

    @Test
    fun ocultos_con_seis_datos_botones_y_pantalla_baja_son_el_quinto_y_el_sexto() {
        val e = estadoDeDatos(Configuracion(datos = seis, botonesEnPantalla = true))
        assertEquals(listOf(POTENCIA_MEDIA, POTENCIA_MAXIMA), e.ocultos(640.dp))
        assertEquals(listOf(POTENCIA_MEDIA, POTENCIA_MAXIMA), e.ocultos(759.dp))
    }

    @Test
    fun con_cinco_datos_se_oculta_solo_el_quinto() {
        val e = estadoDeDatos(Configuracion(datos = seis.take(5), botonesEnPantalla = true))
        assertEquals(listOf(POTENCIA_MEDIA), e.ocultos(640.dp))
    }

    @Test
    fun sin_botones_con_cuatro_datos_o_en_pantalla_alta_no_se_oculta_nada() {
        assertTrue(estadoDeDatos(Configuracion(datos = seis, botonesEnPantalla = false)).ocultos(640.dp).isEmpty())
        assertTrue(estadoDeDatos(Configuracion(datos = seis.take(4), botonesEnPantalla = true)).ocultos(640.dp).isEmpty())
        assertTrue(estadoDeDatos(Configuracion(datos = seis, botonesEnPantalla = true)).ocultos(760.dp).isEmpty())
        assertTrue(estadoDeDatos(Configuracion(datos = seis, botonesEnPantalla = true)).ocultos(844.dp).isEmpty())
    }
}
