// Rotas auxiliares: entrar com nome de usuario, pedido de verificacao, trocar senha/e-mail, arquivos (R2) e a camada /rest/v1.
import { preflight, reply, Http, tokenOf, authUser, hashPassword, checkPassword, signOutOthers, safeObjectPath, CORS, nowIso, PUBLIC_PREFIX } from "./core.js";
import { makePg, PgError } from "./pg.js";

const FORGOT_MESSAGE = "Se existir uma conta com esses dados, enviamos o link para criar uma nova senha.";

// ---------- /functions/v1/qinbly-login : entrar com nome de usuario ----------
export async function handleLogin(req, env, handleApi) {
  if (req.method === "OPTIONS") return preflight();
  if (req.method !== "POST") return reply({ error: "Método não permitido." }, 405);
  try {
    const body = await req.json().catch(() => ({}));
    const uname = String(body.username || "").trim().toLowerCase().replace(/^@/, "");
    const password = String(body.password || "");
    const forgot = body.action === "forgot";
    const wrong = () => reply({ error: "Usuário ou senha incorretos." }, 400);
    if (!/^[a-z0-9_.]{3,30}$/.test(uname) || (!forgot && !password)) return forgot ? reply({ ok: true, message: FORGOT_MESSAGE }) : wrong();
    if (forgot) return reply({ ok: true, message: FORGOT_MESSAGE });
    const row = await env.DB.prepare("SELECT a.email AS email FROM profiles p JOIN accounts a ON a.id = p.id WHERE lower(p.username) = ?").bind(uname).first();
    if (!row?.email) return wrong();
    const inner = new Request(new URL("/functions/v1/qinbly-api/api/auth/login", req.url), {
      method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ email: row.email, password }),
    });
    const res = await handleApi(inner, env);
    if (res.status === 400 || res.status === 401) return wrong();
    return res;
  } catch (e) {
    return reply({ error: "Não foi possível entrar agora. Tente de novo." }, 500);
  }
}

// ---------- /functions/v1/qinbly-verify : pedido de selo de verificacao ----------
export async function handleVerify(req, env) {
  if (req.method === "OPTIONS") return preflight();
  try {
    const user = await authUser(env, tokenOf(req));
    if (!user) return reply({ error: "Entre na sua conta para pedir verificação." }, 401);
    const id = user.id;
    const pg = makePg(env, id);
    const path = new URL(req.url).pathname.replace(/\/$/, "");
    const profile = (await pg.rest(`profiles?id=eq.${id}&select=id,bio,avatar_path,cover_path,is_verified`))?.[0];
    const last = (await pg.rest(`verification_requests?user_id=eq.${id}&select=id,status,review_note,created_at,reviewed_at&order=created_at.desc&limit=1`))?.[0] || null;
    if (req.method === "GET") return reply({ verified: !!profile?.is_verified, request: last });
    if (req.method === "POST" && path.endsWith("/request")) {
      if (profile?.is_verified) return reply({ error: "Sua conta já é verificada." }, 409);
      if (last?.status === "pending") return reply({ error: "Você já tem um pedido em análise." }, 409);
      const body = await req.json().catch(() => ({}));
      const reason = String(body.reason || "").trim();
      if (reason.length < 10 || reason.length > 1000) return reply({ error: "Explique em 10 a 1000 caracteres por que sua conta deve ser verificada." }, 400);
      const missing = [];
      if (!profile?.avatar_path && !profile?.cover_path) missing.push("foto de perfil");
      if (!String(profile?.bio || "").trim()) missing.push("bio");
      const posts = await pg.rest(`posts?user_id=eq.${id}&select=id&limit=1`);
      if (!posts?.length) missing.push("pelo menos 1 publicação");
      if (missing.length) return reply({ error: `Para pedir verificação, complete: ${missing.join(", ")}.` }, 400);
      await pg.rest("verification_requests", { method: "POST", headers: { Prefer: "return=minimal" }, body: JSON.stringify([{ user_id: id, reason, status: "pending" }]) });
      return reply({ ok: true, status: "pending" }, 201);
    }
    return reply({ error: "Rota não encontrada." }, 404);
  } catch (e) {
    return reply({ error: e instanceof PgError ? e.pgMessage : (e instanceof Error ? e.message : String(e)) }, e instanceof Http ? e.status : 500);
  }
}

