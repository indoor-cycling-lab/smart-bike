# Design

## Context

Los hallazgos salen de `docs/revision/2026-10-29-auditoria-produccion.md` (motivo en `proposal.md`). El estado actual que condiciona el diseño:

- **`DirconServer`** abre un `ServerSocket` con `bind(InetSocketAddress(puerto))`, que escucha en `::`, es decir, en todas las interfaces.
  - Atiende un cliente cada vez: el que llega cierra al anterior.
  - Lee con `DirconCodec.Lector`, que concatena el búfer en cada `read`.
  - El socket no tiene timeout ni keepalive.
  - `publicar()` no hace nada si ya está publicado, así que un cambio de dirección de la wifi no vuelve a abrir el socket.
- **Dirección de la wifi.** `PuentePlataforma.wifiCambia(direccion)` la recibe de `AdaptadorWifi.direccion` y vuelve a llamar a `publicar()`.
- **Zwift** (arquitectura.md § 7.3) manda `11` cada pocos segundos solo mientras rueda. En los menús puede pasar mucho tiempo sin enviar nada. Por eso el silencio de Zwift no sirve para saber si sigue vivo.
- **`GattServerHost`** recibe los avisos de Android en hilos binder.
  - `onServiceAdded` toca `gatt`, `pendientes` y el `Advertiser` sin pasar por el hilo de control.
  - `ColaNotificaciones` envía cada notificación en serie a todos los que hayan usado el servidor, con una sola confirmación compartida y hasta 500 ms de espera por destino.
  - Las suscripciones de `ServidorGatt` son comunes a todos los clientes. Es a propósito: gobiernan si se genera la telemetría.
- **`ClienteBle`** (Nordic) llama a `getRemoteDevice` fuera del `try`. `setConnectionParametersListener` escribe estado desde el hilo de Nordic.
- **`Sesion.cancelarIntento`** cancela el `Job` del intento y espera a `desconectar()` sin límite.
  - En el bloque 1 se le puso un límite de 1,5 s. En el OnePlus 8 el Square dejó de conectar: el intento nuevo entraba en la cola de Nordic detrás de la desconexión pendiente.
  - Sin límite funciona. El riesgo de que la espera no termine nunca está anotado en `Sesion.terminar()`, que sí mantiene su límite porque después ya no se conecta nada.

## Goals / Non-Goals

**Goals:**
- **Política de clientes y ritmo en `:core`.** Que la política de sustitución de cliente y el límite de ritmo sean funciones puras, con tests y sin sockets.
- **Sin cambios en `ServidorGatt` ni en `PuenteFtms`.** Lo nuevo de DirCon y del GATT queda en los transportes (`DirconServer`, `GattServerHost`).
- **Cada decisión que dependa del comportamiento real tiene su prueba en el OnePlus 8.** Son Zwift, la pila TCP de Android y Nordic.

**Non-Goals:**
- **Detectar la caída de Zwift por silencio.** Zwift puede callar en los menús, así que solo se usa el silencio para decidir si un cliente nuevo puede sustituir al actual.
- **Cambiar la telemetría o el formato de DirCon.**

## Decisions

### 1. DirCon escucha en la IPv4 de la wifi

`DirconServer` recibe `direccionWifi: () -> String?` (como `SondaMdns`) y abre el `ServerSocket` con `bind(InetSocketAddress(InetAddress.getByName(dir), puerto))`.

- **Sin dirección**, no se abre. `PuentePlataforma` ya retira la conexión sin wifi.
- **Con otra dirección.** `publicar()` recuerda la dirección con la que abrió. Si `wifiCambia` lo vuelve a llamar con otra dirección, hace `parar()` y vuelve a publicar. El `join` del trabajo anterior ya ordena el cierre y la apertura.

Alternativa: filtrar en `accept()` las conexiones que no vienen de la subred de la wifi. Se descarta porque el puerto seguiría abierto en las demás interfaces, y NF5 pide que no se llegue.

### 2. Sustitución de cliente con condiciones

`PoliticaClientes` (`:core`, paquete `dominio`) es una función pura. Recibe el cliente actual (IP e instante de su último mensaje), la IP del nuevo y el instante actual, y decide:

- **`Sustituir`** si no hay cliente actual, si el nuevo viene de la misma IP o si el actual lleva más de `SILENCIO_SUSTITUIBLE_MS` = 15 000 ms sin enviar nada.
- **`Rechazar`** en otro caso. El socket nuevo se cierra enseguida y el registro anota «DirCon: rechazado <ip>, la plataforma de <ip> sigue activa».

La IP se compara como texto. Zwift reconecta desde la misma IPv4. Los 15 s cubren el caso de otro equipo cuando el primero se quedó colgado en un momento en que la app no envía telemetría.

Alternativas:
- **Rechazar siempre mientras haya cliente.** Zwift no podría volver tras un corte de wifi hasta que se detectara el socket medio abierto.
- **El último gana, como hasta ahora.** Lo descartó el usuario: cualquiera de la red tomaría el control del Neo.

