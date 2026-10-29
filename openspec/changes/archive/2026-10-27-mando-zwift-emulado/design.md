## Context

Punto de partida, tras el change 12 (`prototipo-mando-zwift`) y lo validado en `arquitectura.md` § 7.4:

- `MandoZwiftCodec` (`:nucleo`, `protocolo/`) codifica el handshake, la respuesta a la petición de información, el mensaje de botones con la máscara invertida y las palancas analógicas, con sus variantes como parámetros. Tiene vectores en `MandoZwiftCodecTest`.
- En `app/src/debug/.../prototipo/` viven `ServicioMando` (servicio del mando sobre un `ServidorGatt`, handshake, información, *keepalive* de 5 s, botones y palancas) y `PerifericoMando` (servidor GATT nativo, anuncio `FC82` + fabricante `0x094A`, clientes por uso, notificaciones en serie), que implementa `TransporteZwift`. Lo que hace falta para el 13 ya funcionó con Zwift en PC por Bluetooth, con la sesión de rodaje conectada al Neo y al Square.
- La `Sesion` (`:app`) recibe los eventos del Square en el hilo de control: `EventosBoton` → `ReconocedorGestos` → `Asignacion` → `ejecutar`. El reconocedor da «pulsar» al pulsar, sin espera, si la entrada tiene «pulsar» asignado; no avisa al soltar.
- `Accion` (`:nucleo`) es serializable con nombres estables, y la asignación por defecto (`POR_DEFECTO`, `POR_DEFECTO_PLATOS`) no tiene nada en la cruceta (salvo con Campag. I), A, B, Y, Z, ✕, ○ ni en los frenos. `Configuracion` va por la versión 4 y añade reglas nuevas en las migraciones con `conReglasSiLibres`.
- Botones muestra «Funciones de Zwift» como filas informativas (`FilaZwift`), y Asignar acción avisa de los botones que lee Zwift con el Square emparejado (`AvisoAsignar.Zwift`).
- No hay servidor GATT nativo ni anuncio en `main`. `BLUETOOTH_ADVERTISE` solo está en el manifiesto de depuración.
- Por DirCon, en el 12, Zwift hacía el handshake con el servicio del mando pero no lo trataba como mando. La decisión 0 prueba otra forma de publicarlo antes de seguir: si funciona, cambia el transporte de este change y las decisiones 4 a 6, 8 y 11 se reescriben.

Motivo y alcance: `proposal.md`. Comportamiento: las specs de este change.

## Goals / Non-Goals

**Goals:**

- Mando de Zwift publicado por la sesión de rodaje con lo validado en el 12, sin variantes, y los botones del Square llegando a Zwift con pulsar y soltar.
- Funciones de Zwift como acciones de la asignación, con el mismo camino que el resto (`arquitectura.md` § 5.3).
- Un servidor GATT nativo y un anuncio en `ble/` que el change 11 pueda ampliar si llega a hacerse, sin diseñarlo para el puente FTMS.
- Sin efecto en la latencia de las palancas (NF1).

**Non-Goals:**

- Servir FTMS o Cycling Power por Bluetooth, o elegir el transporte (change 11).
- Recortar las variantes de `MandoZwiftCodec`: no estorban y sus tests siguen valiendo.
- Cambiar el nombre Bluetooth del móvil.

## Decisions

### 0. Transporte del mando: prueba por DirCon antes que Bluetooth

El usuario prefiere el mando por wifi: usa el `DirconServer` que ya tiene la sesión y evita el servidor GATT nativo, el anuncio BLE, `BLUETOOTH_ADVERTISE`, la confusión entre Zwift y los enlaces centrales y el enlace antiguo que Windows recuerda (§ 7.4). En el change 12 no funcionó, pero hay una causa probable que no se probó.

**Lo que se sabe** (de leer el código de BikeControl anterior a su etiqueta `gpl3` y el de qdomyos-zwift; solo datos de protocolo, sin copiar código, NF6):

