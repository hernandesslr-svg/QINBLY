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
/**
 * Tudo que o editor pode aplicar numa foto, sempre a partir da foto ORIGINAL (nada acumula):
 * retoque (liga/desliga), HDR (0 = desligado, 0..1 = intensidade) e fundo (bgMode vazio = sem troca de fundo).
 * bgMode: blur, bokeh, motion, bw, dim, light, color, gradient, scene, image.
 */
internal data class QinblyFxSpec(
    val retouch: Boolean = false,
    val hdr: Float = 0f,
    val bgMode: String = "",
    val bgIntensity: Float = 0.5f,
    val bgColor: Int = 0xFF222222.toInt(),
    val bgIndex: Int = 0,
    val bgImage: Uri? = null
) {
    val isEmpty: Boolean get() = !retouch && hdr <= 0f && bgMode.isBlank()
}

internal val QinblyBgGradients: List<Pair<Int, Int>> = listOf(
    0xFFFF9A8B.toInt() to 0xFFFF6A88.toInt(), 0xFF4FACFE.toInt() to 0xFF00F2FE.toInt(), 0xFF43E97B.toInt() to 0xFF38F9D7.toInt(),
    0xFF7F00FF.toInt() to 0xFFE100FF.toInt(), 0xFFFDC830.toInt() to 0xFFF37335.toInt(), 0xFF141E30.toInt() to 0xFF243B55.toInt()
)
internal val QinblyBgSceneNames = listOf("Pôr do sol", "Noite estrelada", "Cidade à noite", "Neon")

internal fun qinblyApplyPhotoFx(context: Context, source: Uri, spec: QinblyFxSpec, onDone: (Uri?, String) -> Unit) {
    val main = Handler(Looper.getMainLooper())
    Thread {
        var uri: Uri? = null
        var note = ""
        try {
            val notes = ArrayList<String>()
            uri = qinblyPhotoFxSync(context, source, spec, notes)
            note = notes.joinToString(" ")
        } catch (t: Throwable) {
            note = "Efeito falhou: ${t.javaClass.simpleName}${t.message?.let { ": " + it.take(90) } ?: ""}"
        }
        main.post { onDone(uri, note) }
    }.start()
}

internal fun qinblyApplyPhotoEffects(
    context: Context, source: Uri, retouch: Boolean, blurBackground: Boolean, hdr: Boolean,
    onDone: (Uri?, String) -> Unit
) = qinblyApplyPhotoFx(context, source, QinblyFxSpec(retouch = retouch, hdr = if (hdr) 1f else 0f, bgMode = if (blurBackground) "blur" else "", bgIntensity = 0.5f), onDone)

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

private fun fxApplyHdr(bmp: Bitmap, k: Float = 1f) {
    val lut = IntArray(256) { i ->
        val y = i / 255f
        val lifted = y.toDouble().pow((1.0 - 0.32 * k)).toFloat()
        val s = lifted * lifted * (3f - 2f * lifted)
        val mixS = 0.55f * k
        ((lifted * (1f - mixS) + s * mixS) * 255f).roundToInt().coerceIn(0, 255)
    }
    val w = bmp.width
    val h = bmp.height
    val px = IntArray(w * h)
    bmp.getPixels(px, 0, w, 0, 0, w, h)
    for (i in px.indices) {
        val p = px[i]
        px[i] = (p and 0xFF000000.toInt()) or (lut[(p shr 16) and 0xFF] shl 16) or (lut[(p shr 8) and 0xFF] shl 8) or lut[p and 0xFF]
    }
    // Contraste local (o "HDR" que se vê): realça textura e detalhe comparando cada pixel com a vizinhança borrada.
    val lifted = bmp.copy(Bitmap.Config.ARGB_8888, true)
    lifted.setPixels(px, 0, w, 0, 0, w, h)
    val soft = fxSoftBlur(lifted, 16)
    lifted.recycle()
    val bl = IntArray(w * h)
    soft.getPixels(bl, 0, w, 0, 0, w, h)
    soft.recycle()
    val amt = 0.85f * k
    for (i in px.indices) {
        val p = px[i]; val q = bl[i]
        val r = (p shr 16) and 0xFF; val g = (p shr 8) and 0xFF; val b = p and 0xFF
        val nr = (r + amt * (r - ((q shr 16) and 0xFF))).roundToInt().coerceIn(0, 255)
        val ng = (g + amt * (g - ((q shr 8) and 0xFF))).roundToInt().coerceIn(0, 255)
        val nb = (b + amt * (b - (q and 0xFF))).roundToInt().coerceIn(0, 255)
        px[i] = (p and 0xFF000000.toInt()) or (nr shl 16) or (ng shl 8) or nb
    }
    bmp.setPixels(px, 0, w, 0, 0, w, h)
    val saturated = Paint().apply { colorFilter = ColorMatrixColorFilter(ColorMatrix().apply { setSaturation(1f + 0.22f * k) }) }
    val copy = bmp.copy(Bitmap.Config.ARGB_8888, false)
    Canvas(bmp).drawBitmap(copy, 0f, 0f, saturated)
    copy.recycle()
}

