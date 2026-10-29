package app.smartbike.plataforma.zwift

import app.smartbike.datos.Origen
import app.smartbike.datos.RegistroSesion
import app.smartbike.dominio.EstadoMandoZwift
import app.smartbike.dominio.FuncionZwift
import app.smartbike.dominio.IdentidadDispositivo
import app.smartbike.dominio.LadoGiro
import app.smartbike.plataforma.EstadoMando
import app.smartbike.plataforma.EstadoTransporte
import app.smartbike.plataforma.TransporteDircon
import app.smartbike.protocolo.CaracteristicaGatt
import app.smartbike.protocolo.DirconCodec.uuid16
import app.smartbike.protocolo.MandoZwiftCodec
import app.smartbike.protocolo.NotificacionGatt
import app.smartbike.protocolo.PropiedadGatt.Escribir
import app.smartbike.protocolo.PropiedadGatt.Indicar
import app.smartbike.protocolo.PropiedadGatt.Leer
import app.smartbike.protocolo.PropiedadGatt.Notificar
import app.smartbike.protocolo.RespuestaHandshake
import app.smartbike.protocolo.RespuestaInfo
import app.smartbike.protocolo.ServicioGatt
import app.smartbike.protocolo.ServidorGatt
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Cómo se monta el servicio GATT del mando; solo cambia los datos del servicio, no el comportamiento del mando (design.md
 * del change `mando-zwift-dircon`, decisión 1). [BLUETOOTH]: las cinco características de un Zwift Ride, con Device
 * Information y Battery, en su propio servidor. [EN_RODILLO]: el servicio `FC82` con las tres mínimas (`0003` escritura
 * y `0002` y `0004` notificación), dentro del servidor DirCon del puente.
 */
enum class ServicioDelMando { BLUETOOTH, EN_RODILLO }

/**
 * El mando Zwift Ride emulado (design.md del change `mando-zwift-emulado`, decisión 5), a partir del prototipo del
 * change 12, con lo validado y sin variantes: `RideOn` al handshake, las páginas del protobuf a la petición de
 * información, las funciones de [EstadoMandoZwift] por ASYNC al cambiar y, cada [intervaloKeepaliveMs] sin cambios, el
 * estado actual también por ASYNC.
 *
 * Independiente del transporte y del modo: solo conoce su [servidor] y un [TransporteDircon] por el que notificar; quien
 * lo usa lo publica (el anuncio Bluetooth o el servidor DirCon del puente) y puede añadirle lo suyo con
 * [alEscrituraDesconocida] y [notificarAsync]. Todo se llama desde el hilo de control, en el que corre [ambito].
 */
