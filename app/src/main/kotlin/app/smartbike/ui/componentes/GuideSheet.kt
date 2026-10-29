package app.smartbike.ui.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.smartbike.R
import app.smartbike.dominio.GuiaPantalla
import app.smartbike.dominio.ModoConexion
import app.smartbike.dominio.PasoApp
import app.smartbike.dominio.PasoDispositivos
import app.smartbike.dominio.SiguientePaso
import app.smartbike.ui.rodaje.GuiaUi
import app.smartbike.ui.tema.SqTema

/** Lo que hacen la hoja y la tira de la guía de inicio, y adónde llevan sus accesos. */
class AccionesGuia(
    val minimizar: () -> Unit = {},
    val abrir: () -> Unit = {},
    val cerrar: () -> Unit = {},
    val noMostrar: () -> Unit = {},
    val irA: (Int) -> Unit = {},
    val abrirDispositivos: () -> Unit = {},
    val abrirConexion: () -> Unit = {},
    val abrirAjustes: () -> Unit = {},
)

/** El relleno horizontal de los botones del pie: más estrecho que el de los demás para que «No volver a mostrar» y los dos botones quepan en una línea. */
private val RELLENO_BOTON = 16.dp

/** Lo que hay que deslizar, en dp, para que cambie de página. */
private val UMBRAL_DESLIZAMIENTO = 64.dp

/**
 * Hoja inferior de la guía de inicio (`sq-guide--sheet`): tres páginas (qué es, para empezar y conectar con la app de
 * ciclismo indoor) con «Siguiente» y «Atrás», también deslizando, el indicador de página, y minimizar y cerrar en la
 * cabecera. Flota sobre Rodaje, anclada al pie, sin bloquear. Con [ancha] (horizontal), los tres elementos de cada página
 * van en columnas. El contenido se desplaza solo si la letra del sistema es tan grande que no cabe.
 */
@Composable
fun GuideSheet(guia: GuiaUi, acciones: AccionesGuia, ancha: Boolean, modifier: Modifier = Modifier) {
    val c = SqTema.colores
    val forma = RoundedCornerShape(topStart = SqTema.radios.radiusMd, topEnd = SqTema.radios.radiusMd)
    var arrastre by remember { mutableFloatStateOf(0f) }
    val umbral = with(LocalDensity.current) { UMBRAL_DESLIZAMIENTO.toPx() }
    val descripcion = stringResource(R.string.guia_descripcion, guia.pagina, GuiaPantalla.ULTIMA)
    Column(
        modifier
            .fillMaxWidth()
            .clip(forma)
            .background(c.surface)
            .border(1.dp, c.lineStrong, forma)
            .semantics { paneTitle = descripcion }
            .pointerInput(guia.pagina) {
                detectHorizontalDragGestures(
                    onDragStart = { arrastre = 0f },
                    onDragEnd = {
                        if (arrastre <= -umbral) {
                            acciones.irA(guia.pagina + 1)
                        } else if (arrastre >= umbral) {
                            acciones.irA(guia.pagina - 1)
                        }
                    },
                ) { _, delta -> arrastre += delta }
            },
    ) {
        Box(
            Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = SqTema.espacios.space2)
                .size(width = 32.dp, height = 4.dp)
                .background(c.lineStrong, CircleShape),
        )
        Cabecera(guia.pagina, acciones)
        Column(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState())) {
            when (guia.pagina) {
                1 -> PaginaQueEs(ancha)
                2 -> PaginaPasos(guia, acciones, ancha)
                3 -> PaginaModos(ancha)
                else -> PaginaConectar(guia, ancha)
            }
        }
        Pie(guia, acciones)
    }
}