/**
 * Retoque de pele de verdade: só suaviza os pixels de PELE dentro de cada rosto e preserva as bordas
 * (olhos, sobrancelhas, boca, cabelo, contorno ficam nítidos). Aplicado uma única vez a partir da foto original.
 */
private fun fxRetouch(bmp: Bitmap, notes: MutableList<String>) {
    val detector = FaceDetection.getClient(
        FaceDetectorOptions.Builder().setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE).build()
    )
    val faces = try { Tasks.await(detector.process(InputImage.fromBitmap(bmp, 0))) } finally { detector.close() }
    if (faces.isEmpty()) { notes.add("Não achei rosto para o retoque."); return }
    val soft = fxSoftBlur(bmp, 4)
    val bw = bmp.width
    val bh = bmp.height
    faces.forEach { face ->
        val box = face.boundingBox
        val cx = box.exactCenterX()
        val cy = box.exactCenterY()
        val rx = box.width() * 0.62f
        val ry = box.height() * 0.70f
        val x0 = max(0, (cx - rx).toInt()); val y0 = max(0, (cy - ry).toInt())
        val x1 = minOf(bw, (cx + rx).toInt() + 1); val y1 = minOf(bh, (cy + ry).toInt() + 1)
        val rw = x1 - x0; val rh = y1 - y0
        if (rw <= 2 || rh <= 2) return@forEach
        val orig = IntArray(rw * rh); val blur = IntArray(rw * rh)
        bmp.getPixels(orig, 0, rw, x0, y0, rw, rh)
        soft.getPixels(blur, 0, rw, x0, y0, rw, rh)
        for (yy in 0 until rh) for (xx in 0 until rw) {
            val i = yy * rw + xx
            val dx = ((x0 + xx) - cx) / rx; val dy = ((y0 + yy) - cy) / ry
            val d2 = dx * dx + dy * dy
            if (d2 >= 1f) continue
            val p = orig[i]
            val r = (p shr 16) and 0xFF; val g = (p shr 8) and 0xFF; val b = p and 0xFF
            // Só pele: faixa Cb/Cr de pele (YCbCr). Olhos, boca, sobrancelhas e cabelo ficam de fora.
            val cb = 128f - 0.168736f * r - 0.331264f * g + 0.5f * b
            val cr = 128f + 0.5f * r - 0.418688f * g - 0.081312f * b
            if (cb < 77f || cb > 127f || cr < 133f || cr > 173f) continue
            val q = blur[i]
            val br = (q shr 16) and 0xFF; val bg = (q shr 8) and 0xFF; val bb = q and 0xFF
            val lo = 0.299f * r + 0.587f * g + 0.114f * b
            val lb = 0.299f * br + 0.587f * bg + 0.114f * bb
            // Borda forte (diferença grande entre o pixel e a versão borrada) fica como está; só o "ruído" fino da pele é alisado.
            val edge = kotlin.math.abs(lo - lb)
            val keep = (1f - edge / 26f).coerceIn(0f, 1f)
            val falloff = (1f - d2).coerceIn(0f, 1f).let { it * it * (3f - 2f * it) }
            val w = 0.85f * keep * falloff
            if (w <= 0f) continue
            val nr = (r + (br - r) * w).roundToInt().coerceIn(0, 255)
            val ng = (g + (bg - g) * w).roundToInt().coerceIn(0, 255)
            val nb = (b + (bb - b) * w).roundToInt().coerceIn(0, 255)
            orig[i] = (p and 0xFF000000.toInt()) or (nr shl 16) or (ng shl 8) or nb
        }
        bmp.setPixels(orig, 0, rw, x0, y0, rw, rh)
    }
    soft.recycle()
}

