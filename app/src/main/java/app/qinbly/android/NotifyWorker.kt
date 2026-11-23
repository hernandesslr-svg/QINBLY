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
        // Aviso de nova versão do app: roda mesmo sem conta logada, respeitando "Notificações" e "Avisos do sistema".
        if (prefs.getBoolean("setting_notifications", true) && prefs.getBoolean("setting_notice_system", true)) {
            checkAppUpdate(ctx)
        }
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
                    qinblyNotify(ctx, n.actorDisplayName.ifBlank { n.actorUsername.ifBlank { "Qinbly" } }, qinblyNotificationText(n.kind), n.id.hashCode(), n.kind, n.entityId, n.actorUsername)
                    shown++
                }
            }

            if (prefs.getBoolean("setting_notice_messages", true)) {
                for (c in QinblyApi.getInbox(token).filter { it.unreadCount > 0 }) {
                    val key = "msg:" + c.username + ":" + c.createdAt
                    if (key in told) continue
                    told += key
                    if (!firstRun && shown < 3) {
                        qinblyNotify(ctx, c.displayName.ifBlank { c.username }, c.body.take(80).ifBlank { "Nova mensagem" }, key.hashCode(), "message", "", c.username)
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

    /**
     * Consulta a última versão publicada no GitHub (no máximo 1 vez por hora) e avisa uma única vez por versão nova.
     * O código da versão segue a mesma conta do workflow: major*1.000.000 + minor*10.000 + patch.
     */
    private fun checkAppUpdate(ctx: Context) {
        try {
            val sp = ctx.getSharedPreferences("qinbly_update_check", Context.MODE_PRIVATE)
            val now = System.currentTimeMillis()
            if (now - sp.getLong("last_check", 0L) < TimeUnit.MINUTES.toMillis(10)) return
            sp.edit().putLong("last_check", now).apply()
            val connection = (java.net.URL("https://api.github.com/repos/hernandesslr-svg/QINBLY/releases/latest").openConnection() as java.net.HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 12000
                readTimeout = 12000
                setRequestProperty("Accept", "application/vnd.github+json")
                setRequestProperty("User-Agent", "QINBLY-Android-Updater")
            }
            val tag = try {
                if (connection.responseCode !in 200..299) return
                org.json.JSONObject(connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }).optString("tag_name")
            } finally {
                connection.disconnect()
            }
            val m = Regex("^v(\\d+)\\.(\\d+)\\.(\\d+)$").matchEntire(tag) ?: return
            val latestCode = m.groupValues[1].toInt() * 1000000 + m.groupValues[2].toInt() * 10000 + m.groupValues[3].toInt()
            if (latestCode <= BuildConfig.VERSION_CODE || latestCode == sp.getInt("notified_code", 0)) return
            if (qinblyNotifyUpdate(ctx, tag.removePrefix("v"))) sp.edit().putInt("notified_code", latestCode).apply()
        } catch (_: Exception) { }
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
                // Uma checagem logo ao abrir o app, sem esperar o ciclo de 15 minutos.
                WorkManager.getInstance(context).enqueueUniqueWork(
                    "qinbly_notify_now", androidx.work.ExistingWorkPolicy.REPLACE,
                    androidx.work.OneTimeWorkRequestBuilder<QinblyNotifyWorker>()
                        .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()).build()
                )
            } catch (_: Exception) { }
        }
    }
}

/** Aviso "Nova atualização disponível". Ao tocar, abre a tela de atualização do app. Retorna false se não pôde mostrar. */
internal fun qinblyNotifyUpdate(context: Context, version: String = ""): Boolean {
    return try {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        qinblyEnsureUpdateChannel(manager)
        if (android.os.Build.VERSION.SDK_INT >= 33 &&
            androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) return false
        val open = android.app.PendingIntent.getActivity(
            context, 7001, android.content.Intent(context, RemoteUiActivity::class.java).addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK),
            android.app.PendingIntent.FLAG_IMMUTABLE or android.app.PendingIntent.FLAG_UPDATE_CURRENT
        )
        val updateText = (if (version.isNotBlank()) "A versão $version do QINBLY está pronta. " else "A versão mais recente do QINBLY está pronta. ") + "Toque para atualizar."
        val notification = androidx.core.app.NotificationCompat.Builder(context, "qinbly_update_v2")
            .setSmallIcon(R.drawable.ic_stat_qinbly)
            .setContentTitle("Nova atualização disponível")
            .setContentText(updateText)
            .setStyle(androidx.core.app.NotificationCompat.BigTextStyle().bigText(updateText))
            .setContentIntent(open)
            .setAutoCancel(true)
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
            .setDefaults(androidx.core.app.NotificationCompat.DEFAULT_ALL)
            .build()
        manager.notify(7001, notification)
        true
    } catch (_: Exception) { false }
}

/** Canal das atualizações do app: importância alta, com som e vibração (o canal antigo `qinbly_update` era discreto). */
internal fun qinblyEnsureUpdateChannel(manager: android.app.NotificationManager) {
    if (android.os.Build.VERSION.SDK_INT < 26) return
    val channel = android.app.NotificationChannel("qinbly_update_v2", "Atualizações do app", android.app.NotificationManager.IMPORTANCE_HIGH).apply {
        description = "Aviso quando sai uma versão nova do QINBLY"
        enableVibration(true)
        setSound(
            android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION),
            android.media.AudioAttributes.Builder().setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION).setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION).build()
        )
    }
    manager.createNotificationChannel(channel)
}
