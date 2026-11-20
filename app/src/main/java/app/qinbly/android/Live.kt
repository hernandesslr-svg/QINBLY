package app.qinbly.android

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import io.livekit.android.LiveKit
import io.livekit.android.events.RoomEvent
import io.livekit.android.renderer.TextureViewRenderer
import io.livekit.android.room.Room
import io.livekit.android.room.track.LocalVideoTrack
import io.livekit.android.room.track.Track
import io.livekit.android.room.track.VideoTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/** Lives de verdade (LiveKit). O servidor que libera a entrada fica em functions/api/live.js (Cloudflare Pages). */
internal object QinblyLiveNet {
    const val API = "https://qinbly.pages.dev/api/live"

    class Access(val url: String, val token: String, val room: String)

    private fun call(method: String, action: String, token: String, body: JSONObject?): JSONObject? {
        val c = (URL("$API?action=$action").openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 15000
            readTimeout = 20000
            setRequestProperty("Authorization", "Bearer $token")
            setRequestProperty("Accept", "application/json")
            if (body != null) {
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
            }
        }
        try {
            if (body != null) c.outputStream.use { it.write(body.toString().toByteArray()) }
            val text = (if (c.responseCode in 200..299) c.inputStream else c.errorStream)?.bufferedReader()?.use { it.readText() }.orEmpty()
            return try { JSONObject(text).also { it.put("_status", c.responseCode) } } catch (_: Exception) { null }
        } finally {
            c.disconnect()
        }
    }

    /** Pede a permissão de entrada. Devolve o motivo em texto quando não der. */
    suspend fun access(userToken: String, username: String, host: Boolean, room: String?): Pair<Access?, String> = withContext(Dispatchers.IO) {
        try {
            val body = JSONObject().put("username", username).put("host", host)
            if (room != null) body.put("room", room)
            val r = call("POST", "token", userToken, body) ?: return@withContext null to "O servidor de lives não respondeu."
            if (r.optInt("_status") == 200 && r.has("token")) Access(r.getString("url"), r.getString("token"), r.optString("room")) to ""
            else null to r.optString("error", "Não foi possível entrar na live.")
        } catch (_: Exception) {
            null to "Sem conexão com o servidor de lives."
        }
    }

    suspend fun list(userToken: String): List<QinblyLive>? = withContext(Dispatchers.IO) {
        try {
            val r = call("GET", "list", userToken, null) ?: return@withContext null
            if (r.optInt("_status") != 200) return@withContext null
            val a = r.optJSONArray("lives") ?: return@withContext emptyList()
            (0 until a.length()).map {
                val o = a.getJSONObject(it)
                QinblyLive(o.optString("id"), o.optString("host"), o.optInt("viewers"), 0, o.optString("host"))
            }
        } catch (_: Exception) { null }
    }

    fun userToken(context: Context): String =
        context.getSharedPreferences("qinbly_session", Context.MODE_PRIVATE).byAccount().getString("token", "").orEmpty()
}

private fun attachVideo(room: Room, renderer: TextureViewRenderer, track: VideoTrack?) {
    if (track == null) return
    try { track.addRenderer(renderer) } catch (_: Exception) { }
}

/**
 * Quem transmite: liga câmera e microfone na sala do próprio usuário e mostra a imagem em tela cheia.
 * [paused] desliga câmera e microfone sem sair da sala. [front] vira a câmera. [onViewers] recebe quantas pessoas assistem.
 */
