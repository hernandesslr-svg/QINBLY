import "jsr:@supabase/functions-js/edge-runtime.d.ts";

// Entrar (ou pedir link de nova senha) com nome de usuário: descobre o e-mail da conta no servidor (o e-mail nunca volta para o app)
// e repassa o login para a função principal, devolvendo exatamente a mesma resposta.
const SUPABASE_URL = Deno.env.get("SUPABASE_URL")!;
const ANON_KEY = Deno.env.get("SUPABASE_ANON_KEY")!;
const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, apikey, content-type, x-client-info",
  "Access-Control-Allow-Methods": "POST, OPTIONS",
  "Content-Type": "application/json; charset=utf-8",
};
const FORGOT_MESSAGE = "Se existir uma conta com esses dados, enviamos o link para criar uma nova senha.";
const reply = (data: unknown, status = 200) => new Response(JSON.stringify(data), { status, headers: corsHeaders });

Deno.serve(async (req: Request) => {
  if (req.method === "OPTIONS") return new Response("ok", { headers: corsHeaders });
  if (req.method !== "POST") return reply({ error: "Método não permitido." }, 405);
  try {
    const body: any = await req.json().catch(() => ({}));
    const uname = String(body.username || "").trim().toLowerCase().replace(/^@/, "");
    const password = String(body.password || "");
    const forgot = body.action === "forgot";
    const wrong = () => reply({ error: "Usuário ou senha incorretos." }, 400);
    const service = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY");
    if (!service) return reply({ error: "Entrar com nome de usuário não está disponível agora. Use o seu e-mail." }, 503);
    if (!/^[a-z0-9_.]{3,30}$/.test(uname) || (!forgot && !password)) return forgot ? reply({ ok: true, message: FORGOT_MESSAGE }) : wrong();

    const found = await fetch(`${SUPABASE_URL}/rest/v1/profiles?username=eq.${encodeURIComponent(uname)}&select=id`, {
      headers: { apikey: ANON_KEY, Authorization: `Bearer ${ANON_KEY}` },
    });
    const rows = found.ok ? await found.json().catch(() => []) : [];
    const uid = rows?.[0]?.id;
    const adminRes = uid ? await fetch(`${SUPABASE_URL}/auth/v1/admin/users/${uid}`, {
      headers: { apikey: service, Authorization: `Bearer ${service}` },
    }) : null;
    const adminUser = adminRes && adminRes.ok ? await adminRes.json().catch(() => null) : null;

    // Esqueci a senha pelo nome de usuário: manda o link para o e-mail da conta e responde sempre igual,
    // exista a conta ou não (ninguém descobre quem tem cadastro).
    if (forgot) {
      if (adminUser?.email) {
        await fetch(`${SUPABASE_URL}/auth/v1/recover`, {
          method: "POST",
          headers: { apikey: ANON_KEY, "Content-Type": "application/json" },
          body: JSON.stringify({ email: String(adminUser.email).toLowerCase() }),
        }).catch(() => null);
      }
      return reply({ ok: true, message: FORGOT_MESSAGE });
    }
    if (!adminUser?.email) return wrong();

    const login = await fetch(`${SUPABASE_URL}/functions/v1/qinbly-api/api/auth/login`, {
      method: "POST",
      headers: { apikey: ANON_KEY, Authorization: `Bearer ${ANON_KEY}`, "Content-Type": "application/json" },
      body: JSON.stringify({ email: String(adminUser.email).toLowerCase(), password }),
    });
    const text = await login.text();
    // Senha errada vira a mesma mensagem de "usuário não existe", para não revelar quais nomes existem.
    if (login.status === 400 || login.status === 401) return wrong();
    return new Response(text, { status: login.status, headers: corsHeaders });
  } catch (e) {
    console.error("qinbly-login:", e instanceof Error ? e.message : String(e));
    return reply({ error: "Não foi possível entrar agora. Tente de novo." }, 500);
  }
});
