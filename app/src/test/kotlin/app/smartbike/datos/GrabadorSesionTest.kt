package app.smartbike.datos

import app.smartbike.dominio.Configuracion
import java.io.File
import java.io.IOException
import java.io.Writer
import java.time.ZoneOffset
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

/** El grabador con un directorio temporal, el reloj del test y un espacio falso (spec `session-recording`). */
@OptIn(ExperimentalCoroutinesApi::class)
class GrabadorSesionTest {

    @TempDir
    lateinit var dir: File

    private val inicio = 1_793_299_327_123L // 2026-10-29 18:42:07.123 UTC
    private var libre = 50L
    private val total = 100L

    private class Fixture(val registro: RegistroSesion, val grabador: GrabadorSesion)

    private fun TestScope.montar(
        capacidadCola: Int = 1_000,
        abrir: (File) -> Writer = { it.bufferedWriter(Charsets.UTF_8) },
        borrar: (File) -> Boolean = { it.delete() },
    ): Fixture {
        val registro = RegistroSesion(relojMonotonico = { testScheduler.currentTime }, relojPared = { inicio + testScheduler.currentTime })
        val grabador = GrabadorSesion(
            File(dir, "sesiones"), registro, relojPared = { inicio }, relojMonotonico = { testScheduler.currentTime },
            zona = ZoneOffset.UTC, version = "0.9.0", espacio = { Espacio(libre, total) }, ambito = backgroundScope,
            capacidadCola = capacidadCola, abrir = abrir, borrar = borrar,
        )
        return Fixture(registro, grabador)
    }

    private fun archivos(): List<File> = File(dir, "sesiones").listFiles()?.sortedBy { it.name } ?: emptyList()

    private fun TestScope.pasar(ms: Long) {
        advanceTimeBy(ms)
        runCurrent()
    }

    @Test
    fun cabecera_y_sucesos_en_orden() = runTest {
        val f = montar()
        f.grabador.iniciar(Configuracion())
        runCurrent()
        f.registro.suceso(Origen.Neo, "Conectado")
        f.registro.suceso(Origen.App, "Marcha 5")
        pasar(1_500)
        val texto = archivos().single().readText()
        assertEquals("sesion-20261029-184207.txt", archivos().single().name)
        assertTrue(texto.startsWith("# Smart Bike · sesión grabada\n# Inicio: 2026-10-29 18:42:07\n# Versión de la app: 0.9.0\n"))
        val lineas = texto.lines()
        assertEquals(listOf("18:42:07.123 NEO Conectado", "18:42:07.123 APP Marcha 5"), lineas.filter { !it.startsWith("#") && it.isNotBlank() })
        assertEquals(EstadoGrabacion.Grabando("sesion-20261029-184207.txt"), f.grabador.estado.value)
    }

    @Test
    fun solo_sucesos_sin_tramas() = runTest {
        val f = montar()
        f.grabador.iniciar(Configuracion())
        f.registro.trama(Origen.Square, Sentido.Rx, "0x43", byteArrayOf(1, 2, 3))
        f.registro.trama(Origen.Neo, Sentido.Tx, "fec3", byteArrayOf(4))
        f.registro.suceso(Origen.App, "uno")
        pasar(1_500)
        val cuerpo = archivos().single().readLines().filter { !it.startsWith("#") && it.isNotBlank() }
        assertEquals(1, cuerpo.size)
        assertTrue(cuerpo.single().endsWith("APP uno"))
    }

    @Test
    fun se_puede_leer_el_archivo_mientras_se_graba() = runTest {
        val f = montar()
        f.grabador.iniciar(Configuracion())
        pasar(100)
        f.registro.suceso(Origen.App, "primero")
        pasar(1_100)
        assertTrue(archivos().single().readText().contains("APP primero"))
        f.registro.suceso(Origen.App, "segundo")
        pasar(1_100)
        assertTrue(archivos().single().readText().contains("APP segundo"))
        assertTrue(f.grabador.estado.value is EstadoGrabacion.Grabando)
    }

    @Test
    fun para_por_poco_espacio_con_su_linea_final_y_no_reanuda() = runTest {
        val f = montar()
        f.grabador.iniciar(Configuracion(espacioLibreMinimoPct = 10))
        pasar(1_000)
        libre = 8
        pasar(30_000)
        val nombre = archivos().single().name
        assertEquals(EstadoGrabacion.DetenidaPorEspacio(nombre), f.grabador.estado.value)
        assertEquals("# Grabación detenida: queda 8 % libre, mínimo 10 %", archivos().single().readLines().last { it.isNotBlank() })
        val tamano = archivos().single().length()
        libre = 90
        f.registro.suceso(Origen.App, "después")
        pasar(60_000)
        assertEquals(tamano, archivos().single().length())
        assertNull(f.registro.sumidero)
        assertEquals(EstadoGrabacion.DetenidaPorEspacio(nombre), f.grabador.estado.value)
    }

