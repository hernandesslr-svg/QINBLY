package app.qinbly.android

import android.content.Context
import android.os.Handler
import android.os.Looper
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.BusinessCenter
import androidx.compose.material.icons.outlined.Chair
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material.icons.outlined.ChildCare
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.DirectionsRun
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material.icons.outlined.FamilyRestroom
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Flight
import androidx.compose.material.icons.outlined.Kitchen
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.LocalFlorist
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Nightlife
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Park
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material.icons.outlined.SentimentVerySatisfied
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.SportsBasketball
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material.icons.outlined.TwoWheeler
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import org.json.JSONArray
import org.json.JSONObject

private val Panel = Color(0xFF1F1F21)
private val FieldBg = Color(0xFF262628)
private const val STICKER_BASE = "https://jqomgrdlocdqaarfqkay.supabase.co/storage/v1/object/public/post-media/"

private fun sess(c: Context) = c.getSharedPreferences("qinbly_session", Context.MODE_PRIVATE)
private fun main(block: () -> Unit) { Handler(Looper.getMainLooper()).post(block) }


/** Dentro de QinblyDialog o recuo dos botões do celular já foi aplicado na janela inteira. */
internal val LocalQinblyNavHandled = androidx.compose.runtime.compositionLocalOf { false }

/** Altura da barra de botões do celular, medida de três formas (insets do Compose, métricas da janela do app e medida do sistema). */
@Composable
internal fun qinblyNavBarPadding(floor: androidx.compose.ui.unit.Dp = 98.dp): androidx.compose.ui.unit.Dp {
    if (LocalQinblyNavHandled.current) return 0.dp
    val density = androidx.compose.ui.platform.LocalDensity.current
    val context = LocalContext.current
    val fromCompose = with(density) { WindowInsets.navigationBars.getBottom(this).toDp() }
    val measured = remember {
        val res = context.resources
        val d = res.displayMetrics.density
        var activity: Context? = context
        while (activity is android.content.ContextWrapper && activity !is android.app.Activity) activity = activity.baseContext
        var fromWindow = 0f
        if (android.os.Build.VERSION.SDK_INT >= 30 && activity is android.app.Activity) {
            try { fromWindow = activity.windowManager.currentWindowMetrics.windowInsets.getInsetsIgnoringVisibility(android.view.WindowInsets.Type.navigationBars()).bottom / d } catch (_: Exception) { }
        }
        val gesture = try { android.provider.Settings.Secure.getInt(context.contentResolver, "navigation_mode", 0) == 2 } catch (_: Exception) { false }
        val id = res.getIdentifier("navigation_bar_height", "dimen", "android")
        val sys = if (id > 0) res.getDimensionPixelSize(id) / d else 0f
        maxOf(fromWindow, if (gesture) minOf(sys, 24f) else sys)
    }
    // Mínimo fixo: em alguns aparelhos todas as medições chegam zeradas dentro de janelas flutuantes.
    return maxOf(fromCompose, measured.dp, floor)
}

/**
 * Padrão do app: campo de texto só fica "aceso" (borda azul e cursor piscando) com o teclado aberto.
 * Quando o teclado fecha (voltar, descer, tocar fora), a seleção sai do campo. Chamar uma vez dentro de cada janela
 * (tela principal, QinblyDialog e AlertDialog com campo de texto); cada janela tem o próprio teclado e o próprio foco.
 */
@Composable
internal fun QinblyClearFocusWhenKeyboardCloses() {
    val focus = androidx.compose.ui.platform.LocalFocusManager.current
    val open = WindowInsets.ime.getBottom(androidx.compose.ui.platform.LocalDensity.current) > 0
    val wasOpen = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    androidx.compose.runtime.LaunchedEffect(open) {
        if (wasOpen.value && !open) focus.clearFocus()
        wasOpen.value = open
    }
}

/**
 * Padrão de toda conversa: quando o teclado abre ou fecha, a lista de mensagens volta para o fim (última mensagem
 * sempre visível, nunca escondida embaixo). Se a pessoa estiver lendo mensagens bem antigas, não puxa a lista.
 */
@Composable
internal fun QinblyKeepChatAtBottom(state: androidx.compose.foundation.lazy.LazyListState, lastIndex: Int) {
    val open = WindowInsets.ime.getBottom(androidx.compose.ui.platform.LocalDensity.current) > 0
    val last = androidx.compose.runtime.rememberUpdatedState(lastIndex)
    val first = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(true) }
    androidx.compose.runtime.LaunchedEffect(open) {
        if (first.value) { first.value = false; return@LaunchedEffect }
        // O teclado leva uns instantes para terminar de abrir/fechar: confirma a posição algumas vezes.
        repeat(4) {
            val l = last.value
            if (l >= 0) {
                val seen = state.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
                if (seen >= l - 12) state.scrollToItem(l)
            }
            kotlinx.coroutines.delay(120)
        }
    }
}

/**
 * Padrão de todo campo de texto que filtra o que a pessoa digita (nome de usuário etc.): o cursor fica onde a pessoa
 * colocou, mesmo quando um caractere não permitido é descartado. Só a pessoa move o cursor.
 */
internal fun qinblySanitized(new: androidx.compose.ui.text.input.TextFieldValue, sanitize: (String) -> String): androidx.compose.ui.text.input.TextFieldValue {
    val clean = sanitize(new.text)
    // Nada mudou: devolve o valor original, com a palavra que o teclado está montando (composição).
    // Recriar o TextFieldValue apagava essa palavra e o teclado deixava de mostrar a faixa de sugestões.
    if (clean == new.text) return new
    val pos = sanitize(new.text.take(new.selection.end)).length.coerceAtMost(clean.length)
    return androidx.compose.ui.text.input.TextFieldValue(clean, androidx.compose.ui.text.TextRange(pos))
}

/** Espacinho padrão entre qualquer barra de escrever (comentário, mensagem, legenda) e o teclado aberto. */
internal val QinblyKeyboardGap = 6.dp

