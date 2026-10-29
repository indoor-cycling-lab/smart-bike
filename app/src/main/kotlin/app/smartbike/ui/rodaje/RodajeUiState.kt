package app.smartbike.ui.rodaje

import app.smartbike.dominio.Accion
import app.smartbike.dominio.AvisoVentilador
import app.smartbike.dominio.BateriaSquare
import app.smartbike.dominio.Configuracion
import app.smartbike.dominio.DatoRodaje
import app.smartbike.dominio.EstadoMarcha
import app.smartbike.dominio.GuiaPantalla
import app.smartbike.dominio.IdiomaApp
import app.smartbike.dominio.LadoGiro
import app.smartbike.dominio.MandoEnModo
import app.smartbike.dominio.MandoPublicado
import app.smartbike.dominio.ModoCambio
import app.smartbike.dominio.ModoConexion
import app.smartbike.dominio.ModoControl
import app.smartbike.dominio.PasosGuia
import app.smartbike.dominio.TipoModelo
import app.smartbike.dominio.TopeMarcha
import app.smartbike.dominio.Transmision
import app.smartbike.plataforma.EstadoMando
import app.smartbike.plataforma.EstadoPlataforma
import app.smartbike.sesion.EstadoDispositivo
import app.smartbike.sesion.EstadoRodaje
import app.smartbike.sesion.Simulacion
import app.smartbike.sesion.pasosGuia
import app.smartbike.ui.componentes.Enlace
import app.smartbike.ui.componentes.EstadoChip
import app.smartbike.ui.componentes.TonoDato
import app.smartbike.ui.tema.locale
import java.util.Locale
import kotlin.math.abs

/**
 * Un enlace de la barra de conexiones de Rodaje, con su icono ([enlace]) y la forma de su [estado]; [enPalabra] es el
 * estado que lee el lector de pantalla («sin conexión»), o null si está conectado. El rodillo y el mando de Zwift son
 * enlaces distintos, cada uno con el suyo.
 */
data class EnlaceUi(val enlace: Enlace, val estado: EstadoChip, val enPalabra: EnPalabra?) {
    enum class EnPalabra { Conectando, Reconectando, Esperando, SinConexion, NoVisible, NoDisponible, BateriaBaja }
}

/**
 * El estado de la plataforma a la que se presenta la app, en palabras (spec `ride-screen`, «Estado de la plataforma en
 * Rodaje»): [nombre] de la plataforma elegida, la forma del [estado] y el [texto] que se dice. [rodillo] y [mando] son lo que
 * lee el lector de pantalla («Plataforma: Zwift, conectado; mando, esperando»); [mando] es `null` si el mando no está en juego.
 */
data class PlataformaUi(
    val estado: EstadoChip,
    val texto: Texto,
    val rodillo: EnlaceUi.EnPalabra?,
    val mando: EnlaceUi.EnPalabra? = null,
    val pulso: EnlaceUi.EnPalabra? = null,
) {
    enum class Texto { Conectado, ConectadoSinMando, ConectadoSinPulso, ConectadoSinMandoNiPulso, Esperando, NoVisible, SinWifi, SinBluetooth, NoDisponible, SinRodillo }
}

/**
 * Lo que dice el pie sin aviso de acción (spec `ride-screen`): cómo despertar lo que la app intenta conectar, o, si
 * falta alguno sin intento, que se toque la barra de conexiones. Si hay intento, prima la ayuda, porque se resuelve solo.
 */
enum class PieUi { Nada, TocarParaConectar, DespertarNeo, DespertarSquare, DespertarLosDos }

/** Aviso del pie durante 1,5 s (F11.6): la acción, con lo que cambió de verdad para los saltos recortados. */
sealed interface AvisoUi {
    data class Marcha(val accion: Accion, val diferencia: Int) : AvisoUi

    /** En ERG, «+10 W», «−30 W»… */
    data class Vatios(val diferenciaW: Int) : AvisoUi

    /** En ERG de Zwift, □ quita el ajuste. */
    data object PotenciaZwift : AvisoUi

