package app.smartbike.sesion

import app.smartbike.dominio.Accion
import app.smartbike.dominio.AvisoVentilador
import app.smartbike.dominio.ComandoRodillo
import app.smartbike.dominio.DatosSesion
import app.smartbike.dominio.DispositivoConfigurado
import app.smartbike.dominio.Enlace
import app.smartbike.dominio.EnlaceGuia
import app.smartbike.dominio.Entrada
import app.smartbike.dominio.EstadoMarcha
import app.smartbike.dominio.EstadoVentilador
import app.smartbike.dominio.Gesto
import app.smartbike.dominio.GuiaInicio
import app.smartbike.dominio.LadoGiro
import app.smartbike.dominio.ModoBajadas
import app.smartbike.dominio.ModoCambio
import app.smartbike.dominio.ModoConexion
import app.smartbike.dominio.ModoControl
import app.smartbike.dominio.NombreAnunciado
import app.smartbike.dominio.PasosGuia
import app.smartbike.dominio.ResultadoAjuste
import app.smartbike.dominio.ResultadoCambio
import app.smartbike.dominio.ServiciosPublicados
import app.smartbike.dominio.SimulacionZwift
import app.smartbike.dominio.TipoModelo
import app.smartbike.dominio.TopeMarcha
import app.smartbike.dominio.Transmision
import app.smartbike.plataforma.EstadoMando
import app.smartbike.plataforma.EstadoPlataforma
import app.smartbike.protocolo.FtmsCodec

/**
 * Estado de un dispositivo tal como lo ve el usuario (spec `device-connection`). `Conectando`: intento con el
 * configurado al abrir la app o al volver el Bluetooth, o tras encontrarlo al buscar; `Reconectando`: intento tras perder
 * la conexión (design.md del change `conexion-automatica-en-segundo-plano`, decisión 3).
 */
enum class EstadoDispositivo { SinConexion, Buscando, Conectando, Reconectando, Conectado, NoEncontrado }

/** Envío manual desde Diagnóstico, con la hora de reloj para mostrarla. */
sealed interface Envio {
    val horaMs: Long

    data class Pendiente(val pct: Double, override val horaMs: Long) : Envio
    data class Potencia(val vatios: Int, override val horaMs: Long) : Envio
}

/**
 * El último gesto con lo que produjo: [accion] `null` si no tiene ninguna; [resultado] `null` sin acción o en ERG de
 * Zwift, donde lo que produjo es [ajuste].
 */
data class GestoReconocido(
    val gesto: Gesto,
    val accion: Accion?,
    val resultado: ResultadoCambio?,
    val ajuste: ResultadoAjuste? = null,
)

/** El último mensaje de la plataforma por el punto de control, con la hora de reloj. */
data class MensajePlataforma(val comando: FtmsCodec.Comando, val horaMs: Long)

/** Lo último que la plataforma ha mandado en sus `SET` de marchas virtuales, acumulado, con la hora de reloj (change `mando-zwift-dircon`). */
data class SimulacionPlataformaRecibida(val simulacion: SimulacionZwift, val horaMs: Long, val instanteMs: Long)

/**
 * Lo que muestra la sección Simulación de Diagnóstico (F13.3): cómo se traduce la marcha en resistencia, quién manda
 * en el Neo ([modo]) y lo último que ha pedido Zwift.
 */
data class Simulacion(
    val pendienteBasePct: Double,
    val ajustePct: Double,
    val enviado: ComandoRodillo?,
    val modo: ModoControl = ModoControl.SimLibre,
    val plataformaEnEspera: Boolean = false,
    val ultimoPlataforma: MensajePlataforma? = null,
    val ultimaSimulacionPlataforma: SimulacionPlataformaRecibida? = null,
    val bajadas: ModoBajadas = ModoBajadas.LA_MARCHA_NO_CREA_BAJADAS,
    /** La pendiente que daría el modelo sin el límite de [bajadas]; `null` en ERG o en control manual. */
    val sinLimitePct: Double? = null,
    /** El modelo elegido y la dificultad (F6); con el físico, lo que calcula (spec `diagnostics`, «Modelo físico en la simulación»). */
    val tipoModelo: TipoModelo = TipoModelo.SIMPLE,
    val dificultadPct: Int = 100,
    val fisico: FisicoSim? = null,
)

/** Lo que muestra Diagnóstico del modelo físico: la potencia sin y con límites, la mínima, la cadencia suavizada y la velocidad. */
data class FisicoSim(
    val calculadaW: Int,
    val minimaW: Int,
    val cadenciaSuaveRpm: Int,
    val velocidadKmh: Double?,
    val relacion: Double,
)

