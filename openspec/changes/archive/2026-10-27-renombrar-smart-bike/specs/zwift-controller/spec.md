## MODIFIED Requirements

### Requirement: Mando reconocido por Zwift
Con el mando anunciado y una fuente de potencia ya emparejada en Zwift, Zwift SHALL listar el móvil en la pantalla de emparejamiento como mando y SHALL poder emparejarlo. Zwift ve dos dispositivos de la app: el rodillo, la potencia y la cadencia por wifi y el mando por Bluetooth.

#### Scenario: Emparejar el mando en Zwift para PC
- **WHEN** el mando está anunciado, «Smart Bike» está emparejado por wifi como potencia, cadencia y rodillo, y el usuario abre la lista de mandos de Zwift
- **THEN** el móvil aparece con su nombre Bluetooth, se puede emparejar y Dispositivos muestra el mando «Conectado» tras el saludo de Zwift

#### Scenario: Sin el Square emparejado en Zwift
- **WHEN** el Square no está emparejado en Zwift y el mando está conectado
- **THEN** se navega, se gira, se frena, se usa el powerup y se da Ride On en Zwift con los botones del Square

#### Scenario: Otras conexiones Bluetooth del móvil
- **WHEN** el mando está conectado a Zwift mientras la app está conectada al Neo y al Square, y el Square se desconecta y vuelve a conectarse
- **THEN** Zwift sigue con el mando conectado, sin repetir el emparejamiento, y los botones vuelven a llegarle

#### Scenario: Mando por wifi
- **WHEN** Zwift empareja «Smart Bike» por wifi
- **THEN** aparece como potencia, cadencia y rodillo controlable, pero no en la lista de mandos: el mando aparece aparte, con el nombre Bluetooth del móvil

#### Scenario: Rodillo sin Neo
- **WHEN** Zwift está conectado al rodillo y al mando y el Neo no está conectado
- **THEN** Zwift recibe potencia y cadencia 0 y los botones del Square siguen llegando a Zwift por el mando

### Requirement: Mando por wifi dentro del rodillo
Con el modo «Todo por wifi (modo Zwift Ride)», la app SHALL ofrecer el mando en el mismo dispositivo que el rodillo, la potencia y la cadencia, de modo que Zwift vea un solo dispositivo, «Smart Bike», como fuente de potencia y como mando. En ese caso la app SHALL NOT anunciarse por Bluetooth como mando. Las funciones de Zwift de los botones del Square SHALL llegar a Zwift y comportarse como por Bluetooth: navegar, girar, frenar, powerup y Ride On, las palancas sueltas a 0 y el mando activo sin pulsar nada.

#### Scenario: Emparejar el mando por wifi
- **WHEN** el modo es «Todo por wifi», Zwift empareja «Smart Bike» por wifi como potencia y como mando y la app tiene wifi
- **THEN** Zwift muestra «Smart Bike» como rodillo y como mando a la vez, y Dispositivos muestra el mando «Conectado» tras el saludo de Zwift

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