@Composable
private fun Cabecera(pagina: Int, acciones: AccionesGuia) {
    val c = SqTema.colores
    Row(Modifier.fillMaxWidth().heightIn(min = SqTema.tamanos.touchMin).padding(start = SqTema.espacios.space4), verticalAlignment = Alignment.CenterVertically) {
        Row(
            Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SqTema.espacios.space2),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(SqTema.espacios.space1), verticalAlignment = Alignment.CenterVertically) {
                (GuiaPantalla.PRIMERA..GuiaPantalla.ULTIMA).forEach {
                    Box(Modifier.size(width = if (it == pagina) 20.dp else 8.dp, height = 8.dp).background(if (it == pagina) c.gear else c.inkMuted, CircleShape))
                }
            }
            Text(stringResource(R.string.guia_pagina, pagina, GuiaPantalla.ULTIMA), color = c.inkMuted, style = SqTema.tipografia.body.copy(fontSize = 13.sp, lineHeight = 18.sp))
        }
        BotonIcono(R.drawable.ic_expand_more, stringResource(R.string.guia_minimizar), acciones.minimizar)
        BotonIcono(R.drawable.ic_close, stringResource(R.string.guia_cerrar), acciones.cerrar)
    }
}

@Composable
internal fun BotonIcono(icono: Int, descripcion: String, alTocar: () -> Unit, modifier: Modifier = Modifier, giro: Float = 0f) {
    Box(
        modifier.size(SqTema.tamanos.touchMin).clickable(role = Role.Button, onClickLabel = descripcion, onClick = alTocar).semantics { contentDescription = descripcion },
        contentAlignment = Alignment.Center,
    ) {
        Icon(painterResource(icono), contentDescription = null, tint = SqTema.colores.inkMuted, modifier = Modifier.rotate(giro))
    }
}

@Composable
private fun Titulo(texto: String, ancha: Boolean, opcional: Boolean = false) {
    // «Opcional» va tras el título y, si no cabe, pasa a la línea de abajo.
    FlowRow(
        Modifier.padding(start = SqTema.espacios.space4, end = SqTema.espacios.space4, bottom = SqTema.espacios.space2).semantics(mergeDescendants = true) { heading() },
        horizontalArrangement = Arrangement.spacedBy(SqTema.espacios.space2),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            texto,
            color = SqTema.colores.ink,
            style = SqTema.tipografia.body.copy(fontSize = if (ancha) 17.sp else 20.sp, lineHeight = if (ancha) 22.sp else 26.sp, fontWeight = FontWeight(600)),
            modifier = Modifier.align(Alignment.CenterVertically),
        )
        if (opcional) Etiqueta(stringResource(R.string.guia_opcional), Modifier.align(Alignment.CenterVertically))
    }
}

@Composable
private fun Parrafo(texto: String) {
    Text(
        texto,
        color = SqTema.colores.inkMuted,
        style = SqTema.tipografia.body.copy(fontSize = 14.sp, lineHeight = 20.sp),
        modifier = Modifier.padding(horizontal = SqTema.espacios.space4, vertical = SqTema.espacios.space1),
    )
}

/** Los elementos de una página: en vertical, uno bajo otro con una línea encima; en horizontal, en columnas con una línea entre ellas. */
@Composable
private fun Elementos(ancha: Boolean, elementos: List<@Composable () -> Unit>) {
    val linea = SqTema.colores.line
    if (ancha) {
        // La altura de la fila es la de su columna más alta; las líneas entre columnas se dibujan a un tercio y a dos tercios, sin medidas intrínsecas.
        Row(
            Modifier.fillMaxWidth().lineaSuperior(linea).drawBehind {
                val trazo = 1.dp.toPx()
                for (i in 1 until elementos.size) {
                    val x = size.width * i / elementos.size
                    drawLine(linea, androidx.compose.ui.geometry.Offset(x, 0f), androidx.compose.ui.geometry.Offset(x, size.height), strokeWidth = trazo)
                }
            },
        ) {
            elementos.forEach { e -> Box(Modifier.weight(1f)) { e() } }
        }
    } else {
        Column(Modifier.fillMaxWidth()) {
            elementos.forEach { e -> Box(Modifier.fillMaxWidth().lineaSuperior(linea)) { e() } }
        }
    }
}

