# Auditoría de preparación para producción (29-10-2026)

Revisión de todo el repositorio (app, core, scripts, web) buscando fallos, vulnerabilidades, código a optimizar y código muerto antes de considerar la app productiva. Las referencias `fichero:línea` son del commit `0cc05d4`.

Estado de partida: los 1306 tests unitarios y `ktlintCheck` pasan; `lintRelease` da 5 errores (no bloquea el build).

Leyenda del estado: ⬜ pendiente · 🟡 hecho en parte · ✅ hecho · 🔀 movido a un change de OpenSpec · ➖ aceptado como limitación conocida (ver «Decisiones»).

## Crítico

| # | Estado | Dónde | Problema | Arreglo |
|---|---|---|---|---|
| C1 | ✅ | `FecCodec.kt:21,28`, `FtmsCodec.kt:122`, `SimulacionZwiftCodec.kt:59`, `Sesion.kt:1867`, `AppGraph.kt:73` | Una pendiente fuera de ±200 % (FTMS permite ±327 %) o una potencia > 4000 W llega sin recortar a `FecCodec`, cuyo `require` lanza `IllegalArgumentException`. Nadie la captura y la app cae. Se provoca con un solo paquete por DirCon (TCP 36866) o BLE (`2AD9`), y sin atacante con el modelo físico y mucha pendiente o viento de cara. El móvil queda renombrado hasta reabrir la app. | Recortar pendiente y potencia antes de codificar; `CoroutineExceptionHandler` en `ambitoApp` que lo anote en el registro; capturar excepciones al atender DirCon y las escrituras GATT. **Hecho:** `ComandoRodillo.limitado()` en `ControlRodillo` y en `Sesion.codificar`; `CoroutineExceptionHandler` en `ambitoApp`. Queda para el change de DirCon/BLE responder al cliente GATT cuando la escritura falla. |
| C2 | ✅ | `CopiaConfiguracion.kt:48` | `InputStream.readNBytes(int)` requiere API 33 y `minSdk` es 28. En Android 9–12 importar una configuración lanza `NoSuchMethodError` (un `Error`, no lo captura el `catch (Exception)`) y la app cae. Lint: `NewApi`. | Leer con un bucle acotado a `MAX_BYTES + 1`. |
| C3 | ✅ | `Sesion.kt:455,468,1411,1505,1553,1781,769-802` | Una `IOException` de DataStore en cualquier corrutina de la sesión tumba el proceso en pleno rodaje. `terminar()` no protege nada: si `guardar()` falla, el Neo no recibe el 0 % ni se desconecta nada. | `try/catch` en las llamadas al almacén y `try/finally` en `terminar()`. **Hecho:** `terminar()` con `try/finally` y el guardado protegido; el handler de `ambitoApp` evita el cierre. Queda M6 para no perder datos al leer. |

## Alto

