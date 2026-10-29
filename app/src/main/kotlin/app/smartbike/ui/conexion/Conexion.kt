package app.smartbike.ui.conexion

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import app.smartbike.R
import app.smartbike.dominio.Identidad
import app.smartbike.dominio.ModoConexion
import app.smartbike.dominio.ModoPublicado
import app.smartbike.dominio.NombreAnunciado
import app.smartbike.dominio.Validacion
import app.smartbike.ui.componentes.AppBar
import app.smartbike.ui.componentes.Button
import app.smartbike.ui.componentes.Caption
import app.smartbike.ui.componentes.Enlace
import app.smartbike.ui.componentes.EstadoChip
import app.smartbike.ui.componentes.Label
import app.smartbike.ui.componentes.Notice
import app.smartbike.ui.componentes.Segmentado
import app.smartbike.ui.componentes.ServiceRow
import app.smartbike.ui.componentes.SettingsItemCasilla
import app.smartbike.ui.componentes.SettingsList
import app.smartbike.ui.componentes.SettingsOpcion
import app.smartbike.ui.componentes.SettingsSection
import app.smartbike.ui.dispositivos.rememberAccionesConexion
import app.smartbike.ui.navegacion.PantallaPestana
import app.smartbike.ui.navegacion.Pestana
import app.smartbike.ui.tema.SqTema

/** Acciones de Conexión; activar el Bluetooth lo resuelve [ConexionRuta] con Android. */
class AccionesConexionPestana(
    val irA: (Pestana) -> Unit = {},
    val modo: (ModoConexion) -> Unit = {},
    val identidad: (Identidad) -> Unit = {},
    val nombrePorBluetooth: (Boolean) -> Unit = {},
    val activarBluetooth: () -> Unit = {},
)

@Composable
fun ConexionRuta(irA: (Pestana) -> Unit, vm: ConexionViewModel = viewModel(factory = ConexionViewModel.Factory)) {
    val estado by vm.estado.collectAsStateWithLifecycle()
    val conexion = rememberAccionesConexion(permisoConcedido = {}, buscar = {})
    Conexion(estado, AccionesConexionPestana(irA, vm::modo, vm::identidad, vm::nombrePorBluetooth, conexion.activarBluetooth))
}

/**
 * Conexión (maquetas/conexion.html; spec `platform-settings`): cómo se conecta la app de entrenamiento a Smart Bike. De
 * arriba abajo, los avisos, el estado de cada servicio publicado, el modo y el nombre ante la app.
 */
@Composable
fun Conexion(estado: ConexionUiState, acciones: AccionesConexionPestana, modifier: Modifier = Modifier) {
    PantallaPestana(Pestana.Conexion, acciones.irA, modifier) {
        // El párrafo va pegado al título, sin la separación entre bloques (`.sq-appbar + .sq-intro`).
        Column {
            AppBar(stringResource(R.string.conexion))
            Caption(stringResource(R.string.conexion_intro))
        }
        if (estado.sinRodillo) Notice(stringResource(R.string.conexion_sin_rodillo))
        if (estado.noVisible) Notice(stringResource(R.string.conexion_no_visible), error = true)
        if (estado.avisoBluetooth) Notice(stringResource(R.string.conexion_bluetooth_apagado), error = true)
        SettingsSection(stringResource(R.string.conexion_estado)) {
            SettingsList(
                listOf(
                    { Servicio(Enlace.Neo, R.string.servicio_rodillo, R.string.sin_rodillo_configurado, estado.rodillo, estado, acciones) },
                    { Servicio(Enlace.Mando, R.string.servicio_controlador_zwift, R.string.sin_mando_configurado, estado.mando, estado, acciones) },
                    { Servicio(Enlace.Pulso, R.string.servicio_pulso, R.string.sin_pulsometro_configurado, estado.pulso, estado, acciones) },
                ),
            )
        }
        if (estado.avisoBluetooth) Button(stringResource(R.string.activar_bluetooth), acciones.activarBluetooth, Modifier.fillMaxWidth(), principal = true)
        Modos(estado, acciones)
        NombreAnteLaApp(estado, acciones)
    }
}

