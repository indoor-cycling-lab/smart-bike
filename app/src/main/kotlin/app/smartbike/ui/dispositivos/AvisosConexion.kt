package app.smartbike.ui.dispositivos

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import app.smartbike.R
import app.smartbike.plataforma.EstadoPlataforma
import app.smartbike.sesion.EstadoDispositivo
import app.smartbike.sesion.EstadoRodaje

enum class Permiso { Desconocido, Concedido, Denegado }

/**
 * Avisos de las conexiones, en el orden en que se calculan. Los de reconexión son informativos: el corte se recupera
 * solo y no pide hacer nada más que despertarlo; el resto son de error. Dispositivos solo muestra los de error.
 */
enum class Aviso(val error: Boolean = true) {
    BluetoothApagado,
    SinPermiso,
    NeoNoEncontrado,
    NeoReconectando(error = false),
    SquareNoEncontrado,
    SquareReconectando(error = false),
    SinWifi(error = false),
    PlataformaNoVisible,
    PulsometroReconectando(error = false),
    PulsometroNoEncontrado,
    VentiladorReconectando(error = false),
    VentiladorNoEncontrado,
}

/** El botón que resuelve el problema de las conexiones: buscar, activar el Bluetooth o abrir los ajustes. */
enum class AccionConexiones { Buscar, Buscando, ActivarBluetooth, AbrirAjustes }

/** Los avisos de las conexiones. */
fun avisosDeConexion(c: EstadoRodaje, permiso: Permiso): List<Aviso> = buildList {
    if (!c.bluetoothEncendido) add(Aviso.BluetoothApagado)
    if (permiso == Permiso.Denegado) add(Aviso.SinPermiso)
    if (c.neo == EstadoDispositivo.NoEncontrado) add(Aviso.NeoNoEncontrado)
    if (c.neo == EstadoDispositivo.Reconectando) add(Aviso.NeoReconectando)
    if (c.square == EstadoDispositivo.NoEncontrado) add(Aviso.SquareNoEncontrado)
    if (c.square == EstadoDispositivo.Reconectando) add(Aviso.SquareReconectando)
    if (c.pulsometroConfigurado != null && c.pulsometro == EstadoDispositivo.Reconectando) add(Aviso.PulsometroReconectando)
    if (c.ventiladorConfigurado != null && c.ventilador == EstadoDispositivo.Reconectando) add(Aviso.VentiladorReconectando)
    if (c.plataforma == EstadoPlataforma.SinWifi) add(Aviso.SinWifi)
    if (c.plataforma == EstadoPlataforma.NoVisible) add(Aviso.PlataformaNoVisible)
}

/** El botón que resuelve el problema de las conexiones: buscar, activar el Bluetooth o abrir los ajustes. */
fun accionDeConexiones(c: EstadoRodaje, permiso: Permiso): AccionConexiones = when {
    !c.bluetoothEncendido -> AccionConexiones.ActivarBluetooth
    permiso == Permiso.Denegado -> AccionConexiones.AbrirAjustes
    EstadoDispositivo.Buscando in setOf(c.neo, c.square) -> AccionConexiones.Buscando
    else -> AccionConexiones.Buscar
}

/** El texto de un aviso; el del wifi y el de la plataforma no visible nombran la plataforma elegida. */
@Composable
internal fun textoAviso(aviso: Aviso, plataforma: String): String = if (aviso == Aviso.SinWifi || aviso == Aviso.PlataformaNoVisible) stringResource(aviso.texto, plataforma) else stringResource(aviso.texto)

internal val Aviso.texto: Int
    get() = when (this) {
        Aviso.BluetoothApagado -> R.string.aviso_bluetooth_apagado
        Aviso.SinPermiso -> R.string.aviso_sin_permiso
        Aviso.NeoNoEncontrado -> R.string.aviso_neo_no_encontrado
        Aviso.NeoReconectando -> R.string.aviso_neo_reconectando
        Aviso.SquareNoEncontrado -> R.string.aviso_square_no_encontrado
        Aviso.SquareReconectando -> R.string.aviso_square_reconectando
        Aviso.SinWifi -> R.string.aviso_sin_wifi
        Aviso.PlataformaNoVisible -> R.string.aviso_plataforma_no_visible
        Aviso.PulsometroReconectando -> R.string.aviso_pulsometro_reconectando
        Aviso.PulsometroNoEncontrado -> R.string.aviso_pulsometro_no_encontrado
        Aviso.VentiladorReconectando -> R.string.aviso_ventilador_reconectando
        Aviso.VentiladorNoEncontrado -> R.string.aviso_ventilador_no_encontrado
    }
