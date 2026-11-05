ALTER TABLE posts DROP CONSTRAINT IF EXISTS posts_media_type_check;
ALTER TABLE posts ADD CONSTRAINT posts_media_type_check CHECK (media_type IN ('text','image','video','poll'));
ALTER TABLE posts ADD COLUMN IF NOT EXISTS poll_options JSONB;
CREATE TABLE IF NOT EXISTS poll_votes (
  post_id UUID NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
  user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  option_index INTEGER NOT NULL CHECK (option_index >= 0 AND option_index < 6),
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  PRIMARY KEY(post_id,user_id)
);
CREATE INDEX IF NOT EXISTS poll_votes_post_id_idx ON poll_votes(post_id);
