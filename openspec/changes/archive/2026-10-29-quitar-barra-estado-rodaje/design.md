# Design

## Context

Motivación y alcance en `proposal.md`. Estado actual relevante:

- `RodajeRuta` oculta las barras del sistema (`hide(systemBars())`, comportamiento transitorio por deslizamiento) y las restaura al salir. `MainActivity` ya llama a `enableEdgeToEdge` y el resto de pantallas aplican `safeDrawingPadding()`. `Rodaje.kt` usa `safeDrawing` en las dos orientaciones, así que los insets ya llegan al contenido.
- La réplica cruza cuatro capas: `:core` (`EstadoMovil`, `Bateria`, `colocarFranja`), `:app/movil` (`AdaptadorEstadoMovil`: `TIME_TICK`, `ACTION_BATTERY_CHANGED`, el wifi de `AdaptadorWifi`), el componente `DeviceStatus` y la fila superior de `Rodaje.kt` (en vertical, `Layout` de la franja con `colocarFranja`; en horizontal, `Layout` de tres columnas con el grupo centrado).
- `AdaptadorWifi` y el permiso `ACCESS_NETWORK_STATE` los usa también el estado de la plataforma («Sin wifi») y la Z de Zwift tachada, así que se quedan.
- El tachado y la marca de espera de `ConnectionBar` se dibujan con capas fuera de pantalla compartidas con el wifi de `DeviceStatus` (comentario en `ConnectionBar.kt`). Hay que dejar el helper solo para la barra.
- Las maquetas no dibujan ninguna barra del sistema ni cámara en el marco; hoy la cámara es un estado propio (`camara-centro`) y la franja se reserva con altura mínima de 48 dp.

## Goals / Non-Goals

**Goals:**
- Que el sistema pinte la hora, el wifi y la batería, y no quede en el repositorio nada que los calcule o los dibuje.
- Una fila de la plataforma con las conexiones a la derecha, una fila del modo con Ajustes a la derecha, y la barra de conexiones agrupada.

**Non-Goals:**
- Cambiar el tema de las barras del sistema (`app-theme` ya lo fija) ni forzar su color en Rodaje.
- Rediseñar el estado de la plataforma o el contenido de la barra de conexiones.
- Reaprovechar `AdaptadorEstadoMovil` para otra cosa (hora o batería en Diagnóstico): nada lo pide hoy.

## Decisions

