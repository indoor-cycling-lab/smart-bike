# Tasks

## 1. Maqueta

- [x] 1.1 Ajustar la variante tocable de `ModeBadge` en `design-system/`: sin glifo ⇄, con relleno tintado con el color del modo, borde más marcado, estado de pulsación y objetivo de 48 dp con el aspecto de 32 dp (la forma de botón es el indicio, no solo el color) en `components.css`, su `README.md` y `preview.html`, y regenerar con `python3 scripts/design-system/generar.py`; verificar que el generador termina sin error y que la guía muestra la variante en los dos temas
- [x] 1.2 Ajustar en `maquetas/rodaje.html` y `maquetas/rodaje-vertical.html` los estados con el cambio de modo activado y el indicador tocable sin glifo (Libre, SIM, ERG manual y ERG APP), con el cambio de Libre o SIM a ERG manual y su aviso del pie, y la etiqueta con los botones − y + visibles y el cambio de modo desactivado, en el selector de estado (`data-mock-state`); verificar que cada estado se abre por su `#estado=…` y que el indicador, Ajustes y la barra de conexiones no se solapan a 360 dp en vertical ni a la anchura mínima en horizontal
- [x] 1.3 Actualizar `maquetas/ajustes.html`: «Botones − y + en pantalla» vuelve a su nombre y ayuda originales y se añade la casilla «Cambiar de modo tocando» con su ayuda (activada y desactivada) y añadir los estados nuevos a `maquetas/index.html`; verificar que los enlaces del índice abren los estados
- [x] 1.4 Validación de la maqueta por el usuario: dar los enlaces exactos (`maquetas/rodaje.html#estado=…`, `maquetas/rodaje-vertical.html#estado=…`, `maquetas/ajustes.html#estado=…` y la sección de `ModeBadge` en la guía) y esperar su aprobación explícita; si pide cambios, ajustar la maqueta, `design.md` y los specs, y volver a pedir validación

## 2. App

- [x] 2.1 Añadir `modoTactil: Boolean = false` a `Configuracion` (con su copia de seguridad y su ajuste en `AjustesViewModel`), `RodajeViewModel.alternarModo()`, que envía `Accion.AlternarErgManual` por el mismo camino que las palancas, y `modoTocable` a `RodajeUiState` desde `modoTactil`; verificar con tests unitarios que un documento anterior sin el campo lo lee desactivado, que el valor viaja en la copia de seguridad, que con el ajuste activado el toque alterna Libre o SIM ⇄ ERG manual, que en un entrenamiento de la app no cambia el modo y avisa, que sin Neo conectado alterna igual y que con los botones visibles y el ajuste apagado `modoTocable` es falso
- [x] 2.2 Hacer tocable `ModeBadge` según la maqueta aprobada (objetivo de `touchMin`, `Role.Button`, descripción accesible con la acción) y conectarlo en `Modo()` de `Rodaje.kt`, en horizontal y en vertical; verificar con un test de instrumentación que el toque alterna el modo con el ajuste activado y no hace nada sin él, con o sin los botones − y + visibles, y que el objetivo táctil mide al menos 48 dp sin solaparse con Ajustes
- [x] 2.3 Añadir la casilla «Cambiar de modo tocando» y su ayuda en `Ajustes.kt` y en los recursos de texto en español e inglés, sin tocar la de los botones; verificar que `TextosInglesTest` pasa, que el test de instrumentación de Ajustes lee la casilla y su ayuda y que activar los botones no activa el cambio de modo
- [x] 2.4 Ejecutar `./gradlew :app:testDebugUnitTest ktlintCheck` y corregir lo que falle con `./gradlew ktlintFormat` o a mano; verificar que ambos terminan sin errores
- [x] 2.5 Probar con el equipo real en el OnePlus 8 con el Neo, sin Square asignado y con «Cambiar de modo tocando» activado: alternar tocando el indicador, comprobar que el Neo mantiene la potencia de ERG manual a dos cadencias y vuelve a la pendiente de la marcha en Libre, y repetir en una ruta y en un entrenamiento de la app; verificar que el comportamiento es el de los escenarios del spec de `manual-erg`

## 3. Documentación

- [x] 3.1 Regenerar las capturas de la web con `python3 scripts/sitio/capturas.py` si cambian las de Rodaje o Ajustes; verificar que `site/img/` refleja el indicador de modo y la casilla
- [x] 3.2 Actualizar la documentación: revisar `docs/funcional.md` (F7.3, F11.5, F11.8 y F12.7, y la casilla nueva), `docs/arquitectura.md`, `docs/planificacion.md` (la fila de `modo-tactil-rodaje` y su nota, y que `guia-inicio` pasa a depender de él) y `docs/desarrollo.md`, y actualizar lo que el change haya cambiado; verificar que la fila del change dice «En curso» o «Archivado» según corresponda

## Workflow follow-up

- Archivar con `/openspec-archive-change` cuando estén hechas las tareas, con las specs sincronizadas en `openspec/specs/`.
