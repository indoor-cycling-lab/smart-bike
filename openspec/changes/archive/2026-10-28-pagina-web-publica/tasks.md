# Tasks

Impacto visual: la sección «Maqueta» construye la web y la deja lista para validarla en local; no se publica nada hasta la aprobación. La sección «App» no aplica (no cambia la app); se sustituye por «Publicación».

## 1. Maqueta

- [x] 1.1 Añadir la vista de captura a las maquetas: `revision.js` reconoce `vista=captura` en el hash y `revision.css` oculta la barra de revisión. Verificar: abrir `maquetas/rodaje.html#estado=shimano-auto&tema=claro&vista=captura` con `python3 -m http.server 8000` muestra solo la pantalla, y sin `vista` la barra sigue igual; actualizar el apartado de la herramienta en `maquetas/index.html`.
- [x] 1.2 Elegir los estados a mostrar y escribir `site/capturas.json` (maqueta, estado, orientación, nombre y texto alternativo en español e inglés): el hero (Rodaje horizontal), marchas virtuales (Shimano automático y plato), Zwift (ruta y mando), pulso y ventilador, Ajustes y Diagnóstico. Verificar: cada par maqueta/estado existe en `maquetas/` y no muestra valores reales ni pendientes entre corchetes.
- [x] 1.3 Escribir `scripts/sitio/capturas.py`: sirve el repositorio, lanza Chrome headless por cada entrada y tema y guarda las imágenes en `site/img/`. Verificar: ejecutarlo genera las imágenes en oscuro y claro, vuelve a dar los mismos ficheros y falla con un estado inexistente.
- [x] 1.4 Escribir `site/contenido/es.json` y `en.json` y la plantilla, a partir del README y de `docs/funcional.md` (qué es, setup, funcionalidades, límites, marcas, enlaces). Verificar: las dos versiones tienen las mismas claves y ningún texto promete algo marcado «a validar».
- [x] 1.5 Escribir `scripts/sitio/generar.py`: renderiza `site/es/index.html`, `site/en/index.html` y la raíz con redirección, copia `tokens.css`, fuentes e identidad, y escribe `site/sitio.css` solo con tokens. Verificar: `python3 scripts/sitio/generar.py` genera todo sin errores y repetirlo no cambia nada.
- [x] 1.6 Diseñar la página: hero con marco de móvil, diagrama del setup, secciones por funcionalidad con captura alterna (texto y captura), selector de idioma, conmutador de tema, pie con marcas y licencia; adaptable de 360 px a escritorio, foco visible y `prefers-reduced-motion`. Verificar: revisar en el navegador a 360, 768 y 1280 px, en claro y oscuro, con JavaScript desactivado y solo con teclado.
- [x] 1.7 Escribir `scripts/sitio/test_sitio.py` (`python3 -m unittest`): mismas secciones e imágenes en los dos idiomas, enlaces y `src` locales existentes, todo `img` con `alt`, sin URLs externas salvo las permitidas, capturas de la lista presentes en los dos temas y sin patrones de datos personales (direcciones MAC, IPv4 privadas, correos) en `site/`. Verificar: los tests pasan y fallan al romper un enlace, quitar un `alt` o meter una IP.
- [x] 1.8 Validación de la maqueta por el usuario: dar los enlaces (`http://localhost:8000/site/`, `/site/es/`, `/site/en/`) y la lista de capturas a revisar en el móvil y el escritorio, y esperar. Verificar: el usuario aprueba de forma explícita en la conversación; si pide cambios, ajustar y volver a pedir validación.

## 2. Publicación

- [x] 2.1 Escribir `.github/workflows/pages.yml` (disparo en `push` a `main` con cambios en `site/**` y manual, permisos mínimos, acciones con versión fijada, `upload-pages-artifact` con `path: site` y `deploy-pages`). Verificar: `python3 -c "import yaml"` o revisión manual del YAML sin errores, y que solo sube `site/`.
- [ ] 2.2 Con confirmación expresa del usuario, hacer el commit y subirlo, y pedirle que active Pages con «Source: GitHub Actions» en los ajustes del repositorio. Verificar: la Action termina en verde y `https://indoor-cycling-lab.github.io/smart-bike/` responde.
- [ ] 2.3 Comprobar la web publicada: los dos idiomas, claro y oscuro, el móvil del usuario y que `/docs/arquitectura.md` no existe en esa dirección. Verificar: lista de comprobaciones confirmada por el usuario.
- [ ] 2.4 Enlazar la web desde `README.md` y como «Website» en la descripción del repositorio (acción del usuario). Verificar: el enlace del README abre la web.

## 3. Documentación

- [x] 3.1 Actualizar la documentación: revisar `docs/funcional.md` (sin cambios previstos), `docs/arquitectura.md` (sin cambios previstos), `docs/planificacion.md` (fila del change 21: «En curso» al crearlo y «Archivado» al cerrarlo, con su nota) y `docs/desarrollo.md` (cómo regenerar capturas y web, previsualizar en local y publicar); en `AGENTS.md`, registrar que `site/` se genera con `scripts/sitio/` (no editar a mano `site/es`, `site/en` ni `site/img`), que la web es bilingüe (excepción a la regla del idioma) y que un change de UI vuelve a pasar por las capturas. Verificar: los cuatro documentos revisados y la fila del change en `planificacion.md` actualizada.
