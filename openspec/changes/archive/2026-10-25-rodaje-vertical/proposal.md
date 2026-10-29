## Why

El soporte del usuario pone el móvil en vertical, y la pantalla de rodaje del change `marchas-sin-zwift` está fijada en horizontal: hay que girar la cabeza o el soporte para leer la marcha. La disposición vertical estaba prevista en el change 10 (`rodaje-completo-y-ajustes`), junto con los datos configurables y los botones − y +. Este change adelanta solo la vertical, con lo que ya hace la app hoy (transmisión lineal de 24 marchas, potencia y cadencia, sin Zwift), para poder rodar con el soporte tal como está.

## What Changes

- La pantalla de rodaje deja de fijarse en horizontal y sigue la orientación del móvil, respetando el bloqueo de rotación del sistema. En vertical usa la disposición de `RideScreen` en vertical (F11):
  - Arriba, las conexiones y el modo con la transmisión.
  - En el centro, la marcha en grande con «Marcha · de 24» y la escalera de marchas debajo.
  - Debajo, potencia y cadencia en una fila de 2 columnas que ocupa el resto del alto.
  - Al pie, el aviso de acción o el texto «Toca Neo o Square para conectarlos».
- El resto del comportamiento de Rodaje no cambia en vertical: tope, animación, «—» sin Neo, chips que abren Diagnóstico, pantalla siempre encendida y sin barras del sistema.
- **Maqueta:** `maquetas/rodaje-vertical.html` añade los estados lineales sin Zwift que no tiene (sin Zwift, tope en lineal, Square sin conexión y sin dispositivos), con los mismos textos que en horizontal.

Fuera de alcance:

- Datos configurables (de 1 a 6, F11.3), botones táctiles − y + (F11.8) y ajustes de «Al rodar» (F12.7): siguen en el change 10. En vertical solo se muestran potencia y cadencia.
- Transmisiones de marca con plato y cassette en vertical: llegan con el change 8, que ya tiene la maqueta vertical de SRAM como referencia.
- Un ajuste para fijar la orientación desde la app: se usa el bloqueo de rotación del propio Android.
- Diagnóstico: ya sigue la orientación del móvil y no cambia.

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

- `ride-screen`: la pantalla de rodaje se abre en la orientación del móvil y tiene disposición vertical, en lugar de estar fijada en horizontal.
- `screen-mockups`: la maqueta de rodaje en vertical añade los estados lineales sin Zwift.

## Impact

- **Código `:app`:** `ui/rodaje/RodajeRuta.kt` (no fija la orientación), `ui/rodaje/Rodaje.kt` (disposición vertical según el espacio disponible) y `ui/componentes/MetricGrid.kt` (distribución en vertical del README: 1–2 datos en una fila de 2 columnas). Sus tests de Compose y JVM.
- **Maquetas:** `maquetas/rodaje-vertical.html` y `maquetas/index.html`. El sistema de diseño no cambia: `sq-ride--v` ya existe.
- **Dependencias:** ninguna.
- **Documentos de `docs/`:** `planificacion.md` (fila del change nuevo en la fase 1 y la vertical básica quitada del change 10) y `arquitectura.md` § 9.2 si cambia lo que dice de la orientación. `funcional.md` y `desarrollo.md`: ninguno.
