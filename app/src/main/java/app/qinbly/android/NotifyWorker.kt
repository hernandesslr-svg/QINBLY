package app.qinbly.android

import android.app.ActivityManager
import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit

/**
 * Avisos com o app fechado. Não é push de verdade: o Android acorda o app a cada ~15 minutos (mínimo permitido)
 * e ele consulta novidades. Com o app aberto, quem avisa é o próprio app (a cada 15 s), então aqui não repete.
 */
class QinblyNotifyWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        val ctx = applicationContext
        val prefs = ctx.getSharedPreferences("qinbly_session", Context.MODE_PRIVATE).byAccount()
        val token = prefs.getString("token", null)
        if (token.isNullOrBlank() || !prefs.getBoolean("setting_notifications", true)) return Result.success()
        if (isAppInForeground(ctx)) return Result.success()
        QinblyApi.initialize(ctx)
        return try {
            val told = prefs.getStringSet("bg_notified", emptySet()).orEmpty().toMutableSet()
            val firstRun = !prefs.getBoolean("bg_notified_init", false)
            val seen = prefs.getStringSet("notif_seen_ids", emptySet()).orEmpty()
            var shown = 0

            val notifications = QinblyApi.getNotifications(token).filter { it.readAt.isBlank() && it.kind != "message" && it.id !in seen }
            for (n in notifications) {
                if (n.id in told) continue
                told += n.id
                if (!firstRun && shown < 3 && kindAllowed(prefs, n.kind)) {
                    qinblyNotify(ctx, n.actorDisplayName.ifBlank { n.actorUsername.ifBlank { "Qinbly" } }, qinblyNotificationText(n.kind), n.id.hashCode())
                    shown++
                }
            }

            if (prefs.getBoolean("setting_notice_messages", true)) {
                for (c in QinblyApi.getInbox(token).filter { it.unreadCount > 0 }) {
                    val key = "msg:" + c.username + ":" + c.createdAt
                    if (key in told) continue
                    told += key
                    if (!firstRun && shown < 3) {
                        qinblyNotify(ctx, c.displayName.ifBlank { c.username }, c.body.take(80).ifBlank { "Nova mensagem" }, key.hashCode())
                        shown++
                    }
                }
            }

            prefs.edit().putStringSet("bg_notified", told.toList().takeLast(200).toSet()).putBoolean("bg_notified_init", true).apply()
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }

    private fun kindAllowed(sp: android.content.SharedPreferences, kind: String): Boolean = when (kind) {
        "like" -> sp.getBoolean("setting_notice_likes", true)
        "comment", "reply" -> sp.getBoolean("setting_notice_comments", true)
        "mention", "mention_comment", "tag" -> sp.getBoolean("setting_notice_mentions", true)
        "follow", "follow_request", "follow_accepted" -> sp.getBoolean("setting_notice_followers", true)
        else -> sp.getBoolean("setting_notice_system", true)
    }

    private fun isAppInForeground(ctx: Context): Boolean {
        val am = ctx.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager ?: return false
        return am.runningAppProcesses?.any {
            it.processName == ctx.packageName && it.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
        } == true
    }

    companion object {
        /** Liga a verificação periódica (a cada 15 min, só com internet). Chamar de novo não duplica. */
        fun schedule(context: Context) {
            try {
                val request = PeriodicWorkRequestBuilder<QinblyNotifyWorker>(15, TimeUnit.MINUTES)
                    .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                    .build()
                WorkManager.getInstance(context).enqueueUniquePeriodicWork("qinbly_notify", ExistingPeriodicWorkPolicy.KEEP, request)
            } catch (_: Exception) { }
        }
    }
}
