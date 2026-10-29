package app.smartbike.ble

import android.bluetooth.BluetoothGatt
import app.smartbike.protocolo.DirconCodec.uuid16
import app.smartbike.protocolo.ErrorGatt
import app.smartbike.protocolo.NotificacionGatt
import app.smartbike.protocolo.ResultadoGatt
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/** La lógica de `GattServerHost` que no depende de Android (design.md del change `mando-zwift-emulado`, decisión 4). */
class GattServerHostTest {

    // Clientes por uso (arquitectura.md § 7.4)

    @Test
    fun una_conexion_de_la_app_como_central_no_es_cliente() {
        val c = ClientesGatt<String>()
        // El Square se desconecta sin haber usado el servidor: no cuenta.
        assertEquals(ClientesGatt.Desconexion.NoEraCliente, c.desconectado("SQUARE"))
        assertTrue(c.todos.isEmpty())
    }

    @Test
    fun cliente_es_quien_usa_el_servidor_una_vez() {
        val c = ClientesGatt<String>()
        assertTrue(c.usa("ZWIFT", "zwift"))
        assertFalse(c.usa("ZWIFT", "zwift"))
        assertEquals(listOf("zwift"), c.todos)
        assertEquals(ClientesGatt.Desconexion.NoEraCliente, c.desconectado("NEO"))
        assertEquals(ClientesGatt.Desconexion.NingunoQueda, c.desconectado("ZWIFT"))
        assertTrue(c.todos.isEmpty())
    }

    @Test
    fun con_dos_clientes_queda_el_ultimo() {
        val c = ClientesGatt<String>()
        c.usa("PC", "pc")
        c.usa("TABLETA", "tableta")
        assertEquals(ClientesGatt.Desconexion.QuedanOtros("PC"), c.desconectado("TABLETA"))
        c.vaciar()
        assertTrue(c.todos.isEmpty())
    }

    // Alta de servicios y escrituras

    @Test
    fun un_aviso_tardio_tras_parar_no_anuncia() {
        val anadidos = mutableListOf<String>()
        var anunciado = false
        val alta = AltaServicios(listOf("a", "b"), { anadidos += it }, { anunciado = true })
        alta.siguiente()
        alta.parar()
        // `onServiceAdded` llega tarde, tras `parar()`: ni añade el siguiente ni anuncia.
        alta.siguiente()
        alta.siguiente()
        assertEquals(listOf("a"), anadidos)
        assertFalse(anunciado)
    }

    @Test
    fun anuncia_al_terminar_el_ultimo_servicio() {
        val anadidos = mutableListOf<String>()
        var anunciado = 0
        val alta = AltaServicios(listOf("a", "b"), { anadidos += it }, { anunciado++ })
        alta.siguiente()
        alta.siguiente()
        assertEquals(0, anunciado)
        alta.siguiente()
        assertEquals(listOf("a", "b"), anadidos)
        assertEquals(1, anunciado)
    }

    private val ok = ResultadoGatt.Ok(listOf(n(1)))

    @Test
    fun una_escritura_preparada_no_se_admite_ni_se_atiende() {
        var atendida = false
        val r = resolverEscritura(true) {
            atendida = true
            ok
        }
        assertEquals(BluetoothGatt.GATT_REQUEST_NOT_SUPPORTED, r.estado)
        assertFalse(atendida)
    }

    @Test
    fun una_escritura_correcta_devuelve_sus_notificaciones() {
        val r = resolverEscritura(false) { ok }
        assertEquals(BluetoothGatt.GATT_SUCCESS, r.estado)
        assertEquals(1, r.notificaciones.size)
    }

    @Test
    fun un_error_del_servidor_no_esta_permitido() {
        val r = resolverEscritura(false) { ResultadoGatt.Error(ErrorGatt.OperacionNoAdmitida) }
        assertEquals(BluetoothGatt.GATT_WRITE_NOT_PERMITTED, r.estado)
    }

    @Test
    fun una_excepcion_es_un_fallo_anotado() {
        val r = resolverEscritura(false) { error("fallo") }
        assertEquals(BluetoothGatt.GATT_FAILURE, r.estado)
        assertTrue(r.error!!.contains("fallo"))
    }

    private fun n(b: Int) = NotificacionGatt(uuid16(0x0002), byteArrayOf(b.toByte()))
}
