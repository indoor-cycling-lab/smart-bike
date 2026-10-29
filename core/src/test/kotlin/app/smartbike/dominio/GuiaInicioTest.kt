package app.smartbike.dominio

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/** Los pasos de la guía de inicio y lo que recuerda `estado.json` (spec `getting-started-guide`). */
class GuiaInicioTest {
    private val neo = DispositivoConfigurado("AA:BB:CC:DD:EE:01", "Tacx Neo 1")
    private val square = DispositivoConfigurado("AA:BB:CC:DD:EE:02", "Elite Square")

    private fun pasos(
        rodillo: EnlaceGuia = EnlaceGuia.NO_CONFIGURADO,
        controlador: EnlaceGuia = EnlaceGuia.NO_CONFIGURADO,
        plataforma: Boolean = false,
    ) = PasosGuia.de(rodillo, controlador, plataforma)

    @Test
    fun primer_uso_todo_pendiente() {
        val p = pasos()
        assertEquals(PasoDispositivos.PENDIENTE, p.dispositivos)
        assertEquals(PasoApp.PENDIENTE, p.app)
        assertEquals(SiguientePaso.DISPOSITIVOS, p.siguiente)
        assertFalse(p.completa)
    }

    @Test
    fun rodillo_configurado_y_sin_conexion_sigue_pendiente_pero_la_app_espera() {
        val p = pasos(rodillo = EnlaceGuia.SIN_CONEXION)
        assertEquals(PasoDispositivos.PENDIENTE, p.dispositivos)
        assertEquals(PasoApp.ESPERANDO, p.app)
    }

    @Test
    fun un_rodillo_dormido_al_abrir_dice_conectando_y_no_pendiente() {
        val p = pasos(rodillo = EnlaceGuia.CONECTANDO)
        assertEquals(PasoDispositivos.CONECTANDO, p.dispositivos)
        assertEquals(PasoApp.ESPERANDO, p.app)
    }

    @Test
    fun rodillo_conectado_sin_controlador_dice_que_falta_el_controlador() {
        listOf(EnlaceGuia.NO_CONFIGURADO, EnlaceGuia.SIN_CONEXION, EnlaceGuia.CONECTANDO).forEach {
            assertEquals(PasoDispositivos.FALTA_CONTROLADOR, pasos(EnlaceGuia.CONECTADO, it).dispositivos, it.name)
        }
        assertEquals(SiguientePaso.DISPOSITIVOS, pasos(EnlaceGuia.CONECTADO).siguiente)
    }

    @Test
    fun rodillo_y_controlador_conectados_hacen_el_primer_paso_y_lo_siguiente_es_la_app() {
        val p = pasos(EnlaceGuia.CONECTADO, EnlaceGuia.CONECTADO)
        assertEquals(PasoDispositivos.HECHO, p.dispositivos)
        assertEquals(PasoApp.ESPERANDO, p.app)
        assertEquals(SiguientePaso.APP, p.siguiente)
        assertFalse(p.completa)
    }

    @Test
    fun con_los_dos_pasos_hechos_la_guia_esta_completa() {
        val p = pasos(EnlaceGuia.CONECTADO, EnlaceGuia.CONECTADO, plataforma = true)
        assertEquals(PasoApp.HECHO, p.app)
        assertTrue(p.completa)
        assertNull(p.siguiente)
    }

    @Test
    fun la_plataforma_conectada_hace_el_paso_de_la_app_aunque_falten_los_dispositivos() {
        val p = pasos(EnlaceGuia.CONECTADO, plataforma = true)
        assertEquals(PasoApp.HECHO, p.app)
        assertEquals(SiguientePaso.DISPOSITIVOS, p.siguiente)
        assertFalse(p.completa)
    }

    @Test
    fun un_estado_perdido_vuelve_a_pendiente_o_conectando() {
        val hecho = pasos(EnlaceGuia.CONECTADO, EnlaceGuia.CONECTADO, plataforma = true)
        assertTrue(hecho.completa)
        val sinNeo = pasos(EnlaceGuia.SIN_CONEXION, EnlaceGuia.CONECTADO, plataforma = true)
        assertEquals(PasoDispositivos.PENDIENTE, sinNeo.dispositivos)
        val reconectando = pasos(EnlaceGuia.CONECTANDO, EnlaceGuia.CONECTADO, plataforma = true)
        assertEquals(PasoDispositivos.CONECTANDO, reconectando.dispositivos)
        assertFalse(reconectando.completa)
    }

    @Test
    fun solo_la_guia_visible_esta_activa() {
        assertTrue(GuiaInicio.VISIBLE.activa)
        assertFalse(GuiaInicio.DESCARTADA.activa)
        assertFalse(GuiaInicio.FINALIZADA.activa)
    }

    @Test
    fun sin_dispositivos_la_evaluacion_inicial_deja_la_guia_visible() {
        assertEquals(GuiaInicio.VISIBLE, EstadoGuardado().conGuiaEvaluada().guiaInicio)
    }

    @Test
    fun con_algun_dispositivo_configurado_la_actualizacion_no_la_enseña() {
        listOf(
            EstadoGuardado(rodillo = neo),
            EstadoGuardado(mando = square),
            EstadoGuardado(pulsometro = neo),
            EstadoGuardado(ventilador = neo),
        ).forEach { assertEquals(GuiaInicio.FINALIZADA, it.conGuiaEvaluada().guiaInicio, it.toString()) }
    }

