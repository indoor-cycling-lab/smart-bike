## Why

Android puede dar por registrado el anuncio mDNS de la app («Square Shift», `_wahoo-fitness-tnp._tcp`) sin responder de verdad en la red. Entonces Zwift no ve el rodillo, mientras la app dice «Esperando» como si todo fuera bien. Pasó el 27-10-2026 en el OnePlus 8. `dumpsys servicediscovery` daba el servicio por anunciado («Probing finished», `onRegisterServiceSucceeded`), pero desde el Mac no se veía y el móvil no contestaba a consultas mDNS directas. En el servicio mDNS del sistema había errores `EADDRINUSE` al unirse al grupo multicast en `wlan0`. Apagar y encender el wifi lo arregló. No es un fallo de la app: al usuario le pasaba también a veces con qdomyos-zwift, y lo resolvía reiniciando el wifi o el móvil. Hoy la única forma de saberlo es comprobarlo desde otro equipo, así que el usuario pierde tiempo buscando la causa en Zwift.

## What Changes

- Mientras la app se anuncia y Zwift no está conectado, comprueba que su propio anuncio responde en la red wifi. Lo hace al anunciarse y después cada minuto, y deja de comprobarlo en cuanto Zwift se conecta.
- Si el anuncio no responde, la app se vuelve a anunciar una vez por si basta con eso. Si sigue sin responder, muestra Zwift como «No visible» y dice qué hacer: apagar y volver a encender el wifi del móvil y, si sigue igual, reiniciar el móvil.
- En Dispositivos, la fila de Zwift dice «No visible», con el aviso de error y qué hacer. En Rodaje, el chip «Zwift» pasa al anillo vacío con el borde de alerta, y su descripción accesible dice «Zwift, no visible».
- En cuanto el anuncio vuelve a responder (por ejemplo, al reiniciar el wifi), el aviso desaparece sin hacer nada más.
- El registro de la sesión (Diagnóstico) apunta el resultado de cada comprobación.

Fuera de alcance:
- Arreglar el servicio mDNS de Android desde la app (no está a su alcance) o reiniciar el wifi por el usuario (Android no lo permite a las apps desde la versión 10).
- `MulticastLock`. No arregla este fallo, porque los sockets son del servicio del sistema y no de la app. Sigue pendiente validar si hace falta con la pantalla apagada (`arquitectura.md`, «a validar»), que es otro problema.
- Comprobar que Zwift ve la app, o que el equipo de Zwift está en la misma red: la app solo puede saber si su anuncio responde.
- El transporte Bluetooth (change 11) y el mando por DirCon (change 13b).

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

- `zwift-bridge`: requisitos nuevos «Anuncio comprobado en la red» (la comprobación y el reintento) y «Zwift no visible» (el aviso y la vuelta a «Esperando»).
- `ride-screen`: «Conexiones en la barra superior» añade el chip «Zwift» en «No visible», también con el mando de Zwift activado.
- `screen-mockups`: «Dispositivos» añade el estado «Zwift no visible», y un requisito nuevo lo añade a las maquetas de rodaje.

## Impact

- Funcionalidades: F8.5 (conexión con Zwift por wifi) y F1.4 (estado de conexión visible). Sirve también a NF2 (no hay que diagnosticar desde otro equipo).
- App: una sonda mDNS que envía una consulta propia y espera la respuesta del responder del sistema (`zwift/`). El estado `EstadoZwift.NoVisible` y su calendario de comprobaciones en `Sesion`. Textos y estado en `ui/dispositivos` y en el chip de `ui/rodaje`. Sin dependencias nuevas.
- Núcleo: codificar la consulta DNS y reconocer la respuesta, en Kotlin puro y con tests.
- Sistema de diseño y maquetas: no hay componentes nuevos (se usan `ConnectionChip` en `off` y `Notice` `is-danger`). Hay un estado nuevo `zwift-no-visible` en `maquetas/dispositivos.html`, `maquetas/rodaje.html` y `maquetas/rodaje-vertical.html`.
- Prueba con el equipo real: el caso normal (sin falsos avisos) en el OnePlus 8 con Zwift en PC. El fallo real no se puede provocar a voluntad (ver `design.md`).
- Documentos de `docs/`: `planificacion.md` (fila y nota del change 6b), `arquitectura.md` (sonda del anuncio en DirCon y lo validado) y `funcional.md` (nota en F8). `desarrollo.md`: cómo comprobar el anuncio desde el Mac con `dns-sd`.
- Parte de las specs tal como las dejó el change 13 (`mando-zwift-emulado`, archivado): con el mando activado, el chip «Zwift» reúne el rodillo y el mando.
