package app.smartbike.ui.tema

import android.content.pm.ActivityInfo
import app.smartbike.dominio.OrientacionApp
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class OrientacionElegidaTest {
    @Test
    fun cada_opcion_pide_su_orientacion_de_android() {
        assertEquals(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED, OrientacionApp.SEGUN_MOVIL.comoPantalla())
        assertEquals(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT, OrientacionApp.VERTICAL.comoPantalla())
        assertEquals(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE, OrientacionApp.HORIZONTAL.comoPantalla())
    }
}
