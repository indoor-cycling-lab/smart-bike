package app.smartbike.ui.ajustes

import app.smartbike.datos.AlmacenConfiguracion
import app.smartbike.datos.CopiaConfiguracion
import app.smartbike.dominio.Configuracion
import app.smartbike.dominio.IdiomaApp
import app.smartbike.dominio.MotivoImportacion
import app.smartbike.dominio.ResultadoImportacion
import app.smartbike.dominio.TemaApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/** Spec `settings-backup`: las transiciones de «Copia de seguridad» y que importar no toca nada hasta confirmar. */
@OptIn(ExperimentalCoroutinesApi::class)
class AjustesCopiaTest {
    private class AlmacenFalso(inicial: Configuracion = Configuracion()) : AlmacenConfiguracion {
        val actual = MutableStateFlow(inicial)
        var escrituras = 0
        override val configuracion: Flow<Configuracion> = actual
        override suspend fun actualizarConfiguracion(cambio: (Configuracion) -> Configuracion) {
            escrituras++
            actual.value = cambio(actual.value)
        }
    }

    private class CopiaFalsa : CopiaConfiguracion {
        var escrito: Pair<String, Configuracion>? = null
        var puede = true
        var lectura: ResultadoImportacion = ResultadoImportacion.Invalida(MotivoImportacion.VACIO)
        override suspend fun exportar(destino: String, c: Configuracion): Boolean {
            if (puede) escrito = destino to c
            return puede
        }
        override suspend fun leer(origen: String) = lectura
    }

    @BeforeEach fun antes() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterEach fun despues() = Dispatchers.resetMain()

    /** `estado` solo se calcula con alguien mirando, como en la pantalla. */
    private fun TestScope.crear(almacen: AlmacenFalso, copia: CopiaFalsa): AjustesViewModel {
        val vm = AjustesViewModel(almacen, copia = copia)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.estado.collect { } }
        return vm
    }

    private val otra = Configuracion(tema = TemaApp.CLARO, pesoCiclistaKg = 90)

    @Test
    fun exportar_guarda_la_configuracion_actual() = runTest {
        val almacen = AlmacenFalso(otra)
        val copia = CopiaFalsa()
        val vm = crear(almacen, copia)
        vm.exportar("content://destino")
        assertEquals("content://destino" to otra, copia.escrito)
        assertEquals(Copia.Exportada, vm.estado.value.copia)
    }

    @Test
    fun exportar_con_el_selector_cerrado_no_hace_nada() = runTest {
        val copia = CopiaFalsa()
        val vm = crear(AlmacenFalso(), copia)
        vm.exportar(null)
        assertEquals(null, copia.escrito)
        assertEquals(Copia.Ninguna, vm.estado.value.copia)
    }

    @Test
    fun exportar_que_falla() = runTest {
        val copia = CopiaFalsa().apply { puede = false }
        val vm = crear(AlmacenFalso(), copia)
        vm.exportar("content://solo-lectura")
        assertEquals(Copia.ErrorExportar, vm.estado.value.copia)
    }

    @Test
    fun archivo_valido_queda_pendiente_sin_cambiar_los_ajustes() = runTest {
        val almacen = AlmacenFalso()
        val vm = crear(almacen, CopiaFalsa().apply { lectura = ResultadoImportacion.Valida(otra) })
        vm.elegirArchivo("content://archivo")
        assertEquals(Copia.Pendiente(otra), vm.estado.value.copia)
        assertEquals(0, almacen.escrituras)
        assertEquals(Configuracion(), almacen.actual.value)
    }

    @Test
    fun confirmar_aplica_la_configuracion_del_archivo() = runTest {
        val almacen = AlmacenFalso()
        val vm = crear(almacen, CopiaFalsa().apply { lectura = ResultadoImportacion.Valida(otra) })
        vm.elegirArchivo("content://archivo")
        vm.confirmarImportacion()
        assertEquals(otra, almacen.actual.value)
        assertEquals(Copia.Importada, vm.estado.value.copia)
    }

    @Test
    fun confirmar_un_archivo_con_idioma_lo_aplica() = runTest {
        val almacen = AlmacenFalso(Configuracion(idioma = IdiomaApp.ES))
        val vm = crear(almacen, CopiaFalsa().apply { lectura = ResultadoImportacion.Valida(Configuracion(idioma = IdiomaApp.EN)) })
        vm.elegirArchivo("content://archivo")
        vm.confirmarImportacion()
        assertEquals(IdiomaApp.EN, almacen.actual.value.idioma)
    }

    @Test
    fun confirmar_un_archivo_anterior_al_ajuste_conserva_el_idioma_de_la_app() = runTest {
        val almacen = AlmacenFalso(Configuracion(idioma = IdiomaApp.EN))
        val vm = crear(almacen, CopiaFalsa().apply { lectura = ResultadoImportacion.Valida(otra) })
        vm.elegirArchivo("content://archivo")
        vm.confirmarImportacion()
        assertEquals(otra.copy(idioma = IdiomaApp.EN), almacen.actual.value)
    }

    @Test
    fun elegir_idioma_lo_guarda_y_lo_muestra() = runTest {
        val almacen = AlmacenFalso(Configuracion(idioma = IdiomaApp.ES))
        val vm = crear(almacen, CopiaFalsa())
        vm.idioma(IdiomaApp.EN)
        assertEquals(IdiomaApp.EN, almacen.actual.value.idioma)
        assertEquals(IdiomaApp.EN, vm.estado.value.idioma)
    }

    @Test
    fun cancelar_deja_los_ajustes_como_estaban() = runTest {
        val almacen = AlmacenFalso()
        val vm = crear(almacen, CopiaFalsa().apply { lectura = ResultadoImportacion.Valida(otra) })
        vm.elegirArchivo("content://archivo")
        vm.descartarCopia()
        assertEquals(Copia.Ninguna, vm.estado.value.copia)
        assertEquals(Configuracion(), almacen.actual.value)
    }

    @Test
    fun archivo_no_valido_no_cambia_nada() = runTest {
        val almacen = AlmacenFalso()
        val vm = crear(almacen, CopiaFalsa().apply { lectura = ResultadoImportacion.Invalida(MotivoImportacion.VERSION_MAS_NUEVA) })
        vm.elegirArchivo("content://archivo")
        assertEquals(Copia.ErrorImportar(MotivoImportacion.VERSION_MAS_NUEVA), vm.estado.value.copia)
        assertEquals(0, almacen.escrituras)
    }

    @Test
    fun confirmar_sin_nada_pendiente_no_hace_nada() = runTest {
        val almacen = AlmacenFalso()
        val vm = crear(almacen, CopiaFalsa())
        vm.confirmarImportacion()
        assertEquals(0, almacen.escrituras)
    }

    @Test
    fun selector_cerrado_al_importar_no_muestra_nada() = runTest {
        val vm = crear(AlmacenFalso(), CopiaFalsa())
        vm.elegirArchivo(null)
        assertEquals(Copia.Ninguna, vm.estado.value.copia)
    }
}
