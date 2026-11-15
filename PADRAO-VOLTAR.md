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
