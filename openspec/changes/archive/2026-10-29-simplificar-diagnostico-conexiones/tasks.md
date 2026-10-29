# Tasks

## 1. Maqueta

- [x] 1.1 Rehacer en `maquetas/diagnostico.html` la sección «Conexiones» como «Datos de conexión» (app de entrenamiento con su dirección, nombre anunciado, simulación de Zwift, batería del Square, pulsómetro y ventilador), sin filas de estado, avisos ni botones de búsqueda, conservando los identificadores de estado. Verificación: abrir `maquetas/diagnostico.html` y recorrer todos los estados del selector, sin filas de estado ni avisos de conexión.
- [x] 1.2 Comprobar en `maquetas/conexion.html` y `maquetas/dispositivos.html` que ya cubren los avisos que salen de Diagnóstico (Bluetooth apagado, sin permiso, sin wifi, reconectando, batería baja) y anotar en `design.md` lo que falte. Verificación: lista de avisos y dónde se ven, en `design.md`.
- [x] 1.3 Actualizar `maquetas/index.html` (descripción y enlaces a los estados afectados) y regenerar `python3 scripts/sitio/capturas.py` si `site/capturas.json` usa Diagnóstico. Verificación: el índice enlaza los estados y las capturas de la web se regeneran sin errores.
- [x] 1.4 Mover la «Simulación de Zwift» a la sección Simulación de `maquetas/diagnostico.html` (solo en el estado `zwift-wifi`) y quitar su fila de «Datos de conexión». Verificación: abrir `#estado=zwift-wifi` y ver la fila en Simulación y no en «Datos de conexión».
- [x] 1.5 Añadir a `maquetas/dispositivos.html` los estados «Neo no encontrado», «Sin permiso de Bluetooth» y «Square con la batería baja» (aviso informativo `sq-notice` bajo el Controlador, con icono y texto, sin tocar sus botones), con sus entradas en `maquetas/index.html`, y regenerar capturas si hace falta. Verificación: abrir `dispositivos.html#estado=neo-no-encontrado`, `#estado=sin-permiso` y `#estado=square-bateria-baja` en los dos temas.
- [x] 1.6 Comprobar que `diagnostico.html#estado=bateria-baja` dice «15 % · baja» sin aviso aparte.
- [x] 1.7 Añadir a `maquetas/diagnostico.html` el estado `todos-los-datos` (Zwift por wifi desde 192.168.0.10, batería del Square 15 % · baja, pulso 128 bpm, ventilador manual 40 % y la simulación de Zwift en Simulación) y comprobar que cabe a 360 dp sin desplazamiento horizontal. Verificación: abrir `diagnostico.html#estado=todos-los-datos` a 360 dp.
- [x] 1.8 Cambiar «Neo» por «rodillo» en los textos de `maquetas/diagnostico.html` y en su índice (etiqueta del estado `neo-no-encontrado`, que conserva su identificador). Verificación: `grep Neo maquetas/diagnostico.html` sin textos visibles.
- [x] 1.9 Validación de la maqueta por el usuario. Dar los enlaces exactos (p. ej. `maquetas/diagnostico.html#estado=conectado`, `#estado=todos-los-datos`, `#estado=zwift-wifi`, `#estado=bateria-baja`, `#estado=pulso-128`, `#estado=ventilador-manual-40`, `#estado=sin-wifi`, y `maquetas/dispositivos.html#estado=sin-permiso`, `#estado=neo-no-encontrado`, `#estado=square-bateria-baja`) y esperar su aprobación explícita.

## 2. App

- [x] 2.1 Quitar de la pantalla de Diagnóstico las filas de estado, los avisos y las acciones de búsqueda de la sección Conexiones y su estado de UI, pintar «Datos de conexión» y mover la simulación de Zwift a Simulación, según la maqueta aprobada; mover `avisosDeConexion` y `accionDeConexiones` a Dispositivos, que los sigue usando; textos en `strings`. Test unitario del estado de UI (datos presentes, ausentes y «—») y verificación con `./gradlew :app:testDebugUnitTest`.
- [x] 2.2 Cambiar «Neo» por «rodillo» en los textos de Diagnóstico de `strings.xml` (tráfico, control manual, «Enviada al rodillo», origen y sentido en las filas de tráfico) y en sus tests; el registro de sesión no cambia. Verificación: `./gradlew :app:testDebugUnitTest` y búsqueda de «Neo» en los textos de Diagnóstico.
- [x] 2.3 Mostrar en Dispositivos, bajo el Controlador, el aviso de batería baja del Square (con el Square conectado y el nivel bajo el umbral; desaparece al subir o al desconectarse), con su texto en `strings`. Test unitario del estado de UI (baja, recuperada, sin conexión y sin Square recordado) y verificación con `./gradlew :app:testDebugUnitTest`.
- [x] 2.4 Actualizar los tests de instrumentación de Diagnóstico a la sección nueva (sin «Buscar dispositivos» ni filas de estado; con las filas de datos). Verificación: se compilan con `./gradlew :app:assembleDebugAndroidTest` y, con el móvil conectado, pasan los de Diagnóstico.
- [x] 2.5 `./gradlew ktlintCheck` y la batería completa de tests sin errores.
- [x] 2.6 Probar en el OnePlus 8 (skill `desplegar-movil`) con Zwift conectado, con el Square desconectado y con el Bluetooth apagado y, si se puede, sin el permiso de Bluetooth y con la batería del Square baja: Diagnóstico sin avisos ni estado repetido y con los datos al día, y los avisos en Dispositivos. Verificación: capturas o registro, y anotar lo que quede pendiente.

## 3. Documentación

- [x] 3.1 Actualizar la documentación: revisar `docs/funcional.md` (F13.1, F13.3 y la nota de Diagnóstico), `docs/arquitectura.md`, `docs/planificacion.md` (fila y sección del change 28, «En curso» al crearlo y «Archivado» al archivarlo) y `docs/desarrollo.md`, y actualizar lo que haya cambiado. Verificación: `git diff docs/` solo con cambios coherentes con el change.
