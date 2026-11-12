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
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.AlertDialog
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

private val Panel = Color(0xFF1E1830)
private val FieldBg = Color(0xFF2B2342)
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
private fun Chip(text: String, selected: Boolean, accent: Color = Color(0xFF7C4DFF), onClick: () -> Unit) {
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
        val col = try { Color(android.graphics.Color.parseColor(o.optString("mood_color", "#7C4DFF"))) } catch (_: Exception) { Color(0xFF7C4DFF) }
        out[o.optString("username")] = QinblyMood(o.optString("mood_emoji"), col)
    }
    return out
}

@Composable
internal fun QinblyMoodDialog(onDismiss: () -> Unit, onNotice: (String) -> Unit, onChanged: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { sess(context) }
    var emoji by remember { mutableStateOf(moodEmojis[0]) }
    var color by remember { mutableStateOf(moodColors[0]) }
    fun save(clear: Boolean) {
        val token = prefs.getString("token", null); val uid = prefs.getString("user_id", null)
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
    QinblyBottomPanel("Clima do dia", onDismiss) {
        Text("Como você está hoje? Aparece no seu card por 24 horas.", color = Color(0xFFA9B5C8), fontSize = 16.sp)
        moodEmojis.chunked(4).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                row.forEach { e -> Box(Modifier.size(74.dp).clip(RoundedCornerShape(24.dp)).background(if (emoji == e) chosen.copy(alpha = 0.45f) else FieldBg).border(if (emoji == e) 2.dp else 0.dp, chosen, RoundedCornerShape(24.dp)).clickable { emoji = e }, contentAlignment = Alignment.Center) { Text(e, fontSize = 38.sp) } }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            moodColors.forEach { c -> Box(Modifier.size(38.dp).clip(CircleShape).background(Color(android.graphics.Color.parseColor(c))).border(if (color == c) 4.dp else 0.dp, Color.White, CircleShape).clickable { color = c }) }
        }
        Box(Modifier.align(Alignment.CenterHorizontally).clip(RoundedCornerShape(28.dp)).background(chosen).padding(horizontal = 26.dp, vertical = 14.dp)) { Text("$emoji  meu clima", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold) }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            BigButton("Limpar", FieldBg, Modifier.weight(1f)) { save(true) }
            BigButton("Definir", Color(0xFF7C4DFF), Modifier.weight(1.4f)) { save(false) }
        }
    }
}

