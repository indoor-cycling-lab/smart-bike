# Tasks

## Maqueta

### 1. Maqueta de rodaje en vertical

- [x] 1.1 Añadir a `maquetas/rodaje-vertical.html` los estados `sin-zwift` (lineal 24, marcha 14, potencia y cadencia en una fila, chips Neo y Square sin chip de Zwift, aviso «+1 marcha»), `tope-lineal` («24» atenuado con «Tope · marcha más dura», último segmento de la escalera resaltado), `square-off` (chip «Square sin conexión», datos con valor, pie «Toca Neo o Square para conectarlos») y `sin-dispositivos` (los dos chips «sin conexión», datos «—», mismo pie), cada uno en su `section` (design.md, decisión 4), con los chips enlazando a `diagnostico.html` y la marca de referencia en la 12, solo con clases del sistema de diseño; verificar cada estado con `#estado=…` en los dos temas, a 390 dp de ancho, sin desplazamiento y contra la spec delta de `screen-mockups`
- [x] 1.2 Comprobar que `GearDisplay` lineal con «Marcha · de 24» cabe al lado de «24» a 390 dp y a 360 dp de ancho (design.md, riesgo 1); si no cabe, ajustar la disposición vertical en la maqueta (y en `components.css` y el README de `RideScreen` si hace falta una variante), regenerar con `python3 scripts/design-system/generar.py` y verificar que termina sin errores de contraste
- [x] 1.3 Actualizar `maquetas/index.html` con los estados nuevos de Rodaje (vertical); verificar que cada enlace abre su estado

### 2. Validación

- [x] 2.1 Validación de la maqueta por el usuario: darle los enlaces exactos (`maquetas/rodaje-vertical.html#estado=sin-zwift`, `#estado=tope-lineal`, `#estado=square-off` y `#estado=sin-dispositivos`) y esperar su aprobación explícita; si pide cambios, ajustar maqueta, `design.md` y specs y volver a pedirla

## App

### 3. Disposición vertical

- [x] 3.1 `MetricGrid` con el parámetro `vertical` (2 columnas, filas según el número de datos, 64 sp con 1–2 datos; design.md, decisión 3); test en `ComponentesTest` con 2 datos en vertical en una fila; verificar con `./gradlew connectedDebugAndroidTest`
- [x] 3.2 `Rodaje` elige la disposición con `BoxWithConstraints` (vertical si el alto es mayor que el ancho) y compone la vertical de la maqueta aprobada (design.md, decisión 2); `RodajeTest` fija el tamaño del contenedor en cada test y añade los estados verticales (`sin-zwift`, `tope-lineal`, `sin-dispositivos`) comprobando que todo es visible; verificar con `./gradlew connectedDebugAndroidTest`
- [x] 3.3 `RodajeRuta` deja de fijar la orientación y mantiene las barras ocultas y la pantalla encendida (design.md, decisión 1); verificar en el OnePlus 8 (skill `desplegar-movil`) con capturas en vertical y en horizontal comparadas con `maquetas/rodaje-vertical.html#estado=sin-zwift` y `maquetas/rodaje.html#estado=sin-zwift`, y que con el bloqueo de rotación activado se queda en vertical

### 4. Validación con el equipo real

- [x] 4.1 Prueba con el móvil en el soporte en vertical: el usuario rueda con el Neo y el Square, cambia de marcha, llega a un tope y apaga y vuelve a conectar algo desde Diagnóstico; confirma que todo se lee desde la bici; anotar el resultado en `design.md`

## Documentación

### 5. Documentación

- [x] 5.1 Actualizar la documentación: revisar `docs/funcional.md`, `docs/arquitectura.md` (§ 9.2, la orientación de Rodaje), `docs/planificacion.md` (fila y notas del change 4b y alcance del 10) y `docs/desarrollo.md`, y actualizar lo que el change haya cambiado; verificar que ningún documento dice que Rodaje está fijada en horizontal
