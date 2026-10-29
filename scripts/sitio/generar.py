#!/usr/bin/env python3
"""Genera la web pública (`site/`) a partir de los textos y las capturas.

Entradas:  site/contenido/{es,en}.json, site/capturas.json, design-system/.
Salidas:   site/index.html (redirección por idioma), site/es/index.html,
           site/en/index.html y site/assets/ (tokens, fuentes y logo).
`site/sitio.css` y `site/img/` se mantienen aparte (a mano y con capturas.py).

Uso: python3 scripts/sitio/generar.py
"""
import html
import json
import re
import shutil
from pathlib import Path

from capturas import ID, leer_capturas

RAIZ = Path(__file__).resolve().parents[2]
SITE = RAIZ / "site"
DS = RAIZ / "design-system"

REPO = "https://github.com/indoor-cycling-lab/smart-bike"
URL_BASE = "https://indoor-cycling-lab.github.io/smart-bike/"
ENLACES = {
    "licencia": f"{REPO}/blob/main/LICENSE",
    "planificacion": f"{REPO}/blob/main/docs/planificacion.md",
    "issue": f"{REPO}/issues/new?template=compatibilidad.yml",
    "cafe": "https://ko-fi.com/jmlopezdona",
}
IDIOMAS = ["es", "en"]


def e(texto):
    return html.escape(texto, quote=True)


DISPOSICIONES = {"ancha", "alterna"}


def validar_textos(idioma, t, capturas):
    """Los identificadores y referencias de los textos: acaban en atributos y anclas, así que falla con un mensaje claro si no encajan."""
    def falla(mensaje):
        raise SystemExit(f"site/contenido/{idioma}.json: {mensaje}")

    def ident(valor, donde):
        if not isinstance(valor, str) or not ID.fullmatch(valor):
            falla(f"{donde} = {valor!r} no es un identificador (minúsculas, cifras y guiones)")
        return valor

    def captura(valor, donde):
        if valor not in capturas:
            falla(f"{donde} = {valor!r} no está en site/capturas.json")

    secciones = {ident(t[s]["id"], f"{s}.id") for s in ("setup", "pasos", "funciones", "pantallas", "limites")}
    for k in t["ui"]["nav"]:
        if ident(k, "ui.nav") not in secciones:
            falla(f"ui.nav enlaza a «{k}», que no es ninguna sección")
    for n, item in enumerate(t["funciones"]["items"]):
        ident(item["id"], f"funciones.items[{n}].id")
        if item["disposicion"] not in DISPOSICIONES:
            falla(f"funciones.items[{n}].disposicion = {item['disposicion']!r}; vale {sorted(DISPOSICIONES)}")
        for c in item["imagenes"]:
            captura(c, f"funciones.items[{n}].imagenes")
    for c in t["pantallas"]["imagenes"]:
        captura(c, "pantallas.imagenes")
    captura("rodaje", "hero (fija)")


# --- assets derivados del sistema de diseño ---------------------------------

def generar_assets():
    destino = SITE / "assets"
    (destino / "fonts").mkdir(parents=True, exist_ok=True)
    for woff in (DS / "fonts").glob("*.woff2"):
        shutil.copyfile(woff, destino / "fonts" / woff.name)

    tokens = (DS / "tokens.css").read_text(encoding="utf-8")
    claro = re.search(r'\[data-theme="claro"\] \{(.*?)\n\}', tokens, re.S).group(1)
    # Sin JavaScript manda el tema del sistema: los mismos valores del tema claro, bajo la media query.
    sistema = (
        "\n/* Tema del sistema cuando la página no fija uno (mismos valores que el tema claro). */\n"
        "@media (prefers-color-scheme: light) {\n  :root:not([data-theme]) {" + claro.replace("\n", "\n  ") + "\n  }\n}\n"
    )
    fuentes = [l for l in (DS / "components.css").read_text(encoding="utf-8").splitlines() if l.startswith("@font-face")]
    cabecera = "/* Generado por scripts/sitio/generar.py a partir de design-system/. No editar a mano. */\n"
    (destino / "base.css").write_text(cabecera + "\n".join(fuentes) + "\n" + tokens + sistema, encoding="utf-8")

    (destino / "icono.svg").write_text(icono_pestana(tokens), encoding="utf-8")


