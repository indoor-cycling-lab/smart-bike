# Proposal

## Why

Hoy Zwift ve dos dispositivos de la app: el rodillo por wifi (DirCon) y el mando por Bluetooth (`mando-zwift-emulado`). En las pruebas de `prototipo-mando-zwift` y `mando-zwift-emulado` (27-10-2026) el mando funcionó por DirCon **dentro del rodillo**, en un solo dispositivo, sin servidor GATT ni anuncio Bluetooth. Zwift lo trató entonces como un rodillo con marchas virtuales («GEAR» en el HUD) y empezó a mandar `SET` con la pendiente, CW, Crr, las masas y la relación de su marcha. Se descartó por chocar con F9.3 (las marchas son de la app).

La etapa 1 de este change (27-10-2026, con el equipo real) comprobó que se puede **usar ese protocolo de rodillo de Zwift y seguir con nuestras marchas**: los `SET` traen la pendiente de la ruta y la potencia de ERG, y la app puede contestarlos y mandar la potencia y la cadencia con su mensaje de datos de pedaleo, ignorando la relación de marcha de Zwift. El usuario obtiene un modo con **un solo dispositivo ante Zwift y todo por wifi**, con las marchas simple, por tabla o físico de siempre (F6, F9).

## What Changes

- **Segundo modo de conexión con Zwift: «Todo por wifi (modo Zwift Ride)».** El rodillo y el mando son un solo dispositivo DirCon: el servicio `FC82` del mando va dentro del rodillo, sin servidor GATT ni anuncio Bluetooth. El modo de hoy («Rodillo por wifi y mando por Bluetooth») no cambia y sigue siendo el predeterminado.
- **Protocolo de rodillo de Zwift, sin sus marchas.** En el modo nuevo, Zwift manda la pendiente, Crr y CW, y la potencia de los entrenamientos ERG, por `SET`; la app los contesta, simula en el Neo el terreno con la marcha de la app y el modelo elegido, y manda a Zwift la potencia y la cadencia con su mensaje de datos de pedaleo. La relación de marcha de Zwift se registra y se ignora: las palancas del Square siguen cambiando la marcha de la app. Solo se implementa el subconjunto del protocolo que hace falta.
- **Identidad propia del modo nuevo.** Zwift recuerda los dispositivos por su identidad; el modo nuevo se anuncia con otra, para que cambiar de modo no deje a Zwift con un dispositivo que ya no encaja.
- **Los dos modos, separados en el código.** Cada uno es una implementación de la misma interfaz (`ConexionZwift`), sin ramas por modo en la `Sesion`; comparten solo lo que no depende del modo (DirCon, el puente FTMS, el núcleo del mando y el camino del terreno hasta el Neo).
- **Ajustes, Dispositivos y Diagnóstico.** Ajustes, en «Zwift», ofrece «Conexión con Zwift» con los dos modos; Dispositivos dice cómo emparejar el mando según el modo; Diagnóstico muestra la última simulación recibida de Zwift (pendiente, Crr, CW, masas, relación de su marcha) y su tráfico en el registro.
- **Etapa 1 hecha.** La prueba con el equipo real (pantalla de depuración, sin maqueta, como el 12 y el 13) está hecha y la puerta resuelta; sus resultados están en `design.md`. Quedan la maqueta y la app definitiva.
- Pasa por maqueta: Ajustes (la elección del modo), Dispositivos (fila del mando con el modo nuevo y sus estados) y Diagnóstico (simulación de Zwift).

**Fuera de alcance**
- Que Zwift siga las marchas de la app o que la app siga las de Zwift (change `marchas-de-zwift`, 13c): Zwift avisa de que el rodillo no admite marchas virtuales cuando se le mandan sus botones de cambio, incluso declarando capacidad de rodillo, y no está resuelto. El HUD de Zwift muestra su marcha fija; es un coste conocido del modo nuevo.
- Los botones de cambio de marcha del Ride hacia Zwift: las palancas del Square no se reenvían, como hasta ahora.
- Implementar el protocolo de Zwift entero (páginas de configuración del rodillo, índices de marcha, `virtualShiftingMode`, viento, calibración).
- Quitar el modo de hoy: sigue siendo el predeterminado y el validado.
- Mando por DirCon como dispositivo aparte del rodillo: Zwift solo admite un dispositivo DirCon por IP.
- Usar las masas o el diámetro de rueda de Zwift en el modelo físico (se muestran en Diagnóstico).
- El transporte Bluetooth del puente FTMS (change 11) y Apple TV.
- Copiar código de BikeControl o de qdomyos-zwift (NF6): el protocolo se reimplementa a partir de su descripción.

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities
- `zwift-bridge`: el modo nuevo acepta los `SET` de Zwift, los contesta, simula en el Neo su terreno con la marcha de la app, atiende el ERG y manda a Zwift la potencia y la cadencia con su mensaje de datos de pedaleo.
- `zwift-controller`: el mando puede ir por wifi dentro del rodillo en un solo dispositivo ante Zwift, con identidad propia, y cambiar de modo con la sesión abierta.
- `settings-screen`: elección del modo de conexión con Zwift en la sección «Zwift».
- `device-connection`: la fila del mando según el modo.
- `diagnostics`: la simulación que manda Zwift en Conexiones y en el registro.
- `screen-mockups`: los estados nuevos de las maquetas de Ajustes, Dispositivos y Diagnóstico.

## Impact

- **Código:** `:nucleo` (`protocolo/`: `SimulacionZwiftCodec` y `PedaleoZwiftCodec`, a mano como el resto de protobuf; `dominio/`: `SimulacionZwift`, `AcumuladorSimulacion`, `ModoConexionZwift` en `Configuracion` versión 6, `puenteZwiftRide` en `EstadoGuardado`); `:app` (`zwift/`: interfaz `ConexionZwift` con `ConexionZwiftFtms` y `ConexionZwiftRide`, `MandoZwift` reducido a su núcleo, `ZwiftBridge` con servicios extra; `Sesion` que solo conoce la interfaz; `AppGraph`; `Ajustes`, `Dispositivos`, `Diagnostico`). El andamiaje de la etapa 1 (pantalla de depuración, variantes) se retira.
- **Sistema de diseño y maquetas:** ningún componente ni token nuevos previstos; cambios en `maquetas/ajustes.html`, `dispositivos.html`, `diagnostico.html` y el índice.
- **Documentos de `docs/`:** `funcional.md` (F9.2, F12.9, F8.5), `arquitectura.md` (§ 4.3, § 5.2, § 5.4, § 7.3, § 7.4, § 10, § 15 decisión 4), `planificacion.md` (fila y notas del 13b y del 13c), `desarrollo.md` solo si cambia la prueba con el equipo.
- **Equipo real:** la etapa 1 validó el protocolo (ver `design.md`); queda validar con la app definitiva los dos modos, el cambio de modo rodando, la latencia de la palanca (NF1) y 10 minutos sin pulsar nada.
