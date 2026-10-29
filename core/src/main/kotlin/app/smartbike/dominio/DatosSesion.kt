package app.smartbike.dominio

import kotlin.math.roundToInt

/**
 * Datos de la sesión para Rodaje (F11.3; design.md del change `rodaje-completo-y-ajustes`, decisión 5): potencia de
 * 3 s, tiempo pedaleando, potencia media y máxima. Puro y sin reloj: cada muestra trae su marca de tiempo monotónica.
 *
 * Para el tiempo y la media, cada muestra vale hasta la siguiente: solo cuentan los intervalos que empiezan con cadencia,
 * recortados a [HUECO_MAX_MS] para no contar el hueco de una muestra perdida; [pausar] corta la serie al perder el Neo,
 * así que una desconexión no suma nada.
 */
class DatosSesion {
    private val ventana = ArrayDeque<Muestra>()
    private var anterior: Muestra? = null
    private var tiempoMs = 0L
    private var energiaWms = 0.0
    private var maximaW = 0
    private var potencia3sW = 0

    private data class Muestra(val tMs: Long, val potenciaW: Int, val cadenciaRpm: Int)

    /** Una lectura de potencia del Neo, con la cadencia de ese momento. */
    fun muestra(tMs: Long, potenciaW: Int, cadenciaRpm: Int) {
        val m = Muestra(tMs, potenciaW.coerceAtLeast(0), cadenciaRpm)
        anterior?.let { a ->
            if (a.cadenciaRpm > 0) {
                val dt = (tMs - a.tMs).coerceIn(0, HUECO_MAX_MS)
                tiempoMs += dt
                energiaWms += a.potenciaW * dt.toDouble()
            }
        }
        anterior = m
        ventana.addLast(m)
        potencia3sW = media3s(tMs)
        maximaW = maxOf(maximaW, potencia3sW)
    }

    /** Sin Neo: la serie se corta y la potencia de 3 s vuelve a 0, pero tiempo, media y máxima se conservan. */
    fun pausar() {
        anterior = null
        ventana.clear()
        potencia3sW = 0
    }

    val resumen: Resumen get() = Resumen(potencia3sW, tiempoMs, if (tiempoMs > 0) (energiaWms / tiempoMs).roundToInt() else 0, maximaW)

    /**
     * Media ponderada por tiempo de los últimos [VENTANA_MS]: cada lectura vale para el intervalo desde la anterior, como
     * la potencia que mide el Neo. La primera de la serie vale [VENTANA_MS] hacia atrás.
     */
    private fun media3s(tMs: Long): Int {
        val desde = tMs - VENTANA_MS
        // Se conserva la última lectura anterior a la ventana: marca dónde empieza la primera de dentro.
        while (ventana.size > 1 && ventana[1].tMs <= desde) ventana.removeFirst()
        var suma = 0.0
        var duracion = 0L
        for (i in ventana.indices) {
            val inicio = maxOf(if (i > 0) ventana[i - 1].tMs else ventana[i].tMs - VENTANA_MS, desde)
            val fin = ventana[i].tMs
            if (fin > inicio) {
                suma += ventana[i].potenciaW * (fin - inicio).toDouble()
                duracion += fin - inicio
            }
        }
        return if (duracion > 0) (suma / duracion).roundToInt() else ventana.last().potenciaW
    }

    /** Lo que muestra Rodaje; [mediaW] es 0 hasta que se pedalea. */
    data class Resumen(val potencia3sW: Int, val tiempoMs: Long, val mediaW: Int, val maximaW: Int) {
        /** W/kg con [pesoKg], a un decimal. */
        fun wKg(pesoKg: Int): Double = (potencia3sW * 10.0 / pesoKg).roundToInt() / 10.0
    }

    companion object {
        const val VENTANA_MS = 3_000L
        const val HUECO_MAX_MS = 2_000L
    }
}
