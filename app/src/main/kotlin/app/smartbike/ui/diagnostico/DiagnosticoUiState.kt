package app.smartbike.ui.diagnostico

import app.smartbike.datos.EntradaRegistro
import app.smartbike.datos.Origen
import app.smartbike.datos.Sentido
import app.smartbike.dominio.Accion
import app.smartbike.dominio.BateriaSquare
import app.smartbike.dominio.ComandoRodillo
import app.smartbike.dominio.Enlace as EnlaceConexion
import app.smartbike.dominio.EstadoMarcha
import app.smartbike.dominio.EstadoVentilador
import app.smartbike.dominio.ModoBajadas
import app.smartbike.dominio.ModoControl
import app.smartbike.dominio.NombreAnunciado
import app.smartbike.dominio.ResultadoAjuste
import app.smartbike.dominio.ResultadoCambio
import app.smartbike.dominio.Terreno
import app.smartbike.dominio.TipoGesto
import app.smartbike.dominio.TipoModelo
import app.smartbike.dominio.TopeMarcha
import app.smartbike.plataforma.EstadoPlataforma
import app.smartbike.protocolo.SquareButton
import app.smartbike.sesion.Envio
import app.smartbike.sesion.EstadoDispositivo
import app.smartbike.sesion.EstadoRodaje
import app.smartbike.sesion.FisicoSim
import app.smartbike.sesion.MensajePlataforma
import app.smartbike.ui.NOMBRE_APP_ENTRENAMIENTO
import app.smartbike.ui.componentes.FilaHex
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.abs

/** Lo que dice la fila del Square de su batería (spec `diagnostics`, «Batería del Square en Datos de conexión»). */
sealed interface BateriaFila {
    data object SinLectura : BateriaFila
    data class Nivel(val pct: Int, val baja: Boolean) : BateriaFila
    data class Ultima(val pct: Int) : BateriaFila
}

/** Nada si el Square nunca se ha conectado: sin Square conectado solo se recuerda el último nivel válido, si lo hay. */
fun bateriaFila(square: EstadoDispositivo, nivel: Int?): BateriaFila? = when {
    square == EstadoDispositivo.Conectado ->
        if (nivel == null) BateriaFila.SinLectura else BateriaFila.Nivel(nivel, BateriaSquare.baja(nivel))

    nivel != null -> BateriaFila.Ultima(nivel)

    else -> null
}

/**
 * La fila del pulsómetro en Datos de conexión (spec `diagnostics`, «Pulso en Datos de conexión»): solo con una banda
 * configurada. [bpm] e [instanteMs] (monotónico) son `null` si no hay pulso, también con la banda sin conexión.
 */
data class FilaPulsometroUi(val bpm: Int?, val instanteMs: Long?)

/**
 * La fila del ventilador en Datos de conexión (spec `diagnostics`, «Ventilador en Datos de conexión»): solo con uno
 * configurado, con el modo y la velocidad que él comunicó ([estadoVentilador] es `null` sin ninguna notificación) y la
 * hora de la última, también con el ventilador sin conexión.
 */
data class FilaVentiladorUi(val estadoVentilador: EstadoVentilador?, val instanteMs: Long?)

/**
 * Lo último que Zwift ha mandado en su simulación (spec `diagnostics`, «Simulación de Zwift en Simulación»): lo que
 * falte, `null`. [instanteMs] es el reloj monotónico de la sesión.
 */
data class FilaSimulacionZwiftUi(
    val pendientePct: Double?,
    val relacionMarcha: Double?,
    val crr: Double?,
    val cw: Double?,
    val masaCiclistaKg: Double?,
    val masaBiciKg: Double?,
    val instanteMs: Long,
)

/** Segundos desde la lectura de [instanteMs], sin bajar de 0 (el reloj de [ahoraMs] es el monotónico de la sesión). */
fun pulsoHaceS(instanteMs: Long, ahoraMs: Long): Int = ((ahoraMs - instanteMs) / 1000).coerceAtLeast(0).toInt()

