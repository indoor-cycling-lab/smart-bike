# platform-bridge Specification

## Purpose
Puente con la plataforma de indoor cycling elegida, por wifi: la app se presenta ante ella como un rodillo inteligente en la red local, le envía la potencia, la cadencia y el pulso, y aplica en el Neo el terreno o la potencia que pide, junto con la marcha de la app. Hoy la plataforma es Zwift; lo que aquí se dice vale para cualquier plataforma que se conecte con el mismo protocolo.

## Requirements

### Requirement: Rodillo visible en la plataforma por wifi
Mientras dura la sesión, hay un rodillo configurado, el modo usa wifi para el rodillo y el móvil está conectado a una red wifi, la app SHALL anunciarse en esa red con el nombre elegido en Conexión («Smart Bike» por defecto) como un rodillo que la plataforma lista como rodillo controlable, fuente de potencia y fuente de cadencia, sin que el usuario active nada en la app.

#### Scenario: Emparejar en Zwift para PC
- **WHEN** la app está abierta con un Neo configurado, el móvil y el PC de Zwift están en la misma red wifi y el usuario abre la pantalla de emparejamiento de Zwift
- **THEN** «Smart Bike» aparece por wifi en las listas de potencia, cadencia y rodillo controlable, y se puede elegir en las tres

#### Scenario: La plataforma vuelve a otra sesión
- **WHEN** «Smart Bike» se emparejó en Zwift en una sesión anterior y se abren la app y Zwift otra vez
- **THEN** Zwift lo reconoce como el mismo dispositivo y se conecta sin volver a elegirlo

#### Scenario: Neo sin conexión
- **WHEN** la plataforma está conectada a la app y el Neo configurado todavía no se ha conectado
- **THEN** la plataforma sigue viendo «Smart Bike» conectado, recibe potencia y cadencia 0 y, al conectarse el Neo, empieza a recibir las suyas

### Requirement: Potencia y cadencia hacia la plataforma
Con la plataforma conectada, la app SHALL enviarle la potencia y la cadencia que mide el Neo, de forma continua mientras se pedalea, y SHALL enviar 0 al dejar de pedalear o si el Neo no está conectado.

#### Scenario: Pedaleando
- **WHEN** se pedalea a unos 200 W y 90 rpm con el Neo y la plataforma conectados
- **THEN** la plataforma muestra una potencia y una cadencia que siguen a las de la pantalla de rodaje, sin cortes

#### Scenario: Dejar de pedalear
- **WHEN** se deja de pedalear
- **THEN** en unos 2 s la plataforma muestra potencia y cadencia 0

#### Scenario: Neo perdido
- **WHEN** se pierde la conexión con el Neo con la plataforma conectada
- **THEN** la plataforma sigue conectada a la app y recibe potencia y cadencia 0 hasta que el Neo vuelve

### Requirement: Pendiente de la plataforma con la marcha
Cuando la plataforma envía el terreno de la ruta, la app SHALL simular en el Neo su pendiente en lugar de la pendiente base, más el ajuste de la marcha puesta (1 % por marcha respecto a la 12), con el límite inferior que fije el ajuste «Bajadas», y SHALL seguir enviando cada cambio de marcha al instante.

#### Scenario: Subida en marcha neutra
- **WHEN** la plataforma envía una pendiente de 4 % y la marcha es la 12
- **THEN** el Neo simula +4 %

#### Scenario: Subida en marcha dura
- **WHEN** la plataforma envía una pendiente de 4 % y se pasa de la marcha 12 a la 14
- **THEN** el Neo simula +6 % en cuanto se pulsa la palanca, sin esperar al siguiente terreno de la plataforma

#### Scenario: Bajada
- **WHEN** la plataforma envía una pendiente de −3 % y la marcha es la 10, con el ajuste «Bajadas» por defecto
- **THEN** el Neo simula −3 %, porque la marcha no hace la bajada más pronunciada que la de la plataforma

#### Scenario: Bajada sin protección
- **WHEN** la plataforma envía una pendiente de −3 % y la marcha es la 10, con «Bajadas» en «Sin protección»
- **THEN** el Neo simula −5 %

#### Scenario: Bajada sin empuje
- **WHEN** la plataforma envía una pendiente de −3 % y la marcha es la 10, con «Bajadas» en «Nunca empujar»
- **THEN** el Neo simula 0 %

#### Scenario: Terreno que cambia muy seguido
- **WHEN** la plataforma envía pendientes nuevas varias veces por segundo
- **THEN** el Neo recibe como mucho 4 pendientes por segundo, siempre con la última, y no recibe las que difieren menos de 0,1 % de la anterior

