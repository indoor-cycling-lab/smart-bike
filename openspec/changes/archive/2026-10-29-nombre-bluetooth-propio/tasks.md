# Tasks

## 1. Maqueta

- [x] 1.1 En `maquetas/conexion.html`, añadir a la sección «Nombre ante la app» la casilla «Usar el nombre también por Bluetooth» (`sq-check`) con su ayuda, que nombra «Smart Bike» y «OnePlus 8», en los estados con «Wifi + Bluetooth» (desmarcada), y en «Solo Bluetooth» mostrar la sección solo con la casilla (sin selector); en «Solo wifi», sin casilla. Verificar recorriendo todos los estados existentes de la maqueta: cada uno cumple «Orden de la pantalla» de la spec `screen-mockups`.
- [x] 1.2 Añadir los estados nuevos `solo-bluetooth-nombre` (casilla marcada, selector visible, ayudas del rodillo y del controlador con «Smart Bike»), `wifi-bluetooth-nombre` (casilla marcada, ayuda del controlador con «Smart Bike») y `no-se-pudo-renombrar` (casilla marcada, `Notice` de alerta con icono y texto, ayudas con «OnePlus 8») a `data-mock-states`, y enlazarlos en `maquetas/index.html`. Verificar que el selector de estado de `revision.js` los muestra y que se ven bien en los dos temas, con objetivos táctiles de al menos 48 dp.
- [x] 1.3 Regenerar las capturas de la web (`python3 scripts/sitio/capturas.py`) si alguna usa la maqueta de Conexión, y comprobar con `git status` que solo cambian las esperadas.
- [x] 1.4 Validación de la maqueta por el usuario: dar los enlaces exactos (`maquetas/conexion.html#estado=inicio`, `#estado=solo-bluetooth`, `#estado=solo-bluetooth-nombre`, `#estado=wifi-bluetooth-nombre`, `#estado=no-se-pudo-renombrar` y `#estado=solo-wifi`, también con `&tema=claro`) y esperar su aprobación explícita antes de pasar a la sección «App».

## 2. App: dominio

- [x] 2.1 `:core`: añadir `nombrePorBluetooth: Boolean = false` a `AjustesConexion`, sin cambiar `Configuracion.VERSION`, y hacer que `identidadEfectiva` admita el nombre elegido con «Solo Bluetooth» cuando está activado. Verificar con `ConfiguracionTest` y `ConfiguracionImportarTest` (`./gradlew :core:test`): valor por defecto, ida y vuelta, un documento sin el campo se lee desmarcado conservando modo y nombre, importar una copia con él marcado, e `identidadEfectiva` en los tres modos con el ajuste activado y desactivado.
- [x] 2.2 `:core`: `NombreBluetooth.deseado(config, servicios, identidad)` (design.md, decisión 3). Verificar con su test: `null` con el ajuste desactivado, sin publicación, con «Solo wifi» y con «Wifi + Bluetooth» sin mando; el nombre con «Solo Bluetooth» y con «Wifi + Bluetooth» y el mando aparte; «KICKR BIKE PRO» con el sufijo de su identidad.
- [x] 2.3 `:core`: `nombreBluetoothOriginal` y `nombreBluetoothPuesto` opcionales en `EstadoGuardado`, sin cambio de versión, y la decisión pura de recuperación (restaurar si el actual es el puesto, no tocar si es otro, borrar los dos campos en ambos casos). Verificar con sus tests: ida y vuelta, documento anterior sin los campos y los tres casos de recuperación.

## 3. App: renombrado

