## Why

El mando de Zwift emulado (F9) es lo que permite dejar de emparejar el Square en Zwift y que Zwift vea un solo dispositivo (F9.2), pero depende de un protocolo de Zwift no documentado. `arquitectura.md` § 7.4 deja **a validar** la respuesta al handshake, la máscara de botones a partir de `Z`, cómo se codifica el freno y qué hace que Zwift reconozca el dispositivo como mando, y § 15 deja abierta la decisión 4: si Zwift acepta el mando por DirCon o solo por Bluetooth. Este change es el prototipo aislado de la fase 4 (`planificacion.md`, change 12): resuelve esas incógnitas con Zwift real antes de comprometer la integración con el Square (change 13) o de pasar al teclado Bluetooth (F10, change 14).

## What Changes

- Pantalla de prueba del mando, **solo en la compilación de depuración**, que se abre con `adb shell am start` como la Muestra del tema y no arranca la sesión de rodaje ni toca el Neo ni el Square.
- La pantalla presenta la app ante Zwift como un mando Zwift Ride y envía los botones que se mantienen pulsados en pantalla: cruceta, A, B, Y, Z, powerup, encendido y cambio de cada lado, y el freno izquierdo y derecho. Varios a la vez se envían juntos.
- Tres configuraciones que se eligen en la pantalla antes de publicar:
  - **Solo mando por wifi** (DirCon), con el nombre «Square Shift Mando».
  - **Mando y rodillo por wifi**: el mando en el mismo servidor que los servicios de rodillo, potencia y cadencia del puente, con potencia y cadencia 0, para comprobar que Zwift empareja los dos en un solo dispositivo.
  - **Solo mando por Bluetooth**, como periférico BLE: solo si Zwift no acepta el mando por wifi.
- Variantes experimentales elegibles en la pantalla para lo que las fuentes no aclaran: respuesta al handshake, valores de la máscara a partir de `Z`, codificación del freno (campo, ubicación y valor) y, por Bluetooth, nombre y datos de fabricante del anuncio.
- Estado visible de la conexión con Zwift (parado, esperando, conectado, handshake hecho) y tráfico del mando en el registro de sesión, para leerlo desde el Mac.
- Sesión de validación guiada con Zwift en PC que deja resuelto, con el resultado, lo marcado **a validar** en `arquitectura.md` § 7.4 y la decisión abierta 4 de § 15.
- `MandoZwiftCodec` en `:nucleo`, con tests de vectores, para que el change 13 lo reutilice. `DirconServer` admite otro nombre de anuncio.

Fuera de alcance:

- Botones del Square hacia Zwift, acciones `Zwift(…)` en la asignación y reenvío de pulsar y soltar desde `ReconocedorGestos` (change 13).
- El mando dentro de la sesión de rodaje y del servidor DirCon de la app: aquí va en una instancia propia, aparte de la `Sesion`.
- El puente FTMS por Bluetooth y su elección en Ajustes (F8.5, change 11). Por Bluetooth solo se prueba el mando, y solo si falla el wifi.
- Teclado Bluetooth (F10, change 14): solo si este change descarta el mando emulado.
- Emular un Zwift Hub para que Zwift muestre las marchas (arquitectura B de las referencias).
- Zwift en Apple TV y en Mac: la validación se hace en PC (`funcional.md`, decisión 7).
- Maqueta: la pantalla es una herramienta de prueba desechable, solo de depuración; se valida en el móvil (decisión del usuario al proponer el change).

## Capabilities

### New Capabilities

- `zwift-controller`: la app como mando de Zwift: reconocimiento en Zwift, handshake, botones mantenidos, varios a la vez, freno y pérdida de la conexión. En este change, accionado desde la pantalla de prueba de depuración; el change 13 lo conecta a los botones del Square.

### Modified Capabilities

Ninguna. El puente con Zwift (`zwift-bridge`) no cambia: el servidor DirCon de la sesión sigue igual y la configuración «mando y rodillo» vive solo en la pantalla de prueba.

## Impact

- **Código:** `:nucleo` (`protocolo/MandoZwiftCodec`, con tests); `:app` main (`DirconServer` con nombre de anuncio configurable, sin cambiar el de la sesión); `:app` debug (`PrototipoMandoActivity`, su `ViewModel`, el servicio del mando sobre `ServidorGatt` y, si hace falta, un periférico BLE mínimo con `BluetoothGattServer` y `BluetoothLeAdvertiser`); manifiesto de depuración con la actividad y, si se llega a Bluetooth, `BLUETOOTH_ADVERTISE`.
- **Dependencias nuevas:** ninguna.
- **Sistema de diseño y maquetas:** ninguno; la pantalla usa componentes `sq-*` existentes.
- **Documentos de `docs/`:** `arquitectura.md` (§ 7.4 con lo validado, § 15 decisión 4 resuelta, § 4.3 si cambia el reparto de `MandoZwiftCodec` o `MandoZwift`), `funcional.md` (riesgo de F9 y, si se descarta, paso a F10), `planificacion.md` (estado del change 12 y dependencias y estado de los changes 11, 13 y 14 según el resultado) y `desarrollo.md` (cómo abrir la pantalla de prueba).
