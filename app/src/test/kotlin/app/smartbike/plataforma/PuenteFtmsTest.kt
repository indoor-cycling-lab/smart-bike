package app.smartbike.plataforma

import app.smartbike.datos.RegistroSesion
import app.smartbike.dominio.IdentidadDispositivo
import app.smartbike.plataforma.zwift.MandoZwift
import app.smartbike.plataforma.zwift.ServicioDelMando
import app.smartbike.protocolo.DirconCodec.uuid16
import app.smartbike.protocolo.ResultadoGatt
import kotlinx.coroutines.cancel
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/** Spec `zwift-bridge`, «Pulso hacia Zwift»; design.md del change `pulso-zwift`, decisión 4. */
class PuenteFtmsTest {
    private val puente = PuenteFtms("0.0-test") { 0 }
    private val servicioPulso = uuid16(0x180D)
    private val medida = uuid16(0x2A37)

    @Test
    fun ofrece_el_servicio_de_pulso_y_el_txt_lo_incluye() {
        assertTrue(servicioPulso in puente.servidor.descubrirServicios())
        val txt = DirconServer.registros(puente.servidor, IdentidadDispositivo("02:11:22:33:44:55", "0123456789"))
        assertTrue("0x180D" in txt.getValue("ble-service-uuids").split(","))
    }

    @Test
    fun con_el_servicio_del_mando_el_rodillo_ofrece_los_cinco_servicios_y_el_txt_lleva_fc82() {
        val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Unconfined)
        val mando = MandoZwift(
            scope,
            app.smartbike.datos.RegistroSesion(relojMonotonico = { 0L }, relojPared = { 0L }),
            object : TransporteDircon {
                override val estado = kotlinx.coroutines.flow.MutableStateFlow<EstadoTransporte>(EstadoTransporte.Parado)
                override fun publicar(servidor: app.smartbike.protocolo.ServidorGatt, identidad: IdentidadDispositivo, nombre: String) = Unit
                override fun parar() = Unit
                override fun notificar(n: app.smartbike.protocolo.NotificacionGatt) = Unit
            },
            servicioDelMando = ServicioDelMando.EN_RODILLO,
        )
        val conMando = PuenteFtms("0.0-test", listOf(mando.servicio)) { 0 }
        assertEquals(
            listOf(0x1826, 0x1818, 0x180D, 0x180A, 0xFC82).map(::uuid16),
            conMando.servidor.descubrirServicios(),
        )
        val txt = DirconServer.registros(conMando.servidor, IdentidadDispositivo("02:11:22:33:44:55", "0123456789"))
        assertEquals("0x1826,0x1818,0x180D,0x180A,0xFC82", txt.getValue("ble-service-uuids"))
        scope.cancel()
    }

    @Test
    fun sin_el_servicio_del_mando_nada_cambia() {
        assertEquals(listOf(0x1826, 0x1818, 0x180D, 0x180A).map(::uuid16), puente.servidor.descubrirServicios())
    }

    private fun leerTexto(p: PuenteFtms, uuid: Int) = String((p.servidor.leer(uuid16(uuid)) as ResultadoGatt.Ok).valor, Charsets.UTF_8)

    @Test
    fun ofrece_serie_firmware_y_hardware() {
        val p = PuenteFtms("1.2.3") { 0 }
        p.fijarIdentidad(IdentidadDispositivo("02:11:22:33:44:55", "0123456789"))
        assertEquals("0123456789", leerTexto(p, 0x2A25))
        assertEquals("1.2.3", leerTexto(p, 0x2A26))
        assertEquals("1.0", leerTexto(p, 0x2A27))
        // Lo de siempre sigue donde estaba.
        assertEquals("Smart Bike", leerTexto(p, 0x2A29))
        assertEquals("1.2.3", leerTexto(p, 0x2A28))
    }

    @Test
    fun la_serie_es_la_de_la_identidad_vigente_al_leer() {
        val p = PuenteFtms("0.0-test") { 0 }
        p.fijarIdentidad(IdentidadDispositivo("02:11:22:33:44:55", "1111111111"))
        assertEquals("1111111111", leerTexto(p, 0x2A25))
        p.fijarIdentidad(IdentidadDispositivo("02:aa:bb:cc:dd:ee", "2222222222"))
        assertEquals("2222222222", leerTexto(p, 0x2A25))
    }

    @Test
    fun la_lectura_queda_en_el_registro_con_su_valor() {
        val registro = RegistroSesion(relojMonotonico = { 0L }, relojPared = { 0L })
        val p = PuenteFtms("1.2.3", registro = registro) { 0 }
        p.fijarIdentidad(IdentidadDispositivo("02:11:22:33:44:55", "0123456789"))
        leerTexto(p, 0x2A25)
        leerTexto(p, 0x2A26)
        leerTexto(p, 0x2A27)
        val textos = registro.ultimas(10).map { it.texto }
        assertTrue(textos.any { "serie" in it && "0123456789" in it }, "$textos")
        assertTrue(textos.any { "firmware" in it && "1.2.3" in it }, "$textos")
        assertTrue(textos.any { "hardware" in it && "1.0" in it }, "$textos")
    }

    @Test
    fun la_ubicacion_del_sensor_se_lee_como_otra() {
        val r = puente.servidor.leer(uuid16(0x2A38))
        assertArrayEquals(byteArrayOf(0), (r as ResultadoGatt.Ok).valor)
    }

    @Test
    fun sin_suscripcion_no_sale_pulso() = assertNull(puente.pulso(132))

    @Test
    fun con_suscripcion_sale_la_medida_de_8_bits() {
        puente.servidor.suscribir(medida, true)
        val n = checkNotNull(puente.pulso(132))
        assertEquals(medida, n.caracteristica)
        assertArrayEquals(byteArrayOf(0x00, 132.toByte()), n.valor)
    }

    @Test
    fun suscribirse_solo_al_pulso_no_activa_la_telemetria() {
        puente.servidor.suscribir(medida, true)
        assertFalse(puente.suscrito)
    }

    @Test
    fun suscribirse_a_la_potencia_no_activa_el_pulso() {
        puente.servidor.suscribir(uuid16(0x2A63), true)
        assertTrue(puente.suscrito)
        assertNull(puente.pulso(132))
    }
}
