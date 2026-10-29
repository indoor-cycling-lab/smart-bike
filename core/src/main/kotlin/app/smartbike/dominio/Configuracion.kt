package app.smartbike.dominio

import kotlin.random.Random
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

/**
 * Configuración de la app con los valores por defecto de `funcional.md` (arquitectura.md § 10), guardada en
 * `configuracion.json`. Desde la versión 2, cada transmisión tiene su apartado con su referencia y su asignación de
 * botones (design.md del change `transmisiones-de-marca`, decisión 6). La versión 3 añade la regla de △ larga para
 * alternar SIM ⇄ ERG (design.md del change `erg-manual`, decisión 5). La versión 4 añade los datos en pantalla, los
 * botones − y +, el peso del ciclista y las reglas de pantalla anterior o siguiente (design.md del change
 * `rodaje-completo-y-ajustes`, decisión 6). La versión 5 añade el mando de Zwift y las reglas de sus funciones (design.md
 * del change `mando-zwift-emulado`, decisión 7). La versión 6 añade el modo de conexión con Zwift (design.md del change
 * `mando-zwift-dircon`, decisión 5), sin migración: los documentos anteriores lo leen en el modo de siempre. La versión 8
 * añade la plataforma y la identidad ante la app (design.md del change `pestana-plataformas`, decisión 2): el interruptor
 * `simularKickrBike` pasa a la identidad. La versión 9 guarda los ajustes de cada plataforma (modo, mando y nombre ante
 * la app) en [plataformas] (design.md del change `generalizar-plataformas`, decisión 2), sin migración: un documento
 * anterior conserva todo lo que no es de la plataforma y toma sus ajustes por defecto. La versión 10 deja de elegir
 * plataforma y quita el interruptor del mando: solo guarda el modo y el nombre en [conexion] (design.md del change
 * `pestana-conexion`, decisión 1), también sin migración. La versión 11 añade la grabación de sesiones y su espacio libre
 * mínimo (design.md del change `registro-sesiones`, decisión 9), igualmente sin migración. La versión 12 añade el idioma de la interfaz (design.md del change `idioma-es-en`, decisión 1), también
 * sin migración.
 */
