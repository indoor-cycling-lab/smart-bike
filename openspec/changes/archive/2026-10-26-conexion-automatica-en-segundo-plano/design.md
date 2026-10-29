## Context

Tras los changes 4 y 4b, la `Sesion` (en `AppGraph.hiloControl`) es la dueña del estado de rodaje y de las conexiones, y el `RideService` (`connectedDevice`) la mantiene viva con la pantalla apagada y muestra la notificación. Lo que hay hoy y condiciona el diseño:

- `ClienteBle.conectar(direccion)` hace una conexión directa (`useAutoConnect(false)`, 3 reintentos, 15 s). `EstadoEnlace` distingue `Perdida` (corte no pedido) de `Fallo` y `Desconectado`.
- `Sesion.buscar()` busca por prefijo de nombre con `BleScanner` (sin `ScanFilter`, 30 s) y conecta al primero. Solo guarda la dirección del Neo (`EstadoGuardado.direccionRodillo`), y al encontrarlo, no al conectar. Nadie la lee.
- `EstadoDispositivo` tiene `Perdida`, que se queda así hasta que el usuario busca. Con el Bluetooth apagado, los conectados pasan a `Perdida`.
- `EventosBoton.perdida()` ya libera los botones al perder el Square, y al pasar el Neo a `Listo` la `Sesion` le reenvía el comando de la marcha (`ControlRodillo.neoListo`).
- La `Sesion` no sabe si `BLUETOOTH_CONNECT` está concedido: lo comprueba `MainActivity.onResume` solo para arrancar el servicio.

Motivo y alcance: `proposal.md`. Comportamiento: las specs delta de este change.

## Goals / Non-Goals

**Goals:**

- Una sola política de conexión en la `Sesion`, probada en la JVM con dobles y reloj virtual, que cubra la apertura, la pérdida, el Bluetooth que vuelve y la búsqueda manual.
- Reconectar con la pantalla apagada durante horas sin gastar batería del móvil en búsquedas.
- Validar con el equipo real lo que la política da por supuesto (decisión 8).

**Non-Goals:**

- Cambiar `BleScanner` o la búsqueda manual, salvo que recuerde lo que conecta.
- Reconexión hacia Zwift (change 6): allí la app no conecta, publica y espera (`arquitectura.md` § 8).
- Pedir la exención de optimización de batería si la prueba de 2 h no la exige (decisión 7).

## Decisions

### 1. Dispositivos recordados en `estado.json`, versión 2

`EstadoGuardado` pasa a la versión 2 con `rodillo` y `mando`, cada uno un `DispositivoRecordado(direccion, nombre)` opcional, y deja `direccionRodillo`. La migración de la versión 1 lleva `direccionRodillo` a `rodillo` con el nombre `Tacx Neo`. Así, el móvil de pruebas, que ya la tiene guardada desde el change 4, se conecta al Neo en la primera apertura con esta versión. El Square se recuerda al conectarlo por primera vez.

- Se guarda al llegar a `Listo`, no al encontrarlo en la búsqueda como ahora. Así no se recuerda un dispositivo que se ve pero al que no se puede conectar (por ejemplo, otro Square con un servicio distinto, que da `Fallo`).
- Uno de cada tipo: el último que llega a `Listo` sustituye al anterior (spec `device-connection`, «Recordar los dispositivos»).
- **Alternativa: añadir `nombreRodillo`, `direccionMando`… sin cambiar de versión.** Funciona con `ignoreUnknownKeys`, pero deja un campo suelto por dato. La versión 2 con su migración probada sigue lo previsto en `arquitectura.md` § 10 y prepara el terreno para F12.1 (olvidar).

### 2. Conexión directa primero, `autoConnect` después

Cada intento de la `Sesion` con un dispositivo recordado tiene dos pasos:

1. **Directa** (`conectar(direccion)`, como hoy): si el dispositivo está despierto, conecta en 1–2 s.
2. Si falla, **en segundo plano** (`conectarEnSegundoPlano(direccion)`): `connect(device).useAutoConnect(true)` de Nordic, sin límite de tiempo. Android pone la dirección en su lista de aceptación y conecta en cuanto el dispositivo se anuncia, también con la pantalla apagada y sin que la app busque. La llamada vuelve al conectar y se cancela con `desconectar()`.

Este intento se usa al abrir la app (estado `Conectando`), al perder un dispositivo (`Reconectando`) y al volver el Bluetooth (`Conectando`). Mientras dura, la `Sesion` no reintenta nada más: el segundo paso no vence.

