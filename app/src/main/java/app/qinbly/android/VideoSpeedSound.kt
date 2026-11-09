package app.qinbly.android

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.media3.common.Effect
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.audio.SonicAudioProcessor
import androidx.media3.effect.SpeedChangeEffect
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.EditedMediaItemSequence
import androidx.media3.transformer.Effects
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.Transformer
import java.io.File

/**
 * Regrava o vídeo da câmera com a velocidade escolhida ([speed]: 0,5 / 2 / 3...) e, se houver, mistura o som escolhido
 * (cortado no tamanho do vídeo; o áudio original da gravação continua junto). Precisa ser chamada na thread principal.
 * O resultado volta em [onResult] na thread principal: o arquivo novo, ou null com uma mensagem de erro.
 */
internal fun qinblyReencodeVideo(context: Context, video: Uri, speed: Float, sound: Uri?, onResult: (Uri?, String) -> Unit) {
    try {
        val out = File(context.cacheDir, "qinbly-video-fx-${System.currentTimeMillis()}.mp4")
        var durationMs = 0L
        try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, video)
            durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
            retriever.release()
        } catch (_: Exception) { }

        val audioProcessors = ArrayList<AudioProcessor>()
        val videoEffects = ArrayList<Effect>()
        if (speed != 1f) {
            audioProcessors.add(SonicAudioProcessor().apply { setSpeed(speed); setPitch(1f) })
            videoEffects.add(SpeedChangeEffect(speed))
        }
        val videoItem = EditedMediaItem.Builder(MediaItem.fromUri(video))
            .setEffects(Effects(audioProcessors, videoEffects))
            .build()
        val videoSequence = EditedMediaItemSequence.Builder().addItem(videoItem).build()
        val composition = if (sound == null) {
            Composition.Builder(videoSequence).build()
        } else {
            val clip = MediaItem.ClippingConfiguration.Builder()
            // O som acompanha o vídeo já na velocidade nova: corta no tamanho final.
            if (durationMs > 0) clip.setEndPositionMs((durationMs / speed).toLong().coerceAtLeast(500L))
            val soundItem = EditedMediaItem.Builder(MediaItem.Builder().setUri(sound).setClippingConfiguration(clip.build()).build()).build()
            val soundSequence = EditedMediaItemSequence.Builder().addItem(soundItem).build()
            Composition.Builder(videoSequence, soundSequence).build()
        }
        val transformer = Transformer.Builder(context)
            .setVideoMimeType(MimeTypes.VIDEO_H264)
            .addListener(object : Transformer.Listener {
                override fun onCompleted(composition: Composition, exportResult: ExportResult) { onResult(Uri.fromFile(out), "") }
                override fun onError(composition: Composition, exportResult: ExportResult, exportException: ExportException) {
                    onResult(null, "Não foi possível aplicar velocidade e som: " + (exportException.message ?: "erro ao exportar"))
                }
            })
            .build()
        transformer.start(composition, out.absolutePath)
    } catch (e: Exception) {
        onResult(null, "Não foi possível aplicar velocidade e som: " + (e.localizedMessage ?: "tente novamente"))
    }
}
