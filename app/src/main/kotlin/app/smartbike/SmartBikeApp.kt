package app.smartbike

import android.app.Application

class SmartBikeApp : Application() {
    /** Contenedor de dependencias de la app; vive tanto como el proceso. */
    lateinit var graph: AppGraph
        private set

    override fun onCreate() {
        super.onCreate()
        graph = AppGraph(this)
    }
}
