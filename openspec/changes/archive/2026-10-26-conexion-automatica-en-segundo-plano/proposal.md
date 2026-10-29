## Why

Tras los changes 4 y 4b ya se rueda con marchas sin Zwift, pero cada sesión empieza con un paso manual: tocar un chip, abrir Diagnóstico y pulsar «Buscar dispositivos». Además, si el Square se duerme o el Neo pierde la conexión a mitad, hay que bajarse a tocar el móvil. Este change es el 5 de `planificacion.md` (fase 1, F1.2–F1.5): cumple los criterios de aceptación de F1 («al abrir la app se conecta a Neo y Square sin pasos manuales» y «una desconexión breve se recupera sola sin perder la marcha») y valida NF4 con una sesión de 2 h con la pantalla apagada. Va antes del puente con Zwift (change 6), que depende de él.

## What Changes

- **Dispositivos recordados (F1.2):** al conectar un Neo o un Square, la app guarda su dirección y su nombre en `estado.json`. Una búsqueda manual que encuentra otro dispositivo del mismo tipo lo sustituye.
- **Conexión al abrir (F1.2):** con dispositivos recordados, la sesión se conecta a ellos sola al abrir la app, sin buscar y sin tocar nada. En el primer uso (nada recordado) se sigue conectando desde Diagnóstico, como hoy.
- **Reconexión automática (F1.3):** al perder un dispositivo conectado, o si no responde al abrir la app, la app lo vuelve a intentar sola mientras dure la sesión, también con la pantalla apagada, hasta que el dispositivo vuelve. Se conservan la marcha y la otra conexión, y al volver el Neo recibe la resistencia de la marcha puesta. Con el Bluetooth apagado se espera y, al encenderlo, se reconecta. **BREAKING** respecto a la spec `device-connection`, que decía «en este change no reconecta sola».
- **Estado visible mientras reconecta (F1.4):** en Rodaje, el chip del dispositivo que falta dice «conectando» y el pie dice qué hacer para despertarlo («Pedalea para despertar el Neo», «Pulsa un botón del Square para despertarlo»); en Diagnóstico, su fila dice «Reconectando» con un aviso informativo en lugar del error; la notificación dice qué dispositivo se está reconectando.
- **Sesión larga en segundo plano (F1.5, NF4):** prueba de 2 h con la pantalla apagada en el OnePlus 8. Si el sistema corta el servicio o la conexión, se decide con el usuario cómo pedir la exención de optimización de batería (decisión abierta 3 de `arquitectura.md` § 15) y se actualiza este change antes de implementarla.
- **Pruebas pendientes de changes anteriores:** apagar el Square a mitad de sesión sin perder la marcha (del 4), apagarlo con un botón mantenido y comprobar que se libera y el Neo sigue conectado (del 3), y el efecto en la batería del Square de la prioridad alta sostenida (del 4), con su nivel de batería en el registro.
- **Maqueta:** estados nuevos de Rodaje (horizontal y vertical) y de Diagnóstico para la reconexión, validados por el usuario antes de la app.

Fuera de alcance:

- Pantalla de Dispositivos, «Olvidar» un dispositivo y elegir uno de una lista (F12.1, change 10). Aquí, para cambiar de Neo o de Square se usa «Buscar dispositivos» en Diagnóstico.
- El chip y la conexión de Zwift (F1.4 para Zwift, change 6) y del mando (changes 12 y 13).
- Mostrar la batería del Square en pantalla: solo va al registro.
- Guardar y compartir el registro de sesión (F13.4, change 16); la prueba de 2 h se analiza con `adb logcat`.
- Cambiar cuándo empieza o termina la sesión: sigue empezando al abrir la app y terminando con Atrás desde Rodaje o «Terminar».

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

- `device-connection`: los dispositivos se recuerdan, la app se conecta a ellos al abrir y reconecta sola al perderlos o al volver el Bluetooth; la búsqueda manual sustituye al recordado; el estado «Reconectando» sustituye a «Conexión perdida» mientras se intenta.
- `ride-screen`: el chip de un dispositivo que se está reconectando dice «conectando» y el pie dice cómo despertarlo; «Toca Neo o Square para conectarlos» queda para cuando no hay nada recordado.
- `ride-session`: la notificación dice qué se está reconectando, y la sesión aguanta 2 h con la pantalla apagada sin cortes.
- `screen-mockups`: estados de reconexión en Rodaje (horizontal y vertical) y en Diagnóstico.

## Impact

- **Código `:nucleo`:** `EstadoGuardado` con el Neo y el Square recordados (dirección y nombre), compatible con los `estado.json` actuales.
- **Código `:app`:** `ble/ClienteBle` y `ConexionBle` (conexión en segundo plano a una dirección conocida y estado `Reconectando`), `sesion/Sesion` (conexión al abrir, reconexión, Bluetooth que vuelve, recordar dispositivos), `SquareClient` (lectura de la batería al registro), `RideService` (textos de la notificación), `ui/rodaje` y `ui/diagnostico` (estados y textos nuevos), `strings.xml`. Sin dependencias nuevas.
- **Maquetas y sistema de diseño:** `maquetas/rodaje.html`, `maquetas/rodaje-vertical.html`, `maquetas/diagnostico.html` y `maquetas/index.html`. Se espera reutilizar `ConnectionChip` y `Notice` (informativo) sin variantes nuevas.
- **Documentos de `docs/`:** `arquitectura.md` (§ 8, máquina de estados de reconexión y conexión en segundo plano; § 4.4 y § 15, exención de batería resuelta; § 7.1, batería del Square y estabilidad de las direcciones; § 10, dispositivos recordados); `funcional.md` (F1 cumplida y, si hace falta, nota sobre la batería); `planificacion.md` (estado del change y lo que pase al 6 o al 10); `desarrollo.md` (cómo hacer la prueba de 2 h y leer el registro).
