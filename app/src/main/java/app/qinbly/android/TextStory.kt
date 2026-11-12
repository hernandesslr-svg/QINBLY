package app.qinbly.android

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/** Story de texto: o texto e o estilo vão dentro da legenda ("qinbly:text:<cor>:<fonte>:<alinhamento>\n<texto>"). */
internal data class QinblyTextStory(val bg: Int, val font: Int, val align: Int, val text: String, val collab: Boolean = false)

internal val qinblyTextBgs: List<List<Color>> = listOf(
    listOf(Color(0xFF7C4DFF), Color(0xFF4A2FB0)), listOf(Color(0xFFE0457B), Color(0xFF8A2A5A)), listOf(Color(0xFF1D9E75), Color(0xFF0F6E56)),
    listOf(Color(0xFFEF9F27), Color(0xFFB45309)), listOf(Color(0xFF378ADD), Color(0xFF185FA5)), listOf(Color(0xFF111111), Color(0xFF2B2B2B)),
    listOf(Color(0xFFD85A30), Color(0xFF993C1D)), listOf(Color(0xFF639922), Color(0xFF3B6D11))
)
private val qinblyTextFonts = listOf("Aa" to FontFamily.Default, "Aa" to FontFamily.Serif, "Aa" to FontFamily.Monospace, "Aa" to FontFamily.Cursive)
private val qinblyTextEmojis = "😀 😂 🥹 😍 🥰 😎 🤔 😢 😭 😡 🥳 🙏 👏 💪 👍 🔥 ✨ ❤️ 💜 💔 🎉 🌅 🌧️ 🌟 🍀 ☕ 🎶 📚 🙌 😴".split(" ")

internal fun qinblyEncodeTextStory(s: QinblyTextStory): String = "qinbly:${if (s.collab) "collab" else "text"}:${s.bg}:${s.font}:${s.align}\n${s.text}"

internal fun qinblyParseTextStory(caption: String): QinblyTextStory? {
    val collab = caption.startsWith("qinbly:collab:")
    if (!collab && !caption.startsWith("qinbly:text:")) return null
    val nl = caption.indexOf('\n')
    val head = (if (nl >= 0) caption.substring(0, nl) else caption).split(":")
    if (head.size < 5) return null
    return QinblyTextStory(head[2].toIntOrNull() ?: 0, head[3].toIntOrNull() ?: 0, head[4].toIntOrNull() ?: 1, if (nl >= 0) caption.substring(nl + 1) else "", collab)
}

private fun textSizeFor(len: Int): Int = when { len < 40 -> 32; len < 100 -> 26; len < 220 -> 21; len < 450 -> 18; len < 900 -> 16; else -> 14 }
private fun alignOf(i: Int) = when (i) { 0 -> TextAlign.Start; 2 -> TextAlign.End; else -> TextAlign.Center }

