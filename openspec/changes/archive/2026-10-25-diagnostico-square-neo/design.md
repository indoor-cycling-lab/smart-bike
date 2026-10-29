## Context

El change 2 dejó el esqueleto: `:nucleo` vacío, `:app` con `SqTema`, `Tokens.kt` generado y una `MainActivity` provisional. No hay Bluetooth, permisos, navegación ni iconos en la app. La arquitectura objetivo (`arquitectura.md` § 4–8) pone las conexiones en un `RideService` gobernado por la `Sesion`; ninguno de los dos existe todavía y llegan con los changes 4 y 5. Este change necesita conexiones reales antes que ellos, así que el diseño monta solo las piezas de abajo (clientes, protocolo, registro) con la forma definitiva y deja provisional la orquestación.

Los supuestos que valida están en `arquitectura.md` § 7.1 (Square) y § 7.2 (Neo). Motivo y alcance: `proposal.md`. Comportamiento: las specs de este change.

## Goals / Non-Goals

**Goals:**

- `SquareClient`, `NeoClient`, `SquareDecoder`, `EventosBoton`, `FecCodec` y `RegistroSesion` con la interfaz que usarán la `Sesion` y el `RideService`, para que el change 4 los conecte sin rehacerlos.
- Que la validación con el equipo real deje datos objetivos (trazas con hora) y no solo impresiones.
- Primeros composables del sistema de diseño en `ui.componentes`, reutilizables por las pantallas siguientes.

**Non-Goals:**

- `Sesion`, `RideService`, hilo de control compartido con Zwift y gestos: la orquestación de este change es una clase provisional que el change 4 sustituye.
- Persistencia (`Almacen`, DataStore): nada se guarda entre aperturas.

## Decisions

### 1. Orquestación provisional en `ConexionesDiagnostico`, no en la `Sesion`

`AppGraph` (creado en `SquareShiftApp : Application`) construye `BleScanner`, `SquareClient`, `NeoClient` y `RegistroSesion`, y una clase provisional `ConexionesDiagnostico` que lanza la búsqueda, combina los estados de conexión y expone un `StateFlow` que lee `DiagnosticoViewModel`. Corre en un despachador secuencial (`Dispatchers.Default.limitedParallelism(1)`), como el hilo de control de § 6.1: los *callbacks* de Bluetooth solo copian bytes a un `Channel`.

- **Alternativa:** montar ya una `Sesion` mínima. Se descarta porque la `Sesion` nace con marchas, modos de control y servicio en el change 4; una versión a medias obligaría a rehacer su interfaz.
- **Ciclo de vida:** las conexiones viven en `AppGraph`, así que sobreviven a girar el móvil. Al cerrarse `MainActivity` con `isFinishing`, `ConexionesDiagnostico.terminar()` envía pendiente 0 % al Neo, espera su confirmación como mucho 500 ms y desconecta los dos, en el ámbito de la aplicación y no en el del `ViewModel`, que ya se ha cancelado. Sin servicio, Android puede cortar las conexiones en segundo plano; se acepta en la fase 0 (spec `device-connection`, «Conexión mientras la app está abierta»).

### 2. Búsqueda con `BluetoothLeScanner` nativo y filtro por nombre en el *callback*

`BleScanner` (`:app/ble`) busca en `SCAN_MODE_LOW_LATENCY` sin `ScanFilter` y filtra por prefijo de nombre (`Tacx Neo`, `SQUARE`, sin distinguir mayúsculas) en el *callback*, porque `ScanFilter` no admite prefijos y no sabemos si los anuncios llevan el UUID de servicio. Para la búsqueda de cada dispositivo cuando aparece el primero que encaja o a los 30 s.

- **Alternativa:** Nordic Scanner Compat Library. Con `minSdk 28` no aporta nada sobre la API nativa.
- Se registra cada anuncio que encaja (nombre, dirección, RSSI, UUID de servicio anunciados): sirve para decidir en el change 5 si se filtra por UUID.

### 3. Clientes sobre Nordic Android-BLE-Library (`ble-ktx`)

