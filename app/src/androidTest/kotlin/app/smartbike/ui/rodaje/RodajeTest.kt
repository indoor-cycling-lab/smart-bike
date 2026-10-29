package app.smartbike.ui.rodaje

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import app.smartbike.dominio.Accion
import app.smartbike.dominio.DatoRodaje
import app.smartbike.dominio.Desarrollos
import app.smartbike.dominio.EstadoMarcha
import app.smartbike.dominio.LadoGiro
import app.smartbike.dominio.ModoCambio
import app.smartbike.dominio.TopeMarcha
import app.smartbike.dominio.Transmision
import app.smartbike.plataforma.EstadoMando
import app.smartbike.ui.componentes.Enlace
import app.smartbike.ui.componentes.EstadoChip
import app.smartbike.ui.createComposeRuleEs
import app.smartbike.ui.tema.SqTema
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** Un test por estado lineal de maquetas/rodaje.html y rodaje-vertical.html, con y sin Zwift. */
class RodajeTest {
    @get:Rule
    val compose = createComposeRuleEs()

    /** El estado de un enlace, sin decir cuál: `estadoUi` y `con` lo ponen en su sitio. */
    private data class Chip(val estado: EstadoChip, val palabra: EnlaceUi.EnPalabra?)

    private val ok = Chip(EstadoChip.Ok, null)
    private val sin = Chip(EstadoChip.Sin, EnlaceUi.EnPalabra.SinConexion)
    private val conectando = Chip(EstadoChip.Esperando, EnlaceUi.EnPalabra.Conectando)
    private val esperando = Chip(EstadoChip.Esperando, EnlaceUi.EnPalabra.Esperando)
    private val noVisible = Chip(EstadoChip.Sin, EnlaceUi.EnPalabra.NoVisible)

    /** Los dispositivos de la barra: Neo, Square y, si está configurado, el pulsómetro; Zwift y el mando ya no van en ella. */
    private fun enlaces(neo: Chip, square: Chip, pulso: Chip? = null) = listOf(Enlace.Neo to neo, Enlace.Square to square, Enlace.Pulso to pulso)
        .mapNotNull { (enlace, c) -> c?.let { EnlaceUi(enlace, it.estado, it.palabra) } }

    /** El estado de la plataforma a partir del de Zwift y, si el mando va aparte, el del mando (spec `ride-screen`). */
    private fun plataforma(zwift: Chip, mando: Chip? = null) = PlataformaUi(
        zwift.estado,
        when (zwift) {
            ok -> if (mando != null && mando != ok) PlataformaUi.Texto.ConectadoSinMando else PlataformaUi.Texto.Conectado
            esperando -> PlataformaUi.Texto.Esperando
            noVisible -> PlataformaUi.Texto.NoVisible
            else -> PlataformaUi.Texto.SinWifi
        },
        zwift.palabra,
        mando?.palabra,
    )

    private fun estadoUi(
        neo: Chip = sin,
        square: Chip = sin,
        zwift: Chip = sin,
        mando: Chip? = null,
        pulso: Chip? = null,
        marcha: Int = 12,
        referencia: Int = 12,
        potencia: Int? = null,
        cadencia: Int? = null,
        datos: List<DatoUi>? = null,
        botones: Boolean = false,
        platos: PlatosUi? = null,
        transmision: Transmision = Transmision.Lineal(24),
        modoCambio: ModoCambio = ModoCambio.AUTOMATICO,
        erg: ErgUi? = null,
        pie: PieUi = PieUi.TocarParaConectar,
        tope: TopeMarcha? = null,
        aviso: AvisoUi? = null,
        libre: Boolean = true,
        modoTocable: Boolean = false,
    ) = RodajeUiState(
        marcha = marcha, referencia = referencia, potencia = potencia, cadencia = cadencia, datos = datos, botones = botones, platos = platos, transmision = transmision, modoCambio = modoCambio,
        enlaces = enlaces(neo, square, pulso), plataforma = plataforma(zwift, mando), erg = erg, pie = pie, tope = tope, aviso = aviso, libre = libre, modoTocable = modoTocable,
    )

    private fun Chip?.de(e: RodajeUiState, enlace: Enlace) = this ?: e.enlace(enlace)?.let { Chip(it.estado, it.enPalabra) }

    /** El mismo estado con otros enlaces y otro estado de la plataforma; `sinMando` quita el del mando. */
    private fun RodajeUiState.con(neo: Chip? = null, square: Chip? = null, zwift: Chip? = null, mando: Chip? = null, sinMando: Boolean = false): RodajeUiState {
        val z = zwift ?: Chip(plataforma.estado, plataforma.rodillo)
        val m = if (sinMando) null else mando ?: plataforma.mando?.let { Chip(EstadoChip.Esperando, it) }
        return copy(enlaces = enlaces(neo.de(this, Enlace.Neo)!!, square.de(this, Enlace.Square)!!, enlace(Enlace.Pulso)?.let { Chip(it.estado, it.enPalabra) }), plataforma = plataforma(z, m))
    }

    private fun barra() = compose.onNode(hasContentDescription("Conexiones", substring = true))

    private val neoConectando = estadoUi(zwift = esperando, marcha = 14, neo = conectando, square = ok, pie = PieUi.DespertarNeo)
    private val squareReconectando =
        estadoUi(zwift = esperando, marcha = 14, potencia = 247, cadencia = 91, neo = ok, square = conectando, pie = PieUi.DespertarSquare)
    private val losDosConectando = estadoUi(zwift = esperando, marcha = 14, neo = conectando, square = conectando, pie = PieUi.DespertarLosDos)

    /**
     * Cada test fija el tamaño del contenedor, porque `Rodaje` elige la disposición por el espacio y los tests corren con
     * el móvil en vertical. La horizontal (844 × 390) no cabe en la pantalla: sus tests comprueban que existe, no que se ve.
     */
    private fun mostrar(e: RodajeUiState, abrir: () -> Unit = {}, vertical: Boolean = false) {
        val tamano = if (vertical) DpSize(360.dp, 720.dp) else DpSize(844.dp, 390.dp)
        compose.setContent { SqTema { Box(Modifier.requiredSize(tamano)) { Rodaje(e, abrir, animar = false) } } }
    }

    @Test
    fun sin_zwift() {
        mostrar(estadoUi(zwift = esperando, marcha = 14, potencia = 247, cadencia = 91, neo = ok, square = ok, pie = PieUi.Nada, aviso = AvisoUi.Marcha(Accion.MarchaMas(1), 1)))
        compose.onNodeWithContentDescription("Marcha 14 de 24").assertExists()
        compose.onNodeWithText("LIBRE").assertExists()
        compose.onNodeWithText("Lineal · 24 marchas").assertExists()
        compose.onNodeWithText("247").assertExists()
        compose.onNodeWithText("91").assertExists()
        compose.onNodeWithText("+1 marcha").assertExists()
        // El estado de la plataforma va en palabras, bajo la fila de Ajustes (spec `ride-screen`, «Estado de la plataforma en Rodaje»).
        compose.onNodeWithText("Esperando conexión…").assertExists()
        compose.onNodeWithContentDescription("Conexión: rodillo, esperando", substring = true).assertExists()
        compose.onNodeWithText("Toca las conexiones para conectar el Neo y el Square").assertDoesNotExist()
        // F11.2: nada del modelo de resistencia.
        compose.onNodeWithText("%", substring = true).assertDoesNotExist()
    }

    @Test
    fun referencia_fijada() {
        // Spec `ride-screen`, «Fijar la referencia».
        mostrar(estadoUi(zwift = esperando, marcha = 15, referencia = 15, neo = ok, square = ok, pie = PieUi.Nada, aviso = AvisoUi.Referencia(EstadoMarcha.Numerada(15, 24))))
        compose.onNodeWithText("Referencia en la 15").assertExists()
    }

    @Test
    fun tope_lineal() {
        mostrar(estadoUi(zwift = esperando, marcha = 24, neo = ok, square = ok, pie = PieUi.Nada, tope = TopeMarcha.MasDura))
        compose.onNodeWithContentDescription("Marcha 24 de 24, tope").assertExists()
        compose.onNodeWithText("Tope · marcha más dura").assertExists()
    }

    @Test
    fun square_sin_conexion() {
        mostrar(estadoUi(zwift = esperando, marcha = 14, potencia = 247, cadencia = 91, neo = ok, square = sin))
        compose.onNodeWithText("sin conexión").assertDoesNotExist()
        compose.onNodeWithContentDescription("Controlador, sin conexión", substring = true).assertExists()
        compose.onNodeWithText("247").assertExists()
        compose.onNodeWithText("Toca las conexiones para conectar el Neo y el Square").assertExists()
    }

    @Test
    fun sin_dispositivos_y_la_barra_abre_dispositivos() {
        var abierto = 0
        mostrar(estadoUi(zwift = esperando, marcha = 12), abrir = { abierto++ })
        compose.onNodeWithContentDescription("Neo, sin conexión", substring = true).assertExists()
        compose.onNodeWithContentDescription("Controlador, sin conexión", substring = true).assertExists()
        compose.onAllNodesWithText("—").assertCountEquals(2)
        compose.onNodeWithText("Toca las conexiones para conectar el Neo y el Square").assertExists()
        barra().performClick()
        assertEquals(1, abierto)
    }