/** Valores elegidos en el control manual. La pendiente va en medios puntos para no acumular errores de coma flotante. */
data class ControlManual(val pendienteMedios: Int = 0, val potencia: Int = POTENCIA_INICIAL) {
    val pendiente: Double get() = pendienteMedios / 2.0
    val puedeBajarPendiente get() = pendienteMedios > PENDIENTE_MIN_MEDIOS
    val puedeSubirPendiente get() = pendienteMedios < PENDIENTE_MAX_MEDIOS
    val puedeBajarPotencia get() = potencia > POTENCIA_MIN
    val puedeSubirPotencia get() = potencia < POTENCIA_MAX

    fun pendiente(delta: Int) = copy(pendienteMedios = (pendienteMedios + delta).coerceIn(PENDIENTE_MIN_MEDIOS, PENDIENTE_MAX_MEDIOS))
    fun potencia(delta: Int) = copy(potencia = (potencia + delta * POTENCIA_PASO).coerceIn(POTENCIA_MIN, POTENCIA_MAX))

    companion object {
        // Spec trainer-manual-control, «Límites del control manual».
        const val PENDIENTE_MIN_MEDIOS = -20 // −10,0 %
        const val PENDIENTE_MAX_MEDIOS = 40 // +20,0 %
        const val POTENCIA_MIN = 50
        const val POTENCIA_MAX = 1000
        const val POTENCIA_PASO = 10
        const val POTENCIA_INICIAL = 150
    }
}

/** Lo que produjo el último gesto: una acción (con lo que cambió de verdad), un tope o nada. */
sealed interface ResultadoGestoUi {
    data class Hecha(val accion: Accion, val diferencia: Int?) : ResultadoGestoUi

    /** Con platos, una acción que cambió el plato: «Plato 52» (spec `diagnostics`, «Plato con la Shimano»). */
    data class Plato(val dientes: Int) : ResultadoGestoUi
    data class Tope(val tope: TopeMarcha) : ResultadoGestoUi

    /** En ERG: «+10 W», «Potencia de Zwift», «Vuelta a 200 W» (ERG manual) o «Tope · 50 W». */
    data class Vatios(val diferenciaW: Int) : ResultadoGestoUi
    data object PotenciaZwift : ResultadoGestoUi
    data class VueltaA(val potenciaW: Int) : ResultadoGestoUi
    data class TopePotencia(val potenciaW: Int) : ResultadoGestoUi
    data object SinAccion : ResultadoGestoUi
}

/** «Campag. D + Palanca D1 · pulsar · +3 marchas»: botones con el modificador primero, gesto y resultado. */
data class UltimoGestoUi(val botones: List<SquareButton>, val gesto: TipoGesto, val resultado: ResultadoGestoUi)

/** Quién manda en el rodillo, como se nombra en la sección Simulación. */
enum class ModoUi { Sim, SimPlataforma, ErgPlataforma, ErgManual, Manual }

/**
 * Sección Simulación (spec `diagnostics`). [enviado] y [latenciaMs] son `null` si no hay dato. Con Zwift, [terreno] es
 * el de su ruta (y sustituye a la pendiente base) y, en ERG, [potenciaPlataformaW] y [ajustePalancasW] lo que pide y el
 * ajuste de las palancas. En ERG manual, [potenciaFijadaW] y [entradaW] (spec `diagnostics`, «ERG manual»).
 */
data class SimulacionUi(
    /** La marcha: «17 de 24», o «52×17» con su relación. */
    val marcha: EstadoMarcha = EstadoMarcha.Numerada(12, 24),
    val pendienteBasePct: Double = 0.0,
    val ajustePct: Double = 0.0,
    val enviado: ComandoRodillo? = null,
    val latenciaMs: Long? = null,
    val modo: ModoUi = ModoUi.Sim,
    val terreno: Terreno? = null,
    val potenciaPlataformaW: Int? = null,
    val ajustePalancasW: Int? = null,
    val potenciaFijadaW: Int? = null,
    val entradaW: Int? = null,
    val plataformaEnEspera: Boolean = false,
    val ultimoPlataforma: MensajePlataforma? = null,
    val bajadas: ModoBajadas = ModoBajadas.LA_MARCHA_NO_CREA_BAJADAS,
    /** La pendiente sin el límite de [bajadas], solo si el límite cambia lo enviado al Neo (spec `diagnostics`). */
    val sinLimitePct: Double? = null,
    /** El modelo elegido, la dificultad y, con el físico, lo que calcula (spec `diagnostics`, «Modelo físico en la simulación»). */
    val modelo: TipoModelo = TipoModelo.SIMPLE,
    val dificultadPct: Int = 100,
    val fisico: FisicoSim? = null,
    /** Con el físico, la potencia sin la mínima ni el alivio, solo si lo enviado es otra (spec `diagnostics`). */
    val sinLimiteW: Int? = null,
)

