package app.smartbike

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.w3c.dom.Element

/**
 * Los textos en inglés (spec `app-language`): `values-en/strings.xml` tiene todas las cadenas y plurales de `values/`, con
 * los mismos marcadores de formato (design.md del change `idioma-es-en`, decisión 6). El test corre en el directorio del
 * módulo `app`.
 */
class TextosInglesTest {
    private class Texto(val tipo: String, val traducible: Boolean, val marcadores: List<List<String>>)

    private val marcador = Regex("%(\\d+\\$)?[0-9.]*[a-zA-Z%]")

    private fun leer(ruta: String): Map<String, Texto> {
        val raiz = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(File(ruta)).documentElement
        val resultado = linkedMapOf<String, Texto>()
        val hijos = raiz.childNodes
        for (i in 0 until hijos.length) {
            val e = hijos.item(i) as? Element ?: continue
            val traducible = e.getAttribute("translatable") != "false"
            val marcadores = when (e.tagName) {
                "string" -> listOf(marcadoresDe(e.textContent))
                "plurals" -> (0 until e.getElementsByTagName("item").length).map { marcadoresDe(e.getElementsByTagName("item").item(it).textContent) }
                else -> continue
            }
            resultado[e.getAttribute("name")] = Texto(e.tagName, traducible, marcadores)
        }
        return resultado
    }

    private fun marcadoresDe(texto: String): List<String> = marcador.findAll(texto).map { it.value.replace(Regex("^%\\d+\\$"), "%") }.sorted().toList()

    private val es = leer("src/main/res/values/strings.xml")
    private val en = leer("src/main/res/values-en/strings.xml")

    @Test
    fun cada_cadena_tiene_su_traduccion() {
        val faltan = es.filterValues { it.traducible }.keys - en.keys
        assertTrue(faltan.isEmpty(), "Faltan en values-en: $faltan")
    }

    @Test
    fun no_hay_cadenas_de_mas_en_ingles() {
        val sobran = en.keys - es.keys
        assertTrue(sobran.isEmpty(), "Sobran en values-en: $sobran")
    }

    @Test
    fun mismo_tipo_y_mismos_marcadores_de_formato() {
        val distintas = es.filter { (k, v) -> v.traducible && en[k] != null }.mapNotNull { (k, v) ->
            val t = en.getValue(k)
            val marcadoresEn = t.marcadores.flatten().sorted()
            val marcadoresEs = v.marcadores.flatten().sorted()
            // Un plural en inglés puede escribir el número en letra en `one` (sin marcador): se compara el conjunto de todos.
            if (t.tipo != v.tipo || marcadoresEn.toSet() != marcadoresEs.toSet()) k else null
        }
        assertEquals(emptyList<String>(), distintas, "Distinto tipo o marcadores entre es y en")
    }

    @Test
    fun los_marcadores_de_una_cadena_coinciden_en_numero() {
        val distintas = es.filter { (k, v) -> v.traducible && v.tipo == "string" && en[k] != null }.mapNotNull { (k, v) ->
            if (en.getValue(k).marcadores != v.marcadores) k else null
        }
        assertEquals(emptyList<String>(), distintas, "Distinto número de marcadores entre es y en")
    }
}
