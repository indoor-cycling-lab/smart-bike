# Tasks

## 1. Dominio: política de clientes, ritmo y lector DirCon

- [x] 1.1 `:core` (`dominio`): `PoliticaClientes` (design.md, decisión 2), que decide `Sustituir` o `Rechazar` a partir del cliente actual (IP e instante de su último mensaje), la IP del nuevo y el instante actual, con `SILENCIO_SUSTITUIBLE_MS` = 15 000. Verificar con su test: sin cliente actual, misma IP, otra IP con el actual activo (14,9 s) y con el actual en silencio (15 s).
- [x] 1.2 `:core` (`dominio`): `LimiteRitmo`, un cubo de fichas con capacidad 100, recarga de 50 por segundo y reloj inyectado (decisión 4). Verificar con su test: una ráfaga de 100 pasa y la 101 no; con 50 por segundo sostenidos nunca se agota; se recupera tras una pausa.
- [x] 1.3 `:core`: `DirconCodec.Lector` con un búfer fijo de `CABECERA + 65 535` bytes e índices (decisión 4). Verificar con los tests actuales del lector y con uno nuevo: un mensaje de 65 535 bytes de contenido entregado de 1 en 1 byte da un solo mensaje correcto; varios mensajes en un bloque; medio mensaje y el resto después.

## 2. DirCon: interfaz, sustitución, vigilancia y ritmo

- [x] 2.1 `DirconServer`: recibir `direccionWifi: () -> String?`, hacer `bind` a esa IPv4, no abrir sin dirección, volver a abrir si `publicar` llega con otra dirección y anotar en el registro la dirección y el puerto en los que escucha (decisión 1). Pasarla desde `AppGraph` (`wifi.direccion.value`). Verificar con `DirconServerTest`: se escucha en la dirección dada (con `127.0.0.1` en el test), sin dirección no se abre, y un `publicar` con otra dirección cierra y vuelve a abrir.
- [x] 2.2 `DirconServer`: aplicar `PoliticaClientes` en `accept` (cerrar el socket rechazado y anotarlo) y guardar el instante del último mensaje de cada cliente. Verificar con `DirconServerTest`: dos clientes desde la misma IP (el segundo sustituye); el segundo rechazado mientras el primero envía mensajes, con el primero aún atendido; y el segundo aceptado tras 15 s de silencio del primero (reloj inyectado).
- [x] 2.3 `DirconServer`: `TCP_USER_TIMEOUT` de 15 s y `keepAlive` en cada socket aceptado, anotando en el registro si no se pudo, más el vigilante de escritura bloqueada (10 s) (decisión 3). Comprobar antes con lint (`NewApi`) la API mínima de `OsConstants.TCP_USER_TIMEOUT` y limitarlo a las versiones que lo admitan. Verificar con un test del vigilante con un `OutputStream` que se bloquea: el socket se cierra y se aplica «Plataforma desconectada».
- [x] 2.4 `DirconServer`: `LimiteRitmo` por cliente (desconectar y anotar al agotarlo) y `try/catch` alrededor de `DirconCodec.atender`, que responde `ERROR_INESPERADO` sin cerrar la conexión. Verificar con `DirconServerTest`: una ráfaga de 200 mensajes desconecta al cliente y la secuencia de conexión de Zwift (unos 30 mensajes) no; un `ServidorGatt` que lanza al escribir devuelve `ERROR_INESPERADO` y la conexión sigue.

## 3. Servidor GATT y anuncio

- [x] 3.1 `GattServerHost` y `Advertiser`: todos los avisos de Android en el hilo de control, con una generación por `publicar` para ignorar los de un servidor ya parado; `alAnunciar` solo desde ese hilo (decisión 5). Verificar con un test de la lógica de generación: un `onServiceAdded` tardío tras `parar()` no anuncia.
- [x] 3.2 Suscripciones por dispositivo y `ColaNotificaciones` con una cola y una confirmación por dispositivo suscrito; cada cola se cierra al desconectarse su dispositivo (decisión 5). Verificar con `ColaNotificacionesTest`: un destino que nunca confirma no retrasa a otro; un dispositivo sin suscripción no recibe nada; al quitar un destino, su cola se cierra.
- [x] 3.3 Escrituras preparadas y `onExecuteWrite` responden `GATT_REQUEST_NOT_SUPPORTED`; una escritura que lanza responde `GATT_FAILURE` y se anota. Verificar con un test de la función que decide la respuesta (preparada, error del servidor, excepción, correcta).
- [x] 3.4 `SecurityException` capturada y anotada en `Advertiser.iniciar`, `GattServerHost.publicar` y `BleScanner` (decisión 7). Verificar compilando y con un test del escáner o del anunciante con un adaptador falso que lanza, si su interfaz lo permite; si no, revisarlo en el código.

## 4. Conexión con el Neo y el Square

