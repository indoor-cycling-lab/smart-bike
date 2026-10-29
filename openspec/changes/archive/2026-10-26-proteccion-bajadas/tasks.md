# Tasks

Se empieza cuando el change 7 (`gestos-y-asignacion`) esté archivado, y se parte de su código y de sus maquetas (design.md, Context).

## Maqueta

### 1. Maquetas

- [x] 1.1 `maquetas/ajustes.html`: grupo «Bajadas» en la sección Rodillo, con tres `sq-opt` («Sin protección», «La marcha no crea bajadas» elegida y «Nunca empujar») y las ayudas de design.md decisión 6, y estado `desde-diagnostico` con el botón de volver a Diagnóstico, solo la sección Rodillo con «Bajadas» y sin barra inferior; verificar contra la spec delta de `screen-mockups` y la de `settings-screen` en los dos temas y a 360 dp sin desplazamiento horizontal
- [x] 1.2 `maquetas/diagnostico.html`: enlace «Ajustes» junto a «Asignación de botones», que abre `ajustes.html#estado=desde-diagnostico`; métrica «Bajadas» en Simulación en todos los estados con el Neo conectado; estado nuevo `bajada-limitada` (Zwift −7,0 %, marcha 1, ajuste −11,0 %, enviada al Neo −7,0 % y la fila «Limitada por Bajadas: sin límite sería −18,0 %»); verificar contra la spec delta de `screen-mockups` y la de `diagnostics` en los dos temas
- [x] 1.3 Actualizar `maquetas/index.html` con los estados nuevos de Ajustes y Diagnóstico y sus funcionalidades (F6.5, F12.5); verificar que cada enlace abre su estado

### 2. Validación

- [x] 2.1 Validación de la maqueta por el usuario: darle los enlaces exactos (`maquetas/ajustes.html#estado=inicio`, `maquetas/ajustes.html#estado=desde-diagnostico`, `maquetas/diagnostico.html#estado=conectado` y `maquetas/diagnostico.html#estado=bajada-limitada`) y la sección `SettingsList` de la guía, y esperar su aprobación explícita; si pide cambios (textos de las ayudas incluidos), ajustar maqueta, `design.md` y specs y volver a pedirla

## App

### 3. Dominio (`:nucleo`)

- [x] 3.1 `ModoBajadas` serializable con `suelo(pendienteTerrenoPct)` (design.md decisión 1); `ModoBajadasTest` con la tabla de la spec delta de `trainer-resistance` (terreno 0 %, +4 %, −7 %, con los tres modos); verificar con `./gradlew :nucleo:test`
- [x] 3.2 `ControlRodillo` con el modo en el constructor, el suelo aplicado en `comandoDe` solo a `Pendiente` de `SimLibre` y `SimZwift`, `cambiarBajadas(modo, marcha)` y `sinLimite(marcha)` (decisiones 2–4). Tests:
  - en `ResistenciaTest` o en un `ControlRodilloBajadasTest` nuevo: escenarios «Extremos», «Extremos sin protección», «Marchas que dan la misma pendiente» (sin envío), «Control manual» (sin límite), ERG sin límite y «Cambiar de modo rodando» (devuelve la pendiente nueva, o `null` sin Neo o si no cambia);
  - en `ControlRodilloZwiftTest`: «Bajada», «Bajada sin protección», «Bajada sin empuje» y una bajada larga con «Nunca empujar» que no reenvía nada.
  
  Adaptar las expectativas actuales que daban pendientes negativas por la marcha, por ejemplo en `RodajeTrazaTest`. Verificar con `./gradlew :nucleo:test`
- [x] 3.3 `Configuracion` con `bajadas` por defecto en `LA_MARCHA_NO_CREA_BAJADAS`, sin cambiar `VERSION` (decisión 5); en `ConfiguracionTest`: valor por defecto, ida y vuelta con cada modo, y documento anterior sin el campo que conserva los demás ajustes; verificar con `./gradlew :nucleo:test`

### 4. Sesión (`:app`)

