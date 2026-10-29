package app.smartbike.ui.botones

import app.smartbike.dominio.Accion
import app.smartbike.dominio.Asignacion
import app.smartbike.dominio.Entrada
import app.smartbike.dominio.FuncionZwift
import app.smartbike.dominio.Regla
import app.smartbike.dominio.TipoGesto
import app.smartbike.protocolo.SquareButton
import app.smartbike.sesion.EstadoCaptura
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/** Avisos de Asignar acción con el mando de Zwift (design.md del change `mando-zwift-emulado`, decisión 9). */
class AsignarAccionUiStateTest {
    private val a = Asignacion.POR_DEFECTO
    private val mas1 = Accion.MarchaMas(1)
    private val capturadaA = EstadoCaptura.Capturada(Entrada(SquareButton.A))

    @Test
    fun a_solo_avisa_de_que_deja_de_seleccionar() {
        // Spec `button-mapping`: sin interruptor del mando, no hay aviso de los botones que lee Zwift.
        val e = estadoDeAsignar(mas1, a, emptyList(), capturadaA, TipoGesto.Pulsar)
        val perdida = Regla(Entrada(SquareButton.A), TipoGesto.Pulsar, Accion.Mando(FuncionZwift.A))
        assertEquals(listOf(AvisoAsignar.EnUso(listOf(perdida))), e.avisos)
        // Al guardar, A sube una marcha y deja de llegar a Zwift.
        val guardada = asignacionAlGuardar(a, mas1, emptyList(), Entrada(SquareButton.A), TipoGesto.Pulsar)
        assertEquals(mas1, guardada.accion(Entrada(SquareButton.A), TipoGesto.Pulsar))
        assertEquals(emptyList<Regla>(), guardada.reglasDe(Accion.Mando(FuncionZwift.A)))
    }

    @Test
    fun funcion_de_zwift_en_una_combinacion_libre() {
        // Estado `funcion-zwift` de la maqueta.
        val rideOn = Accion.Mando(FuncionZwift.Z)
        val combinacion = Entrada(SquareButton.Triangulo, SquareButton.CampagnoloDerecho)
        val e = estadoDeAsignar(rideOn, a, emptyList(), EstadoCaptura.Capturada(combinacion), TipoGesto.Pulsar)
        assertEquals(listOf(Entrada(SquareButton.Z)), e.reglas.map { it.entrada })
        assertEquals(listOf(AvisoAsignar.Libre(combinacion, TipoGesto.Pulsar)), e.avisos)
    }
}