@Serializable
data class Configuracion(
    val version: Int = VERSION,
    /** La transmisión elegida: [Transmision.Lineal.ID] o el de un desarrollo de [Desarrollos]. */
    val transmision: String = Transmision.Lineal.ID,
    val lineal: AjustesLineal = AjustesLineal(),
    /** Por desarrollo; si falta, sus valores por defecto. */
    val platos: Map<String, AjustesPlatos> = emptyMap(),
    val incrementoPct: Double = 1.0,
    val recordarUltimaMarcha: Boolean = true,
    val mostrarAccion: Boolean = true,
    val vibracion: Boolean = true,
    val botones: ConfiguracionBotones = ConfiguracionBotones(),
    /** Opcional y con valor por defecto: los documentos anteriores sin él se leen igual, sin migración (F6.5). */
    val bajadas: ModoBajadas = ModoBajadas.LA_MARCHA_NO_CREA_BAJADAS,
    /** Los datos de Rodaje, en su orden: de 1 a [MAX_DATOS], sin repetidos (F11.3). */
    @Serializable(with = DatosSerializer::class) val datos: List<DatoRodaje> = DATOS_POR_DEFECTO,
    /** Botones − y + en Rodaje (F11.8), desactivados por defecto (decisión abierta 6 de `funcional.md`). */
    val botonesEnPantalla: Boolean = false,
    /** Tocar el indicador de modo de Rodaje alterna Libre o SIM ⇄ ERG manual (F7.3), desactivado por defecto; opcional como [bajadas]: los documentos anteriores lo leen desactivado. */
    val modoTactil: Boolean = false,
    /** Para W/kg, entre [PESO_KG] (F12.2). */
    val pesoCiclistaKg: Int = 75,
    /** Opcional y con valor por defecto, como [bajadas]: los documentos sin él se leen en oscuro (F12.8). */
    val tema: TemaApp = TemaApp.OSCURO,
    /**
     * El idioma de la interfaz; `null` es «aún sin decidir»: la app lo resuelve en el primer arranque con el del móvil y lo
     * guarda (change `idioma-es-en`). Opcional como [tema]: los documentos sin él se leen sin decidir.
     */
    val idioma: IdiomaApp? = null,
    /** Opcional y con valor por defecto, como [tema]: los documentos sin él se leen «según el móvil» (F11, F12.8). */
    val orientacion: OrientacionApp = OrientacionApp.SEGUN_MOVIL,
    /** El modo de conexión y el nombre ante la app (spec `platform-settings`); opcional como [bajadas]. */
    val conexion: AjustesConexion = AjustesConexion(),
    /** Modelo de resistencia (F6); opcional como [bajadas]: los documentos anteriores lo leen «simple». */
    val modelo: TipoModelo = TipoModelo.SIMPLE,
    /** Dificultad global en % (F6.4), dentro de [DIFICULTAD_PCT]. */
    val dificultadPct: Int = 100,
    /** Potencia mínima del modelo físico en W (F6.3, F12.5), dentro de [POTENCIA_MINIMA_W]. */
    val potenciaMinimaW: Int = 30,
    /** Para el modelo físico, en kg, dentro de [PESO_BICI_KG] (F12.5). */
    val pesoBiciKg: Double = 8.0,
    /** Las tablas editadas, por transmisión; una ausente o con otro número de marchas es la del modelo simple (F6.2). */
    val tablas: Map<String, List<Double>> = emptyMap(),
    /** Graba cada sesión en un archivo de sucesos (F13.4); apagado por defecto y opcional como [bajadas]. */
    val grabarSesiones: Boolean = false,
    /** Espacio libre mínimo para grabar, en % del total, dentro de [ESPACIO_LIBRE_MINIMO_PCT]. */
    val espacioLibreMinimoPct: Int = 10,
) {
    /** El modo elegido, tal como lo describe la tabla de compatibilidad. */
    val modoPublicado: ModoPublicado get() = Compatibilidad.deModo(conexion.modo)

    /**
     * El nombre ante la app que se usa de verdad: el elegido, o «Smart Bike» si el modo no deja elegirlo (por Bluetooth es
     * el nombre del móvil) y [AjustesConexion.nombrePorBluetooth] está desactivado. El elegido se conserva en [conexion]
     * para cuando se vuelva a un modo que lo admita.
     */
    val identidadEfectiva: Identidad
        get() = if (modoPublicado.nombreElegible || conexion.nombrePorBluetooth) conexion.identidad else Identidad.SMART_BIKE

    /** La app se anuncia como «KICKR BIKE PRO …» (la identidad efectiva). */
    val simularKickrBike: Boolean get() = identidadEfectiva == Identidad.KICKR_BIKE_PRO

    /** La transmisión elegida; un identificador desconocido es la lineal. */
    val transmisionElegida: Transmision
        get() = Desarrollos.porId(transmision) ?: Transmision.Lineal(lineal.marchas)

    fun ajustes(t: Transmision.Platos): AjustesPlatos = platos[t.id] ?: AjustesPlatos()

    /** La referencia de [t]; una guardada que no pertenece al desarrollo es la de por defecto. */
    fun referencia(t: Transmision): EstadoMarcha = when (t) {
        is Transmision.Lineal -> t.marcha(lineal.referencia)
        is Transmision.Platos -> ajustes(t).referencia?.takeIf { t.contiene(it) } ?: t.referencia
    }

    fun asignacion(t: Transmision): Asignacion = when (t) {
        is Transmision.Lineal -> lineal.asignacion
        is Transmision.Platos -> ajustes(t).asignacion
    }

    fun modo(t: Transmision): ModoCambio = (t as? Transmision.Platos)?.let { ajustes(it).modo } ?: ModoCambio.AUTOMATICO

    /** La referencia, la asignación y el modo de la transmisión elegida. */
    val referencia: EstadoMarcha get() = referencia(transmisionElegida)
    val asignacion: Asignacion get() = asignacion(transmisionElegida)
    val modo: ModoCambio get() = modo(transmisionElegida)

    /** Cambia la referencia de la transmisión de [m], sin tocar las demás. */
    fun conReferencia(m: EstadoMarcha): Configuracion = when (val t = transmisionElegida) {
        is Transmision.Lineal -> copy(lineal = lineal.copy(referencia = (m as EstadoMarcha.Numerada).posicion))
        is Transmision.Platos -> conPlatos(t) { it.copy(referencia = m as EstadoMarcha.PlatoPinon) }
    }

    /** Cambia la asignación de la transmisión elegida, sin tocar las demás. */
    fun conAsignacion(a: Asignacion): Configuracion = when (val t = transmisionElegida) {
        is Transmision.Lineal -> copy(lineal = lineal.copy(asignacion = a))
        is Transmision.Platos -> conPlatos(t) { it.copy(asignacion = a) }
    }

    fun conModo(modo: ModoCambio): Configuracion = (transmisionElegida as? Transmision.Platos)?.let { t -> conPlatos(t) { it.copy(modo = modo) } } ?: this

    private fun conPlatos(t: Transmision.Platos, f: (AjustesPlatos) -> AjustesPlatos) = copy(platos = platos + (t.id to f(ajustes(t))))

    /** El modelo simple con el incremento y la neutra de Ajustes, sobre [t]. */
    fun modeloSimple(t: Transmision): ModeloSimple = ModeloSimple(incrementoPct, lineal.neutra, t)

    /** La tabla de [t]: la editada, o el reparto del modelo simple si no hay o no encaja. */
    fun tabla(t: Transmision): List<Double> = tablas[t.id]?.takeIf { it.size == t.escalera().size } ?: modeloSimple(t).tablaPorDefecto()

    /** Guarda [valores] como la tabla de [t]. */
    fun conTabla(t: Transmision, valores: List<Double>): Configuracion = copy(tablas = tablas + (t.id to valores))

    /** Vuelve la tabla de [t] al reparto del modelo simple. */
    fun sinTabla(t: Transmision): Configuracion = copy(tablas = tablas - t.id)

    /** El modelo de resistencia elegido, sobre [t] (design.md del change `modelos-tabla-y-fisico`). */
    fun modeloResistencia(t: Transmision): ModeloResistencia = when (modelo) {
        TipoModelo.SIMPLE -> modeloSimple(t)
        TipoModelo.TABLA -> ModeloTabla(tabla(t), t)
        TipoModelo.FISICO -> ModeloFisico(pesoCiclistaKg.toDouble(), pesoBiciKg, t)
    }

    /** Datos, peso, pendiente y tiempos dentro de sus límites, para un documento guardado a mano o de otra versión. */
    fun normalizada(): Configuracion = copy(
        dificultadPct = dificultadPct.coerceIn(DIFICULTAD_PCT),
        potenciaMinimaW = potenciaMinimaW.coerceIn(POTENCIA_MINIMA_W),
        pesoBiciKg = pesoBiciKg.coerceIn(PESO_BICI_KG),
        espacioLibreMinimoPct = espacioLibreMinimoPct.coerceIn(ESPACIO_LIBRE_MINIMO_PCT),
        tablas = tablas.filter { (id, v) ->
            val t = if (id == Transmision.Lineal.ID) Transmision.Lineal(lineal.marchas) else Desarrollos.porId(id)
            t != null && v.size == t.escalera().size
        }.mapValues { (_, v) -> v.map { it.coerceIn(AJUSTE_TABLA_PCT) } },
    ).normalizadaBase()

    private fun normalizadaBase(): Configuracion = copy(
        datos = datos.distinct().take(MAX_DATOS).ifEmpty { DATOS_POR_DEFECTO },
        pesoCiclistaKg = pesoCiclistaKg.coerceIn(PESO_KG),
        botones = botones.copy(tiempos = botones.tiempos.normalizados()),
    )

    companion object {
        const val VERSION = 12

        const val MAX_DATOS = 6
        val DATOS_POR_DEFECTO = listOf(DatoRodaje.POTENCIA, DatoRodaje.CADENCIA)
        val PESO_KG = 40..150
        val DIFICULTAD_PCT = 50..150
        const val PASO_DIFICULTAD_PCT = 5
        val POTENCIA_MINIMA_W = 0..150
        const val PASO_POTENCIA_MINIMA_W = 5
        val PESO_BICI_KG = 5.0..25.0
        const val PASO_PESO_BICI_KG = 0.5
        val AJUSTE_TABLA_PCT = -20.0..20.0
        const val PASO_TABLA_PCT = 0.1
        val ESPACIO_LIBRE_MINIMO_PCT = 5..50
        const val PASO_ESPACIO_LIBRE_MINIMO_PCT = 5

        private val json = Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }

        fun aJson(c: Configuracion): String = json.encodeToString(serializer(), c)

        /**
         * Un documento ilegible se sustituye por los valores por defecto (spec `button-mapping`, «Configuración
         * ilegible»), y un campo inválido, por el suyo ([leerGuardada]); los tiempos fuera de sus límites se recortan. Uno de la versión 1 se migra: su
         * referencia, su neutra y su asignación pasan a la lineal (decisión 6). Uno anterior a la versión 3 recibe la
         * regla de alternar SIM ⇄ ERG donde esté libre, una sola vez; uno anterior a la versión 4, las de pantalla
         * anterior y siguiente, y uno anterior a la 5, las funciones de Zwift ([conReglasSiLibres]). Datos, peso,
         * pendiente y tiempos se recortan ([normalizada]).
         */
        fun deJson(texto: String): Configuracion = leerGuardada(texto).valor

        /**
         * Lo que [deJson], diciendo lo que se perdió: un campo que no se entiende toma su valor por defecto y los demás
         * se conservan ([leerPorCampos]); solo un documento que no es un objeto JSON se sustituye entero.
         */
        fun leerGuardada(texto: String): Lectura<Configuracion> = try {
            leerPorCampos(json.parseToJsonElement(texto).jsonObject, ::leer)
        } catch (_: Exception) {
            Lectura(Configuracion(), ilegible = true)
        }

        /**
         * Lo que [deJson] hace con un documento, pero sin sustituir por los valores por defecto el que no se entiende:
         * dice por qué (design.md del change `exportar-bateria-y-giro`, decisión 2). Un documento válido de una versión
         * anterior se migra y se recorta como al abrir la app; uno de una versión más nueva se rechaza.
         */
        fun validar(texto: String): ResultadoImportacion {
            if (texto.isBlank()) return ResultadoImportacion.Invalida(MotivoImportacion.VACIO)
            val raiz = try {
                json.parseToJsonElement(texto)
            } catch (_: Exception) {
                return ResultadoImportacion.Invalida(MotivoImportacion.NO_ES_JSON)
            }
            val objeto = raiz as? JsonObject ?: return ResultadoImportacion.Invalida(MotivoImportacion.NO_ES_CONFIGURACION)
            val version = (objeto["version"] as? JsonPrimitive)?.takeIf { !it.isString }?.intOrNull
            if (version == null || version < 1) return ResultadoImportacion.Invalida(MotivoImportacion.NO_ES_CONFIGURACION)
            if (version > VERSION) return ResultadoImportacion.Invalida(MotivoImportacion.VERSION_MAS_NUEVA)
            return try {
                ResultadoImportacion.Valida(leer(objeto))
            } catch (_: Exception) {
                ResultadoImportacion.Invalida(MotivoImportacion.NO_ES_CONFIGURACION)
            }
        }

        private fun leer(objeto: JsonObject): Configuracion {
            val version = objeto["version"]?.jsonPrimitive?.intOrNull ?: 1
            val v2 = if (version < 2) migrarV1(objeto) else objeto
            var c = json.decodeFromJsonElement(serializer(), v2)
            if (version < 3) c = c.conReglasSiLibres(Asignacion.ALTERNAR_ERG)
            if (version < 4) c = c.conReglasSiLibres(Asignacion.PANTALLA_ANTERIOR, Asignacion.PANTALLA_SIGUIENTE)
            if (version < 5) c = c.conReglasSiLibres(*Asignacion.ZWIFT_POR_DEFECTO.toTypedArray())
            return c.copy(version = VERSION).normalizada()
        }

        /**
         * A la asignación de la lineal y a la de cada desarrollo guardado se les añaden [reglas] si su entrada y su gesto
         * están libres; los desarrollos sin guardar ya las traen por defecto.
         */
        private fun Configuracion.conReglasSiLibres(vararg reglas: Regla): Configuracion {
            fun Asignacion.conLibres() = reglas.fold(this) { a, r -> a.conReglaSiLibre(r) }
            return copy(
                lineal = lineal.copy(asignacion = lineal.asignacion.conLibres()),
                platos = platos.mapValues { (_, p) -> p.copy(asignacion = p.asignacion.conLibres()) },
            )
        }

        private fun migrarV1(v1: JsonObject): JsonObject = buildJsonObject {
            val anteriores = setOf("version", "transmision", "marchaInicial", "marchaReferencia", "marchaNeutra")
            v1.forEach { (clave, valor) -> if (clave !in anteriores) put(clave, valor) }
            put("version", VERSION)
            put("transmision", Transmision.Lineal.ID)
            put(
                "lineal",
                buildJsonObject {
                    v1["transmision"]?.jsonObject?.get("marchas")?.let { put("marchas", it) }
                    v1["marchaReferencia"]?.let { put("referencia", it) }
                    v1["marchaNeutra"]?.let { put("neutra", it) }
                    v1["botones"]?.jsonObject?.get("asignacion")?.let { put("asignacion", it) }
                },
            )
        }
    }
}

