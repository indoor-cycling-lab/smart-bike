package app.smartbike.ui.ajustes

import android.content.ActivityNotFoundException
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import app.smartbike.R
import app.smartbike.datos.EstadoGrabacion
import app.smartbike.dominio.IdiomaApp
import app.smartbike.dominio.ModoBajadas
import app.smartbike.dominio.ModoConexion
import app.smartbike.dominio.ModoVentilador
import app.smartbike.dominio.MotivoImportacion
import app.smartbike.dominio.OrientacionApp
import app.smartbike.dominio.TemaApp
import app.smartbike.dominio.TipoModelo
import app.smartbike.dominio.Transmision
import app.smartbike.ui.componentes.AppBar
import app.smartbike.ui.componentes.Button
import app.smartbike.ui.componentes.Caption
import app.smartbike.ui.componentes.Label
import app.smartbike.ui.componentes.Notice
import app.smartbike.ui.componentes.SettingsItem
import app.smartbike.ui.componentes.SettingsItemCasilla
import app.smartbike.ui.componentes.SettingsList
import app.smartbike.ui.componentes.SettingsOpcion
import app.smartbike.ui.componentes.SettingsSection
import app.smartbike.ui.componentes.Stepper
import app.smartbike.ui.datos.resumenDatos
import app.smartbike.ui.diagnostico.decimal
import app.smartbike.ui.navegacion.PantallaPestana
import app.smartbike.ui.navegacion.Pestana
import app.smartbike.ui.nombreModoVentilador
import app.smartbike.ui.sesiones.tamanoLegible
import app.smartbike.ui.tema.SqTema
import app.smartbike.ui.tema.localeActual
import app.smartbike.ui.textoTransmision
import java.time.LocalDate

/** Acciones de Ajustes. */
class AccionesAjustes(
    val irA: (Pestana) -> Unit = {},
    val abrirMarchas: () -> Unit = {},
    val abrirBotones: () -> Unit = {},
    val abrirDiagnostico: () -> Unit = {},
    /** «Guía de inicio»: vuelve a mostrar la guía y abre Rodaje con la hoja en la página 1. */
    val abrirGuia: () -> Unit = {},
    val abrirDatos: () -> Unit = {},
    val abrirTabla: () -> Unit = {},
    val abrirSesiones: () -> Unit = {},
    val grabarSesiones: (Boolean) -> Unit = {},
    val espacioLibreMinimo: (Int) -> Unit = {},
    val modelo: (TipoModelo) -> Unit = {},
    val dificultad: (Int) -> Unit = {},
    val potenciaMinima: (Int) -> Unit = {},
    val pesoBici: (Int) -> Unit = {},
    val bajadas: (ModoBajadas) -> Unit = {},
    val botonesEnPantalla: (Boolean) -> Unit = {},
    val modoTactil: (Boolean) -> Unit = {},
    val mostrarAccion: (Boolean) -> Unit = {},
    val vibracion: (Boolean) -> Unit = {},
    val peso: (Int) -> Unit = {},
    val idioma: (IdiomaApp) -> Unit = {},
    val tema: (TemaApp) -> Unit = {},
    val orientacion: (OrientacionApp) -> Unit = {},
    val ventiladorModo: (ModoVentilador) -> Unit = {},
    val ventiladorVelocidad: (Int) -> Unit = {},
    val ventiladorUmbral: (Int, Int) -> Unit = { _, _ -> },
    val exportar: () -> Unit = {},
    val importar: () -> Unit = {},
    val confirmarImportacion: () -> Unit = {},
    val cancelarImportacion: () -> Unit = {},
)

