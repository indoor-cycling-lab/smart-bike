#!/usr/bin/env python3
"""Genera design-system/tokens.css, design-system/styleguide/index.html y los derivados de la app.

Fuentes: design-system/tokens.json, design-system/fonts/*.ttf,
design-system/icons.svg y design-system/components/<Componente>/ (README.md y
preview.html). Después
comprueba el contraste de los tokens (contraste.py) y sale con error si algún
par no llega a su mínimo. Los derivados de la app (Tokens.kt, res/font, el
color de la ventana y los iconos de res/drawable) solo se escriben si el contraste pasa, para que la app no
reciba un color sin comprobar. No edites a mano los ficheros generados: cambia
las fuentes y ejecuta

    python3 scripts/design-system/generar.py
"""
import html
import json
import re
import struct
import shutil
import sys
from pathlib import Path

import contraste

RAIZ = Path(__file__).resolve().parents[2]
DS = RAIZ / "design-system"
APP = RAIZ / "app" / "src" / "main"
PAQUETE = "app.smartbike.ui.tema"
TOKENS_KT = APP / "kotlin" / Path(*PAQUETE.split(".")) / "Tokens.kt"
RES_FONT = APP / "res" / "font"
COLOR_VENTANA = APP / "res" / "values" / "color_ventana.xml"
RES_DRAWABLE = APP / "res" / "drawable"
AVISO_GENERADO = "Generado por scripts/design-system/generar.py"

# Peso de cada fichero de fuente según su sufijo (BarlowCondensed-SemiBold.ttf → 600).
PESOS = {"Regular": 400, "Medium": 500, "SemiBold": 600, "Bold": 700}

# Orden de los grupos en la guía; los componentes sin grupo van al final.
GRUPOS = ["Rodaje", "Ajustes", "Pantallas", "Square", "Diagnóstico", "Patrones", "Identidad"]


def valor(token, tema):
    v = token["value"]
    if isinstance(v, str):
        return "var(--%s)" % v.strip("{}")
    return v[tema]


def generar_tokens(t):
    colores = t["color"]["tokens"]
    lineas = [
        "/* Generado por scripts/design-system/generar.py a partir de tokens.json. No editar a mano. */",
        "/* Tema por defecto: rodaje (oscuro). El tema claro se aplica con data-theme=\"claro\" en cualquier contenedor. */",
        ":root, [data-theme=\"rodaje\"] {",
    ]
    lineas += ["  --%s: %s;" % (c["name"], valor(c, "rodaje")) for c in colores]
    lineas += ["  --font-%s: %s;" % (k, v) for k, v in t["type"]["families"].items()]
    for grupo in ("spacing", "radius", "size"):
        lineas += ["  --%s: %s;" % (x["name"], x["value"]) for x in t[grupo]["tokens"]]
    lineas += ["  color-scheme: dark;", "}", "[data-theme=\"claro\"] {"]
    lineas += ["  --%s: %s;" % (c["name"], c["value"]["claro"]) for c in colores if not isinstance(c["value"], str)]
    lineas += ["  color-scheme: light;", "}", ""]
    (DS / "tokens.css").write_text("\n".join(lineas), encoding="utf-8")


def leer_componente(carpeta):
    fuente = (carpeta / "preview.html").read_text(encoding="utf-8")
    m = re.search(r"@dsCard([^>]*)-->", fuente)
    attrs = m.group(1) if m else ""
    grupo = re.search(r'group="([^"]+)"', attrs)
    subtitulo = re.search(r'subtitle="([^"]+)"', attrs)
    estilos = "\n".join(re.findall(r"<style>(.*?)</style>", fuente, re.S))
    cuerpo = re.search(r"<body[^>]*>(.*)</body>", fuente, re.S).group(1).strip()
    # Las vistas previas enlazan el sprite desde components/<Componente>/; la guía, desde styleguide/.
    cuerpo = cuerpo.replace('href="../../icons.svg#', 'href="../icons.svg#')
    resumen = ""
    readme = carpeta / "README.md"
    if readme.exists():
        parrafos = [p.strip() for p in readme.read_text(encoding="utf-8").split("\n\n")]
        resumen = next((p for p in parrafos[1:] if p and not p.startswith(("#", "**", "-", "|"))), "")
    return {
        "nombre": carpeta.name,
        "grupo": grupo.group(1) if grupo else ("Portada" if carpeta.name == "Cover" else "Otros"),
        "subtitulo": subtitulo.group(1) if subtitulo else "",
        "estilos": estilos,
        "cuerpo": cuerpo,
        "resumen": resumen,
        "readme": readme.exists(),
    }


