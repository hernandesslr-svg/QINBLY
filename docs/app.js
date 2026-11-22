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
    menu: '<path d="M4 7h16M4 12h16M4 17h16"/>',
    dots: '<circle cx="5" cy="12" r="1.2"/><circle cx="12" cy="12" r="1.2"/><circle cx="19" cy="12" r="1.2"/>'
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
      (!mine && !p.isFollowing && me ? '<button class="sb" style="flex:none;width:76px" data-follow="' + p.authorId + '">Seguir</button>' : "") + '<button class="ib" data-pmenu="' + p.id + '" aria-label="Mais opções">' + svg("dots") + "</button></div>" +
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
    var me = cur(), reply = null;
    var s = sheet("Comentários", spin(),
      '<div id="rp" class="rpl" hidden></div><form class="cin" id="cf"><input id="ci" maxlength="2000" placeholder="Adicione um comentário…" autocomplete="off"><button type="submit">Enviar</button></form>');
    var box = s.querySelector(".body"), list = [];
    function one(c, sub) {
      var a = c.author || {}, mine = me && a.id === me.id;
      return '<div class="cm ' + (sub ? "sub" : "") + '"><a href="#/u/' + esc(a.username) + '">' + avatar(a.avatarUrl, a.displayName || a.username, "s") + '</a><div class="b"><b>' + esc(a.username) + "</b>" + linkify(c.body) + "<small>" + ago(c.createdAt) + (c.editedAt ? " · editado" : "") + ' · <button data-crep="' + c.id + ":" + esc(a.username) + '">Responder</button>' + (mine ? ' · <button data-cdel="' + c.id + '">Apagar</button>' : ' · <button data-crpt="' + c.id + '">Denunciar</button>') + '</small></div><button class="act ' + (c.likedByMe ? "on" : "") + '" data-clike="' + c.id + '" aria-label="Curtir">' + svg("heart") + (c.likes || "") + "</button></div>";
    }
    function draw() {
      var top = list.filter(function (c) { return !c.parentId; }), h = "";
      top.forEach(function (c) { h += one(c, false); list.filter(function (r) { return r.parentId === c.id; }).forEach(function (r) { h += one(r, true); }); });
      box.innerHTML = h || '<div class="empty">Nenhum comentário ainda. Seja o primeiro.</div>';
    }
    api("/api/posts/" + id + "/comments").then(function (j) { list = j.comments || []; draw(); }).catch(function (e) { box.innerHTML = '<div class="empty">' + esc(e.message) + "</div>"; });
    s.addEventListener("click", function (e) {
      var t = e.target.closest("[data-crep],[data-cdel],[data-clike],[data-crpt]"); if (!t) return; e.stopPropagation();
      if (t.dataset.crep) { var a = t.dataset.crep.split(":"); reply = a[0]; var r = s.querySelector("#rp"); r.hidden = false; r.textContent = "Respondendo a @" + a[1] + " (toque para cancelar)"; s.querySelector("#ci").value = "@" + a[1] + " "; s.querySelector("#ci").focus(); }
      else if (t.dataset.cdel) api("/api/comments/" + t.dataset.cdel, { method: "DELETE" }).then(function () { list = list.filter(function (c) { return c.id !== t.dataset.cdel && c.parentId !== t.dataset.cdel; }); draw(); if (POSTS[id]) { POSTS[id].comments = Math.max(0, (POSTS[id].comments || 1) - 1); repaint(id); } }).catch(onErr);
      else if (t.dataset.clike) api("/api/comments/" + t.dataset.clike + "/like", { method: "POST", body: {} }).then(function (r) { list.forEach(function (c) { if (c.id === t.dataset.clike) { c.likedByMe = !!r.liked; c.likes = Math.max(0, (c.likes || 0) + (r.liked ? 1 : -1)); } }); draw(); }).catch(onErr);
      else if (t.dataset.crpt) { closeSheet(); reportSheet("comment", t.dataset.crpt); }
    });
    s.querySelector("#rp").addEventListener("click", function () { reply = null; this.hidden = true; });
    s.querySelector("#cf").addEventListener("submit", function (e) {
      e.preventDefault();
      var inp = s.querySelector("#ci"), t = inp.value.trim(); if (!t) return;
      if (!cur()) { location.hash = "#/login"; return; }
      inp.disabled = true;
      api("/api/posts/" + id + "/comments", { method: "POST", body: { body: t, parentId: reply || undefined } }).then(function (j) {
        list.push(j.comment); draw(); inp.value = ""; reply = null; s.querySelector("#rp").hidden = true; box.scrollTop = box.scrollHeight; if (POSTS[id]) { POSTS[id].comments = (POSTS[id].comments || 0) + 1; repaint(id); }
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
      if (own && s.id) foot = '<div class="sfoot"><span>👁 ' + (s.views || 0) + '</span><button data-shl>Destaque</button><button data-sdel>Apagar</button></div>';
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
      var t = e.target.closest("[data-x],[data-prev],[data-next],[data-shl],[data-sdel]"); if (!t) return;
      if (t.hasAttribute("data-x")) return end();
      var s = items[i];
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
  var FTABS = ["Descobrir", "Seguindo", "Em alta", "Vídeos", "Lives"];
  function hot(p) { return (p.likes || 0) * 2 + (p.comments || 0) * 3 + (p.reposts || 0) * 3; }
  function feedList() {
    var l = FEED.filter(function (p) { return !isHidden(p); });
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
      var tray = (j.tray || []).filter(function (t) { return t.mine || (t.stories && t.stories.length) || t.note; });
      window.__tray = tray;
      var el = document.getElementById("st"); if (!el) return;
      el.innerHTML = '<div class="stories">' + tray.map(function (t, i) {
        return '<div class="story">' + (t.note ? '<span class="note">' + esc(t.note.body) + "</span>" : "") + '<button class="ring ' + (t.hasUnseen ? "new" : "") + '" data-story="' + i + '" aria-label="Ver story">' + avatar(t.avatarUrl, t.displayName || t.username) + "</button>" +
          (t.mine ? '<button class="plus" data-addstory aria-label="Criar story">+</button>' : "") + "<span>" + (t.mine ? "Seu story" : esc(t.username)) + "</span></div>";
      }).join("") + "</div>";
    }).catch(function () { /* stories são opcionais */ });
  }
  function drawFeed() {
    var fd = document.getElementById("fd"); if (!fd) return;
    if (feedTab === "Lives") return liveList();
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
    setView(top("@" + esc(name), { back: !isMeRoute, right: isMeRoute ? '<button class="ib" data-go="#/settings" aria-label="Configurações">' + svg("gear") + "</button>" : '<button class="ib" data-umenu aria-label="Mais">' + svg("dots") + "</button>" }) + '<div id="pf">' + spin() + "</div>", isMeRoute ? "me" : "");
    api("/api/users/" + encodeURIComponent(name)).then(function (j) {
      var u = j.user, posts = (j.posts || []).slice().sort(function (a, b) { return (b.pinned ? 1 : 0) - (a.pinned ? 1 : 0); }), own = me && u.id === me.id;
      window.__pu = u;
      if (own) { me.displayName = u.displayName; me.avatarUrl = u.avatarUrl; me.username = u.username; save(); }
      function gridOf(list) {
        return list.map(function (p) {
          POSTS[p.id] = p;
          if (p.mediaType === "text" || p.mediaType === "poll") return '<button class="tx" data-open="' + p.id + '" style="' + (p.textBg ? "background:" + bgCss(p.textBg) : "") + '">' + (p.pinned ? "📌 " : "") + esc((p.caption || "").slice(0, 60)) + "</button>";
          var img = p.mediaType === "video" ? "" : (p.mediaUrl ? "background-image:url('" + safeUrl(p.mediaUrl) + "')" : "");
          return '<button data-open="' + p.id + '" style="' + img + '">' + (p.pinned ? "📌" : "") + (p.mediaType === "video" ? "▶" : "") + "</button>";
        }).join("");
      }
      var pf = document.getElementById("pf");
      pf.innerHTML =
        '<div class="ptop"><span class="ring">' + avatar(u.avatarUrl, u.displayName || u.username, "l") + '</span><div class="pname"><h2>' + esc(u.displayName || u.username) + (u.verified ? VERIFIED : "") + "</h2>" +
        (own ? '<button class="at" data-accounts>@' + esc(u.username) + svg("chev") + "</button>" : '<span class="at">@' + esc(u.username) + "</span>") + "</div></div>" +
        (u.bio ? '<div class="bio">' + linkify(u.bio) + "</div>" : "") +
        '<div class="stats"><div><b>' + (u.posts_count || 0) + '</b>Publicações</div><button data-stat="followers"><b>' + (u.followers || 0) + '</b>Seguidores</button><button data-stat="following"><b>' + (u.following || 0) + "</b>Seguindo</button></div>" +
        '<div class="btns">' + (own ? '<button class="sb" data-edit>Editar perfil</button><button class="sb" data-shareprofile="' + esc(u.username) + '">Compartilhar perfil</button>'
          : '<button class="sb ' + (u.isFollowing ? "" : "pri") + '" data-follow="' + u.id + '" data-profile="' + esc(u.username) + '">' + (u.isFollowing ? "Seguindo" : "Seguir") + '</button><button class="sb" data-chat="' + esc(u.username) + '">Mensagem</button>') + "</div>" +
        '<div id="hl"></div><div class="tabs"><button class="on" data-ptab="grid" aria-label="Publicações">' + svg("grid") + "</button>" + (own ? '<button data-ptab="saved" aria-label="Salvos">' + svg("save") + "</button>" : "") + '</div><div id="pg">' + (posts.length ? '<div class="grid">' + gridOf(posts) + "</div>" : '<div class="empty">Nenhuma publicação ainda.</div>') + "</div>";
      pf.addEventListener("click", function (e) {
        var t = e.target.closest("[data-ptab],[data-hl]"); if (!t) return;
        if (t.dataset.hl !== undefined) { var h = (window.__hls || [])[Number(t.dataset.hl)]; if (h) openHighlight(h); return; }
        pf.querySelectorAll("[data-ptab]").forEach(function (b) { b.classList.toggle("on", b === t); });
        var pg = document.getElementById("pg");
        if (t.dataset.ptab === "grid") pg.innerHTML = posts.length ? '<div class="grid">' + gridOf(posts) + "</div>" : '<div class="empty">Nenhuma publicação ainda.</div>';
        else { pg.innerHTML = spin(); api("/api/me/saved").then(function (r) { var l = r.posts || []; pg.innerHTML = l.length ? '<div class="grid">' + gridOf(l) + "</div>" : '<div class="empty">Você ainda não salvou nada.</div>'; }).catch(onErr); }
      });
      api("/api/users/" + encodeURIComponent(u.username) + "/highlights").then(function (r) {
        var l = r.highlights || []; window.__hls = l; var el = document.getElementById("hl"); if (!el || !l.length) return;
        el.innerHTML = '<div class="stories" style="border:0;padding-top:0">' + l.map(function (h, i) { return '<div class="story"><button class="ring new" data-hl="' + i + '" aria-label="Destaque">' + avatar(h.mediaType === "image" ? h.mediaUrl : "", h.title) + "</button><span>" + esc(h.title) + "</span></div>"; }).join("") + "</div>";
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
    setView(top("@" + esc(name), { back: true, right: '<a class="ib" href="#/u/' + esc(name) + '" aria-label="Ver perfil">' + svg("user") + "</a>" }) + '<div class="chat" id="ch">' + spin() + "</div>" +
      '<div class="composer"><form class="in" id="cf"><button type="button" class="ib" id="at" aria-label="Anexar">' + svg("plus") + '</button><textarea id="mi" rows="1" maxlength="10000" placeholder="Mensagem…"></textarea><button class="send" type="submit">Enviar</button></form></div>', "inbox", "noNav chatv");
    function show(list) {
      var sig = list.map(function (m) { return m.id + (m.poll ? JSON.stringify(m.poll.votes) + m.poll.myVote : ""); }).join();
      if (sig === last) return; last = sig;
      var ch = document.getElementById("ch"); if (!ch) return;
      var stick = (window.innerHeight + window.scrollY) >= document.body.scrollHeight - 160 || !ch.dataset.done;
      ch.dataset.done = "1";
      ch.innerHTML = list.length ? list.map(function (m) {
        return '<div class="' + bubbleClass(m) + (m.senderId === me.id ? " me" : "") + '">' + msgBody(m) + "<small>" + new Date(m.createdAt).toLocaleTimeString("pt-BR", { hour: "2-digit", minute: "2-digit" }) + "</small></div>";
      }).join("") : '<div class="empty">Diga olá para @' + esc(name) + ".</div>";
      if (stick) window.scrollTo(0, document.body.scrollHeight);
    }
    function load() { api("/api/messages/" + encodeURIComponent(name)).then(function (j) { show(j.messages || []); }).catch(function (e) { if (e.expired) onErr(e); }); }
    function send(text) { return api("/api/messages/" + encodeURIComponent(name), { method: "POST", body: { body: text } }); }
    window.__reload = load; load(); timers.push(setInterval(load, 5000));
    document.getElementById("at").addEventListener("click", function () { attachMenu(send, load); });
    document.getElementById("cf").addEventListener("submit", function (e) {
      e.preventDefault();
      var inp = document.getElementById("mi"), t = inp.value.trim(); if (!t) return;
      inp.value = "";
      send(t).then(load).catch(function (x) { inp.value = t; onErr(x); });
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
    setView(top("Comunidade", { back: true }) + '<div id="ch0">' + spin() + '</div><div class="chat" id="ch"></div><div class="composer" id="cc" hidden><form class="in" id="cf"><button type="button" class="ib" id="at" aria-label="Anexar">' + svg("plus") + '</button><textarea id="mi" rows="1" maxlength="10000" placeholder="Escreva para a comunidade…"></textarea><button class="send" type="submit">Enviar</button></form></div>', "communities", "noNav chatv");
    function load() {
      api("/api/communities/" + id).then(function (j) {
        var c = j.community, h = document.getElementById("ch0"); if (!h) return;
        h.innerHTML = '<div class="ptop"><span class="av l" style="font-size:32px">' + esc(c.emoji) + '</span><div class="pname"><h2>' + esc(c.name) + "</h2><span class=\"at\">" + c.members + " membro" + (c.members === 1 ? "" : "s") + " · criada por @" + esc(c.ownerUsername) + "</span></div>" + (c.isOwner ? "" : '<button class="sb ' + (c.joined ? "" : "pri") + '" style="flex:none;width:84px" data-joincom="' + c.id + '" data-stay>' + (c.joined ? "Sair" : "Entrar") + "</button>") + "</div>" + (c.description ? '<div class="bio">' + linkify(c.description) + "</div>" : "");
        document.getElementById("cc").hidden = !c.joined;
        var sig = (j.posts || []).map(function (m) { return m.id + (m.poll ? JSON.stringify(m.poll.votes) + m.poll.myVote : ""); }).join(); if (sig === last) return; last = sig;
        var ch = document.getElementById("ch"), stick = (window.innerHeight + window.scrollY) >= document.body.scrollHeight - 160 || !ch.dataset.done; ch.dataset.done = "1";
        ch.innerHTML = (j.posts || []).length ? j.posts.map(function (m) {
          var a = m.author || {};
          return '<div class="' + bubbleClass(m) + (m.mine ? " me" : "") + '">' + (m.mine ? "" : "<b style='font-size:12px'>" + esc(a.username) + "</b><br>") + msgBody(m) + "<small>" + ago(m.createdAt) + "</small></div>";
        }).join("") : '<div class="empty">Ninguém escreveu ainda.</div>';
        if (stick) window.scrollTo(0, document.body.scrollHeight);
      }).catch(function (e) { var h = document.getElementById("ch0"); if (h) h.innerHTML = '<div class="empty">' + esc(e.message) + "</div>"; });
    }
    window.__reload = load; load(); timers.push(setInterval(load, 6000));
    function sendC(text) { return api("/api/communities/" + id + "/posts", { method: "POST", body: { body: text } }); }
    document.getElementById("at").addEventListener("click", function () { attachMenu(sendC, load); });
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
  function moreMenu() {
    sheet("Mais",
      '<button class="row" data-accounts><span class="t"><b>Trocar de conta</b><small>@' + esc(cur() ? cur().username : "") + "</small></span></button>" +
      '<a class="row" href="#/saved"><span class="t"><b>Salvos</b></span></a>' +
      '<a class="row" href="#/settings"><span class="t"><b>Configurações</b><small>Conta, privacidade, tema e mais</small></span></a>' +
      '<a class="row" href="#/golive"><span class="t"><b>Transmitir ao vivo</b></span></a>' +
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
      rows = '<button class="row" data-pact="repost:' + id + '"><span class="t"><b>' + (p.repostedByMe ? "Desfazer repostagem" : "Repostar") + "</b></span></button>" + rows +
        '<button class="row" data-pact="report:' + id + '"><span class="t"><b>Denunciar</b></span></button>' +
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
    if (act === "share") { var url = new URL("#/post/" + id, location.href).href; if (navigator.share) navigator.share({ title: "QINBLY", url: url }).catch(function () { }); else if (navigator.clipboard) navigator.clipboard.writeText(url).then(function () { toast("Link copiado."); }); return; }
    if (act === "repost") return api("/api/posts/" + id + "/repost", { method: "POST", body: {} }).then(function (r) { p.repostedByMe = !!r.reposted; toast(r.reposted ? "Repostado." : "Repostagem desfeita."); }).catch(onErr);
    if (act === "report") return reportSheet("post", id);
    if (act === "block") return confirmSheet("Bloquear @" + (p.author || {}).username + "?", "Vocês deixam de ver as publicações um do outro.", "Bloquear", true, function () {
      api("/api/users/" + p.authorId + "/block", { method: "POST", body: {} }).then(function () { FEED = FEED.filter(function (x) { return x.authorId !== p.authorId; }); toast("Conta bloqueada."); render(); }).catch(onErr);
    });
    if (act === "pin") return api("/api/posts/" + id + "/pin", { method: "POST", body: {} }).then(function (r) { p.pinned = !!r.pinned; toast(r.pinned ? "Fixada no perfil." : "Desafixada."); }).catch(onErr);
    if (act === "archive") return api("/api/posts/" + id + "/archive", { method: "POST", body: {} }).then(function () { FEED = FEED.filter(function (x) { return x.id !== id; }); toast("Arquivada."); var el = document.querySelector('[data-post="' + id + '"]'); if (el) el.remove(); }).catch(onErr);
    if (act === "vis") return api("/api/posts/" + id, { method: "PATCH", body: { visibility: p.visibility === "private" ? "public" : "private" } }).then(function (j) { POSTS[id] = j.post; repaint(id); toast("Visibilidade alterada."); }).catch(onErr);
    if (act === "del") return confirmSheet("Excluir publicação?", "Isso não pode ser desfeito.", "Excluir", true, function () {
      api("/api/posts/" + id, { method: "DELETE" }).then(function () { FEED = FEED.filter(function (x) { return x.id !== id; }); toast("Publicação excluída."); var el = document.querySelector('[data-post="' + id + '"]'); if (el) el.remove(); else history.back(); }).catch(onErr);
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
    var s = sheet("Enviar", ["photo:Foto", "doc:Documento", "audio:Áudio (gravar)", "sticker:Figurinha", "poll:Enquete"].map(function (x) { var a = x.split(":"); return '<button class="row" data-att="' + a[0] + '"><span class="t"><b>' + a[1] + "</b></span></button>"; }).join(""));
    function pick(accept, cb) { var f = document.createElement("input"); f.type = "file"; f.accept = accept; f.onchange = function () { if (f.files[0]) cb(f.files[0]); }; f.click(); }
    function go(p) { closeSheet(); toast("Enviando…"); p.then(function () { done && done(); }).catch(onErr); }
    s.addEventListener("click", function (e) {
      var b = e.target.closest("[data-att]"); if (!b) return; var k = b.dataset.att;
      if (k === "photo") pick("image/*", function (f) { if (IMG_OK.indexOf(f.type) < 0) return toast("Use JPG, PNG, WebP ou GIF."); go(shrink(f).then(function (x) { return upload(x, "message_media"); }).then(function (path) { return send("qinbly:img:" + path); })); });
      else if (k === "doc") pick(".pdf,.doc,.docx,.xls,.xlsx,.ppt,.pptx,.txt,.csv,.zip", function (f) { if (DOC_OK.indexOf(f.type) < 0) return toast("Tipo de documento não aceito."); go(upload(f, "message_file").then(function (path) { return send("qinbly:file:" + path + ":" + encodeURIComponent(f.name)); })); });
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
  var STORY_BGS = [["#2F68E0", "#1F4FB0"], ["#E0457B", "#8A2A5A"], ["#1D9E75", "#0F6E56"], ["#EF9F27", "#B45309"], ["#378ADD", "#185FA5"], ["#111111", "#2B2B2B"], ["#D85A30", "#993C1D"], ["#639922", "#3B6D11"]];
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
    var s = sheet("Story de texto", '<div class="form"><div id="tsp" class="tbg" style="min-height:200px;border-radius:12px;background:' + storyBg(0) + '"><textarea id="tst" maxlength="300" placeholder="Escreva aqui" style="background:none;border:0;color:#fff;font:inherit;text-align:center;width:100%;resize:none;outline:none;min-height:120px"></textarea></div><div class="bgs" id="tsb">' + STORY_BGS.map(function (_, i) { return '<button data-b="' + i + '" class="' + (i === 0 ? "on" : "") + '" style="background:' + storyBg(i) + '"></button>'; }).join("") + "</div>" + err("tse") + '<button class="btn" id="tss">Publicar story</button></div>');
    s.querySelector("#tsb").addEventListener("click", function (e) { var b = e.target.closest("[data-b]"); if (!b) return; bg = Number(b.dataset.b); s.querySelector("#tsp").style.background = storyBg(bg); s.querySelectorAll("#tsb button").forEach(function (x) { x.classList.toggle("on", x === b); }); });
    s.querySelector("#tss").addEventListener("click", function () {
      var text = s.querySelector("#tst").value.trim(); if (!text) { s.querySelector("#tse").textContent = "Escreva algo para publicar."; return; }
      call(HOST + "/rest/v1/stories", { method: "POST", body: { user_id: cur().id, media_path: "text", media_type: "text", caption: "qinbly:text:" + bg + ":3:1:0:0\n" + text, expires_at: new Date(Date.now() + 86400000).toISOString() } })
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
  var SET_ITEMS = [["account", "Conta", "Senha, e-mail, aparelhos e exclusão"], ["privacy", "Privacidade", "Quem vê, quem fala com você e bloqueios"], ["content", "Conteúdo", "Palavras ocultas e publicações arquivadas"], ["appearance", "Tela", "Tema claro ou escuro"], ["accessibility", "Acessibilidade", "Texto maior e menos animações"], ["storage", "Dados e armazenamento", "Cache e seus dados"], ["verify", "Verificação", "Peça o selo de conta verificada"], ["help", "Ajuda e suporte", "Termos, privacidade e sobre"]];
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
    var th = LS.get("qb_theme", "dark"), light = th === "light" || (th === "auto" && window.matchMedia && matchMedia("(prefers-color-scheme: light)").matches);
    document.documentElement.setAttribute("data-theme", light ? "light" : "dark");
    document.body.classList.toggle("big", !!LS.get("qb_big", false));
    document.body.classList.toggle("calm", !!LS.get("qb_calm", false));
    var m = document.querySelector('meta[name="theme-color"]'); if (m) m.content = light ? "#ffffff" : "#16161a";
  }

  /* ---------- destaques, comentários, stories ---------- */
  function openHighlight(h) { openStories({ username: "", displayName: h.title, avatarUrl: "", stories: [{ id: "", mediaUrl: h.mediaUrl, mediaType: h.mediaType, caption: h.caption, createdAt: h.createdAt }], hl: true }); }

  /* ---------- cliques (uma escuta só para o app inteiro) ---------- */
  function toggleFollow(id, name) {
    api("/api/users/" + id + "/follow", { method: "POST", body: {} }).then(function () { if (name) viewProfile(name); else { toast("Seguindo."); RC.sug = RC.sug.filter(function (u) { return u.id !== id; }); fillRcol(); Object.keys(POSTS).forEach(function (k) { if (POSTS[k].authorId === id) { POSTS[k].isFollowing = true; repaint(k); } }); } }).catch(onErr);
  }
  document.addEventListener("click", function (e) {
    var t = e.target.closest("[data-go],[data-back],[data-like],[data-save],[data-comments],[data-share],[data-vote],[data-follow],[data-open],[data-story],[data-more],[data-settings],[data-accounts],[data-edit],[data-shareprofile],[data-chat],[data-find],[data-switch],[data-drop],[data-addacc],[data-install],[data-mode],[data-moremenu],[data-ftab],[data-ctab],[data-joincom],[data-newcom],[data-mk],[data-listing],[data-addstory],[data-pmenu],[data-pact],[data-cvote],[data-golive],[data-stat],[data-umenu],[data-newlisting]");
    if (!t) return;
    var d = t.dataset;
    if (d.go) { if (location.hash === d.go) render(); else location.hash = d.go; return; }
    if (t.hasAttribute("data-back")) { if (history.length > 1 && !/add/.test(location.hash)) history.back(); else location.hash = "#/"; return; }
    if (d.like) { var p = POSTS[d.like]; if (!cur()) { location.hash = "#/login"; return; } api("/api/posts/" + d.like + "/like", { method: "POST", body: {} }).then(function (r) { p.likedByMe = !!r.liked; p.likes = Math.max(0, (p.likes || 0) + (r.liked ? 1 : -1)); repaint(d.like); }).catch(onErr); return; }
    if (d.save) { var q = POSTS[d.save]; api("/api/posts/" + d.save + "/save", { method: "POST", body: {} }).then(function (r) { q.savedByMe = r.saved !== undefined ? !!r.saved : !q.savedByMe; repaint(d.save); toast(q.savedByMe ? "Publicação salva." : "Removida dos salvos."); }).catch(onErr); return; }
    if (d.comments) return openComments(d.comments);
    if (d.share) { var url = new URL("#/post/" + d.share, location.href).href; if (navigator.share) navigator.share({ title: "QINBLY", url: url }).catch(function () { }); else if (navigator.clipboard) navigator.clipboard.writeText(url).then(function () { toast("Link copiado."); }); return; }
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
    if (t.hasAttribute("data-install")) { sheet("Instalar no iPhone", '<div style="padding:0 20px 20px;font-size:16px;line-height:1.6">1. Abra esta página no <b>Safari</b>.<br>2. Toque no botão de compartilhar (quadrado com seta para cima).<br>3. Escolha <b>Adicionar à Tela de Início</b>.<br>4. Abra o ícone QINBLY como um app.</div>'); return; }
    if (d.mode) { viewLogin(d.mode); return; }
    if (t.hasAttribute("data-moremenu")) return moreMenu();
    if (d.ftab) { feedTab = d.ftab; document.querySelectorAll(".ftabs button").forEach(function (b) { b.classList.toggle("on", b.dataset.ftab === feedTab); }); drawFeed(); return; }
    if (d.ctab) { comTab = d.ctab; viewCommunities(); return; }
    if (d.joincom) { api("/api/communities/" + d.joincom + "/join", { method: "POST", body: {} }).then(function () { if (t.hasAttribute("data-stay")) viewCommunity(d.joincom); else viewCommunities(); }).catch(onErr); return; }
    if (t.hasAttribute("data-newcom")) return newCommunity();
    if (d.mk !== undefined) { mkCat = Number(d.mk); viewMarket(); return; }
    if (d.listing) return openListing(d.listing);
    if (t.hasAttribute("data-addstory")) return storyMenu();
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
  setInterval(pollUnread, 30000); setTimeout(pollUnread, 1500);

  if ("serviceWorker" in navigator) { window.addEventListener("load", function () { navigator.serviceWorker.register("sw.js").catch(function () { }); }); }
})();
