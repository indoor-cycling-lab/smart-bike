## Context

Así está hoy:

- **Barra superior de Rodaje** (`Rodaje.kt`, `BarraSuperior` en horizontal y la columna de `RodajeVertical`): pinta tres `ConnectionChip` compactos y después `BotonAjustes`.
- **Estado de los chips:** sale de `RodajeUiState`, con `neo`, `square` y `zwift` como `ChipUi`. `chipZwift(z, mando)` combina el estado del rodillo (`EstadoZwift`) con el del mando (`EstadoMando`) en un solo `EstadoChip`. Además guarda el mando para la descripción («Zwift, conectado; mando, esperando»).
- **Dispositivos y Diagnóstico** usan `ConnectionChip` con palabra. Diagnóstico pone un icono en cada fila, pero con otro reparto: `directions_bike` para el Neo, `sports_esports` para el Square y `gamepad` para el mando.
- **Iconos:** `scripts/design-system/generar.py` convierte cada `<symbol>` de `icons.svg` en `ic_<nombre>.xml`. Exige `viewBox="0 -960 960 960"` y **un solo `<path>` con relleno**: no admite trazos (`stroke`) ni varios elementos.
- **Iconografía:** la pauta de `design-system/README.md` dice que Rodaje no usa iconos. El icono `tune` de Ajustes ya era una excepción.
- **Exploración previa:** se hizo una prueba desechable con los iconos candidatos y los tres estados, en los dos temas y en las dos orientaciones. De ahí salen los iconos elegidos, la disposición y que la «Z» de trazo se leía como una letra.

## Goals / Non-Goals

**Goals:**
- Un componente `ConnectionBar` de ancho fijo para un conjunto de enlaces dado, que no dependa de qué enlaces existen. Así, añadir el pulso o el ventilador será solo un enlace más.
- El estado del rodillo y el del mando, separados desde `RodajeUiState`.
- Un único juego de iconos de enlace para toda la app.

**Non-Goals:**
- Cambiar `EstadoZwift`, `EstadoMando`, `EstadoDispositivo` o la `Sesion`.
- Animaciones de entrada o salida de iconos: un enlace solo aparece o desaparece al cambiar Ajustes, nunca rodando.

## Decisions

### 1. Componente nuevo `ConnectionBar` (`sq-connbar`), no una variante de `ConnectionChip`

`ConnectionChip` es una píldora con borde, punto y texto. La barra es otra cosa: iconos sueltos sin fondo y un único objetivo táctil para todos. Como variante, el chip necesitaría ramas en casi todo su CSS y en el composable. Por eso se crea un componente aparte, y `ConnectionChip` pierde la variante compacta, que se queda sin uso.

- **Estructura HTML:**
  ```html
  <a class="sq-connbar" href="…" aria-label="Conexiones: …">
    <svg class="sq-connbar__i is-ok|is-wait|is-off">…</svg>…
  </a>
  ```
- **Composable:** `ConnectionBar(enlaces: List<EnlaceUi>, alTocar, modifier)`, con `EnlaceUi(icono, nombre, estado, enPalabra)`.
- **Alternativa descartada:** una fila de `ConnectionChip` sin texto ni borde. Arrastraría el relleno de 32 px del chip y un objetivo táctil por icono, que es justo lo que se quiere quitar.

### 2. Estados dibujados sobre el icono, iguales para todos

- **Conectado:** el icono en `ink`.
- **Esperando:** el icono en `ink-muted`, con un hueco circular recortado en la esquina inferior derecha y, dentro, un arco que gira. Es el mismo arco de 270° de `ConnectionChip` y para con movimiento reducido.
- **Sin conexión:** el icono en `danger`, con un hueco diagonal de 5 px recortado de esquina a esquina y una línea de 2 px en `danger` dentro.

El recorte («halo») separa la marca del dibujo para cualquier icono:
- En HTML, con un `<mask>` SVG común.
- En Compose, con `drawWithContent` y `BlendMode.Clear` sobre una capa (`graphicsLayer(compositingStrategy = Offscreen)`).