### Requirement: Entrenamiento ERG de la plataforma
Cuando la plataforma pide una potencia en un entrenamiento, la app SHALL fijar esa potencia en el Neo, más el ajuste de las palancas, y SHALL conservar la marcha puesta sin enviarla mientras dure el ERG; al volver la plataforma a enviar el terreno, SHALL volver a la pendiente con esa marcha.

#### Scenario: Bloque de 230 W
- **WHEN** la plataforma pide 230 W y no se han tocado las palancas
- **THEN** el Neo mantiene unos 230 W aunque cambie la cadencia

#### Scenario: Cambio de bloque
- **WHEN** la plataforma pasa de pedir 230 W a pedir 280 W
- **THEN** el Neo pasa a 280 W más el ajuste de las palancas

#### Scenario: Fin del entrenamiento
- **WHEN** la marcha era la 14 antes del entrenamiento, se ajusta la potencia con las palancas y la plataforma vuelve a enviar una pendiente de 2 %
- **THEN** el Neo simula +4 %, la marcha sigue siendo la 14 y el ajuste de las palancas se pone a 0

### Requirement: Palancas en ERG de la plataforma
En ERG de la plataforma, las palancas SHALL ajustar la potencia en lugar de la marcha: 10 W por cada marcha que pedirían, en el mismo sentido (más dura, más potencia), y □ SHALL quitar el ajuste. Las acciones «+10 W» y «−10 W» SHALL ajustar la potencia 10 W. Las de plato y las de marcha más dura o más ligera SHALL NOT cambiar la potencia. El ajuste SHALL mantenerse entre bloques y la potencia resultante SHALL quedar entre 50 W y 1000 W.

#### Scenario: Subir 10 W
- **WHEN** la plataforma pide 230 W y se pulsa la palanca D1
- **THEN** el Neo pasa a 240 W y la marcha no cambia

#### Scenario: Salto con modificador
- **WHEN** la plataforma pide 230 W, el ajuste es 0 y se mantiene Campag. I y se pulsa la palanca I1
- **THEN** el Neo pasa a 200 W

#### Scenario: Quitar el ajuste
- **WHEN** la plataforma pide 230 W, el ajuste es +20 W y se pulsa □
- **THEN** el Neo vuelve a 230 W

#### Scenario: Ajuste entre bloques
- **WHEN** el ajuste es +10 W y la plataforma pasa de pedir 230 W a pedir 280 W
- **THEN** el Neo pasa a 290 W

#### Scenario: Potencia mínima
- **WHEN** la plataforma pide 60 W y se pulsa la palanca I1 dos veces
- **THEN** la primera pulsación deja el Neo en 50 W y la segunda no cambia nada y vibra como en un tope

#### Scenario: Marcha más dura o más ligera en ERG
- **WHEN** en ERG se mantiene Campag. D y se pulsa la palanca D2
- **THEN** ni la potencia ni la marcha cambian

#### Scenario: Mantener la palanca en ERG
- **WHEN** la plataforma pide 230 W, la autorrepetición está activada y se mantiene la palanca D1 hasta que repite dos veces
- **THEN** el Neo pasa a 240 W al pulsar y después a 250 W y 260 W, y la marcha no cambia

#### Scenario: Botón asignado a −10 W en ERG
- **WHEN** «−10 W» está asignado a B «pulsar», la plataforma pide 230 W y se pulsa B
- **THEN** el Neo pasa a 220 W y el ajuste de las palancas es −10 W

#### Scenario: Fijar la referencia en ERG
- **WHEN** en ERG con la marcha en la 15 se mantiene □ hasta el umbral de pulsación larga
- **THEN** la referencia pasa a la 15 y la potencia no cambia

#### Scenario: Shimano en ERG
- **WHEN** la transmisión es la Shimano, la plataforma pide 230 W y se pulsa la palanca D2
- **THEN** el Neo pasa a 240 W y la marcha no cambia

#### Scenario: Palanca D1 con la Shimano en ERG
- **WHEN** la transmisión es la Shimano, la plataforma pide 230 W y se pulsa la palanca D1
- **THEN** el Neo pasa a 220 W

#### Scenario: Plato en ERG
- **WHEN** la transmisión es la Shimano, la plataforma pide 230 W y se pulsa la palanca I1
- **THEN** la potencia y la marcha no cambian

### Requirement: Plataforma desconectada
Si la plataforma se desconecta, la app SHALL mantener en el Neo lo último que pidió (terreno o potencia) durante 30 s y, si no vuelve, SHALL volver a la pendiente base con la marcha puesta; SHALL seguir anunciándose para que la plataforma se reconecte sola.