/**
 * Folga de baixo de uma barra de escrever, igual no app todo: teclado aberto = espacinho fixo colado nele;
 * teclado fechado = acima dos botões do celular. Precisa ser chamada DENTRO da janela da barra (o Dialog recebe o teclado,
 * a tela de trás não), por isso não dá para ler o teclado lá fora e repassar.
 */
@Composable
internal fun qinblyInputBottomPadding(closedExtra: androidx.compose.ui.unit.Dp = 0.dp): androidx.compose.ui.unit.Dp {
    val imeOpen = WindowInsets.ime.getBottom(androidx.compose.ui.platform.LocalDensity.current) > 0
    val nav = qinblyNavBarPadding()
    return if (imeOpen) QinblyKeyboardGap else nav + closedExtra
}

/** Letras menores só dentro do conteúdo informado (usado nas telas de story). */
@Composable
internal fun QinblySmallText(content: @Composable () -> Unit) {
    val base = androidx.compose.ui.platform.LocalDensity.current
    val smaller = androidx.compose.ui.unit.Density(base.density, fontScale = minOf(base.fontScale, 1f) * 0.86f)
    androidx.compose.runtime.CompositionLocalProvider(androidx.compose.ui.platform.LocalDensity provides smaller) { content() }
}

/** Igual ao Dialog do Compose, mas as telas que ocupam a janela inteira terminam sempre acima dos botões do celular. */
@Composable
internal fun QinblyDialog(
    onDismissRequest: () -> Unit,
    properties: androidx.compose.ui.window.DialogProperties = androidx.compose.ui.window.DialogProperties(),
    lift: Boolean = false,
    smallText: Boolean = false,
    content: @Composable () -> Unit
) {
    val pad = if (lift && !properties.decorFitsSystemWindows) qinblyNavBarPadding() else 0.dp
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismissRequest, properties = properties) {
        QinblyClearFocusWhenKeyboardCloses()
        if (lift) androidx.compose.runtime.CompositionLocalProvider(LocalQinblyNavHandled provides true) {
            Box(Modifier.padding(bottom = pad)) { content() }
        } else if (smallText) QinblySmallText { content() } else content()
    }
}

/** Aviso que aparece por cima de qualquer janela (o aviso normal fica escondido atrás das folhas). */
@Composable
internal fun qinblyToast(@Suppress("UNUSED_PARAMETER") original: (String) -> Unit): (String) -> Unit {
    val ctx = androidx.compose.ui.platform.LocalContext.current
    return { m -> android.widget.Toast.makeText(ctx, m, android.widget.Toast.LENGTH_LONG).show() }
}


// ───────── Ícones de verdade no lugar de símbolos/emojis usados como botão ─────────
private fun glyph(v: androidx.compose.ui.graphics.vector.ImageVector, color: Color, size: androidx.compose.ui.unit.Dp, modifier: Modifier): @Composable () -> Unit =
    { androidx.compose.material3.Icon(v, contentDescription = null, tint = color, modifier = modifier.size(size)) }

@Composable internal fun QinblyCloseIcon(color: Color, size: androidx.compose.ui.unit.Dp = 18.dp, modifier: Modifier = Modifier) = glyph(QinblyIcon.Close, color, size, modifier)()
@Composable internal fun QinblyCheckIcon(color: Color, size: androidx.compose.ui.unit.Dp = 18.dp, modifier: Modifier = Modifier) = glyph(QinblyIcon.Check, color, size, modifier)()
@Composable internal fun QinblySendIcon(color: Color, size: androidx.compose.ui.unit.Dp = 20.dp, modifier: Modifier = Modifier) = glyph(QinblyIcon.Send, color, size, modifier)()
@Composable internal fun QinblyBackIcon(color: Color, size: androidx.compose.ui.unit.Dp = 22.dp, modifier: Modifier = Modifier) = glyph(QinblyIcon.ArrowBack, color, size, modifier)()
@Composable internal fun QinblyIconOf(v: androidx.compose.ui.graphics.vector.ImageVector, color: Color, size: androidx.compose.ui.unit.Dp = 20.dp, modifier: Modifier = Modifier) = glyph(v, color, size, modifier)()

/** Tela inteira com seta de voltar no topo (no lugar das "abinhas" que subiam de baixo). */
@Composable
internal fun QinblyFullScreen(
    title: String,
    onDismiss: () -> Unit,
    background: Color = Panel,
    contentColor: Color = Color.White,
    scroll: Boolean = true,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit
) {
    QinblyDialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false, dismissOnClickOutside = false),
        lift = false
    ) {
        val nav = qinblyNavBarPadding()
        val density = androidx.compose.ui.platform.LocalDensity.current
        val imeOpen = androidx.compose.foundation.layout.WindowInsets.ime.getBottom(density) > 0
        Column(Modifier.fillMaxSize().background(background).statusBarsPadding().imePadding()) {
            Row(Modifier.fillMaxWidth().height(56.dp).padding(start = 4.dp, end = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(48.dp).clip(CircleShape).clickable { onDismiss() }, contentAlignment = Alignment.Center) {
                    androidx.compose.foundation.Canvas(Modifier.size(24.dp)) {
                        val w = size.minDimension
                        val path = androidx.compose.ui.graphics.Path().apply {
                            moveTo(w * 0.64f, w * 0.18f); lineTo(w * 0.30f, w * 0.5f); lineTo(w * 0.64f, w * 0.82f)
                        }
                        drawPath(path, contentColor, style = androidx.compose.ui.graphics.drawscope.Stroke(w * 0.1f, cap = androidx.compose.ui.graphics.StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round))
                    }
                }
                Text(title, color = contentColor, fontSize = 19.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis, modifier = Modifier.padding(start = 4.dp))
            }
            val body = Modifier.weight(1f).fillMaxWidth()
            val bottom = if (imeOpen) 0.dp else nav
            if (scroll) Column(
                body.verticalScroll(androidx.compose.foundation.rememberScrollState()).padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = bottom + 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) { content() }
            else Column(body.padding(bottom = bottom)) { content() }
        }
    }
}

/** Mantido pelo nome antigo: agora abre em tela inteira. */
@Composable
internal fun QinblyBottomPanel(title: String, onDismiss: () -> Unit, content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) =
    QinblyFullScreen(title, onDismiss, content = content)

