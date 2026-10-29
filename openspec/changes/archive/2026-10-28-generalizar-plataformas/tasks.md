# Tasks

Cambio sin impacto visual: no hay sección «Maqueta». Cada grupo deja `:core:test`, `:app:testDebugUnitTest` y `:app:assembleDebug` en verde antes de pasar al siguiente (el último paso lo comprueba cada tarea que toca la app).

## 1. Dominio y configuración

- [x] 1.1 Sustituir `ModoConexionZwift` por `ModoConexion` (`FTMS_WIFI` «ftms-wifi», `ZWIFT_RIDE` «zwift-ride») en `Plataforma.kt`, con `MandoEnModo` (`NINGUNO`, `BLUETOOTH_OPCIONAL`, `DENTRO_DEL_MODO`), `AjustesPlataforma(modo, mando, identidad)` y la tabla `Compatibilidad` ya por plataforma (diseño, decisión 1); verificar con `CompatibilidadTest` (modos de Zwift, recomendado, mando por modo, que `Compatibilidad` no ofrece lo no implementado) y `:core:test`.
- [x] 1.2 Renombrar `PuenteZwift` a `IdentidadDispositivo` y sustituir las cuatro identidades de `EstadoGuardado` por `identidades: Map<String, IdentidadDispositivo>` con `ClaveIdentidad(plataforma, modo, identidad)` (decisión 3), versión 3 sin migración; verificar con tests que la clave es estable y distinta por plataforma, modo y nombre, que un `estado.json` antiguo se lee ignorando las identidades viejas y que se crean nuevas.
- [x] 1.3 Pasar `Configuracion` a la versión 9 con `plataforma` y `plataformas: Map<Plataforma, AjustesPlataforma>`, `ajustesPlataforma` de la elegida, `mandoActivo` derivado de la tabla y `normalizada()` por plataforma; quitar `conexionZwift`, `mandoZwift`, `identidad` sueltos y `migrarV7` (decisión 2); verificar con `ConfiguracionTest` y `ConfiguracionImportarTest`: valores por defecto, ida y vuelta del mapa, una configuración de la versión 8 conserva lo que no es de la plataforma y toma el modo, el mando y el nombre por defecto, e importar una copia igual. El escenario «Cada plataforma recuerda lo suyo» queda cubierto en cuanto haya una segunda plataforma (change 11): anotarlo en el test.
- [x] 1.4 Renombrar en `ControlRodillo` lo genérico de Zwift (`SimZwift`, `ErgZwift`, `ZwiftManda`, `zwiftEnEspera`, `zwiftPerdido`, `zwiftAusente`, `terrenoZwift`, `potenciaZwift`) a sus nombres de plataforma, y sus tests (`ControlRodilloZwiftTest` y los de modelos, bajadas y pendiente base); verificar con `:core:test` sin cambiar ninguna expectativa.
- [x] 1.5 Introducir `FuncionMando` (`FuncionZwift` la implementa) y cambiar `Accion.Zwift(funcion)` por `Accion.Mando(funcion)` en `Asignacion` y sus usos (`ZWIFT`, `ZWIFT_POR_DEFECTO`); verificar con `AsignacionTest` y `:core:test`.

## 2. Conexiones y puente de plataforma

- [x] 2.1 Mover `app/.../zwift/` a `plataforma/` y `plataforma/zwift/` según la tabla del diseño (decisión 4) y renombrar `ZwiftBridge` a `PuenteFtms` y `TransporteZwift` a `TransporteDircon`, con sus tests y dobles de test (`DoblesZwift`); verificar con `:app:testDebugUnitTest` sin cambiar expectativas.
- [x] 2.2 Definir `ConexionPlataforma`, `MandoEmulado`, `ReceptorPlataforma` y `UltimoDePlataforma` (decisiones 4 y 6); verificar que compila y que los dobles de test los implementan.
- [x] 2.3 Partir `ConexionZwiftFtms` en `ConexionFtmsWifi` (servidor FTMS por DirCon, telemetría, pulso) y `MandoZwiftBluetooth` (ajuste, Bluetooth, permisos, modo periférico, publicar y parar el mando), con `mando` opcional; verificar con `ConexionFtmsWifiTest` y `MandoZwiftBluetoothTest`, que parten del `ConexionZwiftFtmsTest` actual y mantienen sus casos: terreno y ERG por FTMS, sin servicio de mando por wifi, mando por Bluetooth publicado y parado.
- [x] 2.4 Adaptar `ConexionZwiftRide` a `ConexionPlataforma` con su mando dentro como `MandoEmulado`; verificar con `ConexionZwiftRideTest` sin cambiar expectativas (SET, datos de pedaleo, páginas de información, funciones por wifi).
- [x] 2.5 Extraer `PuentePlataforma` de `Sesion` con `DestinoPlataforma` (decisión 5): usar la conexión del modo, el wifi, `EstadoPlataforma`, la telemetría a 4 Hz, el pulso, el mando y la identidad; `Sesion` conserva lo del Neo y el control. Verificar con `PuentePlataformaTest` (cambio de modo y de nombre rodando, wifi que va y viene, telemetría, plataforma perdida) y `SesionTest` sin cambiar expectativas.
- [x] 2.6 Hacer que `AppGraph` cree la conexión por (plataforma, modo) y tome los ajustes de `Configuracion.ajustesPlataforma`; verificar con `:app:assembleDebug` y `:app:testDebugUnitTest`, e instalar en el móvil (skill `desplegar-movil`) comprobando que la app abre, que Plataformas muestra Zwift, el modo y el nombre por defecto y que el registro no tiene errores.

