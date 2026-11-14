package app.qinbly.android

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.temporal.ChronoUnit

private val CapBg = Color(0xFF171719)
private val CapCard = Color(0xFF232325)
private val CapField = Color(0xFF262628)
private val CapAccent = Color(0xFF1877F2)
private val CapMuted = Color(0xFFA9B5C8)
private val CapMonths = listOf("janeiro", "fevereiro", "março", "abril", "maio", "junho", "julho", "agosto", "setembro", "outubro", "novembro", "dezembro")

private fun capSess(c: Context) = c.getSharedPreferences("qinbly_session", Context.MODE_PRIVATE)
private fun capMain(block: () -> Unit) { Handler(Looper.getMainLooper()).post(block) }
private fun capLong(d: LocalDate) = "${d.dayOfMonth} de ${CapMonths[d.monthValue - 1]} de ${d.year}"
private fun capShort(i: Instant): String { val d = i.atZone(ZoneId.systemDefault()).toLocalDate(); return "%02d/%02d/%d".format(d.dayOfMonth, d.monthValue, d.year) }

internal data class QinblyCapsule(
    val id: String, val fromId: String, val toId: String, val fromName: String, val toName: String,
    val body: String, val message: String, val mediaPath: String, val mediaType: String,
    val unlockAt: Instant, val createdAt: Instant, val opened: Boolean
) {
    val closed get() = unlockAt.isAfter(Instant.now())
}

private fun fetchCapsules(token: String, uid: String): List<QinblyCapsule> {
    val a = JSONArray(QinblyApi.rest(token, "GET", "time_capsules?or=(from_user.eq.$uid,to_user.eq.$uid)&select=id,from_user,to_user,body,message,media_path,media_type,unlock_at,created_at,opened&order=unlock_at.asc&limit=200"))
    val raw = (0 until a.length()).map { a.getJSONObject(it) }
    val others = raw.flatMap { listOf(it.optString("from_user"), it.optString("to_user")) }.filter { it.isNotBlank() && it != uid }.distinct()
    val names = mutableMapOf<String, String>()
    if (others.isNotEmpty()) {
        try {
            val p = JSONArray(QinblyApi.rest(token, "GET", "profiles?id=in.(${others.joinToString(",")})&select=id,username"))
            for (i in 0 until p.length()) names[p.getJSONObject(i).getString("id")] = p.getJSONObject(i).optString("username")
        } catch (_: Exception) { }
    }
    return raw.map { o ->
        val f = o.optString("from_user"); val t = o.optString("to_user")
        QinblyCapsule(
            o.getString("id"), f, t, if (f == uid) "você" else names[f].orEmpty(), if (t == uid) "você" else names[t].orEmpty(),
            o.optString("body"), o.optString("message"), o.optString("media_path").takeIf { it != "null" }.orEmpty(), o.optString("media_type", "text"),
            try { OffsetDateTime.parse(o.optString("unlock_at")).toInstant() } catch (_: Exception) { Instant.now() },
            try { OffsetDateTime.parse(o.optString("created_at")).toInstant() } catch (_: Exception) { Instant.now() },
            o.optBoolean("opened")
        )
    }
}

// ───────── Cartão no topo dos stories: "Sua cápsula abriu" ─────────