data class DiagnosticoUiState(
    val neo: EstadoDispositivo = EstadoDispositivo.SinConexion,
    val square: EstadoDispositivo = EstadoDispositivo.SinConexion,
    val plataforma: EstadoPlataforma = EstadoPlataforma.SinWifi,
    /** Con el modo por Bluetooth, la app de entrenamiento se dice «Por Bluetooth» y el nombre es el del móvil (spec `diagnostics`). */
    val enlace: EnlaceConexion = EnlaceConexion.WIFI,
    /** El nombre de la plataforma en uso, con el que se nombra en las filas y los avisos. */
    val nombrePlataforma: String = NOMBRE_APP_ENTRENAMIENTO,
    /** El nombre con el que la app se anuncia ahora (spec `diagnostics`, «Datos de la app de entrenamiento en Diagnóstico»). */
    val nombreAnunciado: String = NombreAnunciado.NORMAL,
    /** Lo último que Zwift ha mandado en su simulación de marchas virtuales, si ha mandado alguna: va en Simulación (spec `diagnostics`). */
    val simulacionZwift: FilaSimulacionZwiftUi? = null,
    /** El pulsómetro, solo si hay uno configurado. */
    val pulsometro: FilaPulsometroUi? = null,
    /** El ventilador, solo si hay uno configurado. */
    val ventilador: FilaVentiladorUi? = null,
    val nombreMovil: String? = null,
    /** De la batería del Square, en su fila de Datos de conexión; con [BateriaFila.Nivel] baja, «baja» junto al nivel y sin aviso aparte. */
    val bateriaSquare: BateriaFila? = null,
    val pulsados: Set<SquareButton> = emptySet(),
    /** Máscara en hexadecimal (`0x80020000`), o `null` sin Square. */
    val mascara: String? = null,
    /** Último gesto reconocido; `null` si aún no se ha pulsado nada. */
    val ultimoGesto: UltimoGestoUi? = null,
    val simulacion: SimulacionUi = SimulacionUi(),
    val trafico: List<FilaHex> = emptyList(),
    val pausado: Boolean = false,
    /** «Tráfico en directo» marcada: solo entonces se ven el volcado, «Pausar» y «Limpiar». */
    val traficoVisible: Boolean = false,
    val control: ControlManual = ControlManual(),
    val ultimoEnvio: Envio? = null,
) {
    val squareConectado get() = square == EstadoDispositivo.Conectado
    val neoConectado get() = neo == EstadoDispositivo.Conectado
}

