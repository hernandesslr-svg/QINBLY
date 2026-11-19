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
import androidx.compose.foundation.layout.widthIn
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

/**
 * Músicas livres do Qinbly: faixas CC0 (domínio público, sem direitos a pagar nem crédito obrigatório) do FreePD.com,
 * cortadas em 30 s e guardadas dentro do app (assets/music). Lista e origem em MUSICAS-LIVRES.md.
 * A "url" delas é qinbly://music/<arquivo>; o player lê direto dos assets.
 */
internal const val QINBLY_MUSIC_SCHEME = "qinbly://music/"
private fun free(slug: String, title: String) = QinblyMusic(title, "Música livre", QINBLY_MUSIC_SCHEME + slug)
internal val qinblyFreeTracks: List<Pair<String, List<QinblyMusic>>> = listOf(
    "Animadas" to listOf(
        free("funshine", "Funshine"), free("funky_energy_loop", "Funky Energy Loop"), free("groovin", "Groovin"),
        free("happy_whistling_ukulele", "Happy Whistling Ukulele"), free("hippety_hop", "Hippety Hop"), free("be_jammin", "Be Jammin"),
        free("bollywood_groove", "Bollywood Groove"), free("city_sunshine", "City Sunshine"), free("celebration", "Celebration"), free("energizing", "Energizing")
    ),
    "Festa e viagem" to listOf(
        free("downtown_boogie", "Downtown Boogie"), free("pina_colada", "Pina Colada"), free("sunny_rasta", "Sunny Rasta"),
        free("take_the_ride", "Take the Ride"), free("vintage_party", "Vintage Party")
    ),
    "Calmas" to listOf(
        free("martini_sunset", "Martini Sunset"), free("kalimba_relaxation_music", "Kalimba Relaxation Music"), free("study_and_relax", "Study and Relax"),
        free("nostalgic_piano", "Nostalgic Piano"), free("lovely_piano_song", "Lovely Piano Song"), free("sunday_dub", "Sunday Dub"),
        free("meditating_beat", "Meditating Beat"), free("relaxing_ballad", "Relaxing Ballad"), free("romantic_inspiration", "Romantic Inspiration")
    ),
    "Inspiradoras" to listOf(
        free("elevate_inspirate", "Elevate Inspirate"), free("horizon_flare", "Horizon Flare"), free("shining_stars", "Shining Stars"),
        free("hopeful", "Hopeful"), free("inspiration", "Inspiration"), free("journey_of_hope", "Journey of Hope")
    )
)

private fun clean(s: String) = s.replace("§", "").replace("\n", " ").trim()

/** Música do post (se tiver), para reanexar à legenda ao editar. */
internal fun QinblyApi.PostSummary.music(): QinblyMusic? = if (musicUrl.isBlank()) null else QinblyMusic(musicTitle, musicArtist, musicUrl)

internal fun qinblyAttachMusic(caption: String, m: QinblyMusic?): String =
    if (m == null) caption else caption + "\n♪§" + clean(m.title) + "§" + clean(m.artist) + "§" + m.url

/** Separa a legenda da música anexada. */
internal fun qinblySplitMusic(caption: String): Pair<String, QinblyMusic?> {
    val i = caption.indexOf("\n♪§")
    if (i < 0) return caption to null
    val parts = caption.substring(i + 1).split("§")
    val text = caption.substring(0, i)
    return if (parts.size >= 4 && (parts[3].startsWith("http") || parts[3].startsWith(QINBLY_MUSIC_SCHEME))) text to QinblyMusic(parts[1], parts[2], parts[3]) else text to null
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
    Row(modifier, verticalAlignment = Alignment.CenterVertically) { QinblyIconOf(QinblyIcon.MusicNote, color, 14.dp); Text(" " + m.title + " · " + m.artist, color = color, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) }
}

