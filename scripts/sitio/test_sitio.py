"""Comprobaciones de la web pública (`site/`). Uso: python3 -m unittest scripts/sitio/test_sitio.py"""
import json
import re
import sys
import unittest
from html.parser import HTMLParser
from pathlib import Path

SITE = Path(__file__).resolve().parents[2] / "site"
sys.path.insert(0, str(Path(__file__).resolve().parent))
import diccionario  # noqa: E402
IDIOMAS = ["es", "en"]
PERMITIDOS = ("https://github.com/indoor-cycling-lab/smart-bike", "https://ko-fi.com/jmlopezdona")
PATRONES_PERSONALES = {
    "dirección MAC": re.compile(r"\b[0-9A-Fa-f]{2}(?::[0-9A-Fa-f]{2}){5}\b"),
    "IPv4 privada": re.compile(r"\b(?:10\.\d+\.\d+\.\d+|192\.168\.\d+\.\d+|172\.(?:1[6-9]|2\d|3[01])\.\d+\.\d+)\b"),
    "correo": re.compile(r"[\w.+-]+@[\w-]+\.[\w.]+"),
    "ruta personal": re.compile(r"/Users/\w+"),
}


class Pagina(HTMLParser):
    def __init__(self):
        super().__init__()
        self.refs, self.imgs, self.ids, self.h1, self.lang = [], [], [], 0, None
        self.titulo = False
        self.texto = []

    def handle_starttag(self, tag, attrs):
        a = dict(attrs)
        if tag == "html":
            self.lang = a.get("lang")
        if tag == "h1":
            self.h1 += 1
        if "id" in a:
            self.ids.append(a["id"])
        if tag == "img":
            self.imgs.append(a)
        # canonical, alternate y og:url son metadatos, no recursos que cargue la página
        if tag in ("a", "img", "script") or (tag == "link" and a.get("rel") in ("stylesheet", "icon")):
            ref = a.get("href") or a.get("src")
            if ref:
                self.refs.append(ref)

    def handle_data(self, data):
        self.texto.append(data)


def leer(idioma):
    p = Pagina()
    p.feed((SITE / idioma / "index.html").read_text(encoding="utf-8"))
    return p


class TestSitio(unittest.TestCase):
    def test_estructura_igual_en_los_dos_idiomas(self):
        es, en = leer("es"), leer("en")
        self.assertEqual(es.ids, en.ids)
        # Las mismas capturas en el mismo orden; cada página con las de su idioma.
        self.assertEqual([i["src"].replace("-es-", "-") for i in es.imgs], [i["src"].replace("-en-", "-") for i in en.imgs])
        for idioma, p in (("es", es), ("en", en)):
            for img in p.imgs:
                if "/img/" in img["src"]:
                    self.assertIn(f"-{idioma}-", img["src"], f"{idioma}: {img['src']}")

    def test_idioma_declarado_y_un_solo_h1(self):
        for idioma in IDIOMAS:
            p = leer(idioma)
            self.assertEqual(p.lang, idioma)
            self.assertEqual(p.h1, 1, idioma)

    def test_recursos_locales_existen(self):
        for idioma in IDIOMAS:
            for ref in leer(idioma).refs:
                if ref.startswith(("http", "#", "mailto:")):
                    continue
                destino = (SITE / idioma / ref.split("#")[0]).resolve()
                self.assertTrue(destino.exists(), f"{idioma}: {ref}")

    def test_toda_imagen_tiene_alt(self):
        for idioma in IDIOMAS:
            for img in leer(idioma).imgs:
                self.assertTrue(img.get("alt", "").strip(), f"{idioma}: {img.get('src')}")

    def test_sin_urls_externas_salvo_el_repositorio(self):
        for idioma in IDIOMAS:
            for ref in leer(idioma).refs:
                if ref.startswith("http"):
                    self.assertTrue(ref.startswith(PERMITIDOS), f"{idioma}: {ref}")
        css = (SITE / "sitio.css").read_text(encoding="utf-8") + (SITE / "assets" / "base.css").read_text(encoding="utf-8")
        self.assertNotRegex(css, r"url\(\s*[\"']?https?:", "el CSS no debe cargar nada externo")

    def test_capturas_en_los_dos_idiomas_y_los_dos_temas(self):
        capturas = json.loads((SITE / "capturas.json").read_text(encoding="utf-8"))["capturas"]
        for c in capturas:
            for idioma in IDIOMAS:
                for tema in ("oscuro", "claro"):
                    self.assertTrue((SITE / "img" / f"{c['id']}-{idioma}-{tema}.png").exists(), f"{c['id']}-{idioma}-{tema}")
            self.assertEqual(set(c["alt"]), set(IDIOMAS), c["id"])

    def test_textos_con_las_mismas_claves(self):
        def claves(o, ruta=""):
            if isinstance(o, dict):
                return {k2 for k, v in o.items() for k2 in claves(v, f"{ruta}/{k}")}
            if isinstance(o, list):
                return {f"{ruta}[{len(o)}]"} | {k2 for v in o for k2 in claves(v, ruta + "[]")}
            return {ruta}

        es, en = (json.loads((SITE / "contenido" / f"{i}.json").read_text(encoding="utf-8")) for i in IDIOMAS)
        self.assertEqual(claves(es), claves(en))

    def test_sin_datos_personales(self):
        ficheros = [*SITE.glob("*/index.html"), SITE / "index.html", *(SITE / "contenido").glob("*.json"), SITE / "capturas.json"]
        for f in ficheros:
            texto = f.read_text(encoding="utf-8")
            for nombre, patron in PATRONES_PERSONALES.items():
                self.assertIsNone(patron.search(texto), f"{f.name}: {nombre}")

    def test_sin_estilos_con_colores_literales(self):
        css = (SITE / "sitio.css").read_text(encoding="utf-8")
        self.assertNotRegex(css, r"#[0-9a-fA-F]{3,8}\b", "sitio.css solo usa tokens")
        self.assertNotRegex(css, r"\b(?:rgb|hsl)a?\(", "sitio.css solo usa tokens")


class TestDiccionario(unittest.TestCase):
    def test_en_js_coincide_con_sus_fuentes(self):
        self.assertEqual(
            diccionario.SALIDA.read_text(encoding="utf-8"),
            diccionario.escribir(diccionario.construir()),
            "maquetas/en.js está desactualizado: python3 scripts/sitio/diccionario.py",
        )

    def test_dos_traducciones_de_la_app_sin_decidir_fallan(self):
        pares = {"Pendiente": {"Grade", "Pending"}, "Atrás": {"Back"}}
        with self.assertRaises(ValueError):
            diccionario.construir(pares, ({}, {}, []))

    def test_el_manual_resuelve_el_conflicto_y_manda_sobre_la_app(self):
        pares = {"Pendiente": {"Grade", "Pending"}, "Atrás": {"Back"}}
        d = diccionario.construir(pares, ({"Pendiente": "Grade", "Atrás": "Return"}, {"x": {"A": "B"}}, ["12 W"]))
        self.assertEqual(d, {"textos": {"Atrás": "Return", "Pendiente": "Grade"}, "contextos": {"x": {"A": "B"}}, "iguales": ["12 W"]})

    def test_cadenas_con_formato_no_entran_de_la_app(self):
        self.assertFalse([es for es in diccionario.pares_de_la_app() if diccionario.FORMATO.search(es) or "<" in es])


if __name__ == "__main__":
    unittest.main()
