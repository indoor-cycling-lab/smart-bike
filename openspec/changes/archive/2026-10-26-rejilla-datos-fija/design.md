## Context

Hoy, en `Rodaje.kt`, la rejilla (`MetricGrid`) va con `Modifier.weight(1f)`: ocupa lo que dejan la marcha, el pie y los botones. En vertical, con los botones, la pantalla se compacta con valores fijos (`ESCALA_COMPACTA` = 104/136, cassette de 40 dp, separación `space4`). En horizontal, la fila reparte 1,3 : 1 entre la marcha y los datos, y los botones se añaden como tercera columna. `MetricGrid` toma un tamaño máximo de una tabla por número de datos y orientación y lo baja (`tamanoQueCabe`) hasta que caben los valores *actuales*. La maqueta hace lo mismo con `--mg-size` por `data-n`, pero con otros valores en vertical (60/44 px frente a 52/40 sp). Ver proposal.md, «Why».

Las opciones de área se compararon en un boceto desechable a 390 × 844 y 844 × 390: lo que sobra (hoy), A (medida con botones y Shimano), B (40 %) y C (medida sin botones). El usuario eligió B, con el espacio de los botones tomado primero de la holgura de la rejilla y el último impar centrado.

## Goals / Non-Goals

**Goals:**
- Que el área de datos y su cifra dependan solo del tamaño de la pantalla, de la orientación y del número de datos.
- Que la maqueta y la app calculen la cifra con la misma regla, sin tablas que puedan divergir.
- Que nada de la rejilla se mueva mientras se rueda.

**Non-Goals:**
- Cambiar `GearDisplay`, `CassetteView` o `ErgTarget` más allá de poder reducirse para caber.
- Cambiar la miniatura de «Datos en pantalla» (`sq-mgrid--mini`).

## Decisions

**1. Área: 40 % del espacio útil.** El espacio útil es el de la pantalla de rodaje tras `safeDrawingPadding` y su relleno. En vertical, la rejilla mide el 40 % de ese alto; en horizontal, el 40 % de ese ancho, y todo el alto de la fila principal. Es la opción B del boceto: 324 × 340 px en vertical y 318 × 254 px en horizontal a 390 × 844. Alternativas descartadas:
- A (el alto que queda con botones y la Shimano): ata el área a la transmisión más alta.
- C (el que queda sin botones): obliga a encoger mucho la marcha (al 61 %) con botones.

En CSS, el 40 % es el valor por defecto de la rejilla (`flex: none; height: 40%` en vertical, `width: 40%` en horizontal), y el script de la maqueta lo ajusta con los botones (decisión 5).

**2. La marcha encaja en lo que queda.** La zona de la marcha ocupa el resto (`weight(1f)`). Su bloque principal es la cifra con su tope, plato × piñón con los platos, o la potencia objetivo en ERG. Se mide a su tamaño natural y, si no cabe, se reduce con un factor `k = min(1, ancho/ancho natural, alto/alto natural)`. Nunca crece por encima de su tamaño actual. La escalera y el cassette no se escalan: van a todo el ancho con su alto fijo (56 dp en vertical y 48 dp en horizontal, ya sin el de 40 dp con botones).

En Compose, un `Layout` privado de Rodaje (`Encajar`) mide al hijo sin límites, calcula `k` y lo coloca con `graphicsLayer { scaleX = k; scaleY = k }`, con el tamaño ya escalado. Alternativa descartada: pasar una escala a `GearDisplay`, `ChainringIndicator` y `ErgTarget` con `SubcomposeLayout` en dos pasadas. Es más código y obliga a que cada componente sepa escalarse. Con la capa, el texto se sigue dibujando como vector (sin capa fuera de pantalla). Desaparecen `ESCALA_COMPACTA` y el parámetro `escala` de `GearDisplay`, que solo usaba la compactación.

**3. Separaciones constantes.** En vertical, el relleno y la separación entre zonas pasan a ser `space4` siempre (hoy `space6` sin botones y `space4` con ellos), para que activar los botones no desplace nada más que lo necesario. En horizontal no cambian.

**4. Cifra de los datos: la mayor que cabe en la celda, hasta 144, medida con la plantilla de cada dato.** Cada dato tiene una plantilla con su valor más largo:

| Dato | Plantilla |
|---|---|
| Potencia, potencia media y máxima | «8888» W |
| Cadencia | «188» rpm |
| Tiempo | «8:88:88» |
| W/kg | «88,8» |