CLASES_ICONO = {"cuadro", "rueda-delantera", "rueda-trasera", "radio", "buje", "tambor"}


def formas_icono():
    """Las formas del icono de la app (sin el fondo), con sus clases. Falla si una clase no tiene color en la web."""
    svg = re.sub(r"<!--.*?-->", "", (DS / "identidad" / "icono-app.svg").read_text(encoding="utf-8"), flags=re.S)
    formas = [f for f in re.findall(r"<(?:rect|circle|path)\b[^>]*/>", svg) if 'class="fondo"' not in f]
    clases = set(re.findall(r'class="([\w-]+)"', "".join(formas)))
    if not clases <= CLASES_ICONO:
        raise SystemExit("icono-app.svg: la web no da color a las clases %s" % sorted(clases - CLASES_ICONO))
    return "".join(formas)


def logo():
    """La bici del icono de la app, en línea, con los colores de los tokens (sitio.css)."""
    return f'<svg class="logo" viewBox="18 29 72 50" aria-hidden="true" focusable="false">{formas_icono()}</svg>'


def icono_pestana(tokens):
    """Icono de pestaña: un SVG suelto no ve el CSS de la página, así que lleva los valores del tema oscuro."""
    def valor(nombre):
        return re.search(rf"--{nombre}: (#[0-9a-fA-F]{{6}});", tokens).group(1)

    svg = (DS / "identidad" / "icono-app.svg").read_text(encoding="utf-8")
    estilo = (
        f"<style>.fondo{{fill:{valor('bg')}}}.cuadro{{stroke:{valor('ink')}}}"
        f".rueda-delantera{{stroke:{valor('line-strong')}}}"
        f".rueda-trasera,.radio{{stroke:{valor('gear-fill')}}}"
        f".buje{{fill:{valor('gear-fill')}}}.tambor{{fill:{valor('line-strong')}}}</style>"
    )
    return svg.replace("</svg>", estilo + "</svg>")


# --- piezas de la página ------------------------------------------------------

def imagen(c, idioma, cargar_ya):
    """Las dos variantes de tema de una captura, en el idioma de la página; CSS muestra la del tema activo."""
    alt = e(c["alt"][idioma])
    atributos = f'width="{c["ancho"] * 2}" height="{c["alto"] * 2}" alt="{alt}"' + ("" if cargar_ya else ' loading="lazy" decoding="async"')
    oscuro = f'<img class="img-oscuro" src="../img/{e(c["id"])}-{idioma}-oscuro.png" {atributos}>'
    claro = f'<img class="img-claro" src="../img/{e(c["id"])}-{idioma}-claro.png" {atributos}>'
    return oscuro + claro


def marco(c, idioma, cargar_ya=False):
    forma = "horizontal" if c["ancho"] > c["alto"] else "vertical"
    return f'<figure class="marco marco--{forma}">{imagen(c, idioma, cargar_ya)}</figure>'


def cabecera(t, idioma, otro):
    ui = t["ui"]
    enlaces = "".join(f'<li><a href="#{e(k)}">{e(v)}</a></li>' for k, v in ui["nav"].items())
    return f"""<header class="cabecera">
  <a class="saltar" href="#principal">{e(ui["saltar"])}</a>
  <div class="cabecera__fila">
    <a class="marca" href="./" aria-label="{e(ui["inicio"])}">{logo()}<span>Smart Bike</span></a>
    <nav class="nav" aria-label="{e(ui["nav_etiqueta"])}"><ul>{enlaces}</ul></nav>
    <div class="herramientas">
      <a class="boton boton--discreto" id="enlace-idioma" href="../{otro}/" hreflang="{otro}" lang="{otro}" aria-label="{e(ui["idioma_etiqueta"])}">{e(ui["idioma_texto"])}</a>
      <button class="boton boton--discreto" id="tema" type="button" hidden aria-label="{e(ui["tema_etiqueta"])}" data-oscuro="{e(ui["tema_oscuro"])}" data-claro="{e(ui["tema_claro"])}"><span class="visible-solo-oscuro">{e(ui["tema_claro"])}</span><span class="visible-solo-claro">{e(ui["tema_oscuro"])}</span></button>
    </div>
  </div>
</header>"""


