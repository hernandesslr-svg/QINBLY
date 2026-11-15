package app.qinbly.android

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

/**
 * PADRÃO DO APLICATIVO (ver PADRAO-FORMATO.md): em qualquer lugar que a pessoa escolhe o formato de uma foto,
 * ela vê o quadro com guias, arrasta e dá zoom para escolher o enquadramento, e decide se a foto sai
 * cortada (Preencher) ou inteira (Inteira = barras pretas, Desfocar = fundo desfocado).
 */
internal val QINBLY_FORMATS = listOf("Original", "1:1", "4:5", "3:4", "4:3", "9:16", "16:9")

internal data class QinblyFrameRect(val left: Float, val top: Float, val w: Float, val h: Float)

/** Onde fica o quadro (centralizado) dentro de uma área; é a mesma conta da prévia, dos adesivos, do texto e do desenho. */
internal fun qinblyFrameRect(boxW: Int, boxH: Int, frameRatio: Float): QinblyFrameRect {
    if (boxW <= 0 || boxH <= 0 || frameRatio <= 0f) return QinblyFrameRect(0f, 0f, boxW.toFloat(), boxH.toFloat())
    val fw = if (boxW.toFloat() / boxH > frameRatio) boxH * frameRatio else boxW.toFloat()
    val fh = fw / frameRatio
    return QinblyFrameRect((boxW - fw) / 2f, (boxH - fh) / 2f, fw, fh)
}

/** Zoom em que a foto cabe inteira no quadro (1 = preenche). Menor que 1 quando a foto é de outro formato. */
internal fun qinblyFitFactor(imageRatio: Float, frameRatio: Float): Float {
    if (imageRatio <= 0f || frameRatio <= 0f) return 1f
    return minOf(imageRatio / frameRatio, frameRatio / imageRatio).coerceIn(0.05f, 1f)
}

/** Posição vertical inicial ao preencher: o rosto fica por volta de 1/3 do quadro (cabeça nunca de fora). */
internal suspend fun qinblyFacePanY(context: Context, uri: Uri, imageRatio: Float, frameRatio: Float): Float {
    if (imageRatio >= frameRatio - 0.02f) return 0f
    val fy = QinblyFaceFocus.find(context, uri.toString())
    val visible = (imageRatio / frameRatio).coerceIn(0.05f, 1f)
    val room = 1f - visible
    if (fy < 0f || room < 0.001f) return 0f
    val top = (fy - 0.33f * visible).coerceIn(0f, room)
    val lim = qinblyMaxPan(imageRatio, frameRatio, 1f).second
    return ((room / 2f - top) / visible).coerceIn(-lim, lim)
}