@Composable
private fun Servicio(
    enlace: Enlace,
    @StringRes nombre: Int,
    @StringRes sinConfigurado: Int,
    s: ServicioUi,
    estado: ConexionUiState,
    acciones: AccionesConexionPestana,
) {
    val movil = estado.nombreMovil ?: stringResource(R.string.este_movil)
    val noSeOfrece = s.estado == EstadoServicio.NoSeOfrece
    val via = when (s.via) {
        null -> stringResource(sinConfigurado)
        Via.Wifi -> stringResource(R.string.via_wifi)
        Via.Bluetooth -> stringResource(R.string.via_bluetooth)
        Via.DentroWifi -> stringResource(R.string.via_dentro_wifi)
        Via.DentroBluetooth -> stringResource(R.string.via_dentro_bluetooth, movil)
    }
    ServiceRow(
        enlace,
        stringResource(nombre),
        if (enlace == Enlace.Neo && s.via != null) stringResource(R.string.via_rodillo, via.replaceFirstChar { it.lowercase() }) else via,
        chip(s.estado),
        stringResource(textoEstado(s.estado)),
        s.ayuda?.let { textoAyuda(it, estado.nombreAnunciado, movil) },
        noSeOfrece = noSeOfrece,
        alTocar = if (noSeOfrece) ({ acciones.irA(Pestana.Dispositivos) }) else null,
    )
}

private fun chip(e: EstadoServicio): EstadoChip = when (e) {
    EstadoServicio.Conectado, EstadoServicio.Enviando -> EstadoChip.Ok
    EstadoServicio.Esperando -> EstadoChip.Esperando
    EstadoServicio.SinWifi, EstadoServicio.SinBluetooth, EstadoServicio.NoVisible, EstadoServicio.SinLectura -> EstadoChip.Sin
    EstadoServicio.NoDisponible, EstadoServicio.NoSeOfrece -> EstadoChip.Neutro
}

@StringRes
private fun textoEstado(e: EstadoServicio): Int = when (e) {
    EstadoServicio.Conectado -> R.string.estado_conectado
    EstadoServicio.Enviando -> R.string.estado_enviando
    EstadoServicio.Esperando -> R.string.estado_esperando
    EstadoServicio.SinWifi -> R.string.estado_sin_wifi
    EstadoServicio.SinBluetooth -> R.string.estado_sin_bluetooth
    EstadoServicio.NoVisible -> R.string.estado_no_visible
    EstadoServicio.NoDisponible -> R.string.estado_no_disponible
    EstadoServicio.SinLectura -> R.string.estado_sin_lectura
    EstadoServicio.NoSeOfrece -> R.string.estado_no_se_ofrece
}

@Composable
private fun textoAyuda(a: AyudaServicio, nombreAnunciado: String, movil: String): String = when (a) {
    AyudaServicio.EmparejarRodillo -> stringResource(R.string.ayuda_emparejar_rodillo, nombreAnunciado)
    AyudaServicio.EmparejarRodilloMovil -> stringResource(R.string.ayuda_emparejar_rodillo_movil, movil)
    AyudaServicio.EmparejarTodoWifi -> stringResource(R.string.ayuda_emparejar_todo_wifi, nombreAnunciado)
    AyudaServicio.MandoBluetooth -> stringResource(R.string.ayuda_mando_bluetooth, movil)
    AyudaServicio.MandoBluetoothNombre -> stringResource(R.string.ayuda_mando_bluetooth_nombre, movil)
    AyudaServicio.MandoDentro -> stringResource(R.string.ayuda_mando_dentro)
    AyudaServicio.MandoSinSquare -> stringResource(R.string.ayuda_mando_sin_square)
    AyudaServicio.PulsoElegir -> stringResource(R.string.ayuda_pulso_elegir)
    AyudaServicio.PulsoElegirMovil -> stringResource(R.string.ayuda_pulso_elegir_movil, movil)
    AyudaServicio.SinWifi -> stringResource(R.string.ayuda_sin_wifi)
    AyudaServicio.SinBluetooth -> stringResource(R.string.ayuda_sin_bluetooth)
    AyudaServicio.RodilloNoDisponible -> stringResource(R.string.ayuda_rodillo_no_disponible)
    AyudaServicio.MandoNoDisponible -> stringResource(R.string.ayuda_mando_no_disponible)
    AyudaServicio.BandaSinLectura -> stringResource(R.string.ayuda_banda_sin_lectura)
    AyudaServicio.NeoReconectando -> stringResource(R.string.ayuda_neo_reconectando)
    AyudaServicio.SquareReconectando -> stringResource(R.string.ayuda_square_reconectando)
    AyudaServicio.BuscarRodillo -> stringResource(R.string.ayuda_buscar_rodillo)
    AyudaServicio.BuscarMando -> stringResource(R.string.ayuda_buscar_mando)
    AyudaServicio.BuscarPulso -> stringResource(R.string.ayuda_buscar_pulso)
}