#### Scenario: Corte breve
- **WHEN** se rueda en una subida de Zwift al 5 % con la marcha 12 y Zwift pierde la conexión durante 10 s
- **THEN** el Neo sigue simulando +5 % durante el corte y, al volver Zwift, la pendiente vuelve a seguir el terreno sin tocar el móvil

#### Scenario: Plataforma cerrada
- **WHEN** se rueda en una subida de Zwift al 5 % con la marcha 14 y se cierra Zwift
- **THEN** a los 30 s el Neo pasa a simular +2 % (pendiente base 0 % y marcha 14) y las palancas siguen cambiando de marcha

#### Scenario: Plataforma cerrada en un entrenamiento
- **WHEN** Zwift pide 250 W en un entrenamiento y se cierra Zwift
- **THEN** el Neo mantiene 250 W durante 30 s y después pasa a la pendiente de la marcha puesta

#### Scenario: Plataforma que deja de responder
- **WHEN** se rueda en una subida de Zwift al 5 % con la marcha 14 por wifi y el PC de Zwift se suspende sin cerrar Zwift
- **THEN** la app da la plataforma por desconectada en menos de 30 s, el estado de la plataforma deja de decir «Conectado» y, 30 s después, el Neo pasa a simular +2 %

#### Scenario: Plataforma en los menús sin enviar nada
- **WHEN** Zwift está conectado por wifi y el usuario pasa 10 minutos en los menús sin rodar
- **THEN** la app no da la plataforma por desconectada mientras Zwift siga en marcha

#### Scenario: Zwift vuelve desde el mismo equipo
- **WHEN** Zwift está conectado desde un PC, pierde la wifi unos segundos y vuelve a conectarse desde el mismo PC antes de que la app detecte el corte
- **THEN** la app atiende a la conexión nueva y deja la anterior, sin esperar a que caduque

#### Scenario: Otro equipo de la plataforma
- **WHEN** Zwift está conectado desde un PC y le está enviando datos a la app, y otro equipo de la red se conecta a la app
- **THEN** la app rechaza la conexión nueva, sigue atendiendo a Zwift en el PC sin cortes y anota el rechazo con la dirección del otro equipo en el registro

#### Scenario: Otro equipo cuando la plataforma ya no responde
- **WHEN** Zwift estaba conectado desde un PC, lleva más de 15 s sin enviar nada y se conecta Zwift desde otro equipo de la red
- **THEN** la app atiende al otro equipo y deja de atender al PC

### Requirement: Sin wifi
Sin red wifi, la app SHALL seguir rodando sin la plataforma y SHALL decir que la plataforma necesita la misma red wifi; al recuperar el wifi, SHALL volver a anunciarse sin que el usuario haga nada.

#### Scenario: Wifi apagado al abrir
- **WHEN** se abre la app con el wifi del móvil apagado
- **THEN** las marchas funcionan con la pendiente base, el estado de la plataforma dice «sin conexión» y Conexión explica que hay que conectar el móvil a la misma red wifi que la plataforma

#### Scenario: Wifi perdido rodando
- **WHEN** se rueda en Zwift y el móvil pierde el wifi durante 1 minuto
- **THEN** tras 30 s el Neo vuelve a la pendiente base con la marcha puesta y, al volver el wifi, Zwift se reconecta a la app sin tocar el móvil

### Requirement: Latencia de la palanca con la plataforma
Con la plataforma conectada, un cambio de marcha SHALL llegar al Neo con la misma inmediatez que sin ella: el terreno o la telemetría de la plataforma SHALL NOT retrasar el comando de la marcha.

#### Scenario: Cambio en una subida
- **WHEN** se rueda en Zwift en una subida y se pulsa la palanca D1
- **THEN** Diagnóstico muestra una latencia de la palanca del mismo orden que sin Zwift

### Requirement: Anuncio comprobado en la red
Mientras la app se anuncia en la red wifi y la plataforma no está conectada, la app SHALL comprobar que su anuncio responde en esa red, al anunciarse y después cada minuto. SHALL dejar de comprobarlo mientras la plataforma esté conectada. Si no responde, SHALL volver a anunciarse una vez y comprobarlo de nuevo antes de avisar. El registro de la sesión SHALL guardar el resultado de cada comprobación.

#### Scenario: Anuncio que responde
- **WHEN** se abre la sesión con wifi y el anuncio responde en la red
- **THEN** la plataforma sigue en «Esperando», no aparece ningún aviso y el registro de la sesión dice que el anuncio responde

