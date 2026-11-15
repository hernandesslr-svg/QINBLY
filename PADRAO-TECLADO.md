# Padrão: barra de escrever e teclado

Regra do dono do app: **toda** barra de escrever que sobe com o teclado (mensagem do story, chat, comunidade, cápsula,
story coletivo, story de texto, legendas, comentários) fica **colada no teclado com o mesmo espacinho**, em todo o app.

- Espaço padrão: `QinblyKeyboardGap` (6dp), em `Extras.kt`. Para mudar o espaço do app inteiro, mude só esse valor.
- Folga de baixo da barra: `qinblyInputBottomPadding(closedExtra)` (Extras.kt). Teclado aberto = `QinblyKeyboardGap`;
  fechado = acima dos botões do celular (`qinblyNavBarPadding()` + extra).
- Chamar **dentro** da janela da barra. Quem recebe o teclado é o Dialog; a tela de trás não vê o teclado e acha que está
  fechado (era o vão enorme do story). Nunca ler `WindowInsets.ime` fora do Dialog e repassar para dentro.
- Em janelas que já usam `.imePadding()`, não somar o `qinblyNavBarPadding()` fixo com o teclado aberto.
- Tela nova com barra de escrever: usar estas duas peças, sem número solto.

## Campo aceso só com o teclado aberto

Regra do dono do app: o campo de texto só fica "aceso" (borda azul e cursor piscando) com o teclado aberto. Fechou o
teclado (voltar, descer, tocar fora), a seleção sai do campo e a borda e o cursor somem.

- Peça única: `QinblyClearFocusWhenKeyboardCloses()` (Extras.kt). Observa o teclado e limpa o foco quando ele fecha.
- Já está em: tela principal (setContent), `QinblyDialog` (vale para `QinblyFullScreen`) e nos `AlertDialog` que têm campo
  de texto. Cada janela tem o próprio teclado e o próprio foco, então precisa estar dentro de cada uma.
- `AlertDialog` ou janela nova com campo de texto: chamar `QinblyClearFocusWhenKeyboardCloses()` dentro do conteúdo.

## Conversas: última mensagem sempre visível
Toda lista de mensagens (chat direto, chat de comunidade e qualquer conversa nova) chama `QinblyKeepChatAtBottom(listState, ultimoIndice)`. Quando o teclado abre ou fecha, a lista volta para o fim, para a última mensagem nunca ficar escondida embaixo. Se a pessoa está lendo mensagens muito antigas, a lista não é puxada.

## Cursor dos campos de texto
Campo que filtra o que a pessoa digita (nome de usuário, etc.) não pode mexer no cursor: só a pessoa move. Usar `TextFieldValue` + `qinblySanitized(novo) { filtro }` (Extras.kt), que mantém o cursor onde estava mesmo quando um caractere proibido é descartado. Nada de `onValueChange = { texto = it.filter... }` com texto simples, porque isso joga o cursor para o fim.

## Texto no editor (palavras, fontes e cores)
A barra do texto (seleção de palavras, fontes e cores) sobe INTEIRA colada no topo do teclado. `EditorTextOverlay` NÃO usa mais Dialog (a janela separada não recebia o tamanho do teclado e escondia as cores): é uma camada dentro do editor (`zIndex`) com `.imePadding()`, igual ao texto da câmera. Com o teclado fechado a barra fica ACIMA do 6h/12h/24h (175dp da base da janela). A cor escolhida aparece na hora no letreiro (a transformação visual tem chave nas cores).