// ───────── Cápsula do tempo ─────────
@Composable
internal fun QinblyCapsuleDialog(accent: Color, onDismiss: () -> Unit, onNotice: (String) -> Unit) {
    val context = LocalContext.current
    val prefs = remember { sess(context) }
    var toMe by remember { mutableStateOf(true) }
    var who by remember { mutableStateOf("") }
    var text by remember { mutableStateOf("") }
    var date by remember { mutableStateOf<java.time.LocalDate?>(null) }
    var busy by remember { mutableStateOf(false) }
    val today = java.time.LocalDate.now()
    fun pick() {
        val t = today.plusDays(1)
        val d = android.app.DatePickerDialog(context, { _, y, m, day -> date = java.time.LocalDate.of(y, m + 1, day) }, t.year, t.monthValue - 1, t.dayOfMonth)
        d.datePicker.minDate = t.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        d.show()
    }
    fun save() {
        val token = prefs.getString("token", null); val uid = prefs.getString("user_id", null); val d = date
        if (token.isNullOrBlank() || uid.isNullOrBlank()) { onNotice("Entre na sua conta."); return }
        if (text.isBlank()) { onNotice("Escreva a mensagem."); return }
        if (d == null) { onNotice("Escolha quando ela abre."); return }
        if (!toMe && who.isBlank()) { onNotice("Diga para quem é."); return }
        busy = true
        Thread {
            try {
                val target = if (toMe) uid else {
                    val a = JSONArray(QinblyApi.rest(token, "GET", "profiles?username=eq.${who.lowercase()}&select=id"))
                    if (a.length() == 0) throw IllegalStateException("Não achei @$who.")
                    a.getJSONObject(0).getString("id")
                }
                val at = d.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toString()
                QinblyApi.rest(token, "POST", "time_capsules", JSONObject().put("to_user", target).put("body", text.trim()).put("unlock_at", at).toString(), "return=minimal")
                main { onNotice("Cápsula guardada. Abre em ${d.dayOfMonth}/${d.monthValue}/${d.year}."); onDismiss() }
            } catch (e: Exception) { main { busy = false; onNotice(e.message ?: "Não foi possível guardar.") } }
        }.start()
    }
    QinblyBottomPanel("Cápsula do tempo", { if (!busy) onDismiss() }) {
        Text("Escreva agora, abre só na data escolhida.", color = Color(0xFFA9B5C8), fontSize = 16.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { Chip("Para mim", toMe, accent) { toMe = true }; Chip("Para um amigo", !toMe, accent) { toMe = false } }
        if (!toMe) QinblyPillField(who, { who = it.trim().removePrefix("@").take(24) }, "@usuário", Modifier.fillMaxWidth())
        QinblyPillField(text, { text = it.take(2000) }, "Escreva a mensagem…", Modifier.fillMaxWidth().heightIn(min = 130.dp), maxLines = 8)
        Text("Abre em", color = Color(0xFFA9B5C8), fontSize = 16.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Chip("1 semana", date == today.plusWeeks(1), accent) { date = today.plusWeeks(1) }
            Chip("1 mês", date == today.plusMonths(1), accent) { date = today.plusMonths(1) }
            Chip("1 ano", date == today.plusYears(1), accent) { date = today.plusYears(1) }
        }
        Chip(date?.let { "📅  ${it.dayOfMonth}/${it.monthValue}/${it.year}" } ?: "📅  Escolher outra data", false, accent) { pick() }
        BigButton(if (busy) "Guardando…" else "Guardar cápsula", accent, Modifier.fillMaxWidth()) { if (!busy) save() }
    }
}

/** Mostra (uma por vez) as cápsulas que já chegaram na data. */
@Composable
internal fun QinblyCapsuleInbox() {
    val context = LocalContext.current
    val prefs = remember { sess(context) }
    var caps by remember { mutableStateOf<List<JSONObject>>(emptyList()) }
    LaunchedEffect(Unit) {
        val token = prefs.getString("token", null); val uid = prefs.getString("user_id", null)
        if (token.isNullOrBlank() || uid.isNullOrBlank()) return@LaunchedEffect
        caps = try {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                val a = JSONArray(QinblyApi.rest(token, "GET", "time_capsules?to_user=eq.$uid&opened=eq.false&unlock_at=lte.${java.time.Instant.now()}&select=id,body,created_at,profiles!time_capsules_from_user_fkey(username,display_name)&order=unlock_at.asc&limit=20"))
                (0 until a.length()).map { a.getJSONObject(it) }
            }
        } catch (_: Exception) { emptyList() }
    }
    val c = caps.firstOrNull() ?: return
    val fromName = c.optJSONObject("profiles")?.optString("username").orEmpty()
    val made = c.optString("created_at").take(10).split("-").let { if (it.size == 3) "${it[2]}/${it[1]}/${it[0]}" else "" }
    fun close() {
        val id = c.optString("id"); val token = prefs.getString("token", null)
        caps = caps.drop(1)
        if (!token.isNullOrBlank()) Thread { try { QinblyApi.rest(token, "PATCH", "time_capsules?id=eq.$id", JSONObject().put("opened", true).toString(), "return=minimal") } catch (_: Exception) { } }.start()
    }
    QinblyBottomPanel("💌 Cápsula do tempo", { close() }) {
        Text("De @$fromName • guardada em $made", color = Color(0xFFA9B5C8), fontSize = 16.sp)
        Text(c.optString("body"), color = Color.White, fontSize = 19.sp, lineHeight = 26.sp, modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(FieldBg).padding(20.dp))
        BigButton("Fechar", Color(0xFF7C4DFF), Modifier.fillMaxWidth()) { close() }
    }
}

// ───────── Pacotes de figurinhas da comunidade ─────────
private data class Pack(val id: String, val name: String, val owner: String, val paths: List<String>)

