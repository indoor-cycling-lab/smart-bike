package app.smartbike.ui.dispositivos

import app.smartbike.dominio.DispositivoConfigurado
import app.smartbike.dominio.EstadoMarcha
import app.smartbike.plataforma.EstadoMando
import app.smartbike.plataforma.EstadoPlataforma
import app.smartbike.sesion.EstadoDispositivo
import app.smartbike.sesion.EstadoRodaje
import app.smartbike.sesion.Simulacion
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/** Spec `device-connection`, «Pantalla de Dispositivos» y «Olvidar un dispositivo», sobre el estado. */
class DispositivosUiStateTest {

    private val neo = DispositivoConfigurado("AA:AA", "Tacx Neo 12345")
    private val square = DispositivoConfigurado("BB:BB", "SQUARE 1234")
    private val m = EstadoMarcha.Numerada(12, 24)

    private fun rodaje(
        neo: EstadoDispositivo = EstadoDispositivo.SinConexion,
        square: EstadoDispositivo = EstadoDispositivo.SinConexion,
        zwift: EstadoPlataforma = EstadoPlataforma.Esperando,
        bluetooth: Boolean = true,
        configurados: Boolean = true,
    ) = EstadoRodaje(
        neo = neo,
        square = square,
        plataforma = zwift,
        bluetoothEncendido = bluetooth,
        marcha = m,
        referencia = m,
        simulacion = Simulacion(0.0, 0.0, null),
        neoConfigurado = this.neo.takeIf { configurados },
        squareConfigurado = this.square.takeIf { configurados },
    )

    @Test
    fun todo_conectado() {
        val e = estadoDeDispositivos(rodaje(EstadoDispositivo.Conectado, EstadoDispositivo.Conectado, EstadoPlataforma.Conectado("192.168.0.10")), Permiso.Concedido)
        assertEquals(FilaDispositivoUi(EstadoFila.Conectado, "Tacx Neo 12345"), e.neo)
        assertEquals(FilaDispositivoUi(EstadoFila.Conectado, "SQUARE 1234"), e.square)
        assertTrue(e.avisos.isEmpty())
        assertEquals(AccionConexiones.Buscar, e.accion)
    }

    @Test
    fun primer_uso() {
        val e = estadoDeDispositivos(rodaje(configurados = false), Permiso.Desconocido)
        assertEquals(FilaDispositivoUi(EstadoFila.SinConexion, null), e.neo)
        assertNull(e.square.configurado)
    }

    @Test
    fun neo_dormido_y_square_reconectando() {
        val e = estadoDeDispositivos(rodaje(EstadoDispositivo.Conectando, EstadoDispositivo.Reconectando), Permiso.Concedido)
        assertEquals(EstadoFila.Conectando, e.neo.estado)
        // «Conexión perdida con la pantalla abierta»: la reconexión se dice «Conectando», sin aviso aparte.
        assertEquals(EstadoFila.Conectando, e.square.estado)
        assertTrue(e.avisos.isEmpty())
    }

    @Test
    fun no_encontrado_con_aviso() {
        val e = estadoDeDispositivos(rodaje(neo = EstadoDispositivo.NoEncontrado), Permiso.Concedido)
        assertEquals(EstadoFila.NoEncontrado, e.neo.estado)
        assertEquals(listOf(Aviso.NeoNoEncontrado), e.avisos)
    }

    @Test
    fun bluetooth_apagado_y_sin_permiso() {
        val apagado = estadoDeDispositivos(rodaje(bluetooth = false), Permiso.Concedido)
        assertEquals(listOf(Aviso.BluetoothApagado), apagado.avisos)
        assertEquals(AccionConexiones.ActivarBluetooth, apagado.accion)
        val sinPermiso = estadoDeDispositivos(rodaje(), Permiso.Denegado)
        assertEquals(listOf(Aviso.SinPermiso), sinPermiso.avisos)
        assertEquals(AccionConexiones.AbrirAjustes, sinPermiso.accion)
    }

    // Zwift y el mando ya no están en Dispositivos (spec `device-connection`, «Dispositivos de la app»; su estado está en Plataformas).

    @Test
    fun el_estado_de_zwift_no_cambia_nada_de_dispositivos() {
        val conectado = estadoDeDispositivos(rodaje(EstadoDispositivo.Conectado, EstadoDispositivo.Conectado, EstadoPlataforma.Esperando), Permiso.Concedido)
        for (z in listOf(EstadoPlataforma.SinWifi, EstadoPlataforma.NoVisible, EstadoPlataforma.Conectado("192.168.0.10"))) {
            assertEquals(conectado, estadoDeDispositivos(rodaje(EstadoDispositivo.Conectado, EstadoDispositivo.Conectado, z), Permiso.Concedido))
        }
    }

    @Test
    fun zwift_no_visible_no_da_aviso_en_dispositivos() {
        val e = estadoDeDispositivos(rodaje(EstadoDispositivo.Conectado, EstadoDispositivo.Conectado, EstadoPlataforma.NoVisible), Permiso.Concedido)
        assertFalse(Aviso.PlataformaNoVisible in e.avisos)
        assertTrue(e.avisos.isEmpty())
    }