- En el 12, el mando iba por DirCon con el servicio `00000001-19CA-4651-86E5-FA29DCDD09D1`. Dentro de un rodillo DirCon, ese servicio es el de las **marchas virtuales del Zwift Hub**: qdomyos lo publica así para eso. Encaja con lo que hizo Zwift: handshake, `GET 0`, `GET 16` y `LOG_LEVEL_SET`, y ningún caso a los botones.
- BikeControl controla Zwift por red desde diciembre de 2025 y es su conexión recomendada. Publica el mando por DirCon con **un solo servicio, `0000FC82-0000-1000-8000-00805F9B34FB`** (el del Ride), con tres características: `0003` SYNC_RX (escritura), `0002` ASYNC (notificación) y `0004` SYNC_TX (notificación, no indicación). Sin Device Information ni Battery. TXT `ble-service-uuids` = `FC82`. El nombre mDNS era siempre el de una KICKR BIKE («KICKR BIKE PRO 1337»).
- El handshake y el mensaje de botones son los mismos que ya validamos por Bluetooth. Zwift corta un mando por red tras unos 30 s sin tráfico; BikeControl envía el reposo por ASYNC cada 5 s.
- Hasta su versión 5.5, el mando era un dispositivo DirCon aparte del rodillo. Desde la 5.6 van en uno solo, y Zwift selecciona el mando al elegir ese dispositivo como fuente de potencia.

**Variantes nuevas en la pantalla de prueba** (`app/src/debug/.../prototipo/`, sin tocar `main` salvo lo imprescindible):

| Variante | Valores | Por defecto |
|---|---|---|
| Servicio del mando | `0001-19CA` (como en el 12) / `FC82` | `FC82` |
| Características | Completas (`0002`…`0006`, Device Information y Battery) / Mínimas (`0003` escritura, `0002` notificación, `0004` notificación) | Mínimas |
| Nombre del anuncio DirCon | «Square Shift Mando» / «KICKR BIKE PRO» + 4 cifras hexadecimales fijas | «Square Shift Mando» |
| *Keepalive* | Por SYNC_TX (como en el 12) / reposo por ASYNC | ASYNC |

En «Mando y rodillo por wifi», el `ServidorGatt` junta FTMS, Cycling Power y Device Information del `ZwiftBridge` con el servicio del mando. Con las características mínimas, el mando no añade Device Information ni Battery. El TXT sale de los servicios, así que con `FC82` incluirá `0xFC82` como el resto de UUID cortos, igual que el rodillo validado. Si con eso no aparece, se prueba `FC82` sin `0x`, como BikeControl.

**Prueba con Zwift en PC**, guiada con el usuario y leyendo el registro con `desplegar-movil`:

1. **Mando aparte:** «Solo mando por wifi» con `FC82`, características mínimas y la sesión de rodaje abierta con el Neo y el Square (rodillo «Square Shift» emparejado; el Square desemparejado en Zwift). ¿Aparece «Square Shift Mando» en Mandos? Tras el handshake: ↓, A y B en los menús.
2. Si no aparece o no reacciona: nombre «KICKR BIKE PRO …». Si sigue sin reaccionar: `FC82` sin `0x` en el TXT.
3. **Un solo dispositivo:** «Mando y rodillo por wifi» con la variante que haya funcionado. ¿Zwift toma el mando al elegirlo como fuente de potencia?
4. Con la configuración que funcione: Z, freno y dirección con las palancas (palancas a 0 al soltar), y diez minutos sin tocar nada (*keepalive*).

**Decisión tomada en la puerta (27-10-2026): Bluetooth.** El mando funciona por DirCon solo como único dispositivo de la IP o dentro del rodillo con las marchas virtuales de Zwift (ver «Resultados de la prueba por DirCon»). El usuario elige mando por Bluetooth y rodillo por DirCon, y documentar la alternativa por si se reconsidera. De la prueba se quedan para Bluetooth la página de información completa y el *keepalive* por ASYNC (decisión 5).

