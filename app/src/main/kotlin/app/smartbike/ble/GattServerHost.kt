package app.smartbike.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.BluetoothStatusCodes
import android.content.Context
import android.os.Build
import app.smartbike.datos.Origen
import app.smartbike.datos.RegistroSesion
import app.smartbike.datos.Sentido
import app.smartbike.dominio.IdentidadDispositivo
import app.smartbike.plataforma.EstadoTransporte
import app.smartbike.plataforma.TransporteDircon
import app.smartbike.protocolo.DirconCodec
import app.smartbike.protocolo.DirconCodec.uuid16
import app.smartbike.protocolo.NotificacionGatt
import app.smartbike.protocolo.PropiedadGatt
import app.smartbike.protocolo.ResultadoGatt
import app.smartbike.protocolo.ServicioGatt
import app.smartbike.protocolo.ServidorGatt
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Quién usa el servidor GATT, por dirección (arquitectura.md § 7.4). Android avisa de todas las conexiones del móvil,
 * también de las que la app abre como central (el Neo, el Square): solo es cliente quien lee, escribe o se suscribe.
 * Seguro entre hilos: Android llama desde los suyos.
 */
class ClientesGatt<D> {
    private val porDireccion = LinkedHashMap<String, D>()

    /** Lo que deja una desconexión. */
    sealed interface Desconexion {
        /** Era una conexión de la app como central, o de alguien que no usó el servidor. */
        data object NoEraCliente : Desconexion
        data class QuedanOtros(val ultimo: String) : Desconexion
        data object NingunoQueda : Desconexion
    }

    /** [d] usa el servidor; `true` si es un cliente nuevo. */
    fun usa(direccion: String, d: D): Boolean = synchronized(this) { porDireccion.put(direccion, d) == null }

    fun desconectado(direccion: String): Desconexion = synchronized(this) {
        when {
            porDireccion.remove(direccion) == null -> Desconexion.NoEraCliente
            porDireccion.isEmpty() -> Desconexion.NingunoQueda
            else -> Desconexion.QuedanOtros(porDireccion.keys.last())
        }
    }

    val todos: List<D> get() = synchronized(this) { porDireccion.values.toList() }

    fun vaciar() = synchronized(this) { porDireccion.clear() }
}

/**
 * Alta de los servicios de una publicación (design.md del change `endurecer-dircon-ble`, decisión 5). Android los añade
 * de uno en uno y avisa en `onServiceAdded`; cada publicación tiene su propia instancia, así que el aviso tardío de un
 * servidor ya parado llega a una instancia [parar]ada y no añade ni anuncia nada. Solo se toca desde el hilo de control.
 */
class AltaServicios<S>(servicios: List<S>, private val anadir: (S) -> Unit, private val alTerminar: () -> Unit) {
    private val pendientes = ArrayDeque(servicios)
    private var parada = false

    /** Añade el siguiente servicio o, si no queda ninguno, termina. No hace nada tras [parar]. */
    fun siguiente() {
        if (parada) return
        val s = pendientes.removeFirstOrNull()
        if (s != null) anadir(s) else alTerminar()
    }

    fun parar() {
        parada = true
        pendientes.clear()
    }
}

/** Lo que se responde a una escritura y lo que hay que notificar después; [error] es lo que se anota en el registro. */
class RespuestaEscritura(val estado: Int, val notificaciones: List<NotificacionGatt> = emptyList(), val error: String? = null)

/**
 * Decide la respuesta a una escritura del cliente (design.md del change `endurecer-dircon-ble`, decisión 5): las
 * preparadas (largas) no se admiten, un error del servidor es «no permitido» y una excepción es un fallo, anotado.
 */
fun resolverEscritura(preparada: Boolean, escribir: () -> ResultadoGatt<List<NotificacionGatt>>?): RespuestaEscritura {
    if (preparada) return RespuestaEscritura(BluetoothGatt.GATT_REQUEST_NOT_SUPPORTED)
    val r = try {
        escribir()
    } catch (e: Exception) {
        return RespuestaEscritura(BluetoothGatt.GATT_FAILURE, error = e.toString())
    }
    return when (r) {
        is ResultadoGatt.Ok -> RespuestaEscritura(BluetoothGatt.GATT_SUCCESS, r.valor)
        else -> RespuestaEscritura(BluetoothGatt.GATT_WRITE_NOT_PERMITTED)
    }
}

