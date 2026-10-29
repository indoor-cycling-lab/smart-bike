# GuideStep

Fila de un paso de la página «Para empezar» de la guía de inicio (`GuideSheet`). Dice qué hacer, el estado real si lo tiene y lleva a la pantalla donde se hace.

**Pasos, en orden natural**
1. **Empareja tus dispositivos** → Dispositivos. Con ayuda de qué falta («Rodillo (necesario) y controlador (opcional)») o de lo que ya está conectado («Tacx Neo 1 y Elite Square conectados»). Es el único paso con estado.
2. **Configura la conexión** (Opcional) → Conexión. Cómo se conecta la app de ciclismo indoor a Smart Bike; el modo ya viene elegido por defecto y se dice en la ayuda («Ahora: Wifi + Bluetooth»). Sin estado: no se «completa».
3. **Ajusta la app** → Ajustes (tipo de marcha, botones, pantalla…). Sin estado.

Conectar la app de ciclismo indoor no es un paso de esta página: es la página 3 de la guía, que dice el estado de esa conexión.

**Estados** (`is-ok`, `is-wait`, `is-todo`; `is-info` los que no tienen)
- **Hecho:** punto relleno en `ok` y la palabra «Hecho».
- **Conectando** (rodillo): anillo girando en `ink-muted` y «Conectando».
- **Pendiente:** raya corta en `ink-muted` y «Pendiente»; nunca en `danger`: falta algo, no ha fallado nada. A medias (rodillo conectado y sin controlador): raya y «Falta el controlador».
- **Informativo** (`is-info`): sin punto visible (conserva su hueco para alinear el texto con los demás) y sin palabra de estado. Lleva a su pantalla con el mismo chevron que los otros pasos, sin icono propio.

**Uso**
- Es un enlace (`<a class="sq-gstep">`) de fila completa, de `touch-min` (48 dp) como mínimo, con un chevron al final.
- El estado se dice con la forma del punto y con la palabra, no solo con el color. Contraste de 4,5:1 en los dos temas con los tokens existentes.
- Accesibilidad: el `aria-label` del enlace da la lectura completa («Empareja tus dispositivos, hecho», «Configura la conexión, opcional. Modo Wifi + Bluetooth»).

**No**
- No usar `danger` ni el icono de error para un paso pendiente.
- No repetir aquí lo que ya explica la página 1 (qué es cada pieza).
