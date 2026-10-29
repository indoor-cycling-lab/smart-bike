# Tasks

## 1. Rotación de las sesiones grabadas

- [x] 1.1 `:core` (`ArchivoSesion`): `aBorrar(nombres, conservar = SESIONES_CONSERVADAS)` con `SESIONES_CONSERVADAS = 20` (design.md, decisión 6). Verificar con su test:
  - 12 nombres no devuelven nada; con 19 tampoco.
  - Con 20 devuelve el más antiguo; con 25, los 6 más antiguos.
  - Ignora el orden de entrada y ordena por la fecha del nombre, incluido el sufijo `-2`.
- [x] 1.2 `GrabadorSesion.iniciar`: tras comprobar el espacio y antes de abrir el archivo nuevo, borrar lo que diga `aBorrar` y anotar cada borrado o fallo en el registro. Verificar con `GrabadorSesionTest`:
  - Con 20 sesiones en la carpeta, al iniciar queda la más antigua borrada y la nueva grabando (20 en total).
  - Con 25, quedan 20.
  - Sin espacio no se borra nada.
  - Si un `delete()` falla, la grabación empieza igual y el registro lo dice.
  - Sin iniciar (ajuste apagado) no se borra nada.
- [x] 1.3 Texto de la introducción de «Sesiones grabadas» con el límite de 20, en `strings.xml` (`sesiones_intro`) y en `maquetas/sesiones.html`. Es un cambio de texto: no necesita validación previa (AGENTS.md). Verificar abriendo la maqueta y, en la tarea 5.3, en la app.

## 2. Copia de seguridad

- [x] 2.1 `res/xml/reglas_copia.xml` (`<cloud-backup>` y `<device-transfer>`) y `res/xml/copia_completa.xml`, los dos con solo `<include domain="file" path="datastore/configuracion.json"/>`. En el manifiesto: `allowBackup="true"`, `dataExtractionRules` y `fullBackupContent` (decisión 5). Verificar con `./gradlew :app:lintRelease` sin avisos de copia de seguridad y con un test que compruebe que las dos reglas incluyen exactamente esa ruta y que la ruta coincide con la de `Almacen`.

## 3. Compilación release

- [x] 3.1 Versión desde `-PetiquetaVersion` en `app/build.gradle.kts` (decisión 3): `versionName` y `versionCode` calculados; `0.0.0-dev` y 1 sin la propiedad; error claro si está mal formada. Verificar:
  - `./gradlew :app:assembleDebug -PetiquetaVersion=v1.2.3` y `aapt2 dump badging` del APK dan `versionName='1.2.3'` y `versionCode='1002003'`.
  - Sin la propiedad, dan `0.0.0-dev` y `1`.
  - Con `-PetiquetaVersion=v1.2`, la compilación falla con el formato esperado.
- [x] 3.2 Firma opcional desde propiedades o variables de entorno (decisión 2): `signingConfig` `release` solo con las cuatro claves, v2 y v3, y aviso si faltan. Verificar que `./gradlew :app:assembleRelease` sin propiedades genera `app-release-unsigned.apk` con el aviso, y que `:app:lintRelease` y los tests siguen en verde.
- [x] 3.3 R8 y `shrinkResources` con `proguard-android-optimize.txt` y `app/proguard-rules.pro` (`-dontobfuscate`) (decisión 1). Verificar que `./gradlew :app:assembleRelease` compila sin avisos de R8 sin resolver, que el APK es más pequeño que antes (anotar los dos tamaños) y que no contiene `ic_expand_more` ni `ic_landscape` (`aapt2 dump resources`).

## 4. Workflow de publicación

- [x] 4.1 `.github/workflows/release.yml` según la decisión 4: etiqueta `v*`, entorno `release`, validación de la etiqueta y de que está en `main`, comprobación de los secrets, compilación con las comprobaciones, verificación de la huella con `HUELLA_CERTIFICADO`, `gh release create` con el APK, su SHA-256 y la huella, y borrado del `.jks` con `if: always()`. Acciones fijadas por versión exacta, como en `pages.yml`. Verificar:
  - Con `actionlint`, si está disponible.
  - Revisando que ningún paso imprime las contraseñas ni el `.jks`.
  - En la tarea 6.4 con una etiqueta real.
