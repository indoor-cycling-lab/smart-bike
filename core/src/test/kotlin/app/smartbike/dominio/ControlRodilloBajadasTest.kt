package app.smartbike.dominio

import app.smartbike.dominio.ComandoRodillo.Pendiente
import app.smartbike.dominio.ComandoRodillo.Potencia
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

/** Escenarios de las specs `trainer-resistance` («Protección en bajadas») y `settings-screen` sobre `ControlRodillo`. */
class ControlRodilloBajadasTest {
    private val modelo = ModeloSimple(incrementoPct = 1.0, marchaNeutra = 12)
    private fun m(p: Int) = EstadoMarcha.Numerada(p, 24)

    private fun conectado(marcha: Int, modo: ModoBajadas = ModoBajadas.LA_MARCHA_NO_CREA_BAJADAS) = ControlRodillo(modelo, Terreno(0.0), modo).also { it.neoListo(m(marcha)) }

    @Test
    fun extremos_por_defecto() {
        assertEquals(Pendiente(0.0), conectado(1).ultimoEnviado)
        assertEquals(Pendiente(12.0), conectado(24).ultimoEnviado)
    }

    @Test
    fun extremos_sin_proteccion() {
        assertEquals(Pendiente(-11.0), conectado(1, ModoBajadas.SIN_PROTECCION).ultimoEnviado)
        assertEquals(Pendiente(12.0), conectado(24, ModoBajadas.SIN_PROTECCION).ultimoEnviado)
    }

    @Test
    fun marchas_que_dan_la_misma_pendiente_no_envian_nada() {
        val c = conectado(3)
        assertNull(c.alCambiarMarcha(m(4)))
        assertEquals(Pendiente(1.0), c.alCambiarMarcha(m(13)))
    }

    @Test
    fun control_manual_sin_limite() {
        val c = conectado(12, ModoBajadas.NUNCA_EMPUJAR)
        assertEquals(Pendiente(-5.0), c.manual(Pendiente(-5.0)))
        assertNull(c.sinLimite(m(12)))
    }

    @Test
    fun erg_sin_limite() {
        val c = conectado(1, ModoBajadas.NUNCA_EMPUJAR)
        assertEquals(Potencia(230), c.potenciaPlataforma(230))
        assertNull(c.cambiarBajadas(ModoBajadas.SIN_PROTECCION, m(1)))
        assertNull(c.sinLimite(m(1)))
    }

    @Test
    fun cambiar_de_modo_rodando() {
        val c = conectado(5, ModoBajadas.SIN_PROTECCION)
        assertEquals(Pendiente(-7.0), c.ultimoEnviado)
        assertEquals(Pendiente(0.0), c.cambiarBajadas(ModoBajadas.LA_MARCHA_NO_CREA_BAJADAS, m(5)))
        assertNull(c.cambiarBajadas(ModoBajadas.NUNCA_EMPUJAR, m(5)))
        assertEquals(ModoBajadas.NUNCA_EMPUJAR, c.bajadas)
    }

    @Test
    fun cambiar_de_modo_sin_neo_se_aplica_al_conectar() {
        val c = ControlRodillo(modelo, Terreno(0.0), ModoBajadas.SIN_PROTECCION)
        assertNull(c.cambiarBajadas(ModoBajadas.NUNCA_EMPUJAR, m(5)))
        assertEquals(Pendiente(0.0), c.neoListo(m(5)))
    }

    @Test
    fun pendiente_sin_limite_para_diagnostico() {
        val c = conectado(1)
        c.terrenoPlataforma(Terreno(-7.0), m(1), 0)
        assertEquals(Pendiente(-7.0), c.ultimoEnviado)
        assertEquals(-18.0, c.sinLimite(m(1)))
    }
}
