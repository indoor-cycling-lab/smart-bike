## RENAMED Requirements

- FROM: `### Requirement: Mando sin interruptor`
- TO: `### Requirement: Controlador sin interruptor`

- FROM: `### Requirement: Estado del mando`
- TO: `### Requirement: Estado del controlador`

## MODIFIED Requirements

### Requirement: Pestaña Conexión
La app SHALL tener una pestaña «Conexión» en la barra inferior, sin botón de volver. Su introducción SHALL decir que la app publica sus servicios y que la app de entrenamiento (Zwift, MyWhoosh, Rouvy…) se conecta a ellos. De arriba abajo SHALL mostrar los avisos, el estado de cada servicio, el modo y, si el modo lo admite, «Nombre ante la app». Un cambio SHALL aplicarse al momento, también rodando, y guardarse.

#### Scenario: Abrir Conexión
- **WHEN** se toca «Conexión» en la barra inferior
- **THEN** se ve la introducción, la sección de estado con el rodillo, el controlador Zwift y el pulso, la sección del modo con «Wifi + Bluetooth» elegido y «Nombre ante la app», sin botón de volver ni elección de plataforma

#### Scenario: Valores por defecto
- **WHEN** se abre Conexión por primera vez
- **THEN** el modo es «Wifi + Bluetooth» y el nombre es «Smart Bike»

#### Scenario: Cambio rodando
- **WHEN** se rueda con el Neo y el Square y se cambia de modo en Conexión
- **THEN** la marcha y lo que simula el Neo no cambian y la app pasa a publicar el modo nuevo, como en «Cambio de modo o de nombre con la sesión abierta» (spec `platform-bridge`)

### Requirement: Tres modos de conexión
La pantalla SHALL ofrecer, como única elección y con un solo modo elegido a la vez, «Wifi + Bluetooth» (recomendado), «Solo Bluetooth» y «Solo wifi (modo Zwift Ride)». Cada modo SHALL decir por dónde van el rodillo, el pulso y el controlador, con el detalle técnico (FTMS, DirCon) como texto secundario. SHALL NOT ofrecer elegir plataforma ni modos sin implementar.

#### Scenario: Modos ofrecidos
- **WHEN** se abre Conexión
- **THEN** se ofrecen los tres modos, y solo «Wifi + Bluetooth» lleva la marca de recomendado

#### Scenario: Wifi + Bluetooth
- **WHEN** se lee el modo «Wifi + Bluetooth»
- **THEN** dice que el rodillo y el pulso van por wifi (DirCon, FTMS) y el controlador por Bluetooth

#### Scenario: Solo Bluetooth
- **WHEN** se elige «Solo Bluetooth»
- **THEN** queda elegido, se guarda y su texto dice que el rodillo, el pulso y el controlador van por Bluetooth (FTMS) en un solo dispositivo, que no hace falta wifi y que hay que emparejar de nuevo

#### Scenario: Solo wifi
- **WHEN** se elige «Solo wifi (modo Zwift Ride)»
- **THEN** queda elegido, se guarda y su texto dice que todo va por wifi (DirCon) en un solo dispositivo, que la app de entrenamiento mostrará su propia marcha, que no es la de la app, y que hay que emparejar de nuevo

#### Scenario: Un solo modo elegido
- **WHEN** está elegido «Wifi + Bluetooth» y se toca «Solo Bluetooth»
- **THEN** pasa a estar elegido el segundo y deja de estarlo el primero

#### Scenario: Móvil sin modo periférico
- **WHEN** el móvil no puede anunciarse como dispositivo Bluetooth
- **THEN** los tres modos se pueden elegir, y «Wifi + Bluetooth» y «Solo Bluetooth» dicen que este móvil no puede publicar por Bluetooth el controlador ni, en el segundo, el rodillo

### Requirement: Controlador sin interruptor
La pantalla SHALL NOT tener un interruptor para el controlador: el controlador se publica según el modo siempre que haya un controlador recordado en Dispositivos (spec `platform-bridge`). El servicio SHALL llamarse «Controlador Zwift» y llevar el icono de Zwift, porque emula el controlador de Zwift; fuera de ese nombre, «protocolo de Zwift» SHALL aparecer solo en su ayuda de emparejamiento y en la compatibilidad.

#### Scenario: Sin interruptor en ningún modo
- **WHEN** se recorren los tres modos en Conexión
- **THEN** ninguno muestra un interruptor del controlador

#### Scenario: Nombre del mando
- **WHEN** se ve la fila del controlador en el estado
- **THEN** se llama «Controlador Zwift», lleva el icono de Zwift y su ayuda de emparejamiento dice que usa el protocolo de los mandos de Zwift

