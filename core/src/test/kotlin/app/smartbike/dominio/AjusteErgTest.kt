package app.smartbike.dominio

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/** Escenarios de «Palancas en ERG de Zwift» (spec `zwift-bridge`) y del aviso recortado (spec `ride-screen`). */
class AjusteErgTest {

    @Test
    fun subir_10_w() {
        assertEquals(ResultadoAjuste.Cambiado(ajusteW = 10, diferenciaW = 10), AjusteErg.aplicar(Accion.MarchaMas(1), 230, 0))
    }

    @Test
    fun salto_con_modificador() {
        assertEquals(ResultadoAjuste.Cambiado(ajusteW = -30, diferenciaW = -30), AjusteErg.aplicar(Accion.MarchaMenos(3), 230, 0))
    }

    @Test
    fun quitar_el_ajuste() {
        assertEquals(ResultadoAjuste.Quitado, AjusteErg.aplicar(Accion.MarchaReferencia, 230, 20))
        assertEquals(ResultadoAjuste.SinCambio, AjusteErg.aplicar(Accion.MarchaReferencia, 230, 0))
    }

    @Test
    fun potencia_minima() {
        val primera = AjusteErg.aplicar(Accion.MarchaMenos(1), 60, 0)
        assertEquals(ResultadoAjuste.Cambiado(ajusteW = -10, diferenciaW = -10), primera)
        assertEquals(ResultadoAjuste.Tope(50, maximo = false), AjusteErg.aplicar(Accion.MarchaMenos(1), 60, -10))
    }

    @Test
    fun salto_recortado_por_el_limite() {
        assertEquals(ResultadoAjuste.Cambiado(ajusteW = -10, diferenciaW = -10), AjusteErg.aplicar(Accion.MarchaMenos(3), 60, 0))
    }

    @Test
    fun potencia_maxima() {
        assertEquals(ResultadoAjuste.Cambiado(ajusteW = 10, diferenciaW = 10), AjusteErg.aplicar(Accion.MarchaMas(3), 990, 0))
        assertEquals(ResultadoAjuste.Tope(1000, maximo = true), AjusteErg.aplicar(Accion.MarchaMas(1), 1500, 0))
    }

    @Test
    fun marcha_mas_dura_o_mas_ligera_no_hace_nada() {
        assertEquals(ResultadoAjuste.SinCambio, AjusteErg.aplicar(Accion.MarchaMasDura, 230, 0))
        assertEquals(ResultadoAjuste.SinCambio, AjusteErg.aplicar(Accion.MarchaMasLigera, 230, 0))
    }

    @Test
    fun el_plato_no_ajusta_la_potencia() {
        // Spec `zwift-bridge`, «Plato en ERG».
        assertEquals(ResultadoAjuste.SinCambio, AjusteErg.aplicar(Accion.PlatoMas, 230, 0))
        assertEquals(ResultadoAjuste.SinCambio, AjusteErg.aplicar(Accion.PlatoMenos, 230, 20))
    }

    @Test
    fun mas_y_menos_10_w() {
        // Spec `manual-erg`, «Ajuste de ±10 W», y `zwift-bridge`, «Botón asignado a −10 W en ERG».
        assertEquals(ResultadoAjuste.Cambiado(ajusteW = 10, diferenciaW = 10), AjusteErg.aplicar(Accion.ErgMas, 200, 0))
        assertEquals(ResultadoAjuste.Cambiado(ajusteW = -10, diferenciaW = -10), AjusteErg.aplicar(Accion.ErgMenos, 230, 0))
        assertEquals(ResultadoAjuste.Tope(1000, maximo = true), AjusteErg.aplicar(Accion.ErgMas, 200, 800))
        assertEquals(ResultadoAjuste.Cambiado(ajusteW = 20, diferenciaW = 10), AjusteErg.aplicar(Accion.ErgMas, 980, 10))
    }

    @Test
    fun alternar_no_ajusta() {
        assertEquals(ResultadoAjuste.SinCambio, AjusteErg.aplicar(Accion.AlternarErgManual, 200, 20))
    }

    @Test
    fun vuelta_a_la_potencia_de_entrada() {
        // Spec `manual-erg`, «Volver a la potencia de entrada»: entrada 200 W, ajuste +40 W.
        assertEquals(ResultadoAjuste.Quitado, AjusteErg.aplicar(Accion.MarchaReferencia, 200, 40))
    }
}
