# TurnIndicator

Aviso de que el giro de Zwift está pulsado: una flecha a cada lado de la marcha, la de la izquierda mientras se mantiene el giro a la izquierda y la de la derecha mientras se mantiene el giro a la derecha.

**Recibe:** qué giros están activos (ninguno, izquierda, derecha o los dos).

**Uso**
- Envuelve la marcha (`GearDisplay`, o los platos con el piñón en las transmisiones físicas) en la zona de la marcha de Rodaje. Las dos flechas se ven en `gear`, de 56 px.
- El sitio de las dos flechas está siempre reservado (`visibility: hidden`, no `display: none`): al aparecer o desaparecer no se mueve ni cambia de tamaño nada. La marcha se reduce para dejarles sitio, igual que para el resto de su zona.
- La flecha derecha es la izquierda reflejada (`i-arrow_back` con `scaleX(-1)`).
- Se ve por la forma y la posición, nunca solo por el color.
- Solo con «Mando de Zwift» activado: indica lo que la app envía a Zwift. Un giro breve se ve al menos 0,5 s.
- Accesibilidad: región viva con «Girando a la izquierda» o «Girando a la derecha»; las flechas van con `aria-hidden`.

**No**
- No es táctil ni tiene objetivo táctil.
- No indica el freno de Zwift. Con el freno de la misma palanca pulsado, no se muestra su giro (Zwift tampoco gira).
- No sustituye al aviso de acción del pie: pueden verse a la vez.
