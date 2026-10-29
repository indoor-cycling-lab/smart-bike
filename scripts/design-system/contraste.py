#!/usr/bin/env python3
"""Comprueba el contraste WCAG de los pares de tokens de color en los dos temas.

Lee design-system/tokens.json y sale con código 1 si algún par no llega a su
mínimo (4,5:1 para texto, 3:1 para bordes de controles). Al añadir un token de
color, añade aquí los fondos sobre los que se usa.

    python3 scripts/design-system/contraste.py
"""
import json
import sys
from pathlib import Path

RAIZ = Path(__file__).resolve().parents[2]
TOKENS = RAIZ / "design-system" / "tokens.json"
TEMAS = ("rodaje", "claro")

TEXTO = 4.5
GRAFICO = 3.0

# (primer plano, fondo, mínimo)
PARES = (
    [(c, f, TEXTO) for c in ("ink", "ink-muted", "gear", "climb", "descent", "sim", "erg", "ok", "danger")
     for f in ("bg", "surface")]
    + [(c, "surface-2", TEXTO) for c in ("ink", "ink-muted")]
    + [("on-gear-fill", "gear-fill", TEXTO)]
    + [("erg", "erg-soft", TEXTO)]
    + [("gear", "gear-soft", TEXTO)]
    + [("line-strong", f, GRAFICO) for f in ("bg", "surface")]
)

# Icono de la app (design-system/identidad): es el mismo en los dos temas y su fondo es el bg de Rodaje, así que
# se comprueba solo en ese tema. Rueda trasera (gear-fill) sobre bg, cuadro (ink) sobre bg y sobre la rueda delantera (line-strong); el tambor y la
# rueda delantera (line-strong) sobre bg ya están arriba. El cuadro (ink) no se compara con la rueda trasera
# (gear-fill, 1,6:1): no se tocan, porque el cuadro termina antes del borde de la rueda con un hueco del color de bg.
PARES_RODAJE = [("gear-fill", f, GRAFICO) for f in ("bg", "line-strong")] + [("ink", f, GRAFICO) for f in ("bg", "line-strong")]


def colores(tema, tokens):
    por_nombre = {t["name"]: t["value"] for t in tokens}

    def resolver(nombre):
        v = por_nombre[nombre]
        if isinstance(v, str):
            return resolver(v.strip("{}"))
        return v[tema]

    return resolver


def luminancia(hexa):
    hexa = hexa.lstrip("#")
    canales = [int(hexa[i:i + 2], 16) / 255 for i in (0, 2, 4)]
    r, g, b = [c / 12.92 if c <= 0.04045 else ((c + 0.055) / 1.055) ** 2.4 for c in canales]
    return 0.2126 * r + 0.7152 * g + 0.0722 * b


def ratio(a, b):
    la, lb = sorted((luminancia(a), luminancia(b)), reverse=True)
    return (la + 0.05) / (lb + 0.05)


def comprobar(tokens):
    """Devuelve la lista de fallos como (tema, primer plano, fondo, ratio, mínimo)."""
    fallos = []
    for tema in TEMAS:
        color = colores(tema, tokens)
        for frente, fondo, minimo in PARES + (PARES_RODAJE if tema == "rodaje" else []):
            r = ratio(color(frente), color(fondo))
            if r < minimo:
                fallos.append((tema, frente, fondo, r, minimo))
    return fallos


def main():
    tokens = json.loads(TOKENS.read_text(encoding="utf-8"))["color"]["tokens"]
    fallos = comprobar(tokens)
    total = len(PARES) * len(TEMAS) + len(PARES_RODAJE)
    if fallos:
        for tema, frente, fondo, r, minimo in fallos:
            print("Contraste insuficiente en %s: %s sobre %s = %.2f:1 (mínimo %.1f:1)"
                  % (tema, frente, fondo, r, minimo), file=sys.stderr)
        print("Contraste: %d de %d pares fallan" % (len(fallos), total), file=sys.stderr)
        return 1
    print("Contraste: %d pares correctos en los dos temas" % total)
    return 0


if __name__ == "__main__":
    sys.exit(main())
