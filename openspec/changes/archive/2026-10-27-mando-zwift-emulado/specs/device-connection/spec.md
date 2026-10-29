## MODIFIED Requirements

### Requirement: Pantalla de Dispositivos
Dispositivos SHALL mostrar una fila para el Neo y otra para el Square, con su nombre y su estado en palabra («Conectado», «Conectando», «Buscando», «No encontrado» o «Sin conexión») y, mientras la app intenta conectarlo, la ayuda para despertarlo; una fila para Zwift con su estado («Conectado», «Esperando» o «Sin conexión») y cómo emparejarlo; y una fila para el mando de Zwift con su estado («Conectado», «Esperando», «Desactivado», «Sin conexión» o «No disponible») y cómo emparejarlo en Zwift con el nombre Bluetooth del móvil. Al pie SHALL tener «Buscar dispositivos».

#### Scenario: Todo conectado
- **WHEN** el Neo, el Square, Zwift y el mando están conectados y se abre Dispositivos
- **THEN** las cuatro filas dicen «Conectado», con el nombre del Neo y del Square

#### Scenario: Primer uso
- **WHEN** se abre Dispositivos sin dispositivos recordados
- **THEN** el Neo y el Square dicen «Sin conexión» y «Buscar dispositivos» está disponible

#### Scenario: Neo dormido
- **WHEN** el Neo recordado está dormido y la app intenta conectarlo
- **THEN** su fila dice «Conectando» con «Pedalea para despertarlo»

#### Scenario: Zwift
- **WHEN** el móvil tiene wifi y Zwift no está conectado
- **THEN** la fila de Zwift dice «Esperando» y explica que en la pantalla de emparejamiento de Zwift se elige «Square Shift» como rodillo, potencia y cadencia

#### Scenario: Sin wifi
- **WHEN** el móvil no tiene wifi
- **THEN** la fila de Zwift dice «Sin conexión» y pide conectar el móvil a la misma red wifi que Zwift, y el mando sigue con su estado

#### Scenario: Bluetooth apagado
- **WHEN** el Bluetooth del móvil está apagado y se abre Dispositivos
- **THEN** el Neo, el Square y el mando dicen «Sin conexión» y se ve el aviso «El Bluetooth está apagado. Actívalo para buscar el Neo y el Square.» con «Activar Bluetooth», como en «Bluetooth apagado»

#### Scenario: Sin permiso
- **WHEN** falta el permiso de Bluetooth y se toca «Buscar dispositivos» en Dispositivos
- **THEN** la app pide el permiso y, si se deniega, muestra el aviso con «Abrir ajustes», como en «Permisos de Bluetooth»

#### Scenario: No encontrado con aviso
- **WHEN** el Neo no aparece en 30 s de búsqueda desde Dispositivos
- **THEN** su fila dice «No encontrado» y se ve el aviso de «Dispositivo no encontrado» con qué hacer

#### Scenario: Conexión perdida con la pantalla abierta
- **WHEN** Dispositivos está abierta y se pierde la conexión con el Square
- **THEN** su fila pasa a «Conectando» con «Pulsa un botón del Square para despertarlo», y vuelve a «Conectado» al reconectar

#### Scenario: Mando esperando
- **WHEN** «Mando de Zwift» está activado, el Bluetooth encendido y Zwift no está conectado al mando
- **THEN** la fila del mando dice «Esperando» y explica que en Zwift, en la lista de mandos, se elige el móvil por su nombre Bluetooth (por ejemplo «OnePlus 8») y que el Square no se empareja en Zwift

#### Scenario: Mando conectado
- **WHEN** Zwift está conectado al mando y lo ha saludado
- **THEN** la fila del mando dice «Conectado»

#### Scenario: Mando desactivado
- **WHEN** «Mando de Zwift» está desactivado
- **THEN** la fila del mando dice «Desactivado», explica que se activa en Ajustes y que, sin él, el Square puede seguir emparejado en Zwift para girar

#### Scenario: Móvil sin modo periférico
- **WHEN** «Mando de Zwift» está activado y el móvil no puede anunciarse como dispositivo Bluetooth
- **THEN** la fila del mando dice «No disponible» y explica que este móvil no puede hacer de mando de Zwift
