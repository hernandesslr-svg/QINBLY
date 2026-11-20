package app.qinbly.android

import org.json.JSONObject
import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.AddBox
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.EmojiEmotions
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.outlined.StickyNote2
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import coil.request.videoFrameMillis
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Menu "Criar" da faixa de conversas: publicar story (galeria ou câmera), escrever/apagar a nota e ver o próprio story.
 * [onChanged] avisa que algo foi publicado ou apagado, para a faixa recarregar.
 */
@Composable
internal fun QinblyStoryMenu(
    accent: Color, hasStory: Boolean, hasNote: Boolean, currentNote: String = "",
    onDismiss: () -> Unit, onNotice: (String) -> Unit, onChanged: () -> Unit, onViewMine: () -> Unit, onAddStory: () -> Unit = {}
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("qinbly_session", Context.MODE_PRIVATE).byAccount() }
    var step by remember { mutableStateOf("menu") } // menu, text, collab, capsule, note
    var noteText by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }

    when (step) {
        "menu" -> QinblyDialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false), lift = false) {
            val card = Color(0xFF232325)
            // Cartão quadrado de atalho (rola para o lado): ícone em cima e nome embaixo.
            // Quatro cartões ocupam a largura da tela (16 dp de margem e 8 dp entre eles).
            val cardWidth = ((androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp - 32 - 24) / 4f).dp
            @Composable fun Square(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, onClick: () -> Unit) {
                Column(
                    Modifier.width(cardWidth).height(cardWidth + 12.dp).clip(RoundedCornerShape(16.dp)).background(card).clickable { onClick() }.padding(horizontal = 4.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
                ) {
                    Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(30.dp))
                    Spacer(Modifier.height(8.dp))
                    Text(title, color = Color.White, fontSize = 11.sp, lineHeight = 13.sp, fontWeight = FontWeight.Medium, maxLines = 2,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                }
            }
            var galleryGranted by remember { mutableStateOf(hasGalleryPermission(context)) }
            var galleryItems by remember { mutableStateOf<List<GalleryItem>>(emptyList()) }
            val galleryPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { galleryGranted = hasGalleryPermission(context) }
            val systemPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                if (uri != null) { qinblySetPickedMedia(uri, (context.contentResolver.getType(uri) ?: "").startsWith("video/")); onDismiss(); onAddStory() }
            }
            LaunchedEffect(galleryGranted) {
                if (galleryGranted) galleryItems = withContext(Dispatchers.IO) { loadRecentMedia(context) }
            }
            Column(Modifier.fillMaxSize().background(Color(0xFF171719)).statusBarsPadding()) {
                Box(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 6.dp).padding(vertical = 12.dp).heightIn(min = 40.dp), contentAlignment = Alignment.Center) {
                    Text("Novo story", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                    Box(Modifier.align(Alignment.CenterEnd).size(40.dp).clip(CircleShape).background(Color(0x22FFFFFF)).clickable { onDismiss() }, contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar", tint = Color.White, modifier = Modifier.size(22.dp))
                    }
                }
                androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp)) {
                    item { Square(Icons.Outlined.TextFields, "Texto") { step = "text" } }
                    item { Square(Icons.Outlined.MusicNote, "Música") { step = "music" } }
                    item { Square(Icons.Outlined.Groups, "Story coletivo") { step = "collab" } }
                    item { Square(Icons.Outlined.HourglassEmpty, "Cápsula do tempo") { step = "capsule" } }
                }
                Text("Galeria", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium, textAlign = androidx.compose.ui.text.style.TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 18.dp, bottom = 8.dp))
                androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                    androidx.compose.foundation.lazy.grid.GridCells.Fixed(3), modifier = Modifier.weight(1f).fillMaxWidth().navigationBarsPadding(),
                    horizontalArrangement = Arrangement.spacedBy(2.dp), verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    // Primeiro quadradinho: a câmera do app.
                    item(key = "camera-tile") {
                        Column(
                            Modifier.aspectRatio(0.75f).background(Color(0xFF2A2A30)).clickable { onDismiss(); onAddStory() },
                            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Outlined.PhotoCamera, contentDescription = null, tint = Color.White, modifier = Modifier.size(30.dp))
                            Spacer(Modifier.height(6.dp))
                            Text("Câmera", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                    if (!galleryGranted) item(key = "gallery-permission", span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                        Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Permita o acesso às suas fotos para escolher daqui.", color = Color(0xFFF2F4F8), fontSize = 12.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                            Text("Permitir acesso", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(Color(0xFF2F68E0))
                                .clickable { galleryPermission.launch(if (android.os.Build.VERSION.SDK_INT >= 33) arrayOf(android.Manifest.permission.READ_MEDIA_IMAGES, android.Manifest.permission.READ_MEDIA_VIDEO) else arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)) }
                                .padding(horizontal = 18.dp, vertical = 9.dp))
                            Text("Escolher pela galeria do celular", color = Color(0xFF7FA6F2), fontSize = 11.sp, modifier = Modifier.clickable { systemPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)) }.padding(6.dp))
                        }
                    }
                    gridItems(galleryItems, key = { it.uri.toString() }) { g ->
                        Box(Modifier.aspectRatio(0.75f).clickable { qinblySetPickedMedia(g.uri, g.video); onDismiss(); onAddStory() }) {
                            MediaThumb(g.uri, g.video, Modifier.fillMaxSize(), size = 400)
                            if (g.video) QinblyIconOf(QinblyIcon.PlayArrow, Color.White, 20.dp, Modifier.align(Alignment.BottomStart).padding(6.dp))
                        }
                    }
                }
            }
        }
        "collab" -> QinblyContributeFlow(
            storyId = "", newCollab = true, onNotice = onNotice,
            onCamera = { qinblyStartCollabStory(); onDismiss(); onAddStory() },
            onDone = { onChanged(); onDismiss() }, onDismiss = { step = "menu" }
        )
        "music" -> QinblyMusicPicker(
            onPick = { m ->
                val token = prefs.getString("token", null)
                val uid = QinblyApi.currentUserId(prefs)
                step = "menu"
                if (token.isNullOrBlank() || uid.isNullOrBlank()) onNotice("Entre na sua conta.")
                else Thread {
                    val main = Handler(Looper.getMainLooper())
                    try {
                        val bg = (m.title.hashCode() and 0x7fffffff) % qinblyTextBgs.size
                        val text = "♪ " + m.title + if (m.artist.isNotBlank()) "\n" + m.artist else ""
                        QinblyApi.createTextStory(token, uid, qinblyAttachMusic(qinblyEncodeTextStory(QinblyTextStory(bg, 3, 1, text)), m))
                        main.post { onNotice("Música compartilhada no seu story por 24 horas."); onChanged(); onDismiss() }
                    } catch (e: Exception) { main.post { onNotice(e.message ?: "Não foi possível compartilhar agora.") } }
                }.start()
            },
            onDismiss = { step = "menu" }
        )
        "capsule" -> QinblyCapsuleHub(accent, { step = "menu" }, onNotice)
        "packs" -> QinblyStickerPacksDialog(accent, { step = "menu" }, onNotice)
        "text" -> QinblyTextStoryComposer(onDismiss = { step = "menu" }, onPublished = { onChanged(); onDismiss() }, onNotice = onNotice)
        else -> AlertDialog(
            onDismissRequest = { step = "menu" },
            title = { Text("Sua nota") },
            text = { QinblyClearFocusWhenKeyboardCloses();
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Um texto curto que aparece sobre a sua foto, para quem te segue. Some em 24 horas.", fontSize = 14.sp)
                    OutlinedTextField(noteText, { noteText = it.take(60) }, label = { Text("Nota (até 60 letras)") }, singleLine = true)
                }
            },
            confirmButton = {
                TextButton(enabled = noteText.isNotBlank() && !busy, onClick = {
                    val token = prefs.getString("token", null)
                    if (token.isNullOrBlank()) { onNotice("Entre na sua conta."); return@TextButton }
                    busy = true
                    Thread {
                        val main = Handler(Looper.getMainLooper())
                        try { QinblyApi.saveNote(token, noteText); main.post { busy = false; onNotice("Nota compartilhada."); onChanged(); onDismiss() } }
                        catch (e: Exception) { main.post { busy = false; onNotice(e.message ?: "Não foi possível salvar a nota.") } }
                    }.start()
                }) { Text("Compartilhar", color = accent, fontWeight = FontWeight.SemiBold) }
            },
            dismissButton = { TextButton(onClick = { step = "menu" }) { Text("Voltar") } }
        )
    }
}

