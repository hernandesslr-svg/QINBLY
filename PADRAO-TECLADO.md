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