- **Alternativa: reintentos directos con espera creciente (1, 2, 4… 30 s)**, como decía `arquitectura.md` § 8. Cada conexión directa escanea de forma agresiva hasta su límite. Con el Square sin batería durante una hora, se repetiría sin parar. `autoConnect` hace lo mismo con el escaneo de bajo consumo del propio sistema, sin temporizadores en la app. Se actualiza § 8.
- **Alternativa: buscar por nombre en cada reintento.** Desde Android 8.1, las búsquedas sin `ScanFilter` se paran con la pantalla apagada, y el prefijo de nombre no se puede expresar como filtro. Habría que filtrar por UUID de servicio y, además, Android limita a 5 los inicios de búsqueda cada 30 s. `autoConnect` evita las dos cosas, pero exige que la dirección no cambie (riesgo 1).
- La directa va primero porque `autoConnect` de Android puede tardar varios segundos más en un corte breve, y porque la biblioteca de Nordic recomienda no usar `autoConnect` en el primer intento con un dispositivo cuya caché GATT no se conoce.
- **Detalles de Nordic vistos en el equipo real (tarea 8.2).** `useAutoConnect(true)` hace por defecto otra conexión directa antes del `autoConnect` (`autoConnectCreateDirectConnectionFirst`); se pide `useAutoConnect(true, false)`, porque la directa ya la ha hecho la `Sesion`. Además, si la petición en segundo plano llega antes de que Nordic cierre el enlace de la directa fallida (unos milisegundos), se pierde sin fallar ni conectarse. `ClienteBle` espera a que el enlace quede `Disconnected`, como mucho 2 s, antes de pedirla.

### 3. Estados: `Conectando` y `Reconectando` sustituyen a `Perdida`

`EstadoDispositivo` pasa a `SinConexion`, `Buscando`, `Conectando`, `Reconectando`, `Conectado` y `NoEncontrado`. `Perdida` desaparece: un dispositivo conectado siempre está recordado, así que una pérdida pasa directamente a `Reconectando`. `EstadoEnlace.Perdida` se queda en `ble`: es lo que le dice a la `Sesion` que empiece el intento.

| Situación | Estado | Rodaje (chip · pie) | Diagnóstico (fila · aviso) |
|---|---|---|---|
| Nada recordado | `SinConexion` | sin conexión · «Toca Neo o Square…» | Sin conexión · — |
| Intento al abrir o con el Bluetooth de vuelta | `Conectando` | conectando · ayuda para despertarlo | Conectando + ayuda · — |
| Intento tras una pérdida | `Reconectando` | conectando · ayuda para despertarlo | Reconectando + ayuda · informativo |
| Búsqueda manual | `Buscando` | conectando · ayuda para despertarlo | Buscando + ayuda · — |
| Búsqueda sin resultado | `NoEncontrado` | sin conexión · «Toca Neo o Square…» | No encontrado · error |
| Bluetooth apagado | `SinConexion` | sin conexión · «Toca Neo o Square…» | Sin conexión · error de Bluetooth |

- La ayuda del pie de Rodaje depende de qué dispositivos están en `Buscando`, `Conectando` o `Reconectando`: el Neo («Pedalea para despertar el Neo»), el Square («Pulsa un botón del Square para despertarlo») o los dos («Pedalea y pulsa un botón del Square para despertarlos»). Si además falta otro sin intento, prima la ayuda, porque el intento se resuelve solo. `RodajeUiState` sustituye `faltaAlguno` por un `pie` con esos casos.
- El chip no distingue `Conectando` de `Reconectando`: los dos dicen «conectando» con el anillo de espera de `ConnectionChip`, que ya existe. La diferencia solo importa en Diagnóstico, donde se explica qué pasó.
- En vertical a 360 dp, «Neo conectando» y «Square conectando» no cabían en una fila (325 de 312 px) y bajaban la pantalla 42 px. En `.sq-ride--v`, `sq-conn` reduce su relleno lateral a `space-2` (309 px), decidido con el usuario al hacer la maqueta. El composable de Rodaje en vertical lo reproduce.
- El aviso informativo de Diagnóstico usa `Notice` sin `is-danger` (icono `info`), ya en el sistema de diseño. Un corte que se recupera solo no es un error que pida hacer algo.
- Con el Bluetooth apagado, las filas pasan a «Sin conexión» en lugar de «Conexión perdida», que ya no existe. El aviso de error de Bluetooth sigue diciendo qué pasa.

### 4. Búsqueda manual con un dispositivo recordado

«Buscar dispositivos» sigue disponible mientras hay un intento en curso. Al tocarlo, la `Sesion` cancela el intento de los que no están `Conectado` (`desconectar()`), los pasa a `Buscando` y busca como hoy. Si encuentra uno, se conecta a él y, al llegar a `Listo`, lo recuerda (decisión 1). Si no encuentra ninguno en 30 s, pasa a `NoEncontrado` y la app deja de intentarlo: el usuario ha tomado el control y el aviso de error le dice qué hacer. Vuelve a intentarlo sola al volver el Bluetooth o en la siguiente apertura de la app.

