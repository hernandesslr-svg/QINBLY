-- Perto de mim: anúncios do bairro (vendas, doações, achados e perdidos, eventos, serviços).
-- Rode no SQL Editor do Supabase (veja COMO_EXECUTAR_MIGRACAO.md). Pode rodar mais de uma vez.
CREATE TABLE IF NOT EXISTS public.marketplace_listings (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id uuid NOT NULL DEFAULT auth.uid() REFERENCES public.profiles(id) ON DELETE CASCADE,
  kind text NOT NULL CHECK (kind IN ('venda','doacao','achado','perdido','evento','servico')),
  title text NOT NULL CHECK (char_length(title) BETWEEN 1 AND 120),
  description text NOT NULL DEFAULT '' CHECK (char_length(description) <= 2000),
  price numeric(12,2) CHECK (price IS NULL OR price >= 0),
  extra text NOT NULL DEFAULT '' CHECK (char_length(extra) <= 80),   -- estado (Novo/Bom/Usado) ou "quando" do evento
  photos text[] NOT NULL DEFAULT '{}' CHECK (cardinality(photos) <= 6),
  lat double precision NOT NULL,   -- sempre aproximada (~1 km) pelo app
  lng double precision NOT NULL,
  created_at timestamptz NOT NULL DEFAULT now(),
  expires_at timestamptz NOT NULL DEFAULT (now() + interval '7 days')
);

CREATE INDEX IF NOT EXISTS marketplace_listings_geo_idx ON public.marketplace_listings (lat, lng);
CREATE INDEX IF NOT EXISTS marketplace_listings_kind_idx ON public.marketplace_listings (kind, created_at DESC);

ALTER TABLE public.marketplace_listings ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS marketplace_select ON public.marketplace_listings;
CREATE POLICY marketplace_select ON public.marketplace_listings FOR SELECT TO authenticated
  USING (expires_at > now() OR user_id = auth.uid());
DROP POLICY IF EXISTS marketplace_insert ON public.marketplace_listings;
CREATE POLICY marketplace_insert ON public.marketplace_listings FOR INSERT TO authenticated
  WITH CHECK (user_id = auth.uid());
DROP POLICY IF EXISTS marketplace_update ON public.marketplace_listings;
CREATE POLICY marketplace_update ON public.marketplace_listings FOR UPDATE TO authenticated
  USING (user_id = auth.uid()) WITH CHECK (user_id = auth.uid());
DROP POLICY IF EXISTS marketplace_delete ON public.marketplace_listings;
CREATE POLICY marketplace_delete ON public.marketplace_listings FOR DELETE TO authenticated
  USING (user_id = auth.uid());
