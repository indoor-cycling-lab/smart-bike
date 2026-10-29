# SettingsList

Las piezas de las pantallas de ajustes: secciones con título y grupos de filas con borde, donde cada fila es un enlace, un interruptor, una opción, un `Stepper` o un `ButtonMappingRow`.

**Piezas**
- `sq-section` con `sq-section__title` (`heading`). Si agrupa opciones excluyentes es un `fieldset` con `legend`.
- `sq-list`: el grupo, con borde `line`, `radius-md` y separadores entre filas.
- `sq-item`: fila de `touch-min` con texto (nombre en 600 y ayuda en `caption`) y un elemento al final: `chevron_right` si navega, casilla si activa algo, asa `drag_indicator` si se reordena.
- `sq-opt`: opción de radio con descripción (radio dibujado, no el nativo: aro `line-strong`; elegido, aro y punto `gear`, el mismo acento que el anillo de la fila, igual en la app); la elegida lleva un anillo `gear` de 2 px dentro de la fila, que en la primera y la última sigue la esquina redondeada de la lista. Con el `input` `disabled` (p. ej. el modo del ventilador sin conexión), toda la opción baja al 45 % de opacidad y deja de responder; la elegida conserva su anillo y su punto, así que sigue marcada sin depender del color.
- `sq-seg`: control segmentado para 2–4 opciones cortas; la elegida en `gear-fill`. Si lo titula una `legend` con `sq-label`, esta deja `space-2` debajo.
- `sq-item--stack`: fila apilada, para contenido que no cabe en una línea (cabecera con estado, párrafos de ayuda, teclas, platos y cassette). Sus hijos ocupan todo el ancho, separados por `space-3` (o el `sq-gap-*` que se indique); los párrafos no tienen margen y un `ActionToast` mantiene su ancho.
- Fila de dato ordenable (Datos en pantalla): `div.sq-item` con la casilla (`sq-check`) al principio, el texto como `label` de la casilla y el asa (`sq-handle`, `drag_indicator`, de `touch-min`) al final. Los datos no elegidos llevan `.is-off`: el nombre baja a `ink-muted` y la fila sigue activa para marcarla. El único dato elegido queda marcado y con la casilla `disabled`, y su ayuda dice que hace falta al menos uno, para que no dependa del color.
- `summary.sq-item`: cabecera de una sección plegable (`details.sq-list`), sin el marcador del navegador y con `expand_more` al final.

**Uso**
- La fila entera es el objetivo táctil (`label` o `a` de toda la fila).
- Ayudas de una línea, en tuteo: qué cambia al activarlo.
- Una fila desactivada baja su nombre a `ink-muted` y su control queda `disabled`.
- El asa se arrastra para reordenar; para el lector de pantalla ofrece las acciones «Subir» y «Bajar», que mueven la fila un puesto. Su `aria-label` es «Reordenar <dato>».

**No**
- No anidar listas ni meter tarjetas dentro de una fila.