**Puerta.** Se decidía el transporte con el usuario:

| Resultado | Siguiente |
|---|---|
| Funciona en un solo dispositivo | Mando en el `ServidorGatt` del `ZwiftBridge`, en el mismo `DirconServer` de la sesión. Se reescriben con `/openspec-update-change` la propuesta, las decisiones 4 a 6, 8 y 11, las specs (estados del mando sin Bluetooth) y las tareas desde la sección 1 |
| Solo como dispositivo aparte | Igual, con un segundo anuncio DirCon del mando (otro nombre, puerto e identidad). Zwift ve dos dispositivos, los dos por wifi |
| No funciona con ninguna variante | Se sigue con el plan por Bluetooth de este diseño, sin cambios |

Los resultados se anotan en «Resultados de la prueba por DirCon», al final de este documento, y en `arquitectura.md` § 7.4.

- **Alternativa: seguir directamente por Bluetooth.** Ya está validado, pero arrastra lo que el usuario quiere evitar, y la prueba por wifi cuesta poco con la pantalla de depuración.
- **Alternativa: un change aparte para la prueba.** El resultado decide cómo es este change; meterla como etapa previa evita rehacer el diseño dos veces.

### 1. `FuncionZwift` y `Accion.Zwift` en el dominio

`FuncionZwift` (`:nucleo`, `dominio/`) enumera las doce funciones: `ARRIBA`, `ABAJO`, `IZQUIERDA`, `DERECHA`, `A`, `B`, `Y`, `Z`, `GIRAR_IZQUIERDA`, `GIRAR_DERECHA`, `FRENO_IZQUIERDO` y `FRENO_DERECHO`, con nombres serializados estables (`arriba`, `girar-izquierda`…). `Accion.Zwift(funcion)` se serializa como `zwift`.

- `Accion.TODAS` añade las doce al final, para no mover las posiciones anteriores; `asignables(transmision)` las añade en las dos transmisiones. Botones las pinta en su grupo, no entre las de la app.
- No son repetibles: Zwift ya trata el botón mantenido.
- **Alternativa: una acción `Zwift(boton: SquareButton)` que reenvíe el botón tal cual.** Ata la función al botón físico y no deja asignar Ride On a otra combinación (F4.2). Con `FuncionZwift`, la función es independiente del botón.

### 2. Traducción de las funciones al mando, pura, en `:nucleo`

`EstadoMandoZwift` (`:nucleo`, `dominio/`, puro) guarda cuántas entradas mantienen cada función y da la trama de botones del estado actual con `MandoZwiftCodec`, con las variantes validadas: tabla `ENUM_MANUAL`, palancas en el campo 3 y **las dos palancas siempre presentes, a 0 si están sueltas**.

| Función | En el mensaje |
|---|---|
| ←, ↑, →, ↓, A, B, Y, Z | Bit de su botón (`0x1`…`0x80`) |
| Girar a la izquierda | Palanca 0 a −100 |
| Freno izquierdo | Palanca 0 a +100 |
| Girar a la derecha | Palanca 1 a +100 |
| Freno derecho | Palanca 1 a −100 |

- Si una palanca tiene a la vez su giro y su freno (por ejemplo ✕ y la maneta izquierda), gana el freno: es la acción segura y no está validado qué hace Zwift con los dos.
- Contar las entradas que mantienen cada función permite asignar la misma función a dos botones sin que soltar uno la suelte.
- Puro y con tests de vectores: pulsar y soltar en distinto orden, dos frenos, giro y freno en la misma palanca, `soltarTodo`.

### 3. Pulsar y soltar en la `Sesion`, no en el reconocedor

El reconocedor no cambia. La `Sesion`, en el hilo de control:

- Un gesto «pulsar» con `Accion.Zwift(f)` pulsa `f` y apunta `botón → f` en `mantenidas`. El `EventoBoton.Soltado` de ese botón la suelta. Se envía al instante, en el mismo paso que la notificación del Square, como las marchas (§ 5.1).
- «corta», «larga» o «doble» dan una pulsación breve: pulsar y, 100 ms después, soltar, con un temporizador en el hilo de control.
- Se sueltan todas al perder el Square (`soltarBotones`), al abrir la captura de Asignar acción, al desactivar el ajuste, al perder a Zwift y al terminar.
- Las funciones de Zwift no pasan por el camino de ERG (van antes que `gestoErg`), no vibran, no emiten aviso de acción y sí actualizan el último gesto de Diagnóstico.
- **Alternativa: un tipo de gesto `Mantener` en el reconocedor** (lo que preveía `arquitectura.md` § 4.3). Añade un gesto a la pantalla, a la serialización y a las reglas de exclusión, cuando «pulsar» ya sale al pulsar; lo único que falta es el soltar, que la `Sesion` ya recibe con cada evento. Se actualiza § 4.3.

### 4. `GattServerHost` y `Advertiser` en `ble/`, a partir de `PerifericoMando`

`PerifericoMando` se parte en dos clases de `main`, sin las variantes de la prueba:

- `GattServerHost`: abre el `BluetoothGattServer` con los servicios de un `ServidorGatt`, añade los servicios de uno en uno, atiende lecturas, escrituras y descriptores CCCD en el hilo de control y envía las notificaciones en serie, esperando a `onNotificationSent`. **Cliente es solo quien usa el servidor** (lee, escribe o se suscribe); las conexiones de la app como central (el Neo y el Square) no cuentan (§ 7.4). Implementa `TransporteZwift`, así que los tests de la `Sesion` usan los mismos dobles; la identidad de DirCon no se usa por Bluetooth.
- `Advertiser`: anuncio conectable con `FC82`, datos de fabricante `0x094A` + `0x08` + 2 bytes fijos y el nombre del móvil en la respuesta de escaneo. Se envían los dos, fabricante y nombre, como en la prueba (`proposal.md`, fuera de alcance).
- Comprobación de modo periférico (NF3): `bluetoothLeAdvertiser == null` o `isMultipleAdvertisementSupported == false` da «No disponible».
- Genéricos, sin nada del mando: `GattServerHost` sirve cualquier `ServidorGatt` y `Advertiser` anuncia lo que se le pase. El mando los usa a través de `TransporteZwift` (decisión 5), y el change 11 los podrá usar para el puente FTMS.
- **Alternativa: hacer antes el change 11 con el transporte general.** Descartada por el usuario al proponer: el rodillo va por DirCon, que funciona mejor, y el 11 puede no hacerse. Si se hace, añade sus servicios a este servidor.

### 5. `MandoZwift` en `zwift/`, a partir de `ServicioMando`

Monta su propio `ServidorGatt` con el servicio del mando, Device Information (fabricante «Zwift», modelo, serie, hardware `A.0`, firmware) y Battery (100), y lo publica con `GattServerHost`. Separado del `ServidorGatt` del puente, que va por DirCon.