class MandoZwift(
    private val ambito: CoroutineScope,
    private val registro: RegistroSesion,
    private val transporte: TransporteDircon,
    private val intervaloKeepaliveMs: Long = KEEPALIVE_MS,
    private val servicioDelMando: ServicioDelMando = ServicioDelMando.BLUETOOTH,
) {
    private var ultimoSyncTx = ByteArray(0)
    private var keepalive: Job? = null
    private var handshake = false
    private var funciones = EstadoMandoZwift()

    private val _estado = MutableStateFlow(EstadoMando.Desactivado)
    val estado: StateFlow<EstadoMando> = _estado.asStateFlow()

    /** Las funciones pulsadas ahora mismo, aunque Zwift aún no haya saludado. */
    val pulsadas: Set<FuncionZwift> get() = funciones.pulsadas

    /** Los giros que Zwift ve ahora (spec `ride-screen`, «Aviso de giro»). */
    val giro: Set<LadoGiro> get() = funciones.giro()

    /** Si Zwift ha saludado (handshake) y está atendido. */
    val saludado: Boolean get() = handshake

    /** Cuántos handshakes ha habido: distingue a un cliente del siguiente. */
    var saludos = 0
        private set

    /**
     * Lo que llega por SYNC_RX y el mando no entiende (no es el handshake ni una petición de información): quien lo usa
     * puede atenderlo y devolver lo que contesta, o `null` para ignorarlo.
     */
    var alEscrituraDesconocida: ((ByteArray) -> List<NotificacionGatt>?)? = null

    /** Notifica [trama] a Zwift por ASYNC. */
    fun notificarAsync(trama: ByteArray) {
        servidor.notificar(ASYNC, trama)?.let(transporte::notificar)
    }

    /**
     * Por Bluetooth, las cinco características de un Zwift Ride, como se validaron en el change 12. Dentro del rodillo,
     * el servicio `FC82` con las tres mínimas, como se validó por DirCon en el 13: `0003` escritura y `0002` y `0004`
     * notificación.
     */
    val servicio = when (servicioDelMando) {
        ServicioDelMando.BLUETOOTH -> ServicioGatt(
            SERVICIO,
            listOf(
                CaracteristicaGatt(ASYNC, setOf(Notificar)),
                CaracteristicaGatt(SYNC_RX, setOf(Escribir), alEscribir = ::alEscribirSyncRx),
                CaracteristicaGatt(SYNC_TX, setOf(Leer, Indicar), leer = { ultimoSyncTx.copyOf() }),
                CaracteristicaGatt(DESCONOCIDA_5, setOf(Notificar)),
                CaracteristicaGatt(DESCONOCIDA_6, setOf(Leer, Escribir, Indicar), leer = { ByteArray(0) }, alEscribir = { emptyList() }),
            ),
        )

        ServicioDelMando.EN_RODILLO -> ServicioGatt(
            uuid16(SERVICIO_ANUNCIO),
            listOf(
                CaracteristicaGatt(ASYNC, setOf(Notificar)),
                CaracteristicaGatt(SYNC_RX, setOf(Escribir), alEscribir = ::alEscribirSyncRx),
                CaracteristicaGatt(SYNC_TX, setOf(Notificar)),
            ),
        )
    }

    /**
     * Con [ServicioDelMando.BLUETOOTH], el mando con su información de dispositivo y su batería. Con
     * [ServicioDelMando.EN_RODILLO], el del puente ([usarServidor]), donde Zwift se suscribe.
     */
    var servidor = if (servicioDelMando == ServicioDelMando.BLUETOOTH) ServidorGatt(listOf(servicio, informacion(), bateria())) else ServidorGatt(listOf(servicio))
        private set

    /** Con [ServicioDelMando.EN_RODILLO], el [ServidorGatt] del puente que contiene [servicio]. */
    fun usarServidor(s: ServidorGatt) {
        servidor = s
    }

    init {
        ambito.launch {
            transporte.estado.collect { if (it !is EstadoTransporte.Conectado) clientePerdido() }
        }
    }

    /** Pasa a esperar a Zwift; una sola vez hasta [parar]. Publicarlo en el transporte lo hace quien lo usa. */
    fun publicar() {
        if (_estado.value != EstadoMando.Desactivado) return
        _estado.value = EstadoMando.Esperando
        registro.suceso(Origen.Plataforma, "Mando: publicado")
    }

    fun parar() {
        if (_estado.value == EstadoMando.Desactivado) return
        olvidar()
        _estado.value = EstadoMando.Desactivado
        registro.suceso(Origen.Plataforma, "Mando: parado")
    }

    fun pulsar(f: FuncionZwift) = cambiar(funciones.pulsar(f))

    fun soltar(f: FuncionZwift) = cambiar(funciones.soltar(f))

    fun soltarTodo() = cambiar(funciones.soltarTodo())

    private fun cambiar(nuevo: EstadoMandoZwift) {
        val antes = funciones.pulsadas
        funciones = nuevo
        if (nuevo.pulsadas == antes || !handshake) return
        enviarEstado()
        programarKeepalive()
    }

    private fun enviarEstado() {
        servidor.notificar(ASYNC, funciones.trama())?.let(transporte::notificar)
    }

    /** Zwift se ha ido: todo suelto y sin handshake; el siguiente cliente lo vuelve a hacer. */
    private fun clientePerdido() {
        if (handshake || funciones.pulsadas.isNotEmpty()) registro.suceso(Origen.Plataforma, "Mando: Zwift perdido, todo suelto")
        olvidar()
        if (_estado.value == EstadoMando.Conectado) _estado.value = EstadoMando.Esperando
    }

    private fun olvidar() {
        keepalive?.cancel()
        keepalive = null
        handshake = false
        funciones = funciones.soltarTodo()
    }

    private fun alEscribirSyncRx(valor: ByteArray): List<NotificacionGatt> {
        val id = MandoZwiftCodec.peticionInfo(valor)
        if (id != null) {
            val r = MandoZwiftCodec.respuestaInfo(RespuestaInfo.PAGINA_RIDE, id)
            ultimoSyncTx = r
            val conDatos = r.size > MandoZwiftCodec.respuestaInfo(RespuestaInfo.VACIA, id).size
            registro.suceso(Origen.Plataforma, "Mando: información $id respondida ${if (conDatos) "con datos" else "solo con el identificador"}")
            return listOf(NotificacionGatt(SYNC_TX, r))
        }
        if (!MandoZwiftCodec.esHandshake(valor)) {
            // `41 08 05` y lo demás quedan en el registro como trama recibida.
            return alEscrituraDesconocida?.invoke(valor) ?: emptyList()
        }
        val respuesta = MandoZwiftCodec.respuestaHandshake(RespuestaHandshake.PELADO)
        ultimoSyncTx = respuesta
        handshake = true
        saludos++
        if (_estado.value != EstadoMando.Desactivado) _estado.value = EstadoMando.Conectado
        registro.suceso(Origen.Plataforma, "Mando: handshake hecho")
        programarKeepalive()
        return listOf(NotificacionGatt(SYNC_TX, respuesta))
    }

    /** Cada [intervaloKeepaliveMs] sin enviar nada, el estado actual por ASYNC: Zwift corta un mando sin tráfico. */
    private fun programarKeepalive() {
        keepalive?.cancel()
        keepalive = ambito.launch {
            while (true) {
                delay(intervaloKeepaliveMs)
                enviarEstado()
            }
        }
    }

    private fun informacion() = ServicioGatt(
        INFORMACION,
        listOf(
            texto(0x2A29, "Zwift"),
            texto(0x2A24, "Zwift Ride"),
            texto(0x2A25, "0000000001"),
            texto(0x2A27, "A.0"),
            texto(0x2A26, "1.0.0"),
        ),
    )

    private fun bateria() = ServicioGatt(BATERIA, listOf(CaracteristicaGatt(NIVEL_BATERIA, setOf(Leer, Notificar), leer = { byteArrayOf(100) })))

    private fun texto(uuid: Int, valor: String) = CaracteristicaGatt(uuid16(uuid), setOf(Leer), leer = { valor.toByteArray(Charsets.UTF_8) })

    companion object {
        const val KEEPALIVE_MS = 5_000L

        /** Lo que anuncia el mando por Bluetooth: `FC82` y los datos de fabricante de Zwift, `0x08` (Ride izquierdo). */
        const val SERVICIO_ANUNCIO = 0xFC82
        const val FABRICANTE_ZWIFT = 0x094A
        val DATOS_FABRICANTE = byteArrayOf(0x08, 0x5A, 0x01)

        /** La identidad del mando por Bluetooth, que el transporte pide aunque no se use. */
        val IDENTIDAD = IdentidadDispositivo("02:53:51:4D:41:01", "5351000001")

        private fun zwift(n: Int): UUID = UUID.fromString("0000000$n-19ca-4651-86e5-fa29dcdd09d1")

        val SERVICIO: UUID = zwift(1)
        val ASYNC: UUID = zwift(2)
        val SYNC_RX: UUID = zwift(3)
        val SYNC_TX: UUID = zwift(4)
        val DESCONOCIDA_5: UUID = zwift(5)
        val DESCONOCIDA_6: UUID = zwift(6)
        val INFORMACION: UUID = uuid16(0x180A)
        val BATERIA: UUID = uuid16(0x180F)
        val NIVEL_BATERIA: UUID = uuid16(0x2A19)
    }
}
