## MODIFIED Requirements

### Requirement: Zwift en Conexiones
La sección Conexiones de Diagnóstico SHALL tener una fila de Zwift con su estado en palabra («Conectado», «Esperando» o «Sin conexión») y qué hacer: elegir «Smart Bike» en Zwift mientras espera, desde qué dirección de la red está conectado, o conectar el móvil a la red wifi de Zwift si no hay wifi, con un aviso informativo. Debajo SHALL tener una fila del mando de Zwift con su estado en palabra, como en Dispositivos.

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
- **WHEN** «Mando de Zwift» está desactivado
- **THEN** la fila del mando dice «Desactivado»

### Requirement: Nombre anunciado a Zwift en Diagnóstico
Diagnóstico SHALL mostrar en Conexiones, bajo la fila de Zwift, el nombre con el que la app se anuncia ahora ante Zwift, y SHALL registrar en el registro cada vez que se anuncia o se retira con ese nombre. El nombre SHALL mostrarse también con Zwift sin conectar y sin wifi, como el último que se anunció o se anunciaría.

#### Scenario: Ajuste desactivado
- **WHEN** «Simular un KICKR BIKE» está desactivado y se abre Diagnóstico
- **THEN** bajo la fila de Zwift se muestra «Smart Bike» como nombre anunciado

#### Scenario: Ajuste activado
- **WHEN** «Simular un KICKR BIKE» está activado y se abre Diagnóstico
- **THEN** bajo la fila de Zwift se muestra el «KICKR BIKE PRO» con sus cuatro caracteres y el registro tiene el anuncio con ese nombre

#### Scenario: Cambio del ajuste
- **WHEN** se cambia «Simular un KICKR BIKE» con Diagnóstico abierto
- **THEN** el registro tiene la retirada del nombre anterior y el anuncio del nuevo, y bajo la fila de Zwift se muestra el nuevo

#### Scenario: Sin wifi
- **WHEN** no hay wifi y se abre Diagnóstico con el ajuste activado
- **THEN** bajo la fila de Zwift se muestra el nombre que se anunciará, junto al estado sin conexión
