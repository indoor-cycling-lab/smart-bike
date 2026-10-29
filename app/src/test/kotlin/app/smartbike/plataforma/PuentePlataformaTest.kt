package app.smartbike.plataforma

import app.smartbike.datos.RegistroSesion
import app.smartbike.dominio.ClaveIdentidad
import app.smartbike.dominio.Configurados
import app.smartbike.dominio.FuncionMando
import app.smartbike.dominio.FuncionZwift
import app.smartbike.dominio.Identidad
import app.smartbike.dominio.IdentidadDispositivo
import app.smartbike.dominio.LadoGiro
import app.smartbike.dominio.ModoConexion
import app.smartbike.dominio.Publicacion
import app.smartbike.dominio.ServiciosPublicados
import app.smartbike.dominio.Terreno
import app.smartbike.red.EstadoWifi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/** Spec `platform-bridge`: lo que el puente hace con la conexión, el wifi y la telemetría, sin `Sesion` (design.md del change `generalizar-plataformas`, decisión 5). */
@OptIn(ExperimentalCoroutinesApi::class)
class PuentePlataformaTest {
    private val registro = RegistroSesion(relojMonotonico = { 0L }, relojPared = { 0L })

    private class WifiFalso : EstadoWifi {
        override val direccion = MutableStateFlow<String?>(null)
        var activo = false
        override fun mantenerActivo(activo: Boolean) {
            this.activo = activo
        }
    }

