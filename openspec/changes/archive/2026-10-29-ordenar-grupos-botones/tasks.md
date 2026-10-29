# Tasks

## 1. Maqueta

- [x] 1.1 En `maquetas/botones.html`, mover la sección «Funciones de Zwift» después de «Acciones de la app» y antes de «Gestos». Verificar con `grep -n sq-section__title maquetas/botones.html` que el orden es Acciones de la app, Funciones de Zwift, Gestos, y que los estados `lista`, `sin-square`, `modificada`, `shimano` y `restaurar` conservan sus filas y avisos (se miran en `http://localhost:8000/maquetas/botones.html#estado=<estado>`).
- [x] 1.2 Comprobar `maquetas/index.html`: ningún enlace ni descripción habla del orden anterior; si lo hace, corregirlo.
- [x] 1.3 Validación de la maqueta por el usuario: dar los enlaces exactos (`maquetas/botones.html`, `maquetas/botones.html#estado=sin-square` y `maquetas/botones.html#estado=shimano`) y esperar la aprobación explícita antes de empezar la sección «App».

## 2. App

- [x] 2.1 En `ui/botones/Botones.kt`, llamar a `AccionesApp` antes que a `FuncionesZwift` en la columna de `Botones`. Verificar con `./gradlew :app:compileDebugKotlin`.
- [x] 2.2 Añadir en `BotonesTest` (androidTest) el caso `grupos_en_orden`: la posición vertical de «Acciones de la app» es menor que la de «Funciones de Zwift» y esta es menor que la de «Gestos». Verificar con `./gradlew :app:connectedDebugAndroidTest` o, si no hay dispositivo, con la revisión en el OnePlus 8 de la tarea 2.4.
- [x] 2.3 Ejecutar `./gradlew :app:testDebugUnitTest ktlintCheck` y verificar que pasan sin avisos nuevos.
- [x] 2.4 Desplegar con la skill `desplegar-movil` y verificar en el OnePlus 8 que Ajustes → Botones muestra «Acciones de la app» primero, con «+1 marcha» y «−1 marcha» visibles sin desplazarse, y que tocar una fila de cada grupo abre Asignar acción.

## 3. Web pública

- [x] 3.1 En `site/capturas.json`, cambiar el texto alternativo de la captura `botones` en `es` y `en` para que describa las acciones de la app asignadas a los botones del Square (y no solo las funciones de Zwift). Verificar que `es` y `en` siguen teniendo las mismas claves.
- [x] 3.2 Regenerar con `python3 scripts/sitio/generar.py` y `python3 scripts/sitio/capturas.py` y verificar que `site/img/` muestra la captura de Botones con el nuevo orden.

## 4. Documentación

- [x] 4.1 Actualizar la documentación: fila del change en `docs/planificacion.md` (puesta «En curso» al crearlo; marcarla «Archivado» al archivar) y revisión de `docs/funcional.md`, `docs/arquitectura.md` y `docs/desarrollo.md`; corregir cualquier mención del orden anterior de los grupos. Verificar con `grep -rn "Funciones de Zwift" docs/*.md` que ninguna línea afirma el orden antiguo.
