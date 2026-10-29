# Design

## Context

Motivación y alcance en `proposal.md`; comportamiento en las specs (`heart-rate` y los deltas de `zwift-bridge`, `device-connection`, `ride-screen` y `diagnostics`). Estado actual relevante:

- `ZwiftBridge` construye el `ServidorGatt` que se ofrece a Zwift: FTMS, Cycling Power y Device Information. `DirconServer.registros` publica los UUID de ese servidor en el TXT `ble-service-uuids` al anunciarse, una sola vez por publicación. La telemetría sale de un bucle de `Sesion` (`telemetriaAZwift`, cada `PERIODO_TELEMETRIA_MS`) solo si `puente.suscrito`, y vale 0 sin Neo.
- Los clientes BLE (`NeoClient`, `SquareClient`) heredan de `ClienteBle` (Nordic Android-BLE-Library): conectar, reconexión en segundo plano sin límite, estado `EstadoEnlace` y `Notificacion`. `Sesion` los orquesta con `EstadoDispositivo` y los recordados en `EstadoGuardado` (`rodillo`, `mando`, que es el Square).
- `BleScanner` busca por prefijo de nombre, sin `ScanFilter`, y registra cada anuncio con sus servicios (`Anuncio: … servicios=…`). `Diagnostico` ya muestra esas líneas.
- La barra de Rodaje (`ConnectionBar`, `Enlace`, `RodajeUiState.enlaces`) admite más enlaces sumando uno a la lista; el icono `ic_favorite` ya está generado desde `icons.svg` (`planificacion.md`, 10f).
- La HW9 es un pulsómetro de brazo con Bluetooth y ANT+. El ventilador Wahoo KICKR Headwind sigue su ANT+; no pasa por la app. Cuántas conexiones Bluetooth admite la HW9 y cómo se anuncia (nombre y servicios) **no está confirmado**.

## Goals / Non-Goals

**Goals:**
- Una porción vertical probable con el equipo real: banda → móvil → Zwift, con un icono de estado en Rodaje.
- Que el pulso no afecte a nada de lo que ya funciona: ni a la latencia de las palancas (NF1), ni a la telemetría del Neo, ni a quien no tiene banda.
- Un cliente de pulsómetro que valga para cualquier banda estándar, no solo la HW9.

**Non-Goals:**
- Un modelo de pulso con zonas, medias o filtros (`proposal.md`, fuera de alcance).
- Cambiar cómo se presenta el rodillo a Zwift ni la identidad del puente.

## Decisions

### 1. Entrada: servicio estándar Heart Rate, un cliente propio sobre `ClienteBle`

`PulsoClient` hereda de `ClienteBle` como `SquareClient`: exige el servicio `0x180D`, se suscribe a `0x2A37` y emite las lecturas por un `Flow<Notificacion>`. No lee la ubicación del sensor ni el punto de control. **Alternativas:** una biblioteca de pulsómetros: dependencia nueva para decodificar dos formatos de un byte; ANT+: el móvil no tiene radio ANT+ salvo con un adaptador USB y la banda ya la ocupa el ventilador.

Prioridad de conexión: la normal. El Square pide prioridad alta sostenida por NF1 (palancas); el pulso va a ~1 Hz y no la necesita, así que se evita gastar batería del móvil y de la banda.

### 2. Decodificación y salida en `:nucleo`, con una sola codificación hacia Zwift

`PulsoCodec` (puro, con tests): `decodificar(bytes): Int?` lee el byte de banderas (bit 0 = formato de 8 o 16 bits) y devuelve los bpm, o `null` si el paquete es corto; `medida(bpm): ByteArray` produce `[0x00, bpm]` (formato de 8 bits, sin contacto, sin energía, sin RR). No se reenvía el paquete tal cual: puede venir en 16 bits o con RR y energía que Zwift no usa, y sus banderas de contacto describen a la banda, no a lo que la app ofrece. Los bpm válidos son 1–250 (spec `heart-rate`): un 0, un 255 o un paquete ilegible se ignoran.