`SquareClient` y `NeoClient` heredan de `BleManager` (dependencias `no.nordicsemi.android:ble` y `ble-ktx`, última 2.x estable al implementar, fijada en `gradle/libs.versions.toml`). Ya está elegida en `arquitectura.md` § 3; su alternativa (`android.bluetooth` con cola propia) se descartó allí.

- **Square:** servicio `347b0001-…`; se suscribe a `0x45` (contadores por botón) y a `0x43` (máscara), las dos al registro. Tras la validación (decisión 10, punto 3) solo `0x45` alimenta `SquareDecoder`; `0x43` se queda en el registro para seguir comparando. Lo decodifica `ConexionesDiagnostico`, no el cliente: `ble` mueve bytes y estados y no conoce el dominio (`arquitectura.md` § 4.1).
- **Prioridad de conexión del Square:** `requestConnectionPriority(HIGH)` al conectar. El Square renegocia a los ~4 s un intervalo de 187,5 ms con latencia 4, lo que solo en este tramo ya supera NF1. `SquareClient` escucha los cambios de parámetros (`setConnectionParametersListener` de Nordic) y, si el intervalo pasa de 15 ms, vuelve a pedir prioridad alta, como mucho 3 veces por conexión, anotando cada cambio en el registro. Si el Square no lo acepta, queda como riesgo para NF1 (ver Riesgos).
- **Neo:** servicio FE-C `6e40fec1-…`; notificaciones de `fec2`, `0x2A63` y `0x2A5B` al registro, en crudo; escrituras en `fec3` con respuesta. `requestConnectionPriority(HIGH)`.
- **Comando pendiente (§ 6.2):** `NeoClient` tiene ya la ranura única de comando pendiente (el último gana) y el tiempo máximo de escritura, aunque aquí solo escriba el control manual: es su responsabilidad en la arquitectura y la probará la `Sesion` sin cambios.
- Los estados de conexión son los de § 8 sin `Reconectando` (change 5): `Desconectado`, `Buscando`, `Conectando`, `Descubriendo`, `Listo`, más `NoEncontrado` y `Perdida` para lo que muestra la UI. Un servicio o característica que falte al descubrir cuenta como fallo de conexión y se registra.
- Para los tests de `ConexionesDiagnostico`, los clientes se usan a través de interfaces (`ConexionSquare`, `ConexionNeo`) con dobles de prueba (`arquitectura.md` § 13).

### 4. Botones: `SquareButton`, `SquareDecoder` y `EventosBoton` en `:nucleo`

- `SquareButton` (`protocolo`): enum de los 20 botones con su bit en la máscara (`arquitectura.md` § 7.1) y la posición de su contador en `0x45`; su nombre corto para la UI se resuelve en `:app` con `strings.xml`. La máscara se conserva como representación del conjunto de botones pulsados (Diagnóstico, combinaciones), aunque ya no se lea de `0x43`.
- **`0x45`, validada con el Square real:** 11 bytes; el byte 0 va a cero y, desde el *nibble* alto del byte 1, hay un contador de 4 bits por botón que sube uno al pulsar y otro al soltar (impar = pulsado). Orden de los 20 contadores: ↑, ←, ↓, →, ✕, □, Campag. I, freno I, palanca I1, palanca I2, Y, A, B, Z, ○, △, Campag. D, freno D, palanca D1, palanca D2.
- `SquareDecoder.contadores(bytes): IntArray?` (20 valores de 0 a 15, en el orden de `SquareButton`) y `SquareDecoder.mascara(contadores)` por paridad; `null` con menos de 11 bytes. `SquareDecoder.mascara(bytes)` de `0x43` se mantiene para comparar en las trazas.
- `EventosBoton` (`dominio`, adelantado del change 4) trabaja con contadores: la diferencia con el anterior, módulo 16, es el número de transiciones de cada botón, alternando pulsar y soltar según la paridad, así que una pulsación completa entre dos notificaciones da `Pulsado` y `Soltado`. La primera notificación fija la línea base y emite `Pulsado` de los contadores impares. En una misma notificación van primero los botones cuya primera transición es soltar y después el resto, en orden de bit; `Pulsado(boton, tMs, mascara)` lleva la máscara de ese momento. `perdida(tMs)` suelta los pulsados y olvida la línea base, para que una reconexión empiece de nuevo. Sin reloj propio: la hora llega con cada notificación.
- «Último botón» se calcula en la UI: los botones de la máscara en el último `Pulsado`, con los Campagnolo delante.

