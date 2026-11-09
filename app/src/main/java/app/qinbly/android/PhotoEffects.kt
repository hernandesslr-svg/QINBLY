package app.qinbly.android

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.ComposeShader
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.RadialGradient
import android.graphics.Shader
import android.net.Uri
import android.os.Handler
import android.os.Looper
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.segmentation.Segmentation
import com.google.mlkit.vision.segmentation.selfie.SelfieSegmenterOptions
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Efeitos de foto feitos no próprio aparelho:
 *  - Retoque: suaviza a pele só dentro dos rostos achados (borda com transição suave);
 *  - Fundo: mantém a pessoa nítida e desfoca o resto (segmentação de pessoa do ML Kit);
 *  - HDR: clareia sombras e comprime brilhos, com um pouco mais de cor (realce, não HDR de sensor).
 * Roda fora da tela principal; o resultado volta em [onDone] na thread principal.
 * [onDone] recebe o arquivo novo (ou null se falhou) e um aviso para mostrar à pessoa (pode ser vazio).
 */
internal fun qinblyApplyPhotoEffects(
    context: Context, source: Uri, retouch: Boolean, blurBackground: Boolean, hdr: Boolean,
    onDone: (Uri?, String) -> Unit
) {
    val main = Handler(Looper.getMainLooper())
    Thread {
        var uri: Uri? = null
        var note = ""
        try {
            val notes = ArrayList<String>()
            uri = qinblyPhotoEffectsSync(context, source, retouch, blurBackground, hdr, notes)
            note = notes.joinToString(" ")
        } catch (t: Throwable) {
            note = "Não foi possível aplicar o efeito nesta foto."
        }
        main.post { onDone(uri, note) }
    }.start()
}

private fun fxExifMatrix(exif: android.media.ExifInterface): Matrix {
    val m = Matrix()
    when (exif.getAttributeInt(android.media.ExifInterface.TAG_ORIENTATION, android.media.ExifInterface.ORIENTATION_NORMAL)) {
        android.media.ExifInterface.ORIENTATION_ROTATE_90 -> m.postRotate(90f)
        android.media.ExifInterface.ORIENTATION_ROTATE_180 -> m.postRotate(180f)
        android.media.ExifInterface.ORIENTATION_ROTATE_270 -> m.postRotate(270f)
        android.media.ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> m.postScale(-1f, 1f)
        android.media.ExifInterface.ORIENTATION_FLIP_VERTICAL -> m.postScale(1f, -1f)
    }
    return m
}

internal fun fxLoadUpright(context: Context, source: Uri): Bitmap {
    val resolver = context.contentResolver
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    resolver.openInputStream(source)?.use { BitmapFactory.decodeStream(it, null, bounds) }
    var sample = 1
    while (max(bounds.outWidth, bounds.outHeight) / sample > 2200) sample *= 2
    val raw = resolver.openInputStream(source)?.use { BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample }) }
        ?: throw IllegalStateException("sem imagem")
    val matrix = try {
        resolver.openInputStream(source)?.use { fxExifMatrix(android.media.ExifInterface(it)) } ?: Matrix()
    } catch (_: Exception) { Matrix() }
    val upright = if (matrix.isIdentity) raw else Bitmap.createBitmap(raw, 0, 0, raw.width, raw.height, matrix, true)
    if (upright !== raw) raw.recycle()
    return upright.copy(Bitmap.Config.ARGB_8888, true).also { if (upright !== it) upright.recycle() }
}

/** Desfoque suave: encolhe pela metade várias vezes e amplia de volta. [divisor] maior = mais desfoque. */
private fun fxSoftBlur(src: Bitmap, divisor: Int): Bitmap {
    var cur = src
    var f = 1
    while (f < divisor) {
        val next = Bitmap.createScaledBitmap(cur, max(1, cur.width / 2), max(1, cur.height / 2), true)
        if (cur !== src) cur.recycle()
        cur = next
        f *= 2
    }
    val up = Bitmap.createScaledBitmap(cur, src.width, src.height, true)
    if (cur !== src) cur.recycle()
    return up
}

private fun fxApplyHdr(bmp: Bitmap) {
    val lut = IntArray(256) { i ->
        val y = i / 255f
        val lifted = y.toDouble().pow(0.82).toFloat()
        val s = lifted * lifted * (3f - 2f * lifted)
        ((lifted * 0.6f + s * 0.4f) * 255f).roundToInt().coerceIn(0, 255)
    }
    val w = bmp.width
    val h = bmp.height
    val px = IntArray(w * h)
    bmp.getPixels(px, 0, w, 0, 0, w, h)
    for (i in px.indices) {
        val p = px[i]
        px[i] = (p and 0xFF000000.toInt()) or (lut[(p shr 16) and 0xFF] shl 16) or (lut[(p shr 8) and 0xFF] shl 8) or lut[p and 0xFF]
    }
    bmp.setPixels(px, 0, w, 0, 0, w, h)
    val saturated = Paint().apply { colorFilter = ColorMatrixColorFilter(ColorMatrix().apply { setSaturation(1.12f) }) }
    val copy = bmp.copy(Bitmap.Config.ARGB_8888, false)
    Canvas(bmp).drawBitmap(copy, 0f, 0f, saturated)
    copy.recycle()
}

