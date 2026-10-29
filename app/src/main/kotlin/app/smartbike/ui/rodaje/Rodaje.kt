package app.smartbike.ui.rodaje

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.smartbike.R
import app.smartbike.ui.ajustes.nombreModelo
import app.smartbike.ui.componentes.AccionesGuia
import app.smartbike.ui.componentes.ActionToast
import app.smartbike.ui.componentes.ButtonRide
import app.smartbike.ui.componentes.Caption
import app.smartbike.ui.componentes.CassetteView
import app.smartbike.ui.componentes.ChainringIndicator
import app.smartbike.ui.componentes.ConnectionBar
import app.smartbike.ui.componentes.ConnectionChip
import app.smartbike.ui.componentes.Dato
import app.smartbike.ui.componentes.Enlace
import app.smartbike.ui.componentes.EnlaceBarra
import app.smartbike.ui.componentes.ErgTarget
import app.smartbike.ui.componentes.EstadoChip
import app.smartbike.ui.componentes.ExitNotice
import app.smartbike.ui.componentes.GearDisplay
import app.smartbike.ui.componentes.GearLadder
import app.smartbike.ui.componentes.GuideSheet
import app.smartbike.ui.componentes.GuideStrip
import app.smartbike.ui.componentes.MedidaRejilla
import app.smartbike.ui.componentes.MetricGrid
import app.smartbike.ui.componentes.ModeBadge
import app.smartbike.ui.componentes.TurnIndicator
import app.smartbike.ui.componentes.datosParaMedir
import app.smartbike.ui.componentes.medirRejilla
import app.smartbike.ui.conSigno
import app.smartbike.ui.datos.nombreDato
import app.smartbike.ui.datos.plantillaDato
import app.smartbike.ui.tema.SqTema
import app.smartbike.ui.textoAccion
import app.smartbike.ui.textoAvisoVentilador
import app.smartbike.ui.textoReferenciaFijada
import app.smartbike.ui.textoTope
import app.smartbike.ui.textoTransmision
import app.smartbike.ui.textoVatios
import kotlin.math.roundToInt

/**
 * Pantalla de rodaje (`RideScreen`), sin estado propio: en vertical si el alto disponible es mayor que el ancho
 * (maquetas/rodaje-vertical.html) y en horizontal en otro caso (maquetas/rodaje.html). No muestra nada del modelo de
 * resistencia (F11.2). La barra de conexiones de los dispositivos abre Dispositivos ([abrirDispositivos]), el estado de la
 * publicación abre Conexión ([abrirConexion]) y el icono de la esquina superior derecha, Ajustes ([abrirAjustes]); [mas] y [menos] son los botones − y +, si están activados. Los datos tienen un área fija y la marcha ocupa el resto (`RideScreen`).
 */
@Composable
fun Rodaje(
    estado: RodajeUiState,
    abrirDispositivos: () -> Unit,
    modifier: Modifier = Modifier,
    abrirAjustes: () -> Unit = {},
    abrirConexion: () -> Unit = {},
    animar: Boolean = true,
    mas: () -> Unit = {},
    menos: () -> Unit = {},
    alternarModo: () -> Unit = {},
    guia: AccionesGuia = AccionesGuia(),
) {
    val botones = Botones(mas, menos, alternarModo)
    val abrir = Abrir(abrirDispositivos, abrirAjustes, abrirConexion)
    val acciones = AccionesGuia(guia.minimizar, guia.abrir, guia.cerrar, guia.noMostrar, guia.irA, abrirDispositivos, abrirConexion, abrirAjustes)
    BoxWithConstraints(modifier.fillMaxSize()) {
        if (maxHeight > maxWidth) {
            // Como `sq-ride--v` (sin relleno arriba: las zonas táctiles de 48 dp de la primera fila dejan 8 dp sobre el chip de 32): bajo la barra de estado del sistema, que muestra la hora, el wifi y la batería, y dentro del
            // área segura (la cámara, los lados y la barra de navegación).
            BoxWithConstraints(Modifier.fillMaxSize().safeDrawingPadding()) {
                RodajeVertical(estado, abrir, animar, botones, acciones, maxHeight - SqTema.espacios.space4, maxWidth - SqTema.espacios.space6 * 2, maxHeight)
            }
        } else {
            Box(Modifier.fillMaxSize().safeDrawingPadding()) { RodajeHorizontal(estado, abrir, animar, botones, acciones) }
        }
        // La hoja de la guía de inicio abierta: sale desde abajo y flota sobre Rodaje, sin ocupar sitio ni mover nada (design.md de `guia-inicio`, decisión 3).
        estado.guia?.takeIf { it.abierta }?.let { g ->
            GuideSheet(
                g,
                acciones,
                ancha = maxHeight <= maxWidth,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom))
                    .heightIn(max = maxHeight * HOJA_ALTO_MAXIMO),
            )
        }
        // Flotando en el centro, sobre el contenido: no ocupa sitio ni mueve nada (design.md de `confirmar-salida-rodaje`, decisión 4).
        if (estado.salida != SalidaUi.Ninguna) {
            val cerrando = estado.salida == SalidaUi.Cerrando
            ExitNotice(
                texto = stringResource(if (cerrando) R.string.salida_cerrando else R.string.salida_confirmar),
                descripcion = stringResource(if (cerrando) R.string.salida_cerrando_voz else R.string.salida_confirmar),
                modifier = Modifier.align(Alignment.Center).padding(horizontal = SqTema.espacios.space6),
            )
        }
    }
}

