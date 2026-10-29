## Context

Tras el change 6 (`puente-zwift-wifi`), la app se presenta ante Zwift por DirCon con un modelo de servidor independiente del transporte:

- `ServidorGatt` (`:nucleo`) guarda servicios y características, atiende lecturas, escrituras y suscripciones y decide qué se notifica; trata las indicaciones como notificaciones, como DirCon. `DirconCodec` usa UUID de 128 bits, así que admite servicios propios como el del mando.
- `DirconServer` (`:app`) publica un `ServidorGatt` con un anuncio mDNS de nombre fijo (`NOMBRE = "Square Shift"`) y registros TXT generados a partir de los servicios (`ble-service-uuids`); un solo cliente, y uno nuevo sustituye al anterior. `AnuncioNsd` hace el anuncio y `AdaptadorWifi` sigue la red.
- `ZwiftBridge` monta FTMS, Cycling Power y Device Information sobre un `ServidorGatt` y recibe los comandos del punto de control en un callback.
- La `Sesion` crea su `ZwiftBridge` y publica el `DirconServer` de `AppGraph` cuando hay wifi; el `RegistroSesion` tiene el origen `Zwift` y, en depuración, se refleja en `logcat` con la etiqueta `SqTrama`.
- La compilación de depuración ya tiene una actividad propia que no es *launcher*, `MuestraTemaActivity`, abierta con `adb shell am start`.
- No hay servidor GATT nativo ni anuncio BLE: llegan con el change 11.

Lo que se sabe del mando Zwift Ride y lo que falta por validar está en `arquitectura.md` § 7.4. Motivo y alcance: `proposal.md`. Comportamiento: la spec `zwift-controller` de este change.

## Goals / Non-Goals

**Goals:**

- Responder con Zwift en PC, por este orden: si acepta el mando por DirCon, si lo acepta en el mismo dispositivo que el rodillo y, solo si no, si lo acepta por Bluetooth (decisión abierta 4 de `arquitectura.md` § 15).
- Fijar el handshake, la máscara a partir de `Z`, el freno y lo que hace que Zwift reconozca el mando, cambiando variantes en la pantalla sin recompilar.
- Dejar en `:nucleo` la codificación del mando probada con vectores y con lo que muestre Zwift, para que el change 13 la use tal cual.

**Non-Goals:**

- Integrar el mando en la `Sesion`, en el `DirconServer` de `AppGraph` o en la asignación (change 13).
- Un servidor GATT nativo general ni la elección de transporte en Ajustes (change 11). El periférico BLE de este change solo sirve el mando y vive en la compilación de depuración.
- Calidad de producto en la pantalla de prueba: es una herramienta desechable, sin maqueta ni tests de Compose.

## Decisions

### 1. Actividad de depuración con su propio servidor, aparte de la `Sesion`

`PrototipoMandoActivity` vive en `app/src/debug/` (paquete `jmlopezdona.squareshift.prototipo`), se declara en el manifiesto de depuración como la Muestra del tema y se abre con:

```
adb shell am start -n jmlopezdona.squareshift.debug/jmlopezdona.squareshift.prototipo.PrototipoMandoActivity
```

Crea su propio `DirconServer`, su `AnuncioNsd` y su `AdaptadorWifi`, sobre `AppGraph.ambitoApp`, `AppGraph.hiloControl` y `AppGraph.registro`, para que el tráfico salga en el mismo registro y en `adb logcat -s SqTrama`. No toca la `Sesion`: abrir la actividad no arranca el `RideService` ni busca el Neo y el Square.

- **Alternativa: un módulo de app aparte.** Obligaría a extraer `ble/` y `zwift/` a un módulo compartido (`arquitectura.md` § 4.1), a otro `applicationId` y a ampliar el hook de `adb`. Más trabajo para una prueba desechable.
- **Alternativa: una sección de Diagnóstico.** Convive con la `Sesion` y su servidor DirCon, y ensucia una pantalla de producto con controles experimentales. Elegido con el usuario al proponer el change.
- Sin maqueta (excepción a «Cambios de UI: maqueta primero» de `AGENTS.md`, acordada con el usuario): la pantalla es de depuración y desechable. Usa los composables existentes (`AppBar`, `SettingsList`, `Button`, `ConnectionChip`, `Notice`, `SquareKey`, `HexLog`) sobre `SqTema`, sin componentes nuevos.

