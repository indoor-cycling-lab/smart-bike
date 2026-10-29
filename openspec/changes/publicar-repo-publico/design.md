# Design

## Context

Ver `proposal.md` para el motivo. Estado medido en la revisión de privacidad del 27-10-2026:

- 86 commits, todos con el mismo nombre y correo personales de autor y comitter, y 28 con un enlace `Claude-Session` en el mensaje. Hay también la rama remota `simular-kickr-bike` y un `stash` local.
- Sin credenciales, claves, tokens, `.env`, keystores, rutas del Mac ni nombres de wifi, ni en el árbol ni en el historial. `local.properties`, `docs/referencias/` y el fichero con la IP del móvil de pruebas están ignorados y nunca se subieron.
- Datos personales en el árbol actual:
  - **Identificadores de dispositivos reales**, en las capturas `core/src/test/resources/trazas/` (tres ficheros), en `SesionTest`, `BusquedaAnunciosTest`, `DispositivosUiStateTest` y `DispositivosTest`, en `ConexionFtmsTest` (una dirección Bluetooth real, dos veces), en la maqueta `maquetas/dispositivos.html`, en `NeoClient.kt` (un comentario), en `docs/arquitectura.md`, en la fila 20 de `docs/planificacion.md` y en A9 de `docs/revision/2026-10-29-auditoria-produccion.md` (hasta el 29-10-2026; ya sustituida) y en tres diseños archivados (`diagnostico-square-neo`, `conexion-automatica-en-segundo-plano` y `puente-zwift-wifi`): las direcciones Bluetooth del rodillo y del mando, la identidad que la app generó ante Zwift y el nombre BLE del rodillo, que incluye parte de su número de serie.
  - **Direcciones IP de la red local** (del móvil y del PC de Zwift) en 17 ficheros: tests, maquetas, `openspec/specs/diagnostics` y diseños archivados.
  - **Detalles del equipo:** el sufijo del nombre real del ventilador en `docs/arquitectura.md` y `docs/planificacion.md`, y en `docs/desarrollo.md` el modelo del móvil y la lista de apps que tiene instaladas, con el nombre de paquete de una de ellas.
- El inventario del 29-10-2026 (tarea 1.1) amplía la lista anterior:
  - **Número de serie del rodillo dentro de las tramas FE-C:** la página 81 (información del producto) de las capturas `neo-erg-200w.txt` y `neo-pendiente-xor.txt` lleva el número de serie real, el mismo que forma el sufijo del nombre BLE. Se sustituye por el del nombre de ejemplo, recalculando la suma de comprobación de la trama.
  - **Nombre BLE del rodillo** también en `openspec/specs/device-connection` y `openspec/specs/screen-mockups`, en las specs delta archivadas de `pestana-conexion`, `renombrar-controlador` y `simplificar-diagnostico-conexiones`, en el diccionario de las maquetas (`maquetas/en.json` y el generado `maquetas/en.js`) y en A9 de `docs/revision/2026-10-29-auditoria-produccion.md`, que lo cita al describir el hallazgo.
  - **Sufijo real del ventilador** también en el diseño archivado de `ventilador-headwind`.
  - **Direcciones IP** también en `PuentePlataformaTest`, `ConexionUiStateTest`, `maquetas/en.json` y `en.js`, `openspec/specs/screen-mockups` y las specs delta y tareas archivadas de `puente-zwift-wifi`, `pestana-conexion` y `simplificar-diagnostico-conexiones`. Solo dos IPs son reales (la del móvil y la del PC de Zwift); las demás de los tests (`192.168.1.10`, `.20`, `.35`, `10.0.0.x`) son inventadas y se conservan.
  - **Capturas de la web** (`site/img/dispositivos-*` y `diagnostico-*`): se generan de las maquetas y muestran el nombre del rodillo y la IP. Se regeneran con `scripts/sitio/capturas.py` tras sanear las maquetas.
  - Sin correos, rutas `/Users/` (solo el marcador `<usuario>` de `docs/desarrollo.md` y el patrón de `test_sitio.py`) ni credenciales: las contraseñas de firma solo aparecen como nombres de secrets y variables.