data class EstadoRodaje(
    val neo: EstadoDispositivo = EstadoDispositivo.SinConexion,
    val square: EstadoDispositivo = EstadoDispositivo.SinConexion,
    val bluetoothEncendido: Boolean = true,
    /** Botones pulsados ahora; `null` sin Square conectado. */
    val mascara: UInt? = null,
    val marcha: EstadoMarcha,
    val referencia: EstadoMarcha,
    /** La transmisión elegida y, con platos, su modo de cambio (spec `ride-screen`, «Modo y transmisión»). */
    val transmision: Transmision = Transmision.Lineal(),
    val modoCambio: ModoCambio = ModoCambio.AUTOMATICO,
    /** Potencia (W) y cadencia (rpm) del Neo; `null` sin Neo conectado o sin datos todavía. */
    val potencia: Int? = null,
    val cadencia: Int? = null,
    val ultimoGesto: GestoReconocido? = null,
    val simulacion: Simulacion,
    /** De la notificación de la palanca a la entrega del comando al Neo, del último cambio con palanca (NF1). */
    val latenciaMs: Long? = null,
    val ultimoEnvio: Envio? = null,
    val plataforma: EstadoPlataforma = EstadoPlataforma.SinWifi,
    /** El mando emulado ante la plataforma (design.md del change `mando-zwift-emulado`, decisión 6). */
    val mando: EstadoMando = EstadoMando.Desactivado,
    /** Lo que se publica según el modo y lo configurado; `null` sin rodillo configurado (design.md del change `pestana-conexion`, decisión 3). */
    val servicios: ServiciosPublicados? = null,
    /** Si la app de entrenamiento usa el rodillo (suscrita a su telemetría) y el pulso (decisión 6). */
    val usoRodillo: Boolean = false,
    val usoPulso: Boolean = false,
    /** El modo de conexión con la plataforma en uso (design.md del change `mando-zwift-dircon`, decisión 6). */
    val modoConexion: ModoConexion = ModoConexion.FTMS_WIFI,
    /** Por dónde llega a la plataforma el modo de [modoConexion] (Diagnóstico no mira el mando por Bluetooth). */
    val enlaceConexion: Enlace = Enlace.WIFI,
    /** El nombre con el que la app se anuncia ahora ante la plataforma (change `simular-kickr-bike`; spec `diagnostics`). */
    val nombreAnunciado: String = NombreAnunciado.NORMAL,
    /** Potencia de 3 s, tiempo, media y máxima de la sesión (spec `ride-screen`, «Datos de la sesión»). */
    val datos: DatosSesion.Resumen = DatosSesion.Resumen(0, 0, 0, 0),
    /** Los configurados en `estado.json`, para Dispositivos y «Olvidar»; `null` si no hay ninguno de ese tipo. */
    val neoConfigurado: DispositivoConfigurado? = null,
    val squareConfigurado: DispositivoConfigurado? = null,
    /** El pulsómetro (spec `heart-rate`): opcional, sin configurado no se muestra nada de él. */
    val pulsometro: EstadoDispositivo = EstadoDispositivo.SinConexion,
    val pulsometroConfigurado: DispositivoConfigurado? = null,
    /** El pulso válido en bpm; `null` si no hay (spec `heart-rate`, «Pulso válido»). Rodaje no lo muestra. */
    val pulso: Int? = null,
    /** El instante monotónico de la última lectura válida de [pulso], para su antigüedad en Diagnóstico. */
    val pulsoInstanteMs: Long? = null,
    /** El ventilador (spec `fan-control`): opcional, sin configurado no se muestra nada de él. */
    val ventilador: EstadoDispositivo = EstadoDispositivo.SinConexion,
    val ventiladorConfigurado: DispositivoConfigurado? = null,
    /** Lo que el ventilador ha comunicado (no lo último enviado); `null` sin ninguna notificación. */
    val ventiladorEstado: EstadoVentilador? = null,
    /** El instante monotónico de la última notificación del ventilador, para su antigüedad en Diagnóstico. */
    val ventiladorInstanteMs: Long? = null,
    /** Último nivel válido de la batería del Square (%); `null` sin lectura o sin Square configurado (spec `square-battery`). */
    val bateriaSquare: Int? = null,
    /** Los giros de Zwift pulsados ahora o hace menos de [Sesion.GIRO_MINIMO_MS] (spec `ride-screen`, «Aviso de giro»). */
    val giro: Set<LadoGiro> = emptySet(),
    /** Del modelo físico (decisión 12 de `funcional.md`): `null` con otro modelo o sin Neo; la distancia se conserva. */
    val velocidadKmh: Double? = null,
    val distanciaKm: Double = 0.0,
    /** Lo que recuerda `estado.json` de la guía de inicio; `null` hasta que se lee (change `guia-inicio`). */
    val guiaInicio: GuiaInicio? = null,
)

