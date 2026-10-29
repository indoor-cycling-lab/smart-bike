package app.smartbike.ui.dispositivos

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import app.smartbike.R
import app.smartbike.plataforma.EstadoMando
import app.smartbike.sesion.Sesion
import app.smartbike.ui.componentes.AppBar
import app.smartbike.ui.componentes.Button
import app.smartbike.ui.componentes.Caption
import app.smartbike.ui.componentes.ConnectionChip
import app.smartbike.ui.componentes.Enlace
import app.smartbike.ui.componentes.EstadoChip
import app.smartbike.ui.componentes.IconoEnlace
import app.smartbike.ui.componentes.Notice
import app.smartbike.ui.componentes.SettingsItemApilado
import app.smartbike.ui.componentes.SettingsList
import app.smartbike.ui.componentes.SettingsSection
import app.smartbike.ui.navegacion.PantallaPestana
import app.smartbike.ui.navegacion.Pestana
import app.smartbike.ui.tema.SqTema

/** Acciones de Dispositivos; las de Android (permisos, ajustes, Bluetooth) las resuelve [DispositivosRuta]. */
class AccionesDispositivos(
    val irA: (Pestana) -> Unit = {},
    val buscarRodillo: () -> Unit = {},
    val activarBluetooth: () -> Unit = {},
    val abrirAjustes: () -> Unit = {},
    val olvidar: (Sesion.Dispositivo) -> Unit = {},
    val buscarPulsometro: () -> Unit = {},
    val buscarVentilador: () -> Unit = {},
    val buscarMando: () -> Unit = {},
    val reconectar: (Sesion.Dispositivo) -> Unit = {},
)

@Composable
fun DispositivosRuta(irA: (Pestana) -> Unit, vm: DispositivosViewModel = viewModel(factory = DispositivosViewModel.Factory)) {
    val estado by vm.estado.collectAsStateWithLifecycle()
    val conexion = rememberAccionesConexion(vm::permisoConcedido, vm::buscarRodillo, vm::buscarPulsometro, vm::buscarVentilador, vm::buscarMando, vm::reconectar)
    Dispositivos(
        estado,
        AccionesDispositivos(
            irA,
            conexion.buscar,
            conexion.activarBluetooth,
            conexion.abrirAjustes,
            vm::olvidar,
            conexion.buscarPulsometro,
            conexion.buscarVentilador,
            conexion.buscarMando,
            conexion.reconectar,
        ),
    )
}

/**
 * Dispositivos (maquetas/dispositivos.html): los dispositivos a los que se conecta la app, una sección por tipo (Rodillo,
 * Mando, Pulsómetro y Ventilador) con su estado y «Buscar» sin configurado, u «Olvidar» y, sin conexión, «Reconectar» (spec
 * `device-connection`). Lo que la app publica está en Conexión. Se abre también desde la barra de conexiones de Rodaje.
 */
@Composable
fun Dispositivos(estado: DispositivosUiState, acciones: AccionesDispositivos, modifier: Modifier = Modifier) {
    PantallaPestana(Pestana.Dispositivos, acciones.irA, modifier) {
        // El párrafo va pegado al título, sin la separación entre bloques (`.sq-appbar + .sq-intro`).
        Column {
            AppBar(stringResource(R.string.dispositivos))
            Caption(stringResource(R.string.dispositivos_intro))
        }
        estado.avisos.forEach { Notice(textoAviso(it, estado.nombrePlataforma), error = true) }
        Seccion(Tipo.Rodillo, estado.neo, estado.buscandoNeo, null, acciones.buscarRodillo, { acciones.reconectar(Sesion.Dispositivo.Neo) }, { acciones.olvidar(Sesion.Dispositivo.Neo) })
        Seccion(
            Tipo.Mando,
            estado.square,
            estado.buscandoSquare,
            null,
            acciones.buscarMando,
            { acciones.reconectar(Sesion.Dispositivo.Square) },
            { acciones.olvidar(Sesion.Dispositivo.Square) },
            aviso = estado.bateriaSquareBajaPct?.let { stringResource(R.string.aviso_bateria_square_baja, it) },
        )
        Seccion(
            Tipo.Pulsometro,
            estado.pulsometro,
            estado.buscandoPulsometro,
            stringResource(R.string.pulsometro_ayuda_conectado, estado.nombreAnunciado),
            acciones.buscarPulsometro,
            { acciones.reconectar(Sesion.Dispositivo.Pulsometro) },
            { acciones.olvidar(Sesion.Dispositivo.Pulsometro) },
        )
        Seccion(
            Tipo.Ventilador,
            estado.ventilador,
            estado.buscandoVentilador,
            stringResource(R.string.ventilador_ayuda_conectado),
            acciones.buscarVentilador,
            { acciones.reconectar(Sesion.Dispositivo.Ventilador) },
            { acciones.olvidar(Sesion.Dispositivo.Ventilador) },
        )
        when (estado.accion) {
            AccionConexiones.ActivarBluetooth -> Button(stringResource(R.string.activar_bluetooth), acciones.activarBluetooth, Modifier.fillMaxWidth(), principal = true)
            AccionConexiones.AbrirAjustes -> Button(stringResource(R.string.abrir_ajustes), acciones.abrirAjustes, Modifier.fillMaxWidth(), principal = true)
            AccionConexiones.Buscar, AccionConexiones.Buscando -> Unit
        }
    }
}

