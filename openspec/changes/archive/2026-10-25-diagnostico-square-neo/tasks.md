# Tasks

## Maqueta

### 1. Sistema de diseño

- [x] 1.1 Añadir a `design-system/components.css` el estado deshabilitado de los botones del stepper (`.sq-step-ctl button:disabled`: atenuado, sin cursor de acción) y documentarlo en `components/Stepper/README.md` y `preview.html` con un stepper en el máximo; verificar que `python3 scripts/design-system/generar.py` termina sin errores de contraste y que la guía muestra el estado en los dos temas
- [x] 1.2 Comprobar que el texto del valor y del botón activo junto al deshabilitado mantienen 4,5:1 en tema rodaje y claro (el atenuado no lleva información que no esté también en el estado accesible); verificar en la guía regenerada

### 2. Maqueta de Diagnóstico

- [x] 2.1 Añadir a `maquetas/diagnostico.html` la sección «Conexiones» (filas Neo y Square con `sq-conn` y su ayuda, botón «Buscar dispositivos») y los estados `conectado` (por defecto), `buscando`, `neo-no-encontrado`, `square-perdido`, `bluetooth-apagado`, `sin-permiso`, `limites` y `rodando`, con los textos de las specs `device-connection`, `diagnostics` y `trainer-manual-control`; verificar cada estado con `#estado=…` en los dos temas
- [x] 2.2 En los estados sin rodaje, «Último botón» sin gesto ni acción y sin sección Simulación; en `rodando`, el último gesto con acción y la Simulación actuales; añadir «Limpiar» junto a «Pausar», el «Último envío» y los textos de botones o envío deshabilitados sin Square o sin Neo; verificar contra la spec delta de `screen-mockups`
- [x] 2.3 Actualizar `maquetas/index.html` con los estados de Diagnóstico y sus funcionalidades (F1.1, F2, F13.1, F13.2); verificar que cada enlace abre su estado

### 3. Validación

- [x] 3.1 Validación de la maqueta por el usuario: darle los enlaces exactos (`maquetas/diagnostico.html#estado=…` de cada estado y la sección Stepper de la guía) y esperar su aprobación explícita; si pide cambios, ajustar maqueta, `design.md` y specs y volver a pedirla

## App

### 4. Dependencias y esqueleto

- [x] 4.1 Añadir a `gradle/libs.versions.toml` y a `app/build.gradle.kts` Nordic `ble` y `ble-ktx` (última 2.x estable), `navigation-compose`, `lifecycle-viewmodel-compose`, `kotlinx-coroutines-android` y `kotlinx-coroutines-test`; activar `buildFeatures.buildConfig`; verificar con `./gradlew :app:assembleDebug`
- [x] 4.2 Manifiesto: permisos de Bluetooth de `arquitectura.md` § 12 que usa este change (`BLUETOOTH_SCAN` con `neverForLocation`, `BLUETOOTH_CONNECT`; `BLUETOOTH`, `BLUETOOTH_ADMIN`, `ACCESS_FINE_LOCATION` con `maxSdkVersion="30"`) y `android.hardware.bluetooth_le` requerida; `SquareShiftApp` con `AppGraph`; verificar con `./gradlew :app:assembleDebug` y que la app arranca en el OnePlus 8 (skill `desplegar-movil`)

### 5. Iconos generados

- [x] 5.1 Ampliar `scripts/design-system/generar.py` para emitir `app/src/main/res/drawable/ic_<nombre>.xml` por cada `<symbol>` de `icons.svg` (decisión 8 de `design.md`), con la cabecera de fichero generado; ejecutar el generador y verificar que se crean los 15 iconos
- [x] 5.2 `IconosSincronizadosTest` (JVM, `:app`) que compara iconos y `pathData` con `icons.svg` (declarado como entrada del test) y falla nombrando el icono y el comando de regeneración; verificar que pasa y que falla al tocar un icono sin regenerar
- [x] 5.3 Actualizar `AGENTS.md` (derivados de la app) y `arquitectura.md` § 9.1 con los iconos generados; verificar que mencionan `res/drawable/ic_*.xml`

### 6. Protocolo y dominio de los botones (`:nucleo`)

