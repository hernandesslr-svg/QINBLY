package app.qinbly.android

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage

internal data class GalleryItem(val uri: Uri, val video: Boolean)

internal fun hasGalleryPermission(context: Context): Boolean {
    fun ok(p: String) = androidx.core.content.ContextCompat.checkSelfPermission(context, p) == android.content.pm.PackageManager.PERMISSION_GRANTED
    return if (Build.VERSION.SDK_INT >= 33) ok(android.Manifest.permission.READ_MEDIA_IMAGES) || ok(android.Manifest.permission.READ_MEDIA_VIDEO) || (Build.VERSION.SDK_INT >= 34 && ok("android.permission.READ_MEDIA_VISUAL_USER_SELECTED"))
    else ok(android.Manifest.permission.READ_EXTERNAL_STORAGE)
}

internal fun loadRecentMedia(context: Context): List<GalleryItem> {
    val out = mutableListOf<GalleryItem>()
    try {
        val idCol = MediaStore.Files.FileColumns._ID
        val typeCol = MediaStore.Files.FileColumns.MEDIA_TYPE
        val image = MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
        val video = MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO
        context.contentResolver.query(
            MediaStore.Files.getContentUri("external"), arrayOf(idCol, typeCol), "$typeCol=? OR $typeCol=?",
            arrayOf(image.toString(), video.toString()), MediaStore.Files.FileColumns.DATE_ADDED + " DESC"
        )?.use { c ->
            while (c.moveToNext() && out.size < 150) {
                val id = c.getLong(0); val isVideo = c.getInt(1) == video
                out.add(GalleryItem(ContentUris.withAppendedId(if (isVideo) MediaStore.Video.Media.EXTERNAL_CONTENT_URI else MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id), isVideo))
            }
        }
    } catch (_: Exception) { }
    return out
}

@Composable
internal fun MediaThumb(uri: Uri, video: Boolean, modifier: Modifier, size: Int = 320) {
    val context = LocalContext.current
    val bmp by produceState<android.graphics.Bitmap?>(null, uri) {
        value = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try { if (Build.VERSION.SDK_INT >= 29) context.contentResolver.loadThumbnail(uri, android.util.Size(size, size), null) else null } catch (_: Exception) { null }
        }
    }
    val b = bmp
    if (b != null) Image(b.asImageBitmap(), contentDescription = null, modifier = modifier, contentScale = ContentScale.Crop)
    else if (!video) AsyncImage(model = uri, contentDescription = null, modifier = modifier, contentScale = ContentScale.Crop)
    else Box(modifier.background(Color(0xFF2B2342)), contentAlignment = Alignment.Center) { Text("▶", color = Color.White, fontSize = 22.sp) }
}

/**
 * "Participar" do story coletivo: abre a galeria com atalhos de Câmera e Texto em cima.
 * Escolher uma foto/vídeo leva à legenda (com emojis) e ao envio.
 */
