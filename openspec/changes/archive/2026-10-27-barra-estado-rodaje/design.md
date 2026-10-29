## Context

Así está hoy:

- **Rodaje sin barras del sistema.** `RodajeRuta` oculta `systemBars()` con `BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE` y mantiene la pantalla encendida (`ride-screen`, «Rodaje como pantalla inicial»). Eso no cambia: el change devuelve tres datos sin devolver la barra.
- **Vertical.** `FranjaSuperior` (`Rodaje.kt`) es una `Box` de al menos `touch-min` que lleva la `ConnectionBar` a la derecha, centrada. En la maqueta, `sq-ride__strip::before` dibuja un punto fijo donde iría la cámara.
- **Horizontal.** `BarraSuperior` pone el modo con la transmisión a la izquierda y, a la derecha, la barra de conexiones y Ajustes, en una línea con `space-between`.
- **Wifi.** `AdaptadorWifi` implementa `EstadoWifi.direccion: StateFlow<String?>`, con la IPv4 de la red wifi (también sin internet) o `null`. Es la misma fuente con la que el puente decide «Sin wifi» y tacha la Z de Zwift.
- **Iconos.** `scripts/design-system/generar.py` convierte cada `<symbol>` de `icons.svg` en `ic_<nombre>.xml`, y exige `viewBox="0 -960 960 960"` y un solo `<path>` con relleno. `IconosSincronizadosTest` falla si no coinciden.
- **Estados con la forma del icono.** `ConnectionBar` ya fija el patrón: normal, atenuado con marca girando y tachado en `danger` (`is-off`), con un recorte diagonal y una línea.
- **Texto.** Los estilos de texto existentes incluyen `body` (16 sp) y `heading` (18 sp), en la fuente numérica con cifras tabulares (`sq-num`).

Ver `proposal.md` para el motivo y el alcance.

## Goals / Non-Goals

**Goals:**
- Un componente `DeviceStatus` independiente de `ConnectionBar`, con ancho fijo, que Rodaje coloca según la orientación y la cámara.
- Hora, wifi y batería leídos del sistema solo mientras Rodaje se ve, sin servicios ni permisos nuevos.
- Una regla de colocación que se pueda probar sin móvil (un test unitario), porque la cámara centrada no se puede probar con el OnePlus 8.

**Non-Goals:**
- Devolver la barra de estado del sistema ni sus notificaciones.
- Un ajuste de usuario para los indicadores.
- Cambiar `Sesion`, `RodajeViewModel` más allá de exponer los tres datos, o los estados de conexión.

## Decisions

### 1. Componente nuevo `DeviceStatus` (`sq-devstatus`), aparte de `ConnectionBar`

`ConnectionBar` es un único `clickable` que abre Dispositivos, con un icono por enlace. Los indicadores del móvil son otra cosa: solo informan, no se tocan y no son enlaces. Meterlos en la barra los dejaría dentro del objetivo táctil, y tocar la hora abriría Dispositivos. Se crea un componente aparte y Rodaje lo coloca al lado.

- **Estructura HTML:**
  ```html
  <div class="sq-devstatus" role="group" aria-label="14:32, wifi conectado, batería 82 %, cargando">
    <span class="sq-devstatus__time sq-num" aria-hidden="true">14:32</span>
    <span class="sq-devstatus__i" aria-hidden="true">…wifi…</span>
    <span class="sq-devstatus__bat" aria-hidden="true">…icono… 82 %</span>
  </div>
  ```
- **Composable:** `DeviceStatus(hora, wifi, bateria, modifier)`, con una sola descripción accesible (`clearAndSetSemantics`) y sin `clickable`.
- **El grupo mide entero**, de modo que quien lo coloca lo trata como un bloque: lo pega a un lado en vertical y lo centra en horizontal.
- **Alternativa descartada:** ampliar `ConnectionBar` con los tres indicadores. Mezcla lo informativo con lo accionable, y la descripción accesible de las conexiones pasaría a hablar de la hora.

### 2. Texto e iconos, todos en `ink`, sin tokens nuevos

