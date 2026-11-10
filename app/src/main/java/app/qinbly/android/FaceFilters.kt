package app.qinbly.android

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.face.FaceLandmark
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.Executors
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.sin

/** Filtros de rosto próprios do QINBLY (desenhados em código, sem arquivos externos). */
internal val QinblyFaceFilterNames = listOf(
    "Cachorro", "Coroa", "Gato", "Coelho", "Óculos de sol", "Estrelas",
    "Urso", "Palhaço", "Festa", "Bigode", "Flores", "Anjo"
)

// ───────────────────────── Desenho ─────────────────────────

private fun DrawScope.fOval(c: Color, cx: Float, cy: Float, rx: Float, ry: Float) =
    drawOval(c, Offset(cx - rx, cy - ry), Size(rx * 2f, ry * 2f))

private fun DrawScope.fOvalB(b: Brush, cx: Float, cy: Float, rx: Float, ry: Float) =
    drawOval(b, Offset(cx - rx, cy - ry), Size(rx * 2f, ry * 2f))

private fun starPath(cx: Float, cy: Float, r: Float): Path {
    val p = Path()
    for (i in 0 until 10) {
        val rad = if (i % 2 == 0) r else r * 0.45f
        val a = (-90.0 + i * 36.0) * Math.PI / 180.0
        val x = cx + (rad * cos(a)).toFloat()
        val y = cy + (rad * sin(a)).toFloat()
        if (i == 0) p.moveTo(x, y) else p.lineTo(x, y)
    }
    p.close()
    return p
}

/**
 * Desenha o filtro [name] num rosto cujos olhos estão em [eyeA] e [eyeB] (pixels do canvas).
 * Tudo é desenhado numa unidade em que 1,0 = distância entre os olhos, origem no meio dos olhos
 * e Y para baixo; o conjunto é girado conforme a inclinação da cabeça.
 */
internal const val QINBLY_DOG_YAW_SIGN = 1f

/** Peças realistas do Cachorro (PNG em assets/dog). Se faltar alguma, o filtro usa o desenho simples. */
internal object QinblyDogSprites {
    @Volatile var ear: ImageBitmap? = null
    @Volatile var nose: ImageBitmap? = null
    @Volatile var tongue: ImageBitmap? = null
    @Volatile private var tried = false

    fun ensure(context: android.content.Context) {
        if (tried) return
        tried = true
        try {
            fun load(name: String): ImageBitmap? =
                context.assets.open("dog/$name").use { BitmapFactory.decodeStream(it)?.asImageBitmap() }
            ear = load("dog_ear.png")
            nose = load("dog_nose.png")
            tongue = load("dog_tongue.png")
        } catch (_: Throwable) { }
    }
}

private fun DrawScope.fSprite(img: ImageBitmap, cx: Float, cy: Float, w: Float, h: Float, flip: Boolean = false) {
    val kx = w / img.width
    val ky = h / img.height
    withTransform({
        translate(cx, cy)
        scale(if (flip) -kx else kx, ky, Offset.Zero)
        translate(-img.width / 2f, -img.height / 2f)
    }) {
        drawImage(
            img,
            androidx.compose.ui.unit.IntOffset.Zero,
            androidx.compose.ui.unit.IntSize(img.width, img.height),
            androidx.compose.ui.unit.IntOffset.Zero,
            androidx.compose.ui.unit.IntSize(img.width, img.height),
            1f,
            androidx.compose.ui.graphics.drawscope.Fill,
            null,
            androidx.compose.ui.graphics.BlendMode.SrcOver,
            androidx.compose.ui.graphics.FilterQuality.High
        )
    }
}

