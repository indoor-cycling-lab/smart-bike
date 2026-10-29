package app.smartbike.plataforma

import android.os.ParcelFileDescriptor
import android.system.Os
import android.system.OsConstants
import java.net.Socket

/**
 * Ajusta un socket de DirCon para que el sistema dé por perdido a un cliente que desaparece sin cerrar (design.md del
 * change `endurecer-dircon-ble`, decisión 3): `keepAlive` y `TCP_USER_TIMEOUT` de [TIMEOUT_USUARIO_MS]. Devuelve el
 * motivo si no pudo aplicar alguno, o `null` si los aplicó todos; el llamante lo anota en el registro.
 */
fun ajustarSocketDircon(s: Socket): String? {
    val fallos = mutableListOf<String>()
    try {
        s.keepAlive = true
    } catch (e: Exception) {
        fallos += "keepAlive: ${e.message}"
    }
    try {
        // `fromSocket` duplica el descriptor; cerrar el duplicado no cierra el socket.
        ParcelFileDescriptor.fromSocket(s).use {
            Os.setsockoptInt(it.fileDescriptor, OsConstants.IPPROTO_TCP, OsConstants.TCP_USER_TIMEOUT, TIMEOUT_USUARIO_MS)
        }
    } catch (e: Exception) {
        fallos += "TCP_USER_TIMEOUT: ${e.message}"
    }
    return fallos.takeIf { it.isNotEmpty() }?.joinToString("; ")
}

const val TIMEOUT_USUARIO_MS = 15_000