def por_tema(cuerpo, tema):
    """Cada demo sale una vez por tema: prefija name, id y for para que los grupos de radio y las etiquetas no se mezclen."""
    return re.sub(r'\b(name|id|for)="([^"]+)"', lambda m: '%s="%s-%s"' % (m.group(1), tema, m.group(2)), cuerpo)


def texto_md(s):
    s = html.escape(s)
    s = re.sub(r"`([^`]+)`", r"<code>\1</code>", s)
    return re.sub(r"\*\*([^*]+)\*\*", r"<strong>\1</strong>", s)


def generar_guia(t):
    comps = [leer_componente(c) for c in sorted((DS / "components").iterdir()) if (c / "preview.html").exists()]
    comps = [c for c in comps if c["nombre"] != "Cover"]
    orden = {g: i for i, g in enumerate(GRUPOS)}
    comps.sort(key=lambda c: (orden.get(c["grupo"], 99), c["nombre"]))

    swatches = []
    for c in t["color"]["tokens"]:
        swatches.append(
            '<div class="sg-swatch"><span class="sg-chip" style="background: var(--%s)"></span>'
            '<code>%s</code><span class="sq-caption">%s</span></div>' % (c["name"], c["name"], texto_md(c["usage"]))
        )
    estilos_texto = []
    for g in t["type"]["groups"]:
        for s in g["styles"]:
            css = "font-family: var(--font-%s); font-size: %s; line-height: %s; font-weight: %s;" % (
                g["family"], s["fontSize"], s["lineHeight"], s["fontWeight"])
            if s.get("letterSpacing"):
                css += " letter-spacing: %s;" % s["letterSpacing"]
            if s["name"] == "label":
                css += " text-transform: uppercase;"
            estilos_texto.append(
                '<div class="sg-type"><code>%s · %s/%s</code><span style="%s">%s</span><span class="sq-caption">%s</span></div>'
                % (s["name"], s["fontSize"], s["lineHeight"], css, html.escape(s["sample"]), texto_md(s["usage"])))

    secciones, indice, grupo_actual = [], [], None
    for c in comps:
        if c["grupo"] != grupo_actual:
            grupo_actual = c["grupo"]
            secciones.append('<h2 class="sg-group">%s</h2>' % html.escape(grupo_actual))
        indice.append('<a href="#%s">%s</a>' % (c["nombre"], c["nombre"]))
        enlace = ' · <a href="../components/%s/README.md">pautas</a>' % c["nombre"] if c["readme"] else ""
        temas = "".join(
            '<div class="sg-demo" data-theme="%s"><span class="sg-demo__tag">%s</span>%s</div>' % (tema, etiqueta, por_tema(c["cuerpo"], tema))
            for tema, etiqueta in (("rodaje", "Rodaje"), ("claro", "Claro")))
        secciones.append(
            '<section class="sg-comp" id="%s"><h3>%s</h3><p class="sq-caption">%s%s</p>%s<div class="sg-demos">%s</div></section>'
            % (c["nombre"], c["nombre"], texto_md(c["subtitulo"]), enlace,
               '<p class="sg-summary">%s</p>' % texto_md(c["resumen"]) if c["resumen"] else "", temas))

    pagina = PLANTILLA.format(
        estilos="\n".join(c["estilos"] for c in comps),
        indice=" ".join(indice),
        swatches="\n".join(swatches),
        tipos="\n".join(estilos_texto),
        secciones="\n".join(secciones),
    )
    (DS / "styleguide").mkdir(exist_ok=True)
    (DS / "styleguide" / "index.html").write_text(pagina, encoding="utf-8")
    return len(comps)


