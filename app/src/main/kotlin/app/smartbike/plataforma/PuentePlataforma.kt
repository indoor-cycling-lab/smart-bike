package app.smartbike.plataforma

import app.smartbike.datos.Origen
import app.smartbike.datos.RegistroSesion
import app.smartbike.dominio.ClaveIdentidad
import app.smartbike.dominio.Enlace
import app.smartbike.dominio.FuncionMando
import app.smartbike.dominio.Identidad
import app.smartbike.dominio.IdentidadDispositivo
import app.smartbike.dominio.LadoGiro
import app.smartbike.dominio.ModoConexion
import app.smartbike.dominio.ServiciosPublicados
import app.smartbike.red.EstadoWifi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * La plataforma tal como la ve el usuario (specs `platform-bridge` y `ftms-bluetooth`): sin wifi, la app anunciada esperando, anunciada pero
 * sin responder en la red (`NoVisible`, change `comprobar-anuncio-zwift`), o la plataforma conectada.
 */
sealed interface EstadoPlataforma {
    data object SinWifi : EstadoPlataforma
    data object Esperando : EstadoPlataforma
    data object NoVisible : EstadoPlataforma

    /** Solo con el modo por Bluetooth: Bluetooth apagado o sin permiso (change `ftms-bluetooth`). */
    data object SinBluetooth : EstadoPlataforma

    /** Solo con el modo por Bluetooth: el móvil no puede anunciarse como periférico. */
    data object NoDisponible : EstadoPlataforma

    /** No hay rodillo configurado: no se publica nada (spec `platform-bridge`, «Lo publicado según los dispositivos configurados»). */
    data object NoSeOfrece : EstadoPlataforma
    data class Conectado(val direccion: String) : EstadoPlataforma
}

/**
 * Lo que el [PuentePlataforma] necesita de quien lo usa (la `Sesion`) y le cuenta: lo que llega de la plataforma, ya en
 * términos del dominio ([ReceptorPlataforma]), cómo está el enlace, y dónde se guardan las identidades. Todo se llama desde
 * el hilo de control.
 */
interface DestinoPlataforma : ReceptorPlataforma {
    /** La plataforma se ha ido tras estar conectada (design.md del change `generalizar-plataformas`, decisión 5). */
    fun perdida()

    /** Lo que ve el usuario de la plataforma ha cambiado. */
    fun estadoPlataforma(estado: EstadoPlataforma)

    /** Va a cambiar la conexión: hay que soltar todo lo pulsado en el mando de la anterior. */
    fun soltarTodo()

    /** Se ha puesto una conexión nueva: su modo y el nombre con el que se anuncia. */
    fun conexionNueva(modo: ModoConexion, nombre: String)

    /**
     * Si la app de entrenamiento usa el rodillo (suscrita a su telemetría) y el pulso (suscrita a él), para el estado de
     * cada servicio (design.md del change `pestana-conexion`, decisión 6). Se avisa a menudo: quien lo recibe descarta lo
     * repetido.
     */
    fun usoServicios(rodillo: Boolean, pulso: Boolean)

    /** El mando tal como lo ve el usuario; [EstadoMando.Desactivado] si el modo no tiene mando. */
    fun estadoMando(estado: EstadoMando)

    /** La identidad guardada con [clave] (`ClaveIdentidad`), si hay. */
    fun identidad(clave: String): IdentidadDispositivo?

    /** Guarda la identidad nueva de [clave]. */
    fun guardarIdentidad(clave: String, identidad: IdentidadDispositivo)
}

/**
 * Lo que la app hace ante la plataforma elegida (spec `platform-bridge`), sacado de la `Sesion` (design.md del change
 * `generalizar-plataformas`, decisión 5): usar la conexión del modo y la identidad, reaccionar al wifi y al estado del
 * transporte, mandar la telemetría a 4 Hz, el pulso y las funciones del mando. Lo que toca al Neo y al control
 * (terreno, ERG, plataforma perdida) se lo devuelve a la `Sesion` por el [DestinoPlataforma]. Corre en el hilo de control
 * de [ambito].
 */