@Composable
internal fun QinblyLiveHostStream(
    paused: Boolean, front: Boolean, micOn: Boolean, username: String, modifier: Modifier,
    onViewers: (Int) -> Unit, onError: (String) -> Unit
) {
    val context = LocalContext.current
    val room = remember { LiveKit.create(context.applicationContext) }
    val renderer = remember { TextureViewRenderer(context) }
    var started by remember { mutableStateOf(false) }
    DisposableEffect(Unit) {
        room.initVideoRenderer(renderer)
        onDispose { try { room.disconnect(); room.release() } catch (_: Exception) { } }
    }
    LaunchedEffect(Unit) {
        // Dá um instante para a prévia antiga soltar a câmera.
        kotlinx.coroutines.delay(500)
        val (access, why) = QinblyLiveNet.access(QinblyLiveNet.userToken(context), username, true, null)
        if (access == null) { onError(why); return@LaunchedEffect }
        try {
            room.connect(access.url, access.token)
            room.localParticipant.setMicrophoneEnabled(micOn)
            room.localParticipant.setCameraEnabled(true)
            attachVideo(room, renderer, room.localParticipant.getTrackPublication(Track.Source.CAMERA)?.track as? VideoTrack)
            started = true
            onViewers(room.remoteParticipants.size)
            room.events.events.collect { e ->
                if (e is RoomEvent.ParticipantConnected || e is RoomEvent.ParticipantDisconnected) onViewers(room.remoteParticipants.size)
                if (e is RoomEvent.Disconnected) onError("A transmissão caiu. Verifique a conexão.")
            }
        } catch (e: Exception) {
            onError("Não foi possível começar a live (" + (e.message ?: "erro").take(60) + ").")
        }
    }
    LaunchedEffect(paused, micOn, started) {
        if (!started) return@LaunchedEffect
        try {
            room.localParticipant.setCameraEnabled(!paused)
            room.localParticipant.setMicrophoneEnabled(!paused && micOn)
            if (!paused) attachVideo(room, renderer, room.localParticipant.getTrackPublication(Track.Source.CAMERA)?.track as? VideoTrack)
        } catch (_: Exception) { }
    }
    val firstFront = remember { booleanArrayOf(true) }
    LaunchedEffect(front) {
        if (firstFront[0]) { firstFront[0] = false; return@LaunchedEffect }
        try { (room.localParticipant.getTrackPublication(Track.Source.CAMERA)?.track as? LocalVideoTrack)?.switchCamera() } catch (_: Exception) { }
    }
    AndroidView(factory = { renderer }, modifier = modifier)
}

/** Quem assiste: entra na sala do anfitrião e mostra o vídeo dele. [onEnded] quando a live acaba ou cai. */
@Composable
internal fun QinblyLiveViewerStream(
    roomName: String, username: String, modifier: Modifier,
    onViewers: (Int) -> Unit, onEnded: (String) -> Unit
) {
    val context = LocalContext.current
    val room = remember { LiveKit.create(context.applicationContext) }
    val renderer = remember { TextureViewRenderer(context) }
    DisposableEffect(Unit) {
        room.initVideoRenderer(renderer)
        onDispose { try { room.disconnect(); room.release() } catch (_: Exception) { } }
    }
    LaunchedEffect(roomName) {
        val (access, why) = QinblyLiveNet.access(QinblyLiveNet.userToken(context), username, false, roomName)
        if (access == null) { onEnded(why); return@LaunchedEffect }
        try {
            room.connect(access.url, access.token)
            onViewers(room.remoteParticipants.size)
            room.remoteParticipants.values.forEach { p ->
                attachVideo(room, renderer, p.getTrackPublication(Track.Source.CAMERA)?.track as? VideoTrack)
            }
            room.events.events.collect { e ->
                when (e) {
                    is RoomEvent.TrackSubscribed -> attachVideo(room, renderer, e.track as? VideoTrack)
                    is RoomEvent.ParticipantConnected, is RoomEvent.ParticipantDisconnected -> {
                        onViewers(room.remoteParticipants.size)
                        if (room.remoteParticipants.isEmpty()) onEnded("A live terminou.")
                    }
                    is RoomEvent.Disconnected -> onEnded("A live terminou.")
                    else -> {}
                }
            }
        } catch (e: Exception) {
            onEnded("Não foi possível entrar na live (" + (e.message ?: "erro").take(60) + ").")
        }
    }
    AndroidView(factory = { renderer }, modifier = modifier)
}
