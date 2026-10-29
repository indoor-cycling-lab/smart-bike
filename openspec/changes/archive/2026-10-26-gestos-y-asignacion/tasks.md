# Tasks

## Maqueta

### 1. Sistema de diseño

- [x] 1.1 `ButtonMappingRow`: variante de fila de Zwift solo informativa (sin chevron ni estado táctil, distinta de la fila enlace también sin color) y fila de acción «Sin botón» en `ink-muted`; actualizar `components.css`, `components/ButtonMappingRow/README.md` y `preview.html` (spec delta de `ui-components`)
- [x] 1.2 `CapturePanel`: estado sin Square («Conecta el Square para capturar un botón», sin borde de escucha) y lista de entradas de la acción con un botón de quitar de `touch-min` (icono `close` de Material Symbols copiado en `icons.svg` si hace falta); actualizar `components.css`, `README.md` y `preview.html`
- [x] 1.3 Regenerar con `python3 scripts/design-system/generar.py`; verificar que termina sin errores de contraste en los dos temas, que la guía muestra las variantes nuevas en los dos temas y, si se añadió un icono, que se generó su `ic_*.xml`

### 2. Maquetas

- [x] 2.1 `maquetas/botones.html`: grupo «Funciones de Zwift» informativo con su explicación (Square emparejado en Zwift), grupo «Acciones de la app» con una fila por acción de la spec `button-mapping` («+1 marcha», «−1 marcha», «+3 marchas», «−3 marchas», «Marcha más dura», «Marcha más ligera», «Volver a la marcha de referencia», «Fijar la marcha de referencia») con sus entradas y gestos por defecto, sección «Gestos» con los cuatro tiempos (`Stepper` en ms, con sus límites) y el interruptor de autorrepetición, interruptor «Invertir palancas» y «Restaurar asignación por defecto»; estados `lista` (por defecto), `modificada` (una acción «Sin botón», una combinación nueva e «Invertir palancas» activado), `restaurar` (`Notice` de confirmación con «Restaurar» y «Cancelar») y `desde-diagnostico` (botón de volver a Diagnóstico, sin barra inferior); verificar cada estado contra la spec delta de `screen-mockups` en los dos temas y a 360 dp sin desplazamiento horizontal
- [x] 2.2 `maquetas/asignar.html`: entradas actuales de la acción con quitar, zona de captura, gesto segmentado (Pulsar, Corta, Larga, Doble; «Pulsar» deshabilitado para un Campagnolo solo) y estados `esperando`, `sin-square`, `detectado` (combinación libre), `en-uso` (la palanca D1 dejará de hacer «+1 marcha») y `zwift` (A capturada con el aviso de Zwift), con «Guardar» deshabilitado sin captura; verificar en los dos temas
- [x] 2.3 `maquetas/diagnostico.html`: enlace «Asignación de botones» en la sección de botones del Square que abre `botones.html#estado=desde-diagnostico`, y «Último gesto» con un gesto no «pulsar» en algún estado (por ejemplo «□ · larga · Fijar la marcha de referencia»); verificar en los dos temas
- [x] 2.4 Actualizar `maquetas/index.html` con los estados nuevos de Botones, Asignar acción y Diagnóstico y sus funcionalidades (F3, F4, F12.4); verificar que cada enlace abre su estado

### 3. Validación

- [x] 3.1 Validación de la maqueta por el usuario: darle los enlaces exactos (`maquetas/botones.html#estado=lista`, `#estado=modificada`, `#estado=restaurar`, `#estado=desde-diagnostico`, `maquetas/asignar.html#estado=esperando`, `#estado=sin-square`, `#estado=detectado`, `#estado=en-uso`, `#estado=zwift`, `maquetas/diagnostico.html#estado=conectado` y las secciones `ButtonMappingRow` y `CapturePanel` de la guía) y esperar su aprobación explícita; si pide cambios, ajustar maqueta, `design.md` y specs y volver a pedirla

## App

### 4. Traza de combinaciones (antes del reconocedor, design.md decisión 6)

- [x] 4.1 Grabar con el Square real, desde Diagnóstico y la skill `desplegar-movil`, la traza pendiente del change 3: Campagnolo + palanca de cada lado, freno + palanca, los dos Campagnolo + palanca, una palanca mantenida 3 s, △ doble rápida y el apagado del Square con los dos Campagnolo e I1 y D1 en el orden en que lo hace el usuario; guardarla en `nucleo/src/test/resources/trazas/` con el formato de las anteriores
- [x] 4.2 Analizar la traza de apagado: si los cuatro botones llegan pulsados a la vez antes de la desconexión y cuánto tardan las sueltas al mantener; anotar en `design.md` los retrasos observados; si la última pulsación no llega antes de perder el Square, parar y actualizar la decisión 6 (`/openspec-update-change`)

