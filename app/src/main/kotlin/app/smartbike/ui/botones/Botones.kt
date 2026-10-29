package app.smartbike.ui.botones

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import app.smartbike.R
import app.smartbike.dominio.Accion
import app.smartbike.dominio.Transmision
import app.smartbike.ui.componentes.AppBar
import app.smartbike.ui.componentes.Button
import app.smartbike.ui.componentes.ButtonMappingRow
import app.smartbike.ui.componentes.Capa
import app.smartbike.ui.componentes.Caption
import app.smartbike.ui.componentes.EntradaUi
import app.smartbike.ui.componentes.Notice
import app.smartbike.ui.componentes.SettingsItemCasilla
import app.smartbike.ui.componentes.SettingsList
import app.smartbike.ui.componentes.SettingsSection
import app.smartbike.ui.componentes.Stepper
import app.smartbike.ui.nombreAccion
import app.smartbike.ui.teclasDe
import app.smartbike.ui.tema.SqTema
import app.smartbike.ui.textoGesto

/** Acciones de la pantalla de Botones. */
class AccionesBotones(
    val volver: () -> Unit = {},
    val asignar: (Accion) -> Unit = {},
    val invertirPalancas: (Boolean) -> Unit = {},
    val autorrepeticion: (Boolean) -> Unit = {},
    val tiempo: (Tiempo, Int) -> Unit = { _, _ -> },
    val pedirRestaurar: () -> Unit = {},
    val cancelarRestaurar: () -> Unit = {},
    val restaurar: () -> Unit = {},
)

@Composable
fun BotonesRuta(volver: () -> Unit, asignar: (Accion) -> Unit, vm: BotonesViewModel = viewModel(factory = BotonesViewModel.Factory)) {
    val estado by vm.estado.collectAsStateWithLifecycle()
    Botones(
        estado,
        AccionesBotones(
            volver = volver,
            asignar = asignar,
            invertirPalancas = vm::invertirPalancas,
            autorrepeticion = vm::autorrepeticion,
            tiempo = vm::tiempo,
            pedirRestaurar = vm::pedirRestaurar,
            cancelarRestaurar = vm::cancelarRestaurar,
            restaurar = vm::restaurar,
        ),
    )
}

/** Pantalla de Botones (maquetas/botones.html): se abre desde Ajustes, con botón de volver y sin barra inferior. */
@Composable
fun Botones(estado: BotonesUiState, acciones: AccionesBotones, modifier: Modifier = Modifier) {
    Column(
        modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(start = SqTema.espacios.space4, end = SqTema.espacios.space4, top = SqTema.espacios.space4, bottom = SqTema.espacios.space8),
        verticalArrangement = Arrangement.spacedBy(SqTema.espacios.space6),
    ) {
        AppBar(stringResource(R.string.botones), volver = stringResource(R.string.volver_ajustes) to acciones.volver)
        AccionesApp(estado, acciones)
        FuncionesZwift(estado, acciones)
        Gestos(estado, acciones)
        Restaurar(estado, acciones)
    }
}

/**
 * Las funciones de Zwift, asignables como las acciones de la app; sin las palancas, que cambian las marchas de la app
 * (design.md del change `mando-zwift-emulado`, decisión 9).
 */
@Composable
private fun FuncionesZwift(estado: BotonesUiState, acciones: AccionesBotones) {
    val ayuda = stringResource(
        when (estado.mando) {
            MandoBotones.Aparte -> R.string.funciones_zwift_ayuda
            MandoBotones.Dentro -> R.string.funciones_zwift_dentro
            MandoBotones.SinSquare -> R.string.funciones_zwift_sin_mando
        },
    )
    SettingsSection(stringResource(R.string.funciones_zwift), ayuda = ayuda) {
        SettingsList(estado.funcionesZwift.map { fila -> { FilaAsignable(fila, Capa.Zwift, acciones) } })
    }
}

/** Una acción o función con sus entradas; tocarla abre Asignar acción. */
@Composable
private fun FilaAsignable(fila: FilaAccion, capa: Capa, acciones: AccionesBotones) {
    ButtonMappingRow(
        nombreAccion(fila.accion),
        fila.reglas.map { EntradaUi(teclasDe(it.entrada), textoGesto(it.gesto)) },
        capa,
        sinBoton = stringResource(R.string.sin_boton),
        alTocar = { acciones.asignar(fila.accion) },
    )
}

@Composable
private fun AccionesApp(estado: BotonesUiState, acciones: AccionesBotones) {
    val deTransmision = when (val t = estado.transmision) {
        is Transmision.Lineal -> stringResource(R.string.asignacion_de_lineal)
        is Transmision.Platos -> stringResource(R.string.asignacion_de, t.nombre)
    }
    SettingsSection(stringResource(R.string.acciones_app), ayuda = deTransmision) {
        Caption(stringResource(R.string.acciones_app_ayuda))
        SettingsList(estado.acciones.map { fila -> { FilaAsignable(fila, Capa.App, acciones) } })
        SettingsList(
            listOf {
                SettingsItemCasilla(
                    stringResource(R.string.invertir_palancas),
                    estado.invertirPalancas,
                    acciones.invertirPalancas,
                    ayuda = stringResource(R.string.invertir_palancas_ayuda),
                )
            },
        )
    }
}

@Composable
private fun Gestos(estado: BotonesUiState, acciones: AccionesBotones) {
    SettingsSection(stringResource(R.string.gestos), ayuda = stringResource(R.string.gestos_ayuda)) {
        val filas = mutableListOf<@Composable () -> Unit>({
            SettingsItemCasilla(
                stringResource(R.string.repetir_al_mantener),
                estado.autorrepeticion,
                acciones.autorrepeticion,
                ayuda = stringResource(R.string.repetir_al_mantener_ayuda),
            )
        })
        listOf(
            Triple(Tiempo.Larga, R.string.tiempo_larga, R.string.tiempo_larga_ayuda),
            Triple(Tiempo.Doble, R.string.tiempo_doble, R.string.tiempo_doble_ayuda),
            Triple(Tiempo.Retardo, R.string.tiempo_retardo, R.string.tiempo_retardo_ayuda),
            Triple(Tiempo.Ritmo, R.string.tiempo_ritmo, R.string.tiempo_ritmo_ayuda),
        ).forEach { (t, nombre, ayuda) ->
            filas += {
                val n = stringResource(nombre)
                Stepper(
                    n,
                    t.de(estado.tiempos).toString(),
                    stringResource(R.string.unidad_ms),
                    alRestar = { acciones.tiempo(t, -1) },
                    alSumar = { acciones.tiempo(t, 1) },
                    descripcionRestar = stringResource(R.string.bajar_tiempo, n.lowercase()),
                    descripcionSumar = stringResource(R.string.subir_tiempo, n.lowercase()),
                    puedeRestar = estado.puedeBajar(t),
                    puedeSumar = estado.puedeSubir(t),
                    ayuda = stringResource(ayuda),
                )
            }
        }
        SettingsList(filas)
    }
}

@Composable
private fun Restaurar(estado: BotonesUiState, acciones: AccionesBotones) {
    if (!estado.confirmandoRestaurar) {
        Button(stringResource(R.string.restaurar_defecto), acciones.pedirRestaurar, Modifier.fillMaxWidth())
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(SqTema.espacios.space4)) {
        Notice(stringResource(R.string.restaurar_aviso))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(SqTema.espacios.space3)) {
            Button(stringResource(R.string.cancelar), acciones.cancelarRestaurar, Modifier.weight(1f))
            Button(stringResource(R.string.restaurar), acciones.restaurar, Modifier.weight(1f), principal = true)
        }
    }
}