/** Fundo colorido com o texto; usado no visualizador do story. */
@Composable
internal fun QinblyTextStoryContent(s: QinblyTextStory, modifier: Modifier = Modifier) {
    val colors = qinblyTextBgs[s.bg.coerceIn(0, qinblyTextBgs.lastIndex)]
    Box(modifier.background(Brush.verticalGradient(colors)), contentAlignment = Alignment.Center) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 110.dp), verticalArrangement = Arrangement.Center) {
            Text(s.text, color = Color.White, fontSize = textSizeFor(s.text.length).sp, lineHeight = (textSizeFor(s.text.length) * 1.3f).sp,
                fontFamily = qinblyTextFonts[s.font.coerceIn(0, qinblyTextFonts.lastIndex)].second, fontWeight = FontWeight.Medium,
                textAlign = alignOf(s.align), modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
internal fun QinblyTextStoryComposer(onDismiss: () -> Unit, onPublished: () -> Unit, onNotice: (String) -> Unit, collab: Boolean = false) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("qinbly_session", Context.MODE_PRIVATE) }
    var text by remember { mutableStateOf("") }
    var bg by remember { mutableStateOf(0) }
    var font by remember { mutableStateOf(0) }
    var align by remember { mutableStateOf(1) }
    var showEmojis by remember { mutableStateOf(false) }
    var confirmExit by remember { mutableStateOf(false) }
    var sending by remember { mutableStateOf(false) }
    val focus = remember { androidx.compose.ui.focus.FocusRequester() }
    LaunchedEffect(Unit) { try { focus.requestFocus() } catch (_: Exception) { } }
    fun tryExit() { if (text.isBlank()) onDismiss() else confirmExit = true }
    BackHandler { tryExit() }
    QinblyDialog(onDismissRequest = { tryExit() }, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false, dismissOnBackPress = false), lift = false) {
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(qinblyTextBgs[bg]))) {
            Row(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("✕", color = Color.White, fontSize = 22.sp, modifier = Modifier.clip(CircleShape).clickable { tryExit() }.padding(horizontal = 10.dp, vertical = 4.dp))
                Spacer(Modifier.weight(1f))
                Text(if (sending) "Publicando…" else "Publicar", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp,
                    modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(Color(0x44FFFFFF)).clickable(enabled = !sending) {
                        val token = prefs.getString("token", null)
                        val uid = prefs.getString("user_id", null)
                        if (text.isBlank()) { onNotice("Escreva alguma coisa."); return@clickable }
                        if (token.isNullOrBlank() || uid.isNullOrBlank()) { onNotice("Entre na sua conta."); return@clickable }
                        sending = true
                        Thread {
                            val main = Handler(Looper.getMainLooper())
                            try {
                                QinblyApi.createTextStory(token, uid, qinblyEncodeTextStory(QinblyTextStory(bg, font, align, text.trim(), collab)))
                                main.post { onNotice("Story publicado. Fica no ar por 24 horas."); onPublished() }
                            } catch (e: Exception) { main.post { sending = false; onNotice(e.message ?: "Não foi possível publicar.") } }
                        }.start()
                    }.padding(horizontal = 16.dp, vertical = 8.dp))
            }
            Box(Modifier.fillMaxSize().statusBarsPadding().padding(top = 60.dp, bottom = 150.dp).padding(horizontal = 24.dp), contentAlignment = Alignment.Center) {
                Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                    val size = textSizeFor(text.length)
                    BasicTextField(
                        value = text, onValueChange = { text = it },
                        textStyle = TextStyle(color = Color.White, fontSize = size.sp, lineHeight = (size * 1.3f).sp, fontFamily = qinblyTextFonts[font].second, fontWeight = FontWeight.Medium, textAlign = alignOf(align)),
                        cursorBrush = SolidColor(Color.White), modifier = Modifier.fillMaxWidth().focusRequester(focus),
                        decorationBox = { inner ->
                            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                if (text.isEmpty()) Text(if (collab) "Faça uma pergunta ou lance um desafio…" else "Escreva aqui…", color = Color(0xAAFFFFFF), fontSize = 28.sp, textAlign = TextAlign.Center)
                                inner()
                            }
                        }
                    )
                }
            }
            Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth().imePadding().padding(bottom = 12.dp + qinblyNavBarPadding()), verticalArrangement = Arrangement.spacedBy(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                if (showEmojis) Column(Modifier.padding(horizontal = 14.dp).clip(RoundedCornerShape(16.dp)).background(Color(0x66000000)).padding(8.dp)) {
                    qinblyTextEmojis.chunked(10).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            row.forEach { e -> Text(e, fontSize = 22.sp, modifier = Modifier.clip(CircleShape).clickable { text += e }.padding(4.dp)) }
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    qinblyTextFonts.forEachIndexed { i, f ->
                        Text(f.first, color = Color.White, fontFamily = f.second, fontSize = 16.sp, fontWeight = if (font == i) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(if (font == i) Color(0x55FFFFFF) else Color(0x22FFFFFF)).clickable { font = i }.padding(horizontal = 12.dp, vertical = 6.dp))
                    }
                    Text(when (align) { 0 -> "⇤"; 2 -> "⇥"; else -> "↔" }, color = Color.White, fontSize = 16.sp,
                        modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(Color(0x22FFFFFF)).clickable { align = (align + 1) % 3 }.padding(horizontal = 12.dp, vertical = 6.dp))
                    Text("😊", fontSize = 16.sp, modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(if (showEmojis) Color(0x55FFFFFF) else Color(0x22FFFFFF)).clickable { showEmojis = !showEmojis }.padding(horizontal = 12.dp, vertical = 6.dp))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    qinblyTextBgs.forEachIndexed { i, c ->
                        Box(Modifier.size(28.dp).clip(CircleShape).background(c[0]).border(if (bg == i) 3.dp else 1.dp, Color.White, CircleShape).clickable { bg = i })
                    }
                }
            }
        }
    }
    if (confirmExit) {
        AlertDialog(
            onDismissRequest = { confirmExit = false },
            title = { Text("Descartar o story?") },
            text = { Text("O texto que você escreveu vai ser perdido.") },
            confirmButton = { TextButton(onClick = { confirmExit = false; onDismiss() }) { Text("Descartar") } },
            dismissButton = { TextButton(onClick = { confirmExit = false }) { Text("Continuar escrevendo") } }
        )
    }
}

