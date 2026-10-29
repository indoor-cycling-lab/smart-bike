package app.smartbike.dominio

import app.smartbike.protocolo.BotonMando
import app.smartbike.protocolo.MandoZwiftCodec
import app.smartbike.protocolo.TablaBotones

/**
 * Las funciones de Zwift pulsadas en el mando emulado y su trama de botones (design.md del change `mando-zwift-emulado`,
 * decisión 2). Cuenta cuántas entradas mantienen cada función: con la misma función en dos botones, soltar uno no la
 * suelta. Inmutable y puro.
 */
class EstadoMandoZwift private constructor(private val cuentas: Map<FuncionZwift, Int>) {

    constructor() : this(emptyMap())

    val pulsadas: Set<FuncionZwift> get() = cuentas.keys

    fun pulsar(f: FuncionZwift): EstadoMandoZwift = EstadoMandoZwift(cuentas + (f to (cuentas[f] ?: 0) + 1))

    /** Soltar una función que no está pulsada no cambia nada. */
    fun soltar(f: FuncionZwift): EstadoMandoZwift {
        val n = cuentas[f] ?: return this
        return EstadoMandoZwift(if (n > 1) cuentas + (f to n - 1) else cuentas - f)
    }

    fun soltarTodo(): EstadoMandoZwift = EstadoMandoZwift()

    /**
     * Los giros que Zwift ve ahora: el de una palanca no cuenta si esa misma palanca tiene el freno (gana el freno, como
     * en [trama]). Es lo que indica Rodaje (spec `ride-screen`, «Aviso de giro»).
     */
    fun giro(): Set<LadoGiro> = buildSet {
        if (FuncionZwift.GIRAR_IZQUIERDA in cuentas && FuncionZwift.FRENO_IZQUIERDO !in cuentas) add(LadoGiro.IZQUIERDA)
        if (FuncionZwift.GIRAR_DERECHA in cuentas && FuncionZwift.FRENO_DERECHO !in cuentas) add(LadoGiro.DERECHA)
    }

    /**
     * `RideKeyPadStatus` con la tabla `ENUM_MANUAL` y las dos palancas siempre presentes en el campo 3, a 0 si están
     * sueltas: Zwift conserva el último valor de cada una (arquitectura.md § 7.4). Palanca 0: −100 gira a la izquierda,
     * +100 frena; palanca 1: +100 gira a la derecha, −100 frena. Con el giro y el freno de la misma palanca, gana el
     * freno, que es la acción segura.
     */
    fun trama(): ByteArray {
        val botones = pulsadas.mapNotNullTo(mutableSetOf()) { BOTONES[it] }
        val izquierda = when {
            FuncionZwift.FRENO_IZQUIERDO in cuentas -> PALANCA
            FuncionZwift.GIRAR_IZQUIERDA in cuentas -> -PALANCA
            else -> 0
        }
        val derecha = when {
            FuncionZwift.FRENO_DERECHO in cuentas -> -PALANCA
            FuncionZwift.GIRAR_DERECHA in cuentas -> PALANCA
            else -> 0
        }
        return MandoZwiftCodec.botones(botones, TablaBotones.ENUM_MANUAL, mapOf(0 to izquierda, 1 to derecha))
    }

    override fun equals(other: Any?) = other is EstadoMandoZwift && other.cuentas == cuentas

    override fun hashCode() = cuentas.hashCode()

    override fun toString() = "EstadoMandoZwift($cuentas)"

    private companion object {
        const val PALANCA = 100

        val BOTONES = mapOf(
            FuncionZwift.ARRIBA to BotonMando.ARRIBA,
            FuncionZwift.ABAJO to BotonMando.ABAJO,
            FuncionZwift.IZQUIERDA to BotonMando.IZQUIERDA,
            FuncionZwift.DERECHA to BotonMando.DERECHA,
            FuncionZwift.A to BotonMando.A,
            FuncionZwift.B to BotonMando.B,
            FuncionZwift.Y to BotonMando.Y,
            FuncionZwift.Z to BotonMando.Z,
        )
    }
}

/** Un lado del giro de Zwift. */
enum class LadoGiro { IZQUIERDA, DERECHA }
