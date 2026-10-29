## Context

Tras el change 5, la `Sesion` (en `AppGraph.hiloControl`) conecta sola el Neo y el Square, traduce los botones en marchas y las marchas en comandos FE-C, y el `RideService` la mantiene viva con la pantalla apagada. Lo que hay hoy y condiciona el diseño:

- `ControlRodillo` (`:nucleo`) solo conoce `SimLibre` (terreno fijo con la pendiente base) y `Manual`; al salir de `Manual` vuelve siempre a `SimLibre`. Recuerda el último comando enviado para no repetirlo.
- `ModeloSimple` ya recibe un `Terreno` con pendiente, Crr, Cw y viento, aunque solo use la pendiente.
- La telemetría del Neo llega por `0x2A63` (potencia) y `0x2A5B` (biela) a ~1 Hz; `CalculoCadencia` saca la cadencia. No se lee la velocidad.
- `Asignacion` convierte gestos en acciones de marcha (`MarchaMas(n)`, `MarchaMenos(n)`, `MarchaMasDura`, `MarchaMasLigera`, `MarchaReferencia`); la `Sesion` las aplica con `Cambio`.
- `RegistroSesion` tiene los orígenes `Square`, `Neo` y `App`; Diagnóstico lee sus entradas.
- La app no tiene permisos de red ni código de red. `targetSdk` 36, `minSdk` 28; el OnePlus 8 de pruebas.
- Las maquetas ya tienen el chip de Zwift, la insignia «ERG ZWIFT», la zona de potencia objetivo de `RideScreen` (`sq-erg-hero`, `sq-target`, `sq-badge-erg`) y un estado de Diagnóstico con Zwift, pero con transmisión de marca y datos que este change no hace.
- DirCon no tiene especificación pública. Lo que dice `arquitectura.md` § 7.3 está **a validar** en su totalidad.

Motivo y alcance: `proposal.md`. Comportamiento: las specs delta de este change (`zwift-bridge` y los cambios en `ride-screen`, `ride-session`, `diagnostics`, `trainer-manual-control` y `screen-mockups`).

## Goals / Non-Goals

**Goals:**

- Comprobar DirCon con Zwift en PC antes de construir nada encima (decisión 1).
- Un modelo de servidor GATT independiente del transporte, para que el change 11 (Bluetooth) y el mando (changes 12 y 13) lo reutilicen sin tocar el puente.
- Los modos de control con Zwift en el dominio puro, probados en la JVM con reloj virtual, sin que el terreno de Zwift retrase el camino caliente de la marcha (NF1).
- Cerrar las decisiones abiertas 1, 2 y 3 de `arquitectura.md` § 15 con medidas del equipo real.

**Non-Goals:**

- Elegir el transporte, el servidor BLE y el anuncio BLE (change 11).
- Usar Crr, Cw o viento en la resistencia (modelo físico, change 15): se guardan y se muestran en Diagnóstico.
- Leer la página FE-C 0x19 para dar telemetría a 4 Hz con datos nuevos (decisión 5).

## Decisions

### 1. Primero, DirCon contra Zwift en PC; si no funciona, se para

El orden de las tareas de la app empieza por la porción mínima que Zwift puede ver: `DirconCodec`, `ServidorGatt` con FTMS, Cycling Power y Device Information de solo lectura, `DirconServer` con el anuncio mDNS, y la potencia y cadencia del Neo como telemetría. Con eso se comprueba, por este orden:

1. Desde el Mac, en la misma red: `dns-sd -B _wahoo-fitness-tnp._tcp` lista «Square Shift» y `dns-sd -L` muestra el puerto y los registros TXT.
2. Zwift en PC lista «Square Shift» por wifi como potencia, cadencia y rodillo controlable (el icono de wifi en la lista distingue DirCon de Bluetooth).
3. Al emparejarlo, el registro muestra el descubrimiento de servicios y características, la suscripción a notificaciones y los primeros mensajes al punto de control, y Zwift muestra la potencia.

Si Zwift no lo lista o no se queda conectado tras ajustar lo que muestre el registro, se para y se replantea con el usuario si se adelanta el change 11 (`/openspec-update-change`). Para comparar sin copiar código, se puede observar con Wireshark en el PC el diálogo de Zwift con otro dispositivo DirCon (por ejemplo, la app que el usuario usa hoy, si tiene el modo wifi): se observa el protocolo, no se reutiliza su código (NF6).

