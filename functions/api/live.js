// QINBLY · Lives (LiveKit). Função do Cloudflare Pages: https://qinbly.pages.dev/api/live
//
//   POST ?action=token  (Authorization: Bearer <token do usuário do app>)
//        corpo: { "username": "fulano", "host": true }            -> libera transmitir na sala do próprio usuário
//               { "username": "fulano", "host": false, "room": "live_xxx" } -> libera assistir
//   GET  ?action=list   -> lives no ar agora
//
// O login (token) vem do proprio servidor QINBLY (D1), nao mais do Supabase.
//
// Variáveis no painel do Cloudflare (Settings > Variables and Secrets):
//   LIVEKIT_URL         wss://seu-projeto.livekit.cloud
//   LIVEKIT_API_KEY     chave pública do LiveKit
//   LIVEKIT_API_SECRET  chave secreta do LiveKit (Secret / criptografada)
//
// A chave secreta fica só aqui, nunca dentro do app.

import { authUser, tokenOf } from "../_lib/core.js";

const enc = new TextEncoder();

const json = (data, status = 200) =>
  new Response(JSON.stringify(data), { status, headers: { "content-type": "application/json; charset=utf-8", "cache-control": "no-store" } });

const b64url = (buf) => {
  let s = "";
  const bytes = new Uint8Array(buf);
  for (let i = 0; i < bytes.length; i++) s += String.fromCharCode(bytes[i]);
  return btoa(s).replace(/\+/g, "-").replace(/\//g, "_").replace(/=+$/, "");
};
const fromB64url = (str) => {
  const pad = "=".repeat((4 - (str.length % 4)) % 4);
  const bin = atob(str.replace(/-/g, "+").replace(/_/g, "/") + pad);
  const out = new Uint8Array(bin.length);
  for (let i = 0; i < bin.length; i++) out[i] = bin.charCodeAt(i);
  return out;
};

async function signHs256(payload, secret) {
  const head = b64url(enc.encode(JSON.stringify({ alg: "HS256", typ: "JWT" })));
  const body = b64url(enc.encode(JSON.stringify(payload)));
  const key = await crypto.subtle.importKey("raw", enc.encode(secret), { name: "HMAC", hash: "SHA-256" }, false, ["sign"]);
  const sig = await crypto.subtle.sign("HMAC", key, enc.encode(head + "." + body));
  return head + "." + body + "." + b64url(sig);
}

// Confere o token de sessao do app (emitido pelo proprio servidor QINBLY). Devolve o id do usuario.
async function verifyUser(request, env) {
  const user = await authUser(env, tokenOf(request));
  return user ? user.id : null;
}

const cleanName = (s) => String(s || "").replace(/[^A-Za-z0-9_.-]/g, "").slice(0, 30);

async function livekitToken(env, identity, name, grant, ttl = 3600) {
  const now = Math.floor(Date.now() / 1000);
  return signHs256({ iss: env.LIVEKIT_API_KEY, sub: identity, name, nbf: now - 10, exp: now + ttl, video: grant }, env.LIVEKIT_API_SECRET);
}

async function twirp(env, service, method, grant, body) {
  const httpUrl = env.LIVEKIT_URL.replace(/^wss:/, "https:").replace(/^ws:/, "http:").replace(/\/$/, "");
  const jwt = await livekitToken(env, "qinbly-server", "qinbly-server", grant, 60);
  const res = await fetch(httpUrl + "/twirp/livekit." + service + "/" + method, {
    method: "POST",
    headers: { "content-type": "application/json", authorization: "Bearer " + jwt },
    body: JSON.stringify(body),
  });
  if (!res.ok) throw new Error(service + " " + res.status);
  return res.json();
}

export async function onRequest({ request, env }) {
  if (!env.LIVEKIT_URL || !env.LIVEKIT_API_KEY || !env.LIVEKIT_API_SECRET) {
    return json({ error: "Servidor de lives ainda não configurado." }, 503);
  }
  const url = new URL(request.url);
  const action = url.searchParams.get("action");
  const uid = await verifyUser(request, env);
  if (!uid) return json({ error: "Entre na sua conta de novo." }, 401);

  try {
    if (action === "token" && request.method === "POST") {
      const body = await request.json().catch(() => ({}));
      const username = cleanName(body.username) || "visitante";
      if (body.host === true) {
        const room = "live_" + uid;
        const token = await livekitToken(env, uid, username, { room, roomJoin: true, canPublish: true, canSubscribe: true, canPublishData: true });
        return json({ url: env.LIVEKIT_URL, token, room });
      }
      const room = String(body.room || "");
      if (!/^live_[0-9a-fA-F-]{36}$/.test(room)) return json({ error: "Live inválida." }, 400);
      // Quem assiste entra com identidade única (a mesma pessoa pode abrir em dois aparelhos).
      const identity = uid + "~" + Math.random().toString(36).slice(2, 8);
      const token = await livekitToken(env, identity, username, { room, roomJoin: true, canPublish: false, canSubscribe: true, canPublishData: true });
      return json({ url: env.LIVEKIT_URL, token, room });
    }

    if (action === "list" && request.method === "GET") {
      const data = await twirp(env, "RoomService", "ListRooms", { roomList: true }, {});
      const rooms = (data.rooms || []).filter((r) => String(r.name).startsWith("live_"));
      const lives = [];
      for (const r of rooms.slice(0, 30)) {
        const count = Number(r.numParticipants ?? r.num_participants ?? 0);
        if (count < 1) continue;
        const hostId = String(r.name).slice(5);
        let host = "";
        try {
          const p = await twirp(env, "RoomService", "ListParticipants", { roomAdmin: true, room: r.name }, { room: r.name });
          host = ((p.participants || []).find((x) => x.identity === hostId) || {}).name || "";
        } catch (_) {}
        if (!host) continue;
        lives.push({ id: r.name, host, viewers: Math.max(0, count - 1) });
      }
      return json({ lives });
    }
    return json({ error: "Pedido inválido." }, 400);
  } catch (e) {
    return json({ error: "Não foi possível falar com o servidor de vídeo." }, 502);
  }
}
