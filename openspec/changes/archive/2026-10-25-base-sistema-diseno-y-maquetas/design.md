# Design

## Context

Estado actual (ver proposal.md para el porqué):

- `design-system/` tiene `tokens.json` como fuente, `tokens.css` y `styleguide/index.html` generados por `scripts/design-system/generar.py`, `components.css` con las clases `sq-*` y una carpeta por componente con `README.md` y `preview.html`.
- `maquetas/` tiene 9 pantallas en HTML estático editadas a mano, con `revision.js` y `revision.css` (selector de estado y tema, marco de dispositivo) y un `index.html`.
- Desviaciones respecto a las specs de este change:
  - Las maquetas y algunas vistas previas usan atributos `style` con valores literales (alturas del cassette, ajustes de rejilla, tamaños de la vista previa de datos, disposición de algunas filas).
  - Las fuentes y los iconos (Material Symbols) se cargan de Google Fonts.
  - No hay comprobación de contraste.
  - La maqueta de rodaje no tiene el estado de Neo desconectado con datos «—».

## Goals / Non-Goals

**Goals:**
- Que el repositorio cumpla las cuatro specs de base sin cambiar el aspecto validado.
- Que la guía y las maquetas funcionen sin conexión.
- Que el contraste se pueda comprobar con un comando.

**Non-Goals:**
- Generar las maquetas con un script: siguen siendo HTML editado a mano.
- Tema de Compose o cualquier código Android.

## Decisions

### 1. Estilos en línea → clases y variables documentadas

Cada `style="…"` de las maquetas pasa a una clase o modificador de `components.css`, documentado en las pautas del componente. Cuando el valor es un dato (altura de cada piñón según sus dientes), se pasa como variable CSS documentada: `style="--cog: 64%"` en `CassetteView`. La spec admite variables documentadas; es el mismo criterio que minuto-y-resultado con `--team-accent`.

- Alternativa: permitir `style` en las maquetas. Descartada porque las maquetas son la referencia de lo que el sistema ofrece: si una pantalla necesita un estilo, el sistema debe tenerlo, y la app lo necesitará también.
- Alternativa: una clase por altura de piñón. Descartada: las alturas dependen del cassette elegido.

La vista previa en miniatura de «Datos en pantalla» se convierte en una variante del componente (`MetricGrid` en miniatura) en lugar de redefinir tamaños en línea.

Inventario (tarea 3.1) y sustitución de cada caso:

| Estilo en línea | Dónde | Sustituto |
|---|---|---|
| `height: N%` en `.sq-cog` | rodaje, rodaje vertical, marchas, datos; vistas previas `CassetteView`, `RideScreen` | Variable documentada `--cog: N%` (`.sq-cog` usa `height: var(--cog, 100%)`) |
| `grid-template-columns` de dos columnas en `.sq-ride__main` | rodaje (todos los estados sin botones) | La rejilla tiene dos columnas por defecto y añade la tercera solo si contiene `.sq-ride__buttons` |
| `margin: 0` en `p.sq-caption` y en `p` de una fila apilada | botones, diagnóstico, marchas, datos, dispositivos | `.sq-caption` sin margen; los `p` de `.sq-item--stack`, sin margen |
| `margin-bottom: 8px` en `legend` | ajustes, asignar; vista previa `SettingsList` | `.sq-section > legend` con `space-2` debajo |
| `flex-direction: column; align-items: stretch; gap` en `.sq-item` | dispositivos, diagnóstico, marchas | Modificador `.sq-item--stack` (fila apilada). La escalera de marchas se agrupa con sus extremos, como en rodaje |
| `align-self: flex-start` en `.sq-toast` | diagnóstico | Dentro de `.sq-item--stack`, el aviso mantiene su ancho |
| `justify-content: space-between` en `.sq-row` | datos, diagnóstico, marchas | Modificador `.sq-row--between` |
| `flex: 1` en `.sq-item__name` | dispositivos | Envolverlo en `.sq-item__text`, que ya crece |
| Rejillas de teclas de 4 y 2 columnas | diagnóstico | `.sq-grid` (2 columnas) y `.sq-grid--4`, con `space-2` de separación |
| Rejilla de 2 columnas con `gap: 16px` en `.sq-item` | diagnóstico | `.sq-item.sq-grid.sq-gap-4` |
| `font-size: 28px; line-height: 32px` en `.sq-metric-val` | diagnóstico | Tamaño `.sq-metric-val.md` (`metric-md`) |
| `line-height: 32px` en un valor de texto | diagnóstico | `.sq-metric-val.text`: valor en texto a la altura de `md` |
| `font-family: var(--font-mono); font-size: 13px` en `code` | diagnóstico | Clase `.sq-mono` (estilo `mono`) |
| `gap: 4px` en `.sq-col` y `font-size: 18px` en el nombre | asignar | `.sq-gap-1` y `.sq-section__title` (estilo `heading`) |
| `display: grid; place-items: center; text-decoration: none` en `a.sq-btn` | asignar | `a.sq-btn` centra su texto y no se subraya |
| `color: inherit; text-decoration: none` en `a.sq-map` | botones | `a.sq-map` hereda el color y no se subraya |
| `cursor: pointer; list-style: none` en `summary.sq-item` | marchas | `summary.sq-item` sin marcador y con cursor de enlace |
| Miniatura de la pantalla de rodaje (rejilla, cifra, cassette, métricas y etiquetas reducidas) | datos | `.sq-ride-mini` y `.sq-mgrid--mini`, con tamaño de cifra según `data-n` |
| `max-width` de la demo (390, 420, 560, 640 px) | vistas previas | `.sq-w-phone` (390 dp, móvil en vertical) y `.sq-w-panel` (640 dp) |
| `gap` y `align-items` de la demo | vistas previas | `.sq-gap-1`, `-2`, `-4`, `-6`, `-8`, `-12` y `.sq-row--start`, `--end` |
| `width: 300px` en `.sq-capture` | vista previa `CapturePanel` | Las dos variantes en una `.sq-grid` de `.sq-w-panel` |
| `gap: 0` en `.sq-col` | vista previa `Stepper` | `.sq-item__text`, como en las maquetas |
| `align-items: flex-end; gap; padding-bottom` en `.sq-col` | vista previa `RideScreen` | `.sq-ride__rings`, que ya lo define |
| `padding: var(--space-2) 20px` en `.sq-toast` | vistas previas `ActionToast`, `RideScreen` | Se quita: las maquetas de rodaje usan el relleno del componente |
| Tamaños de cifra y rejilla redefinidos con `.zone` y `.v` | vista previa `MetricGrid` | La vista previa usa `.sq-mgrid[data-n]` real dentro de una zona fija |

