package app.smartbike.ui.diagnostico

import android.os.SystemClock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.dp
import app.smartbike.dominio.Accion
import app.smartbike.dominio.ComandoRodillo
import app.smartbike.dominio.DispositivoConfigurado
import app.smartbike.dominio.Entrada
import app.smartbike.dominio.EstadoMarcha
import app.smartbike.dominio.EstadoVentilador
import app.smartbike.dominio.Gesto
import app.smartbike.dominio.ModoControl
import app.smartbike.dominio.ModoVentilador
import app.smartbike.dominio.ResultadoCambio
import app.smartbike.dominio.SimulacionZwift
import app.smartbike.dominio.Terreno
import app.smartbike.dominio.TipoGesto
import app.smartbike.plataforma.EstadoMando
import app.smartbike.plataforma.EstadoPlataforma
import app.smartbike.protocolo.FtmsCodec
import app.smartbike.protocolo.SquareButton
import app.smartbike.sesion.Envio
import app.smartbike.sesion.EstadoDispositivo
import app.smartbike.sesion.EstadoDispositivo.Buscando
import app.smartbike.sesion.EstadoDispositivo.Conectado
import app.smartbike.sesion.EstadoDispositivo.NoEncontrado
import app.smartbike.sesion.EstadoDispositivo.Reconectando
import app.smartbike.sesion.EstadoDispositivo.SinConexion
import app.smartbike.sesion.EstadoRodaje
import app.smartbike.sesion.GestoReconocido
import app.smartbike.sesion.MensajePlataforma
import app.smartbike.sesion.Simulacion
import app.smartbike.sesion.SimulacionPlataformaRecibida
import app.smartbike.ui.componentes.Enlace
import app.smartbike.ui.componentes.FilaHex
import app.smartbike.ui.componentes.etiquetaIcono
import app.smartbike.ui.createComposeRuleEs
import app.smartbike.ui.tema.SqTema
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** Un test por estado de maquetas/diagnostico.html («rodando» es la referencia a largo plazo, con transmisión de marca). */
class DiagnosticoTest {
    @get:Rule
    val compose = createComposeRuleEs()

    private val trafico = listOf(
        FilaHex("12:04:31.236", "→ Rodillo", "fec3", "a4 09 4e 05 31 ff ff ff ff ff 20 03 0b", enviada = true),
        FilaHex("12:04:31.208", "Square", "0x43", "0a 1b 2c 80 02 00 00 ff"),
    )

    private val salto = GestoReconocido(
        Gesto(Entrada(SquareButton.PalancaDerecha1, SquareButton.CampagnoloDerecho), TipoGesto.Pulsar, 0),
        Accion.MarchaMas(3),
        ResultadoCambio.Cambiada(EstadoMarcha.Numerada(14, 24), EstadoMarcha.Numerada(17, 24), 3),
    )

    /** La sesión en la marcha 17, como en la maqueta; con [ultimoBoton], el último gesto es el salto. */
    private fun rodaje(
        neo: EstadoDispositivo = SinConexion,
        square: EstadoDispositivo = SinConexion,
        bluetoothEncendido: Boolean = true,
        mascara: UInt? = null,
        ultimoBoton: Boolean = false,
        ultimoEnvio: Envio? = null,
        gesto: GestoReconocido? = salto.takeIf { ultimoBoton },
        enviado: ComandoRodillo? = ComandoRodillo.Pendiente(5.0),
        zwift: EstadoPlataforma = EstadoPlataforma.Esperando,
        simulacion: Simulacion = Simulacion(0.0, 5.0, enviado),
    ) = EstadoRodaje(
        neo = neo, square = square, bluetoothEncendido = bluetoothEncendido, mascara = mascara,
        marcha = EstadoMarcha.Numerada(17, 24), referencia = EstadoMarcha.Numerada(12, 24), ultimoGesto = gesto,
        simulacion = simulacion, ultimoEnvio = ultimoEnvio, plataforma = zwift,
    )

