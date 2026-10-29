# Tasks

## 1. Esqueleto del proyecto Android

- [x] 1.1 Generar el Gradle Wrapper con la distribución de gradle.org y crear `settings.gradle.kts`, `build.gradle.kts` raíz y `gradle/libs.versions.toml` con las versiones compatibles de AGP, Kotlin, plugin de Compose y Compose BOM (design.md § 1); verificar con `./gradlew --version` que usa JDK 21
- [x] 1.2 Crear `:nucleo` (Kotlin/JVM 21, JUnit Jupiter, sin código) y verificar que `./gradlew :nucleo:test` termina sin errores
- [x] 1.3 Crear `:app` con `namespace` y `applicationId` `jmlopezdona.squareshift`, sufijo `.debug`, `minSdk 28`, `compileSdk`/`targetSdk 36`, dependencia de `:nucleo` y `MainActivity` vacía con `setContent`; verificar que `./gradlew :app:assembleDebug` genera el APK y que `aapt dump badging` muestra `jmlopezdona.squareshift.debug`
- [x] 1.4 Configurar en `:app` JUnit Jupiter para los tests JVM (`useJUnitPlatform()`) y Compose UI Test para los instrumentados, con un test trivial de cada tipo; verificar que `./gradlew :app:testDebugUnitTest` pasa

## 2. Fuentes TTF

- [x] 2.1 Añadir a `design-system/fonts/` los TTF de Barlow (Regular, SemiBold), Barlow Condensed (Medium, SemiBold, Bold) y JetBrains Mono (Regular) desde los repositorios oficiales; verificar que son la misma versión que los WOFF2 comparando la versión del nombre de fuente y el número de glifos (design.md § 4) y que la licencia OFL incluida los cubre

## 3. Generador de `Tokens.kt`

- [x] 3.1 Ampliar `generar.py` para emitir `Tokens.kt` (colores de los dos temas con alias resueltos, tipografía con `tnum` en las cifras y `LineHeightStyle`, espaciados, radios, tamaños, familias con `R.font`, ejemplos tipográficos y funciones `porNombre()`) según design.md § 2, y copiar los TTF a `app/src/main/res/font/`; verificar que `python3 scripts/design-system/generar.py` termina sin error, que `tokens.css` y la guía no cambian (`git diff`) y que `./gradlew :app:assembleDebug` compila con el fichero generado
- [x] 3.2 Hacer que `generar.py` solo escriba `Tokens.kt` y copie las fuentes si `contraste.py` pasa (design.md § 5); verificar cambiando de forma temporal `ink-muted` de rodaje a un valor sin contraste que el generador falla con el par y su ratio y que `Tokens.kt` no cambia, y deshacer el cambio
- [x] 3.3 Escribir `TokensSincronizadosTest` (JUnit Jupiter) que compara `tokens.json` con `porNombre()` en los dos temas, detecta nombres que sobran o faltan y termina cada fallo con el comando de regeneración; pasar la ruta por `systemProperty` y declarar `tokens.json` como entrada de la tarea de test; verificar que pasa, que falla nombrando el token tras editar un valor en `tokens.json` sin regenerar, que vuelve a pasar tras regenerar y que Gradle no lo da por actualizado si solo cambia `tokens.json`

## 4. Tema `SqTema`

- [x] 4.1 Implementar `Tema`, `SqTema` con sus `CompositionLocal` y accesores, el fondo y `LocalContentColor` por tema y el `MaterialTheme` interno derivado de los tokens (design.md § 6); `MainActivity` muestra el arranque provisional en `SqTema`
- [x] 4.2 Test instrumentado de Compose: sin tema elegido se usa `bg` e `ink` de rodaje; un `SqTema(Tema.Claro)` anidado cambia solo su subárbol; `focus` dentro de claro vale `gear` de claro; verificar con `./gradlew connectedDebugAndroidTest` en el OnePlus 8 (skill `desplegar-movil` para conectar)

## 5. Muestra del tema (solo depuración)

- [x] 5.1 Crear `MuestraTemaActivity` y su composable en `app/src/debug/`, con manifiesto de depuración y `exported="true"`, que recorren `porNombre()` y muestran colores y estilos en rodaje y en claro con desplazamiento vertical y horizontal (design.md § 7); verificar que `./gradlew :app:assembleRelease` no contiene la clase (`apkanalyzer dex packages` o `unzip` + búsqueda en `classes.dex`)
- [x] 5.2 Test instrumentado de la muestra: aparecen todos los nombres de color y de estilo en los dos temas y `gear-hero` se alcanza desplazando; verificar con `./gradlew connectedDebugAndroidTest`
- [x] 5.3 Instalar con la skill `desplegar-movil`, abrir la muestra con `adb shell am start -n jmlopezdona.squareshift.debug/jmlopezdona.squareshift.ui.tema.MuestraTemaActivity` y capturar la pantalla en los dos temas; comparar con las secciones «Color» y «Tipografía» de `http://localhost:8000/design-system/styleguide/` y anotar en el change cualquier diferencia de métricas de fuente
- [x] 5.4 Validación de la muestra en el móvil por el usuario: dar al usuario el comando para abrirla y el enlace a la guía, y marcar solo con su aprobación explícita

## 6. Documentación

- [x] 6.1 Actualizar `AGENTS.md` («UI y sistema de diseño») para incluir `Tokens.kt` y `app/src/main/res/font/` entre los ficheros generados que no se editan a mano; verificar que el párrafo nombra el comando de regeneración y el test que detecta el desajuste
- [x] 6.2 Actualizar la documentación: revisar `docs/funcional.md`, `docs/arquitectura.md` (§ 9.1: fuentes TTF, contraste antes de escribir `Tokens.kt`, muestra del tema en depuración, iconos en el primer change que los use), `docs/planificacion.md` (fila del change 2; la nota del change 3 ya no incluye el esqueleto ni el `applicationId`) y `docs/desarrollo.md` (el proyecto existe, versiones fijadas, comandos del § 5 confirmados y cómo abrir la muestra), y actualizar lo que el change haya cambiado o decidido

## Workflow follow-up

- Archivar con `/openspec-archive-change` cuando la tarea 5.4 tenga la aprobación del usuario y la 6.2 esté hecha; al archivar, marcar el change como «Archivado» en `docs/planificacion.md`.
