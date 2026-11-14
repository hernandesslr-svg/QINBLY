package app.qinbly.android

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import org.json.JSONArray
import org.json.JSONObject

private val Panel = Color(0xFF1F1F21)
private val FieldBg = Color(0xFF262628)
private const val STICKER_BASE = "https://jqomgrdlocdqaarfqkay.supabase.co/storage/v1/object/public/post-media/"

private fun sess(c: Context) = c.getSharedPreferences("qinbly_session", Context.MODE_PRIVATE)
private fun main(block: () -> Unit) { Handler(Looper.getMainLooper()).post(block) }


/** Dentro de QinblyDialog o recuo dos botões do celular já foi aplicado na janela inteira. */
internal val LocalQinblyNavHandled = androidx.compose.runtime.compositionLocalOf { false }

/** Altura da barra de botões do celular, medida de três formas (insets do Compose, métricas da janela do app e medida do sistema). */
@Composable
internal fun qinblyNavBarPadding(): androidx.compose.ui.unit.Dp {
    if (LocalQinblyNavHandled.current) return 0.dp
    val density = androidx.compose.ui.platform.LocalDensity.current
    val context = LocalContext.current
    val fromCompose = with(density) { WindowInsets.navigationBars.getBottom(this).toDp() }
    val measured = remember {
        val res = context.resources
        val d = res.displayMetrics.density
        var activity: Context? = context
        while (activity is android.content.ContextWrapper && activity !is android.app.Activity) activity = activity.baseContext
        var fromWindow = 0f
        if (android.os.Build.VERSION.SDK_INT >= 30 && activity is android.app.Activity) {
            try { fromWindow = activity.windowManager.currentWindowMetrics.windowInsets.getInsetsIgnoringVisibility(android.view.WindowInsets.Type.navigationBars()).bottom / d } catch (_: Exception) { }
        }
        val gesture = try { android.provider.Settings.Secure.getInt(context.contentResolver, "navigation_mode", 0) == 2 } catch (_: Exception) { false }
        val id = res.getIdentifier("navigation_bar_height", "dimen", "android")
        val sys = if (id > 0) res.getDimensionPixelSize(id) / d else 0f
        maxOf(fromWindow, if (gesture) minOf(sys, 24f) else sys)
    }
    // Mínimo fixo: em alguns aparelhos todas as medições chegam zeradas dentro de janelas flutuantes.
    return maxOf(fromCompose, measured.dp, 98.dp)
}

/** Letras menores só dentro do conteúdo informado (usado nas telas de story). */
@Composable
internal fun QinblySmallText(content: @Composable () -> Unit) {
    val base = androidx.compose.ui.platform.LocalDensity.current
    val smaller = androidx.compose.ui.unit.Density(base.density, fontScale = minOf(base.fontScale, 1f) * 0.86f)
    androidx.compose.runtime.CompositionLocalProvider(androidx.compose.ui.platform.LocalDensity provides smaller) { content() }
}

/** Igual ao Dialog do Compose, mas as telas que ocupam a janela inteira terminam sempre acima dos botões do celular. */
@Composable
internal fun QinblyDialog(
    onDismissRequest: () -> Unit,
    properties: androidx.compose.ui.window.DialogProperties = androidx.compose.ui.window.DialogProperties(),
    lift: Boolean = false,
    smallText: Boolean = false,
    content: @Composable () -> Unit
) {
    val pad = if (lift && !properties.decorFitsSystemWindows) qinblyNavBarPadding() else 0.dp
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismissRequest, properties = properties) {
        if (lift) androidx.compose.runtime.CompositionLocalProvider(LocalQinblyNavHandled provides true) {
            Box(Modifier.padding(bottom = pad)) { content() }
        } else if (smallText) QinblySmallText { content() } else content()
    }
}

/** Aviso que aparece por cima de qualquer janela (o aviso normal fica escondido atrás das folhas). */
@Composable
internal fun qinblyToast(@Suppress("UNUSED_PARAMETER") original: (String) -> Unit): (String) -> Unit {
    val ctx = androidx.compose.ui.platform.LocalContext.current
    return { m -> android.widget.Toast.makeText(ctx, m, android.widget.Toast.LENGTH_LONG).show() }
}

/** Painel grande que sobe de baixo, sempre acima dos botões do celular. */
@Composable
internal fun QinblyBottomPanel(title: String, onDismiss: () -> Unit, content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    val nav = qinblyNavBarPadding()
    QinblyDialog(onDismissRequest = onDismiss, properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false), lift = false) {
        Box(Modifier.fillMaxSize().background(Color(0x99000000)).clickable(indication = null, interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }) { onDismiss() }) {
            Column(
                Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(top = 48.dp, bottom = nav + 8.dp).clip(RoundedCornerShape(32.dp)).background(Panel)
                    .clickable(indication = null, interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }) { }
                    .imePadding().verticalScroll(androidx.compose.foundation.rememberScrollState()).padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(Modifier.align(Alignment.CenterHorizontally).size(width = 44.dp, height = 5.dp).clip(CircleShape).background(Color(0x55FFFFFF)))
                Text(title, color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                content()
            }
        }
    }
}

