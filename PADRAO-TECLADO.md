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
