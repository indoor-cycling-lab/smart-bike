## Why

Todo lo que viene después (marchas, puente Zwift, gestos) se apoya en supuestos sobre el Elite Square y el Tacx Neo 1 que solo conocemos por implementaciones de terceros y que `arquitectura.md` § 7.1 y § 7.2 marca **a validar**: que la característica `0x43` informe de todos los botones con pulsaciones rápidas, qué bit es cada palanca, con qué frecuencia notifica el Square y qué checksum acepta el Neo en la página FE-C 0x33. Este change es la fase 0 de `planificacion.md`: la primera porción vertical con el equipo real, que confirma o corrige esos supuestos y deja trazas grabadas para los tests de los changes siguientes.

## What Changes

- La app busca y conecta el Neo y el Square desde la propia pantalla de Diagnóstico (F1.1), pide los permisos de Bluetooth y muestra el estado de cada conexión, con los casos de Bluetooth apagado, permiso denegado, dispositivo no encontrado y conexión perdida.
- Lectura de los 20 botones del Square (F2.1) a partir de los contadores por botón de la característica `0x45`: la validación con el Square real mostró que la máscara de `0x43` pierde la mitad de las pulsaciones rápidas y `0x45` ninguna. De los contadores salen los eventos y la máscara de botones pulsados, con eventos de pulsar y soltar y varios a la vez (F2.2, adelantado del change 4 junto con `EventosBoton`, porque Diagnóstico los necesita para iluminar varias teclas y liberarlas al perder la conexión): las teclas se iluminan mientras se pulsan y se ve la última combinación pulsada y la máscara en hexadecimal.
- Tráfico en crudo del Square y del Neo con hora, origen y bytes, con pausa y limpieza (F13.1).
- Control manual del Neo: enviar una pendiente (FE-C 0x33) o una potencia (FE-C 0x31) con steppers con límites (F13.2). Al salir de Diagnóstico, el Neo vuelve a pendiente 0 %.
- Registro de sesión en memoria con todas las tramas, volcado también al registro del sistema con una etiqueta propia para capturar trazas desde el Mac.
- Arranque real de la app: la pantalla provisional de `MainActivity` se sustituye por la navegación, con Diagnóstico como pantalla inicial mientras no existan las demás.
- Iconos de `icons.svg` como recursos de la app, generados por `generar.py` igual que el resto de derivados.
- Maqueta de Diagnóstico ampliada con la sección de conexiones y los estados de búsqueda, Bluetooth apagado, permiso denegado, dispositivo no encontrado, conexión perdida, límites del control manual y sin rodaje en curso; estado deshabilitado del stepper en el sistema de diseño.
- Sesión de validación guiada con el equipo real que resuelve lo marcado **a validar** en § 7.1 y § 7.2 y, si se puede, la decisión abierta 2 de `funcional.md` (palanca 1 y 2).

Fuera de alcance:

- Gestos, asignación, marchas y modelo de resistencia (changes 4 y 7): Diagnóstico muestra la última combinación de botones, sin gesto ni acción, y oculta la sección Simulación mientras no haya rodaje.
- Recordar dispositivos, conectar al abrir, reconectar solo y mantener la conexión en segundo plano (F1.2–F1.5, change 5): aquí una conexión perdida se recupera tocando «Buscar dispositivos», y la conexión vive mientras la app está abierta, sin *foreground service*.
- Decodificar la telemetría del Neo (potencia y cadencia) y medir la latencia de las palancas (NF1): llegan con el change 4. Aquí la telemetría se ve en crudo.
- Guardar y compartir el registro de sesión (F13.4, change 16): el botón «Compartir registro» de la maqueta no aparece todavía en la app.
- Lo que envía Zwift (F13.3, change 6).
- Elegir entre varios Neo o Square cercanos: se conecta el primero cuyo nombre encaja.

## Capabilities

### New Capabilities

- `device-connection`: búsqueda y conexión con el Neo y el Square, permisos y Bluetooth, estado de cada conexión y su pérdida.
- `square-buttons`: reconocimiento de los botones del Square como eventos de pulsar y soltar, incluidos varios a la vez y la liberación al perder la conexión.
- `trainer-manual-control`: envío manual al Neo de una pendiente o una potencia, con límites, y vuelta a la pendiente 0 % al terminar.
- `diagnostics`: pantalla de Diagnóstico de la app (botones en vivo, tráfico en crudo, control manual) y registro de sesión en memoria.

### Modified Capabilities

- `screen-mockups`: la maqueta de Diagnóstico añade la sección de conexiones y los estados de conexión, límites y sin rodaje; el último gesto sin acción mientras no existan los gestos.
- `ui-components`: el stepper muestra deshabilitado el botón del límite alcanzado; el volcado de tráfico añade la característica, la hora con milésimas y el estado vacío.
- `app-theme`: los iconos del sistema de diseño también son derivados de la app, generados y comprobados como los tokens.

## Impact

- **Código:** `:nucleo` (`protocolo`: `SquareDecoder`, `FecCodec`, `SquareButton`; `dominio`: `EventosBoton`), `:app` (`AppGraph`, `ble/`: `BleScanner`, `SquareClient`, `NeoClient`; `datos/RegistroSesion`; `ui/`: navegación, `Diagnostico` y composables `SquareKey`, `HexLog`, `Stepper`, `ConnectionChip`, `Notice`, `AppBar`, `Button`, `SettingsList`), manifiesto con permisos de Bluetooth y `android.hardware.bluetooth_le`.
- **Sistema de diseño y maquetas:** `components.css` (stepper deshabilitado, columna de característica en HexLog, teclas y aviso del último gesto sin cortes de línea), `maquetas/diagnostico.html`, `maquetas/index.html`; `scripts/design-system/generar.py` emite los iconos como *vector drawables*.
- **Dependencias nuevas:** Nordic Android-BLE-Library, Navigation Compose, `lifecycle-viewmodel-compose` y `kotlinx-coroutines`; las dos primeras ya están en la pila de `arquitectura.md` § 3.
- **Trazas:** ficheros de trazas reales del Square y del Neo en los recursos de test de `:nucleo`.
- **Documentos de `docs/`:** `arquitectura.md` (resultados de § 7.1 y § 7.2, `BleScanner` en § 4.3, ciclo de vida provisional sin servicio, iconos generados en § 9.1), `funcional.md` (decisión abierta 2 resuelta: la palanca 1 es la grande y la 2 la pequeña), riesgo de latencia NF1 por el intervalo de conexión que impone el Square (`arquitectura.md` § 6.3), `planificacion.md` (estado del change, F2.2 y `EventosBoton` pasan del change 4 al 3, y dependencias que cambien según la validación) y `desarrollo.md` (captura de trazas con `logcat` y la opción de checksum de depuración).
