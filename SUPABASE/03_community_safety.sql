-- QINBLY: segurança de comunidade (bloqueio, denúncia, suspensão).
-- Aditivo e idempotente: usa policies RESTRICTIVE, que se SOMAM às policies atuais
-- (não reescreve posts_read etc., então funciona mesmo que o banco tenha evoluído além do 01_schema.sql).
-- Execute no SQL Editor do Supabase como dono do projeto, DEPOIS de 01_schema.sql e 02_social_notifications.sql.
BEGIN;

ALTER TABLE public.profiles ADD COLUMN IF NOT EXISTS suspended_at timestamptz;  -- sem GRANT de UPDATE: só o painel/service_role altera

-- ---------------------------------------------------------------------------
-- Tabelas (sem acesso direto: tudo passa por funções SECURITY DEFINER abaixo)
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.user_blocks (
  blocker_id uuid NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
  blocked_id uuid NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
  created_at timestamptz NOT NULL DEFAULT now(),
  PRIMARY KEY (blocker_id, blocked_id),
  CONSTRAINT user_blocks_no_self CHECK (blocker_id <> blocked_id)
);
CREATE INDEX IF NOT EXISTS user_blocks_blocked_idx ON public.user_blocks (blocked_id);

-- target_id sem FK de propósito: a denúncia deve sobreviver à remoção do conteúdo.
CREATE TABLE IF NOT EXISTS public.content_reports (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  reporter_id uuid NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
  target_type text NOT NULL CHECK (target_type IN ('post','comment','user','message')),
  target_id uuid NOT NULL,
  reason text NOT NULL CHECK (reason IN ('spam','harassment','hate','violence','sexual','minor_safety','self_harm','illegal','other')),
  details text NOT NULL DEFAULT '' CHECK (char_length(details) <= 500),
  status text NOT NULL DEFAULT 'open' CHECK (status IN ('open','actioned','dismissed')),
  resolution_note text,
  created_at timestamptz NOT NULL DEFAULT now(),
  resolved_at timestamptz,
  UNIQUE (reporter_id, target_type, target_id)
);
CREATE INDEX IF NOT EXISTS content_reports_status_idx ON public.content_reports (status, created_at);

ALTER TABLE public.user_blocks ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.content_reports ENABLE ROW LEVEL SECURITY;
REVOKE ALL ON public.user_blocks, public.content_reports FROM anon, authenticated;

-- ---------------------------------------------------------------------------
-- Funções auxiliares usadas pelas policies
-- ---------------------------------------------------------------------------
-- true se o autor pode ser visto por quem faz a requisição: não suspenso e sem bloqueio em nenhum sentido.
CREATE OR REPLACE FUNCTION public.author_visible(author uuid)
RETURNS boolean LANGUAGE sql STABLE SECURITY DEFINER SET search_path = ''
AS $fn$
  SELECT author IS NOT NULL
    AND NOT EXISTS (SELECT 1 FROM public.profiles pr
                    WHERE pr.id = author AND pr.suspended_at IS NOT NULL
                      AND pr.id IS DISTINCT FROM (SELECT auth.uid()))
    AND NOT EXISTS (SELECT 1 FROM public.user_blocks b
                    WHERE (SELECT auth.uid()) IS NOT NULL
                      AND ((b.blocker_id = (SELECT auth.uid()) AND b.blocked_id = author)
                        OR (b.blocker_id = author AND b.blocked_id = (SELECT auth.uid()))));
$fn$;

CREATE OR REPLACE FUNCTION public.not_suspended()
RETURNS boolean LANGUAGE sql STABLE SECURITY DEFINER SET search_path = ''
AS $fn$
  SELECT NOT EXISTS (SELECT 1 FROM public.profiles WHERE id = (SELECT auth.uid()) AND suspended_at IS NOT NULL);
$fn$;

REVOKE ALL ON FUNCTION public.author_visible(uuid), public.not_suspended() FROM PUBLIC;
GRANT EXECUTE ON FUNCTION public.author_visible(uuid), public.not_suspended() TO anon, authenticated;

