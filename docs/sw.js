// QINBLY web: guarda só a "casca" do app para abrir rápido. Dados (API) e fotos sempre vêm da rede.
const CACHE = "qinbly-web-v13";
const SHELL = ["./", "app.css", "app.js", "manifest.webmanifest", "icon-192.png"];
self.addEventListener("install", (e) => { e.waitUntil(caches.open(CACHE).then((c) => c.addAll(SHELL)).then(() => self.skipWaiting())); });
self.addEventListener("activate", (e) => {
  e.waitUntil(caches.keys().then((ks) => Promise.all(ks.filter((k) => k !== CACHE).map((k) => caches.delete(k)))).then(() => self.clients.claim()));
});
self.addEventListener("fetch", (e) => {
  const req = e.request, url = new URL(req.url);
  if (req.method !== "GET" || url.origin !== location.origin) return;
  // só a "casca" do app pode vir da cópia guardada; dados (API, mensagens, publicações) nunca: senão uma coisa apagada "volta"
  const shell = url.pathname === "/" || /\.(html|css|js|png|webmanifest)$/.test(url.pathname);
  if (!shell || url.pathname.startsWith("/functions/") || url.pathname.startsWith("/rest/") || url.pathname.startsWith("/api/") || url.pathname.startsWith("/storage/")) return;
  e.respondWith(fetch(req).then((res) => { const copy = res.clone(); caches.open(CACHE).then((c) => c.put(req, copy)); return res; }).catch(() => caches.match(req)));
});
