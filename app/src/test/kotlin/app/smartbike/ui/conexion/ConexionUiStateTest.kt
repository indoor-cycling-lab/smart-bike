package app.smartbike.ui.conexion

import app.smartbike.ble.EstadoNombre
import app.smartbike.datos.AlmacenConfiguracion
import app.smartbike.dominio.AjustesConexion
import app.smartbike.dominio.Configuracion
import app.smartbike.dominio.Configurados
import app.smartbike.dominio.DispositivoConfigurado
import app.smartbike.dominio.EstadoMarcha
import app.smartbike.dominio.Identidad
import app.smartbike.dominio.ModoConexion
import app.smartbike.dominio.Publicacion
import app.smartbike.plataforma.EstadoMando
import app.smartbike.plataforma.EstadoPlataforma
import app.smartbike.sesion.EstadoDispositivo
import app.smartbike.sesion.EstadoRodaje
import app.smartbike.sesion.Simulacion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/** Spec `platform-settings` sobre el estado de Conexión y lo que cambia al elegir. */
@OptIn(ExperimentalCoroutinesApi::class)
class ConexionUiStateTest {

    private class AjustesFalsos : AlmacenConfiguracion {
        override val configuracion = MutableStateFlow(Configuracion())
        override suspend fun actualizarConfiguracion(cambio: (Configuracion) -> Configuracion) {
            configuracion.value = cambio(configuracion.value)
        }
    }

    private val dispatcher = StandardTestDispatcher()

    @BeforeEach
    fun antes() = Dispatchers.setMain(dispatcher)

    @AfterEach
    fun despues() = Dispatchers.resetMain()

    private val m = EstadoMarcha.Numerada(12, 24)
    private val todo = Configurados(rodillo = true, mando = true, pulsometro = true)

    private fun rodaje(
        modo: ModoConexion = ModoConexion.FTMS_WIFI,
        configurados: Configurados = todo,
        plataforma: EstadoPlataforma = EstadoPlataforma.Esperando,
        mando: EstadoMando = EstadoMando.Esperando,
        usoRodillo: Boolean = false,
        usoPulso: Boolean = false,
        pulso: Int? = null,
        neo: EstadoDispositivo = EstadoDispositivo.Conectado,
        square: EstadoDispositivo = EstadoDispositivo.Conectado,
        bluetooth: Boolean = true,
    ) = EstadoRodaje(
        neo = neo,
        square = square,
        plataforma = plataforma,
        mando = mando,
        servicios = Publicacion.de(modo, configurados),
        usoRodillo = usoRodillo,
        usoPulso = usoPulso,
        pulso = pulso,
        squareConfigurado = if (configurados.mando) DispositivoConfigurado("BB:BB", "SQUARE 1234") else null,
        bluetoothEncendido = bluetooth,
        marcha = m,
        referencia = m,
        simulacion = Simulacion(0.0, 0.0, null),
    )

    private fun c(modo: ModoConexion = ModoConexion.FTMS_WIFI, identidad: Identidad = Identidad.SMART_BIKE) = Configuracion(conexion = AjustesConexion(modo, identidad))

    @Test
    fun por_defecto_los_tres_servicios_esperan_en_wifi_mas_bluetooth() {
        // Spec `platform-settings`, «Abrir Conexión» y «Por dónde va cada servicio».
        val e = estadoDeConexion(c(), rodaje(), "OnePlus 8")
        assertEquals(ServicioUi(EstadoServicio.Esperando, Via.Wifi, AyudaServicio.EmparejarRodillo), e.rodillo)
        assertEquals(ServicioUi(EstadoServicio.Esperando, Via.Bluetooth, AyudaServicio.MandoBluetooth), e.mando)
        assertEquals(ServicioUi(EstadoServicio.Esperando, Via.Wifi, AyudaServicio.PulsoElegir), e.pulso)
        assertTrue(e.muestraNombre)
        assertFalse(e.sinRodillo)
    }

    @Test
    fun conectado_solo_si_la_app_de_entrenamiento_usa_el_rodillo() {
        // Spec `platform-settings`, «Rodillo conectado y pulso sin usar» (decisión 6: estado por suscripción).
        val sinUso = estadoDeConexion(c(), rodaje(plataforma = EstadoPlataforma.Conectado("192.168.0.10")))
        assertEquals(EstadoServicio.Esperando, sinUso.rodillo.estado)
        val e = estadoDeConexion(c(), rodaje(plataforma = EstadoPlataforma.Conectado("192.168.0.10"), usoRodillo = true))
        assertEquals(EstadoServicio.Conectado, e.rodillo.estado)
        assertEquals(EstadoServicio.Esperando, e.pulso.estado)
    }