    @Test
    fun el_mando_no_cambia_nada_de_dispositivos() {
        val base = estadoDeDispositivos(rodaje(), Permiso.Concedido)
        for (m in EstadoMando.entries) assertEquals(base, estadoDeDispositivos(rodaje().copy(mando = m), Permiso.Concedido))
    }

    // --- Pulsómetro (spec `device-connection`, «Buscar el pulsómetro aparte»; change `pulso-zwift`) ---

    private val banda = DispositivoConfigurado("CC:CC", "HW9-1234")

    private fun conBanda(e: EstadoDispositivo, configurada: Boolean = true) = rodaje(EstadoDispositivo.Conectado, EstadoDispositivo.Conectado).copy(pulsometro = e, pulsometroConfigurado = banda.takeIf { configurada })

    @Test
    fun sin_banda_configurada_la_fila_y_el_boton_se_ven_sin_error() {
        val e = estadoDeDispositivos(conBanda(EstadoDispositivo.SinConexion, configurada = false), Permiso.Concedido)
        assertEquals(FilaDispositivoUi(EstadoFila.SinConexion, null), e.pulsometro)
        assertFalse(e.buscandoPulsometro)
        assertTrue(e.avisos.isEmpty())
    }

    @Test
    fun los_estados_de_la_fila_del_pulsometro() {
        fun fila(e: EstadoDispositivo) = estadoDeDispositivos(conBanda(e), Permiso.Concedido).pulsometro
        assertEquals(FilaDispositivoUi(EstadoFila.Conectado, "HW9-1234"), fila(EstadoDispositivo.Conectado))
        assertEquals(EstadoFila.Buscando, fila(EstadoDispositivo.Buscando).estado)
        assertEquals(EstadoFila.Conectando, fila(EstadoDispositivo.Conectando).estado)
        // Es el único que dice «Reconectando»: el Neo y el Square lo dicen «Conectando».
        assertEquals(EstadoFila.Reconectando, fila(EstadoDispositivo.Reconectando).estado)
        assertEquals(EstadoFila.NoEncontrado, fila(EstadoDispositivo.NoEncontrado).estado)
    }

    @Test
    fun conectando_con_lo_encontrado_sigue_deshabilitando_buscar() {
        // Pulsar «Buscar» al conectar con lo encontrado cortaría esa conexión para empezar de cero.
        val e = estadoDeDispositivos(rodaje(neo = EstadoDispositivo.Conectando, configurados = false), Permiso.Concedido)
        assertTrue(e.buscandoNeo)
        assertFalse(e.buscandoSquare)
        val encontrado = estadoDeDispositivos(rodaje(neo = EstadoDispositivo.NoEncontrado, configurados = false), Permiso.Concedido)
        assertFalse(encontrado.buscandoNeo)
    }

    @Test
    fun buscando_deshabilita_solo_buscar_pulsometro() {
        val e = estadoDeDispositivos(conBanda(EstadoDispositivo.Buscando, configurada = false), Permiso.Concedido)
        assertTrue(e.buscandoPulsometro)
        assertEquals(AccionConexiones.Buscar, e.accion)
    }

    @Test
    fun banda_no_encontrada_da_el_aviso_de_error_solo_en_dispositivos() {
        val c = conBanda(EstadoDispositivo.NoEncontrado, configurada = false)
        assertEquals(listOf(Aviso.PulsometroNoEncontrado), estadoDeDispositivos(c, Permiso.Concedido).avisos)
    }

    @Test
    fun banda_reconectando_no_da_aviso_de_error_en_dispositivos() {
        assertTrue(estadoDeDispositivos(conBanda(EstadoDispositivo.Reconectando), Permiso.Concedido).avisos.isEmpty())
    }

    @Test
    fun sin_permiso_o_sin_bluetooth_los_avisos_y_acciones_son_los_de_buscar_dispositivos() {
        val sinPermiso = estadoDeDispositivos(conBanda(EstadoDispositivo.SinConexion, configurada = false), Permiso.Denegado)
        assertEquals(AccionConexiones.AbrirAjustes, sinPermiso.accion)
        assertEquals(listOf(Aviso.SinPermiso), sinPermiso.avisos)
        val sinBluetooth = estadoDeDispositivos(conBanda(EstadoDispositivo.SinConexion, configurada = false).copy(bluetoothEncendido = false), Permiso.Concedido)
        assertEquals(AccionConexiones.ActivarBluetooth, sinBluetooth.accion)
        assertEquals(listOf(Aviso.BluetoothApagado), sinBluetooth.avisos)
    }

    // --- Ventilador (spec `device-connection`, «Buscar el ventilador aparte»; change `ventilador-headwind`) ---

    private val ventiladorConfigurado = DispositivoConfigurado("DD:DD", "HEADWIND 4F2A")

