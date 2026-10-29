# Design

## Context

Ver `proposal.md` para el motivo. Estado actual:

- El paquete `jmlopezdona.squareshift` aparece en `app/` y `nucleo/` (cinco conjuntos de código: `main`, `test`, `androidTest`, `androidTestDebug` y `debug`), en `app/build.gradle.kts` (`namespace` y `applicationId`), en `scripts/design-system/generar.py` (`PAQUETE` y el import de `R` del `Tokens.kt` generado) y como cadena en la acción `…TERMINAR` del servicio.
- El nombre «Square Shift» sale en tres sitios de ejecución: `ZwiftBridge.NOMBRE` (anuncio por Bluetooth y nombre del mando), `NombreAnunciado.NORMAL` (anuncio DirCon/mDNS) y el recurso `app_name`. Los tests comparan el valor literal.
- `.claude/hooks/adb_guard.py` solo deja usar `adb` sobre `PAQUETE = "jmlopezdona.squareshift"`. Con el paquete nuevo, el hook bloquearía todo `adb` sobre la app.
- Android exige al menos dos segmentos en el `applicationId`, y no se puede cambiar sin crear otra app.
- Los datos de la app (`estado.json`, `configuracion.json` en `filesDir/datastore/`) viven bajo el `applicationId`. El de la app nueva empieza vacío, con identidad propia ante Zwift.
- El texto de las specs vigentes se actualiza desde los deltas de este change al archivarlo; no se editan a mano en `openspec/specs/`.

## Goals / Non-Goals

**Goals:**

- Que ningún texto visible, identificador ni herramienta del repositorio llame «Square Shift» al producto, salvo el registro histórico.
- Que el cambio sea verificable por el compilador y los tests: si queda una referencia al paquete viejo, no compila o falla un test de sincronización.
- Que el renombrado entre en un único commit atómico de código, separado de los cambios de texto y documentación, para que `git log --follow` y `git blame` sigan funcionando.

**Non-Goals:**

- Migrar datos, ni mantener un alias del paquete viejo.
- Reorganizar módulos ni renombrar clases que no lleven el nombre del producto.
- Renombrar `Sq*`, `sq-*` ni nada que se refiera al Elite Square.

## Decisions

### 1. Paquete `app.smartbike`

Un segmento no es válido como `applicationId`, y se descarta el nombre del autor. `app.smartbike` es corto, neutro y no finge ser un dominio propio. Alternativas descartadas: `io.github.indoorcyclinglab.smartbike` (la convención correcta si se publicara desde la organización, pero alarga cada comando `adb`), `com.smartbike` y `org.smartbike` (sugieren un dominio ajeno). Si algún día se publica en una tienda, el id se revisará antes de la primera publicación; mientras sea una app personal, no hay coste.

### 2. Renombrar por sustitución mecánica y comprobar con el compilador

El orden evita sustituciones parciales:

1. `git mv` de `…/kotlin/jmlopezdona/squareshift` a `…/kotlin/app/smartbike` en cada conjunto de código de `app/` y `nucleo/`, para que Git registre renombrados y no borrados y altas.
2. Sustitución textual, solo en ficheros rastreados y excluyendo `openspec/changes/archive/`, `openspec/specs/` y este change:
   - `jmlopezdona.squareshift` → `app.smartbike` y `jmlopezdona/squareshift` → `app/smartbike`.
   - `SquareShift` → `SmartBike` (clase `SmartBikeApp`, estilo `Theme.SmartBike` y su referencia generada `R.style.Theme_SmartBike_Splash`, wake lock `SmartBike:zwift`).
   - `Square Shift` → `Smart Bike` y `square-shift` → `smart-bike`, revisados a mano caso por caso en los textos (ver decisión 5).
3. Compilar y pasar los tests unitarios: `:nucleo:test`, `:app:testDebugUnitTest` y la compilación de los `androidTest`.
4. Regenerar con `python3 scripts/design-system/generar.py` y comprobar que `Tokens.kt` y la guía quedan sin diferencias respecto a lo sustituido (`TokensSincronizadosTest` lo exige).

Alternativa descartada: el renombrado de IDE (refactor de paquete), que no toca cadenas, XML, scripts ni documentos y obligaría a repasar igualmente.

### 3. Un nombre anunciado, tres lugares, una sola fuente de verdad en los tests

`ZwiftBridge.NOMBRE` y `NombreAnunciado.NORMAL` hoy repiten la misma cadena. Se cambian los dos a «Smart Bike» sin unificarlos: unificarlos es un refactor aparte. El nombre del mando por Bluetooth se deriva del primero («Smart Bike Mando»); los tests de `DirconServerTest`, `SesionTest`, `ConexionZwift*Test`, `NombreAnunciadoTest` y `ConsultaMdnsTest` se actualizan al valor nuevo.

«Smart Bike» tiene 10 caracteres; «Smart Bike Mando» tiene 16, menos que los 18 de «Square Shift Mando»: no se agrava el límite del anuncio por Bluetooth.

### 4. Datos y emparejamiento: sin migración

