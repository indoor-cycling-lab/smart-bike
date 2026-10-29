package app.smartbike.ui.tema

import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.isUnspecified
import java.io.File
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Tokens.kt se genera a partir de design-system/tokens.json con generar.py, que Gradle no ejecuta.
 * Este test falla si se cambió tokens.json sin regenerar o si se editó Tokens.kt a mano.
 */
class TokensSincronizadosTest {

    private val tokens: JsonObject = Json.parseToJsonElement(
        File(checkNotNull(System.getProperty("tokens.json")) { "Falta la propiedad tokens.json (app/build.gradle.kts)" })
            .readText(),
    ).jsonObject

    private val fallos = mutableListOf<String>()

    @Test
    fun tokens_kt_coincide_con_tokens_json() {
        comprobarColores()
        comprobarTipografia()
        comprobarDimensiones("spacing", SqEspacios.porNombre().mapValues { it.value.value })
        comprobarDimensiones("radius", SqRadios.porNombre().mapValues { it.value.value })
        comprobarDimensiones("size", SqTamanos.porNombre().mapValues { it.value.value })

        assertTrue(fallos.isEmpty()) {
            "Tokens.kt no coincide con design-system/tokens.json:\n" +
                fallos.joinToString("\n") { "  - $it" } +
                "\nEjecuta python3 scripts/design-system/generar.py"
        }
    }

    private fun comprobarColores() {
        val lista = tokens.getValue("color").jsonObject.getValue("tokens").jsonArray.map { it.jsonObject }
        val porNombre = lista.associate { it.texto("name") to it.getValue("value") }

        fun resolver(nombre: String, tema: String): String = when (val v = porNombre.getValue(nombre)) {
            is JsonPrimitive -> resolver(v.content.trim('{', '}'), tema)
            else -> v.jsonObject.texto(tema)
        }

        for ((tema, colores) in listOf("rodaje" to ColoresRodaje, "claro" to ColoresClaro)) {
            val app = colores.porNombre()
            comprobarNombres("color ($tema)", porNombre.keys, app.keys)
            for (nombre in porNombre.keys.intersect(app.keys)) {
                val esperado = resolver(nombre, tema).lowercase()
                val encontrado = "#%06x".format(app.getValue(nombre).toArgb() and 0xFFFFFF)
                if (esperado != encontrado) fallos += "color $nombre ($tema): esperado $esperado, en Tokens.kt $encontrado"
            }
        }
    }

    private fun comprobarTipografia() {
        val tipo = tokens.getValue("type").jsonObject
        val familias = tipo.getValue("families").jsonObject.keys
        comprobarNombres("familia", familias, SqFamilias.porNombre().keys)

        val app = SqTipografia.porNombre()
        val json = tipo.getValue("groups").jsonArray.flatMap { g ->
            val familia = g.jsonObject.texto("family")
            g.jsonObject.getValue("styles").jsonArray.map { familia to it.jsonObject }
        }
        comprobarNombres("estilo", json.map { it.second.texto("name") }.toSet(), app.keys)
        comprobarNombres("ejemplo", json.map { it.second.texto("name") }.toSet(), SqTipografia.ejemplos.keys)

        for ((familia, s) in json) {
            val nombre = s.texto("name")
            val estilo: TextStyle = app[nombre] ?: continue
            fun comparar(campo: String, esperado: Any?, encontrado: Any?) {
                if (esperado != encontrado) fallos += "estilo $nombre, $campo: esperado $esperado, en Tokens.kt $encontrado"
            }
            comparar("familia", SqFamilias.porNombre()[familia], estilo.fontFamily)
            comparar("peso", s.getValue("fontWeight").jsonPrimitive.int, estilo.fontWeight?.weight)
            comparar("tamaño", px(s.texto("fontSize")), estilo.fontSize.valorSp())
            comparar("interlineado", px(s.texto("lineHeight")), estilo.lineHeight.valorSp())
            comparar(
                "espaciado de letras",
                s["letterSpacing"]?.jsonPrimitive?.content?.removeSuffix("em")?.toFloat(),
                estilo.letterSpacing.takeUnless { it.isUnspecified }?.value,
            )
            comparar("cifras tabulares", if (familia == "numeric") "tnum" else null, estilo.fontFeatureSettings)
            comparar("ejemplo", s.texto("sample"), SqTipografia.ejemplos[nombre])
        }
    }

    private fun comprobarDimensiones(grupo: String, app: Map<String, Float>) {
        val json = tokens.getValue(grupo).jsonObject.getValue("tokens").jsonArray
            .associate { it.jsonObject.texto("name") to px(it.jsonObject.texto("value")) }
        comprobarNombres(grupo, json.keys, app.keys)
        for (nombre in json.keys.intersect(app.keys)) {
            if (json[nombre] != app[nombre]) fallos += "$grupo $nombre: esperado ${json[nombre]} dp, en Tokens.kt ${app[nombre]} dp"
        }
    }

    private fun comprobarNombres(grupo: String, json: Set<String>, app: Set<String>) {
        (json - app).forEach { fallos += "$grupo $it: está en tokens.json y falta en Tokens.kt" }
        (app - json).forEach { fallos += "$grupo $it: está en Tokens.kt y no en tokens.json" }
    }

    private fun JsonObject.texto(clave: String) = getValue(clave).jsonPrimitive.content

    private fun px(valor: String) = valor.removeSuffix("px").toFloat()

    private fun TextUnit.valorSp(): Float? = if (isSp) value else null
}
