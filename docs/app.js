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
    comment: '<path d="M21 12a8 8 0 0 1-11.6 7.1L4 20l1-4.6A8 8 0 1 1 21 12z"/>',
    send: '<path d="M21 3L3 10.5l7 2.5 2.5 7L21 3z"/><path d="M10 13l11-10"/>',
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
    menu: '<path d="M4 7h16M4 12h16M4 17h16"/>'
  };
  function svg(n) { return '<svg viewBox="0 0 24 24" aria-hidden="true">' + IC[n] + "</svg>"; }
  var VERIFIED = '<svg class="vf" viewBox="0 0 24 24" aria-label="verificado"><path d="M12 2l2.4 2 3.1-.2 1 3 2.7 1.6-.9 3 .9 3-2.7 1.6-1 3-3.1-.2L12 22l-2.4-2-3.1.2-1-3-2.7-1.6.9-3-.9-3 2.7-1.6 1-3 3.1.2z"/><path d="M8.5 12l2.5 2.5 4.5-5" stroke="#fff" fill="none" stroke-width="2"/></svg>';
  function avatar(url, name, cls) {
    var st = safeUrl(url) ? ' style="background-image:url(\'' + safeUrl(url) + '\')"' : "";
    var ini = safeUrl(url) ? "" : esc((name || "?").trim().charAt(0).toUpperCase());
    return '<span class="av ' + (cls || "") + '"' + st + ">" + ini + "</span>";
  }
  var TEXT_BGS = [["#2F68E0", "#1F4FB0"], ["#E0457B", "#8A2A5A"], ["#1D9E75", "#0F6E56"], ["#EF9F27", "#B45309"], ["#378ADD", "#185FA5"], ["#111111", "#2B2B2B"]];
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
    return '<aside class="side"><button class="logo" data-go="#/">QINBLY</button>' +
      it("home", "Início", "home", "#/") + it("search", "Buscar", "search", "#/search") + it("trending", "Em alta", "flame", "#/trending") +
      it("inbox", "Mensagens", "msg", "#/inbox", badge("inbox", unread)) + it("notif", "Notificações", "bell", "#/notifications", badge("notif", notifN)) +
      it("communities", "Comunidades", "groups", "#/communities") + it("market", "Mercado", "bag", "#/market") + it("saved", "Salvos", "save", "#/saved") +
      it("new", "Criar", "plus", "#/new") +
      '<button class="si ' + (tab === "me" ? "on" : "") + '" data-go="#/me" aria-label="Perfil">' + (me ? avatar(me.avatarUrl, me.displayName || me.username, "xs") : svg("user")) + '<span class="lbl">Perfil</span></button>' +
      '<button class="si more" data-moremenu aria-label="Mais">' + svg("menu") + '<span class="lbl">Mais</span></button></aside>';
  }
  function bottomNav(tab) {
    function b(id, label, icon, href) { return '<button data-go="' + href + '" class="' + (tab === id ? "on" : "") + '" aria-label="' + label + '">' + svg(icon) + label + (id === "inbox" ? badge("inbox", unread) : "") + "</button>"; }
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
      '<div class="ph"><a href="#/u/' + esc(a.username) + '" class="ring">' + avatar(a.avatarUrl, a.displayName || a.username, "s") + "</a>" +
      '<div class="who"><b><a href="#/u/' + esc(a.username) + '">' + esc(a.displayName || a.username) + "</a>" + (a.verified ? VERIFIED : "") + "</b><small>@" + esc(a.username) + " · " + ago(p.createdAt) + (p.visibility === "private" ? " · privado" : "") + "</small></div>" +
      (!mine && !p.isFollowing && me ? '<button class="sb" style="flex:none;width:76px" data-follow="' + p.authorId + '">Seguir</button>' : "") + "</div>" +
      (cap ? '<div class="cap">' + linkify(cap) + "</div>" : "") + media +
      '<div class="acts"><button class="act ' + (p.likedByMe ? "on" : "") + '" data-like="' + p.id + '" aria-label="Curtir">' + svg("heart") + (p.likes || "") + "</button>" +
      '<button class="act" data-comments="' + p.id + '" aria-label="Comentários">' + svg("comment") + (p.comments || "") + "</button>" +
      '<button class="act" data-share="' + p.id + '" aria-label="Compartilhar">' + svg("send") + "</button><span class=\"sp\"></span>" +
      '<button class="act sv ' + (p.savedByMe ? "on" : "") + '" data-save="' + p.id + '" aria-label="Salvar">' + svg("save") + "</button></div></article>";
  }
  function repaint(id) {
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
    var s = sheet("Comentários", spin(),
      '<form class="cin" id="cf"><input id="ci" maxlength="2000" placeholder="Adicione um comentário…" autocomplete="off"><button type="submit">Enviar</button></form>');
    var box = s.querySelector(".body");
    function draw(list) {
      box.innerHTML = list.length ? list.map(function (c) {
        var a = c.author || {};
        return '<div class="cm"><a href="#/u/' + esc(a.username) + '">' + avatar(a.avatarUrl, a.displayName || a.username, "s") + '</a><div class="b"><b>' + esc(a.username) + "</b>" + linkify(c.body) + "<small>" + ago(c.createdAt) + "</small></div></div>";
      }).join("") : '<div class="empty">Nenhum comentário ainda. Seja o primeiro.</div>';
      box.scrollTop = box.scrollHeight;
    }
    var list = [];
    api("/api/posts/" + id + "/comments").then(function (j) { list = j.comments || []; draw(list); }).catch(function (e) { box.innerHTML = '<div class="empty">' + esc(e.message) + "</div>"; });
    s.querySelector("#cf").addEventListener("submit", function (e) {
      e.preventDefault();
      var inp = s.querySelector("#ci"), t = inp.value.trim(); if (!t) return;
      if (!cur()) { location.hash = "#/login"; return; }
      inp.disabled = true;
      api("/api/posts/" + id + "/comments", { method: "POST", body: { body: t } }).then(function (j) {
        list.push(j.comment); draw(list); inp.value = ""; if (POSTS[id]) { POSTS[id].comments = (POSTS[id].comments || 0) + 1; repaint(id); }
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
    var items = entry.stories || [], i = 0, timer = null, v = document.createElement("div");
    v.className = "viewer";
    document.body.appendChild(v);
    function end() { clearTimeout(timer); v.remove(); }
    function show() {
      clearTimeout(timer);
      if (i >= items.length) return end();
      var s = items[i], cp = parseCap(s.caption), isText = cp.bg >= 0 && !safeUrl(s.mediaUrl);
      var bars = items.map(function (_, k) { return '<i><b style="width:' + (k < i ? 100 : 0) + '%"></b></i>'; }).join("");
      var media = isText ? '<div class="tx" style="background:' + bgCss(cp.bg || 1) + '">' + esc(cp.text) + "</div>"
        : s.mediaType === "video" ? '<video src="' + safeUrl(s.mediaUrl) + '" autoplay playsinline></video>' : '<img src="' + safeUrl(s.mediaUrl) + '" alt="">';
      v.innerHTML = '<div class="bar">' + bars + '</div><div class="who">' + avatar(entry.avatarUrl, entry.displayName || entry.username, "s") + "<b>" + esc(entry.username) + "</b><small>" + ago(s.createdAt) + '</small><span class="sp" style="flex:1"></span><button class="ib" data-x aria-label="Fechar">' + svg("x") + '</button></div><div class="stage">' + media + '<div class="zone" style="left:0" data-prev></div><div class="zone" style="right:0" data-next></div></div>' + (!isText && cp.text ? '<div class="cap2">' + esc(cp.text) + "</div>" : "");
      var bar = v.querySelectorAll(".bar i b")[i];
      var vid = v.querySelector("video");
      function next() { i++; show(); }
      if (vid) { vid.addEventListener("ended", next); } else {
        var t0 = Date.now(); (function tick() { var p = Math.min(100, (Date.now() - t0) / 50); if (bar) bar.style.width = p + "%"; if (p < 100) timer = setTimeout(tick, 50); else next(); })();
      }
    }
    v.addEventListener("click", function (e) {
      var t = e.target.closest("[data-x],[data-prev],[data-next]"); if (!t) return;
      if (t.hasAttribute("data-x")) return end();
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
      '<div class="alt">' + (reg ? 'Já tem conta? <button data-mode="login">Entrar</button>' : 'Não tem conta? <button data-mode="register">Criar conta</button>') + "</div>" +
      (add && accounts.length ? '<div class="alt"><button data-back>Cancelar</button></div>' : "") +
      '<div class="alt" style="font-size:12px">Entrou no app só com o Google? Defina uma senha em Configurações, no app, para usar aqui.</div>' +
      '<div class="alt" style="font-size:12px"><a class="link" href="baixar.html">Baixar o app para Android</a></div>' + (ios ? '<div class="alt" style="font-size:12px">Para instalar no iPhone: toque em compartilhar e depois em “Adicionar à Tela de Início”.</div>' : "") +
      "</main>";
    initGoogle();
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
  var FTABS = ["Descobrir", "Seguindo", "Em alta", "Vídeos"];
  function hot(p) { return (p.likes || 0) * 2 + (p.comments || 0) * 3 + (p.reposts || 0) * 3; }
  function feedList() {
    var l = FEED;
    if (feedTab === "Seguindo") l = l.filter(function (p) { return p.isFollowing; });
    else if (feedTab === "Vídeos") l = l.filter(function (p) { return p.mediaType === "video"; });
    else if (feedTab === "Em alta") { var lim = Date.now() - 7 * 86400000; l = l.filter(function (p) { return Date.parse(p.createdAt) > lim; }).sort(function (x, y) { return hot(y) - hot(x); }).slice(0, 30); }
    return l;
  }
  function viewFeed(tab) {
    if (tab) feedTab = tab;
    var mob = '<header class="top mob"><h1 class="brand">QINBLY</h1><button class="ib" data-go="#/search" aria-label="Buscar">' + svg("search") + '</button><button class="ib" data-go="#/notifications" aria-label="Notificações">' + svg("bell") + badge("notif", notifN) + '</button><button class="ib" data-moremenu aria-label="Menu">' + svg("menu") + "</button></header>";
    setView(mob + '<div id="st"></div><div class="ftabs">' + FTABS.map(function (t) { return '<button data-ftab="' + t + '" class="' + (t === feedTab ? "on" : "") + '">' + t + "</button>"; }).join("") + '</div><div id="fd">' + spin() + "</div>", tab ? "trending" : "home");
    loadStories();
    if (FEED.length) drawFeed();
    loadFeed(true);
  }
  function loadStories() {
    api("/api/stories").then(function (j) {
      var tray = (j.tray || []).filter(function (t) { return t.mine || (t.stories && t.stories.length); });
      window.__tray = tray;
      var el = document.getElementById("st"); if (!el) return;
      el.innerHTML = '<div class="stories">' + tray.map(function (t, i) {
        return '<div class="story"><button class="ring ' + (t.hasUnseen ? "new" : "") + '" data-story="' + i + '" aria-label="Ver story">' + avatar(t.avatarUrl, t.displayName || t.username) + "</button>" +
          (t.mine ? '<button class="plus" data-addstory aria-label="Criar story">+</button>' : "") + "<span>" + (t.mine ? "Seu story" : esc(t.username)) + "</span></div>";
      }).join("") + "</div>";
    }).catch(function () { /* stories são opcionais */ });
  }
  function drawFeed() {
    var fd = document.getElementById("fd"); if (!fd) return;
    var l = feedList();
    var empty = { "Descobrir": "Ainda não há publicações. Toque em Criar e publique a primeira.", "Seguindo": "Siga pessoas para ver as publicações delas aqui.", "Em alta": "Nada em alta nos últimos 7 dias.", "Vídeos": "Ainda não há vídeos." }[feedTab];
    fd.innerHTML = l.length ? l.map(postHTML).join("") : '<div class="empty">' + empty + "</div>";
    if (feedMore) fd.insertAdjacentHTML("beforeend", '<button class="more" data-more>Carregar mais</button>');
  }
  function loadFeed(first) {
    api("/api/posts?limit=40" + (first || !feedBefore ? "" : "&before=" + encodeURIComponent(feedBefore))).then(function (j) {
      var ps = j.posts || [];
      FEED = first ? ps : FEED.concat(ps);
      if (ps.length) feedBefore = ps[ps.length - 1].createdAt;
      feedMore = ps.length >= 40;
      drawFeed();
    }).catch(function (e) { var fd = document.getElementById("fd"); if (first && !FEED.length && fd) fd.innerHTML = '<div class="empty">' + esc(e.message) + "</div>"; else onErr(e); });
  }

  function viewPost(id) {
    setView(top("Publicação", { back: true }) + '<div id="pp">' + spin() + "</div>", "", "noNav");
    api("/api/posts/" + id).then(function (j) { document.getElementById("pp").innerHTML = postHTML(j.post); })
      .catch(function (e) { document.getElementById("pp").innerHTML = '<div class="empty">' + esc(e.message) + "</div>"; });
  }

  function viewProfile(name) {
    var me = cur(), isMeRoute = name === "me";
    if (isMeRoute) name = me.username;
    setView(top("@" + esc(name), { back: !isMeRoute, right: isMeRoute ? '<button class="ib" data-settings aria-label="Configurações">' + svg("gear") + "</button>" : "" }) + '<div id="pf">' + spin() + "</div>", isMeRoute ? "me" : "");
    api("/api/users/" + encodeURIComponent(name)).then(function (j) {
      var u = j.user, posts = j.posts || [], own = me && u.id === me.id;
      if (own) { me.displayName = u.displayName; me.avatarUrl = u.avatarUrl; me.username = u.username; save(); }
      var grid = posts.map(function (p) {
        POSTS[p.id] = p;
        if (p.mediaType === "text" || p.mediaType === "poll") return '<button class="tx" data-open="' + p.id + '" style="' + (p.textBg ? "background:" + bgCss(p.textBg) : "") + '">' + esc((p.caption || "").slice(0, 60)) + "</button>";
        var img = p.mediaType === "video" ? "" : (p.mediaUrl ? "background-image:url('" + safeUrl(p.mediaUrl) + "')" : "");
        return '<button data-open="' + p.id + '" style="' + img + '">' + (p.mediaType === "video" ? "▶" : "") + "</button>";
      }).join("");
      document.getElementById("pf").innerHTML =
        '<div class="ptop"><span class="ring">' + avatar(u.avatarUrl, u.displayName || u.username, "l") + '</span><div class="pname"><h2>' + esc(u.displayName || u.username) + (u.verified ? VERIFIED : "") + "</h2>" +
        (own ? '<button class="at" data-accounts>@' + esc(u.username) + svg("chev") + "</button>" : '<span class="at">@' + esc(u.username) + "</span>") + "</div></div>" +
        (u.bio ? '<div class="bio">' + linkify(u.bio) + "</div>" : "") +
        '<div class="stats"><div><b>' + (u.posts_count || 0) + "</b>Publicações</div><div><b>" + (u.followers || 0) + "</b>Seguidores</div><div><b>" + (u.following || 0) + "</b>Seguindo</div></div>" +
        '<div class="btns">' + (own ? '<button class="sb" data-edit>Editar perfil</button><button class="sb" data-shareprofile="' + esc(u.username) + '">Compartilhar perfil</button>'
          : '<button class="sb ' + (u.isFollowing ? "" : "pri") + '" data-follow="' + u.id + '" data-profile="' + esc(u.username) + '">' + (u.isFollowing ? "Seguindo" : "Seguir") + '</button><button class="sb" data-chat="' + esc(u.username) + '">Mensagem</button>') + "</div>" +
        '<div class="tabs"><button class="on" aria-label="Publicações">' + svg("grid") + "</button></div>" +
        (grid ? '<div class="grid">' + grid + "</div>" : '<div class="empty">Nenhuma publicação ainda.</div>');
    }).catch(function (e) { document.getElementById("pf").innerHTML = '<div class="empty">' + esc(e.message) + "</div>"; });
  }

  function viewEdit() {
    var me = cur();
    setView(top("Editar perfil", { back: true }) + '<div class="form" id="ef">' + spin() + "</div>", "", "noNav");
    api("/api/auth/me").then(function (j) {
      var u = j.user;
      document.getElementById("ef").innerHTML =
        '<div style="text-align:center"><span class="ring">' + avatar(u.avatarUrl, u.displayName || u.username, "l") + '</span><br><label class="link" style="cursor:pointer;display:inline-block;padding:10px;font-weight:600">Trocar foto<input id="af" type="file" accept="image/*" hidden></label></div>' +
        '<label class="fld">Nome<input class="inp" id="dn" maxlength="60" value="' + esc(u.displayName) + '"></label>' +
        '<label class="fld">Nome de usuário<input class="inp" id="un" maxlength="30" autocapitalize="none" value="' + esc(u.username) + '"></label>' +
        '<label class="fld">Bio<textarea class="inp" id="bi" maxlength="500">' + esc(u.bio) + '</textarea></label><div class="err" id="er"></div><button class="btn" id="sv">Salvar</button>';
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
        api("/api/users/me", { method: "PATCH", body: { displayName: ef.querySelector("#dn").value, username: ef.querySelector("#un").value, bio: ef.querySelector("#bi").value } })
          .then(function (r) { me.username = r.user.username; me.displayName = r.user.displayName; save(); toast("Perfil salvo."); location.hash = "#/me"; })
          .catch(function (e) { ef.querySelector("#er").textContent = e.message; btn.disabled = false; });
      });
    }).catch(onErr);
  }

  function viewNew() {
    var kind = "text", bg = 0, files = [], vis = "public";
    setView(top("Nova publicação") + '<div class="form" id="nf"></div>', "new");
    var nf = document.getElementById("nf");
    function draw() {
      nf.innerHTML =
        '<div class="seg"><button data-k="text" class="' + (kind === "text" ? "on" : "") + '">Texto</button><button data-k="image" class="' + (kind === "image" ? "on" : "") + '">Foto</button><button data-k="video" class="' + (kind === "video" ? "on" : "") + '">Vídeo</button></div>' +
        (kind === "text" ? '<div class="bgs"><button data-bg="0" class="' + (bg === 0 ? "on" : "") + '" style="background:var(--surface);border-color:' + (bg === 0 ? "#fff" : "var(--line)") + '" aria-label="Sem fundo"></button>' + TEXT_BGS.map(function (_, i) { return '<button data-bg="' + (i + 1) + '" class="' + (bg === i + 1 ? "on" : "") + '" style="background:' + bgCss(i + 1) + '" aria-label="Fundo ' + (i + 1) + '"></button>'; }).join("") + "</div>" : "") +
        (kind !== "text" ? '<label class="btn ghost" style="cursor:pointer">' + (files.length ? "Trocar " + (kind === "image" ? "fotos" : "vídeo") : "Escolher " + (kind === "image" ? "fotos" : "vídeo")) + '<input id="fi" type="file" hidden accept="' + (kind === "image" ? "image/*" : "video/*") + '" ' + (kind === "image" ? "multiple" : "") + "></label>" : "") +
        (files.length ? '<div class="pick">' + files.map(function (f, i) { return '<div class="th" style="' + (kind === "image" ? "background-image:url(" + URL.createObjectURL(f) + ")" : "") + '">' + (kind === "video" ? '<span style="position:absolute;inset:0;display:flex;align-items:center;justify-content:center">▶</span>' : "") + '<button data-rm="' + i + '" aria-label="Remover">×</button></div>'; }).join("") + "</div>" : "") +
        '<textarea class="inp" id="cp" maxlength="5000" placeholder="' + (kind === "text" ? "Escreva algo…" : "Escreva uma legenda…") + '"></textarea>' +
        '<div class="seg"><button data-v="public" class="' + (vis === "public" ? "on" : "") + '">Público</button><button data-v="private" class="' + (vis === "private" ? "on" : "") + '">Só eu</button></div>' +
        '<div class="err" id="er"></div><button class="btn" id="pb">Publicar</button>';
      var cp = nf.querySelector("#cp"); cp.value = draw.cap || ""; cp.addEventListener("input", function () { draw.cap = cp.value; });
      var fi = nf.querySelector("#fi");
      if (fi) fi.addEventListener("change", function (e) {
        var list = Array.prototype.slice.call(e.target.files).slice(0, 10);
        var okTypes = kind === "image" ? IMG_OK : VID_OK;
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
      else if (t.id === "pb") publish(t);
    });
    function publish(btn) {
      var er = nf.querySelector("#er"), caption = nf.querySelector("#cp").value.trim(); er.textContent = "";
      if (kind === "text" && !caption) { er.textContent = "Escreva algo para publicar."; return; }
      if (kind !== "text" && !files.length) { er.textContent = kind === "image" ? "Escolha pelo menos uma foto." : "Escolha um vídeo."; return; }
      btn.disabled = true; btn.textContent = "Publicando…";
      var chain = Promise.resolve([]);
      if (kind !== "text") {
        chain = files.reduce(function (acc, f) {
          return acc.then(function (ids) { return (kind === "image" ? shrink(f) : Promise.resolve(f)).then(function (b) { return upload(b, "post_media"); }).then(function (id) { return ids.concat(id); }); });
        }, Promise.resolve([]));
      }
      chain.then(function (ids) {
        var body = { caption: caption, mediaType: kind, visibility: vis };
        if (kind === "text" && bg) body.textBg = bg;
        if (ids.length) { body.mediaFileId = ids[0]; if (ids.length > 1) body.mediaFileIds = ids; }
        return api("/api/posts", { method: "POST", body: body });
      }).then(function () { draw.cap = ""; feedBefore = null; toast("Publicado."); location.hash = "#/"; })
        .catch(function (x) { er.textContent = x.message; btn.disabled = false; btn.textContent = "Publicar"; });
    }
    draw.cap = ""; draw();
  }

  function viewInbox() {
    setView(top("Mensagens", { right: '<button class="ib" data-find aria-label="Nova conversa">' + svg("search") + "</button>" }) + '<div id="ib">' + spin() + "</div>", "inbox");
    api("/api/inbox").then(function (j) {
      var cs = j.conversations || [];
      unread = cs.reduce(function (s, c) { return s + (c.unreadCount || 0); }, 0);
      document.getElementById("ib").innerHTML = cs.length ? cs.map(function (c) {
        return '<button class="row" data-chat="' + esc(c.username) + '">' + avatar(c.avatarUrl, c.displayName || c.username) + '<span class="t"><b>' + esc(c.displayName || c.username) + "</b><small>" + esc(c.body) + '</small></span><span class="meta">' + ago(c.createdAt) + "</span>" + (c.unreadCount ? '<span class="dot">' + c.unreadCount + "</span>" : "") + "</button>";
      }).join("") : '<div class="empty">Nenhuma conversa ainda. Toque na lupa para procurar alguém.</div>';
      paintBadges();
    }).catch(onErr);
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

  function viewChat(name) {
    var me = cur(), last = "";
    setView(top("@" + esc(name), { back: true }) + '<div class="chat" id="ch">' + spin() + "</div>" +
      '<div class="composer"><form class="in" id="cf"><textarea id="mi" rows="1" maxlength="10000" placeholder="Mensagem…"></textarea><button class="send" type="submit">Enviar</button></form></div>', "inbox", "noNav chatv");
    function show(list) {
      var sig = list.map(function (m) { return m.id; }).join();
      if (sig === last) return; last = sig;
      var ch = document.getElementById("ch"); if (!ch) return;
      var stick = (window.innerHeight + window.scrollY) >= document.body.scrollHeight - 160 || !ch.dataset.done;
      ch.dataset.done = "1";
      ch.innerHTML = list.length ? list.map(function (m) {
        var txt = typeof m.body === "string" && !/^qinbly:/.test(m.body) ? esc(m.body) : "<i>Mensagem especial (foto, áudio, figurinha ou enquete). Abra no app para ver.</i>";
        return '<div class="msg ' + (m.senderId === me.id ? "me" : "") + '">' + txt + "<small>" + new Date(m.createdAt).toLocaleTimeString("pt-BR", { hour: "2-digit", minute: "2-digit" }) + "</small></div>";
      }).join("") : '<div class="empty">Diga olá para @' + esc(name) + ".</div>";
      if (stick) window.scrollTo(0, document.body.scrollHeight);
    }
    function load() { api("/api/messages/" + encodeURIComponent(name)).then(function (j) { show(j.messages || []); }).catch(function (e) { if (e.expired) onErr(e); }); }
    load(); timers.push(setInterval(load, 5000));
    document.getElementById("cf").addEventListener("submit", function (e) {
      e.preventDefault();
      var inp = document.getElementById("mi"), t = inp.value.trim(); if (!t) return;
      inp.value = "";
      api("/api/messages/" + encodeURIComponent(name), { method: "POST", body: { body: t } }).then(load).catch(function (x) { inp.value = t; onErr(x); });
    });
  }

  function accountsSheet() {
    var s = sheet("Suas contas", accounts.map(function (a) {
      return '<div class="row" style="padding:0 16px 0 0"><button class="row" data-switch="' + a.id + '">' + avatar(a.avatarUrl, a.displayName || a.username) + '<span class="t"><b>' + esc(a.displayName || a.username) + "</b><small>@" + esc(a.username) + "</small></span>" + (a.id === activeId ? '<span class="link">●</span>' : "") + '</button><button class="link" data-drop="' + a.id + '" style="font-size:12px;color:var(--muted)">Sair</button></div>';
    }).join("") + '<button class="row" data-addacc><span class="av">+</span><span class="t"><b>Adicionar outra conta</b></span></button>');
    return s;
  }
  function settingsSheet() {
    sheet("Configurações",
      '<a class="row" href="baixar.html"><span class="t"><b>Baixar o app para Android</b><small>Instalar o QINBLY no celular</small></span></a>' +
      '<button class="row" data-install><span class="t"><b>Instalar no iPhone</b><small>Como adicionar à Tela de Início</small></span></button>' +
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
  function viewNotifs() {
    setView(top("Notificações") + '<div id="nt">' + spin() + "</div>", "notif");
    api("/api/notifications").then(function (j) {
      var l = j.notifications || [];
      document.getElementById("nt").innerHTML = l.length ? l.map(function (n) {
        var a = n.actor || {}, href = /^(like|comment|mention|mention_comment|tag|reply)$/.test(n.kind) && n.entityId ? "#/post/" + n.entityId : n.kind === "message" ? "#/chat/" + esc(a.username) : a.username ? "#/u/" + esc(a.username) : "#/";
        return '<a class="row" href="' + href + '">' + avatar(a.avatarUrl, a.displayName || a.username) + '<span class="t"><b style="font-weight:400">' + (a.username ? "<b style='font-weight:600'>" + esc(a.username) + "</b> " : "") + esc(NOTIF_TEXT[n.kind] || "tem uma novidade para você") + "</b><small>" + ago(n.createdAt) + "</small></span>" + (n.readAt ? "" : '<span class="dot" style="min-width:10px;height:10px;padding:0"></span>') + "</a>";
      }).join("") : '<div class="empty">Nenhuma notificação ainda.</div>';
      if (l.some(function (n) { return !n.readAt; })) api("/api/notifications/read", { method: "POST", body: {} }).then(function () { notifN = 0; paintBadges(); }).catch(function () { });
    }).catch(function (e) { document.getElementById("nt").innerHTML = '<div class="empty">' + esc(e.message) + "</div>"; });
  }

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
    var me = cur(), last = "";
    setView(top("Comunidade", { back: true }) + '<div id="ch0">' + spin() + '</div><div class="chat" id="ch"></div><div class="composer" id="cc" hidden><form class="in" id="cf"><textarea id="mi" rows="1" maxlength="10000" placeholder="Escreva para a comunidade…"></textarea><button class="send" type="submit">Enviar</button></form></div>', "communities", "noNav chatv");
    function load() {
      api("/api/communities/" + id).then(function (j) {
        var c = j.community, h = document.getElementById("ch0"); if (!h) return;
        h.innerHTML = '<div class="ptop"><span class="av l" style="font-size:32px">' + esc(c.emoji) + '</span><div class="pname"><h2>' + esc(c.name) + "</h2><span class=\"at\">" + c.members + " membro" + (c.members === 1 ? "" : "s") + " · criada por @" + esc(c.ownerUsername) + "</span></div>" + (c.isOwner ? "" : '<button class="sb ' + (c.joined ? "" : "pri") + '" style="flex:none;width:84px" data-joincom="' + c.id + '" data-stay>' + (c.joined ? "Sair" : "Entrar") + "</button>") + "</div>" + (c.description ? '<div class="bio">' + linkify(c.description) + "</div>" : "");
        document.getElementById("cc").hidden = !c.joined;
        var sig = (j.posts || []).map(function (m) { return m.id; }).join(); if (sig === last) return; last = sig;
        var ch = document.getElementById("ch"), stick = (window.innerHeight + window.scrollY) >= document.body.scrollHeight - 160 || !ch.dataset.done; ch.dataset.done = "1";
        ch.innerHTML = (j.posts || []).length ? j.posts.map(function (m) {
          var a = m.author || {}, txt = typeof m.body === "string" && !/^qinbly:/.test(m.body) ? linkify(m.body) : "<i>Mensagem especial. Abra no app para ver.</i>";
          return '<div class="msg ' + (m.mine ? "me" : "") + '">' + (m.mine ? "" : "<b style='font-size:12px'>" + esc(a.username) + "</b><br>") + txt + "<small>" + ago(m.createdAt) + "</small></div>";
        }).join("") : '<div class="empty">Ninguém escreveu ainda.</div>';
        if (stick) window.scrollTo(0, document.body.scrollHeight);
      }).catch(function (e) { var h = document.getElementById("ch0"); if (h) h.innerHTML = '<div class="empty">' + esc(e.message) + "</div>"; });
    }
    load(); timers.push(setInterval(load, 6000));
    document.getElementById("cf").addEventListener("submit", function (e) {
      e.preventDefault();
      var inp = document.getElementById("mi"), t = inp.value.trim(); if (!t) return; inp.value = "";
      api("/api/communities/" + id + "/posts", { method: "POST", body: { body: t } }).then(load).catch(function (x) { inp.value = t; onErr(x); });
    });
  }

  /* ---------- mercado ---------- */
  var MK_CATS = [["Tudo", ""], ["Vendas", "venda"], ["Doações", "doacao"], ["Achados", "achado,perdido"], ["Eventos", "evento"], ["Serviços", "servico"], ["Avisos", "aviso"]];
  var MK_LABEL = { venda: "Venda", doacao: "Doação", achado: "Achado", perdido: "Perdido", evento: "Evento", servico: "Serviço", aviso: "Aviso" };
  var mkCat = 0, mkCity = LS.get("qb_city", "");
  var OLD_PUB = "https://jqomgrdlocdqaarfqkay.supabase.co/storage/v1/object/public/post-media/";
  function photoUrl(p) { if (!p) return ""; if (p.indexOf(OLD_PUB) === 0) p = p.slice(OLD_PUB.length); return /^https?:/.test(p) ? p : HOST + "/storage/v1/object/public/post-media/" + p; }
  var MK = {};
  function viewMarket() {
    setView(top("Mercado") + '<div class="chips">' + MK_CATS.map(function (c, i) { return '<button data-mk="' + i + '" class="' + (i === mkCat ? "on" : "") + '">' + c[0] + "</button>"; }).join("") + '</div><div style="padding:0 16px 8px"><input class="inp" id="city" placeholder="Filtrar por cidade (opcional)" value="' + esc(mkCity) + '"></div><div id="ml">' + spin() + "</div>", "market");
    var ci = document.getElementById("city"), t;
    ci.addEventListener("input", function () { clearTimeout(t); t = setTimeout(function () { mkCity = ci.value.trim(); LS.set("qb_city", mkCity); load(); }, 400); });
    function load() {
      var kinds = MK_CATS[mkCat][1], q = "/rest/v1/marketplace_listings?select=id,user_id,kind,title,description,price,extra,photos,city,created_at,profiles!marketplace_listings_user_id_fkey(username,display_name)&order=created_at.desc&limit=60";
      if (kinds) q += "&kind=in.(" + kinds + ")";
      if (mkCity) q += "&city=ilike.*" + encodeURIComponent(mkCity.replace(/[,()*]/g, " ")) + "*";
      call(HOST + q).then(function (l) {
        var ml = document.getElementById("ml"); if (!ml) return;
        if (!Array.isArray(l)) l = [];
        l.forEach(function (x) { MK[x.id] = x; });
        ml.innerHTML = l.length ? l.map(function (x) {
          var ph = (x.photos || [])[0], pr = x.price != null && x.price !== "" ? Number(x.price).toLocaleString("pt-BR", { style: "currency", currency: "BRL" }) : "";
          return '<button class="row" data-listing="' + x.id + '"><span class="av" style="width:64px;height:64px;border-radius:10px;' + (ph ? "background-image:url('" + safeUrl(photoUrl(ph)) + "')" : "") + '"></span><span class="t"><b>' + esc(x.title) + "</b><small>" + (pr ? pr + " · " : "") + esc(MK_LABEL[x.kind] || "Aviso") + (x.city ? " · " + esc(x.city) : "") + " · " + ago(x.created_at) + "</small></span></button>";
        }).join("") : '<div class="empty">Nada por aqui' + (mkCity ? " em " + esc(mkCity) : "") + " ainda.</div>";
      }).catch(function (e) { var ml = document.getElementById("ml"); if (ml) ml.innerHTML = '<div class="empty">' + esc(e.message) + "</div>"; });
    }
    load();
  }
  function openListing(id) {
    var x = MK[id]; if (!x) return;
    var p = x.profiles || {}, pr = x.price != null && x.price !== "" ? Number(x.price).toLocaleString("pt-BR", { style: "currency", currency: "BRL" }) : "";
    sheet(x.title, '<div class="gal">' + (x.photos || []).map(function (u) { return '<img src="' + safeUrl(photoUrl(u)) + '" alt="">'; }).join("") + '</div><div style="padding:12px 16px"><b style="font-size:20px">' + pr + "</b><div style='color:var(--muted);font-size:12px;margin:4px 0 8px'>" + esc(MK_LABEL[x.kind] || "Aviso") + (x.city ? " · " + esc(x.city) : "") + "</div>" + (x.description ? "<div class='cap' style='padding:0'>" + linkify(x.description) + "</div>" : "") + (x.extra ? "<div style='color:var(--muted);font-size:14px;margin-top:8px'>" + esc(x.extra) + "</div>" : "") + "</div>" +
      (p.username ? '<a class="row" href="#/u/' + esc(p.username) + '"><span class="t"><b>' + esc(p.display_name || p.username) + "</b><small>@" + esc(p.username) + '</small></span></a><div style="padding:8px 16px 16px"><button class="btn" data-chat="' + esc(p.username) + '">Falar com o vendedor</button></div>' : ""));
  }

  /* ---------- mais, criar story ---------- */
  function moreMenu() {
    sheet("Mais",
      '<button class="row" data-accounts><span class="t"><b>Trocar de conta</b><small>@' + esc(cur() ? cur().username : "") + "</small></span></button>" +
      '<a class="row" href="#/saved"><span class="t"><b>Salvos</b></span></a>' +
      '<a class="row" href="baixar.html"><span class="t"><b>Baixar o app para Android</b><small>Instalar o QINBLY no celular</small></span></a>' +
      '<button class="row" data-install><span class="t"><b>Instalar no iPhone</b><small>Como adicionar à Tela de Início</small></span></button>' +
      '<a class="row" href="privacidade.html"><span class="t"><b>Privacidade</b></span></a><a class="row" href="termos.html"><span class="t"><b>Termos de uso</b></span></a>' +
      '<button class="row" data-drop="' + activeId + '"><span class="t"><b style="color:var(--danger)">Sair desta conta</b></span></button>');
  }
  function addStory() {
    var f = document.createElement("input"); f.type = "file"; f.accept = "image/*,video/*";
    f.addEventListener("change", function () {
      var file = f.files[0]; if (!file) return;
      var isVid = file.type.indexOf("video/") === 0;
      if ((isVid ? VID_OK : IMG_OK).indexOf(file.type) < 0) return toast("Esse tipo de arquivo não é aceito.");
      toast("Enviando story…");
      (isVid ? Promise.resolve(file) : shrink(file)).then(function (b) { return upload(b, "story_media"); })
        .then(function (path) { return api("/api/stories", { method: "POST", body: { mediaFileId: path, mediaType: isVid ? "video" : "image" } }); })
        .then(function () { toast("Story publicado por 24 horas."); loadStories(); }).catch(onErr);
    });
    f.click();
  }

  /* ---------- cliques (uma escuta só para o app inteiro) ---------- */
  function toggleFollow(id, name) {
    api("/api/users/" + id + "/follow", { method: "POST", body: {} }).then(function () { if (name) viewProfile(name); else { toast("Seguindo."); RC.sug = RC.sug.filter(function (u) { return u.id !== id; }); fillRcol(); Object.keys(POSTS).forEach(function (k) { if (POSTS[k].authorId === id) { POSTS[k].isFollowing = true; repaint(k); } }); } }).catch(onErr);
  }
  document.addEventListener("click", function (e) {
    var t = e.target.closest("[data-go],[data-back],[data-like],[data-save],[data-comments],[data-share],[data-vote],[data-follow],[data-open],[data-story],[data-more],[data-settings],[data-accounts],[data-edit],[data-shareprofile],[data-chat],[data-find],[data-switch],[data-drop],[data-addacc],[data-install],[data-mode],[data-moremenu],[data-ftab],[data-ctab],[data-joincom],[data-newcom],[data-mk],[data-listing],[data-addstory]");
    if (!t) return;
    var d = t.dataset;
    if (d.go) { if (location.hash === d.go) render(); else location.hash = d.go; return; }
    if (t.hasAttribute("data-back")) { if (history.length > 1 && !/add/.test(location.hash)) history.back(); else location.hash = "#/"; return; }
    if (d.like) { var p = POSTS[d.like]; if (!cur()) { location.hash = "#/login"; return; } api("/api/posts/" + d.like + "/like", { method: "POST", body: {} }).then(function (r) { p.likedByMe = !!r.liked; p.likes = Math.max(0, (p.likes || 0) + (r.liked ? 1 : -1)); repaint(d.like); }).catch(onErr); return; }
    if (d.save) { var q = POSTS[d.save]; api("/api/posts/" + d.save + "/save", { method: "POST", body: {} }).then(function (r) { q.savedByMe = r.saved !== undefined ? !!r.saved : !q.savedByMe; repaint(d.save); toast(q.savedByMe ? "Publicação salva." : "Removida dos salvos."); }).catch(onErr); return; }
    if (d.comments) return openComments(d.comments);
    if (d.share) { var url = new URL("#/post/" + d.share, location.href).href; if (navigator.share) navigator.share({ title: "QINBLY", url: url }).catch(function () { }); else if (navigator.clipboard) navigator.clipboard.writeText(url).then(function () { toast("Link copiado."); }); return; }
    if (d.vote) { var pv = d.vote.split(":"); api("/api/posts/" + pv[0] + "/vote", { method: "POST", body: { optionIndex: Number(pv[1]) } }).then(function () { return api("/api/posts/" + pv[0]); }).then(function (j) { POSTS[pv[0]] = j.post; repaint(pv[0]); }).catch(onErr); return; }
    if (d.follow) return toggleFollow(d.follow, d.profile);
    if (d.open) { location.hash = "#/post/" + d.open; return; }
    if (d.story !== undefined) { var en = (window.__tray || [])[Number(d.story)]; if (en && en.stories && en.stories.length) openStories(en); else addStory(); return; }
    if (t.hasAttribute("data-more")) { loadFeed(false); return; }
    if (t.hasAttribute("data-settings")) return settingsSheet();
    if (t.hasAttribute("data-accounts")) return accountsSheet();
    if (t.hasAttribute("data-edit")) { location.hash = "#/edit"; return; }
    if (d.shareprofile) { var pu = new URL("#/u/" + d.shareprofile, location.href).href; if (navigator.share) navigator.share({ title: "QINBLY", url: pu }).catch(function () { }); else if (navigator.clipboard) navigator.clipboard.writeText(pu).then(function () { toast("Link copiado."); }); return; }
    if (d.chat) { closeSheet(); location.hash = "#/chat/" + d.chat; return; }
    if (t.hasAttribute("data-find")) return findPeople();
    if (d.switch) { activeId = d.switch; save(); closeSheet(); goto("#/"); return; }
    if (d.drop) { var was = d.drop === activeId; dropAccount(d.drop); closeSheet(); goto(accounts.length ? "#/" : "#/login"); return; }
    if (t.hasAttribute("data-addacc")) { closeSheet(); location.hash = "#/login?add"; return; }
    if (t.hasAttribute("data-install")) { sheet("Instalar no iPhone", '<div style="padding:0 20px 20px;font-size:16px;line-height:1.6">1. Abra esta página no <b>Safari</b>.<br>2. Toque no botão de compartilhar (quadrado com seta para cima).<br>3. Escolha <b>Adicionar à Tela de Início</b>.<br>4. Abra o ícone QINBLY como um app.</div>'); return; }
    if (d.mode) { viewLogin(d.mode); return; }
    if (t.hasAttribute("data-moremenu")) return moreMenu();
    if (d.ftab) { feedTab = d.ftab; document.querySelectorAll(".ftabs button").forEach(function (b) { b.classList.toggle("on", b.dataset.ftab === feedTab); }); drawFeed(); return; }
    if (d.ctab) { comTab = d.ctab; viewCommunities(); return; }
    if (d.joincom) { api("/api/communities/" + d.joincom + "/join", { method: "POST", body: {} }).then(function () { if (t.hasAttribute("data-stay")) viewCommunity(d.joincom); else viewCommunities(); }).catch(onErr); return; }
    if (t.hasAttribute("data-newcom")) return newCommunity();
    if (d.mk !== undefined) { mkCat = Number(d.mk); viewMarket(); return; }
    if (d.listing) return openListing(d.listing);
    if (t.hasAttribute("data-addstory")) return addStory();
  });

  /* ---------- rotas ---------- */
  function render() {
    clearTimers(); closeSheet();
    var h = (location.hash || "#/").replace(/^#/, ""), parts = h.split("?")[0].split("/").filter(Boolean);
    if (!cur() && parts[0] !== "u" && parts[0] !== "post") { if (parts[0] !== "login") location.hash = "#/login"; return viewLogin("login"); }
    if (parts[0] === "login") return viewLogin("login");
    var r = parts[0] || "home";
    if (r === "home") return viewFeed();
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
    if (r === "chat" && parts[1]) return viewChat(decodeURIComponent(parts[1]));
    location.hash = "#/";
  }
  window.addEventListener("hashchange", render);
  render();

  /* contadores de mensagens e notificações novas */
  function pollUnread() {
    if (!cur() || document.hidden) return;
    api("/api/inbox").then(function (j) { unread = (j.conversations || []).reduce(function (n, c) { return n + (c.unreadCount || 0); }, 0); paintBadges(); }).catch(function () { });
    api("/api/notifications").then(function (j) { notifN = (j.notifications || []).filter(function (n) { return !n.readAt; }).length; paintBadges(); }).catch(function () { });
  }
  setInterval(pollUnread, 30000); setTimeout(pollUnread, 1500);

  if ("serviceWorker" in navigator) { window.addEventListener("load", function () { navigator.serviceWorker.register("sw.js").catch(function () { }); }); }
})();
