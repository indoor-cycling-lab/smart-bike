# Tasks

Sin secciones «Maqueta» y «App»: la pantalla de prueba es de depuración y desechable, y el usuario acordó al proponer el change que no pase por maqueta (`design.md`, decisión 1). Se valida en el móvil.

## 1. Codificación del mando (`:nucleo`)

- [x] 1.1 `BotonMando` y `TablaBotones` (`PROTO` y `ENUM_MANUAL`, `design.md` decisión 2) en `protocolo/MandoZwiftCodec.kt`, con tests de los 16 valores de cada tabla y de que los siete primeros coinciden; verificar con `./gradlew :nucleo:test`
- [x] 1.2 `MandoZwiftCodec.botones` y `reposo` (varint a mano, máscara invertida) con tests de vectores: ninguno pulsado (`23 08 FF FF FF FF 0F`), cada botón solo en las dos tablas, ← + A a la vez y todos pulsados; verificar con `./gradlew :nucleo:test`
- [x] 1.3 Freno: `CodificacionFreno` (campo 2 o 3, `Location` por freno, valor en zigzag) en `botones`, con tests de vectores de un freno, los dos, freno con un botón pulsado, cada variante de campo y signo, y valores negativos en zigzag; verificar con `./gradlew :nucleo:test`
- [x] 1.4 `esHandshake` y `respuestaHandshake` con sus tres variantes, con tests: `RideOn` pelado, `RideOn 02`, `RideOn 02 00`, trama corta, otra trama que no es handshake y el eco de los bytes de Zwift; verificar con `./gradlew :nucleo:test`

- [x] 1.5 `RespuestaInfo` (ninguna, vacía, con nombre), `peticionInfo` y `respuestaInfo` en `MandoZwiftCodec` (añadido tras la primera prueba con Zwift, `design.md` «Resultados de la validación»), con tests de vectores: `00 08 00`, `00 08 10`, identificador de dos bytes, escrituras que no son petición y cada variante de respuesta; verificar con `./gradlew :nucleo:test`

## 2. Servicio del mando y servidor de prueba (`:app`)

- [x] 2.1 `DirconServer` con el parámetro `nombre` (por defecto `NOMBRE`) y la variante de `ble-service-uuids` con `0xFC82` añadido (`design.md` decisión 4); test en el `DirconServerTest` existente con un anunciador doble que comprueba el nombre y el TXT, y que el de `AppGraph` sigue anunciando «Square Shift»; verificar con `./gradlew :app:testDebugUnitTest`
- [x] 2.2 `ServicioMando` en `app/src/debug/` (`jmlopezdona.squareshift.prototipo`): servicio `…0001…` con sus cinco características, Device Information y Battery sobre `ServidorGatt`; handshake por SYNC_RX con la respuesta elegida, botones solo tras el handshake, *keepalive* cada 5 s y olvido de botones y handshake al perder el cliente (decisión 3); tests JVM en `app/src/testDebug/` con reloj virtual para los escenarios «Antes del handshake», «Dos botones a la vez», «Diez minutos sin tocar nada» y «Zwift se cierra con un botón mantenido» de la spec `zwift-controller`; verificar con `./gradlew :app:testDebugUnitTest`
- [x] 2.3 Configuración «Mando y rodillo por wifi»: `ServidorGatt` con los servicios de un `ZwiftBridge` (comandos anotados y respondidos con éxito, telemetría a 0 cada 250 ms) más los del mando, con un solo Device Information; test JVM de que el descubrimiento devuelve FTMS, Cycling Power, Device Information, el mando y Battery, y de que un `0x11` al punto de control no llega al Neo; verificar con `./gradlew :app:testDebugUnitTest`
- [x] 2.4 Anotar en `RegistroSesion` (origen `Zwift`) cada escritura de Zwift en el mando, cada envío de la app, la variante usada al publicar y el handshake hecho; verificar en el test de 2.2 que el registro contiene las tramas en hexadecimal con su sentido

- [x] 2.5 `ServicioMando` responde por SYNC_TX a la petición de información con la variante elegida, y `ENUM_MANUAL` pasa a ser la tabla de partida; tests en `ServicioMandoTest` de cada variante; verificar con `./gradlew :app:testDebugUnitTest`

## 3. Pantalla de prueba (`:app`, depuración)

- [x] 3.1 `PrototipoMandoViewModel`: configuración y variantes (deshabilitadas mientras está publicado), publicar y parar el `DirconServer` propio con «Square Shift Mando» y su identidad fija, sin wifi → aviso y anuncio de nuevo al volver la red, estado parado / esperando / conectado / handshake hecho, y conjunto de botones pulsados; tests JVM con dobles de `EstadoWifi` y del transporte para «Publicar el mando por wifi», «Parar», «Sin wifi» y «Se pierde el wifi»; verificar con `./gradlew :app:testDebugUnitTest`
- [x] 3.2 `PrototipoMandoActivity` con `SqTema` y composables existentes: elección de configuración y variantes en `SettingsList`, «Publicar» / «Parar», `ConnectionChip` con el estado, `Notice` para los avisos, botones del mando con `SquareKey` que envían pulsado al tocar y suelto al levantar el dedo (varios a la vez con varios dedos), frenos izquierdo y derecho, y `HexLog` con el tráfico del mando; declararla en `app/src/debug/AndroidManifest.xml` como la Muestra del tema; verificar que `./gradlew :app:assembleRelease` no la incluye (`apkanalyzer` o el manifiesto fusionado) y abrirla en el OnePlus 8 con `adb shell am start -n jmlopezdona.squareshift.debug/jmlopezdona.squareshift.prototipo.PrototipoMandoActivity` (skill `desplegar-movil`), comprobando con una captura que no arranca la sesión ni busca el Neo y el Square

