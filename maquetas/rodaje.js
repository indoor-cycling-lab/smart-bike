/* ==========================================================================
   Smart Bike — reparto de la pantalla de rodaje en las maquetas
   --------------------------------------------------------------------------
   Reproduce lo que hace la app (README de MetricGrid y de RideScreen):

   1. Área fija de datos: el 40 % del alto útil en vertical y del ancho útil
      en horizontal (lo pone components.css; aquí se fija en píxeles).
   2. Cifra de los datos: la mayor, hasta 144 px, con la que caben en su
      celda la plantilla del dato («8888» W, «188» rpm, «8:88:88», «88,8») y
      su valor actual. No depende de lo que marque: no se mueve al rodar.
   3. Botones − y + y guía minimizada: la rejilla les cede lo que le sobra con
      esa cifra (alto en vertical, ancho en horizontal para los botones); lo que
      falte lo pierde la marcha. En horizontal la guía minimizada va bajo la fila principal.
   4. La marcha, los platos o la potencia objetivo se reducen (zoom) para
      caber en lo que queda, nunca por encima de su tamaño; la escalera y el
      cassette no se reducen.
   5. Pantalla baja (change rodaje-pantallas-bajas): en vertical, con menos de
      760 px de alto, el .sq-ride lleva sq-ride--baja (menos aire y el cassette
      más bajo, lo pone components.css) y, con los botones − y +, la rejilla
      muestra como máximo los 4 primeros datos; los demás se ocultan sin
      cambiar los elegidos.

   Se recalcula al cambiar de estado (revision.js), al cargar las fuentes y al
   cambiar el tamaño de la ventana. Uso: <script src="rodaje.js" defer></script>.
   ========================================================================== */