- La hora y el porcentaje usan `body` (16 sp) en la fuente numérica con cifras tabulares, en `ink`. Con `heading` (18 sp) el grupo sumaría unos 14 dp más y no cabría con la cámara centrada a 360 dp (decisión 6).
- Todos los iconos de estado de Rodaje, los de la barra de conexiones y los de wifi y batería, miden 14 dp (bajados de 18 dp por petición del usuario al verlos en el móvil), y van en `ink`. El único color de estado es `danger`, para el wifi tachado y la batería baja sin cargar, y siempre acompañado de la forma.
- No hace falta ningún token ni par de contraste nuevo: `ink` y `danger` ya están sobre `bg` y `surface` en `contraste.py`. Se comprueba igualmente ejecutando el generador.
- **Alternativa descartada:** `ink-muted` para distinguir lo del móvil de lo de la app. La posición ya los separa, y el texto atenuado se lee peor a 1–2 m.

### 3. Wifi: un icono con la misma marca de «sin conexión» que `ConnectionBar`

El sprite gana `wifi` (Material Symbols, un solo `<path>`). «Sin wifi» no usa `wifi_off`: aplica al mismo icono el tachado de `is-off` (recorte diagonal y línea de 2 px en `danger`), que ya existe en el CSS de la barra y en la app. Así el tachado significa lo mismo en toda la pantalla y el sprite no crece.

La fuente es `EstadoWifi.direccion`: hay wifi si no es `null`. Es la misma con la que se tacha la Z de Zwift, así que los dos iconos nunca se contradicen.

### 4. Batería: iconos horizontales de Material por pasos

El nivel se muestra con los iconos horizontales `battery_android_1` a `battery_android_6` y `battery_android_full` de Material Symbols (como el icono de batería del móvil), y con el cargador, `battery_android_bolt`: el contorno con un rayo. Cada uno es un solo `<path>`, así que pasan por el generador y por `IconosSincronizadosTest` sin tocar nada. El dibujo ocupa el 83 % del ancho de su caja de 24, así que se recorta el resto con márgenes laterales negativos (un 7 % a cada lado), en CSS y en Compose.

- **Elección del icono:** `barras = clamp(round(nivel × 7 / 100), 1, 7)`, donde 7 es `battery_android_full`. Con menos del 15 % y sin cargar se usa el de una barra (la «franja mínima» de la spec) en `danger`.
- **Carga:** `EXTRA_PLUGGED` distinto de cero, no `STATUS_CHARGING`, para que el rayo siga visible al 100 % mientras el móvil está conectado (spec, «Móvil cargando»). Con el cargador, el icono no muestra el nivel: lo da el porcentaje.
- **Alternativa descartada (probada):** los verticales `battery_N_bar` y `battery_charging_*`, que conservan el nivel al cargar y ocupan 6 dp de ancho. En el móvil apenas ahorraban sitio (lo que ocupaba eran los huecos entre elementos, que se redujeron) y quedaban muy finos.
- **Alternativa descartada:** dibujar el contorno y el relleno proporcional (un `Canvas` en Compose y un `div` en HTML). Da el nivel exacto y ahorra iconos, pero duplica el dibujo en dos sitios, se sale de la regla de que los iconos salen del sprite y no pasa por `IconosSincronizadosTest`. Con el porcentaje al lado, un paso de unos 14 puntos basta para leer el nivel de un vistazo.
- **Coste:** ocho símbolos nuevos en el sprite, de la misma licencia que el resto.

### 5. Colocación: una regla pura y dos disposiciones

**Vertical.** La regla vive en `:nucleo` como una función pura que recibe el ancho de la franja, los tramos horizontales de los recortes que tocan la franja, el ancho del grupo y el de los enlaces, y devuelve el inicio de cada uno:

1. Sin recorte en la franja: el grupo empieza en el margen (`space4`) y los enlaces acaban en el margen derecho.
2. Con recorte: si a su izquierda cabe el grupo entero más `space2` de aire, el grupo va allí, desde el margen. Si no, empieza a la derecha del recorte más `space4` de aire.
3. Los enlaces van al final de la franja; si un recorte los toca (cámara a la derecha), acaban antes de él con `space2` de aire.

En Compose, el recorte sale de `WindowInsetsCompat.displayCutout.boundingRects` (los de `top == 0`) de la vista raíz. La franja pasa los tramos a la función y coloca los dos grupos con `Modifier.offset`. En la maqueta, `sq-ride__strip` lo hace con una variable CSS por estado (`--cutout-start`, `--cutout-end`), en lugar del punto fijo `::before`.

**Horizontal.** El conjunto de la hora, el wifi y la batería va centrado en la línea. En HTML, una rejilla `1fr auto 1fr` con el conjunto en la celda central. En Compose, un `Layout` propio que centra el conjunto y limita el ancho del modo (a la izquierda) y de las conexiones con Ajustes (a la derecha) a lo que queda hasta él. Si el modo con la transmisión no cabe, pasa a dos líneas, como en vertical.