@Composable
private fun BigButton(text: String, color: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(modifier.clip(RoundedCornerShape(28.dp)).background(color).clickable { onClick() }.padding(vertical = 17.dp), contentAlignment = Alignment.Center) {
        Text(text, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
    }
}

/** Campo de texto arredondado (pílula) usado nas telas novas. */
@Composable
internal fun QinblyPillField(value: String, onChange: (String) -> Unit, hint: String, modifier: Modifier = Modifier, maxLines: Int = 1) {
    Box(modifier.clip(RoundedCornerShape(26.dp)).background(FieldBg).padding(horizontal = 20.dp, vertical = 16.dp)) {
        if (value.isEmpty()) Text(hint, color = Color(0xFF8E87A8), fontSize = 17.sp)
        BasicTextField(value, onChange, textStyle = TextStyle(color = Color.White, fontSize = 17.sp), cursorBrush = SolidColor(Color.White), maxLines = maxLines, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun Chip(text: String, selected: Boolean, accent: Color = Color(0xFF1877F2), onClick: () -> Unit) {
    Text(text, color = Color.White, fontSize = 16.sp, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        modifier = Modifier.clip(RoundedCornerShape(22.dp)).background(if (selected) accent else FieldBg).clickable { onClick() }.padding(horizontal = 18.dp, vertical = 11.dp))
}

internal fun qinblyAgo(iso: String): String = try {
    val m = java.time.Duration.between(java.time.OffsetDateTime.parse(iso).toInstant(), java.time.Instant.now()).toMinutes()
    when { m < 1 -> "agora"; m < 60 -> "há ${m} min"; m < 1440 -> "há ${m / 60} h"; else -> "há ${m / 1440} d" }
} catch (_: Exception) { "" }

// ───────── Clima do dia ─────────
private val moodEmojis = listOf("😊", "😄", "😎", "🥰", "😴", "🤒", "😢", "😡", "🤩", "🧘", "💪", "🎉")
private val moodColors = listOf("#7C4DFF", "#E0457B", "#1D9E75", "#EF9F27", "#378ADD", "#D85A30", "#639922", "#888780")

internal data class QinblyMood(val emoji: String, val color: Color)

/** Climas ativos (últimas 24 h) das pessoas informadas, por @. */
internal fun qinblyFetchMoods(token: String, usernames: List<String>): Map<String, QinblyMood> {
    val names = usernames.filter { it.isNotBlank() && it.all { ch -> ch.isLetterOrDigit() || ch == '_' || ch == '.' } }.distinct()
    if (names.isEmpty()) return emptyMap()
    val arr = JSONArray(QinblyApi.rest(token, "GET", "profiles?username=in.(${names.joinToString(",")})&mood_emoji=not.is.null&select=username,mood_emoji,mood_color,mood_at"))
    val out = HashMap<String, QinblyMood>()
    for (i in 0 until arr.length()) {
        val o = arr.getJSONObject(i)
        val fresh = try { java.time.Duration.between(java.time.OffsetDateTime.parse(o.optString("mood_at")).toInstant(), java.time.Instant.now()).toHours() < 24 } catch (_: Exception) { false }
        if (!fresh) continue
        val col = try { Color(android.graphics.Color.parseColor(o.optString("mood_color", "#7C4DFF"))) } catch (_: Exception) { Color(0xFF1877F2) }
        out[o.optString("username")] = QinblyMood(o.optString("mood_emoji"), col)
    }
    return out
}

@Composable
internal fun QinblyMoodDialog(onDismiss: () -> Unit, onNotice: (String) -> Unit, onChanged: () -> Unit) {
    val onNotice: (String) -> Unit = qinblyToast(onNotice)
    val context = LocalContext.current
    val prefs = remember { sess(context) }
    var emoji by remember { mutableStateOf(moodEmojis[0]) }
    var color by remember { mutableStateOf(moodColors[0]) }
    fun save(clear: Boolean) {
        val token = prefs.getString("token", null); val uid = QinblyApi.currentUserId(prefs)
        if (token.isNullOrBlank() || uid.isNullOrBlank()) { onNotice("Entre na sua conta."); return }
        Thread {
            try {
                val b = JSONObject()
                if (clear) b.put("mood_emoji", JSONObject.NULL).put("mood_color", JSONObject.NULL).put("mood_at", JSONObject.NULL)
                else b.put("mood_emoji", emoji).put("mood_color", color).put("mood_at", java.time.Instant.now().toString())
                QinblyApi.rest(token, "PATCH", "profiles?id=eq.$uid", b.toString(), "return=minimal")
                main { onNotice(if (clear) "Clima removido." else "Clima do dia definido. Dura 24 horas."); onChanged(); onDismiss() }
            } catch (e: Exception) { main { onNotice(e.message ?: "Não foi possível salvar.") } }
        }.start()
    }
    val chosen = Color(android.graphics.Color.parseColor(color))
    var pickingEmoji by remember { mutableStateOf(false) }
    QinblyDialog(onDismissRequest = { if (pickingEmoji) pickingEmoji = false else onDismiss() }, properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false), lift = false) {
        Box(Modifier.fillMaxSize().background(Color(0xFF171719))) {
            Column(Modifier.fillMaxSize().statusBarsPadding()) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(40.dp).clip(CircleShape).background(Color(0x22FFFFFF)).clickable { if (pickingEmoji) pickingEmoji = false else onDismiss() }, contentAlignment = Alignment.Center) {
                        Text(if (pickingEmoji) "←" else "✕", color = Color.White, fontSize = 18.sp)
                    }
                    Text(if (pickingEmoji) "Escolher emoji" else "Clima do dia", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 17.sp, modifier = Modifier.padding(start = 14.dp))
                }
                if (pickingEmoji) {
                    Box(Modifier.size(84.dp).align(Alignment.CenterHorizontally).clip(CircleShape).background(chosen), contentAlignment = Alignment.Center) { Text(emoji, fontSize = 44.sp) }
                    Spacer(Modifier.size(12.dp))
                    QinblyEmojiPanel(Modifier.padding(bottom = qinblyNavBarPadding()), Color(0xFF171719), gridHeight = 380.dp) { e -> emoji = e; pickingEmoji = false }
                } else {
                    Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(androidx.compose.foundation.rememberScrollState()).padding(horizontal = 22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Spacer(Modifier.size(18.dp))
                        Box(Modifier.size(120.dp).clip(CircleShape).background(chosen).border(4.dp, Color.White, CircleShape), contentAlignment = Alignment.Center) { Text(emoji, fontSize = 62.sp) }
                        Spacer(Modifier.size(16.dp))
                        Text("Como você está hoje?", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Text("Aparece no seu cartão de story por 24 horas.", color = Color(0xFFA9B5C8), fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp))
                        Spacer(Modifier.size(24.dp))
                        (moodEmojis + "＋").chunked(5).forEach { row ->
                            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                                row.forEach { e ->
                                    val isMore = e == "＋"
                                    val on = if (isMore) emoji !in moodEmojis else emoji == e
                                    Box(Modifier.size(56.dp).clip(RoundedCornerShape(18.dp)).background(if (on) chosen.copy(alpha = 0.4f) else FieldBg).border(if (on) 2.dp else 0.dp, chosen, RoundedCornerShape(18.dp)).clickable { if (isMore) pickingEmoji = true else emoji = e }, contentAlignment = Alignment.Center) {
                                        Text(if (isMore && emoji !in moodEmojis) emoji else e, fontSize = if (isMore && emoji in moodEmojis) 22.sp else 28.sp, color = Color.White)
                                    }
                                }
                                repeat(5 - row.size) { Spacer(Modifier.size(56.dp)) }
                            }
                        }
                        Spacer(Modifier.size(22.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            moodColors.forEach { c -> Box(Modifier.size(32.dp).clip(CircleShape).background(Color(android.graphics.Color.parseColor(c))).border(if (color == c) 3.dp else 1.dp, Color.White, CircleShape).clickable { color = c }) }
                        }
                        Spacer(Modifier.size(24.dp))
                    }
                    Column(Modifier.fillMaxWidth().padding(start = 22.dp, end = 22.dp, bottom = 14.dp + qinblyNavBarPadding()), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Publicar clima", color = Color(0xFF26215C), fontWeight = FontWeight.Bold, fontSize = 17.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)).background(Color.White).clickable { save(false) }.padding(vertical = 16.dp))
                        Text("Limpar meu clima", color = Color(0xFF6AA6F8), fontSize = 15.sp, modifier = Modifier.clip(RoundedCornerShape(16.dp)).clickable { save(true) }.padding(horizontal = 16.dp, vertical = 8.dp))
                    }
                }
            }
        }
    }
}