    /** En ERG manual, □ vuelve a la potencia de entrada: «Vuelta a 200 W». */
    data class VueltaA(val potenciaW: Int) : AvisoUi

    /** En ERG, «Tope · 50 W». */
    data class TopePotencia(val potenciaW: Int) : AvisoUi

    /** Al alternar: «ERG manual · 200 W», o con `null` «Modo Libre» ([libre]) o «Modo SIM». */
    data class ModoErg(val potenciaW: Int?, val libre: Boolean = true) : AvisoUi

    /** Alternar en un entrenamiento de Zwift: «Entrenamiento de Zwift». */
    data object EntrenamientoZwift : AvisoUi

    /** «Referencia en la 15» o «Referencia en 36×17» al fijarla. */
    data class Referencia(val marcha: EstadoMarcha) : AvisoUi

    /** Con platos, una acción que cambió el plato, también si lo cambió solo el modo automático: «Plato 52». */
    data class Plato(val dientes: Int) : AvisoUi

    /** Con «Siguiente modelo»: «Modelo: Físico». */
    data class Modelo(val modelo: TipoModelo) : AvisoUi

    /** Resultado de una acción del ventilador: «Ventilador: 50 %», «Sin ventilador»… (spec `ride-screen`). */
    data class Ventilador(val aviso: AvisoVentilador) : AvisoUi
}

/**
 * ERG: la potencia que se envía al Neo y, en ERG de Zwift ([manual] falso), la que pide Zwift y el ajuste de las
 * palancas. En ERG manual no hay línea debajo (spec `ride-screen`, «Potencia objetivo en ERG manual»).
 */
data class ErgUi(val potenciaW: Int, val deZwiftW: Int, val ajusteW: Int, val manual: Boolean = false)

/** Con platos, la marcha como plato × piñón con los platos y el cassette (design.md del change `transmisiones-de-marca`, decisión 9). */
data class PlatosUi(val plato: Int, val pinon: Int, val platos: List<Int>, val pinones: List<Int>)

/** Un dato de la rejilla con su valor ya formateado; [valor] `null` se muestra como «—». [tono] colorea la pendiente. */
data class DatoUi(val dato: DatoRodaje, val valor: String?, val unidad: String? = null, val tono: TonoDato = TonoDato.Neutro)

/**
 * La guía de inicio a la vista (spec `getting-started-guide`; maquetas `guia-*`): la [pagina] (1 a 3), si la hoja está
 * [abierta] o minimizada en el pie, los [pasos] con su estado, los modelos reales del rodillo y del controlador (`null` si
 * no están configurados) y, para la página 3, el [modo] de conexión, el [nombreApp] con el que se ve la app y el
 * [nombreMovil] Bluetooth.
 */
data class GuiaUi(
    val pagina: Int,
    val abierta: Boolean,
    val pasos: PasosGuia,
    val rodillo: String?,
    val controlador: String?,
    val modo: ModoConexion,
    val nombreApp: String,
    val nombreMovil: String?,
)