private fun Modifier.lineaSuperior(color: Color): Modifier = drawBehind { drawLine(color, androidx.compose.ui.geometry.Offset(0f, 0f), androidx.compose.ui.geometry.Offset(size.width, 0f), strokeWidth = 1.dp.toPx()) }

private fun Modifier.lineaIzquierda(color: Color): Modifier = drawBehind { drawLine(color, androidx.compose.ui.geometry.Offset(0f, 0f), androidx.compose.ui.geometry.Offset(0f, size.height), strokeWidth = 1.dp.toPx()) }

@Composable
private fun Pieza(nombre: String, ejemplo: String, texto: String, opcional: Boolean) {
    val c = SqTema.colores
    Column(Modifier.fillMaxWidth().padding(horizontal = SqTema.espacios.space4, vertical = SqTema.espacios.space2).semantics(mergeDescendants = true) {}) {
        // El nombre, el ejemplo y «Opcional» van seguidos y, si no caben (horizontal), pasan a la línea de abajo.
        FlowRow(horizontalArrangement = Arrangement.spacedBy(SqTema.espacios.space1), verticalArrangement = Arrangement.Center) {
            Text(nombre, color = c.ink, style = SqTema.tipografia.body.copy(fontSize = 15.sp, lineHeight = 20.sp, fontWeight = FontWeight(600)), modifier = Modifier.align(Alignment.CenterVertically))
            Text(stringResource(R.string.guia_ejemplo, ejemplo), color = c.inkMuted, style = SqTema.tipografia.body.copy(fontSize = 15.sp, lineHeight = 20.sp), modifier = Modifier.align(Alignment.CenterVertically))
            if (opcional) Etiqueta(stringResource(R.string.guia_opcional), Modifier.align(Alignment.CenterVertically))
        }
        Text(texto, color = c.inkMuted, style = SqTema.tipografia.body.copy(fontSize = 13.sp, lineHeight = 18.sp))
    }
}

/** Página 1: qué es la app y qué piezas intervienen, en genérico y con un ejemplo cada una. */
@Composable
private fun PaginaQueEs(ancha: Boolean) {
    Titulo(stringResource(R.string.guia_p1_titulo), ancha)
    Parrafo(stringResource(R.string.guia_p1_intro))
    Elementos(
        ancha,
        listOf(
            { Pieza(stringResource(R.string.guia_rodillo), stringResource(R.string.guia_ejemplo_rodillo), stringResource(R.string.guia_p1_rodillo), opcional = false) },
            { Pieza(stringResource(R.string.guia_controlador), stringResource(R.string.guia_ejemplo_controlador), stringResource(R.string.guia_p1_controlador), opcional = true) },
            { Pieza(stringResource(R.string.guia_app), stringResource(R.string.guia_ejemplo_app), stringResource(R.string.guia_p1_app), opcional = true) },
        ),
    )
    Parrafo(stringResource(R.string.guia_p1_cierre))
}