- [x] 4.1 `Sesion` crea `ControlRodillo` con el modo guardado y llama a `cambiarBajadas` en el hilo de control cuando cambia `bajadas` en la configuración (decisión 3); el estado de la sesión expone el modo y la pendiente sin límite para Diagnóstico. Tests en `SesionTest`: cambio de modo rodando (envía la pendiente nueva y la marcha no cambia), cambio sin Neo y primer envío al conectar con el modo guardado; verificar con `./gradlew :app:testDebugUnitTest`

### 5. Pantallas (`:app`)

- [x] 5.1 Composable `SettingsOpcion` en `SettingsList.kt`, como `sq-opt` de la guía: radio con nombre y ayuda, anillo `gear` en la elegida y la fila entera como objetivo táctil de `touch-min` (decisión 6); verificar con `./gradlew :app:assembleDebug` y comparándolo con la sección `SettingsList` de la guía
- [x] 5.2 `AjustesViewModel` y `Ajustes`: `AppBar` con volver, sección Rodillo con el grupo «Bajadas», elección guardada en `Almacen`. Tests de `AjustesUiState` (valor por defecto y elegir otra opción) y `AjustesTest` con los estados `inicio` y `desde-diagnostico` de la maqueta, incluido «Objetivo táctil». Verificar con `./gradlew :app:testDebugUnitTest connectedDebugAndroidTest`
- [x] 5.3 Navegación: ruta `ajustes` en `MainActivity`, enlace «Ajustes» en Diagnóstico y Atrás de vuelta a Diagnóstico; verificado con los tests de `DiagnosticoTest` (enlace) y `AjustesTest` (volver) en `connectedDebugAndroidTest`, y en el OnePlus 8 la ida y vuelta y el Atrás del sistema de `settings-screen` (no hay tests de `MainActivity` en el proyecto)
- [x] 5.4 Simulación de Diagnóstico con la métrica «Bajadas» y la fila «Limitada por Bajadas: sin límite sería …» cuando la diferencia supera 0,05 % (decisiones 4 y 7); ampliar `DiagnosticoUiStateTest` con «Marcha dura», «Pendiente limitada», «Límite que deja de actuar» y «Neo sin conexión» de la spec delta de `diagnostics`; verificar con `./gradlew :app:testDebugUnitTest`
- [x] 5.5 Desplegar en el OnePlus 8 (skill `desplegar-movil`) y comparar capturas de Ajustes y de Diagnóstico con `maquetas/ajustes.html#estado=desde-diagnostico` y `maquetas/diagnostico.html#estado=conectado`

### 6. Validación con el equipo real

- [x] 6.1 Prueba del empuje del motor (design.md decisión 8): desde el control manual, de −0,5 % a −5 % en pasos de 0,5 %, pedalear y dejar de pedalear, y anotar en `design.md` a partir de qué pendiente el volante se mantiene o acelera solo (el usuario pedalea y escucha; el agente envía y lee el registro)
- [x] 6.2 Sin Zwift: en la marcha 1 con el modo por defecto, no hay empuje y Diagnóstico enseña el límite; cambiar a «Sin protección» con la sesión en marcha y comprobar que vuelve el empuje sin cambiar la marcha; cerrar y abrir la app y comprobar que el modo se conserva
- [ ] 6.3 Con Zwift (si está a mano): en una bajada, con «La marcha no crea bajadas», bajar a la 1 no aumenta el empuje; con «Nunca empujar», el motor no empuja y las marchas duras siguen endureciendo

## Documentación

- [x] 7.1 Actualizar la documentación:
  - `docs/funcional.md`: F6.5 cumplida con los tres modos y el modo por defecto; F12.5 en parte; decisión abierta 8, con las marchas ligeras en llano que ya no dan pendiente negativa.
  - `docs/arquitectura.md`: § 4.3 y § 5.5, `ModoBajadas` en `ControlRodillo`; § 9.2, Ajustes desde Diagnóstico; § 10, `bajadas` en `configuracion.json`; el resultado de la prueba 6.1.
  - `docs/planificacion.md`: fila y notas del change; F6.5 fuera del change 15; el suelo configurable, si la prueba lo justifica; lo que pasa al change 10 (Ajustes en la barra inferior con el resto de secciones).
  - `docs/desarrollo.md`: revisar; previsiblemente sin cambios.
