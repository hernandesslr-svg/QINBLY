/* QINBLY web — versão do aplicativo que roda no navegador (e instala no iPhone pelo Safari).
   Usa o MESMO servidor do app Android: mesmas contas, publicações e mensagens. */
(function () {
  "use strict";
  var HOST = "https://qinbly.pages.dev";
  var API = HOST + "/functions/v1/qinbly-api";
  var LOGIN_URL = HOST + "/functions/v1/qinbly-login";
  var $app = document.getElementById("app");
  var $sheet = document.getElementById("sheetRoot");
  var $toast = document.getElementById("toast");

  /* ---------- utilidades ---------- */
  function esc(s) { return String(s == null ? "" : s).replace(/[&<>"']/g, function (c) { return { "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&apos;" }[c]; }); }
  function safeUrl(u) { return /^https?:\/\//i.test(u || "") ? esc(u).replace(/&apos;/g, "%27").replace(/\(/g, "%28").replace(/\)/g, "%29") : ""; }
  function toast(t) { $toast.textContent = t; $toast.classList.add("on"); clearTimeout(toast.t); toast.t = setTimeout(function () { $toast.classList.remove("on"); }, 2800); }

  var APK_URL = "https://github.com/hernandesslr-svg/QINBLY/releases/latest/download/QINBLY-release.apk";
  var installEvt = null;
  window.addEventListener("beforeinstallprompt", function (e) { e.preventDefault(); installEvt = e; });
  window.addEventListener("appinstalled", function () { installEvt = null; toast("App instalado."); });
  function installNow() {
    if (installEvt) { var ev = installEvt; installEvt = null; ev.prompt(); return; }
    if (/Android/i.test(navigator.userAgent)) { location.href = APK_URL; return; }
    if (/iPhone|iPad|iPod/i.test(navigator.userAgent)) { sheet("Instalar no iPhone", '<div style="padding:0 20px 20px;font-size:16px;line-height:1.6">O iPhone não deixa instalar com um toque. Toque em <b>Compartilhar</b> (quadrado com seta) e escolha <b>Adicionar à Tela de Início</b>.</div>'); return; }
    location.href = APK_URL;
  }
  function ago(iso) {
    var s = Math.max(1, (Date.now() - Date.parse(iso)) / 1000);
    if (s < 60) return "agora";
    if (s < 3600) return "há " + Math.floor(s / 60) + " min";
    if (s < 86400) return "há " + Math.floor(s / 3600) + " h";
    if (s < 604800) return "há " + Math.floor(s / 86400) + " d";
    return new Date(iso).toLocaleDateString("pt-BR");
  }
  function linkify(text) {
    return esc(text).replace(/(https?:\/\/[^\s<]+)|(@[a-zA-Z0-9_.]{3,30})|(#[\p{L}\d_]+)/gu, function (m, url, men, tag) {
      if (url) return '<a href="' + url + '" target="_blank" rel="noopener noreferrer">' + url + "</a>";
      if (men) return '<a href="#/u/' + men.slice(1).toLowerCase() + '">' + men + "</a>";
      return '<a href="#/tag/' + encodeURIComponent(tag.slice(1)) + '" class="link">' + tag + "</a>";
    });
  }
  var IC = {
    home: '<path d="M3 11l9-8 9 8"/><path d="M5 10v10h5v-6h4v6h5V10"/>',
    plus: '<circle cx="12" cy="12" r="9"/><path d="M12 8v8M8 12h8"/>',
    msg: '<path d="M4 5h16a1 1 0 0 1 1 1v10a1 1 0 0 1-1 1h-9l-5 4v-4H4a1 1 0 0 1-1-1V6a1 1 0 0 1 1-1z"/>',
    user: '<circle cx="12" cy="8" r="4"/><path d="M4 21c0-4.4 3.6-8 8-8s8 3.6 8 8"/>',
    heart: '<path d="M12 20s-7-4.5-9-9.2C1.7 6.6 5.5 3.5 9 5.5c1.3.7 2.3 1.8 3 3 .7-1.2 1.7-2.3 3-3 3.5-2 7.3 1.1 6 5.3-2 4.7-9 9.2-9 9.2z"/>',
    comment: '<path d="M3 20l1.3-3.9C2 12.7 2.9 8.2 6.4 5.7c3.5-2.5 8.6-2.3 11.8.5 3.3 2.8 3.7 7.3 1 10.5-2.7 3.2-7.6 4.2-11.6 2.3L3 20"/><path d="M8 10h8M8 13.5h5"/>',
    commentR: '<path transform="translate(24 0) scale(-1 1)" d="M3 20l1.3-3.9C2 12.7 2.9 8.2 6.4 5.7c3.5-2.5 8.6-2.3 11.8.5 3.3 2.8 3.7 7.3 1 10.5-2.7 3.2-7.6 4.2-11.6 2.3L3 20"/><path d="M8 10h8M8 13.5h5"/>',
    send: '<path d="M5.67 8.94 L17.99 4.0 Q21.97 2.4 20.76 6.81 L17.71 17.97 Q16.62 22.0 14.4 18.56 L12.77 16.02 Q11.08 13.4 8.25 12.46 L5.74 11.63 Q2.03 10.4 5.67 8.94 Z"/><path d="M11.63 13.9 L19.2 5.2"/>',
    save: '<path d="M6 3h12v18l-6-4-6 4z"/>',
    back: '<path d="M15 5l-7 7 7 7"/>',
    gear: '<circle cx="12" cy="12" r="3"/><path d="M12 2v3M12 19v3M2 12h3M19 12h3M4.9 4.9l2.1 2.1M17 17l2.1 2.1M4.9 19.1L7 17M17 7l2.1-2.1"/>',
    search: '<circle cx="11" cy="11" r="7"/><path d="M21 21l-4.3-4.3"/>',
    grid: '<rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/>',
    chev: '<path d="M6 9l6 6 6-6"/>',
    x: '<path d="M6 6l12 12M18 6L6 18"/>',
    play: '<path d="M8 5v14l11-7z"/>',
    flame: '<path d="M12 3c1 4 5 5 5 10a5 5 0 0 1-10 0c0-2 1-3 2-4 0 2 1 3 2 3 0-3-1-5 1-9z"/>',
    bell: '<path d="M6 16v-5a6 6 0 0 1 12 0v5l2 2H4z"/><path d="M10 21h4"/>',
    groups: '<circle cx="9" cy="8" r="3"/><path d="M3 20c0-3.3 2.7-6 6-6s6 2.7 6 6"/><circle cx="17" cy="9" r="2.4"/><path d="M16 14.2c2.9.3 5 2.6 5 5.8"/>',
    bag: '<path d="M5 8h14l-1 12H6z"/><path d="M9 8a3 3 0 0 1 6 0"/>',
    menu: '<path d="M4 7h16M4 12h16M4 17h16"/>',
    add: '<path d="M12 5v14M5 12h14"/>',
    photo: '<rect x="3" y="4" width="18" height="16" rx="2"/><circle cx="9" cy="10" r="1.6"/><path d="M21 16l-5-5-8 8"/>',
    cal: '<rect x="4" y="5" width="16" height="15" rx="2"/><path d="M4 10h16M9 3v4M15 3v4"/>',
    edit: '<path d="M4 20h4L19 9l-4-4L4 16z"/><path d="M13.5 6.5l4 4"/>',
    gift: '<rect x="3" y="8" width="18" height="4" rx="1"/><path d="M12 8v13M5 12v8h14v-8M12 8c-2.5 0-4.5-1-4.5-3S10 3 12 8zm0 0c2.5 0 4.5-1 4.5-3S14 3 12 8z"/>',
    tool: '<path d="M14.7 6.3a4 4 0 00-5 5L4 17l3 3 5.7-5.7a4 4 0 005-5l-2.4 2.4-2.3-.7-.7-2.3z"/>',
    mega: '<path d="M3 10v4l3 .5L13 19V5L6 9.5z"/><path d="M17 9a4 4 0 010 6"/>',
    adduser: '<circle cx="9" cy="8" r="3.5"/><path d="M2.5 20a6.5 6.5 0 0113 0M19 8v6M16 11h6"/>',
    download: '<path d="M12 4v11m0 0l-4-4m4 4l4-4M5 20h14"/>',
    spark: '<path d="M12 3l1.8 5.2L19 10l-5.2 1.8L12 17l-1.8-5.2L5 10l5.2-1.8z"/><path d="M18 16l.7 1.8 1.8.7-1.8.7L18 21l-.7-1.8-1.8-.7 1.8-.7z"/>',
    play: '<circle cx="12" cy="12" r="9"/><path d="M10 8.5l6 3.5-6 3.5z"/>',
    vdisp: '<rect x="3" y="5" width="18" height="14" rx="3.5"/><path d="M10.5 9.5l4 2.5-4 2.5z"/>',
    sensors: '<circle cx="12" cy="12" r="2"/><path d="M7.8 7.8a6 6 0 000 8.4M16.2 7.8a6 6 0 010 8.4M4.9 4.9a10 10 0 000 14.2M19.1 4.9a10 10 0 010 14.2"/>',
    pin: '<path d="M12 21s7-6.2 7-11.5A7 7 0 005 9.5C5 14.8 12 21 12 21z"/><circle cx="12" cy="9.5" r="2.5"/>',
    repeat: '<path d="M20 11a8.1 8.1 0 0 0-15.5-2m-.5-4v4h4"/><path d="M4 13a8.1 8.1 0 0 0 15.5 2m.5 4v-4h-4"/>',
    check: '<path d="M5 12.5l4.5 4.5L19 7"/>',
    dots: '<circle cx="5" cy="12" r="1.2"/><circle cx="12" cy="12" r="1.2"/><circle cx="19" cy="12" r="1.2"/>'
  };
  function svg(n) { return '<svg viewBox="0 0 24 24" aria-hidden="true">' + IC[n] + "</svg>"; }
  var VERIFIED = '<svg class="vf" viewBox="0 0 24 24" aria-label="verificado"><path d="M12 2l2.4 2 3.1-.2 1 3 2.7 1.6-.9 3 .9 3-2.7 1.6-1 3-3.1-.2L12 22l-2.4-2-3.1.2-1-3-2.7-1.6.9-3-.9-3 2.7-1.6 1-3 3.1.2z"/><path d="M8.5 12l2.5 2.5 4.5-5" stroke="#fff" fill="none" stroke-width="2"/></svg>';
  function avatar(url, name, cls) {
    var AVC = ["#7C4DFF", "#2F68E0", "#E0368C", "#1E9E6A", "#F08A24", "#1AA3B8", "#E5484D", "#565BF0"], hh = 0, key = String(name || "?").trim().replace(/^@/, "").toLowerCase();
    for (var i = 0; i < key.length; i++) hh = (hh * 31 + key.charCodeAt(i)) & 0x7fffffff;
    var st = safeUrl(url) ? ' style="background-image:url(\'' + safeUrl(url) + '\')"' : ' style="background:' + AVC[hh % AVC.length] + '"';
    var ini = safeUrl(url) ? "" : esc((name || "?").trim().charAt(0).toUpperCase());
    return '<span class="av ' + (cls || "") + '"' + st + ">" + ini + "</span>";
  }
  var TEXT_BGS = [["#7C4DFF", "#2F68E0"], ["#E0457B", "#8A2A5A"], ["#1D9E75", "#0F6E56"], ["#EF9F27", "#B45309"], ["#378ADD", "#185FA5"], ["#111111", "#2B2B2B"]];
  function bgCss(n) {
    n = Number(n) || 0;
    if (n >= 16777216) return "#" + (n & 0xffffff).toString(16).padStart(6, "0");
    var c = TEXT_BGS[(n < 1 ? 1 : n > 6 ? 6 : n) - 1];
    return "linear-gradient(135deg," + c[0] + "," + c[1] + ")";
  }

  /* ---------- contas guardadas neste aparelho ---------- */
  var LS = {
    get: function (k, d) { try { var v = localStorage.getItem(k); return v ? JSON.parse(v) : d; } catch (e) { return d; } },
    set: function (k, v) { try { localStorage.setItem(k, JSON.stringify(v)); } catch (e) { /* sem armazenamento */ } }
  };
  var accounts = LS.get("qb_accounts", []);
  var activeId = LS.get("qb_active", "");
  function save() { LS.set("qb_accounts", accounts); LS.set("qb_active", activeId); }
  function cur() { return accounts.filter(function (a) { return a.id === activeId; })[0] || null; }
  function putAccount(res) {
    var u = res.user || {};
    var a = { id: u.id, username: u.username, displayName: u.displayName || "", avatarUrl: u.avatarUrl || "", token: res.token, refreshToken: res.refreshToken };
    accounts = accounts.filter(function (x) { return x.id !== a.id; }).concat(a);
    activeId = a.id; save();
  }
  function dropAccount(id) {
    accounts = accounts.filter(function (a) { return a.id !== id; });
    if (activeId === id) activeId = accounts.length ? accounts[0].id : "";
    save();
  }

  /* ---------- servidor ---------- */
  var refreshing = {};
  function refresh(acc) {
    if (!acc.refreshToken) return Promise.resolve(false);
    if (refreshing[acc.id]) return refreshing[acc.id];
    refreshing[acc.id] = fetch(API + "/api/auth/refresh", { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ refreshToken: acc.refreshToken }) })
      .then(function (r) { return r.ok ? r.json() : null; })
      .then(function (j) {
        if (!j || !j.token) return false;
        acc.token = j.token; acc.refreshToken = j.refreshToken; if (j.user) { acc.username = j.user.username; acc.displayName = j.user.displayName || ""; acc.avatarUrl = j.user.avatarUrl || ""; }
        save(); return true;
      }).catch(function () { return false; })
      .then(function (ok) { delete refreshing[acc.id]; return ok; });
    return refreshing[acc.id];
  }
  function call(url, opt) {
    opt = opt || {};
    var acc = opt.acc || cur();
    function go() {
      var h = {};
      if (opt.body !== undefined) h["Content-Type"] = "application/json";
      if (opt.headers) Object.keys(opt.headers).forEach(function (k) { h[k] = opt.headers[k]; });
      if (acc && acc.token && !opt.anon) h.Authorization = "Bearer " + acc.token;
      return fetch(url, { method: opt.method || "GET", headers: h, body: opt.body !== undefined ? JSON.stringify(opt.body) : undefined });
    }
    return go().then(function (r) {
      if (r.status === 401 && acc && !opt.anon && !opt.noRefresh) {
        return refresh(acc).then(function (ok) {
          if (!ok) { var e = new Error("Sua sessão expirou. Entre novamente."); e.expired = true; throw e; }
          return go();
        });
      }
      return r;
    }).then(function (r) {
      return r.json().catch(function () { return {}; }).then(function (j) {
        if (!r.ok) { var e = new Error(j.error || j.message || j.msg || ("Erro " + r.status)); e.status = r.status; throw e; }
        return j;
      });
    });
  }
  function api(path, opt) { return call(API + path, opt); }
  function goto(h) { if (location.hash === h) render(); else location.hash = h; }
  function onErr(e) {
    if (e && e.expired) { dropAccount(activeId); goto(accounts.length ? "#/" : "#/login"); return; }
    toast(e && e.message ? e.message : "Sem conexão. Tente de novo.");
  }

  /* ---------- envio de arquivos ---------- */
  var IMG_OK = ["image/jpeg", "image/png", "image/webp", "image/gif"];
  var VID_OK = ["video/mp4", "video/quicktime", "video/webm"];
  function shrink(file) {
    if (IMG_OK.indexOf(file.type) < 0 || file.type === "image/gif") return Promise.resolve(file);
    return new Promise(function (res) {
      var img = new Image(), url = URL.createObjectURL(file);
      img.onload = function () {
        var max = 1600, w = img.naturalWidth, h = img.naturalHeight, k = Math.min(1, max / Math.max(w, h));
        if (k === 1 && file.size < 1500000) { URL.revokeObjectURL(url); return res(file); }
        var c = document.createElement("canvas"); c.width = Math.round(w * k); c.height = Math.round(h * k);
        c.getContext("2d").drawImage(img, 0, 0, c.width, c.height);
        c.toBlob(function (b) { URL.revokeObjectURL(url); res(b ? new File([b], (file.name || "foto").replace(/\.\w+$/, "") + ".jpg", { type: "image/jpeg" }) : file); }, "image/jpeg", 0.85);
      };
      img.onerror = function () { URL.revokeObjectURL(url); res(file); };
      img.src = url;
    });
  }
  function upload(file, category) {
    var type = file.type;
    return api("/api/files/upload-url", { method: "POST", body: { category: category, contentType: type, sizeBytes: file.size, fileName: file.name || "arquivo" } }).then(function (up) {
      var h = {}; for (var k in (up.requiredHeaders || {})) h[k] = up.requiredHeaders[k];
      h.Authorization = "Bearer " + cur().token;
      return fetch(up.uploadUrl, { method: "PUT", headers: h, body: file }).then(function (r) {
        if (!r.ok) throw new Error("Não foi possível enviar o arquivo. Tente de novo.");
        var enc = up.objectPath.split("/").map(encodeURIComponent).join("/");
        return api("/api/files/" + enc + "/complete", { method: "POST", body: {} });
      }).then(function () { return up.objectPath; });
    });
  }

  /* ---------- estrutura das telas ---------- */
  var timers = [];
  function clearTimers() { timers.forEach(clearInterval); timers = []; }
  var unread = 0, notifN = 0;
  function badge(key, n) { return '<span class="badge" data-bd="' + key + '"' + (n ? "" : " hidden") + ">" + (n > 99 ? "99+" : n) + "</span>"; }
  function paintBadges() {
    [["inbox", unread], ["notif", notifN]].forEach(function (p) {
      document.querySelectorAll('[data-bd="' + p[0] + '"]').forEach(function (el) { el.textContent = p[1] > 99 ? "99+" : p[1]; el.hidden = !p[1]; });
    });
  }
  function side(tab) {
    function it(id, label, icon, href, extra) { return '<button class="si ' + (tab === id ? "on" : "") + '" data-go="' + href + '" aria-label="' + label + '">' + svg(icon) + '<span class="lbl">' + label + "</span>" + (extra || "") + "</button>"; }
    var me = cur();
    return '<aside class="side"><button class="logo brandg" data-go="#/">QINBLY</button>' +
      it("home", "Início", "home", "#/") + it("search", "Buscar", "search", "#/search") + it("trending", "Em alta", "flame", "#/trending") +
      it("inbox", "Mensagens", "msg", "#/inbox", badge("inbox", unread)) + it("notif", "Notificações", "bell", "#/notifications", badge("notif", notifN)) +
      it("communities", "Comunidades", "groups", "#/communities") + it("market", "Mercado", "bag", "#/market") + it("saved", "Salvos", "save", "#/saved") +
      it("new", "Criar", "plus", "#/new") +
      '<button class="si ' + (tab === "me" ? "on" : "") + '" data-go="#/me" aria-label="Perfil">' + (me ? avatar(me.avatarUrl, me.displayName || me.username, "xs") : svg("user")) + '<span class="lbl">Perfil</span></button>' +
      '<button class="si more" data-moremenu aria-label="Mais">' + svg("menu") + '<span class="lbl">Mais</span></button></aside>';
  }
  function bottomNav(tab) {
    function b(id, label, icon, href) { return '<button data-go="' + href + '" class="' + (tab === id ? "on" : "") + (id === "new" ? " mk" : "") + '" aria-label="' + label + '">' + (id === "new" ? '<span class="mkc"><svg viewBox="0 0 24 24" aria-hidden="true"><defs><linearGradient id="gq" x1="0" y1="0" x2="1" y2="1"><stop offset="0" stop-color="#7C4DFF"/><stop offset="1" stop-color="#FF4F9A"/></linearGradient></defs><path d="M12 5v14M5 12h14" stroke="url(#gq)"/></svg></span>' : '<span class="ni">' + svg(icon) + "</span>") + label + (id === "inbox" ? badge("inbox", unread) : "") + "</button>"; }
    return '<nav class="nav"><div class="in">' + b("home", "Início", "home", "#/") + b("communities", "Comunidades", "groups", "#/communities") + b("new", "Criar", "plus", "#/new") + b("inbox", "Mensagens", "msg", "#/inbox") + b("me", "Perfil", "user", "#/me") + "</div></nav>";
  }
  var RC = { t: 0, sug: [], tags: [] };
  function fillRcol() {
    var r = document.getElementById("rcol"), me = cur(); if (!r || !me) return;
    function draw() {
      r.innerHTML = '<div class="racc"><a href="#/me" class="ring">' + avatar(me.avatarUrl, me.displayName || me.username) + '</a><div class="t"><b>@' + esc(me.username) + "</b><small>" + esc(me.displayName) + '</small></div><button class="link" data-accounts>Trocar</button></div>' +
        (RC.sug.length ? '<div class="rh">Sugestões para você</div>' + RC.sug.slice(0, 5).map(function (u) {
          return '<div class="racc"><a href="#/u/' + esc(u.username) + '">' + avatar(u.avatarUrl, u.displayName || u.username, "s") + '</a><div class="t"><b><a href="#/u/' + esc(u.username) + '">' + esc(u.username) + "</a></b><small>" + esc(u.displayName) + '</small></div><button class="link" data-follow="' + u.id + '">Seguir</button></div>';
        }).join("") : "") +
        (RC.tags.length ? '<div class="rh">Em alta</div><div class="tags">' + RC.tags.slice(0, 6).map(function (t) { return '<a href="#/tag/' + encodeURIComponent(t.tag) + '" class="tg">#' + esc(t.tag) + "<small>" + t.posts + " publ.</small></a>"; }).join("") + "</div>" : "") +
        '<div class="rf"><a href="baixar.html">Baixar o app</a> · <a href="privacidade.html">Privacidade</a> · <a href="termos.html">Termos</a><br>QINBLY</div>';
    }
    draw();
    if (Date.now() - RC.t > 300000) {
      RC.t = Date.now();
      Promise.all([api("/api/users/suggested").catch(function () { return { users: [] }; }), api("/api/hashtags/trending").catch(function () { return { tags: [] }; })])
        .then(function (a) { RC.sug = a[0].users || []; RC.tags = a[1].tags || []; draw(); });
    }
  }
  function top(title, opts) {
    opts = opts || {};
    return '<header class="top">' + (opts.back ? '<button class="ib" data-back aria-label="Voltar">' + svg("back") + "</button>" : "") +
      '<h1 class="' + (opts.brand ? "brand" : "") + '">' + title + "</h1>" + (opts.right || "") + "</header>";
  }
  function spin() { return '<div class="spin"><i></i></div>'; }
  function setView(html, tab, cls) {
    cls = cls || "";
    $app.innerHTML = '<div class="layout">' + side(tab) + '<main class="view ' + cls + '">' + html + '</main><aside class="rcol" id="rcol"></aside></div>' + (/noNav/.test(cls) ? "" : bottomNav(tab));
    fillRcol(); window.scrollTo(0, 0);
  }

  /* ---------- publicações ---------- */
  var POSTS = {};
  function postHTML(p) {
    POSTS[p.id] = p;
    var a = p.author || {}, me = cur(), mine = me && p.authorId === me.id;
    var media = "";
    if (p.mediaType === "text") {
      media = '<div class="tbg" style="background:' + (p.textBg ? bgCss(p.textBg) : "var(--surface)") + '">' + esc(p.caption) + "</div>";
    } else if (p.mediaType === "poll") {
      var tot = (p.pollVotes || []).reduce(function (s, v) { return s + v.votes; }, 0);
      media = '<div class="poll">' + (p.pollOptions || []).map(function (o, i) {
        var v = (p.pollVotes && p.pollVotes[i] ? p.pollVotes[i].votes : 0), pc = tot ? Math.round(v * 100 / tot) : 0;
        return '<button class="opt ' + (p.myPollVote === i ? "mine" : "") + '" data-vote="' + p.id + ":" + i + '"><i style="width:' + (p.myPollVote >= 0 ? pc : 0) + '%"></i><span>' + esc(o) + "</span><span>" + (p.myPollVote >= 0 ? pc + "%" : "") + "</span></button>";
      }).join("") + '<small style="color:var(--muted)">' + tot + " voto" + (tot === 1 ? "" : "s") + "</small></div>";
    } else if (p.mediaType === "video" && safeUrl(p.mediaUrl)) {
      media = '<div class="media"><video src="' + safeUrl(p.mediaUrl) + '" controls playsinline preload="metadata"></video></div>';
    } else if (safeUrl(p.mediaUrl)) {
      var urls = (p.mediaUrls && p.mediaUrls.length > 1) ? p.mediaUrls : [p.mediaUrl];
      media = '<div class="media ' + (urls.length > 1 ? "gal" : "") + '">' + urls.map(function (u) { return '<img src="' + safeUrl(u) + '" loading="lazy" alt="">'; }).join("") + "</div>";
    }
    var cap = (p.mediaType !== "text" && p.mediaType !== "poll" && p.caption) || (p.mediaType === "poll" ? p.caption : "");
    return '<article class="post" data-post="' + p.id + '">' +
      '<div class="ph"><a href="#/u/' + esc(a.username) + '" class="ring ' + ((window.__unseen || {})[String(a.username).toLowerCase()] ? "new" : "") + '" data-u="' + esc(String(a.username).toLowerCase()) + '">' + avatar(a.avatarUrl, a.displayName || a.username, "m") + "</a>" +
      '<div class="who"><b><a href="#/u/' + esc(a.username) + '">' + esc(a.displayName || a.username) + "</a>" + (a.verified ? VERIFIED : "") + "</b><small>@" + esc(a.username) + " · " + ago(p.createdAt) + (p.visibility === "private" ? " · privado" : "") + "</small></div>" +
      (!mine && !p.isFollowing && me ? '<button class="sb" style="flex:none;width:76px" data-follow="' + p.authorId + '">Seguir</button>' : "") + '<button class="ib" data-pmenu="' + p.id + '" aria-label="Mais opções">' + svg("dots") + "</button></div>" +
      (p.repostedBy && p.repostedBy.username ? '<div class="rpl">Repostado por <a href="#/u/' + esc(p.repostedBy.username) + '">' + esc(p.repostedBy.displayName || p.repostedBy.username) + "</a> · " + ago(p.repostedAt || p.createdAt) + "</div>" : "") + (cap ? '<div class="cap">' + linkify(cap) + "</div>" : "") + media + ((p.taggedUsers && p.taggedUsers.length) ? '<div class="tagged">Com ' + p.taggedUsers.map(function (u) { return '<a href="#/u/' + esc(u) + '">@' + esc(u) + "</a>"; }).join(", ") + "</div>" : "") +
      '<div class="acts"><button class="act ' + (p.likedByMe ? "on" : "") + '" data-like="' + p.id + '" aria-label="Curtir">' + svg("heart") + (p.likes || "") + "</button>" +
      '<button class="act" data-comments="' + p.id + '" aria-label="Comentários">' + svg("comment") + (p.comments || "") + "</button>" +
      '<button class="act rp ' + (p.repostedByMe ? "on" : "") + '" data-repost="' + p.id + '" aria-label="Repostar">' + svg("repeat") + (p.reposts || "") + "</button>" +
      '<button class="act" data-share="' + p.id + '" aria-label="Enviar">' + svg("send") + "</button>" +
      '<button class="act sv ' + (p.savedByMe ? "on" : "") + '" data-save="' + p.id + '" aria-label="Salvar">' + svg("save") + "</button>" + "</div></article>";
  }
  function repaint(id) {
    var rl = document.querySelector('[data-reel="' + id + '"]');
    if (rl && POSTS[id]) { repaintReel(rl, POSTS[id]); return; }
    var el = document.querySelector('[data-post="' + id + '"]');
    if (el && POSTS[id]) { var t = document.createElement("div"); t.innerHTML = postHTML(POSTS[id]); el.replaceWith(t.firstChild); }
  }
  function sheet(title, body, foot) {
    closeSheet();
    var d = document.createElement("div"); d.className = "shade";
    d.innerHTML = '<div class="sheet" role="dialog" aria-label="' + esc(title) + '"><div class="grab"></div><div class="hd">' + esc(title) + '</div><div class="body">' + body + "</div>" + (foot || "") + "</div>";
    d.addEventListener("click", function (e) { if (e.target === d) closeSheet(); });
    $sheet.appendChild(d); return d;
  }
  function closeSheet() { $sheet.innerHTML = ""; }

  function openComments(id) {
    var me = cur(), reply = null, editing = null;
    var s = sheet("Comentários", spin(),
      '<div id="rp" class="rpl" hidden></div><form class="cin" id="cf"><input id="ci" maxlength="2000" placeholder="Adicione um comentário…" autocomplete="off"><button type="submit">Enviar</button></form>');
    var box = s.querySelector(".body"), list = [];
    var openN = {};
    function one(c, sub) {
      var a = c.author || {}, mine = me && a.id === me.id;
      var body = /^qinbly:sticker:/.test(c.body) ? '<img class="mstk" style="width:90px;height:90px" src="' + safeUrl(HOST + "/storage/v1/object/public/post-media/" + c.body.slice(15)) + '" alt="Figurinha">' : linkify(c.body);
      return '<div class="cm2 ' + (sub ? "sub" : "") + '"><a href="#/u/' + esc(a.username) + '">' + avatar(a.avatarUrl, a.displayName || a.username, "s") + '</a><div class="b"><div class="l1"><b>' + esc(a.username) + "</b><small>" + ago(c.createdAt) + (c.editedAt ? " · editado" : "") + '</small></div><div class="tx">' + body + '</div><div class="ac"><button data-crep="' + c.id + ":" + esc(a.username) + '">Responder</button>' +
        (mine ? '<button data-cedit="' + c.id + '">Editar</button><button data-cdel="' + c.id + '">Apagar</button>' : '<button data-crpt="' + c.id + '">Denunciar</button>') + '</div></div><button class="lk ' + (c.likedByMe ? "on" : "") + '" data-clike="' + c.id + '" aria-label="Curtir">' + svg("heart") + (c.likes ? "<span>" + c.likes + "</span>" : "") + "</button></div>";
    }
    function draw() {
      var top = list.filter(function (c) { return !c.parentId; }), h = "";
      top.forEach(function (c) {
        h += one(c, false);
        var reps = list.filter(function (r) { return r.parentId === c.id; }); if (!reps.length) return;
        var n = Math.min(openN[c.id] || 0, reps.length), left = reps.length - n;
        if (!n) { h += '<div class="rtg"><button data-rmore="' + c.id + '">Ver ' + reps.length + (reps.length === 1 ? " resposta" : " respostas") + svg("chev") + "</button></div>"; return; }
        reps.slice(0, n).forEach(function (r) { h += one(r, true); });
        h += '<div class="rtg">' + (left > 0 ? '<button data-rmore="' + c.id + '">Ver mais ' + Math.min(10, left) + svg("chev") + "</button>" : "") + '<button data-rhide="' + c.id + '">Ocultar' + svg("chev") + "</button></div>";
      });
      box.innerHTML = h || '<div class="empty">Nenhum comentário ainda. Seja o primeiro.</div>';
    }
    api("/api/posts/" + id + "/comments").then(function (j) { list = j.comments || []; draw(); }).catch(function (e) { box.innerHTML = '<div class="empty">' + esc(e.message) + "</div>"; });
    s.addEventListener("click", function (e) {
      var t = e.target.closest("[data-crep],[data-cdel],[data-clike],[data-crpt],[data-cedit],[data-rmore],[data-rhide]"); if (!t) return; e.stopPropagation();
      if (t.dataset.rmore) { var rid = t.dataset.rmore, tot = list.filter(function (r) { return r.parentId === rid; }).length; openN[rid] = openN[rid] ? Math.min(tot, openN[rid] + 10) : Math.min(tot, 3); draw(); return; }
      if (t.dataset.rhide) { openN[t.dataset.rhide] = 0; draw(); return; }
      if (t.dataset.crep) { var a = t.dataset.crep.split(":"); reply = a[0]; var r = s.querySelector("#rp"); r.hidden = false; r.textContent = "Respondendo a @" + a[1] + " (toque para cancelar)"; s.querySelector("#ci").value = "@" + a[1] + " "; s.querySelector("#ci").focus(); }
      else if (t.dataset.cedit) { var cc = list.filter(function (c) { return c.id === t.dataset.cedit; })[0]; editing = cc.id; var r2 = s.querySelector("#rp"); r2.hidden = false; r2.textContent = "Editando comentário (toque para cancelar)"; s.querySelector("#ci").value = cc.body; s.querySelector("#ci").focus(); }
      else if (t.dataset.cdel) api("/api/comments/" + t.dataset.cdel, { method: "DELETE" }).then(function () { list = list.filter(function (c) { return c.id !== t.dataset.cdel && c.parentId !== t.dataset.cdel; }); draw(); if (POSTS[id]) { POSTS[id].comments = Math.max(0, (POSTS[id].comments || 1) - 1); repaint(id); } }).catch(onErr);
      else if (t.dataset.clike) api("/api/comments/" + t.dataset.clike + "/like", { method: "POST", body: {} }).then(function (r) { list.forEach(function (c) { if (c.id === t.dataset.clike) { c.likedByMe = !!r.liked; c.likes = Math.max(0, (c.likes || 0) + (r.liked ? 1 : -1)); } }); draw(); }).catch(onErr);
      else if (t.dataset.crpt) { closeSheet(); reportSheet("comment", t.dataset.crpt); }
    });
    s.querySelector("#rp").addEventListener("click", function () { reply = null; editing = null; this.hidden = true; s.querySelector("#ci").value = ""; });
    s.querySelector("#cf").addEventListener("submit", function (e) {
      e.preventDefault();
      var inp = s.querySelector("#ci"), t = inp.value.trim(); if (!t) return;
      if (!cur()) { location.hash = "#/login"; return; }
      inp.disabled = true;
      if (editing) { var eid = editing; api("/api/comments/" + eid, { method: "PATCH", body: { body: t } }).then(function () { list.forEach(function (c) { if (c.id === eid) { c.body = t; c.editedAt = new Date().toISOString(); } }); draw(); inp.value = ""; editing = null; s.querySelector("#rp").hidden = true; }).catch(onErr).then(function () { inp.disabled = false; inp.focus(); }); return; }
      api("/api/posts/" + id + "/comments", { method: "POST", body: { body: t, parentId: reply || undefined } }).then(function (j) {
        list.push(j.comment); if (j.comment.parentId) openN[j.comment.parentId] = list.filter(function (r) { return r.parentId === j.comment.parentId; }).length; draw(); inp.value = ""; reply = null; s.querySelector("#rp").hidden = true; box.scrollTop = box.scrollHeight; if (POSTS[id]) { POSTS[id].comments = (POSTS[id].comments || 0) + 1; repaint(id); }
      }).catch(onErr).then(function () { inp.disabled = false; inp.focus(); });
    });
  }
  /* ---------- stories ---------- */
  function parseCap(c) {
    c = String(c || "");
    var m = c.match(/^qinbly:(text|collab):(\d+):[^\n]*\n?/);
    var out = { bg: -1, text: c };
    if (m) { out.text = c.slice(m[0].length); if (m[1] === "text") out.bg = Number(m[2]); }
    out.text = out.text.replace(/^⧉§[^§]*§[^§]*§(video|image)\n?/, "");
    return out;
  }
  function openStories(entry) {
    var items = entry.stories || [], i = 0, timer = null, v = document.createElement("div"), me = cur(), own = !!entry.mine;
    v.className = "viewer";
    document.body.appendChild(v);
    function end() { clearTimeout(timer); v.remove(); }
    function show() {
      clearTimeout(timer);
      if (i >= items.length) return end();
      var s = items[i], cp = parseCap(s.caption), isText = s.mediaType === "text" || (cp.bg >= 0 && !safeUrl(s.mediaUrl));
      if (!own && s.id && !s.seen) { api("/api/stories/" + s.id + "/view", { method: "POST", body: {} }).catch(function () { }); s.seen = true; }
      var bars = items.map(function (_, k) { return '<i><b style="width:' + (k < i ? 100 : 0) + '%"></b></i>'; }).join("");
      var media = isText ? '<div class="tx" style="background:' + storyBg(cp.bg < 0 ? 0 : cp.bg) + '">' + esc(cp.text) + "</div>"
        : s.mediaType === "video" ? '<video src="' + safeUrl(s.mediaUrl) + '" autoplay playsinline></video>' : '<img src="' + safeUrl(s.mediaUrl) + '" alt="">';
      var foot = "";
      if (own && s.id) foot = '<div class="sfoot"><button data-sviews>👁 ' + (s.views || 0) + ' · Quem viu</button><button data-shl>Destaque</button><button data-sdel>Apagar</button></div>';
      else if (!entry.hl && s.id && me) foot = '<form class="cin sreply" id="srf"><input id="sri" maxlength="300" placeholder="Responder a @' + esc(entry.username) + '…" autocomplete="off"><button type="submit">Enviar</button></form>';
      v.innerHTML = '<div class="bar">' + bars + '</div><div class="who">' + avatar(entry.avatarUrl, entry.displayName || entry.username, "s") + "<b>" + esc(entry.hl ? entry.displayName : entry.username) + "</b><small>" + ago(s.createdAt) + '</small><span class="sp" style="flex:1"></span><button class="ib" data-x aria-label="Fechar">' + svg("x") + '</button></div><div class="stage">' + media + '<div class="zone" style="left:0" data-prev></div><div class="zone" style="right:0" data-next></div></div>' + (!isText && cp.text ? '<div class="cap2">' + esc(cp.text) + "</div>" : "") + foot;
      var bar = v.querySelectorAll(".bar i b")[i];
      var vid = v.querySelector("video");
      function next() { i++; show(); }
      var frm = v.querySelector("#srf");
      if (frm) {
        var inp = frm.querySelector("#sri"); inp.addEventListener("focus", function () { clearTimeout(timer); });
        frm.addEventListener("submit", function (e) { e.preventDefault(); var t = inp.value.trim(); if (!t) return; inp.value = ""; call(HOST + "/rest/v1/story_comments", { method: "POST", body: { story_id: s.id, user_id: me.id, body: t, kind: "comment" } }).then(function () { toast("Resposta enviada."); }).catch(onErr); });
      }
      if (vid) { vid.addEventListener("ended", next); } else {
        var t0 = Date.now(); (function tick() { if (document.activeElement && document.activeElement.id === "sri") { t0 = Date.now() - (bar ? parseFloat(bar.style.width) * 50 : 0); timer = setTimeout(tick, 200); return; } var p = Math.min(100, (Date.now() - t0) / 50); if (bar) bar.style.width = p + "%"; if (p < 100) timer = setTimeout(tick, 50); else next(); })();
      }
    }
    v.addEventListener("click", function (e) {
      var t = e.target.closest("[data-x],[data-prev],[data-next],[data-shl],[data-sdel],[data-sviews]"); if (!t) return;
      if (t.hasAttribute("data-x")) return end();
      var s = items[i];
      if (t.hasAttribute("data-sviews")) { clearTimeout(timer); storyInfo(items[i]); return; }
      if (t.hasAttribute("data-shl")) { clearTimeout(timer); var ti = prompt("Nome do destaque", "Destaques"); if (ti === null) return show(); api("/api/highlights", { method: "POST", body: { storyId: s.id, title: ti } }).then(function () { toast("Adicionado aos destaques."); }).catch(onErr); return; }
      if (t.hasAttribute("data-sdel")) { clearTimeout(timer); if (!confirm("Apagar este story?")) return show(); api("/api/stories/" + s.id, { method: "DELETE" }).then(function () { items.splice(i, 1); toast("Story apagado."); loadStories(); show(); }).catch(onErr); return; }
      if (t.hasAttribute("data-prev")) { i = Math.max(0, i - 1); } else { i++; }
      show();
    });
    show();
  }
  /* ---------- telas ---------- */
  function viewLogin(mode) {
    var add = /add/.test(location.hash), reg = mode === "register";
    var ios = /iphone|ipad|ipod/i.test(navigator.userAgent) && !window.navigator.standalone;
    $app.innerHTML = '<main class="login"><div class="logo">QINBLY</div><div class="sub">' + (reg ? "Crie sua conta" : "Entre na sua conta") + "</div>" +
      '<form class="form" id="lf" style="padding:0">' +
      (reg ? '<label class="fld">Nome de usuário<input class="inp" id="u" autocapitalize="none" autocomplete="username" placeholder="seu.usuario"></label>' : "") +
      '<label class="fld">' + (reg ? "E-mail" : "E-mail ou nome de usuário") + '<input class="inp" id="e" autocapitalize="none" autocomplete="' + (reg ? "email" : "username") + '" ' + (reg ? 'type="email"' : "") + "></label>" +
      '<label class="fld">Senha<input class="inp" id="p" type="password" autocomplete="' + (reg ? "new-password" : "current-password") + '"></label>' +
      '<div class="err" id="er"></div><button class="btn" id="go" type="submit">' + (reg ? "Criar conta" : "Entrar") + "</button></form>" +
      '<div class="alt" style="margin:16px 0 8px">ou</div><div id="gb" style="display:flex;justify-content:center;min-height:44px"></div><div class="err" id="ger" style="text-align:center"></div>' +
      (reg ? "" : '<div class="alt"><button data-forgot>Esqueci a senha</button></div>') +
      '<div class="alt">' + (reg ? 'Já tem conta? <button data-mode="login">Entrar</button>' : 'Não tem conta? <button data-mode="register">Criar conta</button>') + "</div>" +
      (add && accounts.length ? '<div class="alt"><button data-back>Cancelar</button></div>' : "") +
      '<div class="alt" style="font-size:12px">Entrou no app só com o Google? Defina uma senha em Configurações, no app, para usar aqui.</div>' +
      '<div class="alt" style="font-size:12px"><button class="link" data-install>Baixar o app</button></div>' +
      "</main>";
    initGoogle();
    var ue = $app.querySelector("#u");
    if (ue) { var ut; ue.addEventListener("input", function () { clearTimeout(ut); var v = ue.value.trim().toLowerCase(), er0 = $app.querySelector("#er"); if (v.length < 3) { er0.textContent = ""; return; } ut = setTimeout(function () { call(API + "/api/users/username-available?u=" + encodeURIComponent(v), { anon: true }).then(function (j) { er0.style.color = j.available ? "#4ade80" : ""; er0.textContent = j.available ? "Nome de usuário livre." : "Esse nome de usuário já existe."; }).catch(function () { }); }, 400); }); }
    var f = $app.querySelector("#lf"), er = $app.querySelector("#er"), go = $app.querySelector("#go");
    f.addEventListener("submit", function (e) {
      e.preventDefault(); er.textContent = ""; go.disabled = true;
      var id = f.querySelector("#e").value.trim(), pw = f.querySelector("#p").value, un = reg ? f.querySelector("#u").value.trim() : "", req;
      if (reg) req = call(API + "/api/auth/register", { method: "POST", anon: true, body: { email: id, password: pw, username: un } });
      else if (id.indexOf("@") > 0) req = call(API + "/api/auth/login", { method: "POST", anon: true, body: { email: id, password: pw } });
      else req = call(LOGIN_URL, { method: "POST", anon: true, body: { username: id, password: pw } });
      req.then(function (j) { if (!j.token) throw new Error("Não foi possível entrar agora."); putAccount(j); goto("#/"); })
        .catch(function (x) { er.textContent = x.message; go.disabled = false; });
    });
  }

  var GOOGLE_ID = "491553779497-bsgfj74l57u0gffi68jv6131joso0o5u.apps.googleusercontent.com";
  var gsiLoading = null;
  function loadGsi() {
    if (window.google && google.accounts && google.accounts.id) return Promise.resolve();
    if (!gsiLoading) gsiLoading = new Promise(function (ok, bad) {
      var sc = document.createElement("script"); sc.src = "https://accounts.google.com/gsi/client"; sc.async = true; sc.onload = ok; sc.onerror = function () { gsiLoading = null; bad(new Error("gsi")); };
      document.head.appendChild(sc);
    });
    return gsiLoading;
  }
  function initGoogle() {
    var box = document.getElementById("gb"); if (!box) return;
    loadGsi().then(function () {
      var b = document.getElementById("gb"); if (!b) return;
      google.accounts.id.initialize({
        client_id: GOOGLE_ID, ux_mode: "popup", auto_select: false,
        callback: function (r) {
          var er = document.getElementById("ger"); if (er) er.textContent = "";
          call(API + "/api/auth/google", { method: "POST", anon: true, body: { idToken: r.credential } })
            .then(function (j) { if (!j.token) throw new Error("Não foi possível entrar com o Google agora."); putAccount(j); goto("#/"); })
            .catch(function (x) { var e2 = document.getElementById("ger"); if (e2) e2.textContent = x.message; });
        }
      });
      google.accounts.id.renderButton(b, { theme: "filled_black", size: "large", shape: "pill", text: "continue_with", locale: "pt-BR", width: Math.min(300, b.clientWidth || 300) });
    }).catch(function () { var b = document.getElementById("gb"); if (b) b.innerHTML = '<span class="link" style="font-size:12px">Google indisponível agora. Use e-mail e senha.</span>'; });
  }

  var FEED = [], feedTab = "Descobrir", feedBefore = null, feedMore = false;
  var FTABS = ["Descobrir", "Vídeos", "Seguindo", "Em alta", "Lives"];
  var FTAB_LABEL = { "Descobrir": "Explorar", "Lives": "Ao vivo" };
  var FTAB_IC = { "Descobrir": "spark", "Seguindo": "groups", "Em alta": "flame", "Vídeos": "play", "Lives": "sensors", "Perto de mim": "pin" };
  function shortPlace() { var p = LS.get("qb_place", ""); if (!p) return ""; var a = p.split(","), c = a[0].trim(), st = (a[1] || "").trim(); return c + (st.length === 2 ? ", " + st : ""); }
  function hot(p) { return (p.likes || 0) * 2 + (p.comments || 0) * 3 + (p.reposts || 0) * 3; }
  /* ---------- Em alta (mesmo desenho do app) ---------- */
  var altaLives = [], altaLivesAt = 0;
  function altaLivesLoad() {
    if (Date.now() - altaLivesAt < 60000) return;
    altaLivesAt = Date.now();
    call(HOST + "/api/live?action=list").then(function (j) {
      altaLives = j.lives || [];
      if (altaLives.length && feedTab === "Em alta" && el("acar")) drawFeed();
    }).catch(function () { });
  }
  function altaCarInit() {
    var c = el("acar"), d = el("adots"); if (!c || !d) return;
    c.addEventListener("scroll", function () {
      var i = Math.round(c.scrollLeft / Math.max(1, c.clientWidth));
      Array.prototype.forEach.call(d.children, function (x, k) { x.className = k === i ? "on" : ""; });
    });
  }
  var altaPeriod = "Semana", ALTA_DAYS = { "Hoje": 1, "Semana": 7, "Mês": 30 };
  function altaPosts(period) {
    var lim = Date.now() - ALTA_DAYS[period || altaPeriod] * 86400000;
    return FEED.filter(function (p) { return !isHidden(p) && (p.mediaType === "video" || p.mediaType === "image") && safeUrl(p.mediaUrl) && Date.parse(p.createdAt) > lim; }).sort(function (x, y) { return hot(y) - hot(x); });
  }
  function cntFmt(n) {
    n = Number(n) || 0;
    var one = function (v) { return v.toFixed(1).replace(/\.0$/, "").replace(".", ","); };
    return n >= 1e6 ? one(n / 1e6) + " mi" : n >= 1e3 ? one(n / 1e3) + " mil" : String(n);
  }
  function thumbIn(p) {
    var u = safeUrl(p.mediaUrl);
    if (p.mediaType === "video" && u) return '<video src="' + u + '#t=0.1" preload="metadata" muted playsinline></video>';
    if (p.mediaType === "image" && u) return '<i style="background-image:url(\'' + u + '\')"></i>';
    return '<b class="ta">Aa</b>';
  }
  function arow(p, i) {
    var a = p.author || {}, t = String(p.caption || "").split("\n")[0].trim() || a.displayName || a.username || "";
    return '<a class="arow" href="#/post/' + esc(p.id) + '"><span class="rk r' + (i < 3 ? i + 1 : 0) + '">' + (i + 1) + '</span><span class="athumb">' + thumbIn(p) + '</span><span class="ainf"><b>' + esc(t) + "</b><small>@" + esc(a.username || "") + "</small><small>" + cntFmt(p.likes) + " curtidas · " + cntFmt(p.comments) + " comentários</small></span></a>";
  }
  function periodChips(attr, cur2) {
    return '<div class="pchips">' + ["Hoje", "Semana", "Mês"].map(function (x) { return '<button ' + attr + '="' + x + '" class="' + (x === cur2 ? "on" : "") + '">' + x + "</button>"; }).join("") + "</div>";
  }
  function altaHTML(l) {
    if (!l.length) return periodChips("data-aperiod", altaPeriod) + '<div class="empty">Nada em alta neste período.</div>';
    var out = "", cards = [];
    var best = function (fn) { var m = null; l.forEach(function (p) { if (fn(p) > 0 && (!m || fn(p) > fn(m))) m = p; }); return m; };
    var bl = best(function (p) { return p.likes || 0; }), bc = best(function (p) { return p.comments || 0; });
    if (bl) cards.push({ k: "likes", p: bl });
    if (bc) cards.push({ k: "comments", p: bc });
    if (altaLives.length) cards.push({ k: "live", lv: altaLives.slice().sort(function (x, y) { return y.viewers - x.viewers; })[0] });
    if (!cards.length) cards.push({ k: "likes", p: l[0] });
    out += '<div class="acw"><div class="acar" id="acar">' + cards.map(function (c) {
      var p = c.p, a = p ? (p.author || {}) : {}, u = p ? safeUrl(p.mediaUrl) : "";
      var hm = u ? (p.mediaType === "video" ? '<video src="' + u + '#t=0.1" preload="metadata" muted playsinline></video>' : p.mediaType === "image" ? '<i style="background-image:url(\'' + u + '\')"></i>' : "") : "";
      var href = c.lv ? "#/live/" + esc(c.lv.id) + "/" + encodeURIComponent(c.lv.host) : "#/post/" + esc(p.id);
      var chip = c.k === "likes" ? svg("heart") + "Mais curtido" : c.k === "comments" ? svg("comment") + "Mais comentado" : svg("flame") + "Live em alta";
      var title = c.lv ? (c.lv.displayName || c.lv.host) : c.k === "likes" ? "EM ALTA" : (String(p.caption || "").split("\n")[0].trim() || a.displayName || a.username || "");
      var sub = c.lv ? "@" + c.lv.host + " · " + cntFmt(c.lv.viewers) + " assistindo" : "@" + (a.username || "") + " · " + (c.k === "comments" ? cntFmt(p.comments) + " comentários" : cntFmt(p.likes) + " curtidas");
      return '<a class="ahero' + (c.lv ? " live" : "") + '" href="' + href + '">' + hm + (c.lv ? '<span class="alive"><u></u>AO VIVO</span>' : "") + '<span class="ahin"><span class="atag">' + chip + "</span><strong" + (c.k === "likes" ? ' class="big"' : "") + ">" + esc(title) + "</strong><small>" + esc(sub) + "</small></span></a>";
    }).join("") + "</div>" + (cards.length > 1 ? '<div class="adots" id="adots">' + cards.map(function (c, i) { return "<i" + (i === 0 ? ' class="on"' : "") + "></i>"; }).join("") + "</div>" : "") + "</div>";
    out += periodChips("data-aperiod", altaPeriod);
    var cm = {};
    l.forEach(function (p) { var a = p.author || {}, k = String(a.username || "").toLowerCase(); if (!k) return; (cm[k] = cm[k] || { a: a, n: 0 }).n += (p.likes || 0); });
    var cr = Object.keys(cm).map(function (k) { return cm[k]; }).sort(function (x, y) { return y.n - x.n; }).slice(0, 3);
    if (cr.length) out += '<h3 class="ash">Top criadores ' + { "Hoje": "de hoje", "Semana": "da semana", "Mês": "do mês" }[altaPeriod] + '</h3><div class="acre">' + cr.map(function (c, i) { return '<a href="#/u/' + encodeURIComponent(c.a.username) + '"><span class="acav">' + avatar(c.a.avatarUrl, c.a.displayName || c.a.username, "l") + '<em class="rk r' + (i + 1) + '">' + (i + 1) + "</em></span><b>" + esc(c.a.displayName || c.a.username) + "</b><small>" + cntFmt(c.n) + " curtidas</small></a>"; }).join("") + "</div>";
    var vids = l.filter(function (p) { return p.mediaType === "video" && safeUrl(p.mediaUrl); }).slice(0, 3);
    var others = l.filter(function (p) { return vids.indexOf(p) < 0; }).slice(0, 10);
    out += vids.length ? '<div class="ash2"><h3 class="ash">Vídeos em alta</h3><a class="asee" href="#/trending/all">Ver todos</a></div>' + vids.map(arow).join("") + (others.length ? '<h3 class="ash" style="margin-top:12px">Publicações em alta</h3>' : "") : '<h3 class="ash">Mais curtidas</h3>';
    return out + others.map(arow).join("");
  }
  /* "Ver todos": lista completa de vídeos em alta, com período, ordem e busca */
  function viewTrendingAll() {
    var per = altaPeriod, srt = "likes", q = "";
    setView(top("Vídeos em alta", { back: true, right: '<button class="ib" id="asb" aria-label="Buscar">' + svg("search") + "</button>" }) + '<div class="asrch" id="asw" hidden><input id="asq" placeholder="Buscar vídeo ou @usuário" autocomplete="off"></div><div id="ach"></div><div id="alist">' + spin() + "</div>", "trending", "noNav");
    function list() {
      var qq = q.trim().replace(/^@/, "").toLowerCase();
      var l = altaPosts(per).filter(function (p) { var a = p.author || {}; return p.mediaType === "video" && safeUrl(p.mediaUrl) && (!qq || String(p.caption || "").toLowerCase().indexOf(qq) >= 0 || String(a.username || "").toLowerCase().indexOf(qq) >= 0 || String(a.displayName || "").toLowerCase().indexOf(qq) >= 0); });
      l.sort(srt === "comments" ? function (x, y) { return (y.comments || 0) - (x.comments || 0); } : srt === "recent" ? function (x, y) { return Date.parse(y.createdAt) - Date.parse(x.createdAt); } : function (x, y) { return (y.likes || 0) - (x.likes || 0); });
      return l.slice(0, 50);
    }
    function draw() {
      var SO = [["likes", "Mais curtidos"], ["comments", "Mais comentados"], ["recent", "Recentes"]];
      el("ach").innerHTML = periodChips("data-allper", per) + '<div class="asort">' + SO.map(function (x) { return '<button data-allsort="' + x[0] + '" class="' + (x[0] === srt ? "on" : "") + '">' + x[1] + "</button>"; }).join("") + "</div>";
      var l = list();
      el("alist").innerHTML = l.length ? l.map(arow).join("") : '<div class="empty">' + (q.trim() ? "Nenhum vídeo encontrado." : "Nenhum vídeo em alta neste período.") + "</div>";
    }
    el("ach").addEventListener("click", function (e) {
      var b = e.target.closest("button"); if (!b) return;
      if (b.dataset.allper) per = b.dataset.allper; else if (b.dataset.allsort) srt = b.dataset.allsort; else return;
      draw();
    });
    el("asb").addEventListener("click", function () { var w = el("asw"); w.hidden = !w.hidden; if (w.hidden) { q = ""; el("asq").value = ""; draw(); } else el("asq").focus(); });
    el("asq").addEventListener("input", function () { q = this.value; el("alist").innerHTML = ""; draw(); });
    function go() { draw(); }
    if (FEED.length) go(); else api("/api/posts?limit=40").then(function (j) { FEED = j.posts || []; go(); }).catch(function (e) { el("alist").innerHTML = '<div class="empty">' + esc(e.message) + "</div>"; });
  }
  function feedList() {
    var hp = lget("hposts", []), l = FEED.filter(function (p) { return !isHidden(p) && hp.indexOf(p.id) < 0; });
    if (feedTab === "Seguindo") l = l.filter(function (p) { return p.isFollowing; });
    else if (feedTab === "Vídeos") l = l.filter(function (p) { return p.mediaType === "video"; });
    else if (feedTab === "Em alta") l = altaPosts().slice(0, 30);
    return l;
  }
  function viewFeed(tab) {
    if (tab) feedTab = tab;
    var mob = '<header class="top mob apphd"><button class="ib" data-moremenu aria-label="Menu e configurações">' + svg("menu") + '</button><h1 class="brandg">QINBLY</h1><div class="hr"><button class="ib" data-go="#/market" aria-label="Mercado e avisos">' + svg("bag") + '</button><button class="ib" data-go="#/search" aria-label="Buscar">' + svg("search") + '</button><button class="ib" data-go="#/notifications" aria-label="Notificações">' + svg("bell") + badge("notif", notifN) + "</button></div></header>";
    setView(mob + '<div class="ftabs">' + FTABS.map(function (t) { return '<button data-ftab="' + t + '" class="' + (t === feedTab ? "on" : "") + '">' + (FTAB_LABEL[t] || t) + "</button>"; }).join("") + '</div><div id="st"' + (feedTab === "Descobrir" ? "" : " hidden") + '></div><div id="fd">' + spin() + "</div>", tab ? "trending" : "home");
    loadStories();
    if (FEED.length) drawFeed();
    loadFeed(true);
  }
  function loadStories() {
    api("/api/stories").then(function (j) {
      var tray = (j.tray || []).filter(function (t) { return t.mine || (t.stories && t.stories.length) || t.note; });
      window.__tray = tray;
      var el = document.getElementById("st"); if (!el) return;
      window.__unseen = {}; tray.forEach(function (t) { if (!t.mine && t.hasUnseen) window.__unseen[String(t.username).toLowerCase()] = 1; });
      document.querySelectorAll(".post .ring[data-u]").forEach(function (r) { r.classList.toggle("new", !!window.__unseen[r.dataset.u]); });
      el.innerHTML = '<div class="stories">' + tray.map(function (t, i) {
        var has = t.stories && t.stories.length, cls = !has ? "none" : t.hasUnseen ? "new" : "seen";
        return '<div class="story">' + (t.note ? '<span class="note">' + esc(t.note.body) + "</span>" : "") + '<button class="ring2 ' + cls + '" data-story="' + i + '" aria-label="Ver story">' + avatar(t.avatarUrl, t.displayName || t.username) + "</button>" +
          (t.mine ? '<button class="plus" data-addstory aria-label="Criar story">' + svg("add") + "</button>" : "") + "<span>" + (t.mine ? "Seu story" : esc(t.displayName || t.username)) + "</span></div>";
      }).join("") + "</div>";
    }).catch(function () { /* stories são opcionais */ });
  }
  /* ---------- aba Vídeos: tela cheia, um vídeo por tela (como o app) ---------- */
  var REELIO = null, REELMUTE = true;
  var NOTE = '<svg viewBox="0 0 24 24" aria-hidden="true"><path d="M9 18V5l11-2v13"/><circle cx="6.5" cy="18" r="2.5"/><circle cx="17.5" cy="16" r="2.5"/></svg>';
  var SPK = '<svg viewBox="0 0 24 24" aria-hidden="true"><path d="M4 9v6h4l5 4V5L8 9z"/><path class="w" d="M16 9a4 4 0 0 1 0 6M18.5 6.5a8 8 0 0 1 0 11"/><path class="x" d="M16 9l5 6M21 9l-5 6"/></svg>';
  var ARW = '<svg class="arw" viewBox="0 0 34 34" aria-hidden="true"><path d="M18 4L31 16L18 28V22C12 22 8 24 4 29C4 18 8 11 18 10Z" stroke-linejoin="round"/></svg>';
  function reelHTML(p) {
    POSTS[p.id] = p;
    var a = p.author || {}, me = cur(), mine = me && p.authorId === me.id;
    var legend = String(p.caption || "").replace(/(\s*#[\p{L}\p{N}_]+)+\s*$/u, "").trim();
    var nm = esc(a.displayName || a.username);
    return '<section class="reel" data-reel="' + p.id + '"><video src="' + safeUrl(p.mediaUrl) + '" loop muted playsinline preload="metadata"></video>' +
      '<div class="rside">' +
      '<button class="ra' + (p.likedByMe ? " on" : "") + '" data-like="' + p.id + '" aria-label="Curtir">' + svg("heart") + "<span>" + (p.likes || "") + "</span></button>" +
      '<button class="ra" data-comments="' + p.id + '" aria-label="Comentários">' + svg("commentR") + "<span>" + (p.comments || "") + "</span></button>" +
      '<button class="ra' + (p.savedByMe ? " on" : "") + '" data-save="' + p.id + '" aria-label="Salvar">' + svg("save") + "<span>Salvar</span></button>" +
      '<button class="ra" data-share="' + p.id + '" aria-label="Enviar">' + ARW + "<span>Enviar</span></button>" +
      '<button class="ra" data-pmenu="' + p.id + '" aria-label="Mais opções">' + svg("dots") + "</button></div>" +
      '<div class="rinfo"><div class="rwho"><a href="#/u/' + esc(a.username) + '">' + avatar(a.avatarUrl, a.displayName || a.username, "m") + '</a><b><a href="#/u/' + esc(a.username) + '">@' + esc(a.username) + "</a></b>" +
      (!mine && !p.isFollowing && me ? '<button class="rfol" data-follow="' + p.authorId + '">Seguir</button>' : "") + "</div>" +
      (legend ? '<div class="rcap">' + linkify(legend) + "</div>" : "") +
      '<div class="rsrow"><span class="rsnd">' + NOTE + "Som original · " + nm + '</span><button class="rmute m" data-rmute aria-label="Ligar ou desligar o som">' + SPK + "</button></div></div>" +
      '<div class="rbar"><i></i></div></section>';
  }
  function repaintReel(rl, p) {
    var b;
    if ((b = rl.querySelector("[data-like]"))) { b.classList.toggle("on", !!p.likedByMe); b.lastElementChild.textContent = p.likes || ""; }
    if ((b = rl.querySelector("[data-comments]"))) b.lastElementChild.textContent = p.comments || "";
    if ((b = rl.querySelector("[data-save]"))) b.classList.toggle("on", !!p.savedByMe);
    if (p.isFollowing && (b = rl.querySelector(".rfol"))) b.remove();
  }
  function reelsInit() {
    var box = document.getElementById("reels"); if (!box) return;
    if (REELIO) { try { REELIO.disconnect(); } catch (e) { } }
    if ("IntersectionObserver" in window) {
      REELIO = new IntersectionObserver(function (es) {
        es.forEach(function (en) {
          var v = en.target.querySelector("video"); if (!v) return;
          if (en.isIntersecting && en.intersectionRatio >= 0.6) {
            v.muted = REELMUTE; var pr = v.play(); if (pr && pr.catch) pr.catch(function () { });
            var m = en.target.querySelector(".rmute"); if (m) m.classList.toggle("m", REELMUTE);
          } else { v.pause(); }
        });
      }, { root: box, threshold: [0, 0.6, 1] });
      box.querySelectorAll(".reel").forEach(function (r) { REELIO.observe(r); });
    }
    box.addEventListener("scroll", function () {
      var vw = document.querySelector("main.view"); if (vw) vw.classList.toggle("vscr", box.scrollTop > box.clientHeight * 0.5);
    });
    box.addEventListener("timeupdate", function (e) {
      var v = e.target; if (!v || !v.duration) return;
      var i = v.parentNode.querySelector(".rbar i"); if (i) i.style.width = Math.min(100, v.currentTime / v.duration * 100) + "%";
    }, true);
    box.addEventListener("click", function (e) {
      var rm = e.target.closest("[data-rmute],.rsnd");
      if (rm) { REELMUTE = !REELMUTE; box.querySelectorAll("video").forEach(function (v) { v.muted = REELMUTE; }); box.querySelectorAll(".rmute").forEach(function (m) { m.classList.toggle("m", REELMUTE); }); return; }
      if (e.target.closest("a,button,.rcap,.cx-more,.cx-less")) return;
      var r = e.target.closest(".reel"), v = r && r.querySelector("video"); if (!v) return;
      if (v.paused) { var pr = v.play(); if (pr && pr.catch) pr.catch(function () { }); } else v.pause();
    });
    box.addEventListener("dblclick", function (e) {
      if (e.target.closest("a,button,.rcap")) return;
      var r = e.target.closest(".reel"); if (!r) return;
      var lk = r.querySelector("[data-like]"); if (lk && !lk.classList.contains("on")) lk.click();
      var h = document.createElement("div"); h.className = "rburst"; h.innerHTML = svg("heart"); r.appendChild(h); setTimeout(function () { h.remove(); }, 750);
    });
  }
  function drawFeed() {
    var fd = document.getElementById("fd"); if (!fd) return;
    var vw = document.querySelector("main.view"); if (vw) { vw.classList.toggle("vid", feedTab === "Vídeos"); vw.classList.remove("vscr"); }
    if (feedTab === "Lives") return liveList();
    if (feedTab === "Perto de mim") return nearbyView();
    var l = feedList();
    if (feedTab === "Vídeos" && l.length) { fd.innerHTML = '<div class="reels" id="reels">' + l.map(reelHTML).join("") + (feedMore ? '<section class="reel rmoresec"><button class="more" data-more>Carregar mais</button></section>' : "") + "</div>"; reelsInit(); return; }
    var empty = { "Descobrir": "Ainda não há publicações. Toque em Criar e publique a primeira.", "Seguindo": "Siga pessoas para ver as publicações delas aqui.", "Em alta": "Nada em alta nos últimos 7 dias.", "Vídeos": "Ainda não há vídeos.", "Perto de mim": "" }[feedTab];
    if (feedTab === "Em alta") { fd.innerHTML = altaHTML(l); altaCarInit(); altaLivesLoad(); return; }
    fd.innerHTML = (feedTab === "Descobrir" ? homeHeader() : "") + (l.length ? l.map(postHTML).join("") : '<div class="empty">' + empty + "</div>");
    if (feedMore) fd.insertAdjacentHTML("beforeend", '<button class="more" data-more>Carregar mais</button>');
  }
  function loadFeed(first) {
    api("/api/posts?limit=40" + (first || !feedBefore ? "" : "&before=" + encodeURIComponent(feedBefore))).then(function (j) {
      var ps = j.posts || [];
      var rb = document.getElementById("reels"), jaRolou = first && FEED.length && (window.scrollY > 120 || (rb && rb.scrollTop > 60));
      FEED = first ? ps : FEED.concat(ps);
      if (ps.length) feedBefore = ps[ps.length - 1].createdAt;
      feedMore = ps.length >= 40;
      /* Como o Instagram: se a pessoa já começou a rolar, o feed novo não troca a tela por cima (aparece na próxima vez). */
      if (jaRolou) return;
      drawFeed();
    }).catch(function (e) { var fd = document.getElementById("fd"); if (first && !FEED.length && fd) fd.innerHTML = '<div class="empty">' + esc(e.message) + "</div>"; else onErr(e); });
  }

  function viewPost(id) {
    setView(top("Publicação", { back: true }) + '<div id="pp">' + spin() + "</div>", "", "noNav");
    api("/api/posts/" + id).then(function (j) { document.getElementById("pp").innerHTML = postHTML(j.post); })
      .catch(function (e) { document.getElementById("pp").innerHTML = '<div class="empty">' + esc(e.message) + "</div>"; });
  }

  function parseBio(bio) {
    var country = "", links = [], desc = [];
    String(bio || "").split("\n").forEach(function (l) {
      if (l.indexOf("✦ ") === 0) return;
      if (l.indexOf("🌍 ") === 0) country = l.slice(3).trim();
      else if (l.indexOf("🔗 ") === 0) links = l.slice(3).split(" | ").map(function (x) { return x.trim(); }).filter(Boolean).slice(0, 3);
      else desc.push(l);
    });
    return { cats: [], country: country, links: links, desc: desc.join("\n").trim() };
  }
  function linkClean(x) { return String(x || "").replace(/[\s|]/g, "").slice(0, 80); }
  function linkLabel(x) { return String(x).replace(/^https?:\/\//i, "").replace(/^www\./i, "").replace(/\/$/, ""); }
  function linksHtml(list) {
    list = (list || []).filter(Boolean); if (!list.length) return "";
    var w = Math.min(window.innerWidth || 400, 470) - 32, chars = list.reduce(function (n, l) { return n + linkLabel(l).length; }, 0) || 1;
    var px = Math.max(9, Math.min(14, (w - list.length * 22 - (list.length - 1) * 12) / (chars * 0.53)));
    var ic = '<svg viewBox="0 0 24 24"><path d="M10 14a4 4 0 005.7 0l3-3a4 4 0 00-5.7-5.7l-1 1"/><path d="M14 10a4 4 0 00-5.7 0l-3 3a4 4 0 005.7 5.7l1-1"/></svg>';
    return '<div class="plk" style="font-size:' + px.toFixed(1) + 'px">' + list.map(function (l) {
      var url = /^https?:\/\//i.test(l) ? l : "https://" + l;
      if (!/^https?:\/\/[^\s"'<>]+$/i.test(url)) return "";
      return '<a href="' + esc(url) + '" target="_blank" rel="noopener noreferrer">' + ic + "<span>" + esc(linkLabel(l)) + "</span></a>";
    }).join("") + "</div>";
  }
  function joinedTxt(u, short) {
    var d = u.createdAt || u.created_at; if (!d) return ""; var dt = new Date(d); if (isNaN(dt)) return "";
    return "Entrou em " + dt.toLocaleDateString("pt-BR", { month: short ? "short" : "long", year: "numeric" }).replace(".", "");
  }
  function viewProfile(name) {
    var me = cur(), isMeRoute = name === "me";
    if (isMeRoute) name = me.username;
    var hdr = isMeRoute
      ? '<header class="top pfhd"><span class="sp"></span><button class="ib" data-go="#/settings" aria-label="Configurações do perfil">' + svg("menu") + "</button></header>"
      : '<header class="top pfhd"><button class="ib" data-back aria-label="Voltar">' + svg("back") + '</button><span class="sp"></span><button class="ib" data-umenu aria-label="Opções deste perfil">' + svg("dots") + "</button></header>";
    setView(hdr + '<div id="pf">' + spin() + "</div>", isMeRoute ? "me" : "");
    api("/api/users/" + encodeURIComponent(name)).then(function (j) {
      var u = j.user, posts = (j.posts || []).slice().sort(function (a, b) { return (b.pinned ? 1 : 0) - (a.pinned ? 1 : 0); }), own = me && u.id === me.id;
      window.__pu = u;
      if (own) { me.displayName = u.displayName; me.avatarUrl = u.avatarUrl; me.username = u.username; save(); }
      posts.forEach(function (p) { if (!p.author) p.author = { username: u.username, displayName: u.displayName, avatarUrl: u.avatarUrl, verified: u.verified }; if (!p.authorId) p.authorId = u.id; });
      var bio = parseBio(u.bio), shown = u.displayName || u.username, unseen = (window.__unseen || {})[String(u.username).toLowerCase()];
      var tabs = own ? ["Publicações", "Vídeos", "Fotos", "Salvos"] : ["Publicações", "Vídeos", "Fotos"];
      var TIC = { "Publicações": "grid", "Vídeos": "vdisp", "Fotos": "photo", "Salvos": "save" };
      var likes = posts.reduce(function (n, p) { return n + (Number(p.likes) || 0); }, 0);
      var INFO = '<svg viewBox="0 0 24 24"><circle cx="12" cy="12" r="9"/><path d="M12 11v5M12 8h.01"/></svg>';
      var handle = own ? '<button class="at3" data-accounts>@' + esc(u.username) + svg("chev") + "</button>" : '<span class="at3">@' + esc(u.username) + "</span>";
      var where = bio.country ? '<span class="dot">·</span><span class="pl">' + svg("pin") + esc(bio.country) + "</span>" : "";
      var topH = '<div class="ptop"><span class="pring xl s ' + (unseen ? "new" : "") + '">' + avatar(u.avatarUrl, shown, "l") + '</span><div class="pid"><div class="pnm">' + (own ? '<button data-edit><span>' + esc(shown) + "</span></button>" : "<span>" + esc(shown) + "</span>") + (u.verified ? VERIFIED : "") + '</div><div class="pat">' + handle + where + '<button class="inf" data-about aria-label="Sobre este perfil">' + INFO + '</button></div><div class="pn3"><div><b>' + likes + '</b><span>Curtidas</span></div><button data-stat="following"><b>' + (u.following || 0) + '</b><span>Seguindo</span></button><button data-stat="followers"><b>' + (u.followers || 0) + "</b><span>Seguidores</span></button></div></div></div>";
      var btns = own
        ? '<div class="pbt"><button class="pill" data-edit>Editar perfil</button><button class="pill" data-shareprofile="' + esc(u.username) + '">Compartilhar perfil</button></div>'
        : '<div class="pfb"><button class="fol ' + (u.isFollowing ? "on" : "") + '" data-follow="' + u.id + '" data-profile="' + esc(u.username) + '">' + svg(u.isFollowing ? "check" : "add") + "<span>" + (u.isFollowing ? "Seguindo" : "Seguir") + '</span></button><button class="msgb" data-chat="' + esc(u.username) + '">' + svg("msg") + "Mensagem</button></div>";
      var bioHtml = bio.desc ? '<div class="pbio2"><p>' + linkify(bio.desc) + "</p></div>" : (own ? '<div class="pbio2"><p class="mut">Adicione uma descrição ao seu perfil</p></div>' : "");
      var head = topH + bioHtml + linksHtml(bio.links) + btns;
      function gridOf(list) {
        return list.map(function (p) { POSTS[p.id] = p; return '<button data-open="' + p.id + '" style="' + (p.mediaUrl ? "background-image:url('" + safeUrl(p.mediaUrl) + "')" : "") + '"></button>'; }).join("");
      }
      function listOf(list, emptyTxt, icon) {
        if (!list.length) return '<div class="pemp">' + svg(icon) + "<b>" + emptyTxt + "</b></div>";
        return list.map(postHTML).join("");
      }
      function show(tab) {
        var pg = document.getElementById("pg"), pf = document.getElementById("pf");
        pf.querySelectorAll("[data-ptab]").forEach(function (b) { var on = b.dataset.ptab === tab; b.classList.toggle("on", on); });
        if (tab === "Fotos") { var ph = posts.filter(function (p) { return p.mediaType === "image"; }); pg.innerHTML = ph.length ? '<div class="pgrid">' + gridOf(ph) + "</div>" : '<div class="pemp">' + svg("photo") + "<b>Nenhuma foto ainda</b></div>"; return; }
        if (tab === "Vídeos") { pg.innerHTML = listOf(posts.filter(function (p) { return p.mediaType === "video"; }), "Nenhum vídeo ainda", "play"); return; }
        if (tab === "Salvos") { pg.innerHTML = spin(); api("/api/me/saved").then(function (r) { var l = r.posts || []; pg.innerHTML = l.length ? l.map(postHTML).join("") : '<div class="pemp">' + svg("save") + "<b>Nenhuma publicação salva</b><small>Toque no marcador de uma publicação para guardá-la aqui. Só você vê os seus salvos.</small></div>"; }).catch(onErr); return; }
        pg.innerHTML = listOf(posts, own ? "Você ainda não publicou nada" : "Nenhuma publicação pública encontrada nesta conta", "grid");
      }
      var pf = document.getElementById("pf");
      pf.innerHTML = head + '<div id="hl"></div><div class="ptabs">' + tabs.map(function (t, i) { return '<button data-ptab="' + t + '" aria-label="' + t + '" class="' + (i ? "" : "on") + '">' + svg(TIC[t]) + "<i></i></button>"; }).join("") + '</div><div id="pg"></div>';
      show("Publicações");
      pf.addEventListener("click", function (e) {
        var t = e.target.closest("[data-ptab],[data-hl],[data-about]"); if (!t) return;
        if (t.hasAttribute("data-about")) { sheet("Sobre este perfil", '<div class="row"><span class="t"><b>' + esc(joinedTxt(u, false) || "Sem informações de entrada") + "</b></span></div>" + (bio.country ? '<div class="row"><span class="t"><b>País: ' + esc(bio.country) + "</b></span></div>" : "")); return; }
        if (t.dataset.hl !== undefined) { var h = (window.__hls || [])[Number(t.dataset.hl)]; if (h) openHighlight(h); return; }
        show(t.dataset.ptab);
      });
      api("/api/users/" + encodeURIComponent(u.username) + "/highlights").then(function (r) {
        var l = r.highlights || [], groups = [], idx = {};
        l.forEach(function (h) { if (idx[h.title] === undefined) { idx[h.title] = groups.length; groups.push({ title: h.title, items: [] }); } groups[idx[h.title]].items.push(h); });
        window.__hls = groups; var el = document.getElementById("hl"); if (!el || !groups.length) return;
        el.innerHTML = '<div class="hls">' + groups.map(function (g, i) { var h = g.items[0]; return '<button data-hl="' + i + '" aria-label="Favorito"><span class="hc2" ' + (h.mediaType === "image" && h.mediaUrl ? 'style="background-image:url(\'' + safeUrl(h.mediaUrl) + '\')"' : "") + ">" + (h.mediaType === "image" && h.mediaUrl ? "" : "Aa") + "</span><em>" + esc(g.title) + "</em></button>"; }).join("") + "</div>";
      }).catch(function () { });
    }).catch(function (e) { document.getElementById("pf").innerHTML = '<div class="empty">' + esc(e.message) + "</div>"; });
  }
  function peopleSheet(name, kind) {
    var s = sheet(kind === "followers" ? "Seguidores" : "Seguindo", spin());
    api("/api/users/" + encodeURIComponent(name) + "/" + kind).then(function (j) {
      var l = j.users || []; s.querySelector(".body").innerHTML = l.length ? l.map(function (u) { return '<a class="row" href="#/u/' + esc(u.username) + '">' + avatar(u.avatarUrl, u.displayName || u.username) + '<span class="t"><b>' + esc(u.displayName || u.username) + "</b><small>@" + esc(u.username) + "</small></span></a>"; }).join("") : '<div class="empty">Ninguém por aqui ainda.</div>';
    }).catch(function (e) { s.querySelector(".body").innerHTML = '<div class="empty">' + esc(e.message) + "</div>"; });
  }
  function userMenu() {
    var u = window.__pu; if (!u) return;
    var s = sheet("@" + u.username, '<button class="row" data-um="share"><span class="t"><b>Compartilhar perfil</b></span></button><button class="row" data-um="report"><span class="t"><b>Denunciar</b></span></button><button class="row" data-um="block"><span class="t"><b style="color:var(--danger)">Bloquear</b></span></button>');
    s.addEventListener("click", function (e) {
      var b = e.target.closest("[data-um]"); if (!b) return; closeSheet();
      if (b.dataset.um === "share") { var url = new URL("#/u/" + u.username, location.href).href; if (navigator.share) navigator.share({ title: "QINBLY", url: url }).catch(function () { }); else if (navigator.clipboard) navigator.clipboard.writeText(url).then(function () { toast("Link copiado."); }); }
      else if (b.dataset.um === "report") reportSheet("user", u.id);
      else confirmSheet("Bloquear @" + u.username + "?", "Vocês deixam de ver as publicações um do outro.", "Bloquear", true, function () { api("/api/users/" + u.id + "/block", { method: "POST", body: {} }).then(function (r) { toast(r.blocked ? "Conta bloqueada." : "Conta desbloqueada."); FEED = []; location.hash = "#/"; }).catch(onErr); });
    });
  }

  /* ---------- mercado completo ---------- */
  var MK_CATS = [["Tudo", ""], ["Vendas", "venda"], ["Doações", "doacao"], ["Achados", "achado,perdido"], ["Eventos", "evento"], ["Serviços", "servico"], ["Avisos", "aviso"], ["Meus", "@me"], ["Salvos", "@saved"]];
  var MKS = {};
  function mkCols() { return "id,user_id,kind,title,description,price,extra,photos,city,created_at,profiles!marketplace_listings_user_id_fkey(username,display_name)"; }
  function viewMarket() {
    setView(top("Mercado", { right: '<button class="ib" data-newlisting aria-label="Anunciar">' + svg("plus") + "</button>" }) + '<div class="chips">' + MK_CATS.map(function (c, i) { return '<button data-mk="' + i + '" class="' + (i === mkCat ? "on" : "") + '">' + c[0] + "</button>"; }).join("") + '</div><div style="padding:0 16px 8px"><input class="inp" id="city" placeholder="Filtrar por cidade (opcional)" value="' + esc(mkCity) + '"></div><div id="ml">' + spin() + "</div>", "market");
    var ci = document.getElementById("city"), t;
    ci.addEventListener("input", function () { clearTimeout(t); t = setTimeout(function () { mkCity = ci.value.trim(); LS.set("qb_city", mkCity); load(); }, 400); });
    function paint(l) {
      var ml = document.getElementById("ml"); if (!ml) return;
      l.forEach(function (x) { MK[x.id] = x; });
      ml.innerHTML = l.length ? l.map(function (x) {
        var ph = (x.photos || [])[0], pr = x.price != null && x.price !== "" ? Number(x.price).toLocaleString("pt-BR", { style: "currency", currency: "BRL" }) : "";
        return '<button class="row" data-listing="' + x.id + '"><span class="av" style="width:64px;height:64px;border-radius:10px;' + (ph ? "background-image:url('" + safeUrl(photoUrl(ph)) + "')" : "") + '"></span><span class="t"><b>' + esc(x.title) + "</b><small>" + (pr ? pr + " · " : "") + esc(MK_LABEL[x.kind] || "Aviso") + (x.city ? " · " + esc(x.city) : "") + " · " + ago(x.created_at) + "</small></span></button>";
      }).join("") : '<div class="empty">' + (MK_CATS[mkCat][1] === "@me" ? "Você ainda não anunciou nada. Toque no + para anunciar." : "Nada por aqui" + (mkCity ? " em " + esc(mkCity) : "") + " ainda.") + "</div>";
    }
    function load() {
      var kinds = MK_CATS[mkCat][1], q;
      if (kinds === "@saved") q = "/rest/v1/marketplace_saves?select=created_at,marketplace_listings(" + mkCols() + ")&order=created_at.desc&limit=100";
      else {
        q = "/rest/v1/marketplace_listings?select=" + mkCols() + "&order=created_at.desc&limit=60";
        if (kinds === "@me") q += "&user_id=eq." + cur().id;
        else { if (kinds) q += "&kind=in.(" + kinds + ")"; if (mkCity) q += "&city=ilike.*" + encodeURIComponent(mkCity.replace(/[,()*]/g, " ")) + "*"; }
      }
      call(HOST + q).then(function (l) {
        if (!Array.isArray(l)) l = [];
        if (kinds === "@saved") l = l.map(function (x) { return x.marketplace_listings; }).filter(Boolean);
        paint(l);
      }).catch(function (e) { var ml = document.getElementById("ml"); if (ml) ml.innerHTML = '<div class="empty">' + esc(e.message) + "</div>"; });
    }
    call(HOST + "/rest/v1/marketplace_saves?select=listing_id&limit=500").then(function (r) { MKS = {}; (Array.isArray(r) ? r : []).forEach(function (x) { MKS[x.listing_id] = 1; }); }).catch(function () { });
    load();
  }
  function openListing(id) {
    var x = MK[id]; if (!x) return;
    var p = x.profiles || {}, me = cur(), own = me && x.user_id === me.id, pr = x.price != null && x.price !== "" ? Number(x.price).toLocaleString("pt-BR", { style: "currency", currency: "BRL" }) : "";
    var s = sheet(x.title, '<div class="gal">' + (x.photos || []).map(function (u) { return '<img src="' + safeUrl(photoUrl(u)) + '" alt="">'; }).join("") + '</div><div style="padding:12px 16px"><b style="font-size:20px">' + pr + "</b><div style='color:var(--muted);font-size:12px;margin:4px 0 8px'>" + esc(MK_LABEL[x.kind] || "Aviso") + (x.city ? " · " + esc(x.city) : "") + "</div>" + (x.description ? "<div class='cap' style='padding:0'>" + linkify(x.description) + "</div>" : "") + (x.extra ? "<div style='color:var(--muted);font-size:14px;margin-top:8px'>" + esc(x.extra) + "</div>" : "") + "</div>" +
      (p.username ? '<a class="row" href="#/u/' + esc(p.username) + '"><span class="t"><b>' + esc(p.display_name || p.username) + "</b><small>@" + esc(p.username) + "</small></span></a>" : "") +
      '<div style="padding:8px 16px 16px;display:flex;flex-direction:column;gap:8px">' + (own ? '<button class="btn" id="mkd" style="background:var(--danger)">Excluir anúncio</button>' : (p.username ? '<button class="btn" data-chat="' + esc(p.username) + '">Falar com o vendedor</button>' : "") + '<button class="btn ghost" id="mks">' + (MKS[id] ? "Remover dos salvos" : "Salvar anúncio") + "</button>") + "</div>");
    var d = s.querySelector("#mkd"); if (d) d.addEventListener("click", function () { call(HOST + "/rest/v1/marketplace_listings?id=eq." + id, { method: "DELETE" }).then(function () { closeSheet(); toast("Anúncio excluído."); viewMarket(); }).catch(onErr); });
    var sv = s.querySelector("#mks"); if (sv) sv.addEventListener("click", function () {
      (MKS[id] ? call(HOST + "/rest/v1/marketplace_saves?listing_id=eq." + id, { method: "DELETE" }) : call(HOST + "/rest/v1/marketplace_saves", { method: "POST", body: { listing_id: id, user_id: me.id } })).then(function () { if (MKS[id]) delete MKS[id]; else MKS[id] = 1; closeSheet(); toast(MKS[id] ? "Anúncio salvo." : "Removido dos salvos."); }).catch(onErr);
    });
  }
  function viewEdit() {
    var me = cur();
    setView(top("Editar perfil", { back: true }) + '<div class="form" id="ef">' + spin() + "</div>", "", "noNav");
    api("/api/auth/me").then(function (j) {
      var u = j.user, P = parseBio(u.bio), COUNTRIES = ["Brasil", "Portugal", "Estados Unidos", "Argentina", "Chile", "Colômbia", "México", "Espanha", "França", "Alemanha", "Itália", "Reino Unido", "Canadá", "Japão", "Angola", "Moçambique", "Outro"];
      document.getElementById("ef").innerHTML =
        '<div style="text-align:center"><span class="ring">' + avatar(u.avatarUrl, u.displayName || u.username, "l") + '</span><br><label class="link" style="cursor:pointer;display:inline-block;padding:10px;font-weight:600">Trocar foto<input id="af" type="file" accept="image/*" hidden></label></div>' +
        '<label class="fld">Nome<input class="inp" id="dn" maxlength="60" value="' + esc(u.displayName) + '"></label>' +
        '<label class="fld">Nome de usuário<input class="inp" id="un" maxlength="30" autocapitalize="none" value="' + esc(u.username) + '"></label>' +
        '<label class="fld">Bio<textarea class="inp" id="bi" maxlength="300">' + esc(P.desc) + '</textarea></label>' +
        '<label class="fld">País<select class="inp" id="ct"><option value="">Selecionar meu país</option>' + COUNTRIES.map(function (c) { return '<option' + (c === P.country ? " selected" : "") + ">" + esc(c) + "</option>"; }).join("") + "</select></label>" +
        '<div class="fld">Links (até 3)' + [0, 1, 2].map(function (i) { return '<input class="inp lkin" maxlength="80" inputmode="url" autocapitalize="none" placeholder="meusite.com" value="' + esc(P.links[i] || "") + '">'; }).join("") + '</div><div class="err" id="er"></div><button class="btn" id="sv">Salvar</button>';
      var ef = document.getElementById("ef");
      ef.querySelector("#af").addEventListener("change", function (e) {
        var f = e.target.files[0]; if (!f) return;
        toast("Enviando foto…");
        shrink(f).then(function (b) { return upload(b, "avatar"); })
          .then(function (path) { return api("/api/users/me/avatar", { method: "PATCH", body: { fileId: path } }); })
          .then(function (r) { me.avatarUrl = r.user.avatarUrl; save(); toast("Foto atualizada."); viewEdit(); }).catch(onErr);
      });
      ef.querySelector("#sv").addEventListener("click", function () {
        var btn = this; btn.disabled = true; ef.querySelector("#er").textContent = "";
        var lks = Array.prototype.map.call(ef.querySelectorAll(".lkin"), function (i) { return linkClean(i.value); }).filter(Boolean).slice(0, 3);
        if (lks.some(function (l) { return l.indexOf(".") < 1 || l.slice(-1) === "."; })) { ef.querySelector("#er").textContent = "Um dos links não parece certo. Use algo como meusite.com."; btn.disabled = false; return; }
        var ctry = ef.querySelector("#ct").value, composed = (ctry ? "🌍 " + ctry + "\n" : "") + (lks.length ? "🔗 " + lks.join(" | ") + "\n" : "") + ef.querySelector("#bi").value.trim();
        api("/api/users/me", { method: "PATCH", body: { displayName: ef.querySelector("#dn").value, username: ef.querySelector("#un").value, bio: composed } })
          .then(function (r) { me.username = r.user.username; me.displayName = r.user.displayName; save(); toast("Perfil salvo."); location.hash = "#/me"; })
          .catch(function (e) { ef.querySelector("#er").textContent = e.message; btn.disabled = false; });
      });
    }).catch(onErr);
  }

  function viewNew() {
    var kind = "text", bg = 0, files = [], vis = "public", dur = 24, multi = false, sched = "", tags = "", opts = ["", ""];
    setView(top("Nova publicação") + '<div class="form" id="nf"></div>', "new");
    var nf = document.getElementById("nf");
    function draw() {
      var K = [["text", "Texto"], ["image", "Foto"], ["video", "Vídeo"], ["poll", "Enquete"]];
      nf.innerHTML =
        '<div class="seg">' + K.map(function (k) { return '<button data-k="' + k[0] + '" class="' + (kind === k[0] ? "on" : "") + '">' + k[1] + "</button>"; }).join("") + "</div>" +
        (kind === "text" ? '<div class="bgs"><button data-bg="0" class="' + (bg === 0 ? "on" : "") + '" style="background:var(--surface);border-color:' + (bg === 0 ? "#fff" : "var(--line)") + '" aria-label="Sem fundo"></button>' + TEXT_BGS.map(function (_, i) { return '<button data-bg="' + (i + 1) + '" class="' + (bg === i + 1 ? "on" : "") + '" style="background:' + bgCss(i + 1) + '" aria-label="Fundo ' + (i + 1) + '"></button>'; }).join("") + "</div>" : "") +
        (kind === "image" || kind === "video" ? '<label class="btn ghost" style="cursor:pointer">' + (files.length ? "Trocar " + (kind === "image" ? "fotos" : "vídeo") : "Escolher " + (kind === "image" ? "fotos" : "vídeo")) + '<input id="fi" type="file" hidden accept="' + (kind === "image" ? "image/*" : "video/*") + '" ' + (kind === "image" ? "multiple" : "") + "></label>" : "") +
        (files.length ? '<div class="pick">' + files.map(function (f, i) { return '<div class="th" style="' + (kind === "image" ? "background-image:url(" + URL.createObjectURL(f) + ")" : "") + '">' + (kind === "video" ? '<span style="position:absolute;inset:0;display:flex;align-items:center;justify-content:center">▶</span>' : "") + '<button data-rm="' + i + '" aria-label="Remover">×</button></div>'; }).join("") + "</div>" : "") +
        '<textarea class="inp" id="cp" maxlength="5000" placeholder="' + (kind === "text" ? "Escreva algo…" : kind === "poll" ? "Pergunta da enquete" : "Escreva uma legenda…") + '"></textarea>' +
        (kind === "poll" ? opts.map(function (o, i) { return '<input class="inp po" data-oi="' + i + '" maxlength="80" placeholder="Opção ' + (i + 1) + '" value="' + esc(o) + '">'; }).join("") + (opts.length < 6 ? '<button class="btn ghost" data-addopt>+ Adicionar opção</button>' : "") +
          '<label class="fld">Duração<select class="inp" id="du">' + [[1, "1 hora"], [6, "6 horas"], [24, "1 dia"], [72, "3 dias"], [168, "7 dias"]].map(function (d) { return '<option value="' + d[0] + '"' + (dur === d[0] ? " selected" : "") + ">" + d[1] + "</option>"; }).join("") + '</select></label><button class="row" data-multi style="padding:8px 0"><span class="t"><b>Permitir várias respostas</b></span><span class="tog ' + (multi ? "on" : "") + '"><i></i></span></button>' : "") +
        '<label class="fld">Marcar pessoas (@usuario, separados por vírgula)<input class="inp" id="tg" value="' + esc(tags) + '" autocapitalize="none"></label>' +
        '<label class="fld">Agendar publicação (opcional)<input class="inp" id="sc" type="datetime-local" value="' + esc(sched) + '"></label>' +
        '<div class="seg"><button data-v="public" class="' + (vis === "public" ? "on" : "") + '">Público</button><button data-v="private" class="' + (vis === "private" ? "on" : "") + '">Só eu</button></div>' +
        '<div class="err" id="er"></div><button class="btn" id="pb">' + (sched ? "Agendar" : "Publicar") + "</button>";
      var cp = nf.querySelector("#cp"); cp.value = draw.cap || ""; cp.addEventListener("input", function () { draw.cap = cp.value; });
      nf.querySelector("#tg").addEventListener("input", function (e) { tags = e.target.value; });
      nf.querySelector("#sc").addEventListener("change", function (e) { sched = e.target.value; nf.querySelector("#pb").textContent = sched ? "Agendar" : "Publicar"; });
      var du = nf.querySelector("#du"); if (du) du.addEventListener("change", function () { dur = Number(du.value); });
      nf.querySelectorAll(".po").forEach(function (inp) { inp.addEventListener("input", function () { opts[Number(inp.dataset.oi)] = inp.value; }); });
      var fi = nf.querySelector("#fi");
      if (fi) fi.addEventListener("change", function (e) {
        var list = Array.prototype.slice.call(e.target.files).slice(0, 10), okTypes = kind === "image" ? IMG_OK : VID_OK;
        files = list.filter(function (f) { return okTypes.indexOf(f.type) >= 0; });
        if (files.length < list.length) toast("Alguns arquivos não são aceitos (use JPG, PNG, WebP" + (kind === "video" ? ", MP4, MOV ou WebM" : "") + ").");
        draw();
      });
    }
    nf.addEventListener("click", function (e) {
      var t = e.target.closest("button"); if (!t) return;
      if (t.dataset.k) { kind = t.dataset.k; files = []; draw(); }
      else if (t.dataset.bg !== undefined) { bg = Number(t.dataset.bg); draw(); }
      else if (t.dataset.v) { vis = t.dataset.v; draw(); }
      else if (t.dataset.rm !== undefined) { files.splice(Number(t.dataset.rm), 1); draw(); }
      else if (t.hasAttribute("data-addopt")) { opts.push(""); draw(); }
      else if (t.hasAttribute("data-multi")) { multi = !multi; draw(); }
      else if (t.id === "pb") publish(t);
    });
    function publish(btn) {
      var er = nf.querySelector("#er"), caption = nf.querySelector("#cp").value.trim(); er.textContent = "";
      if ((kind === "text" || kind === "poll") && !caption) { er.textContent = kind === "poll" ? "Escreva a pergunta da enquete." : "Escreva algo para publicar."; return; }
      var pollOpts = opts.map(function (o) { return o.trim(); }).filter(Boolean);
      if (kind === "poll" && pollOpts.length < 2) { er.textContent = "A enquete precisa de pelo menos 2 opções."; return; }
      if ((kind === "image" || kind === "video") && !files.length) { er.textContent = kind === "image" ? "Escolha pelo menos uma foto." : "Escolha um vídeo."; return; }
      btn.disabled = true; btn.textContent = "Publicando…";
      var chain = Promise.resolve([]);
      if (kind === "image" || kind === "video") {
        chain = files.reduce(function (acc, f) { return acc.then(function (ids) { return (kind === "image" ? shrink(f) : Promise.resolve(f)).then(function (b) { return upload(b, "post_media"); }).then(function (id) { return ids.concat(id); }); }); }, Promise.resolve([]));
      }
      chain.then(function (ids) {
        var body = { caption: caption, mediaType: kind, visibility: vis };
        if (kind === "text" && bg) body.textBg = bg;
        if (ids.length) { body.mediaFileId = ids[0]; if (ids.length > 1) body.mediaFileIds = ids; }
        if (kind === "poll") { body.pollOptions = pollOpts; body.durationHours = dur; body.multipleAnswers = multi; }
        var tg = tags.split(",").map(function (x) { return x.trim().replace(/^@/, ""); }).filter(Boolean); if (tg.length) body.taggedUsernames = tg;
        if (sched) body.scheduledAt = new Date(sched).toISOString();
        return api("/api/posts", { method: "POST", body: body });
      }).then(function () { draw.cap = ""; feedBefore = null; toast(sched ? "Publicação agendada." : "Publicado."); location.hash = "#/"; })
        .catch(function (x) { er.textContent = x.message; btn.disabled = false; btn.textContent = sched ? "Agendar" : "Publicar"; });
    }
    draw.cap = ""; if (NEWPRE) { kind = NEWPRE.kind; draw.cap = NEWPRE.text; NEWPRE = null; } draw();
  }
  function findPeople() {
    var s = sheet("Nova conversa", '<div style="padding:0 16px 8px"><input class="inp" id="q" placeholder="Procurar por nome ou @usuário" autocomplete="off"></div><div id="qr"></div>');
    var t; s.querySelector("#q").addEventListener("input", function (e) {
      clearTimeout(t); var q = e.target.value.trim(); if (!q) { s.querySelector("#qr").innerHTML = ""; return; }
      t = setTimeout(function () {
        api("/api/users/search?q=" + encodeURIComponent(q.replace(/^@/, ""))).then(function (j) {
          s.querySelector("#qr").innerHTML = (j.users || []).map(function (u) {
            return '<button class="row" data-chat="' + esc(u.username) + '">' + avatar(u.avatarUrl, u.displayName || u.username) + '<span class="t"><b>' + esc(u.displayName || u.username) + "</b><small>@" + esc(u.username) + "</small></span></button>";
          }).join("") || '<div class="empty">Ninguém encontrado.</div>';
        }).catch(onErr);
      }, 300);
    });
    setTimeout(function () { var q = s.querySelector("#q"); if (q) q.focus(); }, 50);
  }

  function accountsSheet() {
    var s = sheet("Suas contas", accounts.map(function (a) {
      return '<div class="row" style="padding:0 16px 0 0"><button class="row" data-switch="' + a.id + '">' + avatar(a.avatarUrl, a.displayName || a.username) + '<span class="t"><b>' + esc(a.displayName || a.username) + "</b><small>@" + esc(a.username) + "</small></span>" + (a.id === activeId ? '<span class="link">●</span>' : "") + '</button><button class="link" data-drop="' + a.id + '" style="font-size:12px;color:var(--muted)">Sair</button></div>';
    }).join("") + '<button class="row" data-addacc><span class="av">+</span><span class="t"><b>Adicionar outra conta</b></span></button>');
    return s;
  }
  function settingsSheet() {
    sheet("Configurações",
      '' +
      '<button class="row" data-install><span class="t"><b>Instalar o app</b><small>Instala direto no celular</small></span></button>' +
      '<a class="row" href="privacidade.html"><span class="t"><b>Privacidade</b></span></a>' +
      '<a class="row" href="termos.html"><span class="t"><b>Termos de uso</b></span></a>' +
      '<button class="row" data-drop="' + activeId + '"><span class="t"><b style="color:var(--danger)">Sair desta conta</b></span></button>');
  }

  /* ---------- buscar, em alta por assunto, salvos ---------- */
  function postsInto(id, posts, empty) {
    var el = document.getElementById(id); if (!el) return;
    el.innerHTML = posts.length ? posts.map(postHTML).join("") : '<div class="empty">' + empty + "</div>";
  }
  function userRow(u) {
    return '<a class="row" href="#/u/' + esc(u.username) + '">' + avatar(u.avatarUrl, u.displayName || u.username) + '<span class="t"><b>' + esc(u.displayName || u.username) + (u.verified ? " " + VERIFIED : "") + "</b><small>@" + esc(u.username) + (u.bio ? " · " + esc(u.bio) : "") + "</small></span></a>";
  }
  function viewSearch(prefill) {
    setView(top("Buscar") + '<div style="padding:12px 16px"><input class="inp" id="q" placeholder="Pessoas, publicações e #assuntos" autocomplete="off" value="' + esc(prefill || "") + '"></div><div id="sr"></div>', "search");
    var inp = document.getElementById("q"), t;
    function run() {
      var q = inp.value.trim(), sr = document.getElementById("sr"); if (!sr) return;
      if (!q) { sr.innerHTML = RC.tags.length ? '<div class="sect">Em alta</div>' + RC.tags.map(function (g) { return '<a class="row" href="#/tag/' + encodeURIComponent(g.tag) + '"><span class="t"><b>#' + esc(g.tag) + "</b><small>" + g.posts + " publicações</small></span></a>"; }).join("") : ""; return; }
      sr.innerHTML = spin();
      api("/api/search?q=" + encodeURIComponent(q)).then(function (j) {
        var h = "";
        if ((j.users || []).length) h += '<div class="sect">Pessoas</div>' + j.users.map(userRow).join("");
        if ((j.tags || []).length) h += '<div class="sect">Assuntos</div>' + j.tags.map(function (g) { return '<a class="row" href="#/tag/' + encodeURIComponent(g.tag) + '"><span class="t"><b>#' + esc(g.tag) + "</b><small>" + g.posts + " publicações</small></span></a>"; }).join("");
        if ((j.posts || []).length) h += '<div class="sect">Publicações</div>' + j.posts.map(postHTML).join("");
        sr.innerHTML = h || '<div class="empty">Nada encontrado para “' + esc(q) + "”.</div>";
      }).catch(function (e) { sr.innerHTML = '<div class="empty">' + esc(e.message) + "</div>"; });
    }
    inp.addEventListener("input", function () { clearTimeout(t); t = setTimeout(run, 300); });
    run(); if (!prefill) inp.focus();
  }
  function viewTag(tag) {
    setView(top("#" + esc(tag), { back: true }) + '<div id="tp">' + spin() + "</div>", "search");
    api("/api/hashtags/" + encodeURIComponent(tag)).then(function (j) { postsInto("tp", j.posts || [], "Nenhuma publicação com esse assunto."); }).catch(function (e) { document.getElementById("tp").innerHTML = '<div class="empty">' + esc(e.message) + "</div>"; });
  }
  function viewSaved() {
    setView(top("Salvos") + '<div id="sv">' + spin() + "</div>", "saved");
    api("/api/me/saved").then(function (j) { postsInto("sv", j.posts || [], "Você ainda não salvou nada. Toque no marcador de uma publicação para guardar."); }).catch(function (e) { document.getElementById("sv").innerHTML = '<div class="empty">' + esc(e.message) + "</div>"; });
  }

  /* ---------- notificações ---------- */
  var NOTIF_TEXT = { verification_approved: "Sua conta foi verificada! Parabéns.", verification_rejected: "Seu pedido de verificação não foi aprovado.", follow: "começou a seguir você", follow_request: "pediu para seguir você", follow_accepted: "aceitou seu pedido de seguir", like: "curtiu sua publicação", comment: "comentou na sua publicação", mention: "mencionou você em uma publicação", mention_comment: "mencionou você em um comentário", tag: "marcou você em uma publicação", reply: "respondeu ao seu comentário", message: "enviou uma mensagem" };
  /* ---------- comunidades ---------- */
  var comTab = "mine";
  function comRow(c) {
    return '<div class="row" style="padding:0 16px 0 0"><a class="row" href="#/community/' + c.id + '"><span class="av" style="font-size:20px">' + esc(c.emoji) + '</span><span class="t"><b>' + esc(c.name) + "</b><small>" + c.members + " membro" + (c.members === 1 ? "" : "s") + (c.description ? " · " + esc(c.description) : "") + "</small></span></a>" +
      (c.isOwner ? "" : '<button class="sb ' + (c.joined ? "" : "pri") + '" style="flex:none;width:84px" data-joincom="' + c.id + '">' + (c.joined ? "Sair" : "Entrar") + "</button>") + "</div>";
  }
  function viewCommunities() {
    setView(top("Comunidades", { right: '<button class="ib" data-newcom aria-label="Criar comunidade">' + svg("plus") + "</button>" }) + '<div class="ftabs"><button data-ctab="mine" class="' + (comTab === "mine" ? "on" : "") + '">Minhas</button><button data-ctab="discover" class="' + (comTab === "discover" ? "on" : "") + '">Descobrir</button><button data-ctab="trending" class="' + (comTab === "trending" ? "on" : "") + '">Em alta</button></div><div id="cl">' + spin() + "</div>", "communities");
    api("/api/communities?filter=" + comTab).then(function (j) {
      var l = j.communities || [];
      document.getElementById("cl").innerHTML = l.length ? l.map(comRow).join("") : '<div class="empty">' + (comTab === "mine" ? "Você ainda não participa de nenhuma comunidade. Veja a aba Descobrir ou crie a sua." : "Nenhuma comunidade por aqui ainda.") + "</div>";
    }).catch(function (e) { document.getElementById("cl").innerHTML = '<div class="empty">' + esc(e.message) + "</div>"; });
  }
  function newCommunity() {
    var s = sheet("Criar comunidade", '<div class="form"><label class="fld">Ícone (um emoji)<input class="inp" id="ce" maxlength="4" value="👥"></label><label class="fld">Nome<input class="inp" id="cn" maxlength="60"></label><label class="fld">Descrição<textarea class="inp" id="cd" maxlength="500"></textarea></label><div class="err" id="cer"></div><button class="btn" id="cs">Criar</button></div>');
    s.querySelector("#cs").addEventListener("click", function () {
      var b = this; b.disabled = true;
      api("/api/communities", { method: "POST", body: { name: s.querySelector("#cn").value, description: s.querySelector("#cd").value, emoji: s.querySelector("#ce").value } })
        .then(function (j) { closeSheet(); location.hash = "#/community/" + j.community.id; })
        .catch(function (e) { s.querySelector("#cer").textContent = e.message; b.disabled = false; });
    });
  }
  function viewCommunity(id) {
    var me = cur(), last = "", COM = null, PM = [], ckey = "com:" + id;
    setView(top("Comunidade", { back: true, right: '<button class="ib" id="cmn2" aria-label="Opções">' + svg("dots") + "</button>" }) + '<div id="ch0">' + spin() + '</div><div class="chat" id="ch"></div><div class="composer" id="cc" hidden><form class="in" id="cf"><button type="button" class="ib" id="at" aria-label="Anexar">' + svg("plus") + '</button><button type="button" class="ib" id="em" aria-label="Emojis">😊</button><textarea id="mi" rows="1" maxlength="10000" placeholder="Escreva para a comunidade…"></textarea><button class="send" type="submit">Enviar</button></form><div id="emp" class="emp" hidden></div></div>', "communities", "noNav chatv");
    function load() {
      api("/api/communities/" + id).then(function (j) {
        var c = j.community, h = document.getElementById("ch0"); if (!h) return; COM = c;
        h.innerHTML = '<div class="ptop"><span class="av l" style="font-size:32px">' + esc(c.emoji) + '</span><div class="pname"><h2>' + esc(c.name) + "</h2><span class=\"at\">" + c.members + " membro" + (c.members === 1 ? "" : "s") + " · criada por @" + esc(c.ownerUsername) + "</span></div>" + (c.isOwner ? "" : '<button class="sb ' + (c.joined ? "" : "pri") + '" style="flex:none;width:84px" data-joincom="' + c.id + '" data-stay>' + (c.joined ? "Sair" : "Entrar") + "</button>") + "</div>" + (c.description ? '<div class="bio">' + linkify(c.description) + "</div>" : "");
        document.getElementById("cc").hidden = !c.joined;
        var hid = lget("hmsgs", []), cl = chatPref(ckey).cleared || "";
        PM = (j.posts || []).filter(function (m) { return hid.indexOf(m.id) < 0 && (!cl || m.createdAt > cl); }); j.posts = PM;
        var sig = (j.posts || []).map(function (m) { return m.id + (m.poll ? JSON.stringify(m.poll.votes) + m.poll.myVote : ""); }).join(); if (sig === last) return; last = sig;
        var ch = document.getElementById("ch"), stick = (window.innerHeight + window.scrollY) >= document.body.scrollHeight - 160 || !ch.dataset.done; ch.dataset.done = "1";
        ch.innerHTML = (j.posts || []).length ? j.posts.map(function (m) {
          var a = m.author || {};
          return '<div class="' + bubbleClass(m) + (m.mine ? " me" : "") + '" data-msg="' + m.id + '">' + (m.mine ? "" : "<b style='font-size:12px'>" + esc(a.username) + "</b><br>") + msgBody(m) + "<small>" + ago(m.createdAt) + "</small></div>";
        }).join("") : '<div class="empty">Ninguém escreveu ainda.</div>';
        hydrateShared(ch);
        if (stick) window.scrollTo(0, document.body.scrollHeight);
      }).catch(function (e) { var h = document.getElementById("ch0"); if (h) h.innerHTML = '<div class="empty">' + esc(e.message) + "</div>"; });
    }
    window.__reload = load; load(); timers.push(setInterval(load, 6000));
    function sendC(text) { return api("/api/communities/" + id + "/posts", { method: "POST", body: { body: text } }); }
    var cctx = {
      own: function (m) { return !!m.mine; }, react: false, forward: true, hide: true, reload: load, repaint: function () { last = ""; load(); },
      del: function (m) { return m.mine || (COM && COM.isOwner); },
      remove: function (m) { confirmSheet("Excluir mensagem?", "Ela some para todos da comunidade.", "Excluir", true, function () { api("/api/communities/" + id + "/posts/" + m.id, { method: "DELETE" }).then(function () { toast("Mensagem excluída."); load(); }).catch(onErr); }); }
    };
    document.getElementById("ch").addEventListener("click", function (e) {
      var b = e.target.closest("[data-msg]"); if (!b || e.target.closest("a,audio,video,button,.opt,.shp")) return;
      var m = PM.filter(function (x) { return x.id === b.dataset.msg; })[0]; if (m) msgSheet(m, cctx);
    });
    document.getElementById("cmn2").addEventListener("click", function () {
      var pf = chatPref(ckey), c = COM || {};
      function row(k, t, d) { return '<button class="row" data-cx="' + k + '"><span class="t"><b' + (d ? ' style="color:var(--danger)"' : "") + ">" + t + "</b></span></button>"; }
      var sh = sheet(c.name || "Comunidade", row("share", "Compartilhar comunidade") + row("mute", pf.mute ? "Reativar avisos" : "Silenciar avisos") + row("clear", "Limpar conversa", true) + (c.joined && !c.isOwner ? row("leave", "Sair da comunidade", true) : "") + (c.isOwner ? row("del", "Excluir comunidade", true) : ""));
      sh.addEventListener("click", function (e) {
        var b = e.target.closest("[data-cx]"); if (!b) return; var k = b.dataset.cx; closeSheet();
        if (k === "share") { var u = new URL("#/community/" + id, location.href).href; if (navigator.share) navigator.share({ title: c.name || "QINBLY", url: u }).catch(function () { }); else copyText(u, "Link copiado."); }
        else if (k === "mute") { setChatPref(ckey, { mute: !pf.mute }); toast(pf.mute ? "Avisos reativados." : "Avisos silenciados."); }
        else if (k === "clear") confirmSheet("Limpar conversa?", "As mensagens somem só para você, neste navegador.", "Limpar", true, function () { setChatPref(ckey, { cleared: new Date().toISOString() }); last = ""; load(); toast("Conversa limpa."); });
        else if (k === "leave") confirmSheet("Sair da comunidade?", "Você deixa de ver e enviar mensagens.", "Sair", true, function () { api("/api/communities/" + id + "/join", { method: "POST", body: {} }).then(function () { toast("Você saiu da comunidade."); location.hash = "#/communities"; }).catch(onErr); });
        else if (k === "del") confirmSheet("Excluir comunidade?", "Todas as mensagens e membros serão removidos. Não dá para desfazer.", "Excluir", true, function () { api("/api/communities/" + id, { method: "DELETE" }).then(function () { toast("Comunidade excluída."); location.hash = "#/communities"; }).catch(onErr); });
      });
    });
    document.getElementById("at").addEventListener("click", function () { attachMenu(sendC, load); });
    document.getElementById("em").addEventListener("click", function () { emojiPanel(document.getElementById("emp"), document.getElementById("mi")); });
    document.getElementById("cf").addEventListener("submit", function (e) {
      e.preventDefault();
      var inp = document.getElementById("mi"), t = inp.value.trim(); if (!t) return; inp.value = "";
      sendC(t).then(load).catch(function (x) { inp.value = t; onErr(x); });
    });
  }

  /* ---------- mercado ---------- */
    var MK_LABEL = { venda: "Venda", doacao: "Doação", achado: "Achado", perdido: "Perdido", evento: "Evento", servico: "Serviço", aviso: "Aviso" };
  var mkCat = 0, mkCity = LS.get("qb_city", "");
  var OLD_PUB = "https://jqomgrdlocdqaarfqkay.supabase.co/storage/v1/object/public/post-media/";
  function photoUrl(p) { if (!p) return ""; if (p.indexOf(OLD_PUB) === 0) p = p.slice(OLD_PUB.length); return /^https?:/.test(p) ? p : HOST + "/storage/v1/object/public/post-media/" + p; }
  var MK = {};
  /* ---------- mais, criar story ---------- */
  /* Menu dos três tracinhos: tela cheia "Atalhos", igual ao app */
  function closeShortcuts() { var o = document.getElementById("scr"); if (o) o.remove(); document.removeEventListener("keydown", scrEsc); }
  function scrEsc(e) { if (e.key === "Escape") closeShortcuts(); }
  function moreMenu() {
    closeShortcuts();
    var me = cur();
    function item(ic, act, title, sub) { return '<button class="sci" data-sc="' + act + '">' + svg(ic) + '<span class="t"><b>' + title + "</b>" + (sub ? "<small>" + sub + "</small>" : "") + "</span></button>"; }
    function lab(t) { return '<div class="scl">' + t + "</div>"; }
    var o = document.createElement("div"); o.id = "scr"; o.className = "scr";
    o.innerHTML = '<div class="scw"><div class="sch"><button class="ib" data-sc="close" aria-label="Voltar">' + svg("back") + "</button><h2>Atalhos</h2></div>" +
      (me ? '<button class="scp" data-sc="profile">' + avatar(me.avatarUrl, me.displayName || me.username) + '<span class="t"><b>' + esc(me.username) + "</b><small>Ver meu perfil</small></span></button>" : "") +
      lab("EXPLORAR") + item("play", "tab:Vídeos", "Vídeos", "Curtos e longos") + item("flame", "tab:Em alta", "Em alta", "O que está bombando") + item("sensors", "tab:Lives", "Ao vivo", "Transmissões ao vivo") + item("pin", "tab:Perto de mim", "Perto de mim", "Gente da sua cidade") +
      lab("NA SUA CIDADE") + item("bag", "mk:1", "Mercado", "Compre e venda perto de você") + item("gift", "mk:2", "Doações", "Dê e ganhe coisas") + item("search", "mk:3", "Achados", "Achei algo, perdi algo") + item("cal", "mk:4", "Eventos", "O que vai rolar na cidade") + item("tool", "mk:5", "Serviços", "Quem faz o quê") + item("mega", "mk:6", "Avisos", "Recados do bairro") +
      lab("SEUS ESPAÇOS") + item("groups", "go:#/communities", "Comunidades", "Seus grupos") + item("save", "go:#/saved", "Salvos", "Publicações que você guardou") + item("adduser", "invite", "Convidar amigos", "Compartilhe o QINBLY") + item("sensors", "go:#/golive", "Transmitir ao vivo", "Comece uma live agora") +
      '<div class="scg"></div>' + item("gear", "go:#/settings", "Configurações", "Conta, privacidade, tela e mais") + item("groups", "accounts", "Trocar de conta", me ? "@" + esc(me.username) : "") + item("download", "install", "Instalar o app", "Instala direto no celular") +
      '<div class="scf"><a href="privacidade.html">Privacidade</a><a href="termos.html">Termos de uso</a></div>' +
      '<button class="sci out" data-sc="drop">' + svg("x") + '<span class="t"><b>Sair desta conta</b></span></button></div>';
    document.body.appendChild(o);
    document.addEventListener("keydown", scrEsc);
    o.addEventListener("click", function (e) {
      var t = e.target.closest("[data-sc]"); if (!t) return; var k = t.dataset.sc;
      closeShortcuts();
      if (k === "close") return;
      if (k === "profile") { location.hash = "#/me"; return; }
      if (k === "accounts") return accountsSheet();
      if (k === "install") return installNow();
      if (k === "drop") return dropAccount(activeId);
      if (k === "invite") return inviteFriends();
      if (k.indexOf("go:") === 0) { location.hash = k.slice(3); return; }
      if (k.indexOf("mk:") === 0) { mkCat = Number(k.slice(3)); if (location.hash === "#/market") viewMarket(); else location.hash = "#/market"; return; }
      if (k.indexOf("tab:") === 0) { feedTab = k.slice(4); if (location.hash === "#/" || location.hash === "" || location.hash === "#") render(); else location.hash = "#/"; return; }
    });
  }
  /* =====================================================================
     Recursos completos: menu do post, mensagens com mídia, enquetes,
     mercado, configurações, lives, destaques e notas
     ===================================================================== */
  function fld(label, inner) { return '<label class="fld">' + label + inner + "</label>"; }
  function err(id) { return '<div class="err" id="' + id + '"></div>'; }
  function confirmSheet(title, text, okLabel, danger, fn) {
    var s = sheet(title, '<div style="padding:0 20px 8px;font-size:16px">' + esc(text) + '</div><div class="form"><button class="btn" id="cok" style="' + (danger ? "background:var(--danger)" : "") + '">' + esc(okLabel) + '</button><button class="btn ghost" id="cno">Cancelar</button></div>');
    s.querySelector("#cno").addEventListener("click", closeSheet);
    s.querySelector("#cok").addEventListener("click", function () { closeSheet(); fn(); });
  }
  function hiddenWords() { return LS.get("qb_hidden", []); }
  function isHidden(p) {
    var w = hiddenWords(); if (!w.length) return false;
    var t = String(p.caption || "").toLowerCase();
    return w.some(function (x) { return x && t.indexOf(x) >= 0; });
  }
  function followUser(id, name) {
    call(HOST + "/rest/v1/rpc/request_follow", { method: "POST", body: { target: id } }).catch(function () { return "open"; }).then(function (r) {
      r = String(r || "").replace(/"/g, "");
      if (r === "requested") { toast("Pedido enviado. Você passa a seguir quando a pessoa aprovar."); if (name) viewProfile(name); return; }
      if (r === "cancelled") { toast("Pedido de seguir cancelado."); if (name) viewProfile(name); return; }
      toggleFollow(id, name);
    });
  }

  /* ---------- menu do post ---------- */
  function postMenu(id) {
    var p = POSTS[id]; if (!p) return; var me = cur(), own = me && p.authorId === me.id;
    var url = new URL("#/post/" + id, location.href).href;
    var rows = '<button class="row" data-pact="share:' + id + '"><span class="t"><b>Compartilhar</b></span></button>';
    if (own) {
      rows = '<button class="row" data-pact="edit:' + id + '"><span class="t"><b>Editar legenda</b></span></button>' +
        '<button class="row" data-pact="pin:' + id + '"><span class="t"><b>' + (p.pinned ? "Desafixar do perfil" : "Fixar no perfil") + "</b></span></button>" +
        '<button class="row" data-pact="vis:' + id + '"><span class="t"><b>' + (p.visibility === "private" ? "Tornar público" : "Tornar privado (só eu)") + "</b></span></button>" +
        '<button class="row" data-pact="archive:' + id + '"><span class="t"><b>Arquivar</b><small>Some do perfil e do feed. Veja em Configurações, Conteúdo.</small></span></button>' +
        '<button class="row" data-pact="stats:' + id + '"><span class="t"><b>Estatísticas</b></span></button>' + rows +
        '<button class="row" data-pact="del:' + id + '"><span class="t"><b style="color:var(--danger)">Excluir publicação</b></span></button>';
    } else {
      rows = '<button class="row" data-pact="hide:' + id + '"><span class="t"><b>Ocultar publicação</b></span></button><button class="row" data-pact="copy:' + id + '"><span class="t"><b>Copiar link</b></span></button>' + '<button class="row" data-pact="repost:' + id + '"><span class="t"><b>' + (p.repostedByMe ? "Desfazer repostagem" : "Repostar") + "</b></span></button>" + rows +
        '<button class="row" data-pact="report:' + id + '"><span class="t"><b>Denunciar</b></span></button>' +
        '<button class="row" data-pact="mute:' + id + '"><span class="t"><b>Silenciar @' + esc((p.author || {}).username) + "</b></span></button>" +
        '<button class="row" data-pact="block:' + id + '"><span class="t"><b style="color:var(--danger)">Bloquear @' + esc((p.author || {}).username) + "</b></span></button>";
    }
    sheet("Publicação", rows);
  }
  function reportSheet(type, targetId) {
    var reasons = [["spam", "É spam"], ["harassment", "Assédio ou bullying"], ["violence", "Violência"], ["nudity", "Nudez ou conteúdo sexual"], ["hate", "Discurso de ódio"], ["other", "Outro motivo"]];
    var s = sheet("Por que você está denunciando?", reasons.map(function (r) { return '<button class="row" data-rep="' + r[0] + '"><span class="t"><b>' + r[1] + "</b></span></button>"; }).join(""));
    s.addEventListener("click", function (e) {
      var b = e.target.closest("[data-rep]"); if (!b) return;
      api("/api/reports", { method: "POST", body: { targetType: type, targetId: targetId, reason: b.dataset.rep } }).then(function () { closeSheet(); toast("Denúncia enviada. Obrigado por avisar."); }).catch(onErr);
    });
  }
  function postAction(act, id) {
    var p = POSTS[id]; closeSheet();
    if (act === "share") return shareSheet(id);
    if (act === "share_old") { var url = new URL("#/post/" + id, location.href).href; if (navigator.share) navigator.share({ title: "QINBLY", url: url }).catch(function () { }); else if (navigator.clipboard) navigator.clipboard.writeText(url).then(function () { toast("Link copiado."); }); return; }
    if (act === "copy") return copyText(postLink(id), "Link copiado.");
    if (act === "hide") { var hp2 = lget("hposts", []); hp2.push(id); lset("hposts", hp2); FEED = FEED.filter(function (x) { return x.id !== id; }); var pe = document.querySelector('[data-post="' + id + '"]'); if (pe) pe.remove(); return toast("Publicação ocultada deste feed."); }
    if (act === "repost") return api("/api/posts/" + id + "/repost", { method: "POST", body: {} }).then(function (r) { p.repostedByMe = !!r.reposted; toast(r.reposted ? "Repostado." : "Repostagem desfeita."); }).catch(onErr);
    if (act === "report") return reportSheet("post", id);
    if (act === "mute") return api("/api/users/" + p.authorId + "/mute", { method: "POST", body: {} }).then(function (r) { if (r.muted) { FEED = FEED.filter(function (x) { return x.authorId !== p.authorId; }); render(); } toast(r.muted ? "Você não verá mais as publicações dessa conta." : "Conta reativada."); }).catch(onErr);
    if (act === "block") return confirmSheet("Bloquear @" + (p.author || {}).username + "?", "Vocês deixam de ver as publicações um do outro.", "Bloquear", true, function () {
      api("/api/users/" + p.authorId + "/block", { method: "POST", body: {} }).then(function () { FEED = FEED.filter(function (x) { return x.authorId !== p.authorId; }); toast("Conta bloqueada."); render(); }).catch(onErr);
    });
    if (act === "pin") return api("/api/posts/" + id + "/pin", { method: "POST", body: {} }).then(function (r) { p.pinned = !!r.pinned; toast(r.pinned ? "Fixada no perfil." : "Desafixada."); }).catch(onErr);
    if (act === "archive") return api("/api/posts/" + id + "/archive", { method: "POST", body: {} }).then(function () { FEED = FEED.filter(function (x) { return x.id !== id; }); toast("Arquivada."); var el = document.querySelector('[data-post="' + id + '"]'); if (el) el.remove(); }).catch(onErr);
    if (act === "vis") return api("/api/posts/" + id, { method: "PATCH", body: { visibility: p.visibility === "private" ? "public" : "private" } }).then(function (j) { POSTS[id] = j.post; repaint(id); toast("Visibilidade alterada."); }).catch(onErr);
    if (act === "del") return confirmSheet("Excluir publicação?", "Isso não pode ser desfeito.", "Excluir", true, function () {
      api("/api/posts/" + id, { method: "DELETE" }).then(function () { FEED = FEED.filter(function (x) { return x.id !== id; }); toast("Publicação excluída."); var hp3 = lget("hposts", []); hp3.push(id); lset("hposts", hp3); delete POSTS[id]; var el = document.querySelector('[data-post="' + id + '"]'); if (el) el.remove(); else history.back(); }).catch(onErr);
    });
    if (act === "edit") {
      var s = sheet("Editar legenda", '<div class="form"><textarea class="inp" id="ec" maxlength="5000"></textarea>' + err("eer") + '<button class="btn" id="es">Salvar</button></div>');
      s.querySelector("#ec").value = p.caption || "";
      s.querySelector("#es").addEventListener("click", function () {
        api("/api/posts/" + id, { method: "PATCH", body: { caption: s.querySelector("#ec").value } }).then(function (j) { POSTS[id] = j.post; closeSheet(); repaint(id); toast("Legenda salva."); }).catch(function (e) { s.querySelector("#eer").textContent = e.message; });
      });
      return;
    }
    if (act === "stats") return api("/api/posts/" + id + "/stats").then(function (j) {
      sheet("Estatísticas", '<div class="stats" style="padding:16px"><div><b>' + j.views + "</b>Visualizações</div><div><b>" + j.viewers + "</b>Pessoas</div><div><b>" + j.likes + "</b>Curtidas</div></div><div class=\"stats\" style=\"padding:0 16px 20px\"><div><b>" + j.comments + "</b>Comentários</div><div><b>" + j.reposts + "</b>Reposts</div></div>");
    }).catch(onErr);
  }

  /* ---------- mensagens: foto, documento, áudio, figurinha, enquete ---------- */
  var DOC_OK = ["application/pdf", "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "application/vnd.ms-powerpoint", "application/vnd.openxmlformats-officedocument.presentationml.presentation", "text/plain", "text/csv", "application/zip", "application/x-zip-compressed"];
  function pollHTML(p) {
    var tot = (p.votes || []).reduce(function (a, b) { return a + b; }, 0);
    return '<div class="cpoll"><b>' + esc(p.question) + "</b>" + (p.options || []).map(function (o, i) {
      var pc = tot ? Math.round((p.votes[i] || 0) * 100 / tot) : 0;
      return '<button class="opt ' + (p.myVote === i ? "mine" : "") + '" data-cvote="' + p.id + ":" + i + '"><i style="width:' + (p.myVote >= 0 ? pc : 0) + '%"></i><span>' + esc(o) + "</span><span>" + (p.myVote >= 0 ? pc + "%" : "") + "</span></button>";
    }).join("") + "</div>";
  }
  function msgBody(m) {
    if (m.imageUrl) return '<a href="' + safeUrl(m.imageUrl) + '" target="_blank" rel="noopener"><img class="mimg" src="' + safeUrl(m.imageUrl) + '" loading="lazy" alt="Foto"></a>';
    if (m.stickerUrl) return '<img class="mstk" src="' + safeUrl(m.stickerUrl) + '" alt="Figurinha">';
    if (m.audioUrl) return '<audio controls preload="none" src="' + safeUrl(m.audioUrl) + '"></audio>';
    if (m.fileUrl) return '<a class="mfile" href="' + safeUrl(m.fileUrl) + '" target="_blank" rel="noopener">📄 ' + esc(m.fileName || "Documento") + "</a>";
    if (m.poll) return pollHTML(m.poll);
    if (typeof m.body === "string" && m.body && !/^qinbly:/.test(m.body)) return linkify(m.body);
    return m.body ? "<i>Mensagem especial. Abra no app para ver.</i>" : "";
  }
  function bubbleClass(m) { return m.imageUrl || m.stickerUrl ? "msg media" : "msg"; }
  function attachMenu(send, done) {
    var s = sheet("Enviar", ["photo:Foto", "doc:Documento", "audio:Áudio (gravar)", "camera:Câmera", "loc:Localização", "sticker:Figurinha", "poll:Enquete"].map(function (x) { var a = x.split(":"); return '<button class="row" data-att="' + a[0] + '"><span class="t"><b>' + a[1] + "</b></span></button>"; }).join(""));
    function pick(accept, cb) { var f = document.createElement("input"); f.type = "file"; f.accept = accept; f.onchange = function () { if (f.files[0]) cb(f.files[0]); }; f.click(); }
    function go(p) { closeSheet(); toast("Enviando…"); p.then(function () { done && done(); }).catch(onErr); }
    s.addEventListener("click", function (e) {
      var b = e.target.closest("[data-att]"); if (!b) return; var k = b.dataset.att;
      if (k === "photo") pick("image/*", function (f) { if (IMG_OK.indexOf(f.type) < 0) return toast("Use JPG, PNG, WebP ou GIF."); go(shrink(f).then(function (x) { return upload(x, "message_media"); }).then(function (path) { return send("qinbly:img:" + path); })); });
      else if (k === "doc") pick(".pdf,.doc,.docx,.xls,.xlsx,.ppt,.pptx,.txt,.csv,.zip", function (f) { if (DOC_OK.indexOf(f.type) < 0) return toast("Tipo de documento não aceito."); go(upload(f, "message_file").then(function (path) { return send("qinbly:file:" + path + ":" + encodeURIComponent(f.name)); })); });
      else if (k === "camera") { var cf = document.createElement("input"); cf.type = "file"; cf.accept = "image/*"; cf.setAttribute("capture", "environment"); cf.onchange = function () { var f = cf.files[0]; if (!f) return; if (IMG_OK.indexOf(f.type) < 0) return toast("Use JPG, PNG, WebP ou GIF."); go(shrink(f).then(function (x) { return upload(x, "message_media"); }).then(function (path) { return send("qinbly:img:" + path); })); }; cf.click(); }
      else if (k === "loc") sendLocation(send, done);
      else if (k === "audio") recordSheet(send, done);
      else if (k === "sticker") stickerSheet(send, done);
      else if (k === "poll") pollSheet(send, done);
    });
  }
  function recordSheet(send, done) {
    if (!navigator.mediaDevices || !window.MediaRecorder) return toast("Seu navegador não grava áudio.");
    var s = sheet("Gravar áudio", '<div class="form" style="text-align:center"><div id="rt" style="font-size:24px">0:00</div><button class="btn" id="rs">Começar a gravar</button><button class="btn ghost" id="rc">Cancelar</button></div>');
    var rec, chunks = [], t0 = 0, tick, stream, cancelled = false;
    function stop() { clearInterval(tick); try { rec && rec.state !== "inactive" && rec.stop(); } catch (e) { } if (stream) stream.getTracks().forEach(function (t) { t.stop(); }); }
    s.addEventListener("click", function (e) { if (e.target === s) stop(); });
    s.querySelector("#rc").addEventListener("click", function () { cancelled = true; stop(); closeSheet(); });
    s.querySelector("#rs").addEventListener("click", function () {
      var btn = this;
      if (rec && rec.state === "recording") { stop(); return; }
      navigator.mediaDevices.getUserMedia({ audio: true }).then(function (st) {
        stream = st; var mime = MediaRecorder.isTypeSupported("audio/mp4") ? "audio/mp4" : "audio/webm";
        rec = new MediaRecorder(st); chunks = [];
        rec.ondataavailable = function (ev) { if (ev.data.size) chunks.push(ev.data); };
        rec.onstop = function () {
          if (cancelled) return; var secs = Math.max(1, Math.round((Date.now() - t0) / 1000));
          var f = new File([new Blob(chunks, { type: mime })], "voz." + (mime === "audio/mp4" ? "m4a" : "webm"), { type: mime });
          closeSheet(); toast("Enviando áudio…");
          upload(f, "message_audio").then(function (path) { return send("qinbly:audio:" + path + ":" + secs); }).then(function () { done && done(); }).catch(onErr);
        };
        t0 = Date.now(); rec.start(); btn.textContent = "Parar e enviar";
        tick = setInterval(function () { var n = Math.round((Date.now() - t0) / 1000); var el = document.getElementById("rt"); if (el) el.textContent = Math.floor(n / 60) + ":" + String(n % 60).padStart(2, "0"); if (n >= 300) stop(); }, 500);
      }).catch(function () { toast("Permita o microfone para gravar."); });
    });
  }
  function stickerSheet(send, done) {
    var s = sheet("Figurinhas", '<div class="stk" id="sg">' + spin() + '</div><div class="form"><button class="btn ghost" id="sn">Criar nova figurinha (PNG, WebP ou GIF)</button></div>');
    function load() {
      api("/api/stickers").then(function (j) {
        var g = s.querySelector("#sg"); if (!g) return;
        g.innerHTML = (j.stickers || []).length ? j.stickers.map(function (x) { return '<button data-stk="' + esc(x.path) + '"><img src="' + safeUrl(x.url) + '" alt=""></button>'; }).join("") : '<div class="empty">Você ainda não tem figurinhas.</div>';
      }).catch(onErr);
    }
    load();
    s.addEventListener("click", function (e) {
      var b = e.target.closest("[data-stk]");
      if (b) { closeSheet(); send("qinbly:sticker:" + b.dataset.stk).then(function () { done && done(); }).catch(onErr); }
    });
    s.querySelector("#sn").addEventListener("click", function () {
      var f = document.createElement("input"); f.type = "file"; f.accept = "image/png,image/webp,image/gif";
      f.onchange = function () {
        var file = f.files[0]; if (!file) return; if (file.size > 2 * 1024 * 1024) return toast("A figurinha passa de 2 MB.");
        upload(file, "sticker").then(function (path) { return api("/api/stickers", { method: "POST", body: { fileId: path } }); }).then(load).catch(onErr);
      }; f.click();
    });
  }
  function pollSheet(send, done) {
    var s = sheet("Nova enquete", '<div class="form"><label class="fld">Pergunta<input class="inp" id="pq" maxlength="300"></label>' + [1, 2, 3, 4].map(function (i) { return '<label class="fld">Opção ' + i + (i > 2 ? " (opcional)" : "") + '<input class="inp po" maxlength="80"></label>'; }).join("") + err("per") + '<button class="btn" id="ps">Enviar enquete</button></div>');
    s.querySelector("#ps").addEventListener("click", function () {
      var opts = Array.prototype.map.call(s.querySelectorAll(".po"), function (i) { return i.value.trim(); }).filter(Boolean);
      api("/api/chat-polls", { method: "POST", body: { question: s.querySelector("#pq").value, options: opts } }).then(function (j) { closeSheet(); return send("qinbly:poll:" + j.id).then(function () { done && done(); }); })
        .catch(function (e) { var el = s.querySelector("#per"); if (el) el.textContent = e.message; });
    });
  }

  /* ---------- notas e destaques ---------- */
  function noteSheet() {
    var s = sheet("Sua nota", '<div class="form"><input class="inp" id="nb" maxlength="120" placeholder="Compartilhe um pensamento (dura 24 h)">' + err("ner") + '<button class="btn" id="ns">Publicar nota</button><button class="btn ghost" id="nd">Apagar nota atual</button></div>');
    s.querySelector("#ns").addEventListener("click", function () { api("/api/notes", { method: "POST", body: { body: s.querySelector("#nb").value } }).then(function () { closeSheet(); toast("Nota publicada."); loadStories(); }).catch(function (e) { s.querySelector("#ner").textContent = e.message; }); });
    s.querySelector("#nd").addEventListener("click", function () { api("/api/notes", { method: "DELETE" }).then(function () { closeSheet(); toast("Nota apagada."); loadStories(); }).catch(onErr); });
  }
  var STORY_BGS = [["#7C4DFF", "#2F68E0"], ["#E0457B", "#8A2A5A"], ["#1D9E75", "#0F6E56"], ["#EF9F27", "#B45309"], ["#378ADD", "#185FA5"], ["#111111", "#2B2B2B"], ["#D85A30", "#993C1D"], ["#639922", "#3B6D11"]];
  function storyBg(n) { var c = STORY_BGS[(Number(n) || 0) % STORY_BGS.length]; return "linear-gradient(135deg," + c[0] + "," + c[1] + ")"; }
  function storyMenu() {
    var s = sheet("Criar", '<button class="row" data-sm="media"><span class="t"><b>Foto ou vídeo</b><small>Story de 24 horas</small></span></button><button class="row" data-sm="text"><span class="t"><b>Texto</b><small>Story só com palavras e cor</small></span></button><button class="row" data-sm="note"><span class="t"><b>Nota</b><small>Frase curta ao lado da sua foto</small></span></button>');
    s.addEventListener("click", function (e) {
      var b = e.target.closest("[data-sm]"); if (!b) return; closeSheet();
      if (b.dataset.sm === "media") addStory(); else if (b.dataset.sm === "note") noteSheet(); else textStory();
    });
  }
  function textStory() {
    var bg = 0;
    var s = sheet("Story de texto", '<div class="form"><div id="tsp" class="tbg" style="min-height:200px;border-radius:12px;background:' + storyBg(0) + '"><textarea id="tst" maxlength="300" placeholder="Escreva aqui" style="background:none;border:0;color:#fff;font:inherit;text-align:center;width:100%;resize:none;outline:none;min-height:120px"></textarea></div><div class="bgs" id="tsb">' + STORY_BGS.map(function (_, i) { return '<button data-b="' + i + '" class="' + (i === 0 ? "on" : "") + '" style="background:' + storyBg(i) + '"></button>'; }).join("") + "</div>" + '<label class="fld">Quem pode ver<select class="inp" id="tsa">' + AUDIENCES.map(function (a) { return '<option value="' + a[0] + '">' + a[1] + "</option>"; }).join("") + "</select></label>" + err("tse") + '<button class="btn" id="tss">Publicar story</button></div>');
    s.querySelector("#tsb").addEventListener("click", function (e) { var b = e.target.closest("[data-b]"); if (!b) return; bg = Number(b.dataset.b); s.querySelector("#tsp").style.background = storyBg(bg); s.querySelectorAll("#tsb button").forEach(function (x) { x.classList.toggle("on", x === b); }); });
    s.querySelector("#tss").addEventListener("click", function () {
      var text = s.querySelector("#tst").value.trim(); if (!text) { s.querySelector("#tse").textContent = "Escreva algo para publicar."; return; }
      call(HOST + "/rest/v1/stories", { method: "POST", body: { user_id: cur().id, media_path: "text", media_type: "text", caption: "qinbly:text:" + bg + ":3:1:0:0\n" + text, audience: s.querySelector("#tsa").value, city: s.querySelector("#tsa").value === "near" ? myCity() : "", expires_at: new Date(Date.now() + 86400000).toISOString() } })
        .then(function () { closeSheet(); toast("Story publicado por 24 horas."); loadStories(); }).catch(function (e) { s.querySelector("#tse").textContent = e.message; });
    });
  }

  /* ---------- mercado: anunciar, meus anúncios, salvos ---------- */
  var MK_KINDS = [["venda", "Venda"], ["doacao", "Doação"], ["achado", "Achado"], ["perdido", "Perdido"], ["evento", "Evento"], ["servico", "Serviço"], ["aviso", "Aviso"]];
  function newListing() {
    var photos = [];
    var s = sheet("Anunciar", '<div class="form"><label class="fld">Tipo<select class="inp" id="lk">' + MK_KINDS.map(function (k) { return '<option value="' + k[0] + '">' + k[1] + "</option>"; }).join("") + '</select></label><label class="fld">Título<input class="inp" id="lt" maxlength="120"></label><label class="fld">Descrição<textarea class="inp" id="ld" maxlength="2000"></textarea></label><label class="fld" id="lpw">Preço (R$)<input class="inp" id="lp" inputmode="decimal" placeholder="0,00"></label><label class="fld">Data, local ou detalhe (opcional)<input class="inp" id="le" maxlength="80"></label><label class="fld">Cidade<input class="inp" id="lc" value="' + esc(mkCity) + '"></label><button class="btn ghost" id="lph">Adicionar fotos (até 5)</button><div id="lpv" class="pick"></div>' + err("ler") + '<button class="btn" id="lgo">Publicar anúncio</button></div>');
    function price() { var k = s.querySelector("#lk").value; s.querySelector("#lpw").style.display = (k === "venda" || k === "servico") ? "" : "none"; }
    s.querySelector("#lk").addEventListener("change", price); price();
    s.querySelector("#lph").addEventListener("click", function () {
      var f = document.createElement("input"); f.type = "file"; f.accept = "image/*"; f.multiple = true;
      f.onchange = function () { photos = Array.prototype.slice.call(f.files).filter(function (x) { return ["image/jpeg", "image/png", "image/webp"].indexOf(x.type) >= 0; }).slice(0, 5); s.querySelector("#lpv").innerHTML = photos.map(function (x) { return '<div class="th" style="background-image:url(' + URL.createObjectURL(x) + ')"></div>'; }).join(""); };
      f.click();
    });
    s.querySelector("#lgo").addEventListener("click", function () {
      var btn = this, e = s.querySelector("#ler"), kind = s.querySelector("#lk").value, title = s.querySelector("#lt").value.trim(), city = s.querySelector("#lc").value.trim(), pr = parseFloat(String(s.querySelector("#lp").value).replace(",", "."));
      if (!title) { e.textContent = "Escreva um título."; return; }
      if (!city) { e.textContent = "Informe a cidade."; return; }
      if (kind === "venda" && !(pr >= 0)) { e.textContent = "Informe o preço."; return; }
      if ((kind === "venda" || kind === "doacao") && !photos.length) { e.textContent = "Adicione pelo menos uma foto."; return; }
      btn.disabled = true; btn.textContent = "Publicando…"; var uid = cur().id;
      Promise.all(photos.map(function (f, i) {
        return shrink(f).then(function (b) {
          var path = uid + "/market/" + (crypto.randomUUID ? crypto.randomUUID() : Date.now() + "" + i) + "-" + i + ".jpg";
          return fetch(HOST + "/storage/v1/object/post-media/" + path, { method: "PUT", headers: { Authorization: "Bearer " + cur().token, "Content-Type": b.type || "image/jpeg" }, body: b }).then(function (r) { if (!r.ok) throw new Error("Não foi possível enviar a foto."); return path; });
        });
      })).then(function (paths) {
        var body = { user_id: uid, kind: kind, title: title.slice(0, 120), description: s.querySelector("#ld").value.trim().slice(0, 2000), extra: s.querySelector("#le").value.trim().slice(0, 80), photos: paths, city: city };
        if ((kind === "venda" || kind === "servico") && pr >= 0) body.price = pr;
        return call(HOST + "/rest/v1/marketplace_listings", { method: "POST", body: body });
      }).then(function () { mkCity = city; LS.set("qb_city", city); closeSheet(); toast("Anúncio publicado. Fica no ar por 7 dias."); viewMarket(); })
        .catch(function (x) { e.textContent = x.message; btn.disabled = false; btn.textContent = "Publicar anúncio"; });
    });
  }

  /* ---------- lives ---------- */
  var liveRoom = null, liveStop = null;
  var LK_SRC = "https://cdn.jsdelivr.net/npm/livekit-client@2.5.7/dist/livekit-client.umd.min.js";
  function loadLK() {
    if (window.LivekitClient) return Promise.resolve();
    return new Promise(function (ok, bad) { var s = document.createElement("script"); s.src = LK_SRC; s.onload = ok; s.onerror = function () { bad(new Error("Não foi possível carregar o player de lives.")); }; document.head.appendChild(s); });
  }
  function leaveLive() { if (liveStop) { try { liveStop(); } catch (e) { } liveStop = null; } if (liveRoom) { try { liveRoom.disconnect(); } catch (e) { } liveRoom = null; } }
  function liveList() {
    var el = document.getElementById("fd"); if (!el) return;
    el.innerHTML = spin();
    call(HOST + "/api/live?action=list").then(function (j) {
      var l = j.lives || [];
      l.sort(function (a, b) { return b.viewers - a.viewers; });
      el.innerHTML = '<div style="padding:12px 16px"><button class="btn" data-golive>Transmitir ao vivo</button></div>' + (l.length ? l.map(function (x) { return '<a class="row" href="#/live/' + esc(x.id) + "/" + encodeURIComponent(x.host) + '"><span class="av" style="background:var(--danger)">●</span><span class="t"><b>@' + esc(x.host) + '</b><small>Ao vivo · ' + x.viewers + " assistindo</small></span><span class=\"sb pri\" style=\"flex:none;width:72px;line-height:30px;text-align:center\">Assistir</span></a>"; }).join("") : '<div class="empty">Ninguém está ao vivo agora.</div>');
    }).catch(function (e) { el.innerHTML = '<div class="empty">' + esc(e.message) + "</div>"; });
  }
  function liveChat(host, box, getLast, setLast) {
    function load() {
      var a = getLast();
      api("/api/lives/" + encodeURIComponent(host) + "/comments" + (a ? "?after=" + encodeURIComponent(a) : "")).then(function (j) {
        var l = j.comments || []; if (!l.length) return; setLast(l[l.length - 1].createdAt);
        l.forEach(function (c) { var d = document.createElement("div"); d.className = "lc"; d.innerHTML = "<b>" + esc(c.username) + "</b> " + esc(c.body); box.appendChild(d); });
        box.scrollTop = box.scrollHeight;
      }).catch(function () { });
    }
    load(); return setInterval(load, 3000);
  }
  function viewLiveWatch(roomId, host) {
    var last = "", likes = 0, ivs = [];
    setView(top("@" + esc(host), { back: true, right: '<span class="live-badge">AO VIVO</span>' }) + '<div id="lv" class="livebox"><div class="livemsg">Conectando…</div></div><div class="liveinfo"><span id="lvn">0 assistindo</span><span id="lvl">♥ 0</span><button class="sb" style="flex:none;width:90px" id="lvh">♥ Curtir</button></div><div id="lcm" class="lcm"></div><form class="cin" id="lf2"><input id="lci" maxlength="300" placeholder="Comente na live…" autocomplete="off"><button type="submit">Enviar</button></form>', "", "noNav chatv");
    var box = document.getElementById("lv");
    loadLK().then(function () { return call(HOST + "/api/live?action=token", { method: "POST", body: { username: cur().username, host: false, room: roomId } }); }).then(function (t) {
      var LK = window.LivekitClient, room = new LK.Room({ adaptiveStream: true }); liveRoom = room;
      room.on(LK.RoomEvent.TrackSubscribed, function (track) { if (track.kind === "video") { box.innerHTML = ""; var v = track.attach(); v.setAttribute("playsinline", ""); box.appendChild(v); } else { var a = track.attach(); a.style.display = "none"; document.body.appendChild(a); } });
      room.on(LK.RoomEvent.TrackUnsubscribed, function (track) { track.detach().forEach(function (e) { e.remove(); }); });
      function count() { var n = document.getElementById("lvn"); if (n) n.textContent = Math.max(0, room.remoteParticipants.size - 1) + " assistindo"; }
      room.on(LK.RoomEvent.ParticipantConnected, count); room.on(LK.RoomEvent.ParticipantDisconnected, count);
      room.on(LK.RoomEvent.DataReceived, function (payload) { try { var m = JSON.parse(new TextDecoder().decode(payload)); if (m.t === "total") { likes = m.n; var l = document.getElementById("lvl"); if (l) l.textContent = "♥ " + likes; } } catch (e) { } });
      room.on(LK.RoomEvent.Disconnected, function () { var b = document.getElementById("lv"); if (b && liveRoom) b.innerHTML = '<div class="livemsg">A live terminou.</div>'; });
      return room.connect(t.url, t.token).then(count);
    }).catch(function (e) { box.innerHTML = '<div class="livemsg">' + esc(e.message || "Não foi possível entrar na live.") + "</div>"; });
    ivs.push(liveChat(host, document.getElementById("lcm"), function () { return last; }, function (v) { last = v; }));
    liveStop = function () { ivs.forEach(clearInterval); };
    document.getElementById("lvh").addEventListener("click", function () { if (liveRoom) try { liveRoom.localParticipant.publishData(new TextEncoder().encode(JSON.stringify({ t: "like", n: 1 })), { reliable: true }); } catch (e) { } });
    document.getElementById("lf2").addEventListener("submit", function (e) { e.preventDefault(); var i = document.getElementById("lci"), tx = i.value.trim(); if (!tx) return; i.value = ""; api("/api/lives/" + encodeURIComponent(host) + "/comments", { method: "POST", body: { body: tx } }).catch(onErr); });
  }
  function viewGoLive() {
    var mode = "camera", live = false, last = "", likes = 0, ivs = [], stream = null;
    var me = cur();
    setView(top("Transmitir ao vivo", { back: true }) + '<div class="form" id="gl"></div>', "", "noNav");
    function draw() {
      document.getElementById("gl").innerHTML = '<div class="seg"><button data-gm="camera" class="' + (mode === "camera" ? "on" : "") + '">Câmera</button><button data-gm="game" class="' + (mode === "game" ? "on" : "") + '">Jogos</button></div><div class="livebox" id="gv"><div class="livemsg">' + (mode === "game" ? "Você vai escolher a tela ou janela do jogo" : "Sua câmera aparece aqui") + '</div></div><div class="err" id="ger2"></div><button class="btn" id="gs">Iniciar live</button>';
    }
    draw();
    document.getElementById("gl").addEventListener("click", function (e) {
      var m = e.target.closest("[data-gm]"); if (m) { mode = m.dataset.gm; draw(); return; }
      if (e.target.id === "gs") start(e.target);
      if (e.target.id === "ge") end();
    });
    function end() { leaveLive(); if (stream) stream.getTracks().forEach(function (t) { t.stop(); }); toast("Live encerrada."); location.hash = "#/trending"; }
    function start(btn) {
      var er = document.getElementById("ger2"); er.textContent = ""; btn.disabled = true; var n = 5; btn.textContent = "Começando em " + n + "…";
      var cd = setInterval(function () { n--; if (n > 0) { btn.textContent = "Começando em " + n + "…"; return; } clearInterval(cd); go(btn, er); }, 1000);
    }
    function go(btn, er) {
      loadLK().then(function () { return call(HOST + "/api/live?action=token", { method: "POST", body: { username: me.username, host: true } }); }).then(function (t) {
        var LK = window.LivekitClient, room = new LK.Room(); liveRoom = room;
        room.on(LK.RoomEvent.DataReceived, function (payload) { try { var m = JSON.parse(new TextDecoder().decode(payload)); if (m.t === "like" && m.n > 0) { likes += m.n; room.localParticipant.publishData(new TextEncoder().encode(JSON.stringify({ t: "total", n: likes })), { reliable: true }); var l = document.getElementById("gl2"); if (l) l.textContent = "♥ " + likes; } } catch (e) { } });
        function count() { var c = document.getElementById("gn"); if (c) c.textContent = room.remoteParticipants.size + " assistindo"; }
        room.on(LK.RoomEvent.ParticipantConnected, count); room.on(LK.RoomEvent.ParticipantDisconnected, count);
        return room.connect(t.url, t.token).then(function () {
          if (mode === "game") return room.localParticipant.setScreenShareEnabled(true, { audio: true }).then(function () { return room.localParticipant.setMicrophoneEnabled(true); });
          return room.localParticipant.setCameraEnabled(true).then(function () { return room.localParticipant.setMicrophoneEnabled(true); });
        }).then(function () {
          live = true;
          var pubs = Array.from(room.localParticipant.videoTrackPublications.values()); var vt = pubs[0] && pubs[0].track;
          var box = document.getElementById("gv"); box.innerHTML = ""; if (vt) { var v = vt.attach(); v.muted = true; v.setAttribute("playsinline", ""); box.appendChild(v); }
          document.getElementById("gl").insertAdjacentHTML("beforeend", "");
          btn.parentNode.querySelector(".seg").remove();
          btn.outerHTML = '<div class="liveinfo"><span class="live-badge">AO VIVO</span><span id="gn">0 assistindo</span><span id="gl2">♥ 0</span></div><div id="gcm" class="lcm"></div><button class="btn" id="ge" style="background:var(--danger)">Encerrar live</button>';
          ivs.push(liveChat(me.username.toLowerCase(), document.getElementById("gcm"), function () { return last; }, function (v2) { last = v2; }));
          liveStop = function () { ivs.forEach(clearInterval); };
        });
      }).catch(function (e) { leaveLive(); er.textContent = (e && e.name === "NotAllowedError") ? "Permita a câmera/tela e o microfone para transmitir." : (e.message || "Não foi possível iniciar a live."); btn.disabled = false; btn.textContent = "Iniciar live"; });
    }
  }

  /* ---------- configurações ---------- */
  var SET_ITEMS = [["activity", "Atividade", "Aparecer online"], ["location", "Localização", "Perto de mim e dados de localização"], ["fonts", "Fontes", "Letra do site e dos nomes"], ["stories", "Stories", "Quem vê seus stories por padrão"], ["account", "Conta", "Senha, e-mail, aparelhos e exclusão"], ["privacy", "Privacidade", "Quem vê, quem fala com você e bloqueios"], ["content", "Conteúdo", "Palavras ocultas e publicações arquivadas"], ["appearance", "Tela", "Tema claro ou escuro"], ["accessibility", "Acessibilidade", "Texto maior e menos animações"], ["storage", "Dados e armazenamento", "Cache e seus dados"], ["verify", "Verificação", "Peça o selo de conta verificada"], ["help", "Ajuda e suporte", "Termos, privacidade e sobre"]];
  function viewSettings(section) {
    if (!section) {
      setView(top("Configurações", { back: true }) + SET_ITEMS.map(function (i) { return '<a class="row" href="#/settings/' + i[0] + '"><span class="t"><b>' + i[1] + "</b><small>" + i[2] + '</small></span></a>'; }).join("") + '<button class="row" data-drop="' + activeId + '"><span class="t"><b style="color:var(--danger)">Sair desta conta</b></span></button>', "");
      return;
    }
    var title = (SET_ITEMS.filter(function (i) { return i[0] === section; })[0] || [0, "Configurações"])[1];
    setView(top(title, { back: true }) + '<div id="sb">' + spin() + "</div>", "");
    var sb = document.getElementById("sb"), me = cur();
    function sw(key, label, sub, on) { return '<button class="row" data-sw="' + key + '"><span class="t"><b>' + label + "</b>" + (sub ? "<small>" + sub + "</small>" : "") + '</span><span class="tog ' + (on ? "on" : "") + '"><i></i></span></button>'; }
    function choice(key, opts, val) { return opts.map(function (o) { return '<button class="row" data-ch="' + key + ":" + o[0] + '"><span class="rad ' + (val === o[0] ? "on" : "") + '"></span><span class="t"><b>' + o[1] + "</b></span></button>"; }).join(""); }
    if (section === "account") {
      sb.innerHTML = '<div class="form"><div class="sect" style="padding:0">Trocar senha</div><input class="inp" id="np" type="password" placeholder="Nova senha (mínimo 6)" autocomplete="new-password"><button class="btn" id="npb">Salvar senha</button><div class="sect" style="padding:12px 0 0">Trocar e-mail</div><input class="inp" id="ne" type="email" placeholder="novo@email.com"><button class="btn" id="neb">Salvar e-mail</button>' + err("aer") +
        '<button class="btn ghost" id="lo">Sair dos outros aparelhos</button><button class="btn" id="dl" style="background:var(--danger)">Excluir minha conta</button></div>';
      function upd(body, msg) { call(HOST + "/auth/v1/user", { method: "PUT", body: body }).then(function () { toast(msg); sb.querySelector("#aer").textContent = ""; }).catch(function (e) { sb.querySelector("#aer").textContent = e.message; }); }
      sb.querySelector("#npb").addEventListener("click", function () { upd({ password: sb.querySelector("#np").value }, "Senha alterada."); });
      sb.querySelector("#neb").addEventListener("click", function () { upd({ email: sb.querySelector("#ne").value }, "E-mail alterado."); });
      sb.querySelector("#lo").addEventListener("click", function () { call(HOST + "/auth/v1/logout?scope=others", { method: "POST" }).then(function () { toast("Pronto. Os outros aparelhos saíram da conta."); }).catch(onErr); });
      sb.querySelector("#dl").addEventListener("click", function () {
        var s = sheet("Excluir conta", '<div class="form"><div>Isso apaga sua conta e seus dados para sempre. Digite seu nome de usuário (@' + esc(me.username) + ") para confirmar.</div><input class=\"inp\" id=\"cu\">" + err("cer2") + '<button class="btn" id="cd2" style="background:var(--danger)">Excluir para sempre</button></div>');
        s.querySelector("#cd2").addEventListener("click", function () { api("/api/users/me/delete", { method: "POST", body: { confirmUsername: s.querySelector("#cu").value } }).then(function () { closeSheet(); dropAccount(activeId); toast("Conta excluída."); goto(accounts.length ? "#/" : "#/login"); }).catch(function (e) { s.querySelector("#cer2").textContent = e.message; }); });
      });
    } else if (section === "privacy") {
      call(HOST + "/rest/v1/profiles?id=eq." + me.id + "&select=is_private,who_can_message,who_can_comment").then(function (r) {
        var p = (r && r[0]) || {}, st = { is_private: !!p.is_private, who_can_message: p.who_can_message || "all", who_can_comment: p.who_can_comment || "all" };
        function draw() {
          sb.innerHTML = sw("is_private", "Conta privada", "Só quem você aprovar segue você e vê suas publicações e stories", st.is_private) + '<div class="sect">Quem pode falar com você</div>' + choice("who_can_message", [["all", "Todos"], ["following", "Só quem você segue"], ["nobody", "Ninguém"]], st.who_can_message) +
            '<div class="sect">Quem pode comentar suas publicações</div>' + choice("who_can_comment", [["all", "Todos"], ["followers", "Só seus seguidores"], ["nobody", "Ninguém"]], st.who_can_comment) +
            '<div class="sect">Pedidos de seguir</div><div id="fr"></div><div class="sect">Contas bloqueadas</div><div id="bl"></div>';
          reqs(); blocks();
        }
        function save(k, v) { var old = st[k]; st[k] = v; draw(); call(HOST + "/rest/v1/profiles?id=eq." + me.id, { method: "PATCH", body: JSON.parse('{"' + k + '":' + JSON.stringify(v) + "}") }).catch(function (e) { st[k] = old; draw(); onErr(e); }); }
        function reqs() {
          call(HOST + "/rest/v1/follow_requests?target_id=eq." + me.id + "&select=requester_id,profiles!follow_requests_requester_id_fkey(username,display_name,avatar_path)&order=created_at.desc&limit=100").then(function (l) {
            var el = document.getElementById("fr"); if (!el) return; l = Array.isArray(l) ? l : [];
            el.innerHTML = l.length ? l.map(function (x) { var p = x.profiles || {}; return '<div class="row">' + avatar(photoUrl(p.avatar_path), p.display_name || p.username) + '<span class="t"><b>' + esc(p.display_name || p.username) + "</b><small>@" + esc(p.username) + '</small></span><button class="sb pri" style="flex:none;width:72px" data-fr="ok:' + x.requester_id + '">Aceitar</button><button class="sb" style="flex:none;width:72px" data-fr="no:' + x.requester_id + '">Recusar</button></div>'; }).join("") : '<div class="empty" style="padding:12px 16px;text-align:left">Nenhum pedido.</div>';
          }).catch(function () { });
        }
        function blocks() {
          api("/api/users/me/blocks").then(function (j) { var el = document.getElementById("bl"); if (!el) return; var l = j.blocks || []; el.innerHTML = l.length ? l.map(function (b) { return '<div class="row"><span class="t"><b>' + esc(b.displayName || b.username) + "</b><small>@" + esc(b.username) + '</small></span><button class="sb" style="flex:none;width:90px" data-unblock="' + b.id + '">Desbloquear</button></div>'; }).join("") : '<div class="empty" style="padding:12px 16px;text-align:left">Ninguém bloqueado.</div>'; }).catch(function () { });
        }
        sb.addEventListener("click", function (e) {
          var t = e.target.closest("[data-sw],[data-ch],[data-fr],[data-unblock]"); if (!t) return;
          if (t.dataset.sw) save("is_private", !st.is_private);
          else if (t.dataset.ch) { var a = t.dataset.ch.split(":"); save(a[0], a[1]); }
          else if (t.dataset.fr) { var f = t.dataset.fr.split(":"); (f[0] === "ok" ? call(HOST + "/rest/v1/rpc/approve_follow_request", { method: "POST", body: { requester: f[1] } }) : call(HOST + "/rest/v1/follow_requests?requester_id=eq." + f[1] + "&target_id=eq." + me.id, { method: "DELETE" })).then(reqs).catch(onErr); }
          else if (t.dataset.unblock) api("/api/users/" + t.dataset.unblock + "/block", { method: "POST", body: {} }).then(blocks).catch(onErr);
        });
        draw();
      }).catch(function (e) { sb.innerHTML = '<div class="empty">' + esc(e.message) + "</div>"; });
    } else if (section === "content") {
      sb.innerHTML = '<div class="form"><label class="fld">Palavras ocultas (separe por vírgula)<textarea class="inp" id="hw" placeholder="Publicações com essas palavras não aparecem no seu feed"></textarea></label><button class="btn" id="hws">Salvar</button></div><div class="sect">Publicações arquivadas</div><div id="ar">' + spin() + "</div>";
      sb.querySelector("#hw").value = hiddenWords().join(", ");
      sb.querySelector("#hws").addEventListener("click", function () { LS.set("qb_hidden", sb.querySelector("#hw").value.split(",").map(function (x) { return x.trim().toLowerCase(); }).filter(Boolean)); toast("Palavras salvas."); });
      call(HOST + "/rest/v1/posts?user_id=eq." + me.id + "&archived_at=not.is.null&select=id,caption,media_type,media_path&order=archived_at.desc&limit=100").then(function (l) {
        var el = document.getElementById("ar"); if (!el) return; l = Array.isArray(l) ? l : [];
        el.innerHTML = l.length ? l.map(function (x) { return '<div class="row"><span class="av" style="border-radius:8px;' + (x.media_type === "image" && x.media_path ? "background-image:url('" + safeUrl(photoUrl(x.media_path)) + "')" : "") + '"></span><span class="t"><b>' + esc((x.caption || "Sem legenda").slice(0, 60)) + '</b></span><button class="sb" style="flex:none;width:90px" data-unarch="' + x.id + '">Desarquivar</button></div>'; }).join("") : '<div class="empty" style="padding:12px 16px;text-align:left">Nada arquivado.</div>';
      }).catch(function () { });
      sb.addEventListener("click", function (e) { var b = e.target.closest("[data-unarch]"); if (b) api("/api/posts/" + b.dataset.unarch + "/archive", { method: "POST", body: {} }).then(function () { b.closest(".row").remove(); toast("Publicação desarquivada."); feedBefore = null; }).catch(onErr); });
    } else if (section === "appearance") {
      var th = LS.get("qb_theme", "dark");
      sb.innerHTML = choice("theme", [["auto", "Igual ao aparelho"], ["dark", "Escuro"], ["light", "Claro"]], th);
      sb.addEventListener("click", function (e) { var t = e.target.closest("[data-ch]"); if (!t) return; var v = t.dataset.ch.split(":")[1]; LS.set("qb_theme", v); applyPrefs(); viewSettings("appearance"); });
    } else if (section === "accessibility") {
      sb.innerHTML = sw("big", "Texto maior", "Aumenta as letras do site", !!LS.get("qb_big", false)) + sw("calm", "Menos animações", "Tira transições e movimentos", !!LS.get("qb_calm", false));
      sb.addEventListener("click", function (e) { var t = e.target.closest("[data-sw]"); if (!t) return; var k = "qb_" + t.dataset.sw; LS.set(k, !LS.get(k, false)); applyPrefs(); viewSettings("accessibility"); });
    } else if (section === "activity") {
      sb.innerHTML = sw("online", "Mostrar que estou online", "Outras pessoas podem ver quando você está ativo", LS.get("qb_online", true) !== false) + '<div class="sect">Convites</div><button class="row" data-invite><span class="t"><b>Convidar amigos</b><small>Compartilhe o link do QINBLY</small></span></button>';
      sb.addEventListener("click", function (e) { var t = e.target.closest("[data-sw]"); if (!t) return; LS.set("qb_online", LS.get("qb_online", true) === false); viewSettings("activity"); });
    } else if (section === "location") {
      sb.innerHTML = sw("locon", "Usar minha localização", "Mostra publicações e avisos perto de você", LS.get("qb_loc_on", true) !== false) + '<div class="sect">Sua cidade</div><div style="padding:0 16px 12px;color:var(--muted)">' + esc(myCity() || "Ainda não definida. Abra Perto de mim no feed.") + '</div><div class="form"><button class="btn ghost" id="lcl" style="color:var(--danger)">Apagar dados de localização</button></div>';
      sb.addEventListener("click", function (e) { var t = e.target.closest("[data-sw]"); if (t) { LS.set("qb_loc_on", LS.get("qb_loc_on", true) === false); viewSettings("location"); } });
      sb.querySelector("#lcl").addEventListener("click", function () { confirmSheet("Apagar dados de localização?", "Apaga sua cidade e os avisos que você escreveu no mural.", "Apagar", true, function () { call(HOST + "/rest/v1/neighborhood_notices?user_id=eq." + me.id, { method: "DELETE" }).catch(function () { }).then(function () { return call(HOST + "/rest/v1/profiles?id=eq." + me.id, { method: "PATCH", body: { city: "" }, headers: { Prefer: "return=minimal" } }); }).then(function () { LS.set("qb_place", ""); HERE = null; toast("Dados de localização apagados."); viewSettings("location"); }).catch(onErr); }); });
    } else if (section === "fonts") {
      var ft = LS.get("qb_font", "padrao"), fn = LS.get("qb_nfont", "padrao");
      sb.innerHTML = '<div class="sect">Fonte do site</div>' + FONTS.map(function (f) { return '<button class="row" data-ch="font:' + f[0] + '"><span class="rad ' + (ft === f[0] ? "on" : "") + '"></span><span class="t"><b style="' + (f[2] ? "font-family:" + f[2] : "") + '">' + f[1] + "</b></span></button>"; }).join("") + '<div class="sect">Fonte dos nomes</div>' + FONTS.map(function (f) { return '<button class="row" data-ch="nfont:' + f[0] + '"><span class="rad ' + (fn === f[0] ? "on" : "") + '"></span><span class="t"><b style="' + (f[2] ? "font-family:" + f[2] : "") + '">' + f[1] + "</b></span></button>"; }).join("");
      sb.addEventListener("click", function (e) { var t = e.target.closest("[data-ch]"); if (!t) return; var p = t.dataset.ch.split(":"); LS.set("qb_" + p[0], p[1]); applyFonts(); viewSettings("fonts"); });
    } else if (section === "stories") {
      var sa = LS.get("qb_story_aud", "all");
      sb.innerHTML = '<div style="padding:12px 16px;color:var(--muted)">Em cada story você ainda pode mudar isso antes de publicar.</div>' + AUDIENCES.map(function (a) { return '<button class="row" data-ch="aud:' + a[0] + '"><span class="rad ' + (sa === a[0] ? "on" : "") + '"></span><span class="t"><b>' + a[1] + "</b></span></button>"; }).join("");
      sb.addEventListener("click", function (e) { var t = e.target.closest("[data-ch]"); if (!t) return; LS.set("qb_story_aud", t.dataset.ch.split(":")[1]); viewSettings("stories"); });
    } else if (section === "storage") {
      sb.innerHTML = '<div class="form"><button class="btn ghost" id="cc">Limpar cache deste aparelho</button><button class="btn ghost" id="ex">Baixar meus dados (arquivo JSON)</button></div>';
      sb.querySelector("#cc").addEventListener("click", function () { (window.caches ? caches.keys().then(function (k) { return Promise.all(k.map(function (x) { return caches.delete(x); })); }) : Promise.resolve()).then(function () { FEED = []; toast("Cache limpo."); }); });
      sb.querySelector("#ex").addEventListener("click", function () { api("/api/users/me/export").then(function (j) { var a = document.createElement("a"); a.href = URL.createObjectURL(new Blob([JSON.stringify(j, null, 2)], { type: "application/json" })); a.download = "meus-dados-qinbly.json"; a.click(); toast("Arquivo gerado."); }).catch(onErr); });
    } else if (section === "verify") {
      call(HOST + "/functions/v1/qinbly-verify").then(function (j) {
        if (j.verified) { sb.innerHTML = '<div class="empty">Sua conta já é verificada. ' + VERIFIED + "</div>"; return; }
        var r = j.request;
        sb.innerHTML = '<div class="form">' + (r ? "<div>Seu último pedido: <b>" + (r.status === "pending" ? "em análise" : r.status === "approved" ? "aprovado" : "não aprovado") + "</b>" + (r.review_note ? "<br><small style='color:var(--muted)'>" + esc(r.review_note) + "</small>" : "") + "</div>" : "") +
          (r && r.status === "pending" ? "" : '<div style="color:var(--muted)">Para pedir, tenha foto de perfil, bio e pelo menos 1 publicação.</div><textarea class="inp" id="vr" maxlength="1000" placeholder="Por que sua conta deve ser verificada? (10 a 1000 caracteres)"></textarea>' + err("ver") + '<button class="btn" id="vs">Enviar pedido</button>') + "</div>";
        var b = sb.querySelector("#vs"); if (b) b.addEventListener("click", function () { call(HOST + "/functions/v1/qinbly-verify/request", { method: "POST", body: { reason: sb.querySelector("#vr").value } }).then(function () { toast("Pedido enviado."); viewSettings("verify"); }).catch(function (e) { sb.querySelector("#ver").textContent = e.message; }); });
      }).catch(function (e) { sb.innerHTML = '<div class="empty">' + esc(e.message) + "</div>"; });
    } else {
      sb.innerHTML = '<a class="row" href="termos.html"><span class="t"><b>Termos de uso</b></span></a><a class="row" href="privacidade.html"><span class="t"><b>Política de privacidade</b></span></a><a class="row" href="baixar.html"><span class="t"><b>Baixar o app para Android</b></span></a><button class="row" data-install><span class="t"><b>Instalar no iPhone</b><small>Adicionar à Tela de Início</small></span></button><div class="empty">QINBLY · versão web</div>';
    }
  }
  function applyPrefs() {
    applyFonts();
    var th = LS.get("qb_theme", "dark"), light = th === "light" || (th === "auto" && window.matchMedia && matchMedia("(prefers-color-scheme: light)").matches);
    document.documentElement.setAttribute("data-theme", light ? "light" : "dark");
    document.body.classList.toggle("big", !!LS.get("qb_big", false));
    document.body.classList.toggle("calm", !!LS.get("qb_calm", false));
    var m = document.querySelector('meta[name="theme-color"]'); if (m) m.content = light ? "#ffffff" : "#16161a";
  }

  /* ---------- destaques, comentários, stories ---------- */
  function openHighlight(g) {
    var items = (g.items || [g]).slice().reverse();
    openStories({ username: "", displayName: g.title, avatarUrl: "", stories: items.map(function (h) { return { id: "", mediaUrl: h.mediaUrl, mediaType: h.mediaType, caption: h.caption, createdAt: h.createdAt }; }), hl: true });
  }

  /* =====================================================================
     Segunda leva: mensagens completas, compartilhar, caixa de entrada,
     preferências locais, esqueci a senha, presença
     ===================================================================== */
  function el(id) { return document.getElementById(id); }
  function enc(s) { return encodeURIComponent(s); }
  function lk(name) { var m = cur(); return "qb_" + (m ? m.id : "x") + "_" + name; }
  function lget(name, d) { return LS.get(lk(name), d); }
  function lset(name, v) { LS.set(lk(name), v); }
  /* apagar mensagem "para mim" de verdade: fica guardado no servidor e vale em todos os aparelhos */
  function hideMsgsServer(ids) {
    ids = (ids || []).filter(Boolean); if (!ids.length) return Promise.resolve();
    return call(HOST + "/rest/v1/message_hides?on_conflict=user_id,message_id", { method: "POST", body: ids.map(function (i) { return { message_id: i }; }), headers: { Prefer: "resolution=merge-duplicates,return=minimal" } });
  }
  function hideConversationServer(peerId) {
    var me = cur(); if (!me || !peerId) return Promise.resolve();
    return call(HOST + "/rest/v1/messages?select=id&or=(and(sender_id.eq." + me.id + ",recipient_id.eq." + peerId + "),and(sender_id.eq." + peerId + ",recipient_id.eq." + me.id + "))&limit=5000").then(function (r) { return hideMsgsServer((Array.isArray(r) ? r : []).map(function (x) { return x.id; })); });
  }
  function peerOf(list) { var me = cur(), m = (list || [])[0]; return m && me ? (m.senderId === me.id ? m.recipientId : m.senderId) : ""; }
  function chatPref(u) { return (lget("chats", {}) || {})[String(u).toLowerCase()] || {}; }
  function setChatPref(u, patch) { var all = lget("chats", {}) || {}, k = String(u).toLowerCase(); all[k] = Object.assign(all[k] || {}, patch); lset("chats", all); }
  function myCity() { return LS.get("qb_city", "") || LS.get("qb_place", ""); }
  function copyText(t, msg) { if (navigator.clipboard) navigator.clipboard.writeText(t).then(function () { toast(msg || "Copiado."); }).catch(function () { toast("Não foi possível copiar."); }); else toast("Não foi possível copiar."); }
  function postLink(id) { return new URL("#/post/" + id, location.href).href; }
  function pathOf(url) { var i = String(url).indexOf("/post-media/"); return i < 0 ? url : url.slice(i + 12); }
  function dayLabel(d) {
    var t = new Date(), y = new Date(Date.now() - 86400000);
    if (d.toDateString() === t.toDateString()) return "Hoje";
    if (d.toDateString() === y.toDateString()) return "Ontem";
    return d.toLocaleDateString("pt-BR");
  }

  /* ---------- esqueci a senha ---------- */
  function forgotSheet() {
    var s = sheet("Esqueci a senha", '<div class="form"><div style="color:var(--muted)">Digite seu e-mail ou nome de usuário. Enviamos um link para criar uma nova senha.</div><input class="inp" id="fg" autocapitalize="none" placeholder="E-mail ou usuário"><div class="err" id="fge"></div><button class="btn" id="fgb">Enviar link</button></div>');
    s.querySelector("#fgb").addEventListener("click", function () {
      var v = s.querySelector("#fg").value.trim().toLowerCase(), b = this; if (!v) return;
      b.disabled = true;
      (v.indexOf("@") > 0 ? call(API + "/api/auth/forgot", { method: "POST", anon: true, body: { email: v } }) : call(API + "/signin/username", { method: "POST", anon: true, body: { action: "forgot", username: v.replace(/^@/, "") } }))
        .then(function (j) { s.querySelector(".body").innerHTML = '<div class="empty">' + esc(j.message || "Se existir uma conta com esses dados, enviamos o link para criar uma nova senha.") + "</div>"; })
        .catch(function (e) { s.querySelector("#fge").textContent = e.message; b.disabled = false; });
    });
  }

  /* ---------- presença (online) ---------- */
  function beat() { if (!cur() || document.hidden || LS.get("qb_online", true) === false) return; api("/api/presence/heartbeat", { method: "POST", body: {} }).catch(function () { }); }

  /* ---------- mensagens: tipos, emojis, reações ---------- */
  var REACTS = ["👍", "❤️", "😂", "😮", "😢", "🙏", "🔥", "👏", "😍", "🎉", "🤔", "😡", "🥳", "😎", "💯", "👎", "🤣", "😭", "🙌", "💔"];
  var EMOJIS = Array.from("😀😃😄😁😆😅😂🤣😊😇🙂😉😌😍🥰😘😋😛😜🤪🤗🤭🤔😐😏😒🙄😬😮😯😲😳🥺😢😭😤😡🤬😱😨😰😓😴🤒🤕🤢🤮🥳😎🤓🥱👍👎👏🙌🙏💪👋🤝✌️🤞👌❤️🧡💛💚💙💜🖤💔💕💯🔥✨⭐🎉🎂🎁🍕🍔🍟🍺☕⚽🏆🎵🎶📸🚗✈️🌴🌞🌙🌧️🐶🐱🐼🦋🌹");
  function reactOrder() { return REACTS.slice().sort(function (a, b) { return LS.get("qb_rc_" + b, 0) - LS.get("qb_rc_" + a, 0); }); }
  function msgText(m) { return m.imageUrl ? "foto" : m.stickerUrl ? "figurinha" : m.audioUrl ? "mensagem de voz" : m.fileUrl ? (m.fileName || "documento") : m.poll ? (m.poll.question || "enquete") : String(m.body || ""); }
  function snippet(m) {
    if (m.poll) return "Enquete"; if (m.stickerUrl) return "Figurinha"; if (m.imageUrl) return "Foto"; if (m.audioUrl) return "Mensagem de voz"; if (m.fileUrl) return m.fileName || "Documento";
    return String(m.body || "").replace(/\n/g, " ").slice(0, 80);
  }
  function rawBody(m) {
    if (m.poll) return null;
    if (m.stickerUrl) return "qinbly:sticker:" + pathOf(m.stickerUrl);
    if (m.imageUrl) return "qinbly:img:" + pathOf(m.imageUrl);
    if (m.audioUrl) return "qinbly:audio:" + pathOf(m.audioUrl) + ":" + (m.audioSeconds || 1);
    if (m.fileUrl) return "qinbly:file:" + pathOf(m.fileUrl) + ":" + enc(m.fileName || "documento");
    return m.body || null;
  }
  var SHARED = {};
  function msgHTML(m) {
    var b = typeof m.body === "string" ? m.body : "";
    if (!m.imageUrl && !m.stickerUrl && !m.audioUrl && !m.fileUrl && !m.poll) {
      var sp = /post\/([0-9a-fA-F-]{36})/.exec(b);
      if (sp) {
        var lines = b.split("\n").filter(function (x) { return x.trim() && x.indexOf("/post/") < 0; }), ii = -1;
        lines.forEach(function (x, i) { if (ii < 0 && /^Confira esta publicação no [^:]*:/.test(x)) ii = i; });
        var note = ii > 0 ? lines.slice(0, ii).join("\n") : "";
        return '<div class="shp" data-sp="' + sp[1] + '"><div class="spin"><i></i></div></div>' + (note ? '<div class="shn">' + linkify(note) + "</div>" : "");
      }
      if (b.indexOf("> ") === 0 && b.indexOf("\n") > 0) return '<div class="qt">' + esc(b.slice(2, b.indexOf("\n"))) + "</div>" + linkify(b.slice(b.indexOf("\n") + 1));
    }
    return msgBody(m);
  }
  function hydrateShared(root) {
    if (!root) return;
    root.querySelectorAll("[data-sp]:not([data-ok])").forEach(function (box) {
      box.setAttribute("data-ok", "1"); var id = box.dataset.sp;
      (SHARED[id] ? Promise.resolve(SHARED[id]) : api("/api/posts/" + id).then(function (j) { SHARED[id] = j.post; return j.post; })).then(function (p) {
        var a = p.author || {}, u = safeUrl(p.mediaUrl);
        var media = p.mediaType === "video" && u ? '<video src="' + u + '" preload="metadata" muted playsinline></video>' : u ? '<img src="' + u + '" loading="lazy" alt="">' : '<div class="shtx" style="' + (p.textBg ? "background:" + bgCss(p.textBg) : "") + '">' + esc((p.caption || "").slice(0, 160)) + "</div>";
        box.innerHTML = '<a href="#/post/' + id + '">' + media + '<span class="shw">' + avatar(a.avatarUrl, a.displayName || a.username, "s") + "<b>" + esc(a.displayName || a.username || "") + "</b></span>" + (u && p.caption ? "<small>" + esc(p.caption.slice(0, 120)) + "</small>" : "") + "</a>";
      }).catch(function () { box.innerHTML = '<a href="#/post/' + id + '">Publicação compartilhada</a>'; });
    });
  }
  function reactChips(list, myId) {
    var g = {}; list.forEach(function (r) { g[r.emoji] = g[r.emoji] || { n: 0, mine: false }; g[r.emoji].n++; if (r.user_id === myId) g[r.emoji].mine = true; });
    return '<span class="rxs">' + Object.keys(g).map(function (k) { return '<i class="' + (g[k].mine ? "on" : "") + '">' + k + (g[k].n > 1 ? " " + g[k].n : "") + "</i>"; }).join("") + "</span>";
  }

  /* ---------- menu de uma mensagem ---------- */
  function msgSheet(m, ctx) {
    var me = cur(), own = ctx.own(m), mine = ((ctx.reacts && ctx.reacts[m.id]) || []).filter(function (r) { return r.user_id === me.id; })[0];
    var rx = reactOrder(), rows = "";
    if (ctx.react) rows += '<div class="rxrow">' + rx.slice(0, 6).map(function (e) { return '<button data-re="' + e + '" class="' + (mine && mine.emoji === e ? "on" : "") + '">' + e + "</button>"; }).join("") + '<button data-re="+" aria-label="Mais">＋</button></div><div id="rxall" class="rxrow wrap" hidden>' + rx.slice(6).map(function (e) { return '<button data-re="' + e + '">' + e + "</button>"; }).join("") + "</div>";
    function row(k, t, danger) { return '<button class="row" data-ma="' + k + '"><span class="t"><b' + (danger ? ' style="color:var(--danger)"' : "") + ">" + t + "</b></span></button>"; }
    if (ctx.reply) rows += row("reply", "Responder");
    var txt = rawBody(m); if (txt && !m.stickerUrl && !m.imageUrl && !m.audioUrl && !m.fileUrl && !/post\/[0-9a-fA-F-]{36}/.test(txt)) rows += row("copy", "Copiar texto");
    if (m.imageUrl || m.fileUrl || m.audioUrl) rows += row("open", m.imageUrl ? "Abrir foto" : m.audioUrl ? "Abrir áudio" : "Abrir documento");
    if (m.stickerUrl) rows += row("savestk", "Salvar nas minhas figurinhas");
    if (ctx.forward && rawBody(m)) rows += row("fwd", "Encaminhar");
    if (ctx.pin) rows += row("pin", ctx.pinned === m.id ? "Desafixar" : "Fixar no topo");
    if (ctx.cancel && own) rows += row("cancel", "Cancelar envio", true);
    if (ctx.hide) rows += row("hide", "Apagar para mim", true);
    if (ctx.del && ctx.del(m)) rows += row("del", "Excluir mensagem", true);
    if (!own && ctx.report) rows += row("report", "Denunciar mensagem", true);
    var s = sheet("Sobre a mensagem", rows);
    s.addEventListener("click", function (e) {
      var r = e.target.closest("[data-re]"), a = e.target.closest("[data-ma]");
      if (r) {
        if (r.dataset.re === "+") { s.querySelector("#rxall").hidden = false; r.hidden = true; return; }
        var em = r.dataset.re; closeSheet(); LS.set("qb_rc_" + em, LS.get("qb_rc_" + em, 0) + 1);
        (mine && mine.emoji === em ? call(HOST + "/rest/v1/message_reactions?message_id=eq." + m.id, { method: "DELETE" }) : call(HOST + "/rest/v1/message_reactions?on_conflict=message_id,user_id", { method: "POST", body: { message_id: m.id, emoji: em }, headers: { Prefer: "resolution=merge-duplicates,return=minimal" } }))
          .then(function () { ctx.reload(); }).catch(function () { toast("Não foi possível reagir."); });
        return;
      }
      if (!a) return; var k = a.dataset.ma; closeSheet();
      if (k === "reply") ctx.reply(m);
      else if (k === "copy") copyText(txt, "Texto copiado.");
      else if (k === "open") window.open(safeUrl(m.imageUrl || m.audioUrl || m.fileUrl), "_blank", "noopener");
      else if (k === "savestk") api("/api/stickers", { method: "POST", body: { fileId: pathOf(m.stickerUrl) } }).then(function () { toast("Figurinha salva nas suas figurinhas."); }).catch(function (x) { toast(x.message || "Não foi possível salvar a figurinha."); });
      else if (k === "fwd") forwardSheet(rawBody(m));
      else if (k === "pin") ctx.pin(m);
      else if (k === "cancel") confirmSheet("Cancelar envio?", "A mensagem some para as duas pessoas.", "Cancelar envio", true, function () {
        call(HOST + "/rest/v1/messages?id=eq." + m.id, { method: "DELETE", headers: { Prefer: "return=representation" } }).then(function (r) { if (Array.isArray(r) && !r.length) throw new Error("Só quem enviou pode cancelar o envio."); toast("Envio cancelado."); var gh = lget("hmsgs", []); gh.push(m.id); lset("hmsgs", gh); ctx.repaint(); }).catch(function (x) { toast(x.message || "Não foi possível cancelar o envio."); });
      });
      else if (k === "hide") { var h = lget("hmsgs", []); h.push(m.id); lset("hmsgs", h); ctx.repaint(); (m.senderId ? hideMsgsServer([m.id]) : Promise.resolve()).then(function () { toast("Mensagem apagada só para você."); }).catch(function () { toast("Apagada aqui, mas não consegui salvar no servidor."); }); }
      else if (k === "del") ctx.remove(m);
      else if (k === "report") reportSheet("message", m.id);
    });
  }
  function convPicker(title, onPick, withCommunities) {
    var s = sheet(title, '<div style="padding:0 16px 8px"><input class="inp" id="cq" placeholder="Buscar pessoas" autocomplete="off"></div><div id="cr">' + spin() + "</div>");
    var recent = [], t;
    function row(u, kind) { return '<button class="row" data-pick="' + kind + ":" + esc(u.username || u.id) + '">' + (kind === "com" ? '<span class="av" style="font-size:18px">' + esc(u.emoji || "👥") + "</span>" : avatar(u.avatarUrl, u.displayName || u.username)) + '<span class="t"><b>' + esc(kind === "com" ? u.name : (u.displayName || u.username)) + "</b>" + (kind === "com" ? "<small>Comunidade</small>" : "<small>@" + esc(u.username) + "</small>") + "</span></button>"; }
    function paint(users) { el("cr").innerHTML = (users.length ? '<div class="sect">' + (el("cq").value.trim() ? "Pessoas encontradas" : "Conversas recentes") + "</div>" + users.map(function (u) { return row(u, "u"); }).join("") : '<div class="empty">Nenhuma pessoa encontrada.</div>') + (!el("cq").value.trim() && s.__coms && s.__coms.length ? '<div class="sect">Comunidades</div>' + s.__coms.map(function (c) { return row({ username: c.id, name: c.name, emoji: c.emoji }, "com"); }).join("") : ""); }
    Promise.all([api("/api/inbox").catch(function () { return { conversations: [] }; }), withCommunities ? api("/api/communities?filter=mine").catch(function () { return { communities: [] }; }) : Promise.resolve({ communities: [] })]).then(function (a) {
      recent = (a[0].conversations || []).map(function (c) { return { username: c.username, displayName: c.displayName, avatarUrl: c.avatarUrl }; }); s.__coms = a[1].communities || []; if (el("cr")) paint(recent);
    });
    s.querySelector("#cq").addEventListener("input", function (e) {
      clearTimeout(t); var q = e.target.value.trim().replace(/^@/, "");
      if (q.length < 2) { paint(recent); return; }
      t = setTimeout(function () { api("/api/users/search?q=" + enc(q)).then(function (j) { paint(j.users || []); }).catch(function () { }); }, 300);
    });
    s.addEventListener("click", function (e) { var b = e.target.closest("[data-pick]"); if (!b) return; var p = b.dataset.pick.split(":"); onPick(p[0], p.slice(1).join(":"), s); });
  }
  function forwardSheet(raw) {
    convPicker("Encaminhar para", function (kind, who, s) {
      closeSheet(); toast("Enviando…");
      api("/api/messages/" + enc(who), { method: "POST", body: { body: raw } }).then(function () { toast("Mensagem encaminhada."); }).catch(function () { toast("Não foi possível encaminhar."); });
    }, false);
  }
  function shareSheet(id) {
    var p = POSTS[id] || SHARED[id] || {}, link = postLink(id), noteHtml = '<div style="padding:0 16px 8px"><input class="inp" id="shn" maxlength="300" placeholder="Escreva uma mensagem (opcional)"></div>';
    var s = sheet("Compartilhar", noteHtml + '<button class="row" data-sh="copy"><span class="t"><b>Copiar link</b></span></button>' + (navigator.share ? '<button class="row" data-sh="app"><span class="t"><b>Compartilhar em outro aplicativo</b></span></button>' : "") + '<div id="shbox"></div>');
    var box = s.querySelector("#shbox");
    box.insertAdjacentHTML("beforeend", '<div class="sect">Enviar para</div><div id="shr">' + spin() + "</div>");
    var recent = [], coms = [];
    function row(u, kind) { return '<button class="row" data-pick="' + kind + ":" + esc(u.username || u.id) + '">' + (kind === "com" ? '<span class="av" style="font-size:18px">' + esc(u.emoji || "👥") + "</span>" : avatar(u.avatarUrl, u.displayName || u.username)) + '<span class="t"><b>' + esc(kind === "com" ? u.name : (u.displayName || u.username)) + "</b><small>" + (kind === "com" ? "Comunidade" : "@" + esc(u.username)) + '</small></span><span class="sb pri sent" style="flex:none;width:72px;line-height:30px;text-align:center">Enviar</span></button>'; }
    function paint(users) { var h = users.map(function (u) { return row(u, "u"); }).join("") + coms.map(function (c) { return row({ username: c.id, name: c.name, emoji: c.emoji }, "com"); }).join(""); var r = el("shr"); if (r) r.innerHTML = h || '<div class="empty">Nenhuma conversa recente. Busque uma pessoa na aba Mensagens.</div>'; }
    Promise.all([api("/api/inbox").catch(function () { return { conversations: [] }; }), api("/api/communities?filter=mine").catch(function () { return { communities: [] }; })]).then(function (a) { recent = (a[0].conversations || []).map(function (c) { return { username: c.username, displayName: c.displayName, avatarUrl: c.avatarUrl }; }); coms = a[1].communities || []; paint(recent); });
    s.addEventListener("click", function (e) {
      var sh = e.target.closest("[data-sh]"), pk = e.target.closest("[data-pick]");
      if (sh) { if (sh.dataset.sh === "copy") { copyText(link, "Link copiado."); closeSheet(); } else { navigator.share({ title: "QINBLY", url: link }).catch(function () { }); } return; }
      if (!pk) return;
      var kind = pk.dataset.pick.split(":")[0], who = pk.dataset.pick.split(":").slice(1).join(":"), note = s.querySelector("#shn").value.trim();
      var body = (note ? note + "\n" : "") + "Confira esta publicação no QINBLY: " + String(p.caption || "").slice(0, 160) + "\nhttps://qinbly.com/post/" + id;
      var b = pk.querySelector(".sent"); if (b) b.textContent = "Enviando";
      (kind === "com" ? api("/api/communities/" + who + "/posts", { method: "POST", body: { body: body } }) : api("/api/messages/" + enc(who), { method: "POST", body: { body: body } }))
        .then(function () { if (b) { b.textContent = "Enviado"; b.classList.remove("pri"); } }).catch(function (x) { if (b) b.textContent = "Enviar"; toast(x.message || "Não foi possível enviar."); });
    });
  }

  /* ---------- emojis, localização, câmera ---------- */
  function emojiPanel(box, ta) {
    box.hidden = !box.hidden; if (box.hidden) return;
    box.innerHTML = EMOJIS.map(function (e) { return "<button type=\"button\">" + e + "</button>"; }).join("");
    box.onclick = function (e) { var b = e.target.closest("button"); if (!b) return; var st = ta.selectionStart || ta.value.length; ta.value = ta.value.slice(0, st) + b.textContent + ta.value.slice(ta.selectionEnd || st); ta.focus(); };
  }
  function sendLocation(send, done) {
    if (!navigator.geolocation) return toast("Este navegador não tem localização.");
    toast("Pegando sua localização…");
    navigator.geolocation.getCurrentPosition(function (pos) {
      send("Minha localização:\nhttps://maps.google.com/?q=" + pos.coords.latitude + "," + pos.coords.longitude).then(function () { done && done(); }).catch(onErr);
    }, function () { toast("Não consegui pegar sua localização. Permita o acesso e tente de novo."); }, { timeout: 15000 });
  }

  /* ---------- menu e mídia da conversa ---------- */
  function sharedMedia(msgs) {
    var tab = "m";
    var s = sheet("Mídia, links e documentos", '<div style="padding:0 16px 8px"><div class="seg" id="smt"><button data-t="m" class="on">Mídia</button><button data-t="l">Links</button><button data-t="d">Documentos</button><button data-t="a">Áudios</button></div></div><div id="smb"></div>');
    function paint() {
      var h = "";
      if (tab === "m") { var l = msgs.filter(function (m) { return m.imageUrl; }); h = l.length ? '<div class="grid">' + l.map(function (m) { return '<a href="' + safeUrl(m.imageUrl) + '" target="_blank" rel="noopener" style="background-image:url(\'' + safeUrl(m.imageUrl) + "')\"></a>"; }).join("") + "</div>" : '<div class="empty">Nenhuma foto compartilhada ainda.</div>'; }
      if (tab === "l") { var ls = []; msgs.forEach(function (m) { var r = String(m.body || "").match(/https?:\/\/[^\s]+/g); if (r) r.forEach(function (u) { ls.push(u); }); }); h = ls.length ? ls.map(function (u) { return '<a class="row" href="' + safeUrl(u) + '" target="_blank" rel="noopener"><span class="t"><b style="font-size:14px;word-break:break-all">' + esc(u) + "</b></span></a>"; }).join("") : '<div class="empty">Nenhum link compartilhado ainda.</div>'; }
      if (tab === "d") { var d = msgs.filter(function (m) { return m.fileUrl; }); h = d.length ? d.map(function (m) { return '<a class="row" href="' + safeUrl(m.fileUrl) + '" target="_blank" rel="noopener"><span class="t"><b>📄 ' + esc(m.fileName || "Documento") + "</b></span></a>"; }).join("") : '<div class="empty">Nenhum documento compartilhado ainda.</div>'; }
      if (tab === "a") { var a = msgs.filter(function (m) { return m.audioUrl; }); h = a.length ? a.map(function (m) { return '<div style="padding:8px 16px"><audio controls preload="none" src="' + safeUrl(m.audioUrl) + '"></audio></div>'; }).join("") : '<div class="empty">Nenhum áudio compartilhado ainda.</div>'; }
      s.querySelector("#smb").innerHTML = h;
    }
    s.querySelector("#smt").addEventListener("click", function (e) { var b = e.target.closest("[data-t]"); if (!b) return; tab = b.dataset.t; s.querySelectorAll("#smt button").forEach(function (x) { x.classList.toggle("on", x === b); }); paint(); });
    paint();
  }
  function chatMenu(name, ctx) {
    var pf = chatPref(name);
    function row(k, t, d) { return '<button class="row" data-cx="' + k + '"><span class="t"><b' + (d ? ' style="color:var(--danger)"' : "") + ">" + t + "</b></span></button>"; }
    var s = sheet("@" + name, row("search", "Pesquisar nesta conversa") + row("media", "Mídia, links e documentos") + row("pin", pf.pin ? "Desafixar conversa" : "Fixar conversa") + row("mute", pf.mute ? "Reativar notificações" : "Silenciar notificações") + row("profile", "Ver perfil") + row("clear", "Limpar conversa", true) + row("del", "Apagar conversa", true) + row("block", "Bloquear usuário", true) + row("report", "Denunciar usuário", true));
    s.addEventListener("click", function (e) {
      var b = e.target.closest("[data-cx]"); if (!b) return; var k = b.dataset.cx; closeSheet();
      if (k === "search") ctx.search();
      else if (k === "media") sharedMedia(ctx.msgs());
      else if (k === "pin") { setChatPref(name, { pin: !pf.pin }); toast(pf.pin ? "Conversa desafixada." : "Conversa fixada."); }
      else if (k === "mute") { setChatPref(name, { mute: !pf.mute }); toast(pf.mute ? "Notificações reativadas." : "Notificações silenciadas."); }
      else if (k === "profile") location.hash = "#/u/" + name;
      else if (k === "clear") confirmSheet("Limpar conversa?", "As mensagens somem só para você. A outra pessoa continua vendo.", "Limpar", true, function () { setChatPref(name, { cleared: new Date().toISOString() }); ctx.repaint(); hideConversationServer(peerOf(ctx.msgs ? ctx.msgs() : [])).then(function () { toast("Conversa limpa."); }).catch(function () { toast("Limpa aqui, mas não consegui salvar no servidor."); }); });
      else if (k === "del") confirmSheet("Apagar conversa?", "Ela some da sua lista e as mensagens não voltam. A outra pessoa continua vendo.", "Apagar", true, function () { setChatPref(name, { cleared: new Date().toISOString(), hiddenAt: new Date().toISOString() }); hideConversationServer(peerOf(ctx.msgs ? ctx.msgs() : [])).then(function () { toast("Conversa apagada."); }).catch(function () { toast("Apagada aqui, mas não consegui salvar no servidor."); }).then(function () { location.hash = "#/inbox"; }); });
      else api("/api/users/" + enc(name)).then(function (j) {
        if (k === "report") reportSheet("user", j.user.id);
        else confirmSheet("Bloquear @" + name + "?", "Vocês deixam de ver as publicações e mensagens um do outro.", "Bloquear", true, function () { api("/api/users/" + j.user.id + "/block", { method: "POST", body: {} }).then(function () { toast("Conta bloqueada."); location.hash = "#/inbox"; }).catch(onErr); });
      }).catch(onErr);
    });
  }

  /* ---------- conversa ---------- */
  function viewChat(name) {
    var me = cur(), last = "", MSGS = [], REACT = {}, reply = null, q = "";
    setView(top("@" + esc(name), { back: true, right: '<button class="ib" id="cs" aria-label="Pesquisar">' + svg("search") + '</button><button class="ib" id="cmn" aria-label="Opções">' + svg("dots") + "</button>" }) +
      '<div id="sbar" class="sbar" hidden><input id="sq" placeholder="Pesquisar mensagens..." autocomplete="off"></div><div id="pbar" class="pbar" hidden></div><div class="chat" id="ch">' + spin() + "</div>" +
      '<div id="rpq" class="rpl" hidden></div><div class="composer"><form class="in" id="cf"><button type="button" class="ib" id="at" aria-label="Anexar">' + svg("plus") + '</button><button type="button" class="ib" id="em" aria-label="Emojis">😊</button><textarea id="mi" rows="1" maxlength="10000" placeholder="Mensagem…"></textarea><button class="send" type="submit">Enviar</button></form><div id="emp" class="emp" hidden></div></div>', "inbox", "noNav chatv");
    function own(m) { return m.senderId === me.id; }
    function visible() {
      var hid = lget("hmsgs", []), cl = chatPref(name).cleared || "";
      return MSGS.filter(function (m) { return hid.indexOf(m.id) < 0 && (!cl || m.createdAt > cl) && (!q || msgText(m).toLowerCase().indexOf(q) >= 0); });
    }
    function paint() {
      var ch = el("ch"); if (!ch) return;
      var list = visible(), pinId = chatPref(name).pinmsg || "", pm = pinId && MSGS.filter(function (m) { return m.id === pinId; })[0], pb = el("pbar");
      if (pm) { pb.hidden = false; pb.innerHTML = "📌 " + esc(snippet(pm)) + '<button aria-label="Desafixar" data-unpin>✕</button>'; } else pb.hidden = true;
      var sig = list.map(function (m) { return m.id + (m.poll ? JSON.stringify(m.poll.votes) + m.poll.myVote : "") + ((REACT[m.id] || []).map(function (r) { return r.user_id + r.emoji; }).join()); }).join("|") + pinId + q;
      if (sig === last) return; last = sig;
      var stick = (window.innerHeight + window.scrollY) >= document.body.scrollHeight - 160 || !ch.dataset.done; ch.dataset.done = "1";
      var h = "", prev = "";
      list.forEach(function (m, i) {
        var d = new Date(m.createdAt), day = d.toDateString();
        if (day !== prev) { prev = day; h += '<div class="day">' + dayLabel(d) + "</div>"; }
        h += '<div class="' + bubbleClass(m) + (own(m) ? " me" : "") + '" data-msg="' + m.id + '">' + msgHTML(m) + ((REACT[m.id] || []).length ? reactChips(REACT[m.id], me.id) : "") + "</div>";
        var nx = list[i + 1];
        if (!nx || own(nx) !== own(m) || new Date(nx.createdAt).toDateString() !== day) {
          h += '<div class="tm' + (own(m) ? " me" : "") + '">' + d.toLocaleTimeString("pt-BR", { hour: "2-digit", minute: "2-digit" }) + "</div>";
        }
      });
      ch.innerHTML = h || '<div class="empty">' + (q ? "Nenhuma mensagem encontrada." : "Diga olá para @" + esc(name) + ".") + "</div>";
      hydrateShared(ch);
      if (stick && !q) window.scrollTo(0, document.body.scrollHeight);
    }
    function load() {
      api("/api/messages/" + enc(name)).then(function (j) {
        MSGS = j.messages || [];
        var ids = MSGS.slice(-80).map(function (m) { return m.id; });
        return (ids.length ? call(HOST + "/rest/v1/message_reactions?select=message_id,user_id,emoji&message_id=in.(" + ids.join(",") + ")").catch(function () { return []; }) : Promise.resolve([])).then(function (r) {
          REACT = {}; (Array.isArray(r) ? r : []).forEach(function (x) { (REACT[x.message_id] = REACT[x.message_id] || []).push(x); }); paint();
        });
      }).catch(function (e) { if (e.expired) onErr(e); });
    }
    function send(text) { return api("/api/messages/" + enc(name), { method: "POST", body: { body: text } }); }
    window.__reload = load; load(); timers.push(setInterval(load, 5000));
    var ctx = {
      own: own, reacts: REACT, react: true, reload: load, repaint: function () { last = ""; paint(); }, forward: true, report: true, cancel: true, hide: true, pinned: "",
      reply: function (m) { reply = m; var r = el("rpq"); r.hidden = false; r.textContent = "Respondendo: " + snippet(m) + "  ✕"; el("mi").focus(); },
      pin: function (m) { var cp = chatPref(name).pinmsg; setChatPref(name, { pinmsg: cp === m.id ? "" : m.id }); last = ""; paint(); }
    };
    el("ch").addEventListener("click", function (e) {
      var b = e.target.closest("[data-msg]"); if (!b || e.target.closest("a,audio,video,button,.opt,.shp")) return;
      var m = MSGS.filter(function (x) { return x.id === b.dataset.msg; })[0]; if (!m) return;
      ctx.reacts = REACT; ctx.pinned = chatPref(name).pinmsg || ""; msgSheet(m, ctx);
    });
    el("pbar").addEventListener("click", function (e) { if (e.target.closest("[data-unpin]")) { setChatPref(name, { pinmsg: "" }); last = ""; paint(); } });
    el("rpq").addEventListener("click", function () { reply = null; this.hidden = true; });
    el("cs").addEventListener("click", function () { var b = el("sbar"); b.hidden = !b.hidden; if (!b.hidden) el("sq").focus(); else { q = ""; el("sq").value = ""; last = ""; paint(); } });
    el("sq").addEventListener("input", function (e) { q = e.target.value.trim().toLowerCase(); last = ""; paint(); });
    el("cmn").addEventListener("click", function () { chatMenu(name, { search: function () { el("sbar").hidden = false; el("sq").focus(); }, msgs: function () { return MSGS; }, repaint: ctx.repaint }); });
    el("at").addEventListener("click", function () { attachMenu(send, load); });
    el("em").addEventListener("click", function () { emojiPanel(el("emp"), el("mi")); });
    el("cf").addEventListener("submit", function (e) {
      e.preventDefault();
      var inp = el("mi"), t = inp.value.trim(); if (!t) return;
      var body = reply ? "> " + snippet(reply) + "\n" + t : t; inp.value = ""; var rp = reply; reply = null; el("rpq").hidden = true;
      send(body).then(load).catch(function (x) { inp.value = t; reply = rp; onErr(x); });
    });
  }

  /* ---------- caixa de entrada ---------- */
  var FOLLOWING = null;
  function viewInbox() {
    var tab = "all", cs = [], me = cur(), qv = "";
    setView(top("Mensagens", { right: '<button class="ib" data-find aria-label="Nova conversa">' + svg("search") + "</button>" }) + '<div class="chips"><button data-it="all" class="on">Todas</button><button data-it="unread">Não lidas</button><button data-it="req">Solicitações</button></div><div style="padding:0 16px 8px"><input class="inp" id="iq" placeholder="Pesquisar conversas" autocomplete="off"></div><div id="ib">' + spin() + "</div>", "inbox");
    function paint() {
      var box = el("ib"); if (!box) return;
      var l = cs.filter(function (c) { var p = chatPref(c.username); return !(p.hiddenAt && c.createdAt <= p.hiddenAt); });
      if (tab === "unread") l = l.filter(function (c) { return c.unreadCount > 0; });
      if (tab === "req") l = l.filter(function (c) { return FOLLOWING && !FOLLOWING[String(c.username).toLowerCase()]; });
      else if (FOLLOWING) { /* todas */ }
      if (qv) l = l.filter(function (c) { return (c.displayName + " " + c.username + " " + c.body).toLowerCase().indexOf(qv) >= 0; });
      l.sort(function (a, b) { return (chatPref(b.username).pin ? 1 : 0) - (chatPref(a.username).pin ? 1 : 0); });
      var sug = RC.sug || [];
      box.innerHTML = l.length ? l.map(function (c) {
        var p = chatPref(c.username), snip = c.body && /^qinbly:/.test(c.body) ? "Mídia" : (c.body || "");
        return '<button class="row" data-chat="' + esc(c.username) + '">' + avatar(c.avatarUrl, c.displayName || c.username) + '<span class="t"><b>' + (p.pin ? "📌 " : "") + esc(c.displayName || c.username) + (p.mute ? " 🔕" : "") + "</b><small>" + esc(snip) + '</small></span><span class="meta">' + ago(c.createdAt) + (c.unreadCount ? ' <i class="dotn">' + c.unreadCount + "</i>" : "") + "</span></button>";
      }).join("") : '<div class="empty">' + (tab === "unread" ? "Tudo em dia!" : tab === "req" ? "Nenhuma solicitação. Mensagens de pessoas que você não segue aparecem aqui." : "Nenhuma conversa ainda. Toque na lupa para procurar alguém.") + "</div>" +
        (tab === "all" && !l.length && sug.length ? '<div class="sect">Pessoas para seguir</div>' + sug.slice(0, 5).map(function (u) { return '<a class="row" href="#/u/' + esc(u.username) + '">' + avatar(u.avatarUrl, u.displayName || u.username) + '<span class="t"><b>' + esc(u.displayName || u.username) + "</b><small>@" + esc(u.username) + "</small></span></a>"; }).join("") : "");
    }
    api("/api/inbox").then(function (j) {
      cs = j.conversations || [];
      unread = cs.reduce(function (s, c) { return s + (c.unreadCount || 0); }, 0); paintBadges(); paint();
    }).catch(onErr);
    (FOLLOWING ? Promise.resolve() : api("/api/users/" + enc(me.username) + "/following").then(function (j) { FOLLOWING = {}; (j.users || []).forEach(function (u) { FOLLOWING[String(u.username).toLowerCase()] = 1; }); }).catch(function () { })).then(paint);
    document.querySelector(".chips").addEventListener("click", function (e) { var b = e.target.closest("[data-it]"); if (!b) return; tab = b.dataset.it; document.querySelectorAll(".chips button").forEach(function (x) { x.classList.toggle("on", x === b); }); paint(); });
    el("iq").addEventListener("input", function (e) { qv = e.target.value.trim().toLowerCase(); paint(); });
  }


  /* =====================================================================
     Home (pergunta do dia, desafio), Perto de mim, mural de avisos,
     notificações completas, stories com público, configurações extras
     ===================================================================== */
  var DAILY_Q = ["Qual foi seu melhor momento de hoje?", "O que te fez sorrir hoje?", "Qual música não sai da sua cabeça?", "Qual é o seu lugar favorito?", "O que você aprendeu essa semana?", "Qual foi sua última boa surpresa?", "Com quem você gostaria de falar hoje?"];
  var WEEK_CH = [["meucafe", "Foto do seu café"], ["meupet", "Foto do seu pet"], ["pordosol", "Um pôr do sol perto de você"], ["meuprato", "O que você comeu de melhor"], ["meucantinho", "Seu cantinho favorito"], ["diadetreino", "Seu treino de hoje"]];
  var NEWPRE = null;
  function dayOfYear() { var n = new Date(), s = new Date(n.getFullYear(), 0, 0); return Math.floor((n - s) / 86400000); }
  function homeHeader() {
    var d = dayOfYear(), q = DAILY_Q[d % DAILY_Q.length], ch = WEEK_CH[Math.floor(d / 7) % WEEK_CH.length];
    var part = {}; FEED.forEach(function (p) { if ((p.caption || "").toLowerCase().indexOf("#" + ch[0]) >= 0 && p.author) part[p.author.username] = 1; });
    var n = Object.keys(part).length;
    return '<button class="comp" data-newwith="text:"><span>✏️</span><span class="g">Escrever algo...</span><span class="pl" data-newwith="poll:">Enquete</span></button>' +
      '<div class="hcards"><button class="hc" data-newwith="text:#perguntadodia "><small>PERGUNTA DO DIA</small><b>' + esc(q) + "</b><span>Responder com uma publicação →</span></button>" +
      '<div class="hc"><small>DESAFIO DA SEMANA</small><b>' + esc(ch[1]) + "</b>" + (n ? "<span>" + n + (n === 1 ? " participante" : " participantes") + "</span>" : "") + '<span><a href="#/tag/' + ch[0] + '">Ver</a> · <button data-newwith="image:#' + ch[0] + ' " style="color:var(--accent);font-weight:600">Participar</button></span></div></div>';
  }

  /* ---------- localização ---------- */
  function getPlace(ok, fail) {
    if (!navigator.geolocation) return fail("Este navegador não tem localização.");
    navigator.geolocation.getCurrentPosition(function (pos) {
      var lat = pos.coords.latitude, lng = pos.coords.longitude;
      fetch("https://nominatim.openstreetmap.org/reverse?format=jsonv2&zoom=10&accept-language=pt-BR&lat=" + lat + "&lon=" + lng).then(function (r) { return r.json(); }).then(function (j) {
        var a = j.address || {}, city = a.city || a.town || a.village || a.municipality || a.county || "", st = a.state || "";
        ok({ lat: lat, lng: lng, place: city ? city + (st ? ", " + st : "") : "" });
      }).catch(function () { ok({ lat: lat, lng: lng, place: "" }); });
    }, function () { fail("Não consegui pegar sua localização. Permita o acesso no navegador e tente de novo."); }, { timeout: 15000, maximumAge: 60000 });
  }
  var HERE = null;
  function nearbyView() {
    var fd = el("fd"); if (!fd) return;
    if (LS.get("qb_loc_on", true) === false) { fd.innerHTML = '<div class="empty">A localização está desligada.<br><br><button class="btn" data-locon style="max-width:260px;margin:auto">Ligar localização</button></div>'; return; }
    function content() {
      fd.innerHTML = '<div class="empty" style="padding:24px 24px 8px"><b style="color:var(--text);font-size:18px">Você está em ' + esc(HERE.place || "um lugar sem nome") + '</b><br><button class="sb" data-nrefresh style="width:120px;margin-top:10px">Atualizar</button></div><div class="chips" id="ntabs"><button data-nt="posts" class="on">Publicações</button><button data-nt="board">Mural de avisos</button></div><div id="nbody">' + spin() + "</div>";
      var tabs = el("ntabs"); tabs.addEventListener("click", function (e) { var b = e.target.closest("[data-nt]"); if (!b) return; tabs.querySelectorAll("button").forEach(function (x) { x.classList.toggle("on", x === b); }); if (b.dataset.nt === "posts") nearPosts(); else board(); });
      nearPosts();
    }
    if (HERE) return content();
    fd.innerHTML = '<div class="empty">Procurando sua localização…</div>';
    getPlace(function (h) {
      HERE = h; if (h.place) { LS.set("qb_place", h.place); var me = cur(); call(HOST + "/rest/v1/profiles?id=eq." + me.id, { method: "PATCH", body: { city: h.place }, headers: { Prefer: "return=minimal" } }).catch(function () { }); }
      if (el("fd") && feedTab === "Perto de mim") content();
    }, function (msg) { if (el("fd")) fd.innerHTML = '<div class="empty">' + esc(msg) + '<br><br><button class="btn" data-nrefresh style="max-width:260px;margin:auto">Tentar de novo</button></div>'; });
  }
  function nearPosts() {
    var nb = el("nbody"); if (!nb) return; nb.innerHTML = spin();
    var place = HERE.place || myCity(), city = place.split(",")[0].replace(/[*%,()]/g, " ").trim();
    if (!city) { nb.innerHTML = '<div class="empty">Não foi possível descobrir sua cidade.</div>'; return; }
    var now = new Date().toISOString(), me = cur();
    call(HOST + "/rest/v1/posts?select=id,profiles!posts_user_id_fkey!inner(city)&profiles.city=ilike." + enc(city) + "*&visibility=eq.public&archived_at=is.null&or=(publish_at.is.null,publish_at.lte." + now + ")&user_id=neq." + me.id + "&order=created_at.desc&limit=24").then(function (rows) {
      var ids = (Array.isArray(rows) ? rows : []).map(function (r) { return r.id; });
      if (!ids.length) { nb.innerHTML = '<div class="empty">Ainda não há publicações perto de você.<br>Quando alguém da sua cidade publicar, aparece aqui.</div>'; return; }
      return Promise.all(ids.map(function (id) { return api("/api/posts/" + id).then(function (j) { return j.post; }).catch(function () { return null; }); })).then(function (ps) {
        ps = ps.filter(Boolean); ps.forEach(function (p) { POSTS[p.id] = p; });
        if (el("nbody")) el("nbody").innerHTML = ps.length ? ps.map(postHTML).join("") : '<div class="empty">Ainda não há publicações perto de você.</div>';
      });
    }).catch(function (e) { if (el("nbody")) el("nbody").innerHTML = '<div class="empty">Não foi possível carregar. ' + esc(e.message) + "</div>"; });
  }
  var NOTICES = {};
  function board() {
    var nb = el("nbody"); if (!nb) return;
    var me = cur(), gLat = Math.round(HERE.lat * 100) / 100, gLng = Math.round(HERE.lng * 100) / 100;
    nb.innerHTML = '<div class="form" style="padding-bottom:8px"><textarea class="inp" id="nbt" maxlength="500" placeholder="Escreva um aviso para a vizinhança…"></textarea><button class="btn" id="nbp">Publicar</button></div><div class="sect">Avisos de até ~5 km · somem em 48 h</div><div id="nbl">' + spin() + "</div>";
    function load() {
      var dLat = 0.05, dLng = 0.05 / Math.max(0.2, Math.cos(HERE.lat * Math.PI / 180));
      call(HOST + "/rest/v1/neighborhood_notices?lat=gte." + (gLat - dLat) + "&lat=lte." + (gLat + dLat) + "&lng=gte." + (gLng - dLng) + "&lng=lte." + (gLng + dLng) + "&expires_at=gt." + new Date().toISOString() + "&select=id,body,kind,photo,created_at,user_id,profiles!neighborhood_notices_user_id_fkey(username,display_name),neighborhood_comments(count)&order=created_at.desc&limit=50").then(function (l) {
        l = Array.isArray(l) ? l : []; l.forEach(function (n) { NOTICES[n.id] = n; });
        var box = el("nbl"); if (!box) return;
        box.innerHTML = l.length ? l.map(function (n) {
          var p = n.profiles || {}, cnt = (n.neighborhood_comments && n.neighborhood_comments[0] && n.neighborhood_comments[0].count) || 0;
          return '<button class="row" data-notice="' + n.id + '" style="align-items:flex-start"><span class="t"><small style="color:#9A9EF7">' + esc(p.display_name || p.username || "") + " · " + ago(n.created_at) + '</small><b style="font-weight:400;white-space:pre-wrap">' + esc(n.body) + "</b><small>" + (cnt ? cnt + (cnt === 1 ? " comentário" : " comentários") : "Comentar") + "</small></span></button>";
        }).join("") : '<div class="empty">Nenhum aviso por perto ainda. Escreva o primeiro.</div>';
      }).catch(function (e) { if (el("nbl")) el("nbl").innerHTML = '<div class="empty">' + esc(e.message) + "</div>"; });
    }
    load();
    el("nbp").addEventListener("click", function () {
      var t = el("nbt").value.trim(), b = this; if (!t) return; b.disabled = true;
      call(HOST + "/rest/v1/neighborhood_notices", { method: "POST", body: { body: t, lat: gLat, lng: gLng, kind: "aviso" }, headers: { Prefer: "return=minimal" } }).then(function () { toast("Aviso publicado. Some em 48 horas."); el("nbt").value = ""; load(); }).catch(function (e) { toast(e.message || "Não foi possível publicar."); }).then(function () { b.disabled = false; });
    });
  }
  function noticeSheet(id) {
    var n = NOTICES[id]; if (!n) return; var me = cur(), p = n.profiles || {}, reply = null, list = [];
    var s = sheet("Aviso", '<div style="padding:4px 16px 12px"><small style="color:#9A9EF7">' + esc(p.display_name || p.username || "") + " · " + ago(n.created_at) + '</small><div style="white-space:pre-wrap;font-size:16px;margin-top:4px">' + linkify(n.body) + "</div>" + (n.user_id === me.id ? '<button class="link" id="nd" style="color:var(--danger);margin-top:8px">Apagar aviso</button>' : "") + '</div><div id="nc">' + spin() + '</div>', '<div id="nrp" class="rpl" hidden></div><form class="cin" id="ncf"><input id="nci" maxlength="1000" placeholder="Comentar…" autocomplete="off"><button type="submit">Enviar</button></form>');
    function draw() {
      var tops = list.filter(function (c) { return !c.parent_id; });
      function one(c, sub) { var a = c.profiles || {}; return '<div class="cm ' + (sub ? "sub" : "") + '"><div class="b"><b>' + esc(a.username || "") + "</b>" + linkify(c.body) + "<small>" + ago(c.created_at) + ' · <button data-nr="' + c.id + ":" + esc(a.username || "") + '">Responder</button>' + (c.user_id === me.id ? ' · <button data-nx="' + c.id + '">Apagar</button>' : "") + "</small></div></div>"; }
      var h = ""; tops.forEach(function (c) { h += one(c, false); list.filter(function (r) { return r.parent_id === c.id; }).forEach(function (r) { h += one(r, true); }); });
      el("nc").innerHTML = h || '<div class="empty" style="padding:16px">Nenhum comentário ainda.</div>';
    }
    function load() { call(HOST + "/rest/v1/neighborhood_comments?notice_id=eq." + id + "&select=id,parent_id,user_id,body,created_at,profiles!neighborhood_comments_user_id_fkey(username,display_name)&order=created_at.asc&limit=300").then(function (r) { list = Array.isArray(r) ? r : []; draw(); }).catch(function () { }); }
    load();
    var del = s.querySelector("#nd"); if (del) del.addEventListener("click", function () { call(HOST + "/rest/v1/neighborhood_notices?id=eq." + id, { method: "DELETE" }).then(function () { closeSheet(); toast("Aviso apagado."); if (el("nbl")) board(); }).catch(onErr); });
    s.addEventListener("click", function (e) {
      var r = e.target.closest("[data-nr]"), x = e.target.closest("[data-nx]");
      if (r) { var a = r.dataset.nr.split(":"); reply = a[0]; var rp = s.querySelector("#nrp"); rp.hidden = false; rp.textContent = "Respondendo a @" + a[1] + " (toque para cancelar)"; s.querySelector("#nci").focus(); }
      if (x) call(HOST + "/rest/v1/neighborhood_comments?id=eq." + x.dataset.nx, { method: "DELETE" }).then(load).catch(onErr);
    });
    s.querySelector("#nrp").addEventListener("click", function () { reply = null; this.hidden = true; });
    s.querySelector("#ncf").addEventListener("submit", function (e) {
      e.preventDefault(); var i = s.querySelector("#nci"), t = i.value.trim(); if (!t) return; i.value = "";
      var body = { notice_id: id, user_id: me.id, body: t }; if (reply) body.parent_id = reply;
      call(HOST + "/rest/v1/neighborhood_comments", { method: "POST", body: body, headers: { Prefer: "return=minimal" } }).then(function () { reply = null; s.querySelector("#nrp").hidden = true; load(); }).catch(onErr);
    });
  }

  /* ---------- notificações completas ---------- */
  var NOTIF_KINDS = [["follow", "Novos seguidores e pedidos de seguir"], ["like", "Curtidas"], ["comment", "Comentários"], ["mention", "Menções (@)"], ["message", "Mensagens"]];
  function notifOff(kind) { return (lget("noff", []) || []).indexOf(kind) >= 0; }
  function viewNotifs() {
    var filt = "all", list = [], reqs = [], backs = [];
    setView(top("Notificações", { right: '<button class="ib" id="nread" aria-label="Marcar como lidas">' + svg("check") + '</button><button class="ib" id="nset" aria-label="Ajustes">' + svg("gear") + "</button>" }) + '<div class="chips"><button data-nf="all" class="on">Todas</button><button data-nf="act">Atividades</button><button data-nf="men">Menções</button><button data-nf="fol">Seguindo</button></div><div id="nt">' + spin() + "</div>", "notif");
    var me = cur();
    function kindOf(n) { return /^(mention|mention_comment|tag)$/.test(n.kind) ? "mention" : /^(follow|follow_request|follow_accepted)$/.test(n.kind) ? "follow" : /^(comment|reply)$/.test(n.kind) ? "comment" : n.kind; }
    function paint() {
      var box = el("nt"); if (!box) return;
      var l = list.filter(function (n) { return !notifOff(kindOf(n)); });
      if (filt === "act") l = l.filter(function (n) { return n.kind !== "message"; });
      if (filt === "men") l = l.filter(function (n) { return kindOf(n) === "mention"; });
      if (filt === "fol") l = l.filter(function (n) { return kindOf(n) === "follow"; });
      var h = "";
      if (filt === "all" && reqs.length) h += '<button class="row" data-reqs><span class="av" style="font-size:18px">👤</span><span class="t"><b>Pedidos de seguir</b><small>' + reqs.length + (reqs.length === 1 ? " pedido" : " pedidos") + " para aprovar</small></span></button>";
      function item(n) {
        var a = n.actor || {}, href = /^(like|comment|mention|mention_comment|tag|reply)$/.test(n.kind) && n.entityId ? "#/post/" + n.entityId : n.kind === "message" ? "#/chat/" + esc(a.username) : a.username ? "#/u/" + esc(a.username) : "#/";
        return '<a class="row" href="' + href + '">' + avatar(a.avatarUrl, a.displayName || a.username) + '<span class="t"><b style="font-weight:400">' + (a.username ? "<b style='font-weight:600'>" + esc(a.username) + "</b> " : "") + esc(NOTIF_TEXT[n.kind] || "tem uma novidade para você") + "</b><small>" + ago(n.createdAt) + "</small></span>" + (n.kind === "follow" && a.username && !n.followedBack ? '<span class="sb pri fb" data-fback="' + esc(a.username) + '" style="flex:none;width:104px;line-height:30px;text-align:center">Seguir de volta</span>' : "") + (n.readAt ? "" : '<i class="dotn"></i>') + "</a>";
      }
      var fresh = l.filter(function (n) { return !n.readAt; }), old = l.filter(function (n) { return n.readAt; });
      if (fresh.length) h += '<div class="sect">Novas</div>' + fresh.map(item).join("");
      if (old.length) h += '<div class="sect">' + (fresh.length ? "Anteriores" : "Recentes") + "</div>" + old.map(item).join("");
      if (!l.length && !reqs.length) h += '<div class="empty">' + (filt === "all" ? "Você está em dia! Novos seguidores, curtidas e comentários aparecem aqui." : "Nada por aqui.") + "</div>";
      box.innerHTML = h;
      box.querySelectorAll("[data-fback]").forEach(function (b) { b.addEventListener("click", function (e) { e.preventDefault(); e.stopPropagation(); api("/api/users/" + enc(b.dataset.fback)).then(function (j) { return followUser(j.user.id, ""); }).then(function () { b.remove(); }).catch(onErr); }); });
    }
    Promise.all([api("/api/notifications"), call(HOST + "/rest/v1/follow_requests?target_id=eq." + me.id + "&select=requester_id,profiles!follow_requests_requester_id_fkey(username,display_name,avatar_path)&order=created_at.desc&limit=100").catch(function () { return []; })]).then(function (a) {
      list = a[0].notifications || []; reqs = Array.isArray(a[1]) ? a[1] : []; paint();
      if (list.some(function (n) { return !n.readAt; })) setTimeout(function () { api("/api/notifications/read", { method: "POST", body: {} }).then(function () { notifN = 0; paintBadges(); }).catch(function () { }); }, 4000);
    }).catch(function (e) { el("nt").innerHTML = '<div class="empty">' + esc(e.message) + "</div>"; });
    document.querySelector(".chips").addEventListener("click", function (e) { var b = e.target.closest("[data-nf]"); if (!b) return; filt = b.dataset.nf; document.querySelectorAll(".chips button").forEach(function (x) { x.classList.toggle("on", x === b); }); paint(); });
    el("nread").addEventListener("click", function () { api("/api/notifications/read", { method: "POST", body: {} }).then(function () { list.forEach(function (n) { n.readAt = n.readAt || "x"; }); notifN = 0; paintBadges(); paint(); toast("Marcadas como lidas."); }).catch(onErr); });
    el("nset").addEventListener("click", function () {
      var s = sheet("Ajustes dos avisos", NOTIF_KINDS.map(function (k) { return '<button class="row" data-nk="' + k[0] + '"><span class="t"><b>' + k[1] + '</b></span><span class="tog ' + (notifOff(k[0]) ? "" : "on") + '"><i></i></span></button>'; }).join("") + '<div style="padding:8px 16px 16px;color:var(--muted);font-size:12px">Vale só para este navegador.</div>');
      s.addEventListener("click", function (e) { var b = e.target.closest("[data-nk]"); if (!b) return; var off = lget("noff", []) || [], k = b.dataset.nk, i = off.indexOf(k); if (i >= 0) off.splice(i, 1); else off.push(k); lset("noff", off); b.querySelector(".tog").classList.toggle("on", i >= 0); paint(); });
    });
    document.getElementById("nt").addEventListener("click", function (e) {
      if (!e.target.closest("[data-reqs]")) return;
      var s = sheet("Pedidos de seguir", "");
      function draw() { s.querySelector(".body").innerHTML = reqs.length ? reqs.map(function (r) { var p = r.profiles || {}; return '<div class="row">' + avatar(photoUrl(p.avatar_path), p.display_name || p.username) + '<span class="t"><b>' + esc(p.display_name || p.username || "") + "</b><small>@" + esc(p.username || "") + '</small></span><button class="sb pri" style="flex:none;width:74px" data-ap="' + r.requester_id + '">Aceitar</button><button class="sb" style="flex:none;width:74px" data-rj="' + r.requester_id + '">Recusar</button></div>'; }).join("") : '<div class="empty">Nenhum pedido.</div>'; }
      draw();
      s.addEventListener("click", function (e) {
        var a = e.target.closest("[data-ap]"), r = e.target.closest("[data-rj]");
        if (a) call(HOST + "/rest/v1/rpc/approve_follow_request", { method: "POST", body: { requester: a.dataset.ap } }).then(function () { reqs = reqs.filter(function (x) { return x.requester_id !== a.dataset.ap; }); draw(); paint(); toast("Pedido aceito."); }).catch(onErr);
        if (r) call(HOST + "/rest/v1/follow_requests?requester_id=eq." + r.dataset.rj + "&target_id=eq." + me.id, { method: "DELETE" }).then(function () { reqs = reqs.filter(function (x) { return x.requester_id !== r.dataset.rj; }); draw(); paint(); }).catch(onErr);
      });
    });
  }

  /* ---------- story: publicar com público e duração ---------- */
  var AUDIENCES = [["all", "Todos"], ["followers", "Seguidores"], ["friends", "Amigos (quem você segue e te segue)"], ["near", "Perto de você (sua cidade)"]];
  function addStory() {
    var f = document.createElement("input"); f.type = "file"; f.accept = "image/*,video/*";
    f.addEventListener("change", function () {
      var file = f.files[0]; if (!file) return;
      var isVid = file.type.indexOf("video/") === 0;
      if ((isVid ? VID_OK : IMG_OK).indexOf(file.type) < 0) return toast("Esse tipo de arquivo não é aceito.");
      var url = URL.createObjectURL(file), aud = LS.get("qb_story_aud", "all");
      var s = sheet("Publicar story", '<div class="form"><div class="tbg" style="background:#000;border-radius:12px;padding:0;overflow:hidden;max-height:260px">' + (isVid ? '<video src="' + url + '" controls style="max-height:260px"></video>' : '<img src="' + url + '" style="max-height:260px;object-fit:contain" alt="">') + '</div><input class="inp" id="spc" maxlength="300" placeholder="Legenda (opcional)"><div class="sect" style="padding-left:0">Quem pode ver</div>' + AUDIENCES.map(function (a) { return '<button class="row" style="padding:8px 0" data-aud="' + a[0] + '"><span class="rad ' + (aud === a[0] ? "on" : "") + '"></span><span class="t"><b style="font-size:14px">' + a[1] + "</b></span></button>"; }).join("") + '<label class="fld">Duração<select class="inp" id="sdu">' + [24, 12, 6, 3, 1].map(function (h) { return '<option value="' + h + '">' + h + (h === 1 ? " hora" : " horas") + "</option>"; }).join("") + '</select></label><div class="err" id="spe"></div><button class="btn" id="spb">Publicar story</button></div>');
      s.addEventListener("click", function (e) { var a = e.target.closest("[data-aud]"); if (!a) return; aud = a.dataset.aud; s.querySelectorAll("[data-aud] .rad").forEach(function (r) { r.classList.remove("on"); }); a.querySelector(".rad").classList.add("on"); });
      s.querySelector("#spb").addEventListener("click", function () {
        var b = this, er = s.querySelector("#spe"); b.disabled = true; b.textContent = "Enviando…";
        if (aud === "near" && !myCity()) { er.textContent = "Abra Perto de mim no feed primeiro para saber sua cidade."; b.disabled = false; b.textContent = "Publicar story"; return; }
        (isVid ? Promise.resolve(file) : shrink(file)).then(function (x) { return upload(x, "story_media"); }).then(function (path) {
          return call(HOST + "/rest/v1/stories", { method: "POST", body: { media_path: path, media_type: isVid ? "video" : "image", caption: s.querySelector("#spc").value.trim(), audience: aud, city: aud === "near" ? myCity() : "", expires_at: new Date(Date.now() + Number(s.querySelector("#sdu").value) * 3600000).toISOString() }, headers: { Prefer: "return=minimal" } });
        }).then(function () { closeSheet(); LS.set("qb_story_aud", aud); toast("Story publicado."); loadStories(); }).catch(function (x) { er.textContent = x.message; b.disabled = false; b.textContent = "Publicar story"; });
      });
    });
    f.click();
  }
  function storyInfo(st) {
    var s = sheet("Seu story", '<div class="sect">Quem viu</div><div id="svw">' + spin() + '</div><div class="sect">Respostas</div><div id="scm"></div><div class="sect">Quem pode ver</div>' + AUDIENCES.map(function (a) { return '<button class="row" data-aud="' + a[0] + '"><span class="t"><b style="font-size:14px">' + a[1] + "</b></span></button>"; }).join(""));
    api("/api/stories/" + st.id + "/views").then(function (j) { var l = j.viewers || []; el("svw").innerHTML = l.length ? l.map(function (v) { return '<a class="row" href="#/u/' + esc(v.username) + '">' + avatar(v.avatarUrl, v.displayName || v.username) + '<span class="t"><b style="font-size:14px">' + esc(v.displayName || v.username) + "</b><small>" + ago(v.viewedAt) + "</small></span></a>"; }).join("") : '<div class="empty" style="padding:12px">Ninguém viu ainda.</div>'; }).catch(function () { if (el("svw")) el("svw").innerHTML = ""; });
    call(HOST + "/rest/v1/story_comments?story_id=eq." + st.id + "&select=id,body,created_at,kind,profiles(username,display_name)&order=created_at.asc&limit=200").then(function (l) { l = Array.isArray(l) ? l : []; el("scm").innerHTML = l.length ? l.map(function (c) { var p = c.profiles || {}; return '<div class="cm"><div class="b"><b>' + esc(p.username || "") + "</b>" + esc(c.kind === "comment" ? c.body : "(" + c.kind + ")") + "<small>" + ago(c.created_at) + "</small></div></div>"; }).join("") : '<div class="empty" style="padding:12px">Nenhuma resposta.</div>'; }).catch(function () { });
    s.addEventListener("click", function (e) { var a = e.target.closest("[data-aud]"); if (!a) return; if (a.dataset.aud === "near" && !myCity()) return toast("Abra Perto de mim no feed primeiro para saber sua cidade."); call(HOST + "/rest/v1/stories?id=eq." + st.id, { method: "PATCH", body: a.dataset.aud === "near" ? { audience: "near", city: myCity() } : { audience: a.dataset.aud }, headers: { Prefer: "return=minimal" } }).then(function () { closeSheet(); toast("Público do story atualizado."); }).catch(onErr); });
  }

  /* ---------- configurações extras ---------- */
  var FONTS = [["padrao", "Padrão", ""], ["moderna", "Moderna", "'Trebuchet MS','Segoe UI',sans-serif"], ["classica", "Clássica", "Georgia,'Times New Roman',serif"], ["elegante", "Elegante", "'Palatino Linotype',Palatino,serif"], ["minimalista", "Minimalista", "'Helvetica Neue',Arial,sans-serif"], ["arredondada", "Arredondada", "ui-rounded,'Arial Rounded MT Bold',Nunito,sans-serif"], ["mono", "Monoespaçada", "ui-monospace,Consolas,monospace"]];
  function fontCss(k) { var f = FONTS.filter(function (x) { return x[0] === k; })[0]; return f ? f[2] : ""; }
  function applyFonts() {
    var r = document.documentElement.style, a = fontCss(LS.get("qb_font", "padrao")), n = fontCss(LS.get("qb_nfont", "padrao"));
    if (a) r.setProperty("--font", a); else r.removeProperty("--font");
    if (n) r.setProperty("--nfont", n); else r.removeProperty("--nfont");
  }
  function inviteFriends() {
    var url = new URL("./", location.href).href, text = "Venha para o QINBLY: " + url;
    if (navigator.share) navigator.share({ title: "QINBLY", text: "Venha para o QINBLY", url: url }).catch(function () { }); else copyText(text, "Convite copiado.");
  }

  /* ---------- cliques (uma escuta só para o app inteiro) ---------- */
  function toggleFollow(id, name) {
    api("/api/users/" + id + "/follow", { method: "POST", body: {} }).then(function () { if (name) viewProfile(name); else { toast("Seguindo."); RC.sug = RC.sug.filter(function (u) { return u.id !== id; }); fillRcol(); Object.keys(POSTS).forEach(function (k) { if (POSTS[k].authorId === id) { POSTS[k].isFollowing = true; repaint(k); } }); } }).catch(onErr);
  }
  document.addEventListener("click", function (e) {
    var t = e.target.closest("[data-go],[data-back],[data-like],[data-save],[data-comments],[data-share],[data-vote],[data-follow],[data-open],[data-story],[data-more],[data-settings],[data-accounts],[data-edit],[data-shareprofile],[data-chat],[data-find],[data-switch],[data-drop],[data-addacc],[data-install],[data-mode],[data-moremenu],[data-ftab],[data-aperiod],[data-ctab],[data-joincom],[data-newcom],[data-mk],[data-listing],[data-addstory],[data-pmenu],[data-pact],[data-repost],[data-newwith],[data-locon],[data-nrefresh],[data-notice],[data-invite],[data-forgot],[data-cvote],[data-golive],[data-stat],[data-umenu],[data-newlisting]");
    if (!t) return;
    var d = t.dataset;
    if (d.go) { if (location.hash === d.go) render(); else location.hash = d.go; return; }
    if (t.hasAttribute("data-back")) { if (history.length > 1 && !/add/.test(location.hash)) history.back(); else location.hash = "#/"; return; }
    if (d.like) { var p = POSTS[d.like]; if (!cur()) { location.hash = "#/login"; return; } api("/api/posts/" + d.like + "/like", { method: "POST", body: {} }).then(function (r) { p.likedByMe = !!r.liked; p.likes = Math.max(0, (p.likes || 0) + (r.liked ? 1 : -1)); repaint(d.like); }).catch(onErr); return; }
    if (d.save) { var q = POSTS[d.save]; api("/api/posts/" + d.save + "/save", { method: "POST", body: {} }).then(function (r) { q.savedByMe = r.saved !== undefined ? !!r.saved : !q.savedByMe; repaint(d.save); toast(q.savedByMe ? "Publicação salva." : "Removida dos salvos."); }).catch(onErr); return; }
    if (d.comments) return openComments(d.comments);
    if (d.share) return shareSheet(d.share);
    if (d.share_old) { var url = new URL("#/post/" + d.share, location.href).href; if (navigator.share) navigator.share({ title: "QINBLY", url: url }).catch(function () { }); else if (navigator.clipboard) navigator.clipboard.writeText(url).then(function () { toast("Link copiado."); }); return; }
    if (d.vote) { var pv = d.vote.split(":"); api("/api/posts/" + pv[0] + "/vote", { method: "POST", body: { optionIndex: Number(pv[1]) } }).then(function () { return api("/api/posts/" + pv[0]); }).then(function (j) { POSTS[pv[0]] = j.post; repaint(pv[0]); }).catch(onErr); return; }
    if (d.follow) return followUser(d.follow, d.profile);
    if (d.open) { location.hash = "#/post/" + d.open; return; }
    if (d.story !== undefined) { var en = (window.__tray || [])[Number(d.story)]; if (en && en.stories && en.stories.length) openStories(en); else storyMenu(); return; }
    if (t.hasAttribute("data-more")) { loadFeed(false); return; }
    if (t.hasAttribute("data-settings")) { location.hash = "#/settings"; return; }
    if (t.hasAttribute("data-accounts")) return accountsSheet();
    if (t.hasAttribute("data-edit")) { location.hash = "#/edit"; return; }
    if (d.shareprofile) { var pu = new URL("#/u/" + d.shareprofile, location.href).href; if (navigator.share) navigator.share({ title: "QINBLY", url: pu }).catch(function () { }); else if (navigator.clipboard) navigator.clipboard.writeText(pu).then(function () { toast("Link copiado."); }); return; }
    if (d.chat) { closeSheet(); location.hash = "#/chat/" + d.chat; return; }
    if (t.hasAttribute("data-find")) return findPeople();
    if (d.switch) { activeId = d.switch; save(); closeSheet(); goto("#/"); return; }
    if (d.drop) { var was = d.drop === activeId; dropAccount(d.drop); closeSheet(); goto(accounts.length ? "#/" : "#/login"); return; }
    if (t.hasAttribute("data-addacc")) { closeSheet(); location.hash = "#/login?add"; return; }
    if (t.hasAttribute("data-install")) { installNow(); return; }
    if (d.mode) { viewLogin(d.mode); return; }
    if (t.hasAttribute("data-moremenu")) return moreMenu();
    if (d.aperiod) { altaPeriod = d.aperiod; drawFeed(); return; }
    if (d.ftab) { feedTab = d.ftab; document.querySelectorAll(".ftabs button").forEach(function (b) { b.classList.toggle("on", b.dataset.ftab === feedTab); }); var stb = el("st"); if (stb) stb.hidden = feedTab !== "Descobrir"; drawFeed(); return; }
    if (d.ctab) { comTab = d.ctab; viewCommunities(); return; }
    if (d.joincom) { api("/api/communities/" + d.joincom + "/join", { method: "POST", body: {} }).then(function () { if (t.hasAttribute("data-stay")) viewCommunity(d.joincom); else viewCommunities(); }).catch(onErr); return; }
    if (t.hasAttribute("data-newcom")) return newCommunity();
    if (d.mk !== undefined) { mkCat = Number(d.mk); viewMarket(); return; }
    if (d.listing) return openListing(d.listing);
    if (t.hasAttribute("data-addstory")) return storyMenu();
    if (d.newwith !== undefined) { var nw = d.newwith.split(":"); NEWPRE = { kind: nw[0] || "text", text: nw.slice(1).join(":") }; location.hash = "#/new"; if (location.hash === "#/new") render(); return; }
    if (t.hasAttribute("data-locon")) { LS.set("qb_loc_on", true); nearbyView(); return; }
    if (t.hasAttribute("data-nrefresh")) { HERE = null; nearbyView(); return; }
    if (d.notice) return noticeSheet(d.notice);
    if (t.hasAttribute("data-invite")) return inviteFriends();
    if (t.hasAttribute("data-forgot")) return forgotSheet();
    if (d.repost) { var rq = POSTS[d.repost]; if (!rq) return; if (!cur()) { location.hash = "#/login"; return; } api("/api/posts/" + d.repost + "/repost", { method: "POST", body: {} }).then(function (r) { rq.repostedByMe = !!r.reposted; rq.reposts = Math.max(0, (rq.reposts || 0) + (r.reposted ? 1 : -1)); repaint(d.repost); toast(r.reposted ? "Repostado." : "Repostagem desfeita."); }).catch(onErr); return; }
    if (d.pmenu) return postMenu(d.pmenu);
    if (d.pact) { var pa = d.pact.split(":"); closeSheet(); return postAction(pa[0], pa[1]); }
    if (d.cvote) { var cv = d.cvote.split(":"); call(HOST + "/api/chat-polls/" + cv[0] + "/vote", { method: "POST", body: { optionIndex: Number(cv[1]) } }).then(function () { toast("Voto registrado."); if (window.__reload) window.__reload(); }).catch(onErr); return; }
    if (t.hasAttribute("data-golive")) { location.hash = "#/golive"; return; }
    if (d.stat) { var pu2 = window.__pu; if (pu2) peopleSheet(pu2.username, d.stat); return; }
    if (t.hasAttribute("data-umenu")) return userMenu();
    if (t.hasAttribute("data-newlisting")) return newListing();
  });

  /* ---------- rotas ---------- */
  function render() {
    clearTimers(); closeSheet(); leaveLive();
    var h = (location.hash || "#/").replace(/^#/, ""), parts = h.split("?")[0].split("/").filter(Boolean);
    if (!cur() && parts[0] !== "u" && parts[0] !== "post") { if (parts[0] !== "login") location.hash = "#/login"; return viewLogin("login"); }
    if (parts[0] === "login") return viewLogin("login");
    var r = parts[0] || "home";
    if (r === "home") return viewFeed();
    if (r === "trending" && parts[1] === "all") return viewTrendingAll();
    if (r === "trending") return viewFeed("Em alta");
    if (r === "search") return viewSearch();
    if (r === "notifications") return viewNotifs();
    if (r === "communities") return viewCommunities();
    if (r === "community" && parts[1]) return viewCommunity(parts[1]);
    if (r === "market") return viewMarket();
    if (r === "saved") return viewSaved();
    if (r === "tag" && parts[1]) return viewTag(decodeURIComponent(parts[1]));
    if (r === "new") return viewNew();
    if (r === "inbox") return viewInbox();
    if (r === "me") return viewProfile("me");
    if (r === "edit") return viewEdit();
    if (r === "u" && parts[1]) return cur() ? viewProfile(decodeURIComponent(parts[1])) : viewLogin("login");
    if (r === "post" && parts[1]) return viewPost(parts[1]);
    if (r === "settings") return viewSettings(parts[1] || "");
    if (r === "live" && parts[1] && parts[2]) return viewLiveWatch(parts[1], decodeURIComponent(parts[2]));
    if (r === "golive") return viewGoLive();
    if (r === "chat" && parts[1]) return viewChat(decodeURIComponent(parts[1]));
    location.hash = "#/";
  }
  window.addEventListener("hashchange", render);
  applyPrefs();
  render();

  /* contadores de mensagens e notificações novas */
  function pollUnread() {
    if (!cur() || document.hidden) return;
    api("/api/inbox").then(function (j) { unread = (j.conversations || []).reduce(function (n, c) { return n + (c.unreadCount || 0); }, 0); paintBadges(); }).catch(function () { });
    api("/api/notifications").then(function (j) { notifN = (j.notifications || []).filter(function (n) { return !n.readAt; }).length; paintBadges(); }).catch(function () { });
  }
  /* Voltou para o site depois de 1 minuto ou mais: busca o feed novo sozinho (sem ficar consultando enquanto usa). */
  var hiddenAt = 0;
  document.addEventListener("visibilitychange", function () {
    if (document.hidden) { hiddenAt = Date.now(); return; }
    if (hiddenAt && Date.now() - hiddenAt >= 60000 && cur() && document.getElementById("fd") && window.scrollY < 120) loadFeed(true);
    hiddenAt = 0;
  });
  setInterval(pollUnread, 30000); setTimeout(pollUnread, 1500);
  setInterval(beat, 60000); setTimeout(beat, 3000);

  if ("serviceWorker" in navigator) { window.addEventListener("load", function () { navigator.serviceWorker.register("sw.js").catch(function () { }); }); }
})();

/* Legenda longa no feed: 4 linhas (com foto/vídeo) ou 6 (só texto) e "… mais" no fim da última; toque abre tudo ("menos" recolhe). */
(function () {
  var LINES = 4;
  function lineH(el) {
    var cs = getComputedStyle(el), l = parseFloat(cs.lineHeight);
    return isNaN(l) ? parseFloat(cs.fontSize) * 1.4 : l;
  }
  function span(cls, txt) { var s = document.createElement("span"); s.className = cls; s.textContent = txt; return s; }
  function renderCut(el, k) {
    var full = el._full, left = k;
    el.textContent = "";
    var nodes = full.childNodes;
    for (var i = 0; i < nodes.length && left > 0; i++) {
      var n = nodes[i], len = n.textContent.length;
      if (len <= left) { el.appendChild(n.cloneNode(true)); left -= len; }
      else { var c = n.cloneNode(true); (c.nodeType === 3 ? c : c.firstChild).textContent = n.textContent.slice(0, left); el.appendChild(c); left = 0; break; }
    }
    var last = el.lastChild;
    if (last && last.nodeType === 3) last.textContent = last.textContent.replace(/\s+$/, "");
    el.appendChild(document.createTextNode("… "));
    el.appendChild(span("cx-more", "mais"));
  }
  function renderFull(el) {
    el.textContent = "";
    var nodes = el._full.childNodes;
    for (var i = 0; i < nodes.length; i++) el.appendChild(nodes[i].cloneNode(true));
    el.appendChild(span("cx-less", "menos"));
  }
  function collapse(el) {
    if (el.getAttribute("data-cx") || el.offsetParent === null) return;
    el.setAttribute("data-cx", "1");
    var limit = lineH(el) * (el.classList.contains("rcap") ? 2 : el.classList.contains("tbg") ? 6 : LINES) + 2;
    if (el.scrollHeight <= limit) return;
    el._full = el.cloneNode(true);
    var lo = 0, hi = el.textContent.length;
    while (lo < hi) {
      var mid = Math.ceil((lo + hi) / 2);
      renderCut(el, mid);
      if (el.scrollHeight <= limit) lo = mid; else hi = mid - 1;
    }
    el._k = lo;
    renderCut(el, lo);
  }
  function scan() { var l = document.querySelectorAll(".post .cap:not([data-cx]), .post .tbg:not([data-cx]), .reel .rcap:not([data-cx])"); for (var i = 0; i < l.length; i++) collapse(l[i]); }
  var raf = 0;
  new MutationObserver(function () { if (!raf) raf = requestAnimationFrame(function () { raf = 0; scan(); }); }).observe(document.documentElement, { childList: true, subtree: true });
  document.addEventListener("click", function (e) {
    var t = e.target && e.target.closest ? e.target.closest(".cx-more,.cx-less") : null;
    if (!t) return;
    e.preventDefault(); e.stopPropagation();
    var el = t.parentNode;
    if (t.className === "cx-more") renderFull(el); else renderCut(el, el._k);
  }, true);
})();

/* Igual ao TikTok: em pé enche a tela; largo/quadrado aparece inteiro, sem zoom, com faixas pretas. */
document.addEventListener("loadedmetadata", function (e) {
  var v = e.target;
  if (!v || v.tagName !== "VIDEO" || !v.closest || !v.videoWidth || !v.videoHeight) return;
  var box = v.closest(".reel");
  if (!box) return;
  var cw = box.clientWidth, ch = box.clientHeight, a = v.videoWidth / v.videoHeight;
  if (!cw || !ch) return;
  var wide = a >= cw / ch, fitW = wide ? cw : ch * a, fillW = wide ? ch * a : cw;
  var w = fillW <= fitW * 1.08 ? fillW : fitW;
  v.classList.add("fit");
  v.style.width = Math.round(w) + "px";
  v.style.height = Math.round(w / a) + "px";
}, true);