@Composable
private fun BigButton(text: String, color: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(modifier.clip(RoundedCornerShape(28.dp)).background(color).clickable { onClick() }.padding(vertical = 17.dp), contentAlignment = Alignment.Center) {
        Text(text, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
    }
}

/** Campo de texto arredondado (pílula) usado nas telas novas. */
@Composable
internal fun QinblyPillField(value: String, onChange: (String) -> Unit, hint: String, modifier: Modifier = Modifier, maxLines: Int = 1) {
    Box(modifier.clip(RoundedCornerShape(26.dp)).background(FieldBg).padding(horizontal = 20.dp, vertical = 16.dp)) {
        if (value.isEmpty()) Text(hint, color = Color(0xFF8E87A8), fontSize = 17.sp)
        BasicTextField(value, onChange, textStyle = TextStyle(color = Color.White, fontSize = 17.sp), cursorBrush = SolidColor(Color.White), maxLines = maxLines, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun Chip(text: String, selected: Boolean, accent: Color = Color(0xFF2F68E0), onClick: () -> Unit) {
    Text(text, color = Color.White, fontSize = 16.sp, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        modifier = Modifier.clip(RoundedCornerShape(22.dp)).background(if (selected) accent else FieldBg).clickable { onClick() }.padding(horizontal = 18.dp, vertical = 11.dp))
}

internal fun qinblyAgo(iso: String): String = try {
    val m = java.time.Duration.between(java.time.OffsetDateTime.parse(iso).toInstant(), java.time.Instant.now()).toMinutes()
    when { m < 1 -> "agora"; m < 60 -> "há ${m} min"; m < 1440 -> "há ${m / 60} h"; else -> "há ${m / 1440} d" }
} catch (_: Exception) { "" }

// ───────── Clima do dia ─────────
private val moodEmojis = listOf("😊", "😄", "😎", "🥰", "😴", "🤒", "😢", "😡", "🤩", "🧘", "💪", "🎉")
private val moodColors = listOf("#7C4DFF", "#E0457B", "#1D9E75", "#EF9F27", "#378ADD", "#D85A30", "#639922", "#888780")

internal data class QinblyMood(val emoji: String, val color: Color)

/** Climas ativos (últimas 24 h) das pessoas informadas, por @. */
internal fun qinblyFetchMoods(token: String, usernames: List<String>): Map<String, QinblyMood> {
    val names = usernames.filter { it.isNotBlank() && it.all { ch -> ch.isLetterOrDigit() || ch == '_' || ch == '.' } }.distinct()
    if (names.isEmpty()) return emptyMap()
    val arr = JSONArray(QinblyApi.rest(token, "GET", "profiles?username=in.(${names.joinToString(",")})&mood_emoji=not.is.null&select=username,mood_emoji,mood_color,mood_at"))
    val out = HashMap<String, QinblyMood>()
    for (i in 0 until arr.length()) {
        val o = arr.getJSONObject(i)
        val fresh = try { java.time.Duration.between(java.time.OffsetDateTime.parse(o.optString("mood_at")).toInstant(), java.time.Instant.now()).toHours() < 24 } catch (_: Exception) { false }
        if (!fresh) continue
        val col = try { Color(android.graphics.Color.parseColor(o.optString("mood_color", "#7C4DFF"))) } catch (_: Exception) { Color(0xFF2F68E0) }
        out[o.optString("username")] = QinblyMood(o.optString("mood_emoji"), col)
    }
    return out
}

@Composable
internal fun QinblyMoodDialog(onDismiss: () -> Unit, onNotice: (String) -> Unit, onChanged: () -> Unit) {
    val onNotice: (String) -> Unit = qinblyToast(onNotice)
    val context = LocalContext.current
    val prefs = remember { sess(context) }
    var emoji by remember { mutableStateOf(moodEmojis[0]) }
    var color by remember { mutableStateOf(moodColors[0]) }
    fun save(clear: Boolean) {
        val token = prefs.getString("token", null); val uid = QinblyApi.currentUserId(prefs)
        if (token.isNullOrBlank() || uid.isNullOrBlank()) { onNotice("Entre na sua conta."); return }
        Thread {
            try {
                val b = JSONObject()
                if (clear) b.put("mood_emoji", JSONObject.NULL).put("mood_color", JSONObject.NULL).put("mood_at", JSONObject.NULL)
                else b.put("mood_emoji", emoji).put("mood_color", color).put("mood_at", java.time.Instant.now().toString())
                QinblyApi.rest(token, "PATCH", "profiles?id=eq.$uid", b.toString(), "return=minimal")
                main { onNotice(if (clear) "Clima removido." else "Clima do dia definido. Dura 24 horas."); onChanged(); onDismiss() }
            } catch (e: Exception) { main { onNotice(e.message ?: "Não foi possível salvar.") } }
        }.start()
    }
    val chosen = Color(android.graphics.Color.parseColor(color))
    var pickingEmoji by remember { mutableStateOf(false) }
    QinblyDialog(onDismissRequest = { if (pickingEmoji) pickingEmoji = false else onDismiss() }, properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false), lift = false) {
        Box(Modifier.fillMaxSize().background(Color(0xFF171719))) {
            Column(Modifier.fillMaxSize().statusBarsPadding()) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(40.dp).clip(CircleShape).background(Color(0x22FFFFFF)).clickable { if (pickingEmoji) pickingEmoji = false else onDismiss() }, contentAlignment = Alignment.Center) {
                        Text(if (pickingEmoji) "←" else "✕", color = Color.White, fontSize = 18.sp)
                    }
                    Text(if (pickingEmoji) "Escolher emoji" else "Clima do dia", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 17.sp, modifier = Modifier.padding(start = 14.dp))
                }
                if (pickingEmoji) {
                    Box(Modifier.size(84.dp).align(Alignment.CenterHorizontally).clip(CircleShape).background(chosen), contentAlignment = Alignment.Center) { Text(emoji, fontSize = 44.sp) }
                    Spacer(Modifier.size(12.dp))
                    QinblyEmojiPanel(Modifier.padding(bottom = qinblyNavBarPadding()), Color(0xFF171719), gridHeight = 380.dp) { e -> emoji = e; pickingEmoji = false }
                } else {
                    Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(androidx.compose.foundation.rememberScrollState()).padding(horizontal = 22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Spacer(Modifier.size(18.dp))
                        Box(Modifier.size(120.dp).clip(CircleShape).background(chosen).border(4.dp, Color.White, CircleShape), contentAlignment = Alignment.Center) { Text(emoji, fontSize = 62.sp) }
                        Spacer(Modifier.size(16.dp))
                        Text("Como você está hoje?", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Text("Aparece no seu cartão de story por 24 horas.", color = Color(0xFFA9B5C8), fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp))
                        Spacer(Modifier.size(24.dp))
                        (moodEmojis + "＋").chunked(5).forEach { row ->
                            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                                row.forEach { e ->
                                    val isMore = e == "＋"
                                    val on = if (isMore) emoji !in moodEmojis else emoji == e
                                    Box(Modifier.size(56.dp).clip(RoundedCornerShape(18.dp)).background(if (on) chosen.copy(alpha = 0.4f) else FieldBg).border(if (on) 2.dp else 0.dp, chosen, RoundedCornerShape(18.dp)).clickable { if (isMore) pickingEmoji = true else emoji = e }, contentAlignment = Alignment.Center) {
                                        Text(if (isMore && emoji !in moodEmojis) emoji else e, fontSize = if (isMore && emoji in moodEmojis) 22.sp else 28.sp, color = Color.White)
                                    }
                                }
                                repeat(5 - row.size) { Spacer(Modifier.size(56.dp)) }
                            }
                        }
                        Spacer(Modifier.size(22.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            moodColors.forEach { c -> Box(Modifier.size(32.dp).clip(CircleShape).background(Color(android.graphics.Color.parseColor(c))).border(if (color == c) 3.dp else 1.dp, Color.White, CircleShape).clickable { color = c }) }
                        }
                        Spacer(Modifier.size(24.dp))
                    }
                    Column(Modifier.fillMaxWidth().padding(start = 22.dp, end = 22.dp, bottom = 14.dp + qinblyNavBarPadding()), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Publicar clima", color = Color(0xFF26215C), fontWeight = FontWeight.Bold, fontSize = 17.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)).background(Color.White).clickable { save(false) }.padding(vertical = 16.dp))
                        Text("Limpar meu clima", color = Color(0xFF7FA6F2), fontSize = 15.sp, modifier = Modifier.clip(RoundedCornerShape(16.dp)).clickable { save(true) }.padding(horizontal = 16.dp, vertical = 8.dp))
                    }
                }
            }
        }
    }
}

