# Tasks

## Maqueta

### 1. Maqueta de rodaje

- [x] 1.1 Añadir a `maquetas/rodaje.html` los estados `neo-conectando` (chip «Neo conectando» con el anillo de espera, Square con el punto lleno, potencia y cadencia «—», pie «Pedalea para despertar el Neo»), `square-reconectando` (chip «Square conectando», datos con valor, pie «Pulsa un botón del Square para despertarlo») y `conectando` (los dos chips «conectando», datos «—», pie «Pedalea y pulsa un botón del Square para despertarlos»), con los chips enlazando a `diagnostico.html` y solo clases del sistema de diseño (design.md, decisión 3); verificar cada estado con `#estado=…` en los dos temas y contra la spec delta de `screen-mockups`
- [x] 1.2 Añadir los mismos tres estados a `maquetas/rodaje-vertical.html`, cada uno en su `section`, con el hueco del tope reservado; verificar a 390 dp y a 360 dp de ancho, en los dos temas, que nada se mueve respecto a `#estado=sin-zwift` y que el pie más largo cabe sin desplazamiento

### 2. Maqueta de Diagnóstico

- [x] 2.1 En `maquetas/diagnostico.html`, sustituir el estado `square-perdido` por `square-reconectando` (fila «Reconectando» con «Pulsa cualquier botón para despertarlo», aviso informativo `sq-notice` sin `is-danger` con el icono `info` y el texto «Reconectando con el Square. Pulsa cualquier botón para despertarlo.», teclas sin pulsar, máscara «—», Neo conectado, «Buscar dispositivos» disponible), añadir `conectando` (las dos filas «Conectando» con su ayuda, sin aviso, «Buscar dispositivos» disponible) y cambiar las filas de `bluetooth-apagado` a «Sin conexión»; verificar cada estado contra la spec delta de `screen-mockups` en los dos temas
- [x] 2.2 Comprobar en la guía que `ConnectionChip` (`wait` con «conectando») y `Notice` informativo cubren todo sin variantes nuevas; si falta alguna, añadirla a `design-system/` (`components.css`, `README.md` y `preview.html`), regenerar con `python3 scripts/design-system/generar.py` y verificar que termina sin errores de contraste
- [x] 2.3 Actualizar `maquetas/index.html` con los estados nuevos de Rodaje (horizontal y vertical) y Diagnóstico y su funcionalidad (F1); verificar que cada enlace abre su estado y que ninguno apunta a `square-perdido`

### 3. Validación

- [x] 3.1 Validación de la maqueta por el usuario: darle los enlaces exactos (`maquetas/rodaje.html#estado=neo-conectando`, `#estado=square-reconectando`, `#estado=conectando`, `maquetas/rodaje-vertical.html#estado=neo-conectando`, `#estado=square-reconectando`, `#estado=conectando`, `maquetas/diagnostico.html#estado=conectando`, `#estado=square-reconectando` y `#estado=bluetooth-apagado`) y esperar su aprobación explícita; si pide cambios, ajustar maqueta, `design.md` y specs y volver a pedirla

## App

### 4. Dispositivos recordados (`:nucleo` y `:app`)

- [x] 4.1 `EstadoGuardado` versión 2 con `rodillo` y `mando` (`DispositivoRecordado(direccion, nombre)`) y migración desde la versión 1 (`direccionRodillo` → `rodillo` con el nombre `Tacx Neo`) (design.md, decisión 1); tests en `ConfiguracionTest`: ida y vuelta JSON, fichero de ejemplo de la versión 1 migrado, versión 1 sin dirección, documento ilegible → valores por defecto; verificar con `./gradlew :nucleo:test`
- [x] 4.2 La `Sesion` recuerda el Neo y el Square al llegar a `Listo` (no al encontrarlos) y sustituye al anterior del mismo tipo; tests en `SesionTest` con el almacén en memoria: primera conexión recordada, otro Square sustituye al recordado, un `Fallo` tras encontrarlo no se recuerda; verificar con `./gradlew :app:testDebugUnitTest`

