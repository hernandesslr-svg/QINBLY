package app.qinbly.android

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.videoFrameMillis

// ───────── Bio: a bio guarda também o país ("🌍 ") e os links ("🔗 ", separados por " | ") em linhas próprias ─────────

internal data class QinblyBioParts(val description: String, val country: String, val links: List<String>)

/** Separa a bio salva em texto, país e links. A antiga linha de categorias ("✦ ") é ignorada. */
internal fun qinblyBioParts(bio: String): QinblyBioParts {
    var country = ""
    var links: List<String> = emptyList()
    val desc = ArrayList<String>()
    bio.lineSequence().forEach { l ->
        when {
            l.startsWith("✦ ") -> Unit
            l.startsWith("🌍 ") -> country = l.removePrefix("🌍 ").trim()
            l.startsWith("🔗 ") -> links = l.removePrefix("🔗 ").split(" | ").map { it.trim() }.filter { it.isNotBlank() }.take(3)
            else -> desc.add(l)
        }
    }
    return QinblyBioParts(desc.joinToString("\n").trim(), country, links)
}

internal fun qinblyComposeBio(description: String, country: String, links: List<String>): String =
    (if (country.isNotBlank()) "🌍 $country\n" else "") +
        (if (links.isNotEmpty()) "🔗 " + links.joinToString(" | ") + "\n" else "") +
        description.trim()

/** Tira espaços, quebras de linha e o separador "|" de um link digitado. */
internal fun qinblyCleanLink(raw: String): String = raw.filter { !it.isWhitespace() && it != '|' }.take(80)

internal fun qinblyLinkLabel(raw: String): String = raw.removePrefix("https://").removePrefix("http://").removePrefix("www.").trimEnd('/')

internal fun qinblyLinkUrl(raw: String): String = if (raw.startsWith("http://") || raw.startsWith("https://")) raw else "https://$raw"

private fun qinblyStatText(n: Int): String = when {
    n >= 1_000_000 -> String.format(java.util.Locale("pt", "BR"), "%.1f mi", n / 1_000_000f).replace(",0", "")
    n >= 10_000 -> "${n / 1000} mil"
    n >= 1_000 -> String.format(java.util.Locale("pt", "BR"), "%.1f mil", n / 1000f).replace(",0", "")
    else -> n.toString()
}

// ───────── Topo do perfil: foto à esquerda; ao lado nome, @ · país e os números ─────────

@Composable
internal fun QinblyProfileTop(
    avatar: @Composable () -> Unit,
    name: String,
    verified: Boolean,
    handle: String,
    onHandle: (() -> Unit)?,
    arrow: Boolean,
    country: String,
    onAbout: (() -> Unit)?,
    likes: Int,
    following: Int,
    followers: Int,
    onFollowing: () -> Unit,
    onFollowers: () -> Unit
) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        avatar()
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(name, color = RemoteAppearance.screenTextColor, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                if (verified) QinblyVerifiedBadge(19.dp)
            }
            Row(Modifier.padding(top = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                Row(if (onHandle != null) Modifier.weight(1f, fill = false).clickable(onClick = onHandle) else Modifier.weight(1f, fill = false), verticalAlignment = Alignment.CenterVertically) {
                    Text(handle, color = RemoteAppearance.screenSecondaryTextColor, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                    if (arrow) Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Trocar de conta", tint = RemoteAppearance.screenTextColor, modifier = Modifier.size(18.dp))
                }
                if (country.isNotBlank()) {
                    Text(" · ", color = RemoteAppearance.screenSecondaryTextColor, fontSize = 14.sp)
                    Icon(Icons.Default.LocationOn, contentDescription = "País", tint = RemoteAppearance.screenSecondaryTextColor, modifier = Modifier.size(14.dp))
                    Text(country, color = RemoteAppearance.screenSecondaryTextColor, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(start = 2.dp).weight(1f, fill = false))
                }
                if (onAbout != null) QinblyAccentIcon(Icons.Outlined.Info, contentDescription = "Sobre este perfil",
                    modifier = Modifier.padding(start = 6.dp).size(18.dp).clickable(onClick = onAbout))
            }
            Row(Modifier.padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(22.dp)) {
                QinblyProfileStat("Seguindo", following, onFollowing)
                QinblyProfileStat("Seguidores", followers, onFollowers)
                QinblyProfileStat("Curtidas", likes, null)
            }
        }
    }
}