internal fun DrawScope.qinblyDrawFaceFilter(name: String, eyeA: Offset, eyeB: Offset, extra: QinblyFace3d? = null) {
    val left = if (eyeA.x <= eyeB.x) eyeA else eyeB
    val right = if (eyeA.x <= eyeB.x) eyeB else eyeA
    val dist = hypot(right.x - left.x, right.y - left.y)
    if (dist < 4f) return
    val mid = Offset((left.x + right.x) / 2f, (left.y + right.y) / 2f)
    val deg = Math.toDegrees(atan2((right.y - left.y).toDouble(), (right.x - left.x).toDouble())).toFloat()
    withTransform({
        translate(mid.x, mid.y)
        rotate(deg, Offset.Zero)
        scale(dist, dist, Offset.Zero)
    }) {
        val line = Stroke(width = 0.05f, cap = StrokeCap.Round)
        when (name) {
            "Cachorro" -> {
                val turn = if (extra != null) (extra.yaw * QINBLY_DOG_YAW_SIGN / 40f).coerceIn(-1f, 1f) else 0f
                val open = (extra?.jawOpen ?: 0f).coerceIn(0f, 1f)
                val sEar = QinblyDogSprites.ear
                val sNose = QinblyDogSprites.nose
                val sTongue = QinblyDogSprites.tongue
                if (sEar != null && sNose != null && sTongue != null) {
                    for (sg in listOf(-1f, 1f)) {
                        val cx = sg * 0.80f + turn * 0.2f
                        rotate(-sg * 15f, Offset(cx, -1.15f)) {
                            fSprite(sEar, cx, -0.78f, 0.52f, 0.81f, flip = sg < 0f)
                        }
                    }
                    fSprite(sNose, 0f, 0.47f, 0.36f, 0.26f)
                    if (open > 0.2f) {
                        val len = 0.30f + 0.35f * open
                        fSprite(sTongue, 0f, 0.82f + len / 2f, len * 0.4f, len)
                    }
                } else for (sg in listOf(-1f, 1f)) {
                    val away = (sg * turn).coerceIn(-1f, 1f)
                    val w = 0.22f * (1f - 0.35f * away)
                    val cx = sg * 0.78f + turn * 0.2f
                    rotate(-sg * 15f, Offset(cx, -1.15f)) {
                        fOvalB(Brush.verticalGradient(listOf(Color(0xFFA56A3B), Color(0xFF5A3418)), -1.2f, -0.4f), cx, -0.8f, w, 0.4f)
                        drawOval(Color(0xFF3E220F), Offset(cx - w, -1.2f), Size(w * 2f, 0.8f), style = Stroke(width = 0.02f))
                        fOval(Color(0xAAE8A5A5), cx, -0.74f, w * 0.55f, 0.24f)
                    }
                }
                if (sEar == null || sNose == null || sTongue == null) {
                fOval(Color(0xFF1B1B1F), 0f, 0.44f, 0.13f, 0.09f)
                fOval(Color(0x99FFFFFF), -0.04f, 0.41f, 0.045f, 0.022f)
                drawLine(Color(0xFF3E2A2A), Offset(0f, 0.53f), Offset(0f, 0.68f), 0.025f, StrokeCap.Round)
                if (open > 0.2f) {
                    val top = 0.8f + 0.1f * open
                    val len = 0.15f + 0.3f * open
                    fOvalB(Brush.verticalGradient(listOf(Color(0xFFF0728A), Color(0xFFD94A66)), top, top + len), 0f, top + len / 2f, 0.13f, len / 2f)
                    drawLine(Color(0x55A03048), Offset(0f, top + 0.02f), Offset(0f, top + len * 0.8f), 0.018f, StrokeCap.Round)
                }
                }
            }
            "Coroa" -> {
                val p = Path().apply {
                    moveTo(-0.9f, -1.7f); lineTo(-1.0f, -2.5f); lineTo(-0.5f, -2.1f); lineTo(0f, -2.7f)
                    lineTo(0.5f, -2.1f); lineTo(1.0f, -2.5f); lineTo(0.9f, -1.7f); close()
                }
                drawPath(p, Brush.verticalGradient(listOf(Color(0xFFFFE08A), Color(0xFFE0A82E)), -2.7f, -1.7f))
                drawPath(p, Color(0xFFB8860B), style = line)
                drawRect(Color(0xFFE0A82E), Offset(-0.9f, -1.85f), Size(1.8f, 0.15f))
                fOval(Color(0xFFE24B4A), 0f, -2.7f, 0.07f, 0.07f)
                fOval(Color(0xFF378ADD), -1.0f, -2.5f, 0.07f, 0.07f)
                fOval(Color(0xFF1D9E75), 1.0f, -2.5f, 0.07f, 0.07f)
                fOval(Color(0xFFFFFFFF), 0f, -1.78f, 0.06f, 0.05f)
            }
            "Gato" -> {
                for (sg in listOf(-1f, 1f)) {
                    val outer = Path().apply { moveTo(sg * 1.55f, -0.9f); lineTo(sg * 1.4f, -2.4f); lineTo(sg * 0.5f, -1.65f); close() }
                    val inner = Path().apply { moveTo(sg * 1.35f, -1.15f); lineTo(sg * 1.3f, -2.0f); lineTo(sg * 0.75f, -1.6f); close() }
                    drawPath(outer, Color(0xFF9AA0A6))
                    drawPath(inner, Color(0xFFF4B6C2))
                    drawLine(Color(0xEEFFFFFF), Offset(sg * 0.5f, 1.0f), Offset(sg * 1.7f, 0.85f), 0.025f, StrokeCap.Round)
                    drawLine(Color(0xEEFFFFFF), Offset(sg * 0.5f, 1.1f), Offset(sg * 1.7f, 1.15f), 0.025f, StrokeCap.Round)
                }
                val nose = Path().apply { moveTo(-0.14f, 0.72f); lineTo(0.14f, 0.72f); lineTo(0f, 0.9f); close() }
                drawPath(nose, Color(0xFFE88CA0))
            }
            "Coelho" -> {
                for (sg in listOf(-1f, 1f)) {
                    rotate(sg * 8f, Offset(sg * 0.6f, -3.0f)) {
                        fOval(Color(0xFFF7F1F1), sg * 0.6f, -3.0f, 0.32f, 1.15f)
                        fOval(Color(0xFFF4B6C2), sg * 0.6f, -2.9f, 0.16f, 0.85f)
                    }
                }
                fOval(Color(0xFFF08FA4), 0f, 0.82f, 0.13f, 0.1f)
                drawRect(Color.White, Offset(-0.09f, 1.15f), Size(0.08f, 0.15f))
                drawRect(Color.White, Offset(0.01f, 1.15f), Size(0.08f, 0.15f))
            }
            "Óculos de sol" -> {
                for (sg in listOf(-1f, 1f)) {
                    drawRoundRect(Color(0xF0101014), Offset(sg * 0.5f - 0.48f, -0.32f), Size(0.96f, 0.66f), CornerRadius(0.26f, 0.26f))
                    drawLine(Color(0x66FFFFFF), Offset(sg * 0.5f - 0.3f, -0.2f), Offset(sg * 0.5f - 0.12f, -0.26f), 0.05f, StrokeCap.Round)
                    drawLine(Color(0xFF101014), Offset(sg * 0.98f, -0.15f), Offset(sg * 1.45f, -0.25f), 0.07f, StrokeCap.Round)
                }
                drawLine(Color(0xFF101014), Offset(-0.03f, -0.05f), Offset(0.03f, -0.05f), 0.08f, StrokeCap.Round)
            }
            "Estrelas" -> {
                drawPath(starPath(-1.6f, -1.2f, 0.28f), Color(0xFFF2C14E))
                drawPath(starPath(1.5f, -1.6f, 0.22f), Color(0xFFFF4FD8))
                drawPath(starPath(0f, -2.4f, 0.3f), Color(0xFFF2C14E))
                drawPath(starPath(1.75f, -0.2f, 0.18f), Color(0xFF5DE0F2))
                drawPath(starPath(-1.75f, 0.45f, 0.2f), Color(0xFFFF4FD8))
                drawPath(starPath(-0.9f, -2.0f, 0.14f), Color(0xFFFFFFFF))
            }
            "Urso" -> {
                for (sg in listOf(-1f, 1f)) {
                    fOval(Color(0xFF7A5230), sg * 1.2f, -1.55f, 0.5f, 0.5f)
                    fOval(Color(0xFFD9AA7A), sg * 1.2f, -1.5f, 0.28f, 0.28f)
                }
                fOval(Color(0xFFD9AA7A), 0f, 0.95f, 0.55f, 0.4f)
                fOval(Color(0xFF2B1A10), 0f, 0.78f, 0.2f, 0.14f)
                drawLine(Color(0xFF2B1A10), Offset(0f, 0.9f), Offset(0f, 1.1f), 0.04f, StrokeCap.Round)
            }
            "Palhaço" -> {
                val wig = listOf(Color(0xFFFF7A2F), Color(0xFFFFC53D), Color(0xFFFF4F7B))
                for (sg in listOf(-1f, 1f)) {
                    fOval(wig[0], sg * 1.5f, -0.5f, 0.55f, 0.55f)
                    fOval(wig[1], sg * 1.35f, -1.35f, 0.5f, 0.5f)
                    fOval(wig[2], sg * 0.75f, -1.95f, 0.5f, 0.5f)
                }
                fOval(Color(0xFFFF7A2F), 0f, -2.05f, 0.45f, 0.45f)
                fOvalB(Brush.verticalGradient(listOf(Color(0xFFFF6B6B), Color(0xFFD62828)), 0.5f, 1.2f), 0f, 0.85f, 0.32f, 0.32f)
                fOval(Color(0x88FFFFFF), -0.1f, 0.74f, 0.08f, 0.06f)
            }
            "Festa" -> {
                val hat = Path().apply { moveTo(0.3f, -3.7f); lineTo(-0.7f, -1.85f); lineTo(1.0f, -1.8f); close() }
                drawPath(hat, Brush.verticalGradient(listOf(Color(0xFFFF5FA2), Color(0xFF8E5CF0)), -3.7f, -1.8f))
                drawLine(Color(0xAAFFFFFF), Offset(-0.35f, -2.5f), Offset(0.7f, -2.45f), 0.07f, StrokeCap.Round)
                drawLine(Color(0xAAFFFFFF), Offset(-0.1f, -3.05f), Offset(0.5f, -3.0f), 0.07f, StrokeCap.Round)
                fOval(Color(0xFFF2C14E), 0.3f, -3.7f, 0.17f, 0.17f)
            }
            "Bigode" -> {
                val m = Path().apply {
                    moveTo(0f, 1.0f)
                    cubicTo(-0.2f, 0.85f, -0.7f, 0.8f, -1.05f, 1.2f)
                    cubicTo(-0.7f, 1.4f, -0.3f, 1.25f, 0f, 1.22f)
                    cubicTo(0.3f, 1.25f, 0.7f, 1.4f, 1.05f, 1.2f)
                    cubicTo(0.7f, 0.8f, 0.2f, 0.85f, 0f, 1.0f)
                    close()
                }
                drawPath(m, Color(0xFF3A2416))
            }
            "Flores" -> {
                val pts = listOf(Offset(-1.3f, -1.45f), Offset(-0.65f, -1.85f), Offset(0f, -2.0f), Offset(0.65f, -1.85f), Offset(1.3f, -1.45f))
                val cols = listOf(Color(0xFFF4A5C0), Color(0xFFFFD27A), Color(0xFFB9A6F0), Color(0xFFFFD27A), Color(0xFFF4A5C0))
                pts.forEachIndexed { i, c ->
                    fOval(Color(0xFF4E8F4A), c.x + 0.18f, c.y + 0.12f, 0.14f, 0.07f)
                    for (k in 0 until 5) {
                        val a = (k * 72.0 - 90.0) * Math.PI / 180.0
                        fOval(cols[i], c.x + (0.17f * cos(a)).toFloat(), c.y + (0.17f * sin(a)).toFloat(), 0.12f, 0.12f)
                    }
                    fOval(Color(0xFFF2B01E), c.x, c.y, 0.09f, 0.09f)
                }
            }
            "Anjo" -> {
                fOval(Color(0x33FFE08A), 0f, -2.5f, 0.95f, 0.3f)
                drawOval(Color(0xFFF2C14E), Offset(-0.85f, -2.72f), Size(1.7f, 0.44f), style = Stroke(width = 0.1f))
            }
        }
    }
}