| # | Estado | Dónde | Problema | Arreglo |
|---|---|---|---|---|
| A1 | ✅ | `DirconServer.kt:253` | `bind` sin dirección: escucha en `::` (todas las interfaces), también datos móviles con IPv6, punto de acceso o VPN. Contradice NF5. | `bind` a la IPv4 del wifi y republicar si cambia. **Hecho en `endurecer-dircon-ble`.** |
| A2 | ✅ | `DirconServer.kt:95-104`, `ConexionFtms.kt:80-89`, `PuenteFtms.kt:115-124` | Sin autenticación; un cliente nuevo echa al anterior. No se exige `PedirControl` (0x00) antes de `0x05`/`0x11`. | Sustituir solo desde la misma IP o tras N s sin tráfico; exigir `0x00` por conexión. **Hecho en `endurecer-dircon-ble`.** |
| A3 | ✅ | `DirconServer.kt:174-192` | Socket sin `soTimeout` ni `keepAlive`. Si el PC con Zwift se suspende, la conexión no se da nunca por perdida y el Neo se queda con la última resistencia indefinidamente. | `keepAlive` y un vigilante de lectura/escritura. **Hecho en `endurecer-dircon-ble`.** |
| A4 | ➖ | `AppGraph.kt:209-223` | `iniciarSesion()` devuelve la sesión que está terminando; al reabrir la app enseguida, la actividad se ata a ella y se cierra sola (`MainActivity.kt:76`). | Si la sesión actual es la que termina, crear una nueva. **Aplazado:** arreglarlo deja dos sesiones vivas a la vez sobre los mismos clientes BLE (la que termina desconectaría a la nueva) y `RideService` espera pasar por `null`. Necesita diseño: que la nueva espere a que acabe la anterior. **Aceptado como limitación conocida** (29-10-2026): ver «Decisiones». |
| A5 | ✅ | `RideService.kt:49-51` | Con sesión `null` hace `stopSelf()` sin `startForeground()` tras `startForegroundService`: Android cierra la app. | Llamar siempre a `startForeground` y luego parar. |
| A6 | ✅ | `Sesion.kt:1595-1601` | `cancelarIntento` llama a `desconectar()` sin límite de tiempo dentro del colector de Bluetooth; con el Square dormido en `autoConnect` y el Bluetooth apagado, el colector se cuelga (a validar con el equipo real). | Envolver en `conLimite { }`. **Probado y deshecho (29-10-2026, OnePlus 8):** con `conLimite`, a los 1,5 s se lanzaba un intento nuevo mientras la desconexión seguía en la cola de Nordic, y el Square no llegaba a conectar (bucle «Buscando» → «Conectando» → «Una desconexión no termina»). Sin el límite conecta. Hay que esperar a que Nordic descarte el intento (por ejemplo, cancelar la petición `connect` antes de `disconnect`) en lugar de abandonar la espera. **Hecho en `endurecer-dircon-ble`.** |
| A7 | ✅ | `app/build.gradle.kts:33-36` | Release sin R8, sin `shrinkResources` y sin firma. | Activarlos con reglas para kotlinx.serialization y Nordic; firma desde el entorno. |
| A8 | ✅ | `AndroidManifest.xml` | Sin `allowBackup` ni `dataExtractionRules`: la copia de Google incluye las sesiones (sin límite, falla al pasar de 25 MB), las MAC y `nombreBluetoothOriginal`. | Decidir la política y excluir `sesiones/`. |
| A9 | ⬜ | `core/src/test/resources/trazas/*.txt`, `ConexionFtmsTest.kt:213,252`, `maquetas/dispositivos.html` | Identificadores de dispositivos reales versionados (MAC, «Tacx Neo 12345»). La dirección Bluetooth real de `ConexionFtmsTest` faltaba en el inventario de `publicar-repo-publico`. | Añadida al inventario y a la tarea 2.2 de ese change (29-10-2026); se sustituye al aplicarlo. |

## Medio