@Composable
private fun QinblyProfileStat(label: String, n: Int, onClick: (() -> Unit)?) {
    Column(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier) {
        Text(qinblyStatText(n), color = RemoteAppearance.screenTextColor, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
        Text(label, color = RemoteAppearance.screenSecondaryTextColor, fontSize = 12.sp)
    }
}

/** Bio inteira, alinhada à esquerda (desce de linha, sem "mais"). */
@Composable
internal fun QinblyProfileBioLeft(text: String, placeholder: String = "") {
    val clean = text.trim()
    if (clean.isBlank() && placeholder.isBlank()) return
    if (clean.isBlank()) Text(placeholder, color = RemoteAppearance.screenSecondaryTextColor, fontSize = 14.sp, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp))
    else QinblyRichText(
        text = clean,
        color = RemoteAppearance.screenTextColor,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        fontSize = 14.sp,
        textAlign = androidx.compose.ui.text.style.TextAlign.Start,
        linkColor = QinblyBrand.Accent
    )
}

/** Até 3 links numa linha só; a letra diminui sozinha para caber. */
@Composable
internal fun QinblyProfileLinks(links: List<String>) {
    if (links.isEmpty()) return
    val context = LocalContext.current
    val widthDp = LocalConfiguration.current.screenWidthDp - 32
    val labels = links.map(::qinblyLinkLabel)
    val chars = labels.sumOf { it.length }.coerceAtLeast(1)
    val fixed = links.size * 22 + (links.size - 1) * 12
    val sp = ((widthDp - fixed) / (chars * 0.53f)).coerceIn(9f, 14f)
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        links.forEachIndexed { i, raw ->
            Row(Modifier.weight(1f, fill = false).clickable {
                try { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(qinblyLinkUrl(raw)))) } catch (_: Exception) { }
            }, verticalAlignment = Alignment.CenterVertically) {
                QinblyAccentIcon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(16.dp))
                QinblyAccentText(labels[i], fontSize = sp.sp, maxLines = 1, softWrap = false, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(start = 4.dp, end = 2.dp))
            }
        }
    }
}

/** Botão cinza claro do perfil. */
@Composable
internal fun QinblyGrayButton(label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val bg = if (QiblyTheme.dark) Color(0x1AFFFFFF) else Color(0xFFF1F2F5)
    val line = if (QiblyTheme.dark) Color.White else Color(0xFF111111)
    val shape = RoundedCornerShape(10.dp)
    Box(modifier.height(34.dp).clip(shape).background(bg).border(1.dp, line, shape).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        QinblyAccentText(label, fontSize = 13.sp, fontWeight = FontWeight.Medium, maxLines = 1)
    }
}

/** Folha "Sobre este perfil": data de entrada e país. */
@Composable
internal fun QinblyProfileAbout(joined: String, country: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sobre este perfil") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (joined.isNotBlank()) Text("Entrou em $joined")
                if (country.isNotBlank()) Text("País: $country")
                if (joined.isBlank() && country.isBlank()) Text("Ainda não há informações.")
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Fechar", color = QinblyBrand.Accent) } },
        containerColor = QiblyTheme.card, titleContentColor = QiblyTheme.text, textContentColor = QiblyTheme.text
    )
}

// ───────── Favoritos ─────────

