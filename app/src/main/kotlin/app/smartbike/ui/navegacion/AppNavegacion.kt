package app.smartbike.ui.navegacion

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import app.smartbike.dominio.Accion
import app.smartbike.ui.tema.SqTema
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.withContext

/**
 * Las pantallas de la app, cada una con lo que necesita para navegar: [irA] una pestaña, [abrir] una secundaria por su
 * ruta y [volver] de una secundaria. En la app son las rutas reales; en los tests, pantallas de prueba.
 */
class Pantallas(
    val rodaje: @Composable (abrirDispositivos: () -> Unit, abrirAjustes: () -> Unit, abrirConexion: () -> Unit) -> Unit,
    val dispositivos: @Composable (irA: (Pestana) -> Unit) -> Unit,
    val conexion: @Composable (irA: (Pestana) -> Unit) -> Unit,
    val botones: @Composable (volver: () -> Unit, asignar: (Accion) -> Unit) -> Unit,
    val ajustes: @Composable (irA: (Pestana) -> Unit, abrir: (String) -> Unit) -> Unit,
    val marchas: @Composable (volver: () -> Unit) -> Unit,
    val datos: @Composable (volver: () -> Unit) -> Unit,
    val tabla: @Composable (volver: () -> Unit) -> Unit,
    val diagnostico: @Composable (volver: () -> Unit) -> Unit,
    val sesiones: @Composable (volver: () -> Unit) -> Unit,
    val asignar: @Composable (accion: Accion, volver: () -> Unit) -> Unit,
)

/**
 * Navegación de la app (spec `app-navigation`; design.md del change `rodaje-completo-y-ajustes`, decisiones 1 y 4).
 * Las pestañas tienen Rodaje como raíz: la pila es siempre [rodaje] o [rodaje, pestaña, secundaria…], así Atrás en una
 * pestaña vuelve a Rodaje y Atrás en Rodaje lo resuelve su pantalla (terminar la sesión). [pantallaSiguiente] trae las
 * acciones de pantalla del Square (`true` la siguiente); solo se recogen con la app a la vista.
 */
@Composable
fun AppNavegacion(
    pantallas: Pantallas,
    pantallaSiguiente: Flow<Boolean> = emptyFlow(),
    nav: NavHostController = rememberNavController(),
) {
    val irA: (Pestana) -> Unit = { p ->
        if (p == Pestana.Rodar) {
            nav.popBackStack(Rutas.RODAJE, inclusive = false)
        } else {
            nav.navigate(p.ruta) {
                popUpTo(Rutas.RODAJE)
                launchSingleTop = true
            }
        }
    }
    val abrir: (String) -> Unit = { ruta -> nav.navigate(ruta) { launchSingleTop = true } }
    val volver: () -> Unit = { nav.popBackStack() }

    val ciclo = LocalLifecycleOwner.current
    LaunchedEffect(pantallaSiguiente) {
        ciclo.repeatOnLifecycle(Lifecycle.State.STARTED) {
            pantallaSiguiente.collect { siguiente ->
                // Navigation exige el hilo principal; el evento llega del hilo de control de la sesión.
                withContext(Dispatchers.Main.immediate) { irA(Pestana.vecina(nav.currentDestination?.route, siguiente)) }
            }
        }
    }

    // Sin transiciones animadas: con el Square se cambia de pantalla seguido, y navegar a mitad de una transición deja
    // entradas de la pila a medio crear (`IllegalStateException` de Navigation). Además, a 2 m se lee mejor un cambio seco.
    NavHost(
        nav,
        startDestination = Rutas.RODAJE,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None },
    ) {
        destino(Rutas.RODAJE) { pantallas.rodaje({ irA(Pestana.Dispositivos) }, { irA(Pestana.Ajustes) }, { irA(Pestana.Conexion) }) }
        destino(Rutas.DISPOSITIVOS) { pantallas.dispositivos(irA) }
        destino(Rutas.CONEXION) { pantallas.conexion(irA) }
        destino(Rutas.BOTONES) { pantallas.botones(volver) { abrir("${Rutas.ASIGNAR}/${Accion.TODAS.indexOf(it)}") } }
        destino(Rutas.AJUSTES) { pantallas.ajustes(irA, abrir) }
        destino(Rutas.MARCHAS) { pantallas.marchas(volver) }
        destino(Rutas.DATOS) { pantallas.datos(volver) }
        destino(Rutas.TABLA) { pantallas.tabla(volver) }
        destino(Rutas.DIAGNOSTICO) { pantallas.diagnostico(volver) }
        destino(Rutas.SESIONES) { pantallas.sesiones(volver) }
        destino("${Rutas.ASIGNAR}/{accion}", arguments = listOf(navArgument("accion") { type = NavType.IntType })) { entrada ->
            pantallas.asignar(Accion.TODAS[entrada.arguments?.getInt("accion") ?: 0], volver)
        }
    }
}

/**
 * Un destino con el fondo del tema. Aunque no haya animación, el `NavHost` compone a la vez la pantalla que sale y la que
 * entra durante algún fotograma; sin fondo propio se verían una a través de la otra (p. ej. los dos títulos superpuestos
 * al cambiar de pestaña).
 */
private fun NavGraphBuilder.destino(
    ruta: String,
    arguments: List<NamedNavArgument> = emptyList(),
    contenido: @Composable (NavBackStackEntry) -> Unit,
) = composable(ruta, arguments = arguments) { entrada ->
    Box(Modifier.fillMaxSize().background(SqTema.colores.bg)) { contenido(entrada) }
}
