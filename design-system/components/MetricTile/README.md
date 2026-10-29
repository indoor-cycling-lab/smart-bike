# MetricTile

Una métrica de rodaje: etiqueta en mayúsculas encima, cifra grande y unidad a la derecha.

**Recibe:** etiqueta, valor numérico, unidad, tamaño de cifra (lo decide `MetricGrid` según cuántos datos hay) y, opcional, una línea de objetivo.

**Datos disponibles:** potencia, cadencia, velocidad, tiempo, distancia, pendiente, W/kg, potencia media y potencia máxima. El usuario elige hasta 6 y su orden.

**Uso**
- Etiqueta con `label` en `ink-muted`; cifra en `numeric` 600 y `ink`, al tamaño que marque `MetricGrid`; unidad en `ink-muted`.
- Cifras tabulares siempre, para que no bailen al cambiar.
- Sin dato (sensor desconectado): mostrar «—» en `ink-muted`, sin unidad, nunca 0. En HTML, `sq-metric-val is-empty`.
- Fuera de `MetricGrid` hay tres tamaños fijos: `xl` (`metric-xl`), `lg` (`metric-lg`) y `md` (`metric-md`, para paneles de Diagnóstico). Un valor que es texto («Simple») usa `text`: familia `sans` a la altura de línea de `md`.
- Potencia suavizada a 3 s; cadencia a 1 s, igual que el filtro del modelo.
- La línea de objetivo ERG usa `erg`.
- **Pendiente:** la de la simulación (la que envía la app), con signo explícito, coma decimal y un decimal, y «%» como unidad: «+4,2 %», «−7,0 %», «0,0 %» (en Libre). `is-climb` la pinta en `climb` si es mayor que 0,5 y `is-descent` en `descent` si es menor que −0,5; entre medias, `ink`. El signo ya lo dice, así que el color no es lo único que la distingue. En ERG no aplica: «—» en `ink-muted` (`is-empty`). Nunca la pendiente base ni la efectiva (eso es de `GradeReadout`, en Diagnóstico).

**No**
- No usarlo suelto en la pantalla de rodaje: siempre dentro de `MetricGrid`.
- No poner las métricas dentro de tarjetas con borde: se separan por espacio.