- **Alternativa: volver a `Reconectando` tras una búsqueda fallida.** El aviso de error desaparecería sin que el usuario sepa si la búsqueda sirvió, y la spec de «Dispositivo no encontrado» dejaría de cumplirse.

### 5. Permiso y Bluetooth como entradas de la `Sesion`

`EstadoBluetooth` gana `permiso: StateFlow<Boolean>`, que `AdaptadorBluetooth` vuelve a comprobar cuando `MainActivity.onResume` llama a `AppGraph.comprobarPermisos()`, el mismo punto que hoy arranca el servicio. La `Sesion` empieza el intento con los recordados cuando se cumplen tres cosas: ha leído `estado.json`, tiene el permiso y el Bluetooth está encendido. Lo vuelve a empezar al pasar el Bluetooth de apagado a encendido. Sin permiso no intenta nada, y Diagnóstico sigue mostrando su aviso.

- **Alternativa: que `MainActivity` llame a `Sesion.conectarRecordados()`.** Repartiría la política entre la `Activity` y la `Sesion`, y los tests de la `Sesion` no podrían reproducir «se concede el permiso más tarde».

### 6. Notificación

`RideService.textoConexiones` pasa a considerar el intento. Con los dos conectados dice «Neo y Square conectados». Si alguno está en `Conectando`, `Reconectando` o `Buscando`, dice «Conectando el Neo», «Conectando el Square» o «Conectando el Neo y el Square». Si no, se queda como hoy («Neo sin conexión», «Square sin conexión», «Neo y Square sin conexión»). El servicio ya observa la marcha y las dos conexiones con `distinctUntilChanged`, así que no cambia nada más.

### 7. NF4 y la exención de optimización de batería

Primero se prueba sin pedir nada: el servicio `connectedDevice` en primer plano y `autoConnect`, que no necesita `WAKE_LOCK` porque las conexiones y su reconexión las mantiene la pila Bluetooth del sistema. La prueba (tarea 8.4) es una sesión de 2 h en el OnePlus 8 con la pantalla apagada, sin cargador y con la optimización de batería de la app en su valor por defecto («Optimizada» en OxygenOS), con un corte provocado del Square a la hora.

- Si pasa, la decisión abierta 3 de `arquitectura.md` § 15 se cierra con «no hace falta» en los móviles de prueba.
- Si el sistema corta el servicio, retrasa la reconexión o deja de entregar notificaciones, se para la implementación. Se decide con el usuario si se pide `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` o se guía a los ajustes del sistema. Las dos opciones necesitan pantalla, así que pasarían por maqueta, y se actualiza este change (`/openspec-update-change`) antes de seguir.

### 8. Validación con el equipo real

Lo **a validar** que da por supuesto la decisión 2, con la sesión guiada de `desarrollo.md` § 3 y el registro (`adb logcat -s SqTrama`):

| Supuesto | Prueba | Si falla |
|---|---|---|
| El Square y el Neo mantienen su dirección al apagarse y encenderse (sin direcciones privadas aleatorias) | Apagar y encender cada uno y comparar la dirección del anuncio en el registro antes y después | Guardar el nombre completo y, al fallar la directa, buscar con `ScanFilter` por UUID de servicio con la pantalla encendida. Se replantea con el usuario |
| `autoConnect` reconecta tras un corte en un tiempo razonable | Medir en el registro, del anuncio o del encendido a `Listo`: Square apagado y encendido, Neo desenchufado y enchufado, con la pantalla encendida y apagada. Objetivo orientativo: menos de 10 s desde que se despierta | Probar reintentos directos espaciados (alternativa de la decisión 2) y comparar |
| Una pérdida no deja la marcha ni el otro dispositivo en mal estado | Apagar el Square a mitad con la marcha 17 y con Campag. D mantenido; ver que el Neo sigue, que la marcha no cambia y que, al volver, D1 sube una | — (ya cubierto por tests; es la prueba que quedó pendiente en los changes 3 y 4) |
| La prioridad alta sostenida no agota el Square | Leer su batería (`0x2A19`) al conectar y cada 10 min durante la prueba de 2 h, al registro | Se decide con el usuario si se limita la prioridad sostenida (por ejemplo, solo mientras se pedalea) en otro change |

- La batería del Square se lee con una lectura periódica en `SquareClient`, no con una suscripción: el Square notifica `0x45` y `0x43` sin parar, y no hace falta más tráfico. Solo va al registro (fuera de alcance mostrarla).

## Risks / Trade-offs

