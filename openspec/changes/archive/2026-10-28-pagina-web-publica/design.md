# Design

## Context

Ver `proposal.md` para el motivo. Estado actual:

- El repositorio vive en `indoor-cycling-lab/smart-bike` y pasa a ser público con el change 20. No hay `.github/` ni web.
- `design-system/` ofrece `tokens.css` (temas oscuro y claro), fuentes WOFF2, iconos e identidad (logo e icono). `maquetas/` son HTML estáticos con datos ficticios, estados en el hash (`#estado=…&tema=…`) y una barra de revisión (`revision.js`, `revision.css`).
- Las maquetas de Dispositivos y Diagnóstico llevan, tras el change 20, valores de ejemplo en lugar de identificadores reales; aun así, cada captura se revisa a ojo antes de subirla.
- En el Mac hay Google Chrome, que se puede lanzar en modo headless para fotografiar páginas. No hay Playwright ni Node en el proyecto.
- El contenido de cara al usuario sale de `docs/funcional.md` y del README (que ya enumera funcionalidades y límites).

## Goals / Non-Goals

**Goals:**

- Una web estática, ligera y sin dependencias de ejecución, que cuente el proyecto con capturas reales de las maquetas.
- Que añadir un idioma o cambiar una captura sea reproducible con un comando.
- Que publicar sea solo subir `site/`.

**Non-Goals:**

- Sustituir el README ni la documentación técnica: la web enlaza, no duplica el detalle.
- Añadir componentes `sq-` o tokens al sistema de diseño: la maquetación de una página de presentación no es parte de la app.

## Decisions

1. **HTML generado desde una plantilla y dos ficheros de textos.** `scripts/sitio/generar.py` lee `site/contenido/es.json` y `en.json` y una plantilla, y escribe `site/es/index.html` y `site/en/index.html`. Las dos versiones comparten estructura, así que no divergen, y añadir un idioma es añadir un JSON. Alternativas: dos HTML escritos a mano (se desincronizan); un generador estático como Jekyll o Hugo (dependencia pesada para una página); traducción en el navegador con JavaScript (rompe «sin JavaScript» y el SEO). El script usa solo la biblioteca estándar de Python, como `generar.py` del sistema de diseño.

2. **Dos rutas por idioma y redirección en la raíz.** `site/es/` y `site/en/` con `<html lang>` y enlaces `hreflang` cruzados; `site/index.html` redirige con un script mínimo según `navigator.language`, con `<noscript>` y enlace manual a las dos versiones, y cae en español. Alternativa: una sola página con los dos textos ocultos por CSS (lectores de pantalla y buscadores los leen mal). GitHub Pages no negocia el idioma en el servidor, de ahí la redirección en el cliente.

3. **Estilos propios de la web, solo con tokens.** `site/sitio.css` importa una copia de `tokens.css` y de las fuentes y usa únicamente variables `--sq-*`; no define colores ni medidas literales nuevas. Una copia, hecha por el generador, porque Pages publica solo `site/` y el sistema de diseño es la única fuente. Si la web necesita un color que no existe, se añade antes a `tokens.json` (con su contraste) y no aquí. Alternativa: componentes `sq-` para hero, tarjetas y marco de móvil: se descarta porque no los usa la app y ensancharían el sistema por una página de presentación; se revisa si la web crece.

4. **Capturas con Chrome headless sobre las maquetas.** `scripts/sitio/capturas.py` sirve el repositorio en local, abre cada maqueta y estado de una lista (`site/capturas.json`: maqueta, estado, tamaño, nombre y texto alternativo en los dos idiomas) con `#estado=…&tema=…&vista=captura`, y guarda PNG en oscuro y claro (más WebP si Chrome lo permite). Las imágenes se suben al repositorio, así que la Action no necesita navegador y la web publicada coincide con lo validado. Si un estado deja de existir en las maquetas, el script falla. Alternativas: capturas reales del móvil (se descartan por decisión del usuario: las maquetas permiten datos ilustrativos y son reproducibles); Playwright o Puppeteer (dependencia de Node para una tarea ocasional).

