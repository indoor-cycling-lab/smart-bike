## 1. Maqueta

- [x] 1.1 En `design-system/icons.svg`, añadir `i-bolt`, `i-favorite` y `i-mode_fan` (Material Symbols Rounded, peso 400, 24 px) e `i-zwift`: la «Z» en recuadro redondeado, propia, como un solo `<path>` relleno con 80 unidades de grosor (decisión 3). Actualizar el comentario de cabecera: `i-zwift` es propio del proyecto y no imita el logo de Zwift; el sprite incluye también los iconos de la guía. Comprobar con `python3 scripts/design-system/generar.py` que acepta los cuatro símbolos y genera sus `ic_*.xml`
- [x] 1.2 Crear el componente `ConnectionBar` en `design-system/`:
  - En `components.css`, `sq-connbar` (enlace de 48 px de alto, con margen vertical negativo para que la línea siga en 32 px) y `sq-connbar__i` con `is-ok`, `is-wait` e `is-off`. Las máscaras de espera y de tachado, la marca girando y la versión sin movimiento con `prefers-reduced-motion` (decisiones 2 y 6).
  - `components/ConnectionBar/README.md`: estados, orden de los enlaces, iconos, descripción accesible y que es un solo objetivo táctil.
  - `preview.html`: cada icono en los tres estados, la barra con los cuatro enlaces actuales y con seis (pulso y ventilador) a 360 px con el icono de Ajustes.
- [x] 1.3 Actualizar las pautas:
  - `design-system/README.md`, Iconografía: los iconos en Rodaje, solo para Ajustes y la barra; la tabla de iconos por enlace; `i-zwift` es propio.
  - `components/RideScreen/README.md`: la barra superior con `ConnectionBar`; en horizontal, en la línea del modo con Ajustes separado; en vertical, encima del modo.
  - `components/ConnectionChip/README.md`: sin la variante compacta de Rodaje. Retirar `.sq-ride--v .sq-conn` de `components.css`.
  - Regenerar la guía y comprobar que `ConnectionBar` aparece en los dos temas y que el contraste pasa.
- [x] 1.4 En `maquetas/rodaje.html`, sustituir los chips por `sq-connbar` en todos los estados, con la `aria-label` que da el estado de cada enlace. Añadir el icono del mando donde «Mando de Zwift» está activado y el estado nuevo `zwift-mando-sin-conexion`. Cambiar el pie a «Toca las conexiones para conectar el Neo y el Square». Comprobar con `rodaje.js` que la zona de la marcha y la rejilla de datos no cambian de tamaño respecto a antes
- [x] 1.5 En `maquetas/rodaje-vertical.html`, lo mismo que en 1.4. Comprobar a 360 × 640 que la barra con cuatro iconos y Ajustes caben en una línea y que la transmisión se lee entera en «Shimano 105 · automático» y en los estados ERG
- [x] 1.6 En `maquetas/dispositivos.html` y `maquetas/diagnostico.html`, poner el icono de cada enlace delante de su nombre (Neo, Square, Zwift y mando), con los mismos iconos que la barra, en todos los estados. En Diagnóstico, sustituir `directions_bike` del Neo, `sports_esports` del Square y `gamepad` del mando. Retirar `i-gamepad` del sprite si ya nada lo usa
- [x] 1.7 Actualizar `maquetas/index.html` con el estado nuevo y los enlaces a revisar. Revisar todos los estados de rodaje en los dos temas y anotar si «esperando» se distingue a 1–2 m (riesgo del `design.md`)
- [x] 1.8 Validación de la maqueta por el usuario
- [x] 1.9 Pasar a la maqueta lo probado en el OnePlus 8 (decisiones 1 y 6 del `design.md`):
  - En `components.css`, iconos de `sq-connbar` de 18 px, con la marca de espera y el tachado escalados al mismo tamaño y 10 px entre iconos.
  - En `rodaje-vertical.html`, en todos los estados: la barra en la franja superior, a la derecha, y debajo una línea con el modo y la transmisión a la izquierda y Ajustes a la derecha. Dibujar la franja en el marco del móvil (la zona de la cámara). Comprobar con `rodaje.js` que la marcha gana el alto de una línea y que la rejilla no cambia.
  - Comprobar a 360 × 640 que «Shimano 105 · automático», SRAM y los estados ERG se leen enteros junto a Ajustes (una o dos líneas).
  - Actualizar `components/ConnectionBar/README.md` (tamaño, franja), `components/RideScreen/README.md` (vertical), `design-system/README.md` si cita el tamaño, y la vista previa (seis enlaces a 360 px en la franja). Regenerar la guía y comprobar el contraste en los dos temas.
  - Actualizar `maquetas/index.html` con lo que cambia.
- [x] 1.10 Validación por el usuario de la disposición vertical probada en el móvil (27-10-2026, «la vista vertical queda perfecta»), con iconos de 18 dp. Al pasarla a la maqueta (1.9), si algo difiere de lo probado, volver a pedirle validación.

## 2. App

