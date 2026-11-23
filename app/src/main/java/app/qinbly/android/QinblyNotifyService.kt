package app.qinbly.android

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.IBinder

/**
 * Avisos com o app fechado, quase na hora: um serviço em primeiro plano (com um aviso discreto fixo na barra)
 * que consulta novidades a cada ~40 segundos. Sem Firebase/Google é o jeito que o Android permite de não ser
 * dormido ou atrasado como acontece com tarefas agendadas. Para parar: desligar "Notificações" nas configurações do app.
 */
class QinblyNotifyService : Service() {
    @Volatile private var running = false
    private var worker: Thread? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startAsForeground()
        if (worker?.isAlive != true) {
            running = true
            worker = Thread { loop() }.also { it.isDaemon = true; it.start() }
        }
        return START_STICKY
    }

    private fun startAsForeground() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (android.os.Build.VERSION.SDK_INT >= 26) {
            // Importância mínima: sem som, sem ícone na tela de bloqueio, recolhida na barra.
            manager.createNotificationChannel(NotificationChannel(CHANNEL, "Receber avisos em segundo plano", NotificationManager.IMPORTANCE_MIN).apply {
                description = "Mantém o QINBLY recebendo avisos com o app fechado"
                setShowBadge(false)
            })
        }
        val open = PendingIntent.getActivity(
            this, 7002, Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notification: Notification = androidx.core.app.NotificationCompat.Builder(this, CHANNEL)
            .setSmallIcon(R.drawable.ic_stat_qinbly)
            .setContentTitle("QINBLY")
            .setContentText("Recebendo seus avisos")
            .setContentIntent(open)
            .setOngoing(true)
            .setSilent(true)
            .setShowWhen(false)
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_MIN)
            .setCategory(androidx.core.app.NotificationCompat.CATEGORY_SERVICE)
            .build()
        if (android.os.Build.VERSION.SDK_INT >= 34) {
            startForeground(NOTIFICATION_ID, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun loop() {
        while (running) {
            val prefs = applicationContext.getSharedPreferences("qinbly_session", Context.MODE_PRIVATE).byAccount()
            val token = prefs.getString("token", null)
            if (token.isNullOrBlank() || !prefs.getBoolean("setting_notifications", true)) {
                stopSelf()
                break
            }
            try { QinblyNotifyWorker.pollOnce(applicationContext) } catch (_: Exception) { }
            try { Thread.sleep(POLL_MS) } catch (_: InterruptedException) { break }
        }
    }

    override fun onDestroy() {
        running = false
        worker?.interrupt()
        super.onDestroy()
    }

    companion object {
        private const val CHANNEL = "qinbly_background"
        private const val NOTIFICATION_ID = 7002
        private const val POLL_MS = 40_000L

        /** Liga o serviço se a pessoa está logada e com as notificações ligadas. Chamar de novo é seguro. */
        fun start(context: Context) {
            try {
                val prefs = context.getSharedPreferences("qinbly_session", Context.MODE_PRIVATE).byAccount()
                if (prefs.getString("token", null).isNullOrBlank() || !prefs.getBoolean("setting_notifications", true)) return
                androidx.core.content.ContextCompat.startForegroundService(context, Intent(context, QinblyNotifyService::class.java))
            } catch (_: Exception) { }
        }
    }
}

/** Religa o serviço quando o celular liga ou o app é atualizado. */
class QinblyBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        QinblyNotifyService.start(context)
    }
}
