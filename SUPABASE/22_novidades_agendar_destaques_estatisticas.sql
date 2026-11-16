-- QINBLY — agendar publicação, destaques do perfil e estatísticas da publicação. Pode rodar mais de uma vez.

-- 1) Agendar: a publicação só aparece para os outros quando publish_at chega.
alter table public.posts add column if not exists publish_at timestamptz;
create index if not exists posts_publish_at_idx on public.posts (publish_at) where publish_at is not null;
drop policy if exists posts_read on public.posts;
create policy posts_read on public.posts for select
  using (
    user_id = (select auth.uid())
    or (visibility = 'public' and public.qinbly_can_see_author(user_id) and (publish_at is null or publish_at <= now()))
  );

-- 2) Destaques: cópia permanente de um story (o story some em 24 h, o destaque fica no perfil).
create table if not exists public.story_highlights (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references public.profiles(id) on delete cascade,
  title text not null default 'Destaques' check (char_length(title) between 1 and 24),
  media_path text not null,
  media_type text not null check (media_type in ('image','video','text')),
  caption text not null default '',
  created_at timestamptz not null default now()
);
create index if not exists story_highlights_user_idx on public.story_highlights (user_id, created_at desc);
alter table public.story_highlights enable row level security;
drop policy if exists story_highlights_select on public.story_highlights;
create policy story_highlights_select on public.story_highlights for select
  using (user_id = (select auth.uid()) or public.qinbly_can_see_author(user_id));
drop policy if exists story_highlights_insert on public.story_highlights;
create policy story_highlights_insert on public.story_highlights for insert to authenticated
  with check (user_id = (select auth.uid()));
drop policy if exists story_highlights_delete on public.story_highlights;
create policy story_highlights_delete on public.story_highlights for delete to authenticated
  using (user_id = (select auth.uid()));

-- 3) Estatísticas: uma visualização por pessoa, por publicação e por dia. Só o dono da publicação lê.
create table if not exists public.post_views (
  post_id uuid not null references public.posts(id) on delete cascade,
  viewer_id uuid not null references public.profiles(id) on delete cascade,
  day date not null default ((now() at time zone 'utc')::date),
  created_at timestamptz not null default now(),
  primary key (post_id, viewer_id, day)
);
create index if not exists post_views_viewer_idx on public.post_views (viewer_id);
alter table public.post_views enable row level security;
drop policy if exists post_views_insert on public.post_views;
create policy post_views_insert on public.post_views for insert to authenticated
  with check (viewer_id = (select auth.uid()));
drop policy if exists post_views_select on public.post_views;
create policy post_views_select on public.post_views for select to authenticated
  using (exists (select 1 from public.posts p where p.id = post_id and p.user_id = (select auth.uid())));
