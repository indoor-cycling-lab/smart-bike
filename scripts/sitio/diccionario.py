#!/usr/bin/env python3
"""Genera el diccionario es→en con el que las maquetas se ven en inglés (`#idioma=en`).

Entradas:  app/src/main/res/values/strings.xml y values-en/strings.xml (los pares
           por clave) y maquetas/en.json (a mano: lo que no sale de la app).
Salida:    maquetas/en.js (`window.RV_EN`), que carga revision.js solo con idioma=en.

`maquetas/en.json` tiene tres claves:
  - «textos»: texto español → inglés. Manda sobre los pares de la app.
  - «contextos»: clase CSS del elemento → {texto español → inglés}, para el mismo
    texto con otro sentido según dónde esté («Pendiente»: dato o estado de la guía).
  - «=»: textos que valen igual en los dos idiomas (cifras con unidades, marcas); se
    suman los que la app ya escribe igual en los dos.

De la app solo entran las cadenas sin marcadores de formato (%1$s, %d) ni marcas HTML, que son
las que una maqueta puede repetir al pie de la letra; el resto va en «textos».
Si dos cadenas de la app tienen el mismo español y distinto inglés, hay que decidir
en «textos»: el script falla en lugar de elegir una.

Uso: python3 scripts/sitio/diccionario.py
"""
import html
import json
import re
import sys
from pathlib import Path

RAIZ = Path(__file__).resolve().parents[2]
RES = RAIZ / "app" / "src" / "main" / "res"
MANUAL = RAIZ / "maquetas" / "en.json"
SALIDA = RAIZ / "maquetas" / "en.js"
FORMATO = re.compile(r"%(?:\d+\$)?[sdf]")
CADENA = re.compile(r'<string\s+name="([^"]+)"([^>]*)>(.*?)</string>', re.S)


def normalizar(texto):
    """Mismo criterio que revision.js: espacios colapsados y recortados."""
    return re.sub(r"\s+", " ", texto).strip()


def leer_cadenas(fichero):
    cadenas = {}
    for nombre, atributos, valor in CADENA.findall(fichero.read_text(encoding="utf-8")):
        if 'translatable="false"' in atributos:
            continue
        valor = html.unescape(valor).replace("\\'", "'").replace('\\"', '"').replace("\\n", "\n").replace("\\@", "@")
        cadenas[nombre] = normalizar(valor)
    return cadenas


def cadenas_de_la_app():
    """Pares (español, inglés) por clave, sin formatos ni marcas HTML."""
    es, en = leer_cadenas(RES / "values" / "strings.xml"), leer_cadenas(RES / "values-en" / "strings.xml")
    return [(texto.replace("%%", "%"), en[clave].replace("%%", "%")) for clave, texto in es.items()
            if en.get(clave) and not any(FORMATO.search(s) or "<" in s for s in (texto, en[clave]))]


def pares_de_la_app():
    """es → conjunto de inglés, sin las cadenas que son iguales en los dos idiomas."""
    pares = {}
    for es, en in cadenas_de_la_app():
        if es != en:
            pares.setdefault(es, set()).add(en)
    return pares


def iguales_de_la_app():
    """Textos que la app escribe igual en español y en inglés (Neo, Zwift…)."""
    return {es for es, en in cadenas_de_la_app() if es == en}


def leer_manual():
    datos = json.loads(MANUAL.read_text(encoding="utf-8")) if MANUAL.exists() else {}
    desconocidas = set(datos) - {"textos", "contextos", "="}
    if desconocidas:
        raise ValueError(f"{MANUAL.name}: claves desconocidas {sorted(desconocidas)}")
    textos = {normalizar(k): normalizar(v) for k, v in datos.get("textos", {}).items()}
    contextos = {clase: {normalizar(k): normalizar(v) for k, v in pares.items()} for clase, pares in datos.get("contextos", {}).items()}
    iguales = [normalizar(t) for t in datos.get("=", [])]
    return textos, contextos, iguales


def construir(pares=None, manual=None):
    pares = pares_de_la_app() if pares is None else pares
    textos_manual, contextos, iguales = leer_manual() if manual is None else manual
    textos = {}
    conflictos = []
    for es, opciones in pares.items():
        if es in textos_manual:
            continue
        if len(opciones) > 1:
            conflictos.append(f"«{es}» → {sorted(opciones)}")
        else:
            textos[es] = next(iter(opciones))
    if conflictos:
        raise ValueError("Textos con dos traducciones en la app; decide en maquetas/en.json («textos»):\n  " + "\n  ".join(conflictos))
    textos.update(textos_manual)
    iguales = set(iguales) | (iguales_de_la_app() if manual is None else set())
    return {"textos": dict(sorted(textos.items())), "contextos": dict(sorted(contextos.items())), "iguales": sorted(iguales)}


def escribir(diccionario):
    cuerpo = json.dumps(diccionario, ensure_ascii=False, indent=1)
    return (
        "/* Generado por scripts/sitio/diccionario.py: no se edita a mano (ver maquetas/en.json). */\n"
        f"window.RV_EN = {cuerpo};\n"
    )


def main():
    try:
        SALIDA.write_text(escribir(construir()), encoding="utf-8")
    except ValueError as error:
        sys.exit(str(error))
    print(f"  {SALIDA.relative_to(RAIZ)}")


if __name__ == "__main__":
    main()