// ───────── Pacotes de figurinhas da comunidade ─────────
private data class Pack(val id: String, val name: String, val owner: String, val paths: List<String>)

@Composable
internal fun QinblyStickerPacksDialog(accent: Color, onDismiss: () -> Unit, onNotice: (String) -> Unit) {
    val onNotice: (String) -> Unit = qinblyToast(onNotice)
    val context = LocalContext.current
    val prefs = remember { sess(context) }
    var packs by remember { mutableStateOf<List<Pack>?>(null) }
    var name by remember { mutableStateOf("") }
    var refresh by remember { mutableStateOf(0) }
    LaunchedEffect(refresh) {
        val token = prefs.getString("token", null)
        if (token.isNullOrBlank()) { packs = emptyList(); return@LaunchedEffect }
        packs = try {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                val a = JSONArray(QinblyApi.rest(token, "GET", "sticker_packs?select=id,name,profiles(username),sticker_pack_items(path)&order=created_at.desc&limit=30"))
                (0 until a.length()).map { i ->
                    val o = a.getJSONObject(i); val items = o.optJSONArray("sticker_pack_items") ?: JSONArray()
                    Pack(o.getString("id"), o.optString("name"), o.optJSONObject("profiles")?.optString("username").orEmpty(), (0 until items.length()).map { items.getJSONObject(it).optString("path") })
                }
            }
        } catch (_: Exception) { packs ?: emptyList() }
    }
    fun publish() {
        val token = prefs.getString("token", null); val uid = QinblyApi.currentUserId(prefs)
        if (token.isNullOrBlank() || uid.isNullOrBlank()) { onNotice("Entre na sua conta."); return }
        if (name.isBlank()) { onNotice("Dê um nome ao pacote."); return }
        val n = name.trim()
        Thread {
            try {
                val mine = QinblyApi.getMyStickers(token)
                if (mine.isEmpty()) throw IllegalStateException("Crie figurinhas primeiro.")
                val r = JSONArray(QinblyApi.rest(token, "POST", "sticker_packs", JSONObject().put("name", n).toString(), "return=representation"))
                val pid = r.getJSONObject(0).getString("id")
                val items = JSONArray(); mine.forEach { items.put(JSONObject().put("pack_id", pid).put("path", it.path)) }
                QinblyApi.rest(token, "POST", "sticker_pack_items", items.toString(), "return=minimal")
                main { name = ""; onNotice("Pacote publicado."); refresh++ }
            } catch (e: Exception) { main { onNotice(e.message ?: "Não foi possível publicar.") } }
        }.start()
    }
    fun add(p: Pack) {
        val token = prefs.getString("token", null); val uid = QinblyApi.currentUserId(prefs)
        if (token.isNullOrBlank() || uid.isNullOrBlank()) return
        Thread {
            try {
                val have = QinblyApi.getMyStickers(token).map { it.path }.toSet()
                val items = JSONArray(); p.paths.filter { it !in have }.forEach { items.put(JSONObject().put("owner_id", uid).put("path", it)) }
                if (items.length() > 0) QinblyApi.rest(token, "POST", "user_stickers", items.toString(), "return=minimal")
                main { onNotice(if (items.length() > 0) "${items.length()} figurinhas adicionadas." else "Você já tem todas.") }
            } catch (e: Exception) { main { onNotice(e.message ?: "Não foi possível adicionar.") } }
        }.start()
    }
    QinblyBottomPanel("Pacotes de figurinhas", onDismiss) {
        Text("Publique as suas figurinhas como um pacote e adicione as da comunidade.", color = Color(0xFFA9B5C8), fontSize = 16.sp)
        QinblyPillField(name, { name = it.take(40) }, "Nome do meu pacote", Modifier.fillMaxWidth())
        BigButton("Publicar meu pacote", accent, Modifier.fillMaxWidth()) { publish() }
        Text("Publicar usa todas as suas figurinhas atuais.", color = Color(0xFF8E87A8), fontSize = 13.sp)
        val list = packs
        if (list == null) Text("Carregando…", color = Color(0xFFA9B5C8), fontSize = 16.sp)
        else if (list.isEmpty()) Text("Nenhum pacote ainda. Seja o primeiro!", color = Color(0xFFA9B5C8), fontSize = 16.sp)
        else list.forEach { p ->
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(FieldBg).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(p.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("por @${p.owner} • ${p.paths.size} figurinhas", color = Color(0xFFA9B5C8), fontSize = 14.sp)
                    }
                    Text("Adicionar", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(accent).clickable { add(p) }.padding(horizontal = 16.dp, vertical = 10.dp))
                }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(p.paths.take(8)) { path -> AsyncImage(model = STICKER_BASE + path, contentDescription = null, modifier = Modifier.size(64.dp)) }
                }
            }
        }
    }
}

