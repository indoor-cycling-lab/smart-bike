package app.smartbike.ble

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import app.smartbike.datos.Origen
import app.smartbike.datos.RegistroSesion
import app.smartbike.datos.Sentido
import java.util.UUID
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import no.nordicsemi.android.ble.ktx.suspend

/**
 * Cliente del Tacx Neo (arquitectura.md § 7.2): canal FE-C (escritura en `fec3`, respuestas en `fec2`) y telemetría
 * de Cycling Power (`2a63`) y CSC (`2a5b`), que decodifica la `Sesion`.
 *
 * Como mucho un comando pendiente: uno nuevo sustituye al que espera, y no se entrega otro a la biblioteca hasta que
 * termina la escritura anterior o vence [LIMITE_ESCRITURA_MS] (arquitectura.md § 6.2).
 */
class NeoClient(
    context: Context,
    registro: RegistroSesion,
    ambito: CoroutineScope,
    reloj: () -> Long,
) : ClienteBle(context, Origen.Neo, registro, ambito, reloj),
    ConexionNeo {

    private class Comando(val bytes: ByteArray, val hecho: CompletableDeferred<Boolean>?, val alConfirmar: ((Boolean) -> Unit)?)

    private val pendiente = Channel<Comando>(Channel.CONFLATED)

    private var fecEscritura: BluetoothGattCharacteristic? = null
    private val suscripciones = mutableListOf<Pair<BluetoothGattCharacteristic, String>>()

    init {
        ambito.launch {
            for (c in pendiente) {
                val ok = withTimeoutOrNull(LIMITE_ESCRITURA_MS) { escribir(c.bytes) } ?: false
                if (!ok) registro.suceso(Origen.Neo, "Escritura no confirmada")
                c.hecho?.complete(ok)
                c.alConfirmar?.invoke(ok)
            }
        }
    }

    override fun enviar(comando: ByteArray, alConfirmar: ((Boolean) -> Unit)?) {
        pendiente.trySend(Comando(comando, null, alConfirmar))
    }

    override suspend fun enviarYEsperar(comando: ByteArray, limiteMs: Long): Boolean {
        val hecho = CompletableDeferred<Boolean>()
        pendiente.trySend(Comando(comando, hecho, null))
        return withTimeoutOrNull(limiteMs) { hecho.await() } ?: false
    }

    private suspend fun escribir(bytes: ByteArray): Boolean {
        val c = fecEscritura ?: return false
        registro.trama(Origen.Neo, Sentido.Tx, ETIQUETA_FEC_ESCRITURA, bytes)
        return try {
            writeCharacteristic(c, bytes, TIPO_ESCRITURA).suspend()
            true
        } catch (_: Exception) {
            false
        }
    }

    override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
        suscripciones.clear()
        val fec = gatt.getService(SERVICIO_FEC) ?: return false
        fecEscritura = fec.getCharacteristic(FEC_ESCRITURA)
        fec.getCharacteristic(FEC_RESPUESTA)?.let { suscripciones += it to ETIQUETA_FEC_RESPUESTA }
        gatt.getService(SERVICIO_POTENCIA)?.getCharacteristic(POTENCIA)?.let { suscripciones += it to ETIQUETA_POTENCIA }
        gatt.getService(SERVICIO_CSC)?.getCharacteristic(CSC)?.let { suscripciones += it to ETIQUETA_CSC }
        return fecEscritura != null
    }

    override fun initialize() {
        // Paso 3 de la investigación de latencia (design.md, decisión 10): ¿admite fec3 escritura sin respuesta?
        fecEscritura?.let { registro.suceso(Origen.Neo, "Propiedades de fec3: 0x%02x".format(it.properties)) }
        // Sin repetirla: el Neo real se desconecta al volver a pedirle prioridad alta (validación del 25-10-2026).
        prioridadAltaSostenida(sostener = false)
        suscripciones.forEach { (c, etiqueta) -> escuchar(c, etiqueta) }
    }

    override fun onServicesInvalidated() {
        fecEscritura = null
        suscripciones.clear()
    }

    companion object {
        // Validado con el Neo 1 real: se anuncia como «Tacx Neo 12345» (arquitectura.md § 7.2).
        const val PREFIJO_NOMBRE = "Tacx Neo"

        // Con el intervalo de conexión de 187,5 ms que acaba teniendo el Neo, la confirmación tarda más de 300 ms
        // (validado con el Neo real); 1 s deja margen sin bloquear la ranura si una escritura se pierde.
        const val LIMITE_ESCRITURA_MS = 1_000L

        // Paso 3 de la investigación de latencia (design.md, decisión 10): sin respuesta, la escritura termina al
        // entregarla a la pila Bluetooth, sin esperar la confirmación del Neo (fec3 admite las dos: propiedades 0x0c).
        const val TIPO_ESCRITURA = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
        const val ETIQUETA_FEC_ESCRITURA = "fec3"
        const val ETIQUETA_FEC_RESPUESTA = "fec2"
        const val ETIQUETA_POTENCIA = "2a63"
        const val ETIQUETA_CSC = "2a5b"
        val SERVICIO_FEC: UUID = UUID.fromString("6e40fec1-b5a3-f393-e0a9-e50e24dcca9e")
        val FEC_RESPUESTA: UUID = UUID.fromString("6e40fec2-b5a3-f393-e0a9-e50e24dcca9e")
        val FEC_ESCRITURA: UUID = UUID.fromString("6e40fec3-b5a3-f393-e0a9-e50e24dcca9e")
        val SERVICIO_POTENCIA: UUID = UUID.fromString("00001818-0000-1000-8000-00805f9b34fb")
        val POTENCIA: UUID = UUID.fromString("00002a63-0000-1000-8000-00805f9b34fb")
        val SERVICIO_CSC: UUID = UUID.fromString("00001816-0000-1000-8000-00805f9b34fb")
        val CSC: UUID = UUID.fromString("00002a5b-0000-1000-8000-00805f9b34fb")
    }
}
