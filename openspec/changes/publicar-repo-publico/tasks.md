# Tasks

No hay sección «Maqueta»: no hay impacto visual (los cambios de `maquetas/` son valores de ejemplo en textos). Los valores reales no se escriben en ningún fichero ni mensaje de commit: se tratan por tipo y fichero (design.md, «Context»). Antes de empezar, el árbol de Git debe estar limpio, con el `README.md` y el `LICENSE` ya comiteados.

## 1. Inventario

- [x] 1.1 Listar con `git grep` los ficheros rastreados con direcciones MAC, IPv4 privadas, correos y rutas `/Users/`, y revisar cada coincidencia, para confirmar que la lista de design.md («Context») está completa y que no hay otros datos. Verificar: la lista de ficheros por tipo coincide con la de design.md o la actualiza si falta alguno.

## 2. Saneado del código y de los tests

- [x] 2.1 Sustituir las direcciones Bluetooth reales, el nombre BLE real del rodillo y su número de serie en las tramas FE-C de la página 81 (recalculando la suma de comprobación) por los valores de ejemplo de design.md (decisión 2) en las capturas de `core/src/test/resources/trazas/`, siguiendo el mismo valor en cada fichero. Verificar: `./gradlew :core:test` en verde y las capturas siguen analizándose igual (mismos asertos).
- [x] 2.2 Sustituir el nombre BLE real del rodillo en `SesionTest`, `BusquedaAnunciosTest`, `DispositivosUiStateTest`, `DispositivosTest` y en el comentario de `NeoClient.kt`, la dirección Bluetooth real de `ConexionFtmsTest` por una de ejemplo, y las direcciones IP reales de la red local por las de ejemplo en tests y `androidTest` (`SesionTest`, `RodajeUiStateTest`, `DiagnosticoTest`, `DiagnosticoUiStateTest`, `DispositivosTest`, `DispositivosUiStateTest`, `TextoConexionesTest`, `ConexionZwiftRideTest`, `PuentePlataformaTest`, `ConexionUiStateTest`). Verificar: `./gradlew :core:test :app:testDebugUnitTest :app:compileDebugAndroidTestKotlin` en verde.
- [x] 2.3 Ejecutar los tests de interfaz que cambian en el móvil (`DispositivosTest` y `DiagnosticoTest`) con la skill `desplegar-movil`, mirando el informe de `app/build/reports/androidTests/connected/debug/index.html` (el código de salida no es fiable, `docs/desarrollo.md`); después, `./gradlew installDebug` y avisar al usuario de que se pierde el emparejamiento de Zwift. Verificar: el informe da 0 fallos en las dos clases.

## 3. Saneado de la documentación y las maquetas

- [x] 3.1 Sustituir en `maquetas/dispositivos.html`, `maquetas/diagnostico.html` y el diccionario `maquetas/en.json` (regenerando `en.js` con `python3 scripts/sitio/diccionario.py`) el nombre del rodillo y las IPs por los valores de ejemplo, y en `openspec/specs/diagnostics`, `device-connection` y `screen-mockups` los mismos valores. Regenerar las capturas de la web con `python3 scripts/sitio/capturas.py`. Verificar: abrir `maquetas/dispositivos.html` y `maquetas/diagnostico.html` con `python3 -m http.server 8000` y ver los valores de ejemplo en los estados con rodillo y Zwift, y que solo cambian las capturas `site/img/dispositivos-*` y `diagnostico-*`.
- [x] 3.2 En `docs/arquitectura.md`, sustituir el nombre real del rodillo y el sufijo real del ventilador por los valores de ejemplo, en `docs/planificacion.md` el sufijo del ventilador y en `docs/revision/2026-10-29-auditoria-produccion.md` (A9) el nombre del rodillo. Verificar: `git grep` de esos dos valores no devuelve nada en `docs/`.
- [x] 3.3 En `docs/desarrollo.md`, sustituir la lista de apps instaladas en el móvil y el nombre de paquete de una de ellas por un texto genérico («otras apps de entrenamiento del móvil»), y revisar el resto de comandos y ejemplos del documento por datos propios. Verificar: el párrafo del Neo y el del ventilador siguen explicando qué hacer sin nombrar apps ni paquetes concretos.
- [x] 3.4 En los changes archivados (`diagnostico-square-neo`, `conexion-automatica-en-segundo-plano`, `puente-zwift-wifi`, `ventilador-headwind`, `pestana-conexion`, `renombrar-controlador`, `simplificar-diagnostico-conexiones` y los que repitan IPs: `comprobar-anuncio-zwift`, `mando-zwift-emulado`, `renombrar-smart-bike`), sustituir solo los identificadores reales por los de ejemplo, sin tocar nada más. Verificar: `git diff --stat -- openspec/changes/archive` muestra cambios de pocas líneas por fichero y `git diff` no toca nada que no sea un identificador.