- Respuestas fijas: `RideOn` sin más bytes al handshake; a la petición de información, las páginas del protobuf (`RespuestaInfo.PAGINA_RIDE`: `DevInfoPage` completa de un Ride izquierdo a la 0, `ClientServerCfgPage` a la 16, solo el identificador a las demás), que por DirCon es lo que hace que Zwift reconozca el mando (decisión 0); `41 08 05`, los `SET` y lo demás se anotan y se ignoran.
- Antes del handshake no envía botones. Tras él, cada cambio de `EstadoMandoZwift` sale por ASYNC al momento, y cada 5 s sin cambios, el estado actual también por ASYNC (*keepalive*, validado 10 min por DirCon; por Bluetooth se probó por SYNC_TX sin llegar a los 10 min).
- `estado: StateFlow<EstadoMando>`: `Desactivado`, `SinConexion` (Bluetooth apagado o sin permiso), `NoDisponible`, `Esperando` y `Conectado` (tras el handshake). Si Zwift se va (sin clientes), suelta todo y olvida el handshake.
- Todo el tráfico va al `RegistroSesion` con el origen `Zwift`, como en el prototipo.
- **Independiente del transporte.** `MandoZwift` solo conoce un `ServidorGatt` y un `TransporteZwift`: no importa `ble/`, ni sabe de anuncios, permisos o clientes BLE, que son cosa de `GattServerHost` y `Advertiser`. Expone su `ServicioGatt` para montarlo en cualquier servidor. Así el change 13b (`mando-zwift-dircon`, `planificacion.md`) podrá montarlo en el `ServidorGatt` del `ZwiftBridge` y publicarlo por el `DirconServer` sin tocar `MandoZwift`, `EstadoMandoZwift`, la `Sesion` ni las pantallas. `EstadoMando` habla de Zwift (esperando, conectado), no de Bluetooth; lo propio del Bluetooth (apagado, sin permiso, sin modo periférico) lo aporta el transporte.

### 6. Ciclo de vida en la `Sesion`

`AppGraph` crea el `GattServerHost`, el `Advertiser` y el `MandoZwift` una vez, como el `DirconServer`. La `Sesion` publica el mando con el ajuste activado, el Bluetooth encendido y los permisos concedidos, y lo para al terminar, al apagar el Bluetooth o al desactivar el ajuste.

- Se publica **una vez por sesión** y no se vuelve a publicar con cada reconexión del Neo o del Square: volver a publicar con Zwift abierto deja a Windows con el enlace anterior y Zwift en «Conectando…» (§ 7.4).
- `EstadoRodaje` gana `mando: EstadoMando`. Rodaje compone el chip «Zwift» con el estado del puente y el del mando (spec `ride-screen`); Dispositivos y Diagnóstico muestran la fila del mando con el nombre Bluetooth del móvil (`BluetoothAdapter.name`).

### 7. Ajuste y migración: `Configuracion` versión 5

- `mandoZwift: Boolean = true`. Un documento sin el campo lo lee activado.
- `ZWIFT_POR_DEFECTO`: las doce reglas «pulsar» del anexo A.1 (cruceta, A, B, Y, Z, ✕, ○, Freno I y Freno D), en `POR_DEFECTO` y `POR_DEFECTO_PLATOS`. No chocan con las de pantalla, que llevan Campag. I.
- Al leer una versión anterior a la 5, `conReglasSiLibres(ZWIFT_POR_DEFECTO)` en cada transmisión, como con ERG y pantalla. Restaurar la asignación por defecto las devuelve.
- **Alternativa: no tocar la asignación guardada y que el usuario asigne las funciones.** El mando no haría nada al actualizar, que es justo lo que el ajuste activado por defecto promete.

### 8. Permisos y manifiesto

`BLUETOOTH_ADVERTISE` pasa al manifiesto principal y se pide con `BLUETOOTH_SCAN` y `BLUETOOTH_CONNECT` (`arquitectura.md` § 12). Son del mismo grupo («Dispositivos cercanos»): en una instalación que ya tenía los otros dos, Android lo concede sin diálogo o con uno solo. Sin él, el mando queda «Sin conexión» y el resto funciona. En Android 11 o inferior no hace falta.

### 9. Interfaz y maqueta