/** Lo máximo que ocupa la hoja de la guía: casi todo el alto, para que siempre quede un trozo de Rodaje a la vista. */
private const val HOJA_ALTO_MAXIMO = 0.92f

/** Lo que hacen los botones − y + de la pantalla y el indicador de modo tocable. */
private class Botones(val mas: () -> Unit, val menos: () -> Unit, val modo: () -> Unit = {})

/** Adónde llevan la barra de conexiones, el estado de la plataforma y el icono de Ajustes. */
private class Abrir(val dispositivos: () -> Unit, val ajustes: () -> Unit, val plataformas: () -> Unit)

@Composable
private fun RodajeHorizontal(estado: RodajeUiState, abrir: Abrir, animar: Boolean, botones: Botones, acciones: AccionesGuia) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(start = SqTema.espacios.space6, end = SqTema.espacios.space6, top = 0.dp, bottom = SqTema.espacios.space4),
    ) {
        BarraSuperior(estado, abrir, botones.modo)
        // Entre la línea superior y la zona principal quedan 4 dp: la línea mide 48 dp (zona táctil de las conexiones), como en la maqueta.
        BoxWithConstraints(Modifier.weight(1f).fillMaxWidth().padding(top = SqTema.espacios.space1)) {
            // Columna de datos con el 40 % del ancho útil, menos lo que ceda a los botones (`RideScreen`).
            val datos = datosDeRejilla(estado)
            val area = maxWidth * AREA_DATOS
            val medida = medida(datos, area, maxHeight, vertical = false)
            val hueco = SqTema.tamanos.touchRide + SqTema.espacios.space8
            val ancho = if (estado.botones) area - cesion(area, medida, hueco) else area
            Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(SqTema.espacios.space8)) {
                // Como `sq-ride__gear`: la marcha (o la potencia objetivo) y la escalera, centradas en el resto del
                // ancho; la marcha se reduce si no cabe.
                Box(Modifier.weight(1f).fillMaxHeight()) {
                    Column(
                        Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(SqTema.espacios.space3, Alignment.CenterVertically),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        val erg = estado.erg
                        Encajar(Modifier.weight(1f, fill = false)) {
                            if (erg != null) {
                                Objetivo(erg)
                            } else {
                                // Como `sq-ride__gearrow`: con platos, los platos a la derecha de la marcha.
                                TurnIndicator(estado.giro) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(SqTema.espacios.space6), verticalAlignment = Alignment.Bottom) {
                                        Marcha(estado, animar)
                                        Platos(estado, vertical = false)
                                    }
                                }
                            }
                        }
                        if (erg == null) Escalera(estado, vertical = false)
                    }
                }
                Datos(datos, medida, Modifier.width(ancho).fillMaxHeight(), vertical = false)
                // Como `sq-ride__buttons` en horizontal: una columna a la derecha, + arriba y − abajo.
                if (estado.botones) {
                    Column(Modifier.fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(SqTema.espacios.space3, Alignment.CenterVertically)) {
                        ButtonRide("+", stringResource(R.string.marcha_mas_dura), botones.mas)
                        ButtonRide("−", stringResource(R.string.marcha_mas_ligera), botones.menos)
                    }
                }
            }
        }
        Spacer(Modifier.height(SqTema.espacios.space3))
        Pie(estado, acciones)
    }
}

