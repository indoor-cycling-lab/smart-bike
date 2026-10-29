# Tasks

## Maqueta

### 1. Maqueta de rodaje

- [x] 1.1 Añadir a `maquetas/rodaje.html` los estados `sin-zwift` (lineal 24, marcha 14, potencia y cadencia, chips Neo y Square sin chip de Zwift, aviso «+1 marcha»), `tope-lineal` («24» atenuado con «Tope · marcha más dura», último segmento de la escalera resaltado), `square-off` (chip «Square sin conexión», datos con valor, pie «Toca Neo o Square para conectarlos») y `sin-dispositivos` (los dos chips «sin conexión», datos «—», mismo pie), con los chips enlazando a `diagnostico.html`, solo con clases del sistema de diseño; verificar cada estado con `#estado=…` en los dos temas y contra la spec delta de `screen-mockups`
- [x] 1.2 Comprobar en la guía y en la maqueta que el pie con el texto de ayuda y los chips enlazados no necesitan componentes ni variantes nuevas; si alguno falta, añadirlo a `design-system/` (`components.css`, `README.md` y `preview.html`), regenerar con `python3 scripts/design-system/generar.py` y verificar que termina sin errores de contraste

### 2. Maqueta de Diagnóstico

- [x] 2.1 Pasar los estados de `maquetas/diagnostico.html` sin Zwift (`conectado`, `buscando`, `neo-no-encontrado`, `square-perdido`, `bluetooth-apagado`, `sin-permiso`, `limites`) a la sesión de rodaje: botón de volver a `rodaje.html`, «Último gesto» con acción («Campag. D + Palanca D1 · pulsar · +3 marchas») y sección Simulación con «Pendiente base» en lugar de «Pendiente Zwift» y la latencia de la palanca (`[—]` hasta capturarla), con «Enviada al Neo» en «—» sin Neo; el estado `rodando` se queda como referencia con Zwift (etiqueta «Rodando con Zwift»); verificar cada estado contra la spec delta de `screen-mockups`
- [x] 2.2 Actualizar `maquetas/index.html` con los estados nuevos de Rodaje y Diagnóstico y sus funcionalidades (F5, F11, F13); verificar que cada enlace abre su estado

### 3. Validación

- [x] 3.1 Validación de la maqueta por el usuario: darle los enlaces exactos (`maquetas/rodaje.html#estado=sin-zwift`, `#estado=tope-lineal`, `#estado=square-off`, `#estado=sin-dispositivos`, `maquetas/diagnostico.html#estado=conectado`, `#estado=neo-no-encontrado` y `#estado=rodando`) y esperar su aprobación explícita; si pide cambios, ajustar maqueta, `design.md` y specs y volver a pedirla

## App

### 4. Dependencias y manifiesto

- [x] 4.1 Añadir a `gradle/libs.versions.toml` `androidx.datastore:datastore` (última estable) y el plugin `org.jetbrains.kotlin.plugin.serialization`; aplicarlo en `:nucleo` con `kotlinx-serialization-json` en `implementation`, y DataStore en `:app` (design.md, decisión 8); verificar con `./gradlew :nucleo:test :app:assembleDebug`
- [x] 4.2 Manifiesto: `RideService` con `foregroundServiceType="connectedDevice"` y los permisos `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_CONNECTED_DEVICE`, `POST_NOTIFICATIONS` y `VIBRATE` (`arquitectura.md` § 12); textos nuevos en `strings.xml`; verificar con `./gradlew :app:assembleDebug`

### 5. Dominio de marchas y resistencia (`:nucleo`)

- [x] 5.1 `Gesto.Press` y `ReconocedorGestos` (un `Press` por `Pulsado`, con los Campagnolo ya pulsados como modificadores); tests: palanca sola, Campag. D antes de D1, Campag. D después de D1 (sin modificador), varios botones; verificar con `./gradlew :nucleo:test`
- [x] 5.2 `Accion` (grupo de marchas) y `Asignacion` fija (decisión 4); tests de cada fila de la tabla, modificador del otro lado (±1), □ y botones sin acción; verificar con `./gradlew :nucleo:test`
- [x] 5.3 `Transmision.Lineal`, `EstadoMarcha` y `Cambio` con `Cambiada`, `Tope` y `SinCambio` (decisión 5); tests de los escenarios de la spec `virtual-gears`: ±1, ±3, extremos, referencia, tope en 1 y 24, salto recortado sin tope, referencia sin cambio; verificar con `./gradlew :nucleo:test`
- [x] 5.4 `Terreno`, `ComandoRodillo`, `ModeloResistencia` y `ModeloSimple` (decisión 6); tests: marcha 12 → 0 %, 17 → +5 %, 1 → −11 %, 24 → +12 %; verificar con `./gradlew :nucleo:test`
- [x] 5.5 `ControlRodillo` con `SimLibre` y `Manual` (decisión 6); tests: envío al cambiar, sin reenvío del mismo comando, `Manual` no envía con cambios de marcha, salir de `Manual` envía la marcha, pérdida del Neo cancela `Manual`, `Listo` reenvía el vigente, pendiente 0 % al terminar; verificar con `./gradlew :nucleo:test`
- [x] 5.6 `Configuracion` con los valores por defecto y `EstadoGuardado` serializable con validación al cargar (decisión 8); tests: valores por defecto, ida y vuelta JSON, marcha fuera de rango y documento ilegible → valores por defecto; verificar con `./gradlew :nucleo:test`