/** Toca a música em repetição enquanto estiver na tela. */
@Composable
internal fun QinblyMusicPlayer(url: String, muted: Boolean, paused: Boolean = false) {
    val appContext = androidx.compose.ui.platform.LocalContext.current.applicationContext
    val holder = remember(url) { arrayOfNulls<MediaPlayer>(1) }
    val ready = remember(url) { booleanArrayOf(false) }
    DisposableEffect(url) {
        val mp = MediaPlayer()
        holder[0] = mp
        try {
            if (url.startsWith(QINBLY_MUSIC_SCHEME)) {
                appContext.assets.openFd("music/" + url.removePrefix(QINBLY_MUSIC_SCHEME) + ".mp3").use { mp.setDataSource(it.fileDescriptor, it.startOffset, it.length) }
            } else mp.setDataSource(url)
            mp.isLooping = true
            mp.setOnPreparedListener { ready[0] = true; if (!paused) it.start() }
            mp.prepareAsync()
        } catch (_: Exception) { }
        val unreg = QinblyPlayback.add(
            { val w = try { ready[0] && mp.isPlaying } catch (_: Exception) { false }; try { if (w) mp.pause() } catch (_: Exception) { }; w },
            { try { if (ready[0]) mp.start() } catch (_: Exception) { } }
        )
        onDispose { unreg(); holder[0] = null; ready[0] = false; try { mp.release() } catch (_: Exception) { } }
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

/** Só uma música de post toca por vez: guarda qual post está tocando. */
internal object QinblyFeedMusic { var playingId by mutableStateOf("") }

/** Faixa "♪ música" de um post (feed e visualizador): toque para ouvir/pausar. Para quando o post sai da tela. */
@Composable
internal fun QinblyPostMusicChip(post: QinblyApi.PostSummary, modifier: Modifier = Modifier, color: Color = Color(0xE6FFFFFF)) {
    if (post.musicUrl.isBlank()) return
    val on = QinblyFeedMusic.playingId == post.id
    DisposableEffect(post.id) { onDispose { if (QinblyFeedMusic.playingId == post.id) QinblyFeedMusic.playingId = "" } }
    Row(
        modifier.clip(CircleShape).background(Color(0x33808080)).clickable { QinblyFeedMusic.playingId = if (on) "" else post.id }
            .padding(start = 10.dp, end = 14.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        QinblyIconOf(if (on) QinblyIcon.Pause else QinblyIcon.PlayArrow, color, 18.dp)
        Text("  " + post.musicTitle + " · " + post.musicArtist, color = color, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.widthIn(max = 240.dp))
    }
    if (on) QinblyMusicPlayer(post.musicUrl, muted = false)
}

@Composable
private fun QinblyMusicRow(m: QinblyMusic, playing: String, onToggle: () -> Unit, onPick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).clickable { onPick() }.padding(vertical = 8.dp, horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(42.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFF3A3260)), contentAlignment = Alignment.Center) { QinblyIconOf(QinblyIcon.MusicNote, Color(0xFF7FA6F2), 22.dp) }
        Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
            Text(m.title, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(m.artist, color = Color(0xFFF2F4F8), fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Box(Modifier.size(40.dp).clip(CircleShape).background(Color(0xFF262628)).clickable { onToggle() }, contentAlignment = Alignment.Center) {
            QinblyIconOf(if (playing == m.url) QinblyIcon.Pause else QinblyIcon.PlayArrow, Color.White, 20.dp)
        }
    }
}

/**
 * Folha para escolher uma música; o ícone de play toca um trecho.
 * Mostra primeiro as músicas livres do Qinbly. Com freeOnly (posts), a busca só filtra essas músicas;
 * sem freeOnly (story), a busca procura também no catálogo do iTunes.
 */
@Composable
internal fun QinblyMusicPicker(onPick: (QinblyMusic) -> Unit, onDismiss: () -> Unit, freeOnly: Boolean = false) {
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<QinblyMusic>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    var playing by remember { mutableStateOf("") }
    LaunchedEffect(query) {
        if (freeOnly || query.isBlank()) { results = emptyList(); error = ""; return@LaunchedEffect }
        kotlinx.coroutines.delay(450)
        loading = true; error = ""
        try { results = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) { qinblySearchMusic(query.trim()) } }
        catch (_: Exception) { results = emptyList(); error = "Não foi possível buscar agora. Verifique a internet." }
        loading = false
    }
    val q = query.trim()
    val freeMatches = remember(q) { if (q.isBlank()) emptyList() else qinblyFreeTracks.flatMap { it.second }.filter { it.title.contains(q, ignoreCase = true) } }
    QinblyFullScreen("Adicionar música", onDismiss, scroll = false) {
        Box(Modifier.fillMaxWidth().padding(horizontal = 16.dp).clip(RoundedCornerShape(24.dp)).background(Color(0xFF262628)).padding(horizontal = 16.dp, vertical = 12.dp)) {
            if (query.isEmpty()) Text(if (freeOnly) "Buscar nas músicas livres" else "Buscar música ou artista", color = Color(0xFF8E87A8), fontSize = 15.sp)
            BasicTextField(query, { query = it.take(60) }, singleLine = true, textStyle = TextStyle(color = Color.White, fontSize = 15.sp), cursorBrush = SolidColor(Color.White), modifier = Modifier.fillMaxWidth())
        }
        val listModifier = Modifier.weight(1f).fillMaxWidth().padding(top = 8.dp, start = 16.dp, end = 16.dp)
        if (q.isBlank()) {
            LazyColumn(listModifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                item { Text("Músicas livres do Qinbly. Toque no play pra ouvir e na linha pra escolher.", color = Color(0xFFF2F4F8), fontSize = 13.sp, modifier = Modifier.padding(vertical = 8.dp)) }
                qinblyFreeTracks.forEach { (group, tracks) ->
                    item(key = "h-$group") { Text(group, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)) }
                    items(tracks, key = { it.url }) { m -> QinblyMusicRow(m, playing, { playing = if (playing == m.url) "" else m.url }) { playing = ""; onPick(m) } }
                }
            }
        } else if (freeOnly) {
            if (freeMatches.isEmpty()) Text("Nenhuma música encontrada.", color = Color(0xFFF2F4F8), modifier = Modifier.padding(16.dp))
            else LazyColumn(listModifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                items(freeMatches, key = { it.url }) { m -> QinblyMusicRow(m, playing, { playing = if (playing == m.url) "" else m.url }) { playing = ""; onPick(m) } }
            }
        } else if (loading && freeMatches.isEmpty()) Text("Buscando…", color = Color(0xFFF2F4F8), modifier = Modifier.padding(16.dp))
        else if (error.isNotBlank() && freeMatches.isEmpty()) Text(error, color = Color(0xFFF2F4F8), modifier = Modifier.padding(16.dp))
        else if (results.isEmpty() && freeMatches.isEmpty()) Text("Nenhuma música encontrada.", color = Color(0xFFF2F4F8), modifier = Modifier.padding(16.dp))
        else LazyColumn(listModifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
            items(freeMatches + results, key = { it.url }) { m -> QinblyMusicRow(m, playing, { playing = if (playing == m.url) "" else m.url }) { playing = ""; onPick(m) } }
        }
    }
    if (playing.isNotBlank()) QinblyMusicPlayer(playing, muted = false)
}
