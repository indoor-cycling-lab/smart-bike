package app.smartbike.ui.diagnostico

import app.smartbike.datos.EntradaRegistro
import app.smartbike.datos.Origen
import app.smartbike.datos.Sentido
import app.smartbike.dominio.Accion
import app.smartbike.dominio.ComandoRodillo
import app.smartbike.dominio.DispositivoConfigurado
import app.smartbike.dominio.Entrada
import app.smartbike.dominio.EstadoMarcha
import app.smartbike.dominio.EstadoVentilador
import app.smartbike.dominio.Gesto
import app.smartbike.dominio.ModoBajadas
import app.smartbike.dominio.ModoControl
import app.smartbike.dominio.ModoVentilador
import app.smartbike.dominio.ResultadoAjuste
import app.smartbike.dominio.ResultadoCambio
import app.smartbike.dominio.Terreno
import app.smartbike.dominio.TipoGesto
import app.smartbike.dominio.TipoModelo
import app.smartbike.dominio.TopeMarcha
import app.smartbike.plataforma.EstadoPlataforma
import app.smartbike.protocolo.FtmsCodec
import app.smartbike.protocolo.SquareButton
import app.smartbike.sesion.EstadoDispositivo
import app.smartbike.sesion.EstadoDispositivo.Buscando
import app.smartbike.sesion.EstadoDispositivo.Conectado
import app.smartbike.sesion.EstadoDispositivo.Conectando
import app.smartbike.sesion.EstadoDispositivo.NoEncontrado
import app.smartbike.sesion.EstadoDispositivo.Reconectando
import app.smartbike.sesion.EstadoDispositivo.SinConexion
import app.smartbike.sesion.EstadoRodaje
import app.smartbike.sesion.FisicoSim
import app.smartbike.sesion.GestoReconocido
import app.smartbike.sesion.MensajePlataforma
import app.smartbike.sesion.Simulacion
import java.time.ZoneOffset
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class DiagnosticoUiStateTest {

    private val salto = GestoReconocido(
        Gesto(Entrada(SquareButton.PalancaDerecha1, SquareButton.CampagnoloDerecho), TipoGesto.Pulsar, 0),
        Accion.MarchaMas(3),
        ResultadoCambio.Cambiada(EstadoMarcha.Numerada(14, 24), EstadoMarcha.Numerada(17, 24), 3),
    )

    /** Estado de la sesión con la marcha 17, enviada al Neo, y [ultimoBoton] si el último gesto fue el salto. */
    private fun rodaje(
        neo: EstadoDispositivo = SinConexion,
        square: EstadoDispositivo = SinConexion,
        bluetoothEncendido: Boolean = true,
        mascara: UInt? = null,
        ultimoBoton: Boolean = false,
        gesto: GestoReconocido? = salto.takeIf { ultimoBoton },
        latenciaMs: Long? = null,
        plataforma: EstadoPlataforma = EstadoPlataforma.Esperando,
        simulacion: Simulacion = Simulacion(0.0, 5.0, ComandoRodillo.Pendiente(5.0)),
    ) = EstadoRodaje(
        neo = neo, square = square, bluetoothEncendido = bluetoothEncendido, mascara = mascara,
        marcha = EstadoMarcha.Numerada(17, 24), referencia = EstadoMarcha.Numerada(12, 24), ultimoGesto = gesto,
        simulacion = simulacion, latenciaMs = latenciaMs, plataforma = plataforma,
    )

    private fun pantalla(c: EstadoRodaje, control: ControlManual = ControlManual()) = estadoDePantalla(c, control, emptyList(), pausado = false)

    // Simulación de Zwift (spec `diagnostics`, «Simulación de Zwift en Simulación»; change `mando-zwift-dircon`)

    private val recibida = app.smartbike.sesion.SimulacionPlataformaRecibida(
        app.smartbike.dominio.SimulacionZwift(
            pendientePct = 4.0,
            relacionVirtual = 1.68,
            crr = 0.004,
            cw = 0.51,
            masaCiclistaKg = 95.0,
            masaBiciKg = 8.51,
        ),
        horaMs = 45_871_000L,
        instanteMs = 1_000L,
    )

    @Test
    fun sin_simulacion_de_zwift_no_hay_fila() {
        assertNull(pantalla(rodaje()).simulacionZwift)
    }

    @Test
    fun la_simulacion_de_zwift_llega_a_la_pantalla_aparte_de_lo_que_simula_el_neo() {
        val c = rodaje(simulacion = Simulacion(0.0, 2.0, ComandoRodillo.Pendiente(6.0), ultimaSimulacionPlataforma = recibida))
        val e = pantalla(c)
        val z = checkNotNull(e.simulacionZwift)
        assertEquals(4.0, z.pendientePct!!, 1e-9)
        assertEquals(1.68, z.relacionMarcha!!, 1e-9)
        assertEquals(0.004, z.crr!!, 1e-9)
        assertEquals(1_000L, z.instanteMs)
        // La sesión de rodaje conserva aparte lo que se envía al Neo: la pendiente de Zwift más lo que suma la marcha.
        assertEquals(ComandoRodillo.Pendiente(6.0), c.simulacion.enviado)
    }

    @Test
    fun la_bateria_del_square_en_su_fila() {
        // Spec `diagnostics`, «Batería del Square en Datos de conexión».
        assertEquals(BateriaFila.Nivel(80, false), pantalla(rodaje(square = Conectado).copy(bateriaSquare = 80)).bateriaSquare)
        assertEquals(BateriaFila.SinLectura, pantalla(rodaje(square = Conectado)).bateriaSquare)
        assertEquals(BateriaFila.Nivel(15, true), pantalla(rodaje(square = Conectado).copy(bateriaSquare = 15)).bateriaSquare)
        assertEquals(BateriaFila.Nivel(20, true), pantalla(rodaje(square = Conectado).copy(bateriaSquare = 20)).bateriaSquare)
        assertEquals(BateriaFila.Ultima(60), pantalla(rodaje(square = SinConexion).copy(bateriaSquare = 60)).bateriaSquare)
        assertEquals(BateriaFila.Ultima(60), pantalla(rodaje(square = Reconectando).copy(bateriaSquare = 60)).bateriaSquare)
        assertEquals(null, pantalla(rodaje(square = SinConexion)).bateriaSquare)
    }

    @Test
    fun todo_conectado_con_dos_botones() {
        val p = pantalla(rodaje(neo = Conectado, square = Conectado, mascara = 0x8002_0000u, ultimoBoton = true))
        assertEquals(setOf(SquareButton.CampagnoloDerecho, SquareButton.PalancaDerecha1), p.pulsados)
        assertEquals("0x80020000", p.mascara)
        assertEquals(listOf(SquareButton.CampagnoloDerecho, SquareButton.PalancaDerecha1), p.ultimoGesto?.botones)
        assertEquals(ResultadoGestoUi.Hecha(Accion.MarchaMas(3), 3), p.ultimoGesto?.resultado)
    }

    @Test
    fun sin_square_no_hay_teclas_ni_mascara() {
        val p = pantalla(rodaje(neo = Conectado, square = Reconectando, mascara = null, ultimoBoton = true))
        assertTrue(p.pulsados.isEmpty())
        assertNull(p.mascara)
        // La última combinación se conserva.
        assertEquals(2, p.ultimoGesto?.botones?.size)
    }

    @Test
    fun sin_pulsaciones_todavia() {
        assertNull(pantalla(rodaje(square = Conectado, mascara = 0u)).ultimoGesto)
    }

    @Test
    fun rodillo_reconectando_no_tiene_enviada() {
        val p = pantalla(rodaje(neo = Reconectando, square = Conectado))
        assertNull(p.simulacion.enviado)
    }

    @Test
    fun valores_iniciales_del_control_manual() {
        val c = ControlManual()
        assertEquals(0.0, c.pendiente)
        assertEquals(150, c.potencia)
    }

    @Test
    fun limites_de_la_pendiente() {
        var c = ControlManual()
        repeat(100) { c = c.pendiente(+1) }
        assertEquals(20.0, c.pendiente)
        assertFalse(c.puedeSubirPendiente)
        assertTrue(c.puedeBajarPendiente)
        repeat(200) { c = c.pendiente(-1) }
        assertEquals(-10.0, c.pendiente)
        assertFalse(c.puedeBajarPendiente)
    }

    @Test
    fun pasos_de_la_pendiente_sin_errores_de_redondeo() {
        var c = ControlManual()
        repeat(7) { c = c.pendiente(+1) }
        assertEquals(3.5, c.pendiente)
    }

    @Test
    fun limites_de_la_potencia() {
        var c = ControlManual()
        repeat(100) { c = c.potencia(-1) }
        assertEquals(50, c.potencia)
        assertFalse(c.puedeBajarPotencia)
        assertTrue(c.puedeSubirPotencia)
        repeat(200) { c = c.potencia(+1) }
        assertEquals(1000, c.potencia)
        assertFalse(c.puedeSubirPotencia)
    }

    @Test
    fun filas_de_trafico() {
        fun trama(t: Long, o: Origen, s: Sentido, ch: String, vararg b: Int) = EntradaRegistro(t, 45_871_208L + t, o, s, ch, ByteArray(b.size) { b[it].toByte() })
        // La más reciente primero, como RegistroSesion.ultimas().
        val entradas = listOf(
            trama(30, Origen.Neo, Sentido.Tx, "fec3", 0xa4, 0x09),
            EntradaRegistro(25, 0, Origen.App, Sentido.Evt, "-", texto = "Conectado"),
            trama(20, Origen.Square, Sentido.Rx, "0x43", 0, 0, 2, 0),
            trama(10, Origen.Square, Sentido.Rx, "0x43", 0, 0, 0, 0),
        )
        val filas = filasTrafico(entradas, ZoneOffset.UTC)

        assertEquals(3, filas.size)
        assertEquals("12:44:31.238", filas[0].hora)
        assertEquals("→ Rodillo", filas[0].origen)
        assertTrue(filas[0].enviada)
        assertEquals("a4 09", filas[0].contenido)
        assertEquals(setOf(2), filas[1].cambiados)
        assertEquals("Square", filas[1].origen)
        assertEquals(emptySet<Int>(), filas[2].cambiados)
    }

    @Test
    fun el_rodillo_recibido_se_llama_rodillo_en_el_trafico() {
        val e = EntradaRegistro(10, 45_871_208L, Origen.Neo, Sentido.Rx, "2a63", byteArrayOf(0x01, 0x02))
        assertEquals("Rodillo", filasTrafico(listOf(e), ZoneOffset.UTC)[0].origen)
    }

    @Test
    fun gesto_sin_accion_y_tope() {
        val a = GestoReconocido(Gesto(Entrada(SquareButton.Y), TipoGesto.Pulsar, 0), null, null)
        assertEquals(ResultadoGestoUi.SinAccion, pantalla(rodaje(square = Conectado, gesto = a)).ultimoGesto?.resultado)
        val t = GestoReconocido(Gesto(Entrada(SquareButton.PalancaDerecha1), TipoGesto.Pulsar, 0), Accion.MarchaMas(1), ResultadoCambio.Tope(TopeMarcha.MasDura))
        assertEquals(ResultadoGestoUi.Tope(TopeMarcha.MasDura), pantalla(rodaje(square = Conectado, gesto = t)).ultimoGesto?.resultado)
    }

    @Test
    fun simulacion_con_neo() {
        val s = pantalla(rodaje(neo = Conectado, latenciaMs = 85)).simulacion
        assertEquals(SimulacionUi(EstadoMarcha.Numerada(17, 24), 0.0, 5.0, ComandoRodillo.Pendiente(5.0), 85), s)
    }

    @Test
    fun simulacion_sin_neo_no_tiene_enviada() {
        val s = pantalla(rodaje(neo = Reconectando)).simulacion
        assertNull(s.enviado)
        assertEquals(EstadoMarcha.Numerada(17, 24), s.marcha)
        assertEquals(5.0, s.ajustePct)
    }

    // --- Bajadas (spec delta de `diagnostics` del change `proteccion-bajadas`) ---

    @Test
    fun marcha_dura_con_el_modo_por_defecto_sin_limite() {
        val s = pantalla(rodaje(neo = Conectado, simulacion = Simulacion(0.0, 5.0, ComandoRodillo.Pendiente(5.0), sinLimitePct = 5.0))).simulacion
        assertEquals(ModoBajadas.LA_MARCHA_NO_CREA_BAJADAS, s.bajadas)
        assertNull(s.sinLimitePct)
    }

    @Test
    fun pendiente_limitada() {
        val sim = Simulacion(0.0, -11.0, ComandoRodillo.Pendiente(-7.0), ModoControl.SimPlataforma(Terreno(-7.0)), sinLimitePct = -18.0)
        assertEquals(-18.0, pantalla(rodaje(neo = Conectado, simulacion = sim)).simulacion.sinLimitePct)
    }

    @Test
    fun limite_que_deja_de_actuar() {
        val sim = Simulacion(0.0, 8.0, ComandoRodillo.Pendiente(1.0), ModoControl.SimPlataforma(Terreno(-7.0)), sinLimitePct = 1.0)
        assertNull(pantalla(rodaje(neo = Conectado, simulacion = sim)).simulacion.sinLimitePct)
    }

    @Test
    fun neo_sin_conexion_muestra_el_modo_y_no_el_limite() {
        val sim = Simulacion(0.0, -11.0, ComandoRodillo.Pendiente(-7.0), bajadas = ModoBajadas.NUNCA_EMPUJAR, sinLimitePct = -18.0)
        val s = pantalla(rodaje(neo = Reconectando, simulacion = sim)).simulacion
        assertEquals(ModoBajadas.NUNCA_EMPUJAR, s.bajadas)
        assertNull(s.sinLimitePct)
        assertNull(s.enviado)
    }

    // --- Modelos (spec delta de `diagnostics` del change `modelos-tabla-y-fisico`) ---

    private fun fisico(calculadaW: Int, enviadaW: Int) = Simulacion(
        0.0,
        0.0,
        ComandoRodillo.Potencia(enviadaW),
        tipoModelo = TipoModelo.FISICO,
        fisico = FisicoSim(calculadaW, 30, 90, 24.2, 2.12),
    )

    @Test
    fun fisico_rodando() {
        val s = pantalla(rodaje(neo = Conectado, simulacion = fisico(156, 156))).simulacion
        assertEquals(TipoModelo.FISICO, s.modelo)
        assertEquals(156, s.fisico?.calculadaW)
        assertEquals(30, s.fisico?.minimaW)
        assertEquals(24.2, s.fisico?.velocidadKmh)
        assertNull(s.sinLimiteW)
    }

    @Test
    fun potencia_minima_actuando() {
        val s = pantalla(rodaje(neo = Conectado, simulacion = fisico(12, 30))).simulacion
        assertEquals(12, s.sinLimiteW)
    }

    @Test
    fun fisico_sin_neo_no_indica_limite() {
        val s = pantalla(rodaje(neo = Reconectando, simulacion = fisico(12, 30))).simulacion
        assertNull(s.sinLimiteW)
        assertNull(s.enviado)
    }

    @Test
    fun por_tabla_y_dificultad() {
        val sim = Simulacion(0.0, 7.0, ComandoRodillo.Pendiente(8.4), tipoModelo = TipoModelo.TABLA, dificultadPct = 120)
        val s = pantalla(rodaje(neo = Conectado, simulacion = sim)).simulacion
        assertEquals(TipoModelo.TABLA, s.modelo)
        assertEquals(7.0, s.ajustePct)
        assertEquals(120, s.dificultadPct)
        assertNull(s.fisico)
    }

    // --- Zwift (spec delta de `diagnostics`) ---

    @Test
    fun la_app_de_entrenamiento_llega_con_su_estado() {
        assertEquals(EstadoPlataforma.Esperando, pantalla(rodaje()).plataforma)
        assertEquals(EstadoPlataforma.Conectado("192.168.0.10"), pantalla(rodaje(plataforma = EstadoPlataforma.Conectado("192.168.0.10"))).plataforma)
        assertEquals(EstadoPlataforma.SinWifi, pantalla(rodaje(neo = Conectado, square = Conectado, plataforma = EstadoPlataforma.SinWifi)).plataforma)
    }

    private val terreno = Terreno(4.0, 0.004, 0.51, 0.0)

    @Test
    fun simulacion_en_una_ruta_de_zwift() {
        val sim = Simulacion(
            0.0,
            2.0,
            ComandoRodillo.Pendiente(6.0),
            ModoControl.SimPlataforma(terreno),
            ultimoPlataforma = MensajePlataforma(FtmsCodec.Comando.Simulacion(0.0, 4.0, 0.004, 0.51), 1_000L),
        )
        val s = pantalla(rodaje(neo = Conectado, plataforma = EstadoPlataforma.Conectado("x"), simulacion = sim)).simulacion
        assertEquals(ModoUi.SimPlataforma, s.modo)
        assertEquals(terreno, s.terreno)
        assertEquals(ComandoRodillo.Pendiente(6.0), s.enviado)
        assertEquals(1_000L, s.ultimoPlataforma?.horaMs)
        assertNull(s.potenciaPlataformaW)
    }

    @Test
    fun simulacion_en_un_entrenamiento_de_zwift() {
        val sim = Simulacion(0.0, 5.0, ComandoRodillo.Potencia(240), ModoControl.ErgPlataforma(230, 10))
        val s = pantalla(rodaje(neo = Conectado, simulacion = sim)).simulacion
        assertEquals(ModoUi.ErgPlataforma, s.modo)
        assertEquals(230, s.potenciaPlataformaW)
        assertEquals(10, s.ajustePalancasW)
        assertEquals(ComandoRodillo.Potencia(240), s.enviado)
        assertEquals(EstadoMarcha.Numerada(17, 24), s.marcha)
    }

    @Test
    fun simulacion_en_erg_manual() {
        // Spec `diagnostics`, «ERG manual»: fijada 220 W, entrada 200 W, enviada 220 W y la marcha visible.
        val sim = Simulacion(0.0, 5.0, ComandoRodillo.Potencia(220), ModoControl.ErgManual(200, 20, ModoControl.SimLibre))
        val s = pantalla(rodaje(neo = Conectado, simulacion = sim)).simulacion
        assertEquals(ModoUi.ErgManual, s.modo)
        assertEquals(220, s.potenciaFijadaW)
        assertEquals(200, s.entradaW)
        assertNull(s.potenciaPlataformaW)
        assertEquals(ComandoRodillo.Potencia(220), s.enviado)
        assertEquals(EstadoMarcha.Numerada(17, 24), s.marcha)
    }

    @Test
    fun simulacion_en_erg_manual_sobre_una_ruta() {
        val ultimo = MensajePlataforma(FtmsCodec.Comando.Simulacion(0.0, 3.0, 0.004, 0.51), 2_000L)
        val sim = Simulacion(0.0, 5.0, ComandoRodillo.Potencia(200), ModoControl.ErgManual(200, 0, ModoControl.SimPlataforma(terreno)), ultimoPlataforma = ultimo)
        val s = pantalla(rodaje(neo = Conectado, plataforma = EstadoPlataforma.Conectado("x"), simulacion = sim)).simulacion
        assertEquals(ModoUi.ErgManual, s.modo)
        assertEquals(ultimo, s.ultimoPlataforma)
        assertNull(s.terreno)
        assertEquals(ComandoRodillo.Potencia(200), s.enviado)
    }

    @Test
    fun ultimo_gesto_en_erg_manual() {
        val sim = Simulacion(0.0, 5.0, null, ModoControl.ErgManual(200, 0, ModoControl.SimLibre))
        val quitado = salto.copy(resultado = null, ajuste = ResultadoAjuste.Quitado)
        assertEquals(ResultadoGestoUi.VueltaA(200), pantalla(rodaje(square = Conectado, gesto = quitado, simulacion = sim)).ultimoGesto?.resultado)
        val alternar = GestoReconocido(Gesto(Entrada(SquareButton.Triangulo), TipoGesto.Larga, 0), Accion.AlternarErgManual, null)
        assertEquals(ResultadoGestoUi.Hecha(Accion.AlternarErgManual, null), pantalla(rodaje(square = Conectado, gesto = alternar)).ultimoGesto?.resultado)
    }

    @Test
    fun simulacion_con_envio_manual_sigue_mostrando_zwift() {
        val sim = Simulacion(0.0, 5.0, ComandoRodillo.Pendiente(3.0), ModoControl.Manual(ComandoRodillo.Pendiente(3.0), ModoControl.SimPlataforma(terreno)))
        val s = pantalla(rodaje(neo = Conectado, simulacion = sim)).simulacion
        assertEquals(ModoUi.Manual, s.modo)
        assertEquals(terreno, s.terreno)
    }

    @Test
    fun zwift_en_espera() {
        val sim = Simulacion(0.0, 5.0, null, ModoControl.SimPlataforma(terreno), plataformaEnEspera = true)
        assertTrue(pantalla(rodaje(simulacion = sim)).simulacion.plataformaEnEspera)
    }

    @Test
    fun ultimo_gesto_en_erg() {
        val g = salto.copy(resultado = null, ajuste = ResultadoAjuste.Cambiado(-30, -30))
        assertEquals(ResultadoGestoUi.Vatios(-30), pantalla(rodaje(square = Conectado, gesto = g)).ultimoGesto?.resultado)
        val tope = salto.copy(resultado = null, ajuste = ResultadoAjuste.Tope(50, maximo = false))
        assertEquals(ResultadoGestoUi.TopePotencia(50), pantalla(rodaje(square = Conectado, gesto = tope)).ultimoGesto?.resultado)
        val quitado = salto.copy(resultado = null, ajuste = ResultadoAjuste.Quitado)
        assertEquals(ResultadoGestoUi.PotenciaZwift, pantalla(rodaje(square = Conectado, gesto = quitado)).ultimoGesto?.resultado)
    }

    @Test
    fun pulsacion_larga_fija_la_referencia() {
        // Spec `diagnostics`, «Pulsación larga»: «□ · larga · Fijar la marcha de referencia».
        val g = GestoReconocido(Gesto(Entrada(SquareButton.Cuadrado), TipoGesto.Larga, 0), Accion.FijarReferencia, ResultadoCambio.ReferenciaFijada(EstadoMarcha.Numerada(15, 24)))
        val u = pantalla(rodaje(square = Conectado, gesto = g)).ultimoGesto
        assertEquals(UltimoGestoUi(listOf(SquareButton.Cuadrado), TipoGesto.Larga, ResultadoGestoUi.Hecha(Accion.FijarReferencia, null)), u)
    }

    @Test
    fun repeticion_y_corta_reasignada() {
        val r = GestoReconocido(
            Gesto(Entrada(SquareButton.PalancaDerecha1), TipoGesto.Repetir, 0),
            Accion.MarchaMas(1),
            ResultadoCambio.Cambiada(EstadoMarcha.Numerada(14, 24), EstadoMarcha.Numerada(15, 24), 1),
        )
        assertEquals(TipoGesto.Repetir, pantalla(rodaje(square = Conectado, gesto = r)).ultimoGesto?.gesto)
        val c = GestoReconocido(
            Gesto(Entrada(SquareButton.Triangulo), TipoGesto.Corta, 0),
            Accion.MarchaMenos(3),
            ResultadoCambio.Cambiada(EstadoMarcha.Numerada(14, 24), EstadoMarcha.Numerada(11, 24), -3),
        )
        val u = pantalla(rodaje(square = Conectado, gesto = c)).ultimoGesto
        assertEquals(UltimoGestoUi(listOf(SquareButton.Triangulo), TipoGesto.Corta, ResultadoGestoUi.Hecha(Accion.MarchaMenos(3), -3)), u)
    }

    @Test
    fun shimano() {
        // Spec `diagnostics`, «Shimano» y «Plato con la Shimano».
        val m = EstadoMarcha.PlatoPinon(52, 17)
        val s = pantalla(rodaje(neo = Conectado).copy(marcha = m)).simulacion
        assertEquals(m, s.marcha)
        assertEquals(3.06, m.relacion, 0.005)
        val plato = GestoReconocido(
            Gesto(Entrada(SquareButton.PalancaIzquierda1), TipoGesto.Pulsar, 0),
            Accion.PlatoMas,
            ResultadoCambio.Cambiada(EstadoMarcha.PlatoPinon(36, 17), EstadoMarcha.PlatoPinon(52, 21), 7),
        )
        val u = pantalla(rodaje(square = Conectado, gesto = plato)).ultimoGesto
        assertEquals(UltimoGestoUi(listOf(SquareButton.PalancaIzquierda1), TipoGesto.Pulsar, ResultadoGestoUi.Plato(52)), u)
        val tope = GestoReconocido(Gesto(Entrada(SquareButton.PalancaIzquierda1), TipoGesto.Pulsar, 0), Accion.PlatoMas, ResultadoCambio.Tope(TopeMarcha.Plato(52)))
        assertEquals(ResultadoGestoUi.Tope(TopeMarcha.Plato(52)), pantalla(rodaje(square = Conectado, gesto = tope)).ultimoGesto?.resultado)
    }

    @Test
    fun `el hexadecimal del volcado va en minúsculas y separado por espacios`() {
        assertEquals("a4 09 4e 00 ff", hex(byteArrayOf(0xa4.toByte(), 0x09, 0x4e, 0x00, 0xff.toByte())))
        assertEquals("", hex(ByteArray(0)))
    }

    // --- Pulso en Datos de conexión (spec `diagnostics`; change `pulso-zwift`) ---

    private val banda = DispositivoConfigurado("CC:CC", "COOSPO HW9")

    @Test
    fun sin_pulsometro_configurado_no_hay_fila() {
        val e = pantalla(rodaje(Conectado, Conectado).copy(pulsometro = Conectado))
        assertNull(e.pulsometro)
    }

    @Test
    fun banda_dando_pulso() {
        val e = pantalla(rodaje(Conectado, Conectado).copy(pulsometro = Conectado, pulsometroConfigurado = banda, pulso = 128, pulsoInstanteMs = 9_000))
        assertEquals(FilaPulsometroUi(128, 9_000), e.pulsometro)
        assertEquals(2, pulsoHaceS(9_000, 11_400))
    }

    @Test
    fun sin_pulso_no_hay_bpm() {
        val e = pantalla(rodaje(Conectado, Conectado).copy(pulsometro = Conectado, pulsometroConfigurado = banda))
        assertEquals(FilaPulsometroUi(null, null), e.pulsometro)
    }

    @Test
    fun banda_reconectando_no_muestra_el_ultimo_pulso() {
        val e = pantalla(rodaje(Conectado, Conectado).copy(pulsometro = Reconectando, pulsometroConfigurado = banda, pulso = 128, pulsoInstanteMs = 9_000))
        assertEquals(FilaPulsometroUi(null, null), e.pulsometro)
    }

    @Test
    fun la_antiguedad_no_baja_de_cero() = assertEquals(0, pulsoHaceS(5_000, 4_000))

    // --- Ventilador en Datos de conexión (spec `diagnostics`; change `ventilador-headwind`) ---

    private val ventiladorConfigurado = DispositivoConfigurado("DD:DD", "HEADWIND 4F2A")

    @Test
    fun sin_ventilador_configurado_no_hay_fila() {
        val e = pantalla(rodaje(Conectado, Conectado).copy(ventilador = Conectado))
        assertNull(e.ventilador)
    }

    @Test
    fun ventilador_con_modo_y_velocidad() {
        val estado = EstadoVentilador(ModoVentilador.Manual, 40, 40)
        val e = pantalla(rodaje(Conectado, Conectado).copy(ventilador = Conectado, ventiladorConfigurado = ventiladorConfigurado, ventiladorEstado = estado, ventiladorInstanteMs = 9_000))
        assertEquals(FilaVentiladorUi(estado, 9_000), e.ventilador)
    }

    @Test
    fun ventilador_sin_notificacion() {
        val e = pantalla(rodaje(Conectado, Conectado).copy(ventilador = Conectado, ventiladorConfigurado = ventiladorConfigurado))
        assertEquals(FilaVentiladorUi(null, null), e.ventilador)
    }

    @Test
    fun ventilador_reconectando_conserva_lo_ultimo_conocido() {
        val estado = EstadoVentilador(ModoVentilador.Manual, 40, 40)
        val e = pantalla(rodaje(Conectado, Conectado).copy(ventilador = Reconectando, ventiladorConfigurado = ventiladorConfigurado, ventiladorEstado = estado, ventiladorInstanteMs = 1_000))
        assertEquals(FilaVentiladorUi(estado, 1_000), e.ventilador)
    }

    @Test
    fun el_nombre_anunciado_llega_a_la_pantalla_y_por_defecto_es_square_shift() {
        assertEquals("Smart Bike", pantalla(rodaje()).nombreAnunciado)
        assertEquals("KICKR BIKE PRO 669C", pantalla(rodaje().copy(nombreAnunciado = "KICKR BIKE PRO 669C")).nombreAnunciado)
    }

    @Test
    fun por_bluetooth_el_estado_de_la_app_de_entrenamiento_es_el_del_bluetooth() {
        // Spec `ftms-bluetooth`: con el modo por Bluetooth el wifi no cuenta.
        for (z in listOf(EstadoPlataforma.SinBluetooth, EstadoPlataforma.NoDisponible)) {
            val p = pantalla(rodaje(neo = Conectado, square = Conectado, plataforma = z))
            assertEquals(z, p.plataforma)
        }
    }

    @Test
    fun el_enlace_del_modo_llega_a_diagnostico() {
        assertEquals(app.smartbike.dominio.Enlace.WIFI, pantalla(rodaje()).enlace)
        assertEquals(app.smartbike.dominio.Enlace.BLUETOOTH, pantalla(rodaje().copy(enlaceConexion = app.smartbike.dominio.Enlace.BLUETOOTH)).enlace)
    }
}
