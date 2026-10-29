## Context

Tras el change 3, `AppGraph` crea `SquareClient`, `NeoClient`, `BleScanner`, `RegistroSesion` y la orquestación provisional `ConexionesDiagnostico`, que busca y conecta, decodifica los botones de `0x45` con `EventosBoton` y envía el control manual. Las conexiones viven mientras la `MainActivity` está abierta, sin servicio. Diagnóstico es la única pantalla. `NeoClient` ya tiene la ranura única de comando pendiente (canal `CONFLATED`, el último gana) con un límite de escritura de 1 s. La telemetría del Neo (`0x2A63`, `0x2A5B`) solo se registra en crudo. Nada se guarda entre aperturas.

Lo validado en el change 3 que condiciona este diseño: el Square y el Neo terminan en un intervalo de conexión de 187,5 ms. Volver a pedir prioridad alta lo baja a 12,5 ms solo unos 5 s, con un máximo de 3 reintentos, y una escritura con respuesta en `fec3` tarda más de 300 ms en confirmarse (`arquitectura.md` § 6.3). Motivo y alcance: `proposal.md`. Comportamiento: las specs de este change.

## Goals / Non-Goals

**Goals:**

- `Sesion` como único dueño del estado de rodaje, con la forma que ampliarán los changes 5–10 (reconexión, Zwift, gestos, ERG) sin rehacerla.
- Camino caliente de `arquitectura.md` § 5.1 completo y medido por tramos, sin saltos de hilo entre la notificación del Square y la escritura en el Neo.
- Datos objetivos de latencia con el equipo real, para decidir NF1 con el usuario.

**Non-Goals:**

- Reconexión automática y dispositivos recordados (change 5): la máquina de estados de § 8 sigue sin `Reconectando`.
- `configuracion.json` y su exportación: la configuración tiene modelo y valores por defecto en el dominio, pero no se guarda hasta que haya UI para cambiarla (changes 8 y 10).
- Reconocedor de gestos con temporizadores (change 7).

## Decisions

### 1. `Sesion` sustituye a `ConexionesDiagnostico` y la crea `AppGraph`

La lógica de búsqueda, estados de conexión y Bluetooth de `ConexionesDiagnostico` pasa a `sesion/Sesion`, que además lleva el dominio de rodaje. Sus tests (`ConexionesDiagnosticoTest`) pasan a `SesionTest`, con los mismos dobles de `ConexionSquare` y `ConexionNeo`. `AppGraph` crea la `Sesion` al empezar la sesión (al abrir la app) y la publica como `StateFlow<Sesion?>`. Al terminar, la descarta y la siguiente apertura crea otra.

- **Alternativa: que la cree el `RideService`** (`arquitectura.md` § 4.4). Desde Android 14, un servicio en primer plano de tipo `connectedDevice` exige tener ya concedido `BLUETOOTH_CONNECT` al llamar a `startForeground`. En el primer uso ese permiso se pide en Diagnóstico, después de abrir la app, así que la `Sesion` tiene que existir antes que el servicio. Por eso el servicio no es el dueño de la `Sesion`, solo la mantiene viva (decisión 2). Se actualiza § 4.4.
- La `Sesion` corre en `AppGraph.hiloControl` (§ 6.1). Expone `estado: StateFlow<EstadoRodaje>` (conexiones, marcha, telemetría, último gesto, simulación, latencia) y `eventos: SharedFlow<EventoRodaje>` (cambio, tope), que la UI usa para el aviso y la animación (§ 9.2). La vibración la lanza la propia `Sesion` a través de una interfaz `Vibrador` de `:app`, para que funcione con la pantalla apagada.

### 2. `RideService`: primer plano y notificación, sin lógica

`RideService` (tipo `connectedDevice`) arranca en cuanto `BLUETOOTH_CONNECT` está concedido: al abrir la app si ya lo estaba, o al concederlo en Diagnóstico. Antes no hay conexiones que mantener. Observa `Sesion.estado` con `distinctUntilChanged` sobre la marcha y las dos conexiones, y actualiza la notificación (canal «Sesión», importancia baja, sin sonido). Su acción «Terminar» y Atrás desde Rodaje llaman a `Sesion.terminar()`. Esta guarda la marcha y envía pendiente 0 % esperando la confirmación como mucho 500 ms, como ya hacía `ConexionesDiagnostico`. Después desconecta, detiene el servicio y cierra la `Activity` si está abierta. Se ejecuta en `ambitoApp`, no en el de un `ViewModel`.

