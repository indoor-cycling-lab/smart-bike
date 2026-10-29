package app.smartbike.ui.tabla

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import app.smartbike.R
import app.smartbike.dominio.EstadoMarcha
import app.smartbike.dominio.ModoCambio
import app.smartbike.ui.componentes.AppBar
import app.smartbike.ui.componentes.Button
import app.smartbike.ui.componentes.Caption
import app.smartbike.ui.componentes.GearStepper
import app.smartbike.ui.componentes.Label
import app.smartbike.ui.diagnostico.decimal
import app.smartbike.ui.tema.SqTema
import app.smartbike.ui.tema.localeActual
import app.smartbike.ui.textoTransmision
import java.util.Locale

/** Acciones de la tabla de marchas. */
class AccionesTabla(
    val volver: () -> Unit = {},
    val ajustar: (indice: Int, pasos: Int) -> Unit = { _, _ -> },
    val restablecer: () -> Unit = {},
)

@Composable
fun TablaRuta(volver: () -> Unit, vm: TablaViewModel = viewModel(factory = TablaViewModel.Factory)) {
    val estado by vm.estado.collectAsStateWithLifecycle()
    Tabla(estado, AccionesTabla(volver, vm::ajustar, vm::restablecer))
}

/**
 * Tabla de marchas (maquetas/tabla-marchas.html): una fila por marcha de la transmisión elegida, de la más ligera a la más
 * dura, con la puesta marcada y a la vista (design.md del change `modelos-tabla-y-fisico`, decisión 9). Secundaria de
 * Ajustes.
 */
@Composable
fun Tabla(estado: TablaUiState, acciones: AccionesTabla, modifier: Modifier = Modifier) {
    val locale = localeActual()
    val lista = rememberLazyListState()
    // Se abre desplazada a la marcha puesta, una vez que hay filas; al girar el móvil se queda donde estaba.
    var desplazada by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(estado.filas.isNotEmpty()) {
        if (estado.filas.isNotEmpty() && !desplazada) {
            lista.scrollToItem((CABECERA + estado.indicePuesta - 3).coerceAtLeast(0))
            desplazada = true
        }
    }
    val forma = RoundedCornerShape(SqTema.radios.radiusMd)
    LazyColumn(
        modifier.fillMaxSize().safeDrawingPadding().padding(horizontal = SqTema.espacios.space4),
        state = lista,
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        item {
            Column(Modifier.padding(top = SqTema.espacios.space4), verticalArrangement = Arrangement.spacedBy(SqTema.espacios.space4)) {
                AppBar(stringResource(R.string.tabla_marchas), volver = stringResource(R.string.volver_ajustes) to acciones.volver)
                Caption(stringResource(R.string.tabla_intro))
            }
        }
        item {
            Button(
                stringResource(R.string.restablecer),
                acciones.restablecer,
                Modifier.padding(top = SqTema.espacios.space4),
            )
        }
        item {
            Row(
                Modifier.fillMaxWidth().padding(vertical = SqTema.espacios.space4),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Label(textoTransmision(estado.transmision, ModoCambio.AUTOMATICO))
                Caption(stringResource(R.string.tabla_orden))
            }
        }
        itemsIndexed(estado.filas) { i, fila ->
            val ultima = i == estado.filas.lastIndex
            Column(
                Modifier
                    .then(if (i == 0) Modifier.clip(RoundedCornerShape(topStart = SqTema.radios.radiusMd, topEnd = SqTema.radios.radiusMd)) else Modifier)
                    .then(if (ultima) Modifier.clip(RoundedCornerShape(bottomStart = SqTema.radios.radiusMd, bottomEnd = SqTema.radios.radiusMd)) else Modifier),
            ) {
                val nombre = nombreMarcha(fila.marcha)
                val relacion = (fila.marcha as? EstadoMarcha.PlatoPinon)?.let { stringResource(R.string.relacion, decimal(it.relacion, locale, 2)) }
                    ?: stringResource(R.string.ajuste_de_la_marcha)
                GearStepper(
                    nombre = nombre,
                    ayuda = if (fila.puesta) stringResource(R.string.marcha_puesta, relacion) else relacion,
                    valor = ajusteTexto(fila.ajustePct, locale),
                    alRestar = { acciones.ajustar(i, -1) },
                    alSumar = { acciones.ajustar(i, 1) },
                    descripcionRestar = stringResource(R.string.bajar_ajuste_marcha, nombre),
                    descripcionSumar = stringResource(R.string.subir_ajuste_marcha, nombre),
                    puesta = fila.puesta,
                    puedeRestar = fila.puedeBajar,
                    puedeSumar = fila.puedeSubir,
                )
                if (!ultima) HorizontalDivider(thickness = 1.dp, color = SqTema.colores.line)
            }
        }
        item { Spacer(Modifier.height(SqTema.espacios.space8)) }
    }
}

private const val CABECERA = 3

fun nombreMarcha(m: EstadoMarcha): String = when (m) {
    is EstadoMarcha.Numerada -> m.posicion.toString()
    is EstadoMarcha.PlatoPinon -> m.toString()
}

/** «+3,2», «−1,5», «0,0» (con punto decimal en inglés): con signo explícito salvo el cero. */
fun ajusteTexto(pct: Double, locale: Locale): String {
    val t = String.format(locale, "%.1f", kotlin.math.abs(pct))
    return when {
        kotlin.math.abs(pct) < 0.05 -> String.format(locale, "%.1f", 0.0)
        pct > 0 -> "+$t"
        else -> "−$t"
    }
}
