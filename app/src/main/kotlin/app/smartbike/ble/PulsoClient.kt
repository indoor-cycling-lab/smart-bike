package app.smartbike.ble

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import app.smartbike.datos.Origen
import app.smartbike.datos.RegistroSesion
import java.util.UUID
import kotlinx.coroutines.CoroutineScope

/**
 * Cliente de un pulsómetro Bluetooth estándar (design.md del change `pulso-zwift`, decisión 1): exige el servicio de
 * frecuencia cardiaca (`0x180D`) y se suscribe a la medida (`0x2A37`); cada notificación va al registro y al flujo, y
 * la `Sesion` la decodifica. Con la prioridad de conexión normal: el pulso va a ~1 Hz y no la necesita. No lee la
 * ubicación del sensor ni usa el punto de control.
 */
class PulsoClient(
    context: Context,
    registro: RegistroSesion,
    ambito: CoroutineScope,
    reloj: () -> Long,
) : ClienteBle(context, Origen.Pulso, registro, ambito, reloj),
    ConexionPulso {

    private var medida: BluetoothGattCharacteristic? = null

    override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
        medida = gatt.getService(SERVICIO)?.getCharacteristic(MEDIDA)
        return medida != null
    }

    override fun initialize() {
        medida?.let { escuchar(it, ETIQUETA_MEDIDA) }
    }

    override fun onServicesInvalidated() {
        medida = null
    }

    companion object {
        /** La clave con la que la búsqueda devuelve el resultado del pulsómetro. */
        const val CLAVE = "Pulsómetro"
        const val ETIQUETA_MEDIDA = "2A37"
        val SERVICIO: UUID = UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb")
        val MEDIDA: UUID = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb")
    }
}