### 3. «Hay pulso» como regla pura con reloj inyectado

`PulsoVigente(reloj, caducidadMs = 5_000)` en `:nucleo`: guarda el último valor válido con su instante y `valor(ahora): Int?` da `null` pasados 5 s. Es la regla de la spec («si pasan 5 s sin una lectura válida, no hay pulso»), con el mismo estilo que `FiltroBateria`: se prueba sin Bluetooth. La conexión perdida lo pone a `null` al momento (no espera los 5 s). El valor se mantiene entre lecturas, no se promedia. **Alternativa:** que Zwift decida la caducidad: es lo que hará para su sensor, pero Rodaje, Diagnóstico y el envío tienen que coincidir en lo que significa «sin pulso» en la app.

### 4. Salida a Zwift: un servicio más en el mismo servidor, siempre ofrecido

`ZwiftBridge` añade `ServicioGatt(0x180D)` con `0x2A37` (notificar) y `0x2A38` (ubicación del sensor, lectura, valor 0 «otra»: la banda va en el brazo). Se ofrece **siempre**, haya o no banda: el TXT mDNS se publica al anunciarse y cambiarlo después obligaría a anunciar de nuevo (y a la sonda de `comprobar-anuncio-zwift` a revalidarlo), mientras que el servicio sin datos es inocuo. Consecuencia aceptada: Zwift lista «Square Shift» también como pulso a quien no tiene banda; no la elige quien no la quiere.

El envío es por lectura, no por bucle: cada lectura válida de `PulsoClient` pasa por `PulsoVigente` y de ahí a `puente.pulso(bpm)` → `servidor.notificar(0x2A37, medida)` → `transporte.notificar`, por el hilo de control como `telemetriaAZwift`. Si Zwift no está suscrito, `notificar` devuelve `null` y no se envía nada; sin pulso no se emite nada y Zwift lo trata como sensor sin datos. `puente.suscrito` **no** incluye el pulso: gobierna el bucle de potencia y cadencia, que no debe cambiar con la banda. **Alternativa:** reenviar el pulso en el bucle de telemetría a cada periodo: añadiría hasta un periodo de retraso, enviaría valores repetidos y ataría el pulso a la lógica del Neo (el bucle envía 0 sin Neo, justo lo que el pulso no debe hacer).

El `registro` guarda la trama recibida (`Origen.Pulso`, `Rx`) y la enviada a Zwift (`Origen.Zwift`, `Tx`, como el resto de notificaciones).

### 5. Búsqueda por servicio, aparte de «Buscar dispositivos»

`BleScanner` gana un criterio de búsqueda por servicio anunciado (`180D`) además del prefijo de nombre. Los `ResultadoBusqueda` por clave se mantienen. «Buscar pulsómetro» lanza solo esa búsqueda, con los mismos 30 s, y «Buscar dispositivos» solo incluye al pulsómetro si ya hay uno recordado (para reconectarlo), así que quien no tiene banda no ve nunca «No encontrado» de pulso. **Si la HW9 no incluye `180D` en el anuncio** (es opcional y el escáner sin filtro no lo asegura), el criterio añade el prefijo de nombre que se vea en el registro de anuncios; es la primera tarea de la parte de la app, antes de escribir la búsqueda. **Alternativas:** elegir de una lista (más UI y más maqueta para un solo dispositivo; la regla del proyecto es «el primero que encuentra»); buscar siempre con «Buscar dispositivos» (errores para quien no tiene banda).

### 6. Recordar una banda y reconectar como las demás

`EstadoGuardado.pulsometro: DispositivoRecordado?`, opcional con valor por defecto `null` y sin subir `VERSION` (como `puenteZwift`). Al abrir la sesión, si está recordado, se conecta en segundo plano, con reconexión sin límite, igual que el Square. `Sesion` expone `pulsometro: EstadoDispositivo`, `pulsometroRecordado` y `pulso: Int?` en `EstadoRodaje`. Olvidar la banda desconecta, borra el recordado y pone `pulso` a `null`. No se persiste el valor del pulso.

