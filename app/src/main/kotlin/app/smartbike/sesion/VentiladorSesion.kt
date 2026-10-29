package app.smartbike.sesion

import app.smartbike.ble.ConexionVentilador
import app.smartbike.ble.EstadoEnlace
import app.smartbike.ble.Notificacion
import app.smartbike.datos.Origen
import app.smartbike.datos.RegistroSesion
import app.smartbike.dominio.Accion
import app.smartbike.dominio.ConfiguracionVentilador
import app.smartbike.dominio.EstadoVentilador
import app.smartbike.dominio.ModoVentilador
import app.smartbike.dominio.OrdenVentilador
import app.smartbike.dominio.ReglasVentilador
import app.smartbike.dominio.ResultadoVentilador
import app.smartbike.protocolo.NotificacionVentilador
import app.smartbike.protocolo.VentiladorCodec
import app.smartbike.sesion.Sesion.Companion.ESPERA_VENTILADOR_MS
import app.smartbike.sesion.Sesion.Companion.REBOTE_UMBRALES_MS
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Las órdenes al ventilador y lo que comunica (spec `fan-control`; design.md del change `ventilador-headwind`, decisiones 3
 * y 4), para la [Sesion]: escribe en su [estado] y corre en su hilo de control. La conexión la lleva [ConexionesSesion].
 */
