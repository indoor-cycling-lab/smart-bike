# Tasks

## Maqueta

### 1. Sistema de diseño

- [x] 1.1 Revisar que `GearDisplay` (variante física), `ChainringIndicator` y `CassetteView` cubren lo que piden las specs: plato × piñón con tope atenuado, dos platos y cassette de 12 con el 36 a la izquierda. Comprobar también la opción con ayuda (`sq-opt`) para el modo de cambio y la fila enlace con valor para la entrada «Marchas» de Ajustes. Si falta una variante, añadirla a `components.css`, a su `README.md` y a `preview.html`. Actualizar los ejemplos de las pautas y de las vistas previas que usan 50/34 · 11–34 al desarrollo 52/36 · 11–36
- [x] 1.2 Regenerar con `python3 scripts/design-system/generar.py`; verificar que termina sin errores de contraste en los dos temas y que la guía muestra las variantes revisadas en los dos temas

### 2. Maquetas

- [x] 2.1 `maquetas/marchas.html` (spec delta de `screen-mockups`, «Marchas»):
  - Solo dos transmisiones: «Lineal» y «Shimano 105 2×12 · 52/36 · 11–36».
  - Con la Shimano: platos 36/52 y cassette 11–36 con la referencia 36×17 resaltada; modo de cambio «Automático», por defecto, y «Manual», con sus ayudas de la spec `settings-screen`.
  - «Al empezar»: referencia 36×17 o 12, y «Recordar la última marcha» con «Si no, empieza en la de referencia».
  - Estados `lineal`, `shimano` (automático), `shimano-manual` y `avanzada`; el botón de volver lleva a `ajustes.html#estado=desde-diagnostico`.
  - Verificar en los dos temas y a 360 dp sin desplazamiento horizontal.
- [x] 2.2 `maquetas/rodaje.html` (spec delta de `screen-mockups`, «Shimano 105 en la maqueta de rodaje»):
  - Estados nuevos `shimano-auto` (52×17, «+1 marcha»), `shimano-plato` (52×19, «Plato 52») y `shimano-tope-plato` (manual, «Tope · plato 52»), todos con potencia y cadencia.
  - Los estados `shimano-3` y `tope` pasan al desarrollo 52/36 · 11–36, con las alturas `--cog` según la fórmula de `CassetteView` y la barra superior «Shimano 105 · automático».
  - Verificar cada estado contra la spec en los dos temas.
- [x] 2.3 `maquetas/rodaje-vertical.html`: estados `shimano-auto` y `shimano-plato` con platos y cassette bajo la marcha; verificar a 360 dp sin desplazamiento y que nada se mueve entre `shimano-auto` y `shimano-plato`
- [x] 2.4 `maquetas/botones.html`:
  - Estado `shimano` con la línea «Asignación de Shimano 105. Cada transmisión tiene la suya.», la asignación por defecto de la Shimano (design.md, decisión 4) y «Plato más grande» y «Plato más pequeño» tras «−3 marchas».
  - El estado `lista` dice que es la asignación de la lineal.
  - Verificar en los dos temas.
- [x] 2.5 `maquetas/ajustes.html`, estado `desde-diagnostico`: entrada «Marchas» con el valor «Lineal · 24 marchas», que abre `marchas.html#estado=lineal`, encima de la sección Rodillo; verificar la ida y vuelta entre Diagnóstico, Ajustes y Marchas
- [x] 2.6 `maquetas/diagnostico.html`: estado `shimano` con Simulación «52×17 · relación 3,06», ajuste +5,1 %, enviada +5,1 % y «Último gesto» «Palanca D2 · pulsar · +1 marcha»; verificar en los dos temas
- [x] 2.7 Actualizar `maquetas/index.html` con los estados nuevos de Marchas, Rodaje (horizontal y vertical), Botones, Ajustes y Diagnóstico y sus funcionalidades (F5.7, F5.8, F12.3); verificar que cada enlace abre su estado

### 3. Validación

