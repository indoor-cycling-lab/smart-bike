# Proposal

## Why

Hoy la app solo llega a la plataforma por wifi (DirCon). Hay casos en los que el wifi no sirve: Zwift en Apple TV (F8.4) y las plataformas que solo descubren el rodillo por Bluetooth, además de redes que aíslan los dispositivos entre sí. La planificación (F8.5, NF3) prevé desde el principio el FTMS por Bluetooth como alternativa, y el change `generalizar-plataformas` (24) ya dejó el puente, los modos y los ajustes sin particularizar en Zwift para añadirlo como una fila más de la tabla de compatibilidad. Este change es el 11 de `docs/planificacion.md`, que pasa a llamarse `ftms-bluetooth` porque no es de Zwift.

## What Changes

- Nuevo modo de conexión **«Rodillo por Bluetooth (FTMS)»** (`FTMS_BLUETOOTH`): la app se anuncia por Bluetooth como periférico con FTMS (`0x1826`) y Cycling Power (`0x1818`), con la misma telemetría, el mismo terreno y el mismo ERG que el modo por wifi. Los mismos bytes FTMS que ya valida `FtmsCodec`, otro transporte.
- Aparece en la pestaña **Plataformas** como tercera fila de Zwift, sin recomendar y marcado «a validar» en lo que falta (Apple TV, subida real, ERG y sesión larga). No necesita wifi, pero sí Bluetooth encendido, permisos y modo periférico en el móvil (NF3).
- El modo lleva el **mando de Zwift dentro del mismo dispositivo**: un solo servidor GATT y un solo anuncio (FTMS, Cycling Power y `FC82` con los datos de fabricante de Zwift), siempre activo y sin interruptor, como Zwift Ride pero por Bluetooth. Comprobado con Zwift en PC (tarea 6.6): lista el móvil también como mando, hace el handshake y los botones del Square mueven Zwift, así que el Square ya no se empareja en Zwift. El nombre Bluetooth que ve la plataforma es el del móvil (lo único que Android deja anunciar), así que «Nombre ante la app» no se ofrece en este modo.
- El estado de la plataforma en Rodaje, Plataformas y Diagnóstico habla de Bluetooth (no de wifi) cuando el modo es este: «Sin Bluetooth para Zwift» y «No disponible» si el móvil no puede anunciarse como periférico.
- Cambiar de modo desde o hacia este rodando funciona como cualquier otro cambio (mantiene lo último 30 s, la marcha no cambia).
- Pasa por maqueta (validada en una primera versión sin mando y por validar de nuevo con el mando dentro): Plataformas y Diagnóstico con el modo nuevo y sus estados, y Rodaje con el estado de la plataforma por Bluetooth.

## Capabilities

### New Capabilities
- `ftms-bluetooth`: la app como rodillo FTMS y fuente de potencia y cadencia por Bluetooth: anuncio, identidad, requisitos del móvil, terreno y ERG, desconexión y comportamiento sin Bluetooth.

### Modified Capabilities
- `platform-settings`: la lista de modos de Zwift incluye el tercero; con él no hay «Nombre ante la app» ni interruptor del mando (el mando va dentro, siempre activo); la tabla de compatibilidad lo marca «a validar»; el estado de la plataforma y del mando habla de Bluetooth.
- `platform-bridge`: «Nombre ante la plataforma» pasa a valer solo para los modos por wifi.
- `ride-screen`: el estado de la plataforma en Rodaje con el modo por Bluetooth («Sin Bluetooth para Zwift»).
- `screen-mockups`: la maqueta de Plataformas, Diagnóstico y Rodaje con los estados del modo nuevo, con el mando dentro.

## Impact

- **Código (`:core`):** `ModoConexion.FTMS_BLUETOOTH` y su fila en `Compatibilidad` (`MandoEnModo.DENTRO_DEL_MODO`, `Validacion.EN_PARTE`); `Configuracion` sin cambio de versión (el modo nuevo es un valor más del enumerado).
- **Código (`:app`):** `ConexionFtmsWifi` pasa a `ConexionFtms`, parametrizada por modo y transporte; un segundo `GattServerHost` con el anuncio FTMS, Cycling Power y `FC82`; `MandoZwiftEnServidor`, el mando de Zwift como un servicio más del servidor del rodillo; `PuentePlataforma` deja de suponer que el enlace es el wifi; `AppGraph`, estados de `Rodaje`, `Plataformas` y `Diagnóstico`.
- **Dependencias nuevas:** ninguna.
- **Documentos de `docs/`:** `funcional.md` (F8.5 y F12.9), `arquitectura.md` (§ 4.3, § 7.3 y § 15), `planificacion.md` (fila y nota del 11) y `desarrollo.md` si cambia algo del despliegue o de las pruebas.
- **Prueba con el equipo real:** OnePlus 8 con el Neo y el Square, Zwift en PC y en Apple TV por Bluetooth: emparejar rodillo, potencia y cadencia; rodar una ruta con subidas y un entrenamiento ERG; cambiar de modo rodando.

## Fuera de alcance

- Las marchas virtuales de Zwift: con el mando dentro, Zwift sigue controlando el rodillo por FTMS (`Simulacion`) y las marchas son las de la app.
- Que Zwift reconozca el dispositivo entre sesiones por Bluetooth: queda como limitación (ver `design.md`, riesgos).
- Otras plataformas (MyWhoosh, Rouvy…); salen de este change solo si se añaden a la tabla en uno posterior.
- Un servicio de pulso específico para Bluetooth: el modo ofrece el mismo conjunto de servicios que el de wifi (el `PuenteFtms` incluye Heart Rate), sin cambios; que la plataforma lo acepte por Bluetooth queda «a validar».
- Que Zwift admita a la vez el rodillo por wifi y por Bluetooth: un solo modo a la vez, como hoy.
- Nombre anunciado distinto del nombre Bluetooth del móvil.