internal class VentiladorSesion(
    private val ambito: CoroutineScope,
    private val ventilador: ConexionVentilador,
    private val registro: RegistroSesion,
    private val estado: MutableStateFlow<EstadoRodaje>,
    private val relojMonotonico: () -> Long,
    /** Si hay un ventilador configurado: sin él, al desconectarse se olvida su estado. */
    private val configurado: () -> Boolean,
) {
    private var objetivoVelocidad: Int? = null
    private var verificacion: Job? = null
    private var escrituraUmbrales: Job? = null
    private val cambiosUmbral = mutableMapOf<Int, Int>()
    private val mutexConfiguracion = Mutex()
    private val respuestas = MutableSharedFlow<NotificacionVentilador>(extraBufferCapacity = 16)

    /**
     * Una acción del ventilador: la regla pura decide la orden y el aviso, y la orden se envía. Sin ventilador conectado
     * no se envía ni se encola nada.
     */
    fun accion(accion: Accion): ResultadoVentilador {
        val actual = estado.value.ventiladorEstado?.takeIf { estado.value.ventilador == EstadoDispositivo.Conectado }
        val r = when (accion) {
            Accion.VentiladorAlternar -> ReglasVentilador.alternar(actual)
            Accion.VentiladorMas -> ReglasVentilador.masVelocidad(actual, objetivoVelocidad)
            else -> ReglasVentilador.menosVelocidad(actual, objetivoVelocidad)
        }
        r.orden?.let(::enviar)
        return r
    }

    /** Pasa a [modo] manual o automático si el ventilador está conectado y en otro. */
    fun fijarModo(modo: ModoVentilador) {
        val actual = estado.value.ventiladorEstado ?: return
        if (estado.value.ventilador != EstadoDispositivo.Conectado || modo == ModoVentilador.Otro || actual.modo == modo) return
        enviar(OrdenVentilador.Modo(modo))
    }

    /**
     * Manda la orden y la verifica con lo que el ventilador notifique: si en [ESPERA_VENTILADOR_MS] no llega, la repite
     * una vez y, si tampoco, se queda con lo que él comunique (decisión 3).
     */
    private fun enviar(orden: OrdenVentilador) {
        val bytes = when (orden) {
            is OrdenVentilador.Velocidad -> VentiladorCodec.velocidad(orden.porcentaje).also { objetivoVelocidad = orden.porcentaje }
            is OrdenVentilador.Modo -> VentiladorCodec.modo(if (orden.modo == ModoVentilador.Pulso) VentiladorCodec.MODO_PULSO else VentiladorCodec.MODO_MANUAL)
        }
        ventilador.enviar(bytes)
        verificacion?.cancel()
        verificacion = ambito.launch {
            repeat(2) { intento ->
                delay(ESPERA_VENTILADOR_MS)
                if (cumplida(orden)) return@launch
                if (intento == 0) {
                    registro.suceso(Origen.Ventilador, "Sin confirmar: se repite la orden")
                    ventilador.enviar(bytes)
                }
            }
            objetivoVelocidad = null
        }
    }

    private fun cumplida(orden: OrdenVentilador): Boolean {
        val e = estado.value.ventiladorEstado ?: return false
        return when (orden) {
            is OrdenVentilador.Velocidad -> e.velocidad == orden.porcentaje
            is OrdenVentilador.Modo -> e.modo == orden.modo
        }
    }

    /** Lo que el ventilador comunica es lo que se muestra; solo la velocidad tiene un objetivo local hasta confirmarse. */
    fun leido(n: Notificacion) {
        val r = VentiladorCodec.decodificar(n.datos) ?: return
        respuestas.tryEmit(r)
        val ahora = relojMonotonico()
        when (r) {
            is NotificacionVentilador.Estado -> fijarEstado(ahora) { actualizado(it, ModoVentilador.deCodigo(r.modo), r.velocidad) }

            is NotificacionVentilador.Velocidad -> if (r.correcto) fijarEstado(ahora) { actualizado(it, it?.modo ?: ModoVentilador.Otro, r.velocidad) }

            is NotificacionVentilador.Modo -> if (r.correcto) fijarEstado(ahora) { actualizado(it, ModoVentilador.deCodigo(r.modo), it?.velocidad ?: 0) }

            is NotificacionVentilador.Configuracion -> if (r.correcto) {
                val c = ConfiguracionVentilador.desde(r.bytes)
                if (c == null) {
                    registro.suceso(Origen.Ventilador, "Configuración de ${r.bytes.size} bytes: no se usa")
                } else if (cambiosUmbral.isEmpty()) {
                    fijarEstado(ahora) { (it ?: EstadoVentilador(ModoVentilador.Otro, 0)).copy(configuracion = c) }
                }
            }

            is NotificacionVentilador.Confirmacion -> if (!r.correcto) registro.suceso(Origen.Ventilador, "Orden ${r.orden} rechazada")
        }
    }

    private fun actualizado(antes: EstadoVentilador?, modo: ModoVentilador, velocidad: Int): EstadoVentilador {
        if (objetivoVelocidad == velocidad) objetivoVelocidad = null
        return EstadoVentilador(
            modo = modo,
            velocidad = velocidad,
            velocidadManual = if (modo == ModoVentilador.Manual) velocidad else antes?.velocidadManual,
            configuracion = antes?.configuracion,
        )
    }

    private fun fijarEstado(ahora: Long, f: (EstadoVentilador?) -> EstadoVentilador) {
        estado.update { it.copy(ventiladorEstado = f(it.ventiladorEstado), ventiladorInstanteMs = ahora) }
    }

    /** Sin conexión no hay objetivo pendiente; el estado conocido se conserva para mostrarlo (spec `settings-screen`). */
    fun cambia(nuevo: EstadoDispositivo) {
        if (nuevo == EstadoDispositivo.Conectado) return
        objetivoVelocidad = null
        verificacion?.cancel()
        if (nuevo == EstadoDispositivo.SinConexion && ventilador.estado.value != EstadoEnlace.Perdida && !configurado()) {
            estado.update { it.copy(ventiladorEstado = null, ventiladorInstanteMs = null) }
        }
    }

    /**
     * Cambia el umbral de la [zona] (1 a 4) a [bpm] (design.md, decisión 4): el valor se ve al momento, y tras 400 ms sin
     * más cambios se lee la configuración, se cambia solo ese byte, se escribe y se vuelve a leer, en serie. Si falla,
     * vuelve el valor que tiene el ventilador.
     */
    fun cambiarUmbral(zona: Int, bpm: Int) {
        val e = estado.value.ventiladorEstado ?: return
        if (estado.value.ventilador != EstadoDispositivo.Conectado) return
        val base = e.configuracion ?: return
        val nueva = base.conUmbral(zona, bpm)
        cambiosUmbral[zona] = nueva.umbrales[zona - 1]
        estado.update { it.copy(ventiladorEstado = e.copy(configuracion = nueva)) }
        escrituraUmbrales?.cancel()
        escrituraUmbrales = ambito.launch {
            delay(REBOTE_UMBRALES_MS)
            mutexConfiguracion.withLock { escribirUmbrales() }
        }
    }

    private suspend fun escribirUmbrales() {
        val cambios = cambiosUmbral.toMap()
        if (cambios.isEmpty()) return
        val leida = pedirConfiguracion()
        val nueva = leida?.let { base -> cambios.entries.sortedBy { it.key }.fold(base) { c, (zona, bpm) -> c.conUmbral(zona, bpm) } }
        val correcto = nueva != null && run {
            val confirmacion = coroutineScope {
                val d = async(start = CoroutineStart.UNDISPATCHED) {
                    withTimeoutOrNull(ESPERA_VENTILADOR_MS) {
                        respuestas.first { it is NotificacionVentilador.Confirmacion && it.orden == 6 }
                    }
                }
                ventilador.enviar(VentiladorCodec.escribirConfiguracion(nueva))
                d.await()
            }
            (confirmacion as? NotificacionVentilador.Confirmacion)?.correcto == true
        }
        if (!correcto) registro.suceso(Origen.Ventilador, "No se pudo escribir la configuración: vuelve el valor del ventilador")
        cambiosUmbral.clear()
        val final = pedirConfiguracion() ?: leida
        estado.update { it.copy(ventiladorEstado = it.ventiladorEstado?.copy(configuracion = final ?: it.ventiladorEstado.configuracion)) }
    }

    /** Pide la configuración y espera su respuesta, como mucho [ESPERA_VENTILADOR_MS]; `null` si no llega o no sirve. */
    private suspend fun pedirConfiguracion(): ConfiguracionVentilador? = coroutineScope {
        val d = async(start = CoroutineStart.UNDISPATCHED) {
            withTimeoutOrNull(ESPERA_VENTILADOR_MS) { respuestas.first { it is NotificacionVentilador.Configuracion } }
        }
        ventilador.enviar(VentiladorCodec.leerConfiguracion())
        (d.await() as? NotificacionVentilador.Configuracion)?.takeIf { it.correcto }?.let { ConfiguracionVentilador.desde(it.bytes) }
    }

    /** Al terminar la sesión: sin verificaciones ni escrituras pendientes. */
    fun terminar() {
        verificacion?.cancel()
        escrituraUmbrales?.cancel()
    }
}