private fun qinblyPhotoFxSync(context: Context, source: Uri, spec: QinblyFxSpec, notes: MutableList<String>): Uri {
    var bmp = fxLoadUpright(context, source)
    // Cada efeito é isolado: se um falhar, os outros continuam aplicados e o aviso diz qual falhou.
    if (spec.hdr > 0f) try { fxApplyHdr(bmp, spec.hdr.coerceIn(0.1f, 1f)) } catch (t: Throwable) { notes.add("HDR falhou: ${t.javaClass.simpleName}.") }
    if (spec.retouch) try { fxRetouch(bmp, notes) } catch (t: Throwable) { notes.add("Retoque falhou: ${t.javaClass.simpleName}.") }
    if (spec.bgMode.isNotBlank()) try { bmp = fxReplaceBackground(context, bmp, spec, notes) } catch (t: Throwable) { notes.add("Fundo falhou: ${t.javaClass.simpleName}${t.message?.let { ": " + it.take(60) } ?: ""}.") }
    val out = File(context.cacheDir, "qinbly-fx-${System.currentTimeMillis()}.jpg")
    FileOutputStream(out).use { bmp.compress(Bitmap.CompressFormat.JPEG, 94, it) }
    bmp.recycle()
    return Uri.fromFile(out)
}

/** Soma de caixa (média numa janela de raio r), em tempo linear. Bordas: média só do que existe na janela. */
private fun fxBoxBlur(src: FloatArray, w: Int, h: Int, r: Int): FloatArray {
    val tmp = FloatArray(w * h)
    val out = FloatArray(w * h)
    val pre = DoubleArray(max(w, h) + 1)
    for (y in 0 until h) {
        val row = y * w
        pre[0] = 0.0
        for (x in 0 until w) pre[x + 1] = pre[x] + src[row + x]
        for (x in 0 until w) {
            val lo = max(0, x - r); val hi = minOf(w - 1, x + r)
            tmp[row + x] = ((pre[hi + 1] - pre[lo]) / (hi - lo + 1)).toFloat()
        }
    }
    for (x in 0 until w) {
        pre[0] = 0.0
        for (y in 0 until h) pre[y + 1] = pre[y] + tmp[y * w + x]
        for (y in 0 until h) {
            val lo = max(0, y - r); val hi = minOf(h - 1, y + r)
            out[y * w + x] = ((pre[hi + 1] - pre[lo]) / (hi - lo + 1)).toFloat()
        }
    }
    return out
}

/** Filtro guiado: faz a borda da máscara [p] seguir os contornos reais da foto [guide] (cabelo, braço, cintura). */
private fun fxGuidedFilter(guide: FloatArray, p: FloatArray, w: Int, h: Int, r: Int, eps: Float): FloatArray {
    val n = w * h
    val meanI = fxBoxBlur(guide, w, h, r)
    val meanP = fxBoxBlur(p, w, h, r)
    val corrIp = fxBoxBlur(FloatArray(n) { guide[it] * p[it] }, w, h, r)
    val corrI = fxBoxBlur(FloatArray(n) { guide[it] * guide[it] }, w, h, r)
    val a = FloatArray(n)
    val b = FloatArray(n)
    for (i in 0 until n) {
        val varI = corrI[i] - meanI[i] * meanI[i]
        val cov = corrIp[i] - meanI[i] * meanP[i]
        a[i] = cov / (varI + eps)
        b[i] = meanP[i] - a[i] * meanI[i]
    }
    val ma = fxBoxBlur(a, w, h, r)
    val mb = fxBoxBlur(b, w, h, r)
    return FloatArray(n) { (ma[it] * guide[it] + mb[it]).coerceIn(0f, 1f) }
}

/** Preenche com o vizinho de 4 lados todos os pixels ligados a [start] com o valor [target]; devolve quantos achou (os índices ficam em [queue]). */
private fun fxFlood(start: Int, w: Int, h: Int, cond: BooleanArray, target: Boolean, labels: IntArray, label: Int, queue: IntArray): Int {
    var head = 0
    var tail = 0
    queue[tail++] = start
    labels[start] = label
    while (head < tail) {
        val idx = queue[head++]
        val x = idx % w
        val y = idx / w
        if (x > 0) { val j = idx - 1; if (labels[j] == 0 && cond[j] == target) { labels[j] = label; queue[tail++] = j } }
        if (x < w - 1) { val j = idx + 1; if (labels[j] == 0 && cond[j] == target) { labels[j] = label; queue[tail++] = j } }
        if (y > 0) { val j = idx - w; if (labels[j] == 0 && cond[j] == target) { labels[j] = label; queue[tail++] = j } }
        if (y < h - 1) { val j = idx + w; if (labels[j] == 0 && cond[j] == target) { labels[j] = label; queue[tail++] = j } }
    }
    return tail
}