/** Un dato de la rejilla de Rodaje (F11.3). Velocidad y distancia las calcula el modelo físico (decisión 12 de `funcional.md`). */
@Serializable
enum class DatoRodaje {
    @SerialName("potencia")
    POTENCIA,

    @SerialName("cadencia")
    CADENCIA,

    @SerialName("tiempo")
    TIEMPO,

    @SerialName("w-kg")
    W_KG,

    @SerialName("potencia-media")
    POTENCIA_MEDIA,

    @SerialName("potencia-maxima")
    POTENCIA_MAXIMA,

    @SerialName("velocidad")
    VELOCIDAD,

    @SerialName("distancia")
    DISTANCIA,

    /** La pendiente de la simulación (la que envía la app; 0 en Libre; sin dato en ERG), nunca la base ni lo enviado al Neo. */
    @SerialName("pendiente")
    PENDIENTE,
}

/** Los modelos de resistencia (F6): cómo una marcha se traduce en resistencia. */
@Serializable
enum class TipoModelo {
    @SerialName("simple")
    SIMPLE,

    @SerialName("tabla")
    TABLA,

    @SerialName("fisico")
    FISICO,

    ;

    /** Simple → por tabla → físico → simple (acción «Siguiente modelo»). */
    fun siguiente(): TipoModelo = entries[(ordinal + 1) % entries.size]
}

