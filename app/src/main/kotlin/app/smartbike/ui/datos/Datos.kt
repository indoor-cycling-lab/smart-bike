package app.smartbike.ui.datos

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import app.smartbike.R
import app.smartbike.dominio.DatoRodaje
import app.smartbike.dominio.EstadoMarcha
import app.smartbike.ui.componentes.AppBar
import app.smartbike.ui.componentes.Caption
import app.smartbike.ui.componentes.DESHABILITADO
import app.smartbike.ui.componentes.Label
import app.smartbike.ui.componentes.Notice
import app.smartbike.ui.rodaje.DATOS_CON_BOTONES
import app.smartbike.ui.tema.SqTema
import kotlin.math.roundToInt

/** Acciones de Datos en pantalla. */
class AccionesDatos(
    val volver: () -> Unit = {},
    val alternar: (DatoRodaje) -> Unit = {},
    val mover: (DatoRodaje, Int) -> Unit = { _, _ -> },
)

@Composable
fun DatosRuta(volver: () -> Unit, vm: DatosViewModel = viewModel(factory = DatosViewModel.Factory)) {
    val estado by vm.estado.collectAsStateWithLifecycle()
    Datos(estado, AccionesDatos(volver, vm::alternar, vm::mover))
}

/**
 * Datos en pantalla (maquetas/datos.html): vista previa, contador y los seis datos con casilla y asa para ordenarlos
 * (design.md del change `rodaje-completo-y-ajustes`, decisión 9). Secundaria de Ajustes.
 */
@Composable
fun Datos(estado: DatosUiState, acciones: AccionesDatos, modifier: Modifier = Modifier) {
    Column(
        modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(start = SqTema.espacios.space4, end = SqTema.espacios.space4, top = SqTema.espacios.space4, bottom = SqTema.espacios.space8),
        verticalArrangement = Arrangement.spacedBy(SqTema.espacios.space4),
    ) {
        AppBar(stringResource(R.string.datos_en_pantalla), volver = stringResource(R.string.volver_ajustes) to acciones.volver)
        Caption(stringResource(R.string.datos_intro))
        // Pantalla baja con botones: Rodaje oculta los datos a partir del quinto (spec `settings-screen`, «Aviso de datos ocultos en pantalla baja»).
        val ocultos = estado.ocultos(LocalConfiguration.current.screenHeightDp.dp)
        if (ocultos.isNotEmpty()) {
            val nombres = ocultos.map { stringResource(nombreDato(it)) }
            Notice(
                if (nombres.size == 1) {
                    stringResource(R.string.datos_ocultos_uno, DATOS_CON_BOTONES, nombres[0])
                } else {
                    stringResource(R.string.datos_ocultos_dos, DATOS_CON_BOTONES, nombres[0], nombres[1])
                },
            )
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Label(stringResource(R.string.vista_previa))
            Caption(stringResource(R.string.datos_contador, estado.elegidos.size, MAX))
        }
        VistaPrevia(estado)
        ListaDatos(estado, acciones)
        Caption(stringResource(R.string.datos_nota_erg))
    }
}

private const val MAX = 6

/** Valores de ejemplo de la vista previa, como en la maqueta. */
private fun ejemplo(d: DatoRodaje): Pair<String, String?> = when (d) {
    DatoRodaje.POTENCIA -> "247" to "W"
    DatoRodaje.CADENCIA -> "91" to "rpm"
    DatoRodaje.TIEMPO -> "1:12:40" to null
    DatoRodaje.W_KG -> "3,3" to null
    DatoRodaje.POTENCIA_MEDIA -> "231" to "W"
    DatoRodaje.POTENCIA_MAXIMA -> "612" to "W"
    DatoRodaje.VELOCIDAD -> "24,0" to "km/h"
    DatoRodaje.DISTANCIA -> "18,40" to "km"
    DatoRodaje.PENDIENTE -> "+4,2" to "%"
}

/**
 * Rodaje a escala (`sq-ride-mini`): la marcha de referencia a la izquierda y los datos elegidos a la derecha. Mide al
 * menos 132 dp y crece con la letra del sistema, para que ninguna etiqueta ni cifra se corte.
 */
@Composable
private fun VistaPrevia(estado: DatosUiState) {
    val c = SqTema.colores
    val forma = RoundedCornerShape(SqTema.radios.radiusMd)
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 132.dp)
            .height(IntrinsicSize.Min)
            .clip(forma)
            .border(1.dp, c.line, forma)
            .background(c.surface)
            .padding(SqTema.espacios.space3),
        horizontalArrangement = Arrangement.spacedBy(SqTema.espacios.space3),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.weight(1.2f), contentAlignment = Alignment.CenterStart) {
            Text(
                when (val m = estado.marcha) {
                    is EstadoMarcha.Numerada -> m.posicion.toString()
                    is EstadoMarcha.PlatoPinon -> m.toString()
                },
                style = SqTema.tipografia.metricXl.copy(fontSize = 44.sp, lineHeight = 44.sp),
                color = c.gear,
                maxLines = 1,
            )
        }
        val columnas = if (estado.elegidos.size == 1) 1 else 2
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(SqTema.espacios.space1)) {
            estado.elegidos.chunked(columnas).forEach { fila ->
                Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(SqTema.espacios.space1)) {
                    fila.forEach { d ->
                        val (valor, unidad) = ejemplo(d)
                        Column(
                            Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(c.bg, RoundedCornerShape(SqTema.radios.radiusSm))
                                .padding(horizontal = SqTema.espacios.space2, vertical = SqTema.espacios.space1),
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Text(stringResource(nombreDato(d)).uppercase(), style = SqTema.tipografia.label.copy(fontSize = 9.sp, lineHeight = 11.sp), color = c.inkMuted, maxLines = 2)
                            Text(
                                listOfNotNull(valor, unidad).joinToString(" "),
                                style = SqTema.tipografia.body.copy(fontSize = 13.sp, lineHeight = 16.sp, fontWeight = FontWeight(700)),
                                maxLines = 1,
                            )
                        }
                    }
                    repeat(columnas - fila.size) { Box(Modifier.weight(1f)) }
                }
            }
        }
    }
}

