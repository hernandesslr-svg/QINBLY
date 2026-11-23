// QINBLY API (rotas /api/...) portada do Supabase para o Cloudflare (D1 + R2).
// Mesmas rotas e mesmas respostas do servidor antigo; so o armazenamento e o login mudaram.
import { CORS, preflight, Http, UUID_RE, tokenOf, authUser, issueSession, refreshSession, verifyGoogleIdToken, hashPassword, checkPassword, createAccountWithProfile, publicUrl, safeObjectPath, nowIso, uuid, signOutOthers } from "./core.js";
import { makePg, PgError } from "./pg.js";

const corsHeaders: Record<string, string> = { ...CORS, "Content-Type": "application/json; charset=utf-8" };
const reply = (data: unknown, status = 200) => new Response(JSON.stringify(data), { status, headers: corsHeaders });

const objectMimeType = (info: any, objectPath = "") => {
  const reported = String(info?.metadata?.mimetype || "").toLowerCase().split(";")[0].trim();
  if (reported.startsWith("image/") || reported.startsWith("video/") || reported.startsWith("audio/")) return reported;
  const extension = objectPath.toLowerCase().split("?")[0].split(".").pop() || "";
  const extensionMime: Record<string, string> = {
    jpg: "image/jpeg", jpeg: "image/jpeg", png: "image/png", webp: "image/webp", gif: "image/gif",
    mp4: "video/mp4", mov: "video/quicktime", webm: "video/webm",
  };
  return reported || extensionMime[extension] || "";
};

