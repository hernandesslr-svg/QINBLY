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
 * Se o Android bloquear a abertura em segundo plano, aparece o aviso "QINBLY atualizado" para tocar e abrir.
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
        try {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            qinblyEnsureUpdateChannel(manager)
            if (android.os.Build.VERSION.SDK_INT >= 33 &&
                androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) return
            val open = PendingIntent.getActivity(context, 7002, launch, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
            val notification = androidx.core.app.NotificationCompat.Builder(context, "qinbly_update_v2")
                .setSmallIcon(R.drawable.ic_stat_qinbly)
                .setContentTitle("QINBLY atualizado")
                .setContentText("Toque para abrir o app.")
                .setContentIntent(open)
                .setAutoCancel(true)
                .setTimeoutAfter(30_000L)
                .build()
            manager.notify(7002, notification)
        } catch (_: Exception) { }
    }
}

/** Marca que a atualização foi iniciada por dentro do app, para o app reabrir sozinho quando terminar de instalar. */
internal fun qinblyMarkRelaunchAfterUpdate(context: Context) {
    context.getSharedPreferences("qinbly_update_relaunch", Context.MODE_PRIVATE).edit().putLong("marked_at", System.currentTimeMillis()).apply()
}
