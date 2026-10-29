# PlatformBadge

El estado de lo que la app publica (el rodillo y lo que falta de él), en palabras, en la pantalla de rodaje. No nombra ninguna app de entrenamiento: la app solo publica y no sabe quién se ha conectado. Complementa a `ConnectionBar`, que lleva solo los dispositivos a los que se conecta la app.

**Recibe:** el estado del rodillo publicado (`ok` | `wait` | `off`), el texto y, con el rodillo conectado, si falta el mando o el pulso configurados.

**Textos**
- `ok`: «Conectado». Con «Wifi + Bluetooth», si hay un Square configurado y la app de entrenamiento no ha saludado al mando, «Conectado · sin mando» (con el mando dentro del mismo dispositivo no se muestra); si hay una banda configurada y no se ha suscrito al pulso, «Conectado · sin pulso»; si faltan los dos, «Conectado · sin mando ni pulso». Lo no configurado no se menciona.
- `wait`: «Esperando conexión…». Es el estado de siempre al rodar sin la app de entrenamiento abierta, así que va en `ink-muted`.
- `off`: «No visible en la red», «Sin wifi», «Sin Bluetooth», «No disponible» o «Sin rodillo» (no hay rodillo configurado y no se publica nada). Nunca solo por color.
- No dice por dónde va («por wifi»): eso está en Conexión. El transporte solo se nombra cuando falta.

**Forma**
- Un `sq-conn` dentro de `a.sq-pbadge`: punto verde (`ok`), aro que gira (`wait`) o aro vacío con borde `danger` (`off`), como `ConnectionChip`. El estado se lee por la forma y por la palabra.
- Una línea de 32 px, **a la izquierda de la barra de conexiones** (`.sq-ride__statusrow` en vertical; en horizontal, centrado en `.sq-ride__top--status`), con el modo y la transmisión en la fila de debajo en vertical (`.sq-ride__moderow`). Con margen vertical negativo, su zona táctil llega a `touch-min` sin alargar la línea. El texto más largo, «Conectado · sin mando ni pulso», cabe a 360 dp.
- Descripción accesible: `aria-label="Conexión: rodillo, conectado; mando, esperando"`.

**Uso**
- Es un enlace: abre Conexión, donde está el estado de cada servicio y cómo emparejarlo.
- Solo en Rodaje. Fuera de Rodaje, Conexión muestra cada servicio con `ServiceRow`.
- No cambia el alto del área de datos fija: la marcha cede lo que haga falta.

**No**
- No mostrar el modelo de resistencia ni nada de la simulación.
- No mostrar un icono de la plataforma ni del mando en `ConnectionBar`.
- No nombrar Zwift ni otra app de entrenamiento.