def hero(t, capturas, idioma):
    h = t["hero"]
    return f"""<section class="hero" aria-labelledby="titulo-hero">
  <div class="hero__texto">
    <p class="eyebrow">{e(h["eyebrow"])}</p>
    <h1 id="titulo-hero">{e(h["titulo"])}</h1>
    <p class="hero__lema">{e(h["texto"])}</p>
    <p class="hero__acciones"><a class="boton boton--principal" href="{REPO}">{e(h["cta_repo"])}</a><a class="boton" href="#funciones">{e(h["cta_mas"])}</a></p>
    <p class="hero__nota">{e(h["nota"])}</p>
  </div>
  <div class="hero__imagen">{marco(capturas["rodaje"], idioma, cargar_ya=True)}</div>
</section>"""


def cifras(t):
    c = t["cifras"]
    items = "".join(
        f'<li><span class="cifra__valor">{e(i["valor"])}</span><span class="cifra__texto">{e(i["texto"])}</span></li>'
        for i in c["items"]
    )
    return f"""<section class="cifras" aria-label="{e(c["etiqueta"])}">
  <ul class="cifras__lista">{items}</ul>
</section>"""


def setup(t):
    s = t["setup"]
    disp = "".join(
        f'<li class="nodo"><span class="nodo__nombre">{e(d["nombre"])}</span><span class="nodo__detalle">{e(d["detalle"])}</span><span class="enlace">{e(d["via"])}</span></li>'
        for d in s["dispositivos"]
    )
    return f"""<section class="seccion" id="{e(s["id"])}" aria-labelledby="titulo-setup">
  <h2 id="titulo-setup">{e(s["titulo"])}</h2>
  <p class="seccion__intro">{e(s["intro"])}</p>
  <div class="esquema" role="group" aria-label="{e(s["resumen"])}">
    <div class="esquema__col"><h3 class="etiqueta">{e(s["etiqueta_dispositivos"])}</h3><ul class="esquema__lista" aria-hidden="true">{disp}</ul></div>
    <div class="esquema__flecha" aria-hidden="true"></div>
    <div class="esquema__col esquema__col--central" aria-hidden="true"><h3 class="etiqueta">{e(s["etiqueta_central"])}</h3><div class="nodo nodo--central"><span class="nodo__nombre">{e(s["central"]["nombre"])}</span><span class="nodo__detalle">{e(s["central"]["detalle"])}</span></div></div>
    <div class="esquema__flecha esquema__flecha--wifi" aria-hidden="true"><span class="enlace">{e(s["destino"]["via"])}</span></div>
    <div class="esquema__col" aria-hidden="true"><h3 class="etiqueta">{e(s["etiqueta_destino"])}</h3><div class="nodo nodo--destino"><span class="nodo__nombre">{e(s["destino"]["nombre"])}</span><span class="nodo__detalle">{e(s["destino"]["detalle"])}</span></div></div>
  </div>
</section>"""


def pasos(t):
    p = t["pasos"]
    items = "".join(f'<li class="paso"><h3>{e(i["titulo"])}</h3><p>{e(i["texto"])}</p></li>' for i in p["items"])
    return f"""<section class="seccion" id="{e(p["id"])}" aria-labelledby="titulo-pasos">
  <h2 id="titulo-pasos">{e(p["titulo"])}</h2>
  <p class="seccion__intro">{e(p["intro"])}</p>
  <ol class="pasos">{items}</ol>
</section>"""


