package app.smartbike.protocolo

import app.smartbike.protocolo.DirconCodec.Peticion
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class DirconCodecTest {

    private fun hex(b: ByteArray) = b.joinToString(" ") { "%02x".format(it) }
    private fun bytes(h: String) = h.split(" ").filter { it.isNotEmpty() }.map { it.toInt(16).toByte() }.toByteArray()

    private val ftms = DirconCodec.uuid16(0x1826)
    private val puntoControl = DirconCodec.uuid16(0x2AD9)
    private val hexFtms = "00 00 18 26 00 00 10 00 80 00 00 80 5f 9b 34 fb"
    private val hexPuntoControl = "00 00 2a d9 00 00 10 00 80 00 00 80 5f 9b 34 fb"

    @Test
    fun uuid_corto_en_128_bits() {
        assertEquals("00001826-0000-1000-8000-00805f9b34fb", ftms.toString())
        assertEquals(hexFtms, hex(DirconCodec.bytes(ftms)))
        assertEquals("2ad9", DirconCodec.nombreCorto(puntoControl))
        assertEquals(
            "6e40fec1-b5a3-f393-e0a9-e50e24dcca9e",
            DirconCodec.nombreCorto(java.util.UUID.fromString("6e40fec1-b5a3-f393-e0a9-e50e24dcca9e")),
        )
    }

    @Test
    fun cabecera_con_longitud_big_endian() {
        val m = DirconCodec.respuestaServicios(7, listOf(ftms))
        assertEquals("01 01 07 00 00 10 $hexFtms", hex(DirconCodec.codificar(m)))
    }

    @Test
    fun descubrir_servicios() {
        val p = DirconCodec.peticion(DirconCodec.Lector().anadir(bytes("01 01 03 00 00 00")).single())
        assertEquals(Peticion.DescubrirServicios(3), p)
    }

    @Test
    fun descubrir_caracteristicas_y_respuesta() {
        val p = DirconCodec.peticion(DirconCodec.Lector().anadir(bytes("01 02 04 00 00 10 $hexFtms")).single())
        assertEquals(Peticion.DescubrirCaracteristicas(4, ftms), p)
        val r = DirconCodec.respuestaCaracteristicas(
            4,
            ftms,
            listOf(DirconCodec.Caracteristica(puntoControl, DirconCodec.PROP_ESCRIBIR or DirconCodec.PROP_NOTIFICAR)),
        )
        assertEquals("01 02 04 00 00 21 $hexFtms $hexPuntoControl 06", hex(DirconCodec.codificar(r)))
    }

    @Test
    fun leer_y_respuesta() {
        val p = DirconCodec.peticion(DirconCodec.Lector().anadir(bytes("01 03 05 00 00 10 $hexPuntoControl")).single())
        assertEquals(Peticion.Leer(5, puntoControl), p)
        val r = DirconCodec.respuestaLectura(5, puntoControl, bytes("00 01"))
        assertEquals("01 03 05 00 00 12 $hexPuntoControl 00 01", hex(DirconCodec.codificar(r)))
    }

    @Test
    fun escribir_el_punto_de_control() {
        val p = DirconCodec.peticion(
            DirconCodec.Lector().anadir(bytes("01 04 06 00 00 17 $hexPuntoControl 11 00 00 90 01 28 33")).single(),
        )
        p as Peticion.Escribir
        assertEquals(6, p.secuencia)
        assertEquals(puntoControl, p.caracteristica)
        assertEquals("11 00 00 90 01 28 33", hex(p.valor))
        assertEquals("01 04 06 00 00 10 $hexPuntoControl", hex(DirconCodec.codificar(DirconCodec.respuestaEscritura(6, puntoControl))))
    }

    @Test
    fun suscribir_y_desuscribir() {
        val l = DirconCodec.Lector()
        assertEquals(
            Peticion.Suscribir(8, puntoControl, true),
            DirconCodec.peticion(l.anadir(bytes("01 05 08 00 00 11 $hexPuntoControl 01")).single()),
        )
        assertEquals(
            Peticion.Suscribir(9, puntoControl, false),
            DirconCodec.peticion(l.anadir(bytes("01 05 09 00 00 11 $hexPuntoControl 00")).single()),
        )
    }

    @Test
    fun notificacion_del_servidor() {
        val n = DirconCodec.notificacion(puntoControl, bytes("80 11 01"))
        assertEquals("01 06 00 00 00 13 $hexPuntoControl 80 11 01", hex(DirconCodec.codificar(n)))
    }

    @Test
    fun tipo_desconocido_y_contenido_corto() {
        val l = DirconCodec.Lector()
        assertEquals(
            Peticion.Desconocida(1, 0x42, DirconCodec.MENSAJE_DESCONOCIDO),
            DirconCodec.peticion(l.anadir(bytes("01 42 01 00 00 00")).single()),
        )
        assertEquals(
            Peticion.Desconocida(2, DirconCodec.LEER, DirconCodec.ERROR_INESPERADO),
            DirconCodec.peticion(l.anadir(bytes("01 03 02 00 00 02 00 00")).single()),
        )
        assertEquals(
            "01 03 02 04 00 00",
            hex(DirconCodec.codificar(DirconCodec.error(DirconCodec.LEER, 2, DirconCodec.CARACTERISTICA_NO_ENCONTRADA))),
        )
    }

    @Test
    fun cabecera_incompleta_espera_al_resto() {
        val l = DirconCodec.Lector()
        val todo = bytes("01 03 05 00 00 10 $hexPuntoControl")
        assertTrue(l.anadir(todo.copyOfRange(0, 4)).isEmpty())
        assertTrue(l.anadir(todo.copyOfRange(4, 12)).isEmpty())
        assertEquals(Peticion.Leer(5, puntoControl), DirconCodec.peticion(l.anadir(todo.copyOfRange(12, todo.size)).single()))
    }

    @Test
    fun dos_mensajes_en_un_bloque() {
        val l = DirconCodec.Lector()
        val m = l.anadir(bytes("01 01 01 00 00 00 01 03 02 00 00 10 $hexPuntoControl 01 01"))
        assertEquals(listOf(Peticion.DescubrirServicios(1), Peticion.Leer(2, puntoControl)), m.map(DirconCodec::peticion))
        // El resto (media cabecera) queda para el siguiente bloque.
        assertEquals(Peticion.DescubrirServicios(3), DirconCodec.peticion(l.anadir(bytes("03 00 00 00")).single()))
    }

    private val servidor = ServidorGatt(
        listOf(
            ServicioGatt(
                ftms,
                listOf(
                    CaracteristicaGatt(DirconCodec.uuid16(0x2ACC), setOf(PropiedadGatt.Leer), leer = { bytes("02 40 00 00") }),
                    CaracteristicaGatt(puntoControl, setOf(PropiedadGatt.Escribir, PropiedadGatt.Indicar), alEscribir = { v ->
                        listOf(NotificacionGatt(puntoControl, byteArrayOf(0x80.toByte(), v[0], 1)))
                    }),
                ),
            ),
        ),
    )

    private fun atender(h: String) = DirconCodec.atender(servidor, DirconCodec.peticion(DirconCodec.Lector().anadir(bytes(h)).single()))

    @Test
    fun atender_descubrimiento() {
        assertEquals("01 01 01 00 00 10 $hexFtms", hex(DirconCodec.codificar(atender("01 01 01 00 00 00").respuesta)))
        val c = atender("01 02 02 00 00 10 $hexFtms").respuesta
        assertEquals(
            "01 02 02 00 00 32 $hexFtms 00 00 2a cc 00 00 10 00 80 00 00 80 5f 9b 34 fb 01 $hexPuntoControl 06",
            hex(DirconCodec.codificar(c)),
        )
        assertEquals(DirconCodec.SERVICIO_NO_ENCONTRADO, atender("01 02 03 00 00 10 $hexPuntoControl").respuesta.codigo)
    }

    @Test
    fun atender_escritura_suscrita_notifica_despues() {
        assertTrue(atender("01 04 01 00 00 11 $hexPuntoControl 00").notificaciones.isEmpty())
        atender("01 05 02 00 00 11 $hexPuntoControl 01")
        val a = atender("01 04 03 00 00 11 $hexPuntoControl 07")
        assertEquals("01 04 03 00 00 10 $hexPuntoControl", hex(DirconCodec.codificar(a.respuesta)))
        val n = a.notificaciones.single()
        assertEquals(
            "01 06 00 00 00 13 $hexPuntoControl 80 07 01",
            hex(DirconCodec.codificar(DirconCodec.notificacion(n.caracteristica, n.valor))),
        )
    }

    @Test
    fun atender_errores() {
        assertEquals(DirconCodec.OPERACION_NO_ADMITIDA, atender("01 05 01 00 00 11 00 00 2a cc 00 00 10 00 80 00 00 80 5f 9b 34 fb 01").respuesta.codigo)
        assertEquals(DirconCodec.CARACTERISTICA_NO_ENCONTRADA, atender("01 03 02 00 00 10 $hexFtms").respuesta.codigo)
        assertEquals(DirconCodec.MENSAJE_DESCONOCIDO, atender("01 42 03 00 00 00").respuesta.codigo)
    }

    @Test
    fun mensaje_de_contenido_maximo_entregado_byte_a_byte() {
        val contenido = ByteArray(DirconCodec.CONTENIDO_MAX) { it.toByte() }
        val m = DirconCodec.Mensaje(tipo = 4, secuencia = 7, codigo = 0, contenido = contenido)
        val l = DirconCodec.Lector()
        val salida = mutableListOf<DirconCodec.Mensaje>()
        for (b in DirconCodec.codificar(m)) salida += l.anadir(byteArrayOf(b))
        assertEquals(1, salida.size)
        assertEquals(4, salida[0].tipo)
        assertTrue(contenido.contentEquals(salida[0].contenido))
    }

    @Test
    fun varios_mensajes_en_un_bloque_y_medio_mensaje_despues() {
        val a = bytes("01 03 05 00 00 10 $hexPuntoControl")
        val b = bytes("01 03 06 00 00 10 $hexPuntoControl")
        val l = DirconCodec.Lector()
        assertEquals(2, l.anadir(a + b).size)
        assertTrue(l.anadir(a + b.copyOfRange(0, 3), a.size + 3).size == 1)
        assertEquals(6, l.anadir(b.copyOfRange(3, b.size)).single().secuencia)
    }
}