/** Limpa a máscara binária: fecha falhas, tapa buracos pequenos dentro da pessoa e tira manchas soltas no fundo. */
private fun fxCleanBinary(bin: BooleanArray, w: Int, h: Int): BooleanArray {
    val n = w * h
    val rr = max(2, (max(w, h) * 0.012f).toInt())
    val dil = fxBoxBlur(FloatArray(n) { if (bin[it]) 1f else 0f }, w, h, rr)
    val ero = fxBoxBlur(FloatArray(n) { if (dil[it] > 0.0005f) 1f else 0f }, w, h, rr)
    val m = BooleanArray(n) { bin[it] || ero[it] > 0.9995f }
    val queue = IntArray(n)
    // Buracos: fundo que NÃO encosta na borda da foto e é pequeno vira pessoa (ex.: pedaço do corpo que o modelo errou).
    val bg = IntArray(n)
    for (x in 0 until w) {
        if (!m[x] && bg[x] == 0) fxFlood(x, w, h, m, false, bg, 1, queue)
        val l = (h - 1) * w + x
        if (!m[l] && bg[l] == 0) fxFlood(l, w, h, m, false, bg, 1, queue)
    }
    for (y in 0 until h) {
        val l = y * w
        if (!m[l] && bg[l] == 0) fxFlood(l, w, h, m, false, bg, 1, queue)
        val rgt = y * w + w - 1
        if (!m[rgt] && bg[rgt] == 0) fxFlood(rgt, w, h, m, false, bg, 1, queue)
    }
    var label = 2
    for (i in 0 until n) {
        if (!m[i] && bg[i] == 0) {
            val count = fxFlood(i, w, h, m, false, bg, label++, queue)
            if (count < n * 0.06f) for (k in 0 until count) m[queue[k]] = true
        }
    }
    // Manchas soltas: pedaço de "pessoa" pequeno e longe do corpo principal vira fundo.
    val fg = IntArray(n)
    val sizes = ArrayList<Int>()
    sizes.add(0)
    var biggest = 0
    for (i in 0 until n) {
        if (m[i] && fg[i] == 0) {
            val count = fxFlood(i, w, h, m, true, fg, sizes.size, queue)
            sizes.add(count)
            if (count > biggest) biggest = count
        }
    }
    for (i in 0 until n) {
        val l = fg[i]
        if (l > 0 && sizes[l] < n * 0.02f && sizes[l] < biggest * 0.3f) m[i] = false
    }
    return m
}

/** Máscara da pessoa (alfa) do tamanho da foto, ou null se não achar uma pessoa. Refinada: sem buracos, sem halo, borda no contorno real. */
internal fun fxPersonMask(bmp: Bitmap): Bitmap? {
    val segmenter = Segmentation.getClient(
        SelfieSegmenterOptions.Builder().setDetectorMode(SelfieSegmenterOptions.SINGLE_IMAGE_MODE).build()
    )
    val mask = try { Tasks.await(segmenter.process(InputImage.fromBitmap(bmp, 0))) } finally { segmenter.close() }
    val mw = mask.width
    val mh = mask.height
    val buf = mask.buffer
    buf.rewind()
    val conf = FloatArray(mw * mh)
    var person = 0
    for (i in conf.indices) {
        val c = buf.getFloat().coerceIn(0f, 1f)
        conf[i] = c
        if (c > 0.5f) person++
    }
    if (person < conf.size / 50) return null
    // Trabalha numa versão menor (rápido) e depois amplia o resultado para o tamanho da foto.
    val big = max(bmp.width, bmp.height)
    val scale = if (big > 960) 960f / big else 1f
    val w = max(8, (bmp.width * scale).roundToInt())
    val h = max(8, (bmp.height * scale).roundToInt())
    val bin = BooleanArray(w * h)
    for (y in 0 until h) {
        val sy = minOf(mh - 1, (y.toFloat() / h * mh).toInt())
        for (x in 0 until w) {
            val sx = minOf(mw - 1, (x.toFloat() / w * mw).toInt())
            bin[y * w + x] = conf[sy * mw + sx] > 0.5f
        }
    }
    val clean = fxCleanBinary(bin, w, h)
    val small = Bitmap.createScaledBitmap(bmp, w, h, true)
    val px = IntArray(w * h)
    small.getPixels(px, 0, w, 0, 0, w, h)
    if (small !== bmp) small.recycle()
    val guide = FloatArray(w * h) {
        val p = px[it]
        (0.299f * ((p shr 16) and 0xFF) + 0.587f * ((p shr 8) and 0xFF) + 0.114f * (p and 0xFF)) / 255f
    }
    val radius = max(3, (max(w, h) * 0.008f).toInt())
    val q = fxGuidedFilter(guide, FloatArray(w * h) { if (clean[it]) 1f else 0f }, w, h, radius, 0.0008f)
    // Curva firme (0,38 → 0,80): tira o halo do fundo antigo e deixa a borda definida, sem serrilhado.
    val out = IntArray(w * h) {
        val t = ((q[it] - 0.38f) / 0.42f).coerceIn(0f, 1f)
        val a = (t * t * (3f - 2f * t) * 255f).roundToInt().coerceIn(0, 255)
        (a shl 24) or 0x00FFFFFF
    }
    val maskSmall = Bitmap.createBitmap(out, w, h, Bitmap.Config.ARGB_8888)
    val maskFull = Bitmap.createScaledBitmap(maskSmall, bmp.width, bmp.height, true)
    // Se a máscara já tem o tamanho da foto, createScaledBitmap devolve a MESMA imagem: reciclar aqui matava a máscara ("recycled bitmap").
    if (maskFull !== maskSmall) maskSmall.recycle()
    return maskFull
}