- `POST_NOTIFICATIONS` se pide una sola vez, al abrir Rodaje por primera vez en Android 13 o superior. Que ya se pidió se guarda en `estado.json`. Si se deniega, el servicio sigue en primer plano sin notificación visible.
- **Alternativa: *bound service*.** Innecesario: todo corre en el mismo proceso (§ 4.4).

### 3. Camino caliente en el hilo de control

```
SquareClient (callback) ─Channel─▶ Sesion: SquareDecoder.contadores ─▶ EventosBoton ─▶ ReconocedorGestos
   ─▶ Asignacion ─▶ Cambio ─▶ ModeloSimple ─▶ ControlRodillo ─▶ FecCodec ─▶ NeoClient.enviar (ranura única)
```

Todo ocurre en una sola corrutina del hilo de control, sin `suspend` entre la notificación y `NeoClient.enviar`. La notificación ya lleva la hora monotónica de llegada (`tMs`) desde el *callback*. El estado, los eventos, el registro y la vibración se actualizan después de entregar el comando.

### 4. `ReconocedorGestos` y `Asignacion` provisionales, con la forma definitiva

- `ReconocedorGestos` (`:nucleo/dominio`) solo produce `Gesto.Press(boton, modificadores, tMs)` en cada `Pulsado`. `modificadores` son los Campagnolo que ya estaban pulsados, sacados de la máscara del evento sin el propio botón. No tiene temporizadores. El change 7 añade `Tap`, `LongPress`, `DoubleTap`, `Repeat`, `Hold` y `Chord` detrás de la misma salida.
- `Asignacion` (`:nucleo/dominio`) traduce un gesto en una `Accion` o en nada, con una tabla fija: el subconjunto del anexo A de `funcional.md` que resuelve este change. Solo cuenta el Campagnolo del mismo lado que la palanca, y una palanca con modificador no produce además su ±1. □ produce `MarchaReferencia` con `Press`. Cuando el change 7 le asigne también la pulsación larga, □ pasará a esperar a saber qué gesto es (F3.4), y eso se acepta. El change 7 sustituye la tabla por el perfil configurable, con esta como parte de la asignación por defecto.
- `Accion` (§ 5.3) se define completa para el grupo de marchas (`MarchaMas(n)`, `MarchaMenos(n)`, `MarchaMasDura`, `MarchaMasLigera`, `MarchaReferencia`). Los otros grupos llegan con sus changes.

### 5. `Transmision`, `Cambio` y `EstadoMarcha`

- `Transmision.Lineal(marchas = 24)` y `Cambio`, puro y sin estado global. `Cambio` aplica una `Accion` a un `EstadoMarcha` y devuelve `Cambiada(anterior, nueva)`, `Tope(extremo)` o `SinCambio`. Un salto que sobrepasa el extremo se recorta y cuenta como `Cambiada`. Solo es `Tope` si ya estaba en el extremo. El aviso usa la diferencia real («+1 marcha» al subir 3 desde la 23).
- `EstadoMarcha` lleva la posición y el total. La **relación** que pide § 5.5 para el modelo físico no existe en una transmisión lineal: se añade cuando la necesiten las transmisiones de marca (change 8) o el modelo físico (change 15).

### 6. `ModeloSimple` y `ControlRodillo`

- `ModeloSimple(incrementoPct = 1.0, marchaNeutra = 12)`: `pendiente = terreno.pendientePct + (marcha − marchaNeutra) × incrementoPct`, como `ComandoRodillo.Pendiente`. El incremento por defecto es el 1 % al que el usuario está acostumbrado (F6.1). La marcha neutra es un parámetro aparte de la de referencia, que el change 7 permitirá mover sin cambiar la resistencia de cada marcha. Sin Zwift, el terreno es la pendiente base (0 %).
- `ControlRodillo` (`:nucleo/dominio`) tiene los modos `SimLibre` y `Manual`. `Manual` es el control manual de Diagnóstico (§ 11), que aquí pasa a ser un modo de control. Calcula el comando vigente y decide si se envía:
  - En `SimLibre`, cada cambio de marcha da un comando nuevo, que se envía al instante. Un comando igual al último enviado no se reenvía.
  - En `Manual`, el comando es el del usuario y los cambios de marcha no envían nada. Se sale de `Manual` al salir de Diagnóstico (lo avisa `DiagnosticoViewModel.onCleared`, que no se llama al girar el móvil, a diferencia de un `DisposableEffect`) o al perder el Neo, y se vuelve a `SimLibre` con el comando de la marcha.
  - Cuando el Neo pasa a `Listo`, se olvida el último enviado y se envía el vigente (spec `trainer-resistance`, «Reenvío al conectar el Neo»).
