# SquareKey

Representa un botón físico del Elite Square como una tecla. Es el vocabulario visual de toda la configuración de botones.

**Recibe:** botón (uno de los 20) y si está pulsado.

**Uso**
- Botones de símbolo (△ ○ □ ✕) y cruceta (↑ ↓ ← →): tecla cuadrada con el icono dibujado (`i-tecla_triangulo`, `i-tecla_circulo`, `i-tecla_cuadrado`, `i-tecla_equis`, `i-tecla_arriba`, `i-tecla_abajo`, `i-tecla_izquierda`, `i-tecla_derecha`), de 20 px en la tecla de 36 y de 28 px en la de 48. Toma el color de la tecla (`ink` en reposo, `on-gear-fill` pulsada). La tecla lleva `role="img"` y `aria-label` con el nombre del símbolo («triángulo», «flecha arriba»).
- Letras (A B Y Z): tecla cuadrada con la letra en la fuente `numeric`.
- Palancas, frenos y Campagnolo: tecla ancha con nombre corto. Lado con I/D y número de palanca: «Palanca D1», «Freno I», «Campag. D».
- Pulsado (en vivo, en la pantalla de captura o en diagnóstico): `gear-fill` con `on-gear-fill`.
- Combinación: teclas separadas por «+» en `ink-muted`, el modificador primero.

**No**
- No usar emojis ni glifos de texto para los símbolos y la cruceta: ninguna fuente los trae todos con el mismo tamaño y la misma línea base.
- No usar iconos distintos de los `i-tecla_*` (ni los de Material) para esos botones.
