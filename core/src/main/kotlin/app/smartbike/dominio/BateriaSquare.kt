package app.smartbike.dominio

/**
 * La batería del Square (spec `square-battery`; design.md del change `exportar-bateria-y-giro`, decisión 6). La lectura de
 * `0x2A19` es gruesa (da 100 % con una pila de dos años), así que el umbral es provisional hasta las lecturas periódicas
 * de la prueba de 2 h del change 6.
 */
object BateriaSquare {
    /** Con este nivel o menos la batería se considera baja. */
    const val UMBRAL_BAJA_PCT = 20

    /** Una lectura de 0 a 100 %; cualquier otra no vale. */
    fun nivelValido(lectura: Int?): Int? = lectura?.takeIf { it in 0..100 }

    fun baja(nivel: Int?): Boolean = nivel != null && nivel <= UMBRAL_BAJA_PCT
}

/**
 * Decide qué lecturas de la batería cuentan como nivel (spec `square-battery`, «Nivel de batería del Square»): la
 * primera de cada conexión no cuenta, porque el Square da valores sin sentido justo después de encenderse, y una fuera de
 * 0–100 % tampoco. Se [reinicia] al conectar.
 */
class FiltroBateria {
    private var primera = true

    fun reiniciar() {
        primera = true
    }

    /** El nivel que aporta esta [lectura], o `null` si no cuenta. */
    fun lectura(lectura: Int?): Int? {
        if (primera) {
            primera = false
            return null
        }
        return BateriaSquare.nivelValido(lectura)
    }
}