/** Los tres modos (`ModeList`): el nombre por transporte, por dónde va cada servicio, el detalle técnico y la compatibilidad. */
@Composable
private fun Modos(estado: ConexionUiState, acciones: AccionesConexionPestana) {
    Column(verticalArrangement = Arrangement.spacedBy(SqTema.espacios.space2)) {
        Label(stringResource(R.string.conexion_modo))
        Caption(stringResource(R.string.conexion_modo_ayuda))
        SettingsList(
            estado.modos.map { m ->
                {
                    val (nombre, ayuda, detalle) = textosModo(m.modo)
                    SettingsOpcion(
                        stringResource(nombre),
                        stringResource(ayuda),
                        elegida = m.modo == estado.conexion.modo,
                        alElegir = { acciones.modo(m.modo) },
                        etiqueta = if (m.recomendado) stringResource(R.string.recomendado) else null,
                        detalle = stringResource(detalle),
                        etiquetas = compatibilidad(m),
                    )
                }
            },
            Modifier.selectableGroup(),
        )
    }
}

private fun textosModo(modo: ModoConexion): Triple<Int, Int, Int> = when (modo) {
    ModoConexion.FTMS_WIFI -> Triple(R.string.modo_wifi_bluetooth, R.string.modo_wifi_bluetooth_ayuda, R.string.modo_wifi_bluetooth_detalle)
    ModoConexion.FTMS_BLUETOOTH -> Triple(R.string.modo_solo_bluetooth, R.string.modo_solo_bluetooth_ayuda, R.string.modo_solo_bluetooth_detalle)
    ModoConexion.ZWIFT_RIDE -> Triple(R.string.modo_solo_wifi, R.string.modo_solo_wifi_ayuda, R.string.modo_solo_wifi_detalle)
}

/** «**Zwift:** validado», «**Otras apps con FTMS:** a validar» o «Solo para Zwift»: solo lo probado, por el texto. */
@Composable
private fun compatibilidad(m: ModoPublicado): List<AnnotatedString> {
    val probadas = m.probadoCon.map { p ->
        etiqueta("${p.app}:", stringResource(if (p.validacion == Validacion.VALIDADO) R.string.compat_validado else R.string.compat_en_parte))
    }
    val otras = if (m.otrasFtmsAValidar) {
        etiqueta(stringResource(R.string.compat_otras_ftms), stringResource(R.string.compat_a_validar))
    } else {
        AnnotatedString(stringResource(R.string.compat_solo_zwift))
    }
    return probadas + otras
}

private fun etiqueta(prefijo: String, texto: String) = buildAnnotatedString {
    withStyle(SpanStyle(fontWeight = FontWeight(600))) { append(prefijo) }
    append(" ")
    append(texto)
}

/**
 * «Nombre ante la app»: el selector con wifi y la casilla «Usar el nombre también por Bluetooth» con un modo que publica por
 * Bluetooth; con «Solo Bluetooth», la casilla va primero y el selector solo con ella marcada (spec `platform-settings`).
 */
@Composable
private fun NombreAnteLaApp(estado: ConexionUiState, acciones: AccionesConexionPestana) {
    if (!estado.muestraNombre && !estado.muestraCasilla) return
    Column(verticalArrangement = Arrangement.spacedBy(SqTema.espacios.space2)) {
        Label(stringResource(R.string.nombre_ante_la_app))
        if (estado.muestraCasilla && estado.casillaPrimero) CasillaNombreBluetooth(estado, acciones)
        if (estado.muestraNombre) SelectorNombre(estado, acciones)
        if (estado.muestraCasilla && !estado.casillaPrimero) CasillaNombreBluetooth(estado, acciones)
    }
}

@Composable
private fun SelectorNombre(estado: ConexionUiState, acciones: AccionesConexionPestana) {
    Column(verticalArrangement = Arrangement.spacedBy(SqTema.espacios.space2)) {
        val identidades = Identidad.entries
        Segmentado(
            identidades.map { if (it == Identidad.KICKR_BIKE_PRO) NombreAnunciado.KICKR else NombreAnunciado.NORMAL },
            identidades.indexOf(estado.conexion.identidad),
            { acciones.identidad(identidades[it]) },
        )
        Caption(stringResource(if (estado.conexion.identidad == Identidad.KICKR_BIKE_PRO) R.string.nombre_kickr_ayuda_conexion else R.string.nombre_ayuda_conexion))
    }
}

@Composable
private fun CasillaNombreBluetooth(estado: ConexionUiState, acciones: AccionesConexionPestana) {
    val original = estado.nombreOriginalMovil ?: stringResource(R.string.este_movil)
    Column(verticalArrangement = Arrangement.spacedBy(SqTema.espacios.space2)) {
        SettingsList(
            listOf {
                SettingsItemCasilla(
                    stringResource(R.string.nombre_por_bluetooth),
                    estado.conexion.nombrePorBluetooth,
                    acciones.nombrePorBluetooth,
                    ayuda = stringResource(R.string.nombre_por_bluetooth_ayuda, estado.nombreElegido, original),
                )
            },
        )
        if (estado.noSePudoRenombrar) Notice(stringResource(R.string.nombre_por_bluetooth_fallo, original), error = true)
    }
}
