package app.smartbike.dominio

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

/** Spec `fan-control`; design.md del change `ventilador-headwind`, decisiones 2 y 3. */
class VentiladorTest {
    private fun cfg(n: Int) = ConfiguracionVentilador.desde(ByteArray(n) { i -> (if (i < 4) 120 + 15 * i else 7 + i).toByte() })!!

    @Test
    fun cambiar_un_umbral_deja_el_resto_de_bytes_igual_en_8_9_y_18() {
        for (n in listOf(8, 9, 18)) {
            val c = cfg(n)
            val nueva = c.conUmbral(2, 138)
            assertEquals(listOf(120, 138, 150, 165), nueva.umbrales)
            val antes = c.bytes
            val despues = nueva.bytes
            for (i in antes.indices) if (i != 1) assertEquals(antes[i], despues[i])
            assertEquals(n, despues.size)
        }
    }

    @Test
    fun una_configuracion_corta_se_rechaza() {
        assertNull(ConfiguracionVentilador.desde(ByteArray(7)))
        assertNotNull(ConfiguracionVentilador.desde(ByteArray(8)))
    }

    @Test
    fun los_umbrales_son_estrictamente_crecientes() {
        val c = cfg(9) // 120, 135, 150, 165
        assertEquals(151, c.conUmbral(4, 150).umbrales[3])
        assertEquals(134, c.conUmbral(1, 200).umbrales[0])
        assertEquals(121, c.conUmbral(2, 100).umbrales[1])
        assertEquals(149, c.conUmbral(2, 149).umbrales[1])
    }

    @Test
    fun los_extremos_son_60_y_220() {
        val c = cfg(9)
        assertEquals(60, c.conUmbral(1, 10).umbrales[0])
        assertEquals(220, c.conUmbral(4, 500).umbrales[3])
        assertEquals(60..134, c.limites(1))
        assertEquals(151..220, c.limites(4))
    }

    @Test
    fun la_copia_no_comparte_bytes() {
        val c = cfg(9)
        c.bytes[0] = 1
        assertEquals(120, c.umbrales[0])
    }

    private fun manual(v: Int) = EstadoVentilador(ModoVentilador.Manual, v, velocidadManual = v)
    private val auto = EstadoVentilador(ModoVentilador.Pulso, 25, velocidadManual = 40)
    private val otro = EstadoVentilador(ModoVentilador.Otro, 0, velocidadManual = 40)

    @Test
    fun subir_de_10_en_10_y_con_extremos() {
        assertEquals(ResultadoVentilador(OrdenVentilador.Velocidad(50), AvisoVentilador.Velocidad(50)), ReglasVentilador.masVelocidad(manual(40), null))
        assertEquals(ResultadoVentilador(OrdenVentilador.Velocidad(100), AvisoVentilador.Velocidad(100)), ReglasVentilador.masVelocidad(manual(100), null))
        assertEquals(ResultadoVentilador(OrdenVentilador.Velocidad(0), AvisoVentilador.Velocidad(0)), ReglasVentilador.menosVelocidad(manual(0), null))
        assertEquals(ResultadoVentilador(OrdenVentilador.Velocidad(30), AvisoVentilador.Velocidad(30)), ReglasVentilador.menosVelocidad(manual(40), null))
    }

    @Test
    fun tres_pasos_seguidos_desde_20_acaban_en_50_sin_notificacion() {
        var objetivo: Int? = null
        repeat(3) {
            val r = ReglasVentilador.masVelocidad(manual(20), objetivo)
            objetivo = (r.orden as OrdenVentilador.Velocidad).porcentaje
        }
        assertEquals(50, objetivo)
    }

    @Test
    fun en_automatico_la_velocidad_no_cambia() {
        assertEquals(ResultadoVentilador(null, AvisoVentilador.EnAutomatico), ReglasVentilador.masVelocidad(auto, null))
        assertEquals(ResultadoVentilador(null, AvisoVentilador.EnAutomatico), ReglasVentilador.menosVelocidad(auto, null))
    }

    @Test
    fun en_otro_modo_la_velocidad_no_hace_nada() {
        assertEquals(ResultadoVentilador(null, null), ReglasVentilador.masVelocidad(otro, null))
    }

    @Test
    fun alternar_desde_cada_modo() {
        assertEquals(ResultadoVentilador(OrdenVentilador.Modo(ModoVentilador.Pulso), AvisoVentilador.Automatico), ReglasVentilador.alternar(manual(40)))
        assertEquals(ResultadoVentilador(OrdenVentilador.Modo(ModoVentilador.Manual), AvisoVentilador.Manual(40)), ReglasVentilador.alternar(auto))
        assertEquals(ResultadoVentilador(OrdenVentilador.Modo(ModoVentilador.Manual), AvisoVentilador.Manual(40)), ReglasVentilador.alternar(otro))
    }

    @Test
    fun sin_ventilador_no_se_manda_nada_y_se_avisa() {
        val sin = ResultadoVentilador(null, AvisoVentilador.SinVentilador)
        assertEquals(sin, ReglasVentilador.alternar(null))
        assertEquals(sin, ReglasVentilador.masVelocidad(null, 30))
        assertEquals(sin, ReglasVentilador.menosVelocidad(null, null))
    }

    @Test
    fun el_modo_sale_del_codigo() {
        assertEquals(ModoVentilador.Manual, ModoVentilador.deCodigo(4))
        assertEquals(ModoVentilador.Pulso, ModoVentilador.deCodigo(2))
        assertEquals(ModoVentilador.Otro, ModoVentilador.deCodigo(1))
        assertEquals(ModoVentilador.Otro, ModoVentilador.deCodigo(5))
    }
}