// ───────── Mural de avisos ─────────
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
internal fun QinblyNeighborhoodBoard(lat: Double, lng: Double, place: String, textColor: Color, secondary: Color, onRefreshLocation: () -> Unit, onNotice: (String) -> Unit, onOpenConversation: (String) -> Unit = {}) {
    val context = LocalContext.current
    val prefs = remember { sess(context) }
    val myId = remember { QinblyApi.currentUserId(prefs) }
    // Aproxima a posição (~1 km) antes de gravar, para não expor o ponto exato de quem avisa.
    val gLat = Math.round(lat * 100) / 100.0
    val gLng = Math.round(lng * 100) / 100.0
    var notices by remember { mutableStateOf<List<JSONObject>?>(null) }
    var draft by remember { mutableStateOf("") }
    var refresh by remember { mutableStateOf(0) }
    var posting by remember { mutableStateOf(false) }
    var openNotice by remember { mutableStateOf<JSONObject?>(null) }
    LaunchedEffect(refresh, gLat, gLng) {
        val token = prefs.getString("token", null)
        if (token.isNullOrBlank()) { notices = emptyList(); return@LaunchedEffect }
        notices = try {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                val dLat = 0.05; val dLng = 0.05 / Math.cos(Math.toRadians(lat)).coerceAtLeast(0.2)
                val q = "neighborhood_notices?lat=gte.${gLat - dLat}&lat=lte.${gLat + dLat}&lng=gte.${gLng - dLng}&lng=lte.${gLng + dLng}&expires_at=gt.${java.time.Instant.now()}&select=id,body,kind,photo,created_at,user_id,profiles!neighborhood_notices_user_id_fkey(username,display_name),neighborhood_comments(count)&order=created_at.desc&limit=50"
                val a = JSONArray(QinblyApi.rest(token, "GET", q))
                (0 until a.length()).map { a.getJSONObject(it) }
            }
        } catch (_: Exception) { notices ?: emptyList() }
    }
    fun post() {
        val token = prefs.getString("token", null); val body = draft.trim()
        if (token.isNullOrBlank() || body.isBlank() || posting) return
        posting = true
        Thread {
            try {
                val j = JSONObject().put("body", body).put("lat", gLat).put("lng", gLng).put("kind", "aviso")
                QinblyApi.rest(token, "POST", "neighborhood_notices", j.toString(), "return=minimal")
                main { draft = ""; posting = false; onNotice("Aviso publicado. Some em 48 horas."); refresh++ }
            } catch (e: Exception) { main { posting = false; onNotice(e.message ?: "Não foi possível publicar.") } }
        }.start()
    }
    Column(Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Mural de avisos", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = textColor)
                Text(if (place.isNotBlank()) "Perto de $place • avisos somem em 48 h" else "Avisos de até ~5 km • somem em 48 h", color = secondary, fontSize = 12.sp)
            }
        }
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(FieldBg).padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.fillMaxWidth().heightIn(min = 64.dp).padding(start = 6.dp, top = 8.dp, end = 4.dp)) {
                if (draft.isEmpty()) Text("Escreva um aviso para a vizinhança…", color = Color(0xFF8E87A8), fontSize = 16.sp)
                androidx.compose.foundation.text.BasicTextField(draft, { draft = it.take(500) }, textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 16.sp), cursorBrush = androidx.compose.ui.graphics.SolidColor(Color.White), maxLines = 6, modifier = Modifier.fillMaxWidth())
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${draft.length}/500", color = Color(0xFF8E87A8), fontSize = 11.sp, modifier = Modifier.padding(start = 6.dp))
                Spacer(Modifier.weight(1f))
                val ready = draft.isNotBlank() && !posting
                Text(if (posting) "Enviando…" else "Publicar", color = if (ready) Color.White else Color(0xFF8E87A8), fontWeight = FontWeight.Bold, fontSize = 14.sp,
                    modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(if (ready) Color(0xFF2F68E0) else Color(0xFF2E2E30)).clickable(enabled = ready) { post() }.padding(horizontal = 20.dp, vertical = 9.dp))
            }
        }
        val list = notices
        var pulling by remember { mutableStateOf(false) }
        LaunchedEffect(notices) { if (notices != null) pulling = false }
        LaunchedEffect(pulling) { if (pulling) { kotlinx.coroutines.delay(2500); pulling = false } }
        androidx.compose.material3.pulltorefresh.PullToRefreshBox(isRefreshing = pulling, onRefresh = { pulling = true; refresh++; onRefreshLocation() }, modifier = Modifier.weight(1f).fillMaxWidth()) {
        if (list == null) Column(Modifier.fillMaxSize().verticalScroll(androidx.compose.foundation.rememberScrollState())) { Text("Carregando…", color = secondary) }
        else if (list.isEmpty()) Column(Modifier.fillMaxSize().verticalScroll(androidx.compose.foundation.rememberScrollState())) { Text("Nenhum aviso por perto ainda. Escreva o primeiro.", color = secondary, fontSize = 14.sp) }
        else LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(list, key = { it.optString("id") }) { n ->
                val p = n.optJSONObject("profiles")
                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(FieldBg).clickable { openNotice = n }.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text((p?.optString("display_name").orEmpty().ifBlank { p?.optString("username").orEmpty() }) + " • " + qinblyAgo(n.optString("created_at")), color = Color(0xFF7FA6F2), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                        if (n.optString("user_id") == myId) Text("Apagar", color = Color(0xFFE0457B), fontSize = 12.sp, modifier = Modifier.clickable {
                            val id = n.optString("id"); val token = prefs.getString("token", null) ?: return@clickable
                            notices = notices?.filterNot { it.optString("id") == id }
                            Thread { try { QinblyApi.rest(token, "DELETE", "neighborhood_notices?id=eq.$id") } catch (_: Exception) { } }.start()
                        })
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(n.optString("body"), color = Color.White, fontSize = 15.sp, maxLines = 4, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                    }
                    val cnt = n.optJSONArray("neighborhood_comments")?.optJSONObject(0)?.optInt("count") ?: 0
                    Row(verticalAlignment = Alignment.CenterVertically) { QinblyIconOf(QinblyIcon.ChatBubbleOutline, secondary, 16.dp); Text(if (cnt == 0) "  Comentar" else if (cnt == 1) "  1 comentário" else "  $cnt comentários", color = secondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                }
            }
        }
        }
    }

    openNotice?.let { n ->
        NoticeDetail(n, myId, prefs, onDismiss = { openNotice = null }, onNotice = onNotice, onChanged = { refresh++ })
    }
}