// ───────────────────────── Detecção ─────────────────────────

internal class QinblyFaceState {
    val track = QinblyFaceTrackerState()
    var eyes by mutableStateOf<List<Pair<Offset, Offset>>>(emptyList())
    var width by mutableIntStateOf(1)
    var height by mutableIntStateOf(1)
}

private fun qinblyEyesOf(face: Face): Pair<Offset, Offset>? {
    val l = face.getLandmark(FaceLandmark.LEFT_EYE)?.position
    val r = face.getLandmark(FaceLandmark.RIGHT_EYE)?.position
    if (l != null && r != null) return Offset(l.x, l.y) to Offset(r.x, r.y)
    val b = face.boundingBox
    if (b.width() < 40) return null
    return Offset(b.centerX() - b.width() * 0.22f, b.top + b.height() * 0.40f) to Offset(b.centerX() + b.width() * 0.22f, b.top + b.height() * 0.40f)
}

/** Recebe os quadros da câmera e atualiza [state] com a posição dos olhos de cada rosto. Só trabalha se [enabled]. */
internal class QinblyFaceAnalyzer(private val state: QinblyFaceState, private val tracker: QinblyFaceTracker3D? = null) : ImageAnalysis.Analyzer {
    @Volatile var enabled: Boolean = false
    @Volatile var use3d: Boolean = false
    private val detector by lazy {
        FaceDetection.getClient(
            FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                .setMinFaceSize(0.15f)
                .build()
        )
    }