/**
 * Lista de datos que, al leerla, descarta los que no se entienden (uno de una versión futura) sin invalidar el resto,
 * como [ReglasSerializer].
 */
object DatosSerializer : KSerializer<List<DatoRodaje>> {
    private val lista = ListSerializer(DatoRodaje.serializer())
    override val descriptor: SerialDescriptor = lista.descriptor

    override fun serialize(encoder: Encoder, value: List<DatoRodaje>) = lista.serialize(encoder, value)

    override fun deserialize(decoder: Decoder): List<DatoRodaje> {
        val json = decoder as? JsonDecoder ?: return lista.deserialize(decoder)
        val elementos = json.decodeJsonElement() as? JsonArray ?: return emptyList()
        return elementos.mapNotNull { runCatching { json.json.decodeFromJsonElement(DatoRodaje.serializer(), it) }.getOrNull() }
    }
}

/** Ajustes de la transmisión lineal (F5.5, F6.1). */
@Serializable
data class AjustesLineal(
    val marchas: Int = 24,
    val referencia: Int = 12,
    val neutra: Int = 12,
    val asignacion: Asignacion = Asignacion.POR_DEFECTO,
) {
    init {
        require(marchas >= 2 && referencia in 1..marchas && neutra in 1..marchas) { "Marchas fuera de 1..$marchas" }
    }
}

