# Story de texto (rascunho, ainda não implementado)

Substitui as "notas". Um story feito só de texto sobre fundo colorido, com comentários.

## Telas (aprovadas no esboço de 12/11/2026)
1. Criar (Criar -> Texto): fundo colorido, texto centralizado, bolinhas de cor embaixo, botão Publicar.
2. Faixa de cima (Descobrir): cartão 168x306 dp com a cor de fundo, começo do texto e nome.
3. Ver e comentar: tela cheia, barra de progresso, comentários embaixo, campo "Comentar no story...".

## Como se escreve (esboço de 12/11/2026)
- Sem limite de texto. A letra diminui sozinha; na tela cheia o texto longo rola.
- Cor de fundo (bolinhas), fonte (algumas), alinhamento (esquerda, centro, direita) e emoji.
- Botão Publicar no topo, X para sair.

## A decidir
- Quem vê os comentários (suposição por enquanto: só o autor vê todos; quem comentou vê o próprio).

## Servidor (precisa de mudança)
- Story com tipo "texto": campos de texto e cor.
- Tabela de comentários do story (RLS: autor do story e quem comentou).
- Remover notas (menu Criar, cartões, nota completa).