    @androidx.annotation.OptIn(ExperimentalGetImage::class)
    override fun analyze(image: ImageProxy) {
        val media = image.image
        if (!enabled || media == null) {
            if (state.eyes.isNotEmpty()) state.eyes = emptyList()
            image.close()
            return
        }
        tracker?.let {
            it.enabled = use3d
            if (use3d) it.feed(image)
        }
        val rot = image.imageInfo.rotationDegrees
        val upW = if (rot == 90 || rot == 270) image.height else image.width
        val upH = if (rot == 90 || rot == 270) image.width else image.height
        detector.process(InputImage.fromMediaImage(media, rot))
            .addOnSuccessListener { faces ->
                state.width = upW
                state.height = upH
                state.eyes = faces.mapNotNull { qinblyEyesOf(it) }
            }
            .addOnFailureListener { state.eyes = emptyList() }
            .addOnCompleteListener { image.close() }
    }
}

/** Camada por cima da prévia da câmera: desenha o filtro em cada rosto encontrado. */
@Composable
internal fun QinblyFaceOverlay(filter: String, state: QinblyFaceState, mirror: Boolean, modifier: Modifier = Modifier) {
    QinblyDogSprites.ensure(LocalContext.current)
    if (filter == "Cachorro" && state.track.available && !qinblyDog3DFailed) {
        QinblyDog3DOverlay(state, mirror, modifier)
        return
    }
    Canvas(modifier) {
        if (filter == "Nenhum" || state.eyes.isEmpty()) return@Canvas
        val sc = max(size.width / state.width, size.height / state.height)
        val offX = (size.width - state.width * sc) / 2f
        val offY = (size.height - state.height * sc) / 2f
        fun map(p: Offset): Offset {
            val x = p.x * sc + offX
            return Offset(if (mirror) size.width - x else x, p.y * sc + offY)
        }
        val f3 = if (filter == "Cachorro") state.track.face else null
        state.eyes.forEach { (a, b) -> qinblyDrawFaceFilter(filter, map(a), map(b), f3) }
    }
}

