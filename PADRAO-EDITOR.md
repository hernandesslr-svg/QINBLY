# Padrão: tudo que entra na foto é móvel (editor e câmera)

Regra do dono do app: texto, emote, adesivo ou qualquer coisa que a pessoa coloque na foto (post de feed, story, etc.) tem que poder ser **movido por ela para onde quiser**.

## Como está
- **Texto:** arrastar com um dedo (já era assim).
- **Emotes/adesivos** (`QinblyEditorSticker` em MainActivity.kt): tocar no emote adiciona um novo (pode ter vários); arrastar move; dois dedos ampliam e giram; toque duas vezes apaga. Posição, tamanho e giro vão para a imagem final (`createEditorOverlayImage`).
- **Desenho:** livre na tela.

## Para qualquer coisa nova que entre na foto
Guardar posição (fração 0..1), escala e giro; mover com `detectTransformGestures`; desenhar na imagem final com a mesma posição.

## Limite conhecido
A posição é guardada como fração da tela do editor, igual ao texto e ao desenho. Quando o corte escolhido tem proporção diferente da tela, o resultado pode sair levemente deslocado.

## Foto cortada na tela: o corte acompanha o rosto
Toda foto mostrada cortada (feed e, quando for o caso, miniaturas) usa `rememberQinblyFaceAlignment(url, proporcaoDaFoto, proporcaoDoQuadro)` (FaceFocus.kt) no parâmetro `alignment` do `AsyncImage` com `ContentScale.Crop`. O rosto maior é achado uma vez por foto (cache em memória) e o corte sobe até ele entrar no quadro; sem rosto, o corte fica um pouco acima do meio. Tocar na foto abre inteira. Feito no feed; miniaturas do perfil ainda não.
