package app.qinbly.android

import android.app.ActivityManager
import android.content.Context
import androidx.work.Constraints
import androidx.work.ListenableWorker
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
    override fun doWork(): ListenableWorker.Result {
        val result = pollOnce(applicationContext)
        ensureChain(applicationContext)
        return result
    }

    companion object {
        fun pollOnce(ctx: Context): ListenableWorker.Result {
            val prefs = ctx.getSharedPreferences("qinbly_session", Context.MODE_PRIVATE).byAccount()
            // Aviso de nova versão do app: roda mesmo sem conta logada, respeitando "Notificações" e "Avisos do sistema".
            if (prefs.getBoolean("setting_notifications", true) && prefs.getBoolean("setting_notice_system", true)) {
                checkAppUpdate(ctx)
            }
            val token = prefs.getString("token", null)
            if (token.isNullOrBlank() || !prefs.getBoolean("setting_notifications", true)) return ListenableWorker.Result.success()
            if (isAppInForeground(ctx)) return ListenableWorker.Result.success()
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
                            qinblyNotify(ctx, c.displayName.ifBlank { c.username }, c.body.take(80).ifBlank { "Nova mensagem" }, key.hashCode(), "message", "", c.username, c.avatarUrl)
                            shown++
                        }
                    }
                }

                prefs.edit().putStringSet("bg_notified", told.toList().takeLast(200).toSet()).putBoolean("bg_notified_init", true).apply()
                ListenableWorker.Result.success()
            } catch (_: Exception) {
                ListenableWorker.Result.retry()
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
                var apkReady = false
                val tag = try {
                    if (connection.responseCode !in 200..299) return
                    val release = org.json.JSONObject(connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() })
                    // Só avisa depois que a compilação terminou: a versão só vale quando o APK já foi enviado ao GitHub.
                    val assets = release.optJSONArray("assets")
                    if (assets != null && !release.optBoolean("draft", false) && !release.optBoolean("prerelease", false)) {
                        for (i in 0 until assets.length()) {
                            val a = assets.optJSONObject(i) ?: continue
                            if (a.optString("name").equals("QINBLY-release.apk", ignoreCase = true) && a.optString("state", "uploaded") == "uploaded" && a.optLong("size", 1L) > 0L) apkReady = true
                        }
                    }
                    release.optString("tag_name")
                } finally {
                    connection.disconnect()
                }
                if (!apkReady) return
                val m = Regex("^v(\\d+)\\.(\\d+)\\.(\\d+)$").matchEntire(tag) ?: return
                val latestCode = m.groupValues[1].toInt() * 1000000 + m.groupValues[2].toInt() * 10000 + m.groupValues[3].toInt()
                if (latestCode <= BuildConfig.VERSION_CODE || latestCode == sp.getInt("notified_code", 0)) return
                if (qinblyNotifyUpdate(ctx, tag.removePrefix("v"))) sp.edit().putInt("notified_code", latestCode).apply()
            } catch (_: Exception) { }
        }

        /** Só o básico: curtida e comentário/resposta. Seguidor, menção, marcação etc. não geram aviso na barra. */
        private fun kindAllowed(sp: android.content.SharedPreferences, kind: String): Boolean = when (kind) {
            "like" -> sp.getBoolean("setting_notice_likes", true)
            "comment", "reply" -> sp.getBoolean("setting_notice_comments", true)
            else -> false
        }

        private fun isAppInForeground(ctx: Context): Boolean {
            val am = ctx.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager ?: return false
            return am.runningAppProcesses?.any {
                it.processName == ctx.packageName && it.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
            } == true
        }

        /**
         * O agendamento periódico do Android só roda a cada 15 min. Para as mensagens chegarem mais rápido, uma sequência de tarefas
         * de uma consulta cada (a cada ~2 min) se reagenda sozinha enquanto houver conta logada e "Notificações" ligado.
         */
        fun ensureChain(ctx: Context) {
            try {
                val wm = WorkManager.getInstance(ctx)
                val alive = listOf("qinbly_chain_a", "qinbly_chain_b").any { name ->
                    wm.getWorkInfosForUniqueWork(name).get().any { !it.state.isFinished }
                }
                if (!alive) enqueueChain(ctx, "a", 0)
            } catch (_: Exception) { }
        }

        internal fun enqueueChain(ctx: Context, slot: String, delayMinutes: Long) {
            try {
                val request = androidx.work.OneTimeWorkRequestBuilder<QinblyNotifyChainWorker>()
                    .setInitialDelay(delayMinutes, TimeUnit.MINUTES)
                    .setInputData(androidx.work.workDataOf("slot" to slot))
                    .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                    .build()
                WorkManager.getInstance(ctx).enqueueUniqueWork("qinbly_chain_$slot", androidx.work.ExistingWorkPolicy.REPLACE, request)
            } catch (_: Exception) { }
        }

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

/** Uma consulta de avisos e agenda a próxima (trocando entre dois nomes para não cancelar a si mesma). Para quando não há conta ou "Notificações" está desligado. */
class QinblyNotifyChainWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): ListenableWorker.Result {
        val ctx = applicationContext
        val prefs = ctx.getSharedPreferences("qinbly_session", Context.MODE_PRIVATE).byAccount()
        val token = prefs.getString("token", null)
        if (token.isNullOrBlank() || !prefs.getBoolean("setting_notifications", true)) return ListenableWorker.Result.success()
        try { QinblyNotifyWorker.pollOnce(ctx) } catch (_: Exception) { }
        val next = if (inputData.getString("slot") == "b") "a" else "b"
        QinblyNotifyWorker.enqueueChain(ctx, next, 2)
        return ListenableWorker.Result.success()
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
