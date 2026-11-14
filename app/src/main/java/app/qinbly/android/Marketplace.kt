package app.qinbly.android

import android.content.Context
import android.net.Uri
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

// ───────── Perto de mim: anúncios do bairro (vendas, doações, achados, eventos, serviços) ─────────

private val MarketBlue = Color(0xFF1877F2)
private const val MARKET_STORAGE = "https://jqomgrdlocdqaarfqkay.supabase.co/storage/v1/object/public/post-media/"
private fun marketMain(block: () -> Unit) { Handler(Looper.getMainLooper()).post(block) }

private data class QinblyListing(
    val id: String, val userId: String, val username: String, val displayName: String,
    val kind: String, val title: String, val description: String, val price: Double?, val extra: String,
    val photos: List<String>, val city: String, val createdAt: String
)

private val marketCategories = listOf("Tudo", "Vendas", "Doações", "Achados", "Eventos", "Serviços", "Mural")

private fun kindLabel(kind: String) = when (kind) {
    "venda" -> "Venda"; "doacao" -> "Doação"; "achado" -> "Achado"; "perdido" -> "Perdido"; "evento" -> "Evento"; "servico" -> "Serviço"; else -> "Aviso"
}

private fun kindColor(kind: String) = when (kind) {
    "venda" -> Color(0xFF3A5BA5); "doacao" -> Color(0xFF1D9E75); "achado" -> Color(0xFFE0457B); "perdido" -> Color(0xFFD85A30)
    "evento" -> Color(0xFFFF4F9A); "servico" -> Color(0xFF2F6FDB); else -> Color(0xFF5B5B66)
}

private fun money(v: Double): String = java.text.NumberFormat.getCurrencyInstance(java.util.Locale("pt", "BR")).format(v)

private fun priceText(l: QinblyListing): String = when (l.kind) {
    "venda" -> l.price?.let { money(it) } ?: "Venda"
    "doacao" -> "Grátis"
    "achado" -> "Achado"
    "perdido" -> "Perdido"
    "evento" -> l.extra.ifBlank { "Evento" }
    "servico" -> l.price?.let { money(it) } ?: "Serviço"
    else -> ""
}

private fun photoUrl(p: String): String = if (p.startsWith("http")) p else MARKET_STORAGE + p

private fun parseListing(o: JSONObject): QinblyListing {
    val p = o.optJSONObject("profiles")
    val ph = o.optJSONArray("photos")
    val photos = if (ph == null) emptyList() else (0 until ph.length()).map { ph.optString(it) }.filter { it.isNotBlank() }
    return QinblyListing(
        o.optString("id"), o.optString("user_id"), p?.optString("username").orEmpty(), p?.optString("display_name").orEmpty(),
        o.optString("kind"), o.optString("title"), o.optString("description"),
        if (o.isNull("price")) null else o.optDouble("price"), o.optString("extra"), photos,
        o.optString("city"), o.optString("created_at")
    )
}

/** Sobe uma imagem para o bucket público do app (pasta do próprio usuário) e devolve a URL pública. */
private fun uploadMarketPhoto(context: Context, token: String, userId: String, uri: Uri, index: Int): String {
    val resolver = context.contentResolver
    val type = (resolver.getType(uri) ?: "image/jpeg").lowercase()
    require(type in setOf("image/jpeg", "image/png", "image/webp")) { "Use fotos JPG, PNG ou WebP." }
    val bytes = resolver.openInputStream(uri)?.use { it.readBytes() } ?: throw IllegalStateException("Não foi possível ler a foto.")
    require(bytes.isNotEmpty() && bytes.size <= 15 * 1024 * 1024) { "A foto passa de 15 MB." }
    val ext = when (type) { "image/png" -> "png"; "image/webp" -> "webp"; else -> "jpg" }
    val path = "$userId/market/${java.util.UUID.randomUUID()}-$index.$ext"
    val c = (URL("https://jqomgrdlocdqaarfqkay.supabase.co/storage/v1/object/post-media/$path").openConnection() as HttpURLConnection).apply {
        requestMethod = "POST"; connectTimeout = 20000; readTimeout = 60000; doOutput = true
        setRequestProperty("apikey", BuildConfig.SUPABASE_PUBLISHABLE_KEY)
        setRequestProperty("Authorization", "Bearer $token")
        setRequestProperty("Content-Type", type)
        setFixedLengthStreamingMode(bytes.size)
    }
    try {
        c.outputStream.use { it.write(bytes) }
        val status = c.responseCode
        if (status !in 200..299) throw IllegalStateException("Falha ao enviar a foto (HTTP $status).")
    } finally { c.disconnect() }
    return MARKET_STORAGE + path
}