@Composable
internal fun QinblyStickerPacksDialog(accent: Color, onDismiss: () -> Unit, onNotice: (String) -> Unit) {
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
        val token = prefs.getString("token", null); val uid = prefs.getString("user_id", null)
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
        val token = prefs.getString("token", null); val uid = prefs.getString("user_id", null)
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
@Composable
internal fun QinblyNeighborhoodBoard(lat: Double, lng: Double, place: String, textColor: Color, secondary: Color, onRefreshLocation: () -> Unit, onNotice: (String) -> Unit) {
    val context = LocalContext.current
    val prefs = remember { sess(context) }
    val myId = remember { prefs.getString("user_id", null) }
    // Aproxima a posição (~1 km) antes de gravar, para não expor o ponto exato de quem avisa.
    val gLat = Math.round(lat * 100) / 100.0
    val gLng = Math.round(lng * 100) / 100.0
    var notices by remember { mutableStateOf<List<JSONObject>?>(null) }
    var draft by remember { mutableStateOf("") }
    var refresh by remember { mutableStateOf(0) }
    LaunchedEffect(refresh, gLat, gLng) {
        val token = prefs.getString("token", null)
        if (token.isNullOrBlank()) { notices = emptyList(); return@LaunchedEffect }
        notices = try {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                val dLat = 0.05; val dLng = 0.05 / Math.cos(Math.toRadians(lat)).coerceAtLeast(0.2)
                val q = "neighborhood_notices?lat=gte.${gLat - dLat}&lat=lte.${gLat + dLat}&lng=gte.${gLng - dLng}&lng=lte.${gLng + dLng}&expires_at=gt.${java.time.Instant.now()}&select=id,body,created_at,user_id,profiles(username,display_name)&order=created_at.desc&limit=50"
                val a = JSONArray(QinblyApi.rest(token, "GET", q))
                (0 until a.length()).map { a.getJSONObject(it) }
            }
        } catch (_: Exception) { notices ?: emptyList() }
    }
    fun post() {
        val token = prefs.getString("token", null); val body = draft.trim()
        if (token.isNullOrBlank() || body.isBlank()) return
        draft = ""
        Thread {
            try {
                QinblyApi.rest(token, "POST", "neighborhood_notices", JSONObject().put("body", body).put("lat", gLat).put("lng", gLng).toString(), "return=minimal")
                main { onNotice("Aviso publicado. Some em 48 horas."); refresh++ }
            } catch (e: Exception) { main { onNotice(e.message ?: "Não foi possível publicar.") } }
        }.start()
    }
    Column(Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Mural do bairro", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = textColor)
                Text(if (place.isNotBlank()) "Perto de $place • avisos somem em 48 h" else "Avisos de até ~5 km • somem em 48 h", color = secondary, fontSize = 12.sp)
            }
            Text("↻", fontSize = 22.sp, color = textColor, modifier = Modifier.clip(CircleShape).clickable { refresh++; onRefreshLocation() }.padding(10.dp))
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            QinblyPillField(draft, { draft = it.take(280) }, "Avise a vizinhança…", Modifier.weight(1f), maxLines = 3)
            Box(Modifier.size(46.dp).clip(CircleShape).background(if (draft.isBlank()) Color(0xFF3A3256) else Color(0xFF7C4DFF)).clickable(enabled = draft.isNotBlank()) { post() }, contentAlignment = Alignment.Center) { Text("➤", color = Color.White, fontSize = 18.sp) }
        }
        val list = notices
        if (list == null) Text("Carregando…", color = secondary)
        else if (list.isEmpty()) Text("Nenhum aviso por perto ainda. Escreva o primeiro: achados e perdidos, eventos, recomendações…", color = secondary, fontSize = 14.sp)
        else LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(list, key = { it.optString("id") }) { n ->
                val p = n.optJSONObject("profiles")
                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(FieldBg).padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text((p?.optString("display_name").orEmpty().ifBlank { p?.optString("username").orEmpty() }) + " • " + qinblyAgo(n.optString("created_at")), color = Color(0xFFC9B8FF), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                        if (n.optString("user_id") == myId) Text("Apagar", color = Color(0xFFE0457B), fontSize = 12.sp, modifier = Modifier.clickable {
                            val id = n.optString("id"); val token = prefs.getString("token", null) ?: return@clickable
                            notices = notices?.filterNot { it.optString("id") == id }
                            Thread { try { QinblyApi.rest(token, "DELETE", "neighborhood_notices?id=eq.$id") } catch (_: Exception) { } }.start()
                        })
                    }
                    Text(n.optString("body"), color = Color.White, fontSize = 15.sp)
                }
            }
        }
    }
}