    private fun conVentilador(e: EstadoDispositivo, configurado: Boolean = true) = rodaje(EstadoDispositivo.Conectado, EstadoDispositivo.Conectado).copy(ventilador = e, ventiladorConfigurado = ventiladorConfigurado.takeIf { configurado })

    @Test
    fun sin_ventilador_configurado_la_fila_y_el_boton_se_ven_sin_error() {
        val e = estadoDeDispositivos(conVentilador(EstadoDispositivo.SinConexion, configurado = false), Permiso.Concedido)
        assertEquals(FilaDispositivoUi(EstadoFila.SinConexion, null), e.ventilador)
        assertFalse(e.buscandoVentilador)
        assertTrue(e.avisos.isEmpty())
    }

    @Test
    fun los_estados_de_la_fila_del_ventilador() {
        fun fila(e: EstadoDispositivo) = estadoDeDispositivos(conVentilador(e), Permiso.Concedido).ventilador
        assertEquals(FilaDispositivoUi(EstadoFila.Conectado, "HEADWIND 4F2A"), fila(EstadoDispositivo.Conectado))
        assertEquals(EstadoFila.Buscando, fila(EstadoDispositivo.Buscando).estado)
        assertEquals(EstadoFila.Reconectando, fila(EstadoDispositivo.Reconectando).estado)
        assertEquals(EstadoFila.NoEncontrado, fila(EstadoDispositivo.NoEncontrado).estado)
    }

    @Test
    fun buscando_deshabilita_solo_buscar_ventilador() {
        val e = estadoDeDispositivos(conVentilador(EstadoDispositivo.Buscando, configurado = false), Permiso.Concedido)
        assertTrue(e.buscandoVentilador)
        assertFalse(e.buscandoPulsometro)
        assertEquals(AccionConexiones.Buscar, e.accion)
    }

    @Test
    fun ventilador_no_encontrado_da_el_aviso_de_error_y_reconectando_no() {
        val c = conVentilador(EstadoDispositivo.NoEncontrado, configurado = false)
        assertEquals(listOf(Aviso.VentiladorNoEncontrado), estadoDeDispositivos(c, Permiso.Concedido).avisos)
        assertTrue(estadoDeDispositivos(conVentilador(EstadoDispositivo.Reconectando), Permiso.Concedido).avisos.isEmpty())
    }

    @Test
    fun el_nombre_anunciado_llega_al_estado_y_por_defecto_es_square_shift() {
        assertEquals("Smart Bike", estadoDeDispositivos(rodaje(), Permiso.Concedido).nombreAnunciado)
        assertEquals("KICKR BIKE PRO 669C", estadoDeDispositivos(rodaje().copy(nombreAnunciado = "KICKR BIKE PRO 669C"), Permiso.Concedido).nombreAnunciado)
    }

    // Batería baja del Square (spec `device-connection`, «Batería baja del Square en Dispositivos»)

    @Test
    fun bateria_baja_con_el_square_conectado_da_el_nivel_para_el_aviso() {
        val c = rodaje(EstadoDispositivo.Conectado, EstadoDispositivo.Conectado).copy(bateriaSquare = 15)
        assertEquals(15, estadoDeDispositivos(c, Permiso.Concedido).bateriaSquareBajaPct)
        assertEquals(20, estadoDeDispositivos(c.copy(bateriaSquare = 20), Permiso.Concedido).bateriaSquareBajaPct)
    }

    @Test
    fun bateria_normal_o_sin_lectura_no_da_aviso() {
        val c = rodaje(EstadoDispositivo.Conectado, EstadoDispositivo.Conectado)
        assertNull(estadoDeDispositivos(c.copy(bateriaSquare = 21), Permiso.Concedido).bateriaSquareBajaPct)
        assertNull(estadoDeDispositivos(c.copy(bateriaSquare = 100), Permiso.Concedido).bateriaSquareBajaPct)
        assertNull(estadoDeDispositivos(c, Permiso.Concedido).bateriaSquareBajaPct)
    }

    @Test
    fun bateria_baja_con_el_square_sin_conexion_no_da_aviso() {
        val c = rodaje(EstadoDispositivo.Conectado, EstadoDispositivo.SinConexion).copy(bateriaSquare = 15)
        assertNull(estadoDeDispositivos(c, Permiso.Concedido).bateriaSquareBajaPct)
        assertNull(estadoDeDispositivos(c.copy(square = EstadoDispositivo.Reconectando), Permiso.Concedido).bateriaSquareBajaPct)
    }

    @Test
    fun bateria_baja_no_cambia_ni_el_estado_ni_los_avisos_de_error() {
        val c = rodaje(EstadoDispositivo.Conectado, EstadoDispositivo.Conectado)
        val baja = estadoDeDispositivos(c.copy(bateriaSquare = 15), Permiso.Concedido)
        val normal = estadoDeDispositivos(c.copy(bateriaSquare = 80), Permiso.Concedido)
        assertEquals(normal.square, baja.square)
        assertEquals(normal.avisos, baja.avisos)
        assertEquals(normal.accion, baja.accion)
    }
}