/** Comentários de um story (folha que sobe de baixo, campo em pílula). O autor vê todos; quem comentou vê só os próprios. */
@Composable
internal fun QinblyStoryCommentsDialog(storyId: String, isOwner: Boolean, accent: Color, collab: Boolean, onNotice: (String) -> Unit, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("qinbly_session", Context.MODE_PRIVATE) }
    val token = remember { prefs.getString("token", null) }
    val uid = remember { prefs.getString("user_id", null) }
    var comments by remember { mutableStateOf<List<QinblyApi.StoryComment>?>(null) }
    var refresh by remember { mutableStateOf(0) }
    LaunchedEffect(refresh) {
        if (token.isNullOrBlank()) { comments = emptyList(); return@LaunchedEffect }
        comments = try { kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) { QinblyApi.getStoryComments(token, storyId) } } catch (_: Exception) { comments ?: emptyList() }
    }
    val player = remember { arrayOfNulls<android.media.MediaPlayer>(1) }
    androidx.compose.runtime.DisposableEffect(Unit) { onDispose { try { player[0]?.release() } catch (_: Exception) { } } }
    fun playVoice(commentId: String) {
        if (token.isNullOrBlank()) return
        Thread {
            val main = Handler(Looper.getMainLooper())
            try {
                val arr = org.json.JSONArray(QinblyApi.rest(token, "GET", "story_comments?id=eq.$commentId&select=audio_b64"))
                val b64 = arr.getJSONObject(0).optString("audio_b64")
                val f = java.io.File(context.cacheDir, "sc_play.m4a"); f.writeBytes(android.util.Base64.decode(b64, android.util.Base64.DEFAULT))
                main.post {
                    try {
                        player[0]?.release()
                        val p = android.media.MediaPlayer(); p.setDataSource(f.absolutePath); p.prepare(); p.start()
                        p.setOnCompletionListener { it.release(); if (player[0] === it) player[0] = null }
                        player[0] = p
                    } catch (_: Exception) { onNotice("Não foi possível tocar o áudio.") }
                }
            } catch (_: Exception) { main.post { onNotice("Não foi possível carregar o áudio.") } }
        }.start()
    }
    val sheet = Color(0xFF1E1830)
    QinblyDialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false), lift = false) {
        Box(Modifier.fillMaxSize().clickable(indication = null, interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }) { onDismiss() }) {
            Column(
                Modifier.align(Alignment.BottomCenter).fillMaxWidth().clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)).background(sheet)
                    .clickable(indication = null, interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }) {}
                    .imePadding().padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 10.dp + qinblyNavBarPadding())
            ) {
                Box(Modifier.align(Alignment.CenterHorizontally).width(40.dp).size(width = 40.dp, height = 4.dp).clip(CircleShape).background(Color(0x55FFFFFF)))
                Spacer(Modifier.size(12.dp))
                Text(if (collab) "Contribuições" else if (isOwner) "Comentários" else "Comentar no story", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 17.sp)
                Spacer(Modifier.size(8.dp))
                val list = comments?.filter { (it.kind == "contrib") == collab }
                if (list == null) Text("Carregando…", color = Color(0xFFA9B5C8), modifier = Modifier.padding(vertical = 12.dp))
                else if (list.isEmpty()) Text(if (isOwner) "Ninguém comentou ainda." else if (collab) "Seja o primeiro a contribuir." else "Só quem postou o story vê o seu comentário.", color = Color(0xFFA9B5C8), modifier = Modifier.padding(vertical = 12.dp))
                else LazyColumn(Modifier.heightIn(max = 300.dp)) {
                    items(list, key = { it.id }) { c ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(Modifier.size(34.dp).clip(CircleShape).background(accent), contentAlignment = Alignment.Center) {
                                Text((c.displayName.ifBlank { c.username }).take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Column(Modifier.clip(RoundedCornerShape(18.dp)).background(Color(0xFF2B2342)).padding(horizontal = 14.dp, vertical = 8.dp)) {
                                Text(c.displayName.ifBlank { c.username }, color = Color(0xFFC9B8FF), fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                if (c.body == QINBLY_VOICE) Text("▶  Ouvir mensagem de voz", color = Color.White, fontSize = 15.sp, modifier = Modifier.clickable { playVoice(c.id) }) else Text(c.body, color = Color.White, fontSize = 15.sp)
                            }
                        }
                    }
                }
                Spacer(Modifier.size(10.dp))
                QinblyStoryReplyBar(storyId, collab, accent, Color(0xFF2B2342), Color(0xFF8E87A8), onNotice, onSent = { refresh++ })
            }
        }
    }
}