- Las constantes de identidad que el propio proyecto define para el mando (`MandoZwift.IDENTIDAD`) y las direcciones inventadas de los tests (`AA:AA`, `AA:BB:CC:DD:EE:…`, `11:22:33:…`, `02:aa:bb:…`, `02:00:00:…`, `02:11:22:…`) no son datos personales y se conservan. Tampoco la dirección de ejemplo de `simular-kickr-bike` (`50:50:25:…`, con el nombre `KICKR BIKE PRO 669C` que deriva de ella), ni el nombre del ventilador de los tests (`HEADWIND 4F2A`, inventado).

**Los valores reales no se escriben en ningún artefacto de este change** ni en ningún fichero del repositorio nuevo: este change se publicará con el resto. Las tareas se refieren a ellos por tipo y por fichero, y los patrones de búsqueda son genéricos (formato de dirección MAC, IPv4 privada, correo). Los valores literales reales solo se usan desde la terminal al ejecutar el escaneo, sin guardarlos.

## Goals / Non-Goals

**Goals:**

- Que el repositorio público no contenga datos personales ni de red del usuario, ni en el árbol ni en el historial.
- Que el saneado no cambie el comportamiento: los tests siguen pasando con los mismos asertos sobre valores de ejemplo.
- Que lo publicado se compruebe, no se presuma: escaneo y compilación desde un clon limpio.

**Non-Goals:**

- Ocultar la autoría: el nombre figura en la licencia y en el README, y se mantiene.
- Mantener el historial de commits en el repositorio público.

## Decisions

### 1. Repositorio nuevo con un único commit, no limpiar el existente

Los datos están repartidos por decenas de commits, y reescribirlos exige `git filter-repo`, `push --force` y confiar en que GitHub deje de servir los commits antiguos: aun tras un force push, GitHub conserva los objetos antiguos accesibles por su hash (y por referencias de pull requests o forks) hasta una recolección que el usuario no controla. Un repositorio nuevo nace sin ninguno de esos objetos.

Alternativas descartadas:

- **Hacer público el repositorio actual tal cual:** publica todo el historial con los datos personales.
- **`git filter-repo` y force push sobre el repositorio actual:** conserva los commits, pero cambia todos los hashes y deja el riesgo anterior.
- **Rama huérfana dentro del mismo repositorio:** el historial viejo sigue en el repositorio y sería visible en cuanto se publique.

Contrapartida: se pierde el historial de commits en público. El historial de decisiones sigue disponible en `openspec/changes/archive/`.

### 2. Política de sustitución: valores de ejemplo evidentes

| Dato | Sustituto |
|---|---|
| Direcciones Bluetooth reales (rodillo, mando) | Direcciones de ejemplo reconocibles, distintas para cada dispositivo (por ejemplo `AA:BB:CC:00:00:01` y `AA:BB:CC:00:00:02`) |
| Identidad generada ante Zwift (en docs archivados) | Una dirección de ejemplo con el mismo formato |
| Nombre BLE del rodillo (`Tacx Neo` más su sufijo real) | `Tacx Neo 12345` |
| Número de serie del rodillo en la página 81 de FE-C | 12345, el del nombre de ejemplo, con la suma de comprobación recalculada |
| Sufijo real del nombre del ventilador | `HEADWIND 1234` |
| IPs de la red local | `192.168.0.10` y `192.168.0.11` (rango privado genérico) |
| Lista de apps del móvil y paquete de una de ellas | Texto genérico: «otras apps de entrenamiento del móvil» |

Se sustituye cada valor por el mismo valor de ejemplo en todos los sitios, para que capturas, tests y documentos sigan siendo coherentes entre sí. Se mantienen el modelo del móvil de pruebas y los modelos de la banda y del ventilador, porque informan de compatibilidad y no identifican a nadie.

Los diseños archivados solo se editan en los identificadores concretos de la tabla; no se reescribe nada más.

### 3. Árbol exacto con `git archive`, sin copiar la carpeta

El commit inicial se construye con `git archive HEAD` sobre una carpeta nueva y `git init`: solo entran los ficheros rastreados, y no los ignorados (`build/`, `local.properties`, archivos locales). Copiar la carpeta de trabajo podría arrastrar ficheros sin rastrear.

### 4. Autor y mensaje del commit inicial

El commit inicial usa el correo `noreply` de GitHub (con el formato `<id>+<usuario>@users.noreply.github.com`) y el nombre que el usuario decida, sin trailers ni enlaces a sesiones. Se activa en GitHub «Keep my email addresses private» y «Block command line pushes that expose my email», para que los commits siguientes tampoco expongan el correo. Los commits que haga después el agente en el repositorio público pueden incluir o no la atribución; se decide con el usuario al aplicar.

