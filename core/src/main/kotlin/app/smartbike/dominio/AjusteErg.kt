package app.smartbike.dominio

/** Qué hace una acción de marcha o de vatios en ERG, de Zwift o manual. */
sealed interface ResultadoAjuste {
    /** La potencia cambia en [diferenciaW] (recortada por los límites); [ajusteW] es el ajuste nuevo. */
    data class Cambiado(val ajusteW: Int, val diferenciaW: Int) : ResultadoAjuste

    /** □: se quita el ajuste y se vuelve a la potencia de Zwift, o a la de entrada en ERG manual. */
    data object Quitado : ResultadoAjuste

    /** Ya estaba en el límite en el sentido pedido. */
    data class Tope(val potenciaW: Int, val maximo: Boolean) : ResultadoAjuste

    /** La acción no cambia nada en ERG (más dura o más ligera, plato, o □ sin ajuste). */
    data object SinCambio : ResultadoAjuste
}

/**
 * Palancas en ERG (F7.2, F8.3; design.md del change `puente-zwift-wifi`, decisión 7, y del change `erg-manual`,
 * decisión 1): las acciones de marcha ajustan la potencia en [PASO_W] por marcha, en el mismo sentido, «+10 W» y
 * «−10 W» en [PASO_W], y □ quita el ajuste. [objetivoW] es lo que pide Zwift o la potencia de entrada del ERG manual.
 * La potencia resultante queda entre [MIN_W] y [MAX_W], los límites del control manual.
 */
object AjusteErg {
    const val PASO_W = 10
    const val MIN_W = 50
    const val MAX_W = 1000

    fun limitar(vatios: Int): Int = vatios.coerceIn(MIN_W, MAX_W)

    fun aplicar(accion: Accion, objetivoW: Int, ajusteW: Int): ResultadoAjuste {
        val delta = when (accion) {
            is Accion.MarchaMas -> accion.n * PASO_W

            is Accion.MarchaMenos -> -accion.n * PASO_W

            Accion.ErgMas -> PASO_W

            Accion.ErgMenos -> -PASO_W

            Accion.MarchaReferencia -> return if (ajusteW == 0) ResultadoAjuste.SinCambio else ResultadoAjuste.Quitado

            Accion.MarchaMasDura, Accion.MarchaMasLigera -> return ResultadoAjuste.SinCambio

            // El plato no ajusta la potencia (spec `zwift-bridge`, «Plato en ERG»).
            Accion.PlatoMas, Accion.PlatoMenos -> return ResultadoAjuste.SinCambio

            // Fijar la referencia no toca la potencia: lo resuelve la Sesion con la marcha (spec `zwift-bridge`).
            Accion.FijarReferencia -> return ResultadoAjuste.SinCambio

            // Alternar lo resuelve la Sesion con ControlRodillo, y cambiar de pantalla, la UI; las funciones de Zwift van
            // por el mando, antes del camino de ERG.
            Accion.AlternarErgManual, Accion.PantallaAnterior, Accion.PantallaSiguiente, Accion.SiguienteModelo, Accion.VentiladorAlternar, Accion.VentiladorMas, Accion.VentiladorMenos, is Accion.Mando -> return ResultadoAjuste.SinCambio
        }
        val actual = limitar(objetivoW + ajusteW)
        val nueva = limitar(actual + delta)
        if (nueva == actual) return ResultadoAjuste.Tope(actual, maximo = delta > 0)
        return ResultadoAjuste.Cambiado(ajusteW = nueva - objetivoW, diferenciaW = nueva - actual)
    }
}
