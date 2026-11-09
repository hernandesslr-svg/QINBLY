-- QINBLY — permite editar o próprio comentário. Pode rodar mais de uma vez.
ALTER TABLE public.comments ADD COLUMN IF NOT EXISTS edited_at timestamptz;
DROP POLICY IF EXISTS comments_update_own ON public.comments;
CREATE POLICY comments_update_own ON public.comments FOR UPDATE TO authenticated
  USING (user_id = auth.uid()) WITH CHECK (user_id = auth.uid());
