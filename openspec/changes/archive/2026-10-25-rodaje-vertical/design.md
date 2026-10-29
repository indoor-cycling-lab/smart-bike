## Context

Tras el change `marchas-sin-zwift`, `RodajeRuta` fija la `Activity` en `sensorLandscape` mientras se ve Rodaje y restaura la orientación al salir. `Rodaje` compone la disposición horizontal de `RideScreen`: barra superior en `FlowRow`, marcha y escalera a la izquierda (1,3 partes) y `MetricGrid` a la derecha (1 parte), y pie. `MetricGrid` solo implementa la distribución horizontal del README. La `Sesion` vive en `AppGraph`, así que recrear la `Activity` al girar no la toca. El OnePlus 8 mide 360 × ~800 dp en vertical. Motivo y alcance: `proposal.md`. Comportamiento: las specs de este change.

## Goals / Non-Goals

**Goals:**

- Que Rodaje se lea en vertical con lo que ya muestra, sin tocar la `Sesion` ni el dominio.
- Que la elección entre las dos disposiciones dependa del espacio disponible, no de un ajuste.

**Non-Goals:**

- La disposición vertical con plato y cassette (change 8) y con 3–6 datos o botones − y + (change 10).

## Decisions

### 1. Orientación: la del sistema, sin forzarla

`RodajeRuta` deja de cambiar `requestedOrientation`. La `Activity` se queda con la orientación por defecto (`unspecified`), que gira con el móvil y respeta el bloqueo de rotación de Android (spec `ride-screen`, «Bloqueo de rotación»). El resto del `DisposableEffect` se queda: barras del sistema ocultas y pantalla encendida mientras se ve Rodaje.

- **Alternativa: `fullUser` o `sensor`.** `sensor` ignoraría el bloqueo de rotación, que es justo lo que permite dejar el móvil fijo en el soporte. `fullUser` añade la orientación invertida, que en este uso no aporta nada.
- **Alternativa: un ajuste horizontal o vertical en la app.** Sería otro ajuste que mantener, cuando el bloqueo del sistema ya lo resuelve. Queda fuera de alcance.
- Girar recrea la `Activity` (no se declara `configChanges`). La `Sesion` y los `ViewModel` sobreviven. Se pierde a lo sumo el aviso de acción que estuviera visible: el `ViewModel` lo conserva y vuelve a pintarse.

### 2. Disposición según el espacio, con `BoxWithConstraints`

`Rodaje` elige la disposición vertical cuando el alto disponible es mayor que el ancho, y la horizontal en otro caso. No lee la orientación de `LocalConfiguration`: con el espacio real funciona igual en pantalla dividida y en los tests de Compose, que corren en vertical.

- En vertical, en una `Column` con el relleno de `sq-ride--v` (`space-6` y `space-6` entre zonas):
  1. Conexiones (`FlowRow`, como en la maqueta).
  2. Modo y transmisión.
  3. `GearDisplay` centrado, con `GearLadder` a todo el ancho debajo.
  4. `MetricGrid` en vertical con `weight(1f)`.
  5. Pie.
- La horizontal no cambia.

### 3. `MetricGrid` en vertical

`MetricGrid` recibe un parámetro `vertical`. Si está activado, usa siempre 2 columnas y las filas de su README (1–2 datos en una fila, 3–4 en dos, 5–6 en tres). La cifra mide 64 sp con 1–2 datos (`.sq-ride--v .sq-mgrid[data-n="1"|"2"]` de `components.css`). Para 3–6 datos se deja el tamaño de la tabla horizontal, porque la app aún no los usa; F11.3 (change 10) lo afinará con la maqueta.

### 4. Maqueta vertical: estados en `section` propias

`maquetas/rodaje-vertical.html` tiene ahora un solo escenario con elementos que se muestran según el estado. Los cuatro estados lineales nuevos van en sus propias `section data-mock-state`, como en `rodaje.html`, porque cambian la zona de la marcha entera (número y escalera en lugar de plato y cassette). `revision.js` ya admite las dos formas en la misma página.

### 5. Hueco del tope siempre reservado

Al aparecer, «Tope · marcha más dura» bajaba la escalera unos 30 dp, en contra de la spec («sin que se mueva el resto de la pantalla»); en horizontal ya pasaba un poco. El hueco de la etiqueta queda reservado siempre: en el sistema de diseño, `sq-limit is-reserva` (invisible y con `aria-hidden`) en `components.css` y en el README de `GearDisplay`, y en las maquetas lineales de las dos orientaciones; en la app, `GearDisplay` pinta la etiqueta con opacidad 0 y sin semántica cuando no hay tope, con el texto de [reservaTope]. Se descubrió al revisar la app con el usuario después de validar la maqueta, y el usuario pidió arreglarlo.

## Risks / Trade-offs

- [La marcha en `gear-hero` (168 sp) y «Marcha · de 24» no caben juntas en 360 dp con dos cifras] → «24» en Barlow Condensed ocupa unos 170 dp y el bloque de «de 24», unos 100 dp; con los 48 dp de relleno cabe justo. Se comprueba en la maqueta a 390 dp y en el OnePlus a 360 dp. Si no cabe, «Marcha · de 24» pasa debajo de la cifra en vertical, que es un cambio de maqueta que se valida en la Etapa 1.
- [Los tests de Compose existentes corren en vertical y dan por hecha la disposición horizontal] → `RodajeTest` fija el tamaño del contenedor en cada test (horizontal o vertical), para que cada uno pruebe la disposición que dice.
- [Girar el móvil a mitad de rodaje recrea la pantalla y se nota un parpadeo] → Aceptable. Con el soporte fijo y el bloqueo de rotación no debería ocurrir mientras se rueda.

## Migration Plan

No hay datos que migrar. Al instalar la versión, Rodaje empieza a seguir la orientación del móvil.

## Validación con el equipo real

2026-10-25, OnePlus 8 en el soporte en vertical, con el Neo y el Square: el usuario confirma que Rodaje se lee bien desde la bici, con cambios de marcha, tope y desconexión y reconexión desde Diagnóstico. El giro a horizontal y el bloqueo de rotación funcionan como dice la spec.
