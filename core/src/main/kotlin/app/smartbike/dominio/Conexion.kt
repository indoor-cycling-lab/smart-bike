package app.smartbike.dominio

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * El protocolo de un mando emulado: cada mando ignora las funciones que no son de su protocolo ([FuncionMando]). Hoy solo
 * el de los mandos de Zwift, que otras apps de entrenamiento pueden aceptar (a validar). No es una plataforma: la app no
 * elige ante quién se presenta (design.md del change `pestana-conexion`, decisión 1).
 */
enum class ProtocoloMando { ZWIFT, }

/** El nombre con el que la app se anuncia ante la app de entrenamiento («Nombre ante la app»). */
@Serializable
enum class Identidad {
    @SerialName("smart-bike")
    SMART_BIKE,

    @SerialName("kickr-bike-pro")
    KICKR_BIKE_PRO,
}

/**
 * Cómo publica la app sus servicios: el transporte y el perfil del rodillo (design.md del change
 * `generalizar-plataformas`, decisión 1). Los nombres serializados son estables; los de la pantalla son otros («Wifi +
 * Bluetooth», «Solo wifi (modo Zwift Ride)», «Solo Bluetooth»).
 */
@Serializable
enum class ModoConexion {
    /** El rodillo y el pulso por wifi con FTMS (DirCon); el mando, aparte por Bluetooth. */
    @SerialName("ftms-wifi")
    FTMS_WIFI,

    /** El rodillo, el pulso y el mando, un solo dispositivo por wifi con el protocolo de rodillo de Zwift («modo Zwift Ride»). */
    @SerialName("zwift-ride")
    ZWIFT_RIDE,

    /** El rodillo, el pulso y el mando, un solo dispositivo por Bluetooth con FTMS: la app como periférico, sin wifi. */
    @SerialName("ftms-bluetooth")
    FTMS_BLUETOOTH,
}

/** Por dónde llega la app de entrenamiento al rodillo en un modo: la red wifi (DirCon) o el Bluetooth como periférico. */
enum class Enlace { WIFI, BLUETOOTH }

/** Dónde va el mando que la app publica en un modo, si hay un mando configurado. No hay interruptor. */
enum class MandoEnModo {
    /** El modo no tiene mando. */
    NINGUNO,

    /** Por Bluetooth, en un anuncio aparte del rodillo («Wifi + Bluetooth»). */
    BLUETOOTH_APARTE,

    /** Dentro del mismo dispositivo que el rodillo («Solo wifi», «Solo Bluetooth»). */
    DENTRO_DEL_MODO,
}

/**
 * Lo que la app recuerda de la conexión: el modo, el nombre ante la app y si ese nombre se usa también por Bluetooth
 * (spec `platform-settings`; [nombrePorBluetooth] renombra el móvil mientras publica, spec `bluetooth-name`). Opcional
 * como `bajadas`: un documento sin él se lee desmarcado.
 */
@Serializable
data class AjustesConexion(
    val modo: ModoConexion = ModoConexion.FTMS_WIFI,
    val identidad: Identidad = Identidad.SMART_BIKE,
    val nombrePorBluetooth: Boolean = false,
)

/** Lo comprobado con el equipo real de un modo con una app de entrenamiento. */
enum class Validacion { VALIDADO, EN_PARTE }

/** Una app de entrenamiento con la que se ha probado un modo, con el resultado comprobado. */
data class Prueba(val app: String, val validacion: Validacion)

/**
 * Un modo de conexión que la app ofrece: dónde va el mando, por qué [enlace] llega la app de entrenamiento al rodillo, si
 * se puede elegir el nombre ante la app (por Bluetooth es el del móvil) y con qué apps se ha probado ([probadoCon]). Con
 * [otrasFtmsAValidar], la pantalla añade que otras apps con FTMS están a validar; sin él, que es solo para Zwift. Con
 * [mandoSiempre], el servicio del mando se publica aunque no haya mando configurado, porque el rodillo lo necesita: en el
 * modo Zwift Ride, el terreno y el ERG llegan por él (design.md del change `pestana-conexion`, decisión 8, V3).
 */
