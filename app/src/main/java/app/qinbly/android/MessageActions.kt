package app.qinbly.android

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.layout.layout
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import java.io.File
import java.io.FileOutputStream

private enum class MsgKind { TEXT, STICKER, AUDIO, IMAGE, FILE, POLL }

private val ALL_REACTIONS = listOf("👍", "❤️", "😂", "😮", "😢", "🙏", "🔥", "👏", "😍", "🎉", "🤔", "😡", "🥳", "😎", "💯", "👎", "🤣", "😭", "🙌", "💔")

/** Emojis do mais usado para o menos usado (empate mantém a ordem padrão). */
private fun reactionOrder(prefs: android.content.SharedPreferences): List<String> =
    ALL_REACTIONS.sortedByDescending { prefs.getInt("react_count_$it", 0) }

private fun countReaction(prefs: android.content.SharedPreferences, e: String) {
    prefs.edit().putInt("react_count_$e", prefs.getInt("react_count_$e", 0) + 1).apply()
}

private fun pathOf(url: String) = url.substringAfter("/post-media/")

/** Texto "bruto" da mensagem (com a marca interna) para encaminhar. Enquete não encaminha. */
private fun rawBodyOf(m: QinblyApi.MessageSummary): String? = when {
    m.poll != null -> null
    m.stickerUrl.isNotBlank() -> "qinbly:sticker:" + pathOf(m.stickerUrl)
    m.imageUrl.isNotBlank() -> "qinbly:img:" + pathOf(m.imageUrl)
    m.audioUrl.isNotBlank() -> "qinbly:audio:" + pathOf(m.audioUrl) + ":" + m.audioSeconds
    m.fileUrl.isNotBlank() -> "qinbly:file:" + pathOf(m.fileUrl) + ":" + java.net.URLEncoder.encode(m.fileName.ifBlank { "documento" }, "UTF-8")
    m.body.isNotBlank() -> m.body
    else -> null
}

/** Resumo curto para citar numa resposta. */
internal fun qinblyMessageSnippet(m: QinblyApi.MessageSummary): String = when {
    m.poll != null -> "Enquete"
    m.stickerUrl.isNotBlank() || QinblyStickers.idOf(m.body) != null -> "Figurinha"
    m.imageUrl.isNotBlank() -> "Foto"
    m.audioUrl.isNotBlank() -> "Mensagem de voz"
    m.fileUrl.isNotBlank() -> m.fileName.ifBlank { "Documento" }
    else -> m.body.replace("\n", " ").take(80)
}

