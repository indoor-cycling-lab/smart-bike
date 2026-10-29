package app.smartbike.datos

import app.smartbike.dominio.AjustesLineal
import app.smartbike.dominio.Configuracion
import app.smartbike.dominio.ConfiguracionBotones
import app.smartbike.dominio.Lectura
import app.smartbike.dominio.TiemposGestos
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AlmacenTest {

    private val perdidas = mutableListOf<Pair<String, Lectura<Configuracion>>>()
    private val serializador = SerializadorTolerante(Configuracion(), Configuracion::leerGuardada, Configuracion::aJson) { texto, l -> perdidas += texto to l }

    private suspend fun leer(texto: String) = serializador.readFrom(ByteArrayInputStream(texto.toByteArray()))

    @Test
    fun configuracion_danada_da_los_valores_por_defecto_y_lo_cuenta() = runTest {
        assertEquals(Configuracion(), leer("{\"botones\":"))
        assertEquals(Configuracion(), leer(""))
        assertEquals(listOf("{\"botones\":", ""), perdidas.map { it.first })
        assertTrue(perdidas.all { it.second.ilegible })
    }

    @Test
    fun un_campo_danado_se_cuenta_con_el_texto_original() = runTest {
        val texto = """{"version":10,"pesoCiclistaKg":81,"conexion":{"modo":"por-telepatia"}}"""
        assertEquals(81, leer(texto).pesoCiclistaKg)
        assertEquals(texto, perdidas.single().first)
        assertEquals(listOf("conexion"), perdidas.single().second.descartados)
    }

    @Test
    fun configuracion_ida_y_vuelta_sin_perdidas() = runTest {
        val c = Configuracion(lineal = AjustesLineal(referencia = 15), botones = ConfiguracionBotones(tiempos = TiemposGestos(ritmoRepeticionMs = 400)))
        val salida = ByteArrayOutputStream()
        serializador.writeTo(c, salida)
        assertEquals(c, serializador.readFrom(ByteArrayInputStream(salida.toByteArray())))
        assertTrue(perdidas.isEmpty())
    }
}
