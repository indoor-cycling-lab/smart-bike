package app.smartbike.dominio

import app.smartbike.dominio.TipoGesto.Corta
import app.smartbike.dominio.TipoGesto.Doble
import app.smartbike.dominio.TipoGesto.Pulsar
import app.smartbike.dominio.TipoGesto.Repetir
import app.smartbike.protocolo.SquareButton
import app.smartbike.protocolo.SquareDecoder
import app.smartbike.trazas.Traza
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Reproduce `square-combinaciones.txt` (Square real, change `gestos-y-asignacion`) por el reconocedor con la asignación
 * por defecto y △ con corta y doble, hasta el apagado: cada notificación también vence los temporizadores, como el de
 * la `Sesion`. Las horas son relativas a la primera notificación.
 */
class CombinacionesTrazaTest {

    private val gestos: List<Gesto> by lazy {
        val asignacion = Asignacion.POR_DEFECTO
            .asignar(Entrada(SquareButton.Triangulo), Corta, Accion.MarchaMas(1))
            .asignar(Entrada(SquareButton.Triangulo), Doble, Accion.MarchaMenos(1))
        val reconocedor = ReconocedorGestos(asignacion)
        val eventos = EventosBoton()
        val salida = mutableListOf<Gesto>()
        var t0 = 0L
        for (l in Traza.leer("square-combinaciones.txt").filter { it.origen == "SQUARE" && it.caracteristica == "0x45" }) {
            val c = SquareDecoder.contadores(l.datos) ?: continue
            if (t0 == 0L) t0 = l.tMs
            val t = l.tMs - t0
            // El apagado (los cuatro botones a la vez) lo resuelve la Sesion: la reproducción para ahí.
            if (t >= 117_000) break
            salida += reconocedor.vencer(t)
            for (e in eventos.procesar(c, t)) salida += reconocedor.procesar(e)
        }
        salida
    }

    private fun entre(desde: Long, hasta: Long) = gestos.filter { it.tMs in desde..hasta }

    @Test
    fun saltos_con_el_campagnolo_de_su_lado() {
        assertEquals(listOf(Gesto(Entrada(SquareButton.PalancaDerecha1, SquareButton.CampagnoloDerecho), Pulsar, 70_972)), entre(69_000, 75_000))
        assertEquals(listOf(Gesto(Entrada(SquareButton.PalancaIzquierda1, SquareButton.CampagnoloIzquierdo), Pulsar, 77_059)), entre(75_000, 80_000))
    }

    @Test
    fun freno_mas_palanca_no_es_combinacion() {
        val g = entre(86_000, 90_000)
        assertEquals(Gesto(Entrada(SquareButton.FrenoDerecho), Pulsar, 86_414), g[0])
        assertEquals(Gesto(Entrada(SquareButton.PalancaDerecha1), Pulsar, 87_354), g[1])
        // La palanca se mantuvo 1,5 s: repite a los 800 ms y cada 250 ms hasta soltarla.
        assertEquals(listOf(88_154L, 88_404L, 88_654L), g.drop(2).map { it.tMs })
        assertTrue(g.drop(2).all { it.tipo == Repetir })
    }

    @Test
    fun los_dos_campagnolo_no_hacen_nada() {
        assertEquals(emptyList<Gesto>(), entre(95_000, 100_000))
    }

    @Test
    fun palanca_mantenida_tres_segundos() {
        // De 102 747 a 105 393 ms: pulsar y 8 repeticiones.
        val g = entre(102_000, 106_000)
        assertEquals(Pulsar, g.first().tipo)
        assertEquals(8, g.count { it.tipo == Repetir })
    }

    @Test
    fun doble_rapida_en_el_triangulo() {
        // La suelta y la segunda pulsación llegaron en la misma notificación: es doble, sin corta.
        assertEquals(listOf(Gesto(Entrada(SquareButton.Triangulo), Doble, 109_662)), entre(109_000, 116_000))
    }
}