/**
 * Como `sq-ride--v`: conexiones y modo, marcha centrada, escalera a todo el ancho, datos con el 40 % del alto útil, pie
 * y, si están activados, los botones − y +. La rejilla les cede lo que le sobra sin bajar su cifra; lo que falte lo
 * pierde la marcha, que se reduce para caber (`RideScreen`).
 */
@Composable
private fun RodajeVertical(estado: RodajeUiState, abrir: Abrir, animar: Boolean, botones: Botones, acciones: AccionesGuia, altoUtil: Dp, anchoUtil: Dp, altoRodaje: Dp) {
    // Pantalla baja (`sq-ride--baja`): menos aire entre bloques, el cassette más bajo y, con los botones, solo los primeros datos.
    val baja = esPantallaBaja(altoRodaje)
    val separacion = if (baja) SqTema.espacios.space2 else SqTema.espacios.space4
    val datos = datosDeRejilla(estado, recorteDatos(altoRodaje, estado.botones, estado.datosVisibles).visibles)
    val area = altoUtil * AREA_DATOS
    val medida = medida(datos, anchoUtil, area, vertical = true)
    val hueco = SqTema.tamanos.touchRide + separacion
    val alto = if (estado.botones) area - cesion(area, medida, hueco) else area
    Column(
        Modifier.fillMaxSize().padding(start = SqTema.espacios.space6, end = SqTema.espacios.space6, top = 0.dp, bottom = SqTema.espacios.space4),
        verticalArrangement = Arrangement.spacedBy(separacion),
    ) {
        // La fila de la plataforma (`sq-ride__statusrow`): el estado de la publicación a la izquierda, que pasa a dos líneas
        // antes de empujar nada, y la barra de conexiones a la derecha; debajo (`sq-ride__modeline`), el modo con la
        // transmisión, que si es largo pasa a dos líneas, y Ajustes al final.
        // Las dos filas van juntas, sin el hueco entre bloques: sus zonas táctiles de 48 dp quedan adyacentes y entre el chip y el modo
        // quedan unos 12 dp (`.sq-ride--v > .sq-ride__statusrow`).
        Column {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(SqTema.espacios.space3), verticalAlignment = Alignment.Top) {
                Box(Modifier.weight(1f).heightIn(min = SqTema.tamanos.touchMin), contentAlignment = Alignment.CenterStart) { EstadoDePlataforma(estado.plataforma, abrir.plataformas) }
                Conexiones(estado, abrir.dispositivos)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(SqTema.espacios.space3), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.weight(1f)) { Modo(estado, botones.modo) }
                BotonAjustes(abrir.ajustes)
            }
        }
        val erg = estado.erg
        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            Encajar {
                if (erg != null) {
                    Objetivo(erg)
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(SqTema.espacios.space3)) {
                        TurnIndicator(estado.giro) { Marcha(estado, animar) }
                        Platos(estado, vertical = true)
                    }
                }
            }
        }
        if (erg == null) Escalera(estado, vertical = true, baja = baja)
        Datos(datos, medida, Modifier.height(alto).fillMaxWidth(), vertical = true)
        Pie(estado, acciones)
        // Como `.sq-ride--v .sq-ride__buttons`: una fila al pie, − a la izquierda y + a la derecha.
        if (estado.botones) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(SqTema.espacios.space3)) {
                ButtonRide("−", stringResource(R.string.marcha_mas_ligera), botones.menos, Modifier.weight(1f))
                ButtonRide("+", stringResource(R.string.marcha_mas_dura), botones.mas, Modifier.weight(1f))
            }
        }
    }
}

