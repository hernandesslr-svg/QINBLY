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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
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
    val prefs = remember { context.getSharedPreferences("qinbly_session", Context.MODE_PRIVATE) }
    var step by remember { mutableStateOf("menu") } // menu, compose, note
    var picked by remember { mutableStateOf<Uri?>(null) }
    var pickedIsVideo by remember { mutableStateOf(false) }
    var caption by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var pendingPhoto by remember { mutableStateOf<File?>(null) }

    val galleryPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            picked = uri
            pickedIsVideo = (context.contentResolver.getType(uri) ?: "").startsWith("video/")
            step = "compose"
        }
    }
    // Câmera padrão do app (a mesma do Criar), em vez da câmera do celular.
    var showAppCamera by remember { mutableStateOf(false) }
    fun launchCamera() { showAppCamera = true }
    if (showAppCamera) QinblyCameraCapture(
        onResult = { u -> showAppCamera = false; picked = u; pickedIsVideo = false; step = "compose" },
        onDismiss = { showAppCamera = false }
    )
    val cameraPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) launchCamera() else onNotice("Permita a câmera para tirar a foto do story.")
    }

    when (step) {
        "menu" -> QinblyDialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false), lift = false) {
            val soon: (String) -> Unit = { onNotice("$it: em breve.") }
            val card = Color(0xFF232325)
            val red = Color(0xFFE5484D)
            @Composable fun Big(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, sub: String, modifier: Modifier, onClick: () -> Unit) {
                Column(modifier.clip(RoundedCornerShape(18.dp)).background(card).clickable { onClick() }.padding(horizontal = 6.dp, vertical = 16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(22.dp))
                    Text(title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Text(sub, color = Color(0xFFA9B5C8), fontSize = 10.sp, lineHeight = 12.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                }
            }
            @Composable fun Short(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, sub: String, tint: Color = Color.White, onClick: () -> Unit) {
                Row(Modifier.fillMaxWidth().clickable { onClick() }.padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(38.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFF2C2C2E)), contentAlignment = Alignment.Center) { Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp)) }
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(title, color = tint, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        Text(sub, color = Color(0xFFA9B5C8), fontSize = 10.sp)
                    }
                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color(0xFFA9B5C8), modifier = Modifier.size(18.dp))
                }
            }
            Column(Modifier.fillMaxSize().background(Color(0xFF171719)).statusBarsPadding().verticalScroll(rememberScrollState()).padding(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 24.dp)) {
                Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Criar", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                    Text("  v" + BuildConfig.VERSION_NAME, color = Color(0x88FFFFFF), fontSize = 12.sp, modifier = Modifier.weight(1f))
                    Box(Modifier.size(40.dp).clip(CircleShape).background(Color(0x22FFFFFF)).clickable { onDismiss() }, contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar", tint = Color.White, modifier = Modifier.size(22.dp))
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Big(Icons.Outlined.AddBox, "Story", "Compartilhe fotos e vídeos", Modifier.weight(1f)) { onDismiss(); onAddStory() }
                    Big(Icons.Outlined.TextFields, "Texto", "Escreva e deixe comentarem", Modifier.weight(1f)) { step = "text" }
                    Big(Icons.Outlined.MusicNote, "Música", "Compartilhe uma música no seu perfil", Modifier.weight(1f)) { soon("Música") }
                }
                Text("Mais", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(top = 22.dp, bottom = 8.dp))
                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Color(0xFF232325)).padding(vertical = 6.dp)) {
                    if (hasStory) Short(Icons.Outlined.Visibility, "Ver meu story", "Abrir o story que está no ar") { onDismiss(); onViewMine() }
                    Short(Icons.Outlined.Groups, "Story coletivo", "Lance um tema e os amigos participam") { step = "collab" }
                    Short(Icons.Outlined.EmojiEmotions, "Clima do dia", "Mostre seu humor com emoji e cor") { step = "mood" }
                    Short(Icons.Outlined.HourglassEmpty, "Cápsula do tempo", "Guarde uma lembrança para abrir no futuro") { step = "capsule" }
                }
            }
        }
        "collab" -> QinblyContributeFlow(
            storyId = "", newCollab = true, onNotice = onNotice,
            onCamera = { qinblyStartCollabStory(); onDismiss(); onAddStory() },
            onDone = { onChanged(); onDismiss() }, onDismiss = onDismiss
        )
        "mood" -> QinblyMoodDialog(onDismiss = onDismiss, onNotice = onNotice, onChanged = onChanged)
        "capsule" -> QinblyCapsuleHub(accent, onDismiss, onNotice)
        "packs" -> QinblyStickerPacksDialog(accent, onDismiss, onNotice)
        "text" -> QinblyTextStoryComposer(onDismiss = onDismiss, onPublished = { onChanged(); onDismiss() }, onNotice = onNotice)
        "compose" -> AlertDialog(
            onDismissRequest = { if (!busy) onDismiss() },
            title = { Text("Novo story") },
            text = { QinblyClearFocusWhenKeyboardCloses();
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    val shown = picked
                    if (shown != null) {
                        if (pickedIsVideo) Text("Vídeo escolhido. Ele fica no ar por 24 horas.", fontSize = 14.sp)
                        else AsyncImage(model = shown, contentDescription = "Prévia do story", contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxWidth().heightIn(max = 220.dp).clip(RoundedCornerShape(14.dp)))
                    }
                    OutlinedTextField(caption, { caption = it.take(200) }, label = { Text("Legenda (opcional)") }, enabled = !busy)
                    if (busy) CircularProgressIndicator(color = accent, modifier = Modifier.size(26.dp))
                }
            },
            confirmButton = {
                TextButton(enabled = !busy && picked != null, onClick = {
                    val uri = picked ?: return@TextButton
                    val token = prefs.getString("token", null)
                    if (token.isNullOrBlank()) { onNotice("Entre na sua conta para publicar."); return@TextButton }
                    busy = true
                    Thread {
                        val main = Handler(Looper.getMainLooper())
                        try {
                            QinblyApi.createStory(context, token, uri, caption)
                            main.post { busy = false; onNotice("Story publicado. Fica no ar por 24 horas."); onChanged(); onDismiss() }
                        } catch (e: Exception) {
                            main.post { busy = false; onNotice(e.message ?: "Não foi possível publicar o story.") }
                        } finally { pendingPhoto?.delete() }
                    }.start()
                }) { Text("Publicar", color = accent, fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(enabled = !busy, onClick = onDismiss) { Text("Cancelar") } }
        )
        else -> AlertDialog(
            onDismissRequest = onDismiss,
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
                }) { Text("Compartilhar", color = accent, fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun MenuRow(text: String, accent: Color, onClick: () -> Unit) {
    Text(text, color = accent, fontSize = 15.sp, fontWeight = FontWeight.SemiBold,
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).clickable { onClick() }.padding(vertical = 12.dp, horizontal = 4.dp))
}

@Composable
internal fun StoryVideo(url: String, onEnded: () -> Unit) {
    val context = LocalContext.current
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
        onDispose { player.removeListener(listener); player.release() }
    }
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx -> PlayerView(ctx).apply { useController = false; resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT; this.player = player } },
        update = { it.player = player }
    )
}