@Composable
private fun MenuRow(text: String, accent: Color, onClick: () -> Unit) {
    Text(text, color = accent, fontSize = 15.sp, fontWeight = FontWeight.SemiBold,
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).clickable { onClick() }.padding(vertical = 12.dp, horizontal = 4.dp))
}

@Composable
internal fun StoryVideo(url: String, paused: Boolean = false, onProgress: suspend (Float) -> Unit = {}, onEnded: () -> Unit) {
    val context = LocalContext.current
    val progressCallback by androidx.compose.runtime.rememberUpdatedState(onProgress)
    val player = remember(url) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(url))
            repeatMode = Player.REPEAT_MODE_OFF
            playWhenReady = true
            prepare()
        }
    }
    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) { if (state == Player.STATE_ENDED) onEnded() }
        }
        player.addListener(listener)
        val unreg = QinblyPlayback.add({ val w = player.playWhenReady; player.pause(); w }, { player.play() })
        onDispose { unreg(); player.removeListener(listener); player.release() }
    }
    // Menu/lista/janela aberta: o vídeo pausa junto com o story.
    LaunchedEffect(player, paused) { player.playWhenReady = !paused }
    // A barra de cima acompanha o vídeo de verdade (posição / duração).
    LaunchedEffect(player) {
        while (true) {
            val d = player.duration
            if (d > 0) progressCallback((player.currentPosition.toFloat() / d).coerceIn(0f, 1f))
            kotlinx.coroutines.delay(80)
        }
    }
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx -> PlayerView(ctx).apply { useController = false; resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT; this.player = player } },
        update = { it.player = player }
    )
}

@Composable
private fun QinblyStoryMenuRow(label: String, danger: Boolean = false, onClick: () -> Unit) {
    androidx.compose.material3.DropdownMenuItem(
        text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(label, color = if (danger) Color(0xFFD33B3B) else Color.Unspecified)
            }
        },
        onClick = onClick
    )
}

internal fun qinblyDownloadToCache(context: Context, url: String, ext: String): java.io.File? = try {
    val f = java.io.File(context.cacheDir, "story_dl_" + System.currentTimeMillis() + ext)
    java.net.URL(url).openStream().use { i -> f.outputStream().use { o -> i.copyTo(o) } }
    f
} catch (_: Exception) { null }

/**
 * Visualizador de stories em tela cheia: barras de progresso, toque na direita avança e na esquerda volta,
 * passa sozinho (6 s para foto; vídeo até o fim, no máximo 20 s). Se o story é seu: contagem de quem viu, lista e excluir.
 */
