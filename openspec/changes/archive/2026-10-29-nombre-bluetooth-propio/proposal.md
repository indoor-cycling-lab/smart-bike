# Proposal

## Why

Por Bluetooth, la app de entrenamiento ve la app con el nombre Bluetooth del móvil («OnePlus 8»), no con «Smart Bike». Pasa con «Solo Bluetooth» (rodillo, pulso y controlador) y con el controlador Zwift en «Wifi + Bluetooth». Android no deja anunciar otro nombre: solo incluye o quita el del adaptador. La única forma de que Zwift muestre el nombre elegido es cambiar el nombre Bluetooth del móvil (`BluetoothAdapter.setName`), y eso afecta a todo el teléfono. Por eso se propone como ajuste opcional, limitado al tiempo en que la app publica por Bluetooth y que devuelve el nombre original al terminar (F8.5, F9.2, F12.9).

## What Changes

- Nuevo ajuste en Conexión, «Usar el nombre también por Bluetooth», desactivado por defecto. Se muestra en los modos que publican algo por Bluetooth («Wifi + Bluetooth» y «Solo Bluetooth»), no en «Solo wifi». Su ayuda avisa de que, mientras la app publica por Bluetooth, el móvil se llama así para todos los dispositivos (coche, auriculares, otros móviles) y de que al terminar recupera su nombre.
- Con el ajuste activado, el nombre Bluetooth del móvil pasa a ser el de «Nombre ante la app» («Smart Bike» o «KICKR BIKE PRO» con sus cuatro caracteres) mientras la app se anuncia por Bluetooth. Al dejar de anunciarse (terminar la sesión, cambiar a «Solo wifi», olvidar el Square en «Wifi + Bluetooth» o desactivar el ajuste), el móvil recupera el nombre que tenía.
- «Nombre ante la app» se muestra también con «Solo Bluetooth» cuando el ajuste está activado. Sin el ajuste, se sigue usando el nombre del móvil, como hasta ahora.
- Si la app se cierra de golpe con el móvil renombrado, el nombre original se recupera la siguiente vez que se abre la app. Si entretanto el usuario ha renombrado el móvil, la app no toca su nombre.
- Si no se puede renombrar (Bluetooth apagado, Android lo rechaza), la app se anuncia con el nombre del móvil y Conexión lo dice. Lo vuelve a intentar al encender el Bluetooth.
- Las ayudas de emparejamiento de Conexión nombran el nombre que verá la app de entrenamiento: el elegido con el ajuste activado y el del móvil sin él.
- El ajuste se guarda en la configuración y entra en la copia de seguridad (F12.6). El nombre original del móvil no se exporta.

**Fuera de alcance:**
- Anunciar un nombre propio sin renombrar el móvil: Android no lo permite.
- Un nombre de texto libre: se reutilizan los de «Nombre ante la app».
- Que el móvil se quede renombrado fuera de la sesión (renombrado permanente).
- Una dirección Bluetooth estable para que Zwift reconozca el dispositivo entre sesiones (sigue siendo una limitación de «Solo Bluetooth»).
- Cambios en DirCon: por wifi el nombre ya es el elegido.

## Capabilities

### New Capabilities
- `bluetooth-name`: renombrado temporal del Bluetooth del móvil mientras la app publica por Bluetooth: cuándo se renombra, cómo y cuándo se recupera el nombre original (también tras un cierre inesperado), y qué pasa si no se puede renombrar.

### Modified Capabilities
- `platform-settings`: «Nombre ante la app» también con «Solo Bluetooth» con el ajuste activado; nuevo ajuste «Usar el nombre también por Bluetooth»; las ayudas del rodillo y del controlador nombran el nombre efectivo por Bluetooth.
- `ftms-bluetooth`: «Nombre del móvil ante la plataforma» deja de ser fijo: con el ajuste activado, Zwift ve el nombre elegido.
- `platform-bridge`: «Nombre ante la plataforma» con «Solo Bluetooth» usa el nombre elegido si el ajuste está activado.
- `screen-mockups`: la maqueta de Conexión muestra «Nombre ante la app» y el ajuste según el modo, con estados nuevos (ajuste activado con «Solo Bluetooth» y no se pudo renombrar).

## Impact

- `:core`: `Configuracion` (`AjustesConexion` gana el ajuste, sin cambio de versión ni migración, opcional como `bajadas`); una función pura que decide el nombre Bluetooth deseado según el modo, lo publicado y el ajuste.
- `:app`: nuevo `RenombradorBluetooth` (`ble/`) sobre `BluetoothAdapter.setName` y `ACTION_LOCAL_NAME_CHANGED`; `Advertiser`/`GattServerHost` esperan al nombre nuevo antes de anunciarse; `estado.json` guarda el nombre original pendiente de recuperar; `Sesion`/`AppGraph` lo atan a la publicación por Bluetooth y lo recuperan al abrir la app; `ui/conexion` (ajuste, ayudas, aviso de fallo) y `strings.xml`.
- `maquetas/conexion.html` y `maquetas/index.html` (estados nuevos). Sin componentes nuevos: el ajuste usa el interruptor existente del sistema de diseño.
- Sin permisos nuevos: `BLUETOOTH_CONNECT` (Android 12+) y `BLUETOOTH_ADMIN` (Android 11 o inferior) ya están en el manifiesto.
- Documentos: `docs/funcional.md` (F8.5, F12.9), `docs/arquitectura.md` (§ 7.3 transporte por Bluetooth, § 10 persistencia), `docs/planificacion.md` (fila 31). `docs/desarrollo.md`: ninguno.