/** Menu de uma mensagem (segurar o balão), no estilo do WhatsApp: as opções mudam com o tipo e com quem enviou. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun QinblyMessageSheet(
    message: QinblyApi.MessageSummary, anchor: androidx.compose.ui.geometry.Rect?, isOwn: Boolean, pinned: Boolean, myReaction: String, onReact: (String) -> Unit, accent: Color, bg: Color, textColor: Color,
    onDismiss: () -> Unit, onCanceled: (String) -> Unit, onHide: (String) -> Unit, onReport: (String) -> Unit,
    onReply: (QinblyApi.MessageSummary) -> Unit, onTogglePin: (String) -> Unit, onSavedSticker: () -> Unit, onNotice: (String) -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("qinbly_session", Context.MODE_PRIVATE) }
    var confirmCancel by remember { mutableStateOf(false) }
    var showInfo by remember { mutableStateOf(false) }
    var showMore by remember { mutableStateOf(false) }
    var showForward by remember { mutableStateOf(false) }
    var forwardTo by remember { mutableStateOf("") }
    var showBig by remember { mutableStateOf(false) }
    val kind = when {
        message.poll != null -> MsgKind.POLL
        message.stickerUrl.isNotBlank() || QinblyStickers.idOf(message.body) != null -> MsgKind.STICKER
        message.audioUrl.isNotBlank() -> MsgKind.AUDIO
        message.imageUrl.isNotBlank() -> MsgKind.IMAGE
        message.fileUrl.isNotBlank() -> MsgKind.FILE
        else -> MsgKind.TEXT
    }
    val raw = rawBodyOf(message)
    @Composable
    fun Option(icon: ImageVector, label: String, tint: Color, labelColor: Color = textColor, flip: Boolean = false, onClick: () -> Unit) {
        Row(
            Modifier.fillMaxWidth().clickable { onClick() }.padding(horizontal = 24.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(24.dp).graphicsLayer(scaleX = if (flip) -1f else 1f))
            Text(label, color = labelColor, fontSize = 16.sp)
        }
    }
    fun ensureSticker(favorite: Boolean) {
        val token = prefs.getString("token", null) ?: return
        Thread {
            val main = Handler(Looper.getMainLooper())
            try {
                val path = pathOf(message.stickerUrl)
                var found = QinblyApi.getMyStickers(token).firstOrNull { it.path == path }
                val existed = found != null
                if (found == null) {
                    val tmp = File(context.cacheDir, "saved-sticker-${System.currentTimeMillis()}.png")
                    java.net.URL(message.stickerUrl).openStream().use { i -> FileOutputStream(tmp).use { o -> i.copyTo(o) } }
                    found = try { QinblyApi.createSticker(context, token, tmp, "image/png") } finally { tmp.delete() }
                }
                if (favorite) {
                    val local = context.getSharedPreferences("qinbly_stickers", Context.MODE_PRIVATE)
                    local.edit().putStringSet("favs", (local.getStringSet("favs", emptySet()) ?: emptySet()) + found.id).apply()
                }
                main.post { onNotice(if (favorite) "Figurinha nos favoritos." else if (existed) "Essa figurinha já está nas suas." else "Figurinha salva nas suas figurinhas."); onSavedSticker() }
            } catch (e: Exception) { main.post { onNotice(e.message ?: "Não foi possível salvar a figurinha.") } }
        }.start()
    }
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss, properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        androidx.compose.foundation.layout.BoxWithConstraints(Modifier.fillMaxSize().clickable(indication = null, interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }) { onDismiss() }) {
            val density = androidx.compose.ui.platform.LocalDensity.current
            val screenW = maxWidth
            val screenH = maxHeight
            val navBottomDp = with(density) { WindowInsets.navigationBars.getBottom(density).toDp() }
            val usableH = screenH - maxOf(navBottomDp, 40.dp)
            val cardW = 262.dp
            val barW = 262.dp
            val topDp = with(density) { (anchor?.top ?: 0f).toDp() }
            val bottomDp = with(density) { (anchor?.bottom ?: 0f).toDp() }
            val hasAnchor = anchor != null
            val navPx = WindowInsets.navigationBars.getBottom(density)
            val marginPx = maxOf(navPx, with(density) { 48.dp.roundToPx() }) + with(density) { 12.dp.roundToPx() }
            val topMinPx = with(density) { 70.dp.roundToPx() }
            val sidePx = with(density) { 12.dp.roundToPx() }
            val gapPx = with(density) { 8.dp.roundToPx() }
            val anchorBottom = anchor?.bottom
            Column(
                Modifier.layout { measurable, c ->
                    val p = measurable.measure(c.copy(minHeight = 0, maxHeight = (c.maxHeight - marginPx - topMinPx).coerceAtLeast(0)))
                    layout(c.maxWidth, c.maxHeight) {
                        val x = if (isOwn) c.maxWidth - p.width - sidePx else sidePx
                        val desired = if (anchorBottom != null) anchorBottom.toInt() + gapPx else (c.maxHeight - p.height) / 2
                        val y = desired.coerceAtMost(c.maxHeight - p.height - marginPx).coerceAtLeast(topMinPx)
                        p.place(x, y)
                    }
                }.width(cardW),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
            Row(
                Modifier.width(barW).clip(RoundedCornerShape(30.dp)).background(Color(0xFF1B1727)).horizontalScroll(rememberScrollState()).padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                reactionOrder(prefs).forEach { e ->
                    Text(e, fontSize = 24.sp, modifier = Modifier.clip(androidx.compose.foundation.shape.CircleShape)
                        .background(if (myReaction == e) accent.copy(alpha = 0.35f) else Color.Transparent)
                        .clickable { if (myReaction != e) countReaction(prefs, e); onReact(if (myReaction == e) "" else e); onDismiss() }.padding(horizontal = 6.dp, vertical = 3.dp))
                }
            }
        Column(Modifier.width(cardW).clip(RoundedCornerShape(18.dp)).background(Color(0xFF1B1727)).verticalScroll(rememberScrollState()).padding(vertical = 4.dp), horizontalAlignment = Alignment.Start) {
                Option(Icons.Default.Reply, "Responder", accent) { onReply(message); onDismiss() }
                if (raw != null) Option(Icons.Default.Reply, "Encaminhar", accent, flip = true) { showForward = true }
                if (kind == MsgKind.STICKER && message.stickerUrl.isNotBlank()) {
                    Option(Icons.Default.Star, "Favoritar", Color(0xFFFFD60A)) { onDismiss(); ensureSticker(true) }
                }
                Option(Icons.Default.VisibilityOff, "Apagar para mim", textColor) { onHide(message.id); onDismiss(); onNotice("Mensagem apagada só para você.") }
                if (isOwn) Option(Icons.Default.Delete, "Cancelar envio", Color(0xFFE5484D), Color(0xFFE5484D)) { confirmCancel = true }
                Option(Icons.Default.Info, "Sobre a mensagem", accent) { showInfo = true }
                Option(Icons.Default.PushPin, if (pinned) "Desafixar" else "Fixar no topo", accent) { onTogglePin(message.id); onDismiss() }
                Option(if (showMore) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, "Mais", textColor) { showMore = !showMore }
                if (showMore) {
                    if (kind == MsgKind.TEXT) Option(Icons.Outlined.ContentCopy, "Copiar texto", accent) {
                        (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(ClipData.newPlainText("mensagem", message.body))
                        onNotice("Texto copiado."); onDismiss()
                    }
                    val shareText = when (kind) {
                        MsgKind.TEXT -> message.body; MsgKind.STICKER -> message.stickerUrl; MsgKind.AUDIO -> message.audioUrl
                        MsgKind.IMAGE -> message.imageUrl; MsgKind.FILE -> message.fileUrl; MsgKind.POLL -> ""
                    }
                    val openUrl = when (kind) { MsgKind.AUDIO -> message.audioUrl; MsgKind.IMAGE -> message.imageUrl; MsgKind.FILE -> message.fileUrl; else -> "" }
                    if (openUrl.isNotBlank()) Option(Icons.Default.OpenInNew, when (kind) { MsgKind.AUDIO -> "Abrir áudio"; MsgKind.IMAGE -> "Abrir foto"; else -> "Abrir documento" }, accent) {
                        onDismiss()
                        try { context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(openUrl)).addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)) }
                        catch (_: Exception) { onNotice("Não foi possível abrir.") }
                    }
                    if (shareText.isNotBlank()) Option(Icons.Default.Share, "Compartilhar", accent) {
                        onDismiss()
                        val send = android.content.Intent(android.content.Intent.ACTION_SEND).setType("text/plain").putExtra(android.content.Intent.EXTRA_TEXT, shareText)
                        context.startActivity(android.content.Intent.createChooser(send, "Compartilhar").addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK))
                    }
                    if (kind == MsgKind.STICKER && message.stickerUrl.isNotBlank() && !isOwn) Option(Icons.Default.Star, "Salvar nas minhas figurinhas", Color(0xFFFFD60A)) { onDismiss(); ensureSticker(false) }
                    if (!isOwn) Option(Icons.Default.Flag, "Denunciar mensagem", Color(0xFFE5484D), Color(0xFFE5484D)) { onReport(message.id) }
                }
            }
        }
    }
            }
    if (showBig) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showBig = false }) {
            AsyncImage(model = message.stickerUrl, contentDescription = "Figurinha", modifier = Modifier.fillMaxWidth().padding(16.dp).clickable { showBig = false })
        }
    }
    if (showForward) {
        AlertDialog(
            onDismissRequest = { showForward = false },
            title = { Text("Encaminhar para") },
            text = {
                OutlinedTextField(value = forwardTo, onValueChange = { forwardTo = it.take(40) }, singleLine = true, placeholder = { Text("@usuário") })
            },
            confirmButton = {
                TextButton(onClick = {
                    val token = prefs.getString("token", null)
                    val to = forwardTo.trim().removePrefix("@")
                    val body = raw
                    showForward = false; onDismiss()
                    if (token != null && to.isNotBlank() && body != null) Thread {
                        val main = Handler(Looper.getMainLooper())
                        try { QinblyApi.sendMessage(token, to, body); main.post { onNotice("Mensagem encaminhada para @$to.") } }
                        catch (e: Exception) { main.post { onNotice(e.message ?: "Não foi possível encaminhar.") } }
                    }.start()
                }) { Text("Encaminhar", color = accent) }
            },
            dismissButton = { TextButton(onClick = { showForward = false }) { Text("Cancelar") } }
        )
    }
    if (showInfo) {
        val whenSent = try { java.time.Instant.parse(message.createdAt).atZone(java.time.ZoneId.systemDefault()).format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm")) } catch (_: Exception) { message.createdAt }
        val tipo = when (kind) { MsgKind.TEXT -> "Texto"; MsgKind.STICKER -> "Figurinha"; MsgKind.AUDIO -> "Mensagem de voz"; MsgKind.IMAGE -> "Foto"; MsgKind.FILE -> "Documento"; MsgKind.POLL -> "Enquete" }
        AlertDialog(
            onDismissRequest = { showInfo = false },
            title = { Text("Sobre a mensagem") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    if (kind == MsgKind.STICKER && message.stickerUrl.isNotBlank()) AsyncImage(model = message.stickerUrl, contentDescription = "Figurinha", modifier = Modifier.size(200.dp).padding(bottom = 12.dp))
                    Text("Tipo: $tipo\nEnviada por: ${if (isOwn) "você" else "a outra pessoa"}\nData: $whenSent")
                }
            },
            confirmButton = { TextButton(onClick = { showInfo = false }) { Text("Fechar") } }
        )
    }
    if (confirmCancel) {
        AlertDialog(
            onDismissRequest = { confirmCancel = false },
            title = { Text("Cancelar envio?") },
            text = { Text("A mensagem será apagada para você e para a outra pessoa. Não dá para desfazer.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmCancel = false
                    val token = prefs.getString("token", null)
                    onDismiss()
                    if (token != null) Thread {
                        val main = Handler(Looper.getMainLooper())
                        try { QinblyApi.deleteMessage(token, message.id); main.post { onCanceled(message.id); onNotice("Envio cancelado.") } }
                        catch (e: Exception) { main.post { onNotice(e.message ?: "Não foi possível cancelar o envio.") } }
                    }.start()
                }) { Text("Cancelar envio", color = Color(0xFFE5484D)) }
            },
            dismissButton = { TextButton(onClick = { confirmCancel = false }) { Text("Voltar") } }
        )
    }
}
