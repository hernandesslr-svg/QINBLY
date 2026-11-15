package app.qinbly.android

import android.content.Context
import android.os.SystemClock
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.ImageProcessingOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarker
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarkerResult
import kotlin.math.atan2
import kotlin.math.asin

/**
 * Rosto em 3D de um quadro: rotação da cabeça (graus) e expressões (0..1).
 * Os pontos vêm normalizados (0..1) na imagem já girada para "em pé".
 */
internal class QinblyFace3d(
    val yaw: Float,
    val pitch: Float,
    val roll: Float,
    val jawOpen: Float,
    val smile: Float,
    val blinkLeft: Float,
    val blinkRight: Float,
    val browUp: Float,
    val points: FloatArray,
    val matrix: FloatArray?
)

/** Estado observável pelo Compose. Fica vazio quando o MediaPipe não está disponível. */
internal class QinblyFaceTrackerState {
    var face by mutableStateOf<QinblyFace3d?>(null)
    var available by mutableStateOf(false)
    var width by mutableStateOf(1)
    var height by mutableStateOf(1)
}

/**
 * Rastreador 3D com MediaPipe Face Landmarker (modo LIVE_STREAM).
 * Roda em paralelo com o detector antigo (ML Kit) e NUNCA derruba a câmera:
 * se o arquivo do modelo não existir ou algo falhar, [state].available fica false.
 * O modelo precisa estar em app/src/main/assets/face_landmarker.task.
 */
internal class QinblyFaceTracker3D(
    context: Context,
    private val state: QinblyFaceTrackerState
) : ImageAnalysis.Analyzer {
    @Volatile var enabled: Boolean = false
    private val appContext = context.applicationContext
    private var landmarker: FaceLandmarker? = null
    private var failed = false
    private var lastW = 1
    private var lastH = 1

    private fun ensureLandmarker(): FaceLandmarker? {
        landmarker?.let { return it }
        if (failed) return null
        return try {
            val base = BaseOptions.builder().setModelAssetPath("face_landmarker.task").build()
            val options = FaceLandmarker.FaceLandmarkerOptions.builder()
                .setBaseOptions(base)
                .setRunningMode(RunningMode.LIVE_STREAM)
                .setNumFaces(1)
                .setOutputFaceBlendshapes(true)
                .setOutputFacialTransformationMatrixes(true)
                .setResultListener { result, _ -> onResult(result) }
                .setErrorListener { _ -> state.face = null }
                .build()
            FaceLandmarker.createFromOptions(appContext, options).also {
                landmarker = it
                state.available = true
            }
        } catch (t: Throwable) {
            failed = true
            state.available = false
            null
        }
    }

    override fun analyze(image: ImageProxy) {
        try {
            feed(image)
        } finally {
            image.close()
        }
    }

    /** Envia o quadro ao MediaPipe sem fechá-lo (quem chama fecha). */
    fun feed(image: ImageProxy) {
        try {
            if (!enabled) {
                if (state.face != null) state.face = null
                return
            }
            val lm = ensureLandmarker() ?: return
            val rot = image.imageInfo.rotationDegrees
            lastW = if (rot == 90 || rot == 270) image.height else image.width
            lastH = if (rot == 90 || rot == 270) image.width else image.height
            val bitmap = image.toBitmap()
            val mp = BitmapImageBuilder(bitmap).build()
            val opts = ImageProcessingOptions.builder().setRotationDegrees(rot).build()
            lm.detectAsync(mp, opts, SystemClock.uptimeMillis())
        } catch (t: Throwable) {
            state.face = null
        }
    }

    private fun onResult(result: FaceLandmarkerResult) {
        val landmarks = result.faceLandmarks()
        if (landmarks.isEmpty()) {
            state.face = null
            return
        }
        val pts = landmarks[0]
        val arr = FloatArray(pts.size * 3)
        for (i in pts.indices) {
            arr[i * 3] = pts[i].x()
            arr[i * 3 + 1] = pts[i].y()
            arr[i * 3 + 2] = pts[i].z()
        }
        val shapes = HashMap<String, Float>()
        if (result.faceBlendshapes().isPresent) {
            val list = result.faceBlendshapes().get()
            if (list.isNotEmpty()) {
                for (c in list[0]) shapes[c.categoryName()] = c.score()
            }
        }
        var m: FloatArray? = null
        if (result.facialTransformationMatrixes().isPresent) {
            val ms = result.facialTransformationMatrixes().get()
            if (ms.isNotEmpty()) m = ms[0]
        }
        var yaw = 0f
        var pitch = 0f
        var roll = 0f
        if (m != null && m.size >= 16) {
            // Matriz 4x4 em ordem de colunas: m[coluna*4 + linha].
            val r00 = m[0]; val r10 = m[1]; val r20 = m[2]
            val r21 = m[6]; val r22 = m[10]
            pitch = Math.toDegrees(atan2(-r21.toDouble(), r22.toDouble())).toFloat()
            yaw = Math.toDegrees(asin(r20.toDouble().coerceIn(-1.0, 1.0))).toFloat()
            roll = Math.toDegrees(atan2(-r10.toDouble(), r00.toDouble())).toFloat()
        }
        state.width = lastW
        state.height = lastH
        state.face = QinblyFace3d(
            yaw = yaw,
            pitch = pitch,
            roll = roll,
            jawOpen = shapes["jawOpen"] ?: 0f,
            smile = ((shapes["mouthSmileLeft"] ?: 0f) + (shapes["mouthSmileRight"] ?: 0f)) / 2f,
            blinkLeft = shapes["eyeBlinkLeft"] ?: 0f,
            blinkRight = shapes["eyeBlinkRight"] ?: 0f,
            browUp = shapes["browInnerUp"] ?: 0f,
            points = arr,
            matrix = m
        )
    }

    fun close() {
        try { landmarker?.close() } catch (_: Throwable) {}
        landmarker = null
    }
}

