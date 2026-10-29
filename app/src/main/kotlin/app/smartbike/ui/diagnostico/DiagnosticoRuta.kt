package app.smartbike.ui.diagnostico

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

/** Diagnóstico con su `ViewModel`. */
@Composable
fun DiagnosticoRuta(volver: () -> Unit, vm: DiagnosticoViewModel = viewModel(factory = DiagnosticoViewModel.Factory)) {
    val estado by vm.estado.collectAsStateWithLifecycle()
    Diagnostico(
        estado,
        AccionesDiagnostico(
            pendiente = vm::pendiente,
            potencia = vm::potencia,
            enviarPendiente = vm::enviarPendiente,
            enviarPotencia = vm::enviarPotencia,
            alternarTrafico = vm::alternarTrafico,
            alternarPausa = vm::alternarPausa,
            limpiar = vm::limpiar,
            volver = volver,
        ),
    )
}
