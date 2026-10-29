package app.smartbike.dominio

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

/** «Configurado = se publica» (spec `platform-bridge`, «Lo publicado según los dispositivos configurados»). */
class PublicacionTest {
    private val todo = Configurados(rodillo = true, mando = true, pulsometro = true)

    @Test
    fun sin_rodillo_configurado_no_se_publica_nada() {
        ModoConexion.entries.forEach { assertNull(Publicacion.de(it, todo.copy(rodillo = false)), it.name) }
    }

    @Test
    fun rodillo_solo_sin_mando_ni_pulso() {
        listOf(ModoConexion.FTMS_WIFI, ModoConexion.FTMS_BLUETOOTH).forEach {
            assertEquals(ServiciosPublicados(it, mando = null, pulso = false), Publicacion.de(it, Configurados(rodillo = true)), it.name)
        }
    }

    @Test
    fun el_mando_va_aparte_con_wifi_mas_bluetooth_y_dentro_en_los_otros_modos() {
        assertEquals(MandoPublicado.APARTE_BLUETOOTH, Publicacion.de(ModoConexion.FTMS_WIFI, todo)?.mando)
        assertEquals(MandoPublicado.DENTRO, Publicacion.de(ModoConexion.FTMS_BLUETOOTH, todo)?.mando)
        assertEquals(MandoPublicado.DENTRO, Publicacion.de(ModoConexion.ZWIFT_RIDE, todo)?.mando)
    }

    @Test
    fun sin_square_configurado_no_se_publica_el_mando_salvo_en_zwift_ride() {
        assertNull(Publicacion.de(ModoConexion.FTMS_WIFI, todo.copy(mando = false))?.mando)
        assertNull(Publicacion.de(ModoConexion.FTMS_BLUETOOTH, todo.copy(mando = false))?.mando)
        // En Zwift Ride el terreno y el ERG llegan por el servicio del mando: va siempre (V3).
        assertEquals(MandoPublicado.DENTRO, Publicacion.de(ModoConexion.ZWIFT_RIDE, todo.copy(mando = false))?.mando)
    }

    @Test
    fun el_pulso_se_publica_solo_con_pulsometro_configurado() {
        ModoConexion.entries.forEach {
            assertEquals(true, Publicacion.de(it, todo)?.pulso, it.name)
            assertEquals(false, Publicacion.de(it, todo.copy(pulsometro = false))?.pulso, it.name)
        }
    }
}