    private fun mostrar(
        c: EstadoRodaje,
        control: ControlManual = ControlManual(),
        filas: List<FilaHex> = emptyList(),
        acciones: AccionesDiagnostico = AccionesDiagnostico(),
        traficoVisible: Boolean = true,
        nombreMovil: String? = null,
    ) {
        compose.setContent { SqTema { Diagnostico(estadoDePantalla(c, control, filas, pausado = false, traficoVisible, nombreMovil), acciones) } }
    }

    /** La fila «Tráfico en directo», que se anuncia como casilla con su nombre. */
    private fun casillaTrafico() = compose.onNode(isToggleable() and hasText("Tráfico en directo")).performScrollTo()

    private fun texto(t: String) = compose.onNodeWithText(t).performScrollTo()

    private fun avisoBateria() = compose.onAllNodesWithText("Batería del Square baja", substring = true)

    @Test
    fun bateria_del_square_en_su_fila() {
        // Spec `diagnostics`, «Batería del Square en Datos de conexión»; estado `bateria-ok` de la maqueta.
        val c = rodaje(neo = Conectado, square = Conectado, mascara = 0u)
        mostrar(c.copy(bateriaSquare = 80))
        texto("Batería del Square")
        texto("80 %")
        avisoBateria().assertCountEquals(0)
    }

    @Test
    fun bateria_sin_lectura_todavia() {
        mostrar(rodaje(neo = Conectado, square = Conectado, mascara = 0u))
        texto("Sin lectura todavía")
        avisoBateria().assertCountEquals(0)
    }

    @Test
    fun bateria_baja_se_dice_en_la_fila_sin_aviso_y_se_recupera() {
        var nivel by mutableStateOf(15)
        compose.setContent {
            SqTema {
                Diagnostico(estadoDePantalla(rodaje(neo = Conectado, square = Conectado, mascara = 0u).copy(bateriaSquare = nivel), ControlManual(), emptyList(), pausado = false), AccionesDiagnostico())
            }
        }
        texto("15 % · baja")
        avisoBateria().assertCountEquals(0)
        nivel = 100
        compose.waitForIdle()
        texto("100 %")
        compose.onNodeWithText("15 % · baja").assertDoesNotExist()
    }

    @Test
    fun square_sin_conexion_con_la_ultima_bateria() {
        mostrar(rodaje(neo = Conectado, square = Reconectando).copy(bateriaSquare = 60))
        texto("Última lectura: 60 %")
        avisoBateria().assertCountEquals(0)
    }

    @Test
    fun sin_square_configurado_no_hay_fila_de_bateria() {
        mostrar(rodaje(neo = Conectado, square = SinConexion))
        compose.onNodeWithText("Batería del Square").assertDoesNotExist()
        compose.onAllNodesWithText("atería", substring = true).assertCountEquals(0)
    }

    @Test
    fun la_app_de_entrenamiento_con_su_direccion_y_sin_estado_de_conexion() {
        // Spec `diagnostics`, «Datos de la app de entrenamiento en Diagnóstico»; estado `rodando` de la maqueta.
        mostrar(rodaje(neo = Conectado, square = Conectado, mascara = 0u, zwift = EstadoPlataforma.Conectado("192.168.0.10")).copy(mando = EstadoMando.Conectado))
        texto("App de entrenamiento")
        texto("Desde 192.168.0.10")
        compose.onNodeWithText("Mando de Zwift").assertDoesNotExist()
        compose.onAllNodesWithText("Conectado", useUnmergedTree = true).assertCountEquals(0)
    }

    @Test
    fun la_app_de_entrenamiento_por_bluetooth() {
        // Estado `bt-rodando` de la maqueta.
        mostrar(
            rodaje(neo = Conectado, square = Conectado, mascara = 0u, zwift = EstadoPlataforma.Conectado("")).copy(enlaceConexion = app.smartbike.dominio.Enlace.BLUETOOTH),
            nombreMovil = "OnePlus 8",
        )
        texto("Por Bluetooth")
        texto("OnePlus 8 · nombre Bluetooth del móvil")
    }