| # | Estado | Dónde | Problema | Arreglo |
|---|---|---|---|---|
| M1 | ✅ | `Rodaje.kt:240`, `MetricGrid.kt:180` | `remember(datos, …)` incluye los valores: `medirRejilla` (búsqueda de 144 sp hacia abajo de 2 en 2) se repite en el hilo principal con cada dato nuevo. | Clave con etiqueta, unidad, plantilla y longitud del valor, o búsqueda binaria. |
| M2 | ✅ | `ConnectionBar.kt:110`, `ConnectionChip.kt:98` | `rememberInfiniteTransition` siempre creada: pide fotogramas todo el rodaje aunque no haya estado «Esperando». | Crearla solo en la rama `Esperando`. |
| M3 | ✅ | `Stepper.kt:110-128` | Actúa en `awaitFirstDown`: empezar a desplazar sobre un − o + cambia el valor. | Actuar al levantar si no hubo desplazamiento. |
| M4 | ✅ | `Sesion.kt:769-802` | Durante `terminar()` las entradas siguen vivas: una palanca envía otra pendiente tras el 0 %, o se vuelve a publicar o renombrar. | Cancelar primero los colectores de entrada y comprobar `terminada`. **Hecho:** `enviarNeo`, `actualizarPlataforma` y `botones` no hacen nada con la sesión terminando; el 0 % de `terminar()` va por `enviarYEsperar`. Test `mientras_termina_una_palanca_ya_no_pisa_la_pendiente_cero`. |
| M5 | ✅ | `Sesion.kt:533-556` | `buscarTipos` suspende antes de registrar la búsqueda: dos toques seguidos lanzan dos escaneos y dejan uno huérfano. | Marcar la búsqueda antes de suspender. **Hecho:** la búsqueda se crea `LAZY` y se registra antes de cancelar los intentos; arranca después, salvo que entretanto se haya cancelado. Test `dos_buscar_seguidos_lanzan_una_sola_busqueda`. |
| M6 | ✅ | `Configuracion.kt:205-209,519-525`, `Almacen.kt:41,48` | Lectura todo o nada: un campo inválido devuelve los valores por defecto sin rastro y la siguiente escritura machaca el archivo (se pierden `nombreBluetoothOriginal` y emparejamientos). Los `ReplaceFileCorruptionHandler` nunca se ejecutan. | `coerceInputValues`, anotar el fallo y guardar copia del archivo ilegible. **Hecho:** en lugar de `coerceInputValues`, `leerPorCampos` (`Lectura.kt`): si el documento no se entiende entero, cada campo de primer nivel que falla toma su valor por defecto y los demás se conservan, en `configuracion.json` y en `estado.json`. Si se pierde algo, `Almacen` copia el original a `<archivo>.ilegible` y lo anota en el registro («Almacén: …»). Los `ReplaceFileCorruptionHandler`, que nunca se ejecutaban, se quitan. |
| M7 | ✅ | `GrabadorSesion.kt:85-99` | Las sesiones grabadas no rotan; solo se para por espacio libre en disco. | Conservar las últimas N o X MB. |
| M8 | ✅ | `GattServerHost.kt:89-100` | Notificaciones en serie con hasta 500 ms por cliente, a todo el que haya leído (no solo suscritos); con `DROP_OLDEST` se pueden perder sueltas de botón. | Suscripciones por dispositivo y tiempo por destino. **Hecho en `endurecer-dircon-ble`.** |
| M9 | ✅ | `GattServerHost.kt:238-249`, `Advertiser.kt:46,79-88` | `onServiceAdded` en hilo binder sin sincronizar con `parar()`: anuncio fantasma tras parar. | Pasar los callbacks al hilo de control. **Hecho en `endurecer-dircon-ble`.** |
| M10 | ✅ | `DirconCodec.kt:186-201`, `DirconServer.kt:194-203`, `GattServerHost.kt:291` | Reensamblado cuadrático con mensajes troceados; sin límite de ritmo por conexión. | Búfer fijo con índices; limitar mensajes por segundo. **Hecho en `endurecer-dircon-ble`.** |
| M11 | ✅ | `GattServerHost.kt:280-299` | Escrituras preparadas tratadas como completas; sin `onExecuteWrite`. | Responder `REQUEST_NOT_SUPPORTED` o acumular. **Hecho en `endurecer-dircon-ble`.** |
| M12 | ✅ | `MainActivity.kt:109` | `Flow` creado en la composición (lint `FlowOperatorInvokedInComposition`); el `LaunchedEffect` se reinicia y puede perder un evento. | `remember(sesion) { }`. |
| M13 | ✅ | `contraste.py:22-29` | Falta el par `gear` sobre `gear-soft` (aviso de tope, `ButtonMappingRow`). | Añadir el par. |
| M14 | ⬜ | `RideService`, `AdaptadorWifi.kt:36` | Se declara `WAKE_LOCK` y solo hay `WifiLock`; los temporizadores con pantalla apagada dependen de la CPU despierta (ya pendiente en `arquitectura.md`). | Incluir en la prueba de 2 h. |

## Bajo