@Composable
fun AjustesRuta(
    irA: (Pestana) -> Unit,
    abrirBotones: () -> Unit,
    abrirMarchas: () -> Unit,
    abrirDiagnostico: () -> Unit,
    abrirDatos: () -> Unit,
    abrirTabla: () -> Unit,
    abrirSesiones: () -> Unit,
    vm: AjustesViewModel = viewModel(factory = AjustesViewModel.Factory),
) {
    val estado by vm.estado.collectAsStateWithLifecycle()
    // El selector de archivos del sistema, sin permisos (spec `settings-backup`): cerrarlo sin elegir no hace nada.
    val crear = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { vm.exportar(it?.toString()) }
    val abrir = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { vm.elegirArchivo(it?.toString()) }
    // Los mensajes de la copia no sobreviven a salir de Ajustes, pero sí a girar el móvil (el ViewModel sigue).
    val actividad = LocalActivity.current
    DisposableEffect(Unit) { onDispose { if (actividad?.isChangingConfigurations != true) vm.descartarCopia() } }
    Ajustes(
        estado,
        AccionesAjustes(
            irA = irA,
            abrirBotones = abrirBotones,
            abrirMarchas = abrirMarchas,
            abrirDiagnostico = abrirDiagnostico,
            abrirGuia = {
                vm.reabrirGuia()
                irA(Pestana.Rodar)
            },
            abrirDatos = abrirDatos,
            abrirTabla = abrirTabla,
            abrirSesiones = abrirSesiones,
            grabarSesiones = vm::grabarSesiones,
            espacioLibreMinimo = vm::espacioLibreMinimo,
            modelo = vm::modelo,
            dificultad = vm::dificultad,
            potenciaMinima = vm::potenciaMinima,
            pesoBici = vm::pesoBici,
            bajadas = vm::bajadas,
            botonesEnPantalla = vm::botonesEnPantalla,
            modoTactil = vm::modoTactil,
            mostrarAccion = vm::mostrarAccion,
            vibracion = vm::vibracion,
            peso = vm::peso,
            idioma = vm::idioma,
            tema = vm::tema,
            orientacion = vm::orientacion,
            ventiladorModo = vm::ventiladorModo,
            ventiladorVelocidad = vm::ventiladorVelocidad,
            ventiladorUmbral = vm::ventiladorUmbral,
            exportar = {
                try {
                    crear.launch("smart-bike-configuracion-${LocalDate.now()}.json")
                } catch (_: ActivityNotFoundException) {
                    vm.errorSelector()
                }
            },
            importar = {
                try {
                    abrir.launch(arrayOf("application/json", "text/plain"))
                } catch (_: ActivityNotFoundException) {
                    vm.errorSelector()
                }
            },
            confirmarImportacion = vm::confirmarImportacion,
            cancelarImportacion = vm::descartarCopia,
        ),
    )
}

/**
 * Pantalla de Ajustes (maquetas/ajustes.html): una pestaña con las entradas Marchas, Botones y Diagnóstico y las secciones
 * Al rodar, Pantalla, Ciclista, Rodillo y Copia de seguridad (spec `settings-screen`, «Ajustes como pestaña»). Lo que la app
 * publica ante Zwift está en Plataformas.
 */