### 5. Conexión y reconexión (`:app`)

- [x] 5.1 `ConexionBle.conectarEnSegundoPlano(direccion)` en `ClienteBle` con `useAutoConnect(true)` sin límite de tiempo, que vuelve al conectar y se cancela con `desconectar()` (design.md, decisión 2); dobles de `ConexionSquare` y `ConexionNeo` de `SesionTest` con el modo de conexión; verificar con `./gradlew :app:testDebugUnitTest :app:assembleDebug`
- [x] 5.2 `EstadoBluetooth.permiso` y `AppGraph.comprobarPermisos()` llamado desde `MainActivity.onResume`, junto al arranque del servicio (decisión 5); verificar con `./gradlew :app:testDebugUnitTest :app:assembleDebug`
- [x] 5.3 `EstadoDispositivo` con `Conectando` y `Reconectando` en lugar de `Perdida`, e intento con los recordados en la `Sesion` (directa y, si falla, en segundo plano) al abrir con permiso y Bluetooth, al perder un dispositivo y al volver el Bluetooth; con el Bluetooth apagado, `SinConexion` (decisiones 2, 3 y 5); tests en `SesionTest` con dobles y reloj virtual de los escenarios de `device-connection`: los dos al abrir, Neo dormido al abrir, primer uso sin intento, permiso concedido más tarde, corte breve del Square, Neo perdido con la 17 y reenvío, cambios a la 19 mientras reconecta, Square perdido con Campag. D mantenido, Bluetooth apagado y encendido con la 14, sin intentos al terminar la sesión; verificar con `./gradlew :app:testDebugUnitTest`
- [x] 5.4 «Buscar dispositivos» con un intento en curso: cancela el intento de los no conectados, busca y, sin resultado en 30 s, deja `NoEncontrado` sin volver a intentarlo hasta otra búsqueda, el Bluetooth de vuelta o la siguiente apertura (decisión 4); tests en `SesionTest`: buscar mientras reconecta encuentra otro Square, búsqueda sin resultado, Bluetooth apagado y encendido tras `NoEncontrado`; verificar con `./gradlew :app:testDebugUnitTest`
- [x] 5.5 Lectura de la batería del Square (`0x2A19`) al conectar y cada 10 min, solo al registro (decisión 8); verificar con `./gradlew :app:assembleDebug` y, en el móvil, que el registro muestra el nivel al conectar

### 6. Notificación (`:app`)

- [x] 6.1 `RideService.textoConexiones` con «Conectando el Neo», «Conectando el Square» y «Conectando el Neo y el Square» y los textos actuales sin intento (decisión 6), con sus `strings.xml`; test JVM de la tabla de textos para cada combinación de estados; verificar con `./gradlew :app:testDebugUnitTest`

### 7. Rodaje y Diagnóstico (`:app`)

- [x] 7.1 `RodajeUiState`: el chip dice «conectando» en `Buscando`, `Conectando` y `Reconectando`, y `pie` sustituye a `faltaAlguno` con la ayuda para despertar el Neo, el Square o los dos, o «Toca Neo o Square para conectarlos» (decisión 3); el aviso de acción tiene prioridad sobre la ayuda; tests en `RodajeUiStateTest` con cada fila de la tabla de la decisión 3; verificar con `./gradlew :app:testDebugUnitTest`
- [x] 7.2 `Rodaje` muestra el pie nuevo en horizontal y en vertical como en la maqueta aprobada; `RodajeTest` con los estados `neo-conectando`, `square-reconectando` y `conectando` en las dos disposiciones; verificar con `./gradlew connectedDebugAndroidTest`
- [x] 7.3 `DiagnosticoUiState` y `Diagnostico`: filas «Conectando» y «Reconectando» con la ayuda, aviso informativo `Notice` al reconectar el Neo o el Square con sus textos, «Buscar dispositivos» disponible salvo mientras se busca, y filas «Sin conexión» con el Bluetooth apagado; tests en `DiagnosticoUiStateTest` y `DiagnosticoTest` con los estados `conectando`, `square-reconectando` y `bluetooth-apagado` de la maqueta; verificar con `./gradlew :app:testDebugUnitTest connectedDebugAndroidTest`
- [x] 7.4 Desplegar en el OnePlus 8 (skill `desplegar-movil`) y comparar capturas de Rodaje (con el Square apagado y con el Neo dormido al abrir) y de Diagnóstico reconectando con `maquetas/rodaje.html#estado=square-reconectando`, `maquetas/rodaje-vertical.html#estado=neo-conectando` y `maquetas/diagnostico.html#estado=square-reconectando`

