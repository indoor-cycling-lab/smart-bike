# Proposal

## Why

La auditoría de producción (`docs/revision/2026-10-29-auditoria-produccion.md`) dejó tres puntos que impiden distribuir la app fuera del móvil de desarrollo:

- **Release sin optimizar ni firmar (A7).** La variante release se compila sin R8, sin `shrinkResources` y sin firma. No se puede instalar ni actualizar fuera de Android Studio, y el APK lleva todo el código legible.
- **Copia de seguridad sin política (A8).** Sin una política declarada, Android copia todos los datos de la app en la copia de Google. Eso incluye las sesiones grabadas, sin límite: la copia falla en cuanto pasa de 25 MB, y a partir de ahí no se copia ni la configuración. También incluye las direcciones del Neo y del Square, que no sirven en otro móvil, y el nombre Bluetooth original, que podría renombrar un móvil nuevo con el nombre del viejo.
- **Sesiones grabadas sin límite (M7).** Las sesiones grabadas no rotan nunca: solo se para la grabación cuando falta espacio.

Este change deja la app lista para publicar APK firmados en las Releases de GitHub, el canal elegido mientras no haya Google Play.

## What Changes

- **Release optimizada y firmada (A7).**
  - La variante release se compila con R8 y `shrinkResources`, con las reglas que necesiten kotlinx.serialization y la librería BLE de Nordic.
  - Se firma con una clave propia, autofirmada, cuya ubicación y contraseñas se leen del entorno y nunca están en el repositorio.
  - Sin la clave, la release se compila sin firmar en vez de fallar.
- **Versiones por etiqueta.**
  - El nombre y el número de versión salen de una etiqueta `vX.Y.Z` en `main`.
  - No hay rama de release: se publica empujando la etiqueta.
- **Publicación automática.**
  - Un workflow de GitHub Actions se dispara con cada etiqueta `v*`.
  - Comprueba que la etiqueta está en `main` y pasa los tests, ktlint y lint.
  - Compila la release, la firma con la clave guardada en los secrets del repositorio y crea la Release de GitHub con el APK, su SHA-256 y la huella del certificado de firma.
- **Copia de seguridad solo de la configuración (A8).**
  - La copia de Google y la transferencia entre móviles incluyen solo la configuración (lo mismo que «Exportar configuración»).
  - Quedan fuera las sesiones grabadas, los dispositivos configurados, la identidad ante Zwift, la última marcha y el nombre Bluetooth original.
- **Rotación de las sesiones grabadas (M7).**
  - Al empezar una grabación, la app borra las sesiones cerradas más antiguas para que queden como mucho 20, contando la nueva.
  - Nunca se borra la que se está grabando.
  - La introducción de «Sesiones grabadas» lo dice.
  - **BREAKING** respecto a la spec actual, que dice que la app no borra ninguna sesión por su cuenta.
- **Clave y secrets.** Se documenta cómo crear la clave, guardarla en un gestor de contraseñas y subirla a los secrets de GitHub. Lo hace el usuario: las contraseñas no pasan por el agente.

**Fuera de alcance:**

- **Google Play:** ni ficha, ni Play App Signing, ni AAB. La clave elegida sirve para Play más adelante.
- **Versiones previas** (`v1.0.0-beta.1`): solo `vX.Y.Z`.
- **Actualización dentro de la app:** no se avisa de versiones nuevas.
- **Notas de versión escritas a mano:** la Release lleva las que genera GitHub.
- **Un ajuste para el número de sesiones conservadas:** el límite es fijo.
- **Los identificadores de dispositivos reales del repositorio (A9):** van en `publicar-repo-publico`.
- **El resto de la auditoría:** A4, M4–M6, B4, B8, B10 y dividir `Sesion.kt`.

## Capabilities

### New Capabilities

- `app-distribution`: cómo se publica e instala la app fuera del entorno de desarrollo. Cubre las releases firmadas en GitHub por etiqueta, la versión visible, las actualizaciones que se instalan sobre la versión anterior conservando los datos y la verificación de la firma.

### Modified Capabilities

- `settings-backup`: nuevo requisito «Copia de seguridad del sistema»: qué se restaura al cambiar de móvil o reinstalar con la copia de Google y qué no.
- `session-recording`:
  - «Parada por poco espacio libre» deja de prohibir que la app borre sesiones.
  - Nuevo requisito «Sesiones conservadas»: las 20 más recientes.
  - «Lista de sesiones grabadas» suma el límite al texto de introducción.

## Impact

- **Código:**
  - `app/build.gradle.kts`: `isMinifyEnabled`, `isShrinkResources`, `signingConfigs` desde el entorno y versión desde propiedades de Gradle.
  - `app/proguard-rules.pro`, nuevo.
  - `AndroidManifest.xml`: `dataExtractionRules` y `fullBackupContent`, con dos archivos nuevos en `res/xml/`.
  - `GrabadorSesion` y `RepositorioSesiones`: rotación al empezar una grabación.
  - `strings.xml`: `sesiones_intro`.
- **Tests:** rotación de sesiones en `GrabadorSesionTest`; prueba de la release en el OnePlus 8 con el Neo, el Square y Zwift.
- **CI:** `.github/workflows/release.yml`, nuevo. Necesita cuatro secrets en el repositorio. Si `publicar-repo-publico` crea un repositorio nuevo, hay que volver a darlos de alta allí.
- **Maquetas:** `maquetas/sesiones.html`, solo el texto de introducción (no necesita validación previa, AGENTS.md).
- **Documentación:**
  - `docs/desarrollo.md`: cómo publicar una versión, la clave, el gestor de contraseñas y los secrets.
  - `docs/arquitectura.md`: compilación release, copia de seguridad y rotación de sesiones.
  - `docs/funcional.md`: F13.4, con el límite de 20 sesiones.
  - `docs/planificacion.md`: fila 33.
  - `docs/revision/2026-10-29-auditoria-produccion.md`: A7, A8 y M7.
