## ADDED Requirements

### Requirement: Conexión con Zwift en Ajustes
La sección «Zwift» de Ajustes SHALL tener la elección «Conexión con Zwift» entre «Rodillo por wifi y mando por Bluetooth», por defecto, y «Todo por wifi (modo Zwift Ride)», con una ayuda que explique la diferencia: con el primero, Zwift ve dos dispositivos; con «Todo por wifi» ve uno solo, mostrará su propia marcha, que no será la de la app, y hay que emparejar de nuevo en Zwift. El cambio SHALL aplicarse al momento, también rodando. Con «Mando de Zwift» desactivado, la elección SHALL poder cambiarse y quedar guardada.

#### Scenario: Valor por defecto
- **WHEN** se abre Ajustes por primera vez
- **THEN** la conexión con Zwift es «Rodillo por wifi y mando por Bluetooth»

#### Scenario: Elegir todo por wifi
- **WHEN** se elige «Todo por wifi (modo Zwift Ride)»
- **THEN** queda elegido, se guarda y la ayuda dice que Zwift ve un solo dispositivo, que mostrará su marcha y que hay que emparejar de nuevo

#### Scenario: Modo guardado
- **WHEN** se elige «Todo por wifi» y se cierra y se vuelve a abrir la app
- **THEN** sigue elegido y el mando se ofrece por wifi desde el primer momento

#### Scenario: Configuración anterior al modo
- **WHEN** se abre la app con una configuración guardada antes de que existiera esta elección
- **THEN** se conservan los demás ajustes y la conexión es «Rodillo por wifi y mando por Bluetooth»

#### Scenario: Móvil sin modo periférico
- **WHEN** el móvil no puede anunciarse como dispositivo Bluetooth y se abre Ajustes
- **THEN** «Todo por wifi» sigue pudiéndose elegir y su ayuda dice que no hace falta el modo periférico

## MODIFIED Requirements

### Requirement: Mando de Zwift en Ajustes
Ajustes SHALL tener una sección «Zwift» con el interruptor «Mando de Zwift», activado por defecto, y una ayuda que diga que con él los botones del Square llegan a Zwift a través de la app y que el Square ya no se empareja en Zwift. El cambio SHALL aplicarse al momento, también rodando.

#### Scenario: Valor por defecto
- **WHEN** se abre Ajustes por primera vez
- **THEN** la sección «Zwift» tiene «Mando de Zwift» activado

#### Scenario: Activar rodando
- **WHEN** «Mando de Zwift» está desactivado, se rueda con el Neo y el Square y se activa
- **THEN** el mando empieza a ofrecerse por el modo elegido sin cambiar la marcha ni cortar la conexión del rodillo con Zwift

#### Scenario: Móvil sin modo periférico
- **WHEN** el móvil no puede anunciarse como dispositivo Bluetooth, el modo es el de siempre y se abre Ajustes
- **THEN** «Mando de Zwift» sigue pudiéndose cambiar y su ayuda dice que este móvil no puede hacer de mando por Bluetooth