    @Test
    fun sin_zwift_en_vertical() {
        mostrar(estadoUi(zwift = esperando, marcha = 14, potencia = 247, cadencia = 91, neo = ok, square = ok, pie = PieUi.Nada, aviso = AvisoUi.Marcha(Accion.MarchaMas(1), 1)), vertical = true)
        barra().assertIsDisplayed()
        compose.onNodeWithText("LIBRE").assertIsDisplayed()
        compose.onNodeWithText("Lineal · 24 marchas").assertIsDisplayed()
        compose.onNodeWithContentDescription("Marcha 14 de 24").assertIsDisplayed()
        compose.onNodeWithText("+1 marcha").assertIsDisplayed()
        // El estado de la plataforma va en palabras, bajo la fila de Ajustes (spec `ride-screen`, «Estado de la plataforma en Rodaje»).
        compose.onNodeWithText("Esperando conexión…").assertExists()
        compose.onNodeWithContentDescription("Conexión: rodillo, esperando", substring = true).assertExists()
        // Potencia y cadencia una al lado de la otra.
        val potencia = compose.onNodeWithText("247").assertIsDisplayed().getUnclippedBoundsInRoot()
        val cadencia = compose.onNodeWithText("91").assertIsDisplayed().getUnclippedBoundsInRoot()
        assertEquals(potencia.top, cadencia.top)
        assertTrue(cadencia.left >= potencia.right)
    }

    @Test
    fun tope_lineal_en_vertical_sin_mover_el_resto() {
        var estado by mutableStateOf(estadoUi(zwift = esperando, marcha = 24, potencia = 247, cadencia = 91, neo = ok, square = ok, pie = PieUi.Nada))
        compose.setContent { SqTema { Box(Modifier.requiredSize(360.dp, 720.dp)) { Rodaje(estado, {}, animar = false) } } }
        val antes = compose.onNodeWithText("247").getUnclippedBoundsInRoot()
        estado = estado.copy(tope = TopeMarcha.MasDura)
        compose.onNodeWithContentDescription("Marcha 24 de 24, tope").assertIsDisplayed()
        assertEquals(antes, compose.onNodeWithText("247").getUnclippedBoundsInRoot())
        compose.onNodeWithText("Tope · marcha más dura").assertIsDisplayed()
        compose.onNodeWithText("247").assertIsDisplayed()
        compose.onNodeWithText("91").assertIsDisplayed()
    }

    @Test
    fun sin_dispositivos_en_vertical() {
        mostrar(estadoUi(zwift = esperando, marcha = 12), vertical = true)
        compose.onNodeWithText("sin conexión").assertDoesNotExist()
        compose.onNodeWithContentDescription("Neo, sin conexión", substring = true).assertIsDisplayed()
        compose.onNodeWithContentDescription("Controlador, sin conexión", substring = true).assertIsDisplayed()
        compose.onAllNodesWithText("—")[0].assertIsDisplayed()
        compose.onAllNodesWithText("—")[1].assertIsDisplayed()
        compose.onNodeWithText("Toca las conexiones para conectar el Neo y el Square").assertIsDisplayed()
    }

    // --- Reconexión: maquetas/rodaje.html y rodaje-vertical.html, #estado=neo-conectando, square-reconectando y conectando

    @Test
    fun neo_conectando() {
        mostrar(neoConectando)
        compose.onNodeWithContentDescription("Neo, conectando", substring = true).assertExists()
        compose.onAllNodesWithText("—").assertCountEquals(2)
        compose.onNodeWithText("Pedalea para despertar el Neo").assertExists()
    }

    @Test
    fun square_reconectando() {
        mostrar(squareReconectando)
        compose.onNodeWithContentDescription("Controlador, conectando", substring = true).assertExists()
        compose.onNodeWithText("247").assertExists()
        compose.onNodeWithText("Pulsa un botón del Square para despertarlo").assertExists()
        compose.onNodeWithText("Toca las conexiones para conectar el Neo y el Square").assertDoesNotExist()
    }

    @Test
    fun los_dos_conectando() {
        mostrar(losDosConectando)
        compose.onNodeWithText("conectando").assertDoesNotExist()
        compose.onNodeWithContentDescription("Neo, conectando", substring = true).assertExists()
        compose.onNodeWithContentDescription("Controlador, conectando", substring = true).assertExists()
        compose.onAllNodesWithText("—").assertCountEquals(2)
        compose.onNodeWithText("Pedalea y pulsa un botón del Square para despertarlos").assertExists()
    }

    @Test
    fun el_aviso_de_accion_prima_sobre_la_ayuda() {
        mostrar(neoConectando.copy(aviso = AvisoUi.Marcha(Accion.MarchaMas(1), 1)))
        compose.onNodeWithText("+1 marcha").assertExists()
        compose.onNodeWithText("Pedalea para despertar el Neo").assertDoesNotExist()
    }

    @Test
    fun reconexion_en_vertical_sin_mover_nada() {
        var estado by mutableStateOf(estadoUi(zwift = esperando, marcha = 14, potencia = 247, cadencia = 91, neo = ok, square = ok, pie = PieUi.Nada))
        compose.setContent { SqTema { Box(Modifier.requiredSize(360.dp, 720.dp)) { Rodaje(estado, {}, animar = false) } } }
        val marcha = compose.onNodeWithContentDescription("Marcha 14 de 24").getUnclippedBoundsInRoot()
        val modo = compose.onNodeWithText("Lineal · 24 marchas").getUnclippedBoundsInRoot()
        val anchoBarra = barra().getUnclippedBoundsInRoot().let { it.right - it.left }
        for ((e, pie) in listOf(
            neoConectando to "Pedalea para despertar el Neo",
            squareReconectando to "Pulsa un botón del Square para despertarlo",
            losDosConectando to "Pedalea y pulsa un botón del Square para despertarlos",
        )) {
            estado = e
            compose.onNodeWithText(pie).assertIsDisplayed()
            // La barra y Ajustes caben en una fila a 360 dp, sin el estado en palabra.
            val barra = barra().assertIsDisplayed().getUnclippedBoundsInRoot()
            assertEquals(pie, barra.right - barra.left, anchoBarra)
            assertEquals(pie, marcha, compose.onNodeWithContentDescription("Marcha 14 de 24").getUnclippedBoundsInRoot())
            assertEquals(pie, modo, compose.onNodeWithText("Lineal · 24 marchas").getUnclippedBoundsInRoot())
        }
    }

    // --- Con Zwift: #estado=zwift-ruta, zwift-erg, zwift-erg-tope y sin-wifi ---

    private val ruta = estadoUi(marcha = 14, potencia = 247, cadencia = 91, neo = ok, square = ok, zwift = ok, pie = PieUi.Nada, libre = false)
    private val ergZwift = ruta.copy(potencia = 238, erg = ErgUi(240, 230, 10), aviso = AvisoUi.Vatios(10))

    @Test
    fun ruta_de_zwift_con_el_mando_esperando() {
        // Estado `zwift-mando-esperando` de la maqueta: Zwift conectado y el mando en espera: «Conectado · sin controlador».
        var estado by mutableStateOf(ruta.con(mando = ok))
        compose.setContent { SqTema { Box(Modifier.requiredSize(360.dp, 720.dp)) { Rodaje(estado, {}, animar = false) } } }
        compose.onNodeWithText("Conectado").assertExists()
        compose.onNodeWithText("sin controlador", substring = true).assertDoesNotExist()
        val antes = barra().getUnclippedBoundsInRoot()
        estado = ruta.con(mando = esperando)
        compose.onNodeWithText("Conectado · sin controlador").assertExists()
        compose.onNodeWithContentDescription("Conexión: rodillo, conectado; controlador, esperando", substring = true).assertExists()
        assertEquals(antes, barra().getUnclippedBoundsInRoot())
    }

    @Test
    fun ruta_de_zwift_con_el_mando_desactivado() {
        // Spec `ride-screen`, «Mando desactivado»: «Conectado», sin «sin mando», y la barra no lleva ni el icono de Zwift ni el del mando.
        mostrar(ruta)
        compose.onNodeWithText("Conectado").assertExists()
        compose.onNodeWithText("sin controlador", substring = true).assertDoesNotExist()
        compose.onNodeWithContentDescription("Conexión: rodillo", substring = true).assertExists()
        barra().assertContentDescriptionEquals("Conexiones: Neo, conectado; Controlador, conectado")
    }

    @Test
    fun libre_sin_zwift_y_sim_con_la_ruta_sin_solaparse_con_ajustes_a_360_dp() {
        // Spec `ride-screen`, «Modo y transmisión»: la barra superior dice LIBRE sin app y SIM en una ruta.
        var estado by mutableStateOf(ruta.copy(libre = true))
        compose.setContent { SqTema { Box(Modifier.requiredSize(360.dp, 720.dp)) { Rodaje(estado, {}, animar = false) } } }
        for ((e, texto, otro) in listOf(Triple(ruta.copy(libre = true), "LIBRE", "SIM"), Triple(ruta, "SIM", "LIBRE"))) {
            estado = e
            compose.waitForIdle()
            val modo = compose.onNodeWithText(texto).assertIsDisplayed().getUnclippedBoundsInRoot()
            compose.onNodeWithText(otro).assertDoesNotExist()
            val ajustes = compose.onNodeWithContentDescription("Ajustes").getUnclippedBoundsInRoot()
            assertTrue(modo.right <= ajustes.left || modo.bottom <= ajustes.top)
        }
    }

