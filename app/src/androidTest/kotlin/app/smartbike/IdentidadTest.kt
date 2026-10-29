package app.smartbike

import android.content.ComponentName
import android.content.pm.PackageManager
import android.util.TypedValue
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import org.xmlpull.v1.XmlPullParser

/** Spec `app-identity`: icono declarado en el manifiesto y arranque con la splash que pasa a `Theme.SmartBike`. */
class IdentidadTest {
    private val contexto = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun la_app_declara_icono_y_icono_redondo() {
        val info = contexto.packageManager.getApplicationInfo(contexto.packageName, 0)
        assertNotEquals(0, info.icon)
        // ApplicationInfo no expone roundIcon: se lee del manifiesto empaquetado.
        contexto.assets.openXmlResourceParser("AndroidManifest.xml").use { xml ->
            var redondo = 0
            while (xml.next() != XmlPullParser.END_DOCUMENT) {
                if (xml.eventType == XmlPullParser.START_TAG && xml.name == "application") {
                    redondo = xml.getAttributeResourceValue("http://schemas.android.com/apk/res/android", "roundIcon", 0)
                }
            }
            assertNotEquals(0, redondo)
        }
    }

    @Test
    fun la_actividad_arranca_con_la_splash() {
        val info = contexto.packageManager.getActivityInfo(ComponentName(contexto, MainActivity::class.java), PackageManager.GET_META_DATA)
        assertEquals(R.style.Theme_SmartBike_Splash, info.themeResource)
    }

    @Test
    fun la_actividad_abierta_usa_el_tema_de_la_app() {
        ActivityScenario.launch(MainActivity::class.java).use { escenario ->
            escenario.onActivity { actividad ->
                val valor = TypedValue()
                // La biblioteca aplica postSplashScreenTheme encima del tema de arranque (los atributos de la splash siguen
                // ahí), así que se comprueba lo que pone Theme.SmartBike: la ventana de Rodaje.
                assertEquals(true, actividad.theme.resolveAttribute(android.R.attr.windowBackground, valor, true))
                assertEquals(actividad.getColor(R.color.ventana_bg), valor.data)
            }
        }
    }
}
