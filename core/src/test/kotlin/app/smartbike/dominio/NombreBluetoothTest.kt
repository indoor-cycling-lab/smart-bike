package app.smartbike.dominio

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/** `NombreBluetooth.deseado` (spec `bluetooth-name`, «Renombrar el móvil mientras publica por Bluetooth»). */
class NombreBluetoothTest {
    private val identidad = IdentidadDispositivo(mac = "AA:BB:CC:DD:66:9C", serie = "1")

    private fun config(modo: ModoConexion, activado: Boolean = true, nombre: Identidad = Identidad.SMART_BIKE) = Configuracion(conexion = AjustesConexion(modo, nombre, nombrePorBluetooth = activado))

    private fun servicios(modo: ModoConexion, mando: MandoPublicado?) = ServiciosPublicados(modo, mando, pulso = false)

    @Test
    fun sin_el_ajuste_no_hay_nombre() {
        val c = config(ModoConexion.FTMS_BLUETOOTH, activado = false)
        assertEquals(null, NombreBluetooth.deseado(c, servicios(ModoConexion.FTMS_BLUETOOTH, MandoPublicado.DENTRO), identidad))
    }

    @Test
    fun sin_publicacion_o_sin_identidad_no_hay_nombre() {
        val c = config(ModoConexion.FTMS_BLUETOOTH)
        assertEquals(null, NombreBluetooth.deseado(c, null, identidad))
        assertEquals(null, NombreBluetooth.deseado(c, servicios(ModoConexion.FTMS_BLUETOOTH, null), null))
    }

    @Test
    fun solo_wifi_no_publica_nada_por_bluetooth() {
        val c = config(ModoConexion.ZWIFT_RIDE)
        assertEquals(null, NombreBluetooth.deseado(c, servicios(ModoConexion.ZWIFT_RIDE, MandoPublicado.DENTRO), identidad))
    }

    @Test
    fun wifi_y_bluetooth_sin_mando_no_hay_nombre() {
        val c = config(ModoConexion.FTMS_WIFI)
        assertEquals(null, NombreBluetooth.deseado(c, servicios(ModoConexion.FTMS_WIFI, null), identidad))
    }

    @Test
    fun solo_bluetooth_usa_el_nombre() {
        val c = config(ModoConexion.FTMS_BLUETOOTH)
        assertEquals("Smart Bike", NombreBluetooth.deseado(c, servicios(ModoConexion.FTMS_BLUETOOTH, null), identidad))
    }

    @Test
    fun wifi_y_bluetooth_con_el_mando_aparte_usa_el_nombre() {
        val c = config(ModoConexion.FTMS_WIFI)
        assertEquals("Smart Bike", NombreBluetooth.deseado(c, servicios(ModoConexion.FTMS_WIFI, MandoPublicado.APARTE_BLUETOOTH), identidad))
    }

    @Test
    fun kickr_bike_pro_lleva_el_sufijo_de_su_identidad() {
        val c = config(ModoConexion.FTMS_BLUETOOTH, nombre = Identidad.KICKR_BIKE_PRO)
        assertEquals("KICKR BIKE PRO 669C", NombreBluetooth.deseado(c, servicios(ModoConexion.FTMS_BLUETOOTH, null), identidad))
    }
}
