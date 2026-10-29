package app.smartbike.dominio

import kotlin.math.abs
import kotlin.math.roundToInt

/** Quién manda en el Neo (arquitectura.md § 5.4). */
sealed interface ModoControl {
    /** Pendiente base + marcha, según el modelo. */
    data object SimLibre : ModoControl

    /** Terreno de Zwift + marcha, según el modelo (F8.2). */
    data class SimPlataforma(val terreno: Terreno) : ModoControl

    /** Una potencia fija: [objetivoW] más el ajuste de las palancas, que es lo que trata [AjusteErg]. */
    sealed interface Erg : ModoControl {
        val objetivoW: Int
        val ajusteW: Int

        /** Lo que se envía al Neo: siempre entre [AjusteErg.MIN_W] y [AjusteErg.MAX_W]. */
        val potenciaW: Int get() = AjusteErg.limitar(objetivoW + ajusteW)
    }

    /** Potencia que pide Zwift más el ajuste de las palancas (F8.3). */
    data class ErgPlataforma(override val objetivoW: Int, override val ajusteW: Int) : Erg

    /**
     * ERG manual (F7.2, F7.3; design.md del change `erg-manual`, decisión 1): [entradaW] es la potencia con la que se
     * entró y [ajusteW] lo que han sumado las palancas. [debajo] es el SIM al que se vuelve, que sigue recibiendo el
     * terreno de Zwift.
     */
    data class ErgManual(val entradaW: Int, override val ajusteW: Int, val debajo: ModoControl) : Erg {
        override val objetivoW: Int get() = entradaW
    }

    /**
     * Control manual de Diagnóstico: el comando del usuario sustituye al de la marcha y al de Zwift mientras dura.
     * [anterior] se actualiza con lo que llega de Zwift, para volver a él al salir.
     */
    data class Manual(val comando: ComandoRodillo, val anterior: ModoControl) : ModoControl
}

/** Qué ha hecho «Alternar SIM ⇄ ERG», con el comando que hay que enviar, o `null` si no hay que enviar nada. */
sealed interface ResultadoAlternar {
    data class ErgManual(val potenciaW: Int, val comando: ComandoRodillo?) : ResultadoAlternar
    data class Sim(val comando: ComandoRodillo?) : ResultadoAlternar

    /** En ERG de Zwift no se alterna: manda el entrenamiento. */
    data object PlataformaManda : ResultadoAlternar
}

/**
 * Decide qué comando va al Neo y cuándo (F6, F7.1, F8.2, F8.3, F13.2; design.md del change `puente-zwift-wifi`,
 * decisiones 6 y 8). No envía nada: cada método devuelve el comando que hay que enviar, o `null` si no hay que enviar
 * nada. Recuerda el último enviado para no repetirlo y sabe si el Neo está conectado, porque sin él no se envía nada.
 * Las horas ([tMs]) son monotónicas, en milisegundos. En SIM, la pendiente del modelo se limita según [bajadas] (F6.5;
 * design.md del change `proteccion-bajadas`, decisión 2).
 */
