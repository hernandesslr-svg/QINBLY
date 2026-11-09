# Comunidade e moderação — o que foi adicionado

Funções que toda rede social precisa (e que as lojas de apps exigem): **bloquear, denunciar, moderar, apagar comentário e excluir conta.**

## Qual backend o app usa?
O app (`API_BASE_URL` no `app/build.gradle.kts`) fala com a **Edge Function do Supabase** (`SUPABASE/functions/qinbly-api`).
O Express em `BACKEND/` é o backend antigo (Railway) e **não é compatível** com o app atual (não tem `/api/auth/refresh`).
As funções novas foram implementadas nos dois, com as mesmas rotas e formatos de resposta.

## Para ativar (nesta ordem)
1. **Banco:** rode `SUPABASE/03_community_safety.sql` no SQL Editor do Supabase. É aditivo e pode ser rodado de novo sem problema.
2. **Edge Function:** publique `SUPABASE/functions/qinbly-api/index.ts` (`supabase functions deploy qinbly-api`).
   `SUPABASE_SERVICE_ROLE_KEY` já é injetada pelo Supabase; ela é usada só no servidor, para excluir contas.
3. **App:** gere um novo APK (o workflow do GitHub Actions já compila e assina).
4. *(Só se usar o Express)* rode `npm run migrate` (migração `010`) e defina `ADMIN_API_TOKEN` com 32+ caracteres.

## Rotas novas
| Rota | O que faz |
|---|---|
| `POST /api/users/:id/block` | Alterna bloqueio. Bloquear desfaz o seguimento nos dois sentidos. |
| `GET /api/users/me/blocks` | Lista quem você bloqueou. |
| `POST /api/reports` | Denuncia `post`, `comment`, `user` ou `message` com motivo. |
| `DELETE /api/comments/:id` | O autor ou o dono do post apaga o comentário. |
| `POST /api/users/me/delete` (ou `DELETE /api/users/me`) | Exclui a conta e todos os arquivos. Exige `confirmUsername`. |

## Como o bloqueio funciona no Supabase
Por **policies RLS RESTRICTIVE**: elas se somam às policies atuais e filtram automaticamente feed, comentários, curtidas, seguidores, mensagens e perfis nos dois sentidos. Não é preciso alterar cada rota. Contas suspensas ficam invisíveis e não conseguem publicar, comentar, curtir, seguir nem mandar mensagem.

## Moderação
Não há painel. As denúncias ficam em `content_reports`, e o SQL para fila, remoção e suspensão está comentado no fim de `03_community_safety.sql`. No Express existem `GET /api/admin/reports` e `POST /api/admin/reports/:id/resolve` (cabeçalho `x-admin-token`).

## Corrigido no caminho
- **Seguir pessoas estava quebrado na Edge Function** (a rota usava `user` antes de ele existir e sempre dava erro 500). Agora funciona.
- Express: limite de requisições em toda a API (antes só no login), JWT com algoritmo fixo (HS256) e suspensão de contas.

## No app
- Menu da publicação: **Denunciar** e **Bloquear @usuário**.
- Chat: Bloquear e Denunciar agora chamam o servidor (antes só mostravam um aviso).
- Visualizador de foto/publicação: Denunciar funcional.
- Configurações: **Contas bloqueadas** (com desbloquear) e **Excluir conta**.

## O que ainda NÃO foi feito
- Painel web de moderação, filtro automático de conteúdo, banimento de dispositivo.
- Push notifications, lives, "Comunidades" (a tela existe, mas é só aviso).
- Quebrar o `MainActivity.kt` (8 mil linhas), testes automatizados, escolher um único backend.