/** Cómo está un dispositivo para la guía de inicio: configurado o no, y en qué estado de conexión. */
private fun enlaceGuia(e: EstadoDispositivo, configurado: Boolean): EnlaceGuia = when {
    e == EstadoDispositivo.Conectado -> EnlaceGuia.CONECTADO
    e == EstadoDispositivo.Buscando || e == EstadoDispositivo.Conectando || e == EstadoDispositivo.Reconectando -> EnlaceGuia.CONECTANDO
    configurado -> EnlaceGuia.SIN_CONEXION
    else -> EnlaceGuia.NO_CONFIGURADO
}

/**
 * Los pasos de la guía de inicio con lo que la app ya sabe (spec `getting-started-guide`): el Neo y el Square, y si hay una
 * plataforma conectada que usa el rodillo, igual que el estado de la plataforma en Rodaje.
 */
fun pasosGuia(e: EstadoRodaje): PasosGuia = PasosGuia.de(
    rodillo = enlaceGuia(e.neo, e.neoConfigurado != null),
    controlador = enlaceGuia(e.square, e.squareConfigurado != null),
    plataformaConectada = e.plataforma is EstadoPlataforma.Conectado && e.usoRodillo,
)

/** Efectos puntuales para la UI (arquitectura.md § 9.2): no son estado, para que no se repitan al recomponer. */
sealed interface EventoRodaje {
    data class Cambio(val accion: Accion, val cambio: ResultadoCambio.Cambiada) : EventoRodaje
    data class Tope(val tope: TopeMarcha) : EventoRodaje

    /**
     * En ERG, una acción que ajusta la potencia ([resultado] `Cambiado` o `Quitado`). [entradaW] es la potencia de
     * entrada en ERG manual, a la que vuelve `Quitado`, y `null` en ERG de Zwift.
     */
    data class Ajuste(val accion: Accion, val resultado: ResultadoAjuste, val entradaW: Int? = null) : EventoRodaje

    /** Resultado de una acción del ventilador, para el aviso del pie (spec `ride-screen`). */
    data class Ventilador(val aviso: AvisoVentilador) : EventoRodaje

    /** En ERG, la potencia ya está en su límite. */
    data class TopePotencia(val potenciaW: Int) : EventoRodaje

    /** Se ha entrado en ERG manual a [potenciaW] ([activo]) o se ha vuelto a Libre o a SIM ([libre], según haya o no terreno de la plataforma; spec `manual-erg`). */
    data class ModoErg(val activo: Boolean, val potenciaW: Int, val libre: Boolean = true) : EventoRodaje

    /** Se ha pedido alternar en un entrenamiento de Zwift, donde manda Zwift. */
    data object ErgPlataformaActivo : EventoRodaje

    /** La marcha puesta pasa a ser la de referencia. */
    data class ReferenciaFijada(val marcha: EstadoMarcha) : EventoRodaje

    /** La pestaña siguiente ([siguiente]) o la anterior (design.md del change `rodaje-completo-y-ajustes`, decisión 4). */
    data class Pantalla(val siguiente: Boolean) : EventoRodaje

    /** Otro modelo de resistencia, elegido con la acción «Siguiente modelo». */
    data class ModeloCambiado(val modelo: TipoModelo) : EventoRodaje
}

/**
 * Captura de una entrada en Asignar acción (design.md del change `gestos-y-asignacion`, decisión 7): mientras dura,
 * los botones del Square no ejecutan acciones.
 */
sealed interface EstadoCaptura {
    data object SinSquare : EstadoCaptura
    data object Esperando : EstadoCaptura
    data class Capturada(val entrada: Entrada) : EstadoCaptura
}

/**
 * Vibración del móvil; la lanza la `Sesion` para que funcione con la pantalla apagada. Una por gesto (decisión 9):
 * corta al pulsar, en la corta y al repetir; larga en la larga; triple en la doble; doble en los topes.
 */
interface Vibrador {
    fun corta()
    fun doble()
    fun larga()
    fun triple()
}