@Composable
internal fun QinblyContributeFlow(storyId: String, onNotice: (String) -> Unit, onCamera: () -> Unit, onDone: () -> Unit, onDismiss: () -> Unit, newCollab: Boolean = false) {
    val onNotice: (String) -> Unit = qinblyToast(onNotice)
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("qinbly_session", Context.MODE_PRIVATE) }
    var step by remember { mutableStateOf("gallery") }
    var picked by remember { mutableStateOf<GalleryItem?>(null) }
    var granted by remember { mutableStateOf(hasGalleryPermission(context)) }
    var items by remember { mutableStateOf<List<GalleryItem>>(emptyList()) }
    var caption by remember { mutableStateOf("") }
    var showEmojis by remember { mutableStateOf(false) }
    var music by remember { mutableStateOf<QinblyMusic?>(null) }
    var showMusic by remember { mutableStateOf(false) }
    var confirm by remember { mutableStateOf(false) }
    var sending by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { granted = hasGalleryPermission(context) }
    val systemPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) { picked = GalleryItem(uri, (context.contentResolver.getType(uri) ?: "").startsWith("video/")); step = "caption" }
    }
    LaunchedEffect(granted) {
        if (granted) items = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) { loadRecentMedia(context) }
    }
    LaunchedEffect(Unit) {
        if (!granted) permissionLauncher.launch(if (Build.VERSION.SDK_INT >= 33) arrayOf(android.Manifest.permission.READ_MEDIA_IMAGES, android.Manifest.permission.READ_MEDIA_VIDEO) else arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE))
    }

    fun send() {
        val item = picked ?: return
        val token = prefs.getString("token", null)
        if (token.isNullOrBlank()) { onNotice("Entre na sua conta."); return }
        sending = true
        Thread {
            val main = Handler(Looper.getMainLooper())
            try {
                if (newCollab) {
                    QinblyApi.createStory(context, token, item.uri, qinblyAttachMusic(qinblyCollabCaption(caption), music))
                    main.post { onNotice("Story coletivo publicado. Fica no ar por 24 horas."); onDone() }
                } else {
                    val (fileId, isVideo) = QinblyApi.uploadStoryMedia(context, token, item.uri)
                    QinblyApi.addContribution(token, storyId, fileId, if (isVideo) "video" else "image", qinblyAttachMusic(caption, music))
                    main.post { onNotice("Resposta enviada."); onDone() }
                }
            } catch (e: Exception) { main.post { sending = false; onNotice(e.message ?: "Não foi possível enviar.") } }
        }.start()
    }

    if (step == "text") {
        QinblyTextStoryComposer(onDismiss = { step = "gallery" }, onPublished = onDone, onNotice = onNotice, collab = newCollab, contributeTo = if (newCollab) null else storyId)
        return
    }

    QinblyDialog(onDismissRequest = { if (step == "caption" && !sending) step = "gallery" else if (!sending) onDismiss() }, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false), lift = false) {
        Box(Modifier.fillMaxSize().background(Color(0xFF14101F))) {
            if (step == "gallery") {
                Column(Modifier.fillMaxSize().statusBarsPadding()) {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(40.dp).clip(CircleShape).background(Color(0x22FFFFFF)).clickable { onDismiss() }, contentAlignment = Alignment.Center) { Text("✕", color = Color.White, fontSize = 18.sp) }
                        Text("Recentes", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 17.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center, modifier = Modifier.weight(1f).padding(end = 40.dp))
                    }
                    Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        @Composable fun Chip(label: String, onClick: () -> Unit, mod: Modifier) {
                            Box(mod.clip(RoundedCornerShape(22.dp)).background(Color(0xFF3A2D6B)).clickable { onClick() }.padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                                Text(label, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                            }
                        }
                        Chip("📷  Câmera", { onCamera() }, Modifier.weight(1f))
                        Chip("Aa  Texto", { step = "text" }, Modifier.weight(1f))
                    }
                    Spacer(Modifier.size(10.dp))
                    if (!granted) {
                        Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Permita o acesso às suas fotos para escolher daqui.", color = Color(0xFFA9B5C8), fontSize = 15.sp)
                            Text("Permitir acesso", color = Color.White, fontWeight = FontWeight.SemiBold, modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(Color(0xFF7C4DFF))
                                .clickable { permissionLauncher.launch(if (Build.VERSION.SDK_INT >= 33) arrayOf(android.Manifest.permission.READ_MEDIA_IMAGES, android.Manifest.permission.READ_MEDIA_VIDEO) else arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)) }.padding(horizontal = 22.dp, vertical = 11.dp))
                            Text("Escolher pela galeria do celular", color = Color(0xFFC9B8FF), modifier = Modifier.clickable { systemPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)) }.padding(8.dp))
                        }
                    } else if (items.isEmpty()) {
                        Text("Nenhuma foto ou vídeo encontrado.", color = Color(0xFFA9B5C8), modifier = Modifier.padding(24.dp))
                    } else {
                        LazyVerticalGrid(GridCells.Fixed(3), modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(2.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            items(items, key = { it.uri.toString() }) { g ->
                                Box(Modifier.aspectRatio(1f).clickable { picked = g; step = "caption" }) {
                                    MediaThumb(g.uri, g.video, Modifier.fillMaxSize())
                                    if (g.video) Text("▶", color = Color.White, fontSize = 14.sp, modifier = Modifier.align(Alignment.BottomStart).padding(6.dp))
                                }
                            }
                        }
                    }
                }
            } else {
                val item = picked
                Column(Modifier.fillMaxSize().statusBarsPadding().imePadding().padding(bottom = if (showEmojis) 0.dp else qinblyNavBarPadding())) {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(40.dp).clip(CircleShape).background(Color(0x22FFFFFF)).clickable(enabled = !sending) { step = "gallery" }, contentAlignment = Alignment.Center) { Text("←", color = Color.White, fontSize = 18.sp) }
                    }
                    Box(Modifier.weight(1f).fillMaxWidth().padding(horizontal = 14.dp), contentAlignment = Alignment.Center) {
                        if (item != null) MediaThumb(item.uri, item.video, Modifier.fillMaxSize().clip(RoundedCornerShape(18.dp)), size = 900)
                        val m = music
                        if (m != null) Row(Modifier.align(Alignment.TopStart).padding(10.dp).clip(RoundedCornerShape(16.dp)).background(Color(0x99000000)).padding(start = 12.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            QinblyMusicLine(m, Modifier.widthIn(max = 220.dp))
                            Text("✕", color = Color.White, fontSize = 14.sp, modifier = Modifier.clickable { music = null }.padding(10.dp))
                        }
                    }
                    Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(26.dp)).background(Color(0xFF2B2342)).padding(start = 18.dp, end = 8.dp, top = 6.dp, bottom = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.weight(1f).padding(vertical = 8.dp)) {
                                if (caption.isEmpty()) Text("Adicionar legenda…", color = Color(0xFF8E87A8), fontSize = 15.sp)
                                BasicTextField(value = caption, onValueChange = { caption = it.take(300) }, textStyle = TextStyle(color = Color.White, fontSize = 15.sp), cursorBrush = SolidColor(Color.White), maxLines = 3, modifier = Modifier.fillMaxWidth())
                            }
                            Box(Modifier.size(36.dp).clip(CircleShape).background(if (music != null) Color(0x44FFFFFF) else Color.Transparent).clickable { showEmojis = false; showMusic = true }, contentAlignment = Alignment.Center) { Text("🎵", fontSize = 19.sp) }
                            Box(Modifier.size(36.dp).clip(CircleShape).background(if (showEmojis) Color(0x44FFFFFF) else Color.Transparent).clickable { showEmojis = !showEmojis }, contentAlignment = Alignment.Center) { Text("😊", fontSize = 20.sp) }
                        }
                        Text(if (sending) "Enviando…" else if (newCollab) "Publicar story coletivo" else "Enviar ao coletivo", color = Color(0xFF26215C), fontWeight = FontWeight.SemiBold, fontSize = 16.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)).background(Color.White).clickable(enabled = !sending) { confirm = true }.padding(vertical = 15.dp))
                    }
                    if (showEmojis) QinblyEmojiPanel(Modifier.padding(bottom = qinblyNavBarPadding()), Color(0xFF1E1830)) { e -> caption = (caption + e).take(300) }
                }
            }
        }
    }
    if (showMusic) QinblyMusicPicker(onPick = { music = it; showMusic = false }, onDismiss = { showMusic = false })
    if (confirm) {
        AlertDialog(
            onDismissRequest = { confirm = false },
            title = { Text(if (newCollab) "Publicar story coletivo?" else "Enviar resposta?") },
            text = { Text(if (newCollab) "Seus seguidores vão ver e poderão participar." else "Sua resposta aparece para todos que veem este story.") },
            confirmButton = { TextButton(onClick = { confirm = false; send() }) { Text(if (newCollab) "Publicar" else "Enviar") } },
            dismissButton = { TextButton(onClick = { confirm = false }) { Text("Cancelar") } }
        )
    }
}
