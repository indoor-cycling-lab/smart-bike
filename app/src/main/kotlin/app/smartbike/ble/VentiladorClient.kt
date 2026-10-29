package app.smartbike.ble

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import app.smartbike.datos.Origen
import app.smartbike.datos.RegistroSesion
import app.smartbike.datos.Sentido
import app.smartbike.protocolo.VentiladorCodec
import java.util.UUID
import kotlinx.coroutines.CoroutineScope

/**
 * Cliente del ventilador Wahoo KICKR Headwind (design.md del change `ventilador-headwind`, decisión 1): exige su servicio
 * y el control point, se suscribe a sus notificaciones y escribe **sin respuesta**. Al conectar pide velocidad, modo y
 * configuración para partir del estado real. Con la prioridad de conexión normal: habla a ~1 Hz y las órdenes son raras.
 * Cada orden es un encargo a la cola de Nordic de su propia conexión: no espera ni bloquea el hilo de control.
 */
class VentiladorClient(
    context: Context,
    registro: RegistroSesion,
    ambito: CoroutineScope,
    reloj: () -> Long,
) : ClienteBle(context, Origen.Ventilador, registro, ambito, reloj),
    ConexionVentilador {

    private var controlPoint: BluetoothGattCharacteristic? = null

    override fun enviar(orden: ByteArray) {
        val c = controlPoint ?: return
        registro.trama(Origen.Ventilador, Sentido.Tx, ETIQUETA_CONTROL, orden)
        writeCharacteristic(c, orden, BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE)
            .fail { _, status -> registro.suceso(Origen.Ventilador, "Escritura fallida ($status)") }
            .enqueue()
    }

    override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
        controlPoint = gatt.getService(SERVICIO)?.getCharacteristic(CONTROL_POINT)
        return controlPoint != null
    }

    override fun initialize() {
        val c = controlPoint ?: return
        escuchar(c, ETIQUETA_CONTROL)
        enviar(VentiladorCodec.leerVelocidad())
        enviar(VentiladorCodec.leerModo())
        enviar(VentiladorCodec.leerConfiguracion())
    }

    override fun onServicesInvalidated() {
        controlPoint = null
    }

    companion object {
        /** El ventilador no anuncia su servicio, solo su nombre `HEADWIND XXXX`; la búsqueda es por prefijo. */
        const val PREFIJO_NOMBRE = "HEADWIND"
        const val ETIQUETA_CONTROL = "e038"

        // Protocolo no oficial, a validar con el ventilador real (design.md, «Context»).
        val SERVICIO: UUID = UUID.fromString("a026ee0c-0a7d-4ab3-97fa-f1500f9feb8b")
        val CONTROL_POINT: UUID = UUID.fromString("a026e038-0a7d-4ab3-97fa-f1500f9feb8b")
    }
}