/** Tela "Perto de mim" depois que a localização foi encontrada: categorias em cima, anúncios embaixo e "+" para publicar. */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
internal fun QinblyNearbyHub(
    lat: Double, lng: Double, place: String, textColor: Color, secondary: Color,
    onRefreshLocation: () -> Unit, onOpenConversation: (String) -> Unit, onNotice: (String) -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("qinbly_session", Context.MODE_PRIVATE) }
    val myId = remember { QinblyApi.currentUserId(prefs) }
    // Cidade: a da localização, ou a que a pessoa escolheu (fica guardada no aparelho).
    var chosenCity by remember { mutableStateOf(prefs.getString("market_city", null)?.takeIf { it.isNotBlank() }) }
    val city = chosenCity ?: place
    var showCityPicker by remember { mutableStateOf(false) }
    var savedIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var cat by remember { mutableStateOf("Tudo") }
    var sub by remember { mutableStateOf("achado") }
    var listings by remember { mutableStateOf<List<QinblyListing>?>(null) }
    var failed by remember { mutableStateOf(false) }
    var refresh by remember { mutableIntStateOf(0) }
    var open by remember { mutableStateOf<QinblyListing?>(null) }
    var showMenu by remember { mutableStateOf(false) }
    var createKind by remember { mutableStateOf<String?>(null) }
    var mine by remember { mutableStateOf<String?>(null) }
    var showDrawer by remember { mutableStateOf(false) }
    var info by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(refresh) {
        val token = prefs.getString("token", null) ?: return@LaunchedEffect
        try {
            savedIds = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                val a = JSONArray(QinblyApi.rest(token, "GET", "marketplace_saves?select=listing_id&limit=500"))
                (0 until a.length()).map { a.getJSONObject(it).optString("listing_id") }.toSet()
            }
        } catch (_: Exception) { }
    }
    LaunchedEffect(cat, sub, refresh, city, mine) {
        if (cat == "Mural" && mine == null) return@LaunchedEffect
        val token = prefs.getString("token", null)
        if (token.isNullOrBlank()) { listings = emptyList(); return@LaunchedEffect }
        listings = null; failed = false
        listings = try {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                val kindFilter = when (cat) {
                    "Vendas" -> "&kind=eq.venda"; "Doações" -> "&kind=eq.doacao"; "Achados" -> "&kind=eq.$sub"
                    "Eventos" -> "&kind=eq.evento"; "Serviços" -> "&kind=eq.servico"; else -> ""
                }
                val cols = "id,user_id,kind,title,description,price,extra,photos,city,created_at,profiles!marketplace_listings_user_id_fkey(username,display_name)"
                if (mine == "saved") {
                    val a = JSONArray(QinblyApi.rest(token, "GET", "marketplace_saves?select=created_at,marketplace_listings($cols)&order=created_at.desc&limit=100"))
                    return@withContext (0 until a.length()).mapNotNull { a.getJSONObject(it).optJSONObject("marketplace_listings")?.let { o -> parseListing(o) } }
                }
                val q = if (mine != null) "marketplace_listings?user_id=eq.$myId" + (if (mine == "doacao") "&kind=eq.doacao" else "") +
                    "&select=$cols&order=created_at.desc&limit=100"
                else "marketplace_listings?city=eq.${java.net.URLEncoder.encode(city, "UTF-8")}" +
                    "&expires_at=gt.${java.time.Instant.now()}$kindFilter&select=$cols&order=created_at.desc&limit=60"
                val a = JSONArray(QinblyApi.rest(token, "GET", q))
                (0 until a.length()).map { parseListing(a.getJSONObject(it)) }
            }
        } catch (_: Exception) { failed = true; emptyList() }
    }

    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(start = 8.dp, end = 12.dp, top = 6.dp, bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.size(44.dp).clip(CircleShape).clickable { showDrawer = true }, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                for (i in 0 until 3) Box(Modifier.padding(vertical = 2.5.dp).size(width = 20.dp, height = 2.5.dp).clip(CircleShape).background(textColor))
            }
            Column(Modifier.weight(1f).padding(start = 4.dp)) {
                if (mine != null) {
                    Text(if (mine == "doacao") "Minhas doações" else if (mine == "saved") "Anúncios salvos" else "Meus anúncios", color = textColor, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    Text("Toque nos três tracinhos para voltar", color = secondary, fontSize = 12.sp)
                } else {
                    Row(Modifier.clip(RoundedCornerShape(20.dp)).background(QiblyTheme.soft).clickable { showCityPicker = true }.padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(if (city.isNotBlank()) city else "Escolher cidade", color = textColor, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                        Text("  ▾", color = secondary, fontSize = 13.sp)
                    }
                    Text("Anúncios somem em 7 dias", color = secondary, fontSize = 11.sp, modifier = Modifier.padding(start = 6.dp, top = 2.dp))
                }
            }
        }
        if (mine == null) LazyRow(contentPadding = PaddingValues(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(vertical = 6.dp)) {
            items(marketCategories) { name ->
                val selected = name == cat
                val (emoji, tone) = chipStyle(name)
                Row(
                    Modifier.clip(RoundedCornerShape(20.dp)).background(if (selected) QiblyTheme.text else QiblyTheme.soft).clickable { cat = name }.padding(start = 6.dp, end = 14.dp, top = 5.dp, bottom = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(Modifier.size(24.dp).clip(CircleShape).background(tone), contentAlignment = Alignment.Center) { Text(emoji, fontSize = 12.sp) }
                    Text(name, color = if (selected) QiblyTheme.background else secondary, fontSize = 13.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium, modifier = Modifier.padding(start = 7.dp))
                }
            }
        }
        if (cat == "Achados" && mine == null) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 2.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for ((key, label) in listOf("achado" to "Achados", "perdido" to "Perdidos")) {
                    val selected = sub == key
                    Text(
                        label, color = if (selected) QiblyTheme.background else secondary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f).clip(RoundedCornerShape(18.dp)).background(if (selected) QiblyTheme.text else QiblyTheme.soft)
                            .clickable { sub = key }.padding(vertical = 8.dp)
                    )
                }
            }
        }
        Box(Modifier.fillMaxWidth().weight(1f)) {
            if (cat == "Mural" && mine == null) {
                QinblyNeighborhoodBoard(lat, lng, place, textColor, secondary, onRefreshLocation = onRefreshLocation, onNotice = onNotice)
            } else {
                val list = listings
                var pulling by remember { mutableStateOf(false) }
                LaunchedEffect(listings, failed) { if (listings != null || failed) pulling = false }
                androidx.compose.material3.pulltorefresh.PullToRefreshBox(
                    isRefreshing = pulling, onRefresh = { pulling = true; refresh++ }, modifier = Modifier.fillMaxSize()
                ) {
                    when {
                        list == null -> Box(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), contentAlignment = Alignment.Center) { Text("Carregando…", color = secondary, modifier = Modifier.padding(top = 120.dp)) }
                        failed -> Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            Text("Não foi possível carregar os anúncios", color = textColor, fontWeight = FontWeight.Bold, fontSize = 16.sp, textAlign = TextAlign.Center)
                            Text("Puxe a tela para baixo ou toque abaixo para tentar de novo.", color = secondary, fontSize = 14.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 6.dp))
                            Text("Tentar de novo", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp,
                                modifier = Modifier.padding(top = 14.dp).clip(RoundedCornerShape(20.dp)).background(MarketBlue).clickable { refresh++ }.padding(horizontal = 20.dp, vertical = 10.dp))
                        }
                        list.isEmpty() && mine != null -> Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            Text(if (mine == "saved") "Nenhum anúncio salvo ainda" else "Você ainda não publicou nada", color = textColor, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                            Text("Abra o menu (três tracinhos) e toque em Novo anúncio.", color = secondary, fontSize = 14.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 6.dp))
                        }
                        list.isEmpty() -> Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 28.dp, vertical = 24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            Box(Modifier.size(124.dp).clip(CircleShape).background(MarketBlue.copy(alpha = 0.22f)), contentAlignment = Alignment.Center) {
                                Box(Modifier.size(84.dp).clip(CircleShape).background(androidx.compose.ui.graphics.Brush.linearGradient(listOf(MarketBlue, Color(0xFF7C4DFF)))), contentAlignment = Alignment.Center) {
                                    Text("+", color = Color.White, fontSize = 44.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Text("Seja o primeiro de " + city.substringBefore(",").ifBlank { "sua cidade" }, color = textColor, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 22.dp))
                            Text("Venda, doe ou avise algo para os vizinhos.", color = secondary, fontSize = 14.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 6.dp))
                            Text("Publicar anúncio", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp,
                                modifier = Modifier.padding(top = 18.dp).clip(RoundedCornerShape(26.dp)).background(MarketBlue).clickable { showMenu = true }.padding(horizontal = 34.dp, vertical = 13.dp))
                            Row(Modifier.padding(top = 26.dp), horizontalArrangement = Arrangement.spacedBy(22.dp)) {
                                for ((label, emoji, tone, key) in listOf(
                                    listOf("Vender", "\uD83D\uDCB2", 0xFF3A5BA5, "venda"), listOf("Doar", "\uD83C\uDF81", 0xFF1D9E75, "doacao"),
                                    listOf("Achados", "\uD83D\uDD0E", 0xFFE0457B, "achado"), listOf("Eventos", "\uD83C\uDF89", 0xFFFF4F9A, "evento")
                                ).map { Quad(it[0] as String, it[1] as String, it[2] as Long, it[3] as String) }) {
                                    Column(Modifier.clickable { createKind = key }, horizontalAlignment = Alignment.CenterHorizontally) {
                                        Box(Modifier.size(46.dp).clip(CircleShape).background(Color(tone)), contentAlignment = Alignment.Center) { Text(emoji, fontSize = 20.sp) }
                                        Text(label, color = textColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 6.dp))
                                    }
                                }
                            }
                        }
                        else -> LazyVerticalGrid(
                            columns = GridCells.Fixed(2), contentPadding = PaddingValues(start = 10.dp, end = 10.dp, top = 6.dp, bottom = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()
                        ) {
                            val donations = list.count { it.kind == "doacao" }
                            if (cat == "Tudo" && mine == null && donations > 0) item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                                Row(
                                    Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(androidx.compose.ui.graphics.Brush.horizontalGradient(listOf(Color(0xFF1D9E75), Color(0xFF2DD4BF))))
                                        .clickable { cat = "Doações" }.padding(horizontal = 18.dp, vertical = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(Modifier.weight(1f)) {
                                        Text("Doações perto de você", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                                        Text(if (donations == 1) "1 item de graça" else "$donations itens de graça", color = Color(0xFFE6FFF5), fontSize = 12.sp)
                                    }
                                    Text("›", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            if (cat == "Tudo" && mine == null) item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                                Text("Recentes", color = textColor, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, modifier = Modifier.padding(start = 4.dp, top = 6.dp))
                            }
                            gridItems(list, key = { it.id }) { l -> ListingTile(l, cat == "Tudo", textColor, secondary) { open = l } }
                        }
                    }
                }
            }
        }
    }

    if (showDrawer) {
        QinblyDialog(onDismissRequest = { showDrawer = false }, properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false), lift = false) {
            Box(Modifier.fillMaxSize().background(Color(0x99000000)).clickable(indication = null, interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }) { showDrawer = false }) {
                Column(
                    Modifier.align(Alignment.CenterStart).fillMaxSize().padding(end = 64.dp).background(Color(0xFF1F1F21))
                        .clickable(indication = null, interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }) { }
                        .statusBarsPadding().verticalScroll(rememberScrollState()).padding(horizontal = 14.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val myName = (prefs.getString("display_name", "")?.takeIf { it.isNotBlank() } ?: prefs.getString("username", "").orEmpty().removePrefix("@")).ifBlank { "Você" }
                    Row(Modifier.padding(start = 4.dp, top = 6.dp, bottom = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(52.dp).clip(CircleShape).background(Color(0xFF3C3C3E)), contentAlignment = Alignment.Center) {
                            Text(myName.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                        }
                        Column(Modifier.padding(start = 14.dp)) {
                            Text(myName, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text((if (city.isNotBlank()) city else "Escolher cidade") + " ▾", color = Color(0xFFA9B5C8), fontSize = 13.sp,
                                modifier = Modifier.clickable { showDrawer = false; mine = null; showCityPicker = true }.padding(top = 2.dp))
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        for ((label, emoji, color, target) in listOf(
                            listOf("Anúncios", "\uD83C\uDFF7\uFE0F", 0xFF3A5BA5, "all"), listOf("Salvos", "\uD83D\uDD16", 0xFFE0A800, "saved"), listOf("Doações", "\uD83C\uDF81", 0xFF1D9E75, "doacao")
                        ).map { Quad(it[0] as String, it[1] as String, it[2] as Long, it[3] as String) }) {
                            Column(
                                Modifier.weight(1f).clip(RoundedCornerShape(18.dp)).background(Color(0xFF262628)).clickable { showDrawer = false; mine = target }.padding(vertical = 14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(Modifier.size(36.dp).clip(CircleShape).background(Color(color)), contentAlignment = Alignment.Center) { Text(emoji, fontSize = 17.sp) }
                                Text(label, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp))
                            }
                        }
                    }
                    Box(Modifier.fillMaxWidth().padding(vertical = 6.dp).height(1.dp).background(Color(0xFF2E2E30)))
                    for ((label, emoji, color) in listOf(
                        Triple("Trocar cidade", "\uD83D\uDCCD", 0xFF1877F2L), Triple("Dicas de segurança", "\uD83D\uDEE1\uFE0F", 0xFF7C4DFFL), Triple("Regras da comunidade", "\uD83D\uDCCB", 0xFF6B7280L)
                    )) {
                        Row(
                            Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).clickable {
                                showDrawer = false
                                if (label == "Trocar cidade") { mine = null; showCityPicker = true } else info = label
                            }.padding(horizontal = 6.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(Modifier.size(34.dp).clip(CircleShape).background(Color(color)), contentAlignment = Alignment.Center) { Text(emoji, fontSize = 16.sp) }
                            Text(label, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, modifier = Modifier.padding(start = 14.dp).weight(1f))
                            Text("›", color = Color(0xFFA9B5C8), fontSize = 22.sp)
                        }
                    }
                    if (mine != null) Text("← Voltar para Perto de mim", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp,
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color(0xFF262628)).clickable { showDrawer = false; mine = null }.padding(horizontal = 16.dp, vertical = 14.dp))
                    Spacer(Modifier.height(6.dp))
                    Text("+ Novo anúncio", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp, textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(26.dp)).background(MarketBlue).clickable { showDrawer = false; showMenu = true }.padding(vertical = 14.dp))
                }
            }
        }
    }
    info?.let { which ->
        QinblyBottomPanel(which, onDismiss = { info = null }) {
            val lines = if (which == "Dicas de segurança") listOf(
                "Combine a troca em local público e movimentado, de preferência de dia.",
                "Não envie dinheiro adiantado nem códigos que chegarem por mensagem.",
                "Confira o item antes de pagar e desconfie de preço bom demais.",
                "Seu endereço não aparece: o anúncio mostra só a cidade e uma distância aproximada.",
                "Algo suspeito? Pare a conversa e avise a equipe do QINBLY."
            ) else listOf(
                "Anuncie só o que é seu e que você pode entregar.",
                "Nada de armas, drogas, animais silvestres, produtos falsificados ou conteúdo adulto.",
                "Respeite os vizinhos: sem ofensas, golpes ou spam.",
                "Doações são de graça de verdade: não cobre nada de quem pedir.",
                "Anúncios que quebram as regras podem ser removidos."
            )
            for (l in lines) Text("• " + l, color = Color(0xFFE6EAF2), fontSize = 15.sp)
        }
    }

    if (showCityPicker) {
        CityPicker(
            current = city, deviceCity = place, prefs = prefs,
            onPick = { c ->
                chosenCity = c.takeIf { it != place }
                prefs.edit().putString("market_city", if (c == place) "" else c).apply()
                showCityPicker = false; mine = null
                refresh++
            },
            onDismiss = { showCityPicker = false }
        )
    }
    open?.let { l ->
        ListingDetail(
            l, mine = l.userId == myId, saved = l.id in savedIds, textColor = textColor, secondary = secondary,
            onSave = {
                val token = prefs.getString("token", null) ?: return@ListingDetail
                val now = l.id !in savedIds
                savedIds = if (now) savedIds + l.id else savedIds - l.id
                Thread {
                    try {
                        if (now) QinblyApi.rest(token, "POST", "marketplace_saves", JSONObject().put("listing_id", l.id).toString(), "return=minimal")
                        else QinblyApi.rest(token, "DELETE", "marketplace_saves?listing_id=eq.${l.id}")
                    } catch (_: Exception) { }
                }.start()
            },
            onDismiss = { open = null },
            onChat = { open = null; if (l.username.isNotBlank()) onOpenConversation(l.username) },
            onDelete = {
                val token = prefs.getString("token", null) ?: return@ListingDetail
                open = null
                listings = listings?.filterNot { it.id == l.id }
                Thread { try { QinblyApi.rest(token, "DELETE", "marketplace_listings?id=eq.${l.id}") } catch (_: Exception) { } }.start()
                onNotice("Anúncio apagado.")
            }
        )
    }
    if (showMenu) {
        QinblyBottomPanel("O que você quer publicar?", onDismiss = { showMenu = false }) {
            val options = listOf(
                Triple("venda", "Vender", "Usados e desapegos"), Triple("doacao", "Doar", "Dê de graça para quem precisa"),
                Triple("achado", "Achado ou perdido", "Chaves, pets, documentos"), Triple("evento", "Evento", "Feiras, encontros"),
                Triple("servico", "Serviço", "Ofereça ou peça um serviço"), Triple("aviso", "Aviso ao bairro", "Recado que some em 48 h")
            )
            for ((key, title, sub2) in options) {
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Color(0xFF262628)).clickable {
                        showMenu = false
                        if (key == "aviso") cat = "Mural" else createKind = key
                    }.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(Modifier.size(36.dp).clip(CircleShape).background(kindColor(key)))
                    Column(Modifier.padding(start = 12.dp).weight(1f)) {
                        Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(sub2, color = Color(0xFFA9B5C8), fontSize = 12.sp)
                    }
                    Text("›", color = Color(0xFFA9B5C8), fontSize = 22.sp)
                }
            }
        }
    }
    createKind?.let { k ->
        ListingForm(k, city, onDismiss = { createKind = null }, onPosted = { posted ->
            createKind = null
            cat = when (posted) { "venda" -> "Vendas"; "doacao" -> "Doações"; "achado", "perdido" -> "Achados"; "evento" -> "Eventos"; else -> "Serviços" }
            if (posted == "achado" || posted == "perdido") sub = posted
            refresh++
        }, onNotice = onNotice)
    }
}