    @Test
    fun pulso_enviando_y_sin_lectura() {
        val conectado = EstadoPlataforma.Conectado("192.168.0.10")
        assertEquals(EstadoServicio.Enviando, estadoDeConexion(c(), rodaje(plataforma = conectado, usoRodillo = true, usoPulso = true, pulso = 132)).pulso.estado)
        val sin = estadoDeConexion(c(), rodaje(plataforma = conectado, usoRodillo = true, usoPulso = true, pulso = null)).pulso
        assertEquals(ServicioUi(EstadoServicio.SinLectura, Via.Wifi, AyudaServicio.BandaSinLectura), sin)
    }

    @Test
    fun servicios_no_ofrecidos() {
        // Spec `platform-settings`, «Servicio no ofrecido».
        val sinBanda = estadoDeConexion(c(), rodaje(configurados = todo.copy(pulsometro = false)))
        assertEquals(ServicioUi(EstadoServicio.NoSeOfrece, null, AyudaServicio.BuscarPulso), sinBanda.pulso)
        val sinSquare = estadoDeConexion(c(), rodaje(configurados = todo.copy(mando = false)))
        assertEquals(ServicioUi(EstadoServicio.NoSeOfrece, null, AyudaServicio.BuscarMando), sinSquare.mando)
        val primerUso = estadoDeConexion(c(), rodaje(configurados = Configurados(), plataforma = EstadoPlataforma.NoSeOfrece))
        assertTrue(primerUso.sinRodillo)
        listOf(primerUso.rodillo, primerUso.mando, primerUso.pulso).forEach { assertEquals(EstadoServicio.NoSeOfrece, it.estado) }
    }

    @Test
    fun en_zwift_ride_el_mando_va_siempre_aunque_no_haya_square() {
        // Spec `platform-settings`, «Sin Square con Solo wifi».
        val e = estadoDeConexion(c(ModoConexion.ZWIFT_RIDE), rodaje(ModoConexion.ZWIFT_RIDE, todo.copy(mando = false)))
        assertEquals(ServicioUi(EstadoServicio.Esperando, Via.DentroWifi, AyudaServicio.MandoSinSquare), e.mando)
    }

    @Test
    fun solo_bluetooth_sin_nombre_elegible_y_todo_por_bluetooth() {
        val e = estadoDeConexion(c(ModoConexion.FTMS_BLUETOOTH, Identidad.KICKR_BIKE_PRO), rodaje(ModoConexion.FTMS_BLUETOOTH))
        assertFalse(e.muestraNombre)
        assertEquals(ServicioUi(EstadoServicio.Esperando, Via.Bluetooth, AyudaServicio.EmparejarRodilloMovil), e.rodillo)
        assertEquals(ServicioUi(EstadoServicio.Esperando, Via.DentroBluetooth, AyudaServicio.MandoDentro), e.mando)
        assertEquals(AyudaServicio.PulsoElegirMovil, e.pulso.ayuda)
    }

    @Test
    fun sin_wifi_el_rodillo_y_el_pulso_lo_dicen_y_el_mando_aparte_no() {
        // Spec `platform-settings`, «Sin wifi» del rodillo y del pulso.
        val e = estadoDeConexion(c(), rodaje(plataforma = EstadoPlataforma.SinWifi))
        assertEquals(ServicioUi(EstadoServicio.SinWifi, Via.Wifi, AyudaServicio.SinWifi), e.rodillo)
        assertEquals(EstadoServicio.SinWifi, e.pulso.estado)
        assertEquals(EstadoServicio.Esperando, e.mando.estado)
    }

    @Test
    fun bluetooth_apagado_con_el_mando_aparte() {
        // Spec `platform-settings`, «Bluetooth apagado con el mando aparte».
        val e = estadoDeConexion(c(), rodaje(plataforma = EstadoPlataforma.Conectado("1.2.3.4"), usoRodillo = true, mando = EstadoMando.SinConexion, bluetooth = false))
        assertEquals(EstadoServicio.SinBluetooth, e.mando.estado)
        assertEquals(EstadoServicio.Conectado, e.rodillo.estado)
        assertTrue(e.avisoBluetooth)
    }

    @Test
    fun no_visible_avisa_arriba_sin_ayuda_de_emparejar() {
        val e = estadoDeConexion(c(), rodaje(plataforma = EstadoPlataforma.NoVisible))
        assertTrue(e.noVisible)
        assertEquals(ServicioUi(EstadoServicio.NoVisible, Via.Wifi, null), e.rodillo)
    }

    @Test
    fun reconectando_se_sigue_publicando_con_una_nota() {
        // Spec `platform-settings`, «Neo reconectando» y «Square reconectando».
        val e = estadoDeConexion(
            c(),
            rodaje(plataforma = EstadoPlataforma.Conectado("1.2.3.4"), usoRodillo = true, mando = EstadoMando.Conectado, neo = EstadoDispositivo.Reconectando, square = EstadoDispositivo.Reconectando),
        )
        assertEquals(ServicioUi(EstadoServicio.Conectado, Via.Wifi, AyudaServicio.NeoReconectando), e.rodillo)
        assertEquals(ServicioUi(EstadoServicio.Conectado, Via.Bluetooth, AyudaServicio.SquareReconectando), e.mando)
    }

