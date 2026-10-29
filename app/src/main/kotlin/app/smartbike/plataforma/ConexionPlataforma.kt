package app.smartbike.plataforma

import app.smartbike.dominio.Compatibilidad
import app.smartbike.dominio.Enlace
import app.smartbike.dominio.FuncionMando
import app.smartbike.dominio.IdentidadDispositivo
import app.smartbike.dominio.LadoGiro
import app.smartbike.dominio.ModoConexion
import app.smartbike.dominio.NombreAnunciado
import app.smartbike.dominio.SimulacionZwift
import app.smartbike.dominio.Terreno
import app.smartbike.protocolo.FtmsCodec
import kotlinx.coroutines.flow.StateFlow

/**
 * Lo que la `Sesion` recibe de la plataforma, ya traducido al dominio y sin saber por qué modo de conexión llegó (design.md
 * del change `mando-zwift-dircon`, decisión 1). Se llama desde el hilo de control.
 */
interface ReceptorPlataforma {
    /** La plataforma manda un terreno nuevo (pendiente, Crr, CW…): F8.2. */
    fun terreno(terreno: Terreno)

    /** La plataforma pide una potencia en un entrenamiento (F8.3). */
    fun potenciaErg(vatios: Int)

    /** La plataforma ha mandado algo que no cambia lo que se simula. */
    fun sinCambio()

    /** Lo último que ha mandado la plataforma, para Diagnóstico; se avisa antes de lo anterior. */
    fun ultimoDePlataforma(ultimo: UltimoDePlataforma)
}

/**
 * Lo último que ha mandado la plataforma, según el modo: un comando FTMS o una simulación de sus marchas virtuales.
 * [Simulacion] lleva un tipo de Zwift mientras ninguna otra plataforma tenga algo parecido (design.md del change
 * `generalizar-plataformas`, riesgos).
 */
sealed interface UltimoDePlataforma {
    data class Comando(val comando: FtmsCodec.Comando) : UltimoDePlataforma
    data class Simulacion(val simulacion: SimulacionZwift) : UltimoDePlataforma
}

/**
 * Cómo está el mando que la app publica (spec `platform-settings`, «Estado del mando»). El mando solo da [Desactivado]
 * (sin publicar todavía; no hay interruptor), [Esperando] y [Conectado] (la app de entrenamiento ha saludado):
 * [SinConexion] (Bluetooth apagado o sin permiso) y [NoDisponible] (móvil sin modo periférico, NF3) los pone quien conoce
 * el transporte.
 */
enum class EstadoMando { Desactivado, SinConexion, NoDisponible, Esperando, Conectado }

/**
 * Un modo de conexión con una plataforma (spec `platform-bridge`): el rodillo y lo que se manda a la plataforma. La
 * `Sesion` solo conoce esta interfaz; cada modo es una implementación (decisión 1): [ConexionFtmsWifi] (rodillo por
 * wifi con FTMS) y las propias de cada plataforma, como `ConexionZwiftRide`. Todo se llama desde el hilo de control.
 */
interface ConexionPlataforma {
    val modo: ModoConexion

    /** Por dónde llega a la plataforma, según la tabla de compatibilidad: el wifi o el Bluetooth. */
    val enlace: Enlace get() = Compatibilidad.deModo(modo).enlace

    /** Si se anuncia como un KICKR BIKE (change `simular-kickr-bike`); fijo para cada conexión, que se rehace al cambiarlo. */
    val simularKickr: Boolean

    /** El enlace con la plataforma por DirCon: anunciado, con la plataforma conectada o sin responder en la red. */
    val estado: StateFlow<EstadoTransporte>

    /** Si la plataforma recibe telemetría. */
    val suscrito: Boolean

    /** Si la plataforma está suscrita al pulso; siempre `false` si el pulso no se publica. */
    val suscritoPulso: Boolean

    /** El mando de este modo, o `null` si no tiene (decisión 4). */
    val mando: MandoEmulado?

    /** El nombre con el que se anuncia con [identidad] (spec `platform-bridge`, «Nombre de KICKR BIKE ante la plataforma»). */
    fun nombre(identidad: IdentidadDispositivo): String = NombreAnunciado.de(identidad, simularKickr)

    /** Empieza a anunciarse (hay wifi); idempotente. */
    fun publicar(identidad: IdentidadDispositivo)

    /** Deja de anunciarse (se ha ido el wifi). */
    fun retirar()

    /** Se acaba la sesión o se sustituye este modo: se para todo. */
    fun terminar()

    /** Potencia y cadencia del Neo hacia la plataforma. */
    fun telemetria(t: TelemetriaPlataforma)

    /** El pulso de la banda hacia la plataforma. */
    fun pulso(bpm: Int)
}

/**
 * El mando que la app emula ante una plataforma, dentro del modo o aparte por Bluetooth (design.md del change
 * `generalizar-plataformas`, decisión 4). Un mando ignora las funciones que no son de su protocolo.
 */
interface MandoEmulado {
    /** El mando tal como lo ve el usuario (spec `device-connection`). */
    val estado: StateFlow<EstadoMando>

    /** Empieza (o deja) de publicarse; sin interruptor, el puente lo activa al crearlo (spec `platform-settings`, «Mando sin interruptor»). */
    fun activar(activado: Boolean)

    fun pulsar(funcion: FuncionMando)

    fun soltar(funcion: FuncionMando)

    fun soltarTodo()

    /** Los giros que la plataforma ve ahora. */
    val giro: Set<LadoGiro>

    /** Se acaba la sesión o se sustituye el modo: se para el mando. */
    fun terminar()
}
