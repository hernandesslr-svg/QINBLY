# Padrão: formato da foto (vale para o aplicativo inteiro)

Em qualquer lugar onde a pessoa escolhe o formato de uma foto (editor de post/story, e onde mais surgir):

1. **Quadro com guias**: borda, linhas de terços e cantos grossos. O que fica fora do quadro aparece escurecido (não vai sair).
2. **Mover a foto dentro do quadro**: arrastar e dar zoom com dois dedos. A pessoa escolhe a posição que quer publicar.
3. **Cortada ou inteira, quem decide é a pessoa**:
   - **Preencher**: foto cobre o quadro (corta), já começando com o rosto em foco (rosto perto de 1/3 do quadro).
   - **Inteira**: foto toda dentro do quadro, sobras pretas.
   - **Desfocar**: foto toda dentro do quadro, sobras com a própria foto desfocada.
4. **Formatos**: Original, 1:1, 4:5, 3:4, 4:3, 9:16, 16:9 (lista única `QINBLY_FORMATS`).
5. **O que se vê é o que sai (WYSIWYG)**: texto, emotes e desenho ficam presos ao quadro da foto, não à tela (`editorFrame`). Ao trocar de formato eles acompanham a foto.
6. **Feed**: o quadro segue a proporção da foto entre 4:5 e 1.8:1; só fora disso o corte segue o rosto; ao tocar abre inteira.
7. **Miniatura do perfil**: corte segue o rosto (`QinblyFaceThumb`).

Código: `FormatFrame.kt` (`QinblyFormatFrame`, `QinblyFormatBar`, `qinblyFrameRect`, `qinblyFitFactor`, `qinblyFacePanY`) e `qinblyRenderEditedPhoto` (gera a foto final, com fundo desfocado quando a foto fica menor que o quadro).

Pendente: a câmera ainda corta a foto no instante do clique pelo formato escolhido (ViewPort do CameraX); o editor depois só enxerga a parte já capturada.

## Tudo que fica por cima da foto é móvel (padrão)
Texto, emotes/figurinhas e desenho: arrastar para mover (emote: dois dedos para ampliar/girar, toque duplo apaga). Vale no editor, na câmera (modo TEXTO) e no story de texto (a posição vai dentro da legenda: `qinbly:text:bg:fonte:alinh:dx:dy`, em milésimos da tela).

## Zoom
Dois dedos direto na foto, ou a barra "Zoom" na ferramenta Formato (de menor que 1 = foto inteira, até 4x).

## Foto da galeria
Em qualquer lugar (Criar, cápsula, chat, comunidade, story coletivo) a foto escolhida da galeria passa pelo MESMO editor da câmera antes de seguir. Vídeo da cápsula ainda segue direto.
