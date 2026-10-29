package app.smartbike.ui.diagnostico

import android.os.SystemClock
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import app.smartbike.R
import app.smartbike.dominio.ComandoRodillo
import app.smartbike.dominio.Enlace as EnlaceConexion
import app.smartbike.dominio.EstadoMarcha
import app.smartbike.dominio.ModoConexion
import app.smartbike.dominio.NombreAnunciado
import app.smartbike.dominio.TipoModelo
import app.smartbike.plataforma.EstadoMando
import app.smartbike.plataforma.EstadoPlataforma
import app.smartbike.protocolo.FtmsCodec
import app.smartbike.protocolo.SquareButton
import app.smartbike.sesion.Envio
import app.smartbike.sesion.EstadoDispositivo
import app.smartbike.ui.ajustes.nombreBajadas
import app.smartbike.ui.ajustes.nombreModelo
import app.smartbike.ui.componentes.AppBar
import app.smartbike.ui.componentes.Button
import app.smartbike.ui.componentes.Caption
import app.smartbike.ui.componentes.CombinacionSquare
import app.smartbike.ui.componentes.ConnectionChip
import app.smartbike.ui.componentes.Dato
import app.smartbike.ui.componentes.Enlace
import app.smartbike.ui.componentes.EstadoChip
import app.smartbike.ui.componentes.HexLog
import app.smartbike.ui.componentes.Label
import app.smartbike.ui.componentes.MetricTile
import app.smartbike.ui.componentes.Notice
import app.smartbike.ui.componentes.SettingsItem
import app.smartbike.ui.componentes.SettingsItemApilado
import app.smartbike.ui.componentes.SettingsItemCasilla
import app.smartbike.ui.componentes.SettingsList
import app.smartbike.ui.componentes.SettingsSection
import app.smartbike.ui.componentes.SquareKey
import app.smartbike.ui.componentes.Stepper
import app.smartbike.ui.componentes.etiquetaIcono
import app.smartbike.ui.dispositivos.EstadoMandoChip
import app.smartbike.ui.dispositivos.textoMovil
import app.smartbike.ui.nombreCorto
import app.smartbike.ui.nombreModoVentilador
import app.smartbike.ui.tema.SqTema
import app.smartbike.ui.tema.localeActual
import app.smartbike.ui.textoAccion
import app.smartbike.ui.textoGesto
import app.smartbike.ui.textoTope
import app.smartbike.ui.textoVatios
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.delay

/** Acciones de la pantalla; las que dependen de Android (permisos, ajustes) las resuelve [DiagnosticoRuta]. */
class AccionesDiagnostico(
    val pendiente: (Int) -> Unit = {},
    val potencia: (Int) -> Unit = {},
    val enviarPendiente: () -> Unit = {},
    val enviarPotencia: () -> Unit = {},
    val alternarTrafico: () -> Unit = {},
    val alternarPausa: () -> Unit = {},
    val limpiar: () -> Unit = {},
    val volver: () -> Unit = {},
)

/** Pantalla de Diagnóstico (maquetas/diagnostico.html), sin estado propio. Secundaria de Ajustes (spec `diagnostics`). */
@Composable
fun Diagnostico(estado: DiagnosticoUiState, acciones: AccionesDiagnostico, modifier: Modifier = Modifier) {
    Column(
        modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(start = SqTema.espacios.space4, end = SqTema.espacios.space4, top = SqTema.espacios.space4, bottom = SqTema.espacios.space8),
        verticalArrangement = Arrangement.spacedBy(SqTema.espacios.space6),
    ) {
        AppBar(stringResource(R.string.diagnostico), volver = stringResource(R.string.volver_ajustes) to acciones.volver)
        DatosConexion(estado)
        Botones(estado)
        Simulacion(estado.simulacion, estado.nombrePlataforma, estado.simulacionZwift)
        Trafico(estado, acciones)
        ControlManual(estado, acciones)
    }
}

/**
 * Datos de conexión (spec `diagnostics`): solo lo que no se ve en Conexión ni en Dispositivos, un dato por fila y sin el
 * estado de ninguna conexión, sin avisos ni acciones de búsqueda.
 */
