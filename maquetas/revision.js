/* ==========================================================================
   Smart Bike — selector de estado y de tema de las maquetas
   --------------------------------------------------------------------------
   Herramienta de revisión, no forma parte de la app.

   - <body data-mock-states="id:Etiqueta, id2:Etiqueta 2"> declara los estados
     en el orden del selector. El primero es el de partida.
   - [data-mock-state="id"] marca un bloque que solo se ve en ese estado.
     Admite varios separados por espacios: data-mock-state="a b".
   - [data-mock-attr="id:atributo=valor; id2:atributo=valor2"] cambia un
     atributo según el estado (p. ej. data-n de la rejilla de datos).
   - El estado y el tema van en el hash (#estado=id&tema=claro), así se pueden
     enlazar. El tema por defecto es rodaje (oscuro).
   - Con idioma=en en el hash (#estado=id&idioma=en) la maqueta se ve en inglés:
     revision.js carga en.js (generado por scripts/sitio/diccionario.py, ver
     maquetas/en.json) y sustituye textos, aria-label, title y placeholder por
     coincidencia exacta. Cada texto visible que se queda sin traducir recibe
     data-rv-sin-traducir (lo lee scripts/sitio/capturas.py). Sin idioma, o con
     idioma=es, la maqueta no cambia.
   - Con vista=captura en el hash (#estado=id&tema=claro&vista=captura) no se
     monta la barra y la pantalla queda sola, sin marco, para fotografiarla.

   Sin JavaScript se ven todos los bloques apilados.
   Uso: <script src="revision.js"></script> en <head> (sin defer).
   ========================================================================== */