/** De lo que sabe la sesión (y el permiso) a lo que muestra la pantalla. */
fun estadoDePantalla(
    c: EstadoRodaje,
    control: ControlManual,
    trafico: List<FilaHex>,
    pausado: Boolean,
    traficoVisible: Boolean = false,
    nombreMovil: String? = null,
): DiagnosticoUiState {
    val conSquare = c.square == EstadoDispositivo.Conectado
    return DiagnosticoUiState(
        neo = c.neo,
        square = c.square,
        plataforma = c.plataforma,
        enlace = c.enlaceConexion,
        nombrePlataforma = NOMBRE_APP_ENTRENAMIENTO,
        nombreAnunciado = c.nombreAnunciado,
        simulacionZwift = c.simulacion.ultimaSimulacionPlataforma?.let { r ->
            val s = r.simulacion
            FilaSimulacionZwiftUi(s.pendientePct, s.relacionVirtual, s.crr, s.cw, s.masaCiclistaKg, s.masaBiciKg, r.instanteMs)
        },
        // Sin conexión con la banda no hay pulso que mostrar; del ventilador se conserva el último modo y velocidad.
        pulsometro = c.pulsometroConfigurado?.let {
            val conectado = c.pulsometro == EstadoDispositivo.Conectado
            FilaPulsometroUi(c.pulso.takeIf { conectado }, c.pulsoInstanteMs.takeIf { conectado })
        },
        ventilador = c.ventiladorConfigurado?.let { FilaVentiladorUi(c.ventiladorEstado, c.ventiladorInstanteMs) },
        nombreMovil = nombreMovil,
        bateriaSquare = bateriaFila(c.square, c.bateriaSquare),
        pulsados = if (conSquare) SquareButton.pulsados(c.mascara ?: 0u).toSet() else emptySet(),
        mascara = if (conSquare) "0x%08X".format((c.mascara ?: 0u).toLong()) else null,
        ultimoGesto = c.ultimoGesto?.let { g ->
            UltimoGestoUi(
                listOfNotNull(g.gesto.entrada.modificador, g.gesto.entrada.boton),
                g.gesto.tipo,
                when (val r = g.resultado) {
                    null -> when (val a = g.ajuste) {
                        is ResultadoAjuste.Cambiado -> ResultadoGestoUi.Vatios(a.diferenciaW)

                        ResultadoAjuste.Quitado -> ergManual(c.simulacion.modo)?.let { ResultadoGestoUi.VueltaA(it.entradaW) }
                            ?: ResultadoGestoUi.PotenciaZwift

                        is ResultadoAjuste.Tope -> ResultadoGestoUi.TopePotencia(a.potenciaW)

                        // Alternar SIM ⇄ ERG y cambiar de pantalla no cambian la marcha ni ajustan: se nombra la acción.
                        ResultadoAjuste.SinCambio, null ->
                            if (g.accion in NOMBRADAS) ResultadoGestoUi.Hecha(g.accion!!, null) else ResultadoGestoUi.SinAccion
                    }

                    is ResultadoCambio.Tope -> ResultadoGestoUi.Tope(r.tope)

                    is ResultadoCambio.Cambiada ->
                        if (r.cambiaPlato) {
                            ResultadoGestoUi.Plato((r.nueva as EstadoMarcha.PlatoPinon).plato)
                        } else {
                            ResultadoGestoUi.Hecha(g.accion!!, r.pasos)
                        }

                    ResultadoCambio.SinCambio, is ResultadoCambio.ReferenciaFijada -> ResultadoGestoUi.Hecha(g.accion!!, null)
                },
            )
        },
        simulacion = simulacionUi(c),
        trafico = trafico,
        pausado = pausado,
        traficoVisible = traficoVisible,
        control = control,
        ultimoEnvio = c.ultimoEnvio,
    )
}

private fun simulacionUi(c: EstadoRodaje): SimulacionUi {
    val modo = c.simulacion.modo
    // Con un envío manual, lo de Zwift sigue llegando y se muestra: es a lo que se vuelve al salir.
    val base = (modo as? ModoControl.Manual)?.anterior ?: modo
    return SimulacionUi(
        marcha = c.marcha,
        pendienteBasePct = c.simulacion.pendienteBasePct,
        ajustePct = c.simulacion.ajustePct,
        enviado = if (c.neo == EstadoDispositivo.Conectado) c.simulacion.enviado else null,
        latenciaMs = c.latenciaMs,
        modo = when (modo) {
            ModoControl.SimLibre -> ModoUi.Sim
            is ModoControl.SimPlataforma -> ModoUi.SimPlataforma
            is ModoControl.ErgPlataforma -> ModoUi.ErgPlataforma
            is ModoControl.ErgManual -> ModoUi.ErgManual
            is ModoControl.Manual -> ModoUi.Manual
        },
        terreno = (base as? ModoControl.SimPlataforma)?.terreno,
        potenciaPlataformaW = (base as? ModoControl.ErgPlataforma)?.objetivoW,
        ajustePalancasW = (base as? ModoControl.ErgPlataforma)?.ajusteW,
        potenciaFijadaW = (base as? ModoControl.ErgManual)?.potenciaW,
        entradaW = (base as? ModoControl.ErgManual)?.entradaW,
        plataformaEnEspera = c.simulacion.plataformaEnEspera,
        ultimoPlataforma = c.simulacion.ultimoPlataforma,
        bajadas = c.simulacion.bajadas,
        sinLimitePct = sinLimite(c),
        modelo = c.simulacion.tipoModelo,
        dificultadPct = c.simulacion.dificultadPct,
        fisico = c.simulacion.fisico,
        sinLimiteW = sinLimiteFisico(c),
    )
}

/** Diferencias menores que esta entre la potencia calculada y la enviada son rampa y redondeo, no límite. */
private const val UMBRAL_LIMITE_W = 2