- [x] 4.1 `ClienteBle`: comprobar la dirección con `BluetoothAdapter.checkBluetoothAddress` antes de `getRemoteDevice` y devolver `false` anotándolo; el listener de parámetros de conexión salta al hilo de control antes de tocar su estado (decisión 7). Verificar con `SesionTest`: un configurado con una dirección inválida queda «Sin conexión» sin excepción y el otro dispositivo se conecta.
- [x] 4.2 `Sesion.cancelarIntento` y `ClienteBle`: esperar a que termine el intento (`join`), desconectar con un límite de 10 s y, si se agota, cancelar la conexión pendiente y vaciar la cola de Nordic (`cancelPendingConnection()` y `cancelQueue()`, sin cerrar el gestor) antes de seguir (decisión 6). Verificar con `SesionTest` y conexiones falsas: una desconexión que tarda 3 s se espera entera antes del intento nuevo; una que no termina provoca la cancelación forzosa a los 10 s y el intento siguiente se lanza después, nunca antes.

## 5. Limpieza del prototipo

- [x] 5.1 Comprobar con grep que producción no usa cada variante y borrarla con sus tests: `RespuestaHandshake.ECO` y `RIDE_ON_02_00`; `RespuestaInfo.NINGUNA`, `NOMBRE` y `PAGINA_CLICK_V2`; `MandoZwiftCodec.reposo()` y `TablaBotones.PROTO`; `sinPrefijoCorto` y `sinPrefijo` de `DirconServer.registros`; y `CriterioAnuncio.servicio(prefijoNombre)` (decisión 8). Verificar con `./gradlew test :app:testDebugUnitTest ktlintCheck :app:lintRelease`, sin errores de lint.

## 6. Prueba con el equipo real

- [x] 6.1 Desplegar en el OnePlus 8 (skill `desplegar-movil`) y, con Zwift en PC por wifi, emparejar, rodar 10 minutos en ruta, cerrar Zwift y volver. Comprobar en el registro que no hay desconexiones por exceso de mensajes ni rechazos, y que el tiempo de la palanca sigue por debajo de 100 ms (NF1). (Probado en tramos cortos, no 10 min seguidos: conexión de Zwift con 15 mensajes en 250 ms sin exceso de ritmo, sin rechazos con Zwift activo, y palancas de 37 a 77 ms con 9 cambios, por debajo de 100 ms. Aceptado por el usuario.)
- [x] 6.2 Con Zwift rodando por wifi, suspender el PC: la plataforma se da por desconectada en menos de 30 s y, 30 s después, el Neo vuelve a la pendiente de la marcha. Dejar Zwift 10 minutos en los menús: no se desconecta (probado 3 min, acordado con el usuario: los temporizadores son de 10 a 15 s).
- [x] 6.3 Con Zwift conectado por wifi, conectar desde otro equipo de la red (por ejemplo, `nc <ip-del-móvil> 36866` desde el Mac): queda rechazado en el registro y Zwift sigue. Con los datos móviles activos, comprobar en el registro que el servidor escucha en la IP de la wifi y no en todas las interfaces. (Probado: con Zwift rodando se rechaza al extraño; con Zwift en los menús, más de 15 s sin mensajes, un equipo de la wifi lo sustituye, como prevé la decisión 2. Escucha en la IP de la wifi según el registro; sin probar con datos móviles.)
- [x] 6.4 Con «Solo Bluetooth» y Zwift en PC: emparejar, rodar y usar el controlador Zwift; cambiar a «Solo wifi» durante el anuncio y comprobar que Zwift deja de listarla por Bluetooth. (Probado: con «Solo Bluetooth» Zwift conecta rodillo y controlador, y las pulsaciones llegan; al cambiar a wifi el registro anota «BLE: parado» y el anuncio mDNS vuelve, por lo que deja de anunciarse por Bluetooth.)
- [x] 6.5 Con el Square configurado y dormido, apagar y encender el Bluetooth y despertarlo: conecta sin reiniciar la app. Repetir con «Olvidar» y «Buscar» (lo que falló en el bloque 1). Anotar si el registro llega alguna vez a la cancelación forzosa de Nordic y comprobar que el siguiente intento conecta.

## 7. Documentación

- [x] 7.1 Actualizar la documentación: revisar `docs/funcional.md` (NF5: DirCon solo en la wifi), `docs/arquitectura.md` (§ 7.3: interfaz, sustitución de cliente, vigilancia y ritmo de DirCon; § 7.4: suscripciones por dispositivo y escrituras del servidor GATT; lo validado en la sección 6), `docs/planificacion.md` (fila del change 32 y su nota) y `docs/desarrollo.md`, y actualizar lo que haya cambiado. Marcar en `docs/revision/2026-10-29-auditoria-produccion.md` el estado de A1, A2, A3, A6, M8–M11 y B1–B3 y de las variantes del prototipo.