- Botones: el grupo «Funciones de Zwift» pasa a tener una fila por función con sus entradas, como las acciones de la app, y abre Asignar acción. Desaparece la fila informativa de las palancas («Cambiar marcha»), que ya están en las acciones de la app; se confirma en la maqueta.
- Asignar acción: el gesto se elige igual; con una función de Zwift, «pulsar» se explica como «mientras se mantiene». `AvisoAsignar.Zwift` solo con el ajuste desactivado.
- Ajustes: sección «Zwift» al final, con el interruptor de la fila con casilla que ya usan otros ajustes.
- Dispositivos y Diagnóstico: fila del mando con `ConnectionStatus`. «Desactivado» y «No disponible» no son ni espera ni alerta: si la maqueta pide una forma neutra, se añade una variante a `sq-conn` en `design-system/` (con su `README.md`, `preview.html` y la guía regenerada) antes de usarla; si no, se usa la de «sin conexión». Decidido en la Etapa 1: variante neutra `sq-conn idle`, con un guion en `ink-muted` en lugar del punto y el borde normal, más la palabra; sin tokens nuevos. Solo fuera de Rodaje.
- Sin componentes nuevos previstos. Etapa 1 (maqueta) y puerta de validación del usuario antes de la Etapa 2 (`AGENTS.md`).

### 10. Retirada del prototipo

Se borran `app/src/debug/kotlin/jmlopezdona/squareshift/prototipo/`, sus tests de `app/src/testDebug/.../prototipo/` y la actividad del manifiesto de depuración; sus tests útiles (servicio del mando, clientes por uso) pasan a `MandoZwiftTest` y `GattServerHost` donde se pueda probar en la JVM. El parámetro `nombre` de `DirconServer` se queda: no molesta.

### 11. Validación con el equipo real

Guiada con el usuario, Zwift en PC, el Neo y el Square, **con el Square desemparejado en Zwift**. El agente lee el registro con la skill `desplegar-movil`.

1. Abrir la app: Dispositivos dice «Esperando» en el mando. En Zwift, emparejar «Square Shift» por wifi y el móvil en Mandos. El mando pasa a «Conectado» y el chip «Zwift», a punto lleno.
2. Con el Square: menús con la cruceta, A y B; cargar una ruta; ↑ y B en rodaje (barra de acciones); Y con un powerup; Z con alguien cerca; ✕ y ○ en la calzada; ← y → en un cruce; cada maneta 3 s a más de 30 km/h y las dos a la vez.
3. Las palancas cambian la marcha de la app sin aviso de Zwift sobre marchas virtuales, con la latencia de Diagnóstico como antes (NF1, 30 ms con Zwift en el 6).
4. Reasignar A a «+1 marcha»: Zwift deja de recibirla.
5. Diez minutos sin tocar nada y luego A: llega (*keepalive* por ASYNC). Al emparejar, Zwift muestra la bienvenida a Zwift Ride (página de información completa); si no, se anota y se prueba con la respuesta «Zwift SF2» del 12.
6. Mantener ○ y cerrar Zwift; reabrir: el mando se reconecta, el avatar va recto. Mantener un freno y apagar el Square: el avatar deja de frenar.
7. Apagar y encender el Square rodando: el mando no se pierde.
8. Desactivar «Mando de Zwift» rodando: Zwift pierde el mando y el rodillo sigue. Activarlo de nuevo: si Zwift se queda en «Conectando…», cerrar y abrir Zwift (riesgo conocido).

Cada resultado se anota en `arquitectura.md` § 7.4 y lo que no se pueda probar queda como prueba pendiente en `tasks.md`.

## Risks / Trade-offs

