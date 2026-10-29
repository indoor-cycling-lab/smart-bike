# Design

## Context

- `design-system/tokens.json` tiene 18 tokens de color (17 con valor por tema y `focus`, que remite a `gear`), tres familias como pilas de CSS (`numeric`, `sans`, `mono`), 11 estilos tipográficos en píxeles, y espaciados, radios y tamaños en píxeles. `generar.py` emite `tokens.css` y la guía, y al final llama a `contraste.py`.
- Las fuentes están solo en WOFF2 (Barlow 400 y 600, Barlow Condensed 500, 600 y 700, JetBrains Mono 400), que Android no lee en `res/font`.
- No existe proyecto Android. En el Mac hay JDK 21, Android Studio y el SDK con `platforms/android-35` y `android-36`; no hay Gradle instalado aparte. El móvil de pruebas tiene Android 13 (SDK 33).
- `arquitectura.md` § 3 y § 9.1 fijan Compose, tema propio `SqTema` con `CompositionLocal`, Material 3 solo como base técnica, `Tokens.kt` versionado y un test JVM que lo contrasta con `tokens.json`.

## Goals / Non-Goals

**Goals:**

- Un esqueleto de Gradle que el change 3 pueda ampliar sin reestructurarlo: módulos, catálogo de versiones, `applicationId` y tests JVM e instrumentados ya funcionando.
- Que añadir o cambiar un token solo requiera editar `tokens.json` y ejecutar `generar.py`.
- Paridad visual con la guía (mismos valores, mismas fuentes, mismo interlineado), comprobable en el móvil.

**Non-Goals:**

- Componentes `sq-*` en Compose e iconos (ver propuesta).
- Generar los derivados desde Gradle (Gradle no ejecuta Python; `arquitectura.md` § 9.1).
- Optimizar el tamaño del APK por las fuentes (unos 300 KB en TTF).

## Decisions

### 1. Esqueleto del proyecto

- `settings.gradle.kts` con `:nucleo` y `:app`; `gradle/libs.versions.toml` con todas las versiones; Gradle Wrapper versionado (`gradlew`, `gradle/wrapper/`). Como no hay Gradle instalado, el wrapper se genera una vez con la distribución descargada de gradle.org y no se vuelve a necesitar.
- `:nucleo`: Kotlin/JVM 21 con JUnit Jupiter (JUnit 6, la versión estable actual; la API es la de JUnit 5), sin código todavía. Se crea ahora para que `:app ──▶ :nucleo` exista desde el principio y el change 3 solo añada paquetes.
- `:app`: `namespace` y `applicationId` `jmlopezdona.squareshift`, `applicationIdSuffix ".debug"` en depuración, `minSdk 28`, `compileSdk 37` y `targetSdk 36`, Java/Kotlin 21. Compose 1.12 (BOM 2026.09.00) exige compilar contra la API 37; AGP descargó la plataforma al SDK del Mac la primera vez. `targetSdk` se queda en 36 para no adoptar aún el comportamiento de Android 17. Una sola Activity, `MainActivity`, que muestra en `SqTema` un arranque provisional (fondo `bg` y el nombre de la app en `title`) hasta que el change 3 la sustituya por la navegación y Diagnóstico.
- Sin permisos en el manifiesto todavía: llegan con BLE en el change 3 (`arquitectura.md` § 12).
- Versiones: las últimas estables de AGP, Kotlin (con el plugin `org.jetbrains.kotlin.plugin.compose`), Compose BOM y `activity-compose` compatibles entre sí y con JDK 21 en el momento de implementar, fijadas en el catálogo y anotadas en `desarrollo.md`.

**Dependencias nuevas.** Todas están en `arquitectura.md` § 3 o son su base técnica:

| Dependencia | Para qué | Alternativa descartada |
|---|---|---|
| AGP, Kotlin, plugin de Compose | Compilar la app | Ninguna razonable |
| Compose BOM: `ui`, `foundation`, `material3` | UI; Material 3 solo para ripple, accesibilidad y `LocalContentColor` | Solo `foundation`: obliga a reimplementar ripple e indicaciones táctiles |
| `activity-compose` | `setContent` en la Activity | `ComposeView` en una Activity clásica: más código sin ventaja |
| JUnit Jupiter (JUnit 6, sobre JUnit Platform) | Test JVM de `Tokens.kt` y tests de `:nucleo`; en `:app` se activa con `useJUnitPlatform()` sin plugin adicional | Plugin `android-junit5`: solo necesario para tests instrumentados con JUnit 5, que no se usan |
| `kotlinx-serialization-json` (solo `testImplementation` en `:app`) | Leer `tokens.json` en el test | `org.json` de Android: en tests JVM es un *stub* sin implementación. Ya está en la pila para la persistencia |
| `compose-ui-test-junit4` y `androidx.test` (solo `androidTestImplementation`) | Tests de interfaz en el móvil | Robolectric: dependencia más pesada y no se parece tanto al móvil real |

