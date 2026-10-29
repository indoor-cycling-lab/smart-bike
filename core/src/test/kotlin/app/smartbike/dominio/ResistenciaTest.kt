package app.smartbike.dominio

import app.smartbike.dominio.ComandoRodillo.Pendiente
import app.smartbike.dominio.ComandoRodillo.Potencia
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ResistenciaTest {

    private val modelo = ModeloSimple(incrementoPct = 1.0, marchaNeutra = 12)
    private val llano = Terreno(0.0)
    private fun m(p: Int) = EstadoMarcha.Numerada(p, 24)

    @Test
    fun modelo_simple_por_marcha() {
        assertEquals(Pendiente(0.0), modelo.comando(llano, 0.0, m(12)))
        assertEquals(Pendiente(5.0), modelo.comando(llano, 0.0, m(17)))
        assertEquals(Pendiente(-11.0), modelo.comando(llano, 0.0, m(1)))
        assertEquals(Pendiente(12.0), modelo.comando(llano, 0.0, m(24)))
    }

    @Test
    fun modelo_simple_suma_el_terreno() {
        assertEquals(Pendiente(6.0), modelo.comando(Terreno(4.0), 0.0, m(14)))
    }

    // Con la Shimano 105, proporcional a la relación (design.md del change `transmisiones-de-marca`, decisión 5)

    private val shimano = ModeloSimple(incrementoPct = 1.0, marchaNeutra = 12, transmision = Desarrollos.SHIMANO_105)
    private fun s(plato: Int, pinon: Int) = EstadoMarcha.PlatoPinon(plato, pinon)

    @Test
    fun pendiente_por_relacion() {
        mapOf(
            s(36, 36) to -11.50,
            s(36, 19) to -2.04,
            s(36, 17) to -0.39,
            s(52, 24) to -0.05,
            s(52, 19) to 3.41,
            s(52, 17) to 5.05,
            s(52, 11) to 11.50,
        ).forEach { (m, pct) -> assertEquals(pct, shimano.ajuste(m), 0.005, "$m") }
    }

    @Test
    fun la_misma_relacion_da_la_misma_pendiente() {
        assertEquals(shimano.ajuste(s(36, 18)), shimano.ajuste(s(52, 26)), 0.05)
        assertTrue(kotlin.math.abs(shimano.ajuste(s(36, 19)) - shimano.ajuste(s(52, 28))) < 0.5)
    }

    @Test
    fun suma_el_terreno() {
        // Spec `trainer-resistance`, «Ruta de Zwift»: 4,0 % y la 52×17, unos +9,1 %.
        assertEquals(9.05, (shimano.comando(Terreno(4.0), 0.0, s(52, 17)) as Pendiente).pct, 0.005)
    }

    @Test
    fun el_cambio_de_plato_automatico_salta_como_un_pinon() {
        val salto = shimano.ajuste(s(52, 19)) - shimano.ajuste(s(36, 15))
        assertEquals(1.95, salto, 0.01)
    }

    @Test
    fun cambiar_de_modelo_envia_la_pendiente_nueva() {
        val c = ControlRodillo(modelo, llano, ModoBajadas.SIN_PROTECCION)
        c.neoListo(m(17))
        val nuevo = c.cambiarModelo(shimano, s(52, 17)) as Pendiente
        assertEquals(5.05, nuevo.pct, 0.005)
        // La misma pendiente no se reenvía.
        assertNull(c.cambiarModelo(shimano, s(52, 17)))
    }

    private fun control() = ControlRodillo(modelo, llano)

    @Test
    fun sin_neo_no_se_envia_nada() {
        val c = control()
        assertNull(c.alCambiarMarcha(m(13)))
        assertNull(c.manual(Potencia(200)))
        assertNull(c.terminar())
    }

    @Test
    fun al_conectar_se_envia_el_vigente_y_cada_cambio_al_instante() {
        val c = control()
        assertEquals(Pendiente(2.0), c.neoListo(m(14)))
        assertEquals(Pendiente(3.0), c.alCambiarMarcha(m(15)))
        assertEquals(Pendiente(3.0), c.ultimoEnviado)
    }

    @Test
    fun no_reenvia_el_mismo_comando() {
        val c = control()
        c.neoListo(m(12))
        assertNull(c.alCambiarMarcha(m(12)))
    }

    @Test
    fun manual_no_envia_con_cambios_de_marcha_y_al_salir_vuelve_a_la_marcha() {
        val c = control()
        c.neoListo(m(14))
        assertEquals(Potencia(200), c.manual(Potencia(200)))
        assertNull(c.alCambiarMarcha(m(15)))
        assertEquals(Potencia(200), c.vigente(m(15)))
        assertEquals(Pendiente(3.0), c.salirManual(m(15)))
        assertEquals(ModoControl.SimLibre, c.modo)
        assertNull(c.salirManual(m(15)))
    }

    @Test
    fun manual_repetido_se_envia_otra_vez() {
        val c = control()
        c.neoListo(m(12))
        c.manual(Potencia(200))
        assertEquals(Potencia(200), c.manual(Potencia(200)))
    }

    @Test
    fun perder_el_neo_cancela_el_manual_y_al_volver_manda_la_marcha() {
        val c = control()
        c.neoListo(m(14))
        c.manual(Pendiente(3.0))
        c.neoPerdido()
        assertEquals(ModoControl.SimLibre, c.modo)
        assertNull(c.alCambiarMarcha(m(17)))
        assertEquals(Pendiente(5.0), c.neoListo(m(17)))
    }

    @Test
    fun al_reconectar_se_reenvia_aunque_sea_igual() {
        val c = control()
        c.neoListo(m(17))
        c.neoPerdido()
        assertEquals(Pendiente(5.0), c.neoListo(m(17)))
    }

    @Test
    fun terminar_deja_el_neo_en_llano() {
        val c = control()
        c.neoListo(m(20))
        assertEquals(Pendiente(0.0), c.terminar())
    }
}