#### Scenario: Basta con volver a anunciarse
- **WHEN** la primera comprobación falla y, tras volver a anunciarse, el anuncio responde
- **THEN** la plataforma sigue en «Esperando», sin aviso, y el registro de la sesión dice que hizo falta volver a anunciarse

#### Scenario: Plataforma conectada
- **WHEN** la plataforma está conectada a la app
- **THEN** la app no comprueba el anuncio y nunca muestra «No visible», porque la conexión ya demuestra que la plataforma la encontró

#### Scenario: Sin wifi
- **WHEN** el móvil no tiene wifi
- **THEN** la app no comprueba el anuncio y la plataforma dice «Sin conexión» con la ayuda de conectarse a la misma red, no «No visible»

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

### Requirement: Pulso hacia la plataforma
Con un pulsómetro configurado, la app SHALL ofrecer a la plataforma, además de rodillo, potencia y cadencia, una fuente de pulso en el mismo dispositivo que el rodillo. Sin pulsómetro configurado SHALL NOT ofrecerla. Con la plataforma suscrita al pulso, la app SHALL enviarle los bpm en cada lectura válida. Sin pulso, SHALL NOT enviarle nada (ni un 0). El pulso SHALL NOT cambiar la potencia, la cadencia, la pendiente ni la marcha.

#### Scenario: Emparejar el pulso en Zwift para PC
- **WHEN** hay un pulsómetro configurado, el móvil y el PC están en la misma red wifi y el usuario abre la pantalla de emparejamiento de Zwift
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

#### Scenario: Sin pulsómetro configurado
- **WHEN** no hay pulsómetro configurado y se abre el emparejamiento de la plataforma
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
El modo «Wifi + Bluetooth» SHALL ofrecer a la plataforma el rodillo con FTMS y la potencia y la cadencia por FTMS y Cycling Power por wifi, y SHALL NOT ofrecer ningún servicio de mando por wifi: con un Square configurado, el mando se anuncia aparte por Bluetooth (spec `zwift-controller`).

#### Scenario: Rodillo con FTMS
- **WHEN** el modo es «Wifi + Bluetooth», Zwift envía una pendiente de 4 % y la marcha es la 12
- **THEN** el Neo simula +4 % y Zwift ve la potencia y la cadencia como siempre

#### Scenario: Sin mando por wifi
- **WHEN** el modo es «Wifi + Bluetooth» con un Square configurado
- **THEN** Zwift ve el rodillo por wifi y el mando por Bluetooth, y el rodillo no ofrece el servicio del mando

### Requirement: Nombre ante la plataforma
Con el nombre elegido en «Nombre ante la app» de Conexión, la app SHALL anunciarse por wifi con ese nombre: «Smart Bike», por defecto, o «KICKR BIKE PRO» seguido de cuatro caracteres hexadecimales, en cualquiera de los modos con wifi. Lo que se ofrece y se envía SHALL ser lo mismo con los dos nombres. Con «Solo Bluetooth», el nombre SHALL ser el nombre Bluetooth del móvil, que es el elegido si «Usar el nombre también por Bluetooth» está marcada (specs `ftms-bluetooth` y `bluetooth-name`).

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
- **WHEN** el modo es «Solo Bluetooth», «KICKR BIKE PRO» está elegido y «Usar el nombre también por Bluetooth» está desmarcada
- **THEN** Zwift lista el móvil por su nombre Bluetooth y el nombre elegido no se usa hasta volver a un modo con wifi o marcar la casilla

#### Scenario: Modo por Bluetooth con el nombre elegido
- **WHEN** el modo es «Solo Bluetooth», «KICKR BIKE PRO» está elegido y «Usar el nombre también por Bluetooth» está marcada
- **THEN** Zwift lista «KICKR BIKE PRO» con sus cuatro caracteres por Bluetooth y recibe lo mismo que con «Smart Bike»

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
- **WHEN** el mando está conectado por wifi, hay un Square configurado y se elige «Wifi + Bluetooth»
- **THEN** el mando se anuncia por Bluetooth, el rodillo deja de ofrecer el servicio del mando y Zwift vuelve a controlarlo con FTMS

#### Scenario: Con el mando desactivado
- **WHEN** no hay Square configurado y se cambia entre los tres modos
- **THEN** el mando no se publica en ninguno, sin interruptor que lo active, y los botones del Square no llegan a la app de entrenamiento hasta configurar un Square

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