@Composable
private fun DatosConexion(estado: DiagnosticoUiState) {
    SettingsSection(stringResource(R.string.datos_conexion), ayuda = stringResource(R.string.datos_conexion_ayuda)) {
        SettingsList(
            buildList<@Composable () -> Unit> {
                val porBluetooth = estado.enlace == EnlaceConexion.BLUETOOTH
                add { FilaAppEntrenamiento(estado.plataforma, porBluetooth) }
                add { FilaNombreAnunciado(if (porBluetooth) estado.nombreMovil ?: estado.nombreAnunciado else estado.nombreAnunciado, porBluetooth) }
                estado.bateriaSquare?.let { b -> add { FilaBateriaSquare(b) } }
                estado.pulsometro?.let { p -> add { FilaPulso(p) } }
                estado.ventilador?.let { v -> add { FilaVentilador(v) } }
            },
        )
    }
}

/** La dirección desde la que se ha conectado la app de entrenamiento, sea cual sea, o «—» si no hay ninguna. */
@Composable
private fun FilaAppEntrenamiento(z: EstadoPlataforma, porBluetooth: Boolean) {
    val dato = if (z is EstadoPlataforma.Conectado) {
        stringResource(if (porBluetooth) R.string.plataforma_por_bluetooth else R.string.plataforma_desde, z.direccion)
    } else {
        stringResource(R.string.pulso_sin_dato)
    }
    SettingsItem(stringResource(R.string.app_entrenamiento), icono = R.drawable.ic_sensors, iconoTag = ETIQUETA_ICONO_APP, ayuda = dato)
}

/** La batería del Square: nivel con «baja» si lo está, sin lectura todavía o la última conocida sin conexión. */
@Composable
private fun FilaBateriaSquare(b: BateriaFila) {
    val dato = when (b) {
        BateriaFila.SinLectura -> stringResource(R.string.bateria_dato_sin_lectura)
        is BateriaFila.Nivel -> stringResource(if (b.baja) R.string.bateria_dato_baja else R.string.bateria_dato, b.pct)
        is BateriaFila.Ultima -> stringResource(R.string.bateria_dato_ultima, b.pct)
    }
    SettingsItem(stringResource(R.string.bateria_square), icono = Enlace.Square.icono, iconoTag = etiquetaIcono(Enlace.Square) + "-bateria", ayuda = dato)
}

/** El último pulso y su antigüedad, o «—» sin pulso (spec `diagnostics`, «Pulso en Datos de conexión»). */
@Composable
private fun FilaPulso(p: FilaPulsometroUi) {
    val ahora by produceState(SystemClock.elapsedRealtime(), p.instanteMs) {
        while (true) {
            value = SystemClock.elapsedRealtime()
            delay(1_000)
        }
    }
    val dato = if (p.bpm != null && p.instanteMs != null) {
        stringResource(R.string.pulso_bpm_hace, p.bpm, pulsoHaceS(p.instanteMs, ahora))
    } else {
        stringResource(R.string.pulso_sin_dato)
    }
    SettingsItem(stringResource(R.string.pulsometro), icono = Enlace.Pulso.icono, iconoTag = etiquetaIcono(Enlace.Pulso), ayuda = dato)
}

/** El modo y la velocidad notificados y su antigüedad, o «—» sin notificación (spec `diagnostics`, «Ventilador en Datos de conexión»). */
@Composable
private fun FilaVentilador(v: FilaVentiladorUi) {
    val ahora by produceState(SystemClock.elapsedRealtime(), v.instanteMs) {
        while (true) {
            value = SystemClock.elapsedRealtime()
            delay(1_000)
        }
    }
    val e = v.estadoVentilador
    val dato = if (e != null && v.instanteMs != null) {
        stringResource(R.string.ventilador_modo_velocidad_hace, stringResource(nombreModoVentilador(e.modo)), e.velocidad, pulsoHaceS(v.instanteMs, ahora))
    } else {
        stringResource(R.string.pulso_sin_dato)
    }
    SettingsItem(stringResource(R.string.ventilador), icono = Enlace.Ventilador.icono, iconoTag = etiquetaIcono(Enlace.Ventilador), ayuda = dato)
}

