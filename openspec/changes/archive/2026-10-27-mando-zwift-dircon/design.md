# Design

## Context

Punto de partida, tras `puente-zwift-wifi`, `prototipo-mando-zwift`, `mando-zwift-emulado` y `modelos-tabla-y-fisico` (resultados en `arquitectura.md` § 7.3 y § 7.4):

- El rodillo va a Zwift por DirCon con un `ServidorGatt` propio de `ZwiftBridge` (FTMS, Cycling Power, Heart Rate, Device Information). El comando `0x11` de FTMS llega a `Sesion.comandoZwift`, que lo convierte en `Terreno(pendientePct, crr, cw, vientoMs)` y lo entrega a `ControlRodillo.terrenoZwift`; este aplica la ventana de 250 ms, el modelo de resistencia (simple, tabla o físico), «Bajadas» y la dificultad.
- `MandoZwift` (`zwift/`) tiene su `ServicioGatt` (UUID de Bluetooth `00000001-19CA-…`), el handshake, la información, los botones y el *keepalive* por ASYNC, y notifica por un `TransporteZwift`. Hoy lo publica `GattServerHost` por Bluetooth.
- `Configuracion` va por la versión 5 y el ajuste «Mando de Zwift» es un booleano.

**Lo aprendido en la etapa 1** (detalle en «Resultados de la prueba», al final): con el servicio `FC82` dentro del rodillo, Zwift habla su protocolo de rodillo con marchas virtuales: la pendiente y el ERG llegan por `SET`, la potencia y la cadencia solo se leen de `TrainerNotification` (no de FTMS) y Zwift activa un «GEAR» propio que no usamos.

Motivo y alcance: `proposal.md`. Comportamiento: las specs de este change.

## Goals / Non-Goals

**Goals:**

- Un segundo modo de conexión, **«Todo por wifi (modo Zwift Ride)»**, en el que el rodillo y el mando son un solo dispositivo DirCon ante Zwift, con las marchas virtuales de la app (simple, tabla o físico) sobre la pendiente de los `SET`.
- **Los dos modos separados.** El modo de hoy («Rodillo por wifi y mando por Bluetooth») no cambia de comportamiento, y ningún código de uno se mezcla con el del otro salvo lo que se comparte a propósito (decisión 1).
- Un único camino del terreno al `ControlRodillo`, venga de FTMS o de un `SET`: el modelo de marchas no sabe de dónde viene la pendiente.
- Sin efecto en la latencia de las palancas (NF1): los `SET` llegan por el camino de Zwift, no por el caliente del Square.

**Non-Goals:**

- Dar a Zwift la marcha de la app, o seguir las marchas de Zwift (change 13c): Zwift no nos reconoce como rodillo apto para sus marchas virtuales.
- Implementar el protocolo de Zwift entero: solo el subconjunto de la decisión 3.
- Cambiar el modo por defecto: sigue siendo el de hoy.

## Decisions

### 0. Etapa 1: prueba con el equipo real (hecha) y puerta (resuelta)

Se hizo el 27-10-2026 con una pantalla de depuración (`app/src/debug/`, sin maqueta) y el Neo y el Square reales; los resultados están al final de este documento. **Puerta resuelta con el usuario: «funciona con matices», se sigue con los ajustes de este documento.** La pantalla de depuración y las variantes que se probaron se retiran en la etapa 2 (decisión 9).

### 1. Dos modos aislados detrás de `ConexionZwift`

Cada modo es una implementación de una misma interfaz, `ConexionZwift`, en `zwift/`. La `Sesion` solo conoce la interfaz: **ninguna rama `if (modo)` en la `Sesion`, en `MandoZwift` ni en `ZwiftBridge`**.

| | `ConexionZwiftFtms` (modo actual) | `ConexionZwiftRide` (modo nuevo) |
|---|---|---|
| Rodillo | `ZwiftBridge` por DirCon, FTMS estándar | `ZwiftBridge` por DirCon con el servicio `FC82` añadido |
| Entrada de Zwift | Comandos del punto de control FTMS (`0x11`, `0x05`) | `SET` (`0x3F`): pendiente, Crr, CW y `ergPower` |
| Salida hacia Zwift | Indoor Bike Data y Cycling Power | `TrainerNotification` por ASYNC (más Indoor Bike Data y Cycling Power, que Zwift ignora) |
| Mando | `MandoZwift` por Bluetooth (`GattServerHost`, `Advertiser`) | `MandoZwift` dentro del rodillo (servicio `FC82`) |
| Identidad ante Zwift | La de siempre (`puenteZwift`) | La suya (`puenteZwiftRide`, decisión 7) |