(function () {
  "use strict";

  // Antes del primer pintado: oculta los estados inactivos (ver revision.css).
  document.documentElement.classList.add("rv-js");

  var EN = /[#&]idioma=en\b/.test(location.hash);
  // Sincronizado y en el <head>: el diccionario está antes de que arranque start().
  if (EN) document.write('<script src="' + document.currentScript.src.replace(/revision\.js(\?.*)?$/, "en.js") + '"><\/script>');

  var TEMAS = [{ id: "rodaje", label: "Rodaje (oscuro)" }, { id: "claro", label: "Claro" }];

  function parseStates() {
    var raw = document.body.getAttribute("data-mock-states") || "";
    return raw.split(",").map(function (s) {
      var i = s.indexOf(":");
      return { id: s.slice(0, i).trim(), label: s.slice(i + 1).trim() };
    }).filter(function (s) { return s.id; });
  }

  function fromHash(key, valid, fallback) {
    var m = location.hash.match(new RegExp(key + "=([\\w-]+)"));
    var id = m && m[1];
    for (var i = 0; i < valid.length; i++) if (valid[i].id === id) return id;
    return fallback;
  }

  function writeHash(state, theme) {
    var parts = [];
    if (state) parts.push("estado=" + state);
    if (theme && theme !== "rodaje") parts.push("tema=" + theme);
    history.replaceState(null, "", parts.length ? "#" + parts.join("&") : location.pathname);
  }

  function apply(id) {
    var blocks = document.querySelectorAll("[data-mock-state]");
    for (var i = 0; i < blocks.length; i++) {
      var ids = blocks[i].getAttribute("data-mock-state").split(/\s+/);
      blocks[i].classList.toggle("is-rv-active", ids.indexOf(id) !== -1);
    }
    var attrs = document.querySelectorAll("[data-mock-attr]");
    for (var j = 0; j < attrs.length; j++) {
      attrs[j].getAttribute("data-mock-attr").split(";").forEach(function (rule) {
        var m = rule.trim().match(/^([\w-]+):([\w-]+)=(.*)$/);
        if (m && m[1] === id) attrs[j].setAttribute(m[2], m[3]);
      });
    }
    document.documentElement.setAttribute("data-rv-state", id);
  }

  function applyTheme(theme) {
    document.documentElement.setAttribute("data-theme", theme);
  }

  // --- Idioma inglés ----------------------------------------------------------

  var CON_LETRAS = /[A-Za-zÀ-ÿ]/;
  var ATRIBUTOS = ["aria-label", "title", "placeholder"];
  var pendientes = new WeakMap(); // nodo de texto → texto español que no se pudo traducir

  function norm(s) { return s.replace(/\s+/g, " ").trim(); }

  // Primero el contexto (clases del elemento que contiene el texto), luego el global.
  function traducir(texto, el) {
    var d = window.RV_EN;
    for (var i = 0; i < el.classList.length; i++) {
      var ctx = d.contextos[el.classList[i]];
      if (ctx && ctx[texto] !== undefined) return ctx[texto];
    }
    return d.textos[texto];
  }

  function traducirDocumento() {
    var d = window.RV_EN;
    if (!d) { console.error("revision.js: no se cargó en.js"); return; }
    document.documentElement.lang = "en";
    document.title = d.textos[norm(document.title)] || document.title;
    var iguales = {};
    d.iguales.forEach(function (t) { iguales[t] = true; });
    var walker = document.createTreeWalker(document.body, NodeFilter.SHOW_TEXT);
    var nodos = [];
    while (walker.nextNode()) nodos.push(walker.currentNode);
    nodos.forEach(function (n) {
      var el = n.parentElement;
      if (!el || el.closest(".rv-bar, script, style")) return;
      var texto = norm(n.nodeValue);
      if (!texto || !CON_LETRAS.test(texto) || iguales[texto]) return;
      var en = traducir(texto, el);
      if (en === undefined) pendientes.set(n, texto);
      else n.nodeValue = n.nodeValue.match(/^\s*/)[0] + en + n.nodeValue.match(/\s*$/)[0];
    });
    Array.prototype.forEach.call(document.querySelectorAll("[aria-label], [title], [placeholder]"), function (el) {
      if (el.closest(".rv-bar")) return;
      ATRIBUTOS.forEach(function (a) {
        var v = el.getAttribute(a);
        if (!v) return;
        var en = traducir(norm(v), el);
        if (en !== undefined) el.setAttribute(a, en);
      });
    });
  }

  // Tras elegir el estado: lo que está a la vista y sigue en español.
  function marcarSinTraducir() {
    Array.prototype.forEach.call(document.querySelectorAll("[data-rv-sin-traducir]"), function (el) { el.removeAttribute("data-rv-sin-traducir"); });
    var walker = document.createTreeWalker(document.body, NodeFilter.SHOW_TEXT);
    while (walker.nextNode()) {
      var texto = pendientes.get(walker.currentNode);
      var el = walker.currentNode.parentElement;
      if (texto && el.getClientRects().length && getComputedStyle(el).visibility !== "hidden") {
        el.setAttribute("data-rv-sin-traducir", (el.getAttribute("data-rv-sin-traducir") || "") + texto + " | ");
      }
    }
  }

  function select(id, label, options, value, onChange) {
    var l = document.createElement("label");
    l.className = "rv-bar__label";
    l.htmlFor = id;
    l.textContent = label;
    var s = document.createElement("select");
    s.className = "rv-bar__select";
    s.id = id;
    options.forEach(function (o) {
      var opt = document.createElement("option");
      opt.value = o.id;
      opt.textContent = o.label;
      s.appendChild(opt);
    });
    s.value = value;
    s.addEventListener("change", function () { onChange(s.value); });
    return [l, s];
  }

  function buildBar(states, current, theme) {
    var bar = document.createElement("div");
    bar.className = "rv-bar";
    bar.setAttribute("role", "region");
    bar.setAttribute("aria-label", "Herramienta de revisión de la maqueta");

    var tag = document.createElement("a");
    tag.className = "rv-bar__tag";
    tag.href = "index.html";
    tag.textContent = "Maqueta";
    tag.title = "Índice de maquetas";
    bar.appendChild(tag);

    if (states.length > 1) {
      select("rv-state", "Estado", states, current, function (v) {
        current = v;
        writeHash(current, theme);
        apply(current);
      }).forEach(function (el) { bar.appendChild(el); });
    }
    select("rv-theme", "Tema", TEMAS, theme, function (v) {
      theme = v;
      writeHash(current, theme);
      applyTheme(theme);
    }).forEach(function (el) { bar.appendChild(el); });

    document.body.insertBefore(bar, document.body.firstChild);
  }

  function start() {
    var states = parseStates();
    var current = fromHash("estado", states, states.length ? states[0].id : null);
    var theme = fromHash("tema", TEMAS, "rodaje");
    if (/[#&]vista=captura\b/.test(location.hash)) document.documentElement.classList.add("rv-captura");
    else buildBar(states, current, theme);
    applyTheme(theme);
    if (EN) traducirDocumento();
    if (current) apply(current);
    if (EN) marcarSinTraducir();
    window.addEventListener("hashchange", function () {
      var s = fromHash("estado", states, states.length ? states[0].id : null);
      var t = fromHash("tema", TEMAS, "rodaje");
      var ss = document.getElementById("rv-state");
      var ts = document.getElementById("rv-theme");
      if (ss) ss.value = s;
      if (ts) ts.value = t;
      applyTheme(t);
      if (s) apply(s);
      if (EN) marcarSinTraducir();
    });
  }

  if (document.readyState === "loading") document.addEventListener("DOMContentLoaded", start);
  else start();
})();
