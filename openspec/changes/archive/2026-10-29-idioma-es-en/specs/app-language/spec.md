## ADDED Requirements

### Requirement: Idiomas de la interfaz
La app SHALL mostrar su interfaz en español o en inglés, y en un solo idioma a la vez. Todo texto que ve el usuario SHALL estar en el idioma elegido: pantallas, avisos, diálogos, mensajes de error, descripciones para el lector de pantalla y la notificación de la sesión en primer plano. La documentación, las trazas y las líneas del registro de Diagnóstico SHALL seguir en español en los dos idiomas.

#### Scenario: Interfaz en inglés
- **WHEN** el idioma es «English» y se recorren Rodaje, Conexión, Dispositivos, Ajustes y sus pantallas secundarias
- **THEN** no queda ningún texto de la interfaz en español, salvo el nombre de la app y las líneas del registro de Diagnóstico

#### Scenario: Notificación de la sesión
- **WHEN** el idioma es «English» y hay una sesión en primer plano
- **THEN** la notificación, su canal y su botón para terminar están en inglés

#### Scenario: Lector de pantalla
- **WHEN** el idioma es «English» y el lector de pantalla recorre la barra de conexiones y los ajustes
- **THEN** las descripciones de los iconos y de las opciones están en inglés

#### Scenario: Plurales
- **WHEN** el idioma es «English» y se cambian 1 y 3 marchas de una vez
- **THEN** el aviso dice «+1 gear» y «+3 gears»

### Requirement: Idioma del primer arranque
En el primer arranque, sin idioma guardado, la app SHALL usar el español si el idioma principal del móvil es el español y el inglés en cualquier otro caso. La app SHALL guardar ese resultado como idioma elegido antes de mostrar ninguna pantalla, de modo que la primera pantalla ya salga en él.

#### Scenario: Móvil en español
- **WHEN** se abre la app por primera vez con el móvil en español (de cualquier región)
- **THEN** la interfaz está en español y Ajustes muestra «Español» elegido

#### Scenario: Móvil en otro idioma
- **WHEN** se abre la app por primera vez con el móvil en francés
- **THEN** la interfaz está en inglés y Ajustes muestra «English» elegido

#### Scenario: Sin parpadeo de idioma
- **WHEN** se abre la app por primera vez con el móvil en inglés
- **THEN** ninguna pantalla se muestra antes en español

#### Scenario: Actualizar desde una versión sin idioma
- **WHEN** se abre la app, con ajustes guardados por una versión anterior a este ajuste y el móvil en inglés
- **THEN** se conservan todos los ajustes y la interfaz está en inglés

### Requirement: Idioma elegido por el usuario
Tras el primer arranque, el idioma SHALL ser el que el usuario eligió en Ajustes, y SHALL conservarse al cerrar y volver a abrir la app. La app SHALL NOT volver a seguir el idioma del móvil.

#### Scenario: Volver a abrir la app
- **WHEN** se elige «English» y se cierra y se vuelve a abrir la app
- **THEN** la interfaz sigue en inglés desde la primera pantalla

#### Scenario: Cambiar el idioma del móvil después
- **WHEN** la app está en español y se cambia el idioma del móvil a inglés
- **THEN** la app sigue en español

### Requirement: Cambio de idioma al momento
Al elegir otro idioma, la interfaz SHALL cambiar al momento, en toda la app, sin terminar la sesión, cambiar la marcha ni cortar ninguna conexión, y sin que Zwift lo note.

#### Scenario: Cambiar rodando
- **WHEN** se rueda en la marcha 14 con el Neo y el Square conectados, se elige «English» en Ajustes y se vuelve a Rodaje
- **THEN** Ajustes está en inglés en cuanto se elige, Rodaje está en inglés con la marcha 14 y los dos enlaces conectados

#### Scenario: Cambiar con Zwift conectado
- **WHEN** se rueda en Zwift y se cambia el idioma
- **THEN** Zwift sigue conectado, recibiendo la potencia y la cadencia, y la resistencia del Neo no cambia

#### Scenario: Notificación al cambiar
- **WHEN** hay una sesión en primer plano y se cambia el idioma
- **THEN** la notificación pasa al idioma nuevo sin que se reinicie la sesión

#### Scenario: Pantallas secundarias
- **WHEN** se cambia a «English» y se abren Marchas, Datos en pantalla, Tabla de marchas, Diagnóstico y Sesiones grabadas desde Ajustes
- **THEN** las cinco muestran sus textos en inglés

### Requirement: Formatos según el idioma
Los números, las fechas y los tamaños de archivo SHALL escribirse según el idioma elegido: en español con coma decimal y meses en español, y en inglés con punto decimal y meses en inglés. Las unidades (km/h, W, rpm, km) no cambian.

#### Scenario: Decimales
- **WHEN** se rueda a 24,5 km/h con la distancia en 12,34 km y se cambia a «English»
- **THEN** Rodaje dice «24.5 km/h» y «12.34 km», y en español dice «24,5 km/h» y «12,34 km»

#### Scenario: Fecha de una sesión grabada
- **WHEN** hay una sesión grabada el 29 de octubre de 2026 a las 18:42
- **THEN** Sesiones grabadas dice «29 oct 2026 · 18:42» en español y «29 Oct 2026 · 18:42» en inglés

#### Scenario: Tamaño de archivo
- **WHEN** una sesión grabada pesa 1,2 MB y el idioma es «English»
- **THEN** su tamaño dice «1.2 MB»

#### Scenario: Valores negativos legibles
- **WHEN** la pendiente simulada es −3,5 % y el idioma es «English»
- **THEN** Diagnóstico y Rodaje muestran «−3.5 %» con el mismo signo que en español

### Requirement: Textos que caben en los dos idiomas
Cada pantalla SHALL mostrar todos sus textos completos en español y en inglés, a 360 dp de ancho y con el tamaño de fuente del sistema al 130 %, sin cortarse ni tapar otro elemento, y los objetivos táctiles SHALL mantener su tamaño mínimo.

#### Scenario: Pantalla estrecha en inglés
- **WHEN** el idioma es «English» y se abre Rodaje en vertical a 360 dp de ancho con las cuatro conexiones
- **THEN** el estado de la publicación, la transmisión, los datos y los botones se ven completos, sin texto cortado

#### Scenario: Objetivo táctil
- **WHEN** el idioma es «English» y se toca cualquier punto de la fila de una opción de Ajustes
- **THEN** se elige esa opción, igual que en español

### Requirement: Idioma en la copia de seguridad
El idioma elegido SHALL formar parte de la configuración que se exporta y se importa. Al confirmar una importación, la interfaz SHALL pasar al idioma del archivo al momento. Un archivo de una versión anterior a este ajuste SHALL conservar el idioma que ya tiene la app.

#### Scenario: Importar en otro idioma
- **WHEN** la app está en español y se importa un archivo exportado con «English» elegido
- **THEN** al confirmar, la interfaz pasa a inglés y el aviso de importación se muestra ya en inglés

#### Scenario: Importar un archivo anterior al ajuste
- **WHEN** la app está en inglés y se importa un archivo exportado antes de este ajuste
- **THEN** los demás ajustes son los del archivo y la interfaz sigue en inglés