@Composable
private fun ListingTile(l: QinblyListing, showTag: Boolean, textColor: Color, secondary: Color, onClick: () -> Unit) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(QiblyTheme.card).clickable { onClick() }.padding(6.dp)) {
        Box(Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(12.dp)).background(QiblyTheme.soft)) {
            if (l.photos.isNotEmpty()) AsyncImage(model = photoUrl(l.photos.first()), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            else Box(Modifier.fillMaxSize().background(kindColor(l.kind).copy(alpha = 0.55f)), contentAlignment = Alignment.Center) {
                Text(kindLabel(l.kind), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            if (showTag) Text(kindLabel(l.kind), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(6.dp).clip(RoundedCornerShape(9.dp)).background(Color(0x99000000)).padding(horizontal = 7.dp, vertical = 3.dp))
        }
        Text(priceText(l), color = textColor, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(start = 4.dp, top = 8.dp))
        Text(l.title, color = textColor, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(horizontal = 4.dp))
        Text((l.city.substringBefore(",").ifBlank { "" }.let { if (it.isNotBlank()) "$it • " else "" }) + qinblyAgo(l.createdAt), color = secondary, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(start = 4.dp, end = 4.dp, bottom = 4.dp))
    }
}

@Composable
private fun ListingDetail(
    l: QinblyListing, mine: Boolean, saved: Boolean, textColor: Color, secondary: Color,
    onSave: () -> Unit, onDismiss: () -> Unit, onChat: () -> Unit, onDelete: () -> Unit
) {
    QinblyDialog(onDismissRequest = onDismiss, properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false), lift = true) {
        Box(Modifier.fillMaxSize().background(QiblyTheme.background)) {
            Column(Modifier.fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState())) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(40.dp).clip(CircleShape).background(QiblyTheme.soft).clickable { onDismiss() }, contentAlignment = Alignment.Center) { Text("←", color = textColor, fontSize = 18.sp) }
                    Text(kindLabel(l.kind), color = textColor, fontWeight = FontWeight.SemiBold, fontSize = 17.sp, modifier = Modifier.padding(start = 14.dp).weight(1f))
                    Text(if (saved) "Salvo" else "Salvar", color = if (saved) Color(0xFFFFC93C) else textColor, fontWeight = FontWeight.Bold, fontSize = 14.sp,
                        modifier = Modifier.clip(RoundedCornerShape(18.dp)).background(QiblyTheme.soft).clickable { onSave() }.padding(horizontal = 16.dp, vertical = 9.dp))
                }
                if (l.photos.isNotEmpty()) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp), contentPadding = PaddingValues(horizontal = 12.dp)) {
                        items(l.photos) { p ->
                            AsyncImage(model = photoUrl(p), contentDescription = null, contentScale = ContentScale.Crop,
                                modifier = Modifier.width(300.dp).height(300.dp).clip(RoundedCornerShape(16.dp)).background(QiblyTheme.soft))
                        }
                    }
                }
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (priceText(l).isNotBlank()) Text(priceText(l), color = textColor, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp)
                    Text(l.title, color = textColor, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(kindLabel(l.kind) + (if (l.city.isNotBlank()) " • " + l.city else "") + " • " + qinblyAgo(l.createdAt), color = secondary, fontSize = 13.sp)
                    if (l.extra.isNotBlank() && l.kind != "evento") Text("Estado: " + l.extra, color = secondary, fontSize = 13.sp)
                    if (l.description.isNotBlank()) Text(l.description, color = textColor, fontSize = 15.sp, modifier = Modifier.padding(top = 8.dp))
                    Text("Anunciado por " + l.displayName.ifBlank { l.username }.ifBlank { "um vizinho" }, color = secondary, fontSize = 13.sp, modifier = Modifier.padding(top = 10.dp))
                    Spacer(Modifier.height(10.dp))
                    if (mine) Text("Apagar anúncio", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp, textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(26.dp)).background(Color(0xFFE0457B)).clickable { onDelete() }.padding(vertical = 14.dp))
                    else Text("Chamar no chat", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp, textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(26.dp)).background(MarketBlue).clickable { onChat() }.padding(vertical = 14.dp))
                    if (!mine) Text("Combine em local público e movimentado.", color = secondary, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 6.dp))
                }
            }
        }
    }
}

