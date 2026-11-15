-- 18: privacidade da conta (conta privada, pedidos de seguir, quem fala e quem comenta)
alter table public.profiles add column if not exists is_private boolean not null default false;
alter table public.profiles add column if not exists who_can_message text not null default 'all';
alter table public.profiles add column if not exists who_can_comment text not null default 'all';
alter table public.profiles drop constraint if exists profiles_who_can_message_chk;
alter table public.profiles add constraint profiles_who_can_message_chk check (who_can_message in ('all','following','nobody'));
alter table public.profiles drop constraint if exists profiles_who_can_comment_chk;
alter table public.profiles add constraint profiles_who_can_comment_chk check (who_can_comment in ('all','followers','nobody'));

create table if not exists public.follow_requests (
  requester_id uuid not null references public.profiles(id) on delete cascade,
  target_id uuid not null references public.profiles(id) on delete cascade,
  created_at timestamptz not null default now(),
  primary key (requester_id, target_id),
  check (requester_id <> target_id)
);
alter table public.follow_requests enable row level security;
drop policy if exists follow_requests_select on public.follow_requests;
create policy follow_requests_select on public.follow_requests for select
  using (requester_id = (select auth.uid()) or target_id = (select auth.uid()));
drop policy if exists follow_requests_delete on public.follow_requests;
create policy follow_requests_delete on public.follow_requests for delete
  using (requester_id = (select auth.uid()) or target_id = (select auth.uid()));
-- sem policy de INSERT/UPDATE: só as funções abaixo criam e aprovam pedidos.

-- Quem pode ver o conteúdo de um autor: ele mesmo, contas abertas, ou quem já segue uma conta privada.
create or replace function public.qinbly_can_see_author(author uuid) returns boolean
language sql stable security definer set search_path = public as $$
  select author = auth.uid()
    or not coalesce((select p.is_private from public.profiles p where p.id = author), false)
    or exists (select 1 from public.follows f where f.follower_id = auth.uid() and f.following_id = author);
$$;
grant execute on function public.qinbly_can_see_author(uuid) to anon, authenticated;

drop policy if exists posts_read on public.posts;
create policy posts_read on public.posts for select
  using (user_id = (select auth.uid()) or (visibility = 'public' and public.qinbly_can_see_author(user_id)));

drop policy if exists stories_select on public.stories;
create policy stories_select on public.stories for select using (
  user_id = auth.uid() or (
    expires_at > now() and public.qinbly_can_see_author(user_id) and (
      audience = 'all'
      or (audience = 'followers' and exists (select 1 from public.follows f where f.follower_id = auth.uid() and f.following_id = stories.user_id))
      or (audience = 'friends' and exists (select 1 from public.follows f where f.follower_id = auth.uid() and f.following_id = stories.user_id)
                               and exists (select 1 from public.follows g where g.follower_id = stories.user_id and g.following_id = auth.uid()))
      or (audience = 'near' and city <> '' and exists (select 1 from public.profiles p where p.id = auth.uid() and lower(p.city) = lower(stories.city)))
    )
  )
);

-- Seguir: conta privada não aceita seguir direto (só por pedido aprovado).
create or replace function public.qinbly_guard_follow() returns trigger
language plpgsql set search_path = public as $$
begin
  if coalesce(current_setting('qinbly.allow_follow', true), '') = '1' then return new; end if;
  if exists (select 1 from public.profiles p where p.id = new.following_id and p.is_private) then
    raise exception 'Esta conta é privada. Envie um pedido de seguir.' using errcode = '42501';
  end if;
  return new;
end $$;
drop trigger if exists trg_guard_follow on public.follows;
create trigger trg_guard_follow before insert on public.follows for each row execute function public.qinbly_guard_follow();

-- Mensagens: respeita "quem pode falar comigo" (quem já recebeu mensagem da pessoa pode responder).
create or replace function public.qinbly_guard_message() returns trigger
language plpgsql set search_path = public as $$
declare mode text;
begin
  select who_can_message into mode from public.profiles where id = new.recipient_id;
  if coalesce(mode, 'all') = 'all' then return new; end if;
  if exists (select 1 from public.messages m where m.sender_id = new.recipient_id and m.recipient_id = new.sender_id) then return new; end if;
  if mode = 'following' and exists (select 1 from public.follows f where f.follower_id = new.recipient_id and f.following_id = new.sender_id) then return new; end if;
  raise exception 'Esta pessoa não está recebendo mensagens suas.' using errcode = '42501';