private fun fxRetouch(bmp: Bitmap, notes: MutableList<String>) {
    val detector = FaceDetection.getClient(
        FaceDetectorOptions.Builder().setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE).build()
    )
    val faces = try { Tasks.await(detector.process(InputImage.fromBitmap(bmp, 0))) } finally { detector.close() }
    if (faces.isEmpty()) { notes.add("Não achei rosto para o retoque."); return }
    val soft = fxSoftBlur(bmp, 4)
    val canvas = Canvas(bmp)
    faces.forEach { face ->
        val box = face.boundingBox
        val cx = box.exactCenterX()
        val cy = box.exactCenterY()
        val r = max(box.width(), box.height()) * 0.62f
        val fade = RadialGradient(cx, cy, r, intArrayOf(0xFFFFFFFF.toInt(), 0xFFFFFFFF.toInt(), 0x00FFFFFF), floatArrayOf(0f, 0.55f, 1f), Shader.TileMode.CLAMP)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = ComposeShader(BitmapShader(soft, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP), fade, PorterDuff.Mode.DST_IN)
            alpha = (255 * 0.65f).toInt()
        }
        canvas.drawCircle(cx, cy, r, paint)
    }
    soft.recycle()
}

private fun fxBlurBackground(bmp: Bitmap, notes: MutableList<String>): Bitmap {
    val segmenter = Segmentation.getClient(
        SelfieSegmenterOptions.Builder().setDetectorMode(SelfieSegmenterOptions.SINGLE_IMAGE_MODE).build()
    )
    val mask = try { Tasks.await(segmenter.process(InputImage.fromBitmap(bmp, 0))) } finally { segmenter.close() }
    val mw = mask.width
    val mh = mask.height
    val buf = mask.buffer
    buf.rewind()
    val px = IntArray(mw * mh)
    var person = 0
    for (i in px.indices) {
        val c = buf.getFloat().coerceIn(0f, 1f)
        val a = (c * c * (3f - 2f * c) * 255f).roundToInt().coerceIn(0, 255)
        if (a > 128) person++
        px[i] = (a shl 24) or 0x00FFFFFF
    }
    if (person < px.size / 50) { notes.add("Não achei uma pessoa para separar do fundo."); return bmp }
    val maskSmall = Bitmap.createBitmap(px, mw, mh, Bitmap.Config.ARGB_8888)
    val maskFull = Bitmap.createScaledBitmap(maskSmall, bmp.width, bmp.height, true)
    maskSmall.recycle()
    val out = fxSoftBlur(bmp, 16).copy(Bitmap.Config.ARGB_8888, true)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        shader = ComposeShader(
            BitmapShader(bmp, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP),
            BitmapShader(maskFull, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP),
            PorterDuff.Mode.DST_IN
        )
    }
    Canvas(out).drawRect(0f, 0f, bmp.width.toFloat(), bmp.height.toFloat(), paint)
    maskFull.recycle()
    bmp.recycle()
    return out
}

private fun qinblyPhotoEffectsSync(context: Context, source: Uri, retouch: Boolean, blurBackground: Boolean, hdr: Boolean, notes: MutableList<String>): Uri {
    var bmp = fxLoadUpright(context, source)
    if (hdr) fxApplyHdr(bmp)
    if (retouch) fxRetouch(bmp, notes)
    if (blurBackground) bmp = fxBlurBackground(bmp, notes)
    val out = File(context.cacheDir, "qinbly-fx-${System.currentTimeMillis()}.jpg")
    FileOutputStream(out).use { bmp.compress(Bitmap.CompressFormat.JPEG, 94, it) }
    bmp.recycle()
    return Uri.fromFile(out)
}

/** Recorta a pessoa da foto: devolve uma imagem com fundo transparente, ou null se não achar uma pessoa. */
internal fun fxPersonCutout(bmp: Bitmap): Bitmap? {
    val segmenter = Segmentation.getClient(
        SelfieSegmenterOptions.Builder().setDetectorMode(SelfieSegmenterOptions.SINGLE_IMAGE_MODE).build()
    )
    val mask = try { Tasks.await(segmenter.process(InputImage.fromBitmap(bmp, 0))) } finally { segmenter.close() }
    val mw = mask.width
    val mh = mask.height
    val buf = mask.buffer
    buf.rewind()
    val px = IntArray(mw * mh)
    var person = 0
    for (i in px.indices) {
        val c = buf.getFloat().coerceIn(0f, 1f)
        val a = (c * c * (3f - 2f * c) * 255f).roundToInt().coerceIn(0, 255)
        if (a > 128) person++
        px[i] = (a shl 24) or 0x00FFFFFF
    }
    if (person < px.size / 50) return null
    val maskSmall = Bitmap.createBitmap(px, mw, mh, Bitmap.Config.ARGB_8888)
    val maskFull = Bitmap.createScaledBitmap(maskSmall, bmp.width, bmp.height, true)
    maskSmall.recycle()
    val out = Bitmap.createBitmap(bmp.width, bmp.height, Bitmap.Config.ARGB_8888)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        shader = ComposeShader(
            BitmapShader(bmp, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP),
            BitmapShader(maskFull, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP),
            PorterDuff.Mode.DST_IN
        )
    }
    Canvas(out).drawRect(0f, 0f, bmp.width.toFloat(), bmp.height.toFloat(), paint)
    maskFull.recycle()
    return out
}
