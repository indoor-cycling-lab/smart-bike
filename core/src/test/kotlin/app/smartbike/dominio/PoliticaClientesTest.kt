package app.smartbike.dominio

import app.smartbike.dominio.PoliticaClientes.Actual
import app.smartbike.dominio.PoliticaClientes.Decision
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/** Spec `device-connection`, sustitución de cliente DirCon. */
class PoliticaClientesTest {
    private val actual = Actual("192.168.1.10", ultimoMensajeMs = 1_000)

    @Test
    fun sin_cliente_actual_se_acepta() = assertEquals(Decision.Sustituir, PoliticaClientes.decidir(null, "192.168.1.20", 5_000))

    @Test
    fun la_misma_direccion_sustituye_aunque_el_actual_este_activo() = assertEquals(Decision.Sustituir, PoliticaClientes.decidir(actual, "192.168.1.10", 1_100))

    @Test
    fun otra_direccion_con_el_actual_activo_se_rechaza() = assertEquals(Decision.Rechazar, PoliticaClientes.decidir(actual, "192.168.1.20", 1_000 + 14_900))

    @Test
    fun otra_direccion_con_el_actual_en_silencio_sustituye() = assertEquals(Decision.Sustituir, PoliticaClientes.decidir(actual, "192.168.1.20", 1_000 + 15_000))
}