end $$;
drop trigger if exists trg_guard_message on public.messages;
create trigger trg_guard_message before insert on public.messages for each row execute function public.qinbly_guard_message();

-- Comentários: respeita "quem pode comentar" do dono da publicação (o dono sempre pode).
create or replace function public.qinbly_guard_comment() returns trigger
language plpgsql set search_path = public as $$
declare owner uuid; mode text;
begin
  select p.user_id, pr.who_can_comment into owner, mode from public.posts p join public.profiles pr on pr.id = p.user_id where p.id = new.post_id;
  if owner is null or owner = new.user_id or coalesce(mode, 'all') = 'all' then return new; end if;
  if mode = 'followers' and exists (select 1 from public.follows f where f.follower_id = new.user_id and f.following_id = owner) then return new; end if;
  raise exception 'Os comentários desta publicação estão limitados pelo dono.' using errcode = '42501';
end $$;
drop trigger if exists trg_guard_comment on public.comments;
create trigger trg_guard_comment before insert on public.comments for each row execute function public.qinbly_guard_comment();

-- Pedir para seguir. Retorna: 'following' (já segue), 'open' (conta aberta), 'requested', 'cancelled'.
create or replace function public.request_follow(target uuid) returns text
language plpgsql security definer set search_path = public as $$
declare me uuid := auth.uid();
begin
  if me is null then raise exception 'Entre na sua conta.' using errcode = '42501'; end if;
  if target = me then raise exception 'Você não pode seguir a própria conta.'; end if;
  if exists (select 1 from public.follows where follower_id = me and following_id = target) then return 'following'; end if;
  if not coalesce((select is_private from public.profiles where id = target), false) then return 'open'; end if;
  if exists (select 1 from public.follow_requests where requester_id = me and target_id = target) then
    delete from public.follow_requests where requester_id = me and target_id = target;
    delete from public.notifications where recipient_id = target and actor_id = me and kind = 'follow_request';
    return 'cancelled';
  end if;
  if exists (select 1 from public.user_blocks where blocker_id = target and blocked_id = me) then
    raise exception 'Não foi possível enviar o pedido.';
  end if;
  insert into public.follow_requests(requester_id, target_id) values (me, target);
  insert into public.notifications(recipient_id, actor_id, kind, entity_id) values (target, me, 'follow_request', me);
  return 'requested';
end $$;
grant execute on function public.request_follow(uuid) to authenticated;

-- Aprovar pedido (só o dono da conta).
create or replace function public.approve_follow_request(requester uuid) returns void
language plpgsql security definer set search_path = public as $$
declare me uuid := auth.uid();
begin
  if me is null then raise exception 'Entre na sua conta.' using errcode = '42501'; end if;
  if not exists (select 1 from public.follow_requests where requester_id = requester and target_id = me) then
    raise exception 'Pedido não encontrado.';
  end if;
  perform set_config('qinbly.allow_follow', '1', true);
  insert into public.follows(follower_id, following_id) values (requester, me) on conflict do nothing;
  delete from public.follow_requests where requester_id = requester and target_id = me;
  delete from public.notifications where recipient_id = me and actor_id = requester and kind = 'follow_request';
  insert into public.notifications(recipient_id, actor_id, kind, entity_id) values (requester, me, 'follow_accepted', me);
end $$;
grant execute on function public.approve_follow_request(uuid) to authenticated;

-- Se a conta deixa de ser privada, os pedidos pendentes viram seguidores.
create or replace function public.qinbly_open_account() returns trigger
language plpgsql security definer set search_path = public as $$
begin
  if old.is_private and not new.is_private then
    perform set_config('qinbly.allow_follow', '1', true);
    insert into public.follows(follower_id, following_id)
      select requester_id, new.id from public.follow_requests where target_id = new.id on conflict do nothing;
    delete from public.follow_requests where target_id = new.id;
    delete from public.notifications where recipient_id = new.id and kind = 'follow_request';
  end if;
  return new;
end $$;
drop trigger if exists trg_open_account on public.profiles;
create trigger trg_open_account after update of is_private on public.profiles for each row execute function public.qinbly_open_account();
