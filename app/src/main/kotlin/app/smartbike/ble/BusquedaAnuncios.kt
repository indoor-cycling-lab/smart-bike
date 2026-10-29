package app.smartbike.ble

import java.util.UUID
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.transformWhile

/** Un anuncio Bluetooth, tal como lo ve la búsqueda: sin tipos de Android para poder probarla. */
data class AnuncioVisto(val nombre: String?, val direccion: String, val servicios: Set<UUID>)

/**
 * Qué anuncios encajan con una búsqueda y con qué [clave] se devuelve el resultado: por prefijo de nombre (el Neo y el
 * Square) o por servicio anunciado (el pulsómetro; design.md del change `pulso-zwift`, decisión 5).
 */
class CriterioAnuncio(val clave: String, private val criterio: (AnuncioVisto) -> Boolean) {
    fun encaja(a: AnuncioVisto): Boolean = criterio(a)

    companion object {
        fun prefijo(prefijo: String) = CriterioAnuncio(prefijo) { a -> a.nombre?.startsWith(prefijo, ignoreCase = true) == true }

        /** Encaja con el que anuncia [servicio]. */
        fun servicio(clave: String, servicio: UUID) = CriterioAnuncio(clave) { a -> servicio in a.servicios }
    }
}

/**
 * Un resultado por criterio: el primer anuncio que encaja, o [ResultadoBusqueda.NoEncontrado] al cumplirse [limiteMs].
 * Termina cuando todos los criterios tienen resultado.
 */
fun resolverBusqueda(anuncios: Flow<AnuncioVisto>, criterios: List<CriterioAnuncio>, limiteMs: Long): Flow<ResultadoBusqueda> = flow {
    val encontrados = anuncios.mapNotNull { a ->
        criterios.firstOrNull { it.encaja(a) }?.let { ResultadoBusqueda.Encontrado(it.clave, a.direccion, a.nombre ?: a.direccion) }
    }
    val limite = flow<ResultadoBusqueda> {
        delay(limiteMs)
        criterios.forEach { emit(ResultadoBusqueda.NoEncontrado(it.clave)) }
    }
    val pendientes = criterios.map { it.clave }.toMutableSet()
    emitAll(
        merge(encontrados, limite).transformWhile { r ->
            if (pendientes.remove(r.prefijo)) emit(r)
            pendientes.isNotEmpty()
        },
    )
}
