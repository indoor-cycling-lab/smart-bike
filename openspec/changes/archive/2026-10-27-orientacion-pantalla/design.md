## Context

- **Orientación hoy.** `MainActivity` no declara `android:screenOrientation` ni `configChanges`: la app gira con el móvil y respeta el bloqueo de rotación del sistema. Girar recrea la actividad; la `Sesion` (en `AppGraph`, que vive en la `Application`) y los `ViewModel` sobreviven. `Rodaje` elige la disposición por el espacio disponible (`BoxWithConstraints`), no por `LocalConfiguration` (arquitectura § 9.2), así que cualquier forma de fijar la orientación le sirve sin tocarla.
- **El tema, como precedente.** `Configuracion.tema: TemaApp` es un campo opcional con valor por defecto, sin subir la versión. `AppGraph.tema: StateFlow<TemaApp?>` es `null` hasta la primera lectura de `configuracion.json`; `MainActivity` lo aplica a la ventana en `onCreate` si ya lo conoce y, si no, en cuanto llega, y no compone las pantallas mientras sea `null` (`SqTemaElegido`). Ajustes lo cambia con `AjustesViewModel.tema(t)` y lo muestra en la sección «Pantalla» con `SettingsOpcion` (`sq-opt`) en un `selectableGroup`.
- **Configuración.** `Configuracion` está en la versión 5. Se lee con `ignoreUnknownKeys` y sin `coerceInputValues`.
- **Change 10f en curso** (`barra-conexiones-iconos`): modifica otros requisitos de `ride-screen` («Conexiones en la barra superior», «Disposición en vertical»…), no «Rodaje como pantalla inicial», y no toca Ajustes. Los dos changes pueden archivarse en cualquier orden.

## Goals / Non-Goals

**Goals:**
- Fijar la app en vertical o en horizontal desde Ajustes, sin depender del bloqueo de rotación del sistema.
- Aplicarlo al momento y al volver a abrir la app, sin terminar la sesión, cambiar la marcha ni cortar conexiones.
- Que, con una orientación fija, Rodaje no cambie de disposición aunque se mueva el móvil en el soporte.

**Non-Goals:**
- Disposiciones nuevas para ninguna pantalla.
- Una orientación por pantalla.
- Elegir el sentido del giro dentro de una orientación.

## Decisions

**1. El ajuste en la configuración: `orientacion: OrientacionApp = OrientacionApp.SEGUN_MOVIL`.** `OrientacionApp { SEGUN_MOVIL, VERTICAL, HORIZONTAL }` va en `:nucleo` (`dominio/OrientacionApp.kt`), con `@SerialName("movil")`, `@SerialName("vertical")` y `@SerialName("horizontal")`, como `TemaApp`.
- Campo opcional con valor por defecto, como `tema` y `bajadas`: un documento sin él se lee con `SEGUN_MOVIL`, sin subir `VERSION` (sigue en 5) ni migrar.
- Queda en el mismo documento que los demás ajustes, así que lo exportará el change 10b sin trabajo extra.
- Alternativa descartada: `SharedPreferences` aparte para leerlo de forma síncrona en `onCreate`. Duplica dónde viven los ajustes, como se descartó para el tema.

**2. Traducción a Android: `UNSPECIFIED`, `SENSOR_PORTRAIT` y `SENSOR_LANDSCAPE`.** `:app` traduce `OrientacionApp` a `ActivityInfo.screenOrientation` con una función pura (`OrientacionApp.comoPantalla()`, junto a `TemaApp.comoTema()`):
- `SEGUN_MOVIL` → `SCREEN_ORIENTATION_UNSPECIFIED`: exactamente lo de hoy, que respeta el bloqueo de rotación.
- `VERTICAL` → `SCREEN_ORIENTATION_SENSOR_PORTRAIT` y `HORIZONTAL` → `SCREEN_ORIENTATION_SENSOR_LANDSCAPE`: la orientación queda fija aunque el bloqueo del sistema esté en la contraria, y dentro de ella la pantalla se ve derecha en el sentido en que esté el móvil (spec, «Horizontal en el otro sentido»). En el soporte, el móvil puede ir con el puerto de carga a uno u otro lado.
- Alternativa descartada: `PORTRAIT` y `LANDSCAPE` a secas. Con el móvil montado al revés, Rodaje se vería boca abajo.
- Alternativa descartada: `USER_PORTRAIT` y `USER_LANDSCAPE`. Con el bloqueo de rotación activado no siguen el sentido del móvil, y el usuario fija la orientación justo para no depender de ese bloqueo.
- A validar en el OnePlus 8: si admite vertical boca abajo con `SENSOR_PORTRAIT` (muchos móviles no). No cambia nada del ajuste: solo si el giro de 180° en vertical se ve o no.

