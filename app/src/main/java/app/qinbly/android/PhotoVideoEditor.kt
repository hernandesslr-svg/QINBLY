package app.qinbly.android

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Effect
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.effect.MatrixTransformation
import androidx.media3.effect.Presentation
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.EditedMediaItemSequence
import androidx.media3.transformer.Effects
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.Transformer
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.roundToInt

private val PvBlue = Color(0xFF7AA8FF)
private const val PvPxPerSec = 22
private const val PvMaxPhotos = 20
private val PvTransitions = listOf("Nenhuma", "Zoom", "Deslizar", "Surgir")

/** Uma foto na linha do tempo do vídeo: duração, filtro e transição de entrada. */
internal data class PvClip(val uri: Uri, val secs: Int = 3, val filter: String = "Original", val transition: String = "Nenhuma")

private fun pvTime(sec: Int): String = "${sec / 60}:${(sec % 60).toString().padStart(2, '0')}"

private fun pvFilter(name: String): ColorFilter? =
    if (name == "Original") null else ColorFilter.colorMatrix(ColorMatrix(qinblyPvFilterMatrix(name)))

/**
 * Tela de edição de vídeo de fotos. Prévia no topo, linha do tempo no meio e ferramentas embaixo.
 * As ferramentas valem para a foto que está sob a linha branca. Tudo tem desfazer e refazer.
 */
