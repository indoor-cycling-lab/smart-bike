package app.smartbike.protocolo

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SimulacionZwiftCodecTest {
    private fun bytes(h: String) = h.split(" ").map { it.toInt(16).toByte() }.toByteArray()

    // Tramas de la prueba del change `mando-zwift-emulado` (27-10-2026, Zwift en PC).
    private val ajustes = bytes("3f 08 01 10 00 22 0c 40 ec 27 50 d3 06 58 9c 4a 60 90 03")
    private val pendienteMenos164 = bytes("3f 08 01 10 01 22 05 28 c7 02 38 00")
    private val relacion16800 = bytes("3f 08 01 10 02 22 04 38 a0 83 01")
    private val pendienteMenos153 = bytes("3f 08 00 10 03 22 03 28 b1 02")
    private val pendienteMenos101 = bytes("3f 08 00 10 04 22 03 28 c9 01")

    @Test
    fun ajustes_de_conexion() {
        val set = SimulacionZwiftCodec.decodificar(ajustes)!!
        assertEquals(1, set.opciones)
        assertEquals(0, set.idMensaje)
        val s = set.simulacion!!
        assertEquals(0.51, s.cw!!, 1e-9)
        assertEquals(8.51, s.masaBiciKg!!, 1e-9)
        assertEquals(95.0, s.masaCiclistaKg!!, 1e-9)
        assertEquals(0.004, s.crr!!, 1e-9)
        assertNull(s.pendientePct)
    }

    @Test
    fun pendiente_con_relacion_cero() {
        val set = SimulacionZwiftCodec.decodificar(pendienteMenos164)!!
        assertEquals(1, set.idMensaje)
        assertEquals(-1.64, set.simulacion!!.pendientePct!!, 1e-9)
        assertEquals(0.0, set.simulacion!!.relacionVirtual!!, 1e-9)
    }

    @Test
    fun relacion_virtual_sola() {
        val s = SimulacionZwiftCodec.decodificar(relacion16800)!!.simulacion!!
        assertEquals(1.68, s.relacionVirtual!!, 1e-9)
        assertNull(s.pendientePct)
    }

    @Test
    fun pendientes_sueltas() {
        val a = SimulacionZwiftCodec.decodificar(pendienteMenos153)!!
        assertEquals(0, a.opciones)
        assertEquals(3, a.idMensaje)
        assertEquals(-1.53, a.simulacion!!.pendientePct!!, 1e-9)
        assertEquals(-1.01, SimulacionZwiftCodec.decodificar(pendienteMenos101)!!.simulacion!!.pendientePct!!, 1e-9)
    }

    @Test
    fun pendiente_positiva() {
        // 4,00 % = 400 → zigzag 800 → varint a0 06.
        val s = SimulacionZwiftCodec.decodificar(bytes("3f 08 00 10 05 22 03 28 a0 06"))!!.simulacion!!
        assertEquals(4.0, s.pendientePct!!, 1e-9)
    }

    @Test
    fun trama_truncada_da_null() {
        assertNull(SimulacionZwiftCodec.decodificar(ajustes.copyOf(ajustes.size - 3)))
        assertNull(SimulacionZwiftCodec.decodificar(bytes("3f 08 01 10 00 22 0c 40")))
        assertNull(SimulacionZwiftCodec.decodificar(bytes("3f 08")))
    }

    @Test
    fun campo_desconocido_se_salta() {
        // Campo 20 (varint) y campo 21 (con longitud) antes de la pendiente.
        val s = SimulacionZwiftCodec.decodificar(bytes("3f 08 01 10 00 22 0a a0 01 07 aa 01 01 ff 28 c7 02"))!!.simulacion!!
        assertEquals(-1.64, s.pendientePct!!, 1e-9)
    }

    @Test
    fun no_es_un_set() {
        assertFalse(SimulacionZwiftCodec.esSet(bytes("52 69 64 65 4f 6e")))
        assertNull(SimulacionZwiftCodec.decodificar(bytes("23 08 ff")))
        assertTrue(SimulacionZwiftCodec.esSet(ajustes))
    }

    @Test
    fun set_sin_cuerpo() {
        val set = SimulacionZwiftCodec.decodificar(bytes("3f 08 01 10 07"))!!
        assertNotNull(set)
        assertNull(set.simulacion)
        assertEquals(7, set.idMensaje)
    }

    @Test
    fun respuesta_con_estado_y_msgId() {
        val set = SimulacionZwiftCodec.decodificar(pendienteMenos164)!!
        assertArrayEquals(bytes("40 08 00 10 01"), SimulacionZwiftCodec.respuesta(set))
        assertArrayEquals(bytes("40 08 00 10 ac 02"), SimulacionZwiftCodec.respuesta(SetZwift(1, 300, null)))
    }
}