    @Test
    fun sin_app_conectada_dice_raya_y_no_explica_como_emparejar() {
        mostrar(rodaje(neo = Conectado, square = Conectado, mascara = 0u))
        texto("App de entrenamiento")
        compose.onNodeWithText("Elige Smart Bike", substring = true).assertDoesNotExist()
        compose.onNodeWithText("Esperando", useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun la_simulacion_de_zwift_va_en_simulacion_y_no_en_datos_de_conexion() {
        // Spec `diagnostics`, «Simulación de Zwift en Simulación»; estado `zwift-wifi` de la maqueta.
        val simulacion = Simulacion(
            0.0,
            2.0,
            ComandoRodillo.Pendiente(6.0),
            ultimaSimulacionPlataforma = SimulacionPlataformaRecibida(
                SimulacionZwift(pendientePct = 4.0, relacionVirtual = 1.68, crr = 0.004, cw = 0.51, masaCiclistaKg = 95.0, masaBiciKg = 8.51),
                horaMs = 0L,
                instanteMs = SystemClock.elapsedRealtime(),
            ),
        )
        mostrar(rodaje(neo = Conectado, square = Conectado, mascara = 0u, zwift = EstadoPlataforma.Conectado("192.168.0.10"), simulacion = simulacion))
        texto("Simulación de Zwift")
        compose.onNodeWithText("Pendiente 4,0 %", substring = true).performScrollTo().assertExists()
        compose.onNodeWithText("relación de su marcha 1,68", substring = true).assertExists()
        compose.onNodeWithText("Crr 0,004", substring = true).assertExists()
        compose.onNodeWithText("hace 0 s", substring = true).assertExists()
        val y = { t: String -> compose.onNodeWithText(t).fetchSemanticsNode().positionInRoot.y }
        assertTrue(y("Datos de conexión") < y("Botones del Square"))
        assertTrue(y("Botones del Square") < y("Simulación de Zwift"))
    }

    @Test
    fun sin_simulacion_de_zwift_no_aparece_la_fila() {
        mostrar(rodaje(neo = Conectado, square = Conectado, mascara = 0u, zwift = EstadoPlataforma.Conectado("192.168.0.10")))
        compose.onNodeWithText("Simulación de Zwift").assertDoesNotExist()
    }

    @Test
    fun cada_dato_lleva_su_icono_y_ninguno_el_de_zwift_ni_el_del_rodillo() {
        mostrar(
            rodaje(neo = Conectado, square = Conectado, mascara = 0u, zwift = EstadoPlataforma.Esperando)
                .copy(
                    bateriaSquare = 80,
                    pulsometro = Conectado,
                    pulsometroConfigurado = DispositivoConfigurado("CC:CC", "COOSPO HW9"),
                    ventilador = Conectado,
                    ventiladorConfigurado = DispositivoConfigurado("DD:DD", "HEADWIND 4F2A"),
                ),
        )
        for (tag in listOf(ETIQUETA_ICONO_APP, ETIQUETA_ICONO_NOMBRE, etiquetaIcono(Enlace.Square) + "-bateria", etiquetaIcono(Enlace.Pulso), etiquetaIcono(Enlace.Ventilador))) {
            compose.onNodeWithTag(tag, useUnmergedTree = true).performScrollTo().assertExists()
        }
        for (e in listOf(Enlace.Neo, Enlace.Mando)) compose.onNodeWithTag(etiquetaIcono(e), useUnmergedTree = true).assertDoesNotExist()
        // El icono de Zwift solo lo lleva la simulación de Zwift, que aquí no ha llegado.
        compose.onNodeWithTag(etiquetaIcono(Enlace.Zwift) + "-simulacion", useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun conectado() {
        mostrar(
            rodaje(
                neo = Conectado,
                square = Conectado,
                mascara = 0x8002_0000u,
                ultimoBoton = true,
                ultimoEnvio = Envio.Potencia(200, 0L),
            ),
            control = ControlManual(pendienteMedios = 6, potencia = 200),
            filas = trafico,
        )
        compose.onAllNodesWithText("Conectado", useUnmergedTree = true).assertCountEquals(0)
        compose.onAllNodesWithText("Palanca D1")[0].assertIsSelected()
        compose.onAllNodesWithText("Campag. D")[0].assertIsSelected()
        compose.onAllNodesWithText("Palanca I1")[0].assertIsNotSelected()
        texto("0x80020000")
        texto("Se iluminan mientras los pulsas.")
        texto("→ Rodillo")
        texto("3,0 %")
        texto("Enviar pend.").assertIsEnabled()
        texto("ÚLTIMO ENVÍO")
        texto("ÚLTIMO GESTO")
        texto("pulsar")
        texto("+3 marchas")
        texto("Simulación")
        // Sin app de entrenamiento, el modo se llama Libre (spec `diagnostics`).
        compose.onAllNodesWithText("Libre")[0].assertExists()
        texto("PENDIENTE BASE")
        // Ajuste por la marcha y lo enviado al rodillo.
        compose.onAllNodesWithText("+5,0").assertCountEquals(2)
        texto("LATENCIA PALANCA")
    }

    @Test
    fun volver_a_ajustes() {
        // Spec `diagnostics`, «Volver a Ajustes».
        var volver = 0
        mostrar(rodaje(), acciones = AccionesDiagnostico(volver = { volver++ }))
        compose.onNodeWithContentDescription("Volver a Ajustes").performClick()
        assertEquals(1, volver)
    }

    @Test
    fun boton_sin_accion() {
        val y = GestoReconocido(Gesto(Entrada(SquareButton.Y), TipoGesto.Pulsar, 0), null, null)
        mostrar(rodaje(neo = Conectado, square = Conectado, mascara = 0u, gesto = y))
        texto("sin acción")
    }

    @Test
    fun diagnostico_no_tiene_ni_la_busqueda_ni_los_avisos_de_conexion() {
        // Spec `diagnostics`, «Datos de conexión»: el estado y las acciones de conexión están en Dispositivos y en Conexión.
        mostrar(rodaje(neo = Buscando, square = Buscando))
        compose.onNodeWithText("Buscar dispositivos").assertDoesNotExist()
        compose.onNodeWithText("Activar Bluetooth").assertDoesNotExist()
        compose.onNodeWithText("Abrir ajustes").assertDoesNotExist()
        compose.onAllNodesWithText("Buscando", useUnmergedTree = true).assertCountEquals(0)
        texto("Ninguno todavía")
        texto("Sin tráfico todavía")
        texto("0,0 %")
        texto("150 W")
        texto("Enviar pend.").assertIsNotEnabled()
        texto("Conecta el rodillo para enviarle una pendiente o una potencia.")
    }

    @Test
    fun rodillo_no_encontrado_sin_aviso_y_con_el_envio_deshabilitado() {
        mostrar(rodaje(neo = NoEncontrado, square = Conectado, mascara = 0u))
        compose.onNodeWithText("No se encuentra el Neo", substring = true).assertDoesNotExist()
        compose.onNodeWithText("No encontrado", useUnmergedTree = true).assertDoesNotExist()
        texto("Enviar potencia").assertIsNotEnabled()
    }

    @Test
    fun square_reconectando_sin_aviso_y_sin_teclas_pulsadas() {
        mostrar(rodaje(neo = Conectado, square = Reconectando, mascara = null, ultimoBoton = true))
        compose.onNodeWithText("Reconectando", substring = true, useUnmergedTree = true).assertDoesNotExist()
        texto("Conecta el Square para ver sus botones.")
        compose.onAllNodesWithText("Palanca D1")[0].assertIsNotSelected()
        texto("Enviar pend.").assertIsEnabled()
    }

    @Test
    fun bluetooth_apagado_y_sin_permiso_no_dicen_nada_en_diagnostico() {
        mostrar(rodaje(neo = SinConexion, square = SinConexion, bluetoothEncendido = false))
        compose.onNodeWithText("El Bluetooth está apagado", substring = true).assertDoesNotExist()
        compose.onNodeWithText("Sin permiso", substring = true).assertDoesNotExist()
        compose.onAllNodesWithText("Sin conexión", useUnmergedTree = true).assertCountEquals(0)
    }

    @Test
    fun pulsacion_larga_sin_enlaces_a_otras_pestanas() {
        // Estado `limites` de la maqueta: «□ · larga · Fijar la marcha de referencia». Spec `diagnostics`, «Sin enlaces
        // a otras pestañas».
        val larga = GestoReconocido(Gesto(Entrada(SquareButton.Cuadrado), TipoGesto.Larga, 0), Accion.FijarReferencia, ResultadoCambio.ReferenciaFijada(EstadoMarcha.Numerada(17, 24)))
        mostrar(rodaje(neo = Conectado, square = Conectado, mascara = 0u, gesto = larga))
        texto("larga")
        texto("Fijar la marcha de referencia")
        assertEquals(0, compose.onAllNodesWithText("Asignación de botones").fetchSemanticsNodes().size)
        assertEquals(0, compose.onAllNodesWithText("Ajustes").fetchSemanticsNodes().size)
    }

    @Test
    fun bajada_limitada() {
        // Estado `bajada-limitada` de la maqueta: Zwift −7 % en la marcha 1, con «La marcha no crea bajadas».
        val sim = Simulacion(0.0, -11.0, ComandoRodillo.Pendiente(-7.0), ModoControl.SimPlataforma(Terreno(-7.0, 0.004, 0.51, 0.0)), sinLimitePct = -18.0)
        mostrar(rodaje(neo = Conectado, square = Conectado, mascara = 0u, simulacion = sim))
        texto("La marcha no crea bajadas")
        texto("LIMITADA POR BAJADAS")
        texto("sin límite sería −18,0 %")
    }

    @Test
    fun limites() {
        var sumas = 0
        mostrar(
            rodaje(neo = Conectado, square = Conectado, mascara = 0u),
            control = ControlManual(pendienteMedios = ControlManual.PENDIENTE_MAX_MEDIOS, potencia = ControlManual.POTENCIA_MIN),
            acciones = AccionesDiagnostico(pendiente = { sumas += it }),
        )
        texto("20,0 %")
        compose.onNodeWithContentDescription("Subir pendiente").performScrollTo().assertIsNotEnabled().performClick()
        compose.onNodeWithContentDescription("Bajar pendiente").assertIsEnabled()
        compose.onNodeWithContentDescription("Bajar potencia").assertIsNotEnabled()
        compose.onNodeWithContentDescription("Subir potencia").assertIsEnabled()
        assertEquals(0, sumas)
    }

    // --- Zwift: zwift-ruta, zwift-erg y sin-wifi ---

    @Test
    fun zwift_ruta() {
        val sim = Simulacion(
            0.0,
            3.0,
            ComandoRodillo.Pendiente(7.0),
            ModoControl.SimPlataforma(Terreno(4.0, 0.004, 0.51, 0.0)),
            ultimoPlataforma = MensajePlataforma(FtmsCodec.Comando.Simulacion(0.0, 4.0, 0.004, 0.51), 0L),
        )
        mostrar(
            rodaje(neo = Conectado, square = Conectado, mascara = 0u, zwift = EstadoPlataforma.Conectado("192.168.0.10"), simulacion = sim),
            filas = listOf(
                FilaHex("12:04:32.021", "→ Zwift", "2ad9", "80 11 01", enviada = true),
                FilaHex("12:04:32.016", "Zwift", "2ad9", "11 00 00 90 01 28 33"),
            ),
        )
        texto("Desde 192.168.0.10")
        texto("SIM · Zwift")
        texto("PENDIENTE ZWIFT")
        texto("4,0")
        texto("+3,0")
        texto("+7,0")
        texto("Crr 0,0040 · Cw 0,51 kg/m · viento 0,0 m/s")
        texto("ÚLTIMO DE ZWIFT")
        compose.onNodeWithText("Simulación · ", substring = true).performScrollTo()
        texto("→ Zwift")
        compose.onNodeWithText("PENDIENTE BASE").assertDoesNotExist()
    }

    @Test
    fun zwift_erg() {
        val sim = Simulacion(0.0, 5.0, ComandoRodillo.Potencia(240), ModoControl.ErgPlataforma(230, 10))
        mostrar(rodaje(neo = Conectado, square = Conectado, mascara = 0u, zwift = EstadoPlataforma.Conectado("192.168.0.10"), simulacion = sim))
        texto("ERG · Zwift")
        texto("POTENCIA ZWIFT")
        texto("230")
        texto("AJUSTE PALANCAS")
        texto("+10")
        texto("240")
        texto("17")
    }

    @Test
    fun sin_wifi_sin_aviso_y_con_la_direccion_en_raya() {
        mostrar(rodaje(neo = Conectado, square = Conectado, mascara = 0u, zwift = EstadoPlataforma.SinWifi))
        compose.onNodeWithText("Sin wifi", substring = true).assertDoesNotExist()
        texto("App de entrenamiento")
        texto("Nombre anunciado")
        texto("Smart Bike")
    }

    @Test
    fun shimano() {
        // Estado `shimano` de la maqueta: la marcha 52×17 con su relación y el plato en «Último gesto».
        val plato = GestoReconocido(
            Gesto(Entrada(SquareButton.PalancaIzquierda1), TipoGesto.Pulsar, 0),
            Accion.PlatoMas,
            ResultadoCambio.Cambiada(EstadoMarcha.PlatoPinon(36, 17), EstadoMarcha.PlatoPinon(52, 21), 7),
        )
        mostrar(rodaje(neo = Conectado, square = Conectado, mascara = 0u, gesto = plato).copy(marcha = EstadoMarcha.PlatoPinon(52, 17)))
        texto("Plato 52")
        texto("52×17")
        texto("3,06")
        compose.onNodeWithText("Relación", ignoreCase = true).performScrollTo()
    }

    @Test
    fun traficoOcultoAlAbrir() {
        mostrar(rodaje(neo = Conectado, square = Conectado), filas = trafico, traficoVisible = false)
        casillaTrafico().assertIsOff().assertHeightIsAtLeast(48.dp)
        texto("Cada trama del rodillo, el Square y Zwift según llega")
        compose.onNodeWithText("→ Rodillo").assertDoesNotExist()
        compose.onNodeWithText("Pausar").assertDoesNotExist()
        compose.onNodeWithText("Limpiar").assertDoesNotExist()
    }

    @Test
    fun mostrarYOcultarTrafico() {
        val c = rodaje(neo = Conectado, square = Conectado)
        compose.setContent {
            var visible by remember { mutableStateOf(false) }
            SqTema {
                Diagnostico(
                    estadoDePantalla(c, ControlManual(), trafico, pausado = false, traficoVisible = visible),
                    AccionesDiagnostico(alternarTrafico = { visible = !visible }),
                )
            }
        }
        casillaTrafico().performClick().assertIsOn()
        texto("→ Rodillo")
        texto("Pausar")
        texto("Limpiar")
        casillaTrafico().performClick().assertIsOff()
        compose.onNodeWithText("→ Rodillo").assertDoesNotExist()
        compose.onNodeWithText("Pausar").assertDoesNotExist()
    }

    // --- Pulso en Datos de conexión: #estado=pulso-128, pulso-sin-lectura y pulso-reconectando ---

    private val banda = DispositivoConfigurado("CC:CC", "COOSPO HW9")
    private fun conBanda(e: EstadoDispositivo, bpm: Int? = null) = rodaje(neo = Conectado, square = Conectado, mascara = 0u)
        .copy(pulsometro = e, pulsometroConfigurado = banda, pulso = bpm, pulsoInstanteMs = bpm?.let { SystemClock.elapsedRealtime() })

    @Test
    fun banda_dando_pulso() {
        mostrar(conBanda(Conectado, 128))
        texto("Pulsómetro")
        compose.onNodeWithText("128 bpm", substring = true).assertExists()
        compose.onNodeWithText("COOSPO HW9", substring = true).assertDoesNotExist()
    }

    @Test
    fun sin_pulso_no_dice_bpm() {
        mostrar(conBanda(Conectado))
        texto("Pulsómetro")
        compose.onNodeWithText("bpm", substring = true).assertDoesNotExist()
    }

    @Test
    fun banda_reconectando_sin_aviso_y_sin_pulso() {
        mostrar(conBanda(Reconectando, 128))
        texto("Pulsómetro")
        compose.onNodeWithText("Reconectando", substring = true, useUnmergedTree = true).assertDoesNotExist()
        compose.onNodeWithText("bpm", substring = true).assertDoesNotExist()
    }

    @Test
    fun sin_banda_configurada_no_hay_fila_de_pulsometro() {
        mostrar(rodaje(neo = Conectado, square = Conectado, mascara = 0u))
        compose.onNodeWithText("Pulsómetro").assertDoesNotExist()
    }

    // --- Ventilador en Datos de conexión: #estado=ventilador-manual-40, ventilador-sin-notificacion y ventilador-reconectando ---

    private val ventilador = DispositivoConfigurado("DD:DD", "HEADWIND 4F2A")
    private fun conVentilador(e: EstadoDispositivo, estado: EstadoVentilador? = null) = rodaje(neo = Conectado, square = Conectado, mascara = 0u)
        .copy(ventilador = e, ventiladorConfigurado = ventilador, ventiladorEstado = estado, ventiladorInstanteMs = estado?.let { SystemClock.elapsedRealtime() })

    @Test
    fun ventilador_en_manual_al_40() {
        mostrar(conVentilador(Conectado, EstadoVentilador(ModoVentilador.Manual, 40)))
        texto("Ventilador")
        compose.onNodeWithText("Manual · 40 %", substring = true).assertExists()
    }

    @Test
    fun ventilador_reconectando_conserva_lo_ultimo_sin_aviso() {
        mostrar(conVentilador(Reconectando, EstadoVentilador(ModoVentilador.Manual, 40)))
        compose.onNodeWithText("Manual · 40 %", substring = true).assertExists()
        compose.onNodeWithText("Reconectando", substring = true, useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun ventilador_sin_notificacion() {
        mostrar(conVentilador(Conectado))
        texto("Ventilador")
        compose.onNodeWithText("Manual", substring = true).assertDoesNotExist()
    }

    @Test
    fun nombre_anunciado_a_zwift_sin_y_con_la_simulacion_de_kickr_bike() {
        // Spec `diagnostics`, «Datos de la app de entrenamiento en Diagnóstico»; estados `conectado` y `kickr` de la maqueta.
        mostrar(rodaje(neo = Conectado, square = Conectado, mascara = 0u, zwift = EstadoPlataforma.Conectado("192.168.0.10")))
        texto("Nombre anunciado")
        compose.onNodeWithText("Smart Bike", substring = true).performScrollTo().assertExists()
    }

    @Test
    fun nombre_anunciado_con_kickr_bike_lleva_identidad_propia() {
        mostrar(rodaje(neo = Conectado, square = Conectado, mascara = 0u, zwift = EstadoPlataforma.Conectado("192.168.0.10")).copy(nombreAnunciado = "KICKR BIKE PRO 669C"))
        texto("Nombre anunciado")
        compose.onNodeWithText("KICKR BIKE PRO 669C · identidad propia").performScrollTo().assertExists()
    }
}