private fun qinblyUploadFavorite(context: Context, prefs: android.content.SharedPreferences, title: String, uris: List<Uri>, onNotice: (String) -> Unit, done: () -> Unit) {
    val token = prefs.getString("token", null)
    val main = android.os.Handler(android.os.Looper.getMainLooper())
    if (token.isNullOrBlank()) { onNotice("Entre na sua conta."); return }
    onNotice("Enviando fotos…")
    Thread {
        try {
            val ids = uris.map { QinblyApi.uploadStoryMedia(context, token, it).first }
            QinblyApi.createFavorite(token, title, ids)
            main.post { onNotice("Favorito salvo."); done() }
        } catch (e: Exception) {
            main.post { onNotice(e.message ?: "Não foi possível salvar agora.") }
        }
    }.start()
}

/** Fila de favoritos do perfil: "Novo" primeiro (só no seu perfil) e uma bolinha por favorito, com a última foto adicionada como capa. */
@Composable
internal fun QinblyHighlightsRow(username: String, prefs: android.content.SharedPreferences, mine: Boolean, onNotice: (String) -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var list by remember(username) { mutableStateOf<List<QinblyApi.Highlight>>(emptyList()) }
    var reload by remember(username) { mutableIntStateOf(0) }
    var openTitle by remember(username) { mutableStateOf("") }
    var creating by remember { mutableStateOf(false) }
    LaunchedEffect(username, reload) {
        if (username.isBlank()) return@LaunchedEffect
        try { list = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) { QinblyApi.getHighlights(username, prefs.getString("token", null)) } } catch (_: Exception) { }
    }
    val groups = remember(list) { list.groupBy { it.title }.toList() }
    if (groups.isEmpty() && !mine) return
    val gray = if (QiblyTheme.dark) Color(0x1AFFFFFF) else Color(0xFFF1F2F5)
    LazyRow(modifier.fillMaxWidth(), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        if (mine) item(key = "novo") {
            Column(Modifier.width(64.dp).clickable { creating = true }, horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.size(60.dp).clip(CircleShape).background(gray), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Add, contentDescription = "Novo favorito", tint = RemoteAppearance.screenTextColor, modifier = Modifier.size(26.dp))
                }
                Text("Novo", color = RemoteAppearance.screenTextColor, fontSize = 12.sp, maxLines = 1, modifier = Modifier.padding(top = 4.dp))
            }
        }
        items(groups.size, key = { "g:" + groups[it].first }) { i ->
            val (title, photos) = groups[i]
            val cover = photos.first()
            Column(Modifier.width(64.dp).clickable { openTitle = title }, horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.size(60.dp).clip(CircleShape).background(gray), contentAlignment = Alignment.Center) {
                    if (cover.mediaType == "text" || cover.mediaUrl.isBlank()) {
                        Text("Aa", color = RemoteAppearance.screenTextColor, fontWeight = FontWeight.SemiBold)
                    } else {
                        val request = remember(cover.mediaUrl) {
                            coil.request.ImageRequest.Builder(context).data(cover.mediaUrl).apply {
                                if (cover.mediaType == "video") { decoderFactory(coil.decode.VideoFrameDecoder.Factory()); videoFrameMillis(500) }
                            }.crossfade(true).build()
                        }
                        AsyncImage(model = request, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    }
                }
                Text(title, color = RemoteAppearance.screenTextColor, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
    if (creating) QinblyFavoriteEditor(existingTitle = null, onClose = { creating = false }) { title, uris ->
        creating = false
        qinblyUploadFavorite(context, prefs, title, uris, onNotice) { reload++ }
    }
    val open = groups.firstOrNull { it.first == openTitle }
    if (open != null) QinblyFavoriteViewer(open.first, open.second, mine, prefs, onNotice, onClose = { openTitle = "" }, onChanged = { reload++ })
}

/** Tela "Novo favorito" (ou "Adicionar a <nome>"): nome com lápis, capa redonda = última foto escolhida e a galeria com a câmera no primeiro quadrado. */
@Composable
internal fun QinblyFavoriteEditor(existingTitle: String?, onClose: () -> Unit, onDone: (String, List<Uri>) -> Unit) {
    val context = LocalContext.current
    var title by remember { mutableStateOf(existingTitle.orEmpty()) }
    var granted by remember { mutableStateOf(hasGalleryPermission(context)) }
    var items by remember { mutableStateOf<List<GalleryItem>>(emptyList()) }
    val selected = remember { mutableStateListOf<Uri>() }
    var showCamera by remember { mutableStateOf(false) }
    val perms = if (android.os.Build.VERSION.SDK_INT >= 33) arrayOf(android.Manifest.permission.READ_MEDIA_IMAGES)
        else arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { granted = hasGalleryPermission(context) }
    LaunchedEffect(Unit) { if (!granted) permissionLauncher.launch(perms) }
    LaunchedEffect(granted) {
        if (granted) items = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) { loadRecentMedia(context).filter { !it.video } }
    }
    if (showCamera) QinblyCameraCapture(onResult = { u -> showCamera = false; if (u !in selected && selected.size < 30) selected.add(u) }, onDismiss = { showCamera = false })
    val tile = if (QiblyTheme.dark) Color(0xFF262628) else Color(0xFFE9EAEE)
    QinblyDialog(onDismissRequest = onClose, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false), lift = false) {
        Column(Modifier.fillMaxSize().background(QiblyTheme.background).statusBarsPadding().padding(bottom = qinblyNavBarPadding())) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(40.dp).clickable { onClose() }, contentAlignment = Alignment.Center) { QinblyCloseIcon(QiblyTheme.text, 22.dp) }
                Text(if (existingTitle == null) "Novo favorito" else "Adicionar a $existingTitle", color = QiblyTheme.text, fontWeight = FontWeight.SemiBold, fontSize = 17.sp,
                    modifier = Modifier.weight(1f).padding(horizontal = 12.dp), maxLines = 1, overflow = TextOverflow.Ellipsis)
                val ready = title.isNotBlank() && selected.isNotEmpty()
                Text("Adicionar", color = if (ready) QinblyBrand.Accent else RemoteAppearance.screenSecondaryTextColor, fontWeight = FontWeight.SemiBold, fontSize = 16.sp,
                    modifier = Modifier.clickable(enabled = ready) { onDone(title.trim(), selected.toList()) }.padding(8.dp))
            }
            if (existingTitle == null) Column(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.size(96.dp).clip(CircleShape).background(tile), contentAlignment = Alignment.Center) {
                    val last = selected.lastOrNull()
                    if (last != null) MediaThumb(last, false, Modifier.fillMaxSize(), 400)
                    else QinblyIconOf(QinblyIcon.PhotoCamera, RemoteAppearance.screenSecondaryTextColor, 30.dp)
                }
                Row(Modifier.padding(top = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    BasicTextField(
                        value = title,
                        onValueChange = { title = it.replace("\n", "").take(12) },
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(color = QiblyTheme.text, fontSize = 18.sp, fontWeight = FontWeight.Medium, textAlign = androidx.compose.ui.text.style.TextAlign.Center),
                        cursorBrush = androidx.compose.ui.graphics.SolidColor(QinblyBrand.Accent),
                        modifier = Modifier.widthIn(min = 100.dp, max = 200.dp),
                        decorationBox = { inner ->
                            Box(contentAlignment = Alignment.Center) {
                                if (title.isEmpty()) Text("Nome do favorito", color = RemoteAppearance.screenSecondaryTextColor, fontSize = 18.sp)
                                inner()
                            }
                        }
                    )
                    Icon(Icons.Default.Edit, contentDescription = "Escrever o nome", tint = RemoteAppearance.screenSecondaryTextColor, modifier = Modifier.padding(start = 8.dp).size(17.dp))
                }
            }
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Galeria", color = QiblyTheme.text, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, modifier = Modifier.weight(1f))
                if (selected.isNotEmpty()) Text(if (selected.size == 1) "1 selecionada" else "${selected.size} selecionadas", color = RemoteAppearance.screenSecondaryTextColor, fontSize = 13.sp)
            }
            LazyVerticalGrid(GridCells.Fixed(3), modifier = Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                item {
                    Box(Modifier.aspectRatio(1f).background(tile).clickable { showCamera = true }, contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            QinblyIconOf(QinblyIcon.PhotoCamera, QiblyTheme.text, 30.dp)
                            Text("Câmera", color = QiblyTheme.text, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                }
                items(selected.filter { u -> items.none { it.uri == u } }, key = { "cam:" + it.toString() }) { u ->
                    Box(Modifier.aspectRatio(1f).clickable { selected.remove(u) }) {
                        MediaThumb(u, false, Modifier.fillMaxSize())
                        Box(Modifier.align(Alignment.TopEnd).padding(6.dp).size(22.dp).clip(CircleShape).background(qinblyAccentBrush()), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                        }
                    }
                }
                items(items, key = { it.uri.toString() }) { g ->
                    val on = g.uri in selected
                    Box(Modifier.aspectRatio(1f).clickable {
                        if (on) selected.remove(g.uri) else if (selected.size < 30) selected.add(g.uri)
                    }) {
                        MediaThumb(g.uri, false, Modifier.fillMaxSize())
                        Box(Modifier.align(Alignment.TopEnd).padding(6.dp).size(22.dp).clip(CircleShape).background(if (on) QinblyBrand.Accent else Color(0x66000000)), contentAlignment = Alignment.Center) {
                            if (on) Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                        }
                    }
                }
                if (!granted) item(span = { GridItemSpan(maxLineSpan) }) {
                    Column(Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Permita o acesso às suas fotos para escolher daqui.", color = QiblyTheme.text, fontSize = 16.sp)
                        Text("Permitir acesso", color = Color.White, fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(qinblyAccentBrush()).clickable { permissionLauncher.launch(perms) }.padding(horizontal = 20.dp, vertical = 10.dp))
                    }
                }
            }
        }
    }
}

