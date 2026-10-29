package app.smartbike.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.content.Context
import android.os.ParcelUuid
import app.smartbike.datos.Origen
import app.smartbike.datos.RegistroSesion
import app.smartbike.protocolo.DirconCodec.uuid16

/**
 * Lo que se anuncia: uno o varios servicios de 16 bits, datos de fabricante opcionales y, en la respuesta de escaneo, el
 * nombre Bluetooth del móvil, que es el único que Android deja anunciar sin renombrar el teléfono. El mando anuncia un
 * servicio (`FC82`); el rodillo por Bluetooth, FTMS y Cycling Power (design.md del change `ftms-bluetooth`, decisión 3).
 */
class Anuncio(
    val servicios16: List<Int>,
    val fabricante: Int? = null,
    val datosFabricante: ByteArray = ByteArray(0),
    val nombreDelMovil: Boolean = true,
) {
    constructor(
        servicio16: Int,
        fabricante: Int? = null,
        datosFabricante: ByteArray = ByteArray(0),
        nombreDelMovil: Boolean = true,
    ) : this(listOf(servicio16), fabricante, datosFabricante, nombreDelMovil)
}

/**
 * Anuncio BLE conectable (design.md del change `mando-zwift-emulado`, decisión 4). Genérico: anuncia lo que se le pase.
 * Comprueba si el móvil puede anunciarse como periférico (NF3).
 */
@SuppressLint("MissingPermission") // Solo se anuncia con BLUETOOTH_ADVERTISE concedido.
class Advertiser(context: Context, private val registro: RegistroSesion) {
    private val adaptador = context.getSystemService(BluetoothManager::class.java)?.adapter

    /**
     * El anuncio en curso. Cada [iniciar] crea su propio callback: Android guarda el callback como clave de los anuncios
     * activos y, si se apaga el Bluetooth sin poder pararlo (el anunciante es `null`), un callback reutilizado se rechaza al
     * encender con `ADVERTISE_FAILED_ALREADY_STARTED` (error 3) y la app no vuelve a anunciarse (change `ftms-bluetooth`).
     * Solo se toca desde el hilo de control.
     */
    private var alAnunciar: AdvertiseCallback? = null

    private fun callback() = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings) = registro.suceso(Origen.Plataforma, "BLE: anunciado")

        override fun onStartFailure(errorCode: Int) = registro.suceso(Origen.Plataforma, "BLE: no se pudo anunciar (error $errorCode)")
    }

    /**
     * Si el móvil puede anunciarse como periférico. Con el Bluetooth apagado Android no lo sabe y da `false`: solo vale
     * con el Bluetooth encendido.
     */
    fun disponible(): Boolean {
        val a = adaptador ?: return false
        return a.isMultipleAdvertisementSupported && a.bluetoothLeAdvertiser != null
    }

    fun iniciar(anuncio: Anuncio) {
        val anunciador = adaptador?.bluetoothLeAdvertiser ?: run {
            registro.suceso(Origen.Plataforma, "BLE: el móvil no puede anunciarse")
            return
        }
        val ajustes = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .setConnectable(true)
            .build()
        val datos = AdvertiseData.Builder()
            .apply { anuncio.servicios16.forEach { addServiceUuid(ParcelUuid(uuid16(it))) } }
            .apply { anuncio.fabricante?.let { addManufacturerData(it, anuncio.datosFabricante) } }
            .build()
        val respuesta = AdvertiseData.Builder().setIncludeDeviceName(anuncio.nombreDelMovil).build()
        // Si había un anuncio en curso, se para antes de empezar otro con su propio callback.
        parar()
        val nuevo = callback()
        alAnunciar = nuevo
        try {
            anunciador.startAdvertising(ajustes, datos, respuesta, nuevo)
        } catch (e: SecurityException) {
            alAnunciar = null
            registro.suceso(Origen.Plataforma, "BLE: sin permiso para anunciarse ($e)")
        }
    }

    fun parar() {
        val actual = alAnunciar ?: return
        alAnunciar = null
        adaptador?.bluetoothLeAdvertiser?.stopAdvertising(actual)
    }
}
