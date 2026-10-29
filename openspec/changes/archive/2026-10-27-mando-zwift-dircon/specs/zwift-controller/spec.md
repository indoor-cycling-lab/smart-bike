## ADDED Requirements

### Requirement: Mando por wifi dentro del rodillo
Con el modo «Todo por wifi (modo Zwift Ride)», la app SHALL ofrecer el mando en el mismo dispositivo que el rodillo, la potencia y la cadencia, de modo que Zwift vea un solo dispositivo, «Square Shift», como fuente de potencia y como mando. En ese caso la app SHALL NOT anunciarse por Bluetooth como mando. Las funciones de Zwift de los botones del Square SHALL llegar a Zwift y comportarse como por Bluetooth: navegar, girar, frenar, powerup y Ride On, las palancas sueltas a 0 y el mando activo sin pulsar nada.

#### Scenario: Emparejar el mando por wifi
- **WHEN** el modo es «Todo por wifi», Zwift empareja «Square Shift» por wifi como potencia y como mando y la app tiene wifi
- **THEN** Zwift muestra «Square Shift» como rodillo y como mando a la vez, y Dispositivos muestra el mando «Conectado» tras el saludo de Zwift

#### Scenario: Funciones desde el Square por wifi
- **WHEN** el mando está conectado por wifi y se pulsa la cruceta ↑ y después B
- **THEN** Zwift abre la barra de acciones y la cierra, como por Bluetooth

#### Scenario: Freno y giro por wifi
- **WHEN** el mando está conectado por wifi, se mantiene una maneta de freno y se suelta
- **THEN** el avatar frena mientras se mantiene y, al soltar, Zwift no conserva el freno

#### Scenario: Diez minutos sin pulsar nada por wifi
- **WHEN** el mando está conectado por wifi y pasan diez minutos sin pulsar nada
- **THEN** Zwift sigue con el mando conectado y el siguiente botón llega

#### Scenario: Sin Bluetooth
- **WHEN** el modo es «Todo por wifi» y el Bluetooth del móvil está apagado
- **THEN** el mando sigue disponible por wifi sin anunciarse por Bluetooth, mientras el Neo y el Square no se conectan hasta que vuelva

#### Scenario: Zwift perdido por wifi
- **WHEN** el mando está conectado por wifi con un botón mantenido y se pierde la conexión con Zwift
- **THEN** la app suelta todos los botones y el siguiente Zwift que se conecta empieza sin nada pulsado

#### Scenario: Mando desactivado por wifi
- **WHEN** el modo es «Todo por wifi» y «Mando de Zwift» está desactivado
- **THEN** los botones del Square no llegan a Zwift y el rodillo sigue funcionando en este modo

### Requirement: Cambio de modo de conexión
La app SHALL permitir cambiar entre los modos «Rodillo por wifi y mando por Bluetooth» y «Todo por wifi (modo Zwift Ride)» con la sesión abierta. Al cambiar, SHALL soltar todas las funciones de Zwift, dejar de ofrecer lo del modo anterior y ofrecer lo del nuevo, sin cambiar la marcha ni lo que simula el Neo. Zwift SHALL tener que reconectarse y emparejar el dispositivo del modo nuevo.

#### Scenario: Del modo de siempre a todo por wifi
- **WHEN** Zwift está conectado al rodillo por wifi y al mando por Bluetooth y se elige «Todo por wifi»
- **THEN** el mando deja de anunciarse por Bluetooth, Zwift se reconecta por wifi y la marcha y lo que simula el Neo no cambian

#### Scenario: De todo por wifi al modo de siempre
- **WHEN** el mando está conectado por wifi y se elige «Rodillo por wifi y mando por Bluetooth»
- **THEN** el mando se anuncia por Bluetooth, el rodillo deja de ofrecer el servicio del mando y Zwift vuelve a controlarlo con FTMS

#### Scenario: Con el mando desactivado
- **WHEN** «Mando de Zwift» está desactivado y se elige otro modo
- **THEN** el modo queda guardado y los botones siguen sin llegar a Zwift

### Requirement: Identidad propia de cada modo
Cada modo de conexión SHALL anunciarse ante Zwift con su propia identidad, de modo que Zwift los vea como dispositivos distintos y cambiar de modo y volver no deje a Zwift con un dispositivo que ya no encaja.

#### Scenario: Volver al modo de siempre
- **WHEN** se rueda en «Todo por wifi», se cambia al modo de siempre y se vuelve a emparejar el rodillo en Zwift
- **THEN** Zwift conecta el rodillo y muestra la potencia, sin quedarse en «Conectando…»

#### Scenario: Primera vez en todo por wifi
- **WHEN** se elige «Todo por wifi» por primera vez
- **THEN** Zwift lo muestra como un dispositivo nuevo que hay que emparejar

#### Scenario: Identidad conservada
- **WHEN** se cierra y se vuelve a abrir la app en «Todo por wifi»
- **THEN** Zwift reconoce el mismo dispositivo y se reconecta sin volver a emparejar

## MODIFIED Requirements

### Requirement: Mando publicado con la sesión de rodaje
Mientras dura la sesión de rodaje, «Mando de Zwift» está activado y el modo es «Rodillo por wifi y mando por Bluetooth», la app SHALL anunciarse por Bluetooth como mando de Zwift, con el nombre Bluetooth del móvil, aunque el rodillo, la potencia y la cadencia vayan por wifi. Al terminar la sesión o desactivar el ajuste, SHALL dejar de anunciarse y Zwift SHALL perder el mando.

#### Scenario: Abrir la app con el ajuste activado
- **WHEN** se abre la app con «Mando de Zwift» activado, el modo de siempre y el Bluetooth encendido
- **THEN** el mando se anuncia sin tocar nada y Dispositivos dice «Esperando» en su fila

#### Scenario: Desactivar el ajuste rodando
- **WHEN** Zwift está conectado al mando y se desactiva «Mando de Zwift» en Ajustes
- **THEN** Zwift pierde el mando, los botones del Square dejan de llegarle por la app y el rodillo sigue conectado por wifi sin cambios

#### Scenario: Terminar la sesión
- **WHEN** se termina la sesión con el mando conectado a Zwift
- **THEN** el mando deja de anunciarse y Zwift lo da por desconectado

#### Scenario: Bluetooth apagado
- **WHEN** «Mando de Zwift» está activado con el modo de siempre y se apaga el Bluetooth del móvil
- **THEN** el mando deja de anunciarse y, al volver a encender el Bluetooth, se anuncia otra vez sin tocar nada

#### Scenario: Móvil sin modo periférico
- **WHEN** «Mando de Zwift» está activado con el modo de siempre en un móvil que no puede anunciarse como dispositivo Bluetooth
- **THEN** la app no anuncia el mando, Dispositivos dice «No disponible» en su fila y el resto de la sesión funciona igual