- **Alternativa descartada:** centrar solo la hora, con el wifi y la batería a su derecha. Quedaba descentrado el conjunto, y el usuario pidió centrar el conjunto.
- **Alternativa descartada:** el conjunto en la fila del modo con `space-between`. Ya no estaría centrado y se movería según el ancho de los lados.
- **Alternativa descartada:** un ajuste «Cámara» en Ajustes. El recorte lo da el sistema; preguntarlo al usuario sería pedirle un dato que la app ya tiene.

### 6. Ancho estable

El ancho del grupo no depende de los valores: las cifras de la hora y del porcentaje son tabulares, y el porcentaje reserva el ancho de «100 %». El wifi tachado y el rayo ocupan el mismo hueco que sus iconos normales. Con la batería no disponible, el hueco de la batería se quita y el grupo es más corto: es un caso raro y no ocurre rodando.

### 7. Lectura de la hora y de la batería

Dos `Flow` en `:app`, en un adaptador nuevo `AdaptadorEstadoMovil` que `AppGraph` crea una vez:

- **Hora:** un `callbackFlow` con un `BroadcastReceiver` para `ACTION_TIME_TICK`, `ACTION_TIME_CHANGED` y `ACTION_TIMEZONE_CHANGED`, que emite la hora local formateada `HH:mm`. `TIME_TICK` solo se puede registrar en código, no en el manifiesto, lo que encaja: solo se registra mientras Rodaje se ve. Emite el valor actual al empezar. El formato siempre es de 24 horas, sin mirar el ajuste del sistema.
- **Batería:** un `callbackFlow` con `ACTION_BATTERY_CHANGED`, un broadcast pegajoso que no pide permisos. Da `nivel = EXTRA_LEVEL × 100 / EXTRA_SCALE` y el cargador por `EXTRA_PLUGGED`. Si `EXTRA_LEVEL` o `EXTRA_SCALE` son −1, el nivel es `null` y no se pinta la batería.
- **Wifi:** `EstadoWifi.direccion` mapeado a booleano.
- **Dónde se consumen.** `RodajeViewModel` los combina en `RodajeUiState.movil` con `stateIn(WhileSubscribed)`. Sin colectores (Rodaje no se ve), los receptores se dan de baja. No añade trabajo en segundo plano ni al servicio en primer plano.
- **Alternativa descartada:** un `delay` hasta el siguiente minuto. Se desfasa si el usuario cambia la hora o la zona horaria, y obligaría a escuchar esos avisos igualmente.

## Risks / Trade-offs

- [Con la cámara en el centro y un móvil de 360 dp, el grupo apenas cabe a su izquierda (unos 136 dp de grupo frente a unos 150 dp libres)] → La maqueta lo comprueba en un estado propio a 360 dp. Si no cabe, se baja el aire a `space1`, y como último recurso el porcentaje pasa a `label` (13 sp) antes de quitar nada.
- [La cámara centrada no se puede probar con el OnePlus 8] → Se cubre con tests de la regla de colocación (cámara a la izquierda, al centro, a la derecha, sin recorte, y un grupo que no cabe a la izquierda) y queda anotada como «a validar» en `docs/`.
- [La función de colocación usa los rectángulos del recorte, que en algunos móviles incluyen una muesca ancha o varios recortes] → La regla trabaja con los tramos que tocan la franja y se queda con el más cercano al grupo; con dos recortes, usa el más a la izquierda. Se prueba con un caso de dos recortes.
- [El paso de 1/7 de la batería es más grueso que el del sistema] → El porcentaje va al lado, y el nivel se lee por el número.
- [Los iconos de Material cambian de forma entre versiones del set] → El sprite guarda una copia fija, como el resto de iconos, con su licencia.
- [Un wifi conectado sin IPv4 se vería como «sin wifi»] → Es lo mismo que decide el puente con Zwift: el puente necesita esa IPv4, así que coincide con lo que importa.
- [Un móvil con la cámara a la derecha empuja los enlaces hacia la izquierda] → Hay una regla para ello y un test, pero no se prueba con equipo real.

## Migration Plan

No hay datos que migrar ni ajustes nuevos. El cambio es visual y se revierte volviendo atrás el commit. La pauta de iconografía de Rodaje pasa de dos a tres excepciones.

## Open Questions

- Si, con la cámara en el centro y 360 dp, hace falta el último recurso (el porcentaje en 13 sp), se decide al ver la maqueta; no cambia las specs ni las tareas.
