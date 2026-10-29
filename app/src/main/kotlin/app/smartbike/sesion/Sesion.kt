package app.smartbike.sesion

import app.smartbike.ble.BuscadorDispositivos
import app.smartbike.ble.ConexionNeo
import app.smartbike.ble.ConexionPulso
import app.smartbike.ble.ConexionSquare
import app.smartbike.ble.ConexionVentilador
import app.smartbike.ble.EstadoBluetooth
import app.smartbike.ble.EstadoNombre
import app.smartbike.ble.NeoClient
import app.smartbike.ble.Notificacion
import app.smartbike.ble.PulsoClient
import app.smartbike.ble.RenombradorBluetooth
import app.smartbike.ble.SquareClient
import app.smartbike.ble.VentiladorClient
import app.smartbike.datos.AlmacenConfiguracion
import app.smartbike.datos.AlmacenEstado
import app.smartbike.datos.Origen
import app.smartbike.datos.RegistroSesion
import app.smartbike.dominio.Accion
import app.smartbike.dominio.AjusteErg
import app.smartbike.dominio.Cambio
import app.smartbike.dominio.ComandoRodillo
import app.smartbike.dominio.Compatibilidad
import app.smartbike.dominio.Configuracion
import app.smartbike.dominio.Configurados
import app.smartbike.dominio.ControlRodillo
import app.smartbike.dominio.DatosSesion
import app.smartbike.dominio.Entrada
import app.smartbike.dominio.EstadoGuardado
import app.smartbike.dominio.EstadoMarcha
import app.smartbike.dominio.EventoBoton
import app.smartbike.dominio.EventosBoton
import app.smartbike.dominio.FuncionMando
import app.smartbike.dominio.Gesto
import app.smartbike.dominio.GuiaInicio
import app.smartbike.dominio.GuiaPantalla
import app.smartbike.dominio.IdentidadDispositivo
import app.smartbike.dominio.LadoGiro
import app.smartbike.dominio.ModeloFisico
import app.smartbike.dominio.ModeloResistencia
import app.smartbike.dominio.ModeloSimple
import app.smartbike.dominio.ModeloTabla
import app.smartbike.dominio.ModoBajadas
import app.smartbike.dominio.ModoConexion
import app.smartbike.dominio.ModoControl
import app.smartbike.dominio.ModoVentilador
import app.smartbike.dominio.NombreBluetooth
import app.smartbike.dominio.Publicacion
import app.smartbike.dominio.PulsoVigente
import app.smartbike.dominio.ReconocedorGestos
import app.smartbike.dominio.Recorrido
import app.smartbike.dominio.ResultadoAjuste
import app.smartbike.dominio.ResultadoAlternar
import app.smartbike.dominio.ResultadoCambio
import app.smartbike.dominio.ServiciosPublicados
import app.smartbike.dominio.Terreno
import app.smartbike.dominio.TipoGesto
import app.smartbike.dominio.TipoModelo
import app.smartbike.dominio.Transmision
import app.smartbike.dominio.limitado
import app.smartbike.plataforma.ConexionPlataforma
import app.smartbike.plataforma.DestinoPlataforma
import app.smartbike.plataforma.EstadoMando
import app.smartbike.plataforma.EstadoPlataforma
import app.smartbike.plataforma.PuentePlataforma
import app.smartbike.plataforma.ReceptorPlataforma
import app.smartbike.plataforma.TelemetriaPlataforma
import app.smartbike.plataforma.UltimoDePlataforma
import app.smartbike.protocolo.CalculoCadencia
import app.smartbike.protocolo.FecCodec
import app.smartbike.protocolo.PulsoCodec
import app.smartbike.protocolo.SquareButton
import app.smartbike.protocolo.SquareDecoder
import app.smartbike.protocolo.TelemetriaCodec
import app.smartbike.red.EstadoWifi
import kotlin.math.roundToInt
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Sesión de rodaje (arquitectura.md § 4.3): único dueño del estado. Conecta el Neo y el Square (a los configurados sola,
 * y reconecta al perderlos), convierte los botones en marchas y las marchas en comandos al Neo, y guarda la marcha. Todo corre en el hilo de control de [ambitoPadre],
 * que es secuencial; el camino caliente (notificación del Square → `NeoClient.enviar`) no suspende (§ 5.1).
 */