/**
 * Servidor GATT nativo que sirve un [ServidorGatt] por Bluetooth (design.md del change `mando-zwift-emulado`, decisión
 * 4), a partir del prototipo del change 12. Genérico: no sabe del mando; al terminar de añadir los servicios, anuncia
 * con [advertiser] lo que diga [anuncio] para el servidor publicado (sin el mando si no se publica: design.md del change
 * `pestana-conexion`, decisión 3). Todos los avisos de Android saltan a [hilo], el de control, y los de un servidor ya
 * parado se ignoran (design.md del change `endurecer-dircon-ble`, decisión 5). Las suscripciones son por dispositivo.
 * La identidad de DirCon no se usa por Bluetooth.
 */
@SuppressLint("MissingPermission") // La sesión solo publica con BLUETOOTH_CONNECT y BLUETOOTH_ADVERTISE concedidos.
class GattServerHost(
    private val context: Context,
    private val ambito: CoroutineScope,
    private val hilo: CoroutineDispatcher,
    private val registro: RegistroSesion,
    private val advertiser: Advertiser,
    private val anuncio: (ServidorGatt) -> Anuncio,
) : TransporteDircon {
    private val gestor = context.getSystemService(BluetoothManager::class.java)
    private val _estado = MutableStateFlow<EstadoTransporte>(EstadoTransporte.Parado)
    override val estado: StateFlow<EstadoTransporte> = _estado.asStateFlow()

    private var gatt: BluetoothGattServer? = null
    private var servidor: ServidorGatt? = null

    @Volatile private var cola: ColaNotificaciones<BluetoothDevice>? = null
    private val clientes = ClientesGatt<BluetoothDevice>()
    private val caracteristicas = mutableMapOf<UUID, BluetoothGattCharacteristic>()

    /** El alta de servicios de la publicación en curso; identifica a su servidor frente a los avisos tardíos de otro. */
    private var alta: AltaServicios<BluetoothGattService>? = null

    /** Características a las que se ha suscrito cada dispositivo (CCCD), por dirección. */
    private val suscripciones = ConcurrentHashMap<String, MutableSet<UUID>>()

    override fun publicar(servidor: ServidorGatt, identidad: IdentidadDispositivo, nombre: String) {
        if (gatt != null) return
        this.servidor = servidor
        caracteristicas.clear()
        // Android añade los servicios de uno en uno: el siguiente, al confirmarse el anterior (onServiceAdded).
        val nueva = AltaServicios(
            servidor.servicios.map(::servicioNativo),
            anadir = { gatt?.addService(it) },
            alTerminar = {
                advertiser.iniciar(anuncio(servidor))
                _estado.value = EstadoTransporte.Esperando
            },
        )
        val g = try {
            gestor?.openGattServer(context, callbackDe(nueva))
        } catch (e: SecurityException) {
            registro.suceso(Origen.Plataforma, "BLE: sin permiso para abrir el servidor GATT ($e)")
            null
        }
        if (g == null) {
            registro.suceso(Origen.Plataforma, "BLE: no se pudo abrir el servidor GATT")
            return
        }
        gatt = g
        alta = nueva
        cola = ColaNotificaciones(
            ambito,
            destinos = { clientes.todos },
            suscrito = { d, u -> suscripciones[d.address]?.contains(u) == true },
            enviar = ::enviar,
            alSalir = { n -> registro.trama(Origen.Plataforma, Sentido.Tx, DirconCodec.nombreCorto(n.caracteristica), n.valor) },
        )
        nueva.siguiente()
    }

    override fun parar() {
        val g = gatt ?: return
        alta?.parar()
        alta = null
        advertiser.parar()
        clientes.todos.forEach { g.cancelConnection(it) }
        g.close()
        gatt = null
        clientes.vaciar()
        suscripciones.clear()
        cola?.cerrar()
        cola = null
        servidor?.clientePerdido()
        _estado.value = EstadoTransporte.Parado
        registro.suceso(Origen.Plataforma, "BLE: parado")
    }

    override fun notificar(n: NotificacionGatt) {
        cola?.encolar(n)
    }

    private fun enviar(d: BluetoothDevice, n: NotificacionGatt): Boolean {
        val g = gatt ?: return false
        val c = caracteristicas[n.caracteristica] ?: return false
        val indicar = c.properties and BluetoothGattCharacteristic.PROPERTY_INDICATE != 0
        val ok = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            g.notifyCharacteristicChanged(d, c, indicar, n.valor) == BluetoothStatusCodes.SUCCESS
        } else {
            @Suppress("DEPRECATION")
            c.value = n.valor
            @Suppress("DEPRECATION")
            g.notifyCharacteristicChanged(d, c, indicar)
        }
        if (!ok) registro.suceso(Origen.Plataforma, "BLE: no se pudo notificar ${DirconCodec.nombreCorto(n.caracteristica)} a ${d.address}")
        return ok
    }

    private fun usa(d: BluetoothDevice) {
        if (clientes.usa(d.address, d)) {
            registro.suceso(Origen.Plataforma, "BLE: ${d.address} usa el servidor")
            _estado.value = EstadoTransporte.Conectado(d.address)
        }
    }

    private fun servicioNativo(s: ServicioGatt): BluetoothGattService {
        val nativo = BluetoothGattService(s.uuid, BluetoothGattService.SERVICE_TYPE_PRIMARY)
        for (c in s.caracteristicas) {
            var propiedades = 0
            var permisos = 0
            if (PropiedadGatt.Leer in c.propiedades) {
                propiedades = propiedades or BluetoothGattCharacteristic.PROPERTY_READ
                permisos = permisos or BluetoothGattCharacteristic.PERMISSION_READ
            }
            if (PropiedadGatt.Escribir in c.propiedades) {
                propiedades = propiedades or BluetoothGattCharacteristic.PROPERTY_WRITE or
                    BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE
                permisos = permisos or BluetoothGattCharacteristic.PERMISSION_WRITE
            }
            if (PropiedadGatt.Notificar in c.propiedades) propiedades = propiedades or BluetoothGattCharacteristic.PROPERTY_NOTIFY
            if (PropiedadGatt.Indicar in c.propiedades) propiedades = propiedades or BluetoothGattCharacteristic.PROPERTY_INDICATE
            val nativa = BluetoothGattCharacteristic(c.uuid, propiedades, permisos)
            if (propiedades and (BluetoothGattCharacteristic.PROPERTY_NOTIFY or BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0) {
                nativa.addDescriptor(
                    BluetoothGattDescriptor(CCCD, BluetoothGattDescriptor.PERMISSION_READ or BluetoothGattDescriptor.PERMISSION_WRITE),
                )
            }
            caracteristicas[c.uuid] = nativa
            nativo.addCharacteristic(nativa)
        }
        return nativo
    }

    /** Ejecuta [bloque] en el hilo de control, salvo que la publicación de [propia] ya no sea la vigente. */
    private fun enHilo(propia: AltaServicios<BluetoothGattService>, bloque: () -> Unit) {
        ambito.launch(hilo) { if (alta === propia) bloque() }
    }

    private fun callbackDe(propia: AltaServicios<BluetoothGattService>) = object : BluetoothGattServerCallback() {
        override fun onServiceAdded(status: Int, service: BluetoothGattService) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                registro.suceso(Origen.Plataforma, "BLE: no se pudo añadir ${DirconCodec.nombreCorto(service.uuid)} ($status)")
            }
            // Un aviso tardío tras `parar()` llega a un alta parada y no anuncia.
            ambito.launch(hilo) { propia.siguiente() }
        }

        override fun onConnectionStateChange(device: BluetoothDevice, status: Int, newState: Int) {
            if (newState != BluetoothProfile.STATE_DISCONNECTED) return
            enHilo(propia) {
                suscripciones.remove(device.address)
                cola?.quitar(device)
                when (val r = clientes.desconectado(device.address)) {
                    ClientesGatt.Desconexion.NoEraCliente -> Unit

                    is ClientesGatt.Desconexion.QuedanOtros -> _estado.value = EstadoTransporte.Conectado(r.ultimo)

                    // Las suscripciones del servidor son comunes: solo se olvidan cuando no queda ningún cliente.
                    ClientesGatt.Desconexion.NingunoQueda -> {
                        registro.suceso(Origen.Plataforma, "BLE: ${device.address} se ha ido ($status)")
                        servidor?.clientePerdido()
                        if (gatt != null) _estado.value = EstadoTransporte.Esperando
                    }
                }
            }
        }

        override fun onCharacteristicReadRequest(device: BluetoothDevice, requestId: Int, offset: Int, c: BluetoothGattCharacteristic) {
            enHilo(propia) {
                usa(device)
                val valor = (servidor?.leer(c.uuid) as? ResultadoGatt.Ok)?.valor
                if (valor == null) {
                    gatt?.sendResponse(device, requestId, BluetoothGatt.GATT_READ_NOT_PERMITTED, offset, null)
                } else {
                    val desde = offset.coerceAtMost(valor.size)
                    gatt?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, valor.copyOfRange(desde, valor.size))
                }
            }
        }

        override fun onCharacteristicWriteRequest(
            device: BluetoothDevice,
            requestId: Int,
            c: BluetoothGattCharacteristic,
            preparedWrite: Boolean,
            responseNeeded: Boolean,
            offset: Int,
            value: ByteArray,
        ) {
            enHilo(propia) {
                usa(device)
                registro.trama(Origen.Plataforma, Sentido.Rx, DirconCodec.nombreCorto(c.uuid), value)
                val r = resolverEscritura(preparedWrite) { servidor?.escribir(c.uuid, value) }
                r.error?.let { registro.suceso(Origen.Plataforma, "BLE: error al escribir ${DirconCodec.nombreCorto(c.uuid)}: $it") }
                if (responseNeeded) gatt?.sendResponse(device, requestId, r.estado, offset, null)
                r.notificaciones.forEach(::notificar)
            }
        }

        override fun onExecuteWrite(device: BluetoothDevice, requestId: Int, execute: Boolean) {
            enHilo(propia) { gatt?.sendResponse(device, requestId, BluetoothGatt.GATT_REQUEST_NOT_SUPPORTED, 0, null) }
        }

        override fun onDescriptorWriteRequest(
            device: BluetoothDevice,
            requestId: Int,
            d: BluetoothGattDescriptor,
            preparedWrite: Boolean,
            responseNeeded: Boolean,
            offset: Int,
            value: ByteArray,
        ) {
            enHilo(propia) {
                usa(device)
                if (preparedWrite) {
                    if (responseNeeded) gatt?.sendResponse(device, requestId, BluetoothGatt.GATT_REQUEST_NOT_SUPPORTED, offset, null)
                    return@enHilo
                }
                val activar = value.any { it.toInt() != 0 }
                val u = d.characteristic.uuid
                if (d.uuid == CCCD) {
                    val delDispositivo = suscripciones.getOrPut(device.address) { ConcurrentHashMap.newKeySet() }
                    if (activar) delDispositivo += u else delDispositivo -= u
                    servidor?.suscribir(u, activar)
                    registro.suceso(Origen.Plataforma, "BLE: ${if (activar) "suscrito a" else "sin suscripción a"} ${DirconCodec.nombreCorto(u)}")
                }
                if (responseNeeded) gatt?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, null)
            }
        }

        override fun onDescriptorReadRequest(device: BluetoothDevice, requestId: Int, offset: Int, d: BluetoothGattDescriptor) {
            enHilo(propia) {
                val suscrita = servidor?.suscrita(d.characteristic.uuid) == true
                val valor = if (suscrita) BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE else BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
                gatt?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, valor)
            }
        }

        override fun onNotificationSent(device: BluetoothDevice, status: Int) {
            cola?.confirmar(device)
        }

        override fun onMtuChanged(device: BluetoothDevice, mtu: Int) = registro.suceso(Origen.Plataforma, "BLE: MTU $mtu")
    }

    private companion object {
        val CCCD: UUID = uuid16(0x2902)
    }
}