/** Ajustes de una transmisión de platos; sin [referencia], la del desarrollo (F5.8). */
@Serializable
data class AjustesPlatos(
    val modo: ModoCambio = ModoCambio.AUTOMATICO,
    val referencia: EstadoMarcha.PlatoPinon? = null,
    val asignacion: Asignacion = Asignacion.POR_DEFECTO_PLATOS,
)

/** Tiempos de los gestos y autorrepetición (F3.5; spec `button-mapping`), comunes a todas las transmisiones. */
@Serializable
data class ConfiguracionBotones(
    val tiempos: TiemposGestos = TiemposGestos(),
    val autorrepeticion: Boolean = true,
)

/** Un Neo o un Square conectado alguna vez, para conectarse a él sin buscarlo (F1.2). */
@Serializable
data class DispositivoConfigurado(val direccion: String, val nombre: String)

/**
 * Identidad del dispositivo que la app simula ante la plataforma en el anuncio DirCon (`mac-address` y `serial-number`),
 * para que la plataforma reconozca el mismo dispositivo entre sesiones y se reconecte sola (design.md del change
 * `puente-zwift-wifi`, decisión 9). Se genera una vez por [ClaveIdentidad] y se guarda.
 */
@Serializable
data class IdentidadDispositivo(val mac: String, val serie: String) {
    companion object {
        /** Dirección local administrada (bit 1 del primer byte a 1, unidifusión) y número de serie de 10 cifras. */
        fun nuevo(aleatorio: Random = Random.Default): IdentidadDispositivo {
            val bytes = aleatorio.nextBytes(6)
            bytes[0] = ((bytes[0].toInt() or 0x02) and 0xFE).toByte()
            val mac = bytes.joinToString(":") { "%02X".format(it) }
            val serie = (1..10).joinToString("") { aleatorio.nextInt(10).toString() }
            return IdentidadDispositivo(mac, serie)
        }
    }
}