- [x] 6.1 `SquareButton` (20 botones con su máscara) y `SquareDecoder.mascara` con tests de vectores: cada uno de los 20 bits, varios a la vez (`0x80020000`), todo a cero y tramas de menos de 7 bytes; verificar con `./gradlew :nucleo:test`
- [x] 6.2 `EventosBoton` (pulsar y soltar por diferencia de máscaras, orden, sin eventos con la misma máscara, `perdida` libera los pulsados) con tests de los escenarios de la spec `square-buttons`; verificar con `./gradlew :nucleo:test`
- [x] 6.3 Tras la validación (decisiones 4 y 10 de `design.md`): posición del contador de `0x45` en `SquareButton`, `SquareDecoder.contadores` y máscara por paridad, con tests de vectores (cada uno de los 20 contadores, trama de menos de 11 bytes) y con la traza real `square-20-botones.txt`, donde cada botón debe dar el mismo resultado por `0x43` y por `0x45`; verificar con `./gradlew :nucleo:test`
- [x] 6.4 `EventosBoton` por contadores (diferencia módulo 16, pulsación completa entre dos notificaciones, vuelta del contador, línea base con un botón pulsado, `perdida` olvida la línea base) con tests de los escenarios de la spec `square-buttons` y con la traza real `square-pulsaciones-rapidas.txt`, que debe dar 10 pulsaciones de cada palanca; verificar con `./gradlew :nucleo:test`

### 7. Protocolo del Neo (`:nucleo`)

- [x] 7.1 `FecCodec.pendiente` y `FecCodec.potencia` con `ChecksumPagina33` (XOR por defecto y suma + 1), con tests de vectores: 0 %, +5 %, −5 %, +20 %, −10 %, redondeo de 0,5 %, 50 W, 200 W, 1000 W, checksum de cada variante y rechazo de valores fuera del rango del protocolo; verificar con `./gradlew :nucleo:test`

### 8. Registro de sesión

- [x] 8.1 `RegistroSesion` (búfer circular de 10 000 entradas, instantánea limitada, espejo en `logcat` con etiqueta `SqTrama` solo en depuración y formato de línea de la decisión 6) con tests JVM de desbordamiento, orden y formato; verificar con `./gradlew :app:testDebugUnitTest`
- [x] 8.2 Lector de trazas en los tests de `:nucleo` (`Traza.leer`) con un test sobre una traza de ejemplo escrita a mano; verificar con `./gradlew :nucleo:test`

### 9. Conexiones

- [x] 9.1 `BleScanner` con filtro por prefijo de nombre, búsqueda en paralelo de Neo y Square, parada al encontrar o a los 30 s, y registro de los anuncios que encajan; verificar en el móvil con el Neo y el Square encendidos que aparecen en `adb logcat -s SqTrama`
- [x] 9.2 `SquareClient` (suscripción a `0x43` y `0x45`, prioridad alta; las notificaciones van en crudo a `ConexionesDiagnostico`, que las decodifica) y `NeoClient` (suscripción a `fec2`, `0x2A63` y `0x2A5B`, escritura en `fec3` con ranura de comando pendiente y tiempo máximo), detrás de `ConexionSquare` y `ConexionNeo`; verificar en el móvil que los dos llegan a `Listo` y que las tramas aparecen en el registro
- [x] 9.3 `ConexionesDiagnostico` provisional (estados combinados, búsqueda solo de lo que falta, no encontrado, conexión perdida, Bluetooth apagado, `terminar()` con pendiente 0 % y desconexión) con tests JVM sobre dobles de `ConexionSquare` y `ConexionNeo` y `kotlinx-coroutines-test`, cubriendo los escenarios de las specs `device-connection` y `trainer-manual-control` («Vuelta a pendiente 0 %», «Conexión perdida y recuperada»); verificar con `./gradlew :app:testDebugUnitTest`
- [x] 9.4 `ConexionesDiagnostico` lee los botones de `0x45` (decisión 4) y `SquareClient` vuelve a pedir prioridad alta cuando el Square sube el intervalo por encima de 15 ms (decisión 3), con los tests de `ConexionesDiagnostico` adaptados; verificar con `./gradlew :app:testDebugUnitTest` y en el móvil con diez pulsaciones rápidas por palanca (10 de 10 en pantalla y en el registro), anotando el intervalo de conexión que queda

### 10. Pantalla de Diagnóstico