/**
 * Visualizador de stories em tela cheia: barras de progresso, toque na direita avança e na esquerda volta,
 * passa sozinho (6 s para foto; vídeo até o fim, no máximo 20 s). Se o story é seu: contagem de quem viu, lista e excluir.
 */
@Composable
internal fun QinblyStoryViewer(entry: QinblyApi.TrayEntry, accent: Color, onNotice: (String) -> Unit, onDismiss: () -> Unit, onCamera: (String) -> Unit = {}) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("qinbly_session", Context.MODE_PRIVATE) }
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
    var index by remember(entry.username) { mutableIntStateOf(0) }
    var showViewers by remember { mutableStateOf(false) }
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
    fun next() { if (index < items.lastIndex) index++ else closeSafe() }
    fun previous() { if (index > 0) index-- }

    LaunchedEffect(current.id) {
        if (!entry.mine && current.page == "story" && !token.isNullOrBlank()) {
            Thread {
                try {
                    QinblyApi.markStoryViewed(token, current.id)
                    // Guarda a cidade de quem viu (só a dona do story enxerga) para separar "Perto de você" de "De mais longe".
                    val sp = context.getSharedPreferences("qinbly_session", Context.MODE_PRIVATE)
                    val uid = QinblyApi.currentUserId(sp)
                    val city = (sp.getString("market_city", null)?.takeIf { it.isNotBlank() } ?: sp.getString("device_place", null)).orEmpty()
                    if (!uid.isNullOrBlank() && city.isNotBlank())
                        QinblyApi.rest(token, "PATCH", "story_views?story_id=eq.${current.id}&viewer_id=eq.$uid", JSONObject().put("city", city).toString(), "return=minimal")
                } catch (_: Exception) { }
            }.start()
        }
    }
    // Curtidas do story: quem vê guarda a própria; a dona vê o total.
    var liked by remember(current.id) { mutableStateOf(false) }
    var likeCount by remember(current.id) { mutableIntStateOf(0) }
    LaunchedEffect(current.id) {
        if (token.isNullOrBlank() || current.page != "story") return@LaunchedEffect
        val sp = context.getSharedPreferences("qinbly_session", Context.MODE_PRIVATE)
        val uid = QinblyApi.currentUserId(sp)
        try {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                if (entry.mine) likeCount = org.json.JSONArray(QinblyApi.rest(token, "GET", "story_likes?story_id=eq.${current.id}&select=user_id")).length()
                else if (!uid.isNullOrBlank()) liked = org.json.JSONArray(QinblyApi.rest(token, "GET", "story_likes?story_id=eq.${current.id}&user_id=eq.$uid&select=story_id")).length() > 0
            }
        } catch (_: Exception) { }
    }
    fun toggleLike() {
        val sp = context.getSharedPreferences("qinbly_session", Context.MODE_PRIVATE)
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
    LaunchedEffect(current.id, showViewers, showComments, replying, contribTarget) {
        if (showViewers || showComments || replying || contribTarget.isNotBlank()) { progress.stop(); return@LaunchedEffect }
        val total = if (current.mediaType == "video") 20000 else 6000
        val remaining = ((1f - progress.value) * total).toInt().coerceAtLeast(1)
        progress.animateTo(1f, tween(durationMillis = remaining, easing = LinearEasing))
        next()
    }

    // securePolicy = SecureOn liga o FLAG_SECURE só enquanto o story está aberto: print e gravação de tela saem pretos.
    QinblyDialog(onDismissRequest = { closeSafe() }, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false, securePolicy = androidx.compose.ui.window.SecureFlagPolicy.SecureOn), lift = false) {
        Box(
            Modifier.fillMaxSize().background(Color.Black).pointerInput(current.id, replying) {
                detectTapGestures { offset ->
                    if (replying) focusManager.clearFocus()
                    else if (offset.y < size.height * 0.80f && !tooSoon()) { if (offset.x < size.width / 3f) previous() else next() }
                }
            }
        ) {
            val textStory = if (current.mediaType == "text") qinblyParseTextStory(current.caption) else null
            if (textStory != null) QinblyTextStoryContent(textStory, Modifier.fillMaxSize())
            else if (current.mediaType == "video") StoryVideo(current.mediaUrl) { next() }
            else AsyncImage(model = current.mediaUrl, contentDescription = "Story", contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize())

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
                        QinblyMusicPlayer(music.url, muted, paused = showViewers || showComments || replying || contribTarget.isNotBlank())
                    }
                    Icon(Icons.Default.Close, contentDescription = "Fechar", tint = Color.White, modifier = Modifier.size(28.dp).clickable { onDismiss() })
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
                val showViews = entry.mine && current.page == "story"
                val canDelete = (entry.mine && current.page != "invite") || (current.page == "contrib" && current.authorMine)
                if (showViews || canDelete) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        if (showViews) Row(
                            Modifier.clip(RoundedCornerShape(20.dp)).background(Color(0x66000000)).clickable {
                                showViewers = true
                            }.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Outlined.Visibility, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(current.views.toString(), color = Color.White, fontWeight = FontWeight.SemiBold)
                        }
                        if (showViews && likeCount > 0) Row(
                            Modifier.clip(RoundedCornerShape(20.dp)).background(Color(0x66000000)).clickable { showViewers = true }.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            QinblyIconOf(QinblyIcon.Favorite, Color(0xFFE5484D), 18.dp)
                            Spacer(Modifier.width(6.dp))
                            Text(likeCount.toString(), color = Color.White, fontWeight = FontWeight.SemiBold)
                        }
                        if (canDelete) Icon(Icons.Outlined.Delete, contentDescription = if (current.page == "contrib") "Apagar resposta" else "Excluir story", tint = Color.White,
                            modifier = Modifier.size(28.dp).clickable {
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
                            })
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


    if (showViewers) QinblyStorySummary(current.id, current.createdAt, accent, { showViewers = false }, onNotice)
}


