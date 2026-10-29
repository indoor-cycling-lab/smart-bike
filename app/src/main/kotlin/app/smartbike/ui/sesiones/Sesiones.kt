package app.smartbike.ui.sesiones

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import app.smartbike.R
import app.smartbike.datos.EstadoGrabacion
import app.smartbike.datos.EstadoSesion
import app.smartbike.datos.SesionGrabada
import app.smartbike.ui.componentes.AppBar
import app.smartbike.ui.componentes.Button
import app.smartbike.ui.componentes.Caption
import app.smartbike.ui.componentes.Notice
import app.smartbike.ui.componentes.SessionRow
import app.smartbike.ui.componentes.SettingsItemApilado
import app.smartbike.ui.componentes.SettingsList
import app.smartbike.ui.componentes.SettingsSection
import app.smartbike.ui.tema.SqTema
import app.smartbike.ui.tema.localeActual

/** Acciones de Sesiones grabadas. */
class AccionesSesiones(
    val volver: () -> Unit = {},
    val abrir: (SesionGrabada) -> Unit = {},
    val compartir: (SesionGrabada) -> Unit = {},
    val pedirBorrar: (SesionGrabada) -> Unit = {},
    val pedirBorrarTodas: () -> Unit = {},
    val confirmar: () -> Unit = {},
    val cancelar: () -> Unit = {},
)

@Composable
fun SesionesRuta(volver: () -> Unit, vm: SesionesViewModel = viewModel(factory = SesionesViewModel.Factory)) {
    val estado by vm.estado.collectAsStateWithLifecycle()
    val sinApp by vm.sinApp.collectAsStateWithLifecycle()
    val contexto = LocalContext.current
    fun lanzar(s: SesionGrabada, compartir: Boolean) {
        val archivo = vm.archivo(s.nombre) ?: return
        val intent = if (compartir) CompartirSesion.intentCompartir(contexto, archivo) else CompartirSesion.intentAbrir(contexto, archivo)
        vm.avisarSinApp(!CompartirSesion.lanzar(contexto, intent))
    }
    Sesiones(
        estado,
        AccionesSesiones(
            volver = volver,
            abrir = { lanzar(it, compartir = false) },
            compartir = { lanzar(it, compartir = true) },
            pedirBorrar = { vm.pedirBorrar(it.nombre) },
            pedirBorrarTodas = vm::pedirBorrarTodas,
            confirmar = vm::confirmar,
            cancelar = vm::cancelar,
        ),
        sinApp,
    )
}

/**
 * Sesiones grabadas (maquetas/sesiones.html): la ayuda sobre el contenido, el estado de la grabación, la lista con
 * abrir, compartir y borrar, y «Borrar todas». Secundaria de Ajustes (spec `session-recording`).
 */
