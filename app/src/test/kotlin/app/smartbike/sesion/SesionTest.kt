package app.smartbike.sesion

import app.smartbike.ble.BuscadorDispositivos
import app.smartbike.ble.ConexionNeo
import app.smartbike.ble.ConexionPulso
import app.smartbike.ble.ConexionSquare
import app.smartbike.ble.ConexionVentilador
import app.smartbike.ble.EstadoBluetooth
import app.smartbike.ble.EstadoEnlace
import app.smartbike.ble.EstadoNombre
import app.smartbike.ble.NeoClient
import app.smartbike.ble.NombreDelAdaptador
import app.smartbike.ble.Notificacion
import app.smartbike.ble.PulsoClient
import app.smartbike.ble.RenombradorBluetooth
import app.smartbike.ble.ResultadoBusqueda
import app.smartbike.ble.VentiladorClient
import app.smartbike.datos.AlmacenConfiguracion
import app.smartbike.datos.AlmacenEstado
import app.smartbike.datos.RegistroSesion
import app.smartbike.dominio.Accion
import app.smartbike.dominio.AjustesConexion
import app.smartbike.dominio.AjustesLineal
import app.smartbike.dominio.Asignacion
import app.smartbike.dominio.AvisoVentilador
import app.smartbike.dominio.ClaveIdentidad
import app.smartbike.dominio.ComandoRodillo
import app.smartbike.dominio.ComandoRodillo.Pendiente
import app.smartbike.dominio.ComandoRodillo.Potencia
import app.smartbike.dominio.Configuracion
import app.smartbike.dominio.ConfiguracionBotones
import app.smartbike.dominio.Desarrollos
import app.smartbike.dominio.DispositivoConfigurado
import app.smartbike.dominio.Enlace
import app.smartbike.dominio.Entrada
import app.smartbike.dominio.EstadoGuardado
import app.smartbike.dominio.EstadoMarcha
import app.smartbike.dominio.EstadoVentilador
import app.smartbike.dominio.FuncionMando
import app.smartbike.dominio.FuncionZwift
import app.smartbike.dominio.GuiaInicio
import app.smartbike.dominio.GuiaPantalla
import app.smartbike.dominio.Identidad
import app.smartbike.dominio.IdentidadDispositivo
import app.smartbike.dominio.IdiomaApp
import app.smartbike.dominio.LadoGiro
import app.smartbike.dominio.ModeloSimple
import app.smartbike.dominio.ModoBajadas
import app.smartbike.dominio.ModoCambio
import app.smartbike.dominio.ModoConexion
import app.smartbike.dominio.ModoControl
import app.smartbike.dominio.ModoVentilador
import app.smartbike.dominio.NombreAnunciado
import app.smartbike.dominio.ResultadoAjuste
import app.smartbike.dominio.ResultadoCambio
import app.smartbike.dominio.ServiciosPublicados
import app.smartbike.dominio.TemaApp
import app.smartbike.dominio.Terreno
import app.smartbike.dominio.TipoGesto
import app.smartbike.dominio.TipoModelo
import app.smartbike.dominio.TopeMarcha
import app.smartbike.dominio.Transmision
import app.smartbike.plataforma.ConexionFtms
import app.smartbike.plataforma.ConexionPlataforma
import app.smartbike.plataforma.EstadoMando
import app.smartbike.plataforma.EstadoPlataforma
import app.smartbike.plataforma.EstadoTransporte
import app.smartbike.plataforma.MandoEmulado
import app.smartbike.plataforma.ReceptorPlataforma
import app.smartbike.plataforma.TelemetriaPlataforma
import app.smartbike.plataforma.TransporteBluetooth
import app.smartbike.plataforma.TransporteDircon
import app.smartbike.plataforma.zwift.ConexionZwiftRide
import app.smartbike.plataforma.zwift.MandoZwift
import app.smartbike.plataforma.zwift.MandoZwiftBluetooth
import app.smartbike.plataforma.zwift.MandoZwiftEnServidor
import app.smartbike.plataforma.zwift.ServicioDelMando
import app.smartbike.protocolo.DirconCodec
import app.smartbike.protocolo.FecCodec
import app.smartbike.protocolo.MandoZwiftCodec
import app.smartbike.protocolo.NotificacionGatt
import app.smartbike.protocolo.ResultadoGatt
import app.smartbike.protocolo.ServidorGatt
import app.smartbike.protocolo.SquareButton
import app.smartbike.red.EstadoWifi
import app.smartbike.sesion.EstadoDispositivo.Buscando
import app.smartbike.sesion.EstadoDispositivo.Conectado
import app.smartbike.sesion.EstadoDispositivo.Conectando
import app.smartbike.sesion.EstadoDispositivo.NoEncontrado
import app.smartbike.sesion.EstadoDispositivo.Reconectando
import app.smartbike.sesion.EstadoDispositivo.SinConexion
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/** La posición de una marcha de la lineal, para los tests que solo usan la lineal. */
@OptIn(ExperimentalCoroutinesApi::class)
private val EstadoMarcha.posicion: Int get() = (this as EstadoMarcha.Numerada).posicion

private fun lineal(n: Int) = EstadoMarcha.Numerada(n, 24)

class SesionTest {

    private open class EnlaceFalso {
        val estado = MutableStateFlow(EstadoEnlace.Desconectado)
        val notificaciones = MutableSharedFlow<Notificacion>(extraBufferCapacity = 16)

        /** Si la conexión directa lo encuentra despierto. */
        var conecta = true

        /** Direcciones de las conexiones directas. */
        val conexiones = mutableListOf<String>()

        /** Cada conexión con su modo: «directa AA:AA» o «segundo plano AA:AA». */
        val modos = mutableListOf<String>()
        private var espera: CompletableDeferred<Boolean>? = null

        /** Si hay una conexión en segundo plano esperando a que el dispositivo se anuncie. */
        val esperando: Boolean get() = espera?.isActive == true

        /** Direcciones que el cliente real rechaza por inválidas, sin llegar a conectar. */
        val invalidas = mutableSetOf<String>()

        suspend fun conectar(direccion: String): Boolean {
            if (direccion in invalidas) return false
            conexiones += direccion
            modos += "directa $direccion"
            estado.value = if (conecta) EstadoEnlace.Listo else EstadoEnlace.Fallo
            return conecta
        }

        suspend fun conectarEnSegundoPlano(direccion: String): Boolean {
            if (direccion in invalidas) return false
            modos += "segundo plano $direccion"
            val e = CompletableDeferred<Boolean>().also { espera = it }
            return try {
                e.await()
            } finally {
                if (espera === e) espera = null
            }
        }

        /** El dispositivo se despierta y se anuncia: conecta la espera en segundo plano. */
        fun despertar() {
            val e = checkNotNull(espera) { "Nadie espera en segundo plano" }
            espera = null
            estado.value = EstadoEnlace.Listo
            e.complete(true)
        }

        /** La desconexión nunca termina (un intento de conexión pendiente tras apagar y encender el Bluetooth). */
        var desconexionColgada = false

        /** Lo que tarda en terminar la desconexión. */
        var tardaDesconexionMs = 0L

        /** Veces que se ha cancelado la conexión pendiente; deja la cola limpia, así que la desconexión vuelve a terminar. */
        var cancelaciones = 0

        fun cancelarConexionPendiente() {
            cancelaciones++
            desconexionColgada = false
        }

        suspend fun desconectar() {
            if (desconexionColgada) kotlinx.coroutines.awaitCancellation()
            if (tardaDesconexionMs > 0) delay(tardaDesconexionMs)
            espera?.complete(false)
            espera = null
            estado.value = EstadoEnlace.Desconectado
        }
    }

    private class SquareFalso :
        EnlaceFalso(),
        ConexionSquare {
        override val nivelBateria = MutableSharedFlow<Int>(extraBufferCapacity = 16)
    }

    private class NeoFalso :
        EnlaceFalso(),
        ConexionNeo {
        val enviados = mutableListOf<ByteArray>()

        /** Confirmaciones pendientes, en orden; el test decide cuándo llegan. */
        val porConfirmar = mutableListOf<(Boolean) -> Unit>()
        override fun enviar(comando: ByteArray, alConfirmar: ((Boolean) -> Unit)?) {
            enviados += comando
            alConfirmar?.let { porConfirmar += it }
        }

        override suspend fun enviarYEsperar(comando: ByteArray, limiteMs: Long): Boolean {
            enviados += comando
            return true
        }
    }

    private class PulsoFalso :
        EnlaceFalso(),
        ConexionPulso

    private class VentiladorFalso :
        EnlaceFalso(),
        ConexionVentilador {
        /** Cada orden escrita en el control point, en orden. */
        val enviados = mutableListOf<ByteArray>()

        /** Responde al instante a cada orden, como el ventilador real. */
        var respuesta: ((ByteArray) -> Unit)? = null

        override fun enviar(orden: ByteArray) {
            enviados += orden
            respuesta?.invoke(orden)
        }

        fun ordenes() = enviados.map { b -> b.joinToString(" ") { "%02X".format(it) } }
    }

    private class BuscadorFalso : BuscadorDispositivos {
        val pedidos = mutableListOf<Set<String>>()
        var busquedasPulsometro = 0
        val resultadosPulsometro = Channel<ResultadoBusqueda>(Channel.UNLIMITED)
        override fun buscarPulsometro(limiteMs: Long): Flow<ResultadoBusqueda> {
            busquedasPulsometro++
            return resultadosPulsometro.receiveAsFlow().take(1)
        }
        val resultados = Channel<ResultadoBusqueda>(Channel.UNLIMITED)
        override fun buscar(prefijos: Set<String>, limiteMs: Long): Flow<ResultadoBusqueda> {
            pedidos += prefijos
            // Como el real: termina cuando cada prefijo tiene su resultado.
            return resultados.receiveAsFlow().take(prefijos.size)
        }
    }

    private val square = SquareFalso()
    private val neo = NeoFalso()
    private val pulsometro = PulsoFalso()
    private val ventilador = VentiladorFalso()
    private val buscador = BuscadorFalso()
    private val bluetooth = object : EstadoBluetooth {
        override val encendido = MutableStateFlow(true)
        override val permiso = MutableStateFlow(true)
        override val permisoAnunciar = MutableStateFlow(true)
    }
    private class WifiFalso : EstadoWifi {
        override val direccion = MutableStateFlow<String?>(null)
        var activo = false
        override fun mantenerActivo(activo: Boolean) {
            this.activo = activo
        }
    }

    private val wifi = WifiFalso()

    private class TransporteFalso : TransporteDircon {
        override val estado = MutableStateFlow<EstadoTransporte>(EstadoTransporte.Parado)
        var servidor: ServidorGatt? = null
        var identidad: IdentidadDispositivo? = null
        var nombre: String? = null
        var publicaciones = 0
        var paradas = 0
        val notificaciones = mutableListOf<NotificacionGatt>()
        override fun publicar(servidor: ServidorGatt, identidad: IdentidadDispositivo, nombre: String) {
            this.servidor = servidor
            this.identidad = identidad
            this.nombre = nombre
            publicaciones++
            estado.value = EstadoTransporte.Esperando
        }
        override fun parar() {
            paradas++
            estado.value = EstadoTransporte.Parado
        }
        override fun notificar(n: NotificacionGatt) {
            notificaciones += n
        }

        /** Zwift se conecta y se suscribe a la telemetría. */
        fun conectarZwift(vararg suscripciones: Int) {
            val s = checkNotNull(servidor)
            s.clientePerdido()
            suscripciones.forEach { s.suscribir(DirconCodec.uuid16(it), true) }
            estado.value = EstadoTransporte.Conectado("192.168.0.10")
        }
    }

    private val transporte = TransporteFalso()

    /** El adaptador del móvil: «OnePlus 8» hasta que la app lo renombra. */
    private class AdaptadorNombreFalso : NombreDelAdaptador {
        var real: String? = "OnePlus 8"
        override val confirmado = MutableStateFlow<String?>("OnePlus 8")
        val llamadas = mutableListOf<String>()
        var acepta = true
        override fun actual() = real
        override fun poner(nombre: String): Boolean {
            llamadas += nombre
            if (!acepta) return false
            real = nombre
            confirmado.value = nombre
            return true
        }
    }

    private val adaptadorNombre = AdaptadorNombreFalso()
    private val renombrador by lazy { RenombradorBluetooth(adaptadorNombre, almacen, registro) }

    /** El mando de Zwift, con su propio transporte (Bluetooth en la app). */
    private val transporteMando = TransporteFalso()
    private var perifericoDisponible = true
    private fun TestScope.nuevoMando() = MandoZwift(backgroundScope, registro, transporteMando)
    private var mandoZwift: MandoZwift? = null

    /** La conexión con Zwift de cada modo, con el rodillo por [rodillo] y el mando de Bluetooth por [transporteMando]. */
    private fun TestScope.fabrica(rodillo: TransporteFalso = transporte): (ServiciosPublicados, Boolean, ReceptorPlataforma, CoroutineScope) -> ConexionPlataforma = { servicios, simularKickr, receptor, ambito ->
        val modo = servicios.modo
        when (modo) {
            ModoConexion.FTMS_WIFI -> {
                val mando = if (servicios.mando != null) MandoZwiftBluetooth(ambito, nuevoMando().also { mandoZwift = it }, transporteMando, bluetooth) { perifericoDisponible } else null
                ConexionFtms(modo, registro, rodillo, "0.0-test", receptor, simularKickr, mando, servicios.pulso)
            }

            ModoConexion.ZWIFT_RIDE -> ConexionZwiftRide(ambito, registro, rodillo, "0.0-test", receptor, simularKickr, servicios.pulso)

            ModoConexion.FTMS_BLUETOOTH -> {
                val bt = TransporteBluetooth(ambito, rodillo, bluetooth) { perifericoDisponible }
                val mando = if (servicios.mando != null) {
                    MandoZwiftEnServidor(ambito, MandoZwift(ambito, registro, bt, servicioDelMando = ServicioDelMando.BLUETOOTH).also { mandoZwift = it }, bt.estado)
                } else {
                    null
                }
                ConexionFtms(modo, registro, bt, "0.0-test", receptor, simularKickr, mando = mando, pulso = servicios.pulso)
            }
        }
    }

    private fun config(modo: ModoConexion = ModoConexion.FTMS_WIFI, identidad: Identidad = Identidad.SMART_BIKE) = Configuracion(conexion = AjustesConexion(modo, identidad))

    private fun Configuracion.conPlataforma(modo: ModoConexion? = null, identidad: Identidad? = null) = copy(conexion = conexion.copy(modo = modo ?: conexion.modo, identidad = identidad ?: conexion.identidad))

    /** La identidad guardada en `estado.json` para el modo y el nombre (spec `platform-bridge`, «Identidad por plataforma, modo y nombre»). */
    private fun guardada(modo: ModoConexion, nombre: Identidad = Identidad.SMART_BIKE) = almacen.guardado.identidades[ClaveIdentidad.de(modo, nombre)]

    /**
     * Reloj monotónico de la sesión: sigue al reloj virtual del test, con un desfase que los tests fijan asignando
     * [ahora]. Así los temporizadores del reconocedor (`delay`) y la hora de las notificaciones van juntos.
     */
    private var desfase = 0L
    private var virtual: () -> Long = { 0L }
    private var ahora: Long
        get() = desfase + virtual()
        set(v) {
            desfase = v - virtual()
        }
    private val registro = RegistroSesion(relojMonotonico = { ahora }, relojPared = { 0L })

    private class AlmacenFalso(var guardado: EstadoGuardado = EstadoGuardado()) : AlmacenEstado {
        var escrituras = 0
        override suspend fun leer() = guardado
        override suspend fun actualizar(cambio: (EstadoGuardado) -> EstadoGuardado) {
            guardado = cambio(guardado)
            escrituras++
        }
    }

    private class VibradorFalso : Vibrador {
        val vibraciones = mutableListOf<String>()
        override fun corta() {
            vibraciones += "corta"
        }
        override fun doble() {
            vibraciones += "doble"
        }
        override fun larga() {
            vibraciones += "larga"
        }
        override fun triple() {
            vibraciones += "triple"
        }
    }

    private class AjustesFalsos : AlmacenConfiguracion {
        override val configuracion = MutableStateFlow(Configuracion())
        override suspend fun actualizarConfiguracion(cambio: (Configuracion) -> Configuracion) {
            configuracion.value = cambio(configuracion.value)
        }
    }

    private val almacen = AlmacenFalso()
    private val ajustes = AjustesFalsos()
    private val vibrador = VibradorFalso()

    /** El Neo y el Square configurados, sin buscarlos: «configurado = se publica» (spec `platform-bridge`). */
    private fun configurarNeoYSquare() {
        almacen.guardado = almacen.guardado.copy(
            rodillo = DispositivoConfigurado("AA:AA", "Tacx Neo 12345"),
            mando = DispositivoConfigurado("BB:BB", "SQUARE 1234"),
        )
    }

    private fun TestScope.conexiones(): Sesion {
        virtual = { testScheduler.currentTime }
        return Sesion(
            buscador, square, neo, pulsometro, ventilador, bluetooth, wifi, fabrica(), renombrador, registro, almacen, ajustes, vibrador,
            backgroundScope, relojMonotonico = { ahora }, relojPared = { 45_871_000L },
        ).also { runCurrent() }
    }

    private fun TestScope.conectarLosDos(c: Sesion) {
        c.buscar()
        runCurrent()
        buscador.resultados.trySend(ResultadoBusqueda.Encontrado("Tacx Neo", "AA:AA", "Tacx Neo 12345"))
        buscador.resultados.trySend(ResultadoBusqueda.Encontrado("SQUARE", "BB:BB", "SQUARE 1234"))
        runCurrent()
    }

    /** Notificación de 0x45; cada cambio suma ese número de transiciones al contador del botón (impar = pulsado). */
    private val contadores = IntArray(SquareButton.entries.size) { 2 }

    private fun botones(vararg cambios: Pair<SquareButton, Int>): Notificacion {
        cambios.forEach { (b, n) -> contadores[b.ordinal] = (contadores[b.ordinal] + n) % 16 }
        val nibbles = IntArray(22) { if (it < 2) 0 else contadores[SquareButton.entries.first { b -> b.contador == it - 2 }.ordinal] }
        return Notificacion("0x45", ByteArray(11) { ((nibbles[it * 2] shl 4) or nibbles[it * 2 + 1]).toByte() }, ahora)
    }

    @Test
    fun busca_y_conecta_los_dos() = runTest {
        val c = conexiones()
        c.buscar()
        runCurrent()
        assertEquals(Buscando, c.estado.value.neo)
        assertEquals(Buscando, c.estado.value.square)
        assertEquals(setOf("Tacx Neo", "SQUARE"), buscador.pedidos.single())

        buscador.resultados.trySend(ResultadoBusqueda.Encontrado("Tacx Neo", "AA:AA", "Tacx Neo 12345"))
        buscador.resultados.trySend(ResultadoBusqueda.Encontrado("SQUARE", "BB:BB", "SQUARE 1234"))
        runCurrent()
        assertEquals(Conectado, c.estado.value.neo)
        assertEquals(Conectado, c.estado.value.square)
        assertEquals(listOf("AA:AA"), neo.conexiones)
        assertEquals(listOf("BB:BB"), square.conexiones)
        assertEquals(0u, c.estado.value.mascara)
    }

    @Test
    fun con_uno_conectado_busca_solo_el_otro() = runTest {
        val c = conexiones()
        neo.estado.value = EstadoEnlace.Listo
        runCurrent()
        c.buscar()
        runCurrent()
        assertEquals(setOf("SQUARE"), buscador.pedidos.single())
        assertEquals(Conectado, c.estado.value.neo)
    }

    @Test
    fun no_encontrado() = runTest {
        val c = conexiones()
        c.buscar()
        runCurrent()
        buscador.resultados.trySend(ResultadoBusqueda.NoEncontrado("Tacx Neo"))
        runCurrent()
        assertEquals(NoEncontrado, c.estado.value.neo)
        assertEquals(Buscando, c.estado.value.square)
    }

    @Test
    fun fallo_al_conectar_cuenta_como_no_encontrado() = runTest {
        val c = conexiones()
        neo.conecta = false
        c.buscar()
        runCurrent()
        buscador.resultados.trySend(ResultadoBusqueda.Encontrado("Tacx Neo", "AA:AA", "Tacx Neo 12345"))
        runCurrent()
        assertEquals(NoEncontrado, c.estado.value.neo)
    }

    @Test
    fun conexion_perdida_del_square_libera_los_botones() = runTest {
        val c = conexiones()
        conectarLosDos(c)
        square.notificaciones.tryEmit(botones())
        square.notificaciones.tryEmit(botones(SquareButton.FrenoIzquierdo to 1))
        runCurrent()
        assertEquals(SquareButton.FrenoIzquierdo.mascara, c.estado.value.mascara)

        square.conecta = false
        square.estado.value = EstadoEnlace.Perdida
        runCurrent()
        assertEquals(Reconectando, c.estado.value.square)
        assertNull(c.estado.value.mascara)
        assertEquals(Conectado, c.estado.value.neo)

        // Al reconectar, ningún botón sigue pulsado.
        square.despertar()
        runCurrent()
        assertEquals(Conectado, c.estado.value.square)
        assertEquals(0u, c.estado.value.mascara)
    }

    @Test
    fun ultimo_boton_con_modificador() = runTest {
        val c = conexiones()
        conectarLosDos(c)
        listOf(
            botones(),
            botones(SquareButton.CampagnoloDerecho to 1),
            botones(SquareButton.PalancaDerecha1 to 1),
            botones(SquareButton.PalancaDerecha1 to 1),
            botones(SquareButton.CampagnoloDerecho to 1),
        ).forEach { square.notificaciones.tryEmit(it) }
        runCurrent()
        assertEquals(Entrada(SquareButton.PalancaDerecha1, SquareButton.CampagnoloDerecho), c.estado.value.ultimoGesto?.gesto?.entrada)
        assertEquals(Accion.MarchaMas(3), c.estado.value.ultimoGesto?.accion)
        assertEquals(0u, c.estado.value.mascara)
    }

    @Test
    fun notificacion_corta_no_cambia_nada() = runTest {
        val c = conexiones()
        conectarLosDos(c)
        square.notificaciones.tryEmit(Notificacion("0x45", ByteArray(7), 0L))
        // La máscara de 0x43 ya no mueve los botones: solo va al registro.
        square.notificaciones.tryEmit(Notificacion("0x43", byteArrayOf(0, 0, 0, -1, -1, -1, -1), 0L))
        runCurrent()
        assertEquals(0u, c.estado.value.mascara)
        assertNull(c.estado.value.ultimoGesto)
    }

    @Test
    fun envios_manuales_solo_con_el_neo_conectado() = runTest {
        val c = conexiones()
        c.controlManual(Potencia(200))
        runCurrent()
        assertTrue(neo.enviados.isEmpty())
        assertNull(c.estado.value.ultimoEnvio)

        conectarLosDos(c)
        c.controlManual(Potencia(200))
        c.controlManual(Pendiente(3.0))
        runCurrent()
        // El primero es la marcha 12 al conectar el Neo.
        assertArrayEquals(FecCodec.pendiente(0.0), neo.enviados[0])
        assertArrayEquals(FecCodec.potencia(200), neo.enviados[1])
        assertArrayEquals(FecCodec.pendiente(3.0), neo.enviados[2])
        assertEquals(Envio.Pendiente(3.0, 45_871_000L), c.estado.value.ultimoEnvio)
    }

    @Test
    fun conexion_del_neo_perdida_y_recuperada_reenvia_la_marcha_no_el_manual() = runTest {
        val c = conexiones()
        conectarLosDos(c)
        c.controlManual(Pendiente(3.0))
        runCurrent()
        neo.estado.value = EstadoEnlace.Perdida
        runCurrent()
        neo.estado.value = EstadoEnlace.Listo
        runCurrent()
        assertArrayEquals(FecCodec.pendiente(0.0), neo.enviados.last())
        assertEquals(Conectado, c.estado.value.neo)
    }

    @Test
    fun terminar_deja_el_neo_en_pendiente_cero_y_desconecta() = runTest {
        val c = conexiones()
        conectarLosDos(c)
        c.controlManual(Potencia(300))
        runCurrent()
        c.terminar()
        runCurrent()
        assertArrayEquals(FecCodec.pendiente(0.0), neo.enviados.last())
        assertEquals(SinConexion, c.estado.value.neo)
        assertEquals(SinConexion, c.estado.value.square)
    }

    @Test
    fun wifi_en_baja_latencia_mientras_hay_red_y_sesion() = runTest {
        configurarNeoYSquare()
        val c = conexiones()
        assertFalse(wifi.activo)
        wifi.direccion.value = "192.168.1.20"
        runCurrent()
        assertTrue(wifi.activo)
        assertTrue(registro.ultimas(5).any { it.texto == "Wifi: 192.168.1.20" })
        wifi.direccion.value = null
        runCurrent()
        assertFalse(wifi.activo)
        wifi.direccion.value = "192.168.1.20"
        runCurrent()
        c.terminar()
        runCurrent()
        assertFalse(wifi.activo)
    }

    @Test
    fun terminar_acaba_aunque_la_desconexion_del_square_no_termine() = runTest {
        // Con «conectando al Square» pendiente, `desconectar` puede no volver nunca: «Terminar» no se puede quedar colgado.
        val c = conMarcha(12)
        square.desconexionColgada = true
        val fin = launch { c.terminar() }
        advanceTimeBy(Sesion.LIMITE_DESCONEXION_MS + Sesion.LIMITE_TERMINAR_MS + 100)
        runCurrent()
        assertTrue(fin.isCompleted, "terminar debe acabar")
        assertEquals(EstadoDispositivo.SinConexion, c.estado.value.square)
        assertEquals(EstadoDispositivo.SinConexion, c.estado.value.neo)
    }

