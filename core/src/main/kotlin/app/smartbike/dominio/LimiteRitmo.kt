package app.smartbike.dominio

/**
 * Cubo de fichas que limita el ritmo de mensajes de un cliente (design.md del change `endurecer-dircon-ble`,
 * decisión 4): admite una ráfaga de [CAPACIDAD] y, de forma sostenida, [RECARGA_POR_SEGUNDO] por segundo. [reloj] da
 * milisegundos monotónicos.
 */
class LimiteRitmo(private val reloj: () -> Long) {
    private var fichas = CAPACIDAD.toDouble()
    private var ultimoMs = reloj()

    /** Consume una ficha; devuelve `false` si el cubo está vacío, es decir, si el cliente se pasa de ritmo. */
    fun permitir(): Boolean {
        val ahora = reloj()
        fichas = minOf(CAPACIDAD.toDouble(), fichas + (ahora - ultimoMs) * RECARGA_POR_SEGUNDO / 1_000.0)
        ultimoMs = ahora
        if (fichas < 1.0) return false
        fichas -= 1.0
        return true
    }

    companion object {
        const val CAPACIDAD = 100
        const val RECARGA_POR_SEGUNDO = 50
    }
}