private fun fxScene(w: Int, h: Int, index: Int): Bitmap {
    val out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    val c = Canvas(out)
    val p = Paint(Paint.ANTI_ALIAS_FLAG)
    val fw = w.toFloat(); val fh = h.toFloat()
    fun sky(top: Int, bottom: Int) { p.shader = android.graphics.LinearGradient(0f, 0f, 0f, fh, top, bottom, Shader.TileMode.CLAMP); c.drawRect(0f, 0f, fw, fh, p); p.shader = null }
    when (index) {
        0 -> { // pôr do sol
            sky(0xFF2B1055.toInt(), 0xFFFF9966.toInt())
            p.color = 0xFFFFE08A.toInt(); c.drawCircle(fw * 0.5f, fh * 0.62f, fw * 0.16f, p)
            p.color = 0xFF1B1030.toInt(); c.drawRect(0f, fh * 0.78f, fw, fh, p)
        }
        1 -> { // noite estrelada
            sky(0xFF050A24.toInt(), 0xFF1B2A5C.toInt())
            p.color = Color_WHITE
            val rnd = java.util.Random(7)
            for (i in 0 until 90) { p.alpha = 120 + rnd.nextInt(135); c.drawCircle(rnd.nextFloat() * fw, rnd.nextFloat() * fh * 0.8f, 1.5f + rnd.nextFloat() * 3f, p) }
            p.alpha = 255; p.color = 0xFFF5F3CE.toInt(); c.drawCircle(fw * 0.78f, fh * 0.16f, fw * 0.06f, p)
        }
        2 -> { // cidade à noite
            sky(0xFF0B1026.toInt(), 0xFF3A2D5F.toInt())
            val rnd = java.util.Random(11)
            var x = 0f
            while (x < fw) {
                val bwid = fw * (0.06f + rnd.nextFloat() * 0.08f); val bh = fh * (0.22f + rnd.nextFloat() * 0.3f)
                p.color = 0xFF0A0D1A.toInt(); c.drawRect(x, fh - bh, x + bwid, fh, p)
                p.color = 0xFFFFD66B.toInt()
                var wy = fh - bh + bwid * 0.2f
                while (wy < fh - bwid * 0.3f) { var wx = x + bwid * 0.15f; while (wx < x + bwid * 0.8f) { if (rnd.nextInt(3) != 0) c.drawRect(wx, wy, wx + bwid * 0.1f, wy + bwid * 0.12f, p); wx += bwid * 0.22f }; wy += bwid * 0.28f }
                x += bwid
            }
        }
        else -> { // neon
            sky(0xFF14002E.toInt(), 0xFF00122E.toInt())
            p.style = Paint.Style.STROKE; p.strokeWidth = fw * 0.012f
            val cols = intArrayOf(0xFFFF2A9D.toInt(), 0xFF00E5FF.toInt(), 0xFF9B4DFF.toInt())
            for (i in 0 until 9) { p.color = cols[i % 3]; c.drawLine(-fw * 0.1f, fh * (0.1f + i * 0.1f), fw * 1.1f, fh * (0.0f + i * 0.12f), p) }
            p.style = Paint.Style.FILL
        }
    }
    return out
}
private const val Color_WHITE = -1