(function () {
  "use strict";

  var TOPE = 144, MINIMO = 16, ALTO_BAJO = 760, DATOS_CON_BOTONES = 4;

  /** Valor más largo de cada dato, por su etiqueta. Con números tabulares el 8 mide lo que cualquier cifra. */
  var PLANTILLAS = {
    "potencia": "8888", "potencia media": "8888", "potencia máxima": "8888",
    "cadencia": "188", "tiempo": "8:88:88", "w/kg": "88,8", "pendiente": "−88,8",
  };

  function px(v) { return parseFloat(v) || 0; }

  /** El nodo de texto con el valor de un dato (antes de la unidad). */
  function nodoValor(metric) {
    var val = metric.querySelector(".sq-metric-val");
    for (var n = val.firstChild; n; n = n.nextSibling) if (n.nodeType === 3) return n;
    return null;
  }

  function plantilla(metric) {
    var etiqueta = metric.querySelector(".sq-label").textContent.trim().toLowerCase();
    return PLANTILLAS[etiqueta];
  }

  /** Pone en cada dato su plantilla (o lo devuelve a su valor) para medir. */
  function conPlantillas(metrics, fn) {
    var guardados = metrics.map(function (m) {
      var n = nodoValor(m), p = plantilla(m);
      var antes = n ? n.nodeValue : null;
      if (n && p) n.nodeValue = p;
      return antes;
    });
    try { return fn(); } finally {
      metrics.forEach(function (m, i) { var n = nodoValor(m); if (n) n.nodeValue = guardados[i]; });
    }
  }

  function celdasCaben(metrics) {
    return metrics.every(function (m) {
      var val = m.querySelector(".sq-metric-val"), lab = m.querySelector(".sq-label");
      var alto = lab.offsetHeight + px(getComputedStyle(m).rowGap) + val.offsetHeight;
      return val.scrollWidth <= m.clientWidth + 0.5 && lab.scrollWidth <= m.clientWidth + 0.5 && alto <= m.clientHeight + 0.5;
    });
  }

  /** La mayor cifra con la que caben la plantilla y el valor actual de todos los datos. */
  function ajustarCifra(grid, metrics) {
    function cabe(t) {
      grid.style.setProperty("--mg-size", t + "px");
      return celdasCaben(metrics) && conPlantillas(metrics, function () { return celdasCaben(metrics); });
    }
    var lo = MINIMO, hi = TOPE;
    while (lo < hi) { var m = Math.ceil((lo + hi) / 2); if (cabe(m)) lo = m; else hi = m - 1; }
    grid.style.setProperty("--mg-size", lo + "px");
    return lo;
  }

  /** Lo mínimo que puede medir la rejilla (alto en vertical, ancho en horizontal) con la cifra que tiene. */
  function minimoRejilla(grid, metrics, vertical) {
    var cs = getComputedStyle(grid);
    var cols = cs.gridTemplateColumns.split(" ").length, filas = cs.gridTemplateRows.split(" ").length;
    return conPlantillas(metrics, function () {
      if (vertical) {
        var alto = Math.max.apply(null, metrics.map(function (m) {
          return m.querySelector(".sq-label").offsetHeight + px(getComputedStyle(m).rowGap) + m.querySelector(".sq-metric-val").offsetHeight;
        }));
        return Math.ceil(filas * alto + (filas - 1) * px(cs.rowGap));
      }
      var ancho = function (m) { return Math.max(m.querySelector(".sq-metric-val").scrollWidth, m.querySelector(".sq-label").scrollWidth); };
      var normales = metrics.filter(function (m) { return getComputedStyle(m).gridColumnStart !== "1" || getComputedStyle(m).gridColumnEnd !== "-1"; });
      var anchas = metrics.filter(function (m) { return normales.indexOf(m) === -1; });
      var w = cols * Math.max.apply(null, [0].concat(normales.map(ancho))) + (cols - 1) * px(cs.columnGap);
      return Math.ceil(Math.max.apply(null, [w].concat(anchas.map(ancho))));
    });
  }

  /** Con la pantalla baja y los botones, solo los primeros datos; si no, todos. */
  function recortarDatos(grid, recortar) {
    var todos = Array.prototype.slice.call(grid.querySelectorAll(":scope > .sq-metric"));
    todos.forEach(function (m, i) { m.style.display = recortar && i >= DATOS_CON_BOTONES ? "none" : ""; });
    grid.setAttribute("data-n", recortar ? Math.min(todos.length, DATOS_CON_BOTONES) : todos.length);
    return todos.slice(0, recortar ? DATOS_CON_BOTONES : todos.length);
  }

  function esEscalera(el) { return !!(el.querySelector && el.querySelector(".sq-ladder, .sq-cass")); }

  /** Envuelve la marcha (todo menos la escalera o el cassette) en .sq-ride__fit, una sola vez. */
  function envoltorio(zona) {
    var fit = zona.querySelector(":scope > .sq-ride__fit");
    if (fit) return fit;
    fit = document.createElement("div");
    fit.className = "sq-ride__fit";
    Array.prototype.slice.call(zona.children).forEach(function (c) { if (!esEscalera(c)) fit.appendChild(c); });
    zona.insertBefore(fit, zona.firstChild);
    return fit;
  }

  function encajarMarcha(zona) {
    var fit = envoltorio(zona);
    fit.style.zoom = 1;
    var otros = Array.prototype.slice.call(zona.children).filter(function (c) { return c !== fit; });
    var hueco = otros.reduce(function (a, c) { return a + c.offsetHeight + px(getComputedStyle(zona).rowGap); }, 0);
    var cs = getComputedStyle(zona);
    var alto = zona.clientHeight - px(cs.paddingTop) - px(cs.paddingBottom) - hueco;
    var ancho = zona.clientWidth - px(cs.paddingLeft) - px(cs.paddingRight);
    var k = Math.min(1, ancho / fit.offsetWidth, alto / fit.offsetHeight);
    fit.style.zoom = k;
    return k;
  }

  function repartir(ride) {
    var vertical = ride.classList.contains("sq-ride--v");
    var grid = ride.querySelector(".sq-mgrid");
    var zona = ride.querySelector(".sq-ride__gear, .sq-ride__erg");
    var botones = ride.querySelector(".sq-ride__buttons");
    if (!grid || !zona) return;
    ride.classList.toggle("sq-ride--baja", vertical && ride.clientHeight < ALTO_BAJO);
    var metrics = recortarDatos(grid, ride.classList.contains("sq-ride--baja") && !!botones);
    var cs = getComputedStyle(ride);

    // 1. Área fija.
    var area = vertical
      ? Math.floor(0.4 * (ride.clientHeight - px(cs.paddingTop) - px(cs.paddingBottom)))
      : Math.floor(0.4 * (ride.clientWidth - px(cs.paddingLeft) - px(cs.paddingRight)));
    grid.style[vertical ? "height" : "width"] = area + "px";

    // 2. Cifra.
    var cifra = ajustarCifra(grid, metrics);

    // 3. Botones y guía de inicio: primero la holgura de la rejilla. En horizontal la guía va bajo la fila principal y no le quita ancho.
    var cede = 0;
    var guia = ride.querySelector(":scope > .sq-guide:not(.sq-guide--sheet)");
    if (botones || guia) {
      var main = ride.querySelector(".sq-ride__main");
      var hueco = vertical
        ? (botones ? botones.offsetHeight + px(cs.rowGap) : 0) + (guia ? guia.offsetHeight + px(cs.rowGap) : 0)
        : (botones ? botones.offsetWidth + px(getComputedStyle(main).columnGap) : 0);
      cede = Math.max(0, Math.min(hueco, area - minimoRejilla(grid, metrics, vertical)));
      grid.style[vertical ? "height" : "width"] = (area - cede) + "px";
    }

    // 4. La marcha en lo que queda.
    var k = encajarMarcha(zona);
    // Para revisar a mano: lo calculado queda en el atributo.
    ride.setAttribute("data-reparto", "area=" + area + " cede=" + cede + " cifra=" + cifra + " marcha=" + k.toFixed(2));
  }

  function todo() {
    var rides = document.querySelectorAll(".sq-ride");
    for (var i = 0; i < rides.length; i++) if (rides[i].offsetParent) repartir(rides[i]);
  }

  var pendiente = false;
  function programar() {
    if (pendiente) return;
    pendiente = true;
    requestAnimationFrame(function () { pendiente = false; todo(); });
  }

  /** Las fuentes se cargan al usarlas: se piden todas las de la pantalla antes de medir. */
  var FUENTES = ['400 16px "Barlow"', '600 16px "Barlow"', '600 16px "Barlow Condensed"', '700 16px "Barlow Condensed"'];

  function start() {
    programar();
    if (document.fonts) Promise.all(FUENTES.map(function (f) { return document.fonts.load(f); })).then(programar, programar);
    new MutationObserver(programar).observe(document.documentElement, { attributes: true, attributeFilter: ["data-rv-state"] });
    window.addEventListener("resize", programar);
    if (document.fonts) {
      document.fonts.ready.then(programar);
      document.fonts.addEventListener("loadingdone", programar);
    }
  }

  if (document.readyState === "loading") document.addEventListener("DOMContentLoaded", start);
  else start();
})();
