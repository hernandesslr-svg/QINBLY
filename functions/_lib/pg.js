// Camada compativel com o PostgREST do Supabase, em cima do D1.
// Mantem as mesmas consultas do app ("tabela?coluna=eq.valor&select=..."), as mesmas regras de acesso
// (equivalentes as politicas RLS do banco antigo) e os mesmos gatilhos (notificacoes, privacidade etc.).
import META from "./meta.js";
import { uuid, nowIso, Http } from "./core.js";

export class PgError extends Error {
  constructor(code, message, status = 400) {
    super(JSON.stringify({ code, message, details: null, hint: null }));
    this.code = code; this.pgMessage = message; this.status = status;
  }
}
const TABLES = META.tables;
const FKS = META.fks;
const IDENT = /^[a-z_][a-z0-9_]*$/;
const JSON_COLS = { posts: ["poll_options", "media_paths"], chat_polls: ["options"], marketplace_listings: ["photos"] };
const colType = (t, c) => TABLES[t]?.cols?.[c];

// ---------- valores SQL ----------
const lit = (v) => {
  if (v === null || v === undefined) return "NULL";
  if (typeof v === "boolean") return v ? "1" : "0";
  if (typeof v === "number") return Number.isFinite(v) ? String(v) : "NULL";
  return "'" + String(v).replace(/\0/g, "").replace(/'/g, "''") + "'";
};
const encodeValue = (table, col, v) => {
  if (v === null || v === undefined) return null;
  if (typeof v === "boolean") return v ? 1 : 0;
  if (typeof v === "object") return JSON.stringify(v);
  return v;
};
const decodeValue = (table, col, v, type) => {
  if (v === null || v === undefined) return v;
  if (type === "bool") return v === 1 || v === "1" || v === true;
  if (JSON_COLS[table]?.includes(col)) { try { return JSON.parse(v); } catch { return v; } }
  return v;
};
const outRow = (table, row) => {
  const o = {};
  for (const [k, v] of Object.entries(row)) o[k] = decodeValue(table, k, v, colType(table, k));
  return o;
};
const filterValue = (table, col, raw) => {
  const t = colType(table, col);
  if (t === "bool") return raw === "true" || raw === "1" ? 1 : 0;
  return raw;
};

// ---------- leitura da consulta ----------
const splitTop = (s, sep = ",") => {
  const out = []; let depth = 0, cur = "", q = false;
  for (const ch of s) {
    if (ch === '"') q = !q;
    if (!q) { if (ch === "(") depth++; else if (ch === ")") depth--; }
    if (ch === sep && depth === 0 && !q) { out.push(cur); cur = ""; } else cur += ch;
  }
  if (cur !== "") out.push(cur);
  return out;
};
const unq = (s) => { s = s.trim(); return s.length >= 2 && s[0] === '"' && s[s.length - 1] === '"' ? s.slice(1, -1) : s; };
const dec = (s) => { try { return decodeURIComponent(s); } catch { return s; } };

const parseCond = (key, value) => {
  // key = coluna (ou or/and), value = "op.valor" ou "(...)"
  if (key === "or" || key === "and") return { group: key, items: parseGroup(value) };
  return parseOp(key, value);
};
const parseOp = (col, value) => {
  let neg = false;
  let rest = value;
  if (rest.startsWith("not.")) { neg = true; rest = rest.slice(4); }
  const dot = rest.indexOf(".");
  const op = dot < 0 ? rest : rest.slice(0, dot);
  const val = dot < 0 ? "" : rest.slice(dot + 1);
  return { col, op, val, neg };
};
const parseGroup = (value) => {
  const inner = value.trim().replace(/^\(/, "").replace(/\)$/, "");
  return splitTop(inner).map((part) => {
    const m = part.match(/^(and|or)\((.*)\)$/s);
    if (m) return { group: m[1], items: parseGroup("(" + m[2] + ")") };
    const i = part.indexOf(".");
    return parseOp(part.slice(0, i), part.slice(i + 1));
  });
};

export const parseRequestPath = (pathAndQuery) => {
  const qi = pathAndQuery.indexOf("?");
  const table = qi < 0 ? pathAndQuery : pathAndQuery.slice(0, qi);
  const qs = qi < 0 ? "" : pathAndQuery.slice(qi + 1);
  const q = { table: table.replace(/^\/+/, ""), select: "*", order: [], limit: null, offset: 0, onConflict: null, conds: [] };
  for (const part of qs.split("&")) {
    if (!part) continue;
    const eq = part.indexOf("=");
    const key = dec(eq < 0 ? part : part.slice(0, eq));
    const val = dec(eq < 0 ? "" : part.slice(eq + 1));
    if (key === "select") q.select = val;
    else if (key === "order") q.order = val.split(",").filter(Boolean).map((o) => { const [c, d] = o.split("."); return { col: c, desc: d === "desc" }; });
    else if (key === "limit") q.limit = Math.max(0, parseInt(val, 10) || 0);
    else if (key === "offset") q.offset = Math.max(0, parseInt(val, 10) || 0);
    else if (key === "on_conflict") q.onConflict = val.split(",").map((s) => s.trim()).filter(Boolean);
    else if (key === "columns") { /* ignorado */ }
    else q.conds.push(parseCond(key, val));
  }
  return q;
};

const parseSelect = (sel) => {
  const items = splitTop(sel).map((s) => s.trim()).filter(Boolean);
  const cols = []; const embeds = [];
  for (const it of items) {
    const m = it.match(/^([a-z_][a-z0-9_]*)((?:![a-z0-9_]+)*)\((.*)\)$/s);
    if (m) {
      const hints = m[2].split("!").filter(Boolean);
      const inner = hints.includes("inner");
      const hint = hints.find((h) => h !== "inner") || null;
      embeds.push({ name: m[1], hint, inner, cols: m[3] });
    } else cols.push(it);
  }
  return { cols, embeds };
};

// ---------- relacoes (embeds) ----------
const relationFor = (main, e) => {
  if (e.hint && FKS[e.hint]) {
    const fk = FKS[e.hint];
    if (fk.table === main && fk.target === e.name) return { kind: "one", local: fk.col, foreign: TABLES[e.name].pk[0] || "id" };
    if (fk.target === main && fk.table === e.name) return { kind: "many", local: TABLES[main].pk[0] || "id", foreign: fk.col };
  }
  const direct = Object.values(FKS).find((fk) => fk.table === main && fk.target === e.name);
  if (direct) return { kind: "one", local: direct.col, foreign: TABLES[e.name].pk[0] || "id" };
  const reverse = Object.values(FKS).find((fk) => fk.target === main && fk.table === e.name);
  if (reverse) return { kind: "many", local: TABLES[main].pk[0] || "id", foreign: reverse.col };
  throw new PgError("PGRST200", `Could not find a relationship between '${main}' and '${e.name}'`, 400);
};

// ---------- politicas de acesso (equivalem as RLS do banco antigo) ----------
const policies = (U, NOW) => {
  const canSee = (col) => `(${col} = ${U} OR NOT COALESCE((SELECT is_private FROM profiles WHERE id = ${col}),0) OR EXISTS (SELECT 1 FROM follows f WHERE f.follower_id = ${U} AND f.following_id = ${col}))`;
  const postPublic = (col) => `EXISTS (SELECT 1 FROM posts p WHERE p.id = ${col} AND p.visibility = 'public')`;
  const postAny = (col) => `EXISTS (SELECT 1 FROM posts p WHERE p.id = ${col})`;
  const storyOwner = (col) => `EXISTS (SELECT 1 FROM stories s WHERE s.id = ${col} AND s.user_id = ${U})`;
  const msgMine = (col) => `EXISTS (SELECT 1 FROM messages m WHERE m.id = ${col} AND (m.sender_id = ${U} OR m.recipient_id = ${U}))`;
  const comPublicOrOwner = (col) => `EXISTS (SELECT 1 FROM communities c WHERE c.id = ${col} AND (c.visibility = 'public' OR c.owner_id = ${U}))`;
  const isMember = (col) => `EXISTS (SELECT 1 FROM community_members m WHERE m.community_id = ${col} AND m.user_id = ${U})`;
  const isOwner = (col) => `EXISTS (SELECT 1 FROM communities c WHERE c.id = ${col} AND c.owner_id = ${U})`;
  const own = (c) => `${c} = ${U}`;
  const ALL = "1=1";
  // select: SQL que filtra linhas; auth: exige login; insert: expressao de checagem com v(col); update/delete: SQL "using"
  return {
    challenge_posts: { auth: true, select: postPublic("challenge_posts.post_id"), insert: (v) => `${v("user_id")} = ${U} AND EXISTS (SELECT 1 FROM posts p WHERE p.id = ${v("post_id")} AND p.user_id = ${U} AND p.visibility='public') AND EXISTS (SELECT 1 FROM weekly_challenges c WHERE c.id = ${v("challenge_id")} AND ${lit(NOW)} >= c.starts_at AND ${lit(NOW)} <= c.ends_at)` },
    chat_poll_votes: { auth: true, select: ALL, insert: (v) => `${v("user_id")} = ${U}`, update: own("user_id") },
    chat_polls: { auth: true, select: ALL, insert: (v) => `${v("creator_id")} = ${U}` },
    comment_likes: { select: `(user_id = ${U} OR EXISTS (SELECT 1 FROM comments c WHERE c.id = comment_likes.comment_id))`, insert: (v) => `${v("user_id")} = ${U}`, delete: own("user_id") },
    comments: { select: postPublic("comments.post_id"), insert: (v) => `${v("user_id")} = ${U} AND ${postPublic(v("post_id"))}`, update: own("user_id"), delete: own("user_id") },
    communities: { auth: true, select: `(visibility = 'public' OR owner_id = ${U} OR ${isMember("communities.id")})`, insert: (v) => `${v("owner_id")} = ${U}`, update: own("owner_id"), delete: own("owner_id") },
    community_members: { auth: true, select: `(${comPublicOrOwner("community_members.community_id")} OR user_id = ${U})`, insert: (v) => `${v("user_id")} = ${U} AND ${v("role")} = 'member' AND EXISTS (SELECT 1 FROM communities c WHERE c.id = ${v("community_id")} AND c.visibility = 'public')`, delete: own("user_id") },
    community_messages: { auth: true, select: `(${comPublicOrOwner("community_messages.community_id")} OR ${isMember("community_messages.community_id")})`, insert: (v) => `${v("user_id")} = ${U} AND (${isMember(v("community_id"))} OR ${isOwner(v("community_id"))})`, delete: `(user_id = ${U} OR ${isOwner("community_messages.community_id")})` },
    community_posts: { auth: true, select: `(${comPublicOrOwner("community_posts.community_id")} AND ${postPublic("community_posts.post_id")})`, insert: (v) => `EXISTS (SELECT 1 FROM posts p WHERE p.id = ${v("post_id")} AND p.user_id = ${U} AND p.visibility='public') AND (${isOwner(v("community_id"))} OR (EXISTS (SELECT 1 FROM communities c WHERE c.id = ${v("community_id")} AND c.visibility='public') AND ${isMember(v("community_id"))}))` },
    content_reports: { auth: true, select: own("reporter_id"), insert: (v) => `${v("reporter_id")} = ${U}` },
    daily_questions: { auth: true, select: ALL },
    follow_requests: { select: `(requester_id = ${U} OR target_id = ${U})`, delete: `(requester_id = ${U} OR target_id = ${U})` },
    follows: { select: `(follower_id = ${U} OR following_id = ${U} OR (${canSee("follows.follower_id")} AND ${canSee("follows.following_id")}))`, insert: (v) => `${v("follower_id")} = ${U} AND ${v("follower_id")} <> ${v("following_id")}`, delete: own("follower_id") },
    live_comments: { select: ALL, insert: (v) => `${v("user_id")} = ${U}`, delete: `(user_id = ${U} OR lower((SELECT username FROM profiles WHERE id = ${U})) = live_host)` },
    marketplace_listings: { select: `(user_id = ${U} OR (expires_at > ${lit(NOW)} AND ${canSee("marketplace_listings.user_id")}))`, insert: (v) => `${v("user_id")} = ${U}`, update: own("user_id"), delete: own("user_id") },
    marketplace_saves: { auth: true, select: own("user_id"), insert: (v) => `${v("user_id")} = ${U}`, delete: own("user_id") },
    message_post_attachments: { auth: true, select: msgMine("message_post_attachments.message_id"), insert: (v) => `EXISTS (SELECT 1 FROM messages m WHERE m.id = ${v("message_id")} AND m.sender_id = ${U}) AND ${postPublic(v("post_id"))}` },
    message_reactions: { auth: true, select: msgMine("message_reactions.message_id"), insert: (v) => `${v("user_id")} = ${U} AND ${msgMine(v("message_id"))}`, update: own("user_id"), delete: own("user_id") },
    messages: { auth: true, select: `(sender_id = ${U} OR recipient_id = ${U})`, insert: (v) => `${v("sender_id")} = ${U}`, update: own("recipient_id"), delete: own("sender_id") },
    neighborhood_comments: { auth: true, select: `EXISTS (SELECT 1 FROM neighborhood_notices n WHERE n.id = neighborhood_comments.notice_id)`, insert: (v) => `${v("user_id")} = ${U} AND EXISTS (SELECT 1 FROM neighborhood_notices n WHERE n.id = ${v("notice_id")})`, update: own("user_id"), delete: `(user_id = ${U} OR EXISTS (SELECT 1 FROM neighborhood_notices n WHERE n.id = neighborhood_comments.notice_id AND n.user_id = ${U}))` },
    neighborhood_notices: { select: `(expires_at > ${lit(NOW)} AND ${canSee("neighborhood_notices.user_id")})`, insert: (v) => `${v("user_id")} = ${U}`, delete: own("user_id") },
    notes: { select: `(user_id = ${U} OR (expires_at > ${lit(NOW)} AND ${canSee("notes.user_id")}))`, insert: (v) => `${v("user_id")} = ${U}`, update: own("user_id"), delete: own("user_id") },
    notifications: { auth: true, select: own("recipient_id"), update: own("recipient_id") },
    poll_votes: { select: `(user_id = ${U} OR ${postAny("poll_votes.post_id")})`, insert: (v) => `${v("user_id")} = ${U} AND EXISTS (SELECT 1 FROM posts p WHERE p.id = ${v("post_id")} AND p.media_type='poll' AND p.poll_expires_at > ${lit(NOW)} AND 0 <= ${v("option_index")} AND ${v("option_index")} < json_array_length(p.poll_options))` },
    post_likes: { select: postPublic("post_likes.post_id"), insert: (v) => `${v("user_id")} = ${U} AND ${postPublic(v("post_id"))}`, delete: own("user_id") },
    post_reposts: { auth: true, select: postPublic("post_reposts.original_post_id"), insert: (v) => `${v("user_id")} = ${U} AND ${postPublic(v("original_post_id"))}`, delete: own("user_id") },
    post_shares: { select: `(user_id = ${U} OR ${postAny("post_shares.post_id")})`, insert: (v) => `${v("user_id")} = ${U} AND ${postPublic(v("post_id"))}` },
    post_tags: { select: `(tagged_user_id = ${U} OR tagged_by = ${U} OR ${postAny("post_tags.post_id")})`, insert: (v) => `${v("tagged_by")} = ${U} AND EXISTS (SELECT 1 FROM posts p WHERE p.id = ${v("post_id")} AND p.user_id = ${U})`, delete: `(tagged_by = ${U} OR tagged_user_id = ${U})` },
    post_views: { auth: true, select: `EXISTS (SELECT 1 FROM posts p WHERE p.id = post_views.post_id AND p.user_id = ${U})`, insert: (v) => `${v("viewer_id")} = ${U}` },
    posts: { select: `(user_id = ${U} OR (visibility = 'public' AND ${canSee("posts.user_id")} AND (publish_at IS NULL OR publish_at <= ${lit(NOW)})))`, insert: (v) => `${v("user_id")} = ${U}`, update: own("user_id"), delete: own("user_id") },
    private_user_data: { auth: true, select: own("user_id"), insert: (v) => `${v("user_id")} = ${U}`, update: own("user_id") },
    profiles: { select: ALL, update: own("id") },
    saved_posts: { auth: true, select: own("user_id"), insert: (v) => `${v("user_id")} = ${U} AND EXISTS (SELECT 1 FROM posts p WHERE p.id = ${v("post_id")} AND (p.visibility='public' OR p.user_id = ${U}))`, delete: own("user_id") },
    sticker_pack_items: { auth: true, select: ALL, insert: (v) => `EXISTS (SELECT 1 FROM sticker_packs p WHERE p.id = ${v("pack_id")} AND p.owner_id = ${U})` },
    sticker_packs: { auth: true, select: ALL, insert: (v) => `${v("owner_id")} = ${U}`, delete: own("owner_id") },
    stories: {
      select: `(user_id = ${U} OR (expires_at > ${lit(NOW)} AND ${canSee("stories.user_id")} AND (audience = 'all' OR (audience = 'followers' AND EXISTS (SELECT 1 FROM follows f WHERE f.follower_id = ${U} AND f.following_id = stories.user_id)) OR (audience = 'friends' AND EXISTS (SELECT 1 FROM follows f WHERE f.follower_id = ${U} AND f.following_id = stories.user_id) AND EXISTS (SELECT 1 FROM follows g WHERE g.follower_id = stories.user_id AND g.following_id = ${U})) OR (audience = 'near' AND city <> '' AND EXISTS (SELECT 1 FROM profiles p WHERE p.id = ${U} AND lower(p.city) = lower(stories.city))))))`,
      insert: (v) => `${v("user_id")} = ${U}`, update: own("user_id"), delete: own("user_id"),
    },
    story_comments: { select: `((kind = 'contrib' AND EXISTS (SELECT 1 FROM stories s WHERE s.id = story_comments.story_id)) OR user_id = ${U} OR ${storyOwner("story_comments.story_id")})`, insert: (v) => `${v("user_id")} = ${U} AND EXISTS (SELECT 1 FROM stories s WHERE s.id = ${v("story_id")} AND s.expires_at > ${lit(NOW)})`, delete: `(user_id = ${U} OR ${storyOwner("story_comments.story_id")})` },
    story_contributions: { auth: true, select: `(EXISTS (SELECT 1 FROM stories s WHERE s.id = story_contributions.story_id AND (s.expires_at > ${lit(NOW)} OR s.user_id = ${U})) OR user_id = ${U})`, insert: (v) => `${v("user_id")} = ${U} AND EXISTS (SELECT 1 FROM stories s WHERE s.id = ${v("story_id")} AND s.expires_at > ${lit(NOW)} AND s.caption LIKE 'qinbly:collab:%')`, delete: `(user_id = ${U} OR ${storyOwner("story_contributions.story_id")})` },
    story_highlights: { select: `(user_id = ${U} OR ${canSee("story_highlights.user_id")})`, insert: (v) => `${v("user_id")} = ${U}`, delete: own("user_id") },
    story_likes: { auth: true, select: `(user_id = ${U} OR ${storyOwner("story_likes.story_id")})`, insert: (v) => `${v("user_id")} = ${U} AND EXISTS (SELECT 1 FROM stories s WHERE s.id = ${v("story_id")} AND s.expires_at > ${lit(NOW)})`, delete: own("user_id") },
    story_mutes: { auth: true, select: own("user_id"), insert: (v) => `${v("user_id")} = ${U}`, update: own("user_id"), delete: own("user_id") },
    story_views: { auth: true, select: `(viewer_id = ${U} OR ${storyOwner("story_views.story_id")})`, insert: (v) => `${v("viewer_id")} = ${U}`, update: own("viewer_id") },
    time_capsules: { auth: true, select: `(from_user = ${U} OR (to_user = ${U} AND unlock_at <= ${lit(NOW)}))`, insert: (v) => `${v("from_user")} = ${U} AND ${v("unlock_at")} > ${lit(NOW)}`, update: `((to_user = ${U} AND unlock_at <= ${lit(NOW)}) OR (from_user = ${U} AND unlock_at <= ${lit(NOW)}))` },
    user_blocks: { auth: true, select: own("blocker_id"), insert: (v) => `${v("blocker_id")} = ${U}`, delete: own("blocker_id") },
    user_documents: { auth: true, select: own("user_id"), insert: (v) => `${v("user_id")} = ${U} AND substr(${v("object_path")}, 1, 37) = ${U} || '/'`, update: own("user_id"), delete: own("user_id") },
    user_presence: { auth: true, select: ALL, insert: (v) => `${v("user_id")} = ${U}`, update: own("user_id"), delete: own("user_id") },
    user_settings: { auth: true, select: own("user_id"), insert: (v) => `${v("user_id")} = ${U}`, update: own("user_id") },
    user_stickers: { auth: true, select: own("owner_id"), insert: (v) => `${v("owner_id")} = ${U}`, delete: own("owner_id") },
    verification_requests: { auth: true, select: own("user_id"), insert: (v) => `${v("user_id")} = ${U} AND ${v("status")} = 'pending'` },
    weekly_challenges: { auth: true, select: ALL },
  };
};
const UID_DEFAULT = { chat_polls: "creator_id", chat_poll_votes: "user_id", post_tags: "tagged_by", user_stickers: "owner_id", live_comments: "user_id", stories: "user_id", story_views: "viewer_id", notes: "user_id", message_reactions: "user_id", neighborhood_notices: "user_id", time_capsules: "from_user", sticker_packs: "owner_id", story_contributions: "user_id", marketplace_listings: "user_id", marketplace_saves: "user_id", neighborhood_comments: "user_id", story_mutes: "user_id" };
const OWNER_COL = { posts: "user_id", comments: "user_id", messages: "sender_id", profiles: "id", communities: "owner_id", marketplace_listings: "user_id", notes: "user_id", stories: "user_id" };

// ---------- cascata ao apagar (o banco antigo usava ON DELETE CASCADE) ----------
const CASCADE = {
  posts: [["post_likes", "post_id"], ["comments", "post_id"], ["saved_posts", "post_id"], ["post_reposts", "original_post_id"], ["poll_votes", "post_id"], ["post_tags", "post_id"], ["post_views", "post_id"], ["post_shares", "post_id"], ["community_posts", "post_id"], ["challenge_posts", "post_id"], ["message_post_attachments", "post_id"]],
  comments: [["comment_likes", "comment_id"]],
  stories: [["story_views", "story_id"], ["story_likes", "story_id"], ["story_comments", "story_id"], ["story_contributions", "story_id"]],
  communities: [["community_members", "community_id"], ["community_messages", "community_id"], ["community_posts", "community_id"]],
  messages: [["message_reactions", "message_id"], ["message_post_attachments", "message_id"]],
  marketplace_listings: [["marketplace_saves", "listing_id"]],
  sticker_packs: [["sticker_pack_items", "pack_id"]],
  neighborhood_notices: [["neighborhood_comments", "notice_id"]],
  chat_polls: [["chat_poll_votes", "poll_id"]],
};

const PROFILE_CHECKS = (row) => {
  if (row.username !== undefined && !/^[a-zA-Z0-9_.]{3,40}$/.test(String(row.username))) throw new PgError("23514", 'new row for relation "profiles" violates check constraint "profiles_username_check"');
  if (row.display_name !== undefined && String(row.display_name).length > 80) throw new PgError("23514", 'new row for relation "profiles" violates check constraint "profiles_display_name_check"');
  if (row.bio !== undefined && String(row.bio).length > 500) throw new PgError("23514", 'new row for relation "profiles" violates check constraint "profiles_bio_check"');
  if (row.who_can_message !== undefined && !["all", "following", "nobody"].includes(row.who_can_message)) throw new PgError("23514", "invalid who_can_message");
  if (row.who_can_comment !== undefined && !["all", "followers", "nobody"].includes(row.who_can_comment)) throw new PgError("23514", "invalid who_can_comment");
  if (row.profile_frame !== undefined && !(row.profile_frame === -1 || (row.profile_frame >= 0 && row.profile_frame <= 24))) throw new PgError("23514", "invalid profile_frame");
};

export const makePg = (env, uid) => {
  const DB = env.DB;
  const U = uid ? lit(uid) : "NULL";
  const NOW = nowIso();
  const POL = policies(U, NOW);
  const all = async (sql) => (await DB.prepare(sql).all()).results || [];
  const first = async (sql) => (await DB.prepare(sql).first()) || null;
  const wrapDb = async (fn) => {
    try { return await fn(); } catch (e) {
      if (e instanceof PgError || e instanceof Http) throw e;
      const msg = String(e?.message || e);
      if (/UNIQUE constraint failed|PRIMARY KEY/i.test(msg)) throw new PgError("23505", `duplicate key value violates unique constraint (${msg})`, 409);
      if (/NOT NULL constraint failed/i.test(msg)) throw new PgError("23502", `null value in column violates not-null constraint (${msg})`, 400);
      throw new PgError("XX000", msg, 500);
    }
  };

  // ----- filtros -> SQL -----
  const condSql = (table, c) => {
    if (c.group) {
      const parts = c.items.map((i) => condSql(table, i)).filter(Boolean);
      if (!parts.length) return "1=1";
      return "(" + parts.join(c.group === "or" ? " OR " : " AND ") + ")";
    }
    const col = c.col;
    if (!IDENT.test(col)) throw new PgError("PGRST100", "coluna inválida: " + col);
    if (!TABLES[table]?.cols?.[col]) throw new PgError("42703", `column ${table}.${col} does not exist`);
    const q = `${table}.${col}`;
    let sql;
    switch (c.op) {
      case "eq": sql = `${q} = ${lit(filterValue(table, col, c.val))}`; break;
      case "neq": sql = `${q} <> ${lit(filterValue(table, col, c.val))}`; break;
      case "gt": sql = `${q} > ${lit(c.val)}`; break;
      case "gte": sql = `${q} >= ${lit(c.val)}`; break;
      case "lt": sql = `${q} < ${lit(c.val)}`; break;
      case "lte": sql = `${q} <= ${lit(c.val)}`; break;
      case "like": case "ilike": {
        const pat = c.val.replace(/\\/g, "\\\\").replace(/%/g, "\\%").replace(/_/g, "\\_").replace(/\*/g, "%");
        sql = `lower(${q}) LIKE lower(${lit(pat)}) ESCAPE '\\'`; break;
      }
      case "in": {
        const inner = c.val.trim().replace(/^\(/, "").replace(/\)$/, "");
        const vals = splitTop(inner).map((s) => unq(s)).filter((s) => s !== "");
        sql = vals.length ? `${q} IN (${vals.map((s) => lit(filterValue(table, col, s))).join(",")})` : "0";
        break;
      }
      case "is": {
        const v = c.val.toLowerCase();
        sql = v === "null" ? `${q} IS NULL` : v === "true" ? `${q} = 1` : v === "false" ? `${q} = 0` : `${q} IS NULL`;
        break;
      }
      default: throw new PgError("PGRST100", "operador não suportado: " + c.op);
    }
    return c.neg ? `NOT (${sql})` : sql;
  };

  const whereFor = (table, conds, extra = []) => {
    const parts = [];
    const embedConds = {};
    for (const c of conds) {
      if (!c.group && c.col.includes(".")) { const [emb, col] = c.col.split("."); (embedConds[emb] ||= []).push({ ...c, col }); continue; }
      parts.push(condSql(table, c));
    }
    for (const [emb, list] of Object.entries(embedConds)) {
      const rel = relationFor(table, { name: emb, hint: null });
      const sub = list.map((c) => condSql(emb, c)).join(" AND ");
      parts.push(`EXISTS (SELECT 1 FROM ${emb} WHERE ${emb}.${rel.foreign} = ${table}.${rel.local} AND ${sub})`);
    }
    return [...parts, ...extra].filter(Boolean).join(" AND ");
  };
  const readPolicy = (table) => {
    const p = POL[table];
    if (!p || !p.select) throw new PgError("42501", `permission denied for table ${table}`, 403);
    if (p.auth && !uid) return "0"; // sem login: o banco antigo devolvia lista vazia (RLS), nao erro
    return p.select;
  };

  // ----- projecao (colunas + embeds) -----
  const project = async (table, rows, selectSpec) => {
    const { cols, embeds } = parseSelect(selectSpec || "*");
    const wantAll = cols.length === 0 || cols.includes("*");
    const out = rows.map((r) => {
      const o = outRow(table, r);
      if (wantAll) return o;
      const only = {};
      for (const c of cols) if (c in o) only[c] = o[c];
      return only;
    });
    const drop = new Set();
    for (const e of embeds) {
      const rel = relationFor(table, e);
      const keys = [...new Set(rows.map((r) => r[rel.local]).filter((v) => v !== null && v !== undefined))];
      let found = [];
      if (keys.length) {
        const where = [`${e.name}.${rel.foreign} IN (${keys.map(lit).join(",")})`, readPolicySql(e.name)].filter(Boolean).join(" AND ");
        found = await all(`SELECT * FROM ${e.name} WHERE ${where}`);
      }
      const projected = await project(e.name, found, e.cols.trim() || "*");
      const byKey = new Map();
      projected.forEach((p, i) => {
        const k = found[i][rel.foreign];
        if (rel.kind === "one") byKey.set(k, p);
        else { if (!byKey.has(k)) byKey.set(k, []); byKey.get(k).push(p); }
      });
      out.forEach((o, i) => {
        const v = byKey.get(rows[i][rel.local]);
        o[e.name] = rel.kind === "one" ? (v ?? null) : (v ?? []);
        if (e.inner && rel.kind === "one" && !v) drop.add(i);
      });
    }
    return drop.size ? out.filter((_, i) => !drop.has(i)) : out;
  };
  const readPolicySql = (table) => { try { return readPolicy(table); } catch { return "0"; } };

  // ----- SELECT -----
  const select = async (q) => {
    const table = q.table;
    if (!TABLES[table]) throw new PgError("42P01", `relation "public.${table}" does not exist`, 404);
    const pol = readPolicy(table);
    const where = whereFor(table, q.conds, [pol]);
    const order = q.order.filter((o) => TABLES[table].cols[o.col]).map((o) => `${table}.${o.col} ${o.desc ? "DESC" : "ASC"}`).join(", ");
    let sql = `SELECT * FROM ${table}` + (where ? ` WHERE ${where}` : "") + (order ? ` ORDER BY ${order}` : "");
    if (q.limit !== null) sql += ` LIMIT ${q.limit}`;
    if (q.offset) sql += (q.limit === null ? " LIMIT -1" : "") + ` OFFSET ${q.offset}`;
    const rows = await wrapDb(() => all(sql));
    return project(table, rows, q.select);
  };

  // ----- gatilhos -----
  const notify = (recipient, actor, kind, entity) => recipient && actor && recipient !== actor
    ? DB.prepare("INSERT INTO notifications (id, recipient_id, actor_id, kind, entity_id) VALUES (?, ?, ?, ?, ?)").bind(uuid(), recipient, actor, kind, entity)
    : null;
  const beforeInsert = async (table, row, ctx) => {
    if (table === "profiles") { delete row.is_verified; PROFILE_CHECKS(row); }
    if (table === "follows" && !ctx.allowFollow) {
      const t = await first(`SELECT is_private FROM profiles WHERE id = ${lit(row.following_id)}`);
      if (t?.is_private) throw new PgError("42501", "Esta conta é privada. Envie um pedido de seguir.", 403);
    }
    if (table === "messages") {
      const t = await first(`SELECT who_can_message AS mode FROM profiles WHERE id = ${lit(row.recipient_id)}`);
      const mode = t?.mode || "all";
      if (mode !== "all") {
        const replied = await first(`SELECT 1 AS x FROM messages WHERE sender_id = ${lit(row.recipient_id)} AND recipient_id = ${lit(row.sender_id)} LIMIT 1`);
        const okFollow = mode === "following" && await first(`SELECT 1 AS x FROM follows WHERE follower_id = ${lit(row.recipient_id)} AND following_id = ${lit(row.sender_id)}`);
        if (!replied && !okFollow) throw new PgError("42501", "Esta pessoa não está recebendo mensagens suas.", 403);
      }
    }
    if (table === "comments") {
      const t = await first(`SELECT p.user_id AS owner, pr.who_can_comment AS mode FROM posts p JOIN profiles pr ON pr.id = p.user_id WHERE p.id = ${lit(row.post_id)}`);
      if (t && t.owner !== row.user_id && (t.mode || "all") !== "all") {
        const ok = t.mode === "followers" && await first(`SELECT 1 AS x FROM follows WHERE follower_id = ${lit(row.user_id)} AND following_id = ${lit(t.owner)}`);
        if (!ok) throw new PgError("42501", "Os comentários desta publicação estão limitados pelo dono.", 403);
      }
    }
    if (table === "posts") {
      if (row.media_type === "poll") { if (!row.poll_expires_at) row.poll_expires_at = new Date(Date.parse(row.created_at || NOW) + 3 * 3600_000).toISOString(); }
      else row.poll_expires_at = null;
    }
  };
  const afterInsert = async (table, row) => {
    const stmts = [];
    if (table === "follows") stmts.push(notify(row.following_id, row.follower_id, "follow", row.follower_id));
    else if (table === "post_likes") { const p = await first(`SELECT user_id FROM posts WHERE id = ${lit(row.post_id)}`); stmts.push(notify(p?.user_id, row.user_id, "like", row.post_id)); }
    else if (table === "comments") { const p = await first(`SELECT user_id FROM posts WHERE id = ${lit(row.post_id)}`); stmts.push(notify(p?.user_id, row.user_id, "comment", row.post_id)); }
    else if (table === "messages") stmts.push(notify(row.recipient_id, row.sender_id, "message", row.id));
    else if (table === "post_tags") stmts.push(notify(row.tagged_user_id, row.tagged_by, "tag", row.post_id));
    const real = stmts.filter(Boolean);
    if (real.length) await DB.batch(real);
  };

  // ----- INSERT / upsert -----
  const insert = async (q, rows, prefer, ctx = {}) => {
    const table = q.table;
    if (!TABLES[table]) throw new PgError("42P01", `relation "public.${table}" does not exist`, 404);
    const pol = POL[table];
    if (!pol?.insert) throw new PgError("42501", `new row violates row-level security policy for table "${table}"`, 403);
    if (pol.auth && !uid) throw new PgError("42501", "permission denied", 401);
    const ignore = /resolution=ignore-duplicates/.test(prefer);
    const merge = /resolution=merge-duplicates/.test(prefer);
    const wantRows = /return=representation/.test(prefer);
    const stmts = [], metaRows = [];
    for (const raw of rows) {
      const row = { ...raw };
      for (const k of Object.keys(row)) if (!TABLES[table].cols[k]) throw new PgError("PGRST204", `Could not find the '${k}' column of '${table}' in the schema cache`, 400);
      if (UID_DEFAULT[table] && uid && (row[UID_DEFAULT[table]] === undefined || row[UID_DEFAULT[table]] === null)) row[UID_DEFAULT[table]] = uid;
      if (TABLES[table].pk.length === 1 && TABLES[table].pk[0] === "id" && !row.id) row.id = uuid();
      if (table === "profiles" && !row.username) row.username = `user_${uuid().replaceAll("-", "").slice(0, 8)}`;
      if (table === "posts" && row.media_paths === undefined) row.media_paths = [];
      if (table === "marketplace_listings") { if (row.photos === undefined) row.photos = []; if (!row.expires_at) row.expires_at = new Date(Date.now() + 7 * 86400_000).toISOString(); }
      if (table === "stories" && !row.expires_at) row.expires_at = new Date(Date.now() + 24 * 3600_000).toISOString();
      if (table === "notes" && !row.expires_at) row.expires_at = new Date(Date.now() + 24 * 3600_000).toISOString();
      if (table === "neighborhood_notices" && !row.expires_at) row.expires_at = new Date(Date.now() + 48 * 3600_000).toISOString();
      if (table === "post_views" && !row.day) row.day = NOW.slice(0, 10);
      if (!row.created_at && TABLES[table].cols.created_at && ["posts", "comments", "messages"].includes(table)) row.created_at = NOW;
      await beforeInsert(table, row, ctx);
      // checagem da politica de insercao (WITH CHECK)
      const v = (c) => lit(encodeValue(table, c, row[c]));
      const check = pol.insert(v);
      const ok = await first(`SELECT CASE WHEN (${check}) THEN 1 ELSE 0 END AS ok`);
      if (!ok?.ok) throw new PgError("42501", `new row violates row-level security policy for table "${table}"`, 403);
      const cols = Object.keys(row).filter((c) => row[c] !== undefined);
      const vals = cols.map((c) => lit(encodeValue(table, c, row[c])));
      let conflict = "";
      if (merge) {
        const target = (q.onConflict && q.onConflict.length ? q.onConflict : TABLES[table].pk);
        const upd = cols.filter((c) => !target.includes(c));
        conflict = upd.length ? ` ON CONFLICT (${target.join(",")}) DO UPDATE SET ${upd.map((c) => `${c} = excluded.${c}`).join(", ")}` : ` ON CONFLICT (${target.join(",")}) DO NOTHING`;
      } else if (ignore) conflict = " ON CONFLICT DO NOTHING";
      stmts.push(DB.prepare(`INSERT INTO ${table} (${cols.join(",")}) VALUES (${vals.join(",")})${conflict} RETURNING *`));
      metaRows.push(row);
    }
    if (!stmts.length) return [];
    const results = await wrapDb(() => DB.batch(stmts));
    const inserted = [];
    results.forEach((r, i) => { if ((r.results || [])[0]) inserted.push(r.results[0]); });
    // gatilhos (notificacoes) so para linhas realmente criadas
    for (let i = 0; i < results.length; i++) if ((results[i].results || [])[0]) await afterInsert(table, { ...metaRows[i], ...results[i].results[0] });
    if (!wantRows) return null;
    return project(table, inserted, q.select);
  };

  // ----- UPDATE -----
  const update = async (q, patchRaw, prefer) => {
    const table = q.table;
    if (!TABLES[table]) throw new PgError("42P01", `relation "public.${table}" does not exist`, 404);
    const pol = POL[table];
    if (!pol?.update || (pol.auth && !uid)) return /return=representation/.test(prefer) ? [] : null;
    const patch = { ...patchRaw };
    for (const k of Object.keys(patch)) if (!TABLES[table].cols[k]) throw new PgError("PGRST204", `Could not find the '${k}' column of '${table}' in the schema cache`, 400);
    const ownerCol = OWNER_COL[table];
    if (ownerCol && patch[ownerCol] !== undefined && patch[ownerCol] !== uid && table !== "messages") throw new PgError("42501", `new row violates row-level security policy for table "${table}"`, 403);
    if (table === "profiles") {
      delete patch.is_verified; PROFILE_CHECKS(patch);
      if (patch.cover_path) patch.avatar_path = patch.cover_path;
      patch.updated_at = NOW;
    }
    if (table === "posts") patch.updated_at = NOW;
    if (table === "private_user_data") patch.updated_at = NOW;
    const where = whereFor(table, q.conds, [pol.update]);
    const before = table === "profiles" && patch.is_private === false ? await all(`SELECT id, is_private FROM profiles WHERE ${where}`) : [];
    const sets = Object.keys(patch).map((c) => `${c} = ${lit(encodeValue(table, c, patch[c]))}`);
    if (!sets.length) return /return=representation/.test(prefer) ? [] : null;
    const rows = await wrapDb(() => all(`UPDATE ${table} SET ${sets.join(", ")}${where ? ` WHERE ${where}` : ""} RETURNING *`));
    // conta passou de privada para aberta: aprova os pedidos pendentes (gatilho qinbly_open_account)
    for (const b of before) if (b.is_private) await openAccount(b.id);
    return /return=representation/.test(prefer) ? project(table, rows, q.select) : null;
  };
  const openAccount = async (id) => {
    await DB.batch([
      DB.prepare(`INSERT OR IGNORE INTO follows (follower_id, following_id) SELECT requester_id, target_id FROM follow_requests WHERE target_id = ?`).bind(id),
      DB.prepare(`DELETE FROM follow_requests WHERE target_id = ?`).bind(id),
      DB.prepare(`DELETE FROM notifications WHERE recipient_id = ? AND kind = 'follow_request'`).bind(id),
    ]);
  };

  // ----- DELETE -----
  const remove = async (q, prefer) => {
    const table = q.table;
    if (!TABLES[table]) throw new PgError("42P01", `relation "public.${table}" does not exist`, 404);
    const pol = POL[table];
    if (!pol?.delete || (pol.auth && !uid)) return /return=representation/.test(prefer) ? [] : null;
    const where = whereFor(table, q.conds, [pol.delete]);
    const casc = CASCADE[table];
    if (casc) {
      const pk = TABLES[table].pk[0];
      const ids = (await all(`SELECT ${pk} FROM ${table}${where ? ` WHERE ${where}` : ""}`)).map((r) => r[pk]);
      if (ids.length) await DB.batch(casc.map(([t, c]) => DB.prepare(`DELETE FROM ${t} WHERE ${c} IN (${ids.map(lit).join(",")})`)));
    }
    const rows = await wrapDb(() => all(`DELETE FROM ${table}${where ? ` WHERE ${where}` : ""} RETURNING *`));
    return /return=representation/.test(prefer) ? project(table, rows, q.select) : null;
  };

  // ----- chamadas no formato do PostgREST -----
  const parseBody = (body) => { if (body === undefined || body === null || body === "") return []; const j = typeof body === "string" ? JSON.parse(body) : body; return Array.isArray(j) ? j : [j]; };
  const rest = async (pathAndQuery, init = {}) => {
    const method = (init.method || "GET").toUpperCase();
    const prefer = (init.headers && (init.headers.Prefer || init.headers.prefer)) || (init.prefer || "");
    if (pathAndQuery.startsWith("rpc/")) return rpc(pathAndQuery.slice(4).split("?")[0], init.body ? JSON.parse(init.body) : {});
    const q = parseRequestPath(pathAndQuery);
    if (method === "GET") return select(q);
    if (method === "POST") return insert(q, parseBody(init.body), prefer);
    if (method === "PATCH") return update(q, parseBody(init.body)[0] || {}, prefer);
    if (method === "DELETE") return remove(q, prefer);
    throw new PgError("PGRST100", "método não suportado", 405);
  };

  // ----- funcoes (rpc) -----
  const needUser = () => { if (!uid) throw new PgError("42501", "Entre na sua conta.", 401); return uid; };
  const rpc = async (name, args = {}) => {
    switch (name) {
      case "trending_tags": {
        const since = new Date(Date.now() - 14 * 86400_000).toISOString();
        const rows = await all(`SELECT id, caption FROM posts WHERE visibility='public' AND archived_at IS NULL AND created_at > ${lit(since)} AND caption LIKE '%#%'`);
        const counts = new Map();
        for (const r of rows) {
          const tags = new Set([...String(r.caption).matchAll(/#([\p{L}\p{N}_]+)/gu)].map((m) => m[1].toLowerCase()));
          for (const t of tags) counts.set(t, (counts.get(t) || 0) + 1);
        }
        return [...counts.entries()].sort((a, b) => b[1] - a[1] || a[0].localeCompare(b[0])).slice(0, 20).map(([tag, posts]) => ({ tag, posts }));
      }
      case "story_mute_list": {
        needUser();
        const rows = await all(`SELECT lower(p.username) AS u FROM story_mutes m JOIN profiles p ON p.id = m.muted_user_id WHERE m.user_id = ${U}`);
        return rows.map((r) => r.u);
      }
      case "story_mute_set": {
        const me = needUser();
        const uname = String(args.p_username || "").replace(/^@+|@+$/g, "").trim().toLowerCase();
        const t = await first(`SELECT id FROM profiles WHERE lower(username) = ${lit(uname)}`);
        if (!t || t.id === me) return null;
        if (args.p_muted) await DB.prepare("INSERT OR IGNORE INTO story_mutes (user_id, muted_user_id) VALUES (?, ?)").bind(me, t.id).run();
        else await DB.prepare("DELETE FROM story_mutes WHERE user_id = ? AND muted_user_id = ?").bind(me, t.id).run();
        return null;
      }
      case "request_follow": {
        const me = needUser();
        const target = String(args.target || "");
        if (target === me) throw new PgError("P0001", "Você não pode seguir a própria conta.");
        if (await first(`SELECT 1 AS x FROM follows WHERE follower_id = ${U} AND following_id = ${lit(target)}`)) return "following";
        const t = await first(`SELECT is_private FROM profiles WHERE id = ${lit(target)}`);
        if (!t?.is_private) return "open";
        if (await first(`SELECT 1 AS x FROM follow_requests WHERE requester_id = ${U} AND target_id = ${lit(target)}`)) {
          await DB.batch([
            DB.prepare("DELETE FROM follow_requests WHERE requester_id = ? AND target_id = ?").bind(me, target),
            DB.prepare("DELETE FROM notifications WHERE recipient_id = ? AND actor_id = ? AND kind = 'follow_request'").bind(target, me),
          ]);
          return "cancelled";
        }
        if (await first(`SELECT 1 AS x FROM user_blocks WHERE blocker_id = ${lit(target)} AND blocked_id = ${U}`)) throw new PgError("P0001", "Não foi possível enviar o pedido.");
        await DB.batch([
          DB.prepare("INSERT INTO follow_requests (requester_id, target_id) VALUES (?, ?)").bind(me, target),
          notify(target, me, "follow_request", me),
        ]);
        return "requested";
      }
      case "approve_follow_request": {
        const me = needUser();
        const requester = String(args.requester || "");
        if (!(await first(`SELECT 1 AS x FROM follow_requests WHERE requester_id = ${lit(requester)} AND target_id = ${U}`))) throw new PgError("P0001", "Pedido não encontrado.");
        await DB.batch([
          DB.prepare("INSERT OR IGNORE INTO follows (follower_id, following_id) VALUES (?, ?)").bind(requester, me),
          DB.prepare("DELETE FROM follow_requests WHERE requester_id = ? AND target_id = ?").bind(requester, me),
          DB.prepare("DELETE FROM notifications WHERE recipient_id = ? AND actor_id = ? AND kind = 'follow_request'").bind(me, requester),
          notify(requester, me, "follow_accepted", me),
        ]);
        return null;
      }
      case "delete_my_account": {
        const me = needUser();
        await deleteAccount(me);
        return null;
      }
      default: throw new PgError("PGRST202", `Could not find the function public.${name}`, 404);
    }
  };

  /** Apaga a conta e tudo que e dela (equivale a apagar auth.users com cascata). */
  const deleteAccount = async (me) => {
    const L = lit(me);
    const stmts = [];
    const myPosts = (await all(`SELECT id FROM posts WHERE user_id = ${L}`)).map((r) => r.id);
    const myComments = (await all(`SELECT id FROM comments WHERE user_id = ${L}`)).map((r) => r.id);
    const myStories = (await all(`SELECT id FROM stories WHERE user_id = ${L}`)).map((r) => r.id);
    const myMessages = (await all(`SELECT id FROM messages WHERE sender_id = ${L} OR recipient_id = ${L}`)).map((r) => r.id);
    const myCommunities = (await all(`SELECT id FROM communities WHERE owner_id = ${L}`)).map((r) => r.id);
    const myListings = (await all(`SELECT id FROM marketplace_listings WHERE user_id = ${L}`)).map((r) => r.id);
    const myPacks = (await all(`SELECT id FROM sticker_packs WHERE owner_id = ${L}`)).map((r) => r.id);
    const myNotices = (await all(`SELECT id FROM neighborhood_notices WHERE user_id = ${L}`)).map((r) => r.id);
    const myPolls = (await all(`SELECT id FROM chat_polls WHERE creator_id = ${L}`)).map((r) => r.id);
    const inl = (ids) => ids.map(lit).join(",");
    const cascadeIds = (table, ids) => { if (ids.length) for (const [t, c] of CASCADE[table] || []) stmts.push(DB.prepare(`DELETE FROM ${t} WHERE ${c} IN (${inl(ids)})`)); };
    cascadeIds("posts", myPosts); cascadeIds("comments", myComments); cascadeIds("stories", myStories); cascadeIds("messages", myMessages);
    cascadeIds("communities", myCommunities); cascadeIds("marketplace_listings", myListings); cascadeIds("sticker_packs", myPacks);
    cascadeIds("neighborhood_notices", myNotices); cascadeIds("chat_polls", myPolls);
    const userCols = new Map();
    for (const fk of Object.values(FKS)) if (fk.target === "profiles" && fk.table !== "profiles") (userCols.get(fk.table) || userCols.set(fk.table, []).get(fk.table)).push(fk.col);
    for (const [t, cols] of userCols) stmts.push(DB.prepare(`DELETE FROM ${t} WHERE ${cols.map((c) => `${c} = ${L}`).join(" OR ")}`));
    stmts.push(DB.prepare(`DELETE FROM private_user_data WHERE user_id = ${L}`));
    stmts.push(DB.prepare(`DELETE FROM profiles WHERE id = ${L}`));
    stmts.push(DB.prepare(`DELETE FROM refresh_tokens WHERE account_id = ${L}`));
    stmts.push(DB.prepare(`DELETE FROM accounts WHERE id = ${L}`));
    await DB.batch(stmts);
    // arquivos do usuario no R2
    try {
      let cursor;
      do {
        const list = await env.FILES.list({ prefix: me + "/", cursor, limit: 500 });
        if (list.objects.length) await env.FILES.delete(list.objects.map((o) => o.key));
        cursor = list.truncated ? list.cursor : undefined;
      } while (cursor);
    } catch { /* arquivos orfaos nao impedem a exclusao */ }
  };

  return { rest, rpc, select, insert, update, remove, openAccount, parseRequestPath, PgError };
};
