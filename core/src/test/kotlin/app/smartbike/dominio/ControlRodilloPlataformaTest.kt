package app.smartbike.dominio

import app.smartbike.dominio.ComandoRodillo.Pendiente
import app.smartbike.dominio.ComandoRodillo.Potencia
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/** Escenarios de las specs `zwift-bridge` y `trainer-manual-control` sobre `ControlRodillo`. */
class ControlRodilloPlataformaTest {
    private val modelo = ModeloSimple(incrementoPct = 1.0, marchaNeutra = 12)
    private fun m(p: Int) = EstadoMarcha.Numerada(p, 24)
    private fun t(pct: Double) = Terreno(pct, crr = 0.004, cw = 0.51, vientoMs = 0.0)

    private fun conectado(marcha: Int = 12) = ControlRodillo(modelo, Terreno(0.0)).also { it.neoListo(m(marcha)) }

    // --- Pendiente de Zwift con la marcha ---

    @Test
    fun subida_en_marcha_neutra() {
        val c = conectado()
        assertEquals(Pendiente(4.0), c.terrenoPlataforma(t(4.0), m(12), 0))
        assertEquals(ModoControl.SimPlataforma(t(4.0)), c.modo)
    }

    @Test
    fun subida_en_marcha_dura_se_envia_al_cambiar_sin_esperar_a_zwift() {
        val c = conectado()
        c.terrenoPlataforma(t(4.0), m(12), 0)
        assertEquals(Pendiente(5.0), c.alCambiarMarcha(m(13), 10))
        assertEquals(Pendiente(6.0), c.alCambiarMarcha(m(14), 20))
    }

    @Test
    fun una_pendiente_fuera_del_rango_de_fec_se_recorta() {
        // FTMS admite hasta ±327 %; FE-C, ±200 %. Sin recorte, `FecCodec` lanzaría una excepción y cerraría la app.
        val c = conectado()
        assertEquals(Pendiente(200.0), c.terrenoPlataforma(t(327.0), m(12), 0))
        assertEquals(Pendiente(-200.0), c.terrenoPlataforma(t(-327.0), m(12), 1_000))
    }

    @Test
    fun una_pendiente_no_numerica_se_envia_como_llano() {
        assertEquals(Pendiente(0.0), ComandoRodillo.Pendiente(Double.NaN).limitado())
    }

    @Test
    fun una_potencia_fuera_del_rango_de_fec_se_recorta() {
        assertEquals(Potencia(4000), Potencia(6000).limitado())
        assertEquals(Potencia(0), Potencia(-5).limitado())
        assertEquals(Potencia(250), Potencia(250).limitado())
    }

    @Test
    fun bajada_la_marcha_no_la_hace_mas_pronunciada() {
        val c = conectado(10)
        assertEquals(Pendiente(-3.0), c.terrenoPlataforma(t(-3.0), m(10), 0))
    }

    @Test
    fun bajada_sin_proteccion() {
        val c = ControlRodillo(modelo, Terreno(0.0), ModoBajadas.SIN_PROTECCION).also { it.neoListo(m(10)) }
        assertEquals(Pendiente(-5.0), c.terrenoPlataforma(t(-3.0), m(10), 0))
    }

    @Test
    fun bajada_sin_empuje() {
        val c = ControlRodillo(modelo, Terreno(0.0), ModoBajadas.NUNCA_EMPUJAR).also { it.neoListo(m(10)) }
        assertNull(c.terrenoPlataforma(t(-3.0), m(10), 0))
        assertEquals(Pendiente(0.0), c.ultimoEnviado)
    }

    @Test
    fun bajada_larga_sin_empuje_no_reenvia_nada() {
        val c = ControlRodillo(modelo, Terreno(0.0), ModoBajadas.NUNCA_EMPUJAR).also { it.neoListo(m(12)) }
        c.terrenoPlataforma(t(-2.0), m(12), 0)
        assertNull(c.terrenoPlataforma(t(-5.0), m(12), 1_000))
        assertNull(c.terrenoPlataforma(t(-7.0), m(12), 2_000))
        assertNull(c.proximoEnvioMs())
        assertEquals(Pendiente(2.0), c.alCambiarMarcha(m(21), 3_000))
    }

    @Test
    fun terreno_muy_seguido_como_mucho_cada_250_ms_y_siempre_el_ultimo() {
        val c = conectado()
        assertEquals(Pendiente(4.0), c.terrenoPlataforma(t(4.0), m(12), 1_000))
        assertNull(c.terrenoPlataforma(t(4.5), m(12), 1_100))
        assertNull(c.terrenoPlataforma(t(4.8), m(12), 1_200))
        assertEquals(1_250L, c.proximoEnvioMs())
        assertEquals(Pendiente(4.8), c.enviarPendiente(m(12), 1_250))
        assertNull(c.proximoEnvioMs())
        assertEquals(Pendiente(5.2), c.terrenoPlataforma(t(5.2), m(12), 1_600))
    }

    @Test
    fun terreno_que_apenas_cambia_no_se_envia() {
        val c = conectado()
        c.terrenoPlataforma(t(4.0), m(12), 0)
        assertNull(c.terrenoPlataforma(t(4.05), m(12), 1_000))
        assertNull(c.proximoEnvioMs())
        assertEquals(Pendiente(4.1), c.terrenoPlataforma(t(4.1), m(12), 2_000))
    }