def funciones(t, capturas, idioma):
    f = t["funciones"]
    bloques = []
    alternas = 0
    for item in f["items"]:
        puntos = "".join(f"<li>{e(p)}</li>" for p in item["puntos"])
        imgs = "".join(marco(capturas[c], idioma, cargar_ya=False) for c in item["imagenes"])
        # «ancha»: texto arriba y capturas a todo el ancho; «alterna»: texto e imagen a izquierda y derecha, alternando.
        if item["disposicion"] == "ancha":
            clase = " funcion--ancha"
        else:
            clase = " funcion--inversa" if alternas % 2 else ""
            alternas += 1
        bloques.append(
            f"""<article class="funcion{clase}" id="{e(item["id"])}" aria-labelledby="t-{e(item["id"])}">
  <div class="funcion__texto">
    <p class="eyebrow">{e(item["kicker"])}</p>
    <h3 id="t-{e(item["id"])}">{e(item["titulo"])}</h3>
    <p>{e(item["texto"])}</p>
    <ul class="puntos">{puntos}</ul>
  </div>
  <div class="funcion__imagenes funcion__imagenes--{len(item["imagenes"])}">{imgs}</div>
</article>"""
        )
    return f"""<section class="seccion" id="{e(f["id"])}" aria-labelledby="titulo-funciones">
  <h2 id="titulo-funciones">{e(f["titulo"])}</h2>
  {"".join(bloques)}
  <p class="pie-captura">{e(t["ui"]["pie_captura"])}</p>
</section>"""


def pantallas(t, capturas, idioma):
    p = t["pantallas"]
    imgs = "".join(marco(capturas[c], idioma) for c in p["imagenes"])
    return f"""<section class="seccion" id="{e(p["id"])}" aria-labelledby="titulo-pantallas">
  <h2 id="titulo-pantallas">{e(p["titulo"])}</h2>
  <p class="seccion__intro">{e(p["texto"])}</p>
  <div class="galeria">{imgs}</div>
</section>"""


def limites(t):
    l = t["limites"]
    items = "".join(f'<li><h3>{e(i["titulo"])}</h3><p>{e(i["texto"])}</p></li>' for i in l["items"])
    return f"""<section class="seccion" id="{e(l["id"])}" aria-labelledby="titulo-limites">
  <h2 id="titulo-limites">{e(l["titulo"])}</h2>
  <p class="seccion__intro">{e(l["intro"])}</p>
  <ul class="limites">{items}</ul>
  <p class="limites__peticion">{e(l["peticion"])}</p>
  <p class="acciones"><a class="boton boton--principal" href="{ENLACES["issue"]}">{e(l["cta_issue"])}</a><a class="boton" href="{ENLACES["planificacion"]}">{e(l["cta"])}</a></p>
</section>"""


def pie(t):
    p = t["pie"]
    return f"""<footer class="pie">
  <p class="pie__cafe">{e(p["cafe"])} <a class="boton boton--discreto" href="{ENLACES["cafe"]}">{e(p["cafe_enlace"])}</a></p>
  <p>{e(p["marcas"])}</p>
  <p>{e(p["licencia"])} <a href="{ENLACES["licencia"]}">{e(p["licencia_enlace"])}</a> · <a href="{REPO}">{e(p["repo"])}</a></p>
</footer>"""


SCRIPT_TEMA = """<script>
(function () {
  var d = document.documentElement;
  try { var t = localStorage.getItem("sb-tema"); if (t === "claro" || t === "rodaje") d.setAttribute("data-theme", t); } catch (e) {}
})();
</script>"""

SCRIPT_PAGINA = """<script>
(function () {
  var d = document.documentElement;
  var boton = document.getElementById("tema");
  function claro() {
    var t = d.getAttribute("data-theme");
    return t ? t === "claro" : matchMedia("(prefers-color-scheme: light)").matches;
  }
  if (boton) {
    boton.hidden = false;
    boton.addEventListener("click", function () {
      var nuevo = claro() ? "rodaje" : "claro";
      d.setAttribute("data-theme", nuevo);
      try { localStorage.setItem("sb-tema", nuevo); } catch (e) {}
    });
  }
  var idioma = document.getElementById("enlace-idioma");
  if (idioma) {
    var base = idioma.getAttribute("href").split("#")[0];
    var sincronizar = function () { idioma.setAttribute("href", base + location.hash); };
    window.addEventListener("hashchange", sincronizar);
    sincronizar();
  }
})();
</script>"""