@Composable
internal fun QinblyStoryViewer(entry: QinblyApi.TrayEntry, accent: Color, onNotice: (String) -> Unit, onDismiss: () -> Unit, onCamera: (String) -> Unit = {}, startIndex: Int = 0,
                               onOpenProfile: (String, Int) -> Unit = { _, _ -> }, onOpenOriginal: (QinblyStoryOrigin, Int) -> Unit = { _, _ -> },
                               hasNextPerson: Boolean = false, hasPreviousPerson: Boolean = false, onNextPerson: () -> Unit = {}, onPreviousPerson: () -> Unit = {}, onAddStory: (() -> Unit)? = null) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("qinbly_session", Context.MODE_PRIVATE).byAccount() }
    val token = remember { prefs.getString("token", null) }
    val items = remember(entry.username) {
        mutableStateListOf<QinblyApi.StoryItem>().apply {
            entry.stories.forEach { st ->
                add(st)
                // Story coletivo: depois do tema e das respostas vem a página final com o botão Participar.
                if (QinblyApi.isCollabCaption(st.caption)) add(st.copy(id = st.id + "#invite", page = "invite", parentId = st.id))
            }
        }
    }
    var index by remember(entry.username) { mutableIntStateOf(startIndex.coerceIn(0, (items.size - 1).coerceAtLeast(0))) }
    var showViewers by remember { mutableStateOf(false) }
    var menuOpen by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    var highlightAsk by remember { mutableStateOf(false) }
    var audienceAsk by remember { mutableStateOf(false) }
    var highlightTitle by remember { mutableStateOf("Destaques") }
    var confirmBlock by remember { mutableStateOf(false) }
    var reportOpen by remember { mutableStateOf(false) }
    var muted by remember { mutableStateOf(false) }
    var showComments by remember { mutableStateOf(false) }
    var replying by remember { mutableStateOf(false) }
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    val imeOpen = WindowInsets.ime.getBottom(androidx.compose.ui.platform.LocalDensity.current) > 0
    var viewers by remember { mutableStateOf<List<QinblyApi.StoryViewer>?>(null) }
    var contribTarget by remember { mutableStateOf("") }
    var reloadContribs by remember { mutableIntStateOf(0) }
    fun applyContribs(storyId: String, list: List<QinblyApi.Contribution>) {
        val curId = items.getOrNull(index)?.id
        items.removeAll { it.page == "contrib" && it.parentId == storyId }
        val invIdx = items.indexOfFirst { it.page == "invite" && it.parentId == storyId }
        if (invIdx < 0) return
        items.addAll(invIdx, list.map { c ->
            QinblyApi.StoryItem(id = "c:" + c.id, mediaUrl = c.mediaUrl, mediaType = c.mediaType, caption = c.caption, createdAt = c.createdAt, seen = true, views = 0,
                page = "contrib", contribId = c.id, authorName = c.displayName.ifBlank { c.username }, authorUsername = c.username, authorAvatar = c.avatarUrl, authorMine = c.mine, parentId = storyId)
        })
        if (curId != null) { val ni = items.indexOfFirst { it.id == curId }; if (ni >= 0) index = ni }
    }
    LaunchedEffect(entry.username, reloadContribs) {
        if (token.isNullOrBlank()) return@LaunchedEffect
        for (st in entry.stories.filter { QinblyApi.isCollabCaption(it.caption) }) {
            val list = try { kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) { QinblyApi.getContributions(token, st.id) } } catch (_: Exception) { continue }
            applyContribs(st.id, list)
        }
    }
    if (items.isEmpty()) { LaunchedEffect(Unit) { onDismiss() }; return }
    val current = items[index.coerceIn(0, items.lastIndex)]
    val progress = remember(current.id) { Animatable(0f) }

    // Ignora toques logo após abrir: o toque que abriu o story não pode fechá-lo de novo.
    val openedAt = remember { System.currentTimeMillis() }
    fun tooSoon() = System.currentTimeMillis() - openedAt < 500
    fun closeSafe() { if (!tooSoon()) onDismiss() }
    // Fim dos stories de uma pessoa: segue para a próxima da fila (como no Instagram); só fecha quando acabam todas.
    fun next() { if (index < items.lastIndex) index++ else if (hasNextPerson) onNextPerson() else closeSafe() }
    fun previous() { if (index > 0) index-- else if (hasPreviousPerson) onPreviousPerson() }

    LaunchedEffect(current.id) {
        if (current.page == "story") QinblyStoryRings.markViewed(prefs, entry, current.id)
        if (!entry.mine && current.page == "story" && !token.isNullOrBlank()) {
            Thread {
                try {
                    QinblyApi.markStoryViewed(token, current.id)
                    // Guarda a cidade de quem viu (só a dona do story enxerga) para separar "Perto de você" de "De mais longe".
                    val sp = context.getSharedPreferences("qinbly_session", Context.MODE_PRIVATE).byAccount()
                    val uid = QinblyApi.currentUserId(sp)
                    val city = (sp.getString("market_city", null)?.takeIf { it.isNotBlank() } ?: sp.getString("device_place", null)).orEmpty()
                    if (!uid.isNullOrBlank() && city.isNotBlank())
                        QinblyApi.rest(token, "PATCH", "story_views?story_id=eq.${current.id}&viewer_id=eq.$uid", JSONObject().put("city", city).toString(), "return=minimal")
                } catch (_: Exception) { }
            }.start()
        }
    }
    // Avatares de quem viu (só a dona do story): os 3 mais recentes.
    LaunchedEffect(current.id, showViewers) {
        if (!entry.mine || current.page != "story" || token.isNullOrBlank()) { viewers = null; return@LaunchedEffect }
        viewers = try { kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) { QinblyApi.getStoryViewers(token, current.id) } } catch (_: Exception) { emptyList() }
    }
    // Curtidas do story: quem vê guarda a própria; a dona vê o total.
    var liked by remember(current.id) { mutableStateOf(false) }
    var likeCount by remember(current.id) { mutableIntStateOf(0) }
    LaunchedEffect(current.id) {
        if (token.isNullOrBlank() || current.page != "story") return@LaunchedEffect
        val sp = context.getSharedPreferences("qinbly_session", Context.MODE_PRIVATE).byAccount()
        val uid = QinblyApi.currentUserId(sp)
        try {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                if (entry.mine) likeCount = org.json.JSONArray(QinblyApi.rest(token, "GET", "story_likes?story_id=eq.${current.id}&select=user_id")).length()
                else if (!uid.isNullOrBlank()) liked = org.json.JSONArray(QinblyApi.rest(token, "GET", "story_likes?story_id=eq.${current.id}&user_id=eq.$uid&select=story_id")).length() > 0
            }
        } catch (_: Exception) { }
    }
    fun toggleLike() {
        val sp = context.getSharedPreferences("qinbly_session", Context.MODE_PRIVATE).byAccount()
        val uid = QinblyApi.currentUserId(sp)
        if (token.isNullOrBlank() || uid.isNullOrBlank()) return
        val id = current.id
        val now = !liked
        liked = now
        Thread {
            try {
                if (now) QinblyApi.rest(token, "POST", "story_likes", JSONObject().put("story_id", id).put("user_id", uid).toString(), "return=minimal,resolution=ignore-duplicates")
                else QinblyApi.rest(token, "DELETE", "story_likes?story_id=eq.$id&user_id=eq.$uid")
            } catch (_: Exception) { Handler(Looper.getMainLooper()).post { liked = !now; onNotice("Não foi possível curtir agora.") } }
        }.start()
    }
    // O story pausa enquanto o menu, a lista de visualizações ou qualquer janela está aberta.
    val holding = showViewers || showComments || replying || contribTarget.isNotBlank() || menuOpen || confirmDelete || highlightAsk || confirmBlock || reportOpen
    LaunchedEffect(current.id, holding) {
        if (holding) return@LaunchedEffect
        // Vídeo: a própria reprodução dirige a barra (veja StoryVideo). Foto/texto: 6 s.
        if (current.mediaType == "video") return@LaunchedEffect
        // Relógio feito à mão (tempo real + quadro a quadro) e NÃO animateTo: com "animações desligadas" no celular (escala 0), animateTo termina na hora,
        // a barra enche de uma vez e o story pula sozinho até o último, ficando parado.
        val total = 6000f
        val startValue = progress.value
        val startTime = android.os.SystemClock.elapsedRealtime()
        while (progress.value < 1f) {
            androidx.compose.runtime.withFrameMillis { }
            progress.snapTo((startValue + (android.os.SystemClock.elapsedRealtime() - startTime) / total).coerceIn(0f, 1f))
        }
        next()
    }

    val isContribPage = current.page == "contrib"
    val storyOwner = if (isContribPage) current.authorUsername else entry.username
    val ownerIsMe = if (isContribPage) current.authorMine else entry.mine
    val deletable = (entry.mine && current.page != "invite") || (isContribPage && current.authorMine)
    fun deleteCurrent() {
        val id = current.id
        if (current.page == "contrib") {
            val cid = current.contribId
            if (!token.isNullOrBlank()) Thread { try { QinblyApi.deleteContribution(token, cid) } catch (_: Exception) { } }.start()
            items.removeAll { it.id == id }
        } else {
            if (!token.isNullOrBlank()) Thread { try { QinblyApi.deleteStory(token, id) } catch (_: Exception) { } }.start()
            items.removeAll { it.id == id || it.parentId == id }
        }
        if (items.isEmpty()) onDismiss() else if (index > items.lastIndex) index = items.lastIndex
    }
    fun saveCurrent() {
        val url = current.mediaUrl
        val video = current.mediaType == "video"
        onNotice("Salvando...")
        Thread {
            val f = qinblyDownloadToCache(context, url, if (video) ".mp4" else ".jpg")
            val ok = f != null && qinblySaveMediaCopy(context, Uri.fromFile(f), video)
            f?.delete()
            Handler(Looper.getMainLooper()).post { onNotice(if (ok) "Salvo na galeria." else "Não foi possível salvar agora.") }
        }.start()
    }

    // securePolicy = SecureOn liga o FLAG_SECURE só enquanto o story está aberto: print e gravação de tela saem pretos.
    QinblyDialog(onDismissRequest = { closeSafe() }, properties = DialogProperties(dismissOnClickOutside = false, usePlatformDefaultWidth = false, decorFitsSystemWindows = false, securePolicy = androidx.compose.ui.window.SecureFlagPolicy.SecureOn), lift = false) {
        Box(
            Modifier.fillMaxSize().background(Color.Black).pointerInput(current.id, entry.mine) {
                // Dona do story: arrastar pra cima na parte de baixo da tela abre a lista de visualizações.
                if (entry.mine && current.page == "story") detectVerticalDragGestures { change, dy -> if (dy < -10f && change.position.y > size.height * 0.55f) showViewers = true }
            }
        ) {
            val textStory = if (current.mediaType == "text") qinblyParseTextStory(current.caption) else null
            if (textStory != null) QinblyTextStoryContent(textStory, Modifier.fillMaxSize())
            else if (current.mediaType == "video") StoryVideo(current.mediaUrl, paused = holding, onProgress = { progress.snapTo(it) }) { next() }
            else AsyncImage(model = current.mediaUrl, contentDescription = "Story", contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize())

            // Toque (não arrastar): terço da ESQUERDA volta o story, o resto da tela pula para o próximo. Duas áreas transparentes por cima da mídia
            // e por baixo dos botões, então botões e campos continuam tendo prioridade.
            Row(Modifier.fillMaxSize()) {
                Box(Modifier.weight(1f).fillMaxHeight().clickable(interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }, indication = null) {
                    if (replying) focusManager.clearFocus() else if (!tooSoon()) previous()
                })
                Box(Modifier.weight(2f).fillMaxHeight().clickable(interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }, indication = null) {
                    if (replying) focusManager.clearFocus() else if (!tooSoon()) next()
                })
            }

            Column(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 10.dp, vertical = 8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    items.forEachIndexed { i, _ ->
                        val fraction = when { i < index -> 1f; i == index -> progress.value; else -> 0f }
                        Box(Modifier.weight(1f).height(3.dp).clip(RoundedCornerShape(2.dp)).background(Color(0x55FFFFFF))) {
                            Box(Modifier.fillMaxWidth(fraction.coerceIn(0f, 1f)).fillMaxHeight().background(Color.White))
                        }
                    }
                }
                Row(Modifier.fillMaxWidth().padding(top = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    val headAvatar = if (current.page == "contrib") current.authorAvatar else entry.avatarUrl
                    val music = if (current.mediaType != "text" && current.page != "invite") qinblySplitMusic(current.caption).second else null
                    Box(Modifier.size(46.dp).clip(CircleShape).background(Color(0xFF2B3544))) {
                        if (headAvatar.isNotBlank()) AsyncImage(model = headAvatar, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(if (current.page == "contrib") current.authorName else (if (entry.mine) "Seu story" else entry.displayName.ifBlank { entry.username }) + (if (QinblyApi.isCollabCaption(current.caption)) "  ·  Story coletivo" else ""), color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp,
                            maxLines = 1, overflow = TextOverflow.Ellipsis)
                        if (music != null) QinblyMusicLine(music)
                    }
                    if (music != null) {
                        QinblyIconOf(if (muted) QinblyIcon.VolumeOff else QinblyIcon.VolumeUp, Color.White, 28.dp, Modifier.clip(CircleShape).clickable { muted = !muted }.padding(horizontal = 6.dp, vertical = 4.dp))
                        QinblyMusicPlayer(music.url, muted, paused = holding)
                    }
                    if (current.page != "invite") Box {
                        Column(
                            Modifier.size(36.dp).clip(CircleShape).clickable { menuOpen = true },
                            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically), horizontalAlignment = Alignment.CenterHorizontally
                        ) { repeat(3) { Box(Modifier.size(4.dp).clip(CircleShape).background(Color.White)) } }
                        androidx.compose.material3.DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                            val origin = qinblyParseOrigin(current.caption)
                            if (origin != null) {
                                QinblyStoryMenuRow("Ver original") { menuOpen = false; onOpenOriginal(origin, index) }
                                QinblyStoryMenuRow("Ver perfil de @" + origin.username) { menuOpen = false; onOpenProfile(origin.username, index) }
                            }
                            if (isContribPage && !current.authorMine) QinblyStoryMenuRow("Ver perfil de @" + current.authorUsername) { menuOpen = false; onOpenProfile(current.authorUsername, index) }
                            if (!isContribPage && entry.mine) {
                                if (onAddStory != null) QinblyStoryMenuRow("Adicionar story") { menuOpen = false; onAddStory() }
                                if (current.mediaType != "text" && origin == null) QinblyStoryMenuRow("Salvar na galeria") { menuOpen = false; saveCurrent() }
                                if (current.page != "invite") QinblyStoryMenuRow("Quem pode ver") { menuOpen = false; audienceAsk = true }
                                if (current.page != "invite") QinblyStoryMenuRow("Destacar no perfil") { menuOpen = false; highlightTitle = "Destaques"; highlightAsk = true }
                            }
                            if (!isContribPage && !entry.mine) {
                                val mutedNow = QinblyStoryMute.isMuted(prefs, storyOwner)
                                QinblyStoryMenuRow(if (mutedNow) "Reativar stories de @" + storyOwner else "Silenciar @" + storyOwner) {
                                    menuOpen = false
                                    if (mutedNow) { QinblyStoryMute.unmute(prefs, storyOwner); onNotice("Stories de @$storyOwner reativados.") }
                                    else { QinblyStoryMute.mute(prefs, storyOwner); onNotice("Stories de @$storyOwner silenciados. Eles vão para o fim da fila e sem o aro colorido.") }
                                }
                            }
                            if (deletable) QinblyStoryMenuRow(if (isContribPage) "Apagar resposta" else "Excluir story", danger = true) { menuOpen = false; confirmDelete = true }
                            if (!ownerIsMe && storyOwner.isNotBlank()) {
                                QinblyStoryMenuRow("Denunciar story") { menuOpen = false; reportOpen = true }
                                QinblyStoryMenuRow("Bloquear @" + storyOwner, danger = true) { menuOpen = false; confirmBlock = true }
                            }
                        }
                    }
                }
            }

            Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth().imePadding().padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = qinblyInputBottomPadding(16.dp)), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (qinblyDisplayCaption(current.caption).isNotBlank() && current.mediaType != "text") {
                    Text(qinblyDisplayCaption(current.caption), color = Color.White, fontSize = 16.sp, modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(Color(0x88000000)).padding(horizontal = 12.dp, vertical = 8.dp))
                }
                val isCollab = QinblyApi.isCollabCaption(current.caption)
                if (current.page == "invite") {
                    Text("Participar", color = Color(0xFF26215C), fontWeight = FontWeight.SemiBold, fontSize = 16.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)).background(Color.White).clickable { contribTarget = current.parentId }.padding(vertical = 15.dp))
                } else if (current.page == "story" && !isCollab) {
                    // Sem comentários no story: quem vê só envia mensagem (texto) no privado da pessoa.
                    if (!entry.mine) QinblyStoryMessageBar(entry.username, Color(0x66000000), Color(0xCCFFFFFF), onNotice,
                        onSent = { focusManager.clearFocus() }, onFocus = { replying = it }, liked = liked, onLike = { toggleLike() })
                }
                // Visualizações: só a dona do story vê. Avatares de quem viu por último; tocar ou arrastar pra cima abre a lista completa.
                if (entry.mine && current.page == "story") {
                    val recent = (viewers ?: emptyList()).take(3)
                    Column(
                        Modifier.align(Alignment.Start).clip(RoundedCornerShape(16.dp)).clickable { showViewers = true }.padding(horizontal = 4.dp, vertical = 6.dp)
                            .pointerInput(Unit) { detectVerticalDragGestures { _, dy -> if (dy < -8f) showViewers = true } },
                        horizontalAlignment = Alignment.Start
                    ) {
                        if (recent.isNotEmpty()) Row(horizontalArrangement = Arrangement.spacedBy((-10).dp)) {
                            recent.forEach { v ->
                                Box(
                                    Modifier.size(34.dp).clip(CircleShape).border(1.5.dp, Color.White, CircleShape).background(Color(0xFF2B3544)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text((v.displayName.ifBlank { v.username }).take(1).uppercase(), color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                    if (v.avatarUrl.isNotBlank()) AsyncImage(model = v.avatarUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                                }
                            }
                        }
                        Text("Visualizações", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(top = if (recent.isNotEmpty()) 4.dp else 0.dp))
                    }
                }
            }
        }
    }

    if (contribTarget.isNotBlank()) {
        QinblyContributeFlow(
            storyId = contribTarget, onNotice = onNotice,
            onCamera = { val sid = contribTarget; contribTarget = ""; onCamera(sid); onDismiss() },
            onDone = { contribTarget = ""; reloadContribs++ },
            onDismiss = { contribTarget = "" }
        )
    }


    if (showViewers) QinblyStorySummary(current.id, current.createdAt, accent, { showViewers = false }, onNotice, onOpenProfile = { u -> showViewers = false; onOpenProfile(u, index) })

    if (audienceAsk) androidx.compose.material3.AlertDialog(
        onDismissRequest = { audienceAsk = false },
        title = { Text("Quem pode ver este story") },
        text = {
            Column {
                listOf(
                    Triple("all", "Todos", "Qualquer pessoa"),
                    Triple("friends", "Amigos", "Quem você segue e te segue"),
                    Triple("followers", "Seguidores", "Só quem te segue"),
                    Triple("near", "Perto de você", "Quem está na sua cidade")
                ).forEach { (key, title, sub) ->
                    Column(Modifier.fillMaxWidth().clickable {
                        audienceAsk = false
                        val sid = current.id.substringBefore("#")
                        val city = QinblyApi.myCity(prefs)
                        if (token.isNullOrBlank()) onNotice("Entre na sua conta.")
                        else if (key == "near" && city.isBlank()) onNotice("O app ainda não sabe sua cidade. Abra o Perto de mim ou o Mercado e tente de novo.")
                        else Thread {
                            try {
                                QinblyApi.setStoryAudience(token, sid, key, city)
                                android.os.Handler(android.os.Looper.getMainLooper()).post { onNotice("Pronto: agora este story é para \"$title\".") }
                            } catch (e: Exception) {
                                android.os.Handler(android.os.Looper.getMainLooper()).post { onNotice(e.message ?: "Não foi possível mudar agora.") }
                            }
                        }.start()
                    }.padding(vertical = 8.dp)) {
                        Text(title, fontWeight = FontWeight.SemiBold)
                        Text(sub, fontSize = 12.sp, color = Color(0xFF8E87A8))
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = { audienceAsk = false }) { Text("Cancelar", color = accent) } },
        containerColor = QiblyTheme.card, titleContentColor = QiblyTheme.text, textContentColor = QiblyTheme.text
    )
    if (highlightAsk) androidx.compose.material3.AlertDialog(
        onDismissRequest = { highlightAsk = false },
        title = { Text("Destacar no perfil") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Este story fica no seu perfil, na linha de Destaques, mesmo depois das 24 horas.")
                androidx.compose.material3.OutlinedTextField(highlightTitle, { highlightTitle = it.take(24) }, label = { Text("Nome do destaque") }, singleLine = true)
            }
        },
        confirmButton = {
            TextButton(onClick = {
                highlightAsk = false
                val sid = current.id.substringBefore("#")
                val title = highlightTitle.trim().ifBlank { "Destaques" }
                if (token.isNullOrBlank()) onNotice("Entre na sua conta.")
                else Thread {
                    try {
                        QinblyApi.createHighlight(token, sid, title)
                        android.os.Handler(android.os.Looper.getMainLooper()).post { onNotice("Story destacado no seu perfil.") }
                    } catch (e: Exception) {
                        android.os.Handler(android.os.Looper.getMainLooper()).post { onNotice(e.message ?: "Não foi possível destacar agora.") }
                    }
                }.start()
            }) { Text("Destacar", color = accent) }
        },
        dismissButton = { TextButton(onClick = { highlightAsk = false }) { Text("Cancelar", color = accent) } },
        containerColor = QiblyTheme.card, titleContentColor = QiblyTheme.text, textContentColor = QiblyTheme.text
    )
    if (confirmDelete) androidx.compose.material3.AlertDialog(
        onDismissRequest = { confirmDelete = false },
        title = { Text(if (isContribPage) "Apagar esta resposta?" else "Excluir este story?") },
        text = { Text(if (isContribPage) "A resposta some do story coletivo." else "O story some para todo mundo e não dá para desfazer.") },
        confirmButton = { TextButton(onClick = { confirmDelete = false; deleteCurrent() }) { Text(if (isContribPage) "Apagar" else "Excluir", color = Color(0xFFD33B3B)) } },
        dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancelar", color = accent) } },
        containerColor = QiblyTheme.card, titleContentColor = QiblyTheme.text, textContentColor = QiblyTheme.text
    )
    if (confirmBlock) androidx.compose.material3.AlertDialog(
        onDismissRequest = { confirmBlock = false },
        title = { Text("Bloquear @$storyOwner?") },
        text = { Text("Vocês não verão mais as publicações um do outro nem poderão se seguir ou trocar mensagens. Você pode desbloquear em Configurações > Contas bloqueadas.") },
        confirmButton = {
            TextButton(onClick = {
                confirmBlock = false
                if (token.isNullOrBlank()) onNotice("Entre na sua conta para bloquear.")
                else Thread {
                    try {
                        QinblyApi.blockByUsername(token, storyOwner)
                        Handler(Looper.getMainLooper()).post { onNotice("Usuário bloqueado."); onDismiss() }
                    } catch (e: Exception) { Handler(Looper.getMainLooper()).post { onNotice(e.message ?: "Não foi possível bloquear agora.") } }
                }.start()
            }) { Text("Bloquear", color = Color(0xFFD33B3B)) }
        },
        dismissButton = { TextButton(onClick = { confirmBlock = false }) { Text("Cancelar", color = accent) } },
        containerColor = QiblyTheme.card, titleContentColor = QiblyTheme.text, textContentColor = QiblyTheme.text
    )
    if (reportOpen) ReportReasonDialog(title = "Denunciar story: qual o motivo?", onDismiss = { reportOpen = false }) { reason ->
        reportOpen = false
        if (token.isNullOrBlank()) onNotice("Entre na sua conta para denunciar.")
        else Thread {
            try {
                QinblyApi.reportUserByUsername(token, storyOwner, reason)
                Handler(Looper.getMainLooper()).post { onNotice("Denúncia enviada. Obrigado por avisar.") }
            } catch (e: Exception) { Handler(Looper.getMainLooper()).post { onNotice(e.message ?: "Não foi possível enviar a denúncia agora.") } }
        }.start()
    }
}