PLANTILLA = """<!doctype html>
<html lang="es">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>Guía de estilo · Smart Bike</title>
<!-- Generado por scripts/design-system/generar.py. No editar a mano. -->
<link rel="stylesheet" href="../tokens.css">
<link rel="stylesheet" href="../components.css">
<style>
body {{ margin: 0; }}
.sg {{ max-width: 1120px; margin: 0 auto; padding: 24px 16px 64px; display: flex; flex-direction: column; gap: 24px; }}
.sg > h1 {{ margin: 0; font-family: var(--font-numeric); font-size: 56px; line-height: 56px; color: var(--gear); }}
.sg > h2 {{ margin: 24px 0 0; font-size: 24px; line-height: 32px; }}
.sg-index {{ display: flex; flex-wrap: wrap; gap: 8px 16px; font-size: 14px; }}
.sg > p a, .sg-index a, .sg-comp > p a {{ color: var(--gear); }}
.sg-swatches {{ display: grid; grid-template-columns: repeat(auto-fill, minmax(240px, 1fr)); gap: 12px; }}
.sg-swatch {{ display: grid; grid-template-columns: 40px 1fr; gap: 2px 12px; align-items: center; }}
.sg-swatch .sg-chip {{ grid-row: span 2; width: 40px; height: 40px; border-radius: 8px; border: 1px solid var(--line); }}
.sg-type {{ display: grid; grid-template-columns: 200px 1fr; gap: 4px 16px; align-items: baseline; padding: 8px 0; border-bottom: 1px solid var(--line); }}
.sg-type .sq-caption {{ grid-column: 2; }}
.sg-group {{ padding-top: 16px; border-top: 2px solid var(--line-strong); }}
.sg-comp > h3 {{ margin: 0; font-size: 18px; }}
.sg-comp > p {{ margin: 4px 0; }}
.sg-summary {{ max-width: 72ch; }}
.sg-demos {{ display: grid; gap: 12px; margin-top: 12px; }}
.sg-demo {{ position: relative; overflow-x: auto; padding-top: 24px; border: 1px solid var(--line); border-radius: 8px; background: var(--bg); color: var(--ink); font-family: var(--font-sans); }}
.sg-demo__tag {{ position: absolute; top: 6px; left: 12px; font-size: 11px; font-weight: 600; letter-spacing: .08em; text-transform: uppercase; color: var(--ink-muted); }}
@media (max-width: 640px) {{ .sg-type {{ grid-template-columns: 1fr; }} .sg-type .sq-caption {{ grid-column: 1; }} }}
{estilos}
</style>
</head>
<body>
<main class="sg">
  <h1>Smart Bike</h1>
  <p>Guía de estilo generada a partir de <code>tokens.json</code> y de <code>components/</code>. Los principios, la voz y las reglas generales están en <a href="../README.md">README.md</a>; las pautas de cada componente, en su <code>README.md</code>. Cada componente se muestra en los dos temas.</p>
  <nav class="sg-index" aria-label="Componentes">{indice}</nav>
  <h2>Color</h2>
  <div class="sg-swatches">{swatches}</div>
  <h2>Tipografía</h2>
  <div>{tipos}</div>
  {secciones}
</main>
</body>
</html>
"""


def camel(nombre):
    """on-gear-fill → onGearFill, space-4 → space4."""
    partes = nombre.split("-")
    return partes[0] + "".join(p[:1].upper() + p[1:] for p in partes[1:])


def kdoc(texto, sangria="    "):
    return "%s/** %s */" % (sangria, texto.replace("*/", "* /"))


def cadena(texto):
    return '"%s"' % texto.replace("\\", "\\\\").replace('"', '\\"').replace("$", "\\$")


def px(valor):
    """'168px' → '168'; '-0.02em' no pasa por aquí."""
    numero = float(valor.removesuffix("px"))
    return ("%d" % numero) if numero == int(numero) else ("%g" % numero)


def fuentes_de_familia(pila):
    """Ficheros TTF de la primera familia de una pila CSS: "Barlow Condensed", … → BarlowCondensed-*.ttf."""
    familia = re.match(r'\s*"?([^",]+)"?', pila).group(1).strip()
    ficheros = []
    for ttf in sorted((DS / "fonts").glob(familia.replace(" ", "") + "-*.ttf")):
        sufijo = ttf.stem.split("-", 1)[1]
        if sufijo not in PESOS:
            raise SystemExit("Peso desconocido en %s: añade %r a PESOS en generar.py" % (ttf.name, sufijo))
        ficheros.append((ttf, PESOS[sufijo]))
    if not ficheros:
        raise SystemExit("No hay TTF de %r en design-system/fonts/" % familia)
    return familia, ficheros


def altura_natural(ttf):
    """Alto de línea natural de una fuente en em: (ascendente − descendente) / unidades por em de su tabla `hhea`,
    que es lo que usa Android sin relleno de fuente."""
    d = ttf.read_bytes()
    tablas = {}
    for i in range(struct.unpack(">H", d[4:6])[0]):
        etiqueta, _, desplazamiento, _ = struct.unpack(">4sIII", d[12 + 16 * i:28 + 16 * i])
        tablas[etiqueta] = desplazamiento
    unidades = struct.unpack(">H", d[tablas[b"head"] + 18:tablas[b"head"] + 20])[0]
    ascendente, descendente = struct.unpack(">hh", d[tablas[b"hhea"] + 4:tablas[b"hhea"] + 8])
    return (ascendente - descendente) / unidades