- [x] 3.1 Validación de la maqueta por el usuario:
  - Darle los enlaces exactos:
    - `maquetas/marchas.html#estado=lineal`, `#estado=shimano` y `#estado=shimano-manual`.
    - `maquetas/rodaje.html#estado=shimano-auto`, `#estado=shimano-plato`, `#estado=shimano-tope-plato` y `#estado=shimano-3`.
    - `maquetas/rodaje-vertical.html#estado=shimano-auto` y `#estado=shimano-plato`.
    - `maquetas/botones.html#estado=shimano` y `#estado=lista`.
    - `maquetas/ajustes.html#estado=desde-diagnostico`.
    - `maquetas/diagnostico.html#estado=shimano`.
    - Las secciones `GearDisplay`, `ChainringIndicator` y `CassetteView` de la guía.
  - Pedirle de paso que compruebe en su STI mecánico la correspondencia de las palancas (design.md, decisión 10).
  - Esperar su aprobación explícita. Si pide cambios, ajustar maqueta, `design.md` y specs, y volver a pedirla.

## App

### 4. Marcha y transmisión en el dominio (`:nucleo`)

- [x] 4.1 `EstadoMarcha` sellado con `Numerada` y `PlatoPinon` (con `relacion`), y `Tope` en lugar de `Extremo` (decisiones 2 y 3):
  - Adaptar `Cambio`, `ModeloSimple`, `ControlRodillo`, `AjusteErg` y sus tests a `Numerada`, sin cambiar el comportamiento de la lineal.
  - `ResultadoCambio.Cambiada` con `pasos`.
  - Verificar con `./gradlew :nucleo:test` que las trazas de los changes 4 y 7 (`RodajeTrazaTest`, `CombinacionesTrazaTest`) siguen igual.
- [x] 4.2 `Transmision.Platos`, `Sincronizado` y `Desarrollos.SHIMANO_105` (decisión 1); tests de que los dientes y los puntos son los de la spec; verificar con `./gradlew :nucleo:test`
- [x] 4.3 `ModoCambio` y `Cambio(transmision, modo)` con las reglas de la decisión 3, para manual y automático:
  - Tests con un caso por escenario de `virtual-gears`:
    - «Modo automático con la Shimano», con el recorrido completo de subida y de bajada (15 y 15).
    - «Plato a mano en modo automático», «Modo manual con la Shimano» y «Saltos y extremos con la Shimano».
    - «Topes con la Shimano» y «Cambiar de modo».
  - Un test de ±3 seguidos en la zona de histéresis.
  - Verificar con `./gradlew :nucleo:test`.
- [x] 4.4 `Accion.PlatoMas` y `Accion.PlatoMenos` con su `@SerialName`, `Accion.asignables(transmision)` y `Asignacion.POR_DEFECTO_PLATOS` (decisión 4):
  - Con la lineal, las acciones de plato no hacen nada, y en ERG `AjusteErg` las trata como `SinCambio`.
  - Tests de la asignación por defecto de la Shimano contra «Palancas con la Shimano» y «Saltos y extremos con la Shimano», de ida y vuelta en JSON, y de ERG («Shimano en ERG», «Palanca D1 con la Shimano en ERG», «Plato en ERG»).
  - Verificar con `./gradlew :nucleo:test`.
- [x] 4.5 `ModeloSimple` por relación con `MARCHAS_EQUIVALENTES = 24`, y `ControlRodillo.cambiarModelo` (decisión 5):
  - Tests con la tabla de la decisión 5, con dos decimales, y con los escenarios de «Pendiente según la relación con la Shimano», con «Bajadas» incluido.
  - Test de que la lineal da lo mismo que antes.
  - Verificar con `./gradlew :nucleo:test`.

### 5. Configuración (`:nucleo` y `:app`)

