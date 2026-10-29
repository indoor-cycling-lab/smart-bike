package app.smartbike.ui.conexion

import app.smartbike.ble.EstadoNombre
import app.smartbike.dominio.AjustesConexion
import app.smartbike.dominio.Compatibilidad
import app.smartbike.dominio.Configuracion
import app.smartbike.dominio.Enlace
import app.smartbike.dominio.Identidad
import app.smartbike.dominio.MandoEnModo
import app.smartbike.dominio.MandoPublicado
import app.smartbike.dominio.ModoConexion
import app.smartbike.dominio.ModoPublicado
import app.smartbike.dominio.NombreAnunciado
import app.smartbike.plataforma.EstadoMando
import app.smartbike.plataforma.EstadoPlataforma
import app.smartbike.sesion.EstadoDispositivo
import app.smartbike.sesion.EstadoRodaje

/** El estado de un servicio publicado, en palabra y forma (spec `platform-settings`, «Estado de cada servicio»). */
enum class EstadoServicio { Conectado, Enviando, Esperando, SinWifi, SinBluetooth, NoVisible, NoDisponible, SinLectura, NoSeOfrece }

/** Por dónde va un servicio: el texto bajo su nombre. */
enum class Via { Wifi, Bluetooth, DentroWifi, DentroBluetooth }

/** Lo que dice la ayuda bajo un servicio: cómo emparejarlo, qué falta o qué hacer. */
enum class AyudaServicio {
    EmparejarRodillo,
    EmparejarRodilloMovil,
    EmparejarTodoWifi,
    MandoBluetooth,
    MandoBluetoothNombre,
    MandoDentro,
    MandoSinSquare,
    PulsoElegir,
    PulsoElegirMovil,
    SinWifi,
    SinBluetooth,
    RodilloNoDisponible,
    MandoNoDisponible,
    BandaSinLectura,
    NeoReconectando,
    SquareReconectando,
    BuscarRodillo,
    BuscarMando,
    BuscarPulso,
}

data class ServicioUi(val estado: EstadoServicio, val via: Via?, val ayuda: AyudaServicio?)

/**
 * Conexión (maquetas/conexion.html; spec `platform-settings`): el estado de cada servicio que la app publica, el modo y el
 * nombre ante la app. Sin plataforma: la app no sabe qué app de entrenamiento se conecta.
 */
data class ConexionUiState(
    val conexion: AjustesConexion = AjustesConexion(),
    val rodillo: ServicioUi = ServicioUi(EstadoServicio.NoSeOfrece, null, AyudaServicio.BuscarRodillo),
    val mando: ServicioUi = ServicioUi(EstadoServicio.NoSeOfrece, null, AyudaServicio.BuscarMando),
    val pulso: ServicioUi = ServicioUi(EstadoServicio.NoSeOfrece, null, AyudaServicio.BuscarPulso),
    /** El nombre con el que la app se anuncia ahora, que es el que hay que elegir. */
    val nombreAnunciado: String = NombreAnunciado.NORMAL,
    /** El nombre Bluetooth del móvil ahora (el de la app, si está renombrado); `null` si no se sabe. */
    val nombreMovil: String? = null,
    /** El nombre que tenía el móvil antes de renombrarlo, o el de ahora si no lo está. */
    val nombreOriginalMovil: String? = null,
    /** La casilla está marcada y el móvil se llama como la app (spec `bluetooth-name`). */
    val renombrado: Boolean = false,
    /** La casilla está marcada y no se pudo renombrar: la app se anuncia con el nombre del móvil. */
    val noSePudoRenombrar: Boolean = false,
    val bluetoothEncendido: Boolean = true,
    /** Sin rodillo configurado no se publica nada (spec `platform-settings`, «Servicio no ofrecido»). */
    val sinRodillo: Boolean = true,
    /** El anuncio por wifi no responde (spec `platform-bridge`). */
    val noVisible: Boolean = false,
    /** Algo publicado necesita el Bluetooth y está apagado: aviso y «Activar Bluetooth». */
    val avisoBluetooth: Boolean = false,
) {
    val modos: List<ModoPublicado> get() = Compatibilidad.modos

    private val modoElegido: ModoPublicado get() = Compatibilidad.deModo(conexion.modo)

    /** El nombre ante la app, sin el sufijo de «KICKR BIKE PRO», tal como lo nombra la ayuda de la casilla. */
    val nombreElegido: String get() = if (conexion.identidad == Identidad.KICKR_BIKE_PRO) NombreAnunciado.KICKR else NombreAnunciado.NORMAL

    /** La casilla «Usar el nombre también por Bluetooth» solo con un modo que publica algo por Bluetooth. */
    val muestraCasilla: Boolean get() = modoElegido.enlace == Enlace.BLUETOOTH || modoElegido.mando == MandoEnModo.BLUETOOTH_APARTE

    /** El nombre ante la app se elige con wifi; por Bluetooth, solo con la casilla marcada (si no, es el del móvil). */
    val muestraNombre: Boolean get() = modoElegido.nombreElegible || conexion.nombrePorBluetooth

    /** Con «Solo Bluetooth» la casilla va antes que el selector; con wifi, después. */
    val casillaPrimero: Boolean get() = !modoElegido.nombreElegible
}

private val enIntento = setOf(EstadoDispositivo.Buscando, EstadoDispositivo.Conectando, EstadoDispositivo.Reconectando)

/** Lo que falta para publicar por el transporte del rodillo, que comparten el pulso y el mando dentro del mismo dispositivo. */
private fun fallo(p: EstadoPlataforma): EstadoServicio? = when (p) {
    EstadoPlataforma.SinWifi -> EstadoServicio.SinWifi
    EstadoPlataforma.SinBluetooth -> EstadoServicio.SinBluetooth
    EstadoPlataforma.NoVisible -> EstadoServicio.NoVisible
    EstadoPlataforma.NoDisponible -> EstadoServicio.NoDisponible
    else -> null
}