- **Alternativa: hacer primero los modos de control y la UI.** Si DirCon fallara, habría trabajo hecho sobre un transporte que no sirve, y el puente pasaría por Bluetooth con otras restricciones (NF3). El riesgo está en el protocolo, así que va primero.

### 2. Protocolo DirCon (a validar)

Lo que se implementa de partida, deducido de la descripción del protocolo; cada punto se contrasta con el registro en la tarea de validación y se corrige en `arquitectura.md` § 7.3:

| Elemento | Valor de partida |
|---|---|
| Anuncio | `NsdManager.registerService`, tipo `_wahoo-fitness-tnp._tcp`, nombre «Square Shift», puerto TCP 36866 (si está ocupado, uno libre y se anuncia ese) |
| TXT | `ble-service-uuids` con los UUID cortos ofrecidos (`0x1826,0x1818,0x180A`), `mac-address` y `serial-number` estables (decisión 9) |
| Cabecera | 6 bytes: versión (1), identificador de mensaje, número de secuencia, código de respuesta, longitud del contenido (`uint16` *big endian*) |
| Mensajes | `0x01` descubrir servicios, `0x02` descubrir características (UUID de servicio → UUID y propiedades de cada una), `0x03` leer, `0x04` escribir, `0x05` activar o desactivar notificaciones, `0x06` notificación del servidor |
| UUID | 128 bits en los mensajes, también los cortos (`0000xxxx-0000-1000-8000-00805f9b34fb`) |
| Respuestas | Mismo identificador y número de secuencia que la petición; código 0 si va bien, y los de error para mensaje desconocido, servicio o característica inexistente y operación no admitida |
| Indicaciones | DirCon no las distingue: la respuesta del punto de control FTMS (`80 <op> <resultado>`) va como notificación `0x06` de `2AD9` |

`DirconCodec` (`:nucleo`) solo traduce bytes ⇄ mensajes, con tests de vectores; `DirconServer` (`:app`) solo mueve mensajes entre el socket y el `ServidorGatt`.

### 3. `ServidorGatt` en `:nucleo` y `TransporteZwift` en `:app`

`ServidorGatt` es una descripción declarativa de servicios y características (UUID, propiedades, valor de lectura o manejador de escritura) y del estado de suscripción de cada una. Responde a «descubre», «lee», «escribe» y «suscribe» y entrega las notificaciones que hay que mandar. No sabe de sockets ni de Android.

`TransporteZwift` es la interfaz común a los transportes: publicar el servidor, avisar de cliente conectado o perdido y de su dirección, entregar peticiones al hilo de control y mandar notificaciones. `DirconServer` la implementa; en el change 11, `GattServerHost` hará lo mismo. `ZwiftBridge` (`:app`) monta los servicios FTMS, Cycling Power y Device Information sobre el `ServidorGatt`, traduce las escrituras del punto de control en llamadas a la `Sesion` y produce la telemetría. Los tests del puente usan un doble de `TransporteZwift`.

- **Alternativa: `ZwiftBridge` hablando directamente DirCon.** Más corto ahora, pero el change 11 tendría que separar el modelo GATT del transporte de todos modos (`arquitectura.md` § 4.3 ya lo prevé así).

### 4. FTMS mínimo que pide Zwift

| Característica | Contenido |
|---|---|
| `0x2ACC` Feature | Cadencia y potencia medidas; objetivos de potencia y simulación de bicicleta en interior |
| `0x2AD8` Supported Power Range | 0–2000 W en pasos de 1 W (la ERG de Zwift la lee para limitar objetivos; si Zwift no la pide, se mantiene igual) |
| `0x2AD9` Control Point | `0x00` y `0x01` éxito; `0x05` potencia objetivo; `0x07` y `0x08` éxito; `0x11` simulación; el resto, «no admitido» |
| `0x2AD2` Indoor Bike Data | Velocidad, cadencia y potencia instantáneas |
| `0x2ADA` Status, `0x2AD3` Training Status | Presentes para el descubrimiento; Status no se notifica salvo que la validación muestre que Zwift lo espera |
| `0x2A63` Cycling Power Measurement | Potencia y datos de biela (revoluciones acumuladas y hora del último evento) tal como llegan del Neo |
| `0x2A65`, `0x2A5D` | Datos de biela admitidos; sensor en el buje trasero |
| `0x180A` | Fabricante, modelo «Square Shift» y versión de la app |

