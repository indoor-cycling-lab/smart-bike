package app.smartbike.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import app.smartbike.R
import app.smartbike.dominio.Accion
import app.smartbike.dominio.AvisoVentilador
import app.smartbike.dominio.Entrada
import app.smartbike.dominio.EstadoMarcha
import app.smartbike.dominio.FuncionZwift
import app.smartbike.dominio.ModoCambio
import app.smartbike.dominio.ModoVentilador
import app.smartbike.dominio.TipoGesto
import app.smartbike.dominio.TopeMarcha
import app.smartbike.dominio.Transmision
import app.smartbike.protocolo.SquareButton
import kotlin.math.absoluteValue

/**
 * Texto de una acción, igual en el aviso de Rodaje y en Diagnóstico: «+3 marchas», «Marcha más dura»… Con
 * [diferencia] (la que hubo de verdad), un salto recortado por el extremo dice lo que cambió («+1 marcha»).
 */
@Composable
fun textoAccion(accion: Accion, diferencia: Int? = null): String = when (accion) {
    is Accion.MarchaMas -> (diferencia ?: accion.n).absoluteValue.let { pluralStringResource(R.plurals.accion_mas_marchas, it, it) }

    is Accion.MarchaMenos -> (diferencia ?: accion.n).absoluteValue.let { pluralStringResource(R.plurals.accion_menos_marchas, it, it) }

    Accion.MarchaMasDura -> stringResource(R.string.accion_mas_dura)

    Accion.MarchaMasLigera -> stringResource(R.string.accion_mas_ligera)

    Accion.MarchaReferencia -> stringResource(R.string.accion_referencia)

    Accion.FijarReferencia -> stringResource(R.string.accion_fijar_referencia)

    Accion.PlatoMas -> stringResource(R.string.accion_plato_mas)

    Accion.PlatoMenos -> stringResource(R.string.accion_plato_menos)

    Accion.ErgMas -> stringResource(R.string.accion_erg_mas)

    Accion.ErgMenos -> stringResource(R.string.accion_erg_menos)

    Accion.AlternarErgManual -> stringResource(R.string.accion_alternar_erg)

    Accion.PantallaAnterior -> stringResource(R.string.accion_pantalla_anterior)

    Accion.PantallaSiguiente -> stringResource(R.string.accion_pantalla_siguiente)

    Accion.SiguienteModelo -> stringResource(R.string.accion_siguiente_modelo)

    Accion.VentiladorAlternar -> stringResource(R.string.accion_ventilador_alternar)

    Accion.VentiladorMas -> stringResource(R.string.accion_ventilador_mas)

    Accion.VentiladorMenos -> stringResource(R.string.accion_ventilador_menos)

    is Accion.Mando -> when (val f = accion.funcion) {
        is FuncionZwift -> stringResource(f.nombre)
    }
}

/** Nombre de una función de Zwift en Botones, Asignar acción y Diagnóstico: «Ride On», «Freno izquierdo». */
val FuncionZwift.nombre: Int
    get() = when (this) {
        FuncionZwift.ARRIBA -> R.string.zwift_barra_acciones
        FuncionZwift.ABAJO -> R.string.zwift_cambio_sentido
        FuncionZwift.IZQUIERDA -> R.string.zwift_izquierda
        FuncionZwift.DERECHA -> R.string.zwift_derecha
        FuncionZwift.A -> R.string.zwift_seleccionar
        FuncionZwift.B -> R.string.zwift_atras
        FuncionZwift.Y -> R.string.zwift_powerup
        FuncionZwift.Z -> R.string.zwift_ride_on
        FuncionZwift.GIRAR_IZQUIERDA -> R.string.zwift_girar_izquierda
        FuncionZwift.GIRAR_DERECHA -> R.string.zwift_girar_derecha
        FuncionZwift.FRENO_IZQUIERDO -> R.string.zwift_freno_izquierdo
        FuncionZwift.FRENO_DERECHO -> R.string.zwift_freno_derecho
    }

/** Nombre de una acción en Botones y Asignar acción: «Volver a la marcha de referencia» en lugar del aviso. */
@Composable
fun nombreAccion(accion: Accion): String = when (accion) {
    Accion.MarchaReferencia -> stringResource(R.string.accion_volver_referencia)
    else -> textoAccion(accion)
}

/** «pulsar», «corta», «larga», «doble» o «repetir», como en Diagnóstico y en Botones. */
@Composable
fun textoGesto(gesto: TipoGesto): String = stringResource(
    when (gesto) {
        TipoGesto.Pulsar -> R.string.gesto_pulsar
        TipoGesto.Corta -> R.string.gesto_corta
        TipoGesto.Larga -> R.string.gesto_larga
        TipoGesto.Doble -> R.string.gesto_doble
        TipoGesto.Repetir -> R.string.gesto_repetir
    },
)