def recurso_fuente(ttf):
    """BarlowCondensed-SemiBold.ttf → barlow_condensed_semibold (nombre válido de recurso de Android)."""
    familia, peso = ttf.stem.split("-", 1)
    return (re.sub(r"(?<!^)(?=[A-Z])", "_", familia) + "_" + peso).lower()


def generar_kotlin(t):
    """Devuelve el texto de Tokens.kt y los ficheros de fuente que necesita, sin escribir nada."""
    colores = t["color"]["tokens"]
    familias = {nombre: fuentes_de_familia(pila) for nombre, pila in t["type"]["families"].items()}
    L = [
        "// Generado por scripts/design-system/generar.py a partir de design-system/tokens.json. No editar a mano:",
        "// cambia tokens.json y ejecuta python3 scripts/design-system/generar.py. TokensSincronizadosTest",
        "// falla si este fichero no coincide con tokens.json.",
        "package %s" % PAQUETE,
        "",
        "import androidx.compose.ui.graphics.Color",
        "import androidx.compose.ui.text.PlatformTextStyle",
        "import androidx.compose.ui.text.TextStyle",
        "import androidx.compose.ui.text.font.Font",
        "import androidx.compose.ui.text.font.FontFamily",
        "import androidx.compose.ui.text.font.FontWeight",
        "import androidx.compose.ui.text.style.LineHeightStyle",
        "import androidx.compose.ui.unit.Dp",
        "import androidx.compose.ui.unit.dp",
        "import androidx.compose.ui.unit.em",
        "import androidx.compose.ui.unit.sp",
        "import app.smartbike.R",
        "",
        "/** Colores de un tema. Los valores de cada tema están en [ColoresRodaje] y [ColoresClaro]. */",
        "data class SqColores(",
    ]
    for c in colores:
        L += [kdoc(c["usage"]), "    val %s: Color," % camel(c["name"])]
    L += [") {", "    /** Cada color por su nombre en tokens.json, leído de las propiedades. */",
          "    fun porNombre(): Map<String, Color> = linkedMapOf("]
    L += ["        %s to %s," % (cadena(c["name"]), camel(c["name"])) for c in colores]
    L += ["    )", "}", ""]
    for tema, nombre in (("rodaje", "ColoresRodaje"), ("claro", "ColoresClaro")):
        valor = contraste.colores(tema, colores)
        L += ["/** Tema %s. */" % next(x["name"] for x in t["color"]["themes"] if x["id"] == tema),
              "val %s = SqColores(" % nombre]
        L += ["    %s = Color(0xFF%s)," % (camel(c["name"]), valor(c["name"]).lstrip("#").upper()) for c in colores]
        L += [")", ""]

    L += ["/** Familias tipográficas, empaquetadas en res/font. */", "object SqFamilias {"]
    for nombre, (familia, ficheros) in familias.items():
        fuentes = ", ".join("Font(R.font.%s, FontWeight(%d))" % (recurso_fuente(f), p) for f, p in ficheros)
        L += [kdoc(familia), "    val %s = FontFamily(%s)" % (camel(nombre), fuentes)]
    L += ["", "    fun porNombre(): Map<String, FontFamily> = linkedMapOf("]
    L += ["        %s to %s," % (cadena(n), camel(n)) for n in familias]
    L += ["    )", "}", "",
          "// Interlineado repartido arriba y abajo y sin relleno de fuente, como en CSS: la caja de una línea mide su",
          "// interlineado. Si es mayor que el alto natural de la fuente, Trim.None reparte el espacio que sobra; si es",
          "// menor (las cifras grandes), Compose no encoge la línea salvo con Trim.Both y Mode.Tight.",
          "private val InterlineadoCss = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.None, LineHeightStyle.Mode.Fixed)",
          "private val InterlineadoCssAjustado = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.Both, LineHeightStyle.Mode.Tight)",
          "private val SinRellenoDeFuente = PlatformTextStyle(includeFontPadding = false)", "",
          "/** Estilos tipográficos. La mayúscula de `label` la aplica quien lo usa, como text-transform en CSS. */",
          "object SqTipografia {"]
    estilos = []
    for g in t["type"]["groups"]:
        for s in g["styles"]:
            peso = int(s["fontWeight"])
            if peso not in [p for _, p in familias[g["family"]][1]]:
                raise SystemExit("El estilo %s usa el peso %d, que no tiene TTF en la familia %s" % (s["name"], peso, g["family"]))
            estilos.append((g, s))
            L += [kdoc(s["usage"]), "    val %s = TextStyle(" % camel(s["name"]),
                  "        fontFamily = SqFamilias.%s," % camel(g["family"]),
                  "        fontWeight = FontWeight(%d)," % peso,
                  "        fontSize = %s.sp," % px(s["fontSize"]),
                  "        lineHeight = %s.sp," % px(s["lineHeight"])]
            if s.get("letterSpacing"):
                em = float(s["letterSpacing"].removesuffix("em"))
                L += ["        letterSpacing = (%g).em," % em]
            if g["family"] == "numeric":
                L += ['        fontFeatureSettings = "tnum",']
            natural = altura_natural(familias[g["family"]][1][0][0]) * float(px(s["fontSize"]))
            interlineado = "InterlineadoCssAjustado" if float(px(s["lineHeight"])) < natural else "InterlineadoCss"
            L += ["        lineHeightStyle = %s," % interlineado, "        platformStyle = SinRellenoDeFuente,", "    )"]
    L += ["", "    fun porNombre(): Map<String, TextStyle> = linkedMapOf("]
    L += ["        %s to %s," % (cadena(s["name"]), camel(s["name"])) for _, s in estilos]
    L += ["    )", "", "    /** Texto de ejemplo de cada estilo, el mismo que muestra la guía. */",
          "    val ejemplos: Map<String, String> = linkedMapOf("]
    L += ["        %s to %s," % (cadena(s["name"]), cadena(s["sample"])) for _, s in estilos]
    L += ["    )", "}", ""]

    for grupo, objeto, doc in (("spacing", "SqEspacios", "Espaciados"), ("radius", "SqRadios", "Radios"), ("size", "SqTamanos", "Tamaños")):
        L += ["/** %s en dp. %s */" % (doc, t[grupo]["note"]), "object %s {" % objeto]
        for x in t[grupo]["tokens"]:
            L += [kdoc(x["usage"]), "    val %s = %s.dp" % (camel(x["name"]), px(x["value"]))]
        L += ["", "    fun porNombre(): Map<String, Dp> = linkedMapOf("]
        L += ["        %s to %s," % (cadena(x["name"]), camel(x["name"])) for x in t[grupo]["tokens"]]
        L += ["    )", "}", ""]
    ttfs = [f for _, (_, ficheros) in familias.items() for f, _ in ficheros]
    return "\n".join(L), ttfs