**Alternativa descartada:** las variantes `_off` de Material. No existen para todos los iconos (`bolt`, ni para el icono propio de Zwift) y cada una se rompe de una forma distinta.

### 3. Icono propio de Zwift, de un solo trazado relleno

La «Z» en recuadro redondeado se dibuja como **un único `<path>` relleno**, porque `generar.py` no admite trazos:
- el contorno exterior y el interior del recuadro (evenodd o subtrayectoria inversa);
- la Z como polígono con las esquinas redondeadas.

El grosor es de 80 unidades (2 px a 24 px), como Material peso 400. Va en `icons.svg` como `i-zwift`, con un comentario que lo marca como propio del proyecto (no Material, sin la licencia Apache) y que dice que no imita el logo de Zwift.

**Alternativa descartada:** ampliar `generar.py` para que admita `stroke`. Sería más trabajo, y un solo trazado relleno es lo que hace Material de todos modos.

### 4. Iconos por enlace

| Enlace | Símbolo | Uso |
|---|---|---|
| Neo | `i-bolt` (nuevo) | Rodaje, Dispositivos, Diagnóstico |
| Square | `i-directions_bike` (ya existe) | Rodaje, Dispositivos, Diagnóstico |
| Zwift | `i-zwift` (nuevo, propio) | Rodaje, Dispositivos, Diagnóstico |
| Mando | `i-sports_esports` (ya existe) | Rodaje, Dispositivos, Diagnóstico |
| Pulso | `i-favorite` (nuevo) | Solo en la vista previa de la guía |
| Ventilador | `i-mode_fan` (nuevo) | Solo en la vista previa de la guía |

- **Diagnóstico** cambia sus iconos para coincidir con esta tabla. `i-gamepad` deja de usarse para el mando; se retira del sprite si nada más lo usa.
- **`favorite` y `mode_fan`** se añaden al sprite aunque la app no los use todavía. `generar.py` creará sus `ic_*.xml`. Se acepta: son dos vectores pequeños y quedan listos para cuando lleguen esos enlaces.
- La regla del sprite «solo los que usa la interfaz» se amplía a «o su guía».

### 5. `RodajeUiState` con una lista de enlaces

- `chipZwift` desaparece.
- `RodajeUiState` expone `enlaces: List<EnlaceUi>`, calculada en `rodajeUiState(...)`:
  - el Neo y el Square con `chip(e)`, como hoy;
  - Zwift con su propio estado (`Conectado`: ok; `Esperando`: espera; `NoVisible` y `SinWifi`: tachado);
  - el mando solo si `EstadoMando` no es `Desactivado` ni `NoDisponible` (`Esperando`: espera; `SinConexion`: tachado; `Conectado`: ok).
- `EstadoChip` se reutiliza como estado del icono: `Ok`, `Esperando` y `Sin`. `Neutro` no se usa en la barra.
- La descripción accesible se arma en la UI con las cadenas existentes (`chip_conectado`, `chip_esperando`…) y una nueva para la cabecera «Conexiones: %s», unidas con «; ».

La lista mantiene el orden fijo y deja preparado añadir enlaces, sin campos sueltos por dispositivo. Los tests de `RodajeUiState` que hoy prueban `chipZwift` pasan a probar la lista: el orden, la presencia del mando y el estado separado.

### 6. Disposición