/** Pantalla de rodaje (maquetas/rodaje.html y rodaje-vertical.html, estados lineales y Shimano, con y sin Zwift). */
data class RodajeUiState(
    /** Con la lineal, la marcha, el total y la referencia; con platos, [platos]. */
    val marcha: Int = 12,
    val total: Int = 24,
    val referencia: Int = 12,
    val platos: PlatosUi? = null,
    val transmision: Transmision = Transmision.Lineal(total),
    val modoCambio: ModoCambio = ModoCambio.AUTOMATICO,
    val potencia: Int? = null,
    val cadencia: Int? = null,
    /**
     * Los datos elegidos en Ajustes, en su orden (spec `ride-screen`, «Datos elegidos en Rodaje»); sin ellos, los de
     * por defecto con [potencia] y [cadencia] ([datosVisibles]).
     */
    val datos: List<DatoUi>? = null,
    /** Botones − y + en pantalla (F11.8): con la casilla de Ajustes o con el Square no conectado. */
    val botones: Boolean = false,
    /** El indicador de modo se puede tocar (ajuste «Cambiar de modo tocando»); independiente de [botones]. */
    val modoTocable: Boolean = false,
    /** Sin app de entrenamiento enviando pendiente, el modo se llama «LIBRE»; con ella, «SIM» (en ERG no se usa). */
    val libre: Boolean = true,
    /** Los dispositivos que se usan, en orden fijo: Neo, Square y, si están configurados, el pulsómetro y el ventilador (`ConnectionBar`). */
    val enlaces: List<EnlaceUi> = listOf(Enlace.Neo, Enlace.Square).map { EnlaceUi(it, EstadoChip.Sin, EnlaceUi.EnPalabra.SinConexion) },
    /** El estado de la plataforma, bajo la fila de Ajustes (`PlatformBadge`). */
    val plataforma: PlataformaUi = PlataformaUi(EstadoChip.Esperando, PlataformaUi.Texto.Esperando, EnlaceUi.EnPalabra.Esperando),
    /** En ERG, la potencia objetivo, que sustituye a la marcha; `null` en SIM. */
    val erg: ErgUi? = null,
    /** Lo que dice el pie cuando no hay aviso de acción, que tiene prioridad. */
    val pie: PieUi = PieUi.TocarParaConectar,
    /** Tope alcanzado hace menos de 1,5 s. */
    val tope: TopeMarcha? = null,
    /** Acción de hace menos de 1,5 s; `null` si no hay aviso. */
    val aviso: AvisoUi? = null,
    /** Los giros de Zwift pulsados (spec `ride-screen`, «Aviso de giro»), a cada lado de la marcha. */
    val giro: Set<LadoGiro> = emptySet(),
    /** El aviso de salida de Atrás, flotando sobre Rodaje. */
    val salida: SalidaUi = SalidaUi.Ninguna,
    /** La guía de inicio, si se ve; `null` si está descartada, terminada o cerrada con la equis. */
    val guia: GuiaUi? = null,
) {
    fun enlace(enlace: Enlace): EnlaceUi? = enlaces.firstOrNull { it.enlace == enlace }

    val datosVisibles: List<DatoUi>
        get() = datos ?: listOf(DatoUi(DatoRodaje.POTENCIA, potencia?.toString(), "W"), DatoUi(DatoRodaje.CADENCIA, cadencia?.toString(), "rpm"))
}

/** Buscando, conectando o reconectando: la app lo intenta y el chip dice «conectando». */
private fun enIntento(e: EstadoDispositivo) = e == EstadoDispositivo.Buscando || e == EstadoDispositivo.Conectando || e == EstadoDispositivo.Reconectando

fun enlace(enlace: Enlace, e: EstadoDispositivo, bateriaBaja: Boolean = false): EnlaceUi = when {
    // Solo conectado: con otro estado, el de conexión (spec `ride-screen`, «Batería baja del Square en la barra de conexiones»).
    e == EstadoDispositivo.Conectado && bateriaBaja -> EnlaceUi(enlace, EstadoChip.Baja, EnlaceUi.EnPalabra.BateriaBaja)

    e == EstadoDispositivo.Conectado -> EnlaceUi(enlace, EstadoChip.Ok, null)

    enIntento(e) -> EnlaceUi(enlace, EstadoChip.Esperando, EnlaceUi.EnPalabra.Conectando)

    else -> EnlaceUi(enlace, EstadoChip.Sin, EnlaceUi.EnPalabra.SinConexion)
}

/** El ventilador, solo con uno configurado (spec `ride-screen`, «Ventilador en la barra de conexiones»); sin modo ni velocidad en el icono. */
fun enlaceVentilador(e: EstadoDispositivo, configurado: Boolean): EnlaceUi? = when {
    !configurado -> null
    e == EstadoDispositivo.Reconectando -> EnlaceUi(Enlace.Ventilador, EstadoChip.Esperando, EnlaceUi.EnPalabra.Reconectando)
    else -> enlace(Enlace.Ventilador, e)
}

