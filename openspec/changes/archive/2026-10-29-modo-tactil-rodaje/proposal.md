# Proposal

## Why

Hoy SIM ⇄ ERG manual solo se alterna con la acción «Alternar SIM ⇄ ERG» asignada a un botón del Square (△ larga por defecto, F7.3). Quien rueda sin Square, o no ha asignado esa acción, no puede entrar en ERG manual de ninguna forma, aunque Rodaje sí le deja cambiar de marcha y ajustar ±10 W con los botones − y + (F11.8). Es un hueco en el principio de que lo que se hace con un botón se pueda hacer también en pantalla, y deja sin cumplir la promesa de «rodar sin Zwift en Libre o en ERG manual» de la guía de inicio (`guia-inicio`, ya archivada, que `modo-libre-y-pendiente` ajusta a ese nombre).

## What Changes

- El indicador de modo de Rodaje (`ModeBadge`: «LIBRE», «SIM» o «ERG MANUAL», según el modo; ver `modo-libre-y-pendiente`) se puede tocar y alterna Libre o SIM ⇄ ERG manual, con exactamente la misma semántica que la acción «Alternar SIM ⇄ ERG» del Square (F7.3): la potencia de entrada es la última usada, la marcha se conserva y, en un entrenamiento de la app, no hace nada.
- Es tocable solo con un ajuste nuevo e independiente, «Cambiar de modo tocando», desactivado por defecto. No depende de los botones − y +: con ellos visibles y el ajuste apagado, el indicador sigue siendo una etiqueta. La casilla de los botones conserva su nombre, «Botones − y + en pantalla».
- Sin ventana de confirmación: el cambio es reversible con otro toque y el pie ya avisa («ERG manual · 200 W», «Modo Libre» o «Modo SIM»).
- El modo Libre o SIM ⇄ ERG deja de ser exclusivo del Square: sin controlador (o con él sin conexión), se alterna tocando.

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

- `ride-screen`: el indicador de modo se puede tocar (objetivo de al menos 48 dp) cuando el ajuste «Cambiar de modo tocando» está activado, y lo que hace en cada modo; el botón de la fila del modo no debe solaparse con Ajustes en vertical ni con la línea superior en horizontal.
- `manual-erg`: Libre o SIM ⇄ ERG manual también se alterna desde Rodaje con el ajuste activado, no solo desde el Square; «sin tocar el móvil» sigue siendo el modo de uso por defecto.
- `settings-screen`: casilla nueva «Cambiar de modo tocando» en «Al rodar», independiente de la de los botones − y +, con su ayuda.
- `screen-mockups`: la maqueta de Rodaje gana los estados con el indicador de modo tocable (Libre, SIM, ERG manual y ERG APP) y la de Ajustes, la casilla nueva.

## Impact

- **Código:** `Rodaje.kt` y `RodajeViewModel.kt` (el indicador pasa a ser un botón que envía `Accion.AlternarErgManual`, la misma que la del Square, sin lógica nueva de modo), `ModeBadge.kt`, la casilla nueva en `Ajustes.kt` y sus recursos de texto, y `Configuracion`. Sin cambios en `:core`, BLE ni DirCon.
- **Sistema de diseño:** `ModeBadge` gana una variante tocable (relleno tintado, estado de pulsación y objetivo táctil de 48 dp) en `components.css`, su `README.md` y `preview.html`; la guía se regenera.
- **Datos:** `Configuracion` gana un campo booleano (`modoTactil`, `false` por defecto) que viaja en la copia de seguridad; al faltar en los documentos anteriores toma el valor por defecto, así que no hay migración ni cambio de versión.
- **Documentación (`docs/`):** `funcional.md` (F7.3, F11.5, F11.8 y F12.7), `planificacion.md` (fila del change) y `arquitectura.md` solo si cambia el flujo de acciones; `desarrollo.md`, ninguno.
- **Prueba con el equipo real:** en el OnePlus 8, sin Square asignado, alternar Libre ⇄ ERG manual tocando el indicador con el Neo conectado y comprobar que el Neo pasa de la pendiente de la marcha a la potencia fijada y vuelve.

## Fuera de alcance

- Una ventana de confirmación o un toque largo: se descartaron porque el cambio es reversible y el ajuste ya limita el riesgo de toque accidental.
- Que el cambio de modo dependa de la casilla de los botones − y +: son controles independientes.
- Elegir la potencia de ERG manual escribiéndola en pantalla; sigue siendo la última usada y se ajusta ±10 W.
- La guía de inicio (`guia-inicio`, archivada) y la de modos de uso (`guia-modos-de-uso`): usan los nombres de este change.
- Cambiar de modo dentro de un entrenamiento de la app: no se puede, como con el Square.
