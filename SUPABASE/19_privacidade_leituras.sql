-- 19: conta privada fecha tudo que ainda vazava (leituras abertas demais)
-- Quem segue quem: só aparece se as duas pontas são visíveis para quem lê (ou se for você).
drop policy if exists follows_read on public.follows;
create policy follows_read on public.follows for select using (
  follower_id = (select auth.uid()) or following_id = (select auth.uid())
  or (public.qinbly_can_see_author(follower_id) and public.qinbly_can_see_author(following_id))
);
drop policy if exists post_tags_select on public.post_tags;
create policy post_tags_select on public.post_tags for select using (
  tagged_user_id = (select auth.uid()) or tagged_by = (select auth.uid())
  or exists (select 1 from public.posts p where p.id = post_tags.post_id)
);
drop policy if exists post_shares_read on public.post_shares;
create policy post_shares_read on public.post_shares for select using (
  user_id = (select auth.uid()) or exists (select 1 from public.posts p where p.id = post_shares.post_id)
);
drop policy if exists poll_votes_read on public.poll_votes;
create policy poll_votes_read on public.poll_votes for select using (
  user_id = (select auth.uid()) or exists (select 1 from public.posts p where p.id = poll_votes.post_id)
);
drop policy if exists comment_likes_read on public.comment_likes;
create policy comment_likes_read on public.comment_likes for select using (
  user_id = (select auth.uid()) or exists (select 1 from public.comments c where c.id = comment_likes.comment_id)
);
drop policy if exists story_comments_select on public.story_comments;
create policy story_comments_select on public.story_comments for select using (
  (kind = 'contrib' and exists (select 1 from public.stories s where s.id = story_comments.story_id))
  or user_id = auth.uid()
  or exists (select 1 from public.stories s where s.id = story_comments.story_id and s.user_id = auth.uid())
);
drop policy if exists nn_select on public.neighborhood_notices;
create policy nn_select on public.neighborhood_notices for select using (expires_at > now() and public.qinbly_can_see_author(user_id));
drop policy if exists marketplace_select on public.marketplace_listings;
create policy marketplace_select on public.marketplace_listings for select using (
  user_id = auth.uid() or (expires_at > now() and public.qinbly_can_see_author(user_id))
);
drop policy if exists notes_select on public.notes;
create policy notes_select on public.notes for select using (
  user_id = auth.uid() or (expires_at > now() and public.qinbly_can_see_author(user_id))
);