/** El pulsómetro, solo con uno configurado (spec `ride-screen`, «Pulsómetro en la barra de conexiones»); sin pulso en el icono. */
fun enlacePulso(e: EstadoDispositivo, configurado: Boolean): EnlaceUi? = when {
    !configurado -> null
    e == EstadoDispositivo.Reconectando -> EnlaceUi(Enlace.Pulso, EstadoChip.Esperando, EnlaceUi.EnPalabra.Reconectando)
    else -> enlace(Enlace.Pulso, e)
}

/**
 * El estado de lo publicado en Rodaje (spec `ride-screen`, «Estado de la publicación en Rodaje» y «Lo que falta en el estado
 * de Rodaje»), sin nombrar la app de entrenamiento ni el transporte, salvo cuando falta: «Conectado» si la app de
 * entrenamiento usa el rodillo; «esperando» mientras la app se anuncia o no lo usa; «no visible», «sin wifi», «sin
 * Bluetooth», «no disponible» o «sin rodillo». Con el rodillo conectado, «· sin mando» si el mando va aparte por
 * Bluetooth («Wifi + Bluetooth») y no se ha saludado, y «· sin pulso» si se publica y la app de entrenamiento no lo usa.
 */
fun plataformaUi(c: Configuracion, e: EstadoRodaje): PlataformaUi {
    val mandoAparte = e.servicios?.mando == MandoPublicado.APARTE_BLUETOOTH && c.modoPublicado.mando == MandoEnModo.BLUETOOTH_APARTE
    val sinMando = mandoAparte && e.mando != EstadoMando.Conectado
    val mandoEnPalabra = when {
        !sinMando -> null
        e.mando == EstadoMando.SinConexion || e.mando == EstadoMando.NoDisponible -> EnlaceUi.EnPalabra.SinConexion
        else -> EnlaceUi.EnPalabra.Esperando
    }
    val sinPulso = e.servicios?.pulso == true && !e.usoPulso
    val pulsoEnPalabra = if (sinPulso) EnlaceUi.EnPalabra.Esperando else null
    return when (e.plataforma) {
        is EstadoPlataforma.Conectado -> {
            if (!e.usoRodillo) return PlataformaUi(EstadoChip.Esperando, PlataformaUi.Texto.Esperando, EnlaceUi.EnPalabra.Esperando)
            val texto = when {
                sinMando && sinPulso -> PlataformaUi.Texto.ConectadoSinMandoNiPulso
                sinMando -> PlataformaUi.Texto.ConectadoSinMando
                sinPulso -> PlataformaUi.Texto.ConectadoSinPulso
                else -> PlataformaUi.Texto.Conectado
            }
            PlataformaUi(EstadoChip.Ok, texto, null, mandoEnPalabra, pulsoEnPalabra)
        }

        EstadoPlataforma.Esperando -> PlataformaUi(EstadoChip.Esperando, PlataformaUi.Texto.Esperando, EnlaceUi.EnPalabra.Esperando)

        EstadoPlataforma.NoVisible -> PlataformaUi(EstadoChip.Sin, PlataformaUi.Texto.NoVisible, EnlaceUi.EnPalabra.NoVisible)

        EstadoPlataforma.SinWifi -> PlataformaUi(EstadoChip.Sin, PlataformaUi.Texto.SinWifi, EnlaceUi.EnPalabra.SinConexion)

        EstadoPlataforma.SinBluetooth -> PlataformaUi(EstadoChip.Sin, PlataformaUi.Texto.SinBluetooth, EnlaceUi.EnPalabra.SinConexion)

        EstadoPlataforma.NoDisponible -> PlataformaUi(EstadoChip.Sin, PlataformaUi.Texto.NoDisponible, EnlaceUi.EnPalabra.NoDisponible)

        EstadoPlataforma.NoSeOfrece -> PlataformaUi(EstadoChip.Sin, PlataformaUi.Texto.SinRodillo, null)
    }
}