- [x] 3.3 Elegir en la pantalla la respuesta a la petición de información; verificar en el OnePlus 8 con una captura

## 4. Validación con Zwift en PC por wifi

- [x] 4.1 Solo mando por wifi (`design.md` decisión 6, paso 1): con la sesión de rodaje cerrada, `dns-sd -B _wahoo-fitness-tnp._tcp` desde el Mac lista «Square Shift Mando» y Zwift lo lista en Mandos; si no, probar las variantes del TXT y de Device Information; anotar en `design.md` qué lo hace aparecer y el descubrimiento que muestra el registro. Si con ninguna variante aparece, saltar a la sección 5
- [x] 4.2 Handshake (paso 2): emparejar y anotar en `design.md` la trama exacta que escribe Zwift en SYNC_RX y la respuesta que lo deja conectado; si ninguna de las tres vale, parar y replantear con el usuario (`/openspec-update-change`)
- [x] 4.3 Botones (paso 3): menú con la cruceta y A/B, giro manteniendo ←/→ en un cruce, Y con un powerup y Z con alguien cerca; si Z o los de después no responden, tabla `ENUM_MANUAL`; diez minutos sin tocar nada; anotar en `design.md` la tabla buena y qué hace en Zwift cada botón (incluidos cambio, powerup y encendido de cada lado)
  - Por wifi, Zwift no aplica los botones (`design.md`, «Resultados de la validación»). Por Bluetooth funcionan menús, A, B y Z (Ride On); Y y ↑/B en rodaje quedan pendientes en la 5.4.
- [x] 4.4 Freno (paso 4): con la sesión de rodaje abierta y el Neo, pedaleando a más de 30 km/h, mantener cada freno 3 s con cada variante hasta que frene el avatar; probar los dos a la vez y comprobar «Braking Sensitivity» en Hardware; anotar en `design.md` la codificación buena o que no se consiguió
  - Hecho por Bluetooth, con el rodillo por wifi: el freno es una palanca analógica (P0 positivo, P1 negativo) y las palancas sueltas tienen que ir a 0. Por el camino salió también la dirección (P0 negativo a la izquierda, P1 positivo a la derecha).
- [x] 4.5 Mando y rodillo por wifi (paso 5): el mismo dispositivo aparece y se empareja en potencia, cadencia, rodillo controlable y mandos, y los botones siguen funcionando; anotar en `design.md`
  - Resultado negativo: el dispositivo se empareja como potencia, cadencia y rodillo, pero no como mando, y los botones no hacen nada. El mando va por Bluetooth (dos dispositivos ante Zwift).
- [x] 4.6 Pérdidas (paso 6): cerrar Zwift con ← mantenido y volver a abrirlo; quitar el wifi del móvil 10 s; comprobar los escenarios de «Conexión con Zwift perdida» y anotar en `design.md`
  - No aplica por wifi, porque el mando no funciona por ahí. La prueba de pérdidas se hace por Bluetooth en la 5.4.

## 5. Bluetooth (solo si Zwift no acepta el mando por wifi)

- [x] 5.1 Decidir con el usuario, con el resultado de 4.1 y 4.2, si se hace esta sección; si no hace falta, marcar 5.2–5.4 como no aplicables con el motivo en `design.md`
- [x] 5.2 `PerifericoMando` en depuración (`design.md` decisión 5): `BluetoothGattServer` que adapta el mismo `ServidorGatt` (escrituras, CCCD, indicación de SYNC_TX con confirmación) y anuncio con `FC82` y el nombre en la respuesta de escaneo; `BLUETOOTH_ADVERTISE` en el manifiesto de depuración, comprobación de `bluetoothLeAdvertiser` y del Bluetooth encendido con sus avisos; verificar en el OnePlus 8 con nRF Connect en el Mac o en otro móvil que se ven el anuncio, el servicio y que una escritura de `RideOn` recibe respuesta
- [x] 5.3 Variantes del anuncio en la pantalla (datos de fabricante `0x094A` + tipo, y nombre del móvil sí o no; `design.md` decisión 5); verificar con nRF Connect que el anuncio cambia con cada variante
- [ ] 5.4 Repetir con Zwift en PC por Bluetooth los pasos 4.1–4.4 y 4.6, y anotar en `design.md` qué nombre y fabricante lo reconocen, el handshake, los botones y el freno
  - **Pendiente** (27-10-2026): reconocimiento, handshake, botones (menús, A, B, Z), freno y dirección, hechos y anotados. Faltan los diez minutos sin tocar nada, cerrar Zwift con algo mantenido, Y y ↑/B en rodaje, y qué variante del anuncio o de la información es imprescindible (`design.md`, «Pendiente al cerrar la sesión de prueba»).

## 6. Documentación

- [x] 6.1 Actualizar la documentación: revisar `docs/arquitectura.md` (§ 7.4 con lo validado y lo que deja de estar «a validar», § 15 decisión 4 resuelta, § 4.3 si cambia el reparto de `MandoZwiftCodec` o `MandoZwift`, § 7.3 si el TXT de DirCon cambia), `docs/funcional.md` (riesgo de F9, F9.5 si el freno no se consigue y, si se descarta F9, el paso a F10), `docs/planificacion.md` (fila y notas del change 12, y dependencias y estado de los changes 11, 13 y 14 según la tabla de resultados de `design.md` decisión 6) y `docs/desarrollo.md` (cómo abrir la pantalla de prueba con `adb shell am start` y el `dns-sd` del mando), y actualizar lo que el change haya cambiado o decidido; verificar que ningún documento sigue presentando como «a validar» algo que este change resolvió
