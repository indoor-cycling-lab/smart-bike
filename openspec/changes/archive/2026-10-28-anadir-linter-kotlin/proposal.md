# Proposal

## Why

El proyecto no tiene ningún linter ni formateador de Kotlin: solo `kotlin.code.style=official`, que es una preferencia del IDE y no se comprueba en ninguna parte. Con 217 ficheros Kotlin, el estilo depende de quién escribe cada fichero: 69 ficheros tienen los imports sin un orden consistente y el renombrado a Smart Bike (change `renombrar-smart-bike`) dejó 38 ficheros con los imports de `app.smartbike` fuera de sitio, que hubo que arreglar a mano. Una comprobación automática detecta esto al compilar y evita que vuelva a pasar, en un proyecto donde la mayoría del código lo escriben agentes.

## What Changes

- Se añade **ktlint** como comprobación de estilo del Kotlin de `:app` y `:core`, con las tareas `ktlintCheck` y `ktlintFormat` de Gradle.
- Se añade un `.editorconfig` en la raíz con el estilo acordado: el del IDE (`intellij_idea`), imports en orden lexicográfico simple (el que ya sigue la mayor parte del código), y sin límite de longitud de línea, porque el código ya usa líneas largas a propósito (354 pasan de 140 caracteres).
- Se excluye lo generado (`Tokens.kt`, que escribe `generar.py`) y lo de `build/`.
- Se aplica una sola vez el formateo automático a lo que ya hay, en un commit aparte, para que el linter parta en verde. Las reglas que no se puedan corregir solas y tengan muchas infracciones se desactivan en `.editorconfig` o se anotan en un baseline; no se reescribe código a mano.
- `./gradlew ktlintCheck` pasa a ser parte del flujo: `AGENTS.md` lo pide antes de dar una tarea por terminada, y `docs/desarrollo.md` lo documenta junto a los tests.

Queda fuera de alcance:

- Cualquier cambio de comportamiento: el formateo no cambia lo que hace el código, y los tests lo confirman.
- Otros analizadores: detekt (complejidad, olores de código) y la configuración de Android Lint.
- El estilo de Python (`scripts/`), HTML, CSS y JavaScript (`maquetas/`, `design-system/`).
- Integración continua y hooks de Git: el repositorio no tiene `.github/` ni hooks de commit, y no se añaden aquí.
- Adoptar el estilo estricto `ktlint_official` (más reglas de formato, muchas más infracciones).

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

Ninguna. Es herramienta de desarrollo sin efecto observable en la app, así que el change declara `skip_specs: true`.

## Impact

- **Código:** el formateo automático tocará, como mucho, los 217 ficheros Kotlin (sobre todo imports, espacios y saltos de línea), sin cambiar su comportamiento. Para mantener el historial legible va en un commit separado.
- **Build:** `gradle/libs.versions.toml` (plugin de ktlint), `build.gradle.kts` raíz, `app/build.gradle.kts`, `core/build.gradle.kts` y un `.editorconfig` nuevo. Una dependencia nueva de desarrollo; no entra en la app.
- **Documentos de `docs/`:** cambia `desarrollo.md` (comando y qué hacer si falla) y `planificacion.md` (fila del change). `funcional.md` y `arquitectura.md`: ninguno. También `AGENTS.md`.
- **Equipo real:** no aplica; no toca BLE ni Zwift. La comprobación completa es compilar y pasar los tests unitarios y los de instrumentación en el móvil después del formateo.
- **Orden:** depende de `renombrar-smart-bike` (change 18), porque el formateo parte de los ficheros ya movidos a `app/smartbike`.
