-- 24: permite ao dono mudar "quem pode ver" do próprio story depois de publicado.
drop policy if exists stories_update on public.stories;
create policy stories_update on public.stories for update to authenticated
  using (user_id = (select auth.uid())) with check (user_id = (select auth.uid()));
