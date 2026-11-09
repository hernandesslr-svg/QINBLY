-- QINBLY — figurinhas próprias e comentários de live. Rode no SQL Editor; pode rodar mais de uma vez.

-- 1) Figurinhas criadas pela própria pessoa (a imagem fica no bucket "post-media", em <id-da-pessoa>/sticker/)
CREATE TABLE IF NOT EXISTS public.user_stickers (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  owner_id uuid NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE DEFAULT auth.uid(),
  path text NOT NULL,
  created_at timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS user_stickers_owner_idx ON public.user_stickers (owner_id, created_at DESC);
ALTER TABLE public.user_stickers ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS user_stickers_select ON public.user_stickers;
CREATE POLICY user_stickers_select ON public.user_stickers FOR SELECT TO authenticated USING (owner_id = auth.uid());
DROP POLICY IF EXISTS user_stickers_insert ON public.user_stickers;
CREATE POLICY user_stickers_insert ON public.user_stickers FOR INSERT TO authenticated WITH CHECK (owner_id = auth.uid());
DROP POLICY IF EXISTS user_stickers_delete ON public.user_stickers;
CREATE POLICY user_stickers_delete ON public.user_stickers FOR DELETE TO authenticated USING (owner_id = auth.uid());

-- 2) Comentários de live. A live é identificada pelo @ de quem transmite (minúsculo).
CREATE TABLE IF NOT EXISTS public.live_comments (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  live_host text NOT NULL CHECK (char_length(live_host) BETWEEN 3 AND 24),
  user_id uuid NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE DEFAULT auth.uid(),
  body text NOT NULL CHECK (char_length(body) BETWEEN 1 AND 300),
  created_at timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS live_comments_host_idx ON public.live_comments (live_host, created_at DESC);
ALTER TABLE public.live_comments ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS live_comments_select ON public.live_comments;
CREATE POLICY live_comments_select ON public.live_comments FOR SELECT USING (true);
DROP POLICY IF EXISTS live_comments_insert ON public.live_comments;
CREATE POLICY live_comments_insert ON public.live_comments FOR INSERT TO authenticated WITH CHECK (user_id = auth.uid());
DROP POLICY IF EXISTS live_comments_delete ON public.live_comments;
CREATE POLICY live_comments_delete ON public.live_comments FOR DELETE TO authenticated USING (user_id = auth.uid() OR lower((SELECT username FROM public.profiles WHERE id = auth.uid())) = live_host);