La interfaz recoge lo que la `Sesion` necesita y nada más: publicar y parar el modo; el estado de Zwift y el del mando (`StateFlow`); lo que llega de Zwift, ya traducido al dominio (un `Terreno`, una potencia de ERG, Zwift perdido); lo que se envía a Zwift (telemetría y pulso); y las funciones del mando (pulsar y soltar). Cada implementación es dueña de sus transportes, su puente y su mando.

**Lo que se comparte a propósito** (cada pieza se escribe una sola vez y no sabe qué modo la usa): `DirconServer` y `TransporteZwift`; `ZwiftBridge` (FTMS, Cycling Power, Heart Rate, Device Information, más `serviciosExtra`); el núcleo de `MandoZwift` (handshake, información, botones, *keepalive*); `MandoZwiftCodec`, `EstadoMandoZwift`, las acciones `Zwift(…)` y el pulsar y soltar de la `Sesion`; y el camino del terreno hasta `ControlRodillo`.

**Lo que es de un solo modo** (nunca se importa desde el otro): `SimulacionZwiftCodec`, `PedaleoZwiftCodec`, `AcumuladorSimulacion`, el receptor de `SET` y la identidad propia, en `ConexionZwiftRide`; `GattServerHost`, `Advertiser` y el decodificador FTMS del punto de control, en `ConexionZwiftFtms`.

El modo se elige al crear la sesión y al cambiar el ajuste, en `AppGraph`: la `Sesion` recibe la implementación activa y la sustituye al cambiar (decisión 6). Alternativa descartada: un único `MandoZwift` con una `modalidad` y ramas internas, que es lo que dejó el andamiaje de la etapa 1; mezcla el Bluetooth y el rodillo en la misma clase y obliga a probar cada cambio contra los dos.

### 2. Decodificar `Set` y codificar `SET_RESPONSE` en `:nucleo`

Un `SimulacionZwiftCodec` en `protocolo/`, puro y con vectores de la prueba, igual que `MandoZwiftCodec`:

- `decodificar(valor): SetZwift?` si el primer byte es `0x3F`: lee varints y campos con longitud, salta lo que no conoce y devuelve `SetZwift(opciones, idMensaje, simulacion)`. Un mensaje truncado devuelve `null` y no rompe nada.
- `SimulacionZwift` (en `dominio/`) lleva cada campo como nulable, porque cada `SET` trae solo algunos: pendiente, Crr, CW, masas, relación virtual, `ergPower`, resistencia y el resto en bruto.
- `respuesta(set)`: `SetResponse` con `status` 0 (campo 1) y `msgId` (campo 2), `40 08 00 10 <msgId>`; con eso Zwift no repite el `SET`.
- Protobuf **a mano**, como el resto (`arquitectura.md` § 3). Alternativa: `protobuf-lite`, con su dependencia y su plugin de Gradle, que no compensa para estos mensajes.

### 3. Subconjunto del protocolo de Zwift que se implementa

Solo lo necesario para usar nuestras marchas sobre lo que manda Zwift:

- **Sí:** handshake `RideOn` y páginas de información 0, 16, 1024 y 771; recepción del `SET` (pendiente, Crr, CW y `ergPower`) y su respuesta; datos de pedaleo (`TrainerNotification`); botones y *keepalive* del mando (ya existían).
- **No:** la relación de marcha de Zwift (se guarda para Diagnóstico y se ignora), las páginas de configuración del rodillo y de índices de marcha, `virtualShiftingMode`, el viento, la calibración y el resto de mensajes. Lo que Zwift pida y no conozcamos se registra y no se contesta.

