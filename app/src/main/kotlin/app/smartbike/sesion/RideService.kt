package app.smartbike.sesion

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import app.smartbike.MainActivity
import app.smartbike.R
import app.smartbike.SmartBikeApp
import app.smartbike.dominio.EstadoMarcha
import app.smartbike.dominio.ModoControl
import app.smartbike.plataforma.EstadoPlataforma
import app.smartbike.ui.tema.conIdioma
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * *Foreground service* de tipo `connectedDevice` que mantiene viva la sesión con la pantalla apagada o en segundo
 * plano (F1.5) y muestra su notificación. No tiene lógica: la sesión la crea `AppGraph` (design.md del change
 * `marchas-sin-zwift`, decisiones 1 y 2). Se detiene solo cuando la sesión termina.
 */
class RideService : Service() {
    private val graph get() = (application as SmartBikeApp).graph
    private val ambito = CoroutineScope(SupervisorJob())
    private var observando: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACCION_TERMINAR) {
            graph.terminarSesion()
            return START_NOT_STICKY
        }
        crearCanal()
        val tipo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE else 0
        val sesion = graph.sesion.value
        if (sesion == null) {
            // Arrancado con startForegroundService: sin startForeground, Android cerraría la app aunque se pare enseguida.
            val vacia = NotificationCompat.Builder(this, CANAL).setSmallIcon(R.drawable.ic_pedal_bike).setSilent(true).build()
            ServiceCompat.startForeground(this, ID_NOTIFICACION, vacia, tipo)
            ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }
        ServiceCompat.startForeground(this, ID_NOTIFICACION, notificacion(sesion.estado.value), tipo)
        if (observando == null) observar()
        return START_NOT_STICKY
    }

    private fun observar() {
        val gestor = getSystemService(NotificationManager::class.java)
        observando = ambito.launch(graph.hiloControl) {
            val sesion = graph.sesion.filterNotNull().first()
            launch {
                // Solo lo que cambia el texto: la marcha o la potencia en ERG y las conexiones.
                // El idioma también: al cambiarlo en Ajustes, el canal y la notificación pasan al nuevo sin reiniciar la sesión.
                combine(sesion.estado, graph.idioma) { e, idioma ->
                    listOf(e.marcha, potenciaErg(e), e.neo, e.square, e.plataforma is EstadoPlataforma.Conectado, idioma)
                }
                    .distinctUntilChanged().collect {
                        crearCanal()
                        gestor.notify(ID_NOTIFICACION, notificacion(sesion.estado.value))
                    }
            }
            graph.sesion.first { it == null }
            ServiceCompat.stopForeground(this@RideService, ServiceCompat.STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    override fun onDestroy() {
        ambito.cancel()
        super.onDestroy()
    }

    /** Los textos en el idioma elegido en Ajustes; sin leerlo aún, en el del móvil (spec `app-language`, «Notificación de la sesión»). */
    private fun textos(): Context = graph.idioma.value?.let { conIdioma(it) } ?: this

    private fun crearCanal() {
        val textos = textos()
        val canal = NotificationChannel(CANAL, textos.getString(R.string.canal_sesion), NotificationManager.IMPORTANCE_LOW).apply {
            description = textos.getString(R.string.canal_sesion_descripcion)
            setSound(null, null)
            enableVibration(false)
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(canal)
    }

    private fun notificacion(e: EstadoRodaje): Notification {
        val textos = textos()
        val abrir = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val terminar = PendingIntent.getService(
            this,
            1,
            Intent(this, RideService::class.java).setAction(ACCION_TERMINAR),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        return NotificationCompat.Builder(this, CANAL)
            .setSmallIcon(R.drawable.ic_pedal_bike)
            .setContentTitle(
                potenciaErg(e)?.let { textos.getString(R.string.notif_erg, it) }
                    ?: when (val m = e.marcha) {
                        is EstadoMarcha.Numerada -> textos.getString(R.string.marcha_de_total, m.posicion, m.total)
                        is EstadoMarcha.PlatoPinon -> textos.getString(R.string.marcha_plato_pinon, m.toString())
                    },
            )
            .setContentText(textos.getString(textoConexiones(e)))
            .setContentIntent(abrir)
            .addAction(0, textos.getString(R.string.terminar), terminar)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }

    companion object {
        private const val CANAL = "sesion"
        private const val ID_NOTIFICACION = 1
        private const val ACCION_TERMINAR = "app.smartbike.TERMINAR"

        /** Arranca (o refresca) el servicio; solo con `BLUETOOTH_CONNECT` concedido (decisión 2). */
        fun iniciar(context: Context) {
            context.startForegroundService(Intent(context, RideService::class.java))
        }

        /** En ERG, de Zwift o manual, la potencia que se envía al Neo, que sustituye a la marcha en el título (spec `ride-session`). */
        fun potenciaErg(e: EstadoRodaje): Int? = (e.simulacion.modo as? ModoControl.Erg)?.potenciaW

        /** Con un intento en curso, dice qué se está conectando; si no, qué falta (design.md, decisión 6). */
        fun textoConexiones(e: EstadoRodaje): Int {
            val neo = e.neo == EstadoDispositivo.Conectado
            val square = e.square == EstadoDispositivo.Conectado
            val intento = setOf(EstadoDispositivo.Buscando, EstadoDispositivo.Conectando, EstadoDispositivo.Reconectando)
            val conectandoNeo = e.neo in intento
            val conectandoSquare = e.square in intento
            return when {
                neo && square && e.plataforma is EstadoPlataforma.Conectado && e.usoRodillo -> R.string.notif_todo_conectado_plataforma
                neo && square -> R.string.notif_todo_conectado
                conectandoNeo && conectandoSquare -> R.string.notif_conectando_los_dos
                conectandoNeo -> R.string.notif_conectando_neo
                conectandoSquare -> R.string.notif_conectando_square
                neo -> R.string.notif_sin_square
                square -> R.string.notif_sin_neo
                else -> R.string.notif_sin_ninguno
            }
        }
    }
}