Durante la prueba, el usuario cierra la sesión de rodaje (Atrás en Rodaje) para que Zwift no vea también «Square Shift»: no rompe nada, pero confunde las listas. La configuración «Solo mando por wifi» con la sesión abierta es, de hecho, la alternativa de dos dispositivos por wifi (rodillo y mando separados); si «Mando y rodillo» fallara y esa funcionara, también vale para el change 13.

### 2. `MandoZwiftCodec` en `:nucleo`, con las variantes como parámetros

Va en `protocolo/`, en `main`, porque el change 13 lo usará en la app. Funciones puras sobre bytes:

| Función | Qué hace |
|---|---|
| `esHandshake(valor)` | `true` si empieza por `52 69 64 65 4F 6E` («RideOn»), con o sin bytes detrás |
| `respuestaHandshake(variante, recibido)` | `RideOn` pelado, `RideOn` + los bytes que siguieron al de Zwift (eco), o `RideOn 02 00` |
| `botones(pulsados, tabla, freno)` | `0x23` + `RideKeyPadStatus`: campo 1 varint con la máscara **invertida** (`~máscara & 0xFFFFFFFF`) y, si hay freno, las palancas analógicas |
| `reposo()` | `23 08 FF FF FF FF 0F`: ningún botón pulsado; también es el *keepalive* |
| `peticionInfo(valor)` y `respuestaInfo(variante, id)` | Petición de información de Zwift (`00 08 <id>`) y respuesta `3C 08 <id> …` según la variante: ninguna, vacía (solo el identificador) o con el nombre «Zwift SF2» en una página supuesta (`12 <n> 0A <m> 12 <k> "Zwift SF2"`). El formato interno de la página no está publicado: es una suposición que se valida con Zwift (añadido tras la primera prueba, ver «Resultados de la validación») |

- `BotonMando` enumera los botones del Ride (`IZQUIERDA`, `ARRIBA`, `DERECHA`, `ABAJO`, `A`, `B`, `Y`, `Z`, `CAMBIO_SUBIR_I`, `CAMBIO_BAJAR_I`, `POWERUP_I`, `ENCENDIDO_I` y los tres de la derecha) y `TablaBotones` da su valor: `PROTO` (la del `.proto`: `Z = 0x100`…) o `ENUM_MANUAL` (`Z = 0x80`…; la de partida desde la primera prueba, porque un Click v2 real la confirma). Los siete primeros coinciden en las dos.
- `CodificacionFreno` lleva el número de campo (2 o 3), la `Location` de cada freno y el valor (`AnalogValue`, `sint32` en zigzag). Cada palanca analógica es un submensaje `RideAnalogKeyPress`: `Location` (campo 1, varint) y `AnalogValue` (campo 2, zigzag). Con el campo 3 van repetidas directamente en `RideKeyPadStatus`; con el campo 2, dentro de un grupo `AnalogButtons` que las repite en su campo 1. Variantes de partida en la pantalla: campo 3 / campo 2; ubicación izquierda = `LEFT` (0) y derecha = `RIGHT` (1), o las dos en `DOWN`; valor 100 o −100. Con el freno suelto, no se envía palanca (o valor 0 en la variante que lo pida).
- Codificación a mano de varint y zigzag, sin `protobuf-lite` (`arquitectura.md` § 3).
- **Alternativa: valores fijos y recompilar en cada prueba.** Cada vuelta costaría compilar e instalar con Zwift abierto; con las variantes en pantalla se prueban en segundos y el registro dice cuál se usó.

### 3. El servicio del mando sobre `ServidorGatt`

`ServicioMando` (depuración) monta el servicio `00000001-19CA-4651-86E5-FA29DCDD09D1`:

| Característica | Propiedades | Comportamiento |
|---|---|---|
| `…0002…` ASYNC | Notificar | Botones hacia Zwift |
| `…0003…` SYNC_RX | Escribir | Si es handshake, responde por SYNC_TX y marca «handshake hecho»; el resto se anota y se ignora |
| `…0004…` SYNC_TX | Leer, Indicar | Respuesta al handshake y *keepalive*; leer devuelve el último valor |
| `…0005…`, `…0006…` | Notificar / Leer, Escribir, Indicar | Presentes en el original; se anotan y no hacen nada |

Más Device Information (`0x180A`: fabricante «Zwift», modelo, número de serie, hardware `A.0`, firmware) y Battery (`0x180F`, `0x2A19` = 100). Fabricante y modelo son una variante más si Zwift no lo reconoce.

