-- Mural de avisos: aviso e comentário passam de 280 para até 500 letras.
alter table public.neighborhood_notices drop constraint if exists neighborhood_notices_body_check;
alter table public.neighborhood_notices add constraint neighborhood_notices_body_check check (char_length(body) between 1 and 500);
alter table public.neighborhood_comments drop constraint if exists neighborhood_comments_body_check;
alter table public.neighborhood_comments add constraint neighborhood_comments_body_check check (char_length(body) between 1 and 500);
