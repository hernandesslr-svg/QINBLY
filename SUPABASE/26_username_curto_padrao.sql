-- Conta nova (Google ou e-mail) nasce com nome de usuário curto: user_ + 8 letras/números (13 caracteres) em vez de 32.
-- Só muda o padrão para contas novas; quem já tem nome não é alterado.
ALTER TABLE public.profiles
  ALTER COLUMN username SET DEFAULT ('user_' || substr(replace(gen_random_uuid()::text, '-', ''), 1, 8));
