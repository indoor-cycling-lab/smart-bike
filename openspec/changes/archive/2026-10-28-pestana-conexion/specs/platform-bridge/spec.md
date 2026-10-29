## ADDED Requirements

### Requirement: Lo publicado según los dispositivos recordados
En cualquier modo, la app SHALL publicar el rodillo solo con un rodillo recordado en Dispositivos, el mando solo con un Square recordado y el pulso solo con un pulsómetro recordado. Sin rodillo recordado SHALL NOT publicar nada. Que un dispositivo recordado esté desconectado SHALL NOT cambiar lo que se publica: solo su estado.

#### Scenario: Sin rodillo recordado
- **WHEN** se abre la app sin rodillo recordado, con el Square y el pulsómetro recordados
- **THEN** ninguna app de entrenamiento encuentra la app, ni por wifi ni por Bluetooth, y las marchas funcionan como sin app de entrenamiento

#### Scenario: Rodillo y Square sin pulsómetro
- **WHEN** hay un Neo y un Square recordados, ningún pulsómetro, y se abre el emparejamiento de Zwift
- **THEN** «Smart Bike» aparece como potencia, cadencia, rodillo y mando, y no en la lista de pulso

#### Scenario: Neo dormido
- **WHEN** el Neo recordado está dormido y la app de entrenamiento se conecta
- **THEN** la app sigue publicando el rodillo y envía potencia y cadencia 0 hasta que el Neo se conecta

#### Scenario: Square que se duerme
- **WHEN** la app de entrenamiento está conectada al mando y el Square se desconecta un momento
- **THEN** el mando se sigue publicando, la app de entrenamiento no lo pierde y los botones vuelven a llegar al reconectarse el Square

### Requirement: Recordar u olvidar cambia lo publicado
Al recordar u olvidar un rodillo, un Square o un pulsómetro, la app SHALL empezar o dejar de publicar ese servicio sin cambiar la marcha ni lo que simula el Neo, volviendo a anunciarse si hace falta. La app de entrenamiento SHALL poder tener que reconectarse o emparejar de nuevo el dispositivo para ver el cambio.

#### Scenario: Recordar un pulsómetro rodando
- **WHEN** se rueda con Zwift conectado por wifi y se recuerda un pulsómetro en Dispositivos
- **THEN** la app empieza a publicar el pulso, la marcha y lo que simula el Neo no cambian, y Zwift lista «Smart Bike» como fuente de pulso, tras reconectarse o emparejarlo de nuevo si hace falta

#### Scenario: Olvidar el Square
- **WHEN** Zwift está conectado al mando por Bluetooth en «Wifi + Bluetooth» y se olvida el Square
- **THEN** el mando deja de anunciarse, Zwift lo pierde y el rodillo sigue conectado por wifi sin cambios

#### Scenario: Olvidar el rodillo
- **WHEN** Zwift está conectado y se olvida el Neo
- **THEN** la app deja de publicar todo, Zwift pierde la conexión y la marcha sigue igual

## MODIFIED Requirements

### Requirement: Plataforma no visible
Si el anuncio sigue sin responder después de volver a anunciarse, la app SHALL mostrar la plataforma como «No visible» y decir qué hacer: apagar y volver a encender el wifi del móvil y, si sigue igual, reiniciar el móvil. Las marchas SHALL seguir funcionando como sin la plataforma. En cuanto el anuncio vuelva a responder, la app SHALL volver a «Esperando» sin que el usuario haga nada más.

#### Scenario: Anuncio que no responde
- **WHEN** se abre la sesión con wifi, Android da el anuncio por registrado y el anuncio no responde en la red, ni siquiera tras volver a anunciarse
- **THEN** en unos 15 s Conexión muestra el rodillo como «No visible», con el aviso de apagar y volver a encender el wifi del móvil y, si sigue igual, reiniciarlo; las marchas siguen funcionando con la pendiente base

#### Scenario: Reiniciar el wifi
- **WHEN** la plataforma está «No visible» y el usuario apaga y vuelve a encender el wifi del móvil
- **THEN** al volver el wifi, la app se anuncia de nuevo, lo comprueba y, si responde, la plataforma vuelve a «Esperando» y el aviso desaparece sin tocar la app

#### Scenario: El anuncio deja de responder más tarde
- **WHEN** el anuncio respondía al abrir la sesión, la plataforma no se ha conectado y, más tarde, deja de responder
- **THEN** antes de 2 minutos la plataforma pasa a «No visible» con el mismo aviso