1. **Barras visibles sin modo inmersivo.** Se borra el bloque `hide`/`show` y el `systemBarsBehavior` de `RodajeRuta`; `keepScreenOn` se queda. Alternativa descartada: mantener el modo inmersivo sin réplica, que pierde la batería a mitad de una sesión larga con la pantalla siempre encendida; el usuario ya eligió mostrar las barras (exploración previa).
2. **Los insets se quedan como están.** `safeDrawing` ya cubre las barras visibles y la cámara; no se añade ningún cálculo propio. Sin `colocarFranja`, el recorte lo resuelve el sistema (la barra de estado ocupa el borde de la cámara). Se borran `colocarFranja`, `Tramo` y `Colocacion` y su test, no solo se dejan sin uso.
3. **Se borra todo, sin dejar el componente «por si acaso».** `DeviceStatus` solo se usa en Rodaje y tiene dos tests de instrumentación; no hay segundo consumidor. Se eliminan también los 8 drawables `ic_battery_android_*` e `ic_wifi`, y sus símbolos en `icons.svg` (los drawables se regeneran con `generar.py`, no se borran a mano), la sección de la guía y los tests `IconoBateriaTest`, `EstadoMovilTest` y los de `ComponentesTest`/`RodajeTest`/`RodajeUiStateTest` que los ejercitan.
4. **Fila de la plataforma en vertical.** `sq-ride__strip` desaparece. La fila `sq-ride__statusrow` pasa a llevar el estado de la publicación a la izquierda y `sq-connbar` a la derecha; el estado cede ancho antes que la barra (la barra no cambia de ancho con el estado), con hasta dos líneas. Alternativa descartada: recortar el estado con puntos suspensivos, porque su texto es lo que distingue «Conectado · sin controlador» de «Conectado».
5. **Ajustes en la fila del modo.** `sq-ride__settings` pasa a `sq-ride__moderow`, a la derecha. La descripción del modo («SRAM 2×12 · 48/35 · 10–33») es lo que cede, hasta dos líneas, como ya hace con «Shimano 105 · automático». Su objetivo táctil de 48 dp (`::before` con `inset`) se mantiene y no debe solaparse con el de la barra, que queda una fila más arriba.
6. **Horizontal: una fila de dos extremos.** El `Layout` de tres columnas se sustituye por una fila con la plataforma a la izquierda (`weight`, para que ceda) y las conexiones con Ajustes a la derecha (hueco `space-3` en vez de `space-6`). `sq-ride__top--status` pasa de rejilla de tres columnas a `flex` con `space-between`.
7. **Hueco entre iconos de 4 dp.** `gap: 10px` → `var(--space-1)` en `.sq-connbar` y `spacedBy(10.dp)` → `SqTema.espacios.space1` en `ConnectionBar.kt`. La marca de espera sobresale `-3·u` por la esquina y el tachado `-3·u` por la izquierda, con `u = 14/24`: unos 1,75 dp. Con 4 dp de hueco no se tocan, pero se comprueba en la maqueta con un enlace esperando junto a uno tachado, y en `ConnectionBar` con un test de instrumentación de captura de la fila.
8. **Maqueta: barra del sistema de adorno.** Para que se vea cuánto alto se gana y se pierde, el marco de las maquetas de Rodaje (`revision.css`, no el sistema de diseño) dibuja una barra de estado simulada de 24 dp, sin hora ni iconos reales. Es herramienta de revisión, no un componente. Alternativa descartada: no dibujar nada, que haría parecer que la pantalla sube hasta el borde.
9. **Estados de las maquetas.** Se quitan `camara-centro`, `bateria-baja` y `movil-cargando`. `sin-wifi` se queda. El resto de estados pierde el grupo `sq-devstatus` y, en vertical, la franja; se hace con una pasada sobre `rodaje.html` y `rodaje-vertical.html` (el grupo está repetido por estado) comprobada con búsqueda antes y después, para no dejar ninguno.
10. **Limpieza de la web pública.** Las capturas del sitio salen de las maquetas: se regeneran con `capturas.py` y se revisa `site/capturas.json` y los textos de `es.json`/`en.json` que mencionen la hora, el wifi o la batería en Rodaje.
11. **Sin relleno sobre la primera fila.** Pedido tras verlo en el móvil: el hueco entre la barra del sistema y el chip era de unos 24 dp (16 de relleno más 8 de la zona táctil de 48 sobre el chip de 32). Rodaje, en las dos orientaciones, no pone relleno arriba (`sq-ride` y `sq-ride--v`: `space-2` con el margen negativo de las zonas táctiles; Compose: `top = 0`): la zona táctil de 48 dp llega hasta la barra y el chip queda a 8 dp. Es un ajuste fino cubierto por tokens, sin nueva validación de maqueta.
12. **Horizontal: el modo sube a la línea superior y el estado va centrado.** Pedido tras verlo en el móvil: en horizontal hay sitio en la primera fila, y el modo en la esquina de la zona principal reserva 44 dp de alto (`ALTO_MODO`, `padding-top` de la marcha y los datos). La línea pasa a tres columnas: [modo con la transmisión] a la izquierda, [estado de la publicación] centrado en la pantalla y [conexiones][Ajustes] a la derecha (`grid` `1fr auto 1fr`; en Compose, un `Layout` que coloca el estado en el centro). Si el modo no cabe en su lado, la transmisión pasa a dos líneas; el estado y las conexiones no se mueven del sitio. Alternativa descartada: modo y estado juntos a la izquierda (primera maqueta), que deja el estado descentrado y vacío el centro de la línea. Cambia la disposición, así que pasa de nuevo por maqueta y validación.

## Risks / Trade-offs

- [La barra del sistema resta unos 24 dp de alto y la de navegación otros] → La pantalla baja de 760 dp ya compacta el vertical; se revisa a 360 × 640 que la marcha con 6 datos y botones sigue por encima de la mitad de su tamaño (spec «La marcha no desaparece en pantalla baja»). La franja que se quita (≥ 48 dp) compensa.
- [La fila de la plataforma queda apretada en móviles estrechos con la insignia larga y cuatro iconos] → La barra no cede; el estado pasa a dos líneas. Se valida en la maqueta a 360 dp antes de la app (puerta de validación).
- [Ajustes en la fila del modo lo acerca a la caption y al aviso de la acción] → Se revisa en los estados de ERG y con avisos para que no se pise.
- [La barra de estado del sistema queda a la vista en una pantalla que se mira a 1–2 m y que antes la ocultaba] → Es lo que se ha elegido; Android atenúa su contenido solo si el usuario lo pide.
- [Hay que dejar de ofrecer la prueba pendiente de `barra-estado-rodaje`] → Su tarea 2.8 queda sin objeto: se anota en `docs/planificacion.md` que la retira este change.
- [Borrar iconos de `icons.svg` y regenerar puede dejar un `ic_*.xml` huérfano o un test de sincronía en rojo] → Se ejecuta `python3 scripts/design-system/generar.py` y `:app:testDebugUnitTest` (`IconosSincronizadosTest`) después de quitar los símbolos.

## Migration Plan

Sin migración de datos ni de ajustes: nada de lo que se borra se persistía. Reversible con `git revert` del change, ya que no toca el formato de `configuracion.json`. Orden: maqueta y sistema de diseño (con validación del usuario), después la app (retirar el consumo en `Rodaje.kt`, luego la capa de datos y el dominio, luego los recursos), y por último documentación y capturas.

## Open Questions

- Si el estado de la publicación debe pasar a dos líneas o recortarse en el peor caso a 360 dp: la maqueta lo decide al revisarla y, si cambia la regla, se ajusta la spec en la puerta de validación.
