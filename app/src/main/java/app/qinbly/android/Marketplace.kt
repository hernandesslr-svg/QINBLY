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
    val photos: List<String>, val lat: Double, val lng: Double, val createdAt: String
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

private fun distanceText(lat1: Double, lng1: Double, lat2: Double, lng2: Double): String {
    val r = 6371.0
    val dLat = Math.toRadians(lat2 - lat1); val dLng = Math.toRadians(lng2 - lng1)
    val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLng / 2) * Math.sin(dLng / 2)
    val km = 2 * r * Math.asin(Math.sqrt(a))
    return if (km < 1.0) "a menos de 1 km" else String.format(java.util.Locale("pt", "BR"), "a ~%.1f km", km)
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
        o.optDouble("lat"), o.optDouble("lng"), o.optString("created_at")
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
@Composable
internal fun QinblyNearbyHub(
    lat: Double, lng: Double, place: String, textColor: Color, secondary: Color,
    onRefreshLocation: () -> Unit, onOpenConversation: (String) -> Unit, onNotice: (String) -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("qinbly_session", Context.MODE_PRIVATE) }
    val myId = remember { QinblyApi.currentUserId(prefs) }
    // Posição aproximada (~1 km) antes de gravar, para não expor o ponto exato de quem anuncia.
    val gLat = Math.round(lat * 100) / 100.0
    val gLng = Math.round(lng * 100) / 100.0
    var cat by remember { mutableStateOf("Tudo") }
    var sub by remember { mutableStateOf("achado") }
    var listings by remember { mutableStateOf<List<QinblyListing>?>(null) }
    var failed by remember { mutableStateOf(false) }
    var refresh by remember { mutableIntStateOf(0) }
    var open by remember { mutableStateOf<QinblyListing?>(null) }
    var showMenu by remember { mutableStateOf(false) }
    var createKind by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(cat, sub, refresh, gLat, gLng) {
        if (cat == "Mural") return@LaunchedEffect
        val token = prefs.getString("token", null)
        if (token.isNullOrBlank()) { listings = emptyList(); return@LaunchedEffect }
        listings = null; failed = false
        listings = try {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                val dLat = 0.05; val dLng = 0.05 / Math.cos(Math.toRadians(lat)).coerceAtLeast(0.2)
                val kindFilter = when (cat) {
                    "Vendas" -> "&kind=eq.venda"; "Doações" -> "&kind=eq.doacao"; "Achados" -> "&kind=eq.$sub"
                    "Eventos" -> "&kind=eq.evento"; "Serviços" -> "&kind=eq.servico"; else -> ""
                }
                val q = "marketplace_listings?lat=gte.${gLat - dLat}&lat=lte.${gLat + dLat}&lng=gte.${gLng - dLng}&lng=lte.${gLng + dLng}" +
                    "&expires_at=gt.${java.time.Instant.now()}$kindFilter" +
                    "&select=id,user_id,kind,title,description,price,extra,photos,lat,lng,created_at,profiles(username,display_name)&order=created_at.desc&limit=60"
                val a = JSONArray(QinblyApi.rest(token, "GET", q))
                (0 until a.length()).map { parseListing(a.getJSONObject(it)) }
            }
        } catch (_: Exception) { failed = true; emptyList() }
    }

    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(start = 16.dp, end = 12.dp, top = 6.dp, bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(if (place.isNotBlank()) place else "Perto de você", color = textColor, fontWeight = FontWeight.Bold, fontSize = 17.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("Até ~5 km • anúncios somem em 7 dias", color = secondary, fontSize = 12.sp)
            }
            Text("↻", fontSize = 22.sp, color = textColor, modifier = Modifier.clip(CircleShape).clickable { refresh++; onRefreshLocation() }.padding(8.dp))
            Box(Modifier.padding(start = 4.dp).size(38.dp).clip(CircleShape).background(MarketBlue).clickable { showMenu = true }, contentAlignment = Alignment.Center) {
                Text("+", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
        }
        LazyRow(contentPadding = PaddingValues(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(vertical = 6.dp)) {
            items(marketCategories) { name ->
                val selected = name == cat
                Text(
                    name, color = if (selected) QiblyTheme.background else secondary, fontSize = 13.sp,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(if (selected) QiblyTheme.text else QiblyTheme.soft)
                        .clickable { cat = name }.padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }
        }
        if (cat == "Achados") {
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
            if (cat == "Mural") {
                QinblyNeighborhoodBoard(lat, lng, place, textColor, secondary, onRefreshLocation = onRefreshLocation, onNotice = onNotice)
            } else {
                val list = listings
                when {
                    list == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Carregando…", color = secondary) }
                    failed -> Column(Modifier.fillMaxSize().padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Text("Não foi possível carregar os anúncios", color = textColor, fontWeight = FontWeight.Bold, fontSize = 16.sp, textAlign = TextAlign.Center)
                        Text("Confira a internet e tente de novo.", color = secondary, fontSize = 14.sp, modifier = Modifier.padding(top = 6.dp))
                        Text("Tentar de novo", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp,
                            modifier = Modifier.padding(top = 14.dp).clip(RoundedCornerShape(20.dp)).background(MarketBlue).clickable { refresh++ }.padding(horizontal = 20.dp, vertical = 10.dp))
                    }
                    list.isEmpty() -> Column(Modifier.fillMaxSize().padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Text("Nada por perto ainda", color = textColor, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Text("Seja o primeiro: toque no + para publicar.", color = secondary, fontSize = 14.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 6.dp))
                    }
                    else -> LazyVerticalGrid(
                        columns = GridCells.Fixed(2), contentPadding = PaddingValues(start = 10.dp, end = 10.dp, top = 6.dp, bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()
                    ) {
                        gridItems(list, key = { it.id }) { l -> ListingTile(l, lat, lng, cat == "Tudo", textColor, secondary) { open = l } }
                    }
                }
            }
        }
    }

    open?.let { l ->
        ListingDetail(
            l, lat, lng, mine = l.userId == myId, textColor = textColor, secondary = secondary,
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
        ListingForm(k, gLat, gLng, onDismiss = { createKind = null }, onPosted = { posted ->
            createKind = null
            cat = when (posted) { "venda" -> "Vendas"; "doacao" -> "Doações"; "achado", "perdido" -> "Achados"; "evento" -> "Eventos"; else -> "Serviços" }
            if (posted == "achado" || posted == "perdido") sub = posted
            refresh++
        }, onNotice = onNotice)
    }
}

@Composable
private fun ListingTile(l: QinblyListing, lat: Double, lng: Double, showTag: Boolean, textColor: Color, secondary: Color, onClick: () -> Unit) {
    Column(Modifier.fillMaxWidth().clickable { onClick() }) {
        Box(Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(12.dp)).background(QiblyTheme.soft)) {
            if (l.photos.isNotEmpty()) AsyncImage(model = photoUrl(l.photos.first()), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            else Box(Modifier.fillMaxSize().background(kindColor(l.kind).copy(alpha = 0.55f)), contentAlignment = Alignment.Center) {
                Text(kindLabel(l.kind), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            if (showTag) Text(kindLabel(l.kind), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(6.dp).clip(RoundedCornerShape(9.dp)).background(Color(0x99000000)).padding(horizontal = 7.dp, vertical = 3.dp))
        }
        Text(priceText(l), color = textColor, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 6.dp))
        Text(l.title, color = textColor, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(distanceText(lat, lng, l.lat, l.lng), color = secondary, fontSize = 11.sp, maxLines = 1)
    }
}

@Composable
private fun ListingDetail(
    l: QinblyListing, lat: Double, lng: Double, mine: Boolean, textColor: Color, secondary: Color,
    onDismiss: () -> Unit, onChat: () -> Unit, onDelete: () -> Unit
) {
    QinblyDialog(onDismissRequest = onDismiss, properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false), lift = true) {
        Box(Modifier.fillMaxSize().background(QiblyTheme.background)) {
            Column(Modifier.fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState())) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(40.dp).clip(CircleShape).background(QiblyTheme.soft).clickable { onDismiss() }, contentAlignment = Alignment.Center) { Text("←", color = textColor, fontSize = 18.sp) }
                    Text(kindLabel(l.kind), color = textColor, fontWeight = FontWeight.SemiBold, fontSize = 17.sp, modifier = Modifier.padding(start = 14.dp))
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
                    Text(kindLabel(l.kind) + " • " + distanceText(lat, lng, l.lat, l.lng) + " • " + qinblyAgo(l.createdAt), color = secondary, fontSize = 13.sp)
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
private fun ListingForm(kind: String, lat: Double, lng: Double, onDismiss: () -> Unit, onPosted: (String) -> Unit, onNotice: (String) -> Unit) {
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
        if (needsPhoto && photos.isEmpty()) { onNotice("Adicione pelo menos uma foto."); return }
        val price = priceTxt.trim().replace(",", ".").toDoubleOrNull()
        if (kind == "venda" && price == null) { onNotice("Informe o preço."); return }
        posting = true
        val picked = photos.toList()
        Thread {
            try {
                val urls = picked.mapIndexed { i, u -> uploadMarketPhoto(context, token, uid, u, i) }
                val body = JSONObject().put("kind", subKind).put("title", t.take(120)).put("description", desc.trim().take(2000))
                    .put("extra", extra.trim().take(80)).put("photos", JSONArray(urls)).put("lat", lat).put("lng", lng)
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
                Text("Local aproximado (~1 km): não mostramos o ponto exato. O anúncio some em 7 dias.", color = QiblyTheme.secondary, fontSize = 12.sp)
                Text(if (posting) "Publicando…" else "Publicar", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp, textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)).background(if (posting) Color(0xFF3C3C3E) else MarketBlue).clickable(enabled = !posting) { publish() }.padding(vertical = 15.dp))
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}
