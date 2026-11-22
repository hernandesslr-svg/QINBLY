package app.qinbly.android

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties

/**
 * PADRÃO DO APLICATIVO (ver PADRAO-SELETOR-FOTO.md): onde a pessoa escolhe foto da galeria, a câmera é a PRIMEIRA
 * célula da grade, e a câmera que abre é sempre a câmera padrão do app (QinblyCameraCapture, com o mesmo editor).
 * "Ver todas as fotos" abre a galeria completa do celular.
 *
 * Uso: troca `rememberLauncherForActivityResult(PickVisualMedia())` por `rememberQinblyPhotoLauncher { uri -> ... }`
 * (uma foto) ou `rememberQinblyMultiPhotoLauncher(max) { uris -> ... }` (várias) e continua chamando `.launch(...)`.
 */
internal class QinblyPhotoLauncher(private val openIt: () -> Unit) {
    @Suppress("UNUSED_PARAMETER")
    fun launch(request: Any? = null) = openIt()
}

/** Uma foto. Recebe null se a pessoa fechou sem escolher (igual ao seletor do Android). */
@Composable
internal fun rememberQinblyPhotoLauncher(onCamera: (() -> Unit)? = null, onResult: (Uri?) -> Unit): QinblyPhotoLauncher {
    var open by remember { mutableStateOf(false) }
    if (open) QinblyPhotoPickerDialog(
        max = 1, onCamera = onCamera,
        onDone = { open = false; onResult(it.firstOrNull()) },
        onClose = { open = false; onResult(null) },
        onCameraHandled = { open = false }
    )
    return remember { QinblyPhotoLauncher { open = true } }
}

/** Várias fotos (até max). Recebe lista vazia se a pessoa fechou sem escolher. */
@Composable
internal fun rememberQinblyMultiPhotoLauncher(max: Int, onCamera: (() -> Unit)? = null, onResult: (List<Uri>) -> Unit): QinblyPhotoLauncher {
    var open by remember { mutableStateOf(false) }
    if (open) QinblyPhotoPickerDialog(
        max = max.coerceAtLeast(2), onCamera = onCamera,
        onDone = { open = false; onResult(it) },
        onClose = { open = false; onResult(emptyList()) },
        onCameraHandled = { open = false }
    )
    return remember { QinblyPhotoLauncher { open = true } }
}

@Composable
private fun QinblyPhotoPickerDialog(
    max: Int, onCamera: (() -> Unit)?, onDone: (List<Uri>) -> Unit, onClose: () -> Unit, onCameraHandled: () -> Unit
) {
    val context = LocalContext.current
    val multi = max > 1
    var granted by remember { mutableStateOf(hasGalleryPermission(context)) }
    var items by remember { mutableStateOf<List<GalleryItem>>(emptyList()) }
    val selected = remember { mutableStateListOf<Uri>() }
    var showCamera by remember { mutableStateOf(false) }
    val perms = if (android.os.Build.VERSION.SDK_INT >= 33) arrayOf(android.Manifest.permission.READ_MEDIA_IMAGES)
        else arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { granted = hasGalleryPermission(context) }
    val systemSingle = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri -> if (uri != null) onDone(listOf(uri)) }
    val systemMulti = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(max.coerceAtLeast(2))) { uris -> if (uris.isNotEmpty()) onDone(uris) }
    fun openSystem() {
        val req = PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        try { if (multi) systemMulti.launch(req) else systemSingle.launch(req) } catch (_: Exception) { }
    }
    LaunchedEffect(Unit) { if (!granted) permissionLauncher.launch(perms) }
    LaunchedEffect(granted) {
        if (granted) items = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) { loadRecentMedia(context).filter { !it.video } }
    }
    if (showCamera) QinblyCameraCapture(onResult = { u -> showCamera = false; onDone(listOf(u)) }, onDismiss = { showCamera = false })

    QinblyDialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
        lift = false
    ) {
        Column(Modifier.fillMaxSize().background(Color(0xFF171719)).statusBarsPadding().padding(bottom = qinblyNavBarPadding())) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(40.dp).clip(CircleShape).background(Color(0x22FFFFFF)).clickable { onClose() }, contentAlignment = Alignment.Center) { QinblyCloseIcon(Color.White, 22.dp) }
                Text(if (multi && selected.isNotEmpty()) "${selected.size} selecionada(s)" else "Escolher foto", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp,
                    modifier = Modifier.weight(1f).padding(horizontal = 12.dp))
                if (multi) Text("Pronto", color = if (selected.isEmpty()) Color(0x66FFFFFF) else Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp,
                    modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(if (selected.isEmpty()) Color(0x22FFFFFF) else Color(0xFF2F68E0))
                        .clickable(enabled = selected.isNotEmpty()) { onDone(selected.toList()) }.padding(horizontal = 18.dp, vertical = 9.dp))
            }
            LazyVerticalGrid(GridCells.Fixed(3), modifier = Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                // A câmera é sempre a primeira célula.
                item {
                    Box(Modifier.aspectRatio(1f).background(Color(0xFF262628)).clickable {
                        if (onCamera != null) { onCameraHandled(); onCamera() } else showCamera = true
                    }, contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            QinblyIconOf(QinblyIcon.PhotoCamera, Color.White, 30.dp)
                            Text("Câmera", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                }
                items(items, key = { it.uri.toString() }) { g ->
                    val idx = selected.indexOf(g.uri)
                    Box(Modifier.aspectRatio(1f).clickable {
                        if (!multi) onDone(listOf(g.uri))
                        else if (idx >= 0) selected.remove(g.uri) else if (selected.size < max) selected.add(g.uri)
                    }) {
                        MediaThumb(g.uri, false, Modifier.fillMaxSize())
                        if (multi) Box(
                            Modifier.align(Alignment.TopEnd).padding(6.dp).size(22.dp).clip(CircleShape)
                                .background(if (idx >= 0) Color(0xFF2F68E0) else Color(0x66000000)),
                            contentAlignment = Alignment.Center
                        ) { if (idx >= 0) Text("${idx + 1}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                    }
                }
                if (!granted) item(span = { GridItemSpan(maxLineSpan) }) {
                    Column(Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Permita o acesso às suas fotos para escolher daqui.", color = Color(0xFFF2F4F8), fontSize = 16.sp)
                        Text("Permitir acesso", color = Color.White, fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(Color(0xFF2F68E0)).clickable { permissionLauncher.launch(perms) }.padding(horizontal = 20.dp, vertical = 10.dp))
                    }
                }
            }
            Text("Ver todas as fotos", color = Color(0xFF7FA6F2), fontWeight = FontWeight.SemiBold, fontSize = 16.sp,
                modifier = Modifier.fillMaxWidth().clickable { openSystem() }.padding(vertical = 14.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}