/** Troca/modifica só o FUNDO e mantém a pessoa nítida na frente. Se não achar pessoa, devolve a foto como está. */
private fun fxReplaceBackground(context: Context, bmp: Bitmap, spec: QinblyFxSpec, notes: MutableList<String>): Bitmap {
    val mask = fxPersonMask(bmp)
    if (mask == null) { notes.add("Não achei uma pessoa para separar do fundo."); return bmp }
    val w = bmp.width; val h = bmp.height
    val k = spec.bgIntensity.coerceIn(0f, 1f)
    val bg: Bitmap = when (spec.bgMode) {
        "blur" -> fxSoftBlur(bmp, (4 + (28 * k)).roundToInt().coerceIn(4, 32)).copy(Bitmap.Config.ARGB_8888, true)
        "bokeh" -> {
            val b = fxSoftBlur(bmp, (8 + (24 * k)).roundToInt().coerceIn(8, 32)).copy(Bitmap.Config.ARGB_8888, true)
            val c = Canvas(b); val p = Paint(Paint.ANTI_ALIAS_FLAG); val rnd = java.util.Random(5)
            for (i in 0 until 26) {
                val rr = w * (0.025f + rnd.nextFloat() * 0.05f)
                p.color = 0xFFFFFFFF.toInt(); p.alpha = 40 + rnd.nextInt(60)
                c.drawCircle(rnd.nextFloat() * w, rnd.nextFloat() * h, rr, p)
            }
            b
        }
        "motion" -> {
            val steps = (6 + (30 * k)).roundToInt().coerceIn(6, 36)
            val small = Bitmap.createScaledBitmap(bmp, max(2, w / steps), h, true)
            Bitmap.createScaledBitmap(small, w, h, true).also { small.recycle() }.copy(Bitmap.Config.ARGB_8888, true)
        }
        "bw" -> {
            val o = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            Canvas(o).drawBitmap(bmp, 0f, 0f, Paint().apply { colorFilter = ColorMatrixColorFilter(ColorMatrix().apply { setSaturation(0f) }) })
            o
        }
        "dim" -> bmp.copy(Bitmap.Config.ARGB_8888, true).also { Canvas(it).drawColor(android.graphics.Color.argb((40 + 180 * k).toInt().coerceIn(0, 255), 0, 0, 0)) }
        "light" -> bmp.copy(Bitmap.Config.ARGB_8888, true).also { Canvas(it).drawColor(android.graphics.Color.argb((40 + 160 * k).toInt().coerceIn(0, 255), 255, 255, 255)) }
        "color" -> Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888).also { it.eraseColor(spec.bgColor or 0xFF000000.toInt()) }
        "gradient" -> {
            val g = QinblyBgGradients[spec.bgIndex.coerceIn(0, QinblyBgGradients.lastIndex)]
            Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888).also {
                Canvas(it).drawRect(0f, 0f, w.toFloat(), h.toFloat(), Paint().apply { shader = android.graphics.LinearGradient(0f, 0f, w.toFloat() * 0.4f, h.toFloat(), g.first, g.second, Shader.TileMode.CLAMP) })
            }
        }
        "scene" -> fxScene(w, h, spec.bgIndex)
        "image" -> {
            val img = spec.bgImage?.let { try { fxLoadUpright(context, it) } catch (_: Throwable) { null } }
            if (img == null) { notes.add("Não consegui abrir a foto de fundo."); mask.recycle(); return bmp }
            val scale = max(w.toFloat() / img.width, h.toFloat() / img.height)
            val o = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            val dw = img.width * scale; val dh = img.height * scale
            Canvas(o).drawBitmap(img, null, android.graphics.RectF((w - dw) / 2f, (h - dh) / 2f, (w + dw) / 2f, (h + dh) / 2f), Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
            img.recycle(); o
        }
        else -> bmp.copy(Bitmap.Config.ARGB_8888, true)
    }
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        shader = ComposeShader(
            BitmapShader(bmp, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP),
            BitmapShader(mask, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP),
            PorterDuff.Mode.DST_IN
        )
    }
    Canvas(bg).drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)
    mask.recycle()
    bmp.recycle()
    return bg
}

/** Recorta a pessoa da foto: devolve uma imagem com fundo transparente, ou null se não achar uma pessoa. */
internal fun fxPersonCutout(bmp: Bitmap): Bitmap? {
    val maskFull = fxPersonMask(bmp) ?: return null
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
