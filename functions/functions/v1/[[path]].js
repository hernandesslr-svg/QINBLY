// Mesmo endereco do servidor antigo: /functions/v1/qinbly-api, qinbly-verify e qinbly-login.
import { handleApi } from "../../_lib/gateway.ts";
import { handleLogin, handleVerify } from "../../_lib/extras.js";
import { reply, preflight } from "../../_lib/core.js";

export async function onRequest({ request, env, params }) {
  if (request.method === "OPTIONS") return preflight();
  const parts = Array.isArray(params.path) ? params.path : [params.path].filter(Boolean);
  const fn = parts[0];
  if (fn === "qinbly-api") return handleApi(request, env);
  if (fn === "qinbly-verify") return handleVerify(request, env);
  if (fn === "qinbly-login") return handleLogin(request, env, handleApi);
  return reply({ error: "Função não encontrada." }, 404);
}