- En «Mando y rodillo por wifi», el `ServidorGatt` junta los servicios de un `ZwiftBridge` (con un callback que solo anota los comandos y responde éxito, y telemetría a 0 cuatro veces por segundo) y los del mando. Así se prueba el «mismo servidor» de `arquitectura.md` § 7.4 sin la `Sesion`. Para no mezclar el Device Information del puente con el del mando, se queda el del puente.
- Botones antes del handshake: no se envía nada (spec, «Antes del handshake»). Tras el handshake, cada cambio del conjunto de pulsados envía `botones(...)` al instante, y cada 5 s sin cambios se envía `reposo()` por SYNC_TX como *keepalive* (o el estado actual si hay algo pulsado). Si Zwift se va, el conjunto se vacía y el handshake se olvida.
- Todo en `AppGraph.hiloControl`, como el puente: el `ServidorGatt` solo se toca desde ese hilo.

### 4. `DirconServer` con nombre de anuncio configurable

Se añade el parámetro `nombre: String = NOMBRE` al constructor; el de `AppGraph` no cambia. La prueba usa «Square Shift Mando» y una identidad (`mac-address`, `serial-number`) fija y distinta de la del puente, para que Zwift no lo confunda con «Square Shift» emparejado.

- Los registros TXT siguen saliendo de los servicios: `ble-service-uuids` incluirá el UUID largo del mando. Variante en la pantalla: añadir `0xFC82` a la lista (el UUID corto del anuncio BLE del Ride), por si Zwift filtra los mandos por él.
- **Alternativa: un `DirconServer` copiado en depuración.** Duplica el servidor validado; el parámetro es una línea y no cambia el comportamiento de la sesión.

### 5. Bluetooth solo si falla el wifi, con un periférico mínimo de depuración

Si con las variantes de wifi Zwift no lista el mando o no hace el handshake, se pasa a «Solo mando por Bluetooth». `PerifericoMando` (depuración) usa `BluetoothGattServer` y `BluetoothLeAdvertiser` nativos (`arquitectura.md` § 3) y adapta el mismo `ServidorGatt`: escrituras de `…0003…` a `escribir`, descriptores CCCD a `suscribir`, y `notifyCharacteristicChanged` con `confirm = true` para SYNC_TX (indicar).

- Anuncio: UUID de servicio `FC82` y datos de fabricante `0x094A` + tipo + 2 bytes de dirección (fijos, porque Android no da la dirección aleatoria del anuncio). Variantes: tipo (ninguno, `0x07` Ride derecho, `0x08` Ride izquierdo, `0x09` Click, `0x0A` Click v2; por defecto `0x08`, el único que un Ride real necesita conectar) y si la respuesta de escaneo lleva el nombre. Android solo deja anunciar el nombre Bluetooth del propio móvil; cambiarlo a «Zwift SF2» renombraría el teléfono del usuario, así que no se hace sin preguntarle (decidido al implementar, tras la primera prueba).
- Comprueba `bluetoothLeAdvertiser != null` (NF3) y el Bluetooth encendido; pide `BLUETOOTH_ADVERTISE` y `BLUETOOTH_CONNECT` al publicar. `BLUETOOTH_ADVERTISE` se declara en el manifiesto de depuración, no en el principal: el change 11 lo moverá.
- No se generaliza en `GattServerHost` y `Advertiser`: el change 11 los diseña para el puente completo. Si el mando acaba yendo por Bluetooth, el 13 depende del 11 (`planificacion.md`).
- **Alternativa: probar Bluetooth en paralelo desde el principio.** Más código si DirCon funciona, que es lo que hace falta para un solo dispositivo con el puente por wifi. Las tareas de Bluetooth quedan como sección condicional.

### 6. Validación con Zwift en PC

Guiada con el usuario, que maneja Zwift; el agente lee el registro con la skill `desplegar-movil`. Por pasos, y cada resultado se anota en `arquitectura.md` § 7.4:

1. **Solo mando por wifi.** Desde el Mac, `dns-sd -B _wahoo-fitness-tnp._tcp` lista «Square Shift Mando». En Zwift, Emparejar > Mandos: ¿aparece? Si no, variante `0xFC82` en el TXT y nombre o fabricante en Device Information. Al emparejar, el registro muestra el descubrimiento, la suscripción y lo que Zwift escribe en SYNC_RX.
2. **Handshake.** Se prueban las tres respuestas hasta que Zwift se quede conectado y lea o reciba por SYNC_TX. Se anota la trama exacta de Zwift.
3. **Botones.** Menú (cruceta y A/B), giro manteniendo ←/→ en un cruce, Y con un powerup, Z con alguien cerca. Si Z o los de después no responden, tabla `ENUM_MANUAL`. Diez minutos sin tocar nada para el *keepalive*.
4. **Freno.** Hace falta que el avatar se mueva, y con «Mando y rodillo» la potencia es 0. Se prueba con «Solo mando por wifi» y la sesión de rodaje abierta por excepción (rodillo «Square Shift» con el Neo, abierta antes desde la app y con la prueba encima): pedaleando a más de 30 km/h, mantener cada freno 3 s con cada variante hasta ver frenar el avatar, y comprobar que aparece «Braking Sensitivity» en Hardware.
5. **Mando y rodillo por wifi.** El mismo dispositivo en las cuatro listas.
6. **Cierre de Zwift con un botón mantenido** y pérdida de wifi (spec, «Conexión con Zwift perdida»).
7. Solo si 1 o 2 fallan con todas las variantes: los mismos pasos por Bluetooth.

Resultado posible y qué cambia:

| Resultado | Siguiente |
|---|---|
| Mando y rodillo por wifi funcionan | Change 13 sobre el `DirconServer` de la sesión; el 13 ya no depende del 11; el 14 se descarta |
| Solo el mando por wifi, separado del rodillo | Change 13 con dos anuncios DirCon (dos dispositivos en Zwift) |
| Solo por Bluetooth | Change 13 depende del 11; Zwift ve rodillo por wifi y mando por Bluetooth |
| Ninguno | F9 se descarta; el change 14 (teclado, F10) pasa a previsto y el 13 a descartado |

Si el freno no se consigue con ninguna variante, F9.5 se anota como no disponible y el resto de F9 sigue.

## Risks / Trade-offs

- [Zwift solo acepta mandos por Bluetooth y el PC no tiene Bluetooth fiable] → Se usa la llave o el Bluetooth del PC que ya empareja hoy el Square; si no, se prueba con Zwift Companion o en otro equipo y se anota.
- [Zwift reconoce el mando pero lo trata como Play (con cifrado) por el fabricante o el nombre] → Variantes de tipo en los datos de fabricante y en Device Information; se anota qué valor lo identifica como Ride.
- [La respuesta exacta al handshake es la que BikeControl ocultó en su parte privada] → Tres variantes de partida y el registro con la trama de Zwift; si ninguna vale, se deduce de lo que Zwift escriba después y se para a replantearlo (`/openspec-update-change`).
- [Sin un mando oficial para capturar, el freno depende de probar valores] → Variantes de campo, ubicación y signo en pantalla; si ninguna frena, F9.5 queda pendiente sin bloquear el change 13.
- [Dos anuncios DirCon desde el mismo móvil (sesión y prueba) confunden a Zwift] → La prueba usa otro nombre e identidad y se pide cerrar la sesión antes de empezar.
- [Código de prueba que se queda en el repositorio] → Vive solo en `app/src/debug/`; el change 13 lo sustituye por `MandoZwift` en `main` y borra la actividad.
- [Licencias] → Solo se reimplementa a partir de la descripción del protocolo; no se consulta ni copia código de BikeControl ni de qdomyos-zwift (NF6).

## Migration Plan

No hay migración: la actividad solo existe en depuración y el único cambio en `main` es un parámetro con valor por defecto en `DirconServer`. Para retirarla basta con borrar `app/src/debug/kotlin/jmlopezdona/squareshift/prototipo/` y su entrada del manifiesto de depuración.

## Open Questions

- Si Zwift en Apple TV acepta el mando por DirCon como en PC: no bloquea el change 13 y se prueba cuando se pruebe el puente en Apple TV.

## Resultados de la validación

### 26-10-2026, Zwift en PC por wifi (DirCon)