    private fun cn(modo: ModoConexion, porBluetooth: Boolean) = Configuracion(conexion = AjustesConexion(modo, nombrePorBluetooth = porBluetooth))

    private val renombrado = EstadoNombre.Renombrado("Smart Bike", "OnePlus 8")

    @Test
    fun la_casilla_se_muestra_solo_con_un_modo_que_publica_por_bluetooth() {
        assertTrue(estadoDeConexion(cn(ModoConexion.FTMS_WIFI, false), rodaje()).muestraCasilla)
        assertTrue(estadoDeConexion(cn(ModoConexion.FTMS_BLUETOOTH, false), rodaje(ModoConexion.FTMS_BLUETOOTH)).muestraCasilla)
        assertFalse(estadoDeConexion(cn(ModoConexion.ZWIFT_RIDE, true), rodaje(ModoConexion.ZWIFT_RIDE)).muestraCasilla)
    }

    @Test
    fun el_selector_con_solo_bluetooth_solo_con_la_casilla_marcada_y_la_casilla_va_primero() {
        val sin = estadoDeConexion(cn(ModoConexion.FTMS_BLUETOOTH, false), rodaje(ModoConexion.FTMS_BLUETOOTH))
        assertFalse(sin.muestraNombre)
        val con = estadoDeConexion(cn(ModoConexion.FTMS_BLUETOOTH, true), rodaje(ModoConexion.FTMS_BLUETOOTH), "Smart Bike", renombrado)
        assertTrue(con.muestraNombre)
        assertTrue(con.casillaPrimero)
        assertFalse(estadoDeConexion(cn(ModoConexion.FTMS_WIFI, true), rodaje()).casillaPrimero)
    }

    @Test
    fun renombrado_las_ayudas_nombran_el_nombre_elegido_y_la_casilla_el_original() {
        val e = estadoDeConexion(cn(ModoConexion.FTMS_BLUETOOTH, true), rodaje(ModoConexion.FTMS_BLUETOOTH), "Smart Bike", renombrado)
        assertTrue(e.renombrado)
        assertEquals(AyudaServicio.EmparejarRodillo, e.rodillo.ayuda)
        assertEquals("OnePlus 8", e.nombreOriginalMovil)
        assertFalse(e.noSePudoRenombrar)
        val aparte = estadoDeConexion(cn(ModoConexion.FTMS_WIFI, true), rodaje(), "Smart Bike", renombrado)
        assertEquals(AyudaServicio.MandoBluetoothNombre, aparte.mando.ayuda)
    }

    @Test
    fun sin_la_casilla_las_ayudas_nombran_el_movil() {
        val e = estadoDeConexion(cn(ModoConexion.FTMS_BLUETOOTH, false), rodaje(ModoConexion.FTMS_BLUETOOTH), "OnePlus 8")
        assertFalse(e.renombrado)
        assertEquals(AyudaServicio.EmparejarRodilloMovil, e.rodillo.ayuda)
        val aparte = estadoDeConexion(cn(ModoConexion.FTMS_WIFI, false), rodaje(), "OnePlus 8")
        assertEquals(AyudaServicio.MandoBluetooth, aparte.mando.ayuda)
    }

    @Test
    fun no_se_pudo_renombrar_avisa_y_las_ayudas_nombran_el_movil() {
        val e = estadoDeConexion(cn(ModoConexion.FTMS_BLUETOOTH, true), rodaje(ModoConexion.FTMS_BLUETOOTH), "OnePlus 8", EstadoNombre.NoSePudo("Android lo rechaza"))
        assertTrue(e.noSePudoRenombrar)
        assertFalse(e.renombrado)
        assertEquals(AyudaServicio.EmparejarRodilloMovil, e.rodillo.ayuda)
        assertEquals("OnePlus 8", e.nombreOriginalMovil)
        assertFalse(estadoDeConexion(cn(ModoConexion.ZWIFT_RIDE, true), rodaje(ModoConexion.ZWIFT_RIDE), "OnePlus 8", EstadoNombre.NoSePudo("x")).noSePudoRenombrar)
    }

    @Test
    fun elegir_modo_y_nombre_se_guarda() = runTest(dispatcher) {
        val ajustes = AjustesFalsos()
        val vm = ConexionViewModel(ajustes, MutableStateFlow(rodaje()), MutableStateFlow(EstadoNombre.Propio))
        backgroundScope.launch { vm.estado.collect {} }
        vm.modo(ModoConexion.ZWIFT_RIDE)
        vm.identidad(Identidad.KICKR_BIKE_PRO)
        vm.nombrePorBluetooth(true)
        runCurrent()
        assertEquals(AjustesConexion(ModoConexion.ZWIFT_RIDE, Identidad.KICKR_BIKE_PRO, nombrePorBluetooth = true), ajustes.configuracion.value.conexion)
        assertEquals(ModoConexion.ZWIFT_RIDE, vm.estado.value.conexion.modo)
    }
}