/** Página 2: los tres pasos en orden natural, con el estado real de los dos primeros. */
@Composable
private fun PaginaPasos(guia: GuiaUi, acciones: AccionesGuia, ancha: Boolean) {
    val dispositivos = guia.pasos.dispositivos
    val rodillo = guia.rodillo ?: stringResource(R.string.guia_ejemplo_rodillo)
    val controlador = guia.controlador ?: stringResource(R.string.guia_ejemplo_controlador)
    val (estadoD, palabraD, vozD, ayudaD) = when (dispositivos) {
        PasoDispositivos.PENDIENTE -> Cuatro(EstadoPaso.Pendiente, R.string.guia_estado_pendiente, R.string.guia_voz_pendiente, stringResource(R.string.guia_ayuda_dispositivos_pendiente))
        PasoDispositivos.CONECTANDO -> Cuatro(EstadoPaso.Conectando, R.string.guia_estado_conectando, R.string.guia_voz_conectando, stringResource(R.string.guia_ayuda_dispositivos_conectando, rodillo))
        PasoDispositivos.FALTA_CONTROLADOR -> Cuatro(EstadoPaso.Pendiente, R.string.guia_estado_falta_controlador, R.string.guia_voz_falta_controlador, stringResource(R.string.guia_ayuda_dispositivos_falta, rodillo))
        PasoDispositivos.HECHO -> Cuatro(EstadoPaso.Hecho, R.string.guia_estado_hecho, R.string.guia_voz_hecho, stringResource(R.string.guia_ayuda_dispositivos_hecho, rodillo, controlador))
    }
    val modo = stringResource(nombreModo(guia.modo))
    val nombreD = stringResource(R.string.guia_paso_dispositivos)
    val nombreC = stringResource(R.string.guia_paso_conexion)
    val ayudaC = stringResource(R.string.guia_ayuda_conexion, guia.nombreApp, modo)
    val nombreAjustes = stringResource(R.string.guia_paso_ajustes)
    val ayudaAjustes = stringResource(R.string.guia_ayuda_ajustes)
    Titulo(stringResource(R.string.guia_titulo), ancha)
    Elementos(
        ancha,
        listOf(
            {
                GuideStep(
                    nombreD,
                    estadoD,
                    stringResource(R.string.guia_voz_paso, nombreD, stringResource(vozD)),
                    acciones.abrirDispositivos,
                    ayuda = ayudaD,
                    estadoTexto = stringResource(palabraD),
                    ancha = ancha,
                )
            },
            {
                GuideStep(
                    nombreC,
                    EstadoPaso.Informativo,
                    stringResource(R.string.guia_voz_conexion, nombreC, modo),
                    acciones.abrirConexion,
                    ayuda = ayudaC,
                    opcionalTexto = stringResource(R.string.guia_opcional),
                    ancha = ancha,
                )
            },
            { GuideStep(nombreAjustes, EstadoPaso.Informativo, "$nombreAjustes. $ayudaAjustes", acciones.abrirAjustes, ayuda = ayudaAjustes, ancha = ancha) },
        ),
    )
}

/** El nombre del modo de conexión, el mismo de Conexión. */
private fun nombreModo(modo: ModoConexion): Int = when (modo) {
    ModoConexion.FTMS_WIFI -> R.string.modo_wifi_bluetooth
    ModoConexion.FTMS_BLUETOOTH -> R.string.modo_solo_bluetooth
    ModoConexion.ZWIFT_RIDE -> R.string.modo_solo_wifi
}

private data class Cuatro(val estado: EstadoPaso, val palabra: Int, val voz: Int, val ayuda: String)

/** Página 3: las formas de rodar, con los nombres del indicador de modo de Rodaje. Solo lectura. */
@Composable
private fun PaginaModos(ancha: Boolean) {
    Titulo(stringResource(R.string.guia_p3_titulo), ancha)
    Parrafo(stringResource(R.string.guia_p3_intro))
    Elementos(
        ancha,
        listOf(
            { Modo(libre = true, erg = false, nombre = stringResource(R.string.modo_libre), texto = stringResource(R.string.guia_p3_libre)) },
            { Modo(libre = false, erg = false, nombre = stringResource(R.string.modo_sim), texto = stringResource(R.string.guia_p3_sim)) },
            { Modo(libre = false, erg = true, nombre = stringResource(R.string.modo_erg), texto = stringResource(R.string.guia_p3_erg)) },
        ),
    )
    Parrafo(stringResource(R.string.guia_p3_cierre))
}

@Composable
private fun Modo(libre: Boolean, erg: Boolean, nombre: String, texto: String) {
    Column(Modifier.fillMaxWidth().padding(horizontal = SqTema.espacios.space4, vertical = SqTema.espacios.space2).semantics(mergeDescendants = true) {}) {
        ModeBadge(nombre, libre = libre, erg = erg)
        Text(texto, color = SqTema.colores.inkMuted, style = SqTema.tipografia.body.copy(fontSize = 13.sp, lineHeight = 18.sp), modifier = Modifier.padding(top = SqTema.espacios.space2))
    }
}

