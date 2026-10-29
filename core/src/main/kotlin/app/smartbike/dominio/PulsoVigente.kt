package app.smartbike.dominio

/**
 * Cuándo hay pulso (spec `heart-rate`, «Pulso válido»; design.md del change `pulso-zwift`, decisión 3). Solo valen
 * lecturas enteras de 1 a 250: las demás se ignoran y mantienen la anterior. Pasados [caducidadMs] sin una válida no hay
 * pulso, y vuelve a haberlo con la siguiente. El valor no se filtra ni se promedia. [reloj] da milisegundos monotónicos.
 */
class PulsoVigente(private val reloj: () -> Long, private val caducidadMs: Long = CADUCIDAD_MS) {
    private var bpm: Int? = null
    private var instanteMs = 0L

    /** Registra una lectura; devuelve si valía. */
    fun registrar(lectura: Int): Boolean {
        if (lectura !in BPM_MIN..BPM_MAX) return false
        bpm = lectura
        instanteMs = reloj()
        return true
    }

    /** La conexión se ha perdido: no hay pulso desde ya, sin esperar a la caducidad. */
    fun perder() {
        bpm = null
    }

    /** El último pulso válido, o `null` si no lo hay o caducó. */
    fun valor(ahora: Long = reloj()): Int? = bpm?.takeIf { ahora - instanteMs < caducidadMs }

    companion object {
        const val BPM_MIN = 1
        const val BPM_MAX = 250
        const val CADUCIDAD_MS = 5_000L
    }
}