    @Test
    fun sin_espacio_al_empezar_no_crea_archivo() = runTest {
        libre = 4
        val f = montar()
        f.grabador.iniciar(Configuracion(espacioLibreMinimoPct = 10))
        pasar(1_000)
        assertEquals(EstadoGrabacion.SinEspacio, f.grabador.estado.value)
        assertTrue(archivos().isEmpty())
        assertNull(f.registro.sumidero)
    }

    @Test
    fun un_fallo_de_escritura_detiene_la_grabacion_sin_propagarse() = runTest {
        val roto = object : Writer() {
            override fun write(cbuf: CharArray, off: Int, len: Int) = throw IOException("disco lleno")
            override fun flush() = throw IOException("disco lleno")
            override fun close() {}
        }
        val f = montar(abrir = { roto })
        f.grabador.iniciar(Configuracion())
        pasar(1_000)
        assertTrue(f.grabador.estado.value is EstadoGrabacion.DetenidaPorError)
        assertNull(f.registro.sumidero)
        assertTrue(f.registro.ultimas(5).any { it.texto.contains("detenida por un error") })
    }

    @Test
    fun un_fallo_al_abrir_se_anota_y_no_propaga() = runTest {
        val f = montar(abrir = { throw IOException("sin permiso") })
        f.grabador.iniciar(Configuracion())
        assertTrue(f.grabador.estado.value is EstadoGrabacion.DetenidaPorError)
        assertNull(f.registro.sumidero)
    }

    @Test
    fun el_nombre_lleva_sufijo_si_ya_existe() = runTest {
        val f = montar()
        f.grabador.iniciar(Configuracion())
        f.grabador.detener()
        pasar(10)
        f.grabador.iniciar(Configuracion())
        pasar(10)
        assertEquals(listOf("sesion-20261029-184207-2.txt", "sesion-20261029-184207.txt"), archivos().map { it.name })
    }

    @Test
    fun el_desbordamiento_de_la_cola_deja_su_aviso() = runTest {
        val f = montar(capacidadCola = 3)
        f.grabador.iniciar(Configuracion())
        repeat(10) { f.registro.suceso(Origen.App, "e$it") }
        pasar(1_500)
        val texto = archivos().single().readText()
        assertTrue(texto.contains("# se descartaron 7 sucesos"), texto)
        assertTrue(texto.contains("APP e9"))
        assertFalse(texto.contains("APP e0"))
    }

    @Test
    fun detener_cierra_el_archivo_y_deja_de_recibir() = runTest {
        val f = montar()
        f.grabador.iniciar(Configuracion())
        f.registro.suceso(Origen.App, "antes")
        pasar(100)
        f.grabador.detener()
        f.registro.suceso(Origen.App, "después")
        pasar(5_000)
        val texto = archivos().single().readText()
        assertTrue(texto.contains("APP antes"))
        assertFalse(texto.contains("después"))
        assertEquals(EstadoGrabacion.Apagada, f.grabador.estado.value)
        assertNull(f.registro.sumidero)
    }

    @Test
    fun una_parada_se_sigue_viendo_hasta_olvidarla() = runTest {
        libre = 4
        val f = montar()
        f.grabador.iniciar(Configuracion())
        f.grabador.detener()
        assertEquals(EstadoGrabacion.SinEspacio, f.grabador.estado.value)
        f.grabador.detener(olvidarParada = true)
        assertEquals(EstadoGrabacion.Apagada, f.grabador.estado.value)
    }

    private fun TestScope.atado(): Triple<Fixture, MutableStateFlow<Configuracion>, MutableStateFlow<Boolean>> {
        val f = montar()
        val config = MutableStateFlow(Configuracion())
        val sesion = MutableStateFlow(false)
        backgroundScope.atarGrabador(f.grabador, config, sesion)
        return Triple(f, config, sesion)
    }

    @Test
    fun activar_con_la_sesion_en_curso_abre_un_archivo_en_ese_momento() = runTest {
        val (f, config, sesion) = atado()
        sesion.value = true
        pasar(10)
        assertTrue(archivos().isEmpty())
        f.registro.suceso(Origen.App, "antes de activar")
        config.value = Configuracion(grabarSesiones = true)
        pasar(10)
        f.registro.suceso(Origen.App, "después")
        pasar(1_500)
        val texto = archivos().single().readText()
        assertTrue(texto.contains("APP después"))
        assertFalse(texto.contains("antes de activar"))
    }

