# Proposal

## Why

Toda la interfaz de la app (F11, F12, F13) se construye con los tokens de `design-system/`, pero hoy esos tokens solo llegan a la web (`tokens.css`) y el proyecto Android aún no existe. Antes de la primera pantalla (Diagnóstico, change 3) hace falta que los mismos valores lleguen a Compose sin copiarlos a mano y que, si alguien cambia `tokens.json` y olvida regenerar, la compilación de la app lo detecte (`arquitectura.md` § 9.1).

## What Changes

- Se crea el proyecto Android mínimo con Gradle Wrapper: el módulo `:nucleo` (Kotlin/JVM, vacío salvo la configuración) y el módulo `:app` (Compose, una Activity, `minSdk 28`, `applicationId` `jmlopezdona.squareshift` y `.debug` en depuración). Era trabajo previsto en el change 3; se adelanta aquí porque el tema necesita un módulo donde vivir y compilarse.
- `scripts/design-system/generar.py` emite además `Tokens.kt` en el paquete `ui.tema` de `:app`: colores de los temas `rodaje` y `claro`, familias y estilos tipográficos, espaciados, radios y tamaños táctiles.
- Las fuentes de `design-system/fonts/` llegan a `res/font` de la app, generadas por el mismo script.
- Tema propio `SqTema` con `CompositionLocal` y los nombres de los tokens (`SqTema.colores.onGearFill`), en tema rodaje por defecto y con el tema claro aplicable a una pantalla o a una parte de ella. Material 3 queda solo como base técnica.
- Test JVM de `:app` que lee `tokens.json` y falla si `Tokens.kt` no coincide, indicando que hay que regenerar.
- Pantalla «Muestra del tema», solo en la compilación de depuración, que reproduce las secciones de color y tipografía de la guía de estilo en los dos temas, para comprobar en el OnePlus 8 que la app y la guía se ven igual.
- `AGENTS.md` incluye `Tokens.kt` y las fuentes de `res/font` entre los ficheros generados.

Fuera de alcance:

- Los composables de componentes `sq-*` (`GearDisplay`, `MetricGrid`…): cada uno llega con la primera pantalla que lo usa.
- Los iconos de `icons.svg` como *vector drawables*: los importa el primer change que los necesite (previsiblemente el 3).
- Pantallas reales, navegación, `AppGraph`, permisos, BLE y `RideService`: changes 3 en adelante.
- Elegir el tema desde ajustes: no figura en `funcional.md`; la app arranca en rodaje.
- Cambios de valores de tokens o de colores: se usan los actuales tal cual.

Este change es el único sin porción vertical con el Neo, el Square o Zwift (`planificacion.md`, change 2). Su prueba de principio a fin es en el móvil real: instalar la app de depuración en el OnePlus 8 con la skill `desplegar-movil`, abrir la muestra y compararla con la guía de estilo en los dos temas.

## Capabilities

### New Capabilities

- `app-theme`: cómo llegan los tokens del sistema de diseño a la app Android (colores de los dos temas, tipografía con fuentes empaquetadas, espaciados, radios y tamaños táctiles), cómo se detecta que los derivados de la app están desactualizados y la muestra del tema en la compilación de depuración.

### Modified Capabilities

Ninguna. `design-tokens` ya exige una única fuente de tokens y su comprobación de contraste; este change añade un derivado más sin cambiar sus requisitos.

## Impact

- **Código nuevo:** `settings.gradle.kts`, `build.gradle.kts`, `gradle/` (wrapper y catálogo de versiones), `nucleo/`, `app/` con `ui/tema/` (`Tokens.kt` generado, `SqTema`) y la muestra del tema en `app/src/debug/`.
- **Código modificado:** `scripts/design-system/generar.py` (nuevo derivado `Tokens.kt` y copia de fuentes).
- **Dependencias nuevas:** Android Gradle Plugin, Kotlin y su plugin de Compose, Compose BOM (UI, Foundation, Material 3), `activity-compose`, JUnit 5 para el test JVM. Todas figuran en la pila de `arquitectura.md` § 3 o son su base técnica; las versiones se justifican en `design.md`.
- **Fuentes:** se añaden las versiones TTF de Barlow, Barlow Condensed y JetBrains Mono a `design-system/fonts/` (Android no lee WOFF2), con la misma licencia OFL.
- **Documentos de `docs/`:**
  - `planificacion.md`: fila del change 2 «En curso» y luego «Archivado»; la nota del change 3 deja de incluir el esqueleto del proyecto y el `applicationId`.
  - `arquitectura.md` § 9.1: fuentes TTF, muestra del tema en depuración y momento en que se importan los iconos.
  - `desarrollo.md`: el proyecto Android ya existe; se confirman los comandos de Gradle del § 5 que apliquen.
  - `funcional.md`: ninguno.
- **`AGENTS.md`:** lista de ficheros generados.