// ---------- /auth/v1 : trocar senha / e-mail, sair dos outros aparelhos ----------
export async function handleAuthV1(req, env, subpath) {
  if (req.method === "OPTIONS") return preflight();
  try {
    const user = await authUser(env, tokenOf(req));
    if (!user) return reply({ msg: "Sua sessão expirou. Entre novamente." }, 401);
    if (subpath === "user" && req.method === "PUT") {
      const body = await req.json().catch(() => ({}));
      if (body.password !== undefined) {
        const pw = String(body.password);
        if (pw.length < 6) return reply({ msg: "A senha precisa ter pelo menos 6 caracteres." }, 422);
        await env.DB.prepare("UPDATE accounts SET password_hash = ? WHERE id = ?").bind(await hashPassword(pw), user.id).run();
      }
      if (body.email !== undefined) {
        const email = String(body.email).trim().toLowerCase();
        if (!/^[^@\s]+@[^@\s]+\.[^@\s]+$/.test(email)) return reply({ msg: "Informe um e-mail válido." }, 422);
        const taken = await env.DB.prepare("SELECT id FROM accounts WHERE lower(email) = ? AND id <> ?").bind(email, user.id).first();
        if (taken) return reply({ msg: "Esse e-mail já está em uso por outra conta." }, 422);
        await env.DB.prepare("UPDATE accounts SET email = ? WHERE id = ?").bind(email, user.id).run();
      }
      return reply({ id: user.id });
    }
    if (subpath === "logout" && req.method === "POST") {
      const scope = new URL(req.url).searchParams.get("scope");
      if (scope === "others") await signOutOthers(env, user.id, user.sid);
      else await env.DB.prepare("DELETE FROM refresh_tokens WHERE account_id = ? AND COALESCE(sid,'') = ?").bind(user.id, user.sid || "").run();
      return new Response(null, { status: 204, headers: CORS });
    }
    return reply({ msg: "Rota não encontrada." }, 404);
  } catch (e) {
    return reply({ msg: e instanceof Error ? e.message : String(e) }, 500);
  }
}

