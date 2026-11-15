package app.qinbly.android

import java.util.concurrent.CopyOnWriteArrayList

/**
 * PADRÃO DO APLICATIVO: quando o app sai da tela (a pessoa fechou, foi para outro app ou deixou em segundo plano),
 * TODO vídeo, música e áudio do app pausa. Ao voltar, só retoma o que estava tocando.
 * Todo player novo do app TEM que se registrar aqui (veja PADRAO-SEGUNDO-PLANO.md).
 */
internal object QinblyPlayback {
    private class Entry(val pause: () -> Boolean, val resume: () -> Unit) { var wasPlaying = false }
    private val entries = CopyOnWriteArrayList<Entry>()

    /** pause() pausa o player e devolve true se ele estava tocando; resume() retoma. Devolve a função para desregistrar. */
    fun add(pause: () -> Boolean, resume: () -> Unit = {}): () -> Unit {
        val e = Entry(pause, resume)
        entries.add(e)
        return { entries.remove(e) }
    }

    fun pauseAll() { entries.forEach { e -> e.wasPlaying = try { e.pause() } catch (_: Throwable) { false } } }

    fun resumeAll() {
        entries.forEach { e -> if (e.wasPlaying) { e.wasPlaying = false; try { e.resume() } catch (_: Throwable) { } } }
    }
}