    @Test
    fun un_cambio_de_marcha_lleva_el_terreno_pendiente_y_reinicia_la_ventana() {
        val c = conectado()
        c.terrenoPlataforma(t(4.0), m(12), 0)
        assertNull(c.terrenoPlataforma(t(5.0), m(12), 100))
        assertEquals(Pendiente(6.0), c.alCambiarMarcha(m(13), 150))
        assertNull(c.proximoEnvioMs())
        assertNull(c.terrenoPlataforma(t(5.5), m(13), 300))
        assertEquals(400L, c.proximoEnvioMs())
    }

    // --- ERG de Zwift ---

    @Test
    fun bloque_de_230_w_y_la_marcha_no_se_envia() {
        val c = conectado(14)
        assertEquals(Potencia(230), c.potenciaPlataforma(230))
        assertNull(c.alCambiarMarcha(m(15), 0))
        assertEquals(Potencia(280), c.potenciaPlataforma(280))
    }

    @Test
    fun potencia_que_apenas_cambia_no_se_envia() {
        val c = conectado()
        c.potenciaPlataforma(230)
        assertNull(c.potenciaPlataforma(231))
        assertEquals(Potencia(232), c.potenciaPlataforma(232))
    }

    @Test
    fun el_ajuste_se_mantiene_entre_bloques_y_se_pierde_al_volver_a_sim() {
        val c = conectado(14)
        c.potenciaPlataforma(230)
        assertEquals(Potencia(240), c.ajusteErg(10))
        assertEquals(Potencia(290), c.potenciaPlataforma(280))
        assertEquals(Pendiente(4.0), c.terrenoPlataforma(t(2.0), m(14), 5_000))
        c.potenciaPlataforma(230)
        assertEquals(ModoControl.ErgPlataforma(230, 0), c.modo)
    }

    @Test
    fun la_potencia_enviada_queda_entre_50_y_1000() {
        val c = conectado()
        assertEquals(Potencia(50), c.potenciaPlataforma(30))
        assertEquals(Potencia(1000), c.potenciaPlataforma(1500))
    }

    // --- Zwift desconectado ---

    @Test
    fun corte_breve_mantiene_el_terreno_y_sigue_al_volver() {
        val c = conectado()
        c.terrenoPlataforma(t(5.0), m(12), 0)
        c.plataformaPerdida()
        assertTrue(c.plataformaEnEspera)
        assertEquals(Pendiente(6.0), c.terrenoPlataforma(t(6.0), m(12), 10_000))
        assertFalse(c.plataformaEnEspera)
        assertNull(c.plataformaAusente(m(12)))
    }

    @Test
    fun zwift_cerrado_vuelve_a_la_pendiente_base_con_la_marcha() {
        val c = conectado(14)
        c.terrenoPlataforma(t(5.0), m(14), 0)
        c.plataformaPerdida()
        assertEquals(Pendiente(2.0), c.plataformaAusente(m(14)))
        assertEquals(ModoControl.SimLibre, c.modo)
        assertEquals(Pendiente(3.0), c.alCambiarMarcha(m(15), 40_000))
    }

    @Test
    fun zwift_cerrado_en_un_entrenamiento() {
        val c = conectado(12)
        c.potenciaPlataforma(250)
        c.plataformaPerdida()
        assertEquals(Potencia(250), c.vigente(m(12)))
        assertEquals(Pendiente(0.0), c.plataformaAusente(m(12)))
    }

    @Test
    fun sin_zwift_perderlo_no_pone_en_espera() {
        val c = conectado()
        c.plataformaPerdida()
        assertFalse(c.plataformaEnEspera)
    }

    // --- Neo ---

    @Test
    fun sin_neo_se_guarda_lo_de_zwift_y_se_envia_al_volver() {
        val c = ControlRodillo(modelo, Terreno(0.0))
        assertNull(c.terrenoPlataforma(t(4.0), m(14), 0))
        assertEquals(Pendiente(6.0), c.neoListo(m(14)))
        c.neoPerdido()
        assertNull(c.potenciaPlataforma(230))
        assertEquals(Potencia(230), c.neoListo(m(14)))
    }

    // --- Control manual con Zwift ---

    @Test
    fun envio_manual_rodando_en_zwift_guarda_el_terreno_sin_enviarlo() {
        val c = conectado(13)
        c.terrenoPlataforma(t(4.0), m(13), 0)
        assertEquals(Pendiente(3.0), c.manual(Pendiente(3.0)))
        assertNull(c.terrenoPlataforma(t(6.0), m(13), 1_000))
        assertEquals(ModoControl.Manual(Pendiente(3.0), ModoControl.SimPlataforma(t(6.0))), c.modo)
        assertEquals(Pendiente(7.0), c.salirManual(m(13)))
    }

    @Test
    fun volver_a_rodaje_en_un_entrenamiento() {
        val c = conectado()
        c.potenciaPlataforma(230)
        c.ajusteErg(10)
        c.manual(Potencia(150))
        assertNull(c.ajusteErg(20))
        assertEquals(Potencia(240), c.salirManual(m(12)))
    }

    @Test
    fun perder_el_neo_con_manual_vuelve_a_zwift() {
        val c = conectado(14)
        c.terrenoPlataforma(t(4.0), m(14), 0)
        c.manual(Potencia(200))
        c.neoPerdido()
        assertEquals(ModoControl.SimPlataforma(t(4.0)), c.modo)
        assertEquals(Pendiente(6.0), c.neoListo(m(14)))
    }

    @Test
    fun zwift_ausente_durante_el_manual_deja_la_marcha_para_la_salida() {
        val c = conectado(14)
        c.terrenoPlataforma(t(5.0), m(14), 0)
        c.manual(Potencia(200))
        c.plataformaPerdida()
        assertNull(c.plataformaAusente(m(14)))
        assertEquals(Pendiente(2.0), c.salirManual(m(14)))
    }
}