### 2. Fuentes locales en woff2 y los iconos como sprite SVG

- **Texto:** Barlow (400, 600), Barlow Condensed (500, 600, 700) y JetBrains Mono (400) en woff2, subconjunto latino, en `design-system/fonts/` con sus licencias OFL, declaradas con `@font-face` en `components.css`. Son los mismos ficheros que luego irán a `res/font` en la app (NF5: sin internet).
- **Iconos:** un sprite `design-system/icons.svg` con solo los Material Symbols Rounded que se usan (unos 15: `arrow_back`, `chevron_right`, `pedal_bike`, `bluetooth`, `sports_esports`, `tune`, `settings`, `monitor_heart`, `directions_bike`, `cast`, `gamepad`, `drag_indicator`, `info`, `error`, `expand_more`), con su licencia Apache 2.0. Se usan con `<svg class="sq-icon"><use href="…#i-nombre"/></svg>`.
  - Alternativa: la fuente variable completa de Material Symbols. Descartada por su tamaño (varios MB) para unos pocos glifos.
  - En la app, los mismos iconos se importan como vectores de Android a partir de los SVG.

Los ficheros se descargan una vez de sus fuentes oficiales (Google Fonts y el repositorio de Material Symbols) durante la implementación; después no hay ninguna petición externa.

### 3. Comprobación de contraste en Python

`scripts/design-system/contraste.py` lee `tokens.json`, calcula el ratio WCAG de una lista explícita de pares (texto sobre fondos, `on-gear-fill` sobre `gear-fill`, bordes de controles sobre fondos) en los dos temas y sale con error si alguno no llega a su mínimo. `generar.py` lo ejecuta al final.

- Alternativa: un script de Node como en minuto-y-resultado. Descartada para no añadir Node a un proyecto Android; el generador ya es Python de la biblioteca estándar.

La primera ejecución detectó que `line-strong` de rodaje (`#5b6572`) daba 2,89:1 sobre `surface`, por debajo del 3:1 que exigen la spec y la pauta del token. Con el visto bueno del usuario se aclara a `#5f6976` (3,07:1 sobre `surface`, 3,37:1 sobre `bg`). Es la única excepción a «sin cambios de aspecto» y se revisa en la validación de la maqueta.

### 4. Estado «Neo desconectado» en rodaje

Se añade a `maquetas/rodaje.html` para cubrir la conexión perdida: chip del Neo sin conexión, marcha visible (es estado de la app) y potencia y cadencia con «—».

## Risks / Trade-offs

- [Al quitar estilos en línea cambia algo del aspecto validado] → Revisar cada pantalla y estado en la tarea de validación, comparando con la versión actual en los dos temas.
- [Los nombres o trazos de un icono descargado no coinciden con el usado hasta ahora] → Usar los mismos nombres de Material Symbols Rounded, peso 400 y relleno 0.
- [La lista de pares de contraste se queda corta al añadir tokens] → Las pautas de `tokens.json` indican sobre qué fondos se usa cada color; la tarea de alta de un token incluye añadir sus pares.

## Correcciones de la revisión contra las specs (tarea 5.1)

- **Tope en rodaje:** el estado `tope` de `maquetas/rodaje.html` mostraba 50×11 en `gear`, sin atenuar ni etiqueta. Se alinea con `GearDisplay` y con la spec: cifra en `ink-muted` y «Tope · marcha más dura» debajo.
- **Color de marca:** la spec limitaba el ámbar a marcha, acción principal, foco y pestaña activa, pero el diseño validado también lo usa en controles elegidos o pulsados y en lo que resuelve la app (etiqueta APP, aviso informativo, lo enviado al Neo). Se amplía la spec a lo validado, y con ella el principio 3 del README y la pauta del token `gear`.
- **Guía:** los estilos propios de la guía (`.sg h1`, `.sg a`) y los de la vista previa de `RideScreen` (`.ride`, `.main`…) se colaban en las demos; se limitan a sus elementos y se prefijan. Cada demo sale dos veces (una por tema) y el generador prefija ahora `name`, `id` y `for` con el tema, para que los grupos de radio de las dos copias no se mezclen.
- **`GearLadder`:** la vista previa pasa a tener la referencia en la 12, como el escenario de la spec y la maqueta de Marchas.
- **Enlace como botón:** `a.sq-btn` usa `box-sizing: border-box`; el «Cancelar» de Asignar acción medía 2 px más que «Guardar».