/** Página 4 (opcional): los pasos para emparejar la app de ciclismo indoor, según el modo de conexión elegido en Conexión. */
@Composable
private fun PaginaConectar(guia: GuiaUi, ancha: Boolean) {
    val movil = guia.nombreMovil ?: stringResource(R.string.este_movil)
    val pasos = when (guia.modo) {
        ModoConexion.FTMS_WIFI -> listOf(stringResource(R.string.guia_p4_wifi_1), stringResource(R.string.guia_p4_wifi_2, guia.nombreApp), stringResource(R.string.guia_p4_wifi_3))
        ModoConexion.FTMS_BLUETOOTH -> listOf(stringResource(R.string.guia_p4_bt_1), stringResource(R.string.guia_p4_bt_2, movil), stringResource(R.string.guia_p4_bt_3))
        ModoConexion.ZWIFT_RIDE -> listOf(stringResource(R.string.guia_p4_zr_1), stringResource(R.string.guia_p4_zr_2, guia.nombreApp), stringResource(R.string.guia_p4_zr_3))
    }
    Titulo(stringResource(R.string.guia_p4_titulo, guia.nombreApp), ancha, opcional = true)
    Parrafo(stringResource(R.string.guia_p4_modo, stringResource(nombreModo(guia.modo))))
    EstadoConexion(guia.pasos.app)
    if (ancha) {
        Row(Modifier.fillMaxWidth().padding(horizontal = SqTema.espacios.space4, vertical = SqTema.espacios.space2), horizontalArrangement = Arrangement.spacedBy(SqTema.espacios.space4)) {
            pasos.forEachIndexed { i, t -> PasoNumerado(i + 1, t, Modifier.weight(1f)) }
        }
    } else {
        Column(Modifier.fillMaxWidth().padding(horizontal = SqTema.espacios.space4, vertical = SqTema.espacios.space2), verticalArrangement = Arrangement.spacedBy(SqTema.espacios.space3)) {
            pasos.forEachIndexed { i, t -> PasoNumerado(i + 1, t, Modifier.fillMaxWidth()) }
        }
    }
}

/** El estado de la conexión con la app de ciclismo indoor (`sq-gstate`): pendiente sin rodillo, esperando a la app, o conectada. */
@Composable
private fun EstadoConexion(paso: PasoApp) {
    val (chip, texto) = when (paso) {
        PasoApp.PENDIENTE -> EstadoChip.Neutro to R.string.guia_p4_estado_pendiente
        PasoApp.ESPERANDO -> EstadoChip.Esperando to R.string.guia_p4_estado_esperando
        PasoApp.HECHO -> EstadoChip.Ok to R.string.guia_p4_estado_hecho
    }
    Row(
        Modifier.padding(horizontal = SqTema.espacios.space4, vertical = SqTema.espacios.space1).semantics(mergeDescendants = true) {},
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SqTema.espacios.space3),
    ) {
        Punto(chip)
        Text(stringResource(texto), color = SqTema.colores.ink, style = SqTema.tipografia.body.copy(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight(600)))
    }
}

@Composable
private fun PasoNumerado(n: Int, texto: String, modifier: Modifier) {
    val c = SqTema.colores
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(SqTema.espacios.space3)) {
        Box(Modifier.size(24.dp).border(1.dp, c.lineStrong, CircleShape).clearAndSetSemantics {}, contentAlignment = Alignment.Center) {
            Text(n.toString(), color = c.ink, style = SqTema.tipografia.body.copy(fontSize = 14.sp, lineHeight = 14.sp, fontWeight = FontWeight(700)))
        }
        Text(texto, color = c.ink, style = SqTema.tipografia.body.copy(fontSize = 14.sp, lineHeight = 20.sp), modifier = Modifier.padding(top = 2.dp))
    }
}

