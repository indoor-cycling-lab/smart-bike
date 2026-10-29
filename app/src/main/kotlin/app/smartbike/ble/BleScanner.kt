package app.smartbike.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import app.smartbike.datos.Origen
import app.smartbike.datos.RegistroSesion
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf

/** Resultado de buscar un prefijo de nombre: el primer dispositivo que encaja, o que no apareció a tiempo. */
sealed interface ResultadoBusqueda {
    val prefijo: String

    data class Encontrado(override val prefijo: String, val direccion: String, val nombre: String) : ResultadoBusqueda
    data class NoEncontrado(override val prefijo: String) : ResultadoBusqueda
}

interface BuscadorDispositivos {
    /**
     * Busca a la vez un dispositivo por cada prefijo de nombre (sin distinguir mayúsculas). Emite un resultado por prefijo, cuando aparece el
     * primero que encaja o al cumplirse [limiteMs], y termina cuando todos tienen resultado.
     */
    fun buscar(prefijos: Set<String>, limiteMs: Long = LIMITE_BUSQUEDA_MS): Flow<ResultadoBusqueda>

    /**
     * Busca un pulsómetro: el primer dispositivo que anuncia el servicio de frecuencia cardiaca. Emite un resultado, con
     * la clave [PulsoClient.CLAVE], al encontrarlo o al cumplirse [limiteMs].
     */
    fun buscarPulsometro(limiteMs: Long = LIMITE_BUSQUEDA_MS): Flow<ResultadoBusqueda>

    companion object {
        const val LIMITE_BUSQUEDA_MS = 30_000L
    }
}

/**
 * Búsqueda con el `BluetoothLeScanner` nativo, sin `ScanFilter`: no admite prefijos y no sabemos si los anuncios
 * llevan el UUID de servicio, así que se filtra aquí ([resolverBusqueda]). Registra con nombre y dirección solo los
 * anuncios que encajan; de los demás, solo cuántos hubo, para no guardar en el registro exportable los dispositivos de
 * los vecinos (auditoría B4). Que sean cero dice que el escaneo no entregó nada.
 */
class BleScanner(context: Context, private val registro: RegistroSesion) : BuscadorDispositivos {
    private val adaptador = (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter

    override fun buscar(prefijos: Set<String>, limiteMs: Long): Flow<ResultadoBusqueda> = buscar(prefijos.map { CriterioAnuncio.prefijo(it) }, limiteMs)

    override fun buscarPulsometro(limiteMs: Long): Flow<ResultadoBusqueda> = buscar(listOf(CriterioAnuncio.servicio(PulsoClient.CLAVE, PulsoClient.SERVICIO)), limiteMs)

    private fun buscar(criterios: List<CriterioAnuncio>, limiteMs: Long): Flow<ResultadoBusqueda> {
        if (adaptador?.bluetoothLeScanner == null) return flowOf(*criterios.map { ResultadoBusqueda.NoEncontrado(it.clave) }.toTypedArray())
        return resolverBusqueda(anuncios(criterios), criterios, limiteMs)
    }

    // Los permisos los pide la UI antes de buscar; sin ellos startScan lanza SecurityException y la búsqueda falla.
    @SuppressLint("MissingPermission")
    private fun anuncios(criterios: List<CriterioAnuncio>): Flow<AnuncioVisto> = callbackFlow {
        val escaner = checkNotNull(adaptador?.bluetoothLeScanner)
        val vistos = mutableSetOf<String>()
        var otros = 0
        val callback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val nombre = result.scanRecord?.deviceName ?: result.device.name
                val servicios = result.scanRecord?.serviceUuids.orEmpty().map { it.uuid }.toSet()
                val anuncio = AnuncioVisto(nombre, result.device.address, servicios)
                // Una vez por dirección; con servicios, para validar los criterios con el equipo real.
                if (vistos.add(result.device.address)) {
                    if (criterios.any { it.encaja(anuncio) }) {
                        registro.suceso(Origen.App, "Anuncio: ${nombre ?: "(sin nombre)"} ${result.device.address} ${result.rssi} dBm servicios=${servicios.joinToString(",")}")
                    } else {
                        otros++
                    }
                }
                trySend(anuncio)
            }

            // Si falla, los pendientes acaban como no encontrados al cumplirse el límite.
            override fun onScanFailed(errorCode: Int) {
                registro.suceso(Origen.App, "Búsqueda fallida ($errorCode)")
                close()
            }
        }
        val ajustes = ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()
        try {
            escaner.startScan(null, ajustes, callback)
        } catch (e: SecurityException) {
            // Sin permiso, como un Bluetooth no disponible: los pendientes acaban como no encontrados al cumplirse el límite.
            registro.suceso(Origen.App, "Búsqueda: sin permiso de Bluetooth ($e)")
            close()
        }
        awaitClose {
            runCatching { escaner.stopScan(callback) }
            registro.suceso(Origen.App, "Búsqueda terminada; otros dispositivos a la vista: $otros")
        }
    }
}
