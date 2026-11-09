package app.qinbly.android

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
    var picked by remember { mutableStateOf<Uri?>(null) }
    var removeBg by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri -> if (uri != null) picked = uri }
    AlertDialog(
        onDismissRequest = { if (!busy) onDismiss() },
        title = { Text("Criar figurinha") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Escolha uma foto. Ela vira uma figurinha quadrada que você usa nas conversas.", fontSize = 14.sp)
                Button(onClick = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }, enabled = !busy,
                    colors = ButtonDefaults.buttonColors(containerColor = accent)) { Text(if (picked == null) "Escolher foto" else "Trocar foto", color = Color.White) }
                if (picked != null) {
                    AsyncImage(model = picked, contentDescription = "Foto escolhida", modifier = Modifier.size(110.dp).clip(RoundedCornerShape(16.dp)).background(Color(0x11000000)))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = removeBg, onCheckedChange = { removeBg = it }, enabled = !busy, colors = CheckboxDefaults.colors(checkedColor = accent))
                    Text("Tirar o fundo (deixa só a pessoa)", fontSize = 14.sp)
                }
                if (busy) CircularProgressIndicator(color = accent, modifier = Modifier.size(26.dp))
            }
        },
        confirmButton = {
            TextButton(enabled = picked != null && !busy, onClick = {
                val source = picked ?: return@TextButton
                val token = prefs.getString("token", null)
                if (token.isNullOrBlank()) { onNotice("Entre na sua conta para criar figurinhas."); return@TextButton }
                busy = true
                qinblyMakeSticker(context, source, removeBg) { file, info ->
                    if (file == null) { busy = false; onNotice(info.ifBlank { "Não foi possível criar a figurinha." }); return@qinblyMakeSticker }
                    Thread {
                        val main = Handler(Looper.getMainLooper())
                        try {
                            QinblyApi.createSticker(context, token, file, "image/png")
                            main.post { busy = false; onNotice(if (info.isNotBlank()) "Figurinha criada. $info" else "Figurinha criada."); onCreated(); onDismiss() }
                        } catch (e: Exception) {
                            main.post { busy = false; onNotice(e.message ?: "Não foi possível salvar a figurinha.") }
                        } finally { file.delete() }
                    }.start()
                }
            }) { Text("Criar", color = accent, fontWeight = FontWeight.Bold) }
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !busy) { Text("Cancelar") } }
    )
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
