package app.smartbike.protocolo

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class ConsultaMdnsTest {

    private fun bytes(h: String) = h.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
    private fun hex(b: ByteArray) = b.joinToString("") { "%02x".format(it) }

    // Las respuestas de abajo son capturas reales de cuando la app se anunciaba como «Square Shift»: el nombre va dentro de
    // los bytes. Al analizador le da igual cómo se llame la instancia, así que se conserva el de la captura.
    private val instancia = "Square Shift"
    private val tipo = "_wahoo-fitness-tnp._tcp"

    /** Respuesta del OnePlus 8 a la consulta SRV QU (27-10-2026): el SRV con el puerto 36866 y los A, AAAA y NSEC. */
    private val respuestaSrv = bytes(
        "0000840000000001000000030c537175617265205368696674125f7761686f6f2d6669746e6573732d746e70045f746370056c6f" +
            "63616c000021800100000078001900000000900210416e64726f69645f524c36395a30574ec031c04800018001000000780004" +
            "c0a80115c048001c8001000000780010fe8000000000000060ccd2fffe38da96c048002f8001000000780008c04800044000" +
            "0008",
    )

    /** Respuesta del OnePlus 8 a la consulta PTR QU del tipo: el SRV va en los adicionales, con nombres comprimidos. */
    private val respuestaPtr = bytes(
        "000084000000000100000006125f7761686f6f2d6669746e6573732d746e70045f746370056c6f63616c00000c00010000119400" +
            "0f0c537175617265205368696674c00c10416e64726f69645f524c36395a30574ec02400018001000000780004c0a80115c035" +
            "0010800100001194005e1873657269616c2d6e756d6265723d343132353035333836371d6d61632d616464726573733d43363a" +
            "44443a44323a36323a46413a304426626c652d736572766963652d75756964733d3078313832362c3078313831382c307831" +
            "383041c035002f8001000000780009c03500050000800040c044001c8001000000780010fe8000000000000060ccd2fffe38da" +
            "96c044002f8001000000780008c044000440000008c03500218001000000780008000000009002c044",
    )

    @Test
    fun consulta_srv_con_bit_qu() {
        assertEquals(
            "000000000001000000000000" +
                "0c537175617265205368696674125f7761686f6f2d6669746e6573732d746e70045f746370056c6f63616c00" +
                "0021" + "8001",
            hex(ConsultaMdns.consultaSrv(instancia, tipo)),
        )
    }

    @Test
    fun puerto_de_la_respuesta_srv() {
        assertEquals(36866, ConsultaMdns.puertoSrv(respuestaSrv, instancia, tipo))
    }

    @Test
    fun puerto_en_los_adicionales_con_nombres_comprimidos() {
        assertEquals(36866, ConsultaMdns.puertoSrv(respuestaPtr, instancia, tipo))
    }

    @Test
    fun sin_distinguir_mayusculas() {
        assertEquals(36866, ConsultaMdns.puertoSrv(respuestaSrv, "SQUARE SHIFT", "_Wahoo-Fitness-TNP._TCP"))
    }

    @Test
    fun otra_instancia_no_cuenta() {
        assertNull(ConsultaMdns.puertoSrv(respuestaSrv, "Otro rodillo", tipo))
    }

    @Test
    fun una_consulta_no_es_una_respuesta() {
        val consulta = ConsultaMdns.consultaSrv(instancia, tipo)
        assertNull(ConsultaMdns.puertoSrv(consulta, instancia, tipo))
    }

    @Test
    fun respuesta_truncada() {
        // El SRV ocupa los primeros 91 bytes; cortado dentro de él no cuenta.
        for (n in listOf(0, 5, 12, 40, 80, 90)) {
            assertNull(ConsultaMdns.puertoSrv(respuestaSrv.copyOf(n), instancia, tipo), "truncada a $n bytes")
        }
    }

    @Test
    fun bucle_de_compresion() {
        // Cabecera de respuesta con un registro cuyo nombre apunta a sí mismo.
        val p = bytes("000084000000000100000000" + "c00c" + "0021000100000078000600000000" + "9002")
        assertNull(ConsultaMdns.puertoSrv(p, instancia, tipo))
    }
}