/** Cartão que aparece no topo dos stories quando uma cápsula chega na data. */
@Composable
internal fun QinblyCapsuleInbox() {
    val context = LocalContext.current
    val prefs = remember { capSess(context) }
    var caps by remember { mutableStateOf<List<QinblyCapsule>>(emptyList()) }
    var viewing by remember { mutableStateOf<QinblyCapsule?>(null) }
    LaunchedEffect(Unit) {
        val token = prefs.getString("token", null); val uid = QinblyApi.currentUserId(prefs)
        if (token.isNullOrBlank() || uid.isNullOrBlank()) return@LaunchedEffect
        caps = try {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) { fetchCapsules(token, uid).filter { it.toId == uid && !it.opened && !it.closed } }
        } catch (_: Exception) { emptyList() }
    }
    val c = caps.firstOrNull() ?: return
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 4.dp).clip(RoundedCornerShape(22.dp))
            .background(Brush.horizontalGradient(listOf(Color(0xFF0F4AA8), Color(0xFF1877F2)))).clickable { viewing = c }.padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        QinblyIconOf(QinblyIcon.Mail, Color.White, 28.dp)
        Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
            Text("Sua cápsula abriu", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text((if (c.fromId == c.toId) "Guardada por você" else "De @${c.fromName}") + " em ${capShort(c.createdAt)}", color = Color(0xCCFFFFFF), fontSize = 12.sp)
        }
        Text("Abrir", color = Color(0xFF26215C), fontWeight = FontWeight.SemiBold, fontSize = 13.sp, modifier = Modifier.clip(RoundedCornerShape(18.dp)).background(Color.White).padding(horizontal = 16.dp, vertical = 8.dp))
    }
    val v = viewing
    if (v != null) QinblyCapsuleViewer(v, onClose = { viewing = null; caps = caps.filter { it.id != v.id } })
}

// ───────── Visualizador ─────────

@Composable
internal fun QinblyCapsuleViewer(c: QinblyCapsule, onClose: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { capSess(context) }
    LaunchedEffect(c.id) {
        val token = prefs.getString("token", null); val uid = QinblyApi.currentUserId(prefs)
        if (!token.isNullOrBlank() && c.toId == uid && !c.opened) {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                try { QinblyApi.rest(token, "PATCH", "time_capsules?id=eq.${c.id}", JSONObject().put("opened", true).toString(), "return=minimal") } catch (_: Exception) { }
            }
        }
    }
    QinblyDialog(onDismissRequest = onClose, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false), lift = false) {
        Box(Modifier.fillMaxSize().background(Color.Black)) {
            when (c.mediaType) {
                "image" -> AsyncImage(model = QinblyApi.publicMediaUrl(c.mediaPath), contentDescription = null, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize())
                "video" -> StoryVideo(QinblyApi.publicMediaUrl(c.mediaPath)) { }
                else -> {
                    val s = qinblyParseTextStory(c.body)
                    if (s != null) QinblyTextStoryContent(s, Modifier.fillMaxSize())
                    else Box(Modifier.fillMaxSize().background(CapBg).padding(28.dp), contentAlignment = Alignment.Center) { Text(c.body, color = Color.White, fontSize = 22.sp, textAlign = TextAlign.Center) }
                }
            }
            Box(Modifier.statusBarsPadding().padding(12.dp).size(40.dp).clip(CircleShape).background(Color(0x66000000)).clickable { onClose() }, contentAlignment = Alignment.Center) { QinblyCloseIcon(Color.White, 22.dp) }
            val (viewMsg, viewMusic) = qinblySplitMusic(c.message)
            if (viewMusic != null) QinblyMusicPlayer(viewMusic.url, muted = false)
            Column(
                Modifier.align(Alignment.BottomCenter).fillMaxWidth().background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xCC000000))))
                    .padding(start = 20.dp, end = 20.dp, top = 40.dp, bottom = qinblyNavBarPadding() + 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (viewMusic != null) QinblyMusicLine(viewMusic)
                if (viewMsg.isNotBlank()) Text(viewMsg, color = Color.White, fontSize = 17.sp, lineHeight = 23.sp)
                Text((if (c.fromId == c.toId) "Guardada por você" else "De @${c.fromName}") + " em ${capShort(c.createdAt)}", color = Color(0xCCFFFFFF), fontSize = 13.sp)
            }
        }
    }
}

// ───────── Lista: Minhas cápsulas ─────────

