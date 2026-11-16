-- Publicação de texto com fundo colorido: 0 = sem fundo, 1..6 = cor da paleta do app.
alter table public.posts add column if not exists text_bg smallint not null default 0;
alter table public.posts drop constraint if exists posts_text_bg_check;
alter table public.posts add constraint posts_text_bg_check check (text_bg between 0 and 6);