La velocidad de Indoor Bike Data va de partida a 0: la app no decodifica todavía los datos de rueda del CSC del Neo, y Zwift calcula la velocidad del avatar con la potencia. La validación (tarea 8.1) comprueba que Zwift no la usa; si la usara, se calcula con los datos de rueda del Neo (relacionado con la decisión abierta 4 de `funcional.md`).

### 5. Telemetría a 4 Hz con el último valor

Con Zwift suscrito, la `Sesion` notifica `2AD2` y `2A63` cada 250 ms con la última potencia, cadencia y biela del Neo (0 sin Neo), con un temporizador en el hilo de control. Es lo previsto en `arquitectura.md` § 5.2.

- Los datos del Neo llegan a ~1 Hz, así que tres de cada cuatro notificaciones repiten valores. En `2A63`, repetir la misma hora de evento de biela no suma revoluciones, así que la cadencia que calcula Zwift no se altera.
- **Alternativa: notificar al llegar cada dato del Neo (~1 Hz).** Menos tráfico, pero la frecuencia hacia Zwift dependería del Neo. **Alternativa: leer la página FE-C 0x19 (4 Hz) para tener datos nuevos.** Más fiel, pero es más protocolo que validar; se deja para cuando se eche de menos.
- La validación decide la frecuencia (decisión abierta 2): si Zwift va bien a 4 Hz, se queda; si se ve algún problema, se prueba a 1 Hz.

### 6. Modos de control con Zwift en `ControlRodillo`

`ModoControl` pasa a `SimLibre`, `SimZwift(terreno)`, `ErgZwift(objetivoW, ajusteW)` y `Manual(comando, anterior)`, como en `arquitectura.md` § 5.4 sin `ErgManual` (change 9).

- `0x11` → `SimZwift` con el terreno; `0x05` → `ErgZwift` con el objetivo, conservando el ajuste si ya estaba en ERG y con ajuste 0 si no.
- `Manual` guarda el modo anterior y lo actualiza con lo que llegue de Zwift mientras dura; al salir, se vuelve a él y se envía su comando (spec `trainer-manual-control`).
- `ControlRodillo` sigue siendo puro: recibe la hora en cada llamada y devuelve el comando que hay que enviar o `null`.
- **Limitación de terreno (`arquitectura.md` § 6.2):** un terreno nuevo se envía si difiere en 0,1 % o más del último enviado y han pasado 250 ms desde el último envío por terreno; si no han pasado, se guarda y la `Sesion` programa el envío del más reciente al cumplirse. Un cambio de marcha se envía siempre al instante con el terreno vigente y reinicia la ventana. En ERG, la potencia de Zwift se envía al llegar si cambia en 2 W o más.

### 7. Palancas en ERG de Zwift: traducción de las acciones de marcha

`Asignacion` no cambia: sigue produciendo acciones de marcha. En `ErgZwift`, la `Sesion` las pasa a una función pura del dominio que las convierte en ajustes: `MarchaMas(n)` y `MarchaMenos(n)` → ±10·n W; `MarchaReferencia` → ajuste 0 («Potencia de Zwift»); `MarchaMasDura` y `MarchaMasLigera` → nada. El resultado se recorta para que el objetivo quede entre 50 W y 1000 W (los límites del control manual) y devuelve el cambio real, el tope alcanzado o nada, igual que `ResultadoCambio`. La UI recibe un evento nuevo (`EventoRodaje.Ajuste` o `TopePotencia`) para el aviso y la vibración, que es la misma que en un tope de marcha.

- Así el change 7 (asignación configurable) y el 9 (ERG manual con la cruceta) se apoyan en el mismo punto: la acción depende del modo, no del botón.
- **Alternativa: una acción `ErgMas(w)` directamente en `Asignacion` según el modo.** Mezclaría el modo de control con la tabla de botones, que no debe conocerlo (`arquitectura.md` § 5.3).

### 8. Zwift desconectado: 30 s con lo último y vuelta a `SimLibre`

