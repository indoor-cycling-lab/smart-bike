## MODIFIED Requirements

### Requirement: Zwift en Conexiones
La sección Conexiones de Diagnóstico SHALL tener una fila de Zwift con su estado en palabra («Conectado», «Esperando» o «Sin conexión») y su ayuda: elegir «Smart Bike» en Zwift mientras espera, la dirección desde la que está conectado, o conectar el móvil a la wifi de Zwift si no hay wifi, con un aviso. Debajo SHALL tener una fila del mando con su estado en palabra; con el mando dentro del mismo dispositivo, su ayuda SHALL decir dentro de cuál va («Dentro de OnePlus 8 · por Bluetooth»).

#### Scenario: Esperando a Zwift
- **WHEN** el móvil tiene wifi y Zwift no está conectado
- **THEN** la fila de Zwift dice «Esperando» con «Elige Smart Bike en Zwift como rodillo, potencia y cadencia»

#### Scenario: Zwift conectado
- **WHEN** Zwift está conectado desde el PC 192.168.0.10
- **THEN** la fila de Zwift dice «Conectado» con «Desde 192.168.0.10»

#### Scenario: Sin wifi
- **WHEN** el móvil no tiene wifi
- **THEN** la fila de Zwift dice «Sin conexión» con «Conecta el móvil a la misma red wifi que Zwift» y hay un aviso informativo «Sin wifi. Para rodar en Zwift, conecta el móvil a la misma red wifi que el equipo de Zwift.»

#### Scenario: Bluetooth apagado
- **WHEN** el Bluetooth está apagado y el móvil tiene wifi
- **THEN** las filas del Neo, del Square y del mando dicen «Sin conexión» y la de Zwift sigue con su estado

#### Scenario: Mando conectado en Diagnóstico
- **WHEN** Zwift está conectado al mando y lo ha saludado
- **THEN** la fila del mando dice «Conectado»

#### Scenario: Mando desactivado en Diagnóstico
- **WHEN** no hay un Square recordado y el mando no se publica
- **THEN** la fila del mando dice «Desactivado»
