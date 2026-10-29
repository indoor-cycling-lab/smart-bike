package app.smartbike.dominio

import app.smartbike.dominio.ComandoRodillo.Pendiente
import app.smartbike.dominio.ComandoRodillo.Potencia
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/** Escenarios de la spec `manual-erg` sobre `ControlRodillo` (design.md del change `erg-manual`, decisiones 1 y 2). */
class ControlRodilloErgManualTest {
    private val modelo = ModeloSimple(incrementoPct = 1.0, marchaNeutra = 12)
    private fun m(p: Int) = EstadoMarcha.Numerada(p, 24)
    private fun t(pct: Double) = Terreno(pct, crr = 0.004, cw = 0.51, vientoMs = 0.0)

    private fun conectado(marcha: Int = 14) = ControlRodillo(modelo, Terreno(0.0)).also { it.neoListo(m(marcha)) }

    @Test
    fun entrar_en_erg_manual() {
        val c = conectado()
        assertEquals(ResultadoAlternar.ErgManual(200, Potencia(200)), c.alternarErgManual(200, m(14)))
        assertEquals(ModoControl.ErgManual(200, 0, ModoControl.SimLibre), c.modo)
        assertEquals(200, c.ergManual?.potenciaW)
    }

    @Test
    fun volver_a_sim() {
        val c = conectado()
        c.alternarErgManual(200, m(14))
        assertEquals(ResultadoAlternar.Sim(Pendiente(2.0)), c.alternarErgManual(200, m(14)))
        assertEquals(ModoControl.SimLibre, c.modo)
        assertNull(c.ergManual)
    }

    @Test
    fun la_potencia_de_entrada_se_limita() {
        val c = conectado()
        assertEquals(ResultadoAlternar.ErgManual(1000, Potencia(1000)), c.alternarErgManual(1500, m(14)))
    }

    @Test
    fun ajuste_y_vuelta_a_la_entrada() {
        val c = conectado()
        c.alternarErgManual(200, m(14))
        val r = AjusteErg.aplicar(Accion.MarchaMas(1), 200, 0) as ResultadoAjuste.Cambiado
        assertEquals(Potencia(210), c.ajusteErg(r.ajusteW))
        assertEquals(Potencia(240), c.ajusteErg(40))
        assertEquals(Potencia(200), c.ajusteErg(0))
    }

    @Test
    fun la_marcha_no_se_envia_en_erg_manual() {
        val c = conectado()
        c.alternarErgManual(200, m(14))
        assertNull(c.alCambiarMarcha(m(9), 0))
        assertNull(c.cambiarBajadas(ModoBajadas.SIN_PROTECCION, m(9)))
        assertNull(c.cambiarModelo(ModeloSimple(2.0, 12), m(9)))
        assertEquals(Potencia(200), c.ultimoEnviado)
        // Al volver, la pendiente de la marcha que haya, con el modelo y el modo de bajadas nuevos.
        assertEquals(ResultadoAlternar.Sim(Pendiente(-6.0)), c.alternarErgManual(200, m(9)))
    }

    @Test
    fun alternar_en_erg_de_zwift_no_cambia_nada() {
        val c = conectado()
        c.potenciaPlataforma(230)
        assertEquals(ResultadoAlternar.PlataformaManda, c.alternarErgManual(200, m(14)))
        assertEquals(ModoControl.ErgPlataforma(230, 0), c.modo)
        assertEquals(Potencia(230), c.ultimoEnviado)
    }

    @Test
    fun subida_en_erg_manual_sobre_una_ruta() {
        val c = conectado()
        c.terrenoPlataforma(t(2.0), m(14), 0)
        c.alternarErgManual(200, m(14))
        assertNull(c.terrenoPlataforma(t(6.0), m(14), 1_000))
        assertEquals(Potencia(200), c.ultimoEnviado)
        assertNull(c.proximoEnvioMs())
        assertEquals(ModoControl.ErgManual(200, 0, ModoControl.SimPlataforma(t(6.0))), c.modo)
    }

    @Test
    fun vuelta_a_la_ruta() {
        val c = conectado()
        c.terrenoPlataforma(t(1.0), m(14), 0)
        c.alternarErgManual(200, m(14))
        c.terrenoPlataforma(t(3.0), m(14), 1_000)
        assertEquals(ResultadoAlternar.Sim(Pendiente(5.0)), c.alternarErgManual(200, m(14)))
        assertEquals(ModoControl.SimPlataforma(t(3.0)), c.modo)
    }

    @Test
    fun empieza_un_entrenamiento() {
        val c = conectado()
        c.terrenoPlataforma(t(1.0), m(14), 0)
        c.alternarErgManual(200, m(14))
        c.ajusteErg(30)
        c.potenciaPlataforma(230)
        assertEquals(Potencia(230), c.ultimoEnviado)
        assertEquals(ModoControl.ErgPlataforma(230, 0), c.modo)
        assertNull(c.ergManual)
        // Fin del entrenamiento: SIM con la marcha, no ERG manual.
        assertEquals(Pendiente(4.0), c.terrenoPlataforma(t(2.0), m(14), 5_000))
        assertEquals(ModoControl.SimPlataforma(t(2.0)), c.modo)
    }

    @Test
    fun zwift_se_desconecta_en_erg_manual() {
        val c = conectado()
        c.terrenoPlataforma(t(3.0), m(14), 0)
        c.alternarErgManual(200, m(14))
        c.plataformaPerdida()
        assertTrue(c.plataformaEnEspera)
        assertNull(c.plataformaAusente(m(14)))
        assertFalse(c.plataformaEnEspera)
        assertEquals(ModoControl.ErgManual(200, 0, ModoControl.SimLibre), c.modo)
        assertEquals(Potencia(200), c.ultimoEnviado)
        assertEquals(ResultadoAlternar.Sim(Pendiente(2.0)), c.alternarErgManual(200, m(14)))
    }

    @Test
    fun sin_zwift_no_hay_espera() {
        val c = conectado()
        c.alternarErgManual(200, m(14))
        c.plataformaPerdida()
        assertFalse(c.plataformaEnEspera)
    }

    @Test
    fun neo_que_vuelve() {
        val c = conectado()
        c.alternarErgManual(200, m(14))
        c.neoPerdido()
        assertNull(c.ajusteErg(20))
        assertEquals(Potencia(220), c.neoListo(m(14)))
    }

    @Test
    fun entrar_sin_neo() {
        val c = ControlRodillo(modelo, Terreno(0.0))
        assertEquals(ResultadoAlternar.ErgManual(200, null), c.alternarErgManual(200, m(14)))
        assertEquals(Potencia(200), c.neoListo(m(14)))
    }

    @Test
    fun control_manual_de_diagnostico_sobre_erg_manual() {
        val c = conectado()
        c.alternarErgManual(200, m(14))
        c.manual(Pendiente(3.0))
        assertEquals(200, c.ergManual?.potenciaW)
        // Alternar bajo el control manual cambia lo de debajo sin enviar.
        assertEquals(ResultadoAlternar.Sim(null), c.alternarErgManual(200, m(14)))
        assertEquals(Pendiente(3.0), c.ultimoEnviado)
        c.alternarErgManual(210, m(14))
        assertEquals(Potencia(210), c.salirManual(m(14)))
        assertEquals(ModoControl.ErgManual(210, 0, ModoControl.SimLibre), c.modo)
    }

    @Test
    fun sin_limite_es_nulo_en_erg_manual() {
        val c = conectado()
        c.alternarErgManual(200, m(14))
        assertNull(c.sinLimite(m(14)))
        assertEquals(Potencia(200), c.vigente(m(14)))
    }
}