@Composable
fun Ajustes(estado: AjustesUiState, acciones: AccionesAjustes, modifier: Modifier = Modifier) {
    val chevron: @Composable () -> Unit = {
        Icon(painterResource(R.drawable.ic_chevron_right), contentDescription = null, tint = SqTema.colores.inkMuted)
    }
    PantallaPestana(Pestana.Ajustes, acciones.irA, modifier) {
        AppBar(stringResource(R.string.ajustes))
        SettingsList(
            listOf(
                {
                    SettingsItem(
                        stringResource(R.string.marchas),
                        Modifier.clickable(role = Role.Button, onClick = acciones.abrirMarchas),
                        icono = R.drawable.ic_settings,
                        ayuda = textoTransmision(estado.transmision, estado.modoCambio),
                        final = chevron,
                    )
                },
                {
                    SettingsItem(
                        stringResource(R.string.botones),
                        Modifier.clickable(role = Role.Button, onClick = acciones.abrirBotones),
                        icono = R.drawable.ic_botones,
                        ayuda = when (val t = estado.transmision) {
                            is Transmision.Lineal -> stringResource(R.string.botones_entrada_lineal)
                            is Transmision.Platos -> stringResource(R.string.botones_entrada, t.nombre)
                        },
                        final = chevron,
                    )
                },
                {
                    SettingsItem(
                        stringResource(R.string.guia_nombre),
                        Modifier.clickable(role = Role.Button, onClick = acciones.abrirGuia),
                        icono = R.drawable.ic_info,
                        ayuda = stringResource(R.string.guia_ajustes_ayuda),
                        final = chevron,
                    )
                },
                {
                    SettingsItem(
                        stringResource(R.string.diagnostico),
                        Modifier.clickable(role = Role.Button, onClick = acciones.abrirDiagnostico),
                        icono = R.drawable.ic_monitor_heart,
                        ayuda = stringResource(R.string.diagnostico_ayuda),
                        final = chevron,
                    )
                },
            ),
        )
        SettingsSection(stringResource(R.string.al_rodar)) {
            SettingsList(
                listOf(
                    {
                        SettingsItem(
                            stringResource(R.string.datos_en_pantalla),
                            Modifier.clickable(role = Role.Button, onClick = acciones.abrirDatos),
                            ayuda = resumenDatos(estado.datos),
                            ayudaUnaLinea = true,
                            final = chevron,
                        )
                    },
                    {
                        SettingsItemCasilla(
                            stringResource(R.string.botones_en_pantalla),
                            estado.botonesEnPantalla,
                            acciones.botonesEnPantalla,
                            ayuda = stringResource(R.string.botones_en_pantalla_ayuda),
                        )
                    },
                    {
                        SettingsItemCasilla(
                            stringResource(R.string.modo_tactil),
                            estado.modoTactil,
                            acciones.modoTactil,
                            ayuda = stringResource(R.string.modo_tactil_ayuda),
                        )
                    },
                    {
                        SettingsItemCasilla(
                            stringResource(R.string.mostrar_accion),
                            estado.mostrarAccion,
                            acciones.mostrarAccion,
                            ayuda = stringResource(R.string.mostrar_accion_ayuda),
                        )
                    },
                    {
                        SettingsItemCasilla(
                            stringResource(R.string.vibrar),
                            estado.vibracion,
                            acciones.vibracion,
                            ayuda = stringResource(R.string.vibrar_ayuda),
                        )
                    },
                ),
            )
        }
        SettingsSection(stringResource(R.string.pantalla)) {
            Label(stringResource(R.string.idioma))
            SettingsList(
                IdiomaApp.entries.map { idioma ->
                    {
                        SettingsOpcion(
                            stringResource(nombreIdioma(idioma)),
                            ayuda = null,
                            elegida = idioma == estado.idioma,
                            alElegir = { acciones.idioma(idioma) },
                        )
                    }
                },
                Modifier.selectableGroup(),
            )
            Label(stringResource(R.string.tema))
            SettingsList(
                TemaApp.entries.map { tema ->
                    {
                        SettingsOpcion(
                            stringResource(nombreTema(tema)),
                            stringResource(ayudaTema(tema)),
                            elegida = tema == estado.tema,
                            alElegir = { acciones.tema(tema) },
                        )
                    }
                },
                Modifier.selectableGroup(),
            )
            Label(stringResource(R.string.orientacion))
            SettingsList(
                OrientacionApp.entries.map { orientacion ->
                    {
                        SettingsOpcion(
                            stringResource(nombreOrientacion(orientacion)),
                            stringResource(ayudaOrientacion(orientacion)),
                            elegida = orientacion == estado.orientacion,
                            alElegir = { acciones.orientacion(orientacion) },
                        )
                    }
                },
                Modifier.selectableGroup(),
            )
        }
        SettingsSection(stringResource(R.string.ciclista)) {
            SettingsList(
                listOf({
                    Stepper(
                        stringResource(R.string.peso_ciclista),
                        estado.pesoCiclistaKg.toString(),
                        "kg",
                        alRestar = { acciones.peso(-1) },
                        alSumar = { acciones.peso(1) },
                        descripcionRestar = stringResource(R.string.bajar_peso_ciclista),
                        descripcionSumar = stringResource(R.string.subir_peso_ciclista),
                        puedeRestar = estado.puedeBajarPeso,
                        puedeSumar = estado.puedeSubirPeso,
                        ayuda = stringResource(R.string.peso_ciclista_ayuda),
                    )
                }, {
                    Stepper(
                        stringResource(R.string.peso_bici),
                        decimal(estado.pesoBiciKg, localeActual()),
                        "kg",
                        alRestar = { acciones.pesoBici(-1) },
                        alSumar = { acciones.pesoBici(1) },
                        descripcionRestar = stringResource(R.string.bajar_peso_bici),
                        descripcionSumar = stringResource(R.string.subir_peso_bici),
                        puedeRestar = estado.puedeBajarPesoBici,
                        puedeSumar = estado.puedeSubirPesoBici,
                        ayuda = stringResource(R.string.peso_bici_ayuda),
                    )
                }),
            )
        }
        SettingsSection(stringResource(R.string.rodillo)) {
            Label(stringResource(R.string.modelo_resistencia))
            SettingsList(
                TipoModelo.entries.map { modelo ->
                    {
                        SettingsOpcion(
                            stringResource(nombreModelo(modelo)),
                            stringResource(ayudaModelo(modelo)),
                            elegida = modelo == estado.modelo,
                            alElegir = { acciones.modelo(modelo) },
                        )
                    }
                },
                Modifier.selectableGroup(),
            )
            if (estado.muestraTabla) {
                SettingsList(
                    listOf {
                        SettingsItem(
                            stringResource(R.string.tabla_marchas),
                            Modifier.clickable(role = Role.Button, onClick = acciones.abrirTabla),
                            ayuda = textoTransmision(estado.transmision, estado.modoCambio),
                            final = chevron,
                        )
                    },
                    Modifier.padding(top = SqTema.espacios.space2),
                )
            }
            SettingsList(
                listOf {
                    Stepper(
                        stringResource(R.string.dificultad),
                        estado.dificultadPct.toString(),
                        "%",
                        alRestar = { acciones.dificultad(-1) },
                        alSumar = { acciones.dificultad(1) },
                        descripcionRestar = stringResource(R.string.bajar_dificultad),
                        descripcionSumar = stringResource(R.string.subir_dificultad),
                        puedeRestar = estado.puedeBajarDificultad,
                        puedeSumar = estado.puedeSubirDificultad,
                        ayuda = stringResource(R.string.dificultad_ayuda),
                    )
                },
                Modifier.padding(top = SqTema.espacios.space2),
            )
            if (estado.muestraPotenciaMinima) {
                SettingsList(
                    listOf {
                        Stepper(
                            stringResource(R.string.potencia_minima),
                            estado.potenciaMinimaW.toString(),
                            "W",
                            alRestar = { acciones.potenciaMinima(-1) },
                            alSumar = { acciones.potenciaMinima(1) },
                            descripcionRestar = stringResource(R.string.bajar_potencia_minima),
                            descripcionSumar = stringResource(R.string.subir_potencia_minima),
                            puedeRestar = estado.puedeBajarMinima,
                            puedeSumar = estado.puedeSubirMinima,
                            ayuda = stringResource(R.string.potencia_minima_ayuda),
                        )
                    },
                    Modifier.padding(top = SqTema.espacios.space2),
                )
            }
            if (estado.muestraBajadas) {
                Label(stringResource(R.string.bajadas), Modifier.padding(top = SqTema.espacios.space2))
                Caption(stringResource(R.string.bajadas_ayuda))
                SettingsList(
                    ModoBajadas.entries.map { modo ->
                        {
                            SettingsOpcion(
                                stringResource(nombreBajadas(modo)),
                                stringResource(ayudaBajadas(modo)),
                                elegida = modo == estado.bajadas,
                                alElegir = { acciones.bajadas(modo) },
                            )
                        }
                    },
                    Modifier.selectableGroup(),
                )
            }
        }
        estado.ventilador?.let { SeccionVentilador(it, acciones) }
        SettingsSection(stringResource(R.string.copia_seguridad)) {
            when (val copia = estado.copia) {
                Copia.Ninguna -> Unit

                Copia.Exportada -> Notice(stringResource(R.string.copia_exportada))

                Copia.Importada -> Notice(stringResource(R.string.copia_importada))

                is Copia.Pendiente -> {
                    Notice(stringResource(R.string.copia_confirmar))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(SqTema.espacios.space3)) {
                        Button(stringResource(R.string.importar), acciones.confirmarImportacion, Modifier.weight(1f), principal = true)
                        Button(stringResource(R.string.cancelar), acciones.cancelarImportacion, Modifier.weight(1f))
                    }
                }

                Copia.ErrorExportar -> Notice(stringResource(R.string.copia_error_exportar), error = true)

                Copia.ErrorSelector -> Notice(stringResource(R.string.copia_error_selector), error = true)

                is Copia.ErrorImportar -> Notice(stringResource(textoErrorImportar(copia.motivo)), error = true)
            }
            SettingsList(
                listOf(
                    {
                        SettingsItem(
                            stringResource(R.string.exportar_configuracion),
                            Modifier.clickable(role = Role.Button, onClick = acciones.exportar),
                            ayuda = stringResource(R.string.exportar_configuracion_ayuda),
                            final = chevron,
                        )
                    },
                    {
                        SettingsItem(
                            stringResource(R.string.importar_configuracion),
                            Modifier.clickable(role = Role.Button, onClick = acciones.importar),
                            ayuda = stringResource(R.string.importar_configuracion_ayuda),
                            final = chevron,
                        )
                    },
                ),
            )
        }
        SeccionDepuracion(estado, acciones, chevron)
    }
}