def escribir_app(t, kotlin, ttfs):
    TOKENS_KT.parent.mkdir(parents=True, exist_ok=True)
    TOKENS_KT.write_text(kotlin, encoding="utf-8")
    # res/font es entero generado: se quitan las fuentes que ya no están en el sistema de diseño.
    RES_FONT.mkdir(parents=True, exist_ok=True)
    destinos = {recurso_fuente(f) + ".ttf": f for f in ttfs}
    for viejo in RES_FONT.iterdir():
        if viejo.name not in destinos:
            viejo.unlink()
    for nombre, origen in destinos.items():
        shutil.copyfile(origen, RES_FONT / nombre)
    # Fondo de la ventana antes de que Compose pinte, para que no destelle en blanco.
    bg = contraste.colores("rodaje", t["color"]["tokens"])("bg")
    COLOR_VENTANA.write_text(
        '<?xml version="1.0" encoding="utf-8"?>\n'
        "<!-- Generado por scripts/design-system/generar.py a partir de tokens.json. No editar a mano. -->\n"
        '<resources>\n    <color name="ventana_bg">%s</color>\n</resources>\n' % bg, encoding="utf-8")


def leer_iconos():
    """Iconos de icons.svg como {nombre: pathData}, sin el ejemplo del comentario."""
    svg = re.sub(r"<!--.*?-->", "", (DS / "icons.svg").read_text(encoding="utf-8"), flags=re.S)
    iconos = {}
    for nombre, viewbox, cuerpo in re.findall(r'<symbol id="i-([a-z0-9_]+)" viewBox="([^"]+)">(.*?)</symbol>', svg, re.S):
        caminos = re.findall(r'<path d="([^"]+)"\s*/>', cuerpo)
        if viewbox != "0 -960 960 960" or len(caminos) != 1:
            raise SystemExit("icons.svg: i-%s debe tener viewBox \"0 -960 960 960\" y un solo <path>" % nombre)
        iconos[nombre] = caminos[0]
    return iconos


