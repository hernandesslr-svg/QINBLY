package app.qinbly.android

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
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        val f = pendingPhoto
        if (ok && f != null) { picked = Uri.fromFile(f); pickedIsVideo = false; step = "compose" } else f?.delete()
    }
    fun launchCamera() {
        val f = File(context.cacheDir, "qinbly-story-" + System.currentTimeMillis() + ".jpg")
        pendingPhoto = f
        cameraLauncher.launch(androidx.core.content.FileProvider.getUriForFile(context, context.packageName + ".fileprovider", f))
    }
    val cameraPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) launchCamera() else onNotice("Permita a câmera para tirar a foto do story.")
    }

    when (step) {
        "menu" -> QinblyDialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false), lift = false) {
            val soon: (String) -> Unit = { onNotice("$it: em breve.") }
            val card = Color(0xFF222224)
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
            Column(Modifier.fillMaxSize().background(Color(0xFF151517)).statusBarsPadding().verticalScroll(rememberScrollState()).padding(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 24.dp)) {
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
                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Color(0xFF222224)).padding(vertical = 6.dp)) {
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
            text = {
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
            text = {
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
            Thread { try { QinblyApi.markStoryViewed(token, current.id) } catch (_: Exception) { } }.start()
        }
    }
    LaunchedEffect(current.id, showViewers, showComments, replying, contribTarget) {
        if (showViewers || showComments || replying || contribTarget.isNotBlank()) { progress.stop(); return@LaunchedEffect }
        val total = if (current.mediaType == "video") 20000 else 6000
        val remaining = ((1f - progress.value) * total).toInt().coerceAtLeast(1)
        progress.animateTo(1f, tween(durationMillis = remaining, easing = LinearEasing))
        next()
    }

    QinblyDialog(onDismissRequest = { closeSafe() }, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false), lift = false) {
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
                        Text(if (muted) "🔇" else "🔊", fontSize = 20.sp, modifier = Modifier.clip(CircleShape).clickable { muted = !muted }.padding(horizontal = 8.dp, vertical = 4.dp))
                        QinblyMusicPlayer(music.url, muted, paused = showViewers || showComments || replying || contribTarget.isNotBlank())
                    }
                    Icon(Icons.Default.Close, contentDescription = "Fechar", tint = Color.White, modifier = Modifier.size(28.dp).clickable { onDismiss() })
                }
            }

            Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth().imePadding().padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = if (imeOpen) 8.dp else 16.dp + qinblyNavBarPadding()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (qinblyDisplayCaption(current.caption).isNotBlank() && current.mediaType != "text") {
                    Text(qinblyDisplayCaption(current.caption), color = Color.White, fontSize = 16.sp, modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(Color(0x88000000)).padding(horizontal = 12.dp, vertical = 8.dp))
                }
                val isCollab = QinblyApi.isCollabCaption(current.caption)
                if (current.page == "invite") {
                    Text("Participar", color = Color(0xFF26215C), fontWeight = FontWeight.SemiBold, fontSize = 16.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)).background(Color.White).clickable { contribTarget = current.parentId }.padding(vertical = 15.dp))
                } else if (current.page == "story" && !isCollab) {
                    if (entry.mine) {
                        Text("💬  Ver comentários", color = Color.White, fontSize = 15.sp,
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(26.dp)).background(Color(0x55000000)).border(1.dp, Color(0x44FFFFFF), RoundedCornerShape(26.dp)).clickable { showComments = true }.padding(horizontal = 20.dp, vertical = 13.dp))
                    } else {
                        QinblyStoryReplyBar(current.id, false, accent, Color(0x66000000), Color(0xCCFFFFFF), onNotice,
                            onSent = { focusManager.clearFocus() }, onFocus = { replying = it })
                    }
                }
                val showViews = entry.mine && current.page == "story"
                val canDelete = (entry.mine && current.page != "invite") || (current.page == "contrib" && current.authorMine)
                if (showViews || canDelete) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        if (showViews) Row(
                            Modifier.clip(RoundedCornerShape(20.dp)).background(Color(0x66000000)).clickable {
                                showViewers = true
                                viewers = null
                                if (!token.isNullOrBlank()) Thread {
                                    val main = Handler(Looper.getMainLooper())
                                    try { val list = QinblyApi.getStoryViewers(token, current.id); main.post { viewers = list } }
                                    catch (e: Exception) { main.post { viewers = emptyList(); onNotice(e.message ?: "Não foi possível ver quem viu.") } }
                                }.start()
                            }.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Outlined.Visibility, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(current.views.toString(), color = Color.White, fontWeight = FontWeight.SemiBold)
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

    if (showComments) QinblyStoryCommentsDialog(current.id, entry.mine, accent, QinblyApi.isCollabCaption(current.caption), onNotice) { showComments = false }

    if (showViewers) {
        val list = viewers
        val none = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
        QinblyDialog(onDismissRequest = { showViewers = false }, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false), lift = false) {
            Box(Modifier.fillMaxSize().background(Color(0x99000000)).clickable(indication = null, interactionSource = none) { showViewers = false }) {
                Column(
                    Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                        .padding(start = 8.dp, end = 8.dp, top = 64.dp, bottom = qinblyNavBarPadding() + 8.dp)
                        .clip(RoundedCornerShape(32.dp)).background(Color(0xFF1E1E20))
                        .clickable(indication = null, interactionSource = none) { }
                        .padding(top = 12.dp, bottom = 10.dp)
                ) {
                    Box(Modifier.align(Alignment.CenterHorizontally).size(width = 40.dp, height = 5.dp).clip(CircleShape).background(Color(0x55FFFFFF)))
                    Row(Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Visibility, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(10.dp))
                        Text("Visualizações", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.weight(1f))
                        if (list != null) Text(list.size.toString(), color = Color(0xFFC9B8FF), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                    if (list == null) Box(Modifier.fillMaxWidth().padding(vertical = 28.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = accent, modifier = Modifier.size(28.dp)) }
                    else if (list.isEmpty()) Text("Ninguém viu ainda.", color = Color(0xFFA9B5C8), fontSize = 15.sp, modifier = Modifier.padding(horizontal = 22.dp, vertical = 20.dp))
                    else LazyColumn(Modifier.heightIn(max = 440.dp)) {
                        items(list, key = { it.username }) { v ->
                            Row(Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(46.dp).clip(CircleShape).background(accent), contentAlignment = Alignment.Center) {
                                    Text((v.displayName.ifBlank { v.username }).take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                    if (v.avatarUrl.isNotBlank()) AsyncImage(model = v.avatarUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                                }
                                Spacer(Modifier.width(14.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(v.displayName.ifBlank { v.username }, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text("@" + v.username, color = Color(0xFFA9B5C8), fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                                Text(qinblyAgo(v.viewedAt), color = Color(0xFF8E87A8), fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
