-- Comunidades dava erro 500 ("infinite recursion detected in policy for relation communities"):
-- a política de leitura de communities consultava community_members, e a de community_members consultava communities.
-- A checagem de membro passa para uma função security definer, que quebra o ciclo.
create or replace function public.qinbly_is_community_member(cid uuid)
returns boolean
language sql
stable
security definer
set search_path = public
as $$
  select exists (
    select 1 from public.community_members m
    where m.community_id = cid and m.user_id = auth.uid()
  );
$$;
revoke execute on function public.qinbly_is_community_member(uuid) from public, anon;
grant execute on function public.qinbly_is_community_member(uuid) to authenticated;

drop policy if exists communities_read on public.communities;
create policy communities_read on public.communities
  for select to authenticated
  using (
    visibility = 'public'
    or owner_id = (select auth.uid())
    or public.qinbly_is_community_member(id)
  );
