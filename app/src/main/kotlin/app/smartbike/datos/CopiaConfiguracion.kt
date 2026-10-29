package app.smartbike.datos

import android.content.ContentResolver
import android.net.Uri
import app.smartbike.dominio.Configuracion
import app.smartbike.dominio.MotivoImportacion
import app.smartbike.dominio.ResultadoImportacion
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Exportar e importar la configuración a un archivo que elige el usuario con el selector del sistema (spec
 * `settings-backup`). Las ubicaciones son `Uri` en texto para probar el ViewModel sin Android.
 */
interface CopiaConfiguracion {
    /** Escribe [c] en [destino]; `false` si no se pudo. */
    suspend fun exportar(destino: String, c: Configuracion): Boolean

    /** Lee y valida [origen] entero, sin cambiar nada (design.md del change `exportar-bateria-y-giro`, decisiones 2 y 3). */
    suspend fun leer(origen: String): ResultadoImportacion
}

class CopiaArchivo(
    private val entrada: (String) -> InputStream?,
    private val salida: (String) -> OutputStream?,
) : CopiaConfiguracion {

    constructor(resolver: ContentResolver) : this(
        entrada = { resolver.openInputStream(Uri.parse(it)) },
        salida = { resolver.openOutputStream(Uri.parse(it), "wt") },
    )

    override suspend fun exportar(destino: String, c: Configuracion): Boolean = withContext(Dispatchers.IO) {
        try {
            val s = salida(destino) ?: return@withContext false
            s.use { it.write(Configuracion.aJson(c).encodeToByteArray()) }
            true
        } catch (_: Exception) {
            false
        }
    }

    override suspend fun leer(origen: String): ResultadoImportacion = withContext(Dispatchers.IO) {
        try {
            val e = entrada(origen) ?: return@withContext ResultadoImportacion.Invalida(MotivoImportacion.NO_SE_PUDO_LEER)
            val bytes = e.use { leerHasta(it, MAX_BYTES + 1) }
            // Una configuración real son unos pocos KB: un archivo enorme no es una configuración.
            if (bytes.size > MAX_BYTES) {
                ResultadoImportacion.Invalida(MotivoImportacion.NO_ES_CONFIGURACION)
            } else {
                Configuracion.validar(bytes.decodeToString())
            }
        } catch (_: Exception) {
            ResultadoImportacion.Invalida(MotivoImportacion.NO_SE_PUDO_LEER)
        }
    }

    companion object {
        const val MAX_BYTES = 1_000_000

        /** Hasta [limite] bytes de [e]. `InputStream.readNBytes` solo existe desde la API 33 y `minSdk` es 28. */
        internal fun leerHasta(e: InputStream, limite: Int): ByteArray {
            val salida = ByteArrayOutputStream()
            val bufer = ByteArray(8 * 1024)
            while (salida.size() < limite) {
                val n = e.read(bufer, 0, minOf(bufer.size, limite - salida.size()))
                if (n < 0) break
                salida.write(bufer, 0, n)
            }
            return salida.toByteArray()
        }
    }
}
