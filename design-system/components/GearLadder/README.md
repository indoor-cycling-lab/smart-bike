# GearLadder

Barra de segmentos, uno por marcha, que muestra de un vistazo dónde estás en el rango. Solo para la transmisión lineal; con una transmisión física se usa `CassetteView`.

**Recibe:** número de marchas, marcha actual y, opcional, marcha de referencia.

**Uso**
- Debajo o al lado de `GearDisplay`, de ancho completo en la zona principal. Altura `ladder-h`.
- Marchas ya superadas en `line-strong`, la actual en `gear-fill` a altura completa y las restantes en `surface-2`.
- La marcha de referencia lleva una marca inferior en `ink`.
- La marcha ligera a la izquierda y la dura a la derecha, siempre.

**No**
- No hacerla interactiva en la pantalla de rodaje: las marchas se cambian con el Square.