@Composable
private fun ListingForm(kind: String, city: String, onDismiss: () -> Unit, onPosted: (String) -> Unit, onNotice: (String) -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("qinbly_session", Context.MODE_PRIVATE) }
    var subKind by remember { mutableStateOf(if (kind == "achado") "achado" else kind) }
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var priceTxt by remember { mutableStateOf("") }
    var extra by remember { mutableStateOf(if (kind == "venda" || kind == "doacao") "Bom" else "") }
    val photos: SnapshotStateList<Uri> = remember { mutableStateListOf() }
    var posting by remember { mutableStateOf(false) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(6)) { uris ->
        for (u in uris) if (photos.size < 6) photos.add(u)
    }
    val heading = when (kind) { "venda" -> "Novo anúncio"; "doacao" -> "Nova doação"; "achado" -> "Achado ou perdido"; "evento" -> "Novo evento"; else -> "Novo serviço" }
    val needsPhoto = kind == "venda" || kind == "doacao"
    val hasPrice = kind == "venda" || kind == "servico"

    fun publish() {
        val token = prefs.getString("token", null)
        val uid = QinblyApi.currentUserId(prefs)
        if (token.isNullOrBlank() || uid.isNullOrBlank()) { onNotice("Entre na sua conta para publicar."); return }
        val t = title.trim()
        if (t.isBlank()) { onNotice("Escreva um título."); return }
        if (city.isBlank()) { onNotice("Escolha a cidade no topo antes de publicar."); return }
        if (needsPhoto && photos.isEmpty()) { onNotice("Adicione pelo menos uma foto."); return }
        val price = priceTxt.trim().replace(",", ".").toDoubleOrNull()
        if (kind == "venda" && price == null) { onNotice("Informe o preço."); return }
        posting = true
        val picked = photos.toList()
        Thread {
            try {
                val urls = picked.mapIndexed { i, u -> uploadMarketPhoto(context, token, uid, u, i) }
                val body = JSONObject().put("kind", subKind).put("title", t.take(120)).put("description", desc.trim().take(2000))
                    .put("extra", extra.trim().take(80)).put("photos", JSONArray(urls)).put("city", city)
                if (hasPrice && price != null) body.put("price", price)
                QinblyApi.rest(token, "POST", "marketplace_listings", body.toString(), "return=minimal")
                marketMain { posting = false; onNotice("Publicado! Fica no ar por 7 dias."); onPosted(subKind) }
            } catch (e: Exception) {
                marketMain { posting = false; onNotice(e.message ?: "Não foi possível publicar.") }
            }
        }.start()
    }

    QinblyDialog(onDismissRequest = { if (!posting) onDismiss() }, properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false), lift = true) {
        Box(Modifier.fillMaxSize().background(QiblyTheme.background)) {
            Column(Modifier.fillMaxSize().statusBarsPadding().imePadding().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(40.dp).clip(CircleShape).background(QiblyTheme.soft).clickable { if (!posting) onDismiss() }, contentAlignment = Alignment.Center) { Text("✕", color = QiblyTheme.text, fontSize = 18.sp) }
                    Text(heading, color = QiblyTheme.text, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(start = 14.dp))
                }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (photos.size < 6) item {
                        Column(Modifier.size(92.dp).clip(RoundedCornerShape(14.dp)).background(QiblyTheme.soft).clickable {
                            picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            Text("+", color = MarketBlue, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                            Text("Foto", color = QiblyTheme.secondary, fontSize = 11.sp)
                        }
                    }
                    items(photos.toList()) { u ->
                        Box(Modifier.size(92.dp).clip(RoundedCornerShape(14.dp))) {
                            AsyncImage(model = u, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                            Text("✕", color = Color.White, fontSize = 12.sp, modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).clip(CircleShape).background(Color(0x99000000)).clickable { photos.remove(u) }.padding(horizontal = 7.dp, vertical = 3.dp))
                        }
                    }
                }
                Text(if (needsPhoto) "Até 6 fotos (pelo menos 1)" else "Até 6 fotos (opcional)", color = QiblyTheme.secondary, fontSize = 12.sp)
                if (kind == "achado") {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        for ((key, label) in listOf("achado" to "Achei algo", "perdido" to "Perdi algo")) {
                            val selected = subKind == key
                            Text(label, color = if (selected) QiblyTheme.background else QiblyTheme.secondary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f).clip(RoundedCornerShape(20.dp)).background(if (selected) QiblyTheme.text else QiblyTheme.soft).clickable { subKind = key }.padding(vertical = 10.dp))
                        }
                    }
                }
                QinblyPillField(title, { title = it.take(120) }, when (kind) { "venda" -> "O que você está vendendo?"; "doacao" -> "O que você está doando?"; "achado" -> "O que foi?"; "evento" -> "Nome do evento"; else -> "Qual serviço?" }, Modifier.fillMaxWidth())
                QinblyPillField(desc, { desc = it.take(2000) }, "Descrição", Modifier.fillMaxWidth(), maxLines = 5)
                if (hasPrice) QinblyPillField(priceTxt, { priceTxt = it.filter { c -> c.isDigit() || c == ',' || c == '.' }.take(10) }, if (kind == "venda") "Preço (R$)" else "Preço (R$, opcional)", Modifier.fillMaxWidth())
                if (kind == "evento") QinblyPillField(extra, { extra = it.take(80) }, "Quando? (ex.: sábado, 9h)", Modifier.fillMaxWidth())
                if (kind == "venda" || kind == "doacao") {
                    Text("Estado", color = QiblyTheme.secondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (s in listOf("Novo", "Bom", "Usado")) {
                            val selected = extra == s
                            Text(s, color = if (selected) QiblyTheme.background else QiblyTheme.secondary, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium, fontSize = 14.sp,
                                modifier = Modifier.clip(RoundedCornerShape(18.dp)).background(if (selected) QiblyTheme.text else QiblyTheme.soft).clickable { extra = s }.padding(horizontal = 18.dp, vertical = 9.dp))
                        }
                    }
                }
                if (kind == "doacao") Text("Sem preço: aparece como “Grátis”.", color = Color(0xFF63D68D), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Text("Cidade do anúncio: " + city.ifBlank { "não definida" } + ". Para trocar, feche e escolha a cidade no topo. O anúncio some em 7 dias.", color = QiblyTheme.secondary, fontSize = 12.sp)
                Text(if (posting) "Publicando…" else "Publicar", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp, textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)).background(if (posting) Color(0xFF3C3C3E) else MarketBlue).clickable(enabled = !posting) { publish() }.padding(vertical = 15.dp))
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

