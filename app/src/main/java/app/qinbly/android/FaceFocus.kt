package app.qinbly.android

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.ui.platform.LocalContext
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import coil.request.videoFrameMillis
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

/**
 * Padrão de toda foto cortada na tela (feed, miniaturas): o corte acompanha o rosto, a cabeça nunca fica de fora.
 * Acha o rosto maior de cada foto uma vez (guarda na memória) e devolve a altura do rosto (0..1), ou -1 se não achou.
 * A foto inteira continua disponível ao tocar nela (visualizador).
 */
internal object QinblyFaceFocus {
    private val cache = ConcurrentHashMap<String, Float>()
    private val lock = Mutex()
    private var landmarker: FaceLandmarker? = null
    private var failed = false

    fun cached(url: String): Float? = cache[url]

    /** Mesmo detector, para vídeo: usa um quadro do vídeo (o mesmo da capa). Devolve a altura do rosto (0..1) ou -1. */
    fun cachedVideo(url: String): Float? = cache["v:$url"]

    suspend fun findVideo(context: Context, url: String): Float {
        val key = "v:$url"
        cache[key]?.let { return it }
        val result = withContext(Dispatchers.Default) {
            try {
                val request = ImageRequest.Builder(context).data(url)
                    .decoderFactory(coil.decode.VideoFrameDecoder.Factory())
                    .videoFrameMillis(500)
                    .size(512).allowHardware(false)
                    .memoryCacheKey("facevid:$url").diskCacheKey("facevid:$url")
                    .build()
                val loaded = context.imageLoader.execute(request)
                val bmp = ((loaded as? SuccessResult)?.drawable as? android.graphics.drawable.BitmapDrawable)?.bitmap
                if (bmp == null) -1f else lock.withLock { detect(context, bmp) }
            } catch (_: Throwable) { -1f }
        }
        cache[key] = result
        return result
    }

    suspend fun find(context: Context, url: String): Float {
        cache[url]?.let { return it }
        val result = withContext(Dispatchers.Default) {
            try {
                val request = ImageRequest.Builder(context).data(url).size(512).allowHardware(false).build()
                val loaded = context.imageLoader.execute(request)
                val bmp = ((loaded as? SuccessResult)?.drawable as? android.graphics.drawable.BitmapDrawable)?.bitmap
                if (bmp == null) -1f else lock.withLock { detect(context, bmp) }
            } catch (_: Throwable) { -1f }
        }
        cache[url] = result
        return result
    }

    private fun detect(context: Context, source: Bitmap): Float {
        if (failed) return -1f
        return try {
            val lm = landmarker ?: FaceLandmarker.createFromOptions(
                context.applicationContext,
                FaceLandmarker.FaceLandmarkerOptions.builder()
                    .setBaseOptions(BaseOptions.builder().setModelAssetPath("face_landmarker.task").build())
                    .setRunningMode(RunningMode.IMAGE).setNumFaces(3).build()
            ).also { landmarker = it }
            val bmp = if (source.config == Bitmap.Config.ARGB_8888) source else source.copy(Bitmap.Config.ARGB_8888, false)
            val result = lm.detect(BitmapImageBuilder(bmp).build())
            var best = 0f
            var cy = -1f
            for (pts in result.faceLandmarks()) {
                if (pts.isEmpty()) continue
                val minY = pts.minOf { it.y() }; val maxY = pts.maxOf { it.y() }
                val minX = pts.minOf { it.x() }; val maxX = pts.maxOf { it.x() }
                val area = (maxY - minY) * (maxX - minX)
                if (area > best) { best = area; cy = ((minY + maxY) / 2f).coerceIn(0f, 1f) }
            }
            cy
        } catch (_: Throwable) { failed = true; -1f }
    }
}

/**
 * Alinhamento do corte (ContentScale.Crop) para fotos mais altas que o quadro: sobe/desce o corte para o rosto ficar dentro.
 * Sem rosto, o corte fica um pouco acima do meio (cabeças costumam estar na parte de cima).
 */
@Composable
internal fun rememberQinblyFaceAlignment(url: String, imageRatio: Float, frameRatio: Float): Alignment {
    val context = LocalContext.current.applicationContext
    val crops = imageRatio < frameRatio - 0.02f
    var face by remember(url) { mutableStateOf(QinblyFaceFocus.cached(url)) }
    LaunchedEffect(url, crops) { if (crops && face == null) face = QinblyFaceFocus.find(context, url) }
    if (!crops) return Alignment.Center
    val visible = (imageRatio / frameRatio).coerceIn(0.05f, 1f)
    val room = 1f - visible
    if (room < 0.001f) return Alignment.Center
    val fy = face
    // Com rosto: ele fica por volta de 1/3 do quadro, com folga em cima.
    val top = if (fy != null && fy >= 0f) (fy - 0.33f * visible).coerceIn(0f, room) else 0.2f * room
    return BiasAlignment(0f, 2f * top / room - 1f)
}

/** Miniatura quadrada/retangular do perfil: o corte segue o rosto (a cabeça nunca fica de fora). */
@Composable
internal fun QinblyFaceThumb(url: String, modifier: Modifier = Modifier, frameRatio: Float = 0.75f, description: String? = null) {
    var ratio by remember(url) { mutableStateOf(QinblyFaceThumbRatios.map[url] ?: 0.75f) }
    val alignment = rememberQinblyFaceAlignment(url, ratio, frameRatio)
    AsyncImage(
        model = url, contentDescription = description, modifier = modifier,
        contentScale = ContentScale.Crop, alignment = alignment,
        onSuccess = { r ->
            val d = r.result.drawable
            if (d.intrinsicWidth > 0 && d.intrinsicHeight > 0) {
                val v = d.intrinsicWidth.toFloat() / d.intrinsicHeight.toFloat()
                QinblyFaceThumbRatios.map[url] = v
                ratio = v
            }
        }
    )
}

private object QinblyFaceThumbRatios { val map = ConcurrentHashMap<String, Float>() }

/** Onde alinhar o vídeo cortado no quadro: o rosto fica por volta de 1/3 do quadro. Sem rosto, alinha pelo topo (como já era). */
internal fun qinblyFaceBias(faceY: Float?, aspect: Float, wc: Float, hc: Float): Alignment {
    if (aspect <= 0f || wc <= 0f || hc <= 0f) return Alignment.TopCenter
    val w = if (aspect >= wc / hc) hc * aspect else wc
    val visible = (hc / (w / aspect)).coerceIn(0.05f, 1f)
    val room = 1f - visible
    if (room < 0.001f) return Alignment.Center
    val top = if (faceY != null && faceY >= 0f) (faceY - 0.33f * visible).coerceIn(0f, room) else 0f
    return BiasAlignment(0f, 2f * top / room - 1f)
}