/** Miniatura da bolinha: rosto neutro com o filtro desenhado. */
@Composable
internal fun QinblyFaceThumb(name: String, modifier: Modifier = Modifier) {
    QinblyDogSprites.ensure(LocalContext.current)
    Canvas(modifier) {
        drawRect(Color(0xFFDCE6F2))
        val w = size.width
        val h = size.height
        fOvalB(Brush.verticalGradient(listOf(Color(0xFFF6D3B5), Color(0xFFE9B99A))), w * 0.5f, h * 0.72f, w * 0.31f, h * 0.38f)
        val eyeY = h * 0.66f
        val a = Offset(w * 0.38f, eyeY)
        val b = Offset(w * 0.62f, eyeY)
        fOval(Color(0xFF3A2A1E), a.x, a.y, w * 0.025f, w * 0.025f)
        fOval(Color(0xFF3A2A1E), b.x, b.y, w * 0.025f, w * 0.025f)
        qinblyDrawFaceFilter(name, a, b)
    }
}

// ───────────────────────── Foto com filtro ─────────────────────────

private fun qinblyExifMatrix(path: String): Matrix {
    val m = Matrix()
    try {
        val exif = android.media.ExifInterface(path)
        when (exif.getAttributeInt(android.media.ExifInterface.TAG_ORIENTATION, android.media.ExifInterface.ORIENTATION_NORMAL)) {
            android.media.ExifInterface.ORIENTATION_ROTATE_90 -> m.postRotate(90f)
            android.media.ExifInterface.ORIENTATION_ROTATE_180 -> m.postRotate(180f)
            android.media.ExifInterface.ORIENTATION_ROTATE_270 -> m.postRotate(270f)
            android.media.ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> m.postScale(-1f, 1f)
            android.media.ExifInterface.ORIENTATION_FLIP_VERTICAL -> m.postScale(1f, -1f)
            android.media.ExifInterface.ORIENTATION_TRANSPOSE -> { m.postRotate(90f); m.postScale(-1f, 1f) }
            android.media.ExifInterface.ORIENTATION_TRANSVERSE -> { m.postRotate(270f); m.postScale(-1f, 1f) }
        }
    } catch (_: Exception) { }
    return m
}

/**
 * Pega a foto salva pela câmera, acha os rostos, desenha o filtro e entrega o arquivo novo em [onDone] (thread principal).
 * Se não achar rosto ou der erro, entrega a foto original.
 */