### Requirement: Estado de cada servicio
La sección de estado SHALL mostrar una fila por servicio publicado (Rodillo, con potencia, cadencia y resistencia; Controlador Zwift; Pulso), cada una con por dónde se publica, su estado en palabra y forma, y cómo emparejarlo o qué falta. El estado SHALL reflejar si la app de entrenamiento usa ese servicio, no solo si está conectada al dispositivo.

#### Scenario: Rodillo conectado y pulso sin usar
- **WHEN** la app de entrenamiento está conectada y recibe potencia, hay pulsómetro recordado y no ha elegido la app como fuente de pulso
- **THEN** el rodillo dice «Conectado» y el pulso dice «Esperando», con la ayuda de elegir el pulso en la app de entrenamiento

#### Scenario: Estado sin color
- **WHEN** se ve la sección de estado en escala de grises
- **THEN** cada estado se distingue por la palabra y la forma

#### Scenario: Por dónde va cada servicio
- **WHEN** el modo es «Wifi + Bluetooth»
- **THEN** el rodillo y el pulso dicen «por wifi» y el controlador «por Bluetooth»

### Requirement: Estado del controlador
La fila del controlador SHALL decir «Conectado» tras el saludo de la app de entrenamiento al controlador, «Esperando» si se publica y no lo ha saludado, «Sin Bluetooth» o «Sin wifi» si falta el transporte por el que va, y «No disponible» si el móvil no puede publicarlo por Bluetooth. Con el Square recordado sin conexión, SHALL añadir que se está reconectando.

#### Scenario: Mando esperando por Bluetooth
- **WHEN** el modo es «Wifi + Bluetooth», el Square está recordado, el Bluetooth encendido y ningún controlador saludado
- **THEN** el controlador dice «Esperando» y explica que en la lista de mandos se elige el móvil por su nombre Bluetooth y que el Square no se empareja en la app de entrenamiento

#### Scenario: Mando dentro del mismo dispositivo
- **WHEN** el modo es «Solo wifi (modo Zwift Ride)» o «Solo Bluetooth» y el Square está recordado
- **THEN** el controlador dice que va dentro del mismo dispositivo que el rodillo y que se elige ese dispositivo también como controlador

#### Scenario: Mando conectado
- **WHEN** la app de entrenamiento ha saludado al controlador
- **THEN** el controlador dice «Conectado»

#### Scenario: Square reconectando
- **WHEN** el controlador está «Conectado» y se pierde la conexión con el Square
- **THEN** el controlador sigue «Conectado» y añade que el Square se está reconectando

#### Scenario: Mando no disponible
- **WHEN** el modo es «Wifi + Bluetooth», el Square está recordado y el móvil no puede anunciarse como dispositivo Bluetooth
- **THEN** el controlador dice «No disponible» y explica que este móvil no puede publicar el controlador

#### Scenario: Bluetooth apagado con el mando aparte
- **WHEN** el modo es «Wifi + Bluetooth», el Square está recordado y el Bluetooth está apagado
- **THEN** el controlador dice «Sin Bluetooth» y el rodillo conserva su estado por wifi

### Requirement: Servicio no ofrecido
Si falta el dispositivo recordado de un servicio (rodillo, Square o pulsómetro), su fila SHALL decir «No se ofrece», explicar qué dispositivo falta y llevar a Dispositivos. Sin rodillo recordado, las tres filas SHALL decir «No se ofrece» y un aviso SHALL decir que la app no publica nada hasta recordar un rodillo.

#### Scenario: Sin pulsómetro
- **WHEN** no hay pulsómetro recordado
- **THEN** el pulso dice «No se ofrece · sin pulsómetro» y tocarlo abre Dispositivos

#### Scenario: Sin Square
- **WHEN** no hay Square recordado y el modo es «Wifi + Bluetooth» o «Solo Bluetooth»
- **THEN** el controlador dice «No se ofrece · sin controlador» y el rodillo y el pulso muestran su estado

#### Scenario: Sin Square con Solo wifi
- **WHEN** no hay Square recordado y el modo es «Solo wifi (modo Zwift Ride)»
- **THEN** el controlador muestra su estado, porque en este modo va siempre dentro del rodillo, y su ayuda dice que sin Square no llega ningún botón y lleva a Dispositivos

#### Scenario: Primer uso
- **WHEN** se abre Conexión sin ningún dispositivo recordado
- **THEN** las tres filas dicen «No se ofrece» y el aviso dice que la app no publica nada hasta recordar un rodillo en Dispositivos

#### Scenario: Recordar un dispositivo
- **WHEN** Conexión está abierta, se recuerda un pulsómetro en Dispositivos y se vuelve a Conexión
- **THEN** el pulso deja de decir «No se ofrece» y pasa a «Esperando», con la ayuda de que la app de entrenamiento puede tener que emparejar de nuevo el dispositivo