    @Test
    fun a_360_x_640_los_botones_del_square_caido_dejan_cuatro_datos_y_seis_al_conectarse() {
        // Spec `ride-screen`, «Botones − y + según el Square»: se ven 4 datos con los botones y 6 cuando desaparecen.
        var estado by mutableStateOf(estadoUi(zwift = esperando, marcha = 14, neo = ok, square = sin, pie = PieUi.Nada, datos = seis, botones = true))
        compose.setContent { SqTema { Box(Modifier.requiredSize(360.dp, 640.dp)) { Rodaje(estado, {}, animar = false) } } }
        compose.onNodeWithContentDescription("Una marcha más dura").assertIsDisplayed()
        listOf("247", "91", "1:12:40", "3,3").forEach { compose.onNodeWithText(it).assertIsDisplayed() }
        listOf("231", "612").forEach { compose.onNodeWithText(it).assertDoesNotExist() }
        estado = estado.copy(botones = false)
        compose.waitForIdle()
        compose.onNodeWithContentDescription("Una marcha más dura").assertDoesNotExist()
        listOf("247", "91", "1:12:40", "3,3", "231", "612").forEach { compose.onNodeWithText(it).assertIsDisplayed() }
    }

    @Test
    fun ruta_de_zwift() {
        mostrar(ruta)
        compose.onNodeWithText("Conectado").assertExists()
        compose.onNodeWithText("SIM").assertExists()
        compose.onNodeWithContentDescription("Marcha 14 de 24").assertExists()
        compose.onNodeWithText("%", substring = true).assertDoesNotExist()
    }

    @Test
    fun erg_de_zwift() {
        mostrar(ergZwift)
        compose.onNodeWithText("ERG").assertExists()
        compose.onNodeWithText("APP").assertExists()
        // Sin línea que explique las palancas: el modo ya lo dice y el aviso «+10 W» lo muestra.
        compose.onNodeWithText("Entrenamiento de la app", substring = true).assertDoesNotExist()
        compose.onNodeWithText("las palancas ajustan la potencia", substring = true).assertDoesNotExist()
        compose.onNodeWithContentDescription("Potencia objetivo 240 W").assertExists()
        compose.onNodeWithText("230 W de la app").assertExists()
        compose.onNodeWithText("+10 W con las palancas").assertExists()
        compose.onNodeWithText("+10 W").assertExists()
        compose.onNodeWithContentDescription("Marcha 14 de 24").assertDoesNotExist()
    }

    @Test
    fun erg_de_zwift_sin_ajuste() {
        mostrar(ergZwift.copy(erg = ErgUi(230, 230, 0), aviso = null))
        compose.onNodeWithText("230 W de la app").assertExists()
        compose.onNodeWithText("con las palancas", substring = true).assertDoesNotExist()
    }

    @Test
    fun tope_de_potencia_en_erg() {
        mostrar(ergZwift.copy(erg = ErgUi(50, 60, -10), aviso = AvisoUi.TopePotencia(50)))
        compose.onNodeWithContentDescription("Potencia objetivo 50 W").assertExists()
        compose.onNodeWithText("Tope · 50 W").assertExists()
        compose.onNodeWithText("−10 W con las palancas").assertExists()
    }

    @Test
    fun sin_wifi_y_la_barra_abre_dispositivos() {
        var abierto = 0
        mostrar(ruta.con(zwift = sin), abrir = { abierto++ })
        compose.onNodeWithText("Sin wifi").assertExists()
        compose.onNodeWithContentDescription("Marcha 14 de 24").assertExists()
        compose.onNodeWithText("Toca las conexiones para conectar el Neo y el Square").assertDoesNotExist()
        barra().performClick()
        assertEquals(1, abierto)
    }

    @Test
    fun zwift_no_visible_sin_cambiar_de_ancho() {
        // #estado=zwift-no-visible de las maquetas de rodaje: «No visible en la red» y, al volver a responder, «Esperando conexión».
        var estado by mutableStateOf(ruta.con(zwift = noVisible))
        compose.setContent { SqTema { Box(Modifier.requiredSize(360.dp, 720.dp)) { Rodaje(estado, {}, animar = false) } } }
        compose.onNodeWithText("No visible en la red").assertExists()
        val zwift = barra().getUnclippedBoundsInRoot()
        val marcha = compose.onNodeWithContentDescription("Marcha 14 de 24").getUnclippedBoundsInRoot()
        estado = estado.con(zwift = esperando)
        compose.onNodeWithText("Esperando conexión…").assertExists()
        assertEquals(zwift, barra().getUnclippedBoundsInRoot())
        assertEquals(marcha, compose.onNodeWithContentDescription("Marcha 14 de 24").getUnclippedBoundsInRoot())
    }

    @Test
    fun zwift_no_visible_con_el_mando() {
        // La descripción dice el estado de la plataforma y el del mando.
        mostrar(ruta.con(zwift = noVisible, mando = esperando))
        compose.onNodeWithContentDescription("Conexión: rodillo, no visible; controlador, esperando", substring = true).assertExists()
    }

    @Test
    fun erg_de_zwift_en_vertical_sin_desplazamiento() {
        mostrar(ergZwift, vertical = true)
        barra().assertIsDisplayed()
        compose.onNodeWithText("ERG").assertIsDisplayed()
        compose.onNodeWithContentDescription("Potencia objetivo 240 W").assertIsDisplayed()
        compose.onNodeWithText("230 W de la app").assertIsDisplayed()
        compose.onNodeWithText("+10 W con las palancas").assertIsDisplayed()
        val potencia = compose.onNodeWithText("238").assertIsDisplayed().getUnclippedBoundsInRoot()
        val cadencia = compose.onNodeWithText("91").assertIsDisplayed().getUnclippedBoundsInRoot()
        assertEquals(potencia.top, cadencia.top)
        compose.onNodeWithText("+10 W").assertIsDisplayed()
    }

    @Test
    fun estados_con_zwift_en_vertical_sin_mover_el_modo() {
        var estado by mutableStateOf(estadoUi(zwift = esperando, marcha = 14, potencia = 247, cadencia = 91, neo = ok, square = ok, pie = PieUi.Nada))
        compose.setContent { SqTema { Box(Modifier.requiredSize(360.dp, 720.dp)) { Rodaje(estado, {}, animar = false) } } }
        val modo = compose.onNodeWithText("LIBRE").getUnclippedBoundsInRoot()
        val marcha = compose.onNodeWithContentDescription("Marcha 14 de 24").getUnclippedBoundsInRoot()
        // Los iconos caben en una fila y nada se mueve al cambiar su estado.
        for (e in listOf(ruta.copy(libre = true), ruta.con(zwift = sin).copy(libre = true), losDosConectando.con(zwift = esperando).copy(potencia = 247, cadencia = 91))) {
            estado = e
            barra().assertIsDisplayed()
            assertEquals(modo, compose.onNodeWithText("LIBRE").getUnclippedBoundsInRoot())
            assertEquals(marcha, compose.onNodeWithContentDescription("Marcha 14 de 24").getUnclippedBoundsInRoot())
        }
    }

    @Test
    fun barra_junto_al_estado_de_la_plataforma_en_vertical_sin_mover_nada() {
        // Spec `ride-screen`, «Conexiones en una línea en vertical»: Neo conectando, Square sin conexión y Zwift esperando.
        var estado by mutableStateOf(
            estadoUi(zwift = esperando, marcha = 14, potencia = 247, cadencia = 91, neo = conectando, square = sin, pie = PieUi.DespertarNeo),
        )
        compose.setContent { SqTema { Box(Modifier.requiredSize(360.dp, 720.dp)) { Rodaje(estado, {}, animar = false) } } }
        val barra = barra().assertIsDisplayed().getUnclippedBoundsInRoot()
        // La barra va a la derecha del estado de la plataforma y Ajustes en la fila del modo, debajo de ella.
        val ajustes = compose.onNodeWithContentDescription("Ajustes").assertIsDisplayed().getUnclippedBoundsInRoot()
        val plataforma = compose.onNodeWithText("Esperando conexión…").assertIsDisplayed().getUnclippedBoundsInRoot()
        assertTrue(plataforma.right <= barra.left)
        assertTrue(barra.bottom <= ajustes.top)
        assertTrue(barra.right <= 360.dp)
        val marcha = compose.onNodeWithContentDescription("Marcha 14 de 24").getUnclippedBoundsInRoot()
        val datos = compose.onNodeWithText("247").getUnclippedBoundsInRoot()
        estado = estado.con(neo = ok, square = ok, zwift = ok).copy(pie = PieUi.Nada)
        assertEquals(barra, barra().getUnclippedBoundsInRoot())
        assertEquals(marcha, compose.onNodeWithContentDescription("Marcha 14 de 24").getUnclippedBoundsInRoot())
        assertEquals(datos, compose.onNodeWithText("247").getUnclippedBoundsInRoot())
    }

    // --- ERG manual: #estado=erg-manual, erg-manual-ajuste, erg-manual-tope, erg-manual-zwift y zwift-erg-alternar ---