@Composable
fun Sesiones(estado: SesionesUiState, acciones: AccionesSesiones, sinApp: Boolean = false, modifier: Modifier = Modifier) {
    val locale = localeActual()
    Column(
        modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(start = SqTema.espacios.space4, end = SqTema.espacios.space4, top = SqTema.espacios.space4, bottom = SqTema.espacios.space8),
        verticalArrangement = Arrangement.spacedBy(SqTema.espacios.space4),
    ) {
        AppBar(stringResource(R.string.sesiones_grabadas), volver = stringResource(R.string.volver_ajustes) to acciones.volver)
        Caption(stringResource(R.string.sesiones_intro))
        SettingsSection(stringResource(R.string.sesiones_titulo_lista)) {
            if (sinApp) Notice(stringResource(R.string.sesion_sin_app), error = true)
            EstadoDeLaGrabacion(estado.grabacion)
            if (estado.vacia) {
                SettingsList(
                    listOf {
                        SettingsItemApilado {
                            Text(stringResource(R.string.sesiones_vacia), style = SqTema.tipografia.body.copy(fontWeight = FontWeight(600)))
                            Caption(stringResource(R.string.sesiones_vacia_ayuda))
                        }
                    },
                )
            } else {
                SettingsList(
                    estado.sesiones.flatMap { s ->
                        buildList<@Composable () -> Unit> {
                            add { FilaSesion(s, acciones) }
                            if (estado.confirmacion == Confirmacion.Una(s.nombre) && s.borrable) {
                                add {
                                    Confirmar(
                                        stringResource(R.string.sesion_confirmar_una, fechaSesion(s.inicio, s.nombre, locale), tamanoLegible(s.tamanoBytes, locale)),
                                        acciones,
                                    )
                                }
                            }
                        }
                    },
                )
                if (estado.confirmacion == Confirmacion.Todas) {
                    val borrables = estado.borrables
                    val tamano = tamanoLegible(borrables.sumOf { it.tamanoBytes }, locale)
                    val partes = buildList {
                        add(
                            if (borrables.size == 1) {
                                stringResource(R.string.sesion_confirmar_todas_una, tamano)
                            } else {
                                stringResource(R.string.sesion_confirmar_todas_varias, borrables.size, tamano)
                            },
                        )
                        // Solo hay una que se conserva si alguna no se puede borrar: la que se graba.
                        if (borrables.size < estado.sesiones.size) add(stringResource(R.string.sesion_confirmar_conserva))
                        add(stringResource(R.string.sesion_confirmar_deshacer))
                    }
                    Notice(partes.joinToString(" "))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(SqTema.espacios.space3)) {
                        Button(stringResource(R.string.sesion_borrar_accion), acciones.confirmar, Modifier.weight(1f), principal = true)
                        Button(stringResource(R.string.cancelar), acciones.cancelar, Modifier.weight(1f))
                    }
                } else if (estado.puedeBorrarTodas) {
                    Button(stringResource(R.string.sesiones_borrar_todas), acciones.pedirBorrarTodas, Modifier.fillMaxWidth())
                }
            }
        }
    }
}

@Composable
private fun EstadoDeLaGrabacion(grabacion: EstadoGrabacion) {
    when (grabacion) {
        EstadoGrabacion.Apagada -> Notice(stringResource(R.string.grabacion_apagada_aviso))
        is EstadoGrabacion.Grabando -> Notice(stringResource(R.string.grabacion_grabando_crece))
        is EstadoGrabacion.DetenidaPorEspacio -> Notice(stringResource(R.string.grabacion_detenida_espacio), error = true)
        is EstadoGrabacion.DetenidaPorError -> Notice(stringResource(R.string.grabacion_detenida_error), error = true)
        EstadoGrabacion.SinEspacio -> Notice(stringResource(R.string.grabacion_sin_espacio), error = true)
    }
}

@Composable
private fun FilaSesion(s: SesionGrabada, acciones: AccionesSesiones) {
    val locale = localeActual()
    val fecha = fechaSesion(s.inicio, s.nombre, locale)
    val tamano = tamanoLegible(s.tamanoBytes, locale)
    SessionRow(
        nombre = fecha,
        ayuda = when (s.estado) {
            EstadoSesion.CERRADA -> tamano
            EstadoSesion.GRABANDO -> stringResource(R.string.sesion_grabando, tamano)
            EstadoSesion.DETENIDA_POR_ESPACIO -> stringResource(R.string.sesion_detenida_espacio, tamano)
            EstadoSesion.DETENIDA_POR_ERROR -> stringResource(R.string.sesion_detenida_error, tamano)
        },
        abrir = { acciones.abrir(s) },
        compartir = { acciones.compartir(s) },
        descripcionAbrir = stringResource(R.string.sesion_abrir, fecha),
        descripcionCompartir = stringResource(R.string.sesion_compartir, fecha),
        borrar = if (s.borrable) ({ acciones.pedirBorrar(s) }) else null,
        descripcionBorrar = stringResource(R.string.sesion_borrar, fecha),
    )
}

@Composable
private fun Confirmar(texto: String, acciones: AccionesSesiones) {
    SettingsItemApilado {
        Notice(texto)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(SqTema.espacios.space3)) {
            Button(stringResource(R.string.sesion_borrar_accion), acciones.confirmar, Modifier.weight(1f), principal = true)
            Button(stringResource(R.string.cancelar), acciones.cancelar, Modifier.weight(1f))
        }
    }
}
