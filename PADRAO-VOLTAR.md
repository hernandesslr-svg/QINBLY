# Padrão: botão de voltar (app inteiro)

Regra do dono do app: **voltar sempre volta para a tela anterior, do jeito que a pessoa deixou. Nunca fecha tudo nem joga no início.**
Vale para o botão de voltar do celular e para qualquer seta/botão de voltar do app.

Exemplo: Buscar → digitou "ana" → abriu o perfil → voltar → cai na Busca, com "ana" e os resultados.

## Como funciona (MainActivity.kt)
- `handleBack` em `QinblyApp` é o único caminho de voltar. `BackHandler` e todas as setas das páginas (`onBack = handleBack`) usam ele.
- `navHistory` guarda as telas anteriores; `handleBack` volta uma por vez.
- `QinblyKeep` / `rememberKept(...)` / `rememberKeptListState(...)`: guardam estado fora da tela (texto da busca, resultados, filtro, aba do feed, rolagem do feed e das conversas, comunidade aberta), para sobreviver quando a tela sai da composição.
- `QinblyResume.push { ... }`: uma janela por cima (busca, lista de seguidores, hashtag, Clipes, vídeo, lista de seguidores no perfil) que fecha para abrir um perfil chama `push` ANTES de fechar, dizendo como se reabre. Ao voltar para aquele ponto, a janela reabre como estava.
- Trocar de aba na barra de baixo ou de conta limpa os "reabrir" pendentes.

## Ao criar tela/janela nova
1. Seta de voltar → `handleBack` (não setar estado direto).
2. Se a janela fecha para abrir outra tela, chamar `QinblyResume.push` antes de fechar.
3. Estado que a pessoa espera encontrar ao voltar (texto, filtro, rolagem) → `rememberKept`, com chave única (`"busca.xxx"`).

## Criar/publicar (etapas)
Câmera → Editor → Legenda/Publicar: o botão de voltar do celular volta UMA etapa (Legenda → Editor → Câmera). Nunca fecha tudo de uma vez.

## Regra geral (celular E seta do app): voltar = UMA etapa para trás
- Menu de criar (Stories.kt): mood, cápsula, pacotes, texto, novo story, nota e story coletivo voltam para o MENU.
- Cápsula e story coletivo: já voltam etapa por etapa (pick → edit → date → who).
- Live: contagem volta para a configuração.
- Criar conta volta para o login.
- Editor de foto: ferramenta aberta (Formato, Stickers, Desenho, Texto) fecha primeiro; depois sai do editor; legenda/publicar volta para o editor.
- Configurações: já volta seção por seção.
Todo fluxo novo com etapas TEM que seguir isso (BackHandler + seta do app fazendo a mesma coisa).

## Sair do aplicativo (toque duplo)
- Só vale na TELA INICIAL (feed). Em qualquer outra tela/aba, voltar = uma etapa para trás (outras abas voltam primeiro para o feed).
- 1º voltar na tela inicial: aviso "Toque de novo para sair" (2 segundos). 2º voltar dentro desses 2 segundos: fecha o app. Passou 2s: zera.
- Está no fim do `handleBack` (MainActivity.kt). Nunca fechar o app direto em outro lugar.
- Ao sair, vídeo/música/áudio já pausam (PADRAO-SEGUNDO-PLANO).

## Histórico do voltar (regra)
- Trocar de aba pela barra de baixo ZERA o histórico: voltar nunca passeia por abas, câmera, perfil e configurações visitadas antes.
- O histórico só guarda aberturas por cima (notificações, perfil, configurações, conversa). Voltar desfaz uma abertura por vez.
- Sem histórico: aba diferente do início volta para o início; no início, toque duplo para sair.
