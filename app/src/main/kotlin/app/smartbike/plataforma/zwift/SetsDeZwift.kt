package app.smartbike.plataforma.zwift

import app.smartbike.datos.Origen
import app.smartbike.datos.RegistroSesion
import app.smartbike.dominio.AcumuladorSimulacion
import app.smartbike.plataforma.ReceptorPlataforma
import app.smartbike.plataforma.UltimoDePlataforma
import app.smartbike.protocolo.NotificacionGatt
import app.smartbike.protocolo.SetZwift
import app.smartbike.protocolo.SimulacionZwiftCodec

/**
 * Los `SET` que Zwift manda por SYNC_RX del mando cuando trata el dispositivo como un rodillo con marchas virtuales: el
 * modo Zwift Ride (design.md del change `mando-zwift-dircon`, decisión 3) y «Solo Bluetooth» con el mando dentro
 * (design.md del change `pestana-conexion`, decisión 9). El terreno o el ERG van al [receptor]; sus marchas se ignoran.
 */
class SetsDeZwift(
    private val registro: RegistroSesion,
    private val receptor: ReceptorPlataforma,
) {
    private val acumulador = AcumuladorSimulacion()

    /** Lo que llega por SYNC_RX y el mando no entiende: si es un `SET`, lo atiende y devuelve la respuesta; si no, `null`. */
    fun escritura(valor: ByteArray): List<NotificacionGatt>? {
        if (!SimulacionZwiftCodec.esSet(valor)) return null
        val set = SimulacionZwiftCodec.decodificar(valor)
        if (set == null) {
            registro.suceso(Origen.Plataforma, "Mando: SET ilegible")
            return emptyList()
        }
        registro.suceso(Origen.Plataforma, "Mando: SET ${set.idMensaje} ${set.simulacion}")
        simulacion(set)
        return listOf(NotificacionGatt(MandoZwift.SYNC_TX, SimulacionZwiftCodec.respuesta(set)))
    }

    /** Un `SET`: su terreno o su ERG van a la `Sesion`; con los dos a la vez, manda la potencia. */
    private fun simulacion(set: SetZwift) {
        val s = set.simulacion ?: return
        val terreno = acumulador.aplicar(s)
        receptor.ultimoDePlataforma(UltimoDePlataforma.Simulacion(acumulador.ultima))
        val erg = s.potenciaErgW
        when {
            erg != null -> receptor.potenciaErg(erg)
            terreno != null -> receptor.terreno(terreno)
            else -> receptor.sinCambio()
        }
    }
}