- **Horizontal:** `BarraSuperior` sigue siendo una fila con el modo y la transmisión a la izquierda. A la derecha van `ConnectionBar` y `BotonAjustes`, separados por `space-6` (24 dp) para que no se toque uno por otro. Si no cabe en una línea, se mantiene el `FlowRow` de hoy.
- **Vertical:** la barra va arriba a la derecha, en la **franja superior**, y debajo hay una línea con `Modo` a la izquierda (que ocupa el ancho que deja `BotonAjustes`) y `BotonAjustes` a la derecha. Lo decidió el usuario tras probarlo en el OnePlus 8: aprovecha la franja de la cámara, que antes quedaba vacía, y sube la marcha una línea.
  - **Franja:** su alto es el del *inset* superior seguro (`WindowInsets.safeDrawing`, el *cutout* con las barras del sistema ocultas) con un mínimo de `touch-min` (48 dp), y la barra se centra en ella. El contenido de Rodaje se coloca debajo de la franja, de modo que en un móvil sin cámara en el borde (*inset* 0) la franja es de 48 dp y no se solapa con el modo. El resto de la pantalla sigue dentro del área segura, con el relleno de `space-4` de siempre.
  - **Modo largo:** con «Shimano 105 · automático», SRAM o ERG el texto comparte línea con Ajustes. Si no cabe, pasa a dos líneas en lugar de cortarse; se comprueba en la app.
- **Medidas:** cada icono mide 18 dp, con 10 dp entre iconos, y la barra tiene un alto táctil real de 48 dp. Se probó con 24 dp y con 20 dp. La marca de espera y el tachado se dibujan en unidades de un icono de 24 dp y se escalan al tamaño real. No se usa un margen vertical negativo, porque Compose no entrega los toques fuera de los límites del padre y el objetivo de 48 dp se perdería: la línea mide 48 dp, como ya medía con `BotonAjustes`.

### 7. Texto del pie

`tocar_para_conectar` pasa a «Toca las conexiones para conectar el Neo y el Square». Se revisa en la maqueta: es texto, no disposición, y si el usuario prefiere otro se cambia sin tocar nada más.

## Risks / Trade-offs

- **[Iconos sin nombre que hay que aprender]** → Dispositivos y Diagnóstico sirven de leyenda con el mismo icono junto al nombre, y la barra lleva a Dispositivos con un toque. La validación de la maqueta comprueba que se reconocen a 1–2 m.
- **[«Esperando» poco visible a 18 dp]** (visto en la prueba desechable y en el móvil, con la marca más pequeña) → se comprueba rodando a 1–2 m en la prueba con el equipo; si no se distingue, se sube el icono a 20 dp, que también se probó, o se afina la marca y la opacidad. Si hace falta más, un parpadeo suave que respete el movimiento reducido. No cambia las specs.
- **[El mando y la pestaña Botones comparten `sports_esports`]** → la barra inferior no se ve en Rodaje, así que no coinciden en pantalla. Ver Open Questions.
- **[Un solo objetivo táctil: no se puede ir al detalle de un enlace concreto]** → Dispositivos ya muestra todos los enlaces en una sola pantalla, así que no se pierde nada.
- **[Recorte con `BlendMode.Clear` en Compose]** → necesita una capa fuera de pantalla por icono. Con seis iconos de 18 dp el coste es despreciable. Se comprueba en el OnePlus 8 que no hay parpadeos al cambiar de tema.

- **[Modo largo junto a Ajustes en vertical]** → puede pasar a dos líneas con la transmisión más larga y con ERG. Se prueba con las transmisiones del catálogo y se cuenta con ello al repartir el alto de la marcha.
- **[Cámara que no está en la esquina superior derecha]** → la barra queda a la derecha de la franja, lejos del agujero de la cámara del OnePlus 8 (arriba a la izquierda). Con una cámara en el centro o a la derecha, la barra podría quedar tapada: se resuelve con el *inset* del *cutout* si hace falta, sin tocar las specs.

## Migration Plan

No hay datos que migrar: el cambio es solo visual. En la app no queda nada que retirar después; en el sistema de diseño se retira la variante compacta de `ConnectionChip` (`.sq-ride--v .sq-conn`).

## Open Questions

- **Icono del mando:** `sports_esports`, elegido por el usuario, es también el de la pestaña Botones. Si al validar la maqueta resulta confuso, se cambia por `gamepad` sin tocar las specs, que solo piden «un mando de videojuegos».
