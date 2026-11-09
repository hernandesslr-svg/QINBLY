-- QINBLY — tabelas básicas, SÓ PARA UM BANCO NOVO.
-- Cada bloco só age se a tabela ainda não existir; num banco que já funciona, este script não muda nada.
-- Ordem: 00 → (suas políticas de storage/bucket "post-media" públicas) → 03 → 04 → 05.

DO $$ BEGIN
IF to_regclass('public.profiles') IS NULL THEN
  CREATE TABLE public.profiles (
    id uuid PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    username text NOT NULL UNIQUE CHECK (username ~ '^[a-z0-9_.]{3,24}$'),
    display_name text NOT NULL DEFAULT '',
    bio text NOT NULL DEFAULT '',
    avatar_path text, cover_path text, cover_original_path text,
    is_verified boolean NOT NULL DEFAULT false,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now()
  );
  ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;
  CREATE POLICY profiles_select ON public.profiles FOR SELECT USING (true);
  CREATE POLICY profiles_insert ON public.profiles FOR INSERT TO authenticated WITH CHECK (id = auth.uid());
  CREATE POLICY profiles_update ON public.profiles FOR UPDATE TO authenticated USING (id = auth.uid()) WITH CHECK (id = auth.uid());
END IF;

IF to_regclass('public.posts') IS NULL THEN
  CREATE TABLE public.posts (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id uuid NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE DEFAULT auth.uid(),
    caption text NOT NULL DEFAULT '',
    media_type text NOT NULL DEFAULT 'text',
    media_path text,
    visibility text NOT NULL DEFAULT 'public',
    poll_expires_at timestamptz,
    pinned_at timestamptz,
    created_at timestamptz NOT NULL DEFAULT now()
  );
  CREATE INDEX posts_feed_idx ON public.posts (visibility, created_at DESC);
  CREATE INDEX posts_user_idx ON public.posts (user_id, created_at DESC);
  ALTER TABLE public.posts ENABLE ROW LEVEL SECURITY;
  CREATE POLICY posts_select ON public.posts FOR SELECT USING (visibility = 'public' OR user_id = auth.uid());
  CREATE POLICY posts_insert ON public.posts FOR INSERT TO authenticated WITH CHECK (user_id = auth.uid());
  CREATE POLICY posts_update ON public.posts FOR UPDATE TO authenticated USING (user_id = auth.uid()) WITH CHECK (user_id = auth.uid());
  CREATE POLICY posts_delete ON public.posts FOR DELETE TO authenticated USING (user_id = auth.uid());
END IF;

IF to_regclass('public.comments') IS NULL THEN
  CREATE TABLE public.comments (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    post_id uuid NOT NULL REFERENCES public.posts(id) ON DELETE CASCADE,
    user_id uuid NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE DEFAULT auth.uid(),
    body text NOT NULL CHECK (char_length(body) BETWEEN 1 AND 2000),
    created_at timestamptz NOT NULL DEFAULT now()
  );
  CREATE INDEX comments_post_idx ON public.comments (post_id, created_at);
  ALTER TABLE public.comments ENABLE ROW LEVEL SECURITY;
  CREATE POLICY comments_select ON public.comments FOR SELECT USING (true);
  CREATE POLICY comments_insert ON public.comments FOR INSERT TO authenticated WITH CHECK (user_id = auth.uid());
  CREATE POLICY comments_delete ON public.comments FOR DELETE TO authenticated
    USING (user_id = auth.uid() OR EXISTS (SELECT 1 FROM public.posts p WHERE p.id = post_id AND p.user_id = auth.uid()));
END IF;

IF to_regclass('public.post_likes') IS NULL THEN
  CREATE TABLE public.post_likes (
    post_id uuid NOT NULL REFERENCES public.posts(id) ON DELETE CASCADE,
    user_id uuid NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE DEFAULT auth.uid(),
    reaction text NOT NULL DEFAULT 'like',
    created_at timestamptz NOT NULL DEFAULT now(),
    PRIMARY KEY (post_id, user_id)
  );
  ALTER TABLE public.post_likes ENABLE ROW LEVEL SECURITY;
  CREATE POLICY post_likes_select ON public.post_likes FOR SELECT USING (true);
  CREATE POLICY post_likes_insert ON public.post_likes FOR INSERT TO authenticated WITH CHECK (user_id = auth.uid());
  CREATE POLICY post_likes_delete ON public.post_likes FOR DELETE TO authenticated USING (user_id = auth.uid());
END IF;

IF to_regclass('public.follows') IS NULL THEN
  CREATE TABLE public.follows (
    follower_id uuid NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE DEFAULT auth.uid(),
    following_id uuid NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    created_at timestamptz NOT NULL DEFAULT now(),
    PRIMARY KEY (follower_id, following_id),
    CHECK (follower_id <> following_id)
  );
  ALTER TABLE public.follows ENABLE ROW LEVEL SECURITY;
  CREATE POLICY follows_select ON public.follows FOR SELECT USING (true);
  CREATE POLICY follows_insert ON public.follows FOR INSERT TO authenticated WITH CHECK (follower_id = auth.uid());
  CREATE POLICY follows_delete ON public.follows FOR DELETE TO authenticated USING (follower_id = auth.uid());
END IF;

IF to_regclass('public.messages') IS NULL THEN
  CREATE TABLE public.messages (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    sender_id uuid NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE DEFAULT auth.uid(),
    recipient_id uuid NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    body text NOT NULL CHECK (char_length(body) BETWEEN 1 AND 4000),
    read_at timestamptz,
    created_at timestamptz NOT NULL DEFAULT now()
  );
  CREATE INDEX messages_pair_idx ON public.messages (sender_id, recipient_id, created_at);
  CREATE INDEX messages_inbox_idx ON public.messages (recipient_id, read_at);
  ALTER TABLE public.messages ENABLE ROW LEVEL SECURITY;
  CREATE POLICY messages_select ON public.messages FOR SELECT TO authenticated USING (sender_id = auth.uid() OR recipient_id = auth.uid());
  CREATE POLICY messages_insert ON public.messages FOR INSERT TO authenticated WITH CHECK (sender_id = auth.uid());
  CREATE POLICY messages_update ON public.messages FOR UPDATE TO authenticated USING (recipient_id = auth.uid()) WITH CHECK (recipient_id = auth.uid());
END IF;

IF to_regclass('public.notifications') IS NULL THEN
  CREATE TABLE public.notifications (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    recipient_id uuid NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    actor_id uuid REFERENCES public.profiles(id) ON DELETE CASCADE,
    kind text NOT NULL,
    entity_id text,
    read_at timestamptz,
    created_at timestamptz NOT NULL DEFAULT now()
  );
  CREATE INDEX notifications_recipient_idx ON public.notifications (recipient_id, created_at DESC);
  ALTER TABLE public.notifications ENABLE ROW LEVEL SECURITY;
  CREATE POLICY notifications_select ON public.notifications FOR SELECT TO authenticated USING (recipient_id = auth.uid());
  CREATE POLICY notifications_update ON public.notifications FOR UPDATE TO authenticated USING (recipient_id = auth.uid()) WITH CHECK (recipient_id = auth.uid());
  -- Notificações são criadas pela função qinbly-api (chave de serviço); curtir/seguir/comentar em um banco novo
  -- precisam de gatilhos próprios — veja SUPABASE/README.md.
END IF;
END $$;
