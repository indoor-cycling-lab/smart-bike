# Proposal

## Why

La auditoría de producción (`docs/revision/2026-10-29-auditoria-produccion.md`) encontró que el servidor DirCon y el servidor GATT confían en cualquiera que llegue por la red o por Bluetooth. DirCon escucha en todas las interfaces, no solo en la wifi, y eso contradice NF5. Cualquier equipo de la wifi puede echar a Zwift y tomar el control del Neo. Un Zwift que desaparece sin cerrar el socket (PC suspendido, wifi caída) nunca se da por perdido, así que el Neo se queda con la última resistencia indefinidamente, no los 30 s de «Plataforma desconectada». Varios fallos de concurrencia y de robustez en la parte BLE pueden dejar anuncios fantasma, retrasar las pulsaciones del controlador (NF1) o cerrar la app.

El bloque 1 ya quitó lo que cerraba la app con un solo paquete. Este change cierra el resto antes de considerar la app productiva. Además recupera A6, el arreglo que se probó y se deshizo en el OnePlus 8: hay que cancelar el intento de conexión pendiente sin dejar un segundo intento en la cola de Nordic.

## What Changes

- **DirCon solo en la wifi (A1).** El servidor TCP escucha en la dirección IPv4 de la wifi, no en todas las interfaces. Si cambia la wifi o su dirección, se vuelve a abrir en la nueva.
- **Sustitución de cliente con condiciones (A2).** Un cliente nuevo sustituye al conectado solo si viene de la misma dirección (Zwift que vuelve tras un corte) o si el actual lleva un tiempo sin enviar nada. Si no, se rechaza y se anota en el registro. **BREAKING** respecto a la spec actual, en la que siempre gana el último en conectarse.
- **Cliente desaparecido (A3).** Si Zwift deja de responder sin cerrar la conexión, la app la da por perdida y se aplica «Plataforma desconectada» (30 s y vuelta a la pendiente base), en lugar de mantener la última resistencia indefinidamente.
- **Lectura y ritmo de DirCon (M10).** Los mensajes troceados se reensamblan sin coste cuadrático. Un cliente que envía mensajes por encima de un ritmo razonable se desconecta y se anota en el registro, para que no retrase los cambios de marcha.
- **Notificaciones BLE por suscripción (M8).** Cada notificación se envía solo a los clientes suscritos a esa característica. Un cliente lento ya no retrasa las pulsaciones del controlador a los demás.
- **Servidor GATT ordenado (M9, M11 y escrituras fallidas).** Los avisos de Android se atienden en el hilo de control, así que no quedan anuncios después de parar. Las escrituras largas (preparadas) se rechazan en lugar de interpretarse a trozos. Una escritura que falla recibe siempre respuesta, para que el cliente no se quede esperando.
- **Conexión con el Neo y el Square (A6, B1, B2, B3).** Cancelar un intento de conexión descarta de verdad la petición pendiente de Nordic antes de seguir, sin abandonar la espera (lo probado y deshecho en el bloque 1). Se ordenan los avisos de parámetros de conexión. Una dirección guardada inválida no cierra la app. Si el sistema retira un permiso, las llamadas BLE lo anotan en el registro en lugar de lanzar una excepción.
- **Limpieza.** Se borran las variantes del prototipo que producción no usa: `RespuestaHandshake.ECO` y `RIDE_ON_02_00`; `RespuestaInfo.NINGUNA`, `NOMBRE` y `PAGINA_CLICK_V2`; `MandoZwiftCodec.reposo()` y `TablaBotones.PROTO`; `sinPrefijoCorto`; y `CriterioAnuncio.servicio(prefijoNombre)`. El change 13e ya descartó Click v2 y el saludo `02 00`.

**Fuera de alcance:**

- **Exigir «Pedir control» (FTMS 0x00) antes de aceptar pendiente o potencia.** Se decidió no incluirlo para no arriesgar que el Neo deje de responder si Zwift no lo envía.
- **Emparejamiento o cifrado en el servidor GATT.** Un rodillo FTMS real tampoco los pide y Zwift podría no emparejar.
- **Autenticación en DirCon.** El protocolo no la tiene; la protección es la red local y la política de sustitución.
- **Los frenos del controlador** (`FrenoMando`, `CodificacionFreno`). La spec `zwift-controller` los recoge y queda por aclarar si producción los usa. Se revisan aparte.
- **La sesión que termina al reabrir la app (A4)** y lo demás de la auditoría: R8, firma, copia de seguridad, rotación de sesiones y rendimiento de la UI. Van en otros changes.

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

- `platform-bridge`:
  - «Plataforma desconectada»: el escenario «Otro equipo de la plataforma» pasa a la sustitución con condiciones, y se añade el caso de Zwift que deja de responder sin cerrar la conexión.
  - Nuevo requisito: la plataforma solo se alcanza por la wifi.
  - Nuevo requisito: un cliente que inunda de mensajes se desconecta.
- `ftms-bluetooth`:
  - «Un solo cliente por Bluetooth»: las notificaciones solo llegan a quien está suscrito, y un cliente lento no retrasa a los demás.
  - Nuevo escenario para las escrituras que no se admiten.
- `device-connection`:
  - «Bluetooth apagado»: el escenario de apagarlo con un dispositivo dormido en espera de conexión, que al encenderlo debe conectarse solo.
  - Nuevo escenario para un dispositivo configurado con una dirección inválida.

## Impact

- **`:core`:**
  - `DirconCodec.Lector`: búfer con índices.
  - `MandoZwiftCodec`, `TablaBotones` y `BusquedaAnuncios`: borrar las variantes del prototipo, con sus tests.
  - Una política pura de sustitución de cliente y de límite de ritmo, para probarla sin sockets.
- **`:app`:**
  - `plataforma/DirconServer`: bind a la wifi, política de sustitución, vigilancia del cliente y límite de ritmo.
  - `AppGraph`: pasa la dirección de la wifi al servidor DirCon.
  - `ble/GattServerHost` y `ble/Advertiser`: callbacks en el hilo de control, suscripciones por dispositivo, escrituras preparadas y respuesta ante fallo.
  - `ble/ClienteBle`: cancelación del intento, prioridad de conexión y dirección inválida.
  - `ble/BleScanner`: `SecurityException`.
  - `sesion/Sesion`: `cancelarIntento`.
- **Sin permisos ni dependencias nuevas.**
- **Prueba con el equipo real:** Zwift en PC por wifi y por Bluetooth (emparejar, rodar, cerrar Zwift y volver, suspender el PC) y Bluetooth apagado y encendido con el Square dormido.
- **Documentos:**
  - `docs/arquitectura.md`: § 7.3 (puente DirCon: interfaz, sustitución, vigilancia y ritmo) y § 7.4 (servidor GATT: suscripciones por dispositivo y escrituras).
  - `docs/planificacion.md`: fila nueva del change 32.
  - `docs/funcional.md`: NF5, para precisar que DirCon solo escucha en la wifi.
  - `docs/revision/2026-10-29-auditoria-produccion.md`: estado de los hallazgos.
  - `docs/desarrollo.md`: ninguno.
