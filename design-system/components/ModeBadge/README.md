# ModeBadge

Indica cómo se está controlando el Neo: sin pendiente que simular (LIBRE), simulación de pendiente de una app (SIM) o potencia fija (ERG), y quién manda en ERG.

**Recibe:** modo (`libre` | `sim` | `erg`), origen (`manual` | `zwift`) y si es tocable.

**Uso**
- Arriba a la derecha de la pantalla de rodaje, junto a los chips de conexión.
- Contorno y texto en `ink` (`libre`), `sim` o `erg`; fondo `bg`. El modo se lee por la palabra, no solo por el color.
- `LIBRE` es el modo sin app de entrenamiento enviando pendiente: el terreno es llano (0 %) y solo cambia la marcha. `SIM` es con una app que envía pendiente (una ruta de Zwift). Si la app se va, `SIM` pasa a `LIBRE` a los 30 s.
- En ERG (de Zwift o manual), las palancas ajustan ±W en vez de marchas: `ActionToast` muestra «+10 W» o «−10 W» en cada pulsación, si está activado.
- En ERG no hay una línea que explique lo que hacen las palancas: la insignia ya dice el modo y el aviso «+10 W» lo muestra al usarlas.
- El modelo físico manda ERG al Neo, pero para el usuario el modo sigue siendo LIBRE o SIM: la insignia muestra lo que el ciclista siente, no el comando interno.

**Variante tocable** (`<button class="sq-mode …">` en lugar del `<span>`)
- Solo con el ajuste «Cambiar de modo tocando» activado (independiente de los botones − y +). Alterna Libre o SIM ⇄ ERG manual con el mismo efecto que la acción «Alternar SIM ⇄ ERG» del Square. Con el ajuste desactivado, el indicador es la etiqueta de siempre (`<span>`), con o sin los botones − y + visibles.
- Se ve igual de alto (32 px) pero su área de toque llega a `touch-min` (48 px) con un `::before` invisible, como el icono de Ajustes. Esa área no puede solaparse con Ajustes ni con la barra de conexiones.
- El indicio de que se puede tocar no es el color ni un glifo: es la forma de botón, con relleno tintado con el color del modo y borde de 3 px (la etiqueta es solo contorno de 2 px sobre `bg`). Al pulsar (`:active`, `.is-pressed` en las maquetas) el relleno se intensifica; el foco visible usa el anillo `--focus`.
- También es botón en «ERG APP» (en un entrenamiento de la app no cambia el modo y el pie avisa «Entrenamiento de la app»), para que el indicador no cambie de aspecto según quién manda.
- Accesibilidad: `Role.Button` y descripción con la acción («Modo Libre, toca para pasar a ERG manual»).
