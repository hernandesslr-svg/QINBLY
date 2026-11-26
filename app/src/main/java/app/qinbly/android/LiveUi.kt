package app.qinbly.android

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Tela de live (quem transmite e quem assiste): cápsula, corações que sobem, espectadores e avisos. */

private val LiveHeartColors = listOf(Color(0xFFF26B6B), Color(0xFFED93B1), Color(0xFFD4537E))
private val LivePanel = Color(0x8C14141C)
private val LivePanelEdge = Color(0x2EFFFFFF)
internal val QinblyLivePurple = Color(0xFF8B5CF6)

internal class QinblyHeart(val id: Long, val x: Float, val y: Float, val size: Float, val drift: Float)

/** Lista dos corações que estão subindo agora. Cada um some sozinho no fim da subida. */
internal class QinblyLiveHearts {
    val list = mutableStateListOf<QinblyHeart>()
    private var next = 0L
    fun add(x: Float, y: Float) {
        if (list.size > 40) list.removeAt(0)
        list.add(QinblyHeart(next++, x, y, 20f + (Math.random() * 18).toFloat(), ((Math.random() - 0.5) * 70).toFloat()))
    }
    fun remove(id: Long) { list.removeAll { it.id == id } }
}

@Composable
internal fun QinblyHeartsLayer(state: QinblyLiveHearts, modifier: Modifier = Modifier) {
    Box(modifier) {
        state.list.toList().forEach { h ->
            key(h.id) {
                val progress = remember { Animatable(0f) }
                LaunchedEffect(Unit) {
                    progress.animateTo(1f, tween(1800, easing = LinearEasing))
                    state.remove(h.id)
                }
                Icon(
                    QinblyIcon.Favorite, null, tint = LiveHeartColors[(h.id % 3).toInt()],
                    modifier = Modifier
                        .offset { IntOffset(h.x.toInt(), h.y.toInt()) }
                        .size(h.size.dp)
                        .graphicsLayer {
                            translationY = -progress.value * 460.dp.toPx()
                            translationX = Math.sin(progress.value * 6.0).toFloat() * h.drift
                            alpha = (1f - progress.value * progress.value).coerceIn(0f, 1f)
                            val s = 0.7f + 0.5f * (1f - progress.value)
                            scaleX = s; scaleY = s
                        }
                )
            }
        }
    }
}

private fun liveNameColor(name: String): Color {
    val palette = listOf(Color(0xFFED93B1), Color(0xFF85B7EB), Color(0xFFFAC775), Color(0xFF9FE1CB), Color(0xFFC9B8FF))
    return palette[Math.abs(name.hashCode()) % palette.size]
}

/** Bolinha com a foto (ou a inicial, se não tiver foto). */
@Composable
internal fun QinblyLiveAvatar(name: String, url: String, size: androidx.compose.ui.unit.Dp, ring: Color? = null) {
    val base = if (ring != null) Modifier.size(size).clip(CircleShape).border(2.dp, ring, CircleShape) else Modifier.size(size).clip(CircleShape)
    Box(base.background(liveNameColor(name).copy(alpha = 0.55f)), contentAlignment = Alignment.Center) {
        if (url.isNotBlank()) coil.compose.AsyncImage(model = url, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        else Text(name.trim().removePrefix("@").take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Medium, fontSize = (size.value * 0.42f).sp)
    }
}

/** Cápsula de cima: foto, nome, curtidas e (para quem assiste) o botão Seguir. */
@Composable
internal fun QinblyLiveCapsule(name: String, avatarUrl: String, likes: Int, followLabel: String?, onFollow: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier.clip(RoundedCornerShape(26.dp)).background(LivePanel).border(BorderStroke(0.5.dp, LivePanelEdge), RoundedCornerShape(26.dp))
            .padding(start = 4.dp, end = if (followLabel == null) 14.dp else 5.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        QinblyLiveAvatar(name, avatarUrl, 36.dp, Color(0xFFD4537E))
        Column {
            Text(name, color = Color.White, fontWeight = FontWeight.Medium, fontSize = 14.sp, maxLines = 1)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(QinblyIcon.Favorite, null, tint = Color(0xFFF26B6B), modifier = Modifier.size(11.dp))
                Spacer(Modifier.width(3.dp))
                Text("$likes", color = Color(0xFFDDDDDD), fontSize = 11.sp)
            }
        }
        if (followLabel != null) {
            Text(
                followLabel, color = Color.White, fontWeight = FontWeight.Medium, fontSize = 12.sp,
                modifier = Modifier.clip(RoundedCornerShape(16.dp)).background(if (followLabel == "Seguir") QinblyLivePurple else Color(0x33FFFFFF))
                    .clickable { onFollow() }.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
}

/** Fotinhas de quem está assistindo (até duas) e o número. */
@Composable
internal fun QinblyLiveViewersPill(names: List<String>, count: Int, modifier: Modifier = Modifier) {
    Row(
        modifier.clip(RoundedCornerShape(16.dp)).background(LivePanel).border(BorderStroke(0.5.dp, LivePanelEdge), RoundedCornerShape(16.dp))
            .padding(start = 3.dp, end = 9.dp, top = 3.dp, bottom = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        names.take(2).forEachIndexed { i, n ->
            Box(Modifier.offset(x = (-7 * i).dp)) { QinblyLiveAvatar(n, "", 20.dp) }
        }
        Spacer(Modifier.width(if (names.size >= 2) 0.dp else 4.dp))
        Text("$count", color = Color.White, fontSize = 12.sp)
    }
}

/** Botão redondo escuro e translúcido. */
@Composable
internal fun QinblyLiveRoundButton(size: androidx.compose.ui.unit.Dp, background: Color = LivePanel, onClick: () -> Unit, content: @Composable () -> Unit) {
    Box(
        Modifier.size(size).clip(CircleShape).background(background).border(BorderStroke(0.5.dp, if (background == LivePanel) LivePanelEdge else Color.Transparent), CircleShape).clickable { onClick() },
        contentAlignment = Alignment.Center
    ) { content() }
}

/** Aviso "fulano entrou na live", em roxo claro, acima dos comentários. */
@Composable
internal fun QinblyLiveJoinNotice(text: String, modifier: Modifier = Modifier) {
    if (text.isBlank()) return
    Text(text, color = Color(0xFFC9B8FF), fontSize = 11.sp, modifier = modifier)
}

/** Balão de comentário fosco, com o nome em cor. */
@Composable
internal fun QinblyLiveBubble(name: String, body: String) {
    Text(
        buildAnnotatedString {
            withStyle(SpanStyle(fontWeight = FontWeight.Medium, color = liveNameColor(name))) { append(name + "  ") }
            append(body)
        },
        color = Color.White, fontSize = 12.sp, lineHeight = 16.sp,
        modifier = Modifier.clip(RoundedCornerShape(14.dp)).background(LivePanel).border(BorderStroke(0.5.dp, Color(0x24FFFFFF)), RoundedCornerShape(14.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    )
}
