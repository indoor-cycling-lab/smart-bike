# Proposal

## Why

El proyecto nació como un adaptador para los mandos de un Elite Square y se llamó «Square Shift». Ha crecido hasta ser una smartbike: controla el Neo, hace de rodillo ante Zwift, reenvía el pulso, gobierna un ventilador y registra la sesión. El nombre ya no describe lo que hace y deja una marca de Elite en el título de un producto que no es suyo. Cambiarlo ahora es barato: no hay ningún change en curso, la app solo vive en un móvil y su configuración es la de fábrica.

## What Changes

- El producto pasa a llamarse **Smart Bike** (`smart-bike` en el repositorio y en Gradle). «Square» queda solo como el nombre del mando Elite que la app soporta (`SquareClient`, `SquareKey`, `BateriaSquare`, la spec `square-buttons`, «Botones del Square»…).
- El paquete pasa de `jmlopezdona.squareshift` a `app.smartbike`, en `:app` y `:nucleo` y en todos sus conjuntos de código (main, test, androidTest, debug, androidTestDebug). El `applicationId` pasa a `app.smartbike`, y el de depuración a `app.smartbike.debug`.
- **BREAKING:** al cambiar el `applicationId`, Android trata la app como otra distinta. No hereda los datos de la anterior (ajustes, dispositivos recordados, identidad ante Zwift). Hay que desinstalar la versión vieja y volver a emparejar en Zwift.
- **BREAKING:** el nombre con el que la app se anuncia ante Zwift pasa de «Square Shift» a «Smart Bike» (y el del mando por Bluetooth, de «Square Shift Mando» a «Smart Bike Mando»). Zwift lo ve como un dispositivo nuevo.
- Se renombran los identificadores que llevan el nombre: `SquareShiftApp` → `SmartBikeApp`, `Theme.SquareShift` → `Theme.SmartBike`, la acción `…TERMINAR`, el wake lock `SquareShift:zwift` y el archivo de la copia de seguridad (`smart-bike-configuracion-<fecha>.json`).
- Los textos de la UI, las maquetas, la guía de estilo, el README, `AGENTS.md`, `docs/` y las specs vigentes dejan de decir «Square Shift».
- Las herramientas del repositorio apuntan al paquete nuevo: el generador del sistema de diseño (`generar.py`), el hook `adb_guard.py` y la skill `desplegar-movil`.

Queda fuera de alcance:

- El prefijo `Sq` del sistema de diseño (`sq-*`, `SqTema`): se queda tal cual y se documenta que no significa «Square».
- El hardware y todo lo que nombra al Elite Square.
- Los changes archivados (`openspec/changes/archive/`) y las notas históricas de `docs/planificacion.md`: son registro de lo que se hizo con el nombre de entonces.
- Migrar datos de la app anterior.
- Renombrar el repositorio en GitHub y la carpeta local: lo hace el usuario al terminar.
- Cualquier cambio de comportamiento, de pantalla o de diseño. No hay maqueta: solo cambian textos.

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

Cambia el texto de los requisitos que citan el nombre; el comportamiento es el mismo.

- `zwift-bridge`: el rodillo, la fuente de pulso y el modo «Simular un KICKR BIKE» nombran «Smart Bike» como nombre anunciado.
- `zwift-controller`: el mando, solo o dentro del rodillo, se empareja con «Smart Bike».
- `device-connection`: las ayudas de las filas de Zwift y del mando dicen que se elija «Smart Bike».
- `diagnostics`: la fila de Zwift y el nombre anunciado en Diagnóstico dicen «Smart Bike».
- `ride-session`: al terminar la sesión, Zwift pierde la conexión con «Smart Bike».
- `screen-mockups`: los estados de maqueta de Dispositivos y Diagnóstico muestran «Smart Bike».
- `settings-screen`: con «Simular un KICKR BIKE» desactivado, la app se anuncia como «Smart Bike».
- `settings-backup`: los mensajes de error de la importación nombran «Smart Bike».

## Impact

- **Código:** unos 224 ficheros fuera del archivo mencionan el paquete o el nombre. Cambia el árbol de carpetas de `app/` y `nucleo/`, `app/build.gradle.kts`, `settings.gradle.kts`, el manifiesto, `themes.xml`, `strings.xml`, `ZwiftBridge.NOMBRE`, `NombreAnunciado.NORMAL` y los tests que comparan el nombre.
- **Herramientas:** `scripts/design-system/generar.py` (paquete e import de `R`, título de la guía) y los derivados que regenera (`Tokens.kt`, `styleguide/index.html`); `.claude/hooks/adb_guard.py` y `.claude/skills/desplegar-movil/SKILL.md`.
- **Documentos de `docs/`:** cambian `desarrollo.md` (id de la app y comandos `adb`), `arquitectura.md` (nombre anunciado y diagrama), `funcional.md` (la decisión abierta del nombre queda resuelta) y `planificacion.md` (fila del change y nota del cambio de nombre). Además, `README.md`, `AGENTS.md` y `design-system/README.md`.
- **Equipo real:** hay que probar en el OnePlus 8 con la app nueva instalada y la vieja desinstalada, y en Zwift que «Smart Bike» aparece en la pantalla de emparejamiento. Es un cambio de nombre en un anuncio mDNS ya validado, no un comportamiento nuevo de BLE.
- **Dependencias:** ninguna.
