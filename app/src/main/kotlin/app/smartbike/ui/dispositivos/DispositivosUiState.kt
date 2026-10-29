package app.smartbike.ui.dispositivos

import app.smartbike.dominio.BateriaSquare
import app.smartbike.dominio.NombreAnunciado
import app.smartbike.sesion.EstadoDispositivo
import app.smartbike.sesion.EstadoRodaje
import app.smartbike.ui.NOMBRE_APP_ENTRENAMIENTO

/** Lo que dice la fila de un dispositivo: su estado en palabra (`Reconectando` se dice «Conectando», salvo en el pulsómetro). */
enum class EstadoFila { Conectado, Conectando, Reconectando, Buscando, NoEncontrado, SinConexion }

/** Fila del Neo o del Square: estado, nombre del configurado (o `null`) y si la app intenta conectarlo. */
data class FilaDispositivoUi(val estado: EstadoFila = EstadoFila.SinConexion, val configurado: String? = null)

/** Dispositivos (maquetas/dispositivos.html; spec `device-connection`, «Pantalla de Dispositivos»). */
data class DispositivosUiState(
    val neo: FilaDispositivoUi = FilaDispositivoUi(),
    val square: FilaDispositivoUi = FilaDispositivoUi(),
    /** El pulsómetro, opcional: la fila y «Buscar pulsómetro» se ven siempre, el aviso solo si no se encuentra. */
    val pulsometro: FilaDispositivoUi = FilaDispositivoUi(),
    /** El ventilador, opcional como el pulsómetro: la fila y «Buscar ventilador» se ven siempre. */
    val ventilador: FilaDispositivoUi = FilaDispositivoUi(),
    /** El nombre con el que la app se anuncia ante Zwift: lo nombra la ayuda del pulsómetro conectado (Zwift lo ve como una fuente más). */
    val nombreAnunciado: String = NombreAnunciado.NORMAL,
    /** El nombre de la plataforma en uso: lo nombran las ayudas del pulsómetro y los avisos. */
    val nombrePlataforma: String = NOMBRE_APP_ENTRENAMIENTO,
    val bluetoothEncendido: Boolean = true,
    /** Solo los de error: la reconexión y el wifi ya se explican en su fila. */
    val avisos: List<Aviso> = emptyList(),
    val accion: AccionConexiones = AccionConexiones.Buscar,
    /** El nivel de la batería del Square si está conectado y es baja: el aviso informativo va bajo el Controlador (spec `device-connection`). */
    val bateriaSquareBajaPct: Int? = null,
) {
    /**
     * El «Buscar» de cada sección se deshabilita mientras busca, también al conectar con lo encontrado: «Buscar» solo
     * se ve sin dispositivo configurado, así que «Conectando» es el final de la búsqueda, y pulsarlo otra vez cortaría
     * esa conexión para empezar de cero. Los de las demás secciones siguen disponibles.
     */
    val buscandoNeo get() = neo.buscando
    val buscandoSquare get() = square.buscando

    /** «Buscar pulsómetro» se deshabilita mientras busca. */
    val buscandoPulsometro get() = pulsometro.buscando

    /** «Buscar ventilador» se deshabilita mientras busca. */
    val buscandoVentilador get() = ventilador.buscando
}

private val FilaDispositivoUi.buscando get() = estado == EstadoFila.Buscando || estado == EstadoFila.Conectando

private fun fila(e: EstadoDispositivo, configurado: String?, dichoReconectando: Boolean = false) = FilaDispositivoUi(
    when (e) {
        EstadoDispositivo.Conectado -> EstadoFila.Conectado
        EstadoDispositivo.Conectando -> EstadoFila.Conectando
        EstadoDispositivo.Reconectando -> if (dichoReconectando) EstadoFila.Reconectando else EstadoFila.Conectando
        EstadoDispositivo.Buscando -> EstadoFila.Buscando
        EstadoDispositivo.NoEncontrado -> EstadoFila.NoEncontrado
        EstadoDispositivo.SinConexion -> EstadoFila.SinConexion
    },
    configurado,
)

fun estadoDeDispositivos(c: EstadoRodaje, permiso: Permiso) = DispositivosUiState(
    neo = fila(c.neo, c.neoConfigurado?.nombre),
    square = fila(c.square, c.squareConfigurado?.nombre),
    pulsometro = fila(c.pulsometro, c.pulsometroConfigurado?.nombre, dichoReconectando = true),
    ventilador = fila(c.ventilador, c.ventiladorConfigurado?.nombre, dichoReconectando = true),
    nombreAnunciado = c.nombreAnunciado,
    bluetoothEncendido = c.bluetoothEncendido,
    avisos = (
        avisosDeConexion(c, permiso) + listOfNotNull(
            Aviso.PulsometroNoEncontrado.takeIf { c.pulsometro == EstadoDispositivo.NoEncontrado },
            Aviso.VentiladorNoEncontrado.takeIf { c.ventilador == EstadoDispositivo.NoEncontrado },
        )
        ).filter { it.error && it != Aviso.PlataformaNoVisible },
    accion = accionDeConexiones(c, permiso),
    bateriaSquareBajaPct = c.bateriaSquare?.takeIf { c.square == EstadoDispositivo.Conectado && BateriaSquare.baja(it) },
)
