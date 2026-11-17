-- Fundo do texto com cor livre: 0 = sem fundo, 1..6 = paleta, 16777216 + RGB = cor escolhida na roda de cores.
alter table public.posts alter column text_bg type integer;
alter table public.posts drop constraint if exists posts_text_bg_check;
alter table public.posts add constraint posts_text_bg_check check (text_bg between 0 and 6 or text_bg between 16777216 and 33554431);