/** Barra de resposta em pílula (digitar, gravar voz e enviar). Usada direto no story e na folha de comentários. */
@Composable
internal fun QinblyStoryReplyBar(
    storyId: String, collab: Boolean, accent: Color, fieldBg: Color, hintColor: Color,
    onNotice: (String) -> Unit, onSent: () -> Unit = {}, onFocus: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("qinbly_session", Context.MODE_PRIVATE) }
    val token = remember { prefs.getString("token", null) }
    val uid = remember { prefs.getString("user_id", null) }
    var draft by remember { mutableStateOf("") }
    val rec = remember { arrayOfNulls<android.media.MediaRecorder>(1) }
    val recFile = remember { java.io.File(context.cacheDir, "sc_voice.m4a") }
    var recording by remember { mutableStateOf(false) }
    androidx.compose.runtime.DisposableEffect(Unit) { onDispose { try { rec[0]?.release() } catch (_: Exception) { } } }
    fun stopRec(send: Boolean) {
        val r = rec[0] ?: return
        rec[0] = null; recording = false
        try { r.stop() } catch (_: Exception) { }
        try { r.release() } catch (_: Exception) { }
        if (!send || token.isNullOrBlank() || uid.isNullOrBlank() || !recFile.exists() || recFile.length() < 800) return
        Thread {
            val main = Handler(Looper.getMainLooper())
            try {
                val b64 = android.util.Base64.encodeToString(recFile.readBytes(), android.util.Base64.NO_WRAP)
                QinblyApi.addStoryComment(token, storyId, uid, QINBLY_VOICE, if (collab) "contrib" else "comment", b64)
                main.post { onSent() }
            } catch (e: Exception) { main.post { onNotice(e.message ?: "Não foi possível enviar o áudio.") } }
        }.start()
    }
    fun startRec() {
        try {
            val r = if (android.os.Build.VERSION.SDK_INT >= 31) android.media.MediaRecorder(context) else @Suppress("DEPRECATION") android.media.MediaRecorder()
            r.setAudioSource(android.media.MediaRecorder.AudioSource.MIC)
            r.setOutputFormat(android.media.MediaRecorder.OutputFormat.MPEG_4)
            r.setAudioEncoder(android.media.MediaRecorder.AudioEncoder.AAC)
            r.setAudioSamplingRate(16000); r.setAudioEncodingBitRate(24000); r.setMaxDuration(30000)
            r.setOutputFile(recFile.absolutePath)
            r.setOnInfoListener { _, what, _ -> if (what == android.media.MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) stopRec(true) }
            r.prepare(); r.start()
            rec[0] = r; recording = true
        } catch (_: Exception) { rec[0] = null; recording = false; onNotice("Não foi possível gravar o áudio.") }
    }
    val micPermission = androidx.activity.compose.rememberLauncherForActivityResult(androidx.activity.result.contract.ActivityResultContracts.RequestPermission()) { ok ->
        if (ok) startRec() else onNotice("Permita o microfone para gravar.")
    }
    fun send() {
        val body = draft.trim()
        if (body.isBlank() || token.isNullOrBlank() || uid.isNullOrBlank()) return
        draft = ""
        Thread {
            val main = Handler(Looper.getMainLooper())
            try { QinblyApi.addStoryComment(token, storyId, uid, body, if (collab) "contrib" else "comment"); main.post { onSent() } }
            catch (e: Exception) { main.post { onNotice(e.message ?: "Não foi possível comentar.") } }
        }.start()
    }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (recording) {
                        Box(Modifier.weight(1f).clip(RoundedCornerShape(26.dp)).background(Color(0xFF3A2236)).padding(horizontal = 18.dp, vertical = 15.dp)) {
                            Text("🔴  Gravando… (até 30 s)", color = Color.White, fontSize = 15.sp)
                        }
                        Box(Modifier.size(46.dp).clip(CircleShape).background(Color(0xFF3A3256)).clickable { stopRec(false) }, contentAlignment = Alignment.Center) { Text("✕", color = Color.White, fontSize = 17.sp) }
                        Box(Modifier.size(46.dp).clip(CircleShape).background(accent).clickable { stopRec(true) }, contentAlignment = Alignment.Center) { Text("➤", color = Color.White, fontSize = 18.sp) }
                    } else {
                        Box(Modifier.weight(1f).clip(RoundedCornerShape(26.dp)).background(fieldBg).padding(horizontal = 18.dp, vertical = 13.dp)) {
                            if (draft.isEmpty()) Text(if (collab) "Adicionar ao story…" else "Comentar no story…", color = hintColor, fontSize = 15.sp)
                            BasicTextField(value = draft, onValueChange = { draft = it.take(500) }, textStyle = TextStyle(color = Color.White, fontSize = 15.sp),
                                cursorBrush = SolidColor(Color.White), maxLines = 4, modifier = Modifier.fillMaxWidth().onFocusChanged { onFocus(it.isFocused) })
                        }
                        if (draft.isBlank()) Box(Modifier.size(46.dp).clip(CircleShape).background(Color(0xFF3A3256)).clickable {
                            if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) == android.content.pm.PackageManager.PERMISSION_GRANTED) startRec()
                            else micPermission.launch(android.Manifest.permission.RECORD_AUDIO)
                        }, contentAlignment = Alignment.Center) { Text("🎙", fontSize = 18.sp) }
                        else Box(Modifier.size(46.dp).clip(CircleShape).background(accent).clickable { send() }, contentAlignment = Alignment.Center) { Text("➤", color = Color.White, fontSize = 18.sp) }
                    }
                }
}

internal const val QINBLY_VOICE = "🎤 Mensagem de voz"