- Los límites de frecuencia de § 6.2 (4 Hz y umbral apreciable) no hacen falta todavía: sin Zwift, el comando solo cambia con la marcha. Llegan con el change 6.

### 7. Telemetría del Neo

`TelemetriaCodec` (`:nucleo/protocolo`) decodifica Cycling Power Measurement (`0x2A63`: potencia instantánea `sint16` tras las *flags*) y CSC Measurement (`0x2A5B`: revoluciones de biela acumuladas `uint16` y hora del último evento en 1/1024 s). La cadencia sale del cociente de las diferencias, con desbordamiento de 16 bits. Es 0 si la potencia es 0 o si no hay eventos de biela nuevos en 2 s (§ 7.2). Se valida con una traza real contrastándola con la cadencia y la potencia de la página FE-C 0x19, que el Neo envía por `fec2` sin pedirla. Si no coinciden, se usa la 0x19 y se actualiza § 7.2.

- **Alternativa: usar solo la página 0x19.** Es más sencillo (una característica), pero § 7.2 elige las características estándar, que son las mismas que reenviará el puente a Zwift (change 6). La 0x19 queda como referencia de contraste.

### 8. Persistencia: `estado.json` con DataStore y `kotlinx.serialization`

- `EstadoGuardado` (`:nucleo/dominio`, `@Serializable`): `version`, `ultimaMarcha`, `direccionRodillo` y `notificacionesPedidas`. Una sola marcha, no una por rodillo: la app es para un solo Neo (`funcional.md` § 6). Se corrige § 10, que decía «por rodillo». Al cargar, una marcha fuera de 1…24 o un documento ilegible (`CorruptionHandler`) se sustituyen por los valores por defecto.
- `Almacen` (`:app/datos`) usa `DataStore<EstadoGuardado>` con un `Serializer` JSON. La `Sesion` guarda la marcha 2 s después del último cambio, y al terminar sin espera.
- `Configuracion` (`:nucleo/dominio`) reúne los valores por defecto (24 marchas, inicial y referencia 12, incremento 1 %, neutra 12, pendiente base 0 %, recordar la última marcha, aviso de acción y vibración activados). Todavía no se guarda.
- **Dependencias nuevas:** `androidx.datastore:datastore` (el genérico, no el de preferencias), y el plugin de `kotlinx.serialization` con `kotlinx-serialization-json` en `:nucleo`. Las dos están ya elegidas en `arquitectura.md` § 3, con sus alternativas (Proto DataStore, Room, `SharedPreferences`). La otra opción, escribir el fichero a mano con renombrado atómico, evitaría la dependencia, pero habría que repetir la escritura atómica, la serialización de accesos y la recuperación de corrupciones que DataStore ya resuelve.

### 9. Pantalla de rodaje

- `ui/rodaje`: `RodajeRuta`, `RodajeViewModel` y `RodajeUiState` (inmutable y probado con los estados de la maqueta), y los composables de `ui.componentes`: `GearDisplay` (variante lineal, tope atenuado con su etiqueta 1,5 s, entrada de 12 dp en 120 ms con `AnimatedContent`), `GearLadder`, `MetricGrid` y `MetricTile`, `ModeBadge` y `ActionToast` (1,5 s, sustituye sin apilar). `ConnectionChip` (ya existe) gana la acción al tocarlo, con un objetivo táctil de `touch-min`. Los chips de rodaje llevan nombre y punto, como en la maqueta. La reducción a solo el punto que describe su README queda para la pantalla de rodaje final (change 10).
- `MetricGrid` recibe la lista de datos e implementa la tabla de distribución del README, pero solo se prueba y se usa con 2 datos. El resto llega con F11.3 (change 10).
- Movimiento reducido: sin animación si `Settings.Global.ANIMATOR_DURATION_SCALE` es 0.
- Mientras se ve Rodaje, un `DisposableEffect` fija la orientación en `sensorLandscape`, oculta las barras del sistema (`WindowInsetsControllerCompat`, se muestran deslizando) y activa `keepScreenOn`. Al salir de Rodaje se restaura todo. Diagnóstico sigue en la orientación del móvil. Girar recrea la `Activity`, pero la `Sesion` está en `AppGraph` y no se entera.
- Navegación: `MainActivity` arranca en `rodaje`. Los chips navegan a `diagnostico`, que tiene botón de volver. Atrás en Rodaje (`BackHandler`) termina la sesión.

### 10. Latencia (NF1): medida por tramos e investigación con el equipo real