/** La potencia objetivo si manda un ERG; en Diagnóstico con un envío manual, lo que había debajo. */
fun ergUi(modo: ModoControl): ErgUi? = when (val base = (modo as? ModoControl.Manual)?.anterior ?: modo) {
    is ModoControl.ErgPlataforma -> ErgUi(base.potenciaW, base.objetivoW, base.ajusteW)
    is ModoControl.ErgManual -> ErgUi(base.potenciaW, base.entradaW, base.ajusteW, manual = true)
    else -> null
}

/** Los botones − y + se ven con la casilla de Ajustes o, si el Square no está conectado (también buscando o reconectando), siempre. */
fun botonesVisibles(casilla: Boolean, square: EstadoDispositivo): Boolean = casilla || square != EstadoDispositivo.Conectado

/** «LIBRE» si no manda ninguna app: la simulación sin terreno de la plataforma (también tras los 30 s de su ausencia). */
fun ModoControl.esLibre(): Boolean = when (this) {
    ModoControl.SimLibre -> true
    is ModoControl.SimPlataforma, is ModoControl.Erg -> false
    is ModoControl.Manual -> anterior.esLibre()
}

/**
 * La pendiente de la simulación: el terreno de la plataforma, la pendiente base (llano) sin ella y `null` en ERG. Nunca el
 * ajuste de la marcha ni lo enviado al Neo (spec `ride-screen`, «Sin datos del modelo de resistencia»).
 */
fun pendienteDeSimulacion(s: Simulacion): Double? = when (val m = (s.modo as? ModoControl.Manual)?.anterior ?: s.modo) {
    ModoControl.SimLibre -> s.pendienteBasePct
    is ModoControl.SimPlataforma -> m.terreno.pendientePct
    else -> null
}

/** «+4,2 %», «−7,0 %», «0,0 %»; sin pendiente (ERG), «—». */
fun pendienteUi(pct: Double?, locale: Locale): DatoUi {
    if (pct == null) return DatoUi(DatoRodaje.PENDIENTE, null, "%")
    val cifra = String.format(locale, "%.1f", abs(pct))
    val cero = cifra.all { !it.isDigit() || it == '0' }
    val signo = if (cero) {
        ""
    } else if (pct > 0) {
        "+"
    } else {
        "−"
    }
    val tono = when {
        pct > 0.5 -> TonoDato.Subida
        pct < -0.5 -> TonoDato.Bajada
        else -> TonoDato.Neutro
    }
    return DatoUi(DatoRodaje.PENDIENTE, signo + cifra, "%", tono)
}

fun pie(neo: EstadoDispositivo, square: EstadoDispositivo): PieUi = when {
    enIntento(neo) && enIntento(square) -> PieUi.DespertarLosDos
    enIntento(neo) -> PieUi.DespertarNeo
    enIntento(square) -> PieUi.DespertarSquare
    neo != EstadoDispositivo.Conectado || square != EstadoDispositivo.Conectado -> PieUi.TocarParaConectar
    else -> PieUi.Nada
}

/** «42:18» o «1:12:40». */
fun textoTiempo(ms: Long): String {
    val s = ms / 1000
    val (h, m, seg) = Triple(s / 3600, s / 60 % 60, s % 60)
    return if (h > 0) "%d:%02d:%02d".format(h, m, seg) else "%d:%02d".format(m, seg)
}

/** Los datos elegidos con los valores de la sesión: sin Neo, los del momento son «—»; los de la sesión se conservan. */
fun datosDeRodaje(e: EstadoRodaje, c: Configuracion): List<DatoUi> {
    val locale = (c.idioma ?: IdiomaApp.ES).locale
    val conNeo = e.neo == EstadoDispositivo.Conectado && e.potencia != null
    val d = e.datos
    return c.datos.map { dato ->
        when (dato) {
            DatoRodaje.POTENCIA -> DatoUi(dato, d.potencia3sW.toString().takeIf { conNeo }, "W")
            DatoRodaje.CADENCIA -> DatoUi(dato, e.cadencia?.toString()?.takeIf { conNeo }, "rpm")
            DatoRodaje.TIEMPO -> DatoUi(dato, textoTiempo(d.tiempoMs))
            DatoRodaje.W_KG -> DatoUi(dato, String.format(locale, "%.1f", d.wKg(c.pesoCiclistaKg)).takeIf { conNeo })
            DatoRodaje.POTENCIA_MEDIA -> DatoUi(dato, d.mediaW.toString(), "W")
            DatoRodaje.POTENCIA_MAXIMA -> DatoUi(dato, d.maximaW.toString(), "W")
            DatoRodaje.VELOCIDAD -> DatoUi(dato, e.velocidadKmh?.let { String.format(locale, "%.1f", it) }, "km/h")
            DatoRodaje.DISTANCIA -> DatoUi(dato, String.format(locale, "%.2f", e.distanciaKm), "km")
            DatoRodaje.PENDIENTE -> pendienteUi(pendienteDeSimulacion(e.simulacion), locale)
        }
    }
}

