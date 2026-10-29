package app.smartbike.datos

import java.io.File
import java.time.LocalDateTime
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class RepositorioSesionesTest {

    @TempDir
    lateinit var dir: File

    private var estado: EstadoGrabacion = EstadoGrabacion.Apagada
    private fun repo() = RepositorioSesiones(dir, { estado })

    private fun sesion(nombre: String, bytes: Int) = File(dir, nombre).also { it.writeText("x".repeat(bytes)) }

    @Test
    fun lista_vacia_si_no_hay_carpeta_ni_sesiones() {
        assertEquals(emptyList<SesionGrabada>(), RepositorioSesiones(File(dir, "no-existe")) { estado }.listar())
        assertEquals(ResumenSesiones(0, 0), repo().resumen())
    }

    @Test
    fun ordena_por_nombre_descendente_con_tamano_e_inicio() {
        sesion("sesion-20261026-190300.txt", 11)
        sesion("sesion-20261029-184207.txt", 21)
        sesion("sesion-20261028-071500.txt", 18)
        File(dir, "otra-cosa.dat").writeText("no")
        val l = repo().listar()
        assertEquals(listOf("sesion-20261029-184207.txt", "sesion-20261028-071500.txt", "sesion-20261026-190300.txt"), l.map { it.nombre })
        assertEquals(listOf(21L, 18L, 11L), l.map { it.tamanoBytes })
        assertEquals(LocalDateTime.of(2026, 10, 29, 18, 42, 7), l.first().inicio)
        assertEquals(ResumenSesiones(3, 50), repo().resumen())
    }

    @Test
    fun marca_la_que_se_graba_y_las_detenidas() {
        sesion("sesion-20261029-184207.txt", 5)
        sesion("sesion-20261028-071500.txt", 5)
        estado = EstadoGrabacion.Grabando("sesion-20261029-184207.txt")
        assertEquals(listOf(EstadoSesion.GRABANDO, EstadoSesion.CERRADA), repo().listar().map { it.estado })
        assertFalse(repo().listar().first().borrable)
        estado = EstadoGrabacion.DetenidaPorEspacio("sesion-20261029-184207.txt")
        assertEquals(EstadoSesion.DETENIDA_POR_ESPACIO, repo().listar().first().estado)
        estado = EstadoGrabacion.DetenidaPorError("sesion-20261029-184207.txt")
        assertEquals(EstadoSesion.DETENIDA_POR_ERROR, repo().listar().first().estado)
        assertTrue(repo().listar().first().borrable)
    }

    @Test
    fun borra_una() {
        sesion("sesion-20261029-184207.txt", 5)
        sesion("sesion-20261028-071500.txt", 5)
        assertTrue(repo().borrar("sesion-20261028-071500.txt"))
        assertEquals(listOf("sesion-20261029-184207.txt"), repo().listar().map { it.nombre })
    }

    @Test
    fun nunca_borra_la_que_se_graba() {
        sesion("sesion-20261029-184207.txt", 5)
        estado = EstadoGrabacion.Grabando("sesion-20261029-184207.txt")
        assertFalse(repo().borrar("sesion-20261029-184207.txt"))
        assertEquals(1, repo().listar().size)
    }

    @Test
    fun borrar_todas_sin_grabacion_deja_la_lista_vacia() {
        repeat(4) { sesion("sesion-2026102$it-100000.txt", 3) }
        assertEquals(4, repo().borrarTodas())
        assertTrue(repo().listar().isEmpty())
    }

    @Test
    fun borrar_todas_respeta_la_que_se_graba() {
        sesion("sesion-20261029-184207.txt", 5)
        sesion("sesion-20261028-071500.txt", 5)
        sesion("sesion-20261026-190300.txt", 5)
        estado = EstadoGrabacion.Grabando("sesion-20261029-184207.txt")
        assertEquals(2, repo().borrarTodas())
        assertEquals(listOf("sesion-20261029-184207.txt"), repo().listar().map { it.nombre })
    }

    @Test
    fun no_sale_de_la_carpeta() {
        val fuera = File(dir.parentFile, "secreto.txt").also { it.writeText("s") }
        try {
            val sub = File(dir, "sesiones").also { it.mkdirs() }
            val r = RepositorioSesiones(sub) { estado }
            assertNull(r.archivo("../secreto.txt"))
            assertFalse(r.borrar("../secreto.txt"))
            assertTrue(fuera.exists())
        } finally {
            fuera.delete()
        }
    }
}
