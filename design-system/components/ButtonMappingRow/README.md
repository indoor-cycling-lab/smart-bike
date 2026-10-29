# ButtonMappingRow

Una asignación en la pantalla de botones: entrada del Square → acción, con la capa a la que pertenece.

**Recibe:** botón o combinación, gesto, acción, capa (`zwift` | `app`) y si está en modo captura.

**Uso**
- La lista se agrupa en dos secciones, en este orden: «Funciones de Zwift» y «Acciones de la app».
- Etiqueta de capa: ZWIFT en `surface-2`/`ink`; APP en `gear-soft`/`gear`.
- Si la fila abre otra pantalla (Asignar acción), es un enlace (`a.sq-map`) que conserva el color del texto, sin subrayado, con `chevron_right` al final y fondo `surface-2` al pulsarla.
- Las funciones de Zwift (las que llegan a Zwift por el mando de la app) son filas tocables como las de la app, con la etiqueta ZWIFT, y abren Asignar acción.
- Fila solo informativa (`.sq-map.is-info`): sin chevron ni estado táctil. Se distingue de la tocable por la forma, no por el color.
- Acción sin ninguna entrada: en lugar de las teclas, «Sin botón» en `ink-muted` (`.sin`).
- Una combinación separa sus teclas con «+» (`sq-plus`) y botones equivalentes con «/».
- Variante apilada (`.sq-map--stack`), para la pantalla de Botones agrupada por acción: la acción arriba en 600 (`sq-map__act`) y debajo sus entradas, una por línea (`.ent`, con las teclas en `.combo` sin partir y el gesto detrás). Cabe a 360 dp con combinaciones de dos teclas anchas.
- Al tocar la fila entra en captura: borde interior `gear` y el texto «Pulsa un botón o gesto en el Square…». El siguiente gesto se asigna; «Atrás» cancela.
- Si el gesto ya está asignado, avisar debajo con `caption` y ofrecer «Reemplazar».