/** Lo que cambia de una sección a otra: icono, nombres, cómo despertarlo y las ayudas sin configurado y sin encontrar. */
private enum class Tipo(
    val enlace: Enlace,
    @StringRes val nombre: Int,
    @StringRes val accesible: Int,
    @StringRes val despertar: Int,
    @StringRes val sinConfigurado: Int,
    @StringRes val noEncontrado: Int?,
) {
    Rodillo(Enlace.Neo, R.string.rodillo, R.string.tipo_rodillo, R.string.neo_despertar, R.string.rodillo_ayuda_sin_configurado, null),
    Mando(Enlace.Square, R.string.servicio_controlador, R.string.tipo_controlador, R.string.square_despertar, R.string.mando_ayuda_sin_configurado, null),
    Pulsometro(Enlace.Pulso, R.string.pulsometro, R.string.tipo_pulsometro, R.string.pulsometro_despertar, R.string.pulsometro_ayuda_sin_configurado, R.string.pulsometro_ayuda_no_encontrado),
    Ventilador(Enlace.Ventilador, R.string.ventilador, R.string.tipo_ventilador, R.string.ventilador_despertar, R.string.ventilador_ayuda_sin_configurado, R.string.ventilador_ayuda_no_encontrado),
}

/**
 * Una sección de Dispositivos (spec `device-connection`, «Botones de cada sección»): sin configurado, solo «Buscar»; con uno,
 * «Olvidar» y, si no está conectado, «Reconectar», que reinicia el intento con ese dispositivo. El texto visible de los
 * botones no nombra el tipo; su descripción accesible, sí.
 */
@Composable
private fun Seccion(
    tipo: Tipo,
    fila: FilaDispositivoUi,
    buscando: Boolean,
    ayudaConectado: String?,
    alBuscar: () -> Unit,
    alReconectar: () -> Unit,
    alOlvidar: () -> Unit,
    /** Un aviso informativo bajo la fila (la batería baja del Square). */
    aviso: String? = null,
) {
    val nombre = stringResource(tipo.nombre)
    val accesible = stringResource(tipo.accesible)
    val configurado = fila.configurado != null
    SettingsSection(nombre) {
        SettingsList(
            listOf {
                SettingsItemApilado {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(SqTema.espacios.space3), verticalAlignment = Alignment.CenterVertically) {
                        IconoEnlace(tipo.enlace)
                        Column(Modifier.weight(1f)) {
                            Text(nombre, style = SqTema.tipografia.body.copy(fontWeight = FontWeight(600)))
                            Caption(fila.configurado ?: stringResource(R.string.ninguno_configurado))
                        }
                        // Sin configurado y sin buscar, la fila no dice ningún estado: no es un fallo.
                        when {
                            fila.estado == EstadoFila.Conectado -> Estado(EstadoChip.Ok, R.string.estado_conectado)
                            fila.estado == EstadoFila.Conectando -> Estado(EstadoChip.Esperando, R.string.estado_conectando)
                            fila.estado == EstadoFila.Reconectando -> Estado(EstadoChip.Esperando, R.string.estado_reconectando)
                            fila.estado == EstadoFila.Buscando -> Estado(EstadoChip.Esperando, R.string.estado_buscando)
                            fila.estado == EstadoFila.NoEncontrado -> Estado(EstadoChip.Sin, R.string.estado_no_encontrado)
                            configurado -> Estado(EstadoChip.Sin, R.string.estado_sin_conexion)
                        }
                    }
                    // Al conectar con lo encontrado ([buscando]) sigue la búsqueda: la misma ayuda, sin volver a «Busca el…».
                    val ayuda = when {
                        fila.estado == EstadoFila.Conectado -> ayudaConectado
                        fila.estado == EstadoFila.NoEncontrado -> tipo.noEncontrado?.let { stringResource(it) }
                        !configurado && buscando -> stringResource(tipo.despertar)
                        !configurado -> stringResource(tipo.sinConfigurado)
                        else -> stringResource(R.string.dispositivo_reconectar_ayuda, stringResource(tipo.despertar))
                    }
                    if (ayuda != null) Text(ayuda, style = SqTema.tipografia.body)
                    Row(horizontalArrangement = Arrangement.spacedBy(SqTema.espacios.space3)) {
                        if (!configurado) {
                            Button(stringResource(R.string.buscar), alBuscar, habilitado = !buscando, descripcion = stringResource(R.string.buscar_tipo, accesible))
                        } else {
                            if (fila.estado != EstadoFila.Conectado) {
                                Button(stringResource(R.string.reconectar), alReconectar, descripcion = stringResource(R.string.reconectar_tipo, accesible))
                            }
                            Button(stringResource(R.string.olvidar), alOlvidar, descripcion = stringResource(R.string.olvidar_tipo, accesible))
                        }
                    }
                }
            },
        )
        aviso?.let { Notice(it) }
    }
}

@Composable
private fun Estado(chip: EstadoChip, @StringRes texto: Int) = ConnectionChip(chip, stringResource(texto))

/** El estado del mando en palabra, también en Diagnóstico; «Desactivado» y «No disponible» con la forma neutra. */
@Composable
fun EstadoMandoChip(m: EstadoMando) = when (m) {
    EstadoMando.Conectado -> Estado(EstadoChip.Ok, R.string.estado_conectado)
    EstadoMando.Esperando -> Estado(EstadoChip.Esperando, R.string.estado_esperando)
    EstadoMando.SinConexion -> Estado(EstadoChip.Sin, R.string.estado_sin_conexion)
    EstadoMando.Desactivado -> Estado(EstadoChip.Neutro, R.string.estado_desactivado)
    EstadoMando.NoDisponible -> Estado(EstadoChip.Neutro, R.string.estado_no_disponible)
}

/** «OnePlus 8 · por Bluetooth», o «Por Bluetooth» si no se sabe el nombre. */
@Composable
fun textoMovil(nombre: String?): String = nombre?.let { stringResource(R.string.mando_movil, it) } ?: stringResource(R.string.mando_movil_sin_nombre)
