-- Cada pessoa pode editar o texto do próprio comentário no Mural.
create policy nc_update on public.neighborhood_comments for update to authenticated
  using (user_id = auth.uid()) with check (user_id = auth.uid());