/** «Depuración» (maquetas/ajustes.html; spec `settings-screen`): grabar sesiones, su espacio libre mínimo y «Sesiones grabadas». */
@Composable
private fun SeccionDepuracion(estado: AjustesUiState, acciones: AccionesAjustes, chevron: @Composable () -> Unit) {
    SettingsSection(stringResource(R.string.depuracion)) {
        when (estado.grabacion) {
            EstadoGrabacion.Apagada -> Unit
            is EstadoGrabacion.Grabando -> Caption(stringResource(R.string.grabacion_grabando))
            is EstadoGrabacion.DetenidaPorEspacio -> Notice(stringResource(R.string.grabacion_detenida_espacio), error = true)
            is EstadoGrabacion.DetenidaPorError -> Notice(stringResource(R.string.grabacion_detenida_error), error = true)
            EstadoGrabacion.SinEspacio -> Notice(stringResource(R.string.grabacion_sin_espacio), error = true)
        }
        SettingsList(
            listOf(
                {
                    SettingsItemCasilla(
                        stringResource(R.string.grabar_sesiones),
                        estado.grabarSesiones,
                        acciones.grabarSesiones,
                        ayuda = stringResource(R.string.grabar_sesiones_ayuda),
                    )
                },
                {
                    Stepper(
                        stringResource(R.string.espacio_libre_minimo),
                        estado.espacioLibreMinimoPct.toString(),
                        "%",
                        alRestar = { acciones.espacioLibreMinimo(-1) },
                        alSumar = { acciones.espacioLibreMinimo(1) },
                        descripcionRestar = stringResource(R.string.bajar_espacio_libre_minimo),
                        descripcionSumar = stringResource(R.string.subir_espacio_libre_minimo),
                        puedeRestar = estado.puedeBajarEspacioLibre,
                        puedeSumar = estado.puedeSubirEspacioLibre,
                        ayuda = stringResource(R.string.espacio_libre_minimo_ayuda),
                    )
                },
                {
                    SettingsItem(
                        stringResource(R.string.sesiones_grabadas),
                        Modifier.clickable(role = Role.Button, onClick = acciones.abrirSesiones),
                        ayuda = resumenSesiones(estado),
                        final = chevron,
                    )
                },
            ),
        )
    }
}

