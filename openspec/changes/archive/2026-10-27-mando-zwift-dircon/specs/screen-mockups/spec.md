## ADDED Requirements

### Requirement: Modo de conexión con Zwift en la maqueta de Ajustes
La maqueta de Ajustes SHALL mostrar, en la sección «Zwift», la elección «Conexión con Zwift» con «Rodillo por wifi y mando por Bluetooth» y «Todo por wifi (modo Zwift Ride)» y su ayuda, con los estados de cada modo elegido.

#### Scenario: Modo de siempre
- **WHEN** se abre `maquetas/ajustes.html#estado=inicio`
- **THEN** la sección «Zwift» muestra «Mando de Zwift» activado y «Rodillo por wifi y mando por Bluetooth» elegido, con su ayuda

#### Scenario: Modo todo por wifi
- **WHEN** se elige el estado de modo todo por wifi
- **THEN** «Todo por wifi (modo Zwift Ride)» está elegido y la ayuda dice que Zwift ve un solo dispositivo y mostrará su marcha virtual

### Requirement: Mando por wifi en la maqueta de Dispositivos
La maqueta de Dispositivos SHALL tener estados con el mando por wifi: esperando, conectado, sin wifi y desactivado, con la fila del mando explicando cómo emparejarlo en Zwift como potencia y mando a la vez.

#### Scenario: Mando por wifi esperando
- **WHEN** se elige el estado de mando por wifi esperando
- **THEN** la fila del mando dice «Esperando» y que en Zwift se elige «Square Shift» como potencia y mando a la vez

#### Scenario: Mando por wifi conectado y sin wifi
- **WHEN** se eligen los estados de mando por wifi conectado y sin wifi
- **THEN** la fila del mando dice «Conectado», o «Sin conexión» con la ayuda de conectar el móvil a la misma red wifi que Zwift

#### Scenario: Sin «No disponible» por wifi
- **WHEN** se revisan los estados del mando por wifi
- **THEN** ninguno dice «No disponible»

### Requirement: Simulación de Zwift en la maqueta de Diagnóstico
La maqueta de Diagnóstico SHALL tener un estado con la simulación de marchas virtuales de Zwift en Conexiones, bajo la fila del mando, con la pendiente, los coeficientes, las masas, la relación de la marcha de Zwift y la hora.

#### Scenario: Simulación en Diagnóstico
- **WHEN** se elige el estado de simulación de Zwift de la maqueta de Diagnóstico
- **THEN** bajo la fila del mando se ve la pendiente de Zwift y la relación de su marcha, y aparte lo que simula el Neo con la marcha de la app

#### Scenario: Sin simulación
- **WHEN** se elige el estado rodando con Zwift de la maqueta de Diagnóstico
- **THEN** no aparece la línea de la simulación de Zwift