- **Solo mando por wifi:** Zwift no ofrece la lista de mandos hasta que hay una fuente de potencia emparejada, así que esta configuración no se puede probar sola.
- **Mando y rodillo por wifi:** «Square Shift Mando» aparece y se empareja como potencia, cadencia y rodillo controlable. **No aparece en la lista de mandos.**
- Aun así, al emparejar el rodillo Zwift descubre el servicio del mando por DirCon, se suscribe a ASYNC (`…0002`), SYNC_TX (`…0004`), `…0006` y la batería, lee SYNC_TX y `…0006`, y **hace el handshake**: escribe `52 69 64 65 4F 6E 02 04` («RideOn» + `02 04`). La respuesta `RideOn` pelada le basta para seguir.
- Después escribe en SYNC_RX, sin respuesta nuestra: `00 08 00` tres veces, una por segundo; luego `00 08 10`, `41 08 05` y `00 08 10` dos veces más. Deja de insistir a los ~5 s.
- Botones ↓, A y B enviados por ASYNC (`23 08 F7…`, `23 08 EF…`, `23 08 DF…` y el reposo al soltar): **Zwift no reacciona.**
- Fuentes públicas sobre el protocolo, que no son código:
  - `00 08 00` es la petición de información del dispositivo (opcode `0x00`, protobuf con un varint a 0). Un Ride real responde por SYNC_TX con un mensaje que empieza por `3C 08 00 12 2D 0A 2B 12 04…`, con el nombre «Zwift SF2», la dirección BLE y la versión de hardware. No se publica el formato completo.
  - El Ride real se anuncia como «Zwift SF2», con datos de fabricante `0x094A` + tipo (7 Ride derecho, 8 Ride izquierdo, 9 Click) + los 2 últimos bytes de la dirección.
  - En reposo envía periódicamente mensajes `0x19` o `0x15`, con contenido sin documentar.
  - Máscara confirmada con un mando real (Click v2): Y `0x40`, Z `0x80`, A `0x10`, B `0x20`, + `0x1000`. Coincide con la tabla `ENUM_MANUAL`, no con la del `.proto`.
- Tras añadir la variante de respuesta a la petición de información: con «Zwift SF2», Zwift acepta la respuesta al identificador 0, pide el 16, se le responde y deja de insistir; ya no envía `41 08 05`. Con `0xFC82` en el TXT de DirCon se comporta igual. En los dos casos, **ni aparece en la lista de mandos ni reacciona a ↓, A o B**.
- Conclusión por wifi: Zwift habla con el servicio del mando dentro del dispositivo rodillo, pero no lo trata como mando. Indicio, no confirmado: la lista de mandos sale del escaneo Bluetooth (nombre y datos de fabricante `0x094A`), que DirCon no puede llevar. Se pasa a la sección 5, por Bluetooth, de acuerdo con el usuario.

### 26-10-2026, Zwift en PC por Bluetooth