- [El Square o el Neo cambian de dirección al reiniciarse] → `autoConnect` no los encontraría nunca. Se valida lo primero en la sesión guiada (decisión 8) y, mientras tanto, «Buscar dispositivos» los recupera y los vuelve a recordar.
- [`autoConnect` tarda en algunos móviles (escaneo de bajo consumo)] → La conexión directa va primero y cubre los cortes breves con el dispositivo despierto. Se mide el tiempo con el equipo real.
- [Un intento en segundo plano «colgado» que nunca vuelve] → Se cancela al buscar a mano, al apagar el Bluetooth y al terminar la sesión. Si en las pruebas se queda sin conectar con el dispositivo ya anunciándose, se añade un relanzamiento periódico (por ejemplo, cada 5 min), que no cambia las specs.
- [El Neo recibe la resistencia de la marcha al reconectar en mitad de una subida] → Es lo que piden las specs (F1.3) y ya se hace al pasar a `Listo`. Con Zwift (change 6) el comando vigente incluirá el terreno.
- [OxygenOS corta el servicio en segundo plano] → Decisión 7: se detecta en la prueba de 2 h y se para antes de añadir pantallas.
- [Reconexión del Square mientras se mantiene un botón] → La primera notificación tras reconectar fija la línea base y un botón ya pulsado se considera pulsado (spec `square-buttons`). No hay pulsaciones fantasma, pero la que se hizo durante el corte se pierde. Aceptado.

## Migration Plan

- `estado.json` de la versión 1 se migra al leerlo (decisión 1), con test de ida y vuelta de un fichero de ejemplo de la versión 1. Un documento ilegible sigue sustituyéndose por los valores por defecto.
- Volver atrás a la versión anterior de la app: lee la versión 2 con `ignoreUnknownKeys`, pierde los dispositivos recordados y conserva la marcha. No hace falta más.

## Resultados con el equipo real

OnePlus 8 (Android 13), Tacx Neo 12345 y Elite Square.

- **Sesión guiada (tarea 8.1, 26-10-2026):** primer uso con «Buscar dispositivos». `estado.json` queda en la versión 2 con el Neo (`AA:BB:CC:00:00:01`, «Tacx Neo 12345») y el Square (`AA:BB:CC:00:00:02`, «SQUARE»). Al terminar la sesión y abrir la app, los dos se conectan solos con la conexión directa, sin búsqueda: el Neo llega a `Listo` en 1,6 s y recibe la marcha guardada (12, pendiente 0 %), y el Square en 2,4 s. La batería del Square se lee al conectar (100 %).
- **Batería del Square (tarea 5.5):** se lee al conectar y queda en el registro. Justo después de encenderlo da 0 %; en las conexiones sin reinicio da 100 %. La lectura al conectar tras un encendido no vale como medida: para la prueba de 2 h cuentan las lecturas periódicas.
- **Estabilidad de la dirección y reconexión del Square (tarea 8.2, pantalla encendida):** en cuatro apagados y encendidos, el Square se anuncia siempre con la misma dirección (`AA:BB:CC:00:00:02`, aleatoria estática). Encendido antes de 15 s, lo recupera la directa (8,6 s desde el corte). Apagado más tiempo, la directa falla a los 15 s y `autoConnect` lo conecta en cuanto se anuncia: de la conexión a `Listo`, 1,5 s. Las dos primeras pruebas no reconectaron por los dos detalles de Nordic de la decisión 2, ya corregidos.
- **Botón mantenido (tarea 8.3):** el Square solo se apaga pulsando a la vez los dos Campag. y dos palancas, así que siempre se apaga con Campag. mantenido; por eso la marcha cambia al apagarlo (en la prueba, de la 17 a la 19). Durante el corte, el Neo sigue conectado y la marcha no cambia. Al volver el Square, una D1 sola sube una marcha (de la 19 a la 20).
- **Neo y pantalla apagada (tarea 8.2):** el Neo desenchufado sigue el mismo camino que el Square; enchufado, conecta en unos 3 s y llega a `Listo` en unos 5 s contando su arranque, y recibe sola la resistencia de la marcha puesta. Con la pantalla apagada, el Square (encendido hacia las 09:21:00) llega a `Listo` a las 09:21:03 y el Neo, unos 3 s después de conectar. Una D1 con la pantalla apagada cambia la marcha y llega al Neo. Las direcciones no cambian. Medidas por debajo del objetivo de 10 s: se mantiene la decisión 2.
- **Capturas (tarea 7.4):** Rodaje en vertical con el Square reconectando y con el Neo reconectando (igual que «Neo dormido al abrir»), y Diagnóstico con el Square reconectando, coinciden con las maquetas aprobadas.
- **Pendiente fuera de este change:** la combinación que apaga el Square (los dos Campag. con las palancas I1 y D1) se reconoce como gestos y cambia la marcha (−1 y +3).
