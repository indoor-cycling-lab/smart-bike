# GradeReadout

La pendiente que manda Zwift y la que realmente se aplica al Neo tras sumar la marcha. En el modelo físico, la segunda cifra se sustituye por la potencia calculada.

Solo en Diagnóstico, dentro de la sección «Simulación». La pantalla de rodaje no muestra la pendiente base ni la efectiva: el ciclista ve la marcha, no el modelo que hay detrás. La pendiente de la simulación (la que envía la app) sí puede verse en Rodaje, pero como dato opcional de `MetricGrid` («Pendiente»), no con este componente.

**Recibe:** pendiente de Zwift (%), pendiente o potencia efectiva y el modelo activo.

**Uso**
- Pendiente de Zwift en `metric-md` y `ink-muted`; efectiva en `metric-lg` coloreada: `climb` > 0,5 %, `descent` < −0,5 %, `ink-muted` entre medias.
- Signo explícito en la efectiva (`+6,2 %`). Coma decimal y un decimal.
- Sin Zwift conectado, la primera cifra es la pendiente base y la etiqueta dice «Base → efectiva».

**No**
- No ponerla en la pantalla de rodaje ni en los ajustes de marchas (la base y la efectiva; la pendiente de la simulación es el dato «Pendiente» de `MetricGrid`).
- No usar `climb`/`descent` para nada que no sea pendiente.