### 3. Cliente desaparecido: `TCP_USER_TIMEOUT` y vigilante de escritura

- **`TCP_USER_TIMEOUT`.** Cada socket aceptado recibe `TCP_USER_TIMEOUT` = 15 000 ms (`Os.setsockoptInt` sobre el descriptor de `ParcelFileDescriptor.fromSocket`) y `keepAlive = true`.
  - Mientras la app envía telemetría (4 Hz con Zwift suscrito), si el otro extremo deja de acusar recibo, el kernel cierra la conexión a los 15 s. `read()` falla y se aplica el camino actual de «Plataforma desconectada».
  - Si el sistema no admite la opción, se anota en el registro y queda el vigilante.
- **Vigilante de escritura.** El escritor marca el instante en que empieza cada `write`. Una corrutina comprueba cada segundo que ningún `write` lleve más de 10 s bloqueado y, si lo lleva, cierra el socket. Cubre el caso en que el búfer de envío se llena antes de que actúe el kernel.

Sin telemetría (Zwift en los menús, sin suscribirse), una conexión muerta no se detecta. Es aceptable: el Neo no está bajo control de la plataforma, y la decisión 2 deja que cualquier cliente nuevo la sustituya a los 15 s de silencio.

Alternativas:
- **Timeout de lectura por silencio.** Desconectaría a Zwift en los menús (spec: «Plataforma en los menús sin enviar nada»).
- **`TCP_KEEPIDLE`.** No está en `OsConstants` de Android.

**A validar en el OnePlus 8:** con Zwift en PC rodando por wifi, suspender el PC y comprobar en el registro que la plataforma se da por desconectada en menos de 30 s. También, que en los menús no se desconecta en 10 minutos. Antes de usar `TCP_USER_TIMEOUT`, comprobar con lint (`NewApi`) la API mínima de la constante; si es mayor que 28, por debajo solo queda el vigilante.

### 4. Lector con búfer fijo y límite de ritmo

- **Lector.** `DirconCodec.Lector` pasa a un `ByteArray` fijo de `CABECERA + 65 535` bytes con un índice de escritura. Cada `anadir` copia una vez, extrae los mensajes completos y compacta lo que sobra con un único `copyInto`. El coste por byte es constante.
- **Límite de ritmo.** `LimiteRitmo` (`:core`, paquete `dominio`) es un cubo de fichas: `CAPACIDAD` = 100 mensajes y `RECARGA` = 50 mensajes por segundo, con el reloj inyectado.
  - Zwift manda unos 30 mensajes de golpe al conectarse (descubrir, leer, suscribir) y después pocos por segundo, así que queda muy por debajo.
  - El cliente que agota el cubo se desconecta, con el registro «DirCon: <ip> desconectado por exceso de mensajes».
  - **A validar:** que una sesión de Zwift de una hora no lo dispara. La tarea de prueba lo mira en el registro.
- **Errores al atender.** `DirconCodec.atender` se envuelve en `try/catch`. Una excepción responde `ERROR_INESPERADO`, se anota y no cierra la conexión.

### 5. Servidor GATT: hilo de control, suscripciones por dispositivo y escrituras

- **Hilo de control.** Todos los avisos de `BluetoothGattServerCallback` y del `Advertiser` saltan a `hilo` (`ambito.launch(hilo)`), igual que lecturas y escrituras.
  - `onServiceAdded` comprueba que el `gatt` del aviso es el actual (una «generación» que se incrementa en cada `publicar`) antes de añadir el siguiente servicio o de anunciar. Si llega tras un `parar()`, no anuncia.
  - `Advertiser.alAnunciar` se toca solo desde ese hilo.
- **Suscripciones por dispositivo.** `GattServerHost` guarda las suscripciones de cada dispositivo (CCCD por característica), además de llamar a `ServidorGatt.suscribir` como ahora.
  - `ColaNotificaciones` pasa a una cola por dispositivo suscrito, cada una con su propia confirmación (`onNotificationSent(device)`) y su espera de 500 ms. Un dispositivo lento solo se retrasa a sí mismo.
  - Al desconectarse un dispositivo, su cola se cierra.
  - La alternativa de una cola única con espera más corta se descarta: no resuelve que un cliente lento retrase al resto.
- **Escrituras preparadas.** Con `preparedWrite = true` se responde `GATT_REQUEST_NOT_SUPPORTED` sin atender el valor. `onExecuteWrite` responde lo mismo. Zwift no usa escrituras largas: los comandos FTMS caben en 20 bytes.
- **Escritura que falla.** Si `servidor.escribir` lanza, se responde `GATT_FAILURE` (si `responseNeeded`) y se anota en el registro.

### 6. Cancelar un intento de conexión sin dejar dos en la cola de Nordic

