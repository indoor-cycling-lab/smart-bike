package app.smartbike.dominio

import app.smartbike.dominio.TipoGesto.Corta
import app.smartbike.dominio.TipoGesto.Doble
import app.smartbike.dominio.TipoGesto.Larga
import app.smartbike.dominio.TipoGesto.Pulsar
import app.smartbike.dominio.TipoGesto.Repetir
import app.smartbike.protocolo.SquareButton
import app.smartbike.protocolo.SquareButton.A
import app.smartbike.protocolo.SquareButton.CampagnoloDerecho
import app.smartbike.protocolo.SquareButton.CampagnoloIzquierdo
import app.smartbike.protocolo.SquareButton.Cuadrado
import app.smartbike.protocolo.SquareButton.PalancaDerecha1
import app.smartbike.protocolo.SquareButton.PalancaDerecha2
import app.smartbike.protocolo.SquareButton.PalancaIzquierda1
import app.smartbike.protocolo.SquareButton.PalancaIzquierda2
import app.smartbike.protocolo.SquareButton.Triangulo
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class AsignacionTest {

    private val defecto = Asignacion.POR_DEFECTO

    private fun e(b: SquareButton, m: SquareButton? = null) = Entrada(b, m)

    @Test
    fun palancas_sueltas_cambian_una_marcha() {
        assertEquals(Accion.MarchaMas(1), defecto.accion(e(PalancaDerecha1), Pulsar))
        assertEquals(Accion.MarchaMas(1), defecto.accion(e(PalancaDerecha2), Pulsar))
        assertEquals(Accion.MarchaMenos(1), defecto.accion(e(PalancaIzquierda1), Pulsar))
        assertEquals(Accion.MarchaMenos(1), defecto.accion(e(PalancaIzquierda2), Pulsar))
    }

    @Test
    fun combinaciones_con_el_campagnolo_del_mismo_lado() {
        assertEquals(Accion.MarchaMas(3), defecto.accion(e(PalancaDerecha1, CampagnoloDerecho), Pulsar))
        assertEquals(Accion.MarchaMasDura, defecto.accion(e(PalancaDerecha2, CampagnoloDerecho), Pulsar))
        assertEquals(Accion.MarchaMenos(3), defecto.accion(e(PalancaIzquierda1, CampagnoloIzquierdo), Pulsar))
        assertEquals(Accion.MarchaMasLigera, defecto.accion(e(PalancaIzquierda2, CampagnoloIzquierdo), Pulsar))
    }

    @Test
    fun una_combinacion_sin_reglas_hace_lo_del_boton_solo() {
        // Spec `virtual-gears`, «Modificador del otro lado».
        assertEquals(Accion.MarchaMas(1), defecto.accion(e(PalancaDerecha1, CampagnoloIzquierdo), Pulsar))
        assertEquals(Accion.MarchaMenos(1), defecto.accion(e(PalancaIzquierda2, CampagnoloDerecho), Pulsar))
        assertEquals(setOf(Corta, Larga), defecto.gestos(e(Cuadrado, CampagnoloDerecho)))
        assertEquals(Accion.Mando(FuncionZwift.A), defecto.accion(e(A, CampagnoloDerecho), Pulsar))
    }

    @Test
    fun cuadrado_corta_vuelve_y_larga_fija() {
        assertEquals(Accion.MarchaReferencia, defecto.accion(e(Cuadrado), Corta))
        assertEquals(Accion.FijarReferencia, defecto.accion(e(Cuadrado), Larga))
        assertNull(defecto.accion(e(Cuadrado), Pulsar))
        assertEquals(setOf(Corta, Larga), defecto.gestos(e(Cuadrado)))
    }

    @Test
    fun repetir_usa_la_accion_de_pulsar() {
        assertEquals(Accion.MarchaMas(1), defecto.accion(e(PalancaDerecha1), Repetir))
        assertTrue(Accion.MarchaMas(1).repetible && Accion.MarchaMenos(3).repetible)
        assertFalse(Accion.MarchaMasDura.repetible || Accion.MarchaReferencia.repetible || Accion.FijarReferencia.repetible)
    }

    @Test
    fun las_de_vatios_se_repiten_y_alternar_no() {
        assertTrue(Accion.ErgMas.repetible && Accion.ErgMenos.repetible)
        assertFalse(Accion.AlternarErgManual.repetible)
    }

    @ParameterizedTest
    @EnumSource(SquareButton::class, names = ["CampagnoloIzquierdo", "CampagnoloDerecho"])
    fun los_campagnolo_solos_no_tienen_accion(b: SquareButton) {
        assertTrue(defecto.gestos(e(b)).isEmpty())
    }

    @ParameterizedTest
    @EnumSource(SquareButton::class, names = ["PalancaDerecha1", "PalancaDerecha2", "PalancaIzquierda1", "PalancaIzquierda2", "Cuadrado", "Triangulo", "CampagnoloIzquierdo", "CampagnoloDerecho"], mode = EnumSource.Mode.EXCLUDE)
    fun el_resto_de_botones_solo_tiene_su_funcion_de_zwift(b: SquareButton) {
        assertEquals(setOf(Pulsar), defecto.gestos(e(b)))
        assertTrue(defecto.accion(e(b), Pulsar) is Accion.Mando)
    }

    @Test
    fun erg_por_defecto() {
        // Spec `button-mapping`, «ERG por defecto» y «△ corta sin acción».
        for (a in listOf(defecto, Asignacion.POR_DEFECTO_PLATOS)) {
            assertEquals(Accion.AlternarErgManual, a.accion(e(Triangulo), Larga))
            assertEquals(setOf(Larga), a.gestos(e(Triangulo)))
            assertNull(a.accion(e(Triangulo), Corta))
            assertTrue(a.reglasDe(Accion.ErgMas).isEmpty() && a.reglasDe(Accion.ErgMenos).isEmpty())
        }
    }

    @Test
    fun todas_las_acciones_tienen_boton_por_defecto() {
        // Salvo «+10 W» y «−10 W» (design.md del change `erg-manual`, decisión 5).
        Accion.ASIGNABLES.filterNot { it == Accion.ErgMas || it == Accion.ErgMenos || it == Accion.SiguienteModelo || it in Accion.VENTILADOR }.forEach { assertTrue(defecto.reglasDe(it).isNotEmpty(), "$it") }
        assertEquals(listOf(e(PalancaDerecha1), e(PalancaDerecha2)), defecto.reglasDe(Accion.MarchaMas(1)).map { it.entrada })
    }

    @Test
    fun entrada_de_otra_accion() {
        // Spec `button-mapping`, «Entrada de otra acción»: D1 pulsar en −3 marchas quita D1 de +1 marcha.
        val perdidas = defecto.enConflicto(e(PalancaDerecha1), Pulsar, Accion.MarchaMenos(3))
        assertEquals(listOf(Regla(e(PalancaDerecha1), Pulsar, Accion.MarchaMas(1))), perdidas)
        val nueva = defecto.asignar(e(PalancaDerecha1), Pulsar, Accion.MarchaMenos(3))
        assertEquals(Accion.MarchaMenos(3), nueva.accion(e(PalancaDerecha1), Pulsar))
        assertEquals(listOf(e(PalancaDerecha2)), nueva.reglasDe(Accion.MarchaMas(1)).map { it.entrada })
    }

    @Test
    fun entrada_libre() {
        assertTrue(defecto.enConflicto(e(Triangulo, CampagnoloDerecho), Corta, Accion.MarchaMenos(3)).isEmpty())
        val nueva = defecto.asignar(e(Triangulo, CampagnoloDerecho), Corta, Accion.MarchaMenos(3))
        assertEquals(Accion.MarchaMenos(3), nueva.accion(e(Triangulo, CampagnoloDerecho), Corta))
        assertEquals(2, nueva.reglasDe(Accion.MarchaMenos(3)).size)
    }

    @Test
    fun pulsar_excluye_corta_larga_y_doble() {
        // Spec `button-gestures`, «Asignar pulsar sobre un botón con larga».
        val perdidas = defecto.enConflicto(e(Cuadrado), Pulsar, Accion.MarchaMas(1))
        assertEquals(setOf(Accion.MarchaReferencia, Accion.FijarReferencia), perdidas.map { it.accion }.toSet())
        val nueva = defecto.asignar(e(Cuadrado), Pulsar, Accion.MarchaMas(1))
        assertEquals(setOf(Pulsar), nueva.gestos(e(Cuadrado)))
        // Y al revés: una corta sobre una palanca quita su «pulsar».
        val otra = defecto.asignar(e(PalancaDerecha1), Doble, Accion.MarchaMasDura)
        assertEquals(setOf(Doble), otra.gestos(e(PalancaDerecha1)))
    }

    @Test
    fun corta_larga_y_doble_conviven() {
        val a = defecto.asignar(e(Cuadrado), Doble, Accion.MarchaMasLigera)
        assertEquals(setOf(Corta, Larga, Doble), a.gestos(e(Cuadrado)))
        assertTrue(defecto.enConflicto(e(Cuadrado), Doble, Accion.MarchaMasLigera).isEmpty())
    }

    @Test
    fun misma_accion_no_es_conflicto() {
        assertTrue(defecto.enConflicto(e(PalancaDerecha1), Pulsar, Accion.MarchaMas(1)).isEmpty())
        assertEquals(defecto.reglas.size, defecto.asignar(e(PalancaDerecha1), Pulsar, Accion.MarchaMas(1)).reglas.size)
    }

    @Test
    fun quitar_una_palanca() {
        // Spec `button-mapping`, «Quitar una palanca».
        val a = defecto.quitar(e(PalancaDerecha2), Pulsar)
        assertNull(a.accion(e(PalancaDerecha2), Pulsar))
        assertEquals(Accion.MarchaMas(1), a.accion(e(PalancaDerecha1), Pulsar))
    }

    @Test
    fun palancas_invertidas() {
        val a = defecto.copy(invertirPalancas = true)
        assertEquals(Accion.MarchaMenos(1), a.accion(e(PalancaDerecha1), Pulsar))
        assertEquals(Accion.MarchaMas(1), a.accion(e(PalancaIzquierda2), Pulsar))
        // Combinación invertida: Campag. D + D1 hace lo de Campag. I + I1.
        assertEquals(Accion.MarchaMenos(3), a.accion(e(PalancaDerecha1, CampagnoloDerecho), Pulsar))
        assertEquals(Accion.MarchaMasLigera, a.accion(e(PalancaDerecha2, CampagnoloDerecho), Pulsar))
        // El resto de botones no cambia.
        assertEquals(Accion.MarchaReferencia, a.accion(e(Cuadrado), Corta))
    }

    @Test
    fun invertidas_se_ven_y_se_cambian_con_los_botones_fisicos() {
        val a = defecto.copy(invertirPalancas = true)
        assertEquals(listOf(e(PalancaIzquierda1), e(PalancaIzquierda2)), a.reglasDe(Accion.MarchaMas(1)).map { it.entrada })
        val b = a.asignar(e(PalancaDerecha1), Pulsar, Accion.MarchaMasDura)
        assertEquals(Accion.MarchaMasDura, b.accion(e(PalancaDerecha1), Pulsar))
        assertEquals(Accion.MarchaMasDura, b.copy(invertirPalancas = false).accion(e(PalancaIzquierda1), Pulsar))
        assertNull(b.quitar(e(PalancaDerecha1), Pulsar).accion(e(PalancaDerecha1), Pulsar))
    }

    @Test
    fun ida_y_vuelta_json() {
        val a = defecto.asignar(e(Triangulo, CampagnoloDerecho), Doble, Accion.MarchaMenos(3)).copy(invertirPalancas = true)
        val texto = Json.encodeToString(Asignacion.serializer(), a)
        assertTrue("\"fijar-referencia\"" in texto && "\"doble\"" in texto, texto)
        assertEquals(a, Json.decodeFromString(Asignacion.serializer(), texto))
    }

    @Test
    fun una_accion_desconocida_se_descarta_sin_perder_el_resto() {
        val texto = """{"reglas":[
            {"entrada":{"boton":"A"},"gesto":"pulsar","accion":{"type":"modo-erg"}},
            {"entrada":{"boton":"Y"},"gesto":"triple","accion":{"type":"marcha-mas-dura"}},
            {"entrada":{"boton":"PalancaDerecha1"},"gesto":"pulsar","accion":{"type":"marcha-mas","n":1}}
        ]}"""
        val a = Json.decodeFromString(Asignacion.serializer(), texto)
        assertEquals(listOf(Regla(e(PalancaDerecha1), Pulsar, Accion.MarchaMas(1))), a.reglas)
        assertNull(a.accion(e(A), Pulsar))
    }

    // Shimano (spec `virtual-gears`, «Palancas con la Shimano» y «Saltos y extremos con la Shimano»)

    private val platos = Asignacion.POR_DEFECTO_PLATOS

    @Test
    fun palancas_de_la_shimano() {
        assertEquals(Accion.MarchaMenos(1), platos.accion(e(PalancaDerecha1), Pulsar))
        assertEquals(Accion.MarchaMas(1), platos.accion(e(PalancaDerecha2), Pulsar))
        assertEquals(Accion.PlatoMas, platos.accion(e(PalancaIzquierda1), Pulsar))
        assertEquals(Accion.PlatoMenos, platos.accion(e(PalancaIzquierda2), Pulsar))
    }

    @Test
    fun saltos_y_extremos_de_la_shimano() {
        assertEquals(Accion.MarchaMenos(3), platos.accion(e(PalancaDerecha1, CampagnoloDerecho), Pulsar))
        assertEquals(Accion.MarchaMas(3), platos.accion(e(PalancaDerecha2, CampagnoloDerecho), Pulsar))
        assertEquals(Accion.MarchaMasDura, platos.accion(e(PalancaIzquierda1, CampagnoloIzquierdo), Pulsar))
        assertEquals(Accion.MarchaMasLigera, platos.accion(e(PalancaIzquierda2, CampagnoloIzquierdo), Pulsar))
        assertEquals(Accion.MarchaReferencia, platos.accion(e(Cuadrado), Corta))
        assertEquals(Accion.FijarReferencia, platos.accion(e(Cuadrado), Larga))
    }

    @Test
    fun las_de_plato_no_se_repiten() {
        assertFalse(Accion.PlatoMas.repetible)
        assertFalse(Accion.PlatoMenos.repetible)
    }

    @Test
    fun acciones_asignables_por_transmision() {
        assertEquals(Accion.ASIGNABLES + Accion.ZWIFT, Accion.asignables(Transmision.Lineal()))
        val conPlatos = Accion.asignables(Desarrollos.SHIMANO_105).dropLast(Accion.ZWIFT.size)
        assertEquals(Accion.ZWIFT, Accion.asignables(Desarrollos.SHIMANO_105).takeLast(Accion.ZWIFT.size))
        assertEquals(Accion.ASIGNABLES.size + 2, conPlatos.size)
        assertEquals(listOf(Accion.MarchaMenos(3), Accion.PlatoMas, Accion.PlatoMenos), conPlatos.subList(3, 6))
        // Todas tienen botón en la asignación por defecto de la Shimano, salvo «+10 W» y «−10 W».
        conPlatos.filterNot { it == Accion.ErgMas || it == Accion.ErgMenos || it == Accion.SiguienteModelo || it in Accion.VENTILADOR }.forEach { a -> assertTrue(platos.reglas.any { it.accion == a }, "$a") }
        // Las de ERG, tras «Fijar la marcha de referencia», también con platos.
        val erg = listOf(Accion.FijarReferencia, Accion.ErgMas, Accion.ErgMenos, Accion.AlternarErgManual)
        assertEquals(erg, Accion.ASIGNABLES.dropLast(6).takeLast(4))
        assertEquals(erg, conPlatos.dropLast(6).takeLast(4))
        // Spec `button-mapping`, «Acciones de pantalla»: tras «Alternar SIM ⇄ ERG».
        val pantalla = listOf(Accion.PantallaAnterior, Accion.PantallaSiguiente)
        assertEquals(pantalla, Accion.ASIGNABLES.dropLast(4).takeLast(2))
        assertEquals(pantalla, conPlatos.dropLast(4).takeLast(2))
        assertEquals(platos, Asignacion.porDefecto(Desarrollos.SHIMANO_105))
        assertEquals(defecto, Asignacion.porDefecto(Transmision.Lineal()))
    }

    @Test
    fun acciones_de_plato_ida_y_vuelta_json() {
        val texto = Json.encodeToString(Asignacion.serializer(), platos)
        assertTrue("\"plato-mas\"" in texto && "\"plato-menos\"" in texto, texto)
        assertEquals(platos, Json.decodeFromString(Asignacion.serializer(), texto))
    }

    @Test
    fun acciones_de_erg_ida_y_vuelta_json() {
        val a = defecto.asignar(e(A), Pulsar, Accion.ErgMas).asignar(e(SquareButton.B), Pulsar, Accion.ErgMenos)
        val texto = Json.encodeToString(Asignacion.serializer(), a)
        assertTrue("\"erg-mas\"" in texto && "\"erg-menos\"" in texto && "\"alternar-erg\"" in texto, texto)
        assertEquals(a, Json.decodeFromString(Asignacion.serializer(), texto))
    }

    @Test
    fun todas_conserva_las_posiciones_anteriores() {
        // La navegación a Asignar acción usa la posición en TODAS: las de ERG van al final.
        assertEquals(Accion.PlatoMas, Accion.TODAS[8])
        assertEquals(Accion.PlatoMenos, Accion.TODAS[9])
        assertEquals(listOf(Accion.ErgMas, Accion.ErgMenos, Accion.AlternarErgManual), Accion.TODAS.subList(10, 13))
        assertEquals(listOf(Accion.PantallaAnterior, Accion.PantallaSiguiente), Accion.TODAS.subList(13, 15))
        // Las funciones de Zwift, detrás de todas (design.md del change `mando-zwift-emulado`, decisión 1).
        assertEquals(Accion.ZWIFT, Accion.TODAS.drop(15).dropLast(4))
        // «Siguiente modelo» (change `modelos-tabla-y-fisico`) va detrás de las funciones de Zwift, y las del ventilador
        // (change `ventilador-headwind`) detrás de ella.
        assertEquals(Accion.SiguienteModelo, Accion.TODAS[Accion.TODAS.size - 4])
        assertEquals(Accion.VENTILADOR, Accion.TODAS.takeLast(3))
        assertEquals(Accion.TODAS.toSet(), (Accion.ASIGNABLES + Accion.asignables(Desarrollos.SHIMANO_105)).toSet())
    }

    // Pantalla anterior o siguiente (spec `button-mapping`, «Pantalla por defecto» y «Cruceta sin Campagnolo»)

    @Test
    fun pantalla_por_defecto_con_campagnolo_izquierdo_y_cruceta() {
        for (a in listOf(defecto, platos)) {
            assertEquals(Accion.PantallaAnterior, a.accion(e(SquareButton.CrucetaIzquierda, CampagnoloIzquierdo), Pulsar))
            assertEquals(Accion.PantallaSiguiente, a.accion(e(SquareButton.CrucetaDerecha, CampagnoloIzquierdo), Pulsar))
            assertEquals(Accion.PantallaSiguiente, a.accion(e(SquareButton.CrucetaDerecha, CampagnoloIzquierdo), Repetir))
        }
    }

    @Test
    fun cruceta_sin_campagnolo_va_a_zwift() {
        // Spec `button-mapping`, «Cruceta sin Campagnolo»: no cambia de pantalla y, con el mando, llega a Zwift.
        for (a in listOf(defecto, platos)) {
            assertEquals(Accion.Mando(FuncionZwift.DERECHA), a.accion(e(SquareButton.CrucetaDerecha), Pulsar))
            assertEquals(Accion.Mando(FuncionZwift.IZQUIERDA), a.accion(e(SquareButton.CrucetaIzquierda), Pulsar))
            assertEquals(Accion.Mando(FuncionZwift.DERECHA), a.accion(e(SquareButton.CrucetaDerecha, CampagnoloDerecho), Pulsar))
        }
    }

    @Test
    fun acciones_de_pantalla_ida_y_vuelta_json() {
        val texto = Json.encodeToString(Asignacion.serializer(), defecto)
        assertTrue("\"pantalla-anterior\"" in texto && "\"pantalla-siguiente\"" in texto, texto)
        assertEquals(defecto, Json.decodeFromString(Asignacion.serializer(), texto))
        assertFalse(Accion.PantallaSiguiente.repetible)
    }

    // Funciones de Zwift (spec `button-mapping`, «Acciones asignables» y «Zwift por defecto»)

    @Test
    fun funciones_de_zwift_ida_y_vuelta_json() {
        val nombres = listOf(
            "arriba", "abajo", "izquierda", "derecha", "a", "b", "y", "z",
            "girar-izquierda", "girar-derecha", "freno-izquierdo", "freno-derecho",
        )
        FuncionZwift.entries.zip(nombres).forEach { (f, nombre) ->
            val texto = Json.encodeToString(Accion.serializer(), Accion.Mando(f))
            assertEquals("""{"type":"zwift","funcion":"$nombre"}""", texto)
            assertEquals(Accion.Mando(f), Json.decodeFromString(Accion.serializer(), texto))
        }
        val texto = Json.encodeToString(Asignacion.serializer(), defecto)
        assertEquals(defecto, Json.decodeFromString(Asignacion.serializer(), texto))
    }

    @Test
    fun funciones_de_zwift_no_son_repetibles() {
        Accion.ZWIFT.forEach { assertFalse(it.repetible, "$it") }
    }

    @Test
    fun zwift_por_defecto_en_las_dos_transmisiones() {
        val botones = mapOf(
            SquareButton.CrucetaArriba to FuncionZwift.ARRIBA,
            SquareButton.CrucetaAbajo to FuncionZwift.ABAJO,
            SquareButton.CrucetaIzquierda to FuncionZwift.IZQUIERDA,
            SquareButton.CrucetaDerecha to FuncionZwift.DERECHA,
            A to FuncionZwift.A,
            SquareButton.B to FuncionZwift.B,
            SquareButton.Y to FuncionZwift.Y,
            SquareButton.Z to FuncionZwift.Z,
            SquareButton.Equis to FuncionZwift.GIRAR_IZQUIERDA,
            SquareButton.Circulo to FuncionZwift.GIRAR_DERECHA,
            SquareButton.FrenoIzquierdo to FuncionZwift.FRENO_IZQUIERDO,
            SquareButton.FrenoDerecho to FuncionZwift.FRENO_DERECHO,
        )
        for (a in listOf(defecto, platos)) {
            botones.forEach { (b, f) ->
                assertEquals(Accion.Mando(f), a.accion(e(b), Pulsar), "$b")
                assertEquals(listOf(Regla(e(b), Pulsar, Accion.Mando(f))), a.reglasDe(Accion.Mando(f)))
            }
            // Con Campag. I, la cruceta sigue cambiando de pantalla.
            assertEquals(Accion.PantallaAnterior, a.accion(e(SquareButton.CrucetaIzquierda, CampagnoloIzquierdo), Pulsar))
            assertEquals(Accion.PantallaSiguiente, a.accion(e(SquareButton.CrucetaDerecha, CampagnoloIzquierdo), Pulsar))
            // △ y □ siguen como estaban.
            assertNull(a.accion(e(Triangulo), Corta))
            assertEquals(Accion.MarchaReferencia, a.accion(e(Cuadrado), Corta))
        }
    }

    @Test
    fun las_acciones_del_ventilador_van_tras_siguiente_modelo_sin_boton_y_no_se_repiten() {
        val a = Accion.ASIGNABLES
        assertEquals(Accion.VENTILADOR, a.subList(a.indexOf(Accion.SiguienteModelo) + 1, a.size))
        assertEquals(Accion.VENTILADOR, Accion.asignables(Desarrollos.SHIMANO_105).let { it.subList(it.indexOf(Accion.SiguienteModelo) + 1, it.indexOf(Accion.SiguienteModelo) + 4) })
        Accion.VENTILADOR.forEach {
            assertTrue(defecto.reglasDe(it).isEmpty(), "$it")
            assertTrue(Configuracion().asignacion.reglas.none { r -> r.accion == it })
            assertTrue(!it.repetible, "$it")
        }
    }

    @Test
    fun una_asignacion_guardada_con_el_ventilador_se_lee() {
        val c = Configuracion().conAsignacion(Configuracion().asignacion.copy(reglas = Configuracion().asignacion.reglas + Regla(e(PalancaDerecha1), TipoGesto.Pulsar, Accion.VentiladorMas)))
        val leida = Configuracion.deJson(Configuracion.aJson(c))
        assertTrue(leida.asignacion.reglas.any { it.accion == Accion.VentiladorMas })
        assertTrue(Configuracion().asignacion.reglas.none { it.accion in Accion.VENTILADOR })
    }
}
