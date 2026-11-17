-- 23: segurança das funções e velocidade do banco (não muda o comportamento do app).

-- 1) Funções que só quem está logado deve chamar (ou ninguém, no caso do gatilho).
--    qinbly_can_see_author fica como está: as regras de leitura de posts/stories a usam e também rodam para visitantes.
revoke execute on function public.approve_follow_request(uuid) from public, anon;
revoke execute on function public.request_follow(uuid) from public, anon;
grant execute on function public.approve_follow_request(uuid) to authenticated, service_role;
grant execute on function public.request_follow(uuid) to authenticated, service_role;
revoke execute on function public.qinbly_open_account() from public, anon, authenticated;  -- é só gatilho; gatilho roda sem precisar desse direito

-- 2) Regras de acesso: calcular auth.uid() uma vez por consulta, não por linha.
do $$
declare r record; q text; w text; stmt text;
begin
  for r in
    select schemaname, tablename, policyname, qual, with_check from pg_policies
    where schemaname = 'public'
      and ((coalesce(qual,'') ~ 'auth\.uid\(\)' and coalesce(qual,'') !~* 'select auth\.uid\(\)')
        or (coalesce(with_check,'') ~ 'auth\.uid\(\)' and coalesce(with_check,'') !~* 'select auth\.uid\(\)'))
  loop
    stmt := format('alter policy %I on %I.%I', r.policyname, r.schemaname, r.tablename);
    if r.qual is not null then stmt := stmt || ' using (' || replace(r.qual, 'auth.uid()', '(select auth.uid())') || ')'; end if;
    if r.with_check is not null then stmt := stmt || ' with check (' || replace(r.with_check, 'auth.uid()', '(select auth.uid())') || ')'; end if;
    execute stmt;
  end loop;
end $$;

-- 3) Índices para chaves estrangeiras que ainda não têm.
do $$
declare r record; cols text; nm text;
begin
  for r in
    select c.oid, c.conrelid, c.conkey, c.conrelid::regclass as tbl, c.conname
    from pg_constraint c join pg_namespace n on n.oid = c.connamespace
    where c.contype = 'f' and n.nspname = 'public'
      and not exists (
        select 1 from pg_index i
        where i.indrelid = c.conrelid and (i.indkey::int2[])[0:array_length(c.conkey,1)-1] = c.conkey)
  loop
    select string_agg(quote_ident(a.attname), ', ' order by k.ord) into cols
      from unnest(r.conkey) with ordinality k(attnum, ord) join pg_attribute a on a.attrelid = r.conrelid and a.attnum = k.attnum;
    nm := left(r.conname, 55) || '_idx';
    execute format('create index if not exists %I on %s (%s)', nm, r.tbl, cols);
  end loop;
end $$;
