# ConnectionBar

La barra de conexiones de la pantalla de rodaje: un icono por enlace del móvil con un dispositivo, sin nombre ni borde, y un único objetivo táctil para todos.

**Recibe:** la lista de enlaces, en orden fijo, cada uno con su icono y su estado (`ok` | `wait` | `off`), y el destino al tocarla (Dispositivos). Lo que la app publica no va aquí: su estado lo da `PlatformBadge`.

**Iconos por enlace** (los mismos en Rodaje, Dispositivos y Diagnóstico)

| Enlace | Icono |
|---|---|
| Neo | `i-bolt` |
| Square | `i-manillar` (un manillar de bici de carretera: el Square es el controlador de la app) |
| Pulso | `i-favorite` |
| Ventilador | `i-mode_fan` |

**Estado por la forma del icono**, nunca solo por el color
- Conectado (`is-ok`): el icono en `ink`, sin marca.
- Conectando o esperando (`is-wait`): el icono en `ink-muted`, con un hueco circular recortado en la esquina inferior derecha y, dentro, un arco que gira (el mismo de `ConnectionChip`). Con movimiento reducido, el arco se queda quieto.
- Batería baja (`is-low`, solo el Square conectado): el icono en `ink` entero, con una insignia redonda en la esquina superior izquierda y un «!» dentro, sin girar. No usa `danger` ni un color de aviso: se distingue por la forma. Con el Square reconectando o sin conexión se muestra `is-wait` o `is-off`, no `is-low`. La descripción accesible dice «Controlador, batería baja». Rodaje no muestra el porcentaje.
- Sin conexión o no visible (`is-off`): el icono en `danger`, con un hueco diagonal recortado y una línea de 2 px en `danger` dentro: tachado.
- El ancho de la barra no depende del estado: cada icono mide 14 px (18 px, validado al principio, 16 px y 24 px también se probaron), con 4 px entre iconos (`space-1`; con 10 px se veían sueltos). La marca de espera y el tachado se escalan con el icono y sobresalen unos 1,75 px por la esquina, así que con 4 px no llegan a tocar al icono vecino.

**Reglas**
- Solo lleva los dispositivos a los que se conecta la app. Lo que la app publica para la app de entrenamiento (el rodillo, el controlador Zwift y el pulso) se ve con `PlatformBadge`, en la fila de Ajustes.
- Solo se ven los enlaces que se usan. Orden fijo: Neo, Square y, cuando existan, Pulso y Ventilador.
- Es un solo objetivo táctil (`a.sq-connbar`, de `touch-min` de alto) que abre Dispositivos: tocar un icono o el hueco entre dos es lo mismo. En la app, un único `clickable` de 48 dp. Con margen vertical negativo, la línea sigue midiendo 32 px y no le quita alto a la marcha.
- Descripción accesible única, con el estado de cada enlace: `aria-label="Conexiones: Neo, conectado; Controlador, sin conexión"`. Los iconos van con `aria-hidden`.
- **Dónde va.** En horizontal, en la línea superior, a la izquierda de Ajustes y separada de él 12 px (`space-3`). En vertical, a la derecha de la fila del estado de lo publicado (`sq-ride__statusrow`), bajo la barra de estado del sistema; Ajustes va una fila más abajo, al final de la fila del modo.
- Es solo para Rodaje. Fuera de Rodaje (Dispositivos, Diagnóstico), cada enlace va con el mismo icono delante de su nombre en un `ConnectionChip` o una fila, y el estado en palabra.