### 6. Telemetría del Neo (`:nucleo`)

- [x] 6.1 `TelemetriaCodec` para `0x2A63` (potencia) y `0x2A5B` (cadencia por biela con desbordamiento de 16 bits, 0 sin eventos en 2 s o con potencia 0) (decisión 7); tests de vectores, incluido el desbordamiento; verificar con `./gradlew :nucleo:test`
- [x] 6.2 Test con la traza real `neo-erg-200w.txt` que contrasta la potencia y la cadencia decodificadas con la página FE-C 0x19 de `fec2`; si no coinciden, usar la 0x19 y anotarlo en `design.md`; verificar con `./gradlew :nucleo:test`

### 7. Sesión (`:app`)

- [x] 7.1 `Sesion` con la búsqueda, los estados de conexión y Bluetooth de `ConexionesDiagnostico`; pasar `ConexionesDiagnosticoTest` a `SesionTest` y eliminar `ConexionesDiagnostico` (decisión 1); verificar con `./gradlew :app:testDebugUnitTest` que los tests migrados pasan
- [x] 7.2 Camino caliente en el hilo de control (decisión 3): notificación → gesto → acción → marcha → comando → `NeoClient.enviar`, con `EstadoRodaje` (marcha, último gesto, simulación) y `eventos` (cambio, tope) actualizados después; tests con dobles y reloj virtual: palanca → comando entregado sin avanzar el reloj, cinco pulsaciones rápidas → último comando de la marcha final, cambios sin Neo aplicados al conectar, reenvío al pasar a `Listo`, marcha conservada al perder el Square; verificar con `./gradlew :app:testDebugUnitTest`
- [x] 7.3 Telemetría en `EstadoRodaje` (potencia y cadencia; `null` sin Neo) y modo manual por `Sesion.controlManual` y `salirDiagnostico`; tests de los escenarios de `trainer-manual-control` (manual temporal, pérdida y recuperación, salir de Diagnóstico); verificar con `./gradlew :app:testDebugUnitTest`
- [x] 7.4 Medida de latencia `t0`, `t1`, `t2` en el registro y la última en `EstadoRodaje` (decisión 10); registro de gestos, acciones, marchas y comandos con su confirmación; tests: horas en orden, «—» sin cambios, entradas del escenario «Seguir un cambio de marcha» de `diagnostics`; verificar con `./gradlew :app:testDebugUnitTest`
- [x] 7.5 Vibración por la interfaz `Vibrador`: corta en cada cambio y doble en el tope, desde la `Sesion`; implementación con `VibratorManager` o `Vibrator`; tests con un doble; verificar con `./gradlew :app:testDebugUnitTest`

### 8. Persistencia (`:app`)

- [x] 8.1 `Almacen` con `DataStore<EstadoGuardado>`, `Serializer` JSON y `CorruptionHandler` (decisión 8); la `Sesion` carga la marcha al empezar, la guarda 2 s después del último cambio y al terminar sin espera; tests con reloj virtual y un almacén en memoria (marcha recordada, cierre inesperado tras 2 s, primer uso → 12); verificar con `./gradlew :app:testDebugUnitTest`

### 9. Servicio y ciclo de vida (`:app`)

- [x] 9.1 `AppGraph` crea y publica la `Sesion` al abrir la app y la descarta al terminar; `RideService` arranca cuando `BLUETOOTH_CONNECT` está concedido, con su notificación «Marcha N de 24 · …» actualizada al cambiar la marcha o las conexiones, y la acción «Terminar» (decisiones 1 y 2); verificar instalando en el OnePlus 8 (skill `desplegar-movil`) que la notificación aparece y cambia con la marcha
- [x] 9.2 `Sesion.terminar()`: guardar la marcha, pendiente 0 % esperando como mucho 500 ms, desconectar, detener el servicio y cerrar la `Activity`, desde Atrás en Rodaje y desde «Terminar»; test JVM del orden de los pasos; verificar en el móvil que tras terminar el Neo y el Square quedan libres y la notificación desaparece
- [x] 9.3 Permiso `POST_NOTIFICATIONS` pedido una sola vez al abrir Rodaje en Android 13 o superior, guardado en `estado.json`; verificar en el móvil que, denegado, la sesión sigue funcionando con la pantalla apagada y no se vuelve a pedir

### 10. Pantalla de rodaje (`:app`)

