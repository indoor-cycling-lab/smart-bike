# Tasks

No hay sección «Maqueta»: no hay impacto visual. Antes de empezar, `renombrar-smart-bike` debe estar archivado y el árbol de Git limpio (design.md, «Migration Plan»).

## 1. Plugin y medición

- [x] 1.1 Añadir el plugin de ktlint y su versión a `gradle/libs.versions.toml` (última estable con más de dos semanas, decisión 2), declararlo en el `build.gradle.kts` raíz con `apply false` y aplicarlo en `app/build.gradle.kts` y `core/build.gradle.kts`. Verificar: `./gradlew ktlintCheck` arranca sin error de configuración con la caché de configuración activada y sin avisos de incompatibilidad con Gradle 9.8.2 y AGP 9.4.1. Si no funciona, parar y pasar al plan B de design.md (decisión 2) antes de seguir.
- [x] 1.2 Crear `.editorconfig` en la raíz con `ktlint_code_style = intellij_idea`, `ij_kotlin_imports_layout = *` y `max_line_length = off` (decisión 3), y excluir `Tokens.kt` y `build/` en el filtro del plugin (decisión 4). Verificar: `./gradlew ktlintCheck` ejecuta sin error de configuración y no menciona `Tokens.kt`.
- [x] 1.3 Medir con `./gradlew ktlintCheck` cuántas infracciones hay por regla y cuántas corrige `ktlintFormat`. Anotar el recuento en `design.md` (decisión 3) y decidir, regla a regla, si se deja activa, se desactiva en `.editorconfig` con un comentario que diga cuántas eran y por qué, o va al baseline. Verificar: el recuento figura en `design.md` y `.editorconfig` explica cada regla desactivada.

## 2. Formateo del código existente

- [x] 2.1 Pedir al usuario que haga el commit de configuración (plugin, `.editorconfig`, `design.md` con la medición) antes de formatear; no se hace commit sin que lo pida. Verificar: `git log -1 --stat` solo muestra esos ficheros.
- [x] 2.2 Ejecutar `./gradlew ktlintFormat` y arreglar a mano las infracciones que queden y no se hayan desactivado ni anotado en el baseline. Verificar: `./gradlew ktlintCheck` acaba en `BUILD SUCCESSFUL`.
- [x] 2.3 Comprobar que el formateo no cambia el comportamiento: `./gradlew :core:test :app:testDebugUnitTest :app:compileDebugAndroidTestKotlin` en verde, e `python3 scripts/design-system/generar.py` sin dejar diferencias en `git status`. Verificar: esos comandos en verde y `git status` sin cambios tras el generador.
- [x] 2.4 Con el OnePlus 8 conectado (skill `desplegar-movil`), ejecutar `./gradlew :app:connectedDebugAndroidTest` y comprobar en el informe de `app/build/reports/androidTests/connected/debug/index.html` que pasan todos los tests (233 el 27-10-2026); el código de salida no es fiable (`docs/desarrollo.md`). Después, `./gradlew installDebug` y avisar al usuario de que el emparejamiento de Zwift se pierde, por la desinstalación de los tests. Verificar: el informe da 0 fallos y la app queda instalada. **Resultado (28-10-2026):** 239 tests y 8 fallos, los mismos 8 con el código de antes del formateo (comparado con la suite completa en `HEAD`), así que no los causa el formateo; el usuario decidió darlo por bueno y arreglarlos aparte (ver `design.md`, «Comprobación en el móvil»). App instalada.
- [x] 2.5 Pedir al usuario que haga el formateo en un commit aparte (decisión 5) y, si se decide al aplicar, añadir su hash a `.git-blame-ignore-revs`. Verificar: `git show --stat` del commit solo contiene cambios de formato.

## 3. Flujo de trabajo

- [x] 3.1 Añadir a `AGENTS.md` la regla de ejecutar `./gradlew ktlintCheck` antes de dar una tarea de código por terminada, y de usar `ktlintFormat` para corregir lo que se pueda. Comprobar que `ktlintCheck` forma parte de `./gradlew check`. Verificar: `./gradlew check --dry-run` lista `ktlintCheck` para `:app` y `:core`.
- [x] 3.2 Comprobar que la regla funciona: introducir a propósito una infracción (por ejemplo, un import sin usar en un fichero de `:core`), ver que `./gradlew ktlintCheck` falla con el fichero y la línea, deshacerla y ver que vuelve a pasar. Verificar: el fallo mensajea la infracción y `git status` queda limpio después.

## 4. Documentación

- [x] 4.1 Actualizar la documentación: revisar `docs/funcional.md` (ninguno), `docs/arquitectura.md` (ninguno), `docs/planificacion.md` (fila del change y nota con la medición y lo decidido) y `docs/desarrollo.md` (sección con los comandos `ktlintCheck` y `ktlintFormat`, qué hacer cuando falla y qué se excluye). Verificar: `git grep -n ktlint -- docs AGENTS.md` muestra la regla de `AGENTS.md` y la sección de `desarrollo.md`.
