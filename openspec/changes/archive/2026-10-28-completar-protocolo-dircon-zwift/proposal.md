# Proposal

## Why

La captura de red de BikeControl con Zwift (28-10-2026, con el Neo real) enseñó que BikeControl responde a las peticiones de Zwift por DirCon con más datos que nosotros: contesta con contenido las páginas 771 y 1024, y el servicio de información del dispositivo ofrece la serie, el firmware y el hardware que Zwift lee. Nuestra app contesta a esas páginas solo con el identificador y no ofrece esas tres características. No sabemos si Zwift lo nota, pero son las diferencias más baratas y probables con el reconocimiento del mando y del rodillo por DirCon, y el único camino que nos queda para acercarnos al comportamiento observado sin tocar el modelo de marchas.

Zwift pide además la información de dispositivo de un Click v2 en BikeControl y recibe el handshake `RideOn 02 00`; no está claro si eso importa. Se prueba con el equipo real, con variantes solo en la compilación de depuración, y lo que no mejore se descarta.

## What Changes

- **Páginas con datos.** En el modo «Todo por wifi (modo Zwift Ride)», la app responde a la petición de las páginas 1024 (entradas del mando) y 771 con las páginas observadas en la captura, en lugar de contestar solo con el identificador. Las páginas 0 y 16 no cambian.
- **Información del dispositivo.** El rodillo que se ofrece a Zwift por wifi añade a su información de dispositivo el número de serie, la versión de firmware y la versión de hardware, que Zwift lee al emparejar. Los valores salen de la identidad propia de la app.
- **Variantes de prueba, solo en depuración.** La compilación de depuración permite elegir, al abrir la app por `adb`, la respuesta al handshake (`RideOn` pelado o `RideOn 02 00` con el aviso `0x2A`), la información del dispositivo (Ride o Click v2) y la página 1024 (la de hoy, la observada o una con las palancas analógicas), para compararlas con Zwift real. La 1024 es la que más cuidado pide: la de BikeControl declara «ninguna analógica» y la app emula un Ride con freno y giro por palancas analógicas. Sin interfaz ni ajuste. Solo se pasan a la compilación normal las que se validen y mejoren; si ninguna mejora, no cambia nada.
- **Registro.** Lo que Zwift pide y lo que se le contesta queda en el registro de sesión, con las páginas nuevas.
- **Prueba con el equipo real** con Zwift, el Neo y el Square: el mando sigue funcionando como hoy, Zwift empareja, y las páginas y la información del dispositivo nuevas no empeoran nada. Un resultado negativo es un resultado.

**Fuera de alcance**
- Aceptar la relación de marcha de Zwift, o que las marchas de Zwift manden sobre las de la app: las marchas siguen siendo las de la app (F9.3). El número de marcha del HUD de Zwift no importa. No se hace el change previsto `marchas-de-zwift` (13c) ni se reenvían los botones de cambio a Zwift.
- Ofrecer los servicios `00000001-…` y `d273f680-…` a Zwift como hace BikeControl: se anota como posibilidad y se prueba solo si lo anterior no basta.
- El campo 6 del mensaje de datos de pedaleo y el tipo de dispositivo `0x06` del anuncio: se desconoce su significado.
- Ajustes o pantallas nuevas: no hay cambio visual y, por tanto, tampoco maqueta.
- El modo de siempre («Rodillo por wifi y mando por Bluetooth»), que no cambia.
- Copiar código de BikeControl o de qdomyos-zwift (NF6): los datos del protocolo se reimplementan a partir de lo observado.

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities
- `zwift-bridge`: la información del dispositivo que Zwift lee del rodillo incluye serie, firmware y hardware.
- `zwift-controller`: con «Todo por wifi», las páginas que Zwift pide al mando se contestan con datos y el registro muestra lo pedido y lo contestado.

## Impact

- **Código:** `:core` (`protocolo/MandoZwiftCodec`: páginas 1024 y 771 en las dos variantes de información; `RespuestaHandshake` con el aviso `0x2A`); `:app` (`zwift/ZwiftBridge`: características de información; `zwift/MandoZwift`: variantes de prueba y registro; `AppGraph` y `MainActivity`: lectura de las variantes por extras del Intent, solo en depuración).
- **Sistema de diseño y maquetas:** ninguno.
- **Documentos de `docs/`:** `arquitectura.md` (§ 7.4, protocolo del mando por DirCon y lo pendiente de validar), `planificacion.md` (fila nueva «13e» en la fase 4 y su estado) y `funcional.md` solo si la prueba cambia lo previsto para F9; `desarrollo.md` con el comando para elegir las variantes por `adb`.
- **Equipo real:** Zwift en el PC, el Neo y el Square, con el móvil de pruebas. Comparar las variantes y comprobar que no hay regresión (10 min sin pulsar, botones, freno, giro).