    @Test
    fun terminar_guarda_la_marcha() = runTest {
        val c = conexiones()
        conectarLosDos(c)
        pulsar(SquareButton.PalancaDerecha1)
        c.terminar()
        runCurrent()
        assertEquals(13, almacen.guardado.ultimaMarcha)
    }

    @Test
    fun terminar_sin_neo_no_envia() = runTest {
        val c = conexiones()
        c.terminar()
        runCurrent()
        assertTrue(neo.enviados.isEmpty())
    }

    @Test
    fun mientras_termina_una_palanca_ya_no_pisa_la_pendiente_cero() = runTest {
        val c = conMarcha(12)
        // La desconexión del Neo se cuelga: terminar() la espera con el Neo aún conectado, después de enviarle el 0 %.
        neo.desconexionColgada = true
        val fin = launch { c.terminar() }
        runCurrent()
        assertArrayEquals(FecCodec.pendiente(0.0), neo.enviados.last())
        val enviados = neo.enviados.size
        pulsar(SquareButton.PalancaDerecha1)
        assertEquals(enviados, neo.enviados.size)
        advanceTimeBy(Sesion.LIMITE_DESCONEXION_MS + 100)
        runCurrent()
        assertTrue(fin.isCompleted)
        assertArrayEquals(FecCodec.pendiente(0.0), neo.enviados.last())
    }

    @Test
    fun dos_buscar_seguidos_lanzan_una_sola_busqueda() = runTest {
        // Con un intento pendiente y una desconexión lenta, buscarTipos suspende al cancelarlo: el segundo toque llega entonces.
        almacen.guardado = EstadoGuardado(ultimaMarcha = 12)
        configurarNeoYSquare()
        neo.conecta = false
        val c = conexiones()
        assertTrue(neo.esperando)
        neo.tardaDesconexionMs = 1_000
        c.buscarRodillo()
        runCurrent()
        c.buscarRodillo()
        advanceTimeBy(1_100)
        runCurrent()
        assertEquals(listOf(setOf("Tacx Neo")), buscador.pedidos)
        assertEquals(Buscando, c.estado.value.neo)
    }

    @Test
    fun bluetooth_apagado() = runTest {
        val c = conexiones()
        conectarLosDos(c)
        bluetooth.encendido.value = false
        runCurrent()
        assertEquals(SinConexion, c.estado.value.neo)
        assertEquals(SinConexion, c.estado.value.square)
        assertEquals(false, c.estado.value.bluetoothEncendido)

        c.buscar()
        runCurrent()
        assertEquals(1, buscador.pedidos.size)
    }

    // --- Marchas -------------------------------------------------------------------------------------------------

    /** Pulsa y suelta [b] en dos notificaciones, con [modificador] mantenido si se indica. */
    private fun TestScope.pulsar(b: SquareButton, modificador: SquareButton? = null) {
        if (contadores.all { it == 2 }) square.notificaciones.tryEmit(botones())
        modificador?.let { square.notificaciones.tryEmit(botones(it to 1)) }
        square.notificaciones.tryEmit(botones(b to 1))
        square.notificaciones.tryEmit(botones(b to 1))
        modificador?.let { square.notificaciones.tryEmit(botones(it to 1)) }
        runCurrent()
    }

    private fun TestScope.conMarcha(n: Int): Sesion {
        almacen.guardado = EstadoGuardado(ultimaMarcha = n)
        return conexiones().also { conectarLosDos(it) }
    }

    @Test
    fun la_palanca_cambia_la_marcha_y_entrega_el_comando_sin_esperar() = runTest {
        val c = conMarcha(14)
        assertArrayEquals(FecCodec.pendiente(2.0), neo.enviados.last())
        val antes = currentTime
        square.notificaciones.tryEmit(botones())
        square.notificaciones.tryEmit(botones(SquareButton.PalancaDerecha1 to 1))
        runCurrent()
        // El comando ya está entregado sin que avance el reloj virtual: el camino caliente no suspende.
        assertEquals(antes, currentTime)
        assertArrayEquals(FecCodec.pendiente(3.0), neo.enviados.last())
        assertEquals(15, c.estado.value.marcha.posicion)
    }

    @Test
    fun saltos_extremos_y_referencia() = runTest {
        val c = conMarcha(14)
        pulsar(SquareButton.PalancaDerecha1, SquareButton.CampagnoloDerecho)
        assertEquals(17, c.estado.value.marcha.posicion)
        pulsar(SquareButton.PalancaIzquierda2, SquareButton.CampagnoloIzquierdo)
        assertEquals(1, c.estado.value.marcha.posicion)
        // Con «La marcha no crea bajadas», por defecto, la marcha 1 en llano no baja de 0 % (spec `trainer-resistance`).
        assertArrayEquals(FecCodec.pendiente(0.0), neo.enviados.last())
        pulsar(SquareButton.Cuadrado)
        assertEquals(12, c.estado.value.marcha.posicion)
    }

    // --- Bajadas (spec `settings-screen`, «Ajuste aplicado al momento») ---

    @Test
    fun cambiar_las_bajadas_rodando_envia_la_pendiente_nueva_sin_cambiar_la_marcha() = runTest {
        ajustes.configuracion.value = Configuracion(bajadas = ModoBajadas.SIN_PROTECCION)
        val c = conMarcha(5)
        assertArrayEquals(FecCodec.pendiente(-7.0), neo.enviados.last())
        ajustes.configuracion.value = Configuracion(bajadas = ModoBajadas.LA_MARCHA_NO_CREA_BAJADAS)
        runCurrent()
        assertArrayEquals(FecCodec.pendiente(0.0), neo.enviados.last())
        assertEquals(5, c.estado.value.marcha.posicion)
        assertEquals(ModoBajadas.LA_MARCHA_NO_CREA_BAJADAS, c.estado.value.simulacion.bajadas)
        assertEquals(-7.0, c.estado.value.simulacion.sinLimitePct)
    }

    @Test
    fun cambiar_las_bajadas_sin_neo_se_aplica_al_conectar() = runTest {
        ajustes.configuracion.value = Configuracion(bajadas = ModoBajadas.SIN_PROTECCION)
        almacen.guardado = EstadoGuardado(ultimaMarcha = 5)
        val c = conexiones()
        ajustes.configuracion.value = Configuracion(bajadas = ModoBajadas.NUNCA_EMPUJAR)
        runCurrent()
        assertTrue(neo.enviados.isEmpty())
        assertEquals(ModoBajadas.NUNCA_EMPUJAR, c.estado.value.simulacion.bajadas)
        conectarLosDos(c)
        assertArrayEquals(FecCodec.pendiente(0.0), neo.enviados.first())
    }

    @Test
    fun el_modo_guardado_se_aplica_desde_el_primer_envio() = runTest {
        ajustes.configuracion.value = Configuracion(bajadas = ModoBajadas.SIN_PROTECCION)
        conMarcha(1)
        assertArrayEquals(FecCodec.pendiente(-11.0), neo.enviados.first())
    }

    @Test
    fun cinco_pulsaciones_rapidas_terminan_en_la_marcha_final() = runTest {
        val c = conMarcha(12)
        square.notificaciones.tryEmit(botones())
        // Cinco pulsaciones completas en una sola notificación (diez transiciones del contador).
        square.notificaciones.tryEmit(botones(SquareButton.PalancaDerecha1 to 10))
        runCurrent()
        assertEquals(17, c.estado.value.marcha.posicion)
        assertArrayEquals(FecCodec.pendiente(5.0), neo.enviados.last())
    }

    @Test
    fun tope_no_envia_y_vibra_doble() = runTest {
        val c = conMarcha(24)
        val enviados = neo.enviados.size
        val eventos = mutableListOf<EventoRodaje>()
        backgroundScope.launch { c.eventos.collect { eventos += it } }
        runCurrent()
        pulsar(SquareButton.PalancaDerecha1)
        assertEquals(24, c.estado.value.marcha.posicion)
        assertEquals(enviados, neo.enviados.size)
        assertEquals(listOf(EventoRodaje.Tope(TopeMarcha.MasDura)), eventos)
        assertEquals(listOf("doble"), vibrador.vibraciones)
        assertEquals(ResultadoCambio.Tope(TopeMarcha.MasDura), c.estado.value.ultimoGesto?.resultado)
    }

    @Test
    fun cada_cambio_vibra_corta_y_emite_el_evento() = runTest {
        val c = conMarcha(23)
        val eventos = mutableListOf<EventoRodaje>()
        backgroundScope.launch { c.eventos.collect { eventos += it } }
        runCurrent()
        pulsar(SquareButton.PalancaDerecha1, SquareButton.CampagnoloDerecho)
        val e = eventos.single() as EventoRodaje.Cambio
        assertEquals(Accion.MarchaMas(3), e.accion)
        assertEquals(1, e.cambio.pasos)
        assertEquals(listOf("corta"), vibrador.vibraciones)
    }

    @Test
    fun cambios_sin_neo_se_aplican_al_conectarlo() = runTest {
        almacen.guardado = EstadoGuardado(ultimaMarcha = 14)
        val c = conexiones()
        c.buscar()
        runCurrent()
        buscador.resultados.trySend(ResultadoBusqueda.Encontrado("SQUARE", "BB:BB", "SQUARE 1234"))
        runCurrent()
        pulsar(SquareButton.PalancaDerecha1)
        pulsar(SquareButton.PalancaDerecha1)
        assertTrue(neo.enviados.isEmpty())
        assertEquals(16, c.estado.value.marcha.posicion)
        buscador.resultados.trySend(ResultadoBusqueda.Encontrado("Tacx Neo", "AA:AA", "Tacx Neo 12345"))
        runCurrent()
        assertArrayEquals(FecCodec.pendiente(4.0), neo.enviados.single())
    }

    @Test
    fun marcha_conservada_al_perder_el_square() = runTest {
        val c = conMarcha(17)
        square.estado.value = EstadoEnlace.Perdida
        runCurrent()
        assertEquals(17, c.estado.value.marcha.posicion)
    }

    @Test
    fun boton_sin_accion() = runTest {
        // Spec `button-mapping`, «Botón sin acción»: se quita la regla de A.
        val c = conMarcha(14)
        configurar { it.conAsignacion(it.asignacion.quitar(Entrada(SquareButton.A), TipoGesto.Pulsar)) }
        pulsar(SquareButton.A)
        assertEquals(14, c.estado.value.marcha.posicion)
        assertNull(c.estado.value.ultimoGesto?.accion)
    }

    // --- Control manual ------------------------------------------------------------------------------------------

    @Test
    fun manual_temporal_y_vuelta_a_la_marcha_al_salir_de_diagnostico() = runTest {
        val c = conMarcha(14)
        c.controlManual(Potencia(200))
        runCurrent()
        pulsar(SquareButton.PalancaDerecha1)
        assertArrayEquals(FecCodec.potencia(200), neo.enviados.last())
        assertEquals(15, c.estado.value.marcha.posicion)
        c.salirDiagnostico()
        runCurrent()
        assertArrayEquals(FecCodec.pendiente(3.0), neo.enviados.last())
    }

    // --- Latencia y registro -------------------------------------------------------------------------------------

    @Test
    fun latencia_de_la_palanca_hasta_la_entrega() = runTest {
        val c = conMarcha(12)
        assertNull(c.estado.value.latenciaMs)
        neo.porConfirmar.clear()
        ahora = 1_000
        square.notificaciones.tryEmit(botones())
        square.notificaciones.tryEmit(botones(SquareButton.PalancaDerecha1 to 1))
        runCurrent()
        ahora = 1_085
        neo.porConfirmar.single()(true)
        assertEquals(85L, c.estado.value.latenciaMs)
        val textos = registro.ultimas(20).map { it.texto }
        assertTrue(textos.any { it.startsWith("Latencia: notificación 1000, envío 1000, entrega 1085") }, textos.toString())
    }

    @Test
    fun el_registro_sigue_un_cambio_de_marcha_en_orden() = runTest {
        conMarcha(12)
        square.notificaciones.tryEmit(botones())
        square.notificaciones.tryEmit(botones(SquareButton.PalancaDerecha1 to 1))
        runCurrent()
        neo.porConfirmar.last()(true)
        val textos = registro.ultimas(10).asReversed().map { it.texto }
        val i = listOf(
            textos.indexOfFirst { it.startsWith("Comando: Pendiente(pct=1.0)") },
            textos.indexOfFirst { it.startsWith("Gesto: Gesto(entrada=Entrada(boton=PalancaDerecha1") && "Cambiada" in it },
            textos.indexOfFirst { it.startsWith("Latencia:") },
        )
        assertTrue(i.all { it >= 0 } && i == i.sorted(), textos.toString())
    }

    // --- Persistencia --------------------------------------------------------------------------------------------

    @Test
    fun primer_uso_empieza_en_la_12() = runTest {
        val c = conexiones()
        assertEquals(12, c.estado.value.marcha.posicion)
    }

    @Test
    fun guarda_la_marcha_dos_segundos_despues_del_ultimo_cambio() = runTest {
        val c = conMarcha(8)
        pulsar(SquareButton.PalancaDerecha1)
        advanceTimeBy(1_500)
        pulsar(SquareButton.PalancaDerecha1)
        advanceTimeBy(1_900)
        runCurrent()
        assertEquals(8, almacen.guardado.ultimaMarcha)
        advanceTimeBy(200)
        runCurrent()
        assertEquals(10, almacen.guardado.ultimaMarcha)
        assertEquals(10, c.estado.value.marcha.posicion)
    }

    // --- Dispositivos configurados (spec `device-connection`, «Configurar los dispositivos») -------------------------

    private val neoConfigurado = DispositivoConfigurado("AA:AA", "Tacx Neo 12345")
    private val squareConfigurado = DispositivoConfigurado("BB:BB", "SQUARE 1234")

    @Test
    fun primera_conexion_configurada() = runTest {
        val c = conexiones()
        conectarLosDos(c)
        assertEquals(neoConfigurado, almacen.guardado.rodillo)
        assertEquals(squareConfigurado, almacen.guardado.mando)
    }

    @Test
    fun otro_square_sustituye_al_configurado() = runTest {
        almacen.guardado = EstadoGuardado(rodillo = neoConfigurado, mando = squareConfigurado)
        square.conecta = false
        val c = conexiones()
        assertEquals(Conectado, c.estado.value.neo)
        assertEquals(Conectando, c.estado.value.square)
        c.buscar()
        runCurrent()
        assertEquals(setOf("SQUARE"), buscador.pedidos.single())
        square.conecta = true
        buscador.resultados.trySend(ResultadoBusqueda.Encontrado("SQUARE", "CC:CC", "SQUARE 9999"))
        runCurrent()
        assertEquals(Conectado, c.estado.value.square)
        assertEquals(DispositivoConfigurado("CC:CC", "SQUARE 9999"), almacen.guardado.mando)
        assertEquals(neoConfigurado, almacen.guardado.rodillo)
    }

    @Test
    fun un_fallo_tras_encontrarlo_no_se_recuerda() = runTest {
        val c = conexiones()
        neo.conecta = false
        c.buscar()
        runCurrent()
        buscador.resultados.trySend(ResultadoBusqueda.Encontrado("Tacx Neo", "AA:AA", "Tacx Neo 12345"))
        runCurrent()
        assertEquals(NoEncontrado, c.estado.value.neo)
        assertNull(almacen.guardado.rodillo)
    }

    // --- Conexión al abrir y reconexión (spec `device-connection`) -----------------------------------------------

    private fun TestScope.conConfigurados(marcha: Int = 12): Sesion {
        almacen.guardado = EstadoGuardado(ultimaMarcha = marcha, rodillo = neoConfigurado, mando = squareConfigurado)
        return conexiones()
    }

    @Test
    fun los_dos_al_abrir() = runTest {
        val c = conConfigurados(14)
        assertEquals(Conectado, c.estado.value.neo)
        assertEquals(Conectado, c.estado.value.square)
        assertTrue(buscador.pedidos.isEmpty())
        assertEquals(listOf("directa AA:AA"), neo.modos)
        assertEquals(listOf("directa BB:BB"), square.modos)
        assertArrayEquals(FecCodec.pendiente(2.0), neo.enviados.single())
    }

    @Test
    fun neo_dormido_al_abrir() = runTest {
        neo.conecta = false
        val c = conConfigurados(14)
        assertEquals(Conectando, c.estado.value.neo)
        assertEquals(Conectado, c.estado.value.square)
        assertEquals(listOf("directa AA:AA", "segundo plano AA:AA"), neo.modos)
        advanceTimeBy(60_000)
        runCurrent()
        assertEquals(Conectando, c.estado.value.neo)
        assertTrue(neo.esperando)
        neo.despertar()
        runCurrent()
        assertEquals(Conectado, c.estado.value.neo)
        assertArrayEquals(FecCodec.pendiente(2.0), neo.enviados.single())
        assertTrue(buscador.pedidos.isEmpty())
    }

    @Test
    fun primer_uso_sin_intento() = runTest {
        val c = conexiones()
        advanceTimeBy(60_000)
        runCurrent()
        assertEquals(SinConexion, c.estado.value.neo)
        assertEquals(SinConexion, c.estado.value.square)
        assertTrue(neo.modos.isEmpty() && square.modos.isEmpty())
        assertTrue(buscador.pedidos.isEmpty())
    }

    @Test
    fun permiso_concedido_mas_tarde() = runTest {
        bluetooth.permiso.value = false
        val c = conConfigurados()
        assertEquals(SinConexion, c.estado.value.neo)
        assertTrue(neo.modos.isEmpty() && square.modos.isEmpty())
        bluetooth.permiso.value = true
        runCurrent()
        assertEquals(Conectado, c.estado.value.neo)
        assertEquals(Conectado, c.estado.value.square)
        assertTrue(buscador.pedidos.isEmpty())
    }

    @Test
    fun corte_breve_del_square() = runTest {
        val c = conConfigurados()
        square.estado.value = EstadoEnlace.Perdida
        runCurrent()
        // Despierto: la directa lo recupera sin pasar a segundo plano.
        assertEquals(Conectado, c.estado.value.square)
        assertEquals(listOf("directa BB:BB", "directa BB:BB"), square.modos)
        assertEquals(Conectado, c.estado.value.neo)
    }

    @Test
    fun square_apagado_a_mitad() = runTest {
        val c = conConfigurados()
        square.conecta = false
        square.estado.value = EstadoEnlace.Perdida
        runCurrent()
        assertEquals(Reconectando, c.estado.value.square)
        assertEquals(Conectado, c.estado.value.neo)
        // Ausencia larga, sin límite de intentos.
        advanceTimeBy(30 * 60_000L)
        runCurrent()
        assertEquals(Reconectando, c.estado.value.square)
        assertEquals(listOf("directa BB:BB", "directa BB:BB", "segundo plano BB:BB"), square.modos)
        square.despertar()
        runCurrent()
        assertEquals(Conectado, c.estado.value.square)
    }

    @Test
    fun neo_perdido_con_la_17_recibe_la_17_al_volver() = runTest {
        val c = conConfigurados(17)
        neo.conecta = false
        neo.estado.value = EstadoEnlace.Perdida
        runCurrent()
        assertEquals(Reconectando, c.estado.value.neo)
        assertEquals(17, c.estado.value.marcha.posicion)
        assertNull(c.estado.value.potencia)
        val enviados = neo.enviados.size
        neo.despertar()
        runCurrent()
        assertEquals(Conectado, c.estado.value.neo)
        assertEquals(enviados + 1, neo.enviados.size)
        assertArrayEquals(FecCodec.pendiente(5.0), neo.enviados.last())
    }

    @Test
    fun cambios_a_la_19_mientras_reconecta_el_neo() = runTest {
        val c = conConfigurados(17)
        neo.conecta = false
        neo.estado.value = EstadoEnlace.Perdida
        runCurrent()
        val enviados = neo.enviados.size
        pulsar(SquareButton.PalancaDerecha1)
        pulsar(SquareButton.PalancaDerecha1)
        assertEquals(19, c.estado.value.marcha.posicion)
        assertEquals(enviados, neo.enviados.size)
        neo.despertar()
        runCurrent()
        assertArrayEquals(FecCodec.pendiente(7.0), neo.enviados.last())
    }

    @Test
    fun square_perdido_con_campag_d_mantenido() = runTest {
        val c = conConfigurados(17)
        square.notificaciones.tryEmit(botones())
        square.notificaciones.tryEmit(botones(SquareButton.CampagnoloDerecho to 1))
        runCurrent()
        assertEquals(SquareButton.CampagnoloDerecho.mascara, c.estado.value.mascara)
        square.conecta = false
        square.estado.value = EstadoEnlace.Perdida
        runCurrent()
        assertNull(c.estado.value.mascara)
        assertEquals(Conectado, c.estado.value.neo)
        assertEquals(17, c.estado.value.marcha.posicion)
        square.despertar()
        runCurrent()
        // Campag. D se soltó durante el corte: la primera notificación es la línea base y D1 sola sube una.
        square.notificaciones.tryEmit(botones(SquareButton.CampagnoloDerecho to 1))
        runCurrent()
        pulsar(SquareButton.PalancaDerecha1)
        assertEquals(18, c.estado.value.marcha.posicion)
    }

    @Test
    fun bluetooth_apagado_y_encendido_con_la_14() = runTest {
        val c = conConfigurados(14)
        bluetooth.encendido.value = false
        neo.estado.value = EstadoEnlace.Perdida
        square.estado.value = EstadoEnlace.Perdida
        runCurrent()
        assertEquals(SinConexion, c.estado.value.neo)
        assertEquals(SinConexion, c.estado.value.square)
        assertEquals(listOf("directa AA:AA"), neo.modos)
        bluetooth.encendido.value = true
        runCurrent()
        assertEquals(Conectado, c.estado.value.neo)
        assertEquals(Conectado, c.estado.value.square)
        assertArrayEquals(FecCodec.pendiente(2.0), neo.enviados.last())
        assertTrue(buscador.pedidos.isEmpty())
    }

    @Test
    fun sin_intentos_al_terminar_la_sesion() = runTest {
        neo.conecta = false
        val c = conConfigurados()
        assertTrue(neo.esperando)
        c.terminar()
        runCurrent()
        assertFalse(neo.esperando)
        assertEquals(SinConexion, c.estado.value.neo)
        assertEquals(SinConexion, c.estado.value.square)
        square.estado.value = EstadoEnlace.Perdida
        advanceTimeBy(60_000)
        runCurrent()
        assertEquals(listOf("directa AA:AA", "segundo plano AA:AA"), neo.modos)
        assertEquals(listOf("directa BB:BB"), square.modos)
    }

    // --- Búsqueda con un intento en curso (spec `device-connection`, decisión 4) ---------------------------------

    private fun TestScope.squareReconectando(): Sesion {
        val c = conConfigurados()
        square.conecta = false
        square.estado.value = EstadoEnlace.Perdida
        runCurrent()
        assertEquals(Reconectando, c.estado.value.square)
        assertTrue(square.esperando)
        return c
    }

    @Test
    fun buscar_mientras_reconecta_encuentra_otro_square() = runTest {
        val c = squareReconectando()
        c.buscar()
        runCurrent()
        assertFalse(square.esperando)
        assertEquals(Buscando, c.estado.value.square)
        assertEquals(setOf("SQUARE"), buscador.pedidos.single())
        square.conecta = true
        buscador.resultados.trySend(ResultadoBusqueda.Encontrado("SQUARE", "CC:CC", "SQUARE 9999"))
        runCurrent()
        assertEquals(Conectado, c.estado.value.square)
        assertEquals("directa CC:CC", square.modos.last())
        assertEquals(DispositivoConfigurado("CC:CC", "SQUARE 9999"), almacen.guardado.mando)
    }

    @Test
    fun busqueda_sin_resultado_mientras_reconecta() = runTest {
        val c = squareReconectando()
        c.buscar()
        runCurrent()
        buscador.resultados.trySend(ResultadoBusqueda.NoEncontrado("SQUARE"))
        runCurrent()
        assertEquals(NoEncontrado, c.estado.value.square)
        val modos = square.modos.toList()
        advanceTimeBy(10 * 60_000L)
        runCurrent()
        assertEquals(NoEncontrado, c.estado.value.square)
        assertEquals(modos, square.modos)
        assertFalse(square.esperando)
    }

    @Test
    fun bluetooth_apagado_y_encendido_tras_no_encontrado() = runTest {
        val c = squareReconectando()
        c.buscar()
        runCurrent()
        buscador.resultados.trySend(ResultadoBusqueda.NoEncontrado("SQUARE"))
        runCurrent()
        bluetooth.encendido.value = false
        runCurrent()
        assertEquals(SinConexion, c.estado.value.square)
        bluetooth.encendido.value = true
        runCurrent()
        assertEquals(Conectando, c.estado.value.square)
        assertEquals("segundo plano BB:BB", square.modos.last())
        square.despertar()
        runCurrent()
        assertEquals(Conectado, c.estado.value.square)
    }

    // --- Puente con Zwift --------------------------------------------------------------------------------------

    private fun hex(b: ByteArray) = b.joinToString(" ") { "%02x".format(it) }

    @Test
    fun publica_con_wifi_y_para_sin_wifi() = runTest {
        configurarNeoYSquare()
        val c = conexiones()
        assertEquals(EstadoPlataforma.SinWifi, c.estado.value.plataforma)
        assertEquals(0, transporte.publicaciones)
        wifi.direccion.value = "192.168.1.20"
        runCurrent()
        assertEquals(1, transporte.publicaciones)
        assertEquals(EstadoPlataforma.Esperando, c.estado.value.plataforma)
        transporte.conectarZwift()
        runCurrent()
        assertEquals(EstadoPlataforma.Conectado("192.168.0.10"), c.estado.value.plataforma)
        wifi.direccion.value = null
        runCurrent()
        assertEquals(EstadoTransporte.Parado, transporte.estado.value)
        assertEquals(EstadoPlataforma.SinWifi, c.estado.value.plataforma)
    }

