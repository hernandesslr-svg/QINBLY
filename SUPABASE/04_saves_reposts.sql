-- Salvar e repostar de verdade (persistidos no servidor).
-- Rode no SQL Editor do Supabase ANTES de publicar a nova versão da função qinbly-api.

CREATE TABLE IF NOT EXISTS public.post_saves (
  user_id    uuid NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
  post_id    uuid NOT NULL REFERENCES public.posts(id) ON DELETE CASCADE,
  created_at timestamptz NOT NULL DEFAULT now(),
  PRIMARY KEY (user_id, post_id)
);
CREATE TABLE IF NOT EXISTS public.post_reposts (
  user_id    uuid NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
  post_id    uuid NOT NULL REFERENCES public.posts(id) ON DELETE CASCADE,
  created_at timestamptz NOT NULL DEFAULT now(),
  PRIMARY KEY (user_id, post_id)
);
CREATE INDEX IF NOT EXISTS post_saves_user_idx    ON public.post_saves (user_id, created_at DESC);
CREATE INDEX IF NOT EXISTS post_reposts_post_idx  ON public.post_reposts (post_id);
CREATE INDEX IF NOT EXISTS post_reposts_user_idx  ON public.post_reposts (user_id, created_at DESC);

ALTER TABLE public.post_saves   ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.post_reposts ENABLE ROW LEVEL SECURITY;

-- Salvos são privados: só o dono vê e altera.
DROP POLICY IF EXISTS saves_select_own ON public.post_saves;
CREATE POLICY saves_select_own ON public.post_saves FOR SELECT TO authenticated USING (user_id = auth.uid());
DROP POLICY IF EXISTS saves_insert_own ON public.post_saves;
CREATE POLICY saves_insert_own ON public.post_saves FOR INSERT TO authenticated WITH CHECK (user_id = auth.uid());
DROP POLICY IF EXISTS saves_delete_own ON public.post_saves;
CREATE POLICY saves_delete_own ON public.post_saves FOR DELETE TO authenticated USING (user_id = auth.uid());

-- Reposts são públicos (para contar), mas só o dono cria/remove os seus.
DROP POLICY IF EXISTS reposts_select_all ON public.post_reposts;
CREATE POLICY reposts_select_all ON public.post_reposts FOR SELECT TO anon, authenticated USING (true);
DROP POLICY IF EXISTS reposts_insert_own ON public.post_reposts;
CREATE POLICY reposts_insert_own ON public.post_reposts FOR INSERT TO authenticated WITH CHECK (user_id = auth.uid());
DROP POLICY IF EXISTS reposts_delete_own ON public.post_reposts;
CREATE POLICY reposts_delete_own ON public.post_reposts FOR DELETE TO authenticated USING (user_id = auth.uid());
