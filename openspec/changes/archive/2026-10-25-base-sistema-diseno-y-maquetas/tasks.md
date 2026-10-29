# Tasks

Este change solo tiene sección «Maqueta»: no incluye código de la app (ver proposal.md, «Fuera de alcance»).

## Maqueta

### 1. Comprobación de contraste

- [x] 1.1 Crear `scripts/design-system/contraste.py` (Python estándar): lee `tokens.json`, comprueba en rodaje y en claro los pares de texto (`ink`, `ink-muted`, `gear`, `climb`, `descent`, `sim`, `erg`, `ok`, `danger` sobre `bg` y `surface`; `ink` e `ink-muted` también sobre `surface-2`) a 4,5:1, `on-gear-fill` sobre `gear-fill` a 4,5:1 y `line-strong` sobre `bg` y `surface` a 3:1, y sale con código distinto de 0 indicando cada par que falle. Verificar: pasa con los tokens actuales y falla si se cambia temporalmente `ink-muted` de rodaje a `#5b6572`.
- [x] 1.2 Llamar a la comprobación al final de `scripts/design-system/generar.py`, de forma que el generador falle si el contraste falla, y documentarlo en `design-system/README.md` y `AGENTS.md`. Verificar: `python3 scripts/design-system/generar.py` imprime el resultado del contraste.

### 2. Fuentes e iconos locales

- [x] 2.1 Descargar en `design-system/fonts/` los woff2 (subconjunto latino) de Barlow 400 y 600, Barlow Condensed 500, 600 y 700 y JetBrains Mono 400, con sus licencias OFL, y declararlos con `@font-face` en `components.css` en lugar del `@import` de Google Fonts. Verificar: con la red desactivada, la guía y `maquetas/rodaje.html` se ven con Barlow Condensed en la marcha.
- [x] 2.2 Crear `design-system/icons.svg` con los Material Symbols Rounded usados (`arrow_back`, `chevron_right`, `pedal_bike`, `bluetooth`, `sports_esports`, `tune`, `settings`, `monitor_heart`, `directions_bike`, `cast`, `gamepad`, `drag_indicator`, `info`, `error`, `expand_more`) como `<symbol id="i-…">`, con su licencia Apache 2.0, y cambiar `.sq-icon` para pintar `<svg><use/></svg>` con `currentColor`. Documentarlo en la sección Iconografía de `design-system/README.md`. Verificar: `grep -r "Material Symbols\|googleapis" design-system maquetas` no devuelve nada.
- [x] 2.3 Sustituir los iconos de fuente por el sprite en las vistas previas (`AppBar`, `BottomNav`, `SettingsList`, `Notice`) y en todas las maquetas, y regenerar la guía. Verificar: cada icono se ve en las 9 maquetas y en la guía, en los dos temas.

### 3. Maquetas sin estilos en línea

- [x] 3.1 Hacer inventario de los atributos `style` de `maquetas/*.html` y `design-system/components/*/preview.html` y decidir para cada uno la clase, el modificador o la variable documentada que lo sustituye (design.md, decisión 1).
- [x] 3.2 Añadir a `components.css` los modificadores necesarios (p. ej. fila apilada de `SettingsList`, rejilla de rodaje sin columna de botones, `MetricGrid` en miniatura para la vista previa de «Datos en pantalla», etiqueta de acción en `SettingsList`) y la variable `--cog` de `CassetteView`, documentarlos en el `README.md` de cada componente y regenerar la guía. Verificar: la guía muestra las variantes nuevas en los dos temas.
- [x] 3.3 Quitar los atributos `style` de las maquetas y de las vistas previas usando lo anterior. Verificar: `grep -n 'style="' maquetas/*.html design-system/components/*/preview.html | grep -v -- '--'` no devuelve nada, y cada pantalla y estado se ve igual que antes en los dos temas.

### 4. Estados que faltan

- [x] 4.1 Añadir a `maquetas/rodaje.html` el estado «Neo desconectado»: chip del Neo sin conexión, marcha visible, potencia y cadencia con «—» atenuado. Enlazarlo en `maquetas/index.html`. Verificar: `rodaje.html#estado=neo-off` lo muestra y el índice lo enlaza.
- [x] 4.2 Documentar en `design-system/components/MetricTile/README.md` y en la vista previa de `MetricTile` el valor sin dato («—» en `ink-muted`) y regenerar la guía. Verificar: la guía lo muestra.

### 5. Coherencia con las specs

- [x] 5.1 Revisar cada escenario de las cuatro specs del change contra la guía y las maquetas, y corregir lo que no se cumpla (o ajustar la spec si lo validado es distinto, avisando al usuario). Verificar: `openspec validate base-sistema-diseno-y-maquetas --strict` pasa y no queda ningún escenario sin cubrir.
- [x] 5.2 Validación de la maqueta por el usuario: pasar los enlaces a la guía (`design-system/styleguide/`), al índice (`maquetas/`) y al estado nuevo `maquetas/rodaje.html#estado=neo-off`, y esperar su aprobación explícita.

## App

No aplica en este change.

## Workflow follow-up

- Archivar el change con `/openspec-archive-change` para crear `openspec/specs/design-tokens`, `ui-components`, `style-guide` y `screen-mockups`.