### 2. Forma de `Tokens.kt`

`generar.py` escribe `app/src/main/kotlin/jmlopezdona/squareshift/ui/tema/Tokens.kt`, con cabecera «Generado por… No editar a mano» y nombres en camelCase derivados de los del JSON (`on-gear-fill` → `onGearFill`, `space-4` → `space4`, `gear-hero` → `gearHero`):

- `data class SqColores(val bg: Color, …)` y dos instancias, `ColoresRodaje` y `ColoresClaro`. Los alias (`focus` = `{gear}`) se resuelven por tema al generar, igual que `contraste.py`.
- `SqTipografia` con un `TextStyle` por estilo: tamaño e interlineado en `sp`, peso, `letterSpacing` en `em` cuando lo hay, y `fontFeatureSettings = "tnum"` en los de la familia `numeric` (la guía aplica `tabular-nums` a todas las cifras). Para igualar el interlineado de CSS, cada estilo lleva `LineHeightStyle(Alignment.Center, Trim.None)` y `includeFontPadding = false`. La mayúscula de `label` la aplica quien lo usa, como en CSS (`text-transform`), no el estilo.
- `SqEspacios`, `SqRadios` y `SqTamanos` como objetos con valores en `dp`. `radius-full` (9999 px) se emite como `CircleShape` en el uso, pero el token conserva su valor numérico para la comparación.
- Familias: `FontFamily` construidas con `R.font.*`. La primera familia de cada pila CSS (`"Barlow Condensed"`) se asocia a los ficheros `design-system/fonts/BarlowCondensed-<Peso>.ttf` por nombre; el peso sale del sufijo (`Regular` 400, `Medium` 500, `SemiBold` 600, `Bold` 700). Las alternativas de la pila (`Roboto Condensed`…) no se trasladan: las fuentes van empaquetadas.
- Para el test, cada grupo tiene una función `porNombre()` que devuelve un mapa de nombre JSON a valor **leyendo las propiedades** (`"on-gear-fill" to onGearFill`), de modo que el test compruebe lo que usan las pantallas y no una tabla aparte.

Alternativa descartada: un hash de `tokens.json` dentro de `Tokens.kt`. Detecta que no se regeneró, pero no una edición a mano y no dice qué token difiere.

### 3. Test de sincronización

`app/src/test/.../TokensSincronizadosTest` (JUnit Jupiter) lee `tokens.json` y compara, en los dos temas, color a color (ARGB), estilo a estilo (tamaño, interlineado, peso, espaciado de letras, familia) y cada espaciado, radio y tamaño; también que no sobren ni falten nombres. Cada fallo nombra el token, el valor esperado y el encontrado, y termina con «Ejecuta python3 scripts/design-system/generar.py».

- La ruta de `tokens.json` llega por `systemProperty` desde Gradle (`rootProject.file("design-system/tokens.json")`), y el fichero se declara como entrada de la tarea de test: si no, Gradle daría el test por actualizado al cambiar solo `tokens.json`.
- Las fuentes se comprueban aparte: que cada peso referido en `Tokens.kt` tenga su fichero en `res/font`, lo que ya garantiza la compilación por `R.font`.

### 4. Fuentes: TTF junto a los WOFF2

Se añaden a `design-system/fonts/` las versiones TTF de los mismos pesos, sacadas de los repositorios oficiales de cada familia (OFL, misma licencia ya incluida). `generar.py` las copia a `app/src/main/res/font/` con nombres válidos de recurso (`barlow_condensed_bold.ttf`). La web sigue usando WOFF2.

Alternativa descartada: convertir WOFF2 a TTF en `generar.py` con `fontTools` y `brotli`. Añade dependencias de Python al generador, que hoy solo usa la biblioteca estándar.

Comprobado al implementar con `fontTools` (en un entorno aislado, no como dependencia del generador): los TTF son la misma versión que los WOFF2 (Barlow y Barlow Condensed 1.408, JetBrains Mono 2.211), contienen todos sus caracteres (los WOFF2 son un subconjunto latino) y los anchos de avance coinciden en todos los caracteres comunes. Barlow y Barlow Condensed salen de `google/fonts`. El repositorio de JetBrains Mono no publica la 2.211 en estático, así que su TTF es la instancia de peso 400 de la fuente variable 2.211 de `google/fonts` (`fontTools varLib.instancer`); la OFL lo permite y la familia no tiene nombre reservado.