### Requirement: Lo publicado según los dispositivos configurados
En cualquier modo, la app SHALL publicar el rodillo solo con un rodillo configurado en Dispositivos, el mando solo con un Square configurado y el pulso solo con un pulsómetro configurado. Sin rodillo configurado SHALL NOT publicar nada. Que un dispositivo configurado esté desconectado SHALL NOT cambiar lo que se publica: solo su estado.

#### Scenario: Sin rodillo configurado
- **WHEN** se abre la app sin rodillo configurado, con el Square y el pulsómetro configurados
- **THEN** ninguna app de entrenamiento encuentra la app, ni por wifi ni por Bluetooth, y las marchas funcionan como sin app de entrenamiento

#### Scenario: Rodillo y Square sin pulsómetro
- **WHEN** hay un Neo y un Square configurados, ningún pulsómetro, y se abre el emparejamiento de Zwift
- **THEN** «Smart Bike» aparece como potencia, cadencia, rodillo y mando, y no en la lista de pulso

#### Scenario: Neo dormido
- **WHEN** el Neo configurado está dormido y la app de entrenamiento se conecta
- **THEN** la app sigue publicando el rodillo y envía potencia y cadencia 0 hasta que el Neo se conecta

#### Scenario: Square que se duerme
- **WHEN** la app de entrenamiento está conectada al mando y el Square se desconecta un momento
- **THEN** el mando se sigue publicando, la app de entrenamiento no lo pierde y los botones vuelven a llegar al reconectarse el Square

### Requirement: Configurar u olvidar cambia lo publicado
Al configurar u olvidar un rodillo, un Square o un pulsómetro, la app SHALL empezar o dejar de publicar ese servicio sin cambiar la marcha ni lo que simula el Neo, volviendo a anunciarse si hace falta. La app de entrenamiento SHALL poder tener que reconectarse o emparejar de nuevo el dispositivo para ver el cambio.

#### Scenario: Configurar un pulsómetro rodando
- **WHEN** se rueda con Zwift conectado por wifi y se recuerda un pulsómetro en Dispositivos
- **THEN** la app empieza a publicar el pulso, la marcha y lo que simula el Neo no cambian, y Zwift lista «Smart Bike» como fuente de pulso, tras reconectarse o emparejarlo de nuevo si hace falta

#### Scenario: Olvidar el Square
- **WHEN** Zwift está conectado al mando por Bluetooth en «Wifi + Bluetooth» y se olvida el Square
- **THEN** el mando deja de anunciarse, Zwift lo pierde y el rodillo sigue conectado por wifi sin cambios

#### Scenario: Olvidar el rodillo
- **WHEN** Zwift está conectado y se olvida el Neo
- **THEN** la app deja de publicar todo, Zwift pierde la conexión y la marcha sigue igual

### Requirement: Plataforma solo por la red wifi
La app SHALL aceptar conexiones de la plataforma por wifi únicamente desde la red wifi a la que está conectado el móvil; SHALL NOT ser alcanzable por los datos móviles, una VPN ni el punto de acceso del móvil. Si cambia la red wifi o su dirección, SHALL volver a escuchar en la nueva sin que el usuario haga nada.

#### Scenario: Datos móviles activos
- **WHEN** la app está abierta con el wifi y los datos móviles activos
- **THEN** solo se puede conectar con la app desde la red wifi; desde internet, por los datos móviles, no se llega a ella

#### Scenario: Cambio de red wifi
- **WHEN** la app está esperando a la plataforma y el móvil pasa a otra red wifi
- **THEN** la app se anuncia y acepta conexiones en la red nueva, y deja de aceptarlas en la anterior

#### Scenario: Sin wifi
- **WHEN** el móvil no está conectado a ninguna red wifi
- **THEN** la app no acepta conexiones de la plataforma por ninguna otra red y Conexión explica que hace falta la misma red wifi que la plataforma

### Requirement: Cliente que inunda de mensajes
Si un cliente por wifi envía mensajes a un ritmo muy superior al de una plataforma real, la app SHALL desconectarlo y anotarlo en el registro, y SHALL seguir cambiando de marcha sin retrasos mientras tanto.

#### Scenario: Ráfaga de comandos
- **WHEN** un equipo de la red se conecta a la app y envía cientos de comandos por segundo mientras se rueda
- **THEN** la app lo desconecta, el registro dice por qué y las palancas siguen cambiando la resistencia del Neo con la misma latencia (NF1)

#### Scenario: Zwift en uso normal
- **WHEN** se rueda en Zwift por wifi durante una hora con cambios de pendiente, entrenamientos ERG y el controlador
- **THEN** la app nunca desconecta a Zwift por exceso de mensajes