### 5. Dominio (`:nucleo`)

- [x] 5.1 `Entrada`, `TipoGesto` y `Gesto` nuevos; `Accion` `@Serializable` con `@SerialName` estables y `FijarReferencia`; `Cambio` con la referencia en cada llamada y `FijarReferencia` (decisiones 1, 4 y 5); adaptar `CambioTest` y añadir el caso de fijar; verificar con `./gradlew :nucleo:test`
- [x] 5.2 `Asignacion` como dato con `POR_DEFECTO`, búsqueda con inversión de palancas, `asignar` y `quitar` con las reglas que se pierden (misma entrada y gesto, y `Pulsar` frente a `Corta`/`Larga`/`Doble`) y acciones desconocidas descartadas al leer (decisión 4); tests de la asignación por defecto, de cada escenario de `button-mapping` («Aviso de entrada en uso», «Invertir las palancas», «Varias entradas por acción», «Restaurar») y de ida y vuelta en JSON; verificar con `./gradlew :nucleo:test`
- [x] 5.3 `ReconocedorGestos` con `procesar`, `vencer` y `proximoVencimiento`, según la tabla de la decisión 2 y los `TiemposGestos`: pulsar al instante, corta, larga al umbral, doble, corta tras la ventana, repetición con retardo y ritmo, parar la repetición, Campagnolo como modificador y solo, los dos Campagnolo sin gesto, cambio de asignación con botones pulsados (decisiones 1–3); tests con reloj virtual de cada escenario de `button-gestures` y reproducción de la traza de 4.1 (palanca mantenida, △ doble, combinaciones); verificar con `./gradlew :nucleo:test`
- [x] 5.4 `Configuracion` `@Serializable` con `version = 1`, sección `botones` (asignación, tiempos con sus límites, autorrepetición) y `marchaReferencia`; documento ilegible o inválido → valores por defecto (decisión 5); tests en `ConfiguracionTest`: valores por defecto, ida y vuelta, documento dañado, tiempos fuera de límites, regla con acción desconocida; verificar con `./gradlew :nucleo:test`
- [x] 5.5 Sustituir el uso de `Asignacion.accion` fija en `AsignacionTest`, `ReconocedorGestosTest` y `RodajeTrazaTest` por la asignación por defecto, con los mismos resultados en las trazas del change 4; verificar con `./gradlew :nucleo:test`

### 6. Sesión (`:app`)

- [x] 6.1 `Almacen` con `configuracion.json` (`configuracion: Flow`, `actualizarConfiguracion`) y `AppGraph` que la pasa a la `Sesion` como `StateFlow` (decisión 5); test de lectura de un documento dañado si es posible en JVM; verificar con `./gradlew :app:testDebugUnitTest`
- [x] 6.2 `Sesion` con el reconocedor nuevo: temporizador único en el hilo de control, gestos de `vencer` por el mismo camino caliente, configuración viva (asignación, tiempos, referencia), repetición con tope avisado una vez, `FijarReferencia` que actualiza el estado y guarda, ERG de Zwift con `Repetir` y □ larga (decisiones 3, 5 y 8); tests en `SesionTest` con reloj virtual: palanca mantenida, autorrepetición desactivada, tope durante la repetición, Square perdido con la palanca mantenida, □ corta y larga, referencia guardada, cambio de asignación en caliente, ERG con repetición; verificar con `./gradlew :app:testDebugUnitTest`
- [x] 6.3 Deshacer la ráfaga de apagado según lo validado en 4.2 (decisión 6); tests en `SesionTest` con los dos órdenes de la spec y con la traza de 4.1; verificar con `./gradlew :app:testDebugUnitTest`
- [x] 6.4 Modo captura en la `Sesion` (`captura: StateFlow<EstadoCaptura>`): sin Square, esperando, entrada capturada (botón con Campagnolo, Campagnolo solo), sin ejecutar acciones, fin al cancelar y vuelta a sin Square al perderlo (decisión 7); tests en `SesionTest`; verificar con `./gradlew :app:testDebugUnitTest`
- [x] 6.5 `Vibrador` con `larga()` y `triple()` y elección por gesto en la `Sesion` (decisión 9); tests con el doble de `Vibrador` en `SesionTest` (larga, doble, tope, sin acción); verificar con `./gradlew :app:testDebugUnitTest`
- [x] 6.6 Medir que el camino caliente no empeora: test en `SesionTest` de que una palanca con la asignación por defecto entrega el comando al Neo en el mismo paso que su notificación, sin temporizador; verificar con `./gradlew :app:testDebugUnitTest`

