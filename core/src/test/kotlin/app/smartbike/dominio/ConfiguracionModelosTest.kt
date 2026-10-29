package app.smartbike.dominio

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/** Spec `resistance-models`, «Configuración anterior a los modelos», y `settings-screen`, «Datos en pantalla». */
class ConfiguracionModelosTest {
    @Test
    fun por_defecto() {
        val c = Configuracion()
        assertEquals(TipoModelo.SIMPLE, c.modelo)
        assertEquals(100, c.dificultadPct)
        assertEquals(30, c.potenciaMinimaW)
        assertEquals(8.0, c.pesoBiciKg, 0.0)
        assertTrue(c.tablas.isEmpty())
    }

    @Test
    fun documento_anterior_se_lee_con_los_valores_por_defecto() {
        val c = Configuracion.deJson("""{"version":5,"pesoCiclistaKg":80,"bajadas":"nunca-empujar"}""")
        assertEquals(80, c.pesoCiclistaKg)
        assertEquals(TipoModelo.SIMPLE, c.modelo)
        assertEquals(100, c.dificultadPct)
    }

    @Test
    fun valores_fuera_de_limites_se_recortan() {
        val c = Configuracion.deJson("""{"dificultadPct":300,"potenciaMinimaW":-5,"pesoBiciKg":99}""")
        assertEquals(150, c.dificultadPct)
        assertEquals(0, c.potenciaMinimaW)
        assertEquals(25.0, c.pesoBiciKg, 0.0)
    }

    @Test
    fun tabla_con_el_numero_equivocado_de_marchas_se_descarta_y_las_demas_se_conservan() {
        val buena = List(24) { 1.0 }
        val c = Configuracion.deJson(
            """{"tablas":{"lineal":[1.0,2.0],"shimano-105":$buena}}""",
        )
        assertFalse("lineal" in c.tablas)
        assertEquals(buena, c.tablas["shimano-105"])
        assertEquals(ModeloSimple(1.0, 12).tablaPorDefecto(), c.tabla(Transmision.Lineal()))
    }

    @Test
    fun tabla_editada_se_guarda_y_restablece() {
        val t = Transmision.Lineal()
        val editada = Configuracion().tabla(t).toMutableList().also { it[16] = 7.0 }
        val c = Configuracion().conTabla(t, editada)
        assertEquals(7.0, Configuracion.deJson(Configuracion.aJson(c)).tabla(t)[16], 0.0)
        assertEquals(5.0, c.sinTabla(t).tabla(t)[16], 1e-9)
    }

    @Test
    fun una_tabla_por_transmision() {
        val c = Configuracion().conTabla(Transmision.Lineal(), List(24) { 3.0 })
        assertEquals(ModeloSimple(1.0, 12, Desarrollos.SHIMANO_105).tablaPorDefecto(), c.tabla(Desarrollos.SHIMANO_105))
    }

    @Test
    fun datos_nuevos_y_desconocidos() {
        val c = Configuracion.deJson("""{"datos":["potencia","velocidad","distancia","inventado"]}""")
        assertEquals(listOf(DatoRodaje.POTENCIA, DatoRodaje.VELOCIDAD, DatoRodaje.DISTANCIA), c.datos)
    }

    @Test
    fun siguiente_modelo_recorre_los_tres() {
        assertEquals(TipoModelo.TABLA, TipoModelo.SIMPLE.siguiente())
        assertEquals(TipoModelo.FISICO, TipoModelo.TABLA.siguiente())
        assertEquals(TipoModelo.SIMPLE, TipoModelo.FISICO.siguiente())
    }

    @Test
    fun siguiente_modelo_es_asignable_tras_pantalla_siguiente() {
        listOf(Transmision.Lineal(), Desarrollos.SHIMANO_105).forEach {
            val a = Accion.asignables(it)
            assertEquals(Accion.SiguienteModelo, a[a.indexOf(Accion.PantallaSiguiente) + 1])
        }
        assertTrue(Accion.SiguienteModelo in Accion.TODAS)
        assertTrue(Configuracion().asignacion.reglas.none { it.accion == Accion.SiguienteModelo })
    }
}
