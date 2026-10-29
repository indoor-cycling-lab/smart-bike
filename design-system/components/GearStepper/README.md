# GearStepper

Fila de la tabla de marchas (Ajustes → Rodillo → Tabla de marchas): una marcha con su ajuste de pendiente y un [Stepper](../Stepper/README.md) de 0,1 %.

**Recibe:** nombre de la marcha (`36×17`, o el número con la lineal), relación, ajuste en %, y si es la marcha puesta.

**Uso**
- El nombre va en cifra tabular, como en Rodaje; la relación, en la ayuda.
- La marcha puesta se marca con una barra a la izquierda **y** con la palabra «● Puesta» en su ayuda: nada se transmite solo con color. La lista se abre desplazada a ella.
- Botones − y + de `touch-min` (48 dp); en los límites (−20,0 % y +20,0 %) se deshabilita el que los superaría.
- Signo explícito en el valor (`+3,2`, `−1,5`), decimales con coma.

**No**
- No mostrar aquí el modelo ni la pendiente de Zwift: la tabla es solo el ajuste de cada marcha.
