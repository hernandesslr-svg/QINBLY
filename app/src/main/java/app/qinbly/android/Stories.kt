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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
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
    accent: Color, hasStory: Boolean, hasNote: Boolean,
    onDismiss: () -> Unit, onNotice: (String) -> Unit, onChanged: () -> Unit, onViewMine: () -> Unit
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
        "menu" -> AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Criar") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    if (hasStory) MenuRow("Ver meu story", accent) { onDismiss(); onViewMine() }
                    MenuRow("Story com foto ou vídeo da galeria", accent) { galleryPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)) }
                    MenuRow("Story com uma foto tirada agora", accent) {
                        if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED) launchCamera()
                        else cameraPermission.launch(android.Manifest.permission.CAMERA)
                    }
                    MenuRow(if (hasNote) "Mudar minha nota" else "Escrever uma nota", accent) { step = "note" }
                    if (hasNote) MenuRow("Apagar minha nota", accent) {
                        val token = prefs.getString("token", null)
                        if (token.isNullOrBlank()) { onNotice("Entre na sua conta."); return@MenuRow }
                        Thread {
                            val main = Handler(Looper.getMainLooper())
                            try { QinblyApi.deleteNote(token); main.post { onNotice("Nota apagada."); onChanged(); onDismiss() } }
                            catch (e: Exception) { main.post { onNotice(e.message ?: "Não foi possível apagar a nota.") } }
                        }.start()
                    }
                }
            },
            confirmButton = { TextButton(onClick = onDismiss) { Text("Fechar") } }
        )
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
private fun StoryVideo(url: String, onEnded: () -> Unit) {
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
internal fun QinblyStoryViewer(entry: QinblyApi.TrayEntry, accent: Color, onNotice: (String) -> Unit, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("qinbly_session", Context.MODE_PRIVATE) }
    val token = remember { prefs.getString("token", null) }
    val items = remember(entry.username) { mutableStateListOf<QinblyApi.StoryItem>().apply { addAll(entry.stories) } }
    var index by remember(entry.username) { mutableIntStateOf(0) }
    var showViewers by remember { mutableStateOf(false) }
    var viewers by remember { mutableStateOf<List<QinblyApi.StoryViewer>?>(null) }
    if (items.isEmpty()) { onDismiss(); return }
    val current = items[index.coerceIn(0, items.lastIndex)]
    val progress = remember(current.id) { Animatable(0f) }

    fun next() { if (index < items.lastIndex) index++ else onDismiss() }
    fun previous() { if (index > 0) index-- }

    LaunchedEffect(current.id) {
        if (!entry.mine && !token.isNullOrBlank()) {
            Thread { try { QinblyApi.markStoryViewed(token, current.id) } catch (_: Exception) { } }.start()
        }
    }
    LaunchedEffect(current.id, showViewers) {
        if (showViewers) { progress.stop(); return@LaunchedEffect }
        val total = if (current.mediaType == "video") 20000 else 6000
        val remaining = ((1f - progress.value) * total).toInt().coerceAtLeast(1)
        progress.animateTo(1f, tween(durationMillis = remaining, easing = LinearEasing))
        next()
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        Box(
            Modifier.fillMaxSize().background(Color.Black).pointerInput(current.id) {
                detectTapGestures { offset -> if (offset.x < size.width / 3f) previous() else next() }
            }
        ) {
            if (current.mediaType == "video") StoryVideo(current.mediaUrl) { next() }
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
                    Box(Modifier.size(34.dp).clip(CircleShape).background(Color(0xFF2B3544))) {
                        if (entry.avatarUrl.isNotBlank()) AsyncImage(model = entry.avatarUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(if (entry.mine) "Seu story" else entry.displayName.ifBlank { entry.username }, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp,
                        maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                    Icon(Icons.Default.Close, contentDescription = "Fechar", tint = Color.White, modifier = Modifier.size(28.dp).clickable { onDismiss() })
                }
            }

            Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth().navigationBarsPadding().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (current.caption.isNotBlank()) {
                    Text(current.caption, color = Color.White, fontSize = 16.sp, modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(Color(0x88000000)).padding(horizontal = 12.dp, vertical = 8.dp))
                }
                if (entry.mine) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(
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
                        Icon(Icons.Outlined.Delete, contentDescription = "Excluir story", tint = Color.White,
                            modifier = Modifier.size(28.dp).clickable {
                                val id = current.id
                                if (!token.isNullOrBlank()) Thread { try { QinblyApi.deleteStory(token, id) } catch (_: Exception) { } }.start()
                                items.removeAll { it.id == id }
                                if (items.isEmpty()) onDismiss() else if (index > items.lastIndex) index = items.lastIndex
                            })
                    }
                }
            }
        }
    }

    if (showViewers) {
        AlertDialog(
            onDismissRequest = { showViewers = false },
            title = { Text("Quem viu") },
            text = {
                val list = viewers
                if (list == null) CircularProgressIndicator(color = accent, modifier = Modifier.size(26.dp))
                else if (list.isEmpty()) Text("Ninguém viu ainda.")
                else LazyColumn(Modifier.heightIn(max = 300.dp)) {
                    items(list, key = { it.username }) { v ->
                        Column(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                            Text(v.displayName.ifBlank { v.username }, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text("@" + v.username, fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showViewers = false }) { Text("Fechar") } }
        )
    }
}
