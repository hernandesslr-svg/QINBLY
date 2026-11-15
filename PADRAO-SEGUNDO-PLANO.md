# Padrão: segundo plano

Quando o app sai da tela (a pessoa fechou, foi para outro app, bloqueou a tela ou deixou em segundo plano), **tudo que toca pausa**: vídeo do feed, vídeo em tela cheia, vídeo de story, música de post/story, mensagem de voz, áudio de comentário de story. Ao voltar, retoma só o que estava tocando (vídeo e música); mensagem de voz não retoma sozinha.

Como funciona: `Playback.kt` (`QinblyPlayback`) guarda todos os players; `MainActivity.onStop` pausa tudo e `onStart` retoma.

**Todo player novo do app TEM que se registrar** com `QinblyPlayback.add(pause, resume)` dentro de um `DisposableEffect` e desregistrar no `onDispose`.
