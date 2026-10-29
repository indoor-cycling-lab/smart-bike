# Design

## Context

Lo que hay hoy:

- **Compilación.** `app/build.gradle.kts` deja la release con `isMinifyEnabled = false` y sin firma. `versionCode = 1` y `versionName = "0.1.0"` están fijos. La versión se lee en `AppGraph` (`BuildConfig.VERSION_NAME`) para la cabecera de las sesiones grabadas.
- **Datos de la app**, en `files/`:
  - `datastore/configuracion.json`: lo mismo que exporta «Exportar configuración».
  - `datastore/estado.json` (`EstadoGuardado`): los dispositivos configurados, las identidades ante la plataforma, las últimas marchas, la última potencia del ERG, `notificacionesPedidas`, `nombreBluetoothOriginal` y `nombreBluetoothPuesto`.
  - `sesiones/`: las sesiones grabadas.
- **Copia de seguridad.** El manifiesto no declara `allowBackup` ni reglas de copia, así que Android copia todo.
- **Grabación.** `GrabadorSesion.iniciar` cierra la grabación anterior, comprueba el espacio y abre `sesion-AAAAMMDD-HHMMSS[-n].txt`. `RepositorioSesiones` lista las sesiones (por nombre descendente y, a igualdad, por fecha de modificación) y las borra, respetando la que se graba.
- **Reflexión.** No se usa en el código: no hay `Class.forName` ni `getMethod`. La serialización es kotlinx.serialization con el plugin del compilador. Nordic 2.11 no usa reflexión en lo que llama la app.
- **CI.** Solo hay `.github/workflows/pages.yml`, con acciones fijadas por versión exacta.
- **Hook de adb.** Permite instalar `app.smartbike` (release) y `app.smartbike.debug`; no permite `bmgr`.

## Goals / Non-Goals

**Goals:**

- Un APK release reproducible desde una etiqueta, firmado siempre con la misma clave, que nunca se publica sin firmar ni con otra clave.
- La clave fuera del repositorio y del registro de las ejecuciones; el agente nunca ve las contraseñas.
- Trazas de error legibles también en la release.

**Non-Goals:**

- Compilaciones reproducibles bit a bit.
- Firma con varios certificados o rotación de clave (APK Signature Scheme v3 lo permitiría más adelante).
- AAB para Google Play.

## Decisions

### 1. R8 con optimización y reducción, sin ofuscar

La release activa `isMinifyEnabled = true` e `isShrinkResources = true` con `proguard-android-optimize.txt` y un `app/proguard-rules.pro` que lleva `-dontobfuscate`.

- **Por qué sin ofuscar.** El repositorio será público con licencia GPL-3.0, así que ofuscar no protege nada. Las excepciones que acaban en el registro de sesión y en las sesiones grabadas (`"… ($e)"`, `e.javaClass.simpleName`) seguirían siendo legibles sin necesitar el `mapping.txt` de cada versión. Reducir y optimizar se conserva: es lo que baja el tamaño y quita el código muerto.
- **Reglas.**
  - kotlinx.serialization 1.12 trae sus reglas dentro de la librería. Con `-dontobfuscate` no hacen falta reglas propias mientras no se obtengan serializadores por reflexión.
  - Nordic no las necesita.
  - `proguard-rules.pro` empieza solo con `-dontobfuscate`, y cada regla que se añada lleva un comentario con el fallo que corrige.
- **Recursos.** `shrinkResources` quita `ic_expand_more` e `ic_landscape`, que se generan solo para la web y la guía. `IconosSincronizadosTest` compara las fuentes, así que no le afecta.
- **Alternativas.**
  - Ofuscar y subir `mapping.txt` como artefacto: obliga a desofuscar cada registro que mande alguien, sin ganar nada en un proyecto abierto.
  - Dejar R8 apagado: APK más grande y código muerto (la auditoría lo marca como A7).

### 2. Firma desde propiedades o variables de entorno, opcional en local