- [x] 10.1 Composables de `ui.componentes` (`AppBar`, `Button`, `ConnectionChip`, `Notice`, `SquareKey`, `HexLog`, `Stepper` con su estado deshabilitado, `SettingsSection`, `SettingsItem`) sobre `SqTema` e iconos generados, con tests de Compose de los estados de cada uno (pulsado, deshabilitado, error) en los dos temas; verificar con `./gradlew connectedDebugAndroidTest`
- [x] 10.2 `DiagnosticoViewModel` y `DiagnosticoUiState` (conexiones, teclas, máscara, último botón, tráfico con pausa y limpieza, control manual con límites, valores iniciales y último envío) con tests JVM de la transformación de estados; verificar con `./gradlew :app:testDebugUnitTest`
- [x] 10.3 Composable `Diagnostico` sin estado que reproduce la maqueta aprobada, navegación con `diagnostico` como inicio, sustitución de `Arranque` en `MainActivity`, permisos («Abrir ajustes»), «Activar Bluetooth» y textos en `strings.xml`; tests de Compose con un `DiagnosticoUiState` por cada estado de la maqueta; verificar con `./gradlew connectedDebugAndroidTest` y con capturas en el móvil comparadas con la maqueta
- [x] 10.4 *Extra* de depuración `checksum33=suma` leído en `MainActivity` y pasado a `FecCodec`; verificado que llega a la app (el registro anota «SumaMasUno»). La prueba de envío con la suma se descartó al confirmarse el XOR en la validación (11.3), que retira el *extra*
- [x] 10.5 Documentar en `docs/desarrollo.md` la captura de trazas (`adb logcat -s SqTrama`) y el *extra* `checksum33`; verificar que los comandos documentados funcionan tal cual

### 11. Validación con el equipo real

- [x] 11.1 Sesión guiada (decisión 10 de `design.md`), puntos 1–2, ya hechos (resultados en la decisión 10): anotar en `arquitectura.md` § 7.1 la tabla validada de `0x43` y `0x45` y el nombre anunciado del Neo y del Square, y en `funcional.md` la decisión abierta 2 resuelta (palanca 1 = grande, palanca 2 = pequeña)
- [x] 11.2 Puntos 3–4, ya hechos (`0x43` pierde pulsaciones; change actualizado): anotar en `arquitectura.md` § 7.1 que los botones se leen de `0x45` y la frecuencia de notificación, y en § 6.3 el intervalo de conexión que impone el Square y lo que dé la tarea 9.4
- [x] 11.3 Puntos 5–6: checksum de la 0x33 con las dos variantes y potencia 0x31 a dos cadencias; dejar en `FecCodec` solo la variante aceptada, quitar el *extra* `checksum33` y su documentación, y anotar en `arquitectura.md` § 7.2; verificar con `./gradlew :nucleo:test`

Trasladadas al archivar, por decisión del usuario (25-10-2026), para hacerlas con el equipo en otro momento:

- Punto 7, conexión perdida con un botón mantenido y reconexión: pasa al change `conexion-automatica-en-segundo-plano` (5), que ya prueba apagar y encender el Square a mitad de sesión. `EventosBoton.perdida` y `ConexionesDiagnostico` lo cubren con tests JVM.
- Punto 8, traza de combinaciones (Campagnolo + palanca, freno + palanca): pasa al change `gestos-y-asignacion` (7). Ya están guardadas las trazas de los 20 botones, las pulsaciones rápidas, la pendiente con XOR y el ERG a 200 W, con tests que las reproducen.

## Documentación

### 12. Documentación

- [x] 12.1 Actualizar la documentación: revisar `docs/funcional.md` (decisión abierta 2), `docs/arquitectura.md` (§ 4.3 con `BleScanner`, `ConexionesDiagnostico` provisional y `EventosBoton` adelantado, § 4.4 ciclo de vida provisional sin servicio, § 7.1 y § 7.2 validados o corregidos, § 9.1 iconos), `docs/planificacion.md` (fila del change 3 «En curso» al crearlo; F2.2 y `EventosBoton` pasan del 4 al 3; dependencias o alcance de los changes siguientes según la validación) y `docs/desarrollo.md`; verificar que ninguno enlaza `docs/referencias/`

## Workflow follow-up

- Archivar el change con `/openspec-archive-change` cuando la validación con el equipo real esté aprobada, marcándolo «Archivado» en `docs/planificacion.md`.