/** Mensagem de texto para o privado de quem postou o story (sem foto, áudio ou figurinha). */
@Composable
internal fun QinblyStoryMessageBar(username: String, fieldBg: Color, hintColor: Color, onNotice: (String) -> Unit, onSent: () -> Unit = {}, onFocus: (Boolean) -> Unit = {}, liked: Boolean = false, onLike: () -> Unit = {}) {
    val onNotice: (String) -> Unit = qinblyToast(onNotice)
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { context.getSharedPreferences("qinbly_session", android.content.Context.MODE_PRIVATE) }
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
        Box(Modifier.size(46.dp).clip(CircleShape).background(if (ready) Color(0xFF1877F2) else Color(0x66000000)).clickable(enabled = ready) { send() }, contentAlignment = Alignment.Center) { QinblySendIcon(Color.White, 20.dp) }
    }
}

/** Resumo do story, só para quem postou: quantos viram/curtiram, quando viram e quem está perto. Tudo numa rolagem só. */
@Composable
internal fun QinblyStorySummary(storyId: String, createdAt: String, accent: Color, onDismiss: () -> Unit, onNotice: (String) -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { context.getSharedPreferences("qinbly_session", Context.MODE_PRIVATE) }
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
            Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(46.dp).clip(CircleShape).background(accent), contentAlignment = Alignment.Center) {
                    Text((v.displayName.ifBlank { v.username }).take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
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
                        Text(list.size.toString(), color = Color.White, fontSize = 52.sp, fontWeight = FontWeight.Bold, letterSpacing = (-2).sp)
                        Text("viram", color = Color(0xFF8E87A8), fontSize = 14.sp)
                    }
                    Box(Modifier.width(1.dp).height(72.dp).background(line))
                    Column(Modifier.weight(1f).padding(start = 28.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(likedBy.size.toString(), color = Color.White, fontSize = 52.sp, fontWeight = FontWeight.Bold, letterSpacing = (-2).sp)
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
                    Text("Quando viram", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
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
            if (list.isEmpty()) item { Text("Ninguém viu ainda.", color = Color(0xFFA9B5C8), fontSize = 15.sp, modifier = Modifier.padding(vertical = 24.dp)) }
            else {
                item { Row(Modifier.fillMaxWidth().padding(top = 18.dp, bottom = 4.dp)) {
                    Text("Quem viu", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp, modifier = Modifier.weight(1f))
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
