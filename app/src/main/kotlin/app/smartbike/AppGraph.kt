package app.smartbike

import android.content.Context
import android.os.StatFs
import android.os.SystemClock
import android.util.Log
import app.smartbike.ble.AdaptadorBluetooth
import app.smartbike.ble.AdaptadorNombre
import app.smartbike.ble.Advertiser
import app.smartbike.ble.Anuncio
import app.smartbike.ble.BleScanner
import app.smartbike.ble.GattServerHost
import app.smartbike.ble.NeoClient
import app.smartbike.ble.PulsoClient
import app.smartbike.ble.RenombradorBluetooth
import app.smartbike.ble.SquareClient
import app.smartbike.ble.VentiladorClient
import app.smartbike.datos.Almacen
import app.smartbike.datos.CopiaArchivo
import app.smartbike.datos.CopiaConfiguracion
import app.smartbike.datos.Espacio
import app.smartbike.datos.GrabadorSesion
import app.smartbike.datos.Origen
import app.smartbike.datos.RegistroSesion
import app.smartbike.datos.RepositorioSesiones
import app.smartbike.datos.atarGrabador
import app.smartbike.datos.fijarIdiomaInicial
import app.smartbike.dominio.IdiomaApp
import app.smartbike.dominio.MandoPublicado
import app.smartbike.dominio.ModoConexion
import app.smartbike.dominio.OrientacionApp
import app.smartbike.dominio.ServiciosPublicados
import app.smartbike.dominio.TemaApp
import app.smartbike.plataforma.AnuncioNsd
import app.smartbike.plataforma.ConexionFtms
import app.smartbike.plataforma.ConexionPlataforma
import app.smartbike.plataforma.DirconServer
import app.smartbike.plataforma.ReceptorPlataforma
import app.smartbike.plataforma.SondaMdns
import app.smartbike.plataforma.TransporteBluetooth
import app.smartbike.plataforma.zwift.ConexionZwiftRide
import app.smartbike.plataforma.zwift.MandoZwift
import app.smartbike.plataforma.zwift.MandoZwiftBluetooth
import app.smartbike.plataforma.zwift.MandoZwiftEnServidor
import app.smartbike.plataforma.zwift.ServicioDelMando
import app.smartbike.protocolo.FtmsCodec
import app.smartbike.red.AdaptadorWifi
import app.smartbike.sesion.Sesion
import app.smartbike.sesion.VibradorAndroid
import java.io.File
import java.time.ZoneId
import java.util.Locale
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Inyección manual (arquitectura.md § 3): crea una sola vez los objetos que comparten las pantallas y el servicio, y la
 * `Sesion` de cada rodaje (design.md del change `marchas-sin-zwift`, decisión 1).
 */
class AppGraph(context: Context) {
    private val appContext = context.applicationContext

    /** Hilo de control (arquitectura.md § 6.1): todo lo que cambia el estado de la sesión pasa por aquí, en orden. */
    val hiloControl = Dispatchers.Default.limitedParallelism(1)

    private val relojMonotonico = { SystemClock.elapsedRealtime() }
    private val relojPared = { System.currentTimeMillis() }

    /** En depuración, cada entrada va también al registro del sistema para capturar trazas (`adb logcat -s SqTrama`). */
    val registro = RegistroSesion(
        relojMonotonico = relojMonotonico,
        relojPared = relojPared,
        espejo = if (BuildConfig.DEBUG) { linea -> Log.i(ETIQUETA_TRAZAS, linea) } else null,
    )

    /**
     * Ámbito de la aplicación, para el trabajo que debe terminar aunque se cierre la pantalla. Una excepción en una de
     * sus corrutinas (y en las de la sesión, que lo heredan) se anota y no cierra la app: en pleno rodaje, el Neo se
     * quedaría con la última resistencia.
     */
    private val ambitoApp = CoroutineScope(
        SupervisorJob() + hiloControl + CoroutineExceptionHandler { _, e ->
            Log.e(ETIQUETA_TRAZAS, "Error no capturado", e)
            registro.suceso(Origen.App, "Error no capturado: $e")
        },
    )

    private val buscador by lazy { BleScanner(appContext, registro) }
    private val square by lazy { SquareClient(appContext, registro, ambitoApp, relojMonotonico) }
    private val neo by lazy { NeoClient(appContext, registro, ambitoApp, relojMonotonico) }
    private val pulsometro by lazy { PulsoClient(appContext, registro, ambitoApp, relojMonotonico) }
    private val ventilador by lazy { VentiladorClient(appContext, registro, ambitoApp, relojMonotonico) }
    private val bluetooth by lazy { AdaptadorBluetooth(appContext) }

