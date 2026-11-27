package app.qinbly.android

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

/** Onde está o rosto na capa do vídeo: proporção da capa (largura/altura) e topo do rosto (0..1, ou -1 se não achou). */
internal data class QinblyFaceFocus(val aspect: Float, val top: Float)

/** Acha o rosto na capa do vídeo (no próprio celular) para o corte do feed nunca pegar a cabeça. O resultado fica guardado por vídeo. */
internal object QinblyFaceFocusCache {
    private val cache = ConcurrentHashMap<String, QinblyFaceFocus>()
    private val detector by lazy {
        FaceDetection.getClient(
            FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .setMinFaceSize(0.08f)
                .build()
        )
    }

    fun get(url: String): QinblyFaceFocus? = cache[url]

    suspend fun find(context: Context, url: String): QinblyFaceFocus? {
        cache[url]?.let { return it }
        if (!url.startsWith("http")) return null
        return try {
            val request = coil.request.ImageRequest.Builder(context)
                .data(url)
                .decoderFactory(coil.decode.VideoFrameDecoder.Factory())
                .videoFrameMillis(500)
                .memoryCacheKey("face:$url")
                .diskCacheKey("poster:$url")
                .allowHardware(false)
                .size(480)
                .build()
            val result = coil.Coil.imageLoader(context).execute(request)
            val bitmap = (result.drawable as? BitmapDrawable)?.bitmap ?: return null
            if (bitmap.width <= 0 || bitmap.height <= 0) return null
            val image = InputImage.fromBitmap(bitmap, 0)
            val faces = suspendCancellableCoroutine<List<Face>> { cont ->
                detector.process(image)
                    .addOnSuccessListener { cont.resume(it) }
                    .addOnFailureListener { cont.resume(emptyList()) }
            }
            val biggest = faces.maxByOrNull { it.boundingBox.width() * it.boundingBox.height() }
            val top = if (biggest == null) -1f else (biggest.boundingBox.top.toFloat().coerceAtLeast(0f) / bitmap.height).coerceIn(0f, 1f)
            QinblyFaceFocus(bitmap.width.toFloat() / bitmap.height, top).also { cache[url] = it }
        } catch (_: Exception) {
            null
        }
    }
}
