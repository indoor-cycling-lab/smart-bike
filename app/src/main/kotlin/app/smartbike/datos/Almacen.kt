package app.smartbike.datos

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.Serializer
import app.smartbike.dominio.Configuracion
import app.smartbike.dominio.EstadoGuardado
import app.smartbike.dominio.Lectura
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

/** Lo que la `Sesion` necesita de `estado.json`, detrás de una interfaz para probar sin Android. */
interface AlmacenEstado {
    suspend fun leer(): EstadoGuardado
    suspend fun actualizar(cambio: (EstadoGuardado) -> EstadoGuardado)
}

/**
 * `configuracion.json` (design.md del change `gestos-y-asignacion`, decisión 5): lo leen la `Sesion` y las pantallas
 * de ajustes, y lo cambian las dos (la `Sesion` al fijar la referencia).
 */
interface AlmacenConfiguracion {
    val configuracion: Flow<Configuracion>
    suspend fun actualizarConfiguracion(cambio: (Configuracion) -> Configuracion)
}

/**
 * `estado.json` y `configuracion.json` en DataStore (arquitectura.md § 10). Un documento ilegible se sustituye por los
 * valores por defecto en lugar de dejar la app sin arrancar, y un campo que no se entiende, por el suyo: los demás se
 * conservan. Si se pierde algo, el documento tal como estaba se copia a `<archivo>.ilegible` (solo el último) y
 * [avisar] lo cuenta, para el registro de la sesión.
 */
class Almacen(context: Context, ambito: CoroutineScope, private val avisar: (String) -> Unit = {}) :
    AlmacenEstado,
    AlmacenConfiguracion {
    private val carpeta = context.applicationContext.filesDir

    private val estado: DataStore<EstadoGuardado> = DataStoreFactory.create(
        serializer = SerializadorTolerante(EstadoGuardado(), EstadoGuardado::leerGuardado, EstadoGuardado::aJson, perdido(RUTA_ESTADO)),
        scope = ambito,
        produceFile = { File(carpeta, RUTA_ESTADO) },
    )

    private val ajustes: DataStore<Configuracion> = DataStoreFactory.create(
        serializer = SerializadorTolerante(Configuracion(), Configuracion::leerGuardada, Configuracion::aJson, perdido(RUTA_CONFIGURACION)),
        scope = ambito,
        produceFile = { File(carpeta, RUTA_CONFIGURACION) },
    )

    override suspend fun leer(): EstadoGuardado = estado.data.first()

    override suspend fun actualizar(cambio: (EstadoGuardado) -> EstadoGuardado) {
        estado.updateData(cambio)
    }

    override val configuracion: Flow<Configuracion> = ajustes.data

    override suspend fun actualizarConfiguracion(cambio: (Configuracion) -> Configuracion) {
        ajustes.updateData(cambio)
    }

    /** Copia el documento de [ruta] tal como estaba, antes de que la siguiente escritura lo sustituya, y lo cuenta. */
    private fun <T> perdido(ruta: String): (String, Lectura<T>) -> Unit = { texto, lectura ->
        val copia = File(carpeta, "$ruta$SUFIJO_COPIA")
        val copiada = runCatching { copia.writeText(texto) }.isSuccess
        val perdida = if (lectura.ilegible) "ilegible, con los valores por defecto" else "con valores por defecto en ${lectura.descartados.joinToString()}"
        avisar("${File(ruta).name} $perdida; " + if (copiada) "copia en ${copia.name}" else "no se pudo copiar")
    }

    companion object {
        /** La configuración dentro de `files/`: lo único que entra en la copia de seguridad (`res/xml/reglas_copia.xml`). */
        const val RUTA_CONFIGURACION = "datastore/configuracion.json"
        const val RUTA_ESTADO = "datastore/estado.json"

        /** La copia de un documento que no se pudo leer entero, junto a él y fuera de la copia de seguridad. */
        const val SUFIJO_COPIA = ".ilegible"
    }
}

/**
 * Un documento de DataStore que nunca impide arrancar: [leer] sustituye lo que no se entiende por los valores por
 * defecto, entero o campo a campo, y [alPerder] recibe el texto original y lo que se perdió.
 */
internal class SerializadorTolerante<T>(
    override val defaultValue: T,
    private val leer: (String) -> Lectura<T>,
    private val escribir: (T) -> String,
    private val alPerder: (String, Lectura<T>) -> Unit = { _, _ -> },
) : Serializer<T> {
    override suspend fun readFrom(input: InputStream): T {
        val texto = input.readBytes().decodeToString()
        val lectura = leer(texto)
        if (!lectura.completa) alPerder(texto, lectura)
        return lectura.valor
    }

    override suspend fun writeTo(t: T, output: OutputStream) {
        output.write(escribir(t).encodeToByteArray())
    }
}