| # | Estado | Dónde | Problema | Arreglo |
|---|---|---|---|---|
| B1 | ✅ | `ClienteBle.kt:141-183` | Carrera en el estado de prioridad de conexión (sin `@Volatile`, dos hilos). | Saltar al hilo de control en el listener. **Hecho en `endurecer-dircon-ble`.** |
| B2 | ✅ | `ClienteBle.kt:90` | `getRemoteDevice` fuera del `try`: una dirección guardada corrupta tumba la app. | `checkBluetoothAddress` antes. **Hecho en `endurecer-dircon-ble`.** |
| B3 | ✅ | `Advertiser.kt:82`, `GattServerHost.kt:153`, `BleScanner.kt:85` | Llamadas con permiso sin capturar `SecurityException`. | `try/catch` con registro. **Hecho en `endurecer-dircon-ble`.** |
| B4 | ✅ | `ClienteBle.kt:197,205`, `BleScanner.kt:68-74` | En release, el log de Nordic va en INFO (direcciones en logcat); el registro exportable guarda nombre y MAC de todo lo cercano. | `Log.WARN` en release; registrar solo lo que encaja. **Hecho:** Nordic en `Log.WARN` en release; `BleScanner` registra con nombre y MAC solo los anuncios que encajan con lo que se busca y, al terminar, cuántos otros vio («Búsqueda terminada; otros dispositivos a la vista: N»). |
| B5 | ✅ | `ClienteBle.kt:162,172`, `GattServerHost.kt:191` | Lint `WrongConstant`. | Usar las constantes que espera la API o anotar la decisión. |
| B6 | ✅ | `Sesion.kt:1437-1465` | Cada muestra de potencia emite `_estado` dos veces y calcula la velocidad dos veces. | Un único `update`. |
| B7 | ✅ | `Diagnostico.kt:497` | `SimpleDateFormat` nuevo en cada recomposición. | `DateTimeFormatter` constante. |
| B8 | ✅ | `Tabla.kt:62`, `Ajustes.kt:106`, `AccionesConexion.kt:71` | Al girar se pierde estado: desplazamiento, mensaje de copia, acción pendiente de permisos. | `rememberSaveable` u `onCleared`. **Hecho:** Tabla recuerda (`rememberSaveable`) que ya se desplazó a la marcha puesta; Ajustes no descarta el mensaje de la copia si la actividad se recrea por un cambio de configuración; la acción pendiente de permisos se guarda como clave (`rememberSaveable`) en lugar de una lambda. |
| B9 | ✅ | `ConnectionChip.kt:69` | Sin `role = Role.Button` para TalkBack. | Añadir el rol. |
| B10 | ✅ | `scripts/sitio/generar.py`, `scripts/sitio/capturas.py` | Algunos campos van a atributos sin `e()`; rutas desde `capturas.json` sin validar; `sips` solo existe en macOS. | Escapar, validar y fallar con mensaje claro. |

## Código muerto

Comprobado con grep en todo el repositorio, tests incluidos.

| Estado | Qué | Uso |
|---|---|---|
| ✅ | `BOTONES_ZWIFT` (`AsignarAccionUiState.kt:45`) | Ninguno |
| ✅ | `DirconServer.NOMBRE` (`DirconServer.kt:263`), parámetro `sinPrefijoCorto` (`:52`) | Ninguno. Borrados en `endurecer-dircon-ble` |
| ✅ | `GrabadorSesion.trabajo` (`:72,99`) | Se asigna, no se lee |
| ✅ | Permiso `CHANGE_WIFI_MULTICAST_STATE` | No hay `MulticastLock` |
| ✅ | Strings `plataforma`, `plataforma_no_disponible`, `nombre_ayuda`, `nombre_kickr_ayuda` | Ninguno |
| ✅ | Dependencias `compose-ui-tooling` y `compose-ui-tooling-preview` | No hay `@Preview` |
| 🟡 | `Sesion.buscar()`, `PulsoVigente.instante()`, `ArchivoSesion.paradaPorError()`, `RegistroSesion.tamano` | Solo tests. Borrados `instante()` y `paradaPorError()`; `buscar()` lo usan muchos tests de `SesionTest` y `tamano` comprueba la capacidad del registro: se quedan |
| ✅ | `DispositivosUiState.pideDespertar`, `SesionesUiState.sesionAConfirmar` | Solo tests |
| ✅ | `MandoZwiftCodec`: `RespuestaHandshake.ECO`/`RIDE_ON_02_00`, `RespuestaInfo.NINGUNA`/`NOMBRE`/`PAGINA_CLICK_V2`, `reposo()`, `TablaBotones.PROTO`; frenos (`FrenoMando`, rama `campo == 2`); `CriterioAnuncio.servicio(prefijoNombre)` | Solo tests (variantes del prototipo). Borrado todo salvo los frenos en `endurecer-dircon-ble`. Los frenos, borrados el 29-10-2026: el freno de F9.5 va por las palancas de `EstadoMandoZwift` (`AnalogPaddles`), así que sobraban `FrenoMando`, `CodificacionFreno` y la rama de `AnalogButtons`; `botones()` recibe solo botones y palancas, y la trama no cambia (`EstadoMandoZwiftTest` sin tocar) |
| ✅ | `ic_expand_more`, `ic_landscape` | Generados para la web y la guía; que los quite `shrinkResources` (filtrarlos rompe `IconosSincronizadosTest`) |
| ✅ | `AppGraph.ambitoApp`, `AppGraph.renombrador` | Públicos, solo usados dentro: `private` (`ambitoApp` desde el bloque 1) |

## Comprobado y sin problemas

