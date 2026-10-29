package app.smartbike.datos

import app.smartbike.dominio.AjustesConexion
import app.smartbike.dominio.Configuracion
import app.smartbike.dominio.Identidad
import app.smartbike.dominio.ModoConexion
import app.smartbike.dominio.MotivoImportacion
import app.smartbike.dominio.ResultadoImportacion
import app.smartbike.dominio.TemaApp
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

/** Exportar e importar con archivos reales, sin Android: las ubicaciones son rutas (spec `settings-backup`). */
class CopiaConfiguracionTest {
    @TempDir lateinit var carpeta: File
    private fun nuevo(nombre: String) = File(carpeta, nombre).also { it.createNewFile() }

    private val copia = CopiaArchivo(
        entrada = { ruta -> File(ruta).takeIf { it.exists() }?.let(::FileInputStream) },
        salida = { ruta -> FileOutputStream(File(ruta)) },
    )

    @Test
    fun ida_y_vuelta() = runTest {
        val archivo = nuevo("copia.json").path
        val c = Configuracion(tema = TemaApp.CLARO, pesoCiclistaKg = 91, conexion = AjustesConexion(ModoConexion.ZWIFT_RIDE, Identidad.KICKR_BIKE_PRO))
        assertTrue(copia.exportar(archivo, c))
        assertEquals(ResultadoImportacion.Valida(c), copia.leer(archivo))
    }

    @Test
    fun la_lectura_se_detiene_en_el_limite_aunque_lleguen_trozos_sueltos() {
        // Un flujo que entrega pocos bytes cada vez, como un `ContentResolver`; `readNBytes` no existe antes de la API 33.
        val trozos = object : java.io.InputStream() {
            var leidos = 0
            override fun read(): Int = if (leidos++ < 10_000) 'x'.code else -1
            override fun read(b: ByteArray, off: Int, len: Int): Int = super.read(b, off, minOf(len, 7))
        }
        assertEquals(5_001, CopiaArchivo.leerHasta(trozos, 5_001).size)
        assertEquals(3, CopiaArchivo.leerHasta("abc".byteInputStream(), 5_001).size)
    }

    @Test
    fun exportar_pisa_un_archivo_mas_largo() = runTest {
        val archivo = nuevo("copia.json")
        archivo.writeText("x".repeat(5000))
        assertTrue(copia.exportar(archivo.path, Configuracion()))
        assertTrue(copia.leer(archivo.path) is ResultadoImportacion.Valida)
    }

    @Test
    fun archivo_de_mas_de_un_mega() = runTest {
        val archivo = nuevo("grande.json")
        archivo.writeText("{\"version\":5,\"relleno\":\"" + "a".repeat(CopiaArchivo.MAX_BYTES) + "\"}")
        assertEquals(ResultadoImportacion.Invalida(MotivoImportacion.NO_ES_CONFIGURACION), copia.leer(archivo.path))
    }

    @Test
    fun archivo_vacio() = runTest {
        val archivo = nuevo("vacio.json").path
        assertEquals(ResultadoImportacion.Invalida(MotivoImportacion.VACIO), copia.leer(archivo))
    }

    @Test
    fun archivo_que_no_existe() = runTest {
        assertEquals(ResultadoImportacion.Invalida(MotivoImportacion.NO_SE_PUDO_LEER), copia.leer(File(carpeta, "no.json").path))
    }

    @Test
    fun destino_que_no_admite_escritura() = runTest {
        // Una carpeta no se puede abrir para escribir.
        assertFalse(copia.exportar(carpeta.path, Configuracion()))
    }
}