/** El área fija de datos: el 40 % del alto útil en vertical y del ancho útil en horizontal (`RideScreen`). */
private const val AREA_DATOS = 0.4f

/** Lo que la rejilla cede a los botones: lo que le sobra con su cifra, hasta el [hueco] que necesitan. */
private fun cesion(area: Dp, medida: MedidaRejilla, hueco: Dp): Dp = minOf(hueco, maxOf(0.dp, area - medida.minimo))

@Composable
private fun datosDeRejilla(estado: RodajeUiState, datos: List<DatoUi> = estado.datosVisibles): List<Dato> = datos.map { Dato(stringResource(nombreDato(it.dato)), it.valor, it.unidad, plantilla = plantillaDato(it.dato), tono = it.tono) }

@Composable
private fun medida(datos: List<Dato>, ancho: Dp, alto: Dp, vertical: Boolean): MedidaRejilla {
    val medidor = rememberTextMeasurer()
    val densidad = LocalDensity.current
    val aMedir = datosParaMedir(datos)
    return remember(aMedir, ancho, alto, vertical, densidad) { medirRejilla(aMedir, ancho, alto, vertical, medidor, densidad) }
}

/**
 * La marcha, los platos o la potencia objetivo a su tamaño natural y, si no caben en el sitio que les queda, reducidos
 * lo justo (nunca por encima de su tamaño). Como `sq-ride__fit` en las maquetas; la escalera y el cassette no se
 * reducen.
 */
@Composable
private fun Encajar(modifier: Modifier = Modifier, contenido: @Composable () -> Unit) {
    Layout(contenido, modifier) { medibles, restricciones ->
        val p = medibles.single().measure(Constraints())
        val k = minOf(
            1f,
            if (restricciones.hasBoundedWidth) restricciones.maxWidth.toFloat() / p.width else 1f,
            if (restricciones.hasBoundedHeight) restricciones.maxHeight.toFloat() / p.height else 1f,
        )
        val ancho = (p.width * k).roundToInt()
        val alto = (p.height * k).roundToInt()
        layout(ancho, alto) {
            // La medida natural, escalada desde su centro y centrada en la caja ya reducida.
            p.placeWithLayer((ancho - p.width) / 2, (alto - p.height) / 2) {
                scaleX = k
                scaleY = k
            }
        }
    }
}

@Composable
private fun Marcha(estado: RodajeUiState, animar: Boolean) {
    val p = estado.platos
    if (p != null) {
        GearDisplay(
            plato = p.plato,
            pinon = p.pinon,
            descripcion = stringResource(R.string.plato_pinon_descripcion, p.plato, p.pinon) +
                (if (estado.tope != null) ", " + stringResource(R.string.tope) else ""),
            tope = estado.tope?.let { textoTope(it) },
            reservaTope = stringResource(R.string.tope_mas_ligera),
            animar = animar,
        )
        return
    }
    GearDisplay(
        marcha = estado.marcha,
        total = estado.total,
        etiqueta = stringResource(R.string.marcha),
        deTotal = stringResource(R.string.de_total, estado.total),
        descripcion = stringResource(R.string.marcha_de_total, estado.marcha, estado.total) +
            (if (estado.tope != null) ", " + stringResource(R.string.tope) else ""),
        tope = estado.tope?.let { textoTope(it) },
        reservaTope = stringResource(R.string.tope_mas_ligera),
        animar = animar,
    )
}

/** En ERG, la potencia objetivo ocupa el lugar de la marcha (`sq-ride__erg`); en ERG manual, sin línea debajo. */
@Composable
private fun Objetivo(erg: ErgUi) {
    ErgTarget(
        etiqueta = stringResource(R.string.objetivo),
        vatios = erg.potenciaW,
        deZwift = if (erg.manual) null else stringResource(R.string.vatios_de_plataforma, erg.deZwiftW),
        ajuste = if (!erg.manual && erg.ajusteW != 0) stringResource(R.string.ajuste_palancas, conSigno(erg.ajusteW)) else null,
        descripcion = stringResource(R.string.objetivo_descripcion, erg.potenciaW),
    )
}