private fun mentionText(body: String, color: Color): androidx.compose.ui.text.AnnotatedString {
    val end = if (body.startsWith("@")) body.indexOfFirst { it.isWhitespace() }.let { if (it < 0) body.length else it } else 0
    return androidx.compose.ui.text.buildAnnotatedString {
        append(body)
        if (end > 1) addStyle(androidx.compose.ui.text.SpanStyle(color = color, fontWeight = FontWeight.Bold), 0, end)
    }
}

private object MentionTransform : androidx.compose.ui.text.input.VisualTransformation {
    override fun filter(text: androidx.compose.ui.text.AnnotatedString) =
        androidx.compose.ui.text.input.TransformedText(mentionText(text.text, Color(0xFF7FA6F2)), androidx.compose.ui.text.input.OffsetMapping.Identity)
}

/** Aviso aberto: foto, texto e comentários. As respostas ficam escondidas até tocar em "Ver respostas". */
@Composable
private fun NoticeDetail(n: JSONObject, myId: String?, prefs: android.content.SharedPreferences, onDismiss: () -> Unit, onNotice: (String) -> Unit, onChanged: () -> Unit) {
    val noticeId = n.optString("id")
    val noticeOwner = n.optString("user_id")
    val p = n.optJSONObject("profiles")
    var comments by remember { mutableStateOf<List<JSONObject>?>(null) }
    var reload by remember { mutableStateOf(0) }
    var expanded by remember { mutableStateOf<Set<String>>(emptySet()) }
    var replyTo by remember { mutableStateOf<JSONObject?>(null) }
    var input by remember { mutableStateOf(androidx.compose.ui.text.input.TextFieldValue("")) }
    var sending by remember { mutableStateOf(false) }
    var menuFor by remember { mutableStateOf<String?>(null) }
    var editing by remember { mutableStateOf<JSONObject?>(null) }
    val focus = remember { androidx.compose.ui.focus.FocusRequester() }
    LaunchedEffect(reload) {
        val token = prefs.getString("token", null) ?: run { comments = emptyList(); return@LaunchedEffect }
        comments = try {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                val a = JSONArray(QinblyApi.rest(token, "GET", "neighborhood_comments?notice_id=eq.$noticeId&select=id,parent_id,user_id,body,created_at,profiles!neighborhood_comments_user_id_fkey(username,display_name)&order=created_at.asc&limit=300"))
                (0 until a.length()).map { a.getJSONObject(it) }.filter { !QinblyHidden.matches(it.optString("body")) }
            }
        } catch (_: Exception) { comments ?: emptyList() }
    }
    fun nameOf(c: JSONObject): String { val pr = c.optJSONObject("profiles"); return pr?.optString("display_name").orEmpty().ifBlank { pr?.optString("username").orEmpty() }.ifBlank { "Alguém" } }
    fun handleOf(c: JSONObject): String = c.optJSONObject("profiles")?.optString("username").orEmpty().removePrefix("@").ifBlank { nameOf(c).replace(" ", "") }
    fun startReply(c: JSONObject) {
        replyTo = c
        val t = "@" + handleOf(c) + " "
        input = androidx.compose.ui.text.input.TextFieldValue(t, androidx.compose.ui.text.TextRange(t.length))
        val top = c.optString("parent_id").takeIf { it.isNotBlank() && it != "null" } ?: c.optString("id")
        expanded = expanded + top
        try { focus.requestFocus() } catch (_: Exception) { }
    }
    fun send() {
        val token = prefs.getString("token", null); val body = input.text.trim()
        if (token.isNullOrBlank() || body.isBlank() || sending) return
        val ed = editing
        if (ed != null) {
            val eid = ed.optString("id"); val newBody = body.take(500)
            sending = true
            Thread {
                try {
                    QinblyApi.rest(token, "PATCH", "neighborhood_comments?id=eq.$eid", JSONObject().put("body", newBody).toString(), "return=minimal")
                    main { comments = comments?.map { if (it.optString("id") == eid) JSONObject(it.toString()).put("body", newBody) else it }; input = androidx.compose.ui.text.input.TextFieldValue(""); editing = null; sending = false; onChanged() }
                } catch (e: Exception) { main { sending = false; onNotice("Não foi possível editar o comentário.") } }
            }.start()
            return
        }
        val target = replyTo
        val parent = target?.let { it.optString("parent_id").takeIf { x -> x.isNotBlank() && x != "null" } ?: it.optString("id") }
        sending = true
        Thread {
            try {
                val j = JSONObject().put("notice_id", noticeId).put("body", body.take(500))
                if (parent != null) j.put("parent_id", parent)
                QinblyApi.rest(token, "POST", "neighborhood_comments", j.toString(), "return=minimal")
                main { input = androidx.compose.ui.text.input.TextFieldValue(""); replyTo = null; sending = false; if (parent != null) expanded = expanded + parent; reload++; onChanged() }
            } catch (e: Exception) { main { sending = false; onNotice(e.message ?: "Não foi possível comentar.") } }
        }.start()
    }
    fun delete(c: JSONObject) {
        val token = prefs.getString("token", null) ?: return
        val id = c.optString("id")
        comments = comments?.filterNot { it.optString("id") == id || it.optString("parent_id") == id }
        Thread { try { QinblyApi.rest(token, "DELETE", "neighborhood_comments?id=eq.$id"); main { onChanged() } } catch (_: Exception) { } }.start()
    }
    QinblyFullScreen("Aviso", onDismiss, scroll = false) {
        Column(Modifier.fillMaxSize()) {
            Column(Modifier.weight(1f).verticalScroll(androidx.compose.foundation.rememberScrollState()).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text((p?.optString("display_name").orEmpty().ifBlank { p?.optString("username").orEmpty() }) + " • " + qinblyAgo(n.optString("created_at")), color = Color(0xFF7FA6F2), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 8.dp))
                }
                Text(n.optString("body"), color = Color.White, fontSize = 16.sp)
                val all = comments
                val tops = all?.filter { it.optString("parent_id").let { x -> x.isBlank() || x == "null" } }.orEmpty()
                Text(if (all == null) "Carregando comentários…" else if (all.isEmpty()) "Nenhum comentário ainda. Seja o primeiro." else "${all.size} comentário" + (if (all.size > 1) "s" else ""), color = Color(0xFFB4B4BC), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                @Composable fun Bubble(c: JSONObject, small: Boolean) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(Modifier.size(if (small) 24.dp else 30.dp).clip(CircleShape).background(Color(0xFF3A3A3E)), contentAlignment = Alignment.Center) {
                            Text(nameOf(c).take(1).uppercase(), color = Color.White, fontSize = if (small) 11.sp else 13.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(Modifier.weight(1f)) {
                            val mine = c.optString("user_id") == myId
                            val canMenu = mine || noticeOwner == myId
                            Box {
                            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(FieldBg)
                                .pointerInput(c.optString("id"), canMenu) { if (canMenu) detectTapGestures(onLongPress = { menuFor = c.optString("id") }) }
                                .padding(horizontal = 12.dp, vertical = 8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(nameOf(c), color = Color(0xFF7FA6F2), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    if (c.optString("user_id") == noticeOwner) Text("autor", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 6.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFF2F68E0)).padding(horizontal = 6.dp, vertical = 1.dp))
                                    Text(" • " + qinblyAgo(c.optString("created_at")), color = Color(0xFF8E87A8), fontSize = 11.sp)
                                }
                                Text(mentionText(c.optString("body"), Color(0xFF7FA6F2)), color = Color.White, fontSize = 14.sp)
                            }
                            androidx.compose.material3.DropdownMenu(expanded = menuFor == c.optString("id"), onDismissRequest = { menuFor = null }) {
                                if (mine) androidx.compose.material3.DropdownMenuItem(text = { Text("Editar") }, onClick = {
                                    menuFor = null; editing = c; replyTo = null
                                    val t = c.optString("body")
                                    input = androidx.compose.ui.text.input.TextFieldValue(t, androidx.compose.ui.text.TextRange(t.length))
                                    try { focus.requestFocus() } catch (_: Exception) { }
                                })
                                androidx.compose.material3.DropdownMenuItem(text = { Text("Apagar", color = Color(0xFFE0457B)) }, onClick = { menuFor = null; delete(c) })
                            }
                            }
                            Row(Modifier.padding(start = 8.dp, top = 2.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                                Text("Responder", color = Color(0xFF8E87A8), fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { startReply(c) }.padding(vertical = 4.dp))
                            }
                        }
                    }
                }
                for (c in tops) {
                    Bubble(c, false)
                    val replies = all.orEmpty().filter { it.optString("parent_id") == c.optString("id") }
                    if (replies.isNotEmpty()) {
                        val open = c.optString("id") in expanded
                        Text(if (open) "Ocultar respostas ▴" else "Ver ${replies.size} resposta" + (if (replies.size > 1) "s" else "") + " ▾", color = Color(0xFF8E87A8), fontSize = 12.sp, fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 38.dp).clickable { expanded = if (open) expanded - c.optString("id") else expanded + c.optString("id") }.padding(vertical = 4.dp))
                        if (open) Column(Modifier.padding(start = 38.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { for (r in replies) Bubble(r, true) }
                    }
                }
            }
            if (editing != null) Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Editando comentário", color = Color(0xFF7FA6F2), fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                QinblyCloseIcon(Color(0xFFB4B4BC), 18.dp, Modifier.clickable { editing = null; input = androidx.compose.ui.text.input.TextFieldValue("") })
            }
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.weight(1f).clip(RoundedCornerShape(22.dp)).background(FieldBg).padding(horizontal = 16.dp, vertical = 11.dp)) {
                    if (input.text.isEmpty()) Text(if (editing != null) "Edite o comentário…" else if (replyTo != null) "Escreva uma resposta…" else "Escreva um comentário…", color = Color(0xFF8E87A8), fontSize = 14.sp)
                    androidx.compose.foundation.text.BasicTextField(
                        input, { v -> input = androidx.compose.ui.text.input.TextFieldValue(v.text.take(500), v.selection, v.composition); if (v.text.isEmpty()) replyTo = null },
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 14.sp), cursorBrush = androidx.compose.ui.graphics.SolidColor(Color.White),
                        visualTransformation = MentionTransform, maxLines = 3, modifier = Modifier.fillMaxWidth().focusRequester(focus)
                    )
                }
                val ready = input.text.isNotBlank() && !sending
                Box(Modifier.size(42.dp).clip(CircleShape).background(if (ready) Color(0xFF2F68E0) else Color(0xFF2E2E30)).clickable(enabled = ready) { send() }, contentAlignment = Alignment.Center) { QinblySendIcon(Color.White, 19.dp) }
            }
        }
    }
}


