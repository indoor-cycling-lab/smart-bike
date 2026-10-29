## MODIFIED Requirements

### Requirement: Zwift en Conexiones
La sección Conexiones de Diagnóstico SHALL tener una fila de Zwift con su estado en palabra («Conectado», «Esperando» o «Sin conexión») y qué hacer: elegir «Square Shift» en Zwift mientras espera, desde qué dirección de la red está conectado, o conectar el móvil a la red wifi de Zwift si no hay wifi, con un aviso informativo. Debajo SHALL tener una fila del mando de Zwift con su estado en palabra, como en Dispositivos.

#### Scenario: Esperando a Zwift
- **WHEN** el móvil tiene wifi y Zwift no está conectado
- **THEN** la fila de Zwift dice «Esperando» con «Elige Square Shift en Zwift como rodillo, potencia y cadencia»

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
- **WHEN** «Mando de Zwift» está desactivado
- **THEN** la fila del mando dice «Desactivado»
