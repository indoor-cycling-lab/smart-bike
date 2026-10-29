# Spec Delta

## ADDED Requirements

### Requirement: Nombre también por Bluetooth
La sección «Nombre ante la app» SHALL tener la casilla «Usar el nombre también por Bluetooth», desmarcada por defecto, en «Wifi + Bluetooth» y «Solo Bluetooth», y SHALL NOT mostrarla en «Solo wifi (modo Zwift Ride)». Su ayuda SHALL decir que, mientras la app publica por Bluetooth, el móvil se llama así para todos los dispositivos y que al terminar recupera su nombre, nombrando los dos. Un cambio SHALL aplicarse al momento y guardarse.

#### Scenario: Valor por defecto
- **WHEN** se abre Conexión por primera vez
- **THEN** la casilla está desmarcada

#### Scenario: Ayuda con los nombres
- **WHEN** el móvil se llama «OnePlus 8», «Smart Bike» está elegido y se lee la ayuda de la casilla
- **THEN** dice que, mientras la app publica por Bluetooth, el móvil se llamará «Smart Bike» también para los auriculares, el coche y otros dispositivos, y que al terminar vuelve a llamarse «OnePlus 8»

#### Scenario: Marcarla rodando
- **WHEN** se rueda con «Solo Bluetooth» y Zwift conectado, y se marca la casilla
- **THEN** la casilla queda marcada y guardada, la marcha y lo que simula el Neo no cambian, y la ayuda del rodillo dice que hay que emparejar de nuevo con el nombre nuevo

#### Scenario: Con Solo wifi
- **WHEN** el modo es «Solo wifi (modo Zwift Ride)»
- **THEN** la casilla no se muestra y su valor se conserva para cuando se vuelva a un modo con Bluetooth

#### Scenario: No se pudo renombrar
- **WHEN** la casilla está marcada y el móvil no se pudo renombrar
- **THEN** bajo la casilla se ve un aviso, no solo por color, que dice que no se pudo cambiar el nombre y que se usa «OnePlus 8»

#### Scenario: Exportar e importar
- **WHEN** se exporta la configuración con la casilla marcada y se importa en otro móvil
- **THEN** la casilla queda marcada y el nombre que se recupera al terminar es el del móvil nuevo

## MODIFIED Requirements

### Requirement: Nombre ante la app
La pantalla SHALL tener un selector «Nombre ante la app» con «Smart Bike», por defecto, y «KICKR BIKE PRO», con una ayuda que diga que la app de entrenamiento verá la app con ese nombre, que «KICKR BIKE PRO» es una prueba y que hay que emparejar de nuevo al cambiar. SHALL poder cambiarse con cualquiera de los modos con wifi y sin wifi. Con «Solo Bluetooth», el selector SHALL mostrarse solo con «Usar el nombre también por Bluetooth» marcada; sin ella, el nombre es el del móvil.

#### Scenario: Valor por defecto
- **WHEN** se abre Conexión por primera vez
- **THEN** el nombre es «Smart Bike»

#### Scenario: Elegir KICKR BIKE PRO
- **WHEN** se elige «KICKR BIKE PRO»
- **THEN** queda elegido, se guarda y la ayuda dice que la app de entrenamiento verá un «KICKR BIKE PRO», que es una prueba y que hay que emparejarlo de nuevo

#### Scenario: Nombre guardado
- **WHEN** se elige «KICKR BIKE PRO» y se cierra y se vuelve a abrir la app
- **THEN** sigue elegido y la app se anuncia como «KICKR BIKE PRO» desde el primer momento

#### Scenario: Con cualquier modo
- **WHEN** el modo es «Wifi + Bluetooth» o «Solo wifi (modo Zwift Ride)», con o sin wifi, y se cambia el nombre
- **THEN** el nombre se puede cambiar y queda guardado

#### Scenario: Con el modo por Bluetooth
- **WHEN** el modo es «Solo Bluetooth» y «Usar el nombre también por Bluetooth» está desmarcada
- **THEN** se ve la casilla, el selector no se muestra y el nombre elegido antes se conserva

#### Scenario: Con el modo por Bluetooth y la casilla marcada
- **WHEN** el modo es «Solo Bluetooth» y se marca «Usar el nombre también por Bluetooth»
- **THEN** aparece el selector con el nombre elegido antes, y se puede cambiar

#### Scenario: Nombre en la ayuda
- **WHEN** el nombre elegido es «KICKR BIKE PRO» y se abre Conexión
- **THEN** la ayuda del rodillo dice que se elija el «KICKR BIKE PRO» con sus cuatro caracteres, tal como lo lista la app de entrenamiento

#### Scenario: Exportar e importar
- **WHEN** se exporta la configuración con «KICKR BIKE PRO» elegido y se importa en otro móvil
- **THEN** el nombre queda elegido y el móvil nuevo usa su propia identidad, no la del móvil anterior

### Requirement: Estado del rodillo
La fila del rodillo SHALL decir «Conectado» si la app de entrenamiento recibe sus datos o lo controla, «Esperando» si se publica y no lo usa, «Sin wifi» o «Sin Bluetooth» si falta el transporte del modo, «No disponible» si el móvil no puede publicarlo por Bluetooth y «No visible» si el anuncio por wifi no responde. Con el rodillo configurado sin conexión, SHALL añadir que se está reconectando. Por Bluetooth, la ayuda SHALL nombrar el nombre con el que se anuncia.

#### Scenario: Esperando con el nombre
- **WHEN** el modo es «Wifi + Bluetooth», hay wifi y ninguna app de entrenamiento está conectada
- **THEN** el rodillo dice «Esperando» y explica que en la app de entrenamiento se elige «Smart Bike» como rodillo, potencia y cadencia, o el nombre elegido, tal como lo lista

