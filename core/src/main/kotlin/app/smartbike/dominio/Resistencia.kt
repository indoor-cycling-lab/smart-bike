package app.smartbike.dominio

import app.smartbike.protocolo.FecCodec
import kotlin.math.abs
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

/** Terreno que simula el Neo: sin Zwift, solo la pendiente base (F7.1); con Zwift, lo que envía (F8.2). */
data class Terreno(val pendientePct: Double, val crr: Double? = null, val cw: Double? = null, val vientoMs: Double? = null)

/** Lo que se envía al Neo: una pendiente (FE-C 0x33) o una potencia (FE-C 0x31). */
sealed interface ComandoRodillo {
    data class Pendiente(val pct: Double) : ComandoRodillo
    data class Potencia(val vatios: Int) : ComandoRodillo
}

/**
 * El comando dentro del rango de FE-C ([FecCodec]). Una plataforma puede pedir hasta ±327 % (FTMS) y el modelo físico
 * pasar de 4000 W; fuera de rango, `FecCodec` lanzaría una excepción.
 */
fun ComandoRodillo.limitado(): ComandoRodillo = when (this) {
    is ComandoRodillo.Pendiente -> when {
        pct.isNaN() -> ComandoRodillo.Pendiente(0.0)
        pct in FecCodec.PENDIENTE_MIN..FecCodec.PENDIENTE_MAX -> this
        else -> ComandoRodillo.Pendiente(pct.coerceIn(FecCodec.PENDIENTE_MIN, FecCodec.PENDIENTE_MAX))
    }

    is ComandoRodillo.Potencia -> if (vatios in 0..FecCodec.POTENCIA_MAX) this else ComandoRodillo.Potencia(vatios.coerceIn(0, FecCodec.POTENCIA_MAX))
}

/** Marcha + terreno + cadencia → comando (F6, arquitectura.md § 5.5). */
interface ModeloResistencia {
    fun comando(terreno: Terreno, cadenciaRpm: Double, marcha: EstadoMarcha): ComandoRodillo
}

/**
 * Modelo simple (F6.1): cada marcha por encima de [marchaNeutra] suma [incrementoPct] a la pendiente del terreno, y
 * cada marcha por debajo la resta. La neutra va aparte de la de referencia para poder mover esta sin cambiar la
 * resistencia de cada marcha (design.md del change `marchas-sin-zwift`, decisión 6).
 *
 * Con platos y piñones, el ajuste es proporcional al logaritmo de la relación, 0 en el centro geométrico del
 * desarrollo y normalizado para que de la más ligera a la más dura haya lo mismo que de la 1 a la 24 de la lineal
 * (design.md del change `transmisiones-de-marca`, decisión 5).
 */
class ModeloSimple(
    val incrementoPct: Double,
    val marchaNeutra: Int,
    val transmision: Transmision = Transmision.Lineal(),
) : ModeloResistencia {
    private val relacionNeutra: Double
    private val paso: Double

    init {
        val t = transmision as? Transmision.Platos
        val rMin = t?.masLigera?.relacion ?: 1.0
        val rMax = t?.masDura?.relacion ?: 1.0
        relacionNeutra = sqrt(rMin * rMax)
        paso = ln(rMax / rMin) / (MARCHAS_EQUIVALENTES - 1)
    }

    /** Lo que la marcha suma a la pendiente del terreno. */
    fun ajuste(marcha: EstadoMarcha): Double = when (marcha) {
        is EstadoMarcha.Numerada -> (marcha.posicion - marchaNeutra) * incrementoPct
        is EstadoMarcha.PlatoPinon -> incrementoPct * ln(marcha.relacion / relacionNeutra) / paso
    }

    override fun comando(terreno: Terreno, cadenciaRpm: Double, marcha: EstadoMarcha): ComandoRodillo = ComandoRodillo.Pendiente(terreno.pendientePct + ajuste(marcha))

    companion object {
        /** El recorrido de una transmisión de platos equivale al de la lineal por defecto, aunque la lineal cambie. */
        const val MARCHAS_EQUIVALENTES = 24
    }
}

/** Las marchas de [this] de la más ligera a la más dura: el orden de las filas de la tabla (F6.2). */
fun Transmision.escalera(): List<EstadoMarcha> = when (this) {
    is Transmision.Lineal -> (1..marchas).map { marcha(it) }
    is Transmision.Platos -> ordenadas
}

/** El ajuste del modelo simple en cada marcha de su transmisión: la tabla de la que parte el modelo por tabla. */
fun ModeloSimple.tablaPorDefecto(): List<Double> = transmision.escalera().map(::ajuste)

/**
 * Modelo por tabla (F6.2): cada marcha suma su propio ajuste a la pendiente del terreno. [tabla] va en el orden de
 * [escalera] de la transmisión (design.md del change `modelos-tabla-y-fisico`, decisión 2).
 */
class ModeloTabla(val tabla: List<Double>, val transmision: Transmision = Transmision.Lineal()) : ModeloResistencia {
    private val escalera = transmision.escalera()

