-- QINBLY — recursos sociais (rode no SQL Editor do Supabase depois dos scripts 03 e 04).
-- Idempotente: pode rodar mais de uma vez.

-- 1) Respostas e curtidas em comentários
ALTER TABLE public.comments ADD COLUMN IF NOT EXISTS parent_id uuid REFERENCES public.comments(id) ON DELETE CASCADE;
CREATE INDEX IF NOT EXISTS comments_parent_idx ON public.comments (parent_id);

CREATE TABLE IF NOT EXISTS public.comment_likes (
  comment_id uuid NOT NULL REFERENCES public.comments(id) ON DELETE CASCADE,
  user_id uuid NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE DEFAULT auth.uid(),
  created_at timestamptz NOT NULL DEFAULT now(),
  PRIMARY KEY (comment_id, user_id)
);
ALTER TABLE public.comment_likes ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS comment_likes_select ON public.comment_likes;
CREATE POLICY comment_likes_select ON public.comment_likes FOR SELECT USING (true);
DROP POLICY IF EXISTS comment_likes_insert ON public.comment_likes;
CREATE POLICY comment_likes_insert ON public.comment_likes FOR INSERT TO authenticated WITH CHECK (user_id = auth.uid());
DROP POLICY IF EXISTS comment_likes_delete ON public.comment_likes;
CREATE POLICY comment_likes_delete ON public.comment_likes FOR DELETE TO authenticated USING (user_id = auth.uid());

-- 2) Reações nas publicações (💚 🔥 😂 🤯 😢 👏). 'like' é a curtida simples.
ALTER TABLE public.post_likes ADD COLUMN IF NOT EXISTS reaction text NOT NULL DEFAULT 'like';
DROP POLICY IF EXISTS post_likes_update_own ON public.post_likes;
CREATE POLICY post_likes_update_own ON public.post_likes FOR UPDATE TO authenticated USING (user_id = auth.uid()) WITH CHECK (user_id = auth.uid());

-- 3) Fixar (até 3) e arquivar publicações. Arquivar usa visibility = 'archived'.
ALTER TABLE public.posts ADD COLUMN IF NOT EXISTS pinned_at timestamptz;
CREATE INDEX IF NOT EXISTS posts_pinned_idx ON public.posts (user_id, pinned_at) WHERE pinned_at IS NOT NULL;
DO $$
DECLARE c record;
BEGIN
  FOR c IN SELECT conname FROM pg_constraint WHERE conrelid = 'public.posts'::regclass AND contype = 'c' AND pg_get_constraintdef(oid) ILIKE '%visibility%' LOOP
    EXECUTE format('ALTER TABLE public.posts DROP CONSTRAINT %I', c.conname);
  END LOOP;
END $$;

-- 4) Notificações de menção e de resposta: libera os novos tipos, se houver restrição de valores.
DO $$
DECLARE c record;
BEGIN
  FOR c IN SELECT conname FROM pg_constraint WHERE conrelid = 'public.notifications'::regclass AND contype = 'c' AND pg_get_constraintdef(oid) ILIKE '%kind%' LOOP
    EXECUTE format('ALTER TABLE public.notifications DROP CONSTRAINT %I', c.conname);
  END LOOP;
END $$;