### 5. `FecCodec` con las dos variantes de checksum de la página 0x33

`FecCodec.pendiente(pct)` y `FecCodec.potencia(vatios)` (`protocolo`) devuelven los 13 bytes `A4 09 4E 05 <página> … <chk>` de § 7.2: pendiente como `(pct + 200) × 100` redondeado a entero, potencia como `vatios × 4`, `uint16` *little endian*; los valores fuera de rango del protocolo se rechazan con excepción (la UI ya limita).

El checksum de la 0x33 es lo que más dudas tiene. `FecCodec` recibe `ChecksumPagina33 { XOR, SUMA_MAS_UNO }`, XOR por defecto (estándar ANT). En la compilación de depuración, `MainActivity` lee el *extra* `checksum33=suma` del `Intent` (`adb shell am start … --es checksum33 suma`, permitido por el hook), así se prueban las dos sin otra pantalla ni recompilar. Tras la validación se deja solo la que acepta el Neo (si acepta las dos, XOR) y se elimina el *extra*. **Resultado:** el Neo aplica la pendiente con XOR y sus propias páginas usan XOR; `FecCodec` se queda solo con el XOR y el *extra* se retiró sin probar la suma, que no habría cambiado la decisión.

- **Alternativa:** selector en la pantalla de Diagnóstico. Añade UI que hay que maquetar y quitar después para una prueba de un día.

### 6. `RegistroSesion` en memoria con espejo en `logcat`

Búfer circular de 10 000 entradas (`ArrayDeque` con un cerrojo, para que lo escriban los clientes sin saltar de hilo) con `tMs` monotónico (`SystemClock.elapsedRealtime()`), hora de reloj para mostrar, origen (`SQUARE`, `NEO`, `APP`), sentido (`RX`, `TX`, `EVT`), característica (UUID corto) y bytes. La UI no recibe cada entrada: el registro publica un contador de cambios y el `ViewModel` lo muestrea como mucho 10 veces por segundo para pedir las últimas 200 entradas, así no se recompone en cada notificación; pausar congela la instantánea, no el registro.

En depuración (`BuildConfig.DEBUG`, se activa `buildFeatures.buildConfig`), cada entrada se escribe también con `Log.i("SqTrama", …)` en una línea: `<tMs> <origen> <sentido> <característica> <hex>`, p. ej. `183402117 SQUARE RX 0x43 0a1b2c80020000…`. Así el agente captura trazas con `adb logcat -s SqTrama` (permitido) y las guarda en `nucleo/src/test/resources/trazas/` sin depender de F13.4. Un lector de trazas en los tests de `:nucleo` (`Traza.leer`) las reproduce contra `SquareDecoder` y `EventosBoton`.

- **Alternativa:** escribir un fichero en el almacenamiento de la app y bajarlo con `run-as`. Es lo que hará F13.4 (change 16); `logcat` basta y además se ve en directo.

### 7. Navegación y pantalla

- Navigation Compose (elegido en § 3) con un único destino, `diagnostico`, como inicio. Rutas como cadenas mientras no haya argumentos. El *app bar* no tiene botón de volver porque no hay pantalla anterior; el botón atrás del sistema sale de la app.
- `Diagnostico` es un composable sin estado de `DiagnosticoUiState`, construido por `DiagnosticoViewModel` (fábrica con `initializer` y `AppGraph`). Los estados de la maqueta (`data-mock-state`) se reproducen como instancias de `DiagnosticoUiState` en los tests de Compose.
- Composables de `ui.componentes` con el nombre del componente: `AppBar`, `Button`, `ConnectionChip`, `Notice`, `SquareKey`, `HexLog`, `Stepper` y `SettingsSection` / `SettingsItem` (de `SettingsList`). Solo con lo que necesita Diagnóstico; crecen en los changes siguientes.
- Permisos con `rememberLauncherForActivityResult(RequestMultiplePermissions)`: `BLUETOOTH_SCAN` (`neverForLocation`) y `BLUETOOTH_CONNECT` en Android 12+, `ACCESS_FINE_LOCATION` en Android 11 o inferior; «Abrir ajustes» lanza `ACTION_APPLICATION_DETAILS_SETTINGS`, «Activar Bluetooth» lanza `ACTION_REQUEST_ENABLE`. El estado del adaptador se sigue con `ACTION_STATE_CHANGED`.
- Textos en `strings.xml` (NF7).