    @Test
    fun una_guia_ya_evaluada_no_se_reevalua() {
        assertEquals(GuiaInicio.DESCARTADA, EstadoGuardado(guiaInicio = GuiaInicio.DESCARTADA).conGuiaEvaluada().guiaInicio)
        assertEquals(GuiaInicio.VISIBLE, EstadoGuardado(rodillo = neo, guiaInicio = GuiaInicio.VISIBLE).conGuiaEvaluada().guiaInicio)
    }

    @Test
    fun un_documento_sin_el_campo_se_lee_igual_y_sin_evaluar() {
        val sin = """{"version":3,"ultimaMarcha":9}"""
        assertEquals(EstadoGuardado(ultimaMarcha = 9), EstadoGuardado.deJson(sin))
        assertNull(EstadoGuardado.deJson(sin).guiaInicio)
    }

    @Test
    fun el_campo_se_guarda_y_se_lee_con_nombres_estables() {
        GuiaInicio.entries.forEach {
            val e = EstadoGuardado(guiaInicio = it)
            assertEquals(e, EstadoGuardado.deJson(EstadoGuardado.aJson(e)), it.name)
        }
        assertTrue(EstadoGuardado.aJson(EstadoGuardado(guiaInicio = GuiaInicio.DESCARTADA)).contains("\"guiaInicio\":\"descartada\""))
    }

    @Test
    fun la_configuracion_exportada_no_contiene_el_estado_de_la_guia() {
        assertFalse(Configuracion.aJson(Configuracion()).contains("guiaInicio"))
    }

    @Test
    fun un_valor_que_no_se_entiende_se_sustituye_por_el_suyo_y_no_pierde_lo_demas() {
        val roto = EstadoGuardado.leerGuardado("""{"version":3,"ultimaMarcha":9,"guiaInicio":"rara"}""")
        assertNull(roto.valor.guiaInicio)
        assertEquals(9, roto.valor.ultimaMarcha)
    }
}

/** Lo que la guía recuerda solo durante la ejecución (spec `getting-started-guide`, «Minimizar la guía» y «Cuándo se ve la guía»). */
class GuiaPantallaTest {
    @Test
    fun se_abre_sola_solo_sin_dispositivos_configurados() {
        assertTrue(GuiaPantalla.alArrancar(hayDispositivos = false).abierta)
        assertFalse(GuiaPantalla.alArrancar(hayDispositivos = true).abierta)
        assertEquals(1, GuiaPantalla.alArrancar(hayDispositivos = false).pagina)
    }

    @Test
    fun avanza_y_retrocede_sin_salirse_de_las_cuatro_paginas() {
        var g = GuiaPantalla()
        g = g.anterior()
        assertEquals(1, g.pagina)
        g = g.siguiente().siguiente().siguiente()
        assertEquals(4, g.pagina)
        g = g.siguiente()
        assertEquals(4, g.pagina)
        assertEquals(3, g.anterior().pagina)
        assertEquals(1, g.irA(0).pagina)
        assertEquals(4, g.irA(9).pagina)
    }

    @Test
    fun minimizar_y_reabrir_con_la_tira_conserva_la_pagina() {
        val g = GuiaPantalla(abierta = true).irA(2).minimizar()
        assertFalse(g.abierta)
        assertEquals(2, g.pagina)
        assertEquals(2, g.abrir().pagina)
        assertTrue(g.abrir().abierta)
    }

    @Test
    fun la_equis_cierra_la_guia_y_la_oculta_aunque_falte_algo() {
        val g = GuiaPantalla(abierta = true).cerrar()
        assertFalse(g.abierta)
        assertTrue(g.cerrada)
        assertFalse(g.visible(GuiaInicio.VISIBLE, completa = false))
    }

    @Test
    fun solo_se_ve_la_visible_con_algo_por_hacer() {
        val g = GuiaPantalla()
        assertTrue(g.visible(GuiaInicio.VISIBLE, completa = false))
        assertFalse(g.visible(GuiaInicio.DESCARTADA, completa = false))
        assertFalse(g.visible(GuiaInicio.FINALIZADA, completa = false))
        assertFalse(g.visible(null, completa = false))
        assertFalse(g.visible(GuiaInicio.VISIBLE, completa = true))
    }

    @Test
    fun con_todo_hecho_finaliza_salvo_si_se_reabrio_en_esta_ejecucion() {
        assertTrue(GuiaPantalla().finaliza(GuiaInicio.VISIBLE, completa = true))
        assertFalse(GuiaPantalla().finaliza(GuiaInicio.VISIBLE, completa = false))
        assertFalse(GuiaPantalla().finaliza(GuiaInicio.DESCARTADA, completa = true))
        val reabierta = GuiaPantalla().reabrir()
        assertFalse(reabierta.finaliza(GuiaInicio.VISIBLE, completa = true))
        assertTrue(reabierta.visible(GuiaInicio.VISIBLE, completa = true))
    }

    @Test
    fun reabrir_abre_la_hoja_en_la_pagina_1_aunque_se_hubiera_cerrado() {
        val g = GuiaPantalla(abierta = false, cerrada = true).irA(4).reabrir()
        assertEquals(1, g.pagina)
        assertTrue(g.abierta)
        assertFalse(g.cerrada)
        assertTrue(g.reabierta)
    }
}