    /** El nombre Bluetooth del móvil mientras se publica por Bluetooth (change `nombre-bluetooth-propio`). */
    private val renombrador by lazy { RenombradorBluetooth(AdaptadorNombre(appContext), almacen, registro) }
    private val wifi by lazy { AdaptadorWifi(appContext) }
    private val dircon by lazy {
        DirconServer(ambitoApp, hiloControl, registro, AnuncioNsd(appContext, registro), { wifi.direccion.value }, sonda = sonda)
    }
    private val sonda by lazy { SondaMdns({ wifi.direccion.value }) }

    /** Solo en depuración: la sonda del anuncio pregunta por un nombre falso (extra [MainActivity.EXTRA_SONDA_NOMBRE_FALSO]). */
    fun sondaConNombreFalso(activo: Boolean) {
        if (!BuildConfig.DEBUG || sonda.nombreFalso == activo) return
        sonda.nombreFalso = activo
        registro.suceso(Origen.Plataforma, if (activo) "Depuración: la sonda pregunta por un nombre falso" else "Depuración: la sonda vuelve al nombre real")
    }

    /**
     * El mando de Zwift por Bluetooth: un servidor GATT y un anuncio para toda la vida de la app, como el `DirconServer`
     * (design.md del change `mando-zwift-emulado`, decisión 6).
     */
    private val advertiser by lazy { Advertiser(appContext, registro) }
    private val servidorBle by lazy {
        val anuncio = Anuncio(MandoZwift.SERVICIO_ANUNCIO, MandoZwift.FABRICANTE_ZWIFT, MandoZwift.DATOS_FABRICANTE)
        GattServerHost(appContext, ambitoApp, hiloControl, registro, advertiser) { anuncio }
    }
    private val mando by lazy { MandoZwift(ambitoApp, registro, servidorBle) }

    /**
     * El servidor GATT del modo «Rodillo por Bluetooth (FTMS)», con su anuncio de FTMS, Cycling Power y el mando (design.md del change
     * `ftms-bluetooth`, decisión 3). Nunca está abierto a la vez que el del mando: los modos son excluyentes y cada
     * conexión cierra el suyo al terminar.
     */
    private val servidorFtmsBle by lazy {
        // Con el mando dentro, `FC82` y los datos de fabricante de Zwift, para que lo vea también como mando (decisión 5); sin
        // Square configurado, solo el rodillo (design.md del change `pestana-conexion`, decisión 3).
        val conMando = Anuncio(
            listOf(FtmsCodec.SERVICIO_FTMS, FtmsCodec.SERVICIO_POTENCIA, MandoZwift.SERVICIO_ANUNCIO),
            MandoZwift.FABRICANTE_ZWIFT,
            MandoZwift.DATOS_FABRICANTE,
        )
        val sinMando = Anuncio(listOf(FtmsCodec.SERVICIO_FTMS, FtmsCodec.SERVICIO_POTENCIA))
        GattServerHost(appContext, ambitoApp, hiloControl, registro, advertiser) { s ->
            if (s.servicios.any { it.uuid == MandoZwift.SERVICIO }) conMando else sinMando
        }
    }

    /**
     * La conexión de lo que se publica ([ServiciosPublicados]: el modo y si van el mando y el pulso; design.md del change
     * `pestana-conexion`, decisión 3). El servidor DirCon y lo del Bluetooth son únicos y los comparten; en cada momento
     * solo hay una conexión activa.
     */
    private fun crearConexion(
        servicios: ServiciosPublicados,
        simularKickr: Boolean,
        receptor: ReceptorPlataforma,
        ambito: CoroutineScope,
    ): ConexionPlataforma {
        val version = BuildConfig.VERSION_NAME
        return when (servicios.modo) {
            ModoConexion.FTMS_WIFI -> {
                val mandoBluetooth = if (servicios.mando == MandoPublicado.APARTE_BLUETOOTH) {
                    MandoZwiftBluetooth(ambito, mando, servidorBle, bluetooth, advertiser::disponible)
                } else {
                    null
                }
                ConexionFtms(servicios.modo, registro, dircon, version, receptor, simularKickr, mandoBluetooth, servicios.pulso)
            }

            ModoConexion.FTMS_BLUETOOTH -> {
                val transporte = TransporteBluetooth(ambito, servidorFtmsBle, bluetooth, advertiser::disponible)
                val mandoDentro = if (servicios.mando == MandoPublicado.DENTRO) {
                    MandoZwiftEnServidor(ambito, MandoZwift(ambito, registro, transporte, servicioDelMando = ServicioDelMando.BLUETOOTH), transporte.estado)
                } else {
                    null
                }
                ConexionFtms(servicios.modo, registro, transporte, version, receptor, simularKickr, mando = mandoDentro, pulso = servicios.pulso)
            }

            ModoConexion.ZWIFT_RIDE -> ConexionZwiftRide(ambito, registro, dircon, version, receptor, simularKickr, servicios.pulso)
        }
    }

    /**
     * `estado.json` y `configuracion.json`; las pantallas de Botones también leen y cambian la configuración. Lo que se
     * pierda al leerlos queda en el registro.
     */
    val almacen by lazy {
        Almacen(appContext, CoroutineScope(SupervisorJob() + Dispatchers.IO)) { registro.suceso(Origen.App, "Almacén: $it") }
    }

