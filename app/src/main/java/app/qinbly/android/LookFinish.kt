package app.qinbly.android

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader

/**
 * Acabamento dos filtros, no estilo dos filtros de foto do Instagram: além da troca de cor, cada filtro pode ter
 * vinheta (bordas mais escuras), granulado e nitidez. [vignette], [grain] e [sharpen] vão de 0 a 1.
 */
internal data class QinblyFinish(val vignette: Float, val grain: Float, val sharpen: Float) {
    val isNone: Boolean get() = vignette <= 0.001f && grain <= 0.001f && sharpen <= 0.001f
}

private val QinblyFinishTable: Map<String, QinblyFinish> = mapOf(
    // Novos, no estilo dos filtros clássicos do Instagram
    "Nítido" to QinblyFinish(0f, 0f, 0.55f),
    "Vintage suave" to QinblyFinish(0.28f, 0.10f, 0.25f),
    "Quente vibrante" to QinblyFinish(0.15f, 0f, 0.45f),
    "Luminoso" to QinblyFinish(0f, 0f, 0.40f),
    "Retrô dourado" to QinblyFinish(0.22f, 0.10f, 0.30f),
    "Dramático Pro" to QinblyFinish(0.55f, 0f, 0.55f),
    "Lo-Fi" to QinblyFinish(0.50f, 0.08f, 0.45f),
    "Poeirento" to QinblyFinish(0.18f, 0.18f, 0.20f),
    "Lua" to QinblyFinish(0.32f, 0.10f, 0.50f),
    "Tinta" to QinblyFinish(0.22f, 0f, 0.55f),
    // Antigos que ganham acabamento
    "Filme" to QinblyFinish(0.20f, 0.22f, 0.25f),
    "Vintage" to QinblyFinish(0.28f, 0.20f, 0.25f),
    "Retrô" to QinblyFinish(0.28f, 0.20f, 0.25f),
    "Cinema" to QinblyFinish(0.30f, 0.08f, 0.35f),
    "Noir" to QinblyFinish(0.35f, 0.12f, 0.45f),
    "Polaroid" to QinblyFinish(0.30f, 0.18f, 0.20f),
    "Kodak" to QinblyFinish(0.15f, 0.10f, 0.35f),
    "Lomo" to QinblyFinish(0.55f, 0.15f, 0.40f),
    "VHS" to QinblyFinish(0.20f, 0.35f, 0.10f),
    "Dramático" to QinblyFinish(0.40f, 0f, 0.50f),
    "Alto contraste" to QinblyFinish(0.30f, 0f, 0.55f),
    "Prata" to QinblyFinish(0.25f, 0.10f, 0.45f),
    "Sépia" to QinblyFinish(0.25f, 0.20f, 0.25f),
    "Granulado" to QinblyFinish(0f, 0.40f, 0.20f),
    "Vinheta" to QinblyFinish(0.60f, 0f, 0.20f)
)

/** Acabamento do filtro [name], reduzido pela intensidade escolhida (0 a 1). "Original" não muda nada. */
internal fun qinblyFinishOf(name: String, intensity: Float): QinblyFinish {
    if (name == "Original") return QinblyFinish(0f, 0f, 0f)
    val base = QinblyFinishTable[name] ?: QinblyFinish(0f, 0f, 0.30f)
    val k = intensity.coerceIn(0f, 1f)
    return QinblyFinish(base.vignette * k, base.grain * k, base.sharpen * k)
}

