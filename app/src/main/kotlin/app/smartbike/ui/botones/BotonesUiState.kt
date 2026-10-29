package app.smartbike.ui.botones

import app.smartbike.dominio.Accion
import app.smartbike.dominio.Configuracion
import app.smartbike.dominio.MandoEnModo
import app.smartbike.dominio.Regla
import app.smartbike.dominio.TiemposGestos
import app.smartbike.dominio.Transmision

/** Los cuatro tiempos de los gestos, con sus límites (spec `button-gestures`, «Tiempos configurables»). */
enum class Tiempo(val limites: IntRange) {
    Larga(TiemposGestos.LARGA),
    Doble(TiemposGestos.DOBLE),
    Retardo(TiemposGestos.RETARDO),
    Ritmo(TiemposGestos.RITMO),
    ;

    fun de(t: TiemposGestos): Int = when (this) {
        Larga -> t.largaMs
        Doble -> t.dobleMs
        Retardo -> t.retardoRepeticionMs
        Ritmo -> t.ritmoRepeticionMs
    }

    /** [t] con este tiempo cambiado en [pasos] de 50 ms, sin salir de sus límites. */
    fun cambiar(t: TiemposGestos, pasos: Int): TiemposGestos {
        val v = (de(t) + pasos * TiemposGestos.PASO_MS).coerceIn(limites)
        return when (this) {
            Larga -> t.copy(largaMs = v)
            Doble -> t.copy(dobleMs = v)
            Retardo -> t.copy(retardoRepeticionMs = v)
            Ritmo -> t.copy(ritmoRepeticionMs = v)
        }
    }
}

/** Una acción de la app o una función de Zwift con sus entradas físicas; sin ninguna, la fila dice «Sin botón». */
data class FilaAccion(val accion: Accion, val reglas: List<Regla>)

/** Pantalla de Botones (maquetas/botones.html): la asignación de la transmisión elegida (estados `lista` y `shimano`). */
data class BotonesUiState(
    val transmision: Transmision = Transmision.Lineal(),
    /** Las funciones de Zwift, asignables como las acciones (design.md del change `mando-zwift-emulado`, decisión 9). */
    val funcionesZwift: List<FilaAccion> = emptyList(),
    /** Cómo llegan las funciones del mando a la app de entrenamiento (spec `button-mapping`; estado `sin-square` de la maqueta). */
    val mando: MandoBotones = MandoBotones.Aparte,
    val acciones: List<FilaAccion> = emptyList(),
    val invertirPalancas: Boolean = false,
    val autorrepeticion: Boolean = true,
    val tiempos: TiemposGestos = TiemposGestos(),
    /** Se está pidiendo confirmación para restaurar (estado `restaurar` de la maqueta). */
    val confirmandoRestaurar: Boolean = false,
) {
    fun puedeBajar(t: Tiempo) = t.de(tiempos) > t.limites.first
    fun puedeSubir(t: Tiempo) = t.de(tiempos) < t.limites.last
}

/** Por dónde llegan las funciones del mando: aparte por Bluetooth, dentro del mismo dispositivo o, sin Square configurado, por ningún sitio. */
enum class MandoBotones { Aparte, Dentro, SinSquare }

fun estadoDeBotones(c: Configuracion, confirmandoRestaurar: Boolean, squareConfigurado: Boolean = true): BotonesUiState {
    val t = c.transmisionElegida
    return BotonesUiState(
        transmision = t,
        funcionesZwift = Accion.ZWIFT.map { FilaAccion(it, c.asignacion.reglasDe(it)) },
        mando = when {
            // En Zwift Ride el mando va siempre (lleva el terreno), pero sin Square no hay botones que enviar.
            !squareConfigurado -> MandoBotones.SinSquare

            c.modoPublicado.mando == MandoEnModo.DENTRO_DEL_MODO -> MandoBotones.Dentro

            else -> MandoBotones.Aparte
        },
        acciones = Accion.asignables(t).filterNot { it is Accion.Mando }.map { FilaAccion(it, c.asignacion.reglasDe(it)) },
        invertirPalancas = c.asignacion.invertirPalancas,
        autorrepeticion = c.botones.autorrepeticion,
        tiempos = c.botones.tiempos,
        confirmandoRestaurar = confirmandoRestaurar,
    )
}
