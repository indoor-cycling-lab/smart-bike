package app.smartbike.dominio

import app.smartbike.dominio.FuncionZwift.A
import app.smartbike.dominio.FuncionZwift.DERECHA
import app.smartbike.dominio.FuncionZwift.FRENO_DERECHO
import app.smartbike.dominio.FuncionZwift.FRENO_IZQUIERDO
import app.smartbike.dominio.FuncionZwift.GIRAR_DERECHA
import app.smartbike.dominio.FuncionZwift.GIRAR_IZQUIERDA
import app.smartbike.dominio.FuncionZwift.Z
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/** Vectores de la trama del mando (design.md del change `mando-zwift-emulado`, decisión 2; arquitectura.md § 7.4). */
class EstadoMandoZwiftTest {

    private fun hex(b: ByteArray) = b.joinToString(" ") { "%02x".format(it) }

    private val reposo = EstadoMandoZwift()

    /** Las dos palancas a 0: `1a 04 08 <ubicación> 10 00`. */
    private val palancasA0 = "1a 04 08 00 10 00 1a 04 08 01 10 00"

    @Test
    fun reposo_con_las_dos_palancas_a_0() {
        assertEquals("23 08 ff ff ff ff 0f $palancasA0", hex(reposo.trama()))
        assertTrue(reposo.pulsadas.isEmpty())
    }

    @Test
    fun cada_boton_solo() {
        // `ENUM_MANUAL`: ← 0x1, ↑ 0x2, → 0x4, ↓ 0x8, A 0x10, B 0x20, Y 0x40, Z 0x80; el mapa va invertido.
        val esperados = mapOf(
            FuncionZwift.IZQUIERDA to "fe ff ff ff 0f",
            FuncionZwift.ARRIBA to "fd ff ff ff 0f",
            FuncionZwift.DERECHA to "fb ff ff ff 0f",
            FuncionZwift.ABAJO to "f7 ff ff ff 0f",
            FuncionZwift.A to "ef ff ff ff 0f",
            FuncionZwift.B to "df ff ff ff 0f",
            FuncionZwift.Y to "bf ff ff ff 0f",
            FuncionZwift.Z to "ff fe ff ff 0f",
        )
        esperados.forEach { (f, mapa) -> assertEquals("23 08 $mapa $palancasA0", hex(reposo.pulsar(f).trama()), "$f") }
    }

    @Test
    fun giros_y_frenos_solos() {
        // −100 en zigzag es c7 01; +100, c8 01.
        assertEquals("23 08 ff ff ff ff 0f 1a 05 08 00 10 c7 01 1a 04 08 01 10 00", hex(reposo.pulsar(GIRAR_IZQUIERDA).trama()))
        assertEquals("23 08 ff ff ff ff 0f 1a 05 08 00 10 c8 01 1a 04 08 01 10 00", hex(reposo.pulsar(FRENO_IZQUIERDO).trama()))
        assertEquals("23 08 ff ff ff ff 0f 1a 04 08 00 10 00 1a 05 08 01 10 c8 01", hex(reposo.pulsar(GIRAR_DERECHA).trama()))
        assertEquals("23 08 ff ff ff ff 0f 1a 04 08 00 10 00 1a 05 08 01 10 c7 01", hex(reposo.pulsar(FRENO_DERECHO).trama()))
    }

    @Test
    fun dos_frenos() {
        val e = reposo.pulsar(FRENO_IZQUIERDO).pulsar(FRENO_DERECHO)
        assertEquals("23 08 ff ff ff ff 0f 1a 05 08 00 10 c8 01 1a 05 08 01 10 c7 01", hex(e.trama()))
    }

    @Test
    fun con_giro_y_freno_en_la_misma_palanca_gana_el_freno() {
        assertEquals(hex(reposo.pulsar(FRENO_IZQUIERDO).trama()), hex(reposo.pulsar(GIRAR_IZQUIERDA).pulsar(FRENO_IZQUIERDO).trama()))
        assertEquals(hex(reposo.pulsar(FRENO_DERECHO).trama()), hex(reposo.pulsar(FRENO_DERECHO).pulsar(GIRAR_DERECHA).trama()))
        // Al soltar el freno, vuelve el giro que sigue mantenido.
        val girando = reposo.pulsar(GIRAR_IZQUIERDA).pulsar(FRENO_IZQUIERDO).soltar(FRENO_IZQUIERDO)
        assertEquals(hex(reposo.pulsar(GIRAR_IZQUIERDA).trama()), hex(girando.trama()))
    }

