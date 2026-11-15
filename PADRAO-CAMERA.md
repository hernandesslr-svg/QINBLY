# Padrão da câmera (regra do dono do app)

- Existe **uma câmera só**: a do app (`PostPage` / `QinblyCameraCapture`). Em qualquer lugar onde aparecer a opção de câmera (Criar, Story, Mural, Cápsula, Chat, Comunidade…), ela abre **igual**, com a mesma tela e os mesmos botões. Nunca usar a câmera do celular (`TakePicture`).
- A câmera abre **limpa**: nada de faixa de filtros/efeitos aberta. Só aparece quando a pessoa toca em **Filtros**. Vale também para o editor logo depois da foto.
- A foto sai **igual ao que se vê na tela** (mesmo enquadramento); a selfie sai como aparece na tela.
- Mudou algo na câmera? Vale para todas. Não fazer versão diferente por tela.
- A tela nova do story (botão redondo "Story", horas, Quem vê, + Feed) é só do **editor do story comum**; câmera e editor são coisas separadas.

## Editor de foto (também padrão)
- Existe **um editor só**: o do `PostPage`. A câmera chamada por outras telas (`QinblyCameraCapture`, com `editBeforeReturn = true`) abre esse mesmo editor depois da foto e devolve a foto já editada para a tela que chamou (botão "Pronto").
- O editor abre limpo: a faixa de filtros só aparece quando a pessoa toca em Filtros.
- Só o editor do **story comum** do Criar tem a barra nova (botão "Story", horas, Quem vê, + Feed).

## Trocar filtro passando o dedo
- Na câmera e no editor de foto: um dedo arrastando para a esquerda/direita troca o filtro de cor (Original, Aurora, Pôr do sol…), sem abrir a faixa de filtros. Os filtros de rosto e os efeitos animados de tela (chuva, fogo, neve etc.) foram REMOVIDOS do app a pedido do dono: a faixa de filtros tem só filtros de cor (cerca de 50, lista `QinblyColorFilterNames` em MainActivity.kt; para criar um novo, adicionar o nome na lista e uma linha `qinblyLook(...)` em `qinblyFilterMatrix`).

## Miniaturas dos filtros
- As bolinhas dos filtros de cor mostram um rosto de exemplo (`res/drawable-nodpi/filter_sample.jpg`) já com o filtro aplicado, na câmera e no editor. Para trocar a foto de exemplo, basta trocar esse arquivo.
