package app.smartbike.ui.ajustes

import app.smartbike.datos.EstadoGrabacion
import app.smartbike.datos.ResumenSesiones
import app.smartbike.dominio.Configuracion
import app.smartbike.dominio.ConfiguracionVentilador
import app.smartbike.dominio.DatoRodaje
import app.smartbike.dominio.IdiomaApp
import app.smartbike.dominio.ModoBajadas
import app.smartbike.dominio.ModoCambio
import app.smartbike.dominio.ModoConexion
import app.smartbike.dominio.ModoVentilador
import app.smartbike.dominio.MotivoImportacion
import app.smartbike.dominio.OrientacionApp
import app.smartbike.dominio.TemaApp
import app.smartbike.dominio.TipoModelo
import app.smartbike.dominio.Transmision
import app.smartbike.plataforma.EstadoMando
import app.smartbike.sesion.EstadoDispositivo
import app.smartbike.sesion.EstadoRodaje

/**
 * Lo que dice «Copia de seguridad» (spec `settings-backup`, maquetas/ajustes.html): nada, la importación pendiente de
 * confirmar (con la configuración ya validada, sin aplicar), el resultado, o el error.
 */
sealed interface Copia {
    data object Ninguna : Copia
    data class Pendiente(val configuracion: Configuracion) : Copia
    data object Exportada : Copia
    data object Importada : Copia
    data class ErrorImportar(val motivo: MotivoImportacion) : Copia
    data object ErrorExportar : Copia
    data object ErrorSelector : Copia
}

/**
 * La sección «Ventilador» de Ajustes (maquetas/ajustes.html; spec `settings-screen`): solo con un ventilador configurado, con
 * lo último que comunicó. [umbrales] y [limites] son `null` mientras no se ha leído su configuración.
 */
data class VentiladorAjustesUi(
    val conectado: Boolean,
    val modo: ModoVentilador,
    val velocidad: Int,
    val umbrales: List<Int>?,
    val limites: List<IntRange>?,
) {
    val esManual get() = modo == ModoVentilador.Manual
    val puedeCambiarModo get() = conectado
    val puedeBajarVelocidad get() = conectado && esManual && velocidad > 0
    val puedeSubirVelocidad get() = conectado && esManual && velocidad < 100
    fun puedeBajarUmbral(zona: Int) = conectado && umbrales != null && limites != null && umbrales[zona - 1] > limites[zona - 1].first
    fun puedeSubirUmbral(zona: Int) = conectado && umbrales != null && limites != null && umbrales[zona - 1] < limites[zona - 1].last
}

fun ventiladorDeAjustes(e: EstadoRodaje): VentiladorAjustesUi? {
    if (e.ventiladorConfigurado == null) return null
    val estado = e.ventiladorEstado
    val config = estado?.configuracion
    return VentiladorAjustesUi(
        conectado = e.ventilador == EstadoDispositivo.Conectado,
        modo = estado?.modo ?: ModoVentilador.Otro,
        velocidad = (if (estado?.modo == ModoVentilador.Manual) estado.velocidad else estado?.velocidadManual ?: estado?.velocidad) ?: 0,
        umbrales = config?.umbrales,
        limites = config?.let { c -> List(ConfiguracionVentilador.ZONAS) { c.limites(it + 1) } },
    )
}

/**
 * Pantalla de Ajustes (maquetas/ajustes.html): las entradas Marchas, Botones y Diagnóstico, «Al rodar», «Pantalla»,
 * «Ciclista» y «Rodillo» (spec `settings-screen`). Lo que la app publica ante Zwift está en Plataformas.
 */
