// TEMPORARIO: copia os arquivos do Supabase para o R2. Remover depois da migracao.
const SRC = "https://jqomgrdlocdqaarfqkay.supabase.co/storage/v1/object/public/post-media/";
const EXT = { jpg: "image/jpeg", jpeg: "image/jpeg", png: "image/png", mp4: "video/mp4", m4a: "audio/mp4", webp: "image/webp", gif: "image/gif" };
const page = (body, again) =>
  new Response(`<!doctype html><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">${again ? '<meta http-equiv="refresh" content="1">' : ""}<body style="font:18px sans-serif;padding:24px">${body}`, { headers: { "content-type": "text/html; charset=utf-8", "cache-control": "no-store" } });
export async function onRequest({ request, env }) {
  const url = new URL(request.url);
  const row = await env.DB.prepare("SELECT v FROM app_secrets WHERE k='migration_key'").first();
  if (!row || url.searchParams.get("k") !== row.v) return new Response("Not found", { status: 404 });
  const sniff = (b) => (b[0] === 0x89 && b[1] === 0x50 ? "image/png" : b[0] === 0xff && b[1] === 0xd8 ? "image/jpeg" : "application/octet-stream");
  const log = [];
  let budget = 3;
  while (budget > 0) {
    const f = await env.DB.prepare("SELECT path, size FROM migration_files WHERE done=0 AND tries<3 ORDER BY size LIMIT 1").first();
    if (!f) break;
    if (f.size > 8e6 && budget < 3) break; // arquivo grande vai sozinho
    budget -= f.size > 8e6 ? 3 : 1;
    try {
      await env.DB.prepare("UPDATE migration_files SET tries=tries+1 WHERE path=?").bind(f.path).run();
      const res = await fetch(SRC + f.path.split("/").map(encodeURIComponent).join("/"));
      if (!res.ok || !res.body) throw new Error("origem HTTP " + res.status);
      const len = Number(res.headers.get("content-length") || 0);
      const ext = f.path.split(".").pop().toLowerCase();
      let type = EXT[ext] || res.headers.get("content-type") || "application/octet-stream";
      if (!EXT[ext]) type = (res.headers.get("content-type") || "").split(";")[0] || type;
      if (!len) throw new Error("sem content-length");
      const { readable, writable } = new FixedLengthStream(len);
      res.body.pipeTo(writable);
      await env.FILES.put(f.path, readable, { httpMetadata: { contentType: type } });
      await env.DB.prepare("UPDATE migration_files SET done=1, err=NULL WHERE path=?").bind(f.path).run();
      log.push("ok " + f.path.slice(-40));
    } catch (e) {
      await env.DB.prepare("UPDATE migration_files SET err=? WHERE path=?").bind(String(e && e.message || e), f.path).run();
      log.push("ERRO " + f.path.slice(-40) + ": " + (e && e.message || e));
    }
  }
  const st = await env.DB.prepare("SELECT sum(done=1) AS feitos, sum(done=0 AND tries<3) AS faltam, sum(done=0 AND tries>=3) AS falhas, count(*) AS total FROM migration_files").first();
  const finished = !st.faltam;
  return page(`<h2>${finished ? "Pronto ✅" : "Copiando…"}</h2><p>${st.feitos} de ${st.total} arquivos copiados${st.falhas ? `<br><b>${st.falhas} falharam</b>` : ""}</p><pre style="font-size:12px;white-space:pre-wrap">${log.join("\n")}</pre>${finished ? "<p>Pode fechar esta página.</p>" : "<p>Deixe esta página aberta.</p>"}`, !finished);
}
