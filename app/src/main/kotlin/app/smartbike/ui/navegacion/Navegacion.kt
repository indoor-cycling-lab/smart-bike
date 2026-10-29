package app.smartbike.ui.navegacion

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import app.smartbike.R

/** Rutas de la app (spec `app-navigation`). */
object Rutas {
    const val RODAJE = "rodaje"
    const val DISPOSITIVOS = "dispositivos"
    const val CONEXION = "conexion"
    const val BOTONES = "botones"
    const val AJUSTES = "ajustes"
    const val ASIGNAR = "asignar"
    const val MARCHAS = "marchas"
    const val DATOS = "datos"
    const val TABLA = "tabla"
    const val DIAGNOSTICO = "diagnostico"
    const val SESIONES = "sesiones"
}

/** Las pestañas de la barra inferior, en su orden (`BottomNav`). Rodar es la pantalla de rodaje, sin barra. */
enum class Pestana(val ruta: String, @StringRes val nombre: Int, @DrawableRes val icono: Int) {
    Rodar(Rutas.RODAJE, R.string.rodar, R.drawable.ic_pedal_bike),
    Conexion(Rutas.CONEXION, R.string.conexion, R.drawable.ic_sensors),
    Dispositivos(Rutas.DISPOSITIVOS, R.string.dispositivos, R.drawable.ic_bluetooth),
    Ajustes(Rutas.AJUSTES, R.string.ajustes, R.drawable.ic_tune),
    ;

    companion object {
        /**
         * La pestaña de una ruta: la suya, o la del origen de una secundaria (Botones, Marchas, Datos en pantalla, Tabla de
         * marchas, Diagnóstico y Sesiones grabadas son de Ajustes, y Asignar acción, que se abre desde Botones, también). Una ruta desconocida
         * es Rodar.
         */
        fun de(ruta: String?): Pestana {
            val base = ruta?.substringBefore('/')
            return when (base) {
                Rutas.BOTONES, Rutas.ASIGNAR, Rutas.MARCHAS, Rutas.DATOS, Rutas.TABLA, Rutas.DIAGNOSTICO, Rutas.SESIONES -> Ajustes
                else -> entries.firstOrNull { it.ruta == base } ?: Rodar
            }
        }

        /**
         * Pantalla anterior o siguiente con el Square (spec `app-navigation`): la pestaña vecina de la de [ruta], en
         * orden circular (design.md del change `rodaje-completo-y-ajustes`, decisión 4).
         */
        fun vecina(ruta: String?, siguiente: Boolean): Pestana {
            val i = de(ruta).ordinal + if (siguiente) 1 else -1
            return entries[Math.floorMod(i, entries.size)]
        }
    }
}
