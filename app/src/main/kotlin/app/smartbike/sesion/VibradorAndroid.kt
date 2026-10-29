package app.smartbike.sesion

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * Vibración corta en cada cambio y doble en el tope (design-system/components/GearDisplay/README.md); larga y triple
 * para los gestos largo y doble (design.md del change `gestos-y-asignacion`, decisión 9).
 */
class VibradorAndroid(context: Context) : Vibrador {
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator?
    }

    override fun corta() {
        vibrator?.vibrate(VibrationEffect.createOneShot(CORTA_MS, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    override fun doble() {
        vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, DOBLE_MS, DOBLE_PAUSA_MS, DOBLE_MS), -1))
    }

    override fun larga() {
        vibrator?.vibrate(VibrationEffect.createOneShot(LARGA_MS, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    override fun triple() {
        vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, CORTA_MS, TRIPLE_PAUSA_MS, CORTA_MS, TRIPLE_PAUSA_MS, CORTA_MS), -1))
    }

    private companion object {
        const val CORTA_MS = 20L
        const val DOBLE_MS = 40L
        const val DOBLE_PAUSA_MS = 80L
        const val LARGA_MS = 80L
        const val TRIPLE_PAUSA_MS = 60L
    }
}