-- ---------------------------------------------------------------------------
-- RPCs chamadas pela Edge Function com o token do usuário
-- ---------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION public.toggle_block(target uuid)
RETURNS boolean LANGUAGE plpgsql SECURITY DEFINER SET search_path = ''
AS $fn$
DECLARE me uuid := (SELECT auth.uid());
BEGIN
  IF me IS NULL THEN RAISE EXCEPTION 'not_authenticated' USING ERRCODE = '28000'; END IF;
  IF target = me THEN RAISE EXCEPTION 'cannot_block_self' USING ERRCODE = '22023'; END IF;
  IF NOT EXISTS (SELECT 1 FROM public.profiles WHERE id = target) THEN RAISE EXCEPTION 'not_found' USING ERRCODE = 'P0002'; END IF;
  DELETE FROM public.user_blocks WHERE blocker_id = me AND blocked_id = target;
  IF FOUND THEN RETURN false; END IF;
  INSERT INTO public.user_blocks (blocker_id, blocked_id) VALUES (me, target);
  -- Bloquear desfaz o seguimento nos dois sentidos.
  DELETE FROM public.follows WHERE (follower_id = me AND following_id = target) OR (follower_id = target AND following_id = me);
  RETURN true;
END;
$fn$;

CREATE OR REPLACE FUNCTION public.list_my_blocks()
RETURNS TABLE (id uuid, username text, display_name text, avatar_path text, blocked_at timestamptz)
LANGUAGE sql STABLE SECURITY DEFINER SET search_path = ''
AS $fn$
  SELECT p.id, p.username, p.display_name, p.avatar_path, b.created_at
  FROM public.user_blocks b JOIN public.profiles p ON p.id = b.blocked_id
  WHERE b.blocker_id = (SELECT auth.uid())
  ORDER BY b.created_at DESC LIMIT 200;
$fn$;

CREATE OR REPLACE FUNCTION public.report_content(p_type text, p_target uuid, p_reason text, p_details text DEFAULT '')
RETURNS void LANGUAGE plpgsql SECURITY DEFINER SET search_path = ''
AS $fn$
DECLARE me uuid := (SELECT auth.uid()); v_owner uuid;
BEGIN
  IF me IS NULL THEN RAISE EXCEPTION 'not_authenticated' USING ERRCODE = '28000'; END IF;
  IF p_type = 'post' THEN SELECT user_id INTO v_owner FROM public.posts WHERE id = p_target AND visibility = 'public';
  ELSIF p_type = 'comment' THEN SELECT user_id INTO v_owner FROM public.comments WHERE id = p_target;
  ELSIF p_type = 'user' THEN SELECT id INTO v_owner FROM public.profiles WHERE id = p_target;
  ELSIF p_type = 'message' THEN SELECT sender_id INTO v_owner FROM public.messages WHERE id = p_target AND (sender_id = me OR recipient_id = me);
  ELSE RAISE EXCEPTION 'invalid_type' USING ERRCODE = '22023'; END IF;
  IF v_owner IS NULL THEN RAISE EXCEPTION 'not_found' USING ERRCODE = 'P0002'; END IF;
  IF v_owner = me THEN RAISE EXCEPTION 'cannot_report_self' USING ERRCODE = '22023'; END IF;
  INSERT INTO public.content_reports (reporter_id, target_type, target_id, reason, details)
  VALUES (me, p_type, p_target, p_reason, left(coalesce(p_details, ''), 500))
  ON CONFLICT (reporter_id, target_type, target_id) DO NOTHING;
END;
$fn$;

REVOKE ALL ON FUNCTION public.toggle_block(uuid), public.list_my_blocks(), public.report_content(text, uuid, text, text) FROM PUBLIC, anon;
GRANT EXECUTE ON FUNCTION public.toggle_block(uuid), public.list_my_blocks(), public.report_content(text, uuid, text, text) TO authenticated;

-- ---------------------------------------------------------------------------
-- Policies RESTRICTIVE (E lógico com as permissivas já existentes)
-- ---------------------------------------------------------------------------
DROP POLICY IF EXISTS profiles_hide_blocked ON public.profiles;
CREATE POLICY profiles_hide_blocked ON public.profiles AS RESTRICTIVE FOR SELECT TO anon, authenticated
  USING (public.author_visible(id));