const NIL = "00000000-0000-0000-0000-000000000000";
const inList = (ids: string[]) => ids.length ? ids.join(",") : NIL;
const isUuid = (v: string) => UUID_RE.test(v);
const POST_SELECT = "*,profiles!posts_user_id_fkey(id,username,display_name,avatar_path,is_verified)";
const CATEGORIES: Record<string, { types: string[]; max: number }> = {
  avatar: { types: ["image/jpeg", "image/png", "image/webp", "image/gif"], max: 25 },
  cover: { types: ["image/jpeg", "image/png", "image/webp", "image/gif"], max: 25 },
  cover_original: { types: ["image/jpeg", "image/png", "image/webp", "image/gif"], max: 25 },
  post_media: { types: ["image/jpeg", "image/png", "image/webp", "image/gif", "video/mp4", "video/quicktime", "video/webm"], max: 100 },
  story_media: { types: ["image/jpeg", "image/png", "image/webp", "video/mp4", "video/quicktime", "video/webm"], max: 100 },
  sticker: { types: ["image/png", "image/webp", "image/gif"], max: 2 },
  message_media: { types: ["image/jpeg", "image/png", "image/webp", "image/gif"], max: 25 },
  message_audio: { types: ["audio/mp4", "audio/mpeg", "audio/aac", "audio/webm", "audio/ogg"], max: 25 },
  message_file: { types: ["application/pdf", "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "application/vnd.ms-powerpoint", "application/vnd.openxmlformats-officedocument.presentationml.presentation", "text/plain", "text/csv", "application/zip", "application/x-zip-compressed"], max: 25 },
};

// DTOs que dependem do endereco publico dos arquivos
const makeDtos = (origin: string) => {
  const pub = (p?: string | null) => p ? publicUrl(origin, p) : "";
  const profileDto = (p: any) => ({ id:p.id, username:p.username, email:p.email || "", displayName:p.display_name || "", bio:p.bio || "", avatarUrl:p.avatar_path ? pub(p.avatar_path) : "", coverUrl:p.cover_path ? pub(p.cover_path) : null, originalCoverUrl:p.cover_original_path ? pub(p.cover_original_path) : null, verified:!!p.is_verified, createdAt:p.created_at });
  const authorDto = (p: any) => ({ id: p?.id || "", username: p?.username || "", displayName: p?.display_name || "", avatarUrl: pub(p?.avatar_path), verified: !!p?.is_verified });
  const commentDto = (c: any, p: any, likes: any[], viewer: string | null) => ({ id: c.id, body: c.body, parentId: c.parent_id || "", likes: likes.filter((l) => l.comment_id === c.id).length, likedByMe: !!viewer && likes.some((l) => l.comment_id === c.id && l.user_id === viewer), createdAt: c.created_at, editedAt: c.edited_at || "", author: { id: p?.id || "", username: p?.username || "", displayName: p?.display_name || "", avatarUrl: pub(p?.avatar_path) } });
  return { pub, profileDto, authorDto, commentDto };
};

export async function handleApi(req: Request, env: any): Promise<Response> {
  if (req.method === "OPTIONS") return preflight();
  try {
    const url = new URL(req.url);
    const origin = url.origin;
    const { pub, profileDto, authorDto, commentDto } = makeDtos(origin);
    const apiRouteIndex = url.pathname.indexOf("/api/");
    const normalizedPath = apiRouteIndex >= 0 ? url.pathname.slice(apiRouteIndex) : url.pathname.replace(/^\/functions\/v1\/qinbly-api/, "");
    const path = normalizedPath.replace(/\/$/, "") || "/";
    const token = tokenOf(req);
    const body: any = ["POST", "PATCH", "PUT", "DELETE"].includes(req.method) ? await req.json().catch(() => ({})) : {};
    const method = req.method;
    const route = (m: string, re: RegExp) => method === m ? path.match(re) : null;

    // ---------- Sessao propria (Google, e-mail e senha) ----------
    const profileById = async (id: string, preferredUsername = "") => {
      const pg0 = makePg(env, id);
      const rows = await pg0.rest(`profiles?id=eq.${encodeURIComponent(id)}&select=*`);
      if (rows?.length) return rows[0];
      const generated = `user_${id.replaceAll("-", "").slice(0, 8)}`;
      const username = preferredUsername.trim().toLowerCase().replace(/[^a-z0-9_.]/g, "").slice(0, 30);
      const taken = username.length >= 3 ? await env.DB.prepare("SELECT 1 AS x FROM profiles WHERE lower(username) = ?").bind(username).first() : true;
      await env.DB.batch([
        env.DB.prepare("INSERT OR IGNORE INTO profiles (id, username, display_name, bio) VALUES (?, ?, ?, '')").bind(id, taken ? generated : username, preferredUsername.trim().slice(0, 80)),
        env.DB.prepare("INSERT OR IGNORE INTO private_user_data (user_id) VALUES (?)").bind(id),
      ]);
      return (await pg0.rest(`profiles?id=eq.${encodeURIComponent(id)}&select=*`))[0];
    };
    const sessionReply = async (acc: any, status = 200) => {
      const p = await profileById(acc.id, String(acc.email || "").split("@")[0]);
      const s = await issueSession(env, acc);
      return reply({ token: s.token, refreshToken: s.refreshToken, user: { ...profileDto({ ...p, email: acc.email }), username: p.username } }, status);
    };
    if (req.method === "POST" && path === "/api/auth/google") {
      const idToken = String(body.idToken || body.id_token || "");
      if (!idToken) return reply({error:"Credencial Google ausente."},400);
      let claims: any;
      try { claims = await verifyGoogleIdToken(idToken); }
      catch (e) {
        if (e instanceof Http) return reply({ error: e.message, code: "google_auth_failed" }, e.status);
        return reply({ error: e instanceof Error ? e.message : "O login do Google falhou.", code: "google_auth_failed" }, 502);
      }
      const email = String(claims.email || "").trim().toLowerCase();
      let acc: any = await env.DB.prepare("SELECT id, email, google_sub FROM accounts WHERE google_sub = ?").bind(claims.sub).first();
      if (!acc && email && claims.email_verified !== false) {
        acc = await env.DB.prepare("SELECT id, email, google_sub FROM accounts WHERE lower(email) = ?").bind(email).first();
        if (acc && !acc.google_sub) await env.DB.prepare("UPDATE accounts SET google_sub = ? WHERE id = ?").bind(claims.sub, acc.id).run();
      }
      if (!acc) {
        const pref = String(claims.name || email.split("@")[0] || "");
        acc = await createAccountWithProfile(env, { email, googleSub: claims.sub, username: email.split("@")[0], displayName: pref });
      }
      return await sessionReply(acc);
    }
    // Troca o token de renovacao por uma sessao nova; um token de acesso nunca vale como renovacao.
    if (req.method === "POST" && path === "/api/auth/refresh") {
      const refreshToken = String(body.refreshToken || body.refresh_token || "").trim();
      if (!refreshToken || refreshToken.length > 4096) return reply({error:"Sessão vencida. Entre novamente na sua conta."},401);
      const r = await refreshSession(env, refreshToken);
      if (!r) return reply({error:"Não foi possível renovar a sessão. Entre novamente na sua conta.", code:"refresh_failed"},401);
      const p = await profileById(r.account.id, String(r.account.email || "").split("@")[0]);
      return reply({token:r.session.token,refreshToken:r.session.refreshToken,user:profileDto({...p,email:r.account.email})});
    }
    if (req.method === "POST" && (path === "/api/auth/login" || path === "/api/auth/register")) {
      const register = path.endsWith("register");
      const email = String(body.email||"").trim().toLowerCase();
      const password = String(body.password||"");
      if (!/^[^@\s]+@[^@\s]+\.[^@\s]+$/.test(email)) return reply({error:"Informe um e-mail válido."},400);
      if (register) {
        if (password.length < 6) return reply({error:"A senha precisa ter pelo menos 6 caracteres."},422);
        const exists = await env.DB.prepare("SELECT 1 AS x FROM accounts WHERE lower(email) = ?").bind(email).first();
        if (exists) return reply({error:"Este e-mail já tem cadastro. Entre com a sua senha ou com o Google."},422);
        const acc = await createAccountWithProfile(env, { email, passwordHash: await hashPassword(password), username: String(body.username||"").trim().toLowerCase() || email.split("@")[0], displayName: "" });
        return await sessionReply(acc, 201);
      }
      const acc: any = await env.DB.prepare("SELECT id, email, password_hash FROM accounts WHERE lower(email) = ?").bind(email).first();
      if (!acc || !acc.password_hash || !(await checkPassword(password, acc.password_hash))) return reply({error:"E-mail ou senha incorretos."},400);
      return await sessionReply(acc);
    }

    // ---------- Sessão opcional: rotas públicas funcionam sem login; as demais exigem usuário ----------
    const user: any = token ? await authUser(env, token) : null;
    const viewerId: string | null = user?.id || null;
    const pg = makePg(env, viewerId);
    const need = (): string => { if (!viewerId) throw new Http(401, "Sua sessão expirou ou não é mais válida. Entre novamente para continuar."); return viewerId; };
    const rest = (p: string, init: any = {}) => pg.rest(p, init);
    const me = async () => { const r = await pg.rest(`profiles?id=eq.${need()}&select=*`); return r?.[0] || await profileById(need()); };
    const ins = async (table: string, rows: any, prefer = "return=representation", query = "select=*") => pg.rest(query ? (table.includes("?") ? `${table}&${query}` : `${table}?${query}`) : table, { method: "POST", headers: { Prefer: prefer }, body: JSON.stringify(Array.isArray(rows) ? rows : [rows]) });
    const getStoredObjectInfo = async (objectPath: string) => {
      if (!safeObjectPath(objectPath)) return null;
      const o = await env.FILES.head(objectPath);
      return o ? { metadata: { mimetype: o.httpMetadata?.contentType || "" }, size: o.size } : null;
    };
    const profilesByIds = async (ids: string[]) => {
      const uniq = [...new Set(ids.filter(Boolean))];
      const map = new Map<string, any>();
      if (!uniq.length) return map;
      for (const p of (await rest(`profiles?id=in.(${uniq.join(",")})&select=id,username,display_name,bio,avatar_path,is_verified`)) || []) map.set(p.id, p);
      return map;
    };
    const myBlocks = async (): Promise<string[]> => viewerId ? ((await rest(`user_blocks?blocker_id=eq.${viewerId}&select=blocked_id`)) || []).map((r: any) => r.blocked_id) : [];
    const myFollowing = async (): Promise<Set<string>> => viewerId ? new Set(((await rest(`follows?follower_id=eq.${viewerId}&select=following_id`)) || []).map((r: any) => r.following_id)) : new Set();

    // ---------- Montagem de publicações ----------
    const buildPosts = async (rows: any[]) => {
      if (!rows.length) return [];
      const f = inList(rows.map((r) => r.id));
      const [likes, comms, reps, saved, votes, tagRows, following] = await Promise.all([
        rest(`post_likes?post_id=in.(${f})&select=post_id,user_id`),
        rest(`comments?post_id=in.(${f})&select=post_id`),
        rest(`post_reposts?original_post_id=in.(${f})&select=original_post_id,user_id`),
        viewerId ? rest(`saved_posts?post_id=in.(${f})&user_id=eq.${viewerId}&select=post_id`) : [],
        rest(`poll_votes?post_id=in.(${f})&select=post_id,option_index,user_id`),
        rest(`post_tags?post_id=in.(${f})&select=post_id,tagged_user_id`),
        myFollowing(),
      ]);
      const tagProfiles = await profilesByIds((tagRows || []).map((t: any) => t.tagged_user_id));
      const cnt = (list: any[], key: string, id: string) => (list || []).filter((x) => x[key] === id).length;
      return rows.map((p) => {
        const mine = (list: any[], key: string) => !!viewerId && (list || []).some((x) => x[key] === p.id && x.user_id === viewerId);
        const pv = (votes || []).filter((v: any) => v.post_id === p.id);
        const options: string[] = Array.isArray(p.poll_options) ? p.poll_options.map((o: any) => typeof o === "string" ? o : String(o?.text ?? o?.label ?? "")) : [];
        const counts = options.map((_, i) => ({ optionIndex: i, votes: pv.filter((v: any) => v.option_index === i).length }));
        const myVote = viewerId ? (pv.find((v: any) => v.user_id === viewerId)?.option_index ?? -1) : -1;
        return {
          id: p.id, createdAt: p.created_at, caption: p.caption || "", mediaUrl: pub(p.media_path), mediaType: p.media_type,
          mediaUrls: (p.media_paths || []).map(pub), visibility: p.visibility, textBg: Number(p.text_bg) || 0,
          scheduledAt: p.publish_at && Date.parse(p.publish_at) > Date.now() ? p.publish_at : "",
          author: authorDto(p.profiles), authorId: p.user_id,
          likes: cnt(likes, "post_id", p.id), comments: cnt(comms, "post_id", p.id),
          likedByMe: mine(likes, "post_id"), savedByMe: !!viewerId && (saved || []).some((x: any) => x.post_id === p.id),
          repostedByMe: mine((reps || []).map((r: any) => ({ post_id: r.original_post_id, user_id: r.user_id })), "post_id"),
          reposts: cnt(reps, "original_post_id", p.id),
          pinned: !!p.pinned_at, archived: !!p.archived_at, isFollowing: following.has(p.user_id),
          pollOptions: options, pollVotes: counts, myPollVote: myVote,
          taggedUsers: (tagRows || []).filter((t: any) => t.post_id === p.id).map((t: any) => tagProfiles.get(t.tagged_user_id)?.username).filter(Boolean),
        };
      });
    };
    const feedFilter = async () => {
      const blocks = await myBlocks();
      return "archived_at=is.null&or=(publish_at.is.null,publish_at.lte." + encodeURIComponent(new Date().toISOString()) + ")" + (blocks.length ? `&user_id=not.in.(${blocks.join(",")})` : "");
    };
    const visiblePolls = (rows: any[]) => rows.filter((p) => p.media_type !== "poll" || (p.poll_expires_at && Date.parse(p.poll_expires_at) > Date.now()) || p.user_id === viewerId);
    const fileOwned = async (fileId: string, category: string) => {
      if (!safeObjectPath(fileId) || !fileId.startsWith(`${need()}/${category}/`)) throw new Http(403, "O arquivo precisa pertencer à sua conta.");
      const info = await getStoredObjectInfo(fileId);
      if (!info) throw new Http(422, "O arquivo não foi enviado corretamente à nuvem. Envie novamente.");
      return { info, mime: objectMimeType(info, fileId) };
    };

    // ---------- Helpers de mensagens / comunidades ----------
    const decodeMarks = async (rows: any[], bodyKey = "body") => {
      const pollIds = rows.map((r) => String(r[bodyKey] || "").match(/^qinbly:poll:([0-9a-f-]{36})$/i)?.[1]).filter(Boolean) as string[];
      const polls = new Map<string, any>();
      if (pollIds.length) {
        const [ps, vs] = await Promise.all([
          rest(`chat_polls?id=in.(${inList(pollIds)})&select=*`),
          rest(`chat_poll_votes?poll_id=in.(${inList(pollIds)})&select=poll_id,user_id,option_index`),
        ]);
        for (const p of ps || []) {
          const opts: string[] = Array.isArray(p.options) ? p.options.map(String) : [];
          const pv = (vs || []).filter((v: any) => v.poll_id === p.id);
          polls.set(p.id, { id: p.id, question: p.question, options: opts, votes: opts.map((_, i) => pv.filter((v: any) => v.option_index === i).length), myVote: viewerId ? (pv.find((v: any) => v.user_id === viewerId)?.option_index ?? -1) : -1 });
        }
      }
      return (r: any) => {
        const raw = String(r[bodyKey] || "");
        const out: any = { body: raw };
        let m: RegExpMatchArray | null;
        if ((m = raw.match(/^qinbly:img:(.+)$/))) { out.imageUrl = pub(m[1]); out.body = ""; }
        else if ((m = raw.match(/^qinbly:audio:(.+):(\d+)$/))) { out.audioUrl = pub(m[1]); out.audioSeconds = Number(m[2]); out.body = ""; }
        else if ((m = raw.match(/^qinbly:file:(.+?):([^:]*)$/))) { out.fileUrl = pub(m[1]); out.fileName = decodeURIComponent(m[2] || "documento"); out.body = ""; }
        else if ((m = raw.match(/^qinbly:sticker:(.+)$/))) { out.stickerUrl = pub(m[1]); out.body = ""; }
        else if ((m = raw.match(/^qinbly:poll:([0-9a-f-]{36})$/i))) { out.poll = polls.get(m[1]) || null; out.body = ""; }
        return out;
      };
    };
    const previewOf = (raw: string) => raw.startsWith("qinbly:img:") ? "📷 Foto" : raw.startsWith("qinbly:audio:") ? "🎤 Mensagem de voz" : raw.startsWith("qinbly:file:") ? "📎 Documento" : raw.startsWith("qinbly:sticker:") ? "Figurinha" : raw.startsWith("qinbly:poll:") ? "📊 Enquete" : raw;
    const communityDto = (c: any, members: any[], owner: any) => {
      const all = members.filter((m) => m.community_id === c.id);
      const mine = all.filter((m) => m.role !== "pending");
      const meRow = viewerId ? all.find((m) => m.user_id === viewerId) : null;
      const isOwnerNow = !!viewerId && c.owner_id === viewerId;
      return {
        id: c.id, name: c.name, description: c.description || "", emoji: c.emoji || "👥", avatarUrl: pub(c.avatar_path),
        ownerUsername: owner?.username || "", members: mine.length + (mine.some((m) => m.user_id === c.owner_id) ? 0 : 1),
        joined: isOwnerNow || (!!meRow && meRow.role !== "pending"), isOwner: isOwnerNow,
        isAdmin: isOwnerNow || meRow?.role === "admin", pending: !!meRow && meRow.role === "pending",
        joinMode: c.join_mode || "open", writeMode: c.write_mode || "all", rules: c.rules || "",
      };
    };
    const listCommunities = async (filter: string) => {
      const rows = (await rest(`communities?select=*&order=created_at.desc&limit=100`)) || [];
      const members = rows.length ? (await rest(`community_members?community_id=in.(${inList(rows.map((r: any) => r.id))})&select=community_id,user_id,role`)) || [] : [];
      const owners = await profilesByIds(rows.map((r: any) => r.owner_id));
      let list = rows.map((c: any) => communityDto(c, members, owners.get(c.owner_id)));
      if (filter === "mine") list = list.filter((c: any) => c.joined);
      else if (filter === "discover") list = list.filter((c: any) => !c.joined);
      else if (filter === "trending") list = list.sort((a: any, b: any) => b.members - a.members);
      return list;
    };
    const canUseCommunity = async (id: string) => {
      const c = (await rest(`communities?id=eq.${id}&select=*`))?.[0];
      if (!c) throw new Http(404, "Comunidade não encontrada.");
      return c;
    };

    // ---------- Públicas ----------
    if (method === "GET" && path === "/api/ui/navigation") return reply({});
    if (method === "GET" && path === "/api/users/username-available") {
      const u = String(url.searchParams.get("u") || "").trim().toLowerCase();
      if (!/^[a-z0-9_.]{3,30}$/.test(u)) return reply({ available: false });
      const rows = await rest(`profiles?username=eq.${encodeURIComponent(u)}&select=id`);
      return reply({ available: !rows?.length || rows[0].id === viewerId });
    }
    if (method === "POST" && path === "/api/auth/forgot") {
      const email = String(body.email || "").trim().toLowerCase();
      // Sem servico de e-mail configurado ainda: nada e enviado (resposta igual para nao revelar quem tem conta).
      void email;
      return reply({ ok: true, message: "Se esse e-mail tiver uma conta, enviamos o link para criar uma nova senha." });
    }
    if (method === "GET" && path === "/api/hashtags/trending") {
      const rows = await rest("rpc/trending_tags", { method: "POST", body: "{}" });
      return reply({ tags: (rows || []).map((r: any) => ({ tag: r.tag, posts: Number(r.posts) })) });
    }
    const tagM = route("GET", /^\/api\/hashtags\/([^/]+)$/);
    if (tagM) {
      const tag = decodeURIComponent(tagM[1]).replace(/^#/, "").replace(/[^\p{L}\p{N}_]/gu, "");
      if (!tag) return reply({ posts: [] });
      const rows = await rest(`posts?select=${POST_SELECT}&visibility=eq.public&caption=ilike.*%23${encodeURIComponent(tag)}*&${await feedFilter()}&order=created_at.desc&limit=50`);
      return reply({ posts: await buildPosts(visiblePolls(rows || [])) });
    }
    if (method === "GET" && path === "/api/users/suggested") {
      const following = await myFollowing();
      const blocks = await myBlocks();
      const skip = [...following, ...blocks, ...(viewerId ? [viewerId] : [])];
      const rows = await rest(`profiles?select=id,username,display_name,bio,avatar_path,is_verified,created_at${skip.length ? `&id=not.in.(${skip.join(",")})` : ""}&order=created_at.desc&limit=20`);
      return reply({ users: (rows || []).map((p: any) => ({ ...profileDto(p), isFollowing: false })) });
    }
    if (method === "GET" && (path === "/api/users/search" || path === "/api/search")) {
      const q = (url.searchParams.get("q") || "").replace(/[,()*]/g, " ").trim();
      if (!q) return reply({ users: [] });
      const rows = await rest(`profiles?or=(username.ilike.*${encodeURIComponent(q)}*,display_name.ilike.*${encodeURIComponent(q)}*)&select=id,username,display_name,bio,avatar_path,is_verified&limit=30`);
      const following = await myFollowing();
      const usersOut = (rows || []).map((p: any) => ({ ...profileDto(p), isFollowing: following.has(p.id) }));
      if (path !== "/api/search") return reply({ users: usersOut });
      const postRows = await rest(`posts?select=${POST_SELECT}&visibility=eq.public&caption=ilike.*${encodeURIComponent(q)}*&${await feedFilter()}&order=created_at.desc&limit=20`);
      const tagRows = await rest("rpc/trending_tags", { method: "POST", body: "{}" }).catch(() => []);
      const needle = q.toLowerCase().replace(/^#/, "");
      const tags = (tagRows || []).filter((t: any) => String(t.tag).toLowerCase().includes(needle)).slice(0, 10).map((t: any) => ({ tag: t.tag, posts: Number(t.posts) }));
      return reply({ users: usersOut, posts: await buildPosts(visiblePolls(postRows || [])), tags });
    }
    if (method === "GET" && path === "/api/posts") {
      const limit = Math.min(Math.max(Number(url.searchParams.get("limit") || 30), 1), 60);
      const before = url.searchParams.get("before");
      const rows = await rest(`posts?select=${POST_SELECT}&${await feedFilter()}${before ? `&created_at=lt.${encodeURIComponent(before)}` : ""}&order=created_at.desc&limit=${limit}`);
      return reply({ posts: await buildPosts(visiblePolls(rows || [])) });
    }
    const onePost = route("GET", /^\/api\/posts\/([0-9a-f-]{36})$/i);
    if (onePost) {
      const rows = await rest(`posts?id=eq.${onePost[1]}&select=${POST_SELECT}`);
      if (!rows?.length) return reply({ error: "Publicação não encontrada." }, 404);
      return reply({ post: (await buildPosts(rows))[0] });
    }
    const comGet = route("GET", /^\/api\/posts\/([0-9a-f-]{36})\/comments$/i);
    if (comGet) {
      const rows = await rest(`comments?post_id=eq.${comGet[1]}&select=*&order=created_at.asc&limit=500`);
      const blocks = new Set(await myBlocks());
      const list = (rows || []).filter((c: any) => !blocks.has(c.user_id));
      const [profiles, likes] = await Promise.all([
        profilesByIds(list.map((c: any) => c.user_id)),
        list.length ? rest(`comment_likes?comment_id=in.(${inList(list.map((c: any) => c.id))})&select=comment_id,user_id`) : [],
      ]);
      return reply({ comments: list.map((c: any) => commentDto(c, profiles.get(c.user_id), likes || [], viewerId)) });
    }
    const liveGet = route("GET", /^\/api\/lives\/([^/]+)\/comments$/);
    if (liveGet) {
      const host = decodeURIComponent(liveGet[1]).replace(/^@/, "").toLowerCase();
      const after = url.searchParams.get("after");
      const rows = await rest(`live_comments?live_host=eq.${encodeURIComponent(host)}${after ? `&created_at=gt.${encodeURIComponent(after)}` : ""}&select=*&order=created_at.asc&limit=100`);
      const profiles = await profilesByIds((rows || []).map((r: any) => r.user_id));
      return reply({ comments: (rows || []).map((r: any) => ({ id: r.id, body: r.body, createdAt: r.created_at, username: profiles.get(r.user_id)?.username || "", displayName: profiles.get(r.user_id)?.display_name || "" })) });
    }
    // perfis públicos (depois das rotas /api/users/<nome fixo> acima e das rotas /me abaixo)
    const conn = route("GET", /^\/api\/users\/([^/]+)\/(followers|following)$/);
    if (conn && conn[1] !== "me") {
      const name = decodeURIComponent(conn[1]).replace(/^@/, "").toLowerCase();
      const target = (await rest(`profiles?username=eq.${encodeURIComponent(name)}&select=id`))?.[0];
      if (!target) return reply({ users: [] });
      const followers = conn[2] === "followers";
      const links = await rest(`follows?${followers ? "following_id" : "follower_id"}=eq.${target.id}&select=follower_id,following_id`);
      const ids = (links || []).map((x: any) => followers ? x.follower_id : x.following_id);
      const profiles = await profilesByIds(ids);
      const following = await myFollowing();
      return reply({ users: ids.map((id: string) => profiles.get(id)).filter(Boolean).map((p: any) => ({ ...profileDto(p), isFollowing: following.has(p.id) })) });
    }
    const communityList = route("GET", /^\/api\/communities$/);
    if (communityList) return reply({ communities: await listCommunities(url.searchParams.get("filter") || "discover") });

    // ---------- Daqui para baixo: exige sessão (rotas /me primeiro) ----------
    if (method === "GET" && path === "/api/auth/me") { need(); return reply({ user: profileDto({ ...await me(), email: user.email }) }); }
    if (method === "PATCH" && path === "/api/users/me") {
      need();
      const payload: any = {};
      if (body.username) {
        const u = String(body.username).trim().toLowerCase();
        if (!/^[a-z0-9_.]{3,30}$/.test(u)) return reply({ error: "Nome de usuário inválido (3 a 30 letras, números, _ ou .)." }, 400);
        payload.username = u;
      }
      if (body.displayName !== undefined) payload.display_name = String(body.displayName).trim().slice(0, 60);
      if (body.bio !== undefined) payload.bio = String(body.bio).trim().slice(0, 500);
      payload.updated_at = new Date().toISOString();
      try {
        const rows = await rest(`profiles?id=eq.${viewerId}&select=*`, { method: "PATCH", headers: { Prefer: "return=representation" }, body: JSON.stringify(payload) });
        return reply({ user: profileDto({ ...rows?.[0], email: user.email }) });
      } catch (e) {
        if (String(e).includes("duplicate key")) return reply({ error: "Esse nome de usuário já está em uso." }, 409);
        throw e;
      }
    }
    if (method === "GET" && path === "/api/users/me/blocks") {
      const rows = await rest(`user_blocks?blocker_id=eq.${need()}&select=blocked_id&order=created_at.desc`);
      const profiles = await profilesByIds((rows || []).map((r: any) => r.blocked_id));
      return reply({ blocks: (rows || []).map((r: any) => profiles.get(r.blocked_id)).filter(Boolean).map((p: any) => ({ id: p.id, username: p.username, displayName: p.display_name || "" })) });
    }
    if (method === "GET" && path === "/api/users/me/export") {
      const id = need();
      const [posts, comments, messages, likes] = await Promise.all([
        rest(`posts?user_id=eq.${id}&select=*`), rest(`comments?user_id=eq.${id}&select=*`),
        rest(`messages?or=(sender_id.eq.${id},recipient_id.eq.${id})&select=*&limit=2000`), rest(`post_likes?user_id=eq.${id}&select=post_id,created_at`),
      ]);
      return reply({ exportedAt: new Date().toISOString(), profile: profileDto({ ...await me(), email: user.email }), posts, comments, messages, likes });
    }
    if (method === "POST" && path === "/api/users/me/delete") {
      const p = await me();
      if (String(body.confirmUsername || "").trim().toLowerCase() !== String(p.username).toLowerCase()) return reply({ error: "Digite seu nome de usuário exatamente para confirmar." }, 400);
      await rest("rpc/delete_my_account", { method: "POST", body: "{}" });
      return reply({ ok: true });
    }
    if (method === "POST" && path === "/api/presence/heartbeat") {
      const id = need();
      await rest("user_presence?on_conflict=user_id", { method: "POST", headers: { Prefer: "resolution=merge-duplicates,return=minimal" }, body: JSON.stringify([{ user_id: id, last_seen: new Date().toISOString() }]) });
      const online = await rest(`user_presence?last_seen=gte.${encodeURIComponent(new Date(Date.now() - 5 * 60_000).toISOString())}&select=user_id&limit=1000`);
      return reply({ ok: true, onlineCount: (online || []).length });
    }
    if (method === "POST" && path === "/api/files/upload-url") {
      const id = need();
      const category = String(body.category || "");
      const contentType = String(body.contentType || "").toLowerCase();
      const sizeBytes = Number(body.sizeBytes);
      const cat = CATEGORIES[category];
      if (!cat) return reply({ error: "Categoria de arquivo inválida." }, 400);
      if (!cat.types.includes(contentType)) return reply({ error: "Tipo de arquivo não permitido para esta categoria." }, 415);
      const maxBytes = cat.max * 1024 * 1024;
      if (!Number.isSafeInteger(sizeBytes) || sizeBytes < 1 || sizeBytes > maxBytes) return reply({ error: `Arquivo inválido ou maior que ${cat.max} MB.` }, 413);
      const name = String(body.fileName || "file").replace(/[^a-zA-Z0-9._-]/g, "_").slice(-100);
      const objectPath = `${id}/${category}/${crypto.randomUUID()}-${name}`;
      return reply({ file: { id: objectPath, category, contentType, sizeBytes }, uploadUrl: `${origin}/storage/v1/object/post-media/${objectPath}`, objectPath, method: "PUT", requiredHeaders: { "Content-Type": contentType }, expiresInSeconds: null }, 201);
    }
    const fileComplete = route("POST", /^\/api\/files\/(.+)\/complete$/);
    if (fileComplete) {
      const id = need();
      const objectPath = decodeURIComponent(fileComplete[1]);
      if (!safeObjectPath(objectPath) || !objectPath.startsWith(`${id}/`)) return reply({ error: "Arquivo inválido para esta conta." }, 403);
      const parts = objectPath.split("/");
      if (parts.length < 3 || !CATEGORIES[parts[1]]) return reply({ error: "Caminho de mídia inválido." }, 400);
      const info = await getStoredObjectInfo(objectPath);
      if (!info) return reply({ error: "O arquivo ainda não foi enviado à nuvem ou não pode ser acessado. Tente enviar novamente." }, 422);
      const mime = objectMimeType(info, objectPath);
      if (["avatar", "cover", "cover_original", "message_media", "sticker"].includes(parts[1]) && mime && !mime.startsWith("image/")) return reply({ error: "Esse arquivo precisa ser uma imagem." }, 415);
      if (["post_media", "story_media"].includes(parts[1]) && !(mime.startsWith("image/") || mime.startsWith("video/"))) return reply({ error: "A mídia precisa ser uma imagem ou vídeo." }, 415);
      return reply({ ok: true, fileId: objectPath, status: "ready" });
    }
    const profileMedia = route("PATCH", /^\/api\/users\/me\/(avatar|cover|cover_original)$/);
    if (profileMedia) {
      const id = need();
      const category = profileMedia[1];
      const col = category === "cover" ? "cover_path" : category === "cover_original" ? "cover_original_path" : "avatar_path";
      const objectPath = String(body.fileId || "");
      if (!safeObjectPath(objectPath) || !objectPath.startsWith(`${id}/${category}/`)) return reply({ error: "Arquivo inválido para esta conta." }, 403);
      const info = await getStoredObjectInfo(objectPath);
      if (!info || !objectMimeType(info, objectPath).startsWith("image/")) return reply({ error: "A imagem não foi encontrada ou não passou na validação. Envie novamente." }, 422);
      const rows = await rest(`profiles?id=eq.${id}&select=*`, { method: "PATCH", headers: { Prefer: "return=representation" }, body: JSON.stringify({ [col]: objectPath, updated_at: new Date().toISOString() }) });
      return reply({ user: profileDto({ ...rows?.[0], email: user.email }) });
    }

    // ---------- Seguir / bloquear / denunciar ----------
    const followM = route("POST", /^\/api\/users\/([0-9a-f-]{36})\/follow$/i);
    if (followM) {
      const id = followM[1];
      if (id === need()) return reply({ error: "Você não pode seguir a própria conta." }, 400);
      if (!(await rest(`profiles?id=eq.${id}&select=id`))?.length) return reply({ error: "Perfil não encontrado." }, 404);
      const existing = await rest(`follows?follower_id=eq.${viewerId}&following_id=eq.${id}&select=follower_id`);
      if (existing?.length) { await rest(`follows?follower_id=eq.${viewerId}&following_id=eq.${id}`, { method: "DELETE" }); return reply({ following: false }); }
      await ins("follows", { follower_id: viewerId, following_id: id }, "return=minimal", "");
      return reply({ following: true });
    }
    const blockM = route("POST", /^\/api\/users\/([0-9a-f-]{36})\/block$/i);
    if (blockM) {
      const id = blockM[1];
      if (id === need()) return reply({ error: "Você não pode bloquear a própria conta." }, 400);
      const existing = await rest(`user_blocks?blocker_id=eq.${viewerId}&blocked_id=eq.${id}&select=blocked_id`);
      if (existing?.length) { await rest(`user_blocks?blocker_id=eq.${viewerId}&blocked_id=eq.${id}`, { method: "DELETE" }); return reply({ blocked: false }); }
      await ins("user_blocks", { blocker_id: viewerId, blocked_id: id }, "return=minimal", "");
      await rest(`follows?follower_id=eq.${viewerId}&following_id=eq.${id}`, { method: "DELETE" });
      return reply({ blocked: true });
    }
    if (method === "POST" && path === "/api/reports") {
      const id = need();
      const targetType = String(body.targetType || "");
      const targetId = String(body.targetId || "").slice(0, 80);
      const reason = String(body.reason || "other").slice(0, 40);
      if (!["post", "comment", "user", "message"].includes(targetType) || !targetId) return reply({ error: "Denúncia inválida." }, 400);
      await ins("content_reports", { reporter_id: id, target_type: targetType, target_id: targetId, reason }, "return=minimal", "");
      return reply({ ok: true }, 201);
    }

    // ---------- Publicações ----------
    if (method === "POST" && path === "/api/posts") {
      const id = need();
      const caption = String(body.caption || "").trim();
      const mediaType = String(body.mediaType || "text");
      const visibility = body.visibility === "private" ? "private" : "public";
      if (caption.length > 5000) return reply({ error: "A legenda deve ter no máximo 5000 caracteres." }, 400);
      if (!["text", "image", "video", "poll"].includes(mediaType)) return reply({ error: "Tipo de publicação inválido." }, 400);
      const row: any = { user_id: id, caption, media_type: mediaType, media_path: null, visibility };
      if (mediaType === "poll") {
        const options = (Array.isArray(body.pollOptions) ? body.pollOptions : []).map((o: any) => String(o).trim()).filter(Boolean).slice(0, 6);
        if (!caption || options.length < 2) return reply({ error: "A enquete precisa de uma pergunta e pelo menos 2 opções." }, 400);
        const hours = Math.min(Math.max(Number(body.durationHours) || 24, 1), 24 * 7);
        row.poll_options = options;
        row.poll_expires_at = new Date(Date.now() + hours * 3600_000).toISOString();
        row.poll_multiple_answers = !!body.multipleAnswers;
        row.poll_show_results_after_vote = body.showResultsAfterVote !== false;
      } else if (mediaType === "text") {
        if (!caption) return reply({ error: "Escreva algo para publicar." }, 400);
        if (body.mediaFileId) return reply({ error: "Publicação de texto não deve conter arquivo." }, 400);
        const bgPick = Number(body.textBg);
        if (Number.isInteger(bgPick) && bgPick >= 1 && bgPick <= 6) row.text_bg = bgPick;
      } else {
        const ids: string[] = Array.isArray(body.mediaFileIds) && body.mediaFileIds.length ? body.mediaFileIds.map(String).slice(0, 10) : [String(body.mediaFileId || "")];
        for (const fid of ids) {
          const { mime } = await fileOwned(fid, "post_media");
          if (!(mediaType === "image" ? mime.startsWith("image/") : mime.startsWith("video/"))) return reply({ error: "A mídia não corresponde ao tipo da publicação." }, 422);
        }
        row.media_path = ids[0];
        if (ids.length > 1) row.media_paths = ids;
      }
      // Agendamento vale para texto, foto e vídeo (enquete já tem prazo próprio).
      if (mediaType !== "poll") {
        const sched = Date.parse(String(body.scheduledAt || ""));
        if (Number.isFinite(sched)) {
          if (sched < Date.now() + 30_000) return reply({ error: "Escolha um horário no futuro." }, 400);
          if (sched > Date.now() + 90 * 86400_000) return reply({ error: "Agende para no máximo 90 dias." }, 400);
          row.publish_at = new Date(sched).toISOString();
          row.created_at = row.publish_at;
        }
      }
      const created = (await ins("posts", row, "return=representation", `select=${POST_SELECT}`))?.[0];
      const tagged: string[] = (Array.isArray(body.taggedUsernames) ? body.taggedUsernames : []).map((u: any) => String(u).replace(/^@/, "").toLowerCase()).slice(0, 20);
      if (tagged.length) {
        const found = (await rest(`profiles?username=in.(${tagged.map((u) => `"${u.replace(/[^a-z0-9_.]/g, "")}"`).join(",")})&select=id`)) || [];
        const tagRows = found.filter((p: any) => p.id !== id).map((p: any) => ({ post_id: created.id, tagged_user_id: p.id, tagged_by: id }));
        if (tagRows.length) await ins("post_tags", tagRows, "return=minimal,resolution=ignore-duplicates", "");
      }
      return reply({ post: (await buildPosts([created]))[0] }, 201);
    }
    const postM = path.match(/^\/api\/posts\/([0-9a-f-]{36})(?:\/(like|save|repost|pin|archive|vote|comments|view|stats))?$/i);
    if (postM) {
      const pid = postM[1], action = postM[2];
      const id = need();
      if (method === "PATCH" && !action) {
        const patch: any = { updated_at: new Date().toISOString() };
        if (body.caption !== undefined) { const c = String(body.caption).trim(); if (c.length > 5000) return reply({ error: "A legenda deve ter no máximo 5000 caracteres." }, 400); patch.caption = c; }
        if (body.visibility !== undefined) patch.visibility = body.visibility === "private" ? "private" : "public";
        const rows = await rest(`posts?id=eq.${pid}&user_id=eq.${id}&select=${POST_SELECT}`, { method: "PATCH", headers: { Prefer: "return=representation" }, body: JSON.stringify(patch) });
        if (!rows?.length) return reply({ error: "Publicação não encontrada." }, 404);
        return reply({ post: (await buildPosts(rows))[0] });
      }
      if (method === "DELETE" && !action) { await rest(`posts?id=eq.${pid}&user_id=eq.${id}`, { method: "DELETE" }); return reply({ ok: true }); }
      if (method === "POST" && action === "view") {
        const vp = (await rest(`posts?id=eq.${pid}&select=id,user_id`))?.[0];
        if (!vp || vp.user_id === id) return reply({ ok: true });
        await ins("post_views?on_conflict=post_id,viewer_id,day", { post_id: pid, viewer_id: id }, "resolution=ignore-duplicates,return=minimal", "");
        return reply({ ok: true });
      }
      if (method === "GET" && action === "stats") {
        const own = (await rest(`posts?id=eq.${pid}&user_id=eq.${id}&select=id`))?.[0];
        if (!own) return reply({ error: "Só quem publicou pode ver as estatísticas." }, 403);
        const [views, likes, comms, reps] = await Promise.all([
          rest(`post_views?post_id=eq.${pid}&select=viewer_id,day&limit=10000`),
          rest(`post_likes?post_id=eq.${pid}&select=user_id`),
          rest(`comments?post_id=eq.${pid}&select=id`),
          rest(`post_reposts?original_post_id=eq.${pid}&select=user_id`),
        ]);
        const days: { day: string; views: number }[] = [];
        for (let i = 6; i >= 0; i--) {
          const d = new Date(Date.now() - i * 86400_000).toISOString().slice(0, 10);
          days.push({ day: d, views: (views || []).filter((v: any) => v.day === d).length });
        }
        return reply({ views: (views || []).length, viewers: new Set((views || []).map((v: any) => v.viewer_id)).size, likes: (likes || []).length, comments: (comms || []).length, reposts: (reps || []).length, days });
      }
      if (method === "POST" && action === "like") {
        const post = (await rest(`posts?id=eq.${pid}&select=id,visibility`))?.[0];
        if (!post || post.visibility !== "public") return reply({ error: "Publicação não encontrada." }, 404);
        const existing = await rest(`post_likes?post_id=eq.${pid}&user_id=eq.${id}&select=user_id`);
        if (existing?.length) { await rest(`post_likes?post_id=eq.${pid}&user_id=eq.${id}`, { method: "DELETE" }); return reply({ liked: false }); }
        await ins("post_likes", { post_id: pid, user_id: id }, "return=minimal", "");
        return reply({ liked: true });
      }
      if (method === "POST" && action === "save") {
        const existing = await rest(`saved_posts?post_id=eq.${pid}&user_id=eq.${id}&select=post_id`);
        if (existing?.length) { await rest(`saved_posts?post_id=eq.${pid}&user_id=eq.${id}`, { method: "DELETE" }); return reply({ saved: false }); }
        await ins("saved_posts", { post_id: pid, user_id: id }, "return=minimal", "");
        return reply({ saved: true });
      }
      if (method === "POST" && action === "repost") {
        const existing = await rest(`post_reposts?original_post_id=eq.${pid}&user_id=eq.${id}&select=id`);
        if (existing?.length) { await rest(`post_reposts?original_post_id=eq.${pid}&user_id=eq.${id}`, { method: "DELETE" }); return reply({ reposted: false }); }
        await ins("post_reposts", { original_post_id: pid, user_id: id }, "return=minimal", "");
        return reply({ reposted: true });
      }
      if (method === "POST" && (action === "pin" || action === "archive")) {
        const post = (await rest(`posts?id=eq.${pid}&user_id=eq.${id}&select=id,pinned_at,archived_at`))?.[0];
        if (!post) return reply({ error: "Publicação não encontrada." }, 404);
        const col = action === "pin" ? "pinned_at" : "archived_at";
        const on = !post[col];
        if (action === "pin" && on) {
          const pinned = await rest(`posts?user_id=eq.${id}&pinned_at=not.is.null&select=id`);
          if ((pinned || []).length >= 3) return reply({ error: "Você pode fixar no máximo 3 publicações." }, 400);
        }
        await rest(`posts?id=eq.${pid}&user_id=eq.${id}`, { method: "PATCH", headers: { Prefer: "return=minimal" }, body: JSON.stringify({ [col]: on ? new Date().toISOString() : null }) });
        return reply(action === "pin" ? { pinned: on } : { archived: on });
      }
      if (method === "POST" && action === "vote") {
        const post = (await rest(`posts?id=eq.${pid}&select=id,media_type,poll_multiple_answers,poll_options,poll_expires_at`))?.[0];
        if (!post || post.media_type !== "poll") return reply({ error: "Enquete não encontrada." }, 404);
        if (!post.poll_expires_at || Date.parse(post.poll_expires_at) <= Date.now()) return reply({ error: "Essa enquete já terminou." }, 400);
        const idx = Number(body.optionIndex);
        if (!Number.isInteger(idx) || idx < 0 || idx >= (post.poll_options || []).length) return reply({ error: "Opção inválida." }, 400);
        const mineVotes = await rest(`poll_votes?post_id=eq.${pid}&user_id=eq.${id}&select=option_index`);
        if (mineVotes?.some((v: any) => v.option_index === idx)) return reply({ ok: true, myPollVote: idx });
        if (mineVotes?.length) return reply({ error: "Você já votou nessa enquete." }, 409);
        await ins("poll_votes", { post_id: pid, user_id: id, option_index: idx }, "return=minimal", "");
        return reply({ ok: true, myPollVote: idx });
      }
      if (method === "POST" && action === "comments") {
        const text = String(body.body || "").trim();
        if (!text || text.length > 2000) return reply({ error: "O comentário precisa ter entre 1 e 2000 caracteres." }, 400);
        const post = (await rest(`posts?id=eq.${pid}&select=id,visibility`))?.[0];
        if (!post || post.visibility !== "public") return reply({ error: "Publicação não encontrada." }, 404);
        const parentId = body.parentId ? String(body.parentId) : null;
        if (parentId) {
          if (!isUuid(parentId)) return reply({ error: "Comentário de origem inválido." }, 400);
          const parent = (await rest(`comments?id=eq.${parentId}&post_id=eq.${pid}&select=id`))?.[0];
          if (!parent) return reply({ error: "Comentário de origem não encontrado." }, 404);
        }
        const row = (await ins("comments", { post_id: pid, user_id: id, body: text, parent_id: parentId }))?.[0];
        return reply({ comment: commentDto(row, await me(), [], id) }, 201);
      }
    }
    const commentM = path.match(/^\/api\/comments\/([0-9a-f-]{36})(?:\/(like))?$/i);
    if (commentM) {
      const cid = commentM[1];
      const id = need();
      if (method === "PATCH" && !commentM[2]) {
        const text = String(body.body || "").trim();
        if (!text || text.length > 2000) return reply({ error: "O comentário precisa ter entre 1 e 2000 caracteres." }, 400);
        const rows = await rest(`comments?id=eq.${cid}&user_id=eq.${id}&select=*`, { method: "PATCH", headers: { Prefer: "return=representation" }, body: JSON.stringify({ body: text, edited_at: new Date().toISOString() }) });
        if (!rows?.length) return reply({ error: "Comentário não encontrado." }, 404);
        return reply({ comment: commentDto(rows[0], await me(), [], id) });
      }
      if (method === "DELETE" && !commentM[2]) {
        const c = (await rest(`comments?id=eq.${cid}&select=id,user_id,post_id`))?.[0];
        if (!c) return reply({ ok: true });
        if (c.user_id !== id) {
          // o dono da publicação também pode apagar comentários dela (via função pública de moderação indisponível: só o autor apaga)
          return reply({ error: "Você só pode apagar os seus próprios comentários." }, 403);
        }
        await rest(`comments?id=eq.${cid}&user_id=eq.${id}`, { method: "DELETE" });
        return reply({ ok: true });
      }
      if (method === "POST" && commentM[2] === "like") {
        const existing = await rest(`comment_likes?comment_id=eq.${cid}&user_id=eq.${id}&select=user_id`);
        if (existing?.length) { await rest(`comment_likes?comment_id=eq.${cid}&user_id=eq.${id}`, { method: "DELETE" }); return reply({ liked: false }); }
        await ins("comment_likes", { comment_id: cid, user_id: id }, "return=minimal", "");
        return reply({ liked: true });
      }
    }
    if (method === "GET" && path === "/api/me/saved") {
      const id = need();
      const saved = await rest(`saved_posts?user_id=eq.${id}&select=post_id,created_at&order=created_at.desc&limit=100`);
      if (!saved?.length) return reply({ posts: [] });
      const rows = await rest(`posts?id=in.(${inList(saved.map((s: any) => s.post_id))})&select=${POST_SELECT}`);
      const built = await buildPosts(rows || []);
      const order = new Map<string, number>(saved.map((s: any, i: number) => [s.post_id, i]));
      return reply({ posts: built.sort((a: any, b: any) => (order.get(a.id) ?? 0) - (order.get(b.id) ?? 0)) });
    }

    // ---------- Notificações ----------
    if (method === "GET" && path === "/api/notifications") {
      const rows = await rest(`notifications?recipient_id=eq.${need()}&select=*&order=created_at.desc&limit=100`);
      const actors = await profilesByIds((rows || []).map((n: any) => n.actor_id));
      return reply({ notifications: (rows || []).map((n: any) => {
        const p = actors.get(n.actor_id) || {};
        return { id: n.id, kind: n.kind, entityId: n.entity_id || "", createdAt: n.created_at, readAt: n.read_at || "", actor: { id: p.id || "", username: p.username || "", displayName: p.display_name || "", avatarUrl: pub(p.avatar_path) } };
      }) });
    }
    if (method === "POST" && path === "/api/notifications/read") {
      await rest(`notifications?recipient_id=eq.${need()}&read_at=is.null`, { method: "PATCH", headers: { Prefer: "return=minimal" }, body: JSON.stringify({ read_at: new Date().toISOString() }) });
      return reply({ ok: true });
    }
    const notifRead = route("POST", /^\/api\/notifications\/([0-9a-f-]{36})\/read$/i);
    if (notifRead) {
      await rest(`notifications?id=eq.${notifRead[1]}&recipient_id=eq.${need()}`, { method: "PATCH", headers: { Prefer: "return=minimal" }, body: JSON.stringify({ read_at: new Date().toISOString() }) });
      return reply({ ok: true });
    }

    // ---------- Mensagens ----------
    if (method === "GET" && path === "/api/inbox") {
      const id = need();
      const blocks = new Set(await myBlocks());
      const rows = await rest(`messages?or=(sender_id.eq.${id},recipient_id.eq.${id})&select=*&order=created_at.desc&limit=300`);
      const latest = new Map<string, any>();
      for (const m of rows || []) { const peer = m.sender_id === id ? m.recipient_id : m.sender_id; if (!blocks.has(peer) && !latest.has(peer)) latest.set(peer, m); }
      const profiles = await profilesByIds([...latest.keys()]);
      const unread = await rest(`messages?recipient_id=eq.${id}&read_at=is.null&select=sender_id`);
      const unreadCounts = new Map<string, number>();
      for (const m of unread || []) unreadCounts.set(m.sender_id, (unreadCounts.get(m.sender_id) || 0) + 1);
      return reply({ conversations: [...latest.entries()].map(([pid, m]) => { const p = profiles.get(pid) || {}; return { username: p.username || "", displayName: p.display_name || "", avatarUrl: pub(p.avatar_path), body: previewOf(m.body), createdAt: m.created_at, unreadCount: unreadCounts.get(pid) || 0 }; }) });
    }
    const msg = path.match(/^\/api\/messages\/([^/]+)$/);
    if (msg) {
      const id = need();
      const peerName = decodeURIComponent(msg[1]).replace(/^@/, "").toLowerCase();
      const peer = (await rest(`profiles?username=eq.${encodeURIComponent(peerName)}&select=id`))?.[0];
      if (!peer) return method === "GET" ? reply({ messages: [] }) : reply({ error: "Perfil não encontrado." }, 404);
      if (method === "GET") {
        await rest(`messages?sender_id=eq.${peer.id}&recipient_id=eq.${id}&read_at=is.null`, { method: "PATCH", headers: { Prefer: "return=minimal" }, body: JSON.stringify({ read_at: new Date().toISOString() }) });
        const rows = await rest(`messages?or=(and(sender_id.eq.${id},recipient_id.eq.${peer.id}),and(sender_id.eq.${peer.id},recipient_id.eq.${id}))&select=*&order=created_at.asc&limit=500`);
        const dec = await decodeMarks(rows || []);
        return reply({ messages: (rows || []).map((m: any) => ({ id: m.id, senderId: m.sender_id, createdAt: m.created_at, ...dec(m) })) });
      }
      if (method === "POST") {
        const text = String(body.body || "").trim();
        if (!text || text.length > 10000) return reply({ error: "A mensagem precisa ter entre 1 e 10000 caracteres." }, 400);
        if (peer.id === id) return reply({ error: "Você não pode enviar mensagem para sua própria conta." }, 400);
        const row = (await ins("messages", { sender_id: id, recipient_id: peer.id, body: text }))?.[0];
        const dec = await decodeMarks([row]);
        return reply({ message: { id: row.id, senderId: row.sender_id, createdAt: row.created_at, ...dec(row) } }, 201);
      }
    }
    if (method === "POST" && path === "/api/chat-polls") {
      const id = need();
      const question = String(body.question || "").trim().slice(0, 300);
      const options = (Array.isArray(body.options) ? body.options : []).map((o: any) => String(o).trim()).filter(Boolean).slice(0, 6);
      if (!question || options.length < 2) return reply({ error: "A enquete precisa de uma pergunta e pelo menos 2 opções." }, 400);
      const row = (await ins("chat_polls", { creator_id: id, question, options }))?.[0];
      return reply({ id: row.id, question, options }, 201);
    }
    const chatVote = route("POST", /^\/api\/chat-polls\/([0-9a-f-]{36})\/vote$/i);
    if (chatVote) {
      const id = need();
      const poll = (await rest(`chat_polls?id=eq.${chatVote[1]}&select=*`))?.[0];
      if (!poll) return reply({ error: "Enquete não encontrada." }, 404);
      const idx = Number(body.optionIndex);
      if (!Number.isInteger(idx) || idx < 0 || idx >= poll.options.length) return reply({ error: "Opção inválida." }, 400);
      await ins("chat_poll_votes?on_conflict=poll_id,user_id", { poll_id: poll.id, user_id: id, option_index: idx }, "resolution=merge-duplicates,return=minimal", "");
      const votes = (await rest(`chat_poll_votes?poll_id=eq.${poll.id}&select=option_index`)) || [];
      return reply({ votes: poll.options.map((_: any, i: number) => votes.filter((v: any) => v.option_index === i).length), myVote: idx });
    }

    // ---------- Figurinhas ----------
    if (method === "GET" && path === "/api/stickers") {
      const rows = await rest(`user_stickers?owner_id=eq.${need()}&select=*&order=created_at.desc&limit=200`);
      return reply({ stickers: (rows || []).map((s: any) => ({ id: s.id, path: s.path, url: pub(s.path) })) });
    }
    if (method === "POST" && path === "/api/stickers") {
      const id = need();
      await fileOwned(String(body.fileId || ""), "sticker");
      const row = (await ins("user_stickers", { owner_id: id, path: String(body.fileId) }))?.[0];
      return reply({ sticker: { id: row.id, path: row.path, url: pub(row.path) } }, 201);
    }
    const stickerDel = route("DELETE", /^\/api\/stickers\/([0-9a-f-]{36})$/i);
    if (stickerDel) { await rest(`user_stickers?id=eq.${stickerDel[1]}&owner_id=eq.${need()}`, { method: "DELETE" }); return reply({ ok: true }); }

    // ---------- Stories e notas ----------
    if (method === "GET" && path === "/api/stories") {
      const id = need();
      const following = [...await myFollowing()];
      const ids = [id, ...following];
      const nowIso = encodeURIComponent(new Date().toISOString());
      const [stories, notes, views] = await Promise.all([
        rest(`stories?user_id=in.(${inList(ids)})&expires_at=gt.${nowIso}&select=*&order=created_at.asc`),
        rest(`notes?user_id=in.(${inList(ids)})&expires_at=gt.${nowIso}&select=*`),
        rest(`story_views?select=story_id,viewer_id&limit=5000`),
      ]);
      const profiles = await profilesByIds(ids);
      const entries = ids.map((uid) => {
        const p = profiles.get(uid); if (!p) return null;
        const mine = (stories || []).filter((s: any) => s.user_id === uid).map((s: any) => ({
          id: s.id, mediaUrl: pub(s.media_path), mediaType: s.media_type, caption: s.caption || "", createdAt: s.created_at,
          seen: (views || []).some((v: any) => v.story_id === s.id && v.viewer_id === id), views: (views || []).filter((v: any) => v.story_id === s.id).length,
        }));
        const note = (notes || []).find((n: any) => n.user_id === uid);
        if (uid !== id && !mine.length && !note) return null;
        return { username: p.username, displayName: p.display_name || "", avatarUrl: pub(p.avatar_path), mine: uid === id, note: note ? { body: note.body } : null, stories: mine, hasUnseen: mine.some((s: any) => !s.seen && uid !== id) };
      }).filter(Boolean) as any[];
      entries.sort((a, b) => (a.mine ? -1 : 0) - (b.mine ? -1 : 0) || Number(b.hasUnseen) - Number(a.hasUnseen));
      return reply({ tray: entries });
    }
    if (method === "POST" && path === "/api/stories") {
      const id = need();
      const mediaType = body.mediaType === "video" ? "video" : "image";
      const { mime } = await fileOwned(String(body.mediaFileId || ""), "story_media");
      if (!mime.startsWith(mediaType + "/")) return reply({ error: "A mídia não corresponde ao tipo do story." }, 422);
      const row = (await ins("stories", { user_id: id, media_path: String(body.mediaFileId), media_type: mediaType, caption: String(body.caption || "").slice(0, 300), expires_at: new Date(Date.now() + 24 * 3600_000).toISOString() }))?.[0];
      return reply({ story: { id: row.id } }, 201);
    }
    const storyM = path.match(/^\/api\/stories\/([0-9a-f-]{36})(?:\/(view|views))?$/i);
    if (storyM) {
      const sid = storyM[1];
      const id = need();
      if (method === "DELETE" && !storyM[2]) { await rest(`stories?id=eq.${sid}&user_id=eq.${id}`, { method: "DELETE" }); return reply({ ok: true }); }
      if (method === "POST" && storyM[2] === "view") {
        await ins("story_views?on_conflict=story_id,viewer_id", { story_id: sid, viewer_id: id }, "resolution=ignore-duplicates,return=minimal", "");
        return reply({ ok: true });
      }
      if (method === "GET" && storyM[2] === "views") {
        const own = (await rest(`stories?id=eq.${sid}&user_id=eq.${id}&select=id`))?.[0];
        if (!own) return reply({ viewers: [] });
        const rows = await rest(`story_views?story_id=eq.${sid}&select=viewer_id,viewed_at&order=viewed_at.desc&limit=200`);
        const profiles = await profilesByIds((rows || []).map((r: any) => r.viewer_id));
        return reply({ viewers: (rows || []).map((r: any) => { const p = profiles.get(r.viewer_id) || {}; return { username: p.username || "", displayName: p.display_name || "", avatarUrl: pub(p.avatar_path), viewedAt: r.viewed_at }; }) });
      }
    }
    if (method === "POST" && path === "/api/notes") {
      const id = need();
      const text = String(body.body || "").trim().slice(0, 120);
      if (!text) return reply({ error: "Escreva a nota." }, 400);
      await ins("notes?on_conflict=user_id", { user_id: id, body: text, created_at: new Date().toISOString(), expires_at: new Date(Date.now() + 24 * 3600_000).toISOString() }, "resolution=merge-duplicates,return=minimal", "");
      return reply({ ok: true });
    }
    if (method === "DELETE" && path === "/api/notes") { await rest(`notes?user_id=eq.${need()}`, { method: "DELETE" }); return reply({ ok: true }); }

    // ---------- Lives ----------
    const liveSend = route("POST", /^\/api\/lives\/([^/]+)\/comments$/);
    if (liveSend) {
      const id = need();
      const text = String(body.body || "").trim().slice(0, 300);
      if (!text) return reply({ error: "Escreva um comentário." }, 400);
      await ins("live_comments", { live_host: decodeURIComponent(liveSend[1]).replace(/^@/, "").toLowerCase(), user_id: id, body: text }, "return=minimal", "");
      return reply({ ok: true }, 201);
    }

    // ---------- Comunidades ----------
    if (method === "POST" && path === "/api/communities") {
      const id = need();
      const name = String(body.name || "").trim();
      if (name.length < 2 || name.length > 60) return reply({ error: "O nome precisa ter entre 2 e 60 caracteres." }, 400);
      const row = (await ins("communities", { owner_id: id, name, description: String(body.description || "").trim().slice(0, 500), emoji: String(body.emoji || "👥").slice(0, 8) || "👥", visibility: "public" }))?.[0];
      return reply({ community: communityDto(row, [], await me()) }, 201);
    }
    const memM = path.match(/^\/api\/communities\/([0-9a-f-]{36})\/members\/([0-9a-f-]{36})$/i);
    if (memM && method === "POST") {
      const me_ = need(), cid = memM[1], uid = memM[2];
      const c = await canUseCommunity(cid);
      const mem = (await rest(`community_members?community_id=eq.${cid}&select=user_id,role`)) || [];
      const myRole = c.owner_id === me_ ? "owner" : mem.find((m: any) => m.user_id === me_)?.role;
      if (myRole !== "owner" && myRole !== "admin") return reply({ error: "Só o dono ou os admins podem fazer isso." }, 403);
      if (uid === c.owner_id) return reply({ error: "O dono da comunidade não pode ser alterado." }, 400);
      const target = mem.find((m: any) => m.user_id === uid);
      if (!target) return reply({ error: "Essa pessoa não está na comunidade." }, 404);
      const action = String(body.action || "");
      if (action === "approve") {
        if (target.role !== "pending") return reply({ ok: true });
        await rest(`community_members?community_id=eq.${cid}&user_id=eq.${uid}`, { method: "PATCH", headers: { Prefer: "return=minimal" }, body: JSON.stringify({ role: "member" }) });
        return reply({ ok: true });
      }
      if (action === "reject" || action === "remove") {
        if (target.role === "admin" && myRole !== "owner") return reply({ error: "Só o dono pode remover um admin." }, 403);
        await rest(`community_members?community_id=eq.${cid}&user_id=eq.${uid}`, { method: "DELETE" });
        return reply({ ok: true });
      }
      if (action === "admin" || action === "member") {
        if (myRole !== "owner") return reply({ error: "Só o dono pode escolher os admins." }, 403);
        if (target.role === "pending") return reply({ error: "Aprove a pessoa primeiro." }, 400);
        await rest(`community_members?community_id=eq.${cid}&user_id=eq.${uid}`, { method: "PATCH", headers: { Prefer: "return=minimal" }, body: JSON.stringify({ role: action }) });
        return reply({ ok: true });
      }
      return reply({ error: "Ação inválida." }, 400);
    }
    const comM = path.match(/^\/api\/communities\/([0-9a-f-]{36})(?:\/(join|posts)(?:\/([0-9a-f-]{36}))?)?$/i);
    if (comM) {
      const cid = comM[1], sub = comM[2], postId = comM[3];
      const c = await canUseCommunity(cid);
      const members = (await rest(`community_members?community_id=eq.${cid}&select=community_id,user_id,role`)) || [];
      const owner = (await profilesByIds([c.owner_id])).get(c.owner_id);
      const dto = communityDto(c, members, owner);
      if (method === "GET" && !sub) {
        const rows = (await rest(`community_messages?community_id=eq.${cid}&select=*&order=created_at.desc&limit=200`)) || [];
        rows.reverse();
        const profiles = await profilesByIds(rows.map((r: any) => r.user_id));
        const dec = await decodeMarks(rows);
        const canSeePending = dto.isAdmin;
        const memberIds = [c.owner_id, ...members.filter((m: any) => m.role !== "pending" || canSeePending).map((m: any) => m.user_id)];
        const memberProfiles = await profilesByIds(memberIds);
        const seenIds = new Set<string>();
        const memberList = memberIds.filter((u: string) => !seenIds.has(u) && seenIds.add(u)).map((u: string) => {
          const role = u === c.owner_id ? "owner" : (members.find((m: any) => m.user_id === u)?.role || "member");
          return { userId: u, role, ...authorDto(memberProfiles.get(u)) };
        });
        return reply({ community: dto, memberList, posts: rows.map((r: any) => ({ id: r.id, createdAt: r.created_at, mine: r.user_id === viewerId, author: authorDto(profiles.get(r.user_id)), ...dec(r) })) });
      }
      const id = need();
      if (method === "PATCH" && !sub) {
        if (c.owner_id !== id) return reply({ error: "Só quem criou a comunidade pode editá-la." }, 403);
        const patch: any = {};
        if (body.name !== undefined) {
          const n = String(body.name).trim();
          if (n.length < 2 || n.length > 60) return reply({ error: "O nome precisa ter entre 2 e 60 caracteres." }, 400);
          patch.name = n;
        }
        if (body.description !== undefined) patch.description = String(body.description).trim().slice(0, 500);
        if (body.rules !== undefined) patch.rules = String(body.rules).trim().slice(0, 2000);
        if (body.joinMode !== undefined) { if (!["open", "approval"].includes(String(body.joinMode))) return reply({ error: "Tipo de entrada inválido." }, 400); patch.join_mode = String(body.joinMode); }
        if (body.writeMode !== undefined) { if (!["all", "admins"].includes(String(body.writeMode))) return reply({ error: "Opção de escrita inválida." }, 400); patch.write_mode = String(body.writeMode); }
        if (body.avatarFileId !== undefined) {
          const fid = String(body.avatarFileId);
          if (!safeObjectPath(fid) || !fid.startsWith(`${id}/avatar/`)) return reply({ error: "Arquivo inválido para esta conta." }, 403);
          if (!(await getStoredObjectInfo(fid))) return reply({ error: "A foto ainda não foi enviada. Tente de novo." }, 422);
          patch.avatar_path = fid;
        }
        if (!Object.keys(patch).length) return reply({ community: dto });
        try {
          const rows = await rest(`communities?id=eq.${cid}&owner_id=eq.${id}&select=*`, { method: "PATCH", headers: { Prefer: "return=representation" }, body: JSON.stringify(patch) });
          return reply({ community: communityDto(rows?.[0] || { ...c, ...patch }, members, owner) });
        } catch (e) {
          if (String(e).includes("UNIQUE") || String(e).includes("duplicate")) return reply({ error: "Já existe uma comunidade com esse nome." }, 409);
          throw e;
        }
      }
      if (method === "DELETE" && !sub) {
        if (c.owner_id !== id) return reply({ error: "Só quem criou a comunidade pode excluí-la." }, 403);
        await rest(`communities?id=eq.${cid}&owner_id=eq.${id}`, { method: "DELETE" });
        return reply({ ok: true });
      }
      if (method === "POST" && sub === "join") {
        if (c.owner_id === id) return reply({ joined: true, members: dto.members });
        const existing = members.find((m: any) => m.user_id === id);
        if (existing) { await rest(`community_members?community_id=eq.${cid}&user_id=eq.${id}`, { method: "DELETE" }); return reply({ joined: false, pending: false, members: Math.max(dto.members - (existing.role === "pending" ? 0 : 1), 1) }); }
        const approval = (c.join_mode || "open") === "approval";
        await ins("community_members", { community_id: cid, user_id: id, role: approval ? "pending" : "member" }, "return=minimal", "");
        return reply({ joined: !approval, pending: approval, members: approval ? dto.members : dto.members + 1 });
      }
      if (method === "POST" && sub === "posts") {
        const text = String(body.body || "").trim();
        if (!text || text.length > 10000) return reply({ error: "A mensagem precisa ter entre 1 e 10000 caracteres." }, 400);
        if (!dto.joined) return reply({ error: "Entre na comunidade para participar da conversa." }, 403);
        if ((c.write_mode || "all") === "admins" && !dto.isAdmin) return reply({ error: "Só admins podem escrever nesta comunidade." }, 403);
        const row = (await ins("community_messages", { community_id: cid, user_id: id, body: text }))?.[0];
        const dec = await decodeMarks([row]);
        return reply({ post: { id: row.id, createdAt: row.created_at, mine: true, author: authorDto(await me()), ...dec(row) } }, 201);
      }
      if (method === "DELETE" && sub === "posts" && postId) {
        await rest(`community_messages?id=eq.${postId}&community_id=eq.${cid}`, { method: "DELETE" });
        return reply({ ok: true });
      }
    }

    // ---------- Perfil público (por último: casa qualquer /api/users/<nome>) ----------
    const hlList = route("GET", /^\/api\/users\/([^/]+)\/highlights$/);
    if (hlList) {
      const name = decodeURIComponent(hlList[1]).replace(/^@/, "").toLowerCase();
      const target = (await rest(`profiles?username=eq.${encodeURIComponent(name)}&select=id`))?.[0];
      if (!target) return reply({ highlights: [] });
      const rows = await rest(`story_highlights?user_id=eq.${target.id}&select=*&order=created_at.desc&limit=100`);
      return reply({ highlights: (rows || []).map((h: any) => ({ id: h.id, title: h.title, mediaUrl: pub(h.media_path), mediaType: h.media_type, caption: h.caption || "", createdAt: h.created_at })) });
    }
    if (method === "POST" && path === "/api/highlights") {
      const id = need();
      const sid = String(body.storyId || "");
      if (!isUuid(sid)) return reply({ error: "Story inválido." }, 400);
      const story = (await rest(`stories?id=eq.${sid}&user_id=eq.${id}&select=media_path,media_type,caption`))?.[0];
      if (!story) return reply({ error: "Story não encontrado." }, 404);
      const title = String(body.title || "").trim().slice(0, 24) || "Destaques";
      const row = (await ins("story_highlights", { user_id: id, title, media_path: story.media_path || "", media_type: story.media_type, caption: story.caption || "" }))?.[0];
      return reply({ highlight: { id: row.id, title: row.title, mediaUrl: pub(row.media_path), mediaType: row.media_type, caption: row.caption || "", createdAt: row.created_at } }, 201);
    }
    const hlDel = route("DELETE", /^\/api\/highlights\/([0-9a-f-]{36})$/i);
    if (hlDel) { await rest(`story_highlights?id=eq.${hlDel[1]}&user_id=eq.${need()}`, { method: "DELETE" }); return reply({ ok: true }); }

    const profM = route("GET", /^\/api\/users\/([^/]+)$/);
    if (profM) {
      const name = decodeURIComponent(profM[1]).replace(/^@/, "").toLowerCase();
      const p = (await rest(`profiles?username=eq.${encodeURIComponent(name)}&select=*`))?.[0];
      if (!p) return reply({ error: "Perfil não encontrado." }, 404);
      const [followers, following, rows] = await Promise.all([
        rest(`follows?following_id=eq.${p.id}&select=follower_id`),
        rest(`follows?follower_id=eq.${p.id}&select=following_id`),
        rest(`posts?user_id=eq.${p.id}&archived_at=is.null&select=${POST_SELECT}&order=created_at.desc&limit=60`),
      ]);
      const posts = await buildPosts(visiblePolls(rows || []));
      return reply({ user: { ...profileDto(p), cover_path: p.cover_path || "", followers: (followers || []).length, following: (following || []).length, posts_count: posts.length, isFollowing: !!viewerId && (followers || []).some((f: any) => f.follower_id === viewerId), profileFrame: p.profile_frame ?? 0 }, posts });
    }
    return reply({ error: "Rota não encontrada.", path }, 404);
  } catch (e) {
    if (e instanceof Http) return reply({ error: e.message }, e.status);
    if (e instanceof PgError) return reply({ error: e.pgMessage }, [400, 401, 403, 404, 409, 422].includes(e.status) ? e.status : 500);
    const detail = e instanceof Error ? e.message : String(e);
    console.error("QINBLY gateway:", detail);
    return reply({ error: detail || "Erro desconhecido no servidor." }, 500);
  }
}
