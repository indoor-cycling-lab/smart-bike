package app.smartbike.dominio

import app.smartbike.protocolo.SquareButton
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ConfiguracionTest {

    private val c = Configuracion()
    private fun lineal(n: Int) = Transmision.Lineal(24).marcha(n)
    private fun shimano(plato: Int, pinon: Int) = EstadoMarcha.PlatoPinon(plato, pinon)
    private val transmisionShimano = Desarrollos.SHIMANO_105

    /** Las reglas que reciben las configuraciones anteriores a la versión 3 al leerse. */
    private val nuevas = listOf(Asignacion.ALTERNAR_ERG, Asignacion.PANTALLA_ANTERIOR, Asignacion.PANTALLA_SIGUIENTE) + Asignacion.ZWIFT_POR_DEFECTO

    @Test
    fun valores_por_defecto_de_funcional() {
        assertEquals(Transmision.Lineal(24), c.transmisionElegida)
        assertEquals(Transmision.Lineal(24).marcha(12), c.referencia)
        assertEquals(12, c.lineal.neutra)
        assertEquals(1.0, c.incrementoPct)
        assertTrue(c.recordarUltimaMarcha && c.mostrarAccion && c.vibracion)
        assertEquals(ModoBajadas.LA_MARCHA_NO_CREA_BAJADAS, c.bajadas)
        assertEquals(TemaApp.OSCURO, c.tema)
    }

    @Test
    fun tema_ida_y_vuelta() {
        val claro = Configuracion(tema = TemaApp.CLARO)
        val json = Configuracion.aJson(claro)
        assertTrue("\"tema\":\"claro\"" in json)
        assertEquals(TemaApp.CLARO, Configuracion.deJson(json).tema)
    }

    @Test
    fun documento_anterior_sin_tema_se_lee_en_oscuro() {
        val claro = Configuracion(tema = TemaApp.CLARO, pesoCiclistaKg = 68, bajadas = ModoBajadas.NUNCA_EMPUJAR)
        val sinTema = Configuracion.aJson(claro).replace(Regex(",?\"tema\":\"[^\"]*\""), "")
        assertTrue("tema" !in sinTema)
        val leida = Configuracion.deJson(sinTema)
        assertEquals(TemaApp.OSCURO, leida.tema)
        assertEquals(68, leida.pesoCiclistaKg)
        assertEquals(ModoBajadas.NUNCA_EMPUJAR, leida.bajadas)
        assertEquals(Configuracion.VERSION, leida.version)
    }

    @Test
    fun documento_ilegible_da_el_tema_oscuro() {
        assertEquals(TemaApp.OSCURO, Configuracion.deJson("{no es json").tema)
    }

    @Test
    fun orientacion_ida_y_vuelta() {
        val horizontal = Configuracion(orientacion = OrientacionApp.HORIZONTAL)
        val json = Configuracion.aJson(horizontal)
        assertTrue("\"orientacion\":\"horizontal\"" in json)
        assertEquals(OrientacionApp.HORIZONTAL, Configuracion.deJson(json).orientacion)
    }

    @Test
    fun documento_anterior_sin_orientacion_se_lee_segun_el_movil() {
        val c = Configuracion(orientacion = OrientacionApp.VERTICAL, pesoCiclistaKg = 68, tema = TemaApp.CLARO)
        val sinOrientacion = Configuracion.aJson(c).replace(Regex(",?\"orientacion\":\"[^\"]*\""), "")
        assertTrue("orientacion" !in sinOrientacion)
        val leida = Configuracion.deJson(sinOrientacion)
        assertEquals(OrientacionApp.SEGUN_MOVIL, leida.orientacion)
        assertEquals(68, leida.pesoCiclistaKg)
        assertEquals(TemaApp.CLARO, leida.tema)
        assertEquals(Configuracion.VERSION, leida.version)
    }

    @Test
    fun documento_ilegible_da_la_orientacion_segun_el_movil() {
        assertEquals(OrientacionApp.SEGUN_MOVIL, Configuracion.deJson("{no es json").orientacion)
    }

    @Test
    fun bajadas_ida_y_vuelta_con_cada_modo() {
        ModoBajadas.entries.forEach { modo ->
            val conModo = Configuracion(bajadas = modo)
            assertEquals(conModo, Configuracion.deJson(Configuracion.aJson(conModo)))
        }
    }

    @Test
    fun documento_anterior_sin_bajadas_conserva_los_demas_ajustes() {
        val conBotones = Configuracion(lineal = AjustesLineal(referencia = 15))
        val sinBajadas = Configuracion.aJson(conBotones).replace(Regex(",?\"bajadas\":\"[^\"]*\""), "")
        assertTrue("bajadas" !in sinBajadas)
        val leida = Configuracion.deJson(sinBajadas)
        assertEquals(15, leida.lineal.referencia)
        assertEquals(ModoBajadas.LA_MARCHA_NO_CREA_BAJADAS, leida.bajadas)
    }

    @Test
    fun ida_y_vuelta_json() {
        val e = EstadoGuardado(
            ultimaMarcha = 17,
            rodillo = DispositivoConfigurado("AA:BB:CC:DD:EE:FF", "Tacx Neo 2T"),
            mando = DispositivoConfigurado("11:22:33:44:55:66", "SQUARE 1234"),
            notificacionesPedidas = true,
        )
        assertEquals(e, EstadoGuardado.deJson(EstadoGuardado.aJson(e)))
        assertEquals(3, EstadoGuardado.deJson(EstadoGuardado.aJson(e)).version)
    }

    @Test
    fun el_pulsometro_configurado_hace_ida_y_vuelta() {
        val e = EstadoGuardado(pulsometro = DispositivoConfigurado("AA:BB:CC:DD:EE:01", "HW9-12345"))
        assertEquals(e, EstadoGuardado.deJson(EstadoGuardado.aJson(e)))
        assertEquals(3, EstadoGuardado.deJson(EstadoGuardado.aJson(e)).version)
    }

    @Test
    fun un_estado_anterior_sin_pulsometro_se_lee_igual() {
        val antes = """{"version":2,"ultimaMarcha":9,"rodillo":{"direccion":"AA","nombre":"Neo"},"notificacionesPedidas":true}"""
        val leido = EstadoGuardado.deJson(antes)
        assertEquals(null, leido.pulsometro)
        assertEquals(9, leido.ultimaMarcha)
        assertEquals(DispositivoConfigurado("AA", "Neo"), leido.rodillo)
    }

    @Test
    fun la_configuracion_no_guarda_el_pulsometro() {
        assertTrue("pulsometro" !in Configuracion.aJson(Configuracion()))
    }

    @Test
    fun migra_la_version_1() {
        val v1 = """{"version":1,"ultimaMarcha":17,"direccionRodillo":"AA:BB:CC:DD:EE:FF","notificacionesPedidas":true}"""
        assertEquals(
            EstadoGuardado(
                ultimaMarcha = 17,
                rodillo = DispositivoConfigurado("AA:BB:CC:DD:EE:FF", "Tacx Neo"),
                notificacionesPedidas = true,
            ),
            EstadoGuardado.deJson(v1),
        )
    }

    @Test
    fun migra_la_version_1_sin_direccion() {
        val v1 = """{"version":1,"ultimaMarcha":9,"direccionRodillo":null,"notificacionesPedidas":false}"""
        assertEquals(EstadoGuardado(ultimaMarcha = 9), EstadoGuardado.deJson(v1))
    }

    @Test
    fun primer_uso_empieza_en_la_referencia() {
        assertEquals(lineal(12), EstadoGuardado().marchaInicial(c))
    }

    @Test
    fun recuerda_la_ultima() {
        assertEquals(lineal(17), EstadoGuardado(ultimaMarcha = 17).marchaInicial(c))
    }

    @Test
    fun marcha_fuera_de_rango_o_documento_ilegible_dan_la_referencia() {
        assertEquals(lineal(12), EstadoGuardado(ultimaMarcha = 30).marchaInicial(c))
        assertEquals(lineal(12), EstadoGuardado(ultimaMarcha = 0).marchaInicial(c))
        assertEquals(EstadoGuardado(), EstadoGuardado.deJson("{no es json"))
        assertEquals(EstadoGuardado(), EstadoGuardado.deJson("[1, 2]"))
    }

    @Test
    fun sin_recordar_empieza_en_la_referencia() {
        val sinRecordar = c.copy(recordarUltimaMarcha = false, lineal = AjustesLineal(referencia = 15))
        assertEquals(lineal(15), EstadoGuardado(ultimaMarcha = 20).marchaInicial(sinRecordar))
    }

    @Test
    fun version_2_sin_identidades_se_lee_igual() {
        val e = EstadoGuardado.deJson("""{"version":2,"ultimaMarcha":14}""")
        assertEquals(14, e.ultimaMarcha)
        assertTrue(e.identidades.isEmpty())
    }

    @Test
    fun puente_ida_y_vuelta() {
        val clave = ClaveIdentidad.de(ModoConexion.FTMS_WIFI, Identidad.SMART_BIKE)
        val e = EstadoGuardado(identidades = mapOf(clave to IdentidadDispositivo("02:11:22:33:44:55", "0123456789")))
        assertEquals(e, EstadoGuardado.deJson(EstadoGuardado.aJson(e)))
        assertEquals(EstadoGuardado.VERSION, 3)
    }

    @Test
    fun puente_nuevo_con_direccion_local_administrada() {
        repeat(50) { semilla ->
            val p = IdentidadDispositivo.nuevo(kotlin.random.Random(semilla))
            assertTrue(Regex("([0-9A-F]{2}:){5}[0-9A-F]{2}").matches(p.mac), p.mac)
            val primero = p.mac.substring(0, 2).toInt(16)
            assertEquals(0x02, primero and 0x03, p.mac)
            assertTrue(Regex("[0-9]{10}").matches(p.serie), p.serie)
        }
    }

    @Test
    fun configuracion_por_defecto_de_los_botones() {
        assertEquals(Asignacion.POR_DEFECTO, c.asignacion)
        assertEquals(TiemposGestos(800, 300, 800, 250), c.botones.tiempos)
        assertTrue(c.botones.autorrepeticion)
        assertEquals(Configuracion.VERSION, c.version)
    }

    @Test
    fun configuracion_ida_y_vuelta() {
        val a = Asignacion.POR_DEFECTO.asignar(Entrada(SquareButton.Triangulo, SquareButton.CampagnoloDerecho), TipoGesto.Corta, Accion.MarchaMenos(3))
        val x = c.copy(
            lineal = AjustesLineal(referencia = 15, asignacion = a.copy(invertirPalancas = true)),
            platos = mapOf(transmisionShimano.id to AjustesPlatos(ModoCambio.MANUAL, shimano(52, 19), Asignacion.POR_DEFECTO_PLATOS.copy(invertirPalancas = true))),
            transmision = transmisionShimano.id,
            botones = ConfiguracionBotones(TiemposGestos(800, 450, 600, 400), autorrepeticion = false),
        )
        assertEquals(x, Configuracion.deJson(Configuracion.aJson(x)))
    }

    @Test
    fun configuracion_danada_da_los_valores_por_defecto() {
        assertEquals(Configuracion(), Configuracion.deJson("{no es json"))
        assertEquals(Configuracion(), Configuracion.deJson("[1, 2]"))
        assertEquals(Configuracion(), Configuracion.deJson(""))
        // Una marcha de referencia imposible invalida el documento.
        assertEquals(Configuracion(), Configuracion.deJson("""{"version":1,"marchaReferencia":40}"""))
        assertEquals(Configuracion(), Configuracion.deJson("""{"version":2,"lineal":{"referencia":40}}"""))
    }

    @Test
    fun un_campo_danado_solo_pierde_ese_campo() {
        val l = Configuracion.leerGuardada("""{"version":10,"pesoCiclistaKg":81,"conexion":{"modo":"por-telepatia"},"tema":"claro"}""")
        assertEquals(81, l.valor.pesoCiclistaKg)
        assertEquals(TemaApp.CLARO, l.valor.tema)
        assertEquals(Configuracion().conexion, l.valor.conexion)
        assertEquals(listOf("conexion"), l.descartados)
        assertFalse(l.ilegible)
    }

    @Test
    fun la_lectura_dice_si_el_documento_era_ilegible_o_estaba_entero() {
        assertTrue(Configuracion.leerGuardada("{no es json").ilegible)
        assertTrue(Configuracion.leerGuardada("[1, 2]").ilegible)
        assertTrue(Configuracion.leerGuardada(Configuracion.aJson(Configuracion(pesoCiclistaKg = 70))).completa)
        assertTrue(EstadoGuardado.leerGuardado("").ilegible)
        assertTrue(EstadoGuardado.leerGuardado(EstadoGuardado.aJson(EstadoGuardado(ultimaMarcha = 9))).completa)
    }

    @Test
    fun un_campo_danado_del_estado_no_se_lleva_los_emparejamientos_ni_el_nombre_original() {
        val l = EstadoGuardado.leerGuardado(
            """{"version":3,"rodillo":{"direccion":"AA:AA","nombre":"Tacx Neo"},"identidades":{"x":42},""" +
                """"nombreBluetoothOriginal":"OnePlus 8","nombreBluetoothPuesto":"Smart Bike"}""",
        )
        assertEquals(DispositivoConfigurado("AA:AA", "Tacx Neo"), l.valor.rodillo)
        assertEquals("OnePlus 8", l.valor.nombreBluetoothOriginal)
        assertEquals("Smart Bike", l.valor.nombreBluetoothPuesto)
        assertEquals(emptyMap<String, IdentidadDispositivo>(), l.valor.identidades)
        assertEquals(listOf("identidades"), l.descartados)
    }

    @Test
    fun documento_parcial_completa_con_los_valores_por_defecto() {
        val x = Configuracion.deJson("""{"version":1,"marchaReferencia":15}""")
        assertEquals(15, x.lineal.referencia)
        assertEquals(ConfiguracionBotones(), x.botones)
    }

    @Test
    fun tiempos_fuera_de_limites_se_recortan() {
        val x = Configuracion.deJson("""{"botones":{"tiempos":{"largaMs":5000,"dobleMs":10,"retardoRepeticionMs":500,"ritmoRepeticionMs":20}}}""")
        assertEquals(TiemposGestos(1500, 150, 500, 100), x.botones.tiempos)
    }

    @Test
    fun regla_con_accion_desconocida_se_descarta() {
        val x = Configuracion.deJson(
            """{"botones":{"asignacion":{"reglas":[
                {"entrada":{"boton":"A"},"gesto":"pulsar","accion":{"type":"modo-erg"}},
                {"entrada":{"boton":"Cuadrado"},"gesto":"corta","accion":{"type":"marcha-referencia"}}
            ]}}}""",
        )
        assertEquals(listOf(Regla(Entrada(SquareButton.Cuadrado), TipoGesto.Corta, Accion.MarchaReferencia)) + nuevas, x.lineal.asignacion.reglas)
    }

    // Transmisiones (design.md del change `transmisiones-de-marca`, decisión 6)

    @Test
    fun configuracion_anterior_a_la_shimano() {
        // Spec `button-mapping`, «Configuración anterior a la Shimano»: lo de la versión 1 pasa a la lineal.
        val v1 = """{"version":1,"transmision":{"type":"lineal","marchas":24},"marchaInicial":12,"marchaReferencia":15,
            "marchaNeutra":12,"incrementoPct":1.0,"bajadas":"nunca-empujar","botones":{"asignacion":{"reglas":[
            {"entrada":{"boton":"Triangulo"},"gesto":"corta","accion":{"type":"marcha-menos","n":3}}],
            "invertirPalancas":true},"tiempos":{"largaMs":900,"dobleMs":300,"retardoRepeticionMs":800,"ritmoRepeticionMs":250},
            "autorrepeticion":false}}"""
        val x = Configuracion.deJson(v1)
        assertEquals(Configuracion.VERSION, x.version)
        assertEquals(Transmision.Lineal(24), x.transmisionElegida)
        assertEquals(lineal(15), x.referencia)
        assertEquals(listOf(Regla(Entrada(SquareButton.Triangulo), TipoGesto.Corta, Accion.MarchaMenos(3))) + nuevas, x.asignacion.reglas)
        assertTrue(x.asignacion.invertirPalancas)
        assertEquals(900, x.botones.tiempos.largaMs)
        assertEquals(false, x.botones.autorrepeticion)
        assertEquals(ModoBajadas.NUNCA_EMPUJAR, x.bajadas)
        // La Shimano, con sus valores por defecto.
        assertEquals(Asignacion.POR_DEFECTO_PLATOS, x.asignacion(transmisionShimano))
        assertEquals(shimano(36, 17), x.referencia(transmisionShimano))
        assertEquals(ModoCambio.AUTOMATICO, x.modo(transmisionShimano))
        // Al guardarla, ya es la versión 4.
        assertEquals(x, Configuracion.deJson(Configuracion.aJson(x)))
    }

    @Test
    fun shimano_por_defecto() {
        val x = c.copy(transmision = transmisionShimano.id)
        assertEquals(transmisionShimano, x.transmisionElegida)
        assertEquals(shimano(36, 17), x.referencia)
        assertEquals(Asignacion.POR_DEFECTO_PLATOS, x.asignacion)
        assertEquals(ModoCambio.AUTOMATICO, x.modo)
    }

    @Test
    fun cada_transmision_con_su_referencia_y_su_asignacion() {
        val a = Asignacion.POR_DEFECTO_PLATOS.asignar(Entrada(SquareButton.Triangulo), TipoGesto.Corta, Accion.PlatoMas)
        val x = c.copy(transmision = transmisionShimano.id).conReferencia(shimano(52, 19)).conAsignacion(a).conModo(ModoCambio.MANUAL)
        assertEquals(shimano(52, 19), x.referencia)
        assertEquals(a, x.asignacion)
        assertEquals(ModoCambio.MANUAL, x.modo)
        // La lineal no cambia.
        val l = x.copy(transmision = Transmision.Lineal.ID)
        assertEquals(lineal(12), l.referencia)
        assertEquals(Asignacion.POR_DEFECTO, l.asignacion)
        // Y al revés.
        val y = l.conReferencia(lineal(15))
        assertEquals(shimano(52, 19), y.referencia(transmisionShimano))
    }

    @Test
    fun transmision_o_referencia_desconocidas() {
        assertEquals(Transmision.Lineal(24), Configuracion.deJson("""{"version":2,"transmision":"campagnolo-sr"}""").transmisionElegida)
        val fuera = Configuracion.deJson("""{"version":2,"transmision":"shimano-105","platos":{"shimano-105":{"referencia":{"plato":50,"pinon":17}}}}""")
        assertEquals(shimano(36, 17), fuera.referencia)
    }

    @Test
    fun ultima_marcha_por_transmision() {
        val x = c.copy(transmision = transmisionShimano.id)
        val e = EstadoGuardado(ultimaMarcha = 17).conUltima(transmisionShimano, shimano(52, 15))
        assertEquals(shimano(52, 15), e.marchaInicial(x))
        assertEquals(lineal(17), e.marchaInicial(c))
        // Fuera del desarrollo o sin recordar, la referencia.
        assertEquals(shimano(36, 17), EstadoGuardado(ultimasMarchas = mapOf(transmisionShimano.id to shimano(50, 17))).marchaInicial(x))
        assertEquals(shimano(36, 17), e.marchaInicial(x.copy(recordarUltimaMarcha = false)))
        assertEquals(e, EstadoGuardado.deJson(EstadoGuardado.aJson(e)))
        // Un estado.json sin el campo nuevo se lee igual.
        assertEquals(emptyMap<String, EstadoMarcha.PlatoPinon>(), EstadoGuardado.deJson("""{"version":2,"ultimaMarcha":14}""").ultimasMarchas)
    }

    // ERG manual (design.md del change `erg-manual`, decisión 5)

    private val trianguloCorta = Regla(Entrada(SquareButton.Triangulo), TipoGesto.Corta, Accion.MarchaMenos(3))

    private fun v2(lineal: List<Regla>, platos: List<Regla>? = null): String {
        val x = Configuracion(
            version = 2,
            lineal = AjustesLineal(asignacion = Asignacion(lineal)),
            platos = platos?.let { mapOf(transmisionShimano.id to AjustesPlatos(asignacion = Asignacion(it))) } ?: emptyMap(),
        )
        return Configuracion.aJson(x)
    }

    @Test
    fun configuracion_anterior_al_erg_manual() {
        // Spec `button-mapping`, «Configuración anterior al ERG manual».
        val x = Configuracion.deJson(v2(listOf(trianguloCorta), platos = listOf(trianguloCorta)))
        assertEquals(Configuracion.VERSION, x.version)
        assertEquals(Accion.MarchaMenos(3), x.asignacion.accion(Entrada(SquareButton.Triangulo), TipoGesto.Corta))
        assertEquals(Accion.AlternarErgManual, x.asignacion.accion(Entrada(SquareButton.Triangulo), TipoGesto.Larga))
        assertEquals(Accion.AlternarErgManual, x.asignacion(transmisionShimano).accion(Entrada(SquareButton.Triangulo), TipoGesto.Larga))
    }

    @Test
    fun desarrollo_sin_guardar_trae_la_regla_por_defecto() {
        val x = Configuracion.deJson(v2(listOf(trianguloCorta)))
        assertEquals(Asignacion.POR_DEFECTO_PLATOS, x.asignacion(transmisionShimano))
    }

    @Test
    fun entrada_ya_ocupada() {
        // Spec `button-mapping`, «Entrada ya ocupada»: △ pulsar excluye △ larga.
        val ocupada = Regla(Entrada(SquareButton.Triangulo), TipoGesto.Pulsar, Accion.MarchaReferencia)
        val x = Configuracion.deJson(v2(listOf(ocupada)))
        assertEquals(listOf(ocupada, Asignacion.PANTALLA_ANTERIOR, Asignacion.PANTALLA_SIGUIENTE) + Asignacion.ZWIFT_POR_DEFECTO, x.lineal.asignacion.reglas)
        assertTrue(x.asignacion.reglasDe(Accion.AlternarErgManual).isEmpty())
        // Y △ larga en otra acción tampoco se pisa.
        val larga = Regla(Entrada(SquareButton.Triangulo), TipoGesto.Larga, Accion.MarchaMas(3))
        assertEquals(
            listOf(larga, Asignacion.PANTALLA_ANTERIOR, Asignacion.PANTALLA_SIGUIENTE) + Asignacion.ZWIFT_POR_DEFECTO,
            Configuracion.deJson(v2(listOf(larga))).lineal.asignacion.reglas,
        )
    }

    @Test
    fun regla_nueva_quitada_por_el_usuario() {
        // Spec `button-mapping`, «Regla nueva quitada por el usuario»: en la versión 3 no se vuelve a añadir.
        val migrada = Configuracion.deJson(v2(listOf(trianguloCorta)))
        val sin = migrada.conAsignacion(migrada.asignacion.quitar(Entrada(SquareButton.Triangulo), TipoGesto.Larga))
        val x = Configuracion.deJson(Configuracion.aJson(sin))
        assertTrue(x.asignacion.reglasDe(Accion.AlternarErgManual).isEmpty())
    }

    @Test
    fun ultima_potencia_erg() {
        assertEquals(150, EstadoGuardado().potenciaErgManualW)
        assertEquals(150, EstadoGuardado.deJson("""{"version":2,"ultimaMarcha":14}""").potenciaErgManualW)
        val e = EstadoGuardado(ultimaPotenciaErgW = 220)
        assertEquals(220, EstadoGuardado.deJson(EstadoGuardado.aJson(e)).potenciaErgManualW)
        assertEquals(1000, EstadoGuardado(ultimaPotenciaErgW = 5000).potenciaErgManualW)
    }

    // Al rodar, ciclista y pantalla (design.md del change `rodaje-completo-y-ajustes`, decisión 6)

    private fun v3(lineal: List<Regla>, extra: String = ""): String {
        val texto = Configuracion.aJson(Configuracion(version = 3, lineal = AjustesLineal(asignacion = Asignacion(lineal))))
        // Una versión 3 no tenía los campos nuevos.
        val sin = texto.replace(Regex(",\"(datos|botonesEnPantalla|modoTactil|pesoCiclistaKg)\":(\\[[^\\]]*\\]|[^,}]*)"), "")
        assertTrue("datos" !in sin && "pesoCiclistaKg" !in sin && "botonesEnPantalla" !in sin && "modoTactil" !in sin, sin)
        return if (extra.isEmpty()) sin else sin.dropLast(1) + ",$extra}"
    }

    @Test
    fun valores_por_defecto_de_al_rodar() {
        // Decisión abierta 6 de `funcional.md`.
        assertEquals(listOf(DatoRodaje.POTENCIA, DatoRodaje.CADENCIA), c.datos)
        assertEquals(false, c.botonesEnPantalla)
        assertEquals(false, c.modoTactil)
        assertTrue(c.mostrarAccion && c.vibracion)
        assertEquals(75, c.pesoCiclistaKg)
    }

    @Test
    fun configuracion_anterior_a_al_rodar() {
        // Spec `settings-screen`, «Configuración anterior a "Al rodar"».
        val x = Configuracion.deJson(v3(listOf(trianguloCorta) + Asignacion.ALTERNAR_ERG, "\"pendienteBasePct\":2.0,\"vibracion\":false"))
        assertEquals(Configuracion.VERSION, x.version)
        assertEquals(Configuracion.DATOS_POR_DEFECTO, x.datos)
        assertEquals(false, x.botonesEnPantalla)
        assertEquals(false, x.modoTactil)
        assertEquals(75, x.pesoCiclistaKg)
        assertEquals(false, x.vibracion)
        assertEquals(Accion.MarchaMenos(3), x.asignacion.accion(Entrada(SquareButton.Triangulo), TipoGesto.Corta))
        // Y desde la versión 1 y la 2.
        assertEquals(Configuracion.DATOS_POR_DEFECTO, Configuracion.deJson("""{"version":1,"marchaReferencia":15}""").datos)
        assertEquals(75, Configuracion.deJson(v2(listOf(trianguloCorta))).pesoCiclistaKg)
    }

    @Test
    fun configuracion_anterior_a_las_acciones_de_pantalla() {
        // Spec `button-mapping`, «Configuración anterior a las acciones de pantalla»: la cruceta → ocupada se respeta.
        val ocupada = Regla(Entrada(SquareButton.CrucetaDerecha, SquareButton.CampagnoloIzquierdo), TipoGesto.Pulsar, Accion.MarchaMas(3))
        val x = Configuracion.deJson(v3(listOf(ocupada, Asignacion.ALTERNAR_ERG)))
        assertEquals(Accion.MarchaMas(3), x.asignacion.accion(ocupada.entrada, TipoGesto.Pulsar))
        assertTrue(x.asignacion.reglasDe(Accion.PantallaSiguiente).isEmpty())
        assertEquals(listOf(Asignacion.PANTALLA_ANTERIOR), x.asignacion.reglasDe(Accion.PantallaAnterior))
        // Quitada después por el usuario, no vuelve.
        val sin = x.conAsignacion(x.asignacion.quitar(Asignacion.PANTALLA_ANTERIOR.entrada, TipoGesto.Pulsar))
        assertTrue(Configuracion.deJson(Configuracion.aJson(sin)).asignacion.reglasDe(Accion.PantallaAnterior).isEmpty())
    }

    @Test
    fun datos_ida_y_vuelta() {
        val x = c.copy(datos = listOf(DatoRodaje.TIEMPO, DatoRodaje.POTENCIA, DatoRodaje.W_KG), botonesEnPantalla = true, pesoCiclistaKg = 82)
        val texto = Configuracion.aJson(x)
        assertTrue("\"w-kg\"" in texto && "\"tiempo\"" in texto, texto)
        assertEquals(x, Configuracion.deJson(texto))
    }

    @Test
    fun datos_desconocidos_repetidos_o_vacios() {
        // Un dato de una versión futura se descarta sin perder los demás.
        assertEquals(listOf(DatoRodaje.POTENCIA, DatoRodaje.TIEMPO), Configuracion.deJson("""{"version":4,"datos":["potencia","inventado","tiempo"]}""").datos)
        assertEquals(listOf(DatoRodaje.CADENCIA, DatoRodaje.POTENCIA), Configuracion.deJson("""{"version":4,"datos":["cadencia","potencia","cadencia"]}""").datos)
        assertEquals(Configuracion.DATOS_POR_DEFECTO, Configuracion.deJson("""{"version":4,"datos":[]}""").datos)
        assertEquals(Configuracion.DATOS_POR_DEFECTO, Configuracion.deJson("""{"version":4,"datos":["inventado"]}""").datos)
    }

    @Test
    fun peso_fuera_de_limites_se_recorta_y_la_pendiente_sin_zwift_se_ignora() {
        // Spec `settings-screen`, «Configuración con «Pendiente sin Zwift»».
        val x = Configuracion.deJson("""{"version":4,"pesoCiclistaKg":300,"pendienteBasePct":-20.0}""")
        assertEquals(150, x.pesoCiclistaKg)
        assertTrue("pendienteBasePct" !in Configuracion.aJson(x))
        val y = Configuracion.deJson("""{"version":4,"pesoCiclistaKg":10,"pendienteBasePct":40.0}""")
        assertEquals(40, y.pesoCiclistaKg)
    }

    // Mando de Zwift (design.md del change `mando-zwift-emulado`, decisión 7)

    /** Un documento de la versión 4, sin `mandoZwift`, con [lineal] como asignación de la lineal. */
    private fun v4(lineal: List<Regla>): String {
        val texto = Configuracion.aJson(Configuracion(version = 4, lineal = AjustesLineal(asignacion = Asignacion(lineal))))
        val sin = texto.replace(Regex(",\"plataformas\":\\{[^}]*\\}"), "")
        return sin
    }

    private val aCorta = Regla(Entrada(SquareButton.A), TipoGesto.Corta, Accion.MarchaReferencia)

    @Test
    fun la_version_es_la_12() {
        assertEquals(12, Configuracion.VERSION)
    }

    @Test
    fun una_configuracion_v9_con_el_mando_desactivado_conserva_lo_demas_y_toma_la_conexion_por_defecto() {
        // Spec `settings-screen`, «Mando desactivado guardado»: ya no hay interruptor; se publica si hay un Square configurado.
        val x = Configuracion.deJson("""{"version":9,"pesoCiclistaKg":80,"plataformas":{"zwift":{"modo":"zwift-ride","mando":false}}}""")
        assertEquals(80, x.pesoCiclistaKg)
        assertEquals(AjustesConexion(), x.conexion)
        assertEquals(Configuracion.VERSION, x.version)
        assertTrue("plataformas" !in Configuracion.aJson(x) && "\"mando\"" !in Configuracion.aJson(x))
    }

    @Test
    fun configuracion_anterior_al_mando() {
        // Spec `settings-screen`, «Configuración anterior al mando».
        val x = Configuracion.deJson(v4(Asignacion.POR_DEFECTO.reglas.filterNot { it.accion is Accion.Mando }))
        assertEquals(Configuracion.VERSION, x.version)
        assertEquals(AjustesConexion(), x.conexion)
        assertEquals(Asignacion.POR_DEFECTO.reglas.toSet(), x.asignacion.reglas.toSet())
    }

    @Test
    fun configuracion_anterior_a_las_funciones_de_zwift_con_a_ocupada() {
        // Spec `button-mapping`, «Configuración anterior a las funciones de Zwift».
        val x = Configuracion.deJson(v4(listOf(aCorta)))
        assertEquals(Accion.MarchaReferencia, x.asignacion.accion(Entrada(SquareButton.A), TipoGesto.Corta))
        assertTrue(x.asignacion.reglasDe(Accion.Mando(FuncionZwift.A)).isEmpty())
        FuncionZwift.entries.filterNot { it == FuncionZwift.A }.forEach { f ->
            assertEquals(listOf(Regla(Entrada(f.boton), TipoGesto.Pulsar, Accion.Mando(f))), x.asignacion.reglasDe(Accion.Mando(f)), "$f")
        }
    }

    @Test
    fun funciones_de_zwift_en_los_desarrollos_guardados() {
        val texto = Configuracion.aJson(
            Configuracion(version = 4, platos = mapOf(transmisionShimano.id to AjustesPlatos(asignacion = Asignacion(listOf(aCorta))))),
        )
        val x = Configuracion.deJson(texto)
        assertTrue(x.asignacion(transmisionShimano).reglasDe(Accion.Mando(FuncionZwift.A)).isEmpty())
        assertEquals(Accion.Mando(FuncionZwift.Z), x.asignacion(transmisionShimano).accion(Entrada(SquareButton.Z), TipoGesto.Pulsar))
    }

    @Test
    fun funcion_de_zwift_quitada_por_el_usuario_no_vuelve() {
        val migrada = Configuracion.deJson(v4(listOf(aCorta)))
        val sin = migrada.conAsignacion(migrada.asignacion.quitar(Entrada(SquareButton.Y), TipoGesto.Pulsar))
        val x = Configuracion.deJson(Configuracion.aJson(sin))
        assertTrue(x.asignacion.reglasDe(Accion.Mando(FuncionZwift.Y)).isEmpty())
    }

    @Test
    fun restaurar_devuelve_las_funciones_de_zwift() {
        val migrada = Configuracion.deJson(v4(listOf(aCorta)))
        val restaurada = migrada.conAsignacion(Asignacion.porDefecto(migrada.transmisionElegida))
        assertEquals(listOf(Regla(Entrada(SquareButton.A), TipoGesto.Pulsar, Accion.Mando(FuncionZwift.A))), restaurada.asignacion.reglasDe(Accion.Mando(FuncionZwift.A)))
    }

    @Test
    fun documento_ilegible_da_la_conexion_por_defecto() {
        val x = Configuracion.deJson("{no es json")
        assertEquals(Configuracion(), x)
        assertEquals(AjustesConexion(ModoConexion.FTMS_WIFI, Identidad.SMART_BIKE), x.conexion)
    }

    @Test
    fun el_ventilador_configurado_hace_ida_y_vuelta() {
        val e = EstadoGuardado(ventilador = DispositivoConfigurado("AA:BB:CC:DD:EE:02", "HEADWIND 4F2A"))
        assertEquals(e, EstadoGuardado.deJson(EstadoGuardado.aJson(e)))
    }

    @Test
    fun un_estado_anterior_sin_ventilador_se_lee_igual() {
        val leido = EstadoGuardado.deJson("""{"version":2,"ultimaMarcha":5}""")
        assertEquals(null, leido.ventilador)
        assertEquals(5, leido.ultimaMarcha)
        assertEquals(2, leido.version)
    }

    // Modo de conexión y nombre ante la app (spec `platform-settings`; design.md del change `pestana-conexion`, decisión 1)

    @Test
    fun por_defecto_wifi_mas_bluetooth_y_smart_bike() {
        assertEquals(AjustesConexion(ModoConexion.FTMS_WIFI, Identidad.SMART_BIKE), c.conexion)
        assertEquals(Compatibilidad.recomendado, c.conexion.modo)
    }

    @Test
    fun configuracion_anterior_al_modo_de_conexion() {
        // Spec `settings-screen`, «Configuración anterior al modo».
        val x = Configuracion.deJson("""{"version":5,"pesoCiclistaKg":80,"mandoZwift":false}""")
        assertEquals(ModoConexion.FTMS_WIFI, x.conexion.modo)
        assertEquals(80, x.pesoCiclistaKg)
        assertEquals(Configuracion.VERSION, x.version)
    }

    @Test
    fun modo_de_conexion_ida_y_vuelta() {
        // Spec `settings-screen`, «Modo de conexión guardado».
        val x = c.copy(conexion = AjustesConexion(ModoConexion.ZWIFT_RIDE))
        val json = Configuracion.aJson(x)
        assertTrue("\"zwift-ride\"" in json, json)
        assertEquals(ModoConexion.ZWIFT_RIDE, Configuracion.deJson(json).conexion.modo)
    }

    @Test
    fun modo_de_conexion_desconocido_da_los_valores_por_defecto() {
        assertEquals(Configuracion(), Configuracion.deJson("""{"version":10,"conexion":{"modo":"por-telepatia"}}"""))
    }

    @Test
    fun la_clave_de_identidad_es_estable_y_distinta_por_modo_y_nombre() {
        assertEquals("ftms-wifi/smart-bike", ClaveIdentidad.de(ModoConexion.FTMS_WIFI, Identidad.SMART_BIKE))
        assertEquals("zwift-ride/smart-bike", ClaveIdentidad.de(ModoConexion.ZWIFT_RIDE, Identidad.SMART_BIKE))
        assertEquals("ftms-wifi/kickr-bike-pro", ClaveIdentidad.de(ModoConexion.FTMS_WIFI, Identidad.KICKR_BIKE_PRO))
        assertEquals("ftms-bluetooth/smart-bike", ClaveIdentidad.de(ModoConexion.FTMS_BLUETOOTH, Identidad.SMART_BIKE))
        val todas = ModoConexion.entries.flatMap { m -> Identidad.entries.map { ClaveIdentidad.de(m, it) } }
        assertEquals(todas.size, todas.toSet().size)
    }

    @Test
    fun las_identidades_distintas_se_conservan_por_clave() {
        val a = IdentidadDispositivo.nuevo(kotlin.random.Random(1))
        val b = IdentidadDispositivo.nuevo(kotlin.random.Random(2))
        val ka = ClaveIdentidad.de(ModoConexion.FTMS_WIFI, Identidad.SMART_BIKE)
        val kb = ClaveIdentidad.de(ModoConexion.ZWIFT_RIDE, Identidad.SMART_BIKE)
        val leido = EstadoGuardado.deJson(EstadoGuardado.aJson(EstadoGuardado(identidades = mapOf(ka to a, kb to b))))
        assertEquals(a, leido.identidades[ka])
        assertEquals(b, leido.identidades[kb])
        assertTrue(a != b)
    }

    @Test
    fun un_estado_con_claves_de_identidad_anteriores_se_lee_y_no_las_usa() {
        // Design.md del change `pestana-conexion`, decisión 5: las claves con la plataforma delante ya no se consultan.
        val antigua = IdentidadDispositivo.nuevo(kotlin.random.Random(3))
        val e = EstadoGuardado.deJson(EstadoGuardado.aJson(EstadoGuardado(ultimaMarcha = 9, identidades = mapOf("zwift/ftms-wifi/smart-bike" to antigua))))
        assertEquals(9, e.ultimaMarcha)
        assertEquals(null, e.identidades[ClaveIdentidad.de(ModoConexion.FTMS_WIFI, Identidad.SMART_BIKE)])
    }

    @Test
    fun un_estado_antiguo_con_las_cuatro_identidades_fijas_las_ignora_y_se_lee_igual() {
        val viejo = """{"version":2,"ultimaMarcha":14,"puenteZwift":{"mac":"02:00:00:00:00:01","serie":"1"},""" +
            """"puenteKickrRide":{"mac":"02:00:00:00:00:02","serie":"2"}}"""
        val e = EstadoGuardado.deJson(viejo)
        assertEquals(14, e.ultimaMarcha)
        assertTrue(e.identidades.isEmpty())
    }

    @Test
    fun estado_sin_identidades_se_lee_igual() {
        val e = EstadoGuardado.deJson("""{"version":2,"ultimaMarcha":14}""")
        assertTrue(e.identidades.isEmpty())
    }

    @Test
    fun simular_kickr_bike_esta_desactivado_por_defecto_y_un_documento_v6_lo_lee_asi() {
        assertEquals(false, Configuracion().simularKickrBike)
        val v6 = Configuracion.deJson("""{"version":6,"mandoZwift":false,"conexionZwift":"todo-wifi"}""")
        assertEquals(false, v6.simularKickrBike)
        assertEquals(Identidad.SMART_BIKE, v6.conexion.identidad)
        assertEquals(Configuracion.VERSION, v6.version)
    }

    @Test
    fun la_identidad_ante_la_app_se_conserva_al_releer() {
        val x = Configuracion(conexion = AjustesConexion(identidad = Identidad.KICKR_BIKE_PRO))
        val json = Configuracion.aJson(x)
        assertTrue("\"kickr-bike-pro\"" in json && "simularKickrBike" !in json, json)
        val leida = Configuracion.deJson(json)
        assertEquals(Identidad.KICKR_BIKE_PRO, leida.conexion.identidad)
        assertEquals(true, leida.simularKickrBike)
    }

    @Test
    fun documentos_v8_y_v9_conservan_lo_que_no_es_de_conexion_y_toman_la_conexion_por_defecto() {
        // Spec `platform-settings`, «Configuración de la versión anterior»: sin migración.
        val v8 = Configuracion.deJson(
            """{"version":8,"pesoCiclistaKg":80,"plataforma":"zwift","conexionZwift":"todo-wifi","mandoZwift":false,"identidad":"kickr-bike-pro"}""",
        )
        val v9 = Configuracion.deJson(
            """{"version":9,"pesoCiclistaKg":81,"plataforma":"zwift","plataformas":{"zwift":{"modo":"ftms-bluetooth","identidad":"kickr-bike-pro"}}}""",
        )
        assertEquals(80, v8.pesoCiclistaKg)
        assertEquals(81, v9.pesoCiclistaKg)
        assertEquals(AjustesConexion(), v8.conexion)
        assertEquals(AjustesConexion(), v9.conexion)
        assertEquals(Configuracion.VERSION, v9.version)
    }

    @Test
    fun los_ajustes_de_conexion_hacen_ida_y_vuelta() {
        val a = AjustesConexion(ModoConexion.ZWIFT_RIDE, Identidad.KICKR_BIKE_PRO)
        val leida = Configuracion.deJson(Configuracion.aJson(Configuracion(conexion = a)))
        assertEquals(a, leida.conexion)
    }

    @Test
    fun la_tabla_de_compatibilidad_es_coherente() {
        val modos = Compatibilidad.modos
        assertEquals(
            listOf(ModoConexion.FTMS_WIFI, ModoConexion.FTMS_BLUETOOTH, ModoConexion.ZWIFT_RIDE),
            modos.map { it.modo },
        )
        assertEquals(1, modos.count { it.recomendado })
        assertEquals(ModoConexion.FTMS_WIFI, Compatibilidad.recomendado)
        ModoConexion.entries.forEach { assertEquals(it, Compatibilidad.deModo(it).modo) }
        assertEquals(MandoEnModo.BLUETOOTH_APARTE, Compatibilidad.deModo(ModoConexion.FTMS_WIFI).mando)
        assertEquals(MandoEnModo.DENTRO_DEL_MODO, Compatibilidad.deModo(ModoConexion.ZWIFT_RIDE).mando)
        assertEquals(MandoEnModo.DENTRO_DEL_MODO, Compatibilidad.deModo(ModoConexion.FTMS_BLUETOOTH).mando)
    }

    @Test
    fun la_compatibilidad_solo_nombra_lo_probado() {
        // Spec `platform-settings`, «Compatibilidad en cada modo», «Etiquetas de hoy».
        assertEquals(listOf(Prueba("Zwift", Validacion.VALIDADO)), Compatibilidad.deModo(ModoConexion.FTMS_WIFI).probadoCon)
        assertEquals(listOf(Prueba("Zwift", Validacion.EN_PARTE)), Compatibilidad.deModo(ModoConexion.FTMS_BLUETOOTH).probadoCon)
        assertEquals(listOf(Prueba("Zwift", Validacion.EN_PARTE)), Compatibilidad.deModo(ModoConexion.ZWIFT_RIDE).probadoCon)
        assertEquals(true, Compatibilidad.deModo(ModoConexion.FTMS_WIFI).otrasFtmsAValidar)
        assertEquals(true, Compatibilidad.deModo(ModoConexion.FTMS_BLUETOOTH).otrasFtmsAValidar)
        assertEquals(false, Compatibilidad.deModo(ModoConexion.ZWIFT_RIDE).otrasFtmsAValidar)
    }

    @Test
    fun el_modo_por_bluetooth_no_deja_elegir_el_nombre_ni_es_el_recomendado() {
        val bt = Compatibilidad.deModo(ModoConexion.FTMS_BLUETOOTH)
        assertEquals(Enlace.BLUETOOTH, bt.enlace)
        assertEquals(false, bt.nombreElegible)
        assertEquals(false, bt.recomendado)
        assertEquals(Enlace.WIFI, Compatibilidad.deModo(ModoConexion.FTMS_WIFI).enlace)
        assertEquals(Enlace.WIFI, Compatibilidad.deModo(ModoConexion.ZWIFT_RIDE).enlace)
        assertEquals(true, Compatibilidad.deModo(ModoConexion.ZWIFT_RIDE).nombreElegible)
    }

    @Test
    fun el_modo_por_bluetooth_usa_smart_bike_aunque_se_haya_elegido_kickr_y_lo_conserva_para_los_modos_por_wifi() {
        val wifi = Configuracion(conexion = AjustesConexion(ModoConexion.FTMS_WIFI, Identidad.KICKR_BIKE_PRO))
        assertEquals(Identidad.KICKR_BIKE_PRO, wifi.identidadEfectiva)
        assertEquals(true, wifi.simularKickrBike)
        val bt = wifi.copy(conexion = wifi.conexion.copy(modo = ModoConexion.FTMS_BLUETOOTH))
        assertEquals(Identidad.SMART_BIKE, bt.identidadEfectiva)
        assertEquals(false, bt.simularKickrBike)
        assertEquals(Identidad.KICKR_BIKE_PRO, bt.conexion.identidad)
        val vuelta = bt.copy(conexion = bt.conexion.copy(modo = ModoConexion.FTMS_WIFI))
        assertEquals(Identidad.KICKR_BIKE_PRO, vuelta.identidadEfectiva)
    }

    @Test
    fun una_copia_con_el_modo_por_bluetooth_se_importa() {
        val c = Configuracion(conexion = AjustesConexion(ModoConexion.FTMS_BLUETOOTH))
        val r = Configuracion.validar(Configuracion.aJson(c))
        assertTrue(r is ResultadoImportacion.Valida)
        assertEquals(ModoConexion.FTMS_BLUETOOTH, (r as ResultadoImportacion.Valida).configuracion.conexion.modo)
    }

    @Test
    fun la_grabacion_de_sesiones_esta_apagada_con_el_minimo_en_10() {
        assertEquals(false, Configuracion().grabarSesiones)
        assertEquals(10, Configuracion().espacioLibreMinimoPct)
    }

    @Test
    fun grabacion_de_sesiones_ida_y_vuelta() {
        val x = Configuracion(grabarSesiones = true, espacioLibreMinimoPct = 25)
        assertEquals(x, Configuracion.deJson(Configuracion.aJson(x)))
    }

    @Test
    fun una_configuracion_v10_se_lee_con_la_grabacion_apagada_y_conserva_lo_demas() {
        val v10 = Configuracion.deJson("""{"version":10,"pesoCiclistaKg":81,"tema":"claro"}""")
        assertEquals(false, v10.grabarSesiones)
        assertEquals(10, v10.espacioLibreMinimoPct)
        assertEquals(81, v10.pesoCiclistaKg)
        assertEquals(TemaApp.CLARO, v10.tema)
        assertEquals(Configuracion.VERSION, v10.version)
    }

    @Test
    fun el_espacio_libre_minimo_se_recorta_al_rango() {
        assertEquals(50, Configuracion.deJson("""{"version":11,"espacioLibreMinimoPct":90}""").espacioLibreMinimoPct)
        assertEquals(5, Configuracion.deJson("""{"version":11,"espacioLibreMinimoPct":0}""").espacioLibreMinimoPct)
    }

    @Test
    fun nombre_por_bluetooth_esta_desmarcado_por_defecto_y_hace_ida_y_vuelta() {
        assertEquals(false, Configuracion().conexion.nombrePorBluetooth)
        val c = Configuracion(conexion = AjustesConexion(ModoConexion.FTMS_BLUETOOTH, Identidad.KICKR_BIKE_PRO, nombrePorBluetooth = true))
        assertEquals(c, Configuracion.deJson(Configuracion.aJson(c)))
    }

    @Test
    fun documento_sin_nombre_por_bluetooth_lo_lee_desmarcado_conservando_modo_y_nombre() {
        val c = Configuracion(conexion = AjustesConexion(ModoConexion.FTMS_BLUETOOTH, Identidad.KICKR_BIKE_PRO, nombrePorBluetooth = true))
        val sin = Configuracion.aJson(c).replace(Regex(",?\"nombrePorBluetooth\":(true|false)"), "")
        assertTrue("nombrePorBluetooth" !in sin)
        val leida = Configuracion.deJson(sin)
        assertEquals(AjustesConexion(ModoConexion.FTMS_BLUETOOTH, Identidad.KICKR_BIKE_PRO, false), leida.conexion)
    }

    @Test
    fun identidad_efectiva_en_los_tres_modos_con_el_ajuste_activado_y_desactivado() {
        fun efectiva(modo: ModoConexion, porBt: Boolean) = Configuracion(conexion = AjustesConexion(modo, Identidad.KICKR_BIKE_PRO, porBt)).identidadEfectiva
        assertEquals(Identidad.KICKR_BIKE_PRO, efectiva(ModoConexion.FTMS_WIFI, false))
        assertEquals(Identidad.KICKR_BIKE_PRO, efectiva(ModoConexion.ZWIFT_RIDE, false))
        assertEquals(Identidad.SMART_BIKE, efectiva(ModoConexion.FTMS_BLUETOOTH, false))
        assertEquals(Identidad.KICKR_BIKE_PRO, efectiva(ModoConexion.FTMS_BLUETOOTH, true))
        assertEquals(Identidad.KICKR_BIKE_PRO, efectiva(ModoConexion.FTMS_WIFI, true))
    }

    @Test
    fun el_nombre_bluetooth_original_hace_ida_y_vuelta_y_un_estado_anterior_lo_lee_sin_el() {
        val e = EstadoGuardado(nombreBluetoothOriginal = "OnePlus 8", nombreBluetoothPuesto = "Smart Bike")
        assertEquals(e, EstadoGuardado.deJson(EstadoGuardado.aJson(e)))
        val antes = """{"version":3,"ultimaMarcha":9}"""
        val leido = EstadoGuardado.deJson(antes)
        assertEquals(null, leido.nombreBluetoothOriginal)
        assertEquals(null, leido.nombreBluetoothPuesto)
    }

    @Test
    fun recuperacion_restaura_si_el_actual_es_el_puesto_y_no_toca_si_es_otro() {
        assertEquals(RecuperacionNombre.Restaurar("OnePlus 8"), RecuperacionNombre.decidir("OnePlus 8", "Smart Bike", "Smart Bike"))
        assertEquals(RecuperacionNombre.NoTocar, RecuperacionNombre.decidir("OnePlus 8", "Smart Bike", "Bici"))
        assertEquals(RecuperacionNombre.NoTocar, RecuperacionNombre.decidir("OnePlus 8", "Smart Bike", null))
    }
}