### Requirement: Información del dispositivo hacia la plataforma
Con el rodillo de la app emparejado por wifi, en cualquiera de los modos de conexión, la app SHALL ofrecer a la plataforma, además del fabricante, el modelo y la versión de la app, un número de serie, una versión de firmware y una versión de hardware, y SHALL contestar a su lectura sin error. El número de serie SHALL ser el de la identidad del dispositivo y SHALL conservarse entre sesiones.

#### Scenario: La plataforma lee la información al emparejar
- **WHEN** Zwift empareja el rodillo por wifi y lee la información del dispositivo
- **THEN** el registro de sesión muestra la lectura de la serie, el firmware y el hardware, cada una contestada con un valor, y Zwift termina de emparejar sin mensajes de error

#### Scenario: Misma serie en cada sesión
- **WHEN** se cierran y se vuelven a abrir la app y Zwift con el rodillo ya emparejado
- **THEN** Zwift lee el mismo número de serie y reconoce el dispositivo sin volver a emparejarlo

#### Scenario: Dos modos, dos identidades
- **WHEN** se cambia entre «Wifi + Bluetooth» y «Solo wifi (modo Zwift Ride)»
- **THEN** cada modo ofrece la serie de su propia identidad, y la de uno no aparece en el otro

#### Scenario: Con KICKR BIKE PRO
- **WHEN** «KICKR BIKE PRO» está elegido en «Nombre ante la app» y Zwift lee la información del dispositivo
- **THEN** Zwift recibe la serie de esa identidad, que es la misma en cada sesión, y el resto de lo observable no cambia

#### Scenario: Sin Neo conectado
- **WHEN** Zwift lee la información del dispositivo con el Neo desconectado
- **THEN** Zwift la recibe igual, porque no depende del Neo

### Requirement: Rodillo visible en la plataforma por wifi
Mientras dura la sesión, hay un rodillo recordado, el modo usa wifi para el rodillo y el móvil está conectado a una red wifi, la app SHALL anunciarse en esa red con el nombre elegido en Conexión («Smart Bike» por defecto) como un rodillo que la plataforma lista como rodillo controlable, fuente de potencia y fuente de cadencia, sin que el usuario active nada en la app.

#### Scenario: Emparejar en Zwift para PC
- **WHEN** la app está abierta con un Neo recordado, el móvil y el PC de Zwift están en la misma red wifi y el usuario abre la pantalla de emparejamiento de Zwift
- **THEN** «Smart Bike» aparece por wifi en las listas de potencia, cadencia y rodillo controlable, y se puede elegir en las tres

#### Scenario: La plataforma vuelve a otra sesión
- **WHEN** «Smart Bike» se emparejó en Zwift en una sesión anterior y se abren la app y Zwift otra vez
- **THEN** Zwift lo reconoce como el mismo dispositivo y se conecta sin volver a elegirlo

#### Scenario: Neo sin conexión
- **WHEN** la plataforma está conectada a la app y el Neo recordado todavía no se ha conectado
- **THEN** la plataforma sigue viendo «Smart Bike» conectado, recibe potencia y cadencia 0 y, al conectarse el Neo, empieza a recibir las suyas

### Requirement: Pulso hacia la plataforma
Con un pulsómetro recordado, la app SHALL ofrecer a la plataforma, además de rodillo, potencia y cadencia, una fuente de pulso en el mismo dispositivo que el rodillo. Sin pulsómetro recordado SHALL NOT ofrecerla. Con la plataforma suscrita al pulso, la app SHALL enviarle los bpm en cada lectura válida. Sin pulso, SHALL NOT enviarle nada (ni un 0). El pulso SHALL NOT cambiar la potencia, la cadencia, la pendiente ni la marcha.

#### Scenario: Emparejar el pulso en Zwift para PC
- **WHEN** hay un pulsómetro recordado, el móvil y el PC están en la misma red wifi y el usuario abre la pantalla de emparejamiento de Zwift
- **THEN** «Smart Bike» aparece por wifi también en la lista de pulso y se puede elegir

#### Scenario: Pedaleando con la banda
- **WHEN** el pulsómetro da 140 bpm y la plataforma está conectada y suscrita al pulso
- **THEN** la plataforma muestra 140 bpm y los cambios que dé la banda llegan sin cortes

