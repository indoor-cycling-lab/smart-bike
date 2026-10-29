# Design

## Context

- Las maquetas (`maquetas/*.html`, ~1 MB) llevan el texto en español escrito a mano. `rodaje.html` y `rodaje-vertical.html` pesan 260 y 300 KB porque contienen todos sus estados, que `revision.js` activa o desactiva con `data-mock-state`.
- `rodaje.js` solo cambia atributos y clases al cambiar de estado; no escribe texto. Todo el texto de todos los estados está ya en el DOM al cargar.
- `capturas.py` abre cada maqueta con `#estado=…&tema=…&vista=captura` en Chrome sin interfaz, desde un servidor HTTP local, y guarda `<id>-<tema>.png`. `generar.py` usa las mismas imágenes en `site/es/` y `site/en/`; solo traduce el `alt`. `test_sitio.py` exige hoy que las dos páginas tengan las mismas rutas de imagen.
- Medición previa (textos únicos de cada maqueta frente a los pares de `strings.xml`): cubre aproximadamente la mitad (rodaje 94 de 255, marchas 22 de 64, conexión 59 de 87, ajustes 109 de 151, botones 80 de 100). El resto son datos concretos («Cassette 11 a 36, piñón 17»), textos con negritas o enlaces a mitad de frase, el `<title>` y frases que solo existen en la maqueta.

## Goals / Non-Goals

**Goals:**

- Que `site/en/` enseñe la app en inglés y `site/es/`, en español, con el mismo estado, datos y tema.
- Que el inglés de las capturas sea el de la app, no una traducción paralela.
- Que no se pueda regenerar la web con una captura inglesa a medio traducir sin enterarse.

**Non-Goals:**

- Maquetas bilingües para validar la UI (el usuario valida en español).
- Traducir estados que no se fotografían.
- Cambiar el formato de `site/capturas.json` más allá de lo necesario.

## Decisions

### 1. Diccionario aplicado al cargar, no maquetas duplicadas

`revision.js` lee `idioma=en` del hash (junto a `estado`, `tema` y `vista`). Si está, recorre el DOM al arrancar y sustituye, por coincidencia exacta del texto recortado, los nodos de texto y los atributos `aria-label`, `title` y `placeholder`; también el `<title>` y `lang` del `<html>`. Sin `idioma`, o con `idioma=es`, no hace nada.

Alternativas: copiar las maquetas a `maquetas/en/` (se desincronizan, y cada change de UI tocaría 1 MB duplicado); generar las maquetas desde plantillas con claves (reescribir todas, desproporcionado para unas capturas).

### 2. Dos fuentes, un fichero generado

- Base: `scripts/sitio/diccionario.py` empareja por clave las cadenas de `values/strings.xml` y `values-en/strings.xml` y produce es→en. Las cadenas con marcadores de formato (`%1$s`, `%1$d`) no valen para coincidencia exacta; se resuelven en la parte manual.
- Manual: `maquetas/en.json`, es→en para lo que no está en la app: datos concretos de las maquetas, frases partidas por etiquetas (se traduce cada trozo) y cifras con su formato inglés (`0,0040` → `0.0040`). Manda sobre la base si coinciden.
- Resultado: `maquetas/en.js` (`window.RV_EN = {…}`), generado y versionado. Es un `.js` y no un JSON para que cargue también al abrir la maqueta con `file://`. `revision.js` lo inserta con `document.write` de una etiqueta `<script>` solo si hay `idioma=en`, en el `<head>` y antes del primer pintado.
- Un mismo texto español puede tener otro sentido según dónde esté («Pendiente» es el dato «Grade» en `sq-label` y el estado «Pending» en `sq-gstep__state`). `en.json` admite la clave «contextos»: clase CSS del elemento que contiene el texto → {español → inglés}, que `revision.js` mira antes que «textos».
- Cuando dos cadenas españolas de la app tengan la misma forma y distinto inglés, gana la manual; el comando falla si hay conflicto sin resolver, en lugar de elegir una.

Un test (como `TokensSincronizadosTest`) falla si `maquetas/en.js` no coincide con sus fuentes.

### 3. La comprobación de «sin traducir» se hace al fotografiar

Con `idioma=en`, `revision.js` marca en el DOM (`data-rv-sin-traducir`) cada texto visible que tenga letras, no esté en el diccionario y no sea un dato que valga en los dos idiomas (cifras, unidades, marcas y nombres propios: lista corta en `en.json`, clave `"=": [...]`). `capturas.py` descarga el DOM con `--dump-dom` antes de la captura y falla, indicando captura y texto, si hay marcas. Solo cuenta lo visible en el estado fotografiado, por eso el alcance se limita a los 12 estados.

Alternativa descartada: comprobar estáticamente el HTML. No sabe qué está visible en cada estado y daría falsos positivos en estados que no se fotografían.

### 4. Nombres de fichero e integración

- `site/img/<id>-<idioma>-<tema>.png`. `capturas.py` itera idioma × tema, acepta el filtro por `id` como ahora y borra las `<id>-<tema>.png` antiguas al terminar.
- `generar.py` construye `src` con el idioma de la página. `test_sitio.py` deja de exigir las mismas rutas en los dos idiomas y exige que cada ruta exista y lleve el idioma de su página.
- `site/capturas.json` no cambia: ancho, alto y estado valen para los dos idiomas. Si una captura inglesa desbordase por frases más largas, se ve al revisarla, no antes.

### 5. Regla de trabajo

Cada change de UI que toque una maqueta fotografiada añade el inglés del texto nuevo al diccionario (si no sale de `strings.xml`) y vuelve a lanzar `capturas.py`, que falla si falta. Se recoge en `AGENTS.md` (sección de UI) y en `docs/desarrollo.md`.

## Risks / Trade-offs

- [Texto inglés más largo que el español desborda o corta algo en una captura] → se revisan las 24 capturas inglesas al terminar; si una no cabe, se acorta el texto de la maqueta (no el de la app) o se ajusta la maqueta en un change de UI.
- [El diccionario por coincidencia exacta falla con texto partido por etiquetas] → la comprobación de «sin traducir» lo detecta; se traduce cada trozo por separado en `en.json`.
- [La maqueta y la app dicen cosas distintas en un mismo texto] → gana el diccionario manual; se anota en `en.json` y, si es un fallo de la maqueta, se corrige en ella.
- [El renderizado cambia unos bytes de una pasada a otra (`desarrollo.md`)] → con 48 imágenes el ruido en el diff crece; se mantiene la regla de hacer commit solo de lo que de verdad cambia.
- [`site/img/` pasa de 2,6 a unos 5,6 MB] → aceptable para Pages; no hay presupuesto de carga que lo impida (las imágenes de la segunda versión de idioma no se descargan al visitar la primera).

## Migration Plan

1. Diccionario, `revision.js` y comprobación; `capturas.py` en inglés sobre un estado como prueba.
2. Completar `en.json` hasta que las 12 capturas pasen la comprobación.
3. Generar las 48 imágenes, regenerar la web, revisar `site/en/` y `site/es/` con el usuario en local.
4. Sin despliegue propio: la Action de Pages publica `site/` cuando se publique la web. Para volver atrás basta con revertir el commit; `site/capturas.json` no cambia.