    @Test
    fun anuncio_sin_respuesta_es_zwift_no_visible() = runTest {
        configurarNeoYSquare()
        val c = conexiones()
        wifi.direccion.value = "192.168.1.20"
        runCurrent()
        transporte.estado.value = EstadoTransporte.NoVisible
        runCurrent()
        assertEquals(EstadoPlataforma.NoVisible, c.estado.value.plataforma)
        transporte.estado.value = EstadoTransporte.Esperando
        runCurrent()
        assertEquals(EstadoPlataforma.Esperando, c.estado.value.plataforma)
    }

    @Test
    fun sin_wifi_gana_a_no_visible_y_al_volver_se_publica_otra_vez() = runTest {
        configurarNeoYSquare()
        val c = conexiones()
        wifi.direccion.value = "192.168.1.20"
        runCurrent()
        transporte.estado.value = EstadoTransporte.NoVisible
        runCurrent()
        wifi.direccion.value = null
        runCurrent()
        assertEquals(EstadoPlataforma.SinWifi, c.estado.value.plataforma)
        // Reiniciar el wifi vuelve a anunciar, y con ello a comprobar el anuncio.
        wifi.direccion.value = "192.168.1.20"
        runCurrent()
        assertEquals(2, transporte.publicaciones)
        assertEquals(EstadoPlataforma.Esperando, c.estado.value.plataforma)
    }

    @Test
    fun identidad_del_puente_generada_una_vez() = runTest {
        configurarNeoYSquare()
        conexiones()
        wifi.direccion.value = "192.168.1.20"
        runCurrent()
        val generada = checkNotNull(guardada(ModoConexion.FTMS_WIFI))
        assertEquals(generada, transporte.identidad)
        // La siguiente sesión usa la guardada.
        val otra = TransporteFalso()
        Sesion(buscador, square, neo, pulsometro, ventilador, bluetooth, wifi, fabrica(otra), renombrador, registro, almacen, ajustes, vibrador, backgroundScope, { ahora }, { 0L })
        runCurrent()
        assertEquals(generada, otra.identidad)
        assertEquals(generada, guardada(ModoConexion.FTMS_WIFI))
    }

    @Test
    fun telemetria_a_zwift_cuatro_veces_por_segundo() = runTest {
        val c = conexiones()
        conectarLosDos(c)
        neo.notificaciones.tryEmit(Notificacion(NeoClient.ETIQUETA_POTENCIA, byteArrayOf(0, 0, 0xF7.toByte(), 0), ahora))
        wifi.direccion.value = "192.168.1.20"
        runCurrent()
        transporte.conectarZwift(0x2AD2, 0x2A63)
        runCurrent()
        advanceTimeBy(1_001)
        val datos = transporte.notificaciones.filter { it.caracteristica == DirconCodec.uuid16(0x2AD2) }
        assertEquals(4, datos.size)
        assertEquals(4, transporte.notificaciones.count { it.caracteristica == DirconCodec.uuid16(0x2A63) })
        assertEquals("f7 00", hex(datos.last().valor.copyOfRange(6, 8)))
    }

    @Test
    fun sin_neo_la_telemetria_va_a_cero_y_sin_suscripcion_no_se_envia() = runTest {
        configurarNeoYSquare()
        conexiones()
        wifi.direccion.value = "192.168.1.20"
        runCurrent()
        transporte.conectarZwift()
        runCurrent()
        advanceTimeBy(1_001)
        assertTrue(transporte.notificaciones.isEmpty())
        checkNotNull(transporte.servidor).suscribir(DirconCodec.uuid16(0x2AD2), true)
        advanceTimeBy(251)
        assertEquals("44 00 00 00 00 00 00 00", hex(transporte.notificaciones.single().valor))
    }

    @Test
    fun la_telemetria_para_al_irse_zwift() = runTest {
        configurarNeoYSquare()
        conexiones()
        wifi.direccion.value = "192.168.1.20"
        runCurrent()
        transporte.conectarZwift(0x2AD2)
        runCurrent()
        advanceTimeBy(501)
        transporte.estado.value = EstadoTransporte.Esperando
        runCurrent()
        val enviadas = transporte.notificaciones.size
        advanceTimeBy(1_000)
        assertEquals(enviadas, transporte.notificaciones.size)
    }

    @Test
    fun el_punto_de_control_responde_por_notificacion() = runTest {
        configurarNeoYSquare()
        conexiones()
        wifi.direccion.value = "192.168.1.20"
        runCurrent()
        val s = checkNotNull(transporte.servidor)
        s.suscribir(DirconCodec.uuid16(0x2AD9), true)
        val r = s.escribir(DirconCodec.uuid16(0x2AD9), byteArrayOf(0x00)) as ResultadoGatt.Ok
        assertEquals("80 00 01", hex(r.valor.single().valor))
        val n = s.escribir(DirconCodec.uuid16(0x2AD9), byteArrayOf(0x04, 0x64)) as ResultadoGatt.Ok
        assertEquals("80 04 02", hex(n.valor.single().valor))
    }

    @Test
    fun terminar_deja_de_anunciarse() = runTest {
        val c = conexiones()
        wifi.direccion.value = "192.168.1.20"
        runCurrent()
        c.terminar()
        runCurrent()
        assertEquals(EstadoTransporte.Parado, transporte.estado.value)
    }

    // --- Zwift en la sesión: terreno, ERG, desconexión y control manual (bloque 6) ------------------------------

    /** Sesión con el Neo y el Square conectados en [marcha] y Zwift conectado y suscrito al punto de control. */
    private fun TestScope.enZwift(marcha: Int = 12): Sesion {
        val c = conMarcha(marcha)
        wifi.direccion.value = "192.168.1.20"
        runCurrent()
        transporte.conectarZwift(0x2AD9)
        runCurrent()
        return c
    }

    /** Zwift escribe en el punto de control (`11 …` simulación, `05 …` potencia). */
    private fun TestScope.zwift(h: String) {
        val r = checkNotNull(transporte.servidor).escribir(DirconCodec.uuid16(0x2AD9), h.split(" ").map { it.toInt(16).toByte() }.toByteArray())
        assertTrue(r is ResultadoGatt.Ok)
        runCurrent()
    }

    private val pendiente4 = "11 00 00 90 01 28 33"
    private val pendiente5 = "11 00 00 f4 01 28 33"
    private val pendiente6 = "11 00 00 58 02 28 33"
    private val pendiente2 = "11 00 00 c8 00 28 33"
    private val vatios230 = "05 e6 00"
    private val vatios280 = "05 18 01"

    private fun ultimo() = neo.enviados.last()

    @Test
    fun ruta_de_zwift_con_cambio_de_marcha() = runTest {
        val c = enZwift(12)
        zwift(pendiente4)
        assertArrayEquals(FecCodec.pendiente(4.0), ultimo())
        pulsar(SquareButton.PalancaDerecha1)
        pulsar(SquareButton.PalancaDerecha1)
        assertArrayEquals(FecCodec.pendiente(6.0), ultimo())
        assertEquals(14, c.estado.value.marcha.posicion)
    }

    @Test
    fun bajada_de_zwift() = runTest {
        enZwift(10)
        zwift("11 00 00 d4 fe 28 33")
        // −3 % de Zwift en la marcha 10: por defecto, la marcha no hace la bajada más pronunciada (spec `zwift-bridge`).
        assertArrayEquals(FecCodec.pendiente(-3.0), ultimo())
    }

    @Test
    fun bajada_de_zwift_sin_proteccion() = runTest {
        ajustes.configuracion.value = Configuracion(bajadas = ModoBajadas.SIN_PROTECCION)
        enZwift(10)
        zwift("11 00 00 d4 fe 28 33")
        assertArrayEquals(FecCodec.pendiente(-5.0), ultimo())
    }

    @Test
    fun terreno_muy_seguido_se_limita_y_llega_el_ultimo() = runTest {
        enZwift(12)
        zwift(pendiente4)
        val enviados = neo.enviados.size
        ahora = 100
        zwift("11 00 00 c2 01 28 33") // 4,50 %
        ahora = 200
        zwift("11 00 00 e0 01 28 33") // 4,80 %
        assertEquals(enviados, neo.enviados.size)
        ahora = 250
        advanceTimeBy(151)
        assertEquals(enviados + 1, neo.enviados.size)
        assertArrayEquals(FecCodec.pendiente(4.8), ultimo())
    }

    @Test
    fun la_palanca_no_espera_a_la_ventana_del_terreno() = runTest {
        enZwift(12)
        zwift(pendiente4)
        ahora = 100
        zwift(pendiente5)
        assertArrayEquals(FecCodec.pendiente(4.0), ultimo())
        val antes = currentTime
        ahora = 120
        pulsar(SquareButton.PalancaDerecha1)
        assertEquals(antes, currentTime)
        assertArrayEquals(FecCodec.pendiente(6.0), ultimo())
        val enviados = neo.enviados.size
        advanceTimeBy(1_000)
        assertEquals(enviados, neo.enviados.size)
    }

    @Test
    fun entrenamiento_erg_con_palancas_y_cuadrado() = runTest {
        val c = enZwift(14)
        val eventos = mutableListOf<EventoRodaje>()
        backgroundScope.launch { c.eventos.collect { eventos += it } }
        zwift(vatios230)
        assertArrayEquals(FecCodec.potencia(230), ultimo())
        pulsar(SquareButton.PalancaDerecha1)
        assertArrayEquals(FecCodec.potencia(240), ultimo())
        assertEquals(14, c.estado.value.marcha.posicion)
        pulsar(SquareButton.PalancaIzquierda1, SquareButton.CampagnoloIzquierdo)
        assertArrayEquals(FecCodec.potencia(210), ultimo())
        zwift(vatios280)
        assertArrayEquals(FecCodec.potencia(260), ultimo())
        pulsar(SquareButton.Cuadrado)
        assertArrayEquals(FecCodec.potencia(280), ultimo())
        val enviados = neo.enviados.size
        pulsar(SquareButton.PalancaDerecha2, SquareButton.CampagnoloDerecho)
        assertEquals(enviados, neo.enviados.size)
        assertEquals(14, c.estado.value.marcha.posicion)
        assertEquals(
            listOf(
                EventoRodaje.Ajuste(Accion.MarchaMas(1), ResultadoAjuste.Cambiado(10, 10)),
                EventoRodaje.Ajuste(Accion.MarchaMenos(3), ResultadoAjuste.Cambiado(-20, -30)),
                EventoRodaje.Ajuste(Accion.MarchaReferencia, ResultadoAjuste.Quitado),
            ),
            eventos,
        )
    }

    @Test
    fun tope_de_potencia_en_erg_vibra_como_un_tope() = runTest {
        val c = enZwift(12)
        val eventos = mutableListOf<EventoRodaje>()
        backgroundScope.launch { c.eventos.collect { eventos += it } }
        zwift("05 3c 00") // 60 W
        pulsar(SquareButton.PalancaIzquierda1)
        assertArrayEquals(FecCodec.potencia(50), ultimo())
        vibrador.vibraciones.clear()
        pulsar(SquareButton.PalancaIzquierda1)
        assertEquals(listOf("doble"), vibrador.vibraciones)
        assertEquals(EventoRodaje.TopePotencia(50), eventos.last())
    }

    @Test
    fun fin_del_entrenamiento_vuelve_la_marcha_y_el_ajuste_a_cero() = runTest {
        val c = enZwift(14)
        zwift(vatios230)
        pulsar(SquareButton.PalancaDerecha1)
        zwift(pendiente2)
        assertArrayEquals(FecCodec.pendiente(4.0), ultimo())
        assertEquals(14, c.estado.value.marcha.posicion)
        zwift(vatios230)
        assertArrayEquals(FecCodec.potencia(230), ultimo())
    }

    @Test
    fun corte_breve_de_zwift_mantiene_el_terreno() = runTest {
        enZwift(12)
        zwift(pendiente5)
        transporte.estado.value = EstadoTransporte.Esperando
        runCurrent()
        advanceTimeBy(10_000)
        ahora = 10_000
        assertArrayEquals(FecCodec.pendiente(5.0), ultimo())
        transporte.conectarZwift(0x2AD9)
        runCurrent()
        zwift(pendiente6)
        assertArrayEquals(FecCodec.pendiente(6.0), ultimo())
        advanceTimeBy(30_000)
        assertArrayEquals(FecCodec.pendiente(6.0), ultimo())
    }

    @Test
    fun zwift_cerrado_vuelve_a_la_pendiente_base_a_los_30_s() = runTest {
        val c = enZwift(14)
        zwift(pendiente5)
        assertArrayEquals(FecCodec.pendiente(7.0), ultimo())
        transporte.estado.value = EstadoTransporte.Esperando
        runCurrent()
        assertTrue(c.estado.value.simulacion.plataformaEnEspera)
        advanceTimeBy(29_000)
        assertArrayEquals(FecCodec.pendiente(7.0), ultimo())
        advanceTimeBy(1_001)
        assertArrayEquals(FecCodec.pendiente(2.0), ultimo())
        assertEquals(ModoControl.SimLibre, c.estado.value.simulacion.modo)
        pulsar(SquareButton.PalancaDerecha1)
        assertArrayEquals(FecCodec.pendiente(3.0), ultimo())
    }

    @Test
    fun zwift_cerrado_en_un_entrenamiento() = runTest {
        enZwift(12)
        zwift("05 fa 00") // 250 W
        transporte.estado.value = EstadoTransporte.Esperando
        runCurrent()
        advanceTimeBy(29_000)
        assertArrayEquals(FecCodec.potencia(250), ultimo())
        advanceTimeBy(1_001)
        assertArrayEquals(FecCodec.pendiente(0.0), ultimo())
    }

    @Test
    fun wifi_perdido_rodando_en_zwift() = runTest {
        val c = enZwift(12)
        zwift(pendiente5)
        wifi.direccion.value = null
        runCurrent()
        assertEquals(EstadoPlataforma.SinWifi, c.estado.value.plataforma)
        advanceTimeBy(30_001)
        assertArrayEquals(FecCodec.pendiente(0.0), ultimo())
        wifi.direccion.value = "192.168.1.20"
        runCurrent()
        assertEquals(2, transporte.publicaciones)
        assertEquals(EstadoPlataforma.Esperando, c.estado.value.plataforma)
    }

    @Test
    fun otro_cliente_que_sustituye_no_cuenta_como_perdida() = runTest {
        enZwift(12)
        zwift(pendiente5)
        transporte.conectarZwift(0x2AD9)
        runCurrent()
        advanceTimeBy(31_000)
        assertArrayEquals(FecCodec.pendiente(5.0), ultimo())
    }

    @Test
    fun neo_perdido_con_zwift_conectado_manda_ceros_y_al_volver_el_terreno() = runTest {
        enZwift(14)
        checkNotNull(transporte.servidor).suscribir(DirconCodec.uuid16(0x2AD2), true)
        neo.notificaciones.tryEmit(Notificacion(NeoClient.ETIQUETA_POTENCIA, byteArrayOf(0, 0, 0xC8.toByte(), 0), ahora))
        zwift(pendiente4)
        neo.conecta = false
        neo.estado.value = EstadoEnlace.Perdida
        runCurrent()
        transporte.notificaciones.clear()
        advanceTimeBy(251)
        assertEquals("44 00 00 00 00 00 00 00", hex(transporte.notificaciones.last().valor))
        neo.despertar()
        runCurrent()
        assertArrayEquals(FecCodec.pendiente(6.0), ultimo())
    }

    @Test
    fun envio_manual_rodando_en_zwift() = runTest {
        val c = enZwift(13)
        zwift(pendiente4)
        c.controlManual(Pendiente(3.0))
        runCurrent()
        zwift(pendiente6)
        assertArrayEquals(FecCodec.pendiente(3.0), ultimo())
        assertEquals(6.0, (c.estado.value.simulacion.modo as ModoControl.Manual).anterior.let { (it as ModoControl.SimPlataforma).terreno.pendientePct })
        c.salirDiagnostico()
        runCurrent()
        assertArrayEquals(FecCodec.pendiente(7.0), ultimo())
    }

    @Test
    fun envio_manual_en_un_entrenamiento() = runTest {
        val c = enZwift(12)
        zwift(vatios230)
        pulsar(SquareButton.PalancaDerecha1)
        c.controlManual(Potencia(150))
        runCurrent()
        c.salirDiagnostico()
        runCurrent()
        assertArrayEquals(FecCodec.potencia(240), ultimo())
    }

    @Test
    fun simulacion_con_lo_que_pide_zwift() = runTest {
        val c = enZwift(14)
        zwift(pendiente4)
        val s = c.estado.value.simulacion
        assertEquals(ModoControl.SimPlataforma(app.smartbike.dominio.Terreno(4.0, 0.004, 0.51, 0.0)), s.modo)
        assertEquals(Pendiente(6.0), s.enviado)
        assertEquals(2.0, s.ajustePct)
        assertEquals(45_871_000L, s.ultimoPlataforma?.horaMs)
        zwift(vatios230)
        pulsar(SquareButton.PalancaDerecha1)
        assertEquals(ModoControl.ErgPlataforma(230, 10), c.estado.value.simulacion.modo)
        assertEquals(Potencia(240), c.estado.value.simulacion.enviado)
    }

    @Test
    fun terminar_rodando_en_zwift_para_el_puente_y_los_temporizadores() = runTest {
        val c = enZwift(12)
        zwift(pendiente5)
        transporte.estado.value = EstadoTransporte.Esperando
        runCurrent()
        c.terminar()
        runCurrent()
        val enviados = neo.enviados.size
        advanceTimeBy(60_000)
        assertEquals(enviados, neo.enviados.size)
        assertEquals(EstadoTransporte.Parado, transporte.estado.value)
        assertFalse(wifi.activo)
    }

    // --- Gestos y asignación (change gestos-y-asignacion) -----------------------------------------------------------

    private fun TestScope.mantener(vararg b: SquareButton) {
        if (contadores.all { it == 2 }) square.notificaciones.tryEmit(botones())
        b.forEach { square.notificaciones.tryEmit(botones(it to 1)) }
        runCurrent()
    }

    private fun TestScope.soltar(vararg b: SquareButton) = mantener(*b)

    private fun TestScope.esperar(ms: Long) {
        advanceTimeBy(ms)
        runCurrent()
    }

    private fun TestScope.configurar(cambio: (Configuracion) -> Configuracion) {
        ajustes.configuracion.value = cambio(ajustes.configuracion.value)
        runCurrent()
    }

    private fun TestScope.eventosDe(c: Sesion) = mutableListOf<EventoRodaje>().also { l ->
        backgroundScope.launch { c.eventos.collect { l += it } }
        runCurrent()
    }

    @Test
    fun palanca_mantenida_repite_tras_el_retardo_y_a_su_ritmo() = runTest {
        val c = conMarcha(10)
        mantener(SquareButton.PalancaDerecha1)
        assertEquals(11, c.estado.value.marcha.posicion)
        esperar(799)
        assertEquals(11, c.estado.value.marcha.posicion)
        esperar(1)
        assertEquals(12, c.estado.value.marcha.posicion)
        assertArrayEquals(FecCodec.pendiente(0.0), neo.enviados.last())
        esperar(250)
        assertEquals(13, c.estado.value.marcha.posicion)
        assertEquals(TipoGesto.Repetir, c.estado.value.ultimoGesto?.gesto?.tipo)
        soltar(SquareButton.PalancaDerecha1)
        esperar(2_000)
        assertEquals(13, c.estado.value.marcha.posicion)
        assertEquals(listOf("corta", "corta", "corta"), vibrador.vibraciones)
    }

    @Test
    fun pulsacion_normal_de_650_ms_no_repite() = runTest {
        val c = conMarcha(10)
        mantener(SquareButton.PalancaDerecha1)
        esperar(650)
        soltar(SquareButton.PalancaDerecha1)
        esperar(2_000)
        assertEquals(11, c.estado.value.marcha.posicion)
    }

    @Test
    fun autorrepeticion_desactivada() = runTest {
        ajustes.configuracion.value = Configuracion(botones = ConfiguracionBotones(autorrepeticion = false))
        val c = conMarcha(10)
        mantener(SquareButton.PalancaDerecha1)
        esperar(2_000)
        assertEquals(11, c.estado.value.marcha.posicion)
    }

    @Test
    fun tope_durante_la_repeticion_se_avisa_una_vez() = runTest {
        val c = conMarcha(22)
        val eventos = eventosDe(c)
        mantener(SquareButton.PalancaDerecha1)
        esperar(3_000)
        assertEquals(24, c.estado.value.marcha.posicion)
        assertEquals(EventoRodaje.Tope(TopeMarcha.MasDura), eventos.last())
        assertEquals(1, eventos.count { it is EventoRodaje.Tope })
        assertEquals(listOf("corta", "corta", "doble"), vibrador.vibraciones)
    }

    @Test
    fun square_perdido_con_la_palanca_mantenida_para_la_repeticion() = runTest {
        val c = conMarcha(10)
        mantener(SquareButton.PalancaDerecha1)
        esperar(800)
        assertEquals(12, c.estado.value.marcha.posicion)
        square.conecta = false
        square.estado.value = EstadoEnlace.Perdida
        runCurrent()
        esperar(5_000)
        assertEquals(12, c.estado.value.marcha.posicion)
    }

    @Test
    fun cuadrado_corto_vuelve_y_largo_fija_la_referencia() = runTest {
        val c = conMarcha(20)
        val eventos = eventosDe(c)
        mantener(SquareButton.Cuadrado)
        assertEquals(20, c.estado.value.marcha.posicion)
        esperar(200)
        soltar(SquareButton.Cuadrado)
        assertEquals(12, c.estado.value.marcha.posicion)

        repeat(3) { pulsar(SquareButton.PalancaDerecha1) }
        vibrador.vibraciones.clear()
        mantener(SquareButton.Cuadrado)
        esperar(800)
        assertEquals(lineal(15), c.estado.value.referencia)
        assertEquals(15, c.estado.value.marcha.posicion)
        assertEquals(EventoRodaje.ReferenciaFijada(lineal(15)), eventos.last())
        assertEquals(listOf("larga"), vibrador.vibraciones)
        assertEquals(15, ajustes.configuracion.value.lineal.referencia)
        soltar(SquareButton.Cuadrado)
        assertEquals(15, c.estado.value.marcha.posicion)

        pulsar(SquareButton.PalancaDerecha1)
        pulsar(SquareButton.Cuadrado)
        assertEquals(15, c.estado.value.marcha.posicion)
    }

    @Test
    fun referencia_guardada_al_abrir() = runTest {
        ajustes.configuracion.value = Configuracion(lineal = AjustesLineal(referencia = 15))
        val c = conMarcha(20)
        assertEquals(lineal(15), c.estado.value.referencia)
        pulsar(SquareButton.Cuadrado)
        assertEquals(15, c.estado.value.marcha.posicion)
    }

    @Test
    fun cambio_de_asignacion_en_caliente() = runTest {
        val c = conMarcha(14)
        configurar {
            it.conAsignacion(it.asignacion.asignar(Entrada(SquareButton.PalancaDerecha1), TipoGesto.Pulsar, Accion.MarchaMenos(3)))
        }
        pulsar(SquareButton.PalancaDerecha1)
        assertEquals(11, c.estado.value.marcha.posicion)
        configurar { it.conAsignacion(Asignacion.POR_DEFECTO) }
        pulsar(SquareButton.PalancaDerecha1)
        assertEquals(12, c.estado.value.marcha.posicion)
    }

    @Test
    fun palancas_invertidas() = runTest {
        val c = conMarcha(14)
        configurar { it.conAsignacion(it.asignacion.copy(invertirPalancas = true)) }
        pulsar(SquareButton.PalancaDerecha1)
        assertEquals(13, c.estado.value.marcha.posicion)
        pulsar(SquareButton.PalancaDerecha1, SquareButton.CampagnoloDerecho)
        assertEquals(10, c.estado.value.marcha.posicion)
    }

    @Test
    fun doble_pulsacion_vibra_triple_y_sin_accion_no_vibra() = runTest {
        val c = conMarcha(14)
        configurar {
            val a = it.asignacion.asignar(Entrada(SquareButton.Triangulo), TipoGesto.Corta, Accion.MarchaMas(1))
                .asignar(Entrada(SquareButton.Triangulo), TipoGesto.Doble, Accion.MarchaMenos(3))
            it.conAsignacion(a)
        }
        pulsar(SquareButton.Y)
        assertTrue(vibrador.vibraciones.isEmpty())
        pulsar(SquareButton.Triangulo)
        esperar(100)
        pulsar(SquareButton.Triangulo)
        assertEquals(11, c.estado.value.marcha.posicion)
        assertEquals(listOf("triple"), vibrador.vibraciones)
        esperar(1_000)
        assertEquals(11, c.estado.value.marcha.posicion)
        pulsar(SquareButton.Triangulo)
        assertEquals(11, c.estado.value.marcha.posicion)
        esperar(300)
        assertEquals(12, c.estado.value.marcha.posicion)
    }

    @Test
    fun erg_con_repeticion_y_referencia() = runTest {
        val c = enZwift(15)
        zwift(vatios230)
        mantener(SquareButton.PalancaDerecha1)
        assertArrayEquals(FecCodec.potencia(240), ultimo())
        esperar(800)
        assertArrayEquals(FecCodec.potencia(250), ultimo())
        esperar(250)
        assertArrayEquals(FecCodec.potencia(260), ultimo())
        soltar(SquareButton.PalancaDerecha1)
        assertEquals(15, c.estado.value.marcha.posicion)
        // Spec `zwift-bridge`, «Fijar la referencia en ERG»: la referencia cambia y la potencia no.
        val enviados = neo.enviados.size
        mantener(SquareButton.Cuadrado)
        esperar(800)
        soltar(SquareButton.Cuadrado)
        assertEquals(lineal(15), c.estado.value.referencia)
        assertEquals(enviados, neo.enviados.size)
    }