def vector_drawable(nombre, camino):
    # El viewBox empieza en y = -960: se desplaza el dibujo 960 hacia abajo para que quepa en el viewport.
    return (
        '<?xml version="1.0" encoding="utf-8"?>\n'
        "<!-- %s a partir de icons.svg (i-%s). No editar a mano. -->\n"
        '<vector xmlns:android="http://schemas.android.com/apk/res/android"\n'
        '    android:width="24dp"\n'
        '    android:height="24dp"\n'
        '    android:viewportWidth="960"\n'
        '    android:viewportHeight="960">\n'
        '    <group android:translateY="960">\n'
        '        <path\n'
        '            android:fillColor="#FF000000"\n'
        '            android:pathData="%s" />\n'
        "    </group>\n"
        "</vector>\n" % (AVISO_GENERADO, nombre, camino)
    )


def escribir_iconos(iconos):
    RES_DRAWABLE.mkdir(parents=True, exist_ok=True)
    # Solo se borran los iconos generados que ya no están en icons.svg; el resto de drawables no se toca.
    for viejo in RES_DRAWABLE.glob("ic_*.xml"):
        if viejo.stem[3:] not in iconos and AVISO_GENERADO in viejo.read_text(encoding="utf-8"):
            viejo.unlink()
    for nombre, camino in iconos.items():
        (RES_DRAWABLE / ("ic_%s.xml" % nombre)).write_text(vector_drawable(nombre, camino), encoding="utf-8")


ZONA_SEGURA = 33.0  # radio (dp) del círculo seguro del icono adaptativo: 66 dp de diámetro en el lienzo de 108
LIENZO_ICONO = 108.0


def leer_formas(nombre):
    """Formas de design-system/identidad/<nombre> (rect, circle y path con M y L absolutos) como lista de dicts.

    Cada forma lleva su clase, su tipo y su geometría: rect (x, y, w, h, rx), circle (cx, cy, r) o path (puntos);
    y trazo (stroke-width, 0 si no tiene). El fondo no se incluye.
    """
    svg = re.sub(r"<!--.*?-->", "", (DS / "identidad" / nombre).read_text(encoding="utf-8"), flags=re.S)
    formas = []
    for etiqueta, atributos in re.findall(r"<(rect|circle|path)\b([^>]*)/>", svg):
        atrs = dict(re.findall(r'([\w-]+)="([^"]*)"', atributos))
        if atrs.get("class") == "fondo":
            continue
        forma = {"clase": atrs["class"], "tipo": etiqueta, "trazo": float(atrs.get("stroke-width", 0))}
        if etiqueta == "rect":
            forma.update(x=float(atrs["x"]), y=float(atrs["y"]), w=float(atrs["width"]), h=float(atrs["height"]),
                         rx=float(atrs.get("rx", 0)))
        elif etiqueta == "circle":
            forma.update(cx=float(atrs["cx"]), cy=float(atrs["cy"]), r=float(atrs["r"]))
        else:
            forma["subtrazados"] = leer_trazado(nombre, atrs["d"])
        formas.append(forma)
    return formas


def leer_trazado(nombre, d):
    """Subtrazados de un path con solo M y L absolutos: lista de listas de puntos."""
    subtrazados = []
    for orden, numeros in re.findall(r"([A-Za-z])([^A-Za-z]*)", d):
        if orden not in "ML":
            raise SystemExit("identidad/%s: el trazado admite solo M y L absolutos, no «%s»" % (nombre, orden))
        v = [float(x) for x in re.findall(r"-?\d+(?:\.\d+)?", numeros)]
        punto = (v[0], v[1])
        if orden == "M":
            subtrazados.append([punto])
        else:
            subtrazados[-1].append(punto)
    return subtrazados


def lejania_forma(f, c):
    """Distancia máxima (dp) de una forma, trazo incluido, al centro c del lienzo."""
    medio = f["trazo"] / 2
    if f["tipo"] == "circle":
        return ((f["cx"] - c) ** 2 + (f["cy"] - c) ** 2) ** 0.5 + f["r"] + medio
    if f["tipo"] == "path":
        return max(((x - c) ** 2 + (y - c) ** 2) ** 0.5 for sub in f["subtrazados"] for x, y in sub) + medio
    r = f["rx"] + medio
    # El punto más lejano de un rectángulo redondeado está en el arco de una de sus esquinas.
    return max(
        ((f["x"] + dx * f["w"] + (f["rx"] if dx == 0 else -f["rx"]) - c) ** 2
         + (f["y"] + dy * f["h"] + (f["rx"] if dy == 0 else -f["rx"]) - c) ** 2) ** 0.5 + r
        for dx in (0, 1) for dy in (0, 1))


