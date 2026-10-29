# Design

## Context

Ver `proposal.md` para el motivo. Estado actual, medido el 27-10-2026:

- 217 ficheros Kotlin en `:app` (main, test, androidTest, androidTestDebug, debug) y `:core` (main, test). Sin `.editorconfig`, sin ktlint, detekt ni Spotless, y sin `.github/`.
- 1337 líneas de más de 120 caracteres (354 de más de 140): el código usa líneas largas de forma habitual, sobre todo en strings de tests y comentarios KDoc.
- Los imports siguen en la práctica el orden lexicográfico simple (`android`, `androidx`, `app`, `java`, `kotlinx`…), sin mover `java`, `javax` y `kotlin` al final como hace el diseño por defecto del IDE.
- `Tokens.kt` lo escribe `scripts/design-system/generar.py` y `TokensSincronizadosTest` exige que coincida con su fuente: el linter no debe pedir que se reformatee.
- Gradle 9.8.2, AGP 9.4.1, Kotlin 2.4.21, con la caché de configuración activada (`org.gradle.configuration-cache=true`) y los plugins declarados con alias en `gradle/libs.versions.toml`.
- Los agentes escriben una parte importante del código y comprueban su trabajo con `./gradlew … test`; no hay integración continua.

## Goals / Non-Goals

**Goals:**

- Que una infracción de estilo falle con un comando que los agentes ya saben ejecutar (`./gradlew ktlintCheck`), y que el formateo automático la corrija en la mayoría de casos.
- Partir en verde sin reescribir a mano el código existente.
- Que el estilo configurado refleje cómo está escrito el código hoy, no un ideal nuevo.

**Non-Goals:**

- Elegir el estilo más estricto posible, ni medir complejidad o detectar olores de código.
- Bloquear commits o compilaciones: es una comprobación que se ejecuta, no un portero.

## Decisions

### 1. ktlint, con el estilo `intellij_idea`

Es el linter y formateador de referencia de Kotlin, se ejecuta rápido, corrige solo la mayoría de sus avisos y se configura con `.editorconfig`, que además lo leen el IDE y los agentes. Se usa `ktlint_code_style = intellij_idea` y no `ktlint_official` porque el primero es lo que ya hace el IDE con `kotlin.code.style=official`, y el segundo añade reglas de envoltura de líneas y de lambdas que tocarían cientos de líneas sin aportar nada aquí.

Alternativas descartadas:

- **detekt:** analiza complejidad y olores, no formato; es un objetivo distinto y mayor, y queda como posible change posterior.
- **Spotless con ktlint:** hace lo mismo que el plugin de ktlint con más configuración y, además, gestiona otros lenguajes que aquí no se quieren.
- **Android Lint de AGP:** ya existe y revisa errores de Android (recursos, API), no estilo de Kotlin.

### 2. El plugin de Gradle de JLLeitschuh, con un plan B

Se usa `org.jlleitschuh.gradle.ktlint`: da las tareas `ktlintCheck` y `ktlintFormat` por módulo, cuelga `ktlintCheck` de `check` y admite baseline. La versión del plugin y la de ktlint se fijan en `gradle/libs.versions.toml` con la última estable disponible al aplicar el change, con más de dos semanas de antigüedad.

Riesgo: la compatibilidad con Gradle 9.8.2, con AGP 9.4.1 y con la caché de configuración no está comprobada. Se comprueba en la primera tarea. **Plan B** si no funciona: ejecutar el CLI de ktlint fijado por versión desde un script en `scripts/` y documentarlo en `desarrollo.md`; pierde la integración con `check`, pero no depende del plugin.

### 3. `.editorconfig` refleja el código actual

- `ktlint_code_style = intellij_idea`.
- `ij_kotlin_imports_layout = *`: orden lexicográfico simple, sin mover `java`, `javax` y `kotlin` al final, que es lo que ya hace la mayor parte del código.
- `max_line_length = off`.
- Sin otras reglas desactivadas de entrada. Si al medir una regla no corregible sola tiene muchas infracciones, se desactiva en `.editorconfig` con un comentario que diga cuántas había y por qué, o se anota en el baseline si son pocas y localizadas.

Así la configuración nace de una medición, no de una opinión; esa medición queda anotada en este `design.md` al aplicar (tarea 1.3).

**Medición del 28-10-2026** (ktlint 1.8.0, plugin 14.2.0, `intellij_idea`, `Tokens.kt` excluido): 1161 infracciones en 148 ficheros; `ktlintFormat` corrige 1025 y quedan 136.