// ───────── Ícones de linha (sem cor) para as categorias do perfil ─────────
/** Tira emoji/símbolos do fim do nome ("Games 🎮" → "Games"). */
internal fun qinblyCategoryName(raw: String): String = raw.replace(Regex("\\s*[^\\p{L}\\p{N}\\s]+\\s*$"), "").trim()

internal fun qinblyCategoryIcon(raw: String): androidx.compose.ui.graphics.vector.ImageVector {
    val o = androidx.compose.material.icons.Icons.Outlined
    return when (qinblyCategoryName(raw)) {
        "Criador de conteúdo" -> o.Videocam
        "Lifestyle" -> o.WbSunny
        "Viagens" -> o.Flight
        "Games" -> o.SportsEsports
        "Beleza" -> o.Face
        "Moda" -> o.Checkroom
        "Fitness", "Academia", "Musculação" -> o.FitnessCenter
        "Música" -> o.MusicNote
        "Arte" -> o.Palette
        "Fotografia" -> o.PhotoCamera
        "Tecnologia" -> o.Memory
        "Gastronomia" -> o.Restaurant
        "Humor" -> o.SentimentVerySatisfied
        "Educação" -> o.School
        "Esportes" -> o.SportsBasketball
        "Futebol" -> o.SportsSoccer
        "Natureza" -> o.Park
        "Corrida" -> o.DirectionsRun
        "Dança" -> o.Nightlife
        "Saúde e bem-estar" -> o.Spa
        "Nutrição" -> o.Eco
        "Yoga" -> o.SelfImprovement
        "Pets" -> o.Pets
        "Família" -> o.FamilyRestroom
        "Maternidade" -> o.ChildCare
        "Empreendedorismo" -> o.Lightbulb
        "Negócios" -> o.BusinessCenter
        "Finanças" -> o.AccountBalance
        "Carros" -> o.DirectionsCar
        "Motos" -> o.TwoWheeler
        "Cinema" -> o.Movie
        "Livros" -> o.MenuBook
        "DIY e artesanato" -> o.Build
        "Decoração" -> o.Chair
        "Jardinagem" -> o.LocalFlorist
        "Ciência" -> o.Science
        "Comédia" -> o.Mic
        "Culinária" -> o.Kitchen
        else -> o.Tag
    }
}