Al perder el cliente (cierre del socket, error de escritura, pérdida del wifi o un cliente nuevo que lo sustituye), la `Sesion` marca el modo como «en espera» y arranca un temporizador de 30 s; si Zwift vuelve y manda terreno o potencia, se cancela y se sigue; si vence, `SimLibre` con la marcha puesta (decisión abierta 1 de `arquitectura.md` § 15, provisional hasta la validación).

- 30 s cubren un corte de wifi breve o un reinicio del router sin que la resistencia salte al llano en mitad de una subida, y son poco tiempo para quedarse en una potencia fija si se ha cerrado Zwift.
- Un cliente nuevo sustituye al anterior: si Zwift se reconecta tras un corte en el que el socket viejo quedó medio abierto, no hay que esperar a que este caduque.

### 9. Identidad estable del puente en `estado.json`

`mac-address` y `serial-number` del TXT se generan una vez (dirección local administrada aleatoria) y se guardan en `estado.json` como `puenteZwift` opcional. Así Zwift reconoce el mismo dispositivo entre sesiones y se reconecta solo (spec `zwift-bridge`, «Zwift vuelve a otra sesión»).

- Es un campo opcional nuevo con valor por defecto: los documentos de la versión 2 se leen igual, así que no se sube la versión ni hace falta migración. Si falta, se genera al abrir y se guarda.
- **Alternativa: derivarlos del `ANDROID_ID`.** No hace falta un dato del dispositivo para algo que solo tiene que ser estable en este móvil.

### 10. Red: wifi, `WifiLock` y re-anuncio

- Un `ConnectivityManager.NetworkCallback` de redes wifi da a la `Sesion` si hay wifi. Sin wifi, Zwift queda «sin conexión» y el servidor se para; al volver, se arranca y se vuelve a anunciar (`arquitectura.md` § 8).
- Mientras hay sesión y wifi, se mantiene un `WifiLock` de baja latencia (`WIFI_MODE_FULL_LOW_LATENCY` desde Android 10), para que el ahorro de energía con la pantalla apagada no retrase la telemetría ni el terreno (NF4).
- No se pide `MulticastLock`: el anuncio lo hace el servicio mDNS del sistema. Si la prueba con la pantalla apagada muestra que el anuncio desaparece, se añade.
- Permisos nuevos en el manifiesto: `INTERNET`, `ACCESS_NETWORK_STATE`, `ACCESS_WIFI_STATE`, `CHANGE_WIFI_MULTICAST_STATE` y `WAKE_LOCK` (`arquitectura.md` § 12). Ninguno es de tiempo de ejecución. El servicio sigue siendo `connectedDevice`.
- El servidor TCP corre en `Dispatchers.IO`: un bucle de lectura que entrega cada mensaje por un `Channel` al hilo de control, y un escritor único que serializa las respuestas y notificaciones (`TCP_NODELAY`). Ninguno toca el estado de la `Sesion` (`arquitectura.md` § 6.1).

### 11. Estado de Zwift en la sesión y en la UI

`EstadoRodaje` añade el estado de Zwift (`SinWifi`, `Esperando`, `Conectado(direccion)`), el modo de control visible (`Sim`, `SimZwift`, `ErgZwift`, `Manual`, y si está en espera), el terreno y la potencia de Zwift, el ajuste y el último mensaje de Zwift con su hora. `Simulacion` (Diagnóstico) crece con esos datos.

