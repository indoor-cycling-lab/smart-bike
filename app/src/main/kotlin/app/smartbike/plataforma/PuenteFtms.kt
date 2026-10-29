package app.smartbike.plataforma

import app.smartbike.datos.Origen
import app.smartbike.datos.RegistroSesion
import app.smartbike.dominio.IdentidadDispositivo
import app.smartbike.protocolo.CaracteristicaGatt
import app.smartbike.protocolo.DirconCodec.uuid16
import app.smartbike.protocolo.FtmsCodec
import app.smartbike.protocolo.NotificacionGatt
import app.smartbike.protocolo.PropiedadGatt.Escribir
import app.smartbike.protocolo.PropiedadGatt.Indicar
import app.smartbike.protocolo.PropiedadGatt.Leer
import app.smartbike.protocolo.PropiedadGatt.Notificar
import app.smartbike.protocolo.PulsoCodec
import app.smartbike.protocolo.ServicioGatt
import app.smartbike.protocolo.ServidorGatt
import app.smartbike.protocolo.TelemetriaCodec

/** Lo que se envía a la plataforma en cada notificación de telemetría; todo a 0 sin Neo. */
data class TelemetriaPlataforma(val vatios: Int, val cadenciaRpm: Int, val biela: TelemetriaCodec.Biela?, val velocidadKmh: Double)

/**
 * Servicios FTMS, Cycling Power, Heart Rate y Device Information del [ServidorGatt] que se ofrece a la plataforma (arquitectura.md § 4.3;
 * design.md del change `puente-zwift-wifi`, decisión 4). No sabe por qué transporte va. Cada comando del punto de
 * control llega a [alComando], que devuelve el resultado de la respuesta (`FtmsCodec.EXITO`…). [serviciosExtra] se
 * añaden al servidor tal cual: el mando dentro del rodillo (design.md del change `mando-zwift-dircon`, decisión 4). El
 * servicio de pulso solo se ofrece con [pulso], es decir, con un pulsómetro configurado (design.md del change
 * `pestana-conexion`, decisión 3).
 * Device Information ofrece también serie, firmware y hardware, con la serie de la identidad que se fija al publicar
 * ([fijarIdentidad]); si hay [registro], cada lectura queda en él (design.md del change `completar-protocolo-dircon-zwift`,
 * decisión 2).
 */