- Bluetooth del PC de juegos (adaptador MediaTek) encendido. Un escaneo BLE desde el propio PC ve el anuncio del móvil: datos de fabricante `0x094A` (3 bytes) y servicio `FC82`, a unos −75 dBm.
- Configuración: «Solo mando por Bluetooth» con fabricante `0x094A` + `0x08` (Ride izquierdo), nombre del móvil en la respuesta de escaneo, respuesta al handshake `RideOn` pelado, respuesta a la información «Zwift SF2» y tabla `ENUM_MANUAL`. La potencia, la cadencia y el rodillo siguen por wifi con la app normal («Square Shift» por DirCon): **dos dispositivos ante Zwift**.
- **Zwift empareja el mando**. Tras conectar (MTU 517), se suscribe a ASYNC, SYNC_TX, `…0006` y batería, lee la información del dispositivo (`2A24`, `2A25`, `2A26`, `2A27`, `2A29`), hace el handshake (`RideOn 02 04`) y pide la información 0 y 16, que se responden. También envía `41 08 05`, sin respuesta. A los ~33 s se reconectó una vez y repitió la secuencia.
- **↓, ↓ y A navegan en el menú de Zwift.** Es el mismo mensaje de botones que por wifi no hacía nada.
- Queda sin saber qué variante es imprescindible: los datos de fabricante o la respuesta a la información. Se prueba quitándolas de una en una.
- En rodaje los botones dejaban de llegar porque **Android avisa al servidor GATT de todas las conexiones del móvil**, también de las que abre la app como central (el Neo, el Square). El prototipo tomaba al Neo o al Square por Zwift: enviaba los botones al Neo o al Square, y cada reconexión del Square borraba el handshake. Corregido: solo cuenta como cliente quien usa el servidor (se suscribe, lee o escribe), las notificaciones van a todos los clientes y el estado se olvida cuando no queda ninguno. **Para el change 13:** el servidor GATT de la app tiene que distinguir igual a Zwift de los enlaces centrales.
- Tras reinstalar, Windows mantenía el enlace del servidor anterior y Zwift se quedaba en «Conectando…». Lo resolvió cerrar y abrir Zwift. Con el Bluetooth del móvil apagado y encendido no bastó, porque el PC volvió a enlazar antes de publicar el servidor.
- Con el mando emparejado por Bluetooth y el rodillo por wifi: menús con las flechas, A y B, cargar una ruta solo con el mando, y **Z da Ride On**. Esto confirma la tabla `ENUM_MANUAL` (`Z = 0x80`).
- Los botones de cambio (`CAMBIO_*`) hacen que Zwift avise de que el rodillo no admite marchas virtuales, como se esperaba: el puente es FTMS. **Para el change 13:** las palancas del Square no deben reenviarse como botones de cambio.
- **Freno:** Freno I y Freno D, con el campo 3 (`AnalogPaddles`), `LEFT` 0 / `RIGHT` 1 y valor +100, **frenan el avatar hasta pararlo**. Codificación confirmada.
- El giro en los cruces con ←/→ funciona, pero **el movimiento lateral en la calzada no**. Hipótesis: va por las mismas palancas analógicas con otro valor o ubicación. Se añaden al prototipo palancas de prueba mantenidas: ubicación 0 y 1 a −100, y ubicación 2 y 3 a ±100.
- **Dirección:** la palanca de ubicación 0 (`LEFT`) a −100 **mueve al avatar a la izquierda**; la misma palanca a +100 frena. P1 −100, P2 ±100 y P3 ±100 no hacen nada visible. Para la derecha se prueban intensidades intermedias (25, 50, 75) en las ubicaciones 0 y 1, elegibles con el mando publicado.
- Por un fallo de la pantalla, ya corregido, todas las palancas de prueba enviaron −50. Aun así sirvió: **P0 −50 también gira a la izquierda**; **P1 −50, P2 −50 y P3 −50 frenan**. Hasta aquí: la ubicación 0 negativa gira a la izquierda, y el resto de combinaciones probadas (P0 +100, P1 ±, P2 −, P3 −) frenan. Hipótesis: la ubicación 0 es el eje de dirección y la derecha va en positivo con un valor menor que el del freno.
- **Mapa de palancas confirmado** (campo 3, una pulsación por palanca):

  | Palanca | Negativo | Positivo |
  |---|---|---|
  | 0 (`LEFT`) | gira a la izquierda | frena |
  | 1 (`RIGHT`) | frena | gira a la derecha |

  La primera prueba de Freno D (P1 +100) se tomó por freno, y era giro a la derecha. Probablemente quedaba un freno anterior: **Zwift conserva el último valor de cada palanca** mientras no recibe otro (tras P0 +100 el avatar no arrancaba hasta enviar P0 −100). Por eso las palancas sueltas tienen que ir a 0, como hace un Ride real, y «Palancas a 0 con el freno suelto» pasa a estar activada por defecto. El codec envía una sola pulsación por palanca: la de prueba, el freno o 0.
- **Para el change 13:** freno con P0 positivo y P1 negativo (los dos a la vez, más fuerte); giro a la izquierda con P0 negativo y a la derecha con P1 positivo; y siempre las dos palancas a 0 al soltar.
- **Con las palancas a 0 al soltar, todo se comporta como un mando real:** P0 +100 frena y, al soltar, el avatar sigue; P0 −100 gira a la izquierda y P1 +100 a la derecha, y al soltar vuelve a ir recto; P1 −100 frena; P0 +100 y P1 −100 a la vez frenan más.

### Pendiente al cerrar la sesión de prueba (27-10-2026)

- **Diez minutos sin tocar nada** con el mando por Bluetooth (*keepalive*).
- **Cerrar Zwift con un botón o una palanca mantenidos** y comprobar que, al volver, el mando se reconecta sin nada pulsado ni el avatar frenado o girando.
- **Y** (powerup) y **↑ / B** en rodaje (barra de acciones), sin probar con la versión corregida. La vista trasera no tiene botón directo conocido; se llega desde la barra de acciones (↑, ←/→ y A), que funciona en menús.
- **Qué variante es imprescindible** por Bluetooth: los datos de fabricante `0x094A` o la respuesta «Zwift SF2» a la petición de información. Se probó siempre con las dos.
- Bluetooth del móvil sin el enlace de una prueba anterior: tras reinstalar o volver a publicar, hubo que cerrar y abrir Zwift. El change 13 tiene que tenerlo en cuenta, porque el servidor GATT vivirá en la sesión.
