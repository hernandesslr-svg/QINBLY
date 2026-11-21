import { handleStorage } from "../../_lib/extras.js";
export async function onRequest({ request, env, params }) {
  const parts = Array.isArray(params.path) ? params.path : [params.path].filter(Boolean);
  return handleStorage(request, env, parts.join("/"));
}