Se crea un `signingConfig` `release` solo si están las cuatro claves de configuración: `RELEASE_KEYSTORE` (ruta del `.jks`), `RELEASE_KEYSTORE_PASSWORD`, `RELEASE_KEY_ALIAS` y `RELEASE_KEY_PASSWORD`.

- **Origen de los valores.** Cada una se lee con `providers.gradleProperty(…)` y, si no está, con `providers.environmentVariable(…)`. En el Mac van en `~/.gradle/gradle.properties`, fuera del repositorio; en CI, en variables de entorno.
- **Sin las cuatro claves.** La release se compila sin firmar (`app-release-unsigned.apk`) y Gradle avisa con un `logger.warn`. Así `lintRelease`, los tests y la compilación sin clave siguen funcionando.
- **Esquemas de firma.** Solo v2 y v3 (`enableV1Signing = false`), porque `minSdk` es 28.
- **La clave.** RSA de 4096 bits, PKCS12 y validez de 10 000 días. La crea el usuario con `keytool`, con las contraseñas tecleadas por él.
- **Alternativas.**
  - `keystore.properties` en la raíz del repositorio, ignorado por git: es fácil subirlo por error.
  - Fallar sin clave: rompería `lintRelease` y los tests en cualquier clon.

### 3. Versión desde la etiqueta: `-PetiquetaVersion=vX.Y.Z`

`build.gradle.kts` lee la propiedad `etiquetaVersion` y la valida con `^v(\d{1,3})\.(\d{1,3})\.(\d{1,3})$`.

- **Cálculo.** `versionName = "X.Y.Z"` y `versionCode = X·1 000 000 + Y·1 000 + Z`. Es monótono si cada componente es menor que 1000, y cabe en el máximo de Android (2 100 000 000) hasta la versión 2099.
- **Sin la propiedad.** `versionName = "0.0.0-dev"` y `versionCode = 1`. Una release compilada en local nunca se instala encima de una publicada, porque sería una versión anterior: es lo esperado.
- **Con una propiedad mal formada.** La compilación falla con el formato esperado.
- **Alternativas.**
  - `git describe` desde Gradle: depende del historial (`fetch-depth`) y complica la caché de configuración.
  - Fijar la versión a mano en el archivo: se olvida y la release no se instala como actualización.

### 4. Workflow `release.yml`: etiqueta en `main`, comprobaciones, firma y Release

**Disparo y permisos:**
- Disparo: `on: push: tags: ["v*"]`.
- Permisos: `contents: write`, el mínimo para crear la Release.
- Concurrencia por etiqueta.
- El job usa el entorno `release` de GitHub, con la regla de despliegue «solo etiquetas `v*`». Los secrets van en ese entorno, no en el repositorio: un workflow lanzado desde otra rama o etiqueta no los recibe.
- **Mientras el repositorio sea privado**, no: la organización está en el plan gratuito, que no ofrece entornos con secrets ni reglas en repositorios privados (comprobado al aplicar el change). Hasta `publicar-repo-publico`, los secrets son del repositorio y el job no declara entorno. Solo escribe en él el usuario, así que el entorno apenas añadiría protección. En el repositorio público se crea el entorno, los secrets pasan a él y el job vuelve a llevar `environment: release`.

**Pasos:**
1. `actions/checkout@v4.2.2` con `fetch-depth: 0`.
2. Validar la etiqueta con la misma expresión de la decisión 3. Si falla, el error dice qué forma debe tener.
3. Comprobar que la etiqueta está en `main` con `git merge-base --is-ancestor "$GITHUB_SHA" origin/main`. Si no, el error lo dice.
4. `actions/setup-java@v4.7.1`: Temurin 21 con `cache: gradle`.
5. Comprobar que están los cuatro secrets antes de compilar, para que nunca salga una Release sin firmar. Después, decodificar `RELEASE_KEYSTORE_BASE64` a `$RUNNER_TEMP/release.jks`.
6. `./gradlew test :app:testDebugUnitTest ktlintCheck :app:lintRelease :app:assembleRelease -PetiquetaVersion=$GITHUB_REF_NAME`, con las contraseñas como variables de entorno del paso. GitHub las enmascara en el registro.
7. `apksigner verify --print-certs` (de `$ANDROID_HOME/build-tools`) para obtener la huella SHA-256 del certificado.
   - Se compara con la variable del repositorio `HUELLA_CERTIFICADO`, que es pública y no secreta. Si no coincide, se para.
   - Esto garantiza la misma clave en todas las versiones (spec `app-distribution`, «Actualizar sin perder los datos»).