/** Con platos, el indicador de plato; con la lineal, nada. */
@Composable
private fun Platos(estado: RodajeUiState, vertical: Boolean) {
    val p = estado.platos ?: return
    ChainringIndicator(p.platos, p.plato, stringResource(R.string.plato), stringResource(R.string.plato_descripcion, p.plato), vertical = vertical)
}

/** La escalera con la lineal o el cassette con platos (`CassetteView`, 56 dp en vertical y 48 dp en horizontal). */
@Composable
private fun Escalera(estado: RodajeUiState, vertical: Boolean, baja: Boolean = false) {
    val p = estado.platos
    if (p != null) {
        CassetteView(
            p.pinones,
            p.pinon,
            stringResource(R.string.cassette_descripcion, p.pinones.min(), p.pinones.max(), p.pinon),
            alto = if (vertical) (if (baja) 40.dp else 56.dp) else 48.dp,
        )
        return
    }
    GearLadder(estado.total, estado.marcha, estado.referencia, stringResource(R.string.ligera), stringResource(R.string.dura))
}

@Composable
private fun Datos(datos: List<Dato>, medida: MedidaRejilla, modifier: Modifier, vertical: Boolean) {
    MetricGrid(datos, modifier, descripcionSinDato = stringResource(R.string.sin_dato), vertical = vertical, cifra = medida.cifra)
}

@Composable
private fun Pie(estado: RodajeUiState, acciones: AccionesGuia) {
    Box(Modifier.fillMaxWidth().heightIn(min = 40.dp), contentAlignment = Alignment.Center) {
        val aviso = estado.aviso
        when {
            aviso != null -> ActionToast(
                when (aviso) {
                    is AvisoUi.Marcha -> textoAccion(aviso.accion, aviso.diferencia)

                    is AvisoUi.Vatios -> textoVatios(aviso.diferenciaW)

                    AvisoUi.PotenciaZwift -> stringResource(R.string.accion_potencia_plataforma)

                    is AvisoUi.VueltaA -> stringResource(R.string.accion_vuelta_a, aviso.potenciaW)

                    is AvisoUi.TopePotencia -> stringResource(R.string.tope_potencia, aviso.potenciaW)

                    is AvisoUi.ModoErg -> aviso.potenciaW?.let { stringResource(R.string.accion_erg_manual, it) }
                        ?: stringResource(if (aviso.libre) R.string.accion_modo_libre else R.string.accion_modo_sim)

                    AvisoUi.EntrenamientoZwift -> stringResource(R.string.accion_entrenamiento_plataforma)

                    is AvisoUi.Referencia -> textoReferenciaFijada(aviso.marcha)

                    is AvisoUi.Plato -> stringResource(R.string.accion_plato, aviso.dientes)

                    is AvisoUi.Modelo -> stringResource(R.string.accion_modelo, stringResource(nombreModelo(aviso.modelo)))

                    is AvisoUi.Ventilador -> textoAvisoVentilador(aviso.aviso)
                },
            )

            // Con la guía a la vista, su tira ocupa el sitio del texto de conexión (spec `ride-screen`, «Guía de inicio en Rodaje»).
            estado.guia != null -> GuideStrip(estado.guia.pasos.siguiente, acciones, oculta = estado.guia.abierta)

            else -> when (estado.pie) {
                PieUi.Nada -> ActionToast(null)
                PieUi.TocarParaConectar -> Caption(stringResource(R.string.tocar_para_conectar))
                PieUi.DespertarNeo -> Caption(stringResource(R.string.despertar_neo))
                PieUi.DespertarSquare -> Caption(stringResource(R.string.despertar_square))
                PieUi.DespertarLosDos -> Caption(stringResource(R.string.despertar_los_dos))
            }
        }
    }
}