// ---------- /storage/v1 : envio e leitura de arquivos no R2 ----------
const MAX_UPLOAD = 100 * 1024 * 1024;
export async function handleStorage(req, env, subpath, waitUntil) {
  if (req.method === "OPTIONS") return preflight();
  // Leitura publica: object/public/post-media/<caminho>
  const pubMatch = subpath.match(/^object\/public\/post-media\/(.+)$/);
  if (pubMatch && (req.method === "GET" || req.method === "HEAD")) {
    const key = safeKey(decodeURIComponent(pubMatch[1]));
    if (!key) return new Response("Not found", { status: 404, headers: CORS });
    return serveObject(req, env, key, waitUntil);
  }
  // Envio: object/post-media/<caminho> (PUT ou POST) — so dentro da pasta do proprio usuario
  const putMatch = subpath.match(/^object\/post-media\/(.+)$/);
  if (putMatch && (req.method === "PUT" || req.method === "POST")) {
    try {
      const user = await authUser(env, tokenOf(req));
      if (!user) return reply({ message: "Sua sessão expirou. Entre novamente.", statusCode: "401" }, 401);
      const key = safeKey(decodeURIComponent(putMatch[1]));
      if (!key || !key.startsWith(user.id + "/")) return reply({ message: "O arquivo precisa ficar na pasta da sua conta.", statusCode: "403" }, 403);
      const len = Number(req.headers.get("content-length") || 0);
      if (!len || len > MAX_UPLOAD) return reply({ message: "Arquivo vazio ou maior que 100 MB.", statusCode: "413" }, 413);
      const type = (req.headers.get("content-type") || "application/octet-stream").split(";")[0].trim().toLowerCase();
      await env.FILES.put(key, req.body, { httpMetadata: { contentType: type } });
      return reply({ Key: "post-media/" + key, Id: key });
    } catch (e) {
      return reply({ message: e instanceof Error ? e.message : String(e), statusCode: "500" }, 500);
    }
  }
  return reply({ message: "Rota de arquivos não encontrada.", statusCode: "404" }, 404);
}
const safeKey = (k) => (safeObjectPath(k) ? k : null);
const EDGE_MAX = 6 * 1024 * 1024; // fotos entram no cache da borda da Cloudflare; vídeos grandes seguem direto do R2
const serveObject = async (req, env, key, waitUntil) => {
  const range = req.headers.get("range");
  // Foto já vista por alguém perto de você sai do cache da borda: sem consultar o R2 e sem esperar.
  const cacheable = req.method === "GET" && !range && typeof caches !== "undefined" && caches.default;
  const cacheKey = cacheable ? new Request(new URL(req.url).origin + new URL(req.url).pathname, { method: "GET" }) : null;
  if (cacheable) {
    try { const hit = await caches.default.match(cacheKey); if (hit) return hit; } catch (_) {}
  }
  const res = await serveObjectRaw(req, env, key);
  if (cacheable && res.status === 200 && Number(res.headers.get("Content-Length") || 0) <= EDGE_MAX && (res.headers.get("Content-Type") || "").startsWith("image/")) {
    try { const put = caches.default.put(cacheKey, res.clone()); if (waitUntil) waitUntil(put); else await put; } catch (_) {}
  }
  return res;
};
const serveObjectRaw = async (req, env, key) => {
  const range = req.headers.get("range");
  const head = await env.FILES.head(key);
  if (!head) return new Response("Not found", { status: 404, headers: CORS });
  const headers = new Headers(CORS);
  headers.set("Content-Type", head.httpMetadata?.contentType || "application/octet-stream");
  headers.set("Cache-Control", "public, max-age=31536000, immutable");
  headers.set("Accept-Ranges", "bytes");
  headers.set("ETag", head.httpEtag);
  if (req.headers.get("if-none-match") === head.httpEtag) return new Response(null, { status: 304, headers });
  const size = head.size;
  if (range) {
    const m = range.match(/^bytes=(\d*)-(\d*)$/);
    if (m && (m[1] || m[2])) {
      let start, end;
      if (m[1] === "") { const n = Number(m[2]); start = Math.max(0, size - n); end = size - 1; }
      else { start = Number(m[1]); end = m[2] ? Math.min(Number(m[2]), size - 1) : size - 1; }
      if (start >= size || start > end) { headers.set("Content-Range", `bytes */${size}`); return new Response(null, { status: 416, headers }); }
      headers.set("Content-Range", `bytes ${start}-${end}/${size}`);
      headers.set("Content-Length", String(end - start + 1));
      if (req.method === "HEAD") return new Response(null, { status: 206, headers });
      const obj = await env.FILES.get(key, { range: { offset: start, length: end - start + 1 } });
      return new Response(obj.body, { status: 206, headers });
    }
  }
  headers.set("Content-Length", String(size));
  if (req.method === "HEAD") return new Response(null, { status: 200, headers });
  const obj = await env.FILES.get(key);
  return new Response(obj.body, { status: 200, headers });
};

// ---------- /rest/v1 : consultas diretas do app (com as mesmas regras de acesso do banco antigo) ----------
export async function handleRestV1(req, env, subpath) {
  if (req.method === "OPTIONS") return preflight();
  try {
    const user = await authUser(env, tokenOf(req));
    const pg = makePg(env, user?.id || null);
    const url = new URL(req.url);
    const prefer = req.headers.get("prefer") || "";
    const raw = subpath + (url.search || "");
    const bodyText = ["POST", "PATCH", "PUT"].includes(req.method) ? await req.text() : "";
    const result = await pg.rest(raw, { method: req.method, headers: { Prefer: prefer }, body: bodyText || undefined });
    if (result === null || result === undefined) {
      return new Response(null, { status: req.method === "POST" ? 201 : 204, headers: CORS });
    }
    return new Response(JSON.stringify(result), { status: req.method === "POST" ? 201 : 200, headers: { ...CORS, "Content-Type": "application/json; charset=utf-8" } });
  } catch (e) {
    if (e instanceof PgError) {
      return new Response(JSON.stringify({ code: e.code, message: e.pgMessage, details: null, hint: null }), { status: e.status, headers: { ...CORS, "Content-Type": "application/json; charset=utf-8" } });
    }
    return reply({ code: "XX000", message: e instanceof Error ? e.message : String(e) }, 500);
  }
}
