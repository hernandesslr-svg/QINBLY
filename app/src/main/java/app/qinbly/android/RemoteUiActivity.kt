package app.qinbly.android

import android.app.DownloadManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageInstaller
import android.net.Uri
import android.os.Bundle
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.os.Environment
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import org.json.JSONObject

/**
 * Client for the self-hosted QINBLY remote interface.
 * APK downloads are handled by Android; installation is handed to the system installer.
 */
class RemoteUiActivity : ComponentActivity() {
    private lateinit var webView: WebView
    private val prefs by lazy { getSharedPreferences("qinbly_session", MODE_PRIVATE).byAccount() }
    private var pendingDownloadId: Long = -1L

    private val handler = Handler(Looper.getMainLooper())
    private val monitor = object : Runnable {
        override fun run() {
            checkDownload()
            if (prefs.getLong("pending_update_download_id", -1L) > 0L) handler.postDelayed(this, 1500L)
        }
    }

    private fun checkDownload() {
        val id = prefs.getLong("pending_update_download_id", -1L)
        if (id <= 0L) return
        val manager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        try {
            manager.query(DownloadManager.Query().setFilterById(id))?.use { cursor ->
                if (!cursor.moveToFirst()) {
                    prefs.edit().remove("pending_update_download_id").apply()
                    notifyWeb("Download não encontrado. Tente novamente.", true)
                    return
                }
                when (cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))) {
                    DownloadManager.STATUS_SUCCESSFUL -> {
                        prefs.edit().remove("pending_update_download_id").apply()
                        val uri = manager.getUriForDownloadedFile(id)
                        if (uri != null) openInstallerOrRequestPermission(uri)
                        else notifyWeb("Arquivo APK indisponível. Tente novamente.", true)
                    }
                    DownloadManager.STATUS_FAILED -> {
                        val reason = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_REASON))
                        prefs.edit().remove("pending_update_download_id").apply()
                        notifyWeb("Falha no download (código $reason). Tente novamente.", true)
                    }
                    DownloadManager.STATUS_RUNNING -> {
                        val total = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                        val done = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                        notifyWeb(if (total > 0) "Baixando APK: ${(done * 100 / total).coerceIn(0, 100)}%" else "Baixando APK…", false)
                    }
                    else -> notifyWeb("Aguardando download…", false)
                }
            }
        } catch (e: Exception) {
            prefs.edit().remove("pending_update_download_id").apply()
            notifyWeb("Erro ao verificar download. Tente novamente.", true)
        }
    }

    private val downloadReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == DownloadManager.ACTION_DOWNLOAD_COMPLETE) checkDownload()
        }
    }

    /**
     * Retoma a instalação depois que o usuário autoriza o QINBLY a instalar APKs.
     * O URI é mantido nas preferências porque abrir as configurações pausa esta Activity.
     */
    override fun onResume() {
        super.onResume()
        if (prefs.getLong("pending_update_download_id", -1L) > 0L) {
            checkDownload()
            handler.removeCallbacks(monitor)
            if (prefs.getLong("pending_update_download_id", -1L) > 0L) handler.postDelayed(monitor, 1500L)
        }
        val pendingUrl = prefs.getString("pending_update_apk_url", null)
        if (pendingUrl != null && (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || packageManager.canRequestPackageInstalls())) {
            prefs.edit().remove("pending_update_apk_url").apply()
            startInAppUpdate(pendingUrl)
            return
        }
        val pending = prefs.getString("pending_update_apk_uri", null) ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !packageManager.canRequestPackageInstalls()) return
        prefs.edit().remove("pending_update_apk_uri").apply()
        try {
            startActivity(Intent(Intent.ACTION_INSTALL_PACKAGE).apply {
                data = Uri.parse(pending)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            })
        } catch (_: Exception) {
            notifyWeb("O APK foi baixado, mas o Android não conseguiu abrir o instalador. Toque em Atualizar novamente.", true)
        }
    }

    private fun openInstallerOrRequestPermission(apkUri: Uri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !packageManager.canRequestPackageInstalls()) {
            prefs.edit().putString("pending_update_apk_uri", apkUri.toString()).apply()
            notifyWeb("Autorize 'Instalar apps desconhecidos' para o QINBLY. Ao voltar, a instalação será aberta automaticamente.", false)
            try {
                startActivity(Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:$packageName")))
            } catch (_: Exception) {
                notifyWeb("O APK foi baixado. Ative a permissão de instalar apps desconhecidos para o QINBLY e volte ao aplicativo.", true)
            }
            return
        }
        try {
            startActivity(Intent(Intent.ACTION_INSTALL_PACKAGE).apply {
                data = apkUri
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            })
        } catch (_: Exception) {
            notifyWeb("O APK foi baixado, mas não foi possível abrir o instalador do Android. Toque em Atualizar novamente.", true)
        }
    }

    private val installStatusReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action != ACTION_INSTALL_STATUS) return
            when (val status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE)) {
                PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                    @Suppress("DEPRECATION")
                    val confirm = if (Build.VERSION.SDK_INT >= 33) intent.getParcelableExtra(Intent.EXTRA_INTENT, Intent::class.java)
                    else intent.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)
                    if (confirm != null) {
                        try { startActivity(confirm) } catch (_: Exception) {
                            notifyWeb("Não foi possível abrir a confirmação de instalação. Toque em Atualizar novamente.", true)
                        }
                    }
                }
                PackageInstaller.STATUS_SUCCESS -> notifyWeb("Atualização instalada.", false)
                PackageInstaller.STATUS_FAILURE_ABORTED -> notifyWeb("Instalação cancelada. Toque em Atualizar para tentar de novo.", true)
                else -> {
                    val detail = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE).orEmpty()
                    notifyWeb("Falha ao instalar (código $status). $detail".trim(), true)
                }
            }
        }
    }

    /**
     * Atualização dentro do app: baixa o APK direto para a sessão do instalador do Android
     * (sem gravar arquivo na pasta Downloads) e pede a confirmação do sistema ao final.
     */
    private fun startInAppUpdate(apkUrl: String) {
        val uri = Uri.parse(apkUrl)
        if (uri.scheme != "https" || uri.host != "github.com" || !uri.path.orEmpty().contains("/releases/download/") || !uri.path.orEmpty().endsWith("/qinbly-release.apk")) {
            notifyWeb("O endereço do APK não passou na validação de segurança.", true)
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !packageManager.canRequestPackageInstalls()) {
            prefs.edit().putString("pending_update_apk_url", apkUrl).apply()
            notifyWeb("Autorize 'Instalar apps desconhecidos' para o QINBLY. Ao voltar, a atualização continua sozinha.", false)
            try {
                startActivity(Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:$packageName")))
            } catch (_: Exception) {
                notifyWeb("Ative a permissão de instalar apps desconhecidos para o QINBLY e volte ao aplicativo.", true)
            }
            return
        }
        notifyWeb("Baixando atualização…", false)
        Thread {
            var sessionId = -1
            val installer = packageManager.packageInstaller
            try {
                val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    params.setRequireUserAction(PackageInstaller.SessionParams.USER_ACTION_NOT_REQUIRED)
                }
                sessionId = installer.createSession(params)
                val connection = (java.net.URL(apkUrl).openConnection() as java.net.HttpURLConnection).apply {
                    connectTimeout = 15000
                    readTimeout = 30000
                    instanceFollowRedirects = true
                    setRequestProperty("User-Agent", "QINBLY-Android-Updater")
                }
                try {
                    if (connection.responseCode !in 200..299) throw IllegalStateException("HTTP " + connection.responseCode)
                    val total = connection.contentLengthLong
                    installer.openSession(sessionId).use { session ->
                        session.openWrite("qinbly-release.apk", 0, total.takeIf { it > 0 } ?: -1L).use { out ->
                            connection.inputStream.use { input ->
                                val buffer = ByteArray(64 * 1024)
                                var done = 0L
                                var lastPercent = -1
                                while (true) {
                                    val n = input.read(buffer)
                                    if (n < 0) break
                                    out.write(buffer, 0, n)
                                    done += n
                                    if (total > 0) {
                                        val percent = (done * 100 / total).toInt().coerceIn(0, 100)
                                        if (percent != lastPercent && percent % 3 == 0) {
                                            lastPercent = percent
                                            notifyWeb("Baixando atualização: $percent%", false)
                                        }
                                    }
                                }
                            }
                            session.fsync(out)
                        }
                        notifyWeb("Preparando instalação…", false)
                        qinblyMarkRelaunchAfterUpdate(this@RemoteUiActivity)
                        val callback = Intent(ACTION_INSTALL_STATUS).setPackage(packageName)
                        val flags = PendingIntent.FLAG_UPDATE_CURRENT or
                            (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else 0)
                        val pending = PendingIntent.getBroadcast(this, sessionId, callback, flags)
                        session.commit(pending.intentSender)
                    }
                } finally {
                    connection.disconnect()
                }
            } catch (e: Exception) {
                if (sessionId >= 0) try { installer.abandonSession(sessionId) } catch (_: Exception) { }
                // Se o instalador interno falhar, usa o caminho antigo (download + instalador do Android).
                runOnUiThread { downloadViaManager(apkUrl) }
            }
        }.start()
    }

    private fun downloadViaManager(apkUrl: String) {
        try {
            val uri = Uri.parse(apkUrl)
            val request = DownloadManager.Request(uri)
                .setTitle("Atualização do QINBLY")
                .setDescription("Baixando a versão mais recente")
                .setMimeType("application/vnd.android.package-archive")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(false)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "QINBLY-update-" + System.currentTimeMillis() + ".apk")
            val manager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            qinblyMarkRelaunchAfterUpdate(this)
            pendingDownloadId = manager.enqueue(request)
            prefs.edit().putLong("pending_update_download_id", pendingDownloadId).apply()
            notifyWeb("Download iniciado. Acompanhando o progresso…", false)
            handler.removeCallbacks(monitor)
            handler.postDelayed(monitor, 700L)
        } catch (_: Exception) {
            notifyWeb("Não foi possível iniciar o download. Verifique a conexão e tente novamente.", true)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ContextCompat.registerReceiver(
            this, downloadReceiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
            ContextCompat.RECEIVER_EXPORTED
        )
        ContextCompat.registerReceiver(
            this, installStatusReceiver, IntentFilter(ACTION_INSTALL_STATUS),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        webView = WebView(this)
        webView.setBackgroundColor(0xFF171719.toInt())
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = false
            allowContentAccess = false
            mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
            javaScriptCanOpenWindowsAutomatically = false
            setSupportMultipleWindows(false)
            safeBrowsingEnabled = true
        }
        webView.addJavascriptInterface(SessionBridge(), "QinblyNative")
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                val uri = request.url
                if (uri.scheme == "https" && uri.host == OTA_HOST) return false
                if (uri.scheme == "https" || uri.scheme == "http") {
                    startActivity(Intent(Intent.ACTION_VIEW, uri))
                }
                return true
            }

            override fun onPageFinished(view: WebView, url: String?) {
                super.onPageFinished(view, url)
                if (url?.startsWith(OTA_ORIGIN + "/remote-ui/") == true) {
                    val token = prefs.getString("token", "").orEmpty()
                    if (token.isNotBlank()) {
                        val quoted = JSONObject.quote(token)
                        view.evaluateJavascript(
                            "sessionStorage.setItem('qinbly_token', $quoted);" +
                                "window.dispatchEvent(new Event('qinbly-native-session'));",
                            null
                        )
                    }
                }
            }
        }
        setContentView(webView)
        loadLatestReleasePage()
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) webView.goBack() else finish()
            }
        })
    }

    /**
     * A tela de atualização é montada localmente e consulta o GitHub Releases.
     * Assim, a atualização do APK não depende da disponibilidade do backend Railway.
     */
    private fun updatePage(body: String, script: String = ""): String = """
        <!doctype html><html lang="pt-BR"><meta name="viewport" content="width=device-width,initial-scale=1">
        <meta charset="utf-8"><title>Atualizações do QINBLY</title>
        <style>
        *{box-sizing:border-box}
        body{margin:0;padding:34px 18px 28px;background:#171719;color:#f2f4f8;font:15px/1.5 sans-serif}
        .brand{text-align:center;margin:10px 0 22px}
        .logo{display:inline-flex;width:64px;height:64px;align-items:center;justify-content:center;border-radius:20px;background:#7C4DFF;color:#fff;font-size:32px;font-weight:600}
        .word{font-size:22px;font-weight:600;color:#7C4DFF;margin-top:8px;letter-spacing:1px}
        .mut{color:#f2f4f8;font-size:14px}
        .card{background:#262628;border:1px solid #2e2e30;border-radius:18px;padding:18px}
        .row{display:flex;justify-content:space-between;padding:3px 0}
        hr{border:0;border-top:1px solid #3a3b40;margin:14px 0}
        b{font-weight:600}
        .notes{white-space:pre-wrap;overflow-wrap:anywhere;margin:6px 0 0}
        .btn{display:block;width:100%;border:0;border-radius:14px;padding:15px;background:#2F68E0;color:#fff;font-size:15px;font-weight:600;margin-top:18px}
        .btn.sec{background:#2e2e30;color:#f2f4f8}
        .btn:disabled{opacity:.7}
        .bar{height:8px;border-radius:4px;background:#3a3b40;margin-top:16px;overflow:hidden;display:none}
        .bar div{height:8px;width:0;background:#2F68E0}
        .small{font-size:13px;margin:14px 0 0}
        .center{text-align:center}
        a{color:#f2f4f8}
        </style><body>
        <div class="brand"><div class="logo">Q</div><div class="word">QINBLY</div><div class="mut">Atualização do aplicativo</div></div>
        $body
        <script>$script</script>
        </body></html>
    """.trimIndent()

    private fun showReleasePage(html: String) {
        runOnUiThread { if (::webView.isInitialized) webView.loadDataWithBaseURL("https://github.com/", html, "text/html", "UTF-8", null) }
    }

    private fun showUpdateError(message: String) {
        val quoted = JSONObject.quote(message)
        showReleasePage(updatePage("""
            <section class="card center">
            <b>Não foi possível verificar atualizações</b>
            <p class="mut" id="msg"></p>
            <button class="btn" onclick="QinblyNative.retryUpdateCheck()">Tentar novamente</button>
            <p class="small"><a href="https://github.com/hernandesslr-svg/QINBLY/releases">Abrir downloads no GitHub</a></p>
            </section>
        """.trimIndent(), "document.getElementById('msg').textContent=$quoted;"))
    }

    private fun showReleaseFallback() {
        showUpdateError("A consulta ao GitHub está temporariamente indisponível. Nenhuma atualização nova foi confirmada.")
    }

    private fun versionCodeOf(tag: String): Int {
        val m = Regex("^v?(\\d+)\\.(\\d+)\\.(\\d+)$").matchEntire(tag.trim()) ?: return 0
        return m.groupValues[1].toInt() * 1000000 + m.groupValues[2].toInt() * 10000 + m.groupValues[3].toInt()
    }

    private fun loadLatestReleasePage() {
        Thread {
            try {
                val connection = (java.net.URL("https://api.github.com/repos/hernandesslr-svg/QINBLY/releases/latest")
                    .openConnection() as java.net.HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 12000
                    readTimeout = 12000
                    setRequestProperty("Accept", "application/vnd.github+json")
                    setRequestProperty("X-GitHub-Api-Version", "2022-11-28")
                    setRequestProperty("User-Agent", "QINBLY-Android-Updater")
                }
                val release = try {
                    val status = connection.responseCode
                    val stream = if (status in 200..299) connection.inputStream else connection.errorStream
                    val response = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
                    if (status !in 200..299) {
                        // A API do GitHub pode limitar IPs compartilhados de rede móvel.
                        if (status == 403 || status == 429) return@Thread showReleaseFallback()
                        throw IllegalStateException("GitHub respondeu HTTP $status.")
                    }
                    JSONObject(response)
                } finally {
                    connection.disconnect()
                }
                val tag = release.optString("tag_name", "")
                val shownVersion = tag.removePrefix("v").ifBlank { "mais recente" }
                val installedName = BuildConfig.VERSION_NAME
                val latestCode = versionCodeOf(tag)
                if (latestCode in 1..BuildConfig.VERSION_CODE) {
                    val nameJs = JSONObject.quote(installedName)
                    showReleasePage(updatePage("""
                        <section class="card center">
                        <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="#f2f4f8" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="9"/><path d="M8.5 12.5l2.5 2.5 4.5-5"/></svg>
                        <p><b>Você está na versão mais recente</b></p>
                        <p class="mut" id="cur"></p>
                        <button class="btn sec" onclick="QinblyNative.retryUpdateCheck()">Verificar de novo</button>
                        </section>
                    """.trimIndent(), "document.getElementById('cur').textContent=$nameJs;"))
                    return@Thread
                }
                val notes = release.optString("body").takeUnless { it.isBlank() || it == "null" } ?: "Melhorias e correções do QINBLY."
                val assets = release.optJSONArray("assets") ?: org.json.JSONArray()
                var apkUrl = ""
                for (i in 0 until assets.length()) {
                    val asset = assets.optJSONObject(i) ?: continue
                    if (asset.optString("name") == "qinbly-release.apk") {
                        apkUrl = asset.optString("browser_download_url")
                        break
                    }
                }
                if (apkUrl.isBlank()) throw IllegalStateException("A publicação mais recente ainda não contém o arquivo de atualização.")
                val verJs = JSONObject.quote(shownVersion)
                val curJs = JSONObject.quote(installedName)
                val notesJs = JSONObject.quote(notes)
                val urlJs = JSONObject.quote(apkUrl)
                showReleasePage(updatePage("""
                    <section class="card">
                    <div class="row"><span class="mut">Instalada</span><span id="cur"></span></div>
                    <div class="row"><span class="mut">Disponível</span><b id="ver"></b></div>
                    <hr>
                    <b>Novidades</b>
                    <p class="notes" id="notes"></p>
                    <div class="bar" id="bar"><div id="fill"></div></div>
                    <button class="btn" id="updateBtn" onclick="this.disabled=true;this.textContent='Iniciando…';QinblyNative.downloadAndInstall(apkUrl)">Atualizar agora</button>
                    <p class="small" id="updateStatus" role="status"></p>
                    <p class="small mut">Quando terminar de baixar, a instalação começa em seguida. O Android pode pedir uma confirmação. Seus dados não são apagados.</p>
                    </section>
                """.trimIndent(), """
                    var apkUrl=$urlJs;
                    document.getElementById('cur').textContent=$curJs;
                    document.getElementById('ver').textContent=$verJs;
                    document.getElementById('notes').textContent=$notesJs;
                    window.qinblyOtaNativeStatus=function(message,isError){
                      var btn=document.getElementById('updateBtn');
                      document.getElementById('updateStatus').textContent=message;
                      var m=/(\d+)%/.exec(message);
                      var bar=document.getElementById('bar');
                      if(m&&!isError){bar.style.display='block';document.getElementById('fill').style.width=m[1]+'%';}
                      else if(isError){bar.style.display='none';}
                      btn.disabled=!isError;
                      btn.textContent=isError?'Tentar novamente':(m?'Baixando… '+m[1]+'%':'Atualizando…');
                    };
                """.trimIndent()))
            } catch (e: Exception) {
                showUpdateError(e.message ?: "Não foi possível consultar o GitHub Releases.")
            }
        }.start()
    }

    private fun notifyWeb(message: String, isError: Boolean) {
        if (!::webView.isInitialized) return
        val quoted = JSONObject.quote(message)
        runOnUiThread {
            if (::webView.isInitialized) {
                webView.evaluateJavascript(
                    "window.qinblyOtaNativeStatus && window.qinblyOtaNativeStatus($quoted, $isError);",
                    null
                )
            }
        }
    }

    override fun onDestroy() {
        handler.removeCallbacks(monitor)
        try { unregisterReceiver(downloadReceiver) } catch (_: Exception) { }
        try { unregisterReceiver(installStatusReceiver) } catch (_: Exception) { }
        if (::webView.isInitialized) {
            webView.stopLoading()
            webView.removeJavascriptInterface("QinblyNative")
            webView.destroy()
        }
        super.onDestroy()
    }

    private inner class SessionBridge {
        @JavascriptInterface
        fun retryUpdateCheck() { runOnUiThread { loadLatestReleasePage() } }

        @JavascriptInterface
        fun saveSession(token: String?, username: String?) {
            val safeToken = token?.trim().orEmpty()
            if (safeToken.length !in 20..10000) return
            val editor = prefs.edit().putString("token", safeToken)
            val safeUsername = username?.trim().orEmpty()
            if (safeUsername.isNotBlank() && safeUsername.length <= 24) editor.putString("username", safeUsername)
            editor.apply()
        }

        @JavascriptInterface
        fun clearSession() {
            prefs.edit().remove("token").remove("username").remove("display_name").remove("bio").apply()
        }

        @JavascriptInterface
        fun downloadAndInstall(apkUrl: String?) {
            runOnUiThread { startInAppUpdate(apkUrl.orEmpty()) }
        }
    }

    companion object {
        private const val ACTION_INSTALL_STATUS = "app.qinbly.android.UPDATE_INSTALL_STATUS"
        private const val OTA_HOST = "qinbly-production.up.railway.app"
        private const val OTA_ORIGIN = "https://qinbly-production.up.railway.app"
    }
}