@Composable
private fun BarraSuperior(estado: RodajeUiState, abrir: Abrir, alternarModo: () -> Unit) {
    // Como `sq-ride__top--status`: tres columnas. El modo con la transmisión a la izquierda, el estado de la plataforma centrado en
    // la pantalla y las conexiones con Ajustes a la derecha. Si el modo no cabe en su lado, la transmisión pasa a dos líneas; si el
    // estado no cabe en el centro (sin descentrarse), pasa a dos líneas antes de empujar las conexiones.
    val hueco = with(LocalDensity.current) { SqTema.espacios.space3.roundToPx() }
    Layout(
        content = {
            Modo(estado, alternarModo)
            EstadoDePlataforma(estado.plataforma, abrir.plataformas)
            // El botón mide 48 dp y el de la maqueta 32 (su zona táctil se amplía por fuera): los 16 dp de más se restan del hueco
            // de `space-3` entre la barra y el icono, para que el icono quede donde en la maqueta.
            Row(horizontalArrangement = Arrangement.spacedBy(SqTema.espacios.space3 - 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Conexiones(estado, abrir.dispositivos)
                BotonAjustes(abrir.ajustes)
            }
        },
        modifier = Modifier.fillMaxWidth(),
    ) { medidas, restricciones ->
        val ancho = restricciones.maxWidth
        val derecha = medidas[2].measure(Constraints())
        val centro = medidas[1].measure(Constraints(maxWidth = (ancho - 2 * (derecha.width + hueco)).coerceAtLeast(0)))
        val xCentro = (ancho - centro.width) / 2
        val izquierda = medidas[0].measure(Constraints(maxWidth = (xCentro - hueco).coerceAtLeast(0)))
        val alto = maxOf(izquierda.height, centro.height, derecha.height)
        layout(ancho, alto) {
            izquierda.placeRelative(0, (alto - izquierda.height) / 2)
            centro.placeRelative(xCentro, (alto - centro.height) / 2)
            derecha.placeRelative(ancho - derecha.width, (alto - derecha.height) / 2)
        }
    }
}

/**
 * Acceso a Ajustes (`sq-ride__settings`): el icono de la pestaña, en `ink-muted`, con objetivo de `touch-min`. Se
 * desplaza hacia el margen para que el icono quede donde en la maqueta, que lo mide 32 como los chips y amplía la zona
 * táctil por fuera (spec `ride-screen`, «Ajustes desde Rodaje»).
 */
@Composable
private fun BotonAjustes(alTocar: () -> Unit) {
    val descripcion = stringResource(R.string.ajustes)
    Box(
        Modifier
            .offset(x = (SqTema.tamanos.touchMin - 32.dp) / 2)
            .size(SqTema.tamanos.touchMin)
            .clip(CircleShape)
            .clickable(role = Role.Button, onClick = alTocar)
            .semantics { contentDescription = descripcion },
        contentAlignment = Alignment.Center,
    ) {
        Icon(painterResource(R.drawable.ic_tune), contentDescription = null, tint = SqTema.colores.inkMuted)
    }
}

@Composable
private fun Modo(estado: RodajeUiState, alternar: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(SqTema.espacios.space3)) {
        val erg = estado.erg
        val tocable = estado.modoTocable
        if (erg != null && erg.manual) {
            ModeBadge(stringResource(R.string.modo_erg), erg = true, origen = stringResource(R.string.modo_origen_manual), alPulsar = alternar.takeIf { tocable }, descripcion = stringResource(R.string.modo_tocar_erg_manual))
        } else if (erg != null) {
            ModeBadge(stringResource(R.string.modo_erg), erg = true, origen = stringResource(R.string.origen_erg_plataforma), alPulsar = alternar.takeIf { tocable }, descripcion = stringResource(R.string.modo_tocar_erg_app))
        } else {
            ModeBadge(
                stringResource(if (estado.libre) R.string.modo_libre else R.string.modo_sim),
                libre = estado.libre,
                alPulsar = alternar.takeIf { tocable },
                descripcion = stringResource(if (estado.libre) R.string.modo_tocar_libre else R.string.modo_tocar_sim),
            )
            Caption(textoTransmision(estado.transmision, estado.modoCambio))
        }
    }
}