DROP POLICY IF EXISTS posts_hide_blocked ON public.posts;
CREATE POLICY posts_hide_blocked ON public.posts AS RESTRICTIVE FOR SELECT TO anon, authenticated
  USING (public.author_visible(user_id));
DROP POLICY IF EXISTS posts_not_suspended ON public.posts;
CREATE POLICY posts_not_suspended ON public.posts AS RESTRICTIVE FOR INSERT TO authenticated
  WITH CHECK (public.not_suspended());

DROP POLICY IF EXISTS comments_hide_blocked ON public.comments;
CREATE POLICY comments_hide_blocked ON public.comments AS RESTRICTIVE FOR SELECT TO anon, authenticated
  USING (public.author_visible(user_id));
DROP POLICY IF EXISTS comments_block_insert ON public.comments;
CREATE POLICY comments_block_insert ON public.comments AS RESTRICTIVE FOR INSERT TO authenticated
  WITH CHECK (public.not_suspended() AND public.author_visible((SELECT p.user_id FROM public.posts p WHERE p.id = post_id)));

DROP POLICY IF EXISTS likes_hide_blocked ON public.post_likes;
CREATE POLICY likes_hide_blocked ON public.post_likes AS RESTRICTIVE FOR SELECT TO anon, authenticated
  USING (public.author_visible(user_id));
DROP POLICY IF EXISTS likes_block_insert ON public.post_likes;
CREATE POLICY likes_block_insert ON public.post_likes AS RESTRICTIVE FOR INSERT TO authenticated
  WITH CHECK (public.not_suspended() AND public.author_visible((SELECT p.user_id FROM public.posts p WHERE p.id = post_id)));

DROP POLICY IF EXISTS follows_hide_blocked ON public.follows;
CREATE POLICY follows_hide_blocked ON public.follows AS RESTRICTIVE FOR SELECT TO anon, authenticated
  USING (public.author_visible(follower_id) AND public.author_visible(following_id));
DROP POLICY IF EXISTS follows_block_insert ON public.follows;
CREATE POLICY follows_block_insert ON public.follows AS RESTRICTIVE FOR INSERT TO authenticated
  WITH CHECK (public.not_suspended() AND public.author_visible(following_id));

DROP POLICY IF EXISTS messages_hide_blocked ON public.messages;
CREATE POLICY messages_hide_blocked ON public.messages AS RESTRICTIVE FOR SELECT TO authenticated
  USING (public.author_visible(CASE WHEN sender_id = (SELECT auth.uid()) THEN recipient_id ELSE sender_id END));
DROP POLICY IF EXISTS messages_block_insert ON public.messages;
CREATE POLICY messages_block_insert ON public.messages AS RESTRICTIVE FOR INSERT TO authenticated
  WITH CHECK (public.not_suspended() AND public.author_visible(recipient_id));

-- O dono da publicação pode apagar comentários feitos nela (além de cada autor apagar o próprio).
DROP POLICY IF EXISTS comments_delete_by_post_owner ON public.comments;
CREATE POLICY comments_delete_by_post_owner ON public.comments FOR DELETE TO authenticated
  USING (EXISTS (SELECT 1 FROM public.posts p WHERE p.id = post_id AND p.user_id = (SELECT auth.uid())));

COMMIT;

-- ===========================================================================
-- MODERAÇÃO (rode no SQL Editor; ninguém no app tem acesso a estas tabelas)
-- ---------------------------------------------------------------------------
--  Fila de denúncias abertas:
--    SELECT r.id, r.created_at, r.target_type, r.target_id, r.reason, r.details, p.username AS reporter
--    FROM public.content_reports r JOIN public.profiles p ON p.id = r.reporter_id
--    WHERE r.status = 'open' ORDER BY r.created_at;
--  Remover conteúdo:   DELETE FROM public.posts    WHERE id = '<target_id>';
--                      DELETE FROM public.comments WHERE id = '<target_id>';
--  Suspender autor:    UPDATE public.profiles SET suspended_at = now() WHERE id = '<user_id>';
--  Reativar:           UPDATE public.profiles SET suspended_at = NULL  WHERE id = '<user_id>';
--  Fechar denúncia:    UPDATE public.content_reports SET status='actioned', resolved_at=now(), resolution_note='...' WHERE id='<id>';
-- ===========================================================================