- [x] 3.1 `RenombradorBluetooth` (`ble/`): `aplicar(deseado)` con guardado previo del original, `setName`, espera a `ACTION_LOCAL_NAME_CHANGED` con 3 s como máximo, `StateFlow<EstadoNombre>` (`Propio`, `Renombrado`, `NoSePudo`) y una línea en el registro por cada intento. El acceso al adaptador va detrás de una interfaz para poder probarlo. Verificar con `RenombradorBluetoothTest` y un adaptador falso: renombra y guarda el original; no repite si ya se llama así; recupera; no toca un nombre cambiado por el usuario; `NoSePudo` si `setName` da `false`, si salta `SecurityException` o si no llega la confirmación.
- [x] 3.2 `AppGraph`: crear el renombrador y recuperar al arrancar, antes de `iniciarSesion`, si quedó un nombre pendiente en `estado.json`. Verificar con un test: con campos pendientes se recupera, y sin ellos no se llama a `setName`.
- [x] 3.3 `Sesion`: calcular el nombre deseado donde se llama a `PuentePlataforma.usar` y llamar a `aplicar` antes; volver a anunciar si cambia con el servidor ya anunciado; reintentar al encender el Bluetooth; y en `terminar()`, `aplicar(null)` después de parar los servidores. Verificar con tests de la `Sesion` con el renombrador falso: marcar y desmarcar la casilla rodando, cambiar a «Solo wifi», olvidar el Square con «Wifi + Bluetooth», cambiar de nombre, Bluetooth apagado y encendido, terminar la sesión, y la marcha sin cambios en todos los casos.

## 4. App: interfaz

- [x] 4.1 Conexión (`ui/conexion`): la casilla según el modo, el selector en «Solo Bluetooth» solo con la casilla marcada, las ayudas del rodillo y del controlador con el nombre efectivo por Bluetooth (`EstadoNombre` o el nombre del móvil), y el aviso de «No se pudo cambiar el nombre», todo según la maqueta aprobada; textos en `strings.xml`. Verificar con `ConexionUiStateTest` (los tres modos × casilla × estado del renombrado) y con el test de instrumentación de Conexión compilado (`:app:compileDebugAndroidTestKotlin`).
- [x] 4.2 Diagnóstico: comprobar que «Nombre anunciado» con «Solo Bluetooth» muestra el nombre que tiene de verdad el adaptador (el renombrado o el del móvil) y ajustar si no. Verificar con su test de `UiState`.
- [x] 4.3 Ejecutar `./gradlew ktlintCheck :core:test :app:testDebugUnitTest :app:assembleDebug :app:compileDebugAndroidTestKotlin` y comprobar que todo pasa.

## 5. Prueba con el equipo real

- [x] 5.1 Instalar en el OnePlus 8 (skill `desplegar-movil`). El usuario prueba con «Solo Bluetooth» y la casilla marcada: Zwift lista «Smart Bike» desde el primer momento; se empareja y se rueda con el Neo y el Square; al terminar la sesión, el móvil vuelve a llamarse como antes (en los ajustes de Android). Después, con «KICKR BIKE PRO». La hace el usuario: dile exactamente qué hacer y espera. Anotar el resultado (incluido si Android acepta `setName`) en `docs/arquitectura.md` § 7.3.
- [x] 5.2 El usuario prueba con «Wifi + Bluetooth», un Square configurado y la casilla marcada: el controlador aparece como «Smart Bike» en la lista de mandos de Zwift y no se confunde con el rodillo por wifi. Prueba también un cierre forzado desde los ajustes de Android con el móvil renombrado: al volver a abrir la app, el nombre se recupera. Anotar el resultado.

## 6. Documentación

- [x] 6.1 Actualizar la documentación: `docs/funcional.md` (F8.5 y F12.9 con el ajuste, sin «con el nombre Bluetooth del móvil» como algo fijo), `docs/arquitectura.md` (§ 7.3 transporte por Bluetooth y § 4.4 con el renombrador y su ciclo, § 10 con los campos nuevos de `configuracion.json` y `estado.json`), `docs/planificacion.md` (fila 31 `nombre-bluetooth-propio` «En curso» al crearlo, y su nota) y `docs/desarrollo.md` (revisar; previsiblemente sin cambios). Verificar que no queda ningún texto que diga que por Bluetooth el nombre no se puede cambiar.
- [x] 6.2 Validar el change con `openspec validate nombre-bluetooth-propio --strict` y comprobar que no da errores ni avisos.

## Workflow follow-up

- Archivar el change con `/openspec-archive-change` cuando las pruebas del usuario estén hechas, o con las pruebas pendientes si el usuario lo decide, y marcarlo «Archivado» en `docs/planificacion.md`.