## 4. Verificación del árbol saneado

- [x] 4.1 Ejecutar el escaneo de design.md (decisión 5) sobre el árbol: patrones genéricos y los valores reales tecleados en la terminal, sin guardarlos. Verificar: solo quedan las direcciones de ejemplo y las inventadas de los tests (`AA:AA`, `02:11:22:…`, `MandoZwift.IDENTIDAD`), y ninguna coincidencia con los valores reales.
- [x] 4.2 Ejecutar `./gradlew :core:test :app:testDebugUnitTest :app:compileDebugAndroidTestKotlin` y `python3 scripts/design-system/generar.py`. Verificar: todo en verde y `git status` sin cambios tras el generador.
- [x] 4.3 Pedir al usuario que haga el commit del saneado en el repositorio privado (no se hace commit sin que lo pida). Verificar: `git log -1 --stat` solo muestra los ficheros de los grupos 2 y 3.

## 5. Repositorio nuevo, con un único commit (sin publicar)

- [ ] 5.1 Pedir al usuario el nombre y el correo `noreply` de GitHub para el commit inicial (design.md, decisión 4) y que confirme que tiene activados «Keep my email addresses private» y «Block command line pushes that expose my email». Verificar: el usuario confirma los dos datos y los dos ajustes.
- [ ] 5.2 Crear una carpeta nueva fuera del repositorio con `git archive HEAD`, ejecutar `git init -b main`, configurar en ella el nombre y el correo del paso anterior y hacer un único commit con un mensaje en español, sin trailers ni enlaces a sesiones. Verificar: `git log --format='%an <%ae> %s'` muestra un solo commit con el correo `noreply`, y `git ls-files | wc -l` coincide con `git ls-files | wc -l` del repositorio privado.
- [ ] 5.3 En la carpeta nueva, repetir el escaneo (tarea 4.1) sobre el árbol y sobre `git log --all -p`, y ejecutar `./gradlew :core:test :app:testDebugUnitTest` desde ahí, con su propio `local.properties`. Verificar: el escaneo no encuentra datos personales y los tests pasan desde un árbol limpio, sin depender de ficheros ignorados.

## 6. Publicación

- [ ] 6.1 Con confirmación expresa del usuario, renombrar el repositorio privado actual a un nombre de copia (por ejemplo `smart-bike-historial`), dejándolo privado. Verificar: `gh repo view` muestra el nuevo nombre y `isPrivate: true`.
- [ ] 6.2 Con confirmación expresa del usuario, crear el repositorio público `indoor-cycling-lab/smart-bike` (`gh repo create … --public`), añadir el remoto y subir `main` desde la carpeta nueva; fijar descripción y etiquetas. Verificar: `gh repo view` muestra `isPrivate: false`, un solo commit en `main` y la descripción.
- [ ] 6.3 Clonar el repositorio público en una carpeta temporal limpia, repetir el escaneo sobre ese clon y compilar y pasar los tests unitarios. Verificar: escaneo limpio y `BUILD SUCCESSFUL`.
- [ ] 6.4 Confirmar con el usuario que no queden forks, copias ni referencias al repositorio privado en la organización, y que el repositorio de trabajo pasa a ser el clon público. Verificar: confirmación del usuario y `git remote -v` del clon de trabajo apuntando al público.
- [ ] 6.5 En el repositorio público (`docs/desarrollo.md` § 7): el usuario crea el entorno `release` con *Deployment branches and tags* limitado a etiquetas `v*` y da de alta en él los secrets de firma (`gh secret set … --env release`) y la variable `HUELLA_CERTIFICADO`; en el clon de trabajo se añade `environment: release` al job de `.github/workflows/release.yml`. Verificar: `gh secret list --env release` muestra los cuatro secrets, `gh variable list` muestra `HUELLA_CERTIFICADO` y `release.yml` declara el entorno.

## 7. Documentación (en el repositorio público)

- [ ] 7.1 En el clon público, actualizar la documentación: revisar `docs/funcional.md` (ninguno), `docs/arquitectura.md` (ninguno más que lo ya sustituido), `docs/planificacion.md` (fila del change y una nota de que el historial anterior a la publicación vive en una copia privada y de que los valores de las notas históricas son de ejemplo) y `docs/desarrollo.md` (repositorio de trabajo y carpeta). Verificar: `git grep -n -i 'privad' -- docs` muestra solo la nota nueva y la mención al móvil personal del agente.
