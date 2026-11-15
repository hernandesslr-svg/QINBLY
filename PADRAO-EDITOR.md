# Padrão: tudo que entra na foto é móvel (editor e câmera)

Regra do dono do app: texto, emote, adesivo ou qualquer coisa que a pessoa coloque na foto (post de feed, story, etc.) tem que poder ser **movido por ela para onde quiser**.

## Como está
- **Texto:** arrastar com um dedo (já era assim).
- **Emotes/adesivos** (`QinblyEditorSticker` em MainActivity.kt): tocar na lista adiciona um novo (pode ter vários; lista `qinblyEditorEmojis`, ~90 emotes); tocar no emote já colocado o SELECIONA (moldura branca + X no canto que remove só ele); arrastar move; dois dedos ampliam e giram (só aquele emote, de 0,3x a 8x); toque duas vezes também apaga. Posição, tamanho e giro vão para a imagem final (`createEditorOverlayImage`).
- **Desenho:** livre na tela, com barra acima do 6h/12h/24h em 2 linhas: cores (bolinhas de 34dp, fileira rola de lado) e, embaixo, 4 espessuras mostradas como pedacinho de traço na cor escolhida (branco com a borracha ligada), botões redondos grandes Desfazer / Borracha / Limpar (ícone + nome pequeno) e o visto (círculo branco) no lugar de "Pronto". A Borracha apaga SÓ o pedaço por onde o dedo passa (`qinblyEraseAt` divide o traço em pedaços e mantém o resto; também apaga com um toque). Cada traço guarda sua cor e espessura (`editorStrokeMeta`, lista paralela a `editorStrokes`); reabrir o Desenho NÃO apaga o que já foi feito. Com o Desenho aberto a camada de gestos da foto sai da frente (senão ela engole o toque e o lápis não risca). O botão Fundo só fica azul enquanto a barra do Fundo está aberta.

## Para qualquer coisa nova que entre na foto
Guardar posição (fração 0..1), escala e giro; mover com `detectTransformGestures`; desenhar na imagem final com a mesma posição.

## Limite conhecido
A posição é guardada como fração da tela do editor, igual ao texto e ao desenho. Quando o corte escolhido tem proporção diferente da tela, o resultado pode sair levemente deslocado.

## Foto cortada na tela: o corte acompanha o rosto
Toda foto mostrada cortada (feed e, quando for o caso, miniaturas) usa `rememberQinblyFaceAlignment(url, proporcaoDaFoto, proporcaoDoQuadro)` (FaceFocus.kt) no parâmetro `alignment` do `AsyncImage` com `ContentScale.Crop`. O rosto maior é achado uma vez por foto (cache em memória) e o corte sobe até ele entrar no quadro; sem rosto, o corte fica um pouco acima do meio. Tocar na foto abre inteira. Feito no feed; miniaturas do perfil ainda não.

## Seleção: a pinça vale só no item selecionado
Emote ou texto selecionado (moldura branca; emote tem X para remover) = a pinça amplia só ele (emote: 0,3x a 8x, também gira; texto: 0,4x a 4x via `editorTextScale`). A foto só dá zoom quando nada está selecionado. Tocar na foto (fora do item) tira a seleção. Toque no texto: 1º toque seleciona, toque no texto já selecionado abre a edição. A pinça no modo Formato é invertida de propósito: abrir os dedos AUMENTA o quadro.

## Retoque, HDR e Fundo (liga/desliga, nada acumula)
Os três partem sempre da foto ORIGINAL (`fxBase`) e recalculam tudo de uma vez com `QinblyFxSpec` (PhotoEffects.kt): tocar liga e acende o botão, tocar de novo desliga e volta ao original. Nunca aplicar efeito sobre foto já com efeito (HDR repetido estourava a foto).
- **Retoque:** alisa só a PELE dentro do rosto (faixa de cor de pele), preservando bordas (olhos, boca, sobrancelhas, cabelo).
- **HDR:** realce com intensidade Fraco/Médio/Forte.
- **Fundo:** a pessoa fica nítida e só o fundo muda: Nenhum, Desfocar, Bokeh, Movimento, P&B, Escurecer, Clarear, Cores, Degradês, Cenários (desenhados em código: Pôr do sol, Noite estrelada, Cidade à noite, Neon), Galeria (foto escolhida), e Recortar pessoa (salva como figurinha). Não há fundos fotográficos prontos: precisariam de imagens livres de direito (anotar a licença como em MUSICAS-LIVRES.md).