/**
 * La clave de [EstadoGuardado.identidades]: una identidad por modo y nombre ante la app, para que cada una cachee la suya
 * y una no contamine a otra (design.md del change `pestana-conexion`, decisión 5). Las claves anteriores, con la
 * plataforma delante (`zwift/…`), ya no se usan: se crean identidades nuevas y hay que emparejar de nuevo una vez.
 */
object ClaveIdentidad {
    /** Con los nombres serializados, que son estables: `ftms-wifi/smart-bike`. */
    fun de(modo: ModoConexion, identidad: Identidad): String = listOf(
        ModoConexion.serializer().descriptor.getElementName(modo.ordinal),
        Identidad.serializer().descriptor.getElementName(identidad.ordinal),
    ).joinToString("/")
}

/**
 * El nombre con el que la app se anuncia ante Zwift por DirCon (change `simular-kickr-bike`, decisión 3): «Smart Bike»,
 * o con [simularKickr], «KICKR BIKE PRO» y los cuatro últimos hexadecimales de la `mac` de la identidad, en mayúsculas.
 */
object NombreAnunciado {
    const val NORMAL = "Smart Bike"
    const val KICKR = "KICKR BIKE PRO"

    fun de(identidad: IdentidadDispositivo, simularKickr: Boolean): String = if (!simularKickr) NORMAL else "$KICKR ${sufijo(identidad.mac)}"

    private fun sufijo(mac: String): String {
        val hex = mac.filter { it in '0'..'9' || it in 'a'..'f' || it in 'A'..'F' }.uppercase()
        return hex.takeLast(4).padStart(4, '0')
    }
}

/**
 * El nombre Bluetooth que debe tener el móvil (spec `bluetooth-name`, «Renombrar el móvil mientras publica por
 * Bluetooth»): el nombre ante la app si el ajuste está activado y se publica algo por Bluetooth (el rodillo con «Solo
 * Bluetooth» o el mando aparte), y `null` en otro caso, en que el móvil conserva su nombre.
 */