- [x] 5.1 `Configuracion` versión 2 con `transmision`, `lineal`, `platos` (con `AjustesPlatos`), sin `marchaInicial`, y la migración desde la versión 1 (decisión 6):
  - Documento inválido → valores por defecto.
  - Transmisión desconocida → lineal.
  - Referencia que no pertenece al desarrollo → la de por defecto.
  - Tests en `ConfiguracionTest`: valores por defecto, ida y vuelta, y migración de un documento de la versión 1 con asignación modificada, palancas invertidas y referencia en la 15 (escenario «Configuración anterior a la Shimano»).
  - Verificar con `./gradlew :nucleo:test`.
- [x] 5.2 `EstadoGuardado` con `ultimasMarchas` y la marcha inicial por transmisión (la última si se recuerda y es válida, o la referencia); tests de «Recordar la última marcha entre sesiones» (con la Shimano y sin recordar) y de la lectura de un `estado.json` sin el campo nuevo; verificar con `./gradlew :nucleo:test :app:testDebugUnitTest`

### 6. Sesión (`:app`)

- [x] 6.1 `Sesion` con la transmisión, el modo y la asignación de la configuración vigente, y cambios en caliente (decisión 7):
  - Al cambiar de transmisión: guardar la marcha de la saliente, pasar a la de la entrante y enviar el comando si cambia, sin aviso ni vibración.
  - Al cambiar de modo, sin cambiar la marcha. Al cambiar la referencia, solo la marca.
  - `fijarReferencia` y la última marcha en el apartado de la transmisión elegida. Registro con la marcha `36×17`.
  - Tests en `SesionTest`:
    - «Cambiar de transmisión» (ida y vuelta, rodando, sin Neo).
    - «Una asignación por transmisión».
    - «Referencia de la Shimano».
    - El camino caliente de la palanca D2 con la Shimano, que entrega el comando en el mismo paso que su notificación (NF1).
  - Verificar con `./gradlew :app:testDebugUnitTest`.

### 7. Pantallas (`:app`)

- [x] 7.1 Composables `ChainringIndicator` y `CassetteView` en `ui.componentes`, y la variante física de `GearDisplay` (plato × piñón, × en `ink-muted`, tope atenuado, animación según la relación), como en la guía; verificar con `./gradlew :app:assembleDebug` y añadirlos a `ComponentesTest`
- [x] 7.2 `TextosAccion`:
  - «Plato más grande» y «Plato más pequeño» en Botones.
  - Avisos «Plato N», «Referencia en 36×17», «Tope · plato N» y «Tope · piñón N».
  - Transmisión y modo («Lineal · 24 marchas», «Shimano 105 · automático», «Shimano 105 · manual»).
  - Tests de los textos; verificar con `./gradlew :app:testDebugUnitTest`.
- [x] 7.3 Rodaje con `MarchaUi` (decisión 9), en horizontal y en vertical, con la Shimano en plato × piñón, platos y cassette:
  - Ampliar `RodajeUiStateTest` con los escenarios de la spec delta de `ride-screen`: «Marcha 52×17», «Shimano en automático», «Tope de plato», «Plato que cambia solo», «Piñón sin cambiar de plato» y «Referencia con la Shimano».
  - Ampliar `RodajeTest` con la Shimano en vertical sin desplazamiento.
  - Verificar con `./gradlew :app:testDebugUnitTest connectedDebugAndroidTest`.
- [x] 7.4 Pantalla de Marchas (`ui/marchas`, decisión 8) y entrada «Marchas» en Ajustes, con la ruta `marchas` en `MainActivity`:
  - Contenido: transmisión, platos y cassette con la referencia, modo de cambio, referencia con `Stepper` (24 combinaciones ordenadas y extremos deshabilitados) y «Recordar la última marcha», todo guardado en `Almacen`.
  - Tests de `MarchasUiState` y `MarchasTest` con los estados `lineal`, `shimano` y `shimano-manual` de la maqueta.
  - Ampliar `AjustesTest` con «Entrada de Marchas» e «Ida y vuelta».
  - Verificar con `./gradlew :app:testDebugUnitTest connectedDebugAndroidTest`.
