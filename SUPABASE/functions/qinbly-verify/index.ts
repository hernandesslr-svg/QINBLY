import "jsr:@supabase/functions-js/edge-runtime.d.ts";

// Pedido de selo de verificado: GET /status e POST /request. Aprovação manual via SQL:
//   select public.review_verification('<id do pedido>', true, null);   -- aprova
//   select public.review_verification('<id do pedido>', false, 'motivo'); -- recusa
const SUPABASE_URL = Deno.env.get("SUPABASE_URL")!;
const ANON_KEY = Deno.env.get("SUPABASE_ANON_KEY")!;
const cors = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, apikey, content-type, x-client-info",
  "Access-Control-Allow-Methods": "GET, POST, OPTIONS",
  "Content-Type": "application/json; charset=utf-8",
};
const reply = (data: unknown, status = 200) => new Response(JSON.stringify(data), { status, headers: cors });
const rest = async (path: string, token: string, init: RequestInit = {}) => {
  const headers = new Headers(init.headers || {});
  headers.set("apikey", ANON_KEY);
  headers.set("Authorization", `Bearer ${token}`);
  headers.set("Content-Type", "application/json");
  const res = await fetch(`${SUPABASE_URL}/rest/v1/${path}`, { ...init, headers });
  const text = await res.text();
  if (!res.ok) throw new Error(text || `HTTP ${res.status}`);
  return text ? JSON.parse(text) : null;
};

Deno.serve(async (req: Request) => {
  if (req.method === "OPTIONS") return new Response("ok", { headers: cors });
  try {
    const token = (req.headers.get("authorization") || "").replace(/^Bearer\s+/i, "");
    if (!token || token === ANON_KEY) return reply({ error: "Entre na sua conta para pedir verificação." }, 401);
    const r = await fetch(`${SUPABASE_URL}/auth/v1/user`, { headers: { apikey: ANON_KEY, Authorization: `Bearer ${token}` } });
    if (!r.ok) return reply({ error: "Sua sessão expirou. Entre novamente." }, 401);
    const user = await r.json();
    const id = user.id as string;
    const path = new URL(req.url).pathname.replace(/\/$/, "");

    const profile = (await rest(`profiles?id=eq.${id}&select=id,bio,avatar_path,cover_path,is_verified`, token))?.[0];
    const last = (await rest(`verification_requests?user_id=eq.${id}&select=id,status,review_note,created_at,reviewed_at&order=created_at.desc&limit=1`, token))?.[0] || null;

    if (req.method === "GET") return reply({ verified: !!profile?.is_verified, request: last });
    if (req.method === "POST" && path.endsWith("/request")) {
      if (profile?.is_verified) return reply({ error: "Sua conta já é verificada." }, 409);
      if (last?.status === "pending") return reply({ error: "Você já tem um pedido em análise." }, 409);
      const body = await req.json().catch(() => ({}));
      const reason = String(body.reason || "").trim();
      if (reason.length < 10 || reason.length > 1000) return reply({ error: "Explique em 10 a 1000 caracteres por que sua conta deve ser verificada." }, 400);
      const missing: string[] = [];
      if (!profile?.avatar_path && !profile?.cover_path) missing.push("foto de perfil");
      if (!String(profile?.bio || "").trim()) missing.push("bio");
      const posts = await rest(`posts?user_id=eq.${id}&select=id&limit=1`, token);
      if (!posts?.length) missing.push("pelo menos 1 publicação");
      if (missing.length) return reply({ error: `Para pedir verificação, complete: ${missing.join(", ")}.` }, 400);
      await rest("verification_requests", token, { method: "POST", headers: { Prefer: "return=minimal" }, body: JSON.stringify([{ user_id: id, reason, status: "pending" }]) });
      return reply({ ok: true, status: "pending" }, 201);
    }
    return reply({ error: "Rota não encontrada." }, 404);
  } catch (e) {
    return reply({ error: e instanceof Error ? e.message : String(e) }, 500);
  }
});
