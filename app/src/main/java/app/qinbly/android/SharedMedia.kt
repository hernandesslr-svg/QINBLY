package app.qinbly.android

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

/** Tela "Mídia, links e documentos" de uma conversa: tudo que vocês dois compartilharam. */
@Composable
internal fun QinblyChatSharedMedia(
    name: String, messages: List<QinblyApi.MessageSummary>, bg: Color, textColor: Color, secondary: Color, accent: Color, onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val tabs = listOf("Mídia", "Links", "Documentos", "Áudios")
    var tab by remember { mutableStateOf(0) }
    var big by remember { mutableStateOf<String?>(null) }
    val photos = messages.filter { it.imageUrl.isNotBlank() }.asReversed()
    val docs = messages.filter { it.fileUrl.isNotBlank() }.asReversed()
    val audios = messages.filter { it.audioUrl.isNotBlank() }.asReversed()
    val urlRe = remember { Regex("https?://[^\\s]+") }
    val links = messages.filter { it.imageUrl.isBlank() && it.fileUrl.isBlank() && it.audioUrl.isBlank() && it.stickerUrl.isBlank() }
        .flatMap { m -> urlRe.findAll(m.body).map { it.value to m.createdAt }.toList() }.asReversed()
    fun open(url: String) {
        try { context.startActivity(Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) } catch (_: Exception) { }
    }
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss, properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)) {
        Column(Modifier.fillMaxSize().background(bg)) {
            Row(Modifier.fillMaxWidth().padding(top = 28.dp, start = 4.dp, end = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDismiss) { Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = textColor) }
                Column {
                    Text("Mídia, links e documentos", color = textColor, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("Conversa com @${name.removePrefix("@")}", color = secondary, fontSize = 13.sp)
                }
            }
            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                tabs.forEachIndexed { i, t ->
                    Text(
                        t, color = if (tab == i) Color.White else textColor, fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clip(androidx.compose.foundation.shape.RoundedCornerShape(18.dp))
                            .background(if (tab == i) accent else Color(0x22FFFFFF)).clickable { tab = i }.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }
            when (tab) {
                0 -> if (photos.isEmpty()) Empty("Nenhuma foto compartilhada ainda.", secondary) else LazyVerticalGrid(GridCells.Fixed(3), Modifier.fillMaxSize().padding(horizontal = 4.dp), horizontalArrangement = Arrangement.spacedBy(3.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    items(photos) { p -> AsyncImage(model = p.imageUrl, contentDescription = "Foto", contentScale = ContentScale.Crop, modifier = Modifier.aspectRatio(1f).clickable { big = p.imageUrl }) }
                }
                1 -> if (links.isEmpty()) Empty("Nenhum link compartilhado ainda.", secondary) else LazyColumn(Modifier.fillMaxSize()) {
                    items(links) { (u, _) -> Text(u, color = accent, fontSize = 14.sp, maxLines = 2, modifier = Modifier.fillMaxWidth().clickable { open(u) }.padding(horizontal = 20.dp, vertical = 12.dp)) }
                }
                2 -> if (docs.isEmpty()) Empty("Nenhum documento compartilhado ainda.", secondary) else LazyColumn(Modifier.fillMaxSize()) {
                    items(docs) { d -> Text(d.fileName.ifBlank { "Documento" }, color = textColor, fontSize = 15.sp, maxLines = 1, modifier = Modifier.fillMaxWidth().clickable { open(d.fileUrl) }.padding(horizontal = 20.dp, vertical = 14.dp)) }
                }
                else -> if (audios.isEmpty()) Empty("Nenhum áudio compartilhado ainda.", secondary) else LazyColumn(Modifier.fillMaxSize()) {
                    items(audios) { a -> Text("Mensagem de voz · ${a.audioSeconds}s", color = textColor, fontSize = 15.sp, modifier = Modifier.fillMaxWidth().clickable { open(a.audioUrl) }.padding(horizontal = 20.dp, vertical = 14.dp)) }
                }
            }
        }
    }
    big?.let { url ->
        androidx.compose.ui.window.Dialog(onDismissRequest = { big = null }, properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)) {
            Box(Modifier.fillMaxSize().background(Color.Black).clickable { big = null }, contentAlignment = Alignment.Center) {
                AsyncImage(model = url, contentDescription = "Foto", modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun Empty(text: String, color: Color) {
    Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.TopCenter) { Text(text, color = color, fontSize = 15.sp) }
}
