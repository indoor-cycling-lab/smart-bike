package app.smartbike.ble

import app.smartbike.datos.AlmacenEstado
import app.smartbike.datos.RegistroSesion
import app.smartbike.dominio.EstadoGuardado
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/** `RenombradorBluetooth` (spec `bluetooth-name`) con un adaptador falso. */
class RenombradorBluetoothTest {
    private class AdaptadorFalso(nombre: String? = "OnePlus 8") : NombreDelAdaptador {
        var real: String? = nombre
        override val confirmado = MutableStateFlow(nombre)
        val llamadas = mutableListOf<String>()
        var acepta = true
        var confirma = true
        var sinPermiso = false

        override fun actual() = real

        override fun poner(nombre: String): Boolean {
            llamadas += nombre
            if (sinPermiso) throw SecurityException()
            if (!acepta) return false
            if (confirma) {
                real = nombre
                confirmado.value = nombre
            }
            return true
        }
    }

    private class AlmacenFalso(var guardado: EstadoGuardado = EstadoGuardado()) : AlmacenEstado {
        override suspend fun leer() = guardado
        override suspend fun actualizar(cambio: (EstadoGuardado) -> EstadoGuardado) {
            guardado = cambio(guardado)
        }
    }

    private val registro = RegistroSesion(relojMonotonico = { 0 }, relojPared = { 0 })
    private val adaptador = AdaptadorFalso()
    private val almacen = AlmacenFalso()
    private val renombrador = RenombradorBluetooth(adaptador, almacen, registro)

    @Test
    fun renombra_y_guarda_el_original() = runTest {
        assertTrue(renombrador.aplicar("Smart Bike"))
        assertEquals("Smart Bike", adaptador.real)
        assertEquals("OnePlus 8", almacen.guardado.nombreBluetoothOriginal)
        assertEquals("Smart Bike", almacen.guardado.nombreBluetoothPuesto)
        assertEquals(EstadoNombre.Renombrado("Smart Bike", "OnePlus 8"), renombrador.estado.value)
    }

    @Test
    fun no_repite_si_ya_se_llama_asi() = runTest {
        renombrador.aplicar("Smart Bike")
        assertFalse(renombrador.aplicar("Smart Bike"))
        assertEquals(listOf("Smart Bike"), adaptador.llamadas)
    }

    @Test
    fun recupera_el_original_y_borra_los_campos() = runTest {
        renombrador.aplicar("Smart Bike")
        assertTrue(renombrador.aplicar(null))
        assertEquals("OnePlus 8", adaptador.real)
        assertEquals(null, almacen.guardado.nombreBluetoothOriginal)
        assertEquals(null, almacen.guardado.nombreBluetoothPuesto)
        assertEquals(EstadoNombre.Propio, renombrador.estado.value)
    }

    @Test
    fun cambiar_de_nombre_conserva_el_original_del_movil() = runTest {
        renombrador.aplicar("Smart Bike")
        renombrador.aplicar("KICKR BIKE PRO 669C")
        assertEquals("OnePlus 8", almacen.guardado.nombreBluetoothOriginal)
        renombrador.aplicar(null)
        assertEquals("OnePlus 8", adaptador.real)
    }

    @Test
    fun no_toca_un_nombre_cambiado_por_el_usuario() = runTest {
        renombrador.aplicar("Smart Bike")
        adaptador.real = "Bici"
        assertFalse(renombrador.aplicar(null))
        assertEquals("Bici", adaptador.real)
        assertEquals(listOf("Smart Bike"), adaptador.llamadas)
        assertEquals(null, almacen.guardado.nombreBluetoothOriginal)
    }

    @Test
    fun recupera_al_arrancar_lo_que_quedo_pendiente_y_sin_pendiente_no_llama_a_poner() = runTest {
        almacen.guardado = EstadoGuardado(nombreBluetoothOriginal = "OnePlus 8", nombreBluetoothPuesto = "Smart Bike")
        adaptador.real = "Smart Bike"
        renombrador.aplicar(null)
        assertEquals("OnePlus 8", adaptador.real)
        assertEquals(listOf("OnePlus 8"), adaptador.llamadas)
        renombrador.aplicar(null)
        assertEquals(listOf("OnePlus 8"), adaptador.llamadas)
    }

    @Test
    fun no_se_pudo_si_android_lo_rechaza() = runTest {
        adaptador.acepta = false
        assertFalse(renombrador.aplicar("Smart Bike"))
        assertTrue(renombrador.estado.value is EstadoNombre.NoSePudo)
        assertEquals(null, almacen.guardado.nombreBluetoothOriginal)
    }

    @Test
    fun no_se_pudo_si_salta_security_exception() = runTest {
        adaptador.sinPermiso = true
        assertFalse(renombrador.aplicar("Smart Bike"))
        assertTrue(renombrador.estado.value is EstadoNombre.NoSePudo)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun no_se_pudo_si_no_llega_la_confirmacion() = runTest {
        adaptador.confirma = false
        val trabajo = launch { renombrador.aplicar("Smart Bike") }
        advanceUntilIdle()
        assertTrue(trabajo.isCompleted)
        assertTrue(renombrador.estado.value is EstadoNombre.NoSePudo)
        // Se conserva el original por si el cambio llega más tarde.
        assertEquals("OnePlus 8", almacen.guardado.nombreBluetoothOriginal)
    }
}