def comprobar_zona_segura(nombre, formas):
    """Falla si alguna forma sale del círculo seguro (centrado en el lienzo)."""
    c = LIENZO_ICONO / 2
    for i, f in enumerate(formas, 1):
        lejos = lejania_forma(f, c)
        if lejos > ZONA_SEGURA + 1e-6:
            raise SystemExit("identidad/%s: la forma %d (%s) llega a %.1f dp del centro y la zona segura es de %.0f dp"
                             % (nombre, i, f["clase"], lejos, ZONA_SEGURA))


def comprobar_identidad():
    fuentes = sorted(p.name for p in (DS / "identidad").glob("icono-app*.svg"))
    for nombre in fuentes:
        comprobar_zona_segura(nombre, leer_formas(nombre))
    return fuentes


RES = APP / "res"
DURACION_SPLASH = 900  # ms; el máximo de la splash de Android 12 es 1000
VUELTAS_SPLASH = 2  # vueltas de la rueda trasera: termina en un múltiplo de 360° y coincide con el icono quieto
RUEDA_TRASERA = ("rueda-trasera", "radio", "buje")  # clases que giran en la splash


def num(v):
    return ("%.2f" % v).rstrip("0").rstrip(".")


def camino_forma(f):
    if f["tipo"] == "circle":
        cx, cy, r = f["cx"], f["cy"], f["r"]
        return "M%s,%sA%s,%s 0 1 1 %s,%sA%s,%s 0 1 1 %s,%sZ" % (
            num(cx - r), num(cy), num(r), num(r), num(cx + r), num(cy), num(r), num(r), num(cx - r), num(cy))
    if f["tipo"] == "path":
        return "".join("M%s,%s" % tuple(map(num, sub[0])) + "".join("L%s,%s" % tuple(map(num, q)) for q in sub[1:])
                       for sub in f["subtrazados"])
    x, y, w, h, r = f["x"], f["y"], f["w"], f["h"], f["rx"]
    return ("M%s,%sH%sA%s,%s 0 0 1 %s,%sV%sA%s,%s 0 0 1 %s,%sH%sA%s,%s 0 0 1 %s,%sV%sA%s,%s 0 0 1 %s,%sZ" % (
        num(x + r), num(y), num(x + w - r), num(r), num(r), num(x + w), num(y + r), num(y + h - r),
        num(r), num(r), num(x + w - r), num(y + h), num(x + r), num(r), num(r), num(x), num(y + h - r),
        num(y + r), num(r), num(r), num(x + r), num(y)))


def xml_ruta(f, colores, sangria):
    """Un <path> de vector para una forma. colores: clase → (atributo, valor) de color."""
    atributo, valor = colores[f["clase"]]
    atributos = ["android:%s=\"%s\"" % (atributo, valor)]
    if atributo == "strokeColor":
        atributos.append('android:strokeWidth="%s"' % num(f["trazo"]))
        if f["tipo"] != "rect":
            atributos += ['android:strokeLineCap="round"', 'android:strokeLineJoin="round"']
    atributos.append('android:pathData="%s"' % camino_forma(f))
    interior = ("\n" + sangria + "    ").join(atributos)
    return "%s<path\n%s    %s />\n" % (sangria, sangria, interior)


def xml_vector(titulo, formas, colores, gira=False):
    """Vector de 108 dp con una ruta por forma. Con gira, la rueda trasera va en un grupo «rueda» con su centro de pivote."""
    cuerpo, dentro = "", False
    for f in formas:
        en_grupo = gira and f["clase"] in RUEDA_TRASERA
        if en_grupo and not dentro:
            aro = next(g for g in formas if g["clase"] == "rueda-trasera")
            cuerpo += ('    <group\n        android:name="rueda"\n        android:pivotX="%s"\n        android:pivotY="%s">\n'
                       % (num(aro["cx"]), num(aro["cy"])))
        elif dentro and not en_grupo:
            cuerpo += "    </group>\n"
        dentro = en_grupo
        cuerpo += xml_ruta(f, colores, "        " if en_grupo else "    ")
    if dentro:
        cuerpo += "    </group>\n"
    return (
        '<?xml version="1.0" encoding="utf-8"?>\n'
        "<!-- %s. %s a partir de design-system/identidad. No editar a mano. -->\n"
        '<vector xmlns:android="http://schemas.android.com/apk/res/android"\n'
        '    android:width="108dp"\n    android:height="108dp"\n'
        '    android:viewportWidth="108"\n    android:viewportHeight="108">\n%s</vector>\n' % (titulo, AVISO_GENERADO, cuerpo))


