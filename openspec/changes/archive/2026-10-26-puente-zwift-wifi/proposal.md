## Why

Tras los changes 4, 4b y 5 se rueda con marchas desde el Square y las conexiones con el Neo y el Square son automáticas, pero solo sin Zwift. Para rodar en Zwift hay que seguir usando la app genérica. Este change es el 6 de `planificacion.md` (fase 2, F8.1–F8.5 por wifi y F13.3) y cierra el primer hito útil: rodar en Zwift con las marchas de la app, con el Square emparejado en Zwift solo para girar (`funcional.md`, anexo A.3). Incluye además la prueba de 2 h con la pantalla apagada (NF4) que quedó pendiente del change 5.

## What Changes

- **Puente por wifi (F8.1, F8.5):** la app se anuncia en la red local como un rodillo inteligente llamado «Square Shift» (DirCon: anuncio mDNS y servidor TCP) mientras dura la sesión, con los servicios FTMS, Cycling Power y Device Information. En Zwift para PC se empareja como rodillo controlable, potencia y cadencia. Lo primero que se hace es validar DirCon con Zwift en PC; si no funciona, se para y se replantea con el usuario si se adelanta el change 11 (Bluetooth).
- **Telemetría hacia Zwift (F8.1):** potencia y cadencia del Neo, a frecuencia fija.
- **Pendiente de Zwift con la marcha (F8.2):** el terreno que envía Zwift sustituye a la pendiente base de 0 % y la marcha se suma como hasta ahora (modelo simple). Los cambios de marcha siguen llegando al Neo al instante (NF1).
- **ERG de Zwift (F8.3):** en un entrenamiento, el Neo aplica la potencia que pide Zwift y las palancas la ajustan ±10 W en lugar de cambiar de marcha; la marcha se conserva para cuando Zwift vuelve a simulación.
- **Desconexión de Zwift:** si Zwift se va, la app mantiene un rato lo último que pidió y después vuelve a rodar sin Zwift con la marcha puesta; la app sigue anunciándose para que Zwift vuelva a conectarse solo. Sin wifi, lo dice y se anuncia de nuevo al recuperarlo. Resuelve la decisión abierta 1 de `arquitectura.md` § 15.
- **Estado de Zwift en pantalla (F1.4, F11.5):** chip de Zwift en Rodaje («esperando», conectado o «sin conexión» sin wifi), modo «ERG ZWIFT» con la potencia objetivo en lugar de la marcha, fila de Zwift en Diagnóstico y texto de la notificación.
- **Lo que envía Zwift en Diagnóstico (F13.3):** modo de control, pendiente y parámetros de terreno de Zwift, potencia pedida y ajuste de las palancas en ERG, lo enviado al Neo y las tramas de Zwift en el tráfico.
- **Control manual con Zwift:** el envío manual de Diagnóstico sigue mandando sobre Zwift mientras se está en Diagnóstico; al salir, el Neo vuelve a lo que pide Zwift.
- **Sesión larga (NF4, del change 5):** prueba de 2 h con la pantalla apagada, rodando en Zwift. Si el sistema corta el servicio o el wifi, se decide con el usuario la exención de batería (`arquitectura.md` § 15, 3) con maqueta antes de implementarla.
- **Maqueta:** estados con Zwift de Rodaje (horizontal y vertical, SIM y ERG) y de Diagnóstico, validados por el usuario antes de la app. **BREAKING** respecto a la spec `ride-screen`, que decía que no se mostraba el chip de Zwift.

Fuera de alcance:

- Conexión con Zwift por Bluetooth y elegir el transporte en Ajustes (F8.5 por Bluetooth, F12.1; change 11). Aquí solo hay wifi, sin ajuste.
- Mando de Zwift emulado (F9; changes 12 y 13): la dirección sigue llegando del Square emparejado en Zwift.
- Validar Zwift en Mac y Apple TV (F8.4): el criterio de aceptación es Zwift en PC (decisión 7 de `funcional.md`); Apple TV se prueba si está a mano y, si falla, se apunta como pendiente.
- ERG manual y alternar SIM ⇄ ERG desde el Square (F7.2, F7.3; change 9).
- Transmisiones de marca, modelos por tabla y físico, dificultad y protecciones (changes 8 y 15). El terreno de Zwift se guarda entero, pero el modelo simple solo usa la pendiente.
- Datos de Zwift en Rodaje como el intervalo del entrenamiento o la alerta de cadencia baja (change 10).
- Guardar y compartir el registro (F13.4, change 16).
- Marchas visibles dentro de Zwift (F14).

## Capabilities

### New Capabilities

- `zwift-bridge`: la app como rodillo inteligente ante Zwift por wifi: anuncio y emparejamiento, telemetría, pendiente y ERG de Zwift con la marcha y las palancas, desconexión de Zwift y pérdida del wifi.

### Modified Capabilities

- `ride-screen`: chip de Zwift siempre visible, modo «ERG ZWIFT» con la potencia objetivo en lugar de la marcha (horizontal y vertical), avisos de ±W y Diagnóstico también desde el chip de Zwift.
- `ride-session`: la notificación dice si Zwift está conectado y la potencia en ERG; al terminar, la app deja de anunciarse a Zwift; la sesión de 2 h se valida rodando en Zwift.
- `diagnostics`: fila de Zwift en Conexiones, simulación con lo que pide Zwift, tramas de Zwift en el tráfico y en el registro.
- `trainer-manual-control`: el envío manual manda también sobre Zwift y, al salir de Diagnóstico, el Neo vuelve a lo que pide Zwift.
- `screen-mockups`: estados con Zwift en Rodaje (horizontal y vertical) y en Diagnóstico, y chip de Zwift en los estados sin Zwift.

## Impact

- **Código `:nucleo`:** `DirconCodec`, `FtmsCodec` y `ServidorGatt` nuevos en `protocolo`; `ControlRodillo` con los modos `SimZwift` y `ErgZwift`, el terreno de Zwift y el ajuste ±W; acciones de marcha traducidas a ±W en ERG; `EstadoGuardado` con un identificador estable del puente.
- **Código `:app`:** `TransporteZwift`, `DirconServer` (servidor TCP y anuncio con `NsdManager`), `ZwiftBridge`, estado del wifi y `WifiLock`; `Sesion` (estado de Zwift, modos, telemetría), `RideService` (notificación), `RegistroSesion` (origen Zwift), `ui/rodaje` y `ui/diagnostico`; permisos de red en el manifiesto y `strings.xml`. Sin dependencias nuevas.
- **Maquetas y sistema de diseño:** `maquetas/rodaje.html`, `maquetas/rodaje-vertical.html`, `maquetas/diagnostico.html` y `maquetas/index.html`. Se espera reutilizar `ConnectionChip` («esperando»), `ModeBadge` (ERG de Zwift), la zona ERG de `RideScreen` y `Notice`; si la zona ERG no está resuelta en vertical, se añade a `design-system/`.
- **Documentos de `docs/`:** `arquitectura.md` (§ 7.3 con lo validado de DirCon y FTMS, § 5.2 y § 5.4 con la frecuencia de telemetría, el ajuste ±W y el tiempo de espera tras la desconexión, § 4.4 y § 15 con las decisiones 1, 2 y 3 resueltas, § 10 con el identificador del puente); `funcional.md` (F8 por wifi cumplida, nota de F1.5 y decisión abierta 1 si cambia); `planificacion.md` (estado del change y lo que pase a los changes 10, 11 y 12); `desarrollo.md` (cómo probar con Zwift en PC en la misma red y leer las tramas DirCon).