@Composable
internal fun QinblyPhotoVideoEditor(
    clips: SnapshotStateList<PvClip>,
    pendingAdd: List<Uri>,
    onPendingConsumed: () -> Unit,
    busy: Boolean,
    onAdd: () -> Unit,
    onClose: () -> Unit,
    onDone: () -> Unit,
    onNotice: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    val pps = with(LocalDensity.current) { PvPxPerSec.dp.toPx() }
    val scroll = rememberScrollState()
    var playing by remember { mutableStateOf(false) }
    var tool by remember { mutableStateOf("") }
    val undo = remember { mutableStateListOf<List<PvClip>>() }
    val redo = remember { mutableStateListOf<List<PvClip>>() }

    fun commit(next: List<PvClip>) {
        undo.add(clips.toList())
        if (undo.size > 30) undo.removeAt(0)
        redo.clear()
        clips.clear()
        clips.addAll(next)
    }
    fun startSecOf(index: Int): Int {
        var acc = 0
        for (i in 0 until index.coerceAtMost(clips.size)) acc += clips[i].secs
        return acc
    }
    val totalSecs = clips.sumOf { it.secs }
    val posSec = (scroll.value / pps).coerceIn(0f, totalSecs.toFloat())
    var curIndex = clips.lastIndex.coerceAtLeast(0)
    run {
        var acc = 0
        for (i in clips.indices) {
            if (posSec < acc + clips[i].secs) { curIndex = i; break }
            acc += clips[i].secs
        }
    }
    val cur = clips.getOrNull(curIndex)
    val tIn = posSec - startSecOf(curIndex)

    BackHandler(enabled = !busy) { onClose() }

    // Fotos escolhidas na galeria chegam aqui e entram no fim da linha do tempo.
    LaunchedEffect(pendingAdd) {
        if (pendingAdd.isNotEmpty()) {
            val room = PvMaxPhotos - clips.size
            if (room <= 0) onNotice("O vídeo aceita até $PvMaxPhotos fotos.")
            else {
                val firstNew = clips.size
                commit(clips.toList() + pendingAdd.take(room).map { PvClip(it) })
                playing = false
                scroll.scrollTo((startSecOf(firstNew) * pps + 1f).roundToInt())
            }
            onPendingConsumed()
        }
    }
    // Tocar a prévia: a linha do tempo anda sozinha; arrastar com o dedo pausa.
    LaunchedEffect(playing) {
        if (!playing) return@LaunchedEffect
        val end = clips.sumOf { it.secs } * pps
        if (scroll.value >= end.toInt() - 1) scroll.scrollTo(0)
        var acc = scroll.value.toFloat()
        var last = withFrameNanos { it }
        while (playing) {
            val now = withFrameNanos { it }
            acc += pps * ((now - last) / 1_000_000_000f)
            last = now
            if (acc >= end) {
                scroll.scrollTo(end.toInt())
                playing = false
            } else scroll.scrollTo(acc.roundToInt())
        }
    }
    LaunchedEffect(scroll) {
        scroll.interactionSource.interactions.collect { if (it is DragInteraction.Start) playing = false }
    }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(40.dp).clip(CircleShape).background(Color(0xE63C4046)).clickable(enabled = !busy) { onClose() }, contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Voltar", tint = Color.White, modifier = Modifier.size(26.dp))
                }
                Box(Modifier.size(40.dp).clip(CircleShape).background(PvBlue).clickable(enabled = !busy && clips.isNotEmpty()) { playing = false; onDone() }, contentAlignment = Alignment.Center) {
                    if (busy) CircularProgressIndicator(Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
                    else Icon(Icons.Default.ArrowForward, contentDescription = "Continuar", tint = Color.White, modifier = Modifier.size(24.dp))
                }
            }

            Box(Modifier.weight(1f).fillMaxWidth().padding(horizontal = 56.dp, vertical = 2.dp), contentAlignment = Alignment.Center) {
                if (cur != null) {
                    Box(Modifier.fillMaxHeight().aspectRatio(9f / 16f).clip(RoundedCornerShape(14.dp)).background(Color(0xFF15181C)).clickable { playing = !playing }) {
                        AsyncImage(
                            model = cur.uri,
                            contentDescription = "Prévia",
                            contentScale = ContentScale.Crop,
                            colorFilter = pvFilter(cur.filter),
                            modifier = Modifier.fillMaxSize().graphicsLayer {
                                val p = (tIn / 0.5f).coerceIn(0f, 1f)
                                when (cur.transition) {
                                    "Zoom" -> { val s = 1f + 0.15f * (tIn / cur.secs).coerceIn(0f, 1f); scaleX = s; scaleY = s }
                                    "Deslizar" -> { translationX = (1f - p) * size.width }
                                    "Surgir" -> { val s = 0.6f + 0.4f * p; scaleX = s; scaleY = s }
                                    else -> { }
                                }
                            }
                        )
                        Text("Foto ${curIndex + 1} de ${clips.size}", color = Color.White, fontSize = 12.sp,
                            modifier = Modifier.align(Alignment.BottomStart).padding(10.dp).background(Color(0x80000000), RoundedCornerShape(10.dp)).padding(horizontal = 9.dp, vertical = 3.dp))
                    }
                }
            }

            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                Row(Modifier.weight(1f)) {
                    Text(pvTime(posSec.toInt()), color = Color.White, fontSize = 14.sp)
                    Text(" / ${pvTime(totalSecs)}", color = Color(0xFF9AA4B2), fontSize = 14.sp)
                }
                Box(Modifier.size(52.dp).clip(CircleShape).clickable(enabled = clips.isNotEmpty()) { playing = !playing }, contentAlignment = Alignment.Center) {
                    Icon(if (playing) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = if (playing) "Pausar" else "Tocar", tint = Color.White, modifier = Modifier.size(44.dp))
                }
                Row(Modifier.weight(1f), horizontalArrangement = Arrangement.End) {
                    Box(Modifier.size(40.dp).clip(CircleShape).alpha(if (undo.isEmpty()) 0.35f else 1f).clickable(enabled = undo.isNotEmpty() && !busy) {
                        redo.add(clips.toList())
                        val prev = undo.removeAt(undo.lastIndex)
                        clips.clear(); clips.addAll(prev)
                    }, contentAlignment = Alignment.Center) { Icon(Icons.Default.Undo, contentDescription = "Desfazer", tint = Color.White, modifier = Modifier.size(22.dp)) }
                    Box(Modifier.size(40.dp).clip(CircleShape).alpha(if (redo.isEmpty()) 0.35f else 1f).clickable(enabled = redo.isNotEmpty() && !busy) {
                        undo.add(clips.toList())
                        val next = redo.removeAt(redo.lastIndex)
                        clips.clear(); clips.addAll(next)
                    }, contentAlignment = Alignment.Center) { Icon(Icons.Default.Redo, contentDescription = "Refazer", tint = Color.White, modifier = Modifier.size(22.dp)) }
                }
            }

            // Linha do tempo: arraste para andar; a foto sob a linha branca é a que as ferramentas editam.
            BoxWithConstraints(Modifier.fillMaxWidth().height(112.dp)) {
                val half = maxWidth / 2
                Row(Modifier.horizontalScroll(scroll)) {
                    Spacer(Modifier.width(half))
                    Column {
                        Row(Modifier.height(18.dp)) {
                            for (m in 0..(totalSecs / 3)) {
                                Box(Modifier.width((3 * PvPxPerSec).dp)) { Text(pvTime(m * 3), color = Color(0xFF8A94A3), fontSize = 11.sp) }
                            }
                        }
                        Row {
                            clips.forEachIndexed { i, c ->
                                Column(Modifier.width((c.secs * PvPxPerSec).dp).clickable {
                                    playing = false
                                    scope.launch { scroll.animateScrollTo((startSecOf(i) * pps + 1f).roundToInt()) }
                                }) {
                                    Box(
                                        Modifier.padding(end = 3.dp).fillMaxWidth().height(70.dp).clip(RoundedCornerShape(9.dp))
                                            .border(if (i == curIndex) BorderStroke(2.dp, Color.White) else BorderStroke(1.dp, Color(0x40FFFFFF)), RoundedCornerShape(9.dp))
                                    ) {
                                        AsyncImage(model = c.uri, contentDescription = "Foto ${i + 1}", contentScale = ContentScale.Crop, colorFilter = pvFilter(c.filter), modifier = Modifier.fillMaxSize())
                                    }
                                    Text("${c.secs} s", color = Color(0xFF9AA4B2), fontSize = 11.sp, modifier = Modifier.padding(top = 3.dp))
                                }
                            }
                            if (clips.size < PvMaxPhotos) {
                                Box(
                                    Modifier.padding(start = 4.dp).width(46.dp).height(70.dp).clip(RoundedCornerShape(9.dp))
                                        .border(BorderStroke(1.dp, Color(0x99FFFFFF)), RoundedCornerShape(9.dp)).clickable(enabled = !busy) { onAdd() },
                                    contentAlignment = Alignment.Center
                                ) { Icon(Icons.Default.AddPhotoAlternate, contentDescription = "Adicionar foto", tint = Color.White, modifier = Modifier.size(24.dp)) }
                            }
                        }
                    }
                    Spacer(Modifier.width(half))
                }
                Box(Modifier.align(Alignment.TopCenter).width(2.dp).fillMaxHeight().background(Color.White))
            }

            // Opções da ferramenta escolhida (valem para a foto sob a linha branca).
            Box(Modifier.fillMaxWidth().height(48.dp), contentAlignment = Alignment.CenterStart) {
                if (cur != null) {
                    Row(Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        when (tool) {
                            "dur" -> {
                                for (s in 1..10) PvChip("$s s", cur.secs == s) {
                                    commit(clips.toMutableList().also { it[curIndex] = cur.copy(secs = s) })
                                }
                            }
                            "tr" -> {
                                for (t in PvTransitions) PvChip(t, cur.transition == t) {
                                    commit(clips.toMutableList().also { it[curIndex] = cur.copy(transition = t) })
                                }
                            }
                            "fil" -> {
                                for (f in qinblyPvFilterNames()) PvChip(f, cur.filter == f) {
                                    commit(clips.toMutableList().also { it[curIndex] = cur.copy(filter = f) })
                                }
                                PvChip("Aplicar em todas", false) { commit(clips.map { it.copy(filter = cur.filter) }) }
                            }
                            "ord" -> {
                                PvChip("‹ Mover", curIndex > 0) {
                                    val list = clips.toMutableList()
                                    val tmp = list[curIndex]; list[curIndex] = list[curIndex - 1]; list[curIndex - 1] = tmp
                                    commit(list)
                                    val target = list.take(curIndex - 1).sumOf { it.secs }
                                    scope.launch { scroll.scrollTo((target * pps + 1f).roundToInt()) }
                                }
                                PvChip("Mover ›", curIndex < clips.lastIndex) {
                                    val list = clips.toMutableList()
                                    val tmp = list[curIndex]; list[curIndex] = list[curIndex + 1]; list[curIndex + 1] = tmp
                                    commit(list)
                                    val target = list.take(curIndex + 1).sumOf { it.secs }
                                    scope.launch { scroll.scrollTo((target * pps + 1f).roundToInt()) }
                                }
                            }
                            else -> { }
                        }
                    }
                }
            }

            Row(Modifier.fillMaxWidth().background(Color(0xFF14171B)).padding(horizontal = 6.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                PvTool(Modifier.weight(1f), Icons.Default.AddPhotoAlternate, "Adicionar", false) { if (!busy) onAdd() }
                PvTool(Modifier.weight(1f), Icons.Default.Timer, "Duração", tool == "dur") { tool = if (tool == "dur") "" else "dur" }
                PvTool(Modifier.weight(1f), Icons.Default.AutoAwesome, "Transição", tool == "tr") { tool = if (tool == "tr") "" else "tr" }
                PvTool(Modifier.weight(1f), Icons.Default.Palette, "Filtros", tool == "fil") { tool = if (tool == "fil") "" else "fil" }
                PvTool(Modifier.weight(1f), Icons.Default.SwapHoriz, "Ordem", tool == "ord") { tool = if (tool == "ord") "" else "ord" }
                PvTool(Modifier.weight(1f), Icons.Default.Delete, "Excluir", false) {
                    if (clips.size > 1) {
                        val keepAt = startSecOf(curIndex)
                        val list = clips.toMutableList()
                        list.removeAt(curIndex)
                        commit(list)
                        val newTotal = list.sumOf { it.secs }
                        scope.launch { scroll.scrollTo((keepAt.coerceAtMost((newTotal - 1).coerceAtLeast(0)) * pps + 1f).roundToInt()) }
                    } else onNotice("O vídeo precisa de pelo menos uma foto.")
                }
            }
        }
        if (busy) {
            Box(Modifier.fillMaxSize().background(Color(0xB3000000)).clickable { }, contentAlignment = Alignment.Center) {
                Text("Criando o vídeo…", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun PvChip(label: String, active: Boolean, onClick: () -> Unit) {
    Box(
        Modifier.clip(RoundedCornerShape(16.dp)).background(if (active) PvBlue else Color(0xFF2B2F36)).clickable { onClick() }.padding(horizontal = 14.dp, vertical = 8.dp)
    ) { Text(label, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold) }
}

@Composable
private fun PvTool(modifier: Modifier, icon: ImageVector, label: String, active: Boolean, onClick: () -> Unit) {
    Column(
        modifier.padding(horizontal = 3.dp).clip(RoundedCornerShape(12.dp)).background(if (active) Color(0xFF39404A) else Color(0xFF23272D)).clickable { onClick() }.padding(vertical = 9.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Icon(icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(23.dp))
        Text(label, color = Color.White, fontSize = 10.sp, maxLines = 1)
    }
}

/** Efeito de entrada da foto no vídeo final (mesmo comportamento da prévia). */
private fun pvTransitionEffect(clip: PvClip): Effect? {
    val durUs = clip.secs * 1_000_000L
    return when (clip.transition) {
        "Zoom" -> MatrixTransformation { t ->
            val local = (t % durUs).coerceAtLeast(0L)
            val s = 1f + 0.15f * (local.toFloat() / durUs).coerceIn(0f, 1f)
            android.graphics.Matrix().also { it.postScale(s, s) }
        }
        "Deslizar" -> MatrixTransformation { t ->
            val p = ((t % durUs).toFloat() / 500_000f).coerceIn(0f, 1f)
            android.graphics.Matrix().also { it.postTranslate(2f * (1f - p), 0f) }
        }
        "Surgir" -> MatrixTransformation { t ->
            val p = ((t % durUs).toFloat() / 500_000f).coerceIn(0f, 1f)
            val s = 0.6f + 0.4f * p
            android.graphics.Matrix().also { it.postScale(s, s) }
        }
        else -> null
    }
}

/**
 * Gera o MP4 (720x1280, 30 fps, sem áudio): primeiro grava cada foto já com o filtro, depois junta tudo no Media3 Transformer.
 * O resultado volta na thread principal.
 */
internal fun qinblyBuildPhotoVideo(context: Context, clips: List<PvClip>, onResult: (Uri?, String) -> Unit) {
    val main = Handler(Looper.getMainLooper())
    Thread {
        try {
            val baked = clips.map { qinblyPvBake(context, it.uri, it.filter) }
            main.post { pvStartTransformer(context, clips, baked, onResult) }
        } catch (t: Throwable) {
            main.post { onResult(null, "Não foi possível preparar as fotos: " + (t.localizedMessage ?: "tente novamente")) }
        }
    }.start()
}

private fun pvStartTransformer(context: Context, clips: List<PvClip>, baked: List<Uri>, onResult: (Uri?, String) -> Unit) {
    try {
        val out = File(context.cacheDir, "qinbly-photovideo-${System.currentTimeMillis()}.mp4")
        val sequenceBuilder = EditedMediaItemSequence.Builder()
        clips.forEachIndexed { i, clip ->
            val mediaItem = MediaItem.Builder()
                .setUri(baked[i])
                .setMimeType(MimeTypes.IMAGE_JPEG)
                .setImageDurationMs(clip.secs * 1000L)
                .build()
            val videoEffects = mutableListOf<Effect>(Presentation.createForWidthAndHeight(720, 1280, Presentation.LAYOUT_SCALE_TO_FIT_WITH_CROP))
            pvTransitionEffect(clip)?.let { videoEffects.add(it) }
            val edited = EditedMediaItem.Builder(mediaItem)
                .setDurationUs(clip.secs * 1_000_000L)
                .setFrameRate(30)
                .setEffects(Effects(emptyList(), videoEffects))
                .build()
            sequenceBuilder.addItem(edited)
        }
        val composition = Composition.Builder(sequenceBuilder.build()).build()
        val transformer = Transformer.Builder(context)
            .setVideoMimeType(MimeTypes.VIDEO_H264)
            .addListener(object : Transformer.Listener {
                override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                    onResult(Uri.fromFile(out), "")
                }
                override fun onError(composition: Composition, exportResult: ExportResult, exportException: ExportException) {
                    onResult(null, "Não foi possível criar o vídeo: " + (exportException.message ?: "erro ao exportar"))
                }
            })
            .build()
        transformer.start(composition, out.absolutePath)
    } catch (e: Exception) {
        onResult(null, "Não foi possível criar o vídeo: " + (e.localizedMessage ?: "tente novamente"))
    }
}
