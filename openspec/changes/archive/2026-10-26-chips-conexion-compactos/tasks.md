## 1. Maqueta

- [x] 1.1 En `design-system/components.css`, quitar la reserva de dos líneas de `.sq-ride--v .sq-ride__conns` (una sola línea, sin salto)
- [x] 1.2 Actualizar `design-system/components/ConnectionChip/README.md` y `preview.html`: variante de Rodaje con nombre y forma, sin palabra y con `aria-label` («Square, sin conexión»); quitar la regla de reducir a puntos y expandir ante un fallo
- [x] 1.3 Actualizar `design-system/components/RideScreen/README.md`: chips en una línea en vertical, sin palabra en las dos orientaciones, el detalle en Dispositivos; quitar «el chip afectado se expande»
- [x] 1.4 En `maquetas/rodaje.html`, quitar `<span class="st">` de todos los chips y añadir `aria-label` con el estado a cada `a.sq-conn`
- [x] 1.5 En `maquetas/rodaje-vertical.html`, lo mismo en todos los estados, y comprobar a 360 dp que los tres chips caben en una línea en `conectando`, `square-off`, `sin-dispositivos` y `sin-wifi`
- [x] 1.6 Regenerar la guía (`python3 scripts/design-system/generar.py`) y revisar `ConnectionChip` y `RideScreen` en los dos temas
- [x] 1.7 Validación de la maqueta por el usuario

## 2. App

- [x] 2.1 `ConnectionChip`: parámetro `descripcion` que fusiona la semántica del chip; test en `ComponentesTest` (sin palabra visible, descripción «Square, sin conexión», objetivo de 48 dp)
- [x] 2.2 `Rodaje.kt`: dejar de pasar `estadoTexto` en Rodaje y pasar la descripción desde `ChipUi.enPalabra`; actualizar el KDoc de `ChipUi`
- [x] 2.3 `Rodaje.kt`: en vertical, fila de chips en una línea sin reserva de dos; test de Compose a 360 dp con Neo conectando, Square sin conexión y Zwift esperando (los tres en una fila, la marcha no se mueve al pasar a conectados)
- [x] 2.4 Revisar que `RodajeUiStateTest` sigue en verde (no cambia `ChipUi`) y ejecutar `./gradlew :app:testDebugUnitTest`
- [x] 2.5 Probar en el OnePlus 8 en vertical y en horizontal: fila en una línea con el Neo apagado, el Square dormido y Zwift cerrado, nada se mueve al conectarlos, y el chip abre Dispositivos con el estado en palabra

## 3. Documentación

- [x] 3.1 Actualizar la documentación: revisar `docs/funcional.md` (F11.5), `docs/arquitectura.md`, `docs/planificacion.md` (fila del change) y `docs/desarrollo.md`, y actualizar lo que haya cambiado
