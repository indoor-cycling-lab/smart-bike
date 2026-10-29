package app.smartbike.sesion

import app.smartbike.R
import app.smartbike.dominio.EstadoMarcha
import app.smartbike.dominio.ModoControl
import app.smartbike.plataforma.EstadoPlataforma
import app.smartbike.sesion.EstadoDispositivo.Buscando
import app.smartbike.sesion.EstadoDispositivo.Conectado
import app.smartbike.sesion.EstadoDispositivo.Conectando
import app.smartbike.sesion.EstadoDispositivo.NoEncontrado
import app.smartbike.sesion.EstadoDispositivo.Reconectando
import app.smartbike.sesion.EstadoDispositivo.SinConexion
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/** Texto de conexiones de la notificación (design.md del change `conexion-automatica-en-segundo-plano`, decisión 6). */
class TextoConexionesTest {

    private fun texto(neo: EstadoDispositivo, square: EstadoDispositivo) = RideService.textoConexiones(
        EstadoRodaje(neo = neo, square = square, marcha = EstadoMarcha.Numerada(14, 24), referencia = EstadoMarcha.Numerada(12, 24), simulacion = Simulacion(0.0, 2.0, null)),
    )

    @Test
    fun cada_combinacion_de_estados() {
        val intento = listOf(Buscando, Conectando, Reconectando)
        val sinIntento = listOf(SinConexion, NoEncontrado)
        assertEquals(R.string.notif_todo_conectado, texto(Conectado, Conectado))
        for (a in intento) {
            assertEquals(R.string.notif_conectando_neo, texto(a, Conectado), "$a")
            assertEquals(R.string.notif_conectando_square, texto(Conectado, a), "$a")
            for (b in intento) assertEquals(R.string.notif_conectando_los_dos, texto(a, b), "$a $b")
            for (b in sinIntento) {
                assertEquals(R.string.notif_conectando_neo, texto(a, b), "$a $b")
                assertEquals(R.string.notif_conectando_square, texto(b, a), "$b $a")
            }
        }
        for (a in sinIntento) {
            assertEquals(R.string.notif_sin_neo, texto(a, Conectado), "$a")
            assertEquals(R.string.notif_sin_square, texto(Conectado, a), "$a")
            for (b in sinIntento) assertEquals(R.string.notif_sin_ninguno, texto(a, b), "$a $b")
        }
    }

    private fun conZwift(neo: EstadoDispositivo, square: EstadoDispositivo, zwift: EstadoPlataforma, modo: ModoControl = ModoControl.SimLibre) = EstadoRodaje(
        neo = neo,
        square = square,
        plataforma = zwift,
        marcha = EstadoMarcha.Numerada(14, 24),
        referencia = EstadoMarcha.Numerada(12, 24),
        simulacion = Simulacion(0.0, 2.0, null, modo = modo),
    )

    @Test
    fun rodando_en_zwift() {
        assertEquals(R.string.notif_todo_conectado_plataforma, RideService.textoConexiones(conZwift(Conectado, Conectado, EstadoPlataforma.Conectado("192.168.0.10")).copy(usoRodillo = true)))
        assertEquals(R.string.notif_todo_conectado, RideService.textoConexiones(conZwift(Conectado, Conectado, EstadoPlataforma.Esperando)))
        assertEquals(R.string.notif_todo_conectado, RideService.textoConexiones(conZwift(Conectado, Conectado, EstadoPlataforma.SinWifi)))
        assertEquals(R.string.notif_conectando_square, RideService.textoConexiones(conZwift(Conectado, Reconectando, EstadoPlataforma.Conectado("x"))))
    }

    @Test
    fun titulo_en_erg_de_zwift() {
        assertEquals(240, RideService.potenciaErg(conZwift(Conectado, Conectado, EstadoPlataforma.Conectado("x"), ModoControl.ErgPlataforma(230, 10))))
        assertEquals(null, RideService.potenciaErg(conZwift(Conectado, Conectado, EstadoPlataforma.Conectado("x"))))
    }

    @Test
    fun titulo_en_erg_manual() {
        // Spec `ride-session`, «ERG manual»: «ERG 200 W · Neo y Square conectados».
        val e = conZwift(Conectado, Conectado, EstadoPlataforma.Esperando, ModoControl.ErgManual(200, 0, ModoControl.SimLibre))
        assertEquals(200, RideService.potenciaErg(e))
        assertEquals(R.string.notif_todo_conectado, RideService.textoConexiones(e))
    }
}