### 8. Validación con el equipo real

- [x] 8.1 Sesión guiada (`desarrollo.md` § 3): el usuario conecta el Neo y el Square con «Buscar dispositivos» (o ya están recordados), termina la sesión y abre la app; se comprueba que los dos se conectan sin tocar nada y que el Neo recibe la marcha guardada; anotar el resultado en `design.md`
- [x] 8.2 Estabilidad de las direcciones y tiempo de reconexión (design.md, decisión 8): el usuario apaga y enciende el Square y desenchufa y enchufa el Neo, con la pantalla encendida y apagada; se comparan las direcciones del registro y se mide del encendido a `Listo`; si la dirección cambia o la reconexión tarda claramente más de 10 s, parar y replantear con el usuario la decisión 2 (`/openspec-update-change`); anotar las medidas en `design.md`
- [x] 8.3 Pruebas pendientes de los changes 3 y 4: apagar el Square con su combinación de apagado (Campag. I + Campag. D + una palanca de cada lado, más de 5 s; `docs/referencias/elite-square.md`); se comprueba en el registro que los cuatro botones se liberan, que el Neo sigue conectado y con la marcha que había al cortarse la conexión y que, al volver el Square, D1 sube una sola marcha; anotar el resultado en `design.md`
- [ ] 8.4 Prueba de 2 h (NF4, design.md, decisión 7): el usuario rueda 2 h con la pantalla apagada, sin cargador y la optimización de batería por defecto, cambiando de marcha de vez en cuando, y apaga y enciende el Square a la hora; se revisa en el registro que no hay cortes del servicio, que la reconexión fue sola y la batería del Square al principio y al final; si el sistema corta el servicio o retrasa la reconexión, parar y decidir con el usuario la exención de batería (con maqueta y `/openspec-update-change` antes de implementarla); anotar el resultado y el consumo del Square en `design.md` y decidir con el usuario si la prioridad sostenida necesita cambios en otro change
  - **Trasladada al change 6 (`puente-zwift-wifi`)** por decisión del usuario (26-10-2026): la reconexión con la pantalla apagada ya está validada (8.2), pero el usuario no puede rodar 2 h antes de archivar. La exención de batería sigue abierta en `arquitectura.md` § 15.

## Documentación

### 9. Documentación

- [x] 9.1 Actualizar la documentación: revisar `docs/funcional.md` (criterios de F1 cumplidos), `docs/arquitectura.md` (§ 8 con la máquina de estados y la conexión directa más `autoConnect`, § 4.4 y § 15 con la exención de batería resuelta, § 7.1 y § 7.2 con la estabilidad de las direcciones y la batería del Square, § 10 con los dispositivos recordados y la versión 2 de `estado.json`), `docs/planificacion.md` (fila y notas del change 5, y lo que pase a los changes 6 y 10) y `docs/desarrollo.md` (cómo hacer la prueba de 2 h y leer el registro), y actualizar lo que el change haya cambiado o decidido; verificar que ningún documento dice que la app no reconecta sola ni menciona el estado «Conexión perdida»
