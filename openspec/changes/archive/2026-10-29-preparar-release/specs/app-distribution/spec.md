# Spec Delta

## Purpose

Publicar la app fuera del entorno de desarrollo: versiones firmadas en las Releases de GitHub a partir de una etiqueta, con la versión visible, actualizaciones que se instalan sobre la anterior sin perder los datos y un APK que cualquiera puede verificar.

## ADDED Requirements

### Requirement: Versión publicada por etiqueta
Cada versión de la app SHALL publicarse como una Release del repositorio de GitHub al empujar una etiqueta `vX.Y.Z` (tres números) sobre un commit de `main`, y solo entonces: un push a `main` sin etiqueta SHALL NOT publicar nada. La Release SHALL llevar el APK firmado, su SHA-256 y la huella SHA-256 del certificado de firma. Si la etiqueta no tiene esa forma, no está en `main` o no pasan las comprobaciones del proyecto, SHALL NOT publicarse ninguna Release.

#### Scenario: Publicar una versión
- **WHEN** se empuja la etiqueta `v0.2.0` sobre el último commit de `main`
- **THEN** a los pocos minutos el repositorio tiene la Release `v0.2.0` con el APK, su SHA-256 y la huella del certificado

#### Scenario: Push sin etiqueta
- **WHEN** se empuja un commit a `main` sin etiqueta
- **THEN** no se crea ninguna Release

#### Scenario: Etiqueta fuera de main
- **WHEN** se empuja una etiqueta `v0.3.0` sobre un commit que no está en `main`
- **THEN** no se crea ninguna Release y la ejecución dice que la etiqueta no está en `main`

#### Scenario: Comprobaciones que fallan
- **WHEN** se empuja una etiqueta con un test o el linter en rojo
- **THEN** no se crea ninguna Release

#### Scenario: Etiqueta mal formada
- **WHEN** se empuja la etiqueta `v0.2` o `v0.2.0-beta`
- **THEN** no se crea ninguna Release y la ejecución dice qué forma debe tener

### Requirement: Versión visible
La versión de la app SHALL ser la de su etiqueta sin la `v` (`v0.2.0` → `0.2.0`), en Información de la app del sistema y en la cabecera de las sesiones grabadas. Cada versión publicada SHALL tener un número de versión mayor que el de cualquier versión anterior, para que Android la acepte como actualización.

#### Scenario: Versión en el móvil
- **WHEN** se instala el APK de la Release `v0.2.0` y se mira en Información de la app
- **THEN** la versión es `0.2.0`

#### Scenario: Versión en la sesión grabada
- **WHEN** con esa versión y «Grabar sesiones» activado se rueda y se abre el archivo de la sesión
- **THEN** la cabecera dice `0.2.0`

### Requirement: Actualizar sin perder los datos
Un APK publicado SHALL instalarse como actualización sobre cualquier versión publicada antes, sin desinstalarla y conservando la configuración, los dispositivos configurados y las sesiones grabadas. Todas las versiones publicadas SHALL firmarse con el mismo certificado.

#### Scenario: Actualizar
- **WHEN** con la versión `0.2.0` instalada, el Neo y el Square configurados y una transmisión elegida, se instala el APK de `0.3.0`
- **THEN** la instalación se hace como actualización y, al abrir la app, la transmisión, los botones, el Neo y el Square siguen configurados y conectan solos

#### Scenario: Volver a una versión anterior
- **WHEN** con la `0.3.0` instalada se intenta instalar el APK de `0.2.0`
- **THEN** Android lo rechaza por ser una versión anterior y la `0.3.0` sigue instalada con sus datos

### Requirement: Verificar el APK
Cualquiera SHALL poder comprobar que un APK descargado es el publicado y que lo firmó el autor: el SHA-256 del archivo y la huella del certificado SHALL figurar en la Release, y la huella SHALL ser la misma en todas las versiones.

#### Scenario: Comprobar la descarga
- **WHEN** se descarga el APK de una Release y se calcula su SHA-256
- **THEN** coincide con el que figura en esa Release

#### Scenario: Comprobar la firma
- **WHEN** se inspecciona el certificado del APK con las herramientas de Android
- **THEN** su huella SHA-256 coincide con la de la Release y con la de las versiones anteriores

### Requirement: La versión publicada funciona igual que la de desarrollo
La versión publicada SHALL comportarse igual que la de desarrollo en todo lo que ve el usuario: conexión con el Neo, el Square y los demás dispositivos, la plataforma por wifi y por Bluetooth, la configuración guardada, exportar e importar y las sesiones grabadas. Una configuración exportada desde una SHALL importarse en la otra sin diferencias.

#### Scenario: Rodar con la versión publicada
- **WHEN** se instala el APK publicado, se configuran el Neo y el Square y se rueda en Zwift por wifi y por Bluetooth
- **THEN** las palancas cambian de marcha, Zwift recibe potencia y cadencia y controla la pendiente, igual que con la de desarrollo

#### Scenario: Exportar en una e importar en la otra
- **WHEN** se exporta la configuración desde la versión publicada y se importa en la de desarrollo
- **THEN** la configuración importada es la misma: transmisión, botones, datos en pantalla, ciclista, tema y modo de conexión

#### Scenario: Configuración de una versión anterior
- **WHEN** se actualiza desde una versión publicada anterior con su configuración guardada
- **THEN** la app la lee entera, sin volver a los valores por defecto