@Composable
private fun resumenSesiones(estado: AjustesUiState): String = when (val n = estado.sesiones.cantidad) {
    0 -> stringResource(R.string.sesiones_resumen_ninguna)
    1 -> stringResource(R.string.sesiones_resumen_una, tamanoLegible(estado.sesiones.tamanoBytes, localeActual()))
    else -> stringResource(R.string.sesiones_resumen_varias, n, tamanoLegible(estado.sesiones.tamanoBytes, localeActual()))
}

@StringRes
fun textoErrorImportar(motivo: MotivoImportacion): Int = when (motivo) {
    MotivoImportacion.VACIO, MotivoImportacion.NO_SE_PUDO_LEER -> R.string.copia_error_lectura
    MotivoImportacion.NO_ES_JSON, MotivoImportacion.NO_ES_CONFIGURACION -> R.string.copia_error_archivo
    MotivoImportacion.VERSION_MAS_NUEVA -> R.string.copia_error_version
}

@StringRes
fun nombreModelo(modelo: TipoModelo): Int = when (modelo) {
    TipoModelo.SIMPLE -> R.string.modelo_simple
    TipoModelo.TABLA -> R.string.modelo_tabla
    TipoModelo.FISICO -> R.string.modelo_fisico
}

@StringRes
private fun ayudaModelo(modelo: TipoModelo): Int = when (modelo) {
    TipoModelo.SIMPLE -> R.string.modelo_simple_ayuda
    TipoModelo.TABLA -> R.string.modelo_tabla_ayuda
    TipoModelo.FISICO -> R.string.modelo_fisico_ayuda
}