    @Test
    fun la_misma_funcion_mantenida_por_dos_entradas() {
        val dos = reposo.pulsar(Z).pulsar(Z)
        assertEquals(setOf(Z), dos.soltar(Z).pulsadas)
        assertEquals(reposo, dos.soltar(Z).soltar(Z))
    }

    @Test
    fun soltar_en_distinto_orden() {
        val e = reposo.pulsar(A).pulsar(DERECHA).pulsar(FRENO_DERECHO)
        assertEquals(setOf(A, DERECHA, FRENO_DERECHO), e.pulsadas)
        assertEquals(hex(reposo.pulsar(DERECHA).pulsar(FRENO_DERECHO).trama()), hex(e.soltar(A).trama()))
        assertEquals(reposo, e.soltar(DERECHA).soltar(A).soltar(FRENO_DERECHO))
        assertEquals(reposo, e.soltar(FRENO_DERECHO).soltar(A).soltar(DERECHA))
    }

    @Test
    fun soltar_lo_que_no_esta_pulsado_no_cambia_nada() {
        assertEquals(reposo, reposo.soltar(A))
        assertEquals(setOf(Z), reposo.pulsar(Z).soltar(A).pulsadas)
    }

    @Test
    fun soltar_todo() {
        val e = reposo.pulsar(A).pulsar(A).pulsar(FRENO_IZQUIERDO).pulsar(GIRAR_DERECHA)
        assertEquals(reposo, e.soltarTodo())
        assertEquals("23 08 ff ff ff ff 0f $palancasA0", hex(e.soltarTodo().trama()))
    }

    // Spec `ride-screen`, «Aviso de giro».

    private fun estado(vararg f: FuncionZwift) = f.fold(EstadoMandoZwift()) { e, x -> e.pulsar(x) }

    @Test
    fun giro_sin_nada_pulsado() = assertEquals(emptySet<LadoGiro>(), EstadoMandoZwift().giro())

    @Test
    fun giro_a_la_izquierda_y_a_la_derecha() {
        assertEquals(setOf(LadoGiro.IZQUIERDA), estado(FuncionZwift.GIRAR_IZQUIERDA).giro())
        assertEquals(setOf(LadoGiro.DERECHA), estado(FuncionZwift.GIRAR_DERECHA).giro())
        assertEquals(setOf(LadoGiro.IZQUIERDA, LadoGiro.DERECHA), estado(FuncionZwift.GIRAR_IZQUIERDA, FuncionZwift.GIRAR_DERECHA).giro())
    }

    @Test
    fun el_freno_de_la_misma_palanca_anula_el_giro() {
        assertEquals(emptySet<LadoGiro>(), estado(FuncionZwift.FRENO_IZQUIERDO, FuncionZwift.GIRAR_IZQUIERDA).giro())
        assertEquals(emptySet<LadoGiro>(), estado(FuncionZwift.FRENO_DERECHO, FuncionZwift.GIRAR_DERECHA).giro())
    }

    @Test
    fun el_freno_de_la_otra_palanca_no_anula_el_giro() {
        assertEquals(setOf(LadoGiro.IZQUIERDA), estado(FuncionZwift.FRENO_DERECHO, FuncionZwift.GIRAR_IZQUIERDA).giro())
    }

    @Test
    fun otras_funciones_no_son_giro() {
        assertEquals(emptySet<LadoGiro>(), estado(FuncionZwift.A, FuncionZwift.FRENO_IZQUIERDO, FuncionZwift.ARRIBA).giro())
    }

    @Test
    fun la_misma_funcion_en_dos_botones_sigue_girando_al_soltar_uno() {
        val e = estado(FuncionZwift.GIRAR_IZQUIERDA, FuncionZwift.GIRAR_IZQUIERDA).soltar(FuncionZwift.GIRAR_IZQUIERDA)
        assertEquals(setOf(LadoGiro.IZQUIERDA), e.giro())
        assertEquals(emptySet<LadoGiro>(), e.soltar(FuncionZwift.GIRAR_IZQUIERDA).giro())
    }
}
