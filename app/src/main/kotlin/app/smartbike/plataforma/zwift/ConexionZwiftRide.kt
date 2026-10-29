package app.smartbike.plataforma.zwift

import app.smartbike.datos.Origen
import app.smartbike.datos.RegistroSesion
import app.smartbike.dominio.FuncionMando
import app.smartbike.dominio.FuncionZwift
import app.smartbike.dominio.IdentidadDispositivo
import app.smartbike.dominio.LadoGiro
import app.smartbike.dominio.ModoConexion
import app.smartbike.plataforma.ConexionPlataforma
import app.smartbike.plataforma.EstadoMando
import app.smartbike.plataforma.EstadoTransporte
import app.smartbike.plataforma.MandoEmulado
import app.smartbike.plataforma.PuenteFtms
import app.smartbike.plataforma.ReceptorPlataforma
import app.smartbike.plataforma.TelemetriaPlataforma
import app.smartbike.plataforma.TransporteDircon
import app.smartbike.protocolo.FtmsCodec
import app.smartbike.protocolo.PedaleoZwiftCodec
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * El modo «Todo por wifi (modo Zwift Ride)» (design.md del change `mando-zwift-dircon`, decisión 1): el rodillo y el
 * mando son un solo dispositivo DirCon, con el servicio `FC82` del mando dentro del rodillo. Zwift lo trata como un
 * rodillo con marchas virtuales: el terreno y el ERG llegan por `SET`, y la potencia y la cadencia salen en su mensaje de
 * datos de pedaleo. Sus marchas se ignoran. No usa Bluetooth. Su identidad ante Zwift es la suya (decisión 7).
 */
class ConexionZwiftRide(
    ambito: CoroutineScope,
    private val registro: RegistroSesion,
    private val transporte: TransporteDircon,
    versionApp: String,
    receptor: ReceptorPlataforma,
    override val simularKickr: Boolean = false,
    /** Si se publica el pulso: con un pulsómetro configurado (design.md del change `pestana-conexion`, decisión 3). */
    pulso: Boolean = true,
) : ConexionPlataforma {
    override val modo = ModoConexion.ZWIFT_RIDE

    private val mandoZwift = MandoZwift(ambito, registro, transporte, servicioDelMando = ServicioDelMando.EN_RODILLO)
    private val puente = PuenteFtms(versionApp, listOf(mandoZwift.servicio), registro, pulso, ::comandoFtms).also { mandoZwift.usarServidor(it.servidor) }
    private val sets = SetsDeZwift(registro, receptor)

    override val estado: StateFlow<EstadoTransporte> get() = transporte.estado

    private var activado = false
    private var publicado = false

    /** El mando va dentro del rodillo: su estado depende del ajuste, de si se ha publicado y de lo que diga el mando. */
    private val mandoRide = MandoRide()
    override val mando: MandoEmulado = mandoRide

    private inner class MandoRide : MandoEmulado {
        val estadoMutable = MutableStateFlow(EstadoMando.SinConexion)
        override val estado: StateFlow<EstadoMando> = estadoMutable.asStateFlow()
        override val giro: Set<LadoGiro> get() = mandoZwift.giro

        override fun activar(activado: Boolean) {
            this@ConexionZwiftRide.activado = activado
            recalcular()
        }

        override fun pulsar(funcion: FuncionMando) {
            if (funcion is FuncionZwift) mandoZwift.pulsar(funcion)
        }

        override fun soltar(funcion: FuncionMando) {
            if (funcion is FuncionZwift) mandoZwift.soltar(funcion)
        }

        override fun soltarTodo() = mandoZwift.soltarTodo()

        override fun terminar() = Unit
    }

    init {
        mandoZwift.alEscrituraDesconocida = sets::escritura
        ambito.launch { mandoZwift.estado.collect { recalcular() } }
    }

    override val suscrito: Boolean get() = puente.suscrito

    override val suscritoPulso: Boolean get() = puente.suscritoPulso

    override fun publicar(identidad: IdentidadDispositivo) {
        publicado = true
        puente.fijarIdentidad(identidad)
        transporte.publicar(puente.servidor, identidad, nombre(identidad))
        mandoZwift.publicar()
        recalcular()
    }

    override fun retirar() {
        publicado = false
        mandoZwift.parar()
        transporte.parar()
        recalcular()
    }

    override fun terminar() = retirar()

    override fun telemetria(t: TelemetriaPlataforma) {
        puente.telemetria(t).forEach(transporte::notificar)
        // Con el servicio del mando en el rodillo, Zwift toma la potencia y la cadencia de aquí y no de FTMS (decisión 4).
        if (mandoZwift.saludado) mandoZwift.notificarAsync(PedaleoZwiftCodec.datos(t.vatios, t.cadenciaRpm))
    }

    override fun pulso(bpm: Int) {
        puente.pulso(bpm)?.let(transporte::notificar)
    }

    /** Sin el ajuste, los botones no llegan; sin anunciarse, no hay conexión; si no, lo que dice el mando. */
    private fun recalcular() {
        val nuevo = when {
            !activado -> EstadoMando.Desactivado
            !publicado -> EstadoMando.SinConexion
            else -> mandoZwift.estado.value.takeIf { it != EstadoMando.Desactivado } ?: EstadoMando.Esperando
        }
        if (mandoRide.estadoMutable.value != nuevo) mandoRide.estadoMutable.value = nuevo
    }

    /** En este modo Zwift no controla el rodillo por FTMS (no escribió nunca en la prueba): se contesta y no se hace nada. */
    private fun comandoFtms(c: FtmsCodec.Comando): Int {
        registro.suceso(Origen.Plataforma, "Zwift (FTMS, sin uso en este modo): $c")
        return FtmsCodec.EXITO
    }
}