/** Linha de categorias do perfil: ícone de linha + nome, separadas por "•". Aceita o texto salvo (com ou sem emoji). */
@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
internal fun QinblyCategoryLine(raw: String, textColor: Color, iconColor: Color, modifier: Modifier = Modifier) {
    val items = raw.split(" • ").map { it.trim() }.filter { it.isNotBlank() }
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items.forEach { item ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                androidx.compose.material3.Icon(qinblyCategoryIcon(item), contentDescription = null, tint = iconColor, modifier = Modifier.size(17.dp))
                androidx.compose.material3.Text(qinblyCategoryName(item), color = textColor, style = androidx.compose.material3.MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(start = 5.dp))
            }
        }
    }
}


// ───────── Nome do lugar na aba Perto de mim e nas publicações ─────────
private fun qinblyNoAccent(s: String): String =
    java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD).replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "").lowercase().trim()

private val qinblyStateSiglas: Map<String, String> = mapOf(
    "Acre" to "AC", "Alagoas" to "AL", "Amapá" to "AP", "Amazonas" to "AM", "Bahia" to "BA", "Ceará" to "CE", "Distrito Federal" to "DF",
    "Espírito Santo" to "ES", "Goiás" to "GO", "Maranhão" to "MA", "Mato Grosso" to "MT", "Mato Grosso do Sul" to "MS", "Minas Gerais" to "MG",
    "Pará" to "PA", "Paraíba" to "PB", "Paraná" to "PR", "Pernambuco" to "PE", "Piauí" to "PI", "Rio de Janeiro" to "RJ", "Rio Grande do Norte" to "RN",
    "Rio Grande do Sul" to "RS", "Rondônia" to "RO", "Roraima" to "RR", "Santa Catarina" to "SC", "São Paulo" to "SP", "Sergipe" to "SE", "Tocantins" to "TO"
).mapKeys { qinblyNoAccent(it.key) }

/** "Vilhena, Rondônia" → "Vilhena, RO" (nome da cidade inteiro, estado em sigla). */
internal fun qinblyFullPlace(place: String): String {
    val city = place.substringBefore(",").trim()
    val state = place.substringAfter(",", "").trim()
    if (city.isBlank()) return ""
    val sigla = qinblyStateSiglas[qinblyNoAccent(state)] ?: (if (state.length == 2) state.uppercase() else state)
    return if (sigla.isBlank()) city else "$city, $sigla"
}

/** Versão curta para a aba (cabe numa linha): Sta./S./Dr., tira "do/da/d'Oeste" no fim, inicial do primeiro nome e, por último, corta com "…". */
internal fun qinblyShortPlace(place: String, max: Int = 18): String {
    val city0 = place.substringBefore(",").trim()
    if (city0.isBlank()) return ""
    val tail = qinblyFullPlace(place).removePrefix(city0)   // ", RO" ou ""
    var city = city0
        .replace(Regex("^Santa\\s+"), "Sta. ").replace(Regex("^Santo\\s+"), "Sto. ")
        .replace(Regex("^São\\s+"), "S. ").replace(Regex("^Doutor\\s+"), "Dr. ")
    if ((city + tail).length > max) city = city.replace(Regex("\\s+(?:d[aeo]s?\\s|d['’]).*$"), "")
    if ((city + tail).length > max) {
        val parts = city.split(" ").filter { it.isNotBlank() }
        if (parts.size >= 2 && !parts[0].endsWith(".")) city = parts[0].take(1) + ". " + parts.drop(1).joinToString(" ")
    }
    if ((city + tail).length > max) city = city.take((max - tail.length - 1).coerceAtLeast(3)).trimEnd() + "…"
    return city + tail
}
