# MetricGrid

La zona de datos de la pantalla de rodaje: coloca de 1 a 6 `MetricTile` elegidos por el usuario en un área fija y les da la cifra más grande que cabe, sin desplazamiento ni recortes.

**Recibe:** la lista ordenada de datos elegidos entre los nueve posibles (Ajustes > Al rodar > Datos en pantalla) y el área que le da `RideScreen`.

**Área:** fija, el 40 % del ancho útil en horizontal y del alto útil en vertical (`RideScreen`). No cambia con la transmisión, el modo, las conexiones ni el aviso del pie. Solo los botones − y + la reducen, y sin bajar la cifra.

**Columnas × filas**

| Datos | Horizontal | Vertical |
|---|---|---|
| 1 | 1 × 1 | 1 × 1 |
| 2 | 1 × 2 | 2 × 1 |
| 3 | 1 × 3 | 2 × 2, el tercero centrado |
| 4 | 2 × 2 | 2 × 2 |
| 5 | 2 × 3, el quinto centrado | 2 × 3, el quinto centrado |
| 6 | 2 × 3 | 2 × 3 |

**Tamaño de la cifra**
- Es la mayor, hasta 144 px (en pasos de 2, desde 16), con la que la etiqueta y el valor más largo de cada dato caben en su celda. Se mide con la plantilla del dato y con su valor actual, y se queda el menor de los dos tamaños.

  | Dato | Plantilla |
  |---|---|
  | Potencia, potencia media y potencia máxima | «8888» W |
  | Cadencia | «188» rpm |
  | Tiempo | «8:88:88» |
  | W/kg | «88,8» |
  | Pendiente | «−88,8» % |

- Con números tabulares, el 8 mide lo que cualquier cifra. Así el tamaño no depende de lo que marque el dato y no cambia mientras se rueda (de «999» a «1000» W, de «59:59» a «1:00:00»). Solo cambia al girar el móvil, al cambiar los datos o la escala de letra del sistema.
- No hay tabla de tamaños por número de datos: la cifra sale del área. En CSS, quien coloca la rejilla fija `--mg-size` (en las maquetas, `maquetas/rodaje.js`); en Compose, `MetricGrid` la calcula con la misma regla.

**Uso**
- Todas las cifras de la rejilla con el mismo tamaño: la jerarquía la da el orden (el primero arriba a la izquierda), no el tamaño.
- Celdas con `minmax(0, 1fr)` en filas y columnas, y cada dato (etiqueta y cifra) centrado en horizontal y en vertical dentro de su celda, en las dos orientaciones. Separación `space-2` entre filas y `space-6` entre columnas.
- Con 2 columnas y un número impar de datos, el último ocupa la fila entera, centrado, sin hueco a su derecha.
- La unidad («W», «rpm») va alineada por la línea base con la cifra y con su propio interlineado: no alarga el valor, así que la etiqueta queda a la misma distancia del borde en las filas con unidad y sin ella.
- En ERG, la potencia objetivo ocupa la zona de la marcha y no cuenta para el límite de 6.
- Con botones − y +, la rejilla cede primero lo que le sobra con su cifra: el alto que no usan sus filas en vertical, o el ancho que no usan sus columnas en horizontal.
- En Compose: `medirRejilla` calcula la cifra y el mínimo que necesita la rejilla con esa cifra a partir del área, y `MetricGrid` coloca los datos.

**Miniatura (vista previa de «Datos en pantalla»)**
- `sq-ride-mini` reproduce la pantalla de rodaje a escala dentro de una fila de 132dp: marcha (`sq-ride-mini__gear`, con la cifra a 44sp y el cassette a 16dp) a la izquierda y `sq-mgrid sq-mgrid--mini` a la derecha.
- En miniatura cada dato va sobre `surface-2` con `radius-sm`, la etiqueta baja a 9sp y la cifra a 28, 22, 16 o 13sp según `data-n` (1, 2, 3–4 o 5–6 datos). Solo se usa como vista previa: no es legible a 2 m.

**No**
- No permitir desplazamiento en la pantalla de rodaje: más de 6 datos no se pueden elegir.
- No meter las celdas en tarjetas con borde.
- No medir la cifra solo con el valor actual: cambiaría de tamaño en plena salida.
