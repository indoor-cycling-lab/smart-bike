package app.smartbike.dominio

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/** `Configuracion.validar` (spec `settings-backup`, «Archivos no válidos»). */
class ConfiguracionImportarTest {

    private fun motivo(texto: String): MotivoImportacion = (Configuracion.validar(texto) as ResultadoImportacion.Invalida).motivo

    private fun valida(texto: String): Configuracion = (Configuracion.validar(texto) as ResultadoImportacion.Valida).configuracion

    @Test
    fun texto_vacio() {
        assertEquals(MotivoImportacion.VACIO, motivo(""))
        assertEquals(MotivoImportacion.VACIO, motivo("  \n"))
    }

    @Test
    fun texto_que_no_es_json() {
        assertEquals(MotivoImportacion.NO_ES_JSON, motivo("esto no es una configuración"))
        assertEquals(MotivoImportacion.NO_ES_JSON, motivo("""{"version":5"""))
    }

    @Test
    fun json_que_no_es_una_configuracion() {
        assertEquals(MotivoImportacion.NO_ES_CONFIGURACION, motivo("[1,2,3]"))
        assertEquals(MotivoImportacion.NO_ES_CONFIGURACION, motivo("42"))
        assertEquals(MotivoImportacion.NO_ES_CONFIGURACION, motivo("""{"nombre":"otra cosa"}"""))
        assertEquals(MotivoImportacion.NO_ES_CONFIGURACION, motivo("""{"version":"cinco"}"""))
        assertEquals(MotivoImportacion.NO_ES_CONFIGURACION, motivo("""{"version":0}"""))
    }

    @Test
    fun campo_con_tipo_equivocado() {
        assertEquals(MotivoImportacion.NO_ES_CONFIGURACION, motivo("""{"version":5,"pesoCiclistaKg":"mucho"}"""))
        assertEquals(MotivoImportacion.NO_ES_CONFIGURACION, motivo("""{"version":5,"tema":"rosa"}"""))
    }

    @Test
    fun version_mas_nueva() {
        assertEquals(MotivoImportacion.VERSION_MAS_NUEVA, motivo("""{"version":${Configuracion.VERSION + 1}}"""))
    }

    @Test
    fun version_anterior_se_importa_migrada() {
        val c = valida("""{"version":3,"pesoCiclistaKg":70}""")
        assertEquals(Configuracion.VERSION, c.version)
        assertEquals(70, c.pesoCiclistaKg)
        // Las reglas que no tenía, en sus valores por defecto, como al abrir la app.
        assertEquals(Configuracion.deJson("""{"version":3,"pesoCiclistaKg":70}"""), c)
        val v1 = valida("""{"version":1,"marchaReferencia":15}""")
        assertEquals(Configuracion.deJson("""{"version":1,"marchaReferencia":15}"""), v1)
    }

    @Test
    fun valor_fuera_de_limites_se_recorta() {
        assertEquals(Configuracion.PESO_KG.last, valida("""{"version":5,"pesoCiclistaKg":400}""").pesoCiclistaKg)
        assertEquals(Configuracion.DIFICULTAD_PCT.first, valida("""{"version":5,"dificultadPct":1}""").dificultadPct)
    }

    @Test
    fun ida_y_vuelta_con_todo_cambiado() {
        val c = Configuracion(
            tema = TemaApp.CLARO,
            orientacion = OrientacionApp.HORIZONTAL,
            modelo = TipoModelo.FISICO,
            dificultadPct = 120,
            potenciaMinimaW = 40,
            pesoBiciKg = 9.5,
            pesoCiclistaKg = 82,
            conexion = AjustesConexion(ModoConexion.ZWIFT_RIDE, Identidad.KICKR_BIKE_PRO),
            botonesEnPantalla = true,
            modoTactil = true,
            mostrarAccion = false,
            vibracion = false,
            recordarUltimaMarcha = false,
            datos = listOf(DatoRodaje.TIEMPO, DatoRodaje.POTENCIA),
            bajadas = ModoBajadas.NUNCA_EMPUJAR,
        )
        assertEquals(c, valida(Configuracion.aJson(c)))
    }

    @Test
    fun el_documento_exportado_no_lleva_dispositivos() {
        val json = Configuracion.aJson(Configuracion())
        assertTrue("direccion" !in json && "identidades" !in json && "ultimaMarcha" !in json)
    }

    @Test
    fun importar_lleva_los_ajustes_de_conexion_y_no_las_identidades() {
        val exportada = Configuracion.aJson(Configuracion(conexion = AjustesConexion(identidad = Identidad.KICKR_BIKE_PRO)))
        assertEquals(Identidad.KICKR_BIKE_PRO, valida(exportada).conexion.identidad)
        assertTrue("puente" !in exportada.lowercase() && "mac" !in exportada.lowercase().replace("machine", ""))
    }

    @Test
    fun importar_una_copia_de_la_version_anterior_conserva_lo_demas_y_toma_los_ajustes_por_defecto() {
        val v8 = """{"version":8,"pesoCiclistaKg":77,"conexionZwift":"todo-wifi","mandoZwift":false,"identidad":"kickr-bike-pro"}"""
        val c = valida(v8)
        assertEquals(77, c.pesoCiclistaKg)
        assertEquals(AjustesConexion(), c.conexion)
        assertEquals(Configuracion.VERSION, c.version)
    }

    @Test
    fun importar_una_copia_v10_conserva_el_modo_y_el_nombre() {
        val c = Configuracion(conexion = AjustesConexion(ModoConexion.FTMS_BLUETOOTH, Identidad.KICKR_BIKE_PRO))
        assertEquals(c, valida(Configuracion.aJson(c)))
    }

    @Test
    fun importar_una_copia_v9_conserva_lo_demas_y_toma_la_conexion_por_defecto() {
        // Spec `platform-settings`, «Configuración de la versión anterior».
        val v9 = """{"version":9,"pesoCiclistaKg":78,"plataforma":"zwift","plataformas":{"zwift":{"modo":"zwift-ride","mando":false}}}"""
        val c = valida(v9)
        assertEquals(78, c.pesoCiclistaKg)
        assertEquals(AjustesConexion(), c.conexion)
    }

    @Test
    fun importar_una_copia_igual_conserva_la_grabacion_de_sesiones() {
        val c = Configuracion(grabarSesiones = true, espacioLibreMinimoPct = 30)
        assertEquals(c, valida(Configuracion.aJson(c)))
    }

    @Test
    fun importar_un_minimo_de_espacio_fuera_de_rango_se_recorta() {
        assertEquals(50, valida("""{"version":11,"espacioLibreMinimoPct":200}""").espacioLibreMinimoPct)
    }

    @Test
    fun importar_una_copia_v10_toma_la_grabacion_apagada() {
        val c = valida("""{"version":10,"pesoCiclistaKg":79}""")
        assertEquals(false, c.grabarSesiones)
        assertEquals(10, c.espacioLibreMinimoPct)
        assertEquals(79, c.pesoCiclistaKg)
    }

    @Test
    fun importar_una_copia_con_el_nombre_por_bluetooth_marcado_lo_conserva() {
        val exportada = Configuracion.aJson(Configuracion(conexion = AjustesConexion(nombrePorBluetooth = true)))
        assertEquals(true, valida(exportada).conexion.nombrePorBluetooth)
    }
}
