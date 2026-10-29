## 1. Diccionario y maquetas en inglés

- [x] 1.1 `scripts/sitio/diccionario.py`: empareja por clave `values/strings.xml` y `values-en/strings.xml`, mezcla `maquetas/en.json` (manual, manda sobre la base) y escribe `maquetas/en.js`; falla si hay dos traducciones en conflicto. Test en `scripts/sitio/test_sitio.py`: `en.js` coincide con sus fuentes (falla si está desactualizado) y el conflicto se detecta. Verificar: `python3 -m unittest scripts/sitio/test_sitio.py`
- [x] 1.2 `maquetas/revision.js`: con `idioma=en` en el hash carga `en.js` antes del primer pintado y sustituye textos, `aria-label`, `title`, `placeholder`, `<title>` y `lang`; sin `idioma` no cambia nada. Verificar: abrir `maquetas/marchas.html#estado=shimano&idioma=en` en local y, sin `idioma`, comprobar que sigue igual
- [x] 1.3 Marca de «sin traducir» en `revision.js` con `idioma=en` (`data-rv-sin-traducir` en cada texto visible con letras que no esté en el diccionario ni en la lista de datos válidos en los dos idiomas, clave `"="` de `en.json`). Verificar: sobre `marchas#estado=shimano&idioma=en` con `en.json` vacío salen marcas, y con las traducciones no queda ninguna

## 2. Capturas por idioma

- [x] 2.1 `scripts/sitio/capturas.py`: itera idioma × tema, guarda `site/img/<id>-<idioma>-<tema>.png`, descarga el DOM con `--dump-dom` antes de fotografiar y falla indicando captura y texto si hay marcas de sin traducir; borra las `<id>-<tema>.png` antiguas al terminar; mantiene el filtro por `id`. Verificar: `python3 scripts/sitio/capturas.py marchas` genera las cuatro imágenes y falla a propósito si se quita una traducción
- [x] 2.2 Completar `maquetas/en.json` hasta que pasen las 12 capturas (rodaje, rodaje-vertical-plato, marchas, conexion, botones, pulso-ventilador, rodaje-erg, rodaje-vertical-datos, dispositivos, ajustes, diagnostico, guia-inicio), con las cifras en formato inglés y partiendo del inglés de la app. Verificar: `python3 scripts/sitio/capturas.py` termina sin avisos
- [x] 2.3 Revisar a ojo las 24 capturas inglesas: ningún texto desbordado ni cortado, ninguna cifra con formato español. Si una no cabe, acortar el texto de la maqueta. Verificar: revisión de `site/img/*-en-*.png` con el usuario

## 3. Web

- [x] 3.1 `scripts/sitio/generar.py`: cada página enlaza las capturas de su idioma (`<id>-<idioma>-<tema>.png`); `test_sitio.py` pasa a exigir que cada imagen exista y lleve el idioma de su página, en lugar de rutas iguales. Verificar: `python3 scripts/sitio/generar.py && python3 -m unittest scripts/sitio/test_sitio.py`
- [x] 3.2 Revisión final en local de `site/es/` y `site/en/` en claro y oscuro (servidor con `python3 -m http.server 8000`), con el usuario. Verificar: aprobación explícita del usuario; no se marca sin ella

## 4. Documentación

- [x] 4.1 `AGENTS.md` y `docs/desarrollo.md`: la regla de las maquetas fotografiadas (el texto nuevo lleva su inglés, `capturas.py` falla si falta), los comandos nuevos y los nombres de fichero. Verificar: lectura de las dos secciones, sin referencias a `<id>-<tema>.png`
- [x] 4.2 Actualizar la documentación: revisar `docs/funcional.md`, `docs/arquitectura.md`, `docs/planificacion.md` y `docs/desarrollo.md` y actualizar lo que el change haya cambiado (fila 41 de `planificacion.md` siempre). Verificar: la fila 41 refleja el estado y el alcance final