### 5. Contraste antes de escribir los derivados de la app

`generar.py` mantiene su comportamiento para `tokens.css` y la guía (se escriben siempre, para poder ver un color que no pasa) pero solo escribe los derivados de la app si `contraste.py` pasa: `Tokens.kt`, las fuentes de `res/font` y `res/values/color_ventana.xml` (el `bg` de rodaje como fondo de la ventana antes de que Compose pinte, para que no destelle en blanco al abrir). Así la app nunca recibe un valor sin comprobar, y el test de sincronización falla hasta que se corrija el color. Cubre el escenario «Contraste insuficiente» de la spec.

### 6. `SqTema`

- `enum class Tema { Rodaje, Claro }` y un composable `SqTema(tema: Tema = Tema.Rodaje, content)` que provee `LocalSqColores`, `LocalSqTipografia`… y un objeto `SqTema` con accesores (`SqTema.colores`, `SqTema.tipografia`, `SqTema.espacios`).
- Anidar `SqTema(Tema.Claro) { … }` cambia solo los colores de ese subárbol (escenario «Parte de la pantalla en claro»), y pinta su fondo `bg` y fija `LocalContentColor` a `ink`, como `data-theme="claro"` en la guía.
- Por dentro envuelve `MaterialTheme` con un esquema derivado de los tokens (`primary` = `gear`, `background` = `bg`…) solo para que ripple, selección de texto e indicadores no salgan en morado. Las pantallas no leen `MaterialTheme.colorScheme`.

### 7. Muestra del tema

- `MuestraTemaActivity` y su composable viven en `app/src/debug/`, con su entrada en un manifiesto de depuración, así que la compilación de distribución no los contiene. No es *launcher*: se abre con `adb shell am start -n jmlopezdona.squareshift.debug/jmlopezdona.squareshift.ui.tema.MuestraTemaActivity`, lo que permite el hook (`desarrollo.md` § 4); por eso la actividad es `exported="true"`, solo en depuración.
- Reproduce las secciones «Color» y «Tipografía» de la guía: cada token de color con su nombre y su muestra, y cada estilo con `nombre · tamaño/interlineado` y su ejemplo, primero en rodaje y luego en claro, en una columna desplazable vertical y horizontalmente (los ejemplos de `gear-hero` no caben en vertical). Recorre `porNombre()`, así que un token nuevo aparece sin tocar la muestra. Los ejemplos tipográficos (`sample` del JSON) se generan en `Tokens.kt` junto a los estilos; los textos de uso se quedan en la guía.
- En los tests instrumentados, `performScrollTo` y `performScrollToNode` solo mueven el contenedor desplazable más cercano, que para cada ejemplo es su fila horizontal; el test de `gear-hero` desplaza la columna con la acción `ScrollBy` hasta que el ejemplo está entero a la vista.
- Resultado en el OnePlus 8 (Android 13, 1080 × 2400, 480 dpi): colores, fuentes, tamaños e interlineados coinciden con `tokens.json` en los dos temas; no se ha visto ninguna diferencia de métricas de fuente que anotar. `gear-ratio-hero` cabe justo en el ancho en vertical. Los tests instrumentados necesitan la pantalla encendida y desbloqueada: con ella apagada, Compose espera sin fallar.
- Sin etapa de maqueta: la referencia ya existe y está validada (secciones de la guía de estilo), y el arranque provisional de `MainActivity` desaparece en el change 3. La validación es en el móvil, comparando con la guía.

## Risks / Trade-offs

- [El interlineado o la métrica de Barlow en Android no coinciden con el navegador] → `LineHeightStyle` e `includeFontPadding = false`; si aún difiere, se anota en la muestra y se acepta la diferencia de Android, sin tocar los tokens.
- [Versiones de AGP, Kotlin y Compose incompatibles entre sí] → se toman de las tablas de compatibilidad oficiales y se fijan en el catálogo; la primera tarea comprueba que `./gradlew :app:assembleDebug` compila vacío.
- [Los TTF descargados son otra versión que los WOFF2] → comprobación de la decisión 4; si no hay una versión igual, se regeneran también los WOFF2 a partir de los TTF y se revisa la guía.
- [`exported="true"` en depuración expone la muestra a otras apps del móvil] → solo muestra colores y textos; no existe en la compilación de distribución.
- [Adelantar el esqueleto mueve trabajo del change 3] → se actualiza su nota en `planificacion.md` en este mismo change.

## Migration Plan

No hay app ni usuarios previos. Revertir es borrar `app/`, `nucleo/` y los ficheros de Gradle, y deshacer la ampliación de `generar.py`.
