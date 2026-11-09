package app.qinbly.android

import android.media.MediaPlayer
import android.media.MediaRecorder
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.File
import kotlin.math.max

/** Grava a mensagem de voz num arquivo .m4a (AAC) na pasta de cache. */
internal class QinblyVoiceRecorder(private val context: android.content.Context) {
    private var recorder: MediaRecorder? = null
    val file: File = File(context.cacheDir, "qinbly-voz-${System.currentTimeMillis()}.m4a")

    fun start(): Boolean = try {
        val r = if (android.os.Build.VERSION.SDK_INT >= 31) MediaRecorder(context) else @Suppress("DEPRECATION") MediaRecorder()
        r.setAudioSource(MediaRecorder.AudioSource.MIC)
        r.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        r.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        r.setAudioEncodingBitRate(64000)
        r.setAudioSamplingRate(44100)
        r.setOutputFile(file.absolutePath)
        r.prepare()
        r.start()
        recorder = r
        true
    } catch (_: Exception) {
        release()
        false
    }

    /** Volume atual de 0 a 1 (para desenhar as ondas). */
    fun level(): Float = try { ((recorder?.maxAmplitude ?: 0) / 14000f).coerceIn(0f, 1f) } catch (_: Exception) { 0f }

    /** Para a gravação. Retorna true se o arquivo ficou válido. */
    fun stop(): Boolean {
        val r = recorder ?: return false
        recorder = null
        return try {
            r.stop(); r.release(); file.exists() && file.length() > 0
        } catch (_: Exception) {
            try { r.release() } catch (_: Exception) { }
            false
        }
    }

    fun release() {
        try { recorder?.release() } catch (_: Exception) { }
        recorder = null
    }

    fun discard() { release(); file.delete() }
}

/**
 * Barra de gravação (no lugar da barra de texto): lixeira, ponto vermelho, tempo, ondas e seta de enviar.
 * Começa a gravar assim que aparece. [onSend] recebe o arquivo e a duração em segundos.
 */
@Composable
internal fun QinblyVoiceBar(
    accent: Color,
    textColor: Color,
    barColor: Color,
    onCancel: () -> Unit,
    onTooShort: () -> Unit,
    onFailed: () -> Unit,
    onSend: (File, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val recorder = remember { QinblyVoiceRecorder(context) }
    var seconds by remember { mutableIntStateOf(0) }
    val levels = remember { mutableStateListOf<Float>().apply { repeat(28) { add(0.05f) } } }
    var started by remember { mutableStateOf(false) }
    var done by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        started = recorder.start()
        if (!started) { onFailed(); return@LaunchedEffect }
        var ticks = 0
        while (!done) {
            kotlinx.coroutines.delay(100)
            ticks++
            if (levels.size >= 28) levels.removeAt(0)
            levels.add(max(0.05f, recorder.level()))
            if (ticks % 10 == 0) seconds = ticks / 10
            if (ticks >= 3000 && !done) { // limite de 5 minutos
                done = true
                if (recorder.stop()) onSend(recorder.file, seconds.coerceAtLeast(1)) else onFailed()
            }
        }
    }
    DisposableEffect(Unit) { onDispose { if (!done) recorder.discard() } }

    Row(
        modifier.fillMaxWidth().height(58.dp).clip(RoundedCornerShape(29.dp)).background(barColor).padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(Modifier.size(44.dp).clip(CircleShape).clickable { done = true; recorder.discard(); onCancel() }, contentAlignment = Alignment.Center) {
            Icon(Icons.Outlined.Delete, contentDescription = "Cancelar gravação", tint = textColor.copy(alpha = 0.7f), modifier = Modifier.size(28.dp))
        }
        Box(Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFE24B4A)))
        Text("%d:%02d".format(seconds / 60, seconds % 60), color = textColor, fontSize = 16.sp)
        Canvas(Modifier.weight(1f).height(34.dp)) {
            val n = levels.size
            val gap = size.width / n
            val barW = gap * 0.45f
            levels.forEachIndexed { i, l ->
                val h = size.height * (0.15f + 0.85f * l)
                drawRoundRect(
                    textColor.copy(alpha = 0.45f),
                    Offset(i * gap + (gap - barW) / 2f, (size.height - h) / 2f),
                    Size(barW, h),
                    CornerRadius(barW / 2f, barW / 2f)
                )
            }
        }
        Box(
            Modifier.size(50.dp).clip(CircleShape).background(accent).clickable {
                if (done || !started) return@clickable
                if (seconds < 1) { done = true; recorder.discard(); onTooShort(); return@clickable }
                done = true
                if (recorder.stop()) onSend(recorder.file, seconds.coerceAtLeast(1)) else { recorder.discard(); onFailed() }
            },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Send, contentDescription = "Enviar mensagem de voz", tint = Color.White, modifier = Modifier.size(25.dp))
        }
    }
}