/** Lo último que manda Zwift con sus marchas virtuales: lo que usa la app y lo que ignora, en una línea (spec `diagnostics`). */
@Composable
private fun FilaSimulacionZwift(z: FilaSimulacionZwiftUi) {
    val locale = localeActual()
    val ahora by produceState(SystemClock.elapsedRealtime(), z.instanteMs) {
        while (true) {
            value = SystemClock.elapsedRealtime()
            delay(1_000)
        }
    }
    val partes = listOfNotNull(
        z.pendientePct?.let { stringResource(R.string.sim_zwift_pendiente, decimal(it, locale)) },
        z.relacionMarcha?.let { stringResource(R.string.sim_zwift_relacion, decimal(it, locale, 2)) },
        z.crr?.let { stringResource(R.string.sim_zwift_crr, decimal(it, locale, 3)) },
        z.cw?.let { stringResource(R.string.sim_zwift_cw, decimal(it, locale, 2)) },
        if (z.masaCiclistaKg != null && z.masaBiciKg != null) stringResource(R.string.sim_zwift_masas, decimal(z.masaCiclistaKg, locale, 0), decimal(z.masaBiciKg, locale)) else null,
        stringResource(R.string.sim_zwift_hace, pulsoHaceS(z.instanteMs, ahora)),
    )
    SettingsItem(stringResource(R.string.sim_zwift), icono = Enlace.Zwift.icono, iconoTag = etiquetaIcono(Enlace.Zwift) + "-simulacion", ayuda = partes.joinToString(" · "))
}

/** El nombre con el que la app se anuncia; con la simulación de KICKR BIKE, también que lleva identidad propia. */
@Composable
private fun FilaNombreAnunciado(nombre: String, porBluetooth: Boolean) {
    val simulando = !porBluetooth && nombre != NombreAnunciado.NORMAL
    SettingsItem(
        stringResource(R.string.nombre_anunciado),
        icono = R.drawable.ic_sensors,
        iconoTag = ETIQUETA_ICONO_NOMBRE,
        ayuda = when {
            porBluetooth -> stringResource(R.string.nombre_bluetooth_movil, nombre)
            simulando -> stringResource(R.string.nombre_anunciado_kickr, nombre)
            else -> nombre
        },
    )
}

internal const val ETIQUETA_ICONO_APP = "icono-app-entrenamiento"
internal const val ETIQUETA_ICONO_NOMBRE = "icono-nombre-anunciado"

private val CRUCETA = listOf(
    SquareButton.CrucetaArriba, SquareButton.CrucetaAbajo, SquareButton.CrucetaIzquierda, SquareButton.CrucetaDerecha,
    SquareButton.A, SquareButton.B, SquareButton.Y, SquareButton.Z,
    SquareButton.Triangulo, SquareButton.Circulo, SquareButton.Cuadrado, SquareButton.Equis,
)
private val ANCHAS = listOf(
    SquareButton.PalancaIzquierda1,
    SquareButton.PalancaDerecha1,
    SquareButton.PalancaIzquierda2,
    SquareButton.PalancaDerecha2,
    SquareButton.FrenoIzquierdo,
    SquareButton.FrenoDerecho,
    SquareButton.CampagnoloIzquierdo,
    SquareButton.CampagnoloDerecho,
)

@Composable
private fun Botones(estado: DiagnosticoUiState) {
    SettingsSection(
        stringResource(R.string.botones_square),
        ayuda = stringResource(if (estado.squareConectado) R.string.botones_ayuda else R.string.botones_sin_square),
    ) {
        SettingsList(
            listOf<@Composable () -> Unit>({
                SettingsItemApilado {
                    Rejilla(CRUCETA, columnas = 4, ancha = false, estado.pulsados)
                    Rejilla(ANCHAS, columnas = 2, ancha = true, estado.pulsados)
                    Label(stringResource(R.string.ultimo_gesto))
                    val ultimo = estado.ultimoGesto
                    if (ultimo == null) {
                        Caption(stringResource(R.string.ninguno_todavia))
                    } else {
                        CombinacionSquare(
                            ultimo.botones.map { stringResource(it.nombreCorto) },
                            gesto = textoGesto(ultimo.gesto),
                            accion = when (val r = ultimo.resultado) {
                                is ResultadoGestoUi.Hecha -> textoAccion(r.accion, r.diferencia)
                                is ResultadoGestoUi.Plato -> stringResource(R.string.accion_plato, r.dientes)
                                is ResultadoGestoUi.Tope -> textoTope(r.tope)
                                is ResultadoGestoUi.Vatios -> textoVatios(r.diferenciaW)
                                ResultadoGestoUi.PotenciaZwift -> stringResource(R.string.accion_potencia_plataforma, estado.nombrePlataforma)
                                is ResultadoGestoUi.VueltaA -> stringResource(R.string.accion_vuelta_a, r.potenciaW)
                                is ResultadoGestoUi.TopePotencia -> stringResource(R.string.tope_potencia, r.potenciaW)
                                ResultadoGestoUi.SinAccion -> stringResource(R.string.sin_accion)
                            },
                        )
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Label(stringResource(R.string.mascara))
                        Text(estado.mascara ?: "—", style = SqTema.tipografia.mono)
                    }
                }
            }),
        )
    }
}