**Medida.** La `Sesion` anota tres horas monotónicas para cada cambio de marcha con palanca: la llegada de la notificación (`t0`, la del *callback*), la entrega a la biblioteca (`t1`) y la confirmación de la escritura (`t2`). Diagnóstico muestra `t2 − t0`. El registro guarda las tres, y también cada cambio de parámetros de conexión de los dos dispositivos. Esta medida no incluye dos tramos: de la pulsación física a la notificación (como mucho un intervalo de conexión del Square) y lo que tarda el Neo en aplicar la resistencia. El primero se acota con el intervalo registrado y el segundo con la prueba de percepción.

**Investigación**, en una sesión guiada con el equipo real (`desarrollo.md` § 3), por pasos. Se para en cuanto se cumple NF1:

1. **Base:** medir 20 cambios con el comportamiento actual (3 reintentos de prioridad en el Square y ninguno en el Neo).
2. **Prioridad sostenida:** mientras dura la sesión, volver a pedir prioridad alta en el Square **y en el Neo** cada vez que el intervalo pase de 15 ms, sin límite de reintentos pero con al menos 10 s entre peticiones. Medir qué fracción del tiempo se pasa en intervalo corto y la latencia en los dos regímenes.
3. **Escritura sin respuesta:** si `fec3` anuncia `WRITE_NO_RESPONSE` (se registran sus propiedades al descubrir), probar `WRITE_TYPE_NO_RESPONSE` para los comandos de marcha. Así no se espera la confirmación y la ranura única se libera antes. Se comprueba que el Neo aplica todos los comandos (respuestas de `fec2` y resistencia percibida). Si se adopta, `t2` pasa a ser la entrega a la pila y se ajusta la spec `trainer-resistance` con `/openspec-update-change`.
4. **Percepción:** el usuario cambia de marcha y dice si nota retraso.

**Decisión.** El resultado (medidas, régimen de intervalos y lo adoptado) se anota en `arquitectura.md` § 6.3. Si con los pasos 2 y 3 el peor caso sigue por encima de 100 ms, se para y se decide con el usuario: aceptar la latencia medida como límite de NF1 con este hardware, o abrir un change de investigación aparte. Lo que cambie de las specs se actualiza antes de seguir.

**Resultado (25-10-2026, OnePlus 8, Square y Neo reales).** Adoptado y aceptado con el usuario:

| Prueba | Latencia | Observaciones |
|---|---|---|
| 1. Base | 228–786 ms (mediana 414) hasta la confirmación | Square a 187,5 ms tras los 3 reintentos |
| 2. Prioridad sostenida en el Square y el Neo | — | El Neo se desconecta al volver a pedirle prioridad alta (pasa a 12,5 ms y corta a ~1 s) |
| 2. Sostenida en el Square, una sola petición en el Neo | 211–436 ms (mediana 270) hasta la confirmación | El Square alterna 12,5 y 187,5 ms cada ~5 s sin desconectarse; el Neo, a 150 ms |
| 3. Además, escritura sin respuesta en `fec3` (propiedades `0x0c`) | 33–78 ms (mediana 62) hasta la entrega | Todos los comandos enviados; el usuario nota mucho menos retraso y la resistencia correcta tras ráfagas de 5 cambios |

- Se quedan la escritura sin respuesta para todos los comandos al Neo, la prioridad alta sostenida solo en el Square (sin límite, al menos 10 s entre peticiones y repetida al cumplirse la espera si no llega otro aviso) y una sola petición al conectar en el Neo.
- `t2` es ahora la entrega a la pila Bluetooth, no la confirmación del Neo, y la spec `trainer-resistance` mide hasta ahí.
- NF1 se acepta con la medida interna y la percepción del usuario. Quedan fuera de la app, como límite conocido, la espera hasta la notificación del Square (hasta 12,5 o 187,5 ms, según el momento) y hasta el siguiente evento de conexión del Neo (hasta 150 ms).

- **Alternativa descartada: pedir prioridad alta al pulsar una palanca.** Android tarda ~5 s en aplicarla (validación del change 3), así que no ayuda al cambio que la provoca.

### 11. Diagnóstico

- `AppBar` con volver a Rodaje. «Último gesto» sale de `EstadoRodaje.ultimoGesto`: botones, gesto y resultado (acción, tope o sin acción). La sección Simulación sale de `EstadoRodaje.simulacion`: marcha, modelo, pendiente base, ajuste, último comando enviado y latencia.
- `DiagnosticoViewModel` deja de leer `ConexionesDiagnostico` y lee la `Sesion`. Los envíos manuales entran por `Sesion.controlManual(comando)`.

### 12. Trazas y tests