/** «+10» o «−30», con el signo menos tipográfico como en los avisos de marcha. */
fun conSigno(n: Int): String = if (n >= 0) "+$n" else "−${-n}"

/** Texto de un ajuste de potencia en ERG: «+10 W», «−30 W». */
@Composable
fun textoVatios(diferenciaW: Int): String = stringResource(R.string.accion_vatios, conSigno(diferenciaW))

@Composable
fun textoTope(tope: TopeMarcha): String = when (tope) {
    TopeMarcha.MasDura -> stringResource(R.string.tope_mas_dura)
    TopeMarcha.MasLigera -> stringResource(R.string.tope_mas_ligera)
    is TopeMarcha.Plato -> stringResource(R.string.tope_plato, tope.dientes)
    is TopeMarcha.Pinon -> stringResource(R.string.tope_pinon, tope.dientes)
}

/** La transmisión en la barra superior de Rodaje y en Ajustes: «Lineal · 24 marchas» o «Shimano 105 · automático». */
@Composable
fun textoTransmision(t: Transmision, modo: ModoCambio): String = when (t) {
    is Transmision.Lineal -> stringResource(R.string.transmision_lineal, t.marchas)

    is Transmision.Platos -> stringResource(
        if (modo == ModoCambio.AUTOMATICO) R.string.transmision_automatico else R.string.transmision_manual,
        t.nombre,
    )
}

/** «Referencia en la 15» o «Referencia en 36×17». */
@Composable
fun textoReferenciaFijada(m: EstadoMarcha): String = when (m) {
    is EstadoMarcha.Numerada -> stringResource(R.string.referencia_en, m.posicion)
    is EstadoMarcha.PlatoPinon -> stringResource(R.string.referencia_en_plato_pinon, m.toString())
}

/** Nombre corto de un botón como tecla: «Palanca D1», «Campag. I», «△». */
val SquareButton.nombreCorto: Int
    get() = when (this) {
        SquareButton.PalancaIzquierda1 -> R.string.boton_palanca_i1
        SquareButton.PalancaIzquierda2 -> R.string.boton_palanca_i2
        SquareButton.PalancaDerecha1 -> R.string.boton_palanca_d1
        SquareButton.PalancaDerecha2 -> R.string.boton_palanca_d2
        SquareButton.CrucetaArriba -> R.string.boton_cruceta_arriba
        SquareButton.CrucetaAbajo -> R.string.boton_cruceta_abajo
        SquareButton.CrucetaIzquierda -> R.string.boton_cruceta_izquierda
        SquareButton.CrucetaDerecha -> R.string.boton_cruceta_derecha
        SquareButton.Cuadrado -> R.string.boton_cuadrado
        SquareButton.Equis -> R.string.boton_equis
        SquareButton.Triangulo -> R.string.boton_triangulo
        SquareButton.Circulo -> R.string.boton_circulo
        SquareButton.FrenoIzquierdo -> R.string.boton_freno_i
        SquareButton.FrenoDerecho -> R.string.boton_freno_d
        SquareButton.CampagnoloIzquierdo -> R.string.boton_campagnolo_i
        SquareButton.CampagnoloDerecho -> R.string.boton_campagnolo_d
        SquareButton.A -> R.string.boton_a
        SquareButton.B -> R.string.boton_b
        SquareButton.Y -> R.string.boton_y
        SquareButton.Z -> R.string.boton_z
    }

/** Las teclas de una entrada, el modificador primero. */
@Composable
fun teclasDe(entrada: Entrada): List<String> = listOfNotNull(entrada.modificador, entrada.boton).map { stringResource(it.nombreCorto) }

/** El aviso del pie tras una acción del ventilador (spec `ride-screen`, «Aviso de las acciones del ventilador»). */
@Composable
fun textoAvisoVentilador(aviso: AvisoVentilador): String = when (aviso) {
    is AvisoVentilador.Velocidad -> stringResource(R.string.aviso_ventilador_velocidad, aviso.porcentaje)

    is AvisoVentilador.Manual ->
        aviso.porcentaje?.let { stringResource(R.string.aviso_ventilador_manual_velocidad, it) } ?: stringResource(R.string.aviso_ventilador_manual)

    AvisoVentilador.Automatico -> stringResource(R.string.aviso_ventilador_automatico)

    AvisoVentilador.EnAutomatico -> stringResource(R.string.aviso_ventilador_en_automatico)

    AvisoVentilador.SinVentilador -> stringResource(R.string.aviso_sin_ventilador)
}

/** «Manual», «Automático» u «Otro modo»: el modo del ventilador tal como lo comunica él. */
fun nombreModoVentilador(modo: ModoVentilador): Int = when (modo) {
    ModoVentilador.Manual -> R.string.ventilador_modo_manual
    ModoVentilador.Pulso -> R.string.ventilador_modo_automatico
    ModoVentilador.Otro -> R.string.ventilador_modo_otro
}
