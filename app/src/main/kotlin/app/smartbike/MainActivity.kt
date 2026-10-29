package app.smartbike

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import app.smartbike.sesion.EventoRodaje
import app.smartbike.sesion.RideService
import app.smartbike.ui.ajustes.AjustesRuta
import app.smartbike.ui.botones.AsignarAccionRuta
import app.smartbike.ui.botones.BotonesRuta
import app.smartbike.ui.conexion.ConexionRuta
import app.smartbike.ui.datos.DatosRuta
import app.smartbike.ui.diagnostico.DiagnosticoRuta
import app.smartbike.ui.dispositivos.DispositivosRuta
import app.smartbike.ui.marchas.MarchasRuta
import app.smartbike.ui.navegacion.AppNavegacion
import app.smartbike.ui.navegacion.Pantallas
import app.smartbike.ui.navegacion.Rutas
import app.smartbike.ui.rodaje.RodajeRuta
import app.smartbike.ui.sesiones.SesionesRuta
import app.smartbike.ui.tabla.TablaRuta
import app.smartbike.ui.tema.IdiomaElegido
import app.smartbike.ui.tema.OrientacionElegida
import app.smartbike.ui.tema.SqTemaElegido
import app.smartbike.ui.tema.Tema
import app.smartbike.ui.tema.coloresDe
import app.smartbike.ui.tema.comoPantalla
import app.smartbike.ui.tema.comoTema
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val graph get() = (application as SmartBikeApp).graph

    /**
     * `BLUETOOTH_ADVERTISE`, para el mando de Zwift, en una instalación que ya tenía los permisos de buscar y conectar:
     * es del mismo grupo y Android lo concede sin diálogo o con uno solo (design.md del change `mando-zwift-emulado`,
     * decisión 8). Una vez por actividad, para no insistir si se deniega.
     */
    private val pedirAnunciar = registerForActivityResult(ActivityResultContracts.RequestPermission()) { graph.comprobarPermisos() }
    private var anunciarPedido = false

    override fun onCreate(savedInstanceState: Bundle?) {
        // Splash del sistema sin condición de espera: se va en cuanto Rodaje está listo. Antes de super.onCreate.
        installSplashScreen()
        // Al girar, el tema ya está leído: la ventana y las barras salen con él desde el principio. Al abrir la app, hasta
        // leerlo, la ventana es la de rodaje (`color_ventana.xml`) y no se pinta ninguna pantalla.
        aplicarAVentana(graph.tema.value?.comoTema() ?: Tema.Rodaje)
        // La orientación, igual: al recrearse la actividad sale ya con la elegida, sin un giro más.
        graph.orientacion.value?.let { requestedOrientation = it.comoPantalla() }
        super.onCreate(savedInstanceState)
        if (BuildConfig.DEBUG) graph.sondaConNombreFalso(intent.getBooleanExtra(EXTRA_SONDA_NOMBRE_FALSO, false))
        // Abrir la app empieza la sesión, o vuelve a la que está en curso (spec ride-session).
        val sesion = graph.iniciarSesion()
        // Fuera de la composición: un flujo nuevo en cada recomposición reiniciaría su colector y podría perder un evento.
        val pantallaSiguiente = sesion.eventos.filterIsInstance<EventoRodaje.Pantalla>().map { it.siguiente }
        lifecycleScope.launch {
            // Al terminar la sesión (Atrás en Rodaje o «Terminar» en la notificación), la app se cierra.
            graph.sesion.first { it !== sesion }
            finish()
        }
        setContent {
            val tema by graph.tema.collectAsStateWithLifecycle()
            val orientacion by graph.orientacion.collectAsStateWithLifecycle()
            val idioma by graph.idioma.collectAsStateWithLifecycle()
            OrientacionElegida(orientacion)
            LaunchedEffect(tema) { tema?.let { aplicarAVentana(it.comoTema()) } }
            IdiomaElegido(idioma) {
                SqTemaElegido(tema, Modifier.fillMaxSize()) {
                    AppNavegacion(
                        Pantallas(
                            rodaje = { abrirDispositivos, abrirAjustes, abrirConexion -> RodajeRuta(abrirDispositivos, abrirAjustes, abrirConexion) },
                            dispositivos = { irA -> DispositivosRuta(irA) },
                            conexion = { irA -> ConexionRuta(irA) },
                            botones = { volver, asignar -> BotonesRuta(volver, asignar) },
                            ajustes = { irA, abrir ->
                                AjustesRuta(
                                    irA = irA,
                                    abrirBotones = { abrir(Rutas.BOTONES) },
                                    abrirMarchas = { abrir(Rutas.MARCHAS) },
                                    abrirDiagnostico = { abrir(Rutas.DIAGNOSTICO) },
                                    abrirDatos = { abrir(Rutas.DATOS) },
                                    abrirTabla = { abrir(Rutas.TABLA) },
                                    abrirSesiones = { abrir(Rutas.SESIONES) },
                                )
                            },
                            marchas = { volver -> MarchasRuta(volver) },
                            datos = { volver -> DatosRuta(volver) },
                            tabla = { volver -> TablaRuta(volver) },
                            diagnostico = { volver -> DiagnosticoRuta(volver) },
                            sesiones = { volver -> SesionesRuta(volver) },
                            asignar = { accion, volver -> AsignarAccionRuta(accion, volver) },
                        ),
                        pantallaSiguiente = pantallaSiguiente,
                    )
                }
            }
        }
    }

    /**
     * El fondo de la ventana con el `bg` de [tema], para que no asome el otro al girar ni en las transiciones, y los
     * iconos de las barras del sistema legibles sobre él: oscuros en claro y claros en rodaje (spec `app-theme`).
     */
    private fun aplicarAVentana(tema: Tema) {
        window.setBackgroundDrawable(ColorDrawable(coloresDe(tema).bg.toArgb()))
        val barras = when (tema) {
            Tema.Rodaje -> SystemBarStyle.dark(Color.TRANSPARENT)
            Tema.Claro -> SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        }
        enableEdgeToEdge(statusBarStyle = barras, navigationBarStyle = barras)
    }

    override fun onResume() {
        super.onResume()
        // El servicio en primer plano necesita BLUETOOTH_CONNECT concedido; se concede en Diagnóstico, y al volver del
        // diálogo la actividad pasa otra vez por aquí. Con el permiso, la sesión se conecta a los configurados.
        if (graph.comprobarPermisos()) {
            RideService.iniciar(this)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !anunciarPedido && !tiene(Manifest.permission.BLUETOOTH_ADVERTISE)) {
                anunciarPedido = true
                pedirAnunciar.launch(Manifest.permission.BLUETOOTH_ADVERTISE)
            }
        }
    }

    private fun tiene(permiso: String) = ContextCompat.checkSelfPermission(this, permiso) == PackageManager.PERMISSION_GRANTED

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (BuildConfig.DEBUG) graph.sondaConNombreFalso(intent.getBooleanExtra(EXTRA_SONDA_NOMBRE_FALSO, false))
    }

    companion object {
        /** Solo en depuración (`adb shell am start … --ez sonda_nombre_falso true`): simula un anuncio sin respuesta. */
        const val EXTRA_SONDA_NOMBRE_FALSO = "sonda_nombre_falso"
    }
}