#### Scenario: Sin wifi
- **WHEN** el modo usa wifi para el rodillo y el móvil no tiene wifi
- **THEN** el rodillo dice «Sin wifi» y pide conectar el móvil a la misma red wifi que la app de entrenamiento

#### Scenario: Solo Bluetooth esperando
- **WHEN** el modo es «Solo Bluetooth», el Bluetooth está encendido, ninguna app está conectada y «Usar el nombre también por Bluetooth» está desmarcada
- **THEN** el rodillo dice «Esperando» y explica que se elige el móvil por su nombre Bluetooth (por ejemplo «OnePlus 8»)

#### Scenario: Solo Bluetooth esperando con el nombre elegido
- **WHEN** el modo es «Solo Bluetooth», el Bluetooth está encendido, ninguna app está conectada y el móvil está renombrado a «Smart Bike»
- **THEN** el rodillo dice «Esperando» y explica que se elige «Smart Bike»

#### Scenario: Bluetooth apagado con Solo Bluetooth
- **WHEN** el modo es «Solo Bluetooth» y el Bluetooth está apagado
- **THEN** el rodillo dice «Sin Bluetooth», con el aviso de Bluetooth apagado y «Activar Bluetooth»

#### Scenario: Neo reconectando
- **WHEN** la app de entrenamiento está conectada al rodillo y se pierde la conexión con el Neo
- **THEN** el rodillo sigue «Conectado» y añade que el Neo se está reconectando, y la app de entrenamiento recibe potencia y cadencia 0

### Requirement: Estado del controlador
La fila del controlador SHALL decir «Conectado» tras el saludo de la app de entrenamiento al controlador, «Esperando» si se publica y no lo ha saludado, «Sin Bluetooth» o «Sin wifi» si falta el transporte por el que va, y «No disponible» si el móvil no puede publicarlo por Bluetooth. Con el Square configurado sin conexión, SHALL añadir que se está reconectando. Por Bluetooth, la ayuda SHALL nombrar el nombre con el que se anuncia.

#### Scenario: Mando esperando por Bluetooth
- **WHEN** el modo es «Wifi + Bluetooth», el Square está configurado, el Bluetooth encendido, ningún controlador saludado y «Usar el nombre también por Bluetooth» desmarcada
- **THEN** el controlador dice «Esperando» y explica que en la lista de mandos se elige el móvil por su nombre Bluetooth y que el Square no se empareja en la app de entrenamiento

#### Scenario: Mando esperando con el nombre elegido
- **WHEN** el modo es «Wifi + Bluetooth», el Square está configurado, ningún controlador saludado y el móvil está renombrado a «Smart Bike»
- **THEN** el controlador dice «Esperando» y explica que en la lista de mandos se elige «Smart Bike»

#### Scenario: Mando dentro del mismo dispositivo
- **WHEN** el modo es «Solo wifi (modo Zwift Ride)» o «Solo Bluetooth» y el Square está configurado
- **THEN** el controlador dice que va dentro del mismo dispositivo que el rodillo y que se elige ese dispositivo también como controlador

#### Scenario: Mando conectado
- **WHEN** la app de entrenamiento ha saludado al controlador
- **THEN** el controlador dice «Conectado»

#### Scenario: Square reconectando
- **WHEN** el controlador está «Conectado» y se pierde la conexión con el Square
- **THEN** el controlador sigue «Conectado» y añade que el Square se está reconectando

#### Scenario: Mando no disponible
- **WHEN** el modo es «Wifi + Bluetooth», el Square está configurado y el móvil no puede anunciarse como dispositivo Bluetooth
- **THEN** el controlador dice «No disponible» y explica que este móvil no puede publicar el controlador

#### Scenario: Bluetooth apagado con el mando aparte
- **WHEN** el modo es «Wifi + Bluetooth», el Square está configurado y el Bluetooth está apagado
- **THEN** el controlador dice «Sin Bluetooth» y el rodillo conserva su estado por wifi

### Requirement: Ajustes de conexión guardados
El modo, el nombre ante la app y «Usar el nombre también por Bluetooth» SHALL guardarse con los demás ajustes y seguir vigentes al reabrir la app. Si lo guardado no se puede leer, SHALL empezar con «Wifi + Bluetooth», «Smart Bike» y la casilla desmarcada. Una configuración anterior, con ajustes por plataforma, SHALL conservar lo que no es de conexión y empezar la conexión con los valores por defecto. Una configuración sin la casilla SHALL leerse con la casilla desmarcada.

#### Scenario: Valores guardados
- **WHEN** se elige «Solo wifi (modo Zwift Ride)» y «KICKR BIKE PRO» y se cierra y se vuelve a abrir la app
- **THEN** el modo es «Solo wifi (modo Zwift Ride)» y el nombre «KICKR BIKE PRO»

#### Scenario: Lo guardado no se puede leer
- **WHEN** la configuración guardada no se puede leer
- **THEN** la app empieza con «Wifi + Bluetooth» y «Smart Bike», y hay que emparejar de nuevo en la app de entrenamiento

#### Scenario: Configuración de la versión anterior
- **WHEN** se abre la app, o se importa una copia, con una configuración guardada por plataforma
- **THEN** se conservan las marchas, los botones, la pantalla y los demás ajustes, el modo y el nombre toman los valores por defecto, y hay que emparejar de nuevo

#### Scenario: Configuración sin la casilla
- **WHEN** se abre la app, o se importa una copia, con una configuración guardada antes de este ajuste
- **THEN** se conservan el modo y el nombre, y «Usar el nombre también por Bluetooth» está desmarcada