8. Renombrar el APK a `smart-bike-X.Y.Z.apk` y calcular su SHA-256.
9. `gh release create "$GITHUB_REF_NAME" smart-bike-X.Y.Z.apk --title "X.Y.Z" --generate-notes --notes "…"`, con el SHA-256 del APK y la huella en las notas.
   - `gh` viene instalado en el runner, así que no hace falta una acción de terceros.
10. Borrar `$RUNNER_TEMP/release.jks`, también si un paso falla (`if: always()`).

**Alternativas:**
- Disparar con un push a una rama `release`: obliga a mantener dos ramas en un proyecto de una persona, y la etiqueta ya marca la versión.
- Usar `softprops/action-gh-release`: es una dependencia de terceros con acceso de escritura, y `gh` hace lo mismo.
- Usar `workflow_dispatch`: se puede añadir más adelante; ahora la etiqueta es la única puerta.

**El SDK en el runner.** `ubuntu-latest` trae el SDK de Android con las licencias aceptadas, y AGP descarga la plataforma 37 si falta. Si la descarga falla en la primera ejecución, se añade un paso `sdkmanager "platforms;android-37"` antes de compilar.

### 5. Copia de seguridad: incluir solo `configuracion.json`

- **Manifiesto.** `android:allowBackup="true"`, `android:dataExtractionRules="@xml/reglas_copia"` (Android 12 y posteriores) y `android:fullBackupContent="@xml/copia_completa"` (Android 9 a 11; `minSdk` 28).
- **Las reglas.** Las dos solo tienen `<include domain="file" path="datastore/configuracion.json"/>`. Al haber un `include`, Android deja fuera todo lo demás: `estado.json`, `sesiones/`, las bases de datos y las preferencias.
- **Copia y transferencia.** `reglas_copia.xml` aplica la misma regla a `<cloud-backup>` y a `<device-transfer>`.
- **Lo que no se restaura.** `estado.json` no viaja, así que tampoco viajan los dispositivos, las identidades ante la plataforma, las últimas marchas, la última potencia del ERG ni el nombre Bluetooth original. Al restaurar, la app abre como tras importar un archivo.
  - Las identidades se regeneran. Zwift verá un dispositivo nuevo, como al cambiar de móvil hoy con «Importar».
  - Las notificaciones se vuelven a pedir.
- **Alternativas.**
  - Excluir a mano `estado.json` y `sesiones/`: cualquier archivo nuevo entraría en la copia sin decidirlo.
  - Partir `estado.json` para conservar, por ejemplo, la última marcha: no compensa, porque la marcha vuelve a la de referencia.

### 6. Rotación al empezar a grabar, con una función pura

- **La función.** `ArchivoSesion.aBorrar(nombres: List<String>, conservar: Int = SESIONES_CONSERVADAS): List<String>` devuelve las más antiguas que sobran, de modo que al añadir la nueva queden `conservar`.
  - Ordena como `RepositorioSesiones.listar`: por nombre, que empieza por la fecha de inicio.
  - `SESIONES_CONSERVADAS = 20`.
- **Dónde se borra.** `GrabadorSesion.iniciar`, después de comprobar el espacio y antes de abrir el archivo nuevo, pasa a esa función los `.txt` de la carpeta y los borra.
  - En ese momento la grabación anterior ya está cerrada (`cerrar()`), así que ninguna de la lista se está grabando.
  - El archivo nuevo aún no existe, así que nunca se borra.
  - Cada borrado se anota en el registro («Sesión antigua borrada: …»). Un fallo de `delete()` también se anota y no impide grabar.
