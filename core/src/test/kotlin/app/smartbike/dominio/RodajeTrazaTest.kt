package app.smartbike.dominio

import app.smartbike.protocolo.CalculoCadencia
import app.smartbike.protocolo.SquareDecoder
import app.smartbike.protocolo.TelemetriaCodec
import app.smartbike.trazas.Traza
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Reproduce un rodaje real con el Square y el Neo (`rodaje-marchas.txt`) por el camino de los botones a la marcha, y
 * comprueba la secuencia de marchas anotada en su cabecera, topes incluidos (la marcha se repite).
 */
class RodajeTrazaTest {

    private val recurso = checkNotNull(javaClass.getResource("/trazas/rodaje-marchas.txt")).readText()
    private val traza = Traza.leer("rodaje-marchas.txt")

    private val esperadas = recurso.lineSequence().first { it.startsWith("# marchas:") }
        .removePrefix("# marchas:").trim().split(" ").map { it.toInt() }

    @Test
    fun secuencia_de_marchas() {
        val eventos = EventosBoton()
        val reconocedor = ReconocedorGestos()
        val cambio = Cambio(Transmision.Lineal(24))
        val asignacion = Asignacion.POR_DEFECTO
        var marcha = cambio.marcha(12)
        val marchas = mutableListOf(marcha.posicion)
        val referencia = cambio.marcha(12)
        for (l in traza.filter { it.origen == "SQUARE" && it.sentido == "RX" && it.caracteristica == "0x45" }) {
            val contadores = SquareDecoder.contadores(l.datos) ?: continue
            for (e in eventos.procesar(contadores, l.tMs)) {
                for (g in reconocedor.procesar(e)) {
                    val accion = asignacion.accion(g) ?: continue
                    when (val r = cambio.aplicar(marcha, accion, referencia)) {
                        is ResultadoCambio.Cambiada -> marcha = r.nueva as EstadoMarcha.Numerada
                        is ResultadoCambio.Tope -> Unit
                        ResultadoCambio.SinCambio, is ResultadoCambio.ReferenciaFijada -> continue
                    }
                    marchas += marcha.posicion
                }
            }
        }
        assertEquals(esperadas, marchas)
    }

    @Test
    fun la_app_registro_las_mismas_marchas() {
        // Lo que anotó la Sesion en el móvil coincide con lo que da el dominio en la JVM.
        val registradas = traza.filter { it.sentido == "EVT" && it.texto.startsWith("Gesto:") }.mapNotNull {
            Regex("""nueva=EstadoMarcha\(posicion=(\d+)""").find(it.texto)?.groupValues?.get(1)?.toInt()
                ?: if ("Tope" in it.texto) -1 else null
        }
        val sinTopes = esperadas.drop(1)
        assertEquals(sinTopes.size, registradas.size)
        registradas.zip(sinTopes).forEach { (r, e) -> if (r != -1) assertEquals(e, r) }
    }

    @Test
    fun telemetria_sin_pedalear() {
        // En esta traza no se pedaleó: la cadencia por biela y la potencia son 0, como dice la página FE-C 0x19.
        val calculo = CalculoCadencia()
        val cadencias = traza.filter { it.origen == "NEO" && it.caracteristica == "2a5b" }
            .mapNotNull { l -> TelemetriaCodec.biela(l.datos)?.let { calculo.biela(it, l.tMs) } }
        val potencias = traza.filter { it.origen == "NEO" && it.caracteristica == "2a63" }.mapNotNull { TelemetriaCodec.potencia(it.datos) }
        val fec19 = traza.filter { it.origen == "NEO" && it.caracteristica == "fec2" && it.datos.size == 13 && it.datos[4].toInt() == 0x19 }
        assertTrue(cadencias.size > 50 && potencias.size > 50 && fec19.isNotEmpty())
        assertTrue(cadencias.all { it == 0 } && potencias.all { it == 0 })
        assertTrue(fec19.all { it.datos[6].toInt() == 0 })
    }
}