class ControlRodillo(
    modelo: ModeloResistencia,
    terrenoBase: Terreno,
    bajadas: ModoBajadas = ModoBajadas.LA_MARCHA_NO_CREA_BAJADAS,
    dificultadPct: Int = 100,
    potenciaMinimaW: Int = 30,
) {
    /** El terreno sin app de entrenamiento: llano, desde que no hay «Pendiente sin Zwift» (change `pestana-conexion`). */
    val terrenoBase: Terreno = terrenoBase

    var bajadas: ModoBajadas = bajadas
        private set

    /** Dificultad global (F6.4): escala la resistencia de los tres modelos en SIM; no toca la ERG ni el control manual. */
    var dificultadPct: Int = dificultadPct
        private set

    /** Suelo de la potencia del modelo físico (F6.3, F12.5). */
    var potenciaMinimaW: Int = potenciaMinimaW
        private set

    /** Cambia al cambiar de transmisión (design.md del change `transmisiones-de-marca`, decisión 5). */
    var modelo: ModeloResistencia = modelo
        private set

    var modo: ModoControl = ModoControl.SimLibre
        private set

    /** El último comando entregado al Neo, o `null` si no se ha enviado nada desde que se conectó. */
    var ultimoEnviado: ComandoRodillo? = null
        private set

    /** Zwift se ha ido y se mantiene lo último que pidió hasta [plataformaAusente] (decisión 8). */
    var plataformaEnEspera = false
        private set

    private var neoConectado = false

    // Estado del modelo físico (design.md del change `modelos-tabla-y-fisico`, decisión 4)
    private var cadenciaSuave = 0.0
    private var pendienteSuave: Double? = null
    private var tFiltroMs: Long? = null
    private var rampaW: Double? = null
    private var ultimoEnvioFisicoMs: Long? = null

    /** La cadencia suavizada con la que calcula el modelo físico (Diagnóstico y velocidad). */
    val cadenciaSuaveRpm: Double get() = cadenciaSuave

    private val fisico: ModeloFisico? get() = modelo as? ModeloFisico

    /** Hora del último envío por terreno o marcha, para la ventana del terreno. */
    private var ultimoEnvioTerrenoMs: Long? = null

    /** Hay un terreno de Zwift guardado que no se ha enviado por la ventana; se envía en [proximoEnvioMs]. */
    private var terrenoPendiente = false

    /** El modo sin el control manual: lo que manda la marcha o Zwift. */
    private val base: ModoControl get() = (modo as? ModoControl.Manual)?.anterior ?: modo

    /** El ERG manual vigente, también bajo el control manual de Diagnóstico, o `null`. */
    val ergManual: ModoControl.ErgManual? get() = base as? ModoControl.ErgManual

    /** Cambia el modo de debajo del control manual, o el modo si no lo hay. */
    private fun cambiarBase(nuevo: ModoControl) {
        val manual = modo as? ModoControl.Manual
        modo = manual?.copy(anterior = nuevo) ?: nuevo
    }

    /** El comando que corresponde ahora, se haya enviado o no. */
    fun vigente(marcha: EstadoMarcha): ComandoRodillo = comandoDe(modo, marcha)

    private fun comandoDe(m: ModoControl, marcha: EstadoMarcha): ComandoRodillo = when (m) {
        ModoControl.SimLibre -> comandoSim(terrenoBase, marcha)
        is ModoControl.SimPlataforma -> comandoSim(m.terreno, marcha)
        is ModoControl.Erg -> ComandoRodillo.Potencia(m.potenciaW)
        is ModoControl.Manual -> m.comando
    }

    /** Lo que corresponde en SIM: la potencia del físico, o la pendiente del modelo con la dificultad y el suelo de [bajadas]. */
    private fun comandoSim(terreno: Terreno, marcha: EstadoMarcha): ComandoRodillo {
        fisico?.let { return ComandoRodillo.Potencia(potenciaObjetivo(it, terreno, marcha).roundToInt()) }
        val d = dificultadPct / 100.0
        return when (val c = modelo.comando(terreno, 0.0, marcha)) {
            is ComandoRodillo.Pendiente -> ComandoRodillo.Pendiente(bajadas.limitar(c.pct * d, terreno.pendientePct * d))
            is ComandoRodillo.Potencia -> c
        }
    }

    /** La potencia del físico con la cadencia y la pendiente suavizadas, la dificultad, el alivio y la mínima. */
    private fun potenciaObjetivo(f: ModeloFisico, terreno: Terreno, marcha: EstadoMarcha): Double {
        val minima = potenciaMinimaW.toDouble()
        val alivio = (cadenciaSuave / ProteccionesFisico.CADENCIA_BAJA_RPM).coerceIn(0.0, 1.0)
        return minima + (maxOf(potenciaSinLimite(f, terreno, marcha), minima) - minima) * alivio
    }

    private fun potenciaSinLimite(f: ModeloFisico, terreno: Terreno, marcha: EstadoMarcha): Double = f.potenciaW(terreno.copy(pendientePct = pendienteSuave ?: terreno.pendientePct), cadenciaSuave, marcha) * dificultadPct / 100.0

    private fun terrenoSim(): Terreno? = when (val m = modo) {
        ModoControl.SimLibre -> terrenoBase
        is ModoControl.SimPlataforma -> m.terreno
        else -> null
    }

    /** La potencia del físico sin la mínima ni el alivio, o `null` si no es el modelo vigente en SIM. Para Diagnóstico. */
    fun potenciaFisicaSinLimite(marcha: EstadoMarcha): Double? {
        val f = fisico ?: return null
        return potenciaSinLimite(f, terrenoSim() ?: return null, marcha)
    }

    /**
     * La pendiente que daría el modelo sin el límite de [bajadas], o `null` si lo vigente no es una pendiente del modelo
     * (ERG o control manual). Para Diagnóstico (decisión 4).
     */
    fun sinLimite(marcha: EstadoMarcha): Double? {
        val terreno = when (val m = modo) {
            ModoControl.SimLibre -> terrenoBase
            is ModoControl.SimPlataforma -> m.terreno
            else -> return null
        }
        return (modelo.comando(terreno, 0.0, marcha) as? ComandoRodillo.Pendiente)?.pct?.times(dificultadPct / 100.0)
    }

    /** Cambia la dificultad (F6.4); devuelve el comando nuevo si cambia lo que hay que enviar. */
    fun cambiarDificultad(pct: Int, marcha: EstadoMarcha): ComandoRodillo? {
        dificultadPct = pct
        if (modo is ModoControl.Manual || modo is ModoControl.Erg) return null
        return siCambia(vigente(marcha))
    }

    /** Cambia la potencia mínima del modelo físico; devuelve el comando nuevo si cambia lo que hay que enviar. */
    fun cambiarPotenciaMinima(w: Int, marcha: EstadoMarcha): ComandoRodillo? {
        potenciaMinimaW = w
        if (modo is ModoControl.Manual || modo is ModoControl.Erg) return null
        return siCambia(vigente(marcha))
    }

    /**
     * Una muestra de cadencia del Neo. Siempre alimenta los filtros; con el modelo físico en SIM devuelve la potencia que
     * hay que enviar, con rampa y como mucho cada [ProteccionesFisico.VENTANA_ENVIO_MS], si difiere
     * [ProteccionesFisico.UMBRAL_W] o más de lo enviado (decisión 4).
     */
    fun alCambiarCadencia(rpm: Double, marcha: EstadoMarcha, tMs: Long): ComandoRodillo? {
        val dt = tFiltroMs?.let { tMs - it }
        val terreno = terrenoSim()
        if (dt == null || dt <= 0) {
            if (dt == null) cadenciaSuave = rpm
        } else {
            cadenciaSuave += ProteccionesFisico.alfa(dt, ProteccionesFisico.TAU_CADENCIA_MS) * (rpm - cadenciaSuave)
        }
        if (terreno != null) {
            val actual = pendienteSuave
            pendienteSuave = if (actual == null || dt == null || dt <= 0) {
                actual ?: terreno.pendientePct
            } else {
                actual + ProteccionesFisico.alfa(dt, ProteccionesFisico.TAU_PENDIENTE_MS) * (terreno.pendientePct - actual)
            }
        }
        tFiltroMs = tMs
        val f = fisico ?: return null
        if (terreno == null || !neoConectado) return null
        val objetivo = potenciaObjetivo(f, terreno, marcha)
        val anterior = rampaW ?: objetivo
        val s = (dt ?: 0L).coerceAtLeast(0) / 1000.0
        val rampa = if (objetivo > anterior) {
            minOf(objetivo, anterior + ProteccionesFisico.SUBIDA_W_S * s)
        } else {
            maxOf(objetivo, anterior - ProteccionesFisico.BAJADA_W_S * s)
        }
        rampaW = rampa
        val vatios = rampa.roundToInt()
        val ultimo = ultimoEnviado as? ComandoRodillo.Potencia
        val desde = ultimoEnvioFisicoMs
        if (ultimo != null && (
                abs(vatios - ultimo.vatios) < ProteccionesFisico.UMBRAL_W ||
                    (desde != null && tMs - desde < ProteccionesFisico.VENTANA_ENVIO_MS)
                )
        ) {
            return null
        }
        ultimoEnvioFisicoMs = tMs
        return siCambia(ComandoRodillo.Potencia(vatios))
    }

    /** Cambia el modo de bajadas; devuelve la pendiente nueva si cambia lo que hay que enviar (decisión 3). */
    fun cambiarBajadas(modo: ModoBajadas, marcha: EstadoMarcha): ComandoRodillo? {
        bajadas = modo
        if (this.modo is ModoControl.Manual || this.modo is ModoControl.Erg) return null
        return siCambia(vigente(marcha))
    }

    /**
     * Cambia el modelo, al cambiar de transmisión, con la marcha que corresponde a la nueva; devuelve el comando si
     * cambia lo que hay que enviar, como [cambiarBajadas].
     */
    fun cambiarModelo(modelo: ModeloResistencia, marcha: EstadoMarcha): ComandoRodillo? {
        this.modelo = modelo
        if (this.modo is ModoControl.Manual || this.modo is ModoControl.Erg) return null
        return siCambia(vigente(marcha))
    }

    /** La marcha ha cambiado: en SIM se envía al instante con el terreno vigente; en ERG o en manual, no. */
    fun alCambiarMarcha(marcha: EstadoMarcha, tMs: Long = 0): ComandoRodillo? {
        if (modo is ModoControl.Manual || modo is ModoControl.Erg) return null
        val c = siCambia(vigente(marcha)) ?: return null
        ultimoEnvioTerrenoMs = tMs
        ultimoEnvioFisicoMs = tMs
        terrenoPendiente = false
        return c
    }

    /**
     * Terreno de Zwift (`0x11`): pasa a SIM de Zwift. Se envía si cambia en [UMBRAL_PENDIENTE_PCT] o más respecto a lo
     * enviado y ha pasado [VENTANA_TERRENO_MS] desde el último envío; si no ha pasado, queda pendiente.
     */
    fun terrenoPlataforma(terreno: Terreno, marcha: EstadoMarcha, tMs: Long): ComandoRodillo? {
        plataformaEnEspera = false
        val nuevo = ModoControl.SimPlataforma(terreno)
        // En ERG manual, el terreno solo se guarda para volver a SIM (spec `manual-erg`, «ERG manual en una ruta de Zwift»).
        ergManual?.let {
            cambiarBase(it.copy(debajo = nuevo))
            return null
        }
        val manual = modo as? ModoControl.Manual
        if (manual != null) {
            modo = manual.copy(anterior = nuevo)
            return null
        }
        val cambiaDeModo = modo !is ModoControl.SimPlataforma
        modo = nuevo
        if (cambiaDeModo) pendienteSuave = null
        if (!neoConectado) return null
        // El físico sigue el terreno con su pendiente suavizada, desde la cadencia (decisión 4).
        if (fisico != null && !cambiaDeModo) return null
        val c = vigente(marcha)
        val ultimo = ultimoEnviado
        val apreciable = cambiaDeModo || ultimo !is ComandoRodillo.Pendiente || c !is ComandoRodillo.Pendiente ||
            abs(c.pct - ultimo.pct) >= UMBRAL_PENDIENTE_PCT - 1e-9
        if (!apreciable) {
            terrenoPendiente = false
            return null
        }
        val desde = ultimoEnvioTerrenoMs
        if (!cambiaDeModo && desde != null && tMs - desde < VENTANA_TERRENO_MS) {
            terrenoPendiente = true
            return null
        }
        terrenoPendiente = false
        ultimoEnvioTerrenoMs = tMs
        return siCambia(c)
    }

    /** Cuándo enviar el terreno pendiente, o `null` si no hay. */
    fun proximoEnvioMs(): Long? = if (terrenoPendiente) (ultimoEnvioTerrenoMs ?: 0) + VENTANA_TERRENO_MS else null

    /** Envía el terreno pendiente con la marcha de ahora. */
    fun enviarPendiente(marcha: EstadoMarcha, tMs: Long): ComandoRodillo? {
        if (!terrenoPendiente) return null
        terrenoPendiente = false
        if (modo !is ModoControl.SimPlataforma) return null
        ultimoEnvioTerrenoMs = tMs
        return siCambia(vigente(marcha))
    }

    /**
     * Potencia objetivo de Zwift (`0x05`): pasa a ERG de Zwift, conservando el ajuste si ya estaba en ERG de Zwift. Un
     * ERG manual termina, sin pasar su ajuste (spec `manual-erg`, «Entrenamiento de Zwift en ERG manual»).
     */
    fun potenciaPlataforma(vatios: Int): ComandoRodillo? {
        plataformaEnEspera = false
        terrenoPendiente = false
        val ajuste = (base as? ModoControl.ErgPlataforma)?.ajusteW ?: 0
        val nuevo = ModoControl.ErgPlataforma(vatios, ajuste)
        val manual = modo as? ModoControl.Manual
        if (manual != null) {
            modo = manual.copy(anterior = nuevo)
            return null
        }
        modo = nuevo
        val c = ComandoRodillo.Potencia(nuevo.potenciaW)
        val ultimo = ultimoEnviado
        val apreciable = ultimo !is ComandoRodillo.Potencia || c !is ComandoRodillo.Potencia ||
            abs(c.vatios - ultimo.vatios) >= UMBRAL_POTENCIA_W
        return if (apreciable) siCambia(c) else null
    }

    /** Ajuste de las palancas en ERG, de Zwift o manual, ya calculado con [AjusteErg]. */
    fun ajusteErg(ajusteW: Int): ComandoRodillo? {
        val nuevo = when (val m = modo) {
            is ModoControl.ErgPlataforma -> m.copy(ajusteW = ajusteW)
            is ModoControl.ErgManual -> m.copy(ajusteW = ajusteW)
            else -> return null
        }
        modo = nuevo
        return siCambia(ComandoRodillo.Potencia(nuevo.potenciaW))
    }

    /**
     * «Alternar SIM ⇄ ERG» (spec `manual-erg`): de SIM a ERG manual con [entradaW], y de ERG manual al SIM de debajo con
     * la marcha. En ERG de Zwift no cambia nada. Con el control manual de Diagnóstico cambia el modo de debajo sin enviar.
     */
    fun alternarErgManual(entradaW: Int, marcha: EstadoMarcha): ResultadoAlternar {
        terrenoPendiente = false
        val enviar = modo !is ModoControl.Manual
        return when (val b = base) {
            is ModoControl.ErgPlataforma -> ResultadoAlternar.PlataformaManda

            is ModoControl.ErgManual -> {
                cambiarBase(b.debajo)
                ResultadoAlternar.Sim(if (enviar) siCambia(vigente(marcha)) else null)
            }

            else -> {
                val nuevo = ModoControl.ErgManual(AjusteErg.limitar(entradaW), 0, b)
                cambiarBase(nuevo)
                ResultadoAlternar.ErgManual(nuevo.potenciaW, if (enviar) siCambia(vigente(marcha)) else null)
            }
        }
    }

    /** Zwift se ha desconectado: se mantiene lo último que pidió hasta [plataformaAusente]. */
    fun plataformaPerdida() {
        val b = base
        if (b is ModoControl.SimPlataforma || b is ModoControl.ErgPlataforma || (b as? ModoControl.ErgManual)?.debajo is ModoControl.SimPlataforma) {
            plataformaEnEspera = true
        }
    }

    /** Zwift no ha vuelto a tiempo: vuelta a la pendiente base con la marcha puesta. */
    fun plataformaAusente(marcha: EstadoMarcha): ComandoRodillo? {
        if (!plataformaEnEspera) return null
        plataformaEnEspera = false
        terrenoPendiente = false
        // En ERG manual manda la potencia: solo cambia el SIM al que se vuelve.
        ergManual?.let {
            cambiarBase(it.copy(debajo = ModoControl.SimLibre))
            return null
        }
        val manual = modo as? ModoControl.Manual
        if (manual != null) {
            modo = manual.copy(anterior = ModoControl.SimLibre)
            return null
        }
        modo = ModoControl.SimLibre
        return siCambia(vigente(marcha))
    }

    /** Envío manual desde Diagnóstico: se envía siempre, aunque repita el anterior, porque lo pide el usuario. */
    fun manual(comando: ComandoRodillo): ComandoRodillo? {
        if (!neoConectado) return null
        modo = ModoControl.Manual(comando, base)
        return enviado(comando)
    }

    /** Al salir de Diagnóstico se vuelve a lo que corresponde: la marcha, con el terreno o la potencia de Zwift. */
    fun salirManual(marcha: EstadoMarcha): ComandoRodillo? {
        val m = modo as? ModoControl.Manual ?: return null
        modo = m.anterior
        return siCambia(vigente(marcha))
    }

    /** El Neo acaba de conectarse: se olvida lo enviado antes y se envía el vigente (F1.3). */
    fun neoListo(marcha: EstadoMarcha): ComandoRodillo {
        neoConectado = true
        ultimoEnviado = null
        return enviado(vigente(marcha))
    }

    /** Sin Neo, un envío manual deja de tener efecto; lo de Zwift sigue y se envía al volver. */
    fun neoPerdido() {
        neoConectado = false
        ultimoEnviado = null
        terrenoPendiente = false
        modo = base
    }

    /** Al terminar la sesión, el Neo se queda en llano (spec `trainer-manual-control`). */
    fun terminar(): ComandoRodillo? = if (neoConectado) enviado(ComandoRodillo.Pendiente(0.0)) else null

    private fun siCambia(comando: ComandoRodillo): ComandoRodillo? {
        val c = comando.limitado()
        return if (!neoConectado || c == ultimoEnviado) null else enviado(c)
    }

    private fun enviado(comando: ComandoRodillo): ComandoRodillo {
        val c = comando.limitado()
        ultimoEnviado = c
        if (c is ComandoRodillo.Potencia) rampaW = c.vatios.toDouble()
        return c
    }

    companion object {
        const val VENTANA_TERRENO_MS = 250L
        const val UMBRAL_PENDIENTE_PCT = 0.1
        const val UMBRAL_POTENCIA_W = 2
    }
}
