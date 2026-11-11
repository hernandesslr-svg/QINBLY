package app.qinbly.android

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import android.content.Context
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.fillMaxSize
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AColor
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import java.io.File
import java.io.FileOutputStream
import kotlin.math.min

/**
 * Monta a imagem da figurinha (512x512, PNG): corta a foto em quadrado e, se pedido, tira o fundo deixando só a pessoa.
 * Sem tirar o fundo, a figurinha fica com cantos arredondados e uma borda branca.
 * O arquivo volta em [onDone] na thread principal; [info] traz um aviso (por exemplo, "não achei uma pessoa").
 */
internal fun qinblyMakeSticker(context: Context, uri: Uri, removeBackground: Boolean, caption: String = "", captionColor: Int = AColor.WHITE, onDone: (File?, String) -> Unit) {
    val main = Handler(Looper.getMainLooper())
    Thread {
        var file: File? = null
        var info = ""
        try {
            val loaded = fxLoadUpright(context, uri)
            val side = min(loaded.width, loaded.height)
            val cropped = Bitmap.createBitmap(loaded, (loaded.width - side) / 2, (loaded.height - side) / 2, side, side)
            if (cropped !== loaded) loaded.recycle()
            val square = Bitmap.createScaledBitmap(cropped, 512, 512, true)
            if (square !== cropped) cropped.recycle()
            var result: Bitmap? = null
            if (removeBackground) {
                result = fxPersonCutout(square)
                if (result == null) info = "Não achei uma pessoa na foto; usei a foto inteira."
            }
            if (result == null) {
                val out = Bitmap.createBitmap(512, 512, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(out)
                val round = Path().apply { addRoundRect(RectF(0f, 0f, 512f, 512f), 72f, 72f, Path.Direction.CW) }
                canvas.save()
                canvas.clipPath(round)
                canvas.drawBitmap(square, 0f, 0f, null)
                canvas.restore()
                val border = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeWidth = 14f; color = AColor.WHITE }
                canvas.drawRoundRect(RectF(7f, 7f, 505f, 505f), 66f, 66f, border)
                result = out
            }
            square.recycle()
            if (caption.isNotBlank()) {
                val c = Canvas(result)
                val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = captionColor; typeface = android.graphics.Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER; textSize = 62f }
                while (fill.measureText(caption) > 440f && fill.textSize > 18f) fill.textSize -= 2f
                val stroke = Paint(fill).apply { style = Paint.Style.STROKE; strokeWidth = 9f; color = AColor.BLACK; strokeJoin = Paint.Join.ROUND }
                c.drawText(caption, 256f, 462f, stroke)
                c.drawText(caption, 256f, 462f, fill)
            }
            val f = File(context.cacheDir, "qinbly-sticker-${System.currentTimeMillis()}.png")
            FileOutputStream(f).use { result.compress(Bitmap.CompressFormat.PNG, 100, it) }
            result.recycle()
            file = f
        } catch (t: Throwable) {
            info = "Não foi possível criar a figurinha com essa foto."
        }
        main.post { onDone(file, info) }
    }.start()
}