    /** Exportar e importar la configuración con el selector de archivos del sistema (F12.6). */
    val copia: CopiaConfiguracion by lazy { CopiaArchivo(appContext.contentResolver) }
    private val vibrador by lazy { VibradorAndroid(appContext) }

    /**
     * El tema elegido en Ajustes, `null` hasta la primera lectura de `configuracion.json`. Vive con la aplicación, así
     * que al girar el móvil la actividad nueva ya lo conoce (design.md del change `acceso-ajustes-y-tema`, decisión 6).
     */
    val tema: StateFlow<TemaApp?> by lazy {
        almacen.configuracion.map { it.tema }.stateIn(ambitoApp, SharingStarted.Eagerly, null)
    }

    /**
     * El idioma de la interfaz, `null` hasta la primera lectura de `configuracion.json` y hasta que el primer arranque
     * guarda el del móvil, como [tema] (change `idioma-es-en`, decisión 2): `MainActivity` no pinta nada hasta conocerlo.
     */
    val idioma: StateFlow<IdiomaApp?> by lazy {
        ambitoApp.launch { fijarIdiomaInicial(almacen, Locale.getDefault().language) }
        almacen.configuracion.map { it.idioma }.stateIn(ambitoApp, SharingStarted.Eagerly, null)
    }

    /** La orientación elegida en Ajustes, `null` hasta la primera lectura, como [tema] (change `orientacion-pantalla`, decisión 3). */
    val orientacion: StateFlow<OrientacionApp?> by lazy {
        almacen.configuracion.map { it.orientacion }.stateIn(ambitoApp, SharingStarted.Eagerly, null)
    }

    private val _sesion = MutableStateFlow<Sesion?>(null)
    private var terminando: Sesion? = null

    /** La sesión en curso, o `null` antes de abrir la app y después de terminarla. */
    val sesion: StateFlow<Sesion?> = _sesion.asStateFlow()

    /**
     * Vuelve a mirar el permiso de Bluetooth: al concederlo, la sesión se conecta a los configurados (design.md del
     * change `conexion-automatica-en-segundo-plano`, decisión 5). Devuelve si está concedido.
     */
    fun comprobarPermisos(): Boolean = bluetooth.comprobarPermiso()

    /** El nombre Bluetooth del móvil, con el que aparece el mando en Zwift. */
    fun nombreBluetooth(): String? = bluetooth.nombre()

    /** Al abrir la app: la sesión en curso o una nueva (spec `ride-session`, «Sesión al abrir la app»). */
    fun iniciarSesion(): Sesion = _sesion.value ?: Sesion(
        buscador, square, neo, pulsometro, ventilador, bluetooth, wifi, ::crearConexion, renombrador, registro, almacen, almacen, vibrador, ambitoApp,
        relojMonotonico, relojPared,
    ).also { _sesion.value = it }

    /** Atrás desde Rodaje o «Terminar» en la notificación. Al acabar, [sesion] pasa a `null`. */
    fun terminarSesion() {
        val s = _sesion.value ?: return
        if (s === terminando) return
        terminando = s
        ambitoApp.launch {
            s.terminar()
            if (_sesion.value === s) _sesion.value = null
        }
    }

    /** La carpeta de las sesiones grabadas; el `FileProvider` solo expone esta (res/xml/rutas_sesiones.xml). */
    private val carpetaSesiones = File(appContext.filesDir, "sesiones")

    /**
     * Graba los sucesos del registro en un archivo mientras «Grabar sesiones» esté activado y haya una sesión (F13.4;
     * design.md del change `registro-sesiones`). Escribe en un ámbito de IO propio: el hilo de control no espera al disco.
     */
    val grabador by lazy {
        GrabadorSesion(
            carpetaSesiones,
            registro,
            relojPared,
            relojMonotonico,
            ZoneId.systemDefault(),
            BuildConfig.VERSION_NAME,
            espacio = { StatFs(appContext.filesDir.path).let { Espacio(it.availableBytes, it.totalBytes) } },
            ambito = CoroutineScope(SupervisorJob() + Dispatchers.IO),
        )
    }

    /** Las sesiones grabadas: listar, abrir y borrar. */
    val sesionesGrabadas by lazy { RepositorioSesiones(carpetaSesiones) { grabador.estado.value } }

    init {
        // Si la app se cerró con el móvil renombrado, se le devuelve el nombre antes de que la sesión publique nada.
        ambitoApp.launch { renombrador.aplicar(null) }
        // Al final del bloque de propiedades: el grabador necesita el registro, el almacén y la sesión ya creados.
        CoroutineScope(SupervisorJob() + Dispatchers.IO).atarGrabador(grabador, almacen.configuracion, sesion.map { it != null })
    }

    companion object {
        const val ETIQUETA_TRAZAS = "SqTrama"
    }
}