Con números tabulares, el 8 mide lo mismo que cualquier cifra. El tamaño se busca, en pasos de 2 sp entre 16 y 144, como hoy `tamanoQueCabe`. Para cada dato se mide `max(plantilla, valor actual)`, de modo que solo un valor fuera de la plantilla (una sesión de más de 9:59:59) podría bajar la cifra. Las celdas salen del área (decisión 1), de las columnas y filas de siempre (vertical: 1 columna con 1 dato y 2 con 2 a 6; horizontal: 1 columna con 1 a 3 y 2 con 4 a 6) y de las separaciones `space2` y `space6`. La tabla de tamaños por número de datos desaparece, en la maqueta y en la app. `MetricGrid` expone una función de medida (`medirRejilla`) que devuelve la cifra y el mínimo que necesita la rejilla con esa cifra (decisión 5). Rodaje la usa antes de componer.

Alternativa descartada: medir el valor actual, como hoy. Con la decisión 5, la holgura dependería de lo que marca cada dato y la rejilla y la marcha se moverían en plena salida.

**5. El hueco de los botones sale primero de la holgura de la rejilla.** El hueco es `touch-ride` más la separación: 72 + 16 dp en vertical (fila al pie) y 72 + 32 dp en horizontal (columna a la derecha). Con la cifra de la decisión 4, el mínimo de la rejilla es:
- En vertical, `filas × (etiqueta + space1 + cifra) + (filas − 1) × space2`.
- En horizontal, `columnas × (celda más ancha) + (columnas − 1) × space6`. Una celda que ocupa las dos columnas solo necesita caber en el total.

Con eso, `holgura = área − mínimo` y la rejilla cede `min(hueco, holgura)`. Lo que falta lo pierde la zona de la marcha, que se reduce por la decisión 2. Como el mínimo sale de las plantillas, la cesión es estable durante la salida.

En la maqueta, el mismo cálculo lo hace un script compartido, `maquetas/rodaje.js`, que mide en el DOM. Además fija `--mg-size` en cada rejilla y escala la marcha, igual que el boceto. Las vistas previas del sistema de diseño no ejecutan scripts (la guía copia su `body`), así que `MetricGrid/preview.html` muestra ejemplos con `--mg-size` fijado en línea y el algoritmo queda descrito en su `README.md`.

**6. Último impar centrado.** Con 2 columnas y un número impar de datos, el último ocupa las dos columnas y su contenido va centrado. En CSS es `.sq-mgrid .sq-metric:last-child:nth-child(odd) { grid-column: 1 / -1 }`, solo cuando hay 2 columnas. En Compose, la última fila con un solo dato pone ese dato con `weight(1f)` en toda la fila, en lugar de añadir una celda vacía.

**7. La unidad no alarga el valor.** En CSS, `.sq-metric-val .sq-unit { line-height: 1 }`: al alinear por línea base, la unidad heredaba el interlineado de la cifra y alargaba el valor unos 15 px, así que las filas con unidad quedaban desplazadas. En Compose, `MetricTile` alinea hoy la unidad por abajo (`Alignment.Bottom`), no por la línea base. Se pasa a alinear las dos por la línea base (`alignByBaseline`), como en la maqueta, con la unidad a interlineado ajustado (`lineHeight` igual a su tamaño, `LineHeightStyle` con recorte), y un test comprueba que el valor con unidad mide lo mismo que la cifra sola.

## Risks / Trade-offs

- [Con la plantilla, la cifra es algo más pequeña que la que permitirían los valores reales («247» frente a «8888»).] → Es lo que cuesta que no se mueva. Con 1 o 2 datos el tope de 144 px o el alto mandan, así que la diferencia se nota sobre todo con 4 a 6 datos en 2 columnas. Se valida en la maqueta.
- [En horizontal, con botones y la Shimano, la marcha baja hasta el ~90 %.] → Es la marcha la que cede, como se decidió. Con 6 datos se comprueba en el móvil que «52×17» se lee a 1–2 m.
- [`graphicsLayer` no reduce los límites semánticos del bloque escalado.] → `Encajar` informa del tamaño ya escalado, así que el bloque no invade otras zonas; los tests de Compose comprueban que la marcha no se solapa con la rejilla.
- [Letra del sistema muy grande.] → La medida usa la densidad y la escala de letra del móvil, como hoy. Si con 6 datos ni a 16 sp cabe la plantilla, se queda en 16 sp, como el mínimo actual; el test con letra grande sigue.
- [Pantallas más bajas que 844 px, como un móvil de 640 dp de alto.] → El 40 % deja a la marcha menos alto y se reduce. El test de Compose se ejecuta a 360 × 640 en vertical para comprobar que todo cabe sin desplazamiento.

## Migration Plan

Sin datos ni ajustes que migrar: es solo disposición. Si en el móvil no convence, se revierte el change.