#### Scenario: Pulsómetro sin lectura
- **WHEN** la plataforma está suscrita al pulso, la banda se ha desconectado y no hay pulso
- **THEN** la app no envía pulso, que la plataforma trata como sensor sin datos, y la potencia y la cadencia siguen llegando

#### Scenario: Banda de vuelta
- **WHEN** no había pulso, la plataforma sigue suscrita y la banda vuelve a dar lecturas válidas
- **THEN** la plataforma vuelve a recibir los bpm sin volver a emparejar

#### Scenario: Sin pulsómetro recordado
- **WHEN** no hay pulsómetro recordado y se abre el emparejamiento de la plataforma
- **THEN** «Smart Bike» no aparece en la lista de pulso, y la potencia, la cadencia y el rodillo funcionan como siempre

#### Scenario: Plataforma sin suscribirse al pulso
- **WHEN** la plataforma está conectada pero no ha elegido la app como fuente de pulso
- **THEN** la app no envía pulso, y no cambia nada de lo demás

#### Scenario: La plataforma vuelve a otra sesión
- **WHEN** la app se eligió como fuente de pulso en una sesión anterior y se abren la app y la plataforma otra vez
- **THEN** la plataforma la reconoce como la misma fuente y recibe el pulso sin volver a elegirla

#### Scenario: Otro equipo de la plataforma
- **WHEN** Zwift está conectado desde un PC y se conecta Zwift desde otro equipo de la red
- **THEN** el pulso va al último que se ha conectado, como lo demás

### Requirement: Modo de rodillo por wifi sin mando por wifi
El modo «Wifi + Bluetooth» SHALL ofrecer a la plataforma el rodillo con FTMS y la potencia y la cadencia por FTMS y Cycling Power por wifi, y SHALL NOT ofrecer ningún servicio de mando por wifi: con un Square recordado, el mando se anuncia aparte por Bluetooth (spec `zwift-controller`).

#### Scenario: Rodillo con FTMS
- **WHEN** el modo es «Wifi + Bluetooth», Zwift envía una pendiente de 4 % y la marcha es la 12
- **THEN** el Neo simula +4 % y Zwift ve la potencia y la cadencia como siempre

#### Scenario: Sin mando por wifi
- **WHEN** el modo es «Wifi + Bluetooth» con un Square recordado
- **THEN** Zwift ve el rodillo por wifi y el mando por Bluetooth, y el rodillo no ofrece el servicio del mando

### Requirement: Nombre ante la plataforma
Con el nombre elegido en «Nombre ante la app» de Conexión, la app SHALL anunciarse por wifi con ese nombre: «Smart Bike», por defecto, o «KICKR BIKE PRO» seguido de cuatro caracteres hexadecimales, en cualquiera de los modos con wifi. Lo que se ofrece y se envía SHALL ser lo mismo con los dos nombres. Con «Solo Bluetooth», el nombre SHALL ser el nombre Bluetooth del móvil (spec `ftms-bluetooth`).

#### Scenario: Nombre por defecto
- **WHEN** «Smart Bike» está elegido y se abre la pantalla de emparejamiento de Zwift
- **THEN** Zwift lista «Smart Bike»

#### Scenario: KICKR BIKE PRO
- **WHEN** «KICKR BIKE PRO» está elegido y se abre la pantalla de emparejamiento de Zwift
- **THEN** Zwift lista un dispositivo «KICKR BIKE PRO» con cuatro caracteres hexadecimales y no lista «Smart Bike»

#### Scenario: Mismo nombre en cada sesión
- **WHEN** «KICKR BIKE PRO» está elegido y se cierran y se vuelven a abrir la app y Zwift
- **THEN** Zwift lista el dispositivo con el mismo nombre y lo reconoce como el mismo, sin volver a elegirlo

#### Scenario: Mismo comportamiento
- **WHEN** «KICKR BIKE PRO» está elegido, Zwift está conectado y se rueda con el Neo y el Square
- **THEN** Zwift recibe la potencia, la cadencia, el pulso y los botones, y el Neo simula el terreno con la marcha de la app, igual que con «Smart Bike»

#### Scenario: Modo por Bluetooth
- **WHEN** el modo es «Solo Bluetooth» y «KICKR BIKE PRO» está elegido
- **THEN** Zwift lista el móvil por su nombre Bluetooth y el nombre elegido no se usa hasta volver a un modo con wifi