| Regla | Antes | Después de `ktlintFormat` | Decisión |
|---|---|---|---|
| `statement-wrapping` | 206 | 0 | Activa |
| `blank-line-between-when-conditions` | 196 | 0 | Activa |
| `argument-list-wrapping` | 180 | 0 | Activa |
| `function-signature` | 108 | 0 | Activa |
| `annotation` | 64 | 0 | Activa |
| `wrapping`, `multiline-if-else`, `spacing-between-declarations-with-annotations`, `indent`, `import-ordering`, `enum-wrapping`, `parameter-list-wrapping`, `class-signature`, `trailing-comma-*`, `spacing-between-declarations-with-comments`, `no-consecutive-blank-lines`, `function-literal`, `string-template`, `paren-spacing`, `multiline-loop` | 224 | 0 | Activas |
| `function-naming` | 131 | 131 | Todas son `@Composable` (PascalCase, como los componentes del sistema de diseño): se mantiene la regla con `ktlint_function_naming_ignore_when_annotated_with = Composable` en `.editorconfig`; no se desactiva |
| `kdoc` | 3 | 3 | Se arreglan a mano (tarea 2.2) |
| `filename` | 1 | 1 | `Bajadas.kt` contiene `ModoBajadas`; se arregla a mano (tarea 2.2) |
| `property-naming` | 1 | 1 | Se arregla a mano (tarea 2.2) |

No hace falta baseline: tras la regla de `@Composable` quedan 5 infracciones, localizadas y triviales. Nota: el repositorio sí tiene `.github/workflows/pages.yml`, pero solo publica la web (`site/`); no hay integración continua del Kotlin, así que la decisión 6 no cambia.

### 4. Lo generado queda fuera

`Tokens.kt` y todo lo que cuelgue de `build/` se excluyen con el filtro del plugin. Reformatear `Tokens.kt` rompería `TokensSincronizadosTest` y la siguiente ejecución de `generar.py` lo desharía. `generar.py` sigue siendo la única fuente de ese fichero.

### 5. Formatear una vez, en su propio commit

Tras fijar la configuración se ejecuta `ktlintFormat` sobre todo el Kotlin y se comprueba con compilación, tests unitarios y los de instrumentación en el móvil. Va en un commit separado del de configuración para que `git blame` pueda saltárselo (`.git-blame-ignore-revs`) y para poder revertirlo sin perder la configuración. Se vuelve a medir antes de formatear si el renombrado u otro change han movido ficheros.

### 6. Sin portero automático

No hay `.github/` ni hooks de commit, y no se añaden: el repositorio lo trabajan una persona y agentes, y un hook que falle por estilo estorba más que ayuda. La comprobación se hace explícita en el flujo: `AGENTS.md` pide `./gradlew ktlintCheck` antes de dar una tarea por terminada, junto a los tests, y `docs/desarrollo.md` lo documenta.

### Comprobación en el móvil (28-10-2026)

`./gradlew :app:connectedDebugAndroidTest` en el OnePlus 8 da 239 tests y 8 fallos, no los 233 en verde del 27-10-2026. Se ejecutó la suite completa también sin el formateo (`HEAD` con solo la configuración) y fallan exactamente los mismos 8, así que el formateo no cambia el resultado. Los 8 son previos a este change:

- `AjustesTest.copia_de_seguridad_va_despues_de_zwift_con_objetivos_de_48_dp`
- `BotonesTest.mando_desactivado`
- `PlataformasTest`: `estado_inicial`, `rodillo_por_bluetooth_conectado`, `mando_desactivado_y_no_disponible`, `kickr_bike_se_nombra_en_las_ayudas` y `elegir_el_modo_el_mando_y_el_nombre`
- `RodajeTest.todo_visible_en_un_movil_pequeno_con_botones`

Parecen venir de `ftms-bluetooth` (change 11): nodos duplicados con el texto «Zwift», textos que ya no existen y la marcha que no se ve en un móvil pequeño. Se arreglan fuera de este change.

## Risks / Trade-offs

- **[El formateo automático cambia algo más que el aspecto]** → Es un formateador de espacios, saltos de línea e imports, pero se comprueba igual: compilación, `:core:test`, `:app:testDebugUnitTest` y los tests de instrumentación en el móvil, comparando con lo que pasaba antes. Si algo falla, se revierte el commit de formateo.
- **[Un diff enorme tapa los cambios de otros changes]** → Se aplica con el árbol limpio y sin otros changes en vuelo, en su propio commit.
- **[El plugin no soporta esta combinación de Gradle y AGP]** → Plan B de la decisión 2.
- **[Las reglas se vuelven una molestia y se ignoran]** → Se parte de un estilo laxo (decisión 3) y solo se endurece con una decisión explícita.
- **[`ktlintFormat` y `generar.py` discrepan sobre `Tokens.kt`]** → Queda excluido (decisión 4).

## Migration Plan

1. Terminar y archivar `renombrar-smart-bike` (el formateo parte de los ficheros ya movidos).
2. Aplicar el change: configuración, medición, formateo en un commit aparte, documentación.
3. Marcha atrás: revertir el commit de formateo; la configuración puede quedarse sola, con el baseline o desactivando la comprobación en `check`.

## Open Questions

- ¿Conviene un `.git-blame-ignore-revs` con el commit de formateo? Es una decisión menor que se toma al aplicar, sin cambiar el plan.