- [El anuncio y el servidor GATT empeoran la latencia del Square o del Neo] → En el 12 convivieron con la sesión sin notarlo; se mide la latencia de la palanca en Diagnóstico con el mando conectado (paso 3) y, si empeora, se para y se replantea.
- [El usuario sigue con el Square emparejado en Zwift y los botones llegan dos veces] → Dispositivos lo dice en la fila del mando; con el ajuste desactivado todo vuelve a ser como antes.
- [Windows recuerda el enlace anterior y Zwift se queda en «Conectando…» tras volver a publicar] → Una publicación por sesión; la ayuda y la prueba lo cubren (cerrar y abrir Zwift).
- [Zwift necesita algo del anuncio o de la información que no se ha aislado] → Se envían los dos, como en la prueba que funcionó.
- [Una función mantenida que no se suelta deja al avatar frenado o girando] → Se sueltan todas en cada pérdida (Square, Zwift, ajuste, captura, fin) y las palancas van siempre a 0; tests de la `Sesion` para cada caso.
- [Una actualización de Zwift cambia el protocolo no documentado] → El registro guarda todo el tráfico del mando para diagnosticarlo; el ajuste permite volver al Square emparejado.
- [Licencias] → Solo se reimplementa a partir de la descripción del protocolo (NF6). Para la decisión 0 se ha leído el código de BikeControl (versiones GPL-3.0 y la actual) y de qdomyos-zwift: se toman datos de protocolo (UUID, propiedades, flujo, nombres), no código ni bloques de bytes capturados de mandos reales.
- [La prueba por DirCon funciona en PC pero no en otra plataforma] → Solo se valida Zwift en PC, como en el 12; Apple TV se anota si se prueba.
- [Zwift solo acepta el mando por DirCon con nombre de KICKR BIKE] → Variante del nombre en la prueba. Si es imprescindible, se decide con el usuario si se acepta ese nombre en Zwift.

## Migration Plan

- `configuracion.json` pasa a la versión 5 al leerse: «Mando de Zwift» activado y las funciones de Zwift en las entradas libres de cada transmisión. Una versión anterior de la app descarta las reglas `zwift` al leer (`ReglasSerializer`) y el campo nuevo.
- Para volver atrás sin desinstalar, basta con desactivar «Mando de Zwift» y emparejar otra vez el Square en Zwift.

## Resultados de la prueba por DirCon

### 27-10-2026, Zwift en PC

