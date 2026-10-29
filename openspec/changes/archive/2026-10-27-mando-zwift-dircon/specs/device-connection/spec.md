## ADDED Requirements

### Requirement: Mando de Zwift por wifi en Dispositivos
Con el modo «Todo por wifi», la fila del mando de Zwift SHALL mostrar su estado en palabra («Conectado», «Esperando», «Desactivado» o «Sin conexión») y explicar cómo emparejarlo: elegir «Square Shift» en Zwift como potencia, rodillo y mando a la vez, sin buscar el móvil en la lista de mandos. SHALL NOT mostrar «No disponible», porque no hace falta modo periférico.

#### Scenario: Mando por wifi esperando
- **WHEN** «Mando de Zwift» está activado con el modo «Todo por wifi», hay wifi y Zwift no está conectado
- **THEN** la fila del mando dice «Esperando» y explica que en Zwift se elige «Square Shift» como potencia y mando a la vez

#### Scenario: Mando por wifi conectado
- **WHEN** Zwift está conectado al mando por wifi y lo ha saludado
- **THEN** la fila del mando dice «Conectado»

#### Scenario: Sin wifi con el mando por wifi
- **WHEN** el modo es «Todo por wifi (modo Zwift Ride)» y el móvil no tiene wifi
- **THEN** la fila del mando dice «Sin conexión» y pide conectar el móvil a la misma red wifi que Zwift

#### Scenario: Bluetooth apagado con el mando por wifi
- **WHEN** el modo es «Todo por wifi (modo Zwift Ride)», hay wifi y el Bluetooth está apagado
- **THEN** la fila del mando sigue con su estado, mientras las del Neo y el Square dicen «Sin conexión»

#### Scenario: Móvil sin modo periférico con el mando por wifi
- **WHEN** el móvil no puede anunciarse como dispositivo Bluetooth y el modo es «Todo por wifi (modo Zwift Ride)»
- **THEN** la fila del mando no dice «No disponible» y sigue su estado normal
