package app.smartbike.ui.componentes

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

/** La rejilla de Rodaje no se vuelve a medir con cada dato nuevo, solo cuando cambia algo que mueve la medida. */
class MetricGridTest {

    private fun potencia(valor: String?) = Dato("Potencia", valor, "W", plantilla = "8888")

    @Test
    fun valores_que_caben_en_la_plantilla_miden_igual() {
        assertEquals(datosParaMedir(listOf(potencia("185"))), datosParaMedir(listOf(potencia("1240"))))
        assertEquals("8888", datosParaMedir(listOf(potencia("7"))).single().valor)
    }

    @Test
    fun sin_dato_o_mas_largo_que_la_plantilla_cuenta_el_valor() {
        assertEquals(null, datosParaMedir(listOf(potencia(null))).single().valor)
        assertEquals("12345", datosParaMedir(listOf(potencia("12345"))).single().valor)
        assertNotEquals(datosParaMedir(listOf(potencia(null))), datosParaMedir(listOf(potencia("185"))))
    }

    @Test
    fun sin_plantilla_cuenta_el_valor() {
        val d = Dato("Estado", "Rodando", texto = true)
        assertEquals(listOf(d), datosParaMedir(listOf(d)))
    }
}