class PuenteFtms(
    private val versionApp: String,
    serviciosExtra: List<ServicioGatt> = emptyList(),
    private val registro: RegistroSesion? = null,
    private val pulso: Boolean = true,
    private val alComando: (FtmsCodec.Comando) -> Int,
) {
    private val puntoControl = uuid16(FtmsCodec.PUNTO_CONTROL)
    private val datosBici = uuid16(FtmsCodec.INDOOR_BIKE_DATA)
    private val potencia = uuid16(FtmsCodec.CP_MEDIDA)
    private val pulsoMedida = uuid16(SERVICIO_PULSO_MEDIDA)

    @Volatile
    private var serie = ""

    /** La identidad con la que se publica el rodillo: su serie es la que la plataforma lee, también en las lecturas posteriores. */
    fun fijarIdentidad(identidad: IdentidadDispositivo) {
        serie = identidad.serie
    }

    val servidor = ServidorGatt(
        listOf(
            ServicioGatt(
                uuid16(FtmsCodec.SERVICIO_FTMS),
                listOf(
                    CaracteristicaGatt(uuid16(FtmsCodec.FEATURE), setOf(Leer), leer = FtmsCodec::feature),
                    CaracteristicaGatt(uuid16(FtmsCodec.RANGO_POTENCIA), setOf(Leer), leer = FtmsCodec::rangoPotencia),
                    CaracteristicaGatt(puntoControl, setOf(Escribir, Indicar), alEscribir = ::puntoDeControl),
                    CaracteristicaGatt(datosBici, setOf(Notificar)),
                    CaracteristicaGatt(uuid16(FtmsCodec.STATUS), setOf(Notificar)),
                    CaracteristicaGatt(uuid16(FtmsCodec.TRAINING_STATUS), setOf(Leer), leer = FtmsCodec::trainingStatus),
                ),
            ),
            ServicioGatt(
                uuid16(FtmsCodec.SERVICIO_POTENCIA),
                listOf(
                    CaracteristicaGatt(potencia, setOf(Notificar)),
                    CaracteristicaGatt(uuid16(FtmsCodec.CP_FEATURE), setOf(Leer), leer = FtmsCodec::cpFeature),
                    CaracteristicaGatt(uuid16(FtmsCodec.CP_UBICACION), setOf(Leer), leer = FtmsCodec::cpUbicacion),
                ),
            ),
        ) + listOfNotNull(
            ServicioGatt(
                uuid16(SERVICIO_PULSO),
                listOf(
                    CaracteristicaGatt(pulsoMedida, setOf(Notificar)),
                    CaracteristicaGatt(uuid16(PULSO_UBICACION), setOf(Leer), leer = { byteArrayOf(UBICACION_OTRA) }),
                ),
            ).takeIf { pulso },
        ) + listOf(
            ServicioGatt(
                uuid16(FtmsCodec.SERVICIO_INFORMACION),
                listOf(
                    texto(FtmsCodec.FABRICANTE, NOMBRE),
                    texto(FtmsCodec.MODELO, NOMBRE),
                    texto(FtmsCodec.VERSION_SOFTWARE, versionApp),
                    textoLeido(FtmsCodec.SERIE, "serie") { serie },
                    textoLeido(FtmsCodec.VERSION_FIRMWARE, "firmware") { versionApp },
                    textoLeido(FtmsCodec.VERSION_HARDWARE, "hardware") { HARDWARE },
                ),
            ),
        ) + serviciosExtra,
    )

    /**
     * Si la plataforma recibe telemetría: está suscrito a los datos de la bici o a la potencia. No incluye el pulso: gobierna el
     * bucle de potencia y cadencia, que no cambia con la banda.
     */
    val suscrito: Boolean get() = servidor.suscrita(datosBici) || servidor.suscrita(potencia)

    /** Las notificaciones de telemetría para las características a las que está suscrita la plataforma. */
    fun telemetria(t: TelemetriaPlataforma): List<NotificacionGatt> = listOfNotNull(
        servidor.notificar(datosBici, FtmsCodec.indoorBikeData(t.velocidadKmh, t.cadenciaRpm, t.vatios)),
        servidor.notificar(potencia, FtmsCodec.cyclingPower(t.vatios, t.biela)),
    )

    /** Si la plataforma está suscrita al pulso (spec `platform-settings`, «Estado del pulso»). */
    val suscritoPulso: Boolean get() = pulso && servidor.suscrita(pulsoMedida)

    /** La medida de [bpm] para la plataforma, o `null` si no está suscrito al pulso. */
    fun pulso(bpm: Int): NotificacionGatt? = servidor.notificar(pulsoMedida, PulsoCodec.medida(bpm))

    private fun puntoDeControl(valor: ByteArray): List<NotificacionGatt> {
        val c = FtmsCodec.comando(valor)
        val resultado = when (c) {
            is FtmsCodec.Comando.Invalido -> FtmsCodec.PARAMETRO_INVALIDO
            is FtmsCodec.Comando.NoAdmitido -> FtmsCodec.NO_ADMITIDO
            else -> alComando(c)
        }
        val op = if (c.opcode >= 0) c.opcode else 0
        return listOf(NotificacionGatt(puntoControl, FtmsCodec.respuesta(op, resultado)))
    }

    private fun texto(uuid: Int, valor: String) = CaracteristicaGatt(uuid16(uuid), setOf(Leer), leer = { valor.toByteArray(Charsets.UTF_8) })

    /** Una característica de texto cuya lectura se anota en el registro con su valor. */
    private fun textoLeido(uuid: Int, nombre: String, valor: () -> String) = CaracteristicaGatt(
        uuid16(uuid),
        setOf(Leer),
        leer = {
            val v = valor()
            registro?.suceso(Origen.Plataforma, "Información del dispositivo: $nombre «$v» leída")
            v.toByteArray(Charsets.UTF_8)
        },
    )

    companion object {
        const val NOMBRE = "Smart Bike"
        const val HARDWARE = "1.0"
        const val SERVICIO_PULSO = 0x180D
        const val SERVICIO_PULSO_MEDIDA = 0x2A37
        const val PULSO_UBICACION = 0x2A38

        /** Ubicación del sensor «otra» (0): la banda va en el brazo. */
        private const val UBICACION_OTRA: Byte = 0
    }
}
