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
