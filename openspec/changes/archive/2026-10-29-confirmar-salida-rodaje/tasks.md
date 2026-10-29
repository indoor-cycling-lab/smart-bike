# Tasks

## 1. Maqueta

- [x] 1.1 Añadir `.sq-exit-notice` a `design-system/components.css` con tokens existentes y crear `design-system/components/ExitNotice/` (`README.md` y `preview.html`, con el texto en una y en varias líneas); regenerar con `python3 scripts/design-system/generar.py` y verificar que el contraste en los dos temas pasa y que la guía lo muestra
- [x] 1.2 Añadir a `maquetas/rodaje.html` y `maquetas/rodaje-vertical.html` (con `data-mock-states` y `data-mock-state`) los estados `salida-confirmar` y `salida-cerrando`, con el aviso flotante, translúcido y centrado sobre la pantalla (posición y estilo elegidos por el usuario sobre dos variantes); verificar con `python3 -m http.server 8000` que ambos estados se ven en los dos temas y que nada se mueve
- [x] 1.3 Comprobar el estado vertical a 360 dp (texto entero en varias líneas), con el aviso de acción a la vez y con los botones − y +, y actualizar `maquetas/index.html` y la tabla de componentes de `design-system/README.md` si procede
- [x] 1.4 Regenerar las capturas de la web con `python3 scripts/sitio/capturas.py` si Rodaje cambia, y verificar que `git status` solo muestra lo esperado (no hace falta: los estados de la web no cambian y `site/` queda intacto)
- [x] 1.5 Validación de la maqueta por el usuario: dar los enlaces `maquetas/rodaje.html#estado=salida-confirmar`, `maquetas/rodaje.html#estado=salida-cerrando`, `maquetas/rodaje-vertical.html#estado=salida-confirmar`, `maquetas/rodaje-vertical.html#estado=salida-cerrando` (con `&tema=claro` para el claro) y la sección `ExitNotice` de la guía, y esperar su aprobación explícita de la versión final (si pide cambios, ajustar maqueta, `design.md` y specs y volver a pedir validación)

## 2. App

- [x] 2.1 Añadir `SalidaUi` y `atras()` / `cancelarSalida()` en `RodajeViewModel` (ventana `VENTANA_SALIDA_MS = 3_000`), con tests con tiempo virtual: primer Atrás → `Confirmar`; segundo a tiempo → `Cerrando` y `terminarSesion()` una vez; segundo tarde → vuelve a `Confirmar` sin terminar; Atrás en `Cerrando` sin efecto; `cancelarSalida()` limpia `Confirmar` pero no `Cerrando`
- [x] 2.2 Incluir la salida en `RodajeUiState` y verificarlo en `RodajeUiStateTest`
- [x] 2.3 Crear `ExitNotice` en `ui/componentes/` (reproduce `sq-exit-notice`, varias líneas, región viva) y mostrarlo en `Rodaje.kt` flotando sobre el contenido, sin cambiar la medida de nada, en horizontal, vertical y pantalla baja; verificar con una prueba de Compose o de medidas, como las de `PantallaBajaTest`, que aparecer no mueve elementos
- [x] 2.4 Cambiar `RodajeRuta` a `BackHandler { vm.atras() }` y `DisposableEffect` que llama a `cancelarSalida()`; actualizar su comentario de documentación («Atrás termina la sesión» → doble Atrás)
- [x] 2.5 Añadir `salida_confirmar` y `salida_cerrando` a `strings.xml` y, si `idioma-es-en` ya está integrado, a `values-en`; si no, anotarlos en las tareas de ese change; verificar que no hay textos fijos en el código
- [x] 2.6 Ejecutar `./gradlew :app:testDebugUnitTest ktlintCheck` (y `ktlintFormat` si falla) y verificar que pasan, incluidos `TokensSincronizadosTest` e `IconosSincronizadosTest`
- [x] 2.7 Probar en el OnePlus 8 con la skill `desplegar-movil`, con el Neo y el Square conectados: un Atrás suelto no cierra; dos seguidos cierran con «Cerrando…» y dejan el Neo libre y sin notificación; uno y otro a más de 3 s no cierran; con botón y con gesto de navegación; anotar cuánto dura «Cerrando…». Pedir al usuario lo que requiera su manos (pedalear, pulsar el Square, Zwift)

## 3. Documentación

- [x] 3.1 Actualizar la documentación: revisar `docs/funcional.md` (F11: salida con doble Atrás), `docs/arquitectura.md`, `docs/planificacion.md` (fila 35 `confirmar-salida-rodaje`, «En curso» al crear y «Archivado» al archivar) y `docs/desarrollo.md`, actualizar lo que haya cambiado y dejar constancia de lo que no, y verificar que ninguna doc dice ya que Atrás termina la sesión con una pulsación

## Workflow follow-up

- Archivar el change con `/openspec-archive-change` cuando las pruebas en el móvil estén hechas.