@Composable
internal fun QinblyFormatFrame(
    uri: Uri, ratio: String, imageRatio: Float, zoom: Float, panX: Float, panY: Float, blurBg: Boolean,
    colorFilter: ColorFilter?, adjusting: Boolean,
    onChange: (Float, Float, Float) -> Unit, modifier: Modifier = Modifier
) {
    val frameRatio = qinblyRatioValue(ratio, imageRatio)
    val factor = qinblyFitFactor(imageRatio, frameRatio)
    val z by rememberUpdatedState(zoom)
    val px by rememberUpdatedState(panX)
    val py by rememberUpdatedState(panY)
    val change by rememberUpdatedState(onChange)
    val density = LocalDensity.current
    BoxWithConstraints(modifier, contentAlignment = Alignment.Center) {
        val rect = qinblyFrameRect(constraints.maxWidth, constraints.maxHeight, frameRatio)
        val fwDp = with(density) { rect.w.toDp() }
        val fhDp = with(density) { rect.h.toDp() }
        Box(
            Modifier.size(fwDp, fhDp).pointerInput(ratio, imageRatio) {
                detectTransformGestures { _, pan, gestureZoom, _ ->
                    val nz = (z * gestureZoom).coerceIn(minOf(factor, 1f), 6f)
                    val lim = qinblyMaxPan(imageRatio, frameRatio, nz)
                    val nx = (px + pan.x / size.width).coerceIn(-lim.first, lim.first)
                    val ny = (py + pan.y / size.height).coerceIn(-lim.second, lim.second)
                    change(nz, nx, ny)
                }
            }
        ) {
            if (adjusting) {
                // O que fica fora do quadro aparece escurecido: não vai sair na foto.
                AsyncImage(
                    model = uri, contentDescription = null, contentScale = ContentScale.Fit, colorFilter = colorFilter,
                    modifier = Modifier.fillMaxSize().alpha(0.3f).graphicsLayer {
                        scaleX = zoom / factor; scaleY = zoom / factor
                        translationX = panX * size.width; translationY = panY * size.height
                    }
                )
            }
            Box(Modifier.fillMaxSize().clipToBounds().background(Color.Black)) {
                if (blurBg && android.os.Build.VERSION.SDK_INT >= 31) {
                    AsyncImage(model = uri, contentDescription = null, contentScale = ContentScale.Crop, colorFilter = colorFilter,
                        modifier = Modifier.fillMaxSize().blur(32.dp).alpha(0.7f))
                }
                AsyncImage(
                    model = uri, contentDescription = "Prévia da publicação", contentScale = ContentScale.Fit, colorFilter = colorFilter,
                    modifier = Modifier.fillMaxSize().graphicsLayer {
                        scaleX = zoom / factor; scaleY = zoom / factor
                        translationX = panX * size.width; translationY = panY * size.height
                    }
                )
            }
            if (adjusting) {
                Canvas(Modifier.fillMaxSize()) {
                    val line = Color(0x99FFFFFF)
                    drawRect(Color.White, style = Stroke(width = 1.dp.toPx()))
                    for (i in 1..2) {
                        drawLine(line, Offset(size.width * i / 3f, 0f), Offset(size.width * i / 3f, size.height), strokeWidth = 0.5.dp.toPx())
                        drawLine(line, Offset(0f, size.height * i / 3f), Offset(size.width, size.height * i / 3f), strokeWidth = 0.5.dp.toPx())
                    }
                    val len = 18.dp.toPx(); val sw = 3.dp.toPx()
                    val w = size.width; val h = size.height
                    drawLine(Color.White, Offset(0f, 0f), Offset(len, 0f), sw); drawLine(Color.White, Offset(0f, 0f), Offset(0f, len), sw)
                    drawLine(Color.White, Offset(w, 0f), Offset(w - len, 0f), sw); drawLine(Color.White, Offset(w, 0f), Offset(w, len), sw)
                    drawLine(Color.White, Offset(0f, h), Offset(len, h), sw); drawLine(Color.White, Offset(0f, h), Offset(0f, h - len), sw)
                    drawLine(Color.White, Offset(w, h), Offset(w - len, h), sw); drawLine(Color.White, Offset(w, h), Offset(w, h - len), sw)
                }
            }
        }
    }
}

/** Barra de baixo: formatos (Original, 1:1, 4:5, 3:4, 4:3, 9:16, 16:9) e como a foto ocupa o quadro (Preencher, Inteira, Desfocar). */
@Composable
internal fun QinblyFormatBar(
    ratio: String, imageRatio: Float, zoom: Float, blurBg: Boolean,
    onRatio: (String) -> Unit, onFill: (String) -> Unit, modifier: Modifier = Modifier, onZoom: ((Float) -> Unit)? = null
) {
    val factor = qinblyFitFactor(imageRatio, qinblyRatioValue(ratio, imageRatio))
    val whole = factor < 0.97f && zoom <= factor + 0.02f
    val mode = if (whole) (if (blurBg) "blur" else "fit") else if (zoom >= 0.98f) "fill" else ""
    @Composable fun Pill(label: String, on: Boolean, click: () -> Unit) {
        Text(label, color = if (on) Color(0xFF0B0D12) else Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 3.dp).clip(RoundedCornerShape(14.dp))
                .background(if (on) Color.White else Color.Transparent)
                .border(0.5.dp, Color(0x80FFFFFF), RoundedCornerShape(14.dp))
                .clickable { click() }.padding(horizontal = 11.dp, vertical = 6.dp))
    }
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.Center) {
            QINBLY_FORMATS.forEach { r -> Pill(r, ratio == r) { onRatio(r) } }
        }
        if (onZoom != null) {
            // Zoom: também dá para abrir/fechar com dois dedos direto na foto.
            Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Zoom", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                androidx.compose.material3.Slider(
                    value = zoom.coerceIn(minOf(factor, 1f), 4f), onValueChange = { onZoom(it) }, valueRange = minOf(factor, 1f)..4f,
                    colors = androidx.compose.material3.SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = Color.White, inactiveTrackColor = Color(0x55FFFFFF)),
                    modifier = Modifier.weight(1f).padding(start = 10.dp).height(28.dp)
                )
            }
        }
        if (factor < 0.97f) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Pill("Preencher", mode == "fill") { onFill("fill") }
                Pill("Inteira", mode == "fit") { onFill("fit") }
                Pill("Desfocar", mode == "blur") { onFill("blur") }
            }
        }
    }
}