**3. Aplicación en la actividad, desde `AppGraph`.** `AppGraph` expone `orientacion: StateFlow<OrientacionApp?>`, de `almacen.configuracion.map { it.orientacion }` con `stateIn(ambitoApp, Eagerly, null)`, igual que `tema`. `MainActivity`:
- En `onCreate`, antes de `super.onCreate`, fija `requestedOrientation` si la orientación ya se conoce. Así, al recrearse la actividad (por el giro que provoca el propio ajuste o por cualquier otro motivo), sale ya con la orientación buena.
- En `setContent`, un `LaunchedEffect(orientacion)` fija `requestedOrientation` en cada cambio, junto al efecto del tema. Si la orientación pedida no es la actual, Android gira y recrea la actividad, como al girar el móvil hoy: la sesión, la marcha, la navegación (con el estado guardado de `AppNavegacion`) y las conexiones siguen.
- Alternativa descartada: `android:screenOrientation` en el manifiesto. Es fijo en la instalación, no se puede elegir en Ajustes.
- Alternativa descartada: declarar `configChanges="orientation|screenSize"` para no recrear la actividad. Cambia cómo se comporta toda la app al girar, también con «Según el móvil», y no hace falta: ya se recrea bien.

**4. Arranque en frío con una orientación fija.** Hasta la primera lectura de la configuración (milisegundos), la actividad no tiene orientación pedida y no compone ninguna pantalla (el tema tampoco se conoce). Con la lectura llegan el tema y la orientación a la vez: la primera composición fija la orientación y, si el móvil está en la otra, Android gira y recrea la actividad.
- Puede verse un instante Rodaje en la orientación del móvil antes del giro. Se comprueba en el OnePlus 8 (tarea 2.5). Si se ve, `SqTemaElegido` pasa a esperar también a la orientación y la actividad fija `requestedOrientation` desde un colector en `lifecycleScope` antes de componer; no se hace de entrada porque añade un estado más a la raíz por un efecto que puede no notarse.
- No se condiciona la composición a que la orientación actual coincida con la pedida: en pantalla dividida o en ventanas libres Android puede ignorar la petición, y la app no se pintaría nunca.

**5. «Orientación» en la sección «Pantalla», debajo de «Tema».** Con el mismo patrón que «Tema»: `Label` «Orientación» y `SettingsList` de tres `SettingsOpcion` en un `selectableGroup`, con su ayuda:
- «Según el móvil»: «Gira con el móvil y respeta el bloqueo de rotación».
- «Vertical»: «Siempre en vertical, aunque gires el móvil».
- «Horizontal»: «Siempre en horizontal, aunque gires el móvil».

`AjustesUiState` añade `orientacion` y `AjustesViewModel` añade `orientacion(o)` con `cambiar { it.copy(orientacion = o) }`. Va en «Pantalla» porque es de cómo se ve la app, como el tema. Alternativa descartada: un `Segmentado` de tres. Sin ayuda no queda claro que «Según el móvil» respeta el bloqueo del sistema, y `sq-opt` ya distingue la elegida con la marca de radio.

**6. Maqueta.** `maquetas/ajustes.html` añade el `fieldset` «Orientación» debajo del de «Tema», con el mismo marcado `sq-opt`. Los estados pasan a ser:
- `inicio`: Oscuro y «Según el móvil».
- `claro`: Claro y «Según el móvil».
- `vertical`: Oscuro y «Vertical».
- `horizontal`: Oscuro y «Horizontal».

El `fieldset` de «Tema» con «Oscuro» elegido se marca con `data-mock-state="inicio vertical horizontal"`. La maqueta sigue siendo estática y en el marco vertical: no se dibuja Ajustes en horizontal, que no cambia de disposición. Sin componentes ni tokens nuevos, la guía no cambia.

## Risks / Trade-offs

- [Android 16 y pantallas grandes pueden ignorar la orientación pedida por la app.] → En el OnePlus 8 (Android 13) se respeta. En otros dispositivos, la app seguiría girando con el móvil; no se rompe nada. Se anota en `arquitectura.md`.
- [Un instante en la orientación del móvil al abrir la app con una orientación fija.] → Se mide en el OnePlus 8 y, si se ve, se aplica la alternativa de la decisión 4.
- [Cambiar el ajuste recrea la actividad si gira la pantalla.] → Es lo mismo que al girar el móvil hoy, cubierto por los tests de giro de Rodaje. Se añade un test de navegación que cambia la orientación en Ajustes y comprueba que la sesión y la marcha siguen.
- [Un valor de `orientacion` desconocido, de una versión futura, haría ilegible todo `configuracion.json`.] → Igual que con `tema`; se trata con exportar e importar (change 10b).
- [Con «Horizontal» fijo, Ajustes y las demás pantallas con lista se ven en horizontal, con menos filas a la vista.] → Ya pasa hoy al girar el móvil; todas se desplazan y tienen la barra inferior visible.