/** El pie de la hoja: «No volver a mostrar» y la navegación («Atrás» y «Siguiente»; en la última página, solo «Atrás»). */
@Composable
private fun Pie(guia: GuiaUi, acciones: AccionesGuia) {
    val c = SqTema.colores
    FlowRow(
        Modifier.fillMaxWidth().lineaSuperior(c.line).padding(start = SqTema.espacios.space4, end = SqTema.espacios.space4, top = SqTema.espacios.space2, bottom = SqTema.espacios.space3),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalArrangement = Arrangement.spacedBy(SqTema.espacios.space1),
    ) {
        Box(
            Modifier.heightIn(min = SqTema.tamanos.touchMin).clickable(role = Role.Button, onClick = acciones.noMostrar).padding(end = SqTema.espacios.space2),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(stringResource(R.string.guia_no_mostrar), color = c.gear, style = SqTema.tipografia.body.copy(fontSize = 14.sp, fontWeight = FontWeight(600)))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(SqTema.espacios.space2)) {
            if (guia.pagina > GuiaPantalla.PRIMERA) Button(stringResource(R.string.guia_atras), { acciones.irA(guia.pagina - 1) }, relleno = RELLENO_BOTON)
            if (guia.pagina < GuiaPantalla.ULTIMA) {
                Button(stringResource(R.string.guia_siguiente), { acciones.irA(guia.pagina + 1) }, principal = true, relleno = RELLENO_BOTON)
            }
        }
    }
}

/**
 * La tira minimizada (`sq-guide is-compact`): ocupa el sitio del pie con «Para empezar» y, debajo, lo siguiente por hacer. Al
 * tocarla se abre la hoja en la página en la que estaba. Con [oculta] sigue ocupando su sitio, sin verse ni leerse, mientras
 * la hoja está abierta.
 */
@Composable
fun GuideStrip(siguiente: SiguientePaso?, acciones: AccionesGuia, modifier: Modifier = Modifier, oculta: Boolean = false) {
    // Dos ramas, no un modificador condicional: quitar `alpha(0f)` de una tira ya compuesta dejaba sin dibujar su fondo y su borde
    // al minimizar la hoja (el texto sí se veía) hasta que algo la recomponía, p. ej. girar el móvil.
    if (oculta) {
        Box(modifier.alpha(0f).clearAndSetSemantics {}) { Tira(siguiente, acciones) }
    } else {
        Box(modifier) { Tira(siguiente, acciones) }
    }
}

@Composable
private fun Tira(siguiente: SiguientePaso?, acciones: AccionesGuia) {
    val c = SqTema.colores
    val forma = RoundedCornerShape(SqTema.radios.radiusMd)
    val texto = when (siguiente) {
        SiguientePaso.APP -> stringResource(R.string.guia_paso_app)
        else -> stringResource(R.string.guia_paso_dispositivos)
    }
    val voz = stringResource(R.string.guia_tira_voz, texto)
    val base = Modifier.fillMaxWidth().heightIn(min = SqTema.tamanos.touchMin).clip(forma).background(c.surface).border(1.dp, c.line, forma)
    Row(base, verticalAlignment = Alignment.CenterVertically) {
        Row(
            Modifier.weight(1f)
                .heightIn(min = SqTema.tamanos.touchMin)
                .semantics(mergeDescendants = true) { contentDescription = voz }
                .clickable(role = Role.Button, onClick = acciones.abrir)
                .padding(start = SqTema.espacios.space4, end = SqTema.espacios.space2),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.guia_titulo), color = c.ink, style = SqTema.tipografia.body.copy(fontSize = 15.sp, lineHeight = 20.sp, fontWeight = FontWeight(600)))
                Text(texto, color = c.inkMuted, style = SqTema.tipografia.body.copy(fontSize = 13.sp, lineHeight = 18.sp))
            }
            Icon(painterResource(R.drawable.ic_expand_more), contentDescription = null, tint = c.inkMuted, modifier = Modifier.rotate(180f))
        }
        BotonIcono(R.drawable.ic_close, stringResource(R.string.guia_cerrar), acciones.cerrar)
    }
}
