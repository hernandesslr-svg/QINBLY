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
private val qinblyTextFonts = listOf("Aa" to FontFamily.Default, "Aa" to FontFamily.Serif, "Aa" to FontFamily.Monospace, "Aa" to FontFamily.Default)
private val qinblyFontNames = listOf("Moderno", "Clássico", "Máquina", "Negrito")
private fun qinblyFontWeight(i: Int) = if (i == 3) FontWeight.Bold else FontWeight.Medium

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
                fontFamily = qinblyTextFonts[s.font.coerceIn(0, qinblyTextFonts.lastIndex)].second, fontWeight = qinblyFontWeight(s.font.coerceIn(0, qinblyTextFonts.lastIndex)),
                textAlign = alignOf(s.align), modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
internal fun QinblyTextStoryComposer(onDismiss: () -> Unit, onPublished: () -> Unit, onNotice: (String) -> Unit, collab: Boolean = false, contributeTo: String? = null) {
    val onNotice: (String) -> Unit = qinblyToast(onNotice)
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("qinbly_session", Context.MODE_PRIVATE) }
    var text by remember { mutableStateOf("") }
    var bg by remember { mutableStateOf(0) }
    var font by remember { mutableStateOf(0) }
    var align by remember { mutableStateOf(1) }
    var showEmojis by remember { mutableStateOf(false) }
    var showFonts by remember { mutableStateOf(false) }
    var showBgs by remember { mutableStateOf(true) }
    var confirmExit by remember { mutableStateOf(false) }
    var confirmSend by remember { mutableStateOf(false) }
    var sending by remember { mutableStateOf(false) }
    val focus = remember { androidx.compose.ui.focus.FocusRequester() }
    LaunchedEffect(Unit) { try { focus.requestFocus() } catch (_: Exception) { } }
    fun tryExit() { if (text.isBlank()) onDismiss() else confirmExit = true }
    BackHandler { tryExit() }
    fun doPublish() {
        val token = prefs.getString("token", null)
        val uid = QinblyApi.currentUserId(prefs)
        if (text.isBlank()) { onNotice("Escreva alguma coisa."); return }
        if (token.isNullOrBlank() || uid.isNullOrBlank()) { onNotice("Entre na sua conta."); return }
        sending = true
        Thread {
            val main = Handler(Looper.getMainLooper())
            try {
                if (contributeTo != null) {
                    QinblyApi.addContribution(token, contributeTo, null, "text", qinblyEncodeTextStory(QinblyTextStory(bg, font, align, text.trim(), false)))
                    main.post { onNotice("Resposta enviada."); onPublished() }
                } else {
                    QinblyApi.createTextStory(token, uid, qinblyEncodeTextStory(QinblyTextStory(bg, font, align, text.trim(), collab)))
                    main.post { onNotice("Story publicado. Fica no ar por 24 horas."); onPublished() }
                }
            } catch (e: Exception) { main.post { sending = false; onNotice(e.message ?: "Não foi possível publicar.") } }
        }.start()
    }
    QinblyDialog(onDismissRequest = { tryExit() }, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false, dismissOnBackPress = false), lift = false) {
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(qinblyTextBgs[bg]))) {
            Row(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(40.dp).clip(CircleShape).background(Color(0x33000000)).clickable { tryExit() }, contentAlignment = Alignment.Center) { Text("✕", color = Color.White, fontSize = 18.sp) }
                Spacer(Modifier.weight(1f))
                Text(if (sending) "Publicando…" else if (contributeTo != null) "Enviar" else "Publicar", color = Color(0xFF26215C), fontWeight = FontWeight.SemiBold, fontSize = 14.sp,
                    modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(Color.White).clickable(enabled = !sending) {
                        if (text.isBlank()) onNotice("Escreva alguma coisa.")
                        else if (contributeTo != null) confirmSend = true else doPublish()
                    }.padding(horizontal = 18.dp, vertical = 9.dp))
            }
            Column(Modifier.align(Alignment.CenterEnd).padding(end = 10.dp, bottom = 120.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                @Composable fun Tool(on: Boolean, onClick: () -> Unit, content: @Composable () -> Unit) {
                    Box(Modifier.size(42.dp).clip(CircleShape).background(if (on) Color.White else Color(0x44000000)).clickable { onClick() }, contentAlignment = Alignment.Center) { content() }
                }
                Tool(showFonts, { showFonts = !showFonts; if (showFonts) showEmojis = false }) { Text("Aa", color = if (showFonts) Color(0xFF26215C) else Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp) }
                Tool(showBgs, { showBgs = !showBgs }) { Text("🎨", fontSize = 18.sp) }
                Tool(false, { align = (align + 1) % 3 }) { Text(when (align) { 0 -> "⇤"; 2 -> "⇥"; else -> "↔" }, color = Color.White, fontSize = 18.sp) }
                Tool(showEmojis, { showEmojis = !showEmojis; if (showEmojis) showFonts = false }) { Text("😊", fontSize = 18.sp) }
            }
            Box(Modifier.fillMaxSize().statusBarsPadding().padding(top = 60.dp, bottom = 190.dp).padding(start = 24.dp, end = 64.dp), contentAlignment = Alignment.Center) {
                Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                    val size = textSizeFor(text.length)
                    BasicTextField(
                        value = text, onValueChange = { text = it },
                        textStyle = TextStyle(color = Color.White, fontSize = size.sp, lineHeight = (size * 1.3f).sp, fontFamily = qinblyTextFonts[font].second, fontWeight = qinblyFontWeight(font), textAlign = alignOf(align)),
                        cursorBrush = SolidColor(Color.White), modifier = Modifier.fillMaxWidth().focusRequester(focus),
                        decorationBox = { inner ->
                            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                if (text.isEmpty()) Text(if (contributeTo != null) "Escreva sua resposta…" else if (collab) "Qual é o tema? Ex.: mostre seu café de hoje" else "Escreva aqui…", color = Color(0xAAFFFFFF), fontSize = 24.sp, textAlign = TextAlign.Center)
                                inner()
                            }
                        }
                    )
                }
            }
            Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth().imePadding().padding(bottom = if (showEmojis) 0.dp else 12.dp + qinblyNavBarPadding()), verticalArrangement = Arrangement.spacedBy(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                if (showFonts) Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    qinblyFontNames.forEachIndexed { i, name ->
                        Text(name, color = if (font == i) Color(0xFF26215C) else Color.White, fontFamily = qinblyTextFonts[i].second, fontWeight = qinblyFontWeight(i), fontSize = 14.sp,
                            modifier = Modifier.clip(RoundedCornerShape(16.dp)).background(if (font == i) Color.White else Color(0x44000000)).clickable { font = i }.padding(horizontal = 14.dp, vertical = 8.dp))
                    }
                }
                if (showBgs) Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    qinblyTextBgs.forEachIndexed { i, c ->
                        Box(Modifier.size(26.dp).clip(CircleShape).background(c[0]).border(if (bg == i) 3.dp else 1.5.dp, Color.White, CircleShape).clickable { bg = i })
                    }
                }
                if (showEmojis) QinblyEmojiPanel(Modifier.padding(bottom = qinblyNavBarPadding()), Color(0xEE1E1830)) { e -> text += e }
            }
        }
    }
    if (confirmExit) {
        AlertDialog(
            onDismissRequest = { confirmExit = false },
            title = { Text(if (contributeTo != null) "Descartar a resposta?" else "Descartar o story?") },
            text = { Text("O texto que você escreveu vai ser perdido.") },
            confirmButton = { TextButton(onClick = { confirmExit = false; onDismiss() }) { Text("Descartar") } },
            dismissButton = { TextButton(onClick = { confirmExit = false }) { Text("Continuar escrevendo") } }
        )
    }
    if (confirmSend) {
        AlertDialog(
            onDismissRequest = { confirmSend = false },
            title = { Text("Enviar resposta?") },
            text = { Text("Sua resposta aparece para todos que veem este story.") },
            confirmButton = { TextButton(onClick = { confirmSend = false; doPublish() }) { Text("Enviar") } },
            dismissButton = { TextButton(onClick = { confirmSend = false }) { Text("Cancelar") } }
        )
    }
}

/** Comentários de um story (folha que sobe de baixo, campo em pílula). O autor vê todos; quem comentou vê só os próprios. */
@Composable
internal fun QinblyStoryCommentsDialog(storyId: String, isOwner: Boolean, accent: Color, collab: Boolean, onNotice: (String) -> Unit, onDismiss: () -> Unit) {
    val onNotice: (String) -> Unit = qinblyToast(onNotice)
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("qinbly_session", Context.MODE_PRIVATE) }
    val token = remember { prefs.getString("token", null) }
    val uid = remember { QinblyApi.currentUserId(prefs) }
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
    val onNotice: (String) -> Unit = qinblyToast(onNotice)
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("qinbly_session", Context.MODE_PRIVATE) }
    val token = remember { prefs.getString("token", null) }
    val uid = remember { QinblyApi.currentUserId(prefs) }
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