    @Test
    fun apagar_el_square_con_las_palancas_primero_no_cambia_la_marcha() = runTest {
        val c = conMarcha(14)
        mantener(SquareButton.PalancaIzquierda1)
        mantener(SquareButton.PalancaDerecha1)
        mantener(SquareButton.CampagnoloIzquierdo)
        assertEquals(14, c.estado.value.marcha.posicion)
        mantener(SquareButton.CampagnoloDerecho)
        assertEquals(14, c.estado.value.marcha.posicion)
        assertArrayEquals(FecCodec.pendiente(2.0), neo.enviados.last())
        square.conecta = false
        square.estado.value = EstadoEnlace.Perdida
        runCurrent()
        esperar(5_000)
        assertEquals(14, c.estado.value.marcha.posicion)
        assertArrayEquals(FecCodec.pendiente(2.0), neo.enviados.last())
    }

    @Test
    fun apagar_el_square_con_los_campagnolo_primero_no_cambia_la_marcha() = runTest {
        val c = conMarcha(14)
        mantener(SquareButton.CampagnoloIzquierdo)
        mantener(SquareButton.CampagnoloDerecho)
        mantener(SquareButton.PalancaIzquierda1)
        mantener(SquareButton.PalancaDerecha1)
        assertEquals(14, c.estado.value.marcha.posicion)
    }

    @Test
    fun apagar_el_square_deshace_un_salto_de_la_rafaga() = runTest {
        val c = conMarcha(14)
        mantener(SquareButton.CampagnoloIzquierdo)
        mantener(SquareButton.PalancaIzquierda1)
        assertEquals(11, c.estado.value.marcha.posicion)
        mantener(SquareButton.CampagnoloDerecho, SquareButton.PalancaDerecha1)
        assertEquals(14, c.estado.value.marcha.posicion)
        // Al soltar los cuatro, los botones vuelven a cambiar la marcha.
        soltar(SquareButton.CampagnoloIzquierdo, SquareButton.PalancaIzquierda1, SquareButton.CampagnoloDerecho, SquareButton.PalancaDerecha1)
        pulsar(SquareButton.PalancaDerecha1)
        assertEquals(15, c.estado.value.marcha.posicion)
    }

    @Test
    fun apagar_el_square_como_en_la_traza_real() = runTest {
        // `square-combinaciones.txt`: Campag. D y, en la misma notificación, I1, Campag. I y D1. I1 llega antes que
        // Campag. I (orden de bit) y hace Campag. D + I1 → −1; al estar los cuatro, se deshace.
        val c = conMarcha(24)
        mantener(SquareButton.CampagnoloDerecho)
        square.notificaciones.tryEmit(botones(SquareButton.PalancaIzquierda1 to 1, SquareButton.CampagnoloIzquierdo to 1, SquareButton.PalancaDerecha1 to 1))
        runCurrent()
        assertEquals(24, c.estado.value.marcha.posicion)
        assertArrayEquals(FecCodec.pendiente(12.0), neo.enviados.last())
        esperar(4_600)
        square.conecta = false
        square.estado.value = EstadoEnlace.Perdida
        runCurrent()
        assertEquals(24, c.estado.value.marcha.posicion)
    }

    @Test
    fun sin_completar_el_apagado_la_marcha_se_queda() = runTest {
        val c = conMarcha(14)
        mantener(SquareButton.PalancaIzquierda1)
        soltar(SquareButton.PalancaIzquierda1)
        mantener(SquareButton.CampagnoloDerecho)
        mantener(SquareButton.PalancaDerecha1)
        soltar(SquareButton.PalancaDerecha1, SquareButton.CampagnoloDerecho)
        assertEquals(16, c.estado.value.marcha.posicion)
    }

    @Test
    fun captura_sin_ejecutar_acciones() = runTest {
        val c = conMarcha(14)
        c.empezarCaptura()
        runCurrent()
        assertEquals(EstadoCaptura.Esperando, c.captura.value)
        pulsar(SquareButton.PalancaDerecha1)
        assertEquals(EstadoCaptura.Capturada(Entrada(SquareButton.PalancaDerecha1)), c.captura.value)
        assertEquals(14, c.estado.value.marcha.posicion)
        pulsar(SquareButton.Triangulo, SquareButton.CampagnoloDerecho)
        assertEquals(EstadoCaptura.Capturada(Entrada(SquareButton.Triangulo, SquareButton.CampagnoloDerecho)), c.captura.value)
        pulsar(SquareButton.CampagnoloIzquierdo)
        assertEquals(EstadoCaptura.Capturada(Entrada(SquareButton.CampagnoloIzquierdo)), c.captura.value)
        mantener(SquareButton.Cuadrado)
        esperar(2_000)
        soltar(SquareButton.Cuadrado)
        assertEquals(14, c.estado.value.marcha.posicion)
        assertEquals(lineal(12), c.estado.value.referencia)

        square.conecta = false
        square.estado.value = EstadoEnlace.Perdida
        runCurrent()
        assertEquals(EstadoCaptura.SinSquare, c.captura.value)
        square.despertar()
        runCurrent()
        assertEquals(EstadoCaptura.Esperando, c.captura.value)

        c.terminarCaptura()
        runCurrent()
        assertNull(c.captura.value)
        square.notificaciones.tryEmit(botones())
        pulsar(SquareButton.PalancaDerecha1)
        assertEquals(15, c.estado.value.marcha.posicion)
    }

    @Test
    fun captura_sin_square() = runTest {
        val c = conexiones()
        c.empezarCaptura()
        runCurrent()
        assertEquals(EstadoCaptura.SinSquare, c.captura.value)
    }

    @Test
    fun la_palanca_no_espera_a_ningun_temporizador() = runTest {
        // NF1: con la asignación por defecto, la palanca entrega el comando en el paso de su notificación, aunque
        // quede programada la repetición.
        val c = conMarcha(14)
        mantener(SquareButton.PalancaDerecha1)
        assertArrayEquals(FecCodec.pendiente(3.0), neo.enviados.last())
        assertEquals(0, currentTime)
        assertEquals(15, c.estado.value.marcha.posicion)
    }

    // --- Shimano 105 (change `transmisiones-de-marca`) --------------------------------------------------------------

    private val shimano = Desarrollos.SHIMANO_105
    private fun s(plato: Int, pinon: Int) = EstadoMarcha.PlatoPinon(plato, pinon)
    private fun pendienteShimano(m: EstadoMarcha.PlatoPinon) = FecCodec.pendiente(ModeloSimple(1.0, 12, shimano).ajuste(m))

    private fun TestScope.conShimano(): Sesion {
        ajustes.configuracion.value = Configuracion(transmision = shimano.id)
        return conMarcha(12)
    }

    @Test
    fun importar_una_configuracion_rodando_no_corta_las_conexiones() = runTest {
        // Spec `settings-backup`, «Importar rodando»: se aplica la configuración entera, como al cambiarla a mano.
        val c = conMarcha(17)
        assertEquals(Conectado, c.estado.value.neo)
        assertEquals(Conectado, c.estado.value.square)
        val importada = Configuracion(transmision = shimano.id, tema = TemaApp.CLARO, conexion = config(ModoConexion.ZWIFT_RIDE).conexion)
        ajustes.actualizarConfiguracion { importada }
        runCurrent()
        assertEquals(shimano, c.estado.value.transmision)
        assertEquals(Conectado, c.estado.value.neo)
        assertEquals(Conectado, c.estado.value.square)
        assertEquals(listOf("AA:AA"), neo.conexiones)
        assertEquals(listOf("BB:BB"), square.conexiones)
    }

    @Test
    fun cambiar_el_idioma_rodando_no_toca_la_marcha_ni_las_conexiones_ni_el_rodillo() = runTest {
        // Spec `app-language`, «Cambiar rodando»: la marcha, los enlaces y lo enviado al Neo no cambian.
        val c = conMarcha(14)
        val enviados = neo.enviados.size
        val ultimo = neo.enviados.last()
        val eventos = eventosDe(c)
        configurar { it.copy(idioma = IdiomaApp.EN) }
        configurar { it.copy(idioma = IdiomaApp.ES) }
        assertEquals(14, c.estado.value.marcha.posicion)
        assertEquals(Conectado, c.estado.value.neo)
        assertEquals(Conectado, c.estado.value.square)
        assertEquals(listOf("AA:AA"), neo.conexiones)
        assertEquals(listOf("BB:BB"), square.conexiones)
        assertEquals(enviados, neo.enviados.size)
        assertArrayEquals(ultimo, neo.enviados.last())
        assertTrue(eventos.isEmpty())
        assertTrue(vibrador.vibraciones.isEmpty())
    }

    @Test
    fun cambiar_de_transmision_ida_y_vuelta() = runTest {
        // Spec `virtual-gears`, «Cambiar de transmisión», «Ida y vuelta» y «Rodando».
        val c = conMarcha(17)
        assertArrayEquals(FecCodec.pendiente(5.0), neo.enviados.last())
        val eventos = eventosDe(c)
        configurar { it.copy(transmision = shimano.id) }
        assertEquals(s(36, 17), c.estado.value.marcha)
        assertEquals(shimano, c.estado.value.transmision)
        // La 36×17 da unos −0,4 %, que «La marcha no crea bajadas» deja en 0 %.
        assertArrayEquals(FecCodec.pendiente(0.0), neo.enviados.last())
        assertTrue(eventos.isEmpty())
        assertTrue(vibrador.vibraciones.isEmpty())
        // Con la asignación de la Shimano, la palanca D2 endurece y la secuencia cambia de plato sola.
        repeat(4) { pulsar(SquareButton.PalancaDerecha2) }
        assertEquals(s(52, 15), c.estado.value.marcha)
        assertArrayEquals(pendienteShimano(s(52, 15)), neo.enviados.last())

        configurar { it.copy(transmision = Transmision.Lineal.ID) }
        assertEquals(lineal(17), c.estado.value.marcha)
        assertArrayEquals(FecCodec.pendiente(5.0), neo.enviados.last())
        assertEquals(s(52, 15), almacen.guardado.ultimasMarchas[shimano.id])

        configurar { it.copy(transmision = shimano.id) }
        assertEquals(s(52, 15), c.estado.value.marcha)
        assertEquals(17, almacen.guardado.ultimaMarcha)
    }

    @Test
    fun cambiar_de_transmision_sin_neo() = runTest {
        val c = conexiones()
        configurar { it.copy(transmision = shimano.id) }
        assertEquals(s(36, 17), c.estado.value.marcha)
        assertTrue(neo.enviados.isEmpty())
        conectarLosDos(c)
        assertArrayEquals(FecCodec.pendiente(0.0), neo.enviados.last())
    }

    @Test
    fun la_shimano_empieza_en_su_ultima_marcha() = runTest {
        ajustes.configuracion.value = Configuracion(transmision = shimano.id)
        almacen.guardado = EstadoGuardado(ultimaMarcha = 9, ultimasMarchas = mapOf(shimano.id to s(52, 19)))
        val c = conexiones()
        assertEquals(s(52, 19), c.estado.value.marcha)
    }

    @Test
    fun palancas_y_plato_con_la_shimano() = runTest {
        val c = conShimano()
        val eventos = eventosDe(c)
        pulsar(SquareButton.PalancaIzquierda1)
        // Automático: plato grande compensando dos piñones.
        assertEquals(s(52, 21), c.estado.value.marcha)
        val plato = eventos.single() as EventoRodaje.Cambio
        assertTrue(plato.cambio.cambiaPlato)
        pulsar(SquareButton.PalancaDerecha1)
        assertEquals(s(52, 24), c.estado.value.marcha)
        pulsar(SquareButton.PalancaIzquierda1)
        assertEquals(EventoRodaje.Tope(TopeMarcha.Plato(52)), eventos.last())
        assertEquals(listOf("corta", "corta", "doble"), vibrador.vibraciones)
    }

    @Test
    fun cambiar_de_modo_en_caliente() = runTest {
        val c = conShimano()
        configurar { it.conModo(ModoCambio.MANUAL) }
        assertEquals(s(36, 17), c.estado.value.marcha)
        assertEquals(ModoCambio.MANUAL, c.estado.value.modoCambio)
        pulsar(SquareButton.PalancaIzquierda1)
        assertEquals(s(52, 17), c.estado.value.marcha)
    }

    @Test
    fun una_asignacion_por_transmision() = runTest {
        // Spec `button-mapping`, «Reasignar sin afectar a la otra».
        val c = conShimano()
        configurar { it.conAsignacion(it.asignacion.asignar(Entrada(SquareButton.Triangulo), TipoGesto.Corta, Accion.PlatoMas)) }
        pulsar(SquareButton.Triangulo)
        esperar(400)
        assertEquals(s(52, 21), c.estado.value.marcha)
        configurar { it.copy(transmision = Transmision.Lineal.ID) }
        pulsar(SquareButton.Triangulo)
        esperar(400)
        assertEquals(lineal(12), c.estado.value.marcha)
        assertEquals(Asignacion.POR_DEFECTO, ajustes.configuracion.value.asignacion)
    }

    @Test
    fun referencia_de_la_shimano() = runTest {
        // Spec `virtual-gears`, «Referencia de la Shimano».
        val c = conShimano()
        repeat(4) { pulsar(SquareButton.PalancaDerecha2) }
        assertEquals(s(52, 15), c.estado.value.marcha)
        mantener(SquareButton.Cuadrado)
        esperar(800)
        soltar(SquareButton.Cuadrado)
        assertEquals(s(52, 15), c.estado.value.referencia)
        assertEquals(s(52, 15), ajustes.configuracion.value.referencia)
        configurar { it.copy(transmision = Transmision.Lineal.ID) }
        assertEquals(lineal(12), c.estado.value.referencia)
    }

    @Test
    fun la_palanca_de_la_shimano_no_espera_a_ningun_temporizador() = runTest {
        // NF1 con la Shimano: la palanca D2 entrega el comando en el paso de su notificación.
        val c = conShimano()
        ajustes.configuracion.value = ajustes.configuracion.value.copy(bajadas = ModoBajadas.SIN_PROTECCION)
        runCurrent()
        mantener(SquareButton.PalancaDerecha2)
        assertEquals(s(36, 15), c.estado.value.marcha)
        assertArrayEquals(pendienteShimano(s(36, 15)), neo.enviados.last())
        assertEquals(0, currentTime)
    }

    // --- ERG manual (change erg-manual) ---------------------------------------------------------------------------

    /** Mantiene △ hasta la pulsación larga y lo suelta: «Alternar SIM ⇄ ERG» por defecto. */
    private fun TestScope.alternar() {
        mantener(SquareButton.Triangulo)
        esperar(800)
        soltar(SquareButton.Triangulo)
    }

    private fun TestScope.conErg(marcha: Int = 14, vatios: Int? = 200): Sesion {
        almacen.guardado = EstadoGuardado(ultimaMarcha = marcha, ultimaPotenciaErgW = vatios)
        return conexiones().also { conectarLosDos(it) }
    }

    @Test
    fun entrar_y_salir_de_erg_manual() = runTest {
        val c = conErg(14)
        val eventos = eventosDe(c)
        vibrador.vibraciones.clear()
        alternar()
        assertArrayEquals(FecCodec.potencia(200), ultimo())
        assertEquals(ModoControl.ErgManual(200, 0, ModoControl.SimLibre), c.estado.value.simulacion.modo)
        assertEquals(EventoRodaje.ModoErg(activo = true, potenciaW = 200), eventos.last())
        assertEquals(listOf("larga"), vibrador.vibraciones)
        alternar()
        assertArrayEquals(FecCodec.pendiente(2.0), ultimo())
        assertEquals(ModoControl.SimLibre, c.estado.value.simulacion.modo)
        assertEquals(EventoRodaje.ModoErg(activo = false, potenciaW = 200), eventos.last())
        assertEquals(14, c.estado.value.marcha.posicion)
    }

    @Test
    fun primera_vez_a_150_w() = runTest {
        conErg(14, vatios = null)
        alternar()
        assertArrayEquals(FecCodec.potencia(150), ultimo())
    }

    @Test
    fun palancas_en_erg_manual() = runTest {
        val c = conErg(14)
        val eventos = eventosDe(c)
        alternar()
        pulsar(SquareButton.PalancaDerecha1)
        assertArrayEquals(FecCodec.potencia(210), ultimo())
        pulsar(SquareButton.PalancaIzquierda1, SquareButton.CampagnoloIzquierdo)
        assertArrayEquals(FecCodec.potencia(180), ultimo())
        pulsar(SquareButton.PalancaDerecha1)
        pulsar(SquareButton.PalancaDerecha1)
        pulsar(SquareButton.PalancaDerecha1)
        pulsar(SquareButton.PalancaDerecha1)
        pulsar(SquareButton.PalancaDerecha1)
        pulsar(SquareButton.PalancaDerecha1)
        assertArrayEquals(FecCodec.potencia(240), ultimo())
        assertEquals(14, c.estado.value.marcha.posicion)
        // Spec «Volver a la potencia de entrada».
        pulsar(SquareButton.Cuadrado)
        assertArrayEquals(FecCodec.potencia(200), ultimo())
        assertEquals(EventoRodaje.Ajuste(Accion.MarchaReferencia, ResultadoAjuste.Quitado, entradaW = 200), eventos.last())
        assertEquals(14, c.estado.value.marcha.posicion)
    }

    @Test
    fun mas_y_menos_10_w_asignados() = runTest {
        val c = conErg(14)
        configurar { it.conAsignacion(it.asignacion.asignar(Entrada(SquareButton.A), TipoGesto.Pulsar, Accion.ErgMas)) }
        // En SIM no hacen nada.
        val enviados = neo.enviados.size
        pulsar(SquareButton.A)
        assertEquals(enviados, neo.enviados.size)
        assertEquals(14, c.estado.value.marcha.posicion)
        alternar()
        mantener(SquareButton.A)
        assertArrayEquals(FecCodec.potencia(210), ultimo())
        esperar(800)
        assertArrayEquals(FecCodec.potencia(220), ultimo())
        soltar(SquareButton.A)
    }

    @Test
    fun tope_en_erg_manual() = runTest {
        conErg(14, vatios = 60)
        alternar()
        pulsar(SquareButton.PalancaIzquierda1)
        assertArrayEquals(FecCodec.potencia(50), ultimo())
        vibrador.vibraciones.clear()
        mantener(SquareButton.PalancaIzquierda1)
        esperar(2_000)
        soltar(SquareButton.PalancaIzquierda1)
        assertEquals(listOf("doble"), vibrador.vibraciones)
        assertArrayEquals(FecCodec.potencia(50), ultimo())
    }

    @Test
    fun potencia_recordada_entre_sesiones() = runTest {
        val c = conErg(14)
        alternar()
        pulsar(SquareButton.PalancaDerecha1)
        pulsar(SquareButton.PalancaDerecha1)
        alternar()
        c.terminar()
        assertEquals(220, almacen.guardado.ultimaPotenciaErgW)
    }

    @Test
    fun la_potencia_se_guarda_al_ajustar() = runTest {
        conErg(14)
        alternar()
        pulsar(SquareButton.PalancaDerecha1)
        esperar(3_000)
        assertEquals(210, almacen.guardado.ultimaPotenciaErgW)
    }

    @Test
    fun alternar_en_un_entrenamiento_de_zwift() = runTest {
        val c = enZwift(14)
        val eventos = eventosDe(c)
        zwift(vatios230)
        vibrador.vibraciones.clear()
        alternar()
        assertArrayEquals(FecCodec.potencia(230), ultimo())
        assertEquals(EventoRodaje.ErgPlataformaActivo, eventos.last())
        assertEquals(listOf("doble"), vibrador.vibraciones)
        assertEquals(ModoControl.ErgPlataforma(230, 0), c.estado.value.simulacion.modo)
    }

    @Test
    fun erg_manual_sobre_una_ruta_y_entrenamiento_que_lo_termina() = runTest {
        almacen.guardado = EstadoGuardado(ultimaMarcha = 14, ultimaPotenciaErgW = 200)
        val c = conexiones().also { conectarLosDos(it) }
        wifi.direccion.value = "192.168.1.20"
        runCurrent()
        transporte.conectarZwift(0x2AD9)
        runCurrent()
        zwift(pendiente2)
        alternar()
        assertArrayEquals(FecCodec.potencia(200), ultimo())
        zwift(pendiente6)
        assertArrayEquals(FecCodec.potencia(200), ultimo())
        pulsar(SquareButton.PalancaDerecha1)
        // Empieza un entrenamiento: manda Zwift, sin el ajuste del ERG manual, y la potencia queda recordada.
        zwift(vatios230)
        assertArrayEquals(FecCodec.potencia(230), ultimo())
        assertEquals(ModoControl.ErgPlataforma(230, 0), c.estado.value.simulacion.modo)
        esperar(3_000)
        assertEquals(210, almacen.guardado.ultimaPotenciaErgW)
        // Fin del entrenamiento: SIM con la marcha.
        zwift(pendiente2)
        assertArrayEquals(FecCodec.pendiente(4.0), ultimo())
    }

    @Test
    fun vuelta_a_la_ruta_con_el_ultimo_terreno() = runTest {
        enZwift(14)
        zwift(pendiente2)
        alternar()
        zwift(pendiente6)
        alternar()
        // 6 % de Zwift + 2 % de la marcha 14.
        assertArrayEquals(FecCodec.pendiente(8.0), ultimo())
    }

    @Test
    fun square_perdido_en_erg_manual() = runTest {
        val c = conErg(14)
        alternar()
        square.conecta = false
        square.estado.value = EstadoEnlace.Perdida
        runCurrent()
        esperar(5_000)
        assertArrayEquals(FecCodec.potencia(200), ultimo())
        assertEquals(ModoControl.ErgManual(200, 0, ModoControl.SimLibre), c.estado.value.simulacion.modo)
    }

    @Test
    fun apagar_el_square_en_erg_manual_deshace_el_ajuste() = runTest {
        val c = conErg(14)
        alternar()
        mantener(SquareButton.CampagnoloIzquierdo)
        mantener(SquareButton.PalancaIzquierda1)
        assertArrayEquals(FecCodec.potencia(170), ultimo())
        mantener(SquareButton.CampagnoloDerecho, SquareButton.PalancaDerecha1)
        assertArrayEquals(FecCodec.potencia(200), ultimo())
        assertEquals(ModoControl.ErgManual(200, 0, ModoControl.SimLibre), c.estado.value.simulacion.modo)
    }

    // --- Rodaje completo y ajustes (change rodaje-completo-y-ajustes) ----------------------------------------------

    @Test
    fun mas_y_menos_de_la_pantalla_en_lineal() = runTest {
        // Spec `ride-screen`, «Subir con el botón».
        val c = conMarcha(14)
        val eventos = eventosDe(c)
        vibrador.vibraciones.clear()
        c.accion(Accion.MarchaMas(1))
        runCurrent()
        assertEquals(15, c.estado.value.marcha.posicion)
        assertArrayEquals(FecCodec.pendiente(3.0), ultimo())
        assertEquals(Accion.MarchaMas(1), (eventos.single() as EventoRodaje.Cambio).accion)
        assertEquals(listOf("corta"), vibrador.vibraciones)
        // No es un gesto del Square: no cambia el último gesto ni mide latencia.
        assertNull(c.estado.value.ultimoGesto)
        assertNull(c.estado.value.latenciaMs)
    }

    @Test
    fun menos_de_la_pantalla_en_el_tope() = runTest {
        // Spec `ride-screen`, «Tope con el botón».
        val c = conMarcha(1)
        val eventos = eventosDe(c)
        c.accion(Accion.MarchaMenos(1))
        runCurrent()
        assertEquals(1, c.estado.value.marcha.posicion)
        assertEquals(listOf(EventoRodaje.Tope(TopeMarcha.MasLigera)), eventos)
    }

    @Test
    fun mas_de_la_pantalla_con_la_shimano_en_automatico() = runTest {
        // Spec `ride-screen`, «Con la Shimano»: como la palanca D2.
        val c = conShimano()
        assertEquals(s(36, 17), c.estado.value.marcha)
        c.accion(Accion.MarchaMas(1))
        runCurrent()
        assertEquals(s(36, 15), c.estado.value.marcha)
        c.accion(Accion.MarchaMas(1))
        runCurrent()
        assertEquals(s(52, 19), c.estado.value.marcha)
    }

    @Test
    fun mas_de_la_pantalla_en_erg_manual() = runTest {
        // Spec `ride-screen`, «En ERG».
        val c = conErg(14)
        val eventos = eventosDe(c)
        alternar()
        c.accion(Accion.MarchaMas(1))
        runCurrent()
        assertArrayEquals(FecCodec.potencia(210), ultimo())
        assertEquals(14, c.estado.value.marcha.posicion)
        assertTrue(eventos.last() is EventoRodaje.Ajuste)
    }

    @Test
    fun mas_de_la_pantalla_sin_neo() = runTest {
        // Spec `ride-screen`, «Sin Neo».
        almacen.guardado = EstadoGuardado(ultimaMarcha = 14)
        val c = conexiones()
        c.accion(Accion.MarchaMas(1))
        runCurrent()
        assertEquals(15, c.estado.value.marcha.posicion)
        assertTrue(neo.enviados.isEmpty())
        conectarLosDos(c)
        assertArrayEquals(FecCodec.pendiente(3.0), ultimo())
    }

    @Test
    fun la_pantalla_no_hace_nada_durante_la_captura() = runTest {
        val c = conMarcha(14)
        c.empezarCaptura()
        runCurrent()
        c.accion(Accion.MarchaMas(1))
        runCurrent()
        assertEquals(14, c.estado.value.marcha.posicion)
    }

