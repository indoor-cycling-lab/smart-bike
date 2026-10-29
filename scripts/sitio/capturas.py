#!/usr/bin/env python3
"""Genera las capturas de la web a partir de las maquetas.

Sirve el repositorio en local, abre cada maqueta de `site/capturas.json` con
`#estado=…&tema=…&idioma=…&vista=captura` en Chrome sin interfaz y guarda un PNG por
idioma y tema en `site/img/` (`<id>-es-oscuro.png`, `<id>-es-claro.png`,
`<id>-en-oscuro.png` y `<id>-en-claro.png`). Las maquetas se ven en inglés con
`idioma=en` (`maquetas/en.js`, de `scripts/sitio/diccionario.py`): antes de
fotografiar una captura inglesa se vuelca el DOM y, si queda texto visible sin
traducir, el script falla y lo lista; añade la traducción a `maquetas/en.json`.

Uso: python3 scripts/sitio/capturas.py [id ...]
Chrome sin interfaz no baja de 500 px de ancho, así que las capturas se hacen
a 500 px como mínimo y se recortan, desde el centro, al ancho de la pantalla:
con `sips` (macOS) o, si no está, con Pillow (`pip install pillow`).
Chrome: se busca el de macOS o el del PATH; se puede fijar con CHROME=ruta.
"""
import functools
import html
import http.server
import json
import os
import re
import shutil
import subprocess
import sys
import tempfile
import threading
from pathlib import Path

RAIZ = Path(__file__).resolve().parents[2]
SITE = RAIZ / "site"
TEMAS = {"oscuro": None, "claro": "claro"}  # nombre del fichero → tema de la maqueta
IDIOMAS = {"es": None, "en": "en"}  # nombre del fichero → idioma de la maqueta
ANCHO_MINIMO = 500
ID = re.compile(r"[a-z0-9][a-z0-9-]*")
CANDIDATOS = [
    "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome",
    "google-chrome",
    "chromium",
    "chrome",
]


def buscar_chrome():
    for c in [os.environ.get("CHROME")] + CANDIDATOS:
        if c and (Path(c).exists() or shutil.which(c)):
            return c
    sys.exit("No encuentro Chrome: define CHROME=ruta del ejecutable.")


def leer_capturas():
    """`site/capturas.json`, validado: sus campos acaban en rutas, URL y atributos HTML, así que falla con un mensaje claro si alguno no encaja."""
    config = json.loads((SITE / "capturas.json").read_text(encoding="utf-8"))

    def falla(mensaje):
        sys.exit(f"site/capturas.json: {mensaje}")

    def positivo(valor, donde):
        if type(valor) is not int or valor <= 0:
            falla(f"{donde} = {valor!r} no es un entero positivo")

    positivo(config.get("escala"), "escala")
    vistos = set()
    for n, c in enumerate(config.get("capturas", [])):
        for campo in ("id", "maqueta", "estado"):
            if not isinstance(c.get(campo), str) or not ID.fullmatch(c[campo]):
                falla(f"capturas[{n}].{campo} = {c.get(campo)!r} no es un identificador (minúsculas, cifras y guiones)")
        if c["id"] in vistos:
            falla(f"«{c['id']}» está repetida")
        vistos.add(c["id"])
        if not (RAIZ / "maquetas" / f"{c['maqueta']}.html").is_file():
            falla(f"{c['id']}: no existe maquetas/{c['maqueta']}.html")
        positivo(c.get("ancho"), f"{c['id']}.ancho")
        positivo(c.get("alto"), f"{c['id']}.alto")
        alt = c.get("alt")
        if not isinstance(alt, dict) or not alt or not all(isinstance(v, str) and v.strip() for v in alt.values()):
            falla(f"{c['id']}.alt debe tener un texto no vacío por idioma")
    return config


def recortador():
    """`sips` en macOS; si no, Pillow. Sin ninguno, falla antes de fotografiar nada."""
    if shutil.which("sips"):
        def sips(fichero, ancho, alto):
            subprocess.run(["sips", "-c", str(alto), str(ancho), str(fichero)], check=True, stdout=subprocess.DEVNULL)
        return sips
    try:
        from PIL import Image
    except ImportError:
        sys.exit("Para recortar las capturas verticales hace falta `sips` (macOS) o Pillow: pip install pillow.")

    def pillow(fichero, ancho, alto):
        with Image.open(fichero) as imagen:
            izquierda = (imagen.width - ancho) // 2
            superior = (imagen.height - alto) // 2
            imagen.crop((izquierda, superior, izquierda + ancho, superior + alto)).save(fichero)
    return pillow