- [x] 10.1 Composables `GearDisplay` (lineal, tope 1,5 s, animación de 12 dp en 120 ms y sin animación con movimiento reducido), `GearLadder` (actual y referencia), `MetricTile` y `MetricGrid` (2 datos), `ModeBadge` (SIM), `ActionToast` (1,5 s, sustituye) y `ConnectionChip` tocable con objetivo `touch-min` (decisión 9); tests de Compose en `ComponentesTest` con los estados de la maqueta; verificar con `./gradlew connectedDebugAndroidTest`
- [x] 10.2 `RodajeUiState` y `RodajeViewModel` (marcha, datos con «—», chips, pie de ayuda, aviso y tope desde `eventos`); tests JVM de cada estado de la maqueta (`sin-zwift`, `tope-lineal`, `square-off`, `sin-dispositivos`, Neo sin conexión); verificar con `./gradlew :app:testDebugUnitTest`
- [x] 10.3 `RodajeRuta` como destino inicial de `MainActivity`: horizontal, sin barras del sistema y pantalla encendida mientras se ve, chips que abren Diagnóstico y Atrás que termina la sesión; test de Compose `RodajeTest`; verificar con `./gradlew connectedDebugAndroidTest` y con una captura en el OnePlus 8 comparada con `maquetas/rodaje.html#estado=sin-zwift`

### 11. Diagnóstico (`:app`)

- [x] 11.1 `DiagnosticoViewModel` sobre la `Sesion`: volver a Rodaje, «Último gesto» con acción, tope o «sin acción», sección Simulación con latencia y salida del modo manual al dejar la pantalla (decisión 11); actualizar `DiagnosticoUiStateTest` y `DiagnosticoTest` con los estados de la maqueta; verificar con `./gradlew :app:testDebugUnitTest connectedDebugAndroidTest` y con una captura comparada con `maquetas/diagnostico.html#estado=conectado`

### 12. Validación con el equipo real

- [x] 12.1 Sesión guiada de rodaje (`desarrollo.md` § 3): el usuario pedalea con el Neo y el Square, sube y baja marchas, hace los cuatro saltos, □ y los dos topes; el agente comprueba en `adb logcat -s SqTrama` la marcha, la pendiente enviada y la telemetría, y graba la traza `nucleo/src/test/resources/trazas/rodaje-marchas.txt` con su cabecera; anotar el resultado en `design.md`
- [x] 12.2 Test de `:nucleo` que reproduce `rodaje-marchas.txt` por `SquareDecoder` → `EventosBoton` → `ReconocedorGestos` → `Asignacion` → `Cambio` y comprueba la secuencia de marchas de su cabecera, y su telemetría con `TelemetriaCodec`; verificar con `./gradlew :nucleo:test`
- [x] 12.3 Investigación de latencia, pasos 1 a 4 de la decisión 10 (base, prioridad sostenida en el Square y el Neo, escritura sin respuesta si `fec3` la admite, percepción del usuario), parando al cumplir NF1; anotar las medidas en `design.md`; si se adopta la escritura sin respuesta o NF1 no se cumple, parar, decidir con el usuario y actualizar los artefactos con `/openspec-update-change` antes de seguir — Resultado: escritura sin respuesta, prioridad sostenida solo en el Square, 62 ms de mediana hasta la entrega; aceptado con el usuario (`design.md`, decisión 10)
- [x] 12.4 Prueba de sesión: 5 minutos con la pantalla apagada cambiando de marcha con el Square (las marchas llegan al Neo y vibra), ir a otra app y volver, terminar desde la notificación y volver a abrir en la última marcha; perder y reconectar el Neo y el Square desde Diagnóstico sin perder la marcha; anotar el resultado en `design.md` — Resultado (25-10-2026): ~5 min con la pantalla apagada, otra app delante, «Terminar» y reapertura en la última marcha, todo correcto; perder el Neo y reconectarlo, probado en la prueba de latencia. Apagar el Square a mitad de sesión no se pudo probar: lo cubre `SesionTest` y la prueba con el equipo pasa al change 5, que ya la tiene prevista
- [x] 12.5 Pregunta al usuario tras rodar: si el incremento de 1 % por marcha y la 12 como neutra le resultan cómodos, y si las pendientes negativas de las marchas ligeras molestan; si quiere otros valores, cambiarlos en `Configuracion` y actualizar las specs con `/openspec-update-change` — Resultado (25-10-2026): el usuario deja el 1 % por marcha y la 12 como neutra de forma provisional; se revisan al comparar modelos en el change 15

## Documentación

### 13. Documentación

- [x] 13.1 Actualizar la documentación: revisar `docs/funcional.md` (valores por defecto de marcha inicial, neutra e incremento si se confirman), `docs/arquitectura.md` (§ 4.3 sin `ConexionesDiagnostico` y con `TelemetriaCodec`, `ReconocedorGestos` y `Asignacion` provisionales; § 4.4 la `Sesion` creada por `AppGraph` y el `RideService`; § 5.4 el modo `Manual`; § 6.3 el resultado de latencia; § 7.2 la telemetría; § 10 una sola última marcha), `docs/planificacion.md` (fila y notas del change 4, y lo que pase a los changes 5, 7, 8, 10 o a uno nuevo de latencia) y `docs/desarrollo.md` (cómo medir la latencia en la sesión guiada); verificar que ningún documento sigue citando `ConexionesDiagnostico` como vigente

## Workflow follow-up

- Archivar el change con `/openspec-archive-change` cuando estén todas las tareas y la validación del usuario, y marcarlo «Archivado» en `docs/planificacion.md`.