class PuentePlataforma(
    private val ambito: CoroutineScope,
    private val registro: RegistroSesion,
    private val wifi: EstadoWifi,
    /** Crea la conexión de lo que se publica, con el nombre ante la app (`simularKickr`), en su propio ámbito. */
    private val crearConexion: (ServiciosPublicados, Boolean, ReceptorPlataforma, CoroutineScope) -> ConexionPlataforma,
    private val destino: DestinoPlataforma,
    /** La potencia, la cadencia y la biela de ahora, o `null` si no hay que enviar nada. */
    private val telemetria: () -> TelemetriaPlataforma?,
) {
    private var conexion: ConexionPlataforma? = null
    private var servicios: ServiciosPublicados? = null
    private var ambitoConexion: CoroutineScope? = null
    private var identidad: IdentidadDispositivo? = null
    private var telemetriaJob: Job? = null
    private var estado: EstadoPlataforma = EstadoPlataforma.SinWifi
    private var terminada = false

    /** Ya se sabe si hay wifi: desde entonces, cada conexión nueva se publica al crearla, sin esperar a otro cambio de wifi. */
    private var wifiConocido = false

    /** Los giros que la plataforma ve ahora. */
    val giro: Set<LadoGiro> get() = conexion?.mando?.giro ?: emptySet()

    fun pulsar(f: FuncionMando) {
        conexion?.mando?.pulsar(f)
    }

    fun soltar(f: FuncionMando) {
        conexion?.mando?.soltar(f)
    }

    fun soltarTodo() {
        conexion?.mando?.soltarTodo()
    }

    /** El pulso de la banda hacia la plataforma. */
    fun pulso(bpm: Int) {
        conexion?.pulso(bpm)
    }

    /**
     * Publicar [servicios] (el modo y si van el mando y el pulso, según lo configurado) con el nombre [nombre], con la sesión
     * abierta (design.md de los changes `mando-zwift-dircon`, decisión 6, y `pestana-conexion`, decisiones 3 y 4): si cambia
     * algo, se suelta todo, se para la conexión anterior (el anuncio incluido), se crea la nueva con su identidad, se activa
     * su mando (no hay interruptor) y se publica si hay wifi. Con [servicios] `null` (sin rodillo configurado) no se publica
     * nada. No se toca la marcha ni lo que simula el Neo: lo último de la plataforma se mantiene los 30 s de siempre.
     * Devuelve si ha rehecho la conexión (y, por tanto, ya se anuncia con el nombre Bluetooth de ahora).
     */
    fun usar(servicios: ServiciosPublicados?, nombre: Identidad): Boolean {
        val anterior = conexion
        val simularKickr = nombre == Identidad.KICKR_BIKE_PRO
        if (terminada || (anterior != null && this.servicios == servicios && anterior.simularKickr == simularKickr)) return false
        if (anterior == null && servicios == null && estado == EstadoPlataforma.NoSeOfrece) return false
        this.servicios = servicios
        destino.soltarTodo()
        ambitoConexion?.cancel()
        anterior?.terminar()
        conexion = null
        ambitoConexion = null
        identidad = null
        if (servicios == null) {
            registro.suceso(Origen.Plataforma, "Sin rodillo configurado: no se publica nada")
            wifi.mantenerActivo(false)
            destino.estadoMando(EstadoMando.Desactivado)
            transporteCambia(EstadoTransporte.Parado)
            return true
        }
        registro.suceso(Origen.Plataforma, "Publicación: $servicios" + if (simularKickr) " (simulando un KICKR BIKE)" else "")
        val ambitoNuevo = CoroutineScope(ambito.coroutineContext + SupervisorJob(ambito.coroutineContext[Job]))
        val nueva = crearConexion(servicios, simularKickr, destino, ambitoNuevo)
        conexion = nueva
        ambitoConexion = ambitoNuevo
        val id = identidadDe(servicios, nombre)
        identidad = id
        destino.conexionNueva(servicios.modo, nueva.nombre(id))
        ambitoNuevo.launch { nueva.estado.collect { transporteCambia(it) } }
        val mando = nueva.mando
        if (mando == null) {
            destino.estadoMando(EstadoMando.Desactivado)
        } else {
            mando.activar(true)
            ambitoNuevo.launch { mando.estado.collect { destino.estadoMando(it) } }
        }
        if (anterior != null || wifiConocido) wifiCambia(wifi.direccion.value)
        return true
    }

    /** La identidad de [servicios] con [nombre]: la guardada o una nueva, que se guarda (la misma que usa [usar]). */
    fun identidadDe(servicios: ServiciosPublicados, nombre: Identidad): IdentidadDispositivo {
        val clave = ClaveIdentidad.de(servicios.modo, nombre)
        return destino.identidad(clave) ?: IdentidadDispositivo.nuevo().also { destino.guardarIdentidad(clave, it) }
    }

    /**
     * El móvil ha cambiado de nombre Bluetooth con la conexión ya anunciada: vuelve a anunciarse para que la app de
     * entrenamiento lo vea con el nombre nuevo (design.md del change `nombre-bluetooth-propio`, decisión 3). Solo si el
     * anuncio Bluetooth es todo el de la conexión (por wifi no se toca, para no cortar a la app de entrenamiento).
     */
    fun reanunciarBluetooth() {
        val conexion = conexion ?: return
        val id = identidad ?: return
        if (terminada || conexion.enlace != Enlace.BLUETOOTH) return
        registro.suceso(Origen.Plataforma, "Bluetooth: se vuelve a anunciar con el nombre nuevo")
        conexion.retirar()
        conexion.publicar(id)
    }

    /** Con wifi, la app puede anunciarse; el wifi se mantiene en baja latencia mientras dura (decisión 10). */
    fun wifiCambia(direccion: String?) {
        if (terminada) return
        wifiConocido = true
        registro.suceso(Origen.Plataforma, if (direccion != null) "Wifi: $direccion" else "Sin wifi")
        val id = identidad
        val conexion = conexion
        // Solo los modos por wifi lo necesitan: el de Bluetooth se anuncia con o sin wifi (design.md de `ftms-bluetooth`, decisión 4).
        val porWifi = conexion?.enlace != Enlace.BLUETOOTH
        wifi.mantenerActivo(direccion != null && conexion != null && porWifi)
        if ((direccion != null || !porWifi) && id != null && conexion != null) conexion.publicar(id) else conexion?.retirar()
        transporteCambia(conexion?.estado?.value ?: EstadoTransporte.Parado)
    }

    /** Se acaba la sesión: se para la telemetría y la conexión (el anuncio incluido) y el wifi vuelve a su estado. */
    fun terminar() {
        terminada = true
        telemetriaJob?.cancel()
        conexion?.terminar()
        wifi.mantenerActivo(false)
    }

    /** Lo que ve el usuario de la plataforma y la telemetría a 4 Hz mientras está conectada (decisión 5). */
    private fun transporteCambia(t: EstadoTransporte) {
        val porWifi = conexion?.enlace != Enlace.BLUETOOTH
        val nuevo = when {
            servicios == null -> EstadoPlataforma.NoSeOfrece
            t == EstadoTransporte.SinBluetooth -> EstadoPlataforma.SinBluetooth
            t == EstadoTransporte.NoDisponible -> EstadoPlataforma.NoDisponible
            porWifi && wifi.direccion.value == null -> EstadoPlataforma.SinWifi
            t is EstadoTransporte.Conectado -> EstadoPlataforma.Conectado(t.direccion)
            t == EstadoTransporte.NoVisible -> EstadoPlataforma.NoVisible
            else -> EstadoPlataforma.Esperando
        }
        val antes = estado
        if (nuevo == antes) return
        estado = nuevo
        destino.estadoPlataforma(nuevo)
        if (nuevo is EstadoPlataforma.Conectado) {
            if (telemetriaJob?.isActive != true) telemetriaJob = ambito.launch { telemetriaPeriodica() }
        } else {
            telemetriaJob?.cancel()
            telemetriaJob = null
            destino.usoServicios(rodillo = false, pulso = false)
            if (antes is EstadoPlataforma.Conectado) destino.perdida()
        }
    }

    private suspend fun telemetriaPeriodica() {
        while (true) {
            delay(PERIODO_TELEMETRIA_MS)
            val conexion = conexion ?: continue
            destino.usoServicios(conexion.suscrito, conexion.suscritoPulso)
            if (!conexion.suscrito) continue
            telemetria()?.let(conexion::telemetria)
        }
    }

    companion object {
        const val PERIODO_TELEMETRIA_MS = 250L
    }
}