### 8. Iconos generados por `generar.py`

`generar.py` convierte cada `<symbol>` de `design-system/icons.svg` en `app/src/main/res/drawable/ic_<nombre>.xml` (*vector drawable* con `viewportWidth/Height` 960, un `<group android:translateY="960">` por el `viewBox` `0 -960 960 960`, `fillColor` negro para teñir con `tint` desde Compose). `IconosSincronizadosTest` (JVM, `:app`) compara el conjunto de iconos y el `pathData` de cada uno con `icons.svg` y falla nombrando el icono, igual que `TokensSincronizadosTest`. Se generan todos, no solo los que usa Diagnóstico: son 15 ficheros pequeños y se evita una lista que mantener.

- **Alternativas:** `material-icons-extended` (dependencia grande, y los iconos podrían no coincidir con el estilo de `icons.svg`); importarlos a mano con Android Studio (se desincronizan sin aviso).

### 9. Dependencias nuevas

| Dependencia | Para qué | Alternativa descartada |
|---|---|---|
| `no.nordicsemi.android:ble`, `ble-ktx` | Clientes del Neo y del Square | `android.bluetooth` con cola propia (`arquitectura.md` § 3) |
| `androidx.navigation:navigation-compose` | Navegación | Navegación manual con estado (§ 3) |
| `androidx.lifecycle:lifecycle-viewmodel-compose` | `ViewModel` en Compose con fábrica | Estado en `remember` en la Activity: se pierde al girar |
| `org.jetbrains.kotlinx:kotlinx-coroutines-android`, `kotlinx-coroutines-test` | Flujos, despachador secuencial y tests con reloj virtual | Ya elegido en § 3 (RxJava descartado) |

`:nucleo` sigue sin dependencias de producción: `SquareDecoder`, `EventosBoton` y `FecCodec` son funciones puras.

### 10. Validación con el equipo real

Sesión guiada (`desarrollo.md` § 3) con la compilación de depuración, el agente leyendo `adb logcat -s SqTrama` y el usuario en el rodillo. Cada punto se anota en `arquitectura.md` como validado o corregido:

1. **Los 20 botones (§ 7.1, F2.1).** El usuario pulsa cada botón en el orden que le dice el agente; el agente comprueba bit a bit la tabla y que la tecla correcta se ilumina.
2. **Palanca 1 y 2 (decisión abierta 2).** El usuario dice qué palanca física (grande o pequeña) ha pulsado; se anota qué bit es cada una.
3. **Pulsaciones rápidas.** Diez pulsaciones lo más rápido posible en D1, I1, D2 e I2: se cuentan los flancos en `0x43` y los cambios de contador en `0x45`. Si `0x43` pierde pulsaciones y `0x45` no, se para y se replantea con el usuario (la arquitectura prevé `0x45` como respaldo).
4. **Frecuencia y bytes 0–2.** Botón mantenido 5 s y Square en reposo 30 s: ¿notifica solo en los cambios o también periódicamente, y cada cuánto? Se observa si los bytes 0–2 se comportan como un contador o una marca de tiempo.
5. **Checksum 0x33 (§ 7.2).** Con XOR: 0 % → 10 % → 0 % pedaleando a cadencia constante; el usuario dice si nota el cambio y el agente mira `fec2` por si el Neo responde. Se repite con `checksum33=suma`. Se anota cuál acepta.
6. **Potencia 0x31.** 200 W a 70 y 95 rpm; el agente lee la potencia de `0x2A63` en las trazas.
7. **Conexión perdida.** Apagar el Square con un botón mantenido: la tecla se libera y el Neo sigue conectado.
8. **Trazas.** Se guardan en `nucleo/src/test/resources/trazas/` una traza de los 20 botones, una de pulsaciones rápidas, una de combinaciones y una del Neo pedaleando, con un test que las reproduce.

