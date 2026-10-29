package app.smartbike.protocolo

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class MandoZwiftCodecTest {

    private fun hex(b: ByteArray) = b.joinToString(" ") { "%02x".format(it) }
    private fun bytes(h: String) = h.split(" ").map { it.toInt(16).toByte() }.toByteArray()

    /** `ButtonMap` de una trama de botones sin palancas, ya desinvertido: los bits pulsados a 1. */
    private fun pulsadosEn(trama: ByteArray): Long {
        assertEquals(0x23, trama[0].toInt())
        assertEquals(0x08, trama[1].toInt())
        var v = 0L
        var desplazamiento = 0
        for (i in 2 until trama.size) {
            v = v or ((trama[i].toLong() and 0x7F) shl desplazamiento)
            desplazamiento += 7
            if (trama[i].toInt() and 0x80 == 0) {
                assertEquals(trama.size - 1, i, "bytes de más tras el varint")
                break
            }
        }
        return v.inv() and 0xFFFFFFFFL
    }

    @Test
    fun tabla_enum_manual() {
        val esperados = listOf(
            0x1, 0x2, 0x4, 0x8, 0x10, 0x20, 0x40, 0x80,
            0x100, 0x200, 0x400, 0x800, 0x1000, 0x2000, 0x4000, 0x8000,
        )
        assertEquals(esperados, BotonMando.entries.map(TablaBotones.ENUM_MANUAL::valor))
    }

    @Test
    fun ninguno_pulsado() {
        assertEquals("23 08 ff ff ff ff 0f", hex(MandoZwiftCodec.botones(emptySet(), TablaBotones.ENUM_MANUAL)))
    }

    @Test
    fun vectores_de_botones() {
        assertEquals("23 08 fe ff ff ff 0f", hex(MandoZwiftCodec.botones(setOf(BotonMando.IZQUIERDA), TablaBotones.ENUM_MANUAL)))
        assertEquals("23 08 ff fe ff ff 0f", hex(MandoZwiftCodec.botones(setOf(BotonMando.Z), TablaBotones.ENUM_MANUAL)))
    }

    @Test
    fun cada_boton_solo_en_las_dos_tablas() {
        for (tabla in TablaBotones.entries) {
            for (b in BotonMando.entries) {
                assertEquals(tabla.valor(b).toLong(), pulsadosEn(MandoZwiftCodec.botones(setOf(b), tabla)), "$tabla $b")
            }
        }
    }

    @Test
    fun izquierda_y_a_a_la_vez() {
        val trama = MandoZwiftCodec.botones(setOf(BotonMando.IZQUIERDA, BotonMando.A), TablaBotones.ENUM_MANUAL)
        assertEquals("23 08 ee ff ff ff 0f", hex(trama))
    }

    @Test
    fun todos_pulsados() {
        for (tabla in TablaBotones.entries) {
            val mascara = BotonMando.entries.fold(0L) { m, b -> m or tabla.valor(b).toLong() }
            assertEquals(mascara, pulsadosEn(MandoZwiftCodec.botones(BotonMando.entries.toSet(), tabla)))
        }
    }

    private val reposo = "23 08 ff ff ff ff 0f"

    @Test
    fun una_palanca_en_analog_paddles() {
        val t = MandoZwiftCodec.botones(emptySet(), TablaBotones.ENUM_MANUAL, mapOf(1 to 100))
        assertEquals("$reposo 1a 05 08 01 10 c8 01", hex(t))
    }

    @Test
    fun las_palancas_van_en_orden_de_ubicacion() {
        val t = MandoZwiftCodec.botones(emptySet(), TablaBotones.ENUM_MANUAL, mapOf(1 to -100, 0 to 100))
        assertEquals("$reposo 1a 05 08 00 10 c8 01 1a 05 08 01 10 c7 01", hex(t))
    }

    @Test
    fun palanca_con_un_boton_pulsado() {
        val t = MandoZwiftCodec.botones(setOf(BotonMando.IZQUIERDA), TablaBotones.ENUM_MANUAL, mapOf(1 to 100))
        assertEquals("23 08 fe ff ff ff 0f 1a 05 08 01 10 c8 01", hex(t))
    }

    @Test
    fun valores_de_palanca_en_sint32() {
        assertEquals("$reposo 1a 04 08 00 10 00 1a 04 08 01 10 00", hex(MandoZwiftCodec.botones(emptySet(), TablaBotones.ENUM_MANUAL, mapOf(0 to 0, 1 to 0))))
        assertEquals("$reposo 1a 05 08 03 10 c7 01", hex(MandoZwiftCodec.botones(emptySet(), TablaBotones.ENUM_MANUAL, mapOf(3 to -100))))
        assertEquals("$reposo 1a 04 08 01 10 01", hex(MandoZwiftCodec.botones(emptySet(), TablaBotones.ENUM_MANUAL, mapOf(1 to -1))))
        assertEquals(
            "$reposo 1a 08 08 01 10 ff ff ff ff 0f",
            hex(MandoZwiftCodec.botones(emptySet(), TablaBotones.ENUM_MANUAL, mapOf(1 to Int.MIN_VALUE))),
        )
    }

    @Test
    fun sin_palancas_solo_los_botones() {
        assertEquals(reposo, hex(MandoZwiftCodec.botones(emptySet(), TablaBotones.ENUM_MANUAL)))
    }

    @Test
    fun handshake() {
        assertTrue(MandoZwiftCodec.esHandshake(bytes("52 69 64 65 4f 6e")))
        assertTrue(MandoZwiftCodec.esHandshake(bytes("52 69 64 65 4f 6e 02")))
        assertTrue(MandoZwiftCodec.esHandshake(bytes("52 69 64 65 4f 6e 02 00")))
        assertFalse(MandoZwiftCodec.esHandshake(bytes("52 69 64 65 4f")))
        assertFalse(MandoZwiftCodec.esHandshake(bytes("00 08 00")))
        assertFalse(MandoZwiftCodec.esHandshake(ByteArray(0)))
    }

    @Test
    fun respuesta_al_handshake() {
        assertEquals("52 69 64 65 4f 6e", hex(MandoZwiftCodec.respuestaHandshake(RespuestaHandshake.PELADO)))
    }

    @Test
    fun peticiones_de_informacion() {
        assertEquals(0L, MandoZwiftCodec.peticionInfo(bytes("00 08 00")))
        assertEquals(16L, MandoZwiftCodec.peticionInfo(bytes("00 08 10")))
        assertEquals(770L, MandoZwiftCodec.peticionInfo(bytes("00 08 82 06")))
        assertEquals(null, MandoZwiftCodec.peticionInfo(bytes("41 08 05")))
        assertEquals(null, MandoZwiftCodec.peticionInfo(bytes("00 08")))
        assertEquals(null, MandoZwiftCodec.peticionInfo(bytes("00 08 82")))
        assertEquals(null, MandoZwiftCodec.peticionInfo(bytes("00 08 00 00")))
        assertEquals(null, MandoZwiftCodec.peticionInfo(MandoZwiftCodec.RIDE_ON))
    }

    @Test
    fun respuestas_de_informacion() {
        assertEquals("3c 08 00", hex((MandoZwiftCodec.respuestaInfo(RespuestaInfo.VACIA, 0))))
        assertEquals("3c 08 82 06", hex((MandoZwiftCodec.respuestaInfo(RespuestaInfo.VACIA, 770))))
    }

    @Test
    fun pagina_de_dispositivo_de_un_ride() {
        val r = hex((MandoZwiftCodec.respuestaInfo(RespuestaInfo.PAGINA_RIDE, 0)))
        val ascii = { t: String -> t.toByteArray(Charsets.US_ASCII).joinToString(" ") { "%02x".format(it) } }
        val pagina = listOf(
            "08 83 04", // protocolo 515
            "12 04 00 00 01 01", // firmware
            "1a 09 ${ascii("Zwift SF2")}",
            "32 0f ${ascii("08-0253514D4102")}",
            "3a 03 ${ascii("A.0")}",
            "42 04 08 02 10 01", // capacidades: tipo 2, 1
            "48 01", // fabricante
            "50 08", // producto: Ride izquierdo
            "5a 0c ${ascii("0253514D4102")}",
        ).joinToString(" ")
        val largo = pagina.split(" ").size
        assertEquals("3c 08 00 12 ${"%02x".format(largo + 2)} 0a ${"%02x".format(largo)} $pagina", r)
    }

    @Test
    fun configuracion_cliente_servidor_y_otras_paginas() {
        for (v in listOf(RespuestaInfo.PAGINA_RIDE)) {
            assertEquals("3c 08 10 12 02 08 00", hex((MandoZwiftCodec.respuestaInfo(v, 16))))
            assertEquals("3c 08 07", hex((MandoZwiftCodec.respuestaInfo(v, 7))))
        }
    }

    @Test
    fun pagina_1024_observada() {
        for (v in listOf(RespuestaInfo.PAGINA_RIDE)) {
            assertEquals("3c 08 80 08 12 07 0a 05 08 ff 23 10 00", hex((MandoZwiftCodec.respuestaInfo(v, 1024))))
        }
    }

    @Test
    fun pagina_771_observada() {
        for (v in listOf(RespuestaInfo.PAGINA_RIDE)) {
            assertEquals(
                "3c 08 83 06 12 0a 0a 08 08 00 10 64 18 00 20 00",
                hex((MandoZwiftCodec.respuestaInfo(v, 771))),
            )
        }
    }
}
