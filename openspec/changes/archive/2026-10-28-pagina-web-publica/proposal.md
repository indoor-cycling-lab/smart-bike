# Proposal

## Why

Con el repositorio ya público (change 20), el README es lo único que explica el proyecto, y es texto sin imágenes. Quien llegue por casualidad o por una búsqueda no ve cómo es la app. Una página pública en GitHub Pages, con capturas de las pantallas y un diseño cuidado, presenta el proyecto de un vistazo y lleva al código.

## What Changes

- Se crea una **página web estática** (`site/`) que explica qué es Smart Bike y sus funcionalidades principales: marchas virtuales, rodillo y mando para Zwift, pulso y ventilador, pantalla de rodaje y diagnóstico. Incluye el diagrama del setup, los límites del proyecto (hecho para un equipo concreto, solo Android, sin garantías), el aviso de marcas y los enlaces al repositorio.
- Está en **español e inglés**, en dos páginas (`/es/` y `/en/`) con un selector de idioma; la raíz redirige según el idioma del navegador.
- Las **capturas salen de las maquetas** (`maquetas/`), con los datos ilustrativos que ya llevan, en tema oscuro y claro y dentro de un marco de móvil. Un script las genera de forma reproducible y se suben ya hechas, de modo que publicar no exige un navegador.
- La página usa los **tokens del sistema de diseño** (colores, tipografías, espacios) y respeta el tema claro u oscuro del visitante.
- Se **publica con GitHub Pages** mediante una GitHub Action que sube solo `site/`, en `indoor-cycling-lab.github.io/smart-bike`.
- Las maquetas ganan una vista **sin barra de revisión** (`#vista=captura`) para poder capturarlas limpias.
- Etapa de maqueta: antes de publicar, el usuario valida la página en local.

Queda fuera de alcance:

- Cambios en la app, en las pantallas o en el sistema de diseño (la página no añade componentes `sq-` ni tokens).
- Dominio propio, analítica, cookies o formularios; no se hace ninguna petición a servicios externos.
- Capturas reales del móvil y vídeos o animaciones de la app.
- Un blog, documentación técnica o de protocolos en la web: eso sigue en el repositorio.
- Traducir el resto de la documentación o el código al inglés.

## Capabilities

### New Capabilities

- `public-website`: la página pública del proyecto: contenido, idiomas, capturas, tema, accesibilidad, privacidad y publicación.

### Modified Capabilities

- `screen-mockups`: la herramienta de revisión admite una vista de captura sin la barra de revisión.

## Impact

- **Código y ficheros nuevos:** `site/` (HTML generado, CSS, imágenes y textos en `es` y `en`), un script de generación y de captura en `scripts/sitio/` y el flujo `.github/workflows/pages.yml`. Cambian `maquetas/revision.js` y `revision.css` (vista de captura). La app Android no cambia.
- **Documentos de `docs/`:** `desarrollo.md` (cómo regenerar y previsualizar la web) y `planificacion.md` (fila del change 21 y orden). `funcional.md` y `arquitectura.md`: ninguno. `AGENTS.md` y `README.md` enlazan la web y recogen que el sitio es bilingüe, una excepción a la regla del idioma.
- **Equipo real:** no aplica; no toca BLE ni Zwift. Se comprueba en navegador y en el móvil del usuario, en claro y oscuro.
- **Orden:** depende del change 20 (`publicar-repo-publico`): Pages necesita el repositorio público y su dirección final. Se puede preparar y validar en local antes.
- **Acciones fuera del repositorio** (activar Pages en los ajustes del repositorio, publicar): las confirma el usuario; no se ejecutan sin su visto bueno expreso.