/** Escolher a cidade: usar a da localização, buscar outra pelo nome ou voltar a uma recente. */
@Composable
private fun CityPicker(current: String, deviceCity: String, prefs: android.content.SharedPreferences, onPick: (String) -> Unit, onDismiss: () -> Unit) {
    val context = LocalContext.current
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<String>>(emptyList()) }
    var searching by remember { mutableStateOf(false) }
    val recents = remember {
        val raw = prefs.getString("market_recent_cities", "[]") ?: "[]"
        try { val a = JSONArray(raw); (0 until a.length()).map { a.optString(it) }.filter { it.isNotBlank() } } catch (_: Exception) { emptyList<String>() }
    }
    fun choose(c: String) {
        val list = (listOf(c) + recents.filter { it != c }).take(6)
        prefs.edit().putString("market_recent_cities", JSONArray(list).toString()).apply()
        onPick(c)
    }
    LaunchedEffect(query) {
        val q = query.trim()
        if (q.length < 3) { results = emptyList(); return@LaunchedEffect }
        kotlinx.coroutines.delay(500)
        searching = true
        results = try {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                @Suppress("DEPRECATION")
                val found = android.location.Geocoder(context, java.util.Locale("pt", "BR")).getFromLocationName("$q, Brasil", 10).orEmpty()
                found.mapNotNull { a -> listOfNotNull(a.subAdminArea ?: a.locality, a.adminArea).distinct().joinToString(", ").takeIf { it.contains(",") } }.distinct()
            }
        } catch (_: Exception) { emptyList() }
        searching = false
    }
    QinblyBottomPanel("Escolha a cidade", onDismiss = onDismiss) {
        QinblyPillField(query, { query = it.take(60) }, "Buscar cidade…", Modifier.fillMaxWidth())
        if (deviceCity.isNotBlank()) Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color(0xFF262628)).clickable { choose(deviceCity) }.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("Usar minha localização", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(deviceCity, color = Color(0xFFA9B5C8), fontSize = 12.sp)
            }
            if (current == deviceCity) Text("✓", color = MarketBlue, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        if (query.trim().length >= 3) {
            if (searching) Text("Buscando…", color = Color(0xFFA9B5C8), fontSize = 13.sp)
            else if (results.isEmpty()) Text("Nenhuma cidade encontrada.", color = Color(0xFFA9B5C8), fontSize = 13.sp)
            for (c in results) CityRow(c, c == current) { choose(c) }
        } else if (recents.isNotEmpty()) {
            Text("Cidades recentes", color = Color(0xFFA9B5C8), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            for (c in recents) CityRow(c, c == current) { choose(c) }
        }
    }
}

@Composable
private fun CityRow(name: String, selected: Boolean, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Color(0xFF262628)).clickable { onClick() }.padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(name, color = Color.White, fontSize = 15.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium, modifier = Modifier.weight(1f))
        if (selected) Text("✓", color = MarketBlue, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

private data class Quad(val a: String, val b: String, val c: Long, val d: String)

private fun chipStyle(name: String): Pair<String, Color> = when (name) {
    "Tudo" -> "\u2728" to Color(0xFF7C4DFF)
    "Vendas" -> "\uD83D\uDCB2" to Color(0xFF3A5BA5)
    "Doações" -> "\uD83C\uDF81" to Color(0xFF1D9E75)
    "Achados" -> "\uD83D\uDD0E" to Color(0xFFE0457B)
    "Eventos" -> "\uD83C\uDF89" to Color(0xFFFF4F9A)
    "Serviços" -> "\uD83D\uDEE0\uFE0F" to Color(0xFF2F6FDB)
    else -> "\uD83D\uDCAC" to Color(0xFF6B7280)
}
