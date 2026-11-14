-- Comentários e respostas no Mural do bairro.
create table if not exists public.neighborhood_comments (
  id uuid primary key default gen_random_uuid(),
  notice_id uuid not null references public.neighborhood_notices(id) on delete cascade,
  parent_id uuid references public.neighborhood_comments(id) on delete cascade,
  user_id uuid not null default auth.uid() references public.profiles(id) on delete cascade,
  body text not null check (char_length(body) between 1 and 280),
  created_at timestamptz not null default now()
);
create index if not exists neighborhood_comments_notice_idx on public.neighborhood_comments (notice_id, created_at);
alter table public.neighborhood_comments enable row level security;

-- Vê comentários de avisos que ainda estão no ar (a regra do aviso vale aqui também).
create policy nc_select on public.neighborhood_comments for select to authenticated
  using (exists (select 1 from public.neighborhood_notices n where n.id = notice_id));
create policy nc_insert on public.neighborhood_comments for insert to authenticated
  with check (user_id = auth.uid() and exists (select 1 from public.neighborhood_notices n where n.id = notice_id));
-- Cada pessoa apaga o seu; o autor do aviso apaga qualquer um no aviso dele.
create policy nc_delete on public.neighborhood_comments for delete to authenticated
  using (user_id = auth.uid() or exists (select 1 from public.neighborhood_notices n where n.id = notice_id and n.user_id = auth.uid()));
