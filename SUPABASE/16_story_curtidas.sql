-- Curtidas de story (só o dono vê quem curtiu) e cidade de quem viu (para "Perto de você").
alter table public.story_views add column if not exists city text;
drop policy if exists story_views_update_own on public.story_views;
create policy story_views_update_own on public.story_views for update to authenticated
  using (viewer_id = auth.uid()) with check (viewer_id = auth.uid());

create table if not exists public.story_likes (
  story_id uuid not null references public.stories(id) on delete cascade,
  user_id uuid not null references public.profiles(id) on delete cascade,
  created_at timestamptz not null default now(),
  primary key (story_id, user_id)
);
alter table public.story_likes enable row level security;
create policy story_likes_select on public.story_likes for select to authenticated
  using (user_id = auth.uid() or exists (select 1 from public.stories s where s.id = story_id and s.user_id = auth.uid()));
create policy story_likes_insert on public.story_likes for insert to authenticated
  with check (user_id = auth.uid() and exists (select 1 from public.stories s where s.id = story_id and s.expires_at > now()));
create policy story_likes_delete on public.story_likes for delete to authenticated
  using (user_id = auth.uid());
grant select, insert, delete on public.story_likes to authenticated;