def xml_splash():
    return (
        '<?xml version="1.0" encoding="utf-8"?>\n'
        "<!-- Splash animada: la rueda trasera gira %d vueltas acelerando y termina en la posición del icono. %s a partir de design-system/identidad. No editar a mano. -->\n"
        '<animated-vector xmlns:android="http://schemas.android.com/apk/res/android"\n'
        '    xmlns:aapt="http://schemas.android.com/aapt"\n'
        '    android:drawable="@drawable/identidad_splash_vector">\n'
        '    <target android:name="rueda">\n        <aapt:attr name="android:animation">\n'
        '            <objectAnimator\n                android:propertyName="rotation"\n'
        '                android:valueFrom="0"\n                android:valueTo="%d"\n'
        '                android:duration="%d"\n'
        '                android:interpolator="@android:interpolator/accelerate_quad" />\n'
        '        </aapt:attr>\n    </target>\n</animated-vector>\n'
        % (VUELTAS_SPLASH, AVISO_GENERADO, 360 * VUELTAS_SPLASH, DURACION_SPLASH))


def xml_adaptativo():
    return (
        '<?xml version="1.0" encoding="utf-8"?>\n'
        "<!-- Icono adaptativo. %s a partir de design-system/identidad. No editar a mano. -->\n"
        '<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">\n'
        '    <background android:drawable="@color/ventana_bg" />\n'
        '    <foreground android:drawable="@drawable/identidad_icono_primer_plano" />\n'
        '    <monochrome android:drawable="@drawable/identidad_icono_monocromo" />\n'
        "</adaptive-icon>\n" % AVISO_GENERADO)


def generar_identidad(t):
    """Recursos de identidad de la app como {ruta relativa a res/: contenido}."""
    color = contraste.colores("rodaje", t["color"]["tokens"])
    gris, ambar, tinta = color("line-strong"), color("gear-fill"), color("ink")
    negro = "#FF000000"
    trazos = {"cuadro": tinta, "rueda-delantera": gris, "rueda-trasera": ambar, "radio": ambar}
    rellenos = {"buje": ambar, "tambor": gris}
    colores = {**{c: ("strokeColor", v) for c, v in trazos.items()}, **{c: ("fillColor", v) for c, v in rellenos.items()}}
    mono = {**{c: ("strokeColor", negro) for c in trazos}, **{c: ("fillColor", negro) for c in rellenos}}
    formas = leer_formas("icono-app.svg")
    return {
        "drawable/identidad_icono_primer_plano.xml": xml_vector("Icono de la app (primer plano)", formas, colores),
        "drawable/identidad_icono_monocromo.xml": xml_vector(
            "Icono de la app (capa monocroma)", leer_formas("icono-app-monocromo.svg"), mono),
        "drawable/identidad_splash_vector.xml": xml_vector(
            "Splash (vector en su estado final)", formas, colores, gira=True),
        "drawable/identidad_splash.xml": xml_splash(),
        "mipmap-anydpi/ic_launcher.xml": xml_adaptativo(),
        "mipmap-anydpi/ic_launcher_round.xml": xml_adaptativo(),
    }


def escribir_identidad(recursos, destino):
    for ruta, contenido in recursos.items():
        f = destino / ruta
        f.parent.mkdir(parents=True, exist_ok=True)
        f.write_text(contenido, encoding="utf-8")


def main():
    t = json.loads((DS / "tokens.json").read_text(encoding="utf-8"))
    if sys.argv[1:2] == ["--identidad-a"]:
        # Para IdentidadSincronizadaTest: escribe solo los recursos de identidad en otro directorio.
        escribir_identidad(generar_identidad(t), Path(sys.argv[2]))
        return 0
    generar_tokens(t)
    n = generar_guia(t)
    print("tokens.css y styleguide/index.html generados (%d componentes)" % n)
    kotlin, ttfs = generar_kotlin(t)
    resultado = contraste.main()
    if resultado != 0:
        print("Tokens.kt y las fuentes de la app no se actualizan hasta que el contraste pase", file=sys.stderr)
        return resultado
    escribir_app(t, kotlin, ttfs)
    iconos = leer_iconos()
    escribir_iconos(iconos)
    comprobar_identidad()
    escribir_identidad(generar_identidad(t), RES)
    print("Tokens.kt, res/font (%d fuentes), color_ventana.xml y res/drawable (%d iconos) generados" % (len(ttfs), len(iconos)))
    return 0


if __name__ == "__main__":
    sys.exit(main())