/** La potencia del físico sin la mínima ni el alivio, si el Neo está conectado y lo enviado es otra. */
private fun sinLimiteFisico(c: EstadoRodaje): Int? {
    if (c.neo != EstadoDispositivo.Conectado) return null
    val enviada = c.simulacion.enviado as? ComandoRodillo.Potencia ?: return null
    val f = c.simulacion.fisico ?: return null
    return f.calculadaW.takeIf { abs(it - enviada.vatios) > UMBRAL_LIMITE_W }
}

/** Acciones que no cambian la marcha ni ajustan la potencia, pero se nombran en el último gesto. */
private val NOMBRADAS = setOf(Accion.AlternarErgManual, Accion.PantallaAnterior, Accion.PantallaSiguiente)

/** El ERG manual vigente, también bajo el control manual de Diagnóstico. */
private fun ergManual(modo: ModoControl): ModoControl.ErgManual? = ((modo as? ModoControl.Manual)?.anterior ?: modo) as? ModoControl.ErgManual

/** Diferencias menores que esta entre la pendiente sin límite y la enviada son redondeo, no límite (decisión 4). */
private const val UMBRAL_LIMITE_PCT = 0.05

/** La pendiente sin límite, si el Neo está conectado y el límite de las bajadas cambia lo enviado. */
private fun sinLimite(c: EstadoRodaje): Double? {
    if (c.neo != EstadoDispositivo.Conectado) return null
    val enviada = c.simulacion.enviado as? ComandoRodillo.Pendiente ?: return null
    val sin = c.simulacion.sinLimitePct ?: return null
    return sin.takeIf { abs(it - enviada.pct) > UMBRAL_LIMITE_PCT }
}

private val formatoHora = DateTimeFormatter.ofPattern("HH:mm:ss.SSS")

/**
 * Entradas del registro (la más reciente primero) → filas del volcado. Solo tramas, no sucesos. Resalta los bytes
 * que cambian respecto a la trama anterior de la misma característica y origen.
 */
fun filasTrafico(
    entradas: List<EntradaRegistro>,
    zona: ZoneId = ZoneId.systemDefault(),
    nombrePlataforma: String = NOMBRE_APP_ENTRENAMIENTO,
): List<FilaHex> {
    val tramas = entradas.filter { it.sentido != Sentido.Evt }
    val anterior = HashMap<Pair<Origen, String>, ByteArray>()
    // Se recorren de la más antigua a la más reciente para saber cuál es la anterior de cada una.
    return tramas.asReversed().map { e ->
        val clave = e.origen to e.caracteristica
        val previa = anterior[clave]
        anterior[clave] = e.datos
        FilaHex(
            hora = formatoHora.format(Instant.ofEpochMilli(e.horaMs).atZone(zona)),
            origen = when {
                e.sentido == Sentido.Tx -> "→ ${nombreOrigen(e.origen, nombrePlataforma)}"
                else -> nombreOrigen(e.origen, nombrePlataforma)
            },
            caracteristica = e.caracteristica,
            contenido = hex(e.datos),
            enviada = e.sentido == Sentido.Tx,
            cambiados = if (previa == null) emptySet() else e.datos.indices.filter { it >= previa.size || previa[it] != e.datos[it] }.toSet(),
        )
    }.asReversed()
}

/** El origen de una trama como se lee en el volcado; el de la plataforma lleva el nombre de la elegida. */
private fun nombreOrigen(origen: Origen, nombrePlataforma: String) = when (origen) {
    Origen.Plataforma -> nombrePlataforma
    Origen.Neo -> "Rodillo"
    else -> origen.name
}

private val DIGITOS_HEX = "0123456789abcdef".toCharArray()

/** Bytes en hexadecimal separados por espacios (`a4 09 4e`), sin `String.format`, que es lento para cientos de tramas. */
internal fun hex(datos: ByteArray): String {
    if (datos.isEmpty()) return ""
    val texto = CharArray(datos.size * 3 - 1)
    datos.forEachIndexed { i, b ->
        val v = b.toInt() and 0xff
        if (i > 0) texto[i * 3 - 1] = ' '
        texto[i * 3] = DIGITOS_HEX[v ushr 4]
        texto[i * 3 + 1] = DIGITOS_HEX[v and 0x0f]
    }
    return String(texto)
}