/**
 * Con «Mostrar la acción» desactivado no hay aviso de acción, pero el tope de potencia sí (spec `ride-screen`, «Aviso
 * de la acción desactivable»); el tope de la marcha va aparte y no se toca.
 */
fun avisoVisible(aviso: AvisoUi?, mostrarAccion: Boolean): AvisoUi? = if (mostrarAccion || aviso is AvisoUi.TopePotencia) aviso else null

/** La guía de inicio de [e] con lo que recuerda [pantalla] de la ejecución, o `null` si no se ve (spec `getting-started-guide`, «Cuándo se ve la guía»). */
fun guiaUi(e: EstadoRodaje, pantalla: GuiaPantalla, c: Configuracion, nombreMovil: String?): GuiaUi? {
    val pasos = pasosGuia(e)
    if (!pantalla.visible(e.guiaInicio, pasos.completa)) return null
    return GuiaUi(
        pagina = pantalla.pagina,
        abierta = pantalla.abierta,
        pasos = pasos,
        rodillo = e.neoConfigurado?.nombre,
        controlador = e.squareConfigurado?.nombre,
        modo = c.conexion.modo,
        nombreApp = e.nombreAnunciado,
        nombreMovil = nombreMovil,
    )
}

fun estadoDeRodaje(
    e: EstadoRodaje,
    tope: TopeMarcha?,
    aviso: AvisoUi?,
    c: Configuracion = Configuracion(),
    salida: SalidaUi = SalidaUi.Ninguna,
    guia: GuiaPantalla = GuiaPantalla(),
    nombreMovil: String? = null,
): RodajeUiState {
    val conNeo = e.neo == EstadoDispositivo.Conectado
    val lineal = e.marcha as? EstadoMarcha.Numerada
    val conPlatos = e.transmision as? Transmision.Platos
    return RodajeUiState(
        marcha = lineal?.posicion ?: 0,
        total = lineal?.total ?: 0,
        referencia = (e.referencia as? EstadoMarcha.Numerada)?.posicion ?: 0,
        platos = (e.marcha as? EstadoMarcha.PlatoPinon)?.let { m ->
            PlatosUi(m.plato, m.pinon, conPlatos?.platos.orEmpty(), conPlatos?.pinones.orEmpty())
        },
        transmision = e.transmision,
        modoCambio = e.modoCambio,
        potencia = if (conNeo) e.potencia else null,
        cadencia = if (conNeo) e.cadencia else null,
        datos = datosDeRodaje(e, c),
        botones = botonesVisibles(c.botonesEnPantalla, e.square),
        modoTocable = c.modoTactil,
        libre = e.simulacion.modo.esLibre(),
        enlaces = listOfNotNull(enlace(Enlace.Neo, e.neo), enlace(Enlace.Square, e.square, BateriaSquare.baja(e.bateriaSquare)), enlacePulso(e.pulsometro, e.pulsometroConfigurado != null), enlaceVentilador(e.ventilador, e.ventiladorConfigurado != null)),
        erg = ergUi(e.simulacion.modo),
        pie = pie(e.neo, e.square),
        tope = tope,
        aviso = avisoVisible(aviso, c.mostrarAccion),
        plataforma = plataformaUi(c, e),
        giro = e.giro,
        salida = salida,
        guia = guiaUi(e, guia, c, nombreMovil),
    )
}