@Composable
private fun Rejilla(botones: List<SquareButton>, columnas: Int, ancha: Boolean, pulsados: Set<SquareButton>) {
    Column(verticalArrangement = Arrangement.spacedBy(SqTema.espacios.space2)) {
        botones.chunked(columnas).forEach { fila ->
            Row(horizontalArrangement = Arrangement.spacedBy(SqTema.espacios.space2)) {
                fila.forEach { b ->
                    SquareKey(stringResource(b.nombreCorto), Modifier.weight(1f), ancha = ancha, pulsada = b in pulsados)
                }
            }
        }
    }
}

@Composable
private fun Simulacion(s: SimulacionUi, plataforma: String, zwift: FilaSimulacionZwiftUi?) {
    val locale = localeActual()
    val tamano = 28.sp
    val sinDato = stringResource(R.string.sin_dato)
    SettingsSection(stringResource(R.string.simulacion), ayuda = stringResource(R.string.simulacion_ayuda)) {
        SettingsList(
            buildList<@Composable () -> Unit> {
                add {
                    SettingsItemApilado {
                        val modo = stringResource(
                            when (s.modo) {
                                ModoUi.Sim -> R.string.sim_modo_sim
                                ModoUi.SimPlataforma -> R.string.sim_modo_sim_plataforma
                                ModoUi.ErgPlataforma -> R.string.sim_modo_erg_plataforma
                                ModoUi.ErgManual -> R.string.sim_modo_erg_manual
                                ModoUi.Manual -> R.string.sim_modo_manual
                            },
                            plataforma,
                        )
                        val terreno = s.terreno
                        val potenciaPlataforma = s.potenciaPlataformaW
                        val potenciaFijada = s.potenciaFijadaW
                        val fisico = s.fisico
                        // Lo que pide Zwift sustituye a la pendiente base: en una ruta, su pendiente; en ERG, su potencia
                        // y el ajuste de las palancas en lugar del ajuste de la marcha (que no se envía).
                        val celdas = buildList {
                            add(Dato(stringResource(R.string.sim_modo), modo, texto = true))
                            add(Dato(stringResource(R.string.sim_modelo), stringResource(nombreModelo(s.modelo)), texto = true))
                            add(
                                Dato(
                                    stringResource(R.string.bajadas),
                                    stringResource(if (s.modelo == TipoModelo.FISICO) R.string.bajadas_no_aplica_fisico else nombreBajadas(s.bajadas)),
                                    texto = true,
                                ),
                            )
                            if (s.dificultadPct != 100) add(Dato(stringResource(R.string.dificultad), s.dificultadPct.toString(), "%"))
                            when (val m = s.marcha) {
                                is EstadoMarcha.Numerada -> add(Dato(stringResource(R.string.sim_marcha), m.posicion.toString(), stringResource(R.string.de_total, m.total)))

                                is EstadoMarcha.PlatoPinon -> {
                                    add(Dato(stringResource(R.string.sim_marcha), m.toString()))
                                    add(Dato(stringResource(R.string.sim_relacion), decimal(m.relacion, locale, 2)))
                                }
                            }
                            when {
                                potenciaFijada != null -> {
                                    add(Dato(stringResource(R.string.sim_potencia_fijada), potenciaFijada.toString(), "W"))
                                    add(Dato(stringResource(R.string.sim_entrada), s.entradaW?.toString(), "W"))
                                }

                                potenciaPlataforma != null -> {
                                    add(Dato(stringResource(R.string.sim_potencia_plataforma, plataforma), potenciaPlataforma.toString(), "W"))
                                    add(Dato(stringResource(R.string.sim_ajuste_palancas), conSignoEntero(s.ajustePalancasW ?: 0), "W"))
                                }

                                terreno != null -> {
                                    add(Dato(stringResource(R.string.sim_pendiente_plataforma, plataforma), decimal(terreno.pendientePct, locale), "%"))
                                    if (fisico == null) add(Dato(stringResource(R.string.sim_ajuste), conSigno(s.ajustePct, locale), "%"))
                                }

                                else -> {
                                    add(Dato(stringResource(R.string.sim_pendiente_base), decimal(s.pendienteBasePct, locale), "%"))
                                    if (fisico == null) add(Dato(stringResource(R.string.sim_ajuste), conSigno(s.ajustePct, locale), "%"))
                                }
                            }
                            if (fisico != null && potenciaFijada == null && potenciaPlataforma == null) {
                                add(Dato(stringResource(R.string.sim_potencia_calculada), fisico.calculadaW.toString(), "W"))
                                add(Dato(stringResource(R.string.sim_potencia_minima), fisico.minimaW.toString(), "W"))
                                add(Dato(stringResource(R.string.sim_cadencia_suave), fisico.cadenciaSuaveRpm.toString(), "rpm"))
                                add(Dato(stringResource(R.string.sim_velocidad), fisico.velocidadKmh?.let { decimal(it, locale) }, "km/h"))
                            }
                            add(Dato(stringResource(R.string.sim_enviada), s.enviado?.let { textoComando(it, locale) }, s.enviado?.let { if (it is ComandoRodillo.Potencia) "W" else "%" }))
                            add(Dato(stringResource(R.string.sim_latencia), s.latenciaMs?.toString(), "ms"))
                        }
                        celdas.chunked(2).forEach { fila ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(SqTema.espacios.space4)) {
                                fila.forEach { d -> MetricTile(d, tamano, Modifier.weight(1f), descripcionSinDato = sinDato) }
                            }
                        }
                        s.sinLimiteW?.let { FilaDato(stringResource(R.string.limitada_por_minima), stringResource(R.string.sin_limite_seria_w, it)) }
                        s.sinLimitePct?.let { FilaDato(stringResource(R.string.limitada_por_bajadas), stringResource(R.string.sin_limite_seria, conSigno(it, locale))) }
                        if (terreno != null) {
                            FilaDato(
                                stringResource(R.string.sim_terreno_plataforma, plataforma),
                                stringResource(
                                    R.string.sim_terreno_valor,
                                    terreno.crr?.let { decimal(it, locale, 4) } ?: "—",
                                    terreno.cw?.let { decimal(it, locale, 2) } ?: "—",
                                    terreno.vientoMs?.let { decimal(it, locale) } ?: "—",
                                ),
                            )
                        }
                        s.ultimoPlataforma?.let { m ->
                            FilaDato(stringResource(R.string.sim_ultimo_plataforma, plataforma), "${textoComandoZwift(m.comando)} · ${hora(m.horaMs)}")
                        }
                        if (s.plataformaEnEspera) Caption(stringResource(R.string.sim_plataforma_en_espera, plataforma))
                    }
                }
                zwift?.let { z -> add { FilaSimulacionZwift(z) } }
            },
        )
    }
}

