# Padrão: formato da foto (vale para o aplicativo inteiro)

Em qualquer lugar onde a pessoa escolhe o formato de uma foto (editor de post/story, e onde mais surgir):

1. **Quadro com guias**: borda, linhas de terços e cantos grossos. O que fica fora do quadro aparece escurecido (não vai sair).
2. **Ferramenta Formato: a foto fica INTEIRA e parada; o que muda é o QUADRADO em cima dela.** Ao trocar o formato, o quadro muda de forma; a pessoa arrasta o quadro para escolher o ângulo e dá zoom com dois dedos (abrir os dedos AUMENTA o quadro, fechar diminui; no código, `zoom / gz` só no modo picker). O que fica fora do quadro aparece escurecido. Ao salvar/publicar, a foto sai exatamente no quadro escolhido. (`QinblyFormatPicker`)
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
**O zoom é SÓ com os dedos direto na tela, em cima da foto.** Nunca colocar barra/slider de zoom (o dono do app proibiu). Vale de "foto inteira" (menor que 1) até 6x.

## Foto da galeria
Em qualquer lugar (Criar, cápsula, chat, comunidade, story coletivo) a foto escolhida da galeria passa pelo MESMO editor da câmera antes de seguir. Vídeo da cápsula ainda segue direto.

## Regra de ouro dos gestos no editor
A camada transparente de tela cheia por cima da foto (a que mede a tela e troca filtro passando o dedo) CONTINUA existindo, e o gesto de mover/dar zoom na foto vive NELA (`qinblyFormatGestures`), por cima da imagem. Motivo: uma camada com gesto bloqueia o que está embaixo, então o gesto da foto não pode ficar na imagem de baixo. Texto, emotes e desenho ficam ainda mais acima, com seus próprios gestos.
