package app.qinbly.android

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import android.content.Context
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
internal fun qinblyMakeSticker(context: Context, uri: Uri, removeBackground: Boolean, onDone: (File?, String) -> Unit) {
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

/** Janela "Criar figurinha": escolhe uma foto, decide se tira o fundo e salva na sua coleção. */
@Composable
internal fun QinblyStickerCreateDialog(accent: Color, onNotice: (String) -> Unit, onDismiss: () -> Unit, onCreated: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("qinbly_session", Context.MODE_PRIVATE) }
    var busy by remember { mutableStateOf(false) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { source ->
        if (source == null) { onDismiss(); return@rememberLauncherForActivityResult }
        val token = prefs.getString("token", null)
        if (token.isNullOrBlank()) { onNotice("Entre na sua conta para criar figurinhas."); onDismiss(); return@rememberLauncherForActivityResult }
        busy = true
        qinblyMakeSticker(context, source, false) { file, info ->
            if (file == null) { busy = false; onNotice(info.ifBlank { "Não foi possível criar a figurinha." }); onDismiss(); return@qinblyMakeSticker }
            Thread {
                val main = Handler(Looper.getMainLooper())
                try {
                    QinblyApi.createSticker(context, token, file, "image/png")
                    main.post { busy = false; onNotice("Figurinha criada."); onCreated(); onDismiss() }
                } catch (e: Exception) {
                    main.post { busy = false; onNotice(e.message ?: "Não foi possível salvar a figurinha."); onDismiss() }
                } finally { file.delete() }
            }.start()
        }
    }
    LaunchedEffect(Unit) { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
    if (busy) {
        androidx.compose.ui.window.Dialog(onDismissRequest = {}) { CircularProgressIndicator(color = accent, modifier = Modifier.size(36.dp)) }
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
        Text("Minhas figurinhas (segure para excluir)", color = textColor.copy(alpha = 0.65f), fontSize = 11.sp, modifier = Modifier.padding(start = 4.dp, bottom = 2.dp))
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
    LaunchedEffect(refreshKey) {
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
    toDelete?.let { st ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = { Text("Excluir figurinha?") },
            confirmButton = {
                TextButton(onClick = {
                    toDelete = null
                    val token = prefs.getString("token", null) ?: return@TextButton
                    list = list.filterNot { it.id == st.id }
                    Thread {
                        try { QinblyApi.deleteSticker(token, st.id) }
                        catch (_: Exception) { Handler(Looper.getMainLooper()).post { onNotice("Não foi possível excluir a figurinha.") } }
                    }.start()
                }) { Text("Excluir", color = accent, fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { toDelete = null }) { Text("Cancelar") } }
        )
    }
}