class Sesion(
    private val buscador: BuscadorDispositivos,
    private val square: ConexionSquare,
    private val neo: ConexionNeo,
    private val pulsometro: ConexionPulso,
    private val ventilador: ConexionVentilador,
    private val bluetooth: EstadoBluetooth,
    private val wifi: EstadoWifi,
    /** Crea la conexión de lo que se publica (design.md del change `pestana-conexion`, decisión 3), en su propio ámbito. */
    crearConexion: (ServiciosPublicados, Boolean, ReceptorPlataforma, CoroutineScope) -> ConexionPlataforma,
    /** El nombre Bluetooth del móvil mientras se publica por Bluetooth (change `nombre-bluetooth-propio`). */
    private val renombrador: RenombradorBluetooth,
    private val registro: RegistroSesion,
    private val almacen: AlmacenEstado,
    private val ajustes: AlmacenConfiguracion,
    private val vibrador: Vibrador,
    ambitoPadre: CoroutineScope,
    private val relojMonotonico: () -> Long,
    private val relojPared: () -> Long,
) {
    private val ambito = CoroutineScope(ambitoPadre.coroutineContext + SupervisorJob(ambitoPadre.coroutineContext[Job]))

    /**
     * La configuración vigente: la de `configuracion.json` desde que se lee al empezar, y cada cambio que se guarda
     * después (decisión 5). La transmisión, su modo y su asignación cambian con ella en caliente (design.md del change
     * `transmisiones-de-marca`, decisión 7).
     */
    private var configuracion = Configuracion()
    private var cambio = Cambio(configuracion.transmisionElegida)
    private var modelo: ModeloResistencia = configuracion.modeloResistencia(configuracion.transmisionElegida)
    private var claveModelo = claveDe(configuracion, configuracion.transmisionElegida)
    private val control = ControlRodillo(
        modelo,
        // Sin app de entrenamiento, el terreno es llano: «Pendiente sin Zwift» ya no existe (change `pestana-conexion`).
        Terreno(0.0),
        configuracion.bajadas,
        configuracion.dificultadPct,
        configuracion.potenciaMinimaW,
    )
    private val recorrido = Recorrido()

    /** Lo último guardado en `estado.json` de cada transmisión, para volver a ella al cambiar de transmisión. */
    private var ultimas = EstadoGuardado()

    /** La última potencia del ERG manual, con la que se vuelve a entrar; se guarda con la marcha (spec `manual-erg`). */
    private var potenciaErg = EstadoGuardado.POTENCIA_ERG_INICIAL_W
    private var configurada = false
    private val eventosBoton = EventosBoton()
    private val reconocedor = ReconocedorGestos()
    private val cadencia = CalculoCadencia()
    private val datosSesion = DatosSesion()

    /** Temporizador único del reconocedor: larga, repetición y corta tras la ventana de la doble (decisión 3). */
    private var temporizador: Job? = null

    /** Marcha y ajuste de ERG al empezar a pulsar la combinación de apagado del Square (decisión 6). */
    private var antesDeApagar: Pair<EstadoMarcha, Int?>? = null
    private var apagando = false
    private var pulsadosApagado = 0u

    private val _captura = MutableStateFlow<EstadoCaptura?>(null)

    /** La captura de Asignar acción, o `null` si no hay ninguna en curso. */
    val captura: StateFlow<EstadoCaptura?> = _captura.asStateFlow()

    /** Un Campagnolo pulsado durante la captura, sin otro botón todavía: se captura solo si se suelta así. */
    private var campagnoloSolo: SquareButton? = null

    private val _estado = MutableStateFlow(
        EstadoRodaje(
            bluetoothEncendido = bluetooth.encendido.value,
            marcha = configuracion.referencia,
            referencia = configuracion.referencia,
            simulacion = simulacion(configuracion.referencia, velocidadMs = null),
        ),
    )
    val estado: StateFlow<EstadoRodaje> = _estado.asStateFlow()

    private val _eventos = MutableSharedFlow<EventoRodaje>(extraBufferCapacity = 16)
    val eventos: SharedFlow<EventoRodaje> = _eventos

    /** Lo que recuerda la guía de inicio durante la ejecución: página, hoja abierta o minimizada, equis (change `guia-inicio`). */
    private val _guia = MutableStateFlow(GuiaPantalla())
    val guia: StateFlow<GuiaPantalla> = _guia.asStateFlow()

    private val _notificacionesPedidas = MutableStateFlow(true)

    /** Si ya se pidió el permiso de notificaciones alguna vez (se guarda en `estado.json`). */
    val notificacionesPedidas: StateFlow<Boolean> = _notificacionesPedidas.asStateFlow()

    private var guardado: Job? = null

    /**
     * El puente con la plataforma elegida (design.md del change `generalizar-plataformas`, decisión 5): la conexión del
     * modo, el wifi, la telemetría, el pulso y el mando. La `Sesion` no sabe qué plataforma ni qué modo es.
     */
    private val puente = PuentePlataforma(ambito, registro, wifi, crearConexion, DestinoDeLaSesion(), ::telemetriaAPlataforma)

    /** El nombre Bluetooth del móvil tal como lo ve la app, para Conexión. */
    val nombreBluetooth: StateFlow<EstadoNombre> get() = renombrador.estado

    /** Poner el nombre Bluetooth y usar la publicación, en ese orden; uno solo a la vez (decisión 3). */
    private var aplicandoPlataforma: Job? = null

    /** Envío del terreno de Zwift guardado por la ventana de 250 ms (decisión 6). */
    private var envioTerreno: Job? = null

    /** Espera de [ESPERA_PLATAFORMA_MS] tras perder la plataforma antes de volver a la pendiente base (decisión 8). */
    private var esperaPlataforma: Job? = null
    private var ultimoPlataforma: MensajePlataforma? = null
    private var ultimaSimulacionPlataforma: SimulacionPlataformaRecibida? = null

    /** Últimos datos de biela del Neo, que se reenvían a Zwift tal cual. */
    private var biela: TelemetriaCodec.Biela? = null

    /** Las búsquedas, los intentos y el estado de cada dispositivo, con los configurados en `estado.json`. */
    private val conexiones = ConexionesSesion(ambito, buscador, neo, square, pulsometro, ventilador, bluetooth, registro, almacen, _estado, ReaccionesDeLaSesion())

    /** Ya se han leído los configurados: antes no se publica nada, para no anunciar y retirar al abrir la app. */
    private var configuradosLeidos = false

    /** Cuándo hay pulso (spec `heart-rate`) y la espera a que caduque la última lectura. */
    private val pulsoVigente = PulsoVigente(relojMonotonico)
    private var caducidadPulso: Job? = null

    /** Las órdenes al ventilador y lo que comunica (design.md del change `ventilador-headwind`, decisiones 3 y 4). */
    private val ventiladorSesion = VentiladorSesion(ambito, ventilador, registro, _estado, relojMonotonico) { Dispositivo.Ventilador in conexiones.configurados }

    /** Si había Bluetooth encendido y permiso la última vez que se miró. */
    private var puedeConectar = false
    private var terminada = false

    /**
     * Funciones de Zwift mantenidas con «pulsar», por el botón que las mantiene: su `Soltado` las suelta (design.md del
     * change `mando-zwift-emulado`, decisión 3).
     */
    private val mantenidas = mutableMapOf<SquareButton, FuncionMando>()

    /** Pulsaciones breves (corta, larga, doble) pendientes de soltarse. */
    private val breves = mutableListOf<Job>()

    init {
        ambito.launch {
            // Primero la configuración, la marcha guardada (F5.6) y los configurados; después, las conexiones.
            val c = ajustes.configuracion.first()
            val guardado = almacen.leer()
            // La guía se evalúa una vez: con algún dispositivo ya configurado (una actualización) no se enseña (spec `getting-started-guide`).
            val g = guardado.conGuiaEvaluada()
            if (g.guiaInicio != guardado.guiaInicio) almacen.actualizar { it.conGuiaEvaluada() }
            _guia.value = GuiaPantalla.alArrancar(g.hayDispositivos)
            _estado.update { it.copy(guiaInicio = g.guiaInicio) }
            ultimas = g
            potenciaErg = g.potenciaErgManualW
            aplicarConfiguracion(c)
            val marcha = _estado.value.marcha
            _notificacionesPedidas.value = g.notificacionesPedidas
            conexiones.cargar(g)
            publicarConfigurados()
            registro.suceso(Origen.App, "Sesión: marcha ${marcha.texto()}")
            conexiones.configurados.forEach { (d, r) -> registro.suceso(d.origen, "Configurado: ${r.nombre} (${r.direccion})") }
            ambito.launch { ajustes.configuracion.collect { aplicarConfiguracion(it) } }
            ambito.launch { combine(estado, guia) { e, gu -> gu.finaliza(e.guiaInicio, pasosGuia(e).completa) }.collect { if (it) guardarGuia(GuiaInicio.FINALIZADA) } }
            ambito.launch { square.notificaciones.collect { if (it.caracteristica == SquareClient.ETIQUETA_CONTADORES) botones(it) } }
            ambito.launch { neo.notificaciones.collect { telemetria(it) } }
            ambito.launch { square.nivelBateria.collect { n -> _estado.update { it.copy(bateriaSquare = n) } } }
            ambito.launch { square.estado.collect { conexiones.enlace(it, Dispositivo.Square) } }
            ambito.launch { neo.estado.collect { conexiones.enlace(it, Dispositivo.Neo) } }
            ambito.launch { pulsometro.notificaciones.collect { pulsoLeido(it) } }
            ambito.launch { pulsometro.estado.collect { conexiones.enlace(it, Dispositivo.Pulsometro) } }
            ambito.launch { ventilador.notificaciones.collect { ventiladorSesion.leido(it) } }
            ambito.launch { ventilador.estado.collect { conexiones.enlace(it, Dispositivo.Ventilador) } }
            ambito.launch {
                combine(bluetooth.encendido, bluetooth.permiso) { e, p -> e to p }.collect { (e, p) -> bluetoothCambia(e, p) }
            }
            ambito.launch { wifi.direccion.collect { puente.wifiCambia(it) } }
        }
    }

    enum class Dispositivo(val prefijo: String) {
        Neo(NeoClient.PREFIJO_NOMBRE),
        Square(SquareClient.PREFIJO_NOMBRE),
        Pulsometro(PulsoClient.CLAVE),
        Ventilador(VentiladorClient.PREFIJO_NOMBRE),
    }

    /**
     * Busca el Neo y el Square a la vez, y recupera el pulsómetro y el ventilador configurados. Dispositivos ya no lo
     * ofrece (cada tipo tiene su «Buscar»: [buscarRodillo], [buscarMando]); queda para quien necesita los dos.
     */
    fun buscar() {
        ambito.launch { conexiones.buscar() }
    }

    /** «Buscar» de la sección Rodillo (spec `device-connection`, «Buscar el rodillo y el mando por separado»). */
    fun buscarRodillo() {
        ambito.launch { conexiones.buscarUno(Dispositivo.Neo) }
    }

    /** «Buscar» de la sección Mando, sin tocar la conexión del rodillo. */
    fun buscarMando() {
        ambito.launch { conexiones.buscarUno(Dispositivo.Square) }
    }

    /** «Reconectar» de un dispositivo configurado ([ConexionesSesion.reconectar]). */
    fun reconectar(d: Dispositivo) {
        ambito.launch { conexiones.reconectar(d) }
    }

    /** «Buscar ventilador» ([ConexionesSesion.buscarVentilador]). */
    fun buscarVentilador() {
        ambito.launch { conexiones.buscarVentilador() }
    }

    /** «Buscar pulsómetro» ([ConexionesSesion.buscarPulsometro]). */
    fun buscarPulsometro() {
        ambito.launch { conexiones.buscarPulsometro() }
    }

    /** «Olvidar» un dispositivo ([ConexionesSesion.olvidar]). */
    fun olvidar(d: Dispositivo) {
        ambito.launch { conexiones.olvidar(d) }
    }

    /**
     * Una acción desde la pantalla, como los botones − y + de Rodaje: el mismo camino que un gesto «pulsar» del Square,
     * sin latencia de palanca ni último gesto; durante la captura no hace nada (design.md del change
     * `rodaje-completo-y-ajustes`, decisión 3).
     */
    fun accion(a: Accion) {
        ambito.launch {
            if (_captura.value != null) return@launch
            ejecutar(a, null)
        }
    }

    private fun publicarConfigurados() {
        configuradosLeidos = true
        actualizarPlataforma(configuracion)
        val configurados = conexiones.configurados
        _estado.update {
            it.copy(
                neoConfigurado = configurados[Dispositivo.Neo],
                squareConfigurado = configurados[Dispositivo.Square],
                pulsometroConfigurado = configurados[Dispositivo.Pulsometro],
                ventiladorConfigurado = configurados[Dispositivo.Ventilador],
            )
        }
    }

    /** Envío manual desde Diagnóstico: sustituye a la marcha hasta salir de Diagnóstico (spec `trainer-manual-control`). */
    fun controlManual(comando: ComandoRodillo) {
        ambito.launch {
            val c = control.manual(comando) ?: return@launch
            registro.suceso(Origen.App, "Envío manual: $c")
            enviarNeo(c, null)
            val envio = when (c) {
                is ComandoRodillo.Pendiente -> Envio.Pendiente(c.pct, relojPared())
                is ComandoRodillo.Potencia -> Envio.Potencia(c.vatios, relojPared())
            }
            _estado.update { it.copy(ultimoEnvio = envio, simulacion = simulacion(it.marcha)) }
        }
    }

    /** Al salir de Diagnóstico, el Neo vuelve a la marcha. */
    fun salirDiagnostico() {
        ambito.launch {
            val c = control.salirManual(_estado.value.marcha) ?: return@launch
            registro.suceso(Origen.App, "Fin del control manual")
            enviarNeo(c, null)
            _estado.update { it.copy(simulacion = simulacion(it.marcha)) }
        }
    }

    /** Al abrir Asignar acción: los botones del Square dejan de ejecutar acciones y se capturan (decisión 7). */
    fun empezarCaptura() {
        ambito.launch {
            temporizador?.cancel()
            reconocedor.reiniciar()
            soltarPlataforma()
            campagnoloSolo = null
            _captura.value = if (conexiones.estadoDe(Dispositivo.Square) == EstadoDispositivo.Conectado) EstadoCaptura.Esperando else EstadoCaptura.SinSquare
        }
    }

    /** Al cerrar Asignar acción, los botones vuelven a sus acciones. */
    fun terminarCaptura() {
        ambito.launch {
            _captura.value = null
            campagnoloSolo = null
        }
    }

    /** Cambia lo que la guía recuerda durante la ejecución: la página, abrir y minimizar, la equis. */
    fun guia(cambio: (GuiaPantalla) -> GuiaPantalla) {
        _guia.update(cambio)
    }

    /** «No volver a mostrar»: se guarda y la hoja se cierra (spec `getting-started-guide`, «Descartar la guía»). */
    fun noMostrarGuia() {
        _guia.update { it.cerrar() }
        guardarGuia(GuiaInicio.DESCARTADA)
    }

    /** «Guía de inicio» de Ajustes: la guía vuelve a verse abierta en la página 1, también si estaba descartada o terminada. */
    fun reabrirGuia() {
        _guia.update { it.reabrir() }
        guardarGuia(GuiaInicio.VISIBLE)
    }

    private fun guardarGuia(valor: GuiaInicio) {
        if (_estado.value.guiaInicio == valor) return
        _estado.update { it.copy(guiaInicio = valor) }
        ambito.launch { almacen.actualizar { it.copy(guiaInicio = valor) } }
    }

    fun notificacionesPedidas() {
        _notificacionesPedidas.value = true
        ambito.launch { almacen.actualizar { it.copy(notificacionesPedidas = true) } }
    }

    /**
     * Fin de la sesión: guarda la marcha, deja el Neo en pendiente 0 % (esperando su confirmación como mucho
     * [LIMITE_TERMINAR_MS]) y desconecta los dos (spec `ride-session`, «Terminar la sesión»). Se llama desde un ámbito
     * que sobrevive a la sesión.
     */
    suspend fun terminar() {
        terminada = true
        conexiones.detener()
        ventiladorSesion.terminar()
        caducidadPulso?.cancel()
        guardado?.cancel()
        temporizador?.cancel()
        try {
            // Si no se puede guardar la marcha (DataStore), el Neo igualmente se deja en llano y se desconecta todo.
            try {
                guardar()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                registro.suceso(Origen.App, "Fin: no se pudo guardar la marcha ($e)")
            }
            control.terminar()?.let {
                registro.suceso(Origen.App, "Fin: pendiente 0 %")
                neo.enviarYEsperar(codificar(it), LIMITE_TERMINAR_MS)
            }
            envioTerreno?.cancel()
            esperaPlataforma?.cancel()
            soltarPlataforma()
            puente.terminar()
            aplicandoPlataforma?.cancel()
            conLimite(registro) { renombrador.aplicar(null) }
            conexiones.desconectarTodo()
        } finally {
            // El ámbito se cancela enseguida: el estado final se fija aquí y no espera a los colectores.
            conexiones.sinConexion()
            registro.suceso(Origen.App, "Sesión terminada")
            ambito.cancel()
        }
    }

    // --- Camino caliente: sin suspensiones de la notificación a NeoClient.enviar -------------------------------

    private fun botones(n: Notificacion) {
        if (terminada) return
        val contadores = SquareDecoder.contadores(n.datos) ?: return
        for (e in eventosBoton.procesar(contadores, n.tMs)) {
            if (_captura.value != null) {
                capturar(e)
                continue
            }
            val gestos = reconocedor.procesar(e)
            if (e is EventoBoton.Soltado) mantenidas.remove(e.boton)?.let(::soltarFuncion)
            if (apagado(e)) continue
            for (g in gestos) gesto(g)
        }
        _estado.update { it.copy(mascara = eventosBoton.mascara) }
        programarVencimiento()
    }

    /** Los gestos que vencen con el tiempo (larga, repetición, corta tras la ventana) siguen el mismo camino. */
    private fun programarVencimiento() {
        temporizador?.cancel()
        temporizador = null
        val t = reconocedor.proximoVencimiento() ?: return
        temporizador = ambito.launch {
            delay((t - relojMonotonico()).coerceAtLeast(0))
            temporizador = null
            val gestos = reconocedor.vencer(relojMonotonico())
            if (!apagando && _captura.value == null) for (g in gestos) gesto(g)
            programarVencimiento()
        }
    }

    /**
     * Apagar el Square (los dos Campagnolo con I1 y D1) no cambia la marcha (decisión 6): al pulsar el primero de los
     * cuatro se guarda la marcha y, si llegan a estar los cuatro pulsados, se vuelve a ella y no se hace nada más hasta
     * soltarlos. Devuelve si el evento [e] es parte de un apagado y sus gestos no cuentan.
     */
    private fun apagado(e: EventoBoton): Boolean {
        // La máscara de EventosBoton es la del final de la notificación; aquí cuenta la de cada evento.
        pulsadosApagado = when (e) {
            is EventoBoton.Pulsado -> e.mascara and MASCARA_APAGADO
            is EventoBoton.Soltado -> pulsadosApagado and e.boton.mascara.inv()
        }
        val pulsados = pulsadosApagado
        if (e is EventoBoton.Pulsado && e.boton.mascara and MASCARA_APAGADO != 0u && pulsados == e.boton.mascara) {
            antesDeApagar = _estado.value.marcha to (control.modo as? ModoControl.Erg)?.ajusteW
        }
        if (pulsados == 0u) {
            val eraApagado = apagando
            antesDeApagar = null
            apagando = false
            return eraApagado
        }
        if (!apagando && pulsados == MASCARA_APAGADO) {
            apagando = true
            antesDeApagar?.let { (marcha, ajusteW) -> deshacerHasta(marcha, ajusteW) }
        }
        return apagando
    }

    private fun deshacerHasta(marcha: EstadoMarcha, ajusteW: Int?) {
        registro.suceso(Origen.App, "Apagado del Square: se vuelve a la marcha ${marcha.texto()}")
        val erg = control.modo as? ModoControl.Erg
        if (erg != null && ajusteW != null && erg.ajusteW != ajusteW) {
            control.ajusteErg(ajusteW)?.let { enviarNeo(it, null) }
            recordarPotenciaErg()
            _estado.update { it.copy(simulacion = simulacion(it.marcha)) }
        }
        if (marcha != _estado.value.marcha) {
            control.alCambiarMarcha(marcha, relojMonotonico())?.let { enviarNeo(it, null) }
            _estado.update { it.copy(marcha = marcha, simulacion = simulacion(marcha)) }
            guardarEnUnRato()
        }
    }

    /** Captura de entradas en crudo (decisión 7): un botón con el Campagnolo pulsado, o un Campagnolo solo al soltarlo. */
    private fun capturar(e: EventoBoton) {
        when (e) {
            is EventoBoton.Pulsado -> {
                val otros = SquareButton.pulsados(e.mascara).filter { it != e.boton }
                if (e.boton.esModificador) {
                    campagnoloSolo = if (otros.isEmpty()) e.boton else null
                    return
                }
                campagnoloSolo = null
                val modificadores = otros.filter { it.esModificador }
                if (modificadores.size > 1) return
                _captura.value = EstadoCaptura.Capturada(Entrada(e.boton, modificadores.singleOrNull()))
            }

            is EventoBoton.Soltado -> if (e.boton == campagnoloSolo) {
                campagnoloSolo = null
                _captura.value = EstadoCaptura.Capturada(Entrada(e.boton))
            }
        }
    }

    private fun gesto(g: Gesto) = ejecutar(configuracion.asignacion.accion(g), g)

    /**
     * Ejecuta [accion], de un gesto [g] del Square o de la pantalla (`null`, como un «pulsar»). El último gesto, la
     * latencia y la parada de la repetición solo tienen sentido con un gesto.
     */
    private fun ejecutar(accion: Accion?, g: Gesto?) {
        if (accion is Accion.Mando && g != null) return funcionMando(g, accion)
        val tipo = g?.tipo ?: TipoGesto.Pulsar
        if (accion == Accion.AlternarErgManual) return alternarErg(g, accion)
        if (accion == Accion.PantallaAnterior || accion == Accion.PantallaSiguiente) return pantalla(g, accion)
        if (accion == Accion.SiguienteModelo) return siguienteModelo(g, accion)
        if (accion != null && accion in Accion.VENTILADOR) return accionVentilador(g, accion)
        val erg = control.modo as? ModoControl.Erg
        // Fijar la referencia no toca la potencia: en ERG va por el camino de la marcha (spec `zwift-bridge`). «+10 W» y
        // «−10 W» en SIM siguen ese camino y no cambian nada.
        if (accion != null && erg != null && accion != Accion.FijarReferencia) return gestoErg(g, accion, erg)
        val resultado = accion?.let { cambio.aplicar(_estado.value.marcha, it, _estado.value.referencia) }
        if (accion != null && resultado is ResultadoCambio.Cambiada) {
            control.alCambiarMarcha(resultado.nueva, relojMonotonico())?.let { enviarNeo(it, g?.let(::latenciaDe)) }
            envioTerreno?.cancel()
        }
        // Lo que no es el comando va después de entregarlo (arquitectura.md § 5.1).
        _estado.update {
            val marcha = if (resultado is ResultadoCambio.Cambiada) resultado.nueva else it.marcha
            val referencia = if (resultado is ResultadoCambio.ReferenciaFijada) resultado.referencia else it.referencia
            val ultimo = g?.let { GestoReconocido(it, accion, resultado) } ?: it.ultimoGesto
            it.copy(marcha = marcha, referencia = referencia, ultimoGesto = ultimo, simulacion = simulacion(marcha))
        }
        registro.suceso(Origen.App, "${g?.let { "Gesto: $it" } ?: "Pantalla"} → ${accion ?: "sin acción"} → ${resultado ?: "-"}")
        when (resultado) {
            is ResultadoCambio.Cambiada -> {
                _eventos.tryEmit(EventoRodaje.Cambio(accion!!, resultado))
                vibrar(tipo)
                guardarEnUnRato()
            }

            is ResultadoCambio.Tope -> {
                // El tope se avisa una vez: la repetición para hasta soltar (decisión 8).
                g?.let { reconocedor.pararRepeticion(it.entrada) }
                _eventos.tryEmit(EventoRodaje.Tope(resultado.tope))
                if (configuracion.vibracion) vibrador.doble()
            }

            is ResultadoCambio.ReferenciaFijada -> {
                fijarReferencia(resultado.referencia)
                _eventos.tryEmit(EventoRodaje.ReferenciaFijada(resultado.referencia))
                vibrar(tipo)
            }

            ResultadoCambio.SinCambio, null -> Unit
        }
    }

    /**
     * Una función de Zwift va al mando antes que el camino de ERG, sin vibración ni aviso: ya se nota en Zwift (design.md
     * del change `mando-zwift-emulado`, decisión 3). Con «pulsar» queda pulsada hasta soltar el botón; con corta, larga
     * o doble es una pulsación de [PULSACION_BREVE_MS]. Sin mando publicado no se envía nada.
     */
    private fun funcionMando(g: Gesto, accion: Accion.Mando) {
        _estado.update { it.copy(ultimoGesto = GestoReconocido(g, accion, null)) }
        registro.suceso(Origen.App, "Gesto: $g → $accion")
        if (_estado.value.servicios?.mando == null) return
        val f = accion.funcion
        if (g.tipo == TipoGesto.Pulsar) {
            mantenidas.put(g.entrada.boton, f)?.let(::soltarFuncion)
            pulsarFuncion(f)
        } else {
            pulsarFuncion(f)
            breves += ambito.launch {
                delay(PULSACION_BREVE_MS)
                soltarFuncion(f)
            }.also { j -> j.invokeOnCompletion { breves.remove(j) } }
        }
    }

    /** Todo suelto en Zwift: al perder el Square, al capturar, al desactivar el mando y al terminar. También borra el aviso de giro. */
    private fun soltarPlataforma() {
        mantenidas.clear()
        breves.toList().forEach { it.cancel() }
        breves.clear()
        puente.soltarTodo()
        retencionesGiro.values.forEach { it.cancel() }
        retencionesGiro.clear()
        actualizarGiro()
    }

    private fun pulsarFuncion(f: FuncionMando) {
        puente.pulsar(f)
        actualizarGiro()
    }

    private fun soltarFuncion(f: FuncionMando) {
        puente.soltar(f)
        actualizarGiro()
    }

    /** Los giros que se retienen un mínimo de [GIRO_MINIMO_MS] desde que empiezan, para que una pulsación breve se vea. */
    private val retencionesGiro = mutableMapOf<LadoGiro, Job>()
    private var giroAnterior: Set<LadoGiro> = emptySet()

    /**
     * El aviso de giro de Rodaje (spec `ride-screen`, «Aviso de giro»): lo que Zwift ve girar ahora más lo que giró hace
     * menos de [GIRO_MINIMO_MS]. Solo de presentación: el mando envía su trama sin esperar (design.md del change
     * `exportar-bateria-y-giro`, decisión 8).
     */
    private fun actualizarGiro() {
        val activo = puente.giro
        // Solo se retiene al empezar a girar, no cada vez que se recalcula mientras se mantiene.
        (activo - giroAnterior).forEach { lado ->
            retencionesGiro[lado]?.cancel()
            retencionesGiro[lado] = ambito.launch {
                delay(GIRO_MINIMO_MS)
                retencionesGiro.remove(lado)
                actualizarGiro()
            }
        }
        giroAnterior = activo
        val visible = activo + retencionesGiro.keys
        if (_estado.value.giro != visible) _estado.update { it.copy(giro = visible) }
    }

    /**
     * El mando tal como lo ve el usuario: se anota y se refleja en el estado; si deja de estar disponible, se suelta todo
     * (design.md del change `mando-zwift-emulado`, decisión 6).
     */
    private fun mandoCambia(nuevo: EstadoMando) {
        if (nuevo != EstadoMando.Esperando && nuevo != EstadoMando.Conectado) soltarPlataforma()
        if (_estado.value.mando != nuevo) {
            registro.suceso(Origen.Plataforma, "Mando: $nuevo")
            _estado.update { it.copy(mando = nuevo) }
        }
    }

    /** «Siguiente modelo»: pasa al modelo que sigue, lo aplica al momento y lo guarda como un ajuste (spec `resistance-models`). */
    private fun siguienteModelo(g: Gesto?, accion: Accion) {
        val nuevo = configuracion.modelo.siguiente()
        aplicarConfiguracion(configuracion.copy(modelo = nuevo))
        ambito.launch { ajustes.actualizarConfiguracion { it.copy(modelo = nuevo) } }
        g?.let { gesto -> _estado.update { it.copy(ultimoGesto = GestoReconocido(gesto, accion, null)) } }
        registro.suceso(Origen.App, "${g?.let { "Gesto: $it" } ?: "Pantalla"} → $accion → $nuevo")
        _eventos.tryEmit(EventoRodaje.ModeloCambiado(nuevo))
        vibrar(g?.tipo ?: TipoGesto.Pulsar)
    }

    /**
     * Una acción del ventilador (spec `fan-control`): la orden la decide y la envía [ventiladorSesion]. No toca la marcha,
     * el Neo ni Zwift.
     */
    private fun accionVentilador(g: Gesto?, accion: Accion) {
        val r = ventiladorSesion.accion(accion)
        g?.let { gesto -> _estado.update { it.copy(ultimoGesto = GestoReconocido(gesto, accion, null)) } }
        registro.suceso(Origen.App, "${g?.let { "Gesto: $it" } ?: "Pantalla"} → $accion → ${r.orden ?: "sin orden"}")
        r.aviso?.let { _eventos.tryEmit(EventoRodaje.Ventilador(it)) }
        vibrar(g?.tipo ?: TipoGesto.Pulsar)
    }

    /** Fija el modo desde Ajustes: pasa a [modo] manual o automático si el ventilador está conectado y en otro. */
    fun fijarModoVentilador(modo: ModoVentilador) {
        ambito.launch { ventiladorSesion.fijarModo(modo) }
    }

    /** Cambia el umbral de la [zona] (1 a 4) a [bpm] desde Ajustes ([VentiladorSesion.cambiarUmbral]). */
    fun cambiarUmbralVentilador(zona: Int, bpm: Int) {
        ambito.launch { ventiladorSesion.cambiarUmbral(zona, bpm) }
    }

    /** Pantalla anterior o siguiente: solo un evento para la UI, sin tocar el estado (decisión 4). */
    private fun pantalla(g: Gesto?, accion: Accion) {
        _eventos.tryEmit(EventoRodaje.Pantalla(siguiente = accion == Accion.PantallaSiguiente))
        g?.let { gesto -> _estado.update { it.copy(ultimoGesto = GestoReconocido(gesto, accion, null)) } }
        registro.suceso(Origen.App, "${g?.let { "Gesto: $it" } ?: "Pantalla"} → $accion")
        vibrar(g?.tipo ?: TipoGesto.Pulsar)
    }

    /**
     * En ERG, de Zwift o manual, las acciones de marcha y las de vatios ajustan la potencia (decisión 7; design.md del
     * change `erg-manual`, decisión 4); la marcha no cambia.
     */
    private fun gestoErg(g: Gesto?, accion: Accion, erg: ModoControl.Erg) {
        val r = AjusteErg.aplicar(accion, erg.objetivoW, erg.ajusteW)
        when (r) {
            is ResultadoAjuste.Cambiado -> control.ajusteErg(r.ajusteW)
            ResultadoAjuste.Quitado -> control.ajusteErg(0)
            else -> null
        }?.let { enviarNeo(it, g?.let(::latenciaDe)) }
        _estado.update {
            val ultimo = g?.let { GestoReconocido(it, accion, null, r) } ?: it.ultimoGesto
            it.copy(ultimoGesto = ultimo, simulacion = simulacion(it.marcha))
        }
        registro.suceso(Origen.App, "${g?.let { "Gesto en ERG: $it" } ?: "Pantalla en ERG"} → $accion → $r")
        when (r) {
            is ResultadoAjuste.Cambiado, ResultadoAjuste.Quitado -> {
                _eventos.tryEmit(EventoRodaje.Ajuste(accion, r, (erg as? ModoControl.ErgManual)?.entradaW))
                vibrar(g?.tipo ?: TipoGesto.Pulsar)
                if (erg is ModoControl.ErgManual) recordarPotenciaErg()
            }

            is ResultadoAjuste.Tope -> {
                g?.let { reconocedor.pararRepeticion(it.entrada) }
                _eventos.tryEmit(EventoRodaje.TopePotencia(r.potenciaW))
                if (configuracion.vibracion) vibrador.doble()
            }

            ResultadoAjuste.SinCambio -> Unit
        }
    }

    /**
     * «Alternar SIM ⇄ ERG» (spec `manual-erg`): entra en ERG manual con la última potencia o vuelve a SIM con la marcha;
     * en un entrenamiento de Zwift no cambia nada y lo avisa.
     */
    private fun alternarErg(g: Gesto?, accion: Accion) {
        val marcha = _estado.value.marcha
        recordarPotenciaErg()
        val r = control.alternarErgManual(potenciaErg, marcha)
        when (r) {
            is ResultadoAlternar.ErgManual -> r.comando
            is ResultadoAlternar.Sim -> r.comando
            ResultadoAlternar.PlataformaManda -> null
        }?.let { enviarNeo(it, null) }
        envioTerreno?.cancel()
        _estado.update {
            it.copy(ultimoGesto = g?.let { gesto -> GestoReconocido(gesto, accion, null) } ?: it.ultimoGesto, simulacion = simulacion(it.marcha))
        }
        registro.suceso(Origen.App, "${g?.let { "Gesto: $it" } ?: "Pantalla"} → $accion → $r")
        when (r) {
            is ResultadoAlternar.ErgManual -> {
                _eventos.tryEmit(EventoRodaje.ModoErg(activo = true, potenciaW = r.potenciaW))
                vibrar(g?.tipo ?: TipoGesto.Pulsar)
            }

            is ResultadoAlternar.Sim -> {
                _eventos.tryEmit(EventoRodaje.ModoErg(activo = false, potenciaW = potenciaErg, libre = control.modo is ModoControl.SimLibre))
                vibrar(g?.tipo ?: TipoGesto.Pulsar)
                guardarEnUnRato()
            }

            ResultadoAlternar.PlataformaManda -> {
                _eventos.tryEmit(EventoRodaje.ErgPlataformaActivo)
                if (configuracion.vibracion) vibrador.doble()
            }
        }
    }

    /** Toma la potencia del ERG manual vigente como la última, y la guarda en un rato. */
    private fun recordarPotenciaErg() {
        val erg = control.ergManual ?: return
        if (erg.potenciaW == potenciaErg) return
        potenciaErg = erg.potenciaW
        guardarEnUnRato()
    }

    /** La latencia solo se mide en la pulsación de una palanca, que llega con su notificación (NF1). */
    private fun latenciaDe(g: Gesto): Long? = g.tMs.takeIf { g.tipo == TipoGesto.Pulsar && g.entrada.esPalanca }

    private fun vibrar(tipo: TipoGesto) {
        if (!configuracion.vibracion) return
        when (tipo) {
            TipoGesto.Larga -> vibrador.larga()
            TipoGesto.Doble -> vibrador.triple()
            TipoGesto.Pulsar, TipoGesto.Corta, TipoGesto.Repetir -> vibrador.corta()
        }
    }

    /** Entrega [c] al Neo; si viene de una palanca ([t0] es la llegada de su notificación), mide la latencia hasta la entrega a la pila Bluetooth (sin respuesta, no hay confirmación del Neo). */
    private fun enviarNeo(c: ComandoRodillo, t0: Long?) {
        // Al terminar, lo último que recibe el Neo es la pendiente 0 % de terminar(): nada que llegue después la pisa.
        if (terminada) return
        val bytes = codificar(c)
        val t1 = relojMonotonico()
        neo.enviar(bytes) { ok ->
            val t2 = relojMonotonico()
            if (t0 == null) return@enviar
            if (ok) {
                registro.suceso(Origen.App, "Latencia: notificación $t0, envío $t1, entrega $t2 (${t2 - t0} ms)")
                _estado.update { it.copy(latenciaMs = t2 - t0) }
            } else {
                registro.suceso(Origen.App, "Latencia: notificación $t0, envío $t1, sin entrega")
            }
        }
        registro.suceso(Origen.App, "Comando: $c")
    }

    // --- Resto ----------------------------------------------------------------------------------------------------

    /**
     * Configuración nueva: los botones, la referencia, la transmisión y su modo cambian en caliente, sin perder los
     * botones pulsados (design.md del change `transmisiones-de-marca`, decisión 7).
     */
    private fun aplicarConfiguracion(c: Configuracion) {
        val modeloAnterior = claveModelo.tipo
        val primera = !configurada
        configuracion = c
        reconocedor.asignacion = c.asignacion
        reconocedor.tiempos = c.botones.tiempos
        reconocedor.autorrepeticion = c.botones.autorrepeticion
        val t = c.transmisionElegida
        if (!configurada || t != cambio.transmision) {
            cambiarTransmision(t, primera = !configurada)
        } else if (c.modo != cambio.modo) {
            registro.suceso(Origen.App, "Modo de cambio: ${c.modo}")
            cambio = Cambio(t, c.modo)
        }
        configurada = true
        _estado.update { it.copy(referencia = c.referencia, modoCambio = c.modo) }
        if (primera || c.modelo != modeloAnterior) registro.suceso(Origen.App, "Modelo: ${c.modelo}")
        actualizarModelo(c, t)
        if (c.dificultadPct != control.dificultadPct) cambiarDificultad(c.dificultadPct)
        if (c.potenciaMinimaW != control.potenciaMinimaW) cambiarPotenciaMinima(c.potenciaMinimaW)
        if (c.bajadas != control.bajadas) cambiarBajadas(c.bajadas)
        actualizarPlataforma(c)
    }

    /** Lo que, si cambia, obliga a crear el modelo de nuevo: el elegido, la transmisión, la tabla y los pesos. */
    private data class ClaveModelo(
        val tipo: TipoModelo,
        val transmision: Transmision,
        val incrementoPct: Double,
        val neutra: Int,
        val tabla: List<Double>?,
        val pesoCiclistaKg: Int,
        val pesoBiciKg: Double,
    )

    private fun claveDe(c: Configuracion, t: Transmision) = ClaveModelo(
        c.modelo,
        t,
        c.incrementoPct,
        c.lineal.neutra,
        c.tabla(t).takeIf { c.modelo == TipoModelo.TABLA },
        c.pesoCiclistaKg,
        c.pesoBiciKg,
    )

    /** Si el modelo elegido o lo que usa ha cambiado, se crea el nuevo y el Neo recibe el comando si cambia. */
    private fun actualizarModelo(c: Configuracion, t: Transmision) {
        val clave = claveDe(c, t)
        if (clave == claveModelo) return
        claveModelo = clave
        modelo = c.modeloResistencia(t)
        val marcha = _estado.value.marcha
        control.cambiarModelo(modelo, marcha)?.let { enviarNeo(it, null) }
        _estado.update { it.copy(simulacion = simulacion(marcha)) }
    }

    private fun cambiarDificultad(pct: Int) {
        registro.suceso(Origen.App, "Dificultad: $pct %")
        control.cambiarDificultad(pct, _estado.value.marcha)?.let { enviarNeo(it, null) }
        _estado.update { it.copy(simulacion = simulacion(it.marcha)) }
    }

    private fun cambiarPotenciaMinima(w: Int) {
        registro.suceso(Origen.App, "Potencia mínima: $w W")
        control.cambiarPotenciaMinima(w, _estado.value.marcha)?.let { enviarNeo(it, null) }
        _estado.update { it.copy(simulacion = simulacion(it.marcha)) }
    }

    /**
     * Otra transmisión: se guarda la marcha de la saliente y se pone la de la entrante (la última si se recuerda, o su
     * referencia), con su modelo; el Neo recibe la pendiente nueva si cambia. Sin aviso ni vibración.
     */
    private fun cambiarTransmision(t: Transmision, primera: Boolean) {
        if (!primera) {
            val saliente = cambio.transmision
            val actual = _estado.value.marcha
            ultimas = ultimas.conUltima(saliente, actual)
            ambito.launch { almacen.actualizar { it.conUltima(saliente, actual) } }
        }
        cambio = Cambio(t, configuracion.modo)
        modelo = configuracion.modeloResistencia(t)
        claveModelo = claveDe(configuracion, t)
        val marcha = ultimas.marchaInicial(configuracion, t)
        control.cambiarModelo(modelo, marcha)?.let { enviarNeo(it, null) }
        _estado.update { it.copy(marcha = marcha, transmision = t, simulacion = simulacion(marcha)) }
        if (!primera) registro.suceso(Origen.App, "Transmisión: ${t.id}, marcha ${marcha.texto()}")
    }

    /** El modo de bajadas cambia en caliente: se envía la pendiente nueva si cambia (design.md del change `proteccion-bajadas`, decisión 3). */
    private fun cambiarBajadas(modo: ModoBajadas) {
        registro.suceso(Origen.App, "Bajadas: $modo")
        control.cambiarBajadas(modo, _estado.value.marcha)?.let { enviarNeo(it, null) }
        _estado.update { it.copy(simulacion = simulacion(it.marcha)) }
    }

    /** La referencia se guarda en `configuracion.json`, en la transmisión elegida (spec `virtual-gears`, «Referencia guardada»). */
    private fun fijarReferencia(m: EstadoMarcha) {
        configuracion = configuracion.conReferencia(m)
        ambito.launch { ajustes.actualizarConfiguracion { it.conReferencia(m) } }
    }

    private fun telemetria(n: Notificacion) {
        when (n.caracteristica) {
            NeoClient.ETIQUETA_POTENCIA -> TelemetriaCodec.potencia(n.datos)?.let { w ->
                val rpm = cadencia.potencia(w)
                val ahora = if (w == 0) rpm else _estado.value.cadencia ?: rpm
                datosSesion.muestra(n.tMs, w, ahora)
                alCambiarCadencia(ahora) { it.copy(potencia = w, datos = datosSesion.resumen) }
            }

            NeoClient.ETIQUETA_CSC -> TelemetriaCodec.biela(n.datos)?.let { b ->
                biela = b
                alCambiarCadencia(cadencia.biela(b, n.tMs))
            }
        }
    }

    /**
     * Una muestra de cadencia: el modelo físico la usa para recalcular la potencia (decisión 4), y la velocidad y la
     * distancia se actualizan con la cadencia suavizada. El estado se emite una sola vez, con [ademas] (lo que trae la
     * muestra de potencia).
     */
    private fun alCambiarCadencia(rpm: Int?, ademas: (EstadoRodaje) -> EstadoRodaje = { it }) {
        val t = relojMonotonico()
        val marcha = _estado.value.marcha
        control.alCambiarCadencia((rpm ?: 0).toDouble(), marcha, t)?.let { enviarNeo(it, null) }
        val v = velocidadMs(marcha)
        if (v != null) recorrido.muestra(t, v) else recorrido.sinVelocidad(t)
        _estado.update {
            ademas(it).copy(
                cadencia = rpm,
                velocidadKmh = v?.times(3.6),
                distanciaKm = recorrido.distanciaM / 1000.0,
                simulacion = simulacion(marcha, v),
            )
        }
    }

    /** La velocidad del modelo físico con la cadencia suavizada, o `null` sin modelo físico o sin el Neo conectado. */
    private fun velocidadMs(marcha: EstadoMarcha): Double? {
        val f = modelo as? ModeloFisico ?: return null
        return if (_estado.value.neo == EstadoDispositivo.Conectado) f.velocidadMs(control.cadenciaSuaveRpm, marcha) else null
    }

    private fun simulacion(marcha: EstadoMarcha, velocidadMs: Double? = velocidadMs(marcha)) = Simulacion(
        pendienteBasePct = control.terrenoBase.pendientePct,
        ajustePct = (modelo as? ModeloSimple)?.ajuste(marcha) ?: (modelo as? ModeloTabla)?.ajuste(marcha) ?: 0.0,
        enviado = control.ultimoEnviado,
        modo = control.modo,
        plataformaEnEspera = control.plataformaEnEspera,
        ultimoPlataforma = ultimoPlataforma,
        ultimaSimulacionPlataforma = ultimaSimulacionPlataforma,
        bajadas = control.bajadas,
        sinLimitePct = control.sinLimite(marcha),
        tipoModelo = configuracion.modelo,
        dificultadPct = control.dificultadPct,
        fisico = fisicoSim(marcha, velocidadMs),
    )

    private fun fisicoSim(marcha: EstadoMarcha, velocidadMs: Double?): FisicoSim? {
        val f = modelo as? ModeloFisico ?: return null
        val sinLimite = control.potenciaFisicaSinLimite(marcha) ?: return null
        return FisicoSim(sinLimite.roundToInt(), control.potenciaMinimaW, control.cadenciaSuaveRpm.roundToInt(), velocidadMs?.times(3.6), f.relacion(marcha))
    }

    private fun guardarEnUnRato() {
        guardado?.cancel()
        guardado = ambito.launch {
            delay(RETARDO_GUARDADO_MS)
            guardar()
        }
    }

    private suspend fun guardar() {
        val t = cambio.transmision
        val marcha = _estado.value.marcha
        val vatios = potenciaErg
        ultimas = ultimas.conUltima(t, marcha).copy(ultimaPotenciaErgW = vatios)
        almacen.actualizar { it.conUltima(t, marcha).copy(ultimaPotenciaErgW = vatios) }
    }

    /** La marcha en el registro: «14 de 24» o «36×17». */
    private fun EstadoMarcha.texto(): String = when (this) {
        is EstadoMarcha.Numerada -> "$posicion de $total"
        is EstadoMarcha.PlatoPinon -> toString()
    }

    /** Lo que cambia en la sesión cuando cambia un dispositivo o los configurados ([ConexionesSesion]). */
    private inner class ReaccionesDeLaSesion : ReaccionesConexion {
        override fun cambia(d: Dispositivo, antes: EstadoDispositivo, nuevo: EstadoDispositivo) = when (d) {
            Dispositivo.Neo -> neoCambia(antes, nuevo)
            Dispositivo.Square -> squareCambia(nuevo)
            Dispositivo.Pulsometro -> pulsoCambia(nuevo)
            Dispositivo.Ventilador -> ventiladorSesion.cambia(nuevo)
        }

        override fun configuradosCambian() = publicarConfigurados()
    }

    /** Sin Square, nada queda pulsado ni repitiendo, y la captura espera a que vuelva. */
    private fun squareCambia(nuevo: EstadoDispositivo) {
        if (nuevo == EstadoDispositivo.Conectado) {
            if (_captura.value == EstadoCaptura.SinSquare) _captura.value = EstadoCaptura.Esperando
            return
        }
        eventosBoton.perdida(0)
        soltarBotones()
        if (_captura.value != null) _captura.value = EstadoCaptura.SinSquare
    }

    private fun soltarBotones() {
        soltarPlataforma()
        reconocedor.reiniciar()
        temporizador?.cancel()
        temporizador = null
        antesDeApagar = null
        apagando = false
        pulsadosApagado = 0u
        campagnoloSolo = null
    }

    private fun neoCambia(antes: EstadoDispositivo, nuevo: EstadoDispositivo) {
        if (nuevo == antes) return
        if (nuevo == EstadoDispositivo.Conectado) {
            // F1.3: al conectar, el Neo recibe la resistencia de la marcha puesta.
            enviarNeo(control.neoListo(_estado.value.marcha), null)
        } else if (antes == EstadoDispositivo.Conectado) {
            control.neoPerdido()
            cadencia.reiniciar()
            datosSesion.pausar()
            biela = null
            recorrido.sinVelocidad(relojMonotonico())
            _estado.update { it.copy(potencia = null, cadencia = null, velocidadKmh = null, datos = datosSesion.resumen) }
        }
        _estado.update { it.copy(simulacion = simulacion(it.marcha)) }
    }

    /**
     * Con el Bluetooth apagado, todo queda sin conexión y sin intentos; al encenderlo, o al tener por primera vez
     * Bluetooth y permiso, la sesión se conecta a los configurados (decisión 5).
     */
    private suspend fun bluetoothCambia(encendido: Boolean, permiso: Boolean) {
        _estado.update { it.copy(bluetoothEncendido = encendido) }
        if (!encendido) conexiones.bluetoothApagado()
        val puede = encendido && permiso
        if (puede && !puedeConectar) {
            conexiones.intentarConfigurados()
            // Con el Bluetooth otra vez disponible se reintenta el renombrado, antes de que la app se anuncie.
            if (configuradosLeidos) actualizarPlataforma(configuracion)
        }
        puedeConectar = puede
    }

    /**
     * Una lectura del pulsómetro: solo una válida (1–250 bpm) cuenta; se reenvía a Zwift si está suscrito al pulso, en
     * el hilo de control y sin pasar por el bucle de telemetría (design.md del change `pulso-zwift`, decisión 4).
     */
    private fun pulsoLeido(n: Notificacion) {
        val bpm = PulsoCodec.decodificar(n.datos) ?: return
        if (!pulsoVigente.registrar(bpm)) return
        val ahora = relojMonotonico()
        _estado.update { it.copy(pulso = bpm, pulsoInstanteMs = ahora) }
        caducidadPulso?.cancel()
        caducidadPulso = ambito.launch {
            delay(PulsoVigente.CADUCIDAD_MS)
            if (pulsoVigente.valor() == null) _estado.update { it.copy(pulso = null, pulsoInstanteMs = null) }
        }
        puente.pulso(bpm)
    }

    /** Sin conexión con la banda no hay pulso desde ya, sin esperar a los 5 s (spec `heart-rate`, «Pérdida del pulsómetro»). */
    private fun pulsoCambia(nuevo: EstadoDispositivo) {
        if (nuevo == EstadoDispositivo.Conectado) return
        pulsoVigente.perder()
        caducidadPulso?.cancel()
        _estado.update { it.copy(pulso = null, pulsoInstanteMs = null) }
    }

    /** Zwift se ha ido: se mantiene lo último 30 s y, si no vuelve, la pendiente base con la marcha (decisión 8). */
    private fun plataformaPerdida() {
        control.plataformaPerdida()
        if (!control.plataformaEnEspera) return
        registro.suceso(Origen.Plataforma, "Se mantiene lo último de Zwift ${ESPERA_PLATAFORMA_MS / 1000} s")
        envioTerreno?.cancel()
        esperaPlataforma?.cancel()
        esperaPlataforma = ambito.launch {
            delay(ESPERA_PLATAFORMA_MS)
            val c = control.plataformaAusente(_estado.value.marcha)
            registro.suceso(Origen.Plataforma, "Zwift no ha vuelto: pendiente base")
            c?.let { enviarNeo(it, null) }
            _estado.update { it.copy(simulacion = simulacion(it.marcha)) }
        }
        _estado.update { it.copy(simulacion = simulacion(it.marcha)) }
    }

    /** La telemetría de ahora para la plataforma: potencia, cadencia y biela del Neo, o todo a 0 sin Neo. */
    private fun telemetriaAPlataforma(): TelemetriaPlataforma {
        val e = _estado.value
        val conNeo = e.neo == EstadoDispositivo.Conectado
        return TelemetriaPlataforma(
            vatios = if (conNeo) e.potencia ?: 0 else 0,
            cadenciaRpm = if (conNeo) e.cadencia ?: 0 else 0,
            biela = if (conNeo) biela else null,
            // De partida, 0: la plataforma calcula la velocidad con la potencia (design.md, decisión 4; se valida).
            velocidadKmh = 0.0,
        )
    }

    /**
     * Lo que el puente le cuenta a la `Sesion` y le pide: lo que llega de la plataforma, ya en términos del dominio y
     * venga por el modo que venga (design.md del change `mando-zwift-dircon`, decisión 1), el estado del enlace y las
     * identidades guardadas. Todo llega en el hilo de control.
     */
    private inner class DestinoDeLaSesion : DestinoPlataforma {
        override fun ultimoDePlataforma(ultimo: UltimoDePlataforma) {
            when (ultimo) {
                is UltimoDePlataforma.Comando -> ultimoPlataforma = MensajePlataforma(ultimo.comando, relojPared())
                is UltimoDePlataforma.Simulacion -> ultimaSimulacionPlataforma = SimulacionPlataformaRecibida(ultimo.simulacion, relojPared(), relojMonotonico())
            }
        }

        override fun terreno(terreno: Terreno) = entregarDePlataforma(control.terrenoPlataforma(terreno, _estado.value.marcha, relojMonotonico()))

        override fun potenciaErg(vatios: Int) {
            // Un entrenamiento termina el ERG manual: su potencia queda recordada (spec `manual-erg`).
            recordarPotenciaErg()
            entregarDePlataforma(control.potenciaPlataforma(vatios))
        }

        override fun sinCambio() = entregarDePlataforma(null)

        override fun perdida() = plataformaPerdida()

        override fun estadoPlataforma(estado: EstadoPlataforma) {
            _estado.update { it.copy(plataforma = estado) }
        }

        override fun soltarTodo() = soltarPlataforma()

        override fun conexionNueva(modo: ModoConexion, nombre: String) {
            _estado.update {
                it.copy(
                    modoConexion = modo,
                    enlaceConexion = Compatibilidad.deModo(modo).enlace,
                    nombreAnunciado = nombre,
                )
            }
        }

        override fun usoServicios(rodillo: Boolean, pulso: Boolean) {
            if (_estado.value.usoRodillo == rodillo && _estado.value.usoPulso == pulso) return
            _estado.update { it.copy(usoRodillo = rodillo, usoPulso = pulso) }
        }

        override fun estadoMando(estado: EstadoMando) = mandoCambia(estado)

        override fun identidad(clave: String): IdentidadDispositivo? = ultimas.identidades[clave]

        override fun guardarIdentidad(clave: String, identidad: IdentidadDispositivo) {
            ultimas = ultimas.copy(identidades = ultimas.identidades + (clave to identidad))
            ambito.launch { almacen.actualizar { it.copy(identidades = it.identidades + (clave to identidad)) } }
        }
    }

    /**
     * Publica lo que corresponde al modo de [c] y a los dispositivos configurados: «configurado = se publica» (spec
     * `platform-bridge`, «Lo publicado según los dispositivos configurados»). Hasta leer los configurados no se publica nada.
     */
    private fun actualizarPlataforma(c: Configuracion) {
        // Terminando, terminar() retira la publicación y devuelve el nombre: no se vuelve a publicar ni a renombrar.
        if (!configuradosLeidos || terminada) return
        val configurados = conexiones.configurados
        val r = Configurados(
            rodillo = Dispositivo.Neo in configurados,
            mando = Dispositivo.Square in configurados,
            pulsometro = Dispositivo.Pulsometro in configurados,
        )
        val servicios = Publicacion.de(c.conexion.modo, r)
        _estado.update { it.copy(servicios = servicios) }
        val identidad = servicios?.let { puente.identidadDe(it, c.identidadEfectiva) }
        val deseado = NombreBluetooth.deseado(c, servicios, identidad)
        aplicandoPlataforma?.cancel()
        if (deseado == null && renombrador.estado.value == EstadoNombre.Propio) {
            puente.usar(servicios, c.identidadEfectiva)
            return
        }
        // El nombre va antes que la publicación: al anunciarse, el adaptador ya tiene el nombre nuevo.
        aplicandoPlataforma = ambito.launch {
            val cambio = renombrador.aplicar(deseado)
            val rehecha = puente.usar(servicios, c.identidadEfectiva)
            if (cambio && !rehecha) puente.reanunciarBluetooth()
        }
    }

    /** Lo común a lo que llega de Zwift por FTMS o por `SET`: enviar lo que cambia al Neo y programar la ventana del terreno. */
    private fun entregarDePlataforma(enviar: ComandoRodillo?) {
        if (!control.plataformaEnEspera) esperaPlataforma?.cancel()
        enviar?.let { enviarNeo(it, null) }
        programarTerreno()
        _estado.update { it.copy(simulacion = simulacion(it.marcha)) }
    }

    /** Si hay un terreno guardado por la ventana, se envía al cumplirse, con la marcha de ese momento. */
    private fun programarTerreno() {
        val en = control.proximoEnvioMs() ?: return
        if (envioTerreno?.isActive == true) return
        envioTerreno = ambito.launch {
            delay((en - relojMonotonico()).coerceAtLeast(0))
            control.enviarPendiente(_estado.value.marcha, relojMonotonico())?.let { enviarNeo(it, null) }
            _estado.update { it.copy(simulacion = simulacion(it.marcha)) }
        }
    }

    /** [ControlRodillo] ya entrega los comandos dentro de rango; se vuelve a recortar porque `FecCodec` lanzaría una excepción. */
    private fun codificar(comando: ComandoRodillo): ByteArray = when (val c = comando.limitado()) {
        is ComandoRodillo.Pendiente -> FecCodec.pendiente(c.pct)
        is ComandoRodillo.Potencia -> FecCodec.potencia(c.vatios)
    }

    companion object {
        const val LIMITE_TERMINAR_MS = 500L

        /** Lo que se espera a cada desconexión al terminar la sesión: si no llega, se sigue (el ámbito se cancela y la app se cierra). */
        const val LIMITE_DESCONEXION_MS = 1_500L

        /** Lo que se espera a que termine la cancelación de un intento antes de cancelar la conexión pendiente de Nordic (decisión 6). */
        const val LIMITE_CANCELAR_MS = 10_000L

        /** Espera a la confirmación del ventilador antes de repetir una orden o dar por fallida una lectura (decisión 3). */
        const val ESPERA_VENTILADOR_MS = 2_000L

        /** Rebote de los cambios de umbral desde Ajustes: mantener el + manda una sola escritura (decisión 4). */
        const val REBOTE_UMBRALES_MS = 400L
        const val RETARDO_GUARDADO_MS = 2_000L
        const val ESPERA_PLATAFORMA_MS = 30_000L

        /** Lo que dura en Zwift una función con corta, larga o doble. */
        const val PULSACION_BREVE_MS = 100L
        const val GIRO_MINIMO_MS = 500L

        /** Los dos Campagnolo con I1 y D1 apagan el Square. */
        val MASCARA_APAGADO = SquareButton.CampagnoloIzquierdo.mascara or SquareButton.CampagnoloDerecho.mascara or
            SquareButton.PalancaIzquierda1.mascara or SquareButton.PalancaDerecha1.mascara
    }
}