### Requirement: Identidad propia de cada modo y de cada nombre
Cada combinación de modo de conexión y nombre SHALL anunciarse con su propia identidad, que SHALL conservarse entre sesiones, de modo que la plataforma las vea como dispositivos distintos. Cambiar de modo o de nombre y volver SHALL dejar a la plataforma con un dispositivo que ya conoce y reconoce sin emparejar de nuevo. Las identidades de la versión anterior, guardadas por plataforma, SHALL NOT conservarse.

#### Scenario: Dispositivo nuevo al cambiar de nombre
- **WHEN** «Smart Bike» estaba emparejado en Zwift y se elige «KICKR BIKE PRO»
- **THEN** Zwift ve un dispositivo nuevo, sin el emparejamiento de «Smart Bike», y hay que elegirlo en el emparejamiento

#### Scenario: Volver al nombre de siempre
- **WHEN** se vuelve a elegir «Smart Bike» y estaba emparejado en Zwift desde antes
- **THEN** Zwift reconoce «Smart Bike» como el mismo dispositivo de antes

#### Scenario: Cada modo con su identidad
- **WHEN** se cambia entre «Wifi + Bluetooth» y «Solo wifi (modo Zwift Ride)» y se vuelve a emparejar el rodillo en Zwift
- **THEN** cada modo se anuncia con su propia identidad, Zwift conecta el rodillo y muestra la potencia sin quedarse en «Conectando…», y volver a un modo ya emparejado lo reconoce

#### Scenario: Primera vez en un modo
- **WHEN** se elige por primera vez un modo o un nombre
- **THEN** Zwift lo muestra como un dispositivo nuevo que hay que emparejar

#### Scenario: Tras actualizar desde la versión con plataformas
- **WHEN** se actualiza la app desde la versión que guardaba los ajustes por plataforma
- **THEN** Zwift ve un dispositivo nuevo, que hay que emparejar una vez

### Requirement: Cambio de modo o de nombre con la sesión abierta
La app SHALL permitir cambiar el modo de conexión y el nombre con la sesión abierta, desde Conexión. Al cambiar, SHALL soltar todas las funciones del mando, dejar de ofrecer lo anterior, volver a anunciarse con lo nuevo y mantener lo último que pidió la plataforma durante los 30 s habituales, sin cambiar la marcha ni lo que simula el Neo. La plataforma SHALL tener que reconectarse y emparejar el dispositivo nuevo.

#### Scenario: Del modo de siempre a todo por wifi
- **WHEN** Zwift está conectado al rodillo por wifi y al mando por Bluetooth y se elige «Solo wifi (modo Zwift Ride)»
- **THEN** el mando deja de anunciarse por Bluetooth, Zwift se reconecta por wifi y la marcha y lo que simula el Neo no cambian

#### Scenario: De todo por wifi al modo de siempre
- **WHEN** el mando está conectado por wifi, hay un Square recordado y se elige «Wifi + Bluetooth»
- **THEN** el mando se anuncia por Bluetooth, el rodillo deja de ofrecer el servicio del mando y Zwift vuelve a controlarlo con FTMS

#### Scenario: Con el mando desactivado
- **WHEN** no hay Square recordado y se cambia entre los tres modos
- **THEN** el mando no se publica en ninguno, sin interruptor que lo active, y los botones del Square no llegan a la app de entrenamiento hasta recordar un Square

#### Scenario: Cambiar el nombre rodando
- **WHEN** se rueda en una ruta de Zwift con el Neo en la marcha 12 y se elige «KICKR BIKE PRO»
- **THEN** Zwift pierde la conexión con «Smart Bike», la marcha sigue siendo la 12, el Neo mantiene lo último que pidió Zwift y, al emparejar el dispositivo nuevo, vuelve a recibir su terreno

#### Scenario: Volver al nombre de siempre rodando
- **WHEN** se rueda con «KICKR BIKE PRO» elegido y se elige «Smart Bike»
- **THEN** la app vuelve a anunciarse como «Smart Bike», la marcha y lo que simula el Neo no cambian y Zwift reconecta con el dispositivo de siempre

#### Scenario: Sin wifi
- **WHEN** no hay conexión wifi y se cambia el modo o el nombre
- **THEN** lo elegido queda guardado y se aplica al anunciarse la app en cuanto haya wifi

#### Scenario: Latencia de la palanca
- **WHEN** se cambia el modo o el nombre y después se pulsan las palancas del Square
- **THEN** la latencia de la palanca sigue por debajo de 100 ms
