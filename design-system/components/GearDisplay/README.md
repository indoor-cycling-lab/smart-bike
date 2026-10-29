# GearDisplay

La marcha puesta, enorme. Es la pieza central de la pantalla de rodaje y se muestra como en un grupo inalámbrico real.

**Variantes**
- **Física** (Shimano, SRAM, Campagnolo o a medida): plato × piñón, `52×17`, en `gear-ratio-hero`, con el signo × más pequeño en `ink-muted`. Va con `ChainringIndicator` y `CassetteView`.
- **Lineal**: el número de marcha en `gear-hero`, con «Marcha · de N» al lado. Va con `GearLadder`.

**Recibe:** transmisión (física o lineal), plato y piñón actuales o marcha actual y total, y si está en un tope.

**Uso**
- Solo uno por pantalla, arriba a la izquierda de la zona principal y con `space-12` de aire.
- En `gear`. En un tope pasa a `ink-muted` y aparece la etiqueta de tope en `gear-soft` durante 1,5 s.
- El hueco de la etiqueta de tope está siempre reservado (`sq-limit is-reserva`, invisible y oculta al lector de pantalla), para que al aparecer no se mueva nada de la pantalla.
- Al cambiar, la cifra nueva entra desplazándose 12dp en la dirección del cambio (arriba = más dura) en 120 ms. Con movimiento reducido, sin animación. Si el aviso de acción está activado, un cambio de plato se anuncia en `ActionToast` («Plato 52»).
- Con la vibración activada, cada cambio se acompaña de una vibración corta y el tope de una doble.

**No**
- No mostrar nada del modelo de resistencia: ni el incremento de pendiente (`+3 %`), ni la pendiente de Zwift, ni lo que se envía al Neo. Eso va en Diagnóstico (`GradeReadout`).
- No mostrar la marcha lineal como fracción («14/24») en grande: el total va en la etiqueta.
- No colorear la cifra según el terreno: el color de la marcha es siempre `gear`.
