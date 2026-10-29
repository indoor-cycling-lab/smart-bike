package app.smartbike.ble

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import app.smartbike.datos.Origen
import app.smartbike.datos.RegistroSesion
import app.smartbike.dominio.FiltroBateria
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import no.nordicsemi.android.ble.data.Data

/**
 * Cliente del Elite Square (arquitectura.md § 7.1). Se suscribe a los contadores de botones (0x45), de los que se
 * leen los botones, y a la máscara (0x43), que solo va al registro para comparar. Sin emparejamiento ni cifrado.
 *
 * El Square renegocia a los pocos segundos un intervalo de conexión de 187,5 ms, demasiado para NF1: la prioridad alta
 * se sostiene mientras dura la conexión ([prioridadAltaSostenida]). Para ver si eso agota el Square, su batería
 * (`0x2A19`) se lee al conectar y cada [PERIODO_BATERIA_MS]: todo va al registro y las lecturas que cuentan (no la primera)
 * salen por [nivelBateria] (design.md de los changes `conexion-automatica-en-segundo-plano`, decisión 8, y
 * `exportar-bateria-y-giro`, decisión 5).
 */
class SquareClient(
    context: Context,
    registro: RegistroSesion,
    ambito: CoroutineScope,
    reloj: () -> Long,
) : ClienteBle(context, Origen.Square, registro, ambito, reloj),
    ConexionSquare {

    private var mascara: BluetoothGattCharacteristic? = null
    private var contadores: BluetoothGattCharacteristic? = null
    private var bateria: BluetoothGattCharacteristic? = null
    private var lecturaBateria: Job? = null
    private val filtroBateria = FiltroBateria()
    private val _nivelBateria = MutableSharedFlow<Int>(extraBufferCapacity = 4)
    override val nivelBateria: Flow<Int> = _nivelBateria

    override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
        val servicio = gatt.getService(SERVICIO) ?: return false
        mascara = servicio.getCharacteristic(MASCARA)
        contadores = servicio.getCharacteristic(CONTADORES)
        bateria = gatt.getService(SERVICIO_BATERIA)?.getCharacteristic(NIVEL_BATERIA)
        return contadores != null
    }

    override fun initialize() {
        prioridadAltaSostenida()
        mascara?.let { escuchar(it, ETIQUETA_MASCARA) }
        contadores?.let { escuchar(it, ETIQUETA_CONTADORES) }
        filtroBateria.reiniciar()
        val c = bateria
        if (c == null) {
            registro.suceso(Origen.Square, "Sin servicio de batería")
            return
        }
        lecturaBateria?.cancel()
        lecturaBateria = ambito.launch {
            while (true) {
                leerBateria(c)
                delay(PERIODO_BATERIA_MS)
            }
        }
    }

    private fun leerBateria(c: BluetoothGattCharacteristic) {
        readCharacteristic(c)
            .with { _, data ->
                val lectura = data.getIntValue(Data.FORMAT_UINT8, 0)
                registro.suceso(Origen.Square, "Batería: ${lectura ?: "?"} %")
                filtroBateria.lectura(lectura)?.let { _nivelBateria.tryEmit(it) }
            }
            .fail { _, status -> registro.suceso(Origen.Square, "No se pudo leer la batería ($status)") }
            .enqueue()
    }

    override fun onServicesInvalidated() {
        lecturaBateria?.cancel()
        lecturaBateria = null
        mascara = null
        contadores = null
        bateria = null
    }

    companion object {
        const val PREFIJO_NOMBRE = "SQUARE"
        const val ETIQUETA_MASCARA = "0x43"
        const val ETIQUETA_CONTADORES = "0x45"
        val SERVICIO: UUID = UUID.fromString("347b0001-7635-408b-8918-8ff3949ce592")
        val MASCARA: UUID = UUID.fromString("347b0043-7635-408b-8918-8ff3949ce592")
        val CONTADORES: UUID = UUID.fromString("347b0045-7635-408b-8918-8ff3949ce592")
        val SERVICIO_BATERIA: UUID = UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb")
        val NIVEL_BATERIA: UUID = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb")
        const val PERIODO_BATERIA_MS = 10 * 60_000L
    }
}