/** Editor "Criar figurinha" (estilo WhatsApp): escolhe a foto (ou parte de uma figurinha existente), escreve um texto e salva. */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
internal fun QinblyStickerCreateDialog(accent: Color, onNotice: (String) -> Unit, onDismiss: () -> Unit, onCreated: () -> Unit, editing: QinblyApi.CustomSticker? = null) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("qinbly_session", Context.MODE_PRIVATE) }
    var busy by remember { mutableStateOf(false) }
    var picked by remember { mutableStateOf<Uri?>(null) }
    var caption by remember { mutableStateOf("") }
    var colorIdx by remember { mutableStateOf(0) }
    val palette = listOf(AColor.WHITE, 0xFFFFD60A.toInt(), 0xFFFF4F9A.toInt(), 0xFF22D3EE.toInt(), 0xFF7C4DFF.toInt(), AColor.BLACK)
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri == null) onDismiss() else picked = uri
    }
    LaunchedEffect(Unit) {
        if (editing == null) picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        else {
            try {
                val f = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    val tmp = File(context.cacheDir, "edit-sticker-${System.currentTimeMillis()}.png")
                    java.net.URL(editing.url).openStream().use { i -> FileOutputStream(tmp).use { o -> i.copyTo(o) } }
                    tmp
                }
                picked = Uri.fromFile(f)
            } catch (_: Exception) { onNotice("Não foi possível abrir a figurinha."); onDismiss() }
        }
    }
    val src = picked
    if (src != null) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { if (!busy) onDismiss() }, properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
            Column(Modifier.fillMaxSize().background(Color(0xFF130F1D)).imePadding().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(Modifier.fillMaxWidth().padding(top = 24.dp), verticalAlignment = Alignment.CenterVertically) {
                    androidx.compose.material3.IconButton(onClick = { if (!busy) onDismiss() }) { Icon(androidx.compose.material.icons.Icons.Default.Close, contentDescription = "Fechar", tint = Color.White) }
                    Text(if (editing != null) "Editar figurinha" else "Criar figurinha", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp))
                }
                Box(Modifier.padding(top = 24.dp).size(300.dp).clip(RoundedCornerShape(36.dp)).background(Color(0x22FFFFFF)), contentAlignment = Alignment.BottomCenter) {
                    AsyncImage(model = src, contentDescription = "Prévia", contentScale = androidx.compose.ui.layout.ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    if (caption.isNotBlank()) Box(Modifier.padding(bottom = 22.dp)) {
                        val sz = (300 * 0.12f).sp
                        Text(caption, color = Color.Black, fontSize = sz, fontWeight = FontWeight.Bold, maxLines = 1, style = androidx.compose.ui.text.TextStyle(drawStyle = androidx.compose.ui.graphics.drawscope.Stroke(width = 14f, join = androidx.compose.ui.graphics.StrokeJoin.Round)))
                        Text(caption, color = Color(palette[colorIdx]), fontSize = sz, fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                }
                androidx.compose.material3.OutlinedTextField(
                    value = caption, onValueChange = { caption = it.take(40) }, singleLine = true, enabled = !busy,
                    placeholder = { Text("Escreva algo na figurinha…") },
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = accent, unfocusedBorderColor = Color(0x55FFFFFF), cursorColor = accent),
                    modifier = Modifier.padding(top = 20.dp).fillMaxWidth()
                )
                Row(Modifier.padding(top = 14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    palette.forEachIndexed { i, c ->
                        Box(Modifier.size(34.dp).clip(androidx.compose.foundation.shape.CircleShape).background(Color(c))
                            .border(if (i == colorIdx) 3.dp else 1.dp, if (i == colorIdx) accent else Color(0x66FFFFFF), androidx.compose.foundation.shape.CircleShape)
                            .clickable { colorIdx = i })
                    }
                }
                Box(Modifier.weight(1f))
                Button(
                    enabled = !busy, modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accent),
                    onClick = {
                        val token = prefs.getString("token", null)
                        if (token.isNullOrBlank()) { onNotice("Entre na sua conta para criar figurinhas."); return@Button }
                        busy = true
                        qinblyMakeSticker(context, src, false, caption.trim(), palette[colorIdx]) { file, info ->
                            if (file == null) { busy = false; onNotice(info.ifBlank { "Não foi possível criar a figurinha." }); return@qinblyMakeSticker }
                            Thread {
                                val main = Handler(Looper.getMainLooper())
                                try {
                                    QinblyApi.createSticker(context, token, file, "image/png")
                                    if (editing != null) try { QinblyApi.deleteSticker(token, editing.id) } catch (_: Exception) {}
                                    main.post { busy = false; onNotice("Figurinha salva."); onCreated(); onDismiss() }
                                } catch (e: Exception) {
                                    main.post { busy = false; onNotice(e.message ?: "Não foi possível salvar a figurinha.") }
                                } finally { file.delete() }
                            }.start()
                        }
                    }
                ) { if (busy) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp)) else Text("Salvar figurinha", color = Color.White, fontWeight = FontWeight.Bold) }
            }
        }
    }
}

