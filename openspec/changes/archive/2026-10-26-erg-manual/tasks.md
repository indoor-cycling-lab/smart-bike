# Tasks

## Maqueta

### 1. Sistema de diseño y maquetas

- [x] 1.1 Documentar en el README de `RideScreen` (o del componente de la zona `sq-ride__erg`) y en el de `ModeBadge` la variante de ERG manual, centrar la zona ERG en su columna y los datos de `MetricGrid` en su celda, en las dos orientaciones, y la marcha en horizontal: `sq-mode erg` con `<small>MANUAL</small>` y `sq-ride__erg` sin `sq-target` ni `sq-badge-erg` (design.md decisión 6). Si al maquetar hace falta una variante que no sale de lo existente, añadirla a `components.css` con su `preview.html` antes de usarla. Regenerar la guía (`python3 scripts/design-system/generar.py`) y comprobar que pasa el contraste en los dos temas
- [x] 1.2 `maquetas/rodaje.html`: estados `erg-manual` (lineal sin Zwift, «ERG MANUAL», 200 W, pie «ERG manual · 200 W»), `erg-manual-ajuste` (210 W, pie «+10 W»), `erg-manual-tope` (1000 W, pie «Tope · 1000 W»), `erg-manual-zwift` (sobre una ruta de Zwift, chip de Zwift conectado) y `zwift-erg-alternar` (ERG ZWIFT, pie «Entrenamiento de Zwift»); verificar contra las specs delta de `ride-screen` y `manual-erg` en los dos temas
- [x] 1.3 `maquetas/rodaje-vertical.html`: estados `erg-manual` y `erg-manual-tope`, con la potencia en el lugar de la marcha y la fila de datos sin moverse; verificar a 360 dp sin desplazamiento en los dos temas
- [x] 1.4 `maquetas/diagnostico.html`: estado `erg-manual` (modo «ERG · manual», «Potencia fijada» 220 W, «Entrada» 200 W, enviada al Neo 220 W, marcha visible); verificar contra la spec delta de `diagnostics`
- [x] 1.5 `maquetas/botones.html`: en `lista`, `desde-diagnostico` y `shimano`, filas «+10 W» y «−10 W» («Sin botón») y «Alternar SIM ⇄ ERG» (△ larga) después de «Fijar la marcha de referencia»; verificar contra la spec delta de `button-mapping`
- [x] 1.6 Actualizar `maquetas/index.html` con los estados nuevos y sus funcionalidades (F7.2, F7.3); verificar que cada enlace abre su estado

### 2. Validación

- [x] 2.1 Validación de la maqueta por el usuario: darle los enlaces exactos (`maquetas/rodaje.html#estado=erg-manual`, `#estado=erg-manual-ajuste`, `#estado=erg-manual-tope`, `#estado=erg-manual-zwift`, `#estado=zwift-erg-alternar`, `maquetas/rodaje-vertical.html#estado=erg-manual`, `maquetas/diagnostico.html#estado=erg-manual` y `maquetas/botones.html#estado=lista`) y las secciones de la guía tocadas, y esperar su aprobación explícita; si pide cambios (textos de los avisos incluidos), ajustar maqueta, `design.md` y specs y volver a pedirla

## App

### 3. Dominio (`:nucleo`)

- [x] 3.1 `Accion`: `ErgMas`, `ErgMenos` y `AlternarErgManual` con sus nombres serializados; `repetible` con `ErgMas` y `ErgMenos`; `ASIGNABLES` y `asignables(transmision)` con las tres tras `FijarReferencia`; `TODAS` con las tres al final (design.md decisión 3). `AjusteErg.aplicar` con `ErgMas`/`ErgMenos` (±`PASO_W`). Tests en `AjusteErgTest` (±10 W, tope, recortado) y en `AsignacionTest` (orden de las asignables con la lineal y con platos, serialización de las acciones nuevas); verificar con `./gradlew :nucleo:test`
- [x] 3.2 `Asignacion.POR_DEFECTO` y `POR_DEFECTO_PLATOS` con △ larga (sin regla para «+10 W» ni «−10 W»); tests en `AsignacionTest` («ERG por defecto», «△ corta sin acción»); verificar con `./gradlew :nucleo:test`
- [x] 3.3 `ModoControl.ErgManual(entradaW, ajusteW, debajo)` y `ControlRodillo` con `alternarErgManual`, `ajusteErg` para los dos ERG, terreno y potencia de Zwift en ERG manual, Zwift perdido o ausente con `debajo`, y `Manual` sobre `ErgManual` (decisiones 1 y 2). Tests en un `ControlRodilloErgManualTest` nuevo: entrar y salir (vuelve a la pendiente de la marcha), alternar en `ErgZwift` (no permitido), marcha que cambia sin enviarse, terreno de Zwift que solo actualiza `debajo` («Vuelta a la ruta»), entrenamiento de Zwift que termina el ERG manual sin ajuste, «Zwift se desconecta en ERG manual», Neo que vuelve con la potencia vigente, Diagnóstico manual sobre ERG manual, y bajadas o modelo que cambian sin envío; verificar con `./gradlew :nucleo:test`
- [x] 3.4 `EstadoGuardado.ultimaPotenciaErgW` opcional (150 W si falta) y `Configuracion` versión 3 con la migración que añade las reglas nuevas solo en huecos libres a la lineal y a cada desarrollo guardado (decisión 5). Tests en `ConfiguracionTest`: «Configuración anterior al ERG manual» (versión 2 y versión 1), «Entrada ya ocupada», «Regla nueva quitada por el usuario» (versión 3 sin la regla no la recupera) y ida y vuelta de `ultimaPotenciaErgW`; verificar con `./gradlew :nucleo:test`