data class ModoPublicado(
    val modo: ModoConexion,
    val mando: MandoEnModo,
    val recomendado: Boolean = false,
    val enlace: Enlace = Enlace.WIFI,
    val nombreElegible: Boolean = true,
    val probadoCon: List<Prueba>,
    val otrasFtmsAValidar: Boolean,
    val mandoSiempre: Boolean = false,
)

/**
 * La tabla de compatibilidad por modo (spec `platform-settings`, «Compatibilidad en cada modo» y «Tabla de
 * compatibilidad»). Solo contiene lo implementado y solo nombra apps probadas con el equipo real.
 */
object Compatibilidad {
    val modos: List<ModoPublicado> = listOf(
        ModoPublicado(
            ModoConexion.FTMS_WIFI,
            MandoEnModo.BLUETOOTH_APARTE,
            recomendado = true,
            probadoCon = listOf(Prueba("Zwift", Validacion.VALIDADO)),
            otrasFtmsAValidar = true,
        ),
        ModoPublicado(
            ModoConexion.FTMS_BLUETOOTH,
            MandoEnModo.DENTRO_DEL_MODO,
            enlace = Enlace.BLUETOOTH,
            nombreElegible = false,
            probadoCon = listOf(Prueba("Zwift", Validacion.EN_PARTE)),
            otrasFtmsAValidar = true,
        ),
        ModoPublicado(
            ModoConexion.ZWIFT_RIDE,
            MandoEnModo.DENTRO_DEL_MODO,
            probadoCon = listOf(Prueba("Zwift", Validacion.EN_PARTE)),
            otrasFtmsAValidar = false,
            mandoSiempre = true,
        ),
    )

    /** El modo recomendado. */
    val recomendado: ModoConexion = modos.first { it.recomendado }.modo

    fun deModo(modo: ModoConexion): ModoPublicado = modos.first { it.modo == modo }
}

/** Qué tipos de dispositivo hay configurados en Dispositivos: deciden qué se publica (no si están conectados). */
data class Configurados(val rodillo: Boolean = false, val mando: Boolean = false, val pulsometro: Boolean = false)

/** Cómo se publica el mando: en un anuncio Bluetooth aparte o dentro del mismo dispositivo que el rodillo. */
enum class MandoPublicado { APARTE_BLUETOOTH, DENTRO }

/** Lo que la app publica para la app de entrenamiento: el rodillo en [modo] y, si se publican, el mando y el pulso. */
data class ServiciosPublicados(val modo: ModoConexion, val mando: MandoPublicado?, val pulso: Boolean)

/**
 * «Configurado = se publica» (spec `platform-bridge`, «Lo publicado según los dispositivos configurados»; design.md del
 * change `pestana-conexion`, decisión 3): sin rodillo configurado no se publica nada; el mando, con un mando configurado (o
 * siempre, si el modo lo necesita: [ModoPublicado.mandoSiempre]); el pulso, con un pulsómetro configurado. Que estén
 * conectados no cuenta.
 */
object Publicacion {
    fun de(modo: ModoConexion, r: Configurados): ServiciosPublicados? {
        if (!r.rodillo) return null
        val publicado = Compatibilidad.deModo(modo)
        val mando = when (publicado.mando) {
            MandoEnModo.NINGUNO -> null
            MandoEnModo.BLUETOOTH_APARTE -> MandoPublicado.APARTE_BLUETOOTH
            MandoEnModo.DENTRO_DEL_MODO -> MandoPublicado.DENTRO
        }?.takeIf { r.mando || publicado.mandoSiempre }
        return ServiciosPublicados(modo, mando, pulso = r.pulsometro)
    }
}
