package app.smartbike.ble

import app.smartbike.protocolo.DirconCodec.uuid16
import app.smartbike.protocolo.NotificacionGatt
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/** Notificaciones en serie, una cola por dispositivo (design.md del change `endurecer-dircon-ble`, decisión 5). */
@OptIn(ExperimentalCoroutinesApi::class)
class ColaNotificacionesTest {
    private val car = uuid16(0x0002)
    private fun n(b: Int) = NotificacionGatt(car, byteArrayOf(b.toByte()))

    @Test
    fun cada_notificacion_espera_a_la_confirmacion_de_la_anterior() = runTest {
        val enviadas = mutableListOf<Pair<String, Int>>()
        val salidas = mutableListOf<Int>()
        val cola = ColaNotificaciones(
            backgroundScope,
            destinos = { listOf("zwift") },
            suscrito = { _, _ -> true },
            enviar = { d, x ->
                enviadas += d to x.valor[0].toInt()
                true
            },
            alSalir = { salidas += it.valor[0].toInt() },
        )
        cola.encolar(n(1))
        cola.encolar(n(2))
        runCurrent()
        assertEquals(listOf("zwift" to 1), enviadas)
        cola.confirmar("zwift")
        runCurrent()
        assertEquals(listOf("zwift" to 1, "zwift" to 2), enviadas)
        assertEquals(listOf(1), salidas)
        cola.confirmar("zwift")
        runCurrent()
        assertEquals(listOf(1, 2), salidas)
    }

    @Test
    fun sin_confirmacion_sigue_tras_la_espera() = runTest {
        val enviadas = mutableListOf<Int>()
        val cola = ColaNotificaciones(backgroundScope, { listOf("zwift") }, { _, _ -> true }, { _, x ->
            enviadas += x.valor[0].toInt()
            true
        }, {})
        cola.encolar(n(1))
        cola.encolar(n(2))
        runCurrent()
        assertEquals(listOf(1), enviadas)
        advanceTimeBy(ColaNotificaciones.ESPERA_ENVIO_MS + 1)
        runCurrent()
        assertEquals(listOf(1, 2), enviadas)
    }

    @Test
    fun un_destino_que_nunca_confirma_no_retrasa_a_otro() = runTest {
        val enviadas = mutableListOf<Pair<String, Int>>()
        val cola = ColaNotificaciones(backgroundScope, { listOf("lento", "rapido") }, { _, _ -> true }, { d, x ->
            enviadas += d to x.valor[0].toInt()
            true
        }, {})
        cola.encolar(n(1))
        cola.encolar(n(2))
        runCurrent()
        cola.confirmar("rapido")
        runCurrent()
        // El rápido va por la segunda sin esperar los 500 ms del lento.
        assertTrue(("lento" to 1) in enviadas && ("rapido" to 1) in enviadas)
        assertTrue(("rapido" to 2) in enviadas)
        assertTrue(("lento" to 2) !in enviadas)
    }

    @Test
    fun un_dispositivo_sin_suscripcion_no_recibe_nada() = runTest {
        val enviadas = mutableListOf<String>()
        val cola = ColaNotificaciones(backgroundScope, { listOf("a", "b") }, { d, _ -> d == "a" }, { d, _ ->
            enviadas += d
            true
        }, {})
        cola.encolar(n(1))
        runCurrent()
        assertEquals(listOf("a"), enviadas)
    }

    @Test
    fun sin_clientes_o_rechazada_no_sale() = runTest {
        val salidas = mutableListOf<Int>()
        var destinos = emptyList<String>()
        val cola = ColaNotificaciones(backgroundScope, { destinos }, { _, _ -> true }, { _, _ -> false }, { salidas += it.valor[0].toInt() })
        cola.encolar(n(1))
        runCurrent()
        destinos = listOf("zwift")
        cola.encolar(n(2))
        runCurrent()
        assertTrue(salidas.isEmpty())
    }

    @Test
    fun al_quitar_un_destino_su_cola_se_cierra() = runTest {
        val enviadas = mutableListOf<Int>()
        val cola = ColaNotificaciones(backgroundScope, { listOf("zwift") }, { _, _ -> true }, { _, x ->
            enviadas += x.valor[0].toInt()
            true
        }, {})
        cola.encolar(n(1))
        cola.encolar(n(2))
        runCurrent()
        cola.quitar("zwift")
        advanceTimeBy(ColaNotificaciones.ESPERA_ENVIO_MS + 1)
        runCurrent()
        // Lo pendiente (la 2) se descarta al quitar el destino.
        assertEquals(listOf(1), enviadas)
    }

    @Test
    fun cerrada_no_envia_mas() = runTest {
        val enviadas = mutableListOf<Int>()
        val cola = ColaNotificaciones(backgroundScope, { listOf("zwift") }, { _, _ -> true }, { _, x ->
            enviadas += x.valor[0].toInt()
            true
        }, {})
        cola.cerrar()
        cola.encolar(n(1))
        runCurrent()
        assertTrue(enviadas.isEmpty())
    }
}
