# ConnectionChip

El estado de un enlace: Neo, Square, Zwift (rodillo) o Controlador Zwift (el controlador de Zwift emulado).

**Recibe:** nombre del dispositivo, su icono y estado (`ok` | `wait` | `off` | `idle`).

El chip es para Dispositivos y Diagnóstico. En Rodaje, los enlaces van en la `ConnectionBar`, con iconos y sin chips.

**Uso**
- Conectado: punto lleno en `ok` y solo el nombre.
- Conectando o esperando a Zwift: anillo girando en `ink-muted` y la palabra «esperando» o «conectando».
- Desconectado: anillo vacío y borde en `danger`, con «sin conexión». El estado nunca depende solo del color.
- No visible (`off`), solo para Zwift: la app se anuncia en la red wifi, pero el anuncio no responde, así que Zwift no puede encontrarla. Es una alerta, como «sin conexión», con la palabra «No visible»; en Rodaje, con la Z tachada (`ConnectionBar`). En Dispositivos va con un `Notice` de error que dice qué hacer.
- Desactivado o no disponible (`idle`), para un enlace que la app no intenta por decisión del usuario o porque el móvil no puede: un guion en `ink-muted` en lugar del punto y el borde normal, con la palabra («Desactivado», «No disponible»). No es ni espera ni alerta. Solo fuera de Rodaje: en Rodaje, el mando solo se ve si está activado y el rodillo y el mando van cada uno con su icono (`ConnectionBar`).
- El icono del enlace va delante del nombre, el mismo que en la `ConnectionBar` (`i-bolt`, `i-manillar`, `i-zwift`; el manillar es el del Square, e `i-zwift` es el de la plataforma y el del controlador publicado), como leyenda de la barra de Rodaje. El estado va también en palabra tras el nombre.
- Al tocarlo, abre el detalle del dispositivo. Tocable, es un enlace (`a.sq-conn`) con el mismo aspecto y un área táctil de `touch-min` de alto, más grande que el chip; en la app, un objetivo táctil de 48 dp. Mientras no exista la pantalla de Dispositivos, abre Diagnóstico.