    init {
        require(tabla.size == escalera.size) { "Una entrada por marcha: ${tabla.size} de ${escalera.size}" }
    }

    fun ajuste(marcha: EstadoMarcha): Double = tabla[escalera.indexOf(marcha).coerceAtLeast(0)]

    override fun comando(terreno: Terreno, cadenciaRpm: Double, marcha: EstadoMarcha): ComandoRodillo = ComandoRodillo.Pendiente(terreno.pendientePct + ajuste(marcha))
}

/**
 * Modelo físico (F6.3): la potencia de equilibrio a la velocidad que dan la cadencia y la relación de la marcha,
 * con el terreno de Zwift (design.md del change `modelos-tabla-y-fisico`, decisión 3). No filtra nada: el suavizado, la
 * rampa y las protecciones viven en [ControlRodillo].
 */
class ModeloFisico(
    val pesoCiclistaKg: Double,
    val pesoBiciKg: Double,
    val transmision: Transmision = Transmision.Lineal(),
) : ModeloResistencia {
    private val masaKg = pesoCiclistaKg + pesoBiciKg

    /** La relación de la marcha; la lineal, repartida geométricamente entre las de la Shimano 105 del usuario. */
    fun relacion(marcha: EstadoMarcha): Double = when (marcha) {
        is EstadoMarcha.PlatoPinon -> marcha.relacion
        is EstadoMarcha.Numerada -> RELACION_MIN * (RELACION_MAX / RELACION_MIN).pow((marcha.posicion - 1.0) / (marcha.total - 1))
    }

    fun velocidadMs(cadenciaRpm: Double, marcha: EstadoMarcha): Double = cadenciaRpm * relacion(marcha) * CIRCUNFERENCIA_M / 60.0

    fun potenciaW(terreno: Terreno, cadenciaRpm: Double, marcha: EstadoMarcha): Double {
        val v = velocidadMs(cadenciaRpm, marcha)
        val theta = atan(terreno.pendientePct / 100.0)
        val crr = terreno.crr ?: CRR_POR_DEFECTO
        val cw = terreno.cw ?: CW_POR_DEFECTO
        val relativa = v + (terreno.vientoMs ?: 0.0)
        val fuerza = masaKg * GRAVEDAD * (sin(theta) + crr * cos(theta)) + cw * relativa * abs(relativa)
        return (v * fuerza / RENDIMIENTO).coerceAtLeast(0.0)
    }

    override fun comando(terreno: Terreno, cadenciaRpm: Double, marcha: EstadoMarcha): ComandoRodillo = ComandoRodillo.Potencia(potenciaW(terreno, cadenciaRpm, marcha).roundToInt())

    companion object {
        const val CIRCUNFERENCIA_M = 2.096
        const val RENDIMIENTO = 0.975
        const val GRAVEDAD = 9.81
        const val CRR_POR_DEFECTO = 0.004
        const val CW_POR_DEFECTO = 0.51

        /** 36×36 y 52×11, el recorrido de la Shimano 105. */
        const val RELACION_MIN = 1.0
        val RELACION_MAX = 52.0 / 11.0
    }
}

/**
 * Constantes de las protecciones del modelo físico, juntas para afinarlas tras la prueba con el Neo (design.md del
 * change `modelos-tabla-y-fisico`, decisión 4). Todas provisionales.
 */
object ProteccionesFisico {
    const val TAU_CADENCIA_MS = 1000.0
    const val TAU_PENDIENTE_MS = 2000.0
    const val CADENCIA_BAJA_RPM = 40.0
    const val SUBIDA_W_S = 100.0
    const val BAJADA_W_S = 200.0
    const val VENTANA_ENVIO_MS = 250L
    const val UMBRAL_W = 2

    /** Peso de la muestra nueva en una media móvil exponencial de constante [tauMs]. */
    fun alfa(dtMs: Long, tauMs: Double): Double = 1.0 - exp(-dtMs / tauMs)
}

/**
 * Distancia de la sesión (F11.3): integra la velocidad del modelo físico mientras se pedalea. Cada muestra cuenta como
 * mucho [MAX_PASO_MS], para que un hueco en la telemetría no sume kilómetros que no se han recorrido.
 */
class Recorrido {
    private var ultimoMs: Long? = null

    var distanciaM: Double = 0.0
        private set

    /** Una muestra con la [velocidadMs] del físico, o `null` si no se calcula; entonces la distancia se conserva. */
    fun muestra(tMs: Long, velocidadMs: Double?) {
        val antes = ultimoMs
        ultimoMs = tMs
        if (antes == null || velocidadMs == null || velocidadMs <= 0.0) return
        distanciaM += velocidadMs * (tMs - antes).coerceIn(0L, MAX_PASO_MS) / 1000.0
    }

    /** Tras un hueco sin velocidad (otro modelo, Neo ausente) no se integra el tiempo transcurrido. */
    fun sinVelocidad(tMs: Long) {
        ultimoMs = tMs
    }

    companion object {
        const val MAX_PASO_MS = 2000L
    }
}