### 7. Rodaje y Diagnóstico: un enlace y una fila, sin dato de pulso

`Enlace.Pulso(R.drawable.ic_favorite)` al final de la lista de `RodajeUiState.enlaces`, solo si `pulsometroRecordado != null`, con `enlace(Enlace.Pulso, e.pulsometro)` y los estados de siempre. El pulso numérico no sale en Rodaje (spec `ride-screen`): lo decide el change de «Datos en pantalla» y, si el usuario lo pide, lleva su maqueta. Cinco iconos de 14 dp con 10 dp entre ellos son 110 dp, y caben en 360 dp con la hora, el wifi y la batería de la franja superior: se comprueba en la maqueta, no solo en la app. Diagnóstico suma una fila a Conexiones con el estado, el último pulso y su antigüedad, con la misma estructura que las filas del Neo y el Square.

### 8. Resultado de ver cómo anuncia la HW9 (tarea 4.1, 2026-10-27)

La COOSPO HW9 se anuncia como «HW9 35443» y la búsqueda solo por servicio `0x180D` la encontró y se conectó a ella en el OnePlus 8: **anuncia el servicio de frecuencia cardiaca**, así que el criterio de la tarea 4.2 es solo el servicio, sin prefijo de nombre (`CriterioAnuncio.servicio` admite `prefijoNombre` si otra banda lo necesitara). Sus medidas `0x2A37` llegan cada ~1 s en formato de 8 bits, unas con intervalos RR (banderas `0x10`) y otras sin ellos (`0x00`); la app las decodifica igual y reenvía a Zwift `[0x00, bpm]`.

## Risks / Trade-offs

- [Zwift no lista el pulso de un dispositivo DirCon, o solo lo hace tras volver a emparejarlo] → Es lo central de la prueba con el equipo (tarea del change). Si Zwift guarda los servicios por dispositivo, al añadir `180D` hay que olvidar «Square Shift» en Zwift y volver a emparejarlo; se documenta en `docs/desarrollo.md`. Si Zwift no lo admite por DirCon, el change se descarta o pasa al puente Bluetooth (change 11) sin perder el cliente del pulsómetro ni la interfaz.
- [La HW9 admite pocas conexiones Bluetooth y el móvil o el PC se quedan sin hueco] → Sigue siendo opcional y la fila dice «No encontrado» o «Sin conexión» con un aviso que menciona que puede estar conectada a otro dispositivo. Se mide cuántas admite la HW9 al probar con el móvil y Zwift en PC conectados a la vez; el ANT+ del ventilador no ocupa ningún hueco Bluetooth.
- [La HW9 no anuncia `180D`] → Se añade el prefijo de nombre al criterio de búsqueda (decisión 5).
- [Un sensor óptico da lecturas tardías o erráticas al principio] → No se filtra (fuera de alcance); se valida con el uso y, si molesta, se decide en otro change.
- [El servicio de pulso siempre ofrecido aparece en la lista de Zwift de quien no tiene banda] → Aceptado (decisión 4); no cambia nada de lo demás.
- [El 0 de «sin pulso» se leería como 0 bpm en Zwift] → No se envía nada sin pulso (spec `zwift-bridge`).

## Migration Plan

Sin migración de datos: `pulsometro` es un campo opcional de `estado.json`. Al actualizar, quien ya tiene «Square Shift» emparejado en Zwift puede tener que olvidarlo y emparejarlo de nuevo para que aparezca en la lista de pulso (a validar). Marcha atrás: quitar el servicio `180D` de `ZwiftBridge` y la fila del pulsómetro; el campo `pulsometro` queda ignorado.

## Open Questions

- ¿Cuántas conexiones Bluetooth admite la HW9 a la vez? No cambia las specs: se mide en la prueba y se anota en `docs/referencias/` y `docs/arquitectura.md`.
- ¿El umbral de 5 s sin lectura es el adecuado para un sensor óptico? Si no lo es, se cambia la constante y el número de la spec `heart-rate` en el mismo change.
