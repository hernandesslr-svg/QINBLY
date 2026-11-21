# Lives com LiveKit

## Como funciona
- O app (Live.kt) pede ao servidor (functions/api/live.js, Cloudflare Pages) uma permissão de entrada e conecta no LiveKit.
- Quem transmite usa a sala `live_<id do usuário>`. Quem assiste entra nessa sala só para ver e ouvir.
- A lista "Lives em alta" vem do próprio LiveKit (salas abertas), sem depender do banco.
- A chave secreta do LiveKit fica só no Cloudflare. Nunca dentro do app.

## Configuração (uma vez)
1. Conta grátis em livekit.io → criar projeto → copiar: endereço (`wss://...livekit.cloud`), chave pública (API Key) e chave secreta (API Secret).
2. Cloudflare → Workers e Pages → projeto `qinbly` → Settings → Variables and Secrets, criar:
   - `LIVEKIT_URL` = endereço do projeto
   - `LIVEKIT_API_KEY` = chave pública
   - `LIVEKIT_API_SECRET` = chave secreta (tipo Secret)
3. Fazer um novo deploy do site (a pasta `functions/` na raiz do repositório vira a API).
4. Teste no navegador: abrir `https://qinbly.pages.dev/api/live?action=list`. Resposta `{"error":"Entre na sua conta de novo."}` = função no ar. `Servidor de lives ainda não configurado` = faltam as variáveis.

## Limites do plano grátis do LiveKit
Franquia mensal fixa. Ao acabar, as lives param até o mês seguinte.

## Ainda não faz
- Efeitos de rosto e cor durante a live (só na prévia).