- `FileProvider` limitado a `files/sesiones/`, no exportado, solo lectura temporal; `RepositorioSesiones.archivo()` impide salir de la carpeta.
- DataStore escribe de forma atómica; el registro es circular (10 000 entradas) y `DatosSesion` usa una ventana acotada.
- Listado y borrado de sesiones en IO; las colecciones mutables de `Sesion` solo se tocan desde el hilo de control.
- Todos los `collectAsState` son `collectAsStateWithLifecycle`; ningún ViewModel guarda `Context`.
- Parsers mDNS, Square, telemetría, pulso, ventilador y `DirconCodec.peticion` comprueban longitudes; la potencia ERG ya llega limitada por `AjusteErg`.
- Los flags de depuración están protegidos con `BuildConfig.DEBUG` o en `src/debug`.
- Sin secretos, rutas `/Users/`, `TODO`/`FIXME`, `shell=True` ni CDNs externos.

## Decisiones

### A4: reabrir la app mientras se cierra la sesión (29-10-2026)

**Decisión:** no se corrige; queda como limitación conocida.

**Qué pasa:** si se reabre la app mientras la sesión se está cerrando (tras Atrás en Rodaje o «Terminar» en la notificación), la actividad se ata a la sesión que termina y la app se cierra sola al acabar. Abriéndola otra vez funciona con normalidad.

**Por qué no se corrige:**

- La ventana es corta: el cierre guarda la marcha, deja el Neo en 0 % (como mucho 0,5 s) y desconecta, normalmente en menos de un segundo; solo se alarga si una desconexión se cuelga (como mucho 1,5 s cada una).
- No hay pérdida de datos ni riesgo: la marcha ya está guardada y el Neo queda en llano. El efecto es que la app se cierra una vez sin que el usuario la cierre.
- Hace falta un gesto poco habitual: cerrar y reabrir casi a la vez.
- La corrección prevista (una sesión nueva que conviva con la que termina sobre los mismos clientes BLE y espere a que acabe) necesitaría un change, tocaría la conexión y `RideService`, y añadiría riesgo para un caso de segundos.

**Si llega a molestar:** la alternativa barata es que, al abrir durante el cierre, la pantalla de inicio se mantenga hasta que la sesión anterior termine y entonces se cree la nueva (`AppGraph.iniciarSesion` y `MainActivity`), sin dos sesiones vivas a la vez.

## Plan

1. **Bloque 1, sin change** (bugs sin impacto funcional): C1, C2, C3, A5, errores de lint (B5, M12), código muerto y B4. Hecho, salvo A4 (aceptado como limitación conocida, ver «Decisiones») y A6 (deshecho tras probarlo en el móvil); ver sus filas. Tras él: 1308 tests y `ktlintCheck` en verde, `lintRelease` sin errores.
2. **Change «Endurecer DirCon y BLE»**: A1, A2, A3, M8–M11, B1–B3.
3. **Change «Preparar release»** (o dentro de `publicar-repo-publico`): A7, A8, A9, M7. Hecho en `preparar-release` (change 33) salvo A9, que va en `publicar-repo-publico`; `shrinkResources` quita `ic_expand_more` e `ic_landscape` del APK.
4. **Dividir `Sesion.kt`** (1893 líneas): mover los tipos (113-289) a `EstadoRodaje.kt`, sacar el ventilador (1052-1211) y, con `SesionTest` como red, el gestor de conexiones. **Hecho** (29-10-2026), sin cambiar el comportamiento: `Sesion.kt` pasa de 1933 a 1279 líneas, con los tipos en `EstadoRodaje.kt`, el ventilador en `VentiladorSesion.kt` y las conexiones en `ConexionesSesion.kt`; 1356 tests y `ktlintCheck` en verde. Las líneas de `Sesion.kt` citadas en esta auditoría son las de antes de dividirlo.
5. El resto de medios y bajos, por orden de impacto en rodaje (M1, M2, M3 primero). Hechos, sin change, M1, M2, M3, M4, M5, M6, M13, B4, B6, B7, B8, B9 y B10: M1 mide la rejilla con cada valor cambiado por su plantilla si cabe en ella (`datosParaMedir`), así que solo se vuelve a medir cuando cambia algo que mueve la cifra; M3 actúa al levantar el dedo o al empezar a repetir. B10 valida `capturas.json` y los identificadores de los textos antes de generar, escapa todo lo que va a atributos y recorta con `sips` o, fuera de macOS, con Pillow.