object NombreBluetooth {
    fun deseado(config: Configuracion, servicios: ServiciosPublicados?, identidad: IdentidadDispositivo?): String? {
        if (!config.conexion.nombrePorBluetooth || servicios == null || identidad == null) return null
        val porBluetooth = servicios.modo == ModoConexion.FTMS_BLUETOOTH || servicios.mando == MandoPublicado.APARTE_BLUETOOTH
        return if (porBluetooth) NombreAnunciado.de(identidad, config.simularKickrBike) else null
    }
}

/** Qué hacer con el nombre del móvil al recuperarlo (design.md del change `nombre-bluetooth-propio`, decisión 4). */
sealed interface RecuperacionNombre {
    /** Poner [nombre], el que tenía el móvil antes de renombrarlo. */
    data class Restaurar(val nombre: String) : RecuperacionNombre

    /** No tocar el nombre: el usuario lo ha cambiado. */
    data object NoTocar : RecuperacionNombre

    companion object {
        /** Restaura si el móvil sigue llamándose [puesto]; si se llama de otra forma, lo cambió el usuario. */
        fun decidir(original: String, puesto: String, actual: String?): RecuperacionNombre = if (actual == puesto) Restaurar(original) else NoTocar
    }
}

/**
 * Lo que la app recuerda entre sesiones en `estado.json` (F5.6, F1.2). Una sola última marcha por transmisión, no una
 * por rodillo: la app es para un solo Neo (design.md del change `marchas-sin-zwift`, decisión 8). Un rodillo y un mando
 * configurados, uno de cada tipo (design.md del change `conexion-automatica-en-segundo-plano`, decisión 1).
 */
