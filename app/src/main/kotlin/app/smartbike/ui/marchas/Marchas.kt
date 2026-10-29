package app.smartbike.ui.marchas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import app.smartbike.R
import app.smartbike.dominio.EstadoMarcha
import app.smartbike.dominio.ModoCambio
import app.smartbike.dominio.Transmision
import app.smartbike.ui.componentes.AppBar
import app.smartbike.ui.componentes.Caption
import app.smartbike.ui.componentes.CassetteView
import app.smartbike.ui.componentes.ChainringIndicator
import app.smartbike.ui.componentes.GearLadder
import app.smartbike.ui.componentes.Label
import app.smartbike.ui.componentes.SettingsItemApilado
import app.smartbike.ui.componentes.SettingsItemCasilla
import app.smartbike.ui.componentes.SettingsList
import app.smartbike.ui.componentes.SettingsOpcion
import app.smartbike.ui.componentes.SettingsSection
import app.smartbike.ui.componentes.Stepper
import app.smartbike.ui.tema.SqTema

@Composable
fun MarchasRuta(volver: () -> Unit, vm: MarchasViewModel = viewModel(factory = MarchasViewModel.Factory)) {
    val estado by vm.estado.collectAsStateWithLifecycle()
    Marchas(
        estado,
        AccionesMarchas(
            volver = volver,
            transmision = vm::transmision,
            modo = vm::modo,
            referencia = vm::referencia,
            recordarUltimaMarcha = vm::recordarUltimaMarcha,
        ),
    )
}

class AccionesMarchas(
    val volver: () -> Unit = {},
    val transmision: (String) -> Unit = {},
    val modo: (ModoCambio) -> Unit = {},
    val referencia: (dura: Boolean) -> Unit = {},
    val recordarUltimaMarcha: (Boolean) -> Unit = {},
)

/**
 * Pantalla de Marchas (maquetas/marchas.html): se abre desde Ajustes. No incluye el número de marchas de la lineal,
 * «Sensación» ni la simulación avanzada de la maqueta, que llegan con los changes 10 y 15.
 */
@Composable
fun Marchas(estado: MarchasUiState, acciones: AccionesMarchas, modifier: Modifier = Modifier) {
    Column(
        modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(start = SqTema.espacios.space4, end = SqTema.espacios.space4, top = SqTema.espacios.space4, bottom = SqTema.espacios.space8),
        verticalArrangement = Arrangement.spacedBy(SqTema.espacios.space6),
    ) {
        AppBar(stringResource(R.string.marchas), volver = stringResource(R.string.volver_ajustes) to acciones.volver)
        SettingsSection(stringResource(R.string.transmision)) {
            SettingsList(
                estado.transmisiones.map { t ->
                    {
                        SettingsOpcion(
                            nombreTransmision(t),
                            ayudaTransmision(t),
                            elegida = t.id == estado.transmision.id,
                            alElegir = { acciones.transmision(t.id) },
                        )
                    }
                },
                Modifier.selectableGroup(),
            )
        }
        Desarrollo(estado)
        val t = estado.transmision
        if (t is Transmision.Platos) {
            SettingsSection(stringResource(R.string.como_se_cambia)) {
                SettingsList(
                    listOf(
                        ModoCambio.AUTOMATICO to (R.string.modo_automatico to R.string.modo_automatico_ayuda),
                        ModoCambio.MANUAL to (R.string.modo_manual to R.string.modo_manual_ayuda),
                    ).map { (modo, textos) ->
                        {
                            SettingsOpcion(
                                stringResource(textos.first),
                                stringResource(textos.second),
                                elegida = modo == estado.modo,
                                alElegir = { acciones.modo(modo) },
                            )
                        }
                    },
                    Modifier.selectableGroup(),
                )
            }
        } else {
            Caption(stringResource(R.string.palancas_lineal))
        }
        SettingsSection(stringResource(R.string.al_empezar)) {
            SettingsList(
                listOf(
                    {
                        Stepper(
                            stringResource(R.string.marcha_referencia),
                            textoMarcha(estado.referencia),
                            "",
                            alRestar = { acciones.referencia(false) },
                            alSumar = { acciones.referencia(true) },
                            descripcionRestar = stringResource(R.string.referencia_mas_ligera),
                            descripcionSumar = stringResource(R.string.referencia_mas_dura),
                            puedeRestar = estado.puedeAligerar,
                            puedeSumar = estado.puedeEndurecer,
                            ayuda = stringResource(R.string.marcha_referencia_ayuda),
                        )
                    },
                    {
                        SettingsItemCasilla(
                            stringResource(R.string.recordar_ultima_marcha),
                            estado.recordarUltimaMarcha,
                            acciones.recordarUltimaMarcha,
                            ayuda = stringResource(R.string.recordar_ultima_marcha_ayuda),
                        )
                    },
                ),
            )
        }
    }
}

/** Con platos, los platos y el cassette con la referencia resaltada; con la lineal, la escalera. */
@Composable
private fun Desarrollo(estado: MarchasUiState) {
    val t = estado.transmision
    val ref = estado.referencia
    SettingsList(
        listOf {
            SettingsItemApilado {
                if (t is Transmision.Platos && ref is EstadoMarcha.PlatoPinon) {
                    ChainringIndicator(t.platos, ref.plato, stringResource(R.string.platos), stringResource(R.string.plato_descripcion, ref.plato), vertical = true)
                    Label(stringResource(R.string.cassette))
                    CassetteView(t.pinones, ref.pinon, stringResource(R.string.cassette_descripcion, t.pinones.first(), t.pinones.last(), ref.pinon))
                } else if (t is Transmision.Lineal && ref is EstadoMarcha.Numerada) {
                    GearLadder(t.marchas, ref.posicion, ref.posicion, "1", t.marchas.toString())
                }
                Caption(stringResource(R.string.referencia_actual, textoMarcha(ref)))
            }
        },
    )
}

@Composable
private fun nombreTransmision(t: Transmision): String = when (t) {
    is Transmision.Lineal -> stringResource(R.string.lineal)
    is Transmision.Platos -> stringResource(R.string.transmision_platos_nombre, t.nombre, t.platos.size, t.pinones.size)
}

/** «24 marchas numeradas…» o «52/36 · 11–36». */
@Composable
private fun ayudaTransmision(t: Transmision): String = when (t) {
    is Transmision.Lineal -> stringResource(R.string.lineal_ayuda, t.marchas)
    is Transmision.Platos -> t.platos.reversed().joinToString("/") + " · " + t.pinones.first() + "–" + t.pinones.last()
}

private fun textoMarcha(m: EstadoMarcha): String = when (m) {
    is EstadoMarcha.Numerada -> m.posicion.toString()
    is EstadoMarcha.PlatoPinon -> m.toString()
}
