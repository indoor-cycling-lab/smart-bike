# Design

## Context

`SquareKey` recibe un texto y lo pinta: en la fuente `numeric` si es un símbolo o una letra, o con la fuente normal si es un nombre. `ButtonMappingRow` y `CombinacionSquare` pasan listas de texto, y Diagnóstico pasa el nombre corto de cada botón (`nombreCorto`, «△», «A», «Palanca D1»…). En la maqueta, `sq-key` hace lo mismo con el texto del `span`. Los iconos de `design-system/icons.svg` ya se convierten en `drawable/ic_*.xml` con `generar.py`, y `IconosSincronizadosTest` comprueba que coinciden. Ver proposal.md para el problema.

## Goals / Non-Goals

**Goals:**
- Los ocho símbolos con el mismo tamaño óptico, el mismo grosor y bien centrados, en la maqueta y en la app, sin depender de la fuente del sistema.
- No cambiar la API de las teclas en la app: quien llama sigue pasando el texto.

**Non-Goals:**
- Reemplazar los glifos dentro de frases.
- Cambiar los nombres de los botones en `strings.xml`.

## Decisions

1. **Iconos de `icons.svg`, uno por símbolo.** Nombres `i-tecla_triangulo`, `i-tecla_circulo`, `i-tecla_cuadrado`, `i-tecla_equis`, `i-tecla_arriba`, `i-tecla_abajo`, `i-tecla_izquierda` e `i-tecla_derecha`. Cada uno es un solo trazado relleno de grosor fijo, como `i-manillar` e `i-botones`. Alternativa: los iconos de Material (flechas, `change_history`, `circle`, `crop_square`, `close`). Se descarta porque su grosor y su proporción no coinciden entre sí ni con los del proyecto, y el problema es precisamente la falta de consistencia. Alternativa 2: elegir una fuente que traiga todos los glifos. Se descarta porque añade una fuente solo para ocho símbolos y no corrige la línea base entre flechas y formas.
2. **Formas de `i-botones`.** Los cuatro símbolos de las teclas salen de los de `i-botones` (mismo trazo y mismas proporciones), recortados y recentrados cada uno en su caja. Así el Square se reconoce en Ajustes y en las teclas. Las flechas siguen el mismo trazo.
3. **Tamaño óptico, no geométrico.** Cada símbolo se escala para que lea igual de grande: el círculo y el triángulo, algo mayores que el cuadrado, y la cruz, algo menor que el lado. La referencia es la cruz actual (✕), que es el símbolo que hoy ya se ve bien. El tamaño dentro de la tecla de 36 dp es de 20 dp y, en la captura de 48 dp, de 28 dp.
4. **Texto accesible.** La tecla con icono lleva la descripción con el nombre del símbolo («triángulo», «círculo», «cuadrado», «equis», «flecha arriba», «flecha abajo», «flecha a la izquierda», «flecha a la derecha»), con textos propios en `strings.xml`, para no depender de cómo lee TalkBack un glifo. El resto de los textos de la app no cambian.
5. **En la app, el icono se elige por el texto de la tecla.** `SquareKey` mantiene su firma y, si el texto es uno de los ocho glifos, dibuja el icono correspondiente. Así `ButtonMappingRow`, `CombinacionSquare` y Diagnóstico no cambian de llamada. Alternativa: pasar el `SquareButton` en lugar del texto. Se descarta por ahora: obliga a cambiar todos los llamadores y los tests, sin un beneficio visible.
6. **Color.** El icono usa `currentColor`, así que sigue la tecla: `ink` en reposo y `on-gear-fill` pulsada. El contraste de las teclas pulsadas ya está comprobado en `contraste.py` y no cambia.

## Risks / Trade-offs

- [El dibujo no se parece al glifo al que el usuario está acostumbrado] → la validación de la maqueta decide formas y tamaños antes de tocar la app; las formas parten de `i-botones`.
- [Una frase con un glifo de texto desentona con las teclas dibujadas] → se anota; es el alcance excluido.
- [`IconosSincronizadosTest` falla si los `ic_*.xml` no se regeneran] → la tarea de la app regenera con `generar.py` antes de ejecutar los tests.
- [Cambia el aspecto de las capturas de la web] → se regeneran con `capturas.py`.

## Open Questions

Ninguna abierta. El nombre accesible de ✕ es «equis» (decidido por el usuario, 28-10-2026).