data class AjustesUiState(
    val bajadas: ModoBajadas = ModoBajadas.LA_MARCHA_NO_CREA_BAJADAS,
    val transmision: Transmision = Transmision.Lineal(),
    val modoCambio: ModoCambio = ModoCambio.AUTOMATICO,
    val datos: List<DatoRodaje> = Configuracion.DATOS_POR_DEFECTO,
    val botonesEnPantalla: Boolean = false,
    val modoTactil: Boolean = false,
    val mostrarAccion: Boolean = true,
    val vibracion: Boolean = true,
    val pesoCiclistaKg: Int = 75,
    /** La plataforma elegida: «Pendiente sin …» nombra aquello con lo que se rueda. */
    val idioma: IdiomaApp = IdiomaApp.ES,
    val tema: TemaApp = TemaApp.OSCURO,
    val orientacion: OrientacionApp = OrientacionApp.SEGUN_MOVIL,
    val modelo: TipoModelo = TipoModelo.SIMPLE,
    val dificultadPct: Int = 100,
    val potenciaMinimaW: Int = 30,
    val pesoBiciKg: Double = 8.0,
    val copia: Copia = Copia.Ninguna,
    /** El ventilador, `null` sin uno configurado: la sección no aparece. */
    val ventilador: VentiladorAjustesUi? = null,
    /** «Depuración»: grabar sesiones, su espacio libre mínimo, cómo va la grabación y lo grabado. */
    val grabarSesiones: Boolean = false,
    val espacioLibreMinimoPct: Int = 10,
    val grabacion: EstadoGrabacion = EstadoGrabacion.Apagada,
    val sesiones: ResumenSesiones = ResumenSesiones(0, 0),
) {
    val puedeBajarEspacioLibre get() = espacioLibreMinimoPct > Configuracion.ESPACIO_LIBRE_MINIMO_PCT.first
    val puedeSubirEspacioLibre get() = espacioLibreMinimoPct < Configuracion.ESPACIO_LIBRE_MINIMO_PCT.last

    /** «Bajadas» solo vale para las pendientes; el físico fija una potencia (spec `settings-screen`). */
    val muestraBajadas get() = modelo != TipoModelo.FISICO
    val muestraPotenciaMinima get() = modelo == TipoModelo.FISICO
    val muestraTabla get() = modelo == TipoModelo.TABLA
    val puedeBajarDificultad get() = dificultadPct > Configuracion.DIFICULTAD_PCT.first
    val puedeSubirDificultad get() = dificultadPct < Configuracion.DIFICULTAD_PCT.last
    val puedeBajarMinima get() = potenciaMinimaW > Configuracion.POTENCIA_MINIMA_W.first
    val puedeSubirMinima get() = potenciaMinimaW < Configuracion.POTENCIA_MINIMA_W.last
    val puedeBajarPesoBici get() = pesoBiciKg > Configuracion.PESO_BICI_KG.start + 1e-9
    val puedeSubirPesoBici get() = pesoBiciKg < Configuracion.PESO_BICI_KG.endInclusive - 1e-9

    val puedeBajarPeso get() = pesoCiclistaKg > Configuracion.PESO_KG.first
    val puedeSubirPeso get() = pesoCiclistaKg < Configuracion.PESO_KG.last
}

fun estadoDeAjustes(
    c: Configuracion,
    ventilador: VentiladorAjustesUi? = null,
    grabacion: EstadoGrabacion = EstadoGrabacion.Apagada,
    sesiones: ResumenSesiones = ResumenSesiones(0, 0),
) = AjustesUiState(
    bajadas = c.bajadas,
    transmision = c.transmisionElegida,
    modoCambio = c.modo,
    datos = c.datos,
    botonesEnPantalla = c.botonesEnPantalla,
    modoTactil = c.modoTactil,
    mostrarAccion = c.mostrarAccion,
    vibracion = c.vibracion,
    pesoCiclistaKg = c.pesoCiclistaKg,
    idioma = c.idioma ?: IdiomaApp.ES,
    tema = c.tema,
    orientacion = c.orientacion,
    modelo = c.modelo,
    dificultadPct = c.dificultadPct,
    potenciaMinimaW = c.potenciaMinimaW,
    pesoBiciKg = c.pesoBiciKg,
    ventilador = ventilador,
    grabarSesiones = c.grabarSesiones,
    espacioLibreMinimoPct = c.espacioLibreMinimoPct,
    grabacion = grabacion,
    sesiones = sesiones,
)

/** Un paso del espacio libre mínimo, de 5 en 5 y en sus límites. */
fun espacioLibreMinimoMas(pct: Int, pasos: Int): Int = (pct + pasos * Configuracion.PASO_ESPACIO_LIBRE_MINIMO_PCT).coerceIn(Configuracion.ESPACIO_LIBRE_MINIMO_PCT)

/** Un paso de la dificultad, en sus límites. */
fun dificultadMas(pct: Int, pasos: Int): Int = (pct + pasos * Configuracion.PASO_DIFICULTAD_PCT).coerceIn(Configuracion.DIFICULTAD_PCT)

fun potenciaMinimaMas(w: Int, pasos: Int): Int = (w + pasos * Configuracion.PASO_POTENCIA_MINIMA_W).coerceIn(Configuracion.POTENCIA_MINIMA_W)

/** Un paso del peso de la bici, de medio kilo, en sus límites. */
fun pesoBiciMas(kg: Double, pasos: Int): Double = (Math.round((kg + pasos * Configuracion.PASO_PESO_BICI_KG) * 2) / 2.0).coerceIn(Configuracion.PESO_BICI_KG)
