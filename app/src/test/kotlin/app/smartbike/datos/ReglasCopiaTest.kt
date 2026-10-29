package app.smartbike.datos

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.w3c.dom.Element

/** Spec `settings-backup`, «Copia de seguridad del sistema»: solo la configuración entra en la copia. */
class ReglasCopiaTest {

    /** Los `include` y `exclude` de [seccion] (o de todo el documento), como `etiqueta dominio:ruta`. */
    private fun reglas(archivo: String, seccion: String? = null): List<String> {
        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(File("src/main/res/xml/$archivo"))
        val raiz = seccion?.let { doc.getElementsByTagName(it).item(0) as Element } ?: doc.documentElement
        val nodos = raiz.getElementsByTagName("*")
        return (0 until nodos.length).map { nodos.item(it) as Element }
            .filter { it.tagName == "include" || it.tagName == "exclude" }
            .map { "${it.tagName} ${it.getAttribute("domain")}:${it.getAttribute("path")}" }
    }

    private val soloConfiguracion = listOf("include file:${Almacen.RUTA_CONFIGURACION}")

    @Test
    fun la_copia_de_google_y_la_transferencia_solo_llevan_la_configuracion() {
        assertEquals(soloConfiguracion, reglas("reglas_copia.xml", "cloud-backup"))
        assertEquals(soloConfiguracion, reglas("reglas_copia.xml", "device-transfer"))
    }

    @Test
    fun la_copia_de_android_9_a_11_solo_lleva_la_configuracion() {
        assertEquals(soloConfiguracion, reglas("copia_completa.xml"))
    }

    @Test
    fun el_manifiesto_usa_las_dos_reglas() {
        val manifiesto = File("src/main/AndroidManifest.xml").readText()
        listOf("""android:allowBackup="true"""", """android:dataExtractionRules="@xml/reglas_copia"""", """android:fullBackupContent="@xml/copia_completa"""")
            .forEach { assertEquals(1, Regex(Regex.escape(it)).findAll(manifiesto).count(), it) }
    }
}