- Se graba una traza de rodaje real con cambios de marcha, saltos, topes y pedaleo (`rodaje-marchas.txt`). Un test de `:nucleo` la reproduce por `SquareDecoder` → `EventosBoton` → `ReconocedorGestos` → `Asignacion` → `Cambio` y comprueba la secuencia de marchas anotada en su cabecera. Otro decodifica su telemetría.
- `SesionTest` (JVM, `:app`) cubre con dobles y reloj virtual el camino completo, el modo manual, el reenvío al reconectar, la persistencia y `terminar()`. Comprueba también que entre la notificación y `NeoClient.enviar` no hay suspensiones: el comando ya está entregado antes de que avance el reloj virtual.

## Validación con el equipo real (25-10-2026)

- **Telemetría (decisión 7):** la potencia de `0x2A63` y la cadencia de `0x2A5B` coinciden con la página FE-C 0x19 en la traza `neo-erg-200w.txt` (cadencia dentro de ±8 rpm); se queda con las características estándar.
- **Sesión guiada (tarea 12.1):** ±1, los cuatro saltos con Campagnolo, □, los dos topes con vibración doble y cinco cambios rápidos, cada uno con su pendiente enviada al Neo; traza `rodaje-marchas.txt`, reproducida por `RodajeTrazaTest`. El Campagnolo del otro lado no modifica la palanca, como dice la spec.
- **Latencia (decisión 10):** ver su resultado arriba.
- **Sesión (tarea 12.4):** unos 5 minutos con la pantalla apagada cambiando de marcha, otra app delante, «Terminar» desde la notificación y reapertura en la última marcha; el permiso de notificaciones denegado no se vuelve a pedir. Apagar el Square a mitad de sesión no se probó: lo cubre `SesionTest` y pasa al change 5.
- **Interlineado del tema:** en el OnePlus 8 (360 dp de alto en horizontal), las cifras grandes medían 1,2 veces su cuerpo de letra y la escalera de marchas no cabía. Compose no encoge una línea por debajo del alto natural de la fuente con `Trim.None`; `generar.py` usa ahora `Trim.Both` y `Mode.Tight` en los estilos cuyo interlineado es menor que ese alto, que calcula de la tabla `hhea` de cada TTF. Lo comprueba `SqTemaTest.interlineado_como_en_css`.

## Risks / Trade-offs

- [NF1 no se cumple con los intervalos de 187,5 ms] → Resuelto en la decisión 10: con escritura sin respuesta y prioridad sostenida en el Square, 62 ms de mediana hasta la entrega; aceptado con la percepción del usuario. Los intervalos que imponen el Square y el Neo quedan como límite conocido.
- [Pedir prioridad alta sin parar provoca cortes o gasta batería] → Ocurrió con el Neo, que se desconecta: solo se sostiene en el Square, que lo tolera. Falta ver el efecto en la batería del Square en sesiones largas (change 5).
- [Sin confirmación, un comando al Neo se pierde sin que la app lo sepa] → No se ha visto en las pruebas. El siguiente cambio de marcha o la reconexión envían el comando vigente; si aparece, se vuelve a la escritura con respuesta solo para el último comando de una ráfaga.
- [Las marchas ligeras dan pendientes negativas (hasta −11 %) y en llano el Neo empuja] → Es el comportamiento esperado del modelo simple con base 0 %. Se observa en la prueba de rodaje. «Evitar pendientes negativas» (F6.5) está en el change 15; si molesta, se propone adelantarlo o mover la marcha neutra.
- [El incremento de 1 % o la marcha 12 como neutra no resultan cómodos al rodar] → Son valores de `Configuracion` sin UI. Si el usuario quiere otros tras la prueba, se cambian en el código y en las specs con `/openspec-update-change`, y se anotan en `funcional.md`.
- [El OnePlus corta el servicio en segundo plano] → Aquí se prueba con 5 minutos de pantalla apagada. La sesión de 2 h y la exención de batería son del change 5.
- [□ con `Press` cambiará de comportamiento al llegar los gestos] → Documentado en la decisión 4. El change 7 lo resuelve con su asignación por defecto.
- [Fijar la orientación por pantalla recrea la `Activity` al entrar y salir de Diagnóstico] → La `Sesion` y la navegación sobreviven. Solo se nota un parpadeo al girar, aceptable en una pantalla de ajuste.

## Migration Plan

No hay usuarios ni datos que migrar. `ConexionesDiagnostico` y su test se eliminan en el mismo paso en que la `Sesion` los sustituye. `estado.json` no existe hasta el primer cambio de marcha; si falta, se usan los valores por defecto.
