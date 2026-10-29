package app.smartbike.datos

import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

/** Cómo está una sesión de la lista. */
enum class EstadoSesion { CERRADA, GRABANDO, DETENIDA_POR_ESPACIO, DETENIDA_POR_ERROR }

/** Una sesión grabada: su archivo, cuándo empezó (por el nombre) y cuánto ocupa. */
data class SesionGrabada(val nombre: String, val inicio: LocalDateTime?, val tamanoBytes: Long, val estado: EstadoSesion) {
    /** La que se está grabando no se puede borrar. */
    val borrable: Boolean get() = estado != EstadoSesion.GRABANDO
}

/** Cuántas sesiones hay y cuánto ocupan, para el resumen de Ajustes. */
data class ResumenSesiones(val cantidad: Int, val tamanoBytes: Long)

/**
 * Las sesiones grabadas en [directorio] (`files/sesiones/`): las lista, la más reciente primero, y las borra
 * (design.md del change `registro-sesiones`, decisión 6). [estadoGrabacion] dice cuál se está grabando.
 */
class RepositorioSesiones(
    private val directorio: File,
    private val estadoGrabacion: () -> EstadoGrabacion,
) {
    fun listar(): List<SesionGrabada> {
        val estado = estadoGrabacion()
        return directorio.listFiles { f -> f.isFile && f.name.endsWith(EXTENSION) }.orEmpty()
            .sortedWith(compareByDescending<File> { it.name }.thenByDescending { it.lastModified() })
            .map { f -> SesionGrabada(f.name, inicioDe(f.name), f.length(), estadoDe(f.name, estado)) }
    }

    fun resumen(): ResumenSesiones = listar().let { l -> ResumenSesiones(l.size, l.sumOf { it.tamanoBytes }) }

    /** El archivo de [nombre] si es una sesión de la carpeta; nunca una ruta que salga de ella. */
    fun archivo(nombre: String): File? {
        if (!nombre.endsWith(EXTENSION) || nombre != File(nombre).name) return null
        return File(directorio, nombre).takeIf { it.isFile }
    }

    /** Borra [nombre], salvo la que se está grabando. Devuelve si se borró. */
    fun borrar(nombre: String): Boolean {
        val f = archivo(nombre) ?: return false
        if (estadoDe(nombre, estadoGrabacion()) == EstadoSesion.GRABANDO) return false
        return f.delete()
    }

    /** Borra todas las que se pueden borrar (respeta la que se graba). Devuelve cuántas. */
    fun borrarTodas(): Int = listar().count { it.borrable && borrar(it.nombre) }

    private fun estadoDe(nombre: String, e: EstadoGrabacion): EstadoSesion = when {
        e is EstadoGrabacion.Grabando && e.archivo == nombre -> EstadoSesion.GRABANDO
        e is EstadoGrabacion.DetenidaPorEspacio && e.archivo == nombre -> EstadoSesion.DETENIDA_POR_ESPACIO
        e is EstadoGrabacion.DetenidaPorError && e.archivo == nombre -> EstadoSesion.DETENIDA_POR_ERROR
        else -> EstadoSesion.CERRADA
    }

    /** `sesion-20261029-184207.txt` o `sesion-20261029-184207-2.txt` → 2026-10-29 18:42:07. */
    private fun inicioDe(nombre: String): LocalDateTime? = try {
        LocalDateTime.parse(nombre.removePrefix("sesion-").take(15), FORMATO)
    } catch (_: DateTimeParseException) {
        null
    }

    private companion object {
        const val EXTENSION = ".txt"
        val FORMATO: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")
    }
}