/** Faixa "Minhas figurinhas": toque para enviar, segure para excluir. Some se você ainda não criou nenhuma. */
@Composable
internal fun QinblyCustomStickerRow(accent: Color, textColor: Color, refreshKey: Int, onPick: (QinblyApi.CustomSticker) -> Unit, onNotice: (String) -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("qinbly_session", Context.MODE_PRIVATE) }
    var list by remember { mutableStateOf<List<QinblyApi.CustomSticker>>(emptyList()) }
    LaunchedEffect(refreshKey) {
        val token = prefs.getString("token", null)
        list = if (token.isNullOrBlank()) emptyList() else try {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) { QinblyApi.getMyStickers(token) }
        } catch (_: Exception) { emptyList() }
    }
    if (list.isEmpty()) return
    Column(modifier.fillMaxWidth().padding(horizontal = 10.dp)) {
        Text("Minhas figurinhas", color = textColor.copy(alpha = 0.65f), fontSize = 11.sp, modifier = Modifier.padding(start = 4.dp, bottom = 2.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(list, key = { it.id }) { sticker ->
                Box(
                    Modifier.size(64.dp).clip(RoundedCornerShape(12.dp)).background(accent.copy(alpha = 0.07f)).padding(4.dp)
                        .pointerInput(sticker.id) {
                            detectTapGestures(
                                onTap = { onPick(sticker) },
                                onLongPress = {
                                    val token = prefs.getString("token", null) ?: return@detectTapGestures
                                    list = list.filterNot { it.id == sticker.id }
                                    Thread {
                                        try { QinblyApi.deleteSticker(token, sticker.id) }
                                        catch (_: Exception) { Handler(Looper.getMainLooper()).post { onNotice("Não foi possível excluir a figurinha.") } }
                                    }.start()
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(model = sticker.url, contentDescription = "Figurinha", modifier = Modifier.size(56.dp))
                }
            }
        }
    }
}

/** Painel de figurinhas (estilo WhatsApp): grade com o "+" para criar e todas as suas figurinhas salvas. Segurar pede confirmação para excluir. */
@Composable
internal fun QinblyStickerGrid(accent: Color, cardColor: Color, textColor: Color, refreshKey: Int, onCreate: () -> Unit, onPick: (QinblyApi.CustomSticker) -> Unit, onNotice: (String) -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("qinbly_session", Context.MODE_PRIVATE) }
    var list by remember { mutableStateOf<List<QinblyApi.CustomSticker>>(emptyList()) }
    var toDelete by remember { mutableStateOf<QinblyApi.CustomSticker?>(null) }
    var editSticker by remember { mutableStateOf<QinblyApi.CustomSticker?>(null) }
    var reload by remember { mutableStateOf(0) }
    LaunchedEffect(refreshKey, reload) {
        val token = prefs.getString("token", null)
        if (!token.isNullOrBlank()) {
            try { list = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) { QinblyApi.getMyStickers(token) } }
            catch (_: Exception) { onNotice("Não foi possível carregar suas figurinhas.") }
        }
    }
    androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
        columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(4),
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Box(Modifier.aspectRatio(1f).clip(RoundedCornerShape(14.dp)).background(cardColor).border(1.5.dp, accent, RoundedCornerShape(14.dp)).clickable { onCreate() }, contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Add, contentDescription = "Criar figurinha", tint = accent, modifier = Modifier.size(30.dp))
            }
        }
        items(list.size, key = { list[it].id }) { i ->
            val sticker = list[i]
            Box(
                Modifier.aspectRatio(1f).clip(RoundedCornerShape(14.dp)).pointerInput(sticker.id) {
                    detectTapGestures(onTap = { onPick(sticker) }, onLongPress = { toDelete = sticker })
                }.padding(2.dp),
                contentAlignment = Alignment.Center
            ) { AsyncImage(model = sticker.url, contentDescription = "Figurinha", modifier = Modifier.fillMaxSize()) }
        }
    }
    editSticker?.let { es -> QinblyStickerCreateDialog(accent, onNotice, onDismiss = { editSticker = null }, onCreated = { reload++ }, editing = es) }
    toDelete?.let { st ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = { Text("Figurinha") },
            text = {
                Column {
                    TextButton(onClick = { editSticker = st; toDelete = null }) { Text("Editar (adicionar texto)", color = accent) }
                    TextButton(onClick = {
                        toDelete = null
                        val token = prefs.getString("token", null) ?: return@TextButton
                        list = list.filterNot { it.id == st.id }
                        Thread {
                            try { QinblyApi.deleteSticker(token, st.id) }
                            catch (_: Exception) { Handler(Looper.getMainLooper()).post { onNotice("Não foi possível excluir a figurinha.") } }
                        }.start()
                    }) { Text("Remover", color = Color(0xFFE5484D)) }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { toDelete = null }) { Text("Cancelar") } }
        )
    }
}