### 7. Pantallas (`:app`)

- [x] 7.1 Composables `ButtonMappingRow` y `CapturePanel` en `ui.componentes`, como en la guía (variantes de 1.1 y 1.2); verificar con `./gradlew :app:assembleDebug`
- [x] 7.2 `TextosAccion`: nombre de cada acción para Botones y aviso «Referencia en la N»; `TextoGesto` con «pulsar», «corta», «larga», «doble», «repetir»; tests de los textos; verificar con `./gradlew :app:testDebugUnitTest`
- [x] 7.3 `BotonesViewModel` y `Botones`: grupos de Zwift y de la app, «Sin botón», tiempos con sus límites, autorrepetición, «Invertir palancas» y restaurar con confirmación, todo guardado en `Almacen`; tests de `BotonesUiState` y `BotonesTest` con los estados `lista`, `modificada`, `restaurar` y `desde-diagnostico` de la maqueta; verificar con `./gradlew :app:testDebugUnitTest connectedDebugAndroidTest`
- [x] 7.4 `AsignarAccionViewModel` y `AsignarAccion`: entradas de la acción con quitar, captura desde `Sesion.captura`, gesto con «Pulsar» deshabilitado para un Campagnolo solo, avisos de entrada libre, en uso y de Zwift, «Guardar» y «Cancelar»; tests de `AsignarAccionUiState` y `AsignarAccionTest` con los estados `esperando`, `sin-square`, `detectado`, `en-uso` y `zwift`; verificar con `./gradlew :app:testDebugUnitTest connectedDebugAndroidTest`
- [x] 7.5 Navegación: rutas `botones` y `asignar/{accion}` en `MainActivity`, enlace «Asignación de botones» en Diagnóstico y «Último gesto» con el tipo de gesto; ampliar `DiagnosticoUiStateTest` con los escenarios nuevos de la spec delta de `diagnostics`; verificar con `./gradlew :app:testDebugUnitTest connectedDebugAndroidTest`
- [x] 7.6 Aviso «Referencia en la N» y marca de referencia que se mueve en Rodaje; ampliar `RodajeUiStateTest` con «Fijar la referencia» y «Repetición» de la spec delta de `ride-screen`; verificar con `./gradlew :app:testDebugUnitTest`
- [x] 7.7 Desplegar en el OnePlus 8 (skill `desplegar-movil`) y comparar capturas de Botones, Asignar acción (esperando y con una combinación capturada) y Diagnóstico con `maquetas/botones.html#estado=desde-diagnostico`, `maquetas/asignar.html#estado=detectado` y `maquetas/diagnostico.html#estado=conectado`

### 8. Validación con el equipo real

- [ ] 8.1 Gestos con el Square: cada fila de la asignación por defecto (palancas, Campagnolo + palanca, □ corta y □ larga), la palanca mantenida contando marchas (¿se cuela alguna al soltar?), la autorrepetición desactivada y una doble pulsación en △ asignada; latencia de la palanca en Diagnóstico del mismo orden que antes (NF1); anotar resultados en `design.md` y, si hace falta, ajustar los tiempos por defecto en la spec y el diseño (`/openspec-update-change`)
- [ ] 8.2 Asignar pulsando: reasignar Campag. D + △ corta a «−3 marchas», cerrar y abrir la app y comprobarlo; invertir palancas; restaurar; comprobar la vibración por gesto con la pantalla apagada
- [ ] 8.3 Apagar el Square con su combinación en Rodaje: la marcha no cambia y el Neo sigue con la resistencia de antes; repetir en los dos órdenes
- [ ] 8.4 En Zwift (si está a mano): con el Square emparejado, X y Círculo siguen girando y las palancas mantenidas repiten; en ERG de Zwift, la palanca mantenida sube 10 W por repetición

## Documentación

- [x] 9.1 Actualizar la documentación: revisar `docs/funcional.md` (F3 y F4 cumplidas en lo que corresponda, anexo A.2 y A.3 con lo que ya funciona y lo que llega con los changes 9, 13 y 15, tiempos por defecto), `docs/arquitectura.md` (§ 4.3 `ReconocedorGestos` con los tipos nuevos y `Asignacion` configurable, § 5.3 `FijarReferencia`, § 6.1 temporizador del reconocedor, § 9.2 captura de entradas y Botones desde Diagnóstico, § 10 `configuracion.json` en uso), `docs/planificacion.md` (fila y notas del change 7, y lo que pase a los changes 9, 10 y 13: acciones nuevas en la asignación por defecto, barra inferior, botones reasignados que siguen llegando a Zwift) y `docs/desarrollo.md` (traza de combinaciones, si cambia cómo se graba)
