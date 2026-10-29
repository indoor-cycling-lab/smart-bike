# Tasks

No hay sección «Maqueta»: solo cambian textos (design.md, decisión 7). Antes de empezar, cerrar la sesión de la app vieja y desinstalarla del móvil (design.md, «Migration Plan»).

## 1. Paquete y carpetas

- [x] 1.1 Mover con `git mv` `kotlin/jmlopezdona/squareshift` a `kotlin/app/smartbike` en `nucleo/src/{main,test}` y en `app/src/{main,test,androidTest,androidTestDebug,debug}`, y borrar las carpetas `jmlopezdona/` vacías. Verificar: `git status` muestra renombrados (`R`), no borrados y altas, y `find . -path ./build -prune -o -type d -name squareshift -print` no devuelve nada.
- [x] 1.2 Sustituir `jmlopezdona.squareshift` por `app.smartbike` (y `jmlopezdona/squareshift` por `app/smartbike`) en los `package`, `import`, `namespace` y `applicationId` de `app/build.gradle.kts`, en la acción `…TERMINAR` de `RideService` y en cualquier otra cadena. Verificar: `git grep -n 'jmlopezdona'` solo devuelve `openspec/changes/archive/`, `openspec/specs/` (hasta archivar), `docs/` y este change.
- [x] 1.3 Cambiar `rootProject.name` a `smart-bike` en `settings.gradle.kts`. Verificar: `./gradlew projects` muestra `Root project 'smart-bike'`.
- [x] 1.4 Renombrar `SquareShiftApp` a `SmartBikeApp` (fichero, clase y usos en `MainActivity`, `RideService` y los `ViewModel`), `Theme.SquareShift` a `Theme.SmartBike` y `Theme.SquareShift.Splash` a `Theme.SmartBike.Splash` (`themes.xml`, `AndroidManifest.xml` y `R.style.Theme_SmartBike_Splash` en `IdentidadTest`), y el wake lock `SquareShift:zwift` a `SmartBike:zwift`. Verificar: `git grep -n 'SquareShift'` no devuelve nada fuera del archivo y de las notas históricas.
- [x] 1.5 Ejecutar `./gradlew :nucleo:test :app:testDebugUnitTest :app:compileDebugAndroidTestKotlin` y dejarlo en verde. Verificar: la salida acaba en `BUILD SUCCESSFUL`; los fallos por el texto del nombre se arreglan en el grupo 2, no aquí.

## 2. Nombre anunciado y textos de la app

- [x] 2.1 Cambiar `ZwiftBridge.NOMBRE` y `NombreAnunciado.NORMAL` a «Smart Bike», con el comentario de `Configuracion.kt` al día, y actualizar en el mismo paso los tests que comparan el valor: `NombreAnunciadoTest`, `ConsultaMdnsTest`, `DirconServerTest` (incluido «Smart Bike Mando»), `SesionTest`, `ConexionZwiftFtmsTest`, `ConexionZwiftRideTest`, `DiagnosticoUiStateTest` y `DispositivosUiStateTest`. Verificar: esos tests en verde con `./gradlew :nucleo:test :app:testDebugUnitTest`.
- [x] 2.2 Cambiar `app_name` y los textos de `strings.xml` que dicen «Square Shift» (ayuda de Zwift, aviso del modo «Todo por wifi» con y sin KICKR, «Dentro de Smart Bike · por wifi», ayuda de «Simular un KICKR BIKE», mensajes de error de la importación), y los comentarios del código que nombran el producto. Los textos que dicen «Square» a secas no se tocan. Actualizar `AjustesTest`, `DispositivosTest` y `DiagnosticoTest` con las cadenas nuevas. Verificar: `./gradlew :app:compileDebugAndroidTestKotlin` y, con el móvil, esos tres tests de instrumentación en verde.
- [x] 2.3 Cambiar el nombre del archivo de copia de seguridad a `smart-bike-configuracion-<fecha>.json` en `Ajustes.kt`. Comprobar que `CopiaConfiguracion` no lee el nombre del archivo ni un identificador de la app, y anotar el resultado en `design.md` (decisión 4). Verificar: exportar en el móvil y ver el nombre propuesto; importar un archivo exportado con la versión anterior (si existe uno a mano) o con el nuevo.

## 3. Herramientas del repositorio