## 3. Nombres, textos y UI

- [x] 3.1 Renombrar `EstadoZwift` a `EstadoPlataforma`, `ModoConexionZwift` a `ModoConexion` y los campos de `EstadoSesion` y de los `UiState` (`zwift`, `conexionZwift`, `nombreZwift`, `mandoZwift`) en Rodaje, Dispositivos, Plataformas, Ajustes, Botones y Diagnóstico; verificar con sus tests de `UiState` y que las pantallas dicen lo mismo que antes.
- [x] 3.2 Pasar los textos que nombran a Zwift a recursos con el nombre de la plataforma elegida y renombrar los de modo (`modo_ftms_wifi_*`, `modo_zwift_ride_*`); pasar `Origen.Zwift` a `Origen.Plataforma` pintado con el nombre de la plataforma; verificar con `RodajeUiStateTest`, `PlataformasUiStateTest`, `DiagnosticoUiStateTest` y comparando a mano que con Zwift las pantallas dicen lo mismo.
- [x] 3.3 Adaptar los tests de instrumentación (`PlataformasTest`, `RodajeTest`, `BotonesTest`, `DiagnosticoTest`, `DispositivosTest`, `AjustesTest`) y comprobar que compilan con `:app:compileDebugAndroidTestKotlin` (no se ejecutan: `connectedDebugAndroidTest` borra los datos de la app).
- [x] 3.4 Comprobar que el código genérico ya no nombra a Zwift: `grep -rniE "zwift" app/src/main/kotlin/app/smartbike/plataforma/*.kt core/src/main/kotlin/app/smartbike/dominio/Plataforma.kt` solo debe devolver el nombre de la plataforma y comentarios que lo citen como ejemplo, y todo lo propio de Zwift debe estar en `plataforma/zwift/` o llevar `Zwift` en el nombre.

## 4. Specs, documentación y verificación

- [x] 4.1 Validar el change con `openspec validate generalizar-plataformas --strict`; verificar que no da errores ni avisos.
- [x] 4.2 Actualizar la documentación: revisar `docs/funcional.md`, `docs/arquitectura.md`, `docs/planificacion.md` y `docs/desarrollo.md` y actualizar lo que el change haya cambiado o decidido (componentes y paquetes de la arquitectura, la fila y la nota del 24, la fila del 11 con `ftms-bluetooth` y el mapa por plataforma, las rutas que citen `zwift/`); verificar que ninguno cita ya `ModoConexionZwift`, `ConexionZwift`, `PuenteZwift` ni el paquete `zwift/`.
- [x] 4.3 Prueba del usuario con Zwift en el PC, tras emparejar de nuevo una vez: en «Rodillo por wifi (FTMS)» (rodillo, potencia, cadencia, pulso, mando por Bluetooth, terreno y ERG) y en Zwift Ride (mando dentro, terreno y ERG), y cambiar de modo y de nombre rodando; anotar el resultado en `docs/planificacion.md`. La hace el usuario: dile exactamente qué hacer y espera.

## Workflow follow-up

- Archivar el change con `/openspec-archive-change` cuando la prueba del usuario esté hecha, o con la prueba pendiente si el usuario lo decide, y marcarlo «Archivado» en `docs/planificacion.md`.
- Al archivar, `zwift-bridge` se retira (`retire_capabilities`) y sus requisitos quedan en `platform-bridge` y `zwift-ride`.
