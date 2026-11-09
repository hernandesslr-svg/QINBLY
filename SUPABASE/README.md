# Banco e API do QINBLY (Supabase)

Ordem dos scripts no **SQL Editor**: `00_base_schema.sql` (só em banco novo) → `03_community_safety.sql` → `04_saves_reposts.sql` → `05_social_features.sql` → `06_single_profile_photo.sql` → `07_communities.sql` → `08_comment_edit.sql` → `09_chat_extras.sql` → `10_stickers_live.sql` → `11_stories_notes.sql`.
Depois publique a função: `supabase functions deploy qinbly-api`.

- Bucket de storage público `post-media` (mídia de publicações, avatar e capa) precisa existir.
- Notificações de curtida/seguir/comentário em um banco novo dependem de gatilhos no banco (o app já lê a tabela `notifications`); menção e resposta são criadas pela própria função.
- Login com Google e e-mail/senha são configurados em Authentication → Providers; o link de "Esqueci minha senha" usa o template e a URL de redirecionamento de Authentication → URL Configuration.
