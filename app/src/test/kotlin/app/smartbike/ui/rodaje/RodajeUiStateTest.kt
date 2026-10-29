package app.smartbike.ui.rodaje

import app.smartbike.ble.BuscadorDispositivos
import app.smartbike.ble.ConexionNeo
import app.smartbike.ble.ConexionPulso
import app.smartbike.ble.ConexionSquare
import app.smartbike.ble.ConexionVentilador
import app.smartbike.ble.EstadoBluetooth
import app.smartbike.ble.EstadoEnlace
import app.smartbike.ble.NombreDelAdaptador
import app.smartbike.ble.Notificacion
import app.smartbike.ble.RenombradorBluetooth
import app.smartbike.ble.ResultadoBusqueda
import app.smartbike.datos.AlmacenConfiguracion
import app.smartbike.datos.AlmacenEstado
import app.smartbike.datos.RegistroSesion
import app.smartbike.dominio.Accion
import app.smartbike.dominio.AjustesConexion
import app.smartbike.dominio.AvisoVentilador
import app.smartbike.dominio.ComandoRodillo
import app.smartbike.dominio.Configuracion
import app.smartbike.dominio.Configurados
import app.smartbike.dominio.DatoRodaje
import app.smartbike.dominio.Desarrollos
import app.smartbike.dominio.DispositivoConfigurado
import app.smartbike.dominio.EstadoGuardado
import app.smartbike.dominio.EstadoMarcha
import app.smartbike.dominio.EstadoVentilador
import app.smartbike.dominio.GuiaInicio
import app.smartbike.dominio.GuiaPantalla
import app.smartbike.dominio.IdentidadDispositivo
import app.smartbike.dominio.IdiomaApp
import app.smartbike.dominio.ModoCambio
import app.smartbike.dominio.ModoConexion
import app.smartbike.dominio.ModoControl
import app.smartbike.dominio.ModoVentilador
import app.smartbike.dominio.PasoApp
import app.smartbike.dominio.PasoDispositivos
import app.smartbike.dominio.Publicacion
import app.smartbike.dominio.ServiciosPublicados
import app.smartbike.dominio.Terreno
import app.smartbike.dominio.TopeMarcha
import app.smartbike.plataforma.ConexionFtms
import app.smartbike.plataforma.EstadoMando
import app.smartbike.plataforma.EstadoPlataforma
import app.smartbike.plataforma.EstadoTransporte
import app.smartbike.plataforma.ReceptorPlataforma
import app.smartbike.plataforma.TransporteDircon
import app.smartbike.plataforma.zwift.MandoZwift
import app.smartbike.plataforma.zwift.MandoZwiftBluetooth
import app.smartbike.protocolo.NotificacionGatt
import app.smartbike.protocolo.ServidorGatt
import app.smartbike.protocolo.SquareButton
import app.smartbike.red.EstadoWifi
import app.smartbike.sesion.EstadoDispositivo
import app.smartbike.sesion.EstadoDispositivo.Buscando
import app.smartbike.sesion.EstadoDispositivo.Conectado
import app.smartbike.sesion.EstadoDispositivo.Conectando
import app.smartbike.sesion.EstadoDispositivo.NoEncontrado
import app.smartbike.sesion.EstadoDispositivo.Reconectando
import app.smartbike.sesion.EstadoDispositivo.SinConexion
import app.smartbike.sesion.EstadoRodaje
import app.smartbike.sesion.Sesion
import app.smartbike.sesion.Simulacion
import app.smartbike.sesion.Vibrador
import app.smartbike.ui.componentes.Enlace
import app.smartbike.ui.componentes.EstadoChip
import app.smartbike.ui.componentes.TonoDato
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RodajeUiStateTest {

    private fun rodaje(neo: EstadoDispositivo = Conectado, square: EstadoDispositivo = Conectado, marcha: Int = 14) = EstadoRodaje(
        neo = neo,
        square = square,
        marcha = EstadoMarcha.Numerada(marcha, 24),
        referencia = EstadoMarcha.Numerada(12, 24),
        potencia = 247,
        cadencia = 91,
        simulacion = Simulacion(0.0, 2.0, ComandoRodillo.Pendiente(2.0)),
    )

    @Test
    fun sin_zwift() {
        val p = estadoDeRodaje(rodaje(), null, AvisoUi.Marcha(Accion.MarchaMas(1), 1))
        assertEquals(14, p.marcha)
        assertEquals(24, p.total)
        assertEquals(12, p.referencia)
        assertEquals(247, p.potencia)
        assertEquals(91, p.cadencia)
        assertEquals(EnlaceUi(Enlace.Neo, EstadoChip.Ok, null), p.enlace(Enlace.Neo))
        assertEquals(EnlaceUi(Enlace.Square, EstadoChip.Ok, null), p.enlace(Enlace.Square))
        assertEquals(PieUi.Nada, p.pie)
    }

    private val shimano = Desarrollos.SHIMANO_105

    private fun conShimano(m: EstadoMarcha.PlatoPinon, modo: ModoCambio = ModoCambio.AUTOMATICO) = rodaje().copy(
        marcha = m,
        referencia = shimano.referencia,
        transmision = shimano,
        modoCambio = modo,
    )

    @Test
    fun la_salida_va_en_el_estado_y_no_cambia_nada_mas() {
        val base = estadoDeRodaje(rodaje(), null, null)
        assertEquals(SalidaUi.Ninguna, base.salida)
        for (salida in listOf(SalidaUi.Confirmar, SalidaUi.Cerrando)) {
            val p = estadoDeRodaje(rodaje(), null, null, salida = salida)
            assertEquals(salida, p.salida)
            assertEquals(base, p.copy(salida = SalidaUi.Ninguna))
        }
    }

    @Test
    fun marcha_52x17() {
        // Spec `ride-screen`, «Marcha 52×17» y «Shimano en automático».
        val p = estadoDeRodaje(conShimano(EstadoMarcha.PlatoPinon(52, 17)), null, null)
        assertEquals(PlatosUi(52, 17, listOf(36, 52), shimano.pinones), p.platos)
        assertEquals(shimano, p.transmision)
        assertEquals(ModoCambio.AUTOMATICO, p.modoCambio)
    }

    @Test
    fun tope_de_plato() {
        val p = estadoDeRodaje(conShimano(EstadoMarcha.PlatoPinon(52, 17), ModoCambio.MANUAL), TopeMarcha.Plato(52), null)
        assertEquals(TopeMarcha.Plato(52), p.tope)
        assertEquals(ModoCambio.MANUAL, p.modoCambio)
    }

    @Test
    fun lineal_sin_platos() {
        assertNull(estadoDeRodaje(rodaje(), null, null).platos)
    }

    @Test
    fun tope_lineal() {
        val p = estadoDeRodaje(rodaje(marcha = 24), TopeMarcha.MasDura, null)
        assertEquals(24, p.marcha)
        assertEquals(TopeMarcha.MasDura, p.tope)
    }

    @Test
    fun square_sin_conexion() {
        val p = estadoDeRodaje(rodaje(square = SinConexion), null, null)
        assertEquals(EnlaceUi(Enlace.Square, EstadoChip.Sin, EnlaceUi.EnPalabra.SinConexion), p.enlace(Enlace.Square))
        assertEquals(247, p.potencia)
        assertEquals(PieUi.TocarParaConectar, p.pie)
    }

    @Test
    fun neo_sin_conexion_quita_los_datos() {
        val p = estadoDeRodaje(rodaje(neo = Reconectando), null, null)
        assertNull(p.potencia)
        assertNull(p.cadencia)
        assertEquals(14, p.marcha)
    }

    @Test
    fun sin_dispositivos() {
        val p = estadoDeRodaje(rodaje(neo = SinConexion, square = SinConexion, marcha = 12), null, null)
        assertEquals(EstadoChip.Sin, p.enlace(Enlace.Neo)?.estado)
        assertEquals(EstadoChip.Sin, p.enlace(Enlace.Square)?.estado)
        assertNull(p.potencia)
        assertEquals(PieUi.TocarParaConectar, p.pie)
    }

    // Tabla de la decisión 3 del design.md de `conexion-automatica-en-segundo-plano`.

    private fun conectando(e: Enlace) = EnlaceUi(e, EstadoChip.Esperando, EnlaceUi.EnPalabra.Conectando)
    private fun sinConexion(e: Enlace) = EnlaceUi(e, EstadoChip.Sin, EnlaceUi.EnPalabra.SinConexion)

    @Test
    fun neo_conectando_al_abrir() {
        val p = estadoDeRodaje(rodaje(neo = Conectando), null, null)
        assertEquals(conectando(Enlace.Neo), p.enlace(Enlace.Neo))
        assertNull(p.potencia)
        assertNull(p.cadencia)
        assertEquals(PieUi.DespertarNeo, p.pie)
    }

    @Test
    fun square_reconectando() {
        val p = estadoDeRodaje(rodaje(square = Reconectando), null, null)
        assertEquals(conectando(Enlace.Square), p.enlace(Enlace.Square))
        assertEquals(247, p.potencia)
        assertEquals(PieUi.DespertarSquare, p.pie)
    }

    @Test
    fun los_dos_conectando() {
        val p = estadoDeRodaje(rodaje(neo = Conectando, square = Reconectando), null, null)
        assertEquals(conectando(Enlace.Neo), p.enlace(Enlace.Neo))
        assertEquals(conectando(Enlace.Square), p.enlace(Enlace.Square))
        assertEquals(PieUi.DespertarLosDos, p.pie)
    }

    @Test
    fun buscando_tambien_dice_conectando() {
        val p = estadoDeRodaje(rodaje(neo = Buscando, square = Buscando), null, null)
        assertEquals(conectando(Enlace.Neo), p.enlace(Enlace.Neo))
        assertEquals(PieUi.DespertarLosDos, p.pie)
    }

    @Test
    fun no_encontrado_dice_sin_conexion() {
        val p = estadoDeRodaje(rodaje(neo = NoEncontrado), null, null)
        assertEquals(sinConexion(Enlace.Neo), p.enlace(Enlace.Neo))
        assertEquals(PieUi.TocarParaConectar, p.pie)
    }

    @Test
    fun la_ayuda_prima_sobre_el_que_falta_sin_intento() {
        val p = estadoDeRodaje(rodaje(neo = SinConexion, square = Reconectando), null, null)
        assertEquals(sinConexion(Enlace.Neo), p.enlace(Enlace.Neo))
        assertEquals(PieUi.DespertarSquare, p.pie)
    }

    @Test
    fun aviso_de_accion_mientras_reconecta() {
        // El aviso lo muestra el pie en lugar de la ayuda; al caducar, vuelve la ayuda.
        val p = estadoDeRodaje(rodaje(neo = Reconectando), null, AvisoUi.Marcha(Accion.MarchaMas(1), 1))
        assertEquals(AvisoUi.Marcha(Accion.MarchaMas(1), 1), p.aviso)
        assertEquals(PieUi.DespertarNeo, p.pie)
    }

    // --- Aviso y tope: 1,5 s y uno nuevo sustituye al anterior --------------------------------------------------

    private val dispatcher = StandardTestDispatcher()

    @BeforeEach
    fun main() = Dispatchers.setMain(dispatcher)

    @AfterEach
    fun fin() = Dispatchers.resetMain()

    private val notificaciones = MutableSharedFlow<Notificacion>(extraBufferCapacity = 16)
    private val contadores = IntArray(SquareButton.entries.size) { 2 }

    private fun botones(vararg cambios: Pair<SquareButton, Int>): Notificacion {
        cambios.forEach { (b, n) -> contadores[b.ordinal] = (contadores[b.ordinal] + n) % 16 }
        val nibbles = IntArray(22) { if (it < 2) 0 else contadores[SquareButton.entries.first { b -> b.contador == it - 2 }.ordinal] }
        return Notificacion("0x45", ByteArray(11) { ((nibbles[it * 2] shl 4) or nibbles[it * 2 + 1]).toByte() }, dispatcher.scheduler.currentTime)
    }

    private fun sesion(scope: kotlinx.coroutines.CoroutineScope, marcha: Int): Sesion {
        val square = object : ConexionSquare {
            override val estado = MutableStateFlow(EstadoEnlace.Listo)
            override val notificaciones: Flow<Notificacion> = this@RodajeUiStateTest.notificaciones
            override val nivelBateria: Flow<Int> = emptyFlow()
            override suspend fun conectar(direccion: String) = true
            override suspend fun conectarEnSegundoPlano(direccion: String) = true
            override suspend fun desconectar() = Unit
            override fun cancelarConexionPendiente() = Unit
        }
        val neo = object : ConexionNeo {
            override val estado = MutableStateFlow(EstadoEnlace.Desconectado)
            override val notificaciones: Flow<Notificacion> = emptyFlow()
            override suspend fun conectar(direccion: String) = true
            override suspend fun conectarEnSegundoPlano(direccion: String) = true
            override suspend fun desconectar() = Unit
            override fun cancelarConexionPendiente() = Unit
            override fun enviar(comando: ByteArray, alConfirmar: ((Boolean) -> Unit)?) = Unit
            override suspend fun enviarYEsperar(comando: ByteArray, limiteMs: Long) = true
        }
        val almacen = object : AlmacenEstado {
            override suspend fun leer() = EstadoGuardado(ultimaMarcha = marcha)
            override suspend fun actualizar(cambio: (EstadoGuardado) -> EstadoGuardado) = Unit
        }
        val buscador = object : BuscadorDispositivos {
            override fun buscar(prefijos: Set<String>, limiteMs: Long): Flow<ResultadoBusqueda> = emptyFlow()
            override fun buscarPulsometro(limiteMs: Long): Flow<ResultadoBusqueda> = emptyFlow()
        }
        val ventilador = object : ConexionVentilador {
            override val estado = MutableStateFlow(EstadoEnlace.Desconectado)
            override val notificaciones: Flow<Notificacion> = emptyFlow()
            override suspend fun conectar(direccion: String) = true
            override suspend fun conectarEnSegundoPlano(direccion: String) = true
            override suspend fun desconectar() = Unit
            override fun cancelarConexionPendiente() = Unit
            override fun enviar(orden: ByteArray) = Unit
        }
        val pulsometro = object : ConexionPulso {
            override val estado = MutableStateFlow(EstadoEnlace.Desconectado)
            override val notificaciones: Flow<Notificacion> = emptyFlow()
            override suspend fun conectar(direccion: String) = true
            override suspend fun conectarEnSegundoPlano(direccion: String) = true
            override suspend fun desconectar() = Unit
            override fun cancelarConexionPendiente() = Unit
        }
        val bluetooth = object : EstadoBluetooth {
            override val encendido = MutableStateFlow(true)
            override val permiso = MutableStateFlow(true)
            override val permisoAnunciar = MutableStateFlow(true)
        }
        val vibrador = object : Vibrador {
            override fun corta() = Unit
            override fun doble() = Unit
            override fun larga() = Unit
            override fun triple() = Unit
        }
        val ajustes = object : AlmacenConfiguracion {
            override val configuracion = MutableStateFlow(Configuracion())
            override suspend fun actualizarConfiguracion(cambio: (Configuracion) -> Configuracion) {
                configuracion.value = cambio(configuracion.value)
            }
        }
        val wifi = object : EstadoWifi {
            override val direccion = MutableStateFlow<String?>(null)
            override fun mantenerActivo(activo: Boolean) = Unit
        }
        val transporte = object : TransporteDircon {
            override val estado = MutableStateFlow<EstadoTransporte>(EstadoTransporte.Parado)
            override fun publicar(servidor: ServidorGatt, identidad: IdentidadDispositivo, nombre: String) = Unit
            override fun parar() = Unit
            override fun notificar(n: NotificacionGatt) = Unit
        }
        val registro = RegistroSesion(relojMonotonico = { 0 }, relojPared = { 0 })
        val mando = MandoZwift(scope, registro, transporte)
        val conexion = { _: ServiciosPublicados, _: Boolean, receptor: ReceptorPlataforma, ambito: CoroutineScope ->
            ConexionFtms(ModoConexion.FTMS_WIFI, registro, transporte, "", receptor, mando = MandoZwiftBluetooth(ambito, mando, transporte, bluetooth) { true })
        }
        val adaptador = object : NombreDelAdaptador {
            override fun actual(): String? = "OnePlus 8"
            override val confirmado = MutableStateFlow<String?>("OnePlus 8")
            override fun poner(nombre: String) = false
        }
        val renombrador = RenombradorBluetooth(adaptador, almacen, registro)
        return Sesion(buscador, square, neo, pulsometro, ventilador, bluetooth, wifi, conexion, renombrador, registro, almacen, ajustes, vibrador, scope, { dispatcher.scheduler.currentTime }, { 0 })
    }

    @Test
    fun aviso_durante_1_5_s_y_sustituido_por_el_siguiente() = runTest(dispatcher) {
        val vm = RodajeViewModel(sesion(backgroundScope, 14), {})
        backgroundScope.launch { vm.estado.collect {} }
        runCurrent()
        notificaciones.tryEmit(botones())
        notificaciones.tryEmit(botones(SquareButton.PalancaDerecha1 to 2))
        runCurrent()
        assertEquals(AvisoUi.Marcha(Accion.MarchaMas(1), 1), vm.estado.value.aviso)
        advanceTimeBy(1_000)
        notificaciones.tryEmit(botones(SquareButton.PalancaIzquierda1 to 2))
        runCurrent()
        assertEquals(AvisoUi.Marcha(Accion.MarchaMenos(1), -1), vm.estado.value.aviso)
        advanceTimeBy(1_400)
        runCurrent()
        assertEquals(AvisoUi.Marcha(Accion.MarchaMenos(1), -1), vm.estado.value.aviso)
        advanceTimeBy(200)
        runCurrent()
        assertNull(vm.estado.value.aviso)
    }

    @Test
    fun tope_durante_1_5_s_sin_aviso() = runTest(dispatcher) {
        val vm = RodajeViewModel(sesion(backgroundScope, 24), {})
        backgroundScope.launch { vm.estado.collect {} }
        runCurrent()
        notificaciones.tryEmit(botones())
        notificaciones.tryEmit(botones(SquareButton.PalancaDerecha1 to 2))
        runCurrent()
        assertEquals(TopeMarcha.MasDura, vm.estado.value.tope)
        assertNull(vm.estado.value.aviso)
        advanceTimeBy(1_600)
        runCurrent()
        assertNull(vm.estado.value.tope)
    }

    // --- Zwift (spec delta de `ride-screen`) ---

    private fun conZwift(zwift: EstadoPlataforma, modo: ModoControl = ModoControl.SimLibre, neo: EstadoDispositivo = Conectado) = rodaje(neo = neo).copy(plataforma = zwift, simulacion = Simulacion(0.0, 2.0, null, modo = modo))

    private fun enlaces(z: EstadoPlataforma, m: EstadoMando = EstadoMando.Desactivado, neo: EstadoDispositivo = Conectado, square: EstadoDispositivo = Conectado) = estadoDeRodaje(conZwift(z, neo = neo).copy(mando = m, square = square), null, null).enlaces

    private fun config(modo: ModoConexion = ModoConexion.FTMS_WIFI) = Configuracion(conexion = AjustesConexion(modo))

    private val conSquare = Configurados(rodillo = true, mando = true)

    /** El estado de lo publicado con [r] configurados y la app de entrenamiento usando el rodillo ([usoRodillo]) y el pulso. */
    private fun plataforma(
        z: EstadoPlataforma,
        m: EstadoMando = EstadoMando.Desactivado,
        c: Configuracion = Configuracion(),
        r: Configurados = conSquare,
        usoRodillo: Boolean = true,
        usoPulso: Boolean = false,
    ) = estadoDeRodaje(
        conZwift(z).copy(mando = m, servicios = Publicacion.de(c.conexion.modo, r), usoRodillo = usoRodillo, usoPulso = usoPulso),
        null,
        null,
        c,
    ).plataforma

    private fun square(e: EstadoDispositivo, bateria: Int?) = estadoDeRodaje(conZwift(EstadoPlataforma.Conectado("192.168.0.10")).copy(square = e, bateriaSquare = bateria), null, null).enlace(Enlace.Square)

    @Test
    fun el_icono_del_square_con_la_bateria_baja() {
        // Spec `ride-screen`, «Batería baja del Square en la barra de conexiones».
        assertEquals(EnlaceUi(Enlace.Square, EstadoChip.Baja, EnlaceUi.EnPalabra.BateriaBaja), square(Conectado, 15))
        assertEquals(EnlaceUi(Enlace.Square, EstadoChip.Baja, EnlaceUi.EnPalabra.BateriaBaja), square(Conectado, 20))
        assertEquals(EnlaceUi(Enlace.Square, EstadoChip.Ok, null), square(Conectado, 21))
        assertEquals(EnlaceUi(Enlace.Square, EstadoChip.Ok, null), square(Conectado, null))
    }

    @Test
    fun con_el_square_reconectando_o_sin_conexion_no_hay_aviso_de_bateria() {
        assertEquals(EnlaceUi(Enlace.Square, EstadoChip.Esperando, EnlaceUi.EnPalabra.Conectando), square(EstadoDispositivo.Reconectando, 5))
        assertEquals(EnlaceUi(Enlace.Square, EstadoChip.Sin, EnlaceUi.EnPalabra.SinConexion), square(EstadoDispositivo.SinConexion, 5))
    }

    @Test
    fun la_bateria_baja_solo_cambia_el_icono_del_square() {
        val normal = estadoDeRodaje(conZwift(EstadoPlataforma.Conectado("x")).copy(bateriaSquare = 80), null, null)
        val baja = estadoDeRodaje(conZwift(EstadoPlataforma.Conectado("x")).copy(bateriaSquare = 10), null, null)
        assertEquals(normal.copy(enlaces = baja.enlaces), baja)
    }

    @Test
    fun el_estado_de_la_publicacion_en_sus_estados() {
        // Spec `ride-screen`, «Estado de la publicación en Rodaje»: sin nombre de plataforma ni transporte.
        val conectado = plataforma(EstadoPlataforma.Conectado("192.168.0.10"), EstadoMando.Conectado)
        assertEquals(PlataformaUi(EstadoChip.Ok, PlataformaUi.Texto.Conectado, null), conectado)
        assertEquals(PlataformaUi(EstadoChip.Esperando, PlataformaUi.Texto.Esperando, EnlaceUi.EnPalabra.Esperando), plataforma(EstadoPlataforma.Esperando))
        assertEquals(PlataformaUi(EstadoChip.Sin, PlataformaUi.Texto.SinWifi, EnlaceUi.EnPalabra.SinConexion), plataforma(EstadoPlataforma.SinWifi))
        assertEquals(PlataformaUi(EstadoChip.Sin, PlataformaUi.Texto.NoVisible, EnlaceUi.EnPalabra.NoVisible), plataforma(EstadoPlataforma.NoVisible))
        assertEquals(PlataformaUi(EstadoChip.Sin, PlataformaUi.Texto.SinRodillo, null), plataforma(EstadoPlataforma.NoSeOfrece, r = Configurados()))
    }

    @Test
    fun conectado_solo_si_la_app_de_entrenamiento_usa_el_rodillo() {
        // Decisión 6 del design.md de `pestana-conexion`: el estado sale de la suscripción.
        val sinUso = plataforma(EstadoPlataforma.Conectado("x"), EstadoMando.Conectado, usoRodillo = false)
        assertEquals(PlataformaUi.Texto.Esperando, sinUso.texto)
    }

    @Test
    fun sin_pulso_y_sin_mando_ni_pulso() {
        // Spec `ride-screen`, «Pulso sin elegir» y «Sin Square ni pulsómetro configurados».
        val conectado = EstadoPlataforma.Conectado("x")
        val conBanda = conSquare.copy(pulsometro = true)
        assertEquals(PlataformaUi.Texto.ConectadoSinPulso, plataforma(conectado, EstadoMando.Conectado, r = conBanda).texto)
        assertEquals(PlataformaUi.Texto.Conectado, plataforma(conectado, EstadoMando.Conectado, r = conBanda, usoPulso = true).texto)
        assertEquals(PlataformaUi.Texto.ConectadoSinMandoNiPulso, plataforma(conectado, EstadoMando.Esperando, r = conBanda).texto)
        assertEquals(PlataformaUi.Texto.Conectado, plataforma(conectado, EstadoMando.Desactivado, r = Configurados(rodillo = true)).texto)
    }

    @Test
    fun los_enlaces_van_en_orden_fijo_y_sin_zwift_ni_mando() {
        // Spec `ride-screen`, «Conexiones de dispositivos en la barra superior»: Neo y Square; Zwift y el mando salen de la barra.
        for (m in EstadoMando.entries) {
            assertEquals(listOf(Enlace.Neo, Enlace.Square), enlaces(EstadoPlataforma.Conectado("192.168.0.10"), m).map { it.enlace }, "$m")
        }
        assertEquals(listOf(Enlace.Neo, Enlace.Square), enlaces(EstadoPlataforma.SinWifi).map { it.enlace })
    }

    @Test
    fun el_rodillo_conectado_y_el_mando_aparte_sin_conectar_dice_sin_mando() {
        // Spec `ride-screen`, «Rodillo conectado y mando esperando».
        val conectado = EstadoPlataforma.Conectado("192.168.0.10")
        val esperando = plataforma(conectado, EstadoMando.Esperando)
        assertEquals(PlataformaUi.Texto.ConectadoSinMando, esperando.texto)
        assertEquals(EstadoChip.Ok, esperando.estado)
        assertEquals(EnlaceUi.EnPalabra.Esperando, esperando.mando)
        val sinConexion = plataforma(conectado, EstadoMando.SinConexion)
        assertEquals(PlataformaUi.Texto.ConectadoSinMando, sinConexion.texto)
        assertEquals(EnlaceUi.EnPalabra.SinConexion, sinConexion.mando)
        assertEquals(PlataformaUi.Texto.Conectado, plataforma(conectado, EstadoMando.Conectado).texto)
    }

    @Test
    fun sin_sin_mando_sin_square_o_con_el_mando_dentro() {
        val conectado = EstadoPlataforma.Conectado("192.168.0.10")
        // Sin Square configurado no se publica el mando: no se menciona.
        assertEquals(PlataformaUi.Texto.Conectado, plataforma(conectado, EstadoMando.Desactivado, r = Configurados(rodillo = true)).texto)
        // Spec `ride-screen`, «Mando dentro del mismo dispositivo»: con Solo wifi o Solo Bluetooth no se dice nada del mando.
        for (m in listOf(ModoConexion.ZWIFT_RIDE, ModoConexion.FTMS_BLUETOOTH)) {
            assertEquals(PlataformaUi.Texto.Conectado, plataforma(conectado, EstadoMando.Esperando, config(m)).texto, m.name)
        }
    }

    @Test
    fun no_visible_y_sin_wifi_son_de_alerta_solo_en_la_plataforma() {
        for (z in listOf(EstadoPlataforma.NoVisible, EstadoPlataforma.SinWifi)) {
            val e = estadoDeRodaje(conZwift(z).copy(mando = EstadoMando.Conectado), null, null)
            assertEquals(EstadoChip.Sin, e.plataforma.estado, "$z")
            assertEquals(emptyList<Enlace>(), e.enlaces.filter { it.estado == EstadoChip.Sin }.map { it.enlace }, "$z")
        }
    }

    @Test
    fun el_pie_no_depende_de_zwift() {
        for (z in listOf(EstadoPlataforma.SinWifi, EstadoPlataforma.Esperando, EstadoPlataforma.Conectado("x"))) {
            assertEquals(PieUi.Nada, estadoDeRodaje(conZwift(z), null, null).pie, "$z")
        }
    }

    @Test
    fun ruta_de_zwift_muestra_la_marcha_sin_erg() {
        val p = estadoDeRodaje(conZwift(EstadoPlataforma.Conectado("x"), ModoControl.SimPlataforma(Terreno(4.0))), null, null)
        assertNull(p.erg)
        assertEquals(14, p.marcha)
    }

    @Test
    fun erg_de_zwift_con_y_sin_ajuste() {
        assertEquals(ErgUi(230, 230, 0), estadoDeRodaje(conZwift(EstadoPlataforma.Conectado("x"), ModoControl.ErgPlataforma(230, 0)), null, null).erg)
        assertEquals(ErgUi(240, 230, 10), estadoDeRodaje(conZwift(EstadoPlataforma.Conectado("x"), ModoControl.ErgPlataforma(230, 10)), null, null).erg)
    }

    @Test
    fun erg_con_el_neo_desconectado_sigue_visible_sin_datos() {
        val p = estadoDeRodaje(conZwift(EstadoPlataforma.Conectado("x"), ModoControl.ErgPlataforma(230, 0), neo = Reconectando), null, null)
        assertEquals(ErgUi(230, 230, 0), p.erg)
        assertNull(p.potencia)
        assertEquals(conectando(Enlace.Neo), p.enlace(Enlace.Neo))
    }

    @Test
    fun erg_debajo_de_un_envio_manual() {
        val manual = ModoControl.Manual(ComandoRodillo.Potencia(150), ModoControl.ErgPlataforma(230, 10))
        assertEquals(ErgUi(240, 230, 10), estadoDeRodaje(conZwift(EstadoPlataforma.Conectado("x"), manual), null, null).erg)
    }

    @Test
    fun erg_manual_sin_linea_de_zwift() {
        // Spec `ride-screen`, «Potencia objetivo en ERG manual».
        val p = estadoDeRodaje(conZwift(EstadoPlataforma.Esperando, ModoControl.ErgManual(200, 10, ModoControl.SimLibre)), null, null)
        assertEquals(ErgUi(210, 200, 10, manual = true), p.erg)
        val manual = ModoControl.Manual(ComandoRodillo.Potencia(150), ModoControl.ErgManual(200, 0, ModoControl.SimLibre))
        assertEquals(ErgUi(200, 200, 0, manual = true), estadoDeRodaje(conZwift(EstadoPlataforma.Esperando, manual), null, null).erg)
    }

    @Test
    fun vuelta_a_sim_muestra_la_marcha() {
        val p = estadoDeRodaje(conZwift(EstadoPlataforma.Esperando, ModoControl.SimLibre), null, null)
        assertNull(p.erg)
        assertEquals(14, p.marcha)
    }

    @Test
    fun avisos_de_erg_manual() {
        for (a in listOf(AvisoUi.ModoErg(200), AvisoUi.ModoErg(null), AvisoUi.VueltaA(200), AvisoUi.EntrenamientoZwift)) {
            assertEquals(a, estadoDeRodaje(conZwift(EstadoPlataforma.Esperando), null, a).aviso)
        }
    }

    @Test
    fun avisos_de_vatios() {
        assertEquals(AvisoUi.Vatios(-30), estadoDeRodaje(conZwift(EstadoPlataforma.Conectado("x")), null, AvisoUi.Vatios(-30)).aviso)
        assertEquals(AvisoUi.TopePotencia(50), estadoDeRodaje(conZwift(EstadoPlataforma.Conectado("x")), null, AvisoUi.TopePotencia(50)).aviso)
    }

    @Test
    fun fijar_la_referencia_avisa_y_mueve_la_marca() = runTest(dispatcher) {
        // Spec `ride-screen`, «Fijar la referencia»: «Referencia en la 15» y la marca de la escalera en la 15.
        val vm = RodajeViewModel(sesion(backgroundScope, 15), {})
        backgroundScope.launch { vm.estado.collect {} }
        runCurrent()
        notificaciones.tryEmit(botones())
        notificaciones.tryEmit(botones(SquareButton.Cuadrado to 1))
        runCurrent()
        advanceTimeBy(800)
        runCurrent()
        assertEquals(AvisoUi.Referencia(EstadoMarcha.Numerada(15, 24)), vm.estado.value.aviso)
        assertEquals(15, vm.estado.value.referencia)
        assertEquals(15, vm.estado.value.marcha)
    }

    @Test
    fun cada_cambio_de_la_repeticion_sustituye_al_aviso() = runTest(dispatcher) {
        val vm = RodajeViewModel(sesion(backgroundScope, 10), {})
        val avisos = mutableListOf<AvisoUi?>()
        backgroundScope.launch { vm.estado.collect { avisos += it.aviso } }
        runCurrent()
        notificaciones.tryEmit(botones())
        notificaciones.tryEmit(botones(SquareButton.PalancaDerecha1 to 1))
        runCurrent()
        advanceTimeBy(1_100)
        runCurrent()
        assertEquals(13, vm.estado.value.marcha)
        assertEquals(AvisoUi.Marcha(Accion.MarchaMas(1), 1), vm.estado.value.aviso)
        notificaciones.tryEmit(botones(SquareButton.PalancaDerecha1 to 1))
        runCurrent()
        advanceTimeBy(1_400)
        runCurrent()
        assertEquals(AvisoUi.Marcha(Accion.MarchaMas(1), 1), vm.estado.value.aviso)
        advanceTimeBy(200)
        runCurrent()
        assertEquals(null, vm.estado.value.aviso)
    }

    // --- Datos elegidos, botones y aviso desactivable (change rodaje-completo-y-ajustes) ---------------------------

    private val resumen = app.smartbike.dominio.DatosSesion.Resumen(potencia3sW = 247, tiempoMs = 4_360_000, mediaW = 231, maximaW = 612)

    @Test
    fun datos_por_defecto() {
        // Spec `ride-screen`, «Por defecto»: potencia de 3 s y cadencia.
        val p = estadoDeRodaje(rodaje().copy(datos = resumen), null, null)
        assertEquals(listOf(DatoUi(DatoRodaje.POTENCIA, "247", "W"), DatoUi(DatoRodaje.CADENCIA, "91", "rpm")), p.datos)
        assertEquals(false, p.botones)
    }

    @Test
    fun datos_elegidos_en_su_orden() {
        // Spec `ride-screen`, «Orden elegido», con «W/kg» del peso de Ajustes.
        val c = Configuracion(datos = listOf(DatoRodaje.TIEMPO, DatoRodaje.POTENCIA, DatoRodaje.W_KG, DatoRodaje.POTENCIA_MEDIA, DatoRodaje.POTENCIA_MAXIMA))
        val p = estadoDeRodaje(rodaje().copy(datos = resumen), null, null, c)
        assertEquals(
            listOf(
                DatoUi(DatoRodaje.TIEMPO, "1:12:40"),
                DatoUi(DatoRodaje.POTENCIA, "247", "W"),
                DatoUi(DatoRodaje.W_KG, "3,3"),
                DatoUi(DatoRodaje.POTENCIA_MEDIA, "231", "W"),
                DatoUi(DatoRodaje.POTENCIA_MAXIMA, "612", "W"),
            ),
            p.datos,
        )
    }

    @Test
    fun datos_sin_neo() {
        // Spec `ride-screen`, «Neo desconectado» de los datos de la sesión: los del momento a «—», los de la sesión se quedan.
        val c = Configuracion(datos = listOf(DatoRodaje.POTENCIA, DatoRodaje.CADENCIA, DatoRodaje.W_KG, DatoRodaje.TIEMPO, DatoRodaje.POTENCIA_MAXIMA))
        val p = estadoDeRodaje(rodaje(neo = Reconectando).copy(datos = resumen), null, null, c)
        assertEquals(listOf(null, null, null, "1:12:40", "612"), p.datosVisibles.map { it.valor })
    }

    @Test
    fun velocidad_y_distancia_con_el_fisico() {
        // Spec `ride-screen`, «Velocidad y distancia en Rodaje», «Con el físico».
        val c = Configuracion(datos = listOf(DatoRodaje.POTENCIA, DatoRodaje.VELOCIDAD, DatoRodaje.DISTANCIA))
        val p = estadoDeRodaje(rodaje().copy(datos = resumen, velocidadKmh = 24.04, distanciaKm = 18.4), null, null, c)
        assertEquals(listOf("247", "24,0", "18,40"), p.datosVisibles.map { it.valor })
        assertEquals(listOf("W", "km/h", "km"), p.datosVisibles.map { it.unidad })
    }

    @Test
    fun velocidad_y_distancia_en_ingles_con_punto_decimal() {
        // Spec `app-language`, «Formatos según el idioma», «Decimales».
        val c = Configuracion(datos = listOf(DatoRodaje.POTENCIA, DatoRodaje.W_KG, DatoRodaje.VELOCIDAD, DatoRodaje.DISTANCIA), idioma = app.smartbike.dominio.IdiomaApp.EN)
        val p = estadoDeRodaje(rodaje().copy(datos = resumen, velocidadKmh = 24.5, distanciaKm = 12.34), null, null, c)
        assertEquals(listOf("247", "3.3", "24.5", "12.34"), p.datosVisibles.map { it.valor })
    }

    @Test
    fun velocidad_sin_valor_y_distancia_conservada() {
        // «Con otro modelo» y «Neo desconectado»: la velocidad a «—», la distancia se queda.
        val c = Configuracion(datos = listOf(DatoRodaje.VELOCIDAD, DatoRodaje.DISTANCIA))
        val otro = estadoDeRodaje(rodaje().copy(velocidadKmh = null, distanciaKm = 3.2), null, null, c)
        assertEquals(listOf(null, "3,20"), otro.datosVisibles.map { it.valor })
        val sinNeo = estadoDeRodaje(rodaje(neo = Reconectando).copy(velocidadKmh = null, distanciaKm = 3.2), null, null, c)
        assertEquals(listOf(null, "3,20"), sinNeo.datosVisibles.map { it.valor })
    }

    @Test
    fun parado_la_velocidad_es_cero() {
        val c = Configuracion(datos = listOf(DatoRodaje.VELOCIDAD))
        assertEquals("0,0", estadoDeRodaje(rodaje().copy(velocidadKmh = 0.0), null, null, c).datosVisibles.single().valor)
    }

    @Test
    fun el_aviso_del_modelo_solo_con_mostrar_accion() {
        val aviso = AvisoUi.Modelo(app.smartbike.dominio.TipoModelo.FISICO)
        assertEquals(aviso, estadoDeRodaje(rodaje(), null, aviso).aviso)
        assertNull(estadoDeRodaje(rodaje(), null, aviso, Configuracion(mostrarAccion = false)).aviso)
    }

    @Test
    fun texto_del_tiempo() {
        assertEquals("0:00", textoTiempo(0))
        assertEquals("42:18", textoTiempo(42 * 60_000L + 18_000))
        assertEquals("1:12:40", textoTiempo(4_360_000))
    }

    @Test
    fun el_modo_es_tocable_solo_con_el_ajuste() {
        // Spec `ride-screen`, «Cambiar el modo tocándolo»: independiente de los botones − y + y del Square.
        assertEquals(false, estadoDeRodaje(rodaje(), null, null, Configuracion()).modoTocable)
        assertEquals(false, estadoDeRodaje(rodaje(), null, null, Configuracion(botonesEnPantalla = true)).modoTocable)
        assertEquals(false, estadoDeRodaje(conZwift(EstadoPlataforma.Esperando).copy(square = SinConexion), null, null, Configuracion()).modoTocable)
        assertEquals(true, estadoDeRodaje(rodaje(), null, null, Configuracion(modoTactil = true)).modoTocable)
        assertEquals(false, estadoDeRodaje(rodaje(), null, null, Configuracion(modoTactil = true)).botones)
    }

    @Test
    fun botones_activados() {
        assertEquals(true, estadoDeRodaje(rodaje(), null, null, Configuracion(botonesEnPantalla = true)).botones)
    }

    @Test
    fun aviso_de_la_accion_desactivable() {
        // Spec `ride-screen`, «Aviso de la acción desactivable»: sin aviso de acción, pero con los topes y la ayuda.
        val sin = Configuracion(mostrarAccion = false)
        assertNull(estadoDeRodaje(rodaje(), null, AvisoUi.Marcha(Accion.MarchaMas(1), 1), sin).aviso)
        assertEquals(AvisoUi.TopePotencia(1000), estadoDeRodaje(rodaje(), null, AvisoUi.TopePotencia(1000), sin).aviso)
        assertEquals(TopeMarcha.MasDura, estadoDeRodaje(rodaje(), TopeMarcha.MasDura, null, sin).tope)
        assertEquals(PieUi.DespertarNeo, estadoDeRodaje(rodaje(neo = Reconectando), null, null, sin).pie)
    }

    @Test
    fun sin_wifi_la_plataforma_lo_dice_y_zwift_no_tiene_icono_en_la_barra() {
        // Spec `ride-screen`, «Estado de la publicación en Rodaje»: el wifi del móvil lo pinta el sistema; Rodaje solo dice «Sin wifi».
        val p = estadoDeRodaje(rodaje().copy(plataforma = EstadoPlataforma.SinWifi), null, null)
        assertEquals(PlataformaUi.Texto.SinWifi, p.plataforma.texto)
        assertEquals(null, p.enlace(Enlace.Zwift))
    }

    // --- Pulsómetro (spec `ride-screen`, «Pulsómetro en la barra de conexiones»; change `pulso-zwift`) ---

    private val banda = DispositivoConfigurado("CC:CC", "HW9-1234")

    private fun conBanda(e: EstadoDispositivo, configurada: Boolean = true) = estadoDeRodaje(
        conZwift(EstadoPlataforma.Conectado("192.168.0.10")).copy(
            square = Conectado,
            mando = EstadoMando.Conectado,
            pulsometro = e,
            pulsometroConfigurado = banda.takeIf { configurada },
        ),
        null,
        null,
    )

    @Test
    fun sin_pulsometro_configurado_no_hay_corazon() {
        val e = conBanda(SinConexion, configurada = false).enlaces
        assertEquals(listOf(Enlace.Neo, Enlace.Square), e.map { it.enlace })
        // Aunque la sesión sepa de una banda a medias, sin configurada la barra es la de siempre.
        assertEquals(e, conBanda(Conectado, configurada = false).enlaces)
    }

    @Test
    fun con_pulsometro_configurado_el_corazon_va_el_ultimo_con_los_estados_de_los_demas() {
        assertEquals(
            listOf(Enlace.Neo, Enlace.Square, Enlace.Pulso),
            conBanda(Conectado).enlaces.map { it.enlace },
        )
        assertEquals(EnlaceUi(Enlace.Pulso, EstadoChip.Ok, null), conBanda(Conectado).enlace(Enlace.Pulso))
        assertEquals(EnlaceUi(Enlace.Pulso, EstadoChip.Esperando, EnlaceUi.EnPalabra.Reconectando), conBanda(Reconectando).enlace(Enlace.Pulso))
        assertEquals(EnlaceUi(Enlace.Pulso, EstadoChip.Esperando, EnlaceUi.EnPalabra.Conectando), conBanda(Buscando).enlace(Enlace.Pulso))
        assertEquals(EnlaceUi(Enlace.Pulso, EstadoChip.Sin, EnlaceUi.EnPalabra.SinConexion), conBanda(SinConexion).enlace(Enlace.Pulso))
    }

    @Test
    fun el_pulso_no_cambia_nada_de_rodaje_salvo_el_corazon() {
        val sin = conBanda(Conectado)
        val con = estadoDeRodaje(
            conZwift(EstadoPlataforma.Conectado("192.168.0.10")).copy(
                square = Conectado,
                mando = EstadoMando.Conectado,
                pulsometro = Conectado,
                pulsometroConfigurado = banda,
                pulso = 140,
                pulsoInstanteMs = 1_000,
            ),
            null,
            null,
        )
        // Rodaje no muestra el pulso en ningún sitio, y el corazón no cambia con los latidos.
        assertEquals(sin, con)
    }

    // --- Ventilador (spec `ride-screen`, «Ventilador en la barra de conexiones»; change `ventilador-headwind`) ---

    private val ventiladorConfigurado = DispositivoConfigurado("DD:DD", "HEADWIND 4F2A")

    private fun conVentilador(e: EstadoDispositivo, configurado: Boolean = true) = estadoDeRodaje(
        conZwift(EstadoPlataforma.Conectado("192.168.0.10")).copy(
            square = Conectado,
            mando = EstadoMando.Conectado,
            ventilador = e,
            ventiladorConfigurado = ventiladorConfigurado.takeIf { configurado },
        ),
        null,
        null,
    )

    @Test
    fun sin_ventilador_configurado_no_hay_aspa() {
        val e = conVentilador(SinConexion, configurado = false).enlaces
        assertEquals(listOf(Enlace.Neo, Enlace.Square), e.map { it.enlace })
        assertEquals(e, conVentilador(Conectado, configurado = false).enlaces)
    }

    @Test
    fun con_ventilador_configurado_el_aspa_va_la_ultima_con_los_estados_de_los_demas() {
        assertEquals(Enlace.Ventilador, conVentilador(Conectado).enlaces.last().enlace)
        assertEquals(EnlaceUi(Enlace.Ventilador, EstadoChip.Ok, null), conVentilador(Conectado).enlace(Enlace.Ventilador))
        assertEquals(EnlaceUi(Enlace.Ventilador, EstadoChip.Esperando, EnlaceUi.EnPalabra.Reconectando), conVentilador(Reconectando).enlace(Enlace.Ventilador))
        assertEquals(EnlaceUi(Enlace.Ventilador, EstadoChip.Sin, EnlaceUi.EnPalabra.SinConexion), conVentilador(SinConexion).enlace(Enlace.Ventilador))
    }

    @Test
    fun el_pulsometro_y_el_ventilador_van_en_ese_orden_y_son_cuatro() {
        val e = estadoDeRodaje(
            conZwift(EstadoPlataforma.Conectado("192.168.0.10")).copy(
                square = Conectado,
                mando = EstadoMando.Conectado,
                pulsometro = Conectado,
                pulsometroConfigurado = banda,
                ventilador = Conectado,
                ventiladorConfigurado = ventiladorConfigurado,
            ),
            null,
            null,
        )
        assertEquals(listOf(Enlace.Neo, Enlace.Square, Enlace.Pulso, Enlace.Ventilador), e.enlaces.map { it.enlace })
    }

    @Test
    fun el_modo_y_la_velocidad_no_cambian_nada_de_rodaje() {
        val sin = conVentilador(Conectado)
        val con = estadoDeRodaje(
            conZwift(EstadoPlataforma.Conectado("192.168.0.10")).copy(
                square = Conectado,
                mando = EstadoMando.Conectado,
                ventilador = Conectado,
                ventiladorConfigurado = ventiladorConfigurado,
                ventiladorEstado = EstadoVentilador(ModoVentilador.Manual, 70, 70),
                ventiladorInstanteMs = 1_000,
            ),
            null,
            null,
        )
        assertEquals(sin, con)
    }

    @Test
    fun los_avisos_del_ventilador_respetan_mostrar_accion() {
        val aviso = AvisoUi.Ventilador(AvisoVentilador.Velocidad(50))
        assertEquals(aviso, avisoVisible(aviso, mostrarAccion = true))
        assertEquals(null, avisoVisible(aviso, mostrarAccion = false))
    }

    @Test
    fun el_estado_por_bluetooth_no_habla_de_wifi_ni_de_mando() {
        // Spec `ride-screen`, «Estado de la publicación en Rodaje» con «Solo Bluetooth».
        val bt = config(ModoConexion.FTMS_BLUETOOTH)
        val esperando = plataforma(EstadoPlataforma.Esperando, c = bt)
        assertEquals(PlataformaUi.Texto.Esperando, esperando.texto)
        assertEquals(EstadoChip.Esperando, esperando.estado)
        val conectado = plataforma(EstadoPlataforma.Conectado("AA:BB"), EstadoMando.Desactivado, bt)
        assertEquals(PlataformaUi.Texto.Conectado, conectado.texto)
        assertEquals(null, conectado.mando)
        val sinBluetooth = plataforma(EstadoPlataforma.SinBluetooth, c = bt)
        assertEquals(PlataformaUi.Texto.SinBluetooth, sinBluetooth.texto)
        assertEquals(EstadoChip.Sin, sinBluetooth.estado)
        assertEquals(EnlaceUi.EnPalabra.SinConexion, sinBluetooth.rodillo)
        val noDisponible = plataforma(EstadoPlataforma.NoDisponible, c = bt)
        assertEquals(PlataformaUi.Texto.NoDisponible, noDisponible.texto)
        assertEquals(EstadoChip.Sin, noDisponible.estado)
        assertEquals(EnlaceUi.EnPalabra.NoDisponible, noDisponible.rodillo)
    }

    // --- Guía de inicio (spec `getting-started-guide`) ---------------------------------------------------------------

    private fun conGuia(guia: GuiaInicio? = GuiaInicio.VISIBLE, pantalla: GuiaPantalla = GuiaPantalla(abierta = true), e: EstadoRodaje = rodaje()) = estadoDeRodaje(e.copy(guiaInicio = guia), null, null, guia = pantalla)

    @Test
    fun la_guia_visible_trae_la_pagina_la_forma_y_los_pasos() {
        val g = checkNotNull(conGuia(pantalla = GuiaPantalla(abierta = true).irA(2)).guia)
        assertEquals(2, g.pagina)
        assertTrue(g.abierta)
        assertEquals(ModoConexion.FTMS_WIFI, g.modo)
        assertEquals(app.smartbike.dominio.NombreAnunciado.NORMAL, g.nombreApp)
    }

    @Test
    fun sin_evaluar_descartada_o_terminada_no_hay_guia() {
        assertNull(conGuia(guia = null).guia)
        assertNull(conGuia(guia = GuiaInicio.DESCARTADA).guia)
        assertNull(conGuia(guia = GuiaInicio.FINALIZADA).guia)
    }

    @Test
    fun cerrada_con_la_equis_no_hay_guia_aunque_falte_algo() {
        assertNull(conGuia(pantalla = GuiaPantalla(abierta = true).cerrar(), e = rodaje(neo = SinConexion, square = SinConexion)).guia)
    }

    @Test
    fun los_pasos_siguen_el_estado_de_los_dispositivos() {
        val sin = checkNotNull(conGuia(e = rodaje(neo = SinConexion, square = SinConexion)).guia).pasos
        assertEquals(PasoDispositivos.PENDIENTE, sin.dispositivos)
        assertEquals(PasoApp.PENDIENTE, sin.app)
        val dormido = checkNotNull(conGuia(e = rodaje(neo = Conectando, square = SinConexion).copy(neoConfigurado = DispositivoConfigurado("AA", "Tacx Neo 1"))).guia).pasos
        assertEquals(PasoDispositivos.CONECTANDO, dormido.dispositivos)
        assertEquals(PasoApp.ESPERANDO, dormido.app)
        val soloRodillo = checkNotNull(conGuia(e = rodaje(neo = Conectado, square = SinConexion)).guia).pasos
        assertEquals(PasoDispositivos.FALTA_CONTROLADOR, soloRodillo.dispositivos)
        val los2 = checkNotNull(conGuia(e = rodaje(neo = Conectado, square = Conectado)).guia).pasos
        assertEquals(PasoDispositivos.HECHO, los2.dispositivos)
    }

    @Test
    fun el_modelo_real_viene_de_los_dispositivos_configurados() {
        val e = rodaje().copy(
            neoConfigurado = DispositivoConfigurado("AA", "Tacx Neo 1"),
            squareConfigurado = DispositivoConfigurado("BB", "Elite Square"),
        )
        val g = checkNotNull(conGuia(e = e).guia)
        assertEquals("Tacx Neo 1", g.rodillo)
        assertEquals("Elite Square", g.controlador)
        assertNull(checkNotNull(conGuia().guia).rodillo)
    }

    @Test
    fun con_todo_hecho_la_guia_se_oculta_salvo_reabierta() {
        val hecho = rodaje().copy(plataforma = EstadoPlataforma.Conectado("192.168.0.10"), usoRodillo = true)
        assertNull(conGuia(e = hecho).guia)
        assertTrue(conGuia(pantalla = GuiaPantalla().reabrir(), e = hecho).guia != null)
    }

    @Test
    fun la_guia_se_abre_sola_en_la_pagina_1_navega_se_minimiza_y_se_reabre_donde_estaba() = runTest(dispatcher) {
        val vm = RodajeViewModel(sesion(backgroundScope, 14), {})
        backgroundScope.launch { vm.estado.collect {} }
        runCurrent()
        assertEquals(1, checkNotNull(vm.estado.value.guia).pagina)
        assertTrue(checkNotNull(vm.estado.value.guia).abierta)
        vm.paginaGuia(2)
        runCurrent()
        assertEquals(2, checkNotNull(vm.estado.value.guia).pagina)
        vm.minimizarGuia()
        runCurrent()
        assertFalse(checkNotNull(vm.estado.value.guia).abierta)
        assertEquals(2, checkNotNull(vm.estado.value.guia).pagina)
        vm.abrirGuia()
        runCurrent()
        assertTrue(checkNotNull(vm.estado.value.guia).abierta)
        assertEquals(2, checkNotNull(vm.estado.value.guia).pagina)
    }

    @Test
    fun la_equis_oculta_la_guia_sin_cambiar_lo_guardado_y_no_volver_a_mostrar_la_descarta() = runTest(dispatcher) {
        val sesion = sesion(backgroundScope, 14)
        val vm = RodajeViewModel(sesion, {})
        backgroundScope.launch { vm.estado.collect {} }
        runCurrent()
        vm.cerrarGuia()
        runCurrent()
        assertNull(vm.estado.value.guia)
        assertEquals(GuiaInicio.VISIBLE, sesion.estado.value.guiaInicio)
        sesion.reabrirGuia()
        runCurrent()
        assertTrue(vm.estado.value.guia != null)
        vm.noMostrarGuia()
        runCurrent()
        assertNull(vm.estado.value.guia)
        assertEquals(GuiaInicio.DESCARTADA, sesion.estado.value.guiaInicio)
    }

    @Test
    fun descartar_la_guia_no_cambia_la_marcha_ni_las_conexiones() = runTest(dispatcher) {
        val sesion = sesion(backgroundScope, 14)
        val vm = RodajeViewModel(sesion, {})
        backgroundScope.launch { vm.estado.collect {} }
        runCurrent()
        val antes = vm.estado.value
        vm.noMostrarGuia()
        runCurrent()
        assertEquals(antes.marcha, vm.estado.value.marcha)
        assertEquals(antes.enlaces, vm.estado.value.enlaces)
        assertEquals(antes.plataforma, vm.estado.value.plataforma)
    }

    // Spec `ride-screen`, «Modo y transmisión»: LIBRE sin terreno de la plataforma, SIM con él, ERG sin cambio.
    private fun libre(modo: ModoControl) = estadoDeRodaje(conZwift(EstadoPlataforma.Conectado("x"), modo), null, null).libre

    @Test
    fun el_modo_es_libre_sin_terreno_de_la_plataforma_y_sim_con_el_terreno() {
        assertTrue(estadoDeRodaje(conZwift(EstadoPlataforma.Esperando), null, null).libre)
        // Zwift conectado pero sin pendiente todavía: sigue Libre.
        assertTrue(libre(ModoControl.SimLibre))
        assertFalse(libre(ModoControl.SimPlataforma(Terreno(4.2))))
        assertFalse(libre(ModoControl.ErgPlataforma(200, 0)))
        assertFalse(libre(ModoControl.ErgManual(200, 0, ModoControl.SimLibre)))
    }

    @Test
    fun el_diagnostico_manual_conserva_el_modo_que_hay_debajo() {
        assertTrue(libre(ModoControl.Manual(ComandoRodillo.Pendiente(1.0), ModoControl.SimLibre)))
        assertFalse(libre(ModoControl.Manual(ComandoRodillo.Pendiente(1.0), ModoControl.SimPlataforma(Terreno(3.0)))))
    }

    private fun botones(square: EstadoDispositivo, casilla: Boolean = false) = estadoDeRodaje(conZwift(EstadoPlataforma.Esperando).copy(square = square), null, null, Configuracion(botonesEnPantalla = casilla)).botones

    @Test
    fun los_botones_aparecen_con_el_square_no_conectado_aunque_la_casilla_este_desactivada() {
        assertFalse(botones(Conectado))
        listOf(EstadoDispositivo.SinConexion, Buscando, Conectando, Reconectando, NoEncontrado).forEach {
            assertTrue(botones(it), "$it")
        }
    }

    @Test
    fun con_el_square_conectado_los_botones_siguen_la_casilla() {
        assertTrue(botones(Conectado, casilla = true))
        assertTrue(botones(EstadoDispositivo.SinConexion, casilla = true))
    }

    @Test
    fun los_botones_no_cambian_la_marcha() {
        val conectado = estadoDeRodaje(conZwift(EstadoPlataforma.Esperando), null, null)
        val caido = estadoDeRodaje(conZwift(EstadoPlataforma.Esperando).copy(square = EstadoDispositivo.SinConexion), null, null)
        assertEquals(conectado.marcha, caido.marcha)
        assertEquals(conectado.libre, caido.libre)
    }

    private fun pendiente(modo: ModoControl, base: Double = 0.0, idioma: IdiomaApp = IdiomaApp.ES): DatoUi {
        val e = conZwift(EstadoPlataforma.Conectado("x"), modo).let { it.copy(simulacion = it.simulacion.copy(pendienteBasePct = base)) }
        return datosDeRodaje(e, Configuracion(datos = listOf(DatoRodaje.PENDIENTE), idioma = idioma)).single()
    }

    @Test
    fun la_pendiente_es_la_de_la_ruta_con_signo_y_un_decimal() {
        val subida = pendiente(ModoControl.SimPlataforma(Terreno(4.2)))
        assertEquals("+4,2", subida.valor)
        assertEquals("%", subida.unidad)
        assertEquals(TonoDato.Subida, subida.tono)
        val bajada = pendiente(ModoControl.SimPlataforma(Terreno(-7.0)))
        assertEquals("−7,0", bajada.valor)
        assertEquals(TonoDato.Bajada, bajada.tono)
    }

    @Test
    fun la_pendiente_en_libre_es_cero_y_en_erg_no_hay() {
        assertEquals("0,0", pendiente(ModoControl.SimLibre).valor)
        assertEquals(TonoDato.Neutro, pendiente(ModoControl.SimLibre).tono)
        assertNull(pendiente(ModoControl.ErgPlataforma(200, 0)).valor)
        assertNull(pendiente(ModoControl.ErgManual(200, 0, ModoControl.SimPlataforma(Terreno(5.0)))).valor)
    }

    @Test
    fun la_pendiente_no_incluye_la_base_ni_el_ajuste_de_la_marcha() {
        // La base es de Diagnóstico: con terreno de la plataforma manda el terreno; el ajuste de la marcha no se suma.
        assertEquals("+3,0", pendiente(ModoControl.SimPlataforma(Terreno(3.0)), base = 9.0).valor)
    }

    @Test
    fun la_pendiente_en_el_umbral_es_neutra_y_un_cero_redondeado_no_lleva_signo() {
        assertEquals(TonoDato.Neutro, pendiente(ModoControl.SimPlataforma(Terreno(0.5))).tono)
        assertEquals(TonoDato.Subida, pendiente(ModoControl.SimPlataforma(Terreno(0.6))).tono)
        assertEquals("0,0", pendiente(ModoControl.SimPlataforma(Terreno(-0.04))).valor)
    }

    @Test
    fun cuando_zwift_se_va_la_pendiente_vuelve_a_cero() {
        assertEquals("0,0", pendiente(ModoControl.SimLibre).valor)
    }

    @Test
    fun la_pendiente_en_ingles_usa_punto_decimal() {
        assertEquals("+4.2", pendiente(ModoControl.SimPlataforma(Terreno(4.2)), idioma = IdiomaApp.EN).valor)
    }
}
