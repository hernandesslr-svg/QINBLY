-- 25: "Silenciar stories" passa a valer em todos os aparelhos da pessoa (antes ficava só no celular).
create table if not exists public.story_mutes (
  user_id uuid not null default auth.uid() references auth.users(id) on delete cascade,
  muted_user_id uuid not null references auth.users(id) on delete cascade,
  created_at timestamptz not null default now(),
  primary key (user_id, muted_user_id),
  constraint story_mutes_no_self check (user_id <> muted_user_id)
);
create index if not exists story_mutes_muted_idx on public.story_mutes (muted_user_id);
alter table public.story_mutes enable row level security;
revoke all on public.story_mutes from anon, public;
grant select, insert, delete on public.story_mutes to authenticated;
drop policy if exists story_mutes_own on public.story_mutes;
create policy story_mutes_own on public.story_mutes for all to authenticated
  using (user_id = (select auth.uid())) with check (user_id = (select auth.uid()));

-- Liga/desliga o silêncio pelo nome de usuário.
create or replace function public.story_mute_set(p_username text, p_muted boolean) returns void
language plpgsql security invoker set search_path = public as $$
declare target uuid;
begin
  select id into target from public.profiles where lower(username) = lower(trim(both '@' from p_username));
  if target is null or target = (select auth.uid()) then return; end if;
  if p_muted then
    insert into public.story_mutes (user_id, muted_user_id) values ((select auth.uid()), target) on conflict do nothing;
  else
    delete from public.story_mutes where user_id = (select auth.uid()) and muted_user_id = target;
  end if;
end $$;

-- Nomes de usuário que eu silenciei.
create or replace function public.story_mute_list() returns setof text
language sql stable security invoker set search_path = public as $$
  select lower(p.username) from public.story_mutes m join public.profiles p on p.id = m.muted_user_id
  where m.user_id = (select auth.uid())
$$;

revoke execute on function public.story_mute_set(text, boolean) from public, anon;
revoke execute on function public.story_mute_list() from public, anon;
grant execute on function public.story_mute_set(text, boolean) to authenticated;
grant execute on function public.story_mute_list() to authenticated;
