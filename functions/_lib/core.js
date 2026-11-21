// Base comum do servidor QINBLY no Cloudflare: respostas, sessao (JWT), senhas e login do Google.
// Banco: D1 (env.DB). Arquivos: R2 (env.FILES).

export const CORS = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, apikey, content-type, x-client-info, prefer, range, x-upsert",
  "Access-Control-Allow-Methods": "GET, POST, PATCH, PUT, DELETE, OPTIONS, HEAD",
  "Access-Control-Expose-Headers": "content-range, content-length, accept-ranges",
};
export const JSON_HEADERS = { ...CORS, "Content-Type": "application/json; charset=utf-8" };
export const reply = (data, status = 200) => new Response(JSON.stringify(data), { status, headers: JSON_HEADERS });
export const preflight = () => new Response(null, { status: 204, headers: CORS });

export class Http extends Error {
  constructor(status, message) { super(message); this.status = status; }
}

export const UUID_RE = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;
export const nowIso = () => new Date().toISOString();
export const uuid = () => crypto.randomUUID();

// ---------- base64url ----------
const enc = new TextEncoder();
const dec = new TextDecoder();
export const b64u = (bytes) => {
  let s = "";
  const a = bytes instanceof Uint8Array ? bytes : new Uint8Array(bytes);
  for (let i = 0; i < a.length; i++) s += String.fromCharCode(a[i]);
  return btoa(s).replace(/\+/g, "-").replace(/\//g, "_").replace(/=+$/, "");
};
export const unb64u = (str) => {
  const s = String(str).replace(/-/g, "+").replace(/_/g, "/");
  const bin = atob(s + "=".repeat((4 - (s.length % 4)) % 4));
  const out = new Uint8Array(bin.length);
  for (let i = 0; i < bin.length; i++) out[i] = bin.charCodeAt(i);
  return out;
};
const sha256Hex = async (text) => {
  const h = await crypto.subtle.digest("SHA-256", enc.encode(text));
  return [...new Uint8Array(h)].map((b) => b.toString(16).padStart(2, "0")).join("");
};

// ---------- segredo do servidor (guardado no proprio banco, so o servidor le) ----------
let secretCache = null;
const getSecret = async (env) => {
  if (secretCache) return secretCache;
  await env.DB.exec("CREATE TABLE IF NOT EXISTS app_secrets (k TEXT PRIMARY KEY, v TEXT NOT NULL)");
  let row = await env.DB.prepare("SELECT v FROM app_secrets WHERE k='jwt'").first();
  if (!row) {
    const fresh = b64u(crypto.getRandomValues(new Uint8Array(48)));
    await env.DB.prepare("INSERT OR IGNORE INTO app_secrets (k, v) VALUES ('jwt', ?)").bind(fresh).run();
    row = await env.DB.prepare("SELECT v FROM app_secrets WHERE k='jwt'").first();
  }
  secretCache = row.v;
  return secretCache;
};
const hmacKey = async (env) => crypto.subtle.importKey("raw", enc.encode(await getSecret(env)), { name: "HMAC", hash: "SHA-256" }, false, ["sign", "verify"]);

export const signJwt = async (env, payload) => {
  const head = b64u(enc.encode(JSON.stringify({ alg: "HS256", typ: "JWT" })));
  const body = b64u(enc.encode(JSON.stringify(payload)));
  const sig = await crypto.subtle.sign("HMAC", await hmacKey(env), enc.encode(`${head}.${body}`));
  return `${head}.${body}.${b64u(sig)}`;
};
export const verifyJwt = async (env, token) => {
  const parts = String(token || "").split(".");
  if (parts.length !== 3) return null;
  try {
    const ok = await crypto.subtle.verify("HMAC", await hmacKey(env), unb64u(parts[2]), enc.encode(`${parts[0]}.${parts[1]}`));
    if (!ok) return null;
    const payload = JSON.parse(dec.decode(unb64u(parts[1])));
    if (!payload.exp || payload.exp * 1000 < Date.now()) return null;
    return payload;
  } catch { return null; }
};

export const tokenOf = (req) => (req.headers.get("authorization") || "").replace(/^Bearer\s+/i, "").trim();

/** Usuario logado a partir do token Bearer, ou null. */
export const authUser = async (env, token) => {
  if (!token) return null;
  const p = await verifyJwt(env, token);
  if (!p || p.role !== "authenticated" || !p.sub) return null;
  const acc = await env.DB.prepare("SELECT id, email FROM accounts WHERE id = ?").bind(p.sub).first();
  if (!acc) return null;
  return { id: acc.id, email: acc.email || "", sid: p.sid || "" };
};

// ---------- senhas (PBKDF2) ----------
const PBKDF2_ITER = 100000;
export const hashPassword = async (password) => {
  const salt = crypto.getRandomValues(new Uint8Array(16));
  const key = await crypto.subtle.importKey("raw", enc.encode(password), "PBKDF2", false, ["deriveBits"]);
  const bits = await crypto.subtle.deriveBits({ name: "PBKDF2", hash: "SHA-256", salt, iterations: PBKDF2_ITER }, key, 256);
  return `pbkdf2$${PBKDF2_ITER}$${b64u(salt)}$${b64u(bits)}`;
};
export const checkPassword = async (password, stored) => {
  const [kind, iterText, saltB64, hashB64] = String(stored || "").split("$");
  if (kind !== "pbkdf2") return false;
  const key = await crypto.subtle.importKey("raw", enc.encode(password), "PBKDF2", false, ["deriveBits"]);
  const bits = await crypto.subtle.deriveBits({ name: "PBKDF2", hash: "SHA-256", salt: unb64u(saltB64), iterations: Number(iterText) || PBKDF2_ITER }, key, 256);
  const a = new Uint8Array(bits), b = unb64u(hashB64);
  if (a.length !== b.length) return false;
  let diff = 0;
  for (let i = 0; i < a.length; i++) diff |= a[i] ^ b[i];
  return diff === 0;
};

// ---------- sessao: token de acesso (1 h) + token de renovacao (180 dias) ----------
const ACCESS_SECONDS = 3600;
const REFRESH_DAYS = 180;

export const issueSession = async (env, account, sid = "") => {
  sid = sid || uuid();
  const now = Math.floor(Date.now() / 1000);
  const token = await signJwt(env, { sub: account.id, email: account.email || "", role: "authenticated", sid, iat: now, exp: now + ACCESS_SECONDS });
  const refreshToken = b64u(crypto.getRandomValues(new Uint8Array(40)));
  await ensureRefreshSid(env);
  await env.DB.prepare("INSERT INTO refresh_tokens (token_hash, account_id, expires_at, sid) VALUES (?, ?, ?, ?)")
    .bind(await sha256Hex(refreshToken), account.id, new Date(Date.now() + REFRESH_DAYS * 86400000).toISOString(), sid).run();
  // limpa tokens vencidos desta conta
  await env.DB.prepare("DELETE FROM refresh_tokens WHERE account_id = ? AND expires_at < ?").bind(account.id, nowIso()).run();
  return { token, refreshToken, sid };
};
let sidColumnChecked = false;
const ensureRefreshSid = async (env) => {
  if (sidColumnChecked) return;
  const cols = await env.DB.prepare("PRAGMA table_info(refresh_tokens)").all();
  if (!(cols.results || []).some((c) => c.name === "sid")) {
    await env.DB.exec("ALTER TABLE refresh_tokens ADD COLUMN sid TEXT");
  }
  sidColumnChecked = true;
};

/** Troca um token de renovacao por uma sessao nova (o token antigo deixa de valer). */
export const refreshSession = async (env, refreshToken) => {
  await ensureRefreshSid(env);
  const hash = await sha256Hex(refreshToken);
  const row = await env.DB.prepare("SELECT account_id, expires_at, sid FROM refresh_tokens WHERE token_hash = ?").bind(hash).first();
  if (!row || row.expires_at < nowIso()) return null;
  const acc = await env.DB.prepare("SELECT id, email FROM accounts WHERE id = ?").bind(row.account_id).first();
  if (!acc) return null;
  await env.DB.prepare("DELETE FROM refresh_tokens WHERE token_hash = ?").bind(hash).run();
  return { account: acc, session: await issueSession(env, acc, row.sid || "") };
};
export const signOutOthers = async (env, accountId, keepSid) => {
  await ensureRefreshSid(env);
  await env.DB.prepare("DELETE FROM refresh_tokens WHERE account_id = ? AND COALESCE(sid,'') <> ?").bind(accountId, keepSid || "").run();
};

// ---------- login do Google (valida o ID token direto com as chaves do Google) ----------
export const GOOGLE_CLIENT_IDS = ["491553779497-bsgfj74l57u0gffi68jv6131joso0o5u.apps.googleusercontent.com"];
let googleKeys = null, googleKeysAt = 0;
const loadGoogleKeys = async () => {
  if (googleKeys && Date.now() - googleKeysAt < 3600_000) return googleKeys;
  const r = await fetch("https://www.googleapis.com/oauth2/v3/certs");
  if (!r.ok) throw new Error("Não foi possível validar o login do Google agora.");
  googleKeys = (await r.json()).keys || [];
  googleKeysAt = Date.now();
  return googleKeys;
};
export const verifyGoogleIdToken = async (idToken) => {
  const parts = String(idToken).split(".");
  if (parts.length !== 3) throw new Http(400, "Credencial Google inválida.");
  const header = JSON.parse(dec.decode(unb64u(parts[0])));
  const payload = JSON.parse(dec.decode(unb64u(parts[1])));
  const jwk = (await loadGoogleKeys()).find((k) => k.kid === header.kid);
  if (!jwk) throw new Http(401, "Chave do Google não reconhecida. Tente entrar de novo.");
  const key = await crypto.subtle.importKey("jwk", jwk, { name: "RSASSA-PKCS1-v1_5", hash: "SHA-256" }, false, ["verify"]);
  const ok = await crypto.subtle.verify("RSASSA-PKCS1-v1_5", key, unb64u(parts[2]), enc.encode(`${parts[0]}.${parts[1]}`));
  if (!ok) throw new Http(401, "Credencial Google recusada.");
  if (!["accounts.google.com", "https://accounts.google.com"].includes(payload.iss)) throw new Http(401, "Credencial Google de origem inválida.");
  if (!GOOGLE_CLIENT_IDS.includes(payload.aud)) throw new Http(401, "Credencial Google emitida para outro aplicativo.");
  if (!payload.exp || payload.exp * 1000 < Date.now()) throw new Http(401, "Credencial Google vencida. Tente entrar de novo.");
  if (!payload.sub) throw new Http(401, "Credencial Google sem identificação.");
  return payload; // sub, email, email_verified, name, picture
};

// ---------- arquivos publicos ----------
export const PUBLIC_PREFIX = "/storage/v1/object/public/post-media/";
const OLD_PUBLIC = "https://jqomgrdlocdqaarfqkay.supabase.co/storage/v1/object/public/post-media/";
export const publicUrl = (origin, p) => {
  if (!p) return "";
  if (p.startsWith(OLD_PUBLIC)) return origin + PUBLIC_PREFIX + p.slice(OLD_PUBLIC.length);
  if (p.startsWith("http")) return p;
  return origin + PUBLIC_PREFIX + p;
};
export const safeObjectPath = (value) =>
  !!value && !value.startsWith("/") && !value.includes("..") && !value.includes("\\") && value.split("/").every((part) => /^[a-zA-Z0-9._-]+$/.test(part));

/** Cria a conta + perfil + dados privados (equivale ao gatilho handle_new_user do banco antigo). */
export const createAccountWithProfile = async (env, { email, googleSub = null, passwordHash = null, username = "", displayName = "" }) => {
  const id = uuid();
  const gen = `user_${id.replaceAll("-", "").slice(0, 8)}`;
  let uname = String(username || "").trim().toLowerCase().replace(/[^a-z0-9_.]/g, "").slice(0, 30);
  if (uname.length < 3) uname = gen;
  const taken = await env.DB.prepare("SELECT 1 AS x FROM profiles WHERE lower(username) = ?").bind(uname).first();
  if (taken) uname = gen;
  const stamp = nowIso();
  await env.DB.batch([
    env.DB.prepare("INSERT INTO accounts (id, email, google_sub, password_hash, created_at) VALUES (?, ?, ?, ?, ?)").bind(id, email || null, googleSub, passwordHash, stamp),
    env.DB.prepare("INSERT INTO profiles (id, username, display_name, bio) VALUES (?, ?, ?, '')").bind(id, uname, String(displayName || "").slice(0, 80)),
    env.DB.prepare("INSERT OR IGNORE INTO private_user_data (user_id) VALUES (?)").bind(id),
  ]);
  return { id, email: email || "" };
};
