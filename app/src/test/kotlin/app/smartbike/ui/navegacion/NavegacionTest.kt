package app.smartbike.ui.navegacion

import app.smartbike.R
import app.smartbike.ui.componentes.Enlace
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

/** Spec `app-navigation`, «Pantalla anterior o siguiente con el Square», sobre la función pura del destino. */
class NavegacionTest {

    @Test
    fun la_pestana_conexion_tiene_su_icono_distinto_del_de_dispositivos_y_del_mando() {
        // Spec `ui-components`, «Icono de Conexión».
        assertEquals(R.drawable.ic_sensors, Pestana.Conexion.icono)
        assertNotEquals(Pestana.Dispositivos.icono, Pestana.Conexion.icono)
        assertNotEquals(Enlace.Mando.icono, Pestana.Conexion.icono)
        assertNotEquals(Enlace.Zwift.icono, Pestana.Conexion.icono)
    }

    @Test
    fun orden_de_las_pestanas() {
        // Botones ya no es una pestaña: se abre desde Ajustes.
        assertEquals(listOf("rodaje", "conexion", "dispositivos", "ajustes"), Pestana.entries.map { it.ruta })
    }

    @Test
    fun entre_dispositivos_y_ajustes() {
        // Spec `app-navigation`: Rodar, Conexión, Dispositivos y Ajustes.
        assertEquals(Pestana.Dispositivos, Pestana.vecina(Rutas.CONEXION, siguiente = true))
        assertEquals(Pestana.Ajustes, Pestana.vecina(Rutas.DISPOSITIVOS, siguiente = true))
        assertEquals(Pestana.Rodar, Pestana.vecina(Rutas.CONEXION, siguiente = false))
    }

    @Test
    fun desde_rodaje() {
        assertEquals(Pestana.Conexion, Pestana.vecina(Rutas.RODAJE, siguiente = true))
        // «Anterior desde Rodaje».
        assertEquals(Pestana.Ajustes, Pestana.vecina(Rutas.RODAJE, siguiente = false))
    }

    @Test
    fun vuelta_circular() {
        assertEquals(Pestana.Rodar, Pestana.vecina(Rutas.AJUSTES, siguiente = true))
    }

    @Test
    fun desde_una_secundaria() {
        // «Desde una secundaria»: Marchas es de Ajustes, y su anterior es Dispositivos.
        assertEquals(Pestana.Dispositivos, Pestana.vecina(Rutas.MARCHAS, siguiente = false))
        assertEquals(Pestana.Rodar, Pestana.vecina(Rutas.DIAGNOSTICO, siguiente = true))
        assertEquals(Pestana.Rodar, Pestana.vecina(Rutas.DATOS, siguiente = true))
        // Sesiones grabadas se abre desde Ajustes: su siguiente es Rodar y su anterior, Dispositivos.
        assertEquals(Pestana.Ajustes, Pestana.de(Rutas.SESIONES))
        assertEquals(Pestana.Rodar, Pestana.vecina(Rutas.SESIONES, siguiente = true))
        assertEquals(Pestana.Dispositivos, Pestana.vecina(Rutas.SESIONES, siguiente = false))
        // «Desde Botones»: es secundaria de Ajustes, y su siguiente es Rodar.
        assertEquals(Pestana.Ajustes, Pestana.de(Rutas.BOTONES))
        assertEquals(Pestana.Rodar, Pestana.vecina(Rutas.BOTONES, siguiente = true))
        // Asignar acción también es de Ajustes, porque se abre desde Botones (en la app no llega: durante la captura no hay acciones).
        assertEquals(Pestana.Rodar, Pestana.vecina("${Rutas.ASIGNAR}/{accion}", siguiente = true))
    }

    @Test
    fun ruta_desconocida_es_rodar() {
        assertEquals(Pestana.Rodar, Pestana.de(null))
        assertEquals(Pestana.Conexion, Pestana.vecina("otra", siguiente = true))
    }
}