@Composable
internal fun QinblyCapsuleHub(accent: Color, onDismiss: () -> Unit, onNotice: (String) -> Unit) {
    val onNotice: (String) -> Unit = qinblyToast(onNotice)
    val context = LocalContext.current
    val prefs = remember { capSess(context) }
    val uid = remember { QinblyApi.currentUserId(prefs) }
    var caps by remember { mutableStateOf<List<QinblyCapsule>?>(null) }
    var refresh by remember { mutableIntStateOf(0) }
    var tab by remember { mutableIntStateOf(0) }
    var creating by remember { mutableStateOf(false) }
    var viewing by remember { mutableStateOf<QinblyCapsule?>(null) }
    LaunchedEffect(refresh) {
        val token = prefs.getString("token", null)
        if (token.isNullOrBlank() || uid.isNullOrBlank()) { caps = emptyList(); return@LaunchedEffect }
        caps = try { kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) { fetchCapsules(token, uid) } } catch (_: Exception) { onNotice("Não foi possível carregar suas cápsulas."); emptyList() }
    }
    val all = caps.orEmpty()
    val closed = all.filter { it.closed && it.fromId == uid }
    val open = all.filter { !it.closed }.sortedByDescending { it.unlockAt }
    QinblyDialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false), lift = false) {
        Column(Modifier.fillMaxSize().background(CapBg).statusBarsPadding()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(40.dp).clip(CircleShape).background(Color(0x22FFFFFF)).clickable { onDismiss() }, contentAlignment = Alignment.Center) { QinblyCloseIcon(Color.White, 22.dp) }
                Text("Cápsula do tempo", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.padding(start = 14.dp))
            }
            Text("Guarde uma lembrança agora. Ela só abre na data que você escolher.", color = CapMuted, fontSize = 14.sp, modifier = Modifier.padding(horizontal = 18.dp))
            Box(
                Modifier.fillMaxWidth().padding(16.dp).clip(RoundedCornerShape(28.dp)).background(CapAccent).clickable { creating = true }.padding(vertical = 15.dp),
                contentAlignment = Alignment.Center
            ) { Text("＋  Nova cápsula", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp) }
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Fechadas (${closed.size})", "Abertas (${open.size})").forEachIndexed { i, label ->
                    Box(Modifier.weight(1f).clip(RoundedCornerShape(20.dp)).background(if (tab == i) Color.White else CapField).clickable { tab = i }.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                        Text(label, color = if (tab == i) Color(0xFF26215C) else Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    }
                }
            }
            val list = if (tab == 0) closed else open
            if (caps == null) Text("Carregando…", color = CapMuted, modifier = Modifier.padding(24.dp))
            else if (list.isEmpty()) Text(if (tab == 0) "Você não tem cápsulas fechadas." else "Nenhuma cápsula abriu ainda.", color = CapMuted, modifier = Modifier.padding(24.dp))
            else LazyColumn(Modifier.fillMaxSize(), contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = qinblyNavBarPadding() + 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(list, key = { it.id }) { c ->
                    val mine = c.fromId == uid
                    val title = if (c.closed) (if (c.toId == uid) "Para você" else "Para @${c.toName}") else if (mine && c.toId != uid) "Para @${c.toName}" else if (mine) "Guardada por você" else "De @${c.fromName}"
                    val days = ChronoUnit.DAYS.between(LocalDate.now(), c.unlockAt.atZone(ZoneId.systemDefault()).toLocalDate())
                    val sub = if (c.closed) "Abre em ${capShort(c.unlockAt)} · ${if (days <= 1L) "falta 1 dia" else "faltam $days dias"}" else "Abriu em ${capShort(c.unlockAt)}"
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(CapCard)
                            .clickable { if (c.closed) onNotice("Ainda fechada. Abre em ${capShort(c.unlockAt)}.") else viewing = c }.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(Modifier.size(44.dp).clip(CircleShape).background(CapField), contentAlignment = Alignment.Center) { QinblyIconOf(if (c.closed) QinblyIcon.Lock else QinblyIcon.Mail, Color.White, 22.dp) }
                        Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                            Text(title, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                            Text(sub, color = CapMuted, fontSize = 12.sp)
                        }
                        if (c.closed) QinblyCheckIcon(Color(0xFF63D68D), 21.dp)
                        else QinblyIconOf(QinblyIcon.KeyboardArrowRight, CapMuted, 26.dp)
                    }
                }
            }
        }
    }
    if (creating) QinblyCapsuleCreate(onNotice = onNotice, onFinished = { viewList -> creating = false; refresh++; if (!viewList) onDismiss() })
    val v = viewing
    if (v != null) QinblyCapsuleViewer(v, onClose = { viewing = null; refresh++ })
}

// ───────── Criar ─────────

