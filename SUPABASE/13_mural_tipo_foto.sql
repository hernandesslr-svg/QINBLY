-- Mural do bairro: tipo do aviso (etiqueta) e foto opcional.
alter table public.neighborhood_notices add column if not exists kind text not null default 'aviso';
alter table public.neighborhood_notices add column if not exists photo text;
do $$ begin
  if not exists (select 1 from pg_constraint where conname = 'neighborhood_notices_kind_check') then
    alter table public.neighborhood_notices add constraint neighborhood_notices_kind_check check (kind in ('aviso','achado','perdido','pergunta','evento'));
  end if;
end $$;