- `RodajeUiState` añade el chip de Zwift (`Ok`, `Esperando` con «esperando», `Sin` con «sin conexión»), el modo y, en ERG, la potencia objetivo, la de Zwift y el ajuste; el aviso pasa a ser de marcha o de vatios. La zona de potencia objetivo se pinta en `Rodaje` con las clases de `RideScreen` (`sq-erg-hero`, `sq-target`, `sq-badge-erg`); si la maqueta vertical necesita algo que `components.css` no tiene, se añade a `design-system/` antes.
- Resuelto en la maqueta (tareas 1.2 y 2.2): en vertical, «Neo conectando», «Square conectando» y «Zwift esperando» no caben en una fila a 360 dp, así que la fila de chips reserva el alto de dos líneas (`.sq-ride--v .sq-ride__conns`) para que nada se mueva al pasar de una a dos. El fondo del ajuste ERG (`sq-badge-erg`) pasa de `color-mix` a un token nuevo, `erg-soft` (6,1:1 en rodaje y 4,7:1 en claro con `erg`, comprobado por `contraste.py`), porque Compose necesita un color concreto en `Tokens.kt`.
- `RideService` compone el texto de la notificación con «Neo, Square y Zwift conectados» y «ERG 240 W» (spec `ride-session`).
- `RegistroSesion` añade el origen `Zwift`: las tramas GATT de Zwift se registran con su característica corta (`2ad9`, `2ad2`, `2a63`) y en `Rx`/`Tx`; los mensajes DirCon que no son de una característica (descubrimiento, suscripción) van como sucesos. Diagnóstico muestra «Zwift» y «→ Zwift».
- La telemetría hacia Zwift suma unas 8 entradas por segundo al registro. Con 10 000 entradas quedan unos 20 minutos en memoria; para la prueba de 2 h se usa el espejo de `adb logcat`, como en el change 5.

### 12. Validación con el equipo real

Con la skill `desplegar-movil` y Zwift en el PC del usuario, en la misma red. Cada paso se anota en este `design.md`:

1. DirCon (decisión 1).
2. Ruta con subidas: la resistencia sigue al terreno, las marchas suman y la latencia de la palanca se mantiene (spec `zwift-bridge`, «Latencia de la palanca con Zwift»); el Square sigue emparejado en Zwift para girar con X y Círculo a la vez que la app lo usa (`arquitectura.md` § 2, a validar).
3. Entrenamiento ERG: el Neo mantiene la potencia a dos cadencias, las palancas ajustan ±10 W y, al terminar, vuelve la marcha.
4. Desconexión: cerrar Zwift, cortar el wifi del móvil 10 s y 1 min, y comprobar los 30 s y la reconexión sola.
5. Sesión de 2 h en Zwift con la pantalla apagada, sin cargador y la optimización de batería por defecto (spec `ride-session`); apagar y encender el Square a la hora; revisar en el registro cortes del servicio, del wifi o del anuncio, y la batería del Square al principio y al final (para el umbral del change 10). Si el sistema corta algo, se decide con el usuario la exención de batería (decisión abierta 3), con maqueta, antes de implementarla.
6. Opcional: Zwift en Apple TV; si falla, se anota como pendiente para el change 11.

## Validación con el equipo real

### DirCon con Zwift en PC (tarea 4.8, 26-10-2026)

- Desde el Mac, `dns-sd -B` lista «Square Shift» y `dns-sd -L` lo resuelve en el puerto 36866 con `serial-number`, `mac-address` (`AA:BB:CC:00:00:03`, formato con dos puntos) y `ble-service-uuids=0x1826,0x1818,0x180A`. Un cliente DirCon de prueba desde el Mac descubre, lee, se suscribe y escribe en el punto de control por la red real.
- Zwift en PC lista «Square Shift» con el icono de wifi como potencia, cadencia y rodillo controlable, y se empareja en las tres. No hubo que corregir nada del protocolo de partida (decisión 2): cabecera, identificadores, UUID de 128 bits, propiedades (`2AD9` = 0x06) y respuesta del punto de control como notificación `0x06` sirven tal cual.
- Secuencia de Zwift al emparejar: descubre los tres servicios y las características de cada uno; lee `2ACC`; se suscribe a `2AD9`, `2AD2`, `2ADA` y `2A63`; lee fabricante, modelo y versión; y por el punto de control manda `00` (pedir control), `01` (reiniciar), `00` e `07` (iniciar), todos respondidos con `80 <op> 01`. No lee `2AD8` ni `2AD3`. Desde ahí recibe la telemetría a 4 Hz.
- El wifi se detecta al abrir la app (`Wifi: 192.168.0.11` en el registro) y el anuncio sale en menos de un segundo. Apagar y encender el wifi del móvil queda para la tarea 8.3, porque corta la depuración inalámbrica.

### Ruta con subidas en Zwift para PC (tarea 8.1, 26-10-2026)