/** Giro (graus) que o EXIF pede para a foto ficar em pé. */
internal fun qinblyExifRotation(context: Context, uri: android.net.Uri): Int = try {
    context.contentResolver.openInputStream(uri)?.use { s ->
        when (android.media.ExifInterface(s).getAttributeInt(android.media.ExifInterface.TAG_ORIENTATION, android.media.ExifInterface.ORIENTATION_NORMAL)) {
            android.media.ExifInterface.ORIENTATION_ROTATE_90 -> 90
            android.media.ExifInterface.ORIENTATION_ROTATE_180 -> 180
            android.media.ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }
    } ?: 0
} catch (_: Throwable) { 0 }

/** Devolve a foto girada como o celular a mostra (a decodificação crua ignora o EXIF). */
internal fun qinblyUpright(context: Context, uri: android.net.Uri, bmp: android.graphics.Bitmap): android.graphics.Bitmap {
    val rot = qinblyExifRotation(context, uri)
    if (rot == 0) return bmp
    val m = android.graphics.Matrix().apply { postRotate(rot.toFloat()) }
    val out = android.graphics.Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, m, true)
    if (out !== bmp) bmp.recycle()
    return out
}

/** Tamanho da foto (já em pé) e, se achar, o rosto maior: centro e largura em fração da foto (0..1). */
internal class QinblyPhotoInfo(val width: Int, val height: Int, val hasFace: Boolean, val faceCx: Float, val faceCy: Float, val faceW: Float)

internal fun qinblyPhotoInfo(context: Context, uri: android.net.Uri): QinblyPhotoInfo? {
    try {
        val bounds = android.graphics.BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { android.graphics.BitmapFactory.decodeStream(it, null, bounds) }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
        val rot = qinblyExifRotation(context, uri)
        val w = if (rot == 90 || rot == 270) bounds.outHeight else bounds.outWidth
        val h = if (rot == 90 || rot == 270) bounds.outWidth else bounds.outHeight
        var face: Triple<Float, Float, Float>? = null
        var landmarker: FaceLandmarker? = null
        try {
            var sample = 1
            while (maxOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= 1024) sample *= 2
            val small = context.contentResolver.openInputStream(uri)?.use {
                android.graphics.BitmapFactory.decodeStream(it, null, android.graphics.BitmapFactory.Options().apply { inSampleSize = sample })
            }
            if (small != null) {
                val upright = qinblyUpright(context, uri, small)
                val base = BaseOptions.builder().setModelAssetPath("face_landmarker.task").build()
                val options = FaceLandmarker.FaceLandmarkerOptions.builder()
                    .setBaseOptions(base).setRunningMode(RunningMode.IMAGE).setNumFaces(3).build()
                val lm = FaceLandmarker.createFromOptions(context.applicationContext, options)
                landmarker = lm
                val result = lm.detect(BitmapImageBuilder(upright).build())
                var best = 0f
                for (pts in result.faceLandmarks()) {
                    if (pts.isEmpty()) continue
                    val minX = pts.minOf { it.x() }; val maxX = pts.maxOf { it.x() }
                    val minY = pts.minOf { it.y() }; val maxY = pts.maxOf { it.y() }
                    val area = (maxX - minX) * (maxY - minY)
                    if (area > best) { best = area; face = Triple((minX + maxX) / 2f, (minY + maxY) / 2f, maxX - minX) }
                }
                upright.recycle()
            }
        } catch (_: Throwable) {
        } finally { try { landmarker?.close() } catch (_: Throwable) { } }
        return QinblyPhotoInfo(w, h, face != null, face?.first ?: 0.5f, face?.second ?: 0.38f, face?.third ?: 0f)
    } catch (_: Throwable) { return null }
}
