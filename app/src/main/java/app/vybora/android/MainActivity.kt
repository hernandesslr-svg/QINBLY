package app.vybora.android

import android.os.Bundle
import org.json.JSONArray
import org.json.JSONObject
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.asImageBitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import java.io.FileOutputStream
import android.content.Intent
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import java.io.File
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.zIndex
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Title
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.FilterVintage
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Share
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.SettingsInputAntenna
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.PersonAddAlt1
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.animation.core.animateFloat
import kotlinx.coroutines.launch
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

private val Night = Color.White
private val Panel = Color.White
private val Violet = Color(0xFF1677E8)
private val Pink = Color(0xFF1677E8)
private val Muted = Color(0xFF333333)



@androidx.compose.runtime.Composable
private fun RepostVerifiedIcon(verified: Boolean, tint: Color = Color.White) {
    androidx.compose.foundation.Canvas(modifier = Modifier.size(24.dp)) {
        val sx = size.width / 28f
        val sy = size.height / 28f
        val stroke = androidx.compose.ui.graphics.drawscope.Stroke(
            width = 2.2.dp.toPx(),
            cap = androidx.compose.ui.graphics.StrokeCap.Round,
            join = androidx.compose.ui.graphics.StrokeJoin.Round
        )
        val arrows = androidx.compose.ui.graphics.Path().apply {
            moveTo(5f * sx, 13f * sy)
            lineTo(5f * sx, 9f * sy)
            quadraticTo(5f * sx, 5f * sy, 9f * sx, 5f * sy)
            lineTo(23f * sx, 5f * sy)
            moveTo(18f * sx, 1.5f * sy)
            lineTo(23f * sx, 5f * sy)
            lineTo(18f * sx, 9f * sy)
            moveTo(23f * sx, 15f * sy)
            lineTo(23f * sx, 19f * sy)
            quadraticTo(23f * sx, 23f * sy, 19f * sx, 23f * sy)
            lineTo(5f * sx, 23f * sy)
            moveTo(10f * sx, 19f * sy)
            lineTo(5f * sx, 23f * sy)
            lineTo(10f * sx, 26.5f * sy)
        }
        drawPath(arrows, tint, style = stroke)
        if (verified) {
            val check = androidx.compose.ui.graphics.Path().apply {
                moveTo(10f * sx, 14f * sy)
                lineTo(13f * sx, 17f * sy)
                lineTo(19f * sx, 11f * sy)
            }
            drawPath(check, Color(0xFF00E676), style = stroke)
        }
    }
}

@androidx.compose.runtime.Composable
private fun RoundedPaperPlaneIcon(tint: Color = Color.White) {
    // Ícone próprio da VYBORA em grade 24x24, contorno 2dp e cantos suaves.
    androidx.compose.foundation.Canvas(modifier = Modifier.size(24.dp)) {
        val x = size.width / 24f
        val y = size.height / 24f
        val outline = androidx.compose.ui.graphics.Path().apply {
            moveTo(3.8f * x, 5.0f * y)
            lineTo(19.4f * x, 5.0f * y)
            quadraticTo(21.7f * x, 5.0f * y, 20.5f * x, 7.2f * y)
            lineTo(12.8f * x, 20.0f * y)
            quadraticTo(11.3f * x, 22.0f * y, 10.7f * x, 19.4f * y)
            lineTo(8.9f * x, 12.6f * y)
            lineTo(3.0f * x, 7.2f * y)
            quadraticTo(1.6f * x, 5.7f * y, 3.8f * x, 5.0f * y)
            close()
        }
        val stroke = androidx.compose.ui.graphics.drawscope.Stroke(
            width = 2.dp.toPx(),
            cap = androidx.compose.ui.graphics.StrokeCap.Round,
            join = androidx.compose.ui.graphics.StrokeJoin.Round
        )
        drawPath(outline, tint, style = stroke)
        val fold = androidx.compose.ui.graphics.Path().apply {
            moveTo(8.9f * x, 12.6f * y)
            lineTo(17.5f * x, 8.0f * y)
        }
        drawPath(fold, Color.White, style = stroke)
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { VyboraApp() }
    }
}

private fun readImageAspectRatio(context: android.content.Context, source: Uri?): Float {
    if (source == null) return 16f / 10f
    return try {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(source)?.use { BitmapFactory.decodeStream(it, null, options) }
        if (options.outWidth > 0 && options.outHeight > 0) options.outWidth.toFloat() / options.outHeight.toFloat() else 16f / 10f
    } catch (_: Exception) { 16f / 10f }
}

private fun createAdjustedImage(context: android.content.Context, source: Uri, ratio: String, filter: String, zoom: Float = 1f, panX: Float = 0f, panY: Float = 0f, rotationDegrees: Float = 0f): Uri {
    val decoded = context.contentResolver.openInputStream(source)?.use { BitmapFactory.decodeStream(it) }
        ?: throw IllegalArgumentException("Não foi possível abrir a imagem selecionada.")
    val rotated = if (rotationDegrees % 360f != 0f) {
        val rotationMatrix = android.graphics.Matrix().apply { postRotate(rotationDegrees) }
        Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, rotationMatrix, true)
            .also { if (it !== decoded) decoded.recycle() }
    } else decoded

    val targetRatio = when (ratio) {
        "1:1" -> 1f
        "4:5" -> 4f / 5f
        "16:9" -> 16f / 9f
        "9:16" -> 9f / 16f
        "16:10" -> 16f / 10f
        else -> rotated.width.toFloat() / rotated.height.toFloat()
    }
    val outputWidth = 1600
    val outputHeight = (outputWidth / targetRatio).toInt().coerceAtLeast(1)
    val output = Bitmap.createBitmap(outputWidth, outputHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(output)
    canvas.drawColor(android.graphics.Color.rgb(5, 8, 20))

    // Foto do perfil em tela cheia: preencher a proporção ampla com recorte, igual à prévia.
    // Não amplia além do necessário para preencher; só preserva o reposicionamento por arraste.
    val safeZoom = 1f
    val baseScale = maxOf(outputWidth.toFloat() / rotated.width, outputHeight.toFloat() / rotated.height)
    val scale = baseScale * safeZoom
    val drawWidth = rotated.width * scale
    val drawHeight = rotated.height * scale
    val maxPanX = maxOf(0f, (drawWidth - outputWidth) / (2f * outputWidth))
    val maxPanY = maxOf(0f, (drawHeight - outputHeight) / (2f * outputHeight))
    val safePanX = panX.coerceIn(-maxPanX, maxPanX)
    val safePanY = panY.coerceIn(-maxPanY, maxPanY)
    val left = (outputWidth - drawWidth) / 2f + safePanX * outputWidth
    val top = (outputHeight - drawHeight) / 2f + safePanY * outputHeight

    val matrix = ColorMatrix()
    when (filter) {
        "P&B", "Preto e branco" -> matrix.setSaturation(0f)
        "Vivo" -> matrix.setSaturation(1.45f)
        "Neon" -> matrix.setSaturation(1.7f)
        "Quente" -> matrix.set(floatArrayOf(1.08f,0f,0f,0f,12f, 0f,1f,0f,0f,2f, 0f,0f,0.9f,0f,0f, 0f,0f,0f,1f,0f))
        "Frio" -> matrix.set(floatArrayOf(0.92f,0f,0f,0f,0f, 0f,1f,0f,0f,0f, 0f,0f,1.12f,0f,10f, 0f,0f,0f,1f,0f))
        "Cinema" -> matrix.set(floatArrayOf(1.08f,0f,0f,0f,-8f, 0f,0.98f,0f,0f,0f,0f,0f,0.86f,0f,4f, 0f,0f,0f,1f,0f))
        "Retrô" -> { matrix.setSaturation(0.78f); matrix.postConcat(ColorMatrix(floatArrayOf(1f,0f,0f,0f,10f, 0f,0.92f,0f,0f,4f, 0f,0f,0.78f,0f,0f, 0f,0f,0f,1f,0f))) }
        "Brilho" -> matrix.set(floatArrayOf(1.12f,0f,0f,0f,18f, 0f,1.12f,0f,0f,18f, 0f,0f,1.12f,0f,18f, 0f,0f,0f,1f,0f))
        "Contraste" -> matrix.set(floatArrayOf(1.25f,0f,0f,0f,-22f, 0f,1.25f,0f,0f,-22f, 0f,0f,1.25f,0f,-22f, 0f,0f,0f,1f,0f))
        "Saturação" -> matrix.setSaturation(1.65f)
        "Fade" -> matrix.set(floatArrayOf(0.92f,0f,0f,0f,18f, 0f,0.92f,0f,0f,18f, 0f,0f,0.92f,0f,18f, 0f,0f,0f,1f,0f))
        "Glow" -> matrix.set(floatArrayOf(1.18f,0f,0f,0f,14f, 0f,1.08f,0f,0f,8f, 0f,0f,1.2f,0f,16f, 0f,0f,0f,1f,0f))
        "VHS" -> matrix.set(floatArrayOf(1.05f,0f,0f,0f,0f, 0f,0.92f,0f,0f,0f,0f, 0f,0f,1.08f,0f,0f, 0f,0f,0f,1f,0f))
        "Granulado" -> matrix.setSaturation(0.9f)
        "Vinheta" -> matrix.set(floatArrayOf(0.82f,0f,0f,0f,0f, 0f,0.82f,0f,0f,0f, 0f,0f,0.82f,0f,0f, 0f,0f,0f,1f,0f))
        "Desfoque" -> matrix.setSaturation(0.75f)
    }
    val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
        colorFilter = ColorMatrixColorFilter(matrix)
    }
    canvas.drawBitmap(rotated, null, android.graphics.RectF(left, top, left + drawWidth, top + drawHeight), paint)
    if (!rotated.isRecycled) rotated.recycle()

    val file = File(context.cacheDir, "vybora-adjusted-${System.currentTimeMillis()}.jpg")
    FileOutputStream(file).use { output.compress(Bitmap.CompressFormat.JPEG, 92, it) }
    output.recycle()
    return Uri.fromFile(file)
}

private fun createEditorOverlayImage(context: android.content.Context, source: Uri, overlayText: String, sticker: String, strokes: List<List<Offset>> = emptyList()): Uri {
    if (overlayText.isBlank() && sticker.isBlank() && strokes.isEmpty()) return source
    val bitmap = context.contentResolver.openInputStream(source)?.use { BitmapFactory.decodeStream(it) }
        ?: throw IllegalArgumentException("Não foi possível abrir a imagem para aplicar as edições.")
    val canvas = Canvas(bitmap)
    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        textAlign = Paint.Align.CENTER
        typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.BOLD)
        textSize = (bitmap.width * 0.065f).coerceIn(28f, 96f)
        setShadowLayer(8f, 2f, 3f, android.graphics.Color.BLACK)
    }
    if (sticker.isNotBlank()) {
        textPaint.textSize = (bitmap.width * 0.12f).coerceIn(44f, 150f)
        canvas.drawText(sticker, bitmap.width / 2f, bitmap.height * 0.40f, textPaint)
    }
    if (strokes.isNotEmpty()) {
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.rgb(255, 42, 157)
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            strokeWidth = (bitmap.width * 0.009f).coerceIn(8f, 24f)
            setShadowLayer(3f, 1f, 1f, android.graphics.Color.BLACK)
        }
        strokes.forEach { points ->
            if (points.size > 1) {
                val path = android.graphics.Path()
                path.moveTo(points.first().x * bitmap.width, points.first().y * bitmap.height)
                points.drop(1).forEach { path.lineTo(it.x * bitmap.width, it.y * bitmap.height) }
                canvas.drawPath(path, strokePaint)
            }
        }
    }
    if (overlayText.isNotBlank()) {
        textPaint.textSize = (bitmap.width * 0.055f).coerceIn(24f, 72f)
        val maxWidth = bitmap.width * 0.86f
        val words = overlayText.trim().split(Regex("\\s+"))
        val lines = mutableListOf<String>()
        var line = ""
        for (word in words) {
            val candidate = if (line.isEmpty()) word else "$line $word"
            if (textPaint.measureText(candidate) > maxWidth && line.isNotEmpty()) {
                lines.add(line)
                line = word
            } else line = candidate
        }
        if (line.isNotEmpty()) lines.add(line)
        val lineHeight = textPaint.textSize * 1.25f
        val startY = bitmap.height * 0.55f - (lines.size - 1) * lineHeight / 2f
        lines.forEachIndexed { index, value -> canvas.drawText(value, bitmap.width / 2f, startY + index * lineHeight, textPaint) }
    }
    val file = File(context.cacheDir, "vybora-editor-${System.currentTimeMillis()}.jpg")
    FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.JPEG, 94, it) }
    bitmap.recycle()
    return Uri.fromFile(file)
}

private data class SavedVyboraAccount(val username: String, val token: String, val displayName: String = "", val bio: String = "", val refreshToken: String = "")
private fun savedVyboraAccounts(prefs: android.content.SharedPreferences): List<SavedVyboraAccount> = try {
 val a=JSONArray(prefs.getString("vybora_accounts","[]") ?: "[]")
 (0 until a.length()).mapNotNull { i -> val o=a.optJSONObject(i) ?: return@mapNotNull null; val u=o.optString("username"); val t=o.optString("token"); if(u.isBlank()||t.isBlank()) null else SavedVyboraAccount(u,t,o.optString("displayName"),o.optString("bio"),o.optString("refreshToken")) }.distinctBy { it.username.lowercase() }
} catch (_:Exception) { emptyList() }
private fun rememberVyboraAccount(prefs: android.content.SharedPreferences, username:String, token:String, displayName:String="", bio:String="", refreshToken:String=(prefs.getString("refresh_token","") ?: "")) {
 if(username.isBlank()||token.isBlank()) return
 val list=savedVyboraAccounts(prefs).filterNot { it.username.equals(username,true) }.toMutableList()
 list.add(0,SavedVyboraAccount(username,token,displayName,bio,refreshToken))
 val a=JSONArray()
 list.take(8).forEach { a.put(JSONObject().put("username",it.username).put("token",it.token).put("displayName",it.displayName).put("bio",it.bio).put("refreshToken",it.refreshToken)) }
 prefs.edit().putString("vybora_accounts",a.toString()).apply()
}
@Composable
private fun AccountSwitcherDialog(
 accounts: List<SavedVyboraAccount>,
 currentUsername: String,
 onDismiss: () -> Unit,
 onSelect: (SavedVyboraAccount) -> Unit,
 onAddAccount: () -> Unit,
 onRemove: (String) -> Unit
) {
 var confirmRemove by remember { mutableStateOf("") }
 val blue = Color(0xFF1877F2)
 androidx.compose.ui.window.Dialog(
  onDismissRequest = onDismiss,
  properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
 ) {
  Box(Modifier.fillMaxSize().padding(bottom = 92.dp), contentAlignment = Alignment.BottomCenter) {
   Surface(
    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
    color = Color.White,
    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
   ) {
   Column(Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 18.dp)) {
    Spacer(Modifier.height(12.dp))
    Box(Modifier.align(Alignment.CenterHorizontally).size(width = 42.dp, height = 4.dp).background(Color(0xFFD0D5DD), RoundedCornerShape(50)))
    Spacer(Modifier.height(12.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
     IconButton(onClick = onDismiss) { Icon(Icons.Default.ArrowBack, "Voltar", tint = Color.Black) }
     Column(Modifier.weight(1f)) {
      Text("Trocar de conta", fontSize = 25.sp, fontWeight = FontWeight.Bold, color = Color(0xFF101828))
      Text("Selecione o perfil que deseja usar", fontSize = 14.sp, color = Color(0xFF667085))
     }
    }
    Spacer(Modifier.height(16.dp))
    Column(Modifier.heightIn(max = 520.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
     if (accounts.isEmpty()) Text("Nenhuma conta salva neste aparelho.", color = Color.Gray)
     accounts.forEach { account ->
      val active = account.username.equals(currentUsername, ignoreCase = true)
      Surface(
       modifier = Modifier.fillMaxWidth().clickable { if (!active) onSelect(account) },
       color = if (active) Color(0xFFF2F8FF) else Color.White,
       shape = RoundedCornerShape(17.dp),
       border = BorderStroke(if (active) 1.5.dp else 1.dp, if (active) blue else Color(0xFFE3E7ED))
      ) {
       Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
        QiblyAccountAvatar(account.username, "", 66.dp) { if (!active) onSelect(account) }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
         Text(account.displayName.ifBlank { account.username }, color = Color(0xFF101828), fontWeight = FontWeight.Bold, fontSize = 18.sp, maxLines = 1)
         Text("@"+account.username.removePrefix("@"), color = Color(0xFF667085), fontSize = 14.sp)
         if (active) Text("● Conta atual", color = blue, fontSize = 12.sp, modifier = Modifier.padding(top = 5.dp))
        }
        if (active) Icon(Icons.Default.CheckCircle, "Conta atual", tint = blue)
        else Icon(Icons.Default.ChevronRight, "Trocar para esta conta", tint = Color(0xFF667085))
        IconButton(onClick = { confirmRemove = account.username }, modifier = Modifier.size(34.dp)) {
         Icon(Icons.Filled.DeleteForever, contentDescription = "Remover conta deste aparelho", tint = Color(0xFF667085), modifier = Modifier.size(22.dp))
        }
       }
      }
     }
     OutlinedButton(
      onClick = onAddAccount,
      modifier = Modifier.fillMaxWidth().height(56.dp),
      shape = RoundedCornerShape(16.dp),
      border = BorderStroke(1.dp, Color(0xFFDCE9FA))
     ) {
      Icon(Icons.Default.Add, null, tint = blue)
      Spacer(Modifier.width(8.dp))
      Text("Adicionar outra conta", color = blue, fontWeight = FontWeight.SemiBold)
     }
    }
    Spacer(Modifier.height(16.dp))
   }
   }
  }
 }
 if (confirmRemove.isNotBlank()) {
  androidx.compose.material3.AlertDialog(
   onDismissRequest = { confirmRemove = "" },
   title = { Text("Remover conta?") },
   text = { Text("Tem certeza de que deseja remover esta conta da lista neste aparelho?") },
   confirmButton = {
    TextButton(onClick = {
     val target = confirmRemove
     confirmRemove = ""
     onRemove(target)

    }) { Text("Remover", color = Color(0xFF475467)) }
   },
   dismissButton = { TextButton(onClick = { confirmRemove = "" }) { Text("Cancelar") } },
   containerColor = Color.White,
   titleContentColor = Color(0xFF101828),
   textContentColor = Color(0xFF667085)
  )
 }
}

private object QiblyTheme {
    var dark by androidx.compose.runtime.mutableStateOf(false)
    val background: Color get() = if (dark) Color.Black else Color.White
    val card: Color get() = if (dark) Color.Black else Color.White
    val text: Color get() = if (dark) Color(0xFFF5F7FA) else Color.Black
    val secondary: Color get() = if (dark) Color(0xFFB4BDCA) else Color(0xFF555555)
    val border: Color get() = if (dark) Color(0xFF303B4C) else Color(0xFFE4E4E4)
}

private object RemoteAppearance {
    private var feedTextColorBase by androidx.compose.runtime.mutableStateOf(Color.White)
    var feedTextColor: Color
        get() = if (QiblyTheme.dark) Color.White else feedTextColorBase
        set(value) { feedTextColorBase = value }
    private var feedSecondaryTextColorBase by androidx.compose.runtime.mutableStateOf(Color(0xFFADB2CB))
    var feedSecondaryTextColor: Color
        get() = if (QiblyTheme.dark) Color(0xFFB4BDCA) else feedSecondaryTextColorBase
        set(value) { feedSecondaryTextColorBase = value }
    private var feedActionBackgroundColorBase by androidx.compose.runtime.mutableStateOf(Color(0x66030711))
    var feedActionBackgroundColor: Color
        get() = if (QiblyTheme.dark) QiblyTheme.card else feedActionBackgroundColorBase
        set(value) { feedActionBackgroundColorBase = value }
    private var feedDividerColorBase by androidx.compose.runtime.mutableStateOf(Color(0xFF252525))
    var feedDividerColor: Color
        get() = if (QiblyTheme.dark) Color(0xFF303B4C) else feedDividerColorBase
        set(value) { feedDividerColorBase = value }
    var navigationUnderlineWidth by androidx.compose.runtime.mutableIntStateOf(30)
    var navigationUnderlineHeight by androidx.compose.runtime.mutableIntStateOf(3)
    var navigationUnderlineOffset by androidx.compose.runtime.mutableIntStateOf(6)
    var navigationUnderlineVisible by androidx.compose.runtime.mutableStateOf(true)
    private var screenBackgroundColorBase by androidx.compose.runtime.mutableStateOf(Color.White)
    var screenBackgroundColor: Color
        get() = if (QiblyTheme.dark) QiblyTheme.background else screenBackgroundColorBase
        set(value) { screenBackgroundColorBase = value }
    private var screenTextColorBase by androidx.compose.runtime.mutableStateOf(Color.Black)
    var screenTextColor: Color
        get() = if (QiblyTheme.dark) Color(0xFFF5F7FA) else screenTextColorBase
        set(value) { screenTextColorBase = value }
    private var screenSecondaryTextColorBase by androidx.compose.runtime.mutableStateOf(Color(0xFF333333))
    var screenSecondaryTextColor: Color
        get() = if (QiblyTheme.dark) Color(0xFFB4BDCA) else screenSecondaryTextColorBase
        set(value) { screenSecondaryTextColorBase = value }
    private var headerTextColorBase by androidx.compose.runtime.mutableStateOf(Color.White)
    var headerTextColor: Color
        get() = if (QiblyTheme.dark) Color.White else headerTextColorBase
        set(value) { headerTextColorBase = value }
    var headerUnderlineColor by androidx.compose.runtime.mutableStateOf(Color.White)
    var headerTextSize by androidx.compose.runtime.mutableIntStateOf(17)
    var headerVisible by androidx.compose.runtime.mutableStateOf(true)
    private var actionIconColorBase by androidx.compose.runtime.mutableStateOf(Color.White)
    var actionIconColor: Color
        get() = if (QiblyTheme.dark) Color(0xFFF5F7FA) else actionIconColorBase
        set(value) { actionIconColorBase = value }
    var actionIconSize by androidx.compose.runtime.mutableIntStateOf(24)
    var actionsVisible by androidx.compose.runtime.mutableStateOf(true)
    var navigationLabels by androidx.compose.runtime.mutableStateOf(listOf("Início", "Comunidades", "Criar", "Mensagens", "Perfil"))
    var feedLabels by androidx.compose.runtime.mutableStateOf(listOf("Descobrir", "Seguindo", "Comunidade"))
    var navigationLabelSize by androidx.compose.runtime.mutableIntStateOf(14)
    private var headerBackgroundColorBase by androidx.compose.runtime.mutableStateOf(Color.Black)
    var headerBackgroundColor: Color
        get() = if (QiblyTheme.dark) Color.Black else headerBackgroundColorBase
        set(value) { headerBackgroundColorBase = value }
    var headerHorizontalPadding by androidx.compose.runtime.mutableIntStateOf(24)
    var headerVerticalPadding by androidx.compose.runtime.mutableIntStateOf(10)
    var feedSpacing by androidx.compose.runtime.mutableIntStateOf(0)
    var spreadPostActions by androidx.compose.runtime.mutableStateOf(false)
    var badgeColor by androidx.compose.runtime.mutableStateOf(Color(0xFFFF3045))
    var likedColor by androidx.compose.runtime.mutableStateOf(Color(0xFFFF3045))
    var profileAccentColor by androidx.compose.runtime.mutableStateOf(Color.White)
    var commentAccentColor by androidx.compose.runtime.mutableStateOf(Color.White)
    var commentBackgroundColor by androidx.compose.runtime.mutableStateOf(Color.White)
    var commentTextColor by androidx.compose.runtime.mutableStateOf(Color.Black)
    var commentSecondaryTextColor by androidx.compose.runtime.mutableStateOf(Color(0xFF555555))
    var commentInputBackgroundColor by androidx.compose.runtime.mutableStateOf(Color(0xFFF5F5F5))
    var commentDividerColor by androidx.compose.runtime.mutableStateOf(Color(0xFFE5E5E5))
    var messageAccentColor by androidx.compose.runtime.mutableStateOf(Color.White)
    var playerProgressColor by androidx.compose.runtime.mutableStateOf(Color.White)
    var enableDemoFeed by androidx.compose.runtime.mutableStateOf(true)
    var showFeedHeader by androidx.compose.runtime.mutableStateOf(true)
}

private object RemoteUiRefreshSignal {
    var value by androidx.compose.runtime.mutableIntStateOf(0)
}

@Composable
private fun VyboraApp() {
    var tab by remember { mutableIntStateOf(0) }
    var navTopRequest by remember { mutableIntStateOf(0) }
    // GitHub-hosted, data-only UI configuration. No executable code is downloaded.
    val appContext = androidx.compose.ui.platform.LocalContext.current
    val uiPreferences = remember { appContext.getSharedPreferences("vybora_remote_ui", android.content.Context.MODE_PRIVATE) }
    val themePrefs = remember { appContext.getSharedPreferences("vybora_session", android.content.Context.MODE_PRIVATE) }
    LaunchedEffect(Unit) { QiblyTheme.dark = themePrefs.getString("setting_theme", "light") == "dark" }
    fun readUiColor(key: String, fallback: Long): Color {
        val saved = uiPreferences.getString(key, null) ?: return Color(fallback)
        return try { Color(android.graphics.Color.parseColor(saved)) } catch (_: Exception) { Color(fallback) }
    }
    var navigationIndicatorColor by remember { mutableStateOf(readUiColor("indicatorColor", 0x33FFFFFFL)) }
    var navigationBackgroundColor by remember { mutableStateOf(readUiColor("backgroundColor", 0x99030710L)) }
    var navigationSelectedColor by remember { mutableStateOf(readUiColor("selectedIconColor", 0xFFFFFFFFL)) }
    var navigationUnselectedColor by remember { mutableStateOf(readUiColor("unselectedIconColor", 0xFFFFFFFFL)) }
    LaunchedEffect(RemoteUiRefreshSignal.value) {
        val manualRefresh = RemoteUiRefreshSignal.value > 0
        var firstRequest = true
        while (true) {
            val config = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                fun fetchJson(urlString: String, githubApi: Boolean): JSONObject? {
                    val connection = java.net.URL(urlString).openConnection() as java.net.HttpURLConnection
                    try {
                        connection.connectTimeout = 5000
                        connection.readTimeout = 5000
                        connection.useCaches = false
                        connection.setRequestProperty("Cache-Control", "no-cache, no-store, max-age=0")
                        connection.setRequestProperty("Pragma", "no-cache")
                        if (githubApi) connection.setRequestProperty("Accept", "application/vnd.github+json")
                        if (connection.responseCode !in 200..299) return null
                        val raw = connection.inputStream.bufferedReader().use { it.readText() }
                        if (raw.length > 100000) return null
                        if (!githubApi) return JSONObject(raw)
                        val encoded = JSONObject(raw).optString("content").replace("\n", "").replace("\\r", "")
                        val decoded = String(android.util.Base64.decode(encoded, android.util.Base64.DEFAULT), Charsets.UTF_8)
                        return JSONObject(decoded)
                    } finally { connection.disconnect() }
                }
                try {
                    val nonce = System.currentTimeMillis().toString()
                    // GitHub Contents API avoids stale raw.githubusercontent.com CDN revisions.
                    fetchJson("https://api.github.com/repos/hernandesslr-svg/QINBLY/contents/remote-config/android-ui.json?ref=main&nonce=" + nonce, true)
                        ?: fetchJson("https://raw.githubusercontent.com/hernandesslr-svg/QINBLY/main/remote-config/android-ui.json?refresh=" + nonce, false)
                } catch (_: Exception) { null }
            }
            if (manualRefresh && firstRequest) {
                val message = if (config?.optInt("schemaVersion") == 1)
                    "Configuração visual recebida: revisão " + config.optInt("revision")
                else "Falha ao buscar configuração visual no GitHub"
                android.widget.Toast.makeText(appContext, message, android.widget.Toast.LENGTH_SHORT).show()
            }
            firstRequest = false
            if (config?.optInt("schemaVersion") == 1) {
                val navigation = config.optJSONObject("navigation")
                if (navigation != null) {
                    val editor = uiPreferences.edit()
                    fun applyColor(key: String, apply: (Color) -> Unit) {
                        val value = navigation.optString(key)
                        if (Regex("^#[0-9a-fA-F]{8}$").matches(value)) {
                            try {
                                apply(Color(android.graphics.Color.parseColor(value)))
                                editor.putString(key, value)
                            } catch (_: Exception) { }
                        }
                    }
                    applyColor("indicatorColor") { navigationIndicatorColor = it }
                    applyColor("backgroundColor") { navigationBackgroundColor = it }
                    applyColor("selectedIconColor") { navigationSelectedColor = it }
                    applyColor("unselectedIconColor") { navigationUnselectedColor = it }
                    val appearance = config.optJSONObject("appearance")
                    if (appearance != null) {
                        fun color(key: String, fallback: Color): Color {
                            val raw = appearance.optString(key)
                            return if (Regex("^#[0-9a-fA-F]{8}$").matches(raw))
                                try { Color(android.graphics.Color.parseColor(raw)) } catch (_: Exception) { fallback }
                            else fallback
                        }
                        RemoteAppearance.feedTextColor = color("feedTextColor", RemoteAppearance.feedTextColor)
                        RemoteAppearance.feedSecondaryTextColor = color("feedSecondaryTextColor", RemoteAppearance.feedSecondaryTextColor)
                        RemoteAppearance.feedActionBackgroundColor = color("feedActionBackgroundColor", RemoteAppearance.feedActionBackgroundColor)
                        RemoteAppearance.feedDividerColor = color("feedDividerColor", RemoteAppearance.feedDividerColor)
                        RemoteAppearance.screenBackgroundColor = color("screenBackgroundColor", RemoteAppearance.screenBackgroundColor)
                        RemoteAppearance.screenTextColor = color("screenTextColor", RemoteAppearance.screenTextColor)
                        RemoteAppearance.screenSecondaryTextColor = color("screenSecondaryTextColor", RemoteAppearance.screenSecondaryTextColor)
                        RemoteAppearance.navigationUnderlineWidth = appearance.optInt("navigationUnderlineWidth", RemoteAppearance.navigationUnderlineWidth).coerceIn(8, 72)
                        RemoteAppearance.navigationUnderlineHeight = appearance.optInt("navigationUnderlineHeight", RemoteAppearance.navigationUnderlineHeight).coerceIn(1, 8)
                        RemoteAppearance.navigationUnderlineOffset = appearance.optInt("navigationUnderlineOffset", RemoteAppearance.navigationUnderlineOffset).coerceIn(-8, 12)
                        RemoteAppearance.navigationUnderlineVisible = appearance.optBoolean("navigationUnderlineVisible", RemoteAppearance.navigationUnderlineVisible)
                        RemoteAppearance.headerTextColor = color("headerTextColor", RemoteAppearance.headerTextColor)
                        RemoteAppearance.headerUnderlineColor = color("headerUnderlineColor", RemoteAppearance.headerUnderlineColor)
                        RemoteAppearance.actionIconColor = color("actionIconColor", RemoteAppearance.actionIconColor)
                        RemoteAppearance.headerBackgroundColor = color("headerBackgroundColor", RemoteAppearance.headerBackgroundColor)
                        RemoteAppearance.badgeColor = color("badgeColor", RemoteAppearance.badgeColor)
                        RemoteAppearance.likedColor = color("likedColor", RemoteAppearance.likedColor)
                        RemoteAppearance.profileAccentColor = color("profileAccentColor", RemoteAppearance.profileAccentColor)
                        RemoteAppearance.commentAccentColor = color("commentAccentColor", RemoteAppearance.commentAccentColor)
                        RemoteAppearance.commentBackgroundColor = color("commentBackgroundColor", RemoteAppearance.commentBackgroundColor)
                        RemoteAppearance.commentTextColor = color("commentTextColor", RemoteAppearance.commentTextColor)
                        RemoteAppearance.commentSecondaryTextColor = color("commentSecondaryTextColor", RemoteAppearance.commentSecondaryTextColor)
                        RemoteAppearance.commentInputBackgroundColor = color("commentInputBackgroundColor", RemoteAppearance.commentInputBackgroundColor)
                        RemoteAppearance.commentDividerColor = color("commentDividerColor", RemoteAppearance.commentDividerColor)
                        RemoteAppearance.messageAccentColor = color("messageAccentColor", RemoteAppearance.messageAccentColor)
                        RemoteAppearance.playerProgressColor = color("playerProgressColor", RemoteAppearance.playerProgressColor)
                        RemoteAppearance.headerHorizontalPadding = appearance.optInt("headerHorizontalPadding", RemoteAppearance.headerHorizontalPadding).coerceIn(0, 40)
                        RemoteAppearance.headerVerticalPadding = appearance.optInt("headerVerticalPadding", RemoteAppearance.headerVerticalPadding).coerceIn(0, 32)
                        RemoteAppearance.feedSpacing = appearance.optInt("feedSpacing", RemoteAppearance.feedSpacing)
                        RemoteAppearance.spreadPostActions = appearance.optBoolean("spreadPostActions", RemoteAppearance.spreadPostActions)
                        RemoteAppearance.showFeedHeader = appearance.optBoolean("showFeedHeader", RemoteAppearance.showFeedHeader)
                        RemoteAppearance.headerTextSize = appearance.optInt("headerTextSize", RemoteAppearance.headerTextSize).coerceIn(12, 24)
                        RemoteAppearance.actionIconSize = appearance.optInt("actionIconSize", RemoteAppearance.actionIconSize).coerceIn(16, 36)
                        RemoteAppearance.navigationLabelSize = appearance.optInt("navigationLabelSize", RemoteAppearance.navigationLabelSize).coerceIn(10, 18)
                        RemoteAppearance.headerVisible = appearance.optBoolean("headerVisible", RemoteAppearance.headerVisible)
                        RemoteAppearance.actionsVisible = appearance.optBoolean("actionsVisible", RemoteAppearance.actionsVisible)
                        fun labels(key: String, count: Int, fallback: List<String>): List<String> {
                            val arr = appearance.optJSONArray(key) ?: return fallback
                            if (arr.length() != count) return fallback
                            return (0 until count).map { index -> arr.optString(index).take(22).ifBlank { fallback[index] } }
                        }
                        RemoteAppearance.navigationLabels = labels("navigationLabels", 5, RemoteAppearance.navigationLabels)
                        RemoteAppearance.feedLabels = labels("feedLabels", 3, RemoteAppearance.feedLabels)
                    }
                    editor.apply()
                }
            }
            kotlinx.coroutines.delay(60000L)
        }
    }

    var liked by remember { mutableStateOf(setOf<String>()) }
    var notice by remember { mutableStateOf("") }
    var showCreateOptions by remember { mutableStateOf(false) }
    var createEntryMode by remember { mutableStateOf("camera") }
    var profileStat by remember { mutableStateOf("") }
    var profileStatUsers by remember { mutableStateOf<List<VyboraApi.UserSummary>>(emptyList()) }
    var showAuth by remember { mutableStateOf(false) }
    var showAccountSwitcher by remember { mutableStateOf(false) }
    var accountSessionVersion by remember { mutableIntStateOf(0) }
    var showSearchDialog by remember { mutableStateOf(false) }
    var publicProfileUsername by remember { mutableStateOf("") }
    var messageRecipient by remember { mutableStateOf("") }
    VyboraApi.initialize(appContext)
    val appPrefs = remember { appContext.getSharedPreferences("vybora_session", android.content.Context.MODE_PRIVATE) }
    var loggedInUser by remember { mutableStateOf(appPrefs.getString("username", "") ?: "") }
    var unreadMessages by remember { mutableIntStateOf(0) }
    var onlineUsers by remember { mutableIntStateOf(-1) }
    LaunchedEffect(loggedInUser, accountSessionVersion) {
        val token = appPrefs.getString("token", null)
        onlineUsers = -1
        if (!token.isNullOrBlank()) {
            while (true) {
                try {
                    onlineUsers = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                        VyboraApi.heartbeatPresence(token)
                    }
                } catch (_: Exception) {
                    onlineUsers = -1
                }
                kotlinx.coroutines.delay(5_000L)
            }
        }
    }
    LaunchedEffect(loggedInUser, accountSessionVersion) {
        val token = appPrefs.getString("token", null)
        if (token.isNullOrBlank()) {
            unreadMessages = 0
        } else {
            while (true) {
                try {
                    val inbox = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                        VyboraApi.getInbox(token)
                    }
                    unreadMessages = inbox.sumOf { it.unreadCount }.coerceAtLeast(0)
                } catch (_: Exception) {
                    // Mantém o último contador válido se a rede estiver indisponível.
                }
                kotlinx.coroutines.delay(15_000)
            }
        }
    }
    var showSettings by remember { mutableStateOf(false) }
    var nameFontStyle by remember { mutableStateOf(appPrefs.getString("name_font_style", "Padrão") ?: "Padrão") }
    var appFontStyle by remember { mutableStateOf(appPrefs.getString("app_font_style", "Padrão") ?: "Padrão") }
    var showEditProfile by remember { mutableStateOf(false) }
    var showEditProfilePhoto by remember { mutableStateOf(false) }
    var profilePhotoRefreshKey by remember { mutableIntStateOf(0) }
    var showNotifications by remember { mutableStateOf(false) }
    var profileName by remember { mutableStateOf(appPrefs.getString("display_name", "") ?: "") }
    var profileBio by remember { mutableStateOf(appPrefs.getString("bio", "") ?: "") }
    LaunchedEffect(Unit) {
        val token = appPrefs.getString("token", null)
        if (!token.isNullOrBlank() && loggedInUser.isNotBlank()) rememberVyboraAccount(appPrefs, loggedInUser, token, profileName, profileBio)
        if (!token.isNullOrBlank()) {
            Thread {
                try {
                    val response = VyboraApi.getMyProfile(token)
                    val user = response.optJSONObject("user")
                    if (user != null) {
                        val username = user.optString("username")
                        val name = user.optString("displayName", user.optString("display_name"))
                        val bio = user.optString("bio")
                        appPrefs.edit().putString("username", username).putString("display_name", name).putString("bio", bio).apply()
                        rememberVyboraAccount(appPrefs, username, token, name, bio)
                        android.os.Handler(android.os.Looper.getMainLooper()).post {
                            if (username.isNotBlank()) loggedInUser = username
                            profileName = name
                            profileBio = bio
                        }
                    }
                } catch (_: Exception) {
                    // Keep the cached profile visible when the network is unavailable.
                }
            }.start()
        }
    }
    BackHandler {
        when {
            showAccountSwitcher -> showAccountSwitcher = false
            showSearchDialog -> showSearchDialog = false
            profileStat.isNotBlank() -> profileStat = ""
            showEditProfilePhoto -> showEditProfilePhoto = false
            showEditProfile -> showEditProfile = false
            showSettings -> showSettings = false
            showNotifications -> showNotifications = false
            publicProfileUsername.isNotBlank() -> publicProfileUsername = ""
            showAuth -> showAuth = false
            tab != 0 -> {
                tab = 0
                messageRecipient = ""
            }
            else -> (appContext as? android.app.Activity)?.finish()
        }
    }
    MaterialTheme(
        colorScheme = if (QiblyTheme.dark)
            darkColorScheme(primary = Color(0xFF70A9FF), secondary = Color(0xFF70A9FF), background = QiblyTheme.background, surface = QiblyTheme.card, onBackground = QiblyTheme.text, onSurface = QiblyTheme.text)
        else androidx.compose.material3.lightColorScheme(primary = Color(0xFF1677F2), secondary = Color(0xFF1677F2), background = Color.White, surface = Color.White, onBackground = Color.Black, onSurface = Color.Black),
        // Padrão de legibilidade para todas as telas e abas.
        typography = Typography(
            bodyLarge = androidx.compose.ui.text.TextStyle(fontFamily = nameFontFamily(appFontStyle), fontSize = 18.sp, lineHeight = 26.sp),
            bodyMedium = androidx.compose.ui.text.TextStyle(fontFamily = nameFontFamily(appFontStyle), fontSize = 16.sp, lineHeight = 23.sp),
            bodySmall = androidx.compose.ui.text.TextStyle(fontFamily = nameFontFamily(appFontStyle), fontSize = 14.sp, lineHeight = 20.sp),
            titleLarge = androidx.compose.ui.text.TextStyle(fontFamily = nameFontFamily(appFontStyle), fontSize = 23.sp, lineHeight = 30.sp, fontWeight = FontWeight.SemiBold),
            titleMedium = androidx.compose.ui.text.TextStyle(fontFamily = nameFontFamily(appFontStyle), fontSize = 19.sp, lineHeight = 26.sp, fontWeight = FontWeight.SemiBold),
            titleSmall = androidx.compose.ui.text.TextStyle(fontFamily = nameFontFamily(appFontStyle), fontSize = 17.sp, lineHeight = 24.sp, fontWeight = FontWeight.Medium),
            labelLarge = androidx.compose.ui.text.TextStyle(fontFamily = nameFontFamily(appFontStyle), fontSize = 16.sp, lineHeight = 22.sp, fontWeight = FontWeight.Medium),
            labelMedium = androidx.compose.ui.text.TextStyle(fontFamily = nameFontFamily(appFontStyle), fontSize = 14.sp, lineHeight = 20.sp),
            labelSmall = androidx.compose.ui.text.TextStyle(fontFamily = nameFontFamily(appFontStyle), fontSize = 12.sp, lineHeight = 17.sp)
        )
    ) {
        Scaffold(containerColor = QiblyTheme.background, bottomBar = {
            if (tab != 2) {
            Column {
                if (false) {
                    Button(
                        onClick = { notice = "A transmissão ainda não pode começar: o servidor de mídia precisa ser integrado e validado." },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Radio, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Iniciar transmissão")
                    }
                }
                NavigationBar(
                    containerColor = if (QiblyTheme.dark) Color.Black else navigationBackgroundColor,
                    contentColor = if (QiblyTheme.dark) Color.White else Color.Black,
                    tonalElevation = 0.dp
                ) {
                    val navColors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF1677F2),
                        selectedTextColor = Color(0xFF1677F2),
                        indicatorColor = Color.Transparent,
                        unselectedIconColor = if (QiblyTheme.dark) Color.White else navigationUnselectedColor,
                        unselectedTextColor = if (QiblyTheme.dark) Color.White else navigationUnselectedColor
                    )
                    NavigationBarItem(selected = tab == 0 && !showNotifications, onClick = { showSettings = false; showEditProfile = false; showNotifications = false; publicProfileUsername = ""; messageRecipient = ""; showAuth = false; if (tab == 0) navTopRequest++; tab = 0 }, icon = { Box(Modifier.size(32.dp), contentAlignment = Alignment.Center) { Icon(Icons.Default.Home, null); if (RemoteAppearance.navigationUnderlineVisible && tab == 0 && !showNotifications) Box(Modifier.align(Alignment.BottomCenter).offset(y = RemoteAppearance.navigationUnderlineOffset.dp).width(RemoteAppearance.navigationUnderlineWidth.dp).height(RemoteAppearance.navigationUnderlineHeight.dp).clip(RoundedCornerShape(2.dp)).background(navigationIndicatorColor)) } }, label = { Text(RemoteAppearance.navigationLabels[0], fontSize = RemoteAppearance.navigationLabelSize.sp) }, colors = navColors)
                    NavigationBarItem(selected = tab == 3 && !showNotifications, onClick = { showSettings = false; showEditProfile = false; showNotifications = false; publicProfileUsername = ""; showAuth = false; if (tab == 3) navTopRequest++; tab = 3 }, icon = { Box(Modifier.size(32.dp), contentAlignment = Alignment.Center) { Icon(Icons.Default.Groups, contentDescription = "Comunidades"); if (RemoteAppearance.navigationUnderlineVisible && tab == 3 && !showNotifications) Box(Modifier.align(Alignment.BottomCenter).offset(y = RemoteAppearance.navigationUnderlineOffset.dp).width(RemoteAppearance.navigationUnderlineWidth.dp).height(RemoteAppearance.navigationUnderlineHeight.dp).clip(RoundedCornerShape(2.dp)).background(navigationIndicatorColor)) } }, label = { Text("Comunidades", fontSize = RemoteAppearance.navigationLabelSize.sp) }, colors = navColors)
                    NavigationBarItem(selected = false, onClick = { showSettings = false; showEditProfile = false; showNotifications = false; publicProfileUsername = ""; showAuth = false; showCreateOptions = true }, icon = {
                        Box(
                            Modifier.size(width = 30.dp, height = 30.dp)
                                .border(2.dp, Color(0xFF1677F2), RoundedCornerShape(9.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Criar publicação", modifier = Modifier.size(22.dp), tint = Color(0xFF1677F2))
                        }
                    }, label = { Text(RemoteAppearance.navigationLabels[2], fontSize = RemoteAppearance.navigationLabelSize.sp) }, colors = navColors)
                    NavigationBarItem(
                        selected = tab == 1 && !showNotifications,
                        onClick = { showSettings = false; showEditProfile = false; showNotifications = false; publicProfileUsername = ""; showAuth = false; if (tab == 1) navTopRequest++; tab = 1 },
                        icon = {
                            Box(Modifier.size(32.dp), contentAlignment = Alignment.Center) {
                                if (RemoteAppearance.navigationUnderlineVisible && tab == 1 && !showNotifications) Box(Modifier.align(Alignment.BottomCenter).offset(y = RemoteAppearance.navigationUnderlineOffset.dp).width(RemoteAppearance.navigationUnderlineWidth.dp).height(RemoteAppearance.navigationUnderlineHeight.dp).clip(RoundedCornerShape(2.dp)).background(navigationIndicatorColor))
                                Icon(Icons.Default.ChatBubbleOutline, contentDescription = "Mensagens")
                                if (unreadMessages > 0) {
                                    Box(
                                        Modifier.align(Alignment.TopEnd)
                                            .offset(x = 7.dp, y = (-5).dp)
                                            .sizeIn(minWidth = 18.dp, minHeight = 18.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFFF3045))
                                            .padding(horizontal = 4.dp, vertical = 1.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            if (unreadMessages > 99) "99+" else unreadMessages.toString(),
                                            color = Color.Black,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        },
                        label = { Text(RemoteAppearance.navigationLabels[3], fontSize = RemoteAppearance.navigationLabelSize.sp) },
                        colors = navColors
                    )
                    NavigationBarItem(selected = tab == 4 && !showNotifications, onClick = { showSettings = false; showEditProfile = false; showNotifications = false; publicProfileUsername = ""; showAuth = false; if (tab == 4) navTopRequest++; tab = 4 }, icon = { Box(Modifier.size(32.dp), contentAlignment = Alignment.Center) { Icon(Icons.Default.Person, contentDescription = "Perfil"); if (RemoteAppearance.navigationUnderlineVisible && tab == 4 && !showNotifications) Box(Modifier.align(Alignment.BottomCenter).offset(y = RemoteAppearance.navigationUnderlineOffset.dp).width(RemoteAppearance.navigationUnderlineWidth.dp).height(RemoteAppearance.navigationUnderlineHeight.dp).clip(RoundedCornerShape(2.dp)).background(navigationIndicatorColor)) } }, label = { Text(RemoteAppearance.navigationLabels[4], fontSize = RemoteAppearance.navigationLabelSize.sp) }, colors = navColors)
                }
            }
            }
        }) { pad ->
            Column(
                Modifier.fillMaxSize()
                    .padding(pad)
                    .pointerInput(tab, showSettings, showEditProfile, showEditProfilePhoto, showNotifications, showAuth, publicProfileUsername) {
                        var horizontalDistance = 0f
                        detectHorizontalDragGestures(
                            onHorizontalDrag = { _, dragAmount ->
                                if (!showSettings && !showEditProfile && !showEditProfilePhoto &&
                                    !showAuth && publicProfileUsername.isBlank() && tab != 2
                                ) {
                                    horizontalDistance += dragAmount
                                }
                            },
                            onDragEnd = {
                                val threshold = 72.dp.toPx()
                                if (!showSettings && !showEditProfile && !showEditProfilePhoto &&
                                    !showAuth && publicProfileUsername.isBlank()
                                ) {
                                    if (horizontalDistance <= -threshold) {
                                        // Ordem do gesto: Início -> Mensagens -> Lives -> Avisos -> Perfil.
                                        // O botão "+" abre a câmera/publicação e não entra na navegação por gesto.
                                        if (showNotifications) {
                                            showNotifications = false
                                            tab = 4
                                        } else when (tab) {
                                            0 -> tab = 1
                                            1 -> tab = 3
                                            3 -> showNotifications = true
                                            4 -> { /* já está no último destino */ }
                                            else -> tab = 0
                                        }
                                    } else if (horizontalDistance >= threshold) {
                                        if (showNotifications) {
                                            showNotifications = false
                                            tab = 3
                                        } else when (tab) {
                                            0 -> { /* já está no primeiro destino */ }
                                            1 -> tab = 0
                                            3 -> tab = 1
                                            4 -> showNotifications = true
                                            else -> tab = 0
                                        }
                                    }
                                }
                                horizontalDistance = 0f
                            },
                            onDragCancel = { horizontalDistance = 0f }
                        )
                    }
            ) {
                if (publicProfileUsername.isNotBlank() || tab == 2) {
                    // Cabeçalho de marca removido para liberar espaço acima do perfil.
                } else if (!showSettings && !showEditProfile && !showEditProfilePhoto && !showNotifications && !showAuth) when (tab) {
                    0 -> Unit
                    1 -> Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("Mensagens", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    }
                    3 -> Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("Comunidades", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        IconButton(onClick = { showSearchDialog = true }) { Icon(Icons.Default.Search, contentDescription = "Pesquisar lives e pessoas", tint = Color.Black) }
                        IconButton(onClick = { showSettings = true }) { Icon(Icons.Default.Menu, contentDescription = "Abrir menu e configurações", tint = Color.Black) }
                    }
                     else -> Unit
                }
                if (showSettings) {
                    SettingsPage(
                        prefs = appPrefs,
                        username = loggedInUser,
                        nameFontStyle = nameFontStyle,
                        appFontStyle = appFontStyle,
                        onAppFontChange = { selected -> appFontStyle = selected; appPrefs.edit().putString("app_font_style", selected).apply() },
                        onNameFontChange = { selected -> nameFontStyle = selected; appPrefs.edit().putString("name_font_style", selected).apply() },
                        onBack = { showSettings = false },
                        onNotice = { notice = it },
                        onEditProfile = { showSettings = false; showEditProfile = true },
                        onAuth = { showSettings = false; showAuth = true; tab = 4 },
                        onSwitchAccounts = { showAccountSwitcher = true },
                        onLogout = {
                            // Encerrar apenas a sessão atual; manter as outras contas salvas para uso futuro.
                            appPrefs.edit()
                                .remove("token")
                                .remove("refresh_token")
                                .remove("username")
                                .remove("display_name")
                                .remove("bio")
                                .apply()
                            loggedInUser = ""
                            profileName = ""
                            profileBio = ""
                            showAccountSwitcher = false
                            showAuth = false
                            showSettings = false
                            showEditProfile = false
                            showEditProfilePhoto = false
                            publicProfileUsername = ""
                            messageRecipient = ""
                            tab = 4
                            notice = "Você saiu da sua conta."
                        }
                    )
                } else if (showNotifications) {
                    NotificationsPage(
                        token = appPrefs.getString("token", null) ?: "",
                        onBack = { showNotifications = false },
                        onOpen = { notification ->
                            showNotifications = false
                            if (notification.kind == "message" && notification.actorUsername.isNotBlank()) {
                                messageRecipient = notification.actorUsername
                                tab = 1
                            } else if (notification.actorUsername.isNotBlank()) {
                                publicProfileUsername = notification.actorUsername
                            }
                        },
                        onNotice = { notice = it }
                    )
                } else if (showEditProfilePhoto) {
                    EditProfilePhotoPage(
                        onBack = { showEditProfilePhoto = false },
                        onSaved = {
                            showEditProfilePhoto = false
                            profilePhotoRefreshKey++
                            notice = "Foto do perfil atualizada."
                        },
                        onNotice = { notice = it }
                    )
                } else if (showEditProfile) {
                    EditProfilePage(
                        photoRefreshKey = profilePhotoRefreshKey,
                        onEditProfilePhoto = { showEditProfile = false; showEditProfilePhoto = true },
                        initialUsername = loggedInUser,
                        initialName = profileName,
                        initialBio = profileBio,
                        onBack = { showEditProfile = false },
                        onSaved = { username, name, bio ->
                            val previousUsername = loggedInUser
                            val currentToken = appPrefs.getString("token", "") ?: ""
                            val currentRefreshToken = appPrefs.getString("refresh_token", "") ?: ""
                            // Atualizar a conta existente pelo identificador atual; não manter o @ antigo como outra conta.
                            val remainingAccounts = savedVyboraAccounts(appPrefs).filterNot {
                                it.username.equals(previousUsername, true) || it.username.equals(username, true)
                            }
                            val accountsJson = JSONArray()
                            remainingAccounts.forEach { a ->
                                accountsJson.put(JSONObject().put("username", a.username).put("token", a.token)
                                    .put("displayName", a.displayName).put("bio", a.bio).put("refreshToken", a.refreshToken))
                            }
                            appPrefs.edit().putString("vybora_accounts", accountsJson.toString())
                                .putString("username", username).putString("display_name", name).putString("bio", bio).apply()
                            if (currentToken.isNotBlank()) rememberVyboraAccount(appPrefs, username, currentToken, name, bio, currentRefreshToken)
                            loggedInUser = username
                            profileName = name
                            profileBio = bio
                            showEditProfile = false
                            publicProfileUsername = ""
                            tab = 4
                            navTopRequest++
                            notice = "Perfil atualizado e sincronizado."
                        },
                        onNotice = { notice = it }
                    )
                } else if (publicProfileUsername.isNotBlank()) {
                    PublicProfilePage(
                        username = publicProfileUsername,
                        onBack = { publicProfileUsername = "" },
                        onMessage = { recipient -> publicProfileUsername = ""; messageRecipient = recipient; tab = 1 },
                        onNotice = { notice = it }
                    )
                } else androidx.compose.runtime.key(accountSessionVersion) { when (tab) {
                    0 -> FeedPage(scrollToTopRequest = navTopRequest, onlineUsers = onlineUsers, currentUsername = loggedInUser, photoRefreshKey = profilePhotoRefreshKey, nameFontStyle = nameFontStyle, onNotice = { notice = it }, onOpenProfile = { publicProfileUsername = it }, onAddStory = { tab = 2 }, onSearch = { showSearchDialog = true }, onNotifications = { showNotifications = true })
                    1 -> MessagesPage(scrollToTopRequest = navTopRequest, recipientUsername = messageRecipient, onSearch = { showSearchDialog = true }, onOpenConversation = { messageRecipient = it }, onOpenProfile = { publicProfileUsername = it }, onNotice = { notice = it })
                    2 -> PostPage(onNotice = { notice = it }, onCameraCancelled = { tab = 0 }, initialMode = createEntryMode)
                    3 -> CommunitiesPage(onNotice = { notice = it })
                    else -> if (showAuth) {
                        AuthPage(
                            onSuccess = { username ->
                                loggedInUser = username
                                showAuth = false
                                notice = "Login realizado com sucesso!"
                                val token = appPrefs.getString("token", null)
                                if (!token.isNullOrBlank()) Thread {
                                    try {
                                        val response = VyboraApi.getMyProfile(token)
                                        val user = response.optJSONObject("user")
                                        if (user != null) {
                                            val serverName = user.optString("displayName", user.optString("display_name"))
                                            val serverBio = user.optString("bio")
                                            android.os.Handler(android.os.Looper.getMainLooper()).post {
                                                profileName = serverName
                                                profileBio = serverBio
                                                appPrefs.edit().putString("display_name", serverName).putString("bio", serverBio).apply()
                                            }
                                        }
                                    } catch (_: Exception) { }
                                }.start()
                            },
                            onCancel = { showAuth = false }
                        )
                    } else ProfilePage(scrollToTopRequest = navTopRequest, username = loggedInUser, displayName = profileName, bio = profileBio, photoRefreshKey = profilePhotoRefreshKey, onNotice = { notice = it }, onStat = { stat ->
                        profileStat = stat
                        profileStatUsers = emptyList()
                        val tokenUser = loggedInUser
                        if (tokenUser.isNotBlank() && (stat == "Seguidores" || stat == "Seguindo")) {
                            Thread {
                                try {
                                    val users = VyboraApi.getConnections(tokenUser, if (stat == "Seguidores") "followers" else "following")
                                    android.os.Handler(android.os.Looper.getMainLooper()).post { profileStatUsers = users }
                                } catch (e: Exception) {
                                    android.os.Handler(android.os.Looper.getMainLooper()).post { notice = e.message ?: "Não foi possível carregar a lista." }
                                }
                            }.start()
                        }
                    }, onAuth = { showAuth = true }, onEdit = { showEditProfile = true; showSettings = false; showEditProfilePhoto = false; publicProfileUsername = "" }, onEditProfilePhoto = { showEditProfilePhoto = true; showEditProfile = false; showSettings = false; publicProfileUsername = "" }, onSettings = { showSettings = true })
                } }
            }
        }
        if (showAccountSwitcher) {
            AccountSwitcherDialog(savedVyboraAccounts(appPrefs), loggedInUser, { showAccountSwitcher = false }, { account ->
                appPrefs.edit().putString("token",account.token).putString("username",account.username).putString("display_name",account.displayName).putString("bio",account.bio).putString("refresh_token",account.refreshToken).apply()
                loggedInUser=account.username; profileName=account.displayName; profileBio=account.bio
                showAccountSwitcher=false; showSettings=false; showAuth=false; showEditProfile=false
                showEditProfilePhoto=false; showNotifications=false; showSearchDialog=false
                publicProfileUsername=""; messageRecipient=""; profileStat=""; profileStatUsers=emptyList()
                liked=emptySet(); unreadMessages=0; accountSessionVersion++
                tab=0
                notice="Conta alterada para @"+account.username+"."
                Thread {
                    try {
                        val response=VyboraApi.getMyProfile(account.token); val user=response.optJSONObject("user")
                        if(user!=null) {
                            val n=user.optString("displayName",user.optString("display_name")); val b=user.optString("bio"); val u=user.optString("username",account.username)
                            appPrefs.edit().putString("username",u).putString("display_name",n).putString("bio",b).apply()
                            rememberVyboraAccount(appPrefs,u,account.token,n,b)
                            android.os.Handler(android.os.Looper.getMainLooper()).post { loggedInUser=u; profileName=n; profileBio=b }
                        }
                    } catch (_:Exception) { android.os.Handler(android.os.Looper.getMainLooper()).post { notice="Conta selecionada; não foi possível atualizar o perfil pela rede agora." } }
                }.start()
            }, {
                showAccountSwitcher=false; showAuth=true; showSettings=false; tab=4
            }, { usernameToRemove ->
                val remaining = savedVyboraAccounts(appPrefs).filterNot { it.username.equals(usernameToRemove, true) }
                val saved = JSONArray()
                remaining.forEach { account ->
                    saved.put(JSONObject().put("username", account.username).put("token", account.token)
                        .put("displayName", account.displayName).put("bio", account.bio).put("refreshToken", account.refreshToken))
                }
                val removingCurrent = loggedInUser.equals(usernameToRemove, true)
                val edit = appPrefs.edit().putString("vybora_accounts", saved.toString())
                if (removingCurrent) {
                    edit.remove("token").remove("refresh_token").remove("username").remove("display_name").remove("bio")
                    loggedInUser = ""; profileName = ""; profileBio = ""
                    showAccountSwitcher = false; showAuth = true
                    notice = "Conta removida deste aparelho. Entre novamente para continuar."
                } else {
                    notice = "Conta removida da lista deste aparelho."
                }
                edit.apply()
            })
        }
        if (showCreateOptions) {
            androidx.compose.ui.window.Popup(
                alignment = androidx.compose.ui.Alignment.BottomCenter,
                offset = androidx.compose.ui.unit.IntOffset(
                    with(androidx.compose.ui.platform.LocalDensity.current) { 0.dp.roundToPx() },
                    with(androidx.compose.ui.platform.LocalDensity.current) { (-104).dp.roundToPx() }
                ),
                onDismissRequest = { showCreateOptions = false },
                properties = androidx.compose.ui.window.PopupProperties(focusable = true)
            ) {
                Column(
                    modifier = Modifier.width(103.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        "Texto" to "✎",
                        "Galeria" to "▣",
                        "Câmera" to "◎",
                        "Enquete" to "▤",
                        "Live" to "◉"
                    ).forEach { (label, symbol) ->
                        val onChoice: () -> Unit = {
                            showCreateOptions = false
                            when (label) {
                                "Galeria", "Câmera", "Texto", "Enquete" -> {
                                    createEntryMode = when (label) { "Galeria" -> "gallery"; "Texto" -> "text"; "Enquete" -> "poll"; else -> "camera" }
                                    tab = 2
                                }
                                "Live" -> notice = "Transmissões ao vivo ainda não estão disponíveis."
                            }
                        }
                        if (label == "Live") {
                            // O botão Live e o bico inclinado são UM contorno fechado.
                            // Altura do bico: 10 dp (equivalente lógico aos 10 px pedidos).
                            androidx.compose.foundation.layout.Box(
                                modifier = Modifier.fillMaxWidth().height(48.dp).clickable(onClick = onChoice)
                            ) {
                                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                                    val sx = size.width / 103f
                                    val sy = size.height / 48f
                                    val outline = androidx.compose.ui.graphics.Path().apply {
                                        moveTo(19f * sx, 1f * sy)
                                        lineTo(85f * sx, 1f * sy)
                                        quadraticTo(102f * sx, 1f * sy, 102f * sx, 18f * sy)
                                        lineTo(102f * sx, 21f * sy)
                                        quadraticTo(102f * sx, 38f * sy, 85f * sx, 38f * sy)
                                        lineTo(58f * sx, 38f * sy)
                                        lineTo(51.5f * sx, 47f * sy)
                                        lineTo(45f * sx, 38f * sy)
                                        lineTo(19f * sx, 38f * sy)
                                        quadraticTo(7f * sx, 38f * sy, 7f * sx, 21f * sy)
                                        lineTo(7f * sx, 18f * sy)
                                        quadraticTo(7f * sx, 1f * sy, 19f * sx, 1f * sy)
                                        close()
                                    }
                                    drawPath(outline, if (QiblyTheme.dark) Color(0xFF20242C) else Color.White)
                                    drawPath(
                                        outline,
                                        Color(0xFFBFD5F5),
                                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
                                    )
                                }
                                Row(
                                    modifier = Modifier.padding(start = 16.dp, top = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                                ) {
                                    Text(symbol, color = Color(0xFF1677F2), fontSize = 18.sp)
                                    Text(label, color = if (QiblyTheme.dark) Color.White else Color(0xFF172337), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth()
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(if (QiblyTheme.dark) Color(0xFF20242C) else Color.White)
                                    .border(1.dp, Color(0xFFBFD5F5), RoundedCornerShape(24.dp))
                                    .clickable(onClick = onChoice)
                                    .padding(horizontal = 9.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(7.dp)
                            ) {
                                Text(symbol, color = Color(0xFF1677F2), fontSize = 18.sp)
                                Text(label, color = if (QiblyTheme.dark) Color.White else Color(0xFF172337), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
        if (showSearchDialog) {
            SearchUsersDialog(
                searchContext = when (tab) { 0 -> "home"; 1 -> "messages"; 3 -> "lives"; else -> "people" },
                onDismiss = { showSearchDialog = false },
                onOpenProfile = { username ->
                    showSearchDialog = false
                    if (tab == 1) {
                        messageRecipient = username
                    } else {
                        publicProfileUsername = username
                    }
                },
                onError = { notice = it }
            )
        }
        if (profileStat.isNotBlank()) {
            androidx.compose.ui.window.Dialog(
                onDismissRequest = { profileStat = "" },
                properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Night,
                    shape = RoundedCornerShape(0.dp)
                ) {
                    Column(Modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 12.dp)) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { profileStat = "" }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = Color.Black)
                            }
                            Column(Modifier.weight(1f)) {
                                Text(profileStat, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color.Black)
                                Text(
                                    if (profileStat == "Seguidores") "Pessoas que seguem você" else if (profileStat == "Seguindo") "Pessoas que você segue" else "Atividade do perfil",
                                    color = Muted,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        Divider(color = Color.White.copy(alpha = 0.08f))
                        if (profileStat == "Seguidores" || profileStat == "Seguindo") {
                            when {
                                loggedInUser.isBlank() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("Entre na sua conta para consultar esta lista.", color = Muted)
                                }
                                profileStatUsers.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("Nenhuma conta nesta lista.", color = Muted)
                                }
                                else -> LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(vertical = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    items(profileStatUsers, key = { it.id }) { user ->
                                        Row(
                                            Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).clickable {
                                                publicProfileUsername = user.username
                                                profileStat = ""
                                            }.padding(horizontal = 8.dp, vertical = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                Modifier.size(50.dp).clip(CircleShape).background(Brush.linearGradient(listOf(Pink, Violet))),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (user.avatarUrl.isNotBlank()) {
                                                    AsyncImage(model = user.avatarUrl, contentDescription = "Foto de @${user.username}", modifier = Modifier.fillMaxSize().clip(CircleShape), contentScale = androidx.compose.ui.layout.ContentScale.Crop)
                                                } else {
                                                    Text(user.displayName.ifBlank { user.username }.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                            Column(Modifier.weight(1f).padding(start = 12.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                                Text(user.displayName.ifBlank { user.username }, fontWeight = FontWeight.SemiBold, color = Color.Black)
                                                Text("@${user.username}", color = Muted, style = MaterialTheme.typography.bodySmall)
                                            }
                                            Icon(Icons.Default.ChevronRight, contentDescription = "Abrir perfil", tint = Muted)
                                        }
                                    }
                                }
                            }
                        } else {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("As publicações da sua conta serão exibidas quando o feed estiver carregado.", color = Muted)
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun NotificationsPage(
    token: String,
    onBack: () -> Unit,
    onOpen: (VyboraApi.NotificationSummary) -> Unit,
    onNotice: (String) -> Unit
) {
    var notifications by remember { mutableStateOf<List<VyboraApi.NotificationSummary>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var selectedFilter by remember { mutableStateOf("Todas") }
    LaunchedEffect(token) {
        if (token.isBlank()) {
            loading = false
        } else {
            try {
                val loaded = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) { VyboraApi.getNotifications(token) }
                val demoNotifications = listOf(
                    VyboraApi.NotificationSummary("demo-notif-01", "like", "demo-post-06", "Agora", "", "carol.mendes.demo", "Carol Mendes", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=160"),
                    VyboraApi.NotificationSummary("demo-notif-02", "comment", "demo-post-07", "Agora", "", "julia.ribeiro.demo", "Júlia Ribeiro", "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=160"),
                    VyboraApi.NotificationSummary("demo-notif-03", "follow", "", "Agora", "", "marina.costa.demo", "Marina Costa", "https://images.unsplash.com/photo-1531123897727-8f129e1688ce?w=160"),
                    VyboraApi.NotificationSummary("demo-notif-04", "system", "", "Hoje", "", "qinbly", "QINBLY", "")
                )
                notifications = (loaded + demoNotifications).distinctBy { it.id }
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) { VyboraApi.markNotificationsRead(token) }
            } catch (e: Exception) {
                onNotice(e.message ?: "Não foi possível carregar as notificações.")
            } finally {
                loading = false
            }
        }
    }
    // Mensagens pertencem à aba Mensagens; Avisos mostra somente atividade e avisos do sistema.
    val visible = notifications.filter { item ->
        item.kind != "message" && when (selectedFilter) {
            "Atividades" -> item.kind != "system"
            "Do QINBLY" -> item.kind == "system"
            else -> true
        }
    }
    Column(Modifier.fillMaxSize().background(RemoteAppearance.screenBackgroundColor).padding(horizontal = 18.dp, vertical = 10.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = RemoteAppearance.screenTextColor) }
            Text("Notificações", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Box(Modifier.size(30.dp), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Notifications, contentDescription = null, tint = Pink)
                val unreadVisibleNotifications = notifications.count { it.readAt.isBlank() && it.kind != "message" }
                if (unreadVisibleNotifications > 0) {
                    Box(
                        Modifier.align(Alignment.TopEnd).sizeIn(minWidth = 17.dp, minHeight = 17.dp)
                            .clip(CircleShape).background(Color(0xFFFF3045)).padding(horizontal = 3.dp, vertical = 1.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(if (unreadVisibleNotifications > 99) "99+" else unreadVisibleNotifications.toString(),
                            color = RemoteAppearance.screenTextColor, fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Todas", "Atividades", "Do QINBLY").forEach { label ->                Surface(                    modifier = Modifier.clickable { selectedFilter = label },                    color = if (selectedFilter == label) Color(0xFF35113B) else Panel,
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (selectedFilter == label) Pink else Color.Transparent)
                ) {
                    Text(label, modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp), color = if (selectedFilter == label) Pink else Muted, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
        when {
            loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Pink) }
            token.isBlank() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Entre na sua conta para ver as notificações.", color = RemoteAppearance.screenSecondaryTextColor) }
            visible.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Default.Notifications, contentDescription = null, tint = Violet, modifier = Modifier.size(48.dp))
                    Text("Você está em dia!", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Novos seguidores, curtidas e comentários aparecerão aqui.", color = RemoteAppearance.screenSecondaryTextColor, style = MaterialTheme.typography.bodyMedium)
                }
            }
            else -> LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(6.dp), contentPadding = PaddingValues(bottom = 20.dp)) {
                items(visible, key = { it.id }) { item ->
                    val actor = item.actorDisplayName.ifBlank { item.actorUsername.ifBlank { "QINBLY" } }
                    val description = when (item.kind) {
                        "follow" -> "começou a seguir você"
                        "like" -> "curtiu sua publicação"
                        "comment" -> "comentou na sua publicação"
                        "message" -> "enviou uma mensagem"
                        "system" -> "aviso do QINBLY"
                        else -> "tem uma novidade para você"
                    }
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(if (item.readAt.isBlank()) Color(0xFF171024) else Night)
                            .clickable { onOpen(item) }.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (item.actorAvatarUrl.isNotBlank()) {
                            AsyncImage(model = item.actorAvatarUrl, contentDescription = "Foto de $actor", modifier = Modifier.size(46.dp).clip(CircleShape), contentScale = androidx.compose.ui.layout.ContentScale.Crop)
                        } else {
                            Box(Modifier.size(46.dp).clip(CircleShape).background(Brush.linearGradient(listOf(Pink, Violet))), contentAlignment = Alignment.Center) {
                                Icon(if (item.kind == "system") Icons.Default.Info else Icons.Default.Notifications, contentDescription = null, tint = RemoteAppearance.screenTextColor)
                            }
                        }
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text("$actor $description", color = RemoteAppearance.screenTextColor, style = MaterialTheme.typography.bodyMedium)
                            Text(item.createdAt, color = RemoteAppearance.screenSecondaryTextColor, style = MaterialTheme.typography.labelSmall)
                        }
                        if (item.readAt.isBlank()) Box(Modifier.size(9.dp).clip(CircleShape).background(Color(0xFFFF3045)))
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchUsersDialog(searchContext: String, onDismiss: () -> Unit, onOpenProfile: (String) -> Unit, onError: (String) -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { context.getSharedPreferences("vybora_session", android.content.Context.MODE_PRIVATE) }
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<VyboraApi.UserSummary>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var searched by remember { mutableStateOf(false) }
    var searchError by remember { mutableStateOf("") }
    val followingByUser = remember { mutableStateMapOf<String, Boolean>() }
    val followingBusy = remember { mutableStateMapOf<String, Boolean>() }

    // Só consulta quando há pelo menos dois caracteres e descarta resultados de buscas antigas.
    var searchRequest by remember { mutableIntStateOf(0) }
    LaunchedEffect(query) {
        val requestId = ++searchRequest
        kotlinx.coroutines.delay(250)
        val normalized = query.trim().removePrefix("@").trim()
        searchError = ""
        if (normalized.length < 2) {
            results = emptyList()
            loading = false
            searched = false
        } else {
            loading = true
            try {
                val found = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    VyboraApi.searchUsers(normalized, prefs.getString("token", null))
                }
                if (requestId == searchRequest) {
                    results = found
                    searched = true
                }
            } catch (e: Exception) {
                if (requestId == searchRequest) {
                    searchError = e.message ?: "Não foi possível pesquisar usuários."
                    results = emptyList()
                    searched = true
                }
            } finally {
                if (requestId == searchRequest) loading = false
            }
        }
    }

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = Night, shape = RoundedCornerShape(0.dp)) {
            Column(Modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 10.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = Color.Black)
                    }
                    Text(
                        when (searchContext) {
                            "messages" -> "Encontrar pessoas"
                            "lives" -> "Buscar usuários e lives"
                            "home" -> "Buscar pessoas e publicações"
                            else -> "Buscar pessoas"
                        },
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it.take(40) },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 12.dp),
                    singleLine = true,
                    placeholder = { Text("Nome ou @usuário") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Muted) },
                    trailingIcon = {
                        if (query.isNotEmpty()) IconButton(onClick = { query = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Limpar busca", tint = Muted)
                        }
                    },
                    shape = RoundedCornerShape(16.dp)
                )
                Text(
                    if (query.isBlank()) "Sugestões de pessoas" else "Resultados da busca",
                    color = Muted,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(vertical = 6.dp)
                )
                when {
                    loading && results.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Pink)
                    }
                    searchError.isNotBlank() && results.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(searchError, color = Muted, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                            Button(onClick = { query = query + " " }) { Text("Tentar novamente") }
                        }
                    }
                    query.trim().removePrefix("@").trim().length < 2 -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Digite pelo menos 2 letras para encontrar usuários.", color = Muted)
                    }
                    searched && results.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Nenhum usuário encontrado para essa pesquisa.", color = Muted)
                    }
                    else -> LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                        contentPadding = PaddingValues(bottom = 20.dp)
                    ) {
                        items(results, key = { it.id }) { user ->
                            Row(
                                Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                                    .clickable { onOpenProfile(user.username) }
                                    .padding(horizontal = 4.dp, vertical = 11.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    Modifier.size(52.dp).clip(CircleShape).background(Brush.linearGradient(listOf(Pink, Violet))),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (user.avatarUrl.isNotBlank()) {
                                        AsyncImage(model = user.avatarUrl, contentDescription = "Foto de perfil", modifier = Modifier.fillMaxSize().clip(CircleShape), contentScale = androidx.compose.ui.layout.ContentScale.Crop)
                                    } else {
                                        Text(user.displayName.ifBlank { user.username }.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Text(user.displayName.ifBlank { user.username }, color = Color.Black, fontWeight = FontWeight.SemiBold)
                                    Text("@${user.username}", color = Muted, style = MaterialTheme.typography.bodySmall)
                                    if (user.bio.isNotBlank()) Text(user.bio, color = Muted, maxLines = 1, style = MaterialTheme.typography.labelSmall)
                                }
                                TextButton(
                                    enabled = followingBusy[user.id] != true,
                                    onClick = {
                                        val token = prefs.getString("token", null)
                                        if (token.isNullOrBlank()) onError("Entre na sua conta para seguir pessoas.")
                                        else {
                                            followingBusy[user.id] = true
                                            Thread {
                                                try {
                                                    val following = VyboraApi.toggleFollow(token, user.id)
                                                    android.os.Handler(android.os.Looper.getMainLooper()).post {
                                                        followingByUser[user.id] = following
                                                        followingBusy[user.id] = false
                                                    }
                                                } catch (e: Exception) {
                                                    android.os.Handler(android.os.Looper.getMainLooper()).post {
                                                        followingBusy[user.id] = false
                                                        onError(e.message ?: "Não foi possível atualizar o seguimento.")
                                                    }
                                                }
                                            }.start()
                                        }
                                    }
                                ) { Text(if (followingBusy[user.id] == true) "Aguarde..." else if (followingByUser[user.id] == true) "Seguindo" else "Seguir", color = Pink) }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VyboraFullscreenVideo(url: String, modifier: Modifier = Modifier) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val player = remember(url) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(url))
            repeatMode = androidx.media3.common.Player.REPEAT_MODE_ONE
            prepare()
            playWhenReady = true
        }
    }
    var playing by remember(url) { mutableStateOf(true) }
    var position by remember(url) { mutableStateOf(0f) }
    var duration by remember(url) { mutableStateOf(0L) }
    DisposableEffect(player) { onDispose { player.release() } }
    androidx.compose.runtime.LaunchedEffect(player) {
        while (true) {
            val d = player.duration
            if (d > 0) {
                duration = d
                position = player.currentPosition.coerceIn(0L, d).toFloat() / d.toFloat()
            }
            kotlinx.coroutines.delay(200L)
        }
    }
    Box(modifier.background(Color.Black)) {
        AndroidView(modifier = Modifier.fillMaxSize(), factory = { ctx -> PlayerView(ctx).apply { this.player = player; useController = false } }, update = { view -> view.player = player; view.useController = false })
        Box(Modifier.fillMaxSize().clickable {
            if (player.isPlaying) { player.pause(); playing = false } else { player.play(); playing = true }
        }, contentAlignment = Alignment.Center) {
            if (!playing) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Continuar vídeo", tint = Color.White, modifier = Modifier.size(52.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.42f)).padding(12.dp))
            }
        }
        Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(start = 18.dp, end = 18.dp, bottom = 68.dp)) {
            val seconds = (position * duration).toLong().coerceAtLeast(0L) / 1000L
            val total = duration.coerceAtLeast(0L) / 1000L
            Text("${seconds / 60}:${(seconds % 60).toString().padStart(2, '0')} / ${total / 60}:${(total % 60).toString().padStart(2, '0')}", color = Color.Black, fontSize = 12.sp)
            BoxWithConstraints(
                Modifier.fillMaxWidth().height(24.dp)
                    .pointerInput(duration) {
                        detectTapGestures { tap: androidx.compose.ui.geometry.Offset ->
                            val fraction = (tap.x / size.width.toFloat()).coerceIn(0f, 1f)
                            position = fraction
                            if (duration > 0L) player.seekTo((duration * fraction).toLong())
                        }
                    }
                    .pointerInput(duration) {
                        detectDragGestures { change, _ ->
                            val fraction = (change.position.x / size.width.toFloat()).coerceIn(0f, 1f)
                            position = fraction
                            if (duration > 0L) player.seekTo((duration * fraction).toLong())
                            change.consume()
                        }
                    },
                contentAlignment = Alignment.CenterStart
            ) {
                Box(Modifier.fillMaxWidth().height(3.dp).clip(RoundedCornerShape(3.dp)).background(Color.White.copy(alpha = 0.48f)))
                Box(Modifier.fillMaxWidth(position.coerceIn(0f, 1f)).height(3.dp).clip(RoundedCornerShape(3.dp)).background(Color(0xFF168BFF)))
                Box(Modifier.offset(x = maxWidth * position.coerceIn(0f, 1f) - 4.dp).size(8.dp).clip(CircleShape).background(Color(0xFF168BFF)))
            }
        }
    }
}

@Composable
private fun InlineVideoPlayer(url: String, modifier: Modifier = Modifier, autoplay: Boolean = false) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var playbackError by remember(url) { mutableStateOf(false) }
    var isPlaying by remember(url) { mutableStateOf(autoplay) }
    val player = remember(url) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(url))
            playWhenReady = autoplay
            addListener(object : androidx.media3.common.Player.Listener {
                override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                    playbackError = true
                    isPlaying = false
                }
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == androidx.media3.common.Player.STATE_READY) playbackError = false
                }
            })
            prepare()
        }
    }
    DisposableEffect(player) { onDispose { player.release() } }
    Box(modifier = modifier.background(Color.Black), contentAlignment = Alignment.Center) {
        if (playbackError) {
            AsyncImage(
                model = "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?auto=format&fit=crop&w=1200&h=700&q=85",
                contentDescription = "Prévia do vídeo",
                modifier = Modifier.fillMaxSize(),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )
            Box(Modifier.size(62.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.62f))
                .clickable {
                    playbackError = false
                    player.prepare()
                    player.play()
                    isPlaying = true
                }, contentAlignment = Alignment.Center) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Tentar reproduzir vídeo", tint = Color.White, modifier = Modifier.size(38.dp))
            }
        } else {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { viewContext -> PlayerView(viewContext).apply {
                    this.player = player
                    useController = false
                    setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
                    resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                } },
                update = { view ->
                    view.player = player
                    view.useController = false
                    view.resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                }
            )
            if (!isPlaying) {
                Box(
                    modifier = Modifier.size(62.dp).clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.55f))
                        .clickable { player.play(); isPlaying = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Reproduzir vídeo", tint = Color.White, modifier = Modifier.size(38.dp))
                }
            } else {
                Box(Modifier.fillMaxSize().clickable { player.pause(); isPlaying = false })
            }
        }
    }
}

@Composable
private fun QiblyShareDialog(
    post: VyboraApi.PostSummary,
    prefs: android.content.SharedPreferences,
    onDismiss: () -> Unit,
    onNotice: (String) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    var query by remember { mutableStateOf("") }
    var inbox by remember { mutableStateOf<List<VyboraApi.InboxSummary>>(emptyList()) }
    var users by remember { mutableStateOf<List<VyboraApi.UserSummary>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var sendingTo by remember { mutableStateOf("") }
    val chosen = remember { mutableStateListOf<String>() }
    val token = prefs.getString("token", null)
    val shareLink = "https://vybora.com/post/${Uri.encode(post.id)}"
    val message = "Confira esta publicação no QINBLY: " + post.caption.take(160) + "\n" + shareLink
    LaunchedEffect(token) {
        if (!token.isNullOrBlank()) {
            try { inbox = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) { VyboraApi.getInbox(token) } }
            catch (_: Exception) { }
        }
        loading = false
    }
    LaunchedEffect(query, token) {
        users = emptyList()
        if (query.trim().length >= 2) {
            kotlinx.coroutines.delay(300)
            try {
                users = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    VyboraApi.searchUsers(query.trim().removePrefix("@"), token)
                }
            } catch (_: Exception) { }
        }
    }
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            Modifier.fillMaxWidth().fillMaxHeight(0.82f)
                .background(QiblyTheme.card, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .padding(18.dp)
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Compartilhar", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 20.sp, color = QiblyTheme.text)
                IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "Fechar", tint = QiblyTheme.text) }
            }
            Text(post.caption.ifBlank { "Publicação do QINBLY" }.take(120), maxLines = 2, color = QiblyTheme.secondary)
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = query, onValueChange = { query = it.take(50) },
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                label = { Text("Buscar pessoas pelo nome ou @") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
            )
            Spacer(Modifier.height(12.dp))
            Text(if (query.isBlank()) "Conversas recentes" else "Pessoas encontradas", color = QiblyTheme.text, fontWeight = FontWeight.SemiBold)
            if (loading) CircularProgressIndicator(Modifier.padding(12.dp))
            val recipients = if (query.isBlank()) inbox.map { it.username to it.displayName }
                else users.map { it.username to it.displayName }
            androidx.compose.foundation.lazy.LazyColumn(Modifier.weight(1f)) {
                items(recipients.distinctBy { it.first }) { (username, name) ->
                    Row(
                        Modifier.fillMaxWidth().clickable {
                            if (username in chosen) chosen.remove(username) else chosen.add(username)
                        }.padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF1677F2))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(name.ifBlank { username }, color = QiblyTheme.text, fontWeight = FontWeight.Medium)
                            Text("@$username", color = QiblyTheme.secondary, fontSize = 12.sp)
                        }
                        androidx.compose.material3.Checkbox(
                            checked = username in chosen,
                            onCheckedChange = { if (it) { if (username !in chosen) chosen.add(username) } else chosen.remove(username) }
                        )
                    }
                }
                if (!loading && recipients.isEmpty()) item {
                    Text(if (query.isBlank()) "Nenhuma conversa recente. Pesquise uma pessoa para enviar." else "Nenhuma pessoa encontrada.", modifier = Modifier.padding(14.dp), color = QiblyTheme.secondary)
                }
            }
            Button(
                onClick = {
                    if (token.isNullOrBlank()) {
                        onNotice("Entre na sua conta para enviar publicações por mensagem.")
                    } else if (chosen.isNotEmpty() && sendingTo.isBlank()) {
                        val targets = chosen.toList()
                        sendingTo = "Enviando"
                        scope.launch {
                            var sent = 0
                            for (username in targets) {
                                try {
                                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                                        VyboraApi.sendMessage(token, username, message)
                                    }
                                    sent++
                                } catch (_: Exception) { }
                            }
                            sendingTo = ""
                            onNotice(if (sent == targets.size) "Publicação enviada para $sent pessoa(s)." else "Enviado para $sent de ${targets.size} pessoa(s).")
                            if (sent > 0) onDismiss()
                        }
                    }
                },
                enabled = chosen.isNotEmpty() && sendingTo.isBlank(),
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (sendingTo.isNotBlank()) "Enviando..." else "Enviar para ${chosen.size} pessoa(s)") }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                TextButton(onClick = {
                    val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                    clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Publicação QINBLY", shareLink))
                    onNotice("Link copiado.")
                }) { Text("Copiar link") }
                TextButton(onClick = {
                    try {
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, message)
                        }
                        context.startActivity(Intent.createChooser(intent, "Compartilhar em outro aplicativo"))
                    } catch (_: Exception) { onNotice("Não foi possível abrir o compartilhamento.") }
                }) { Text("Outros aplicativos") }
            }
        }
    }
}

@Composable
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
private fun FeedPage(scrollToTopRequest: Int, onlineUsers: Int, currentUsername: String, photoRefreshKey: Int, nameFontStyle: String, onNotice: (String) -> Unit, onOpenProfile: (String) -> Unit, onAddStory: () -> Unit, onSearch: () -> Unit, onNotifications: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { context.getSharedPreferences("vybora_session", android.content.Context.MODE_PRIVATE) }
    var posts by remember { mutableStateOf<List<VyboraApi.PostSummary>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var refreshKey by remember { mutableIntStateOf(0) }
    var refreshingFeed by remember { mutableStateOf(false) }
    var commentPost by remember { mutableStateOf<VyboraApi.PostSummary?>(null) }
    var sharePost by remember { mutableStateOf<VyboraApi.PostSummary?>(null) }
    var comments by remember { mutableStateOf<List<VyboraApi.CommentSummary>>(emptyList()) }
    var commentDraft by remember { mutableStateOf("") }
    var commentsLoading by remember { mutableStateOf(false) }
    var expandedMedia by remember { mutableStateOf("") }
    var fullscreenVideoId by remember { mutableStateOf<String?>(null) }
    var selectedStory by remember { mutableStateOf<Pair<String, String>?>(null) }
    var savedPostIds by remember { mutableStateOf(prefs.getStringSet("saved_post_ids", emptySet())?.toSet() ?: emptySet()) }
    var repostedPostIds by remember { mutableStateOf(prefs.getStringSet("reposted_post_ids_local", emptySet())?.toSet() ?: emptySet()) }
    var unreadNotifications by remember { mutableIntStateOf(0) }
    var selectedFeedTab by remember { mutableStateOf("Descobrir") }
    var selectedFeedFilter by remember { mutableStateOf("Novidades") }
    LaunchedEffect(prefs.getString("token", null)) {
        val token = prefs.getString("token", null)
        if (token.isNullOrBlank()) {
            unreadNotifications = 0
        } else {
            while (true) {
                try {
                    val loaded = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) { VyboraApi.getNotifications(token) }
                    unreadNotifications = loaded.count { it.readAt.isBlank() && it.kind != "message" }
                } catch (_: Exception) {
                    // Mantém o último contador confirmado se a rede estiver indisponível.
                }
                kotlinx.coroutines.delay(15_000)
            }
        }
    }
    // Perfis/publicações demonstrativas locais para testar a interface sem tocar no banco.
    var demoLikes by remember { mutableStateOf(prefs.getStringSet("demo_liked_post_ids", emptySet())?.toSet() ?: emptySet()) }
    var demoComments by remember { mutableStateOf<Map<String, List<VyboraApi.CommentSummary>>>(emptyMap()) }
    // O feed usa somente publicações reais retornadas pela API.
    val allFeedPosts = posts.sortedByDescending { it.createdAt }.distinctBy { it.id }
    val feedTabPosts = when (selectedFeedTab) {
        "Seguindo" -> allFeedPosts.filter { it.isFollowing }
        "Comunidade" -> allFeedPosts.filter { it.comments > 0 }
        else -> allFeedPosts
    }
    val displayedPosts = when (selectedFeedFilter) {
        "Vídeos" -> feedTabPosts.filter { it.mediaType.equals("video", ignoreCase = true) }
        "Fotos" -> feedTabPosts.filter { it.mediaType.equals("image", ignoreCase = true) }
        "Lives" -> feedTabPosts.filter { it.mediaType.equals("live", ignoreCase = true) }
        else -> feedTabPosts
    }
    LaunchedEffect(refreshKey, photoRefreshKey) {
        loading = true
        Thread {
            try {
                val loaded = VyboraApi.getPosts(prefs.getString("token", null))
                android.os.Handler(android.os.Looper.getMainLooper()).post { posts = loaded; loading = false; refreshingFeed = false }
            } catch (e: Exception) {
                val cached = VyboraApi.getCachedPosts(context)
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    if (cached.isNotEmpty()) posts = cached
                    loading = false
                    refreshingFeed = false
                    onNotice(if (cached.isNotEmpty()) "Sem conexão: mostrando o último feed sincronizado." else (e.message ?: "Não foi possível carregar o feed."))
                }
            }
        }.start()
    }
    Column(Modifier.fillMaxSize().background(RemoteAppearance.headerBackgroundColor).padding(0.dp)) {
        Row(
            Modifier.fillMaxWidth().padding(start = RemoteAppearance.headerHorizontalPadding.dp, end = 12.dp, top = RemoteAppearance.headerVerticalPadding.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                listOf("Descobrir", "Seguindo").forEachIndexed { feedIndex, feedTab ->
                    val selected = selectedFeedTab == feedTab
                    Column(
                        modifier = Modifier.clickable { selectedFeedTab = feedTab }
                            .padding(horizontal = 2.dp, vertical = 5.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            if (RemoteAppearance.headerVisible) feedTab else "",
                            color = RemoteAppearance.headerTextColor,
                            fontSize = 18.sp,
                            maxLines = 1,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                        )
                        Spacer(Modifier.height(7.dp))
                        Box(
                            Modifier.width(if (selected) 64.dp else 0.dp).height(3.dp)
                                .clip(CircleShape).background(Color(0xFF1677F2))
                        )
                    }
                    if (feedIndex == 0) {
                        Box(
                            Modifier.padding(horizontal = 8.5.dp).width(1.dp).height(23.dp)
                                .background(RemoteAppearance.headerTextColor.copy(alpha = 0.5f))
                        )
                    }
                }
            }
            Spacer(Modifier.weight(1f))
            Row(
                modifier = Modifier.padding(end = 3.dp)
                    .background(
                        if (QiblyTheme.dark) Color.White.copy(alpha = 0.06f)
                        else Color(0xFF6F8ABF).copy(alpha = 0.06f),
                        RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 7.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.size(6.dp).background(
                    if (onlineUsers >= 0) Color(0xFF16BF6B) else RemoteAppearance.headerTextColor.copy(alpha = 0.35f),
                    CircleShape
                ))
                Spacer(Modifier.width(4.dp))
                Text(
                    if (onlineUsers < 0) "— online"
                    else if (onlineUsers >= 1000) String.format(java.util.Locale("pt", "BR"), "%.1f mil online", onlineUsers / 1000.0)
                    else "$onlineUsers online",
                    fontSize = 10.sp, maxLines = 1,
                    color = RemoteAppearance.headerTextColor.copy(alpha = 0.85f)
                )
            }
            IconButton(onClick = onSearch, modifier = Modifier.size(40.dp)) {
                Icon(Icons.Default.Search, contentDescription = "Buscar", tint = if (QiblyTheme.dark) Color.White else Color.Black, modifier = Modifier.size(26.dp))
            }
            Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                IconButton(onClick = onNotifications, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Default.Notifications, contentDescription = "Notificações", tint = if (QiblyTheme.dark) Color.White else Color.Black, modifier = Modifier.size(28.dp))
                }
                if (unreadNotifications > 0) {
                    Box(Modifier.align(Alignment.TopEnd).offset(x = (-1).dp, y = 1.dp).clip(CircleShape)
                        .background(Color(0xFFFF3045)).padding(horizontal = 5.dp, vertical = 2.dp), contentAlignment = Alignment.Center) {
                        Text(if (unreadNotifications > 99) "99+" else unreadNotifications.toString(), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }


        val feedFilters = listOf("Novidades", "Tendências", "Vídeos", "Lives", "Perto de mim")
        val feedFilterIcons = listOf("✨", "🔥", "▶️", "🔴", "📍")
        val feedFilterAccent = listOf(
            Color(0xFF1655E8), Color(0xFFFF663C), Color(0xFF111827),
            Color(0xFF1655E8), Color(0xFF1655E8)
        )
        androidx.compose.foundation.lazy.LazyRow(
            modifier = Modifier.fillMaxWidth().padding(top = 11.dp, bottom = 15.dp),
            contentPadding = PaddingValues(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            items(feedFilters.size) { index ->
                val filter = feedFilters[index]
                val active = selectedFeedFilter == filter
                Surface(
                    color = Color.Transparent,
                    shape = RoundedCornerShape(25.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(25.dp))
                        .background(
                            if (active) Brush.horizontalGradient(
                                listOf(Color(0xFF244BFF), Color(0xFF006BEB))
                            ) else Brush.horizontalGradient(
                                if (QiblyTheme.dark) listOf(Color(0xFF151515), Color(0xFF151515)) else listOf(Color(0xFFF3F7FF), Color(0xFFEAF2FF))
                            )
                        )
                        .clickable { selectedFeedFilter = filter }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val filterTint = if (active) Color.White else if (filter == "Tendências") Color(0xFFFF663C) else Color(0xFF1677F2)
                        when (filter) {
                            "Novidades", "Vídeos", "Fotos", "Tendências", "Perto de mim" -> {
                                val filterIcon = when (filter) {
                                    "Novidades" -> Icons.Default.AutoAwesome
                                    "Vídeos" -> Icons.Default.PlayCircle
                                    "Fotos" -> Icons.Default.Image
                                    "Perto de mim" -> Icons.Default.LocationOn
                                    else -> Icons.Default.LocalFireDepartment
                                }
                                Icon(
                                    imageVector = filterIcon,
                                    contentDescription = filter,
                                    tint = filterTint,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            "Lives" -> {
                                androidx.compose.foundation.Canvas(modifier = Modifier.size(18.dp)) {
                                    val stroke = androidx.compose.ui.graphics.drawscope.Stroke(
                                        width = 1.9.dp.toPx(),
                                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                                    )
                                    val c = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height / 2f)
                                    drawCircle(color = filterTint, radius = 2.dp.toPx(), center = c)
                                    val small = 6.dp.toPx()
                                    val large = 10.dp.toPx()
                                    for (radius in listOf(small, large)) {
                                        val corner = androidx.compose.ui.geometry.Offset(c.x - radius, c.y - radius)
                                        val arcSize = androidx.compose.ui.geometry.Size(radius * 2f, radius * 2f)
                                        drawArc(color = filterTint, startAngle = 120f, sweepAngle = 120f, useCenter = false, topLeft = corner, size = arcSize, style = stroke)
                                        drawArc(color = filterTint, startAngle = 300f, sweepAngle = 120f, useCenter = false, topLeft = corner, size = arcSize, style = stroke)
                                    }
                                }
                            }
                            else -> {
                                Text(feedFilterIcons[index],
                                    color = if (active) Color.White else feedFilterAccent[index],
                                    fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Text(filter,
                            color = if (active || QiblyTheme.dark) Color.White else Color(0xFF20202B),
                            fontSize = 13.sp,
                            fontWeight = if (active) FontWeight.Bold else FontWeight.SemiBold,
                            maxLines = 1)
                    }
                }
            }
        }
        if (!loading && displayedPosts.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Ainda não há publicações", fontWeight = FontWeight.SemiBold)
                    Text("Quando alguém publicar, as postagens públicas aparecerão aqui.", color = Muted, style = MaterialTheme.typography.bodySmall)
                }
            }
        } else {
            val feedListState = androidx.compose.foundation.lazy.rememberLazyListState()
            LaunchedEffect(refreshKey, displayedPosts.size) { feedListState.scrollToItem(0) }
            LaunchedEffect(scrollToTopRequest) { if (scrollToTopRequest > 0) feedListState.animateScrollToItem(0) }
            androidx.compose.material3.pulltorefresh.PullToRefreshBox(isRefreshing = refreshingFeed, onRefresh = { RemoteUiRefreshSignal.value++; if (!refreshingFeed) { refreshingFeed = true; refreshKey++ } }, modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                state = feedListState,
                flingBehavior = androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior(
                    lazyListState = feedListState,
                    snapPosition = androidx.compose.foundation.gestures.snapping.SnapPosition.Start
                ),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(top = 6.dp, bottom = 18.dp)
            ) {
                if (!loading && displayedPosts.isEmpty()) {
                    item {
                        Column(
                            Modifier.fillMaxWidth().padding(horizontal = 28.dp, vertical = 56.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(if (selectedFeedFilter == "Perto de mim") "Publicações locais indisponíveis" else "Ainda não há publicações", color = RemoteAppearance.feedTextColor, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text(if (selectedFeedFilter == "Perto de você") "A localização das publicações ainda não é fornecida pelo serviço. Não exibiremos resultados inventados." else "Quando as pessoas publicarem, elas aparecerão aqui.", color = RemoteAppearance.feedSecondaryTextColor, fontSize = 14.sp)
                            OutlinedButton(onClick = { refreshKey++ }) {
                                Text("Atualizar feed")
                            }
                        }
                    }
                }
                items(displayedPosts, key = { it.id }) { post ->
                    var postMenuExpanded by remember(post.id) { mutableStateOf(false) }
                    var likedByCurrentUser by remember(post.id, demoLikes, post.likedByMe) {
                        mutableStateOf(if (post.id.startsWith("demo-post-")) post.id in demoLikes else post.likedByMe)
                    }
                    Surface(
                        color = QiblyTheme.card,
                        shape = RoundedCornerShape(22.dp),
                        shadowElevation = 5.dp,
                        border = BorderStroke(1.dp, QiblyTheme.border),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(0.dp)) {
                            val followingPrefKey = "following_${post.username.lowercase()}"
                            var isFollowingPost by remember(post.id, post.isFollowing) {
                                mutableStateOf(if (post.id.startsWith("demo-post-")) prefs.getBoolean(followingPrefKey, false) else post.isFollowing)
                            }

                                        if (post.mediaUrl.isBlank() || (post.mediaType != "image" && post.mediaType != "video")) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().background(Color.Transparent).padding(horizontal = 14.dp, vertical = 7.dp)) {
                                    val avatarUrl = post.avatarUrl.ifBlank { "https://images.unsplash.com/photo-1488426862026-3ee34a7d66df?w=240&auto=format&fit=crop&q=80" }
                                    QiblyAccountAvatar(post.username, avatarUrl, 52.dp) { if (post.username.isNotBlank()) onOpenProfile(post.username) }
                                    Spacer(Modifier.width(10.dp))
                                    Column(Modifier.weight(1f).clickable { if (post.username.isNotBlank()) onOpenProfile(post.username) }) {
                                        Text(post.displayName.ifBlank { post.username }.ifBlank { "QINBLY" }, color = RemoteAppearance.feedTextColor, fontSize = 17.sp, fontWeight = if (nameFontStyle == "Elegante") FontWeight.Medium else FontWeight.Bold, fontFamily = nameFontFamily(nameFontStyle))
                                        Text("Há 2 min · Novo", color = RemoteAppearance.feedSecondaryTextColor, fontSize = 12.sp)
                                    }
                                    val currentUsername = prefs.getString("username", "").orEmpty()
                                    if (post.username.isNotBlank() && !post.username.equals(currentUsername, ignoreCase = true)) {
                                        OutlinedButton(
                                            onClick = {
                                                val token = prefs.getString("token", null)
                                                if (token.isNullOrBlank()) onNotice("Entre na sua conta para seguir pessoas.")
                                                else Thread {
                                                    try {
                                                        val profile = VyboraApi.getPublicProfile(post.username)
                                                        val user = profile.optJSONObject("user") ?: profile
                                                        val userId = user.optString("id")
                                                        if (userId.isBlank()) throw IllegalStateException("Não foi possível localizar este perfil.")
                                                        val following = VyboraApi.toggleFollow(token, userId)
                                                        android.os.Handler(android.os.Looper.getMainLooper()).post {
                                                            isFollowingPost = following
                                                            prefs.edit().putBoolean(followingPrefKey, following).apply()
                                                            onNotice(if (following) "Você está seguindo ${post.displayName.ifBlank { post.username }}." else "Você deixou de seguir ${post.displayName.ifBlank { post.username }}.")
                                                        }
                                                    } catch (e: Exception) {
                                                        android.os.Handler(android.os.Looper.getMainLooper()).post { onNotice(e.message ?: "Não foi possível seguir este perfil.") }
                                                    }
                                                }.start()
                                            },
                                            modifier = Modifier.width(76.dp).height(32.dp),
                                            contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp),
                                            shape = RoundedCornerShape(18.dp),
                                            border = BorderStroke(1.dp, RemoteAppearance.feedTextColor),
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                containerColor = if (isFollowingPost) RemoteAppearance.feedTextColor else Color.Transparent,
                                                contentColor = if (isFollowingPost) Color.White else RemoteAppearance.feedTextColor
                                            )
                                        ) {
                                            Text(if (isFollowingPost) "Seguindo" else "Seguir", fontSize = 14.sp, color = if (isFollowingPost) Color.White else RemoteAppearance.feedTextColor)
                                        }
                                        Spacer(Modifier.width(3.dp))
                                    }
                                    Box {
                                            IconButton(onClick = { postMenuExpanded = true }) {
                                                Icon(Icons.Default.MoreVert, contentDescription = "Opções da publicação")
                                            }
                                            DropdownMenu(expanded = postMenuExpanded, onDismissRequest = { postMenuExpanded = false }) {
                                                
                                                if (post.username.isNotBlank() && post.username.equals(currentUsername.removePrefix("@"), ignoreCase = true)) {
DropdownMenuItem(
                                                    text = { Text("Excluir publicação") },
                                                    leadingIcon = { Icon(Icons.Default.DeleteForever, contentDescription = null) },
                                                    onClick = {
                                                        postMenuExpanded = false
                                                        val token = prefs.getString("token", null)
                                                        if (token.isNullOrBlank()) onNotice("Entre na sua conta para excluir esta publicação.")
                                                        else Thread {
                                                            try {
                                                                VyboraApi.deletePost(token, post.id)
                                                                val refreshed = VyboraApi.getPosts()
                                                                android.os.Handler(android.os.Looper.getMainLooper()).post {
                                                                    posts = refreshed
                                                                    onNotice("Publicação excluída.")
                                                                }
                                                            } catch (e: Exception) {
                                                                android.os.Handler(android.os.Looper.getMainLooper()).post {
                                                                    onNotice(e.message ?: "Não foi possível excluir a publicação.")
                                                                }
                                                            }
                                                        }.start()
                                                    }
                                                )
                                                } else {
                                                    DropdownMenuItem(text = { Text("Ocultar publicação") }, onClick = {
                                                        postMenuExpanded = false
                                                        posts = posts.filterNot { it.id == post.id }
                                                        onNotice("Publicação ocultada deste feed.")
                                                    })
                                                    DropdownMenuItem(text = { Text("Copiar link") }, onClick = {
                                                        postMenuExpanded = false
                                                        val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                                        clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Publicação QINBLY", "https://qinbly.com/post/" + post.id))
                                                        onNotice("Link copiado.")
                                                    })
                                                }

                                            }
                                        }
                                    }
                                        
                            }
                            if (post.mediaUrl.isNotBlank() && (post.mediaType == "image" || post.mediaType == "video")) {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 7.dp),
                                    verticalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                                        QiblyAccountAvatar(post.username, post.avatarUrl, 52.dp) { if (post.username.isNotBlank()) onOpenProfile(post.username) }
                                        Column(Modifier.weight(1f)) {
                                            Text(
                                                post.displayName.ifBlank { post.username }.ifBlank { "QINBLY" },
                                                color = RemoteAppearance.feedTextColor, fontSize = 15.sp, fontWeight = FontWeight.Bold,
                                                modifier = Modifier.clickable { if (post.username.isNotBlank()) onOpenProfile(post.username) }
                                            )
                                            Text("Há 2 min", color = RemoteAppearance.feedSecondaryTextColor, fontSize = 12.sp)
                                        }
                                    val currentUsername = prefs.getString("username", "").orEmpty()
                                    if (post.username.isNotBlank() && !post.username.equals(currentUsername, ignoreCase = true)) {
                                        OutlinedButton(
                                            onClick = {
                                                val token = prefs.getString("token", null)
                                                if (token.isNullOrBlank()) onNotice("Entre na sua conta para seguir pessoas.")
                                                else Thread {
                                                    try {
                                                        val profile = VyboraApi.getPublicProfile(post.username)
                                                        val user = profile.optJSONObject("user") ?: profile
                                                        val userId = user.optString("id")
                                                        if (userId.isBlank()) throw IllegalStateException("Não foi possível localizar este perfil.")
                                                        val following = VyboraApi.toggleFollow(token, userId)
                                                        android.os.Handler(android.os.Looper.getMainLooper()).post {
                                                            isFollowingPost = following
                                                            prefs.edit().putBoolean(followingPrefKey, following).apply()
                                                            onNotice(if (following) "Você está seguindo ${post.displayName.ifBlank { post.username }}." else "Você deixou de seguir ${post.displayName.ifBlank { post.username }}.")
                                                        }
                                                    } catch (e: Exception) {
                                                        android.os.Handler(android.os.Looper.getMainLooper()).post { onNotice(e.message ?: "Não foi possível seguir este perfil.") }
                                                    }
                                                }.start()
                                            },
                                            modifier = Modifier.width(76.dp).height(32.dp),
                                            contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp),
                                            shape = RoundedCornerShape(18.dp),
                                            border = BorderStroke(1.dp, RemoteAppearance.feedTextColor),
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                containerColor = if (isFollowingPost) RemoteAppearance.feedTextColor else Color.Transparent,
                                                contentColor = if (isFollowingPost) Color.White else RemoteAppearance.feedTextColor
                                            )
                                        ) {
                                            Text(if (isFollowingPost) "Seguindo" else "Seguir", fontSize = 14.sp, color = if (isFollowingPost) Color.White else RemoteAppearance.feedTextColor)
                                        }
                                        Spacer(Modifier.width(3.dp))
                                    }
                                        Box {
                                            IconButton(onClick = { postMenuExpanded = true }) {
                                                Icon(Icons.Default.MoreVert, contentDescription = "Opções da publicação", tint = RemoteAppearance.feedTextColor)
                                            }
                                            DropdownMenu(expanded = postMenuExpanded, onDismissRequest = { postMenuExpanded = false }) {
                                                
                                                if (post.username.isNotBlank() && post.username.equals(currentUsername.removePrefix("@"), ignoreCase = true)) {
DropdownMenuItem(
                                                    text = { Text("Excluir publicação") },
                                                    onClick = {
                                                        postMenuExpanded = false
                                                        val token = prefs.getString("token", null)
                                                        if (token.isNullOrBlank()) onNotice("Entre na sua conta para excluir publicações.")
                                                        else Thread {
                                                            try {
                                                                VyboraApi.deletePost(token, post.id)
                                                                val refreshed = VyboraApi.getPosts()
                                                                android.os.Handler(android.os.Looper.getMainLooper()).post {
                                                                    posts = refreshed
                                                                    onNotice("Publicação excluída.")
                                                                }
                                                            } catch (e: Exception) {
                                                                android.os.Handler(android.os.Looper.getMainLooper()).post {
                                                                    onNotice(e.message ?: "Não foi possível excluir a publicação.")
                                                                }
                                                            }
                                                        }.start()
                                                    }
                                                )
                                                } else {
                                                    DropdownMenuItem(text = { Text("Ocultar publicação") }, onClick = {
                                                        postMenuExpanded = false
                                                        posts = posts.filterNot { it.id == post.id }
                                                        onNotice("Publicação ocultada deste feed.")
                                                    })
                                                    DropdownMenuItem(text = { Text("Copiar link") }, onClick = {
                                                        postMenuExpanded = false
                                                        val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                                        clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Publicação QINBLY", "https://qinbly.com/post/" + post.id))
                                                        onNotice("Link copiado.")
                                                    })
                                                }

                                            }
                                        }
                                    }

                                }
                            }
                            if (post.mediaUrl.isNotBlank() && post.mediaType == "image") {
                                val demoGallery = remember(post.id) {
                                    when (post.id) {
                                        "demo-post-06" -> listOf(
                                            "https://images.unsplash.com/photo-1502680390469-be75c86b636f?w=1000&auto=format&fit=crop&q=85",
                                            "https://images.unsplash.com/photo-1519046904884-53103b34b206?w=1000&auto=format&fit=crop&q=85",
                                            "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=1000&auto=format&fit=crop&q=85",
                                            "https://images.unsplash.com/photo-1473116763249-2faaef81ccda?w=1000&auto=format&fit=crop&q=85"
                                        )
                                        "demo-post-07" -> listOf(
                                            "https://images.unsplash.com/photo-1529139574466-a303027c1d8b?w=1000&auto=format&fit=crop&q=85",
                                            "https://images.unsplash.com/photo-1515886657613-9f3515f0d78f?w=1000&auto=format&fit=crop&q=85",
                                            "https://images.unsplash.com/photo-1483985988355-763728e1935b?w=1000&auto=format&fit=crop&q=85",
                                            "https://images.unsplash.com/photo-1485230895905-ec40ba36b9bc?w=1000&auto=format&fit=crop&q=85"
                                        )
                                        "demo-post-08" -> listOf(
                                            "https://images.unsplash.com/photo-1519046904884-53103b34b206?w=1000&auto=format&fit=crop&q=85",
                                            "https://images.unsplash.com/photo-1500375592092-40eb2168fd21?w=1000&auto=format&fit=crop&q=85",
                                            "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=1000&auto=format&fit=crop&q=85",
                                            "https://images.unsplash.com/photo-1473116763249-2faaef81ccda?w=1000&auto=format&fit=crop&q=85"
                                        )
                                        else -> listOf(post.mediaUrl)
                                    }
                                }
                                var photoIndex by remember(post.id) { mutableIntStateOf(0) }
                                val shownPhoto = demoGallery[photoIndex.coerceIn(0, demoGallery.lastIndex)]
                                var dragDistance by remember(post.id) { mutableFloatStateOf(0f) }
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)
                                        .aspectRatio(0.8f)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color.Black)
                                ) {
                                    Surface(
                                        modifier = Modifier.align(Alignment.TopStart).padding(10.dp),
                                        color = Color(0xFF6620C9).copy(alpha = 0.92f),
                                        shape = RoundedCornerShape(18.dp)
                                    ) {
                                        Text("$selectedFeedTab + $selectedFeedFilter", color = Color.White,
                                            fontSize = 12.sp, fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp))
                                    }
                                    AsyncImage(
                                        model = shownPhoto, contentDescription = "Foto ${photoIndex + 1} de ${demoGallery.size}",
                                        modifier = Modifier.fillMaxSize()
                                            .pointerInput(post.id, photoIndex, demoGallery.size) {
                                                detectHorizontalDragGestures(
                                                    onHorizontalDrag = { change, amount ->
                                                        dragDistance += amount
                                                        change.consume()
                                                    },
                                                    onDragEnd = {
                                                        if (dragDistance < -40f) photoIndex = (photoIndex + 1).coerceAtMost(demoGallery.lastIndex)
                                                        else if (dragDistance > 40f) photoIndex = (photoIndex - 1).coerceAtLeast(0)
                                                        dragDistance = 0f
                                                    },
                                                    onDragCancel = { dragDistance = 0f }
                                                )
                                            }
                                            .clickable { expandedMedia = shownPhoto },
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )

                                    
                                    
                                    if (demoGallery.size > 1) {
                                        Box(Modifier.align(Alignment.TopEnd).padding(10.dp).clip(CircleShape)
                                            .background(Color(0x99030711)).padding(horizontal = 10.dp, vertical = 5.dp)) {
                                            Text("${photoIndex + 1}/${demoGallery.size}", color = RemoteAppearance.feedTextColor, fontSize = 14.sp)
                                        }
                                        Row(Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp),
                                            horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                                            demoGallery.indices.forEach { idx ->
                                                Box(Modifier.size(if (idx == photoIndex) 8.dp else 7.dp).clip(CircleShape)
                                                    .background(if (idx == photoIndex) Pink else Color(0xFF777982)))
                                            }
                                        }
                                    }

                                }

                            } else if (post.mediaUrl.isNotBlank() && post.mediaType == "video") {
                                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp).aspectRatio(0.8f)
                                    .clip(RoundedCornerShape(16.dp)).background(Color.Black)) {
                                    Surface(
                                        modifier = Modifier.align(Alignment.TopStart).padding(10.dp),
                                        color = Color(0xFF6620C9).copy(alpha = 0.92f),
                                        shape = RoundedCornerShape(18.dp)
                                    ) {
                                        Text("$selectedFeedTab + $selectedFeedFilter", color = Color.White,
                                            fontSize = 12.sp, fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp))
                                    }
                                    Box(Modifier.fillMaxSize().clickable { fullscreenVideoId = post.id }) {
                                        InlineVideoPlayer(url = post.mediaUrl, modifier = Modifier.fillMaxSize(), autoplay = false)
                                    }
                                    

                                    
                                }
                            }
                            if (post.mediaUrl.isNotBlank() && (post.mediaType == "image" || post.mediaType == "video") && post.caption.isNotBlank()) {
                                Text(
                                    post.caption, color = RemoteAppearance.feedTextColor,
                                    fontSize = 15.sp, lineHeight = 20.sp,
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp)
                                )
                            }
                            if (post.mediaType == "poll" && post.pollOptions.isNotEmpty()) {
                                Column(
                                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalArrangement = Arrangement.spacedBy(9.dp)
                                ) {
                                    Text(post.caption, color = RemoteAppearance.feedTextColor, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                    val totalVotes = post.pollVotes.sum()
                                    val hasVoted = post.myPollVote >= 0
                                    post.pollOptions.forEachIndexed { optionIndex, option ->
                                        val count = post.pollVotes.getOrElse(optionIndex) { 0 }
                                        val percent = if (totalVotes > 0) count * 100 / totalVotes else 0
                                        val selected = post.myPollVote == optionIndex
                                        val pollShape = RoundedCornerShape(14.dp)
                                        Row(
                                            Modifier.fillMaxWidth().height(52.dp)
                                                .background(if (selected) Color(0xFFF0F6FF) else Color.White, pollShape)
                                                .border(
                                                    width = if (selected) 1.5.dp else 1.dp,
                                                    color = if (selected) Color(0xFF1677F2) else Color(0xFFE0E7F0),
                                                    shape = pollShape
                                                )
                                                .clickable(enabled = !hasVoted) {
                                                    val token = prefs.getString("token", null)
                                                    if (token.isNullOrBlank()) onNotice("Entre na sua conta para votar.")
                                                    else Thread {
                                                        try {
                                                            VyboraApi.votePoll(token, post.id, optionIndex)
                                                            android.os.Handler(android.os.Looper.getMainLooper()).post { refreshKey++ }
                                                        } catch (e: Exception) {
                                                            android.os.Handler(android.os.Looper.getMainLooper()).post {
                                                                onNotice(e.message ?: "Não foi possível votar.")
                                                            }
                                                        }
                                                    }.start()
                                                }
                                                .padding(horizontal = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Box(
                                                Modifier.size(34.dp)
                                                    .background(if (selected) Color(0xFFE8F2FF) else Color(0xFFF0F3F8), CircleShape)
                                                    .border(1.dp, if (selected) Color(0xFFC5DEFF) else Color.Transparent, CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    option.replace(Regex("^\\s*\\d+\\s*[—–-]\\s*"), ""),
                                                    color = Color(0xFF14233B), fontSize = 14.sp,
                                                    fontWeight = FontWeight.SemiBold, maxLines = 1
                                                )
                                            }
                                            Box(
                                                Modifier.weight(1f).height(20.dp)
                                                    .background(Color(0xFFE0E9F6), RoundedCornerShape(12.dp))
                                            ) {
                                                if (hasVoted && percent > 0) {
                                                    Box(
                                                        Modifier.fillMaxWidth(percent / 100f).fillMaxHeight()
                                                            .background(Color(0xFF1677F2), RoundedCornerShape(12.dp))
                                                    )
                                                }
                                            }
                                            if (hasVoted) {
                                                Text("$percent%", color = Color(0xFF14233B), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                                if (selected) {
                                                    Box(
                                                        Modifier.size(28.dp).background(Color(0xFF1677F2), CircleShape),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text("✓", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    Text("$totalVotes votos", color = RemoteAppearance.feedTextColor.copy(alpha = 0.65f), fontSize = 12.sp)
                                }
                            } else if (post.mediaType != "image" && post.mediaType != "video" && post.caption.isNotBlank()) {
                                Text(post.caption, color = RemoteAppearance.feedTextColor, fontSize = 15.sp,
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 7.dp),
                                    maxLines = 3, lineHeight = 20.sp)
                            }
                            if (post.mediaType != "poll") { // Enquetes exibem apenas votação e total de votos
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(RemoteAppearance.feedActionBackgroundColor)
                                        .heightIn(min = 52.dp)
                                        .padding(horizontal = 16.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.clickable {
                                            if (post.id.startsWith("demo-post-")) {
                                                val alreadyLiked = post.id in demoLikes
                                                demoLikes = if (alreadyLiked) demoLikes - post.id else demoLikes + post.id
                                                prefs.edit().putStringSet("demo_liked_post_ids", demoLikes.toSet()).apply()
                                                likedByCurrentUser = !alreadyLiked
                                            } else {
                                                val token = prefs.getString("token", null)
                                                if (token.isNullOrBlank()) onNotice("Entre na sua conta para curtir publicações.")
                                                else Thread {
                                                    try {
                                                        val nowLiked = VyboraApi.toggleLike(token, post.id)
                                                        val refreshed = VyboraApi.getPosts()
                                                        android.os.Handler(android.os.Looper.getMainLooper()).post {
                                                            likedByCurrentUser = nowLiked
                                                            posts = refreshed
                                                        }
                                                    } catch (e: Exception) {
                                                        android.os.Handler(android.os.Looper.getMainLooper()).post { onNotice(e.message ?: "Não foi possível curtir.") }
                                                    }
                                                }.start()
                                            }
                                        },
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            if (likedByCurrentUser) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                            contentDescription = if (likedByCurrentUser) "Descurtir" else "Curtir",
                                            tint = if (likedByCurrentUser) Color(0xFFFF3045) else RemoteAppearance.actionIconColor,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Text(
                                            if (post.likes >= 1000) String.format(java.util.Locale("pt", "BR"), "%.1f mil", post.likes / 1000.0) else post.likes.toString(),
                                            color = RemoteAppearance.feedSecondaryTextColor,
                                            fontSize = 12.sp
                                        )
                                    }
                                    Box(Modifier.width(1.dp).height(18.dp).background(Color(0xFFE1E4E9)))
                                    Row(
                                        modifier = Modifier.clickable {
                                            commentPost = post
                                            commentDraft = ""
                                            commentsLoading = true
                                            Thread {
                                                try {
                                                    val loadedComments = VyboraApi.getComments(post.id)
                                                    android.os.Handler(android.os.Looper.getMainLooper()).post {
                                                        comments = loadedComments
                                                        commentsLoading = false
                                                    }
                                                } catch (e: Exception) {
                                                    android.os.Handler(android.os.Looper.getMainLooper()).post {
                                                        commentsLoading = false
                                                        onNotice(e.message ?: "Não foi possível carregar comentários.")
                                                    }
                                                }
                                            }.start()
                                        },
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(Icons.Default.ChatBubbleOutline, contentDescription = "Comentários", tint = RemoteAppearance.actionIconColor, modifier = Modifier.size(24.dp))
                                        Text(
                                            if (post.comments >= 1000) String.format(java.util.Locale("pt", "BR"), "%.1f mil", post.comments / 1000.0) else post.comments.toString(),
                                            color = RemoteAppearance.feedSecondaryTextColor,
                                            fontSize = 12.sp
                                        )
                                    }
                                    Box(Modifier.width(1.dp).height(18.dp).background(Color(0xFFE1E4E9)))
                                    Row(
                                        modifier = Modifier.clickable {
                                            val alreadyReposted = post.id in repostedPostIds
                                            repostedPostIds = if (alreadyReposted) repostedPostIds - post.id else repostedPostIds + post.id
                                            prefs.edit().putStringSet("reposted_post_ids_local", repostedPostIds).apply()
                                            onNotice(if (alreadyReposted) "Marcação de repost removida deste aparelho." else "Repost marcado neste aparelho. Publicação no servidor ainda não disponível.")
                                        },
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        RepostVerifiedIcon(post.id in repostedPostIds, tint = RemoteAppearance.actionIconColor)
                                        Text("0", color = RemoteAppearance.feedSecondaryTextColor, fontSize = 12.sp)
                                    }
                                    Box(Modifier.width(1.dp).height(18.dp).background(Color(0xFFE1E4E9)))
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    IconButton(
                                                                                onClick = {
                                            val wasSaved = post.id in savedPostIds
                                            savedPostIds = if (wasSaved) savedPostIds - post.id else savedPostIds + post.id
                                            prefs.edit().putStringSet("saved_post_ids", savedPostIds).apply()
                                            onNotice(if (wasSaved) "Publicação removida dos salvos." else "Publicação salva.")
                                        },
                                        modifier = Modifier.size(28.dp)

                                    ) {
                                        Icon(
                                            if (post.id in savedPostIds) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                            contentDescription = if (post.id in savedPostIds) "Remover dos salvos" else "Salvar publicação",
                                            tint = if (post.id in savedPostIds) Color(0xFFFFD54F) else RemoteAppearance.actionIconColor,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                        Text("0", color = RemoteAppearance.feedSecondaryTextColor, fontSize = 12.sp)
                                    }

                                        Box(Modifier.width(1.dp).height(18.dp).background(Color(0xFFE1E4E9)))
                                        Row(
                                            modifier = Modifier.clickable {
                                            sharePost = post

                                        },
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        RoundedPaperPlaneIcon(tint = RemoteAppearance.actionIconColor)
                                        Text("0", color = RemoteAppearance.feedSecondaryTextColor, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            }
        }
    sharePost?.let { selectedPost ->
        QiblyShareDialog(
            post = selectedPost,
            prefs = prefs,
            onDismiss = { sharePost = null },
            onNotice = onNotice
        )
    }
    val fullscreenVideos = displayedPosts.filter { it.mediaType == "video" && it.mediaUrl.isNotBlank() }
    val initialFullscreenIndex = fullscreenVideos.indexOfFirst { it.id == fullscreenVideoId }
    if (fullscreenVideoId != null && initialFullscreenIndex >= 0) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { fullscreenVideoId = null },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            val pagerState = androidx.compose.foundation.pager.rememberPagerState(
                initialPage = initialFullscreenIndex,
                pageCount = { fullscreenVideos.size }
            )
            Box(Modifier.fillMaxSize().background(Color.Black)) {
                androidx.compose.foundation.pager.VerticalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    beyondViewportPageCount = 0
                ) { page ->
                    val video = fullscreenVideos[page]
                    Box(Modifier.fillMaxSize().background(Color.Black)) {
                        if (pagerState.currentPage == page) {
                            VyboraFullscreenVideo(video.mediaUrl, Modifier.fillMaxSize())
                        }
                        Column(Modifier.align(Alignment.BottomEnd).padding(end = 14.dp, bottom = 122.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            listOf(Icons.Default.Person, Icons.Default.PersonAdd, Icons.Default.FavoriteBorder, Icons.Default.ChatBubbleOutline, Icons.Default.Share, Icons.Default.Archive, Icons.Default.MoreHoriz).forEach {
                                Icon(it, contentDescription = null, tint = Color.Black, modifier = Modifier.size(34.dp).padding(5.dp))
                            }
                        }
                    }
                }
                IconButton(
                    onClick = { fullscreenVideoId = null },
                    modifier = Modifier.align(Alignment.TopStart).padding(12.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Fechar tela inteira", tint = Color.Black)
                }
                Text(
                    "${pagerState.currentPage + 1}/${fullscreenVideos.size}",
                    modifier = Modifier.align(Alignment.TopEnd).padding(24.dp),
                    color = Color.Black, fontSize = 13.sp
                )
            }
        }
    }
    if (expandedMedia.isNotBlank()) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { expandedMedia = "" }, properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)) {
            Box(Modifier.fillMaxSize().background(Color.Black).clickable { expandedMedia = "" }, contentAlignment = Alignment.Center) {
                AsyncImage(model = expandedMedia, contentDescription = "Imagem ampliada", modifier = Modifier.fillMaxWidth().fillMaxHeight(0.9f), contentScale = androidx.compose.ui.layout.ContentScale.Fit)
                IconButton(onClick = { expandedMedia = "" }, modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)) { Icon(Icons.Default.Close, contentDescription = "Fechar imagem", tint = Color.Black) }
            }
        }
    }
    if (selectedStory != null) {
        val story = selectedStory!!
        androidx.compose.ui.window.Dialog(onDismissRequest = { selectedStory = null }, properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)) {
            Box(Modifier.fillMaxSize().background(Color.Black).clickable { selectedStory = null }) {
                AsyncImage(model = story.second.replace("w=240", "w=1200"), contentDescription = "Story de @" + story.first, modifier = Modifier.fillMaxSize(), contentScale = androidx.compose.ui.layout.ContentScale.Fit)
                Row(Modifier.align(Alignment.TopStart).fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(model = story.second, contentDescription = null, modifier = Modifier.size(42.dp).clip(CircleShape), contentScale = androidx.compose.ui.layout.ContentScale.Crop)
                    Text("@" + story.first, modifier = Modifier.padding(start = 10.dp).weight(1f), color = Color.White, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { selectedStory = null }) { Icon(Icons.Default.Close, contentDescription = "Fechar story", tint = Color.White) }
                }
            }
        }
    }
    val commentEmojiContext = androidx.compose.ui.platform.LocalContext.current
    val commentEmojiPrefs = remember(commentEmojiContext) {
        commentEmojiContext.getSharedPreferences("vybora_comment_emoji_history", android.content.Context.MODE_PRIVATE)
    }
    var recentCommentEmojis by remember {
        mutableStateOf(commentEmojiPrefs.getString("recent", "")!!.split("|").filter { it.isNotBlank() })
    }
    var showCommentEmojis by remember { mutableStateOf(false) }
    var selectedCommentEmojiCategory by remember { mutableStateOf("Emoções") }
    BackHandler(enabled = showCommentEmojis) {
        showCommentEmojis = false
    }
    val commentDensity = androidx.compose.ui.platform.LocalDensity.current
    val keyboardInset = with(commentDensity) { WindowInsets.ime.getBottom(this).toDp() }
    // Android can report a small IME inset even with the keyboard dismissed.
    // Use a real keyboard-height threshold so the sheet doesn't stay collapsed.
    val keyboardVisible = keyboardInset > 120.dp
    val activePost = commentPost
    if (activePost != null) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = {
                if (showCommentEmojis) showCommentEmojis = false else commentPost = null
            },
            properties = androidx.compose.ui.window.DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    // Apply the IME inset to this Dialog window before measuring the sheet.
                    .imePadding()
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.58f))
                    .clickable { if (showCommentEmojis) showCommentEmojis = false else commentPost = null },
                contentAlignment = Alignment.BottomCenter
            ) {
                // Read IME insets inside the Dialog composition, not from the Activity window.
                // Keep Android's gesture/navigation controls in place, but remove the extra dark navigation-bar scrim behind them while the comments dialog is open.
                val dialogWindow = (androidx.compose.ui.platform.LocalView.current.parent as? androidx.compose.ui.window.DialogWindowProvider)?.window
                SideEffect {
                    dialogWindow?.let { window ->
                        window.navigationBarColor = android.graphics.Color.TRANSPARENT
                        window.statusBarColor = android.graphics.Color.TRANSPARENT
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                            window.isNavigationBarContrastEnforced = false
                        }
                        // Work around the Compose 1.7.x Dialog system-bar inset bug.
                        // Keep the sheet's existing dimensions and composer position unchanged.
                        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)
                        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR)
                        window.setLayout(
                            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                            android.view.ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                            window.attributes = window.attributes.apply {
                                fitInsetsTypes = 0
                                fitInsetsSides = 0
                            }
                        }
                    }
                }

                val dialogDensity = androidx.compose.ui.platform.LocalDensity.current
                val dialogKeyboardInset = with(dialogDensity) { WindowInsets.ime.getBottom(this).toDp() }
                val dialogNavigationInset = with(dialogDensity) { WindowInsets.navigationBars.getBottom(this).toDp() }
                val dialogKeyboardVisible = dialogKeyboardInset > 120.dp

                // The sheet height is based on the usable height of this Dialog after IME padding.
                // Keep the sheet inside the screen and lift its lower edge above the system navigation area.
                // The previous 2x height made the sheet extend beyond the top of the display.
                val panelHeight = maxHeight * if (dialogKeyboardVisible) 0.90f else 0.90f
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(panelHeight)
                        .offset(y = (-5).dp)
                        .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                        .background(RemoteAppearance.commentBackgroundColor)
                        .clickable(interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }, indication = null) { }
                ) {
                    Box(Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 8.dp), contentAlignment = Alignment.Center) {
                        Box(Modifier.width(48.dp).height(5.dp).clip(CircleShape).background(Color(0xFF777887)))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(start = 22.dp, end = 18.dp, top = 8.dp, bottom = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Comentários",
                            color = RemoteAppearance.commentTextColor,
                            fontSize = 25.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable(enabled = showCommentEmojis) { showCommentEmojis = false }
                        )
                        Text("  ${comments.size}", color = RemoteAppearance.commentSecondaryTextColor, fontSize = 14.sp, modifier = Modifier.weight(1f))
                        var sortComments by remember { mutableStateOf("Principais") }
                        TextButton(onClick = { sortComments = if (sortComments == "Principais") "Recentes" else "Principais" }) {
                            Text(sortComments, color = RemoteAppearance.commentTextColor, fontSize = 14.sp)
                            Icon(Icons.Default.ChevronRight, contentDescription = "Ordenar comentários", tint = RemoteAppearance.commentTextColor, modifier = Modifier.padding(start = 3.dp).size(18.dp))
                        }
                    }
                    HorizontalDivider(color = RemoteAppearance.commentDividerColor)


                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .clickable(
                                enabled = showCommentEmojis,
                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                indication = null
                            ) { showCommentEmojis = false },
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        items(comments, key = { it.id }) { comment ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier.size(48.dp).clip(CircleShape)
                                        .background(RemoteAppearance.commentInputBackgroundColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        comment.displayName.ifBlank { comment.username }.ifBlank { "U" }.take(1).uppercase(),
                                        color = RemoteAppearance.commentTextColor,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                }
                                Column(modifier = Modifier.weight(1f).padding(start = 14.dp, end = 8.dp)) {
                                    Text(
                                        comment.displayName.ifBlank { comment.username }.ifBlank { "Usuário" },
                                        color = RemoteAppearance.commentTextColor,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 16.sp
                                    )
                                    Text("agora", color = RemoteAppearance.commentSecondaryTextColor, fontSize = 14.sp, modifier = Modifier.padding(top = 2.dp))
                                    Text(comment.body, color = RemoteAppearance.commentTextColor, fontSize = 16.sp, modifier = Modifier.padding(top = 8.dp))
                                    Text(
                                        "Responder",
                                        color = RemoteAppearance.commentTextColor,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(top = 10.dp).clickable { commentDraft = "@${comment.username} " }
                                    )
                                }
                                Icon(Icons.Default.FavoriteBorder, contentDescription = "Curtir comentário", tint = RemoteAppearance.commentTextColor, modifier = Modifier.padding(top = 6.dp).size(22.dp))
                            }
                            HorizontalDivider(color = RemoteAppearance.commentDividerColor)
                        }
                        if (comments.isEmpty() && !commentsLoading) {
                            item {
                                Box(Modifier.fillMaxWidth().padding(vertical = 40.dp), contentAlignment = Alignment.Center) {
                                    Text("Seja a primeira pessoa a comentar.", color = RemoteAppearance.commentSecondaryTextColor)
                                }
                            }
                        }
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            // The Dialog may report a zero navigation inset on some Android versions.
                            // Use a small, explicit lift for the composer only; sheet/header stay fixed.
                            .padding(bottom = if (dialogKeyboardVisible) 0.dp else 48.dp)
                    ) {
                        if (showCommentEmojis) {
                            val commentEmojiGroups = listOf(
                                "Recentes" to recentCommentEmojis,
                                "Emoções" to listOf("😀","😃","😄","😁","😆","😅","🤣","😂","🙂","🙃","😉","😊","😇","🥰","😍","🤩","😘","😗","☺️","😚","😙","🥲","😋","😛","😜","🤪","😝","🤑","🤗","🤭","🫢","🫣","🤫","🤔","🫡","🤐","🤨","😐","😑","😶","🫥","😏","😒","🙄","😬","😮‍💨","🤥","😌","😔","😪","🤤","😴","😷","🤒","🤕","🤢","🤮","🤧","🥵","🥶","🥴","😵","🤯","🤠","🥳","🥸","😎","🤓","🧐","😕","🫤","😟","🙁","☹️","😮","😯","😲","😳","🥺","🥹","😦","😧","😨","😰","😥","😢","😭","😱","😖","😣","😞","😓","😩","😫","🥱","😤","😡","😠","🤬","😈","👿","💀","☠️","💩","🤡","👹","👺","👻","👽","👾","🤖"),
                                "Amor" to listOf("❤️","🩷","🧡","💛","💚","💙","🩵","💜","🤎","🖤","🩶","🤍","💔","❤️‍🔥","❤️‍🩹","💕","💞","💓","💗","💖","💘","💝","💟","❣️","💌","💋","💍","💐","🌹","🌷","🌸","💮","🪷","🌺","🌻","🌼","🌈","🦋","💎","💫","💑","👩‍❤️‍👨","👨‍❤️‍👨","👩‍❤️‍👩","💏","💐","🥀","🌹","🌺","🌻","🌼","🌷","🪻","🪷","🌱","🌿","🍀","☘️","🪴","🌵","🌴","🌳","🌲","🪺","🪷","✨","💍","🫀","💟","💝","💗","💖","💘","💞","💌","💒"),
                                "Pessoas e gestos" to listOf("👋","🤚","🖐️","✋","🖖","👌","🤌","🤏","✌️","🤞","🫰","🤟","🤘","🤙","👈","👉","👆","👇","☝️","👍","👎","✊","👊","🤛","🤜","👏","🙌","👐","🤲","🤝","🙏","✍️","💅","🤳","💪","🦾","🫶","🫂","👶","🧒","👦","👧","🧑","👨","👩","🧔","👮","🕺","💃","🧑‍🎤","🧑‍🚀","🧑‍🎨","👨‍🍳","👩‍🍳","🧑‍🚒","🧑‍⚕️","🧑‍🏫","🧑‍💻","🧑‍🔬","🧑‍🔧","🧑‍🎓","🧑‍🌾","🧑‍⚖️","🧑‍✈️","🧑‍🚀","👰","🤵","🤱","🧑‍🍼","🧏","🦻","🦵","🦶","👂","👃","🧠","🫁","🦷","🦴","👀","👁️","👅","👄","🫦","💋","🗣️","👤","👥","🫂"),
                                "Animais" to listOf("🐶","🐱","🐭","🐹","🐰","🦊","🐻","🐼","🐻‍❄️","🐨","🐯","🦁","🐮","🐷","🐸","🐵","🙈","🙉","🙊","🐔","🐧","🐦","🐤","🦆","🦉","🦄","🐝","🦋","🐌","🐞","🐢","🐍","🦎","🐙","🦀","🐬","🐳","🦈","🐊","🦖","🐘","🦒","🦓","🦩","🦚","🦜","🦥","🦦","🦔","🐾","🐺","🐗","🐴","🫎","🫏","🦌","🦬","🦛","🦏","🦍","🦧","🐪","🐫","🦘","🦙","🦒","🦣","🦫","🦨","🦡","🦃","🦤","🦢","🦜","🦅","🪿","🦩","🦚","🦉","🪶","🪱","🪲","🪳","🦟","🪰","🪸","🪼","🦭","🦐","🦑","🦞","🦪","🐠","🐡","🪼","🐚","🪹","🐌","🐛","🪰","🦗","🕷️","🕸️","🦂","🐜","🐕","🐈","🐩","🐈‍⬛","🐇","🐿️","🦔","🦝","🦨"),
                                "Comida" to listOf("🍏","🍎","🍐","🍊","🍋","🍌","🍉","🍇","🍓","🫐","🍈","🍒","🍑","🥭","🍍","🥥","🥝","🍅","🍆","🥑","🥦","🥕","🌽","🌶️","🥒","🥬","🥔","🍠","🥐","🥨","🍞","🧀","🥚","🍳","🥞","🧇","🥓","🍔","🍟","🍕","🌭","🥪","🌮","🌯","🍜","🍣","🍤","🍰","🧁","🍩","🍪","🍫","🍿","☕","🧋","🥤","🍍","🥑","🫒","🧄","🧅","🥜","🌰","🫘","🫚","🫛","🥗","🥙","🫓","🥯","🥖","🥨","🥩","🍗","🍖","🦴","🌭","🥫","🍝","🍛","🍲","🫕","🥘","🫔","🫙","🥟","🦪","🍱","🍘","🍙","🍚","🍥","🥠","🥮","🍢","🍡","🍧","🍨","🍦","🥧","🍮","🍭","🍬","🍯","🍼","🥛","🫖","🍵","🧃","🧉","🍶","🍺","🍻","🥂","🍷","🥃","🍸","🍹","🧊"),
                                "Atividades" to listOf("⚽","🏀","🏈","⚾","🎾","🏐","🏉","🎱","🏓","🏸","🥊","🥋","⛸️","🎿","🛹","🛼","🎮","🕹️","🎲","🧩","🎯","🎳","🎤","🎧","🎼","🎹","🥁","🎷","🎸","🎻","🎬","🎨","🧵","🪡","📸","📹","🎥","📱","💻","⌚","🎁","🏆","🥇","🏅","🎖️","🎉","🎊","🎈","🎀","🪄","🏃","🚶","🧘","🏋️","🤸","🤼","🤽","🤾","🏌️","🏇","🧗","🏄","🚣","🧑‍🦽","🧑‍🦼","🧑‍🦯","🚴","🚵","🤹","🎪","🎭","🩰","🎟️","🎫","🎹","🪇","🪈","🪗","🪕","🪘","🪉","🎙️","🎚️","🎛️","📻","📺","📼","📀","💿","🧸","🪅","🪆","🪁","🪀","🛝","🎠","🎡","🎢","🎰","🪄","🧿","🪬","🧶","🪢","🧷","🪭","🪮","🛍️","🛒"),
                                "Viagens" to listOf("🚗","🚕","🚌","🚎","🏎️","🚓","🚑","🚒","🚲","🛵","✈️","🛫","🚀","🛸","🚁","⛵","🚢","⚓","🏝️","🏖️","🌋","🗻","🏕️","🏜️","🌅","🌄","🌃","🌌","🌠","🌙","☀️","⭐","🌟","⚡","❄️","☔","🌊","🌪️","🏠","🏡","🏢","🏬","🏥","🏦","🏨","🏩","🏪","🏫","🏭","🏰","🏯","🗼","🗽","⛪","🕌","🛕","🕍","⛩️","🕋","⛲","⛺","🌁","🌉","🌆","🌇","🌄","🌅","🌠","🌌","🌍","🌎","🌏","🌐","🗺️","🧭","🧳","🛣️","🛤️","🚦","🚥","🛑","🚧","⛽","🚏","🛴","🛹","🚂","🚆","🚄","🚅","🚈","🚇","🚊","🚉","🚝","🚞","🚋","🚃","🚟","🚠","🚡","🛶","🛥️","🛳️","🛟","🪝","🛩️","🪂","💺","🛰️","🛎️","🧱","🪵","🪨","🪴"),
                                "Símbolos" to listOf("❤️‍🔥","💯","✨","💫","⭐","🌟","⚡","🔥","💧","❄️","☀️","🌙","🌈","☁️","⛅","🌤️","🌦️","🌧️","⛈️","🌩️","🌨️","🌫️","🌪️","🌀","🌊","💥","💢","💨","🕳️","💤","🎵","🎶","🔔","🔕","📣","📢","🔊","🔉","🔈","🔇","♻️","✅","☑️","✔️","❌","❎","➕","➖","✖️","➗","🟰","♾️","‼️","⁉️","❓","❔","❕","❗","〰️","➰","➿","🔱","⚜️","🔰","♻️","🔆","🔅","〽️","⚠️","🚫","⛔","🔞","📵","🚭","🚯","🚱","🚷","🔃","🔄","🔙","🔚","🔛","🔜","🔝","🆗","🆒","🆕","🆓","🆙","🆖","🅰️","🅱️","🆎","🅾️","🆘","🆔","㊙️","㊗️","🔴","🟠","🟡","🟢","🔵","🟣","⚫","⚪","🟤","🔺","🔻","🔶","🔷","🔸","🔹","🔳","🔲","▪️","▫️","◾","◽","◼️","◻️","⬛","⬜","🟥","🟧","🟨","🟩","🟦","🟪","🟫","💜","👑","💸","💰","💵","💎","🔮","🪩","🪐","🖤","💟","⚡","♛","©️","®️","™️","🔒","🔓","🔑","🗝️","🛡️","⚔️","🪄","🧿","🪬","🎯","🎲","🃏","🀄","♠️","♥️","♦️","♣️","🕉️","☮️","✝️","☪️","☯️","🕎","🔯","🛐","⛎","♈","♉","♊","♋","♌","♍","♎","♏","♐","♑","♒","♓","🆚","🏳️","🏴","🏁","🚩","🎌","🏳️‍🌈","🏳️‍⚧️")
                            )
                            Column(
                                modifier = Modifier.fillMaxWidth()
                                    .heightIn(max = if (dialogKeyboardVisible) 300.dp else 360.dp)
                                    .background(RemoteAppearance.commentBackgroundColor)
                                    .padding(horizontal = 10.dp, vertical = 8.dp)
                            ) {
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    contentPadding = PaddingValues(bottom = 8.dp)
                                ) {
                                    items(commentEmojiGroups) { group ->
                                        Surface(
                                            color = if (selectedCommentEmojiCategory == group.first) RemoteAppearance.commentAccentColor.copy(alpha = 0.18f) else RemoteAppearance.commentInputBackgroundColor,
                                            shape = RoundedCornerShape(18.dp),
                                            modifier = Modifier.clickable { selectedCommentEmojiCategory = group.first }
                                        ) {
                                            Text(
                                                group.first,
                                                color = RemoteAppearance.commentTextColor,
                                                fontSize = 14.sp,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    "EMOJIS E REAÇÕES",
                                    color = RemoteAppearance.commentSecondaryTextColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                                )
                                val visibleCommentEmojis = commentEmojiGroups.firstOrNull { it.first == selectedCommentEmojiCategory }?.second.orEmpty()
                                if (selectedCommentEmojiCategory == "Recentes" && visibleCommentEmojis.isEmpty()) {
                                    Box(
                                        modifier = Modifier.fillMaxWidth().weight(1f),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("Seus emojis recentes aparecerão aqui.", color = RemoteAppearance.commentSecondaryTextColor, fontSize = 13.sp)
                                    }
                                } else {
                                    LazyVerticalGrid(
                                        columns = GridCells.Adaptive(minSize = 42.dp),
                                        modifier = Modifier.fillMaxWidth().weight(1f),
                                        contentPadding = PaddingValues(bottom = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceEvenly,
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        items(visibleCommentEmojis) { emoji ->
                                            Box(
                                                modifier = Modifier.fillMaxWidth().height(42.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .clickable {
                                                        commentDraft = (commentDraft + emoji).take(1200)
                                                        recentCommentEmojis = (listOf(emoji) + recentCommentEmojis.filterNot { it == emoji }).take(40)
                                                        commentEmojiPrefs.edit().putString("recent", recentCommentEmojis.joinToString("|")).apply()
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(emoji, fontSize = 27.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        HorizontalDivider(color = RemoteAppearance.commentDividerColor)
                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .background(RemoteAppearance.commentInputBackgroundColor)
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                                // Move only the composer row clear of Android's overlaid navigation controls.
                                .offset(y = if (dialogKeyboardVisible) 0.dp else 0.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                        Text(
                            "😊",
                            fontSize = 28.sp,
                            modifier = Modifier.size(48.dp).clickable { showCommentEmojis = !showCommentEmojis }
                                .padding(end = 2.dp)
                        )
                        OutlinedTextField(
                            value = commentDraft,
                            onValueChange = { commentDraft = it.take(1200) },
                            placeholder = { Text("Adicione um comentário...", color = RemoteAppearance.commentSecondaryTextColor) },
                            modifier = Modifier.weight(1f).padding(start = 10.dp),
                            shape = RoundedCornerShape(28.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = RemoteAppearance.commentTextColor,
                                unfocusedTextColor = RemoteAppearance.commentTextColor,
                                focusedBorderColor = RemoteAppearance.commentAccentColor,
                                unfocusedBorderColor = RemoteAppearance.commentDividerColor,
                                cursorColor = RemoteAppearance.commentAccentColor,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            )
                        )
                        Spacer(Modifier.width(10.dp))
                        IconButton(
                            enabled = commentDraft.isNotBlank() && !commentsLoading,
                            onClick = {
                                val draftToSend = commentDraft.trim()
                                val token = prefs.getString("token", null)
                                if (draftToSend.isBlank()) return@IconButton
                                if (token.isNullOrBlank()) {
                                    onNotice("Entre na sua conta para comentar.")
                                } else {
                                    commentsLoading = true
                                    Thread {
                                        try {
                                            val loadedComments: List<VyboraApi.CommentSummary>
                                            val loadedPosts: List<VyboraApi.PostSummary>
                                            
                                                VyboraApi.addComment(token ?: "", activePost.id, draftToSend)
                                                loadedComments = VyboraApi.getComments(activePost.id)
                                                loadedPosts = VyboraApi.getPosts()
                                            
                                            android.os.Handler(android.os.Looper.getMainLooper()).post {
                                                comments = loadedComments
                                                posts = loadedPosts
                                                commentDraft = ""
                                                commentsLoading = false
                                            }
                                        } catch (e: Exception) {
                                            android.os.Handler(android.os.Looper.getMainLooper()).post {
                                                commentsLoading = false
                                                onNotice(e.message ?: "Não foi possível publicar o comentário.")
                                            }
                                        }
                                    }.start()
                                }
                            },
                            modifier = Modifier.size(48.dp).clip(CircleShape)
                                .background(if (commentDraft.isNotBlank()) Brush.linearGradient(listOf(RemoteAppearance.commentAccentColor, RemoteAppearance.commentAccentColor)) else Brush.linearGradient(listOf(RemoteAppearance.commentDividerColor, RemoteAppearance.commentDividerColor)))
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Enviar comentário", tint = RemoteAppearance.commentTextColor)
                        }
                    }
                    }
            }
        }
    }
}

}

}

@Composable
private fun CommunitiesPage(onNotice: (String) -> Unit) {
    var selected by remember { mutableStateOf("Descobrir") }
    val examples = listOf(
        Triple("Jogos e Gamers", "Converse sobre jogos e novidades", "🎮"),
        Triple("Academia e Fitness", "Treinos, dicas e motivação", "💪"),
        Triple("Música e Shows", "Artistas, músicas e eventos", "🎵"),
        Triple("Animes e Mangás", "Compartilhe suas histórias favoritas", "✨")
    )
    Column(Modifier.fillMaxSize().background(RemoteAppearance.screenBackgroundColor).padding(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Minhas", "Em alta", "Descobrir").forEach { item ->
                Surface(
                    modifier = Modifier.weight(1f).clickable { selected = item },
                    shape = RoundedCornerShape(22.dp),
                    color = if (selected == item) Color(0xFF1677F2) else QiblyTheme.card
                ) {
                    Text(item, modifier = Modifier.padding(vertical = 12.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        color = if (selected == item) Color.White else RemoteAppearance.screenTextColor)
                }
            }
        }
        Spacer(Modifier.height(18.dp))
        if (selected == "Minhas") {
            Text("Você ainda não participa de comunidades.", color = RemoteAppearance.screenSecondaryTextColor)
            Spacer(Modifier.height(10.dp))
            Text("Descubra comunidades para participar.", modifier = Modifier.clickable { selected = "Descobrir" }, color = Color(0xFF1677F2))
        } else {
            Text(if (selected == "Em alta") "Comunidades em alta" else "Descubra comunidades",
                color = RemoteAppearance.screenTextColor, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            examples.forEach { (name, description, emoji) ->
                Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(emoji, style = MaterialTheme.typography.headlineMedium)
                    Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                        Text(name, color = RemoteAppearance.screenTextColor, fontWeight = FontWeight.SemiBold)
                        Text(description, color = RemoteAppearance.screenSecondaryTextColor, style = MaterialTheme.typography.bodySmall)
                    }
                    OutlinedButton(onClick = { onNotice("A participação em comunidades estará disponível quando o serviço for integrado.") }) {
                        Text("Entrar", color = Color(0xFF1677F2))
                    }
                }
                HorizontalDivider(color = RemoteAppearance.screenSecondaryTextColor.copy(alpha = 0.15f))
            }
        }
    }
}

@Composable
private fun MessagesPage(scrollToTopRequest: Int, recipientUsername: String, onSearch: () -> Unit, onOpenConversation: (String) -> Unit, onOpenProfile: (String) -> Unit, onNotice: (String) -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { context.getSharedPreferences("vybora_session", android.content.Context.MODE_PRIVATE) }
    val inboxListState = androidx.compose.foundation.lazy.rememberLazyListState()
    LaunchedEffect(scrollToTopRequest) { if (scrollToTopRequest > 0) inboxListState.animateScrollToItem(0) }
    var draft by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf<List<VyboraApi.MessageSummary>>(emptyList()) }
    var inbox by remember { mutableStateOf<List<VyboraApi.InboxSummary>>(emptyList()) }
    var loadingInbox by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var sending by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("Todas") }
    var inboxSearch by remember { mutableStateOf("") }
    var showConversationMenu by remember { mutableStateOf(false) }
    var conversationSearch by remember { mutableStateOf(false) }
    var messageSearch by remember { mutableStateOf("") }
    var showClearConfirm by remember { mutableStateOf(false) }
    var showBlockConfirm by remember { mutableStateOf(false) }
    var showReportConfirm by remember { mutableStateOf(false) }
    var isMuted by remember(recipientUsername) { mutableStateOf(prefs.getBoolean("muted_$recipientUsername", false)) }
    var isPinned by remember(recipientUsername) { mutableStateOf(prefs.getBoolean("pinned_$recipientUsername", false)) }
    var ownUserId by remember { mutableStateOf(prefs.getString("user_id", "") ?: "") }
    var recipientAvatarUrl by remember(recipientUsername) { mutableStateOf("") }
    val visibleMessages = if (messageSearch.isBlank()) messages else messages.filter { it.body.contains(messageSearch, ignoreCase = true) }
    val filteredInbox = inbox.filter { inboxSearch.isBlank() || it.displayName.contains(inboxSearch, ignoreCase = true) || it.username.contains(inboxSearch, ignoreCase = true) || it.body.contains(inboxSearch, ignoreCase = true) }
    val visibleInbox = when (selectedFilter) {
        "Não lidas" -> filteredInbox.filter { it.unreadCount > 0 }
        // A API atual só retorna conversas individuais; ainda não classifica grupos nem solicitações.
        "Grupos", "Solicitações" -> emptyList()
        else -> filteredInbox
    }

    LaunchedEffect(prefs.getString("token", null)) {
        val profileToken = prefs.getString("token", null)
        if (!profileToken.isNullOrBlank()) {
            try {
                val profileResponse = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) { VyboraApi.getMyProfile(profileToken) }
                val currentUser = profileResponse.optJSONObject("user")
                if (currentUser != null) {
                    val id = currentUser.optString("id", currentUser.optString("userId", currentUser.optString("user_id")))
                    if (id.isNotBlank()) {
                        ownUserId = id
                        prefs.edit().putString("user_id", id).apply()
                    }
                }
            } catch (_: Exception) { }
        }
    }

    LaunchedEffect(recipientUsername) {
        recipientAvatarUrl = ""
        if (recipientUsername.isNotBlank()) {
            try {
                val publicProfile = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) { VyboraApi.getPublicProfile(recipientUsername) }
                val profileUser = publicProfile.optJSONObject("user") ?: publicProfile.optJSONObject("profile") ?: publicProfile
                recipientAvatarUrl = profileUser.optString("avatarUrl").ifBlank { profileUser.optString("avatar_url") }
            } catch (_: Exception) { }
        }
        messages = emptyList()
        if (recipientUsername.isBlank()) {
            val token = prefs.getString("token", null)
            if (!token.isNullOrBlank()) {
                loadingInbox = true
                var firstInboxLoad = true
                while (true) {
                    try {
                        val loadedInbox = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) { VyboraApi.getInbox(token) }
                        val demoInbox = listOf(
                            VyboraApi.InboxSummary("carol.mendes.demo", "Carol Mendes", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=160", "Amei sua publicação! ❤️", "Agora", 0),
                            VyboraApi.InboxSummary("julia.ribeiro.demo", "Júlia Ribeiro", "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=160", "Vamos combinar aquele café? ☕", "Há 5 min", 0),
                            VyboraApi.InboxSummary("marina.costa.demo", "Marina Costa", "https://images.unsplash.com/photo-1531123897727-8f129e1688ce?w=160", "Que foto linda! ✨", "Há 12 min", 0),
                            VyboraApi.InboxSummary("ana.lima.demo", "Ana Lima", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=160", "Oi, tudo bem? 😊", "Ontem", 0)
                        )
                        inbox = (loadedInbox + demoInbox).distinctBy { it.username }
                    } catch (e: Exception) {
                        if (firstInboxLoad) onNotice(e.message ?: "Não foi possível carregar suas conversas.")
                    } finally {
                        loadingInbox = false
                    }
                    firstInboxLoad = false
                    kotlinx.coroutines.delay(15_000)
                }
            } else {
                inbox = emptyList()
                loadingInbox = false
            }
        }
        if (recipientUsername.isNotBlank()) {
            val token = prefs.getString("token", null)
            if (token.isNullOrBlank()) {
                onNotice("Entre na sua conta para abrir mensagens.")
            } else {
                loading = true
                var firstLoad = true
                while (true) {
                    try {
                        val loaded = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                            VyboraApi.getMessages(token, recipientUsername)
                        }
                        val demoMessages = if (recipientUsername.endsWith(".demo")) listOf(
                            VyboraApi.MessageSummary("demo-msg-01", "Oi! Vi suas fotos no QINBLY 😊", "demo-user", "Hoje"),
                            VyboraApi.MessageSummary("demo-msg-02", "Obrigada! Estou testando as mensagens por aqui ✨", ownUserId, "Hoje"),
                            VyboraApi.MessageSummary("demo-msg-03", "Ficou ótimo! Depois me manda outra publicação ❤️", "demo-user", "Agora")
                        ) else emptyList()
                        messages = if (loaded.isEmpty()) demoMessages else loaded
                        loading = false
                    } catch (e: Exception) {
                        loading = false
                        if (firstLoad) onNotice(e.message ?: "Não foi possível carregar a conversa.")
                    }
                    firstLoad = false
                    kotlinx.coroutines.delay(5000)
                }
            }
        }
    }

    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (recipientUsername.isBlank()) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                listOf("Todas", "Não lidas", "Grupos", "Solicitações").forEach { label ->
                    val selected = selectedFilter == label
                    Surface(
                        modifier = Modifier.clickable { selectedFilter = label },
                        color = if (selected) Color(0xFF1877F2) else RemoteAppearance.screenBackgroundColor,
                        shape = RoundedCornerShape(22.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (selected) Color(0xFF1877F2) else RemoteAppearance.screenSecondaryTextColor.copy(alpha = 0.55f))
                    ) {
                        Text(label, Modifier.padding(horizontal = 11.dp, vertical = 10.dp), color = if (selected) Color.White else RemoteAppearance.screenTextColor, style = MaterialTheme.typography.labelMedium, maxLines = 1)
                    }
                }
            }
            when {
                loadingInbox -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Pink) }
                selectedFilter == "Grupos" || selectedFilter == "Solicitações" -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Default.ChatBubbleOutline, contentDescription = null, tint = Pink, modifier = Modifier.size(36.dp))
                        Text(if (selectedFilter == "Grupos") "Nenhum grupo disponível" else "Nenhuma solicitação", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(
                            if (selectedFilter == "Grupos") "As conversas em grupo precisam ser habilitadas no serviço de mensagens." else "As solicitações aparecerão aqui quando o backend disponibilizar essa função.",
                            color = RemoteAppearance.screenSecondaryTextColor, style = MaterialTheme.typography.bodyMedium, textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
                visibleInbox.isNotEmpty() -> LazyColumn(Modifier.fillMaxSize(), state = inboxListState, verticalArrangement = Arrangement.spacedBy(0.dp), contentPadding = PaddingValues(vertical = 4.dp)) {
                    items(visibleInbox, key = { it.username }) { conversation ->
                        Column(Modifier.fillMaxWidth()) {
                        Row(
                            Modifier.fillMaxWidth()
                                .clickable { onOpenConversation(conversation.username) }.padding(horizontal = 4.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            QiblyAccountAvatar(conversation.username, conversation.avatarUrl, 70.dp) { onOpenProfile(conversation.username) }
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                Text(conversation.displayName.ifBlank { conversation.username }, color = RemoteAppearance.screenTextColor, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                                Text(conversation.body.ifBlank { "@${conversation.username}" }, color = RemoteAppearance.screenSecondaryTextColor, maxLines = 1, style = MaterialTheme.typography.bodyLarge)
                            }
                            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Agora", color = RemoteAppearance.screenSecondaryTextColor, style = MaterialTheme.typography.labelSmall)
                                if (conversation.unreadCount > 0) Box(
                                    Modifier.sizeIn(minWidth = 20.dp, minHeight = 20.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF1877F2))
                                        .padding(horizontal = 5.dp, vertical = 2.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(if (conversation.unreadCount > 99) "99+" else conversation.unreadCount.toString(), color = Color.White, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, maxLines = 1)
                                }
                            }
                        }
                        HorizontalDivider(color = RemoteAppearance.screenSecondaryTextColor.copy(alpha = 0.18f), thickness = 0.7.dp)
                        }
                    }
                }
                else -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(Modifier.size(74.dp).clip(CircleShape).background(Brush.linearGradient(listOf(Color(0xFF28103F), Color(0xFF111A32)))), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.ChatBubbleOutline, contentDescription = null, tint = Pink, modifier = Modifier.size(34.dp))
                        }
                        Text(if (selectedFilter == "Não lidas") "Tudo em dia!" else "Suas conversas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(if (selectedFilter == "Não lidas") "Você não tem mensagens não lidas." else "Pesquise pessoas e descubra contas para iniciar uma conversa.", color = RemoteAppearance.screenSecondaryTextColor, style = MaterialTheme.typography.bodyMedium, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        if (selectedFilter != "Não lidas") Button(onClick = onSearch, colors = ButtonDefaults.buttonColors(containerColor = Pink), shape = RoundedCornerShape(14.dp)) {
                            Icon(Icons.Default.Search, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Encontrar pessoas")
                        }
                    }
                }
            }
        } else {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                IconButton(onClick = { onOpenProfile(recipientUsername) }, modifier = Modifier.size(48.dp)) {
                    Box(
                        Modifier.size(44.dp).clip(CircleShape).background(Brush.linearGradient(listOf(Pink, Violet))).padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (recipientAvatarUrl.isNotBlank()) {
                            AsyncImage(
                                model = recipientAvatarUrl,
                                contentDescription = "Foto de perfil de $recipientUsername",
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                contentScale = androidx.compose.ui.layout.ContentScale.Fit
                            )
                        } else {
                            Box(Modifier.fillMaxSize().clip(CircleShape).background(RemoteAppearance.screenBackgroundColor), contentAlignment = Alignment.Center) {
                                Text(recipientUsername.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                Column(Modifier.weight(1f).clickable { onOpenProfile(recipientUsername) }) {
                    Text(recipientUsername.removePrefix("@"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1)
                    Text("Ver perfil", color = RemoteAppearance.screenSecondaryTextColor, style = MaterialTheme.typography.labelSmall)
                }
                IconButton(onClick = { conversationSearch = !conversationSearch; if (!conversationSearch) messageSearch = "" }) {
                    Icon(Icons.Default.Search, contentDescription = "Pesquisar nesta conversa", tint = Pink)
                }
                IconButton(onClick = { onNotice("A chamada de voz ficará disponível quando o serviço de chamadas for integrado.") }) {
                    Icon(Icons.Default.Call, contentDescription = "Ligar", tint = RemoteAppearance.screenTextColor)
                }
                IconButton(onClick = { onNotice("A chamada de vídeo ficará disponível quando o serviço de chamadas for integrado.") }) {
                    Icon(Icons.Default.Videocam, contentDescription = "Chamada de vídeo", tint = Color.White)
                }
                Box {
                    IconButton(onClick = { showConversationMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Opções da conversa", tint = RemoteAppearance.screenTextColor)
                    }
                    DropdownMenu(expanded = showConversationMenu, onDismissRequest = { showConversationMenu = false }, modifier = Modifier.background(RemoteAppearance.screenBackgroundColor)) {
                        DropdownMenuItem(
                            text = { Text(if (isMuted) "Ativar notificações" else "Silenciar notificações") },
                            leadingIcon = { Icon(Icons.Default.NotificationsOff, null, tint = Pink) },
                            onClick = {
                                isMuted = !isMuted
                                prefs.edit().putBoolean("muted_$recipientUsername", isMuted).apply()
                                showConversationMenu = false
                                onNotice(if (isMuted) "Notificações silenciadas neste dispositivo." else "Notificações reativadas.")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(if (isPinned) "Desafixar conversa" else "Fixar conversa") },
                            leadingIcon = { Icon(Icons.Default.PushPin, null, tint = Pink) },
                            onClick = {
                                isPinned = !isPinned
                                prefs.edit().putBoolean("pinned_$recipientUsername", isPinned).apply()
                                showConversationMenu = false
                                onNotice(if (isPinned) "Conversa marcada como fixada neste dispositivo." else "Conversa desafixada.")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Limpar conversa") },
                            leadingIcon = { Icon(Icons.Default.DeleteForever, null, tint = Pink) },                            onClick = { showConversationMenu = false; showClearConfirm = true }
                        )
                        DropdownMenuItem(                            text = { Text("Bloquear usuário") },
                            leadingIcon = { Icon(Icons.Default.Block, null, tint = Pink) },
                            onClick = { showConversationMenu = false; showBlockConfirm = true }                        )
                        DropdownMenuItem(
                            text = { Text("Denunciar usuário") },
                            leadingIcon = { Icon(Icons.Default.Warning, null, tint = Pink) },
                            onClick = { showConversationMenu = false; showReportConfirm = true }
                        )
                        DropdownMenuItem(
                            text = { Text("Configurações da conversa") },
                            leadingIcon = { Icon(Icons.Default.Settings, null, tint = Pink) },
                            onClick = { showConversationMenu = false; onNotice("As preferências locais desta conversa estão disponíveis neste menu.") }
                        )
                    }
                }
            }
            if (conversationSearch) {
                OutlinedTextField(
                    value = messageSearch,
                    onValueChange = { messageSearch = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("Pesquisar mensagens...") },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = Pink) },
                    trailingIcon = { IconButton(onClick = { messageSearch = ""; conversationSearch = false }) { Icon(Icons.Default.Close, contentDescription = "Fechar pesquisa") } },
                    shape = RoundedCornerShape(18.dp)
                )
            }


            LazyColumn(Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(horizontal = 4.dp, vertical = 14.dp)) {
                items(visibleMessages, key = { it.id }) { message ->
                    val isOwn = (ownUserId.isNotBlank() && message.senderId == ownUserId) ||
                        message.senderId.equals(prefs.getString("username", "") ?: "", ignoreCase = true)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = if (isOwn) Arrangement.End else Arrangement.Start) {
                        val bubbleShape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = if (isOwn) 18.dp else 5.dp, bottomEnd = if (isOwn) 5.dp else 18.dp)
                        Surface(
                            color = Color.Transparent,
                            shape = bubbleShape,
                            modifier = Modifier.widthIn(max = 340.dp).background(
                                brush = if (isOwn) Brush.linearGradient(listOf(Pink, Violet)) else Brush.linearGradient(listOf(Color(0xFF171B30), Color(0xFF111426))),
                                shape = bubbleShape
                            )
                        ) {
                            Column(Modifier.padding(horizontal = 17.dp, vertical = 14.dp)) {
                                Text(message.body, color = Color.White, style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp, lineHeight = 24.sp))
                                val timeLabel = try {
                                    java.time.Instant.parse(message.createdAt).atZone(java.time.ZoneId.systemDefault()).format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
                                } catch (_: Exception) { message.createdAt.take(16).replace("T", " ") }
                                Row(Modifier.align(Alignment.End).padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(timeLabel, color = Color.White.copy(alpha = 0.72f), style = MaterialTheme.typography.labelSmall)
                                    if (isOwn) Icon(Icons.Default.CheckCircle, contentDescription = "Enviada", tint = Color.White.copy(alpha = 0.72f), modifier = Modifier.size(12.dp))
                                }
                            }
                        }
                    }
                }
            }
            Row(Modifier.fillMaxWidth().padding(bottom = 16.dp, top = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(
                    onClick = {
                        try {
                            val intent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
                            if (intent.resolveActivity(context.packageManager) != null) context.startActivity(intent)
                            else onNotice("Nenhuma câmera disponível.")
                        } catch (_: Exception) { onNotice("Não foi possível abrir a câmera.") }
                    },
                    modifier = Modifier.size(48.dp).background(Brush.linearGradient(listOf(Pink, Violet)), CircleShape)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Abrir câmera", tint = Color.White, modifier = Modifier.size(25.dp))
                }
                OutlinedTextField(
                    value = draft,
                    onValueChange = { draft = it.take(2000) },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Escreva uma mensagem...", maxLines = 1) },
                    shape = RoundedCornerShape(28.dp),
                    maxLines = 4,
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { onNotice("O seletor de emojis será integrado à caixa de mensagem.") }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.EmojiEmotions, contentDescription = "Emojis", tint = Muted, modifier = Modifier.size(22.dp))
                            }
                            IconButton(onClick = { onNotice("O envio de fotos ainda precisa ser conectado ao backend.") }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Image, contentDescription = "Escolher imagem", tint = Muted, modifier = Modifier.size(22.dp))
                            }
                            IconButton(onClick = { onNotice("O envio de áudio ainda precisa ser conectado ao backend.") }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Mic, contentDescription = "Gravar áudio", tint = Muted, modifier = Modifier.size(22.dp))
                            }
                        }
                    }
                )
                IconButton(
                    onClick = {
                        if (draft.isBlank()) onNotice("Escreva uma mensagem para enviar.")
                        else if (!sending) {
                            val token = prefs.getString("token", null)
                            if (token.isNullOrBlank()) onNotice("Entre na sua conta para enviar mensagens.")
                            else {
                                sending = true
                                val body = draft.trim()
                                Thread {
                                    try {
                                        VyboraApi.sendMessage(token, recipientUsername, body)
                                        val loaded = VyboraApi.getMessages(token, recipientUsername)
                                        android.os.Handler(android.os.Looper.getMainLooper()).post { messages = loaded; draft = ""; sending = false }
                                    } catch (e: Exception) {
                                        android.os.Handler(android.os.Looper.getMainLooper()).post { sending = false; onNotice(e.message ?: "Não foi possível enviar a mensagem.") }
                                    }
                                }.start()
                            }
                        }
                    },
                    modifier = Modifier.size(48.dp).background(Brush.linearGradient(listOf(Pink, Violet)), CircleShape),
                    enabled = !sending
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Enviar mensagem", tint = RemoteAppearance.screenTextColor, modifier = Modifier.size(25.dp))
                }
            }
            if (showClearConfirm) {
                AlertDialog(
                    onDismissRequest = { showClearConfirm = false },
                    title = { Text("Limpar conversa?") },
                    text = { Text("O servidor atual não oferece exclusão de mensagens. Esta ação limpa apenas a visualização; as mensagens voltarão quando a conversa for recarregada.") },
                    confirmButton = {
                        TextButton(onClick = {
                            messages = emptyList()
                            showClearConfirm = false
                            onNotice("Visualização limpa; as mensagens continuam no servidor.")
                        }) { Text("Limpar visualização", color = Pink) }
                    },
                    dismissButton = { TextButton(onClick = { showClearConfirm = false }) { Text("Cancelar") } },
                    containerColor = Panel, titleContentColor = Color.White, textContentColor = Muted
                )
            }
            if (showBlockConfirm) {
                AlertDialog(
                    onDismissRequest = { showBlockConfirm = false },
                    title = { Text("Bloquear usuário?") },
                    text = { Text("O backend atual ainda não oferece a operação de bloqueio. Nenhuma alteração de conta será feita.") },
                    confirmButton = { TextButton(onClick = { showBlockConfirm = false; onNotice("Bloqueio precisa ser habilitado no backend.") }) { Text("Entendi", color = Pink) } },
                    dismissButton = { TextButton(onClick = { showBlockConfirm = false }) { Text("Cancelar") } },
                    containerColor = Panel, titleContentColor = Color.White, textContentColor = Muted
                )
            }
            if (showReportConfirm) {
                AlertDialog(
                    onDismissRequest = { showReportConfirm = false },
                    title = { Text("Denunciar usuário?") },
                    text = { Text("O backend atual ainda não oferece o envio de denúncias. Nenhuma denúncia será enviada agora.") },
                    confirmButton = { TextButton(onClick = { showReportConfirm = false; onNotice("Denúncias precisam ser habilitadas no backend.") }) { Text("Entendi", color = Pink) } },
                    dismissButton = { TextButton(onClick = { showReportConfirm = false }) { Text("Cancelar") } },
                    containerColor = Panel, titleContentColor = Color.White, textContentColor = Muted
                )
            }
        }
    }
}

// Remove o fundo branco ligado às bordas dos PNGs sem apagar os ornamentos internos.
private fun qinblyFrameWithoutWhiteSquare(source: Bitmap): Bitmap {
    val width = source.width
    val height = source.height
    val pixels = IntArray(width * height)
    source.getPixels(pixels, 0, width, 0, 0, width, height)
    val visited = BooleanArray(pixels.size)
    val queue = IntArray(pixels.size)
    var head = 0
    var tail = 0
    fun isWhite(index: Int): Boolean {
        val color = pixels[index]
        return android.graphics.Color.alpha(color) > 0 &&
            android.graphics.Color.red(color) >= 235 &&
            android.graphics.Color.green(color) >= 235 &&
            android.graphics.Color.blue(color) >= 235
    }
    fun enqueue(index: Int) {
        if (!visited[index] && isWhite(index)) {
            visited[index] = true
            queue[tail++] = index
        }
    }
    for (x in 0 until width) {
        enqueue(x)
        enqueue((height - 1) * width + x)
    }
    for (y in 0 until height) {
        enqueue(y * width)
        enqueue(y * width + width - 1)
    }
    while (head < tail) {
        val index = queue[head++]
        val x = index % width
        val y = index / width
        if (x > 0) enqueue(index - 1)
        if (x + 1 < width) enqueue(index + 1)
        if (y > 0) enqueue(index - width)
        if (y + 1 < height) enqueue(index + width)
    }
    for (i in 0 until tail) pixels[queue[i]] = android.graphics.Color.TRANSPARENT
    // Alguns PNGs têm um quadrado branco interno que não toca a borda da imagem.
    // Remover apenas o branco neutro fora da região circular da moldura.
    // O centro, as pedras e os brilhos da coroa permanecem intactos.
    val centerX = (width - 1) / 2f
    val centerY = (height - 1) / 2f
    val radius = minOf(width, height) * 0.43f
    for (y in 0 until height) {
        for (x in 0 until width) {
            val dx = x - centerX
            val dy = y - centerY
            if (dx * dx + dy * dy < radius * radius) continue
            val i = y * width + x
            val color = pixels[i]
            val red = android.graphics.Color.red(color)
            val green = android.graphics.Color.green(color)
            val blue = android.graphics.Color.blue(color)
            if (android.graphics.Color.alpha(color) > 0 &&
                red >= 238 && green >= 238 && blue >= 238 &&
                maxOf(red, green, blue) - minOf(red, green, blue) <= 12) {
                pixels[i] = android.graphics.Color.TRANSPARENT
            }
        }
    }
    return Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)
}

// Normaliza cada moldura individualmente: remove somente a margem transparente
// externa, preservando todos os ornamentos e uma folga para o brilho.
private fun qinblyNormalizedFrame(bitmap: Bitmap): Bitmap {
    val width = bitmap.width
    val height = bitmap.height
    if (width <= 0 || height <= 0) return bitmap
    val pixels = IntArray(width * height)
    bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
    var left = width
    var top = height
    var right = -1
    var bottom = -1
    for (y in 0 until height) {
        for (x in 0 until width) {
            if (android.graphics.Color.alpha(pixels[y * width + x]) > 8) {
                if (x < left) left = x
                if (x > right) right = x
                if (y < top) top = y
                if (y > bottom) bottom = y
            }
        }
    }
    if (right < left || bottom < top) return bitmap
    val margin = (maxOf(width, height) * 0.025f).toInt().coerceAtLeast(2)
    left = (left - margin).coerceAtLeast(0)
    top = (top - margin).coerceAtLeast(0)
    right = (right + margin).coerceAtMost(width - 1)
    bottom = (bottom + margin).coerceAtMost(height - 1)
    val cropWidth = right - left + 1
    val cropHeight = bottom - top + 1
    // Quadrado transparente para não distorcer o círculo ao exibir.
    val side = maxOf(cropWidth, cropHeight)
    val result = Bitmap.createBitmap(side, side, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(result)
    val src = android.graphics.Rect(left, top, right + 1, bottom + 1)
    val offsetX = (side - cropWidth) / 2
    val offsetY = (side - cropHeight) / 2
    val dst = android.graphics.Rect(offsetX, offsetY, offsetX + cropWidth, offsetY + cropHeight)
    canvas.drawBitmap(bitmap, src, dst, null)
    return result
}

@Composable
private fun QiblyAnimatedFrame(style: Int, diameter: Dp, verticalOffset: Dp = 0.dp) {
    val frames = intArrayOf(
        R.drawable.qinbly_frame_8778,
        R.drawable.qinbly_frame_8779,
        R.drawable.qinbly_frame_8780,
        R.drawable.qinbly_frame_8781,
        R.drawable.qinbly_frame_8783,
        R.drawable.qinbly_frame_8784,
        R.drawable.qinbly_frame_8785,
        R.drawable.qinbly_frame_8786,
        R.drawable.qinbly_frame_8787,
        R.drawable.qinbly_frame_8789,
        R.drawable.qinbly_frame_8790,
        R.drawable.qinbly_frame_8791,
        R.drawable.qinbly_frame_8792,
        R.drawable.qinbly_frame_8793,
        R.drawable.qinbly_frame_8794,
        R.drawable.qinbly_frame_8795,
        R.drawable.qinbly_frame_8796,
        R.drawable.qinbly_frame_8797,
        R.drawable.qinbly_frame_8798,
        R.drawable.qinbly_frame_8799,
        R.drawable.qinbly_frame_8800,
        R.drawable.qinbly_frame_8801,
        R.drawable.qinbly_frame_8802,
        R.drawable.qinbly_frame_8803,
        R.drawable.qinbly_frame_8804
    )
    val context = androidx.compose.ui.platform.LocalContext.current
    val resourceId = frames[style.coerceIn(0, frames.lastIndex)]
    val frameBitmap = androidx.compose.runtime.remember(resourceId) {
        val bitmap = BitmapFactory.decodeResource(context.resources, resourceId)
        qinblyNormalizedFrame(qinblyFrameWithoutWhiteSquare(bitmap)).asImageBitmap()
    }
    // Cada uma das oito molduras ativas tem uma assinatura de movimento própria.
    // A foto permanece imóvel: apenas o PNG transparente e seus reflexos se movem.
    val animationKind = when (style) {
        6 -> 0  // Coração Neon: batimento rosa
        7 -> 1  // Amor Pet: pulso alegre
        9 -> 2  // Laço Rosa: balanço suave
        10 -> 3 // Brilho Azul: reflexo elétrico
        11 -> 4 // Aura Prateada: brilho metálico
        12 -> 5 // Luz Celeste: aura luminosa
        13 -> 6 // Rosa Radiante: respiração rosa
        14 -> 7 // Estrela Neon: cintilação
        else -> 4
    }
    val transition = androidx.compose.animation.core.rememberInfiniteTransition(label = "qinbly_frame_motion_$style")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 6.2831855f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(
                durationMillis = when (animationKind) {
                    0 -> 1350
                    1 -> 1650
                    2 -> 2700
                    3 -> 1900
                    4 -> 3100
                    5 -> 2350
                    6 -> 2800
                    else -> 1450
                },
                easing = androidx.compose.animation.core.LinearEasing
            ),
            repeatMode = androidx.compose.animation.core.RepeatMode.Restart
        ),
        label = "qinbly_frame_phase_$style"
    )
    val wave = kotlin.math.sin(phase)
    val shimmer = (wave + 1f) / 2f
    val frameScale = if (style in 0..5 || style in 15..24) 1.23f else 0.98f
    val motionScale = when (animationKind) {
        0 -> 1f + 0.045f * shimmer
        1 -> 1f + 0.033f * shimmer
        2 -> 1f + 0.014f * shimmer
        3 -> 1f + 0.016f * shimmer
        4 -> 1f + 0.012f * shimmer
        5 -> 1f + 0.027f * shimmer
        6 -> 1f + 0.026f * shimmer
        else -> 1f + 0.024f * shimmer
    }
    val tilt = when (animationKind) {
        2 -> 2.5f * wave
        3 -> 1.3f * wave
        4 -> 1.0f * wave
        7 -> 1.5f * wave
        else -> 0f
    }
    val glowColor = when (animationKind) {
        0, 1, 2, 6 -> Color(0xFFFF35AA)
        3, 5 -> Color(0xFF16C9FF)
        4 -> Color(0xFFE5EEFF)
        else -> Color(0xFFAE60FF)
    }
    // O brilho usa o próprio canal alfa da moldura, sem quadrado ou disco atrás.
    Box(
        Modifier.requiredSize(diameter).offset(y = verticalOffset).graphicsLayer { clip = false },
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.Image(
            bitmap = frameBitmap,
            contentDescription = "Moldura decorativa animada do perfil",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
                .requiredSize(diameter * frameScale)
                .drawBehind {
                    val targetSize = androidx.compose.ui.unit.IntSize(size.width.toInt(), size.height.toInt())
                    val shadowX = 1.5.dp.toPx()
                    val shadowY = 3.dp.toPx()
                    withTransform({
                        translate(left = shadowX, top = shadowY)
                    }) {
                        drawImage(
                            image = frameBitmap,
                            dstSize = targetSize,
                            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Color.Black),
                            alpha = 0.19f
                        )
                    }
                    // Reflexo pulsante visível, especialmente nas peças neon.
                    val haloAlpha = when (animationKind) {
                        0, 1, 3, 5, 7 -> 0.14f + 0.29f * shimmer
                        else -> 0.09f + 0.20f * shimmer
                    }
                    withTransform({
                        scale(scaleX = 1.015f + 0.015f * shimmer, scaleY = 1.015f + 0.015f * shimmer)
                    }) {
                        drawImage(
                            image = frameBitmap,
                            dstSize = targetSize,
                            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(glowColor),
                            alpha = haloAlpha
                        )
                    }
                }
                .graphicsLayer {
                    scaleX = motionScale
                    scaleY = motionScale
                    rotationZ = tilt
                    alpha = 0.91f + 0.09f * shimmer
                    clip = false
                }
                .zIndex(100f)
        )
    }
}

@Composable
private fun QiblyAccountAvatar(username: String, fallbackUrl: String, diameter: Dp, onClick: () -> Unit) {
    var resolvedUrl by remember(username, fallbackUrl) { mutableStateOf(fallbackUrl) }
    var frame by remember(username) { mutableIntStateOf(-1) }
    LaunchedEffect(username) {
        frame = -1
        if (username.isNotBlank() && !username.contains(".demo")) {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                try {
                    val result = VyboraApi.getPublicProfile(username)
                    val profile = result.optJSONObject("user") ?: result.optJSONObject("profile") ?: result
                    val image = profile.optString("avatarUrl").ifBlank { profile.optString("coverUrl") }
                    val selected = profile.optInt("profileFrame", -1)
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        if (image.isNotBlank()) resolvedUrl = image
                        frame = selected.takeIf { it in 6..14 && it != 8 } ?: -1
                    }
                } catch (_: Exception) { }
            }
        }
    }
    Box(Modifier.size(diameter).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Box(Modifier.size(if (frame >= 0) diameter * 0.82f else diameter).clip(CircleShape).background(QiblyTheme.card)) {
            if (resolvedUrl.isNotBlank()) AsyncImage(model = resolvedUrl, contentDescription = "Foto de @$username", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            else Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.fillMaxSize().padding(8.dp))
        }
        if (frame >= 0) QiblyAnimatedFrame(frame, diameter)
    }
}

@Composable
private fun ProfilePage(scrollToTopRequest: Int, username: String, displayName: String, bio: String, photoRefreshKey: Int, onNotice: (String) -> Unit, onStat: (String) -> Unit, onAuth: () -> Unit, onEdit: () -> Unit, onEditProfilePhoto: () -> Unit, onSettings: () -> Unit) {
    val profileScrollState = rememberScrollState()
    LaunchedEffect(scrollToTopRequest) { if (scrollToTopRequest > 0) profileScrollState.animateScrollTo(0) }
    var postsCount by remember { mutableIntStateOf(0) }
    var followersCount by remember { mutableIntStateOf(0) }
    var followingCount by remember { mutableIntStateOf(0) }
    var profilePosts by remember { mutableStateOf<List<VyboraApi.PostSummary>>(emptyList()) }
    var profileTab by remember { mutableStateOf("Publicações") }
    var loadingPosts by remember { mutableStateOf(true) }
    var avatarUrl by remember { mutableStateOf("") }
    var profilePhotoUrl by remember { mutableStateOf("") }
    var originalCoverUrl by remember { mutableStateOf("") }
    var expandedImage by remember { mutableStateOf("") }
    var photoMenuOpen by remember { mutableStateOf(false) }
    var framesOpen by remember { mutableStateOf(false) }
    var selectedFramePreview by remember { mutableIntStateOf(0) }
    var viewingPublication by remember { mutableStateOf(false) }
    var selectedPublicationCaption by remember { mutableStateOf("") }
    var selectedPublication by remember { mutableStateOf<VyboraApi.PostSummary?>(null) }
    var postLiked by remember { mutableStateOf(false) }
    var commentsOpen by remember { mutableStateOf(false) }
    var commentsLoading by remember { mutableStateOf(false) }
    var postComments by remember { mutableStateOf<List<VyboraApi.CommentSummary>>(emptyList()) }
    var commentDraft by remember { mutableStateOf("") }
    var savedIds by remember { mutableStateOf<Set<String>>(emptySet()) }

    var publicationComment by remember { mutableStateOf("") }
    var joinedMonthYear by remember { mutableStateOf("") }

    val shownName = displayName.ifBlank { username }
    val shownBio = bio.lineSequence().filterNot { it.startsWith("🌍 ") }.joinToString("\n").trim()
    val shownCountry = bio.lineSequence().firstOrNull { it.startsWith("🌍 ") }?.removePrefix("🌍 ").orEmpty()
    val profileContext = androidx.compose.ui.platform.LocalContext.current
    val profilePrefs = remember { profileContext.getSharedPreferences("vybora_session", android.content.Context.MODE_PRIVATE) }
    var appliedFrame by remember(username) { mutableIntStateOf(-1) }

    LaunchedEffect(username, photoRefreshKey) {
        loadingPosts = true
        if (username.isNotBlank()) {
            Thread {
                try {
                    val token = profilePrefs.getString("token", "") ?: ""
                    val response = if (token.isNotBlank()) {
                        try { VyboraApi.getMyProfile(token) } catch (_: Exception) { VyboraApi.getPublicProfile(username) }
                    } else VyboraApi.getPublicProfile(username)
                    val user = response.optJSONObject("user") ?: response.optJSONObject("profile") ?: response
                    val accountFrame = user.optInt("profileFrame", -1).takeIf { it in 6..14 && it != 8 } ?: -1
                    // Falha no feed de publicações não pode impedir o carregamento da capa do perfil.
                    val loadedPosts = try {
                        VyboraApi.getPosts().filter { it.username.equals(username, ignoreCase = true) }
                    } catch (_: Exception) { emptyList() }
                    android.os.Handler(android.os.Looper.getMainLooper()).post {
                        if (user != null) {
                            appliedFrame = accountFrame
                            val created = listOf("created_at", "createdAt", "joined_at").map { user.optString(it) }.firstOrNull { it.matches(Regex("\\d{4}-\\d{2}.*")) }.orEmpty()
                            joinedMonthYear = if (created.length >= 7) {
                                val month = created.substring(5, 7).toIntOrNull() ?: 0
                                val months = listOf("janeiro", "fevereiro", "março", "abril", "maio", "junho", "julho", "agosto", "setembro", "outubro", "novembro", "dezembro")
                                if (month in 1..12) "${months[month - 1]} de ${created.substring(0, 4)}" else ""
                            } else ""
                            postsCount = user.optInt("posts_count")
                            followersCount = user.optInt("followers")
                            followingCount = user.optInt("following")
                            fun cleanImageValue(value: String): String =
                                value.trim().takeUnless { it.isBlank() || it.equals("null", true) || it.equals("undefined", true) }.orEmpty()
                            val coverPath = cleanImageValue(user.optString("cover_path"))
                            val savedCover = cleanImageValue(user.optString("coverUrl"))
                                .ifBlank { cleanImageValue(user.optString("cover_url")) }
                                .ifBlank {
                                    if (coverPath.startsWith("http")) coverPath else if (coverPath.isNotBlank()) "https://jqomgrdlocdqaarfqkay.supabase.co/storage/v1/object/public/post-media/$coverPath" else ""
                                }
                            avatarUrl = cleanImageValue(user.optString("avatarUrl"))
                                .ifBlank { cleanImageValue(user.optString("avatar_url")) }
                                .ifBlank {
                                    val avatarPath = cleanImageValue(user.optString("avatar_path"))
                                    if (avatarPath.startsWith("http")) avatarPath else if (avatarPath.isNotBlank()) "https://jqomgrdlocdqaarfqkay.supabase.co/storage/v1/object/public/post-media/$avatarPath" else ""
                                }
                            profilePhotoUrl = savedCover.ifBlank {
                                cleanImageValue(user.optString("profilePhotoUrl")).ifBlank { avatarUrl }
                            }
                            val originalPath = cleanImageValue(user.optString("cover_original_path"))
                            originalCoverUrl = cleanImageValue(user.optString("originalCoverUrl")).ifBlank {
                                if (originalPath.startsWith("http")) originalPath else if (originalPath.isNotBlank()) "https://jqomgrdlocdqaarfqkay.supabase.co/storage/v1/object/public/post-media/$originalPath" else ""
                            }
                        }
                        profilePosts = loadedPosts
                        loadingPosts = false
                    }
                } catch (_: Exception) {
                    android.os.Handler(android.os.Looper.getMainLooper()).post { loadingPosts = false }
                }
            }.start()
        } else {
            postsCount = 0
            followersCount = 0
            followingCount = 0
            avatarUrl = ""
            profilePosts = emptyList()
            loadingPosts = false
        }
    }

    // Tema visual exclusivo da moldura, sem alterar a aparência global.
    val frameAccent = when (appliedFrame) {
        6 -> Color(0xFFED3698) // coração neon
        7 -> Color(0xFFE67AAE) // amor pet
        9 -> Color(0xFFD9509D) // laço rosa
        10 -> Color(0xFF1687F4) // brilho azul
        11 -> Color(0xFF8999B0) // prata
        12 -> Color(0xFF45B5DF) // celeste
        13 -> Color(0xFFE4A32C) // dourado
        14 -> Color(0xFF8E5BDC) // violeta
        else -> Color(0xFF1677E8)
    }
    val profileAccent = if (appliedFrame >= 0) frameAccent else Color(0xFF1677E8)
    val frameTint = if (appliedFrame >= 0) frameAccent.copy(alpha = 0.11f) else RemoteAppearance.screenBackgroundColor
    Column(Modifier.fillMaxSize().background(RemoteAppearance.screenBackgroundColor).verticalScroll(profileScrollState), horizontalAlignment = Alignment.CenterHorizontally) {
        // Capa real e independente: sem retângulo em volta dos dados do perfil.
        Box(
            Modifier.fillMaxWidth().height(if (appliedFrame >= 0) 295.dp else 252.dp)
                .background(
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        if (appliedFrame >= 0) listOf(frameAccent.copy(alpha = 0.48f), frameAccent.copy(alpha = 0.18f), RemoteAppearance.screenBackgroundColor)
                        else listOf(RemoteAppearance.screenBackgroundColor, RemoteAppearance.screenBackgroundColor)
                    )
                )
        ) {
            if (appliedFrame >= 0) {
                // Faixas ornamentais laterais fazem parte da capa, não são bordas de cartão.
                Text(
                    when (appliedFrame) {
                        6 -> "♡\n✿\n♡\n✧"
                        7 -> "🐾\n♡\n🐾"
                        9 -> "✿\n❀\n✿"
                        10 -> "❄\n✧\n❄"
                        11 -> "◇\n✦\n◇"
                        12 -> "☁\n✧\n☁"
                        13 -> "♛\n✦\n♛"
                        else -> "✦\n✧\n✦"
                    },
                    modifier = Modifier.align(Alignment.CenterStart).padding(start = 15.dp),
                    color = frameAccent.copy(alpha = 0.68f), fontSize = 24.sp,
                    lineHeight = 44.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Text(
                    when (appliedFrame) {
                        6 -> "✧\n♡\n✿\n♡"
                        7 -> "♡\n🐾\n♡"
                        9 -> "❀\n✿\n❀"
                        10 -> "✧\n❄\n✧"
                        11 -> "✦\n◇\n✦"
                        12 -> "✧\n☁\n✧"
                        13 -> "✦\n♛\n✦"
                        else -> "✧\n✦\n✧"
                    },
                    modifier = Modifier.align(Alignment.CenterEnd).padding(end = 15.dp),
                    color = frameAccent.copy(alpha = 0.68f), fontSize = 24.sp,
                    lineHeight = 44.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
            if (appliedFrame >= 0) {
                // Ornamentos discretos exclusivos por estilo, sem cobrir a foto.
                Text(
                    when (appliedFrame) {
                        6 -> "♡  ✦  ♡"
                        7 -> "♡  🐾  ♡"
                        9 -> "✿  ♡  ✿"
                        10 -> "✧  ❄  ✧"
                        11 -> "✦  ◇  ✦"
                        12 -> "☁  ✧  ☁"
                        13 -> "✦  ♛  ✦"
                        14 -> "✧  ✦  ✧"
                        else -> "✦  ✧  ✦"
                    },
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = 17.dp),
                    color = frameAccent.copy(alpha = 0.70f),
                    fontSize = 21.sp,
                    letterSpacing = 7.sp
                )
            }
            IconButton(onClick = onSettings, modifier = Modifier.align(Alignment.TopEnd).padding(top = 8.dp, end = 12.dp)) {
                Icon(Icons.Default.Menu, contentDescription = "Configurações do perfil", tint = RemoteAppearance.screenTextColor)
            }
            // Moldura e retrato centralizados na capa, preservando a arte integral.
            Box(Modifier.align(Alignment.BottomCenter).then(if (appliedFrame >= 0) Modifier.offset(y = 85.dp) else Modifier).size(225.dp), contentAlignment = Alignment.Center) {
                Box(Modifier.size(185.dp).zIndex(0f).clip(CircleShape).background(QiblyTheme.card).clickable {
                    val image = originalCoverUrl.ifBlank { profilePhotoUrl.ifBlank { avatarUrl } }
                    if (image.isNotBlank()) { expandedImage = image; viewingPublication = false } else onEditProfilePhoto()
                }) {
                    val image = profilePhotoUrl.ifBlank { avatarUrl }
                    if (image.isNotBlank()) AsyncImage(model = image, contentDescription = "Foto do perfil", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    else Icon(Icons.Default.Person, contentDescription = "Foto do perfil", tint = RemoteAppearance.screenSecondaryTextColor, modifier = Modifier.fillMaxSize().padding(28.dp))
                }
                if (appliedFrame >= 0) QiblyAnimatedFrame(appliedFrame, 225.dp)
            }
        }
        Spacer(Modifier.height(6.dp))
        // Cartão de identidade separado da capa e decorado segundo a moldura.
        Column(
            Modifier.fillMaxWidth().padding(horizontal = if (appliedFrame >= 0) 14.dp else 0.dp)
                .then(if (appliedFrame >= 0) Modifier.background(
                    androidx.compose.ui.graphics.Brush.verticalGradient(listOf(frameAccent.copy(alpha = 0.14f), frameAccent.copy(alpha = 0.035f), RemoteAppearance.screenBackgroundColor)),
                    androidx.compose.foundation.shape.RoundedCornerShape(26.dp)
                ) else Modifier)
                .padding(top = if (appliedFrame >= 0) 94.dp else 15.dp, bottom = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
        Row(Modifier.padding(top = 10.dp).clickable { onEdit() }, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            Text(shownName.ifBlank { if (username.isBlank()) "Seu nome" else username }, color = RemoteAppearance.screenTextColor, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, maxLines = 1)
        }
        Text(if (username.isBlank()) "Entre ou crie sua conta" else "@${username.removePrefix("@")}", color = RemoteAppearance.screenSecondaryTextColor, style = MaterialTheme.typography.bodyMedium)
        if (shownBio.startsWith("✦ ")) {
            Text(shownBio.substringBefore('\n').removePrefix("✦ "), Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 7.dp), color = RemoteAppearance.screenTextColor, textAlign = androidx.compose.ui.text.style.TextAlign.Center, style = MaterialTheme.typography.bodyMedium)
            val description = shownBio.substringAfter('\n', "")
            if (description.isNotBlank()) Text(description, Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 5.dp), color = RemoteAppearance.screenSecondaryTextColor, textAlign = androidx.compose.ui.text.style.TextAlign.Center, maxLines = 4)
        } else if (shownBio.isNotBlank()) Text(shownBio, Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 10.dp), color = RemoteAppearance.screenTextColor, textAlign = androidx.compose.ui.text.style.TextAlign.Center, style = MaterialTheme.typography.bodyMedium, maxLines = 4)
        else Text("Adicione uma descrição ao seu perfil", Modifier.padding(top = 9.dp), color = RemoteAppearance.screenSecondaryTextColor)
        Row(Modifier.fillMaxWidth().padding(top = 9.dp, start = 12.dp, end = 12.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            if (shownCountry.isNotBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    Icon(Icons.Default.LocationOn, contentDescription = "País", tint = RemoteAppearance.screenSecondaryTextColor, modifier = Modifier.size(16.dp))
                    Text(shownCountry, color = RemoteAppearance.screenSecondaryTextColor, style = MaterialTheme.typography.bodySmall)
                }
            }
            if (shownCountry.isNotBlank() && joinedMonthYear.isNotBlank()) Spacer(Modifier.width(20.dp))
            if (joinedMonthYear.isNotBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    Icon(Icons.Default.CalendarToday, contentDescription = "Data de entrada", tint = RemoteAppearance.screenSecondaryTextColor, modifier = Modifier.size(16.dp))
                    Text("Entrou em $joinedMonthYear", color = RemoteAppearance.screenSecondaryTextColor, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(top = 14.dp, bottom = 10.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            ProfileStat("Seguindo", followingCount.toString(), Icons.Default.PersonAddAlt1) { onStat("Seguindo") }
            ProfileStat("Seguidores", followersCount.toString(), Icons.Default.Groups) { onStat("Seguidores") }
        }
        Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = onEdit, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(17.dp))
                Spacer(Modifier.width(6.dp))
                Text("Editar perfil")
            }
            OutlinedButton(onClick = { framesOpen = true }, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(17.dp))
                Spacer(Modifier.width(6.dp))
                Text("Molduras")
            }
        }
        } // cartão de identidade temático
        Spacer(Modifier.height(18.dp))
        // Abas em superfície limpa, separadas por um divisor sutil.
        Row(
            Modifier.fillMaxWidth()
                .background(RemoteAppearance.screenBackgroundColor)
                .padding(top = 10.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("Publicações", "Vídeos", "Fotos", "Salvos").forEach { item ->
                Column(Modifier.weight(1f).clickable { profileTab = item }.padding(vertical = 7.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(when(item) {
                        "Vídeos" -> Icons.Default.PlayCircle
                        "Fotos" -> Icons.Default.Image
                        "Salvos" -> Icons.Default.BookmarkBorder
                        else -> Icons.Outlined.GridView
                    }, contentDescription = item, tint = if (profileTab == item) profileAccent else RemoteAppearance.screenSecondaryTextColor, modifier = Modifier.size(23.dp))
                    Text(item, color = if (profileTab == item) profileAccent else RemoteAppearance.screenTextColor, style = MaterialTheme.typography.labelSmall, maxLines = 1)
                    Spacer(Modifier.height(5.dp))
                    Box(Modifier.fillMaxWidth(0.8f).height(2.dp).background(if (profileTab == item) profileAccent else Color.Transparent))
                }
            }
        }
        if (profileTab == "Salvos") {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(when (profileTab) {
                    "Salvos" -> "Publicações que você salvou"
                    else -> "Publicações que você salvou"
                }, color = RemoteAppearance.screenSecondaryTextColor, style = MaterialTheme.typography.bodyMedium)
            }
        } else if (loadingPosts) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Pink) }
        } else if (profilePosts.isEmpty()) {
            Box(Modifier.fillMaxWidth().padding(22.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.GridView, contentDescription = null, tint = Violet, modifier = Modifier.size(38.dp))
                    Text("Você ainda não publicou nada", fontWeight = FontWeight.Bold, color = RemoteAppearance.screenTextColor)
                    Text("Compartilhe sua primeira publicação com a comunidade.", color = RemoteAppearance.screenSecondaryTextColor, style = MaterialTheme.typography.bodySmall)
                }
            }
        } else {
            val visiblePosts = profilePosts.filter { profileTab != "Vídeos" || it.mediaType == "video" }
                .filter { profileTab != "Fotos" || it.mediaType == "image" }
            if (profileTab == "Publicações") {
                // Lista vertical de publicações mistas, sem modificar o cabeçalho ou as demais abas.
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    visiblePosts.forEach { post ->
                        Column(
                            modifier = Modifier.fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (QiblyTheme.dark) Color(0xFF20242C) else Color.White)
                                .border(1.dp, Color(0xFFE2E5EA), RoundedCornerShape(12.dp))
                                .clickable(enabled = post.mediaType != "poll") {
                                    selectedPublicationCaption = post.caption
                                    selectedPublication = post
                                    postLiked = false
                                    savedIds = profilePrefs.getStringSet("saved_post_ids", emptySet())?.toSet() ?: emptySet()
                                    if (post.mediaUrl.isNotBlank()) {
                                        expandedImage = post.mediaUrl
                                        viewingPublication = true
                                    } else {
                                        onNotice("Publicação de texto: " + post.caption)
                                    }
                                }
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(9.dp)
                        ) {
                            Text(post.caption.ifBlank { "Publicação" }, color = RemoteAppearance.screenTextColor, style = MaterialTheme.typography.bodyMedium)
                            if (post.mediaUrl.isNotBlank()) {
                                Box(Modifier.fillMaxWidth().height(240.dp).clip(RoundedCornerShape(8.dp))) {
                                    AsyncImage(model = post.mediaUrl, contentDescription = "Mídia da publicação", modifier = Modifier.fillMaxSize(), contentScale = androidx.compose.ui.layout.ContentScale.Crop)
                                    if (post.mediaType == "video") Icon(Icons.Default.PlayCircle, contentDescription = "Vídeo", tint = Color.White, modifier = Modifier.align(Alignment.Center).size(44.dp))
                                }
                            } else if (post.mediaType == "poll" && post.pollOptions.isNotEmpty()) {
                                val totalVotes = post.pollVotes.sum()
                                val hasVoted = post.myPollVote >= 0
                                Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                                    post.pollOptions.forEachIndexed { optionIndex, option ->
                                        val count = post.pollVotes.getOrElse(optionIndex) { 0 }
                                        val percent = if (totalVotes > 0) count * 100 / totalVotes else 0
                                        val selected = post.myPollVote == optionIndex
                                        val pollShape = RoundedCornerShape(14.dp)
                                        Row(
                                            Modifier.fillMaxWidth().height(52.dp)
                                                .background(if (selected) Color(0xFFF0F6FF) else Color.White, pollShape)
                                                .border(if (selected) 1.5.dp else 1.dp, if (selected) Color(0xFF1677F2) else Color(0xFFE0E7F0), pollShape)
                                                .clickable(enabled = !hasVoted) {
                                                    val token = profilePrefs.getString("token", null)
                                                    if (token.isNullOrBlank()) onNotice("Entre na sua conta para votar.")
                                                    else Thread {
                                                        try {
                                                            VyboraApi.votePoll(token, post.id, optionIndex)
                                                            val updated = VyboraApi.getPosts().filter { it.username.equals(username, ignoreCase = true) }
                                                            android.os.Handler(android.os.Looper.getMainLooper()).post { profilePosts = updated; onNotice("Voto registrado.") }
                                                        } catch (e: Exception) {
                                                            android.os.Handler(android.os.Looper.getMainLooper()).post { onNotice(e.message ?: "Não foi possível votar.") }
                                                        }
                                                    }.start()
                                                }.padding(horizontal = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Box(
                                                Modifier.size(34.dp)
                                                    .background(if (selected) Color(0xFFE8F2FF) else Color(0xFFF0F3F8), CircleShape)
                                                    .border(1.dp, if (selected) Color(0xFFC5DEFF) else Color.Transparent, CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(option.replace(Regex("^\\s*\\d+\\s*[—–-]\\s*"), ""), color = Color(0xFF14233B), fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                                            }
                                            Box(Modifier.weight(1f).height(20.dp).background(Color(0xFFE0E9F6), RoundedCornerShape(12.dp))) {
                                                if (percent > 0) {
                                                    Box(Modifier.fillMaxWidth(percent / 100f).fillMaxHeight().background(Color(0xFF1677F2), RoundedCornerShape(12.dp)))
                                                }
                                            }
                                            if (true) {
                                                Text("$percent%", color = Color(0xFF14233B), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                                if (selected) {
                                                    Box(Modifier.size(28.dp).background(Color(0xFF1677F2), CircleShape), contentAlignment = Alignment.Center) {
                                                        Text("✓", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    Text("$totalVotes ${if (totalVotes == 1) "voto" else "votos"}", color = RemoteAppearance.screenSecondaryTextColor, fontSize = 12.sp)
                                }
                            }
                            if (post.mediaType != "poll") {
                                val isSaved = post.id in savedIds
                                Row(
                                    Modifier.fillMaxWidth().padding(top = 5.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(Modifier.clickable {
                                        val token = profilePrefs.getString("token", null)
                                        if (token.isNullOrBlank()) onNotice("Entre na sua conta para curtir.")
                                        else Thread {
                                            try {
                                                VyboraApi.toggleLike(token, post.id)
                                                val updated = VyboraApi.getPosts().filter { it.username.equals(username, ignoreCase = true) }
                                                android.os.Handler(android.os.Looper.getMainLooper()).post { profilePosts = updated }
                                            } catch (e: Exception) {
                                                android.os.Handler(android.os.Looper.getMainLooper()).post { onNotice(e.message ?: "Não foi possível curtir.") }
                                            }
                                        }.start()
                                    }, verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.FavoriteBorder, contentDescription = "Curtir", tint = RemoteAppearance.screenTextColor, modifier = Modifier.size(23.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("${post.likes}", fontSize = 12.sp, color = RemoteAppearance.screenSecondaryTextColor)
                                    }
                                    Row(Modifier.clickable {
                                        selectedPublication = post
                                        commentsOpen = true
                                        commentsLoading = true
                                        Thread {
                                            try {
                                                val loaded = VyboraApi.getComments(post.id)
                                                android.os.Handler(android.os.Looper.getMainLooper()).post { postComments = loaded; commentsLoading = false }
                                            } catch (e: Exception) {
                                                android.os.Handler(android.os.Looper.getMainLooper()).post { commentsLoading = false; onNotice(e.message ?: "Não foi possível carregar comentários.") }
                                            }
                                        }.start()
                                    }, verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.ChatBubbleOutline, contentDescription = "Comentar", tint = RemoteAppearance.screenTextColor, modifier = Modifier.size(23.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("${post.comments}", fontSize = 12.sp, color = RemoteAppearance.screenSecondaryTextColor)
                                    }
                                    Row(Modifier.clickable {
                                        val send = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(android.content.Intent.EXTRA_TEXT, post.caption)
                                        }
                                        profileContext.startActivity(android.content.Intent.createChooser(send, "Compartilhar publicação"))
                                    }, verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Send, contentDescription = "Compartilhar", tint = RemoteAppearance.screenTextColor, modifier = Modifier.size(23.dp))
                                    }
                                    Row(Modifier.clickable {
                                        savedIds = if (isSaved) savedIds - post.id else savedIds + post.id
                                        profilePrefs.edit().putStringSet("saved_post_ids", savedIds).apply()
                                        onNotice(if (isSaved) "Publicação removida dos salvos." else "Publicação salva.")
                                    }, verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                            contentDescription = "Salvar",
                                            tint = if (isSaved) Color(0xFFFFC107) else RemoteAppearance.screenTextColor,
                                            modifier = Modifier.size(23.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                LazyVerticalGrid(columns = GridCells.Fixed(3), modifier = Modifier.fillMaxWidth().heightIn(max = 560.dp), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(visiblePosts.size) { index ->
                        val post = visiblePosts[index]
                        Box(Modifier.fillMaxWidth().height(145.dp).clip(RoundedCornerShape(8.dp)).background(Brush.linearGradient(listOf(Color(0xFF241047), Color(0xFF9D236E), Color(0xFF071A38)))).clickable { if (post.mediaUrl.isNotBlank()) { expandedImage = post.mediaUrl; selectedPublicationCaption = post.caption; selectedPublication = post; postLiked = false; savedIds = profilePrefs.getStringSet("saved_post_ids", emptySet())?.toSet() ?: emptySet(); viewingPublication = true } }) {
                            if (post.mediaUrl.isNotBlank()) AsyncImage(model = post.mediaUrl, contentDescription = "Publicação", modifier = Modifier.fillMaxSize(), contentScale = androidx.compose.ui.layout.ContentScale.Crop)
                            else Text(post.caption.ifBlank { "QINBLY" }, color = RemoteAppearance.screenTextColor, modifier = Modifier.align(Alignment.Center).padding(8.dp), maxLines = 4)
                            if (post.mediaType == "video") Icon(Icons.Default.PlayCircle, contentDescription = "Vídeo", tint = RemoteAppearance.screenTextColor, modifier = Modifier.align(Alignment.TopEnd).padding(5.dp))
                        }
                    }
                }
            }
        }
    }

    val likeSelectedPublication: () -> Unit = {
        val post = selectedPublication
        val token = profilePrefs.getString("token", null)
        if (post == null) onNotice("Publicação indisponível.")
        else if (token.isNullOrBlank()) onNotice("Entre na conta para curtir.")
        else if (!postLiked) Thread {
            try {
                val liked = VyboraApi.toggleLike(token, post.id)
                android.os.Handler(android.os.Looper.getMainLooper()).post { postLiked = liked }
            } catch (e: Exception) {
                android.os.Handler(android.os.Looper.getMainLooper()).post { onNotice(e.message ?: "Erro ao curtir.") }
            }
        }.start()
    }
    if (expandedImage.isNotBlank()) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { expandedImage = ""; viewingPublication = false },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
        ) {
            var viewerScale by remember(expandedImage) { mutableFloatStateOf(1f) }
            var viewerOffset by remember(expandedImage) { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }
            var viewerControls by remember(expandedImage) { mutableStateOf(true) }
            Box(Modifier.fillMaxSize().background(Color.Black)) {
                AsyncImage(
                    model = expandedImage,
                    contentDescription = "Visualização da foto",
                    modifier = Modifier.fillMaxSize()
                        .graphicsLayer {
                            scaleX = viewerScale
                            scaleY = viewerScale
                            translationX = viewerOffset.x
                            translationY = viewerOffset.y
                        }
                        .pointerInput(expandedImage) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                viewerScale = (viewerScale * zoom).coerceIn(1f, 5f)
                                viewerOffset = if (viewerScale > 1f) viewerOffset + pan else androidx.compose.ui.geometry.Offset.Zero
                            }
                        }
                        .pointerInput(expandedImage) {
                            detectTapGestures(
                                onTap = { viewerControls = !viewerControls },
                                onDoubleTap = {
                                    viewerScale = if (viewerScale > 1f) 1f else 2f
                                    viewerOffset = androidx.compose.ui.geometry.Offset.Zero
                                }
                            )
                        },
                    contentScale = androidx.compose.ui.layout.ContentScale.Fit
                )
                if (viewerControls) {
                    Row(
                        Modifier.fillMaxWidth().align(Alignment.TopCenter)
                            .statusBarsPadding().padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { expandedImage = ""; viewingPublication = false }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = Color.White)
                        }
                        Spacer(Modifier.weight(1f))
                        if (viewingPublication) {
                            Text("1/1", color = Color.White, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(Modifier.weight(1f))
                        Box {
                            IconButton(onClick = { photoMenuOpen = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "Opções da foto", tint = Color.White)
                            }
                            DropdownMenu(expanded = photoMenuOpen, onDismissRequest = { photoMenuOpen = false }) {
                                DropdownMenuItem(text = { Text("Salvar foto no celular") }, onClick = {
                                    photoMenuOpen = false
                                    saveProfilePhotoToDevice(profileContext, expandedImage, onNotice)
                                })
                                DropdownMenuItem(text = { Text("Compartilhar foto") }, onClick = {
                                    photoMenuOpen = false
                                    val share = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(android.content.Intent.EXTRA_TEXT, expandedImage)
                                    }
                                    profileContext.startActivity(android.content.Intent.createChooser(share, "Compartilhar foto"))
                                })
                            }
                        }
                    }
                    if (viewingPublication && selectedPublication != null) {
                        Column(
                            Modifier.align(Alignment.BottomStart).fillMaxWidth()
                                .padding(bottom = 92.dp)
                                .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.92f))))
                                .padding(start = 20.dp, end = 20.dp, bottom = 16.dp, top = 28.dp)
                        ) {
                            Text("@${selectedPublication?.username.orEmpty()}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            val viewerCaption = selectedPublicationCaption.ifBlank { selectedPublication?.caption.orEmpty() }
                            if (viewerCaption.isNotBlank()) {
                                Spacer(Modifier.height(6.dp))
                                Text(viewerCaption, color = Color.White, fontSize = 14.sp, maxLines = 3)
                            }
                        }
                    }
                }
            }
        }
    }

    if (framesOpen) {
        // Mantém apenas as nove molduras simples (IDs originais 7 a 15).
        // IDs originais preservados para compatibilidade com perfis já salvos.
        val simpleFrameIds = (6..14).filter { it != 8 }
        val frameNamesById = mapOf(
            6 to "Coração Neon",
            7 to "Amor Pet",
            9 to "Laço Rosa",
            10 to "Brilho Azul",
            11 to "Aura Prateada",
            12 to "Luz Celeste",
            13 to "Rosa Radiante",
            14 to "Estrela Neon"
        )
        val frameNames = simpleFrameIds.map { frameNamesById[it] ?: "Moldura" }
        val frameColors = List(simpleFrameIds.size) { Color(0xFF268BFF) }
        if (selectedFramePreview !in simpleFrameIds) selectedFramePreview = simpleFrameIds.first()
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { framesOpen = false },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Column(
                Modifier.fillMaxWidth().fillMaxHeight(0.87f)
                    .clip(RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp)
            ) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Molduras", fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    IconButton(onClick = { framesOpen = false }) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar molduras")
                    }
                }
                Text("8 molduras para a foto de perfil", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(12.dp))
                Box(Modifier.fillMaxWidth().height(290.dp), contentAlignment = Alignment.Center) {
                    Box(Modifier.size(225.dp), contentAlignment = Alignment.Center) {
                        Box(Modifier.size(185.dp).zIndex(0f).clip(CircleShape).background(QiblyTheme.card)) {
                            val image = profilePhotoUrl.ifBlank { avatarUrl }
                            if (image.isNotBlank()) AsyncImage(model = image,
                                contentDescription = "Prévia circular", modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop)
                            else Icon(Icons.Default.Person, contentDescription = null)
                        }
                        QiblyAnimatedFrame(selectedFramePreview, 225.dp)
                    }
                }
                Text(frameNamesById[selectedFramePreview] ?: "Moldura", modifier = Modifier.fillMaxWidth().padding(10.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center, fontWeight = FontWeight.Bold)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(frameNames.size) { index ->
                        Column(
                            Modifier.fillMaxWidth()
                                .border(if (selectedFramePreview == simpleFrameIds[index]) 3.dp else 1.dp, frameColors[index], RoundedCornerShape(14.dp))
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { selectedFramePreview = simpleFrameIds[index] }
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(Modifier.size(225.dp), contentAlignment = Alignment.Center) {
                                Box(Modifier.size(185.dp).zIndex(0f).clip(CircleShape).background(QiblyTheme.card)) {
                                    val image = profilePhotoUrl.ifBlank { avatarUrl }
                                    if (image.isNotBlank()) AsyncImage(model = image,
                                        contentDescription = null, modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop)
                                    else Icon(Icons.Default.Person, contentDescription = null)
                                }
                                QiblyAnimatedFrame(simpleFrameIds[index], 225.dp)
                            }
                            Text(frameNames[index], modifier = Modifier.padding(top = 8.dp), fontSize = 13.sp)
                        }
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = {
                        appliedFrame = -1
                        // Moldura persistida exclusivamente no perfil autenticado do servidor.
                        profilePrefs.getString("token", null)?.takeIf { it.isNotBlank() }?.let { token ->
                            Thread { try { VyboraApi.setProfileFrame(token, -1) } catch (_: Exception) { } }.start()
                        }
                        framesOpen = false
                    }, modifier = Modifier.weight(1f)) { Text("Sem moldura") }
                    Button(onClick = {
                        appliedFrame = selectedFramePreview
                        // Moldura persistida exclusivamente no perfil autenticado do servidor.
                        profilePrefs.getString("token", null)?.takeIf { it.isNotBlank() }?.let { token ->
                            Thread { try { VyboraApi.setProfileFrame(token, selectedFramePreview) } catch (_: Exception) { } }.start()
                        }
                        framesOpen = false
                    }, modifier = Modifier.weight(1f)) { Text("Aplicar") }
                }
            }
        }
    }
    if (commentsOpen) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { commentsOpen = false },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
        ) {
            BoxWithConstraints(
                Modifier.imePadding().fillMaxSize().background(Color.Black.copy(alpha = 0.58f)),
                contentAlignment = Alignment.BottomCenter
            ) {
                // Mesma altura e medição do painel de comentários do feed.
                val dialogKeyboardInset = with(androidx.compose.ui.platform.LocalDensity.current) { WindowInsets.ime.getBottom(this).toDp() }
                val dialogKeyboardVisible = dialogKeyboardInset > 120.dp
                val panelHeight = maxHeight * if (dialogKeyboardVisible) 0.90f else 0.90f
                Column(
                    Modifier.fillMaxWidth().height(panelHeight).offset(y = (-5).dp)
                        .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    Box(Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 8.dp), contentAlignment = Alignment.Center) {
                        Box(Modifier.width(48.dp).height(5.dp).clip(CircleShape).background(Color(0xFF777887)))
                    }
                    Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("Comentários", fontSize = 25.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        IconButton(onClick = { commentsOpen = false }) { Icon(Icons.Default.Close, contentDescription = "Fechar comentários") }
                    }
                    HorizontalDivider()
                    if (commentsLoading) {
                        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                    } else {
                        LazyColumn(Modifier.weight(1f).fillMaxWidth(), contentPadding = PaddingValues(16.dp)) {
                            if (postComments.isEmpty()) item {
                                Text("Seja a primeira pessoa a comentar.", modifier = Modifier.padding(vertical = 24.dp))
                            }
                            items(postComments) { comment ->
                                Column(Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
                                    Text(comment.displayName.ifBlank { comment.username }, fontWeight = FontWeight.Bold)
                                    Text(comment.body, modifier = Modifier.padding(top = 5.dp))
                                }
                                HorizontalDivider()
                            }
                        }
                    }
                    Column(Modifier.fillMaxWidth().padding(bottom = if (dialogKeyboardVisible) 0.dp else 80.dp)) {
                    Row(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant).padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("😊", fontSize = 28.sp, modifier = Modifier.padding(end = 10.dp).clickable { commentDraft += "😊" })
                        OutlinedTextField(
                            value = commentDraft,
                            onValueChange = { commentDraft = it },
                            placeholder = { Text("Adicione um comentário...") },
                            shape = RoundedCornerShape(28.dp),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        TextButton(onClick = {
                            val post = selectedPublication
                            val token = profilePrefs.getString("token", null)
                            val draft = commentDraft.trim()
                            if (post == null || token.isNullOrBlank()) onNotice("Entre na conta para comentar.")
                            else if (draft.isNotBlank() && !commentsLoading) {
                                commentsLoading = true
                                Thread {
                                    try {
                                        VyboraApi.addComment(token, post.id, draft)
                                        val loaded = VyboraApi.getComments(post.id)
                                        android.os.Handler(android.os.Looper.getMainLooper()).post {
                                            postComments = loaded
                                            commentDraft = ""
                                            commentsLoading = false
                                        }
                                    } catch (e: Exception) {
                                        android.os.Handler(android.os.Looper.getMainLooper()).post {
                                            commentsLoading = false
                                            onNotice(e.message ?: "Erro ao comentar.")
                                        }
                                    }
                                }.start()
                            }
                        }, enabled = commentDraft.isNotBlank() && !commentsLoading) { Icon(Icons.Default.Send, contentDescription = "Enviar comentário") }
                    }
                    }
                }
            }
        }
    }

}
@Composable
private fun AuthPage(onSuccess: (String) -> Unit, onCancel: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { context.getSharedPreferences("vybora_session", android.content.Context.MODE_PRIVATE) }
    var createAccount by remember { mutableStateOf(false) }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var googleError by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    // Clear stale profile-photo errors when opening authentication.
    androidx.compose.runtime.LaunchedEffect(Unit) { error = ""; googleError = "" }
    val googleSignInLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val activity = context as? android.app.Activity
        if (activity == null) {
            loading = false
            googleError = "Não foi possível concluir o login Google."
        } else {
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                try {
                    val idToken = GoogleSignIn.idTokenFromResult(result.data)
                    val auth = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                        VyboraApi.authenticateWithGoogle(idToken)
                    }
                    prefs.edit().putString("token", auth.token).putString("refresh_token", auth.refreshToken).putString("username", auth.username).apply()
                    rememberVyboraAccount(prefs, auth.username, auth.token, prefs.getString("display_name", "") ?: "", prefs.getString("bio", "") ?: "")
                    loading = false
                    onSuccess(auth.username)
                } catch (e: Exception) {
                    loading = false
                    val authMessage = e.message?.takeIf { it.isNotBlank() } ?: "Erro de autenticação sem detalhes retornados."
                    googleError = authMessage
                    error = ""
                    android.widget.Toast.makeText(context, authMessage, android.widget.Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    Column(
        Modifier.fillMaxSize().background(Night).verticalScroll(rememberScrollState()).padding(horizontal = 22.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onCancel, enabled = !loading) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = Color.Black)
            }
            Spacer(Modifier.width(4.dp))
            Text("Acesso à conta", color = Muted, style = MaterialTheme.typography.titleSmall)
        }

        Column(Modifier.fillMaxWidth().padding(top = 2.dp, bottom = 4.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                Modifier.size(76.dp).clip(RoundedCornerShape(24.dp)).background(Brush.linearGradient(listOf(Pink, Violet))),
                contentAlignment = Alignment.Center
            ) {
                Text("V", color = Color.Black, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Black)
            }
            Text("QINBLY", color = Color.Black, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
            Text(
                if (createAccount) "Crie sua conta e faça parte da comunidade." else "Entre para se conectar com sua comunidade.",
                color = Muted, style = MaterialTheme.typography.bodyMedium
            )
        }

        Surface(
            modifier = Modifier.fillMaxWidth().border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(26.dp)),
            color = Panel,
            shape = RoundedCornerShape(26.dp)
        ) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(if (createAccount) "Criar conta" else "Bem-vindo de volta", color = Color.Black, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(if (createAccount) "Preencha os dados para começar." else "Escolha como deseja entrar.", color = Muted, style = MaterialTheme.typography.bodySmall)

                Button(
                    onClick = {
                        val activity = context as? android.app.Activity
                        if (activity == null) {
                            googleError = "Não foi possível abrir a seleção da conta Google."
                        } else {
                            loading = true
                            // Reset errors for this Google attempt; profile errors stay separate.
                            error = ""
                            googleError = ""
                            GoogleSignIn.signOut(activity) {
                                try {
                                    googleSignInLauncher.launch(GoogleSignIn.signInIntent(activity))
                                } catch (e: Exception) {
                                    loading = false
                                    googleError = e.message ?: "Não foi possível abrir o login Google."
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    enabled = !loading,
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Violet, contentColor = Color.White)
                ) {
                    if (loading) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.Black)
                    else Text("Continuar com Google", fontWeight = FontWeight.Bold)
                }

                // Show only errors from the current Google authentication attempt.
                if (googleError.isNotBlank()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Pink.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Pink.copy(alpha = 0.45f))
                    ) {
                        Text(
                            text = googleError,
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            color = Pink,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    androidx.compose.material3.HorizontalDivider(Modifier.weight(1f), color = Color.White.copy(alpha = 0.14f))
                    Text("  ou entre com e-mail  ", color = Muted, style = MaterialTheme.typography.labelSmall)
                    androidx.compose.material3.HorizontalDivider(Modifier.weight(1f), color = Color.White.copy(alpha = 0.14f))
                }

                if (createAccount) {
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it.trim().replace(" ", "") },
                        label = { Text("Nome de usuário") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !loading,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Pink, unfocusedBorderColor = Color.White.copy(alpha = 0.22f),
                            focusedLabelColor = Pink, cursorColor = Pink, focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White, focusedContainerColor = Night, unfocusedContainerColor = Night
                        )
                    )
                }
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it.trim() },
                    label = { Text("E-mail") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !loading,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Pink, unfocusedBorderColor = Color.White.copy(alpha = 0.22f),
                        focusedLabelColor = Pink, cursorColor = Pink, focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White, focusedContainerColor = Night, unfocusedContainerColor = Night
                    )
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Senha (mínimo 8 caracteres)") },
                    singleLine = true,
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !loading,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Pink, unfocusedBorderColor = Color.White.copy(alpha = 0.22f),
                        focusedLabelColor = Pink, cursorColor = Pink, focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White, focusedContainerColor = Night, unfocusedContainerColor = Night
                    )
                )

                Button(
                    onClick = {
                        val normalizedEmail = email.trim().lowercase()
                        when {
                            !normalizedEmail.contains("@") || !normalizedEmail.substringAfter("@", "").contains(".") -> error = "Digite um e-mail válido."
                            password.length < 8 -> error = "A senha precisa ter pelo menos 8 caracteres."
                            createAccount && username.trim().length < 3 -> error = "O nome de usuário precisa ter 3 caracteres ou mais."
                            createAccount && !username.matches(Regex("[A-Za-z0-9_.]{3,24}")) -> error = "Use 3–24 caracteres: letras, números, ponto ou sublinhado."
                            else -> {
                                loading = true
                                error = ""
                                Thread {
                                    try {
                                        val result = VyboraApi.authenticate(createAccount, username, normalizedEmail, password)
                                        prefs.edit().putString("token", result.token).putString("refresh_token", result.refreshToken).putString("username", result.username).apply()
                                        rememberVyboraAccount(prefs, result.username, result.token, prefs.getString("display_name", "") ?: "", prefs.getString("bio", "") ?: "")
                                        android.os.Handler(android.os.Looper.getMainLooper()).post {
                                            loading = false
                                            onSuccess(result.username)
                                        }
                                    } catch (e: Exception) {
                                        android.os.Handler(android.os.Looper.getMainLooper()).post {
                                            loading = false
                                            error = e.message ?: "Não foi possível autenticar."
                                        }
                                    }
                                }.start()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    enabled = !loading,
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Pink, contentColor = Color.White)
                ) {
                    if (loading) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.Black)
                    else Text(if (createAccount) "Criar minha conta" else "Entrar", fontWeight = FontWeight.Bold)
                }

                TextButton(
                    onClick = { createAccount = !createAccount; error = "" },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !loading
                ) {
                    Text(if (createAccount) "Já tem conta? Fazer login" else "Não tem conta? Criar agora", color = Violet, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            enabled = !loading,
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(1.dp, Violet.copy(alpha = 0.7f)),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Violet)
        ) {
            Text("Voltar ao perfil", fontWeight = FontWeight.SemiBold)
        }
        Text(
            "Se o acesso falhar por configuração do servidor, verifique a API_BASE_URL conforme BACKEND/README.md.",
            color = Muted, style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
private fun EditProfilePage(initialUsername: String, initialName: String, initialBio: String, photoRefreshKey: Int, onEditProfilePhoto: () -> Unit, onBack: () -> Unit, onSaved: (String, String, String) -> Unit, onNotice: (String) -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { context.getSharedPreferences("vybora_session", android.content.Context.MODE_PRIVATE) }
    var editPhotoUrl by remember { mutableStateOf("") }
    LaunchedEffect(initialUsername, photoRefreshKey) {
        if (initialUsername.isNotBlank()) {
            Thread {
                try {
                    val token = prefs.getString("token", "").orEmpty()
                    val response = if (token.isNotBlank()) {
                        try { VyboraApi.getMyProfile(token) } catch (_: Exception) { VyboraApi.getPublicProfile(initialUsername) }
                    } else VyboraApi.getPublicProfile(initialUsername)
                    val user = response.optJSONObject("user") ?: response.optJSONObject("profile") ?: response
                    fun clean(value: String): String = value.trim().takeUnless { it.isBlank() || it.equals("null", true) || it.equals("undefined", true) }.orEmpty()
                    fun resolve(value: String): String {
                        val v = clean(value)
                        return if (v.startsWith("http")) v else if (v.isNotBlank()) "https://jqomgrdlocdqaarfqkay.supabase.co/storage/v1/object/public/post-media/${v.removePrefix("post-media/")}" else ""
                    }
                    val photo = listOf(
                        clean(user.optString("coverUrl")), clean(user.optString("cover_url")),
                        clean(user.optString("profilePhotoUrl")), clean(user.optString("profile_photo_url")),
                        resolve(user.optString("cover_path")),
                        clean(user.optString("avatarUrl")), clean(user.optString("avatar_url")),
                        resolve(user.optString("avatar_path"))
                    ).firstOrNull { it.startsWith("http") }.orEmpty()
                    android.os.Handler(android.os.Looper.getMainLooper()).post { editPhotoUrl = photo }
                } catch (_: Exception) {
                    try {
                        val user = VyboraApi.getPublicProfile(initialUsername).let { it.optJSONObject("user") ?: it.optJSONObject("profile") ?: it }
                        val image = listOf("coverUrl", "cover_url", "avatarUrl", "avatar_url").map { user.optString(it) }.firstOrNull { it.startsWith("http") }.orEmpty()
                        android.os.Handler(android.os.Looper.getMainLooper()).post { editPhotoUrl = image }
                    } catch (_: Exception) { }
                }
            }.start()
        }
    }
    val countryPrefix = "🌍 "
    val countryOptions = listOf("Brasil", "Portugal", "Estados Unidos", "Argentina", "Chile", "Colômbia", "México", "Espanha", "França", "Alemanha", "Itália", "Reino Unido", "Canadá", "Japão", "Angola", "Moçambique", "Outro")
    val initialCountry = initialBio.lineSequence().firstOrNull { it.startsWith(countryPrefix) }?.removePrefix(countryPrefix).orEmpty()
    var selectedCountry by remember { mutableStateOf(initialCountry) }
    var countryMenuExpanded by remember { mutableStateOf(false) }
    var username by remember { mutableStateOf(initialUsername) }
    var name by remember { mutableStateOf(initialName) }
    val availableCategories = listOf("Criador de conteúdo", "Lifestyle", "Viagens 🌎", "Games 🎮", "Beleza 💄", "Moda", "Fitness", "Música", "Arte", "Fotografia", "Tecnologia", "Gastronomia", "Humor", "Educação", "Esportes", "Natureza", "Academia 🏋️", "Musculação 💪", "Corrida 🏃", "Dança 💃", "Saúde e bem-estar", "Nutrição 🥗", "Yoga 🧘", "Pets 🐾", "Família", "Maternidade", "Empreendedorismo", "Negócios", "Finanças", "Carros 🚗", "Motos 🏍️", "Cinema 🎬", "Livros 📚", "DIY e artesanato", "Decoração", "Jardinagem 🌱", "Ciência", "Comédia", "Futebol ⚽", "Culinária 🍳")
    val categoryPrefix = "✦ "
    val initialCategoryLine = initialBio.lineSequence().firstOrNull().orEmpty()
    val storedCategories = if (initialCategoryLine.startsWith(categoryPrefix)) initialCategoryLine.removePrefix(categoryPrefix).split(" • ").filter { it in availableCategories } else emptyList()
    var selectedCategories by remember { mutableStateOf(storedCategories) }
    var bio by remember { mutableStateOf((if (storedCategories.isNotEmpty()) initialBio.substringAfter('\n', "") else initialBio).lineSequence().filterNot { it.startsWith(countryPrefix) }.joinToString("\n").trim()) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    var adjustingImage by remember { mutableStateOf("") }
    var cropRatio by remember { mutableStateOf("16:10") }
    var imageFilter by remember { mutableStateOf("Original") }

    val effectOptions = listOf("Original", "Vivo", "Neon", "Quente", "Frio", "Preto e branco", "Cinema", "Retrô", "Brilho", "Contraste", "Saturação", "Fade", "Glow", "Desfoque", "VHS", "Granulado", "Vinheta")
    val saveProfile: () -> Unit = {
                    if (name.isBlank()) error = "Informe seu nome."
                    else if (!username.matches(Regex("[a-z0-9_.]{3,24}"))) error = "O @usuário precisa ter de 3 a 24 caracteres."
                    else {
                        loading = true; error = ""
                        val token = prefs.getString("token", null)
                        Thread {
                            try {
                                if (token.isNullOrBlank()) {
                                    android.os.Handler(android.os.Looper.getMainLooper()).post {
                                        loading = false
                                        error = "Entre na sua conta para salvar as alterações."
                                    }
                                    return@Thread
                                }
                                val composedBio = (if (selectedCategories.isNotEmpty()) categoryPrefix + selectedCategories.joinToString(" • ") + "\n" else "") + (if (selectedCountry.isNotBlank()) countryPrefix + selectedCountry + "\n" else "") + bio.trim()
                                VyboraApi.updateProfile(token, username.trim(), name.trim(), composedBio)
                                android.os.Handler(android.os.Looper.getMainLooper()).post {
                                    loading = false
                                    onSaved(username.trim(), name.trim(), composedBio)
                                    onNotice("Informações do perfil atualizadas.")
                                }
                            } catch (e: Exception) {
                                android.os.Handler(android.os.Looper.getMainLooper()).post { loading = false; error = e.message ?: "Não foi possível salvar no servidor." }
                            }
                        }.start()
                    }
    }
    Column(Modifier.fillMaxSize().background(Night).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, enabled = !loading) { Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = Color.Black) }
            Text("Editar perfil", modifier = Modifier.weight(1f), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color.Black)
            Button(
                onClick = saveProfile,
                enabled = !loading,
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1677E8), contentColor = Color.White),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 9.dp)
            ) {
                if (loading) CircularProgressIndicator(Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp) else Text("Salvar", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        Column(Modifier.fillMaxWidth().weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp), contentAlignment = Alignment.Center) {
                Box(Modifier.size(116.dp).clip(CircleShape).background(QiblyTheme.card).clickable { onEditProfilePhoto() }, contentAlignment = Alignment.Center) {
                    if (editPhotoUrl.isNotBlank()) {
                        AsyncImage(model = editPhotoUrl, contentDescription = "Alterar foto de perfil", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    } else {
                        Icon(Icons.Default.Person, contentDescription = "Foto de perfil", tint = RemoteAppearance.screenSecondaryTextColor, modifier = Modifier.size(54.dp))
                    }
                    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Trocar foto de perfil", tint = Color.White.copy(alpha = 0.58f), modifier = Modifier.size(27.dp))
                    }
                }
            }
            Column(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 2.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(name.ifBlank { "Seu nome" }, color = RemoteAppearance.screenTextColor, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                Text("@${username.ifBlank { "seuusuario" }}", color = RemoteAppearance.screenSecondaryTextColor, style = MaterialTheme.typography.bodyLarge, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }

            Text("INFORMAÇÕES DO PERFIL", color = Muted, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)

            @Composable
            fun editCard(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String, hint: String, onValue: (String) -> Unit, singleLine: Boolean = true, max: Int = 80, centered: Boolean = false) {
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp))
                        .background(Brush.linearGradient(listOf(Color(0xAA17162B), Color(0xAA0A1325), Color(0xAA171126))))
                        .border(BorderStroke(1.dp, Brush.linearGradient(listOf(Pink.copy(alpha = 0.72f), Violet.copy(alpha = 0.62f)))), RoundedCornerShape(18.dp))
                        .padding(horizontal = 14.dp, vertical = if (singleLine) 10.dp else 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(icon, contentDescription = null, tint = Pink, modifier = Modifier.size(26.dp))
                    Spacer(Modifier.width(13.dp))
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(label, color = Muted, style = MaterialTheme.typography.labelMedium)
                        androidx.compose.foundation.text.BasicTextField(
                            value = value,
                            onValueChange = { onValue(it.take(max)) },
                            enabled = !loading,
                            singleLine = singleLine,
                            textStyle = androidx.compose.ui.text.TextStyle(color = RemoteAppearance.screenTextColor, fontSize = 16.sp, textAlign = if (centered) androidx.compose.ui.text.style.TextAlign.Center else androidx.compose.ui.text.style.TextAlign.Start),
                            cursorBrush = Brush.linearGradient(listOf(Pink, Violet)),
                            modifier = Modifier.fillMaxWidth(),
                            decorationBox = { inner ->
                                Box {
                                    if (value.isEmpty()) Text(hint, color = Muted.copy(alpha = 0.8f), style = MaterialTheme.typography.bodyMedium)
                                    inner()
                                }
                            }
                        )
                        if (!singleLine) Text("${value.length}/300", color = Muted, style = MaterialTheme.typography.labelSmall, modifier = Modifier.align(Alignment.End))
                    }
                    if (singleLine) Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(22.dp))
                }
            }

            editCard(Icons.Default.Person, "Nome", name, "Seu nome", { name = it }, max = 80, centered = true)
            editCard(Icons.Default.AlternateEmail, "Nome de usuário", username, "seuusuario", { username = it.lowercase().filter { ch -> ch.isLetterOrDigit() || ch == '_' || ch == '.' }.take(24) }, max = 24, centered = true)
            Text("PAÍS", color = RemoteAppearance.screenTextColor, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
            Box {
                OutlinedButton(onClick = { countryMenuExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Language, contentDescription = null, tint = Color(0xFF1677E8))
                    Spacer(Modifier.width(8.dp))
                    Text(selectedCountry.ifBlank { "Selecionar meu país" }, color = RemoteAppearance.screenTextColor)
                    Spacer(Modifier.weight(1f))
                    Icon(Icons.Default.ExpandMore, contentDescription = null, tint = RemoteAppearance.screenTextColor)
                }
                DropdownMenu(expanded = countryMenuExpanded, onDismissRequest = { countryMenuExpanded = false }) {
                    DropdownMenuItem(text = { Text("Não informar") }, onClick = { selectedCountry = ""; countryMenuExpanded = false })
                    countryOptions.forEach { country ->
                        DropdownMenuItem(text = { Text(country) }, onClick = { selectedCountry = country; countryMenuExpanded = false })
                    }
                }
            }
            Text("CATEGORIAS DO PERFIL", color = RemoteAppearance.screenTextColor, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
            Text("Escolha até 5 categorias para aparecerem abaixo do seu nome.", color = RemoteAppearance.screenSecondaryTextColor, style = MaterialTheme.typography.bodySmall)
            Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                availableCategories.chunked(2).forEach { categoryRow ->
                    Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    categoryRow.forEach { category ->
                    val selected = category in selectedCategories
                    androidx.compose.material3.FilterChip(
                        selected = selected,
                        onClick = { selectedCategories = if (selected) selectedCategories - category else if (selectedCategories.size < 5) selectedCategories + category else selectedCategories },
                        label = { Text(category, color = if (selected) Color.White else RemoteAppearance.screenTextColor) },
                        colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF1677E8), containerColor = QiblyTheme.card)
                    )
                    }
                    }
                }
            }
            editCard(Icons.Default.Description, "Biografia", bio, "Escreva uma breve descrição sobre você", { bio = it }, singleLine = false, max = 300)
            if (error.isNotBlank()) Text(error, color = Pink, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun EditProfilePhotoPage(onBack: () -> Unit, onSaved: () -> Unit, onNotice: (String) -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { context.getSharedPreferences("vybora_session", android.content.Context.MODE_PRIVATE) }
    var selectedProfilePhoto by remember { mutableStateOf<Uri?>(null) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    var dragX by remember { mutableFloatStateOf(0f) }
    var dragY by remember { mutableFloatStateOf(0f) }
    var profilePhotoScale by remember { mutableFloatStateOf(1f) }
    val density = androidx.compose.ui.platform.LocalDensity.current
    val screenWidthDp = androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp
    val previewWidthPx = with(density) { (screenWidthDp - 16).coerceAtLeast(1).dp.toPx() }
    val previewHeightPx = previewWidthPx
    val profilePhotoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            selectedProfilePhoto = uri
            dragX = 0f
            dragY = 0f
            profilePhotoScale = 1f
            error = ""
        }
    }

    LaunchedEffect(Unit) { profilePhotoPicker.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }

    Column(Modifier.fillMaxSize().background(Color.Black)) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, enabled = !loading) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Cancelar e voltar", tint = Color.Black)
            }
            Text(
                "Arraste para ajustar",
                modifier = Modifier.weight(1f),
                color = Color.Black,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.titleMedium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            IconButton(
                onClick = { if (selectedProfilePhoto != null && !loading) {
                    // O botão superior confirma a mesma ação do botão Salvar inferior.
                    error = ""
                } },
                enabled = selectedProfilePhoto != null && !loading
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = "Confirmar ajuste", tint = Color.Black)
            }
        }

        Box(
            Modifier.fillMaxWidth().weight(1f),
            contentAlignment = Alignment.Center
        ) {
            if (selectedProfilePhoto != null) {
                Box(
                    Modifier.fillMaxWidth().padding(horizontal = 8.dp).aspectRatio(1f)
                        .clip(CircleShape)
                        .background(Color(0xFF101010))
                        .pointerInput(selectedProfilePhoto) {
                            detectTransformGestures { centroid, pan, zoom, _ ->
                                // Permite arrastar e ampliar/reduzir a foto para ajustar o enquadramento 16:9.
                                profilePhotoScale = (profilePhotoScale * zoom).coerceIn(0.5f, 4f)
                                dragX += pan.x
                                dragY += pan.y
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // Fundo de preenchimento: ocupa a foto inteira, sem faixas pretas.
                    AsyncImage(
                        model = selectedProfilePhoto,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.28f)))
                    // Foto completa por cima: preserva toda a imagem e permite movê-la livremente.
                    AsyncImage(
                        model = selectedProfilePhoto,
                        contentDescription = "Foto completa sobre a foto do perfil em formato amplo; arraste livremente",
                        modifier = Modifier.fillMaxSize().graphicsLayer {
                            translationX = dragX
                            translationY = dragY
                            scaleX = profilePhotoScale
                            scaleY = profilePhotoScale
                        },
                        contentScale = ContentScale.Fit
                    )
                    Box(Modifier.fillMaxSize().border(2.dp, Color.White.copy(alpha = 0.75f), CircleShape))
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(36.dp))
                    Text("Escolha uma foto para ajustar", color = Color.Black, fontWeight = FontWeight.Medium)
                    OutlinedButton(onClick = { profilePhotoPicker.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }) {
                        Text("Escolher imagem", color = Color.Black)
                    }
                }
            }
        }

        if (error.isNotBlank()) {
            Text(error, Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp), color = Pink, style = MaterialTheme.typography.bodySmall)
        }

        Row(
            Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                enabled = !loading,
                modifier = Modifier.weight(1f).height(54.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color.White),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Text("Cancelar", fontWeight = FontWeight.SemiBold)
            }
            Button(
                onClick = {
                    val uri = selectedProfilePhoto
                    val token = prefs.getString("token", null)
                    if (uri == null) {
                        error = "Escolha uma imagem para ajustar."
                    } else if (token.isNullOrBlank()) {
                        error = "Entre na sua conta para alterar a foto."
                    } else {
                        loading = true
                        error = ""
                        val x = dragX
                        val y = dragY
                        val scale = profilePhotoScale
                        val previewW = previewWidthPx
                        val previewH = previewHeightPx
                        Thread {
                            var source: Bitmap? = null
                            var output: Bitmap? = null
                            try {
                                // Salva exatamente o enquadramento mostrado no editor, na mesma proporção 16:9 da capa.
                                source = context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
                                    ?: throw IllegalStateException("Não foi possível abrir a imagem.")
                                val outW = 1080
                                val outH = 1080
                                output = Bitmap.createBitmap(outW, outH, Bitmap.Config.ARGB_8888)
                                val canvas = Canvas(output!!)
                                val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
                                // Camada de fundo: mesmo comportamento ContentScale.Crop da prévia.
                                val bgScale = maxOf(outW.toFloat() / source!!.width, outH.toFloat() / source!!.height)
                                val bgW = source!!.width * bgScale
                                val bgH = source!!.height * bgScale
                                canvas.drawBitmap(source!!, null, android.graphics.RectF((outW-bgW)/2f, (outH-bgH)/2f, (outW+bgW)/2f, (outH+bgH)/2f), paint)
                                canvas.drawColor(android.graphics.Color.argb(71, 0, 0, 0))
                                // Camada principal: mesmo ContentScale.Fit, zoom e arrasto escolhidos pela pessoa.
                                val fitScale = minOf(outW.toFloat() / source!!.width, outH.toFloat() / source!!.height) * scale
                                val fgW = source!!.width * fitScale
                                val fgH = source!!.height * fitScale
                                val left = (outW - fgW) / 2f + x * outW / previewW
                                val top = (outH - fgH) / 2f + y * outH / previewH
                                canvas.drawBitmap(source!!, null, android.graphics.RectF(left, top, left + fgW, top + fgH), paint)
                                val outFile = File(context.cacheDir, "qinbly-profile-square.jpg")
                                FileOutputStream(outFile).use { stream -> output!!.compress(Bitmap.CompressFormat.JPEG, 94, stream) }
                                // Guarda a foto original separadamente para que tocar na capa abra a imagem completa.
                                VyboraApi.uploadProfileImage(context, token, uri, "cover_original")
                                // A capa visível no perfil continua usando exatamente o enquadramento escolhido.
                                VyboraApi.uploadProfileImage(context, token, Uri.fromFile(outFile), "cover")
                                // A foto de perfil também é o avatar público de todas as telas e publicações.
                                VyboraApi.uploadProfileImage(context, prefs.getString("token", token) ?: token, Uri.fromFile(outFile), "avatar")
                                outFile.delete()
                                android.os.Handler(android.os.Looper.getMainLooper()).post {
                                    loading = false
                                    onSaved()
                                }
                            } catch (e: Exception) {
                                android.os.Handler(android.os.Looper.getMainLooper()).post {
                                    loading = false
                                    error = e.message?.takeIf { it.isNotBlank() } ?: "Falha ao salvar a imagem da capa."
                                }
                            } finally {
                                source?.recycle()
                                output?.recycle()
                            }
                        }.start()
                    }
                },
                enabled = !loading && selectedProfilePhoto != null,
                modifier = Modifier.weight(1f).height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1677E8), contentColor = Color.White)
            ) {
                if (loading) CircularProgressIndicator(Modifier.size(18.dp), color = Color.Black, strokeWidth = 2.dp)
                else Text("Salvar", fontWeight = FontWeight.Bold)
            }
        }
    }
}

private fun nameFontFamily(style: String): androidx.compose.ui.text.font.FontFamily = when (style) {
    "Moderna" -> androidx.compose.ui.text.font.FontFamily.SansSerif
    "Elegante" -> androidx.compose.ui.text.font.FontFamily.Cursive
    "Clássica" -> androidx.compose.ui.text.font.FontFamily.Serif
    "Monoespaçada", "Tecnológica" -> androidx.compose.ui.text.font.FontFamily.Monospace
    "Minimalista", "SF Pro (similar)" -> androidx.compose.ui.text.font.FontFamily.SansSerif
    "Arredondada" -> androidx.compose.ui.text.font.FontFamily.SansSerif
    else -> androidx.compose.ui.text.font.FontFamily.SansSerif // SF Pro (similar) usa a alternativa nativa do Android.
}

@Composable
private fun SettingsPage(prefs: android.content.SharedPreferences, username: String, nameFontStyle: String, appFontStyle: String, onAppFontChange: (String) -> Unit, onNameFontChange: (String) -> Unit, onBack: () -> Unit, onNotice: (String) -> Unit, onEditProfile: () -> Unit, onAuth: () -> Unit, onSwitchAccounts: () -> Unit, onLogout: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var section by remember { mutableStateOf("home") }
    var fontSection by remember { mutableStateOf("names") }
    BackHandler {
        if (section == "font_names" || section == "font_app") section = "appearance" else if (section != "home") section = "home" else onBack()
    }
    var search by remember { mutableStateOf("") }
    var notifications by remember { mutableStateOf(prefs.getBoolean("setting_notifications", true)) }
    var privateAccount by remember { mutableStateOf(prefs.getBoolean("setting_private_account", false)) }
    var autoplay by remember { mutableStateOf(prefs.getBoolean("setting_autoplay", true)) }
    var activityStatus by remember { mutableStateOf(prefs.getBoolean("setting_activity_status", false)) }
    var allowMessages by remember { mutableStateOf(prefs.getBoolean("setting_messages", true)) }
    var allowComments by remember { mutableStateOf(prefs.getBoolean("setting_comments", true)) }
    var suggestProfile by remember { mutableStateOf(prefs.getBoolean("setting_suggest_profile", true)) }
    var dataSaver by remember { mutableStateOf(prefs.getBoolean("setting_data_saver", false)) }
    var likesNotice by remember { mutableStateOf(prefs.getBoolean("setting_notice_likes", true)) }
    var commentsNotice by remember { mutableStateOf(prefs.getBoolean("setting_notice_comments", true)) }
    var followersNotice by remember { mutableStateOf(prefs.getBoolean("setting_notice_followers", true)) }
    var messagesNotice by remember { mutableStateOf(prefs.getBoolean("setting_notice_messages", true)) }
    var livesNotice by remember { mutableStateOf(prefs.getBoolean("setting_notice_lives", true)) }
    var contentComments by remember { mutableStateOf(prefs.getBoolean("setting_content_comments", true)) }
    var contentMentions by remember { mutableStateOf(prefs.getBoolean("setting_content_mentions", true)) }
    var contentDirectMessages by remember { mutableStateOf(prefs.getBoolean("setting_content_dm", true)) }
    var contentReuse by remember { mutableStateOf(prefs.getBoolean("setting_content_reuse", true)) }
    var contentShareProfile by remember { mutableStateOf(prefs.getBoolean("setting_content_share_profile", true)) }
    var contentFollowing by remember { mutableStateOf(prefs.getBoolean("setting_content_following", true)) }
    var contentLikedVideos by remember { mutableStateOf(prefs.getBoolean("setting_content_liked", true)) }
    var contentViewers by remember { mutableStateOf(prefs.getBoolean("setting_content_viewers", true)) }

    fun exportAccountData() {
        val token = prefs.getString("token", null)
        if (token.isNullOrBlank()) {
            onNotice("Entre na sua conta antes de exportar os dados.")
            return
        }
        onNotice("Preparando a exportação dos seus dados…")
        Thread {
            try {
                val json = VyboraApi.exportMyData(token).toString(2)
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    try {
                        val share = Intent(Intent.ACTION_SEND).apply {
                            type = "application/json"
                            putExtra(Intent.EXTRA_SUBJECT, "Meus dados do QINBLY")
                            putExtra(Intent.EXTRA_TEXT, json)
                        }
                        context.startActivity(Intent.createChooser(share, "Salvar ou compartilhar seus dados"))
                        onNotice("Exportação preparada. Escolha um aplicativo para salvar o arquivo.")
                    } catch (_: Exception) {
                        onNotice("Os dados foram obtidos, mas não foi possível abrir o compartilhamento.")
                    }
                }
            } catch (e: Exception) {
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    onNotice(e.message ?: "Não foi possível exportar seus dados.")
                }
            }
        }.start()
    }

    val title = when (section) {
        "account" -> "Conta e perfil"
        "privacy" -> "Privacidade e segurança"
        "notifications" -> "Notificações"
        "appearance" -> "Tela"
        "font_names" -> "Fonte dos nomes"
        "font_app" -> "Fonte do aplicativo"
        "content" -> "Conteúdo e interações"
        "lives" -> "Lives e transmissão"
        "storage" -> "Dados e armazenamento"
        "help" -> "Ajuda e suporte"
        "feed" -> "Conteúdo"
        "camera_settings" -> "Publicações"
        "messages_settings" -> "Mensagens"
        "friends_settings" -> "Amigos e seguidores"
        "accessibility" -> "Acessibilidade"
        "moderation" -> "Segurança e moderação"
        "activity_history" -> "Atividade e histórico"
        "account_management" -> "Gerenciamento de contas"
        else -> "Configurações"
    }
    Column(Modifier.fillMaxSize().background(QiblyTheme.background).padding(horizontal = 12.dp, vertical = 8.dp)) {
        Row(Modifier.fillMaxWidth().padding(bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { if (section == "font_names" || section == "font_app") section = "appearance" else if (section == "home") onBack() else section = "home" }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = RemoteAppearance.screenTextColor)
            }
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = RemoteAppearance.screenTextColor)
                Text(if (section == "home") "Gerencie sua conta e sua experiência" else "Personalize sua experiência no QINBLY", color = RemoteAppearance.screenSecondaryTextColor, style = MaterialTheme.typography.bodySmall)
            }
        }
        if (section == "home") {
            Surface(color = QiblyTheme.card, shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).border(1.dp, QiblyTheme.border, RoundedCornerShape(16.dp))) {
                Row(Modifier.padding(horizontal = 13.dp, vertical = 11.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF2196F3))
                    Spacer(Modifier.width(10.dp))
                    androidx.compose.foundation.text.BasicTextField(
                        value = search, onValueChange = { search = it }, singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(color = RemoteAppearance.screenTextColor, fontSize = 15.sp),
                        cursorBrush = Brush.horizontalGradient(listOf(Color(0xFF2196F3), Color(0xFF1976D2))), modifier = Modifier.fillMaxWidth(),
                        decorationBox = { inner -> Box { if (search.isBlank()) Text("Buscar configurações...", color = RemoteAppearance.screenSecondaryTextColor); inner() } }
                    )
                }
            }
        }
        LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(2.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
            if (section == "home") {
                item { Text("CONFIGURAÇÕES", color = Color(0xFF8A9BAB), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold) }
                val categories = listOf(
                    Triple("account", "Conta", "Informações, segurança e mais"),
                    Triple("privacy", "Privacidade", "Controle quem pode ver e interagir"),
                    Triple("notifications", "Notificações", "Gerencie seus avisos"),
                    Triple("feed", "Conteúdo", "Preferências de feed e reprodução"),
                    Triple("appearance", "Tela", "Tema, idioma e aparência"),
                    Triple("camera_settings", "Publicações", "Câmera, vídeo e ferramentas"),
                    Triple("lives", "Lives", "Transmissões e interações"),
                    Triple("messages_settings", "Mensagens", "Conversas e privacidade"),
                    Triple("friends_settings", "Amigos e seguidores", "Gerencie suas conexões"),
                    Triple("storage", "Dados e armazenamento", "Espaço, downloads e economia de dados"),
                    Triple("accessibility", "Acessibilidade", "Tamanho do texto, contraste e mais"),
                    Triple("moderation", "Segurança e moderação", "Contas bloqueadas e denúncias"),
                    Triple("activity_history", "Atividade e histórico", "Histórico e estatísticas"),
                    Triple("help", "Ajuda e suporte", "Central de ajuda e problemas")
                ).filter { it.second.contains(search, true) || it.third.contains(search, true) }
                val loggedIn = username.isNotBlank() && !prefs.getString("token", null).isNullOrBlank()
                val accountActionMatches = search.isBlank() ||
                    "Alternar entre suas contas".contains(search, true) ||
                    "Sair".contains(search, true) ||
                    "Login".contains(search, true)
                categories.forEach { category ->
                    item {
                        val icon = when (category.first) {
                            "account" -> Icons.Default.Person
                            "privacy" -> Icons.Default.Lock
                            "notifications" -> Icons.Default.Notifications
                            "appearance" -> Icons.Default.DarkMode
                            "content" -> Icons.Default.ChatBubbleOutline
                            "lives" -> Icons.Default.Radio
                            "storage" -> Icons.Default.FileDownload
                            "feed" -> Icons.Default.PlayCircle
                            "camera_settings" -> Icons.Default.CameraAlt
                            "messages_settings" -> Icons.Default.ChatBubbleOutline
                            "friends_settings" -> Icons.Default.Person
                            "accessibility" -> Icons.Default.Visibility
                            "moderation" -> Icons.Default.Block
                            "activity_history" -> Icons.Default.Info
                            "account_management" -> Icons.Default.Logout
                            else -> Icons.Default.HelpOutline
                        }
                        SettingsLink(category.second, category.third, icon) { section = category.first }
                    }
                }
                if (loggedIn && (search.isBlank() || "Alternar entre suas contas".contains(search, true))) {
                    item { SettingsLink("Alternar entre suas contas", "Trocar de perfil sem sair da conta", Icons.Default.Person) { onSwitchAccounts() } }
                }
                if (loggedIn && (search.isBlank() || "Sair".contains(search, true) || "Encerrar sessão".contains(search, true))) {
                    item { SettingsLink("Sair", "Encerrar a sessão neste dispositivo", Icons.Default.Logout) { onLogout() } }
                }
                if (!loggedIn && accountActionMatches) {
                    item {
                        Surface(color = Panel, shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = Violet, modifier = Modifier.size(42.dp))
                                Text("Faça login para acessar mais opções", color = RemoteAppearance.screenTextColor, fontWeight = FontWeight.Bold)
                                Text("Entre na sua conta para personalizar sua experiência e salvar suas preferências.", color = RemoteAppearance.screenSecondaryTextColor, style = MaterialTheme.typography.bodySmall)
                                Button(onClick = onAuth, colors = ButtonDefaults.buttonColors(containerColor = Pink), modifier = Modifier.fillMaxWidth()) {
                                    Text("Login", color = RemoteAppearance.screenTextColor, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
                if (categories.isEmpty() && !accountActionMatches) item { Text("Nenhuma configuração encontrada.", color = RemoteAppearance.screenSecondaryTextColor, modifier = Modifier.padding(16.dp)) }
            } else when (section) {
                "account" -> {
                    item { Text(if (username.isBlank()) "Nenhuma conta conectada" else "Conta atual: @$username", color = RemoteAppearance.screenSecondaryTextColor, modifier = Modifier.padding(vertical = 6.dp)) }
                        item { SettingsLink("Segurança e permissões", "Senha, verificação e acesso", Icons.Default.Lock) { try { context.startActivity(Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, android.net.Uri.parse("package:" + context.packageName))) } catch (_: Exception) { onNotice("Não foi possível abrir as configurações de segurança do aplicativo.") } } }
                    item { SettingsLink("Seus pedidos", "Compras e assinaturas", Icons.Default.FileDownload) { onNotice("O módulo de pedidos ainda não está conectado.") } }
                    item { SettingsLink("Compartilhar perfil", "Convide amigos para conhecer seu perfil", Icons.Default.Share) {
                        try {
                            val share = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, "Conheça meu perfil no QINBLY: @${username}") }
                            context.startActivity(Intent.createChooser(share, "Compartilhar perfil"))
                        } catch (_: Exception) { onNotice("Não foi possível abrir o compartilhamento neste aparelho.") }
                    } }
                    item { SettingsLink("Dispositivos conectados", "Veja este dispositivo e encerre a sessão", Icons.Default.Lock) { onNotice("Dispositivo atual: " + android.os.Build.MANUFACTURER + " " + android.os.Build.MODEL + ". Para encerrar esta sessão, use Trocar conta.") } }
                    item { SettingsLink("Baixar seus dados", "Exportar seus dados da conta", Icons.Default.FileDownload) { exportAccountData() } }
                    item { SettingsLink("Desativar ou excluir conta", "Gerencie sua conta permanentemente", Icons.Default.DeleteForever) { onNotice("A exclusão exige um fluxo seguro de confirmação no servidor e não é executada nesta tela.") } }
                }
                "privacy" -> {
                    item { Text("VISIBILIDADE DA CONTA", color = Pink, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold) }
                    item { SettingSwitch("Conta privada", "Preferência local; a API precisa aplicar as regras de visibilidade.", privateAccount, Icons.Default.Lock) { privateAccount = it; prefs.edit().putBoolean("setting_private_account", it).apply(); onNotice("Preferência salva neste aparelho. A visibilidade real depende do backend.") } }
                    item { SettingSwitch("Mostrar status de atividade", "Controle sua preferência de presença.", activityStatus, Icons.Default.Visibility) { activityStatus = it; prefs.edit().putBoolean("setting_activity_status", it).apply() } }
                    item { Text("INTERAÇÕES", color = Pink, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp)) }
                    item { SettingSwitch("Permitir mensagens", "Escolha quem pode iniciar conversas.", allowMessages, Icons.Default.ChatBubbleOutline) { allowMessages = it; prefs.edit().putBoolean("setting_messages", it).apply(); onNotice("Preferência salva localmente; a API ainda precisa aplicar essa regra.") } }
                    item { SettingSwitch("Permitir comentários", "Controle quem pode comentar nas publicações.", allowComments, Icons.Default.ChatBubbleOutline) { allowComments = it; prefs.edit().putBoolean("setting_comments", it).apply(); onNotice("Preferência salva localmente; a API ainda precisa aplicar essa regra.") } }
                    item { SettingSwitch("Sugerir meu perfil", "Controle se seu perfil pode ser sugerido.", suggestProfile, Icons.Default.Person) { suggestProfile = it; prefs.edit().putBoolean("setting_suggest_profile", it).apply() } }
                    item { SettingsLink("Contas bloqueadas", "Gerencie quem não pode interagir com você", Icons.Default.Block) { onNotice("A lista de bloqueio será exibida quando o recurso estiver conectado ao backend.") } }
                }
                "notifications" -> {
                    item { SettingSwitch("Notificações", "Ativar ou desativar notificações neste aparelho", notifications, Icons.Default.Notifications) { notifications = it; prefs.edit().putBoolean("setting_notifications", it).apply() } }
                    item { SettingSwitch("Curtidas", "Quando alguém curtir seu conteúdo", likesNotice, Icons.Default.FavoriteBorder) { likesNotice = it; prefs.edit().putBoolean("setting_notice_likes", it).apply() } }
                    item { SettingSwitch("Comentários", "Quando alguém comentar", commentsNotice, Icons.Default.ChatBubbleOutline) { commentsNotice = it; prefs.edit().putBoolean("setting_notice_comments", it).apply() } }
                    item { SettingSwitch("Novos seguidores", "Quando alguém começar a seguir você", followersNotice, Icons.Default.Person) { followersNotice = it; prefs.edit().putBoolean("setting_notice_followers", it).apply() } }
                    item { SettingSwitch("Mensagens", "Quando você receber mensagens", messagesNotice, Icons.Default.ChatBubbleOutline) { messagesNotice = it; prefs.edit().putBoolean("setting_notice_messages", it).apply() } }
                    item { SettingSwitch("Lives", "Quando alguém iniciar uma live", livesNotice, Icons.Default.Radio) { livesNotice = it; prefs.edit().putBoolean("setting_notice_lives", it).apply() } }
                    item { Text("As preferências são locais; notificações push dependem da configuração do servidor.", color = RemoteAppearance.screenSecondaryTextColor, style = MaterialTheme.typography.labelSmall) }
                }
                "appearance" -> {
                    item { Text("TIPOGRAFIA", color = RemoteAppearance.screenSecondaryTextColor, style = MaterialTheme.typography.labelMedium) }
                    item { SettingsLink("Fonte dos nomes", "Personalize os nomes dos usuários", Icons.Default.Person) { section = "font_names" } }
                    item { SettingsLink("Fonte do aplicativo", "Personalize os menus, títulos e botões", Icons.Default.TextFields) { section = "font_app" } }
                    item { Spacer(Modifier.height(12.dp)) }
                    item { Text("VISUAL", color = RemoteAppearance.screenSecondaryTextColor, style = MaterialTheme.typography.labelMedium) }
                    item {
                        Text("TEMA DO APLICATIVO", color = QiblyTheme.secondary, style = MaterialTheme.typography.labelMedium)
                    }
                    item {
                        SettingSwitch(
                            "Modo escuro",
                            if (QiblyTheme.dark) "Tema escuro ativado" else "Tema claro ativado",
                            QiblyTheme.dark,
                            Icons.Default.DarkMode
                        ) { enabled ->
                            QiblyTheme.dark = enabled
                            prefs.edit().putString("setting_theme", if (enabled) "dark" else "light").apply()
                        }
                    }
                    item { Spacer(Modifier.height(12.dp)) }
                    item { Text("CONTEÚDO E MÍDIA", color = RemoteAppearance.screenSecondaryTextColor, style = MaterialTheme.typography.labelMedium) }
                    item { SettingsLink("Qualidade de vídeo", "Automática", Icons.Default.VideoLibrary) { onNotice("A seleção de qualidade será conectada ao reprodutor.") } }
                    item { SettingSwitch("Reprodução automática", "Reproduzir vídeos automaticamente", autoplay, Icons.Default.PlayCircle) { autoplay = it; prefs.edit().putBoolean("setting_autoplay", it).apply() } }
                    item { SettingSwitch("Economia de dados", "Reduzir o uso de dados móveis", dataSaver, Icons.Default.FileDownload) { dataSaver = it; prefs.edit().putBoolean("setting_data_saver", it).apply() } }
                    item { Spacer(Modifier.height(12.dp)) }
                    item { Text("IDIOMA E REGIÃO", color = RemoteAppearance.screenSecondaryTextColor, style = MaterialTheme.typography.labelMedium) }
                    item { SettingsLink("Idioma", "Português (Brasil)", Icons.Default.Language) { onNotice("O seletor de idiomas ainda não está implementado.") } }
                }
                "font_names", "font_app" -> {
                    item { Text(if (section == "font_names") "Escolha como os nomes aparecem no feed." else "Altera títulos, menus e botões. Não altera mensagens, legendas ou comentários dos usuários.", color = RemoteAppearance.screenSecondaryTextColor, style = MaterialTheme.typography.bodySmall) }
                    val options = listOf("Padrão", "SF Pro (similar)", "Moderna", "Elegante", "Clássica", "Monoespaçada", "Negrito", "Minimalista", "Arredondada", "Tecnológica")
                    items(options) { option ->
                        val selected = if (section == "font_names") nameFontStyle == option else appFontStyle == option
                        Row(Modifier.fillMaxWidth().clickable { if (section == "font_names") onNameFontChange(option) else onAppFontChange(option) }.padding(vertical = 14.dp, horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(if (section == "font_names") "Ana Lima" else "QINBLY", fontFamily = nameFontFamily(option), fontSize = 19.sp, fontWeight = if (option == "Negrito") FontWeight.ExtraBold else FontWeight.Medium, color = RemoteAppearance.screenTextColor, modifier = Modifier.weight(1f))
                            Text(option, color = if (selected) Color.White else Muted, fontSize = 13.sp)
                            Spacer(Modifier.width(12.dp))
                            Icon(if (selected) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked, contentDescription = null, tint = if (selected) Color.White else Muted, modifier = Modifier.size(20.dp))
                        }
                        androidx.compose.material3.HorizontalDivider(color = Color.White.copy(alpha = 0.09f), thickness = 0.5.dp)
                    }
                }
                "feed" -> {
                    item { LocalSettingSwitch("Reproduzir vídeos com som", "setting_feed_reproduzir_videos_com_som", prefs) }
                    item { LocalSettingSwitch("Iniciar vídeos silenciados", "setting_feed_iniciar_videos_silenciados", prefs) }
                    item { LocalSettingSwitch("Repetição automática", "setting_feed_repeticao_automatica", prefs) }
                    item { LocalSettingSwitch("Pré-carregamento de vídeos", "setting_feed_pre_carregamento_de_videos", prefs) }
                    item { LocalSettingSwitch("Reprodução somente por Wi-Fi", "setting_feed_reproducao_somente_por_wi_fi", prefs) }
                    item { LocalSettingSwitch("Ocultar publicações já visualizadas", "setting_feed_ocultar_publicacoes_ja_visualizadas", prefs) }
                    item { LocalSettingSwitch("Mostrar conteúdo sensível", "setting_feed_mostrar_conteudo_sensivel", prefs) }
                    item { SettingsLink("Assuntos de interesse", "Gerenciar assuntos de interesse", Icons.Default.FavoriteBorder) { onNotice("Esta função precisa ser conectada ao serviço correspondente antes de ficar disponível.") } }
                    item { SettingsLink("Palavras e hashtags bloqueadas", "Gerenciar palavras e hashtags bloqueadas", Icons.Default.Block) { onNotice("Esta função precisa ser conectada ao serviço correspondente antes de ficar disponível.") } }
                    item { SettingsLink("Redefinir recomendações", "Gerenciar redefinir recomendações", Icons.Default.Refresh) { onNotice("Esta função precisa ser conectada ao serviço correspondente antes de ficar disponível.") } }
                    item { SettingsLink("Histórico de vídeos assistidos", "Gerenciar histórico de vídeos assistidos", Icons.Default.History) { onNotice("Esta função precisa ser conectada ao serviço correspondente antes de ficar disponível.") } }
                }
                "camera_settings" -> {
                    item { SettingsLink("Qualidade das fotos", "Gerenciar qualidade das fotos", Icons.Default.CameraAlt) { onNotice("Esta função precisa ser conectada ao serviço correspondente antes de ficar disponível.") } }
                    item { SettingsLink("Resolução dos vídeos", "Gerenciar resolução dos vídeos", Icons.Default.VideoLibrary) { onNotice("Esta função precisa ser conectada ao serviço correspondente antes de ficar disponível.") } }
                    item { SettingsLink("Taxa de quadros (FPS)", "Gerenciar taxa de quadros (fps)", Icons.Default.VideoLibrary) { onNotice("Esta função precisa ser conectada ao serviço correspondente antes de ficar disponível.") } }
                    item { LocalSettingSwitch("Estabilização de vídeo", "setting_camera_settings_estabilizacao_de_video", prefs) }
                    item { LocalSettingSwitch("Salvar original na galeria", "setting_camera_settings_salvar_original_na_galeria", prefs) }
                    item { LocalSettingSwitch("Salvar rascunhos automaticamente", "setting_camera_settings_salvar_rascunhos_automaticamente", prefs) }
                    item { LocalSettingSwitch("Publicar somente por Wi-Fi", "setting_camera_settings_publicar_somente_por_wi_fi", prefs) }
                    item { SettingsLink("Compressão de mídia", "Gerenciar compressão de mídia", Icons.Default.FileDownload) { onNotice("Esta função precisa ser conectada ao serviço correspondente antes de ficar disponível.") } }
                    item { LocalSettingSwitch("Marca-d'água", "setting_camera_settings_marca_d_agua", prefs) }
                    item { LocalSettingSwitch("Permitir remix de publicações", "setting_camera_settings_permitir_remix_de_publicacoes", prefs) }
                    item { LocalSettingSwitch("Permitir reutilização do áudio", "setting_camera_settings_permitir_reutilizacao_do_audio", prefs) }
                    item { SettingsLink("Público padrão das publicações", "Gerenciar público padrão das publicações", Icons.Default.Visibility) { onNotice("Esta função precisa ser conectada ao serviço correspondente antes de ficar disponível.") } }
                    item { LocalSettingSwitch("Localização nas publicações", "setting_camera_settings_localizacao_nas_publicacoes", prefs) }
                }
                "messages_settings" -> {
                    item { SettingsLink("Quem pode iniciar conversas", "Gerenciar quem pode iniciar conversas", Icons.Default.ChatBubbleOutline) { onNotice("Esta função precisa ser conectada ao serviço correspondente antes de ficar disponível.") } }
                    item { SettingsLink("Solicitações de mensagens", "Gerenciar solicitações de mensagens", Icons.Default.ChatBubbleOutline) { onNotice("Esta função precisa ser conectada ao serviço correspondente antes de ficar disponível.") } }
                    item { LocalSettingSwitch("Confirmação de leitura", "setting_messages_settings_confirmacao_de_leitura", prefs) }
                    item { LocalSettingSwitch("Indicador de digitação", "setting_messages_settings_indicador_de_digitacao", prefs) }
                    item { LocalSettingSwitch("Mostrar status online", "setting_messages_settings_mostrar_status_online", prefs) }
                    item { LocalSettingSwitch("Download automático de fotos", "setting_messages_settings_download_automatico_de_fotos", prefs) }
                    item { LocalSettingSwitch("Download automático de vídeos", "setting_messages_settings_download_automatico_de_videos", prefs) }
                    item { LocalSettingSwitch("Download automático de áudios", "setting_messages_settings_download_automatico_de_audios", prefs) }
                    item { SettingsLink("Silenciar conversas", "Gerenciar silenciar conversas", Icons.Default.NotificationsOff) { onNotice("Esta função precisa ser conectada ao serviço correspondente antes de ficar disponível.") } }
                    item { SettingsLink("Conversas arquivadas", "Gerenciar conversas arquivadas", Icons.Default.Archive) { onNotice("Esta função precisa ser conectada ao serviço correspondente antes de ficar disponível.") } }
                    item { SettingsLink("Gerenciar grupos", "Gerenciar gerenciar grupos", Icons.Default.People) { onNotice("Esta função precisa ser conectada ao serviço correspondente antes de ficar disponível.") } }
                }
                "friends_settings" -> {
                    item { SettingsLink("Lista de seguidores", "Gerenciar lista de seguidores", Icons.Default.People) { onNotice("Esta função precisa ser conectada ao serviço correspondente antes de ficar disponível.") } }
                    item { SettingsLink("Lista de seguindo", "Gerenciar lista de seguindo", Icons.Default.Person) { onNotice("Esta função precisa ser conectada ao serviço correspondente antes de ficar disponível.") } }
                    item { SettingsLink("Solicitações pendentes", "Gerenciar solicitações pendentes", Icons.Default.PersonAdd) { onNotice("Esta função precisa ser conectada ao serviço correspondente antes de ficar disponível.") } }
                    item { SettingsLink("Sugestões de amizade", "Gerenciar sugestões de amizade", Icons.Default.People) { onNotice("Esta função precisa ser conectada ao serviço correspondente antes de ficar disponível.") } }
                    item { LocalSettingSwitch("Sincronizar contatos", "setting_friends_settings_sincronizar_contatos", prefs) }
                    item { SettingsLink("Encontrar amigos", "Gerenciar encontrar amigos", Icons.Default.Search) { onNotice("Esta função precisa ser conectada ao serviço correspondente antes de ficar disponível.") } }
                    item { LocalSettingSwitch("Ocultar lista de seguidores", "setting_friends_settings_ocultar_lista_de_seguidores", prefs) }
                    item { LocalSettingSwitch("Ocultar lista de seguindo", "setting_friends_settings_ocultar_lista_de_seguindo", prefs) }
                    item { SettingsLink("Remover seguidores", "Gerenciar remover seguidores", Icons.Default.PersonRemove) { onNotice("Esta função precisa ser conectada ao serviço correspondente antes de ficar disponível.") } }
                    item { SettingsLink("Amigos próximos", "Gerenciar amigos próximos", Icons.Default.People) { onNotice("Esta função precisa ser conectada ao serviço correspondente antes de ficar disponível.") } }
                    item { SettingsLink("Contas favoritas", "Gerenciar contas favoritas", Icons.Default.FavoriteBorder) { onNotice("Esta função precisa ser conectada ao serviço correspondente antes de ficar disponível.") } }
                    item { SettingsLink("Contas silenciadas", "Gerenciar contas silenciadas", Icons.Default.NotificationsOff) { onNotice("Esta função precisa ser conectada ao serviço correspondente antes de ficar disponível.") } }
                }
                "accessibility" -> {
                    item { SettingsLink("Tamanho do texto", "Gerenciar tamanho do texto", Icons.Default.TextFields) { onNotice("Esta função precisa ser conectada ao serviço correspondente antes de ficar disponível.") } }
                    item { LocalSettingSwitch("Alto contraste", "setting_accessibility_alto_contraste", prefs) }
                    item { LocalSettingSwitch("Reduzir movimento", "setting_accessibility_reduzir_movimento", prefs) }
                    item { LocalSettingSwitch("Reduzir efeitos visuais", "setting_accessibility_reduzir_efeitos_visuais", prefs) }
                    item { SettingsLink("Compatibilidade com leitores de tela", "Gerenciar compatibilidade com leitores de tela", Icons.Default.Visibility) { onNotice("Esta função precisa ser conectada ao serviço correspondente antes de ficar disponível.") } }
                    item { LocalSettingSwitch("Descrição de imagens", "setting_accessibility_descricao_de_imagens", prefs) }
                    item { LocalSettingSwitch("Legendas automáticas", "setting_accessibility_legendas_automaticas", prefs) }
                    item { SettingsLink("Tamanho das legendas", "Gerenciar tamanho das legendas", Icons.Default.TextFields) { onNotice("Esta função precisa ser conectada ao serviço correspondente antes de ficar disponível.") } }
                    item { SettingsLink("Velocidade de reprodução", "Gerenciar velocidade de reprodução", Icons.Default.PlayCircle) { onNotice("Esta função precisa ser conectada ao serviço correspondente antes de ficar disponível.") } }
                    item { LocalSettingSwitch("Controles maiores", "setting_accessibility_controles_maiores", prefs) }
                    item { LocalSettingSwitch("Feedback tátil", "setting_accessibility_feedback_tatil", prefs) }
                }
                "moderation" -> {
                    item { LocalSettingSwitch("Filtrar comentários ofensivos", "setting_moderation_filtrar_comentarios_ofensivos", prefs) }
                    item { SettingsLink("Palavras bloqueadas", "Gerenciar palavras bloqueadas", Icons.Default.Block) { onNotice("Esta função precisa ser conectada ao serviço correspondente antes de ficar disponível.") } }
                    item { LocalSettingSwitch("Filtrar mensagens suspeitas", "setting_moderation_filtrar_mensagens_suspeitas", prefs) }
                    item { LocalSettingSwitch("Controle de conteúdo sensível", "setting_moderation_controle_de_conteudo_sensivel", prefs) }
                    item { SettingsLink("Denunciar publicação", "Gerenciar denunciar publicação", Icons.Default.Flag) { onNotice("Esta função precisa ser conectada ao serviço correspondente antes de ficar disponível.") } }
                    item { SettingsLink("Histórico das denúncias", "Gerenciar histórico das denúncias", Icons.Default.History) { onNotice("Esta função precisa ser conectada ao serviço correspondente antes de ficar disponível.") } }
                    item { SettingsLink("Usuários bloqueados", "Gerenciar usuários bloqueados", Icons.Default.Block) { onNotice("Esta função precisa ser conectada ao serviço correspondente antes de ficar disponível.") } }
                    item { LocalSettingSwitch("Restringir interações", "setting_moderation_restringir_interacoes", prefs) }
                }
                "activity_history" -> {
                    item { SettingsLink("Minhas publicações", "Veja e gerencie suas publicações", Icons.Default.VideoLibrary) { onNotice("Esta função precisa ser conectada ao serviço correspondente antes de ficar disponível.") } }
                    item { SettingsLink("Comentários feitos", "Veja seus comentários", Icons.Default.ChatBubbleOutline) { onNotice("Esta função precisa ser conectada ao serviço correspondente antes de ficar disponível.") } }
                    item { SettingsLink("Curtidas", "Veja o que você curtiu", Icons.Default.FavoriteBorder) { onNotice("Esta função precisa ser conectada ao serviço correspondente antes de ficar disponível.") } }
                    item { SettingsLink("Publicações compartilhadas", "Veja o que você compartilhou", Icons.Default.Share) { onNotice("Esta função precisa ser conectada ao serviço correspondente antes de ficar disponível.") } }
                    item { SettingsLink("Publicações salvas", "Veja suas publicações salvas", Icons.Default.BookmarkBorder) { onNotice("Esta função precisa ser conectada ao serviço correspondente antes de ficar disponível.") } }
                    item { SettingsLink("Histórico de visualizações", "Veja os vídeos que você assistiu", Icons.Default.History) { onNotice("Esta função precisa ser conectada ao serviço correspondente antes de ficar disponível.") } }
                    item { SettingsLink("Histórico de pesquisas", "Veja suas pesquisas recentes", Icons.Default.Search) { onNotice("Esta função precisa ser conectada ao serviço correspondente antes de ficar disponível.") } }
                    item { SettingsLink("Estatísticas do perfil", "Veja o desempenho do seu perfil", Icons.Default.BarChart) { onNotice("Esta função precisa ser conectada ao serviço correspondente antes de ficar disponível.") } }
                }
                "account_management" -> {
                    item { SettingsLink("Alternar entre contas", "Gerenciar alternar entre contas", Icons.Default.Settings) { onSwitchAccounts() } }
                    item { SettingsLink("Adicionar outra conta", "Gerenciar adicionar outra conta", Icons.Default.PersonAdd) { onAuth() } }
                    item { SettingsLink("Gerenciar contas salvas", "Gerenciar gerenciar contas salvas", Icons.Default.Settings) { onSwitchAccounts() } }
                    item { SettingsLink("Baixar dados da conta", "Gerenciar baixar dados da conta", Icons.Default.Settings) { exportAccountData() } }
                    item { SettingsLink("Desativar ou excluir conta", "Gerenciar desativar ou excluir conta", Icons.Default.DeleteForever) { onNotice("Esta função precisa ser conectada ao serviço correspondente antes de ficar disponível.") } }
                    item { SettingsLink("Sair", "Gerenciar sair", Icons.Default.Settings) { onLogout() } }
                }
                "content" -> {
                    item { SettingSwitch("Comentários", "Permitir comentários nas suas publicações (salvo neste aparelho)", contentComments, Icons.Default.ChatBubbleOutline) { contentComments = it; prefs.edit().putBoolean("setting_content_comments", it).apply() } }
                    item { SettingSwitch("Menções", "Permitir menções ao seu perfil (salvo neste aparelho)", contentMentions, Icons.Default.AlternateEmail) { contentMentions = it; prefs.edit().putBoolean("setting_content_mentions", it).apply() } }
                    item { SettingSwitch("Mensagens diretas", "Preferência local para mensagens diretas", contentDirectMessages, Icons.Default.ChatBubbleOutline) { contentDirectMessages = it; prefs.edit().putBoolean("setting_content_dm", it).apply() } }
                    item { SettingSwitch("Reutilização de conteúdo", "Permitir reutilização (preferência local)", contentReuse, Icons.Default.VideoLibrary) { contentReuse = it; prefs.edit().putBoolean("setting_content_reuse", it).apply() } }
                    item { SettingSwitch("Exibir perfil ao compartilhar links", "Preferência local de visibilidade do perfil", contentShareProfile, Icons.Default.Share) { contentShareProfile = it; prefs.edit().putBoolean("setting_content_share_profile", it).apply() } }
                    item { SettingSwitch("Lista de usuários seguidos", "Permitir exibir a lista (preferência local)", contentFollowing, Icons.Default.Person) { contentFollowing = it; prefs.edit().putBoolean("setting_content_following", it).apply() } }
                    item { SettingSwitch("Vídeos curtidos", "Permitir exibir vídeos curtidos (preferência local)", contentLikedVideos, Icons.Default.FavoriteBorder) { contentLikedVideos = it; prefs.edit().putBoolean("setting_content_liked", it).apply() } }
                    item { SettingSwitch("Espectadores", "Preferência local de visualização de espectadores", contentViewers, Icons.Default.Visibility) { contentViewers = it; prefs.edit().putBoolean("setting_content_viewers", it).apply() } }
                }
                "lives" -> {
                    item { SettingsLink("Integrações de live", "Conecte serviços e provedores de transmissão", Icons.Default.Radio) { onNotice("As transmissões reais ainda exigem configurar e testar o servidor de mídia.") } }
                    item { SettingsLink("Qualidade da transmissão", "Ajustes de qualidade e conexão", Icons.Default.VideoLibrary) { onNotice("A qualidade será configurável quando o provedor estiver integrado.") } }
                    item { SettingsLink("Interações na live", "Comentários, presentes e moderação", Icons.Default.ChatBubbleOutline) { onNotice("As interações serão habilitadas junto com o backend de streaming.") } }
                }
                "storage" -> {
                    item { SettingsLink("Downloads", "Gerencie downloads e arquivos salvos", Icons.Default.FileDownload) { onNotice("O gerenciador de downloads ainda será conectado.") } }
                    item { SettingsLink("Limpar cache", "Apagar arquivos temporários do aplicativo", Icons.Default.DeleteForever) { Thread { val ok = runCatching { context.cacheDir.deleteRecursively(); context.codeCacheDir.deleteRecursively(); true }.getOrDefault(false); android.os.Handler(android.os.Looper.getMainLooper()).post { onNotice(if (ok) "Cache temporário limpo." else "Não foi possível limpar todo o cache.") } }.start() } }
                    item { SettingsLink("Baixar seus dados", "Exportar seus dados da conta", Icons.Default.FileDownload) { exportAccountData() } }
                }
                else -> {
                    item { SettingsLink("Central de ajuda", "Endereço do site em configuração", Icons.Default.HelpOutline) { onNotice("O endereço oficial da central de ajuda do QINBLY ainda não foi configurado.") } }
                    item { SettingsLink("Reportar um problema", "Enviar um relato por e-mail ou aplicativo", Icons.Default.Info) { try { val report = Intent(Intent.ACTION_SENDTO).apply { data = android.net.Uri.parse("mailto:"); putExtra(Intent.EXTRA_SUBJECT, "Relato de problema — QINBLY"); putExtra(Intent.EXTRA_TEXT, "Descreva o problema encontrado:\n\nVersão: 0.3.0\nDispositivo: " + android.os.Build.MANUFACTURER + " " + android.os.Build.MODEL) }; context.startActivity(Intent.createChooser(report, "Reportar problema")) } catch (_: Exception) { onNotice("Não há aplicativo de e-mail disponível neste aparelho.") } } }
                    item { SettingsLink("Sobre o QINBLY", "Versão 0.3.0 · Termos e privacidade", Icons.Default.Info) { onNotice("QINBLY 0.3.0. Os links de termos e privacidade precisam ser publicados antes do lançamento.") } }
                    item { SettingsLink("Atualizações do aplicativo", "Verificar e baixar o APK mais recente", Icons.Default.FileDownload) { context.startActivity(Intent(context, RemoteUiActivity::class.java)) } }
                }
            }
        }
    }
}
private fun settingOptionIcon(title: String): androidx.compose.ui.graphics.vector.ImageVector = when {
    title.contains("vídeo", true) || title.contains("reprodução", true) || title.contains("áudio", true) -> Icons.Default.PlayCircle
    title.contains("foto", true) || title.contains("câmera", true) || title.contains("galeria", true) -> Icons.Default.CameraAlt
    title.contains("mensag", true) || title.contains("coment", true) || title.contains("digitação", true) -> Icons.Default.ChatBubbleOutline
    title.contains("seguidor", true) || title.contains("contato", true) || title.contains("amigo", true) -> Icons.Default.People
    title.contains("download", true) || title.contains("dados", true) || title.contains("Wi-Fi", true) -> Icons.Default.FileDownload
    title.contains("legenda", true) || title.contains("texto", true) -> Icons.Default.TextFields
    title.contains("notifica", true) || title.contains("silenci", true) -> Icons.Default.Notifications
    title.contains("privad", true) || title.contains("bloque", true) || title.contains("sensível", true) -> Icons.Default.Lock
    title.contains("compartilh", true) || title.contains("remix", true) -> Icons.Default.Share
    title.contains("movimento", true) || title.contains("efeitos", true) -> Icons.Default.Visibility
    title.contains("rascunho", true) || title.contains("salvar", true) -> Icons.Default.BookmarkBorder
    else -> Icons.Default.Settings
}

@Composable
private fun LocalSettingSwitch(title: String, key: String, prefs: android.content.SharedPreferences) {
    var checked by remember(key) { mutableStateOf(prefs.getBoolean(key, false)) }
    SettingSwitch(title, "Preferência salva neste aparelho", checked, settingOptionIcon(title)) {
        checked = it
        prefs.edit().putBoolean(key, it).apply()
    }
}

@Composable
private fun SettingsLink(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
                .padding(horizontal = 8.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = Color.White.copy(alpha = 0.78f), modifier = Modifier.size(22.dp))
            Column(Modifier.weight(1f).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(title, fontWeight = FontWeight.Medium)
                Text(subtitle, color = Muted, style = MaterialTheme.typography.bodySmall)
            }
            Text("›", color = Muted, style = MaterialTheme.typography.headlineMedium)
        }
        androidx.compose.material3.HorizontalDivider(color = Color.White.copy(alpha = 0.09f), thickness = 0.5.dp)
    }
}

@Composable
private fun InfoPanel(title: String, message: String) {
    Surface(color = Color(0xFF1A2035), shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(message, color = Muted, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun SettingSwitch(title: String, description: String, checked: Boolean, icon: androidx.compose.ui.graphics.vector.ImageVector, onChange: (Boolean) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = Color.White.copy(alpha = 0.78f), modifier = Modifier.size(22.dp))
            Column(Modifier.weight(1f).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(title, fontWeight = FontWeight.Medium)
                Text(description, color = Muted, style = MaterialTheme.typography.bodySmall)
            }
            Switch(checked = checked, onCheckedChange = onChange, colors = androidx.compose.material3.SwitchDefaults.colors(checkedTrackColor = Color(0xFF168CFA), checkedThumbColor = Color.White, uncheckedTrackColor = Color(0xFF363B44)))
        }
        androidx.compose.material3.HorizontalDivider(color = Color.White.copy(alpha = 0.09f), thickness = 0.5.dp)
    }
}

private fun saveProfilePhotoToDevice(context: android.content.Context, imageUrl: String, onResult: (String) -> Unit) {
    Thread {
        val message = try {
            val request = android.app.DownloadManager.Request(android.net.Uri.parse(imageUrl))
                .setTitle("Foto do perfil QINBLY")
                .setNotificationVisibility(android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(android.os.Environment.DIRECTORY_DOWNLOADS, "QINBLY-perfil-" + System.currentTimeMillis() + ".jpg")
            (context.getSystemService(android.content.Context.DOWNLOAD_SERVICE) as android.app.DownloadManager).enqueue(request)
            "Download da foto iniciado."
        } catch (e: Exception) { e.message ?: "Não foi possível salvar a foto." }
        android.os.Handler(android.os.Looper.getMainLooper()).post { onResult(message) }
    }.start()
}

@Composable
private fun PublicProfilePage(username: String, onBack: () -> Unit, onMessage: (String) -> Unit, onNotice: (String) -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { context.getSharedPreferences("vybora_session", android.content.Context.MODE_PRIVATE) }
    var user by remember(username) { mutableStateOf(org.json.JSONObject()) }
    var loading by remember(username) { mutableStateOf(true) }
    var profileLoadError by remember(username) { mutableStateOf<String?>(null) }
    var publicFrame by remember(username) { mutableIntStateOf(-1) }
    var isFollowing by remember(username) { mutableStateOf(false) }
    var selectedTab by remember(username) { mutableStateOf("Publicações") }
    var expandedImage by remember { mutableStateOf<String?>(null) }
    var photoMenuOpen by remember { mutableStateOf(false) }
    var viewingPublication by remember { mutableStateOf(false) }
    var selectedPublicationCaption by remember { mutableStateOf("") }
    var selectedPublication by remember { mutableStateOf<VyboraApi.PostSummary?>(null) }
    var postLiked by remember { mutableStateOf(false) }
    var commentsOpen by remember { mutableStateOf(false) }
    var commentsLoading by remember { mutableStateOf(false) }
    var postComments by remember { mutableStateOf<List<VyboraApi.CommentSummary>>(emptyList()) }
    var commentDraft by remember { mutableStateOf("") }
    var savedIds by remember { mutableStateOf<Set<String>>(emptySet()) }

    var publicationComment by remember { mutableStateOf("") }
    var showMoreOptions by remember { mutableStateOf(false) }
    var profilePosts by remember(username) { mutableStateOf<List<VyboraApi.PostSummary>>(emptyList()) }
    val displayName = user.optString("displayName").ifBlank { user.optString("display_name").ifBlank { username.removePrefix("@") } }
    val publicUsername = user.optString("username").ifBlank { username.removePrefix("@") }
    val avatarUrl = user.optString("avatar_url").ifBlank { user.optString("avatarUrl").ifBlank { user.optString("profile_photo_url").ifBlank { user.optString("profilePhotoUrl") } } }
    val profilePhotoUrl = user.optString("coverUrl").ifBlank { user.optString("cover_url").ifBlank { user.optString("profilePhotoUrl").ifBlank { avatarUrl } } }
    val bio = user.optString("bio")
    LaunchedEffect(username) {
        loading = true
        profileLoadError = null
        Thread {
            try {
                val response = VyboraApi.getPublicProfile(username, prefs.getString("token", null))
                val found = response.optJSONObject("user") ?: response.optJSONObject("profile") ?: response
                val resolvedUsername = found.optString("username").ifBlank { username.removePrefix("@") }
                // O endpoint de perfil já devolve TODAS as publicações públicas do usuário.
                // Não buscar somente os 30 itens mais recentes do feed global.
                val responsePosts = response.optJSONArray("posts") ?: org.json.JSONArray()
                val posts = (0 until responsePosts.length()).mapNotNull { index ->
                    val post = responsePosts.optJSONObject(index) ?: return@mapNotNull null
                    val author = post.optJSONObject("author") ?: org.json.JSONObject()
                    VyboraApi.PostSummary(
                        id = post.optString("id"), caption = post.optString("caption"),
                        mediaUrl = post.optString("mediaUrl"), mediaType = post.optString("mediaType"),
                        username = author.optString("username").ifBlank { resolvedUsername },
                        displayName = author.optString("displayName").ifBlank { found.optString("displayName") },
                        likes = post.optInt("likes"), comments = post.optInt("comments"),
                        avatarUrl = author.optString("avatarUrl").ifBlank { found.optString("avatarUrl") },
                        createdAt = post.optString("createdAt")
                    )
                }
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    user = found
                    profileLoadError = null
                    publicFrame = found.optInt("profileFrame", -1)
                    isFollowing = found.optBoolean("isFollowing")
                    profilePosts = posts
                    loading = false
                }
            } catch (e: Exception) {
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    loading = false
                    profileLoadError = e.message ?: "Não foi possível carregar os dados do perfil."
                }
            }
        }.start()
    }
    Column(Modifier.fillMaxSize().background(RemoteAppearance.screenBackgroundColor).verticalScroll(rememberScrollState())) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = RemoteAppearance.screenTextColor) }
            Spacer(Modifier.weight(1f))
            Text("QINBLY", color = RemoteAppearance.screenTextColor, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            IconButton(onClick = { showMoreOptions = true }) { Icon(Icons.Default.MoreHoriz, contentDescription = "Opções deste perfil", tint = RemoteAppearance.screenTextColor) }
        }
        Box(Modifier.fillMaxWidth().padding(top = 6.dp, bottom = 8.dp), contentAlignment = Alignment.Center) {
            Box(Modifier.size(225.dp), contentAlignment = Alignment.Center) {
                Box(Modifier.size(if (publicFrame >= 0) 185.dp else 205.dp).clip(CircleShape).background(QiblyTheme.card).clickable {
                    if (profilePhotoUrl.isNotBlank()) { expandedImage = profilePhotoUrl; viewingPublication = false }
                }) {
                    if (profilePhotoUrl.isNotBlank()) AsyncImage(model = profilePhotoUrl, contentDescription = "Foto de $displayName", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    else Icon(Icons.Default.Person, contentDescription = "Sem foto", tint = RemoteAppearance.screenSecondaryTextColor, modifier = Modifier.fillMaxSize().padding(34.dp))
                }
                if (publicFrame in 6..14 && publicFrame != 8) QiblyAnimatedFrame(publicFrame, 225.dp)
            }
        }
        Text(displayName, modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center, color = RemoteAppearance.screenTextColor, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("@$publicUsername", modifier = Modifier.fillMaxWidth().padding(top = 2.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center, color = RemoteAppearance.screenSecondaryTextColor, style = MaterialTheme.typography.bodyMedium)
        val publicCategories = bio.lineSequence().firstOrNull { it.startsWith("✦ ") }?.removePrefix("✦ ").orEmpty()
        val publicCountry = bio.lineSequence().firstOrNull { it.startsWith("🌍 ") }?.removePrefix("🌍 ").orEmpty()
        val publicDescription = bio.lineSequence().filterNot { it.startsWith("✦ ") || it.startsWith("🌍 ") }.joinToString("\n").trim()
        if (publicCategories.isNotBlank()) Text(publicCategories, modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center, color = RemoteAppearance.screenTextColor)
        if (publicDescription.isNotBlank()) Text(publicDescription, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 3.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center, color = RemoteAppearance.screenTextColor)
        val joinedDate = listOf("createdAt", "created_at", "joined_at").map { user.optString(it) }.firstOrNull { it.matches(Regex("\\d{4}-\\d{2}.*")) }.orEmpty()
        val joinMonth = joinedDate.drop(5).take(2).toIntOrNull() ?: 0
        val monthNames = listOf("jan", "fev", "mar", "abr", "mai", "jun", "jul", "ago", "set", "out", "nov", "dez")
        Row(Modifier.fillMaxWidth().padding(top = 9.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            if (publicCountry.isNotBlank()) {
                Icon(Icons.Default.LocationOn, contentDescription = "País", tint = RemoteAppearance.screenSecondaryTextColor, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(5.dp))
                Text(publicCountry, color = RemoteAppearance.screenSecondaryTextColor, style = MaterialTheme.typography.bodySmall)
            }
            if (publicCountry.isNotBlank() && joinMonth in 1..12) Spacer(Modifier.width(18.dp))
            if (joinMonth in 1..12) {
                Icon(Icons.Default.CalendarToday, contentDescription = "Data de entrada", tint = RemoteAppearance.screenSecondaryTextColor, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(5.dp))
                Text("Entrou em ${monthNames[joinMonth - 1]} ${joinedDate.take(4)}", color = RemoteAppearance.screenSecondaryTextColor, style = MaterialTheme.typography.bodySmall)
            }
        }
        Row(Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            ProfileStat("Seguindo", user.optInt("following").toString(), Icons.Default.PersonAddAlt1) { onNotice("Lista de seguindo em preparação.") }
            ProfileStat("Seguidores", user.optInt("followers").toString(), Icons.Default.Groups) { onNotice("Lista de seguidores em preparação.") }
        }
        Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                val token = prefs.getString("token", null)
                val targetId = user.optString("id").ifBlank { user.optString("userId") }.ifBlank { user.optString("user_id") }
                if (token.isNullOrBlank()) onNotice("Entre na sua conta para seguir pessoas.")
                else if (targetId.isBlank()) onNotice("O identificador deste perfil não foi carregado.")
                else Thread {
                    try {
                        val following = VyboraApi.toggleFollow(token, targetId)
                        android.os.Handler(android.os.Looper.getMainLooper()).post {
                            isFollowing = following
                            onNotice(if (following) "Agora você segue @$publicUsername." else "Você deixou de seguir @$publicUsername.")
                        }
                    } catch (e: Exception) {
                        android.os.Handler(android.os.Looper.getMainLooper()).post { onNotice(e.message ?: "Não foi possível atualizar o seguimento.") }
                    }
                }.start()
            }, modifier = Modifier.weight(1f).height(46.dp), colors = ButtonDefaults.buttonColors(containerColor = if (isFollowing) Color(0xFFF1F5F9) else Color(0xFF1677E8), contentColor = if (isFollowing) Color(0xFF1677E8) else Color.White)) {
                Icon(if (isFollowing) Icons.Default.CheckCircle else Icons.Default.PersonAddAlt1, contentDescription = null, tint = if (isFollowing) Color(0xFF1677E8) else Color.White, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(if (isFollowing) "Seguindo" else "Seguir", color = if (isFollowing) Color(0xFF1677E8) else Color.White, fontWeight = FontWeight.Bold)
            }
            OutlinedButton(onClick = {
                onMessage(publicUsername)
            }, modifier = Modifier.weight(0.88f).height(46.dp), border = BorderStroke(1.dp, Color(0xFF1677E8))) {
                Icon(Icons.Default.ChatBubbleOutline, contentDescription = null, modifier = Modifier.size(17.dp), tint = Color(0xFF1677E8))
                Spacer(Modifier.width(6.dp))
                Text("Mensagem", color = Color(0xFF1677E8))
            }
        }
        Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(0.dp)) {
            listOf("Publicações", "Vídeos", "Fotos", "Republicações", "Curtidas").forEach { section ->
                Column(Modifier.weight(1f).clickable { selectedTab = section }.padding(vertical = 7.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(imageVector = when (section) {
                        "Vídeos" -> Icons.Default.PlayCircle
                        "Fotos" -> Icons.Default.Image
                        "Republicações" -> Icons.Default.Repeat
                        "Curtidas" -> Icons.Default.FavoriteBorder
                        "Salvos" -> Icons.Default.BookmarkBorder
                        else -> Icons.Default.GridView
                    }, contentDescription = section, tint = if (selectedTab == section) Color(0xFF1677E8) else RemoteAppearance.screenSecondaryTextColor, modifier = Modifier.size(25.dp))
                    Text(section, color = if (selectedTab == section) Color(0xFF1677E8) else RemoteAppearance.screenTextColor, fontWeight = if (selectedTab == section) FontWeight.Bold else FontWeight.Normal, style = MaterialTheme.typography.labelSmall, maxLines = 1)
                    Box(Modifier.fillMaxWidth().height(3.dp).background(if (selectedTab == section) Brush.horizontalGradient(listOf(Color(0xFF1677E8), Color(0xFF1677E8))) else Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))))
                }
            }
        }
        if (loading) {
            Box(Modifier.fillMaxWidth().padding(30.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Pink) }
        } else if (profileLoadError != null) {
            Text("Não foi possível carregar o perfil: ${profileLoadError}", modifier = Modifier.fillMaxWidth().padding(24.dp), color = RemoteAppearance.screenTextColor, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        } else if (selectedTab !in listOf("Publicações", "Vídeos", "Fotos")) {
            Box(Modifier.fillMaxWidth().padding(28.dp), contentAlignment = Alignment.Center) {
                Text(when (selectedTab) {
                    "Vídeos" -> "Vídeos de $displayName"
                    "Republicações" -> "Republicações de $displayName"
                    "Curtidas" -> "Publicações curtidas"
                    "Salvos" -> "Publicações salvas por $displayName"
                    else -> "Publicações de $displayName"
                }, color = RemoteAppearance.screenSecondaryTextColor, style = MaterialTheme.typography.bodyMedium)
            }
        } else if (profilePosts.isEmpty()) {
            Box(Modifier.fillMaxWidth().padding(28.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.GridView, contentDescription = null, tint = Violet, modifier = Modifier.size(34.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("Nenhuma publicação pública encontrada nesta conta", color = RemoteAppearance.screenTextColor, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                profilePosts.filter { selectedTab != "Vídeos" || it.mediaType == "video" }.filter { selectedTab != "Fotos" || it.mediaType == "image" }.chunked(3).forEach { rowPosts ->                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        rowPosts.forEach { post ->
                            Box(Modifier.weight(1f).height(145.dp).clip(RoundedCornerShape(7.dp)).background(Color(0xFF111426)).clickable {
                                if (post.mediaUrl.isNotBlank()) expandedImage = post.mediaUrl; selectedPublicationCaption = post.caption; selectedPublication = post; postLiked = false; savedIds = prefs.getStringSet("saved_post_ids", emptySet())?.toSet() ?: emptySet(); viewingPublication = true
                            }) {
                                if (post.mediaUrl.isNotBlank()) {
                                    AsyncImage(model = post.mediaUrl, contentDescription = "Publicação de $displayName", modifier = Modifier.fillMaxSize(), contentScale = androidx.compose.ui.layout.ContentScale.Crop)
                                } else {
                                    Text(post.caption.ifBlank { "QINBLY" }, color = RemoteAppearance.screenTextColor, modifier = Modifier.align(Alignment.Center).padding(8.dp), maxLines = 4)
                                }
                                if (post.mediaType == "video") Icon(Icons.Default.PlayCircle, contentDescription = "Vídeo", tint = RemoteAppearance.screenTextColor, modifier = Modifier.align(Alignment.TopEnd).padding(5.dp))
                            }
                        }
                        repeat(3 - rowPosts.size) { Spacer(Modifier.weight(1f).height(145.dp)) }
                    }
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
    val likeSelectedPublication: () -> Unit = {
        val post = selectedPublication
        val token = prefs.getString("token", null)
        if (post == null) onNotice("Publicação indisponível.")
        else if (token.isNullOrBlank()) onNotice("Entre na conta para curtir.")
        else if (!postLiked) Thread {
            try {
                val liked = VyboraApi.toggleLike(token, post.id)
                android.os.Handler(android.os.Looper.getMainLooper()).post { postLiked = liked }
            } catch (e: Exception) {
                android.os.Handler(android.os.Looper.getMainLooper()).post { onNotice(e.message ?: "Erro ao curtir.") }
            }
        }.start()
    }
    if (expandedImage != null) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { expandedImage = null },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Column(Modifier.fillMaxSize().background(Color(0xFF101014)).navigationBarsPadding().padding(start = 12.dp, end = 12.dp, top = 10.dp, bottom = 24.dp)) {
                Row(Modifier.fillMaxWidth().height(48.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { expandedImage = null }) { Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = Color.White) }
                    Spacer(Modifier.weight(1f))
                    if (viewingPublication) Text("Publicação", color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.weight(1f))
                    Box {
                        IconButton(onClick = { photoMenuOpen = true }) { Icon(Icons.Default.MoreVert, contentDescription = "Opções", tint = Color.White) }
                        DropdownMenu(expanded = photoMenuOpen, onDismissRequest = { photoMenuOpen = false }) {
                            DropdownMenuItem(text = { Text("Salvar no celular") }, onClick = {
                                photoMenuOpen = false
                                saveProfilePhotoToDevice(context, expandedImage.orEmpty(), onNotice)
                            })
                            DropdownMenuItem(text = { Text(if (viewingPublication) "Denunciar publicação" else "Denunciar foto") }, onClick = {
                                photoMenuOpen = false
                                onNotice("Denúncia ainda não conectada. Nenhuma denúncia foi enviada.")
                            })
                        }
                    }
                }
                Spacer(Modifier.weight(1f))
                // A foto usa a proporção natural; não esticamos a imagem para preencher a tela.
                // O limite vertical reserva espaço para legenda e ações, sem barras pretas artificiais.
                AsyncImage(
                    model = expandedImage.orEmpty(),
                    contentDescription = "Imagem da publicação",
                    modifier = Modifier.fillMaxWidth()
                        .heightIn(max = androidx.compose.ui.platform.LocalConfiguration.current.screenHeightDp.dp * 0.56f)
                        .clip(RoundedCornerShape(12.dp))
                        .pointerInput(viewingPublication, selectedPublication?.id, postLiked) {
                            detectTapGestures(onDoubleTap = {
                                if (viewingPublication) likeSelectedPublication()
                            })
                        },
                    contentScale = androidx.compose.ui.layout.ContentScale.Fit
                )
                if (viewingPublication) {
                    if (selectedPublicationCaption.isNotBlank()) {
                        Text(selectedPublicationCaption, modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp), color = Color.White, style = MaterialTheme.typography.bodyMedium, maxLines = 4)
                    }
                    Row(
                        Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 12.dp).border(1.dp, Color.DarkGray, RoundedCornerShape(28.dp)).padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = likeSelectedPublication) {
                            Icon(if (postLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder, contentDescription = "Curtir", tint = if (postLiked) Color.Red else Color.White)
                        }
                        IconButton(onClick = { val post = selectedPublication
                            if (post != null) {
                                commentsOpen = true
                                commentsLoading = true
                                Thread {
                                    try {
                                        val loaded = VyboraApi.getComments(post.id)
                                        android.os.Handler(android.os.Looper.getMainLooper()).post { postComments = loaded; commentsLoading = false }
                                    } catch (e: Exception) {
                                        android.os.Handler(android.os.Looper.getMainLooper()).post { commentsLoading = false; onNotice(e.message ?: "Erro ao carregar comentários.") }
                                    }
                                }.start()
                            } }) {
                            Icon(Icons.Default.ChatBubbleOutline, contentDescription = "Comentários", tint = Color.White)
                        }
                        IconButton(onClick = { val post = selectedPublication
                            if (post != null) {
                                val share = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(android.content.Intent.EXTRA_TEXT, post.caption + "\\n" + post.mediaUrl)
                                }
                                context.startActivity(android.content.Intent.createChooser(share, "Compartilhar publicação"))
                            } }) {
                            Icon(Icons.Default.Send, contentDescription = "Compartilhar", tint = Color.White)
                        }
                        IconButton(onClick = { val post = selectedPublication
                            if (post != null) {
                                val wasSaved = post.id in savedIds
                                savedIds = if (wasSaved) savedIds - post.id else savedIds + post.id
                                prefs.edit().putStringSet("saved_post_ids", savedIds).apply()
                                onNotice(if (wasSaved) "Publicação removida dos salvos." else "Publicação salva.")
                            } }) {
                            Icon(if ((selectedPublication?.id?.let { it in savedIds } == true)) Icons.Default.Bookmark else Icons.Default.BookmarkBorder, contentDescription = "Salvar publicação", tint = Color(0xFFFFD54F))
                        }
                    }
                }
                Spacer(Modifier.weight(1f))
            }
        }
    }
    if (commentsOpen) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { commentsOpen = false },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
        ) {
            BoxWithConstraints(
                Modifier.imePadding().fillMaxSize().background(Color.Black.copy(alpha = 0.58f)),
                contentAlignment = Alignment.BottomCenter
            ) {
                // Mesma altura e medição do painel de comentários do feed.
                val dialogKeyboardInset = with(androidx.compose.ui.platform.LocalDensity.current) { WindowInsets.ime.getBottom(this).toDp() }
                val dialogKeyboardVisible = dialogKeyboardInset > 120.dp
                val panelHeight = maxHeight * if (dialogKeyboardVisible) 0.90f else 0.90f
                Column(
                    Modifier.fillMaxWidth().height(panelHeight).offset(y = (-5).dp)
                        .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    Box(Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 8.dp), contentAlignment = Alignment.Center) {
                        Box(Modifier.width(48.dp).height(5.dp).clip(CircleShape).background(Color(0xFF777887)))
                    }
                    Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("Comentários", fontSize = 25.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        IconButton(onClick = { commentsOpen = false }) { Icon(Icons.Default.Close, contentDescription = "Fechar comentários") }
                    }
                    HorizontalDivider()
                    if (commentsLoading) {
                        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                    } else {
                        LazyColumn(Modifier.weight(1f).fillMaxWidth(), contentPadding = PaddingValues(16.dp)) {
                            if (postComments.isEmpty()) item {
                                Text("Seja a primeira pessoa a comentar.", modifier = Modifier.padding(vertical = 24.dp))
                            }
                            items(postComments) { comment ->
                                Column(Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
                                    Text(comment.displayName.ifBlank { comment.username }, fontWeight = FontWeight.Bold)
                                    Text(comment.body, modifier = Modifier.padding(top = 5.dp))
                                }
                                HorizontalDivider()
                            }
                        }
                    }
                    Column(Modifier.fillMaxWidth().padding(bottom = if (dialogKeyboardVisible) 0.dp else 80.dp)) {
                    Row(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant).padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("😊", fontSize = 28.sp, modifier = Modifier.padding(end = 10.dp).clickable { commentDraft += "😊" })
                        OutlinedTextField(
                            value = commentDraft,
                            onValueChange = { commentDraft = it },
                            placeholder = { Text("Adicione um comentário...") },
                            shape = RoundedCornerShape(28.dp),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        TextButton(onClick = {
                            val post = selectedPublication
                            val token = prefs.getString("token", null)
                            val draft = commentDraft.trim()
                            if (post == null || token.isNullOrBlank()) onNotice("Entre na conta para comentar.")
                            else if (draft.isNotBlank() && !commentsLoading) {
                                commentsLoading = true
                                Thread {
                                    try {
                                        VyboraApi.addComment(token, post.id, draft)
                                        val loaded = VyboraApi.getComments(post.id)
                                        android.os.Handler(android.os.Looper.getMainLooper()).post {
                                            postComments = loaded
                                            commentDraft = ""
                                            commentsLoading = false
                                        }
                                    } catch (e: Exception) {
                                        android.os.Handler(android.os.Looper.getMainLooper()).post {
                                            commentsLoading = false
                                            onNotice(e.message ?: "Erro ao comentar.")
                                        }
                                    }
                                }.start()
                            }
                        }, enabled = commentDraft.isNotBlank() && !commentsLoading) { Icon(Icons.Default.Send, contentDescription = "Enviar comentário") }
                    }
                    }
                }
            }
        }
    }
    if (showMoreOptions) {
        AlertDialog(
            onDismissRequest = { showMoreOptions = false },
            title = { Text("Perfil de $displayName") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Ações disponíveis para este perfil:", color = RemoteAppearance.screenSecondaryTextColor)
                    TextButton(onClick = {
                        showMoreOptions = false
                        onNotice("Denúncias ainda precisam ser conectadas ao backend; nenhuma denúncia foi enviada.")
                    }, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Pink)
                        Spacer(Modifier.width(8.dp))
                        Text("Denunciar perfil", color = RemoteAppearance.screenTextColor)
                    }
                    TextButton(onClick = {
                        showMoreOptions = false
                        onNotice("Bloqueio ainda precisa ser conectado ao backend; esta conta não foi bloqueada.")
                    }, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Block, contentDescription = null, tint = Pink)
                        Spacer(Modifier.width(8.dp))
                        Text("Bloquear perfil", color = RemoteAppearance.screenTextColor)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                    clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Perfil QINBLY", "@$publicUsername"))
                    android.widget.Toast.makeText(context, "@$publicUsername copiado", android.widget.Toast.LENGTH_SHORT).show()
                    showMoreOptions = false
                }) { Text("Copiar @") }
            },
            dismissButton = {
                TextButton(onClick = {
                    val sendIntent = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, "Perfil de $displayName no QINBLY: @$publicUsername") }
                    context.startActivity(Intent.createChooser(sendIntent, "Compartilhar perfil"))
                    showMoreOptions = false
                }) { Text("Compartilhar") }
            },
            containerColor = Color(0xFF111426),
            titleContentColor = Color.White,
            textContentColor = Muted
        )
    }
}

@Composable
private fun ProfileStat(label: String, count: String, icon: ImageVector, onClick: () -> Unit) {
    Column(Modifier.clip(RoundedCornerShape(12.dp)).clickable(onClick = onClick).padding(horizontal = 8.dp, vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = label, tint = if (label == "Publicações") Pink else Color.White, modifier = Modifier.size(22.dp))
        Spacer(Modifier.height(3.dp))
        Text(count, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = RemoteAppearance.screenTextColor)
        Text(label, style = MaterialTheme.typography.labelSmall, color = RemoteAppearance.screenSecondaryTextColor)
    }
}

private data class VyboraGalleryAsset(val uri: Uri, val isVideo: Boolean, val dateAdded: Long, val name: String)

private fun extractVyboraVideoFrames(context: android.content.Context, uri: Uri): List<Bitmap> {
    val retriever = android.media.MediaMetadataRetriever()
    return try {
        retriever.setDataSource(context, uri)
        val durationMs = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
        if (durationMs <= 0L) emptyList() else {
            (0..9).mapNotNull { index ->
                val timeUs = ((durationMs * 1000L) * index / 9L).coerceIn(0L, durationMs * 1000L)
                retriever.getFrameAtTime(timeUs, android.media.MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
            }
        }
    } catch (_: Exception) {
        emptyList()
    } finally {
        try { retriever.release() } catch (_: Exception) { }
    }
}

@Composable
private fun PostPage(onNotice: (String) -> Unit, onCameraCancelled: () -> Unit, initialMode: String = "camera") {
    val context = androidx.compose.ui.platform.LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val prefs = remember { context.getSharedPreferences("vybora_session", android.content.Context.MODE_PRIVATE) }
    var caption by remember { mutableStateOf("") }
    var pollOptions by remember { mutableStateOf(listOf("", "")) }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var sourceUri by remember { mutableStateOf<Uri?>(null) }
    var selectedType by remember { mutableStateOf("") }
    var publishing by remember { mutableStateOf(false) }
    var cameraGranted by remember { mutableStateOf(androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED) }
    var cameraError by remember { mutableStateOf("") }
    var lensFacing by remember { mutableIntStateOf(androidx.camera.core.CameraSelector.LENS_FACING_BACK) }
    var selectedFilter by remember { mutableStateOf("Original") }
    var selectedEffect by remember { mutableStateOf("Original") }
    var activeCameraPanel by remember { mutableStateOf("") }
    var selectedCameraFormat by remember { mutableStateOf("9:16") }
    var selectedVideoSpeed by remember { mutableStateOf("1×") }
    var selectedTimerSeconds by remember { mutableStateOf("Desligado") }
    var beautyLevel by remember { mutableFloatStateOf(0.35f) }
    var imageCapture by remember { mutableStateOf<androidx.camera.core.ImageCapture?>(null) }
    var videoCapture by remember { mutableStateOf<androidx.camera.video.VideoCapture<androidx.camera.video.Recorder>?>(null) }
    var activeRecording by remember { mutableStateOf<androidx.camera.video.Recording?>(null) }
    var isRecording by remember { mutableStateOf(false) }
    var captureMode by remember { mutableStateOf("Foto") }
    var selectedDuration by remember { mutableStateOf("15 s") }
    var cameraProvider by remember { mutableStateOf<androidx.camera.lifecycle.ProcessCameraProvider?>(null) }
    var previewView by remember { mutableStateOf<androidx.camera.view.PreviewView?>(null) }
    var cameraControl by remember { mutableStateOf<androidx.camera.core.Camera?>(null) }
    var zoomRatio by remember { mutableFloatStateOf(1f) }
    var flashEnabled by remember { mutableStateOf(false) }
    var showComposer by remember { mutableStateOf(false) }
    var publishSettings by remember { mutableStateOf(false) }
    var editorTool by remember { mutableStateOf("Filtros") }
    var allowComments by remember { mutableStateOf(true) }
    var allowSharing by remember { mutableStateOf(true) }
    var saveToDevice by remember { mutableStateOf(false) }
    var editorText by remember { mutableStateOf("") }
    var editorSticker by remember { mutableStateOf("") }
    var editorRatio by remember { mutableStateOf("Original") }
    val editorStrokes = remember { mutableStateListOf<List<Offset>>() }
    var publishSuccess by remember { mutableStateOf(false) }
    var publishError by remember { mutableStateOf("") }
    var selectedMediaReady by remember { mutableStateOf(false) }
    var galleryOpen by remember { mutableStateOf(false) }
    var galleryFilter by remember { mutableStateOf("Todos") }
    var galleryAssets by remember { mutableStateOf<List<VyboraGalleryAsset>>(emptyList()) }
    var galleryLoading by remember { mutableStateOf(false) }
    var videoFrames by remember { mutableStateOf<List<Bitmap>>(emptyList()) }
    val gallerySelection = remember { mutableStateListOf<Uri>() }
    val cameraPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        cameraGranted = granted
        if (!granted) cameraError = "Permita o acesso à câmera para usar a câmera do QINBLY."
    }
    val galleryPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
        val granted = results.values.any { it }
        if (granted) {
            galleryAssets = emptyList()
            galleryOpen = true
        } else {
            onNotice("Permita o acesso às fotos e vídeos para abrir a galeria do QINBLY.")
        }
    }
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) { sourceUri = uri; selectedUri = uri; selectedType = "foto"; showComposer = true }
    }
    val videoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) { sourceUri = uri; selectedUri = uri; showComposer = true; selectedType = "vídeo" }
    }
    // Seletor do Android: não depende de permissão de acesso a toda a biblioteca.
    val mediaPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            sourceUri = uri
            selectedUri = uri
            selectedType = if (context.contentResolver.getType(uri)?.startsWith("video/") == true) "vídeo" else "foto"
            galleryOpen = false
            publishSettings = false
            showComposer = true
        }
    }
    fun openVyboraGallery() {
        try { mediaPicker.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)) }
        catch (e: Exception) { onNotice("Não foi possível abrir a galeria: " + (e.localizedMessage ?: "tente novamente")) }
    }
    LaunchedEffect(initialMode) {
        if (initialMode == "gallery") openVyboraGallery()
    }
    LaunchedEffect(sourceUri) {
        val media = sourceUri
        selectedMediaReady = media != null && kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try { context.contentResolver.openInputStream(media)?.use { it.read() >= 0 } == true } catch (_: Exception) { false }
        }
    }
    LaunchedEffect(showComposer, sourceUri, selectedType) {
        val uri = sourceUri
        if (showComposer && selectedType == "vídeo" && uri != null) {
            videoFrames = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                extractVyboraVideoFrames(context, uri)
            }
        } else {
            videoFrames = emptyList()
        }
    }
    LaunchedEffect(galleryOpen) {
        if (galleryOpen && galleryAssets.isEmpty()) {
            galleryLoading = true
            val assets = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                val result = mutableListOf<VyboraGalleryAsset>()
                val resolver = context.contentResolver
                val collections = listOf(
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI to false,
                    android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI to true
                )
                collections.forEach { (collection, isVideo) ->
                    val projection = arrayOf(
                        android.provider.MediaStore.MediaColumns._ID,
                        android.provider.MediaStore.MediaColumns.DISPLAY_NAME,
                        android.provider.MediaStore.MediaColumns.DATE_ADDED
                    )
                    resolver.query(collection, projection, null, null,
                        "${android.provider.MediaStore.MediaColumns.DATE_ADDED} DESC")?.use { cursor ->
                        val idCol = cursor.getColumnIndexOrThrow(android.provider.MediaStore.MediaColumns._ID)
                        val nameCol = cursor.getColumnIndexOrThrow(android.provider.MediaStore.MediaColumns.DISPLAY_NAME)
                        val dateCol = cursor.getColumnIndexOrThrow(android.provider.MediaStore.MediaColumns.DATE_ADDED)
                        while (cursor.moveToNext()) {
                            val id = cursor.getLong(idCol)
                            result.add(VyboraGalleryAsset(
                                android.content.ContentUris.withAppendedId(collection, id),
                                isVideo,
                                cursor.getLong(dateCol),
                                cursor.getString(nameCol) ?: ""
                            ))
                        }
                    }
                }
                result.sortedByDescending { it.dateAdded }.take(250)
            }
            galleryAssets = assets
            galleryLoading = false
        }
    }

    fun startVideoRecording() {
        val video = videoCapture
        if (video == null) { cameraError = "A câmera de vídeo ainda está iniciando. Tente novamente."; return }
        try {
            val file = File(context.cacheDir, "vybora-video-" + System.currentTimeMillis() + ".mp4")
            val output = androidx.camera.video.FileOutputOptions.Builder(file).build()
            var pending = video.output.prepareRecording(context, output)
            val audioGranted = androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) == android.content.pm.PackageManager.PERMISSION_GRANTED
            if (audioGranted) pending = pending.withAudioEnabled()
            activeRecording = pending.start(androidx.core.content.ContextCompat.getMainExecutor(context)) { event ->
                when (event) {
                    is androidx.camera.video.VideoRecordEvent.Start -> { isRecording = true; cameraError = "" }
                    is androidx.camera.video.VideoRecordEvent.Finalize -> {
                        isRecording = false
                        activeRecording = null
                        if (event.hasError()) cameraError = "Falha ao finalizar o vídeo (" + event.error + ")."
                        else { sourceUri = Uri.fromFile(file); selectedUri = sourceUri; selectedType = "vídeo"; showComposer = true }
                    }
                }
            }
        } catch (e: Exception) { cameraError = "Não foi possível gravar o vídeo: " + (e.localizedMessage ?: "erro desconhecido") }
    }

    val audioPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) startVideoRecording()
        else cameraError = "Permita o microfone para gravar vídeos com áudio."
    }
    LaunchedEffect(cameraGranted, lensFacing, showComposer, previewView) {
        if (initialMode == "camera" && cameraGranted && !showComposer && previewView != null) {
            try {
                val future = androidx.camera.lifecycle.ProcessCameraProvider.getInstance(context)
                future.addListener({
                    try {
                        val provider = future.get()
                        cameraProvider = provider
                        val view = previewView ?: return@addListener
                        val preview = androidx.camera.core.Preview.Builder().build().also { it.setSurfaceProvider(view.surfaceProvider) }
                        val capture = androidx.camera.core.ImageCapture.Builder().setCaptureMode(androidx.camera.core.ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY).build()
                        val recorder = androidx.camera.video.Recorder.Builder()
                            .setQualitySelector(androidx.camera.video.QualitySelector.from(androidx.camera.video.Quality.HD))
                            .build()
                        val video = androidx.camera.video.VideoCapture.withOutput(recorder)
                        val selector = androidx.camera.core.CameraSelector.Builder().requireLensFacing(lensFacing).build()
                        provider.unbindAll()
                        val boundCamera = provider.bindToLifecycle(lifecycleOwner, selector, preview, capture, video)
                        cameraControl = boundCamera
                        zoomRatio = 1f
                        capture.flashMode = if (flashEnabled) androidx.camera.core.ImageCapture.FLASH_MODE_ON else androidx.camera.core.ImageCapture.FLASH_MODE_OFF
                        imageCapture = capture
                        videoCapture = video
                        cameraError = ""
                    } catch (e: Exception) {
                        cameraError = "Não foi possível iniciar a câmera: " + (e.localizedMessage ?: "dispositivo indisponível")
                    }
                }, androidx.core.content.ContextCompat.getMainExecutor(context))
            } catch (e: Exception) { cameraError = "Câmera indisponível neste aparelho." }
        } else {
            if (isRecording) activeRecording?.stop()
            cameraProvider?.unbindAll()
            cameraControl = null
            imageCapture = null
            videoCapture = null
        }
    }

    if (initialMode == "text" || initialMode == "poll") {
        Column(
            Modifier.fillMaxSize().background(Color.White).padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onCameraCancelled) { Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = Color.Black) }
                Text(if (initialMode == "poll") "Criar enquete" else "Nova publicação",
                    color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.weight(1f))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, contentDescription = null, tint = Color.DarkGray, modifier = Modifier.size(44.dp))
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(prefs.getString("username", "Meu perfil").orEmpty(), fontWeight = FontWeight.Bold, color = Color.Black)
                    Text("Público", color = Color.DarkGray, style = MaterialTheme.typography.labelMedium)
                }
            }
            OutlinedTextField(
                value = caption,
                onValueChange = { if (it.length <= (if (initialMode == "poll") 200 else 5000)) caption = it },
                placeholder = { Text(if (initialMode == "poll") "Qual é a sua pergunta?" else "No que você está pensando?") },
                modifier = Modifier.fillMaxWidth().then(if (initialMode == "text") Modifier.heightIn(min = 240.dp) else Modifier),
                minLines = if (initialMode == "text") 8 else 2,
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.Black, unfocusedTextColor = Color.Black,
                    focusedBorderColor = Color(0xFF1976D2), unfocusedBorderColor = Color.LightGray
                )
            )
            Text("${caption.length} / ${if (initialMode == "poll") 200 else 5000}",
                modifier = Modifier.align(Alignment.End), color = Color.DarkGray, style = MaterialTheme.typography.labelSmall)
            if (initialMode == "poll") {
                pollOptions.forEachIndexed { index, option ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(value = option,
                            onValueChange = { value -> pollOptions = pollOptions.toMutableList().also { it[index] = value } },
                            placeholder = { Text("Opção ${index + 1}") }, modifier = Modifier.weight(1f), singleLine = true)
                        if (pollOptions.size > 2) IconButton(onClick = { pollOptions = pollOptions.filterIndexed { i, _ -> i != index } }) {
                            Icon(Icons.Default.Close, contentDescription = "Remover opção", tint = Color.DarkGray)
                        }
                    }
                }
                if (pollOptions.size < 6) TextButton(onClick = { pollOptions = pollOptions + "" }) {
                    Text("+ Adicionar opção", color = Color.Black)
                }
                HorizontalDivider(color = Color.LightGray)
                Text("Duração da enquete: 3 dias", color = Color.Black)
                Text("Mostrar resultado para todos", color = Color.DarkGray)
            } else {
                HorizontalDivider(color = Color.LightGray)
                Row(Modifier.fillMaxWidth().clickable { openVyboraGallery() }.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Image, contentDescription = null, tint = Color.Black)
                    Spacer(Modifier.width(14.dp))
                    Text("Galeria", color = Color.Black)
                }
                HorizontalDivider(color = Color.LightGray)
                Row(Modifier.fillMaxWidth().clickable {
                    onNotice("Para fotografar, selecione Câmera no menu Criar.")
                }.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.Black)
                    Spacer(Modifier.width(14.dp))
                    Text("Câmera", color = Color.Black)
                }
                HorizontalDivider(color = Color.LightGray)
                Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Black)
                    Spacer(Modifier.width(14.dp))
                    Text("Localização", color = Color.Black)
                }
                HorizontalDivider(color = Color.LightGray)
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Permitir comentários", modifier = Modifier.weight(1f), color = Color.Black)
                    Switch(checked = allowComments, onCheckedChange = { allowComments = it })
                }
                HorizontalDivider(color = Color.LightGray)
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Permitir compartilhamentos", modifier = Modifier.weight(1f), color = Color.Black)
                    Switch(checked = allowSharing, onCheckedChange = { allowSharing = it })
                }
            }
            Button(
                onClick = {
                    val token = prefs.getString("token", null)
                    if (token.isNullOrBlank()) {
                        onNotice("Entre na sua conta antes de publicar.")
                    } else if (!publishing) {
                        publishing = true
                        Thread {
                            try {
                                if (initialMode == "poll") {
                                    // A API atual não expõe votação; preserva pergunta e opções como publicação textual.
                                    val options = pollOptions.filter { it.isNotBlank() }
                                    VyboraApi.createPollPost(token, caption.trim(), options)
                                } else {
                                    VyboraApi.createTextPost(token, caption.trim())
                                }
                                android.os.Handler(android.os.Looper.getMainLooper()).post {
                                    publishing = false
                                    onNotice(if (initialMode == "poll") "Enquete publicada!" else "Texto publicado com sucesso!")
                                    onCameraCancelled()
                                }
                            } catch (e: Exception) {
                                android.os.Handler(android.os.Looper.getMainLooper()).post {
                                    publishing = false
                                    onNotice(e.message ?: "Falha ao publicar.")
                                }
                            }
                        }.start()
                    }
                },
                enabled = !publishing && caption.isNotBlank() && (initialMode != "poll" || pollOptions.count { it.isNotBlank() } >= 2),
                modifier = Modifier.fillMaxWidth()
            ) { Text("Publicar") }
        }
        return
    }

    if (initialMode == "gallery" && !showComposer && !galleryOpen) {
        Column(Modifier.fillMaxSize().background(Color.White).padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center) {
            Text("Selecionar foto ou vídeo", color = Color.Black, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            Button(onClick = { openVyboraGallery() }) { Text("Abrir galeria") }
            TextButton(onClick = onCameraCancelled) { Text("Voltar") }
        }
        return
    }

    if (initialMode == "camera" && !cameraGranted && !showComposer) {
        Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(60.dp))
            Text("Câmera QINBLY", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Text("Câmera integrada ao aplicativo, com prévia e filtros.", color = Muted)
            Button(onClick = { cameraPermission.launch(android.Manifest.permission.CAMERA) }) { Text("Permitir câmera") }
            OutlinedButton(onClick = { imagePicker.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }) { Text("Escolher foto da galeria") }
            OutlinedButton(onClick = { videoPicker.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)) }) { Text("Escolher vídeo da galeria") }
            TextButton(onClick = onCameraCancelled) { Text("Voltar ao início") }
        }
        return
    }

    if (publishSuccess) {
        Box(Modifier.fillMaxSize().background(Color(0xFF050505)), contentAlignment = Alignment.Center) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Box(
                    Modifier.size(124.dp).border(3.dp, Pink, CircleShape).clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.Black, modifier = Modifier.size(76.dp))
                }
                Text("Publicado!", color = Color.Black, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text("Seu conteúdo já está no feed.", color = Color.Black, style = MaterialTheme.typography.bodyLarge)
                Button(
                    onClick = {
                        publishSuccess = false
                        showComposer = false
                        selectedUri = null
                        sourceUri = null
                        selectedType = ""
                        caption = ""
                        onCameraCancelled()
                    },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
                ) { Text("Ver publicação", fontWeight = FontWeight.Bold) }
                OutlinedButton(
                    onClick = {
                        publishSuccess = false
                        showComposer = false
                        selectedUri = null
                        sourceUri = null
                        selectedType = ""
                        caption = ""
                        publishSettings = false
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) { Text("Criar outra publicação") }
            }
        }
        return
    }

    if (initialMode == "camera" && !showComposer) {
        Box(Modifier.fillMaxSize().background(Color(0xFF080808))) {
            androidx.compose.ui.viewinterop.AndroidView(
                factory = { ctx -> androidx.camera.view.PreviewView(ctx).also {
                    it.scaleType = androidx.camera.view.PreviewView.ScaleType.FILL_CENTER
                    it.implementationMode = androidx.camera.view.PreviewView.ImplementationMode.COMPATIBLE
                    previewView = it
                } },
                modifier = Modifier.fillMaxSize().pointerInput(cameraControl, zoomRatio) {
                    detectTransformGestures { _, _, zoomChange, _ ->
                        val camera = cameraControl ?: return@detectTransformGestures
                        val maxZoom = camera.cameraInfo.zoomState.value?.maxZoomRatio ?: 1f
                        val minZoom = camera.cameraInfo.zoomState.value?.minZoomRatio ?: 1f
                        val nextZoom = (zoomRatio * zoomChange).coerceIn(minZoom, maxZoom)
                        camera.cameraControl.setZoomRatio(nextZoom)
                        zoomRatio = nextZoom
                    }
                }
            )
            val tint = when (selectedEffect) {
                "Golden Hour", "Bronze" -> Color(0x44FF9A42)
                "Frio", "Gelo" -> Color(0x443F8CFF)
                "Rosa", "Corações", "Glow" -> Color(0x44FF45B5)
                "Neon", "Óculos Neon" -> Color(0x443F12FF)
                "Verde" -> Color(0x4432FF70)
                "Mono", "VHS" -> Color(0x44808080)
                "Fogo" -> Color(0x44FF3300)
                else -> Color.Transparent
            }
            Box(Modifier.fillMaxSize().background(tint))
            val effectOverlay = when (selectedEffect) {
                "Corações" -> "💖 ✨ 💖"
                "Orelhas de gato" -> "🐱"
                "Cachorrinho" -> "🐶"
                "Coroa" -> "👑"
                "Óculos Neon" -> "🕶️"
                "Unicórnio" -> "🦄"
                "Borboletas" -> "🦋 ✨ 🦋"
                "Estrelas" -> "⭐ ✨ ⭐"
                "Chuva de confete" -> "🎉 ✨ 🎊"
                "Fogo" -> "🔥"
                "Bolhas" -> "🫧 🫧"
                else -> ""
            }
            if (effectOverlay.isNotEmpty()) {
                Text(effectOverlay, modifier = Modifier.align(Alignment.Center).padding(bottom = 120.dp),
                    color = Color.Black, style = MaterialTheme.typography.headlineLarge)
            }
            Box(Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 10.dp)) {
                Row(
                    Modifier.align(Alignment.TopCenter).fillMaxWidth()
                        .background(Color(0x55000000)).padding(horizontal = 4.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = { if (isRecording) activeRecording?.stop(); onCameraCancelled() }) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar câmera", tint = Color.Black, modifier = Modifier.size(28.dp))
                    }
                    IconButton(onClick = {
                        flashEnabled = !flashEnabled
                        imageCapture?.flashMode = if (flashEnabled) androidx.camera.core.ImageCapture.FLASH_MODE_ON else androidx.camera.core.ImageCapture.FLASH_MODE_OFF
                    }) {
                        Text(if (flashEnabled) "⚡" else "ϟ", color = Color.Black, style = MaterialTheme.typography.titleLarge)
                    }
                    IconButton(onClick = { onNotice("Configurações da câmera") }) {
                        Icon(Icons.Default.Settings, contentDescription = "Configurações", tint = Color.Black)
                    }
                }

                Column(
                    Modifier.align(Alignment.CenterEnd).clip(RoundedCornerShape(24.dp))
                        .background(Color(0x77201F25)).padding(horizontal = 7.dp, vertical = 9.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    listOf(
                        "Efeitos" to Icons.Default.AutoAwesome, "Beleza" to Icons.Default.Face,
                        "Timer" to Icons.Default.Timer, "Formato" to Icons.Default.Crop,
                        "Velocidade" to Icons.Default.Speed, "Filtros" to Icons.Default.FilterVintage,
                        "Mais" to Icons.Default.ExpandMore
                    ).forEach { (label, icon) ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(Modifier.size(42.dp).clip(CircleShape)
                                .background(if (activeCameraPanel == label) Color(0xFF55545B) else Color(0x33202025))
                                .border(1.dp, Color(0x557D7D83), CircleShape)
                                .clickable { activeCameraPanel = if (activeCameraPanel == label) "" else label },
                                contentAlignment = Alignment.Center) {
                                Icon(icon, contentDescription = label, tint = Color.Black, modifier = Modifier.size(22.dp))
                            }
                            Text(label, color = Color.Black, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                if (activeCameraPanel.isNotEmpty()) {
                    Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                        .padding(bottom = 150.dp, start = 12.dp, end = 12.dp)
                        .clip(RoundedCornerShape(20.dp)).background(Color(0xE916171D))
                        .border(1.dp, Color(0x557D7D83), RoundedCornerShape(20.dp))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text(activeCameraPanel, color = Color.White, fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                            IconButton(onClick = { activeCameraPanel = "" }, modifier = Modifier.size(30.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Fechar painel", tint = Color.Black)
                            }
                        }
                        when(activeCameraPanel) {
                            "Filtros", "Efeitos" -> Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf("Original", "Quente", "Frio", "Vivo", "Cinema", "P&B").forEach { f ->
                                    Text(f, color = Color.Black, style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.weight(1f).clip(RoundedCornerShape(9.dp))
                                            .background(if (selectedFilter == f) Color(0xFF55545B) else Color(0xFF292A31))
                                            .clickable { selectedFilter = f; selectedEffect = f }
                                            .padding(vertical = 12.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                }
                            }
                            "Velocidade" -> Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                listOf("0,3×", "0,5×", "1×", "2×", "3×").forEach { v ->
                                    Text(v, color = Color.Black, modifier = Modifier.clip(CircleShape)
                                        .background(if (selectedVideoSpeed == v) Color(0xFF55545B) else Color.Transparent)
                                        .clickable { selectedVideoSpeed = v }.padding(10.dp))
                                }
                            }
                            "Formato" -> Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                listOf("9:16", "1:1", "4:3").forEach { f ->
                                    Text(f, color = Color.Black, modifier = Modifier.clip(RoundedCornerShape(8.dp))
                                        .background(if (selectedCameraFormat == f) Color(0xFF55545B) else Color(0xFF292A31))
                                        .clickable { selectedCameraFormat = f }.padding(horizontal = 18.dp, vertical = 10.dp))
                                }
                            }
                            "Timer" -> Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                listOf("Desligado", "3 s", "5 s", "10 s").forEach { v ->
                                    Text(v, color = Color.Black, modifier = Modifier.clip(RoundedCornerShape(8.dp))
                                        .background(if (selectedTimerSeconds == v) Color(0xFF55545B) else Color(0xFF292A31))
                                        .clickable { selectedTimerSeconds = v }.padding(horizontal = 10.dp, vertical = 10.dp))
                                }
                            }
                            "Beleza" -> {
                                Text("Suavização", color = Color(0xFFE4E4E8), style = MaterialTheme.typography.bodySmall)
                                Slider(value = beautyLevel, onValueChange = { beautyLevel = it }, valueRange = 0f..1f)
                            }
                            "Mais" -> {
                                listOf("Grade", "Flash", "Qualidade", "Áudio").forEach { option ->
                                    Row(Modifier.fillMaxWidth().clickable {
                                        if (option == "Flash") {
                                            flashEnabled = !flashEnabled
                                            imageCapture?.flashMode = if (flashEnabled) androidx.camera.core.ImageCapture.FLASH_MODE_ON else androidx.camera.core.ImageCapture.FLASH_MODE_OFF
                                        } else onNotice("$option: configuração da câmera")
                                    }.padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Text(option, color = Color.Black, modifier = Modifier.weight(1f))
                                        Text(if (option == "Flash" && flashEnabled) "Ligado" else "›", color = Color(0xFFB8B8BE))
                                    }
                                }
                            }
                        }
                    }
                }

                Column(
                    Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                        .background(Brush.verticalGradient(listOf(Color(0x00150B20), Color(0xB8140B22), Color(0xF20B0A16))))
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (cameraError.isNotBlank()) {
                        Text(cameraError, color = Color.White, style = MaterialTheme.typography.bodySmall,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        listOf("Foto", "Vídeo", "Live").forEach { option ->
                            val chosen = (option == "Foto" && captureMode == "Foto") ||
                                (option == "Vídeo" && captureMode == "Vídeo")
                            Text(
                                option,
                                color = if (chosen) Color.White else Color(0xFFB7B7B7),
                                fontWeight = if (chosen) FontWeight.Bold else FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 18.dp, vertical = 7.dp)
                                    .clickable {
                                        when (option) {
                                            "Foto" -> if (!isRecording) captureMode = "Foto"
                                            "Vídeo" -> if (!isRecording) { captureMode = "Vídeo"; selectedDuration = "15 s" }
                                            "Live" -> onNotice("A câmera ao vivo será integrada ao recurso Lives do QINBLY.")
                                        }
                                    },
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceEvenly) {
                        IconButton(onClick = { galleryFilter = "Todos"; openVyboraGallery() }, modifier = Modifier.size(52.dp).background(Color(0x77392843), CircleShape)) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = "Galeria", tint = Color.Black)
                        }
                        Box(Modifier.size(78.dp).clip(CircleShape).background(Color.White).padding(5.dp).clip(CircleShape).background(Color(0xFFFF3045)).clickable {
                            if (captureMode == "Vídeo") {
                                if (isRecording) activeRecording?.stop()
                                else {
                                    val audioGranted = androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) == android.content.pm.PackageManager.PERMISSION_GRANTED
                                    if (audioGranted) startVideoRecording() else audioPermission.launch(android.Manifest.permission.RECORD_AUDIO)
                                }
                            } else {
                                val capture = imageCapture
                                if (capture == null) cameraError = "A câmera ainda está iniciando. Tente novamente."
                                else {
                                    val file = File(context.cacheDir, "vybora-camera-" + System.currentTimeMillis() + ".jpg")
                                    val output = androidx.camera.core.ImageCapture.OutputFileOptions.Builder(file).build()
                                    capture.takePicture(output, androidx.core.content.ContextCompat.getMainExecutor(context), object : androidx.camera.core.ImageCapture.OnImageSavedCallback {
                                        override fun onImageSaved(result: androidx.camera.core.ImageCapture.OutputFileResults) {
                                            sourceUri = Uri.fromFile(file)
                                            selectedUri = sourceUri
                                            selectedType = "foto"
                                            showComposer = true
                                        }
                                        override fun onError(exception: androidx.camera.core.ImageCaptureException) { cameraError = "Falha ao tirar foto: " + (exception.message ?: "tente novamente") }
                                    })
                                }
                            }
                        }, contentAlignment = Alignment.Center) {
                            if (captureMode == "Vídeo") Box(Modifier.size(if (isRecording) 30.dp else 42.dp).clip(if (isRecording) RoundedCornerShape(8.dp) else CircleShape).background(if (isRecording) Color.White else Pink))
                            else Box(Modifier.size(58.dp).clip(CircleShape).background(Color(0xFFFF3045)))
                        }
                        IconButton(onClick = { videoPicker.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)) }, modifier = Modifier.size(52.dp).background(Color(0x77171827), CircleShape)) {
                            Icon(Icons.Default.VideoLibrary, contentDescription = "Escolher vídeo", tint = Color.White)
                        }
                    }
                    if (captureMode == "Vídeo" && !isRecording) {
                        Text("Vídeo • até $selectedDuration", color = Color.White.copy(alpha = 0.86f), style = MaterialTheme.typography.labelSmall)
                    } else if (isRecording) {
                        Text("● GRAVANDO — toque para finalizar", color = Color(0xFFFF3045), style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
        return
    }

    if (galleryOpen) {
        Column(Modifier.fillMaxSize().background(Color(0xFF080808)).padding(horizontal = 14.dp)) {
            Row(Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { galleryOpen = false; gallerySelection.clear() }) {
                    Icon(Icons.Default.Close, contentDescription = "Fechar galeria", tint = Color.Black)
                }
                Column(Modifier.weight(1f)) {
                    Text("Recentes", color = Color.Black, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Escolha uma foto ou vídeo", color = Color.LightGray, style = MaterialTheme.typography.bodySmall)
                }
                TextButton(onClick = { galleryFilter = "Todos" }) { Text("▾", color = Color.Black) }
            }
            Row(Modifier.fillMaxWidth().padding(bottom = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Todos", "Fotos", "Vídeos").forEach { filter ->
                    val active = galleryFilter == filter
                    Surface(
                        color = if (active) Color.White else Color(0xFF202020),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.clickable { galleryFilter = filter }
                    ) {
                        Text(filter, color = if (active) Color.Black else Color.White, fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp))
                    }
                }
            }
            val visibleAssets = galleryAssets.filter { when (galleryFilter) {
                "Fotos" -> !it.isVideo
                "Vídeos" -> it.isVideo
                else -> true
            } }
            if (galleryLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Color.Black) }
            } else if (visibleAssets.isEmpty()) {
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("Nenhuma mídia encontrada.", color = Color.LightGray)
                }
            } else {
                androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                    columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(3),
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(bottom = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(visibleAssets.size, key = { visibleAssets[it].uri.toString() }) { index ->
                        val asset = visibleAssets[index]
                        val selectedIndex = gallerySelection.indexOf(asset.uri)
                        Box(
                            Modifier.fillMaxWidth().aspectRatio(0.76f).clip(RoundedCornerShape(4.dp))
                                .clickable {
                                    gallerySelection.clear()
                                    gallerySelection.add(asset.uri)
                                    sourceUri = asset.uri
                                    selectedUri = asset.uri
                                    selectedType = if (asset.isVideo) "vídeo" else "foto"
                                    galleryOpen = false
                                    showComposer = true
                                }
                        ) {
                            AsyncImage(model = asset.uri, contentDescription = asset.name, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                            if (asset.isVideo) {
                                Row(Modifier.align(Alignment.BottomStart).fillMaxWidth().background(Color(0x66000000)).padding(5.dp),
                                    verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                                    Text("VÍDEO", color = Color.Black, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                            Box(Modifier.align(Alignment.TopEnd).padding(6.dp).size(23.dp).border(1.5.dp, Color.White, CircleShape)
                                .background(if (selectedIndex >= 0) Color(0xFFB12AFF) else Color(0x55000000), CircleShape),
                                contentAlignment = Alignment.Center) {
                                if (selectedIndex >= 0) Text("1", color = Color.Black, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
            Button(
                onClick = {
                    val asset = gallerySelection.lastOrNull()?.let { selected -> galleryAssets.firstOrNull { it.uri == selected } }
                    if (asset != null) {
                        sourceUri = asset.uri
                        selectedUri = asset.uri
                        selectedType = if (asset.isVideo) "vídeo" else "foto"
                        galleryOpen = false
                        showComposer = true
                    } else onNotice("Selecione uma foto ou vídeo.")
                },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).height(52.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
            ) { Text(if (gallerySelection.isEmpty()) "Avançar" else "Avançar (1)", fontWeight = FontWeight.Bold) }
        }
        return
    }

    // Etapa 1: editor visual. Etapa 2: opções e publicação.
    if (!publishSettings) {
        Box(Modifier.fillMaxSize().background(Color.Black)) {
            val uri = selectedUri
            if (uri != null && selectedType == "foto") {
                AsyncImage(
                    model = uri,
                    contentDescription = "Prévia da publicação",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else if (uri != null) {
                androidx.compose.ui.viewinterop.AndroidView(
                    factory = { ctx ->
                        android.widget.VideoView(ctx).apply {
                            setVideoURI(uri)
                            setOnPreparedListener { player -> player.isLooping = true; start() }
                            setOnErrorListener { _, _, _ -> true }
                        }
                    },
                    update = { view ->
                        if (view.tag != uri.toString()) {
                            view.tag = uri.toString()
                            view.setVideoURI(uri)
                            view.setOnPreparedListener { player -> player.isLooping = true; view.start() }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
            val editorFilterTint = when (selectedFilter) {
                "Quente" -> Color(0x22FF9A42)
                "Frio" -> Color(0x223F8CFF)
                "Vivo" -> Color(0x18FF45B5)
                "Cinema" -> Color(0x22333355)
                "P&B" -> Color(0x55999999)
                "Retrô" -> Color(0x22C78D5A)
                else -> Color.Transparent
            }
            if (editorFilterTint != Color.Transparent) {
                Box(Modifier.fillMaxSize().background(editorFilterTint))
            }
            if (editorTool == "Desenho") {
                androidx.compose.foundation.Canvas(
                    modifier = Modifier.fillMaxSize().pointerInput(editorTool) {
                        detectDragGestures(
                            onDragStart = { start ->
                                editorStrokes.add(listOf(Offset(start.x / size.width, start.y / size.height)))
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                if (editorStrokes.isNotEmpty()) {
                                    val last = editorStrokes.last()
                                    editorStrokes[editorStrokes.lastIndex] = last + Offset(
                                        (change.position.x / size.width).coerceIn(0f, 1f),
                                        (change.position.y / size.height).coerceIn(0f, 1f)
                                    )
                                }
                            }
                        )
                    }
                ) {
                    editorStrokes.forEach { points ->
                        if (points.size > 1) {
                            val path = androidx.compose.ui.graphics.Path()
                            path.moveTo(points.first().x * size.width, points.first().y * size.height)
                            points.drop(1).forEach { path.lineTo(it.x * size.width, it.y * size.height) }
                            drawPath(path, Color(0xFFFF2A9D), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 7.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round))
                        }
                    }
                }
            }
            if (editorSticker.isNotBlank()) {
                Text(editorSticker, modifier = Modifier.align(Alignment.Center), color = Color.Black, style = MaterialTheme.typography.displayLarge)
            }
            if (editorText.isNotBlank()) {
                Text(editorText, modifier = Modifier.align(Alignment.Center).padding(horizontal = 28.dp, vertical = 72.dp),
                    color = Color.Black, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0x99000000), Color.Transparent, Color.Transparent, Color(0xEE000000)))))
            // Cabeçalho: voltar à esquerda e música centralizada, sem ferramentas horizontais.
            IconButton(
                onClick = {
                    selectedUri = null
                    sourceUri = null
                    selectedType = ""
                    caption = ""
                    editorText = ""
                    editorSticker = ""
                    editorStrokes.clear()
                    editorRatio = "Original"
                    publishSettings = false
                    showComposer = false
                },
                modifier = Modifier.align(Alignment.TopStart).padding(start = 12.dp, top = 12.dp)
                    .size(48.dp).border(1.dp, Color(0x557E828B), CircleShape)
                    .background(Color(0x88303030), CircleShape)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = Color.Black)
            }
            Row(
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 12.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .border(1.dp, Color(0x557E828B), RoundedCornerShape(28.dp))
                    .background(Color(0x88303030))
                    .clickable { onNotice("A biblioteca de músicas será conectada aqui.") }
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                Icon(Icons.Default.MusicNote, contentDescription = null, tint = Color.Black, modifier = Modifier.size(20.dp))
                Text("Adicionar som", color = Color.Black, fontWeight = FontWeight.SemiBold)
            }

            // Nova coluna de ferramentas do editor, com identidade visual própria.
            Column(
                modifier = Modifier.align(Alignment.CenterEnd).padding(end = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(11.dp)
            ) {
                val editorActions = listOf(
                    "Compartilhar" to Icons.Default.Share,
                    "Efeitos" to Icons.Default.AutoAwesome,
                    "Texto" to Icons.Default.Title,
                    "Stickers" to Icons.Default.ChatBubbleOutline,
                    "Editar vídeo" to Icons.Default.Movie,
                    "Camadas" to Icons.Default.Layers,
                    "Recortar" to Icons.Default.Crop
                )
                editorActions.forEachIndexed { index, action ->
                    val (tool, symbol) = action
                    val active = tool == "Editar vídeo"
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                             .shadow(if (active) 7.dp else 2.dp, CircleShape, clip = false)
                            .clip(CircleShape)
                            .background(Color(0xB52A2A2E))
                            .border(
                                width = if (active) 1.5.dp else 1.dp,
                                color = if (active) Color(0xFFE8E8EB) else Color(0x557D7D83),
                                shape = CircleShape
                            )
                            .clickable {
                                when (tool) {
                                    "Compartilhar" -> try {
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            val uri = selectedUri
                                            type = if (selectedType == "vídeo") "video/*" else "image/*"
                                            if (uri != null) {
                                                putExtra(Intent.EXTRA_STREAM, uri)
                                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                            } else {
                                                type = "text/plain"
                                                putExtra(Intent.EXTRA_TEXT, caption)
                                            }
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "Compartilhar publicação"))
                                    } catch (_: Exception) {
                                        onNotice("Não foi possível abrir o compartilhamento neste aparelho.")
                                    }
                                    "Efeitos" -> {
                                        editorTool = "Filtros"
                                        selectedFilter = when (selectedFilter) {
                                            "Original" -> "Quente"; "Quente" -> "Frio"; "Frio" -> "Vivo"
                                            "Vivo" -> "Cinema"; "Cinema" -> "P&B"; else -> "Original"
                                        }
                                    }
                                    "Texto" -> editorTool = "Texto"
                                    "Stickers" -> editorTool = "Stickers"
                                    "Editar vídeo" -> if (selectedType == "vídeo") editorTool = "Vídeo" else videoPicker.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
                                    "Camadas" -> editorTool = "Camadas"
                                    "Recortar" -> editorTool = "Ajustar"
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier.size(42.dp).clip(CircleShape)
                                .background(if (active) Color(0x332FFFFFF) else Color.Transparent),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = symbol,
                                contentDescription = tool,
                                tint = Color.Black,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xE6000000))))
                    .padding(bottom = 20.dp, top = 52.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // A tira de quadros pertence à edição de vídeo. Não repetir
                // a mesma fotografia como se fossem frames de uma timeline.
                if (selectedUri != null && selectedType == "vídeo") {
                    Row(
                        Modifier.fillMaxWidth().height(54.dp).padding(horizontal = 12.dp)
                            .clip(RoundedCornerShape(8.dp)).background(Color(0xAA171717)),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.foundation.lazy.LazyRow(
                            modifier = Modifier.weight(1f).height(50.dp),
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            items(videoFrames.size) { index ->
                                AsyncImage(
                                    model = videoFrames[index],
                                    contentDescription = "Quadro de vídeo ${index + 1}",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.width(43.dp).height(46.dp).clip(RoundedCornerShape(4.dp))
                                )
                            }
                        }
                        Box(Modifier.width(3.dp).height(52.dp).background(Color.White))
                    }
                }
                if (editorTool == "Ajustar") {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        listOf("Original", "1:1", "4:5", "9:16", "16:9").forEach { ratio ->
                            TextButton(onClick = { editorRatio = ratio }) {
                                Text(ratio, color = if (editorRatio == ratio) Color.White else Color.LightGray)
                            }
                        }
                    }
                }
                if (editorTool == "Texto") {
                    OutlinedTextField(
                        value = editorText,
                        onValueChange = { editorText = it },
                        placeholder = { Text("Digite o texto da foto", color = Color.LightGray) },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                            focusedBorderColor = Color.White, unfocusedBorderColor = Color.LightGray,
                            cursorColor = Color.White
                        )
                    )
                }
                if (editorTool == "Stickers") {
                    androidx.compose.foundation.lazy.LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(horizontal = 18.dp)
                    ) {
                        items(listOf("✨", "❤️", "😂", "🔥", "😍", "⭐", "🎉", "🦋", "🌈", "💯")) { sticker ->
                            Text(sticker, modifier = Modifier.clip(CircleShape).clickable { editorSticker = sticker }
                                .background(if (editorSticker == sticker) Color(0x66444444) else Color.Transparent)
                                .padding(8.dp), style = MaterialTheme.typography.headlineSmall)
                        }
                    }
                }
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { onNotice("A publicação no Story será conectada ao recurso de Stories do QINBLY.") },
                        modifier = Modifier.weight(1f).height(54.dp),
                        shape = RoundedCornerShape(28.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF77717E)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black)
                        Spacer(Modifier.width(6.dp))
                        Text("Seu Story", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                    Button(
                        onClick = {
                            if (selectedUri == null && sourceUri == null) {
                                publishError = "Escolha um vídeo ou uma foto antes de continuar."
                                onNotice(publishError)
                            } else if (!selectedMediaReady) {
                                publishError = "Não foi possível ler a mídia da galeria. Selecione o arquivo novamente."
                                onNotice(publishError)
                            } else {
                                publishError = ""
                                publishSettings = true
                            }
                        },
                        modifier = Modifier.weight(1f).height(54.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
                    ) {
                        Text("Avançar", fontWeight = FontWeight.Bold, color = Color.Black)
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Black)
                    }
                }
            }
        }
    } else {
        Column(
            Modifier.fillMaxSize().background(Color(0xFF080808)).verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { publishSettings = false }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Voltar à edição", tint = Color.Black)
                }
                Text("Nova publicação", color = Color.Black, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                TextButton(onClick = { publishSettings = false }) { Text("Editar", color = Color.Black) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.Top) {
                if ((selectedUri ?: sourceUri) != null && selectedType == "foto") {
                    AsyncImage(
                        model = selectedUri ?: sourceUri,
                        contentDescription = "Mídia selecionada",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.width(86.dp).height(112.dp).clip(RoundedCornerShape(10.dp))
                    )
                } else {
                    Box(Modifier.width(86.dp).height(112.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFF222222)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.PlayCircle, contentDescription = null, tint = Color.Black, modifier = Modifier.size(34.dp))
                    }
                }
                OutlinedTextField(
                    value = caption,
                    onValueChange = { caption = it },
                    placeholder = { Text("Escreva uma legenda…", color = Color.LightGray) },
                    modifier = Modifier.weight(1f),
                    minLines = 4,
                    maxLines = 6,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color(0xFF555555),
                        cursorColor = Color.White,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    )
                )
            }
            Divider(color = Color(0xFF303030))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.PersonAddAlt1, contentDescription = null, tint = Color.Black)
                Spacer(Modifier.width(14.dp))
                Text("Marcar pessoas", color = Color.Black, modifier = Modifier.weight(1f))
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Language, contentDescription = null, tint = Color.Black)
                Spacer(Modifier.width(14.dp))
                Text("Adicionar localização", color = Color.Black, modifier = Modifier.weight(1f))
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Mic, contentDescription = null, tint = Color.Black)
                Spacer(Modifier.width(14.dp))
                Text("Adicionar música", color = Color.Black, modifier = Modifier.weight(1f))
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray)
            }
            Divider(color = Color(0xFF303030))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Groups, contentDescription = null, tint = Color.Black)
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text("Público", color = Color.Black)
                    Text("Todos podem ver sua publicação", color = Color.LightGray, style = MaterialTheme.typography.bodySmall)
                }
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ChatBubbleOutline, contentDescription = null, tint = Color.Black)
                Spacer(Modifier.width(14.dp))
                Text("Permitir comentários", color = Color.Black, modifier = Modifier.weight(1f))
                androidx.compose.material3.Switch(checked = allowComments, onCheckedChange = { allowComments = it }, colors = androidx.compose.material3.SwitchDefaults.colors(checkedTrackColor = Color(0xFF1976D2), checkedThumbColor = Color.White))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Share, contentDescription = null, tint = Color.Black)
                Spacer(Modifier.width(14.dp))
                Text("Permitir compartilhamento", color = Color.Black, modifier = Modifier.weight(1f))
                androidx.compose.material3.Switch(checked = allowSharing, onCheckedChange = { allowSharing = it }, colors = androidx.compose.material3.SwitchDefaults.colors(checkedTrackColor = Color(0xFF1976D2), checkedThumbColor = Color.White))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.FileDownload, contentDescription = null, tint = Color.Black)
                Spacer(Modifier.width(14.dp))
                Text("Salvar no dispositivo", color = Color.Black, modifier = Modifier.weight(1f))
                androidx.compose.material3.Switch(checked = saveToDevice, onCheckedChange = { saveToDevice = it }, colors = androidx.compose.material3.SwitchDefaults.colors(checkedTrackColor = Color(0xFF1976D2), checkedThumbColor = Color.White))
            }
            Button(
                onClick = {
                    val token = prefs.getString("token", null)
                    when {
                        token.isNullOrBlank() -> onNotice("Entre na sua conta antes de publicar.")
                        selectedUri == null && sourceUri == null -> { publishError = "Selecione uma foto ou vídeo antes de publicar."; onNotice(publishError); publishSettings = false; openVyboraGallery() }
                        !selectedMediaReady -> { publishError = "O vídeo da galeria não está acessível. Escolha-o novamente."; onNotice(publishError); publishSettings = false }
                        else -> {
                            publishError = ""
                            publishing = true
                            Thread {
                                try {
                                    if (selectedUri == null && sourceUri == null) throw IllegalStateException("Selecione uma foto ou vídeo antes de publicar.")
                                    else {
                                        val selectedMediaUri = selectedUri ?: sourceUri ?: throw IllegalStateException("Mídia não selecionada.")
                                        val mime = context.contentResolver.getType(selectedMediaUri)?.takeIf { it.startsWith("image/") || it.startsWith("video/") } ?: if (selectedType == "vídeo") "video/mp4" else "image/jpeg"
                                        // Copia a mídia para cache para garantir tamanho exato no upload,
                                        // inclusive para URIs de câmera, galeria e provedores de nuvem.
                                        val tempUploadFile = java.io.File.createTempFile("vybora-upload-", if (selectedType == "vídeo") ".mp4" else ".jpg", context.cacheDir)
                                        val uploadUri = Uri.fromFile(tempUploadFile)
                                        val size = try {
                                            context.contentResolver.openInputStream(selectedMediaUri)?.use { input ->
                                                tempUploadFile.outputStream().use { output -> input.copyTo(output, 64 * 1024) }
                                            } ?: throw IllegalStateException("Não foi possível ler a foto ou vídeo selecionado.")
                                            tempUploadFile.length()
                                        } catch (e: Exception) {
                                            tempUploadFile.delete()
                                            throw e
                                        }
                                        try {
                                            if (size <= 0L) throw IllegalStateException("O arquivo selecionado está vazio ou não pôde ser lido.")
                                            val extension = android.webkit.MimeTypeMap.getSingleton().getExtensionFromMimeType(mime) ?: if (mime.startsWith("video/")) "mp4" else "jpg"
                                            val fileName = "vybora-${System.currentTimeMillis()}.$extension"
                                            val fileId = VyboraApi.uploadPostMedia(context, token, uploadUri, fileName, mime, size)
                                            VyboraApi.createMediaPost(token, caption, fileId, if (mime.startsWith("video/")) "video" else "image")
                                        } finally {
                                            tempUploadFile?.delete()
                                        }
                                    }
                                    android.os.Handler(android.os.Looper.getMainLooper()).post {
                                        publishing = false
                                        caption = ""
                                        selectedUri = null
                                        selectedType = ""
                                        publishSettings = false
                                        publishSuccess = true
                                    }
                                } catch (e: Exception) {
                                    android.os.Handler(android.os.Looper.getMainLooper()).post {
                                        publishing = false
                                        publishError = e.message?.takeIf { it.isNotBlank() } ?: "Não foi possível publicar. Verifique a conexão e tente novamente."
                                        onNotice(publishError)
                                    }
                                }
                            }.start()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                enabled = !publishing,
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
            ) {
                if (publishing) CircularProgressIndicator(Modifier.size(19.dp), strokeWidth = 2.dp, color = Color.Black)
                else Icon(Icons.Default.Send, contentDescription = null, tint = Color.Black)
                Spacer(Modifier.width(8.dp))
                Text(if (publishing) "Publicando…" else "Publicar", fontWeight = FontWeight.Bold, color = Color.Black)
            }
            if (publishError.isNotBlank()) {
                Text(publishError, color = Color(0xFFFF8A80), style = MaterialTheme.typography.bodyMedium)
            }
            TextButton(onClick = { publishSettings = false }) {
                Text("Voltar à edição", color = Color.Black)
            }
        }
    }
}

@Composable
private fun LivePage(scrollToTopRequest: Int, onNotice: (String) -> Unit) {
    val liveScrollState = rememberScrollState()
    LaunchedEffect(scrollToTopRequest) { if (scrollToTopRequest > 0) liveScrollState.animateScrollTo(0) }
    var selectedCategory by remember { mutableStateOf("Descobrir") }
    val categories = listOf("Para você", "Em alta", "Jogos", "Música", "Seguindo")
    Column(Modifier.fillMaxSize().verticalScroll(liveScrollState).padding(horizontal = 14.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalAlignment = Alignment.CenterVertically) {
            categories.forEachIndexed { index, category ->
                Surface(
                    color = if (selectedCategory == category) Color(0xFF9D1BCA) else Panel,
                    shape = RoundedCornerShape(24.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (selectedCategory == category) Pink else Color(0xFF292D48)),
                    modifier = Modifier.clickable { selectedCategory = category }
                ) {
                    Text(category, Modifier.padding(horizontal = 10.dp, vertical = 10.dp), color = Color.Black, style = MaterialTheme.typography.labelMedium, maxLines = 1)
                }
            }
        }
        Surface(
            color = Color.Transparent,
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth().height(230.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF503064))
        ) {
            Box(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(Color(0xFF140D31), Color(0xFF5E155D), Color(0xFF10182D))))) {
                Column(Modifier.align(Alignment.Center).padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(Modifier.size(58.dp).clip(CircleShape).background(Brush.linearGradient(listOf(Pink, Violet))), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Radio, contentDescription = null, tint = Color.Black, modifier = Modifier.size(31.dp))
                    }
                    Text("Entre no universo das Lives", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.Black)
                    Text("Descubra criadores, jogos, música e conversas ao vivo.", color = Muted, style = MaterialTheme.typography.bodyMedium)
                    Button(onClick = { onNotice("As transmissões e a reprodução ao vivo ainda precisam ser conectadas ao servidor de mídia.") }, colors = ButtonDefaults.buttonColors(containerColor = Pink), shape = RoundedCornerShape(14.dp)) {
                        Text("Explorar transmissões")
                    }
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Lives recomendadas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Text("QINBLY LIVE", color = Pink, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        }
        Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Visibility, contentDescription = null, tint = Muted, modifier = Modifier.size(40.dp))
                Text("Nenhuma transmissão disponível", fontWeight = FontWeight.SemiBold)
                Text("Quando houver lives conectadas ao serviço, elas aparecerão aqui com público e criador reais.", color = Muted, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
@Composable
private fun SimplePage(title: String, description: String, action: String, onAction: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(22.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Surface(color = Panel, shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Box(Modifier.size(54.dp).clip(RoundedCornerShape(18.dp)).background(Brush.linearGradient(listOf(Violet, Pink))), contentAlignment = Alignment.Center) { Text("V", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black) }
                Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text(description, color = Muted)
                Button(onClick = onAction, modifier = Modifier.fillMaxWidth()) { Text(action) }
            }
        }
        Text("QINBLY • Protótipo Android", color = Muted, style = MaterialTheme.typography.labelSmall)
    }
}