`Sesion.cancelarIntento(d)` sigue esperando a que termine la desconexión, que es lo que funciona. Antes:

1. Cancela el `Job` del intento y espera a que termine (`join`), para que la petición `connect` de Nordic se haya cancelado o completado.
2. Llama a `desconectar()` con un límite largo, `LIMITE_CANCELAR_MS` = 10 000 ms.
3. Si se pasa del límite, pide a `ClienteBle` que cancele la conexión pendiente y vacíe la cola: `ConnectRequest.cancelPendingConnection()` sobre la petición de conexión en curso, que `ClienteBle` guarda, y `cancelQueue()`. Lo anota y sigue. El gestor sigue siendo el mismo.

**Descartado: cerrar el gestor con `close()`.** Probado en el OnePlus 8 el 29-10-2026: tras `close()` el gestor queda inservible. Las conexiones siguientes (directa o en segundo plano) se lanzan, pero Nordic no emite nada, ni siquiera el fallo por tiempo, y el Square se queda en «Conectando» hasta reiniciar la app.

La diferencia con lo que falló: nunca se lanza un intento nuevo con una petición pendiente en la cola. O la desconexión termina, o la cola se vacía sin cerrar el gestor.

**A validar en el OnePlus 8:**
- Con el Square configurado y dormido, apagar y encender el Bluetooth y despertarlo. Debe conectar (spec «Bluetooth apagado con el Square dormido»).
- Repetir con «Olvidar» y «Buscar», que fue lo que falló en el bloque 1.
- En el registro, ver si alguna vez se llega a la cancelación forzosa y comprobar que el intento siguiente conecta.

### 7. Prioridad de conexión, dirección inválida y permisos

- **Prioridad de conexión.** El listener de parámetros de conexión de `ClienteBle` salta al ámbito de la app, que corre en el hilo de control, antes de tocar `intervaloMs` o lanzar la repetición.
- **Dirección inválida.** `conectar` comprueba `BluetoothAdapter.checkBluetoothAddress(direccion)`. Si no es válida, anota «Dirección inválida: <dir>» y devuelve `false`. La `Sesion` ya deja el dispositivo en «Sin conexión» cuando falla también la conexión en segundo plano, que con la misma comprobación falla al instante.
- **Permisos.** `Advertiser.iniciar`, `GattServerHost.publicar` (`openGattServer`) y `BleScanner` (`startScan`) capturan `SecurityException`, la anotan en el registro y dejan el estado como si el Bluetooth no estuviera disponible.

### 8. Limpieza de las variantes del prototipo

Se borran las variantes que solo usan los tests, con sus tests. Antes de cada borrado se comprueba con grep que producción no las usa:

- `RespuestaHandshake.ECO` y `RIDE_ON_02_00`.
- `RespuestaInfo.NINGUNA`, `NOMBRE` y `PAGINA_CLICK_V2`.
- `MandoZwiftCodec.reposo()` y `TablaBotones.PROTO`.
- El parámetro `sinPrefijoCorto` de `DirconServer` y `sinPrefijo` de `registros()`.
- `CriterioAnuncio.servicio(prefijoNombre)`.

Lo validado por el change 13e (sin variantes) queda como única forma.

## Risks / Trade-offs

- **[Zwift reconecta desde otra IP tras un corte, por ejemplo con DHCP nuevo] → rechazado hasta 15 s.** Se acepta porque la sustitución por silencio lo permite enseguida, y en ruta Zwift habla cada pocos segundos, así que el cliente viejo deja de hablar al cortarse.
- **[`TCP_USER_TIMEOUT` no se aplica en algún Android] → la caída solo se detecta con el vigilante de escritura, mucho más tarde.** Se anota en el registro al conectar y se valida en el OnePlus 8.
- **[El límite de ritmo desconecta a Zwift en un pico legítimo] → se pierde el puente unos segundos.** La capacidad de 100 duplica la ráfaga observada al conectar. El registro deja ver si pasa y los valores se ajustan.
- **[Cancelar la conexión pendiente de Nordic en mitad de algo] → se pierde una conexión que estaba a punto de completarse.** Solo ocurre tras 10 s de espera, y el intento siguiente vuelve a conectar.
- **[`cancelPendingConnection()` no libera una conexión directa a medias] → el Square sigue sin reengancharse.** Se valida en el OnePlus 8; si no basta, habrá que recrear el cliente BLE entero en lugar de reutilizar el gestor.
- **[Colas por dispositivo] → más corrutinas.** Solo una por dispositivo suscrito, y en la práctica es uno.

## Migration Plan

No hay datos ni configuración que migrar. El cambio de comportamiento visible es la sustitución de cliente en DirCon. Para volver atrás basta con revertir el change.

## Open Questions

- ¿Usa producción los frenos del controlador (`FrenoMando`, `CodificacionFreno`)? La spec `zwift-controller` los describe y la auditoría dice que se pasan siempre vacíos. No afecta a este change: se revisa aparte.