5. **Vista de captura en las maquetas.** `revision.js` reconoce `vista=captura` en el hash y pone una clase en `<html>` que `revision.css` usa para ocultar la barra y los márgenes de revisión, dejando solo el marco de la pantalla de la app. Es un cambio acotado en la herramienta de revisión, no en las pantallas. Se añade al spec `screen-mockups`. Alternativa: recortar la barra a posteriori por píxeles (frágil si cambia su altura).

6. **Marco de móvil y tema de las capturas en CSS.** El marco (esquinas, borde, sombra) es CSS de la web, no parte de la captura, así que se adapta al tema y al ancho. Rodaje se enseña en horizontal en el hero, como se usa en el soporte, y en vertical donde convenga. Tema: `prefers-color-scheme` por defecto y un conmutador que fija `data-theme` y lo guarda en `localStorage` (con `try/catch`); las capturas de cada tema se alternan con CSS (`<picture>` con `prefers-color-scheme` no responde al conmutador, así que se usan dos `<img>` y se oculta una). Sin JavaScript queda el tema del sistema.

7. **Publicación con una GitHub Action que sube `site/`.** `.github/workflows/pages.yml` se dispara en `push` a `main` cuando cambia `site/**` y a mano; usa `actions/upload-pages-artifact` con `path: site` y `actions/deploy-pages`, con los permisos mínimos (`pages: write`, `id-token: write`). Las acciones se fijan a una versión concreta. Alternativa: servir la carpeta `/docs` desde la rama (obligaría a mover `docs/`, que no es público). Activar Pages con «Source: GitHub Actions» en los ajustes del repositorio es un paso del usuario.

8. **Contenido y marcas.** Los textos salen del README y de `docs/funcional.md` y no prometen más que ellos: lo «a validar» no se presenta como confirmado, y la página repite el aviso de marcas y de protocolos no oficiales. No usa logotipos de terceros (Tacx, Elite, Zwift, Wahoo): solo sus nombres en texto. Se usan el icono e identidad del propio proyecto (`design-system/identidad/`).

9. **Comprobaciones automáticas.** Un test de Python (`scripts/sitio/test_sitio.py`, `unittest`) comprueba sobre `site/`: las dos versiones tienen las mismas secciones e imágenes, todo `src`/`href` local existe, todo `<img>` tiene `alt`, no hay URLs externas salvo la lista de enlaces permitidos (repositorio y licencia), y las capturas listadas existen en los dos temas. El contraste lo cubren los tokens (`contraste.py`).

## Risks / Trade-offs

- [Las capturas de las maquetas se parecen a la app pero no son la app] → Cada pie de captura dice «maqueta» donde corresponda y el README ya remite al equipo real; el spec exige que no se muestre nada como confirmado si está «a validar».
- [Capturas y maquetas se desincronizan al cambiar el diseño] → `capturas.py` es reproducible y `docs/desarrollo.md` indica regenerarlas tras un change de UI; el test comprueba que las imágenes listadas existen, no que estén al día.
- [Un dato personal se cuela en una captura] → Solo se usan estados con datos ilustrativos; la validación del usuario incluye mirar cada captura y la tarea de privacidad escanea los textos de `site/`.
- [La redirección por idioma es del cliente] → Con JavaScript desactivado se muestra la raíz con los dos enlaces; cada versión se puede enlazar directamente.
- [Duplicar tokens y fuentes en `site/` engorda el repositorio] → Son unos pocos ficheros pequeños y los genera el script; el test de sincronía los compara con el sistema de diseño.
- [El nombre de la organización o del repositorio cambia] → Las rutas dentro de la web son relativas; solo cambian los enlaces al repositorio y la URL canónica, que salen de una constante del generador.

## Migration Plan

1. Preparar y validar la web en local (`python3 -m http.server`), sin publicar.
2. Con el visto bueno del usuario, subir los ficheros y que el usuario active Pages («GitHub Actions») en los ajustes.
3. Comprobar la dirección pública en el móvil y en el escritorio, en claro y oscuro, y en los dos idiomas.
4. Vuelta atrás: desactivar Pages en los ajustes o revertir el commit; no afecta a la app ni al resto del repositorio.

## Open Questions

- ¿Se añade una imagen de vista previa para compartir el enlace (`og:image`)? Se decide al ver la web en local; no cambia el resto del plan.