@StringRes
fun nombreBajadas(modo: ModoBajadas): Int = when (modo) {
    ModoBajadas.SIN_PROTECCION -> R.string.bajadas_sin_proteccion
    ModoBajadas.LA_MARCHA_NO_CREA_BAJADAS -> R.string.bajadas_la_marcha_no_crea
    ModoBajadas.NUNCA_EMPUJAR -> R.string.bajadas_nunca_empujar
}

@StringRes
private fun ayudaBajadas(modo: ModoBajadas): Int = when (modo) {
    ModoBajadas.SIN_PROTECCION -> R.string.bajadas_sin_proteccion_ayuda
    ModoBajadas.LA_MARCHA_NO_CREA_BAJADAS -> R.string.bajadas_la_marcha_no_crea_ayuda
    ModoBajadas.NUNCA_EMPUJAR -> R.string.bajadas_nunca_empujar_ayuda
}

/** Cada idioma, escrito siempre en el suyo: se reconoce aunque la app esté en el otro (spec `settings-screen`, «Opciones en su propio idioma»). */
@StringRes
private fun nombreIdioma(idioma: IdiomaApp): Int = when (idioma) {
    IdiomaApp.ES -> R.string.idioma_espanol
    IdiomaApp.EN -> R.string.idioma_english
}

@StringRes
private fun nombreTema(tema: TemaApp): Int = when (tema) {
    TemaApp.OSCURO -> R.string.tema_oscuro
    TemaApp.CLARO -> R.string.tema_claro
}

@StringRes
private fun ayudaTema(tema: TemaApp): Int = when (tema) {
    TemaApp.OSCURO -> R.string.tema_oscuro_ayuda
    TemaApp.CLARO -> R.string.tema_claro_ayuda
}

