package app.smartbike.sesion

import app.smartbike.ble.BuscadorDispositivos
import app.smartbike.ble.ConexionBle
import app.smartbike.ble.EstadoBluetooth
import app.smartbike.ble.EstadoEnlace
import app.smartbike.ble.ResultadoBusqueda
import app.smartbike.datos.AlmacenEstado
import app.smartbike.datos.Origen
import app.smartbike.datos.RegistroSesion
import app.smartbike.dominio.DispositivoConfigurado
import app.smartbike.dominio.EstadoGuardado
import app.smartbike.sesion.Sesion.Companion.LIMITE_CANCELAR_MS
import app.smartbike.sesion.Sesion.Companion.LIMITE_DESCONEXION_MS
import app.smartbike.sesion.Sesion.Dispositivo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/** Lo que la [Sesion] hace cuando cambia un dispositivo o los configurados; todo llega en su hilo de control. */
internal interface ReaccionesConexion {
    /** [d] ha pasado de [antes] a [nuevo], ya fijado en el estado: lo que cambia en la sesión (Neo, botones, pulso, ventilador). */
    fun cambia(d: Dispositivo, antes: EstadoDispositivo, nuevo: EstadoDispositivo)

    /** Se ha recordado u olvidado un dispositivo: hay que volver a publicar. */
    fun configuradosCambian()
}

/**
 * Las conexiones de la [Sesion] con el Neo, el Square, el pulsómetro y el ventilador (spec `device-connection`; design.md
 * del change `conexion-automatica-en-segundo-plano`): las búsquedas, los intentos con los configurados, la reconexión al
 * perderlos y el estado de cada uno tal como lo ve el usuario. Escribe en su [estado] y corre en su hilo de control.
 */
