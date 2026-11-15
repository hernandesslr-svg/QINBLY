-- QINBLY — story: quem pode ver (Todos / Amigos / Seguidores / Perto de você) e duração (6h / 12h / 24h).
-- Pode rodar mais de uma vez.

alter table public.profiles add column if not exists city text not null default '';

alter table public.stories add column if not exists audience text not null default 'all';
alter table public.stories drop constraint if exists stories_audience_check;
alter table public.stories add constraint stories_audience_check check (audience in ('all','friends','followers','near'));
alter table public.stories add column if not exists city text not null default '';

-- Quem pode ver cada story (a regra vale também para quem consulta direto o banco).
drop policy if exists stories_select on public.stories;
create policy stories_select on public.stories for select to authenticated
using (
  user_id = auth.uid()
  or (
    expires_at > now()
    and (
      audience = 'all'
      or (audience = 'followers' and exists (
            select 1 from public.follows f where f.follower_id = auth.uid() and f.following_id = stories.user_id))
      or (audience = 'friends' and exists (
            select 1 from public.follows f where f.follower_id = auth.uid() and f.following_id = stories.user_id)
          and exists (
            select 1 from public.follows g where g.follower_id = stories.user_id and g.following_id = auth.uid()))
      or (audience = 'near' and stories.city <> '' and exists (
            select 1 from public.profiles p where p.id = auth.uid() and lower(p.city) = lower(stories.city)))
    )
  )
);
