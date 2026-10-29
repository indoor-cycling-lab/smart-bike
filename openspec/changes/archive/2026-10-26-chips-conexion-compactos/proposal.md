## Why

En la pantalla de rodaje en vertical, los chips de conexión llevan el estado en palabra («Neo conectando», «Zwift esperando», «Square sin conexión»). A 360 dp, esos textos no caben en una línea, así que la fila reserva el alto de dos líneas aunque casi siempre esté todo conectado. Ese alto se le quita a la rejilla de datos, que es lo que se lee al rodar. La forma del punto ya distingue el estado sin depender del color, y el detalle se ve tocando el chip, que abre Dispositivos.

## What Changes

- En Rodaje, en horizontal y en vertical, los chips del Neo, del Square y de Zwift muestran solo el nombre y la forma del estado: punto lleno si está conectado, anillo de espera girando si está conectando o esperando, y anillo vacío con borde de alerta si no hay conexión. Desaparecen «conectando», «esperando» y «sin conexión».
- El estado en palabra sigue en la descripción accesible del chip («Square, sin conexión») y en Dispositivos, que se abre al tocar el chip, como hasta ahora.
- En vertical, la fila de chips ocupa una sola línea (32 dp) en lugar de reservar dos, y la rejilla de datos (`MetricGrid`) gana ese alto.
- El chip no cambia de ancho al cambiar de estado, así que nada se mueve al conectar o reconectar.
- En `design-system/` se desactualizan dos pautas que se sustituyen por la nueva regla. Una es la de «reducir los chips a puntos si todo está conectado y expandir el afectado ante un fallo», que la maqueta ya no seguía. La otra es la reserva de dos líneas en vertical.
- Fuera de Rodaje (Dispositivos, Diagnóstico) el chip conserva la palabra de estado.

Fuera de alcance:
- Sustituir los nombres por iconos de dispositivo o dejar solo puntos (opciones descartadas por el usuario).
- Cambiar la pantalla de Dispositivos, los textos del pie («Toca Neo o Square para conectarlos», «Pedalea para despertar el Neo») o cualquier otro elemento de Rodaje que no sean los chips.
- La vibración al perder una conexión y el chip del mando de Zwift (F9), que no están en Rodaje.

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

- `ride-screen`: «Conexiones en la barra superior» deja de mostrar el estado en palabra. «Disposición en vertical» pone las conexiones en una línea. Los escenarios de ERG con el Neo desconectado se describen por la forma del chip.
- `ui-components`: «Estado de conexión por forma y palabra» añade la variante compacta de Rodaje, sin palabra y con la descripción accesible.
- `screen-mockups`: los estados de conexión de «Pantalla de rodaje» y «Pantalla de rodaje en vertical» se describen por la forma del chip; en vertical, la fila de conexiones ocupa una línea.

## Impact

- Sistema de diseño: `design-system/components/ConnectionChip/` (README y preview), `design-system/components/RideScreen/README.md`, `components.css` (reglas de `.sq-ride .sq-conn` y `.sq-ride--v .sq-ride__conns`) y guía regenerada.
- Maquetas: `maquetas/rodaje.html` y `maquetas/rodaje-vertical.html`, todos sus estados con chips en conexión no completa.
- App: `ui/componentes/ConnectionChip.kt` (descripción accesible), `ui/rodaje/Rodaje.kt` (fila de chips en una línea, sin palabra), sus tests de componentes y de `RodajeUiState`.
- Funcionalidades: F11.5 (estado de conexiones en Rodaje) y F1.4 (estado de conexión visible).
- Prueba con el equipo real: en el OnePlus 8 en vertical, ver la fila en una línea con el Neo apagado, el Square dormido y Zwift cerrado, y que al encenderlos nada se mueva.
- Documentos de `docs/`: `planificacion.md` (fila nueva del change). `funcional.md`, `arquitectura.md` y `desarrollo.md` se revisan al final; no se esperan cambios en ellos más allá de una nota en F11 si hace falta.