- [x] 2.1 En `RodajeUiState`, sustituir `neo`, `square`, `zwift` y `chipZwift` por `enlaces: List<EnlaceUi>` en orden fijo, con el rodillo y el mando por separado y el mando solo si no está desactivado o no disponible (decisión 5). Tests en `RodajeUiStateTest`:
  - el orden;
  - sin icono del mando con «Mando de Zwift» desactivado y con el móvil sin mando;
  - Zwift conectado y el mando esperando, cada uno con su estado;
  - «No visible» y sin wifi tachan solo Zwift;
  - el mando sin conexión;
  - el Neo y el Square como hoy.
- [x] 2.2 Crear el composable `ConnectionBar` en `ui/componentes`: iconos de 18 dp con 10 dp de separación, el recorte de la marca y del tachado (`BlendMode.Clear` en una capa fuera de pantalla), el arco girando que para con movimiento reducido, un solo `clickable` de 48 dp de alto y la descripción «Conexiones: …» (decisiones 1, 2 y 6). Tests en `ComponentesTest`:
  - el ancho no cambia al pasar un enlace por los tres estados;
  - la descripción accesible lista cada enlace con su estado;
  - el objetivo táctil mide al menos 48 dp;
  - tocar un icono o el hueco entre dos iconos llama a `alTocar`.
- [x] 2.3 En `Rodaje.kt`, sustituir `Chips` por `ConnectionBar`:
  - en horizontal, a la derecha de `Modo`, con `space-6` hasta `BotonAjustes`;
  - en vertical, en la franja superior a la derecha (tarea 2.7).
  
  Cambiar `tocar_para_conectar` y añadir las cadenas de la descripción. Retirar `compacto` de `ConnectionChip` si ya nada lo usa. Tests en `RodajeTest`:
  - a 360 × 640 en vertical, con el Neo conectando, el Square sin conexión, Zwift y el mando esperando, la barra y Ajustes en una línea, y la rejilla conserva su alto;
  - en horizontal, la barra a la derecha del modo y antes de Ajustes;
  - el pie sin dispositivos;
  - tocar la barra abre Dispositivos;
  - el lector de pantalla anuncia la barra y después «Ajustes».
- [x] 2.4 En `Dispositivos.kt` y `Diagnostico.kt`, poner el icono de cada enlace junto a su nombre, con los mismos drawables que la barra. Tests en `DispositivosTest` y `DiagnosticoTest`: cada fila lleva su icono (por `testTag` o descripción). Retirar `i-gamepad` del sprite (ya nada lo usa) y regenerar, que borra `ic_gamepad.xml`
- [x] 2.5 Ejecutar `./gradlew :nucleo:test :app:testDebugUnitTest :app:connectedDebugAndroidTest` y confirmar que pasa en verde, `IconosSincronizadosTest` incluido
- [x] 2.7 Cerrar la prueba de `Rodaje.kt` en vertical (decisión 6): retirar el comentario «PRUEBA» y fijar el diseño.
  - La franja superior mide `max(inset superior seguro, touch-min)`, la barra va centrada a su derecha, y el contenido empieza debajo de ella (también sin cámara en el borde), sin solaparse.
  - La línea del modo y Ajustes: `Modo` ocupa el ancho que deja Ajustes y su texto puede pasar a dos líneas.
  - El lector de pantalla anuncia la barra, después el modo y después «Ajustes».
  - Los iconos de 18 dp, como constante única.
- [x] 2.8 Actualizar los tests a la disposición final:
  - `ComponentesTest`: el ancho mínimo de la barra con iconos de 18 dp.
  - `RodajeTest`: a 360 × 640, la barra en la franja y, debajo, el modo y Ajustes en una línea; sin cámara en el borde, la barra no se solapa con el modo; «Shimano 105 · automático» y ERG de Zwift se leen enteros; la marcha y los datos no se mueven al cambiar los estados. Retirar o adaptar los que daban por hecho barra y Ajustes en la misma línea.
- [x] 2.9 Volver a ejecutar `./gradlew :nucleo:test :app:testDebugUnitTest :app:connectedDebugAndroidTest` y confirmar que pasa en verde.
- [ ] 2.6 Probar en el OnePlus 8 en vertical y en horizontal, en los dos temas, con el Neo, el Square y Zwift en PC:
  - abrir la app con el Neo dormido;
  - apagar el Square;
  - Zwift esperando, conectado al rodillo con el mando esperando, y todo conectado;
  - cerrar Zwift;
  - quitar el wifi;
  - desactivar «Mando de Zwift».
  
  En cada caso, comprobar que los iconos se leen a 1–2 m, que nada se mueve y que tocar la barra abre Dispositivos.

## 3. Documentación

- [x] 3.1 Actualizar la documentación:
  - `docs/funcional.md`: nota en F11 (F11.5, la barra de iconos) y en F1.4 (el mando, por separado en Rodaje).
  - `docs/arquitectura.md` y `docs/funcional.md`: la barra en la franja superior en vertical (F11.5). `docs/arquitectura.md`: § del estado de conexión, ahora con `ConnectionChip` fuera de Rodaje y `ConnectionBar` en Rodaje; lista de componentes de `ui.componentes`.
  - `docs/planificacion.md`: fila 10f y sus notas, y si cambia algo de los changes que añadan pulso o ventilador.
  - `docs/desarrollo.md`: revisar y, si no hay nada que cambiar, dejarlo dicho.
