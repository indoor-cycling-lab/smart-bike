# ServiceRow

Una fila por servicio que la app publica en Conexión (rodillo, controlador Zwift, pulso): por dónde va, si la app de entrenamiento lo usa y qué hacer para emparejarlo o qué falta.

**Recibe:** el icono y el nombre del servicio (Rodillo, Controlador Zwift, Pulso), por dónde se publica («por wifi», «por Bluetooth», «dentro de Smart Bike · por wifi»), su estado y una ayuda.

**Estados** (un `sq-conn`, como `ConnectionChip`, con palabra y forma)
- `ok`: «Conectado» (rodillo y controlador) o «Enviando» (pulso). Punto lleno.
- `wait`: «Esperando». Aro que gira.
- `off`: «Sin wifi», «Sin Bluetooth», «No visible» (rodillo) o «Sin lectura» (pulso). Aro vacío con borde `danger`.
- `idle`: «No disponible» (el móvil no puede publicar por Bluetooth). Raya.
- **No se ofrece** (`is-none` en la fila, con `idle`): falta el dispositivo configurado. Nombre, icono y estado en `ink-muted`. La fila entera es un enlace (`a.sq-svc`) a Dispositivos, con `i-chevron_right`.

**Forma**
- `sq-item sq-item--stack sq-svc`: cabecera `sq-svc__head` con el icono, el nombre y, debajo, por dónde va (`sq-caption`), y a la derecha el estado. Debajo, la ayuda en un `p`.
- Una nota de reconexión del dispositivo de origen («El Square se está reconectando») va en la ayuda, sin cambiar el estado: lo publicado no depende de que el dispositivo esté conectado.

**Uso**
- Solo en Conexión. Rodaje resume el estado en `PlatformBadge`.
- El orden es fijo: Rodillo, Controlador Zwift, Pulso. El icono del controlador es `i-zwift`: emula el controlador de Zwift.
- Objetivo táctil de `touch-min` cuando la fila es un enlace.

**No**
- No mostrar los bpm del pulso.
- No nombrar la app de entrenamiento en el estado: la app no sabe quién se ha conectado. Zwift solo aparece en la ayuda de emparejar, donde aplica.
