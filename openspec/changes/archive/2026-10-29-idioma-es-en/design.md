# Design

## Context

Motivación y alcance en `proposal.md`; comportamiento en `specs/app-language/spec.md`. Estado actual relevante:

- Los textos de la UI ya salen de `strings.xml` (607 cadenas y 2 plurales) con `stringResource`/`pluralStringResource`; el único `getString` fuera de Compose es `RideService` (canal y notificación). No hay textos de UI escritos a mano en los composables.
- Hay formatos fijados a español en el código: `Locale.forLanguageTag("es")` en `Tabla.kt`, `RodajeUiState.kt` y `Diagnostico.kt`; la lista `MESES` y el `.replace('.', ',')` en `TextosSesiones.kt`.
- El tema y la orientación ya tienen el patrón que necesita el idioma: un campo de `Configuracion` (opcional, con valor por defecto), un `StateFlow<…?>` en `AppGraph` que es `null` hasta la primera lectura de `configuracion.json`, y `MainActivity` que no pinta ninguna pantalla hasta conocerlo (la ventana es la de `color_ventana.xml` y el splash sigue). Se aplica al momento sin recrear la sesión.
- `MainActivity` es `ComponentActivity` (sin appcompat), `minSdk` 28. Existe `Context.actividad()` (en `ui/tema/OrientacionElegida.kt`) que desenvuelve `ContextWrapper` hasta la actividad.
- `Configuracion.VERSION` es 11; los campos nuevos son opcionales y las versiones anteriores se leen sin migración explícita (`tema`, `orientacion`).

## Goals / Non-Goals

**Goals:**
- Que la primera pantalla ya salga en el idioma correcto y que cambiarlo en Ajustes se vea al momento, sin tocar la sesión ni las conexiones.
- Que el idioma pase por el mismo camino que el tema: una sola fuente (`Configuracion`), sin segunda copia que pueda divergir.

**Non-Goals:**
- Seguir el idioma del móvil después del primer arranque, o aparecer en «Idiomas de la app» de Android.
- Traducir lo técnico (registro de Diagnóstico, trazas, docs, nombres de archivo).
- Un tercer idioma: la estructura (`IdiomaApp` y `values-<código>`) lo permitiría, pero la detección «español o inglés» y el selector de dos opciones no se generalizan en este change.

## Decisions

1. **`IdiomaApp` en `:core` y `Configuracion.idioma: IdiomaApp?`.** `ES`/`EN`; `null` significa «aún sin decidir». Es opcional y sin valor serializado cuando es `null`, como `tema`; `VERSION` pasa a 12. `:core` lleva también la regla de detección, pura y con test: `IdiomaApp.deLenguaje(codigo: String)` devuelve `ES` si el código es `es` y `EN` en cualquier otro caso. `:app` solo le da `Locale.getDefault().language`. Alternativa descartada: guardar `"es"`/`"en"` como texto libre, que permite valores que no se saben pintar.

2. **La detección ocurre al leer la configuración por primera vez, y se guarda.** En `AppGraph`, la primera lectura con `idioma == null` calcula el idioma del sistema, lo escribe en `Almacen` y emite ese valor. `AppGraph.idioma` es `StateFlow<IdiomaApp?>`: `null` hasta esa lectura, igual que `tema`. Así «sin parpadeo» (spec) sale del mecanismo que ya existe: `MainActivity` no pinta nada hasta tener el idioma. Una actualización desde una versión sin idioma pasa por el mismo camino. Alternativa descartada: no guardar nada y resolver «es → es, otro → en» en cada arranque; sería seguir al móvil, que el usuario descartó.

3. **Aplicarlo con un contexto localizado en la composición, no con `AppCompatDelegate` ni recreando la actividad.** `MainActivity` envuelve el contenido en un `CompositionLocalProvider` de `LocalContext` y `LocalConfiguration` con un `ContextWrapper` de la actividad cuyo `getResources()` sale de `createConfigurationContext` con el `Locale` elegido. `stringResource`, `pluralStringResource` y las `stringResource` de todas las pantallas leen `LocalContext.current.resources`, así que cambian al instante, sin recrear nada. Como es un `ContextWrapper`, `Context.actividad()` y los lanzadores de resultados siguen encontrando la actividad.
   - Alternativa descartada, `AppCompatDelegate.setApplicationLocales`: añade `androidx.appcompat`, por debajo de Android 13 exige `AppCompatActivity` (cambia el tema base y la ventana), y deja el estado en el sistema (su «Idiomas de la app» podría contradecir al ajuste, y «sin elegir» no tiene sitio ahí). Justificación de no añadir la dependencia: la composición ya está desacoplada de la actividad por el patrón del tema.
   - Alternativa descartada, `attachBaseContext` + `recreate()`: parpadea, repite `onCreate`, reaplica ventana y orientación, y no se parece a cómo se cambian el tema y la orientación.
   - Límite conocido: lo que no pasa por la composición (el `Toast` del sistema, los diálogos del selector de archivos y de permisos) usa el idioma del móvil. La app no usa `Toast` y esos diálogos son del sistema (fuera de alcance).

4. **`RideService` lee el idioma de `AppGraph.idioma`.** Un helper `Context.conIdioma(IdiomaApp)` (el mismo que usa la actividad) da el contexto con el que se hacen los `getString`. El servicio recompone la notificación al cambiar el idioma (se suma `idioma` a la clave del `distinctUntilChanged` de `observar()`) y vuelve a crear el canal, que con el mismo id actualiza su nombre y descripción. Mientras el idioma es `null` (sin configuración leída), el servicio usa el idioma del sistema, que es el que la detección resolverá en la mayoría de los casos; el siguiente cambio de clave corrige el texto.