    @Test
    fun campagnolo_izquierdo_y_cruceta_cambian_de_pantalla() = runTest {
        // Spec `app-navigation`, «Desde Rodaje»: solo un evento, sin tocar la marcha ni el Neo.
        val c = conMarcha(14)
        val eventos = eventosDe(c)
        val enviados = neo.enviados.size
        vibrador.vibraciones.clear()
        pulsar(SquareButton.CrucetaDerecha, SquareButton.CampagnoloIzquierdo)
        pulsar(SquareButton.CrucetaIzquierda, SquareButton.CampagnoloIzquierdo)
        assertEquals(listOf(EventoRodaje.Pantalla(siguiente = true), EventoRodaje.Pantalla(siguiente = false)), eventos)
        assertEquals(14, c.estado.value.marcha.posicion)
        assertEquals(enviados, neo.enviados.size)
        assertEquals(listOf("corta", "corta"), vibrador.vibraciones)
        assertEquals(Accion.PantallaAnterior, c.estado.value.ultimoGesto?.accion)
    }

    @Test
    fun capturando_la_cruceta_no_cambia_de_pantalla() = runTest {
        // Spec `app-navigation`, «Capturando una pulsación».
        val c = conMarcha(14)
        val eventos = eventosDe(c)
        c.empezarCaptura()
        runCurrent()
        pulsar(SquareButton.CrucetaDerecha, SquareButton.CampagnoloIzquierdo)
        assertTrue(eventos.isEmpty())
        assertEquals(EstadoCaptura.Capturada(Entrada(SquareButton.CrucetaDerecha, SquareButton.CampagnoloIzquierdo)), c.captura.value)
    }

    @Test
    fun datos_de_la_sesion_con_el_neo_perdido_y_recuperado() = runTest {
        // Spec `ride-screen`, «Datos de la sesión», «Neo desconectado».
        val c = conMarcha(12)
        fun potencia(w: Int) {
            neo.notificaciones.tryEmit(Notificacion(NeoClient.ETIQUETA_POTENCIA, byteArrayOf(0, 0, w.toByte(), (w shr 8).toByte()), ahora))
            runCurrent()
        }
        // Biela para que haya cadencia: 1 vuelta por segundo.
        var revs = 0
        fun segundo(w: Int) {
            revs++
            neo.notificaciones.tryEmit(Notificacion(NeoClient.ETIQUETA_CSC, byteArrayOf(0x02, revs.toByte(), (revs shr 8).toByte(), 0, (revs * 4).toByte()), ahora))
            potencia(w)
            esperar(1_000)
        }
        repeat(30) { segundo(200) }
        val d = c.estado.value.datos
        assertEquals(200, d.potencia3sW)
        assertTrue(d.tiempoMs in 28_000..30_000, "${d.tiempoMs}")
        assertEquals(200, d.maximaW)
        neo.conecta = false
        neo.estado.value = EstadoEnlace.Perdida
        runCurrent()
        esperar(60_000)
        assertEquals(0, c.estado.value.datos.potencia3sW)
        assertEquals(d.tiempoMs, c.estado.value.datos.tiempoMs)
    }

    @Test
    fun sin_zwift_el_terreno_es_llano_aunque_la_configuracion_tuviera_otra_pendiente() = runTest {
        // Spec `trainer-resistance`, «Pendiente sin Zwift de 3 %»: el valor de una configuración anterior se ignora.
        ajustes.configuracion.value = Configuracion.deJson("""{"version":9,"pendienteBasePct":3.0}""")
        val c = conMarcha(14)
        assertArrayEquals(FecCodec.pendiente(2.0), ultimo())
        assertEquals(0.0, c.estado.value.simulacion.pendienteBasePct)
    }

    @Test
    fun vibracion_desactivada() = runTest {
        // Spec `button-gestures`, «Vibración desactivable»: ni gesto, ni cambio, ni topes.
        ajustes.configuracion.value = Configuracion(vibracion = false)
        val c = conErg(24, vatios = 60)
        pulsar(SquareButton.PalancaDerecha1)
        mantener(SquareButton.Cuadrado)
        esperar(800)
        soltar(SquareButton.Cuadrado)
        alternar()
        pulsar(SquareButton.PalancaIzquierda1)
        pulsar(SquareButton.PalancaIzquierda1)
        c.accion(Accion.MarchaMas(1))
        runCurrent()
        assertTrue(vibrador.vibraciones.isEmpty(), "${vibrador.vibraciones}")
        // Al volver a activarla, vibra otra vez.
        configurar { it.copy(vibracion = true) }
        alternar()
        pulsar(SquareButton.PalancaIzquierda1)
        assertEquals("corta", vibrador.vibraciones.last())
    }

    @Test
    fun el_nivel_de_la_bateria_del_square_se_guarda_y_se_mantiene_al_reconectar() = runTest {
        // Spec `square-battery`, «Primer nivel» y «Reconexión».
        val c = conConfigurados(14)
        assertNull(c.estado.value.bateriaSquare)
        square.nivelBateria.tryEmit(80)
        runCurrent()
        assertEquals(80, c.estado.value.bateriaSquare)
        square.estado.value = EstadoEnlace.Perdida
        runCurrent()
        assertEquals(80, c.estado.value.bateriaSquare)
        square.nivelBateria.tryEmit(60)
        runCurrent()
        assertEquals(60, c.estado.value.bateriaSquare)
    }

    // --- Una búsqueda por tipo y «Reconectar» (spec `device-connection`, change `pestana-conexion`) -------------------------

    @Test
    fun buscar_el_rodillo_no_toca_el_mando() = runTest {
        // «Buscar el mando con el rodillo conectado», al revés: buscar el rodillo busca solo el Neo.
        val c = conexiones()
        c.buscarRodillo()
        runCurrent()
        assertEquals(listOf(setOf("Tacx Neo")), buscador.pedidos)
        assertEquals(EstadoDispositivo.Buscando, c.estado.value.neo)
        assertEquals(EstadoDispositivo.SinConexion, c.estado.value.square)
        buscador.resultados.trySend(ResultadoBusqueda.Encontrado("Tacx Neo", "AA:AA", "Tacx Neo 12345"))
        runCurrent()
        assertEquals(Conectado, c.estado.value.neo)
        assertEquals("AA:AA", c.estado.value.neoConfigurado?.direccion)
    }

    @Test
    fun buscar_el_mando_con_el_rodillo_conectado() = runTest {
        val c = conexiones()
        c.buscarRodillo()
        runCurrent()
        buscador.resultados.trySend(ResultadoBusqueda.Encontrado("Tacx Neo", "AA:AA", "Tacx Neo 12345"))
        runCurrent()
        c.buscarMando()
        runCurrent()
        assertEquals(setOf("SQUARE"), buscador.pedidos.last())
        assertEquals(Conectado, c.estado.value.neo)
        buscador.resultados.trySend(ResultadoBusqueda.Encontrado("SQUARE", "BB:BB", "SQUARE 1234"))
        runCurrent()
        assertEquals(Conectado, c.estado.value.square)
        assertEquals(listOf("AA:AA"), neo.conexiones)
    }

    @Test
    fun reconectar_intenta_con_el_configurado_por_su_direccion_sin_buscar() = runTest {
        // «Reconectar un dispositivo configurado»: sin búsqueda por nombre, con el configurado.
        almacen.guardado = EstadoGuardado(ultimaMarcha = 12)
        configurarNeoYSquare()
        square.conecta = false
        val c = conexiones()
        assertTrue(c.estado.value.square != Conectado)
        square.conecta = true
        c.reconectar(Sesion.Dispositivo.Square)
        runCurrent()
        assertEquals(Conectado, c.estado.value.square)
        assertEquals("BB:BB", square.conexiones.last())
        assertTrue(buscador.pedidos.isEmpty())
    }

    @Test
    fun cancelar_un_intento_espera_a_una_desconexion_lenta_antes_del_intento_nuevo() = runTest {
        almacen.guardado = EstadoGuardado(ultimaMarcha = 12)
        configurarNeoYSquare()
        square.conecta = false
        val c = conexiones()
        assertTrue(square.esperando)
        val antes = square.conexiones.size
        square.tardaDesconexionMs = 3_000
        square.conecta = true
        c.reconectar(Sesion.Dispositivo.Square)
        advanceTimeBy(2_999)
        runCurrent()
        assertEquals(antes, square.conexiones.size, "el intento nuevo no puede entrar antes de que acabe la desconexión")
        advanceTimeBy(2)
        runCurrent()
        assertEquals(antes + 1, square.conexiones.size)
        assertEquals(0, square.cancelaciones)
        assertEquals(Conectado, c.estado.value.square)
    }

    @Test
    fun una_desconexion_que_no_termina_cancela_la_conexion_pendiente_a_los_10_s_y_el_intento_va_despues() = runTest {
        almacen.guardado = EstadoGuardado(ultimaMarcha = 12)
        configurarNeoYSquare()
        square.conecta = false
        val c = conexiones()
        val antes = square.conexiones.size
        square.desconexionColgada = true
        square.conecta = true
        c.reconectar(Sesion.Dispositivo.Square)
        advanceTimeBy(Sesion.LIMITE_CANCELAR_MS - 1)
        runCurrent()
        assertEquals(0, square.cancelaciones)
        assertEquals(antes, square.conexiones.size)
        advanceTimeBy(2)
        runCurrent()
        assertEquals(1, square.cancelaciones)
        assertEquals(antes + 1, square.conexiones.size)
        assertEquals(Conectado, c.estado.value.square)
    }

    @Test
    fun una_direccion_invalida_deja_ese_dispositivo_sin_conexion_y_el_otro_conecta() = runTest {
        almacen.guardado = EstadoGuardado(ultimaMarcha = 12)
        configurarNeoYSquare()
        square.invalidas += "BB:BB"
        val c = conexiones()
        runCurrent()
        assertEquals(EstadoDispositivo.SinConexion, c.estado.value.square)
        assertEquals(Conectado, c.estado.value.neo)
    }

    @Test
    fun reconectar_sin_aparecer_sigue_intentandolo_sin_no_encontrado() = runTest {
        almacen.guardado = EstadoGuardado(ultimaMarcha = 12)
        configurarNeoYSquare()
        square.conecta = false
        val c = conexiones()
        c.reconectar(Sesion.Dispositivo.Square)
        runCurrent()
        assertTrue(c.estado.value.square != EstadoDispositivo.NoEncontrado)
        assertTrue(square.esperando, "sigue esperando a que se anuncie")
    }

    @Test
    fun olvidar_el_square_borra_su_nivel_de_bateria() = runTest {
        // Spec `square-battery`, «Olvidar el Square».
        val c = conConfigurados(14)
        square.nivelBateria.tryEmit(50)
        runCurrent()
        c.olvidar(Sesion.Dispositivo.Square)
        runCurrent()
        assertNull(c.estado.value.bateriaSquare)
    }

    @Test
    fun la_bateria_baja_no_cambia_los_botones_ni_las_conexiones() = runTest {
        // Spec `square-battery`, «Se sigue rodando».
        val c = conMarcha(14)
        square.nivelBateria.tryEmit(10)
        runCurrent()
        assertEquals(10, c.estado.value.bateriaSquare)
        assertEquals(Conectado, c.estado.value.square)
        assertEquals(Conectado, c.estado.value.neo)
        pulsar(SquareButton.PalancaDerecha1)
        assertEquals(15, c.estado.value.marcha.posicion)
        assertEquals(Conectado, c.estado.value.square)
    }

    @Test
    fun olvidar_el_square() = runTest {
        // Spec `device-connection`, «Olvidar el Square».
        val c = conConfigurados(14)
        assertEquals(squareConfigurado, c.estado.value.squareConfigurado)
        c.olvidar(Sesion.Dispositivo.Square)
        runCurrent()
        assertEquals(SinConexion, c.estado.value.square)
        assertNull(c.estado.value.squareConfigurado)
        assertNull(almacen.guardado.mando)
        assertEquals(neoConfigurado, almacen.guardado.rodillo)
        assertEquals(14, c.estado.value.marcha.posicion)
        // Desconectado de verdad, y sin reintentos.
        esperar(60_000)
        assertEquals(SinConexion, c.estado.value.square)
        assertEquals(listOf("directa BB:BB"), square.modos)
    }

    @Test
    fun olvidado_en_la_siguiente_sesion() = runTest {
        // Spec `device-connection`, «Olvidado en la siguiente sesión».
        val c = conConfigurados(14)
        c.olvidar(Sesion.Dispositivo.Neo)
        runCurrent()
        c.terminar()
        neo.modos.clear()
        val otra = conexiones()
        assertEquals(SinConexion, otra.estado.value.neo)
        assertTrue(neo.modos.isEmpty())
        assertEquals(Conectado, otra.estado.value.square)
    }

    @Test
    fun volver_a_buscar_tras_olvidar() = runTest {
        // Spec `device-connection`, «Volver a buscar».
        val c = conConfigurados(14)
        c.olvidar(Sesion.Dispositivo.Square)
        runCurrent()
        c.buscar()
        runCurrent()
        assertEquals(setOf("SQUARE"), buscador.pedidos.single())
        buscador.resultados.trySend(ResultadoBusqueda.Encontrado("SQUARE", "BB:BB", "SQUARE 1234"))
        runCurrent()
        assertEquals(Conectado, c.estado.value.square)
        assertEquals(DispositivoConfigurado("BB:BB", "SQUARE 1234"), almacen.guardado.mando)
        assertEquals(DispositivoConfigurado("BB:BB", "SQUARE 1234"), c.estado.value.squareConfigurado)
    }

    @Test
    fun olvidar_el_neo_rodando() = runTest {
        // Spec `device-connection`, «Olvidar el Neo rodando».
        val c = conConfigurados(14)
        c.olvidar(Sesion.Dispositivo.Neo)
        runCurrent()
        assertEquals(SinConexion, c.estado.value.neo)
        assertNull(c.estado.value.potencia)
        val enviados = neo.enviados.size
        pulsar(SquareButton.PalancaDerecha1)
        assertEquals(15, c.estado.value.marcha.posicion)
        assertEquals(enviados, neo.enviados.size)
    }

    @Test
    fun olvidar_mientras_conecta() = runTest {
        // Spec `device-connection`, «Olvidar mientras conecta».
        neo.conecta = false
        val c = conConfigurados(14)
        assertTrue(neo.esperando)
        assertEquals(Conectando, c.estado.value.neo)
        c.olvidar(Sesion.Dispositivo.Neo)
        runCurrent()
        assertFalse(neo.esperando)
        assertEquals(SinConexion, c.estado.value.neo)
    }

    // --- Los dos modos de conexión con Zwift y el cambio entre ellos (change mando-zwift-dircon, decisión 6) -------------

    private val fc82 = DirconCodec.uuid16(0xFC82)

    @Test
    fun abrir_en_el_modo_de_siempre_publica_el_rodillo_con_ftms_y_el_mando_por_bluetooth() = runTest {
        almacen.guardado = EstadoGuardado(ultimaMarcha = 12)
        configurarNeoYSquare()
        conexiones()
        wifi.direccion.value = "192.168.1.20"
        runCurrent()
        assertTrue(fc82 !in checkNotNull(transporte.servidor).descubrirServicios())
        assertEquals(1, transporteMando.publicaciones)
        val id = checkNotNull(guardada(ModoConexion.FTMS_WIFI))
        assertEquals(id, transporte.identidad)
        assertEquals(null, guardada(ModoConexion.ZWIFT_RIDE))
    }

    @Test
    fun abrir_en_todo_por_wifi_ofrece_el_mando_dentro_del_rodillo_sin_bluetooth_y_con_su_identidad() = runTest {
        configurarNeoYSquare()
        ajustes.configuracion.value = config(modo = ModoConexion.ZWIFT_RIDE)
        val c = conexiones()
        wifi.direccion.value = "192.168.1.20"
        runCurrent()
        assertTrue(fc82 in checkNotNull(transporte.servidor).descubrirServicios())
        assertEquals(0, transporteMando.publicaciones)
        assertEquals(null, transporteMando.servidor)
        val id = checkNotNull(guardada(ModoConexion.ZWIFT_RIDE))
        assertEquals(id, transporte.identidad)
        assertEquals(null, guardada(ModoConexion.FTMS_WIFI))
        assertEquals(EstadoMando.Esperando, c.estado.value.mando)
    }

    @Test
    fun cambiar_de_modo_rodando_no_toca_la_marcha_ni_lo_que_simula_el_neo_y_cada_modo_usa_su_identidad() = runTest {
        val c = enZwift(14)
        zwift(pendiente5)
        assertArrayEquals(FecCodec.pendiente(7.0), ultimo())
        val enviados = neo.enviados.size
        val identidadDeSiempre = checkNotNull(transporte.identidad)
        assertEquals(1, transporteMando.publicaciones)

        configurar { it.conPlataforma(modo = ModoConexion.ZWIFT_RIDE) }
        // El mando de Bluetooth se retira, el rodillo se anuncia con el servicio del mando y su propia identidad.
        assertEquals(1, transporteMando.paradas)
        assertTrue(fc82 in checkNotNull(transporte.servidor).descubrirServicios())
        val identidadRide = checkNotNull(transporte.identidad)
        assertTrue(identidadRide != identidadDeSiempre)
        assertEquals(identidadRide, guardada(ModoConexion.ZWIFT_RIDE))
        assertEquals(14, c.estado.value.marcha.posicion)
        assertEquals(enviados, neo.enviados.size)

        configurar { it.conPlataforma(modo = ModoConexion.FTMS_WIFI) }
        // Volver al modo de siempre: su identidad de antes, sin el servicio del mando y con el mando por Bluetooth otra vez.
        assertEquals(identidadDeSiempre, transporte.identidad)
        assertTrue(fc82 !in checkNotNull(transporte.servidor).descubrirServicios())
        assertEquals(2, transporteMando.publicaciones)
        assertEquals(14, c.estado.value.marcha.posicion)
        assertEquals(enviados, neo.enviados.size)
    }

    @Test
    fun la_palanca_llega_al_neo_sin_esperar_en_el_modo_de_siempre_con_zwift_mandando_terreno() = runTest {
        enZwift(12)
        zwift(pendiente4)
        val antes = currentTime
        square.notificaciones.tryEmit(botones())
        square.notificaciones.tryEmit(botones(SquareButton.PalancaDerecha1 to 1))
        runCurrent()
        // NF1: el comando ya está entregado sin que avance el reloj virtual, con Zwift mandando terreno.
        assertEquals(antes, currentTime)
        assertArrayEquals(FecCodec.pendiente(5.0), ultimo())
    }

    @Test
    fun la_palanca_llega_al_neo_sin_esperar_en_todo_por_wifi_con_set_llegando() = runTest {
        enZwiftRide(12)
        set(setPendiente4)
        val antes = currentTime
        square.notificaciones.tryEmit(botones())
        square.notificaciones.tryEmit(botones(SquareButton.PalancaDerecha1 to 1))
        set(setRelacion168)
        runCurrent()
        assertEquals(antes, currentTime)
        assertArrayEquals(FecCodec.pendiente(5.0), ultimo())
    }

    @Test
    fun cambiar_de_modo_suelta_las_funciones_de_zwift() = runTest {
        enZwift(12)
        mandoSaludado()
        mantener(SquareButton.CrucetaDerecha)
        assertEquals(setOf(FuncionZwift.DERECHA), pulsadasEnZwift)
        configurar { it.conPlataforma(modo = ModoConexion.ZWIFT_RIDE) }
        assertTrue(pulsadasEnZwift.isEmpty())
    }

    @Test
    fun todo_por_wifi_con_el_bluetooth_apagado_sigue_con_el_mando() = runTest {
        ajustes.configuracion.value = config(modo = ModoConexion.ZWIFT_RIDE)
        val c = enZwift(12)
        bluetooth.encendido.value = false
        runCurrent()
        assertEquals(EstadoMando.Esperando, c.estado.value.mando)
        assertTrue(fc82 in checkNotNull(transporte.servidor).descubrirServicios())
    }

    @Test
    fun todo_por_wifi_sin_wifi_deja_el_mando_sin_conexion() = runTest {
        ajustes.configuracion.value = config(modo = ModoConexion.ZWIFT_RIDE)
        val c = enZwift(12)
        wifi.direccion.value = null
        runCurrent()
        assertEquals(EstadoMando.SinConexion, c.estado.value.mando)
        assertEquals(EstadoPlataforma.SinWifi, c.estado.value.plataforma)
    }

    @Test
    fun zwift_ride_ofrece_siempre_el_mando_dentro_del_rodillo() = runTest {
        ajustes.configuracion.value = config(modo = ModoConexion.ZWIFT_RIDE)
        val c = enZwift(12)
        assertEquals(EstadoMando.Esperando, c.estado.value.mando)
        assertTrue(fc82 in checkNotNull(transporte.servidor).descubrirServicios())
        assertEquals(ModoConexion.ZWIFT_RIDE, ajustes.configuracion.value.conexion.modo)
    }

    @Test
    fun la_identidad_de_cada_modo_se_conserva_en_la_siguiente_sesion() = runTest {
        configurarNeoYSquare()
        ajustes.configuracion.value = config(modo = ModoConexion.ZWIFT_RIDE)
        conexiones()
        wifi.direccion.value = "192.168.1.20"
        runCurrent()
        val generada = checkNotNull(guardada(ModoConexion.ZWIFT_RIDE))
        val otra = TransporteFalso()
        Sesion(buscador, square, neo, pulsometro, ventilador, bluetooth, wifi, fabrica(otra), renombrador, registro, almacen, ajustes, vibrador, backgroundScope, { ahora }, { 0L })
        runCurrent()
        assertEquals(generada, otra.identidad)
    }

    // --- Simular un KICKR BIKE (change simular-kickr-bike) -----------------------------------------------------------------

    @Test
    fun por_defecto_se_anuncia_como_square_shift() = runTest {
        val c = enZwift(12)
        assertEquals("Smart Bike", transporte.nombre)
        assertEquals("Smart Bike", c.estado.value.nombreAnunciado)
    }

    @Test
    fun con_el_ajuste_se_anuncia_como_kickr_bike_con_la_identidad_propia_de_cada_modo() = runTest {
        ajustes.configuracion.value = config(identidad = Identidad.KICKR_BIKE_PRO)
        val c = enZwift(12)
        val id = checkNotNull(guardada(ModoConexion.FTMS_WIFI, Identidad.KICKR_BIKE_PRO))
        assertEquals(id, transporte.identidad)
        assertEquals(null, guardada(ModoConexion.FTMS_WIFI))
        assertTrue(checkNotNull(transporte.nombre).startsWith("KICKR BIKE PRO "), transporte.nombre)
        assertEquals(transporte.nombre, c.estado.value.nombreAnunciado)

        configurar { it.conPlataforma(modo = ModoConexion.ZWIFT_RIDE) }
        val idRide = checkNotNull(guardada(ModoConexion.ZWIFT_RIDE, Identidad.KICKR_BIKE_PRO))
        assertEquals(idRide, transporte.identidad)
        assertTrue(idRide != id)
        assertEquals(null, guardada(ModoConexion.ZWIFT_RIDE))
        assertTrue(checkNotNull(transporte.nombre).startsWith("KICKR BIKE PRO "), transporte.nombre)
        assertTrue(c.estado.value.nombreAnunciado != "Smart Bike")
    }

    @Test
    fun el_modo_por_bluetooth_se_anuncia_como_smart_bike_aunque_se_haya_elegido_kickr_y_con_su_identidad() = runTest {
        // Spec `ftms-bluetooth`, «Nombre y mando en el modo por Bluetooth»: el nombre elegido no cuenta con este modo.
        ajustes.configuracion.value = config(modo = ModoConexion.FTMS_BLUETOOTH, identidad = Identidad.KICKR_BIKE_PRO)
        val c = enZwift(12)
        assertEquals("Smart Bike", transporte.nombre)
        assertEquals("Smart Bike", c.estado.value.nombreAnunciado)
        assertEquals(checkNotNull(guardada(ModoConexion.FTMS_BLUETOOTH)), transporte.identidad)
        assertEquals(null, guardada(ModoConexion.FTMS_BLUETOOTH, Identidad.KICKR_BIKE_PRO))
        assertEquals(Enlace.BLUETOOTH, c.estado.value.enlaceConexion)
        assertEquals(EstadoMando.Esperando, c.estado.value.mando, "el mando va dentro del mismo dispositivo")

        configurar { it.conPlataforma(modo = ModoConexion.FTMS_WIFI) }
        assertTrue(checkNotNull(transporte.nombre).startsWith("KICKR BIKE PRO "), "al volver a wifi vuelve el nombre elegido")
        assertEquals(Enlace.WIFI, c.estado.value.enlaceConexion)
    }

    /** Zwift saluda al mando que va dentro del servidor del rodillo: se suscribe y manda el `RideOn`. */
    private fun TestScope.mandoSaludadoEnElRodillo() {
        val s = checkNotNull(transporte.servidor)
        transporte.estado.value = EstadoTransporte.Conectado("AA:BB")
        s.suscribir(MandoZwift.ASYNC, true)
        s.suscribir(MandoZwift.SYNC_TX, true)
        s.escribir(MandoZwift.SYNC_RX, MandoZwiftCodec.RIDE_ON)
        runCurrent()
    }

    private fun tramasMandoEnElRodillo() = transporte.notificaciones.filter { it.caracteristica == MandoZwift.ASYNC }.map { hex(it.valor) }

    @Test
    fun con_el_modo_por_bluetooth_el_mando_va_en_el_mismo_servidor_que_el_rodillo() = runTest {
        // Spec `ftms-bluetooth`, «Mando de Zwift dentro del mismo dispositivo».
        ajustes.configuracion.value = config(modo = ModoConexion.FTMS_BLUETOOTH)
        val c = conMarcha(12)
        val servicios = checkNotNull(transporte.servidor).descubrirServicios()
        assertTrue(MandoZwift.SERVICIO in servicios, "el servicio del mando va con el del rodillo")
        assertTrue(DirconCodec.uuid16(0x1826) in servicios)
        assertEquals(0, transporteMando.publicaciones, "sin mando aparte")
        assertEquals(EstadoMando.Esperando, c.estado.value.mando)
        mandoSaludadoEnElRodillo()
        assertEquals(EstadoMando.Conectado, c.estado.value.mando)
    }