- [x] 4.2 `docs/desarrollo.md`, sección «Publicar una versión»:
  - Crear la clave con `keytool` (RSA 4096, PKCS12, 10 000 días).
  - Guardarla en un gestor de contraseñas con el `.jks` adjunto y las dos contraseñas, y por qué un secret de GitHub no sirve de copia.
  - Obtener la huella.
  - Crear el entorno `release` limitado a etiquetas `v*`.
  - Dar de alta los cuatro secrets (`base64 -i … | gh secret set … --env release`, sin poner las contraseñas en la línea de comandos) y la variable `HUELLA_CERTIFICADO`.
  - Configurar la firma local en `~/.gradle/gradle.properties`.
  - Publicar con `git tag vX.Y.Z && git push origin vX.Y.Z`.
  - Deshacer una Release.
  - Volver a dar de alta todo en el repositorio público nuevo.

  Verificar leyendo la sección de principio a fin contra el workflow y el `build.gradle.kts`.

## 5. Clave y prueba de la release en local (con el usuario)

- [x] 5.1 El usuario crea la clave y la guarda en el gestor de contraseñas siguiendo `docs/desarrollo.md`; el agente le da los comandos y no ve las contraseñas. Verificar que el usuario confirma que tiene la entrada con el `.jks` y las dos contraseñas, y que `keytool -list -v` muestra la huella que luego irá en `HUELLA_CERTIFICADO`.
- [x] 5.2 El usuario añade las cuatro propiedades a `~/.gradle/gradle.properties`. Verificar que `./gradlew :app:assembleRelease` genera `app-release.apk` firmado y que `apksigner verify --print-certs` da la huella de 5.1.
- [x] 5.3 Instalar la release en el OnePlus 8 (`./gradlew :app:installRelease`, junto a la de depuración, con otro identificador) y probar con el equipo real (spec `app-distribution`, «La versión publicada funciona igual que la de desarrollo»):
  - Configurar el Neo y el Square, y rodar en Zwift por wifi y por Bluetooth con cambios de marcha.
  - Exportar la configuración en la release e importarla en la de depuración.
  - Activar «Grabar sesiones», rodar y revisar la cabecera y las líneas de error del archivo (legibles, sin nombres ofuscados).
  - Comprobar la introducción de «Sesiones grabadas».

  Si algo falla por R8, añadir la regla con su motivo y repetir.
- [x] 5.4 Copia de seguridad en el OnePlus 8 con `bmgr`, que el usuario lanza con `!` porque el hook no lo permite. Con la release instalada, la configuración cambiada, el Neo configurado y alguna sesión grabada, hacer `bmgr backupnow app.smartbike`, desinstalar, reinstalar y `bmgr restore`. Comprobar (spec `settings-backup`, «Copia de seguridad del sistema»):
  - Vuelve la configuración.
  - No vuelven los dispositivos, las sesiones ni el nombre Bluetooth original.
  - La app abre sin errores.

## 6. Primera publicación (con el usuario)

- [x] 6.1 El usuario da de alta los cuatro secrets del repositorio y la variable `HUELLA_CERTIFICADO` siguiendo `docs/desarrollo.md`. Mientras el repositorio sea privado, no hay entorno `release` (design.md, decisión 4). Verificar con `gh secret list` y `gh variable list`: aparecen los nombres, sin los valores.
- [x] 6.2 Etiqueta mal formada: con el permiso del usuario, empujar `v0.0` y comprobar que el workflow falla en la validación y no crea ninguna Release; borrar la etiqueta después.
- [x] 6.3 Etiqueta fuera de `main`: con el permiso del usuario, empujar `v0.0.1` sobre un commit de una rama de prueba. El workflow falla en la comprobación de `main` y no crea Release. Borrar la etiqueta y la rama.
- [x] 6.4 Primera versión: con el permiso del usuario, `git tag v0.1.0 && git push origin v0.1.0`. Verificar:
  - La Release `v0.1.0` tiene `smart-bike-0.1.0.apk`, su SHA-256 y la huella.
  - El SHA-256 del APK descargado coincide con el publicado.
  - Instalado en el OnePlus 8 sobre la release local de 5.3 (que tiene versión `0.0.0-dev`, código 1), se instala como actualización conservando la configuración, y Información de la app dice `0.1.0`.

## 7. Documentación

- [x] 7.1 Actualizar la documentación:
  - `docs/funcional.md`: F13.4, con el límite de 20 sesiones.
  - `docs/arquitectura.md`: la compilación release (R8 sin ofuscar y firma desde el entorno), la copia de seguridad y la rotación de sesiones.
  - `docs/planificacion.md`: fila 33 y su nota, con la dependencia de `publicar-repo-publico` para volver a dar de alta los secrets.
  - `docs/desarrollo.md`: lo de la tarea 4.2 y lo aprendido en la sección 5.
  - `docs/revision/2026-10-29-auditoria-produccion.md`: A7, A8 y M7 hechos, y quitar las filas de `ic_expand_more` e `ic_landscape` del código muerto.
