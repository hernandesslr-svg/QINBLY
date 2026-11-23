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
 * que consulta novidades a cada ~40 segundos (um alarme acorda o celular a cada consulta). Sem Firebase/Google é o jeito que o Android permite de não ser
 * dormido ou atrasado como acontece com tarefas agendadas. Para parar: desligar "Notificações" nas configurações do app.
 */
class QinblyNotifyService : Service() {
    private val polling = java.util.concurrent.atomic.AtomicBoolean(false)

    override fun onBind(intent: Intent?): IBinder? = null

    /** Cada chamada (abrir o app, alarme, reinício do sistema) faz uma consulta e agenda a próxima. */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Desligado: sem aviso fixo na barra. Um alarme antigo que ainda dispare só encerra o serviço.
        stopSelf()
        return START_NOT_STICKY
    }

    private fun legacyOnStartCommand(intent: Intent?): Int {
        try {
            startAsForeground("Recebendo seus avisos")
        } catch (_: Exception) {
            // Se o Android recusar o serviço fixo, não derruba o app: a tarefa agendada continua como reserva.
            stopSelf()
            return START_NOT_STICKY
        }
        if (polling.compareAndSet(false, true)) {
            Thread {
                // Segura o processador acordado só durante a consulta (celular de tela apagada dorme e atrasa tudo).
                val power = getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
                val lock = power.newWakeLock(android.os.PowerManager.PARTIAL_WAKE_LOCK, "qinbly:avisos").apply { setReferenceCounted(false) }
                try {
                    lock.acquire(60_000L)
                    val prefs = applicationContext.getSharedPreferences("qinbly_session", Context.MODE_PRIVATE).byAccount()
                    val token = prefs.getString("token", null)
                    if (token.isNullOrBlank() || !prefs.getBoolean("setting_notifications", true)) {
                        cancelNext()
                        stopSelf()
                    } else {
                        try { QinblyNotifyWorker.pollOnce(applicationContext) } catch (_: Exception) { }
                        scheduleNext()
                    }
                } catch (_: Exception) {
                    scheduleNext()
                } finally {
                    if (lock.isHeld) lock.release()
                    polling.set(false)
                }
            }.start()
        }
        return START_STICKY
    }

    private fun buildNotification(text: String): Notification {
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
        return androidx.core.app.NotificationCompat.Builder(this, CHANNEL)
            .setSmallIcon(R.drawable.ic_stat_qinbly)
            .setContentTitle("QINBLY")
            .setContentText(text)
            .setContentIntent(open)
            .setOngoing(true)
            .setSilent(true)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_MIN)
            .setVisibility(androidx.core.app.NotificationCompat.VISIBILITY_SECRET)
            .setCategory(androidx.core.app.NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun startAsForeground(text: String) {
        val notification = buildNotification(text)
        if (android.os.Build.VERSION.SDK_INT >= 34) {
            startForeground(NOTIFICATION_ID, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun nextIntent(): PendingIntent =
        PendingIntent.getService(this, 7003, Intent(this, QinblyNotifyService::class.java), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

    /** Alarme que acorda o celular (mesmo em soneca) para a próxima consulta. */
    private fun scheduleNext() {
        try {
            val alarms = getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
            alarms.setAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + POLL_MS, nextIntent())
        } catch (_: Exception) { }
    }

    private fun cancelNext() {
        try { (getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager).cancel(nextIntent()) } catch (_: Exception) { }
    }

    override fun onDestroy() {
        cancelNext()
        super.onDestroy()
    }

    companion object {
        private const val CHANNEL = "qinbly_background"
        private const val NOTIFICATION_ID = 7010
        private const val POLL_MS = 40_000L

        /**
         * O serviço fixo foi desligado de propósito: ele obrigava o Android a mostrar um aviso permanente na barra.
         * Chamar isto limpa o que uma versão antiga deixou (serviço, alarme e aviso fixo). Os avisos com o app fechado
         * vêm da verificação periódica do sistema (QinblyNotifyWorker).
         */
        fun start(context: Context) {
            try {
                context.stopService(Intent(context, QinblyNotifyService::class.java))
                (context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager).cancel(
                    PendingIntent.getService(context, 7003, Intent(context, QinblyNotifyService::class.java), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
                )
                (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancel(NOTIFICATION_ID)
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