internal class ConexionesSesion(
    private val ambito: CoroutineScope,
    private val buscador: BuscadorDispositivos,
    private val neo: ConexionBle,
    private val square: ConexionBle,
    private val pulsometro: ConexionBle,
    private val ventilador: ConexionBle,
    private val bluetooth: EstadoBluetooth,
    private val registro: RegistroSesion,
    private val almacen: AlmacenEstado,
    private val estado: MutableStateFlow<EstadoRodaje>,
    private val reacciones: ReaccionesConexion,
) {
    /** La búsqueda en curso de cada tipo (rodillo y mando, cada uno con la suya: spec `device-connection`). */
    private val busquedas = mutableMapOf<Dispositivo, Job>()
    private val buscando: Boolean get() = busquedas.values.any { !it.isCompleted }
    private var busquedaPulso: Job? = null
    private var busquedaVentilador: Job? = null

    private val _configurados = mutableMapOf<Dispositivo, DispositivoConfigurado>()

    /** Los dispositivos configurados en `estado.json`, uno de cada tipo. */
    val configurados: Map<Dispositivo, DispositivoConfigurado> get() = _configurados

    /** Al que se está conectando cada uno: se recuerda al llegar a `Listo`, no al encontrarlo (decisión 1). */
    private val candidatos = mutableMapOf<Dispositivo, DispositivoConfigurado>()

    /** Conexión en curso de cada dispositivo: intento con el configurado o tras encontrarlo al buscar. */
    private val intentos = mutableMapOf<Dispositivo, Job>()

    /** Tras una búsqueda sin resultado, no se intenta solo hasta que vuelva el Bluetooth o se abra otra vez (decisión 4). */
    private val sinIntento = mutableSetOf<Dispositivo>()
    private var terminada = false

    /** Los configurados guardados, al empezar la sesión. */
    fun cargar(g: EstadoGuardado) {
        g.rodillo?.let { _configurados[Dispositivo.Neo] = it }
        g.mando?.let { _configurados[Dispositivo.Square] = it }
        g.pulsometro?.let { _configurados[Dispositivo.Pulsometro] = it }
        g.ventilador?.let { _configurados[Dispositivo.Ventilador] = it }
    }

    fun estadoDe(d: Dispositivo) = when (d) {
        Dispositivo.Neo -> estado.value.neo
        Dispositivo.Square -> estado.value.square
        Dispositivo.Pulsometro -> estado.value.pulsometro
        Dispositivo.Ventilador -> estado.value.ventilador
    }

    /** Busca el Neo y el Square a la vez, y recupera el pulsómetro y el ventilador configurados ([Sesion.buscar]). */
    suspend fun buscar() {
        if (!estado.value.bluetoothEncendido) return
        reconectarPulsometro()
        reconectarVentilador()
        buscarTipos(listOf(Dispositivo.Neo, Dispositivo.Square))
    }

    /** «Buscar» de la sección Rodillo o de la sección Mando, sin tocar la conexión del otro. */
    suspend fun buscarUno(d: Dispositivo) {
        if (estado.value.bluetoothEncendido) buscarTipos(listOf(d))
    }

    /**
     * «Reconectar» (spec `device-connection`, «Reconectar un dispositivo configurado»): reinicia en el momento el intento con
     * el configurado de [d], por su dirección y no por su nombre, y la app sigue intentándolo sola después.
     */
    suspend fun reconectar(d: Dispositivo) {
        if (!estado.value.bluetoothEncendido || d !in _configurados || estadoDe(d) == EstadoDispositivo.Conectado) return
        registro.suceso(d.origen, "Reconectar")
        sinIntento -= d
        cancelarIntento(d)
        intentar(d, EstadoDispositivo.Conectando)
    }

    /**
     * Busca los de [tipos] que no están conectados ni buscándose, también los que se están intentando conectar, cuyo
     * intento se cancela (decisión 4). Sin resultado, el dispositivo queda en `NoEncontrado` y sin intentos.
     */
    private suspend fun buscarTipos(tipos: List<Dispositivo>) {
        val faltan = tipos.filter { estadoDe(it) != EstadoDispositivo.Conectado && busquedas[it]?.isCompleted != false }
        if (faltan.isEmpty()) return
        // Se registra antes de suspender en cancelarIntento: un segundo «Buscar» mientras tanto ya la ve y no lanza otra.
        val busqueda = ambito.launch(start = CoroutineStart.LAZY) {
            buscador.buscar(faltan.map { it.prefijo }.toSet()).collect { r ->
                val d = Dispositivo.entries.first { it.prefijo == r.prefijo }
                when (r) {
                    is ResultadoBusqueda.NoEncontrado -> noEncontrado(d)

                    is ResultadoBusqueda.Encontrado -> {
                        fijar(d, EstadoDispositivo.Conectando)
                        candidatos[d] = DispositivoConfigurado(r.direccion, r.nombre)
                        intentos[d] = ambito.launch {
                            if (!conexion(d).conectar(r.direccion)) noEncontrado(d)
                        }
                    }
                }
            }
        }
        faltan.forEach { busquedas[it] = busqueda }
        faltan.forEach { cancelarIntento(it) }
        // Si mientras tanto se terminó la sesión o se apagó el Bluetooth, la búsqueda ya está cancelada.
        if (busqueda.isCompleted) return
        faltan.forEach { fijar(it, EstadoDispositivo.Buscando) }
        registro.suceso(Origen.App, "Buscando: ${faltan.joinToString()}")
        busqueda.start()
    }

    /** Con un pulsómetro configurado que no está conectado, «Buscar dispositivos» intenta recuperarlo, sin buscar otro. */
    private suspend fun reconectarPulsometro() {
        val d = Dispositivo.Pulsometro
        if (d !in _configurados || estadoDe(d) == EstadoDispositivo.Conectado || busquedaPulso?.isActive == true) return
        sinIntento -= d
        cancelarIntento(d)
        intentar(d, EstadoDispositivo.Conectando)
    }

    /** Con un ventilador configurado que no está conectado, «Buscar dispositivos» intenta recuperarlo, sin buscar otro. */
    private suspend fun reconectarVentilador() {
        val d = Dispositivo.Ventilador
        if (d !in _configurados || estadoDe(d) == EstadoDispositivo.Conectado || busquedaVentilador?.isActive == true) return
        sinIntento -= d
        cancelarIntento(d)
        intentar(d, EstadoDispositivo.Conectando)
    }

    /**
     * «Buscar ventilador» (spec `device-connection`): busca el primer dispositivo cuyo nombre empiece por `HEADWIND` y se
     * conecta a él, sin elegirlo de una lista; si ya hay otro conectado, este se mantiene hasta que aparezca uno distinto.
     * Sin resultado en 30 s, `NoEncontrado`.
     */
    suspend fun buscarVentilador() {
        val d = Dispositivo.Ventilador
        if (!estado.value.bluetoothEncendido || busquedaVentilador?.isActive == true) return
        val conectado = estadoDe(d) == EstadoDispositivo.Conectado
        if (!conectado) {
            cancelarIntento(d)
            fijar(d, EstadoDispositivo.Buscando)
        }
        registro.suceso(Origen.App, "Buscando: ventilador")
        busquedaVentilador = ambito.launch {
            buscador.buscar(setOf(d.prefijo)).collect { r ->
                when (r) {
                    is ResultadoBusqueda.NoEncontrado -> if (estadoDe(d) != EstadoDispositivo.Conectado) noEncontrado(d)

                    is ResultadoBusqueda.Encontrado -> {
                        if (estadoDe(d) == EstadoDispositivo.Conectado && _configurados[d]?.direccion != r.direccion) {
                            ventilador.desconectar()
                            fijar(d, EstadoDispositivo.SinConexion)
                            reacciones.cambia(d, EstadoDispositivo.Conectado, EstadoDispositivo.SinConexion)
                        } else if (estadoDe(d) == EstadoDispositivo.Conectado) {
                            return@collect
                        }
                        fijar(d, EstadoDispositivo.Conectando)
                        candidatos[d] = DispositivoConfigurado(r.direccion, r.nombre)
                        intentos[d] = ambito.launch {
                            if (!conexion(d).conectar(r.direccion)) noEncontrado(d)
                        }
                    }
                }
            }
        }
    }

    /**
     * «Buscar pulsómetro» (spec `device-connection`): busca la primera banda que anuncie el servicio de frecuencia
     * cardiaca y se conecta a ella; si ya hay otra conectada, esta se mantiene hasta que aparezca una distinta. Sin
     * resultado en 30 s, `NoEncontrado`.
     */
    suspend fun buscarPulsometro() {
        val d = Dispositivo.Pulsometro
        if (!estado.value.bluetoothEncendido || busquedaPulso?.isActive == true) return
        val conectado = estadoDe(d) == EstadoDispositivo.Conectado
        if (!conectado) {
            cancelarIntento(d)
            fijar(d, EstadoDispositivo.Buscando)
        }
        registro.suceso(Origen.App, "Buscando: pulsómetro")
        busquedaPulso = ambito.launch {
            buscador.buscarPulsometro().collect { r ->
                when (r) {
                    is ResultadoBusqueda.NoEncontrado -> if (estadoDe(d) != EstadoDispositivo.Conectado) noEncontrado(d)

                    is ResultadoBusqueda.Encontrado -> {
                        if (estadoDe(d) == EstadoDispositivo.Conectado) {
                            pulsometro.desconectar()
                            fijar(d, EstadoDispositivo.SinConexion)
                            reacciones.cambia(d, EstadoDispositivo.Conectado, EstadoDispositivo.SinConexion)
                        }
                        fijar(d, EstadoDispositivo.Conectando)
                        candidatos[d] = DispositivoConfigurado(r.direccion, r.nombre)
                        intentos[d] = ambito.launch {
                            if (!conexion(d).conectar(r.direccion)) noEncontrado(d)
                        }
                    }
                }
            }
        }
    }

    /**
     * «Olvidar» (spec `device-connection`, «Olvidar un dispositivo»): deja de configurarlo, cancela el intento en curso y
     * lo desconecta; la app no se conecta a él hasta que una búsqueda lo encuentre (design.md del change
     * `rodaje-completo-y-ajustes`, decisión 8). La marcha no cambia.
     */
    suspend fun olvidar(d: Dispositivo) {
        if (_configurados.remove(d) == null) return
        registro.suceso(d.origen, "Olvidado")
        reacciones.configuradosCambian()
        cancelarIntento(d)
        val antes = estadoDe(d)
        if (antes == EstadoDispositivo.Conectado) conexion(d).desconectar()
        fijar(d, EstadoDispositivo.SinConexion)
        if (d == Dispositivo.Square) estado.update { it.copy(bateriaSquare = null) }
        reacciones.cambia(d, antes, EstadoDispositivo.SinConexion)
        almacen.actualizar {
            when (d) {
                Dispositivo.Neo -> it.copy(rodillo = null)
                Dispositivo.Square -> it.copy(mando = null)
                Dispositivo.Pulsometro -> it.copy(pulsometro = null)
                Dispositivo.Ventilador -> it.copy(ventilador = null)
            }
        }
    }

    private fun noEncontrado(d: Dispositivo) {
        sinIntento += d
        fijar(d, EstadoDispositivo.NoEncontrado)
    }

    /** Un cambio del enlace de [d]: lo que ve el usuario, lo que cambia en la sesión y, si se ha perdido, el intento de recuperarlo. */
    fun enlace(e: EstadoEnlace, d: Dispositivo) {
        val antes = estadoDe(d)
        val enCurso = setOf(EstadoDispositivo.Buscando, EstadoDispositivo.Conectando, EstadoDispositivo.Reconectando)
        val nuevo = when (e) {
            EstadoEnlace.Listo -> {
                configurar(d)
                EstadoDispositivo.Conectado
            }

            // Un dispositivo conectado está configurado: se intenta recuperar mientras dure la sesión (F1.3).
            EstadoEnlace.Perdida ->
                if (!terminada && bluetooth.encendido.value && d in _configurados) EstadoDispositivo.Reconectando else EstadoDispositivo.SinConexion

            // Durante un intento o una búsqueda, los pasos intermedios no cambian lo que ve el usuario.
            EstadoEnlace.Conectando, EstadoEnlace.Descubriendo -> if (antes in enCurso) return else EstadoDispositivo.Conectando

            // Un fallo al conectar lo resuelve el intento o la búsqueda; el estado inicial no pisa lo que digan.
            EstadoEnlace.Fallo, EstadoEnlace.Desconectado ->
                if (antes == EstadoDispositivo.Conectado) EstadoDispositivo.SinConexion else return
        }
        fijar(d, nuevo)
        reacciones.cambia(d, antes, nuevo)
        if (nuevo == EstadoDispositivo.Reconectando) intentar(d, EstadoDispositivo.Reconectando)
    }

    /** Recuerda el dispositivo al que se acaba de conectar, en lugar del anterior del mismo tipo (decisión 1). */
    private fun configurar(d: Dispositivo) {
        sinIntento -= d
        val r = candidatos.remove(d) ?: return
        if (_configurados[d] == r) return
        _configurados[d] = r
        reacciones.configuradosCambian()
        registro.suceso(d.origen, "Se recuerda: ${r.nombre} (${r.direccion})")
        ambito.launch {
            almacen.actualizar {
                when (d) {
                    Dispositivo.Neo -> it.copy(rodillo = r)
                    Dispositivo.Square -> it.copy(mando = r)
                    Dispositivo.Pulsometro -> it.copy(pulsometro = r)
                    Dispositivo.Ventilador -> it.copy(ventilador = r)
                }
            }
        }
    }

    /**
     * Intento con el configurado (decisión 2): primero directa, que conecta en 1–2 s si está despierto; si falla, en
     * segundo plano, sin límite de tiempo, hasta que se anuncie. Mientras dura no se reintenta nada más.
     */
    private fun intentar(d: Dispositivo, visible: EstadoDispositivo) {
        val r = _configurados[d] ?: return
        intentos.remove(d)?.cancel()
        fijar(d, visible)
        candidatos[d] = r
        intentos[d] = ambito.launch {
            val c = conexion(d)
            if (c.conectar(r.direccion)) return@launch
            registro.suceso(d.origen, "Sin respuesta: se espera a que se anuncie")
            // Una banda configurada que no responde (apagada, fuera de alcance) se está reconectando, sin aviso de error.
            if (d == Dispositivo.Pulsometro || d == Dispositivo.Ventilador) fijar(d, EstadoDispositivo.Reconectando)
            if (!c.conectarEnSegundoPlano(r.direccion)) {
                registro.suceso(d.origen, "La conexión en segundo plano ha fallado")
                fijar(d, EstadoDispositivo.SinConexion)
            }
        }
    }

    /** Intento con los configurados que no están conectados ni en otra conexión, al abrir o al volver el Bluetooth. */
    fun intentarConfigurados() {
        if (terminada || buscando) return
        for (d in Dispositivo.entries) {
            if (d in sinIntento || estadoDe(d) == EstadoDispositivo.Conectado || intentos[d]?.isActive == true) continue
            intentar(d, EstadoDispositivo.Conectando)
        }
    }

    private suspend fun cancelarIntento(d: Dispositivo) {
        val intento = intentos.remove(d) ?: return
        candidatos.remove(d)
        if (!intento.isActive) return
        // Nunca se lanza un intento nuevo con una petición pendiente en la cola de Nordic (design.md del change
        // `endurecer-dircon-ble`, decisión 6): o la desconexión termina, o se vacía la cola cerrando el gestor.
        val c = conexion(d)
        val termino = withTimeoutOrNull(LIMITE_CANCELAR_MS) {
            intento.cancelAndJoin()
            c.desconectar()
        }
        if (termino == null) {
            registro.suceso(d.origen, "La desconexión no termina en ${LIMITE_CANCELAR_MS / 1000} s; se cancela la conexión pendiente")
            c.cancelarConexionPendiente()
        }
    }

    /** Con el Bluetooth apagado, todo queda sin conexión y sin intentos (decisión 5). */
    suspend fun bluetoothApagado() {
        busquedas.values.forEach { it.cancel() }
        busquedaPulso?.cancel()
        busquedaVentilador?.cancel()
        var antesSquare = EstadoDispositivo.SinConexion
        Dispositivo.entries.forEach { d ->
            cancelarIntento(d)
            val antes = estadoDe(d)
            fijar(d, EstadoDispositivo.SinConexion)
            if (d == Dispositivo.Square) antesSquare = antes else reacciones.cambia(d, antes, EstadoDispositivo.SinConexion)
        }
        // El Square, después de los demás: sin botones pulsados y con la captura esperando a que vuelva.
        reacciones.cambia(Dispositivo.Square, antesSquare, EstadoDispositivo.SinConexion)
        // Al volver el Bluetooth se intenta otra vez también con los que no se encontraron.
        sinIntento.clear()
    }

    /** Al terminar la sesión, lo primero: ni búsquedas ni intentos, y no se reconecta nada. */
    fun detener() {
        terminada = true
        busquedas.values.forEach { it.cancel() }
        busquedaPulso?.cancel()
        busquedaVentilador?.cancel()
        intentos.values.forEach { it.cancel() }
        intentos.clear()
    }

    /**
     * Desconecta los cuatro. Con un intento de conexión pendiente (por ejemplo, el Square dormido tras apagar y encender
     * el Bluetooth) la desconexión puede no terminar nunca: cada una tiene su límite para que la sesión siempre acabe.
     */
    suspend fun desconectarTodo() {
        conLimite(registro) { neo.desconectar() }
        conLimite(registro) { square.desconectar() }
        conLimite(registro) { pulsometro.desconectar() }
        conLimite(registro) { ventilador.desconectar() }
    }

    /** El estado final al terminar, sin esperar a los colectores del enlace. */
    fun sinConexion() = Dispositivo.entries.forEach { fijar(it, EstadoDispositivo.SinConexion) }

    private fun conexion(d: Dispositivo): ConexionBle = when (d) {
        Dispositivo.Neo -> neo
        Dispositivo.Square -> square
        Dispositivo.Pulsometro -> pulsometro
        Dispositivo.Ventilador -> ventilador
    }

    private fun fijar(d: Dispositivo, e: EstadoDispositivo) {
        if (estadoDe(d) == e) return
        registro.suceso(d.origen, "Estado: $e")
        estado.update {
            when (d) {
                Dispositivo.Neo -> it.copy(neo = e)
                Dispositivo.Square -> it.copy(square = e, mascara = if (e == EstadoDispositivo.Conectado) it.mascara ?: 0u else null)
                Dispositivo.Pulsometro -> it.copy(pulsometro = e)
                Dispositivo.Ventilador -> it.copy(ventilador = e)
            }
        }
    }
}

/** El origen de lo que se anota en el registro sobre un dispositivo. */
internal val Dispositivo.origen
    get() = when (this) {
        Dispositivo.Neo -> Origen.Neo
        Dispositivo.Square -> Origen.Square
        Dispositivo.Pulsometro -> Origen.Pulso
        Dispositivo.Ventilador -> Origen.Ventilador
    }

/** Espera a [desconexion] como mucho [LIMITE_DESCONEXION_MS]; si no termina, lo anota y sigue. */
internal suspend fun conLimite(registro: RegistroSesion, desconexion: suspend () -> Unit) {
    if (withTimeoutOrNull(LIMITE_DESCONEXION_MS) { desconexion() } == null) {
        registro.suceso(Origen.App, "Una desconexión no termina; se sigue sin esperarla")
    }
}
