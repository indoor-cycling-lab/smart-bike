package app.smartbike.datos

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class RegistroSesionTest {

    private var t = 1000L
    private val espejo = mutableListOf<String>()
    private fun registro(capacidad: Int = 10) = RegistroSesion(capacidad, relojMonotonico = { t++ }, relojPared = { 0L }, espejo = { espejo += it })

    @Test
    fun linea_de_trama() {
        val r = registro()
        r.trama(Origen.Square, Sentido.Rx, "0x43", byteArrayOf(0x0A, 0x1B, 0x2C, 0x80.toByte(), 0x02, 0, 0, -1))
        assertEquals("1000 SQUARE RX 0x43 0a1b2c80020000ff", espejo.single())
    }

    @Test
    fun linea_de_suceso() {
        val r = registro()
        r.suceso(Origen.Neo, "Conectado")
        assertEquals("1000 NEO EVT - Conectado", espejo.single())
    }

    @Test
    fun la_mas_reciente_primero() {
        val r = registro()
        repeat(3) { r.suceso(Origen.App, "e$it") }
        assertEquals(listOf("e2", "e1"), r.ultimas(2).map { it.texto })
    }

    @Test
    fun descarta_las_mas_antiguas_al_llenarse() {
        val r = registro(capacidad = 10)
        repeat(25) { r.suceso(Origen.App, "e$it") }
        assertEquals(10, r.tamano)
        assertEquals("e24", r.ultimas(10).first().texto)
        assertEquals("e15", r.ultimas(10).last().texto)
        assertEquals(25, espejo.size)
    }

    @Test
    fun capacidad_por_defecto() {
        val r = RegistroSesion(relojMonotonico = { 0L }, relojPared = { 0L })
        repeat(RegistroSesion.CAPACIDAD + 5) { r.suceso(Origen.App, "") }
        assertEquals(10_000, r.tamano)
    }

    @Test
    fun cada_entrada_cuenta_como_cambio() {
        val r = registro()
        repeat(4) { r.suceso(Origen.App, "") }
        assertEquals(4L, r.cambios.value)
    }

    @Test
    fun copia_los_bytes() {
        val r = registro()
        val datos = byteArrayOf(1, 2, 3)
        r.trama(Origen.Neo, Sentido.Rx, "2a63", datos)
        datos[0] = 9
        assertEquals(1, r.ultimas(1).single().datos[0].toInt())
    }

    @Test
    fun el_sumidero_recibe_cada_suceso_una_vez_y_ninguna_trama() {
        val r = registro()
        val recibidos = mutableListOf<String>()
        r.sumidero = { recibidos += it.texto }
        r.suceso(Origen.Neo, "Conectado")
        r.trama(Origen.Square, Sentido.Rx, "0x43", byteArrayOf(1))
        r.trama(Origen.Neo, Sentido.Tx, "fec3", byteArrayOf(2))
        r.suceso(Origen.App, "Marcha 5")
        assertEquals(listOf("Conectado", "Marcha 5"), recibidos)
    }

    @Test
    fun sin_sumidero_no_pasa_nada_y_el_registro_en_memoria_no_cambia() {
        val r = registro()
        r.suceso(Origen.Neo, "a")
        val recibidos = mutableListOf<String>()
        r.sumidero = { recibidos += it.texto }
        r.sumidero = null
        r.suceso(Origen.Neo, "b")
        assertEquals(emptyList<String>(), recibidos)
        assertEquals(listOf("b", "a"), r.ultimas(10).map { it.texto })
        assertEquals(2, espejo.size)
    }

    @Test
    fun el_sumidero_no_altera_el_buffer_ni_el_espejo() {
        val r = registro(capacidad = 2)
        r.sumidero = { }
        repeat(3) { r.suceso(Origen.App, "e$it") }
        assertEquals(listOf("e2", "e1"), r.ultimas(10).map { it.texto })
        assertEquals(3, espejo.size)
    }
}