### 4. Sesión (`:app`)

- [x] 4.1 `Sesion`: `AlternarErgManual` con la potencia recordada, `gestoErg` generalizado a los dos ERG, `ErgMas`/`ErgMenos` sin efecto en SIM, eventos `ModoErg` y `ErgZwiftActivo` con su vibración, aviso de □ con la potencia de entrada, guardar `ultimaPotenciaErgW` (también al terminar por un entrenamiento de Zwift) y deshacer el ajuste al apagar el Square (decisión 4). Tests en `SesionTest`: entrar y salir con △ larga, palancas y «+10 W»/«−10 W» asignados en ERG manual, tope con la repetición parada, □ en ERG manual, △ larga en ERG de Zwift, potencia recordada entre sesiones y Square perdido en ERG manual; verificar con `./gradlew :app:testDebugUnitTest`

### 5. Pantallas (`:app`)

- [x] 5.1 `ErgUi` con origen, `ErgTarget` con `deZwift` y `ajuste` opcionales, `ModeBadge` con «MANUAL» y la explicación de ERG manual, avisos nuevos (`AvisoUi` y textos: «ERG manual · 200 W», «Modo SIM», «Vuelta a 200 W», «Entrenamiento de Zwift») en horizontal y en vertical, con la zona ERG centrada en su columna también para el ERG de Zwift y los datos de `MetricGrid` centrados en su celda, en las dos orientaciones y en todos los modos, y la marcha centrada en su columna en horizontal (lineal y platos) (decisión 6). Tests en `RodajeUiStateTest` (ERG manual, vuelta a SIM, avisos) y en `RodajeTest` con los estados de la maqueta validada, incluida la vertical sin desplazamiento; verificar con `./gradlew :app:testDebugUnitTest connectedDebugAndroidTest`
- [x] 5.2 Notificación de la sesión con la potencia en ERG manual (`potenciaErg`); test en `TextoConexionesTest` o el que cubra el texto de la notificación («ERG manual»); verificar con `./gradlew :app:testDebugUnitTest`
- [x] 5.3 Diagnóstico: `ModoUi.ErgManual` en Simulación con «Potencia fijada» y «Entrada», y el texto del último gesto en ERG manual; tests en `DiagnosticoUiStateTest` («ERG manual» y «ERG manual sobre una ruta de Zwift»); verificar con `./gradlew :app:testDebugUnitTest`
- [x] 5.4 Botones y Asignar acción con los nombres de las acciones nuevas («+10 W», «−10 W», «Alternar SIM ⇄ ERG»); tests en `BotonesUiStateTest` y `BotonesTest` («Acciones de ERG», «ERG por defecto»); verificar con `./gradlew :app:testDebugUnitTest connectedDebugAndroidTest`

### 6. Pruebas con el equipo real

- [x] 6.1 Desplegar en el OnePlus 8 (skill `desplegar-movil`) y comparar capturas de Rodaje en ERG manual (horizontal y vertical), Diagnóstico y Botones con la maqueta validada (Rodaje en ERG manual validado por el usuario en el móvil el 26-10-2026)
- [x] 6.2 ERG manual a 200 W sin Zwift: 30 s a ~70 rpm y 30 s a ~95 rpm, con el registro de sesión; criterio de design.md decisión 7 (media de cada tramo sin los 5 primeros segundos dentro de ±5 %). Anotar el resultado en `design.md`
- [x] 6.3 Con el Square: △ larga entra y sale, palancas ±10 W y ±30 W con repetición, □ vuelve a la potencia de entrada, topes, y la marcha intacta al volver a SIM; cerrar y abrir la app y comprobar la potencia recordada y que una asignación personalizada de antes conserva lo suyo y recibe △ larga
- [ ] 6.4 En Zwift (si está a mano): ERG manual sobre una ruta y vuelta con el terreno vigente; un entrenamiento que interrumpe el ERG manual; △ larga durante el entrenamiento.
  - Pendiente al archivar, por decisión del usuario (no había Zwift a mano el 26-10-2026). Anotada en `docs/planificacion.md`.

## Documentación

- [x] 7.1 Actualizar la documentación:
  - `docs/funcional.md`: F7.2 (±10 W por marcha y con «+10 W»/«−10 W», ±30 W con los saltos en lugar de ±25 W con pulsación larga; potencia inicial recordada, 150 W la primera vez) y F7.3 (△ larga; convivencia con Zwift); anexo A.2 (△ larga ya funciona; «ERG ±10 W» con la cruceta descartado por el cambio de sentido de Zwift, las palancas ajustan la potencia); decisiones abiertas si alguna se cierra.
  - `docs/arquitectura.md`: § 4.3 (`AjusteErg` para los dos ERG), § 5.3 (`ErgMas`, `ErgMenos`, `AlternarErgManual` sin parámetro), § 5.4 (`ErgManual` con `debajo`, qué hacen las palancas, transiciones con Zwift y diagrama), § 10 (`configuracion.json` versión 3 con su migración y `ultimaPotenciaErgW` en `estado.json`).
  - `docs/planificacion.md`: fila 9 y sus notas con lo hecho y lo pendiente (resultado de la prueba de las dos cadencias); notas de la sección 3 del change que dejaba esa prueba a medias.
  - `docs/desarrollo.md`, si cambia cómo se prueba.
