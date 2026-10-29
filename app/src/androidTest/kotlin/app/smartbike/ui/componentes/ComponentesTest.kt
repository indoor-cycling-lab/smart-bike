package app.smartbike.ui.componentes

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.click
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.width
import app.smartbike.ui.createComposeRuleEs
import app.smartbike.ui.navegacion.Pestana
import app.smartbike.ui.tema.ColoresClaro
import app.smartbike.ui.tema.ColoresRodaje
import app.smartbike.ui.tema.SqTema
import app.smartbike.ui.tema.Tema
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ComponentesTest {
    @get:Rule
    val compose = createComposeRuleEs()

    private val temas = listOf(Tema.Rodaje to ColoresRodaje, Tema.Claro to ColoresClaro)

    /** Color más frecuente de la captura del nodo: el fondo de la tecla (el texto ocupa poco). */
    private fun colorDeFondo(tag: String): Color {
        val mapa = compose.onNodeWithTag(tag).captureToImage().toPixelMap()
        val cuenta = HashMap<Color, Int>()
        for (x in 0 until mapa.width) for (y in 0 until mapa.height) cuenta.merge(mapa[x, y], 1, Int::plus)
        return cuenta.maxBy { it.value }.key
    }

    @Test
    fun tecla_pulsada_en_los_dos_temas() {
        var tema by mutableStateOf(Tema.Rodaje)
        compose.setContent { SqTema(tema) { SquareKey("Palanca D1", Modifier.testTag("tecla"), ancha = true, pulsada = true) } }
        for ((t, colores) in temas) {
            tema = t
            compose.waitForIdle()
            compose.onNodeWithTag("tecla").assertIsSelected()
            assertEquals(colores.gearFill, colorDeFondo("tecla"))
        }
    }

    @Test
    fun tecla_suelta() {
        compose.setContent { SqTema { SquareKey("A", Modifier.testTag("tecla")) } }
        assertEquals(ColoresRodaje.surface2, colorDeFondo("tecla"))
    }

    @Test
    fun stepper_en_el_maximo() {
        var sumas = 0
        var restas = 0
        compose.setContent {
            SqTema {
                Stepper(
                    "Pendiente",
                    "20,0",
                    "%",
                    alRestar = { restas++ },
                    alSumar = { sumas++ },
                    descripcionRestar = "Bajar pendiente",
                    descripcionSumar = "Subir pendiente",
                    puedeSumar = false,
                )
            }
        }
        compose.onNodeWithContentDescription("Subir pendiente").assertIsNotEnabled().performClick()
        compose.onNodeWithContentDescription("Bajar pendiente").assertIsEnabled().performClick()
        compose.waitForIdle()
        assertEquals(0, sumas)
        assertEquals(1, restas)
    }

    @Test
    fun boton_deshabilitado() {
        var pulsado = false
        compose.setContent { SqTema { Button("Enviar potencia", { pulsado = true }, habilitado = false) } }
        compose.onNodeWithText("Enviar potencia").assertIsNotEnabled().performClick()
        assertEquals(false, pulsado)
    }

    @Test
    fun aviso_de_error_y_chip_en_los_dos_temas() {
        var tema by mutableStateOf(Tema.Rodaje)
        compose.setContent {
            SqTema(tema) {
                Column {
                    Notice("No se encuentra el Neo.", error = true)
                    ConnectionChip(EstadoChip.Sin, "No encontrado")
                    ConnectionChip(EstadoChip.Esperando, "Buscando")
                }
            }
        }
        for ((t, _) in temas) {
            tema = t
            compose.onNodeWithText("No se encuentra el Neo.").assertExists()
            compose.onNodeWithText("No encontrado").assertExists()
            compose.onNodeWithText("Buscando").assertExists()
        }
    }

    @Test
    fun volcado_vacio_y_con_tramas() {
        var filas by mutableStateOf(emptyList<FilaHex>())
        compose.setContent { SqTema { HexLog(filas, vacio = "Sin tráfico todavía", descripcion = "Tráfico") } }
        compose.onNodeWithText("Sin tráfico todavía").assertExists()
        filas = listOf(FilaHex("12:04:31.236", "→ Neo", "fec3", "a4 09 4e", enviada = true, cambiados = setOf(1)))
        compose.onNodeWithText("→ Neo").assertExists()
        compose.onNodeWithText("fec3").assertExists()
        compose.onNodeWithText("Sin tráfico todavía").assertDoesNotExist()
    }

    // --- Rodaje --------------------------------------------------------------------------------------------------

    @Test
    fun marcha_normal_y_en_tope() {
        var tope by mutableStateOf<String?>(null)
        compose.setContent {
            SqTema {
                GearDisplay(
                    24, 24, "Marcha", "de 24", "Marcha 24 de 24", Modifier.testTag("marcha"),
                    tope = tope, reservaTope = "Tope · marcha más ligera", animar = false,
                )
            }
        }
        compose.onNodeWithContentDescription("Marcha 24 de 24").assertExists()
        compose.onNodeWithText("Tope · marcha más dura").assertDoesNotExist()
        compose.onNodeWithText("Tope · marcha más ligera").assertDoesNotExist()
        val sinTope = compose.onNodeWithTag("marcha").getUnclippedBoundsInRoot()
        tope = "Tope · marcha más dura"
        compose.onNodeWithText("Tope · marcha más dura").assertExists()
        // El hueco estaba reservado: la marcha mide lo mismo y no empuja nada.
        assertEquals(sinTope, compose.onNodeWithTag("marcha").getUnclippedBoundsInRoot())
    }

    @Test
    fun marcha_fisica_normal_y_en_tope() {
        var tope by mutableStateOf<String?>(null)
        compose.setContent {
            SqTema {
                GearDisplay(52, 17, "Plato 52, piñón 17", Modifier.testTag("marcha"), tope = tope, reservaTope = "Tope · marcha más ligera", animar = false)
            }
        }
        compose.onNodeWithContentDescription("Plato 52, piñón 17").assertExists()
        val sinTope = compose.onNodeWithTag("marcha").getUnclippedBoundsInRoot()
        tope = "Tope · plato 52"
        compose.onNodeWithText("Tope · plato 52").assertExists()
        assertEquals(sinTope, compose.onNodeWithTag("marcha").getUnclippedBoundsInRoot())
    }

    @Test
    fun platos_y_cassette() {
        compose.setContent {
            SqTema {
                Column {
                    ChainringIndicator(listOf(36, 52), 52, "Plato", "Plato 52")
                    CassetteView(listOf(11, 12, 13, 14, 15, 17, 19, 21, 24, 28, 32, 36), 17, "Cassette 11 a 36, piñón 17")
                    // Con un solo plato no se muestra.
                    ChainringIndicator(listOf(40), 40, "Plato", "Plato 40")
                }
            }
        }
        compose.onNodeWithContentDescription("Plato 52").assertExists()
        compose.onNodeWithContentDescription("Cassette 11 a 36, piñón 17").assertExists()
        compose.onNodeWithContentDescription("Plato 40").assertDoesNotExist()
    }

    @Test
    fun rejilla_con_y_sin_dato() {
        compose.setContent {
            SqTema { MetricGrid(listOf(Dato("Potencia", "247", "W"), Dato("Cadencia", null, "rpm")), descripcionSinDato = "Sin dato") }
        }
        compose.onNodeWithText("247").assertExists()
        compose.onNodeWithText("W").assertExists()
        compose.onNodeWithContentDescription("Sin dato").assertExists()
        compose.onNodeWithText("rpm").assertDoesNotExist()
    }

    @Test
    fun rejilla_en_vertical_con_dos_datos_en_una_fila() {
        compose.setContent {
            SqTema {
                MetricGrid(listOf(Dato("Potencia", "247", "W"), Dato("Cadencia", "91", "rpm")), Modifier.size(342.dp, 300.dp), vertical = true)
            }
        }
        val potencia = compose.onNodeWithText("247").getUnclippedBoundsInRoot()
        val cadencia = compose.onNodeWithText("91").getUnclippedBoundsInRoot()
        assertEquals(potencia.top, cadencia.top)
        assertTrue(cadencia.left >= potencia.right)
        assertTrue((potencia.bottom - potencia.top).value <= 144f)
    }

    @Test
    fun rejilla_con_la_cifra_estable() {
        // Spec `ui-components`, «Valor largo»: medida con la plantilla, la cifra no cambia al crecer el valor.
        var potencia by mutableStateOf("999")
        var tiempo by mutableStateOf("59:59")
        compose.setContent {
            SqTema {
                MetricGrid(
                    listOf(Dato("Potencia", potencia, "W", plantilla = "8888"), Dato("Tiempo", tiempo, plantilla = "8:88:88")),
                    Modifier.size(300.dp, 260.dp),
                )
            }
        }
        val antes = compose.onNodeWithText("999").getUnclippedBoundsInRoot()
        val tiempoAntes = compose.onNodeWithText("59:59").getUnclippedBoundsInRoot()
        potencia = "1000"
        tiempo = "1:00:00"
        val despues = compose.onNodeWithText("1000").getUnclippedBoundsInRoot()
        assertEquals((antes.bottom - antes.top).value, (despues.bottom - despues.top).value, 0.5f)
        assertEquals(antes.top.value, despues.top.value, 0.5f)
        assertEquals(tiempoAntes.top.value, compose.onNodeWithText("1:00:00").getUnclippedBoundsInRoot().top.value, 0.5f)
    }

    @Test
    fun rejilla_con_el_ultimo_impar_centrado() {
        // Spec `ui-components`, «Último dato impar»: con 5 datos en 2 columnas, el quinto centrado en todo el ancho.
        val datos = listOf("1", "2", "3", "4", "5").map { Dato("Dato $it", "24$it", "W") }
        compose.setContent { SqTema { MetricGrid(datos, Modifier.size(320.dp, 300.dp)) } }
        val quinto = compose.onNodeWithText("245").getUnclippedBoundsInRoot()
        val etiqueta = compose.onNodeWithText("DATO 5").getUnclippedBoundsInRoot()
        val primero = compose.onNodeWithText("241").getUnclippedBoundsInRoot()
        // La etiqueta, centrada en los 320 dp; la cifra con su unidad, también, y a la derecha de donde iría en la primera columna.
        assertEquals(160f, ((etiqueta.left + etiqueta.right) / 2).value, 1f)
        assertTrue(quinto.left > primero.left + 40.dp)
    }

    @Test
    fun rejilla_con_filas_con_y_sin_unidad_alineadas() {
        // Spec `ui-components`, «Filas con y sin unidad»: la unidad no alarga el valor.
        compose.setContent {
            SqTema {
                MetricGrid(listOf(Dato("Potencia", "247", "W"), Dato("W/kg", "3,3")), Modifier.size(342.dp, 200.dp), vertical = true)
            }
        }
        val conUnidad = compose.onNodeWithText("247").getUnclippedBoundsInRoot()
        val sinUnidad = compose.onNodeWithText("3,3").getUnclippedBoundsInRoot()
        assertEquals(conUnidad.top.value, sinUnidad.top.value, 0.5f)
        assertEquals(conUnidad.bottom.value, sinUnidad.bottom.value, 0.5f)
        assertEquals(
            compose.onNodeWithText("POTENCIA").getUnclippedBoundsInRoot().top.value,
            compose.onNodeWithText("W/KG").getUnclippedBoundsInRoot().top.value,
            0.5f,
        )
        // La unidad, sobre la línea base de la cifra: no baja de su borde inferior.
        assertTrue(compose.onNodeWithText("W").getUnclippedBoundsInRoot().bottom <= conUnidad.bottom + 0.5.dp)
    }

    @Test
    fun aviso_de_accion() {
        var texto by mutableStateOf<String?>("+1 marcha")
        compose.setContent { SqTema { ActionToast(texto) } }
        compose.onNodeWithText("+1 marcha").assertExists()
        texto = "−1 marcha"
        compose.onNodeWithText("−1 marcha").assertExists()
        texto = null
        compose.waitForIdle()
        compose.onNodeWithText("−1 marcha").assertDoesNotExist()
    }

    @Test
    fun chip_tocable_con_objetivo_de_48_dp() {
        var toques = 0
        compose.setContent {
            SqTema { ConnectionChip(EstadoChip.Sin, "Square", Modifier.testTag("chip"), estadoTexto = "sin conexión", alTocar = { toques++ }) }
        }
        compose.onNodeWithText("sin conexión").assertExists()
        compose.onNodeWithTag("chip").assertHeightIsAtLeast(48.dp).performClick()
        assertEquals(1, toques)
    }

    @Test
    fun chip_de_rodaje_sin_palabra_con_descripcion() {
        // Variante de Rodaje: solo el nombre y la forma; la palabra, para los lectores de pantalla.
        var toques = 0
        compose.setContent {
            SqTema { ConnectionChip(EstadoChip.Sin, "Square", Modifier.testTag("chip"), alTocar = { toques++ }, descripcion = "Square, sin conexión") }
        }
        compose.onNodeWithText("sin conexión").assertDoesNotExist()
        compose.onNodeWithText("Square").assertExists()
        compose.onNodeWithContentDescription("Square, sin conexión").assertExists()
        compose.onNodeWithTag("chip").assertHeightIsAtLeast(48.dp).performClick()
        assertEquals(1, toques)
    }

    private fun enlacesBarra(neo: EstadoChip, square: EstadoChip, zwift: EstadoChip) = listOf(
        EnlaceBarra(Enlace.Neo, neo, "Neo, ${palabra(neo)}"),
        EnlaceBarra(Enlace.Square, square, "Controlador, ${palabra(square)}"),
        EnlaceBarra(Enlace.Zwift, zwift, "Zwift, ${palabra(zwift)}"),
    )

    private fun palabra(e: EstadoChip) = when (e) {
        EstadoChip.Ok -> "conectado"
        EstadoChip.Esperando -> "esperando"
        EstadoChip.Baja -> "batería baja"
        else -> "sin conexión"
    }

    @Test
    fun barra_de_conexiones_con_el_mismo_ancho_en_los_tres_estados() {
        var estado by mutableStateOf(EstadoChip.Ok)
        compose.setContent { SqTema { ConnectionBar(enlacesBarra(estado, EstadoChip.Ok, EstadoChip.Ok), {}, Modifier.testTag("barra")) } }
        val ancho = compose.onNodeWithTag("barra").getUnclippedBoundsInRoot().let { it.right - it.left }
        for (e in listOf(EstadoChip.Esperando, EstadoChip.Sin, EstadoChip.Baja, EstadoChip.Ok)) {
            estado = e
            assertEquals("$e", ancho, compose.onNodeWithTag("barra").getUnclippedBoundsInRoot().let { it.right - it.left })
        }
    }

    @Test
    fun barra_de_conexiones_con_el_square_con_bateria_baja() {
        // Spec `ride-screen`: la descripción accesible lo dice y la barra no cambia de ancho.
        var baja by mutableStateOf(false)
        compose.setContent {
            SqTema { ConnectionBar(enlacesBarra(EstadoChip.Ok, if (baja) EstadoChip.Baja else EstadoChip.Ok, EstadoChip.Ok), {}, Modifier.testTag("barra")) }
        }
        val ancho = compose.onNodeWithTag("barra").getUnclippedBoundsInRoot().let { it.right - it.left }
        baja = true
        compose.onNodeWithTag("barra")
            .assertContentDescriptionEquals("Conexiones: Neo, conectado; Controlador, batería baja; Zwift, conectado")
        assertEquals(ancho, compose.onNodeWithTag("barra").getUnclippedBoundsInRoot().let { it.right - it.left })
    }

    @Test
    fun barra_de_conexiones_con_los_iconos_agrupados_y_estados_mezclados() {
        // Spec `ui-components`, «Iconos agrupados» y «Estados mezclados sin tocarse»: 14 dp por icono, 4 dp entre ellos y 8 dp a
        // cada lado; un enlace esperando junto a uno sin conexión no cambia el ancho ni la descripción.
        compose.setContent {
            SqTema { ConnectionBar(enlacesBarra(EstadoChip.Esperando, EstadoChip.Sin, EstadoChip.Esperando), {}, Modifier.testTag("barra")) }
        }
        val b = compose.onNodeWithTag("barra").getUnclippedBoundsInRoot()
        assertEquals(2 * 8f + 3 * 14f + 2 * 4f, (b.right - b.left).value, 0.5f)
        compose.onNodeWithTag("barra")
            .assertContentDescriptionEquals("Conexiones: Neo, esperando; Controlador, sin conexión; Zwift, esperando")
    }

    @Test
    fun barra_de_conexiones_con_descripcion_unica_y_un_solo_objetivo() {
        var toques = 0
        compose.setContent {
            SqTema { ConnectionBar(enlacesBarra(EstadoChip.Ok, EstadoChip.Sin, EstadoChip.Esperando), { toques++ }, Modifier.testTag("barra")) }
        }
        compose.onNodeWithTag("barra")
            .assertContentDescriptionEquals("Conexiones: Neo, conectado; Controlador, sin conexión; Zwift, esperando")
            .assertHeightIsAtLeast(48.dp)
        // Un icono, el hueco entre dos iconos (4 dp) y el margen del extremo: todo es la misma barra.
        val b = compose.onNodeWithTag("barra").getUnclippedBoundsInRoot()
        compose.onNodeWithTag("barra").performTouchInput { click(Offset(8.dp.toPx() + 14.dp.toPx() / 2, centerY)) }
        compose.onNodeWithTag("barra").performTouchInput { click(Offset(8.dp.toPx() + 14.dp.toPx() + 2.dp.toPx(), centerY)) }
        compose.onNodeWithTag("barra").performClick()
        assertEquals(3, toques)
        assertTrue(b.right - b.left >= 14.dp * 3 + 4.dp * 2)
    }

    @Test
    fun barra_inferior_con_la_pestana_actual() {
        // `BottomNav`: cuatro pestañas con icono y texto, la actual elegida (no solo por el color) y objetivo de 48 dp.
        val elegidas = mutableListOf<Pestana>()
        compose.setContent { SqTema { BottomNav(Pestana.Ajustes, { elegidas += it }) } }
        listOf("Rodar", "Conexión", "Dispositivos", "Ajustes").forEach { compose.onNodeWithText(it).assertExists() }
        compose.onNodeWithText("Ajustes").assertIsSelected()
        compose.onNodeWithText("Conexión").assertIsNotSelected().assertHeightIsAtLeast(48.dp).performClick()
        // Tocar la actual no hace nada.
        compose.onNodeWithText("Ajustes").performClick()
        assertEquals(listOf(Pestana.Conexion), elegidas)
    }
}