/** Nitidez (máscara de desfoque): realça os detalhes somando a diferença entre a foto e uma versão levemente desfocada. */
internal fun qinblySharpen(bitmap: Bitmap, amount: Float, radius: Int = 2) {
    if (amount <= 0.001f) return
    val w = bitmap.width
    val h = bitmap.height
    if (w < 8 || h < 8) return
    val px = IntArray(w * h)
    bitmap.getPixels(px, 0, w, 0, 0, w, h)
    val channel = IntArray(w * h)
    val tmp = IntArray(w * h)
    for (shift in intArrayOf(16, 8, 0)) {
        for (i in px.indices) channel[i] = (px[i] shr shift) and 0xFF
        qinblyBoxBlur(channel, tmp, w, h, radius)
        // tmp = versão desfocada. Ignora diferenças mínimas para não realçar ruído.
        for (i in px.indices) {
            val orig = (px[i] shr shift) and 0xFF
            val diff = orig - tmp[i]
            val out = if (diff > -3 && diff < 3) orig else (orig + amount * diff).toInt().coerceIn(0, 255)
            px[i] = (px[i] and (0xFF shl shift).inv()) or (out shl shift)
        }
    }
    bitmap.setPixels(px, 0, w, 0, 0, w, h)
}

/** Desfoque de caixa (horizontal e vertical) com soma corrida: custo baixo mesmo em fotos grandes. Resultado em [out]. */
private fun qinblyBoxBlur(src: IntArray, out: IntArray, w: Int, h: Int, r: Int) {
    val mid = IntArray(w * h)
    val size = 2 * r + 1
    for (y in 0 until h) {
        val row = y * w
        var sum = 0
        for (x in -r..r) sum += src[row + x.coerceIn(0, w - 1)]
        for (x in 0 until w) {
            mid[row + x] = sum / size
            sum += src[row + (x + r + 1).coerceAtMost(w - 1)] - src[row + (x - r).coerceAtLeast(0)]
        }
    }
    for (x in 0 until w) {
        var sum = 0
        for (y in -r..r) sum += mid[y.coerceIn(0, h - 1) * w + x]
        for (y in 0 until h) {
            out[y * w + x] = sum / size
            sum += mid[(y + r + 1).coerceAtMost(h - 1) * w + x] - mid[(y - r).coerceAtLeast(0) * w + x]
        }
    }
}

/** Aplica nitidez, granulado e vinheta (nessa ordem) direto na imagem [bitmap], que precisa ser mutável. */
internal fun qinblyApplyFinish(bitmap: Bitmap, finish: QinblyFinish) {
    if (finish.isNone) return
    // Raio proporcional ao tamanho da foto para a nitidez parecer igual em fotos pequenas e grandes.
    qinblySharpen(bitmap, finish.sharpen, if (maxOf(bitmap.width, bitmap.height) > 2400) 3 else 2)
    if (finish.grain > 0.001f) {
        val w = bitmap.width
        val h = bitmap.height
        val px = IntArray(w * h)
        bitmap.getPixels(px, 0, w, 0, 0, w, h)
        val rnd = java.util.Random(7L)
        val strength = finish.grain * 38f
        for (i in px.indices) {
            val n = ((rnd.nextFloat() - 0.5f) * strength).toInt()
            val p = px[i]
            val r = (((p shr 16) and 0xFF) + n).coerceIn(0, 255)
            val g = (((p shr 8) and 0xFF) + n).coerceIn(0, 255)
            val b = ((p and 0xFF) + n).coerceIn(0, 255)
            px[i] = (p and 0xFF000000.toInt()) or (r shl 16) or (g shl 8) or b
        }
        bitmap.setPixels(px, 0, w, 0, 0, w, h)
    }
    if (finish.vignette > 0.001f) {
        val w = bitmap.width.toFloat()
        val h = bitmap.height.toFloat()
        val radius = Math.hypot((w / 2f).toDouble(), (h / 2f).toDouble()).toFloat()
        val edge = android.graphics.Color.argb((finish.vignette * 0.8f * 255f).toInt().coerceIn(0, 255), 0, 0, 0)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(w / 2f, h / 2f, radius, intArrayOf(0x00000000, 0x00000000, edge), floatArrayOf(0f, 0.5f, 1f), Shader.TileMode.CLAMP)
        }
        Canvas(bitmap).drawRect(0f, 0f, w, h, paint)
    }
}