    private class ConexionFalsa(
        override val modo: ModoConexion,
        override val simularKickr: Boolean,
        conMando: Boolean,
    ) : ConexionPlataforma {
        override val estado = MutableStateFlow<EstadoTransporte>(EstadoTransporte.Parado)
        override var suscrito = true
        override var suscritoPulso = false
        val publicadas = mutableListOf<IdentidadDispositivo>()
        var retiradas = 0
        var terminada = false
        val telemetrias = mutableListOf<TelemetriaPlataforma>()
        val pulsos = mutableListOf<Int>()
        val pulsadas = mutableSetOf<FuncionMando>()
        var mandoActivado: Boolean? = null
        val estadoMando = MutableStateFlow(EstadoMando.Esperando)
        override val mando: MandoEmulado? = if (conMando) {
            object : MandoEmulado {
                override val estado: StateFlow<EstadoMando> = estadoMando
                override val giro: Set<LadoGiro> = setOf(LadoGiro.IZQUIERDA)
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
                }
                override fun terminar() = Unit
            }
        } else {
            null
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

    private class DestinoFalso : DestinoPlataforma {
        val eventos = mutableListOf<String>()
        val estados = mutableListOf<EstadoPlataforma>()
        val modos = mutableListOf<Pair<ModoConexion, String>>()
        val estadosMando = mutableListOf<EstadoMando>()
        val identidades = mutableMapOf<String, IdentidadDispositivo>()
        var perdidas = 0
        var soltarTodos = 0
        override fun terreno(terreno: Terreno) {
            eventos += "terreno"
        }
        override fun potenciaErg(vatios: Int) {
            eventos += "erg $vatios"
        }
        override fun sinCambio() {
            eventos += "sin cambio"
        }
        override fun ultimoDePlataforma(ultimo: UltimoDePlataforma) {
            eventos += "ultimo"
        }
        override fun perdida() {
            perdidas++
        }
        override fun estadoPlataforma(estado: EstadoPlataforma) {
            estados += estado
        }
        override fun soltarTodo() {
            soltarTodos++
        }
        val usos = mutableListOf<Pair<Boolean, Boolean>>()

        override fun usoServicios(rodillo: Boolean, pulso: Boolean) {
            usos += rodillo to pulso
        }

        override fun conexionNueva(modo: ModoConexion, nombre: String) {
            modos += modo to nombre
        }
        override fun estadoMando(estado: EstadoMando) {
            estadosMando += estado
        }
        override fun identidad(clave: String) = identidades[clave]
        override fun guardarIdentidad(clave: String, identidad: IdentidadDispositivo) {
            identidades[clave] = identidad
        }
    }

    private val wifi = WifiFalso()
    private val destino = DestinoFalso()
    private val conexiones = mutableListOf<ConexionFalsa>()
    private var biela = 0

    private fun TestScope.puente(): PuentePlataforma {
        val fabrica = { s: ServiciosPublicados, k: Boolean, _: ReceptorPlataforma, _: CoroutineScope ->
            ConexionFalsa(s.modo, k, conMando = s.mando != null).also { conexiones += it }
        }
        return PuentePlataforma(backgroundScope, registro, wifi, fabrica, destino) {
            TelemetriaPlataforma(vatios = 200 + biela, cadenciaRpm = 90, biela = null, velocidadKmh = 0.0)
        }
    }

    private val actual get() = conexiones.last()

    /** Lo que se publica con [m] y el rodillo, el Square y la banda configurados (o lo que se diga). */
    private fun servicios(m: ModoConexion, mando: Boolean = true, pulso: Boolean = true) = checkNotNull(Publicacion.de(m, Configurados(rodillo = true, mando = mando, pulsometro = pulso)))

    @Test
    fun sin_rodillo_configurado_no_se_publica_nada() = runTest {
        // Spec `platform-bridge`, «Sin rodillo configurado».
        val p = puente()
        p.usar(servicios(ModoConexion.FTMS_WIFI), Identidad.SMART_BIKE)
        wifi.direccion.value = "192.168.1.20"
        p.wifiCambia("192.168.1.20")
        val primera = actual
        p.usar(null, Identidad.SMART_BIKE)
        assertTrue(primera.terminada)
        assertEquals(1, conexiones.size)
        assertEquals(EstadoPlataforma.NoSeOfrece, destino.estados.last())
        assertFalse(wifi.activo)
        p.wifiCambia("192.168.0.11")
        assertEquals(1, conexiones.size)
    }

    @Test
    fun configurar_una_banda_rehace_la_conexion_con_el_pulso() = runTest {
        // Spec `platform-bridge`, «Configurar u olvidar cambia lo publicado».
        val p = puente()
        p.usar(servicios(ModoConexion.FTMS_WIFI, pulso = false), Identidad.SMART_BIKE)
        val soltadas = destino.soltarTodos
        p.usar(servicios(ModoConexion.FTMS_WIFI, pulso = true), Identidad.SMART_BIKE)
        assertEquals(2, conexiones.size)
        assertEquals(soltadas + 1, destino.soltarTodos)
        assertEquals(1, destino.identidades.size, "misma identidad: mismo modo y mismo nombre")
    }

    @Test
    fun sin_square_no_hay_mando_y_el_estado_del_mando_es_desactivado() = runTest {
        val p = puente()
        p.usar(servicios(ModoConexion.FTMS_WIFI, mando = false), Identidad.SMART_BIKE)
        runCurrent()
        assertEquals(null, actual.mando)
        assertEquals(EstadoMando.Desactivado, destino.estadosMando.last())
    }

    @Test
    fun el_uso_de_los_servicios_llega_al_destino_con_la_telemetria() = runTest {
        // Design.md de `pestana-conexion`, decisión 6.
        val p = puente()
        p.usar(servicios(ModoConexion.FTMS_WIFI), Identidad.SMART_BIKE)
        wifi.direccion.value = "192.168.1.20"
        p.wifiCambia("192.168.1.20")
        actual.suscritoPulso = true
        actual.estado.value = EstadoTransporte.Conectado("192.168.0.10")
        runCurrent()
        advanceTimeBy(PuentePlataforma.PERIODO_TELEMETRIA_MS + 1)
        assertEquals(true to true, destino.usos.last())
        actual.estado.value = EstadoTransporte.Esperando
        runCurrent()
        assertEquals(false to false, destino.usos.last())
    }

    @Test
    fun usar_crea_la_conexion_con_su_identidad_nueva_y_la_guarda_por_clave() = runTest {
        val p = puente()
        p.usar(servicios(ModoConexion.FTMS_WIFI), Identidad.SMART_BIKE)
        runCurrent()
        val clave = ClaveIdentidad.de(ModoConexion.FTMS_WIFI, Identidad.SMART_BIKE)
        assertEquals(setOf(clave), destino.identidades.keys)
        assertEquals(listOf(ModoConexion.FTMS_WIFI), destino.modos.map { it.first })
        assertEquals("Smart Bike", destino.modos.single().second)
    }

    @Test
    fun la_identidad_guardada_se_reutiliza() = runTest {
        val id = IdentidadDispositivo("02:11:22:33:44:55", "0123456789")
        destino.identidades[ClaveIdentidad.de(ModoConexion.FTMS_WIFI, Identidad.SMART_BIKE)] = id
        val p = puente()
        wifi.direccion.value = "192.168.1.20"
        p.usar(servicios(ModoConexion.FTMS_WIFI), Identidad.SMART_BIKE)
        p.wifiCambia("192.168.1.20")
        assertEquals(listOf(id), actual.publicadas)
        assertEquals(1, destino.identidades.size)
    }

    @Test
    fun el_wifi_que_va_y_viene_publica_y_retira_y_mantiene_el_wifi_activo() = runTest {
        val p = puente()
        p.usar(servicios(ModoConexion.FTMS_WIFI), Identidad.SMART_BIKE)
        wifi.direccion.value = "192.168.1.20"
        p.wifiCambia("192.168.1.20")
        assertEquals(1, actual.publicadas.size)
        assertTrue(wifi.activo)
        assertEquals(EstadoPlataforma.Esperando, destino.estados.last())
        wifi.direccion.value = null
        p.wifiCambia(null)
        assertEquals(1, actual.retiradas)
        assertFalse(wifi.activo)
        assertEquals(EstadoPlataforma.SinWifi, destino.estados.last())
    }

    @Test
    fun el_estado_del_transporte_se_traduce_a_estado_de_la_plataforma() = runTest {
        val p = puente()
        p.usar(servicios(ModoConexion.FTMS_WIFI), Identidad.SMART_BIKE)
        wifi.direccion.value = "192.168.1.20"
        p.wifiCambia("192.168.1.20")
        actual.estado.value = EstadoTransporte.NoVisible
        runCurrent()
        assertEquals(EstadoPlataforma.NoVisible, destino.estados.last())
        actual.estado.value = EstadoTransporte.Conectado("192.168.0.10")
        runCurrent()
        assertEquals(EstadoPlataforma.Conectado("192.168.0.10"), destino.estados.last())
    }

    @Test
    fun telemetria_cuatro_veces_por_segundo_solo_con_la_plataforma_conectada_y_suscrita() = runTest {
        val p = puente()
        p.usar(servicios(ModoConexion.FTMS_WIFI), Identidad.SMART_BIKE)
        wifi.direccion.value = "192.168.1.20"
        p.wifiCambia("192.168.1.20")
        advanceTimeBy(1_000)
        assertTrue(actual.telemetrias.isEmpty(), "sin la plataforma conectada no hay telemetría")
        actual.estado.value = EstadoTransporte.Conectado("192.168.0.10")
        runCurrent()
        advanceTimeBy(1_100)
        assertEquals(4, actual.telemetrias.size)
        assertEquals(200, actual.telemetrias.first().vatios)
        actual.suscrito = false
        advanceTimeBy(1_000)
        assertEquals(4, actual.telemetrias.size)
    }

    @Test
    fun perder_la_plataforma_conectada_lo_avisa_una_vez_y_para_la_telemetria() = runTest {
        val p = puente()
        p.usar(servicios(ModoConexion.FTMS_WIFI), Identidad.SMART_BIKE)
        wifi.direccion.value = "192.168.1.20"
        p.wifiCambia("192.168.1.20")
        actual.estado.value = EstadoTransporte.Conectado("192.168.0.10")
        runCurrent()
        advanceTimeBy(600)
        val enviadas = actual.telemetrias.size
        actual.estado.value = EstadoTransporte.Esperando
        runCurrent()
        assertEquals(1, destino.perdidas)
        advanceTimeBy(1_000)
        assertEquals(enviadas, actual.telemetrias.size)
        // Sin haber estado conectada, perderla no avisa.
        actual.estado.value = EstadoTransporte.NoVisible
        runCurrent()
        assertEquals(1, destino.perdidas)
    }

    @Test
    fun cambiar_de_modo_rodando_suelta_todo_termina_la_anterior_y_publica_la_nueva() = runTest {
        val p = puente()
        p.usar(servicios(ModoConexion.FTMS_WIFI), Identidad.SMART_BIKE)
        wifi.direccion.value = "192.168.1.20"
        p.wifiCambia("192.168.1.20")
        val primera = actual
        val soltadas = destino.soltarTodos
        p.usar(servicios(ModoConexion.ZWIFT_RIDE), Identidad.SMART_BIKE)
        assertEquals(soltadas + 1, destino.soltarTodos)
        assertTrue(primera.terminada)
        assertEquals(2, conexiones.size)
        assertEquals(ModoConexion.ZWIFT_RIDE, actual.modo)
        assertEquals(1, actual.publicadas.size)
        assertEquals(2, destino.identidades.size, "cada modo tiene su identidad")
        assertEquals(listOf(ModoConexion.FTMS_WIFI, ModoConexion.ZWIFT_RIDE), destino.modos.map { it.first })
    }

    @Test
    fun cambiar_de_nombre_rodando_crea_otra_conexion_con_su_nombre_y_su_identidad() = runTest {
        val p = puente()
        p.usar(servicios(ModoConexion.FTMS_WIFI), Identidad.SMART_BIKE)
        p.usar(servicios(ModoConexion.FTMS_WIFI), Identidad.KICKR_BIKE_PRO)
        assertEquals(2, conexiones.size)
        assertTrue(actual.simularKickr)
        assertTrue(destino.modos.last().second.startsWith("KICKR BIKE PRO "))
        assertEquals(2, destino.identidades.size)
    }

    @Test
    fun los_mismos_ajustes_no_rehacen_la_conexion() = runTest {
        val p = puente()
        p.usar(servicios(ModoConexion.FTMS_WIFI), Identidad.SMART_BIKE)
        val soltadas = destino.soltarTodos
        p.usar(servicios(ModoConexion.FTMS_WIFI), Identidad.SMART_BIKE)
        assertEquals(1, conexiones.size)
        assertEquals(soltadas, destino.soltarTodos)
    }

    @Test
    fun el_mando_el_pulso_y_el_giro_van_a_la_conexion_actual() = runTest {
        val p = puente()
        p.usar(servicios(ModoConexion.FTMS_WIFI), Identidad.SMART_BIKE)
        p.pulsar(FuncionZwift.A)
        assertEquals(setOf<FuncionMando>(FuncionZwift.A), actual.pulsadas)
        assertEquals(setOf(LadoGiro.IZQUIERDA), p.giro)
        p.soltar(FuncionZwift.A)
        p.pulsar(FuncionZwift.B)
        p.soltarTodo()
        assertTrue(actual.pulsadas.isEmpty())
        p.pulso(142)
        assertEquals(listOf(142), actual.pulsos)
        assertEquals(true, actual.mandoActivado)
    }

    @Test
    fun el_estado_del_mando_llega_al_destino_y_sin_mando_es_desactivado() = runTest {
        val p = puente()
        p.usar(servicios(ModoConexion.FTMS_WIFI), Identidad.SMART_BIKE)
        runCurrent()
        assertEquals(EstadoMando.Esperando, destino.estadosMando.last())
        actual.estadoMando.value = EstadoMando.Conectado
        runCurrent()
        assertEquals(EstadoMando.Conectado, destino.estadosMando.last())
    }

    @Test
    fun terminar_termina_la_conexion_y_devuelve_el_wifi() = runTest {
        val p = puente()
        p.usar(servicios(ModoConexion.FTMS_WIFI), Identidad.SMART_BIKE)
        wifi.direccion.value = "192.168.1.20"
        p.wifiCambia("192.168.1.20")
        p.terminar()
        assertTrue(actual.terminada)
        assertFalse(wifi.activo)
        // Terminado, ni el wifi ni los ajustes hacen nada más.
        p.wifiCambia("192.168.0.11")
        p.usar(servicios(ModoConexion.ZWIFT_RIDE), Identidad.SMART_BIKE)
        assertEquals(1, conexiones.size)
        assertEquals(1, actual.publicadas.size)
    }

    @Test
    fun lo_que_llega_de_la_plataforma_pasa_al_destino() = runTest {
        // El destino es también el receptor de las conexiones (decisión 5).
        val receptores = mutableListOf<ReceptorPlataforma>()
        val p = PuentePlataforma(
            backgroundScope,
            registro,
            wifi,
            { sv, k, r, _ ->
                receptores += r
                ConexionFalsa(sv.modo, k, conMando = false)
            },
            destino,
            { null },
        )
        p.usar(servicios(ModoConexion.FTMS_WIFI), Identidad.SMART_BIKE)
        receptores.single().terreno(Terreno(3.0))
        receptores.single().potenciaErg(180)
        receptores.single().sinCambio()
        assertEquals(listOf("terreno", "erg 180", "sin cambio"), destino.eventos)
        assertEquals(EstadoMando.Desactivado, destino.estadosMando.last(), "sin mando en el modo")
    }

    // --- Rodillo por Bluetooth (change ftms-bluetooth, decisión 4) ---

    @Test
    fun con_bluetooth_se_publica_sin_wifi_y_el_estado_no_dice_sin_wifi() = runTest {
        val p = puente()
        p.usar(servicios(ModoConexion.FTMS_BLUETOOTH), Identidad.SMART_BIKE)
        p.wifiCambia(null)
        assertEquals(1, actual.publicadas.size, "se anuncia aunque no haya wifi")
        assertEquals(0, actual.retiradas)
        assertFalse(wifi.activo, "el wifi en baja latencia solo lo piden los modos por wifi")
        assertTrue(destino.estados.none { it == EstadoPlataforma.SinWifi })
        assertEquals(EstadoPlataforma.Esperando, destino.estados.last())
    }

    @Test
    fun con_bluetooth_el_wifi_que_va_y_viene_no_cambia_nada() = runTest {
        val p = puente()
        p.usar(servicios(ModoConexion.FTMS_BLUETOOTH), Identidad.SMART_BIKE)
        wifi.direccion.value = "192.168.1.20"
        p.wifiCambia("192.168.1.20")
        wifi.direccion.value = null
        p.wifiCambia(null)
        assertEquals(0, actual.retiradas)
        assertFalse(wifi.activo)
        assertTrue(destino.estados.none { it == EstadoPlataforma.SinWifi })
    }

    @Test
    fun el_estado_del_transporte_por_bluetooth_se_traduce() = runTest {
        val p = puente()
        p.usar(servicios(ModoConexion.FTMS_BLUETOOTH), Identidad.SMART_BIKE)
        p.wifiCambia(null)
        actual.estado.value = EstadoTransporte.SinBluetooth
        runCurrent()
        assertEquals(EstadoPlataforma.SinBluetooth, destino.estados.last())
        actual.estado.value = EstadoTransporte.NoDisponible
        runCurrent()
        assertEquals(EstadoPlataforma.NoDisponible, destino.estados.last())
        actual.estado.value = EstadoTransporte.Conectado("AA:BB")
        runCurrent()
        assertEquals(EstadoPlataforma.Conectado("AA:BB"), destino.estados.last())
        advanceTimeBy(600)
        assertTrue(actual.telemetrias.isNotEmpty(), "la telemetría a 4 Hz no depende del wifi")
    }

    @Test
    fun de_wifi_a_bluetooth_rodando_suelta_el_mando_termina_la_anterior_y_deja_de_pedir_el_wifi() = runTest {
        val p = puente()
        p.usar(servicios(ModoConexion.FTMS_WIFI), Identidad.SMART_BIKE)
        wifi.direccion.value = "192.168.1.20"
        p.wifiCambia("192.168.1.20")
        assertTrue(wifi.activo)
        val primera = actual
        val soltadas = destino.soltarTodos
        p.usar(servicios(ModoConexion.FTMS_BLUETOOTH), Identidad.SMART_BIKE)
        assertEquals(soltadas + 1, destino.soltarTodos)
        assertTrue(primera.terminada)
        assertEquals(ModoConexion.FTMS_BLUETOOTH, actual.modo)
        assertEquals(1, actual.publicadas.size)
        assertFalse(wifi.activo)
        runCurrent()
        assertEquals(EstadoMando.Esperando, destino.estadosMando.last(), "el mando va dentro del modo por Bluetooth")
        assertEquals(ClaveIdentidad.de(ModoConexion.FTMS_BLUETOOTH, Identidad.SMART_BIKE), destino.identidades.keys.last())
    }

    @Test
    fun de_bluetooth_a_wifi_rodando_vuelve_a_pedir_el_wifi_y_publica_por_wifi() = runTest {
        val p = puente()
        wifi.direccion.value = "192.168.1.20"
        p.usar(servicios(ModoConexion.FTMS_BLUETOOTH), Identidad.SMART_BIKE)
        p.wifiCambia("192.168.1.20")
        assertFalse(wifi.activo)
        p.usar(servicios(ModoConexion.FTMS_WIFI), Identidad.SMART_BIKE)
        assertEquals(1, actual.publicadas.size)
        assertTrue(wifi.activo)
        assertEquals(EstadoPlataforma.Esperando, destino.estados.last())
    }

    @Test
    fun el_mando_del_modo_por_bluetooth_va_dentro_y_recibe_las_funciones() = runTest {
        val p = puente()
        p.usar(servicios(ModoConexion.FTMS_BLUETOOTH), Identidad.SMART_BIKE)
        p.pulsar(FuncionZwift.A)
        assertEquals(setOf<FuncionMando>(FuncionZwift.A), actual.pulsadas)
        assertEquals(setOf(LadoGiro.IZQUIERDA), p.giro)
        p.soltar(FuncionZwift.A)
        assertTrue(actual.pulsadas.isEmpty())
        assertEquals(true, actual.mandoActivado)
    }
}