    private val ergManual = estadoUi(
        zwift = esperando, marcha = 14, potencia = 198, cadencia = 88, neo = ok, square = ok, pie = PieUi.Nada,
        erg = ErgUi(200, 200, 0, manual = true), aviso = AvisoUi.ModoErg(200),
    )

    @Test
    fun erg_manual() {
        mostrar(ergManual)
        compose.onNodeWithText("ERG").assertExists()
        compose.onNodeWithText("MANUAL").assertExists()
        compose.onNodeWithText("las palancas ajustan la potencia", substring = true).assertDoesNotExist()
        compose.onNodeWithContentDescription("Potencia objetivo 200 W").assertExists()
        compose.onNodeWithText("ERG manual · 200 W").assertExists()
        compose.onNodeWithText("de la app", substring = true).assertDoesNotExist()
        compose.onNodeWithText("con las palancas", substring = true).assertDoesNotExist()
        compose.onNodeWithContentDescription("Marcha 14 de 24").assertDoesNotExist()
    }

    @Test
    fun erg_manual_ajuste_y_tope() {
        mostrar(ergManual.copy(erg = ErgUi(210, 200, 10, manual = true), aviso = AvisoUi.Vatios(10)))
        compose.onNodeWithContentDescription("Potencia objetivo 210 W").assertExists()
        compose.onNodeWithText("+10 W").assertExists()
        compose.onNodeWithText("con las palancas", substring = true).assertDoesNotExist()
    }

    @Test
    fun erg_manual_tope() {
        mostrar(ergManual.copy(erg = ErgUi(1000, 200, 800, manual = true), aviso = AvisoUi.TopePotencia(1000)))
        compose.onNodeWithContentDescription("Potencia objetivo 1000 W").assertExists()
        compose.onNodeWithText("Tope · 1000 W").assertExists()
    }

    @Test
    fun avisos_de_alternar() {
        var estado by mutableStateOf(ergManual.copy(aviso = AvisoUi.ModoErg(null), erg = null))
        compose.setContent { SqTema { Box(Modifier.requiredSize(844.dp, 390.dp)) { Rodaje(estado, {}, animar = false) } } }
        compose.onNodeWithText("Modo Libre").assertExists()
        compose.onNodeWithContentDescription("Marcha 14 de 24").assertExists()
        estado = ergManual.copy(aviso = AvisoUi.VueltaA(200))
        compose.onNodeWithText("Vuelta a 200 W").assertExists()
        estado = ergZwift.copy(aviso = AvisoUi.EntrenamientoZwift)
        compose.onNodeWithText("Entrenamiento de la app").assertExists()
        compose.onNodeWithText("APP").assertExists()
    }

    @Test
    fun erg_manual_en_vertical_centrado_y_sin_desplazamiento() {
        mostrar(ergManual, vertical = true)
        compose.onNodeWithText("MANUAL").assertIsDisplayed()
        val objetivo = compose.onNodeWithContentDescription("Potencia objetivo 200 W").assertIsDisplayed().getUnclippedBoundsInRoot()
        val potencia = compose.onNodeWithText("198").assertIsDisplayed().getUnclippedBoundsInRoot()
        val cadencia = compose.onNodeWithText("88").assertIsDisplayed().getUnclippedBoundsInRoot()
        assertEquals(potencia.top, cadencia.top)
        // Centrada en horizontal, como la marcha (design.md del change `erg-manual`, decisión 6).
        assertEquals(180f, ((objetivo.left + objetivo.right) / 2).value, 2f)
        compose.onNodeWithText("ERG manual · 200 W").assertIsDisplayed()
    }

    @Test
    fun datos_centrados_en_su_celda() {
        mostrar(ergManual, vertical = true)
        val etiqueta = compose.onNodeWithText("POTENCIA", ignoreCase = true).getUnclippedBoundsInRoot()
        val cifra = compose.onNodeWithText("198").getUnclippedBoundsInRoot()
        // La etiqueta y la cifra con su unidad comparten el centro de la celda: la etiqueta, más estrecha, empieza después.
        assertTrue(etiqueta.left > cifra.left)
    }

    // --- Shimano 105 -----------------------------------------------------------------------------------------------

    private val shimano = Desarrollos.SHIMANO_105

    private fun conShimano(plato: Int, pinon: Int, modo: ModoCambio = ModoCambio.AUTOMATICO) = estadoUi(
        zwift = esperando, potencia = 247, cadencia = 91, neo = ok, square = ok, pie = PieUi.Nada,
        platos = PlatosUi(plato, pinon, shimano.platos, shimano.pinones), transmision = shimano, modoCambio = modo,
    )

    @Test
    fun shimano_automatico() {
        // Estado `shimano-auto` de la maqueta.
        mostrar(conShimano(52, 17).copy(aviso = AvisoUi.Marcha(Accion.MarchaMas(1), 1)))
        compose.onNodeWithText("Shimano 105 · automático").assertExists()
        compose.onNodeWithContentDescription("Plato 52, piñón 17").assertExists()
        compose.onNodeWithContentDescription("Plato 52").assertExists()
        compose.onNodeWithContentDescription("Cassette 11 a 36, piñón 17").assertExists()
        compose.onNodeWithText("+1 marcha").assertExists()
        // Sin escalera ni nada del modelo de resistencia.
        compose.onNodeWithText("Ligera").assertDoesNotExist()
        compose.onNodeWithText("%", substring = true).assertDoesNotExist()
    }

    @Test
    fun shimano_cambio_de_plato_y_referencia() {
        mostrar(conShimano(52, 19).copy(aviso = AvisoUi.Plato(52)))
        compose.onNodeWithText("Plato 52").assertExists()
    }

    @Test
    fun shimano_referencia_fijada() {
        mostrar(conShimano(36, 17).copy(aviso = AvisoUi.Referencia(EstadoMarcha.PlatoPinon(36, 17))))
        compose.onNodeWithText("Referencia en 36×17").assertExists()
    }

    @Test
    fun shimano_tope_de_plato_en_manual() {
        // Estado `shimano-tope-plato` de la maqueta.
        mostrar(conShimano(52, 17, ModoCambio.MANUAL).copy(tope = TopeMarcha.Plato(52)))
        compose.onNodeWithText("Shimano 105 · manual").assertExists()
        compose.onNodeWithContentDescription("Plato 52, piñón 17, tope").assertExists()
        compose.onNodeWithText("Tope · plato 52").assertExists()
    }

    @Test
    fun shimano_en_vertical_sin_mover_el_resto() {
        // Estados `shimano-auto` y `shimano-plato` en vertical: todo visible a 360 dp, y nada se mueve al cambiar.
        var estado by mutableStateOf(conShimano(52, 17))
        compose.setContent { SqTema { Box(Modifier.requiredSize(360.dp, 720.dp)) { Rodaje(estado, {}, animar = false) } } }
        compose.onNodeWithContentDescription("Plato 52, piñón 17").assertIsDisplayed()
        compose.onNodeWithContentDescription("Cassette 11 a 36, piñón 17").assertIsDisplayed()
        compose.onNodeWithText("247").assertIsDisplayed()
        compose.onNodeWithText("91").assertIsDisplayed()
        val antes = compose.onNodeWithText("247").getUnclippedBoundsInRoot()
        estado = conShimano(52, 19).copy(aviso = AvisoUi.Plato(52))
        compose.onNodeWithText("Plato 52").assertIsDisplayed()
        estado = estado.copy(tope = TopeMarcha.Plato(52))
        assertEquals(antes, compose.onNodeWithText("247").getUnclippedBoundsInRoot())
    }

    // --- Datos elegidos y botones − y + (change rodaje-completo-y-ajustes) ----------------------------------------

    private val seis = listOf(
        DatoUi(DatoRodaje.POTENCIA, "247", "W"),
        DatoUi(DatoRodaje.CADENCIA, "91", "rpm"),
        DatoUi(DatoRodaje.TIEMPO, "1:12:40"),
        DatoUi(DatoRodaje.W_KG, "3,3"),
        DatoUi(DatoRodaje.POTENCIA_MEDIA, "231", "W"),
        DatoUi(DatoRodaje.POTENCIA_MAXIMA, "612", "W"),
    )

    @Test
    fun un_solo_dato() {
        // Spec `ride-screen`, «Un solo dato».
        mostrar(estadoUi(zwift = esperando, marcha = 14, neo = ok, square = ok, pie = PieUi.Nada, datos = listOf(DatoUi(DatoRodaje.CADENCIA, "91", "rpm"))), vertical = true)
        compose.onNodeWithText("91").assertIsDisplayed()
        compose.onNodeWithText("CADENCIA").assertIsDisplayed()
        compose.onNodeWithText("POTENCIA").assertDoesNotExist()
    }

    @Test
    fun seis_datos_en_vertical() {
        // Spec `ride-screen`, «Seis datos en vertical»: 2 columnas y 3 filas, todos a la vista.
        mostrar(estadoUi(zwift = esperando, marcha = 14, neo = ok, square = ok, pie = PieUi.Nada, datos = seis), vertical = true)
        listOf("247", "91", "1:12:40", "3,3", "231", "612").forEach { compose.onNodeWithText(it).assertIsDisplayed() }
        val potencia = compose.onNodeWithText("247").getUnclippedBoundsInRoot()
        val cadencia = compose.onNodeWithText("91").getUnclippedBoundsInRoot()
        val tiempo = compose.onNodeWithText("1:12:40").getUnclippedBoundsInRoot()
        assertEquals(potencia.top, cadencia.top)
        assertTrue(tiempo.top > potencia.bottom)
    }

