package app.smartbike.dominio

import java.time.ZoneOffset
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/** El formato del archivo de sesión y la decisión de espacio (spec `session-recording`). */
class ArchivoSesionTest {

    // 2026-10-29 18:42:07.123 UTC
    private val inicio = 1_793_299_327_123L

    @Test
    fun la_cabecera_lleva_fecha_version_modo_nombre_transmision_y_modelo() {
        val c = Configuracion(
            conexion = AjustesConexion(ModoConexion.FTMS_WIFI, Identidad.KICKR_BIKE_PRO),
            modelo = TipoModelo.FISICO,
        )
        val l = ArchivoSesion.cabecera(inicio, ZoneOffset.UTC, "0.9.0", c)
        assertEquals("# Inicio: 2026-10-29 18:42:07", l[1])
        assertEquals("# Versión de la app: 0.9.0", l[2])
        assertEquals("# Modo de conexión: ftms-wifi", l[3])
        assertEquals("# Nombre ante la app: KICKR BIKE PRO", l[4])
        assertEquals("# Transmisión: ${Transmision.Lineal.ID}", l[5])
        assertEquals("# Modelo de resistencia: fisico", l[6])
        assertTrue(l.dropLast(1).all { it.startsWith("# ") })
        assertEquals("", l.last())
    }

    @Test
    fun el_nombre_ante_la_app_es_el_efectivo_del_modo() {
        val c = Configuracion(conexion = AjustesConexion(ModoConexion.FTMS_BLUETOOTH, Identidad.KICKR_BIKE_PRO))
        // Por Bluetooth el modo no deja elegir el nombre: el efectivo es «Smart Bike», el elegido se conserva.
        assertEquals("# Nombre ante la app: Smart Bike", ArchivoSesion.cabecera(inicio, ZoneOffset.UTC, "1", c)[4])
    }

    @Test
    fun la_linea_lleva_hora_de_reloj_origen_y_texto() {
        assertEquals("18:42:07.123 NEO Conectado", ArchivoSesion.linea(inicio, ZoneOffset.UTC, "Neo", "Conectado"))
    }

    @Test
    fun la_hora_sigue_la_zona() {
        assertEquals("19:42:07.123 APP x", ArchivoSesion.linea(inicio, ZoneOffset.ofHours(1), "App", "x"))
    }

    @Test
    fun las_lineas_de_parada_empiezan_por_almohadilla() {
        assertEquals("# Grabación detenida: queda 8 % libre, mínimo 10 %", ArchivoSesion.paradaPorEspacio(8, 100, 10))
        assertEquals("# se descartaron 12 sucesos", ArchivoSesion.descartados(12))
    }

    @Test
    fun hay_espacio_justo_en_el_minimo() {
        assertTrue(ArchivoSesion.hayEspacio(10, 100, 10))
    }

    @Test
    fun hay_espacio_por_encima_del_minimo() {
        assertTrue(ArchivoSesion.hayEspacio(11, 100, 10))
    }

    @Test
    fun no_hay_espacio_por_debajo_del_minimo() {
        assertFalse(ArchivoSesion.hayEspacio(9, 100, 10))
    }

    @Test
    fun sin_total_medible_no_hay_espacio() {
        assertFalse(ArchivoSesion.hayEspacio(0, 0, 5))
        assertFalse(ArchivoSesion.hayEspacio(50, 0, 5))
    }

    @Test
    fun no_desborda_con_almacenamientos_grandes() {
        val tb = 2L * 1024 * 1024 * 1024 * 1024
        assertTrue(ArchivoSesion.hayEspacio(tb / 2, tb, 50))
        assertFalse(ArchivoSesion.hayEspacio(tb / 2 - 1, tb, 50))
    }

    /** `sesion-20261001-000000.txt`, `sesion-20261002-000000.txt`…: uno por día, del más antiguo al más reciente. */
    private fun sesiones(n: Int) = (1..n).map { "sesion-202610%02d-000000.txt".format(it) }

    @Test
    fun por_debajo_del_limite_no_sobra_ninguna() {
        assertEquals(emptyList<String>(), ArchivoSesion.aBorrar(sesiones(12)))
        assertEquals(emptyList<String>(), ArchivoSesion.aBorrar(sesiones(19)))
    }

    @Test
    fun en_el_limite_sobra_la_mas_antigua() {
        assertEquals(listOf("sesion-20261001-000000.txt"), ArchivoSesion.aBorrar(sesiones(20)))
    }

    @Test
    fun por_encima_del_limite_sobran_las_mas_antiguas() {
        assertEquals(sesiones(6), ArchivoSesion.aBorrar(sesiones(25)))
    }

    @Test
    fun ordena_por_la_fecha_del_nombre_y_el_sufijo() {
        val nombres = listOf("sesion-20261029-184207-2.txt", "sesion-20261030-090000.txt", "sesion-20261029-184207.txt")
        assertEquals(
            listOf("sesion-20261029-184207.txt", "sesion-20261029-184207-2.txt"),
            ArchivoSesion.aBorrar(nombres, conservar = 2),
        )
        assertEquals(sesiones(6), ArchivoSesion.aBorrar(sesiones(25).shuffled(java.util.Random(1))))
    }
}
