## Why

Con el change `diagnostico-square-neo` ya se leen todos los botones del Square y el Neo obedece a una pendiente o una potencia enviadas a mano, pero todavía no se puede rodar con marchas. Este change es la fase 1 de `planificacion.md` (change 4): la primera porción vertical útil, en la que las palancas del Square cambian de marcha y el Neo cambia de resistencia, sin Zwift. También sustituye la orquestación provisional `ConexionesDiagnostico` por la `Sesion` definitiva y tiene que medir y acotar la latencia de las palancas (NF1), que con el intervalo de conexión de 187,5 ms que imponen el Square y el Neo no se cumple tal cual (`arquitectura.md` § 6.3).

## What Changes

- **Marchas virtuales (F5.1–F5.4):** transmisión lineal de 24 marchas. La palanca derecha (1 o 2) sube una marcha y la izquierda (1 o 2) baja una, al instante. Los topes no cambian la marcha y se avisan en pantalla y con una vibración doble.
- **Saltos con combinaciones fijas (F5.2, F5.3):** asignación fija y provisional del anexo A.2 de `funcional.md` con un modificador mantenido. Campagnolo derecho + palanca derecha 1 sube 3 marchas y + palanca derecha 2 va a la más dura; Campagnolo izquierdo + palanca izquierda 1 baja 3 y + palanca izquierda 2 va a la más ligera. □ vuelve a la marcha de referencia. Una combinación con modificador no cambia también una marcha suelta.
- **Valores por defecto fijos (F5.5):** 24 marchas, marcha inicial y de referencia 12, incremento de 1 % de pendiente por marcha y pendiente base de 0 %. Viven en el modelo de configuración del dominio; la UI para cambiarlos llega con los changes 8 y 10.
- **Recordar la marcha (F5.6):** la marcha se conserva al perder y recuperar la conexión con el Neo o el Square, y entre sesiones («Recordar la última marcha», activado por defecto, sin ajuste en la UI todavía). Al volver a conectar el Neo, se le reenvía el comando vigente.
- **Modelo simple y rodaje sin Zwift (F6.1, F7.1):** la pendiente enviada al Neo es la pendiente base más el desplazamiento de la marcha por el incremento, en modo `SimLibre`.
- **Telemetría del Neo:** potencia y cadencia decodificadas para la pantalla de rodaje y Diagnóstico.
- **Pantalla de rodaje básica (F11 básica), en horizontal:** la app abre en Rodaje. Muestra la marcha (`GearDisplay` lineal con `GearLadder` y su animación), potencia y cadencia (`MetricGrid` de 2 datos), el modo SIM, la transmisión, los chips de conexión del Neo y del Square (Zwift no aparece hasta el change 6), el aviso de acción (F11.6, activado por defecto) y el tope (F11.4). La pantalla no se apaga mientras se rueda (F11.7). Al tocar un chip se abre Diagnóstico, donde se buscan los dispositivos.
- **Sesión en un *foreground service*:** `Sesion` y `RideService` sustituyen a `ConexionesDiagnostico`. Las conexiones y la marcha siguen con la pantalla apagada o la app en segundo plano. Una notificación permanente muestra la marcha y las conexiones y tiene la acción «Terminar». Al terminar (Atrás desde Rodaje o «Terminar»), el Neo vuelve a pendiente 0 % y se desconecta todo.
- **Diagnóstico como pantalla secundaria:** tiene botón de volver a Rodaje, muestra el último gesto con su acción («Campag. D + Palanca D1 · pulsar · +3 marchas») y la sección Simulación (marcha, modelo, pendiente base, ajuste por marcha, pendiente enviada al Neo y latencia de la última palanca). El control manual pasa a ser un modo temporal. Mientras está activo, las palancas siguen cambiando la marcha pero no se envía al Neo. Al salir de Diagnóstico se vuelve al comando de la marcha.
- **Latencia (NF1):** medida en cada cambio de marcha y registrada por tramos, más una investigación con el equipo real para mantener intervalos de conexión cortos mientras se rueda. El resultado (cumple, se acota o queda como riesgo aceptado) se decide con el usuario.
- **Maqueta:** nuevos estados de rodaje sin Zwift (por defecto, tope en lineal, Square desconectado, sin dispositivos al abrir) y de Diagnóstico rodando sin Zwift, que sustituyen a la «Pendiente Zwift» por la pendiente base.

Fuera de alcance:

- Gestos completos (`Tap`, `LongPress`, `DoubleTap`, `Repeat`, `Chord` general), asignación configurable, inversión de palancas y vibración por gesto (F3, F4, change 7). Aquí solo hay `Press` con modificadores y la asignación fija de arriba. Fijar la marcha de referencia con □ largo también queda para el 7.
- Ajustes y pantalla de Marchas: número de marchas, marcha inicial y de referencia, incremento, pendiente base, «Recordar la última marcha» y «Al rodar» (F5.5 configurable, F12, changes 8 y 10).
- Transmisiones de marca, plato y cassette (F5.7, F5.8, change 8); otros modelos de resistencia, dificultad y evitar pendientes negativas (F6.2–F6.5, change 15).
- ERG manual y alternar SIM ⇄ ERG (F7.2, F7.3, change 9).
- Pantalla de rodaje en vertical, datos configurables, botones − y + y el resto de F11 (change 10). Rodaje queda fija en horizontal.
- Recordar los dispositivos, conectarse al abrir, reconectar solo y la pantalla de Dispositivos (F1.2–F1.4, change 5). Aquí una conexión perdida se recupera desde Diagnóstico con «Buscar dispositivos». La prueba de 2 h con la pantalla apagada (NF4) y la exención de optimización de batería también son del 5.
- Puente con Zwift y el chip de Zwift (change 6).
- Guardar y compartir el registro (F13.4, change 16).

## Capabilities

### New Capabilities

- `virtual-gears`: marcha virtual lineal: subir, bajar, saltos, extremos y referencia, topes con aviso, valores por defecto y recordarla al reconectar y entre sesiones.
- `trainer-resistance`: cómo la marcha se traduce en resistencia del Neo sin Zwift (modelo simple en `SimLibre`), cuándo se envía, reenvío al reconectar y latencia de las palancas.
- `ride-screen`: pantalla de rodaje de la app: marcha, datos, modo, conexiones, aviso de acción y tope, pantalla encendida y acceso a Diagnóstico.
- `ride-session`: sesión de rodaje que se mantiene con la pantalla apagada o en segundo plano, su notificación y cómo se termina.

### Modified Capabilities

- `device-connection`: las conexiones viven mientras dura la sesión, no mientras la app está abierta, y se cierran al terminarla; una pérdida conserva la marcha.
- `diagnostics`: Diagnóstico deja de ser la pantalla inicial y vuelve a Rodaje; muestra el último gesto con su acción y la sección Simulación con la latencia de la última palanca.
- `trainer-manual-control`: el control manual es un modo temporal que se cancela al salir de Diagnóstico y vuelve al comando de la marcha; la pendiente 0 % se envía al terminar la sesión.
- `screen-mockups`: nuevos estados de rodaje sin Zwift y de Diagnóstico rodando sin Zwift.

## Impact

- **Código `:nucleo`:** `dominio`: `ReconocedorGestos` (solo `Press` con modificadores), `Accion`, `Asignacion` fija, `Transmision` lineal, `Cambio`, `EstadoMarcha`, `ModeloResistencia` y `ModeloSimple`, `ControlRodillo` (`SimLibre` y manual), `Configuracion` con los valores por defecto y `EstadoGuardado`. `protocolo`: decodificación de Cycling Power Measurement y CSC Measurement (potencia y cadencia).
- **Código `:app`:** `sesion/Sesion` y `RideService` (sustituyen a `ConexionesDiagnostico`), `datos/Almacen` (`estado.json`), `ble/NeoClient` y `SquareClient` (intervalos de conexión), `ui/rodaje` (pantalla, `ViewModel` y composables `GearDisplay`, `GearLadder`, `MetricGrid`, `MetricTile`, `ModeBadge`, `ActionToast`), cambios en `ui/diagnostico` y en la navegación de `MainActivity`, y el manifiesto (servicio `connectedDevice` y permisos `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_CONNECTED_DEVICE`, `POST_NOTIFICATIONS` y `VIBRATE`).
- **Dependencias nuevas:** DataStore y `kotlinx-serialization-json` (ya en la pila de `arquitectura.md` § 3).
- **Maquetas:** `maquetas/rodaje.html`, `maquetas/diagnostico.html` y `maquetas/index.html`. El sistema de diseño no cambia: los componentes necesarios ya existen.
- **Trazas:** nuevas trazas reales de un rodaje con cambios de marcha y de la telemetría del Neo en los recursos de test de `:nucleo`.
- **Documentos de `docs/`:** `arquitectura.md` (`ConexionesDiagnostico` desaparece y el ciclo de vida pasa a `RideService` en § 4.3 y § 4.4, el resultado de latencia en § 6.3, `ReconocedorGestos` y `Asignacion` provisionales, y el modo manual de Diagnóstico en § 5.4); `funcional.md` (valores por defecto de marcha inicial e incremento si se confirman al rodar); `planificacion.md` (estado del change y lo que pase a los changes 5, 7, 8 y 10); `desarrollo.md` (cómo medir la latencia en la sesión guiada).