private data class CapFriend(val id: String, val username: String, val name: String)

@Composable
internal fun QinblyCapsuleCreate(onNotice: (String) -> Unit, onFinished: (viewList: Boolean) -> Unit) {
    val onNotice: (String) -> Unit = qinblyToast(onNotice)
    val context = LocalContext.current
    val prefs = remember { capSess(context) }
    val today = remember { LocalDate.now() }
    val minDate = remember { today.plusDays(1) }

    var step by remember { mutableStateOf("pick") } // pick, text, edit, date, who, done
    var picked by remember { mutableStateOf<GalleryItem?>(null) }
    var textBody by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf("") }
    var showEmojis by remember { mutableStateOf(false) }
    var music by remember { mutableStateOf<QinblyMusic?>(null) }
    var showMusic by remember { mutableStateOf(false) }
    var date by remember { mutableStateOf<LocalDate?>(null) }
    var year by remember { mutableIntStateOf(minDate.year) }
    var month by remember { mutableIntStateOf(minDate.monthValue) }
    var target by remember { mutableStateOf<CapFriend?>(null) } // null = para mim
    var friends by remember { mutableStateOf<List<CapFriend>?>(null) }
    var search by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }
    var yearOpen by remember { mutableStateOf(false) }

    var granted by remember { mutableStateOf(hasGalleryPermission(context)) }
    var items by remember { mutableStateOf<List<GalleryItem>>(emptyList()) }
    var pendingPhoto by remember { mutableStateOf<File?>(null) }
    val galleryPerms = if (Build.VERSION.SDK_INT >= 33) arrayOf(android.Manifest.permission.READ_MEDIA_IMAGES, android.Manifest.permission.READ_MEDIA_VIDEO) else arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { granted = hasGalleryPermission(context) }
    val systemPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) { picked = GalleryItem(uri, (context.contentResolver.getType(uri) ?: "").startsWith("video/")); textBody = null; step = "edit" }
    }
    var showAppCam by remember { mutableStateOf(false) }
    if (showAppCam) QinblyCameraCapture(
        onResult = { u -> showAppCam = false; picked = GalleryItem(u, false); textBody = null; step = "edit" },
        onDismiss = { showAppCam = false }
    )
    fun launchCamera() { showAppCam = true }
    val cameraPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok -> if (ok) launchCamera() else onNotice("Permita a câmera para tirar a foto.") }
    LaunchedEffect(Unit) { if (!granted) permissionLauncher.launch(galleryPerms) }
    LaunchedEffect(granted) { if (granted) items = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) { loadRecentMedia(context) } }
    LaunchedEffect(step) {
        if (step == "who" && friends == null) {
            val token = prefs.getString("token", null); val uid = QinblyApi.currentUserId(prefs)
            friends = try {
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    if (token.isNullOrBlank() || uid.isNullOrBlank()) emptyList() else {
                        val f = JSONArray(QinblyApi.rest(token, "GET", "follows?follower_id=eq.$uid&select=following_id&limit=300"))
                        val ids = (0 until f.length()).map { f.getJSONObject(it).getString("following_id") }
                        if (ids.isEmpty()) emptyList() else {
                            val p = JSONArray(QinblyApi.rest(token, "GET", "profiles?id=in.(${ids.joinToString(",")})&select=id,username,display_name&order=username.asc"))
                            (0 until p.length()).map { val o = p.getJSONObject(it); CapFriend(o.getString("id"), o.optString("username"), o.optString("display_name").takeIf { n -> n != "null" }.orEmpty()) }
                        }
                    }
                }
            } catch (_: Exception) { emptyList() }
        }
    }

    fun save() {
        val token = prefs.getString("token", null); val uid = QinblyApi.currentUserId(prefs); val d = date
        if (token.isNullOrBlank() || uid.isNullOrBlank()) { onNotice("Entre na sua conta."); return }
        if (d == null) { onNotice("Escolha quando ela abre."); return }
        saving = true
        Thread {
            try {
                val o = JSONObject().put("to_user", target?.id ?: uid).put("unlock_at", d.atStartOfDay(ZoneId.systemDefault()).toInstant().toString())
                val item = picked
                if (item != null) {
                    val (fileId, isVideo) = QinblyApi.uploadStoryMedia(context, token, item.uri)
                    o.put("body", "").put("message", qinblyAttachMusic(message.trim(), music)).put("media_path", fileId).put("media_type", if (isVideo) "video" else "image")
                } else {
                    o.put("body", textBody.orEmpty()).put("message", "").put("media_type", "text")
                }
                QinblyApi.rest(token, "POST", "time_capsules", o.toString(), "return=minimal")
                pendingPhoto?.delete()
                capMain { saving = false; step = "done" }
            } catch (e: Exception) { capMain { saving = false; onNotice(e.message ?: "Não foi possível fechar a cápsula.") } }
        }.start()
    }

    fun back() {
        when (step) {
            "edit" -> { picked = null; step = "pick" }
            "date" -> step = if (textBody != null) "pick" else "edit"
            "who" -> step = "date"
            "pick" -> onFinished(true)
            else -> { }
        }
    }

    if (step == "text") {
        QinblyTextStoryComposer(onDismiss = { step = "pick" }, onPublished = { }, onNotice = onNotice, onCompose = { enc -> textBody = enc; picked = null; step = "date" })
        return
    }

    QinblyDialog(onDismissRequest = { if (!saving && step != "done") back() }, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false), lift = false) {
        Box(Modifier.fillMaxSize().background(CapBg)) {
            @Composable fun TopBar(title: String, closeIcon: String = "←") {
                Row(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(40.dp).clip(CircleShape).background(Color(0x22FFFFFF)).clickable(enabled = !saving) { back() }, contentAlignment = Alignment.Center) { if (closeIcon == "✕") QinblyCloseIcon(Color.White, 22.dp) else QinblyBackIcon(Color.White, 22.dp) }
                    Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(start = 14.dp))
                }
            }
            @Composable fun Primary(label: String, enabled: Boolean = true, modifier: Modifier = Modifier, onClick: () -> Unit) {
                Box(modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)).background(if (enabled) CapAccent else Color(0xFF2E2E30)).clickable(enabled = enabled) { onClick() }.padding(vertical = 15.dp), contentAlignment = Alignment.Center) {
                    Text(label, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
            when (step) {
                "pick" -> Column(Modifier.fillMaxSize()) {
                    TopBar("Nova cápsula", "✕")
                    Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        @Composable fun Chip(label: String, onClick: () -> Unit, mod: Modifier) {
                            Box(mod.clip(RoundedCornerShape(22.dp)).background(Color(0xFF3A2D6B)).clickable { onClick() }.padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val ic = when { label.startsWith("📷") -> QinblyIcon.PhotoCamera; label.startsWith("Aa") -> QinblyIcon.TextFields; else -> null }
                                    if (ic != null) { QinblyIconOf(ic, Color.White, 20.dp); Spacer(Modifier.width(8.dp)) }
                                    Text(label.substringAfter("  "), color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                                }
                            }
                        }
                        Chip("📷  Câmera", {
                            if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED) launchCamera()
                            else cameraPermission.launch(android.Manifest.permission.CAMERA)
                        }, Modifier.weight(1f))
                        Chip("Aa  Texto", { step = "text" }, Modifier.weight(1f))
                    }
                    Text("Recentes", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, modifier = Modifier.padding(start = 16.dp, top = 14.dp, bottom = 8.dp))
                    if (!granted) {
                        Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Permita o acesso às suas fotos para escolher daqui.", color = CapMuted, fontSize = 15.sp)
                            Text("Permitir acesso", color = Color.White, fontWeight = FontWeight.SemiBold, modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(CapAccent).clickable { permissionLauncher.launch(galleryPerms) }.padding(horizontal = 22.dp, vertical = 11.dp))
                            Text("Escolher pela galeria do celular", color = Color(0xFF6AA6F8), modifier = Modifier.clickable { systemPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)) }.padding(8.dp))
                        }
                    } else if (items.isEmpty()) {
                        Text("Nenhuma foto ou vídeo encontrado.", color = CapMuted, modifier = Modifier.padding(24.dp))
                    } else {
                        LazyVerticalGrid(GridCells.Fixed(3), modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(2.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            gridItems(items, key = { it.uri.toString() }) { g ->
                                Box(Modifier.aspectRatio(1f).clickable { picked = g; textBody = null; step = "edit" }) {
                                    MediaThumb(g.uri, g.video, Modifier.fillMaxSize())
                                    if (g.video) QinblyIconOf(QinblyIcon.PlayArrow, Color.White, 20.dp, Modifier.align(Alignment.BottomStart).padding(6.dp))
                                }
                            }
                        }
                    }
                }
                "edit" -> Column(Modifier.fillMaxSize().imePadding().padding(bottom = if (showEmojis) 0.dp else qinblyNavBarPadding())) {
                    TopBar("Sua lembrança")
                    val item = picked
                    Box(Modifier.weight(1f).fillMaxWidth().padding(horizontal = 14.dp), contentAlignment = Alignment.Center) {
                        if (item != null) MediaThumb(item.uri, item.video, Modifier.fillMaxSize().clip(RoundedCornerShape(18.dp)), size = 900)
                        val m = music
                        if (m != null) Row(Modifier.align(Alignment.TopStart).padding(10.dp).clip(RoundedCornerShape(16.dp)).background(Color(0x99000000)).padding(start = 12.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            QinblyMusicLine(m, Modifier.widthIn(max = 220.dp))
                            QinblyCloseIcon(Color.White, 19.dp, modifier = Modifier.clickable { music = null }.padding(10.dp))
                        }
                    }
                    Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(Modifier.weight(1f).clip(RoundedCornerShape(26.dp)).background(CapField).padding(start = 18.dp, end = 8.dp, top = 6.dp, bottom = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.weight(1f).padding(vertical = 8.dp)) {
                                if (message.isEmpty()) Text("Escreva uma mensagem para o futuro…", color = Color(0xFF8E87A8), fontSize = 15.sp)
                                BasicTextField(value = message, onValueChange = { message = it.take(500) }, textStyle = TextStyle(color = Color.White, fontSize = 15.sp), cursorBrush = SolidColor(Color.White), maxLines = 4, modifier = Modifier.fillMaxWidth())
                            }
                            Box(Modifier.size(36.dp).clip(CircleShape).background(if (music != null) Color(0x44FFFFFF) else Color.Transparent).clickable { showEmojis = false; showMusic = true }, contentAlignment = Alignment.Center) { QinblyIconOf(QinblyIcon.MusicNote, Color.White, 22.dp) }
                            Box(Modifier.size(36.dp).clip(CircleShape).background(if (showEmojis) Color(0x44FFFFFF) else Color.Transparent).clickable { showEmojis = !showEmojis }, contentAlignment = Alignment.Center) { QinblyIconOf(QinblyIcon.EmojiEmotions, Color.White, 22.dp) }
                        }
                        Text("Continuar", color = Color(0xFF26215C), fontWeight = FontWeight.SemiBold, fontSize = 15.sp,
                            modifier = Modifier.clip(RoundedCornerShape(24.dp)).background(Color.White).clickable { showEmojis = false; step = "date" }.padding(horizontal = 20.dp, vertical = 13.dp))
                    }
                    if (showEmojis) QinblyEmojiPanel(Modifier.padding(bottom = qinblyNavBarPadding()), Color(0xFF1F1F21)) { e -> message = (message + e).take(500) }
                }
                "date" -> Column(Modifier.fillMaxSize()) {
                    TopBar("Escolha a data")
                    val ym = YearMonth.of(year, month)
                    val minYm = YearMonth.from(minDate)
                    val maxYm = YearMonth.of(minDate.year + 60, 12)
                    fun goTo(t: YearMonth) {
                        val c = if (t.isBefore(minYm)) minYm else if (t.isAfter(maxYm)) maxYm else t
                        year = c.year; month = c.monthValue
                    }
                    Column(
                        Modifier.weight(1f).fillMaxWidth().padding(horizontal = 16.dp)
                            .pointerInput(year, month) {
                                var dx = 0f
                                detectHorizontalDragGestures(onDragStart = { dx = 0f }, onDragEnd = { if (dx > 80f) goTo(ym.minusMonths(1)) else if (dx < -80f) goTo(ym.plusMonths(1)) }) { _, amount -> dx += amount }
                            },
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text("Dia em que a cápsula abre", color = CapMuted, fontSize = 14.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("1 mês" to today.plusMonths(1), "6 meses" to today.plusMonths(6), "1 ano" to today.plusYears(1)).forEach { (label, d) ->
                                Text(label, color = if (date == d) Color(0xFF26215C) else Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp,
                                    modifier = Modifier.clip(RoundedCornerShape(18.dp)).background(if (date == d) Color.White else CapField).clickable { date = d; year = d.year; month = d.monthValue; yearOpen = false }.padding(horizontal = 16.dp, vertical = 9.dp))
                            }
                        }
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            val canBack = ym.isAfter(minYm); val canNext = ym.isBefore(maxYm)
                            Box(Modifier.size(42.dp).clip(CircleShape).background(CapField).clickable(enabled = canBack) { goTo(ym.minusMonths(1)) }, contentAlignment = Alignment.Center) { QinblyIconOf(QinblyIcon.KeyboardArrowLeft, if (canBack) Color.White else Color(0xFF5C5575), 28.dp) }
                            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(CapMonths[month - 1].replaceFirstChar { it.uppercase() }, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                                Row(Modifier.clip(RoundedCornerShape(14.dp)).clickable { yearOpen = !yearOpen }.padding(horizontal = 12.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text("$year", color = Color(0xFF6AA6F8), fontWeight = FontWeight.SemiBold, fontSize = 17.sp)
                                    Text(if (yearOpen) "  ▴" else "  ▾", color = Color(0xFF6AA6F8), fontSize = 15.sp)
                                }
                            }
                            Box(Modifier.size(42.dp).clip(CircleShape).background(CapField).clickable(enabled = canNext) { goTo(ym.plusMonths(1)) }, contentAlignment = Alignment.Center) { QinblyIconOf(QinblyIcon.KeyboardArrowRight, if (canNext) Color.White else Color(0xFF5C5575), 28.dp) }
                        }
                        Box(Modifier.fillMaxWidth()) {
                            Column {
                                Row(Modifier.fillMaxWidth()) {
                                    listOf("D", "S", "T", "Q", "Q", "S", "S").forEach { Text(it, color = CapMuted, fontSize = 13.sp, textAlign = TextAlign.Center, modifier = Modifier.weight(1f)) }
                                }
                                val offset = ym.atDay(1).dayOfWeek.value % 7
                                val total = offset + ym.lengthOfMonth()
                                for (r in 0 until (total + 6) / 7) Row(Modifier.fillMaxWidth()) {
                                    for (k in 0 until 7) {
                                        val dayNum = r * 7 + k - offset + 1
                                        if (dayNum < 1 || dayNum > ym.lengthOfMonth()) { Spacer(Modifier.weight(1f).aspectRatio(1f)); continue }
                                        val d = ym.atDay(dayNum)
                                        val ok = !d.isBefore(minDate)
                                        val sel = date == d
                                        Box(Modifier.weight(1f).aspectRatio(1f).padding(3.dp).clip(CircleShape).background(if (sel) CapAccent else Color.Transparent).clickable(enabled = ok) { date = d; yearOpen = false }, contentAlignment = Alignment.Center) {
                                            Text("$dayNum", color = if (ok) Color.White else Color(0xFF5C5575), fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal, fontSize = 16.sp)
                                        }
                                    }
                                }
                                Text("Arraste pro lado pra mudar o mês", color = Color(0xFF8E87A8), fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 14.dp))
                            }
                            if (yearOpen) {
                                Column(Modifier.align(Alignment.TopCenter).width(130.dp).heightIn(max = 260.dp).clip(RoundedCornerShape(16.dp)).background(Color(0xFF262628)).verticalScroll(rememberScrollState()).padding(vertical = 6.dp)) {
                                    for (y in minDate.year..minDate.year + 60) {
                                        Text("$y", color = if (year == y) Color(0xFF26215C) else Color.White, fontWeight = if (year == y) FontWeight.Bold else FontWeight.Normal, fontSize = 17.sp, textAlign = TextAlign.Center,
                                            modifier = Modifier.fillMaxWidth().background(if (year == y) Color.White else Color.Transparent)
                                                .clickable { goTo(YearMonth.of(y, month)); yearOpen = false }.padding(vertical = 10.dp))
                                    }
                                }
                            }
                        }
                    }
                    Primary("Continuar", enabled = date != null, modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = qinblyNavBarPadding() + 8.dp)) { yearOpen = false; step = "who" }
                }
                "who" -> Column(Modifier.fillMaxSize()) {
                    TopBar("Para quem?")
                    val fl = friends
                    val shown = fl.orEmpty().filter { search.isBlank() || it.username.contains(search.trim().removePrefix("@"), true) || it.name.contains(search.trim(), true) }
                    Column(Modifier.weight(1f).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        @Composable fun Person(label: String, sub: String, selected: Boolean, initial: String, onClick: () -> Unit) {
                            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(if (selected) Color(0xFF3A2D6B) else CapCard).clickable { onClick() }.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(42.dp).clip(CircleShape).background(CapField), contentAlignment = Alignment.Center) { if (initial == "🙂") QinblyIconOf(QinblyIcon.Person, Color.White, 22.dp) else Text(initial, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp) }
                                Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                                    Text(label, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                                    if (sub.isNotBlank()) Text(sub, color = CapMuted, fontSize = 12.sp)
                                }
                                if (selected) QinblyCheckIcon(Color(0xFF63D68D), 21.dp)
                            }
                        }
                        Person("Para mim", "Só você vê quando abrir", target == null, "🙂") { target = null }
                        QinblyPillField(search, { search = it.take(30) }, "Buscar amigo", Modifier.fillMaxWidth())
                        if (fl == null) Text("Carregando amigos…", color = CapMuted, modifier = Modifier.padding(8.dp))
                        else if (fl.isEmpty()) Text("Você ainda não segue ninguém.", color = CapMuted, modifier = Modifier.padding(8.dp))
                        else LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(shown, key = { it.id }) { f ->
                                Person(f.name.ifBlank { "@${f.username}" }, if (f.name.isBlank()) "" else "@${f.username}", target?.id == f.id, f.username.take(1).uppercase()) { target = f }
                            }
                        }
                    }
                    Primary(if (saving) "Fechando…" else "Fechar cápsula", enabled = !saving, modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = qinblyNavBarPadding() + 8.dp)) { confirm = true }
                }
                else -> Column(Modifier.fillMaxSize().statusBarsPadding(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Box(Modifier.size(96.dp).clip(CircleShape).background(Color(0xFF1D9E75)), contentAlignment = Alignment.Center) { QinblyCheckIcon(Color.White, 51.dp) }
                    Text("Cápsula fechada", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 26.sp, modifier = Modifier.padding(top = 22.dp))
                    Text("Ela só abre em ${date?.let { capLong(it) } ?: ""}.", color = CapMuted, fontSize = 16.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 8.dp, start = 32.dp, end = 32.dp))
                    Text(if (target == null) "Para você" else "Para @${target?.username}", color = CapMuted, fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp))
                    Spacer(Modifier.height(32.dp))
                    Primary("Concluir", modifier = Modifier.padding(horizontal = 24.dp)) { onFinished(false) }
                    Text("Ver minhas cápsulas", color = Color(0xFF6AA6F8), fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable { onFinished(true) }.padding(16.dp))
                    Spacer(Modifier.height(qinblyNavBarPadding()))
                }
            }
        }
    }
    if (showMusic) QinblyMusicPicker(onPick = { music = it; showMusic = false }, onDismiss = { showMusic = false })
    if (confirm) {
        AlertDialog(
            onDismissRequest = { confirm = false },
            title = { Text("Fechar a cápsula?") },
            text = { Text("Depois de fechada, ela não pode ser editada nem apagada. Só abre em ${date?.let { capLong(it) } ?: ""}.") },
            confirmButton = { TextButton(onClick = { confirm = false; save() }) { Text("Fechar agora") } },
            dismissButton = { TextButton(onClick = { confirm = false }) { Text("Voltar") } }
        )
    }
}
