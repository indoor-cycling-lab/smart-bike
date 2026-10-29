package app.smartbike.red

import android.content.Context
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiManager
import android.os.Build
import java.net.Inet4Address
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface EstadoWifi {
    /** Dirección IPv4 del móvil en la red wifi, o `null` sin wifi. */
    val direccion: StateFlow<String?>

    /** Mantiene el wifi en baja latencia mientras rueda con Zwift (design.md del change `puente-zwift-wifi`, decisión 10). */
    fun mantenerActivo(activo: Boolean)
}

/**
 * Sigue la red wifi con un `NetworkCallback` y guarda un `WifiLock` de baja latencia. Cuenta cualquier wifi, también
 * una red local sin internet: el puente con Zwift no la necesita (NF5).
 */
class AdaptadorWifi(context: Context) : EstadoWifi {
    private val conectividad = context.getSystemService(ConnectivityManager::class.java)
    private val wifi = context.applicationContext.getSystemService(WifiManager::class.java)
    private val redes = mutableMapOf<Network, String?>()
    private val _direccion = MutableStateFlow<String?>(null)
    override val direccion: StateFlow<String?> = _direccion.asStateFlow()

    @Suppress("DEPRECATION") // WIFI_MODE_FULL_HIGH_PERF es la única opción antes de Android 10.
    private val bloqueo = wifi.createWifiLock(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) WifiManager.WIFI_MODE_FULL_LOW_LATENCY else WifiManager.WIFI_MODE_FULL_HIGH_PERF,
        "SmartBike:zwift",
    ).apply { setReferenceCounted(false) }

    init {
        val peticion = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        conectividad.registerNetworkCallback(
            peticion,
            object : ConnectivityManager.NetworkCallback() {
                override fun onLinkPropertiesChanged(red: Network, propiedades: LinkProperties) {
                    actualizar { redes[red] = ipv4(propiedades) }
                }

                override fun onLost(red: Network) {
                    actualizar { redes.remove(red) }
                }
            },
        )
    }

    override fun mantenerActivo(activo: Boolean) {
        synchronized(bloqueo) {
            if (activo && !bloqueo.isHeld) {
                bloqueo.acquire()
            } else if (!activo && bloqueo.isHeld) {
                bloqueo.release()
            }
        }
    }

    private fun actualizar(cambio: () -> Unit) = synchronized(redes) {
        cambio()
        _direccion.value = redes.values.firstOrNull { it != null }
    }

    private fun ipv4(p: LinkProperties): String? = p.linkAddresses.map { it.address }.firstOrNull { it is Inet4Address && !it.isLoopbackAddress }?.hostAddress
}
