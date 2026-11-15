# Padrão: seletor de foto (galeria) com câmera

Onde a pessoa escolhe foto da galeria, o app mostra o PRÓPRIO seletor (`PhotoPicker.kt`):
- **A câmera é sempre a primeira célula da grade.**
- A câmera que abre é SEMPRE a câmera padrão do app (`QinblyCameraCapture`, com o mesmo editor). Em telas que já têm fluxo próprio de câmera (chat, comunidade, Criar), o seletor chama esse fluxo.
- "Ver todas as fotos" no rodapé abre a galeria completa do celular (com a busca do Google Fotos).
- Uma foto: toca e volta. Várias: toca para marcar (numeradas) e "Pronto".
- Pede permissão de fotos; sem permissão, a câmera e "Ver todas as fotos" continuam funcionando.

Uso no código: troque `rememberLauncherForActivityResult(PickVisualMedia())` por `rememberQinblyPhotoLauncher { uri -> ... }` (uma) ou `rememberQinblyMultiPhotoLauncher(max) { uris -> ... }` (várias) e continue chamando `.launch(...)`.

**Já usam:** anúncio/Marketplace, figurinha própria, chat, comunidade (foto e criar comunidade), foto de perfil, adicionar fotos no vídeo de fotos, Criar → Galeria.
**Ficaram de fora de propósito:** galerias que já têm a câmera no topo (cápsula, story coletivo), seletores só de vídeo ou de foto+vídeo, e o seletor que abre de dentro da própria câmera.
