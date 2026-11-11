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
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
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

/** Menu de uma mensagem (segurar o balão): as opções mudam conforme o tipo e conforme quem enviou. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun QinblyMessageSheet(
    message: QinblyApi.MessageSummary, isOwn: Boolean, accent: Color, bg: Color, textColor: Color,
    onDismiss: () -> Unit, onCanceled: (String) -> Unit, onHide: (String) -> Unit, onReport: (String) -> Unit, onSavedSticker: () -> Unit, onNotice: (String) -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("qinbly_session", Context.MODE_PRIVATE) }
    var confirmCancel by remember { mutableStateOf(false) }
    var showInfo by remember { mutableStateOf(false) }
    val kind = when {
        message.poll != null -> MsgKind.POLL
        message.stickerUrl.isNotBlank() || QinblyStickers.idOf(message.body) != null -> MsgKind.STICKER
        message.audioUrl.isNotBlank() -> MsgKind.AUDIO
        message.imageUrl.isNotBlank() -> MsgKind.IMAGE
        message.fileUrl.isNotBlank() -> MsgKind.FILE
        else -> MsgKind.TEXT
    }
    @Composable
    fun Option(icon: ImageVector, label: String, tint: Color, labelColor: Color = textColor, onClick: () -> Unit) {
        Row(
            Modifier.fillMaxWidth().clickable { onClick() }.padding(horizontal = 24.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
            Text(label, color = labelColor, fontSize = 16.sp)
        }
    }
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = bg, contentColor = textColor) {
        Column(Modifier.fillMaxWidth().padding(bottom = 28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            when (kind) {
                MsgKind.STICKER -> AsyncImage(model = message.stickerUrl, contentDescription = "Figurinha", modifier = Modifier.size(150.dp).padding(bottom = 10.dp))
                MsgKind.IMAGE -> AsyncImage(model = message.imageUrl, contentDescription = "Foto", modifier = Modifier.size(150.dp).padding(bottom = 10.dp))
                MsgKind.TEXT -> Text(message.body.take(140), color = textColor.copy(alpha = 0.8f), fontSize = 14.sp, maxLines = 3, modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp))
                MsgKind.AUDIO -> Text("Mensagem de voz · ${message.audioSeconds}s", color = textColor.copy(alpha = 0.8f), fontSize = 14.sp, modifier = Modifier.padding(8.dp))
                MsgKind.FILE -> Text(message.fileName.ifBlank { "Documento" }, color = textColor.copy(alpha = 0.8f), fontSize = 14.sp, modifier = Modifier.padding(8.dp))
                MsgKind.POLL -> Text("Enquete", color = textColor.copy(alpha = 0.8f), fontSize = 14.sp, modifier = Modifier.padding(8.dp))
            }
            if (kind == MsgKind.TEXT) Option(Icons.Outlined.ContentCopy, "Copiar texto", accent) {
                (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(ClipData.newPlainText("mensagem", message.body))
                onNotice("Texto copiado."); onDismiss()
            }
            if (kind == MsgKind.STICKER && message.stickerUrl.isNotBlank() && !isOwn) Option(Icons.Default.Star, "Salvar nas minhas figurinhas", Color(0xFFFFD60A)) {
                val token = prefs.getString("token", null)
                onDismiss()
                if (token != null) Thread {
                    val main = Handler(Looper.getMainLooper())
                    try {
                        val tmp = File(context.cacheDir, "saved-sticker-${System.currentTimeMillis()}.png")
                        java.net.URL(message.stickerUrl).openStream().use { i -> FileOutputStream(tmp).use { o -> i.copyTo(o) } }
                        try { QinblyApi.createSticker(context, token, tmp, "image/png") } finally { tmp.delete() }
                        main.post { onNotice("Figurinha salva nas suas figurinhas."); onSavedSticker() }
                    } catch (e: Exception) { main.post { onNotice(e.message ?: "Não foi possível salvar a figurinha.") } }
                }.start()
            }
            val shareText = when (kind) {
                MsgKind.TEXT -> message.body
                MsgKind.STICKER -> message.stickerUrl.ifBlank { message.body }
                MsgKind.AUDIO -> message.audioUrl
                MsgKind.IMAGE -> message.imageUrl
                MsgKind.FILE -> message.fileUrl
                MsgKind.POLL -> ""
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
            Option(Icons.Default.Info, "Dados da mensagem", accent) { showInfo = true }
            Option(Icons.Default.VisibilityOff, "Apagar para mim", textColor) { onHide(message.id); onDismiss(); onNotice("Mensagem apagada só para você.") }
            if (isOwn) Option(Icons.Default.Delete, "Cancelar envio", Color(0xFFE5484D), Color(0xFFE5484D)) { confirmCancel = true }
            else Option(Icons.Default.Flag, "Denunciar mensagem", Color(0xFFE5484D), Color(0xFFE5484D)) { onReport(message.id) }
        }
    }
    if (showInfo) {
        val whenSent = try { java.time.Instant.parse(message.createdAt).atZone(java.time.ZoneId.systemDefault()).format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm")) } catch (_: Exception) { message.createdAt }
        val tipo = when (kind) { MsgKind.TEXT -> "Texto"; MsgKind.STICKER -> "Figurinha"; MsgKind.AUDIO -> "Mensagem de voz"; MsgKind.IMAGE -> "Foto"; MsgKind.FILE -> "Documento"; MsgKind.POLL -> "Enquete" }
        AlertDialog(
            onDismissRequest = { showInfo = false },
            title = { Text("Dados da mensagem") },
            text = { Text("Tipo: $tipo\nEnviada por: ${if (isOwn) "você" else "a outra pessoa"}\nData: $whenSent") },
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
