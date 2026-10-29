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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import app.smartbike.R
import app.smartbike.dominio.Accion
import app.smartbike.dominio.Entrada
import app.smartbike.dominio.FuncionZwift
import app.smartbike.dominio.Regla
import app.smartbike.dominio.TipoGesto
import app.smartbike.sesion.EstadoCaptura
import app.smartbike.ui.componentes.AppBar
import app.smartbike.ui.componentes.Button
import app.smartbike.ui.componentes.Caption
import app.smartbike.ui.componentes.CapturePanel
import app.smartbike.ui.componentes.EntradaAccion
import app.smartbike.ui.componentes.EntradaUi
import app.smartbike.ui.componentes.Label
import app.smartbike.ui.componentes.Notice
import app.smartbike.ui.componentes.Segmentado
import app.smartbike.ui.componentes.SettingsList
import app.smartbike.ui.nombre
import app.smartbike.ui.nombreAccion
import app.smartbike.ui.teclasDe
import app.smartbike.ui.tema.SqTema
import app.smartbike.ui.textoGesto

/** Acciones de Asignar acción. */
class AccionesAsignar(
    val volver: () -> Unit = {},
    val quitar: (Regla) -> Unit = {},
    val elegirGesto: (TipoGesto) -> Unit = {},
    val guardar: () -> Unit = {},
)

@Composable
fun AsignarAccionRuta(accion: Accion, volver: () -> Unit) {
    val vm: AsignarAccionViewModel = viewModel(key = "asignar-$accion", factory = AsignarAccionViewModel.factory(accion))
    val estado by vm.estado.collectAsStateWithLifecycle()
    AsignarAccion(estado, AccionesAsignar(volver = volver, quitar = vm::quitar, elegirGesto = vm::elegirGesto, guardar = { vm.guardar(volver) }))
}

/** Pantalla de Asignar acción (maquetas/asignar.html). «Cancelar» y volver no guardan nada. */
@Composable
fun AsignarAccion(estado: AsignarAccionUiState, acciones: AccionesAsignar, modifier: Modifier = Modifier) {
    Column(
        modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(start = SqTema.espacios.space4, end = SqTema.espacios.space4, top = SqTema.espacios.space4, bottom = SqTema.espacios.space8),
        verticalArrangement = Arrangement.spacedBy(SqTema.espacios.space6),
    ) {
        AppBar(stringResource(R.string.asignar_accion), volver = stringResource(R.string.volver_botones) to acciones.volver)
        Column(verticalArrangement = Arrangement.spacedBy(SqTema.espacios.space1)) {
            Label(stringResource(R.string.accion))
            Text(nombreAccion(estado.accion), style = SqTema.tipografia.heading, modifier = Modifier.semantics { heading() })
            if (estado.accion is Accion.Mando) Caption(stringResource(R.string.accion_funcion_zwift))
        }
        if (estado.reglas.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(SqTema.espacios.space2)) {
                Label(stringResource(R.string.botones_de_accion))
                SettingsList(
                    estado.reglas.map { r ->
                        {
                            val texto = textoEntrada(r.entrada, r.gesto)
                            EntradaAccion(EntradaUi(teclasDe(r.entrada), textoGesto(r.gesto)), stringResource(R.string.quitar_entrada, texto), { acciones.quitar(r) })
                        }
                    },
                )
            }
        }
        Captura(estado)
        Column(verticalArrangement = Arrangement.spacedBy(SqTema.espacios.space2)) {
            Label(stringResource(R.string.gesto))
            val opciones = TipoGesto.ASIGNABLES
            Segmentado(
                listOf(R.string.opcion_pulsar, R.string.opcion_corta, R.string.opcion_larga, R.string.opcion_doble).map { stringResource(it) },
                opciones.indexOf(estado.gesto),
                { acciones.elegirGesto(opciones[it]) },
                deshabilitadas = estado.gestosDeshabilitados.map { opciones.indexOf(it) }.toSet(),
            )
            // Con una función de Zwift, «pulsar» la mantiene pulsada en Zwift mientras se mantiene el botón.
            Caption(stringResource(if (estado.accion is Accion.Mando) R.string.gesto_ayuda_zwift else R.string.gesto_ayuda))
        }
        estado.avisos.forEach { Aviso(it, estado) }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(SqTema.espacios.space3)) {
            Button(stringResource(R.string.cancelar), acciones.volver, Modifier.weight(1f))
            Button(stringResource(R.string.guardar), acciones.guardar, Modifier.weight(1f), principal = true, habilitado = estado.puedeGuardar)
        }
    }
}

@Composable
private fun Captura(estado: AsignarAccionUiState) {
    when (val c = estado.captura) {
        EstadoCaptura.SinSquare -> CapturePanel(stringResource(R.string.captura_sin_square), escuchando = false)

        EstadoCaptura.Esperando -> CapturePanel(stringResource(R.string.captura_texto), esperando = stringResource(R.string.captura_esperando))

        is EstadoCaptura.Capturada -> CapturePanel(
            stringResource(R.string.captura_texto),
            teclas = teclasDe(c.entrada),
            detectado = stringResource(if (c.entrada.modificador != null) R.string.detectado_combinacion else R.string.detectado_boton),
        )
    }
}

@Composable
private fun Aviso(a: AvisoAsignar, estado: AsignarAccionUiState) {
    when (a) {
        is AvisoAsignar.Libre -> Notice(stringResource(R.string.entrada_libre, textoEntrada(a.entrada, a.gesto)))

        is AvisoAsignar.YaAsignada -> Notice(stringResource(R.string.entrada_ya_asignada, textoEntrada(a.entrada, a.gesto)))

        is AvisoAsignar.EnUso -> {
            val entrada = estado.capturada ?: return
            val acciones = a.perdidas.map { nombreAccion(it.accion) }.distinct().joinToString("», «")
            Notice(stringResource(R.string.entrada_en_uso, textoEntrada(entrada, estado.gesto), acciones), error = true)
        }
    }
}

/** «Campag. D + △ corta»: las teclas unidas por «+» y el gesto. */
@Composable
private fun textoEntrada(e: Entrada, g: TipoGesto) = teclasDe(e).joinToString(" + ") + " " + textoGesto(g)
