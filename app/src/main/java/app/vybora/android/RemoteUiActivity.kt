package app.vybora.android

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
    private val prefs by lazy { getSharedPreferences("vybora_session", MODE_PRIVATE) }
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ContextCompat.registerReceiver(
            this, downloadReceiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
            ContextCompat.RECEIVER_EXPORTED
        )
        webView = WebView(this)
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
        webView.addJavascriptInterface(SessionBridge(), "VyboraNative")
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
                            "sessionStorage.setItem('vybora_token', $quoted);" +
                                "window.dispatchEvent(new Event('vybora-native-session'));",
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
    private fun showReleaseFallback() {
        val html = """
            <!doctype html><html lang="pt-BR"><meta name="viewport" content="width=device-width,initial-scale=1">
            <meta charset="utf-8"><title>Atualizações do QINBLY</title>
            <style>body{background:#050711;color:#f5f5fb;font:16px Arial,sans-serif;padding:20px}
            .card{background:#111528;border:1px solid #292d43;border-radius:20px;padding:24px;margin-top:24px}
            button{background:#8b35ff;color:white;border:0;border-radius:12px;padding:16px;font-weight:bold}
            p{line-height:1.5;color:#c8ccdb}</style>
            <h1>Atualizações do QINBLY</h1><section class="card">
            <h3>Não foi possível verificar atualizações</h3>
            <p>A consulta ao GitHub está temporariamente indisponível. Nenhuma atualização nova foi confirmada.</p>
            <button onclick="VyboraNative.retryUpdateCheck()">Tentar novamente</button>
            </section></html>
        """.trimIndent()
        runOnUiThread { webView.loadDataWithBaseURL("https://github.com/", html, "text/html", "UTF-8", null) }
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
                        if (status == 403 || status == 429) {
                            // GitHub REST API can be rate-limited on shared mobile IPs.
                            // Fall back to the public release download URL without requiring API access.
                            return@Thread showReleaseFallback()
                        }
                        throw IllegalStateException("GitHub respondeu HTTP $status.")
                    }
                    JSONObject(response)
                } finally {
                    connection.disconnect()
                }
                val tag = release.optString("tag_name", "Versão mais recente")
                val notes = release.optString("body").takeUnless { it.isBlank() || it == "null" } ?: "Melhorias e correções do QINBLY."
                val assets = release.optJSONArray("assets") ?: org.json.JSONArray()
                var apkUrl = ""
                for (i in 0 until assets.length()) {
                    val asset = assets.optJSONObject(i) ?: continue
                    if (asset.optString("name") == "vybora-release.apk") {
                        apkUrl = asset.optString("browser_download_url")
                        break
                    }
                }
                if (apkUrl.isBlank()) throw IllegalStateException("A publicação mais recente ainda não contém o arquivo de atualização.")
                val tagJs = JSONObject.quote(tag)
                val notesJs = JSONObject.quote(notes)
                val urlJs = JSONObject.quote(apkUrl)
                val html = """
                    <!doctype html><html lang="pt-BR"><meta name="viewport" content="width=device-width,initial-scale=1">
                    <meta charset="utf-8"><title>Atualizações do QINBLY</title>
                    <style>
                    *{box-sizing:border-box}body{margin:0;padding:24px 18px;background:#050711;color:#f5f5fb;font:16px Arial,sans-serif}
                    .brand{text-align:center;margin:28px 0 30px}.logo{display:inline-flex;width:76px;height:76px;align-items:center;justify-content:center;border-radius:24px;background:linear-gradient(135deg,#ff2a9d,#8b35ff);font-size:42px;font-weight:800}
                    h1{font-size:27px;margin:14px 0 8px}.muted{color:#adb2cb;line-height:1.5}.card{background:#111528;border:1px solid #292d43;border-radius:22px;padding:22px;margin-top:22px}
                    .version{color:#c58bff;font-weight:700;font-size:19px;margin:10px 0 18px}.btn{display:block;width:100%;border:0;border-radius:14px;padding:17px;background:linear-gradient(90deg,#8b35ff,#ff2a9d);color:white;font-size:16px;font-weight:700;margin-top:22px}
                    .error{color:#ff6ba9;white-space:pre-wrap;overflow-wrap:anywhere}.notes{white-space:pre-wrap;overflow-wrap:anywhere;line-height:1.5;color:#d7d9e8}
                    </style><body><div class="brand"><div class="logo">V</div><h1>QINBLY</h1><div class="muted">Atualização do aplicativo</div></div>
                    <section class="card"><div class="muted">Versão disponível</div><div class="version" id="version"></div><div class="muted">Novidades</div><p class="notes" id="notes"></p>
                    <button class="btn" id="updateBtn" onclick="this.disabled=true;this.textContent='Iniciando download…';VyboraNative.downloadAndInstall(apkUrl)">Baixar e instalar atualização</button>
                    <p class="muted" id="updateStatus" role="status"></p>
                    <p class="muted">O Android solicitará confirmação antes de instalar. Suas informações não são apagadas pela atualização normal.</p></section>
                    <script>const apkUrl=$urlJs;document.getElementById('version').textContent=$tagJs;document.getElementById('notes').textContent=$notesJs;window.vyboraOtaNativeStatus=function(message,isError){document.getElementById('updateStatus').textContent=message;document.getElementById('updateBtn').disabled=!isError;document.getElementById('updateBtn').textContent=isError?'Tentar novamente':message;};</script>
                    </body></html>
                """.trimIndent()
                runOnUiThread { webView.loadDataWithBaseURL("https://github.com/", html, "text/html", "UTF-8", null) }
            } catch (e: Exception) {
                val message = JSONObject.quote(e.message ?: "Não foi possível consultar o GitHub Releases.")
                val html = """
                    <!doctype html><html lang="pt-BR"><meta name="viewport" content="width=device-width,initial-scale=1"><meta charset="utf-8">
                    <style>body{background:#050711;color:#f5f5fb;font:16px Arial;padding:28px 20px}.card{background:#111528;border:1px solid #292d43;border-radius:20px;padding:22px}.muted{color:#adb2cb;line-height:1.5}.error{color:#ff6ba9;overflow-wrap:anywhere}button{padding:14px 18px;background:#8b35ff;color:white;border:0;border-radius:12px;font-weight:bold}</style>
                    <h1>Atualizações do QINBLY</h1><div class="card"><p class="error" id="error"></p><p class="muted">Não foi possível consultar as versões no GitHub.</p><button onclick="location.reload()">Tentar novamente</button><p><a style="color:#c58bff" href="https://github.com/hernandesslr-svg/QINBLY/releases">Abrir downloads no GitHub</a></p></div>
                    <script>document.getElementById('error').textContent=$message;</script></html>
                """.trimIndent()
                runOnUiThread { if (::webView.isInitialized) webView.loadDataWithBaseURL("https://github.com/", html, "text/html", "UTF-8", null) }
            }
        }.start()
    }

    private fun notifyWeb(message: String, isError: Boolean) {
        if (!::webView.isInitialized) return
        val quoted = JSONObject.quote(message)
        runOnUiThread {
            if (::webView.isInitialized) {
                webView.evaluateJavascript(
                    "window.vyboraOtaNativeStatus && window.vyboraOtaNativeStatus($quoted, $isError);",
                    null
                )
            }
        }
    }

    override fun onDestroy() {
        handler.removeCallbacks(monitor)
        try { unregisterReceiver(downloadReceiver) } catch (_: Exception) { }
        if (::webView.isInitialized) {
            webView.stopLoading()
            webView.removeJavascriptInterface("VyboraNative")
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
            runOnUiThread {
                try {
                    val uri = Uri.parse(apkUrl.orEmpty())
                    if (uri.scheme != "https" || uri.host != "github.com" || !uri.path.orEmpty().contains("/releases/download/") || !uri.path.orEmpty().endsWith("/vybora-release.apk")) {
                        notifyWeb("O endereço do APK não passou na validação de segurança.", true)
                        return@runOnUiThread
                    }
                    val request = DownloadManager.Request(uri)
                        .setTitle("Atualização do QINBLY")
                        .setDescription("Baixando a versão mais recente")
                        .setMimeType("application/vnd.android.package-archive")
                        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                        .setAllowedOverMetered(true)
                        .setAllowedOverRoaming(false)
                        .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "QINBLY-update-" + System.currentTimeMillis() + ".apk")
                    val manager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
                    pendingDownloadId = manager.enqueue(request)
                    prefs.edit().putLong("pending_update_download_id", pendingDownloadId).apply()
                    notifyWeb("Download iniciado. Acompanhando o progresso…", false)
                    handler.removeCallbacks(monitor)
                    handler.postDelayed(monitor, 700L)
                } catch (_: Exception) {
                    notifyWeb("Não foi possível iniciar o download. Verifique a conexão e tente novamente.", true)
                }
            }
        }
    }

    companion object {
        private const val OTA_HOST = "vybora-production.up.railway.app"
        private const val OTA_ORIGIN = "https://vybora-production.up.railway.app"
    }
}