def pagina(idioma, textos, capturas):
    t = textos[idioma]
    otro = [i for i in IDIOMAS if i != idioma][0]
    alternas = "".join(
        f'<link rel="alternate" hreflang="{i}" href="{URL_BASE}{i}/">' for i in IDIOMAS
    ) + f'<link rel="alternate" hreflang="x-default" href="{URL_BASE}">'
    return f"""<!doctype html>
<html lang="{idioma}">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>{e(t["meta"]["titulo"])}</title>
<meta name="description" content="{e(t["meta"]["descripcion"])}">
<meta name="color-scheme" content="dark light">
<link rel="canonical" href="{URL_BASE}{idioma}/">
{alternas}
<meta property="og:title" content="{e(t["meta"]["titulo"])}">
<meta property="og:description" content="{e(t["meta"]["descripcion"])}">
<meta property="og:type" content="website">
<meta property="og:url" content="{URL_BASE}{idioma}/">
<link rel="icon" href="../assets/icono.svg" type="image/svg+xml">
<link rel="stylesheet" href="../assets/base.css">
<link rel="stylesheet" href="../sitio.css">
{SCRIPT_TEMA}
</head>
<body>
{cabecera(t, idioma, otro)}
<main id="principal">
{hero(t, capturas, idioma)}
{cifras(t)}
{setup(t)}
{pasos(t)}
{funciones(t, capturas, idioma)}
{pantallas(t, capturas, idioma)}
{limites(t)}
</main>
{pie(t)}
{SCRIPT_PAGINA}
</body>
</html>
"""


def raiz():
    return f"""<!doctype html>
<html lang="es">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>Smart Bike</title>
<meta name="description" content="Smart Bike · Español / English">
<meta name="color-scheme" content="dark light">
<link rel="canonical" href="{URL_BASE}">
<link rel="alternate" hreflang="es" href="{URL_BASE}es/">
<link rel="alternate" hreflang="en" href="{URL_BASE}en/">
<link rel="alternate" hreflang="x-default" href="{URL_BASE}">
<link rel="icon" href="assets/icono.svg" type="image/svg+xml">
<link rel="stylesheet" href="assets/base.css">
<link rel="stylesheet" href="sitio.css">
<script>
(function () {{
  var langs = navigator.languages && navigator.languages.length ? navigator.languages : [navigator.language || "es"];
  for (var i = 0; i < langs.length; i++) {{
    var l = String(langs[i]).toLowerCase().slice(0, 2);
    if (l === "es" || l === "en") {{ location.replace(l + "/" + location.hash); return; }}
  }}
  location.replace("es/" + location.hash);
}})();
</script>
</head>
<body class="raiz">
<main class="raiz__caja">
  <h1>Smart Bike</h1>
  <p class="raiz__enlaces"><a class="boton boton--principal" href="es/" hreflang="es" lang="es">Español</a><a class="boton boton--principal" href="en/" hreflang="en" lang="en">English</a></p>
</main>
</body>
</html>
"""


def main():
    generar_assets()
    textos = {i: json.loads((SITE / "contenido" / f"{i}.json").read_text(encoding="utf-8")) for i in IDIOMAS}
    capturas = {c["id"]: c for c in leer_capturas()["capturas"]}
    for i in IDIOMAS:
        validar_textos(i, textos[i], capturas)
    for i in IDIOMAS:
        (SITE / i).mkdir(exist_ok=True)
        (SITE / i / "index.html").write_text(pagina(i, textos, capturas), encoding="utf-8")
    (SITE / "index.html").write_text(raiz(), encoding="utf-8")
    (SITE / ".nojekyll").write_text("", encoding="utf-8")
    print("site/ generado")


if __name__ == "__main__":
    main()
