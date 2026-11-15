# Músicas livres do Qinbly

Catálogo embutido no app (`app/src/main/assets/music/*.mp3`), usado no botão "Adicionar som" do editor (posts e stories), em stories e na cápsula.

- **Licença:** todas as faixas vêm do FreePD.com, que publica tudo em CC0 1.0 (domínio público: uso comercial liberado, sem crédito obrigatório).
- **Como foram obtidas:** cópias do FreePD no repositório SoundSafari/CC0-1.0-Music (pasta `freepd.com`). Cada faixa foi cortada em 30 s, com entrada/saída suave, e recodificada em MP3 96 kbps (~360 KB cada, ~11 MB no total).
- **Aviso:** a licença CC0 vale pelo que o FreePD declara. Antes de lançar de verdade, vale um advogado conferir (e rever o FreePD se alguma faixa for contestada). Se uma faixa for contestada, é só apagar o arquivo e a linha dela em `Music.kt` (`qinblyFreeTracks`).
- **Como aparece no post:** a música vai escondida no fim da legenda (`\n♪§título§artista§qinbly://music/<arquivo>`); `QinblyApi.parsePost` separa isso em `musicTitle/musicArtist/musicUrl`, então a legenda aparece limpa e o feed mostra a faixa "♪ título · Música livre" com play/pausa.

## Faixas
Animadas: Funshine, Funky Energy Loop, Groovin, Happy Whistling Ukulele, Hippety Hop, Be Jammin, Bollywood Groove, City Sunshine, Celebration, Energizing.
Festa e viagem: Downtown Boogie, Pina Colada, Sunny Rasta, Take the Ride, Vintage Party.
Calmas: Martini Sunset, Kalimba Relaxation Music, Study and Relax, Nostalgic Piano, Lovely Piano Song, Sunday Dub, Meditating Beat, Relaxing Ballad, Romantic Inspiration.
Inspiradoras: Elevate Inspirate, Horizon Flare, Shining Stars, Hopeful, Inspiration, Journey of Hope.

## Para adicionar faixa
Cortar em 30 s (mp3, nome minúsculo com `_`), colocar em `assets/music/` e acrescentar uma linha `free("arquivo_sem_mp3", "Título")` em `qinblyFreeTracks` (Music.kt).

## Padrão (regra do dono do app)
Em **qualquer lugar** do app onde aparecer a opção de adicionar música/som, ela tem que abrir a mesma folha (`QinblyMusicPicker`) com as músicas livres do Qinbly. Hoje: câmera (pílula do topo → "Sons do Qinbly"), editor de foto/vídeo (pílula do topo), Cápsula e Story coletivo. Tela nova com música = chamar `QinblyMusicPicker`, não criar outra folha. Em posts comuns usar `freeOnly = true`.