@Composable
private fun Trafico(estado: DiagnosticoUiState, acciones: AccionesDiagnostico) {
    SettingsSection(stringResource(R.string.trafico)) {
        // Oculto al abrir: el volcado en directo es lo más costoso de la pantalla (spec `diagnostics`).
        SettingsList(
            listOf {
                SettingsItemCasilla(
                    stringResource(R.string.trafico_en_directo),
                    estado.traficoVisible,
                    { acciones.alternarTrafico() },
                    ayuda = stringResource(R.string.trafico_en_directo_ayuda, estado.nombrePlataforma),
                )
            },
        )
        if (estado.traficoVisible) {
            HexLog(estado.trafico, vacio = stringResource(R.string.sin_trafico), descripcion = stringResource(R.string.trafico_descripcion))
            Row(horizontalArrangement = Arrangement.spacedBy(SqTema.espacios.space3)) {
                Button(stringResource(if (estado.pausado) R.string.reanudar else R.string.pausar), acciones.alternarPausa, Modifier.weight(1f))
                Button(stringResource(R.string.limpiar), acciones.limpiar, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ControlManual(estado: DiagnosticoUiState, acciones: AccionesDiagnostico) {
    val locale = localeActual()
    val c = estado.control
    SettingsSection(
        stringResource(R.string.control_manual),
        ayuda = stringResource(if (estado.neoConectado) R.string.control_manual_ayuda else R.string.control_manual_sin_neo),
    ) {
        SettingsList(
            listOf(
                {
                    Stepper(
                        stringResource(R.string.pendiente), decimal(c.pendiente, locale), "%",
                        alRestar = { acciones.pendiente(-1) }, alSumar = { acciones.pendiente(+1) },
                        descripcionRestar = stringResource(R.string.bajar_pendiente), descripcionSumar = stringResource(R.string.subir_pendiente),
                        puedeRestar = c.puedeBajarPendiente, puedeSumar = c.puedeSubirPendiente,
                    )
                },
                {
                    Stepper(
                        stringResource(R.string.potencia), c.potencia.toString(), "W",
                        alRestar = { acciones.potencia(-1) }, alSumar = { acciones.potencia(+1) },
                        descripcionRestar = stringResource(R.string.bajar_potencia), descripcionSumar = stringResource(R.string.subir_potencia),
                        puedeRestar = c.puedeBajarPotencia, puedeSumar = c.puedeSubirPotencia,
                    )
                },
            ),
        )
        estado.ultimoEnvio?.let { envio ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Label(stringResource(R.string.ultimo_envio))
                Text(textoEnvio(envio, locale), style = SqTema.tipografia.mono)
            }
        }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(SqTema.espacios.space3)) {
        Button(
            stringResource(R.string.enviar_pendiente_corto),
            acciones.enviarPendiente,
            Modifier.weight(1f),
            principal = true,
            habilitado = estado.neoConectado,
            descripcion = stringResource(R.string.enviar_pendiente),
        )
        Button(stringResource(R.string.enviar_potencia), acciones.enviarPotencia, Modifier.weight(1f), habilitado = estado.neoConectado)
    }
}

@Composable
private fun FilaDato(etiqueta: String, valor: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(SqTema.espacios.space4), verticalAlignment = Alignment.CenterVertically) {
        Label(etiqueta)
        Text(valor, style = SqTema.tipografia.mono, color = SqTema.colores.ink, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
    }
}

@Composable
private fun textoComandoZwift(c: FtmsCodec.Comando): String = when (c) {
    is FtmsCodec.Comando.Simulacion -> stringResource(R.string.zwift_cmd_simulacion)
    is FtmsCodec.Comando.PotenciaObjetivo -> stringResource(R.string.zwift_cmd_potencia)
    FtmsCodec.Comando.PedirControl -> stringResource(R.string.zwift_cmd_control)
    FtmsCodec.Comando.Reiniciar -> stringResource(R.string.zwift_cmd_reiniciar)
    FtmsCodec.Comando.Iniciar -> stringResource(R.string.zwift_cmd_iniciar)
    is FtmsCodec.Comando.Parar -> stringResource(R.string.zwift_cmd_parar)
    is FtmsCodec.Comando.NoAdmitido, is FtmsCodec.Comando.Invalido -> stringResource(R.string.zwift_cmd_otro, c.opcode)
}

/** «+10», «−30», «0». */
internal fun conSignoEntero(v: Int): String = if (v > 0) {
    "+$v"
} else if (v < 0) {
    "−${-v}"
} else {
    "0"
}

/** Formato de [hora], creado una vez: es inmutable y se puede compartir. */
private val FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ROOT)

internal fun hora(ms: Long): String = FORMATO_HORA.format(Instant.ofEpochMilli(ms).atZone(ZoneId.systemDefault()))

/** «+5,0», «−11,0», «0,0»: con signo si no es cero. */
internal fun conSigno(v: Double, locale: Locale): String = if (v > 0) "+${decimal(v, locale)}" else decimal(v, locale)

internal fun textoComando(c: ComandoRodillo, locale: Locale): String = when (c) {
    is ComandoRodillo.Pendiente -> conSigno(c.pct, locale)
    is ComandoRodillo.Potencia -> c.vatios.toString()
}

/** «3,0», «−5,5» o «3.0», «−5.5» según el [locale]: con signo menos tipográfico. */
internal fun decimal(v: Double, locale: Locale, decimales: Int = 1): String = String.format(locale, "%.${decimales}f", v).replace('-', '−')

internal fun textoEnvio(e: Envio, locale: Locale): String {
    val valor = when (e) {
        is Envio.Pendiente -> "${decimal(e.pct, locale)} %"
        is Envio.Potencia -> "${e.vatios} W"
    }
    return "$valor · ${hora(e.horaMs)}"
}