- **Sin espacio no se borra nada.** Si `iniciar` acaba en `SinEspacio`, no se graba ni se borra, y lo que hubiera se conserva.
- **Por qué en `iniciar` y no al cerrar.** La spec habla del momento en que empieza una grabación. Además, borrar al cerrar dejaría 21 archivos mientras se graba.
- **Alternativas.**
  - Un límite por tamaño: el usuario eligió por número.
  - Rotar en `RepositorioSesiones`: el que sabe cuándo empieza una grabación es el grabador.

## Risks / Trade-offs

- **[R8 quita algo que se usa por reflexión y la release falla donde la de desarrollo no]** → Antes de la primera Release se prueba la release en el OnePlus 8 con el equipo real (tarea 6). La prueba recorre conexión, Zwift por wifi y por Bluetooth, exportar en release e importar en debug, y arrancar con una configuración guardada por la versión de desarrollo. Cualquier regla nueva va con su motivo en `proguard-rules.pro`.
- **[Pérdida de la clave]** → No hay arreglo técnico. Si se pierde, las instalaciones existentes tienen que desinstalarse. `docs/desarrollo.md` explica cómo guardarla en un gestor de contraseñas con el `.jks` adjunto y las dos contraseñas. Un secret de GitHub no sirve de copia, porque no se puede leer.
- **[Fuga de la clave por el workflow]** → En el repositorio público, los secrets solo están en el entorno `release`, limitado a etiquetas `v*`; mientras sea privado, son del repositorio y solo el usuario puede cambiar los workflows. Los PR desde forks no los reciben. El `.jks` vive en `$RUNNER_TEMP` y se borra al final.
- **[Los secrets no se trasladan al repositorio público nuevo]** → La tarea de documentación lo deja escrito en `docs/desarrollo.md`, y `publicar-repo-publico` debe volver a crear el entorno, los cuatro secrets y `HUELLA_CERTIFICADO`. Mientras el repositorio sea privado, sus Releases solo las ve el usuario.
- **[La versión `0.0.0-dev` en la cabecera de las sesiones de depuración]** → Es intencionado: distingue una sesión de desarrollo de una publicada.
- **[La restauración regenera la identidad ante Zwift]** → Zwift puede pedir emparejar de nuevo tras cambiar de móvil. Ya pasa hoy al importar, y la alternativa (copiar `estado.json`) arrastraría las MAC y el nombre Bluetooth.
- **[Borrar sesiones que el usuario quería conservar]** → Solo ocurre con «Grabar sesiones» activado (apagado por defecto), y la introducción de «Sesiones grabadas» avisa del límite. Para guardar una sesión, se comparte antes.
- **[La copia de seguridad no se puede probar con el hook]** → `bmgr` no está permitido. El usuario lanza los comandos de la tarea 6 con `!`.

## Migration Plan

1. **Clave y entorno de GitHub, antes de la primera etiqueta (lo hace el usuario):**
   - Crear la clave y guardarla en el gestor de contraseñas.
   - Crear el entorno `release` limitado a etiquetas `v*`.
   - Dar de alta en él `RELEASE_KEYSTORE_BASE64`, `RELEASE_KEYSTORE_PASSWORD`, `RELEASE_KEY_ALIAS` y `RELEASE_KEY_PASSWORD`, y la variable `HUELLA_CERTIFICADO` en el repositorio.
2. **Probar la release en local** con las propiedades en `~/.gradle/gradle.properties` (`./gradlew installRelease`).
3. **Primera etiqueta: `v0.1.0`.** Su `versionCode` es 1000, mayor que el 1 actual.
4. **Marcha atrás.** Si una Release sale mal, se borra la Release y la etiqueta (`gh release delete v0.1.0 --cleanup-tag`) y se publica la siguiente versión de parche. Nunca se reutiliza un `versionCode` ya instalado.