**Resultados (25-10-2026), puntos 1–4:**

- El Neo se anuncia como «Tacx Neo 12345», no con `TACX `: el prefijo pasa a `Tacx Neo` sin distinguir mayúsculas (decisión 2). El Square, como `SQUARE`, con el servicio `347b0001-…` en el anuncio.
- Los 20 botones tienen el bit de la tabla en `0x43` y su contador en `0x45` (punto 1).
- La palanca 1 es la grande y la 2 la pequeña, en los dos lados (punto 2; decisión abierta 2 de `funcional.md`).
- Diez pulsaciones rápidas por palanca: `0x43` informa de 4–5 de 10, `0x45` de las 10; en 21 notificaciones un contador subió dos pasos, una pulsación completa entre notificaciones (punto 3). Decisión del usuario: leer los botones de `0x45`.
- El Square notifica `0x43` y `0x45` también en reposo, cada 250 ms al principio y al ritmo del intervalo de conexión (~190 ms) después de renegociarlo. El byte 2 de `0x43` crece despacio, como un contador o un nivel; no afecta a los botones (punto 4).
- Con la lectura por `0x45`, la app cuenta 10 de 10 pulsaciones rápidas por palanca en directo. Volver a pedir prioridad alta funciona solo a medias: Android tarda ~5–6 s en aplicarla, el Square la acepta (12,5 ms) y a los ~5 s vuelve a 187,5 ms con latencia 4; tras los 3 reintentos se queda en 187,5 ms. Queda como riesgo abierto para NF1 (ver Riesgos).
- El Neo emite por `fec2` páginas FE-C 0x10, 0x19, 0x50, 0x51 y 0xF0 sin pedirlas, todas con checksum XOR: indicio a favor del XOR en la 0x33, que se confirma en el punto 5.

## Risks / Trade-offs

- [El Square o el Neo no aparecen con el prefijo de nombre esperado] → Se registra cada anuncio con nombre durante la búsqueda en depuración; si el nombre es otro, se corrige el prefijo y la arquitectura.
- [`0x43` pierde pulsaciones rápidas] → Ocurrió (validación 3). Resuelto leyendo los contadores de `0x45` (decisión 4).
- [El Square impone un intervalo de conexión de 187,5 ms, que retrasa cada pulsación hasta ~190 ms y supera NF1] → `SquareClient` vuelve a pedir prioridad alta al detectarlo (decisión 3) y se mide con pulsaciones rápidas. Si el Square no lo acepta, se anota en `arquitectura.md` § 6.3 como riesgo abierto y se trata en el change 4, que mide la latencia completa.
- [El Neo no acepta ninguna de las dos variantes de checksum] → El tráfico de `fec2` y la comparación con una captura de nRF Connect desde otra app ayudan a ver qué espera; se para y se replantea.
- [Sin servicio, Android corta las conexiones con la pantalla apagada] → Aceptado en la fase 0; las pruebas se hacen con la pantalla encendida. El change 5 lo resuelve con `RideService`.
- [La lista de tráfico satura la UI con notificaciones frecuentes] → Instantánea limitada a 200 filas y 10 actualizaciones por segundo.
- [`ConexionesDiagnostico` se queda como orquestador definitivo por inercia] → Se marca como provisional en el código y en `arquitectura.md`; el change 4 la sustituye por la `Sesion`.

## Migration Plan

No hay usuarios ni datos que migrar. La pantalla provisional `Arranque` de `MainActivity` desaparece; la «Muestra del tema» de depuración no cambia.

## Open Questions

- Si el Neo responde por `fec2` a cada comando (página 0x47 de estado de comando) se verá en la validación 5; si lo hace, el change 4 puede usarlo para confirmar los cambios de marcha, sin cambiar este change.
