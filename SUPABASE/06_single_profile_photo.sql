-- QINBLY — uma única foto de perfil. Rode no SQL Editor (pode rodar mais de uma vez).
-- A foto de perfil é a mesma do cabeçalho do perfil (cover_path). Copia para avatar_path onde divergiam.
UPDATE public.profiles
SET avatar_path = cover_path
WHERE cover_path IS NOT NULL AND cover_path <> '' AND avatar_path IS DISTINCT FROM cover_path;