- [x] 7.5 Botones y Asignar acción con la asignación de la transmisión elegida:
  - La línea de la transmisión, las acciones de `Accion.asignables`, y Restaurar y Guardar en su apartado.
  - Ampliar `BotonesUiStateTest`, `BotonesTest` y `AsignarAccionTest` con «Acciones de plato solo con platos», «Acciones de plato con la Shimano», «Shimano por defecto», «Transmisión de la asignación», «Restaurar solo la elegida» y «Reasignar sin afectar a la otra».
  - Verificar con `./gradlew :app:testDebugUnitTest connectedDebugAndroidTest`.
- [x] 7.6 Diagnóstico: Simulación con «Marcha 52×17 · relación 3,06» y el ajuste por relación, y «Último gesto» con «Plato 52»; ampliar `DiagnosticoUiStateTest` con «Shimano» y «Plato con la Shimano»; verificar con `./gradlew :app:testDebugUnitTest`
- [x] 7.7 Desplegar en el OnePlus 8 (skill `desplegar-movil`) y comparar capturas de Marchas, Rodaje con la Shimano (horizontal y vertical), Botones con la Shimano, Ajustes y Diagnóstico con los estados validados de la maqueta

### 8. Validación con el equipo real

- [x] 8.1 Con el Neo y el Square, la Shimano en automático:
  - Subir de 36×36 a 52×11 y bajar, contando 15 pasos en cada sentido y los dos cambios de plato (36×15 → 52×19 y 52×24 → 36×19).
  - Comprobar que cada marcha se nota y que el cambio de plato pesa como un piñón.
  - Comprobar la latencia de la palanca en Diagnóstico, del mismo orden que con la lineal (NF1).
  - Anotar en `design.md` lo que opine el usuario de los puntos de cambio y la compensación. Si los quiere distintos, actualizar `Sincronizado`, specs y diseño (`/openspec-update-change`).
- [x] 8.2 La Shimano en manual: plato con I1 e I2, topes de plato y de piñón, ±3 y extremos con los Campagnolo, y □ corta y larga
- [x] 8.3 Cambiar de transmisión y de modo rodando; cerrar y abrir la app y comprobar que se conservan la transmisión, el modo, la marcha y la referencia de cada transmisión, y que la asignación personalizada de la lineal sigue igual tras la migración
- [ ] 8.4 En Zwift (si está a mano): una ruta con la Shimano (las pendientes de Zwift más el ajuste por relación, visibles en Diagnóstico) y, si hay un entrenamiento, la palanca D2 subiendo 10 W y la I1 sin efecto

## Documentación

- [x] 9.1 Actualizar la documentación:
  - `docs/funcional.md`:
    - Decisiones 3 y 5 pasan a tomadas: pendiente proporcional a la relación normalizada, y Shimano 105 52/36 · 11–36 como único desarrollo de fábrica por ahora.
    - F5.5: la marcha inicial es la de referencia.
    - F5.7 y F5.8, con lo que se cumple y lo que queda para el change nuevo.
    - Anexo A, con la asignación de la Shimano.
  - `docs/arquitectura.md`:
    - § 4.3: `Transmision.Platos`, `Desarrollos`, `ModoCambio`, `Cambio` y `EstadoMarcha`.
    - § 5.3: `PlatoMas` y `PlatoMenos`.
    - § 5.5: modelo simple por relación, y `EstadoMarcha` con plato, piñón y relación.
    - § 10: `configuracion.json` versión 2 y `ultimasMarchas`.
  - `docs/planificacion.md`:
    - La fila 8 se reduce a F5.7 y F5.8 con la Shimano 105 y F12.3 en parte.
    - Fila nueva para SRAM, Campagnolo y «A medida» (con el modo «las dos palancas a la vez» de SRAM).
    - Lo que pasa a los changes 10 y 15 (número de marchas, dificultad, modelo y «Pendiente por marcha» en Marchas).
  - `docs/desarrollo.md`, si cambia cómo se prueba.
