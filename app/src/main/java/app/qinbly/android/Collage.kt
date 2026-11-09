package app.qinbly.android

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.net.Uri
import android.os.Handler
import android.os.Looper
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max

/**
 * Monta a colagem da câmera: 2 fotos (uma em cima da outra) ou 4 fotos (grade 2x2), em 1080x1920.
 * Cada foto é cortada no centro para caber no quadro, com uma fresta branca entre elas.
 * O arquivo novo volta em [onDone] na thread principal (null se der erro).
 */
internal fun qinblyBuildCollage(context: Context, photos: List<Uri>, onDone: (Uri?) -> Unit) {
    val main = Handler(Looper.getMainLooper())
    Thread {
        var result: Uri? = null
        try {
            val w = 1080
            val h = 1920
            val gap = 8f
            val out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(out)
            canvas.drawColor(Color.WHITE)
            val cols = if (photos.size >= 4) 2 else 1
            val rows = if (photos.size >= 4) 2 else photos.size.coerceAtLeast(1)
            val cellW = (w - gap * (cols - 1)) / cols
            val cellH = (h - gap * (rows - 1)) / rows
            val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
            photos.take(cols * rows).forEachIndexed { index, uri ->
                val bmp = fxLoadUpright(context, uri)
                val col = index % cols
                val row = index / cols
                val left = col * (cellW + gap)
                val top = row * (cellH + gap)
                val scale = max(cellW / bmp.width, cellH / bmp.height)
                val srcW = cellW / scale
                val srcH = cellH / scale
                val src = Rect(((bmp.width - srcW) / 2f).toInt(), ((bmp.height - srcH) / 2f).toInt(), ((bmp.width + srcW) / 2f).toInt(), ((bmp.height + srcH) / 2f).toInt())
                canvas.drawBitmap(bmp, src, RectF(left, top, left + cellW, top + cellH), paint)
                bmp.recycle()
            }
            val file = File(context.cacheDir, "qinbly-collage-${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { out.compress(Bitmap.CompressFormat.JPEG, 93, it) }
            out.recycle()
            result = Uri.fromFile(file)
        } catch (_: Throwable) { }
        main.post { onDone(result) }
    }.start()
}
