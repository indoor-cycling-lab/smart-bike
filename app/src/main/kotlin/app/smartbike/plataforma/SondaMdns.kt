package app.smartbike.plataforma

import app.smartbike.protocolo.ConsultaMdns
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.MulticastSocket
import java.net.NetworkInterface
import java.net.SocketTimeoutException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Resultado de preguntar por el propio anuncio: responde por [via] en [ms], o no responde. */
sealed interface ResultadoSonda {
    data class Responde(val ms: Long, val via: String) : ResultadoSonda
    data object SinRespuesta : ResultadoSonda
}

/** Comprueba que el anuncio de [instancia] de [tipo] responde en la red con el [puerto] del servidor. */
interface SondaAnuncio {
    suspend fun comprobar(instancia: String, tipo: String, puerto: Int): ResultadoSonda
}

/**
 * Pregunta por el SRV del propio anuncio con el bit QU, desde un puerto efímero, primero por multicast por la interfaz
 * del wifi y, si no contesta, por unicast a la propia IP del wifi (design.md del change `comprobar-anuncio-zwift`,
 * decisión 1). No se une al grupo multicast, así que no necesita `MulticastLock`: la respuesta llega por unicast.
 *
 * En el OnePlus 8 contesta la multicast en unos ms (validado el 27-10-2026); la unicast queda de respaldo.
 */
class SondaMdns(
    private val direccionWifi: () -> String?,
    private val intentos: Int = INTENTOS,
    private val esperaMs: Int = ESPERA_MS,
) : SondaAnuncio {
    /**
     * Solo en depuración: pregunta por un nombre que no existe, para ver el aviso de «No visible» en el móvil sin el
     * fallo real (design.md del change `comprobar-anuncio-zwift`, decisión 6.3).
     */
    @Volatile var nombreFalso = false

    override suspend fun comprobar(instancia: String, tipo: String, puerto: Int): ResultadoSonda = withContext(Dispatchers.IO) {
        val ip = direccionWifi()?.let { runCatching { InetAddress.getByName(it) }.getOrNull() }
            ?: return@withContext ResultadoSonda.SinRespuesta
        val buscada = if (nombreFalso) "$instancia $SUFIJO_FALSO" else instancia
        val consulta = ConsultaMdns.consultaSrv(buscada, tipo)
        val interfaz = runCatching { NetworkInterface.getByInetAddress(ip) }.getOrNull()
        val variantes = listOfNotNull(
            interfaz?.let { i -> Variante("multicast", GRUPO) { MulticastSocket(0).apply { networkInterface = i } } },
            Variante("unicast", InetSocketAddress(ip, PUERTO_MDNS)) { DatagramSocket(0, ip) },
        )
        val esSuya = { r: ByteArray -> ConsultaMdns.puertoSrv(r, buscada, tipo) == puerto }
        // Los intentos se reparten entre las variantes: multicast, unicast, multicast…
        for (n in 0 until intentos) {
            val v = variantes[n % variantes.size]
            val inicio = System.nanoTime()
            val responde = try {
                preguntar(v.socket(), consulta, v.destino, esSuya)
            } catch (_: IOException) {
                false
            }
            if (responde) return@withContext ResultadoSonda.Responde((System.nanoTime() - inicio) / 1_000_000, v.nombre)
        }
        ResultadoSonda.SinRespuesta
    }

    private class Variante(val nombre: String, val destino: InetSocketAddress, val socket: () -> DatagramSocket)

    /** Envía [consulta] a [destino] y espera hasta [esperaMs] un paquete que cumpla [esSuya]. */
    private fun preguntar(
        socket: DatagramSocket,
        consulta: ByteArray,
        destino: InetSocketAddress,
        esSuya: (ByteArray) -> Boolean,
    ): Boolean = socket.use { s ->
        s.send(DatagramPacket(consulta, consulta.size, destino))
        val bufer = ByteArray(BUFER)
        val limite = System.nanoTime() + esperaMs * 1_000_000L
        var queda = esperaMs
        while (queda > 0) {
            s.soTimeout = queda
            val paquete = DatagramPacket(bufer, bufer.size)
            try {
                s.receive(paquete)
            } catch (_: SocketTimeoutException) {
                return false
            }
            if (esSuya(paquete.data.copyOf(paquete.length))) return true
            queda = ((limite - System.nanoTime()) / 1_000_000).toInt()
        }
        false
    }

    companion object {
        const val INTENTOS = 3
        const val ESPERA_MS = 1000
        const val PUERTO_MDNS = 5353
        private const val BUFER = 9000
        private const val SUFIJO_FALSO = "(sonda falsa)"
        private val GRUPO = InetSocketAddress("224.0.0.251", PUERTO_MDNS)
    }
}
