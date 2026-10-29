package app.smartbike.plataforma

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import app.smartbike.datos.Origen
import app.smartbike.datos.RegistroSesion
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Anuncio mDNS con `NsdManager`, nativo desde Android 4.1 (arquitectura.md § 3). El servicio mDNS del sistema responde
 * por la app, así que no hace falta `MulticastLock` (design.md del change `puente-zwift-wifi`, decisión 10; a validar
 * con la pantalla apagada). Que lo dé por registrado no garantiza que responda en la red: eso lo comprueba
 * [SondaMdns] (design.md del change `comprobar-anuncio-zwift`).
 */
class AnuncioNsd(context: Context, private val registro: RegistroSesion) : Anunciador {
    private val nsd = context.getSystemService(NsdManager::class.java)
    private var oyente: NsdManager.RegistrationListener? = null

    override suspend fun anunciar(nombre: String, tipo: String, puerto: Int, registros: Map<String, String>): Boolean {
        val hecho = CompletableDeferred<Boolean>()
        synchronized(this) {
            retirar()
            val info = NsdServiceInfo().apply {
                serviceName = nombre
                serviceType = tipo
                port = puerto
                registros.forEach { (k, v) -> setAttribute(k, v) }
            }
            val o = object : NsdManager.RegistrationListener {
                override fun onServiceRegistered(i: NsdServiceInfo) {
                    registro.suceso(Origen.Plataforma, "mDNS: anunciado como «${i.serviceName}»")
                    hecho.complete(true)
                }

                override fun onRegistrationFailed(i: NsdServiceInfo, error: Int) {
                    registro.suceso(Origen.Plataforma, "mDNS: no se pudo anunciar (error $error)")
                    hecho.complete(false)
                }

                override fun onServiceUnregistered(i: NsdServiceInfo) = registro.suceso(Origen.Plataforma, "mDNS: anuncio retirado")

                override fun onUnregistrationFailed(i: NsdServiceInfo, error: Int) = registro.suceso(Origen.Plataforma, "mDNS: no se pudo retirar (error $error)")
            }
            oyente = o
            nsd.registerService(info, NsdManager.PROTOCOL_DNS_SD, o)
        }
        return withTimeoutOrNull(ESPERA_REGISTRO_MS) { hecho.await() } ?: run {
            registro.suceso(Origen.Plataforma, "mDNS: el sistema no confirma el anuncio")
            false
        }
    }

    @Synchronized
    override fun retirar() {
        val o = oyente ?: return
        oyente = null
        try {
            nsd.unregisterService(o)
        } catch (e: IllegalArgumentException) {
            registro.suceso(Origen.Plataforma, "mDNS: el anuncio ya no estaba (${e.message})")
        }
    }

    private companion object {
        const val ESPERA_REGISTRO_MS = 5_000L
    }
}
