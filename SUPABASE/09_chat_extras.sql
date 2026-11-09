-- QINBLY — enquetes no chat, marcar pessoas em publicações. Rode no SQL Editor; pode rodar mais de uma vez.
-- (Documentos no chat não precisam de tabela: usam o bucket "post-media".)

-- 1) Enquetes enviadas em conversas (mensagens diretas e comunidades)
CREATE TABLE IF NOT EXISTS public.chat_polls (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  creator_id uuid NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE DEFAULT auth.uid(),
  question text NOT NULL CHECK (char_length(question) BETWEEN 1 AND 200),
  options jsonb NOT NULL,
  created_at timestamptz NOT NULL DEFAULT now()
);
CREATE TABLE IF NOT EXISTS public.chat_poll_votes (
  poll_id uuid NOT NULL REFERENCES public.chat_polls(id) ON DELETE CASCADE,
  user_id uuid NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE DEFAULT auth.uid(),
  option_index int NOT NULL CHECK (option_index BETWEEN 0 AND 5),
  created_at timestamptz NOT NULL DEFAULT now(),
  PRIMARY KEY (poll_id, user_id)
);
ALTER TABLE public.chat_polls ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.chat_poll_votes ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS chat_polls_select ON public.chat_polls;
CREATE POLICY chat_polls_select ON public.chat_polls FOR SELECT TO authenticated USING (true);
DROP POLICY IF EXISTS chat_polls_insert ON public.chat_polls;
CREATE POLICY chat_polls_insert ON public.chat_polls FOR INSERT TO authenticated WITH CHECK (creator_id = auth.uid());
DROP POLICY IF EXISTS chat_poll_votes_select ON public.chat_poll_votes;
CREATE POLICY chat_poll_votes_select ON public.chat_poll_votes FOR SELECT TO authenticated USING (true);
DROP POLICY IF EXISTS chat_poll_votes_insert ON public.chat_poll_votes;
CREATE POLICY chat_poll_votes_insert ON public.chat_poll_votes FOR INSERT TO authenticated WITH CHECK (user_id = auth.uid());
DROP POLICY IF EXISTS chat_poll_votes_update ON public.chat_poll_votes;
CREATE POLICY chat_poll_votes_update ON public.chat_poll_votes FOR UPDATE TO authenticated USING (user_id = auth.uid()) WITH CHECK (user_id = auth.uid());

-- 2) Pessoas marcadas em publicações
CREATE TABLE IF NOT EXISTS public.post_tags (
  post_id uuid NOT NULL REFERENCES public.posts(id) ON DELETE CASCADE,
  tagged_user_id uuid NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
  tagged_by uuid NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE DEFAULT auth.uid(),
  created_at timestamptz NOT NULL DEFAULT now(),
  PRIMARY KEY (post_id, tagged_user_id)
);
CREATE INDEX IF NOT EXISTS post_tags_user_idx ON public.post_tags (tagged_user_id, created_at DESC);
ALTER TABLE public.post_tags ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS post_tags_select ON public.post_tags;
CREATE POLICY post_tags_select ON public.post_tags FOR SELECT USING (true);
DROP POLICY IF EXISTS post_tags_insert ON public.post_tags;
CREATE POLICY post_tags_insert ON public.post_tags FOR INSERT TO authenticated
  WITH CHECK (tagged_by = auth.uid() AND EXISTS (SELECT 1 FROM public.posts p WHERE p.id = post_id AND p.user_id = auth.uid()));
DROP POLICY IF EXISTS post_tags_delete ON public.post_tags;
CREATE POLICY post_tags_delete ON public.post_tags FOR DELETE TO authenticated USING (tagged_by = auth.uid() OR tagged_user_id = auth.uid());

-- 3) Se a tabela de notificações limita os tipos, libera o novo tipo "tag" (marcação).
DO $$
DECLARE c record;
BEGIN
  FOR c IN SELECT conname FROM pg_constraint WHERE conrelid = 'public.notifications'::regclass AND contype = 'c' AND pg_get_constraintdef(oid) ILIKE '%kind%' LOOP
    EXECUTE format('ALTER TABLE public.notifications DROP CONSTRAINT %I', c.conname);
  END LOOP;
END $$;