    @Test
    fun desactivar_cierra_el_archivo() = runTest {
        val (f, config, sesion) = atado()
        sesion.value = true
        config.value = Configuracion(grabarSesiones = true)
        pasar(10)
        assertTrue(f.grabador.estado.value is EstadoGrabacion.Grabando)
        config.value = Configuracion(grabarSesiones = false)
        pasar(10)
        assertEquals(EstadoGrabacion.Apagada, f.grabador.estado.value)
        assertNull(f.registro.sumidero)
        assertEquals(1, archivos().size)
    }

    @Test
    fun terminar_la_sesion_cierra_el_archivo_y_una_nueva_abre_otro() = runTest {
        val (f, config, sesion) = atado()
        config.value = Configuracion(grabarSesiones = true)
        sesion.value = true
        pasar(10)
        sesion.value = false
        pasar(10)
        assertNull(f.registro.sumidero)
        assertEquals(EstadoGrabacion.Apagada, f.grabador.estado.value)
        sesion.value = true
        pasar(10)
        assertEquals(2, archivos().size)
        assertTrue(f.grabador.estado.value is EstadoGrabacion.Grabando)
    }

    @Test
    fun sin_el_ajuste_no_se_escribe_nada_aunque_haya_sesion() = runTest {
        val (f, _, sesion) = atado()
        sesion.value = true
        f.registro.suceso(Origen.App, "x")
        pasar(5_000)
        assertTrue(archivos().isEmpty())
        assertNull(f.registro.sumidero)
    }

    @Test
    fun el_minimo_se_aplica_al_momento_a_una_grabacion_en_curso() = runTest {
        val (f, config, sesion) = atado()
        config.value = Configuracion(grabarSesiones = true, espacioLibreMinimoPct = 10)
        sesion.value = true
        pasar(1_000)
        config.value = Configuracion(grabarSesiones = true, espacioLibreMinimoPct = 60) // libre es 50 %
        pasar(31_000)
        assertTrue(f.grabador.estado.value is EstadoGrabacion.DetenidaPorEspacio)
    }

    /** [n] sesiones cerradas de octubre de 2026, una por día, anteriores a la que se graba en el test. */
    private fun sesionesAntiguas(n: Int): List<String> = (1..n).map { "sesion-202610%02d-000000.txt".format(it) }.onEach {
        File(dir, "sesiones").apply { mkdirs() }.resolve(it).writeText("# antigua\n")
    }

    @Test
    fun con_20_sesiones_al_empezar_se_borra_la_mas_antigua() = runTest {
        val antiguas = sesionesAntiguas(20)
        val f = montar()
        f.grabador.iniciar(Configuracion())
        pasar(10)
        val nombres = archivos().map { it.name }
        assertEquals(20, nombres.size)
        assertFalse(antiguas.first() in nombres)
        assertTrue("sesion-20261029-184207.txt" in nombres)
        assertEquals(EstadoGrabacion.Grabando("sesion-20261029-184207.txt"), f.grabador.estado.value)
        assertTrue(f.registro.ultimas(10).any { it.texto == "Sesión antigua borrada: ${antiguas.first()}" })
    }

    @Test
    fun con_25_sesiones_al_empezar_quedan_20() = runTest {
        val antiguas = sesionesAntiguas(25)
        val f = montar()
        f.grabador.iniciar(Configuracion())
        pasar(10)
        assertEquals(antiguas.drop(6) + "sesion-20261029-184207.txt", archivos().map { it.name })
    }

    @Test
    fun por_debajo_del_limite_no_se_borra_ninguna() = runTest {
        sesionesAntiguas(12)
        val f = montar()
        f.grabador.iniciar(Configuracion())
        pasar(10)
        assertEquals(13, archivos().size)
    }

    @Test
    fun sin_espacio_no_se_borra_ninguna() = runTest {
        sesionesAntiguas(25)
        libre = 4
        val f = montar()
        f.grabador.iniciar(Configuracion(espacioLibreMinimoPct = 10))
        pasar(10)
        assertEquals(EstadoGrabacion.SinEspacio, f.grabador.estado.value)
        assertEquals(25, archivos().size)
    }

    @Test
    fun sin_iniciar_no_se_borra_ninguna() = runTest {
        sesionesAntiguas(25)
        montar()
        pasar(10)
        assertEquals(25, archivos().size)
    }

    @Test
    fun si_no_se_puede_borrar_una_antigua_se_graba_igual() = runTest {
        val antiguas = sesionesAntiguas(20)
        val f = montar(borrar = { false })
        f.grabador.iniciar(Configuracion())
        pasar(10)
        assertEquals(21, archivos().size)
        assertEquals(EstadoGrabacion.Grabando("sesion-20261029-184207.txt"), f.grabador.estado.value)
        assertTrue(f.registro.ultimas(10).any { it.texto == "No se pudo borrar la sesión antigua: ${antiguas.first()}" })
    }
}
