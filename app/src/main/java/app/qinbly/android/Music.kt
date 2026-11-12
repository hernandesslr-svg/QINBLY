package app.qinbly.android

import android.media.MediaPlayer
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/** Música de um story: trecho de 30 s do catálogo público do iTunes. Vai dentro da legenda ("\n♪§título§artista§url"). */
internal data class QinblyMusic(val title: String, val artist: String, val url: String)

private fun clean(s: String) = s.replace("§", "").replace("\n", " ").trim()

internal fun qinblyAttachMusic(caption: String, m: QinblyMusic?): String =
    if (m == null) caption else caption + "\n♪§" + clean(m.title) + "§" + clean(m.artist) + "§" + m.url

/** Separa a legenda da música anexada. */
internal fun qinblySplitMusic(caption: String): Pair<String, QinblyMusic?> {
    val i = caption.indexOf("\n♪§")
    if (i < 0) return caption to null
    val parts = caption.substring(i + 1).split("§")
    val text = caption.substring(0, i)
    return if (parts.size >= 4 && parts[3].startsWith("http")) text to QinblyMusic(parts[1], parts[2], parts[3]) else text to null
}

internal fun qinblySearchMusic(query: String): List<QinblyMusic> {
    val url = URL("https://itunes.apple.com/search?term=" + URLEncoder.encode(query, "UTF-8") + "&media=music&entity=song&limit=25&country=BR")
    val conn = (url.openConnection() as HttpURLConnection).apply { connectTimeout = 12000; readTimeout = 12000 }
    val text = try { conn.inputStream.bufferedReader().use { it.readText() } } finally { conn.disconnect() }
    val arr = JSONObject(text).optJSONArray("results") ?: return emptyList()
    return (0 until arr.length()).mapNotNull { i ->
        val o = arr.optJSONObject(i) ?: return@mapNotNull null
        val p = o.optString("previewUrl")
        if (p.isBlank()) null else QinblyMusic(o.optString("trackName"), o.optString("artistName"), p)
    }
}

/** Linha "♪ título · artista" usada embaixo do nome e sobre as prévias. */
@Composable
internal fun QinblyMusicLine(m: QinblyMusic, modifier: Modifier = Modifier, color: Color = Color(0xE6FFFFFF)) {
    Text("♪  " + m.title + " · " + m.artist, color = color, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = modifier)
}

/** Toca a música em repetição enquanto estiver na tela. */
@Composable
internal fun QinblyMusicPlayer(url: String, muted: Boolean, paused: Boolean = false) {
    val holder = remember(url) { arrayOfNulls<MediaPlayer>(1) }
    val ready = remember(url) { booleanArrayOf(false) }
    DisposableEffect(url) {
        val mp = MediaPlayer()
        holder[0] = mp
        try {
            mp.setDataSource(url)
            mp.isLooping = true
            mp.setOnPreparedListener { ready[0] = true; if (!paused) it.start() }
            mp.prepareAsync()
        } catch (_: Exception) { }
        onDispose { holder[0] = null; ready[0] = false; try { mp.release() } catch (_: Exception) { } }
    }
    LaunchedEffect(muted, paused, url) {
        val mp = holder[0] ?: return@LaunchedEffect
        try {
            val v = if (muted) 0f else 1f
            mp.setVolume(v, v)
            if (ready[0]) { if (paused) { if (mp.isPlaying) mp.pause() } else if (!mp.isPlaying) mp.start() }
        } catch (_: Exception) { }
    }
}

/** Folha para buscar e escolher uma música; o ícone de play toca um trecho. */
@Composable
internal fun QinblyMusicPicker(onPick: (QinblyMusic) -> Unit, onDismiss: () -> Unit) {
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<QinblyMusic>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    var playing by remember { mutableStateOf("") }
    LaunchedEffect(query) {
        if (query.isBlank()) { results = emptyList(); error = ""; return@LaunchedEffect }
        kotlinx.coroutines.delay(450)
        loading = true; error = ""
        try { results = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) { qinblySearchMusic(query.trim()) } }
        catch (_: Exception) { results = emptyList(); error = "Não foi possível buscar agora. Verifique a internet." }
        loading = false
    }
    val nav = qinblyNavBarPadding()
    QinblyDialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false), lift = false) {
        Box(Modifier.fillMaxSize().background(Color(0x99000000)).clickable(indication = null, interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }) { onDismiss() }) {
            Column(
                Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(top = 80.dp).clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)).background(Color(0xFF1E1830))
                    .clickable(indication = null, interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }) { }
                    .imePadding().padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = nav + 8.dp)
            ) {
                Box(Modifier.align(Alignment.CenterHorizontally).size(width = 44.dp, height = 5.dp).clip(CircleShape).background(Color(0x55FFFFFF)))
                Text("Adicionar música", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.padding(top = 12.dp, bottom = 10.dp))
                Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Color(0xFF2B2342)).padding(horizontal = 16.dp, vertical = 12.dp)) {
                    if (query.isEmpty()) Text("Buscar música ou artista", color = Color(0xFF8E87A8), fontSize = 15.sp)
                    BasicTextField(query, { query = it.take(60) }, singleLine = true, textStyle = TextStyle(color = Color.White, fontSize = 15.sp), cursorBrush = SolidColor(Color.White), modifier = Modifier.fillMaxWidth())
                }
                if (loading) Text("Buscando…", color = Color(0xFFA9B5C8), modifier = Modifier.padding(16.dp))
                else if (error.isNotBlank()) Text(error, color = Color(0xFFA9B5C8), modifier = Modifier.padding(16.dp))
                else if (query.isBlank()) Text("Digite o nome da música ou do artista. Toque no play pra ouvir um trecho e na linha pra escolher.", color = Color(0xFFA9B5C8), fontSize = 14.sp, modifier = Modifier.padding(16.dp))
                else if (results.isEmpty()) Text("Nenhuma música encontrada.", color = Color(0xFFA9B5C8), modifier = Modifier.padding(16.dp))
                else LazyColumn(Modifier.fillMaxWidth().padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    items(results, key = { it.url }) { m ->
                        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).clickable { playing = ""; onPick(m) }.padding(vertical = 8.dp, horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(42.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFF3A3260)), contentAlignment = Alignment.Center) { Text("♪", color = Color(0xFFC9B8FF), fontSize = 20.sp) }
                            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                                Text(m.title, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(m.artist, color = Color(0xFFA9B5C8), fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            Box(Modifier.size(40.dp).clip(CircleShape).background(Color(0xFF2B2342)).clickable { playing = if (playing == m.url) "" else m.url }, contentAlignment = Alignment.Center) {
                                Text(if (playing == m.url) "❚❚" else "▶", color = Color.White, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        }
    }
    if (playing.isNotBlank()) QinblyMusicPlayer(playing, muted = false)
}