internal fun qinblyBakeFaceFilter(context: android.content.Context, file: File, filter: String, onDone: (Uri) -> Unit) {
    val main = Handler(Looper.getMainLooper())
    QinblyDogSprites.ensure(context)
    Thread {
        var result: Uri = Uri.fromFile(file)
        try {
            val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(file.absolutePath, opts)
            var sample = 1
            while (max(opts.outWidth, opts.outHeight) / sample > 2200) sample *= 2
            val raw = BitmapFactory.decodeFile(file.absolutePath, BitmapFactory.Options().apply { inSampleSize = sample })
            if (raw != null) {
                val m = qinblyExifMatrix(file.absolutePath)
                val upright = if (m.isIdentity) raw else Bitmap.createBitmap(raw, 0, 0, raw.width, raw.height, m, true)
                if (upright !== raw) raw.recycle()
                val detector = FaceDetection.getClient(
                    FaceDetectorOptions.Builder()
                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                        .build()
                )
                val faces = Tasks.await(detector.process(InputImage.fromBitmap(upright, 0)))
                detector.close()
                val eyes = faces.mapNotNull { qinblyEyesOf(it) }
                if (eyes.isNotEmpty()) {
                    val img = ImageBitmap(upright.width, upright.height)
                    val canvas = androidx.compose.ui.graphics.Canvas(img)
                    CanvasDrawScope().draw(Density(1f), LayoutDirection.Ltr, canvas, Size(upright.width.toFloat(), upright.height.toFloat())) {
                        drawImage(upright.asImageBitmap())
                        eyes.forEach { (a, b) -> qinblyDrawFaceFilter(filter, a, b) }
                    }
                    val out = File(context.cacheDir, "qinbly-face-${System.currentTimeMillis()}.jpg")
                    FileOutputStream(out).use { img.asAndroidBitmap().compress(Bitmap.CompressFormat.JPEG, 94, it) }
                    result = Uri.fromFile(out)
                }
                upright.recycle()
            }
        } catch (_: Throwable) { }
        main.post { onDone(result) }
    }.start()
}

// ───────────────────────── Câmera simples (live) ─────────────────────────

/**
 * Prévia de câmera com filtro de cor e filtro de rosto, para telas simples (a live).
 * A permissão da câmera precisa estar concedida antes de chamar.
 */
@Composable
internal fun QinblyFaceCameraPreview(
    faceFilter: String,
    colorMatrix: FloatArray?,
    front: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val state = remember { QinblyFaceState() }
    val tracker = remember { QinblyFaceTracker3D(context, state.track) }
    val analyzer = remember { QinblyFaceAnalyzer(state, tracker) }
    val executor = remember { Executors.newSingleThreadExecutor() }
    var view by remember { mutableStateOf<PreviewView?>(null) }
    analyzer.enabled = faceFilter != "Nenhum"
    analyzer.use3d = faceFilter == "Cachorro"
    DisposableEffect(view, front) {
        val pv = view
        var provider: ProcessCameraProvider? = null
        if (pv != null) {
            val future = ProcessCameraProvider.getInstance(context)
            future.addListener({
                try {
                    val p = future.get()
                    provider = p
                    val preview = Preview.Builder().build().also { it.setSurfaceProvider(pv.surfaceProvider) }
                    val analysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build().also { it.setAnalyzer(executor, analyzer) }
                    val selector = CameraSelector.Builder()
                        .requireLensFacing(if (front) CameraSelector.LENS_FACING_FRONT else CameraSelector.LENS_FACING_BACK).build()
                    p.unbindAll()
                    p.bindToLifecycle(lifecycleOwner, selector, preview, analysis)
                } catch (_: Exception) { }
            }, androidx.core.content.ContextCompat.getMainExecutor(context))
        }
        onDispose {
            try { provider?.unbindAll() } catch (_: Exception) { }
        }
    }
    androidx.compose.foundation.layout.Box(modifier) {
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).also {
                    it.scaleType = PreviewView.ScaleType.FILL_CENTER
                    it.implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    view = it
                }
            },
            modifier = Modifier.matchParentSize(),
            update = { v ->
                try {
                    if (colorMatrix == null) v.setLayerType(android.view.View.LAYER_TYPE_NONE, null)
                    else v.setLayerType(android.view.View.LAYER_TYPE_HARDWARE, android.graphics.Paint().apply {
                        colorFilter = android.graphics.ColorMatrixColorFilter(colorMatrix)
                    })
                } catch (_: Exception) { }
            }
        )
        QinblyFaceOverlay(faceFilter, state, front, Modifier.matchParentSize())
    }
}
