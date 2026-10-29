# platform-settings Specification

## Purpose
Define la pestaña «Plataformas»: lo que la app simula y publica ante las apps de indoor cycling. El usuario elige una plataforma y un modo de conexión compatible, sin combinaciones que haya que acertar, y ve el estado de esa conexión.

## Requirements

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

### Requirement: Tabla de compatibilidad
Cada modo de conexión que la app ofrece SHALL tener documentado con qué apps de entrenamiento está validado con el equipo real y qué queda «a validar», y la pantalla SHALL NOT presentar como confirmado lo que no lo está. Un modo SHALL ofrecerse solo si está implementado.

#### Scenario: Modos de Zwift
- **WHEN** se consulta la compatibilidad
- **THEN** «Wifi + Bluetooth» y «Solo wifi (modo Zwift Ride)» constan como validados con Zwift en PC en lo que recogen las specs `platform-bridge`, `zwift-ride` y `zwift-controller`, «Solo Bluetooth» consta como validado solo en lo que recoge la spec `ftms-bluetooth`, y lo marcado «a validar» ahí sigue marcado

#### Scenario: Otra app probada
- **WHEN** se prueba un modo con otra app de entrenamiento con el equipo real
- **THEN** la compatibilidad del modo añade esa app con el resultado comprobado, y la pantalla lo muestra

#### Scenario: Modo sin implementar
- **WHEN** un modo no está implementado
- **THEN** no aparece en la pantalla

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
La pantalla SHALL NOT tener un interruptor para el controlador: el controlador se publica según el modo siempre que haya un controlador configurado en Dispositivos (spec `platform-bridge`). El servicio SHALL llamarse «Controlador Zwift» y llevar el icono de Zwift, porque emula el controlador de Zwift; fuera de ese nombre, «protocolo de Zwift» SHALL aparecer solo en su ayuda de emparejamiento y en la compatibilidad.

#### Scenario: Sin interruptor en ningún modo
- **WHEN** se recorren los tres modos en Conexión
- **THEN** ninguno muestra un interruptor del controlador

#### Scenario: Nombre del mando
- **WHEN** se ve la fila del controlador en el estado
- **THEN** se llama «Controlador Zwift», lleva el icono de Zwift y su ayuda de emparejamiento dice que usa el protocolo de los mandos de Zwift

### Requirement: Estado de cada servicio
La sección de estado SHALL mostrar una fila por servicio publicado (Rodillo, con potencia, cadencia y resistencia; Controlador Zwift; Pulso), cada una con por dónde se publica, su estado en palabra y forma, y cómo emparejarlo o qué falta. El estado SHALL reflejar si la app de entrenamiento usa ese servicio, no solo si está conectada al dispositivo.

#### Scenario: Rodillo conectado y pulso sin usar
- **WHEN** la app de entrenamiento está conectada y recibe potencia, hay pulsómetro configurado y no ha elegido la app como fuente de pulso
- **THEN** el rodillo dice «Conectado» y el pulso dice «Esperando», con la ayuda de elegir el pulso en la app de entrenamiento

#### Scenario: Estado sin color
- **WHEN** se ve la sección de estado en escala de grises
- **THEN** cada estado se distingue por la palabra y la forma

#### Scenario: Por dónde va cada servicio
- **WHEN** el modo es «Wifi + Bluetooth»
- **THEN** el rodillo y el pulso dicen «por wifi» y el controlador «por Bluetooth»

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

### Requirement: Estado del pulso
La fila del pulso SHALL decir «Enviando» si la app de entrenamiento está suscrita al pulso y hay pulso válido, «Sin lectura» si está suscrita y no hay pulso válido, y «Esperando» si se publica y no está suscrita. Si falta el transporte del rodillo, SHALL decir lo mismo que el rodillo («Sin wifi», «Sin Bluetooth», «No visible»). SHALL NOT mostrar los bpm.

#### Scenario: Enviando
- **WHEN** la banda da 132 bpm y la app de entrenamiento está suscrita al pulso
- **THEN** el pulso dice «Enviando», sin mostrar los bpm

#### Scenario: Banda sin contacto
- **WHEN** la app de entrenamiento está suscrita al pulso y pasan 5 s sin lectura válida
- **THEN** el pulso dice «Sin lectura» y explica que se compruebe la banda

#### Scenario: Sin wifi
- **WHEN** el modo es «Wifi + Bluetooth», hay pulsómetro configurado y el móvil no tiene wifi
- **THEN** el pulso dice «Sin wifi», como el rodillo