### 5. Comprobación con patrones genéricos, sin guardar los valores

Se ejecutan estos escaneos sobre el árbol saneado y, después, sobre el historial de un único commit del repositorio nuevo:

- Direcciones MAC (`([0-9A-Fa-f]{2}:){5}[0-9A-Fa-f]{2}`), revisando a mano que solo queden las de ejemplo y las inventadas de los tests.
- IPv4 privadas (`192.168.`, `10.`, `172.16–31.`), dejando solo las de ejemplo.
- Correos, `/Users/` y palabras de credenciales (`password`, `token`, `api_key`, claves PEM).
- Los valores reales concretos, tecleados en la terminal a partir de lo que ya se sabe, sin escribirlos en un fichero ni en un artefacto.

No se añade un script al repositorio: tendría que contener los valores a buscar o patrones demasiado genéricos, y el repositorio no necesita mantener esa comprobación.

### 6. El repositorio público pasa a ser el de trabajo

El repositorio privado actual se renombra a un nombre distinto (por ejemplo `smart-bike-historial`) y se mantiene privado como copia de seguridad, con su historial íntegro. El público toma el nombre `smart-bike`, que es el que ya figura en el README. El trabajo continúa en un clon del repositorio público; la carpeta antigua queda como copia local. Este `change` se archiva en el repositorio público, no en el privado: las tareas posteriores a la publicación se hacen allí.

### 7. Orden de las acciones fuera del repositorio

Renombrar el repositorio actual, crear el público y subir el commit son acciones visibles fuera de la máquina y no se deshacen del todo (lo publicado puede quedar en cachés). Cada una se confirma con el usuario justo antes de ejecutarla, y se hace primero el escaneo del repositorio local nuevo, sin subir nada.

## Risks / Trade-offs

- **[Un dato personal se escapa por una variante que el escaneo no cubre]** → Se escanea con patrones genéricos y con los valores reales, se revisa a mano cada coincidencia, y se mira el árbol final del repositorio local antes de subir nada. El repositorio de un solo commit se puede rehacer en minutos si aparece algo.
- **[La sustitución rompe un test o una captura]** → Cada valor se cambia igual en todos los sitios y se pasan `:core:test`, `:app:testDebugUnitTest` y la compilación de los `androidTest`; si falla alguno, se ajusta el valor de ejemplo.
- **[Publicado por error con datos]** → No se sube nada hasta comprobar el repositorio local; si algo se publica con datos, se borra el repositorio público y se repite el proceso con el saneado corregido (otra ventaja del commit único: no hay historial que limpiar).
- **[Se pierde el historial de commits en público]** → Se conserva íntegro en la copia privada y las decisiones, en OpenSpec.
- **[Los datos siguen en el repositorio privado]** → Es privado y es la copia de seguridad; solo debe pasar a público tras sanearlo.
- **[La carpeta local nueva rompe el contexto de Claude Code]** → Su historial y memoria dependen de la ruta; se asume al cambiar de carpeta (lo mismo que ya se avisó al renombrar el proyecto).

## Migration Plan

1. Sanear el árbol actual (código, tests, capturas, documentos), con los tests en verde y el escaneo limpio. Commit en el repositorio privado, a petición del usuario.
2. Crear el repositorio local nuevo con `git archive` y un único commit, y repetir escaneo y pruebas desde esa carpeta. No se sube nada todavía.
3. Con la confirmación expresa del usuario: renombrar el repositorio privado, crear el público `smart-bike`, subir el commit y fijar descripción y etiquetas.
4. Clonar el público en una carpeta limpia y comprobar el escaneo y la compilación desde ese clon.
5. Continuar el trabajo en el clon público: marcar el change como hecho, archivarlo y actualizar `docs/planificacion.md` allí.

Marcha atrás: antes del paso 3 no hay nada publicado y basta con borrar la carpeta nueva. Después del paso 3, se borra el repositorio público y se repite desde el 2; el privado no se ha tocado salvo su nombre.

## Open Questions

- El nombre del autor del commit inicial (el mismo de `LICENSE`, u otro): lo decide el usuario al aplicar; no cambia el plan.