La app nueva arranca con ajustes por defecto y genera su propia identidad (`mac` y serie) ante Zwift. Es lo que el usuario ha elegido: la configuración del móvil es la de fábrica. Zwift verá «Smart Bike» como un dispositivo nuevo, tanto por el nombre como por la identidad, así que hay que emparejarlo de nuevo como rodillo, potencia, cadencia, mando y pulso. `Configuracion.validar` solo mira el campo `version` del JSON, no el nombre ni el id de la app, por lo que una copia exportada con la versión anterior se puede importar en la nueva (se confirma en el móvil en la tarea 2.3); no se promete en las specs.

### 5. Texto histórico: se conserva

- `openspec/changes/archive/` no se toca.
- En `docs/planificacion.md`, las notas por change que narran lo validado con el nombre de entonces se dejan como están; la fila de este change y una nota explican el cambio de nombre. Lo mismo en `docs/arquitectura.md` para el bloque de lo validado en Zwift: se anota «(entonces «Square Shift»)» en vez de reescribirlo.
- En `docs/desarrollo.md`, `docs/arquitectura.md` (descripción vigente), `docs/funcional.md`, `README.md` y `design-system/README.md`, se escribe «Smart Bike». El README deja de decir que el nombre es provisional.
- `ConsultaMdnsTest` conserva «Square Shift» en `instancia`: sus respuestas son capturas reales del anuncio de entonces y el nombre va dentro de los bytes; al analizador le da igual cómo se llame la instancia.
- `docs/referencias/` está fuera del repositorio (ignorado) y no se toca.
- El prefijo `Sq` se queda; `design-system/README.md` aclara que no significa «Square».

### 6. Herramientas del agente: el hook necesita aprobación explícita

`AGENTS.md` prohíbe modificar `.claude/hooks/adb_guard.py` y `.claude/settings.json` sin que el usuario lo pida. Este change lo incluye, pero la tarea correspondiente se detiene y pide confirmación expresa antes de editar el hook. Sin esa confirmación, el hook bloquearía `adb` sobre la app nueva y no se podría probar. `settings.json` no menciona el paquete y no se toca. La skill `desplegar-movil` y `docs/desarrollo.md` cambian en la misma tarea (identificador de la app, comando de arranque con la clase completa y `run-as`).

### 7. Sin maqueta

Solo cambian textos que ya cubren los tokens; la tabla de `AGENTS.md` lo exime de la etapa de maqueta. Se actualizan igualmente los textos de `maquetas/` y de la guía para que sigan siendo la referencia de lo que la app muestra. Los estados afectados son los de Dispositivos y Diagnóstico con Zwift y el mando, `ajustes.html` (copia de seguridad y KICKR) y el índice.

## Risks / Trade-offs

- **[Dos apps instaladas a la vez se pelean por el Neo y por el nombre mDNS]** → La versión vieja se cierra («Terminar» en la notificación) y se desinstala antes de abrir la nueva. Como el hook bloqueará `adb` sobre la app vieja en cuanto cambie, el usuario la desinstala con `! adb uninstall jmlopezdona.squareshift.debug` o desde el móvil.
- **[Una sustitución ciega toca el hardware o la historia]** → «Square» a secas nunca se sustituye; solo las formas completas del nombre. Se revisa el diff de los textos y se cuenta con `git grep` que `Square Shift`, `SquareShift`, `squareshift` y `jmlopezdona` solo quedan en `openspec/changes/archive/`, en las notas históricas decididas en 5 y en este change.
- **[Un derivado generado queda desfasado]** → Se regenera con `generar.py` y se pasan `TokensSincronizadosTest`, `IconosSincronizadosTest` e `IdentidadSincronizadaTest`.
- **[Zwift tarda en olvidar «Square Shift»]** → Es una entrada huérfana en la lista de Zwift; no afecta. Se puede olvidar desde Zwift si molesta.
- **[«Smart Bike» es genérico y puede confundirse con otros dispositivos en la lista de Zwift]** → Con el ajuste «Simular un KICKR BIKE» el nombre ya lleva un sufijo con la identidad. Sin él, el nombre anunciado sigue siendo único en la red del usuario; se revisa si es un problema al probar.
- **[Los tests de instrumentación no se pueden ejecutar sin el móvil]** → Se compilan siempre (`:app:compileDebugAndroidTestKotlin`) y se ejecutan en el OnePlus 8 como parte de la prueba con el equipo.

## Migration Plan

1. Terminar la sesión en la app vieja («Terminar» en la notificación) y desinstalarla del móvil.
2. Aplicar el change, con la compilación y los tests en verde.
3. Instalar la app nueva con la skill `desplegar-movil`, abrirla y comprobar que Rodaje conecta el Neo y el Square.
4. En Zwift, emparejar «Smart Bike» como rodillo, potencia, cadencia, mando y pulso.
5. Archivar el change; el usuario renombra el repositorio en GitHub y la carpeta local. Al cambiar la carpeta, Claude Code deja de ver el historial y la memoria asociados a la ruta anterior.

Marcha atrás: restaurar el commit anterior y reinstalar la app vieja; no se pierde nada porque no hay datos que migrar.
