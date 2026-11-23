package app.qinbly.android

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Depois que o Android termina de instalar uma atualização feita por dentro do app, ele fecha o QINBLY.
 * Este receptor (ACTION_MY_PACKAGE_REPLACED) reabre o app sozinho. Só age se a atualização foi iniciada
 * pelo próprio app (marca gravada por [qinblyMarkRelaunchAfterUpdate]) nos últimos 60 minutos.
 */
class UpdateRelaunchReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_MY_PACKAGE_REPLACED) return
        val sp = context.getSharedPreferences("qinbly_update_relaunch", Context.MODE_PRIVATE)
        val marked = sp.getLong("marked_at", 0L)
        sp.edit().remove("marked_at").apply()
        if (marked <= 0L || System.currentTimeMillis() - marked > 60L * 60L * 1000L) return
        val launch = Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        try { context.startActivity(launch) } catch (_: Exception) { }
    }
}

/** Marca que a atualização foi iniciada por dentro do app, para o app reabrir sozinho quando terminar de instalar. */
internal fun qinblyMarkRelaunchAfterUpdate(context: Context) {
    context.getSharedPreferences("qinbly_update_relaunch", Context.MODE_PRIVATE).edit().putLong("marked_at", System.currentTimeMillis()).apply()
}