#### Scenario: Pulso sin elegir
- **WHEN** hay pulsómetro configurado y la app de entrenamiento no se ha suscrito al pulso
- **THEN** el pulso dice «Esperando» y explica que se elige el mismo dispositivo que el rodillo como fuente de pulso

### Requirement: Servicio no ofrecido
Si falta el dispositivo configurado de un servicio (rodillo, Square o pulsómetro), su fila SHALL decir «No se ofrece», explicar qué dispositivo falta y llevar a Dispositivos. Sin rodillo configurado, las tres filas SHALL decir «No se ofrece» y un aviso SHALL decir que la app no publica nada hasta configurar un rodillo.

#### Scenario: Sin pulsómetro
- **WHEN** no hay pulsómetro configurado
- **THEN** el pulso dice «No se ofrece · sin pulsómetro» y tocarlo abre Dispositivos

#### Scenario: Sin Square
- **WHEN** no hay Square configurado y el modo es «Wifi + Bluetooth» o «Solo Bluetooth»
- **THEN** el controlador dice «No se ofrece · sin controlador» y el rodillo y el pulso muestran su estado

#### Scenario: Sin Square con Solo wifi
- **WHEN** no hay Square configurado y el modo es «Solo wifi (modo Zwift Ride)»
- **THEN** el controlador muestra su estado, porque en este modo va siempre dentro del rodillo, y su ayuda dice que sin Square no llega ningún botón y lleva a Dispositivos

#### Scenario: Primer uso
- **WHEN** se abre Conexión sin ningún dispositivo configurado
- **THEN** las tres filas dicen «No se ofrece» y el aviso dice que la app no publica nada hasta configurar un rodillo en Dispositivos

#### Scenario: Configurar un dispositivo
- **WHEN** Conexión está abierta, se recuerda un pulsómetro en Dispositivos y se vuelve a Conexión
- **THEN** el pulso deja de decir «No se ofrece» y pasa a «Esperando», con la ayuda de que la app de entrenamiento puede tener que emparejar de nuevo el dispositivo

### Requirement: Compatibilidad en cada modo
Cada modo SHALL mostrar con qué apps de entrenamiento se ha probado y con qué resultado («validado», «validado en parte»), solo de lo probado con el equipo real. Los modos con FTMS SHALL añadir que otras apps con FTMS están a validar, y «Solo wifi (modo Zwift Ride)» que es solo para Zwift.

#### Scenario: Etiquetas de hoy
- **WHEN** se abre Conexión
- **THEN** «Wifi + Bluetooth» dice «Zwift: validado», «Solo Bluetooth» y «Solo wifi (modo Zwift Ride)» dicen «Zwift: validado en parte», y ninguno nombra otra app como compatible

#### Scenario: Otras apps con FTMS
- **WHEN** se lee «Wifi + Bluetooth» o «Solo Bluetooth»
- **THEN** dice que otras apps con FTMS están a validar

### Requirement: Aviso de no visible en Conexión
Cuando el anuncio por wifi no responde (spec `platform-bridge`), Conexión SHALL mostrar arriba el aviso de apagar y volver a encender el wifi del móvil y, si sigue igual, reiniciarlo, con el rodillo «No visible», y SHALL quitarlo en cuanto el anuncio vuelva a responder. Dispositivos SHALL NOT mostrar este estado.

#### Scenario: Anuncio que no responde
- **WHEN** el anuncio no responde en la red ni tras volver a anunciarse
- **THEN** Conexión muestra el aviso y el rodillo «No visible», y las marchas siguen funcionando como sin app de entrenamiento

#### Scenario: Vuelve a responder
- **WHEN** el rodillo está «No visible» y el anuncio vuelve a responder
- **THEN** el rodillo pasa a «Esperando» y el aviso desaparece sin tocar nada

### Requirement: Conexión desde Rodaje
Tocar el estado de la publicación en Rodaje SHALL abrir Conexión; tocar la barra de conexiones SHALL abrir Dispositivos. Desde Conexión, «Rodar» o Atrás SHALL volver a Rodaje.

#### Scenario: Tocar el estado
- **WHEN** se toca el estado de la publicación en Rodaje
- **THEN** se abre Conexión con la barra inferior, y Atrás vuelve a Rodaje con la sesión en marcha

#### Scenario: Tocar el icono del Neo
- **WHEN** se toca el icono del Neo en Rodaje
- **THEN** se abre Dispositivos

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