    @Test
    fun con_el_modo_por_bluetooth_los_botones_del_square_llegan_a_zwift_por_el_rodillo() = runTest {
        ajustes.configuracion.value = config(modo = ModoConexion.FTMS_BLUETOOTH)
        conMarcha(12)
        mandoSaludadoEnElRodillo()
        mantener(SquareButton.CrucetaDerecha)
        assertEquals(setOf(FuncionZwift.DERECHA), pulsadasEnZwift)
        soltar(SquareButton.CrucetaDerecha)
        assertTrue(pulsadasEnZwift.isEmpty())
        assertEquals("23 08 fb ff ff ff 0f 1a 04 08 00 10 00 1a 04 08 01 10 00", tramasMandoEnElRodillo().first())
        assertEquals(reposoMando, tramasMandoEnElRodillo().last())
    }

    @Test
    fun con_el_modo_por_bluetooth_sin_bluetooth_el_rodillo_y_el_mando_se_retiran_y_vuelven() = runTest {
        ajustes.configuracion.value = config(modo = ModoConexion.FTMS_BLUETOOTH)
        // Con los dos ya configurados, lo publicado no cambia al conectarlos (spec `platform-bridge`).
        almacen.guardado = EstadoGuardado(ultimaMarcha = 12)
        configurarNeoYSquare()
        val c = conexiones().also { conectarLosDos(it) }
        assertEquals(1, transporte.publicaciones)
        bluetooth.encendido.value = false
        runCurrent()
        assertEquals(1, transporte.paradas)
        assertEquals(EstadoPlataforma.SinBluetooth, c.estado.value.plataforma)
        assertEquals(EstadoMando.SinConexion, c.estado.value.mando, "el mando también se queda sin conexión")
        bluetooth.encendido.value = true
        runCurrent()
        assertEquals(2, transporte.publicaciones)
        assertEquals(EstadoMando.Esperando, c.estado.value.mando)
    }

    private fun TestScope.soloBluetoothConNombre(activado: Boolean = true): Sesion {
        almacen.guardado = EstadoGuardado(ultimaMarcha = 12)
        configurarNeoYSquare()
        ajustes.configuracion.value = Configuracion(conexion = AjustesConexion(ModoConexion.FTMS_BLUETOOTH, nombrePorBluetooth = activado))
        return conexiones().also { runCurrent() }
    }

    private fun nombrePorBluetooth(activado: Boolean) = { c: Configuracion -> c.copy(conexion = c.conexion.copy(nombrePorBluetooth = activado)) }

    @Test
    fun con_el_nombre_por_bluetooth_el_movil_se_renombra_antes_de_anunciarse_y_lo_recupera_al_terminar() = runTest {
        val c = soloBluetoothConNombre()
        assertEquals("Smart Bike", adaptadorNombre.real)
        assertEquals(1, transporte.publicaciones)
        assertEquals(EstadoNombre.Renombrado("Smart Bike", "OnePlus 8"), c.nombreBluetooth.value)
        c.terminar()
        assertEquals("OnePlus 8", adaptadorNombre.real)
        assertEquals(null, almacen.guardado.nombreBluetoothOriginal)
    }

    @Test
    fun sin_el_ajuste_el_movil_conserva_su_nombre() = runTest {
        soloBluetoothConNombre(activado = false)
        assertEquals(emptyList<String>(), adaptadorNombre.llamadas)
        assertEquals(1, transporte.publicaciones)
    }

    @Test
    fun marcar_y_desmarcar_la_casilla_rodando_vuelve_a_anunciar_sin_tocar_la_marcha() = runTest {
        val c = soloBluetoothConNombre(activado = false)
        val enviados = neo.enviados.size
        configurar(nombrePorBluetooth(true))
        assertEquals("Smart Bike", adaptadorNombre.real)
        assertEquals(1, transporte.paradas, "se retira el anuncio con el nombre anterior")
        assertEquals(2, transporte.publicaciones, "y se anuncia con el nuevo")
        configurar(nombrePorBluetooth(false))
        assertEquals("OnePlus 8", adaptadorNombre.real)
        assertEquals(3, transporte.publicaciones)
        assertEquals(12, c.estado.value.marcha.posicion)
        assertEquals(enviados, neo.enviados.size)
    }

    @Test
    fun cambiar_a_solo_wifi_devuelve_el_nombre_y_cambiar_de_nombre_lo_pone_nuevo() = runTest {
        soloBluetoothConNombre()
        configurar { it.conPlataforma(identidad = Identidad.KICKR_BIKE_PRO) }
        assertTrue(checkNotNull(adaptadorNombre.real).startsWith("KICKR BIKE PRO "))
        configurar { it.conPlataforma(modo = ModoConexion.ZWIFT_RIDE) }
        assertEquals("OnePlus 8", adaptadorNombre.real)
    }

    @Test
    fun si_no_se_puede_renombrar_se_publica_igual_y_se_reintenta_al_encender_el_bluetooth() = runTest {
        adaptadorNombre.acepta = false
        val c = soloBluetoothConNombre()
        assertTrue(c.nombreBluetooth.value is EstadoNombre.NoSePudo)
        assertEquals(1, transporte.publicaciones, "la app se anuncia con el nombre del móvil")
        bluetooth.encendido.value = false
        runCurrent()
        adaptadorNombre.acepta = true
        bluetooth.encendido.value = true
        runCurrent()
        assertEquals("Smart Bike", adaptadorNombre.real)
        assertEquals(EstadoNombre.Renombrado("Smart Bike", "OnePlus 8"), c.nombreBluetooth.value)
    }

    @Test
    fun del_modo_de_siempre_al_de_bluetooth_el_mando_pasa_de_aparte_a_dentro_sin_tocar_la_marcha() = runTest {
        val c = enZwift(14)
        assertEquals(1, transporteMando.publicaciones)
        val enviados = neo.enviados.size
        configurar { it.conPlataforma(modo = ModoConexion.FTMS_BLUETOOTH) }
        assertEquals(1, transporteMando.paradas, "el mando aparte se retira")
        assertTrue(MandoZwift.SERVICIO in checkNotNull(transporte.servidor).descubrirServicios())
        assertEquals(14, c.estado.value.marcha.posicion)
        assertEquals(enviados, neo.enviados.size)
        configurar { it.conPlataforma(modo = ModoConexion.FTMS_WIFI) }
        assertEquals(2, transporteMando.publicaciones, "el mando vuelve a anunciarse aparte")
        assertTrue(MandoZwift.SERVICIO !in checkNotNull(transporte.servidor).descubrirServicios())
        assertEquals(14, c.estado.value.marcha.posicion)
    }

    @Test
    fun una_configuracion_con_los_ajustes_de_la_plataforma_se_anuncia_con_su_nombre_y_su_identidad() = runTest {
        // Spec `platform-settings`, «Ajustes de conexión guardados»: el modo y el nombre salen de la configuración.
        ajustes.configuracion.value = Configuracion(conexion = AjustesConexion(ModoConexion.ZWIFT_RIDE, Identidad.KICKR_BIKE_PRO))
        val c = enZwift(12)
        assertTrue(checkNotNull(transporte.nombre).startsWith("KICKR BIKE PRO "), transporte.nombre)
        assertEquals(transporte.nombre, c.estado.value.nombreAnunciado)
        assertEquals(checkNotNull(guardada(ModoConexion.ZWIFT_RIDE, Identidad.KICKR_BIKE_PRO)), transporte.identidad)
        // El mando va dentro del rodillo aunque el interruptor guardado estuviera desactivado.
        assertEquals(EstadoMando.Esperando, c.estado.value.mando)
    }

    @Test
    fun activar_y_desactivar_rodando_no_toca_la_marcha_ni_lo_que_simula_el_neo_y_recupera_la_identidad_de_siempre() = runTest {
        val c = enZwift(14)
        zwift(pendiente5)
        assertArrayEquals(FecCodec.pendiente(7.0), ultimo())
        val enviados = neo.enviados.size
        val deSiempre = checkNotNull(transporte.identidad)
        val publicaciones = transporte.publicaciones
        val paradas = transporte.paradas

        configurar { it.conPlataforma(identidad = Identidad.KICKR_BIKE_PRO) }
        assertEquals(publicaciones + 1, transporte.publicaciones)
        assertTrue(transporte.paradas > paradas)
        val kickr = checkNotNull(transporte.identidad)
        assertTrue(kickr != deSiempre)
        assertTrue(checkNotNull(transporte.nombre).startsWith("KICKR BIKE PRO "))
        assertEquals(14, c.estado.value.marcha.posicion)
        assertEquals(enviados, neo.enviados.size)

        configurar { it.conPlataforma(identidad = Identidad.SMART_BIKE) }
        assertEquals(deSiempre, transporte.identidad)
        assertEquals("Smart Bike", transporte.nombre)
        assertEquals(14, c.estado.value.marcha.posicion)
        assertEquals(enviados, neo.enviados.size)
    }

    @Test
    fun cambiar_modo_y_ajuste_a_la_vez_reconstruye_una_sola_vez() = runTest {
        enZwift(12)
        val publicaciones = transporte.publicaciones
        configurar { it.conPlataforma(modo = ModoConexion.ZWIFT_RIDE, identidad = Identidad.KICKR_BIKE_PRO) }
        assertEquals(publicaciones + 1, transporte.publicaciones)
        assertEquals(checkNotNull(guardada(ModoConexion.ZWIFT_RIDE, Identidad.KICKR_BIKE_PRO)), transporte.identidad)
    }

    @Test
    fun activar_el_ajuste_suelta_las_funciones_y_mantiene_lo_ultimo_de_zwift() = runTest {
        enZwift(12)
        mandoSaludado()
        mantener(SquareButton.CrucetaDerecha)
        assertEquals(setOf(FuncionZwift.DERECHA), pulsadasEnZwift)
        configurar { it.conPlataforma(identidad = Identidad.KICKR_BIKE_PRO) }
        assertTrue(pulsadasEnZwift.isEmpty())
    }

    @Test
    fun sin_wifi_el_ajuste_se_guarda_y_se_aplica_al_volver_el_wifi() = runTest {
        enZwift(12)
        wifi.direccion.value = null
        runCurrent()
        configurar { it.conPlataforma(identidad = Identidad.KICKR_BIKE_PRO) }
        assertEquals(true, ajustes.configuracion.value.simularKickrBike)
        wifi.direccion.value = "192.168.1.20"
        runCurrent()
        assertTrue(checkNotNull(transporte.nombre).startsWith("KICKR BIKE PRO "))
    }

    @Test
    fun la_palanca_llega_al_neo_sin_esperar_despues_de_cambiar_el_ajuste() = runTest {
        enZwift(12)
        zwift(pendiente4)
        configurar { it.conPlataforma(identidad = Identidad.KICKR_BIKE_PRO) }
        val antes = currentTime
        square.notificaciones.tryEmit(botones())
        square.notificaciones.tryEmit(botones(SquareButton.PalancaDerecha1 to 1))
        runCurrent()
        assertEquals(antes, currentTime)
    }

    // --- La `Sesion` solo conoce `ConexionPlataforma` (change mando-zwift-dircon, decisión 1) ---------------------------------

    /** Una conexión con Zwift de mentira: anota lo que la `Sesion` le pide y deja que el test hable como Zwift. */
    private class ConexionFalsa(override val modo: ModoConexion, override val simularKickr: Boolean = false) : ConexionPlataforma {
        lateinit var receptor: ReceptorPlataforma
        override val estado = MutableStateFlow<EstadoTransporte>(EstadoTransporte.Parado)
        val estadoMando = MutableStateFlow(EstadoMando.Desactivado)
        override var suscrito = true
        override var suscritoPulso = false
        val giro: Set<LadoGiro> = emptySet()
        val publicadas = mutableListOf<IdentidadDispositivo>()
        var retiradas = 0
        var terminada = false
        var mandoActivado: Boolean? = null
        val telemetrias = mutableListOf<TelemetriaPlataforma>()
        val pulsos = mutableListOf<Int>()
        val pulsadas = mutableSetOf<FuncionMando>()
        var soltarTodos = 0
        override val mando = object : MandoEmulado {
            override val estado: StateFlow<EstadoMando> = estadoMando
            override val giro: Set<LadoGiro> get() = this@ConexionFalsa.giro
            override fun activar(activado: Boolean) {
                mandoActivado = activado
            }
            override fun pulsar(funcion: FuncionMando) {
                pulsadas += funcion
            }
            override fun soltar(funcion: FuncionMando) {
                pulsadas -= funcion
            }
            override fun soltarTodo() {
                pulsadas.clear()
                soltarTodos++
            }
            override fun terminar() = Unit
        }
        override fun publicar(identidad: IdentidadDispositivo) {
            publicadas += identidad
        }
        override fun retirar() {
            retiradas++
        }
        override fun terminar() {
            terminada = true
        }
        override fun telemetria(t: TelemetriaPlataforma) {
            telemetrias += t
        }
        override fun pulso(bpm: Int) {
            pulsos += bpm
        }
    }

    private fun TestScope.conFalsa(modo: ModoConexion = ModoConexion.FTMS_WIFI): Pair<Sesion, ConexionFalsa> {
        virtual = { testScheduler.currentTime }
        almacen.guardado = EstadoGuardado(ultimaMarcha = 12)
        ajustes.configuracion.value = config(modo = modo)
        lateinit var falsa: ConexionFalsa
        val c = Sesion(
            buscador, square, neo, pulsometro, ventilador, bluetooth, wifi,
            { sv, k, r, _ ->
                ConexionFalsa(sv.modo, k).also {
                    it.receptor = r
                    falsa = it
                }
            },
            renombrador, registro, almacen, ajustes, vibrador, backgroundScope, { ahora }, { 45_871_000L },
        )
        runCurrent()
        conectarLosDos(c)
        return c to falsa
    }

    @Test
    fun la_sesion_no_sabe_el_modo_el_terreno_y_la_potencia_de_la_conexion_llegan_al_neo() = runTest {
        val (c, falsa) = conFalsa(ModoConexion.ZWIFT_RIDE)
        assertEquals(ModoConexion.ZWIFT_RIDE, falsa.modo)
        assertEquals(true, falsa.mandoActivado)
        falsa.receptor.terreno(Terreno(4.0))
        assertArrayEquals(FecCodec.pendiente(4.0), ultimo())
        falsa.receptor.potenciaErg(230)
        assertArrayEquals(FecCodec.potencia(230), ultimo())
        falsa.receptor.terreno(Terreno(2.0))
        ahora += 1_000
        falsa.receptor.terreno(Terreno(3.0))
        assertEquals(12, c.estado.value.marcha.posicion)
    }

    @Test
    fun la_sesion_publica_con_wifi_y_retira_sin_wifi() = runTest {
        val (_, falsa) = conFalsa()
        wifi.direccion.value = "192.168.1.20"
        runCurrent()
        // `publicar` es idempotente: la sesión puede repetirlo con cada cambio de red.
        assertTrue(falsa.publicadas.isNotEmpty())
        val antes = falsa.retiradas
        wifi.direccion.value = null
        runCurrent()
        assertEquals(antes + 1, falsa.retiradas)
    }

    @Test
    fun la_sesion_manda_telemetria_y_pulso_a_la_conexion() = runTest {
        val (_, falsa) = conFalsa()
        wifi.direccion.value = "192.168.1.20"
        runCurrent()
        falsa.estado.value = EstadoTransporte.Conectado("192.168.0.10")
        runCurrent()
        advanceTimeBy(1_000)
        assertTrue(falsa.telemetrias.isNotEmpty())
    }

    @Test
    fun las_funciones_de_zwift_y_el_estado_del_mando_pasan_por_la_conexion() = runTest {
        val (c, falsa) = conFalsa()
        falsa.estadoMando.value = EstadoMando.Conectado
        runCurrent()
        assertEquals(EstadoMando.Conectado, c.estado.value.mando)
        mantener(SquareButton.CrucetaDerecha)
        assertEquals(setOf(FuncionZwift.DERECHA), falsa.pulsadas)
        soltar(SquareButton.CrucetaDerecha)
        assertTrue(falsa.pulsadas.isEmpty())
        // Con el mando mantenido y el mando que deja de estar disponible, la sesión lo suelta todo.
        mantener(SquareButton.CrucetaDerecha)
        val antes = falsa.soltarTodos
        falsa.estadoMando.value = EstadoMando.Desactivado
        runCurrent()
        assertTrue(falsa.soltarTodos > antes)
        assertTrue(falsa.pulsadas.isEmpty())
        assertEquals(EstadoMando.Desactivado, c.estado.value.mando)
    }

    @Test
    fun el_mando_se_activa_al_crear_la_conexion_sin_interruptor() = runTest {
        // Spec `platform-settings`, «Mando sin interruptor».
        val (_, falsa) = conFalsa()
        assertEquals(true, falsa.mandoActivado)
    }

    // --- Marchas virtuales de Zwift por `SET` (change mando-zwift-dircon) ------------------------------------------

    /** La sesión en el modo «Todo por wifi» con el Neo y el Square conectados en [marcha] y Zwift conectado. */
    private fun TestScope.enZwiftRide(marcha: Int = 12): Sesion {
        ajustes.configuracion.value = config(modo = ModoConexion.ZWIFT_RIDE)
        return enZwift(marcha)
    }

    /** Zwift escribe un `SET` por SYNC_RX del mando, que va dentro del rodillo. */
    private fun TestScope.set(h: String) {
        val r = checkNotNull(transporte.servidor).escribir(MandoZwift.SYNC_RX, h.split(" ").map { it.toInt(16).toByte() }.toByteArray())
        assertTrue(r is ResultadoGatt.Ok)
        runCurrent()
    }

    private val setPendiente4 = "3f 08 00 10 00 22 03 28 a0 06"
    private val setPendiente4y5 = "3f 08 00 10 00 22 03 28 84 07"
    private val setPendiente5 = "3f 08 00 10 00 22 03 28 e8 07"
    private val setRelacion168 = "3f 08 00 10 00 22 04 38 a0 83 01"
    private val setAjustes = "3f 08 01 10 00 22 0c 40 ec 27 50 d3 06 58 9c 4a 60 90 03"

    @Test
    fun set_de_zwift_en_la_marcha_neutra_simula_la_pendiente() = runTest {
        enZwiftRide(12)
        set(setPendiente4)
        assertArrayEquals(FecCodec.pendiente(4.0), ultimo())
    }

    @Test
    fun set_de_zwift_con_cambio_de_marcha_al_instante() = runTest {
        val c = enZwiftRide(12)
        set(setPendiente4)
        pulsar(SquareButton.PalancaDerecha1)
        pulsar(SquareButton.PalancaDerecha1)
        assertArrayEquals(FecCodec.pendiente(6.0), ultimo())
        assertEquals(14, c.estado.value.marcha.posicion)
    }

    @Test
    fun set_sin_pendiente_no_envia_nada_al_neo() = runTest {
        val c = enZwiftRide(12)
        set(setPendiente4)
        val enviados = neo.enviados.size
        set(setRelacion168)
        set(setAjustes)
        assertEquals(enviados, neo.enviados.size)
        // La relación de la marcha de Zwift se guarda para Diagnóstico y no cambia lo que simula el Neo.
        val guardada = checkNotNull(c.estado.value.simulacion.ultimaSimulacionPlataforma).simulacion
        assertEquals(1.68, guardada.relacionVirtual!!, 1e-9)
        assertEquals(4.0, guardada.pendientePct!!, 1e-9)
        assertEquals(0.004, guardada.crr!!, 1e-9)
    }

    @Test
    fun set_con_ventana_de_250_ms_y_llega_el_ultimo() = runTest {
        enZwiftRide(12)
        set(setPendiente4)
        val enviados = neo.enviados.size
        ahora += 100
        set(setPendiente4y5)
        assertEquals(enviados, neo.enviados.size)
        advanceTimeBy(300)
        assertArrayEquals(FecCodec.pendiente(4.5), ultimo())
    }

    @Test
    fun en_todo_wifi_los_comandos_de_ftms_no_cambian_lo_que_simula_el_neo() = runTest {
        enZwiftRide(12)
        set(setPendiente4)
        val enviados = neo.enviados.size
        zwift(pendiente2)
        zwift(vatios230)
        assertEquals(enviados, neo.enviados.size)
        assertArrayEquals(FecCodec.pendiente(4.0), ultimo())
    }

    @Test
    fun set_con_zwift_cerrado_mantiene_30_s_y_vuelve_a_la_base() = runTest {
        enZwiftRide(14)
        set(setPendiente5)
        assertArrayEquals(FecCodec.pendiente(7.0), ultimo())
        transporte.estado.value = EstadoTransporte.Esperando
        runCurrent()
        advanceTimeBy(29_000)
        assertArrayEquals(FecCodec.pendiente(7.0), ultimo())
        advanceTimeBy(1_001)
        assertArrayEquals(FecCodec.pendiente(2.0), ultimo())
    }

    @Test
    fun set_con_potencia_de_erg_fija_la_potencia_y_las_palancas_la_ajustan() = runTest {
        val c = enZwiftRide(12)
        set(setPendiente4)
        set("3f 08 01 10 05 22 03 10 e6 01") // ergPower 230 W
        assertArrayEquals(FecCodec.potencia(230), ultimo())
        assertEquals(12, c.estado.value.marcha.posicion)
        pulsar(SquareButton.PalancaDerecha1)
        assertArrayEquals(FecCodec.potencia(240), ultimo())
        assertEquals(12, c.estado.value.marcha.posicion)
    }

    @Test
    fun set_con_pendiente_tras_el_erg_vuelve_a_simular() = runTest {
        enZwiftRide(12)
        set("3f 08 01 10 05 22 03 10 e6 01")
        assertArrayEquals(FecCodec.potencia(230), ultimo())
        ahora += 1_000
        set(setPendiente4)
        assertArrayEquals(FecCodec.pendiente(4.0), ultimo())
    }

    @Test
    fun set_sin_neo_se_guarda_y_se_aplica_al_conectar() = runTest {
        almacen.guardado = EstadoGuardado(ultimaMarcha = 12)
        configurarNeoYSquare()
        // El Neo configurado está dormido: se publica igual (spec `platform-bridge`, «Neo dormido»).
        neo.conecta = false
        ajustes.configuracion.value = config(modo = ModoConexion.ZWIFT_RIDE)
        val c = conexiones()
        wifi.direccion.value = "192.168.1.20"
        runCurrent()
        transporte.conectarZwift(0x2AD9)
        runCurrent()
        set(setPendiente4)
        assertTrue(neo.enviados.isEmpty())
        neo.conecta = true
        conectarLosDos(c)
        assertArrayEquals(FecCodec.pendiente(4.0), ultimo())
    }

    @Test
    fun en_todo_wifi_las_funciones_de_zwift_van_por_el_rodillo() = runTest {
        enZwiftRide(12)
        val s = checkNotNull(transporte.servidor)
        transporte.conectarZwift(0x2AD9)
        s.suscribir(MandoZwift.ASYNC, true)
        s.suscribir(MandoZwift.SYNC_TX, true)
        s.escribir(MandoZwift.SYNC_RX, MandoZwiftCodec.RIDE_ON)
        runCurrent()
        transporte.notificaciones.clear()
        mantener(SquareButton.CrucetaArriba)
        fun asyncs() = transporte.notificaciones.filter { it.caracteristica == MandoZwift.ASYNC }.map { hex(it.valor) }
        assertEquals(listOf("23 08 fd ff ff ff 0f 1a 04 08 00 10 00 1a 04 08 01 10 00"), asyncs())
        soltar(SquareButton.CrucetaArriba)
        assertEquals("23 08 ff ff ff ff 0f 1a 04 08 00 10 00 1a 04 08 01 10 00", asyncs().last())
        // El mando de Bluetooth ni se anuncia en este modo.
        assertNull(transporteMando.servidor)
    }

    // --- Mando de Zwift (change mando-zwift-emulado) -----------------------------------------------------------------

    /** Zwift se conecta al mando: usa el servidor, se suscribe y saluda. */
    private fun TestScope.mandoSaludado() {
        val s = checkNotNull(transporteMando.servidor)
        transporteMando.estado.value = EstadoTransporte.Conectado("PC")
        s.suscribir(MandoZwift.ASYNC, true)
        s.suscribir(MandoZwift.SYNC_TX, true)
        s.escribir(MandoZwift.SYNC_RX, MandoZwiftCodec.RIDE_ON)
        runCurrent()
    }

    private val pulsadasEnZwift get() = checkNotNull(mandoZwift).pulsadas

    /** Tramas de botones enviadas a Zwift por el mando. */
    private fun tramasMando() = transporteMando.notificaciones.filter { it.caracteristica == MandoZwift.ASYNC }.map { hex(it.valor) }

    private val reposoMando = "23 08 ff ff ff ff 0f 1a 04 08 00 10 00 1a 04 08 01 10 00"
    private val zEnMando = "23 08 ff fe ff ff 0f 1a 04 08 00 10 00 1a 04 08 01 10 00"

    // Ciclo de vida (decisión 6)

    @Test
    fun abrir_con_el_ajuste_activado_publica_el_mando() = runTest {
        val c = conMarcha(12)
        assertEquals(1, transporteMando.publicaciones)
        assertEquals(EstadoMando.Esperando, c.estado.value.mando)
        mandoSaludado()
        assertEquals(EstadoMando.Conectado, c.estado.value.mando)
    }

    @Test
    fun una_sola_publicacion_aunque_el_square_y_el_neo_reconecten() = runTest {
        conMarcha(12)
        square.estado.value = EstadoEnlace.Perdida
        runCurrent()
        square.estado.value = EstadoEnlace.Listo
        neo.estado.value = EstadoEnlace.Perdida
        runCurrent()
        neo.estado.value = EstadoEnlace.Listo
        runCurrent()
        assertEquals(1, transporteMando.publicaciones)
    }

