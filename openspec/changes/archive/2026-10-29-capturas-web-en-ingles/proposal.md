# Proposal

## Why

La app tiene interfaz en español y en inglés desde el change 34 (`idioma-es-en`), y la web pública también, pero las capturas de la web salen de las maquetas, que solo existen en español. La página `/en/` explica la app en inglés y la enseña con la interfaz en español: la versión inglesa queda incoherente justo en lo que más pesa, que son las capturas. Conviene resolverlo antes de publicar la web (`publicar-repo-publico`, change 20), para que salga ya con las dos versiones completas.

## What Changes

- Las maquetas pasan a poder mostrarse en inglés mediante `idioma=en` en el hash (junto a `estado`, `tema` y `vista`), sin duplicar el HTML: un diccionario aplicado al cargar sustituye textos, `aria-label` y `title`. La base del diccionario sale de los pares es→en de `values/strings.xml` y `values-en/strings.xml`; lo que no está en la app (datos concretos de las maquetas, cifras con formato propio de cada idioma, frases partidas por etiquetas) va en un fichero a mano.
- `scripts/sitio/capturas.py` fotografía cada captura en los dos idiomas y los dos temas (`<id>-<idioma>-<tema>.png`, 48 imágenes en lugar de 24) y falla si en la captura inglesa queda texto visible sin traducir.
- `scripts/sitio/generar.py` enlaza en cada página la captura de su idioma.
- Alcance acotado: solo los estados fotografiados de `site/capturas.json` (12 capturas de 8 maquetas) tienen que estar traducidos. El resto de maquetas y de estados sigue solo en español, que es el idioma de validación.
- Regla de trabajo nueva en `AGENTS.md` y `docs/desarrollo.md`: un texto nuevo en una maqueta que sale en una captura de la web lleva su inglés.

Fuera de alcance:

- Traducir todas las maquetas y todos sus estados, o validar la UI en inglés sobre la maqueta.
- Cambiar el diseño, los textos de la app o los de la web.
- Un selector de idioma en las maquetas (el idioma solo se fija desde el hash).
- Publicar la web o activar GitHub Pages (queda en `publicar-repo-publico`).

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

- `public-website`: el requisito «Capturas de las maquetas» añade que cada captura existe en el idioma de la página que la muestra, con la interfaz de la app en ese idioma.

## Impact

- Código y herramientas: `maquetas/revision.js` (carga del diccionario), un fichero de diccionario nuevo en `maquetas/`, `scripts/sitio/capturas.py`, `scripts/sitio/generar.py` y `scripts/sitio/test_sitio.py`. Las maquetas `.html` no se editan.
- Generados: `site/img/` (48 PNG) y `site/es/`, `site/en/`.
- Sin cambios en la app, en `design-system/` ni en el flujo de la Action de Pages (sigue publicando `site/`); el peso de `site/img/` se duplica (de 2,6 MB a unos 5,6 MB).
- Documentos: `docs/desarrollo.md` (comandos y regla de las capturas) y `docs/planificacion.md` (fila 41); `AGENTS.md` (regla para las maquetas). `docs/funcional.md` y `docs/arquitectura.md`, ninguno: no hay cambio funcional ni de arquitectura de la app.
- Sin impacto funcional: no pasa por maqueta ni por puerta de validación; el usuario revisa el resultado en `site/en/`.
