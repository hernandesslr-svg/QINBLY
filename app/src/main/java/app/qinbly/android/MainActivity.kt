package app.qinbly.android

import androidx.compose.foundation.layout.windowInsetsPadding

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
import coil.request.videoFrameMillis
import androidx.compose.ui.graphics.toArgb
import java.io.File
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Check
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Headphones
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.DataSaverOn
import androidx.compose.material.icons.outlined.Sensors
import androidx.compose.material.icons.outlined.GroupAdd
import androidx.compose.material.icons.outlined.Brightness4
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.PlayCircleOutline
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Accessibility
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.PersonAddAlt1
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.AlternateEmail
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.VolumeOff
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.FontDownload
import androidx.compose.material.icons.outlined.FormatSize
import androidx.compose.material.icons.outlined.SlowMotionVideo
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.SystemUpdate
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
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.material.icons.filled.Poll
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.PersonAddAlt1
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
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
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

private fun postRelativeTime(createdAt: String, nowMillis: Long): String {
    val timestamp = try {
        java.time.Instant.parse(createdAt).toEpochMilli()
    } catch (_: Exception) {
        try {
            java.time.OffsetDateTime.parse(createdAt.replace(" ", "T")).toInstant().toEpochMilli()
        } catch (_: Exception) { return "Data indisponível" }
    }
    val seconds = ((nowMillis - timestamp) / 1000).coerceAtLeast(0)
    return when {
        seconds < 60 -> "Agora"
        seconds < 3600 -> "Há ${seconds / 60} min"
        seconds < 86400 -> "Há ${seconds / 3600} h"
        seconds < 604800 -> "Há ${seconds / 86400} d"
        else -> java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy", java.util.Locale("pt", "BR"))
            .withZone(java.time.ZoneId.systemDefault()).format(java.time.Instant.ofEpochMilli(timestamp))
    }
}

@androidx.compose.runtime.Composable
private fun LivePostTime(createdAt: String, color: androidx.compose.ui.graphics.Color, suffix: String = "") {
    var nowMillis by androidx.compose.runtime.remember { androidx.compose.runtime.mutableLongStateOf(System.currentTimeMillis()) }
    androidx.compose.runtime.LaunchedEffect(createdAt) {
        while (true) {
            nowMillis = System.currentTimeMillis()
            kotlinx.coroutines.delay(15_000L)
        }
    }
    androidx.compose.material3.Text(postRelativeTime(createdAt, nowMillis) + suffix, color = color, fontSize = androidx.compose.ui.unit.TextUnit(12f, androidx.compose.ui.unit.TextUnitType.Sp))
}


/** Paleta do QINBLY (azul). Para mudar a cor da marca inteira, troque só estes valores. */
/** Guarda a proporção de cada foto já carregada: ao voltar para um post, a altura já está certa e a lista não pula. */
private object QinblyImageRatioCache { val map = java.util.concurrent.ConcurrentHashMap<String, Float>() }

private object QinblyBrand {
    /** Nome do app mostrado nas telas. Para renomear a rede, troque só este valor (e app_name em res/values/strings.xml). */
    const val Name = "QINBLY"
    val Accent = Color(0xFF7C4DFF)
    val AccentDeep = Color(0xFF5E35E0)
    val AccentLight = Color(0xFFB39DFF)
    val AccentLine: Color get() = if (QiblyTheme.dark) Color(0xFF3D3366) else Color(0xFFDDD2FF)
    val AccentSoft: Color get() = if (QiblyTheme.dark) Color(0xFF2A2250) else Color(0xFFF1ECFF)
    val Highlight = Color(0xFFFFB020)
    val Pink = Color(0xFFFF4F9A)
    val Ink = Color(0xFF0B1F2A)
}

private val Night: Color get() = QiblyTheme.background
private val Panel: Color get() = QiblyTheme.card
private val Violet = QinblyBrand.Accent
private val Pink = QinblyBrand.Accent
private val Muted: Color get() = if (QiblyTheme.dark) Color(0xFFA9B5C8) else Color(0xFF333333)



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
    // Ícone próprio do QINBLY em grade 24x24, contorno 2dp e cantos suaves.
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
    // Teclas de volume (aumentar ou diminuir): avisam os vídeos mudos para ligarem o som (o sistema continua tratando a tecla normalmente).
    override fun dispatchKeyEvent(event: android.view.KeyEvent): Boolean {
        if ((event.keyCode == android.view.KeyEvent.KEYCODE_VOLUME_UP || event.keyCode == android.view.KeyEvent.KEYCODE_VOLUME_DOWN) && event.action == android.view.KeyEvent.ACTION_DOWN) QinblyVolumeKey.upTicks++
        return super.dispatchKeyEvent(event)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        QinblyEngagement.init(getSharedPreferences("qinbly_session", MODE_PRIVATE))
        QinblyAvatarCache.init(applicationContext)
        // Janela clara desde o primeiro quadro: qualquer vão entre telas mostra a cor do app, não preto.
        window.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(QiblyTheme.background.toArgb()))
        // Imagens: cache em memória (25% da RAM disponível) e em disco (256 MB); reabrir telas não baixa de novo.
        coil.Coil.setImageLoader(
            coil.ImageLoader.Builder(this)
                .crossfade(120)
                .respectCacheHeaders(false)
                .memoryCache { coil.memory.MemoryCache.Builder(this).maxSizePercent(0.25).build() }
                .diskCache { coil.disk.DiskCache.Builder().directory(cacheDir.resolve("qinbly_images")).maxSizeBytes(256L * 1024 * 1024).build() }
                .build()
        )
        QinblyDeepLink.handle(intent)
        setContent {
            // Ícones da barra de cima/de baixo (hora, bateria, sinal) escuros quando o fundo do app é claro, claros quando é escuro.
            val barView = androidx.compose.ui.platform.LocalView.current
            val lightBackground = QiblyTheme.background.luminance() > 0.5f
            SideEffect {
                val controller = androidx.core.view.WindowCompat.getInsetsController(window, barView)
                controller.isAppearanceLightStatusBars = lightBackground
                controller.isAppearanceLightNavigationBars = lightBackground
            }
            QinblyApp()
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        QinblyDeepLink.handle(intent)
    }
}

/** Links de publicação (https://qinbly.com/post/<id> ou qinbly://post/<id>) abrem a publicação dentro do app. */
/** Puxar para atualizar em todo o app: recarrega a tela atual (refaz o carregamento de dados da página). */
@androidx.compose.runtime.Composable
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
private fun QinblyPullRefresh(enabled: Boolean = true, content: @androidx.compose.runtime.Composable () -> Unit) {
    if (!enabled) { content(); return }
    var nonce by androidx.compose.runtime.remember { androidx.compose.runtime.mutableIntStateOf(0) }
    var refreshing by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    LaunchedEffect(refreshing) { if (refreshing) { kotlinx.coroutines.delay(900); refreshing = false } }
    androidx.compose.material3.pulltorefresh.PullToRefreshBox(
        isRefreshing = refreshing,
        onRefresh = { refreshing = true; nonce++ },
        modifier = Modifier.fillMaxSize()
    ) {
        androidx.compose.runtime.key(nonce) { content() }
    }
}

/** Cartão "Selo de verificado" na edição de perfil: mostra o estado do pedido e deixa pedir a verificação (aprovação manual). */
@androidx.compose.runtime.Composable
private fun QinblyVerificationCard(token: String, onNotice: (String) -> Unit) {
    var verified by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var loaded by remember { mutableStateOf(false) }
    var dialog by remember { mutableStateOf(false) }
    var reason by remember { mutableStateOf("") }
    var sending by remember { mutableStateOf(false) }
    val reload = remember { mutableIntStateOf(0) }
    LaunchedEffect(token, reload.intValue) {
        if (token.isBlank()) return@LaunchedEffect
        Thread {
            try {
                val r = QinblyApi.getVerificationStatus(token)
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    verified = r.optBoolean("verified")
                    val req = r.optJSONObject("request")
                    status = req?.optString("status").orEmpty()
                    note = req?.optString("review_note").orEmpty().takeUnless { it == "null" }.orEmpty()
                    loaded = true
                }
            } catch (_: Exception) { android.os.Handler(android.os.Looper.getMainLooper()).post { loaded = true } }
        }.start()
    }
    if (token.isBlank() || !loaded) return
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(QiblyTheme.card).border(1.dp, QiblyTheme.border, RoundedCornerShape(14.dp)).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            QinblyVerifiedBadge(20.dp)
            Text("Selo de verificado", color = QiblyTheme.text, fontWeight = FontWeight.Bold)
        }
        when {
            verified -> Text("Sua conta é verificada.", color = QiblyTheme.secondary, style = MaterialTheme.typography.bodySmall)
            status == "pending" -> Text("Seu pedido está em análise. Avisaremos você por notificação.", color = QiblyTheme.secondary, style = MaterialTheme.typography.bodySmall)
            else -> {
                if (status == "rejected") Text("Seu último pedido não foi aprovado." + if (note.isNotBlank()) " Motivo: $note" else "", color = QiblyTheme.secondary, style = MaterialTheme.typography.bodySmall)
                else Text("Peça o selo para mostrar que sua conta é autêntica. Não há número mínimo de seguidores: cada pedido é analisado por nós.", color = QiblyTheme.secondary, style = MaterialTheme.typography.bodySmall)
                Button(onClick = { dialog = true }, colors = ButtonDefaults.buttonColors(containerColor = QinblyBrand.Accent, contentColor = Color.White)) {
                    Text(if (status == "rejected") "Pedir de novo" else "Pedir verificação")
                }
            }
        }
    }
    if (dialog) {
        AlertDialog(
            onDismissRequest = { if (!sending) dialog = false },
            title = { Text("Pedir verificação") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Conte quem você é e por que sua conta deve ser verificada. Você precisa ter foto, bio e ao menos uma publicação.", style = MaterialTheme.typography.bodySmall)
                    OutlinedTextField(value = reason, onValueChange = { reason = it.take(1000) }, modifier = Modifier.fillMaxWidth(), minLines = 3, maxLines = 6, label = { Text("Motivo") })
                }
            },
            confirmButton = {
                TextButton(enabled = !sending && reason.trim().length >= 10, onClick = {
                    sending = true
                    Thread {
                        try {
                            QinblyApi.requestVerification(token, reason)
                            android.os.Handler(android.os.Looper.getMainLooper()).post { sending = false; dialog = false; reason = ""; reload.intValue++; onNotice("Pedido enviado. Vamos analisar.") }
                        } catch (e: Exception) {
                            android.os.Handler(android.os.Looper.getMainLooper()).post { sending = false; onNotice(e.message ?: "Não foi possível enviar o pedido.") }
                        }
                    }.start()
                }) { Text(if (sending) "Enviando…" else "Enviar") }
            },
            dismissButton = { TextButton(enabled = !sending, onClick = { dialog = false }) { Text("Cancelar") } }
        )
    }
}

/** Selo de conta verificada (aparece ao lado do nome): selo ondulado azul com check branco. */
@androidx.compose.runtime.Composable
private fun QinblyVerifiedBadge(size: Dp = 16.dp) {
    androidx.compose.foundation.Canvas(Modifier.size(size)) {
        val w = this.size.minDimension
        val c = androidx.compose.ui.geometry.Offset(w / 2f, w / 2f)
        val lobes = 8
        val base = w * 0.45f
        val amp = w * 0.055f
        val path = androidx.compose.ui.graphics.Path()
        val steps = 96
        for (i in 0..steps) {
            val t = (i.toFloat() / steps) * (2f * Math.PI.toFloat())
            val r = base + amp * kotlin.math.cos(lobes * t)
            val x = c.x + r * kotlin.math.cos(t)
            val y = c.y + r * kotlin.math.sin(t)
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        drawPath(path, Color(0xFF1D9BF0))
        val check = androidx.compose.ui.graphics.Path().apply {
            moveTo(w * 0.30f, w * 0.52f); lineTo(w * 0.44f, w * 0.66f); lineTo(w * 0.71f, w * 0.36f)
        }
        drawPath(check, Color.White, style = androidx.compose.ui.graphics.drawscope.Stroke(width = w * 0.11f, cap = androidx.compose.ui.graphics.StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round))
    }
}

/** Texto curto de cada tipo de notificação (usado na lista e na notificação do sistema). */
private fun qinblyNotificationText(kind: String): String = when (kind) {
    "verification_approved" -> "Sua conta foi verificada! Parabéns."
    "verification_rejected" -> "Seu pedido de verificação não foi aprovado."
    "follow" -> "começou a seguir você"
    "like" -> "curtiu sua publicação"
    "comment" -> "comentou na sua publicação"
    "mention" -> "mencionou você em uma publicação"
    "mention_comment" -> "mencionou você em um comentário"
    "tag" -> "marcou você em uma publicação"
    "reply" -> "respondeu ao seu comentário"
    else -> "tem uma novidade para você"
}

/** Notificação do sistema para novidades (aparece enquanto o app está aberto ou ainda em memória). */
private fun qinblyNotify(context: android.content.Context, title: String, text: String, id: Int) {
    try {
        val manager = context.getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        if (android.os.Build.VERSION.SDK_INT >= 26) {
            manager.createNotificationChannel(android.app.NotificationChannel("qinbly_social", "Novidades", android.app.NotificationManager.IMPORTANCE_DEFAULT))
        }
        if (android.os.Build.VERSION.SDK_INT >= 33 &&
            androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) return
        val open = android.app.PendingIntent.getActivity(
            context, 0, android.content.Intent(context, MainActivity::class.java).addFlags(android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP),
            android.app.PendingIntent.FLAG_IMMUTABLE or android.app.PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notification = androidx.core.app.NotificationCompat.Builder(context, "qinbly_social")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        manager.notify(id, notification)
    } catch (_: Exception) { }
}

/** Navegação global para hashtags e menções: qualquer texto clicável abre daqui. */
/** Texto inicial para a próxima publicação (pergunta do dia, desafio da semana). */
private object QinblyComposeSeed { var text: String = "" }

/** Ícones de linha do menu "Criar" (traço de 1,8, pontas arredondadas, grade 24x24). */
private object QinblyCreateIcons {
    private fun build(name: String, vararg paths: String): androidx.compose.ui.graphics.vector.ImageVector {
        val b = androidx.compose.ui.graphics.vector.ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f)
        paths.forEach { d ->
            b.addPath(
                pathData = androidx.compose.ui.graphics.vector.PathParser().parsePathString(d).toNodes(),
                fill = null,
                stroke = androidx.compose.ui.graphics.SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
                strokeLineJoin = androidx.compose.ui.graphics.StrokeJoin.Round
            )
        }
        return b.build()
    }
    val text by lazy { build("criar_texto", "M4 6h10M4 11h7M4 16h5", "m14 19 1-4 6-6a1.4 1.4 0 0 1 2 2l-6 6z") }
    val gallery by lazy { build("criar_galeria", "M6 4h12a3 3 0 0 1 3 3v10a3 3 0 0 1-3 3H6a3 3 0 0 1-3-3V7a3 3 0 0 1 3-3z", "M7.4 10a1.6 1.6 0 1 0 3.2 0a1.6 1.6 0 1 0 -3.2 0", "m3.5 17 5-4.5 4 3.5 3-2.5 5 4") }
    val camera by lazy { build("criar_camera", "M4 8a2 2 0 0 1 2-2h2l1.5-2h5L16 6h2a2 2 0 0 1 2 2v9a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2z", "M8.5 12.5a3.5 3.5 0 1 0 7 0a3.5 3.5 0 1 0 -7 0") }
    val poll by lazy { build("criar_enquete", "M5 20V11M12 20V4M19 20v-6") }
    /** Ícone padrão de figurinhas/emojis em todo o app: carinha com um "+" no canto. */
    val sticker by lazy { build("figurinha", "M19 11a8 8 0 1 0-8 8", "M8.5 9.5h.01M14 9.5h.01", "M8 13.5c1 1.2 2.5 1.2 3.5 0", "M18 14v6M15 17h6") }
}


// ---------- Figurinhas (stickers) ----------
// Uma figurinha viaja como mensagem de texto "::sticker:ID::" e é desenhada como imagem grande em
// mensagens, comunidades e comentários. Figurinhas criadas pelas pessoas dependem do servidor.
private object QinblyStickers {
    private data class P(val d: String, val fill: Long? = null, val stroke: Long? = null, val w: Float = 2f)
    private fun build(name: String, vararg paths: P): androidx.compose.ui.graphics.vector.ImageVector {
        val b = androidx.compose.ui.graphics.vector.ImageVector.Builder(name, 56.dp, 56.dp, 48f, 48f)
        paths.forEach { p ->
            b.addPath(
                pathData = androidx.compose.ui.graphics.vector.PathParser().parsePathString(p.d).toNodes(),
                fill = p.fill?.let { androidx.compose.ui.graphics.SolidColor(Color(it)) },
                stroke = p.stroke?.let { androidx.compose.ui.graphics.SolidColor(Color(it)) },
                strokeLineWidth = if (p.stroke != null) p.w else 0f,
                strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
                strokeLineJoin = androidx.compose.ui.graphics.StrokeJoin.Round
            )
        }
        return b.build()
    }
    private const val FACE = "M6 24a18 18 0 1 0 36 0a18 18 0 1 0 -36 0"
    private const val EYE_L = "M16 21a2 2 0 1 0 4 0a2 2 0 1 0 -4 0"
    private const val EYE_R = "M28 21a2 2 0 1 0 4 0a2 2 0 1 0 -4 0"
    val all: List<Pair<String, androidx.compose.ui.graphics.vector.ImageVector>> by lazy {
        listOf(
            "happy" to build("happy", P(FACE, 0xFFFFD54F, 0xFFB88700), P(EYE_L, 0xFF333333), P(EYE_R, 0xFF333333), P("M16 29c4 6 12 6 16 0", null, 0xFF333333, 2.4f)),
            "heart" to build("heart", P("M24 40S6 29 6 17a9 9 0 0 1 18-3 9 9 0 0 1 18 3c0 12-18 23-18 23z", 0xFF1677E8, 0xFF0E5CB8)),
            "like" to build("like", P("M10 40V20l10-14 4 3-3 11h17a4 4 0 0 1 4 5l-3 12a4 4 0 0 1-4 3z", 0xFFFFB020, 0xFFB87400), P("M4 20h8v20a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2z", 0xFF1677E8, 0xFF0E5CB8)),
            "love" to build("love", P(FACE, 0xFFFF8A80, 0xFFC0392B), P("M14 20c3-4 7-4 10 0", null, 0xFF333333, 2.4f), P("M26 20c3-4 7-4 10 0", null, 0xFF333333, 2.4f), P("M16 32c4-5 12-5 16 0", null, 0xFF333333, 2.4f)),
            "cool" to build("cool", P(FACE, 0xFFFFD54F, 0xFFB88700), P("M10 19h28v3a5 5 0 0 1-5 5h-3a4 4 0 0 1-4-4v-1h-4v1a4 4 0 0 1-4 4h-3a5 5 0 0 1-5-5z", 0xFF222222, 0xFF222222), P("M17 33c4 4 10 4 14 0", null, 0xFF333333, 2.4f)),
            "meh" to build("meh", P(FACE, 0xFF9BE7A8, 0xFF2E8B47), P("M14 21h8M26 21h8", null, 0xFF333333, 2.4f), P("M17 31h14", null, 0xFF333333, 2.4f)),
            "wow" to build("wow", P(FACE, 0xFFC4B5FD, 0xFF6D28D9), P("M14 22a3 3 0 1 0 6 0a3 3 0 1 0 -6 0", 0xFFFFFFFF, 0xFF333333, 1.6f), P("M28 22a3 3 0 1 0 6 0a3 3 0 1 0 -6 0", 0xFFFFFFFF, 0xFF333333, 1.6f), P("M20 32a4 5 0 1 0 8 0a4 5 0 1 0 -8 0", 0xFF333333)),
            "cry" to build("cry", P(FACE, 0xFF93C5FD, 0xFF1D4ED8), P("M14 21c2-2 4-2 6 0M28 21c2-2 4-2 6 0", null, 0xFF333333, 2.4f), P("M17 36c4-4 10-4 14 0", null, 0xFF333333, 2.4f), P("M15 25v8M33 25v8", null, 0xFF1677E8, 2.4f)),
            "star" to build("star", P("M24 4l5 13h14l-11 8 4 14-12-8-12 8 4-14L5 17h14z", 0xFFFFD54F, 0xFFB88700)),
            "fire" to build("fire", P("M24 4c2 8 12 12 12 24a12 12 0 0 1-24 0c0-6 4-8 5-14 3 2 4 5 4 7 2-4 3-9 3-17z", 0xFFFF7A1A, 0xFFC2410C), P("M24 24c1 4 5 5 5 9a5 5 0 0 1-10 0c0-3 3-4 5-9z", 0xFFFFD54F)),
            "ok" to build("ok", P(FACE, 0xFF1677E8, 0xFF0E5CB8), P("M14 25l7 7 14-15", null, 0xFFFFFFFF, 4f)),
            "party" to build("party", P("M6 42l8-24 18 18z", 0xFFFFB020, 0xFFB87400), P("M30 8v4M38 14l-3 2M40 24h-4M26 12l3 3", null, 0xFFE5365B, 2.6f), P("M20 22l8 8", null, 0xFFB87400, 2f))
        )
    }
    fun get(id: String): androidx.compose.ui.graphics.vector.ImageVector? = all.firstOrNull { it.first == id }?.second
    private val token = Regex("^::sticker:([a-z0-9_]+)::$")
    fun idOf(body: String): String? = token.find(body.trim())?.groupValues?.get(1)?.takeIf { get(it) != null }
    fun bodyOf(id: String) = "::sticker:$id::"
}

/** Figurinha desenhada em tamanho grande dentro de um balão/comentário. */
@Composable
private fun QinblyStickerView(id: String, size: Dp = 104.dp) {
    val v = QinblyStickers.get(id) ?: return
    androidx.compose.foundation.Image(v, contentDescription = "Figurinha", modifier = Modifier.size(size))
}

/** Contador de toques na tecla "aumentar volume" do celular; vídeos mudos observam e ligam o som. */
private object QinblyVolumeKey { var upTicks by mutableIntStateOf(0) }

/**
 * Foco dos vídeos do feed: só o vídeo mais visível toca (e só ele recebe o som). Os que saem da tela pausam.
 * Enquanto um vídeo em tela cheia está aberto, nenhum vídeo do feed toca por baixo.
 */
private object QinblyVideoFocus {
    val visible = mutableStateMapOf<String, Float>()
    var fullscreenCount by mutableIntStateOf(0)
    /** Depois que a pessoa liga o som, o próximo vídeo em foco também começa com som. */
    var soundOn by mutableStateOf(false)
    fun focusedKey(): String? {
        if (fullscreenCount > 0) return null
        var best: String? = null
        var bestValue = 0f
        visible.forEach { (key, value) -> if (value > bestValue) { bestValue = value; best = key } }
        return if (bestValue >= 0.4f) best else null
    }
}

private object QinblyTagNav {
    var tag by mutableStateOf<String?>(null)
    var profile by mutableStateOf<String?>(null)
}

/** Contagem curta: 912, 3,1 mil, 1,2 mi. */
private fun qinblyCount(n: Int): String {
    fun one(v: Double) = String.format(java.util.Locale("pt", "BR"), "%.1f", v).removeSuffix(",0")
    return when {
        n >= 1_000_000 -> one(n / 1_000_000.0) + " mi"
        n >= 1_000 -> one(n / 1_000.0) + " mil"
        else -> n.toString()
    }
}
private fun qinblyPostsLabel(n: Int): String = if (n == 1) "1 publicação" else qinblyCount(n) + " publicações"

/** Contagem de hashtags nas publicações do feed já carregado (usada quando o servidor não devolve as mais usadas). */
private fun qinblyFeedTagCounts(): Map<String, Int> =
    QinblyCache.feed.flatMap { p -> Regex("#([\\p{L}\\p{N}_]{2,30})").findAll(p.caption).map { it.groupValues[1].lowercase() }.toList() }
        .groupingBy { it }.eachCount()

/** Histórico das hashtags que a própria pessoa usou (salvo no aparelho): últimas usadas e quantas vezes cada uma. */
private object QinblyHashtagStore {
    private fun prefs(context: android.content.Context) = context.getSharedPreferences("qinbly_hashtags", android.content.Context.MODE_PRIVATE)
    private fun readCounts(context: android.content.Context): LinkedHashMap<String, Int> {
        val out = LinkedHashMap<String, Int>()
        try {
            val o = JSONObject(prefs(context).getString("counts", "{}") ?: "{}")
            val keys = o.keys()
            while (keys.hasNext()) { val k = keys.next(); out[k] = o.optInt(k) }
        } catch (_: Exception) { }
        return out
    }
    fun recent(context: android.content.Context): List<String> = (prefs(context).getString("recent", "") ?: "").split("\n").filter { it.isNotBlank() }
    fun top(context: android.content.Context): List<Pair<String, Int>> = readCounts(context).entries.sortedByDescending { it.value }.map { it.key to it.value }
    fun record(context: android.content.Context, text: String) {
        val tags = Regex("#([\\p{L}\\p{N}_]{2,30})").findAll(text).map { it.groupValues[1].lowercase() }.distinct().toList()
        if (tags.isEmpty()) return
        val counts = readCounts(context)
        tags.forEach { counts[it] = (counts[it] ?: 0) + 1 }
        val recent = (tags + recent(context).filterNot { it in tags }).take(30)
        val o = JSONObject()
        counts.forEach { (k, v) -> o.put(k, v) }
        prefs(context).edit().putString("counts", o.toString()).putString("recent", recent.joinToString("\n")).apply()
    }
    fun clear(context: android.content.Context) { prefs(context).edit().clear().apply() }
}

/** Pinta #hashtags e @menções de azul enquanto a pessoa digita (campos de texto do app). */
private val QinblyTagTransformation = androidx.compose.ui.text.input.VisualTransformation { t ->
    val styled = androidx.compose.ui.text.buildAnnotatedString {
        append(t.text)
        for (m in Regex("[#@][\\p{L}\\p{N}_.]{1,30}").findAll(t.text)) {
            addStyle(androidx.compose.ui.text.SpanStyle(color = QinblyBrand.Accent, fontWeight = FontWeight.Bold), m.range.first, m.range.last + 1)
        }
    }
    androidx.compose.ui.text.input.TransformedText(styled, androidx.compose.ui.text.input.OffsetMapping.Identity)
}

/** Separa a legenda das hashtags que ficam no fim dela (bloco final só de #hashtags). */
private fun qinblySplitCaption(text: String): Pair<String, List<String>> {
    val tags = ArrayList<String>()
    var rest = text.trimEnd()
    val rx = Regex("(?:^|\\s)(#[\\p{L}\\p{N}_]{2,30})$")
    while (true) {
        val m = rx.find(rest) ?: break
        tags.add(0, m.groupValues[1])
        rest = rest.substring(0, m.range.first).trimEnd()
    }
    return rest to tags
}

/** Legenda e hashtags em campos separados; no servidor continuam juntas: legenda, linha em branco e as hashtags. */
private class QinblyCaptionState(initial: String) {
    var legend by mutableStateOf("")
    val tags = mutableStateListOf<String>()
    init {
        val (l, t) = qinblySplitCaption(initial)
        legend = l
        tags.addAll(t)
    }
    fun full(): String = if (tags.isEmpty()) legend else (if (legend.isBlank()) "" else legend.trimEnd() + "\n\n") + tags.joinToString(" ")
}

/** Campo da legenda: hashtag digitada e fechada com espaço vai sozinha para o campo de hashtags. */
@Composable
private fun QinblyLegendField(
    state: QinblyCaptionState, max: Int, placeholder: String, textColor: Color, fontSize: androidx.compose.ui.unit.TextUnit,
    modifier: Modifier = Modifier, onChange: (String) -> Unit
) {
    androidx.compose.foundation.text.BasicTextField(
        value = state.legend,
        onValueChange = { new ->
            var text = new
            val m = Regex("(^|\\s)#([\\p{L}\\p{N}_]{2,30})\\s$").find(text)
            if (m != null && state.tags.size < 30) {
                val tag = "#" + m.groupValues[2]
                if (state.tags.none { it.equals(tag, true) }) state.tags.add(tag)
                text = text.substring(0, m.range.first) + (if (m.range.first > 0) " " else "")
            }
            state.legend = text.take(max)
            onChange(state.full())
        },
        textStyle = androidx.compose.ui.text.TextStyle(color = textColor, fontSize = fontSize, lineHeight = fontSize * 1.4f),
        cursorBrush = androidx.compose.ui.graphics.SolidColor(QinblyBrand.Accent),
        visualTransformation = QinblyTagTransformation,
        modifier = modifier,
        decorationBox = { inner ->
            if (state.legend.isEmpty()) Text(placeholder, color = Color.Gray, fontSize = fontSize)
            inner()
        }
    )
}

/** Campo de hashtags: bolinhas azuis com ×, sugestões com contagem e as últimas hashtags usadas. */
@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun QinblyHashtagField(state: QinblyCaptionState, onChange: (String) -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var draft by remember { mutableStateOf("") }
    var trending by remember { mutableStateOf<List<Pair<String, Int>>>(emptyList()) }
    LaunchedEffect(Unit) {
        Thread {
            try {
                val t = QinblyApi.getTrendingTags()
                android.os.Handler(android.os.Looper.getMainLooper()).post { trending = t }
            } catch (_: Exception) { }
        }.start()
    }
    val mine = remember { QinblyHashtagStore.top(context) }
    val recent = remember { QinblyHashtagStore.recent(context) }
    val counts = remember(trending) {
        val m = LinkedHashMap<String, Int>()
        qinblyFeedTagCounts().forEach { (k, v) -> m[k] = v }
        trending.forEach { (k, v) -> m[k.lowercase()] = v }
        m
    }
    fun add(raw: String) {
        val clean = raw.trim().removePrefix("#").filter { it.isLetterOrDigit() || it == '_' }.take(30)
        if (clean.length < 2 || state.tags.size >= 30) return
        val tag = "#$clean"
        if (state.tags.none { it.equals(tag, true) }) { state.tags.add(tag); onChange(state.full()) }
    }
    val partial = draft.trim().removePrefix("#").lowercase()
    Column(Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(QinblyBrand.Accent.copy(alpha = 0.06f)).padding(12.dp)) {
            Row(Modifier.fillMaxWidth()) {
                Text("Hashtags", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.weight(1f))
                Text("${state.tags.size} / 30", color = Color.Gray, fontSize = 12.sp)
            }
            Spacer(Modifier.height(6.dp))
            androidx.compose.foundation.layout.FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                state.tags.toList().forEach { tag ->
                    Row(
                        Modifier.clip(RoundedCornerShape(14.dp)).background(QiblyTheme.card).border(1.dp, QinblyBrand.AccentLine, RoundedCornerShape(14.dp))
                            .padding(start = 11.dp, end = 6.dp, top = 4.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(tag, color = QinblyBrand.Accent, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Icon(Icons.Default.Close, contentDescription = "Remover $tag", tint = Color.Gray,
                            modifier = Modifier.padding(start = 4.dp).size(16.dp).clickable { state.tags.remove(tag); onChange(state.full()) })
                    }
                }
                androidx.compose.foundation.text.BasicTextField(
                    value = draft,
                    onValueChange = { v -> if (v.endsWith(" ") || v.endsWith(",") || v.endsWith("\n")) { add(v); draft = "" } else draft = v.take(31) },
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(color = QinblyBrand.Accent, fontSize = 14.sp, fontWeight = FontWeight.Bold),
                    cursorBrush = androidx.compose.ui.graphics.SolidColor(QinblyBrand.Accent),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Done),
                    keyboardActions = androidx.compose.foundation.text.KeyboardActions(onDone = { add(draft); draft = "" }),
                    modifier = Modifier.widthIn(min = 130.dp),
                    decorationBox = { inner ->
                        Box(Modifier.padding(vertical = 4.dp)) {
                            if (draft.isEmpty()) Text("# adicionar hashtag", color = Color.Gray, fontSize = 14.sp)
                            inner()
                        }
                    }
                )
            }
        }
        if (partial.isNotEmpty()) {
            val names = (counts.keys + mine.map { it.first }).distinct()
                .filter { it.startsWith(partial) && state.tags.none { t -> t.equals("#$it", true) } }
                .sortedByDescending { counts[it] ?: 0 }.take(5)
            names.forEach { name ->
                Row(Modifier.fillMaxWidth().clickable { add(name); draft = "" }.padding(horizontal = 2.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("#$name", color = QinblyBrand.Accent, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, modifier = Modifier.weight(1f))
                    val n = counts[name]
                    Text(if (n != null) qinblyPostsLabel(n) else "Nova hashtag", color = Color.Gray, fontSize = 13.sp)
                }
            }
        } else {
            val suggestions = recent.filter { r -> state.tags.none { it.equals("#$r", true) } }.take(8)
            if (suggestions.isNotEmpty()) {
                Text("Suas últimas hashtags", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(top = 10.dp, bottom = 6.dp))
                androidx.compose.foundation.layout.FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    suggestions.forEach { r ->
                        Text("#$r", color = QinblyBrand.Accent, fontWeight = FontWeight.SemiBold, fontSize = 13.sp,
                            modifier = Modifier.clip(RoundedCornerShape(14.dp)).background(QinblyBrand.AccentSoft).clickable { add(r) }.padding(horizontal = 11.dp, vertical = 5.dp))
                    }
                }
            }
        }
    }
}

/** Legenda no card do feed: hashtags logo abaixo; com muitas hashtags, mostra as primeiras e um "mais". */
@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun QinblyFeedCaption(caption: String, modifier: Modifier = Modifier) {
    val (legend, tags) = remember(caption) { qinblySplitCaption(caption) }
    var open by remember(caption) { mutableStateOf(false) }
    val limit = 4
    Column(modifier, verticalArrangement = Arrangement.spacedBy(3.dp)) {
        if (legend.isNotBlank()) QinblyRichText(legend, RemoteAppearance.feedTextColor, fontSize = 15.sp)
        if (tags.isNotEmpty()) {
            val shown = if (open || tags.size <= limit) tags else tags.take(limit)
            androidx.compose.foundation.layout.FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                shown.forEach { t -> Text(t, color = QinblyBrand.Accent, fontWeight = FontWeight.Bold, fontSize = 15.sp, modifier = Modifier.clickable { QinblyTagNav.tag = t.removePrefix("#") }) }
                if (!open && tags.size > limit) Text("mais", color = RemoteAppearance.feedSecondaryTextColor, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, modifier = Modifier.clickable { open = true })
                if (open && tags.size > limit) Text("menos", color = RemoteAppearance.feedSecondaryTextColor, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, modifier = Modifier.clickable { open = false })
            }
        }
    }
}

/** Legenda sobre o vídeo no visualizador: as hashtags ficam escondidas atrás do "mais". */
@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun QinblyViewerCaption(caption: String) {
    val (legend, tags) = remember(caption) { qinblySplitCaption(caption) }
    var open by remember(caption) { mutableStateOf(false) }
    val showTags = tags.isNotEmpty() && (open || legend.isBlank())
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        if (legend.isNotBlank()) {
            Row(verticalAlignment = Alignment.Bottom) {
                QinblyRichText(legend, Color.White, modifier = Modifier.weight(1f, fill = false), fontSize = 17.sp, maxLines = if (open) Int.MAX_VALUE else 3)
                if (tags.isNotEmpty() && !open) Text("  mais", color = Color(0xCCFFFFFF), fontWeight = FontWeight.Bold, fontSize = 15.sp, modifier = Modifier.clickable { open = true })
            }
        }
        if (showTags) {
            androidx.compose.foundation.layout.FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                tags.forEach { t -> Text(t, color = Color(0xFF5AA7FF), fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.clickable { QinblyTagNav.tag = t.removePrefix("#") }) }
            }
            if (legend.isNotBlank()) Text("menos", color = Color(0xCCFFFFFF), fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.clickable { open = false })
        }
    }
}

/** Texto com #hashtags e @menções clicáveis. */
@Composable
private fun QinblyRichText(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    fontSize: androidx.compose.ui.unit.TextUnit = 15.sp,
    fontWeight: FontWeight? = null,
    maxLines: Int = Int.MAX_VALUE,
    onLongClick: (() -> Unit)? = null
) {
    val link = QinblyBrand.Accent
    val annotated = remember(text, color, link) {
        androidx.compose.ui.text.buildAnnotatedString {
            val rx = Regex("([#@])([\\p{L}\\p{N}_.]{2,30})")
            var last = 0
            for (match in rx.findAll(text)) {
                var token = match.value
                if (token.endsWith(".")) token = token.trimEnd('.')
                if (token.length < 3) continue
                append(text.substring(last, match.range.first))
                pushStringAnnotation(tag = token.take(1), annotation = token.drop(1))
                pushStyle(androidx.compose.ui.text.SpanStyle(color = link, fontWeight = FontWeight.Bold))
                append(token)
                pop()
                pop()
                last = match.range.first + token.length
            }
            append(text.substring(last))
        }
    }
    if (onLongClick != null) {
        var layout by remember { mutableStateOf<androidx.compose.ui.text.TextLayoutResult?>(null) }
        Text(
            text = annotated,
            modifier = modifier.pointerInput(annotated) {
                detectTapGestures(
                    onLongPress = { onLongClick() },
                    onTap = { pos ->
                        val offset = layout?.getOffsetForPosition(pos) ?: return@detectTapGestures
                        annotated.getStringAnnotations("#", offset, offset).firstOrNull()?.let { QinblyTagNav.tag = it.item }
                            ?: annotated.getStringAnnotations("@", offset, offset).firstOrNull()?.let { QinblyTagNav.profile = it.item }
                    }
                )
            },
            maxLines = maxLines,
            onTextLayout = { layout = it },
            style = androidx.compose.ui.text.TextStyle(color = color, fontSize = fontSize, fontWeight = fontWeight, lineHeight = fontSize * 1.35f)
        )
        return
    }
    androidx.compose.foundation.text.ClickableText(
        text = annotated,
        modifier = modifier,
        maxLines = maxLines,
        style = androidx.compose.ui.text.TextStyle(color = color, fontSize = fontSize, fontWeight = fontWeight, lineHeight = fontSize * 1.35f),
        onClick = { offset ->
            annotated.getStringAnnotations("#", offset, offset).firstOrNull()?.let { QinblyTagNav.tag = it.item }
                ?: annotated.getStringAnnotations("@", offset, offset).firstOrNull()?.let { QinblyTagNav.profile = it.item }
        }
    )
}

/** Tela de uma hashtag: todas as publicações públicas que a usam, com o mesmo card do resto do app. */
@Composable
private fun QinblyTagHost(prefs: android.content.SharedPreferences, currentUsername: String, onNotice: (String) -> Unit) {
    val tag = QinblyTagNav.tag ?: return
    var posts by remember(tag) { mutableStateOf<List<QinblyApi.PostSummary>>(emptyList()) }
    var loading by remember(tag) { mutableStateOf(true) }
    var viewerPostId by remember(tag) { mutableStateOf<String?>(null) }
    var commentTarget by remember(tag) { mutableStateOf<QinblyApi.PostSummary?>(null) }
    var shareTarget by remember(tag) { mutableStateOf<QinblyApi.PostSummary?>(null) }
    LaunchedEffect(tag) {
        Thread {
            try {
                val loaded = QinblyApi.getTagPosts(tag)
                android.os.Handler(android.os.Looper.getMainLooper()).post { QinblyEngagement.learnFromPosts(prefs, loaded); posts = loaded; loading = false }
            } catch (e: Exception) {
                android.os.Handler(android.os.Looper.getMainLooper()).post { loading = false; onNotice(e.message ?: "Não foi possível abrir a hashtag.") }
            }
        }.start()
    }
    androidx.compose.ui.window.Dialog(
        onDismissRequest = { QinblyTagNav.tag = null },
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(Modifier.fillMaxSize(), color = RemoteAppearance.screenBackgroundColor) {
            Column(Modifier.fillMaxSize()) {
                Row(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 8.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { QinblyTagNav.tag = null }) { Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = RemoteAppearance.screenTextColor) }
                    Column(Modifier.weight(1f)) {
                        Text("#$tag", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = QinblyBrand.Accent)
                        if (!loading) Text(qinblyPostsLabel(posts.size), color = RemoteAppearance.screenSecondaryTextColor, style = MaterialTheme.typography.bodySmall)
                    }
                }
                when {
                    loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Pink) }
                    posts.isEmpty() -> Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("Ainda não há publicações com #$tag.", color = RemoteAppearance.screenSecondaryTextColor, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                    else -> LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(posts, key = { it.id }) { post ->
                            QinblyPostCard(
                                post = post,
                                prefs = prefs,
                                nameFontStyle = "",
                                onNotice = onNotice,
                                onOpenProfile = { QinblyTagNav.tag = null; QinblyTagNav.profile = it },
                                onOpenViewer = { viewerPostId = it.id },
                                onComment = { commentTarget = it },
                                onShare = { shareTarget = it },
                                onPostsTransform = { change -> posts = change(posts) },
                                onRefresh = { }
                            )
                        }
                    }
                }
            }
        }
        viewerPostId?.let { startId ->
            val viewable = qinblyViewerList(posts.filter { it.mediaType != "poll" && (it.mediaUrl.isNotBlank() || it.mediaUrls.isNotEmpty()) }, startId)
            if (viewable.any { it.id == startId }) {
                QinblyPostViewer(
                    posts = viewable, startPostId = startId, prefs = prefs, currentUsername = currentUsername, onNotice = onNotice,
                    onDismiss = { viewerPostId = null }, onComment = { commentTarget = it }, onShare = { shareTarget = it },
                    onOpenProfile = { viewerPostId = null; QinblyTagNav.tag = null; QinblyTagNav.profile = it }
                )
            }
        }
        commentTarget?.let { target ->
            QinblyCommentsDialog(
                post = target, prefs = prefs, currentUsername = currentUsername, onNotice = onNotice,
                onDismiss = { commentTarget = null },
                onCountChanged = { postId, count -> posts = posts.map { if (it.id == postId) it.copy(comments = count) else it } }
            )
        }
        shareTarget?.let { target -> QiblyShareDialog(post = target, prefs = prefs, onDismiss = { shareTarget = null }, onNotice = onNotice) }
    }
}

private object QinblyDeepLink {
    var postId by mutableStateOf<String?>(null)
    private val postPattern = Regex("post/([0-9a-fA-F-]{36})")
    fun handle(intent: android.content.Intent?) {
        val data = intent?.data?.toString() ?: return
        postPattern.find(data)?.groupValues?.get(1)?.let { postId = it }
    }
}

@Composable
private fun QinblyDeepLinkHost(prefs: android.content.SharedPreferences, currentUsername: String, onNotice: (String) -> Unit) {
    val id = QinblyDeepLink.postId ?: return
    var post by remember(id) { mutableStateOf<QinblyApi.PostSummary?>(null) }
    var commentTarget by remember(id) { mutableStateOf<QinblyApi.PostSummary?>(null) }
    var shareTarget by remember(id) { mutableStateOf<QinblyApi.PostSummary?>(null) }
    LaunchedEffect(id) {
        Thread {
            try {
                val loaded = QinblyApi.getPost(id)
                android.os.Handler(android.os.Looper.getMainLooper()).post { post = loaded }
            } catch (e: Exception) {
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    QinblyDeepLink.postId = null
                    onNotice(e.message ?: "Não foi possível abrir esta publicação.")
                }
            }
        }.start()
    }
    post?.let { shown ->
        QinblyPostViewer(
            posts = listOf(shown),
            startPostId = shown.id,
            prefs = prefs,
            currentUsername = currentUsername,
            onNotice = onNotice,
            onDismiss = { QinblyDeepLink.postId = null },
            onComment = { commentTarget = it },
            onShare = { shareTarget = it },
            onOpenProfile = { QinblyDeepLink.postId = null }
        )
        commentTarget?.let { target ->
            QinblyCommentsDialog(
                post = target,
                prefs = prefs,
                currentUsername = currentUsername,
                onNotice = onNotice,
                onDismiss = { commentTarget = null },
                onCountChanged = { postId, count -> post = post?.let { if (it.id == postId) it.copy(comments = count) else it } }
            )
        }
        shareTarget?.let { target ->
            QiblyShareDialog(post = target, prefs = prefs, onDismiss = { shareTarget = null }, onNotice = onNotice)
        }
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

    val matrix = qinblyFilterMatrix(filter)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
        colorFilter = ColorMatrixColorFilter(matrix)
    }
    canvas.drawBitmap(rotated, null, android.graphics.RectF(left, top, left + drawWidth, top + drawHeight), paint)
    if (!rotated.isRecycled) rotated.recycle()

    val file = File(context.cacheDir, "qinbly-adjusted-${System.currentTimeMillis()}.jpg")
    FileOutputStream(file).use { output.compress(Bitmap.CompressFormat.JPEG, 92, it) }
    output.recycle()
    return Uri.fromFile(file)
}

private val QinblyTextFonts = listOf("Forte", "Larga", "Leve", "Fina", "Compacta", "Clássica", "Elegante", "Máquina", "Divertida", "Manual")
private val QinblyTextColors = listOf(0xFFFFFFFF, 0xFF111111, 0xFFFFD23F, 0xFFFF8A3D, 0xFFFF3B30, 0xFFFF5A8A, 0xFFC026D3, 0xFF7B3FF2, 0xFF2F7BFF, 0xFF19D3C5, 0xFF34C759, 0xFFC8F560).map { it.toInt() }

private fun qinblyTypeface(font: String): android.graphics.Typeface = when (font) {
    "Forte", "Larga" -> android.graphics.Typeface.create("sans-serif-black", android.graphics.Typeface.NORMAL)
    "Leve" -> android.graphics.Typeface.create("sans-serif-light", android.graphics.Typeface.NORMAL)
    "Fina" -> android.graphics.Typeface.create("sans-serif-thin", android.graphics.Typeface.NORMAL)
    "Compacta" -> android.graphics.Typeface.create("sans-serif-condensed", android.graphics.Typeface.BOLD)
    "Clássica" -> android.graphics.Typeface.create("serif", android.graphics.Typeface.ITALIC)
    "Elegante" -> android.graphics.Typeface.create("serif", android.graphics.Typeface.BOLD)
    "Máquina" -> android.graphics.Typeface.create("monospace", android.graphics.Typeface.BOLD)
    "Divertida" -> android.graphics.Typeface.create("casual", android.graphics.Typeface.NORMAL)
    else -> android.graphics.Typeface.create("cursive", android.graphics.Typeface.NORMAL)
}
private fun qinblyTextSpacingEm(font: String): Float = if (font == "Larga") 0.16f else 0f
private fun qinblyStyledText(font: String, text: String): String = if (font == "Larga") text.uppercase() else text

private fun qinblyIsDarkColor(c: Int): Boolean = android.graphics.Color.red(c) + android.graphics.Color.green(c) + android.graphics.Color.blue(c) < 150
private fun qinblyTextWords(text: String): List<String> = text.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }

/** Texto com cor própria por palavra (palavras sem cor própria usam a cor base). */
private fun qinblyColoredText(font: String, text: String, base: Int, wordColors: Map<Int, Int>): androidx.compose.ui.text.AnnotatedString {
    val t = qinblyStyledText(font, text)
    return androidx.compose.ui.text.buildAnnotatedString {
        append(t)
        var i = 0
        Regex("\\S+").findAll(t).forEach { m ->
            val c = wordColors[i] ?: base
            i++
            addStyle(
                androidx.compose.ui.text.SpanStyle(color = Color(c), shadow = androidx.compose.ui.graphics.Shadow(if (qinblyIsDarkColor(c)) Color(0xCCFFFFFF) else Color(0xCC000000), blurRadius = 8f)),
                m.range.first, m.range.last + 1
            )
        }
    }
}

/** Fileiras de palavras, fontes e cores do texto sobre a foto (câmera e editor). */
@Composable
private fun QinblyTextStyleBar(
    font: String, color: Int, onFont: (String) -> Unit, onColor: (Int) -> Unit,
    words: List<String> = emptyList(), wordColors: Map<Int, Int> = emptyMap(), selWord: Int = -1, onSelWord: (Int) -> Unit = {}
) {
    val activeColor = if (selWord in words.indices) (wordColors[selWord] ?: color) else color
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (words.size > 1) {
            LazyRow(contentPadding = PaddingValues(horizontal = 14.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    val all = selWord !in words.indices
                    Text("Todas", color = if (all) Color(0xFF111111) else Color.White, fontSize = 13.sp,
                        modifier = Modifier.clip(RoundedCornerShape(18.dp)).background(if (all) Color.White else Color(0x33000000))
                            .border(0.5.dp, Color(0xB3FFFFFF), RoundedCornerShape(18.dp))
                            .clickable { onSelWord(-1) }.padding(horizontal = 14.dp, vertical = 6.dp))
                }
                items(words.size) { i ->
                    val chosen = i == selWord
                    Text(words[i], color = Color(wordColors[i] ?: color), fontSize = 13.sp, maxLines = 1,
                        modifier = Modifier.clip(RoundedCornerShape(18.dp)).background(Color(0x66000000))
                            .border(if (chosen) 2.dp else 0.5.dp, if (chosen) Color.White else Color(0xB3FFFFFF), RoundedCornerShape(18.dp))
                            .clickable { onSelWord(i) }.padding(horizontal = 14.dp, vertical = 6.dp))
                }
            }
        }
        LazyRow(contentPadding = PaddingValues(horizontal = 14.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(QinblyTextFonts) { name ->
                val chosen = name == font
                Text(
                    qinblyStyledText(name, name),
                    color = if (chosen) Color(0xFF111111) else Color.White,
                    fontFamily = androidx.compose.ui.text.font.FontFamily(qinblyTypeface(name)),
                    letterSpacing = qinblyTextSpacingEm(name).em,
                    fontSize = 14.sp,
                    modifier = Modifier.clip(RoundedCornerShape(18.dp))
                        .background(if (chosen) Color.White else Color(0x33000000))
                        .border(0.5.dp, Color(0xB3FFFFFF), RoundedCornerShape(18.dp))
                        .clickable { onFont(name) }.padding(horizontal = 14.dp, vertical = 7.dp)
                )
            }
        }
        LazyRow(contentPadding = PaddingValues(horizontal = 14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(QinblyTextColors) { argb ->
                val chosen = argb == activeColor
                Box(
                    Modifier.size(28.dp).clip(CircleShape).background(Color(argb))
                        .border(if (chosen) 3.dp else 1.dp, if (chosen) QinblyBrand.Accent else Color(0x99FFFFFF), CircleShape)
                        .clickable { onColor(argb) }
                )
            }
        }
    }
}

/** Matriz de cor final da foto: filtro + brilho (-1..1) + contraste (-0,5..0,5). Usada na prévia e no arquivo final. */
private fun qinblyEditMatrix(filter: String, intensity: Float, brightness: Float, contrast: Float): ColorMatrix {
    val m = ColorMatrix(qinblyFilterMatrix(filter, intensity))
    val c = 1f + contrast
    val t = (1f - c) * 127.5f + brightness * 100f
    m.postConcat(ColorMatrix(floatArrayOf(c, 0f, 0f, 0f, t, 0f, c, 0f, 0f, t, 0f, 0f, c, 0f, t, 0f, 0f, 0f, 1f, 0f)))
    return m
}

private fun qinblyRatioValue(ratio: String, imageRatio: Float): Float = when (ratio) {
    "1:1" -> 1f
    "4:5" -> 4f / 5f
    "16:9" -> 16f / 9f
    "9:16" -> 9f / 16f
    else -> imageRatio
}

/** Quanto a foto pode andar (fração do quadro) em cada eixo sem mostrar borda vazia. */
private fun qinblyMaxPan(imageRatio: Float, frameRatio: Float, zoom: Float): Pair<Float, Float> {
    val drawW = (if (imageRatio > frameRatio) imageRatio / frameRatio else 1f) * zoom
    val drawH = (if (imageRatio > frameRatio) 1f else frameRatio / imageRatio) * zoom
    return maxOf(0f, (drawW - 1f) / 2f) to maxOf(0f, (drawH - 1f) / 2f)
}

/** Gira (90° por vez) e/ou espelha a foto e salva uma cópia; a prévia e o recorte passam a usar essa cópia. */
private fun qinblyTransformCopy(context: android.content.Context, source: Uri, rotate90: Boolean, mirror: Boolean): Uri {
    val decoded = context.contentResolver.openInputStream(source)?.use { BitmapFactory.decodeStream(it) }
        ?: throw IllegalArgumentException("Não foi possível abrir a imagem.")
    val matrix = android.graphics.Matrix()
    if (rotate90) matrix.postRotate(90f)
    if (mirror) matrix.postScale(-1f, 1f, decoded.width / 2f, decoded.height / 2f)
    val out = Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, matrix, true)
    val file = File(context.cacheDir, "qinbly-edit-${System.currentTimeMillis()}.jpg")
    FileOutputStream(file).use { out.compress(Bitmap.CompressFormat.JPEG, 95, it) }
    if (out !== decoded) decoded.recycle()
    out.recycle()
    return Uri.fromFile(file)
}

/** Pontes para a tela de vídeo de fotos (PhotoVideoEditor.kt), que fica em outro arquivo. */
internal fun qinblyPvFilterNames(): List<String> = QinblyColorFilterNames
internal fun qinblyPvFilterMatrix(name: String): FloatArray = qinblyFilterMatrix(name, 1f).array
internal fun qinblyPvBake(context: android.content.Context, source: Uri, filter: String): Uri =
    qinblyRenderEditedPhoto(context, source, "9:16", 1f, 0f, 0f, qinblyEditMatrix(filter, 1f, 0f, 0f), "", "", emptyList())

/** Gera a foto final: recorte (proporção, zoom e posição), filtro, brilho/contraste e depois texto, sticker e desenho. */
private fun qinblyRenderEditedPhoto(
    context: android.content.Context, source: Uri, ratio: String, zoom: Float, panX: Float, panY: Float,
    matrix: ColorMatrix, text: String, sticker: String, strokes: List<List<Offset>>, textFont: String = "Forte", textColor: Int = android.graphics.Color.WHITE, wordColors: Map<Int, Int> = emptyMap()
): Uri {
    val decoded = context.contentResolver.openInputStream(source)?.use { BitmapFactory.decodeStream(it) }
        ?: throw IllegalArgumentException("Não foi possível abrir a imagem para aplicar as edições.")
    val imageRatio = decoded.width.toFloat() / decoded.height
    val frameRatio = qinblyRatioValue(ratio, imageRatio)
    val outW = decoded.width.coerceIn(720, 1600)
    val outH = (outW / frameRatio).toInt().coerceAtLeast(1)
    val output = Bitmap.createBitmap(outW, outH, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(output)
    canvas.drawColor(android.graphics.Color.BLACK)
    val base = maxOf(outW.toFloat() / decoded.width, outH.toFloat() / decoded.height)
    val drawW = decoded.width * base * zoom
    val drawH = decoded.height * base * zoom
    val (maxX, maxY) = qinblyMaxPan(imageRatio, frameRatio, zoom)
    val left = (outW - drawW) / 2f + panX.coerceIn(-maxX, maxX) * outW
    val top = (outH - drawH) / 2f + panY.coerceIn(-maxY, maxY) * outH
    val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply { colorFilter = ColorMatrixColorFilter(matrix) }
    canvas.drawBitmap(decoded, null, android.graphics.RectF(left, top, left + drawW, top + drawH), paint)
    decoded.recycle()
    val file = File(context.cacheDir, "qinbly-final-${System.currentTimeMillis()}.jpg")
    FileOutputStream(file).use { output.compress(Bitmap.CompressFormat.JPEG, 93, it) }
    output.recycle()
    return createEditorOverlayImage(context, Uri.fromFile(file), text, sticker, strokes, textFont, textColor, wordColors)
}

private fun createEditorOverlayImage(context: android.content.Context, source: Uri, overlayText: String, sticker: String, strokes: List<List<Offset>> = emptyList(), textFont: String = "Forte", textColor: Int = android.graphics.Color.WHITE, wordColors: Map<Int, Int> = emptyMap()): Uri {
    if (overlayText.isBlank() && sticker.isBlank() && strokes.isEmpty()) return source
    val bitmap = context.contentResolver.openInputStream(source)?.use { BitmapFactory.decodeStream(it) }
        ?.let { decoded -> decoded.copy(Bitmap.Config.ARGB_8888, true).also { if (it !== decoded) decoded.recycle() } }
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
        textPaint.typeface = qinblyTypeface(textFont)
        textPaint.letterSpacing = qinblyTextSpacingEm(textFont)
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.textSize = (bitmap.width * 0.075f).coerceIn(32f, 150f)
        val maxWidth = bitmap.width * 0.86f
        val words = qinblyTextWords(qinblyStyledText(textFont, overlayText))
        val lines = mutableListOf<List<Pair<String, Int>>>()
        var current = mutableListOf<Pair<String, Int>>()
        words.forEachIndexed { idx, word ->
            val candidate = (current.map { it.first } + word).joinToString(" ")
            if (textPaint.measureText(candidate) > maxWidth && current.isNotEmpty()) {
                lines.add(current)
                current = mutableListOf(word to idx)
            } else current.add(word to idx)
        }
        if (current.isNotEmpty()) lines.add(current)
        val lineHeight = textPaint.textSize * 1.25f
        val startY = bitmap.height * 0.5f - (lines.size - 1) * lineHeight / 2f
        val spaceW = textPaint.measureText(" ")
        lines.forEachIndexed { index, line ->
            val total = textPaint.measureText(line.joinToString(" ") { it.first })
            var x = (bitmap.width - total) / 2f
            line.forEach { (word, idx) ->
                val c = wordColors[idx] ?: textColor
                textPaint.color = c
                textPaint.setShadowLayer(8f, 2f, 3f, if (qinblyIsDarkColor(c)) android.graphics.Color.WHITE else android.graphics.Color.BLACK)
                canvas.drawText(word, x, startY + index * lineHeight, textPaint)
                x += textPaint.measureText(word) + spaceW
            }
        }
    }
    val file = File(context.cacheDir, "qinbly-editor-${System.currentTimeMillis()}.jpg")
    FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.JPEG, 94, it) }
    bitmap.recycle()
    return Uri.fromFile(file)
}

private data class SavedQinblyAccount(val username: String, val token: String, val displayName: String = "", val bio: String = "", val refreshToken: String = "")
private fun savedQinblyAccounts(prefs: android.content.SharedPreferences): List<SavedQinblyAccount> = try {
 val a=JSONArray(prefs.getString("qinbly_accounts","[]") ?: "[]")
 (0 until a.length()).mapNotNull { i -> val o=a.optJSONObject(i) ?: return@mapNotNull null; val u=o.optString("username"); val t=o.optString("token"); if(u.isBlank()||t.isBlank()) null else SavedQinblyAccount(u,t,o.optString("displayName"),o.optString("bio"),o.optString("refreshToken")) }.distinctBy { it.username.lowercase() }
} catch (_:Exception) { emptyList() }
private fun rememberQinblyAccount(prefs: android.content.SharedPreferences, username:String, token:String, displayName:String="", bio:String="", refreshToken:String=(prefs.getString("refresh_token","") ?: "")) {
 if(username.isBlank()||token.isBlank()) return
 val list=savedQinblyAccounts(prefs).filterNot { it.username.equals(username,true) }.toMutableList()
 list.add(0,SavedQinblyAccount(username,token,displayName,bio,refreshToken))
 val a=JSONArray()
 list.take(8).forEach { a.put(JSONObject().put("username",it.username).put("token",it.token).put("displayName",it.displayName).put("bio",it.bio).put("refreshToken",it.refreshToken)) }
 prefs.edit().putString("qinbly_accounts",a.toString()).apply()
}
@Composable
private fun AccountSwitcherDialog(
 accounts: List<SavedQinblyAccount>,
 currentUsername: String,
 onDismiss: () -> Unit,
 onSelect: (SavedQinblyAccount) -> Unit,
 onAddAccount: () -> Unit,
 onRemove: (String) -> Unit
) {
 var confirmRemove by remember { mutableStateOf("") }
 val blue = QinblyBrand.Accent
 androidx.compose.ui.window.Dialog(
  onDismissRequest = onDismiss,
  properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
 ) {
  Box(Modifier.fillMaxSize().padding(bottom = 92.dp), contentAlignment = Alignment.BottomCenter) {
   Surface(
    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
    color = QiblyTheme.card,
    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
   ) {
   Column(Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 18.dp)) {
    Spacer(Modifier.height(12.dp))
    Box(Modifier.align(Alignment.CenterHorizontally).size(width = 42.dp, height = 4.dp).background(Color(0xFFD0D5DD), RoundedCornerShape(50)))
    Spacer(Modifier.height(12.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
     IconButton(onClick = onDismiss) { Icon(Icons.Default.ArrowBack, "Voltar", tint = QiblyTheme.text) }
     Column(Modifier.weight(1f)) {
      Text("Trocar de conta", fontSize = 25.sp, fontWeight = FontWeight.Bold, color = QiblyTheme.text)
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
       color = if (active) QiblyTheme.soft else QiblyTheme.card,
       shape = RoundedCornerShape(17.dp),
       border = BorderStroke(if (active) 1.5.dp else 1.dp, if (active) blue else QiblyTheme.border)
      ) {
       Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
        QiblyAccountAvatar(account.username, "", 66.dp) { if (!active) onSelect(account) }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
         Text(account.displayName.ifBlank { account.username }, color = QiblyTheme.text, fontWeight = FontWeight.Bold, fontSize = 18.sp, maxLines = 1)
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
      border = BorderStroke(1.dp, QiblyTheme.border)
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
        shape = RoundedCornerShape(28.dp),
   onDismissRequest = { confirmRemove = "" },
   title = { Text("Remover conta?") },
   text = { Text("Tem certeza de que deseja remover esta conta da lista neste aparelho?") },
   confirmButton = {
    TextButton(onClick = {
     val target = confirmRemove
     confirmRemove = ""
     onRemove(target)

    }) { Text("Remover", color = QiblyTheme.secondary) }
   },
   dismissButton = { TextButton(onClick = { confirmRemove = "" }) { Text("Cancelar") } },
   containerColor = QiblyTheme.card,
   titleContentColor = QiblyTheme.text,
   textContentColor = QiblyTheme.secondary
  )
 }
}

private object QiblyTheme {
    var dark by androidx.compose.runtime.mutableStateOf(true)
    var largeText by androidx.compose.runtime.mutableStateOf(false)
    var reduceMotion by androidx.compose.runtime.mutableStateOf(false)
    // Tema escuro "azul-noite": fundo azul bem escuro, cartões um tom mais claros e texto sempre legível.
    val background: Color get() = if (dark) Color(0xFF130F1D) else Color(0xFFFBF9FF)
    val card: Color get() = if (dark) Color(0xFF1D1830) else Color.White
    val soft: Color get() = if (dark) Color(0xFF282240) else Color(0xFFF3F0FA)
    val text: Color get() = if (dark) Color(0xFFF2F4F8) else Color.Black
    val secondary: Color get() = if (dark) Color(0xFFA9B5C8) else Color(0xFF555555)
    val border: Color get() = if (dark) Color(0xFF332C4D) else Color(0xFFE9E4F5)
}

private object RemoteAppearance {
    private var feedTextColorBase by androidx.compose.runtime.mutableStateOf(Color.White)
    var feedTextColor: Color
        get() = if (QiblyTheme.dark) Color.White else feedTextColorBase
        set(value) { feedTextColorBase = value }
    private var feedSecondaryTextColorBase by androidx.compose.runtime.mutableStateOf(Color(0xFFADB2CB))
    var feedSecondaryTextColor: Color
        get() = if (QiblyTheme.dark) QiblyTheme.secondary else feedSecondaryTextColorBase
        set(value) { feedSecondaryTextColorBase = value }
    private var feedActionBackgroundColorBase by androidx.compose.runtime.mutableStateOf(Color(0x66030711))
    var feedActionBackgroundColor: Color
        get() = if (QiblyTheme.dark) QiblyTheme.background else feedActionBackgroundColorBase
        set(value) { feedActionBackgroundColorBase = value }
    private var feedDividerColorBase by androidx.compose.runtime.mutableStateOf(Color(0xFF252525))
    var feedDividerColor: Color
        get() = if (QiblyTheme.dark) QiblyTheme.border else feedDividerColorBase
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
        get() = if (QiblyTheme.dark) QiblyTheme.text else screenTextColorBase
        set(value) { screenTextColorBase = value }
    private var screenSecondaryTextColorBase by androidx.compose.runtime.mutableStateOf(Color(0xFF333333))
    var screenSecondaryTextColor: Color
        get() = if (QiblyTheme.dark) QiblyTheme.secondary else screenSecondaryTextColorBase
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
        get() = if (QiblyTheme.dark) QiblyTheme.text else actionIconColorBase
        set(value) { actionIconColorBase = value }
    var actionIconSize by androidx.compose.runtime.mutableIntStateOf(24)
    var actionsVisible by androidx.compose.runtime.mutableStateOf(true)
    var navigationLabels by androidx.compose.runtime.mutableStateOf(listOf("Início", "Comunidades", "Criar", "Mensagens", "Perfil"))
    var feedLabels by androidx.compose.runtime.mutableStateOf(listOf("Descobrir", "Seguindo", "Comunidade"))
    var navigationLabelSize by androidx.compose.runtime.mutableIntStateOf(14)
    private var headerBackgroundColorBase by androidx.compose.runtime.mutableStateOf(Color.Black)
    var headerBackgroundColor: Color
        get() = if (QiblyTheme.dark) QiblyTheme.background else headerBackgroundColorBase
        set(value) { headerBackgroundColorBase = value }
    var headerHorizontalPadding by androidx.compose.runtime.mutableIntStateOf(24)
    var headerVerticalPadding by androidx.compose.runtime.mutableIntStateOf(10)
    var feedSpacing by androidx.compose.runtime.mutableIntStateOf(0)
    var spreadPostActions by androidx.compose.runtime.mutableStateOf(false)
    var badgeColor by androidx.compose.runtime.mutableStateOf(Color(0xFFFF3045))
    var likedColor by androidx.compose.runtime.mutableStateOf(Color(0xFFFF3045))
    var profileAccentColor by androidx.compose.runtime.mutableStateOf(Color.White)
    var commentAccentColor by androidx.compose.runtime.mutableStateOf(Color.White)
    private var commentBackgroundColorBase by androidx.compose.runtime.mutableStateOf(Color.White)
    var commentBackgroundColor: Color
        get() = if (QiblyTheme.dark) QiblyTheme.card else commentBackgroundColorBase
        set(value) { commentBackgroundColorBase = value }
    private var commentTextColorBase by androidx.compose.runtime.mutableStateOf(Color.Black)
    var commentTextColor: Color
        get() = if (QiblyTheme.dark) QiblyTheme.text else commentTextColorBase
        set(value) { commentTextColorBase = value }
    private var commentSecondaryTextColorBase by androidx.compose.runtime.mutableStateOf(Color(0xFF555555))
    var commentSecondaryTextColor: Color
        get() = if (QiblyTheme.dark) QiblyTheme.secondary else commentSecondaryTextColorBase
        set(value) { commentSecondaryTextColorBase = value }
    private var commentInputBackgroundColorBase by androidx.compose.runtime.mutableStateOf(Color(0xFFF5F5F5))
    var commentInputBackgroundColor: Color
        get() = if (QiblyTheme.dark) QiblyTheme.soft else commentInputBackgroundColorBase
        set(value) { commentInputBackgroundColorBase = value }
    private var commentDividerColorBase by androidx.compose.runtime.mutableStateOf(Color(0xFFE5E5E5))
    var commentDividerColor: Color
        get() = if (QiblyTheme.dark) QiblyTheme.border else commentDividerColorBase
        set(value) { commentDividerColorBase = value }
    var messageAccentColor by androidx.compose.runtime.mutableStateOf(Color.White)
    var playerProgressColor by androidx.compose.runtime.mutableStateOf(Color.White)
    var enableDemoFeed by androidx.compose.runtime.mutableStateOf(true)
    var showFeedHeader by androidx.compose.runtime.mutableStateOf(true)
}

private object RemoteUiRefreshSignal {
    var value by androidx.compose.runtime.mutableIntStateOf(0)
}

@Composable
private fun QinblyApp() {
    var tab by remember { mutableIntStateOf(0) }
    var navTopRequest by remember { mutableIntStateOf(0) }
    // GitHub-hosted, data-only UI configuration. No executable code is downloaded.
    val appContext = androidx.compose.ui.platform.LocalContext.current
    val uiPreferences = remember { appContext.getSharedPreferences("qinbly_remote_ui", android.content.Context.MODE_PRIVATE) }
    val themePrefs = remember { appContext.getSharedPreferences("qinbly_session", android.content.Context.MODE_PRIVATE) }
    LaunchedEffect(Unit) {
        QiblyTheme.dark = themePrefs.getString("setting_theme", "dark") == "dark"
        QiblyTheme.largeText = themePrefs.getBoolean("setting_large_text", false)
        QiblyTheme.reduceMotion = themePrefs.getBoolean("setting_reduce_motion", false)
    }
    SideEffect {
        var host: android.content.Context? = appContext
        while (host is android.content.ContextWrapper && host !is android.app.Activity) host = host.baseContext
        (host as? android.app.Activity)?.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(QiblyTheme.background.toArgb()))
    }
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
    var profileStatUsers by remember { mutableStateOf<List<QinblyApi.UserSummary>>(emptyList()) }
    var profileStatLoading by remember { mutableStateOf(false) }
    var profileStatError by remember { mutableStateOf("") }
    var showAuth by remember { mutableStateOf(false) }
    var showAccountSwitcher by remember { mutableStateOf(false) }
    var accountSessionVersion by remember { mutableIntStateOf(0) }
    var showSearchDialog by remember { mutableStateOf(false) }
    var publicProfileUsername by remember { mutableStateOf("") }
    var messageRecipient by remember { mutableStateOf("") }
    QinblyApi.initialize(appContext)
    val appPrefs = remember { appContext.getSharedPreferences("qinbly_session", android.content.Context.MODE_PRIVATE) }
    var loggedInUser by remember { mutableStateOf(appPrefs.getString("username", "") ?: "") }
    QinblyDeepLinkHost(appPrefs, loggedInUser) { notice = it }
    QinblyTagHost(appPrefs, loggedInUser) { notice = it }
    LaunchedEffect(QinblyTagNav.profile) {
        QinblyTagNav.profile?.let { publicProfileUsername = it; QinblyTagNav.profile = null }
    }
    var unreadMessages by remember { mutableIntStateOf(0) }
    var onlineUsers by remember { mutableIntStateOf(-1) }
    LaunchedEffect(loggedInUser, accountSessionVersion) {
        val token = appPrefs.getString("token", null)
        onlineUsers = -1
        if (!token.isNullOrBlank()) {
            while (true) {
                try {
                    // "Mostrar que estou online" desligado: não envia presença (e o contador some para você).
                    if (appPrefs.getBoolean("setting_activity_status", true)) {
                        onlineUsers = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                            QinblyApi.heartbeatPresence(token)
                        }
                    } else onlineUsers = -1
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
                        QinblyApi.getInbox(token)
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
        if (!token.isNullOrBlank() && loggedInUser.isNotBlank()) rememberQinblyAccount(appPrefs, loggedInUser, token, profileName, profileBio)
        if (!token.isNullOrBlank()) {
            Thread {
                try {
                    val response = QinblyApi.getMyProfile(token)
                    val user = response.optJSONObject("user")
                    if (user != null) {
                        val username = user.optString("username")
                        val name = user.optString("displayName", user.optString("display_name"))
                        val bio = user.optString("bio")
                        appPrefs.edit().putString("username", username).putString("display_name", name).putString("bio", bio).apply()
                        rememberQinblyAccount(appPrefs, username, token, name, bio)
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
            tab == 1 && messageRecipient.isNotBlank() -> messageRecipient = ""
            tab != 0 -> {
                tab = 0
                messageRecipient = ""
            }
            else -> (appContext as? android.app.Activity)?.finish()
        }
    }
    MaterialTheme(
        colorScheme = if (QiblyTheme.dark)
            darkColorScheme(primary = QinblyBrand.AccentLight, secondary = QinblyBrand.AccentLight, background = QiblyTheme.background, surface = QiblyTheme.card, onBackground = QiblyTheme.text, onSurface = QiblyTheme.text)
        else androidx.compose.material3.lightColorScheme(primary = QinblyBrand.Accent, secondary = QinblyBrand.Accent, background = Color(0xFFFBF9FF), surface = Color.White, onBackground = Color.Black, onSurface = Color.Black),
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
        val baseDensity = androidx.compose.ui.platform.LocalDensity.current
        androidx.compose.runtime.CompositionLocalProvider(
            androidx.compose.ui.platform.LocalDensity provides androidx.compose.ui.unit.Density(baseDensity.density, baseDensity.fontScale * (if (QiblyTheme.largeText) 1.18f else 1f))
        ) {
        val keyboardOpen = WindowInsets.ime.getBottom(androidx.compose.ui.platform.LocalDensity.current) > 0
        Scaffold(containerColor = QiblyTheme.background, bottomBar = {
            if (tab != 2 && !keyboardOpen) {
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
                    containerColor = if (QiblyTheme.dark) QiblyTheme.card else navigationBackgroundColor,
                    contentColor = if (QiblyTheme.dark) Color.White else Color.Black,
                    tonalElevation = 0.dp
                ) {
                    val navColors = NavigationBarItemDefaults.colors(
                        selectedIconColor = QinblyBrand.Accent,
                        selectedTextColor = QinblyBrand.Accent,
                        indicatorColor = Color.Transparent,
                        unselectedIconColor = if (QiblyTheme.dark) Color.White else navigationUnselectedColor,
                        unselectedTextColor = if (QiblyTheme.dark) Color.White else navigationUnselectedColor
                    )
                    NavigationBarItem(selected = tab == 0 && !showNotifications, onClick = { showSettings = false; showEditProfile = false; showNotifications = false; publicProfileUsername = ""; messageRecipient = ""; showAuth = false; if (tab == 0) navTopRequest++; tab = 0 }, icon = { Box(Modifier.size(32.dp), contentAlignment = Alignment.Center) { Icon(Icons.Default.Home, null); if (RemoteAppearance.navigationUnderlineVisible && tab == 0 && !showNotifications) Box(Modifier.align(Alignment.BottomCenter).offset(y = RemoteAppearance.navigationUnderlineOffset.dp).width(RemoteAppearance.navigationUnderlineWidth.dp).height(RemoteAppearance.navigationUnderlineHeight.dp).clip(RoundedCornerShape(2.dp)).background(QinblyBrand.Accent)) } }, label = { Text(RemoteAppearance.navigationLabels[0], fontSize = RemoteAppearance.navigationLabelSize.sp) }, colors = navColors)
                    NavigationBarItem(selected = tab == 3 && !showNotifications, onClick = { showSettings = false; showEditProfile = false; showNotifications = false; publicProfileUsername = ""; showAuth = false; if (tab == 3) navTopRequest++; tab = 3 }, icon = { Box(Modifier.size(32.dp), contentAlignment = Alignment.Center) { Icon(Icons.Default.Groups, contentDescription = "Comunidades"); if (RemoteAppearance.navigationUnderlineVisible && tab == 3 && !showNotifications) Box(Modifier.align(Alignment.BottomCenter).offset(y = RemoteAppearance.navigationUnderlineOffset.dp).width(RemoteAppearance.navigationUnderlineWidth.dp).height(RemoteAppearance.navigationUnderlineHeight.dp).clip(RoundedCornerShape(2.dp)).background(QinblyBrand.Accent)) } }, label = { Text("Comunidades", fontSize = RemoteAppearance.navigationLabelSize.sp) }, colors = navColors)
                    NavigationBarItem(selected = false, onClick = { showSettings = false; showEditProfile = false; showNotifications = false; publicProfileUsername = ""; showAuth = false; showCreateOptions = true }, icon = {
                        Box(
                            Modifier.size(36.dp).clip(CircleShape).background(Brush.linearGradient(listOf(QinblyBrand.Accent, QinblyBrand.Pink))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Criar publicação", modifier = Modifier.size(24.dp), tint = Color.White)
                        }
                    }, label = { Text(RemoteAppearance.navigationLabels[2], fontSize = RemoteAppearance.navigationLabelSize.sp) }, colors = navColors)
                    NavigationBarItem(
                        selected = tab == 1 && !showNotifications,
                        onClick = { showSettings = false; showEditProfile = false; showNotifications = false; publicProfileUsername = ""; showAuth = false; if (tab == 1) navTopRequest++; tab = 1 },
                        icon = {
                            Box(Modifier.size(32.dp), contentAlignment = Alignment.Center) {
                                if (RemoteAppearance.navigationUnderlineVisible && tab == 1 && !showNotifications) Box(Modifier.align(Alignment.BottomCenter).offset(y = RemoteAppearance.navigationUnderlineOffset.dp).width(RemoteAppearance.navigationUnderlineWidth.dp).height(RemoteAppearance.navigationUnderlineHeight.dp).clip(RoundedCornerShape(2.dp)).background(QinblyBrand.Accent))
                                Icon(Icons.Default.ChatBubbleOutline, contentDescription = "Mensagens")
                                if (unreadMessages > 0) {
                                    Box(
                                        Modifier.align(Alignment.TopEnd)
                                            .offset(x = 7.dp, y = (-5).dp)
                                            .sizeIn(minWidth = 18.dp, minHeight = 18.dp)
                                            .clip(CircleShape)
                                            .background(QinblyBrand.Accent)
                                            .padding(horizontal = 4.dp, vertical = 1.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            if (unreadMessages > 99) "99+" else unreadMessages.toString(),
                                            color = Color.White,
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
                    NavigationBarItem(selected = tab == 4 && !showNotifications, onClick = { showSettings = false; showEditProfile = false; showNotifications = false; publicProfileUsername = ""; showAuth = false; if (tab == 4) navTopRequest++; tab = 4 }, icon = { Box(Modifier.size(32.dp), contentAlignment = Alignment.Center) { Icon(Icons.Default.Person, contentDescription = "Perfil"); if (RemoteAppearance.navigationUnderlineVisible && tab == 4 && !showNotifications) Box(Modifier.align(Alignment.BottomCenter).offset(y = RemoteAppearance.navigationUnderlineOffset.dp).width(RemoteAppearance.navigationUnderlineWidth.dp).height(RemoteAppearance.navigationUnderlineHeight.dp).clip(RoundedCornerShape(2.dp)).background(QinblyBrand.Accent)) } }, label = { Text(RemoteAppearance.navigationLabels[4], fontSize = RemoteAppearance.navigationLabelSize.sp) }, colors = navColors)
                }
            }
            }
        }) { pad ->
            Column(
                Modifier.fillMaxSize()
                    // Com o teclado aberto, só o teclado empurra o conteúdo (sem somar a margem da barra do sistema, que deixava um vão).
                    .then(if (keyboardOpen) Modifier.padding(top = pad.calculateTopPadding()).imePadding() else Modifier.padding(pad))
            ) {
                if (publicProfileUsername.isNotBlank() || tab == 2) {
                    // Cabeçalho de marca removido para liberar espaço acima do perfil.
                } else if (!showSettings && !showEditProfile && !showEditProfilePhoto && !showNotifications && !showAuth) when (tab) {
                    0 -> Unit
                    1 -> if (messageRecipient.isBlank()) Box(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp).heightIn(min = 44.dp)) {
                        Text("Mensagens", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Center))
                        IconButton(onClick = { showSearchDialog = true }, modifier = Modifier.align(Alignment.CenterEnd)) {
                            Icon(Icons.Default.Search, contentDescription = "Pesquisar pessoas", tint = QiblyTheme.text)
                        }
                    }
                    3 -> Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("Comunidades", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        IconButton(onClick = { showSearchDialog = true }) { Icon(Icons.Default.Search, contentDescription = "Pesquisar lives e pessoas", tint = QiblyTheme.text) }
                        IconButton(onClick = { showSettings = true }) { Icon(Icons.Default.Menu, contentDescription = "Abrir menu e configurações", tint = QiblyTheme.text) }
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
                            QinblyCache.clear(appContext)
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
                    QinblyPullRefresh {
                    NotificationsPage(
                        token = appPrefs.getString("token", null) ?: "",
                        onOpenProfile = { showNotifications = false; publicProfileUsername = it },
                        onBack = { showNotifications = false },
                        onOpen = { notification ->
                            showNotifications = false
                            if (notification.kind == "message" && notification.actorUsername.isNotBlank()) {
                                messageRecipient = notification.actorUsername
                                tab = 1
                            } else if ((notification.kind == "mention" || notification.kind == "mention_comment" || notification.kind == "tag") && notification.entityId.isNotBlank()) {
                                QinblyDeepLink.postId = notification.entityId
                            } else if (notification.actorUsername.isNotBlank()) {
                                publicProfileUsername = notification.actorUsername
                            }
                        },
                        onNotice = { notice = it }
                    )
                    }
                } else if (showEditProfilePhoto) {
                    EditProfilePhotoPage(
                        onBack = { showEditProfilePhoto = false },
                        onSaved = {
                            showEditProfilePhoto = false
                            profilePhotoRefreshKey++
                            qiblyAvatarRefreshVersion.intValue++
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
                            val remainingAccounts = savedQinblyAccounts(appPrefs).filterNot {
                                it.username.equals(previousUsername, true) || it.username.equals(username, true)
                            }
                            val accountsJson = JSONArray()
                            remainingAccounts.forEach { a ->
                                accountsJson.put(JSONObject().put("username", a.username).put("token", a.token)
                                    .put("displayName", a.displayName).put("bio", a.bio).put("refreshToken", a.refreshToken))
                            }
                            appPrefs.edit().putString("qinbly_accounts", accountsJson.toString())
                                .putString("username", username).putString("display_name", name).putString("bio", bio).apply()
                            if (currentToken.isNotBlank()) rememberQinblyAccount(appPrefs, username, currentToken, name, bio, currentRefreshToken)
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
                    QinblyPullRefresh {
                    PublicProfilePage(
                        username = publicProfileUsername,
                        onBack = { publicProfileUsername = "" },
                        onMessage = { recipient -> publicProfileUsername = ""; messageRecipient = recipient; tab = 1 },
                        onNotice = { notice = it }
                    )
                    }
                } else androidx.compose.runtime.key(accountSessionVersion) { QinblyPullRefresh(enabled = tab != 0 && tab != 2) { when (tab) {
                    0 -> FeedPage(scrollToTopRequest = navTopRequest, onlineUsers = onlineUsers, currentUsername = loggedInUser, photoRefreshKey = profilePhotoRefreshKey, nameFontStyle = nameFontStyle, onNotice = { notice = it }, onOpenProfile = { publicProfileUsername = it }, onAddStory = { tab = 2 }, onSearch = { showSearchDialog = true }, onNotifications = { showNotifications = true }, onCreateWith = { mode, seed -> QinblyComposeSeed.text = seed; createEntryMode = mode; tab = 2 })
                    1 -> MessagesPage(scrollToTopRequest = navTopRequest, recipientUsername = messageRecipient, onSearch = { showSearchDialog = true }, onOpenConversation = { messageRecipient = it }, onConversationRead = { n -> unreadMessages = (unreadMessages - n).coerceAtLeast(0) }, onOpenProfile = { publicProfileUsername = it }, onNotice = { notice = it })
                    2 -> PostPage(onNotice = { notice = it }, onCameraCancelled = { tab = 0 }, initialMode = createEntryMode, onSwitchMode = { createEntryMode = it })
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
                                        val response = QinblyApi.getMyProfile(token)
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
                        profileStatError = ""
                        profileStatLoading = true
                        val tokenUser = loggedInUser
                        if (tokenUser.isNotBlank() && (stat == "Seguidores" || stat == "Seguindo")) {
                            Thread {
                                try {
                                    val users = QinblyApi.getConnections(tokenUser, if (stat == "Seguidores") "followers" else "following")
                                    android.os.Handler(android.os.Looper.getMainLooper()).post {
                                        QinblyEngagement.learnFromUsers(appPrefs, users)
                                        profileStatUsers = users
                                        profileStatLoading = false
                                    }
                                } catch (e: Exception) {
                                    android.os.Handler(android.os.Looper.getMainLooper()).post {
                                        profileStatError = e.message ?: "Não foi possível carregar a lista."
                                        profileStatLoading = false
                                    }
                                }
                            }.start()
                        } else profileStatLoading = false
                    }, onAuth = { showAuth = true }, onEdit = { showEditProfile = true; showSettings = false; showEditProfilePhoto = false; publicProfileUsername = "" }, onEditProfilePhoto = { showEditProfilePhoto = true; showEditProfile = false; showSettings = false; publicProfileUsername = "" }, onSettings = { showSettings = true })
                } } }
            }
        }
        }
        if (showAccountSwitcher) {
            AccountSwitcherDialog(savedQinblyAccounts(appPrefs), loggedInUser, { showAccountSwitcher = false }, { account ->
                appPrefs.edit().putString("token",account.token).putString("username",account.username).putString("display_name",account.displayName).putString("bio",account.bio).putString("refresh_token",account.refreshToken).apply()
                loggedInUser=account.username; profileName=account.displayName; profileBio=account.bio
                showAccountSwitcher=false; showSettings=false; showAuth=false; showEditProfile=false
                showEditProfilePhoto=false; showNotifications=false; showSearchDialog=false
                publicProfileUsername=""; messageRecipient=""; profileStat=""; profileStatUsers=emptyList()
                liked=emptySet(); unreadMessages=0; QinblyCache.clear(appContext); accountSessionVersion++
                tab=0
                notice="Conta alterada para @"+account.username+"."
                Thread {
                    try {
                        val response=QinblyApi.getMyProfile(account.token); val user=response.optJSONObject("user")
                        if(user!=null) {
                            val n=user.optString("displayName",user.optString("display_name")); val b=user.optString("bio"); val u=user.optString("username",account.username)
                            appPrefs.edit().putString("username",u).putString("display_name",n).putString("bio",b).apply()
                            rememberQinblyAccount(appPrefs,u,account.token,n,b)
                            android.os.Handler(android.os.Looper.getMainLooper()).post { loggedInUser=u; profileName=n; profileBio=b }
                        }
                    } catch (_:Exception) { android.os.Handler(android.os.Looper.getMainLooper()).post { notice="Conta selecionada; não foi possível atualizar o perfil pela rede agora." } }
                }.start()
            }, {
                showAccountSwitcher=false; showAuth=true; showSettings=false; tab=4
            }, { usernameToRemove ->
                val remaining = savedQinblyAccounts(appPrefs).filterNot { it.username.equals(usernameToRemove, true) }
                val saved = JSONArray()
                remaining.forEach { account ->
                    saved.put(JSONObject().put("username", account.username).put("token", account.token)
                        .put("displayName", account.displayName).put("bio", account.bio).put("refreshToken", account.refreshToken))
                }
                val removingCurrent = loggedInUser.equals(usernameToRemove, true)
                val edit = appPrefs.edit().putString("qinbly_accounts", saved.toString())
                if (removingCurrent) {
                    edit.remove("token").remove("refresh_token").remove("username").remove("display_name").remove("bio")
                    loggedInUser = ""; profileName = ""; profileBio = ""
                    QinblyCache.clear(appContext)
                    showAccountSwitcher = false; showAuth = true
                    notice = "Conta removida deste aparelho. Entre novamente para continuar."
                } else {
                    notice = "Conta removida da lista deste aparelho."
                }
                edit.apply()
            })
        }
        if (showCreateOptions) {
            // Folha de baixo com 4 cartões (Texto, Galeria, Câmera, Enquete); tocar fora fecha.
            androidx.compose.ui.window.Dialog(
                onDismissRequest = { showCreateOptions = false },
                properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
            ) {
                val noRipple = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                val noRipple2 = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                Box(Modifier.fillMaxSize().clickable(indication = null, interactionSource = noRipple) { showCreateOptions = false }) {
                    Column(
                        Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                            .clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
                            .background(QiblyTheme.card)
                            .clickable(indication = null, interactionSource = noRipple2) { }
                            .navigationBarsPadding()
                            .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 96.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(Modifier.align(Alignment.CenterHorizontally).width(34.dp).height(4.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFFD0D5DD)))
                        Text("Criar", color = RemoteAppearance.screenTextColor, fontWeight = FontWeight.SemiBold, fontSize = 17.sp, modifier = Modifier.padding(vertical = 6.dp))
                        listOf(
                            listOf(Triple("Texto", "text", QinblyCreateIcons.text), Triple("Galeria", "gallery", QinblyCreateIcons.gallery)),
                            listOf(Triple("Câmera", "camera", QinblyCreateIcons.camera), Triple("Enquete", "poll", QinblyCreateIcons.poll))
                        ).forEach { row ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                row.forEach { (label, mode, icon) ->
                                    Column(
                                        Modifier.weight(1f).clip(RoundedCornerShape(16.dp))
                                            .background(QiblyTheme.soft)
                                            .clickable { showCreateOptions = false; createEntryMode = mode; tab = 2 }
                                            .padding(vertical = 14.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(7.dp)
                                    ) {
                                        Box(Modifier.size(46.dp).clip(CircleShape).background(QinblyBrand.Accent.copy(alpha = 0.14f)), contentAlignment = Alignment.Center) {
                                            Icon(icon, contentDescription = null, tint = QinblyBrand.Accent, modifier = Modifier.size(24.dp))
                                        }
                                        Text(label, color = RemoteAppearance.screenTextColor, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                    }
                                }
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
                                Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = QiblyTheme.text)
                            }
                            Column(Modifier.weight(1f)) {
                                Text(profileStat, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = QiblyTheme.text)
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
                                profileStatLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator()
                                }
                                profileStatError.isNotBlank() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text(profileStatError, color = Muted)
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
                                        QinblyUserRow(user, appPrefs, onOpenProfile = { publicProfileUsername = it; profileStat = "" }, onError = { notice = it })
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
    onOpen: (QinblyApi.NotificationSummary) -> Unit,
    onNotice: (String) -> Unit,
    onOpenProfile: (String) -> Unit = {}
) {
    var notifications by remember { mutableStateOf<List<QinblyApi.NotificationSummary>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var selectedFilter by remember { mutableStateOf("Todas") }
    val notifContext = androidx.compose.ui.platform.LocalContext.current
    val notifPrefs = remember { notifContext.getSharedPreferences("qinbly_session", android.content.Context.MODE_PRIVATE) }
    var showNotifSettings by remember { mutableStateOf(false) }
    var expandedOthers by remember { mutableStateOf(false) }
    var showAllBack by remember { mutableStateOf(false) }
    // "Seguir de volta": quem segue você e você ainda não segue (lista fixada ao abrir, para o botão virar "Seguindo" sem sumir).
    var followBackList by remember { mutableStateOf<List<QinblyApi.UserSummary>>(emptyList()) }
    LaunchedEffect(token) {
        val me = notifPrefs.getString("username", "").orEmpty().removePrefix("@")
        if (token.isNotBlank() && me.isNotBlank()) {
            val followers = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) { runCatching { QinblyApi.getConnections(me, "followers") }.getOrDefault(emptyList()) }
            followBackList = followers.filter { it.username.isNotBlank() && !QinblyEngagement.isFollowing(notifPrefs, it.username, it.isFollowing) }
        }
    }
    val notificationScope = rememberCoroutineScope()
    var nowNotificationTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            nowNotificationTime = System.currentTimeMillis()
            kotlinx.coroutines.delay(15_000L)
        }
    }
    LaunchedEffect(token) {
        if (token.isBlank()) {
            loading = false
        } else {
            try {
                val loaded = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) { QinblyApi.getNotifications(token) }
                notifications = loaded
                // Ao abrir a tela, o contador do sino zera (as novas continuam azuis até serem tocadas).
                val oldSeen = notifPrefs.getStringSet("notif_seen_ids", emptySet()).orEmpty()
                notifPrefs.edit().putStringSet("notif_seen_ids", (oldSeen + loaded.map { it.id }).toList().takeLast(300).toSet()).apply()
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
            "Do ${QinblyBrand.Name}" -> item.kind == "system"
            else -> true
        }
    }
    val unreadCount = notifications.count { it.readAt.isBlank() && it.kind != "message" }
    fun markRead(item: QinblyApi.NotificationSummary) {
        if (item.readAt.isNotBlank()) return
        notifications = notifications.map { current ->
            if (current.id == item.id) current.copy(readAt = java.time.Instant.now().toString()) else current
        }
        notificationScope.launch {
            try {
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) { QinblyApi.markNotificationRead(token, item.id) }
            } catch (_: Exception) {
                try {
                    notifications = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) { QinblyApi.getNotifications(token) }
                } catch (_: Exception) {}
            }
        }
    }
    Column(Modifier.fillMaxSize().background(QiblyTheme.background).padding(horizontal = 16.dp, vertical = 10.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = QiblyTheme.text) }
            Text("Notificações", color = QiblyTheme.text, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            if (unreadCount > 0) {
                Text(
                    "Marcar como lidas", color = QinblyBrand.Accent, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clip(RoundedCornerShape(10.dp)).clickable { notifications.filter { it.readAt.isBlank() }.forEach { markRead(it) } }.padding(8.dp)
                )
            }
            IconButton(onClick = { showNotifSettings = true }) { Icon(Icons.Default.Menu, contentDescription = "Configurações das notificações", tint = QiblyTheme.text) }
        }
        if (showNotifSettings) {
            androidx.compose.ui.window.Dialog(
                onDismissRequest = { showNotifSettings = false },
                properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
            ) {
                val outer = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                val inner = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                Box(Modifier.fillMaxSize().clickable(indication = null, interactionSource = outer) { showNotifSettings = false }) {
                    Column(
                        Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                            .clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)).background(QiblyTheme.card)
                            .clickable(indication = null, interactionSource = inner) { }
                            .padding(start = 18.dp, end = 18.dp, top = 10.dp, bottom = 28.dp)
                    ) {
                        Box(Modifier.align(Alignment.CenterHorizontally).width(34.dp).height(4.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFFD0D5DD)))
                        Text("Configurações das notificações", color = QiblyTheme.text, fontWeight = FontWeight.SemiBold, fontSize = 17.sp, modifier = Modifier.padding(vertical = 10.dp))
                        // (rótulo, chave, descrição, invertido). "Silenciar tudo" é o contrário de setting_notifications.
                        listOf(
                            Triple("Silenciar tudo", "setting_notifications", "Pausar os avisos neste aparelho"),
                            Triple("Curtidas", "setting_notice_likes", ""),
                            Triple("Comentários", "setting_notice_comments", ""),
                            Triple("Menções", "setting_notice_mentions", "Quando alguém marcar você com @ em um comentário ou publicação"),
                            Triple("Novos seguidores", "setting_notice_followers", ""),
                            Triple("Avisos do ${QinblyBrand.Name}", "setting_notice_system", "")
                        ).forEach { (label, key, hint) ->
                            val inverted = key == "setting_notifications"
                            var stored by remember { mutableStateOf(notifPrefs.getBoolean(key, true)) }
                            Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(label, color = QiblyTheme.text, fontSize = 15.sp, fontWeight = if (inverted) FontWeight.Medium else FontWeight.Normal)
                                    if (hint.isNotBlank()) Text(hint, color = QiblyTheme.secondary, fontSize = 12.sp)
                                }
                                Switch(
                                    checked = if (inverted) !stored else stored,
                                    onCheckedChange = { on -> stored = if (inverted) !on else on; notifPrefs.edit().putBoolean(key, stored).apply() }
                                )
                            }
                        }
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Todas", "Atividades", "Do ${QinblyBrand.Name}").forEach { label ->
                val selected = selectedFilter == label
                Surface(
                    modifier = Modifier.clickable { selectedFilter = label },
                    color = if (selected) QinblyBrand.Accent else QinblyBrand.Accent.copy(alpha = 0.10f),
                    shape = RoundedCornerShape(22.dp)
                ) {
                    Text(label, modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp), color = if (selected) Color.White else QiblyTheme.text, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        when {
            loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = QinblyBrand.Accent) }
            token.isBlank() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Entre na sua conta para ver as notificações.", color = RemoteAppearance.screenSecondaryTextColor) }
            visible.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(Modifier.size(84.dp).clip(CircleShape).background(QinblyBrand.Accent.copy(alpha = 0.10f)), contentAlignment = Alignment.Center) {
                        Icon(androidx.compose.material.icons.Icons.Outlined.NotificationsNone, contentDescription = null, tint = QinblyBrand.Accent, modifier = Modifier.size(42.dp))
                    }
                    Text("Você está em dia!", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = QiblyTheme.text)
                    Text("Novos seguidores, curtidas e comentários aparecerão aqui.", color = QiblyTheme.secondary, style = MaterialTheme.typography.bodyMedium)
                }
            }
            else -> {
                val fresh = visible.filter { it.readAt.isBlank() }
                val older = visible.filter { it.readAt.isNotBlank() }
                @Composable
                fun NotificationRow(item: QinblyApi.NotificationSummary) {
                    val actor = item.actorDisplayName.ifBlank { item.actorUsername.ifBlank { QinblyBrand.Name } }
                    val description = if (item.kind == "system") "enviou um aviso" else qinblyNotificationText(item.kind)
                    val unread = item.readAt.isBlank()
                    val (kindIcon, kindColor) = when (item.kind) {
                        "like" -> Icons.Default.Favorite to Color(0xFFE5365B)
                        "follow" -> Icons.Default.PersonAdd to QinblyBrand.Accent
                        "comment", "reply" -> Icons.Default.ChatBubbleOutline to Color(0xFF2FA36B)
                        "mention", "mention_comment", "tag" -> Icons.Default.AlternateEmail to Color(0xFF8A5CF6)
                        else -> Icons.Default.Info to Color(0xFF6B7280)
                    }
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 2.dp).clip(RoundedCornerShape(16.dp))
                            .background(if (unread) QinblyBrand.Accent.copy(alpha = 0.12f) else Color.Transparent)
                            .clickable { markRead(item); onOpen(item) }.padding(horizontal = 10.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(Modifier.size(56.dp)) {
                            if (item.actorUsername.isNotBlank()) QiblyAccountAvatar(item.actorUsername, item.actorAvatarUrl, 52.dp) { markRead(item); onOpen(item) }
                            else Box(Modifier.size(52.dp).clip(CircleShape).background(QinblyBrand.Accent), contentAlignment = Alignment.Center) { Icon(Icons.Default.Info, contentDescription = null, tint = Color.White) }
                            Box(
                                Modifier.align(Alignment.BottomEnd).size(22.dp).border(2.dp, QiblyTheme.card, CircleShape).clip(CircleShape).background(kindColor),
                                contentAlignment = Alignment.Center
                            ) { Icon(kindIcon, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp)) }
                        }
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(
                                androidx.compose.ui.text.buildAnnotatedString {
                                    pushStyle(androidx.compose.ui.text.SpanStyle(fontWeight = FontWeight.Bold)); append(actor); pop()
                                    append(" $description")
                                },
                                color = QiblyTheme.text, style = MaterialTheme.typography.bodyMedium, maxLines = 3
                            )
                            Text(postRelativeTime(item.createdAt, nowNotificationTime), color = if (unread) QinblyBrand.Accent else Color.DarkGray, style = MaterialTheme.typography.labelSmall, fontWeight = if (unread) FontWeight.SemiBold else FontWeight.Normal)
                        }
                        if (item.kind == "follow" && item.actorUsername.isNotBlank()) QinblyFollowButton(
                            following = QinblyEngagement.isFollowing(notifPrefs, item.actorUsername, false),
                            busy = QinblyEngagement.isFollowBusy(item.actorUsername),
                            onClick = { QinblyEngagement.toggleFollow(notifPrefs, item.actorUsername, "", item.actorDisplayName, onNotice) },
                            idleLabel = "Seguir de volta"
                        )
                    }
                }
                // Primeiras 5 notificações sempre visíveis; o resto abre para baixo na barrinha.
                val limit = if (expandedOthers) Int.MAX_VALUE else 5
                val freshShown = fresh.take(limit)
                val olderShown = older.take((limit - freshShown.size).coerceAtLeast(0))
                val hiddenCount = visible.size - freshShown.size - olderShown.size
                LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 20.dp)) {
                    if (freshShown.isNotEmpty()) {
                        item(key = "h_new") { Text("Novas", color = QiblyTheme.text, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 4.dp, bottom = 6.dp)) }
                        items(freshShown, key = { it.id }) { NotificationRow(it) }
                    }
                    if (olderShown.isNotEmpty()) {
                        item(key = "h_old") { Text(if (fresh.isEmpty()) "Recentes" else "Anteriores", color = QiblyTheme.text, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 14.dp, bottom = 6.dp)) }
                        items(olderShown, key = { it.id }) { NotificationRow(it) }
                    }
                    if (visible.size > 5) item(key = "more_bar") {
                        Text(
                            if (expandedOthers) "▴ Mostrar menos" else "▾ Mais notificações ($hiddenCount)",
                            color = QinblyBrand.Accent, fontWeight = FontWeight.Medium, textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp).clip(RoundedCornerShape(14.dp)).background(QiblyTheme.soft).clickable { expandedOthers = !expandedOthers }.padding(vertical = 10.dp)
                        )
                    }
                    if (selectedFilter == "Todas" && followBackList.isNotEmpty()) {
                        item(key = "fb_header") {
                            Column(Modifier.fillMaxWidth().padding(top = 16.dp)) {
                                Box(Modifier.fillMaxWidth().padding(top = 6.dp).height(1.dp).background(RemoteAppearance.feedDividerColor.copy(alpha = 0.45f)))
                                Text("Seguir de volta", color = QiblyTheme.text, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 14.dp))
                                Text("Pessoas que seguem você e que você não segue ainda.", color = QiblyTheme.secondary, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 2.dp, bottom = 4.dp))
                            }
                        }
                        items(if (showAllBack) followBackList else followBackList.take(6), key = { "fb-" + it.username }) { person ->
                            Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                QiblyAccountAvatar(person.username, person.avatarUrl, 46.dp) { onOpenProfile(person.username) }
                                Column(Modifier.weight(1f).clickable { onOpenProfile(person.username) }) {
                                    Row(verticalAlignment = Alignment.CenterVertically) { Text(person.displayName.ifBlank { person.username }, color = QiblyTheme.text, fontWeight = FontWeight.Bold, maxLines = 1); if (person.verified) { Spacer(Modifier.width(5.dp)); QinblyVerifiedBadge(15.dp) } }
                                    Text("@" + person.username, color = QiblyTheme.secondary, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                                }
                                QinblyFollowButton(
                                    following = QinblyEngagement.isFollowing(notifPrefs, person.username, person.isFollowing),
                                    busy = QinblyEngagement.isFollowBusy(person.username),
                                    onClick = { QinblyEngagement.toggleFollow(notifPrefs, person.username, person.id, person.displayName, onNotice) },
                                    idleLabel = "Seguir de volta"
                                )
                            }
                        }
                        if (followBackList.size > 6) item(key = "fb_all") {
                            Text(
                                if (showAllBack) "Ver menos" else "Ver todos (${followBackList.size})",
                                color = QinblyBrand.Accent, fontWeight = FontWeight.Medium, textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.fillMaxWidth().clickable { showAllBack = !showAllBack }.padding(vertical = 12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QinblyUserRow(user: QinblyApi.UserSummary, prefs: android.content.SharedPreferences, onOpenProfile: (String) -> Unit, onError: (String) -> Unit) {
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
                                    Text(user.displayName.ifBlank { user.username }, color = QiblyTheme.text, fontWeight = FontWeight.SemiBold)
                                    Text("@${user.username}", color = Muted, style = MaterialTheme.typography.bodySmall)
                                    if (user.bio.isNotBlank()) Text(user.bio, color = Muted, maxLines = 1, style = MaterialTheme.typography.labelSmall)
                                }
                                if (!user.username.equals(prefs.getString("username", "").orEmpty().removePrefix("@"), ignoreCase = true)) {
                                    QinblyFollowButton(
                                        following = QinblyEngagement.isFollowing(prefs, user.username, user.isFollowing),
                                        busy = QinblyEngagement.isFollowBusy(user.username),
                                        onClick = { QinblyEngagement.toggleFollow(prefs, user.username, user.id, user.displayName, onError, notifySuccess = false) }
                                    )
                                }
                            }
}

/** Divide composables enormes em métodos menores (evita o limite de 64 KB de um método na JVM). */
@Composable
private fun QinblySplit(content: @Composable () -> Unit) { content() }

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun SearchUsersDialog(searchContext: String, onDismiss: () -> Unit, onOpenProfile: (String) -> Unit, onError: (String) -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { context.getSharedPreferences("qinbly_session", android.content.Context.MODE_PRIVATE) }
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<QinblyApi.UserSummary>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var searched by remember { mutableStateOf(false) }
    var searchError by remember { mutableStateOf("") }
    var trending by remember { mutableStateOf<List<Pair<String, Int>>>(emptyList()) }
    var suggested by remember { mutableStateOf<List<QinblyApi.UserSummary>>(emptyList()) }
    LaunchedEffect(Unit) {
        try {
            val (t, s) = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                (try { QinblyApi.getTrendingTags() } catch (_: Exception) { emptyList() }) to
                    (try { QinblyApi.getSuggestedUsers() } catch (_: Exception) { emptyList() })
            }
            trending = t
            suggested = s
            QinblyEngagement.learnFromUsers(prefs, s)
        } catch (_: Exception) { }
    }
    var history by remember { mutableStateOf((prefs.getString("qinbly_search_history", "") ?: "").split("\n").filter { it.isNotBlank() }) }
    val saveHistory: (String) -> Unit = { term ->
        val t = term.trim()
        if (t.length >= 2) {
            history = (listOf(t) + history.filterNot { it.equals(t, true) }).take(10)
            prefs.edit().putString("qinbly_search_history", history.joinToString("\n")).apply()
        }
    }
    val removeHistory: (String) -> Unit = { term ->
        history = history.filterNot { it == term }
        prefs.edit().putString("qinbly_search_history", history.joinToString("\n")).apply()
    }
    // Se o servidor não devolver hashtags em alta, usa as do feed já carregado.
    val hotTags = if (trending.isNotEmpty()) trending else remember(QinblyCache.feed) {
        QinblyCache.feed.flatMap { p -> Regex("#([\\p{L}\\p{N}_]+)").findAll(p.caption).map { it.groupValues[1].lowercase() }.toList() }
            .groupingBy { it }.eachCount().entries.sortedByDescending { it.value }.take(8).map { it.key to it.value }
    }
    var myTags by remember { mutableStateOf(QinblyHashtagStore.top(context)) }
    var filter by remember { mutableStateOf("Tudo") }
    // Busca de vídeos: carrega as publicações recentes e mostra só os vídeos que combinam com o texto.
    var videoPosts by remember { mutableStateOf<List<QinblyApi.PostSummary>>(emptyList()) }
    var videosLoading by remember { mutableStateOf(false) }
    var videoViewerId by remember { mutableStateOf<String?>(null) }
    var videoCommentTarget by remember { mutableStateOf<QinblyApi.PostSummary?>(null) }
    var videoShareTarget by remember { mutableStateOf<QinblyApi.PostSummary?>(null) }
    LaunchedEffect(filter) {
        if (filter == "Vídeos" && videoPosts.isEmpty()) {
            videosLoading = true
            try {
                val loaded = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    val fresh = try { QinblyApi.getPosts(prefs.getString("token", null)) } catch (_: Exception) { emptyList() }
                    (fresh + QinblyCache.feed).distinctBy { it.id }.filter { it.mediaType == "video" && it.mediaUrl.isNotBlank() }
                }
                videoPosts = loaded
            } finally { videosLoading = false }
        }
    }
    val videoMatches = remember(videoPosts, query) {
        val q = query.trim().removePrefix("@").removePrefix("#").lowercase()
        if (q.isBlank()) videoPosts else videoPosts.filter { it.caption.lowercase().contains(q) || it.username.lowercase().contains(q) || it.displayName.lowercase().contains(q) }
    }
    var liveSort by remember { mutableStateOf("Telespectadores") }
    // Lives no ar: o servidor ainda não tem essa lista; quando existir, é só preencher aqui.
    val topLives = remember { emptyList<QinblyLive>() }
    val voiceLauncher = androidx.activity.compose.rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val spoken = result.data?.getStringArrayListExtra(android.speech.RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
        if (!spoken.isNullOrBlank()) query = spoken.take(40)
    }
    val startVoice: () -> Unit = {
        try {
            voiceLauncher.launch(Intent(android.speech.RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                .putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE_MODEL, android.speech.RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                .putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE, "pt-BR")
                .putExtra(android.speech.RecognizerIntent.EXTRA_PROMPT, "Fale o que quer pesquisar"))
        } catch (_: Exception) { onError("A pesquisa por voz não está disponível neste aparelho.") }
    }
    // Pesquisas em alta (palavras normais): por enquanto, as palavras mais repetidas nas legendas do feed carregado.
    val hotTerms = remember(QinblyCache.feed) {
        val stop = setOf("para", "com", "que", "uma", "uns", "umas", "dos", "das", "mais", "muito", "isso", "esse", "essa", "este", "esta", "você", "voce", "como", "mas", "por", "não", "nao", "hoje", "está", "esta", "foi", "tem", "ser", "seu", "sua", "nos", "nas", "aqui", "tudo", "todos", "vamos", "pra", "pro", "bom", "dia")
        QinblyCache.feed.flatMap { post ->
            post.caption.lowercase().replace(Regex("[#@][\\p{L}\\p{N}_]+"), " ").split(Regex("[^\\p{L}\\p{N}]+"))
                .filter { it.length >= 4 && it !in stop }.distinct()
        }.groupingBy { it }.eachCount().entries.filter { it.value >= 1 }.sortedByDescending { it.value }.take(6).map { it.key }
    }
    val openTag: (String) -> Unit = { tag -> saveHistory("#$tag"); onDismiss(); QinblyTagNav.tag = tag }
    val openUser: (String) -> Unit = { username -> saveHistory("@$username"); onOpenProfile(username) }

    // Só consulta quando há pelo menos dois caracteres e descarta resultados de buscas antigas.
    var searchRequest by remember { mutableIntStateOf(0) }
    LaunchedEffect(query, filter) {
        val requestId = ++searchRequest
        kotlinx.coroutines.delay(250)
        val normalized = query.trim().removePrefix("@").trim()
        searchError = ""
        if (normalized.length < 2 || query.trim().startsWith("#") || filter != "Tudo" && filter != "Pessoas") {
            results = emptyList()
            loading = false
            searched = false
        } else {
            loading = true
            try {
                val found = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    QinblyApi.searchUsers(normalized, prefs.getString("token", null))
                }
                if (requestId == searchRequest) {
                    QinblyEngagement.learnFromUsers(prefs, found)
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
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = QiblyTheme.text)
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
                    placeholder = { Text("Pessoas, #hashtags, publicações") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Muted) },
                    trailingIcon = {
                        if (query.isNotEmpty()) IconButton(onClick = { query = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Limpar busca", tint = Muted)
                        } else IconButton(onClick = startVoice) {
                            Icon(QinblyCameraIcons.mic, contentDescription = "Pesquisar por voz", tint = Muted)
                        }
                    },
                    shape = RoundedCornerShape(28.dp),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Search),
                    keyboardActions = androidx.compose.foundation.text.KeyboardActions(onSearch = { saveHistory(query) })
                )
                LazyRow(Modifier.padding(bottom = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(listOf("Tudo", "Pessoas", "Hashtags", "Vídeos")) { name ->
                        val chosen = name == filter
                        Text(name, fontSize = 13.sp, color = if (chosen) QiblyTheme.background else QiblyTheme.text,
                            modifier = Modifier.clip(RoundedCornerShape(18.dp))
                                .background(if (chosen) QiblyTheme.text else Color.Transparent)
                                .border(0.5.dp, Muted, RoundedCornerShape(18.dp))
                                .clickable { filter = name }.padding(horizontal = 14.dp, vertical = 6.dp))
                    }
                }
                if (query.isNotBlank() && filter != "Vídeos") Text(
                    "Resultados da busca",
                    color = Muted,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(vertical = 6.dp)
                )
                when {
                    filter == "Vídeos" -> when {
                        videosLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Pink) }
                        videoMatches.isEmpty() -> Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                            Text(if (query.isBlank()) "Ainda não há vídeos publicados." else "Nenhum vídeo encontrado para essa busca.", color = Muted, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        }
                        else -> androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                            columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(3), modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.spacedBy(3.dp), verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            items(videoMatches.size, key = { videoMatches[it].id }) { i ->
                                val v = videoMatches[i]
                                Box(Modifier.aspectRatio(0.75f).background(Color(0xFF111111)).clickable { saveHistory(query); videoViewerId = v.id }) {
                                    QinblyVideoPoster(v.mediaUrl, Modifier.fillMaxSize(), ContentScale.Crop)
                                    Text("@" + v.username, color = Color.White, fontSize = 11.sp, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                        modifier = Modifier.align(Alignment.BottomStart).padding(5.dp))
                                }
                            }
                        }
                    }
                    query.trim().startsWith("#") || (filter == "Hashtags" && query.isNotBlank()) -> {
                        val typedTag = query.trim().removePrefix("#").filter { it.isLetterOrDigit() || it == '_' }
                        if (typedTag.length < 2) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Digite o nome da hashtag.", color = Muted) }
                        } else {
                            Row(
                                Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).clickable { openTag(typedTag.lowercase()) }.padding(horizontal = 4.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(Modifier.size(48.dp).clip(CircleShape).background(QinblyBrand.AccentSoft), contentAlignment = Alignment.Center) {
                                    Text("#", color = QinblyBrand.Accent, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                                }
                                Text("Ver publicações de #${typedTag.lowercase()}", color = QiblyTheme.text, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                    loading && results.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Pink)
                    }
                    searchError.isNotBlank() && results.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(searchError, color = Muted, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                            Button(onClick = { query = query + " " }) { Text("Tentar novamente") }
                        }
                    }
                    query.trim().removePrefix("@").trim().length < 2 -> LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 20.dp)
                    ) {
                        if (history.isNotEmpty() && (filter == "Tudo" || filter == "Pessoas")) {
                            item {
                                Row(Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text("Recentes", color = QiblyTheme.text, fontWeight = FontWeight.Bold, fontSize = 17.sp, modifier = Modifier.weight(1f))
                                    Text("Limpar tudo", color = QinblyBrand.Accent, fontSize = 13.sp,
                                        modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable { history = emptyList(); prefs.edit().remove("qinbly_search_history").apply() }.padding(6.dp))
                                }
                            }
                            items(history.size, key = { "h-" + history[it] }) { i ->
                                val term = history[i]
                                Row(
                                    Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable { query = term }.padding(horizontal = 4.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(QinblyCameraIcons.clock, contentDescription = null, tint = Muted, modifier = Modifier.size(20.dp))
                                    Text(term, color = QiblyTheme.text, modifier = Modifier.weight(1f), maxLines = 1)
                                    IconButton(onClick = { removeHistory(term) }, modifier = Modifier.size(32.dp)) {
                                        Icon(Icons.Default.Close, contentDescription = "Remover", tint = Muted, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                        if (filter == "Tudo") {
                            item { QinblyTopLives(topLives, liveSort, { liveSort = it }, prefs) }
                            if (hotTerms.isNotEmpty()) {
                                item { Text("Pesquisas em alta", color = QiblyTheme.text, fontWeight = FontWeight.Bold, fontSize = 17.sp, modifier = Modifier.padding(top = 14.dp, bottom = 4.dp)) }
                                items(hotTerms.size) { i ->
                                    val term = hotTerms[i]
                                    Row(
                                        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable { query = term; saveHistory(term) }.padding(horizontal = 4.dp, vertical = 9.dp),
                                        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Icon(QinblyCameraIcons.trend, contentDescription = null, tint = QinblyBrand.Accent, modifier = Modifier.size(20.dp))
                                        Text(term, color = QiblyTheme.text, modifier = Modifier.weight(1f), maxLines = 1)
                                    }
                                }
                            }
                            item {
                                Text("Explorar por assunto", color = QiblyTheme.text, fontWeight = FontWeight.Bold, fontSize = 17.sp, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
                                androidx.compose.foundation.layout.FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf("Música", "Comida", "Viagem", "Esportes", "Humor", "Moda").forEach { topic ->
                                        Text(topic, color = QiblyTheme.text, fontSize = 14.sp,
                                            modifier = Modifier.clip(RoundedCornerShape(18.dp)).border(0.5.dp, Muted, RoundedCornerShape(18.dp))
                                                .clickable { query = topic; saveHistory(topic) }.padding(horizontal = 14.dp, vertical = 7.dp))
                                    }
                                }
                            }
                        }
                        if ((filter == "Tudo" || filter == "Hashtags") && hotTags.isNotEmpty()) {
                            item { QinblySplit {
                                Text("Hashtags mais usadas", color = QiblyTheme.text, fontWeight = FontWeight.Bold, fontSize = 17.sp, modifier = Modifier.padding(top = 16.dp, bottom = 4.dp))
                                hotTags.take(5).forEachIndexed { i, (tag, count) ->
                                    Row(
                                        Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).clickable { openTag(tag) }.padding(vertical = 9.dp),
                                        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Text("${i + 1}", modifier = Modifier.width(22.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center, fontWeight = FontWeight.Bold, fontSize = 15.sp,
                                            color = when (i) { 0 -> Color(0xFFF5B800); 1 -> Color(0xFFA8B0BC); 2 -> Color(0xFFC98A4B); else -> Muted })
                                        Text("#$tag", color = QinblyBrand.Accent, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, modifier = Modifier.weight(1f))
                                        Text(qinblyPostsLabel(count), color = Muted, fontSize = 13.sp)
                                    }
                                }
                            } }
                        }
                        if ((filter == "Tudo" || filter == "Hashtags") && myTags.isNotEmpty()) {
                            item { QinblySplit {
                                Row(Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text("Suas hashtags", color = QiblyTheme.text, fontWeight = FontWeight.Bold, fontSize = 17.sp, modifier = Modifier.weight(1f))
                                    Text("Limpar", color = QinblyBrand.Accent, fontSize = 14.sp, modifier = Modifier.clickable { QinblyHashtagStore.clear(context); myTags = emptyList() }.padding(4.dp))
                                }
                                androidx.compose.foundation.layout.FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    myTags.take(10).forEach { (tag, times) ->
                                        Text("#$tag · ${times}×", color = QinblyBrand.Accent, fontSize = 14.sp,
                                            modifier = Modifier.clip(RoundedCornerShape(18.dp)).background(QinblyBrand.AccentSoft)
                                                .clickable { openTag(tag) }.padding(horizontal = 13.dp, vertical = 7.dp))
                                    }
                                }
                            } }
                        }
                        if (history.isEmpty() && hotTags.isEmpty() && hotTerms.isEmpty() && filter != "Tudo") {
                            item { Text("Digite pelo menos 2 letras para encontrar pessoas, ou use # para buscar hashtags.", color = Muted, modifier = Modifier.padding(vertical = 24.dp)) }
                        }
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
                            QinblyUserRow(user, prefs, openUser, onError)
                        }
                    }
                }
            }
        }
        videoViewerId?.let { startId ->
            val viewable = qinblyViewerList(videoMatches, startId)
            if (viewable.any { it.id == startId }) {
                QinblyPostViewer(
                    posts = viewable, startPostId = startId, prefs = prefs, currentUsername = prefs.getString("username", "") ?: "", onNotice = onError,
                    onDismiss = { videoViewerId = null }, onComment = { videoCommentTarget = it }, onShare = { videoShareTarget = it },
                    onOpenProfile = { videoViewerId = null; onOpenProfile(it) }
                )
            }
        }
        videoCommentTarget?.let { target ->
            QinblyCommentsDialog(
                post = target, prefs = prefs, currentUsername = prefs.getString("username", "") ?: "", onNotice = onError,
                onDismiss = { videoCommentTarget = null },
                onCountChanged = { postId, count -> videoPosts = videoPosts.map { if (it.id == postId) it.copy(comments = count) else it } }
            )
        }
        videoShareTarget?.let { target -> QiblyShareDialog(post = target, prefs = prefs, onDismiss = { videoShareTarget = null }, onNotice = onError) }
    }
}

/** Primeiro quadro do vídeo mostrado enquanto o player ainda carrega (em vez de um retângulo preto). */
@Composable
private fun QinblyVideoPoster(url: String, modifier: Modifier = Modifier, contentScale: ContentScale = ContentScale.Fit) {
    if (!url.startsWith("http")) return
    val context = androidx.compose.ui.platform.LocalContext.current
    val request = remember(url) {
        coil.request.ImageRequest.Builder(context)
            .data(url)
            .decoderFactory(coil.decode.VideoFrameDecoder.Factory())
            .videoFrameMillis(500)
            .memoryCacheKey("poster:$url")
            .diskCacheKey("poster:$url")
            .crossfade(true)
            .build()
    }
    AsyncImage(model = request, contentDescription = null, modifier = modifier, contentScale = contentScale)
}

@Composable
private fun QinblyLoadingSpinner(modifier: Modifier = Modifier, tint: Color = Color.White) {
    Box(modifier, contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = tint, strokeWidth = 3.dp, modifier = Modifier.size(34.dp))
    }
}

/** Coração que aparece e some no centro quando a pessoa curte com dois toques. `trigger` sobe a cada duplo toque. */
@Composable
private fun QinblyHeartBurst(trigger: Int, modifier: Modifier = Modifier) {
    val progress = remember { androidx.compose.animation.core.Animatable(1f) }
    LaunchedEffect(trigger) {
        if (trigger > 0) {
            progress.snapTo(0f)
            progress.animateTo(1f, androidx.compose.animation.core.tween(if (QiblyTheme.reduceMotion) 250 else 750))
        }
    }
    val p = progress.value
    if (trigger > 0 && p < 1f) {
        Icon(
            Icons.Default.Favorite, contentDescription = null, tint = Color(0xFFFF2D3F),
            modifier = modifier.size(96.dp).graphicsLayer {
                val sc = if (QiblyTheme.reduceMotion) 1f else 0.6f + 0.6f * minOf(p * 3f, 1f)
                scaleX = sc; scaleY = sc
                alpha = if (p < 0.6f) 1f else 1f - (p - 0.6f) / 0.4f
            }
        )
    }
}

@Composable
private fun QinblyFullscreenVideo(url: String, modifier: Modifier = Modifier, showProgress: Boolean = true, onDoubleTap: (() -> Unit)? = null) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var firstFrameShown by remember(url) { mutableStateOf(false) }
    var buffering by remember(url) { mutableStateOf(true) }
    val player = remember(url) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(url))
            repeatMode = androidx.media3.common.Player.REPEAT_MODE_ONE
            addListener(object : androidx.media3.common.Player.Listener {
                override fun onRenderedFirstFrame() { firstFrameShown = true }
                override fun onPlaybackStateChanged(playbackState: Int) { buffering = playbackState == androidx.media3.common.Player.STATE_BUFFERING }
            })
            prepare()
            playWhenReady = true
        }
    }
    var playing by remember(url) { mutableStateOf(true) }
    var likeBurst by remember(url) { mutableIntStateOf(0) }
    DisposableEffect(Unit) {
        QinblyVideoFocus.fullscreenCount += 1
        onDispose { QinblyVideoFocus.fullscreenCount = (QinblyVideoFocus.fullscreenCount - 1).coerceAtLeast(0) }
    }
    val doubleTapAction by rememberUpdatedState(onDoubleTap)
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
        AndroidView(modifier = Modifier.fillMaxSize(), factory = { ctx -> PlayerView(ctx).apply { this.player = player; useController = false; resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT } }, update = { view -> view.player = player; view.useController = false; view.resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT })
        if (!firstFrameShown) QinblyVideoPoster(url, Modifier.fillMaxSize())
        if (!firstFrameShown || buffering) QinblyLoadingSpinner(Modifier.fillMaxSize())
        Box(Modifier.fillMaxSize().pointerInput(url) {
            detectTapGestures(
                onTap = { if (player.isPlaying) { player.pause(); playing = false } else { player.play(); playing = true } },
                onDoubleTap = { doubleTapAction?.let { it(); likeBurst++ } }
            )
        }, contentAlignment = Alignment.Center) {
            if (!playing) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Continuar vídeo", tint = Color.White, modifier = Modifier.size(52.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.42f)).padding(12.dp))
            }
        }
        QinblyHeartBurst(likeBurst, Modifier.align(Alignment.Center))
        if (showProgress) Row(
            Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                .padding(start = 18.dp, end = 18.dp, bottom = 118.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BoxWithConstraints(
                Modifier.weight(1f).height(32.dp)
                    .pointerInput(duration) {
                        detectTapGestures { tap ->
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
                Box(Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(3.dp)).background(Color.White.copy(alpha = 0.9f)))
                Box(Modifier.fillMaxWidth(position.coerceIn(0f, 1f)).height(4.dp).clip(RoundedCornerShape(3.dp)).background(Color(0xFF168BFF)))
                Box(Modifier.offset(x = maxWidth * position.coerceIn(0f, 1f) - 7.dp).size(14.dp).clip(CircleShape).background(Color.White))
            }
            val seconds = (position * duration).toLong().coerceAtLeast(0L) / 1000L
            val total = duration.coerceAtLeast(0L) / 1000L
            Text("${seconds / 60}:${(seconds % 60).toString().padStart(2, '0')} / ${total / 60}:${(total % 60).toString().padStart(2, '0')}", color = Color.White, fontSize = 12.sp)
        }
    }
}

@Composable
private fun QiblyPublicationVideo(url: String, modifier: Modifier = Modifier) {
    val saverContext = androidx.compose.ui.platform.LocalContext.current
    // Economia de dados: com a opção ligada, o vídeo só carrega quando a pessoa aperta play.
    val dataSaver = remember { saverContext.getSharedPreferences("qinbly_session", android.content.Context.MODE_PRIVATE).getBoolean("setting_data_saver", false) }
    Box(modifier.clip(RoundedCornerShape(16.dp)).background(Color.Black)) {
        InlineVideoPlayer(url = url, modifier = Modifier.fillMaxSize(), autoplay = !dataSaver)
    }
}

@Composable
private fun InlineVideoPlayer(url: String, modifier: Modifier = Modifier, autoplay: Boolean = false) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var playbackError by remember(url) { mutableStateOf(false) }
    var isPlaying by remember(url) { mutableStateOf(autoplay) }
    var firstFrameShown by remember(url) { mutableStateOf(false) }
    var buffering by remember(url) { mutableStateOf(false) }
    // Sem autoplay, o vídeo só é preparado (e só consome rede) quando a pessoa aperta play; até lá aparece o primeiro quadro.
    var prepared by remember(url) { mutableStateOf(false) }
    var retryCount by remember(url) { mutableIntStateOf(0) }
    var resumePos by remember(url) { mutableLongStateOf(0L) }
    val videoPrefs = remember { context.getSharedPreferences("qinbly_session", android.content.Context.MODE_PRIVATE) }
    // Reprodução automática no feed começa sem som; o botão do alto-falante liga.
    var muted by remember(url) { mutableStateOf(!QinblyVideoFocus.soundOn && (autoplay || videoPrefs.getBoolean("setting_video_muted", false))) }
    val instanceKey = remember { java.util.UUID.randomUUID().toString() }
    var progress by remember(url) { mutableFloatStateOf(0f) }
    val player = remember(url) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(url))
            volume = if (!QinblyVideoFocus.soundOn && (autoplay || videoPrefs.getBoolean("setting_video_muted", false))) 0f else 1f
            repeatMode = if (autoplay || videoPrefs.getBoolean("setting_video_loop", false)) androidx.media3.common.Player.REPEAT_MODE_ONE else androidx.media3.common.Player.REPEAT_MODE_OFF
            // Só começa a tocar quando este vídeo for o que está em foco na tela (veja QinblyVideoFocus).
            playWhenReady = false
            addListener(object : androidx.media3.common.Player.Listener {
                override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                    // Falhas passageiras (rede lenta, decodificador ocupado) ganham até 3 novas tentativas antes de mostrar o erro.
                    if (prepared && retryCount < 3) {
                        retryCount += 1
                        val wait = 700L * retryCount
                        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                            try {
                                prepare()
                                if (resumePos > 0L) seekTo(resumePos)
                                if (QinblyVideoFocus.focusedKey() == instanceKey || isPlaying) play()
                            } catch (_: Exception) { playbackError = true; isPlaying = false }
                        }, wait)
                    } else {
                        playbackError = true
                        isPlaying = false
                    }
                }
                override fun onRenderedFirstFrame() { firstFrameShown = true }
                override fun onPlaybackStateChanged(playbackState: Int) {
                    buffering = playbackState == androidx.media3.common.Player.STATE_BUFFERING
                    if (playbackState == androidx.media3.common.Player.STATE_READY) { playbackError = false; retryCount = 0 }
                }
            })
        }
    }
    DisposableEffect(player) { onDispose { player.release() } }
    val volumeTicksAtStart = remember(url) { QinblyVolumeKey.upTicks }
    val isFocused by remember(instanceKey) { derivedStateOf { QinblyVideoFocus.focusedKey() == instanceKey } }
    DisposableEffect(instanceKey) { onDispose { QinblyVideoFocus.visible.remove(instanceKey) } }
    LaunchedEffect(QinblyVolumeKey.upTicks) {
        // A tecla de volume liga o som só do vídeo que está em foco.
        if (QinblyVolumeKey.upTicks > volumeTicksAtStart && muted && isFocused) { muted = false; player.volume = 1f; QinblyVideoFocus.soundOn = true }
    }
    LaunchedEffect(isFocused, autoplay) {
        if (isFocused) {
            if (QinblyVideoFocus.soundOn && muted) { muted = false; player.volume = 1f }
            if (autoplay && !playbackError) {
                if (!prepared) { player.prepare(); prepared = true; if (resumePos > 0L) player.seekTo(resumePos) }
                player.play()
                isPlaying = true
            }
        } else {
            player.pause()
            isPlaying = false
            // Fora de foco, solta o decodificador: vários vídeos preparados ao mesmo tempo causavam o erro de reprodução.
            if (prepared) {
                resumePos = player.currentPosition
                player.stop()
                prepared = false
                firstFrameShown = false
                buffering = false
            }
        }
    }
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val focusedNow by rememberUpdatedState(isFocused)
    DisposableEffect(lifecycleOwner, player) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_PAUSE) { player.pause(); isPlaying = false }
            else if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME && focusedNow && autoplay && !playbackError) { player.play(); isPlaying = true }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    LaunchedEffect(player, isPlaying) {
        while (isPlaying) {
            val total = player.duration
            progress = if (total > 0) (player.currentPosition.toFloat() / total).coerceIn(0f, 1f) else 0f
            kotlinx.coroutines.delay(250)
        }
    }
    Box(modifier = modifier.background(Color.Black).onGloballyPositioned { coords ->
        // Mede quanto deste vídeo aparece na tela; o mais visível ganha o foco.
        val height = coords.size.height.toFloat()
        val fraction = if (height <= 0f || !coords.isAttached) 0f else {
            val bounds = coords.boundsInWindow()
            ((bounds.bottom - bounds.top) / height).coerceIn(0f, 1f)
        }
        val before = QinblyVideoFocus.visible[instanceKey]
        if (before == null || kotlin.math.abs(before - fraction) >= 0.03f || fraction == 0f || fraction == 1f) QinblyVideoFocus.visible[instanceKey] = fraction
    }, contentAlignment = Alignment.Center) {
        // Fundo desfocado do próprio vídeo preenche as laterais de vídeos verticais (em vez de faixas lisas).
        if (!playbackError && android.os.Build.VERSION.SDK_INT >= 31) {
            QinblyVideoPoster(url, Modifier.fillMaxSize().blur(32.dp).alpha(0.55f), ContentScale.Crop)
        }
        if (playbackError) {
            Text(
                "Não foi possível reproduzir este vídeo.",
                color = Color.White,
                modifier = Modifier.align(Alignment.TopCenter).padding(16.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Box(Modifier.size(62.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.62f))
                .clickable {
                    playbackError = false
                    player.prepare()
                    prepared = true
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
                    setShutterBackgroundColor(android.graphics.Color.TRANSPARENT)
                    setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
                    resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                } },
                update = { view ->
                    view.player = player
                    view.useController = false
                    view.resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                }
            )
            if (!firstFrameShown) {
                QinblyVideoPoster(url, Modifier.fillMaxSize(), ContentScale.Crop)
            }
            if (isPlaying && (buffering || !firstFrameShown)) QinblyLoadingSpinner(Modifier.fillMaxSize(), tint = QinblyBrand.Accent)
            if (!isPlaying) {
                Box(
                    modifier = Modifier.size(62.dp).clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.55f))
                        .clickable {
                            if (!prepared) { player.prepare(); prepared = true }
                            player.play()
                            isPlaying = true
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Reproduzir vídeo", tint = Color.White, modifier = Modifier.size(38.dp))
                }
            } else {
                Box(Modifier.fillMaxSize().clickable { player.pause(); isPlaying = false })
                // Barra de progresso fina + botão de som, só enquanto o vídeo toca.
                Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(3.dp).background(Color.White.copy(alpha = 0.25f))) {
                    Box(Modifier.fillMaxHeight().fillMaxWidth(progress).background(QinblyBrand.Accent))
                }
                Box(
                    Modifier.align(Alignment.BottomEnd).padding(end = 10.dp, bottom = 14.dp).size(30.dp).clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.18f))
                        .border(0.5.dp, Color.White.copy(alpha = 0.35f), CircleShape)
                        .clickable { muted = !muted; player.volume = if (muted) 0f else 1f; QinblyVideoFocus.soundOn = !muted },
                    contentAlignment = Alignment.Center
                ) { Icon(if (muted) Icons.Default.VolumeOff else Icons.Default.VolumeUp, contentDescription = if (muted) "Ligar o som" else "Desligar o som", tint = Color.White, modifier = Modifier.size(16.dp)) }
            }
        }
    }
}

// =====================================================================================
// AÇÕES PADRONIZADAS: curtir, comentar, repostar, salvar, enviar e seguir.
// Todas as telas usam os MESMOS componentes e o MESMO estado compartilhado: o que o usuário
// fizer em uma tela (curtir, salvar, seguir...) aparece nas demais na hora.
// =====================================================================================
private const val QINBLY_POST_LINK_BASE = "https://qinbly.com/post/"
private fun qinblyPostLink(postId: String): String = QINBLY_POST_LINK_BASE + Uri.encode(postId)

/** Onde o botão aparece: define só as cores (os tamanhos, ícones e comportamento são sempre iguais). */
private enum class QinblyTone { Feed, Screen, Overlay }

private fun qinblyIconTint(tone: QinblyTone): Color = when (tone) {
    QinblyTone.Feed -> RemoteAppearance.actionIconColor
    QinblyTone.Screen -> RemoteAppearance.screenTextColor
    QinblyTone.Overlay -> Color.White
}

private fun qinblyCountColor(tone: QinblyTone): Color = when (tone) {
    QinblyTone.Feed -> RemoteAppearance.feedSecondaryTextColor
    QinblyTone.Screen -> RemoteAppearance.screenSecondaryTextColor
    QinblyTone.Overlay -> Color.White
}

/** Cor de fundo neutra de imagens ainda carregando (nunca preto nem roxo escuro). */
private val qinblyPlaceholderColor: Color get() = if (QiblyTheme.dark) Color(0xFF222C3D) else QiblyTheme.border

/**
 * Memória das últimas respostas: ao voltar para uma tela, ela já abre com o que foi visto e atualiza em segundo plano
 * (em vez de abrir vazia e esperar a rede).
 */
/** Último estado carregado do próprio perfil: trocar de aba e voltar mostra tudo na hora, sem tela vazia. */
private data class OwnProfileSnapshot(
    val username: String, val postsCount: Int, val followers: Int, val following: Int,
    val avatarUrl: String, val profilePhotoUrl: String, val originalCoverUrl: String,
    val frame: Int, val joined: String, val posts: List<QinblyApi.PostSummary>
)

private object QinblyCache {
    var feed: List<QinblyApi.PostSummary> = emptyList()
    var inbox: List<QinblyApi.InboxSummary> = emptyList()
    var own: OwnProfileSnapshot? = null
    val profiles = HashMap<String, Pair<org.json.JSONObject, List<QinblyApi.PostSummary>>>()
    val communities = HashMap<String, List<QinblyApi.CommunitySummary>>()
    /** Ao trocar/sair de conta, nada da conta anterior pode aparecer na próxima. */
    fun clear(context: android.content.Context) {
        feed = emptyList()
        inbox = emptyList()
        own = null
        QinblyAvatarCache.clear()
        profiles.clear()
        communities.clear()
        context.getSharedPreferences("qinbly_profile_snapshot", android.content.Context.MODE_PRIVATE).edit().clear().apply()
        context.getSharedPreferences("qbly_local_cache", android.content.Context.MODE_PRIVATE).edit().remove("feed_posts_v2").apply()
    }
    fun profileKey(username: String): String = username.removePrefix("@").trim().lowercase()
}

/**
 * Estado e regras únicos das ações. Curtir e seguir são otimistas (a tela muda na hora e volta atrás se o servidor falhar).
 * O estado de curtida é guardado como "o que eu fiz" por cima do que o servidor informou; assim a contagem nunca
 * conta duas vezes, mesmo depois de o feed ser recarregado.
 */
private object QinblyEngagement {
    private const val SAVED_KEY = "saved_post_ids"
    private const val REPOSTED_KEY = "reposted_post_ids_local"
    private const val DEMO_LIKED_KEY = "demo_liked_post_ids"
    private val mainHandler = android.os.Handler(android.os.Looper.getMainLooper())

    var savedIds by mutableStateOf<Set<String>>(emptySet())
        private set
    var repostedIds by mutableStateOf<Set<String>>(emptySet())
        private set
    private val likeOverride = mutableStateMapOf<String, Boolean>()
    private val followOverride = mutableStateMapOf<String, Boolean>()
    private val followBusy = mutableStateMapOf<String, Boolean>()
    private val busyLikes = HashSet<String>()
    private var demoLiked: Set<String> = emptySet()
    private var sessionPrefs: android.content.SharedPreferences? = null
    private var ready = false

    /** Chamado uma vez ao abrir o app, fora da composição. */
    fun init(prefs: android.content.SharedPreferences) {
        if (ready) return
        sessionPrefs = prefs
        savedIds = prefs.getStringSet(SAVED_KEY, emptySet())?.toSet() ?: emptySet()
        repostedIds = prefs.getStringSet(REPOSTED_KEY, emptySet())?.toSet() ?: emptySet()
        demoLiked = prefs.getStringSet(DEMO_LIKED_KEY, emptySet())?.toSet() ?: emptySet()
        ready = true
    }

    // As marcações valem por conta: trocar de conta não herda curtidas/seguindo da anterior.
    private fun owner(): String = sessionPrefs?.getString("username", "").orEmpty().trim().lowercase()
    private fun key(id: String): String = owner() + "|" + id.removePrefix("@").lowercase()
    private fun isLocalOnly(postId: String): Boolean = postId.startsWith("demo-post-")

    // ---- Curtir ----
    fun isLiked(post: QinblyApi.PostSummary): Boolean =
        likeOverride[key(post.id)] ?: if (isLocalOnly(post.id)) post.id in demoLiked else post.likedByMe

    fun likeCount(post: QinblyApi.PostSummary): Int {
        val mine = likeOverride[key(post.id)] ?: return post.likes
        return when {
            mine == post.likedByMe -> post.likes
            mine -> post.likes + 1
            else -> (post.likes - 1).coerceAtLeast(0)
        }
    }

    fun toggleLike(prefs: android.content.SharedPreferences, post: QinblyApi.PostSummary, onNotice: (String) -> Unit) {
        val k = key(post.id)
        val wasLiked = isLiked(post)
        if (isLocalOnly(post.id)) {
            val next = !wasLiked
            likeOverride[k] = next
            demoLiked = if (next) demoLiked + post.id else demoLiked - post.id
            prefs.edit().putStringSet(DEMO_LIKED_KEY, demoLiked).apply()
            return
        }
        val token = prefs.getString("token", null)
        if (token.isNullOrBlank()) {
            onNotice("Entre na sua conta para curtir publicações.")
            return
        }
        if (!busyLikes.add(k)) return
        likeOverride[k] = !wasLiked
        Thread {
            try {
                val serverLiked = QinblyApi.toggleLike(token, post.id)
                mainHandler.post { likeOverride[k] = serverLiked; busyLikes.remove(k) }
            } catch (e: Exception) {
                mainHandler.post {
                    likeOverride[k] = wasLiked
                    busyLikes.remove(k)
                    onNotice(e.message ?: "Não foi possível curtir.")
                }
            }
        }.start()
    }

    // ---- Salvar e repostar: gravados no servidor (publicações de demonstração ficam só neste aparelho) ----
    private val saveOverride = mutableStateMapOf<String, Boolean>()
    private val repostOverride = mutableStateMapOf<String, Boolean>()
    private val busyToggles = HashSet<String>()

    fun isSaved(post: QinblyApi.PostSummary): Boolean =
        saveOverride[key(post.id)] ?: if (isLocalOnly(post.id)) post.id in savedIds else post.savedByMe

    fun isReposted(post: QinblyApi.PostSummary): Boolean =
        repostOverride[key(post.id)] ?: if (isLocalOnly(post.id)) post.id in repostedIds else post.repostedByMe

    fun repostCount(post: QinblyApi.PostSummary): Int {
        val mine = repostOverride[key(post.id)] ?: return post.reposts
        return when {
            mine == post.repostedByMe -> post.reposts
            mine -> post.reposts + 1
            else -> (post.reposts - 1).coerceAtLeast(0)
        }
    }

    private fun toggleOnServer(
        prefs: android.content.SharedPreferences,
        post: QinblyApi.PostSummary,
        kind: String,
        overrides: MutableMap<String, Boolean>,
        current: Boolean,
        localKey: String,
        localSet: Set<String>,
        setLocal: (Set<String>) -> Unit,
        call: (String, String) -> Boolean,
        message: (Boolean) -> String,
        onNotice: (String) -> Unit
    ) {
        val k = key(post.id)
        val next = !current
        if (isLocalOnly(post.id)) {
            overrides[k] = next
            val updated = if (next) localSet + post.id else localSet - post.id
            setLocal(updated)
            prefs.edit().putStringSet(localKey, updated).apply()
            onNotice(message(next))
            return
        }
        val token = prefs.getString("token", null)
        if (token.isNullOrBlank()) {
            onNotice("Entre na sua conta para " + (if (kind == "save") "salvar" else "repostar") + " publicações.")
            return
        }
        if (!busyToggles.add(kind + k)) return
        overrides[k] = next
        Thread {
            try {
                val confirmed = call(token, post.id)
                mainHandler.post { overrides[k] = confirmed; busyToggles.remove(kind + k); onNotice(message(confirmed)) }
            } catch (e: Exception) {
                mainHandler.post {
                    overrides[k] = current
                    busyToggles.remove(kind + k)
                    onNotice(e.message ?: "Não foi possível concluir agora.")
                }
            }
        }.start()
    }

    fun toggleSaved(prefs: android.content.SharedPreferences, post: QinblyApi.PostSummary, onNotice: (String) -> Unit) =
        toggleOnServer(prefs, post, "save", saveOverride, isSaved(post), SAVED_KEY, savedIds, { savedIds = it },
            QinblyApi::toggleSave, { if (it) "Publicação salva." else "Publicação removida dos salvos." }, onNotice)

    fun toggleReposted(prefs: android.content.SharedPreferences, post: QinblyApi.PostSummary, onNotice: (String) -> Unit) =
        toggleOnServer(prefs, post, "repost", repostOverride, isReposted(post), REPOSTED_KEY, repostedIds, { repostedIds = it },
            QinblyApi::toggleRepost, { if (it) "Você repostou esta publicação." else "Repost removido." }, onNotice)

    // ---- Seguir ----
    fun isFollowing(prefs: android.content.SharedPreferences, username: String, serverValue: Boolean): Boolean =
        followOverride[key(username)] ?: (serverValue || prefs.getBoolean("following_" + username.removePrefix("@").lowercase(), false))

    fun isFollowBusy(username: String): Boolean = followBusy[key(username)] == true

    /** Aprende o estado de seguir vindo do servidor (listas, feed, perfis) para todas as telas mostrarem o mesmo botão. */
    fun learnFollows(prefs: android.content.SharedPreferences, states: List<Pair<String, Boolean>>) {
        val me = prefs.getString("username", "").orEmpty().removePrefix("@").lowercase()
        val editor = prefs.edit()
        for ((name, following) in states) {
            val handle = name.removePrefix("@").lowercase()
            if (handle.isBlank() || handle == me || followBusy[key(handle)] == true) continue
            followOverride[key(handle)] = following
            editor.putBoolean("following_$handle", following)
        }
        editor.apply()
    }

    fun learnFromPosts(prefs: android.content.SharedPreferences, posts: List<QinblyApi.PostSummary>) =
        learnFollows(prefs, posts.map { it.username to it.isFollowing })

    fun learnFromUsers(prefs: android.content.SharedPreferences, users: List<QinblyApi.UserSummary>) =
        learnFollows(prefs, users.map { it.username to it.isFollowing })

    /** Grava o que o servidor informou (ex.: ao abrir um perfil) e descarta ajustes locais antigos. */
    fun syncFollow(prefs: android.content.SharedPreferences, username: String, following: Boolean) {
        followOverride.remove(key(username))
        prefs.edit().putBoolean("following_" + username.removePrefix("@").lowercase(), following).apply()
    }

    /** knownUserId pode vir vazio: nesse caso o id é buscado pelo perfil. onDone roda na thread principal após o sucesso. */
    fun toggleFollow(
        prefs: android.content.SharedPreferences,
        username: String,
        knownUserId: String,
        displayName: String,
        onNotice: (String) -> Unit,
        notifySuccess: Boolean = true,
        onDone: (Boolean) -> Unit = {}
    ) {
        val handle = username.removePrefix("@").lowercase()
        val token = prefs.getString("token", null)
        if (token.isNullOrBlank()) {
            onNotice("Entre na sua conta para seguir pessoas.")
            return
        }
        val k = key(handle)
        if (followBusy[k] == true) return
        followBusy[k] = true
        Thread {
            try {
                val targetId = knownUserId.ifBlank {
                    val profile = QinblyApi.getPublicProfile(handle, token)
                    (profile.optJSONObject("user") ?: profile).optString("id")
                }
                if (targetId.isBlank()) throw IllegalStateException("Não foi possível localizar este perfil.")
                val following = QinblyApi.toggleFollow(token, targetId)
                val label = displayName.ifBlank { handle }
                mainHandler.post {
                    followOverride[k] = following
                    followBusy[k] = false
                    prefs.edit().putBoolean("following_" + handle, following).apply()
                    if (notifySuccess) onNotice(if (following) "Você está seguindo $label." else "Você deixou de seguir $label.")
                    onDone(following)
                }
            } catch (e: Exception) {
                mainHandler.post {
                    followBusy[k] = false
                    onNotice(e.message ?: "Não foi possível atualizar o seguimento.")
                }
            }
        }.start()
    }
}

/** Base de todo botão de ação: mesma área de toque, mesmo ícone de 24dp, mesmo contador. */
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun QinblyActionButton(
    description: String,
    onClick: () -> Unit,
    tone: QinblyTone,
    vertical: Boolean,
    count: String?,
    onLongClick: (() -> Unit)? = null,
    icon: @Composable () -> Unit
) {
    val content: @Composable () -> Unit = {
        icon()
        if (count != null) Text(count, color = qinblyCountColor(tone), fontSize = 12.sp)
    }
    val clickable = Modifier
        .clip(RoundedCornerShape(24.dp))
        .then(
            if (onLongClick != null) Modifier.combinedClickable(onClickLabel = description, onClick = onClick, onLongClick = onLongClick)
            else Modifier.clickable(onClickLabel = description, onClick = onClick)
        )
        .padding(horizontal = 8.dp, vertical = 6.dp)
    if (vertical) {
        Column(clickable, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) { content() }
    } else {
        Row(clickable.heightIn(min = 40.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) { content() }
    }
}

@Composable
private fun QinblyLikeButton(liked: Boolean, count: Int, onClick: () -> Unit, tone: QinblyTone, vertical: Boolean = false) {
    val pop = remember { androidx.compose.animation.core.Animatable(1f) }
    var firstLikeFrame by remember { mutableStateOf(true) }
    LaunchedEffect(liked) {
        if (firstLikeFrame) { firstLikeFrame = false }
        else if (liked && !QiblyTheme.reduceMotion) {
            pop.animateTo(1.4f, androidx.compose.animation.core.tween(110))
            pop.animateTo(1f, androidx.compose.animation.core.spring(dampingRatio = 0.45f, stiffness = 500f))
        }
    }
    QinblyActionButton(if (liked) "Descurtir" else "Curtir", onClick, tone, vertical, qinblyCount(count)) {
        Icon(
            if (liked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            contentDescription = if (liked) "Descurtir" else "Curtir",
            tint = if (liked) QinblyBrand.Pink else qinblyIconTint(tone),
            modifier = Modifier.size(24.dp).graphicsLayer { scaleX = pop.value; scaleY = pop.value }
        )
    }
}

@Composable
private fun QinblyCommentButton(count: Int, onClick: () -> Unit, tone: QinblyTone, vertical: Boolean = false) {
    QinblyActionButton("Comentários", onClick, tone, vertical, qinblyCount(count)) {
        Icon(Icons.Default.ChatBubbleOutline, contentDescription = "Comentários", tint = qinblyIconTint(tone), modifier = Modifier.size(24.dp))
    }
}

@Composable
private fun QinblyRepostButton(reposted: Boolean, count: Int, onClick: () -> Unit, tone: QinblyTone, vertical: Boolean = false) {
    QinblyActionButton(if (reposted) "Remover repost" else "Repostar", onClick, tone, vertical, qinblyCount(count)) {
        RepostVerifiedIcon(reposted, tint = qinblyIconTint(tone))
    }
}

@Composable
private fun QinblySaveButton(saved: Boolean, onClick: () -> Unit, tone: QinblyTone, vertical: Boolean = false) {
    QinblyActionButton(if (saved) "Remover dos salvos" else "Salvar publicação", onClick, tone, vertical, null) {
        Icon(
            if (saved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
            contentDescription = if (saved) "Remover dos salvos" else "Salvar publicação",
            tint = if (saved) Color(0xFFFFD54F) else qinblyIconTint(tone),
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun QinblyShareButton(onClick: () -> Unit, tone: QinblyTone, vertical: Boolean = false) {
    QinblyActionButton("Enviar publicação", onClick, tone, vertical, null) {
        RoundedPaperPlaneIcon(tint = qinblyIconTint(tone))
    }
}

/** Os cinco botões, sempre na mesma ordem: curtir, comentar, repostar, salvar, enviar. */
@Composable
private fun QinblyPostActionButtons(
    post: QinblyApi.PostSummary,
    prefs: android.content.SharedPreferences,
    tone: QinblyTone,
    vertical: Boolean,
    dividers: Boolean,
    onNotice: (String) -> Unit,
    onComment: () -> Unit,
    onShare: () -> Unit
) {
    val dividerColor = if (tone == QinblyTone.Overlay) Color.White.copy(alpha = 0.25f) else QiblyTheme.border
    val divider: @Composable () -> Unit = {
        if (dividers) Box(Modifier.width(1.dp).height(18.dp).background(dividerColor))
    }
    QinblyLikeButton(QinblyEngagement.isLiked(post), QinblyEngagement.likeCount(post), { QinblyEngagement.toggleLike(prefs, post, onNotice) }, tone, vertical)
    divider()
    QinblyCommentButton(post.comments, onComment, tone, vertical)
    divider()
    QinblyRepostButton(QinblyEngagement.isReposted(post), QinblyEngagement.repostCount(post), { QinblyEngagement.toggleReposted(prefs, post, onNotice) }, tone, vertical)
    divider()
    QinblySaveButton(QinblyEngagement.isSaved(post), { QinblyEngagement.toggleSaved(prefs, post, onNotice) }, tone, vertical)
    divider()
    QinblyShareButton(onShare, tone, vertical)
}

/** Barra de ações padrão de toda publicação (horizontal nos cartões, vertical sobre o vídeo em tela cheia). */
@Composable
private fun QinblyPostActionBar(
    post: QinblyApi.PostSummary,
    prefs: android.content.SharedPreferences,
    tone: QinblyTone,
    onNotice: (String) -> Unit,
    onComment: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier,
    vertical: Boolean = false
) {
    if (vertical) {
        Column(modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
            QinblyPostActionButtons(post, prefs, tone, true, false, onNotice, onComment, onShare)
        }
    } else {
        val background = if (tone == QinblyTone.Feed) RemoteAppearance.feedActionBackgroundColor else Color.Transparent
        Row(
            modifier.fillMaxWidth().background(background).heightIn(min = 52.dp).padding(horizontal = 8.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Curtir, comentar, republicar e enviar juntos à esquerda (espaço médio); só o salvar na ponta direita.
            QinblyLikeButton(QinblyEngagement.isLiked(post), QinblyEngagement.likeCount(post), { QinblyEngagement.toggleLike(prefs, post, onNotice) }, tone, false)
            QinblyCommentButton(post.comments, onComment, tone, false)
            QinblyRepostButton(QinblyEngagement.isReposted(post), QinblyEngagement.repostCount(post), { QinblyEngagement.toggleReposted(prefs, post, onNotice) }, tone, false)
            QinblyShareButton(onShare, tone, false)
            Spacer(Modifier.weight(1f))
            QinblySaveButton(QinblyEngagement.isSaved(post), { QinblyEngagement.toggleSaved(prefs, post, onNotice) }, tone, false)
        }
    }
}

/** Seguir no cabeçalho da publicação: ao seguir mostra "Seguindo" por um instante e some (como nas outras redes);
 *  quem já é seguido nem mostra o botão. Se deixar de seguir em outro lugar, o "Seguir" volta. */
@Composable
private fun QinblyCardFollow(post: QinblyApi.PostSummary, prefs: android.content.SharedPreferences, onNotice: (String) -> Unit) {
    val currentUsername = prefs.getString("username", "").orEmpty()
    if (post.username.isBlank() || post.username.equals(currentUsername, ignoreCase = true)) return
    val following = QinblyEngagement.isFollowing(prefs, post.username, post.isFollowing)
    var hidden by remember(post.id) { mutableStateOf(following) }
    LaunchedEffect(following) {
        if (!following) hidden = false
        else if (!hidden) { kotlinx.coroutines.delay(1500); hidden = true }
    }
    if (!hidden) {
        QinblyFollowButton(
            following = following,
            busy = QinblyEngagement.isFollowBusy(post.username),
            onClick = { QinblyEngagement.toggleFollow(prefs, post.username, post.authorId, post.displayName, onNotice) }
        )
        Spacer(Modifier.width(3.dp))
    }
}

/** Botão de seguir único: "Seguir" preenchido (chamada para ação), "Seguindo" só com contorno. */
@Composable
private fun QinblyFollowButton(
    following: Boolean,
    busy: Boolean,
    onClick: () -> Unit,
    tone: QinblyTone = QinblyTone.Feed,
    large: Boolean = false,
    modifier: Modifier = Modifier,
    idleLabel: String = "Seguir"
) {
    val accent = QinblyBrand.Accent
    val outline = if (tone == QinblyTone.Overlay) Color.White else accent
    val sizing = if (large) modifier.height(46.dp) else modifier.height(32.dp).widthIn(min = 84.dp)
    val shape = RoundedCornerShape(if (large) 24.dp else 18.dp)
    val label: @Composable () -> Unit = {
        if (large) {
            Icon(if (following) Icons.Default.CheckCircle else Icons.Default.PersonAddAlt1, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
        }
        Text(if (busy) "Aguarde..." else if (following) "Seguindo" else idleLabel, fontSize = if (large) 15.sp else 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
    }
    if (following) {
        OutlinedButton(
            onClick = onClick, enabled = !busy, modifier = sizing, shape = shape,
            contentPadding = PaddingValues(horizontal = 14.dp),
            border = BorderStroke(1.dp, outline),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = outline)
        ) { label() }
    } else {
        Button(
            onClick = onClick, enabled = !busy, modifier = sizing, shape = shape,
            contentPadding = PaddingValues(horizontal = 14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = Color.White)
        ) { label() }
    }
}

// =====================================================================================
// VISUALIZADOR ÚNICO DE PUBLICAÇÕES (padrão da tela de Início).
// Usado no Início, no Perfil e no Perfil público: mesmo player, mesma barra azul, mesmos botões laterais,
// mesmo menu "mais", mesma legenda/seguir, mesmo comportamento para fotos e vídeos.
// =====================================================================================
@Composable
private fun QinblyViewerImages(images: List<String>, post: QinblyApi.PostSummary, prefs: android.content.SharedPreferences, onNotice: (String) -> Unit) {
    val pager = androidx.compose.foundation.pager.rememberPagerState(pageCount = { images.size.coerceAtLeast(1) })
    var likeBurst by remember(post.id) { mutableIntStateOf(0) }
    Box(Modifier.fillMaxSize()) {
        androidx.compose.foundation.pager.HorizontalPager(state = pager, modifier = Modifier.fillMaxSize()) { index ->
            Box(
                Modifier.fillMaxSize().pointerInput(post.id) {
                    detectTapGestures(onDoubleTap = {
                        if (!QinblyEngagement.isLiked(post)) QinblyEngagement.toggleLike(prefs, post, onNotice)
                        likeBurst++
                    })
                },
                contentAlignment = Alignment.Center
            ) {
                QinblyLoadingSpinner(Modifier.fillMaxSize())
                AsyncImage(
                    model = images.getOrNull(index).orEmpty(),
                    contentDescription = "Imagem da publicação",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Fit
                )
            }
        }
        QinblyHeartBurst(likeBurst, Modifier.align(Alignment.Center))
        if (images.size > 1) {
            Text(
                "${pager.currentPage + 1}/${images.size}",
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.TopCenter).statusBarsPadding().padding(top = 16.dp)
            )
        }
    }
}

/** Se a pessoa abriu um vídeo, o visualizador mostra só vídeos ao deslizar; abrindo uma foto, mantém a lista como está. */
private fun qinblyViewerList(list: List<QinblyApi.PostSummary>, startId: String?): List<QinblyApi.PostSummary> =
    if (list.firstOrNull { it.id == startId }?.mediaType == "video") list.filter { it.mediaType == "video" } else list

@Composable
private fun QinblyPostViewer(
    posts: List<QinblyApi.PostSummary>,
    startPostId: String,
    prefs: android.content.SharedPreferences,
    currentUsername: String,
    onNotice: (String) -> Unit,
    onDismiss: () -> Unit,
    onComment: (QinblyApi.PostSummary) -> Unit,
    onShare: (QinblyApi.PostSummary) -> Unit,
    onOpenProfile: (String) -> Unit,
    onOpenAudio: ((QinblyApi.PostSummary) -> Unit)? = null
) {
    if (posts.isEmpty()) return
    val context = androidx.compose.ui.platform.LocalContext.current
    var reportPost by remember { mutableStateOf<QinblyApi.PostSummary?>(null) }
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        val pagerState = androidx.compose.foundation.pager.rememberPagerState(
            initialPage = posts.indexOfFirst { it.id == startPostId }.coerceAtLeast(0),
            pageCount = { posts.size }
        )
        Box(Modifier.fillMaxSize().background(Color.Black)) {
            val dialogWindow = (androidx.compose.ui.platform.LocalView.current.parent as? androidx.compose.ui.window.DialogWindowProvider)?.window
            SideEffect {
                dialogWindow?.let { window ->
                    window.navigationBarColor = android.graphics.Color.BLACK
                    window.statusBarColor = android.graphics.Color.BLACK
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) window.isNavigationBarContrastEnforced = false
                }
            }
            androidx.compose.foundation.pager.VerticalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                beyondViewportPageCount = 0
            ) { page ->
                val post = posts[page]
                val isVideo = post.mediaType.equals("video", ignoreCase = true)
                val images = remember(post.id) { post.mediaUrls.ifEmpty { listOf(post.mediaUrl) }.filter { it.isNotBlank() } }
                var menuOpen by remember(post.id) { mutableStateOf(false) }
                Box(Modifier.fillMaxSize().background(Color.Black)) {
                    if (isVideo) {
                        if (pagerState.currentPage == page) {
                            QinblyFullscreenVideo(post.mediaUrl.ifBlank { images.firstOrNull().orEmpty() }, Modifier.fillMaxSize(), onDoubleTap = { if (!QinblyEngagement.isLiked(post)) QinblyEngagement.toggleLike(prefs, post, onNotice) })
                        }
                    } else {
                        QinblyViewerImages(images, post, prefs, onNotice)
                        // Foto: visualização limpa, só a imagem. Salvar/compartilhar/denunciar ficam num menu discreto no canto.
                        Box(Modifier.align(Alignment.TopEnd).padding(12.dp)) {
                            IconButton(onClick = { menuOpen = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "Mais opções", tint = Color.White)
                            }
                            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                                DropdownMenuItem(text = { Text("Salvar no celular") }, onClick = {
                                    menuOpen = false
                                    saveProfilePhotoToDevice(context, post.mediaUrl.ifBlank { images.firstOrNull().orEmpty() }, onNotice)
                                })
                                DropdownMenuItem(text = { Text("Compartilhar") }, onClick = { menuOpen = false; onShare(post) })
                                if (!post.username.equals(currentUsername.removePrefix("@"), ignoreCase = true)) {
                                    DropdownMenuItem(text = { Text("Denunciar publicação") }, onClick = { menuOpen = false; reportPost = post })
                                }
                            }
                        }
                    }
                    if (isVideo) Column(
                        Modifier.align(Alignment.BottomEnd).padding(end = 22.dp, bottom = 180.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        if (isVideo && onOpenAudio != null) {
                            // Disco musical independente, sem painel preto atrás dos controles.
                            Box(
                                modifier = Modifier.padding(bottom = 19.dp).size(45.dp)
                                    .border(2.dp, Color(0xFF43C6FF), CircleShape)
                                    .clickable { onOpenAudio(post) },
                                contentAlignment = Alignment.Center
                            ) {
                                if (post.audioId.isBlank() && post.avatarUrl.isNotBlank()) {
                                    AsyncImage(
                                        model = post.avatarUrl,
                                        contentDescription = "Som original de @${post.username}. Ver publicações com este áudio",
                                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                } else {
                                    Icon(Icons.Default.MusicNote, contentDescription = "Ver publicações com este áudio", tint = Color.White, modifier = Modifier.size(23.dp))
                                }
                            }
                        }
                        QinblyPostActionBar(
                            post = post,
                            prefs = prefs,
                            tone = QinblyTone.Overlay,
                            onNotice = onNotice,
                            onComment = { onComment(post) },
                            onShare = { onShare(post) },
                            vertical = true
                        )
                        Spacer(Modifier.height(4.dp))
                        Box {
                            IconButton(onClick = { menuOpen = true }) {
                                Icon(Icons.Default.MoreHoriz, contentDescription = "Mais opções", tint = Color.White, modifier = Modifier.size(28.dp))
                            }
                            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                                DropdownMenuItem(text = { Text("Salvar no celular") }, onClick = {
                                    menuOpen = false
                                    saveProfilePhotoToDevice(context, post.mediaUrl.ifBlank { images.firstOrNull().orEmpty() }, onNotice)
                                })
                                DropdownMenuItem(text = { Text("Compartilhar") }, onClick = {
                                    menuOpen = false
                                    onShare(post)
                                })
                                if (!post.username.equals(currentUsername.removePrefix("@"), ignoreCase = true)) {
                                    DropdownMenuItem(text = { Text("Denunciar publicação") }, onClick = {
                                        menuOpen = false
                                        reportPost = post
                                    })
                                }
                            }
                        }
                    }
                    if (isVideo) Column(
                        Modifier.align(Alignment.BottomStart).fillMaxWidth(0.88f).padding(start = 18.dp, bottom = 180.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            QiblyAccountAvatar(post.username, post.avatarUrl, 52.dp) { onOpenProfile(post.username) }
                            Text("@" + post.username, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1, fontSize = 17.sp)
                            if (post.username.isNotBlank() && !post.username.equals(currentUsername.removePrefix("@"), ignoreCase = true)) {
                                QinblyFollowButton(
                                    following = QinblyEngagement.isFollowing(prefs, post.username, post.isFollowing),
                                    busy = QinblyEngagement.isFollowBusy(post.username),
                                    onClick = { QinblyEngagement.toggleFollow(prefs, post.username, post.authorId, post.displayName, onNotice) },
                                    tone = QinblyTone.Overlay
                                )
                            }
                        }
                        if (post.caption.isNotBlank()) QinblyViewerCaption(post.caption)
                    }
                }
            }
            IconButton(onClick = onDismiss, modifier = Modifier.align(Alignment.TopStart).padding(12.dp)) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = Color.White)
            }
        }
    }
    reportPost?.let { target ->
        ReportReasonDialog(title = "Denunciar: qual o motivo?", onDismiss = { reportPost = null }) { reason ->
            reportPost = null
            val token = prefs.getString("token", null)
            if (token.isNullOrBlank()) onNotice("Entre na sua conta para denunciar.")
            else Thread {
                try {
                    QinblyApi.reportContent(token, "post", target.id, reason)
                    android.os.Handler(android.os.Looper.getMainLooper()).post { onNotice("Denúncia enviada. Obrigado por ajudar a manter a comunidade segura.") }
                } catch (e: Exception) {
                    android.os.Handler(android.os.Looper.getMainLooper()).post { onNotice(e.message ?: "Não foi possível enviar a denúncia agora.") }
                }
            }.start()
        }
    }
}

/**
 * Tela de comentários única (a do feed): mesmo painel, campo de digitação, emojis, teclado e atualização em todo lugar.
 * Carrega, envia e exclui sozinha; avisa quem a chamou da nova contagem para o número do botão acompanhar.
 */
@Composable
private fun QinblyCommentsDialog(
    post: QinblyApi.PostSummary,
    prefs: android.content.SharedPreferences,
    currentUsername: String,
    onNotice: (String) -> Unit,
    onDismiss: () -> Unit,
    onCountChanged: (String, Int) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val me = currentUsername.removePrefix("@")
    var comments by remember(post.id) { mutableStateOf<List<QinblyApi.CommentSummary>>(emptyList()) }
    var commentsLoading by remember(post.id) { mutableStateOf(true) }
    var commentDraft by remember(post.id) { mutableStateOf("") }
    var replyTo by remember(post.id) { mutableStateOf<QinblyApi.CommentSummary?>(null) }
    val expandedThreads = remember(post.id) { mutableStateListOf<String>() }
    var menuFor by remember(post.id) { mutableStateOf<String?>(null) }
    var editingComment by remember(post.id) { mutableStateOf<QinblyApi.CommentSummary?>(null) }
    var reportingComment by remember(post.id) { mutableStateOf<QinblyApi.CommentSummary?>(null) }
    var editDraft by remember(post.id) { mutableStateOf("") }
    val commentLikeState = remember(post.id) { mutableStateMapOf<String, Boolean>() }
    LaunchedEffect(post.id) {
        commentsLoading = true
        Thread {
            try {
                val loaded = QinblyApi.getComments(post.id)
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    comments = loaded
                    commentsLoading = false
                    onCountChanged(post.id, loaded.size)
                }
            } catch (e: Exception) {
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    commentsLoading = false
                    onNotice(e.message ?: "Não foi possível carregar comentários.")
                }
            }
        }.start()
    }
    val commentEmojiContext = androidx.compose.ui.platform.LocalContext.current
    val commentEmojiPrefs = remember(commentEmojiContext) {
        commentEmojiContext.getSharedPreferences("qinbly_comment_emoji_history", android.content.Context.MODE_PRIVATE)
    }
    var recentCommentEmojis by remember {
        mutableStateOf(commentEmojiPrefs.getString("recent", "")!!.split("|").filter { it.isNotBlank() })
    }
    var showCommentEmojis by remember { mutableStateOf(false) }
    var showCommentStickerCreator by remember { mutableStateOf(false) }
    var commentStickerRefresh by remember { mutableStateOf(0) }
    val commentKb = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current
    val commentFocus = androidx.compose.ui.platform.LocalFocusManager.current
    var selectedCommentEmojiCategory by remember { mutableStateOf("Emoções") }
    BackHandler(enabled = showCommentEmojis) {
        showCommentEmojis = false
    }
    val commentDensity = androidx.compose.ui.platform.LocalDensity.current
    val keyboardInset = with(commentDensity) { WindowInsets.ime.getBottom(this).toDp() }
    // Android can report a small IME inset even with the keyboard dismissed.
    // Use a real keyboard-height threshold so the sheet doesn't stay collapsed.
    val keyboardVisible = keyboardInset > 120.dp
    val activePost = post
    run {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = {
                if (showCommentEmojis) showCommentEmojis = false else onDismiss()
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
                    .clickable { if (showCommentEmojis) showCommentEmojis = false else onDismiss() },
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
                        // Fio: cada comentário seguido das suas respostas (um nível).
                        // Respostas ficam escondidas até a pessoa tocar em "Ver respostas".
                        val threaded = comments.filter { it.parentId.isBlank() }.flatMap { top ->
                            listOf(top) + if (top.id in expandedThreads) comments.filter { it.parentId == top.id } else emptyList()
                        } + comments.filter { it.parentId.isNotBlank() && comments.none { c -> c.id == it.parentId } }
                        items(threaded, key = { it.id }) { comment ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(start = if (comment.parentId.isNotBlank()) 44.dp else 0.dp, top = 14.dp, bottom = 14.dp),
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
                                Column(modifier = Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).combinedClickable(onClick = {}, onLongClick = { menuFor = comment.id }).padding(start = 14.dp, end = 8.dp)) {
                                    val isMine = comment.username.equals(me, ignoreCase = true)
                                    val canDelete = isMine || activePost.username.equals(me, ignoreCase = true)
                                    DropdownMenu(expanded = menuFor == comment.id, onDismissRequest = { menuFor = null }) {
                                        DropdownMenuItem(text = { Text("Copiar") }, onClick = {
                                            menuFor = null
                                            val cm = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                            cm.setPrimaryClip(android.content.ClipData.newPlainText("Comentário", comment.body))
                                            onNotice("Comentário copiado.")
                                        })
                                        if (isMine) DropdownMenuItem(text = { Text("Editar comentário") }, onClick = { menuFor = null; editDraft = comment.body; editingComment = comment })
                                        if (!isMine) DropdownMenuItem(text = { Text("Ver perfil") }, onClick = { menuFor = null; QinblyTagNav.profile = comment.username })
                                        if (canDelete) DropdownMenuItem(text = { Text("Excluir comentário", color = Color(0xFFD33B3B)) }, onClick = {
                                            menuFor = null
                                            val token = prefs.getString("token", null)
                                            if (token.isNullOrBlank()) onNotice("Entre na sua conta para excluir comentários.")
                                            else Thread {
                                                try {
                                                    QinblyApi.deleteComment(token, comment.id)
                                                    val reloaded = QinblyApi.getComments(activePost.id)
                                                    android.os.Handler(android.os.Looper.getMainLooper()).post { comments = reloaded; onCountChanged(activePost.id, reloaded.size) }
                                                } catch (e: Exception) {
                                                    android.os.Handler(android.os.Looper.getMainLooper()).post { onNotice(e.message ?: "Não foi possível excluir o comentário.") }
                                                }
                                            }.start()
                                        })
                                        if (!isMine) DropdownMenuItem(text = { Text("Denunciar comentário", color = Color(0xFFD33B3B)) }, onClick = { menuFor = null; reportingComment = comment })
                                    }
                                    Text(
                                        comment.displayName.ifBlank { comment.username }.ifBlank { "Usuário" },
                                        color = RemoteAppearance.commentTextColor,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 16.sp
                                    )
                                    Text(if (comment.createdAt.isBlank()) "agora" else postRelativeTime(comment.createdAt, System.currentTimeMillis()), color = RemoteAppearance.commentSecondaryTextColor, fontSize = 14.sp, modifier = Modifier.padding(top = 2.dp))
                                    val commentSticker = QinblyStickers.idOf(comment.body)
                                    if (comment.stickerUrl.isNotBlank()) Box(Modifier.padding(top = 8.dp).combinedClickable(onClick = {}, onLongClick = { menuFor = comment.id })) { AsyncImage(model = comment.stickerUrl, contentDescription = "Figurinha", modifier = Modifier.size(96.dp)) }
                                    else if (commentSticker != null) Box(Modifier.padding(top = 8.dp).combinedClickable(onClick = {}, onLongClick = { menuFor = comment.id })) { QinblyStickerView(commentSticker, 96.dp) }
                                    else QinblyRichText(comment.body, RemoteAppearance.commentTextColor, Modifier.padding(top = 8.dp), fontSize = 16.sp, onLongClick = { menuFor = comment.id })
                                    Row(Modifier.padding(top = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                                        Text(
                                            "Responder",
                                            color = RemoteAppearance.commentTextColor,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.clickable {
                                                replyTo = comment
                                                // Respondendo ao comentário principal: sem marcação. Respondendo a uma resposta: marca quem está sendo respondido.
                                                commentDraft = if (comment.parentId.isBlank()) "" else "@${comment.username} "
                                                val topId = comment.parentId.ifBlank { comment.id }
                                                if (topId !in expandedThreads) expandedThreads.add(topId)
                                            }
                                        )
                                        if (comment.parentId.isBlank()) {
                                            val replyCount = comments.count { it.parentId == comment.id }
                                            if (replyCount > 0) {
                                                val open = comment.id in expandedThreads
                                                Text(
                                                    (if (open) "Ocultar respostas" else if (replyCount == 1) "Ver 1 resposta" else "Ver $replyCount respostas") + if (open) " ▴" else " ▾",
                                                    color = QinblyBrand.Accent,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    modifier = Modifier.clickable { if (open) expandedThreads.remove(comment.id) else expandedThreads.add(comment.id) }
                                                )
                                            }
                                        }
                                    }
                                }
                                val cLiked = commentLikeState[comment.id] ?: comment.likedByMe
                                val cCount = (comment.likes + (if (cLiked == comment.likedByMe) 0 else if (cLiked) 1 else -1)).coerceAtLeast(0)
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.clip(RoundedCornerShape(12.dp)).clickable {
                                        val token = prefs.getString("token", null)
                                        if (token.isNullOrBlank()) { onNotice("Entre na sua conta para curtir comentários."); return@clickable }
                                        val before = cLiked
                                        commentLikeState[comment.id] = !before
                                        Thread {
                                            try {
                                                val now = QinblyApi.toggleCommentLike(token, comment.id)
                                                android.os.Handler(android.os.Looper.getMainLooper()).post { commentLikeState[comment.id] = now }
                                            } catch (e: Exception) {
                                                android.os.Handler(android.os.Looper.getMainLooper()).post { commentLikeState[comment.id] = before; onNotice(e.message ?: "Não foi possível curtir o comentário.") }
                                            }
                                        }.start()
                                    }.padding(horizontal = 6.dp, vertical = 4.dp)
                                ) {
                                    Icon(if (cLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder, contentDescription = "Curtir comentário", tint = if (cLiked) Color(0xFFFF3045) else RemoteAppearance.commentTextColor, modifier = Modifier.size(22.dp))
                                    if (cCount > 0) Text(cCount.toString(), color = RemoteAppearance.commentSecondaryTextColor, fontSize = 12.sp)
                                }
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
                        editingComment?.let { target ->
                            AlertDialog(
        shape = RoundedCornerShape(28.dp),
        containerColor = QiblyTheme.card,
                                onDismissRequest = { editingComment = null },
                                title = { Text("Editar comentário") },
                                text = { OutlinedTextField(value = editDraft, onValueChange = { editDraft = it.take(2000) }, minLines = 2, maxLines = 6, visualTransformation = QinblyTagTransformation, modifier = Modifier.fillMaxWidth()) },
                                confirmButton = {
                                    TextButton(enabled = editDraft.isNotBlank() && editDraft.trim() != target.body.trim(), onClick = {
                                        val token = prefs.getString("token", null)
                                        val newBody = editDraft.trim()
                                        editingComment = null
                                        if (token.isNullOrBlank()) { onNotice("Entre na sua conta para editar."); return@TextButton }
                                        Thread {
                                            try {
                                                QinblyApi.editComment(token, target.id, newBody)
                                                val reloaded = QinblyApi.getComments(activePost.id)
                                                android.os.Handler(android.os.Looper.getMainLooper()).post { comments = reloaded; onNotice("Comentário atualizado.") }
                                            } catch (e: Exception) {
                                                android.os.Handler(android.os.Looper.getMainLooper()).post { onNotice(e.message ?: "Não foi possível editar o comentário.") }
                                            }
                                        }.start()
                                    }) { Text("Salvar") }
                                },
                                dismissButton = { TextButton(onClick = { editingComment = null }) { Text("Cancelar") } }
                            )
                        }
                        reportingComment?.let { target ->
                            ReportReasonDialog(title = "Denunciar comentário: qual o motivo?", onDismiss = { reportingComment = null }) { reason ->
                                reportingComment = null
                                val token = prefs.getString("token", null)
                                if (token.isNullOrBlank()) onNotice("Entre na sua conta para denunciar.")
                                else Thread {
                                    try {
                                        QinblyApi.reportContent(token, "comment", target.id, reason)
                                        android.os.Handler(android.os.Looper.getMainLooper()).post { onNotice("Denúncia enviada. Obrigado por ajudar a manter a comunidade segura.") }
                                    } catch (e: Exception) {
                                        android.os.Handler(android.os.Looper.getMainLooper()).post { onNotice(e.message ?: "Não foi possível enviar a denúncia.") }
                                    }
                                }.start()
                            }
                        }
                        replyTo?.let { target ->
                            Row(
                                Modifier.fillMaxWidth().background(RemoteAppearance.commentInputBackgroundColor).padding(horizontal = 18.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Respondendo a @${target.username}", color = RemoteAppearance.commentSecondaryTextColor, fontSize = 13.sp, modifier = Modifier.weight(1f))
                                Text("✕", color = RemoteAppearance.commentSecondaryTextColor, modifier = Modifier.clickable { replyTo = null; commentDraft = "" }.padding(6.dp))
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
                        Box(Modifier.size(48.dp).clip(CircleShape).clickable { showCommentEmojis = !showCommentEmojis; if (showCommentEmojis) { commentKb?.hide(); commentFocus.clearFocus() } else commentKb?.show() }, contentAlignment = Alignment.Center) {
                            Icon(QinblyCreateIcons.sticker, contentDescription = "Emojis e figurinhas", tint = if (showCommentEmojis) QinblyBrand.Accent else RemoteAppearance.commentSecondaryTextColor, modifier = Modifier.size(28.dp))
                        }
                        OutlinedTextField(
                            value = commentDraft,
                            onValueChange = { commentDraft = it.take(1200) },
                            visualTransformation = QinblyTagTransformation,
                            placeholder = { Text("Adicione um comentário...", color = RemoteAppearance.commentSecondaryTextColor) },
                            modifier = Modifier.weight(1f).padding(start = 10.dp).onFocusChanged { if (it.isFocused) showCommentEmojis = false },
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
                                            QinblyApi.addComment(token ?: "", activePost.id, draftToSend, replyTo?.id)
                                            val loadedComments = QinblyApi.getComments(activePost.id)
                                            
                                            android.os.Handler(android.os.Looper.getMainLooper()).post {
                                                comments = loadedComments
                                                onCountChanged(activePost.id, loadedComments.size)
                                                commentDraft = ""
                                                replyTo = null
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
                    if (showCommentEmojis) {
                        QinblyStickerGrid(QinblyBrand.Accent, RemoteAppearance.commentInputBackgroundColor, RemoteAppearance.commentTextColor, commentStickerRefresh,
                            onCreate = { showCommentStickerCreator = true },
                            onPick = { sticker ->
                                val token = prefs.getString("token", null)
                                if (token.isNullOrBlank()) onNotice("Entre na sua conta para comentar.")
                                else if (!commentsLoading) {
                                    commentsLoading = true
                                    Thread {
                                        try {
                                            QinblyApi.addComment(token, activePost.id, "qinbly:sticker:" + sticker.path, replyTo?.id)
                                            val loadedComments = QinblyApi.getComments(activePost.id)
                                            android.os.Handler(android.os.Looper.getMainLooper()).post {
                                                comments = loadedComments
                                                onCountChanged(activePost.id, loadedComments.size)
                                                replyTo = null
                                                showCommentEmojis = false
                                                commentsLoading = false
                                            }
                                        } catch (e: Exception) {
                                            android.os.Handler(android.os.Looper.getMainLooper()).post { commentsLoading = false; onNotice(e.message ?: "Não foi possível enviar a figurinha.") }
                                        }
                                    }.start()
                                }
                            }, onNotice = onNotice,
                            modifier = Modifier.fillMaxWidth().height(300.dp).background(RemoteAppearance.commentInputBackgroundColor).padding(10.dp))
                    }
                    if (showCommentStickerCreator) QinblyStickerCreateDialog(QinblyBrand.Accent, onNotice, onDismiss = { showCommentStickerCreator = false }, onCreated = { commentStickerRefresh++ })
                    }
            }
        }
    }
}
}

/**
 * Card de publicação único (o do feed): cabeçalho com seguir e menu, mídia, legenda, enquete e barra de ações.
 * Usado no Início e nas publicações dos Perfis, então tudo aparece e funciona igual em qualquer aba.
 */
@Composable
private fun QinblyPostCard(
    post: QinblyApi.PostSummary,
    prefs: android.content.SharedPreferences,
    nameFontStyle: String,
    onNotice: (String) -> Unit,
    onOpenProfile: (String) -> Unit,
    onOpenViewer: (QinblyApi.PostSummary) -> Unit,
    onComment: (QinblyApi.PostSummary) -> Unit,
    onShare: (QinblyApi.PostSummary) -> Unit,
    onPostsTransform: ((List<QinblyApi.PostSummary>) -> List<QinblyApi.PostSummary>) -> Unit,
    onRefresh: () -> Unit,
    badge: String? = null
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var editingCaption by remember(post.id) { mutableStateOf(false) }
    var reportingPost by remember(post.id) { mutableStateOf(false) }
    var blockingAuthor by remember(post.id) { mutableStateOf(false) }
    if (editingCaption) {
        QinblyEditCaptionDialog(post, prefs, onNotice, onDismiss = { editingCaption = false }, onDeleted = { onPostsTransform { list -> list.filterNot { it.id == post.id } } }) { updated ->
            onPostsTransform { list -> list.map { if (it.id == post.id) updated.copy(isFollowing = it.isFollowing, likedByMe = it.likedByMe, savedByMe = it.savedByMe, repostedByMe = it.repostedByMe) else it } }
        }
    }
    if (reportingPost) {
        ReportReasonDialog(title = "Denunciar publicação: qual o motivo?", onDismiss = { reportingPost = false }) { reason ->
            reportingPost = false
            val token = prefs.getString("token", null)
            if (token.isNullOrBlank()) onNotice("Entre na sua conta para denunciar.")
            else Thread {
                try {
                    QinblyApi.reportContent(token, "post", post.id, reason)
                    android.os.Handler(android.os.Looper.getMainLooper()).post { onNotice("Denúncia enviada. Obrigado por ajudar a manter a comunidade segura.") }
                } catch (e: Exception) {
                    android.os.Handler(android.os.Looper.getMainLooper()).post { onNotice(e.message ?: "Não foi possível enviar a denúncia agora.") }
                }
            }.start()
        }
    }
    if (blockingAuthor) {
        AlertDialog(
        shape = RoundedCornerShape(28.dp),
            onDismissRequest = { blockingAuthor = false },
            title = { Text("Bloquear @" + post.username + "?") },
            text = { Text("Vocês não verão mais as publicações um do outro nem poderão se seguir ou trocar mensagens. Você pode desbloquear em Configurações > Contas bloqueadas.") },
            confirmButton = {
                TextButton(onClick = {
                    blockingAuthor = false
                    val token = prefs.getString("token", null)
                    if (token.isNullOrBlank()) onNotice("Entre na sua conta para bloquear.")
                    else Thread {
                        try {
                            if (post.authorId.isNotBlank()) QinblyApi.toggleBlock(token, post.authorId) else QinblyApi.blockByUsername(token, post.username)
                            android.os.Handler(android.os.Looper.getMainLooper()).post {
                                onPostsTransform { list -> list.filterNot { it.username.equals(post.username, ignoreCase = true) } }
                                onNotice("Usuário bloqueado.")
                            }
                        } catch (e: Exception) {
                            android.os.Handler(android.os.Looper.getMainLooper()).post { onNotice(e.message ?: "Não foi possível bloquear agora.") }
                        }
                    }.start()
                }) { Text("Bloquear", color = Color.Red) }
            },
            dismissButton = { TextButton(onClick = { blockingAuthor = false }) { Text("Cancelar", color = Pink) } },
            containerColor = QiblyTheme.card, titleContentColor = QiblyTheme.text, textContentColor = QiblyTheme.text
        )
    }
    Surface(
        color = Color.Transparent,
        shape = androidx.compose.ui.graphics.RectangleShape,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(0.dp)) {

                        if (post.mediaUrl.isBlank() || (post.mediaType != "image" && post.mediaType != "video")) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().background(Color.Transparent).padding(horizontal = 14.dp, vertical = 7.dp)) {
                    val avatarUrl = post.avatarUrl
                    QiblyAccountAvatar(post.username, avatarUrl, 52.dp) { if (post.username.isNotBlank()) onOpenProfile(post.username) }
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f).clickable { if (post.username.isNotBlank()) onOpenProfile(post.username) }) {
                        Row(verticalAlignment = Alignment.CenterVertically) { Text(post.displayName.ifBlank { post.username }.ifBlank { QinblyBrand.Name }, color = RemoteAppearance.feedTextColor, fontSize = 17.sp, fontWeight = if (nameFontStyle == "Elegante") FontWeight.Medium else FontWeight.Bold, fontFamily = nameFontFamily(nameFontStyle)); if (post.verified) { Spacer(Modifier.width(5.dp)); QinblyVerifiedBadge(16.dp) } }
                        LivePostTime(post.createdAt, RemoteAppearance.feedSecondaryTextColor)
                    }
                    QinblyCardFollow(post, prefs, onNotice)
                    QinblyPostMenu(post, prefs, onNotice, onPostsTransform, tint = null, onReport = { reportingPost = true }, onBlock = { blockingAuthor = true }, onEdit = { editingCaption = true })
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                post.displayName.ifBlank { post.username }.ifBlank { QinblyBrand.Name },
                                color = RemoteAppearance.feedTextColor, fontSize = 15.sp, fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable { if (post.username.isNotBlank()) onOpenProfile(post.username) }
                            )
                            if (post.verified) { Spacer(Modifier.width(5.dp)); QinblyVerifiedBadge(15.dp) }
                            }
                            LivePostTime(post.createdAt, RemoteAppearance.feedSecondaryTextColor)
                        }
                    QinblyCardFollow(post, prefs, onNotice)
                        QinblyPostMenu(post, prefs, onNotice, onPostsTransform, tint = RemoteAppearance.feedTextColor, onReport = { reportingPost = true }, onBlock = { blockingAuthor = true }, onEdit = { editingCaption = true })
                    }

                }
            }
            if (post.mediaUrl.isNotBlank() && (post.mediaType == "image" || post.mediaType == "video") && post.caption.isNotBlank()) {
                QinblyFeedCaption(post.caption, Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 2.dp, bottom = 8.dp))
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
                        else -> post.mediaUrls.ifEmpty { listOf(post.mediaUrl) }
                    }
                }
                var photoIndex by remember(post.id) { mutableIntStateOf(0) }
                val shownPhoto = demoGallery[photoIndex.coerceIn(0, demoGallery.lastIndex)]
                var imageAspectRatio by remember(shownPhoto) { mutableFloatStateOf(QinblyImageRatioCache.map[shownPhoto] ?: 0.8f) }
                var dragDistance by remember(post.id) { mutableFloatStateOf(0f) }
                var likeBurst by remember(post.id) { mutableIntStateOf(0) }
                Box(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)
                        // Altura máxima 4:5 (como o vídeo): fotos muito altas não empurram os botões para fora da tela.
                        .aspectRatio(imageAspectRatio.coerceIn(0.8f, 1.8f))
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black)
                ) {
                    if (badge != null) Surface(
                        modifier = Modifier.align(Alignment.TopStart).padding(10.dp),
                        color = QinblyBrand.AccentDeep.copy(alpha = 0.92f),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Text(badge.orEmpty(), color = Color.White,
                            fontSize = 12.sp, fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp))
                    }
                    if (android.os.Build.VERSION.SDK_INT >= 31) {
                        // Foto inteira (sem cortar) com a própria foto desfocada preenchendo as laterais.
                        AsyncImage(model = shownPhoto, contentDescription = null, modifier = Modifier.fillMaxSize().blur(32.dp).alpha(0.6f), contentScale = androidx.compose.ui.layout.ContentScale.Crop)
                    }
                    AsyncImage(
                        model = shownPhoto, contentDescription = "Foto ${photoIndex + 1} de ${demoGallery.size}",
                        onSuccess = { result ->
                            val drawable = result.result.drawable
                            if (drawable.intrinsicWidth > 0 && drawable.intrinsicHeight > 0) {
                                imageAspectRatio = drawable.intrinsicWidth.toFloat() / drawable.intrinsicHeight.toFloat()
                                QinblyImageRatioCache.map[shownPhoto] = imageAspectRatio
                            }
                        },
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
                            .pointerInput(post.id) {
                                detectTapGestures(
                                    onTap = { onOpenViewer(post) },
                                    onDoubleTap = {
                                        if (!QinblyEngagement.isLiked(post)) QinblyEngagement.toggleLike(prefs, post, onNotice)
                                        likeBurst++
                                    }
                                )
                            },
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                    QinblyHeartBurst(likeBurst, Modifier.align(Alignment.Center))

                                    
                                    
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
                    if (badge != null) Surface(
                        modifier = Modifier.align(Alignment.TopStart).padding(10.dp),
                        color = QinblyBrand.AccentDeep.copy(alpha = 0.92f),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Text(badge.orEmpty(), color = Color.White,
                            fontSize = 12.sp, fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp))
                    }
                    var videoBurst by remember(post.id) { mutableIntStateOf(0) }
                    Box(Modifier.fillMaxSize().pointerInput(post.id) {
                        detectTapGestures(
                            onTap = { onOpenViewer(post) },
                            onDoubleTap = {
                                if (!QinblyEngagement.isLiked(post)) QinblyEngagement.toggleLike(prefs, post, onNotice)
                                videoBurst++
                            }
                        )
                    }) {
                        QiblyPublicationVideo(url = post.mediaUrl, modifier = Modifier.fillMaxSize())
                        QinblyHeartBurst(videoBurst, Modifier.align(Alignment.Center))
                    }
                                    

                                    
                }
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
                                .background(if (selected) QiblyTheme.soft else QiblyTheme.card, pollShape)
                                .border(
                                    width = if (selected) 1.5.dp else 1.dp,
                                    color = if (selected) QinblyBrand.Accent else QiblyTheme.border,
                                    shape = pollShape
                                )
                                .clickable(enabled = !hasVoted) {
                                    val token = prefs.getString("token", null)
                                    if (token.isNullOrBlank()) onNotice("Entre na sua conta para votar.")
                                    else Thread {
                                        try {
                                            QinblyApi.votePoll(token, post.id, optionIndex)
                                            android.os.Handler(android.os.Looper.getMainLooper()).post { onRefresh() }
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
                                    .background(if (selected) QiblyTheme.soft else QiblyTheme.soft, CircleShape)
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
                                    .background(QiblyTheme.border, RoundedCornerShape(12.dp))
                            ) {
                                if (hasVoted && percent > 0) {
                                    Box(
                                        Modifier.fillMaxWidth(percent / 100f).fillMaxHeight()
                                            .background(QinblyBrand.Accent, RoundedCornerShape(12.dp))
                                    )
                                }
                            }
                            if (hasVoted) {
                                Text("$percent%", color = Color(0xFF14233B), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                if (selected) {
                                    Box(
                                        Modifier.size(28.dp).background(QinblyBrand.Accent, CircleShape),
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
                QinblyRichText(post.caption, RemoteAppearance.feedTextColor, fontSize = 15.sp,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 7.dp),
                    maxLines = 4)
            }
            if (post.taggedUsers.isNotEmpty()) {
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 14.dp, vertical = 2.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Com", color = RemoteAppearance.feedTextColor.copy(alpha = 0.7f), fontSize = 13.sp)
                    post.taggedUsers.forEach { tagged ->
                        Text("@$tagged", color = QinblyBrand.Accent, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable { onOpenProfile(tagged) })
                    }
                }
            }
            if (post.mediaType != "poll") { // Enquetes exibem apenas votação e total de votos
                QinblyPostActionBar(
                    post = post,
                    prefs = prefs,
                    tone = QinblyTone.Feed,
                    onNotice = onNotice,
                    onComment = { onComment(post) },
                    onShare = { onShare(post) }
                )
            }
            // Faixa fina (3 dp) separando uma publicação da outra; sem cartão em volta.
            Spacer(Modifier.height(6.dp))
            Box(Modifier.fillMaxWidth().padding(top = 6.dp).height(1.dp).background(RemoteAppearance.feedDividerColor.copy(alpha = 0.45f)))
        }
    }
}

/** Visualizador único de fotos de perfil: zoom por pinça, toque duplo, controles e menu iguais nos dois perfis. */
@Composable
private fun QinblyPhotoViewer(url: String, onNotice: (String) -> Unit, onDismiss: () -> Unit, onReport: (() -> Unit)? = null) {
    val context = androidx.compose.ui.platform.LocalContext.current
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        var scale by remember(url) { mutableFloatStateOf(1f) }
        var offset by remember(url) { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }
        var controls by remember(url) { mutableStateOf(true) }
        var menuOpen by remember(url) { mutableStateOf(false) }
        Box(Modifier.fillMaxSize().background(Color.Black)) {
            QinblyLoadingSpinner(Modifier.fillMaxSize())
            AsyncImage(
                model = url,
                contentDescription = "Visualização da foto",
                modifier = Modifier.fillMaxSize()
                    .graphicsLayer { scaleX = scale; scaleY = scale; translationX = offset.x; translationY = offset.y }
                    .pointerInput(url) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(1f, 5f)
                            offset = if (scale > 1f) offset + pan else androidx.compose.ui.geometry.Offset.Zero
                        }
                    }
                    .pointerInput(url) {
                        detectTapGestures(
                            onTap = { controls = !controls },
                            onDoubleTap = {
                                scale = if (scale > 1f) 1f else 2f
                                offset = androidx.compose.ui.geometry.Offset.Zero
                            }
                        )
                    },
                contentScale = androidx.compose.ui.layout.ContentScale.Fit
            )
            if (controls) {
                Row(
                    Modifier.fillMaxWidth().align(Alignment.TopCenter).statusBarsPadding().padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = Color.White) }
                    Spacer(Modifier.weight(1f))
                    Box {
                        IconButton(onClick = { menuOpen = true }) { Icon(Icons.Default.MoreVert, contentDescription = "Opções da foto", tint = Color.White) }
                        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                            DropdownMenuItem(text = { Text("Salvar foto no celular") }, onClick = {
                                menuOpen = false
                                saveProfilePhotoToDevice(context, url, onNotice)
                            })
                            DropdownMenuItem(text = { Text("Compartilhar foto") }, onClick = {
                                menuOpen = false
                                val share = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(android.content.Intent.EXTRA_TEXT, url)
                                }
                                context.startActivity(android.content.Intent.createChooser(share, "Compartilhar foto"))
                            })
                            if (onReport != null) {
                                DropdownMenuItem(text = { Text("Denunciar foto") }, onClick = { menuOpen = false; onReport() })
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Menu "⋮" único das publicações (dono: editar, copiar link, excluir; outras pessoas: denunciar, bloquear, ocultar, copiar link). */
@Composable
private fun QinblyPostMenu(
    post: QinblyApi.PostSummary,
    prefs: android.content.SharedPreferences,
    onNotice: (String) -> Unit,
    onPostsTransform: ((List<QinblyApi.PostSummary>) -> List<QinblyApi.PostSummary>) -> Unit,
    tint: Color?,
    onReport: () -> Unit,
    onBlock: () -> Unit,
    onEdit: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var expanded by remember(post.id) { mutableStateOf(false) }
    var confirmDelete by remember(post.id) { mutableStateOf(false) }
    val mine = post.username.isNotBlank() &&
        post.username.equals(prefs.getString("username", "").orEmpty().removePrefix("@"), ignoreCase = true)
    if (confirmDelete) {
        AlertDialog(
        shape = RoundedCornerShape(28.dp),
        containerColor = QiblyTheme.card,
            onDismissRequest = { confirmDelete = false },
            title = { Text("Excluir publicação?") },
            text = { Text("Essa ação não pode ser desfeita.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    val token = prefs.getString("token", null)
                    if (token.isNullOrBlank()) onNotice("Entre na sua conta para excluir esta publicação.")
                    else Thread {
                        try {
                            QinblyApi.deletePost(token, post.id)
                            android.os.Handler(android.os.Looper.getMainLooper()).post {
                                onPostsTransform { list -> list.filterNot { it.id == post.id } }
                                onNotice("Publicação excluída.")
                            }
                        } catch (e: Exception) {
                            android.os.Handler(android.os.Looper.getMainLooper()).post { onNotice(e.message ?: "Não foi possível excluir a publicação.") }
                        }
                    }.start()
                }) { Text("Excluir", color = Color(0xFFD32F2F)) }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancelar") } }
        )
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (post.pinned) Text("📌", fontSize = 15.sp)
        IconButton(onClick = { expanded = true }) {
            if (tint != null) Icon(Icons.Default.MoreVert, contentDescription = "Opções da publicação", tint = tint)
            else Icon(Icons.Default.MoreVert, contentDescription = "Opções da publicação")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            if (mine) {
                DropdownMenuItem(text = { Text("Editar") }, onClick = { expanded = false; onEdit() })
                DropdownMenuItem(text = { Text(if (post.pinned) "Desafixar do perfil" else "Fixar no perfil") }, onClick = {
                    expanded = false
                    val token = prefs.getString("token", null)
                    if (token.isNullOrBlank()) onNotice("Entre na sua conta.")
                    else Thread {
                        try {
                            val nowPinned = QinblyApi.togglePin(token, post.id)
                            android.os.Handler(android.os.Looper.getMainLooper()).post {
                                onPostsTransform { list -> list.map { if (it.id == post.id) it.copy(pinned = nowPinned) else it } }
                                onNotice(if (nowPinned) "Publicação fixada no seu perfil." else "Publicação desafixada.")
                            }
                        } catch (e: Exception) {
                            android.os.Handler(android.os.Looper.getMainLooper()).post { onNotice(e.message ?: "Não foi possível fixar.") }
                        }
                    }.start()
                })
            }
            DropdownMenuItem(text = { Text("Copiar link") }, onClick = {
                expanded = false
                val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Publicação ${QinblyBrand.Name}", qinblyPostLink(post.id)))
                onNotice("Link copiado.")
            })
            if (mine) {
                DropdownMenuItem(
                    text = { Text("Excluir publicação", color = Color(0xFFD32F2F)) },
                    leadingIcon = { Icon(Icons.Default.DeleteForever, contentDescription = null, tint = Color(0xFFD32F2F)) },
                    onClick = { expanded = false; confirmDelete = true }
                )
            } else {
                DropdownMenuItem(text = { Text("Ocultar publicação") }, onClick = {
                    expanded = false
                    onPostsTransform { list -> list.filterNot { it.id == post.id } }
                    onNotice("Publicação ocultada deste feed.")
                })
                DropdownMenuItem(text = { Text("Denunciar publicação") }, onClick = { expanded = false; onReport() })
                DropdownMenuItem(text = { Text("Bloquear @" + post.username) }, onClick = { expanded = false; onBlock() })
            }
        }
    }
}

/** Tela cheia para editar uma publicação própria (só legenda e opções; a mídia não muda). */
@Composable
private fun QinblyEditCaptionDialog(
    post: QinblyApi.PostSummary,
    prefs: android.content.SharedPreferences,
    onNotice: (String) -> Unit,
    onDismiss: () -> Unit,
    onDeleted: () -> Unit = {},
    onSaved: (QinblyApi.PostSummary) -> Unit
) {
    var text by remember(post.id) { mutableStateOf(post.caption) }
    var textCursor by remember(post.id) { mutableIntStateOf(post.caption.length) }
    val capState = remember(post.id) { QinblyCaptionState(post.caption) }
    val tagContext = androidx.compose.ui.platform.LocalContext.current
    var busy by remember(post.id) { mutableStateOf(false) }
    var confirmDelete by remember(post.id) { mutableStateOf(false) }
    var allowComments by remember(post.id) { mutableStateOf(true) }
    var allowSharing by remember(post.id) { mutableStateOf(true) }
    var allowSaveOthers by remember(post.id) { mutableStateOf(true) }
    val isVideo = post.mediaType == "video"
    val hasPreview = post.mediaType == "image" || post.mediaType == "video"
    val changed = text.trim() != post.caption.trim()
    val save: () -> Unit = save@{
        if (busy) return@save
        val token = prefs.getString("token", null)
        if (token.isNullOrBlank()) { onNotice("Entre na sua conta para editar."); return@save }
        if (text.trim().isEmpty() && post.mediaType == "text") { onNotice("Escreva uma legenda."); return@save }
        if (!changed) { onDismiss(); return@save }
        busy = true
        QinblyHashtagStore.record(tagContext, text)
        Thread {
            try {
                val updated = QinblyApi.editPostFull(token, post.id, text.trim(), null, "", false)
                android.os.Handler(android.os.Looper.getMainLooper()).post { busy = false; onSaved(updated); onNotice("Publicação atualizada."); onDismiss() }
            } catch (e: Exception) {
                android.os.Handler(android.os.Looper.getMainLooper()).post { busy = false; onNotice(e.message ?: "Não foi possível salvar.") }
            }
        }.start()
    }
    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            shape = RoundedCornerShape(28.dp),
            containerColor = QiblyTheme.card,
            title = { Text("Excluir publicação?") },
            text = { Text("Essa ação não pode ser desfeita.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    val token = prefs.getString("token", null)
                    if (token.isNullOrBlank()) onNotice("Entre na sua conta para excluir esta publicação.")
                    else Thread {
                        try {
                            QinblyApi.deletePost(token, post.id)
                            android.os.Handler(android.os.Looper.getMainLooper()).post { onNotice("Publicação excluída."); onDeleted(); onDismiss() }
                        } catch (e: Exception) {
                            android.os.Handler(android.os.Looper.getMainLooper()).post { onNotice(e.message ?: "Não foi possível excluir a publicação.") }
                        }
                    }.start()
                }) { Text("Excluir", color = Color(0xFFD32F2F)) }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancelar") } }
        )
    }
    androidx.compose.ui.window.Dialog(
        onDismissRequest = { if (!busy) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Column(Modifier.fillMaxSize().background(QiblyTheme.background).statusBarsPadding().navigationBarsPadding().imePadding()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { if (!busy) onDismiss() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = QiblyTheme.text)
                }
                Text("Editar publicação", color = QiblyTheme.text, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text(if (busy) "Salvando…" else "Salvar", color = QinblyBrand.Accent, fontWeight = FontWeight.Bold, fontSize = 15.sp,
                    modifier = Modifier.clickable(enabled = !busy) { save() }.padding(horizontal = 14.dp, vertical = 8.dp))
            }
            Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (hasPreview) {
                        Box(Modifier.width(96.dp).height(140.dp).clip(RoundedCornerShape(14.dp)).background(Color(0xFF14181F))) {
                            if (isVideo) QinblyVideoPoster(post.mediaUrl, Modifier.fillMaxSize(), androidx.compose.ui.layout.ContentScale.Crop)
                            else AsyncImage(model = post.mediaUrl, contentDescription = "Mídia", contentScale = androidx.compose.ui.layout.ContentScale.Crop, modifier = Modifier.fillMaxSize())
                            if (isVideo) Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White,
                                modifier = Modifier.align(Alignment.TopStart).padding(6.dp).size(24.dp).clip(CircleShape).background(Color(0x80000000)).padding(4.dp))
                        }
                    }
                    Column(Modifier.weight(1f).heightIn(min = 140.dp).clip(RoundedCornerShape(14.dp)).background(QinblyBrand.Accent.copy(alpha = 0.06f)).padding(12.dp)) {
                        QinblyLegendField(capState, 2200, "Escreva uma legenda…", QiblyTheme.text, 15.sp, Modifier.fillMaxWidth().weight(1f)) { text = it; textCursor = it.length }
                        Text("${text.length} / 2200", modifier = Modifier.align(Alignment.End), color = Color.Gray, style = MaterialTheme.typography.labelSmall)
                    }
                }
                if (hasPreview) Text("A foto ou o vídeo não podem ser trocados depois de publicar.", color = Muted, fontSize = 11.sp,
                    modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                QinblyHashtagField(capState) { text = it; textCursor = it.length }
                Column(Modifier.fillMaxWidth()) {
                    Box(Modifier.fillMaxWidth().height(0.5.dp).background(QiblyTheme.border))
                    QinblyPublishRow(QinblyCameraIcons.comment, "Permitir comentários", checked = allowComments, onChecked = { allowComments = it })
                    QinblyPublishRow(QinblyCameraIcons.share, "Permitir compartilhar", checked = allowSharing, onChecked = { allowSharing = it })
                    QinblyPublishRow(QinblyCameraIcons.download, "Permitir que salvem", subtitle = "Outras pessoas podem baixar este conteúdo", checked = allowSaveOthers, onChecked = { allowSaveOthers = it })
                }
            }
            Column(Modifier.fillMaxWidth().padding(horizontal = 18.dp).padding(top = 10.dp, bottom = 22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Button(
                    onClick = { save() },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = QinblyBrand.Accent, contentColor = Color.White)
                ) { Text(if (busy) "Salvando…" else "Salvar alterações", fontWeight = FontWeight.Bold, color = Color.White) }
                Spacer(Modifier.height(34.dp))
                Row(
                    Modifier.clip(RoundedCornerShape(12.dp)).clickable(enabled = !busy) { confirmDelete = true }.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.DeleteForever, contentDescription = null, tint = Color(0xFFD32F2F), modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Excluir publicação", color = Color(0xFFD32F2F), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun QiblyShareDialog(
    post: QinblyApi.PostSummary,
    prefs: android.content.SharedPreferences,
    onDismiss: () -> Unit,
    onNotice: (String) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    var query by remember { mutableStateOf("") }
    var inbox by remember { mutableStateOf<List<QinblyApi.InboxSummary>>(emptyList()) }
    var users by remember { mutableStateOf<List<QinblyApi.UserSummary>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var sendingTo by remember { mutableStateOf("") }
    val chosen = remember { mutableStateListOf<String>() }
    val token = prefs.getString("token", null)
    val shareLink = qinblyPostLink(post.id)
    val message = "Confira esta publicação no ${QinblyBrand.Name}: " + post.caption.take(160) + "\n" + shareLink
    LaunchedEffect(token) {
        if (!token.isNullOrBlank()) {
            try { inbox = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) { QinblyApi.getInbox(token) } }
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
                    QinblyApi.searchUsers(query.trim().removePrefix("@"), token)
                }
            } catch (_: Exception) { }
        }
    }
    var note by remember { mutableStateOf("") }
    val finalMessage = (if (note.isNotBlank()) note.trim() + "\n\n" else "") + message
    fun sendToChosen() {
        if (token.isNullOrBlank()) {
            onNotice("Entre na sua conta para enviar publicações por mensagem.")
        } else if (chosen.isNotEmpty() && sendingTo.isBlank()) {
            val targets = chosen.toList()
            sendingTo = "Enviando"
            scope.launch {
                var sent = 0
                for (username in targets) {
                    try {
                        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) { QinblyApi.sendMessage(token, username, finalMessage) }
                        sent++
                    } catch (_: Exception) { }
                }
                sendingTo = ""
                onNotice(if (sent == targets.size) "Publicação enviada para $sent pessoa(s)." else "Enviado para $sent de ${targets.size} pessoa(s).")
                if (sent > 0) onDismiss()
            }
        }
    }
    val recipients = (if (query.isBlank()) inbox.map { it.username to it.displayName } else users.map { it.username to it.displayName }).distinctBy { it.first }
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            Modifier.fillMaxWidth().fillMaxHeight(0.86f)
                .background(QiblyTheme.card, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .padding(horizontal = 18.dp).padding(top = 10.dp, bottom = 14.dp)
        ) {
            Box(Modifier.align(Alignment.CenterHorizontally).width(44.dp).height(5.dp).background(QiblyTheme.secondary.copy(alpha = 0.35f), RoundedCornerShape(3.dp)))
            Row(Modifier.fillMaxWidth().padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Enviar para", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 20.sp, color = QiblyTheme.text)
                IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "Fechar", tint = QiblyTheme.text) }
            }
            // Prévia da publicação que será enviada
            Row(
                Modifier.fillMaxWidth().background(QinblyBrand.Accent.copy(alpha = 0.08f), RoundedCornerShape(16.dp)).padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.size(56.dp).clip(RoundedCornerShape(12.dp)).background(QiblyTheme.secondary.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                    val thumb = post.mediaUrls.firstOrNull() ?: post.mediaUrl
                    when {
                        post.mediaType == "video" && thumb.startsWith("http") -> QinblyVideoPoster(thumb, Modifier.fillMaxSize(), ContentScale.Crop)
                        post.mediaType == "image" && thumb.startsWith("http") -> AsyncImage(model = thumb, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        else -> Icon(Icons.Default.Share, contentDescription = null, tint = QinblyBrand.Accent)
                    }
                }
                Column(Modifier.weight(1f).padding(start = 12.dp)) {
                    Text("@" + post.username, color = QiblyTheme.text, fontWeight = FontWeight.SemiBold, maxLines = 1)
                    Text(post.caption.ifBlank { if (post.mediaType == "video") "Vídeo" else if (post.mediaType == "image") "Foto" else "Publicação do ${QinblyBrand.Name}" }.take(120), maxLines = 2, color = QiblyTheme.secondary, fontSize = 13.sp)
                }
            }
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = query, onValueChange = { query = it.take(50) },
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                shape = RoundedCornerShape(26.dp),
                placeholder = { Text("Buscar pessoas") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
            )
            Spacer(Modifier.height(10.dp))
            Text(if (query.isBlank()) "Conversas recentes" else "Pessoas encontradas", color = QiblyTheme.text, fontWeight = FontWeight.SemiBold)
            Box(Modifier.weight(1f).fillMaxWidth()) {
                if (loading) CircularProgressIndicator(Modifier.align(Alignment.Center))
                else if (recipients.isEmpty()) Text(if (query.isBlank()) "Nenhuma conversa recente. Busque uma pessoa para enviar." else "Nenhuma pessoa encontrada.", modifier = Modifier.padding(top = 14.dp), color = QiblyTheme.secondary)
                else androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                    columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(4),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(recipients, key = { it.first }) { (username, name) ->
                        val picked = username in chosen
                        Column(
                            Modifier.clip(RoundedCornerShape(12.dp)).clickable { if (picked) chosen.remove(username) else chosen.add(username) },
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(contentAlignment = Alignment.BottomEnd) {
                                Box(Modifier.size(68.dp).then(if (picked) Modifier.border(3.dp, QinblyBrand.Accent, CircleShape) else Modifier).padding(if (picked) 4.dp else 0.dp), contentAlignment = Alignment.Center) {
                                    QiblyAccountAvatar(username, "", 62.dp) { if (picked) chosen.remove(username) else chosen.add(username) }
                                }
                                if (picked) Box(Modifier.size(22.dp).background(QinblyBrand.Accent, CircleShape), contentAlignment = Alignment.Center) {
                                    Text("✓", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Text(name.ifBlank { username }, color = QiblyTheme.text, fontSize = 12.sp, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis, modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                }
            }
            if (chosen.isNotEmpty()) {
                OutlinedTextField(
                    value = note, onValueChange = { note = it.take(300) },
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(26.dp), singleLine = true,
                    placeholder = { Text("Escreva uma mensagem (opcional)") }
                )
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { sendToChosen() },
                    enabled = sendingTo.isBlank(),
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = QinblyBrand.Accent, contentColor = Color.White)
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(if (sendingTo.isNotBlank()) "Enviando..." else if (chosen.size == 1) "Enviar" else "Enviar para ${chosen.size} pessoas", fontWeight = FontWeight.SemiBold)
                }
            } else {
                Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Column(
                        Modifier.clip(RoundedCornerShape(12.dp)).clickable {
                            val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                            clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Publicação ${QinblyBrand.Name}", shareLink))
                            onNotice("Link copiado.")
                        }.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(Modifier.size(52.dp).background(QinblyBrand.Accent.copy(alpha = 0.12f), CircleShape), contentAlignment = Alignment.Center) {
                            Text("🔗", fontSize = 22.sp)
                        }
                        Text("Copiar link", color = QiblyTheme.text, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                    }
                    Column(
                        Modifier.clip(RoundedCornerShape(12.dp)).clickable {
                            try {
                                val intent = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, message) }
                                context.startActivity(Intent.createChooser(intent, "Compartilhar em outro aplicativo"))
                            } catch (_: Exception) { onNotice("Não foi possível abrir o compartilhamento.") }
                        }.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(Modifier.size(52.dp).background(QinblyBrand.Accent.copy(alpha = 0.12f), CircleShape), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = QinblyBrand.Accent)
                        }
                        Text("Outros apps", color = QiblyTheme.text, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }
        }
    }
}

/** Estrela de quatro pontas (um brilho só), usada no ícone do Descobrir. */
@Composable
private fun QinblySparkIcon(tint: Color, size: Dp) {
    androidx.compose.foundation.Canvas(Modifier.size(size)) {
        val w = this.size.width; val h = this.size.height
        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(w / 2f, 0f)
            quadraticBezierTo(w * 0.56f, h * 0.44f, w, h / 2f)
            quadraticBezierTo(w * 0.56f, h * 0.56f, w / 2f, h)
            quadraticBezierTo(w * 0.44f, h * 0.56f, 0f, h / 2f)
            quadraticBezierTo(w * 0.44f, h * 0.44f, w / 2f, 0f)
            close()
        }
        drawPath(path, tint)
    }
}

private val qinblyDailyQuestions = listOf(
    "Qual foi seu melhor momento de hoje?", "O que te fez sorrir hoje?", "Qual música não sai da sua cabeça?",
    "Qual é o seu lugar favorito?", "O que você aprendeu essa semana?", "Qual foi sua última boa surpresa?", "Com quem você gostaria de falar hoje?"
)
private val qinblyWeeklyChallenges = listOf(
    "meucafe" to "Foto do seu café", "meupet" to "Foto do seu pet", "pordosol" to "Um pôr do sol perto de você",
    "meuprato" to "O que você comeu de melhor", "meucantinho" to "Seu cantinho favorito", "diadetreino" to "Seu treino de hoje"
)

private fun qinblyPostAgeHours(createdAt: String): Long? = try {
    java.time.Duration.between(java.time.Instant.parse(createdAt), java.time.Instant.now()).toHours()
} catch (_: Exception) { null }

/** Saudação, pergunta do dia e desafio da semana, no topo do Descobrir. */
@Composable
private fun QinblyHomeHeader(
    posts: List<QinblyApi.PostSummary>, prefs: android.content.SharedPreferences,
    onCreateWith: (String, String) -> Unit
) {
    val hour = java.time.LocalTime.now().hour
    val greeting = if (hour < 12) "Bom dia" else if (hour < 18) "Boa tarde" else "Boa noite"
    val name = prefs.getString("display_name", "").orEmpty().ifBlank { prefs.getString("username", "").orEmpty() }.removePrefix("@").trim().substringBefore(" ")
    val news = posts.count { QinblyEngagement.isFollowing(prefs, it.username, it.isFollowing) && (qinblyPostAgeHours(it.createdAt) ?: 99L) < 24 }
    val today = java.time.LocalDate.now()
    val question = qinblyDailyQuestions[today.dayOfYear % qinblyDailyQuestions.size]
    val challenge = qinblyWeeklyChallenges[(today.dayOfYear / 7) % qinblyWeeklyChallenges.size]
    val participants = posts.filter { it.caption.contains("#" + challenge.first, ignoreCase = true) }.map { it.username.lowercase() }.distinct().size
    Column(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Column {
            Text(if (name.isBlank()) greeting else "$greeting, $name", color = RemoteAppearance.feedTextColor, fontWeight = FontWeight.Bold, fontSize = 24.sp)
            Text(if (news > 0) (if (news == 1) "1 novidade de quem você segue" else "$news novidades de quem você segue") else "Veja o que está acontecendo agora",
                color = RemoteAppearance.feedSecondaryTextColor, fontSize = 14.sp)
        }
        Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(QinblyBrand.Accent.copy(alpha = 0.10f))
                .clickable { onCreateWith("text", "#perguntadodia ") }.padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Text("PERGUNTA DO DIA", color = QinblyBrand.Accent, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
            Text(question, color = RemoteAppearance.feedTextColor, fontWeight = FontWeight.Bold, fontSize = 17.sp, modifier = Modifier.padding(top = 2.dp))
            Text("Responder com uma publicação  →", color = QinblyBrand.Accent, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, modifier = Modifier.padding(top = 6.dp))
        }
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).border(1.dp, RemoteAppearance.feedDividerColor, RoundedCornerShape(16.dp))
                .clickable { QinblyTagNav.tag = challenge.first }.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(androidx.compose.material.icons.Icons.Default.PhotoCamera, contentDescription = null, tint = QinblyBrand.Accent, modifier = Modifier.size(26.dp))
            Column(Modifier.weight(1f)) {
                Text("Desafio da semana: " + challenge.second, color = RemoteAppearance.feedTextColor, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1)
                Text("#" + challenge.first + (if (participants > 0) " · " + (if (participants == 1) "1 participante" else "$participants participantes") else ""), color = RemoteAppearance.feedSecondaryTextColor, fontSize = 13.sp, maxLines = 1)
            }
            Text("Participar", color = QinblyBrand.Accent, fontWeight = FontWeight.SemiBold, fontSize = 14.sp,
                modifier = Modifier.clip(RoundedCornerShape(18.dp)).border(1.dp, QinblyBrand.Accent, RoundedCornerShape(18.dp))
                    .clickable { onCreateWith("camera", "#" + challenge.first + " ") }.padding(horizontal = 14.dp, vertical = 7.dp))
        }
    }
    Box(Modifier.fillMaxWidth().height(1.dp).background(RemoteAppearance.feedDividerColor.copy(alpha = 0.45f)))
}

private fun qinblyHotScore(post: QinblyApi.PostSummary): Int = QinblyEngagement.likeCount(post) + post.comments * 2 + QinblyEngagement.repostCount(post) * 2

private fun qinblyInPeriod(post: QinblyApi.PostSummary, period: String): Boolean {
    val limit = when (period) { "Semana" -> 24L * 7; "Mês" -> 24L * 30; else -> 24L }
    return (qinblyPostAgeHours(post.createdAt) ?: 0L) <= limit
}

private fun qinblyTopHashtags(posts: List<QinblyApi.PostSummary>): List<String> =
    posts.flatMap { p -> Regex("#([\\p{L}\\p{N}_]+)").findAll(p.caption).map { it.groupValues[1].lowercase() }.toList() }
        .groupingBy { it }.eachCount().entries.sortedByDescending { it.value }.take(8).map { it.key }

/** Uma live no ar. A lista vem do servidor (ainda não existe): enquanto vazia, a tela mostra o estado "ninguém ao vivo". */
private data class QinblyLive(val id: String, val host: String, val viewers: Int, val likes: Int, val displayName: String = "", val avatarUrl: String = "")

/** Uma linha de live: posição (coroa/medalha nos 5 primeiros, número depois), foto com aro vermelho, nome, @, números e AO VIVO. */
@Composable
private fun QinblyLiveRow(position: Int, live: QinblyLive, sort: String, showPosition: Boolean = true, highlight: String = "", onClick: () -> Unit) {
    val medal = listOf(Color(0xFFEF9F27), Color(0xFF98978F), Color(0xFFB8733A), QinblyBrand.Accent, Color(0xFF1D9E75)).getOrNull(position - 1)
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable { onClick() }.padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (showPosition) Box(Modifier.width(26.dp), contentAlignment = Alignment.Center) {
            if (medal != null) Icon(if (position <= 3) QinblyCameraIcons.crown else QinblyCameraIcons.medal, contentDescription = "${position}º lugar", tint = medal, modifier = Modifier.size(if (position <= 3) 24.dp else 22.dp))
            else Text("$position", color = Muted, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        }
        Box(Modifier.size(42.dp).border(2.dp, Color(0xFFE24B4A), CircleShape).padding(3.dp).clip(CircleShape).background(Color(0x1A808080)), contentAlignment = Alignment.Center) {
            if (live.avatarUrl.startsWith("http")) AsyncImage(model = live.avatarUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            else Text((live.displayName.ifBlank { live.host }).take(2).uppercase(), color = QiblyTheme.text, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
        Column(Modifier.weight(1f)) {
            Text(qinblyHighlight(live.displayName.ifBlank { live.host }, highlight), color = QiblyTheme.text, fontWeight = FontWeight.SemiBold, maxLines = 1)
            Text(qinblyHighlight("@${live.host}", highlight), color = Muted, fontSize = 12.sp, maxLines = 1)
        }
        Column(horizontalAlignment = Alignment.End) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                Icon(if (sort == "Curtidas") Icons.Default.Favorite else QinblyCameraIcons.eye, contentDescription = null, tint = QiblyTheme.text, modifier = Modifier.size(15.dp))
                Text(if (sort == "Curtidas") "${live.likes}" else "${live.viewers}", color = QiblyTheme.text, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            }
            Text("AO VIVO", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(Color(0xFFE24B4A)).padding(horizontal = 6.dp, vertical = 1.dp))
        }
    }
}

@Composable
private fun QinblySortChips(options: List<String>, sort: String, onSort: (String) -> Unit) {
    options.forEach { name ->
        val chosen = name == sort
        Text(name, fontSize = 11.sp, color = if (chosen) QiblyTheme.background else QiblyTheme.text,
            modifier = Modifier.padding(start = 5.dp).clip(RoundedCornerShape(14.dp))
                .background(if (chosen) QiblyTheme.text else Color.Transparent)
                .border(0.5.dp, Muted, RoundedCornerShape(14.dp))
                .clickable { onSort(name) }.padding(horizontal = 9.dp, vertical = 4.dp))
    }
}

/** Lives em alta: os 5 melhores (por telespectadores ou curtidas). Com mais de 5 no ar e `onSeeMore`, aparece "Ver mais lives". */
@Composable
private fun QinblyTopLives(
    lives: List<QinblyLive>, sort: String, onSort: (String) -> Unit, prefs: android.content.SharedPreferences,
    onOpen: (QinblyLive) -> Unit = {}, onSeeMore: (() -> Unit)? = null, title: String = "Top 5 lives em alta"
) {
    val ranked = (if (sort == "Curtidas") lives.sortedByDescending { it.likes } else lives.sortedByDescending { it.viewers }).take(5)
    Column(Modifier.fillMaxWidth().padding(top = 14.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(title, color = QiblyTheme.text, fontWeight = FontWeight.Bold, fontSize = 17.sp, modifier = Modifier.weight(1f))
            QinblySortChips(listOf("Telespectadores", "Curtidas"), sort, onSort)
        }
        if (ranked.isEmpty()) {
            Text("Ninguém ao vivo agora.", color = Muted, fontSize = 14.sp, modifier = Modifier.padding(vertical = 14.dp))
        } else ranked.forEachIndexed { index, live -> QinblyLiveRow(index + 1, live, sort) { onOpen(live) } }
        if (onSeeMore != null && lives.size > 5) {
            Text("Ver mais lives", color = QinblyBrand.Accent, fontWeight = FontWeight.SemiBold, fontSize = 14.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp).clip(RoundedCornerShape(22.dp))
                    .border(0.5.dp, Muted, RoundedCornerShape(22.dp)).clickable { onSeeMore() }.padding(vertical = 9.dp))
        }
    }
}

/** Lista completa de quem está ao vivo (a partir do 6º), aberta por "Ver mais lives". */
@Composable
private fun QinblyAllLives(lives: List<QinblyLive>, onBack: () -> Unit, onOpen: (QinblyLive) -> Unit) {
    var sort by remember { mutableStateOf("Telespectadores") }
    BackHandler { onBack() }
    val ordered = when (sort) {
        "Curtidas" -> lives.sortedByDescending { it.likes }
        "Mais recentes" -> lives
        else -> lives.sortedByDescending { it.viewers }
    }
    val start = if (sort == "Mais recentes") 0 else 5
    Column(Modifier.fillMaxSize().padding(horizontal = 14.dp)) {
        Row(Modifier.fillMaxWidth().padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = RemoteAppearance.feedTextColor) }
            Text("Todas as lives ao vivo", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = RemoteAppearance.feedTextColor)
        }
        Row(Modifier.fillMaxWidth().padding(bottom = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            QinblySortChips(listOf("Telespectadores", "Curtidas", "Mais recentes"), sort) { sort = it }
        }
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 20.dp)) {
            items(ordered.size - start.coerceAtMost(ordered.size), key = { ordered[start + it].id }) { i ->
                QinblyLiveRow(start + i + 1, ordered[start + i], sort) { onOpen(ordered[start + i]) }
            }
        }
    }
}

/** Texto com o trecho digitado destacado em azul (ignora maiúsculas e acentos simples). */
private fun qinblyHighlight(text: String, query: String): androidx.compose.ui.text.AnnotatedString {
    val q = query.trim().removePrefix("@")
    val at = if (q.isBlank()) -1 else text.indexOf(q, ignoreCase = true)
    return androidx.compose.ui.text.buildAnnotatedString {
        append(text)
        if (at >= 0) addStyle(androidx.compose.ui.text.SpanStyle(color = QinblyBrand.Accent, fontWeight = FontWeight.Bold), at, at + q.length)
    }
}

/** Pesquisa dentro da aba Lives: campo com histórico (campo vazio) e lista de quem está ao vivo filtrada pelo nome ou @. */
@Composable
private fun QinblyLiveSearch(lives: List<QinblyLive>, onBack: () -> Unit, onOpen: (QinblyLive) -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { context.getSharedPreferences("qinbly_session", android.content.Context.MODE_PRIVATE) }
    var query by remember { mutableStateOf("") }
    var history by remember { mutableStateOf((prefs.getString("qinbly_live_search_history", "") ?: "").split("\n").filter { it.isNotBlank() }) }
    val save: (String) -> Unit = { term ->
        val t = term.trim()
        if (t.length >= 2) {
            history = (listOf(t) + history.filterNot { it.equals(t, true) }).take(8)
            prefs.edit().putString("qinbly_live_search_history", history.joinToString("\n")).apply()
        }
    }
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { focus.requestFocus() }
    BackHandler { onBack() }
    val q = query.trim().removePrefix("@").lowercase()
    val results = if (q.isBlank()) emptyList() else lives.filter { it.host.lowercase().contains(q) || it.displayName.lowercase().contains(q) }.sortedByDescending { it.viewers }
    Column(Modifier.fillMaxSize().padding(horizontal = 14.dp)) {
        Row(Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Fechar pesquisa", tint = RemoteAppearance.feedTextColor) }
            OutlinedTextField(
                value = query, onValueChange = { query = it.take(40) },
                modifier = Modifier.weight(1f).focusRequester(focus), singleLine = true,
                placeholder = { Text("Pesquisar quem está ao vivo") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Muted) },
                trailingIcon = { if (query.isNotEmpty()) IconButton(onClick = { query = "" }) { Icon(Icons.Default.Close, contentDescription = "Limpar", tint = Muted) } },
                shape = RoundedCornerShape(28.dp),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Search),
                keyboardActions = androidx.compose.foundation.text.KeyboardActions(onSearch = { save(query) })
            )
        }
        if (q.isBlank()) {
            if (history.isNotEmpty()) {
                Text("Pesquisados recentemente", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = RemoteAppearance.feedTextColor, modifier = Modifier.padding(vertical = 4.dp))
                history.forEach { term ->
                    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable { query = term }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(QinblyCameraIcons.clock, contentDescription = null, tint = Muted, modifier = Modifier.size(20.dp))
                        Text(term, color = RemoteAppearance.feedTextColor, modifier = Modifier.weight(1f), maxLines = 1)
                        IconButton(onClick = { history = history.filterNot { it == term }; prefs.edit().putString("qinbly_live_search_history", history.joinToString("\n")).apply() }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Remover", tint = Muted, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
            Text("Ao vivo agora", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = RemoteAppearance.feedTextColor, modifier = Modifier.padding(top = 14.dp, bottom = 4.dp))
            Text(if (lives.isEmpty()) "Ninguém ao vivo agora." else "Digite um nome ou @ para encontrar quem está transmitindo.", color = Muted, fontSize = 14.sp)
        } else {
            Text("Resultados (${results.size})", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = RemoteAppearance.feedTextColor, modifier = Modifier.padding(vertical = 4.dp))
            if (results.isEmpty()) Text("Nenhuma live encontrada com esse nome.", color = Muted, fontSize = 14.sp, modifier = Modifier.padding(vertical = 20.dp).fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 20.dp)) {
                items(results, key = { it.id }) { live ->
                    QinblyLiveRow(0, live, "Telespectadores", showPosition = false, highlight = query) { save(query); onOpen(live) }
                }
            }
        }
    }
}

/** Aba "Perto de mim": pede a localização, mostra onde você está e abre as configurações se a permissão foi bloqueada. */
@Composable
private fun QinblyNearbyPage(onNotice: (String) -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    fun hasLocation() = androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED ||
        androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
    var allowed by remember { mutableStateOf(hasLocation()) }
    var blocked by remember { mutableStateOf(false) }
    var place by remember { mutableStateOf("") }
    var locating by remember { mutableStateOf(false) }
    var failed by remember { mutableStateOf(false) }
    var attempt by remember { mutableIntStateOf(0) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        allowed = result.values.any { it } || hasLocation()
        blocked = !allowed
    }
    LaunchedEffect(allowed, attempt) {
        if (!allowed) return@LaunchedEffect
        locating = true; failed = false; place = ""
        val lm = context.getSystemService(android.content.Context.LOCATION_SERVICE) as android.location.LocationManager
        var loc: android.location.Location? = null
        try {
            for (provider in listOf(android.location.LocationManager.GPS_PROVIDER, android.location.LocationManager.NETWORK_PROVIDER, android.location.LocationManager.PASSIVE_PROVIDER)) {
                val last = try { lm.getLastKnownLocation(provider) } catch (_: Exception) { null }
                if (last != null && (loc == null || last.time > loc!!.time)) loc = last
            }
            if (loc == null) {
                val provider = when {
                    lm.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER) -> android.location.LocationManager.NETWORK_PROVIDER
                    lm.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER) -> android.location.LocationManager.GPS_PROVIDER
                    else -> null
                }
                if (provider != null) {
                    loc = kotlinx.coroutines.withTimeoutOrNull(10_000) {
                        kotlinx.coroutines.suspendCancellableCoroutine<android.location.Location?> { cont ->
                            androidx.core.location.LocationManagerCompat.getCurrentLocation(lm, provider, null, androidx.core.content.ContextCompat.getMainExecutor(context)) { result ->
                                if (cont.isActive) cont.resumeWith(Result.success(result))
                            }
                        }
                    }
                }
            }
        } catch (_: SecurityException) { allowed = false }
        val found = loc
        if (found == null) { failed = true; locating = false; return@LaunchedEffect }
        place = try {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                @Suppress("DEPRECATION")
                val a = android.location.Geocoder(context, java.util.Locale("pt", "BR")).getFromLocation(found.latitude, found.longitude, 1)?.firstOrNull()
                listOfNotNull(a?.subAdminArea ?: a?.locality, a?.adminArea).distinct().joinToString(", ")
            }
        } catch (_: Exception) { "" }
        locating = false
    }
    Column(
        Modifier.fillMaxSize().padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = QinblyBrand.Accent, modifier = Modifier.size(44.dp))
        Spacer(Modifier.height(12.dp))
        when {
            !allowed -> {
                Text("Veja o que está perto de você", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = RemoteAppearance.feedTextColor)
                Spacer(Modifier.height(6.dp))
                Text("Permita a localização para o ${QinblyBrand.Name} mostrar publicações e pessoas da sua região.", color = RemoteAppearance.feedSecondaryTextColor, fontSize = 14.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                Spacer(Modifier.height(16.dp))
                Button(onClick = {
                    if (blocked) {
                        context.startActivity(Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + context.packageName)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                        onNotice("Ative a permissão de Localização nas configurações do app.")
                    } else launcher.launch(arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION))
                }) { Text(if (blocked) "Abrir configurações" else "Permitir localização") }
            }
            locating -> {
                Text("Procurando sua localização…", color = RemoteAppearance.feedSecondaryTextColor, fontSize = 14.sp)
            }
            failed -> {
                Text("Não foi possível obter sua localização", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = RemoteAppearance.feedTextColor)
                Spacer(Modifier.height(6.dp))
                Text("Ligue o GPS do aparelho e tente de novo.", color = RemoteAppearance.feedSecondaryTextColor, fontSize = 14.sp)
                Spacer(Modifier.height(16.dp))
                OutlinedButton(onClick = { attempt++ }) { Text("Tentar de novo") }
            }
            else -> {
                Text(if (place.isNotBlank()) "Você está em $place" else "Localização encontrada", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = RemoteAppearance.feedTextColor, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                Spacer(Modifier.height(6.dp))
                Text("Ainda não há publicações com localização por perto. Elas aparecerão aqui.", color = RemoteAppearance.feedSecondaryTextColor, fontSize = 14.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                Spacer(Modifier.height(16.dp))
                OutlinedButton(onClick = { attempt++ }) { Text("Atualizar") }
            }
        }
    }
}

/** Aba Lives: barra fina "Começar uma live", "Ao vivo agora" e "Agendadas". */
@Composable
private fun QinblyLivesPage(lives: List<QinblyLive>, onNotice: (String) -> Unit) {
    var openLive by remember { mutableStateOf<QinblyLive?>(null) }
    var hostOpen by remember { mutableStateOf(false) }
    if (hostOpen) {
        QinblyLiveHost(onClose = { hostOpen = false }, onNotice = onNotice)
        return
    }
    openLive?.let { live ->
        QinblyLiveRoom(live, onClose = { openLive = null }, onNotice = onNotice)
        return
    }
    var allOpen by remember { mutableStateOf(false) }
    var liveSort by remember { mutableStateOf("Telespectadores") }
    var searchOpen by remember { mutableStateOf(false) }
    if (searchOpen) {
        QinblyLiveSearch(lives, onBack = { searchOpen = false }, onOpen = { openLive = it })
        return
    }
    if (allOpen) {
        QinblyAllLives(lives, onBack = { allOpen = false }, onOpen = { openLive = it })
        return
    }
    val livesPrefs = androidx.compose.ui.platform.LocalContext.current.getSharedPreferences("qinbly_session", android.content.Context.MODE_PRIVATE)
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Lives", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = RemoteAppearance.feedTextColor, modifier = Modifier.weight(1f))
                IconButton(onClick = { searchOpen = true }) { Icon(Icons.Default.Search, contentDescription = "Pesquisar lives", tint = RemoteAppearance.feedTextColor) }
            }
        }
        item {
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(QinblyBrand.Accent)
                    .clickable { hostOpen = true }.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(androidx.compose.material.icons.Icons.Filled.Videocam, null, tint = Color.White, modifier = Modifier.size(18.dp))
                Text("Começar uma live", color = Color.White, fontWeight = FontWeight.Medium, fontSize = 14.sp, modifier = Modifier.weight(1f))
                Icon(androidx.compose.material.icons.Icons.Filled.ChevronRight, null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }
        item { QinblyTopLives(lives, liveSort, { liveSort = it }, livesPrefs, onOpen = { openLive = it }, onSeeMore = { allOpen = true }, title = "Lives em alta") }
        item { Text("Agendadas", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = RemoteAppearance.feedTextColor) }
        item { Text("Nenhuma live agendada.", color = RemoteAppearance.feedSecondaryTextColor, fontSize = 13.sp) }
    }
}

/** Quem transmite: "Antes de começar" (título, quem assiste, comentários, presentes) → contagem 3-2-1 → "No ar".
 *  Na barra de baixo: comentário, virar câmera, microfone, pausa e X vermelho (encerrar, com confirmação).
 *  A bolinha vermelha fica acesa só enquanto transmite: some (cinza) ao pausar e acende ao retomar.
 *  OBS: ainda é só a interface — a câmera e a transmissão de verdade dependem do serviço de vídeo no servidor. */
@Composable
private fun QinblyLiveHost(onClose: () -> Unit, onNotice: (String) -> Unit) {
    var stage by remember { mutableStateOf("setup") } // setup, count, live
    var count by remember { mutableStateOf(3) }
    var seconds by remember { mutableStateOf(0) }
    var paused by remember { mutableStateOf(false) }
    var micOn by remember { mutableStateOf(true) }
    var title by remember { mutableStateOf("") }
    var audienceAll by remember { mutableStateOf(true) }
    var commentsOn by remember { mutableStateOf(true) }
    var giftsOn by remember { mutableStateOf(true) }
    var comment by remember { mutableStateOf("") }
    var confirmEnd by remember { mutableStateOf(false) }
    var summary by remember { mutableStateOf(false) }
    val liveContext = androidx.compose.ui.platform.LocalContext.current
    var hostCommentRefresh by remember { mutableIntStateOf(0) }
    val hostHandle = remember { liveContext.getSharedPreferences("qinbly_session", android.content.Context.MODE_PRIVATE).getString("username", "").orEmpty().removePrefix("@") }
    var liveCamOk by remember { mutableStateOf(androidx.core.content.ContextCompat.checkSelfPermission(liveContext, android.Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED) }
    val liveCamPermission = androidx.activity.compose.rememberLauncherForActivityResult(androidx.activity.result.contract.ActivityResultContracts.RequestPermission()) { liveCamOk = it }
    var liveFront by remember { mutableStateOf(true) }
    var liveFace by remember { mutableStateOf("Nenhum") }
    var liveColor by remember { mutableStateOf("Original") }
    var fxOpen by remember { mutableStateOf(false) }
    BackHandler { if (stage == "live") confirmEnd = true else onClose() }
    LaunchedEffect(stage) {
        if (stage == "count") {
            count = 3
            while (count > 0) { kotlinx.coroutines.delay(1000); count-- }
            seconds = 0; paused = false; stage = "live"
            onNotice("Prévia: a transmissão de verdade ainda não está disponível.")
        }
    }
    LaunchedEffect(stage, paused) {
        while (stage == "live" && !paused) { kotlinx.coroutines.delay(1000); seconds++ }
    }
    val glass = Color(0x24FFFFFF)
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF4A586D), Color(0xFF222B3A), Color(0xFF12161E))))) {
        if (liveCamOk) {
            QinblyFaceCameraPreview(liveFace, if (liveColor == "Original") null else qinblyFilterMatrix(liveColor, 1f).array, liveFront, Modifier.fillMaxSize())
        } else {
            Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("A câmera aparece aqui", color = Color(0x80FFFFFF), fontSize = 13.sp)
                Button(onClick = { liveCamPermission.launch(android.Manifest.permission.CAMERA) }) { Text("Permitir câmera") }
            }
        }
        when (stage) {
            "setup" -> {
                Row(Modifier.fillMaxWidth().statusBarsPadding().padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(34.dp).clip(CircleShape).background(glass).clickable { onClose() }, contentAlignment = Alignment.Center) {
                        Icon(androidx.compose.material.icons.Icons.Filled.Close, "Fechar", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                    Spacer(Modifier.weight(1f))
                    Text("Prévia", color = Color.White, fontWeight = FontWeight.Medium, fontSize = 13.sp, modifier = Modifier.clip(RoundedCornerShape(14.dp)).background(glass).padding(horizontal = 12.dp, vertical = 5.dp))
                    Spacer(Modifier.weight(1f))
                    Box(Modifier.size(34.dp).clip(CircleShape).background(glass).clickable { liveFront = !liveFront }, contentAlignment = Alignment.Center) {
                        Icon(androidx.compose.material.icons.Icons.Filled.Cameraswitch, "Virar câmera", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
                Column(
                    Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(10.dp).navigationBarsPadding()
                        .clip(RoundedCornerShape(22.dp)).background(Color(0xC70C1018)).padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextField(
                        value = title, onValueChange = { title = it.take(80) }, singleLine = true,
                        placeholder = { Text("Título da live (opcional)", color = Color(0x99FFFFFF)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedIndicatorColor = Color(0x4DFFFFFF), unfocusedIndicatorColor = Color(0x4DFFFFFF), focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                    Text("QUEM PODE ASSISTIR", color = Color(0xA6FFFFFF), fontSize = 10.sp, fontWeight = FontWeight.Medium)
                    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0x1AFFFFFF)).padding(3.dp)) {
                        listOf(true to "Todos", false to "Seguidores").forEach { (all, label) ->
                            Text(label, color = Color.White, fontWeight = if (audienceAll == all) FontWeight.Medium else FontWeight.Normal, fontSize = 13.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).background(if (audienceAll == all) QinblyBrand.Accent else Color.Transparent)
                                    .clickable { audienceAll = all }.padding(vertical = 7.dp))
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Comentários", color = Color.White, fontSize = 14.sp, modifier = Modifier.weight(1f))
                        Switch(checked = commentsOn, onCheckedChange = { commentsOn = it })
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Presentes", color = Color.White, fontSize = 14.sp, modifier = Modifier.weight(1f))
                        Switch(checked = giftsOn, onCheckedChange = { giftsOn = it })
                    }
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(QinblyBrand.Accent).clickable { stage = "count" }.padding(vertical = 13.dp),
                        horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(Modifier.size(9.dp).clip(CircleShape).background(Color.White))
                        Spacer(Modifier.width(8.dp))
                        Text("Iniciar live", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    }
                }
            }
            "count" -> Text(if (count > 0) "$count" else "", color = Color.White, fontSize = 96.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Center))
            else -> {
                if (paused) Column(Modifier.align(Alignment.Center).fillMaxWidth().background(Color(0xB30B0E14)).padding(vertical = 28.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Live pausada", color = Color.White, fontWeight = FontWeight.Medium, fontSize = 16.sp)
                    Row(Modifier.clip(RoundedCornerShape(20.dp)).background(QinblyBrand.Accent).clickable { paused = false }.padding(horizontal = 22.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PlayArrow, null, tint = Color.White, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Retomar", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }
                Row(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 10.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(Modifier.clip(RoundedCornerShape(20.dp)).background(glass).padding(start = 4.dp, end = 12.dp, top = 4.dp, bottom = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        Box(Modifier.size(28.dp).clip(CircleShape).background(Color(0xFF8A92A0)))
                        Column {
                            Text("Você", color = Color.White, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(androidx.compose.material.icons.Icons.Filled.Favorite, null, tint = Color(0xFFFF2D3F), modifier = Modifier.size(11.dp))
                                Spacer(Modifier.width(3.dp))
                                Text("0", color = Color.White, fontSize = 11.sp)
                            }
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    Row(Modifier.clip(RoundedCornerShape(14.dp)).background(glass).padding(horizontal = 10.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                        // Bolinha: vermelha enquanto transmite; cinza se estiver pausada.
                        Box(Modifier.size(8.dp).clip(CircleShape).background(if (paused) Color(0xFF9AA1AD) else Color(0xFFE24B4A)))
                        Spacer(Modifier.width(6.dp))
                        Text("0 · %02d:%02d".format(seconds / 60, seconds % 60), color = Color.White, fontSize = 12.sp)
                    }
                }
                if (commentsOn && hostHandle.isNotBlank()) QinblyLiveCommentsList(hostHandle, hostCommentRefresh,
                    Modifier.align(Alignment.BottomStart).navigationBarsPadding().padding(start = 10.dp, end = 70.dp, bottom = 74.dp))
                Row(
                    Modifier.align(Alignment.BottomCenter).fillMaxWidth().navigationBarsPadding().padding(horizontal = 8.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TextField(
                        value = comment, onValueChange = { comment = it }, singleLine = true, visualTransformation = QinblyTagTransformation,
                        placeholder = { Text("Comente…", color = Color(0xB3FFFFFF)) },
                        modifier = Modifier.weight(1f), shape = RoundedCornerShape(22.dp),
                        colors = TextFieldDefaults.colors(focusedContainerColor = glass, unfocusedContainerColor = glass, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent, focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Send),
                        keyboardActions = androidx.compose.foundation.text.KeyboardActions(onSend = {
                            val text = comment.trim()
                            comment = ""
                            if (text.isNotEmpty()) {
                                if (hostHandle.isBlank()) onNotice("Entre na sua conta para comentar.")
                                else qinblySendLiveComment(liveContext, hostHandle, text, { hostCommentRefresh++ }, onNotice)
                            }
                        })
                    )
                    listOf(
                        Triple(androidx.compose.material.icons.Icons.Filled.Cameraswitch, "Virar câmera", { liveFront = !liveFront }),
                        Triple(if (micOn) Icons.Default.Mic else androidx.compose.material.icons.Icons.Filled.MicOff, if (micOn) "Silenciar" else "Ligar microfone", { micOn = !micOn }),
                        Triple(Icons.Default.Pause, "Pausar", { paused = !paused })
                    ).forEach { (icon, desc, action) ->
                        Box(Modifier.size(38.dp).clip(CircleShape).background(glass).clickable { action() }, contentAlignment = Alignment.Center) {
                            Icon(icon, desc, tint = Color.White, modifier = Modifier.size(19.dp))
                        }
                    }
                    Box(Modifier.size(38.dp).clip(CircleShape).background(Color(0xFFE24B4A)).clickable { confirmEnd = true }, contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Close, "Encerrar", tint = Color.White, modifier = Modifier.size(19.dp))
                    }
                }
            }
        }
        if (stage != "count") {
            Box(
                Modifier.align(Alignment.CenterEnd).padding(end = 10.dp).size(40.dp).clip(CircleShape)
                    .background(if (fxOpen) QinblyBrand.Accent else glass).clickable { fxOpen = !fxOpen },
                contentAlignment = Alignment.Center
            ) {
                Icon(androidx.compose.material.icons.Icons.Filled.AutoAwesome, "Efeitos", tint = Color.White, modifier = Modifier.size(20.dp))
            }
            if (fxOpen) {
                LazyRow(
                    Modifier.align(Alignment.TopCenter).fillMaxWidth().statusBarsPadding().padding(top = 60.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(QinblyColorFilterNames) { cname ->
                        QinblyFilterBubble(cname, liveColor == cname, { liveColor = cname }) {
                            val swatch = qinblyFilterSwatch(cname)
                            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(swatch[0], swatch[0], swatch[1], swatch[2]))))
                        }
                    }
                    items(QinblyFaceFilterNames) { fname ->
                        QinblyFilterBubble(fname, liveFace == fname, { liveFace = if (liveFace == fname) "Nenhum" else fname }) {
                            QinblyFaceThumb(fname, Modifier.fillMaxSize())
                        }
                    }
                }
            }
        }
    }
    if (confirmEnd) {
        AlertDialog(
            onDismissRequest = { confirmEnd = false },
            shape = RoundedCornerShape(28.dp),
            title = { Text("Encerrar a live?") },
            text = { Text("A transmissão será finalizada.") },
            confirmButton = { TextButton(onClick = { confirmEnd = false; summary = true }) { Text("Encerrar", color = Color(0xFFE24B4A)) } },
            dismissButton = { TextButton(onClick = { confirmEnd = false }) { Text("Continuar") } }
        )
    }
    if (summary) {
        AlertDialog(
            onDismissRequest = { summary = false; onClose() },
            shape = RoundedCornerShape(28.dp),
            title = { Text("Live encerrada") },
            text = { Text("Tempo no ar: %02d:%02d\nEspectadores: 0\nCurtidas: 0".format(seconds / 60, seconds % 60)) },
            confirmButton = { TextButton(onClick = { summary = false; onClose() }) { Text("Concluir") } }
        )
    }
}

/** Sala ao vivo: nome + coração e número de curtidas, bolinha vermelha ao lado dos espectadores, duplo toque curte,
 *  botões 📌/📊 pequenos (abrem e fecham), campo de comentário + presente + compartilhar. */
@Composable
private fun QinblyLiveRoom(live: QinblyLive, onClose: () -> Unit, onNotice: (String) -> Unit) {
    val shareContext = androidx.compose.ui.platform.LocalContext.current
    var commentRefresh by remember { mutableIntStateOf(0) }
    var likes by remember { mutableStateOf(live.likes) }
    var heartBurst by remember { mutableStateOf(0) }
    var panel by remember { mutableStateOf("") } // "", "pin" ou "poll"
    var comment by remember { mutableStateOf("") }
    BackHandler { onClose() }
    Box(
        Modifier.fillMaxSize().background(Color(0xFF14181F))
            .pointerInput(Unit) { detectTapGestures(onDoubleTap = { likes++; heartBurst++ }) }
    ) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.size(34.dp).clip(CircleShape).background(Color(0xFF2B3544)))
            Column(Modifier.weight(1f)) {
                Text(live.host, color = Color.White, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(androidx.compose.material.icons.Icons.Filled.Favorite, null, tint = Color(0xFFFF2D3F), modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("$likes", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
            Icon(androidx.compose.material.icons.Icons.Filled.Visibility, null, tint = Color.White, modifier = Modifier.size(15.dp))
            Text("${live.viewers}", color = Color.White, fontSize = 13.sp)
            IconButton(onClick = onClose, modifier = Modifier.size(32.dp)) {
                Icon(androidx.compose.material.icons.Icons.Filled.Close, "Sair da live", tint = Color.White)
            }
        }
        if (heartBurst > 0) {
            key(heartBurst) {
                Icon(androidx.compose.material.icons.Icons.Filled.Favorite, null, tint = Color(0xFFFF2D3F), modifier = Modifier.align(Alignment.Center).size(88.dp).alpha(0.9f))
            }
            LaunchedEffect(heartBurst) { kotlinx.coroutines.delay(700); if (heartBurst > 0) heartBurst = 0 }
        }
        Column(Modifier.align(Alignment.TopEnd).padding(top = 56.dp, end = 10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("pin" to androidx.compose.material.icons.Icons.Filled.PushPin, "poll" to androidx.compose.material.icons.Icons.Filled.Poll).forEach { (id, icon) ->
                Box(
                    Modifier.size(26.dp).clip(CircleShape).background(if (panel == id) QinblyBrand.Accent else Color(0x29FFFFFF))
                        .clickable { panel = if (panel == id) "" else id },
                    contentAlignment = Alignment.Center
                ) { Icon(icon, null, tint = Color.White, modifier = Modifier.size(14.dp)) }
            }
        }
        if (panel.isNotEmpty()) {
            Column(
                Modifier.align(Alignment.TopStart).padding(start = 10.dp, end = 48.dp, top = 56.dp).fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp)).background(Color(0xEB14181F)).padding(12.dp)
            ) {
                Text(if (panel == "pin") "Comentário fixado" else "Enquete", color = Color.White, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                Spacer(Modifier.height(4.dp))
                Text(if (panel == "pin") "Nada fixado por enquanto." else "Nenhuma enquete no momento.", color = Color(0xFFB8C0CC), fontSize = 12.sp)
            }
        }
        QinblyLiveCommentsList(live.host, commentRefresh, Modifier.align(Alignment.BottomStart).navigationBarsPadding().padding(start = 10.dp, end = 70.dp, bottom = 70.dp))
        Row(
            Modifier.align(Alignment.BottomCenter).fillMaxWidth().navigationBarsPadding().padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextField(
                value = comment, onValueChange = { comment = it }, visualTransformation = QinblyTagTransformation,
                placeholder = { Text("Comente…", color = Color(0xB3FFFFFF)) },
                modifier = Modifier.weight(1f), singleLine = true, shape = RoundedCornerShape(22.dp),
                colors = TextFieldDefaults.colors(focusedContainerColor = Color(0x24FFFFFF), unfocusedContainerColor = Color(0x24FFFFFF), focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent, focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Send),
                keyboardActions = androidx.compose.foundation.text.KeyboardActions(onSend = {
                    val text = comment.trim()
                    comment = ""
                    if (text.isNotEmpty()) qinblySendLiveComment(shareContext, live.host, text, { commentRefresh++ }, onNotice)
                })
            )
            Box(Modifier.size(40.dp).clip(CircleShape).background(Color(0x24FFFFFF)).clickable { onNotice("Presentes: em breve.") }, contentAlignment = Alignment.Center) {
                Icon(androidx.compose.material.icons.Icons.Filled.CardGiftcard, "Presente", tint = Color.White, modifier = Modifier.size(20.dp))
            }
            Box(Modifier.size(40.dp).clip(CircleShape).background(Color(0x24FFFFFF)).clickable {
                try {
                    val send = android.content.Intent(android.content.Intent.ACTION_SEND).setType("text/plain")
                        .putExtra(android.content.Intent.EXTRA_TEXT, "Assista à live de @${live.host} no QINBLY!")
                    shareContext.startActivity(android.content.Intent.createChooser(send, "Compartilhar live").addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK))
                } catch (_: Exception) { onNotice("Não foi possível abrir o compartilhamento.") }
            }, contentAlignment = Alignment.Center) {
                Icon(androidx.compose.material.icons.Icons.Filled.Share, "Compartilhar", tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }
    }
}

/** Em alta: período, assuntos em alta e (abaixo, na lista) o ranking das publicações. */
@Composable
private fun QinblyEmAltaHeader(period: String, onPeriod: (String) -> Unit, tags: List<String>, empty: Boolean) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Hoje", "Semana", "Mês").forEach { label ->
                val on = period == label
                Text(label, color = if (on) Color.White else RemoteAppearance.feedTextColor, fontWeight = FontWeight.SemiBold, fontSize = 14.sp,
                    modifier = Modifier.clip(RoundedCornerShape(18.dp)).background(if (on) QinblyBrand.Accent else QinblyBrand.Accent.copy(alpha = 0.10f))
                        .clickable { onPeriod(label) }.padding(horizontal = 16.dp, vertical = 8.dp))
            }
        }
        if (tags.isNotEmpty()) {
            Text("Assuntos em alta", color = RemoteAppearance.feedTextColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                tags.forEach { tag ->
                    Text("#$tag", color = QinblyBrand.Accent, fontWeight = FontWeight.SemiBold, fontSize = 14.sp,
                        modifier = Modifier.clip(RoundedCornerShape(16.dp)).background(QinblyBrand.Accent.copy(alpha = 0.10f))
                            .clickable { QinblyTagNav.tag = tag }.padding(horizontal = 12.dp, vertical = 6.dp))
                }
            }
        }
        Text(if (empty) "Nada em alta neste período" else "Mais curtidas", color = RemoteAppearance.feedTextColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

@Composable
private fun QinblyRankRow(rank: Int, post: QinblyApi.PostSummary, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(rank.toString(), color = QinblyBrand.Accent, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, modifier = Modifier.width(26.dp))
        Box(Modifier.size(64.dp).clip(RoundedCornerShape(12.dp)).background(QinblyBrand.Accent.copy(alpha = 0.10f)), contentAlignment = Alignment.Center) {
            when {
                post.mediaType == "video" && post.mediaUrl.isNotBlank() -> QinblyVideoPoster(post.mediaUrl, Modifier.fillMaxSize(), androidx.compose.ui.layout.ContentScale.Crop)
                post.mediaType == "image" && post.mediaUrl.isNotBlank() -> AsyncImage(model = post.mediaUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = androidx.compose.ui.layout.ContentScale.Crop)
                else -> Icon(Icons.Default.Edit, contentDescription = null, tint = QinblyBrand.Accent)
            }
        }
        Column(Modifier.weight(1f)) {
            Text(post.displayName.ifBlank { post.username }, color = RemoteAppearance.feedTextColor, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 1)
            if (post.caption.isNotBlank()) Text(post.caption, color = RemoteAppearance.feedSecondaryTextColor, fontSize = 14.sp, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
            Text("♥ ${QinblyEngagement.likeCount(post)}  ·  💬 ${post.comments}", color = RemoteAppearance.feedSecondaryTextColor, fontSize = 13.sp)
        }
    }
}

@Composable
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
private fun FeedPage(scrollToTopRequest: Int, onlineUsers: Int, currentUsername: String, photoRefreshKey: Int, nameFontStyle: String, onNotice: (String) -> Unit, onOpenProfile: (String) -> Unit, onAddStory: () -> Unit, onSearch: () -> Unit, onNotifications: () -> Unit, onCreateWith: (String, String) -> Unit = { _, _ -> }) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { context.getSharedPreferences("qinbly_session", android.content.Context.MODE_PRIVATE) }
    var posts by remember { mutableStateOf(QinblyCache.feed.ifEmpty { QinblyApi.getCachedPosts(context) }) }
    var loading by remember { mutableStateOf(posts.isEmpty()) }
    var refreshKey by remember { mutableIntStateOf(0) }
    var refreshingFeed by remember { mutableStateOf(false) }
    var commentPost by remember { mutableStateOf<QinblyApi.PostSummary?>(null) }
    var sharePost by remember { mutableStateOf<QinblyApi.PostSummary?>(null) }
    val openFeedComments: (QinblyApi.PostSummary) -> Unit = { target -> commentPost = target }
    var fullscreenVideoId by remember { mutableStateOf<String?>(null) }
    var selectedStory by remember { mutableStateOf<Pair<String, String>?>(null) }
    var unreadNotifications by remember { mutableIntStateOf(0) }
    // Primeira abertura: pede de uma vez as permissões que o app usa (câmera, microfone, mídia, localização, notificações).
    // Isso só pede a permissão; a câmera continua sendo ligada apenas na tela Criar.
    val startupPermissions = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { }
    LaunchedEffect(Unit) {
        if (!prefs.getBoolean("permissions_asked_v2", false)) {
            val wanted = mutableListOf(
                android.Manifest.permission.CAMERA, android.Manifest.permission.RECORD_AUDIO,
                android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.READ_CONTACTS
            )
            if (android.os.Build.VERSION.SDK_INT >= 33) {
                wanted += android.Manifest.permission.READ_MEDIA_IMAGES
                wanted += android.Manifest.permission.READ_MEDIA_VIDEO
                wanted += android.Manifest.permission.READ_MEDIA_AUDIO
                wanted += android.Manifest.permission.POST_NOTIFICATIONS
            } else wanted += android.Manifest.permission.READ_EXTERNAL_STORAGE
            if (android.os.Build.VERSION.SDK_INT >= 34) wanted += "android.permission.READ_MEDIA_VISUAL_USER_SELECTED"
            if (android.os.Build.VERSION.SDK_INT >= 31) wanted += android.Manifest.permission.BLUETOOTH_CONNECT
            val missing = wanted.filter { androidx.core.content.ContextCompat.checkSelfPermission(context, it) != android.content.pm.PackageManager.PERMISSION_GRANTED }
            prefs.edit().putBoolean("permissions_asked_v2", true).apply()
            if (missing.isNotEmpty()) startupPermissions.launch(missing.toTypedArray())
        }
    }
    var selectedFeedTab by remember { mutableStateOf("Descobrir") }
    var emAltaPeriod by remember { mutableStateOf("Hoje") }
    var reelsOpen by remember { mutableStateOf(false) }
    LaunchedEffect(prefs.getString("token", null)) {
        val token = prefs.getString("token", null)
        if (token.isNullOrBlank()) {
            unreadNotifications = 0
        } else {
            var firstLoad = true
            while (true) {
                try {
                    val loaded = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) { QinblyApi.getNotifications(token) }
                    val seenIds = context.getSharedPreferences("qinbly_session", android.content.Context.MODE_PRIVATE).getStringSet("notif_seen_ids", emptySet()).orEmpty()
                    val newUnread = loaded.count { it.readAt.isBlank() && it.kind != "message" && it.id !in seenIds }
                    if (!firstLoad && newUnread > unreadNotifications) {
                        loaded.firstOrNull { it.readAt.isBlank() && it.kind != "message" }?.takeIf { n ->
                            val sp = context.getSharedPreferences("qinbly_session", android.content.Context.MODE_PRIVATE)
                            sp.getBoolean("setting_notifications", true) && when (n.kind) {
                                "like" -> sp.getBoolean("setting_notice_likes", true)
                                "comment", "reply" -> sp.getBoolean("setting_notice_comments", true)
                                "mention", "mention_comment", "tag" -> sp.getBoolean("setting_notice_mentions", true)
                                "follow" -> sp.getBoolean("setting_notice_followers", true)
                                else -> sp.getBoolean("setting_notice_system", true)
                            }
                        }?.let { n ->
                            qinblyNotify(context, n.actorDisplayName.ifBlank { n.actorUsername.ifBlank { QinblyBrand.Name } }, qinblyNotificationText(n.kind), n.id.hashCode())
                        }
                    }
                    firstLoad = false
                    unreadNotifications = newUnread
                } catch (_: Exception) {
                    // Mantém o último contador confirmado se a rede estiver indisponível.
                }
                kotlinx.coroutines.delay(15_000)
            }
        }
    }
    // Perfis/publicações demonstrativas locais para testar a interface sem tocar no banco.
    var demoComments by remember { mutableStateOf<Map<String, List<QinblyApi.CommentSummary>>>(emptyMap()) }
    // O feed usa somente publicações reais retornadas pela API.
    val allFeedPosts = posts.sortedByDescending { it.createdAt }.distinctBy { it.id }
    val feedTabPosts = when (selectedFeedTab) {
        "Seguindo" -> allFeedPosts.filter { QinblyEngagement.isFollowing(prefs, it.username, it.isFollowing) }
        "Comunidade" -> allFeedPosts.filter { it.comments > 0 }
        else -> allFeedPosts
    }
    val displayedPosts = when (selectedFeedTab) {
        "Vídeos" -> allFeedPosts.filter { it.mediaType.equals("video", ignoreCase = true) }
        "Em alta" -> allFeedPosts.filter { qinblyInPeriod(it, emAltaPeriod) }.sortedByDescending { qinblyHotScore(it) }.take(20)
        else -> feedTabPosts
    }
    LaunchedEffect(refreshKey, photoRefreshKey) {
        loading = posts.isEmpty()
        Thread {
            try {
                val loaded = QinblyApi.getPosts(prefs.getString("token", null))
                QinblyCache.feed = loaded
                if (!prefs.getString("token", null).isNullOrBlank()) android.os.Handler(android.os.Looper.getMainLooper()).post { QinblyEngagement.learnFromPosts(prefs, loaded) }
                android.os.Handler(android.os.Looper.getMainLooper()).post { posts = loaded; loading = false; refreshingFeed = false }
            } catch (e: Exception) {
                val cached = QinblyApi.getCachedPosts(context)
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    if (cached.isNotEmpty()) posts = cached
                    loading = false
                    refreshingFeed = false
                    onNotice(if (cached.isNotEmpty()) "Sem conexão: mostrando o último feed sincronizado." else (e.message ?: "Não foi possível carregar o feed."))
                }
            }
        }.start()
    }
    // Deslizar para os lados troca as abas de cima (Descobrir, Seguindo, Em alta, Vídeos, Lives, Perto de mim); a barra de baixo não muda por gesto.
    val feedTabOrder = listOf("Descobrir", "Seguindo", "Em alta", "Vídeos", "Lives", "Perto de mim")
    Column(
        Modifier.fillMaxSize().background(RemoteAppearance.headerBackgroundColor).padding(0.dp)
            .pointerInput(Unit) {
                var dragged = 0f
                detectHorizontalDragGestures(
                    onHorizontalDrag = { _, amount -> dragged += amount },
                    onDragEnd = {
                        val threshold = 72.dp.toPx()
                        val index = feedTabOrder.indexOf(selectedFeedTab)
                        val target = when {
                            dragged <= -threshold -> feedTabOrder.getOrNull(index + 1)
                            dragged >= threshold -> feedTabOrder.getOrNull(index - 1)
                            else -> null
                        }
                        if (target != null) selectedFeedTab = target
                        dragged = 0f
                    },
                    onDragCancel = { dragged = 0f }
                )
            }
    ) {
        Box(Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp, top = 6.dp).heightIn(min = 42.dp)) {
            Text(QinblyBrand.Name, style = androidx.compose.ui.text.TextStyle(brush = Brush.horizontalGradient(listOf(QinblyBrand.Accent, QinblyBrand.Pink))), fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp, modifier = Modifier.align(Alignment.Center))
            Row(Modifier.align(Alignment.CenterEnd), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onSearch, modifier = Modifier.size(42.dp)) {
                Icon(Icons.Default.Search, contentDescription = "Buscar", tint = RemoteAppearance.headerTextColor, modifier = Modifier.size(26.dp))
            }
            Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                IconButton(onClick = onNotifications, modifier = Modifier.size(48.dp)) {
                    Icon(androidx.compose.material.icons.Icons.Outlined.NotificationsNone, contentDescription = "Notificações", tint = RemoteAppearance.headerTextColor, modifier = Modifier.size(32.dp))
                }
                if (unreadNotifications > 0) {
                    Box(
                        Modifier.align(Alignment.TopEnd).offset(x = 2.dp, y = 0.dp)
                            .sizeIn(minWidth = 20.dp, minHeight = 20.dp)
                            .border(2.dp, RemoteAppearance.screenBackgroundColor, CircleShape)
                            .clip(CircleShape).background(QinblyBrand.Accent).padding(horizontal = if (unreadNotifications > 9) 5.dp else 0.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(if (unreadNotifications > 99) "99+" else unreadNotifications.toString(), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                }
            }
        }
        }
        Row(Modifier.fillMaxWidth().padding(top = 2.dp)) {
            listOf("Descobrir", "Seguindo", "Em alta", "Vídeos", "Lives", "Perto de mim").forEach { feedTab ->
                val selected = selectedFeedTab == feedTab
                val tint = if (selected) QinblyBrand.Accent else RemoteAppearance.headerTextColor.copy(alpha = 0.62f)
                Column(
                    modifier = Modifier.weight(1f).clickable {
                        selectedFeedTab = feedTab
                    }.padding(top = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when (feedTab) {
                        "Descobrir" -> QinblySparkIcon(tint, 22.dp)
                        else -> Icon(
                            when (feedTab) {
                                "Seguindo" -> Icons.Default.People
                                "Em alta" -> Icons.Default.LocalFireDepartment
                                "Vídeos" -> Icons.Default.PlayCircle
                                "Lives" -> androidx.compose.material.icons.Icons.Default.Sensors
                                else -> Icons.Default.LocationOn
                            }, contentDescription = feedTab, tint = tint, modifier = Modifier.size(22.dp)
                        )
                    }
                    Text(feedTab, color = tint, fontSize = 10.sp, maxLines = 1, softWrap = false, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium, modifier = Modifier.padding(top = 2.dp))
                    Spacer(Modifier.height(5.dp))
                    Box(Modifier.fillMaxWidth().height(3.dp).background(if (selected) QinblyBrand.Accent else Color.Transparent))
                }
            }
        }
        Box(Modifier.fillMaxWidth().height(0.5.dp).background(RemoteAppearance.feedDividerColor))
        if (selectedFeedTab == "Lives") {
            QinblyLivesPage(emptyList(), onNotice)
        } else if (selectedFeedTab == "Perto de mim") {
            QinblyNearbyPage(onNotice)
        } else if (!loading && displayedPosts.isEmpty() && selectedFeedTab != "Em alta" && selectedFeedTab != "Descobrir") {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Ainda não há publicações", fontWeight = FontWeight.SemiBold)
                    Text("Quando alguém publicar, as postagens públicas aparecerão aqui.", color = Muted, style = MaterialTheme.typography.bodySmall)
                }
            }
        } else {
            val feedListState = androidx.compose.foundation.lazy.rememberLazyListState()
            LaunchedEffect(refreshKey, selectedFeedTab) { feedListState.scrollToItem(0) }
            LaunchedEffect(scrollToTopRequest) { if (scrollToTopRequest > 0) feedListState.animateScrollToItem(0) }
            androidx.compose.material3.pulltorefresh.PullToRefreshBox(isRefreshing = refreshingFeed, onRefresh = { RemoteUiRefreshSignal.value++; if (!refreshingFeed) { refreshingFeed = true; refreshKey++ } }, modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                state = feedListState,
                verticalArrangement = Arrangement.spacedBy(0.dp),
                contentPadding = PaddingValues(top = 0.dp, bottom = 18.dp)
            ) {
                if (!loading && displayedPosts.isEmpty()) {
                    item {
                        Column(
                            Modifier.fillMaxWidth().padding(horizontal = 28.dp, vertical = 56.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("Ainda não há publicações", color = RemoteAppearance.feedTextColor, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text("Quando as pessoas publicarem, elas aparecerão aqui.", color = RemoteAppearance.feedSecondaryTextColor, fontSize = 14.sp)
                            OutlinedButton(onClick = { refreshKey++ }) {
                                Text("Atualizar feed")
                            }
                        }
                    }
                }
                if (selectedFeedTab == "Descobrir") item(key = "home-header") {
                    QinblyHomeHeader(allFeedPosts, prefs, onCreateWith)
                }
                if (selectedFeedTab == "Em alta") item(key = "alta-header") {
                    QinblyEmAltaHeader(emAltaPeriod, { emAltaPeriod = it }, qinblyTopHashtags(allFeedPosts.filter { qinblyInPeriod(it, emAltaPeriod) }), displayedPosts.isEmpty())
                }
                if (selectedFeedTab == "Em alta") items(displayedPosts.size, key = { "rank-" + displayedPosts[it].id }) { index ->
                    QinblyRankRow(index + 1, displayedPosts[index]) { fullscreenVideoId = displayedPosts[index].id }
                } else items(displayedPosts, key = { it.id }) { post ->
                    Column(Modifier.fillMaxWidth()) {
                    QinblyPostCard(
                        post = post,
                        prefs = prefs,
                        nameFontStyle = nameFontStyle,
                        onNotice = onNotice,
                        onOpenProfile = onOpenProfile,
                        onOpenViewer = { fullscreenVideoId = it.id },
                        onComment = { openFeedComments(it) },
                        onShare = { sharePost = it },
                        onPostsTransform = { change -> posts = change(posts) },
                        onRefresh = { refreshKey++ }
                    )
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
    var selectedAudioPost by remember { mutableStateOf<QinblyApi.PostSummary?>(null) }
    if (reelsOpen) {
        // Clipes: vídeos do feed, um por tela, deslizando na vertical, com o mesmo visualizador, botões e comentários de todo o app.
        val reels = allFeedPosts.filter { it.mediaType == "video" && it.mediaUrl.isNotBlank() }
        if (reels.isEmpty()) {
            LaunchedEffect(Unit) { reelsOpen = false; onNotice("Ainda não há vídeos para mostrar em Clipes.") }
        } else {
            QinblyPostViewer(
                posts = reels, startPostId = reels.first().id, prefs = prefs, currentUsername = currentUsername, onNotice = onNotice,
                onDismiss = { reelsOpen = false }, onComment = { commentPost = it }, onShare = { sharePost = it },
                onOpenProfile = { reelsOpen = false; onOpenProfile(it) }
            )
        }
    }
    val fullscreenStartId = fullscreenVideoId
    val fullscreenVideos = qinblyViewerList(displayedPosts.filter { (it.mediaType == "video" || it.mediaType == "image") && it.mediaUrl.isNotBlank() }, fullscreenStartId)
    if (fullscreenStartId != null && fullscreenVideos.any { it.id == fullscreenStartId }) {
        QinblyPostViewer(
            posts = fullscreenVideos,
            startPostId = fullscreenStartId,
            prefs = prefs,
            currentUsername = currentUsername,
            onNotice = onNotice,
            onDismiss = { fullscreenVideoId = null },
            onComment = { openFeedComments(it) },
            onShare = { sharePost = it },
            onOpenProfile = { fullscreenVideoId = null; onOpenProfile(it) },
            onOpenAudio = { selectedAudioPost = it; fullscreenVideoId = null }
        )
    }
    if (selectedAudioPost != null) {
        val sourcePost = selectedAudioPost!!
        val originalSound = sourcePost.audioId.isBlank()
        val matchingPosts = allFeedPosts.filter { candidate ->
            candidate.mediaType.equals("video", ignoreCase = true) &&
                if (originalSound) candidate.id == sourcePost.id
                else candidate.audioId == sourcePost.audioId
        }.ifEmpty { listOf(sourcePost) }
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { selectedAudioPost = null },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Column(Modifier.fillMaxSize().background(Color(0xFF101010))) {
                Row(
                    Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { selectedAudioPost = null }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = Color.White)
                    }
                    Spacer(Modifier.width(8.dp))
                    Text("Áudio", color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                }
                Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(74.dp).clip(CircleShape).background(Color(0xFF252525)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (originalSound && sourcePost.avatarUrl.isNotBlank()) {
                            AsyncImage(
                                model = sourcePost.avatarUrl, contentDescription = "Autor do som original",
                                modifier = Modifier.fillMaxSize(), contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        } else Icon(Icons.Default.MusicNote, contentDescription = null, tint = Color.White, modifier = Modifier.size(34.dp))
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(if (originalSound) "Som original" else sourcePost.audioTitle.ifBlank { "Áudio" },
                            color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                        Text(if (originalSound) "@${sourcePost.username}" else sourcePost.audioArtist.ifBlank { "@${sourcePost.username}" },
                            color = Color.LightGray, fontSize = 15.sp)
                        Text("${matchingPosts.size} publicação(ões) carregada(s) com este áudio",
                            color = Color.LightGray, fontSize = 12.sp)
                    }
                }
                Text("Publicações com este áudio", color = Color.White, fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 18.dp, top = 12.dp, bottom = 10.dp))
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(matchingPosts, key = { it.id }) { post ->
                        Row(
                            Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF202020))
                                .clickable { selectedAudioPost = null; fullscreenVideoId = post.id }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                                    .data(post.mediaUrl)
                                    .decoderFactory(coil.decode.VideoFrameDecoder.Factory())
                                    .build(),
                                contentDescription = "Vídeo de @${post.username}",
                                modifier = Modifier.size(width = 86.dp, height = 105.dp).clip(RoundedCornerShape(8.dp)),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text("@${post.username}", color = Color.White, fontWeight = FontWeight.Bold)
                                Text(post.caption, color = Color.LightGray, maxLines = 3)
                            }
                            Icon(Icons.Default.PlayCircle, contentDescription = "Assistir", tint = Color.White)
                        }
                    }
                }
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
    commentPost?.let { target ->
        QinblyCommentsDialog(
            post = target,
            prefs = prefs,
            currentUsername = currentUsername,
            onNotice = onNotice,
            onDismiss = { commentPost = null },
            onCountChanged = { postId, count ->
                posts = posts.map { if (it.id == postId && it.comments != count) it.copy(comments = count) else it }
                QinblyCache.feed = posts
            }
        )
    }

}

}

@Composable
private fun CommunitiesPage(onNotice: (String) -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { context.getSharedPreferences("qinbly_session", android.content.Context.MODE_PRIVATE) }
    val mainHandler = remember { android.os.Handler(android.os.Looper.getMainLooper()) }
    var selected by remember { mutableStateOf("Minhas") }
    var list by remember { mutableStateOf(QinblyCache.communities[selected] ?: emptyList<QinblyApi.CommunitySummary>()) }
    var loading by remember { mutableStateOf(list.isEmpty()) }
    var showCreate by remember { mutableStateOf(false) }
    var openId by remember { mutableStateOf<String?>(null) }
    var reload by remember { mutableIntStateOf(0) }
    val filterKey = when (selected) { "Minhas" -> "mine"; "Em alta" -> "trending"; else -> "discover" }

    LaunchedEffect(selected, reload) {
        list = QinblyCache.communities[selected] ?: emptyList()
        loading = list.isEmpty()
        val token = prefs.getString("token", null)
        Thread {
            try {
                val result = QinblyApi.getCommunities(filterKey, token)
                mainHandler.post { QinblyCache.communities[selected] = result; list = result; loading = false }
            } catch (e: Exception) {
                mainHandler.post { loading = false; onNotice(e.message ?: "Não foi possível carregar as comunidades.") }
            }
        }.start()
    }

    fun requireLogin(): String? {
        val t = prefs.getString("token", null)
        if (t.isNullOrBlank()) onNotice("Entre na sua conta para participar de comunidades.")
        return t?.takeIf { it.isNotBlank() }
    }

    fun toggleJoin(c: QinblyApi.CommunitySummary) {
        val token = requireLogin() ?: return
        val optimistic = !c.joined
        list = list.map { if (it.id == c.id) it.copy(joined = optimistic, members = (it.members + if (optimistic) 1 else -1).coerceAtLeast(0)) else it }
        Thread {
            try {
                val (joined, members) = QinblyApi.toggleCommunityJoin(token, c.id)
                mainHandler.post {
                    list = list.map { if (it.id == c.id) it.copy(joined = joined, members = members) else it }
                    QinblyCache.communities.clear()
                    if (selected == "Minhas" && !joined) list = list.filterNot { it.id == c.id }
                }
            } catch (e: Exception) {
                mainHandler.post { list = list.map { if (it.id == c.id) c else it }; onNotice(e.message ?: "Não foi possível atualizar.") }
            }
        }.start()
    }

    Column(Modifier.fillMaxSize().background(RemoteAppearance.screenBackgroundColor).padding(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Minhas", "Em alta", "Descobrir").forEach { item ->
                Surface(
                    modifier = Modifier.weight(1f).clickable { selected = item },
                    shape = RoundedCornerShape(22.dp),
                    color = if (selected == item) QinblyBrand.Accent else QiblyTheme.card
                ) {
                    Text(item, modifier = Modifier.padding(vertical = 12.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        color = if (selected == item) Color.White else RemoteAppearance.screenTextColor)
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = { if (requireLogin() != null) showCreate = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = ButtonDefaults.buttonColors(containerColor = QinblyBrand.Accent, contentColor = Color.White)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Criar comunidade", fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(12.dp))
        if (loading && list.isEmpty()) {
            Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = QinblyBrand.Accent) }
        } else if (list.isEmpty()) {
            Text(
                if (selected == "Minhas") "Você ainda não participa de comunidades." else "Ainda não há comunidades por aqui.",
                color = RemoteAppearance.screenSecondaryTextColor
            )
            Spacer(Modifier.height(8.dp))
            Text(if (selected == "Minhas") "Crie a sua ou descubra outras para participar." else "Seja a primeira pessoa a criar uma.", color = QinblyBrand.Accent,
                modifier = Modifier.clickable { if (selected == "Minhas") selected = "Descobrir" else if (requireLogin() != null) showCreate = true })
        } else {
            LazyColumn(Modifier.fillMaxSize()) {
                items(list, key = { it.id }) { c ->
                    Row(Modifier.fillMaxWidth().clickable { openId = c.id }.padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                        QinblyCommunityAvatar(c.name, 48.dp)
                        Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                            Text(c.name, color = RemoteAppearance.screenTextColor, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                            if (c.description.isNotBlank()) Text(c.description, color = RemoteAppearance.screenSecondaryTextColor, style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                            Text(if (c.members == 1) "1 membro" else "${c.members} membros", color = RemoteAppearance.screenSecondaryTextColor, style = MaterialTheme.typography.labelSmall)
                        }
                        if (c.isOwner) {
                            Text("Sua", color = QinblyBrand.Accent, style = MaterialTheme.typography.labelMedium)
                        } else if (c.joined) {
                            OutlinedButton(onClick = { toggleJoin(c) }) { Text("Participando", color = RemoteAppearance.screenSecondaryTextColor) }
                        } else {
                            Button(onClick = { toggleJoin(c) }, colors = ButtonDefaults.buttonColors(containerColor = QinblyBrand.Accent, contentColor = Color.White)) { Text("Entrar") }
                        }
                    }
                    HorizontalDivider(color = RemoteAppearance.screenSecondaryTextColor.copy(alpha = 0.15f))
                }
            }
        }
    }

    if (showCreate) {
        QinblyCreateCommunityDialog(
            onDismiss = { showCreate = false },
            onCreate = { name, description, emoji, done ->
                val token = prefs.getString("token", null)
                if (token.isNullOrBlank()) { done("Entre na sua conta para criar uma comunidade."); return@QinblyCreateCommunityDialog }
                Thread {
                    try {
                        val created = QinblyApi.createCommunity(token, name, description, emoji)
                        mainHandler.post {
                            done(null); showCreate = false
                            QinblyCache.communities.clear()
                            selected = "Minhas"; reload++
                            openId = created.id
                            onNotice("Comunidade criada.")
                        }
                    } catch (e: Exception) {
                        mainHandler.post { done(e.message ?: "Não foi possível criar a comunidade. Confira se o servidor e o banco foram atualizados.") }
                    }
                }.start()
            }
        )
    }
    openId?.let { id ->
        QinblyCommunityDialog(
            communityId = id,
            onNotice = onNotice,
            onChanged = { QinblyCache.communities.clear(); reload++ },
            onDismiss = { openId = null }
        )
    }
}

@Composable
private fun QinblyCreateCommunityDialog(onDismiss: () -> Unit, onCreate: (String, String, String, (String?) -> Unit) -> Unit) {
    var errorText by remember { mutableStateOf<String?>(null) }
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val emoji = "👥"
    var sending by remember { mutableStateOf(false) }
    val photoContext = androidx.compose.ui.platform.LocalContext.current
    var photoBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) photoBitmap = runCatching { photoContext.contentResolver.openInputStream(uri)?.use { android.graphics.BitmapFactory.decodeStream(it) } }.getOrNull()
    }
    BackHandler(enabled = !sending) { onDismiss() }
    androidx.compose.ui.window.Dialog(
        onDismissRequest = { if (!sending) onDismiss() },
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false, dismissOnClickOutside = false)
    ) {
        Surface(Modifier.fillMaxSize(), color = QiblyTheme.background) {
            Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().imePadding().padding(horizontal = 16.dp)) {
                Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { if (!sending) onDismiss() }) { Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = QiblyTheme.text) }
                    Text("Nova comunidade", color = QiblyTheme.text, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
                Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(Modifier.height(14.dp))
                    Box(Modifier.size(112.dp).clickable { photoPicker.launch("image/*") }, contentAlignment = Alignment.Center) {
                        Box(Modifier.fillMaxSize().clip(CircleShape).background(QinblyBrand.Accent.copy(alpha = 0.10f)).border(1.5.dp, QinblyBrand.Accent, CircleShape), contentAlignment = Alignment.Center) {
                            val bmp = photoBitmap
                            if (bmp != null) androidx.compose.foundation.Image(bmp.asImageBitmap(), contentDescription = "Foto da comunidade", contentScale = androidx.compose.ui.layout.ContentScale.Crop, modifier = Modifier.fillMaxSize())
                            else Icon(Icons.Outlined.CameraAlt, contentDescription = null, tint = QinblyBrand.Accent, modifier = Modifier.size(40.dp))
                        }
                        Box(Modifier.align(Alignment.BottomEnd).size(32.dp).clip(CircleShape).background(QinblyBrand.Accent).border(2.dp, QiblyTheme.background, CircleShape), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }
                    Text(if (photoBitmap == null) "Adicionar foto (opcional)" else "Trocar foto", color = QinblyBrand.Accent, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 8.dp, bottom = 14.dp))
                    Text("NOME", color = QiblyTheme.secondary, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp))
                    OutlinedTextField(
                        value = name, onValueChange = { name = it.take(40) }, singleLine = true,
                        placeholder = { Text("Ex.: Amantes de Música", color = QiblyTheme.secondary) },
                        supportingText = { Text("${name.length}/40", modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.End) },
                        shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()
                    )
                    Text("DESCRIÇÃO", color = QiblyTheme.secondary, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp))
                    OutlinedTextField(
                        value = description, onValueChange = { description = it.take(300) }, minLines = 3, maxLines = 5,
                        placeholder = { Text("Sobre o que é a comunidade?", color = QiblyTheme.secondary) },
                        shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()
                    )
                    errorText?.let { Text(it, color = Color(0xFFD33B3B), style = MaterialTheme.typography.bodySmall, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) }
                    Text("Qualquer pessoa poderá encontrar e entrar na comunidade.", color = QiblyTheme.secondary, style = MaterialTheme.typography.bodySmall, modifier = Modifier.fillMaxWidth().padding(top = 12.dp))
                    Spacer(Modifier.height(16.dp))
                }
                Button(
                    enabled = !sending && name.trim().length >= 3,
                    onClick = {
                        sending = true
                        errorText = null
                        onCreate(name.trim(), description.trim(), emoji) { err -> sending = false; errorText = err }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = QinblyBrand.Accent),
                    modifier = Modifier.fillMaxWidth().height(52.dp).padding(bottom = 0.dp)
                ) { Text(if (sending) "Criando..." else "Criar comunidade", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp) }
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

private fun qinblyDayLabel(createdAt: String): String {
    val d = runCatching { java.time.Instant.parse(createdAt).atZone(java.time.ZoneId.systemDefault()).toLocalDate() }.getOrNull() ?: return ""
    val today = java.time.LocalDate.now()
    return when (d) {
        today -> "Hoje"
        today.minusDays(1) -> "Ontem"
        else -> d.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    }
}

private fun qinblyClockLabel(createdAt: String): String =
    runCatching { java.time.Instant.parse(createdAt).atZone(java.time.ZoneId.systemDefault()).format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")) }.getOrDefault("")

private fun qinblyNameColor(username: String): Color {
    val palette = listOf(Color(0xFFD9480F), Color(0xFF7C3AED), Color(0xFF0E9F6E), Color(0xFF0B7285), Color(0xFFC2255C), Color(0xFF5F3DC4), Color(0xFF2B8A3E), Color(0xFFE67700))
    return palette[(username.hashCode() and 0x7fffffff) % palette.size]
}

/** Foto da comunidade; enquanto não houver foto, círculo azul com a letra inicial do nome. */
@Composable
private fun QinblyCommunityAvatar(name: String, diameter: Dp) {
    Box(Modifier.size(diameter).clip(CircleShape).background(QinblyBrand.Accent), contentAlignment = Alignment.Center) {
        Text(name.trim().take(1).uppercase().ifBlank { "Q" }, color = Color.White, fontWeight = FontWeight.Bold, fontSize = (diameter.value * 0.42f).sp)
    }
}

@Composable
private fun QinblyCommunityDialog(communityId: String, onNotice: (String) -> Unit, onChanged: () -> Unit, onDismiss: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { context.getSharedPreferences("qinbly_session", android.content.Context.MODE_PRIVATE) }
    val mainHandler = remember { android.os.Handler(android.os.Looper.getMainLooper()) }
    var community by remember { mutableStateOf<QinblyApi.CommunitySummary?>(null) }
    var posts by remember { mutableStateOf<List<QinblyApi.CommunityPost>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var text by remember { mutableStateOf("") }
    var sending by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    var confirmLeave by remember { mutableStateOf(false) }
    var confirmClear by remember { mutableStateOf(false) }
    var showReport by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showInfo by remember { mutableStateOf(false) }
    var showEmoji by remember { mutableStateOf(false) }
    val cKb = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current
    val cFocus = androidx.compose.ui.platform.LocalFocusManager.current
    var showAttach by remember { mutableStateOf(false) }
    var refresh by remember { mutableIntStateOf(0) }
    var mutedFlag by remember { mutableStateOf(prefs.getBoolean("community_muted_$communityId", false)) }
    var clearedAt by remember { mutableStateOf(prefs.getString("community_cleared_$communityId", "") ?: "") }
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()

    // A conversa atualiza sozinha a cada poucos segundos.
    LaunchedEffect(communityId) { while (true) { kotlinx.coroutines.delay(6_000L); refresh++ } }
    LaunchedEffect(communityId, refresh) {
        val token = prefs.getString("token", null)
        Thread {
            try {
                val (c, p) = QinblyApi.getCommunity(communityId, token)
                mainHandler.post { community = c; posts = p; loading = false }
            } catch (e: Exception) {
                mainHandler.post { loading = false; if (community == null) onNotice(e.message ?: "Não foi possível abrir a comunidade.") }
            }
        }.start()
    }
    val chat = remember(posts, clearedAt) { posts.sortedBy { it.createdAt }.filter { clearedAt.isBlank() || it.createdAt > clearedAt } }
    LaunchedEffect(chat.size) { if (chat.isNotEmpty()) listState.scrollToItem(chat.lastIndex + 0) }

    var recordingVoiceC by remember { mutableStateOf(false) }
    fun sendCWith(action: (String, String) -> Unit) {
        val token = prefs.getString("token", null)
        val cid = community?.id
        if (token.isNullOrBlank() || cid == null) { onNotice("Entre na sua conta para enviar mensagens."); return }
        if (sending) return
        sending = true
        Thread {
            try {
                action(token, cid)
                mainHandler.post { sending = false; showAttach = false; refresh++ }
            } catch (e: Exception) { mainHandler.post { sending = false; onNotice(e.message ?: "Não foi possível enviar.") } }
        }.start()
    }
    var showCPoll by remember { mutableStateOf(false) }
    var showCStickerCreator by remember { mutableStateOf(false) }
    var cStickerRefresh by remember { mutableIntStateOf(0) }
    val cDocPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) sendCWith { token, cid -> QinblyApi.sendCommunityFile(context, token, cid, uri) }
    }
    val cGalleryPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) sendCWith { token, cid -> QinblyApi.sendCommunityImage(context, token, cid, uri) }
    }
    var pendingCPhoto by remember { mutableStateOf<java.io.File?>(null) }
    val cCameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        val f = pendingCPhoto
        pendingCPhoto = null
        if (ok && f != null) sendCWith { token, cid ->
            try { QinblyApi.sendCommunityImage(context, token, cid, android.net.Uri.fromFile(f)) } finally { f.delete() }
        } else f?.delete()
    }
    fun launchCCamera() {
        val f = java.io.File(context.cacheDir, "qinbly-chat-" + System.currentTimeMillis() + ".jpg")
        pendingCPhoto = f
        cCameraLauncher.launch(androidx.core.content.FileProvider.getUriForFile(context, context.packageName + ".fileprovider", f))
    }
    val cCameraPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) launchCCamera() else onNotice("Permita a câmera para tirar a foto.")
    }
    fun cCameraShot() {
        if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED) launchCCamera()
        else cCameraPermission.launch(android.Manifest.permission.CAMERA)
    }
    val cContactPicker = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val data = result.data?.data ?: return@rememberLauncherForActivityResult
        try {
            context.contentResolver.query(data, arrayOf(android.provider.ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER), null, null, null)?.use { cur ->
                if (cur.moveToFirst()) {
                    val who = cur.getString(0).orEmpty()
                    val number = cur.getString(1).orEmpty()
                    sendCWith { token, cid -> QinblyApi.addCommunityPost(token, cid, "Contato: $who\n$number") }
                }
            }
        } catch (_: Exception) { onNotice("Não foi possível ler o contato escolhido.") }
    }
    fun sendCLocation() {
        try {
            val lm = context.getSystemService(android.content.Context.LOCATION_SERVICE) as android.location.LocationManager
            val best = lm.getProviders(true).mapNotNull { try { lm.getLastKnownLocation(it) } catch (_: SecurityException) { null } }.maxByOrNull { it.time }
            if (best == null) onNotice("Não consegui pegar sua localização. Ligue o GPS e tente de novo.")
            else sendCWith { token, cid -> QinblyApi.addCommunityPost(token, cid, "Minha localização:\nhttps://maps.google.com/?q=" + best.latitude + "," + best.longitude) }
        } catch (_: SecurityException) { onNotice("Permita a localização para enviar onde você está.") }
    }
    val cLocationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { res ->
        if (res.values.any { it }) sendCLocation() else onNotice("Permita a localização para enviar onde você está.")
    }
    val cMicPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) recordingVoiceC = true else onNotice("Permita o microfone para gravar a mensagem de voz.")
    }
    fun startCVoice() {
        if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) == android.content.pm.PackageManager.PERMISSION_GRANTED) recordingVoiceC = true
        else cMicPermission.launch(android.Manifest.permission.RECORD_AUDIO)
    }
    fun onCAttach(label: String) {
        when (label) {
            "Galeria" -> cGalleryPicker.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            "Contato" -> cContactPicker.launch(android.content.Intent(android.content.Intent.ACTION_PICK, android.provider.ContactsContract.CommonDataKinds.Phone.CONTENT_URI))
            "Localização" -> {
                val granted = androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED ||
                    androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
                if (granted) sendCLocation() else cLocationPermission.launch(arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION))
            }
            "Áudio" -> startCVoice()
            "Documento" -> cDocPicker.launch(arrayOf("application/pdf", "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "application/vnd.ms-powerpoint", "application/vnd.openxmlformats-officedocument.presentationml.presentation", "text/plain", "text/csv", "application/zip"))
            "Enquete" -> showCPoll = true
            else -> onNotice("$label: ainda não está disponível.")
        }
    }
    if (showCStickerCreator) {
        QinblyStickerCreateDialog(QinblyBrand.Accent, onNotice, onDismiss = { showCStickerCreator = false }, onCreated = { cStickerRefresh++ })
    }
    if (showCPoll) {
        QinblyPollCreateDialog(QinblyBrand.Accent, onDismiss = { showCPoll = false }) { question, options ->
            showCPoll = false
            sendCWith { token, cid -> QinblyApi.sendCommunityPoll(token, cid, question, options) }
        }
    }
    fun voteC(poll: QinblyApi.ChatPoll, index: Int) {
        val token = prefs.getString("token", null) ?: return
        Thread {
            try {
                val r = QinblyApi.voteChatPoll(token, poll.id, index)
                mainHandler.post { posts = posts.map { q -> if (q.poll?.id == poll.id) q.copy(poll = q.poll?.copy(votes = r.votes, myVote = r.myVote)) else q } }
            } catch (e: Exception) { mainHandler.post { onNotice(e.message ?: "Não foi possível votar.") } }
        }.start()
    }

    fun sendPost(c: QinblyApi.CommunitySummary) {
        val token = prefs.getString("token", null) ?: return
        val body = text.trim()
        if (body.isBlank() || sending) return
        sending = true
        Thread {
            try {
                QinblyApi.addCommunityPost(token, c.id, body)
                mainHandler.post { text = ""; sending = false; showEmoji = false; refresh++ }
            } catch (e: Exception) { mainHandler.post { sending = false; onNotice(e.message ?: "Não foi possível enviar.") } }
        }.start()
    }
    fun toggleJoin(c: QinblyApi.CommunitySummary) {
        val token = prefs.getString("token", null)
        if (token.isNullOrBlank()) { onNotice("Entre na sua conta para participar de comunidades."); return }
        Thread {
            try {
                val (joined, members) = QinblyApi.toggleCommunityJoin(token, c.id)
                mainHandler.post { community = c.copy(joined = joined, members = members); onChanged() }
            } catch (e: Exception) { mainHandler.post { onNotice(e.message ?: "Não foi possível atualizar.") } }
        }.start()
    }
    fun shareCommunity(c: QinblyApi.CommunitySummary) {
        try {
            val share = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, "Entre na comunidade ${c.name} no ${QinblyBrand.Name}!") }
            context.startActivity(Intent.createChooser(share, "Compartilhar comunidade"))
        } catch (_: Exception) { onNotice("Não foi possível abrir o compartilhamento neste aparelho.") }
    }
    BackHandler { if (showInfo) showInfo = false else onDismiss() }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss, properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false, dismissOnClickOutside = false)) {
        Surface(Modifier.fillMaxSize(), color = QiblyTheme.background) {
            val c = community
            if (c == null) {
                Column(Modifier.fillMaxSize().statusBarsPadding().padding(16.dp)) {
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = QiblyTheme.text) }
                    if (loading) Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = QinblyBrand.Accent) }
                    else Text("Comunidade não encontrada.", color = QiblyTheme.secondary, modifier = Modifier.padding(16.dp))
                }
            } else if (showInfo) {
                Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { showInfo = false }) { Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = QiblyTheme.text) }
                        Text("Dados da comunidade", color = QiblyTheme.text, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                    androidx.compose.foundation.lazy.LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp)) {
                        item {
                            Column(Modifier.fillMaxWidth().padding(vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                QinblyCommunityAvatar(c.name, 96.dp)
                                Text(c.name, color = QiblyTheme.text, fontWeight = FontWeight.Bold, fontSize = 22.sp, modifier = Modifier.padding(top = 10.dp))
                                Text("Comunidade · " + (if (c.members == 1) "1 membro" else "${c.members} membros"), color = QiblyTheme.secondary, fontSize = 13.sp)
                                Row(Modifier.padding(top = 14.dp), horizontalArrangement = Arrangement.spacedBy(22.dp)) {
                                    listOf(
                                        Triple(if (mutedFlag) "Reativar" else "Silenciar", if (mutedFlag) Icons.Outlined.NotificationsNone else Icons.Outlined.NotificationsOff, 0),
                                        Triple("Compartilhar", Icons.Outlined.Share, 1)
                                    ).forEach { (label, icon, kind) ->
                                        Column(
                                            Modifier.clip(RoundedCornerShape(14.dp)).clickable {
                                                if (kind == 0) { mutedFlag = !mutedFlag; prefs.edit().putBoolean("community_muted_$communityId", mutedFlag).apply() }
                                                else shareCommunity(c)
                                            }.padding(6.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Box(Modifier.size(46.dp).clip(RoundedCornerShape(14.dp)).background(QinblyBrand.Accent.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                                                Icon(icon, contentDescription = null, tint = QinblyBrand.Accent, modifier = Modifier.size(24.dp))
                                            }
                                            Text(label, color = QinblyBrand.Accent, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }
                            }
                        }
                        if (c.description.isNotBlank()) item {
                            Text("DESCRIÇÃO", color = QiblyTheme.secondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 16.dp, top = 14.dp, bottom = 4.dp))
                            Text(c.description, color = QiblyTheme.text, modifier = Modifier.padding(horizontal = 16.dp))
                        }
                        item {
                            Text((if (c.members == 1) "1 MEMBRO" else "${c.members} MEMBROS"), color = QiblyTheme.secondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 16.dp, top = 18.dp, bottom = 4.dp))
                        }
                        val ownerFirst = (if (c.ownerUsername.isNotBlank()) listOf(c.ownerUsername) else emptyList()) +
                            posts.map { it.username }.filter { it != c.ownerUsername }.distinct()
                        items(ownerFirst) { u ->
                            val p = posts.firstOrNull { it.username == u }
                            Row(Modifier.fillMaxWidth().clickable { QinblyTagNav.profile = u; onDismiss() }.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                QiblyAccountAvatar(u, p?.avatarUrl ?: "", 40.dp) { QinblyTagNav.profile = u; onDismiss() }
                                Text(p?.displayName?.ifBlank { u } ?: "@$u", color = QiblyTheme.text, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 12.dp).weight(1f))
                                if (u == c.ownerUsername) Text("Dono", color = QinblyBrand.Accent, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.border(1.dp, QinblyBrand.Accent, RoundedCornerShape(8.dp)).padding(horizontal = 7.dp, vertical = 2.dp))
                            }
                        }
                        item {
                            Text("A lista completa de membros chega com a atualização do servidor.", color = QiblyTheme.secondary, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                            HorizontalDivider(color = QiblyTheme.border)
                            if (!c.isOwner && c.joined) Row(Modifier.fillMaxWidth().clickable { confirmLeave = true }.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.Logout, contentDescription = null, tint = Color(0xFFD92D20)); Text("Sair da comunidade", color = Color(0xFFD92D20), modifier = Modifier.padding(start = 14.dp))
                            }
                            if (c.isOwner) Row(Modifier.fillMaxWidth().clickable { confirmDelete = true }.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.DeleteOutline, contentDescription = null, tint = Color(0xFFD92D20)); Text("Excluir comunidade", color = Color(0xFFD92D20), modifier = Modifier.padding(start = 14.dp))
                            }
                            Row(Modifier.fillMaxWidth().clickable { showReport = true }.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.Flag, contentDescription = null, tint = QiblyTheme.text); Text("Denunciar comunidade", color = QiblyTheme.text, modifier = Modifier.padding(start = 14.dp))
                            }
                        }
                    }
                }
            } else {
                Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().imePadding()) {
                    // Topo: voltar, foto, nome, membros, menu
                    Row(Modifier.fillMaxWidth().background(QiblyTheme.card).padding(horizontal = 4.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onDismiss) { Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = QiblyTheme.text) }
                        Row(Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).clickable { showInfo = true }.padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                            QinblyCommunityAvatar(c.name, 40.dp)
                            Column(Modifier.padding(start = 10.dp)) {
                                Text(c.name, color = QiblyTheme.text, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                                Text(if (c.members == 1) "1 membro" else "${c.members} membros", color = QiblyTheme.secondary, fontSize = 12.sp)
                            }
                        }
                        Box {
                            IconButton(onClick = { showMenu = true }) { Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = QiblyTheme.text) }
                            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                                DropdownMenuItem(text = { Text("Dados da comunidade") }, leadingIcon = { Icon(Icons.Outlined.Info, null) }, onClick = { showMenu = false; showInfo = true })
                                DropdownMenuItem(text = { Text(if (mutedFlag) "Reativar avisos" else "Silenciar avisos") }, leadingIcon = { Icon(if (mutedFlag) Icons.Outlined.NotificationsNone else Icons.Outlined.NotificationsOff, null) }, onClick = { showMenu = false; mutedFlag = !mutedFlag; prefs.edit().putBoolean("community_muted_$communityId", mutedFlag).apply() })
                                DropdownMenuItem(text = { Text("Limpar conversa") }, leadingIcon = { Icon(Icons.Outlined.DeleteOutline, null) }, onClick = { showMenu = false; confirmClear = true })
                                DropdownMenuItem(text = { Text("Denunciar comunidade") }, leadingIcon = { Icon(Icons.Outlined.Flag, null) }, onClick = { showMenu = false; showReport = true })
                                if (c.isOwner) DropdownMenuItem(text = { Text("Excluir comunidade", color = Color(0xFFD92D20)) }, leadingIcon = { Icon(Icons.Outlined.DeleteOutline, null, tint = Color(0xFFD92D20)) }, onClick = { showMenu = false; confirmDelete = true })
                                else if (c.joined) DropdownMenuItem(text = { Text("Sair da comunidade", color = Color(0xFFD92D20)) }, leadingIcon = { Icon(Icons.Outlined.Logout, null, tint = Color(0xFFD92D20)) }, onClick = { showMenu = false; confirmLeave = true })
                            }
                        }
                    }
                    HorizontalDivider(color = QiblyTheme.border)
                    // Mensagem fixada: a descrição da comunidade
                    if (c.description.isNotBlank()) Row(Modifier.fillMaxWidth().background(QinblyBrand.Accent.copy(alpha = 0.08f)).clickable { showInfo = true }.padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.PushPin, contentDescription = null, tint = QinblyBrand.Accent, modifier = Modifier.size(16.dp))
                        Text(c.description, color = QiblyTheme.text, fontSize = 12.sp, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis, modifier = Modifier.padding(start = 8.dp))
                    }
                    // Conversa
                    Box(Modifier.weight(1f).fillMaxWidth().background(if (QiblyTheme.dark) Color(0xFF0D1119) else Color(0xFFEEF2F8))) {
                        if (chat.isEmpty()) {
                            Text(if (c.joined || c.isOwner) "Ainda não há mensagens. Comece a conversa!" else "Entre na comunidade para ver e enviar mensagens.", color = QiblyTheme.secondary, modifier = Modifier.align(Alignment.Center).padding(24.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        } else {
                            androidx.compose.foundation.lazy.LazyColumn(Modifier.fillMaxSize(), state = listState, contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(chat.size, key = { chat[it].id }) { idx ->
                                    val p = chat[idx]
                                    val day = qinblyDayLabel(p.createdAt)
                                    if (idx == 0 || day != qinblyDayLabel(chat[idx - 1].createdAt)) {
                                        Box(Modifier.fillMaxWidth().padding(vertical = 4.dp), contentAlignment = Alignment.Center) {
                                            Text(day, color = QiblyTheme.secondary, fontSize = 11.sp, modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(QiblyTheme.card).padding(horizontal = 10.dp, vertical = 3.dp))
                                        }
                                    }
                                    val mine = p.mine
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = if (mine) Arrangement.End else Arrangement.Start, verticalAlignment = Alignment.Bottom) {
                                        if (!mine) { QiblyAccountAvatar(p.username, p.avatarUrl, 28.dp) { QinblyTagNav.profile = p.username; onDismiss() }; Spacer(Modifier.width(6.dp)) }
                                        Column(
                                            Modifier.widthIn(max = 280.dp)
                                                .clip(RoundedCornerShape(topStart = if (mine) 14.dp else 4.dp, topEnd = if (mine) 4.dp else 14.dp, bottomStart = 14.dp, bottomEnd = 14.dp))
                                                .background(if (p.stickerUrl.isNotBlank() || QinblyStickers.idOf(p.body) != null) Color.Transparent else if (mine) QinblyBrand.Accent.copy(alpha = if (QiblyTheme.dark) 0.35f else 0.22f) else QiblyTheme.card)
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            if (!mine) Text(p.displayName.ifBlank { p.username } + if (p.username == c.ownerUsername) " · dono" else "", color = qinblyNameColor(p.username), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            val stickerId = QinblyStickers.idOf(p.body)
                                            if (p.poll != null) QinblyChatPollBubble(p.poll, QinblyBrand.Accent, QiblyTheme.text) { index -> voteC(p.poll, index) }
                                            else if (p.stickerUrl.isNotBlank()) AsyncImage(model = p.stickerUrl, contentDescription = "Figurinha", modifier = Modifier.size(120.dp))
                                            else if (p.fileUrl.isNotBlank()) QinblyFileBubble(p.fileUrl, p.fileName, QinblyBrand.Accent, QiblyTheme.text)
                                            else if (p.audioUrl.isNotBlank()) QinblyAudioBubble(p.audioUrl, p.audioSeconds, QinblyBrand.Accent, mine, QiblyTheme.text)
                                            else if (p.imageUrl.isNotBlank()) AsyncImage(model = p.imageUrl, contentDescription = "Foto enviada", modifier = Modifier.width(220.dp).heightIn(max = 320.dp).clip(RoundedCornerShape(12.dp)), contentScale = androidx.compose.ui.layout.ContentScale.Fit)
                                            else if (stickerId != null) QinblyStickerView(stickerId) else QinblyRichText(p.body, QiblyTheme.text)
                                            Row(Modifier.align(Alignment.End), verticalAlignment = Alignment.CenterVertically) {
                                                Text(qinblyClockLabel(p.createdAt), color = QiblyTheme.secondary, fontSize = 10.sp)
                                                if (mine || c.isOwner) Icon(Icons.Outlined.Delete, contentDescription = "Excluir mensagem", tint = QiblyTheme.secondary, modifier = Modifier.padding(start = 6.dp).size(14.dp).clickable {
                                                    val token = prefs.getString("token", null) ?: return@clickable
                                                    posts = posts.filterNot { it.id == p.id }
                                                    Thread {
                                                        try { QinblyApi.deleteCommunityPost(token, c.id, p.id) }
                                                        catch (e: Exception) { mainHandler.post { refresh++; onNotice(e.message ?: "Não foi possível excluir.") } }
                                                    }.start()
                                                })
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (c.joined || c.isOwner) {
                        if (showAttach) {
                            val attachOptions = listOf(
                                Triple("Documento", Icons.Outlined.Description, Color(0xFF5B6CF0)),
                                Triple("Galeria", Icons.Outlined.Image, Color(0xFFA855F7)),
                                Triple("Áudio", Icons.Outlined.Headphones, Color(0xFFF97316)),
                                Triple("Localização", Icons.Outlined.LocationOn, Color(0xFF16A34A)),
                                Triple("Contato", Icons.Outlined.PersonOutline, Color(0xFF0EA5E9)),
                                Triple("Enquete", QinblyCreateIcons.poll, Color(0xFFEAB308))
                            )
                            androidx.compose.foundation.layout.FlowRow(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 4.dp).clip(RoundedCornerShape(18.dp)).background(QiblyTheme.soft).padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly, maxItemsInEachRow = 3
                            ) {
                                attachOptions.forEach { (label, icon, color) ->
                                    Column(Modifier.fillMaxWidth(0.3f).padding(vertical = 6.dp).clip(RoundedCornerShape(12.dp)).clickable { showAttach = false; onCAttach(label) },
                                        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                        Box(Modifier.size(50.dp).clip(CircleShape).background(color), contentAlignment = Alignment.Center) { Icon(icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(25.dp)) }
                                        Text(label, color = QiblyTheme.text, fontSize = 12.sp, fontWeight = FontWeight.Medium, maxLines = 1)
                                    }
                                }
                            }
                        }
                        if (recordingVoiceC) {
                            QinblyVoiceBar(
                                accent = QinblyBrand.Accent, textColor = QiblyTheme.text, barColor = QiblyTheme.soft,
                                onCancel = { recordingVoiceC = false },
                                onTooShort = { recordingVoiceC = false; onNotice("Segure um pouco mais para gravar a mensagem.") },
                                onFailed = { recordingVoiceC = false; onNotice("Não foi possível gravar o áudio neste aparelho.") },
                                onSend = { file, secs ->
                                    recordingVoiceC = false
                                    sendCWith { token, cid ->
                                        try { QinblyApi.sendCommunityVoice(context, token, cid, file, secs) } finally { file.delete() }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp)
                            )
                        } else {
                        Row(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            androidx.compose.material3.TextField(
                                value = text, onValueChange = { text = it.take(2000) }, modifier = Modifier.weight(1f).onFocusChanged { if (it.isFocused) showEmoji = false },
                                placeholder = { Text("Mensagem…", maxLines = 1) }, shape = RoundedCornerShape(26.dp),
                                colors = androidx.compose.material3.TextFieldDefaults.colors(
                                    focusedContainerColor = QiblyTheme.soft, unfocusedContainerColor = QiblyTheme.soft,
                                    focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent, disabledIndicatorColor = Color.Transparent,
                                    cursorColor = QinblyBrand.Accent
                                ),
                                maxLines = 4,
                                leadingIcon = {
                                    IconButton(onClick = { showAttach = false; showEmoji = !showEmoji; if (showEmoji) { cKb?.hide(); cFocus.clearFocus() } else cKb?.show() }, modifier = Modifier.size(44.dp)) {
                                        Icon(QinblyCreateIcons.sticker, contentDescription = "Emojis e figurinhas", tint = if (showEmoji) QinblyBrand.Accent else Muted, modifier = Modifier.size(30.dp))
                                    }
                                },
                                trailingIcon = {
                                    Row(Modifier.padding(end = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(onClick = { showEmoji = false; showAttach = !showAttach }, modifier = Modifier.size(42.dp)) {
                                            Icon(Icons.Outlined.AttachFile, contentDescription = "Anexar", tint = if (showAttach) QinblyBrand.Accent else Muted, modifier = Modifier.size(28.dp))
                                        }
                                        IconButton(onClick = { cCameraShot() }, modifier = Modifier.size(42.dp)) {
                                            Icon(Icons.Outlined.CameraAlt, contentDescription = "Câmera", tint = Muted, modifier = Modifier.size(28.dp))
                                        }
                                    }
                                }
                            )
                            IconButton(
                                onClick = { if (text.isBlank()) startCVoice() else sendPost(c) },
                                modifier = Modifier.size(50.dp).background(QinblyBrand.Accent, CircleShape), enabled = !sending
                            ) {
                                if (text.isBlank()) Icon(Icons.Outlined.Mic, contentDescription = "Gravar áudio", tint = Color.White, modifier = Modifier.size(26.dp))
                                else Icon(Icons.Default.Send, contentDescription = "Enviar mensagem", tint = Color.White, modifier = Modifier.size(25.dp))
                            }
                        }
                        }
                        if (showEmoji) {
                            QinblyStickerGrid(QinblyBrand.Accent, QiblyTheme.card, QiblyTheme.text, cStickerRefresh,
                                onCreate = { showCStickerCreator = true },
                                onPick = { sticker ->
                                    showEmoji = false
                                    sendCWith { token, cid -> QinblyApi.sendCommunitySticker(token, cid, sticker) }
                                }, onNotice = onNotice,
                                modifier = Modifier.fillMaxWidth().height(300.dp).background(QiblyTheme.soft).padding(10.dp))
                        }
                    } else {
                        Button(onClick = { toggleJoin(c) }, shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = QinblyBrand.Accent), modifier = Modifier.fillMaxWidth().padding(12.dp).height(50.dp)) {
                            Text("Entrar na comunidade", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }
            if (confirmDelete && c != null) {
                AlertDialog(
                    shape = RoundedCornerShape(28.dp), containerColor = QiblyTheme.card,
                    onDismissRequest = { confirmDelete = false },
                    title = { Text("Excluir comunidade?") },
                    text = { Text("Todas as publicações e membros serão removidos. Não dá para desfazer.") },
                    confirmButton = {
                        TextButton(onClick = {
                            confirmDelete = false
                            val token = prefs.getString("token", null) ?: return@TextButton
                            Thread {
                                try {
                                    QinblyApi.deleteCommunity(token, c.id)
                                    mainHandler.post { onChanged(); onNotice("Comunidade excluída."); onDismiss() }
                                } catch (e: Exception) { mainHandler.post { onNotice(e.message ?: "Não foi possível excluir.") } }
                            }.start()
                        }) { Text("Excluir", color = Color(0xFFD33B3B)) }
                    },
                    dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancelar") } }
                )
            }
            if (confirmLeave && c != null) {
                AlertDialog(
                    shape = RoundedCornerShape(28.dp), containerColor = QiblyTheme.card,
                    onDismissRequest = { confirmLeave = false },
                    title = { Text("Sair da comunidade?") },
                    text = { Text("Você deixa de ver e enviar mensagens em ${c.name}. Dá para entrar de novo depois.") },
                    confirmButton = { TextButton(onClick = { confirmLeave = false; showInfo = false; toggleJoin(c) }) { Text("Sair", color = Color(0xFFD33B3B)) } },
                    dismissButton = { TextButton(onClick = { confirmLeave = false }) { Text("Cancelar") } }
                )
            }
            if (confirmClear) {
                AlertDialog(
                    shape = RoundedCornerShape(28.dp), containerColor = QiblyTheme.card,
                    onDismissRequest = { confirmClear = false },
                    title = { Text("Limpar conversa?") },
                    text = { Text("Isso apaga as mensagens só da sua tela. Para os outros membros, nada muda.") },
                    confirmButton = { TextButton(onClick = { confirmClear = false; val nowIso = java.time.Instant.now().toString(); clearedAt = nowIso; prefs.edit().putString("community_cleared_$communityId", nowIso).apply() }) { Text("Limpar", color = QinblyBrand.Accent) } },
                    dismissButton = { TextButton(onClick = { confirmClear = false }) { Text("Cancelar") } }
                )
            }
            if (showReport && c != null) {
                ReportReasonDialog(title = "Denunciar comunidade: qual o motivo?", onDismiss = { showReport = false }) { reason ->
                    showReport = false
                    val token = prefs.getString("token", null)
                    if (token.isNullOrBlank()) onNotice("Entre na sua conta para denunciar.")
                    else Thread {
                        try { QinblyApi.reportContent(token, "community", c.id, reason); mainHandler.post { onNotice("Denúncia enviada. Obrigado por avisar.") } }
                        catch (e: Exception) { mainHandler.post { onNotice(e.message ?: "Não foi possível enviar a denúncia.") } }
                    }.start()
                }
            }
        }
    }
}

// ---- Comunidade: denúncia e contas bloqueadas ----
private val reportReasons = listOf(
    "Spam" to "spam",
    "Assédio ou bullying" to "harassment",
    "Discurso de ódio" to "hate",
    "Violência" to "violence",
    "Conteúdo sexual" to "sexual",
    "Risco para menores" to "minor_safety",
    "Automutilação ou suicídio" to "self_harm",
    "Atividade ilegal" to "illegal",
    "Outro motivo" to "other"
)

@Composable
private fun ReportReasonDialog(title: String, onDismiss: () -> Unit, onSelect: (String) -> Unit) {
    AlertDialog(
        shape = RoundedCornerShape(28.dp),
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                reportReasons.forEach { (label, code) ->
                    TextButton(onClick = { onSelect(code) }, modifier = Modifier.fillMaxWidth()) { Text(label, color = QiblyTheme.text) }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar", color = Pink) } },
        containerColor = QiblyTheme.card, titleContentColor = QiblyTheme.text, textContentColor = QiblyTheme.text
    )
}

@Composable
private fun BlockedAccountsDialog(prefs: android.content.SharedPreferences, onDismiss: () -> Unit, onNotice: (String) -> Unit) {
    var blocked by remember { mutableStateOf<List<QinblyApi.BlockedUser>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        val token = prefs.getString("token", null)
        if (token.isNullOrBlank()) loading = false
        else Thread {
            try {
                val result = QinblyApi.getBlockedUsers(token)
                android.os.Handler(android.os.Looper.getMainLooper()).post { blocked = result; loading = false }
            } catch (e: Exception) {
                android.os.Handler(android.os.Looper.getMainLooper()).post { loading = false; onNotice(e.message ?: "Não foi possível carregar as contas bloqueadas.") }
            }
        }.start()
    }
    AlertDialog(
        shape = RoundedCornerShape(28.dp),
        onDismissRequest = onDismiss,
        title = { Text("Contas bloqueadas") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                if (loading) Text("Carregando…")
                else if (blocked.isEmpty()) Text("Você não bloqueou ninguém.")
                else blocked.forEach { user ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(user.displayName.ifBlank { user.username }, fontWeight = FontWeight.SemiBold)
                            Text("@" + user.username, style = MaterialTheme.typography.bodySmall)
                        }
                        TextButton(onClick = {
                            val token = prefs.getString("token", null)
                            if (token.isNullOrBlank()) onNotice("Entre na sua conta para desbloquear.")
                            else Thread {
                                try {
                                    QinblyApi.toggleBlock(token, user.id)
                                    android.os.Handler(android.os.Looper.getMainLooper()).post { blocked = blocked.filterNot { it.id == user.id }; onNotice("Usuário desbloqueado.") }
                                } catch (e: Exception) {
                                    android.os.Handler(android.os.Looper.getMainLooper()).post { onNotice(e.message ?: "Não foi possível desbloquear agora.") }
                                }
                            }.start()
                        }) { Text("Desbloquear", color = Pink) }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Fechar", color = Pink) } },
        containerColor = QiblyTheme.card, titleContentColor = QiblyTheme.text, textContentColor = QiblyTheme.text
    )
}

@Composable
private fun MessagesPage(scrollToTopRequest: Int, recipientUsername: String, onSearch: () -> Unit, onOpenConversation: (String) -> Unit, onOpenProfile: (String) -> Unit, onNotice: (String) -> Unit, onConversationRead: (Int) -> Unit = {}) {
    val readLocally = remember { mutableStateListOf<String>() }
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { context.getSharedPreferences("qinbly_session", android.content.Context.MODE_PRIVATE) }
    val inboxListState = androidx.compose.foundation.lazy.rememberLazyListState()
    LaunchedEffect(scrollToTopRequest) { if (scrollToTopRequest > 0) inboxListState.animateScrollToItem(0) }
    var draft by remember { mutableStateOf("") }
    var showEmojiPicker by remember { mutableStateOf(false) }
    val stickerKb = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current
    val stickerFocus = androidx.compose.ui.platform.LocalFocusManager.current
    var showAttachPanel by remember { mutableStateOf(false) }
    var recordingVoice by remember { mutableStateOf(false) }
    val micPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) recordingVoice = true else onNotice("Permita o microfone para enviar mensagens de voz.")
    }
    var messages by remember { mutableStateOf<List<QinblyApi.MessageSummary>>(emptyList()) }
    var inbox by remember { mutableStateOf(QinblyCache.inbox) }
    var loadingInbox by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var sending by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("Todas") }
    var inboxSearch by remember { mutableStateOf("") }
    var showConversationMenu by remember { mutableStateOf(false) }
    var conversationSearch by remember { mutableStateOf(false) }
    var messageSearch by remember { mutableStateOf("") }
    var showClearConfirm by remember { mutableStateOf(false) }
    var menuMessage by remember { mutableStateOf<QinblyApi.MessageSummary?>(null) }
    var reportingMessageId by remember { mutableStateOf<String?>(null) }
    var showBlockConfirm by remember { mutableStateOf(false) }
    var showReportConfirm by remember { mutableStateOf(false) }
    // Envia algo na conversa (foto, local, contato) e recarrega as mensagens.
    fun sendWith(action: (String) -> Unit) {
        val token = prefs.getString("token", null)
        if (token.isNullOrBlank()) { onNotice("Entre na sua conta para enviar mensagens."); return }
        if (sending) return
        sending = true
        Thread {
            try {
                action(token)
                val loaded = QinblyApi.getMessages(token, recipientUsername)
                android.os.Handler(android.os.Looper.getMainLooper()).post { messages = loaded; sending = false }
            } catch (e: Exception) {
                android.os.Handler(android.os.Looper.getMainLooper()).post { sending = false; onNotice(e.message ?: "Não foi possível enviar.") }
            }
        }.start()
    }
    var showChatPoll by remember { mutableStateOf(false) }
    var showStickerCreator by remember { mutableStateOf(false) }
    var stickerRefresh by remember { mutableIntStateOf(0) }
    var storyTray by remember { mutableStateOf<List<QinblyApi.TrayEntry>>(emptyList()) }
    var storyRefresh by remember { mutableIntStateOf(0) }
    var showStoryMenu by remember { mutableStateOf(false) }
    var viewingStory by remember { mutableStateOf<QinblyApi.TrayEntry?>(null) }
    LaunchedEffect(storyRefresh) {
        val token = prefs.getString("token", null)
        if (!token.isNullOrBlank()) {
            storyTray = try { kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) { QinblyApi.getStoryTray(token) } } catch (_: Exception) { storyTray }
        }
    }
    val myStoryEntry = storyTray.firstOrNull { it.mine }
    val chatDocPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) sendWith { token -> QinblyApi.sendFileMessage(context, token, recipientUsername, uri) }
    }
    val chatGalleryPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) sendWith { token -> QinblyApi.sendImageMessage(context, token, recipientUsername, uri) }
    }
    var pendingChatPhoto by remember { mutableStateOf<java.io.File?>(null) }
    val chatCameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        val f = pendingChatPhoto
        pendingChatPhoto = null
        if (ok && f != null) sendWith { token ->
            try { QinblyApi.sendImageMessage(context, token, recipientUsername, android.net.Uri.fromFile(f)) } finally { f.delete() }
        } else f?.delete()
    }
    fun launchChatCamera() {
        val f = java.io.File(context.cacheDir, "qinbly-chat-" + System.currentTimeMillis() + ".jpg")
        pendingChatPhoto = f
        chatCameraLauncher.launch(androidx.core.content.FileProvider.getUriForFile(context, context.packageName + ".fileprovider", f))
    }
    val chatCameraPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) launchChatCamera() else onNotice("Permita a câmera para tirar a foto.")
    }
    fun chatCameraShot() {
        if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED) launchChatCamera()
        else chatCameraPermission.launch(android.Manifest.permission.CAMERA)
    }
    val chatContactPicker = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val data = result.data?.data ?: return@rememberLauncherForActivityResult
        try {
            context.contentResolver.query(data, arrayOf(android.provider.ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER), null, null, null)?.use { c ->
                if (c.moveToFirst()) {
                    val who = c.getString(0).orEmpty()
                    val number = c.getString(1).orEmpty()
                    sendWith { token -> QinblyApi.sendMessage(token, recipientUsername, "Contato: $who\n$number") }
                }
            }
        } catch (_: Exception) { onNotice("Não foi possível ler o contato escolhido.") }
    }
    fun sendMyLocation() {
        try {
            val lm = context.getSystemService(android.content.Context.LOCATION_SERVICE) as android.location.LocationManager
            val best = lm.getProviders(true).mapNotNull { try { lm.getLastKnownLocation(it) } catch (_: SecurityException) { null } }.maxByOrNull { it.time }
            if (best == null) onNotice("Não consegui pegar sua localização. Ligue o GPS e tente de novo.")
            else sendWith { token -> QinblyApi.sendMessage(token, recipientUsername, "Minha localização:\nhttps://maps.google.com/?q=" + best.latitude + "," + best.longitude) }
        } catch (_: SecurityException) { onNotice("Permita a localização para enviar onde você está.") }
    }
    val chatLocationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { res ->
        if (res.values.any { it }) sendMyLocation() else onNotice("Permita a localização para enviar onde você está.")
    }
    fun onChatAttach(label: String) {
        when (label) {
            "Galeria" -> chatGalleryPicker.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            "Contato" -> chatContactPicker.launch(android.content.Intent(android.content.Intent.ACTION_PICK, android.provider.ContactsContract.CommonDataKinds.Phone.CONTENT_URI))
            "Localização" -> {
                val granted = androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED ||
                    androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
                if (granted) sendMyLocation() else chatLocationPermission.launch(arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION))
            }
            "Áudio" -> { recordingVoice = true }
            "Documento" -> chatDocPicker.launch(arrayOf("application/pdf", "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "application/vnd.ms-powerpoint", "application/vnd.openxmlformats-officedocument.presentationml.presentation", "text/plain", "text/csv", "application/zip"))
            "Enquete" -> showChatPoll = true
            else -> onNotice("$label: ainda não está disponível.")
        }
    }
    if (showStickerCreator) {
        QinblyStickerCreateDialog(QinblyBrand.Accent, onNotice, onDismiss = { showStickerCreator = false }, onCreated = { stickerRefresh++ })
    }
    if (showStoryMenu) {
        QinblyStoryMenu(
            QinblyBrand.Accent, hasStory = myStoryEntry?.stories?.isNotEmpty() == true, hasNote = !myStoryEntry?.note.isNullOrBlank(),
            onDismiss = { showStoryMenu = false }, onNotice = onNotice, onChanged = { storyRefresh++ },
            onViewMine = { viewingStory = myStoryEntry }
        )
    }
    viewingStory?.let { entry ->
        QinblyStoryViewer(entry, QinblyBrand.Accent, onNotice, onDismiss = { viewingStory = null; storyRefresh++ })
    }
    if (showChatPoll) {
        QinblyPollCreateDialog(QinblyBrand.Accent, onDismiss = { showChatPoll = false }) { question, options ->
            showChatPoll = false
            sendWith { token -> QinblyApi.sendPollMessage(token, recipientUsername, question, options) }
        }
    }
    fun voteDm(poll: QinblyApi.ChatPoll, index: Int) {
        val token = prefs.getString("token", null) ?: return
        Thread {
            try {
                val r = QinblyApi.voteChatPoll(token, poll.id, index)
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    messages = messages.map { m -> if (m.poll?.id == poll.id) m.copy(poll = m.poll?.copy(votes = r.votes, myVote = r.myVote)) else m }
                }
            } catch (e: Exception) { android.os.Handler(android.os.Looper.getMainLooper()).post { onNotice(e.message ?: "Não foi possível votar.") } }
        }.start()
    }
    var isMuted by remember(recipientUsername) { mutableStateOf(prefs.getBoolean("muted_$recipientUsername", false)) }
    var isPinned by remember(recipientUsername) { mutableStateOf(prefs.getBoolean("pinned_$recipientUsername", false)) }
    var ownUserId by remember { mutableStateOf(prefs.getString("user_id", "") ?: "") }
    var recipientAvatarUrl by remember(recipientUsername) { mutableStateOf("") }
    val visibleMessages = if (messageSearch.isBlank()) messages else messages.filter { it.body.contains(messageSearch, ignoreCase = true) }
    val filteredInbox = inbox.filter { inboxSearch.isBlank() || it.displayName.contains(inboxSearch, ignoreCase = true) || it.username.contains(inboxSearch, ignoreCase = true) || it.body.contains(inboxSearch, ignoreCase = true) }
    val visibleInbox = when (selectedFilter) {
        "Não lidas" -> filteredInbox.filter { it.unreadCount > 0 }
        // A API atual só retorna conversas individuais; ainda não classifica grupos nem solicitações.
        "Solicitações" -> emptyList()
        else -> filteredInbox
    }

    // Pessoas para seguir: autores do feed que a pessoa ainda não segue (lista fixada ao abrir, para o botão virar "Seguindo" sem sumir).
    var suggestionSource by remember { mutableStateOf(QinblyCache.feed) }
    var moreSuggestions by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (suggestionSource.isEmpty()) {
            try { suggestionSource = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) { QinblyApi.getPosts(prefs.getString("token", null)) } } catch (_: Exception) { }
        }
    }
    val ownHandle = prefs.getString("username", "").orEmpty().removePrefix("@")
    val suggestedPeople = remember(suggestionSource) {
        suggestionSource.filter { it.username.isNotBlank() && !it.username.equals(ownHandle, ignoreCase = true) && !it.username.endsWith(".demo") && !QinblyEngagement.isFollowing(prefs, it.username, it.isFollowing) }
            .distinctBy { it.username.lowercase() }
    }
    fun androidx.compose.foundation.lazy.LazyListScope.suggestionItems() {
        if (suggestedPeople.isEmpty()) return
        item(key = "suggestions-header") {
            Column(Modifier.fillMaxWidth().padding(top = 10.dp)) {
                Box(Modifier.fillMaxWidth().padding(top = 6.dp).height(1.dp).background(RemoteAppearance.feedDividerColor.copy(alpha = 0.45f)))
                Row(Modifier.fillMaxWidth().padding(top = 14.dp, bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Pessoas para seguir", color = RemoteAppearance.screenTextColor, fontWeight = FontWeight.Bold, fontSize = 17.sp, modifier = Modifier.weight(1f))
                    if (suggestedPeople.size > 5) Text(if (moreSuggestions) "Ver menos" else "Ver mais", color = QinblyBrand.Accent, fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable { moreSuggestions = !moreSuggestions }.padding(6.dp))
                }
            }
        }
        items(if (moreSuggestions) suggestedPeople.take(20) else suggestedPeople.take(5), key = { "sug-" + it.username }) { person ->
            Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QiblyAccountAvatar(person.username, person.avatarUrl, 46.dp) { onOpenProfile(person.username) }
                Column(Modifier.weight(1f).clickable { onOpenProfile(person.username) }) {
                    Row(verticalAlignment = Alignment.CenterVertically) { Text(person.displayName.ifBlank { person.username }, color = RemoteAppearance.screenTextColor, fontWeight = FontWeight.Bold, maxLines = 1); if (person.verified) { Spacer(Modifier.width(5.dp)); QinblyVerifiedBadge(15.dp) } }
                    Text("@" + person.username, color = RemoteAppearance.screenSecondaryTextColor, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                }
                QinblyFollowButton(
                    following = QinblyEngagement.isFollowing(prefs, person.username, person.isFollowing),
                    busy = QinblyEngagement.isFollowBusy(person.username),
                    onClick = { QinblyEngagement.toggleFollow(prefs, person.username, person.authorId, person.displayName, onNotice) }
                )
            }
        }
    }

    LaunchedEffect(prefs.getString("token", null)) {
        val profileToken = prefs.getString("token", null)
        if (!profileToken.isNullOrBlank()) {
            try {
                val profileResponse = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) { QinblyApi.getMyProfile(profileToken) }
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
                val publicProfile = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) { QinblyApi.getPublicProfile(recipientUsername) }
                val profileUser = publicProfile.optJSONObject("user") ?: publicProfile.optJSONObject("profile") ?: publicProfile
                recipientAvatarUrl = profileUser.optString("avatarUrl").ifBlank { profileUser.optString("avatar_url") }
            } catch (_: Exception) { }
        }
        messages = emptyList()
        if (recipientUsername.isBlank()) {
            val token = prefs.getString("token", null)
            if (!token.isNullOrBlank()) {
                loadingInbox = inbox.isEmpty()
                var firstInboxLoad = true
                while (true) {
                    try {
                        val loadedInbox = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) { QinblyApi.getInbox(token) }
                        inbox = loadedInbox.distinctBy { it.username }
                        QinblyCache.inbox = inbox
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
                            QinblyApi.getMessages(token, recipientUsername)
                        }
                        val demoMessages = if (recipientUsername.endsWith(".demo")) listOf(
                            QinblyApi.MessageSummary("demo-msg-01", "Oi! Vi suas fotos no ${QinblyBrand.Name} 😊", "demo-user", "Hoje"),
                            QinblyApi.MessageSummary("demo-msg-02", "Obrigada! Estou testando as mensagens por aqui ✨", ownUserId, "Hoje"),
                            QinblyApi.MessageSummary("demo-msg-03", "Ficou ótimo! Depois me manda outra publicação ❤️", "demo-user", "Agora")
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

    Column(Modifier.fillMaxSize().padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = if (WindowInsets.ime.getBottom(androidx.compose.ui.platform.LocalDensity.current) > 0) 0.dp else 10.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (recipientUsername.isBlank()) {
            // Faixa de cima: "Criar" (story ou nota), quem você segue com story/nota e atalhos para quem você conversa.
            val trayOthers = storyTray.filter { !it.mine }
            val trayNames = trayOthers.map { it.username.lowercase() }.toSet()
            androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp), contentPadding = PaddingValues(horizontal = 2.dp)) {
                item(key = "story-create") {
                    val hasMyStory = myStoryEntry?.stories?.isNotEmpty() == true
                    Column(Modifier.width(62.dp).clickable { showStoryMenu = true }, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        QinblyNoteBubble(myStoryEntry?.note.orEmpty())
                        Box(Modifier.size(64.dp).then(if (hasMyStory) Modifier.border(2.dp, Color.Gray.copy(alpha = 0.6f), CircleShape) else Modifier), contentAlignment = Alignment.Center) {
                            Box(Modifier.size(58.dp)) {
                                QiblyAccountAvatar(ownHandle, "", 56.dp) { showStoryMenu = true }
                                Box(Modifier.align(Alignment.BottomEnd).size(20.dp).clip(CircleShape).background(QinblyBrand.Accent).border(2.dp, QiblyTheme.background, CircleShape), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                                }
                            }
                        }
                        Text(if (hasMyStory) "Seu story" else "Criar", color = RemoteAppearance.screenTextColor, fontSize = 12.sp, maxLines = 1)
                    }
                }
                items(trayOthers, key = { "tray-" + it.username }) { person ->
                    val open: () -> Unit = { if (person.stories.isNotEmpty()) { viewingStory = person } else { onOpenConversation(person.username) } }
                    Column(Modifier.width(62.dp).clickable { open() }, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        QinblyNoteBubble(person.note)
                        Box(
                            Modifier.size(64.dp).then(if (person.stories.isNotEmpty()) Modifier.border(2.dp, if (person.hasUnseen) QinblyBrand.Accent else Color.Gray.copy(alpha = 0.6f), CircleShape) else Modifier),
                            contentAlignment = Alignment.Center
                        ) { QiblyAccountAvatar(person.username, person.avatarUrl, 56.dp) { open() } }
                        Text(person.displayName.ifBlank { person.username }, color = RemoteAppearance.screenTextColor, fontSize = 12.sp, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                    }
                }
                items(inbox.filter { it.username.lowercase() !in trayNames }.take(12), key = { "story-" + it.username }) { person ->
                    Column(Modifier.width(62.dp).clickable { onOpenConversation(person.username) }, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        QinblyNoteBubble("")
                        Box(Modifier.size(64.dp), contentAlignment = Alignment.Center) { QiblyAccountAvatar(person.username, person.avatarUrl, 56.dp) { onOpenConversation(person.username) } }
                        Text(person.displayName.ifBlank { person.username }, color = RemoteAppearance.screenTextColor, fontSize = 12.sp, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                listOf("Todas", "Não lidas").forEach { label ->
                    val selected = selectedFilter == label
                    val unreadTotal = if (label == "Não lidas") inbox.count { it.unreadCount > 0 } else 0
                    Surface(
                        modifier = Modifier.clickable { selectedFilter = label },
                        color = if (selected) QinblyBrand.Accent else QinblyBrand.Accent.copy(alpha = 0.10f),
                        shape = RoundedCornerShape(22.dp)
                    ) {
                        Row(Modifier.padding(horizontal = 16.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(label, color = if (selected) Color.White else RemoteAppearance.screenTextColor, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, maxLines = 1)
                            if (unreadTotal > 0) {
                                Spacer(Modifier.width(6.dp))
                                Box(Modifier.size(20.dp).clip(CircleShape).background(if (selected) Color.White else QinblyBrand.Accent), contentAlignment = Alignment.Center) {
                                    Text(if (unreadTotal > 9) "9+" else unreadTotal.toString(), color = if (selected) QinblyBrand.Accent else Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
            when {
                loadingInbox -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Pink) }
                selectedFilter == "Solicitações" -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Default.ChatBubbleOutline, contentDescription = null, tint = Pink, modifier = Modifier.size(36.dp))
                        Text("Nenhuma solicitação", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(
                            "Mensagens de pessoas que você não segue aparecerão aqui.",
                            color = RemoteAppearance.screenSecondaryTextColor, style = MaterialTheme.typography.bodyMedium, textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
                visibleInbox.isNotEmpty() -> LazyColumn(Modifier.fillMaxSize(), state = inboxListState, verticalArrangement = Arrangement.spacedBy(0.dp), contentPadding = PaddingValues(vertical = 4.dp)) {
                    items(visibleInbox, key = { it.username }) { conversation ->
                        val unread = conversation.unreadCount > 0 && conversation.username !in readLocally
                        Row(
                            Modifier.fillMaxWidth().padding(vertical = 3.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(if (unread) QinblyBrand.Accent.copy(alpha = 0.12f) else Color.Transparent)
                                .clickable { if (unread) { readLocally.add(conversation.username); onConversationRead(conversation.unreadCount) }; onOpenConversation(conversation.username) }.padding(horizontal = 10.dp, vertical = 11.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            QiblyAccountAvatar(conversation.username, conversation.avatarUrl, 60.dp) { onOpenProfile(conversation.username) }
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Box(Modifier.weight(1f)) {
                                        Text(conversation.displayName.ifBlank { conversation.username }, color = RemoteAppearance.screenTextColor, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    Text(if (conversation.createdAt.isBlank()) "Agora" else postRelativeTime(conversation.createdAt, System.currentTimeMillis()), color = if (unread) QinblyBrand.Accent else RemoteAppearance.screenSecondaryTextColor, style = MaterialTheme.typography.labelSmall, fontWeight = if (unread) FontWeight.Bold else FontWeight.Normal)
                                }
                                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Box(Modifier.weight(1f)) {
                                        Text((if (QinblyStickers.idOf(conversation.body) != null) "Figurinha" else conversation.body).ifBlank { "@${conversation.username}" }, color = if (unread) RemoteAppearance.screenTextColor else RemoteAppearance.screenSecondaryTextColor, fontWeight = if (unread) FontWeight.SemiBold else FontWeight.Normal, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyMedium)
                                    }
                                    if (unread) Box(
                                        Modifier.padding(start = 8.dp).sizeIn(minWidth = 20.dp, minHeight = 20.dp)
                                            .clip(CircleShape)
                                            .background(QinblyBrand.Accent)
                                            .padding(horizontal = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(if (conversation.unreadCount > 99) "99+" else conversation.unreadCount.toString(), color = Color.White, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, maxLines = 1)
                                    }
                                }
                            }
                        }
                    }
                    if (selectedFilter == "Todas" && inboxSearch.isBlank()) suggestionItems()
                }
                else -> LazyColumn(Modifier.fillMaxSize(), state = inboxListState) { item { Box(Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(Modifier.size(74.dp).clip(CircleShape).background(QinblyBrand.Accent.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.ChatBubbleOutline, contentDescription = null, tint = QinblyBrand.Accent, modifier = Modifier.size(34.dp))
                        }
                        Text(if (selectedFilter == "Não lidas") "Tudo em dia!" else "Suas conversas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(if (selectedFilter == "Não lidas") "Você não tem mensagens não lidas." else "Pesquise pessoas e descubra contas para iniciar uma conversa.", color = RemoteAppearance.screenSecondaryTextColor, style = MaterialTheme.typography.bodyMedium, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        if (selectedFilter != "Não lidas") Button(onClick = onSearch, colors = ButtonDefaults.buttonColors(containerColor = QinblyBrand.Accent), shape = RoundedCornerShape(14.dp)) {
                            Icon(Icons.Default.Search, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Encontrar pessoas")
                        }
                    }
                } }
                    if (selectedFilter == "Todas" && inboxSearch.isBlank()) suggestionItems()
                }
            }
        } else {
            Row(Modifier.fillMaxWidth().padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                IconButton(onClick = { onOpenConversation("") }, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = RemoteAppearance.screenTextColor)
                }
                IconButton(onClick = { onOpenProfile(recipientUsername) }, modifier = Modifier.size(48.dp)) {
                    Box(
                        Modifier.size(42.dp).clip(CircleShape).background(QinblyBrand.Accent).padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (recipientAvatarUrl.isNotBlank()) {
                            AsyncImage(
                                model = recipientAvatarUrl,
                                contentDescription = "Foto de perfil de $recipientUsername",
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
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
                    Icon(Icons.Default.Search, contentDescription = "Pesquisar nesta conversa", tint = RemoteAppearance.screenSecondaryTextColor)
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


            val chatListState = androidx.compose.foundation.lazy.rememberLazyListState()
            LaunchedEffect(visibleMessages.size) { if (visibleMessages.isNotEmpty()) chatListState.scrollToItem(visibleMessages.lastIndex) }
            LazyColumn(Modifier.weight(1f).fillMaxWidth(), state = chatListState, verticalArrangement = Arrangement.spacedBy(6.dp), contentPadding = PaddingValues(horizontal = 4.dp, vertical = 14.dp)) {
                items(visibleMessages.size, key = { visibleMessages[it].id }) { index ->
                    val message = visibleMessages[index]
                    Column(Modifier.fillMaxWidth()) {
                    val zone = java.time.ZoneId.systemDefault()
                    fun dayOf(m: QinblyApi.MessageSummary) = try { java.time.Instant.parse(m.createdAt).atZone(zone).toLocalDate() } catch (_: Exception) { null }
                    val day = dayOf(message)
                    if (day != null && (index == 0 || dayOf(visibleMessages[index - 1]) != day)) {
                        val today = java.time.LocalDate.now(zone)
                        val label = when (day) {
                            today -> "Hoje"
                            today.minusDays(1) -> "Ontem"
                            else -> day.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                        }
                        Box(Modifier.fillMaxWidth().padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                            Text(label, color = RemoteAppearance.screenSecondaryTextColor, style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(QinblyBrand.Accent.copy(alpha = 0.10f)).padding(horizontal = 12.dp, vertical = 4.dp))
                        }
                    }
                    val isOwn = (ownUserId.isNotBlank() && message.senderId == ownUserId) ||
                        message.senderId.equals(prefs.getString("username", "") ?: "", ignoreCase = true)
                    val sameSenderAsPrev = index > 0 && visibleMessages[index - 1].senderId == message.senderId && (day == null || dayOf(visibleMessages[index - 1]) == day)
                    Row(Modifier.fillMaxWidth().padding(top = if (sameSenderAsPrev) 0.dp else 6.dp), horizontalArrangement = if (isOwn) Arrangement.End else Arrangement.Start) {
                        val bubbleShape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = if (isOwn) 18.dp else 4.dp, bottomEnd = if (isOwn) 4.dp else 18.dp)
                        val bubbleColor = if (isOwn) QinblyBrand.Accent else if (QiblyTheme.dark) QiblyTheme.soft else Color(0xFFF0F2F6)
                        val textColor = if (isOwn) Color.White else if (QiblyTheme.dark) Color.White else Color(0xFF111827)
                        Surface(color = if (message.stickerUrl.isNotBlank() || QinblyStickers.idOf(message.body) != null) Color.Transparent else bubbleColor, shape = bubbleShape, modifier = Modifier.widthIn(max = 300.dp).clip(bubbleShape).combinedClickable(onClick = { if (message.stickerUrl.isNotBlank() || QinblyStickers.idOf(message.body) != null) menuMessage = message }, onLongClick = { menuMessage = message })) {
                            Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                                val sharedPostId = remember(message.body) { Regex("post/([0-9a-fA-F-]{36})").find(message.body)?.groupValues?.get(1) }
                                val stickerId = remember(message.body) { QinblyStickers.idOf(message.body) }
                                if (message.poll != null) {
                                    QinblyChatPollBubble(message.poll, QinblyBrand.Accent, textColor) { index -> voteDm(message.poll, index) }
                                } else if (message.stickerUrl.isNotBlank()) {
                                    AsyncImage(model = message.stickerUrl, contentDescription = "Figurinha", modifier = Modifier.size(120.dp))
                                } else if (message.fileUrl.isNotBlank()) {
                                    QinblyFileBubble(message.fileUrl, message.fileName, QinblyBrand.Accent, textColor)
                                } else if (message.imageUrl.isNotBlank()) {
                                    coil.compose.AsyncImage(model = message.imageUrl, contentDescription = "Foto enviada", modifier = Modifier.width(220.dp).heightIn(max = 320.dp).clip(RoundedCornerShape(12.dp)), contentScale = androidx.compose.ui.layout.ContentScale.Fit)
                                } else if (message.audioUrl.isNotBlank()) {
                                    QinblyAudioBubble(message.audioUrl, message.audioSeconds, QinblyBrand.Accent, isOwn, textColor)
                                } else if (stickerId != null) {
                                    QinblyStickerView(stickerId)
                                } else if (sharedPostId == null) {
                                    Text(message.body, color = textColor, style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp, lineHeight = 21.sp))
                                } else {
                                    // Publicação compartilhada: cartão com miniatura, autor e legenda no lugar do link comprido.
                                    val lines = message.body.split("\n").filter { it.isNotBlank() && !it.contains("/post/") }
                                    val intro = Regex("^Confira esta publicação no [^:]*: ?(.*)$")
                                    val introIndex = lines.indexOfFirst { intro.containsMatchIn(it) }
                                    val note = if (introIndex > 0) lines.take(introIndex).joinToString("\n") else ""
                                    val sharedCaption = if (introIndex >= 0) intro.find(lines[introIndex])?.groupValues?.get(1).orEmpty() else ""
                                    val sharedPost = QinblyCache.feed.firstOrNull { it.id == sharedPostId }
                                    if (note.isNotBlank()) Text(note, color = textColor, style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp, lineHeight = 21.sp), modifier = Modifier.padding(bottom = 6.dp))
                                    Column(
                                        Modifier.width(220.dp).clip(RoundedCornerShape(14.dp)).background(QiblyTheme.card)
                                            .clickable { QinblyDeepLink.postId = sharedPostId }
                                    ) {
                                        if (sharedPost != null && sharedPost.mediaUrl.isNotBlank()) {
                                            Box(Modifier.fillMaxWidth().height(120.dp).background(Color.Black)) {
                                                if (sharedPost.mediaType == "video") QinblyVideoPoster(sharedPost.mediaUrl, Modifier.fillMaxSize(), androidx.compose.ui.layout.ContentScale.Crop)
                                                else AsyncImage(model = sharedPost.mediaUrl, contentDescription = "Publicação compartilhada", modifier = Modifier.fillMaxSize(), contentScale = androidx.compose.ui.layout.ContentScale.Crop)
                                            }
                                        }
                                        Column(Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                                            val author = sharedPost?.displayName?.ifBlank { sharedPost.username }.orEmpty()
                                            if (author.isNotBlank()) Text(author, color = QiblyTheme.text, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1)
                                            val cap = sharedPost?.caption?.ifBlank { sharedCaption } ?: sharedCaption
                                            if (cap.isNotBlank()) Text(cap, color = QiblyTheme.secondary, fontSize = 13.sp, maxLines = 2, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                                            Text("Abrir publicação", color = QinblyBrand.Accent, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))
                                        }
                                    }
                                }
                                val timeLabel = try {
                                    java.time.Instant.parse(message.createdAt).atZone(zone).format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
                                } catch (_: Exception) { message.createdAt.take(16).replace("T", " ") }
                                Row(Modifier.align(Alignment.End).padding(top = 3.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(timeLabel, color = textColor.copy(alpha = 0.65f), style = MaterialTheme.typography.labelSmall)
                                    if (isOwn) Icon(Icons.Default.CheckCircle, contentDescription = "Enviada", tint = Color.White.copy(alpha = 0.75f), modifier = Modifier.size(12.dp))
                                }
                            }
                        }
                    }
                    }
                }
            }
            if (showAttachPanel) {
                val attachOptions = listOf(
                    Triple("Documento", Icons.Outlined.Description, Color(0xFF5B6CF0)),
                    Triple("Galeria", Icons.Outlined.Image, Color(0xFFA855F7)),
                    Triple("Áudio", Icons.Outlined.Headphones, Color(0xFFF97316)),
                    Triple("Localização", Icons.Outlined.LocationOn, Color(0xFF16A34A)),
                    Triple("Contato", Icons.Outlined.PersonOutline, Color(0xFF0EA5E9)),
                    Triple("Enquete", QinblyCreateIcons.poll, Color(0xFFEAB308))
                )
                androidx.compose.foundation.layout.FlowRow(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp).clip(RoundedCornerShape(18.dp)).background(QiblyTheme.soft).padding(vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    maxItemsInEachRow = 3
                ) {
                    attachOptions.forEach { (label, icon, color) ->
                        Column(
                            Modifier.fillMaxWidth(0.3f).padding(vertical = 8.dp).clip(RoundedCornerShape(12.dp)).clickable { showAttachPanel = false; onChatAttach(label) },
                            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Box(Modifier.size(52.dp).clip(CircleShape).background(color), contentAlignment = Alignment.Center) { Icon(icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(26.dp)) }
                            Text(label, color = QiblyTheme.text, fontSize = 12.sp, fontWeight = FontWeight.Medium, maxLines = 1)
                        }
                    }
                }
            }
            if (recordingVoice) {
                QinblyVoiceBar(
                    accent = QinblyBrand.Accent, textColor = QiblyTheme.text, barColor = QiblyTheme.soft,
                    onCancel = { recordingVoice = false },
                    onTooShort = { recordingVoice = false; onNotice("Segure um pouco mais para gravar a mensagem.") },
                    onFailed = { recordingVoice = false; onNotice("Não foi possível gravar o áudio neste aparelho.") },
                    onSend = { file, secs ->
                        recordingVoice = false
                        val token = prefs.getString("token", null)
                        if (token.isNullOrBlank()) onNotice("Entre na sua conta para enviar mensagens.")
                        else if (!sending) {
                            sending = true
                            Thread {
                                try {
                                    QinblyApi.sendVoiceMessage(context, token, recipientUsername, file, secs)
                                    val loaded = QinblyApi.getMessages(token, recipientUsername)
                                    android.os.Handler(android.os.Looper.getMainLooper()).post { messages = loaded; sending = false }
                                } catch (e: Exception) {
                                    android.os.Handler(android.os.Looper.getMainLooper()).post { sending = false; onNotice(e.message ?: "Não foi possível enviar a mensagem de voz.") }
                                } finally { file.delete() }
                            }.start()
                        }
                    },
                    modifier = Modifier.padding(bottom = if (WindowInsets.ime.getBottom(androidx.compose.ui.platform.LocalDensity.current) > 0) 4.dp else 16.dp, top = 8.dp)
                )
            } else
            Row(Modifier.fillMaxWidth().padding(bottom = if (showEmojiPicker) 0.dp else if (WindowInsets.ime.getBottom(androidx.compose.ui.platform.LocalDensity.current) > 0) 4.dp else 16.dp, top = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                androidx.compose.material3.TextField(
                    value = draft,
                    onValueChange = { draft = it.take(2000) },
                    modifier = Modifier.weight(1f).onFocusChanged { if (it.isFocused) showEmojiPicker = false },
                    placeholder = { Text("Mensagem…", maxLines = 1) },
                    shape = RoundedCornerShape(26.dp),
                    colors = androidx.compose.material3.TextFieldDefaults.colors(
                        focusedContainerColor = QiblyTheme.soft,
                        unfocusedContainerColor = QiblyTheme.soft,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        cursorColor = QinblyBrand.Accent
                    ),
                    maxLines = 4,
                    leadingIcon = {
                        IconButton(onClick = { showAttachPanel = false; showEmojiPicker = !showEmojiPicker; if (showEmojiPicker) { stickerKb?.hide(); stickerFocus.clearFocus() } else stickerKb?.show() }, modifier = Modifier.size(44.dp)) {
                            Icon(QinblyCreateIcons.sticker, contentDescription = "Emojis e figurinhas", tint = if (showEmojiPicker) QinblyBrand.Accent else Muted, modifier = Modifier.size(30.dp))
                        }
                    },
                    trailingIcon = {
                        Row(Modifier.padding(end = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { showEmojiPicker = false; showAttachPanel = !showAttachPanel }, modifier = Modifier.size(42.dp)) {
                                Icon(Icons.Outlined.AttachFile, contentDescription = "Anexar", tint = if (showAttachPanel) QinblyBrand.Accent else Muted, modifier = Modifier.size(28.dp))
                            }
                            IconButton(onClick = { chatCameraShot() }, modifier = Modifier.size(42.dp)) {
                                Icon(Icons.Outlined.CameraAlt, contentDescription = "Câmera", tint = Muted, modifier = Modifier.size(28.dp))
                            }
                        }
                    }
                )
                IconButton(
                    onClick = {
                        if (draft.isBlank()) {
                            if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) == android.content.pm.PackageManager.PERMISSION_GRANTED) recordingVoice = true
                            else micPermission.launch(android.Manifest.permission.RECORD_AUDIO)
                        }
                        else if (!sending) {
                            val token = prefs.getString("token", null)
                            if (token.isNullOrBlank()) onNotice("Entre na sua conta para enviar mensagens.")
                            else {
                                sending = true
                                val body = draft.trim()
                                Thread {
                                    try {
                                        QinblyApi.sendMessage(token, recipientUsername, body)
                                        val loaded = QinblyApi.getMessages(token, recipientUsername)
                                        android.os.Handler(android.os.Looper.getMainLooper()).post { messages = loaded; draft = ""; showEmojiPicker = false; sending = false }
                                    } catch (e: Exception) {
                                        android.os.Handler(android.os.Looper.getMainLooper()).post { sending = false; onNotice(e.message ?: "Não foi possível enviar a mensagem.") }
                                    }
                                }.start()
                            }
                        }
                    },
                    modifier = Modifier.size(50.dp).background(QinblyBrand.Accent, CircleShape),
                    enabled = !sending
                ) {
                    if (draft.isBlank()) Icon(Icons.Outlined.Mic, contentDescription = "Gravar áudio", tint = Color.White, modifier = Modifier.size(26.dp))
                    else Icon(Icons.Default.Send, contentDescription = "Enviar mensagem", tint = Color.White, modifier = Modifier.size(25.dp))
                }
            }
            if (showEmojiPicker) {
                QinblyStickerGrid(QinblyBrand.Accent, QiblyTheme.card, QiblyTheme.text, stickerRefresh,
                    onCreate = { showStickerCreator = true },
                    onPick = { sticker ->
                        showEmojiPicker = false
                        sendWith { token -> QinblyApi.sendStickerMessage(token, recipientUsername, sticker) }
                    }, onNotice = onNotice,
                    modifier = Modifier.fillMaxWidth().height(300.dp).background(QiblyTheme.soft).padding(10.dp), bleed = 16.dp)
            }
            menuMessage?.let { mm ->
                val own = (ownUserId.isNotBlank() && mm.senderId == ownUserId) || mm.senderId.equals(prefs.getString("username", "") ?: "", ignoreCase = true)
                QinblyMessageSheet(mm, own, QinblyBrand.Accent, QiblyTheme.card, QiblyTheme.text,
                    onDismiss = { menuMessage = null },
                    onCanceled = { id -> messages = messages.filterNot { it.id == id } },
                    onReport = { id -> menuMessage = null; reportingMessageId = id },
                    onSavedSticker = { stickerRefresh++ },
                    onNotice = onNotice)
            }
            reportingMessageId?.let { rid ->
                ReportReasonDialog(title = "Denunciar mensagem: qual o motivo?", onDismiss = { reportingMessageId = null }) { reason ->
                    reportingMessageId = null
                    val token = prefs.getString("token", null)
                    if (token.isNullOrBlank()) onNotice("Entre na sua conta para denunciar.")
                    else Thread {
                        try { QinblyApi.reportContent(token, "message", rid, reason); android.os.Handler(android.os.Looper.getMainLooper()).post { onNotice("Denúncia enviada. Obrigado por ajudar.") } }
                        catch (e: Exception) { android.os.Handler(android.os.Looper.getMainLooper()).post { onNotice(e.message ?: "Não foi possível enviar a denúncia.") } }
                    }.start()
                }
            }
            if (showClearConfirm) {
                AlertDialog(
        shape = RoundedCornerShape(28.dp),
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
                    containerColor = Panel, titleContentColor = QiblyTheme.text, textContentColor = Muted
                )
            }
            if (showBlockConfirm) {
                AlertDialog(
        shape = RoundedCornerShape(28.dp),
                    onDismissRequest = { showBlockConfirm = false },
                    title = { Text("Bloquear usuário?") },
                    text = { Text("Vocês não verão mais as publicações um do outro nem poderão se seguir ou trocar mensagens. Você pode desbloquear em Configurações > Contas bloqueadas.") },
                    confirmButton = {
                        TextButton(onClick = {
                            showBlockConfirm = false
                            val token = prefs.getString("token", null)
                            if (token.isNullOrBlank()) onNotice("Entre na sua conta para bloquear.")
                            else Thread {
                                try {
                                    QinblyApi.blockByUsername(token, recipientUsername)
                                    android.os.Handler(android.os.Looper.getMainLooper()).post { onNotice("Usuário bloqueado.") }
                                } catch (e: Exception) {
                                    android.os.Handler(android.os.Looper.getMainLooper()).post { onNotice(e.message ?: "Não foi possível bloquear agora.") }
                                }
                            }.start()
                        }) { Text("Bloquear", color = Color.Red) }
                    },
                    dismissButton = { TextButton(onClick = { showBlockConfirm = false }) { Text("Cancelar", color = Pink) } },
                    containerColor = QiblyTheme.card, titleContentColor = QiblyTheme.text, textContentColor = QiblyTheme.text
                )
            }
            if (showReportConfirm) {
                ReportReasonDialog(title = "Denunciar usuário: qual o motivo?", onDismiss = { showReportConfirm = false }) { reason ->
                    showReportConfirm = false
                    val token = prefs.getString("token", null)
                    if (token.isNullOrBlank()) onNotice("Entre na sua conta para denunciar.")
                    else Thread {
                        try {
                            QinblyApi.reportUserByUsername(token, recipientUsername, reason)
                            android.os.Handler(android.os.Looper.getMainLooper()).post { onNotice("Denúncia enviada. Obrigado por ajudar a manter a comunidade segura.") }
                        } catch (e: Exception) {
                            android.os.Handler(android.os.Looper.getMainLooper()).post { onNotice(e.message ?: "Não foi possível enviar a denúncia agora.") }
                        }
                    }.start()
                }
            }
        }
    }
}

private val qiblyAvatarRefreshVersion = androidx.compose.runtime.mutableIntStateOf(0)

/**
 * Cache único de fotos de perfil, usado por TODAS as telas (feed, listas, busca, comentários, mensagens).
 * Fica em memória (reativo: todas as fotos atualizam juntas) e no aparelho (já abre com a foto certa ao reabrir o app).
 */
private object QinblyAvatarCache {
    private class Entry(val url: String, val frame: Int, val at: Long, val fetched: Boolean)
    private val entries = mutableStateMapOf<String, Entry>()
    private val fetching = HashSet<String>()
    private var prefs: android.content.SharedPreferences? = null
    private var version = 0
    private const val FRESH_MS = 10 * 60 * 1000L

    fun init(context: android.content.Context) {
        if (prefs != null) return
        val sp = context.applicationContext.getSharedPreferences("qinbly_avatar_cache_v2", android.content.Context.MODE_PRIVATE)
        prefs = sp
        for ((key, value) in sp.all) {
            val raw = value as? String ?: continue
            val parts = raw.split("|", limit = 3)
            if (parts.size == 3) entries[key] = Entry(parts[2], parts[0].toIntOrNull() ?: -1, parts[1].toLongOrNull() ?: 0L, true)
        }
    }

    fun url(key: String): String? = entries[key]?.url?.takeIf { it.isNotBlank() }
    fun frame(key: String): Int = entries[key]?.frame ?: -1
    private fun isFresh(key: String): Boolean = entries[key]?.let { it.fetched && System.currentTimeMillis() - it.at < FRESH_MS } == true

    /** A foto trocou (versão mudou): descarta tudo para buscar de novo. */
    fun checkVersion(v: Int) {
        if (v == version) return
        version = v
        if (v > 0) { entries.clear(); prefs?.edit()?.clear()?.apply() }
    }

    /** Usa a foto que já veio na publicação/lista enquanto o perfil completo não chega. */
    fun seed(key: String, url: String) {
        if (url.isBlank() || entries[key] != null) return
        entries[key] = Entry(url, -1, 0L, false)
    }

    fun needsFetch(key: String): Boolean = !isFresh(key) && fetching.add(key)
    fun endFetch(key: String) { fetching.remove(key) }

    fun put(key: String, url: String, frame: Int) {
        val finalUrl = url.ifBlank { entries[key]?.url.orEmpty() }
        entries[key] = Entry(finalUrl, frame, System.currentTimeMillis(), true)
        prefs?.edit()?.putString(key, "$frame|${System.currentTimeMillis()}|$finalUrl")?.apply()
    }

    fun clear() { entries.clear(); fetching.clear(); prefs?.edit()?.clear()?.apply() }
}

@Composable
private fun QinblyNoteBubble(note: String) {
    // Reserva sempre a mesma altura (20 dp) para os avatares da faixa ficarem alinhados, com ou sem nota.
    Box(Modifier.height(20.dp).fillMaxWidth(), contentAlignment = Alignment.BottomCenter) {
        if (note.isNotBlank()) {
            Text(note, color = RemoteAppearance.screenTextColor, fontSize = 10.sp, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(QinblyBrand.Accent.copy(alpha = 0.14f)).padding(horizontal = 6.dp, vertical = 2.dp))
        }
    }
}

@Composable
private fun QiblyAccountAvatar(username: String, fallbackUrl: String, diameter: Dp, onClick: () -> Unit) {
    val avatarVersion = qiblyAvatarRefreshVersion.intValue
    val avatarContext = androidx.compose.ui.platform.LocalContext.current
    QinblyAvatarCache.init(avatarContext)
    val key = username.removePrefix("@").trim().lowercase()
    // Lê direto do cache compartilhado: a foto já aparece na primeira tela, sem silhueta nem piscada.
    val resolvedUrl = QinblyAvatarCache.url(key) ?: fallbackUrl
    LaunchedEffect(key, avatarVersion) {
        QinblyAvatarCache.checkVersion(avatarVersion)
        QinblyAvatarCache.seed(key, fallbackUrl)
        if (key.isNotBlank() && !key.contains(".demo") && QinblyAvatarCache.needsFetch(key)) {
            try {
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    val result = QinblyApi.getPublicProfile(username)
                    val profile = result.optJSONObject("user") ?: result.optJSONObject("profile") ?: result
                    val image = profile.optString("avatarUrl").ifBlank { profile.optString("avatar_url") }.ifBlank { profile.optString("profilePhotoUrl") }
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) { QinblyAvatarCache.put(key, image, -1) }
                }
            } catch (_: Exception) {
            } finally {
                QinblyAvatarCache.endFetch(key)
            }
        }
    }
    Box(Modifier.size(diameter).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Box(Modifier.size(diameter).clip(CircleShape).background(QiblyTheme.card)) {
            if (resolvedUrl.isNotBlank()) AsyncImage(model = coil.request.ImageRequest.Builder(avatarContext).data(resolvedUrl).memoryCacheKey("avatar:$username:$avatarVersion:$resolvedUrl").diskCacheKey("avatar:$username:$avatarVersion:$resolvedUrl").build(), contentDescription = "Foto de @$username", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            else Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.fillMaxSize().padding(8.dp))
        }
    }
}

@Composable
private fun ProfilePage(scrollToTopRequest: Int, username: String, displayName: String, bio: String, photoRefreshKey: Int, onNotice: (String) -> Unit, onStat: (String) -> Unit, onAuth: () -> Unit, onEdit: () -> Unit, onEditProfilePhoto: () -> Unit, onSettings: () -> Unit) {
    val profileScrollState = rememberScrollState()
    LaunchedEffect(scrollToTopRequest) { if (scrollToTopRequest > 0) profileScrollState.animateScrollTo(0) }
    val snapContext = androidx.compose.ui.platform.LocalContext.current
    // Estado inicial = último perfil carregado (memória) ou, ao abrir o app, o último salvo no aparelho. Evita foto/contadores piscando.
    val snap = remember(username) {
        QinblyCache.own?.takeIf { it.username.equals(username, ignoreCase = true) } ?: run {
            val sp = snapContext.getSharedPreferences("qinbly_profile_snapshot", android.content.Context.MODE_PRIVATE)
            if (sp.getString("username", "").orEmpty().equals(username, ignoreCase = true) && username.isNotBlank()) OwnProfileSnapshot(
                username, sp.getInt("posts", 0), sp.getInt("followers", 0), sp.getInt("following", 0),
                sp.getString("avatar", "").orEmpty(), sp.getString("photo", "").orEmpty(), sp.getString("cover", "").orEmpty(),
                sp.getInt("frame", -1), sp.getString("joined", "").orEmpty(), emptyList()
            ) else null
        }
    }
    var postsCount by remember { mutableIntStateOf(snap?.postsCount ?: 0) }
    var followersCount by remember { mutableIntStateOf(snap?.followers ?: 0) }
    var followingCount by remember { mutableIntStateOf(snap?.following ?: 0) }
    var profilePosts by remember { mutableStateOf<List<QinblyApi.PostSummary>>(snap?.posts ?: emptyList()) }
    var savedPosts by remember { mutableStateOf<List<QinblyApi.PostSummary>>(emptyList()) }
    var loadingSaved by remember { mutableStateOf(false) }
    var reloadKey by remember { mutableIntStateOf(0) }
    var profileTab by remember { mutableStateOf("Publicações") }
    var loadingPosts by remember { mutableStateOf(snap == null || snap.posts.isEmpty()) }
    var avatarUrl by remember { mutableStateOf(snap?.avatarUrl ?: "") }
    var profilePhotoUrl by remember { mutableStateOf(snap?.profilePhotoUrl ?: "") }
    var profileVerified by remember { mutableStateOf(false) }
    var originalCoverUrl by remember { mutableStateOf(snap?.originalCoverUrl ?: "") }
    var expandedImage by remember { mutableStateOf("") }
    var photoMenuOpen by remember { mutableStateOf(false) }
    var viewerPostId by remember { mutableStateOf<String?>(null) }
    var selectedPublicationCaption by remember { mutableStateOf("") }
    var selectedPublication by remember { mutableStateOf<QinblyApi.PostSummary?>(null) }
    var commentsOpen by remember { mutableStateOf(false) }
    var sharePost by remember { mutableStateOf<QinblyApi.PostSummary?>(null) }

    var publicationComment by remember { mutableStateOf("") }
    var joinedMonthYear by remember { mutableStateOf(snap?.joined ?: "") }

    val shownName = displayName.ifBlank { username }
    val shownBio = bio.lineSequence().filterNot { it.startsWith("🌍 ") }.joinToString("\n").trim()
    val shownCountry = bio.lineSequence().firstOrNull { it.startsWith("🌍 ") }?.removePrefix("🌍 ").orEmpty()
    val profileContext = androidx.compose.ui.platform.LocalContext.current
    val profilePrefs = remember { profileContext.getSharedPreferences("qinbly_session", android.content.Context.MODE_PRIVATE) }
    LaunchedEffect(profileTab, reloadKey) {
        if (profileTab == "Salvos") {
            val token = profilePrefs.getString("token", null)
            if (token.isNullOrBlank()) { savedPosts = emptyList() } else {
                loadingSaved = true
                Thread {
                    try {
                        val loaded = QinblyApi.getSavedPosts(token)
                        android.os.Handler(android.os.Looper.getMainLooper()).post { QinblyEngagement.learnFromPosts(profilePrefs, loaded); savedPosts = loaded; loadingSaved = false }
                    } catch (e: Exception) {
                        android.os.Handler(android.os.Looper.getMainLooper()).post { loadingSaved = false; onNotice(e.message ?: "Não foi possível carregar seus salvos.") }
                    }
                }.start()
            }
        }
    }
    val openProfileComments: (QinblyApi.PostSummary) -> Unit = { target ->
        selectedPublication = target
        commentsOpen = true
    }

    LaunchedEffect(username, photoRefreshKey, reloadKey) {
        loadingPosts = profilePosts.isEmpty()
        if (username.isNotBlank()) {
            Thread {
                try {
                    val token = profilePrefs.getString("token", "") ?: ""
                    val response = QinblyApi.getPublicProfile(username, token.takeIf { it.isNotBlank() })
                    val user = response.optJSONObject("user") ?: response.optJSONObject("profile") ?: response
                    // Falha no feed de publicações não pode impedir o carregamento da capa do perfil.
                    val loadedPosts = QinblyApi.parsePosts(response.optJSONArray("posts"), user).ifEmpty {
                        // Servidor antigo (sem publicações no perfil): mantém o caminho anterior.
                        try {
                            QinblyApi.getPosts(profilePrefs.getString("token", null)).filter { it.username.equals(username, ignoreCase = true) }
                        } catch (_: Exception) { emptyList() }
                    }
                    android.os.Handler(android.os.Looper.getMainLooper()).post {
                        if (user != null) {
                            val created = listOf("created_at", "createdAt", "joined_at").map { user.optString(it) }.firstOrNull { it.matches(Regex("\\d{4}-\\d{2}.*")) }.orEmpty()
                            joinedMonthYear = if (created.length >= 7) {
                                val month = created.substring(5, 7).toIntOrNull() ?: 0
                                val months = listOf("janeiro", "fevereiro", "março", "abril", "maio", "junho", "julho", "agosto", "setembro", "outubro", "novembro", "dezembro")
                                if (month in 1..12) "${months[month - 1]} de ${created.substring(0, 4)}" else ""
                            } else ""
                            profileVerified = user.optBoolean("verified")
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
                        val fresh = OwnProfileSnapshot(username, postsCount, followersCount, followingCount, avatarUrl, profilePhotoUrl, originalCoverUrl, -1, joinedMonthYear, loadedPosts)
                        QinblyCache.own = fresh
                        snapContext.getSharedPreferences("qinbly_profile_snapshot", android.content.Context.MODE_PRIVATE).edit()
                            .putString("username", username).putInt("posts", postsCount).putInt("followers", followersCount).putInt("following", followingCount)
                            .putString("avatar", avatarUrl).putString("photo", profilePhotoUrl).putString("cover", originalCoverUrl)
                            .putInt("frame", -1).putString("joined", joinedMonthYear).apply()
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

    val profileAccent = QinblyBrand.Accent
    Column(Modifier.fillMaxSize().background(RemoteAppearance.screenBackgroundColor).verticalScroll(profileScrollState), horizontalAlignment = Alignment.CenterHorizontally) {
        // Capa real e independente: sem retângulo em volta dos dados do perfil.
        Box(
            Modifier.fillMaxWidth().height(252.dp)
                .background(RemoteAppearance.screenBackgroundColor)
        ) {
            IconButton(onClick = onSettings, modifier = Modifier.align(Alignment.TopEnd).padding(top = 8.dp, end = 12.dp)) {
                Icon(Icons.Default.Menu, contentDescription = "Configurações do perfil", tint = RemoteAppearance.screenTextColor)
            }
            // Retrato centralizado na capa.
            Box(Modifier.align(Alignment.BottomCenter).size(225.dp), contentAlignment = Alignment.Center) {
                Box(Modifier.size(205.dp).zIndex(0f).clip(CircleShape).background(QiblyTheme.card).clickable {
                    val image = originalCoverUrl.ifBlank { profilePhotoUrl.ifBlank { avatarUrl } }
                    if (image.isNotBlank()) { expandedImage = image } else onEditProfilePhoto()
                }) {
                    val image = profilePhotoUrl.ifBlank { avatarUrl }
                    if (image.isNotBlank()) AsyncImage(model = image, contentDescription = "Foto do perfil", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    else Icon(Icons.Default.Person, contentDescription = "Foto do perfil", tint = RemoteAppearance.screenSecondaryTextColor, modifier = Modifier.fillMaxSize().padding(28.dp))
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        // Cartão de identidade separado da capa.
        Column(
            Modifier.fillMaxWidth().padding(top = 15.dp, bottom = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
        Row(Modifier.padding(top = 10.dp).clickable { onEdit() }, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            Text(shownName.ifBlank { if (username.isBlank()) "Seu nome" else username }, color = RemoteAppearance.screenTextColor, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, maxLines = 1)
            if (profileVerified) QinblyVerifiedBadge(22.dp)
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
            ProfileStat("Publicações", profilePosts.size.toString(), Icons.Default.GridView) { }
            ProfileStat("Seguindo", followingCount.toString(), Icons.Default.PersonAddAlt1) { onStat("Seguindo") }
            ProfileStat("Seguidores", followersCount.toString(), Icons.Default.Groups) { onStat("Seguidores") }
        }
        Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = onEdit, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(17.dp))
                Spacer(Modifier.width(6.dp))
                Text("Editar perfil")
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
            val shownSaved = savedPosts.filter { QinblyEngagement.isSaved(it) }
            if (loadingSaved && savedPosts.isEmpty()) {
                Box(Modifier.fillMaxWidth().padding(22.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Pink) }
            } else if (shownSaved.isEmpty()) {
                Box(Modifier.fillMaxWidth().padding(22.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.BookmarkBorder, contentDescription = null, tint = Violet, modifier = Modifier.size(38.dp))
                        Text("Nenhuma publicação salva", fontWeight = FontWeight.Bold, color = RemoteAppearance.screenTextColor)
                        Text("Toque no marcador de uma publicação para guardá-la aqui. Só você vê os seus salvos.", color = RemoteAppearance.screenSecondaryTextColor, style = MaterialTheme.typography.bodySmall)
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    shownSaved.forEach { post ->
                        QinblyPostCard(
                            post = post,
                            prefs = profilePrefs,
                            nameFontStyle = "",
                            onNotice = onNotice,
                            onOpenProfile = { },
                            onOpenViewer = { viewerPostId = it.id },
                            onComment = { openProfileComments(it) },
                            onShare = { sharePost = it },
                            onPostsTransform = { change -> savedPosts = change(savedPosts) },
                            onRefresh = { reloadKey++ }
                        )
                    }
                }
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
            val visiblePosts = profilePosts
                .filter { profileTab != "Vídeos" || it.mediaType == "video" }
                .filter { profileTab != "Fotos" || it.mediaType == "image" }
                .sortedByDescending { it.pinned }
            if (profileTab == "Publicações" || profileTab == "Vídeos") {
                // Lista vertical de publicações mistas; "Vídeos" usa o mesmo cartão/player da tela de início, sem modificar o cabeçalho ou as demais abas.
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    visiblePosts.forEach { post ->
                        QinblyPostCard(
                            post = post,
                            prefs = profilePrefs,
                            nameFontStyle = "",
                            onNotice = onNotice,
                            onOpenProfile = { },
                            onOpenViewer = { viewerPostId = it.id },
                            onComment = { openProfileComments(it) },
                            onShare = { sharePost = it },
                            onPostsTransform = { change -> profilePosts = change(profilePosts); postsCount = profilePosts.size },
                            onRefresh = { reloadKey++ }
                        )
                    }
                }
            } else {
                LazyVerticalGrid(columns = GridCells.Fixed(3), modifier = Modifier.fillMaxWidth().heightIn(max = 560.dp), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(visiblePosts.size) { index ->
                        val post = visiblePosts[index]
                        Box(Modifier.fillMaxWidth().height(145.dp).clip(RoundedCornerShape(8.dp)).background(qinblyPlaceholderColor).clickable { if (post.mediaUrl.isNotBlank()) viewerPostId = post.id }) {
                            if (post.mediaUrl.isNotBlank()) AsyncImage(model = post.mediaUrl, contentDescription = "Publicação", modifier = Modifier.fillMaxSize(), contentScale = androidx.compose.ui.layout.ContentScale.Crop)
                            else Text(post.caption.ifBlank { QinblyBrand.Name }, color = RemoteAppearance.screenTextColor, modifier = Modifier.align(Alignment.Center).padding(8.dp), maxLines = 4)
                            if (post.mediaType == "video") Icon(Icons.Default.PlayCircle, contentDescription = "Vídeo", tint = RemoteAppearance.screenTextColor, modifier = Modifier.align(Alignment.TopEnd).padding(5.dp))
                        }
                    }
                }
            }
        }
    }

    if (expandedImage.isNotBlank()) QinblyPhotoViewer(expandedImage, onNotice, onDismiss = { expandedImage = "" })

    viewerPostId?.let { startId ->
        val viewable = qinblyViewerList((if (profileTab == "Salvos") savedPosts else profilePosts).filter { it.mediaType != "poll" && (it.mediaUrl.isNotBlank() || it.mediaUrls.isNotEmpty()) }, startId)
        if (viewable.any { it.id == startId }) {
            QinblyPostViewer(
                posts = viewable,
                startPostId = startId,
                prefs = profilePrefs,
                currentUsername = username,
                onNotice = onNotice,
                onDismiss = { viewerPostId = null },
                onComment = { openProfileComments(it) },
                onShare = { sharePost = it },
                onOpenProfile = { viewerPostId = null }
            )
        }
    }
    sharePost?.let { target ->
        QiblyShareDialog(post = target, prefs = profilePrefs, onDismiss = { sharePost = null }, onNotice = onNotice)
    }
    if (commentsOpen) {
        selectedPublication?.let { target ->
            QinblyCommentsDialog(
                post = target,
                prefs = profilePrefs,
                currentUsername = username,
                onNotice = onNotice,
                onDismiss = { commentsOpen = false },
                onCountChanged = { postId, count ->
                    profilePosts = profilePosts.map { if (it.id == postId && it.comments != count) it.copy(comments = count) else it }
                    savedPosts = savedPosts.map { if (it.id == postId && it.comments != count) it.copy(comments = count) else it }
                    if (selectedPublication?.id == postId) selectedPublication = selectedPublication?.copy(comments = count)
                }
            )
        }
    }

}
@Composable
private fun AuthPage(onSuccess: (String) -> Unit, onCancel: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { context.getSharedPreferences("qinbly_session", android.content.Context.MODE_PRIVATE) }
    var createAccount by remember { mutableStateOf(false) }
    var showForgot by remember { mutableStateOf(false) }
    var forgotEmail by remember { mutableStateOf("") }
    var forgotBusy by remember { mutableStateOf(false) }
    var forgotMessage by remember { mutableStateOf("") }
    if (showForgot) {
        AlertDialog(
        shape = RoundedCornerShape(28.dp),
        containerColor = QiblyTheme.card,
            onDismissRequest = { if (!forgotBusy) showForgot = false },
            title = { Text("Recuperar senha") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Digite o e-mail da sua conta. Vamos enviar um link para criar uma nova senha.")
                    OutlinedTextField(value = forgotEmail, onValueChange = { forgotEmail = it.take(120) }, singleLine = true, label = { Text("E-mail") }, modifier = Modifier.fillMaxWidth())
                    if (forgotMessage.isNotBlank()) Text(forgotMessage, color = Violet)
                }
            },
            confirmButton = {
                TextButton(enabled = !forgotBusy, onClick = {
                    val mail = forgotEmail.trim().lowercase()
                    if (!mail.contains("@") || !mail.substringAfter("@", "").contains(".")) { forgotMessage = "Digite um e-mail válido."; return@TextButton }
                    forgotBusy = true
                    forgotMessage = ""
                    Thread {
                        val msg = try { QinblyApi.forgotPassword(mail) } catch (e: Exception) { e.message ?: "Não foi possível enviar agora." }
                        android.os.Handler(android.os.Looper.getMainLooper()).post { forgotBusy = false; forgotMessage = msg }
                    }.start()
                }) { Text(if (forgotBusy) "Enviando…" else "Enviar link") }
            },
            dismissButton = { TextButton(enabled = !forgotBusy, onClick = { showForgot = false }) { Text("Fechar") } }
        )
    }
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
                        QinblyApi.authenticateWithGoogle(idToken)
                    }
                    prefs.edit().putString("token", auth.token).putString("refresh_token", auth.refreshToken).putString("username", auth.username).apply()
                    rememberQinblyAccount(prefs, auth.username, auth.token, prefs.getString("display_name", "") ?: "", prefs.getString("bio", "") ?: "")
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
                Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = QiblyTheme.text)
            }
            Spacer(Modifier.width(4.dp))
            Text("Acesso à conta", color = Muted, style = MaterialTheme.typography.titleSmall)
        }

        Column(Modifier.fillMaxWidth().padding(top = 2.dp, bottom = 4.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                Modifier.size(76.dp).clip(RoundedCornerShape(24.dp)).background(Brush.linearGradient(listOf(Pink, Violet))),
                contentAlignment = Alignment.Center
            ) {
                Text("V", color = QiblyTheme.text, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Black)
            }
            Text(QinblyBrand.Name, color = QiblyTheme.text, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
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
                Text(if (createAccount) "Criar conta" else "Bem-vindo de volta", color = QiblyTheme.text, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
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
                    if (loading) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = QiblyTheme.text)
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
                            focusedBorderColor = Pink, unfocusedBorderColor = QiblyTheme.border,
                            focusedLabelColor = Pink, cursorColor = Pink, focusedTextColor = QiblyTheme.text,
                            unfocusedTextColor = QiblyTheme.text, focusedContainerColor = Night, unfocusedContainerColor = Night
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
                        focusedBorderColor = Pink, unfocusedBorderColor = QiblyTheme.border,
                        focusedLabelColor = Pink, cursorColor = Pink, focusedTextColor = QiblyTheme.text,
                        unfocusedTextColor = QiblyTheme.text, focusedContainerColor = Night, unfocusedContainerColor = Night
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
                        focusedBorderColor = Pink, unfocusedBorderColor = QiblyTheme.border,
                        focusedLabelColor = Pink, cursorColor = Pink, focusedTextColor = QiblyTheme.text,
                        unfocusedTextColor = QiblyTheme.text, focusedContainerColor = Night, unfocusedContainerColor = Night
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
                                        val result = QinblyApi.authenticate(createAccount, username, normalizedEmail, password)
                                        prefs.edit().putString("token", result.token).putString("refresh_token", result.refreshToken).putString("username", result.username).apply()
                                        rememberQinblyAccount(prefs, result.username, result.token, prefs.getString("display_name", "") ?: "", prefs.getString("bio", "") ?: "")
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
                    if (loading) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = QiblyTheme.text)
                    else Text(if (createAccount) "Criar minha conta" else "Entrar", fontWeight = FontWeight.Bold)
                }

                if (!createAccount) TextButton(
                    onClick = { forgotEmail = email.trim(); forgotMessage = ""; showForgot = true },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !loading
                ) { Text("Esqueci minha senha", color = Muted) }
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

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun EditProfilePage(initialUsername: String, initialName: String, initialBio: String, photoRefreshKey: Int, onEditProfilePhoto: () -> Unit, onBack: () -> Unit, onSaved: (String, String, String) -> Unit, onNotice: (String) -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { context.getSharedPreferences("qinbly_session", android.content.Context.MODE_PRIVATE) }
    var editPhotoUrl by remember { mutableStateOf("") }
    LaunchedEffect(initialUsername, photoRefreshKey) {
        if (initialUsername.isNotBlank()) {
            Thread {
                try {
                    val token = prefs.getString("token", "").orEmpty()
                    val response = if (token.isNotBlank()) {
                        try { QinblyApi.getMyProfile(token) } catch (_: Exception) { QinblyApi.getPublicProfile(initialUsername) }
                    } else QinblyApi.getPublicProfile(initialUsername)
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
                        val user = QinblyApi.getPublicProfile(initialUsername).let { it.optJSONObject("user") ?: it.optJSONObject("profile") ?: it }
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
    // "" = não verificado, "checking", "free" (livre) ou "taken" (já existe outro perfil com esse @usuário).
    var usernameStatus by remember { mutableStateOf("") }
    LaunchedEffect(username) {
        val wanted = username.trim()
        if (wanted.equals(initialUsername, ignoreCase = true) || !wanted.matches(Regex("[a-z0-9_.]{3,24}"))) { usernameStatus = ""; return@LaunchedEffect }
        usernameStatus = "checking"
        kotlinx.coroutines.delay(500)
        val free = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) { runCatching { QinblyApi.isUsernameAvailable(wanted) }.getOrNull() }
        usernameStatus = when (free) { true -> "free"; false -> "taken"; null -> "" }
    }
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
                    else if (usernameStatus == "taken") error = "Esse nome de usuário já está em uso. Escolha outro."
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
                                QinblyApi.updateProfile(token, username.trim(), name.trim(), composedBio)
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
            IconButton(onClick = onBack, enabled = !loading) { Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = QiblyTheme.text) }
            Text("Editar perfil", modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = QiblyTheme.text)
            Button(
                onClick = saveProfile,
                enabled = !loading,
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = QinblyBrand.Accent, contentColor = Color.White),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 9.dp)
            ) {
                if (loading) CircularProgressIndicator(Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp) else Text("Salvar", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        Column(Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
            Text("Alterar foto", color = QinblyBrand.Accent, fontWeight = FontWeight.Medium, fontSize = 14.sp,
                modifier = Modifier.fillMaxWidth().clickable { onEditProfilePhoto() }, textAlign = androidx.compose.ui.text.style.TextAlign.Center)

            Text("SOBRE VOCÊ", color = Muted, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)

            @Composable
            fun editCard(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String, hint: String, onValue: (String) -> Unit, singleLine: Boolean = true, max: Int = 80, centered: Boolean = false) {
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                        .border(BorderStroke(1.dp, QiblyTheme.border), RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp, vertical = if (singleLine) 10.dp else 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(label, color = Muted, style = MaterialTheme.typography.labelMedium)
                        androidx.compose.foundation.text.BasicTextField(
                            value = value,
                            onValueChange = { onValue(it.take(max)) },
                            enabled = !loading,
                            singleLine = singleLine,
                            textStyle = androidx.compose.ui.text.TextStyle(color = RemoteAppearance.screenTextColor, fontSize = 16.sp, textAlign = if (centered) androidx.compose.ui.text.style.TextAlign.Center else androidx.compose.ui.text.style.TextAlign.Start),
                            cursorBrush = androidx.compose.ui.graphics.SolidColor(QinblyBrand.Accent),
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
                }
            }

            editCard(Icons.Default.Person, "Nome", name, "Seu nome", { name = it }, max = 80)
            editCard(Icons.Default.AlternateEmail, "Nome de usuário", username, "seuusuario", { username = it.lowercase().filter { ch -> ch.isLetterOrDigit() || ch == '_' || ch == '.' }.take(24) }, max = 24)
            when (usernameStatus) {
                "checking" -> Text("Verificando…", color = Muted, style = MaterialTheme.typography.bodySmall)
                "free" -> Text("✓ @$username está disponível", color = Color(0xFF1E8E3E), style = MaterialTheme.typography.bodySmall)
                "taken" -> Text("Esse nome de usuário já está em uso. Escolha outro.", color = Color(0xFFD93025), style = MaterialTheme.typography.bodySmall)
            }
            editCard(Icons.Default.Description, "Bio", bio, "Escreva uma breve descrição sobre você", { bio = it }, singleLine = false, max = 300)
            Box {
                OutlinedButton(onClick = { countryMenuExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Language, contentDescription = null, tint = QinblyBrand.Accent)
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
            QinblyVerificationCard(prefs.getString("token", "").orEmpty(), onNotice)
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("CATEGORIAS", color = Muted, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1f))
                Text("${selectedCategories.size} de 5", color = QinblyBrand.Accent, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.labelMedium)
            }
            Text("Escolha até 5 categorias para aparecerem abaixo do seu nome.", color = RemoteAppearance.screenSecondaryTextColor, style = MaterialTheme.typography.bodySmall)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                availableCategories.forEach { category ->
                    val selected = category in selectedCategories
                    androidx.compose.material3.FilterChip(
                        selected = selected,
                        onClick = { selectedCategories = if (selected) selectedCategories - category else if (selectedCategories.size < 5) selectedCategories + category else selectedCategories },
                        label = { Text(category.replace(Regex("\\s*[^\\p{L}\\p{N}\\s]+\\s*$"), ""), color = if (selected) Color.White else RemoteAppearance.screenTextColor) },
                        shape = RoundedCornerShape(16.dp),
                        colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(selectedContainerColor = QinblyBrand.Accent, containerColor = QiblyTheme.card)
                    )
                }
            }
            if (error.isNotBlank()) Text(error, color = Pink, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun EditProfilePhotoPage(onBack: () -> Unit, onSaved: () -> Unit, onNotice: (String) -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { context.getSharedPreferences("qinbly_session", android.content.Context.MODE_PRIVATE) }
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
                Icon(Icons.Default.ArrowBack, contentDescription = "Cancelar e voltar", tint = QiblyTheme.text)
            }
            Text(
                "Arraste para ajustar",
                modifier = Modifier.weight(1f),
                color = QiblyTheme.text,
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
                Icon(Icons.Default.CheckCircle, contentDescription = "Confirmar ajuste", tint = QiblyTheme.text)
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
                    Text("Escolha uma foto para ajustar", color = QiblyTheme.text, fontWeight = FontWeight.Medium)
                    OutlinedButton(onClick = { profilePhotoPicker.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }) {
                        Text("Escolher imagem", color = QiblyTheme.text)
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
                                QinblyApi.uploadProfileImage(context, token, uri, "cover_original")
                                // A capa visível no perfil continua usando exatamente o enquadramento escolhido.
                                QinblyApi.uploadProfileImage(context, token, Uri.fromFile(outFile), "cover")
                                // Existe UMA só foto de perfil: o servidor usa esta mesma imagem em todas as telas e publicações.
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
                colors = ButtonDefaults.buttonColors(containerColor = QinblyBrand.Accent, contentColor = Color.White)
            ) {
                if (loading) CircularProgressIndicator(Modifier.size(18.dp), color = QiblyTheme.text, strokeWidth = 2.dp)
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
    var autoplay by remember { mutableStateOf(prefs.getBoolean("setting_autoplay", true)) }
    var allowComments by remember { mutableStateOf(prefs.getBoolean("setting_comments", true)) }
    var likesNotice by remember { mutableStateOf(prefs.getBoolean("setting_notice_likes", true)) }
    var commentsNotice by remember { mutableStateOf(prefs.getBoolean("setting_notice_comments", true)) }
    var followersNotice by remember { mutableStateOf(prefs.getBoolean("setting_notice_followers", true)) }
    var mentionsNotice by remember { mutableStateOf(prefs.getBoolean("setting_notice_mentions", true)) }
    var systemNotice by remember { mutableStateOf(prefs.getBoolean("setting_notice_system", true)) }

    var cacheTick by remember { mutableIntStateOf(0) }
    val cacheSizeLabel = remember(cacheTick) { val b = runCatching { context.cacheDir.walkTopDown().filter { it.isFile }.sumOf { it.length() } }.getOrDefault(0L); if (b < 1024 * 1024) "${b / 1024} KB" else String.format(java.util.Locale.US, "%.1f MB", b / 1048576.0) }
    var showBlockedAccounts by remember { mutableStateOf(false) }
    var showDeleteAccount by remember { mutableStateOf(false) }
    var deleteConfirmText by remember { mutableStateOf("") }
    var deletingAccount by remember { mutableStateOf(false) }
    if (showBlockedAccounts) BlockedAccountsDialog(prefs = prefs, onDismiss = { showBlockedAccounts = false }, onNotice = onNotice)
    if (showDeleteAccount) {
        AlertDialog(
        shape = RoundedCornerShape(28.dp),
            onDismissRequest = { if (!deletingAccount) showDeleteAccount = false },
            title = { Text("Excluir conta definitivamente?") },
            text = {
                Column {
                    Text("Seu perfil, publicações, comentários, curtidas, mensagens e arquivos serão apagados e não poderão ser recuperados. Para confirmar, digite o seu nome de usuário: " + username.removePrefix("@"))
                    OutlinedTextField(
                        value = deleteConfirmText,
                        onValueChange = { deleteConfirmText = it },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(enabled = !deletingAccount && deleteConfirmText.trim().isNotEmpty(), onClick = {
                    val token = prefs.getString("token", null)
                    if (token.isNullOrBlank()) onNotice("Entre na sua conta para excluí-la.")
                    else {
                        deletingAccount = true
                        Thread {
                            try {
                                QinblyApi.deleteAccount(token, deleteConfirmText.trim())
                                android.os.Handler(android.os.Looper.getMainLooper()).post {
                                    deletingAccount = false
                                    showDeleteAccount = false
                                    deleteConfirmText = ""
                                    onNotice("Sua conta foi excluída.")
                                    onLogout()
                                }
                            } catch (e: Exception) {
                                android.os.Handler(android.os.Looper.getMainLooper()).post { deletingAccount = false; onNotice(e.message ?: "Não foi possível excluir a conta agora.") }
                            }
                        }.start()
                    }
                }) { Text(if (deletingAccount) "Excluindo…" else "Excluir", color = Color.Red) }
            },
            dismissButton = { TextButton(enabled = !deletingAccount, onClick = { showDeleteAccount = false }) { Text("Cancelar", color = Pink) } },
            containerColor = QiblyTheme.card, titleContentColor = QiblyTheme.text, textContentColor = QiblyTheme.text
        )
    }

    fun exportAccountData() {
        val token = prefs.getString("token", null)
        if (token.isNullOrBlank()) {
            onNotice("Entre na sua conta antes de exportar os dados.")
            return
        }
        onNotice("Preparando a exportação dos seus dados…")
        Thread {
            try {
                val json = QinblyApi.exportMyData(token).toString(2)
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    try {
                        val share = Intent(Intent.ACTION_SEND).apply {
                            type = "application/json"
                            putExtra(Intent.EXTRA_SUBJECT, "Meus dados do ${QinblyBrand.Name}")
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
        "account" -> "Conta"
        "privacy" -> "Privacidade e segurança"
        "notifications" -> "Notificações"
        "appearance" -> "Tela"
        "font_names" -> "Fonte dos nomes"
        "font_app" -> "Fonte do aplicativo"
        "content" -> "Conteúdo e interações"
        "lives" -> "Lives e transmissão"
        "storage" -> "Dados e armazenamento"
        "help" -> "Ajuda e suporte"
        "feed" -> "Vídeos"
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
                Text(title, fontSize = 19.sp, fontWeight = FontWeight.Bold, color = RemoteAppearance.screenTextColor)
                Text(if (section == "home") "Gerencie sua conta e sua experiência" else "Personalize sua experiência no ${QinblyBrand.Name}", color = RemoteAppearance.screenSecondaryTextColor, style = MaterialTheme.typography.bodySmall)
            }
        }
        if (section == "home") {
            Surface(color = QiblyTheme.card, shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).border(1.dp, QiblyTheme.border, RoundedCornerShape(16.dp))) {
                Row(Modifier.padding(horizontal = 13.dp, vertical = 11.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Search, contentDescription = null, tint = QinblyBrand.Accent)
                    Spacer(Modifier.width(10.dp))
                    androidx.compose.foundation.text.BasicTextField(
                        value = search, onValueChange = { search = it }, singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(color = RemoteAppearance.screenTextColor, fontSize = 15.sp),
                        cursorBrush = Brush.horizontalGradient(listOf(QinblyBrand.Accent, QinblyBrand.AccentDeep)), modifier = Modifier.fillMaxWidth(),
                        decorationBox = { inner -> Box { if (search.isBlank()) Text("Buscar configurações...", color = RemoteAppearance.screenSecondaryTextColor); inner() } }
                    )
                }
            }
        }
        LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(2.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
            if (section == "home") {
                item { Text("CONFIGURAÇÕES", color = Color(0xFF8A9BAB), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold) }
                val categories = listOf(
                    Triple("account", "Conta", "Perfil, contas bloqueadas e exclusão"),
                    Triple("notifications", "Notificações", "Escolha quais avisos receber"),
                    Triple("feed", "Vídeos", "Som e repetição"),
                    Triple("appearance", "Tela", "Tema escuro e fontes"),
                    Triple("accessibility", "Acessibilidade", "Texto maior e menos animações"),
                    Triple("storage", "Dados e armazenamento", "Cache e seus dados"),
                    Triple("invite", "Convidar amigos", "Compartilhe o ${QinblyBrand.Name}"),
                    Triple("help", "Ajuda e suporte", "Central, sobre e atualizações")
                ).filter { it.second.contains(search, true) || it.third.contains(search, true) }
                val loggedIn = username.isNotBlank() && !prefs.getString("token", null).isNullOrBlank()
                val accountActionMatches = search.isBlank() ||
                    "Alternar entre suas contas".contains(search, true) ||
                    "Sair".contains(search, true) ||
                    "Login".contains(search, true)
                categories.forEach { category ->
                    item {
                        val icon = when (category.first) {
                            "account" -> Icons.Outlined.PersonOutline
                            "notifications" -> Icons.Outlined.NotificationsNone
                            "feed" -> Icons.Outlined.PlayCircleOutline
                            "appearance" -> Icons.Outlined.DarkMode
                            "accessibility" -> Icons.Outlined.Accessibility
                            "storage" -> Icons.Outlined.Storage
                            "invite" -> Icons.Outlined.PersonAddAlt1
                            else -> Icons.Outlined.HelpOutline
                        }
                        SettingsLink(category.second, category.third, icon) {
                            if (category.first == "invite") {
                                try {
                                    val share = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, "Venha para o ${QinblyBrand.Name}! Baixe o app e me siga: @${username}") }
                                    context.startActivity(Intent.createChooser(share, "Convidar amigos"))
                                } catch (_: Exception) { onNotice("Não foi possível abrir o compartilhamento neste aparelho.") }
                            } else section = category.first
                        }
                    }
                }
                if (loggedIn && (search.isBlank() || "Alternar entre suas contas".contains(search, true))) {
                    item { SettingsLink("Alternar entre suas contas", "Trocar de perfil sem sair da conta", Icons.Outlined.SwapHoriz) { onSwitchAccounts() } }
                }
                if (loggedIn && (search.isBlank() || "Sair".contains(search, true) || "Encerrar sessão".contains(search, true))) {
                    item { SettingsLink("Sair", "Encerrar a sessão neste dispositivo", Icons.Outlined.Logout) { onLogout() } }
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
                    item { VideoPrefSwitch("Mostrar que estou online", "Conta você na lista de pessoas online", "setting_activity_status", true, prefs, Icons.Outlined.Sensors) }
                    item { SettingsLink("Compartilhar perfil", "Convide amigos para conhecer seu perfil", Icons.Outlined.Share) {
                        try {
                            val share = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, "Conheça meu perfil no ${QinblyBrand.Name}: @${username}") }
                            context.startActivity(Intent.createChooser(share, "Compartilhar perfil"))
                        } catch (_: Exception) { onNotice("Não foi possível abrir o compartilhamento neste aparelho.") }
                    } }
                    item { SettingsLink("Contas bloqueadas", "Gerenciar quem você bloqueou", Icons.Outlined.Block) { showBlockedAccounts = true } }
                    item { SettingsLink("Excluir conta", "Apagar sua conta e seus dados definitivamente", Icons.Outlined.DeleteOutline) { deleteConfirmText = ""; showDeleteAccount = true } }
                }
                "notifications" -> {
                    item { SettingSwitch("Silenciar tudo", "Desliga todos os avisos do ${QinblyBrand.Name}", !notifications, Icons.Outlined.NotificationsOff) { notifications = !it; prefs.edit().putBoolean("setting_notifications", !it).apply() } }
                    item { Text("AVISOS", color = RemoteAppearance.screenSecondaryTextColor, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = 10.dp, bottom = 2.dp)) }
                    item { SettingSwitch("Curtidas", "Quando alguém curtir sua publicação", likesNotice, Icons.Outlined.FavoriteBorder) { likesNotice = it; prefs.edit().putBoolean("setting_notice_likes", it).apply() } }
                    item { SettingSwitch("Comentários e respostas", "Quando alguém comentar ou responder você", commentsNotice, Icons.Outlined.ChatBubbleOutline) { commentsNotice = it; prefs.edit().putBoolean("setting_notice_comments", it).apply() } }
                    item { SettingSwitch("Menções", "Quando alguém marcar você com @", mentionsNotice, Icons.Outlined.AlternateEmail) { mentionsNotice = it; prefs.edit().putBoolean("setting_notice_mentions", it).apply() } }
                    item { SettingSwitch("Novos seguidores", "Quando alguém começar a seguir você", followersNotice, Icons.Outlined.GroupAdd) { followersNotice = it; prefs.edit().putBoolean("setting_notice_followers", it).apply() } }
                    item { SettingSwitch("Avisos do ${QinblyBrand.Name}", "Novidades e segurança da sua conta", systemNotice, Icons.Outlined.Campaign) { systemNotice = it; prefs.edit().putBoolean("setting_notice_system", it).apply() } }
                }
                "appearance" -> {
                    item { Text("TEMA", color = RemoteAppearance.screenSecondaryTextColor, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(bottom = 2.dp)) }
                    item {
                        SettingSwitch(
                            "Modo escuro",
                            if (QiblyTheme.dark) "Tema escuro ativado" else "Tema claro ativado",
                            QiblyTheme.dark,
                            Icons.Outlined.Brightness4
                        ) { enabled ->
                            QiblyTheme.dark = enabled
                            prefs.edit().putString("setting_theme", if (enabled) "dark" else "light").apply()
                        }
                    }
                    item { Text("FONTES", color = RemoteAppearance.screenSecondaryTextColor, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = 14.dp, bottom = 2.dp)) }
                    item { SettingsLink("Fonte dos nomes", "Personalize os nomes dos usuários", Icons.Outlined.Badge) { section = "font_names" } }
                    item { SettingsLink("Fonte do aplicativo", "Personalize os menus, títulos e botões", Icons.Outlined.FontDownload) { section = "font_app" } }
                }
                "font_names", "font_app" -> {
                    item { Text(if (section == "font_names") "Escolha como os nomes aparecem no feed." else "Altera títulos, menus e botões. Não altera mensagens, legendas ou comentários dos usuários.", color = RemoteAppearance.screenSecondaryTextColor, style = MaterialTheme.typography.bodySmall) }
                    val options = listOf("Padrão", "SF Pro (similar)", "Moderna", "Elegante", "Clássica", "Monoespaçada", "Negrito", "Minimalista", "Arredondada", "Tecnológica")
                    items(options) { option ->
                        val selected = if (section == "font_names") nameFontStyle == option else appFontStyle == option
                        Row(Modifier.fillMaxWidth().clickable { if (section == "font_names") onNameFontChange(option) else onAppFontChange(option) }.padding(vertical = 14.dp, horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(if (section == "font_names") "Ana Lima" else QinblyBrand.Name, fontFamily = nameFontFamily(option), fontSize = 19.sp, fontWeight = if (option == "Negrito") FontWeight.ExtraBold else FontWeight.Medium, color = RemoteAppearance.screenTextColor, modifier = Modifier.weight(1f))
                            Text(option, color = if (selected) QinblyBrand.Accent else RemoteAppearance.screenSecondaryTextColor, fontSize = 13.sp)
                            Spacer(Modifier.width(12.dp))
                            Icon(if (selected) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked, contentDescription = null, tint = if (selected) QinblyBrand.Accent else Muted, modifier = Modifier.size(20.dp))
                        }
                        androidx.compose.material3.HorizontalDivider(color = QiblyTheme.border, thickness = 0.5.dp)
                    }
                }
                "feed" -> {
                    item { VideoPrefSwitch("Economia de dados", "Vídeos só carregam quando você aperta play", "setting_data_saver", false, prefs, Icons.Outlined.DataSaverOn) }
                    item { VideoPrefSwitch("Iniciar vídeos sem som", "Os vídeos começam mudos; toque no som para ligar", "setting_video_muted", false, prefs, Icons.Outlined.VolumeOff) }
                    item { VideoPrefSwitch("Repetir vídeos", "Quando o vídeo termina, ele recomeça sozinho", "setting_video_loop", false, prefs, Icons.Outlined.Repeat) }
                }
                "accessibility" -> {
                    item {
                        var large by remember { mutableStateOf(QiblyTheme.largeText) }
                        SettingSwitch("Texto maior", "Aumenta as letras do aplicativo todo", large, Icons.Outlined.FormatSize) {
                            large = it; QiblyTheme.largeText = it; prefs.edit().putBoolean("setting_large_text", it).apply()
                        }
                    }
                    item {
                        var calm by remember { mutableStateOf(QiblyTheme.reduceMotion) }
                        SettingSwitch("Reduzir animações", "Menos efeitos, como o coração ao curtir", calm, Icons.Outlined.SlowMotionVideo) {
                            calm = it; QiblyTheme.reduceMotion = it; prefs.edit().putBoolean("setting_reduce_motion", it).apply()
                        }
                    }
                }
                "storage" -> {
                    item { SettingsLink("Limpar cache", "Apagar arquivos temporários (${cacheSizeLabel})", Icons.Outlined.CleaningServices) { Thread { val ok = runCatching { context.cacheDir.deleteRecursively(); context.codeCacheDir.deleteRecursively(); true }.getOrDefault(false); android.os.Handler(android.os.Looper.getMainLooper()).post { cacheTick++; onNotice(if (ok) "Cache temporário limpo." else "Não foi possível limpar todo o cache.") } }.start() } }
                    item { SettingsLink("Baixar seus dados", "Exportar seus dados da conta", Icons.Outlined.FileDownload) { exportAccountData() } }
                }
                else -> {
                    item { SettingsLink("Reportar um problema", "Enviar um relato por e-mail ou aplicativo", Icons.Outlined.Flag) { try { val report = Intent(Intent.ACTION_SENDTO).apply { data = android.net.Uri.parse("mailto:"); putExtra(Intent.EXTRA_SUBJECT, "Relato de problema — ${QinblyBrand.Name}"); putExtra(Intent.EXTRA_TEXT, "Descreva o problema encontrado:\n\nVersão: 0.3.0\nDispositivo: " + android.os.Build.MANUFACTURER + " " + android.os.Build.MODEL) }; context.startActivity(Intent.createChooser(report, "Reportar problema")) } catch (_: Exception) { onNotice("Não há aplicativo de e-mail disponível neste aparelho.") } } }
                    item { SettingsLink("Sobre o ${QinblyBrand.Name}", "Versão 0.3.0 · Termos e privacidade", Icons.Outlined.Info) { onNotice("${QinblyBrand.Name} 0.3.0. Os links de termos e privacidade precisam ser publicados antes do lançamento.") } }
                    item { SettingsLink("Atualizações do aplicativo", "Verificar e baixar o APK mais recente", Icons.Outlined.SystemUpdate) { context.startActivity(Intent(context, RemoteUiActivity::class.java)) } }
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
private fun VideoPrefSwitch(title: String, subtitle: String, key: String, default: Boolean, prefs: android.content.SharedPreferences, icon: ImageVector) {
    var checked by remember(key) { mutableStateOf(prefs.getBoolean(key, default)) }
    SettingSwitch(title, subtitle, checked, icon) {
        checked = it
        prefs.edit().putBoolean(key, it).apply()
    }
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
                .padding(horizontal = 8.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(34.dp), contentAlignment = Alignment.Center) { Icon(icon, contentDescription = null, tint = QiblyTheme.text, modifier = Modifier.size(24.dp)) }
            Column(Modifier.weight(1f).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(title, fontWeight = FontWeight.Medium, color = QiblyTheme.text, fontSize = 15.sp)
                Text(subtitle, color = Muted, fontSize = 12.sp, lineHeight = 15.sp)
            }
            Text("›", color = Muted, fontSize = 22.sp)
        }
        androidx.compose.material3.HorizontalDivider(color = QiblyTheme.border, thickness = 0.5.dp)
    }
}

@Composable
private fun InfoPanel(title: String, message: String) {
    Surface(color = QiblyTheme.card, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
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
            Box(Modifier.size(38.dp), contentAlignment = Alignment.Center) { Icon(icon, contentDescription = null, tint = QiblyTheme.text, modifier = Modifier.size(24.dp)) }
            Column(Modifier.weight(1f).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(title, fontWeight = FontWeight.Medium, color = QiblyTheme.text, fontSize = 15.sp)
                Text(description, color = Muted, fontSize = 12.sp, lineHeight = 15.sp)
            }
            Switch(checked = checked, onCheckedChange = onChange, colors = androidx.compose.material3.SwitchDefaults.colors(checkedTrackColor = QinblyBrand.Accent, checkedThumbColor = Color.White, uncheckedTrackColor = if (QiblyTheme.dark) Color(0xFF3A4458) else Color(0xFFC9CED6)))
        }
        androidx.compose.material3.HorizontalDivider(color = QiblyTheme.border, thickness = 0.5.dp)
    }
}

private fun saveProfilePhotoToDevice(context: android.content.Context, imageUrl: String, onResult: (String) -> Unit) {
    Thread {
        val message = try {
            val request = android.app.DownloadManager.Request(android.net.Uri.parse(imageUrl))
                .setTitle("Foto do perfil ${QinblyBrand.Name}")
                .setNotificationVisibility(android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(android.os.Environment.DIRECTORY_DOWNLOADS, "${QinblyBrand.Name}-perfil-" + System.currentTimeMillis() + ".jpg")
            (context.getSystemService(android.content.Context.DOWNLOAD_SERVICE) as android.app.DownloadManager).enqueue(request)
            "Download da foto iniciado."
        } catch (e: Exception) { e.message ?: "Não foi possível salvar a foto." }
        android.os.Handler(android.os.Looper.getMainLooper()).post { onResult(message) }
    }.start()
}

@Composable
private fun PublicProfilePage(username: String, onBack: () -> Unit, onMessage: (String) -> Unit, onNotice: (String) -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { context.getSharedPreferences("qinbly_session", android.content.Context.MODE_PRIVATE) }
    var reloadKey by remember { mutableIntStateOf(0) }
    val cachedProfile = remember(username) { QinblyCache.profiles[QinblyCache.profileKey(username)] }
    var user by remember(username) { mutableStateOf(cachedProfile?.first ?: org.json.JSONObject()) }
    var loading by remember(username) { mutableStateOf(cachedProfile == null) }
    var profileLoadError by remember(username) { mutableStateOf<String?>(null) }
    var connectionListType by remember(username) { mutableStateOf("") }
    var connectionListUsers by remember(username) { mutableStateOf<List<QinblyApi.UserSummary>>(emptyList()) }
    var connectionListLoading by remember(username) { mutableStateOf(false) }
    fun openConnections(type: String) {
        connectionListType = type
        connectionListLoading = true
        Thread {
            try {
                val people = QinblyApi.getConnections(username, type)
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    QinblyEngagement.learnFromUsers(prefs, people)
                    connectionListUsers = people
                    connectionListLoading = false
                }
            } catch (e: Exception) {
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    connectionListLoading = false
                    onNotice(e.message ?: "Não foi possível carregar a lista.")
                }
            }
        }.start()
    }

    var selectedTab by remember(username) { mutableStateOf("Publicações") }
    var expandedImage by remember { mutableStateOf<String?>(null) }
    var photoMenuOpen by remember { mutableStateOf(false) }
    var viewerPostId by remember { mutableStateOf<String?>(null) }
    var selectedPublicationCaption by remember { mutableStateOf("") }
    var selectedPublication by remember { mutableStateOf<QinblyApi.PostSummary?>(null) }
    var commentsOpen by remember { mutableStateOf(false) }
    var sharePost by remember { mutableStateOf<QinblyApi.PostSummary?>(null) }

    var publicationComment by remember { mutableStateOf("") }
    var showMoreOptions by remember { mutableStateOf(false) }
    var profilePosts by remember(username) { mutableStateOf(cachedProfile?.second ?: emptyList()) }
    val openPublicComments: (QinblyApi.PostSummary) -> Unit = { target ->
        selectedPublication = target
        commentsOpen = true
    }
    val displayName = user.optString("displayName").ifBlank { user.optString("display_name").ifBlank { username.removePrefix("@") } }
    val publicUsername = user.optString("username").ifBlank { username.removePrefix("@") }
    val avatarUrl = user.optString("avatar_url").ifBlank { user.optString("avatarUrl").ifBlank { user.optString("profile_photo_url").ifBlank { user.optString("profilePhotoUrl") } } }
    val profilePhotoUrl = user.optString("coverUrl").ifBlank { user.optString("cover_url").ifBlank { user.optString("profilePhotoUrl").ifBlank { avatarUrl } } }
    val bio = user.optString("bio")
    LaunchedEffect(username, reloadKey) {
        loading = cachedProfile == null && user.length() == 0
        profileLoadError = null
        Thread {
            try {
                val response = QinblyApi.getPublicProfile(username, prefs.getString("token", null))
                val found = response.optJSONObject("user") ?: response.optJSONObject("profile") ?: response
                val resolvedUsername = found.optString("username").ifBlank { username.removePrefix("@") }
                // O endpoint de perfil já devolve TODAS as publicações públicas do usuário.
                // Não buscar somente os 30 itens mais recentes do feed global.
                val posts = QinblyApi.parsePosts(response.optJSONArray("posts"), found.apply { if (!has("username")) put("username", resolvedUsername) }).ifEmpty {
                    // Servidor antigo (sem publicações no perfil): usa o feed filtrado como antes.
                    try { QinblyApi.getPosts(prefs.getString("token", null)).filter { it.username.equals(resolvedUsername, ignoreCase = true) } } catch (_: Exception) { emptyList() }
                }
                QinblyCache.profiles[QinblyCache.profileKey(username)] = found to posts
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    user = found
                    profileLoadError = null
                    QinblyEngagement.syncFollow(prefs, resolvedUsername, found.optBoolean("isFollowing"))
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
            Text("@$publicUsername", color = RemoteAppearance.screenTextColor, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1)
            Spacer(Modifier.weight(1f))
            IconButton(onClick = { showMoreOptions = true }) { Icon(Icons.Default.MoreHoriz, contentDescription = "Opções deste perfil", tint = RemoteAppearance.screenTextColor) }
        }
        Box(Modifier.fillMaxWidth().padding(top = 6.dp, bottom = 8.dp), contentAlignment = Alignment.Center) {
            Box(Modifier.size(225.dp), contentAlignment = Alignment.Center) {
                Box(Modifier.size(205.dp).clip(CircleShape).background(QiblyTheme.card).clickable {
                    if (profilePhotoUrl.isNotBlank()) { expandedImage = profilePhotoUrl }
                }) {
                    if (profilePhotoUrl.isNotBlank()) AsyncImage(model = profilePhotoUrl, contentDescription = "Foto de $displayName", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    else Icon(Icons.Default.Person, contentDescription = "Sem foto", tint = RemoteAppearance.screenSecondaryTextColor, modifier = Modifier.fillMaxSize().padding(34.dp))
                }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Text(displayName, textAlign = androidx.compose.ui.text.style.TextAlign.Center, color = RemoteAppearance.screenTextColor, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            if (user.optBoolean("verified")) { Spacer(Modifier.width(6.dp)); QinblyVerifiedBadge(22.dp) }
        }
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
    if (connectionListType.isNotBlank()) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { connectionListType = "" }) {
            Surface(shape = RoundedCornerShape(20.dp), color = QiblyTheme.card) {
                Column(Modifier.fillMaxWidth().padding(16.dp)) {
                    Text(if (connectionListType == "following") "Seguindo" else "Seguidores", fontWeight = FontWeight.Bold, color = QiblyTheme.text)
                    Spacer(Modifier.height(12.dp))
                    if (connectionListLoading) androidx.compose.material3.CircularProgressIndicator()
                    else if (connectionListUsers.isEmpty()) Text("Nenhuma pessoa encontrada.", color = Color.Gray)
                    else LazyColumn(Modifier.heightIn(max = 440.dp)) {
                        items(connectionListUsers, key = { it.id.ifBlank { it.username } }) { person ->
                            QinblyUserRow(person, prefs, onOpenProfile = { connectionListType = ""; QinblyTagNav.profile = it }, onError = onNotice)
                        }
                    }
                    TextButton(onClick = { connectionListType = "" }) { Text("Fechar") }
                }
            }
        }
    }
        Row(Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            ProfileStat("Publicações", profilePosts.size.toString(), Icons.Default.GridView) { }
            ProfileStat("Seguindo", user.optInt("following").toString(), Icons.Default.PersonAddAlt1) { openConnections("following") }
            ProfileStat("Seguidores", user.optInt("followers").toString(), Icons.Default.Groups) { openConnections("followers") }
        }
        Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            QinblyFollowButton(
                following = QinblyEngagement.isFollowing(prefs, publicUsername, user.optBoolean("isFollowing")),
                busy = QinblyEngagement.isFollowBusy(publicUsername),
                onClick = {
                    val targetId = user.optString("id").ifBlank { user.optString("userId") }.ifBlank { user.optString("user_id") }
                    val before = user.optBoolean("isFollowing")
                    QinblyEngagement.toggleFollow(prefs, publicUsername, targetId, "@$publicUsername", onNotice) { following ->
                        user = org.json.JSONObject(user.toString()).apply {
                            if (following != before) put("followers", (optInt("followers") + if (following) 1 else -1).coerceAtLeast(0))
                            put("isFollowing", following)
                        }
                    }
                },
                large = true,
                modifier = Modifier.weight(1f)
            )
            OutlinedButton(onClick = {
                onMessage(publicUsername)
            }, modifier = Modifier.weight(0.88f).height(46.dp), border = BorderStroke(1.dp, QinblyBrand.Accent)) {
                Icon(Icons.Default.ChatBubbleOutline, contentDescription = null, modifier = Modifier.size(17.dp), tint = QinblyBrand.Accent)
                Spacer(Modifier.width(6.dp))
                Text("Mensagem", color = QinblyBrand.Accent)
            }
        }
        Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(0.dp)) {
            listOf("Publicações", "Vídeos", "Fotos").forEach { section ->
                Column(Modifier.weight(1f).clickable { selectedTab = section }.padding(vertical = 7.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(imageVector = when (section) {
                        "Vídeos" -> Icons.Default.PlayCircle
                        "Fotos" -> Icons.Default.Image
                        "Republicações" -> Icons.Default.Repeat
                        "Curtidas" -> Icons.Default.FavoriteBorder
                        "Salvos" -> Icons.Default.BookmarkBorder
                        else -> Icons.Default.GridView
                    }, contentDescription = section, tint = if (selectedTab == section) QinblyBrand.Accent else RemoteAppearance.screenSecondaryTextColor, modifier = Modifier.size(25.dp))
                    Text(section, color = if (selectedTab == section) QinblyBrand.Accent else RemoteAppearance.screenTextColor, fontWeight = if (selectedTab == section) FontWeight.Bold else FontWeight.Normal, style = MaterialTheme.typography.labelSmall, maxLines = 1)
                    Box(Modifier.fillMaxWidth().height(3.dp).background(if (selectedTab == section) Brush.horizontalGradient(listOf(QinblyBrand.Accent, QinblyBrand.Accent)) else Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))))
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
            Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                profilePosts.filter { (selectedTab != "Vídeos" || it.mediaType == "video") }.sortedByDescending { it.pinned }
                    .filter { selectedTab != "Fotos" || it.mediaType == "image" }
                    .forEach { post ->
                        QinblyPostCard(
                            post = post,
                            prefs = prefs,
                            nameFontStyle = "",
                            onNotice = onNotice,
                            onOpenProfile = { },
                            onOpenViewer = { viewerPostId = it.id },
                            onComment = { openPublicComments(it) },
                            onShare = { sharePost = it },
                            onPostsTransform = { change -> profilePosts = change(profilePosts) },
                            onRefresh = { reloadKey++ }
                        )
                    }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
    var showPhotoReport by remember { mutableStateOf(false) }
    if (showPhotoReport) {
        ReportReasonDialog(title = "Denunciar: qual o motivo?", onDismiss = { showPhotoReport = false }) { reason ->
            showPhotoReport = false
            val token = prefs.getString("token", null)
            if (token.isNullOrBlank()) onNotice("Entre na sua conta para denunciar.")
            else Thread {
                try {
                    QinblyApi.reportUserByUsername(token, username, reason)
                    android.os.Handler(android.os.Looper.getMainLooper()).post { onNotice("Denúncia enviada. Obrigado por ajudar a manter a comunidade segura.") }
                } catch (e: Exception) {
                    android.os.Handler(android.os.Looper.getMainLooper()).post { onNotice(e.message ?: "Não foi possível enviar a denúncia agora.") }
                }
            }.start()
        }
    }
    expandedImage?.let { shownPhoto -> QinblyPhotoViewer(shownPhoto, onNotice, onDismiss = { expandedImage = null }, onReport = { showPhotoReport = true }) }
    viewerPostId?.let { startId ->
        val viewable = qinblyViewerList(profilePosts.filter { it.mediaType != "poll" && (it.mediaUrl.isNotBlank() || it.mediaUrls.isNotEmpty()) }, startId)
        if (viewable.any { it.id == startId }) {
            QinblyPostViewer(
                posts = viewable,
                startPostId = startId,
                prefs = prefs,
                currentUsername = prefs.getString("username", "").orEmpty(),
                onNotice = onNotice,
                onDismiss = { viewerPostId = null },
                onComment = { openPublicComments(it) },
                onShare = { sharePost = it },
                onOpenProfile = { viewerPostId = null }
            )
        }
    }
    sharePost?.let { target ->
        QiblyShareDialog(post = target, prefs = prefs, onDismiss = { sharePost = null }, onNotice = onNotice)
    }
    if (commentsOpen) {
        selectedPublication?.let { target ->
            QinblyCommentsDialog(
                post = target,
                prefs = prefs,
                currentUsername = prefs.getString("username", "").orEmpty(),
                onNotice = onNotice,
                onDismiss = { commentsOpen = false },
                onCountChanged = { postId, count ->
                    profilePosts = profilePosts.map { if (it.id == postId && it.comments != count) it.copy(comments = count) else it }
                    if (selectedPublication?.id == postId) selectedPublication = selectedPublication?.copy(comments = count)
                }
            )
        }
    }
    var confirmBlock by remember { mutableStateOf(false) }
    if (showMoreOptions) {
        AlertDialog(
        shape = RoundedCornerShape(28.dp),
        containerColor = QiblyTheme.card,
            onDismissRequest = { showMoreOptions = false },
            title = { Text(displayName, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    TextButton(onClick = {
                        val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                        clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Perfil ${QinblyBrand.Name}", "@$publicUsername"))
                        showMoreOptions = false
                        onNotice("@$publicUsername copiado.")
                    }, modifier = Modifier.fillMaxWidth()) { Text("Copiar @", modifier = Modifier.fillMaxWidth()) }
                    TextButton(onClick = {
                        showMoreOptions = false
                        val sendIntent = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, "Perfil de $displayName no ${QinblyBrand.Name}: @$publicUsername") }
                        context.startActivity(Intent.createChooser(sendIntent, "Compartilhar perfil"))
                    }, modifier = Modifier.fillMaxWidth()) { Text("Compartilhar perfil", modifier = Modifier.fillMaxWidth()) }
                    TextButton(onClick = { showMoreOptions = false; showPhotoReport = true }, modifier = Modifier.fillMaxWidth()) {
                        Text("Denunciar perfil", color = Color(0xFFD33B3B), modifier = Modifier.fillMaxWidth())
                    }
                    TextButton(onClick = { showMoreOptions = false; confirmBlock = true }, modifier = Modifier.fillMaxWidth()) {
                        Text("Bloquear perfil", color = Color(0xFFD33B3B), modifier = Modifier.fillMaxWidth())
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showMoreOptions = false }) { Text("Fechar") } }
        )
    }
    if (confirmBlock) {
        AlertDialog(
        shape = RoundedCornerShape(28.dp),
        containerColor = QiblyTheme.card,
            onDismissRequest = { confirmBlock = false },
            title = { Text("Bloquear @$publicUsername?") },
            text = { Text("Essa pessoa não poderá interagir com você. Você pode desbloquear depois em Configurações > Contas bloqueadas.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmBlock = false
                    val token = prefs.getString("token", null)
                    if (token.isNullOrBlank()) onNotice("Entre na sua conta para bloquear.")
                    else Thread {
                        try {
                            val blocked = QinblyApi.blockByUsername(token, username)
                            android.os.Handler(android.os.Looper.getMainLooper()).post {
                                onNotice(if (blocked) "@$publicUsername bloqueado." else "@$publicUsername desbloqueado.")
                                if (blocked) onBack()
                            }
                        } catch (e: Exception) {
                            android.os.Handler(android.os.Looper.getMainLooper()).post { onNotice(e.message ?: "Não foi possível bloquear agora.") }
                        }
                    }.start()
                }) { Text("Bloquear", color = Color(0xFFD33B3B)) }
            },
            dismissButton = { TextButton(onClick = { confirmBlock = false }) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun ProfileStat(label: String, count: String, icon: ImageVector, onClick: () -> Unit) {
    Column(Modifier.clip(RoundedCornerShape(12.dp)).clickable(onClick = onClick).padding(horizontal = 14.dp, vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(count, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = RemoteAppearance.screenTextColor)
        Text(label, style = MaterialTheme.typography.labelMedium, color = RemoteAppearance.screenSecondaryTextColor)
    }
}

private data class QinblyGalleryAsset(val uri: Uri, val isVideo: Boolean, val dateAdded: Long, val name: String)

private fun extractQinblyVideoFrames(context: android.content.Context, uri: Uri): List<Bitmap> {
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

/** Intensidade (0–1) do filtro de cor escolhido na câmera; usada na prévia e na exportação. */
private object QinblyFilterIntensity { var value: Float = 1f }

private val QinblyColorFilterNames = listOf("Original", "Aurora", "Pôr do sol", "Neon", "Filme", "Pastel", "Noir", "Oceano", "Vintage", "Sonho", "Dourado", "Cinema")
private val QinblyAnimatedEffectNames = listOf("Corações", "Confete", "Neve", "Brilho", "Bolhas", "Chuva", "Fogo")

/** Monta um "look" de cor: saturação, contraste ao redor do cinza médio, ganho por canal (R,G,B) e deslocamento por canal. */
private fun qinblyLook(sat: Float, con: Float, rm: Float, gm: Float, bm: Float, ro: Float, go: Float, bo: Float): ColorMatrix {
    val m = ColorMatrix()
    m.setSaturation(sat)
    val k = (1f - con) * 127.5f
    m.postConcat(ColorMatrix(floatArrayOf(con * rm, 0f, 0f, 0f, k + ro, 0f, con * gm, 0f, 0f, k + go, 0f, 0f, con * bm, 0f, k + bo, 0f, 0f, 0f, 1f, 0f)))
    return m
}

/** Matriz de cor de cada filtro. A intensidade mistura o filtro com a imagem original. */
private fun qinblyFilterMatrix(name: String, intensity: Float = QinblyFilterIntensity.value): ColorMatrix {
    val matrix = ColorMatrix()
    when (name) {
        "Aurora" -> matrix.set(qinblyLook(1.15f, 1.05f, 0.96f, 1.04f, 1.10f, 4f, 6f, 14f).array)
        "Pôr do sol" -> matrix.set(qinblyLook(1.30f, 1.08f, 1.15f, 0.98f, 0.82f, 14f, 0f, -6f).array)
        "Neon" -> matrix.set(qinblyLook(1.70f, 1.18f, 1.05f, 0.95f, 1.15f, 0f, -4f, 8f).array)
        "Filme" -> matrix.set(qinblyLook(0.80f, 1.05f, 1.04f, 1.00f, 0.90f, 10f, 6f, 0f).array)
        "Pastel" -> matrix.set(qinblyLook(0.75f, 0.85f, 1.02f, 1.02f, 1.04f, 34f, 32f, 36f).array)
        "Noir" -> matrix.set(qinblyLook(0f, 1.40f, 1f, 1f, 1f, 0f, 0f, 0f).array)
        "Oceano" -> matrix.set(qinblyLook(1.20f, 1.06f, 0.85f, 1.04f, 1.18f, -4f, 4f, 10f).array)
        "Vintage", "Retrô" -> matrix.set(qinblyLook(0.70f, 0.95f, 1.08f, 0.98f, 0.80f, 18f, 12f, 2f).array)
        "Sonho" -> matrix.set(qinblyLook(1.10f, 0.90f, 1.08f, 1.00f, 1.10f, 30f, 22f, 34f).array)
        "Dourado" -> matrix.set(qinblyLook(1.15f, 1.05f, 1.12f, 1.04f, 0.78f, 14f, 8f, -10f).array)
        "Cinema" -> matrix.set(qinblyLook(0.90f, 1.15f, 1.10f, 1.00f, 0.95f, -6f, 0f, 10f).array)
        "P&B", "Preto e branco" -> matrix.setSaturation(0f)
        "Vivo", "Vívido" -> matrix.setSaturation(1.45f)
        "Quente" -> matrix.set(floatArrayOf(1.08f,0f,0f,0f,12f, 0f,1f,0f,0f,2f, 0f,0f,0.9f,0f,0f, 0f,0f,0f,1f,0f))
        "Frio" -> matrix.set(floatArrayOf(0.92f,0f,0f,0f,0f, 0f,1f,0f,0f,0f, 0f,0f,1.12f,0f,10f, 0f,0f,0f,1f,0f))
        "Sépia" -> matrix.set(floatArrayOf(0.393f,0.769f,0.189f,0f,0f, 0.349f,0.686f,0.168f,0f,0f, 0.272f,0.534f,0.131f,0f,0f, 0f,0f,0f,1f,0f))
        "Suave" -> { matrix.setSaturation(0.85f); matrix.postConcat(ColorMatrix(floatArrayOf(0.94f,0f,0f,0f,14f, 0f,0.94f,0f,0f,14f, 0f,0f,0.94f,0f,14f, 0f,0f,0f,1f,0f))) }
        "Desbotado" -> { matrix.setSaturation(0.55f); matrix.postConcat(ColorMatrix(floatArrayOf(0.9f,0f,0f,0f,22f, 0f,0.9f,0f,0f,22f, 0f,0f,0.9f,0f,22f, 0f,0f,0f,1f,0f))) }
        "Rosado" -> matrix.set(floatArrayOf(1.1f,0f,0f,0f,10f, 0f,0.95f,0f,0f,0f, 0f,0f,1.08f,0f,8f, 0f,0f,0f,1f,0f))
        "Brilho" -> matrix.set(floatArrayOf(1.12f,0f,0f,0f,18f, 0f,1.12f,0f,0f,18f, 0f,0f,1.12f,0f,18f, 0f,0f,0f,1f,0f))
        "Contraste" -> matrix.set(floatArrayOf(1.25f,0f,0f,0f,-22f, 0f,1.25f,0f,0f,-22f, 0f,0f,1.25f,0f,-22f, 0f,0f,0f,1f,0f))
        "Saturação" -> matrix.setSaturation(1.65f)
        "Fade" -> matrix.set(floatArrayOf(0.92f,0f,0f,0f,18f, 0f,0.92f,0f,0f,18f, 0f,0f,0.92f,0f,18f, 0f,0f,0f,1f,0f))
        "Glow" -> matrix.set(floatArrayOf(1.18f,0f,0f,0f,14f, 0f,1.08f,0f,0f,8f, 0f,0f,1.2f,0f,16f, 0f,0f,0f,1f,0f))
        "VHS" -> matrix.set(floatArrayOf(1.05f,0f,0f,0f,0f, 0f,0.92f,0f,0f,0f, 0f,0f,1.08f,0f,0f, 0f,0f,0f,1f,0f))
        "Granulado" -> matrix.setSaturation(0.9f)
        "Vinheta" -> matrix.set(floatArrayOf(0.82f,0f,0f,0f,0f, 0f,0.82f,0f,0f,0f, 0f,0f,0.82f,0f,0f, 0f,0f,0f,1f,0f))
        "Desfoque" -> matrix.setSaturation(0.75f)
    }
    val amount = intensity.coerceIn(0f, 1f)
    if (amount >= 0.999f) return matrix
    val identity = floatArrayOf(1f,0f,0f,0f,0f, 0f,1f,0f,0f,0f, 0f,0f,1f,0f,0f, 0f,0f,0f,1f,0f)
    val target = matrix.array
    return ColorMatrix(FloatArray(20) { identity[it] + (target[it] - identity[it]) * amount })
}

/** Três cores neutras de exemplo (céu claro, pele, chão bege), já com o filtro aplicado: servem de miniatura da bolinha. */
private fun qinblyFilterSwatch(name: String): List<Color> {
    val m = qinblyFilterMatrix(name, 1f).array
    return listOf(Color(0xFFBFD7EA), Color(0xFFE9B99A), Color(0xFFA89A88)).map { c ->
        val r = c.red * 255f
        val g = c.green * 255f
        val b = c.blue * 255f
        val nr = ((m[0] * r + m[1] * g + m[2] * b + m[4]) / 255f).coerceIn(0f, 1f)
        val ng = ((m[5] * r + m[6] * g + m[7] * b + m[9]) / 255f).coerceIn(0f, 1f)
        val nb = ((m[10] * r + m[11] * g + m[12] * b + m[14]) / 255f).coerceIn(0f, 1f)
        Color(nr, ng, nb)
    }
}

/** Ícones de linha da câmera (grade 24x24, traço 1,8). Usados com tint branco. */
private object QinblyCameraIcons {
    private fun build(name: String, vararg paths: String): androidx.compose.ui.graphics.vector.ImageVector {
        val b = androidx.compose.ui.graphics.vector.ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f)
        paths.forEach { d ->
            b.addPath(
                pathData = androidx.compose.ui.graphics.vector.PathParser().parsePathString(d).toNodes(),
                fill = null,
                stroke = androidx.compose.ui.graphics.SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
                strokeLineJoin = androidx.compose.ui.graphics.StrokeJoin.Round
            )
        }
        return b.build()
    }
    val close by lazy { build("cam_fechar", "M6 6l12 12M18 6L6 18") }
    val chevronDown by lazy { build("cam_seta_baixo", "M6 9l6 6 6-6") }
    val chevronUp by lazy { build("cam_seta_cima", "M6 15l6-6 6 6") }
    val flash by lazy { build("cam_flash", "M13 3L5 14h6l-1 7 8-11h-6z") }
    val timer by lazy { build("cam_timer", "M4.5 13.5a7.5 7.5 0 1 0 15 0a7.5 7.5 0 1 0 -15 0", "M12 9.5v4l2.5 1.5M9.5 3h5") }
    val collage by lazy { build("cam_colagem", "M6.5 3.5h11a3 3 0 0 1 3 3v11a3 3 0 0 1-3 3h-11a3 3 0 0 1-3-3v-11a3 3 0 0 1 3-3z", "M12 3.5v17M3.5 12h17") }
    val format by lazy { build("cam_formato", "M8 3H5a2 2 0 0 0-2 2v3M16 3h3a2 2 0 0 1 2 2v3M8 21H5a2 2 0 0 1-2-2v-3M16 21h3a2 2 0 0 0 2-2v-3") }
    val crown by lazy { build("cam_coroa", "M3 7l4.5 4L12 4l4.5 7L21 7l-2 11H5z", "M5 21h14") }
    val medal by lazy { build("cam_medalha", "M12 3a5 5 0 1 0 0 10a5 5 0 1 0 0-10", "M8.5 12.5L7 21l5-3 5 3-1.5-8.5") }
    val eye by lazy { build("cam_olho", "M2 12s4-7 10-7 10 7 10 7-4 7-10 7S2 12 2 12z", "M9 12a3 3 0 1 0 6 0a3 3 0 1 0-6 0") }
    val clock by lazy { build("cam_relogio", "M3 12a9 9 0 1 0 18 0a9 9 0 1 0 -18 0", "M12 7v5l3 2") }
    val trend by lazy { build("cam_alta", "M3 17l6-6 4 4 8-8", "M15 7h6v6") }
    val rotate by lazy { build("cam_girar", "M20 12a8 8 0 1 1-2.6-5.9", "M20 4v4.5h-4.5") }
    val adjust by lazy { build("cam_ajustes", "M12 3v18", "M3 12a9 9 0 1 0 18 0a9 9 0 1 0 -18 0", "M12 3a9 9 0 0 1 0 18z") }
    val crop by lazy { build("cam_recortar", "M7 3v14h14", "M3 7h14v14") }
    val editor by lazy { build("cam_editar", "M4 8h9M17 8h3M4 16h3M11 16h9", "M15 6v4M9 14v4") }
    val pen by lazy { build("cam_desenho", "M4 20l1-4L16.5 4.5a2.1 2.1 0 0 1 3 3L8 19z", "M14 7l3 3") }
    val retouch by lazy { build("cam_retoque", "M3.5 13.5a7 7 0 1 0 14 0a7 7 0 1 0 -14 0", "M7.9 12.5h.01M12.9 12.5h.01", "M8 16c1.4 1.4 3.6 1.4 5 0", "M19 2.5v5M16.5 5h5M20.5 14v3M19 15.5h3") }
    val filters by lazy { build("cam_filtros", "M3 12a6 6 0 1 0 12 0a6 6 0 1 0 -12 0", "M9 12a6 6 0 1 0 12 0a6 6 0 1 0 -12 0") }
    val grid by lazy { build("cam_grade", "M5.5 3.5h13a2 2 0 0 1 2 2v13a2 2 0 0 1-2 2h-13a2 2 0 0 1-2-2v-13a2 2 0 0 1 2-2z", "M9.2 3.5v17M14.8 3.5v17M3.5 9.2h17M3.5 14.8h17") }
    val mirror by lazy { build("cam_espelhar", "M12 3v18", "M9 7L3 17h6zM15 7l6 10h-6z") }
    val speed by lazy { build("cam_velocidade", "M3 6l8 6-8 6zM13 6l8 6-8 6z") }
    val handsFree by lazy { build("cam_maos_livres", "M3.5 12a8.5 8.5 0 1 0 17 0a8.5 8.5 0 1 0 -17 0", "M9 12a3 3 0 1 0 6 0a3 3 0 1 0 -6 0") }
    val exposure by lazy { build("cam_exposicao", "M8.5 12a3.5 3.5 0 1 0 7 0a3.5 3.5 0 1 0 -7 0", "M12 3v2M12 19v2M3 12h2M19 12h2M5.6 5.6L7 7M17 17l1.4 1.4M5.6 18.4L7 17M17 7l1.4-1.4") }
    val night by lazy { build("cam_noite", "M20 14.5A8 8 0 1 1 9.5 4a6.5 6.5 0 0 0 10.5 10.5z") }
    val hdr by lazy { build("cam_hdr", "M5.5 6h13a3 3 0 0 1 3 3v6a3 3 0 0 1-3 3h-13a3 3 0 0 1-3-3V9a3 3 0 0 1 3-3z", "M5 9.5v5M5 12h2.5M7.5 9.5v5", "M10 9.5v5h1a2.5 2.5 0 0 0 0-5h-1", "M15 14.5v-5h1.5a1.5 1.5 0 0 1 0 3H15M16.7 12.5l1.3 2") }
    val stabilize by lazy { build("cam_estabilizar", "M3 9c1.4-2.5 2.6-2.5 4 0s2.6 2.5 4 0 2.6-2.5 4 0 2.6 2.5 4 0", "M3 15.5h18") }
    val quality by lazy { build("cam_qualidade", "M5.5 6h13a3 3 0 0 1 3 3v6a3 3 0 0 1-3 3h-13a3 3 0 0 1-3-3V9a3 3 0 0 1 3-3z", "M7.5 9.5v5M7.5 12h3M10.5 9.5v5", "M13.5 9.5v5h1.5a2.5 2.5 0 0 0 0-5h-1.5") }
    val background by lazy { build("cam_fundo", "M6 4.5h12a2.5 2.5 0 0 1 2.5 2.5v10a2.5 2.5 0 0 1-2.5 2.5h-12a2.5 2.5 0 0 1-2.5-2.5v-10a2.5 2.5 0 0 1 2.5-2.5z", "M3.5 16l5-5 4 4 3-3 5 5", "M14 8.5a1.5 1.5 0 1 0 3 0a1.5 1.5 0 1 0 -3 0") }
    val flip by lazy { build("cam_virar", "M4 12a8 8 0 0 1 13.5-5.8L20 8.5M20 4v4.5h-4.5M20 12a8 8 0 0 1-13.5 5.8L4 15.5M4 20v-4.5h4.5") }
    val music by lazy { build("cam_musica", "M9 18V5l11-2v13", "M4 18a2.5 2.5 0 1 0 5 0a2.5 2.5 0 1 0 -5 0", "M15 16a2.5 2.5 0 1 0 5 0a2.5 2.5 0 1 0 -5 0") }
    val mic by lazy { build("cam_mic", "M9 6a3 3 0 0 1 6 0v5a3 3 0 0 1-6 0z", "M5 11a7 7 0 0 0 14 0M12 18v3") }
    val headphones by lazy { build("cam_fone", "M4 14v-4a8 8 0 0 1 16 0v4", "M3 14h3a1 1 0 0 1 1 1v4a1 1 0 0 1-1 1h-1a2 2 0 0 1-2-2z", "M21 14h-3a1 1 0 0 0-1 1v4a1 1 0 0 0 1 1h1a2 2 0 0 0 2-2z") }
    val people by lazy { build("pub_pessoas", "M5.5 8a3.5 3.5 0 1 0 7 0a3.5 3.5 0 1 0 -7 0", "M3 20c0-3.5 3-5.5 6-5.5", "M17 8v6M14 11h6") }
    val globe by lazy { build("pub_mundo", "M3.5 12a8.5 8.5 0 1 0 17 0a8.5 8.5 0 1 0 -17 0", "M3.5 12h17M12 3.5c3 3 3 14 0 17M12 3.5c-3 3-3 14 0 17") }
    val comment by lazy { build("pub_comentario", "M5 5h14a2 2 0 0 1 2 2v8a2 2 0 0 1-2 2h-7l-5 4v-4H5a2 2 0 0 1-2-2V7a2 2 0 0 1 2-2z") }
    val share by lazy { build("pub_compartilhar", "M5 12v7h14v-7M12 15V3M8 7l4-4 4 4") }
    val download by lazy { build("pub_baixar", "M12 4v11M7 11l5 5 5-5M5 20h14") }
    val phoneSave by lazy { build("pub_celular", "M6.5 5a2.5 2.5 0 0 1 2.5-2.5h6A2.5 2.5 0 0 1 17.5 5v14a2.5 2.5 0 0 1-2.5 2.5H9A2.5 2.5 0 0 1 6.5 19z", "M12 8v6M9.5 11.5L12 14l2.5-2.5") }
    val chevronRight by lazy { build("pub_seta", "M9 6l6 6-6 6") }
    fun effect(name: String): androidx.compose.ui.graphics.vector.ImageVector = when (name) {
        "Corações" -> heart
        "Confete" -> confetti
        "Neve" -> snow
        "Brilho" -> sparkle
        "Bolhas" -> bubbles
        "Chuva" -> rain
        else -> fire
    }
    private val heart by lazy { build("fx_coracao", "M12 20s-7-4.6-7-10a4 4 0 0 1 7-2.5A4 4 0 0 1 19 10c0 5.4-7 10-7 10z") }
    private val confetti by lazy { build("fx_confete", "M5 20l4-11 7 7zM13 5v3M18 8l-2 2M20 14h-3") }
    private val snow by lazy { build("fx_neve", "M12 3v18M4.5 7.5l15 9M4.5 16.5l15-9") }
    private val sparkle by lazy { build("fx_brilho", "M10 4l1.8 4.7L16.5 10.5l-4.7 1.8L10 17l-1.8-4.7L3.5 10.5l4.7-1.8z", "M18 3v4M16 5h4") }
    private val bubbles by lazy { build("fx_bolhas", "M4 14a5 5 0 1 0 10 0a5 5 0 1 0 -10 0", "M14 8a3 3 0 1 0 6 0a3 3 0 1 0 -6 0") }
    private val rain by lazy { build("fx_chuva", "M8 4v10M12 6v12M16 4v10M6 18v2M10 20v1M14 20v1M18 18v2") }
    private val fire by lazy { build("fx_fogo", "M12 3c2 4 5 5 5 9a5 5 0 0 1-10 0c0-2 1-3 2-4 0 2 1 3 2 3 0-3 0-5 1-8z") }
}

/** Item da lateral da câmera: nome à esquerda, ícone à direita. */
@Composable
private fun QinblyCameraSideItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, on: Boolean = false, dim: Boolean = false, compact: Boolean = false, onClick: () -> Unit) {
    Row(
        Modifier.clip(RoundedCornerShape(10.dp)).clickable { onClick() }.padding(horizontal = 2.dp, vertical = 2.dp).alpha(if (dim) 0.55f else 1f),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (label.isNotEmpty()) {
            Text(label, color = Color.White, fontSize = if (compact) 11.sp else 14.sp, fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.labelMedium.copy(shadow = androidx.compose.ui.graphics.Shadow(Color(0xCC000000), blurRadius = 4f)))
        }
        Icon(icon, contentDescription = label, tint = if (on) QinblyBrand.Accent else Color.White, modifier = Modifier.size(if (compact) 27.dp else 33.dp))
    }
}

/** Bolinha do carrossel de filtros (cores e efeitos animados). */
@Composable
private fun QinblyFilterBubble(label: String, selected: Boolean, onClick: () -> Unit, content: @Composable BoxScope.() -> Unit) {
    Column(
        Modifier.width(54.dp).clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            Modifier.size(46.dp).clip(CircleShape)
                .border(if (selected) 2.5.dp else 1.5.dp, if (selected) QinblyBrand.Accent else Color(0xB3FFFFFF), CircleShape),
            contentAlignment = Alignment.Center,
            content = content
        )
        Text(label, color = Color.White, fontSize = 9.sp, maxLines = 1,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
            style = MaterialTheme.typography.labelSmall.copy(shadow = androidx.compose.ui.graphics.Shadow(Color(0xCC000000), blurRadius = 4f)))
    }
}

/** Linha das folhas da câmera (Adicionar som). */
@Composable
private fun QinblyCameraSheetRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String, soon: Boolean = false, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(enabled = !soon) { onClick() }.padding(vertical = 10.dp).alpha(if (soon) 0.6f else 1f),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(Modifier.size(42.dp).clip(RoundedCornerShape(12.dp)).background(Color(0x331677E8)), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = QinblyBrand.Accent, modifier = Modifier.size(24.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(subtitle, color = Color(0xFFA9B5C8), fontSize = 11.sp)
        }
        if (soon) Text("Em breve", color = Color(0xFFA9B5C8), fontSize = 10.sp, fontWeight = FontWeight.Bold,
            modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(Color(0xFF222C3D)).padding(horizontal = 8.dp, vertical = 3.dp))
    }
}

/** Linha da tela "Nova publicação": ícone, título, e interruptor (checked != null) ou seta. */
@Composable
private fun QinblyPublishRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String = "", value: String = "", checked: Boolean? = null, onChecked: (Boolean) -> Unit = {}, onClick: () -> Unit = {}) {
    Row(
        Modifier.fillMaxWidth().clickable { if (checked != null) onChecked(!checked) else onClick() }.padding(vertical = 11.dp, horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(icon, contentDescription = null, tint = QiblyTheme.text, modifier = Modifier.size(22.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = QiblyTheme.text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            if (subtitle.isNotEmpty()) Text(subtitle, color = Muted, fontSize = 11.sp)
        }
        if (checked != null) {
            Switch(
                checked = checked, onCheckedChange = { onChecked(it) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White, checkedTrackColor = QinblyBrand.Accent,
                    uncheckedThumbColor = Color.White, uncheckedTrackColor = Color(0xFFB8C0CC), uncheckedBorderColor = Color.Transparent
                )
            )
        } else {
            if (value.isNotEmpty()) Text(value, color = Muted, fontSize = 12.sp)
            Icon(QinblyCameraIcons.chevronRight, contentDescription = null, tint = Muted, modifier = Modifier.size(16.dp))
        }
    }
    Box(Modifier.fillMaxWidth().height(0.5.dp).background(QiblyTheme.border))
}

/** Guarda uma cópia da foto/vídeo na galeria do celular (pasta Qinbly). Android 10 ou mais novo. */
private fun qinblySaveMediaCopy(context: android.content.Context, source: Uri, isVideo: Boolean): Boolean {
    if (android.os.Build.VERSION.SDK_INT < 29) return false
    return try {
        val resolver = context.contentResolver
        val collection = if (isVideo) android.provider.MediaStore.Video.Media.getContentUri(android.provider.MediaStore.VOLUME_EXTERNAL_PRIMARY)
            else android.provider.MediaStore.Images.Media.getContentUri(android.provider.MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val values = android.content.ContentValues().apply {
            put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, "qinbly-" + System.currentTimeMillis() + (if (isVideo) ".mp4" else ".jpg"))
            put(android.provider.MediaStore.MediaColumns.MIME_TYPE, if (isVideo) "video/mp4" else "image/jpeg")
            put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, (if (isVideo) android.os.Environment.DIRECTORY_MOVIES else android.os.Environment.DIRECTORY_PICTURES) + "/Qinbly")
            put(android.provider.MediaStore.MediaColumns.IS_PENDING, 1)
        }
        val target = resolver.insert(collection, values) ?: return false
        val input = resolver.openInputStream(source) ?: return false
        input.use { ins -> (resolver.openOutputStream(target) ?: return false).use { out -> ins.copyTo(out) } }
        values.clear()
        values.put(android.provider.MediaStore.MediaColumns.IS_PENDING, 0)
        resolver.update(target, values, null, null)
        true
    } catch (_: Exception) { false }
}

/** Efeitos animados por cima da câmera: partículas desenhadas em tempo real (sem rastrear o rosto). */
@Composable
private fun QinblyEffectsOverlay(effect: String, modifier: Modifier = Modifier) {
    if (effect.isBlank() || effect == "Original") return
    var time by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(effect) {
        var last = 0L
        while (true) {
            withFrameNanos { now ->
                if (last != 0L) time += (now - last) / 1_000_000_000f
                last = now
            }
        }
    }
    val particles = remember(effect) {
        val rnd = kotlin.random.Random(effect.hashCode())
        List(28) { floatArrayOf(rnd.nextFloat(), rnd.nextFloat(), 0.5f + rnd.nextFloat(), rnd.nextFloat() * 6.28f, rnd.nextInt(5).toFloat()) }
    }
    val confettiColors = listOf(Color(0xFFFF5C8A), Color(0xFFFFD54F), Color(0xFF5CC8FF), Color(0xFF7CE38B), Color(0xFFB18CFF))
    androidx.compose.foundation.Canvas(modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val d = density
        particles.forEach { p ->
            val speed = p[2]
            val phase = p[3]
            when (effect) {
                "Corações" -> {
                    val prog = (p[1] + time * 0.10f * speed) % 1f
                    val x = p[0] * w + kotlin.math.sin(time * speed + phase) * 16f * d
                    val y = prog * (h + 80f * d) - 40f * d
                    val s = (9f + 9f * speed) * d
                    val a = (1f - kotlin.math.abs(prog - 0.5f) * 1.4f).coerceIn(0.15f, 1f)
                    val c = Color(0xFFFF5C8A).copy(alpha = a)
                    drawCircle(c, s * 0.27f, Offset(x - s * 0.23f, y - s * 0.1f))
                    drawCircle(c, s * 0.27f, Offset(x + s * 0.23f, y - s * 0.1f))
                    val path = androidx.compose.ui.graphics.Path().apply {
                        moveTo(x - s * 0.49f, y - s * 0.02f)
                        lineTo(x + s * 0.49f, y - s * 0.02f)
                        lineTo(x, y + s * 0.55f)
                        close()
                    }
                    drawPath(path, c)
                }
                "Confete" -> {
                    val prog = (p[1] + time * 0.14f * speed) % 1f
                    val x = p[0] * w + kotlin.math.sin(time * speed * 1.3f + phase) * 22f * d
                    val y = prog * (h + 60f * d) - 30f * d
                    val c = confettiColors[p[4].toInt() % 5]
                    withTransform({ rotate(time * 140f * speed + phase * 50f, Offset(x, y)) }) {
                        drawRect(c, Offset(x - 4f * d, y - 7f * d), androidx.compose.ui.geometry.Size(8f * d, 14f * d))
                    }
                }
                "Neve" -> {
                    val prog = (p[1] + time * 0.07f * speed) % 1f
                    val x = p[0] * w + kotlin.math.sin(time * 0.8f * speed + phase) * 20f * d
                    val y = prog * (h + 40f * d) - 20f * d
                    drawCircle(Color.White.copy(alpha = 0.85f), (2f + 2.5f * speed) * d, Offset(x, y))
                }
                "Chuva" -> {
                    val prog = (p[1] + time * 0.55f * speed) % 1f
                    val x = p[0] * w
                    val y = prog * (h + 40f * d) - 20f * d
                    drawLine(Color(0xCCCFE6FF), Offset(x, y), Offset(x - 5f * d, y + 20f * d), strokeWidth = 1.6f * d)
                }
                "Brilho" -> {
                    val x = p[0] * w
                    val y = p[1] * h
                    val a = (0.5f + 0.5f * kotlin.math.sin(time * 2f * speed + phase)).coerceIn(0f, 1f)
                    val s = (6f + 8f * speed) * d * a
                    val c = Color.White.copy(alpha = a)
                    drawLine(c, Offset(x - s, y), Offset(x + s, y), strokeWidth = 2f * d)
                    drawLine(c, Offset(x, y - s), Offset(x, y + s), strokeWidth = 2f * d)
                    drawCircle(c, 2f * d, Offset(x, y))
                }
                "Bolhas" -> {
                    val prog = ((p[1] - time * 0.07f * speed) % 1f + 1f) % 1f
                    val x = p[0] * w + kotlin.math.sin(time * speed + phase) * 12f * d
                    val y = prog * h
                    drawCircle(Color.White.copy(alpha = 0.75f), (6f + 10f * speed) * d, Offset(x, y),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.6f * d))
                }
                "Fogo" -> {
                    val prog = (p[1] + time * 0.22f * speed) % 1f
                    val x = p[0] * w + kotlin.math.sin(time * 2f * speed + phase) * 10f * d
                    val y = h - prog * h * 0.6f
                    val r = (7f - 5f * prog) * d * (0.6f + 0.4f * speed)
                    val c = androidx.compose.ui.graphics.lerp(Color(0xFFFF6A00), Color(0xFFFFD54F), prog)
                    drawCircle(c.copy(alpha = (1f - prog).coerceIn(0f, 1f)), r, Offset(x, y))
                }
                else -> {}
            }
        }
    }
}

@Composable
private fun PostPage(onNotice: (String) -> Unit, onCameraCancelled: () -> Unit, initialMode: String = "camera", onSwitchMode: (String) -> Unit = {}) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val prefs = remember { context.getSharedPreferences("qinbly_session", android.content.Context.MODE_PRIVATE) }
    var caption by remember { mutableStateOf(QinblyComposeSeed.text.also { QinblyComposeSeed.text = "" }) }
    var captionCursor by remember { mutableIntStateOf(caption.length) }
    var pollOptions by remember { mutableStateOf(listOf("", "")) }
    var pollDurationHours by remember { mutableIntStateOf(24) }
    var pollDurationExpanded by remember { mutableStateOf(false) }
    var pollMultipleAnswers by remember { mutableStateOf(false) }
    var pollShowResults by remember { mutableStateOf(true) }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var sourceUri by remember { mutableStateOf<Uri?>(null) }
    var selectedType by remember { mutableStateOf("") }
    var publishing by remember { mutableStateOf(false) }
    var cameraGranted by remember { mutableStateOf(androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED) }
    var cameraError by remember { mutableStateOf("") }
    var lensFacing by remember { mutableIntStateOf(androidx.camera.core.CameraSelector.LENS_FACING_BACK) }
    var selectedFilter by remember { mutableStateOf("Original") }
    var selectedEffect by remember { mutableStateOf("Original") }
    var selectedFace by remember { mutableStateOf("Nenhum") }
    var stabilizeOn by remember { mutableStateOf(false) }
    var stabilizeSupported by remember { mutableStateOf(false) }
    val faceState = remember { QinblyFaceState() }
    val faceTracker = remember { QinblyFaceTracker3D(context, faceState.track) }
    val faceAnalyzer = remember { QinblyFaceAnalyzer(faceState, faceTracker) }
    val faceExecutor = remember { java.util.concurrent.Executors.newSingleThreadExecutor() }
    faceAnalyzer.enabled = selectedFace != "Nenhum"
    faceAnalyzer.use3d = selectedFace == "Cachorro"
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
    var sideExpanded by remember { mutableStateOf(false) }
    var filtersVisible by remember { mutableStateOf(true) }
    var cameraSheet by remember { mutableStateOf("") }
    var filterIntensity by remember { mutableFloatStateOf(1f) }
    var gridOn by remember { mutableStateOf(false) }
    var mirrorOn by remember { mutableStateOf(false) }
    var handsFreeOn by remember { mutableStateOf(false) }
    var exposureIndex by remember { mutableIntStateOf(0) }
    var nightOn by remember { mutableStateOf(false) }
    var retouchOn by remember { mutableStateOf(false) }
    var bgBlurOn by remember { mutableStateOf(false) }
    var hdrOn by remember { mutableStateOf(false) }
    var collageCount by remember { mutableIntStateOf(0) }
    val collageShots = remember { mutableStateListOf<Uri>() }
    var qualityChoice by remember { mutableStateOf("HD") }
    var countdown by remember { mutableIntStateOf(0) }
    var pendingAction by remember { mutableStateOf("") }
    var recElapsed by remember { mutableIntStateOf(0) }
    var holdStarted by remember { mutableStateOf(false) }
    var stopWhenReady by remember { mutableStateOf(false) }
    var showZoomBadge by remember { mutableStateOf(false) }
    var flashLabel by remember { mutableStateOf("") }
    var soundName by remember { mutableStateOf("") }
    var soundUri by remember { mutableStateOf<Uri?>(null) }
    var speedChoice by remember { mutableFloatStateOf(1f) }
    var latestThumb by remember { mutableStateOf<Bitmap?>(null) }
    val soundPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            var label = "Som escolhido"
            try {
                context.contentResolver.query(uri, arrayOf(android.provider.OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c ->
                    if (c.moveToFirst()) label = c.getString(0)?.substringBeforeLast('.') ?: label
                }
            } catch (_: Exception) { }
            soundName = label
            soundUri = uri
            cameraSheet = ""
            onNotice("Som escolhido. Ele será misturado ao vídeo que você gravar.")
        }
    }
    var showComposer by remember { mutableStateOf(false) }
    var publishSettings by remember { mutableStateOf(false) }
    var editorTool by remember { mutableStateOf("Filtros") }
    var allowComments by remember { mutableStateOf(true) }
    var allowSharing by remember { mutableStateOf(true) }
    var saveToDevice by remember { mutableStateOf(false) }
    var allowSaveOthers by remember { mutableStateOf(true) }
    var postVisibility by remember { mutableStateOf("Todos") }
    val taggedPeople = remember { mutableStateListOf<String>() }
    var showTagDialog by remember { mutableStateOf(false) }
    var coverPicker by remember { mutableStateOf(false) }
    var coverIndex by remember { mutableIntStateOf(0) }
    var coverBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var videoDurationLabel by remember { mutableStateOf("") }
    var editorText by remember { mutableStateOf("") }
    var editorTextFont by remember { mutableStateOf("Forte") }
    var editorTextColor by remember { mutableIntStateOf(android.graphics.Color.WHITE) }
    val editorWordColors = remember { androidx.compose.runtime.mutableStateMapOf<Int, Int>() }
    var editorSelWord by remember { mutableIntStateOf(-1) }
    var camTextMode by remember { mutableStateOf(false) }
    var camTextEditing by remember { mutableStateOf(false) }
    var editorSticker by remember { mutableStateOf("") }
    var editorRatio by remember { mutableStateOf("Original") }
    var editorZoom by remember { mutableFloatStateOf(1f) }
    var editorPanX by remember { mutableFloatStateOf(0f) }
    var editorPanY by remember { mutableFloatStateOf(0f) }
    var editorBrightness by remember { mutableFloatStateOf(0f) }
    var editorContrast by remember { mutableFloatStateOf(0f) }
    var editorImageRatio by remember { mutableFloatStateOf(1f) }
    var bakedUri by remember { mutableStateOf<Uri?>(null) }
    var cropOpen by remember { mutableStateOf(false) }
    var editorBusy by remember { mutableStateOf(false) }
    var cropRatio by remember { mutableStateOf("Original") }
    var cropZoom by remember { mutableFloatStateOf(1f) }
    var cropPanX by remember { mutableFloatStateOf(0f) }
    var cropPanY by remember { mutableFloatStateOf(0f) }
    val editorStrokes = remember { mutableStateListOf<List<Offset>>() }
    var publishSuccess by remember { mutableStateOf(false) }
    var publishError by remember { mutableStateOf("") }
    var selectedMediaReady by remember { mutableStateOf(false) }
    var galleryOpen by remember { mutableStateOf(false) }
    var galleryFilter by remember { mutableStateOf("Todos") }
    var galleryAssets by remember { mutableStateOf<List<QinblyGalleryAsset>>(emptyList()) }
    var galleryLoading by remember { mutableStateOf(false) }
    var videoFrames by remember { mutableStateOf<List<Bitmap>>(emptyList()) }
    val gallerySelection = remember { mutableStateListOf<Uri>() }
    var photoVideoOpen by remember { mutableStateOf(false) }
    var photoVideoBusy by remember { mutableStateOf(false) }
    var photoVideoPending by remember { mutableStateOf<List<Uri>>(emptyList()) }
    val photoVideoClips = remember { mutableStateListOf<PvClip>() }
    val addVideoPhotos = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(20)) { uris ->
        if (uris.isNotEmpty()) photoVideoPending = uris
    }
    val cameraPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        cameraGranted = granted
        if (!granted) cameraError = "Permita o acesso à câmera para usar a câmera do ${QinblyBrand.Name}."
    }
    val galleryPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
        val granted = results.values.any { it }
        if (granted) {
            galleryAssets = emptyList()
            galleryOpen = true
        } else {
            onNotice("Permita o acesso às fotos e vídeos para abrir a galeria do ${QinblyBrand.Name}.")
        }
    }
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) { sourceUri = uri; selectedUri = uri; selectedType = "foto"; showComposer = true }
    }
    val videoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) { sourceUri = uri; selectedUri = uri; showComposer = true; selectedType = "vídeo" }
    }
    // Seletor do Android: não depende de permissão de acesso a toda a biblioteca.
    val multiPhotoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(10)) { uris ->
        if (uris.isNotEmpty()) {
            gallerySelection.clear()
            gallerySelection.addAll(uris.take(10))
            sourceUri = uris.first()
            selectedUri = uris.first()
            selectedType = "foto"
            galleryOpen = false
            publishSettings = false
            showComposer = true
        } else if (initialMode == "gallery" && !showComposer) {
            onCameraCancelled()
        }
    }
    val mediaPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            sourceUri = uri
            selectedUri = uri
            selectedType = if (context.contentResolver.getType(uri)?.startsWith("video/") == true) "vídeo" else "foto"
            galleryOpen = false
            publishSettings = false
            showComposer = true
        } else if (initialMode == "gallery" && !showComposer) {
            onCameraCancelled()
        }
    }
    fun openQinblyGallery() {
        try { multiPhotoPicker.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
        catch (e: Exception) { onNotice("Não foi possível abrir a galeria: " + (e.localizedMessage ?: "tente novamente")) }
    }
    fun returnToGallery() {
        publishSettings = false
        showComposer = false
        editorTool = "Filtros"
        openQinblyGallery()
    }
    // O editor é o mesmo para foto da galeria e foto/vídeo da câmera; só muda para onde ele volta.
    val newEditorChrome = initialMode == "gallery" || initialMode == "camera"
    fun leaveEditor() {
        if (initialMode == "gallery") returnToGallery()
        else {
            publishSettings = false
            showComposer = false
            editorTool = "Filtros"
            sourceUri = null; selectedUri = null; bakedUri = null
            editorText = ""; editorSticker = ""; editorWordColors.clear(); editorSelWord = -1
            editorStrokes.clear()
            camTextMode = false; camTextEditing = false
        }
    }
    BackHandler(enabled = newEditorChrome && showComposer) {
        leaveEditor()
    }
    LaunchedEffect(initialMode) {
        if (initialMode == "gallery") openQinblyGallery()
    }
    LaunchedEffect(sourceUri) {
        bakedUri = null
        editorRatio = "Original"; editorZoom = 1f; editorPanX = 0f; editorPanY = 0f
        editorBrightness = 0f; editorContrast = 0f
    }
    LaunchedEffect(selectedUri, selectedType) {
        val current = selectedUri
        if (current != null && selectedType == "foto") {
            editorImageRatio = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                try {
                    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    context.contentResolver.openInputStream(current)?.use { BitmapFactory.decodeStream(it, null, bounds) }
                    if (bounds.outWidth > 0 && bounds.outHeight > 0) bounds.outWidth.toFloat() / bounds.outHeight else 1f
                } catch (_: Exception) { 1f }
            }
        }
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
                extractQinblyVideoFrames(context, uri)
            }
        } else {
            videoFrames = emptyList()
        }
    }
    LaunchedEffect(sourceUri, selectedType) {
        coverBitmap = null
        coverIndex = 0
        val uri = sourceUri
        videoDurationLabel = if (uri != null && selectedType == "vídeo") kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            val retriever = android.media.MediaMetadataRetriever()
            try {
                retriever.setDataSource(context, uri)
                val ms = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
                val total = (ms / 1000L).toInt()
                if (total > 0) "%d:%02d".format(total / 60, total % 60) else ""
            } catch (_: Exception) { "" } finally { try { retriever.release() } catch (_: Exception) { } }
        } else ""
    }
    LaunchedEffect(galleryOpen) {
        if (galleryOpen && galleryAssets.isEmpty()) {
            galleryLoading = true
            val assets = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                val result = mutableListOf<QinblyGalleryAsset>()
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
                            result.add(QinblyGalleryAsset(
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
            val file = File(context.cacheDir, "qinbly-video-" + System.currentTimeMillis() + ".mp4")
            val output = androidx.camera.video.FileOutputOptions.Builder(file).build()
            var pending = video.output.prepareRecording(context, output)
            val audioGranted = androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) == android.content.pm.PackageManager.PERMISSION_GRANTED
            if (audioGranted) pending = pending.withAudioEnabled()
            activeRecording = pending.start(androidx.core.content.ContextCompat.getMainExecutor(context)) { event ->
                when (event) {
                    is androidx.camera.video.VideoRecordEvent.Start -> {
                        isRecording = true
                        cameraError = ""
                        if (flashEnabled) cameraControl?.cameraControl?.enableTorch(true)
                        if (stopWhenReady) { stopWhenReady = false; activeRecording?.stop() }
                    }
                    is androidx.camera.video.VideoRecordEvent.Finalize -> {
                        isRecording = false
                        holdStarted = false
                        try { cameraControl?.cameraControl?.enableTorch(false) } catch (_: Exception) { }
                        activeRecording = null
                        if (event.hasError()) cameraError = "Falha ao finalizar o vídeo (" + event.error + ")."
                        else {
                            val speedNow = speedChoice
                            val soundNow = soundUri
                            if (speedNow != 1f || soundNow != null) {
                                flashLabel = "Aplicando velocidade e som…"
                                qinblyReencodeVideo(context, Uri.fromFile(file), speedNow, soundNow) { made, error ->
                                    if (made == null && error.isNotBlank()) onNotice(error)
                                    val finalUri = made ?: Uri.fromFile(file)
                                    sourceUri = finalUri; selectedUri = finalUri; selectedType = "vídeo"; showComposer = true
                                }
                            } else { sourceUri = Uri.fromFile(file); selectedUri = sourceUri; selectedType = "vídeo"; showComposer = true }
                        }
                    }
                }
            }
        } catch (e: Exception) { cameraError = "Não foi possível gravar o vídeo: " + (e.localizedMessage ?: "erro desconhecido") }
    }

    // Ao sair da tela Criar a câmera é solta de verdade (antes ela ficava ligada no Activity).
    androidx.compose.runtime.DisposableEffect(Unit) {
        onDispose {
            try { if (isRecording) activeRecording?.stop() } catch (_: Exception) {}
            try { cameraProvider?.unbindAll() } catch (_: Exception) {}
        }
    }
    val audioPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) startVideoRecording()
        else cameraError = "Permita o microfone para gravar vídeos com áudio."
    }
    LaunchedEffect(cameraGranted, lensFacing, showComposer, previewView, selectedCameraFormat, qualityChoice, stabilizeOn) {
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
                        val quality = when (qualityChoice) { "UHD" -> androidx.camera.video.Quality.UHD; "FHD" -> androidx.camera.video.Quality.FHD; else -> androidx.camera.video.Quality.HD }
                        val recorder = androidx.camera.video.Recorder.Builder()
                            .setQualitySelector(androidx.camera.video.QualitySelector.from(quality, androidx.camera.video.FallbackStrategy.lowerQualityOrHigherThan(androidx.camera.video.Quality.SD)))
                            .build()
                        stabilizeSupported = try { androidx.camera.video.Recorder.getVideoCapabilities(provider.getCameraInfo(androidx.camera.core.CameraSelector.Builder().requireLensFacing(lensFacing).build())).isStabilizationSupported } catch (_: Exception) { false }
                        val video = androidx.camera.video.VideoCapture.Builder(recorder).setVideoStabilizationEnabled(stabilizeOn && stabilizeSupported).build()
                        val selector = androidx.camera.core.CameraSelector.Builder().requireLensFacing(lensFacing).build()
                        val frame = when (selectedCameraFormat) { "1:1" -> android.util.Rational(1, 1); "4:3" -> android.util.Rational(3, 4); else -> android.util.Rational(9, 16) }
                        val viewPort = androidx.camera.core.ViewPort.Builder(frame, view.display?.rotation ?: android.view.Surface.ROTATION_0).build()
                        val analysis = androidx.camera.core.ImageAnalysis.Builder()
                            .setBackpressureStrategy(androidx.camera.core.ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build().also { it.setAnalyzer(faceExecutor, faceAnalyzer) }
                        fun buildGroup(withAnalysis: Boolean): androidx.camera.core.UseCaseGroup {
                            val b = androidx.camera.core.UseCaseGroup.Builder().setViewPort(viewPort)
                                .addUseCase(preview).addUseCase(capture).addUseCase(video)
                            if (withAnalysis) b.addUseCase(analysis)
                            return b.build()
                        }
                        provider.unbindAll()
                        // Alguns aparelhos não aceitam 4 usos da câmera juntos: nesse caso segue sem o filtro de rosto.
                        val boundCamera = try {
                            provider.bindToLifecycle(lifecycleOwner, selector, buildGroup(true))
                        } catch (_: Exception) {
                            provider.unbindAll()
                            provider.bindToLifecycle(lifecycleOwner, selector, buildGroup(false))
                        }
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

    LaunchedEffect(exposureIndex, cameraControl, nightOn) {
        try {
            val range = cameraControl?.cameraInfo?.exposureState?.exposureCompensationRange
            val boosted = if (nightOn && range != null) maxOf(exposureIndex, (range.upper * 2 / 3).coerceAtLeast(1).coerceAtMost(range.upper)) else exposureIndex
            cameraControl?.cameraControl?.setExposureCompensationIndex(boosted)
        } catch (_: Exception) { }
    }
    LaunchedEffect(zoomRatio) {
        if (zoomRatio > 1.01f) {
            showZoomBadge = true
            kotlinx.coroutines.delay(1200)
            showZoomBadge = false
        }
    }
    LaunchedEffect(flashLabel) {
        if (flashLabel.isNotBlank()) {
            kotlinx.coroutines.delay(1400)
            flashLabel = ""
        }
    }
    LaunchedEffect(isRecording) {
        if (isRecording) {
            recElapsed = 0
            while (isRecording) {
                kotlinx.coroutines.delay(1000)
                recElapsed += 1
                val limit = if (captureMode == "Foto") 60 else when (selectedDuration) { "10 min" -> 600; "60 s" -> 60; else -> 15 }
                if (recElapsed >= limit) activeRecording?.stop()
            }
        } else {
            recElapsed = 0
        }
    }
    // Miniatura da última foto/vídeo da galeria (só aparece se o app já tiver permissão de leitura).
    LaunchedEffect(cameraGranted, showComposer) {
        if (initialMode != "camera" || showComposer) return@LaunchedEffect
        val perm = if (android.os.Build.VERSION.SDK_INT >= 33) android.Manifest.permission.READ_MEDIA_IMAGES else android.Manifest.permission.READ_EXTERNAL_STORAGE
        if (androidx.core.content.ContextCompat.checkSelfPermission(context, perm) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            latestThumb = null
            return@LaunchedEffect
        }
        latestThumb = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val resolver = context.contentResolver
                val files = android.provider.MediaStore.Files.getContentUri("external")
                val typeCol = android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE
                val projection = arrayOf(android.provider.MediaStore.Files.FileColumns._ID, typeCol)
                val selection = "$typeCol=? OR $typeCol=?"
                val args = arrayOf(
                    android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
                    android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString()
                )
                var thumb: Bitmap? = null
                resolver.query(files, projection, selection, args, android.provider.MediaStore.MediaColumns.DATE_ADDED + " DESC")?.use { c ->
                    if (c.moveToFirst()) {
                        val id = c.getLong(0)
                        val isVideo = c.getInt(1) == android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO
                        val base = if (isVideo) android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI else android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        val uri = android.content.ContentUris.withAppendedId(base, id)
                        thumb = if (android.os.Build.VERSION.SDK_INT >= 29) {
                            resolver.loadThumbnail(uri, android.util.Size(160, 160), null)
                        } else if (isVideo) {
                            android.provider.MediaStore.Video.Thumbnails.getThumbnail(resolver, id, android.provider.MediaStore.Video.Thumbnails.MICRO_KIND, null)
                        } else {
                            android.provider.MediaStore.Images.Thumbnails.getThumbnail(resolver, id, android.provider.MediaStore.Images.Thumbnails.MICRO_KIND, null)
                        }
                    }
                }
                thumb
            } catch (_: Exception) { null }
        }
    }

    fun takePhoto() {
        val capture = imageCapture
        if (capture == null) { cameraError = "A câmera ainda está iniciando. Tente novamente."; return }
        val file = File(context.cacheDir, "qinbly-camera-" + System.currentTimeMillis() + ".jpg")
        val metadata = androidx.camera.core.ImageCapture.Metadata().apply {
            isReversedHorizontal = mirrorOn && lensFacing == androidx.camera.core.CameraSelector.LENS_FACING_FRONT
        }
        val output = androidx.camera.core.ImageCapture.OutputFileOptions.Builder(file).setMetadata(metadata).build()
        capture.takePicture(output, androidx.core.content.ContextCompat.getMainExecutor(context), object : androidx.camera.core.ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(result: androidx.camera.core.ImageCapture.OutputFileResults) {
                fun openEditor(uri: Uri) {
                    sourceUri = uri
                    selectedUri = uri
                    selectedType = "foto"
                    showComposer = true
                }
                fun finishPhoto(uri: Uri) {
                    if (collageCount > 0) {
                        collageShots.add(uri)
                        if (collageShots.size >= collageCount) {
                            val list = collageShots.toList()
                            collageShots.clear()
                            flashLabel = "Montando a colagem…"
                            qinblyBuildCollage(context, list) { made -> if (made != null) openEditor(made) else onNotice("Não foi possível montar a colagem.") }
                        } else flashLabel = "Colagem: " + collageShots.size + " de " + collageCount + " fotos"
                    } else openEditor(uri)
                }
                fun withEffects(uri: Uri) {
                    if (retouchOn || bgBlurOn || hdrOn) {
                        flashLabel = "Aplicando efeitos…"
                        qinblyApplyPhotoEffects(context, uri, retouchOn, bgBlurOn, hdrOn) { fx, note ->
                            if (note.isNotBlank()) onNotice(note)
                            finishPhoto(fx ?: uri)
                        }
                    } else finishPhoto(uri)
                }
                if (selectedFace != "Nenhum") {
                    flashLabel = "Aplicando filtro…"
                    qinblyBakeFaceFilter(context, file, selectedFace) { baked -> withEffects(baked) }
                } else {
                    withEffects(Uri.fromFile(file))
                }
            }
            override fun onError(exception: androidx.camera.core.ImageCaptureException) { cameraError = "Falha ao tirar foto: " + (exception.message ?: "tente novamente") }
        })
    }
    fun requestRecording() {
        val audioGranted = androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) == android.content.pm.PackageManager.PERMISSION_GRANTED
        if (audioGranted) startVideoRecording() else audioPermission.launch(android.Manifest.permission.RECORD_AUDIO)
    }
    val timerSeconds = selectedTimerSeconds.filter { it.isDigit() }.toIntOrNull() ?: 0
    fun onShutterTap() {
        if (isRecording) { activeRecording?.stop(); return }
        if (pendingAction.isNotBlank()) { pendingAction = ""; countdown = 0; return }
        val action = if (captureMode == "Vídeo" || handsFreeOn) "video" else "photo"
        if (timerSeconds > 0 || (handsFreeOn && action == "video")) pendingAction = action
        else if (action == "photo") takePhoto() else requestRecording()
    }
    LaunchedEffect(pendingAction) {
        if (pendingAction.isNotBlank()) {
            val wait = if (timerSeconds > 0) timerSeconds else 3
            for (i in wait downTo 1) {
                countdown = i
                kotlinx.coroutines.delay(1000)
            }
            countdown = 0
            val action = pendingAction
            pendingAction = ""
            if (action == "photo") takePhoto() else if (action == "video") requestRecording()
        } else {
            countdown = 0
        }
    }

    if (initialMode == "text" || initialMode == "poll") {
 QinblySplit {
        val maxChars = if (initialMode == "poll") 200 else 5000
        val ownUsername = prefs.getString("username", "").orEmpty().removePrefix("@")
        Column(
            Modifier.fillMaxSize().background(QiblyTheme.background).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
 QinblySplit {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onCameraCancelled) { Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = QiblyTheme.text) }
                Text(if (initialMode == "poll") "Criar enquete" else "Nova publicação",
                    color = QiblyTheme.text, fontWeight = FontWeight.Bold, fontSize = 22.sp, modifier = Modifier.weight(1f))
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (ownUsername.isNotBlank()) QiblyAccountAvatar(ownUsername, "", 46.dp) { }
                else Icon(Icons.Default.Person, contentDescription = null, tint = Color.DarkGray, modifier = Modifier.size(46.dp))
                Column {
                    Text(if (ownUsername.isBlank()) "Meu perfil" else "@$ownUsername", fontWeight = FontWeight.Bold, color = QiblyTheme.text)
                    Text("Todos podem ver", color = Color.DarkGray, style = MaterialTheme.typography.labelMedium)
                }
            }
            val capState = remember { QinblyCaptionState(caption) }
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(QinblyBrand.Accent.copy(alpha = 0.06f)).padding(16.dp)) {
                QinblyLegendField(
                    capState, maxChars, if (initialMode == "poll") "Qual é a sua pergunta?" else "No que você está pensando?", QiblyTheme.text,
                    if (initialMode == "poll") 20.sp else 18.sp,
                    Modifier.fillMaxWidth().heightIn(min = if (initialMode == "text") 200.dp else 70.dp)
                ) { caption = it }
                Text("${caption.length} / $maxChars", modifier = Modifier.align(Alignment.End), color = Color.Gray, style = MaterialTheme.typography.labelSmall)
            }
            QinblyHashtagField(capState) { caption = it }
            if (initialMode == "poll") {
 QinblySplit {
                Text("Opções", color = QiblyTheme.text, fontWeight = FontWeight.Bold)
                pollOptions.forEachIndexed { index, option ->
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(Modifier.size(28.dp).clip(CircleShape).background(QinblyBrand.Accent.copy(alpha = 0.14f)), contentAlignment = Alignment.Center) {
                            Text("${index + 1}", color = QinblyBrand.Accent, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        OutlinedTextField(value = option,
                            onValueChange = { value -> pollOptions = pollOptions.toMutableList().also { it[index] = value.take(60) } },
                            placeholder = { Text("Opção ${index + 1}") }, modifier = Modifier.weight(1f), singleLine = true,
                            shape = RoundedCornerShape(16.dp))
                        if (pollOptions.size > 2) IconButton(onClick = { pollOptions = pollOptions.filterIndexed { i, _ -> i != index } }) {
                            Icon(Icons.Default.Close, contentDescription = "Remover opção", tint = Color.Gray)
                        }
                    }
                }
                if (pollOptions.size < 6) Row(
                    Modifier.clip(RoundedCornerShape(20.dp)).clickable { pollOptions = pollOptions + "" }.padding(horizontal = 6.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = QinblyBrand.Accent)
                    Text("Adicionar opção", color = QinblyBrand.Accent, fontWeight = FontWeight.SemiBold)
                }
                Text("Duração", color = QiblyTheme.text, fontWeight = FontWeight.Bold)
                val pollDurations = listOf(1 to "1 h", 6 to "6 h", 24 to "24 h", 72 to "3 dias", 168 to "7 dias")
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    pollDurations.forEach { (hours, label) ->
                        val on = pollDurationHours == hours
                        Text(label, color = if (on) Color.White else QiblyTheme.text, fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(if (on) QinblyBrand.Accent else QinblyBrand.Accent.copy(alpha = 0.10f))
                                .clickable { pollDurationHours = hours }.padding(horizontal = 16.dp, vertical = 9.dp))
                    }
                }
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Permitir múltiplas respostas", modifier = Modifier.weight(1f), color = QiblyTheme.text)
                    Switch(checked = pollMultipleAnswers, onCheckedChange = { pollMultipleAnswers = it })
                }
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Mostrar resultados ao votar", modifier = Modifier.weight(1f), color = QiblyTheme.text)
                    Switch(checked = pollShowResults, onCheckedChange = { pollShowResults = it })
                }
                Text("Os votos são anônimos. A enquete continua no feed depois de encerrar.", color = Color.Gray, style = MaterialTheme.typography.labelSmall)
             }
} else {
                Text("Dica: use #hashtags para ser encontrado e @nome para marcar alguém.", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
            }
            Button(
                onClick = {
                    val token = prefs.getString("token", null)
                    if (token.isNullOrBlank()) {
                        onNotice("Entre na sua conta antes de publicar.")
                    } else if (!publishing) {
                        publishing = true
                        QinblyHashtagStore.record(context, caption)
                        Thread {
                            try {
                                if (initialMode == "poll") {
                                    // A API atual não expõe votação; preserva pergunta e opções como publicação textual.
                                    val options = pollOptions.filter { it.isNotBlank() }
                                    QinblyApi.createPollPost(token, caption.trim(), options, pollDurationHours, pollMultipleAnswers, pollShowResults)
                                } else {
                                    QinblyApi.createTextPost(token, caption.trim(), if (postVisibility == "Só eu") "private" else "public", taggedPeople.toList())
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
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = QinblyBrand.Accent, contentColor = Color.White)
            ) { Text(if (publishing) "Publicando…" else "Publicar", fontWeight = FontWeight.Bold) }
         }
}
        return@QinblySplit
     }
}

    // A tela intermediária foi removida. Se o seletor do Android for cancelado,
    // voltamos ao início em vez de deixar uma página vazia.
    if (initialMode == "gallery" && !showComposer && !galleryOpen) {
        Box(Modifier.fillMaxSize().background(QiblyTheme.background))
        return
    }

    if (initialMode == "camera" && !cameraGranted && !showComposer) {
        Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(60.dp))
            Text("Câmera ${QinblyBrand.Name}", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Text("Câmera integrada ao aplicativo, com prévia e filtros.", color = Muted)
            Button(onClick = { cameraPermission.launch(android.Manifest.permission.CAMERA) }) { Text("Permitir câmera") }
            OutlinedButton(onClick = { imagePicker.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }) { Text("Escolher foto da galeria") }
            OutlinedButton(onClick = { videoPicker.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)) }) { Text("Escolher vídeo da galeria") }
            TextButton(onClick = onCameraCancelled) { Text("Voltar ao início") }
        }
        return
    }

    if (initialMode == "camera" && !showComposer) {
 QinblySplit {
        val frameRatio = when (selectedCameraFormat) { "1:1" -> 1f; "4:3" -> 3f / 4f; else -> 9f / 16f }
        val sideScroll = rememberScrollState()
        val bubbleItems = remember { QinblyColorFilterNames.map { it to false } + QinblyAnimatedEffectNames.map { it to true } }
        BackHandler(enabled = cameraSheet.isNotBlank()) { cameraSheet = "" }
        Box(Modifier.fillMaxSize().background(Color(0xFF080808))) {
 QinblySplit {
            Box((if (selectedCameraFormat == "9:16") Modifier.fillMaxSize() else Modifier.align(Alignment.Center).fillMaxWidth().aspectRatio(frameRatio)).clipToBounds()) {
 QinblySplit {
                androidx.compose.ui.viewinterop.AndroidView(
                    factory = { ctx -> androidx.camera.view.PreviewView(ctx).also {
                        it.scaleType = androidx.camera.view.PreviewView.ScaleType.FILL_CENTER
                        it.implementationMode = androidx.camera.view.PreviewView.ImplementationMode.COMPATIBLE
                        previewView = it
                    } },
                    modifier = Modifier.fillMaxSize().pointerInput(cameraControl) {
                        detectTransformGestures { _, _, zoomChange, _ ->
                            val camera = cameraControl ?: return@detectTransformGestures
                            val maxZoom = camera.cameraInfo.zoomState.value?.maxZoomRatio ?: 1f
                            val minZoom = camera.cameraInfo.zoomState.value?.minZoomRatio ?: 1f
                            val nextZoom = (zoomRatio * zoomChange).coerceIn(minZoom, maxZoom)
                            camera.cameraControl.setZoomRatio(nextZoom)
                            zoomRatio = nextZoom
                        }
                    },
                    update = { v ->
                        val name = selectedFilter
                        val amount = filterIntensity
                        QinblyFilterIntensity.value = amount
                        try {
                            if (name == "Original") v.setLayerType(android.view.View.LAYER_TYPE_NONE, null)
                            else v.setLayerType(android.view.View.LAYER_TYPE_HARDWARE, android.graphics.Paint().apply {
                                colorFilter = ColorMatrixColorFilter(qinblyFilterMatrix(name, amount))
                            })
                        } catch (_: Exception) { }
                    }
                )
                if (selectedFace != "Nenhum") {
                    QinblyFaceOverlay(selectedFace, faceState, lensFacing == androidx.camera.core.CameraSelector.LENS_FACING_FRONT, Modifier.fillMaxSize())
                }
                if (gridOn) {
                    androidx.compose.foundation.Canvas(Modifier.fillMaxSize()) {
                        val line = Color.White.copy(alpha = 0.35f)
                        for (i in 1..2) {
                            drawLine(line, Offset(size.width * i / 3f, 0f), Offset(size.width * i / 3f, size.height), 1.2f)
                            drawLine(line, Offset(0f, size.height * i / 3f), Offset(size.width, size.height * i / 3f), 1.2f)
                        }
                    }
                }
                QinblyEffectsOverlay(selectedEffect, Modifier.fillMaxSize())
                if (captureMode == "Foto" && camTextMode && !camTextEditing) {
                    Text(
                        if (editorText.isBlank()) androidx.compose.ui.text.AnnotatedString("Toque para escrever") else qinblyColoredText(editorTextFont, editorText, editorTextColor, editorWordColors),
                        color = if (editorText.isBlank()) Color(0xB3FFFFFF) else Color(editorTextColor),
                        fontSize = 28.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily(qinblyTypeface(if (editorText.isBlank()) "Forte" else editorTextFont)),
                        letterSpacing = if (editorText.isBlank()) 0.em else qinblyTextSpacingEm(editorTextFont).em,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        style = MaterialTheme.typography.headlineMedium.copy(shadow = androidx.compose.ui.graphics.Shadow(if (editorTextColor == 0xFF111111.toInt() && editorText.isNotBlank()) Color(0xCCFFFFFF) else Color(0xCC000000), blurRadius = 10f)),
                        modifier = Modifier.align(Alignment.Center).padding(horizontal = 28.dp)
                            .clip(RoundedCornerShape(12.dp)).clickable { camTextEditing = true }.padding(8.dp)
                    )
                }
                if (camTextEditing) {
 QinblySplit {
                    BackHandler { camTextEditing = false }
                    val textFocus = remember { FocusRequester() }
                    val keyboard = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current
                    LaunchedEffect(Unit) { textFocus.requestFocus(); keyboard?.show() }
                    val noRippleText = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                    Column(
                        Modifier.fillMaxSize().background(Color(0x66000000))
                            .clickable(indication = null, interactionSource = noRippleText) { camTextEditing = false; keyboard?.hide() }
                            .imePadding().padding(bottom = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
 QinblySplit {
                        Box(Modifier.weight(1f).fillMaxWidth().padding(horizontal = 24.dp), contentAlignment = Alignment.Center) {
 QinblySplit {
                            androidx.compose.foundation.text.BasicTextField(
                                value = editorText,
                                onValueChange = { editorText = it.take(120) },
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    color = Color(editorTextColor), fontSize = 28.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily(qinblyTypeface(editorTextFont)),
                                    letterSpacing = qinblyTextSpacingEm(editorTextFont).em,
                                    shadow = androidx.compose.ui.graphics.Shadow(Color(0xCC000000), blurRadius = 10f)
                                ),
                                cursorBrush = androidx.compose.ui.graphics.SolidColor(Color.White),
                                visualTransformation = androidx.compose.ui.text.input.VisualTransformation { t ->
                                    androidx.compose.ui.text.input.TransformedText(qinblyColoredText("Forte", t.text, editorTextColor, editorWordColors), androidx.compose.ui.text.input.OffsetMapping.Identity)
                                },
                                modifier = Modifier.fillMaxWidth().focusRequester(textFocus),
                                decorationBox = { inner ->
                                    Box(contentAlignment = Alignment.Center) {
                                        if (editorText.isEmpty()) Text("Digite aqui", color = Color(0x99FFFFFF), fontSize = 28.sp, fontWeight = FontWeight.Bold)
                                        inner()
                                    }
                                }
                            )
                         }
}
                        QinblyTextStyleBar(
                        editorTextFont, editorTextColor, { editorTextFont = it },
                        { c -> if (editorSelWord in 0 until qinblyTextWords(editorText).size) editorWordColors[editorSelWord] = c else { editorTextColor = c; editorWordColors.clear() } },
                        qinblyTextWords(editorText), editorWordColors, editorSelWord, { editorSelWord = it }
                    )
                     }
}
                 }
}
             }
}

            Box(Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 10.dp)) {
 QinblySplit {
                IconButton(onClick = { if (isRecording) activeRecording?.stop(); onCameraCancelled() }, modifier = Modifier.align(Alignment.TopStart).size(40.dp)) {
                    Icon(QinblyCameraIcons.close, contentDescription = "Fechar câmera", tint = Color.White, modifier = Modifier.size(26.dp))
                }

                if (!isRecording) {
                    Row(
                        Modifier.align(Alignment.TopCenter).offset(x = (-14).dp).padding(top = 4.dp)
                            .clip(CircleShape).background(Color(0x73000000)).clickable { cameraSheet = "Som" }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(QinblyCameraIcons.music, contentDescription = null, tint = Color.White, modifier = Modifier.size(19.dp))
                        Text(if (soundName.isBlank()) "Adicionar som" else soundName, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold,
                            maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis, modifier = Modifier.widthIn(max = 130.dp))
                    }
                } else {
                    Text("%d:%02d".format(recElapsed / 60, recElapsed % 60), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp,
                        modifier = Modifier.align(Alignment.TopCenter).padding(top = 8.dp).clip(RoundedCornerShape(14.dp))
                            .background(Color(0x73000000)).padding(horizontal = 12.dp, vertical = 4.dp))
                }

                if (!isRecording) {
 QinblySplit {
                    Column(
                        Modifier.align(Alignment.TopEnd).padding(top = 88.dp).width(140.dp)
                            .then(if (sideExpanded) Modifier.fillMaxHeight().padding(bottom = 8.dp) else Modifier)
                            .verticalScroll(sideScroll),
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(11.dp)
                    ) {
 QinblySplit {
                        QinblyCameraSideItem(QinblyCameraIcons.flash, "Flash", on = flashEnabled) {
                            flashEnabled = !flashEnabled
                            imageCapture?.flashMode = if (flashEnabled) androidx.camera.core.ImageCapture.FLASH_MODE_ON else androidx.camera.core.ImageCapture.FLASH_MODE_OFF
                        }
                        QinblyCameraSideItem(QinblyCameraIcons.timer, "Temporizador", on = selectedTimerSeconds != "Desligado") {
                            selectedTimerSeconds = when (selectedTimerSeconds) { "Desligado" -> "3 s"; "3 s" -> "5 s"; "5 s" -> "10 s"; else -> "Desligado" }
                            flashLabel = if (selectedTimerSeconds == "Desligado") "Temporizador desligado" else "Temporizador: " + selectedTimerSeconds
                        }
                        QinblyCameraSideItem(QinblyCameraIcons.collage, "Colagem", on = collageCount > 0) {
                            collageShots.clear()
                            collageCount = when (collageCount) { 0 -> 2; 2 -> 4; else -> 0 }
                            flashLabel = if (collageCount == 0) "Colagem desligada" else "Colagem de $collageCount fotos: tire uma por vez"
                        }
                        QinblyCameraSideItem(QinblyCameraIcons.format, "Formato") {
                            selectedCameraFormat = when (selectedCameraFormat) { "9:16" -> "4:3"; "4:3" -> "1:1"; else -> "9:16" }
                            flashLabel = "Formato " + selectedCameraFormat
                        }
                        QinblyCameraSideItem(QinblyCameraIcons.retouch, "Retoque", on = retouchOn) {
                            retouchOn = !retouchOn
                            flashLabel = if (retouchOn) "Retoque ligado (aplicado nas fotos)" else "Retoque desligado"
                        }
                        QinblyCameraSideItem(QinblyCameraIcons.filters, "Filtros", on = filtersVisible) { filtersVisible = !filtersVisible }
                        if (sideExpanded) {
 QinblySplit {
                            QinblyCameraSideItem(QinblyCameraIcons.grid, "Grade", on = gridOn) {
                                gridOn = !gridOn
                                flashLabel = if (gridOn) "Grade ligada" else "Grade desligada"
                            }
                            QinblyCameraSideItem(QinblyCameraIcons.mirror, "Espelhar", on = mirrorOn) {
                                mirrorOn = !mirrorOn
                                flashLabel = if (mirrorOn) "Espelhar ligado" else "Espelhar desligado"
                            }
                            QinblyCameraSideItem(QinblyCameraIcons.speed, "Velocidade", on = speedChoice != 1f) {
                                speedChoice = when (speedChoice) { 1f -> 0.5f; 0.5f -> 2f; 2f -> 3f; else -> 1f }
                                flashLabel = if (speedChoice == 1f) "Velocidade normal" else "Velocidade " + (if (speedChoice == 0.5f) "0,5x" else speedChoice.toInt().toString() + "x") + " (nos vídeos)"
                            }
                            QinblyCameraSideItem(QinblyCameraIcons.handsFree, "Mãos livres", on = handsFreeOn) {
                                handsFreeOn = !handsFreeOn
                                flashLabel = if (handsFreeOn) "Mãos livres: o vídeo começa após a contagem" else "Mãos livres desligado"
                            }
                            QinblyCameraSideItem(QinblyCameraIcons.exposure, "Exposição") { cameraSheet = "Exposição" }
                            QinblyCameraSideItem(QinblyCameraIcons.night, "Modo noite", on = nightOn) {
                                val up = cameraControl?.cameraInfo?.exposureState?.exposureCompensationRange?.upper ?: 0
                                if (up <= 0) onNotice("Este aparelho não permite clarear a imagem.")
                                else { nightOn = !nightOn; flashLabel = if (nightOn) "Modo noite ligado" else "Modo noite desligado" }
                            }
                            QinblyCameraSideItem(QinblyCameraIcons.hdr, "HDR", on = hdrOn) {
                                hdrOn = !hdrOn
                                flashLabel = if (hdrOn) "HDR ligado (aplicado nas fotos)" else "HDR desligado"
                            }
                            QinblyCameraSideItem(QinblyCameraIcons.stabilize, "Estabilizar", dim = !stabilizeOn) {
                                if (!stabilizeSupported) onNotice("Este aparelho não tem estabilização de vídeo.")
                                else { stabilizeOn = !stabilizeOn; onNotice(if (stabilizeOn) "Estabilização ligada." else "Estabilização desligada.") }
                            }
                            QinblyCameraSideItem(QinblyCameraIcons.quality, "Qualidade", on = qualityChoice != "HD") {
                                qualityChoice = when (qualityChoice) { "HD" -> "FHD"; "FHD" -> "UHD"; else -> "HD" }
                                flashLabel = "Qualidade " + qualityChoice
                            }
                            QinblyCameraSideItem(QinblyCameraIcons.background, "Fundo", on = bgBlurOn) {
                                bgBlurOn = !bgBlurOn
                                flashLabel = if (bgBlurOn) "Fundo desfocado (aplicado nas fotos)" else "Fundo normal"
                            }
                         }
}
                        QinblyCameraSideItem(if (sideExpanded) QinblyCameraIcons.chevronUp else QinblyCameraIcons.chevronDown, "") { sideExpanded = !sideExpanded }
                        if (sideExpanded) Spacer(Modifier.height(24.dp))
                     }
}
                 }
}

                if (flashLabel.isNotBlank()) {
                    Text(flashLabel, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium.copy(shadow = androidx.compose.ui.graphics.Shadow(Color(0x99000000), blurRadius = 8f)),
                        modifier = Modifier.align(Alignment.TopCenter).padding(top = 64.dp))
                }
                if (countdown > 0) {
                    Text(countdown.toString(), color = Color.White, fontSize = 96.sp, fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.displayLarge.copy(shadow = androidx.compose.ui.graphics.Shadow(Color(0x99000000), blurRadius = 16f)),
                        modifier = Modifier.align(Alignment.Center))
                }
                if (showZoomBadge) {
                    Text("${(zoomRatio * 10).toInt() / 10f}x", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 250.dp).clip(RoundedCornerShape(16.dp))
                            .background(Color(0x80000000)).padding(horizontal = 12.dp, vertical = 5.dp))
                }

                Column(
                    Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(bottom = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
 QinblySplit {
                    if (cameraError.isNotBlank()) {
                        Text(cameraError, color = Color.White, style = MaterialTheme.typography.bodySmall,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                    if (!isRecording) {
 QinblySplit {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                            listOf("10 min", "60 s", "15 s", "FOTO", "TEXTO").forEach { chip ->
                                val chosen = (chip == "FOTO" && captureMode == "Foto" && !camTextMode) || (chip == "TEXTO" && camTextMode) || (chip == selectedDuration && captureMode == "Vídeo" && !camTextMode)
                                Text(chip, color = if (chosen) Color.Black else Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp,
                                    modifier = Modifier.clip(RoundedCornerShape(16.dp)).background(if (chosen) Color.White else Color.Transparent)
                                        .clickable {
                                            when (chip) {
                                                "FOTO" -> { captureMode = "Foto"; camTextMode = false }
                                                "TEXTO" -> { captureMode = "Foto"; camTextMode = true; camTextEditing = true }
                                                else -> { captureMode = "Vídeo"; selectedDuration = chip; camTextMode = false }
                                            }
                                        }.padding(horizontal = 9.dp, vertical = 5.dp))
                            }
                        }
                        if (filtersVisible && selectedFilter != "Original") {
 QinblySplit {
                            Row(
                                Modifier.padding(horizontal = 30.dp).fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color(0x66000000))
                                    .padding(horizontal = 12.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Intensidade", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                Slider(
                                    value = filterIntensity, onValueChange = { filterIntensity = it }, valueRange = 0f..1f,
                                    colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = Color.White, inactiveTrackColor = Color(0x55FFFFFF)),
                                    modifier = Modifier.weight(1f).height(28.dp).padding(horizontal = 8.dp)
                                )
                                Text((filterIntensity * 100).toInt().toString(), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                            }
                         }
}
                        if (filtersVisible) {
 QinblySplit {
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(horizontal = 14.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(bubbleItems) { (name, animated) ->
                                    if (!animated) {
                                        QinblyFilterBubble(name, selectedFilter == name, {
                                            selectedFilter = name
                                            filterIntensity = 1f
                                            flashLabel = name
                                        }) {
                                            val swatch = qinblyFilterSwatch(name)
                                            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(swatch[0], swatch[0], swatch[1], swatch[2]))))
                                        }
                                    } else {
                                        QinblyFilterBubble(name, selectedEffect == name, {
                                            selectedEffect = if (selectedEffect == name) "Original" else name
                                            flashLabel = if (selectedEffect == "Original") "Efeito desligado" else name
                                        }) {
                                            Box(Modifier.fillMaxSize().background(Color(0xCC10151E)), contentAlignment = Alignment.Center) {
                                                Icon(QinblyCameraIcons.effect(name), contentDescription = name, tint = Color.White, modifier = Modifier.size(24.dp))
                                            }
                                        }
                                    }
                                }
                                items(QinblyFaceFilterNames) { faceName ->
                                    QinblyFilterBubble(faceName, selectedFace == faceName, {
                                        selectedFace = if (selectedFace == faceName) "Nenhum" else faceName
                                        flashLabel = if (selectedFace == "Nenhum") "Filtro de rosto desligado" else faceName
                                    }) {
                                        QinblyFaceThumb(faceName, Modifier.fillMaxSize())
                                    }
                                }
                            }
                         }
}
                     }
}
                    Row(Modifier.fillMaxWidth().padding(horizontal = 22.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
 QinblySplit {
                        if (isRecording) {
                            Spacer(Modifier.size(44.dp))
                        } else {
 QinblySplit {
                            Box(
                                Modifier.size(44.dp).clip(RoundedCornerShape(11.dp)).background(Color(0x29FFFFFF))
                                    .border(2.dp, Color.White, RoundedCornerShape(11.dp))
                                    .clickable {
                                        try { mediaPicker.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)) }
                                        catch (e: Exception) { onNotice("Não foi possível abrir a galeria: " + (e.localizedMessage ?: "tente novamente")) }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                val thumb = latestThumb
                                if (thumb != null) androidx.compose.foundation.Image(thumb.asImageBitmap(), contentDescription = "Galeria", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                                else Icon(QinblyCreateIcons.gallery, contentDescription = "Galeria", tint = Color.White, modifier = Modifier.size(26.dp))
                            }
                         }
}
                        Box(
                            Modifier.size(if (isRecording) 92.dp else 72.dp)
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onPress = {
                                            tryAwaitRelease()
                                            if (holdStarted) {
                                                holdStarted = false
                                                val recording = activeRecording
                                                if (recording != null) recording.stop() else stopWhenReady = true
                                            }
                                        },
                                        onLongPress = {
                                            if (!isRecording && pendingAction.isBlank()) {
                                                holdStarted = true
                                                requestRecording()
                                            }
                                        },
                                        onTap = { onShutterTap() }
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
 QinblySplit {
                            val limit = if (captureMode == "Foto") 60 else when (selectedDuration) { "10 min" -> 600; "60 s" -> 60; else -> 15 }
                            androidx.compose.foundation.Canvas(Modifier.fillMaxSize()) {
                                val stroke = 4.dp.toPx()
                                drawCircle(if (isRecording) Color(0x59FFFFFF) else Color.White, radius = size.minDimension / 2f - stroke / 2f, style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke))
                                if (isRecording) {
                                    drawArc(
                                        QinblyBrand.Accent, startAngle = -90f, sweepAngle = 360f * (recElapsed.toFloat() / limit).coerceIn(0f, 1f), useCenter = false,
                                        topLeft = Offset(stroke / 2f, stroke / 2f),
                                        size = androidx.compose.ui.geometry.Size(size.width - stroke, size.height - stroke),
                                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke, cap = androidx.compose.ui.graphics.StrokeCap.Round)
                                    )
                                }
                            }
                            Box(
                                Modifier.size(if (isRecording) 32.dp else 56.dp)
                                    .clip(if (isRecording) RoundedCornerShape(9.dp) else CircleShape)
                                    .background(if (!isRecording && (captureMode == "Vídeo" || handsFreeOn)) QinblyBrand.Accent else Color.White)
                            )
                         }
}
                        if (isRecording) {
                            Spacer(Modifier.size(44.dp))
                        } else {
                            Box(Modifier.size(44.dp).clip(CircleShape).background(Color(0x2EFFFFFF)).clickable {
                                lensFacing = if (lensFacing == androidx.camera.core.CameraSelector.LENS_FACING_BACK) androidx.camera.core.CameraSelector.LENS_FACING_FRONT else androidx.camera.core.CameraSelector.LENS_FACING_BACK
                            }, contentAlignment = Alignment.Center) {
                                Icon(QinblyCameraIcons.flip, contentDescription = "Virar câmera", tint = Color.White, modifier = Modifier.size(24.dp))
                            }
                        }
                     }
}
                    Text(
                        if (isRecording) (if (holdStarted) "Solte para parar" else "Toque para parar") else "Toque para foto · segure para gravar",
                        color = Color.White.copy(alpha = 0.78f), fontSize = 10.sp
                    )
                 }
}
             }
}

            if (cameraSheet.isNotBlank()) {
 QinblySplit {
                Box(Modifier.fillMaxSize().background(Color(0x73000000)).pointerInput(Unit) { detectTapGestures { cameraSheet = "" } })
                Column(
                    Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)).background(Color(0xFF161C27))
                        .pointerInput(Unit) { detectTapGestures { } }
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
 QinblySplit {
                    Box(Modifier.align(Alignment.CenterHorizontally).width(36.dp).height(4.dp).clip(CircleShape).background(Color(0xFF3A4458)))
                    Spacer(Modifier.height(10.dp))
                    if (cameraSheet == "Som") {
                        Text("Escolher som", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        QinblyCameraSheetRow(QinblyCameraIcons.headphones, "Do seu celular", "Escolha uma música ou áudio seu") { soundPicker.launch("audio/*") }
                        QinblyCameraSheetRow(QinblyCameraIcons.mic, "Gravar narração", "Fale sobre o vídeo", soon = true) { }
                        QinblyCameraSheetRow(QinblyCameraIcons.music, "Sons do Qinbly", "Catálogo de músicas", soon = true) { }
                        if (soundName.isNotBlank()) {
                            TextButton(onClick = { soundName = ""; soundUri = null; cameraSheet = "" }) { Text("Remover som escolhido", color = QinblyBrand.Accent) }
                        }
                    } else if (cameraSheet == "Exposição") {
                        val state = cameraControl?.cameraInfo?.exposureState
                        Text("Exposição", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        if (state != null && state.isExposureCompensationSupported) {
                            val low = state.exposureCompensationRange.lower
                            val high = state.exposureCompensationRange.upper
                            Slider(
                                value = exposureIndex.toFloat(), onValueChange = { exposureIndex = it.toInt() },
                                valueRange = low.toFloat()..high.toFloat(), steps = (high - low - 1).coerceAtLeast(0),
                                colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = QinblyBrand.Accent, inactiveTrackColor = Color(0xFF3A4458))
                            )
                            Text("Escuro  ←  →  Claro", color = Color(0xFFA9B5C8), fontSize = 11.sp)
                        } else {
                            Text("Este aparelho não permite ajustar o brilho aqui.", color = Color(0xFFA9B5C8), fontSize = 12.sp)
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                 }
}
             }
}
         }
}
        return@QinblySplit
     }
}

    if (galleryOpen) {
 QinblySplit {
        Column(Modifier.fillMaxSize().background(Color(0xFF080808)).padding(horizontal = 14.dp)) {
 QinblySplit {
            Row(Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { galleryOpen = false; gallerySelection.clear() }) {
                    Icon(Icons.Default.Close, contentDescription = "Fechar galeria", tint = Color.White)
                }
                Column(Modifier.weight(1f)) {
                    Text("Recentes", color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Escolha uma foto ou vídeo", color = Color.LightGray, style = MaterialTheme.typography.bodySmall)
                }
                TextButton(onClick = { galleryFilter = "Todos" }) { Text("▾", color = Color.White) }
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
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Color.White) }
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
                                    if (asset.isVideo) {
                                        gallerySelection.clear()
                                        gallerySelection.add(asset.uri)
                                    } else if (selectedIndex >= 0) {
                                        gallerySelection.remove(asset.uri)
                                    } else if (gallerySelection.size < 10) {
                                        gallerySelection.add(asset.uri)
                                    } else {
                                        onNotice("O limite é de 10 fotos por publicação.")
                                    }
                                }
                        ) {
                            AsyncImage(model = asset.uri, contentDescription = asset.name, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                            if (asset.isVideo) {
                                Row(Modifier.align(Alignment.BottomStart).fillMaxWidth().background(Color(0x66000000)).padding(5.dp),
                                    verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                                    Text("VÍDEO", color = Color.White, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                            Box(Modifier.align(Alignment.TopEnd).padding(6.dp).size(23.dp).border(1.5.dp, Color.White, CircleShape)
                                .background(if (selectedIndex >= 0) Color(0xFFB12AFF) else Color(0x55000000), CircleShape),
                                contentAlignment = Alignment.Center) {
                                if (selectedIndex >= 0) Text("${selectedIndex + 1}", color = Color.White, style = MaterialTheme.typography.labelSmall)
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
            ) { Text(if (gallerySelection.isEmpty()) "Avançar" else "Avançar (${gallerySelection.size}/10)", fontWeight = FontWeight.Bold) }
         }
}
        return@QinblySplit
     }
}

    if (photoVideoOpen) {
 QinblySplit {
        QinblyPhotoVideoEditor(
            clips = photoVideoClips,
            pendingAdd = photoVideoPending,
            onPendingConsumed = { photoVideoPending = emptyList() },
            busy = photoVideoBusy,
            onAdd = {
                try { addVideoPhotos.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
                catch (e: Exception) { onNotice("Não foi possível abrir a galeria.") }
            },
            onClose = { photoVideoOpen = false },
            onDone = {
                if (!photoVideoBusy && photoVideoClips.isNotEmpty()) {
                    photoVideoBusy = true
                    qinblyBuildPhotoVideo(context, photoVideoClips.toList()) { out, error ->
                        photoVideoBusy = false
                        if (out != null) {
                            sourceUri = out; selectedUri = out; selectedType = "vídeo"; bakedUri = null
                            gallerySelection.clear()
                            editorTool = "Filtros"
                            photoVideoOpen = false
                            showComposer = true
                        } else onNotice(error)
                    }
                }
            },
            onNotice = { msg -> onNotice(msg) }
        )
}
        return
     }

    fun applyEditorEffect(retouch: Boolean, blur: Boolean, hdr: Boolean, done: String) {
        val current = selectedUri
        if (selectedType != "foto" || current == null) { onNotice("Esse efeito só funciona em fotos."); return }
        if (editorBusy) return
        editorBusy = true
        qinblyApplyPhotoEffects(context, current, retouch, blur, hdr) { fx, note ->
            editorBusy = false
            if (fx != null) { selectedUri = fx; bakedUri = null }
            onNotice(if (note.isNotBlank()) note else if (fx != null) done else "Não foi possível aplicar o efeito.")
        }
    }
    if (cropOpen && selectedType == "foto" && selectedUri != null) {
 QinblySplit {
        BackHandler { cropOpen = false }
        val cropUri = selectedUri!!
        val frameRatio = qinblyRatioValue(cropRatio, editorImageRatio)
        Column(Modifier.fillMaxSize().background(Color(0xFF0B0D12)).statusBarsPadding().navigationBarsPadding()) {
 QinblySplit {
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { cropOpen = false }) {
                    Icon(QinblyCameraIcons.close, contentDescription = "Fechar", tint = Color.White, modifier = Modifier.size(28.dp))
                }
                Text("Recortar", color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            Box(Modifier.weight(1f).fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp), contentAlignment = Alignment.Center) {
 QinblySplit {
                BoxWithConstraints(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    val maxW = maxWidth; val maxH = maxHeight
                    val frameW = if (maxW / maxH > frameRatio) maxH * frameRatio else maxW
                    val frameH = frameW / frameRatio
                    Box(
                        Modifier.width(frameW).height(frameH).clipToBounds()
                            .pointerInput(cropRatio, editorImageRatio) {
                                detectTransformGestures { _, pan, gestureZoom, _ ->
                                    val newZoom = (cropZoom * gestureZoom).coerceIn(1f, 4f)
                                    val limits = qinblyMaxPan(editorImageRatio, frameRatio, newZoom)
                                    cropZoom = newZoom
                                    cropPanX = (cropPanX + pan.x / size.width).coerceIn(-limits.first, limits.first)
                                    cropPanY = (cropPanY + pan.y / size.height).coerceIn(-limits.second, limits.second)
                                }
                            }
                    ) {
                        AsyncImage(
                            model = cropUri, contentDescription = "Foto", contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().graphicsLayer {
                                scaleX = cropZoom; scaleY = cropZoom
                                translationX = cropPanX * size.width; translationY = cropPanY * size.height
                            }
                        )
                        Box(Modifier.fillMaxSize().border(1.5.dp, Color.White))
                        Column(Modifier.fillMaxSize()) {
                            repeat(2) { Spacer(Modifier.weight(1f)); Box(Modifier.fillMaxWidth().height(0.5.dp).background(Color(0x80FFFFFF))) }
                            Spacer(Modifier.weight(1f))
                        }
                        Row(Modifier.fillMaxSize()) {
                            repeat(2) { Spacer(Modifier.weight(1f)); Box(Modifier.fillMaxHeight().width(0.5.dp).background(Color(0x80FFFFFF))) }
                            Spacer(Modifier.weight(1f))
                        }
                    }
                }
             }
}
            Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.Center) {
                listOf("Original", "1:1", "4:5", "9:16", "16:9").forEach { ratio ->
                    Text(ratio, color = if (cropRatio == ratio) Color(0xFF0B0D12) else Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 3.dp).clip(RoundedCornerShape(14.dp))
                            .background(if (cropRatio == ratio) Color.White else Color.Transparent)
                            .border(0.5.dp, Color(0x80FFFFFF), RoundedCornerShape(14.dp))
                            .clickable { cropRatio = ratio; cropZoom = 1f; cropPanX = 0f; cropPanY = 0f }
                            .padding(horizontal = 11.dp, vertical = 6.dp))
                }
            }
            Box(Modifier.fillMaxWidth().padding(vertical = 4.dp), contentAlignment = Alignment.Center) {
 QinblySplit {
                IconButton(onClick = {
                    if (!editorBusy) {
                        editorBusy = true
                        Thread {
                            try {
                                val copy = qinblyTransformCopy(context, cropUri, true, false)
                                android.os.Handler(android.os.Looper.getMainLooper()).post {
                                    selectedUri = copy
                                    editorImageRatio = 1f / editorImageRatio
                                    cropZoom = 1f; cropPanX = 0f; cropPanY = 0f
                                    editorBusy = false
                                }
                            } catch (e: Exception) {
                                android.os.Handler(android.os.Looper.getMainLooper()).post { editorBusy = false; onNotice("Não foi possível girar a foto.") }
                            }
                        }.start()
                    }
                }) { Icon(QinblyCameraIcons.rotate, contentDescription = "Girar", tint = Color.White, modifier = Modifier.size(26.dp)) }
             }
}
            Column(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 10.dp)) {
                Button(
                    onClick = { editorRatio = cropRatio; editorZoom = cropZoom; editorPanX = cropPanX; editorPanY = cropPanY; cropOpen = false },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = QinblyBrand.Accent, contentColor = Color.White)
                ) { Text("Usar recorte", fontWeight = FontWeight.Bold, color = Color.White) }
            }
         }
}
        return@QinblySplit
     }
}

    // Etapa 1: editor visual. Etapa 2: opções e publicação.
    if (!publishSettings) {
 QinblySplit {
        Box(Modifier.fillMaxSize().background(Color.Black)) {
 QinblySplit {
            val uri = selectedUri
            if (uri != null && selectedType == "foto") {
 QinblySplit {
                val previewFrame = qinblyRatioValue(editorRatio, editorImageRatio)
                val previewMatrix = remember(selectedFilter, filterIntensity, editorBrightness, editorContrast) {
                    androidx.compose.ui.graphics.ColorFilter.colorMatrix(androidx.compose.ui.graphics.ColorMatrix(qinblyEditMatrix(selectedFilter, filterIntensity, editorBrightness, editorContrast).array))
                }
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
 QinblySplit {
                    Box(
                        Modifier.then(if (editorRatio == "Original") Modifier.fillMaxSize() else Modifier.fillMaxWidth().aspectRatio(previewFrame)).clipToBounds()
                    ) {
                        AsyncImage(
                            model = uri,
                            contentDescription = "Prévia da publicação",
                            contentScale = ContentScale.Crop,
                            colorFilter = previewMatrix,
                            modifier = Modifier.fillMaxSize().graphicsLayer {
                                scaleX = editorZoom; scaleY = editorZoom
                                translationX = editorPanX * size.width; translationY = editorPanY * size.height
                            }
                        )
                    }
                 }
}
             }
} else if (uri != null) {
                // Mesmo player do feed (ExoPlayer, repetição e toque para pausar). Sem barra de progresso
                // para não cobrir a barra de ferramentas do editor.
                QinblyFullscreenVideo(url = uri.toString(), modifier = Modifier.fillMaxSize(), showProgress = false)
            }
            val editorFilterTint = when (selectedFilter) {
                "Aurora" -> Color(0x22B9A6F0)
                "Pôr do sol" -> Color(0x33F4A261)
                "Neon" -> Color(0x22FF4FD8)
                "Filme" -> Color(0x22A7864A)
                "Pastel" -> Color(0x33FFFFFF)
                "Oceano" -> Color(0x226EC6E6)
                "Sonho" -> Color(0x22F7C5E3)
                "Dourado" -> Color(0x33E9C46A)
                "Quente" -> Color(0x22FF9A42)
                "Frio" -> Color(0x223F8CFF)
                "Vivo" -> Color(0x18FF45B5)
                "Cinema" -> Color(0x2230485A)
                "P&B" -> Color(0x55999999)
                "Retrô", "Vintage" -> Color(0x22C78D5A)
                "Sépia" -> Color(0x33B5834A)
                "Vívido" -> Color(0x18FF45B5)
                "Suave" -> Color(0x22FFFFFF)
                "Desbotado" -> Color(0x33AAAAAA)
                "Noir" -> Color(0x66222222)
                "Rosado" -> Color(0x22FF6FAF)
                else -> Color.Transparent
            }
            if (editorFilterTint != Color.Transparent && selectedType == "vídeo") {
                Box(Modifier.fillMaxSize().alpha(filterIntensity).background(editorFilterTint))
            }
            QinblyEffectsOverlay(selectedEffect, Modifier.fillMaxSize())
            if (editorTool == "Desenho") {
 QinblySplit {
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
}
            if (editorSticker.isNotBlank()) {
                Text(editorSticker, modifier = Modifier.align(Alignment.Center), color = Color.Black, style = MaterialTheme.typography.displayLarge)
            }
            if (editorText.isNotBlank()) {
                Text(qinblyColoredText(editorTextFont, editorText, editorTextColor, editorWordColors), modifier = Modifier.align(Alignment.Center).padding(horizontal = 28.dp, vertical = 72.dp),
                    color = Color(editorTextColor), fontSize = 28.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily(qinblyTypeface(editorTextFont)),
                    letterSpacing = qinblyTextSpacingEm(editorTextFont).em,
                    style = MaterialTheme.typography.headlineMedium.copy(shadow = androidx.compose.ui.graphics.Shadow(if (editorTextColor == android.graphics.Color.BLACK || editorTextColor == 0xFF111111.toInt()) Color(0xCCFFFFFF) else Color(0xCC000000), blurRadius = 8f)),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0x99000000), Color.Transparent, Color.Transparent, Color(0xEE000000)))))
            if (newEditorChrome) {
 QinblySplit {
            // Mesmo visual da câmera: X no canto e ferramentas de edição de foto na lateral (ícone de linha fina + nome).
            var editorSideExpanded by remember { mutableStateOf(false) }
            val editorBubbles = remember { QinblyColorFilterNames.map { it to false } + QinblyAnimatedEffectNames.map { it to true } }
            IconButton(
                onClick = { leaveEditor() },
                modifier = Modifier.align(Alignment.TopStart).padding(start = 8.dp, top = 8.dp)
            ) {
                Icon(QinblyCameraIcons.close, contentDescription = "Voltar", tint = Color.White, modifier = Modifier.size(28.dp))
            }
            Column(
                modifier = Modifier.align(Alignment.TopEnd).padding(end = 12.dp, top = 92.dp).width(140.dp)
                    .then(if (editorSideExpanded) Modifier.fillMaxHeight().padding(bottom = 8.dp) else Modifier)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(11.dp)
            ) {
 QinblySplit {
                if (selectedType == "foto") QinblyCameraSideItem(QinblyCameraIcons.editor, "Editar") {
                    photoVideoClips.clear()
                    val seed = if (gallerySelection.isNotEmpty()) gallerySelection.toList() else listOfNotNull(sourceUri ?: selectedUri)
                    photoVideoClips.addAll(seed.map { PvClip(it) })
                    photoVideoPending = emptyList()
                    photoVideoOpen = true
                }
                QinblyCameraSideItem(QinblyCreateIcons.text, "Texto", on = editorTool == "Texto") { editorTool = if (editorTool == "Texto") "Filtros" else "Texto" }
                QinblyCameraSideItem(QinblyCreateIcons.sticker, "Stickers", on = editorTool == "Stickers") { editorTool = if (editorTool == "Stickers") "Filtros" else "Stickers" }
                QinblyCameraSideItem(QinblyCameraIcons.format, "Formato", on = editorTool == "Ajustar") { editorTool = if (editorTool == "Ajustar") "Filtros" else "Ajustar" }
                if (selectedType == "foto") QinblyCameraSideItem(QinblyCameraIcons.crop, "Recortar") {
                    cropRatio = editorRatio; cropZoom = editorZoom; cropPanX = editorPanX; cropPanY = editorPanY
                    cropOpen = true
                }
                QinblyCameraSideItem(QinblyCameraIcons.retouch, "Retoque") { applyEditorEffect(true, false, false, "Pele suavizada.") }
                QinblyCameraSideItem(QinblyCameraIcons.filters, "Filtros", on = editorTool == "Filtros") { editorTool = "Filtros" }
                if (editorSideExpanded) {
 QinblySplit {
                    QinblyCameraSideItem(QinblyCameraIcons.pen, "Desenho", on = editorTool == "Desenho") {
                        editorTool = if (editorTool == "Desenho") "Filtros" else "Desenho"
                        if (editorTool == "Desenho") editorStrokes.clear()
                    }
                    if (selectedType == "foto") {
 QinblySplit {
                        QinblyCameraSideItem(QinblyCameraIcons.mirror, "Espelhar") {
                            val current = selectedUri
                            if (current != null && !editorBusy) {
                                editorBusy = true
                                Thread {
                                    try {
                                        val copy = qinblyTransformCopy(context, current, false, true)
                                        android.os.Handler(android.os.Looper.getMainLooper()).post { selectedUri = copy; editorBusy = false }
                                    } catch (e: Exception) {
                                        android.os.Handler(android.os.Looper.getMainLooper()).post { editorBusy = false; onNotice("Não foi possível espelhar a foto.") }
                                    }
                                }.start()
                            }
                        }
                        QinblyCameraSideItem(QinblyCameraIcons.adjust, "Ajustes", on = editorTool == "Ajustes") { editorTool = if (editorTool == "Ajustes") "Filtros" else "Ajustes" }
                        QinblyCameraSideItem(QinblyCameraIcons.hdr, "HDR") { applyEditorEffect(false, false, true, "HDR aplicado.") }
                        QinblyCameraSideItem(QinblyCameraIcons.background, "Fundo") { applyEditorEffect(false, true, false, "Fundo desfocado.") }
                     }
}
                 }
}
                QinblyCameraSideItem(if (editorSideExpanded) QinblyCameraIcons.chevronUp else QinblyCameraIcons.chevronDown, "") { editorSideExpanded = !editorSideExpanded }
             }
}

             }
} else {
 QinblySplit {
            // Cabeçalho: voltar à esquerda e música centralizada, sem ferramentas horizontais.
            IconButton(
                onClick = {
                    returnToGallery()
                },
                modifier = Modifier.align(Alignment.TopStart).padding(start = 12.dp, top = 12.dp)
                    .size(48.dp).border(1.dp, Color(0x557E828B), CircleShape)
                    .background(Color(0x88303030), CircleShape)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = Color.Black)
            }
            // Nova coluna de ferramentas do editor, com identidade visual própria.
            Column(
                modifier = Modifier.align(Alignment.CenterEnd).padding(end = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(11.dp)
            ) {
 QinblySplit {
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
                                            "Original" -> "Aurora"; "Aurora" -> "Pôr do sol"; "Pôr do sol" -> "Neon"
                                            "Neon" -> "Cinema"; "Cinema" -> "Noir"; else -> "Original"
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
}

             }
}
            Column(
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xE6000000))))
                    .padding(bottom = 20.dp, top = 52.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
 QinblySplit {
                // A tira de quadros pertence à edição de vídeo. Não repetir
                // a mesma fotografia como se fossem frames de uma timeline.
                if (selectedUri != null && selectedType == "vídeo") {
 QinblySplit {
                    Row(
                        Modifier.fillMaxWidth().height(54.dp).padding(horizontal = 12.dp)
                            .clip(RoundedCornerShape(8.dp)).background(Color(0xAA171717)),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
 QinblySplit {
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
                 }
}
                if (newEditorChrome && editorTool == "Filtros") {
 QinblySplit {
                    if (selectedFilter != "Original") {
 QinblySplit {
                        Row(
                            Modifier.padding(horizontal = 30.dp).fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color(0x66000000))
                                .padding(horizontal = 12.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Intensidade", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                            Slider(
                                value = filterIntensity, onValueChange = { filterIntensity = it }, valueRange = 0f..1f,
                                colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = Color.White, inactiveTrackColor = Color(0x55FFFFFF)),
                                modifier = Modifier.weight(1f).height(28.dp).padding(horizontal = 8.dp)
                            )
                            Text((filterIntensity * 100).toInt().toString(), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                        }
                     }
}
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(editorBubbles) { (name, animated) ->
                            if (!animated) {
                                QinblyFilterBubble(name, selectedFilter == name, { selectedFilter = name; filterIntensity = 1f }) {
                                    val swatch = qinblyFilterSwatch(name)
                                    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(swatch[0], swatch[0], swatch[1], swatch[2]))))
                                }
                            } else {
                                QinblyFilterBubble(name, selectedEffect == name, { selectedEffect = if (selectedEffect == name) "Original" else name }) {
                                    Box(Modifier.fillMaxSize().background(Color(0xCC10151E)), contentAlignment = Alignment.Center) {
                                        Icon(QinblyCameraIcons.effect(name), contentDescription = name, tint = Color.White, modifier = Modifier.size(24.dp))
                                    }
                                }
                            }
                        }
                    }
                 }
}
                if (newEditorChrome && editorTool == "Ajustes") {
 QinblySplit {
                    Column(Modifier.padding(horizontal = 30.dp).fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color(0x66000000)).padding(horizontal = 12.dp, vertical = 4.dp)) {
 QinblySplit {
                        listOf("Brilho" to 0, "Contraste" to 1).forEach { (label, kind) ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(label, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.width(62.dp))
                                Slider(
                                    value = if (kind == 0) editorBrightness else editorContrast,
                                    onValueChange = { if (kind == 0) editorBrightness = it else editorContrast = it },
                                    valueRange = if (kind == 0) -1f..1f else -0.5f..0.5f,
                                    colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = Color.White, inactiveTrackColor = Color(0x55FFFFFF)),
                                    modifier = Modifier.weight(1f).height(30.dp)
                                )
                                Text((if (kind == 0) editorBrightness * 100 else editorContrast * 200).toInt().toString(), color = Color.White, fontSize = 10.sp, modifier = Modifier.width(30.dp), textAlign = androidx.compose.ui.text.style.TextAlign.End)
                            }
                        }
                     }
}
                 }
}
                if (editorTool == "Ajustar") {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        listOf("Original", "1:1", "4:5", "9:16", "16:9").forEach { ratio ->
                            TextButton(onClick = { editorRatio = ratio; editorZoom = 1f; editorPanX = 0f; editorPanY = 0f }) {
                                Text(ratio, color = if (editorRatio == ratio) Color.White else Color.LightGray)
                            }
                        }
                    }
                }
                if (editorTool == "Texto") {
 QinblySplit {
                    QinblyTextStyleBar(
                        editorTextFont, editorTextColor, { editorTextFont = it },
                        { c -> if (editorSelWord in 0 until qinblyTextWords(editorText).size) editorWordColors[editorSelWord] = c else { editorTextColor = c; editorWordColors.clear() } },
                        qinblyTextWords(editorText), editorWordColors, editorSelWord, { editorSelWord = it }
                    )
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
 QinblySplit {
                    Button(
                        onClick = {
                            if (selectedUri == null && sourceUri == null) {
                                publishError = "Escolha um vídeo ou uma foto antes de continuar."
                                onNotice(publishError)
                            } else if (!selectedMediaReady) {
                                publishError = "Não foi possível ler a mídia da galeria. Selecione o arquivo novamente."
                                onNotice(publishError)
                            } else if (selectedType == "foto" && gallerySelection.size <= 1 && selectedUri != null) {
                                publishError = ""
                                if (!editorBusy) {
                                    editorBusy = true
                                    val src = selectedUri!!
                                    val ratioNow = editorRatio; val z = editorZoom; val px = editorPanX; val py = editorPanY
                                    val matrixNow = qinblyEditMatrix(selectedFilter, filterIntensity, editorBrightness, editorContrast)
                                    val fontNow = editorTextFont; val colorNow = editorTextColor; val wordsNow = editorWordColors.toMap(); val textNow = editorText; val stickerNow = editorSticker; val strokesNow = editorStrokes.toList()
                                    Thread {
                                        try {
                                            val done = qinblyRenderEditedPhoto(context, src, ratioNow, z, px, py, matrixNow, textNow, stickerNow, strokesNow, fontNow, colorNow, wordsNow)
                                            android.os.Handler(android.os.Looper.getMainLooper()).post { bakedUri = done; editorBusy = false; publishSettings = true }
                                        } catch (e: Exception) {
                                            // Se não der para aplicar as edições, publica a foto original em vez de travar.
                                            android.os.Handler(android.os.Looper.getMainLooper()).post { bakedUri = null; editorBusy = false; publishSettings = true; onNotice("Não foi possível aplicar as edições; a foto original será usada.") }
                                        }
                                    }.start()
                                }
                            } else {
                                publishError = ""
                                publishSettings = true
                            }
                        },
                        modifier = Modifier.weight(1f).height(54.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = if (newEditorChrome) QinblyBrand.Accent else Color.White, contentColor = if (newEditorChrome) Color.White else Color.Black)
                    ) {
                        val advanceTint = if (newEditorChrome) Color.White else Color.Black
                        Text(if (editorBusy) "Aplicando…" else "Avançar", fontWeight = FontWeight.Bold, color = advanceTint)
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = advanceTint)
                    }
                 }
}
             }
}
         }
}
     }
} else if (coverPicker) {
        val frames = videoFrames
        Column(Modifier.fillMaxSize().background(QiblyTheme.background)) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { coverPicker = false }) {
                    Icon(QinblyCameraIcons.close, contentDescription = "Fechar", tint = QiblyTheme.text, modifier = Modifier.size(26.dp))
                }
                Text("Escolher capa", color = QiblyTheme.text, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            Box(Modifier.weight(1f).fillMaxWidth().padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                Box(
                    Modifier.fillMaxHeight().aspectRatio(9f / 16f, matchHeightConstraintsFirst = true)
                        .clip(RoundedCornerShape(16.dp)).background(Color(0xFF14181F))
                ) {
                    val shown = frames.getOrNull(coverIndex)
                    if (shown != null) AsyncImage(model = shown, contentDescription = "Capa", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    else QinblyLoadingSpinner(Modifier.fillMaxSize())
                    Text("Capa", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp,
                        modifier = Modifier.align(Alignment.TopStart).padding(10.dp).clip(RoundedCornerShape(10.dp)).background(Color(0x8C000000)).padding(horizontal = 9.dp, vertical = 3.dp))
                }
            }
            if (frames.isNotEmpty()) {
                BoxWithConstraints(Modifier.padding(horizontal = 18.dp, vertical = 12.dp).fillMaxWidth().height(62.dp)) {
                    val count = frames.size
                    val cellWidth = maxWidth / count
                    Row(Modifier.fillMaxSize().clip(RoundedCornerShape(10.dp))) {
                        frames.forEach { frame ->
                            AsyncImage(model = frame, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.weight(1f).fillMaxHeight())
                        }
                    }
                    Box(Modifier.offset(x = cellWidth * coverIndex).width(cellWidth).fillMaxHeight().border(3.dp, QinblyBrand.Accent, RoundedCornerShape(10.dp)))
                    Box(
                        Modifier.fillMaxSize()
                            .pointerInput(count) { detectTapGestures { tap -> coverIndex = (tap.x / size.width * count).toInt().coerceIn(0, count - 1) } }
                            .pointerInput(count) {
                                detectHorizontalDragGestures { change, _ ->
                                    coverIndex = (change.position.x / size.width * count).toInt().coerceIn(0, count - 1)
                                    change.consume()
                                }
                            }
                    )
                }
                Text("Arraste para escolher um momento do vídeo", color = Muted, fontSize = 11.sp,
                    modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
            Column(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp)) {
                Button(
                    onClick = { coverBitmap = frames.getOrNull(coverIndex); coverPicker = false },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = QinblyBrand.Accent, contentColor = Color.White)
                ) { Text("Usar esta capa", fontWeight = FontWeight.Bold, color = Color.White) }
            }
        }
    } else {
        Column(Modifier.fillMaxSize().background(QiblyTheme.background)) {
          Column(
            Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
          ) {
            val capState = remember { QinblyCaptionState(caption) }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { publishSettings = false }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = QiblyTheme.text)
                }
                Text("Nova publicação", color = QiblyTheme.text, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(Modifier.width(96.dp).height(140.dp).clip(RoundedCornerShape(14.dp)).background(Color(0xFF14181F)), contentAlignment = Alignment.Center) {
                    if ((bakedUri ?: selectedUri ?: sourceUri) != null && selectedType == "foto") {
                        AsyncImage(
                            model = bakedUri ?: selectedUri ?: sourceUri,
                            contentDescription = "Mídia selecionada",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        if (gallerySelection.size > 1) Text("${gallerySelection.size}", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 11.sp,
                            modifier = Modifier.align(Alignment.TopEnd).padding(6.dp).clip(RoundedCornerShape(10.dp)).background(Color(0x99000000)).padding(horizontal = 7.dp, vertical = 2.dp))
                    } else {
                        val cover = coverBitmap ?: videoFrames.firstOrNull()
                        if (cover != null) AsyncImage(model = cover, contentDescription = "Capa do vídeo", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White,
                            modifier = Modifier.align(Alignment.TopStart).padding(6.dp).size(24.dp).clip(CircleShape).background(Color(0x80000000)).padding(4.dp))
                        if (videoDurationLabel.isNotBlank()) Text(videoDurationLabel, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp,
                            modifier = Modifier.align(Alignment.BottomEnd).padding(6.dp).clip(RoundedCornerShape(8.dp)).background(Color(0x8C000000)).padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
                Column(Modifier.weight(1f).height(140.dp).clip(RoundedCornerShape(14.dp)).background(QinblyBrand.Accent.copy(alpha = 0.06f)).padding(12.dp)) {
                    QinblyLegendField(capState, 2200, "Escreva uma legenda…", QiblyTheme.text, 15.sp, Modifier.fillMaxWidth().weight(1f)) { caption = it; captionCursor = it.length }
                    Text("${caption.length} / 2200", modifier = Modifier.align(Alignment.End), color = Color.Gray, style = MaterialTheme.typography.labelSmall)
                }
            }
            if (selectedType == "vídeo") {
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(QinblyBrand.Accent.copy(alpha = 0.10f))
                        .clickable { coverIndex = 0; coverPicker = true }.padding(vertical = 11.dp),
                    horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(QinblyCreateIcons.gallery, contentDescription = null, tint = QinblyBrand.Accent, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Escolher capa", color = QinblyBrand.Accent, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
            QinblyHashtagField(capState) { caption = it; captionCursor = it.length }
            Column(Modifier.fillMaxWidth()) {
                Box(Modifier.fillMaxWidth().height(0.5.dp).background(QiblyTheme.border))
                QinblyPublishRow(QinblyCameraIcons.people, "Marcar pessoas", value = if (taggedPeople.isEmpty()) "" else taggedPeople.size.toString(), onClick = { showTagDialog = true })
                if (showTagDialog) {
                    QinblyTagPeopleDialog(prefs.getString("token", null), taggedPeople.toList(), QinblyBrand.Accent, onDismiss = { showTagDialog = false }) { chosen ->
                        taggedPeople.clear(); taggedPeople.addAll(chosen); showTagDialog = false
                    }
                }
                QinblyPublishRow(QinblyCameraIcons.globe, "Quem pode ver", value = postVisibility, onClick = {
                    postVisibility = if (postVisibility == "Todos") "Só eu" else "Todos"
                    onNotice(if (postVisibility == "Só eu") "Só você verá esta publicação, no seu perfil." else "Todos poderão ver esta publicação.")
                })
                QinblyPublishRow(QinblyCameraIcons.comment, "Permitir comentários", checked = allowComments, onChecked = { allowComments = it })
                QinblyPublishRow(QinblyCameraIcons.share, "Permitir compartilhar", checked = allowSharing, onChecked = { allowSharing = it })
                QinblyPublishRow(QinblyCameraIcons.download, "Permitir que salvem", subtitle = "Outras pessoas podem baixar este conteúdo", checked = allowSaveOthers, onChecked = { allowSaveOthers = it })
                QinblyPublishRow(QinblyCameraIcons.phoneSave, "Salvar no meu celular", subtitle = "Guarda uma cópia ao publicar", checked = saveToDevice, onChecked = { saveToDevice = it })
            }
            if (publishError.isNotBlank()) {
                Text(publishError, color = Color(0xFFFF8A80), style = MaterialTheme.typography.bodyMedium)
            }
          }
          Column(Modifier.fillMaxWidth().background(QiblyTheme.background).padding(horizontal = 18.dp, vertical = 10.dp)) {
            Button(
                onClick = {
                    val token = prefs.getString("token", null)
                    when {
                        token.isNullOrBlank() -> onNotice("Entre na sua conta antes de publicar.")
                        selectedUri == null && sourceUri == null -> { publishError = "Selecione uma foto ou vídeo antes de publicar."; onNotice(publishError); publishSettings = false; openQinblyGallery() }
                        !selectedMediaReady -> { publishError = "O vídeo da galeria não está acessível. Escolha-o novamente."; onNotice(publishError); publishSettings = false }
                        else -> {
                            publishError = ""
                            publishing = true
                            QinblyHashtagStore.record(context, caption)
                            Thread {
                                try {
                                    if (selectedUri == null && sourceUri == null) throw IllegalStateException("Selecione uma foto ou vídeo antes de publicar.")
                                    else {
                                        val selectedMediaUri = bakedUri ?: selectedUri ?: sourceUri ?: throw IllegalStateException("Mídia não selecionada.")
                                        val mime = context.contentResolver.getType(selectedMediaUri)?.takeIf { it.startsWith("image/") || it.startsWith("video/") } ?: if (selectedType == "vídeo") "video/mp4" else "image/jpeg"
                                        // Copia a mídia para cache para garantir tamanho exato no upload,
                                        // inclusive para URIs de câmera, galeria e provedores de nuvem.
                                        val tempUploadFile = java.io.File.createTempFile("qinbly-upload-", if (selectedType == "vídeo") ".mp4" else ".jpg", context.cacheDir)
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
                                        if (saveToDevice) {
                                            val saved = qinblySaveMediaCopy(context, selectedMediaUri, selectedType == "vídeo")
                                            android.os.Handler(android.os.Looper.getMainLooper()).post {
                                                onNotice(if (saved) "Cópia salva no celular." else "Não foi possível salvar a cópia no celular.")
                                            }
                                        }
                                        try {
                                            if (size <= 0L) throw IllegalStateException("O arquivo selecionado está vazio ou não pôde ser lido.")
                                            val extension = android.webkit.MimeTypeMap.getSingleton().getExtensionFromMimeType(mime) ?: if (mime.startsWith("video/")) "mp4" else "jpg"
                                            val fileName = "qinbly-${System.currentTimeMillis()}.$extension"
                                            val fileId = QinblyApi.uploadPostMedia(context, token, uploadUri, fileName, mime, size)
                                            if (selectedType == "foto" && gallerySelection.size > 1) {
                                                val uploadedIds = mutableListOf(fileId)
                                                for ((index, extraUri) in gallerySelection.drop(1).withIndex()) {
                                                    val extraMime = context.contentResolver.getType(extraUri)?.takeIf { it.startsWith("image/") } ?: "image/jpeg"
                                                    val extraFile = java.io.File.createTempFile("qinbly-album-", ".jpg", context.cacheDir)
                                                    try {
                                                        val extraSize = context.contentResolver.openInputStream(extraUri)?.use { input ->
                                                            extraFile.outputStream().use { output -> input.copyTo(output, 64 * 1024) }
                                                            extraFile.length()
                                                        } ?: throw IllegalStateException("Não foi possível ler uma das fotos.")
                                                        val extraExt = android.webkit.MimeTypeMap.getSingleton().getExtensionFromMimeType(extraMime) ?: "jpg"
                                                        uploadedIds.add(QinblyApi.uploadPostMedia(context, token, Uri.fromFile(extraFile),
                                                            "qinbly-album-${System.currentTimeMillis()}-${index}.${extraExt}", extraMime, extraSize))
                                                    } finally { extraFile.delete() }
                                                }
                                                QinblyApi.createPhotoAlbum(token, caption, uploadedIds, if (postVisibility == "Só eu") "private" else "public", taggedPeople.toList())
                                            } else {
                                                QinblyApi.createMediaPost(token, caption, fileId, if (mime.startsWith("video/")) "video" else "image", if (postVisibility == "Só eu") "private" else "public", taggedPeople.toList())
                                            }
                                        } finally {
                                            tempUploadFile?.delete()
                                        }
                                    }
                                    android.os.Handler(android.os.Looper.getMainLooper()).post {
                                        publishing = false
                                        caption = ""
                                        selectedUri = null
                                        gallerySelection.clear()
                                        selectedType = ""
                                        publishSettings = false
                                        showComposer = false
                                        sourceUri = null
                                        coverBitmap = null
                                        onNotice("Publicado")
                                        onCameraCancelled()
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
                colors = ButtonDefaults.buttonColors(containerColor = QinblyBrand.Accent, contentColor = Color.White)
            ) {
                if (publishing) CircularProgressIndicator(Modifier.size(19.dp), strokeWidth = 2.dp, color = Color.White)
                else Icon(Icons.Default.Send, contentDescription = null, tint = Color.White)
                Spacer(Modifier.width(8.dp))
                Text(if (publishing) "Publicando…" else "Publicar", fontWeight = FontWeight.Bold, color = Color.White)
            }
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
                    Text(category, Modifier.padding(horizontal = 10.dp, vertical = 10.dp), color = QiblyTheme.text, style = MaterialTheme.typography.labelMedium, maxLines = 1)
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
                        Icon(Icons.Default.Radio, contentDescription = null, tint = Color.White, modifier = Modifier.size(31.dp))
                    }
                    Text("Entre no universo das Lives", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Descubra criadores, jogos, música e conversas ao vivo.", color = Muted, style = MaterialTheme.typography.bodyMedium)
                    Button(onClick = { onNotice("As transmissões e a reprodução ao vivo ainda precisam ser conectadas ao servidor de mídia.") }, colors = ButtonDefaults.buttonColors(containerColor = Pink), shape = RoundedCornerShape(14.dp)) {
                        Text("Explorar transmissões")
                    }
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Lives recomendadas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Text("${QinblyBrand.Name} LIVE", color = Pink, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
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
        Text("${QinblyBrand.Name} • Protótipo Android", color = Muted, style = MaterialTheme.typography.labelSmall)
    }
}