El **terreno** de los `SET` se junta con un `AcumuladorSimulacion` (cada `SET` trae una parte) y llega al `ControlRodillo` por el mismo camino que el `0x11` de FTMS (`Sesion.terrenoDeZwift`). Su **`ergPower`** va a `control.potenciaZwift`, como el `0x05`; con terreno y potencia en el mismo `SET`, manda la potencia. El viento no se usa (Zwift no lo mandó). Las masas, el diámetro y la relación no entran en el `Terreno`: se guardan para Diagnóstico y el modelo físico sigue con el peso de Ajustes.

### 4. Datos de pedaleo hacia Zwift: `TrainerNotification`

Con `FC82` en el rodillo, Zwift no toma la potencia ni la cadencia de FTMS ni de Cycling Power (marcaba 0 W): las lee de `TRAINER_NOTIF` (`0x03`). `ConexionZwiftRide` la manda por ASYNC cada 250 ms, con los cinco campos obligatorios (potencia, cadencia, velocidad de la bici, potencia media y velocidad de la rueda; las velocidades a 0, porque Zwift calcula la suya) y solo tras el handshake del mando. Con tres campos Zwift la descartaba. `PedaleoZwiftCodec` en `:nucleo`, puro y con tests. No declaramos capacidad de rodillo en la información del dispositivo: probada, no ayuda.

### 5. Ajuste y migración: `Configuracion` versión 6

`ModoConexionZwift` (`RodilloYMandoBluetooth` por defecto, `TodoWifi`) en `Configuracion`; `mandoZwift` (activado o desactivado) sigue siendo el interruptor de los botones. Con `TodoWifi` y «Mando de Zwift» desactivado, los botones no llegan a Zwift pero el dispositivo sigue ofreciendo el servicio. La migración desde la versión 5 solo añade el campo con su valor por defecto. Exportar e importar (F12.6) lo lleva sin cambios.

### 6. Ciclo de vida en la `Sesion`

La `Sesion` publica la `ConexionZwift` del modo elegido cuando hay lo que ese modo necesita, y la sustituye al cambiar el ajuste:

| Modo | Condiciones para publicar | `NoDisponible` |
|---|---|---|
| `RodilloYMandoBluetooth` | las de hoy: wifi para el rodillo; ajuste del mando, Bluetooth, permisos y modo periférico para el mando | sí, sin modo periférico |
| `TodoWifi` | wifi; no hacen falta Bluetooth, permisos ni modo periférico | no aplica |

Cambiar de modo, con la sesión abierta: se sueltan todas las funciones de Zwift, se para la conexión actual (anuncio incluido), se publica la nueva con **su identidad** y se mantiene lo último que pidió Zwift durante los 30 s de siempre. No se toca la marcha ni lo que simula el Neo. Zwift tiene que reconectar y volver a emparejar el dispositivo del modo nuevo.

### 7. Identidad propia del modo `TodoWifi`

Zwift recuerda cada dispositivo DirCon por `mac-address` y `serial-number`. En la prueba, con la identidad que ya había sido emparejada con `FC82`, el puente de siempre se quedaba en «Conectando…» (Zwift reintentaba cada 6 s el handshake del mando). Por eso `TodoWifi` usa **otra identidad**, guardada aparte en `estado.json` (`puenteZwiftRide`, generada la primera vez, como `puenteZwift`), y cada modo se anuncia con la suya. Zwift ve dos dispositivos distintos según el modo, y cambiar de modo y volver no deja a Zwift con uno que ya no encaja. El nombre mDNS es «Square Shift» en los dos; Zwift distingue por identidad. Alternativa descartada: una identidad única con un aviso de desemparejar en Zwift, que es justo lo que falló.

### 8. Interfaz y maqueta

Pasa por maqueta (AGENTS.md). Sin componentes nuevos previstos:

- **Ajustes, «Zwift»:** una fila «Conexión con Zwift» con «Rodillo por wifi y mando por Bluetooth» y «Todo por wifi (modo Zwift Ride)», con el mismo patrón de selector que «Bajadas». La ayuda de «Todo por wifi» dice que Zwift ve un solo dispositivo, que mostrará su marcha fija (no la tuya) y que hay que emparejar de nuevo. «Mando de Zwift» sigue como interruptor.
- **Dispositivos:** la fila del mando dice cómo emparejarlo según el modo: elegir el móvil en la lista de mandos de Zwift (Bluetooth) o elegir «Square Shift» como potencia y mando a la vez (todo por wifi). Con todo por wifi no aparece «No disponible».
- **Diagnóstico:** en Conexiones, bajo la fila del mando, la última simulación de Zwift (pendiente, Crr, CW, masas, relación de su marcha y la hora) cuando hay una.
- **Rodaje:** sin cambios. Muestra la marcha de la app, nunca la de Zwift ni el modelo de resistencia (AGENTS.md).

### 9. Retirada del andamiaje de la etapa 1

Al terminar la etapa 2 se retiran: la pantalla de depuración y sus tests; `ModalidadMando`, el campo `mandoEnRodillo` y los métodos `probar*` de la `Sesion`; y las variantes que no sirvieron (`capacidadRodillo`, `respuestaLogLevel`, `CanalDatosRodillo` con `SYNC_TX` y `NINGUNO`, y las variantes de `RespuestaSet`), que quedan fijas en lo validado. Lo útil de sus tests pasa a los de cada modo.

## Risks / Trade-offs

- **El protocolo es privado de Zwift** (conocido solo por su descripción pública) y puede cambiar → solo se decodifica lo que se necesita, con tolerancia a campos desconocidos; el modo de siempre sigue siendo el predeterminado y vuelve atrás con un ajuste.
- **Dos modelos de marchas a la vista.** El HUD de Zwift muestra su «GEAR» fijo (la marcha 8), que no es la de la app → lo dice la ayuda del ajuste; F9.3 sigue en pie. Si molesta, vuelve al modo de siempre.
- **Zwift avisa de que el rodillo no admite marchas virtuales** si se le mandan sus botones de cambio. No los mandamos: las palancas son de la app.
- **Latencia de la palanca.** En la prueba, 205 y 212 ms de la notificación a la entrega, con el intervalo de conexión del Square a 187,5 ms (el envío tardó 3 ms). A medir con la versión final frente a NF1.
- **Reanuncio al cambiar de modo.** Zwift pierde la conexión unos segundos y tiene que emparejar el otro dispositivo → los 30 s de mantener lo último lo cubren; queda dicho en la ayuda.

## Migration Plan

- `configuracion.json` pasa a la versión 6 al leerse, con el modo de siempre. Quien no toque el ajuste no nota nada.
- `estado.json` gana `puenteZwiftRide`, que se genera al elegir por primera vez el modo `TodoWifi`.
- Para volver atrás sin desinstalar, basta con elegir el modo de siempre en Ajustes y volver a emparejar el mando en Zwift.

## Open Questions

- ¿Usar las masas de Zwift en el modelo físico en vez de las de Ajustes? No cambia las specs ni las tareas.

## Resultados de la prueba

### 27-10-2026, OnePlus 8 (compilación de depuración), Neo y Square reales, Zwift en PC

**Validado**