/** Mensagem de texto para o privado de quem postou o story (sem foto, áudio ou figurinha). */
@Composable
internal fun QinblyStoryMessageBar(username: String, fieldBg: Color, hintColor: Color, onNotice: (String) -> Unit, onSent: () -> Unit = {}, onFocus: (Boolean) -> Unit = {}, liked: Boolean = false, onLike: () -> Unit = {}) {
    val onNotice: (String) -> Unit = qinblyToast(onNotice)
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { context.getSharedPreferences("qinbly_session", android.content.Context.MODE_PRIVATE).byAccount() }
    val token = remember { prefs.getString("token", null) }
    var draft by remember { mutableStateOf("") }
    var sending by remember { mutableStateOf(false) }
    fun send() {
        val body = draft.trim()
        if (body.isBlank() || token.isNullOrBlank() || sending) return
        sending = true
        Thread {
            val main = android.os.Handler(android.os.Looper.getMainLooper())
            try {
                QinblyApi.sendMessage(token, username, "Respondeu ao seu story: " + body.take(1000))
                main.post { draft = ""; sending = false; onNotice("Mensagem enviada."); onSent() }
            } catch (e: Exception) { main.post { sending = false; onNotice(e.message ?: "Não foi possível enviar a mensagem.") } }
        }.start()
    }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(Modifier.weight(1f).clip(RoundedCornerShape(26.dp)).background(fieldBg).padding(horizontal = 18.dp, vertical = 14.dp)) {
            if (draft.isEmpty()) Text("Enviar mensagem…", color = hintColor, fontSize = 15.sp)
            androidx.compose.foundation.text.BasicTextField(
                draft, { draft = it.take(1000) },
                textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 15.sp),
                cursorBrush = androidx.compose.ui.graphics.SolidColor(Color.White), maxLines = 3,
                modifier = Modifier.fillMaxWidth().onFocusChanged { onFocus(it.isFocused) }
            )
        }
        Box(Modifier.size(46.dp).clip(CircleShape).background(Color(0x66000000)).clickable { onLike() }, contentAlignment = Alignment.Center) {
            QinblyIconOf(if (liked) QinblyIcon.Favorite else QinblyIcon.FavoriteBorder, if (liked) Color(0xFFE5484D) else Color.White, 24.dp)
        }
        val ready = draft.isNotBlank() && !sending
        Box(Modifier.size(46.dp).clip(CircleShape).background(if (ready) Color(0xFF2F68E0) else Color(0x66000000)).clickable(enabled = ready) { send() }, contentAlignment = Alignment.Center) { QinblySendIcon(Color.White, 20.dp) }
    }
}