- La resistencia sigue al terreno de Zwift y cada marcha la endurece o la afloja de forma perceptible, según el usuario.
- Diagnóstico muestra «SIM · Zwift», la pendiente de Zwift (2,4 %), el terreno (Crr 0,0040, Cw 0,51 kg/m, viento 0) y el último mensaje con su hora. Latencia de la palanca con Zwift: 30 ms (sin Zwift, mediana de 62 ms en `marchas-sin-zwift`). El terreno no retrasa la marcha.
- Zwift manda la simulación cada pocos segundos con cambios de centésimas (2,44 %, 2,45 %): por debajo del umbral de 0,1 % no se reenvían al Neo, como se esperaba (decisión 6).
- Potencia y cadencia en Zwift siguen a las del Neo sin saltos ni cortes con la telemetría a 4 Hz: se queda en 4 Hz (decisión abierta 2 de `arquitectura.md` § 15, resuelta).
- El avatar se mueve normal con la velocidad de Indoor Bike Data a 0: Zwift la calcula con la potencia y no hace falta decodificar los datos de rueda del Neo (decisión 4).
- Tras reinstalar la app (los tests instrumentados borran sus datos y con ellos la identidad del puente), Zwift se reconectó solo en 4 s a «Square Shift» con una dirección nueva en el TXT: parece reconocerlo por el nombre.
- Con el Square emparejado también en Zwift como mando, X y Círculo giran en Zwift mientras las palancas cambian de marcha en la app: el Square admite las dos conexiones a la vez (`arquitectura.md` § 2, validado). Se mantiene así hasta el mando emulado (decisión abierta 1 de `funcional.md`).
- Propuesta del usuario: mostrar en Rodaje cuándo se gira (X o Círculo mantenido). Queda fuera de este change; se apunta en `planificacion.md` para el change 10, con maqueta.
- Visto de paso en Diagnóstico: el origen «→ Zwift» no cabía en la columna de 6 caracteres del tráfico; pasa a 7 en `sq-hex-row` y en `HexLog`.

## Risks / Trade-offs

- [Zwift no lista el dispositivo o se desconecta por un detalle del protocolo sin especificación] → Validación primero (decisión 1), con el registro de cada mensaje en los dos sentidos y la observación con Wireshark de otro dispositivo DirCon; si no se resuelve, se para y se replantea con el usuario.
- [El router aísla los clientes de la red o bloquea mDNS] → Diagnóstico muestra «Esperando» con la dirección del móvil en el registro; `desarrollo.md` explica cómo comprobarlo con `dns-sd` desde el Mac. Es una limitación de la red que el Bluetooth (change 11) resuelve.
- [Con la pantalla apagada, el sistema del OnePlus 8 duerme el wifi o retira el anuncio] → `WifiLock` de baja latencia y prueba de 2 h; si no basta, exención de batería o `MulticastLock`, con el usuario.
- [La dificultad del rodillo de Zwift (por defecto, 50 %) escala la pendiente y hace que las marchas pesen más en proporción] → Se avisa al usuario en la validación; la recomendación final se decide en el change 15 (`arquitectura.md` § 7.3).
- [El Square no admite a la vez la app y Zwift] → Se comprueba en la validación; si no, el giro espera al mando emulado (changes 12 y 13) y se actualiza la decisión abierta 1 de `funcional.md`.
- [Las notificaciones a 4 Hz llenan el registro en memoria] → Aceptado: el registro de la prueba larga sale por `adb logcat`, y guardar el registro llega en el change 16.
- [Versiones futuras de Android exigen permiso para la red local al subir `targetSdk`] → Con el `targetSdk` actual no aplica; se anota en `arquitectura.md` § 12 para cuando se suba.
- [Un ERG de Zwift se queda 30 s tras cerrar Zwift] → Es la contrapartida de no saltar al llano en un corte breve; 30 s es corto y el usuario lo ve en el chip.

## Migration Plan

Sin migración de datos: `puenteZwift` es un campo opcional de `estado.json` que se genera si falta. Para volver atrás basta con instalar la versión anterior; el campo se ignora.

## Open Questions

- Si Zwift necesita `0x2ADA` (Status) notificado tras cada cambio de objetivo para no repetir comandos. Se ve en el registro de la validación y no cambia el diseño: es una notificación más del mismo servidor.
- Valor exacto de la frecuencia de telemetría y del tiempo de espera tras la desconexión, si la validación muestra problemas con 4 Hz o con 30 s.