private fun ayudaDeFallo(f: EstadoServicio, noDisponible: AyudaServicio): AyudaServicio? = when (f) {
    EstadoServicio.SinWifi -> AyudaServicio.SinWifi

    EstadoServicio.SinBluetooth -> AyudaServicio.SinBluetooth

    EstadoServicio.NoDisponible -> noDisponible

    // Sin respuesta en la red, emparejar no sirve: qué hacer lo dice el aviso de arriba.
    else -> null
}

fun estadoDeConexion(c: Configuracion, e: EstadoRodaje, nombreMovil: String? = null, nombre: EstadoNombre = EstadoNombre.Propio): ConexionUiState {
    val servicios = e.servicios
    val modo = c.conexion.modo
    val porBluetooth = c.modoPublicado.enlace == Enlace.BLUETOOTH
    val via = if (porBluetooth) Via.Bluetooth else Via.Wifi
    val falla = fallo(e.plataforma)
    val renombrado = c.conexion.nombrePorBluetooth && nombre is EstadoNombre.Renombrado

    val rodillo = when {
        servicios == null -> ServicioUi(EstadoServicio.NoSeOfrece, null, AyudaServicio.BuscarRodillo)

        falla != null -> ServicioUi(falla, via, ayudaDeFallo(falla, AyudaServicio.RodilloNoDisponible))

        e.plataforma is EstadoPlataforma.Conectado && e.usoRodillo ->
            ServicioUi(EstadoServicio.Conectado, via, AyudaServicio.NeoReconectando.takeIf { e.neo in enIntento })

        else -> ServicioUi(
            EstadoServicio.Esperando,
            via,
            when {
                porBluetooth && !renombrado -> AyudaServicio.EmparejarRodilloMovil
                modo == ModoConexion.ZWIFT_RIDE -> AyudaServicio.EmparejarTodoWifi
                else -> AyudaServicio.EmparejarRodillo
            },
        )
    }

    val mandoPublicado = servicios?.mando
    val mando = if (mandoPublicado == null) {
        ServicioUi(EstadoServicio.NoSeOfrece, null, AyudaServicio.BuscarMando)
    } else {
        val dentro = mandoPublicado == MandoPublicado.DENTRO
        val viaMando = when {
            !dentro -> Via.Bluetooth
            porBluetooth -> Via.DentroBluetooth
            else -> Via.DentroWifi
        }
        // En Zwift Ride el mando va siempre, aunque no haya Square: sin él, no llega ningún botón.
        val sinSquare = e.squareConfigurado == null
        val ayudaEsperando = when {
            sinSquare -> AyudaServicio.MandoSinSquare
            dentro -> AyudaServicio.MandoDentro
            renombrado -> AyudaServicio.MandoBluetoothNombre
            else -> AyudaServicio.MandoBluetooth
        }
        when {
            dentro && falla != null -> ServicioUi(falla, viaMando, ayudaDeFallo(falla, AyudaServicio.MandoNoDisponible))

            e.mando == EstadoMando.Conectado -> ServicioUi(
                EstadoServicio.Conectado,
                viaMando,
                when {
                    sinSquare -> AyudaServicio.MandoSinSquare
                    e.square in enIntento -> AyudaServicio.SquareReconectando
                    else -> null
                },
            )

            e.mando == EstadoMando.SinConexion -> ServicioUi(EstadoServicio.SinBluetooth, viaMando, AyudaServicio.SinBluetooth)

            e.mando == EstadoMando.NoDisponible -> ServicioUi(EstadoServicio.NoDisponible, viaMando, AyudaServicio.MandoNoDisponible)

            else -> ServicioUi(EstadoServicio.Esperando, viaMando, ayudaEsperando)
        }
    }

    val pulso = when {
        servicios?.pulso != true -> ServicioUi(EstadoServicio.NoSeOfrece, null, AyudaServicio.BuscarPulso)
        falla != null -> ServicioUi(falla, via, ayudaDeFallo(falla, AyudaServicio.RodilloNoDisponible))
        e.usoPulso && e.pulso != null -> ServicioUi(EstadoServicio.Enviando, via, null)
        e.usoPulso -> ServicioUi(EstadoServicio.SinLectura, via, AyudaServicio.BandaSinLectura)
        else -> ServicioUi(EstadoServicio.Esperando, via, if (porBluetooth) AyudaServicio.PulsoElegirMovil else AyudaServicio.PulsoElegir)
    }

    val bluetoothNecesario = porBluetooth || mandoPublicado == MandoPublicado.APARTE_BLUETOOTH
    return ConexionUiState(
        conexion = c.conexion,
        rodillo = rodillo,
        mando = mando,
        pulso = pulso,
        nombreAnunciado = e.nombreAnunciado,
        nombreMovil = nombreMovil,
        nombreOriginalMovil = (nombre as? EstadoNombre.Renombrado)?.original ?: nombreMovil,
        renombrado = renombrado,
        noSePudoRenombrar = c.conexion.nombrePorBluetooth && nombre is EstadoNombre.NoSePudo && (porBluetooth || mandoPublicado == MandoPublicado.APARTE_BLUETOOTH),
        bluetoothEncendido = e.bluetoothEncendido,
        sinRodillo = servicios == null,
        noVisible = e.plataforma == EstadoPlataforma.NoVisible,
        avisoBluetooth = servicios != null && bluetoothNecesario && !e.bluetoothEncendido,
    )
}