/** Resumo do story, só para quem postou: quantos viram/curtiram, quando viram e quem está perto. Tudo numa rolagem só. */
@Composable
internal fun QinblyStorySummary(storyId: String, createdAt: String, accent: Color, onDismiss: () -> Unit, onNotice: (String) -> Unit, onOpenProfile: (String) -> Unit = {}) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { context.getSharedPreferences("qinbly_session", Context.MODE_PRIVATE).byAccount() }
    val token = remember { prefs.getString("token", null) }
    val ownerCity = remember { (prefs.getString("market_city", null)?.takeIf { it.isNotBlank() } ?: prefs.getString("device_place", null)).orEmpty() }
    var viewers by remember { mutableStateOf<List<QinblyApi.StoryViewer>?>(null) }
    var cities by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var likedBy by remember { mutableStateOf<Set<String>>(emptySet()) }
    LaunchedEffect(storyId) {
        if (token.isNullOrBlank()) { viewers = emptyList(); return@LaunchedEffect }
        try {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                val list = QinblyApi.getStoryViewers(token, storyId)
                val cm = HashMap<String, String>()
                try {
                    val a = org.json.JSONArray(QinblyApi.rest(token, "GET", "story_views?story_id=eq.$storyId&select=city,profiles!story_views_viewer_id_fkey(username)"))
                    for (i in 0 until a.length()) { val o = a.getJSONObject(i); val u = o.optJSONObject("profiles")?.optString("username").orEmpty(); val c = o.optString("city"); if (u.isNotBlank() && c.isNotBlank() && c != "null") cm[u] = c }
                } catch (_: Exception) { }
                val lk = HashSet<String>()
                try {
                    val a = org.json.JSONArray(QinblyApi.rest(token, "GET", "story_likes?story_id=eq.$storyId&select=profiles!story_likes_user_id_fkey(username)"))
                    for (i in 0 until a.length()) { val u = a.getJSONObject(i).optJSONObject("profiles")?.optString("username").orEmpty(); if (u.isNotBlank()) lk.add(u) }
                } catch (_: Exception) { }
                cities = cm; likedBy = lk; viewers = list
            }
        } catch (e: Exception) { viewers = emptyList(); onNotice(e.message ?: "Não foi possível ver quem viu.") }
    }
    val list = viewers
    QinblyFullScreen("Seu story", onDismiss, scroll = false) {
        if (list == null) { Box(Modifier.fillMaxWidth().padding(vertical = 28.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = accent, modifier = Modifier.size(28.dp)) }; return@QinblyFullScreen }
        fun near(v: QinblyApi.StoryViewer): Boolean { val c = cities[v.username].orEmpty(); return c.isNotBlank() && ownerCity.isNotBlank() && c.equals(ownerCity, ignoreCase = true) }
        // Barras do gráfico: 12 faixas de 2 h desde que o story foi postado.
        val start = try { java.time.OffsetDateTime.parse(createdAt).toInstant().toEpochMilli() } catch (_: Exception) { 0L }
        val buckets = IntArray(12)
        for (v in list) {
            val t = try { java.time.OffsetDateTime.parse(v.viewedAt).toInstant().toEpochMilli() } catch (_: Exception) { 0L }
            if (start > 0 && t > 0) buckets[(((t - start) / 3_600_000L) / 2).toInt().coerceIn(0, 11)]++
        }
        val maxB = (buckets.maxOrNull() ?: 0).coerceAtLeast(1)
        val peak = buckets.indices.maxByOrNull { buckets[it] } ?: 0
        @Composable fun Person(v: QinblyApi.StoryViewer, sub: String) {
            Row(Modifier.fillMaxWidth().clickable { onOpenProfile(v.username) }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(46.dp).clip(CircleShape).background(accent), contentAlignment = Alignment.Center) {
                    Text((v.displayName.ifBlank { v.username }).take(1).uppercase(), color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                    if (v.avatarUrl.isNotBlank()) AsyncImage(model = v.avatarUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(v.displayName.ifBlank { v.username }, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(sub + " · " + qinblyAgo(v.viewedAt), color = Color(0xFF8E87A8), fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                if (v.username in likedBy) QinblyIconOf(QinblyIcon.Favorite, Color(0xFFE5484D), 20.dp)
            }
            androidx.compose.material3.HorizontalDivider(Modifier.padding(start = 60.dp), color = Color(0xFF1E1E23))
        }
        val line = Color(0xFF2A2A30)
        LazyColumn(Modifier.fillMaxSize().padding(horizontal = 22.dp)) {
            item {
                Row(Modifier.fillMaxWidth().padding(top = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(list.size.toString(), color = Color.White, fontSize = 52.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-2).sp)
                        Text("viram", color = Color(0xFF8E87A8), fontSize = 14.sp)
                    }
                    Box(Modifier.width(1.dp).height(72.dp).background(line))
                    Column(Modifier.weight(1f).padding(start = 28.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(likedBy.size.toString(), color = Color.White, fontSize = 52.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-2).sp)
                            Spacer(Modifier.width(8.dp))
                            QinblyIconOf(QinblyIcon.Favorite, Color(0xFFE5484D), 24.dp)
                        }
                        Text("curtiram" + if (list.isNotEmpty() && likedBy.isNotEmpty()) " · ${likedBy.size * 100 / list.size}%" else "", color = Color(0xFF8E87A8), fontSize = 14.sp)
                    }
                }
                androidx.compose.material3.HorizontalDivider(Modifier.padding(top = 18.dp), color = line)
            }
            if (list.isNotEmpty()) item {
                Column(Modifier.fillMaxWidth().padding(top = 18.dp)) {
                    Text("Quando viram", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    Row(Modifier.fillMaxWidth().height(90.dp).padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.Bottom) {
                        for (i in 0 until 12) {
                            val h = (4 + 76f * buckets[i] / maxB).dp
                            Box(Modifier.weight(1f).height(h).clip(RoundedCornerShape(6.dp)).background(if (i == peak && buckets[i] > 0) Color(0xFF4C7BD6) else Color(0xFF25304A)))
                        }
                    }
                    Row(Modifier.fillMaxWidth().padding(top = 4.dp)) {
                        Text("início", color = Color(0xFF6E6E78), fontSize = 12.sp, modifier = Modifier.weight(1f))
                        if (buckets[peak] > 0) Text("pico entre ${peak * 2}h e ${peak * 2 + 2}h", color = Color(0xFF8E87A8), fontSize = 12.sp)
                        Text("24 h", color = Color(0xFF6E6E78), fontSize = 12.sp, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.End)
                    }
                }
                androidx.compose.material3.HorizontalDivider(Modifier.padding(top = 14.dp), color = line)
            }
            if (list.isEmpty()) item { Text("Ninguém viu ainda.", color = Color(0xFFF2F4F8), fontSize = 15.sp, modifier = Modifier.padding(vertical = 24.dp)) }
            else {
                item { Row(Modifier.fillMaxWidth().padding(top = 18.dp, bottom = 4.dp)) {
                    Text("Quem viu", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, modifier = Modifier.weight(1f))
                    Text("${list.size} pessoa" + if (list.size > 1) "s" else "", color = Color(0xFF6E6E78), fontSize = 13.sp)
                } }
                items(list, key = { it.username }) { v ->
                    Person(v, if (near(v)) "Sua cidade" else cities[v.username]?.substringBefore(",") ?: "Cidade não informada")
                }
            }
            item { Spacer(Modifier.height(40.dp)) }
        }
    }
}

/** Linha de "Destaques" do perfil: stories guardados que não somem em 24 horas. Some se não houver nenhum. */
@Composable
internal fun QinblyHighlightsRow(username: String, prefs: android.content.SharedPreferences, mine: Boolean, onNotice: (String) -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var list by remember(username) { mutableStateOf<List<QinblyApi.Highlight>>(emptyList()) }
    var openIndex by remember(username) { mutableIntStateOf(-1) }
    var reload by remember(username) { mutableIntStateOf(0) }
    LaunchedEffect(username, reload) {
        if (username.isBlank()) return@LaunchedEffect
        try { list = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) { QinblyApi.getHighlights(username, prefs.getString("token", null)) } } catch (_: Exception) { }
    }
    if (list.isEmpty()) return
    androidx.compose.foundation.lazy.LazyRow(modifier.fillMaxWidth(), contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        items(list.size, key = { list[it].id }) { i ->
            val h = list[i]
            Column(Modifier.width(68.dp).clickable { openIndex = i }, horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.size(64.dp).clip(CircleShape).background(Color(0xFF2A2438)).border(2.dp, QiblyTheme.border, CircleShape), contentAlignment = Alignment.Center) {
                    if (h.mediaType == "text" || h.mediaUrl.isBlank()) {
                        Text("Aa", color = Color.White, fontWeight = FontWeight.SemiBold)
                    } else {
                        val request = remember(h.mediaUrl) {
                            coil.request.ImageRequest.Builder(context).data(h.mediaUrl).apply {
                                if (h.mediaType == "video") { decoderFactory(coil.decode.VideoFrameDecoder.Factory()); videoFrameMillis(500) }
                            }.crossfade(true).build()
                        }
                        AsyncImage(model = request, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = androidx.compose.ui.layout.ContentScale.Crop)
                    }
                }
                Text(h.title, color = QiblyTheme.text, fontSize = 12.sp, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis, modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
    val current = list.getOrNull(openIndex)
    if (current != null) {
        var confirmRemove by remember(current.id) { mutableStateOf(false) }
        Dialog(onDismissRequest = { openIndex = -1 }, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
            Box(Modifier.fillMaxSize().background(Color.Black)) {
                when {
                    current.mediaType == "video" && current.mediaUrl.isNotBlank() -> StoryVideo(current.mediaUrl, paused = confirmRemove, onEnded = { })
                    current.mediaType == "text" || current.mediaUrl.isBlank() -> Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text(qinblyDisplayCaption(current.caption).ifBlank { current.title }, color = Color.White, fontSize = 24.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center, fontWeight = FontWeight.SemiBold)
                    }
                    else -> AsyncImage(model = current.mediaUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = androidx.compose.ui.layout.ContentScale.Fit)
                }
                Row(Modifier.fillMaxWidth().statusBarsPadding().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(current.title, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, modifier = Modifier.weight(1f).padding(start = 8.dp))
                    if (mine) androidx.compose.material3.IconButton(onClick = { confirmRemove = true }) { Icon(Icons.Outlined.Delete, contentDescription = "Remover destaque", tint = Color.White) }
                    androidx.compose.material3.IconButton(onClick = { openIndex = -1 }) { Icon(Icons.Default.Close, contentDescription = "Fechar", tint = Color.White) }
                }
                if (current.caption.isNotBlank() && current.mediaType != "text" && qinblyDisplayCaption(current.caption).isNotBlank()) {
                    Text(qinblyDisplayCaption(current.caption), color = Color.White, modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(20.dp))
                }
            }
        }
        if (confirmRemove) androidx.compose.material3.AlertDialog(
            onDismissRequest = { confirmRemove = false },
            title = { Text("Remover dos destaques?") },
            text = { Text("O story deixa de aparecer no seu perfil. Não dá para desfazer.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmRemove = false
                    val token = prefs.getString("token", null)
                    val id = current.id
                    if (token.isNullOrBlank()) onNotice("Entre na sua conta.")
                    else Thread {
                        try {
                            QinblyApi.deleteHighlight(token, id)
                            android.os.Handler(android.os.Looper.getMainLooper()).post { openIndex = -1; list = list.filterNot { it.id == id }; reload++; onNotice("Destaque removido.") }
                        } catch (e: Exception) {
                            android.os.Handler(android.os.Looper.getMainLooper()).post { onNotice(e.message ?: "Não foi possível remover agora.") }
                        }
                    }.start()
                }) { Text("Remover", color = Color(0xFFD33B3B)) }
            },
            dismissButton = { TextButton(onClick = { confirmRemove = false }) { Text("Cancelar") } },
            containerColor = QiblyTheme.card, titleContentColor = QiblyTheme.text, textContentColor = QiblyTheme.text
        )
    }
}


/** Stories silenciados: a pessoa vai para o fim da fila, sem o aro colorido. Fica guardado no celular (rápido) e no servidor (vale em todos os aparelhos). */
internal object QinblyStoryMute {
    var version by mutableIntStateOf(0)
    private fun all(prefs: android.content.SharedPreferences): Set<String> = prefs.getStringSet("story_muted", emptySet()).orEmpty()
    fun isMuted(prefs: android.content.SharedPreferences, username: String): Boolean {
        version // faz a tela recompor quando muda
        return username.lowercase() in all(prefs)
    }
    fun mute(prefs: android.content.SharedPreferences, username: String) {
        prefs.edit().putStringSet("story_muted", all(prefs) + username.lowercase()).apply(); version++
        push(prefs, username, true)
    }
    fun unmute(prefs: android.content.SharedPreferences, username: String) {
        prefs.edit().putStringSet("story_muted", all(prefs) - username.lowercase()).apply(); version++
        push(prefs, username, false)
    }
    /** Avisa o servidor em segundo plano (para valer em todos os aparelhos). Se faltar rede, o próximo sync acerta. */
    private fun push(prefs: android.content.SharedPreferences, username: String, muted: Boolean) {
        val token = prefs.getString("token", null)?.takeIf { it.isNotBlank() } ?: return
        Thread {
            try { QinblyApi.rest(token, "POST", "rpc/story_mute_set", JSONObject().put("p_username", username.lowercase()).put("p_muted", muted).toString(), "return=minimal") } catch (_: Exception) {}
        }.start()
    }
    /** Busca a lista do servidor. Na primeira vez, sobe o que já estava só neste celular; depois o servidor manda. Chamar fora da tela principal. */
    fun sync(prefs: android.content.SharedPreferences) {
        val token = prefs.getString("token", null)?.takeIf { it.isNotBlank() } ?: return
        try {
            val arr = org.json.JSONArray(QinblyApi.rest(token, "POST", "rpc/story_mute_list", "{}"))
            val server = (0 until arr.length()).map { arr.optString(it).lowercase() }.filter { it.isNotBlank() }.toSet()
            if (!prefs.getBoolean("story_mute_migrated", false)) {
                for (u in all(prefs) - server) QinblyApi.rest(token, "POST", "rpc/story_mute_set", JSONObject().put("p_username", u).put("p_muted", true).toString(), "return=minimal")
                prefs.edit().putBoolean("story_mute_migrated", true).putStringSet("story_muted", all(prefs) + server).apply()
            } else prefs.edit().putStringSet("story_muted", server).apply()
            version++
        } catch (_: Exception) {}
    }
}
