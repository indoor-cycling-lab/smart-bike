package app.smartbike.dominio

/**
 * La configuración del ventilador tal cual la dio (design.md del change `ventilador-headwind`, decisión 2): los cuatro
 * umbrales de pulso («desde») son una vista sobre los primeros cuatro bytes, y el resto (rango de velocidad,
 * temperatura, campos de un firmware que la app no conoce) vuelve idéntico al escribirla.
 */
class ConfiguracionVentilador private constructor(private val datos: ByteArray) {
    /** Los bytes de la configuración, tal cual llegaron (copia). */
    val bytes: ByteArray get() = datos.copyOf()

    /** Los cuatro umbrales, en bpm: desde cada uno el ventilador entra en su zona. */
    val umbrales: List<Int> get() = List(ZONAS) { datos[it].toInt() and 0xFF }

    /** Los límites de la [zona] (1 a 4): entre los de sus vecinas, estrictamente crecientes, y dentro de 60–220 bpm. */
    fun limites(zona: Int): IntRange {
        val u = umbrales
        val minimo = if (zona == 1) UMBRAL_MIN else u[zona - 2] + 1
        val maximo = if (zona == ZONAS) UMBRAL_MAX else u[zona] - 1
        return minimo..maximo
    }

    /** Una copia con solo el umbral de la [zona] cambiado, recortado a sus [limites]; el resto de bytes no cambia. */
    fun conUmbral(zona: Int, bpm: Int): ConfiguracionVentilador {
        require(zona in 1..ZONAS)
        val l = limites(zona)
        val copia = datos.copyOf()
        copia[zona - 1] = bpm.coerceIn(l.first, maxOf(l.first, l.last)).toByte()
        return ConfiguracionVentilador(copia)
    }

    override fun equals(other: Any?) = other is ConfiguracionVentilador && datos.contentEquals(other.datos)
    override fun hashCode() = datos.contentHashCode()

    companion object {
        const val ZONAS = 4
        const val UMBRAL_MIN = 60
        const val UMBRAL_MAX = 220
        private const val LONGITUD_MINIMA = 8

        /** `null` si son menos de 8 bytes (el ventilador real manda 8 tras el `ST`; design.md, «Resultados de la prueba»): no se escribe una configuración que no se entiende. */
        fun desde(bytes: ByteArray): ConfiguracionVentilador? = if (bytes.size < LONGITUD_MINIMA) null else ConfiguracionVentilador(bytes.copyOf())
    }
}

enum class ModoVentilador {
    Manual,
    Pulso,
    Otro,
    ;

    companion object {
        fun deCodigo(codigo: Int): ModoVentilador = when (codigo) {
            4 -> Manual
            2 -> Pulso
            else -> Otro
        }
    }
}

/**
 * Lo que el ventilador comunica (decisión 3): nunca lo último enviado. [velocidadManual] es la última velocidad vista en
 * manual, para el aviso al volver a él.
 */
data class EstadoVentilador(
    val modo: ModoVentilador,
    val velocidad: Int,
    val velocidadManual: Int? = null,
    val configuracion: ConfiguracionVentilador? = null,
)

/** Qué manda la app al ventilador tras una acción. */
sealed interface OrdenVentilador {
    data class Modo(val modo: ModoVentilador) : OrdenVentilador
    data class Velocidad(val porcentaje: Int) : OrdenVentilador
}

/** El aviso del pie tras la acción (spec `ride-screen`, «Aviso de las acciones del ventilador»). */
sealed interface AvisoVentilador {
    data class Velocidad(val porcentaje: Int) : AvisoVentilador
    data class Manual(val porcentaje: Int?) : AvisoVentilador
    data object Automatico : AvisoVentilador
    data object EnAutomatico : AvisoVentilador
    data object SinVentilador : AvisoVentilador
}

data class ResultadoVentilador(val orden: OrdenVentilador?, val aviso: AvisoVentilador?)

/** Las reglas puras de las tres acciones (spec `fan-control`, «Acciones de los botones según el modo»). */
object ReglasVentilador {
    const val PASO = 10

    /** [estado] es `null` sin ventilador conectado; [objetivo] es la velocidad enviada aún sin confirmar. */
    fun alternar(estado: EstadoVentilador?): ResultadoVentilador = when (estado?.modo) {
        null -> SIN_VENTILADOR

        ModoVentilador.Manual -> ResultadoVentilador(OrdenVentilador.Modo(ModoVentilador.Pulso), AvisoVentilador.Automatico)

        ModoVentilador.Pulso, ModoVentilador.Otro ->
            ResultadoVentilador(OrdenVentilador.Modo(ModoVentilador.Manual), AvisoVentilador.Manual(estado.velocidadManual))
    }

    fun masVelocidad(estado: EstadoVentilador?, objetivo: Int?): ResultadoVentilador = paso(estado, objetivo, PASO)

    fun menosVelocidad(estado: EstadoVentilador?, objetivo: Int?): ResultadoVentilador = paso(estado, objetivo, -PASO)

    private fun paso(estado: EstadoVentilador?, objetivo: Int?, delta: Int): ResultadoVentilador = when (estado?.modo) {
        null -> SIN_VENTILADOR

        ModoVentilador.Pulso -> ResultadoVentilador(null, AvisoVentilador.EnAutomatico)

        ModoVentilador.Otro -> ResultadoVentilador(null, null)

        ModoVentilador.Manual -> {
            val nueva = ((objetivo ?: estado.velocidad) + delta).coerceIn(0, 100)
            ResultadoVentilador(OrdenVentilador.Velocidad(nueva), AvisoVentilador.Velocidad(nueva))
        }
    }

    private val SIN_VENTILADOR = ResultadoVentilador(null, AvisoVentilador.SinVentilador)
}