- **Zwift trata `FC82` dentro del rodillo como un rodillo con marchas virtuales.** Descubre `FC82`, hace el handshake, pide las páginas 0, 16, 1024 y 771 y activa «GEAR» (relación 1,68, la marcha 8 de su tabla, fija mientras no se le mande un botón de cambio). **No escribe nunca en el punto de control FTMS (`2AD9`)**: todo llega por `SET`.
- **`SET`.** Al conectar, CW 0,51, Crr 0,004 y masas (bici 8,51 kg, ciclista 95 kg). Después, la pendiente cada vez que cambia: en una rampa, pasos de 0,1 % cada ~2 s. Es la pendiente de la ruta (HUD en 2 % con `SET` de 1,99 a 2,10). Con la marcha de la app, el Neo recibió pendiente de Zwift más el ajuste de la marcha (1,69 % con una marcha menos, 0,69 %). Las palancas del Square cambian la marcha de la app sin ningún aviso de Zwift.
- **Respuesta al `SET`:** `40 08 00 10 <msgId>` (`SetResponse` con `status` 0 y `msgId`). Zwift no repite el `SET`. Con `40 10 <msgId>` tampoco lo repetía; `NINGUNA` no se probó con la identidad limpia.
- **Potencia y cadencia: con `FC82`, Zwift no usa FTMS ni Cycling Power.** Con vatios correctos por esos dos servicios, marcaba 0 W. Los lee de `TRAINER_NOTIF` (`0x03`, `TrainerNotification`) enviada por **ASYNC** (`…0002…`) con los **cinco campos obligatorios**: potencia (1), cadencia (2), velocidad de la bici (3), potencia media (4) y velocidad de la rueda (5). Con tres campos seguía en 0 W. Por SYNC_TX no se probó tras corregirlo. Las velocidades se envían a 0 y Zwift calcula la suya.
- **ERG.** Zwift pide la potencia por **`ergPower` (campo 2) del `SET`**: 50 W y 55 W (calentamiento), y el Neo recibió `Potencia(50)` y `Potencia(55)`. Al volver a mandar pendiente, la app vuelve a simular. Lo demás (aplicar la potencia en el Neo y el ajuste de las palancas) es lógica de la app, ya validada en `erg-manual`.
- **Botones del Square** (menús, etc.) llegan a Zwift por el mando dentro del rodillo: lo comprobó el usuario.
- **Pregunta 8 (botones de cambio de Zwift).** Con `CAMBIO_SUBIR_D` (tablas `PROTO` y `ENUM_MANUAL`), Zwift sube su marcha y manda un `SET` con la relación nueva a los 110–200 ms: 1,86 → 2,04 → 2,22 → 3,49 → 3,74, coherente con la tabla 1×24. **Pero avisa de que el rodillo no admite marchas virtuales.** Declarar en la información del dispositivo la capacidad de rodillo (tipo 1, máscara `0x14`, la de «KICKR CORE» en qdomyos-zwift) **no cambia el aviso** ni hace que Zwift pida las páginas de rodillo (512, 528, 544). No se probó `0x53` ni `0x07`, ni la respuesta a `41 08 05` (Zwift no la mandó en esa conexión). Qdomyos-zwift habla el dialecto anterior (`0x04`, `protocolVersion` 512), no el `Set` `0x3F`, así que no vale tal cual. Hipótesis sin confirmar: Zwift reconoce los rodillos aptos por una lista de dispositivos («Zwift Ready»). Se anota en el 13c.

**Hallazgo de identidad.** Zwift recuerda cada dispositivo por `mac-address` y `serial-number`. Con la identidad que ya había sido emparejada con `FC82` en pruebas anteriores, el puente normal se quedaba en «Conectando…» (Zwift reintentaba cada 6 s el handshake del mando) y con el mando mostraba 0 W; con datos de la app borrados (identidad nueva), el puente normal mostró vatios. Hay que tenerlo en cuenta al cambiar de transporte.

**Medidas de la sesión.** Latencia de la palanca al Neo de 205 y 212 ms (el envío tardó 3 ms; el resto lo pone el Square con su intervalo de 187,5 ms): a revisar frente a NF1.

**Sin probar:** 10 minutos sin pulsar nada, `SYNC_TX` para los datos de pedaleo, viento (Zwift no lo mandó), pulso hacia Zwift con el mando dentro del rodillo.

### Cambios que impuso al diseño (ya incorporados arriba)

1. **Decisión 4:** en modalidad rodillo, `MandoZwift` envía también `TrainerNotification` por ASYNC (potencia y cadencia del Neo, a ~4 Hz). Sin eso, Zwift ve 0 W.
2. **Decisión 3:** la potencia de ERG del `SET` (campo 2) se aplica; deja de ser «a validar».
3. **Decisión 0:** la respuesta definitiva al `SET` es `40 08 00 10 <msgId>`.
4. **Identidad:** cambiar de transporte puede dejar a Zwift con un dispositivo recordado que ya no encaja. Hay que decidir si el transporte «wifi» usa su propia identidad o si basta con avisar de desemparejar en Zwift.
5. **Specs `zwift-bridge` y `zwift-controller`:** añadir que, con el mando por wifi, la potencia y la cadencia llegan a Zwift por el protocolo del mando y no por FTMS.
6. **Marchas de Zwift (13c):** Zwift no reconoce el rodillo como apto aunque declare capacidad de rodillo; sin resolver.