    @Test
    fun desactivar_rodando_para_el_mando_sin_tocar_el_puente() = runTest {
        val c = conMarcha(12)
        wifi.direccion.value = "192.168.1.20"
        runCurrent()
        transporte.conectarZwift()
        runCurrent()
        mandoSaludado()
        mantener(SquareButton.FrenoIzquierdo)
        assertEquals(setOf(FuncionZwift.FRENO_IZQUIERDO), pulsadasEnZwift)
        // Spec `zwift-controller`, «Desactivar el ajuste rodando»: ahora, olvidar el Square.
        c.olvidar(Sesion.Dispositivo.Square)
        runCurrent()
        assertEquals(EstadoMando.Desactivado, c.estado.value.mando)
        assertEquals(EstadoTransporte.Parado, transporteMando.estado.value)
        // Antes de parar, el freno se suelta.
        assertEquals(reposoMando, tramasMando().last())
        assertTrue(pulsadasEnZwift.isEmpty())
        assertEquals(12, c.estado.value.marcha.posicion)
        // Al volver a configurar el Square, el mando se vuelve a anunciar.
        c.buscar()
        runCurrent()
        buscador.resultados.trySend(ResultadoBusqueda.Encontrado("SQUARE", "BB:BB", "SQUARE 1234"))
        runCurrent()
        assertEquals(2, transporteMando.publicaciones)
        assertEquals(EstadoMando.Esperando, c.estado.value.mando)
    }

    @Test
    fun bluetooth_apagado_y_encendido_vuelve_a_publicar() = runTest {
        val c = conMarcha(12)
        bluetooth.encendido.value = false
        runCurrent()
        assertEquals(EstadoMando.SinConexion, c.estado.value.mando)
        assertEquals(EstadoTransporte.Parado, transporteMando.estado.value)
        bluetooth.encendido.value = true
        runCurrent()
        assertEquals(2, transporteMando.publicaciones)
        assertEquals(EstadoMando.Esperando, c.estado.value.mando)
    }

    @Test
    fun sin_permiso_para_anunciarse_queda_sin_conexion() = runTest {
        bluetooth.permisoAnunciar.value = false
        val c = conMarcha(12)
        assertEquals(EstadoMando.SinConexion, c.estado.value.mando)
        assertEquals(0, transporteMando.publicaciones)
        bluetooth.permisoAnunciar.value = true
        runCurrent()
        assertEquals(1, transporteMando.publicaciones)
    }

    @Test
    fun sin_modo_periferico_no_esta_disponible() = runTest {
        perifericoDisponible = false
        val c = conMarcha(12)
        assertEquals(EstadoMando.NoDisponible, c.estado.value.mando)
        assertEquals(0, transporteMando.publicaciones)
    }

    @Test
    fun sin_square_configurado_no_se_publica_el_mando() = runTest {
        // Spec `zwift-controller`, «Sin Square configurado».
        almacen.guardado = EstadoGuardado(ultimaMarcha = 12, rodillo = DispositivoConfigurado("AA:AA", "Tacx Neo 12345"))
        val c = conexiones()
        runCurrent()
        assertEquals(EstadoMando.Desactivado, c.estado.value.mando)
        assertEquals(null, c.estado.value.servicios?.mando)
        assertEquals(0, transporteMando.publicaciones)
    }

    @Test
    fun sin_rodillo_configurado_no_se_publica_nada() = runTest {
        // Spec `platform-bridge`, «Sin rodillo configurado».
        val c = conexiones()
        wifi.direccion.value = "192.168.1.20"
        runCurrent()
        assertEquals(null, c.estado.value.servicios)
        assertEquals(EstadoPlataforma.NoSeOfrece, c.estado.value.plataforma)
        assertEquals(0, transporte.publicaciones)
    }

    @Test
    fun terminar_para_el_mando() = runTest {
        val c = conMarcha(12)
        mandoSaludado()
        mantener(SquareButton.Circulo)
        c.terminar()
        runCurrent()
        assertEquals(EstadoTransporte.Parado, transporteMando.estado.value)
        assertTrue(pulsadasEnZwift.isEmpty())
    }

    // Reenvío de las funciones (decisión 3)

    @Test
    fun cruceta_pulsada_mientras_se_mantiene() = runTest {
        conMarcha(12)
        mandoSaludado()
        mantener(SquareButton.CrucetaDerecha)
        assertEquals(setOf(FuncionZwift.DERECHA), pulsadasEnZwift)
        esperar(3_000)
        assertEquals(setOf(FuncionZwift.DERECHA), pulsadasEnZwift)
        soltar(SquareButton.CrucetaDerecha)
        assertTrue(pulsadasEnZwift.isEmpty())
        assertEquals("23 08 fb ff ff ff 0f 1a 04 08 00 10 00 1a 04 08 01 10 00", tramasMando().first())
        assertEquals(reposoMando, tramasMando().last())
    }

    @Test
    fun soltar_a_no_suelta_la_cruceta() = runTest {
        conMarcha(12)
        mandoSaludado()
        mantener(SquareButton.CrucetaDerecha)
        mantener(SquareButton.A)
        assertEquals(setOf(FuncionZwift.DERECHA, FuncionZwift.A), pulsadasEnZwift)
        soltar(SquareButton.A)
        assertEquals(setOf(FuncionZwift.DERECHA), pulsadasEnZwift)
    }

    @Test
    fun ride_on_con_triangulo_larga_es_una_pulsacion_breve() = runTest {
        configurar { it.conAsignacion(it.asignacion.asignar(Entrada(SquareButton.Triangulo), TipoGesto.Larga, Accion.Mando(FuncionZwift.Z))) }
        conMarcha(12)
        mandoSaludado()
        mantener(SquareButton.Triangulo)
        // La larga salta a los 800 ms y dura 100 ms en Zwift.
        esperar(850)
        assertEquals(setOf(FuncionZwift.Z), pulsadasEnZwift)
        esperar(Sesion.PULSACION_BREVE_MS)
        assertTrue(pulsadasEnZwift.isEmpty())
        soltar(SquareButton.Triangulo)
        esperar(2_000)
        assertEquals(1, tramasMando().count { it == zEnMando })
    }

    @Test
    fun en_erg_a_no_ajusta_la_potencia_y_llega_a_zwift() = runTest {
        val c = conErg(14)
        mandoSaludado()
        alternar()
        val modo = c.estado.value.simulacion.modo
        val enviados = neo.enviados.size
        pulsar(SquareButton.A)
        assertEquals(modo, c.estado.value.simulacion.modo)
        assertEquals(enviados, neo.enviados.size)
        assertTrue(tramasMando().any { it.startsWith("23 08 ef") })
        assertEquals(Accion.Mando(FuncionZwift.A), c.estado.value.ultimoGesto?.accion)
    }

    @Test
    fun una_funcion_de_zwift_no_vibra_ni_avisa() = runTest {
        val c = conMarcha(12)
        mandoSaludado()
        val eventos = eventosDe(c)
        vibrador.vibraciones.clear()
        pulsar(SquareButton.PalancaDerecha1)
        pulsar(SquareButton.Z)
        pulsar(SquareButton.Y)
        assertEquals(listOf("corta"), vibrador.vibraciones)
        assertEquals(1, eventos.size)
        assertTrue(eventos.single() is EventoRodaje.Cambio)
        assertEquals(1, tramasMando().count { it == zEnMando })
    }

    // Soltar todo (decisión 3)

    @Test
    fun freno_mantenido_y_square_perdido_suelta_el_freno() = runTest {
        conMarcha(12)
        mandoSaludado()
        mantener(SquareButton.FrenoDerecho)
        assertEquals(setOf(FuncionZwift.FRENO_DERECHO), pulsadasEnZwift)
        square.estado.value = EstadoEnlace.Perdida
        runCurrent()
        assertTrue(pulsadasEnZwift.isEmpty())
        assertEquals(reposoMando, tramasMando().last())
    }

    @Test
    fun la_captura_suelta_lo_mantenido_y_no_envia_nada() = runTest {
        val c = conMarcha(12)
        mandoSaludado()
        mantener(SquareButton.Circulo)
        c.empezarCaptura()
        runCurrent()
        assertTrue(pulsadasEnZwift.isEmpty())
        assertEquals(reposoMando, tramasMando().last())
        transporteMando.notificaciones.clear()
        soltar(SquareButton.Circulo)
        pulsar(SquareButton.A)
        assertTrue(transporteMando.notificaciones.isEmpty())
    }

    // Aviso de giro (spec `ride-screen`, «Aviso de giro»; design.md del change `exportar-bateria-y-giro`, decisión 8)

    @Test
    fun el_giro_mantenido_se_ve_mientras_se_mantiene() = runTest {
        val c = conMarcha(12)
        mandoSaludado()
        assertEquals(emptySet<LadoGiro>(), c.estado.value.giro)
        mantener(SquareButton.Equis)
        assertEquals(setOf(LadoGiro.IZQUIERDA), c.estado.value.giro)
        esperar(2_000)
        assertEquals(setOf(LadoGiro.IZQUIERDA), c.estado.value.giro)
        soltar(SquareButton.Equis)
        assertEquals(emptySet<LadoGiro>(), c.estado.value.giro)
    }

    @Test
    fun el_giro_soltado_antes_de_medio_segundo_sigue_visible_hasta_medio_segundo() = runTest {
        val c = conMarcha(12)
        mandoSaludado()
        mantener(SquareButton.Circulo)
        esperar(100)
        soltar(SquareButton.Circulo)
        assertEquals(setOf(LadoGiro.DERECHA), c.estado.value.giro)
        esperar(399)
        assertEquals(setOf(LadoGiro.DERECHA), c.estado.value.giro)
        esperar(2)
        assertEquals(emptySet<LadoGiro>(), c.estado.value.giro)
        // El mando sí lo soltó enseguida.
        assertTrue(pulsadasEnZwift.isEmpty())
    }

    @Test
    fun una_pulsacion_breve_se_ve_al_menos_medio_segundo() = runTest {
        val c = conMarcha(12)
        mandoSaludado()
        configurar { it.conAsignacion(it.asignacion.asignar(Entrada(SquareButton.Triangulo), TipoGesto.Corta, Accion.Mando(FuncionZwift.GIRAR_DERECHA))) }
        pulsar(SquareButton.Triangulo)
        esperar(200)
        assertTrue(pulsadasEnZwift.isEmpty())
        assertEquals(setOf(LadoGiro.DERECHA), c.estado.value.giro)
        esperar(400)
        assertEquals(emptySet<LadoGiro>(), c.estado.value.giro)
    }

    @Test
    fun los_dos_giros_a_la_vez() = runTest {
        val c = conMarcha(12)
        mandoSaludado()
        mantener(SquareButton.Equis, SquareButton.Circulo)
        assertEquals(setOf(LadoGiro.IZQUIERDA, LadoGiro.DERECHA), c.estado.value.giro)
    }

    @Test
    fun el_giro_con_otro_boton_asignado() = runTest {
        val c = conMarcha(12)
        mandoSaludado()
        configurar { it.conAsignacion(it.asignacion.asignar(Entrada(SquareButton.PalancaIzquierda2), TipoGesto.Pulsar, Accion.Mando(FuncionZwift.GIRAR_IZQUIERDA))) }
        mantener(SquareButton.PalancaIzquierda2)
        assertEquals(setOf(LadoGiro.IZQUIERDA), c.estado.value.giro)
    }

    @Test
    fun el_freno_de_la_misma_palanca_anula_el_giro_en_pantalla() = runTest {
        val c = conMarcha(12)
        mandoSaludado()
        mantener(SquareButton.FrenoIzquierdo)
        mantener(SquareButton.Equis)
        assertEquals(emptySet<LadoGiro>(), c.estado.value.giro)
    }

    @Test
    fun perder_el_square_girando_quita_el_aviso() = runTest {
        val c = conMarcha(12)
        mandoSaludado()
        mantener(SquareButton.Equis)
        square.estado.value = EstadoEnlace.Perdida
        runCurrent()
        assertEquals(emptySet<LadoGiro>(), c.estado.value.giro)
    }

    @Test
    fun el_aviso_de_accion_no_cambia_con_el_giro() = runTest {
        val c = conMarcha(12)
        mandoSaludado()
        mantener(SquareButton.Equis)
        pulsar(SquareButton.PalancaDerecha1)
        assertEquals(13, c.estado.value.marcha.posicion)
        assertEquals(setOf(LadoGiro.IZQUIERDA), c.estado.value.giro)
    }

    // Latencia (NF1)

    @Test
    fun latencia_de_la_palanca_igual_con_el_mando_conectado() = runTest {
        val c = conMarcha(12)
        mandoSaludado()
        mantener(SquareButton.Z)
        neo.porConfirmar.clear()
        ahora = 1_000
        val antes = currentTime
        val enviados = neo.enviados.size
        square.notificaciones.tryEmit(botones(SquareButton.PalancaDerecha1 to 1))
        runCurrent()
        // El comando ya está entregado sin que avance el reloj virtual: el mando no se interpone.
        assertEquals(antes, currentTime)
        assertEquals(enviados + 1, neo.enviados.size)
        assertEquals(13, c.estado.value.marcha.posicion)
        ahora = 1_085
        neo.porConfirmar.single()(true)
        assertEquals(85L, c.estado.value.latenciaMs)
    }

    // --- Modelos de resistencia (spec `resistance-models`) ---

    private var revsBiela = 0
    private var hora1024 = 0

    /** Pedalea [segundos] a [rpm] con [w] vatios: cada 2 s, una biela y una potencia, como el Neo. */
    private fun TestScope.pedalear(rpm: Int, segundos: Int, w: Int = 150) {
        repeat(segundos / 2) {
            revsBiela += rpm / 30
            hora1024 = (hora1024 + 2048) and 0xFFFF
            neo.notificaciones.tryEmit(Notificacion(NeoClient.ETIQUETA_CSC, byteArrayOf(0x02, revsBiela.toByte(), (revsBiela shr 8).toByte(), hora1024.toByte(), (hora1024 shr 8).toByte()), ahora))
            neo.notificaciones.tryEmit(Notificacion(NeoClient.ETIQUETA_POTENCIA, byteArrayOf(0, 0, w.toByte(), (w shr 8).toByte()), ahora))
            esperar(2_000)
        }
    }

    @Test
    fun cambiar_de_modelo_rodando_conserva_la_marcha() = runTest {
        val c = conMarcha(17)
        assertEquals(ComandoRodillo.Pendiente(5.0), c.estado.value.simulacion.enviado)
        configurar { it.copy(modelo = TipoModelo.TABLA) }
        assertEquals(ComandoRodillo.Pendiente(5.0), c.estado.value.simulacion.enviado)
        configurar { it.copy(modelo = TipoModelo.FISICO) }
        assertTrue(c.estado.value.simulacion.enviado is ComandoRodillo.Potencia)
        assertEquals(17, c.estado.value.marcha.posicion)
        configurar { it.copy(modelo = TipoModelo.SIMPLE) }
        assertEquals(ComandoRodillo.Pendiente(5.0), c.estado.value.simulacion.enviado)
    }

    @Test
    fun tabla_editada_se_aplica_al_momento() = runTest {
        val c = conMarcha(17)
        val editada = Configuracion().tabla(Transmision.Lineal()).toMutableList().also { it[16] = 7.0 }
        configurar { it.copy(modelo = TipoModelo.TABLA).conTabla(Transmision.Lineal(), editada) }
        assertEquals(ComandoRodillo.Pendiente(7.0), c.estado.value.simulacion.enviado)
        assertEquals(7.0, c.estado.value.simulacion.ajustePct)
    }

    @Test
    fun dificultad_y_potencia_minima_rodando() = runTest {
        val c = conMarcha(17)
        configurar { it.copy(dificultadPct = 150) }
        assertEquals(ComandoRodillo.Pendiente(7.5), c.estado.value.simulacion.enviado)
        configurar { it.copy(modelo = TipoModelo.FISICO, potenciaMinimaW = 50) }
        assertEquals(ComandoRodillo.Potencia(50), c.estado.value.simulacion.enviado)
    }

    @Test
    fun el_fisico_sin_neo_envia_su_potencia_al_conectar() = runTest {
        ajustes.configuracion.value = Configuracion(modelo = TipoModelo.FISICO)
        almacen.guardado = EstadoGuardado(ultimaMarcha = 12)
        val c = conexiones()
        runCurrent()
        assertTrue(neo.enviados.isEmpty())
        conectarLosDos(c)
        assertArrayEquals(FecCodec.potencia(30), neo.enviados.first())
    }

    @Test
    fun el_fisico_sigue_la_cadencia_y_calcula_velocidad_y_distancia() = runTest {
        ajustes.configuracion.value = Configuracion(modelo = TipoModelo.FISICO)
        val c = conMarcha(12)
        pedalear(90, 20)
        val p = (c.estado.value.simulacion.enviado as ComandoRodillo.Potencia).vatios
        assertTrue(p > 30, "$p")
        val v = checkNotNull(c.estado.value.velocidadKmh)
        assertTrue(v in 20.0..30.0, "$v")
        val d = c.estado.value.distanciaKm
        assertTrue(d > 0.0)
        // Con otro modelo, la velocidad no tiene valor y la distancia se conserva sin avanzar.
        configurar { it.copy(modelo = TipoModelo.SIMPLE) }
        pedalear(90, 6)
        assertNull(c.estado.value.velocidadKmh)
        assertEquals(d, c.estado.value.distanciaKm, 1e-9)
    }

    @Test
    fun neo_perdido_quita_la_velocidad_y_conserva_la_distancia() = runTest {
        ajustes.configuracion.value = Configuracion(modelo = TipoModelo.FISICO)
        val c = conMarcha(12)
        pedalear(90, 12)
        val d = c.estado.value.distanciaKm
        neo.conecta = false
        neo.estado.value = EstadoEnlace.Perdida
        runCurrent()
        assertNull(c.estado.value.velocidadKmh)
        assertEquals(d, c.estado.value.distanciaKm, 1e-9)
    }

    @Test
    fun siguiente_modelo_recorre_avisa_y_guarda() = runTest {
        val c = conMarcha(12)
        val eventos = eventosDe(c)
        c.accion(Accion.SiguienteModelo)
        runCurrent()
        assertEquals(EventoRodaje.ModeloCambiado(TipoModelo.TABLA), eventos.last())
        c.accion(Accion.SiguienteModelo)
        runCurrent()
        assertEquals(EventoRodaje.ModeloCambiado(TipoModelo.FISICO), eventos.last())
        assertEquals(TipoModelo.FISICO, ajustes.configuracion.value.modelo)
        assertTrue(c.estado.value.simulacion.enviado is ComandoRodillo.Potencia)
        c.accion(Accion.SiguienteModelo)
        runCurrent()
        assertEquals(EventoRodaje.ModeloCambiado(TipoModelo.SIMPLE), eventos.last())
        assertEquals(12, c.estado.value.marcha.posicion)
    }

    @Test
    fun el_registro_guarda_el_modelo_y_la_potencia_enviada() = runTest {
        ajustes.configuracion.value = Configuracion(modelo = TipoModelo.FISICO)
        conMarcha(12)
        val textos = registro.ultimas(40).map { it.texto }
        assertTrue(textos.any { it == "Modelo: FISICO" }, "$textos")
        assertTrue(textos.any { it.startsWith("Comando: Potencia") }, "$textos")
    }

    @Test
    fun siguiente_modelo_en_erg_no_envia_nada() = runTest {
        val c = conErg(14, vatios = 200)
        val antes = neo.enviados.size
        c.accion(Accion.SiguienteModelo)
        runCurrent()
        assertEquals(TipoModelo.TABLA, ajustes.configuracion.value.modelo)
        assertEquals(antes, neo.enviados.size)
    }

    // --- Pulsómetro (specs `heart-rate`, `device-connection` y `zwift-bridge`; change `pulso-zwift`) -------------------

    private val pulsoConfigurado = DispositivoConfigurado("CC:CC", "HW9-1234")

    private fun TestScope.conBanda(): Sesion {
        almacen.guardado = EstadoGuardado(pulsometro = pulsoConfigurado)
        return conexiones()
    }

    private fun TestScope.conBandaConectada(): Sesion = conBanda().also {
        assertEquals(Conectado, it.estado.value.pulsometro)
    }

    private fun lectura(bpm: Int) = Notificacion("2A37", byteArrayOf(0, bpm.toByte()), ahora)

    private val medidaPulso get() = DirconCodec.uuid16(0x2A37)

    @Test
    fun la_primera_conexion_del_pulsometro_lo_recuerda() = runTest {
        val c = conexiones()
        assertEquals(SinConexion, c.estado.value.pulsometro)
        c.buscarPulsometro()
        runCurrent()
        assertEquals(Buscando, c.estado.value.pulsometro)
        buscador.resultadosPulsometro.trySend(ResultadoBusqueda.Encontrado(PulsoClient.CLAVE, "CC:CC", "HW9-1234"))
        runCurrent()
        assertEquals(Conectado, c.estado.value.pulsometro)
        assertEquals(pulsoConfigurado, almacen.guardado.pulsometro)
        assertEquals(pulsoConfigurado, c.estado.value.pulsometroConfigurado)
        // La búsqueda del pulsómetro no toca al Neo ni al Square.
        assertEquals(SinConexion, c.estado.value.neo)
        assertEquals(SinConexion, c.estado.value.square)
        assertTrue(buscador.pedidos.isEmpty())
    }

    @Test
    fun al_abrir_con_configurado_conecta_sin_buscar() = runTest {
        val c = conBanda()
        assertEquals(Conectado, c.estado.value.pulsometro)
        assertEquals(listOf("directa CC:CC"), pulsometro.modos)
        assertEquals(0, buscador.busquedasPulsometro)
    }

    @Test
    fun buscar_dispositivos_sin_pulsometro_configurado_no_busca_la_banda_ni_da_error() = runTest {
        val c = conexiones()
        c.buscar()
        runCurrent()
        assertEquals(setOf("Tacx Neo", "SQUARE"), buscador.pedidos.single())
        assertEquals(0, buscador.busquedasPulsometro)
        assertEquals(SinConexion, c.estado.value.pulsometro)
        assertNull(c.estado.value.pulsometroConfigurado)
    }

    @Test
    fun buscar_dispositivos_con_banda_configurada_la_reconecta_sin_buscar_otra() = runTest {
        pulsometro.conecta = false
        val c = conBanda()
        assertEquals(Reconectando, c.estado.value.pulsometro)
        pulsometro.conecta = true
        c.buscar()
        runCurrent()
        assertEquals(Conectado, c.estado.value.pulsometro)
        assertEquals(0, buscador.busquedasPulsometro)
        assertEquals(setOf("Tacx Neo", "SQUARE"), buscador.pedidos.single())
    }

    @Test
    fun banda_no_encontrada() = runTest {
        val c = conexiones()
        c.buscarPulsometro()
        runCurrent()
        buscador.resultadosPulsometro.trySend(ResultadoBusqueda.NoEncontrado(PulsoClient.CLAVE))
        runCurrent()
        assertEquals(NoEncontrado, c.estado.value.pulsometro)
        assertNull(almacen.guardado.pulsometro)
        // Se puede volver a buscar.
        c.buscarPulsometro()
        runCurrent()
        assertEquals(Buscando, c.estado.value.pulsometro)
    }

    @Test
    fun una_banda_que_no_conecta_tras_encontrarla_no_se_recuerda() = runTest {
        pulsometro.conecta = false
        val c = conexiones()
        c.buscarPulsometro()
        runCurrent()
        buscador.resultadosPulsometro.trySend(ResultadoBusqueda.Encontrado(PulsoClient.CLAVE, "CC:CC", "HW9-1234"))
        runCurrent()
        assertEquals(NoEncontrado, c.estado.value.pulsometro)
        assertNull(almacen.guardado.pulsometro)
    }

    @Test
    fun banda_apagada_al_abrir_se_reconecta_sola() = runTest {
        pulsometro.conecta = false
        val c = conBanda()
        assertEquals(Reconectando, c.estado.value.pulsometro)
        assertTrue(pulsometro.esperando)
        pulsometro.despertar()
        runCurrent()
        assertEquals(Conectado, c.estado.value.pulsometro)
    }

    @Test
    fun reconectar_tras_perder_la_banda_y_el_pulso_se_pierde_al_momento() = runTest {
        val c = conBandaConectada()
        pulsometro.notificaciones.tryEmit(lectura(132))
        runCurrent()
        assertEquals(132, c.estado.value.pulso)
        pulsometro.conecta = false
        pulsometro.estado.value = EstadoEnlace.Perdida
        runCurrent()
        assertEquals(Reconectando, c.estado.value.pulsometro)
        assertNull(c.estado.value.pulso)
        pulsometro.despertar()
        runCurrent()
        assertEquals(Conectado, c.estado.value.pulsometro)
        pulsometro.notificaciones.tryEmit(lectura(90))
        runCurrent()
        assertEquals(90, c.estado.value.pulso)
    }

    @Test
    fun olvidar_la_banda_la_desconecta_y_quita_el_pulso() = runTest {
        val c = conBandaConectada()
        pulsometro.notificaciones.tryEmit(lectura(132))
        runCurrent()
        c.olvidar(Sesion.Dispositivo.Pulsometro)
        runCurrent()
        assertEquals(SinConexion, c.estado.value.pulsometro)
        assertNull(c.estado.value.pulsometroConfigurado)
        assertNull(almacen.guardado.pulsometro)
        assertNull(c.estado.value.pulso)
    }

    @Test
    fun otra_banda_sustituye_a_la_configurada() = runTest {
        val c = conBandaConectada()
        c.buscarPulsometro()
        runCurrent()
        // La banda conectada se mantiene mientras se busca.
        assertEquals(Conectado, c.estado.value.pulsometro)
        buscador.resultadosPulsometro.trySend(ResultadoBusqueda.Encontrado(PulsoClient.CLAVE, "DD:DD", "HW9-9999"))
        runCurrent()
        assertEquals(Conectado, c.estado.value.pulsometro)
        assertEquals(DispositivoConfigurado("DD:DD", "HW9-9999"), almacen.guardado.pulsometro)
        assertEquals(listOf("CC:CC", "DD:DD"), pulsometro.conexiones)
    }

