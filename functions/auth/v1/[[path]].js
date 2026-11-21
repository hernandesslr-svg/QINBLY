import { handleAuthV1 } from "../../_lib/extras.js";
export async function onRequest({ request, env, params }) {
  const parts = Array.isArray(params.path) ? params.path : [params.path].filter(Boolean);
  return handleAuthV1(request, env, parts.join("/"));
}