def estados_de(maqueta):
    html = (RAIZ / "maquetas" / f"{maqueta}.html").read_text(encoding="utf-8")
    m = re.search(r'data-mock-states="([^"]*)"', html)
    return {p.split(":")[0].strip() for p in m.group(1).split(",")} if m else set()


def servir():
    class Callado(http.server.SimpleHTTPRequestHandler):
        def log_message(self, *args):
            pass

    manejador = functools.partial(Callado, directory=str(RAIZ))
    servidor = http.server.ThreadingHTTPServer(("127.0.0.1", 0), manejador)
    threading.Thread(target=servidor.serve_forever, daemon=True).start()
    return servidor


def chrome_con_perfil(chrome, c, extra, url, escala, intentos=3):
    """Chrome a veces muere al arrancar (SIGKILL): perfil limpio por intento y reintento."""
    for intento in range(1, intentos + 1):
        with tempfile.TemporaryDirectory() as perfil:
            try:
                return subprocess.run(
                    [
                        chrome, "--headless=new", "--disable-gpu", "--hide-scrollbars",
                        "--no-first-run", f"--user-data-dir={perfil}",
                        f"--force-device-scale-factor={escala}",
                        f"--window-size={max(c['ancho'], ANCHO_MINIMO)},{c['alto']}",
                        "--virtual-time-budget=3000", *extra, url,
                    ],
                    check=True, stdout=subprocess.PIPE, stderr=subprocess.DEVNULL, timeout=60,
                ).stdout
            except (subprocess.CalledProcessError, subprocess.TimeoutExpired):
                if intento == intentos:
                    raise


def fotografiar(chrome, recortar, fichero, c, url, escala):
    chrome_con_perfil(chrome, c, [f"--screenshot={fichero}"], url, escala)
    if c["ancho"] < ANCHO_MINIMO:
        recortar(fichero, c["ancho"] * escala, c["alto"] * escala)


def sin_traducir(chrome, c, url, escala):
    """Textos visibles que `revision.js` dejó en español (`data-rv-sin-traducir`)."""
    dom = chrome_con_perfil(chrome, c, ["--dump-dom"], url, escala).decode("utf-8")
    textos = []
    for valor in re.findall(r'data-rv-sin-traducir="([^"]*)"', dom):
        textos += [html.unescape(t).strip() for t in valor.split(" | ") if t.strip()]
    return textos


def main():
    config = leer_capturas()
    filtro = set(sys.argv[1:])
    desconocidas = filtro - {c["id"] for c in config["capturas"]}
    if desconocidas:
        sys.exit(f"No hay capturas con id {', '.join(sorted(desconocidas))} en site/capturas.json.")
    capturas = [c for c in config["capturas"] if not filtro or c["id"] in filtro]
    for c in capturas:
        if c["estado"] not in estados_de(c["maqueta"]):
            sys.exit(f"La maqueta {c['maqueta']} no tiene el estado «{c['estado']}» ({c['id']}).")

    chrome = buscar_chrome()
    recortar = recortador() if any(c["ancho"] < ANCHO_MINIMO for c in capturas) else None
    servidor = servir()
    puerto = servidor.server_address[1]
    destino = SITE / "img"
    destino.mkdir(parents=True, exist_ok=True)
    pendientes = []
    for c in capturas:
        for idioma, parametro in IDIOMAS.items():
            def url_de(tema):
                hash_ = f"estado={c['estado']}" + (f"&tema={tema}" if tema else "") + (f"&idioma={parametro}" if parametro else "") + "&vista=captura"
                return f"http://127.0.0.1:{puerto}/maquetas/{c['maqueta']}.html#{hash_}"

            if parametro:  # el texto sin traducir no depende del tema
                faltan = sin_traducir(chrome, c, url_de(None), config["escala"])
                if faltan:
                    pendientes.append(f"{c['id']}-{idioma}: " + " | ".join(faltan))
                    continue
            for nombre, tema in TEMAS.items():
                fichero = destino / f"{c['id']}-{idioma}-{nombre}.png"
                fotografiar(chrome, recortar, fichero, c, url_de(tema), config["escala"])
                print(f"  {fichero.relative_to(RAIZ)}")
        # Los nombres anteriores al idioma (`<id>-<tema>.png`) ya no los usa nadie.
        for nombre in TEMAS:
            (destino / f"{c['id']}-{nombre}.png").unlink(missing_ok=True)
    servidor.shutdown()
    if pendientes:
        sys.exit("Texto visible sin traducir (añádelo a maquetas/en.json, «textos»; si vale en los dos idiomas, en «=»):\n  " + "\n  ".join(pendientes))


if __name__ == "__main__":
    main()