- **«Mando y rodillo por wifi» con `FC82`** («Square Shift Mando», potencia a 0): Zwift descubre `fc82`, se suscribe a ASYNC y SYNC_TX, hace el handshake (`RideOn 02 04`) y pide la información 0 y 16 y `41 08 05`. No aparece en Mandos y no reacciona a los botones.
- **Dos anuncios DirCon desde el mismo móvil** (la sesión «Square Shift» y la prueba): Zwift muestra uno solo, con el nombre de uno y el puerto del otro. Se conectó al servidor de la sesión mostrando «Square Shift Mando». Indicio: Zwift junta los dispositivos DirCon de la misma IP.
- **Zwift recuerda el nombre por la identidad DirCon** (`mac-address`, `serial-number`): con otro nombre y la misma identidad seguía mostrando el anterior. En la prueba, cada nombre lleva su identidad.
- **Solo mando por wifi, «KICKR BIKE PRO 5351», con la sesión cerrada y el Neo emparejado directamente en Zwift como fuente de potencia:** **aparece en Mandos y se empareja**. Hace el handshake. Con la respuesta «Zwift SF2» a la información, los botones llegan por ASYNC, con o sin palancas en la trama, pero **Zwift no reacciona**.
- La página «Zwift SF2» estaba mal formada: el nombre iba en el campo 2 (`systemFwVersion`) de la `DevInfoPage` y faltaban las capacidades y el producto. Se añade la variante `PAGINA_RIDE` (tarea 0.3b).
- **Con `PAGINA_RIDE`, Zwift muestra «Bienvenido a Zwift Ride»**, pide la información 0, 16, 1024 y 771 (las dos últimas, nuevas, se responden solo con el identificador) y envía `41 08 05`. **↓, ↓ y A navegan en el menú principal.**
- Con las palancas a 0 al soltar: menús, B, Z (Ride On), Y (powerup), dirección y freno funcionan como por Bluetooth.
- Diez minutos sin tocar nada y luego ↓ y A: siguen llegando (*keepalive* del reposo por ASYNC cada 5 s).
- **Un solo dispositivo** («Mando y rodillo por wifi», «Square Shift Mando», `PAGINA_RIDE`, Neo desemparejado en Zwift): aparece a la vez como fuente de potencia y como mando, ↓ y A navegan, y **el nombre KICKR BIKE no hace falta**. Pero **Zwift activa sus marchas virtuales** («GEAR 8» en el HUD) y envía `SET` (`3f 08 01 10 00|01|02 22 …`) por SYNC_RX, tres veces cada uno, sin respuesta nuestra. Choca con F9.3: las marchas son de la app y el rodillo es FTMS.
- En esa prueba combinada, Zwift envía `SET` (`0x3F`) por SYNC_RX. Descodificados con el protobuf público (`Set`, campo 4 `TrainerSimulationParam`): `3f 08 01 10 00 22 0c …` lleva CW 0,51, masa de la bici 8,51 kg, masa del ciclista 95 kg y Crr 0,004; `3f 08 01 10 01 22 05 28 c7 02 38 00`, pendiente −1,64 % y relación virtual 0; `3f 08 01 10 02 22 04 38 a0 83 01`, relación virtual 1,68; y al rodar, `… 28 b1 02` y `… 28 c9 01`, pendiente −1,53 % y −1,01 % (`simulatedGrade`, `sint32` en centésimas). Con `FC82` junto a FTMS, Zwift lo trata como un rodillo con marchas virtuales (el mismo `FC82` que anuncian los rodillos Zwift, según qdomyos), no como un rodillo FTMS con un mando. **No se vio ninguna escritura en el punto de control FTMS, pero tampoco en las conexiones de la prueba sin marchas virtuales** (apenas se rodó, con potencia 0), así que no está probado que Zwift deje de enviar la pendiente por FTMS. Lo seguro es que habría dos modelos de marchas y el HUD mostraría la de Zwift (F9.3). Como los `SET` traen la pendiente, una alternativa no probada es aceptar ese modo, tomar la pendiente de ahí e ignorar la marcha de Zwift (habría que responder `SET_RESPONSE` y ver qué hace el HUD).
- **Sesión abierta («Square Shift», rodillo) y mando aparte («KICKR BIKE PRO 5351», solo `FC82`) desde el mismo móvil:** Zwift no lista ninguno. Al parar el mando, aparece «Square Shift». **Zwift solo admite un dispositivo DirCon por IP.**
- Conclusión: por DirCon el mando solo funciona solo (con el rodillo emparejado por otra vía) o dentro del rodillo con las marchas virtuales de Zwift. Ninguna de las dos sirve con el puente FTMS de la sesión por wifi.

## Resultados de la validación

### 27-10-2026, OnePlus 8 y Zwift en PC, con el Square desemparejado en Zwift

- Paso 1: Zwift empareja «Square Shift» por wifi y el móvil en Mandos por su nombre Bluetooth; Dispositivos y el chip «Zwift» pasan a conectado.
- Paso 2: menús con la cruceta, A y B, cargar una ruta, ↑ y B en rodaje, Y, Z, ✕ y ○ en la calzada, ← y → en un cruce, y cada maneta y las dos a la vez funcionan desde el Square.
- Pasos 3 y 4: las palancas cambian la marcha de la app sin aviso de marchas virtuales en Zwift; A reasignada a «+1 marcha» deja de llegar a Zwift.
- Paso 7: apagar y encender el Square rodando no pierde el mando.
- Paso 8: desactivar y activar «Mando de Zwift» rodando funciona; el rodillo sigue.
- Pendiente al archivar (pasos 5 y 6): diez minutos sin pulsar nada y luego A, cerrar Zwift con ○ mantenido y apagar el Square con un freno mantenido. La app suelta todo en esos casos y lo cubren los tests de `MandoZwiftTest` y `SesionTest`.
- El freno derecho va en la palanca 1 a −100, como en la tabla validada en el change 12; el botón «Freno D» de la pantalla de prueba enviaba +100, que gira a la derecha.