/** Só uma mensagem de voz toca por vez. */
private object QinblyVoicePlayback {
    var stopCurrent: (() -> Unit)? = null
}

/** Balão de mensagem de voz: play/pausa, ondas com progresso e duração. */
@Composable
internal fun QinblyAudioBubble(url: String, seconds: Int, accent: Color, own: Boolean, textColor: Color) {
    var playing by remember(url) { mutableStateOf(false) }
    var progress by remember(url) { mutableFloatStateOf(0f) }
    var failed by remember(url) { mutableStateOf(false) }
    val player = remember(url) { arrayOfNulls<MediaPlayer>(1) }
    val bars = remember(url) {
        val seed = url.hashCode()
        List(26) { i -> 0.25f + 0.75f * (((seed ushr (i % 24)) xor (i * 7919)) and 0xFF) / 255f }
    }

    fun stop() {
        try { player[0]?.let { if (it.isPlaying) it.pause() } } catch (_: Exception) { }
        playing = false
    }

    LaunchedEffect(playing) {
        while (playing) {
            val p = player[0]
            val dur = try { p?.duration ?: 0 } catch (_: Exception) { 0 }
            if (p != null && dur > 0) progress = (try { p.currentPosition } catch (_: Exception) { 0 }).toFloat() / dur
            kotlinx.coroutines.delay(150)
        }
    }
    DisposableEffect(url) {
        onDispose {
            try { player[0]?.release() } catch (_: Exception) { }
            player[0] = null
            if (QinblyVoicePlayback.stopCurrent != null && playing) QinblyVoicePlayback.stopCurrent = null
        }
    }

    val circleColor = if (own) Color.White else accent
    val iconColor = if (own) accent else Color.White
    val activeBar = if (own) Color.White else accent
    val idleBar = if (own) Color.White.copy(alpha = 0.45f) else textColor.copy(alpha = 0.3f)

    Row(Modifier.width(230.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(
            Modifier.size(38.dp).clip(CircleShape).background(circleColor).clickable {
                if (playing) { stop(); return@clickable }
                QinblyVoicePlayback.stopCurrent?.invoke()
                try {
                    var p = player[0]
                    if (p == null) {
                        p = MediaPlayer()
                        p.setDataSource(url)
                        p.setOnCompletionListener { playing = false; progress = 0f }
                        p.setOnErrorListener { _, _, _ -> playing = false; failed = true; true }
                        p.setOnPreparedListener { it.start(); playing = true }
                        player[0] = p
                        p.prepareAsync()
                    } else {
                        p.start(); playing = true
                    }
                    QinblyVoicePlayback.stopCurrent = { stop() }
                } catch (_: Exception) { failed = true }
            },
            contentAlignment = Alignment.Center
        ) {
            Icon(if (playing) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = if (playing) "Pausar" else "Ouvir", tint = iconColor, modifier = Modifier.size(26.dp))
        }
        Canvas(Modifier.weight(1f).height(30.dp)) {
            val n = bars.size
            val gap = size.width / n
            val barW = gap * 0.5f
            bars.forEachIndexed { i, b ->
                val h = size.height * b
                drawRoundRect(
                    if (i.toFloat() / n < progress) activeBar else idleBar,
                    Offset(i * gap + (gap - barW) / 2f, (size.height - h) / 2f),
                    Size(barW, h),
                    CornerRadius(barW / 2f, barW / 2f)
                )
            }
        }
        Text(if (failed) "erro" else "%d:%02d".format(seconds / 60, seconds % 60), color = textColor.copy(alpha = 0.85f), fontSize = 12.sp, modifier = Modifier.padding(end = 2.dp))
    }
}