/** Abre um favorito: fotos em sequência (toque à direita avança, à esquerda volta); no seu perfil tem o menu de 3 pontinhas. */
@Composable
private fun QinblyFavoriteViewer(
    title: String, newestFirst: List<QinblyApi.Highlight>, mine: Boolean, prefs: android.content.SharedPreferences,
    onNotice: (String) -> Unit, onClose: () -> Unit, onChanged: () -> Unit
) {
    val context = LocalContext.current
    val photos = remember(newestFirst) { newestFirst.reversed() }
    var index by remember { mutableIntStateOf(0) }
    var menuOpen by remember { mutableStateOf(false) }
    var ask by remember { mutableStateOf("") } // "", "foto", "tudo", "nome"
    var adding by remember { mutableStateOf(false) }
    var newName by remember(title) { mutableStateOf(title) }
    val current = photos.getOrNull(index.coerceIn(0, (photos.size - 1).coerceAtLeast(0))) ?: return
    val none = remember { MutableInteractionSource() }
    val main = android.os.Handler(android.os.Looper.getMainLooper())
    fun act(work: (String) -> Unit, okMessage: String, closeAfter: Boolean) {
        val token = prefs.getString("token", null)
        if (token.isNullOrBlank()) { onNotice("Entre na sua conta."); return }
        Thread {
            try {
                work(token)
                main.post { onNotice(okMessage); onChanged(); if (closeAfter) onClose() }
            } catch (e: Exception) {
                main.post { onNotice(e.message ?: "Não foi possível agora.") }
            }
        }.start()
    }
    Dialog(onDismissRequest = onClose, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        Box(Modifier.fillMaxSize().background(Color.Black)) {
            when {
                current.mediaType == "video" && current.mediaUrl.isNotBlank() -> StoryVideo(current.mediaUrl, paused = ask.isNotBlank() || menuOpen, onEnded = { })
                else -> AsyncImage(model = current.mediaUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
            }
            Row(Modifier.fillMaxSize()) {
                Box(Modifier.weight(1f).fillMaxSize().clickable(interactionSource = none, indication = null) { if (menuOpen) menuOpen = false else if (index > 0) index-- })
                Box(Modifier.weight(1f).fillMaxSize().clickable(interactionSource = none, indication = null) { if (menuOpen) menuOpen = false else if (index < photos.size - 1) index++ else onClose() })
            }
            Column(Modifier.fillMaxWidth().statusBarsPadding()) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    photos.forEachIndexed { i, _ ->
                        Box(Modifier.weight(1f).height(3.dp).clip(RoundedCornerShape(2.dp)).background(if (i <= index) Color.White else Color(0x59FFFFFF)))
                    }
                }
                Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(title, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, modifier = Modifier.weight(1f).padding(start = 8.dp), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    if (mine) Box(Modifier.size(44.dp).clickable { menuOpen = !menuOpen }, contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Opções do favorito", tint = Color.White)
                    }
                    Box(Modifier.size(44.dp).clickable { onClose() }, contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar", tint = Color.White)
                    }
                }
                if (menuOpen) Box(Modifier.fillMaxWidth().padding(end = 12.dp), contentAlignment = Alignment.TopEnd) {
                    Column(Modifier.width(230.dp).clip(RoundedCornerShape(14.dp)).background(Color.White).padding(vertical = 6.dp)) {
                        @Composable
                        fun row(label: String, red: Boolean = false, onClick: () -> Unit) {
                            Text(label, color = if (red) Color(0xFFD32F2F) else Color(0xFF111111), fontSize = 15.sp,
                                modifier = Modifier.fillMaxWidth().clickable { menuOpen = false; onClick() }.padding(horizontal = 16.dp, vertical = 13.dp))
                        }
                        row("Adicionar fotos") { adding = true }
                        row("Mudar nome") { newName = title; ask = "nome" }
                        row("Remover esta foto") { ask = "foto" }
                        row("Excluir favorito", red = true) { ask = "tudo" }
                    }
                }
            }
        }
    }
    if (adding) QinblyFavoriteEditor(existingTitle = title, onClose = { adding = false }) { _, uris ->
        adding = false
        qinblyUploadFavorite(context, prefs, title, uris, onNotice) { onChanged(); onClose() }
    }
    if (ask == "foto" || ask == "tudo") AlertDialog(
        onDismissRequest = { ask = "" },
        title = { Text(if (ask == "foto") "Remover esta foto?" else "Excluir o favorito?") },
        text = { Text(if (ask == "foto") "A foto deixa de aparecer neste favorito. Não dá para desfazer." else "O favorito \"$title\" e todas as fotos dele saem do seu perfil. Não dá para desfazer.") },
        confirmButton = {
            TextButton(onClick = {
                val wasPhoto = ask == "foto"
                ask = ""
                if (wasPhoto) act({ QinblyApi.deleteHighlight(it, current.id) }, "Foto removida.", photos.size <= 1)
                else act({ QinblyApi.deleteFavorite(it, title) }, "Favorito excluído.", true)
            }) { Text(if (ask == "foto") "Remover" else "Excluir", color = Color(0xFFD33B3B)) }
        },
        dismissButton = { TextButton(onClick = { ask = "" }) { Text("Cancelar") } },
        containerColor = QiblyTheme.card, titleContentColor = QiblyTheme.text, textContentColor = QiblyTheme.text
    )
    if (ask == "nome") AlertDialog(
        onDismissRequest = { ask = "" },
        title = { Text("Mudar nome") },
        text = { OutlinedTextField(newName, { newName = it.replace("\n", "").take(12) }, singleLine = true, label = { Text("Nome do favorito") }) },
        confirmButton = {
            TextButton(onClick = {
                val to = newName.trim()
                ask = ""
                if (to.isNotBlank() && to != title) act({ QinblyApi.renameFavorite(it, title, to) }, "Nome alterado.", true)
            }) { Text("Salvar", color = QinblyBrand.Accent) }
        },
        dismissButton = { TextButton(onClick = { ask = "" }) { Text("Cancelar") } },
        containerColor = QiblyTheme.card, titleContentColor = QiblyTheme.text, textContentColor = QiblyTheme.text
    )
}