- [x] 3.1 En `scripts/design-system/generar.py`, cambiar `PAQUETE` y el import de `R` al paquete nuevo y el título y el encabezado de la guía a «Smart Bike», y regenerar con `python3 scripts/design-system/generar.py`. Verificar: el generador acaba sin error de contraste, `git diff` de `Tokens.kt` y `styleguide/index.html` solo muestra el paquete y el nombre, y pasan `TokensSincronizadosTest`, `IconosSincronizadosTest` e `IdentidadSincronizadaTest`.
- [x] 3.2 **Parar y pedir confirmación expresa al usuario** antes de editar `.claude/hooks/adb_guard.py` (`AGENTS.md` lo prohíbe sin que el usuario lo pida). Con su visto bueno, cambiar `PAQUETE` a `app.smartbike`. No tocar `.claude/settings.json`. Verificar: el hook deja pasar `adb shell pidof -s app.smartbike.debug` y sigue bloqueando un paquete ajeno (por ejemplo `adb shell pidof -s org.cagnulen.qdomyoszwift`).
- [x] 3.3 Actualizar `.claude/skills/desplegar-movil/SKILL.md` (identificador `app.smartbike.debug` y arranque `app.smartbike.debug/app.smartbike.MainActivity`). Verificar: `git grep -n 'squareshift' -- .claude` no devuelve nada.

## 4. Maquetas y guía de estilo

- [x] 4.1 Sustituir «Square Shift» por «Smart Bike» en `maquetas/` (`index.html`, `ajustes.html`, `dispositivos.html`, `diagnostico.html`, `tabla-marchas.html`, `rodaje.html`, `rodaje.js`) y en `design-system/README.md` y las `preview.html` de `Cover` y `SettingsList`, dejando intacto «Square» como mando. Añadir a `design-system/README.md` una línea que aclare que el prefijo `Sq` no significa «Square». Regenerar la guía. Verificar: abrir con `python3 -m http.server 8000` `maquetas/dispositivos.html#estado=mando-wifi-esperando` y `maquetas/diagnostico.html` y ver «Smart Bike»; `git grep -n 'Square Shift' -- maquetas design-system` no devuelve nada.

## 5. Prueba con el equipo real

- [x] 5.1 Con la skill `desplegar-movil` (y el hook ya actualizado), instalar la app de depuración en el OnePlus 8, abrirla y comprobar en el registro que arranca sin errores y que Rodaje conecta el Neo y el Square. Verificar: la sesión recibe marchas y el Neo responde igual que antes del cambio.
- [x] 5.2 Comprobar el anuncio desde el Mac con `dns-sd -B _wahoo-fitness-tnp._tcp` (con un límite de tiempo): debe listar «Smart Bike» y no «Square Shift». Pedir al usuario que, en Zwift, empareje «Smart Bike» como rodillo, potencia, cadencia, mando y pulso, y que compruebe que las marchas y la potencia funcionan. Verificar: confirmación explícita del usuario; lo que no se pruebe se anota como «a validar» en `docs/planificacion.md`.
- [x] 5.3 Con «Simular un KICKR BIKE» activado, comprobar que Zwift lista «KICKR BIKE PRO» y, al desactivarlo, «Smart Bike». Verificar: confirmación del usuario.

## 6. Documentación

- [x] 6.1 Actualizar la documentación: revisar `docs/funcional.md` (la decisión abierta del nombre queda resuelta y F16.2 dice «Smart Bike»), `docs/arquitectura.md` (diagrama, nombre del anuncio mDNS y nota «entonces «Square Shift»» en lo validado), `docs/planificacion.md` (fila de este change y nota del cambio de nombre; las notas históricas se dejan) y `docs/desarrollo.md` (identificador `app.smartbike`, comandos `adb`, `run-as` y nombre en `dns-sd`). Actualizar `README.md` (título, descripción sin «nombre provisional») y `AGENTS.md` (ruta de `Tokens.kt`). Verificar: `git grep -n -E 'Square Shift|SquareShift|squareshift|jmlopezdona' -- . ':!openspec/changes/archive' ':!openspec/changes/renombrar-smart-bike'` solo devuelve las notas históricas decididas en `design.md` (decisión 5) y las specs vigentes, que se actualizan al archivar.

## Workflow follow-up

- Al archivar con `/openspec-archive-change`, los deltas actualizan las specs vigentes; comprobar después que `git grep -n 'Square Shift' -- openspec/specs` no devuelve nada.
- El usuario renombra el repositorio en GitHub (`indoor-cycling-lab/square-shift` → `smart-bike`, con redirección) y la carpeta local; después, `git remote set-url origin` si hace falta.