/** La barra de conexiones (`sq-connbar`): un icono por enlace, con la descripción de cada estado en la de la barra. */
@Composable
private fun Conexiones(estado: RodajeUiState, abrirDispositivos: () -> Unit) {
    val enlaces = estado.enlaces.map { e ->
        val nombre = stringResource(nombreDe(e.enlace))
        val palabra = stringResource(
            when (e.enPalabra) {
                EnlaceUi.EnPalabra.Conectando -> R.string.chip_conectando
                EnlaceUi.EnPalabra.Reconectando -> R.string.chip_reconectando
                EnlaceUi.EnPalabra.Esperando -> R.string.chip_esperando
                EnlaceUi.EnPalabra.SinConexion -> R.string.chip_sin_conexion
                EnlaceUi.EnPalabra.NoVisible -> R.string.chip_no_visible
                EnlaceUi.EnPalabra.NoDisponible -> R.string.chip_no_disponible
                EnlaceUi.EnPalabra.BateriaBaja -> R.string.chip_bateria_baja
                null -> R.string.chip_conectado
            },
        )
        EnlaceBarra(e.enlace, e.estado, stringResource(R.string.chip_descripcion, nombre, palabra))
    }
    ConnectionBar(enlaces, abrirDispositivos)
}

private fun nombreDe(e: Enlace): Int = when (e) {
    Enlace.Neo -> R.string.neo
    Enlace.Square -> R.string.enlace_controlador_square
    Enlace.Zwift -> R.string.zwift
    Enlace.Mando -> R.string.enlace_controlador
    Enlace.Pulso -> R.string.pulsometro
    Enlace.Ventilador -> R.string.ventilador
}

/**
 * El estado de lo publicado (`PlatformBadge`, spec `ride-screen`, «Estado de la publicación en Rodaje»): un chip de una
 * línea con el estado del rodillo y lo que falta, en palabra y forma, sin nombre de plataforma, que abre Conexión.
 * Atenuado mientras solo espera.
 */
@Composable
private fun EstadoDePlataforma(p: PlataformaUi, abrirConexion: () -> Unit) {
    val texto = stringResource(
        when (p.texto) {
            PlataformaUi.Texto.Conectado -> R.string.publicado_conectado
            PlataformaUi.Texto.ConectadoSinMando -> R.string.publicado_conectado_sin_mando
            PlataformaUi.Texto.ConectadoSinPulso -> R.string.publicado_conectado_sin_pulso
            PlataformaUi.Texto.ConectadoSinMandoNiPulso -> R.string.publicado_conectado_sin_mando_ni_pulso
            PlataformaUi.Texto.Esperando -> R.string.publicado_esperando
            PlataformaUi.Texto.NoVisible -> R.string.publicado_no_visible
            PlataformaUi.Texto.SinWifi -> R.string.publicado_sin_wifi
            PlataformaUi.Texto.SinBluetooth -> R.string.publicado_sin_bluetooth
            PlataformaUi.Texto.NoDisponible -> R.string.publicado_no_disponible
            PlataformaUi.Texto.SinRodillo -> R.string.publicado_sin_rodillo
        },
    )
    val descripcion = if (p.texto == PlataformaUi.Texto.SinRodillo) {
        stringResource(R.string.publicado_descripcion_sin_rodillo)
    } else {
        listOfNotNull(
            stringResource(R.string.publicado_descripcion, stringResource(enPalabra(p.rodillo))),
            p.mando?.let { stringResource(R.string.publicado_descripcion_mando, stringResource(enPalabra(it))) },
            p.pulso?.let { stringResource(R.string.publicado_descripcion_pulso, stringResource(enPalabra(it))) },
        ).joinToString("; ")
    }
    ConnectionChip(p.estado, texto, alTocar = abrirConexion, descripcion = descripcion, atenuado = p.estado == EstadoChip.Esperando)
}

private fun enPalabra(e: EnlaceUi.EnPalabra?): Int = when (e) {
    EnlaceUi.EnPalabra.Conectando -> R.string.chip_conectando
    EnlaceUi.EnPalabra.Reconectando -> R.string.chip_reconectando
    EnlaceUi.EnPalabra.Esperando -> R.string.chip_esperando
    EnlaceUi.EnPalabra.SinConexion -> R.string.chip_sin_conexion
    EnlaceUi.EnPalabra.NoVisible -> R.string.chip_no_visible
    EnlaceUi.EnPalabra.NoDisponible -> R.string.chip_no_disponible
    EnlaceUi.EnPalabra.BateriaBaja -> R.string.chip_bateria_baja
    null -> R.string.chip_conectado
}