5. **Formatos con el `Locale` del idioma, pasado como parámetro.** Se quita el `Locale.forLanguageTag("es")` fijo: las funciones de formato de `RodajeUiState`, `Tabla`, `Diagnostico` y `TextosSesiones` reciben un `Locale` (`IdiomaApp.locale`). Los `UiState` ya se construyen en el `ViewModel` a partir del estado de la app, así que el idioma entra como un dato más de su entrada, y la composición no lee `LocalConfiguration` para formatear. `MESES` sale de `DateTimeFormatter.ofPattern("d MMM yyyy · HH:mm", locale)` con las abreviaturas del locale; hay que comprobar que en inglés da «Oct» y en español «oct» (el JDK de Android da «oct.» en algunos dispositivos; si es así se normaliza quitando el punto, con test). El signo menos «−» se mantiene en los dos idiomas.

6. **Textos en `values-en/strings.xml`, con el español como `values/` por defecto.** Cada cadena tiene su entrada en los dos archivos; un test unitario compara las claves (y los marcadores `%1$s`/`%d` de cada una) de `values/` y `values-en/` para que falte ninguna ni cambie su número de argumentos. Los plurales de inglés usan `one`/`other`. Glosario fijado antes de traducir: marcha → gear, plato → chainring, piñón → cog, transmisión → drivetrain, rodaje → ride (la pantalla «Rodaje» se llama «Ride»), rodillo → trainer, controlador → controller, mando → remote, cuadro → frame, bajadas → descents, Conexión → Connection, Ajustes → Settings, ERG/FTMS/SIM sin traducir. Los nombres de producto (Zwift, Neo, Square, Shimano, SRAM, Campagnolo) no se traducen.

7. **El idioma entra en la copia de seguridad como cualquier otro campo; el idioma de un archivo sin él es el actual.** La exportación lo escribe. Al importar, si el archivo no trae `idioma` (versión anterior), se conserva el de la app; si lo trae, se aplica al momento por el mismo `StateFlow`. Es la única excepción a «todos los ajustes pasan a ser los del archivo», y la spec `app-language` la dice. Alternativa descartada: tratar el archivo sin idioma como un primer arranque (detectar de nuevo), que cambiaría el idioma de una persona al restaurar una copia vieja sin que lo pida.

8. **Ajuste «Idioma · Language» con el grupo de opciones de «Tema».** Sin componente ni token nuevos. El título lleva siempre los dos nombres para que se pueda encontrar con la app en un idioma que no se lee; las opciones son «Español» y «English» en su idioma (cadenas que no varían entre `values/` y `values-en/`, marcadas `translatable="false"`). Va encima de «Tema» porque es el ajuste que más urge encontrar. Alternativa descartada: ponerlo en una sección propia «Idioma», que alarga Ajustes por una sola fila.

9. **Tests de instrumentación que buscan textos.** Los tests de instrumentación actuales buscan cadenas españolas; seguirán pasando porque el idioma por defecto del móvil de pruebas ya es el español, pero el primer arranque guarda el idioma: los tests que parten de «instalación limpia» fijan el idioma de forma explícita (en la configuración inicial del test) para no depender del móvil.

## Risks / Trade-offs

- **[Textos más largos en inglés cortan algo o tapan un objetivo táctil]** → Mitigación: el inglés suele ser más corto que el español, pero hay etiquetas de Rodaje a 360 dp con el estado de la publicación en dos líneas; la spec exige comprobarlo a 360 dp y 130 % de fuente, y la tarea de la app incluye una pasada por todas las pantallas en inglés con capturas en el OnePlus 8.
- **[Una traducción mala de un término del dominio confunde]** → Mitigación: glosario en la decisión 6, el usuario revisa `values-en/strings.xml` entero antes de archivar.
- **[El `Locale` por parámetro toca muchas firmas]** → Mitigación: se hace en una sola tarea, con los tests de cada formato (que hoy ya fijan `es`) duplicados en `en`.
- **[`createConfigurationContext` en cada cambio es más caro que un string]** → Mitigación: se calcula una vez por idioma con `remember(idioma)`.
- **[Cadenas añadidas después sin traducción]** → Mitigación: el test de claves de la decisión 6 falla; la regla va a `AGENTS.md`.
- **[`TalkBack` lee con la voz del idioma del móvil, no de la app]** → Aceptado: con un contexto localizado los textos cambian, pero el motor de voz usa el idioma del sistema. Es un límite de no seguir al móvil, y se anota en la guía de accesibilidad.

## Migration Plan

- Sin migración de datos: `idioma` es opcional y los documentos anteriores se leen sin él; el primer arranque tras actualizar detecta y guarda (decisión 2).
- Sin cambio de permisos.
- Reversión: quitar el campo no rompe la lectura (los documentos con `idioma` se ignoran en una versión anterior, ya que el decodificador ignora claves desconocidas — a comprobar con el test de compatibilidad de `Configuracion`).

## Validación con el equipo real

No depende de nada BLE «a validar». En el OnePlus 8: instalar de cero con el móvil en español y en inglés (primer arranque); cambiar el idioma rodando con el Neo y el Square conectados y comprobar que no se pierde la conexión ni la marcha; ver la notificación en los dos idiomas; y pasar por todas las pantallas en inglés a 360 dp.
