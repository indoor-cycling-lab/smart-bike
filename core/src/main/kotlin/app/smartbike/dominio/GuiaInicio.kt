package app.smartbike.dominio

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Lo que recuerda `estado.json` de la guía de inicio (spec `getting-started-guide`; design.md del change `guia-inicio`,
 * decisiones 5, 7 y 8). Los nombres serializados son estables. Ausente en el documento = aún no evaluada
 * ([EstadoGuardado.guiaInicio] `null`).
 */
@Serializable
enum class GuiaInicio {
    /** Se ve mientras falte algún paso con estado. */
    @SerialName("visible")
    VISIBLE,

    /** El usuario pulsó «No volver a mostrar». */
    @SerialName("descartada")
    DESCARTADA,

    /** Los pasos con estado se completaron a la vez, o la app se actualizó con algún dispositivo ya configurado. */
    @SerialName("finalizada")
    FINALIZADA,

    ;

    /** Si la guía puede verse en Rodaje (no está descartada ni terminada). */
    val activa: Boolean get() = this == VISIBLE

    companion object {
        /** La evaluación inicial: con algún dispositivo ya configurado (una actualización) no se enseña; sin ninguno, sí. */
        fun inicial(hayDispositivos: Boolean): GuiaInicio = if (hayDispositivos) FINALIZADA else VISIBLE
    }
}

/** Cómo está un dispositivo para la guía: no configurado, configurado y sin conexión, conectándose o conectado. */
enum class EnlaceGuia { NO_CONFIGURADO, SIN_CONEXION, CONECTANDO, CONECTADO }

/** El paso «Empareja tus dispositivos»: el rodillo y, aparte, el controlador. */
enum class PasoDispositivos {
    PENDIENTE,
    CONECTANDO,

    /** El rodillo está conectado y el controlador (opcional) no. */
    FALTA_CONTROLADOR,
    HECHO,
}

/** El paso «Conecta tu app de ciclismo indoor». */
enum class PasoApp {
    /** Sin rodillo configurado no se publica nada. */
    PENDIENTE,

    /** El rodillo se ofrece y falta emparejarlo en la app. */
    ESPERANDO,
    HECHO,
}

/** Lo que queda por hacer según la guía, para la tira minimizada. */
enum class SiguientePaso { DISPOSITIVOS, APP }

/**
 * Los pasos con estado de la página 2 de la guía (spec `getting-started-guide`, «Estado de los pasos»). «Ajusta la app» no
 * tiene estado y no cuenta. Se calcula solo de lo que la app ya sabe, sin lógica de conexión nueva.
 */
data class PasosGuia(val dispositivos: PasoDispositivos, val app: PasoApp) {
    /** Los dos pasos con estado están hechos a la vez: la guía termina. */
    val completa: Boolean get() = dispositivos == PasoDispositivos.HECHO && app == PasoApp.HECHO

    /** Lo siguiente por hacer, o `null` con todo hecho. */
    val siguiente: SiguientePaso?
        get() = when {
            dispositivos != PasoDispositivos.HECHO -> SiguientePaso.DISPOSITIVOS
            app != PasoApp.HECHO -> SiguientePaso.APP
            else -> null
        }

    companion object {
        /**
         * Los pasos con el estado del [rodillo], del [controlador] y de si hay una plataforma conectada
         * ([plataformaConectada]).
         */
        fun de(rodillo: EnlaceGuia, controlador: EnlaceGuia, plataformaConectada: Boolean): PasosGuia {
            val dispositivos = when (rodillo) {
                EnlaceGuia.NO_CONFIGURADO, EnlaceGuia.SIN_CONEXION -> PasoDispositivos.PENDIENTE

                EnlaceGuia.CONECTANDO -> PasoDispositivos.CONECTANDO

                EnlaceGuia.CONECTADO ->
                    if (controlador == EnlaceGuia.CONECTADO) PasoDispositivos.HECHO else PasoDispositivos.FALTA_CONTROLADOR
            }
            val app = when {
                plataformaConectada -> PasoApp.HECHO
                rodillo == EnlaceGuia.NO_CONFIGURADO -> PasoApp.PENDIENTE
                else -> PasoApp.ESPERANDO
            }
            return PasosGuia(dispositivos, app)
        }
    }
}

/**
 * Lo que recuerda la guía de inicio solo durante la ejecución (design.md del change `guia-inicio`, decisiones 3, 5 y 6): la
 * página, si la hoja está abierta o minimizada, si se cerró con la equis y si se reabrió desde Ajustes. Se pierde al
 * cerrar la app, y por eso la equis oculta la guía «hasta que se vuelva a abrir la app».
 */
data class GuiaPantalla(
    val pagina: Int = PRIMERA,
    val abierta: Boolean = false,
    val cerrada: Boolean = false,
    /** Reabierta desde Ajustes: con todo hecho se mantiene a la vista, sin finalizarse, durante la ejecución. */
    val reabierta: Boolean = false,
) {
    fun irA(p: Int): GuiaPantalla = copy(pagina = p.coerceIn(PRIMERA, ULTIMA))

    fun siguiente(): GuiaPantalla = irA(pagina + 1)

    fun anterior(): GuiaPantalla = irA(pagina - 1)

    /** La tira reabre la hoja en la página en la que estaba. */
    fun abrir(): GuiaPantalla = copy(abierta = true)

    fun minimizar(): GuiaPantalla = copy(abierta = false)

    /** La equis: oculta la guía hasta que se vuelva a abrir la app. */
    fun cerrar(): GuiaPantalla = copy(abierta = false, cerrada = true)

    /** «Guía de inicio» de Ajustes: la hoja abierta en la página 1, también si se había cerrado. */
    fun reabrir(): GuiaPantalla = GuiaPantalla(pagina = PRIMERA, abierta = true, reabierta = true)

    /**
     * Si la guía se ve en Rodaje: guardada como [GuiaInicio.VISIBLE], sin cerrar con la equis y, con los pasos [completa]s,
     * solo si se reabrió desde Ajustes.
     */
    fun visible(guardada: GuiaInicio?, completa: Boolean): Boolean = guardada == GuiaInicio.VISIBLE && !cerrada && (!completa || reabierta)

    /** Si hay que guardar [GuiaInicio.FINALIZADA]: los pasos están [completa]s y la guía no se reabrió en esta ejecución. */
    fun finaliza(guardada: GuiaInicio?, completa: Boolean): Boolean = guardada == GuiaInicio.VISIBLE && completa && !reabierta

    companion object {
        const val PRIMERA = 1
        const val ULTIMA = 4

        /** Al abrir la app: la hoja se abre sola solo mientras no haya ningún dispositivo configurado (primer uso). */
        fun alArrancar(hayDispositivos: Boolean): GuiaPantalla = GuiaPantalla(abierta = !hayDispositivos)
    }
}