// ───────── Pacotes de figurinhas da comunidade ─────────
private data class Pack(val id: String, val name: String, val owner: String, val paths: List<String>)

@Composable
internal fun QinblyStickerPacksDialog(accent: Color, onDismiss: () -> Unit, onNotice: (String) -> Unit) {
    val onNotice: (String) -> Unit = qinblyToast(onNotice)
    val context = LocalContext.current
    val prefs = remember { sess(context) }
    var packs by remember { mutableStateOf<List<Pack>?>(null) }
    var name by remember { mutableStateOf("") }
    var refresh by remember { mutableStateOf(0) }
    LaunchedEffect(refresh) {
        val token = prefs.getString("token", null)
        if (token.isNullOrBlank()) { packs = emptyList(); return@LaunchedEffect }
        packs = try {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                val a = JSONArray(QinblyApi.rest(token, "GET", "sticker_packs?select=id,name,profiles(username),sticker_pack_items(path)&order=created_at.desc&limit=30"))
                (0 until a.length()).map { i ->
                    val o = a.getJSONObject(i); val items = o.optJSONArray("sticker_pack_items") ?: JSONArray()
                    Pack(o.getString("id"), o.optString("name"), o.optJSONObject("profiles")?.optString("username").orEmpty(), (0 until items.length()).map { items.getJSONObject(it).optString("path") })
                }
            }
        } catch (_: Exception) { packs ?: emptyList() }
    }
    fun publish() {
        val token = prefs.getString("token", null); val uid = QinblyApi.currentUserId(prefs)
        if (token.isNullOrBlank() || uid.isNullOrBlank()) { onNotice("Entre na sua conta."); return }
        if (name.isBlank()) { onNotice("Dê um nome ao pacote."); return }
        val n = name.trim()
        Thread {
            try {
                val mine = QinblyApi.getMyStickers(token)
                if (mine.isEmpty()) throw IllegalStateException("Crie figurinhas primeiro.")
                val r = JSONArray(QinblyApi.rest(token, "POST", "sticker_packs", JSONObject().put("name", n).toString(), "return=representation"))
                val pid = r.getJSONObject(0).getString("id")
                val items = JSONArray(); mine.forEach { items.put(JSONObject().put("pack_id", pid).put("path", it.path)) }
                QinblyApi.rest(token, "POST", "sticker_pack_items", items.toString(), "return=minimal")
                main { name = ""; onNotice("Pacote publicado."); refresh++ }
            } catch (e: Exception) { main { onNotice(e.message ?: "Não foi possível publicar.") } }
        }.start()
    }
    fun add(p: Pack) {
        val token = prefs.getString("token", null); val uid = QinblyApi.currentUserId(prefs)
        if (token.isNullOrBlank() || uid.isNullOrBlank()) return
        Thread {
            try {
                val have = QinblyApi.getMyStickers(token).map { it.path }.toSet()
                val items = JSONArray(); p.paths.filter { it !in have }.forEach { items.put(JSONObject().put("owner_id", uid).put("path", it)) }
                if (items.length() > 0) QinblyApi.rest(token, "POST", "user_stickers", items.toString(), "return=minimal")
                main { onNotice(if (items.length() > 0) "${items.length()} figurinhas adicionadas." else "Você já tem todas.") }
            } catch (e: Exception) { main { onNotice(e.message ?: "Não foi possível adicionar.") } }
        }.start()
    }
    QinblyBottomPanel("Pacotes de figurinhas", onDismiss) {
        Text("Publique as suas figurinhas como um pacote e adicione as da comunidade.", color = Color(0xFFA9B5C8), fontSize = 16.sp)
        QinblyPillField(name, { name = it.take(40) }, "Nome do meu pacote", Modifier.fillMaxWidth())
        BigButton("Publicar meu pacote", accent, Modifier.fillMaxWidth()) { publish() }
        Text("Publicar usa todas as suas figurinhas atuais.", color = Color(0xFF8E87A8), fontSize = 13.sp)
        val list = packs
        if (list == null) Text("Carregando…", color = Color(0xFFA9B5C8), fontSize = 16.sp)
        else if (list.isEmpty()) Text("Nenhum pacote ainda. Seja o primeiro!", color = Color(0xFFA9B5C8), fontSize = 16.sp)
        else list.forEach { p ->
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(FieldBg).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(p.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("por @${p.owner} • ${p.paths.size} figurinhas", color = Color(0xFFA9B5C8), fontSize = 14.sp)
                    }
                    Text("Adicionar", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(accent).clickable { add(p) }.padding(horizontal = 16.dp, vertical = 10.dp))
                }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(p.paths.take(8)) { path -> AsyncImage(model = STICKER_BASE + path, contentDescription = null, modifier = Modifier.size(64.dp)) }
                }
            }
        }
    }
}

