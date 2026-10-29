## MODIFIED Requirements

### Requirement: Ajustes guardados
Los ajustes, también los de Marchas y los de «Al rodar», «Pantalla», «Ciclista», «Rodillo», y los de Conexión (spec `platform-settings`), SHALL guardarse en el móvil al cambiarlos y SHALL seguir vigentes al volver a abrir la app. Si lo guardado no se puede leer, la app SHALL empezar con los valores por defecto.

#### Scenario: Volver a abrir la app
- **WHEN** se elige «Nunca empujar» y se cierra y se vuelve a abrir la app
- **THEN** sigue elegida «Nunca empujar» y se aplica desde el primer envío al Neo

#### Scenario: Configuración anterior al ajuste
- **WHEN** se abre la app con una configuración guardada antes de que existiera el ajuste «Bajadas»
- **THEN** se conservan los demás ajustes y «Bajadas» está en «La marcha no crea bajadas»

#### Scenario: Transmisión y modo guardados
- **WHEN** se elige la Shimano en «Manual» y se cierra y se vuelve a abrir la app
- **THEN** la transmisión es la Shimano en manual

#### Scenario: Datos guardados
- **WHEN** se eligen tiempo y potencia, en ese orden, se activan los botones − y + y se cierra y se vuelve a abrir la app
- **THEN** Rodaje muestra tiempo y potencia, en ese orden, y los botones − y +

#### Scenario: Configuración anterior a «Al rodar»
- **WHEN** se abre la app con una configuración guardada antes de este change
- **THEN** se conservan la transmisión, la asignación y los demás ajustes, y los datos son potencia y cadencia, los botones − y + están desactivados, el peso del ciclista es 75 kg

#### Scenario: Tema guardado
- **WHEN** se elige «Claro» y se cierra y se vuelve a abrir la app
- **THEN** Rodaje se abre en claro, sin mostrarse antes en oscuro

#### Scenario: Configuración anterior al tema
- **WHEN** se abre la app con una configuración guardada antes de que existiera el ajuste «Tema»
- **THEN** se conservan los demás ajustes y el tema es «Oscuro»

#### Scenario: Orientación guardada
- **WHEN** se elige «Horizontal» y se cierra y se vuelve a abrir la app con el móvil en vertical
- **THEN** Rodaje se abre en horizontal y «Orientación» sigue en «Horizontal»

#### Scenario: Configuración anterior a la orientación
- **WHEN** se abre la app con una configuración guardada antes de que existiera el ajuste «Orientación»
- **THEN** se conservan los demás ajustes y la orientación es «Según el móvil»

#### Scenario: Modo de conexión guardado
- **WHEN** se elige «Solo Bluetooth» en Conexión y se cierra y se vuelve a abrir la app
- **THEN** sigue elegido y la app se anuncia por Bluetooth desde el primer momento

#### Scenario: Mando desactivado guardado
- **WHEN** se abre la app con una configuración anterior que tenía «Mando de Zwift» desactivado
- **THEN** se conservan los demás ajustes salvo los de conexión, y el mando se publica si hay un Square recordado

#### Scenario: Configuración anterior al mando
- **WHEN** se abre la app con una configuración guardada antes de que existiera «Mando de Zwift»
- **THEN** se conservan los demás ajustes salvo los de conexión, y el mando se publica si hay un Square recordado

#### Scenario: Configuración con «Pendiente sin Zwift»
- **WHEN** se abre la app con una configuración anterior que tenía «Pendiente sin Zwift» en 3,0 %
- **THEN** se conservan los demás ajustes y, sin Zwift, el terreno es llano (0 %)

#### Scenario: Configuración ilegible
- **WHEN** lo guardado no se puede leer al abrir la app
- **THEN** la app empieza con los valores por defecto, con el tema «Oscuro» y la orientación «Según el móvil»

### Requirement: Secciones de Ajustes
Tras las entradas y «Al rodar», Ajustes SHALL mostrar en este orden: la sección «Pantalla», con «Tema» y «Orientación»; la sección «Ciclista»; la sección «Rodillo», con «Bajadas»; y la sección «Copia de seguridad», con «Exportar configuración» e «Importar configuración». Ajustes SHALL NOT tener sección «Zwift»: lo que publica la app está en Conexión.

#### Scenario: Orden de las secciones
- **WHEN** se abre Ajustes y se desplaza hasta el final
- **THEN** las secciones aparecen en el orden indicado y la última es «Copia de seguridad»

## REMOVED Requirements

### Requirement: Pendiente sin Zwift
**Reason**: Decidido por el usuario (28-10-2026): fijar una pendiente en Ajustes no aporta; sin app de entrenamiento el terreno es llano y la dureza la da la marcha.
**Migration**: La pendiente base sin app de entrenamiento es siempre 0 %. Una configuración anterior con otro valor se lee ignorándolo.
