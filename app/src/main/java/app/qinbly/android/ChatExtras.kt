package app.qinbly.android

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/** Documento enviado numa conversa: ícone, nome e "Toque para abrir" (abre pelo navegador/visualizador do aparelho). */
@Composable
internal fun QinblyFileBubble(url: String, name: String, accent: Color, textColor: Color) {
    val context = LocalContext.current
    Row(
        Modifier.widthIn(max = 240.dp).clip(RoundedCornerShape(12.dp)).clickable {
            try { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) } catch (_: Exception) { }
        }.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(accent), contentAlignment = Alignment.Center) {
            Icon(Icons.Outlined.Description, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
        }
        Column {
            Text(name, color = textColor, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text("Toque para abrir", color = textColor.copy(alpha = 0.6f), fontSize = 11.sp)
        }
    }
}

/** Enquete numa conversa: pergunta, opções com barra de porcentagem e contagem; tocar numa opção vota (ou muda o voto). */
@Composable
internal fun QinblyChatPollBubble(poll: QinblyApi.ChatPoll, accent: Color, textColor: Color, onVote: (Int) -> Unit) {
    val total = poll.votes.sum()
    Column(Modifier.widthIn(min = 200.dp, max = 260.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(poll.question, color = textColor, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        poll.options.forEachIndexed { i, option ->
            val count = poll.votes.getOrElse(i) { 0 }
            val share = if (total > 0) count.toFloat() / total else 0f
            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(textColor.copy(alpha = 0.08f)).clickable { onVote(i) }) {
                Box(Modifier.fillMaxWidth(share.coerceIn(0f, 1f)).height(38.dp).background(accent.copy(alpha = 0.30f)))
                Row(Modifier.fillMaxWidth().heightIn(min = 38.dp).padding(horizontal = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text((if (poll.myVote == i) "✓ " else "") + option, color = textColor, fontSize = 14.sp, modifier = Modifier.weight(1f))
                    Text(count.toString(), color = textColor.copy(alpha = 0.7f), fontSize = 12.sp)
                }
            }
        }
        Text(total.toString() + if (total == 1) " voto" else " votos", color = textColor.copy(alpha = 0.6f), fontSize = 11.sp)
    }
}

/** Janela para montar a enquete: pergunta e de 2 a 6 opções. */
@Composable
internal fun QinblyPollCreateDialog(accent: Color, onDismiss: () -> Unit, onCreate: (String, List<String>) -> Unit) {
    var question by remember { mutableStateOf("") }
    val options = remember { mutableStateListOf("", "") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nova enquete") },
        text = { QinblyClearFocusWhenKeyboardCloses();
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(question, { question = it.take(200) }, label = { Text("Pergunta") })
                options.forEachIndexed { i, _ ->
                    OutlinedTextField(options[i], { options[i] = it.take(80) }, label = { Text("Opção " + (i + 1)) }, singleLine = true)
                }
                if (options.size < 6) TextButton(onClick = { options.add("") }) { Text("Adicionar opção", color = accent) }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val cleaned = options.map { it.trim() }.filter { it.isNotEmpty() }
                if (question.isNotBlank() && cleaned.size >= 2) onCreate(question.trim(), cleaned)
            }) { Text("Enviar", color = accent) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

/**
 * Tela cheia "Marcar pessoas": abas Seguindo e Seguidores, busca, check na linha de quem foi marcado (até 10).
 * Devolve os @ escolhidos (sem o "@").
 */
@Composable
internal fun QinblyTagPeopleDialog(token: String?, myUsername: String, initial: List<String>, accent: Color, onDismiss: () -> Unit, onConfirm: (List<String>) -> Unit) {
    var tab by remember { mutableStateOf(0) }
    var query by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var following by remember { mutableStateOf<List<QinblyApi.UserSummary>>(emptyList()) }
    var followers by remember { mutableStateOf<List<QinblyApi.UserSummary>>(emptyList()) }
    var found by remember { mutableStateOf<List<QinblyApi.UserSummary>>(emptyList()) }
    val chosen = remember { mutableStateListOf<String>().apply { addAll(initial) } }
    val limitNotice = remember { mutableStateOf(false) }

    LaunchedEffect(myUsername) {
        val name = myUsername.trim().removePrefix("@")
        if (name.isBlank()) return@LaunchedEffect
        loading = true
        following = try { withContext(Dispatchers.IO) { QinblyApi.getConnections(name, "following") } } catch (_: Exception) { emptyList() }
        followers = try { withContext(Dispatchers.IO) { QinblyApi.getConnections(name, "followers") } } catch (_: Exception) { emptyList() }
        loading = false
    }
    LaunchedEffect(query) {
        val q = query.trim().removePrefix("@")
        if (q.length < 2) {
            found = emptyList()
        } else {
            delay(250)
            found = try { withContext(Dispatchers.IO) { QinblyApi.searchUsers(q, token) } } catch (_: Exception) { emptyList() }
        }
    }

    val q = query.trim().removePrefix("@").lowercase()
    val base = if (tab == 0) following else followers
    val local = if (q.isEmpty()) base else base.filter { it.username.lowercase().contains(q) || it.displayName.lowercase().contains(q) }
    val shown = if (q.length >= 2) (local + found.filter { f -> local.none { it.username.equals(f.username, ignoreCase = true) } }) else local

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Column(Modifier.fillMaxSize().background(QiblyTheme.background).statusBarsPadding().navigationBarsPadding().imePadding()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(36.dp).clip(CircleShape).clickable(onClick = onDismiss), contentAlignment = Alignment.CenterStart) {
                    QinblyCloseIcon(QiblyTheme.text, 22.dp)
                }
                Text("Marcar pessoas", color = QiblyTheme.text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                Text("Pronto", color = accent, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable { onConfirm(chosen.toList()) }.padding(horizontal = 6.dp, vertical = 6.dp))
            }
            Box(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 4.dp)) {
                OutlinedTextField(query, { query = it.take(30) }, modifier = Modifier.fillMaxWidth(), placeholder = { Text("Buscar pessoa") }, singleLine = true)
            }
            if (limitNotice.value) Text("Você pode marcar até 10 pessoas.", color = QiblyTheme.secondary, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
            Row(Modifier.fillMaxWidth().padding(top = 6.dp)) {
                listOf("Seguindo", "Seguidores").forEachIndexed { i, label ->
                    Column(Modifier.weight(1f).clickable { tab = i }, horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(label, color = if (tab == i) QiblyTheme.text else QiblyTheme.secondary, style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (tab == i) FontWeight.SemiBold else FontWeight.Normal, modifier = Modifier.padding(vertical = 10.dp))
                        Box(Modifier.fillMaxWidth().height(2.dp).background(if (tab == i) QiblyTheme.text else Color.Transparent))
                    }
                }
            }
            Box(Modifier.fillMaxWidth().height(0.5.dp).background(QiblyTheme.border))
            if (shown.isEmpty()) {
                Box(Modifier.fillMaxWidth().weight(1f).padding(24.dp), contentAlignment = Alignment.Center) {
                    Text(
                        if (loading) "Carregando…" else if (q.isNotEmpty()) "Ninguém encontrado." else if (tab == 0) "Você ainda não segue ninguém." else "Você ainda não tem seguidores.",
                        color = QiblyTheme.secondary, style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                LazyColumn(Modifier.fillMaxWidth().weight(1f)) {
                    items(shown, key = { it.username.lowercase() }) { user ->
                        val on = chosen.any { it.equals(user.username, ignoreCase = true) }
                        Row(
                            Modifier.fillMaxWidth().clickable {
                                if (on) { chosen.removeAll { it.equals(user.username, ignoreCase = true) }; limitNotice.value = false }
                                else if (chosen.size < 10) chosen.add(user.username)
                                else limitNotice.value = true
                            }.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(Modifier.size(44.dp).clip(CircleShape).background(QiblyTheme.soft), contentAlignment = Alignment.Center) {
                                if (user.avatarUrl.isNotBlank()) {
                                    coil.compose.AsyncImage(model = user.avatarUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = androidx.compose.ui.layout.ContentScale.Crop)
                                } else {
                                    Text((user.displayName.ifBlank { user.username }).take(1).uppercase(), color = QiblyTheme.text, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                }
                            }
                            Column(Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(user.displayName.ifBlank { user.username }, color = QiblyTheme.text, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                                    if (user.verified) QinblyVerifiedBadge(16.dp)
                                }
                                Text("@" + user.username, color = QiblyTheme.secondary, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            if (on) {
                                Box(Modifier.size(24.dp).clip(CircleShape).background(accent), contentAlignment = Alignment.Center) { QinblyCheckIcon(Color.White, 15.dp) }
                            } else {
                                Box(Modifier.size(24.dp).clip(CircleShape).border(1.5.dp, QiblyTheme.border, CircleShape))
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Bolinha de cor livre do fundo do texto. A bolinha é sempre colorida (nunca muda de cor). Segurar e girar o dedo em volta
 * abre uma roda de cores; a cor escolhida aparece ao vivo no painel. Devolve 16777216 + RGB.
 */
@Composable
internal fun QinblyColorWheelDot(selected: Boolean, startColor: Color, onPick: (Int) -> Unit) {
    var holding by remember { mutableStateOf(false) }
    var hue by remember { mutableStateOf(0f) }
    var picked by remember { mutableStateOf(startColor) }
    val ringCenter = remember { floatArrayOf(Float.NaN, Float.NaN) }
    val rainbow = remember { Brush.sweepGradient(listOf(Color.hsv(0f, 0.85f, 0.95f), Color.hsv(60f, 0.85f, 0.95f), Color.hsv(120f, 0.85f, 0.95f), Color.hsv(180f, 0.85f, 0.95f), Color.hsv(240f, 0.85f, 0.95f), Color.hsv(300f, 0.85f, 0.95f), Color.hsv(360f, 0.85f, 0.95f))) }
    Box(
        Modifier.size(40.dp).clip(CircleShape).background(rainbow)
            .border(if (selected) 3.dp else 0.dp, if (selected) QiblyTheme.text else Color.Transparent, CircleShape)
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    down.consume()
                    holding = true
                    var pressed = true
                    while (pressed) {
                        val event = awaitPointerEvent()
                        val p = event.changes.firstOrNull { it.id == down.id } ?: break
                        pressed = p.pressed
                        if (pressed) {
                            val cx = if (ringCenter[0].isNaN()) size.width / 2f else ringCenter[0]
                            val cy = if (ringCenter[1].isNaN()) size.height / 2f else ringCenter[1]
                            val dx = p.position.x - cx
                            val dy = p.position.y - cy
                            if (dx * dx + dy * dy > 20f * 20f) {
                                hue = ((Math.toDegrees(Math.atan2(dy.toDouble(), dx.toDouble())) + 360.0) % 360.0).toFloat()
                                picked = Color.hsv(hue, 0.85f, 0.8f)
                                onPick(16777216 or (picked.toArgb() and 0xFFFFFF))
                            }
                            p.consume()
                        }
                    }
                    holding = false
                    ringCenter[0] = Float.NaN; ringCenter[1] = Float.NaN
                }
            }
    ) {
        if (holding) {
            Popup(popupPositionProvider = object : PopupPositionProvider {
                override fun calculatePosition(anchorBounds: IntRect, windowSize: IntSize, layoutDirection: LayoutDirection, popupContentSize: IntSize): IntOffset {
                    val x = (anchorBounds.center.x - popupContentSize.width / 2).coerceIn(0, (windowSize.width - popupContentSize.width).coerceAtLeast(0))
                    val y = (anchorBounds.center.y - popupContentSize.height / 2).coerceIn(0, (windowSize.height - popupContentSize.height).coerceAtLeast(0))
                    ringCenter[0] = x + popupContentSize.width / 2f - anchorBounds.left
                    ringCenter[1] = y + popupContentSize.height / 2f - anchorBounds.top
                    return IntOffset(x, y)
                }
            }) {
                Box(Modifier.size(190.dp), contentAlignment = Alignment.Center) {
                    Canvas(Modifier.fillMaxSize()) {
                        val stroke = 28.dp.toPx()
                        val r = size.minDimension / 2f - stroke / 2f
                        drawCircle(brush = rainbow, radius = r, style = Stroke(stroke))
                        val a = Math.toRadians(hue.toDouble())
                        val knob = Offset(center.x + (Math.cos(a) * r).toFloat(), center.y + (Math.sin(a) * r).toFloat())
                        drawCircle(Color.White, 16.dp.toPx(), knob)
                        drawCircle(picked, 12.dp.toPx(), knob)
                    }
                    Box(Modifier.size(56.dp).clip(CircleShape).background(picked).border(3.dp, Color.White, CircleShape))
                }
            }
        }
    }
}