    @Test
    fun seis_datos_con_botones_en_vertical_y_en_horizontal() {
        // Spec `ride-screen`, «Seis datos con botones», en el tamaño de la maqueta (390 × 844) y en horizontal.
        var mas = 0
        var menos = 0
        var tamano by mutableStateOf(DpSize(390.dp, 844.dp))
        val estado = estadoUi(zwift = esperando, marcha = 14, neo = ok, square = ok, pie = PieUi.Nada, datos = seis, botones = true)
        compose.setContent { SqTema { Box(Modifier.requiredSize(tamano)) { Rodaje(estado, {}, animar = false, mas = { mas++ }, menos = { menos++ }) } } }
        listOf("247", "91", "1:12:40", "3,3", "231", "612").forEach { compose.onNodeWithText(it).assertIsDisplayed() }
        compose.onNodeWithContentDescription("Una marcha más dura").assertIsDisplayed().assertHeightIsAtLeast(72.dp).performClick()
        compose.onNodeWithContentDescription("Una marcha más ligera").assertIsDisplayed().performClick()
        assertEquals(1, mas)
        assertEquals(1, menos)
        // En horizontal, a la derecha de los datos.
        tamano = DpSize(844.dp, 390.dp)
        compose.waitForIdle()
        val dato = compose.onNodeWithText("612").getUnclippedBoundsInRoot()
        val boton = compose.onNodeWithContentDescription("Una marcha más dura").getUnclippedBoundsInRoot()
        assertTrue(boton.left >= dato.right)
        assertTrue(boton.bottom <= 390.dp)
    }

    @Test
    fun sin_botones_por_defecto() {
        // Spec `ride-screen`, «Por defecto» de los botones.
        mostrar(estadoUi(zwift = esperando, marcha = 14, neo = ok, square = ok, pie = PieUi.Nada), vertical = true)
        compose.onNodeWithContentDescription("Una marcha más dura").assertDoesNotExist()
    }

    private fun indicadorTocableAlterna(tamano: DpSize) {
        var alternados = 0
        compose.setContent { SqTema { Box(Modifier.requiredSize(tamano)) { Rodaje(estadoUi(zwift = esperando, marcha = 14, neo = ok, square = ok, pie = PieUi.Nada, modoTocable = true), {}, animar = false, alternarModo = { alternados++ }) } } }
        val modo = compose.onNodeWithContentDescription("Modo Libre, toca para pasar a ERG manual").assertExists().assertHasClickAction().assertHeightIsAtLeast(48.dp)
        modo.performClick()
        assertEquals(1, alternados)
        val limites = modo.getUnclippedBoundsInRoot()
        val ajustes = compose.onNodeWithContentDescription("Ajustes").getUnclippedBoundsInRoot()
        assertTrue(limites.right <= ajustes.left || limites.bottom <= ajustes.top)
    }

    @Test
    fun el_indicador_de_modo_tocable_alterna_el_modo_en_vertical() {
        // Spec `ride-screen`, «Cambiar el modo tocándolo» y «Espacio del indicador de modo tocable» (360 dp).
        indicadorTocableAlterna(DpSize(360.dp, 720.dp))
    }

    @Test
    fun el_indicador_de_modo_tocable_alterna_el_modo_en_horizontal() {
        indicadorTocableAlterna(DpSize(844.dp, 390.dp))
    }

    @Test
    fun el_indicador_de_modo_sin_el_ajuste_es_una_etiqueta() {
        // Spec `ride-screen`, «Por defecto» y «Botones visibles sin el ajuste»: sin respuesta al toque, aunque los botones − y + se vean.
        var alternados = 0
        compose.setContent { SqTema { Box(Modifier.requiredSize(360.dp, 720.dp)) { Rodaje(estadoUi(zwift = esperando, marcha = 14, neo = ok, square = sin, pie = PieUi.Nada, botones = true), {}, animar = false, alternarModo = { alternados++ }) } } }
        compose.onNodeWithText("LIBRE").assertIsDisplayed().performClick()
        assertEquals(0, alternados)
        compose.onNodeWithContentDescription("Modo Libre, toca para pasar a ERG manual").assertDoesNotExist()
    }

    @Test
    fun la_barra_abre_dispositivos() {
        // Spec `ride-screen`, «Tocar la barra de conexiones»: lo que haga lo decide la navegación (Dispositivos).
        var abiertos = 0
        mostrar(estadoUi(zwift = esperando, marcha = 14, neo = sin, square = sin, pie = PieUi.TocarParaConectar), abrir = { abiertos++ }, vertical = true)
        barra().performClick()
        assertEquals(1, abiertos)
    }