    @Test
    fun terminar_la_sesion_desconecta_la_banda() = runTest {
        val c = conBandaConectada()
        c.terminar()
        runCurrent()
        assertEquals(SinConexion, c.estado.value.pulsometro)
        assertEquals(EstadoEnlace.Desconectado, pulsometro.estado.value)
    }

    @Test
    fun el_pulso_vale_de_1_a_250_y_caduca_a_los_5_s() = runTest {
        val c = conBandaConectada()
        pulsometro.notificaciones.tryEmit(lectura(132))
        runCurrent()
        assertEquals(132, c.estado.value.pulso)
        pulsometro.notificaciones.tryEmit(lectura(0))
        pulsometro.notificaciones.tryEmit(lectura(255))
        runCurrent()
        assertEquals(132, c.estado.value.pulso)
        advanceTimeBy(4_999)
        assertEquals(132, c.estado.value.pulso)
        advanceTimeBy(2)
        assertNull(c.estado.value.pulso)
        pulsometro.notificaciones.tryEmit(lectura(128))
        runCurrent()
        assertEquals(128, c.estado.value.pulso)
    }

    @Test
    fun una_lectura_de_16_bits_da_el_mismo_pulso() = runTest {
        val c = conBandaConectada()
        pulsometro.notificaciones.tryEmit(Notificacion("2A37", byteArrayOf(1, 132.toByte(), 0), ahora))
        runCurrent()
        assertEquals(132, c.estado.value.pulso)
    }

    @Test
    fun el_pulsometro_no_cambia_el_neo_el_square_los_botones_ni_la_marcha() = runTest {
        val c = conexiones()
        conectarLosDos(c)
        val antes = c.estado.value
        val enviados = neo.enviados.size
        c.buscarPulsometro()
        runCurrent()
        buscador.resultadosPulsometro.trySend(ResultadoBusqueda.Encontrado(PulsoClient.CLAVE, "CC:CC", "HW9-1234"))
        runCurrent()
        pulsometro.notificaciones.tryEmit(lectura(140))
        runCurrent()
        pulsometro.estado.value = EstadoEnlace.Perdida
        runCurrent()
        val despues = c.estado.value
        assertEquals(antes.neo, despues.neo)
        assertEquals(antes.square, despues.square)
        assertEquals(antes.marcha, despues.marcha)
        assertEquals(antes.mascara, despues.mascara)
        assertEquals(antes.simulacion, despues.simulacion)
        assertEquals(enviados, neo.enviados.size)
    }

    // --- Pulso hacia Zwift ---------------------------------------------------------------------------------------

    private fun TestScope.conZwiftYBanda(vararg suscripciones: Int): Sesion {
        almacen.guardado = EstadoGuardado(pulsometro = pulsoConfigurado)
        configurarNeoYSquare()
        val c = conexiones()
        wifi.direccion.value = "192.168.1.20"
        runCurrent()
        transporte.conectarZwift(*suscripciones)
        runCurrent()
        return c
    }

    private fun pulsosEnviados() = transporte.notificaciones.filter { it.caracteristica == medidaPulso }.map { hex(it.valor) }

    @Test
    fun el_pulso_llega_a_zwift_y_los_ceros_no() = runTest {
        conZwiftYBanda(0x2A37)
        pulsometro.notificaciones.tryEmit(lectura(140))
        runCurrent()
        pulsometro.notificaciones.tryEmit(lectura(0))
        runCurrent()
        pulsometro.notificaciones.tryEmit(lectura(255))
        runCurrent()
        assertEquals(listOf("00 8c"), pulsosEnviados())
    }

    @Test
    fun sin_lectura_o_sin_banda_no_sale_nada_hacia_zwift_y_vuelve_con_la_banda() = runTest {
        conZwiftYBanda(0x2A37)
        pulsometro.notificaciones.tryEmit(lectura(140))
        runCurrent()
        advanceTimeBy(6_000)
        assertEquals(listOf("00 8c"), pulsosEnviados())
        pulsometro.conecta = false
        pulsometro.estado.value = EstadoEnlace.Perdida
        runCurrent()
        advanceTimeBy(6_000)
        assertEquals(listOf("00 8c"), pulsosEnviados())
        pulsometro.despertar()
        pulsometro.notificaciones.tryEmit(lectura(100))
        runCurrent()
        assertEquals(listOf("00 8c", "00 64"), pulsosEnviados())
    }

    @Test
    fun sin_suscripcion_al_pulso_no_sale_nada() = runTest {
        conZwiftYBanda(0x2AD2, 0x2A63)
        pulsometro.notificaciones.tryEmit(lectura(140))
        runCurrent()
        assertTrue(pulsosEnviados().isEmpty())
    }

    @Test
    fun el_pulso_no_cambia_la_telemetria_del_neo() = runTest {
        conZwiftYBanda(0x2AD2, 0x2A63, 0x2A37)
        neo.notificaciones.tryEmit(Notificacion(NeoClient.ETIQUETA_POTENCIA, byteArrayOf(0, 0, 0xF7.toByte(), 0), ahora))
        pulsometro.notificaciones.tryEmit(lectura(140))
        runCurrent()
        advanceTimeBy(1_001)
        assertEquals(4, transporte.notificaciones.count { it.caracteristica == DirconCodec.uuid16(0x2AD2) })
        assertEquals(4, transporte.notificaciones.count { it.caracteristica == DirconCodec.uuid16(0x2A63) })
        assertEquals(1, pulsosEnviados().size)
    }

    // --- Ventilador (specs `fan-control` y `device-connection`; change `ventilador-headwind`) --------------------------

    private val ventiladorConfigurado = DispositivoConfigurado("DD:DD", "HEADWIND 4F2A")

    private fun TestScope.conVentilador(): Sesion {
        almacen.guardado = EstadoGuardado(ventilador = ventiladorConfigurado)
        return conexiones()
    }

    private fun bytesDe(vararg v: Int) = ByteArray(v.size) { v[it].toByte() }

    private fun TestScope.ventiladorNotifica(vararg v: Int) {
        ventilador.notificaciones.tryEmit(Notificacion("e038", bytesDe(*v), ahora))
        runCurrent()
    }

    /** El ventilador conectado y en manual al [pct] %, con una configuración de [bytes] bytes (120, 135, 150, 165, …). */
    private fun TestScope.ventiladorManual(pct: Int = 40, bytes: Int = 9): Sesion = conVentilador().also {
        ventiladorNotifica(0xFD, 0x01, pct, 4)
        ventiladorNotifica(0xFE, 0x05, 0x01, 120, 135, 150, 165, *IntArray(bytes - 4) { 7 + it })
    }

    @Test
    fun la_primera_conexion_del_ventilador_lo_recuerda_sin_tocar_lo_demas() = runTest {
        val c = conexiones()
        val antes = c.estado.value.marcha
        c.buscarVentilador()
        runCurrent()
        assertEquals(Buscando, c.estado.value.ventilador)
        buscador.resultados.trySend(ResultadoBusqueda.Encontrado(VentiladorClient.PREFIJO_NOMBRE, "DD:DD", "HEADWIND 4F2A"))
        runCurrent()
        assertEquals(setOf("HEADWIND"), buscador.pedidos.single())
        assertEquals(Conectado, c.estado.value.ventilador)
        assertEquals(ventiladorConfigurado, almacen.guardado.ventilador)
        assertEquals(ventiladorConfigurado, c.estado.value.ventiladorConfigurado)
        assertEquals(SinConexion, c.estado.value.neo)
        assertEquals(SinConexion, c.estado.value.square)
        assertEquals(antes, c.estado.value.marcha)
    }

    @Test
    fun al_abrir_con_ventilador_configurado_conecta_sin_buscar() = runTest {
        val c = conVentilador()
        assertEquals(Conectado, c.estado.value.ventilador)
        assertEquals(listOf("directa DD:DD"), ventilador.modos)
        assertTrue(buscador.pedidos.isEmpty())
    }

    @Test
    fun buscar_dispositivos_sin_ventilador_configurado_no_lo_busca_ni_da_error() = runTest {
        val c = conexiones()
        c.buscar()
        runCurrent()
        assertEquals(setOf("Tacx Neo", "SQUARE"), buscador.pedidos.single())
        assertEquals(SinConexion, c.estado.value.ventilador)
        assertNull(c.estado.value.ventiladorConfigurado)
    }

    @Test
    fun ventilador_no_encontrado() = runTest {
        val c = conexiones()
        c.buscarVentilador()
        runCurrent()
        buscador.resultados.trySend(ResultadoBusqueda.NoEncontrado(VentiladorClient.PREFIJO_NOMBRE))
        runCurrent()
        assertEquals(NoEncontrado, c.estado.value.ventilador)
        assertNull(almacen.guardado.ventilador)
        c.buscarVentilador()
        runCurrent()
        assertEquals(Buscando, c.estado.value.ventilador)
    }

    @Test
    fun ventilador_apagado_al_abrir_se_reconecta_solo_y_al_perderlo() = runTest {
        ventilador.conecta = false
        val c = conVentilador()
        assertEquals(Reconectando, c.estado.value.ventilador)
        ventilador.despertar()
        runCurrent()
        assertEquals(Conectado, c.estado.value.ventilador)
        ventilador.estado.value = EstadoEnlace.Perdida
        runCurrent()
        assertEquals(Reconectando, c.estado.value.ventilador)
    }

    @Test
    fun olvidar_el_ventilador_no_escribe_nada_en_el_ventilador() = runTest {
        val c = ventiladorManual()
        c.olvidar(Sesion.Dispositivo.Ventilador)
        runCurrent()
        assertEquals(SinConexion, c.estado.value.ventilador)
        assertNull(c.estado.value.ventiladorConfigurado)
        assertNull(almacen.guardado.ventilador)
        assertTrue(ventilador.enviados.isEmpty())
    }

    @Test
    fun otro_ventilador_sustituye_al_configurado() = runTest {
        val c = conVentilador()
        c.buscarVentilador()
        runCurrent()
        buscador.resultados.trySend(ResultadoBusqueda.Encontrado(VentiladorClient.PREFIJO_NOMBRE, "EE:EE", "HEADWIND 9999"))
        runCurrent()
        assertEquals(DispositivoConfigurado("EE:EE", "HEADWIND 9999"), almacen.guardado.ventilador)
    }

    @Test
    fun terminar_la_sesion_desconecta_el_ventilador() = runTest {
        val c = conVentilador()
        c.terminar()
        assertEquals(EstadoEnlace.Desconectado, ventilador.estado.value)
    }

    @Test
    fun el_estado_sale_de_lo_que_notifica_el_ventilador_tambien_si_cambia_desde_fuera() = runTest {
        val c = ventiladorManual(40)
        assertEquals(EstadoVentilador(ModoVentilador.Manual, 40, 40, c.estado.value.ventiladorEstado?.configuracion), c.estado.value.ventiladorEstado)
        ventiladorNotifica(0xFD, 0x01, 25, 2)
        assertEquals(ModoVentilador.Pulso, c.estado.value.ventiladorEstado?.modo)
        assertEquals(25, c.estado.value.ventiladorEstado?.velocidad)
        // Recuerda la última velocidad manual.
        assertEquals(40, c.estado.value.ventiladorEstado?.velocidadManual)
        assertEquals(listOf(120, 135, 150, 165), c.estado.value.ventiladorEstado?.configuracion?.umbrales)
    }

    @Test
    fun subir_en_manual_envia_la_velocidad_y_avisa() = runTest {
        val c = ventiladorManual(40)
        val eventos = eventosDe(c)
        c.accion(Accion.VentiladorMas)
        runCurrent()
        assertEquals(listOf("02 32"), ventilador.ordenes())
        assertEquals(listOf<EventoRodaje>(EventoRodaje.Ventilador(AvisoVentilador.Velocidad(50))), eventos)
    }

    @Test
    fun tres_subidas_seguidas_desde_20_acaban_en_50_sin_notificacion_y_los_extremos_se_respetan() = runTest {
        val c = ventiladorManual(20)
        repeat(3) { c.accion(Accion.VentiladorMas) }
        runCurrent()
        assertEquals(listOf("02 1E", "02 28", "02 32"), ventilador.ordenes())
    }

    @Test
    fun en_100_sigue_en_100_y_en_0_sigue_en_0() = runTest {
        val c = ventiladorManual(100)
        val eventos = eventosDe(c)
        c.accion(Accion.VentiladorMas)
        runCurrent()
        assertEquals(listOf("02 64"), ventilador.ordenes())
        assertEquals(EventoRodaje.Ventilador(AvisoVentilador.Velocidad(100)), eventos.single())
        // El ventilador confirma los 100 y después baja a 0 por su cuenta.
        ventiladorNotifica(0xFD, 0x01, 100, 4)
        ventiladorNotifica(0xFD, 0x01, 0, 4)
        c.accion(Accion.VentiladorMenos)
        runCurrent()
        assertEquals("02 00", ventilador.ordenes().last())
    }

    @Test
    fun en_automatico_la_velocidad_no_envia_nada_y_avisa() = runTest {
        val c = ventiladorManual(40)
        ventiladorNotifica(0xFD, 0x01, 25, 2)
        val eventos = eventosDe(c)
        c.accion(Accion.VentiladorMas)
        c.accion(Accion.VentiladorMenos)
        runCurrent()
        assertTrue(ventilador.enviados.isEmpty())
        assertEquals(listOf<EventoRodaje>(EventoRodaje.Ventilador(AvisoVentilador.EnAutomatico), EventoRodaje.Ventilador(AvisoVentilador.EnAutomatico)), eventos)
    }

    @Test
    fun alternar_desde_manual_automatico_y_otro_modo() = runTest {
        val c = ventiladorManual(40)
        val eventos = eventosDe(c)
        c.accion(Accion.VentiladorAlternar)
        runCurrent()
        assertEquals(listOf("04 02"), ventilador.ordenes())
        ventiladorNotifica(0xFD, 0x01, 25, 2)
        c.accion(Accion.VentiladorAlternar)
        runCurrent()
        assertEquals("04 04", ventilador.ordenes().last())
        ventiladorNotifica(0xFD, 0x01, 0, 1)
        c.accion(Accion.VentiladorAlternar)
        runCurrent()
        assertEquals("04 04", ventilador.ordenes().last())
        assertEquals(
            listOf<EventoRodaje>(
                EventoRodaje.Ventilador(AvisoVentilador.Automatico),
                EventoRodaje.Ventilador(AvisoVentilador.Manual(40)),
                EventoRodaje.Ventilador(AvisoVentilador.Manual(40)),
            ),
            eventos,
        )
    }

    @Test
    fun sin_ventilador_conectado_no_se_envia_ni_se_encola_nada() = runTest {
        ventilador.conecta = false
        val c = conVentilador()
        val eventos = eventosDe(c)
        c.accion(Accion.VentiladorMas)
        runCurrent()
        ventilador.despertar()
        runCurrent()
        assertTrue(ventilador.enviados.isEmpty())
        assertEquals(listOf<EventoRodaje>(EventoRodaje.Ventilador(AvisoVentilador.SinVentilador)), eventos)
    }

    @Test
    fun sin_ventilador_configurado_las_acciones_avisan() = runTest {
        val c = conexiones()
        val eventos = eventosDe(c)
        c.accion(Accion.VentiladorAlternar)
        runCurrent()
        assertEquals(listOf<EventoRodaje>(EventoRodaje.Ventilador(AvisoVentilador.SinVentilador)), eventos)
    }

    @Test
    fun una_orden_sin_confirmar_se_repite_una_vez() = runTest {
        val c = ventiladorManual(40)
        c.accion(Accion.VentiladorMas)
        runCurrent()
        advanceTimeBy(Sesion.ESPERA_VENTILADOR_MS + 1)
        runCurrent()
        advanceTimeBy(Sesion.ESPERA_VENTILADOR_MS + 1)
        runCurrent()
        assertEquals(listOf("02 32", "02 32"), ventilador.ordenes())
    }

    @Test
    fun una_orden_confirmada_no_se_repite() = runTest {
        val c = ventiladorManual(40)
        c.accion(Accion.VentiladorMas)
        runCurrent()
        ventiladorNotifica(0xFD, 0x01, 50, 4)
        advanceTimeBy(2 * Sesion.ESPERA_VENTILADOR_MS + 1)
        runCurrent()
        assertEquals(listOf("02 32"), ventilador.ordenes())
    }

    @Test
    fun las_acciones_del_ventilador_no_cambian_la_marcha_ni_mandan_nada_al_neo() = runTest {
        val c = ventiladorManual(40)
        val marcha = c.estado.value.marcha
        val neoAntes = neo.enviados.size
        val zwiftAntes = transporte.notificaciones.size
        Accion.VENTILADOR.forEach { c.accion(it) }
        runCurrent()
        assertEquals(marcha, c.estado.value.marcha)
        assertEquals(neoAntes, neo.enviados.size)
        assertEquals(zwiftAntes, transporte.notificaciones.size)
    }

    /** Responde a las órdenes del ventilador falso: `FE 05` con la configuración y `FE 06` con [estadoEscritura]. */
    private fun responderConfiguracion(config: IntArray, estadoEscritura: Int = 1) {
        ventilador.respuesta = { o ->
            when (o[0].toInt()) {
                0x05 -> ventilador.notificaciones.tryEmit(Notificacion("e038", bytesDe(0xFE, 0x05, 0x01, *config), ahora))
                0x06 -> ventilador.notificaciones.tryEmit(Notificacion("e038", bytesDe(0xFE, 0x06, estadoEscritura), ahora))
            }
        }
    }

    @Test
    fun cinco_toques_seguidos_mandan_una_sola_escritura_con_el_valor_final() = runTest {
        val c = ventiladorManual(40)
        ventilador.enviados.clear()
        responderConfiguracion(intArrayOf(120, 135, 150, 165, 7, 8, 9, 10, 11))
        repeat(5) {
            c.cambiarUmbralVentilador(2, (c.estado.value.ventiladorEstado!!.configuracion!!.umbrales[1]) + 1)
            runCurrent()
        }
        assertEquals(140, c.estado.value.ventiladorEstado?.configuracion?.umbrales?.get(1))
        advanceTimeBy(Sesion.REBOTE_UMBRALES_MS + 1)
        runCurrent()
        val escrituras = ventilador.ordenes().filter { it.startsWith("06") }
        assertEquals(listOf("06 78 8C 96 A5 07 08 09 0A 0B"), escrituras)
    }

    @Test
    fun la_configuracion_de_18_bytes_conserva_lo_no_tocado() = runTest {
        val c = ventiladorManual(40, bytes = 18)
        ventilador.enviados.clear()
        val base = intArrayOf(120, 135, 150, 165) + IntArray(14) { 7 + it }
        responderConfiguracion(base)
        c.cambiarUmbralVentilador(1, 121)
        runCurrent()
        advanceTimeBy(Sesion.REBOTE_UMBRALES_MS + 1)
        runCurrent()
        val escritura = ventilador.enviados.single { it[0].toInt() == 0x06 }
        assertEquals(19, escritura.size)
        assertEquals(121, escritura[1].toInt() and 0xFF)
        for (i in 1 until base.size) assertEquals(base[i], escritura[i + 1].toInt() and 0xFF)
        assertEquals(ModoVentilador.Manual, c.estado.value.ventiladorEstado?.modo)
        assertEquals(40, c.estado.value.ventiladorEstado?.velocidad)
    }

    @Test
    fun si_el_ventilador_no_responde_vuelve_el_valor_anterior_sin_error() = runTest {
        val c = ventiladorManual(40)
        ventilador.enviados.clear()
        c.cambiarUmbralVentilador(2, 140)
        runCurrent()
        assertEquals(140, c.estado.value.ventiladorEstado?.configuracion?.umbrales?.get(1))
        // Nadie responde al FE 05: se agota la espera y se vuelve a leer sin éxito.
        advanceTimeBy(Sesion.REBOTE_UMBRALES_MS + 3 * Sesion.ESPERA_VENTILADOR_MS)
        runCurrent()
        assertTrue(ventilador.ordenes().none { it.startsWith("06") })
    }

    @Test
    fun una_escritura_rechazada_deja_el_valor_del_ventilador() = runTest {
        val c = ventiladorManual(40)
        ventilador.enviados.clear()
        responderConfiguracion(intArrayOf(120, 135, 150, 165, 7, 8, 9, 10, 11), estadoEscritura = 2)
        c.cambiarUmbralVentilador(2, 140)
        runCurrent()
        advanceTimeBy(Sesion.REBOTE_UMBRALES_MS + 1)
        runCurrent()
        assertEquals(135, c.estado.value.ventiladorEstado?.configuracion?.umbrales?.get(1))
    }

    // --- Guía de inicio (spec `getting-started-guide`) ---------------------------------------------------------------

    @Test
    fun la_guia_se_evalua_al_arrancar_y_sin_dispositivos_queda_visible_y_abierta() = runTest {
        val c = conexiones()
        assertEquals(GuiaInicio.VISIBLE, c.estado.value.guiaInicio)
        assertEquals(GuiaInicio.VISIBLE, almacen.guardado.guiaInicio)
        assertTrue(c.guia.value.abierta)
        assertEquals(1, c.guia.value.pagina)
    }

    @Test
    fun al_actualizar_con_algun_dispositivo_la_guia_no_se_ensena_y_no_se_abre() = runTest {
        configurarNeoYSquare()
        val c = conexiones()
        assertEquals(GuiaInicio.FINALIZADA, c.estado.value.guiaInicio)
        assertEquals(GuiaInicio.FINALIZADA, almacen.guardado.guiaInicio)
        assertFalse(c.guia.value.abierta)
    }

    @Test
    fun una_guia_ya_evaluada_no_se_reevalua_y_con_dispositivos_arranca_minimizada() = runTest {
        configurarNeoYSquare()
        almacen.guardado = almacen.guardado.copy(guiaInicio = GuiaInicio.VISIBLE)
        val c = conexiones()
        assertEquals(GuiaInicio.VISIBLE, c.estado.value.guiaInicio)
        assertFalse(c.guia.value.abierta)
    }

    @Test
    fun no_volver_a_mostrar_se_guarda_y_cierra_la_hoja() = runTest {
        val c = conexiones()
        c.noMostrarGuia()
        runCurrent()
        assertEquals(GuiaInicio.DESCARTADA, c.estado.value.guiaInicio)
        assertEquals(GuiaInicio.DESCARTADA, almacen.guardado.guiaInicio)
        assertTrue(c.guia.value.cerrada)
        assertFalse(c.guia.value.abierta)
    }

    @Test
    fun la_equis_no_guarda_nada() = runTest {
        val c = conexiones()
        val escrituras = almacen.escrituras
        c.guia { it.cerrar() }
        runCurrent()
        assertEquals(GuiaInicio.VISIBLE, almacen.guardado.guiaInicio)
        assertEquals(escrituras, almacen.escrituras)
        assertTrue(c.guia.value.cerrada)
    }

    @Test
    fun reabrir_la_guia_la_deja_visible_y_abierta_en_la_pagina_1_tambien_si_estaba_descartada() = runTest {
        val c = conexiones()
        c.noMostrarGuia()
        runCurrent()
        c.guia { it.irA(3) }
        c.reabrirGuia()
        runCurrent()
        assertEquals(GuiaInicio.VISIBLE, almacen.guardado.guiaInicio)
        assertEquals(GuiaPantalla(pagina = 1, abierta = true, reabierta = true), c.guia.value)
    }

    @Test
    fun con_el_rodillo_el_controlador_y_la_plataforma_conectados_la_guia_finaliza_y_se_guarda() = runTest {
        configurarNeoYSquare()
        almacen.guardado = almacen.guardado.copy(guiaInicio = GuiaInicio.VISIBLE)
        val c = conexiones()
        conectarLosDos(c)
        assertEquals(GuiaInicio.VISIBLE, c.estado.value.guiaInicio)
        wifi.direccion.value = "192.168.1.20"
        runCurrent()
        transporte.conectarZwift(0x2AD2, 0x2A63)
        runCurrent()
        // La plataforma usa el rodillo cuando se ve suscrita, en la siguiente telemetría periódica.
        advanceTimeBy(app.smartbike.plataforma.PuentePlataforma.PERIODO_TELEMETRIA_MS + 1)
        runCurrent()
        assertTrue(pasosGuia(c.estado.value).completa)
        assertEquals(GuiaInicio.FINALIZADA, c.estado.value.guiaInicio)
        assertEquals(GuiaInicio.FINALIZADA, almacen.guardado.guiaInicio)
        // Una plataforma que se va no la hace reaparecer.
        wifi.direccion.value = null
        runCurrent()
        assertEquals(GuiaInicio.FINALIZADA, c.estado.value.guiaInicio)
    }

    @Test
    fun reabrirla_con_todo_hecho_la_mantiene_a_la_vista_en_esa_ejecucion() = runTest {
        configurarNeoYSquare()
        almacen.guardado = almacen.guardado.copy(guiaInicio = GuiaInicio.FINALIZADA)
        val c = conexiones()
        conectarLosDos(c)
        wifi.direccion.value = "192.168.1.20"
        runCurrent()
        transporte.conectarZwift(0x2AD2, 0x2A63)
        runCurrent()
        // La plataforma usa el rodillo cuando se ve suscrita, en la siguiente telemetría periódica.
        advanceTimeBy(app.smartbike.plataforma.PuentePlataforma.PERIODO_TELEMETRIA_MS + 1)
        runCurrent()
        c.reabrirGuia()
        runCurrent()
        assertTrue(pasosGuia(c.estado.value).completa)
        assertEquals(GuiaInicio.VISIBLE, c.estado.value.guiaInicio)
        assertTrue(c.guia.value.visible(c.estado.value.guiaInicio, pasosGuia(c.estado.value).completa))
    }
}