@Serializable
data class EstadoGuardado(
    val version: Int = VERSION,
    /** La última marcha de la lineal. */
    val ultimaMarcha: Int? = null,
    val rodillo: DispositivoConfigurado? = null,
    val mando: DispositivoConfigurado? = null,
    val notificacionesPedidas: Boolean = false,
    /**
     * Las identidades de dispositivo simulado, por [ClaveIdentidad] (plataforma, modo y nombre). Opcional y con valor por
     * defecto: los documentos anteriores, con sus cuatro identidades fijas, se leen ignorándolas y se crean nuevas.
     */
    val identidades: Map<String, IdentidadDispositivo> = emptyMap(),
    /** La última marcha de cada transmisión de platos, por su identificador; opcional como [identidades]. */
    val ultimasMarchas: Map<String, EstadoMarcha.PlatoPinon> = emptyMap(),
    /** La última potencia del ERG manual (spec `manual-erg`, «Potencia inicial recordada»); opcional como [identidades]. */
    val ultimaPotenciaErgW: Int? = null,
    /** El pulsómetro configurado (spec `device-connection`, «Configurar y reconectar el pulsómetro»); opcional como [identidades]. */
    val pulsometro: DispositivoConfigurado? = null,
    /** El ventilador configurado (spec `device-connection`, «Configurar y reconectar el ventilador»); opcional como [identidades]. */
    val ventilador: DispositivoConfigurado? = null,
    /**
     * El nombre Bluetooth del móvil antes de que la app lo renombrara y el que puso, mientras no se haya recuperado (spec
     * `bluetooth-name`, «Recuperar el nombre tras un cierre inesperado»); opcionales como [identidades]. Van juntos.
     */
    val nombreBluetoothOriginal: String? = null,
    val nombreBluetoothPuesto: String? = null,
    /**
     * Lo que recuerda la guía de inicio (spec `getting-started-guide`); `null` = aún no evaluada. Opcional como
     * [identidades], y no entra en la copia de la configuración: depende del móvil.
     */
    val guiaInicio: GuiaInicio? = null,
) {
    /** Si hay algún dispositivo configurado: el rodillo, el mando, el pulsómetro o el ventilador. */
    val hayDispositivos: Boolean get() = rodillo != null || mando != null || pulsometro != null || ventilador != null

    /** Con la guía aún sin evaluar la evalúa ([GuiaInicio.inicial]); si ya estaba evaluada, no cambia nada. */
    fun conGuiaEvaluada(): EstadoGuardado = if (guiaInicio != null) this else copy(guiaInicio = GuiaInicio.inicial(hayDispositivos))

    /** Con la que se entra en ERG manual: la última, dentro de los límites, o [POTENCIA_ERG_INICIAL_W]. */
    val potenciaErgManualW: Int get() = AjusteErg.limitar(ultimaPotenciaErgW ?: POTENCIA_ERG_INICIAL_W)

    /** La marcha con la que empieza [t]: la última si se recuerda y es válida, o la de referencia. */
    fun marchaInicial(c: Configuracion, t: Transmision = c.transmisionElegida): EstadoMarcha {
        val ultima = if (!c.recordarUltimaMarcha) {
            null
        } else {
            when (t) {
                is Transmision.Lineal -> ultimaMarcha?.takeIf { it in 1..t.marchas }?.let { t.marcha(it) }
                is Transmision.Platos -> ultimasMarchas[t.id]?.takeIf { t.contiene(it) }
            }
        }
        return ultima ?: c.referencia(t)
    }

    /** Guarda [m] como la última marcha de su transmisión. */
    fun conUltima(t: Transmision, m: EstadoMarcha): EstadoGuardado = when (m) {
        is EstadoMarcha.Numerada -> copy(ultimaMarcha = m.posicion)
        is EstadoMarcha.PlatoPinon -> copy(ultimasMarchas = ultimasMarchas + (t.id to m))
    }

    companion object {
        const val VERSION = 3

        /** La potencia del ERG manual la primera vez. */
        const val POTENCIA_ERG_INICIAL_W = 150

        /** Nombre con el que se recuerda el Neo de la versión 1, que solo guardaba su dirección. */
        const val NOMBRE_RODILLO_V1 = "Tacx Neo"

        private val json = Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }

        fun aJson(e: EstadoGuardado): String = json.encodeToString(serializer(), e)

        /**
         * Un documento ilegible se sustituye por los valores por defecto, y un campo que no se entiende, por el suyo
         * ([leerGuardado]). Uno de la versión 1 se migra:
         * `direccionRodillo` pasa a `rodillo` con el nombre [NOMBRE_RODILLO_V1].
         */
        fun deJson(texto: String): EstadoGuardado = leerGuardado(texto).valor

        /** Lo que [deJson], diciendo lo que se perdió, como `Configuracion.leerGuardada`. */
        fun leerGuardado(texto: String): Lectura<EstadoGuardado> = try {
            leerPorCampos(json.parseToJsonElement(texto).jsonObject) { objeto ->
                val version = objeto["version"]?.jsonPrimitive?.intOrNull ?: 1
                json.decodeFromJsonElement(serializer(), if (version < 2) migrarV1(objeto) else objeto)
            }
        } catch (_: Exception) {
            Lectura(EstadoGuardado(), ilegible = true)
        }

        private fun migrarV1(v1: JsonObject): JsonObject = buildJsonObject {
            v1.forEach { (clave, valor) -> if (clave != "direccionRodillo") put(clave, valor) }
            put("version", VERSION)
            val direccion = v1["direccionRodillo"]?.jsonPrimitive?.contentOrNull
            if (direccion != null) {
                put(
                    "rodillo",
                    buildJsonObject {
                        put("direccion", direccion)
                        put("nombre", NOMBRE_RODILLO_V1)
                    },
                )
            }
        }
    }
}

/** Por qué un archivo no se puede importar como configuración (spec `settings-backup`, «Archivos no válidos»). */
enum class MotivoImportacion { VACIO, NO_SE_PUDO_LEER, NO_ES_JSON, NO_ES_CONFIGURACION, VERSION_MAS_NUEVA }

sealed interface ResultadoImportacion {
    data class Valida(val configuracion: Configuracion) : ResultadoImportacion
    data class Invalida(val motivo: MotivoImportacion) : ResultadoImportacion
}
