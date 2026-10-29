package app.smartbike.dominio

import kotlinx.serialization.json.JsonObject

/**
 * Un documento guardado (`configuracion.json` o `estado.json`) ya leído, con lo que se perdió al leerlo: si no se
 * entendía nada ([ilegible]) o qué campos de primer nivel se cambiaron por su valor por defecto ([descartados]).
 */
data class Lectura<T>(val valor: T, val ilegible: Boolean = false, val descartados: List<String> = emptyList()) {
    /** Si se leyó entero, sin perder nada. */
    val completa: Boolean get() = !ilegible && descartados.isEmpty()
}

/**
 * Lee [objeto] con [leer] y, si no se entiende entero, campo a campo: los campos de primer nivel que no se entienden
 * toman su valor por defecto y los demás se conservan, para que un valor dañado no se lleve los emparejamientos o el
 * nombre Bluetooth original. `version` va primero, porque de ella depende la migración de los demás.
 */
internal fun <T> leerPorCampos(objeto: JsonObject, leer: (JsonObject) -> T): Lectura<T> {
    try {
        return Lectura(leer(objeto))
    } catch (_: Exception) {
        // Se sigue campo a campo.
    }
    val campos = objeto.entries.sortedBy { it.key != "version" }
    var conservados = JsonObject(emptyMap())
    val descartados = mutableListOf<String>()
    for ((clave, valor) in campos) {
        val prueba = JsonObject(conservados + (clave to valor))
        if (runCatching { leer(prueba) }.isSuccess) conservados = prueba else descartados += clave
    }
    return Lectura(leer(conservados), descartados = descartados)
}