// ───────── Mural do bairro ─────────
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
internal fun QinblyNeighborhoodBoard(lat: Double, lng: Double, place: String, textColor: Color, secondary: Color, onRefreshLocation: () -> Unit, onNotice: (String) -> Unit, onOpenConversation: (String) -> Unit = {}) {
    val context = LocalContext.current
    val prefs = remember { sess(context) }
    val myId = remember { QinblyApi.currentUserId(prefs) }
    // Aproxima a posição (~1 km) antes de gravar, para não expor o ponto exato de quem avisa.
    val gLat = Math.round(lat * 100) / 100.0
    val gLng = Math.round(lng * 100) / 100.0
    var notices by remember { mutableStateOf<List<JSONObject>?>(null) }
    var draft by remember { mutableStateOf("") }
    var refresh by remember { mutableStateOf(0) }
    var tag by remember { mutableStateOf<String?>(null) }
    var photoUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var posting by remember { mutableStateOf(false) }
    var openNotice by remember { mutableStateOf<JSONObject?>(null) }
    var pendingCamera by remember { mutableStateOf<android.net.Uri?>(null) }
    val cameraLauncher = androidx.activity.compose.rememberLauncherForActivityResult(androidx.activity.result.contract.ActivityResultContracts.TakePicture()) { ok ->
        if (ok) photoUri = pendingCamera
        pendingCamera = null
    }
    fun launchCamera() {
        val f = java.io.File(context.cacheDir, "qinbly-mural-" + System.currentTimeMillis() + ".jpg")
        val u = androidx.core.content.FileProvider.getUriForFile(context, context.packageName + ".fileprovider", f)
        pendingCamera = u
        cameraLauncher.launch(u)
    }
    val cameraPermission = androidx.activity.compose.rememberLauncherForActivityResult(androidx.activity.result.contract.ActivityResultContracts.RequestPermission()) { ok -> if (ok) launchCamera() else onNotice("Permita a câmera para tirar a foto.") }
    LaunchedEffect(refresh, gLat, gLng) {
        val token = prefs.getString("token", null)
        if (token.isNullOrBlank()) { notices = emptyList(); return@LaunchedEffect }
        notices = try {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                val dLat = 0.05; val dLng = 0.05 / Math.cos(Math.toRadians(lat)).coerceAtLeast(0.2)
                val q = "neighborhood_notices?lat=gte.${gLat - dLat}&lat=lte.${gLat + dLat}&lng=gte.${gLng - dLng}&lng=lte.${gLng + dLng}&expires_at=gt.${java.time.Instant.now()}&select=id,body,kind,photo,created_at,user_id,profiles!neighborhood_notices_user_id_fkey(username,display_name),neighborhood_comments(count)&order=created_at.desc&limit=50"
                val a = JSONArray(QinblyApi.rest(token, "GET", q))
                (0 until a.length()).map { a.getJSONObject(it) }
            }
        } catch (_: Exception) { notices ?: emptyList() }
    }
    fun post() {
        val token = prefs.getString("token", null); val body = draft.trim(); val kind = tag
        if (token.isNullOrBlank() || body.isBlank() || kind == null || posting) return
        val uri = photoUri
        posting = true
        Thread {
            try {
                val photo = if (uri != null) uploadMarketPhoto(context, token, myId ?: throw IllegalStateException("Entre de novo na conta."), uri, 0) else null
                val j = JSONObject().put("body", body).put("lat", gLat).put("lng", gLng).put("kind", kind.lowercase())
                if (photo != null) j.put("photo", photo)
                QinblyApi.rest(token, "POST", "neighborhood_notices", j.toString(), "return=minimal")
                main { draft = ""; tag = null; photoUri = null; posting = false; onNotice("Aviso publicado. Some em 48 horas."); refresh++ }
            } catch (e: Exception) { main { posting = false; onNotice(e.message ?: "Não foi possível publicar.") } }
        }.start()
    }
    Column(Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Mural do bairro", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = textColor)
                Text(if (place.isNotBlank()) "Perto de $place • avisos somem em 48 h" else "Avisos de até ~5 km • somem em 48 h", color = secondary, fontSize = 12.sp)
            }
        }
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(FieldBg).padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                Box(Modifier.weight(1f).padding(start = 6.dp, top = 8.dp, end = 4.dp)) {
                    if (draft.isEmpty()) Text("Avise a vizinhança…", color = Color(0xFF8E87A8), fontSize = 16.sp)
                    androidx.compose.foundation.text.BasicTextField(draft, { draft = it.take(280) }, textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 16.sp), cursorBrush = androidx.compose.ui.graphics.SolidColor(Color.White), maxLines = 4, modifier = Modifier.fillMaxWidth())
                }
                Box(Modifier.size(38.dp).clip(CircleShape).background(Color(0xFF2E2E30)).clickable {
                    if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED) launchCamera()
                    else cameraPermission.launch(android.Manifest.permission.CAMERA)
                }, contentAlignment = Alignment.Center) { CameraGlyph(Color.White, Modifier.size(20.dp)) }
            }
            val pu = photoUri
            if (pu != null) Box(Modifier.padding(start = 6.dp)) {
                AsyncImage(model = pu, contentDescription = null, contentScale = androidx.compose.ui.layout.ContentScale.Crop, modifier = Modifier.size(56.dp).clip(RoundedCornerShape(10.dp)))
                Box(Modifier.align(Alignment.TopEnd).padding(2.dp).size(20.dp).clip(CircleShape).background(Color(0xCC000000)).clickable { photoUri = null }, contentAlignment = Alignment.Center) { Text("✕", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
            }
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(NOTICE_TAGS) { t ->
                    val sel = tag == t
                    Text(t, color = if (sel) Color.White else Color(0xFFB4B4BC), fontSize = 12.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.clip(RoundedCornerShape(16.dp)).background(if (sel) noticeTagColor(t) else Color(0xFF2E2E30)).clickable { tag = if (sel) null else t }.padding(horizontal = 12.dp, vertical = 6.dp))
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(Modifier.weight(1f))
                val ready = draft.isNotBlank() && tag != null && !posting
                Text(if (posting) "Enviando…" else "Publicar", color = if (ready) Color.White else Color(0xFF8E87A8), fontWeight = FontWeight.Bold, fontSize = 14.sp,
                    modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(if (ready) Color(0xFF1877F2) else Color(0xFF2E2E30)).clickable(enabled = ready) { post() }.padding(horizontal = 20.dp, vertical = 9.dp))
            }
            if (draft.isNotBlank() && tag == null) Text("Escolha o tipo do aviso para publicar.", color = secondary, fontSize = 11.sp, modifier = Modifier.padding(start = 6.dp))
        }
        val list = notices
        var pulling by remember { mutableStateOf(false) }
        LaunchedEffect(notices) { if (notices != null) pulling = false }
        LaunchedEffect(pulling) { if (pulling) { kotlinx.coroutines.delay(2500); pulling = false } }
        androidx.compose.material3.pulltorefresh.PullToRefreshBox(isRefreshing = pulling, onRefresh = { pulling = true; refresh++; onRefreshLocation() }, modifier = Modifier.weight(1f).fillMaxWidth()) {
        if (list == null) Column(Modifier.fillMaxSize().verticalScroll(androidx.compose.foundation.rememberScrollState())) { Text("Carregando…", color = secondary) }
        else if (list.isEmpty()) Column(Modifier.fillMaxSize().verticalScroll(androidx.compose.foundation.rememberScrollState())) { Text("Nenhum aviso por perto ainda. Escreva o primeiro: achados e perdidos, eventos, recomendações…", color = secondary, fontSize = 14.sp) }
        else LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(list, key = { it.optString("id") }) { n ->
                val p = n.optJSONObject("profiles")
                val kindName = noticeKindLabel(n.optString("kind"))
                val photo = n.optString("photo").takeIf { it.isNotBlank() && it != "null" }
                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(FieldBg).clickable { openNotice = n }.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(kindName, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(noticeTagColor(kindName)).padding(horizontal = 9.dp, vertical = 3.dp))
                        Text((p?.optString("display_name").orEmpty().ifBlank { p?.optString("username").orEmpty() }) + " • " + qinblyAgo(n.optString("created_at")), color = Color(0xFF6AA6F8), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis, modifier = Modifier.weight(1f).padding(start = 8.dp))
                        if (n.optString("user_id") == myId) Text("Apagar", color = Color(0xFFE0457B), fontSize = 12.sp, modifier = Modifier.clickable {
                            val id = n.optString("id"); val token = prefs.getString("token", null) ?: return@clickable
                            notices = notices?.filterNot { it.optString("id") == id }
                            Thread { try { QinblyApi.rest(token, "DELETE", "neighborhood_notices?id=eq.$id") } catch (_: Exception) { } }.start()
                        })
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(n.optString("body"), color = Color.White, fontSize = 15.sp, maxLines = 4, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                        if (photo != null) AsyncImage(model = photo, contentDescription = null, contentScale = androidx.compose.ui.layout.ContentScale.Crop, modifier = Modifier.size(56.dp).clip(RoundedCornerShape(10.dp)))
                    }
                    val cnt = n.optJSONArray("neighborhood_comments")?.optJSONObject(0)?.optInt("count") ?: 0
                    Text(if (cnt == 0) "💬  Comentar" else if (cnt == 1) "💬  1 comentário" else "💬  $cnt comentários", color = secondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        }
    }

    openNotice?.let { n ->
        NoticeDetail(n, myId, prefs, onDismiss = { openNotice = null }, onNotice = onNotice, onChanged = { refresh++ })
    }
}

@Composable
private fun CameraGlyph(color: Color, modifier: Modifier) {
    androidx.compose.foundation.Canvas(modifier) {
        val k = size.minDimension / 24f
        val body = androidx.compose.ui.graphics.vector.PathParser().parsePathString("M4 8.5A2.5 2.5 0 0 1 6.5 6h1.2c.5 0 1-.3 1.2-.7l.5-1c.2-.4.6-.7 1.1-.7h3c.5 0 .9.3 1.1.7l.5 1c.2.4.7.7 1.2.7h1.2A2.5 2.5 0 0 1 20 8.5v8A2.5 2.5 0 0 1 17.5 19h-11A2.5 2.5 0 0 1 4 16.5z").toPath()
        val stroke = androidx.compose.ui.graphics.drawscope.Stroke(1.6f, cap = androidx.compose.ui.graphics.StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round)
        withTransform({ scale(k, k, pivot = androidx.compose.ui.geometry.Offset.Zero) }) {
            drawPath(body, color, style = stroke)
            drawCircle(color, radius = 3.4f, center = androidx.compose.ui.geometry.Offset(12f, 12.5f), style = stroke)
        }
    }
}

private fun mentionText(body: String, color: Color): androidx.compose.ui.text.AnnotatedString {
    val end = if (body.startsWith("@")) body.indexOfFirst { it.isWhitespace() }.let { if (it < 0) body.length else it } else 0
    return androidx.compose.ui.text.buildAnnotatedString {
        append(body)
        if (end > 1) addStyle(androidx.compose.ui.text.SpanStyle(color = color, fontWeight = FontWeight.Bold), 0, end)
    }
}

private object MentionTransform : androidx.compose.ui.text.input.VisualTransformation {
    override fun filter(text: androidx.compose.ui.text.AnnotatedString) =
        androidx.compose.ui.text.input.TransformedText(mentionText(text.text, Color(0xFF6AA6F8)), androidx.compose.ui.text.input.OffsetMapping.Identity)
}

/** Aviso aberto: foto, texto e comentários. As respostas ficam escondidas até tocar em "Ver respostas". */
@Composable
private fun NoticeDetail(n: JSONObject, myId: String?, prefs: android.content.SharedPreferences, onDismiss: () -> Unit, onNotice: (String) -> Unit, onChanged: () -> Unit) {
    val noticeId = n.optString("id")
    val noticeOwner = n.optString("user_id")
    val p = n.optJSONObject("profiles")
    val kindName = noticeKindLabel(n.optString("kind"))
    val photo = n.optString("photo").takeIf { it.isNotBlank() && it != "null" }
    var comments by remember { mutableStateOf<List<JSONObject>?>(null) }
    var reload by remember { mutableStateOf(0) }
    var expanded by remember { mutableStateOf<Set<String>>(emptySet()) }
    var replyTo by remember { mutableStateOf<JSONObject?>(null) }
    var input by remember { mutableStateOf(androidx.compose.ui.text.input.TextFieldValue("")) }
    var sending by remember { mutableStateOf(false) }
    val focus = remember { androidx.compose.ui.focus.FocusRequester() }
    LaunchedEffect(reload) {
        val token = prefs.getString("token", null) ?: run { comments = emptyList(); return@LaunchedEffect }
        comments = try {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                val a = JSONArray(QinblyApi.rest(token, "GET", "neighborhood_comments?notice_id=eq.$noticeId&select=id,parent_id,user_id,body,created_at,profiles!neighborhood_comments_user_id_fkey(username,display_name)&order=created_at.asc&limit=300"))
                (0 until a.length()).map { a.getJSONObject(it) }
            }
        } catch (_: Exception) { comments ?: emptyList() }
    }
    fun nameOf(c: JSONObject): String { val pr = c.optJSONObject("profiles"); return pr?.optString("display_name").orEmpty().ifBlank { pr?.optString("username").orEmpty() }.ifBlank { "Alguém" } }
    fun handleOf(c: JSONObject): String = c.optJSONObject("profiles")?.optString("username").orEmpty().removePrefix("@").ifBlank { nameOf(c).replace(" ", "") }
    fun startReply(c: JSONObject) {
        replyTo = c
        val t = "@" + handleOf(c) + " "
        input = androidx.compose.ui.text.input.TextFieldValue(t, androidx.compose.ui.text.TextRange(t.length))
        val top = c.optString("parent_id").takeIf { it.isNotBlank() && it != "null" } ?: c.optString("id")
        expanded = expanded + top
        try { focus.requestFocus() } catch (_: Exception) { }
    }
    fun send() {
        val token = prefs.getString("token", null); val body = input.text.trim()
        if (token.isNullOrBlank() || body.isBlank() || sending) return
        val target = replyTo
        val parent = target?.let { it.optString("parent_id").takeIf { x -> x.isNotBlank() && x != "null" } ?: it.optString("id") }
        sending = true
        Thread {
            try {
                val j = JSONObject().put("notice_id", noticeId).put("body", body.take(280))
                if (parent != null) j.put("parent_id", parent)
                QinblyApi.rest(token, "POST", "neighborhood_comments", j.toString(), "return=minimal")
                main { input = androidx.compose.ui.text.input.TextFieldValue(""); replyTo = null; sending = false; if (parent != null) expanded = expanded + parent; reload++; onChanged() }
            } catch (e: Exception) { main { sending = false; onNotice(e.message ?: "Não foi possível comentar.") } }
        }.start()
    }
    fun delete(c: JSONObject) {
        val token = prefs.getString("token", null) ?: return
        val id = c.optString("id")
        comments = comments?.filterNot { it.optString("id") == id || it.optString("parent_id") == id }
        Thread { try { QinblyApi.rest(token, "DELETE", "neighborhood_comments?id=eq.$id"); main { onChanged() } } catch (_: Exception) { } }.start()
    }
    QinblyBottomPanel("Aviso", onDismiss) {
        Column(Modifier.heightIn(max = 560.dp)) {
            Column(Modifier.weight(1f, fill = false).verticalScroll(androidx.compose.foundation.rememberScrollState()).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (photo != null) AsyncImage(model = photo, contentDescription = null, contentScale = androidx.compose.ui.layout.ContentScale.Fit, modifier = Modifier.fillMaxWidth().heightIn(max = 260.dp).clip(RoundedCornerShape(16.dp)))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(kindName, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(noticeTagColor(kindName)).padding(horizontal = 9.dp, vertical = 3.dp))
                    Text((p?.optString("display_name").orEmpty().ifBlank { p?.optString("username").orEmpty() }) + " • " + qinblyAgo(n.optString("created_at")), color = Color(0xFF6AA6F8), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 8.dp))
                }
                Text(n.optString("body"), color = Color.White, fontSize = 16.sp)
                val all = comments
                val tops = all?.filter { it.optString("parent_id").let { x -> x.isBlank() || x == "null" } }.orEmpty()
                Text(if (all == null) "Carregando comentários…" else if (all.isEmpty()) "Nenhum comentário ainda. Seja o primeiro." else "${all.size} comentário" + (if (all.size > 1) "s" else ""), color = Color(0xFFB4B4BC), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                @Composable fun Bubble(c: JSONObject, small: Boolean) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(Modifier.size(if (small) 24.dp else 30.dp).clip(CircleShape).background(Color(0xFF3A3A3E)), contentAlignment = Alignment.Center) {
                            Text(nameOf(c).take(1).uppercase(), color = Color.White, fontSize = if (small) 11.sp else 13.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(Modifier.weight(1f)) {
                            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(FieldBg).padding(horizontal = 12.dp, vertical = 8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(nameOf(c), color = Color(0xFF6AA6F8), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    if (c.optString("user_id") == noticeOwner) Text("autor", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 6.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFF1877F2)).padding(horizontal = 6.dp, vertical = 1.dp))
                                    Text(" • " + qinblyAgo(c.optString("created_at")), color = Color(0xFF8E87A8), fontSize = 11.sp)
                                }
                                Text(mentionText(c.optString("body"), Color(0xFF6AA6F8)), color = Color.White, fontSize = 14.sp)
                            }
                            Row(Modifier.padding(start = 8.dp, top = 2.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                                Text("Responder", color = Color(0xFF8E87A8), fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { startReply(c) }.padding(vertical = 4.dp))
                                if (c.optString("user_id") == myId || noticeOwner == myId) Text("Apagar", color = Color(0xFFE0457B), fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { delete(c) }.padding(vertical = 4.dp))
                            }
                        }
                    }
                }
                for (c in tops) {
                    Bubble(c, false)
                    val replies = all.orEmpty().filter { it.optString("parent_id") == c.optString("id") }
                    if (replies.isNotEmpty()) {
                        val open = c.optString("id") in expanded
                        Text(if (open) "—  Ocultar respostas ▴" else "—  Ver ${replies.size} resposta" + (if (replies.size > 1) "s" else "") + " ▾", color = Color(0xFF8E87A8), fontSize = 12.sp, fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 38.dp).clickable { expanded = if (open) expanded - c.optString("id") else expanded + c.optString("id") }.padding(vertical = 4.dp))
                        if (open) Column(Modifier.padding(start = 38.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { for (r in replies) Bubble(r, true) }
                    }
                }
            }
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.weight(1f).clip(RoundedCornerShape(22.dp)).background(FieldBg).padding(horizontal = 16.dp, vertical = 11.dp)) {
                    if (input.text.isEmpty()) Text(if (replyTo != null) "Escreva uma resposta…" else "Escreva um comentário…", color = Color(0xFF8E87A8), fontSize = 14.sp)
                    androidx.compose.foundation.text.BasicTextField(
                        input, { v -> input = androidx.compose.ui.text.input.TextFieldValue(v.text.take(280), v.selection, v.composition); if (v.text.isEmpty()) replyTo = null },
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 14.sp), cursorBrush = androidx.compose.ui.graphics.SolidColor(Color.White),
                        visualTransformation = MentionTransform, maxLines = 3, modifier = Modifier.fillMaxWidth().focusRequester(focus)
                    )
                }
                val ready = input.text.isNotBlank() && !sending
                Box(Modifier.size(42.dp).clip(CircleShape).background(if (ready) Color(0xFF1877F2) else Color(0xFF2E2E30)).clickable(enabled = ready) { send() }, contentAlignment = Alignment.Center) { Text("➤", color = Color.White, fontSize = 16.sp) }
            }
        }
    }
}

private val NOTICE_TAGS = listOf("Achado", "Perdido", "Pergunta", "Evento")
private fun noticeKindLabel(kind: String): String = NOTICE_TAGS.firstOrNull { it.equals(kind, true) } ?: "Aviso"
private fun noticeTagColor(label: String): Color = when (label) {
    "Achado" -> Color(0xFFE0457B); "Perdido" -> Color(0xFFC2410C); "Pergunta" -> Color(0xFF3A5BA5); "Evento" -> Color(0xFFFF4F9A); else -> Color(0xFF1D9E75)
}
