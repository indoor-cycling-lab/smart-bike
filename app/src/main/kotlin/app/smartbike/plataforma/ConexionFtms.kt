package app.smartbike.plataforma

import app.smartbike.datos.Origen
import app.smartbike.datos.RegistroSesion
import app.smartbike.dominio.IdentidadDispositivo
import app.smartbike.dominio.ModoConexion
import app.smartbike.dominio.Terreno
import app.smartbike.plataforma.zwift.MandoZwiftEnServidor
import app.smartbike.plataforma.zwift.SetsDeZwift
import app.smartbike.protocolo.FtmsCodec
import app.smartbike.protocolo.PedaleoZwiftCodec
import kotlinx.coroutines.flow.StateFlow

/**
 * Los modos de rodillo con FTMS estándar, «Rodillo por wifi (FTMS)» y «Rodillo por Bluetooth (FTMS)» (design.md de los
 * changes `generalizar-plataformas`, decisión 4, y `ftms-bluetooth`, decisión 2), válidos para cualquier plataforma: el
 * rodillo va a la plataforma por el [transporte] (DirCon o el servidor GATT) con FTMS estándar (el comando `0x11` da el
 * terreno y el `0x05`, el ERG) y, si el modo tiene [mando], es un periférico aparte que esta clase no conoce, salvo el
 * [MandoZwiftEnServidor], que va dentro del servidor del rodillo. No sabe por dónde va el transporte.
 * Con el mando dentro, Zwift puede tratar el dispositivo como un Zwift Ride y mandar el terreno por `SET` en lugar de
 * por FTMS: se atienden los dos caminos y, desde el primer `SET`, el pedaleo sale también por el mando (design.md del
 * change `pestana-conexion`, decisión 9).
 */
class ConexionFtms(
    override val modo: ModoConexion,
    private val registro: RegistroSesion,
    private val transporte: TransporteDircon,
    versionApp: String,
    private val receptor: ReceptorPlataforma,
    override val simularKickr: Boolean = false,
    override val mando: MandoEmulado? = null,
    /** Si se publica el pulso: con un pulsómetro configurado (design.md del change `pestana-conexion`, decisión 3). */
    pulso: Boolean = true,
) : ConexionPlataforma {
    // Con el mando dentro del servidor (modo por Bluetooth), su servicio va con los del rodillo (design.md de `ftms-bluetooth`, decisión 5).
    private val mandoDentro = mando as? MandoZwiftEnServidor
    private val puente = PuenteFtms(versionApp, listOfNotNull(mandoDentro?.servicio), registro, pulso, ::comando).also { p -> mandoDentro?.usarServidor(p.servidor) }
    private val sets = SetsDeZwift(registro, receptor)

    /** El saludo del mando en el que llegó el último `SET`: ese cliente usa el protocolo de Zwift y espera el pedaleo por el mando. */
    private var saludoConSet: Int? = null

    init {
        mandoDentro?.let { m -> m.alEscrituraDesconocida { valor -> sets.escritura(valor)?.also { saludoConSet = m.saludos } } }
    }

    override val estado: StateFlow<EstadoTransporte> get() = transporte.estado

    override val suscrito: Boolean get() = puente.suscrito

    override val suscritoPulso: Boolean get() = puente.suscritoPulso

    override fun publicar(identidad: IdentidadDispositivo) {
        puente.fijarIdentidad(identidad)
        transporte.publicar(puente.servidor, identidad, nombre(identidad))
        mandoDentro?.publicado(true)
    }

    override fun retirar() {
        mandoDentro?.publicado(false)
        transporte.parar()
    }

    override fun terminar() {
        mando?.terminar()
        transporte.parar()
    }

    override fun telemetria(t: TelemetriaPlataforma) {
        puente.telemetria(t).forEach(transporte::notificar)
        val m = mandoDentro ?: return
        if (m.saludado && saludoConSet == m.saludos) m.notificarAsync(PedaleoZwiftCodec.datos(t.vatios, t.cadenciaRpm))
    }

    override fun pulso(bpm: Int) {
        puente.pulso(bpm)?.let(transporte::notificar)
    }

    /** Un comando del punto de control de la plataforma, en el hilo de control. */
    private fun comando(c: FtmsCodec.Comando): Int {
        registro.suceso(Origen.Plataforma, "Plataforma: $c")
        receptor.ultimoDePlataforma(UltimoDePlataforma.Comando(c))
        when (c) {
            is FtmsCodec.Comando.Simulacion -> receptor.terreno(Terreno(c.pendientePct, c.crr, c.cw, c.vientoMs))
            is FtmsCodec.Comando.PotenciaObjetivo -> receptor.potenciaErg(c.vatios)
            else -> receptor.sinCambio()
        }
        return FtmsCodec.EXITO
    }
}