@StringRes
private fun nombreOrientacion(orientacion: OrientacionApp): Int = when (orientacion) {
    OrientacionApp.SEGUN_MOVIL -> R.string.orientacion_movil
    OrientacionApp.VERTICAL -> R.string.orientacion_vertical
    OrientacionApp.HORIZONTAL -> R.string.orientacion_horizontal
}

@StringRes
private fun ayudaOrientacion(orientacion: OrientacionApp): Int = when (orientacion) {
    OrientacionApp.SEGUN_MOVIL -> R.string.orientacion_movil_ayuda
    OrientacionApp.VERTICAL -> R.string.orientacion_vertical_ayuda
    OrientacionApp.HORIZONTAL -> R.string.orientacion_horizontal_ayuda
}

/** «Ventilador» (maquetas/ajustes.html; spec `settings-screen`): modo, velocidad manual y los cuatro umbrales de pulso. */
@Composable
private fun SeccionVentilador(v: VentiladorAjustesUi, acciones: AccionesAjustes) {
    SettingsSection(stringResource(R.string.ventilador_seccion)) {
        if (!v.conectado) Notice(stringResource(R.string.ventilador_sin_conexion_aviso))
        Label(stringResource(R.string.ventilador_modo))
        val modos = buildList {
            add(ModoVentilador.Manual to R.string.ventilador_modo_manual_ayuda)
            add(ModoVentilador.Pulso to R.string.ventilador_modo_automatico_ayuda)
            if (v.modo == ModoVentilador.Otro) add(ModoVentilador.Otro to R.string.ventilador_modo_otro_ayuda)
        }
        SettingsList(
            modos.map { (modo, ayuda) ->
                {
                    SettingsOpcion(
                        stringResource(nombreModoVentilador(modo)),
                        stringResource(ayuda),
                        elegida = modo == v.modo,
                        alElegir = { acciones.ventiladorModo(modo) },
                        habilitada = v.puedeCambiarModo && modo != ModoVentilador.Otro,
                    )
                }
            },
            Modifier.selectableGroup(),
        )
        SettingsList(
            listOf {
                Stepper(
                    stringResource(R.string.ventilador_velocidad),
                    v.velocidad.toString(),
                    "%",
                    alRestar = { acciones.ventiladorVelocidad(-1) },
                    alSumar = { acciones.ventiladorVelocidad(1) },
                    descripcionRestar = stringResource(R.string.bajar_velocidad_ventilador),
                    descripcionSumar = stringResource(R.string.subir_velocidad_ventilador),
                    puedeRestar = v.puedeBajarVelocidad,
                    puedeSumar = v.puedeSubirVelocidad,
                    ayuda = stringResource(
                        when (v.modo) {
                            ModoVentilador.Manual -> R.string.ventilador_velocidad_ayuda
                            ModoVentilador.Pulso -> R.string.ventilador_velocidad_automatico
                            ModoVentilador.Otro -> R.string.ventilador_velocidad_otro_modo
                        },
                    ),
                )
            },
            Modifier.padding(top = SqTema.espacios.space2),
        )
        v.umbrales?.let { umbrales ->
            Caption(stringResource(R.string.ventilador_umbrales_ayuda))
            SettingsList(
                umbrales.mapIndexed { i, bpm ->
                    {
                        val zona = i + 1
                        Stepper(
                            stringResource(R.string.ventilador_zona, zona),
                            bpm.toString(),
                            "bpm",
                            alRestar = { acciones.ventiladorUmbral(zona, -1) },
                            alSumar = { acciones.ventiladorUmbral(zona, 1) },
                            descripcionRestar = stringResource(R.string.bajar_umbral_zona, zona),
                            descripcionSumar = stringResource(R.string.subir_umbral_zona, zona),
                            puedeRestar = v.puedeBajarUmbral(zona),
                            puedeSumar = v.puedeSubirUmbral(zona),
                            ayuda = stringResource(R.string.ventilador_desde),
                        )
                    }
                },
            )
        }
    }
}