/**
 * La lista con casilla y asa (variante de `SettingsList` documentada en su README). Los elegidos se ordenan
 * arrastrando el asa o con «Subir» y «Bajar» del lector de pantalla; los demás no tienen asa: al marcarlos van al final.
 */
@Composable
private fun ListaDatos(estado: DatosUiState, acciones: AccionesDatos) {
    val c = SqTema.colores
    val forma = RoundedCornerShape(SqTema.radios.radiusMd)
    var arrastrado by remember { mutableStateOf<DatoRodaje?>(null) }
    var desplazamiento by remember { mutableFloatStateOf(0f) }
    var altoFila by remember { mutableIntStateOf(1) }
    val subir = stringResource(R.string.subir)
    val bajar = stringResource(R.string.bajar)
    Column(Modifier.fillMaxWidth().clip(forma).border(1.dp, c.line, forma)) {
        estado.filas.forEachIndexed { i, d ->
            if (i > 0) HorizontalDivider(thickness = 1.dp, color = c.line)
            val elegido = d in estado.elegidos
            val sinSitio = estado.sinSitio(d)
            val bloqueado = estado.bloqueado(d) || sinSitio
            val nombre = stringResource(nombreDato(d))
            val esteArrastrado = arrastrado == d
            Row(
                Modifier
                    .fillMaxWidth()
                    .zIndex(if (esteArrastrado) 1f else 0f)
                    .graphicsLayer { translationY = if (esteArrastrado) desplazamiento else 0f }
                    .onSizeChanged { altoFila = it.height.coerceAtLeast(1) }
                    .background(c.surface)
                    .heightIn(min = SqTema.tamanos.touchMin),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    Modifier
                        .weight(1f)
                        .toggleable(elegido, enabled = !bloqueado, role = Role.Checkbox, onValueChange = { acciones.alternar(d) })
                        .padding(start = SqTema.espacios.space4, top = SqTema.espacios.space3, bottom = SqTema.espacios.space3),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(SqTema.espacios.space3),
                ) {
                    Checkbox(
                        checked = elegido,
                        onCheckedChange = null,
                        enabled = !bloqueado,
                        modifier = Modifier.alpha(if (bloqueado) DESHABILITADO else 1f),
                        colors = CheckboxDefaults.colors(
                            checkedColor = c.gearFill,
                            checkmarkColor = c.onGearFill,
                            uncheckedColor = c.lineStrong,
                            disabledCheckedColor = c.gearFill,
                        ),
                    )
                    Column(Modifier.weight(1f)) {
                        Text(nombre, style = SqTema.tipografia.body.copy(fontWeight = FontWeight(600)), color = if (elegido) c.ink else c.inkMuted)
                        Caption(
                            stringResource(
                                when {
                                    sinSitio -> R.string.datos_sin_sitio
                                    bloqueado -> R.string.datos_al_menos_uno
                                    else -> ayudaDato(d)
                                },
                            ),
                        )
                    }
                }
                if (elegido) {
                    val pos = estado.elegidos.indexOf(d)
                    val descripcion = stringResource(R.string.reordenar, nombre)
                    Box(
                        Modifier
                            .size(SqTema.tamanos.touchMin)
                            .semantics {
                                contentDescription = descripcion
                                customActions = buildList {
                                    if (pos > 0) {
                                        add(
                                            CustomAccessibilityAction(subir) {
                                                acciones.mover(d, -1)
                                                true
                                            },
                                        )
                                    }
                                    if (pos < estado.elegidos.lastIndex) {
                                        add(
                                            CustomAccessibilityAction(bajar) {
                                                acciones.mover(d, 1)
                                                true
                                            },
                                        )
                                    }
                                }
                            }
                            .pointerInput(d) {
                                detectVerticalDragGestures(
                                    onDragStart = {
                                        arrastrado = d
                                        desplazamiento = 0f
                                    },
                                    onDragEnd = {
                                        val pasos = (desplazamiento / altoFila).roundToInt()
                                        if (pasos != 0) acciones.mover(d, pasos)
                                        arrastrado = null
                                        desplazamiento = 0f
                                    },
                                    onDragCancel = {
                                        arrastrado = null
                                        desplazamiento = 0f
                                    },
                                    onVerticalDrag = { cambio, dy ->
                                        cambio.consume()
                                        desplazamiento += dy
                                    },
                                )
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(painterResource(R.drawable.ic_drag_indicator), contentDescription = null, tint = c.inkMuted)
                    }
                }
            }
        }
    }
}