    @Test
    fun seis_datos_en_vertical_con_letra_grande_sin_cortes() {
        // Fallo visto en el OnePlus 8: con la letra del sistema grande, las cifras se cortaban por abajo y el pie las
        // tapaba. La rejilla baja de tamaño hasta que caben (spec `ui-components`, «Valor largo»).
        val estado = estadoUi(zwift = esperando, marcha = 12, neo = ok, square = ok, pie = PieUi.TocarParaConectar, datos = seis)
        compose.setContent {
            val d = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(d.density, fontScale = 1.3f)) {
                SqTema { Box(Modifier.requiredSize(411.dp, 860.dp)) { Rodaje(estado, {}, animar = false) } }
            }
        }
        val filas = listOf(listOf("247" to "TIEMPO"), listOf("1:12:40" to "POTENCIA MEDIA"))
        filas.flatten().forEach { (valor, siguiente) ->
            val cifra = compose.onNodeWithText(valor).getUnclippedBoundsInRoot()
            val etiqueta = compose.onNodeWithText(siguiente).getUnclippedBoundsInRoot()
            assertTrue("$valor invade $siguiente", cifra.bottom <= etiqueta.top)
        }
        val ultima = compose.onNodeWithText("231").getUnclippedBoundsInRoot()
        val pie = compose.onNodeWithText("Toca las conexiones para conectar el Neo y el Square").getUnclippedBoundsInRoot()
        assertTrue("la última fila invade el pie", ultima.bottom <= pie.top)
    }

    // --- Área de datos fija (change rejilla-datos-fija) --------------------------------------------------------------

    private val cuatro = seis.take(4)
    private val base = estadoUi(zwift = esperando, marcha = 14, potencia = 247, cadencia = 91, neo = ok, square = ok, pie = PieUi.Nada)

    private fun alto(texto: String) = compose.onNodeWithText(texto).getUnclippedBoundsInRoot().let { (it.bottom - it.top).value }

    @Test
    fun misma_rejilla_en_lineal_shimano_y_erg() {
        // Spec `ride-screen`, «Misma área en lineal y con la Shimano» y «Misma área en ERG».
        var estado by mutableStateOf(base.copy(datos = cuatro))
        compose.setContent { SqTema { Box(Modifier.requiredSize(390.dp, 844.dp)) { Rodaje(estado, {}, animar = false) } } }
        val lineal = compose.onNodeWithText("247").getUnclippedBoundsInRoot()
        estado = conShimano(52, 17).copy(datos = cuatro)
        assertEquals(lineal, compose.onNodeWithText("247").getUnclippedBoundsInRoot())
        estado = base.copy(datos = cuatro, erg = ErgUi(200, 200, 0, manual = true))
        assertEquals(lineal, compose.onNodeWithText("247").getUnclippedBoundsInRoot())
    }

    @Test
    fun los_botones_no_cambian_la_cifra_en_vertical() {
        // Spec `ride-screen`, «Dos datos en vertical» y «Seis datos con botones».
        var estado by mutableStateOf(base)
        compose.setContent { SqTema { Box(Modifier.requiredSize(390.dp, 844.dp)) { Rodaje(estado, {}, animar = false) } } }
        val cifra = alto("247")
        val marcha = compose.onNodeWithContentDescription("Marcha 14 de 24").getUnclippedBoundsInRoot()
        estado = base.copy(botones = true)
        assertEquals(cifra, alto("247"), 0.5f)
        // Con 2 datos la rejilla cede todo el hueco: la marcha no cambia de tamaño.
        val conBotones = compose.onNodeWithContentDescription("Marcha 14 de 24").getUnclippedBoundsInRoot()
        assertEquals((marcha.bottom - marcha.top).value, (conBotones.bottom - conBotones.top).value, 0.5f)
        estado = base.copy(datos = seis)
        val seisSin = alto("1:12:40")
        estado = base.copy(datos = seis, botones = true)
        assertEquals(seisSin, alto("1:12:40"), 0.5f)
        listOf("247", "91", "1:12:40", "3,3", "231", "612").forEach { compose.onNodeWithText(it).assertIsDisplayed() }
    }

    @Test
    fun los_botones_no_cambian_la_cifra_en_horizontal() {
        // Spec `ride-screen`, «Seis datos en horizontal»: la columna de datos no cambia y la marcha se reduce.
        var estado by mutableStateOf(conShimano(52, 17).copy(datos = seis))
        compose.setContent { SqTema { Box(Modifier.requiredSize(844.dp, 390.dp)) { Rodaje(estado, {}, animar = false) } } }
        val dato = compose.onNodeWithText("612").getUnclippedBoundsInRoot()
        val marcha = compose.onNodeWithContentDescription("Plato 52, piñón 17").getUnclippedBoundsInRoot()
        estado = estado.copy(botones = true)
        compose.waitForIdle()
        val datoConBotones = compose.onNodeWithText("612").getUnclippedBoundsInRoot()
        assertEquals((dato.bottom - dato.top).value, (datoConBotones.bottom - datoConBotones.top).value, 0.5f)
        val marchaConBotones = compose.onNodeWithContentDescription("Plato 52, piñón 17").getUnclippedBoundsInRoot()
        assertTrue((marchaConBotones.right - marchaConBotones.left).value <= (marcha.right - marcha.left).value + 0.01f)
        // La marcha, reducida, no invade la columna de datos.
        assertTrue(marchaConBotones.right <= compose.onNodeWithText("POTENCIA").getUnclippedBoundsInRoot().left)
        // Con 2 datos, la cifra tampoco cambia.
        estado = conShimano(52, 17)
        val dos = alto("247")
        estado = conShimano(52, 17).copy(botones = true)
        assertEquals(dos, alto("247"), 0.5f)
    }

    @Test
    fun cuatro_datos_con_botones_en_un_movil_pequeno() {
        // Spec `ride-screen`, «Cuatro datos con botones en pantalla baja»: a 360 × 640 con la Shimano y 6 datos elegidos se ven la marcha, los 4 primeros y los botones.
        mostrarEn(conShimano(52, 17).copy(datos = seis, botones = true), DpSize(360.dp, 640.dp))
        compose.onNodeWithContentDescription("Plato 52, piñón 17").assertIsDisplayed()
        listOf("247", "91", "1:12:40", "3,3").forEach { compose.onNodeWithText(it).assertIsDisplayed() }
        // El quinto y el sexto se ocultan sin cambiar los elegidos.
        listOf("231", "612").forEach { compose.onAllNodesWithText(it).assertCountEquals(0) }
        compose.onNodeWithContentDescription("Una marcha más dura").assertIsDisplayed()
        compose.onNodeWithContentDescription("Una marcha más ligera").assertIsDisplayed()
        val ultima = compose.onNodeWithText("3,3").getUnclippedBoundsInRoot()
        assertTrue(ultima.bottom <= compose.onNodeWithContentDescription("Una marcha más dura").getUnclippedBoundsInRoot().top)
    }

    @Test
    fun seis_datos_sin_botones_en_un_movil_pequeno() {
        // Spec `ride-screen`, «Seis datos sin botones en pantalla baja».
        mostrarEn(conShimano(52, 17).copy(datos = seis, botones = false), DpSize(360.dp, 640.dp))
        compose.onNodeWithContentDescription("Plato 52, piñón 17").assertIsDisplayed()
        listOf("247", "91", "1:12:40", "3,3", "231", "612").forEach { compose.onNodeWithText(it).assertIsDisplayed() }
        compose.onAllNodesWithContentDescription("Una marcha más dura").assertCountEquals(0)
    }

    @Test
    fun cuatro_datos_con_botones_a_360_x_720_ocultan_el_quinto_y_el_sexto() {
        // Spec `ride-screen`, «Móvil de 720 dp»: el umbral es 760 dp.
        mostrarEn(conShimano(52, 17).copy(datos = seis, botones = true), DpSize(360.dp, 720.dp))
        compose.onNodeWithContentDescription("Plato 52, piñón 17").assertIsDisplayed()
        compose.onAllNodesWithText("231").assertCountEquals(0)
        compose.onAllNodesWithText("612").assertCountEquals(0)
    }

    @Test
    fun seis_datos_con_botones_en_una_pantalla_alta() {
        // Spec `ride-screen`, «Pantalla alta»: 390 × 844 no recorta.
        mostrarEn(conShimano(52, 17).copy(datos = seis, botones = true), DpSize(390.dp, 844.dp))
        compose.onNodeWithContentDescription("Plato 52, piñón 17").assertIsDisplayed()
        listOf("247", "91", "1:12:40", "3,3", "231", "612").forEach { compose.onNodeWithText(it).assertIsDisplayed() }
        compose.onNodeWithContentDescription("Una marcha más dura").assertIsDisplayed()
    }

    @Test
    fun cuatro_datos_con_botones_en_erg_en_un_movil_pequeno() {
        // Spec `ride-screen`, «En ERG»: la potencia objetivo, los 4 primeros datos y los botones.
        mostrarEn(ergZwift.copy(datos = seis, botones = true), DpSize(360.dp, 640.dp))
        compose.onNodeWithContentDescription("Potencia objetivo 240 W").assertIsDisplayed()
        listOf("247", "91", "1:12:40", "3,3").forEach { compose.onNodeWithText(it).assertIsDisplayed() }
        compose.onAllNodesWithText("231").assertCountEquals(0)
        compose.onNodeWithContentDescription("Una marcha más dura").assertIsDisplayed()
    }

    @Test
    fun cinco_datos_en_vertical_con_el_quinto_centrado() {
        // Spec `ride-screen`, «Cinco datos en vertical».
        mostrarEn(base.copy(datos = seis.take(5)), DpSize(390.dp, 844.dp))
        // El centro de la rejilla es el punto medio entre los centros de las dos columnas de arriba.
        fun centro(texto: String) = compose.onNodeWithText(texto).getUnclippedBoundsInRoot().let { ((it.left + it.right) / 2).value }
        assertEquals((centro("POTENCIA") + centro("CADENCIA")) / 2, centro("POTENCIA MEDIA"), 1f)
    }

    private fun mostrarEn(e: RodajeUiState, tamano: DpSize) {
        compose.setContent { SqTema { Box(Modifier.requiredSize(tamano)) { Rodaje(e, {}, animar = false) } } }
    }

    @Test
    fun plataforma_con_la_barra_en_una_fila_y_el_modo_con_ajustes_debajo_a_360_x_640() {
        // Spec `ride-screen`, «Línea de chips en vertical con el icono»: la rejilla conserva su alto.
        var estado by mutableStateOf(
            estadoUi(neo = conectando, square = sin, zwift = esperando, mando = esperando, marcha = 14, potencia = 247, cadencia = 91, pie = PieUi.DespertarNeo),
        )
        compose.setContent { SqTema { Box(Modifier.requiredSize(360.dp, 640.dp)) { Rodaje(estado, {}, animar = false) } } }
        compose.onNodeWithContentDescription("Conexiones: Neo, conectando; Controlador, sin conexión").assertIsDisplayed()
        val barra = barra().getUnclippedBoundsInRoot()
        val ajustes = compose.onNodeWithContentDescription("Ajustes").assertIsDisplayed().getUnclippedBoundsInRoot()
        val estadoPlataforma = compose.onNodeWithText("Esperando conexión…").assertIsDisplayed().getUnclippedBoundsInRoot()
        val modo = compose.onNodeWithText("Lineal · 24 marchas").assertIsDisplayed().getUnclippedBoundsInRoot()
        // El estado de la plataforma y la barra, en una fila; debajo, el modo con Ajustes al final.
        assertEquals(centroY(estadoPlataforma).value, centroY(barra).value, 8f)
        assertTrue(estadoPlataforma.right <= barra.left)
        assertTrue(barra.right <= 360.dp)
        assertTrue(barra.bottom <= ajustes.top && barra.bottom <= modo.top)
        assertEquals(centroY(modo).value, centroY(ajustes).value, 8f)
        assertTrue(modo.right <= ajustes.left)
        val datos = compose.onNodeWithText("247").getUnclippedBoundsInRoot()
        estado = estado.con(neo = ok, square = ok, zwift = ok, mando = ok).copy(pie = PieUi.Nada)
        assertEquals(datos, compose.onNodeWithText("247").getUnclippedBoundsInRoot())
    }

    @Test
    fun la_fila_de_la_plataforma_no_se_solapa_con_el_modo() {
        // Spec `ride-screen`, «Móvil sin cámara en el borde»: sin franja propia, la fila de la barra mide al menos 48 dp y el modo queda debajo.
        mostrar(ruta, vertical = true)
        val barra = barra().assertIsDisplayed().getUnclippedBoundsInRoot()
        val modo = compose.onNodeWithText("SIM").assertIsDisplayed().getUnclippedBoundsInRoot()
        assertTrue(barra.bottom <= modo.top)
        assertTrue(barra.bottom - barra.top >= 48.dp)
    }

    @Test
    fun transmision_larga_junto_a_ajustes_se_lee_entera() {
        // Spec `ride-screen`, «Transmisión larga junto a Ajustes»: «Shimano 105 · automático» y el modo ERG de Zwift a 360 dp, enteros.
        var estado by mutableStateOf(conShimano(52, 17))
        compose.setContent { SqTema { Box(Modifier.requiredSize(360.dp, 720.dp)) { Rodaje(estado, {}, animar = false) } } }
        for (e in listOf(conShimano(52, 17), ergZwift)) {
            estado = e
            val texto = compose.onNodeWithText(if (e.erg == null) "Shimano 105 · automático" else "APP", substring = true)
                .assertIsDisplayed().getUnclippedBoundsInRoot()
            assertTrue(texto.right <= 360.dp)
        }
    }

    // --- Estado de la plataforma y conexiones (spec `ride-screen`, «Disposición en vertical» y «Línea superior en horizontal») ---

    @Test
    fun el_estado_largo_y_cuatro_enlaces_caben_en_una_fila_a_360_dp() {
        // «Estado de la publicación largo junto a las conexiones»: se lee entero y los iconos no se empujan.
        val sinMando = ergZwift.con(mando = esperando)
        val e = sinMando.copy(enlaces = sinMando.enlaces + EnlaceUi(Enlace.Pulso, EstadoChip.Ok, null) + EnlaceUi(Enlace.Ventilador, EstadoChip.Ok, null))
        compose.setContent { SqTema { Box(Modifier.requiredSize(360.dp, 640.dp)) { Rodaje(e, {}, animar = false) } } }
        val plataforma = compose.onNodeWithText("Conectado · sin controlador").assertIsDisplayed().getUnclippedBoundsInRoot()
        val barra = barra().assertIsDisplayed().getUnclippedBoundsInRoot()
        assertTrue("el estado (${plataforma.right}) acaba antes de la barra (${barra.left})", plataforma.right <= barra.left)
        assertTrue(barra.right <= 360.dp)
    }

    private fun centroX(r: androidx.compose.ui.unit.DpRect) = (r.left + r.right) / 2

    private fun estadoDePlataforma() = compose.onNodeWithContentDescription("Conexión: rodillo", substring = true)

    @Test
    fun modo_a_la_izquierda_estado_centrado_y_conexiones_con_ajustes_a_la_derecha_en_horizontal() {
        // «Línea superior en horizontal» y «Estado de la plataforma centrado», con la Shimano 105 en automático a 640 × 360.
        mostrarEn(conShimano(52, 17), DpSize(640.dp, 360.dp))
        val raiz = compose.onRoot().getUnclippedBoundsInRoot()
        val modo = compose.onNodeWithText("Shimano 105 · automático", substring = true).assertIsDisplayed().getUnclippedBoundsInRoot()
        val sim = compose.onNodeWithText("LIBRE").getUnclippedBoundsInRoot()
        val plataforma = estadoDePlataforma().getUnclippedBoundsInRoot()
        val barra = barra().getUnclippedBoundsInRoot()
        val ajustes = compose.onNodeWithContentDescription("Ajustes").getUnclippedBoundsInRoot()
        assertEquals("el estado está centrado", centroX(raiz).value, centroX(plataforma).value, 1f)
        assertTrue(modo.right <= plataforma.left)
        assertTrue(plataforma.right <= barra.left)
        assertTrue(barra.right <= ajustes.left)
        assertTrue("las zonas táctiles no se solapan", ajustes.left - barra.right <= 16.dp)
        assertEquals("el modo va en la misma línea", centroY(sim).value, centroY(barra).value, 8f)
        assertEquals(centroY(barra).value, centroY(ajustes).value, 1f)
    }

    @Test
    fun la_marcha_empieza_bajo_la_linea_superior_sin_el_hueco_del_modo_en_horizontal() {
        // «Más sitio para la marcha y los datos»: nada reserva la esquina de la zona principal.
        mostrarEn(conShimano(52, 17), DpSize(640.dp, 360.dp))
        val barra = barra().getUnclippedBoundsInRoot()
        val datos = compose.onNodeWithText("247").getUnclippedBoundsInRoot()
        assertTrue("los datos (${datos.top}) empiezan a menos de 32 dp bajo la línea (${barra.bottom})", datos.top - barra.bottom < 32.dp)
    }

    @Test
    fun todo_largo_a_la_anchura_mas_estrecha_la_transmision_cede_y_el_estado_sigue_centrado() {
        // «Todo largo a la anchura más estrecha»: Shimano 105 en automático, «Conectado · sin controlador» y cuatro enlaces a 640 × 360.
        val base = conShimano(52, 17).con(zwift = ok, mando = esperando)
        val e = base.copy(enlaces = base.enlaces + EnlaceUi(Enlace.Pulso, EstadoChip.Ok, null) + EnlaceUi(Enlace.Ventilador, EstadoChip.Ok, null))
        mostrarEn(e, DpSize(640.dp, 360.dp))
        val raiz = compose.onRoot().getUnclippedBoundsInRoot()
        val plataforma = estadoDePlataforma().getUnclippedBoundsInRoot()
        val modo = compose.onNodeWithText("Shimano 105 · automático", substring = true).getUnclippedBoundsInRoot()
        val barra = barra().getUnclippedBoundsInRoot()
        val ajustes = compose.onNodeWithContentDescription("Ajustes").getUnclippedBoundsInRoot()
        compose.onNodeWithText("Conectado · sin controlador").assertExists()
        assertEquals(centroX(raiz).value, centroX(plataforma).value, 1f)
        assertTrue(modo.right <= plataforma.left)
        assertTrue(plataforma.right <= barra.left)
        assertTrue(barra.right <= ajustes.left)
    }

    // --- Acceso a Ajustes (spec `ride-screen`, «Ajustes desde Rodaje») ---

    private fun centroY(r: androidx.compose.ui.unit.DpRect) = (r.top + r.bottom) / 2

    @Test
    fun icono_de_ajustes_al_final_de_la_fila_del_modo_en_vertical() {
        // «Ajustes lejos de las conexiones en vertical»: a 360 × 640, Neo conectando, Square sin conexión y Zwift esperando; Ajustes al final de la fila del modo.
        var estado by mutableStateOf(
            estadoUi(zwift = esperando, marcha = 14, potencia = 247, cadencia = 91, neo = conectando, square = sin, pie = PieUi.DespertarNeo),
        )
        var abierto = 0
        compose.setContent {
            SqTema { Box(Modifier.requiredSize(360.dp, 640.dp)) { Rodaje(estado, {}, abrirAjustes = { abierto++ }, animar = false) } }
        }
        val icono = compose.onNodeWithContentDescription("Ajustes").assertIsDisplayed().assertHasClickAction()
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button)).getUnclippedBoundsInRoot()
        val barra = barra().assertIsDisplayed().getUnclippedBoundsInRoot()
        val modo = compose.onNodeWithText("Lineal · 24 marchas").getUnclippedBoundsInRoot()
        // En la misma fila que el modo, a su derecha y en la esquina, bajo la barra (sin solapar su objetivo táctil) y sin salirse de la pantalla.
        assertEquals(centroY(modo).value, centroY(icono).value, 8f)
        assertTrue(icono.left > modo.right)
        assertTrue(icono.top >= barra.bottom)
        assertTrue(icono.right <= 360.dp)
        assertTrue(icono.right >= 360.dp - 24.dp - 1.dp)
        val datos = compose.onNodeWithText("247").assertIsDisplayed().getUnclippedBoundsInRoot()
        estado = estado.con(neo = ok, square = ok, zwift = ok).copy(pie = PieUi.Nada)
        assertEquals(icono, compose.onNodeWithContentDescription("Ajustes").getUnclippedBoundsInRoot())
        assertEquals(datos, compose.onNodeWithText("247").getUnclippedBoundsInRoot())
        compose.onNodeWithContentDescription("Ajustes").performClick()
        assertEquals(1, abierto)
    }

    @Test
    fun icono_de_ajustes_a_la_derecha_de_la_barra_en_horizontal() {
        var abierto = 0
        compose.setContent {
            SqTema { Box(Modifier.requiredSize(844.dp, 390.dp)) { Rodaje(ruta, {}, abrirAjustes = { abierto++ }, animar = false) } }
        }
        val icono = compose.onNodeWithContentDescription("Ajustes").assertExists().getUnclippedBoundsInRoot()
        val barra = barra().getUnclippedBoundsInRoot()
        assertEquals(centroY(barra).value, centroY(icono).value, 1f)
        assertTrue(icono.left > barra.right)
        // La barra, a la derecha del estado de la plataforma y antes de Ajustes, con 12 dp entre ellas (el icono mide 32 dp y su zona táctil 48).
        assertTrue(barra.left > estadoDePlataforma().getUnclippedBoundsInRoot().right)
        assertTrue(icono.left - barra.right >= 0.dp)
        compose.onNodeWithContentDescription("Ajustes").performClick()
        assertEquals(1, abierto)
    }

    @Test
    fun icono_de_ajustes_sin_dispositivos_y_en_erg() {
        // «Sin dispositivos» y «En ERG»: el icono sigue en su sitio.
        var estado by mutableStateOf(estadoUi(zwift = esperando, marcha = 12))
        compose.setContent { SqTema { Box(Modifier.requiredSize(360.dp, 720.dp)) { Rodaje(estado, {}, animar = false) } } }
        val icono = compose.onNodeWithContentDescription("Ajustes").assertIsDisplayed().getUnclippedBoundsInRoot()
        compose.onNodeWithText("Toca las conexiones para conectar el Neo y el Square").assertExists()
        estado = ergManual
        assertEquals(icono, compose.onNodeWithContentDescription("Ajustes").assertIsDisplayed().getUnclippedBoundsInRoot())
    }

    @Test
    fun velocidad_y_distancia_en_la_rejilla() {
        // Spec `ride-screen`, «Velocidad y distancia en Rodaje».
        mostrar(
            estadoUi().copy(
                datos = listOf(
                    DatoUi(DatoRodaje.POTENCIA, "247", "W"),
                    DatoUi(DatoRodaje.CADENCIA, "91", "rpm"),
                    DatoUi(DatoRodaje.VELOCIDAD, "24,0", "km/h"),
                    DatoUi(DatoRodaje.DISTANCIA, "18,40", "km"),
                ),
            ),
        )
        compose.onNodeWithText("VELOCIDAD", ignoreCase = true).assertExists()
        compose.onNodeWithText("DISTANCIA", ignoreCase = true).assertExists()
        compose.onNodeWithText("24,0", substring = true).assertExists()
        compose.onNodeWithText("18,40", substring = true).assertExists()
    }

    @Test
    fun velocidad_sin_valor_muestra_raya() {
        mostrar(
            estadoUi().copy(datos = listOf(DatoUi(DatoRodaje.VELOCIDAD, null, "km/h"), DatoUi(DatoRodaje.DISTANCIA, "3,20", "km"))),
        )
        compose.onNodeWithText("VELOCIDAD", ignoreCase = true).assertExists()
        compose.onNodeWithText("3,20", substring = true).assertExists()
    }

    // --- Aviso de giro (spec `ride-screen`, «Aviso de giro») ---------------------------------------------------------

    private val girando = estadoUi(zwift = ok, mando = ok, marcha = 14, potencia = 247, cadencia = 91, neo = ok, square = ok, pie = PieUi.Nada, botones = true)

    private fun medidas(vararg descripciones: String) = descripciones.map { compose.onNodeWithContentDescription(it).getUnclippedBoundsInRoot() }

    private fun comprobarQueNadaSeMueve(base: RodajeUiState, vertical: Boolean, marcha: String) {
        var e by mutableStateOf(base)
        val tamano = if (vertical) DpSize(360.dp, 720.dp) else DpSize(844.dp, 390.dp)
        compose.setContent { SqTema { Box(Modifier.requiredSize(tamano)) { Rodaje(e, {}, animar = false) } } }
        val comunes = arrayOf(marcha, "Una marcha más dura", "Una marcha más ligera")
        val sin = medidas(*comunes).plus(compose.onNodeWithText("247").getUnclippedBoundsInRoot())
        e = base.copy(giro = setOf(LadoGiro.IZQUIERDA))
        compose.waitForIdle()
        assertEquals("izquierda", sin, medidas(*comunes).plus(compose.onNodeWithText("247").getUnclippedBoundsInRoot()))
        e = base.copy(giro = setOf(LadoGiro.IZQUIERDA, LadoGiro.DERECHA))
        compose.waitForIdle()
        assertEquals("los dos", sin, medidas(*comunes).plus(compose.onNodeWithText("247").getUnclippedBoundsInRoot()))
    }

    @Test
    fun el_giro_no_mueve_nada_en_vertical_y_en_horizontal() {
        comprobarQueNadaSeMueve(girando, vertical = true, marcha = "Marcha 14 de 24")
    }

    @Test
    fun el_giro_no_mueve_nada_en_horizontal() {
        comprobarQueNadaSeMueve(girando, vertical = false, marcha = "Marcha 14 de 24")
    }

    @Test
    fun el_giro_se_anuncia_y_cada_flecha_va_en_su_lado() {
        var e by mutableStateOf(girando)
        compose.setContent { SqTema { Box(Modifier.requiredSize(360.dp, 720.dp)) { Rodaje(e, {}, animar = false) } } }
        compose.onAllNodesWithContentDescription("Girando", substring = true).assertCountEquals(0)
        val marcha = compose.onNodeWithContentDescription("Marcha 14 de 24").getUnclippedBoundsInRoot()
        e = girando.copy(giro = setOf(LadoGiro.IZQUIERDA))
        compose.waitForIdle()
        val izquierda = compose.onNodeWithContentDescription("Girando a la izquierda").assertIsDisplayed().getUnclippedBoundsInRoot()
        assertTrue("flecha $izquierda marcha $marcha", (izquierda.left + izquierda.right).value / 2 < (marcha.left + marcha.right).value / 2)
        e = girando.copy(giro = setOf(LadoGiro.DERECHA))
        compose.waitForIdle()
        val derecha = compose.onNodeWithContentDescription("Girando a la derecha").assertIsDisplayed().getUnclippedBoundsInRoot()
        assertTrue("flecha $derecha marcha $marcha", (derecha.left + derecha.right).value / 2 > (marcha.left + marcha.right).value / 2)
        compose.onAllNodesWithContentDescription("Girando a la izquierda").assertCountEquals(0)
    }

    @Test
    fun con_la_shimano_el_giro_no_tapa_la_marcha() {
        var e by mutableStateOf(conShimano(52, 17))
        compose.setContent { SqTema { Box(Modifier.requiredSize(360.dp, 720.dp)) { Rodaje(e, {}, animar = false) } } }
        val sin = compose.onNodeWithContentDescription("Plato 52, piñón 17", substring = true).getUnclippedBoundsInRoot()
        e = e.copy(giro = setOf(LadoGiro.DERECHA))
        compose.waitForIdle()
        compose.onNodeWithContentDescription("Girando a la derecha").assertIsDisplayed()
        assertEquals(sin, compose.onNodeWithContentDescription("Plato 52, piñón 17", substring = true).getUnclippedBoundsInRoot())
    }

    // --- Pulsómetro: #estado=pulso-conectado, pulso-reconectando, pulso-sin-conexion y cinco-enlaces ---

    private val reconectandoPulso = Chip(EstadoChip.Esperando, EnlaceUi.EnPalabra.Reconectando)

    @Test
    fun cinco_enlaces_a_360_dp_caben_sin_recortarse_ni_cambiar_el_ancho_con_el_estado() {
        var estado by mutableStateOf(estadoUi(zwift = ok, marcha = 14, neo = ok, square = ok, mando = ok, pulso = ok, pie = PieUi.Nada))
        compose.setContent { SqTema { Box(Modifier.requiredSize(360.dp, 720.dp)) { Rodaje(estado, {}, animar = false) } } }
        val ancho = barra().assertIsDisplayed().getUnclippedBoundsInRoot().let { it.right - it.left }
        val ajustes = compose.onNodeWithContentDescription("Ajustes").getUnclippedBoundsInRoot()
        for (p in listOf(ok, reconectandoPulso, sin, ok)) {
            estado = estadoUi(zwift = ok, marcha = 14, neo = ok, square = ok, mando = ok, pulso = p, pie = PieUi.Nada)
            val b = barra().assertIsDisplayed().getUnclippedBoundsInRoot()
            assertEquals("$p", ancho, b.right - b.left)
            val cruzan = b.left < ajustes.right && b.right > ajustes.left && b.top < ajustes.bottom && b.bottom > ajustes.top
            assertTrue("$p: la barra no puede pisar Ajustes", !cruzan)
            assertTrue("$p: la barra cabe en la pantalla", b.left >= 0.dp && b.right <= 360.dp)
        }
    }

    @Test
    fun la_descripcion_de_la_barra_dice_el_estado_del_pulsometro() {
        var estado by mutableStateOf(estadoUi(neo = ok, square = ok, zwift = ok, mando = ok, pulso = ok, pie = PieUi.Nada))
        compose.setContent { SqTema { Box(Modifier.requiredSize(360.dp, 720.dp)) { Rodaje(estado, {}, animar = false) } } }
        for ((chip, palabra) in listOf(ok to "conectado", reconectandoPulso to "reconectando", sin to "sin conexión")) {
            estado = estadoUi(neo = ok, square = ok, zwift = ok, mando = ok, pulso = chip, pie = PieUi.Nada)
            compose.onNode(hasContentDescription("Pulsómetro, $palabra", substring = true)).assertExists()
        }
    }

    @Test
    fun sin_pulsometro_configurado_la_barra_no_nombra_el_pulso_ni_hay_numero_de_pulso() {
        compose.setContent { SqTema { Box(Modifier.requiredSize(360.dp, 720.dp)) { Rodaje(ruta, {}, animar = false) } } }
        compose.onNode(hasContentDescription("Pulsómetro", substring = true)).assertDoesNotExist()
        compose.onNodeWithText("bpm", substring = true).assertDoesNotExist()
    }

    @Test
    fun el_estado_de_la_plataforma_por_bluetooth() {
        // #estado=bt-sin-bluetooth y bt-no-disponible de maquetas/rodaje.html: sin hablar de wifi ni de mando.
        val base = estadoUi(neo = ok, square = ok, marcha = 14, pie = PieUi.Nada)
        mostrar(base.copy(plataforma = PlataformaUi(EstadoChip.Sin, PlataformaUi.Texto.SinBluetooth, EnlaceUi.EnPalabra.SinConexion)))
        compose.onNodeWithText("Sin Bluetooth").assertExists()
        compose.onNodeWithText("Sin wifi").assertDoesNotExist()
    }

    @Test
    fun el_estado_de_la_plataforma_por_bluetooth_en_un_movil_sin_modo_periferico() {
        val base = estadoUi(neo = ok, square = ok, marcha = 14, pie = PieUi.Nada)
        mostrar(base.copy(plataforma = PlataformaUi(EstadoChip.Sin, PlataformaUi.Texto.NoDisponible, EnlaceUi.EnPalabra.NoDisponible)))
        compose.onNodeWithText("No disponible").assertExists()
        compose.onNodeWithText("Conectado · sin controlador").assertDoesNotExist()
    }
}
