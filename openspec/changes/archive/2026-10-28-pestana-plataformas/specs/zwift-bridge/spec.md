# Spec Delta

## MODIFIED Requirements

### Requirement: Zwift no visible
Si el anuncio sigue sin responder después de volver a anunciarse, la app SHALL mostrar Zwift como «No visible» y decir qué hacer: apagar y volver a encender el wifi del móvil y, si sigue igual, reiniciar el móvil. Las marchas SHALL seguir funcionando como sin Zwift. En cuanto el anuncio vuelva a responder, la app SHALL volver a «Esperando» sin que el usuario haga nada más.

#### Scenario: Anuncio que no responde
- **WHEN** se abre la sesión con wifi, Android da el anuncio por registrado y el anuncio no responde en la red, ni siquiera tras volver a anunciarse
- **THEN** en unos 15 s Plataformas muestra Zwift como «No visible», con el aviso de apagar y volver a encender el wifi del móvil y, si sigue igual, reiniciarlo; las marchas siguen funcionando con la pendiente base

#### Scenario: Reiniciar el wifi
- **WHEN** Zwift está «No visible» y el usuario apaga y vuelve a encender el wifi del móvil
- **THEN** al volver el wifi, la app se anuncia de nuevo, lo comprueba y, si responde, Zwift vuelve a «Esperando» y el aviso desaparece sin tocar la app

#### Scenario: El anuncio deja de responder más tarde
- **WHEN** el anuncio respondía al abrir la sesión, Zwift no se ha conectado y, más tarde, deja de responder
- **THEN** antes de 2 minutos Zwift pasa a «No visible» con el mismo aviso

### Requirement: El modo de siempre no cambia
El modo «Rodillo por wifi (FTMS)», antes «Rodillo por wifi y mando por Bluetooth», SHALL comportarse como antes de este change: Zwift controla el rodillo con FTMS, recibe la potencia y la cadencia por FTMS y Cycling Power, y no recibe ningún servicio de mando por wifi. El mando por Bluetooth es el interruptor «Mando de Zwift» de Plataformas.

#### Scenario: Rodillo con FTMS
- **WHEN** el modo es el de siempre, Zwift envía una pendiente de 4 % y la marcha es la 12
- **THEN** el Neo simula +4 % y Zwift ve la potencia y la cadencia como siempre

#### Scenario: Sin mando por wifi
- **WHEN** el modo es el de siempre
- **THEN** Zwift ve el rodillo por wifi y, con «Mando de Zwift» activado, el mando por Bluetooth, y el rodillo no ofrece el servicio del mando

### Requirement: Nombre de KICKR BIKE ante Zwift
Con «KICKR BIKE PRO» elegido en «Nombre ante la app» de Plataformas, la app SHALL anunciarse ante Zwift por wifi como «KICKR BIKE PRO» seguido de cuatro caracteres hexadecimales, en vez de «Smart Bike», en cualquiera de los dos modos de conexión con Zwift. Con «Smart Bike», que es el valor por defecto, la app SHALL anunciarse como «Smart Bike». Lo que se ofrece y se envía a Zwift SHALL ser lo mismo en los dos casos.

#### Scenario: Ajuste desactivado
- **WHEN** «Smart Bike» está elegido y se abre la pantalla de emparejamiento de Zwift
- **THEN** Zwift lista «Smart Bike», como hasta ahora

#### Scenario: Ajuste activado
- **WHEN** «KICKR BIKE PRO» está elegido y se abre la pantalla de emparejamiento de Zwift
- **THEN** Zwift lista un dispositivo «KICKR BIKE PRO» con cuatro caracteres hexadecimales y no lista «Smart Bike»

#### Scenario: Mismo nombre en cada sesión
- **WHEN** «KICKR BIKE PRO» está elegido y se cierran y se vuelven a abrir la app y Zwift
- **THEN** Zwift lista el dispositivo con el mismo nombre y lo reconoce como el mismo, sin volver a elegirlo

#### Scenario: Cada modo con su nombre
- **WHEN** «KICKR BIKE PRO» está elegido y se cambia entre «Rodillo por wifi (FTMS)» y «Todo por wifi (modo Zwift Ride)»
- **THEN** cada modo se anuncia con su propio nombre y su propia identidad, y volver a un modo ya emparejado lo reconoce Zwift

#### Scenario: Mismo comportamiento
- **WHEN** «KICKR BIKE PRO» está elegido, Zwift está conectado y se rueda con el Neo y el Square
- **THEN** Zwift recibe la potencia, la cadencia, el pulso y los botones, y el Neo simula el terreno con la marcha de la app, igual que con «Smart Bike»

### Requirement: Identidad propia de la simulación de KICKR BIKE
Con «KICKR BIKE PRO» elegido, la app SHALL anunciarse ante Zwift con una identidad distinta de la que usa con «Smart Bike», en cada modo, para que Zwift lo trate como un dispositivo nuevo. Al volver a elegir «Smart Bike», la app SHALL volver a la identidad de siempre de ese modo, y Zwift SHALL reconocerla sin volver a emparejarla si ya lo estaba.

#### Scenario: Dispositivo nuevo al activar
- **WHEN** «Smart Bike» estaba emparejado en Zwift y se elige «KICKR BIKE PRO»
- **THEN** Zwift ve un dispositivo nuevo, sin el emparejamiento de «Smart Bike», y hay que elegirlo en el emparejamiento

#### Scenario: Volver al dispositivo de siempre
- **WHEN** se vuelve a elegir «Smart Bike» y estaba emparejado en Zwift desde antes
- **THEN** Zwift vuelve a reconocer «Smart Bike» como el mismo dispositivo de antes

### Requirement: Cambiar la simulación de KICKR BIKE con la sesión abierta
Cambiar el «Nombre ante la app» con la sesión abierta SHALL volver a anunciar la app con el nombre y la identidad nuevos, y SHALL mantener lo último que pidió Zwift durante los 30 s habituales sin cambiar la marcha ni lo que simula el Neo. Zwift SHALL tener que reconectar y emparejar el dispositivo nuevo.

#### Scenario: Activar rodando
- **WHEN** se rueda en una ruta de Zwift con el Neo en la marcha 12 y se elige «KICKR BIKE PRO»
- **THEN** Zwift pierde la conexión con «Smart Bike», la marcha sigue siendo la 12, el Neo mantiene lo último que pidió Zwift y, al emparejar el dispositivo nuevo, vuelve a recibir su terreno

#### Scenario: Desactivar rodando
- **WHEN** se rueda con «KICKR BIKE PRO» elegido y se elige «Smart Bike»
- **THEN** la app vuelve a anunciarse como «Smart Bike», la marcha y lo que simula el Neo no cambian y Zwift reconecta con el dispositivo de siempre

#### Scenario: Sin wifi
- **WHEN** no hay conexión wifi y se cambia el nombre
- **THEN** el nombre queda guardado y se aplica al anunciarse la app en cuanto haya wifi

#### Scenario: Latencia de la palanca
- **WHEN** se cambia el nombre y después se pulsan las palancas del Square
- **THEN** la latencia de la palanca sigue por debajo de 100 ms

### Requirement: Información del dispositivo hacia Zwift
Con el rodillo de la app emparejado en Zwift por wifi, en cualquiera de los dos modos de conexión, la app SHALL ofrecer a Zwift, además del fabricante, el modelo y la versión de la app, un número de serie, una versión de firmware y una versión de hardware, y SHALL contestar a su lectura sin error. El número de serie SHALL ser el de la identidad propia de la app ante Zwift y SHALL conservarse entre sesiones.

#### Scenario: Zwift lee la información al emparejar
- **WHEN** Zwift empareja el rodillo por wifi y lee la información del dispositivo
- **THEN** el registro de sesión muestra la lectura de la serie, el firmware y el hardware, cada una contestada con un valor, y Zwift termina de emparejar sin mensajes de error

#### Scenario: Misma serie en cada sesión
- **WHEN** se cierran y se vuelven a abrir la app y Zwift con el rodillo ya emparejado
- **THEN** Zwift lee el mismo número de serie y reconoce el dispositivo sin volver a emparejarlo

#### Scenario: Dos modos, dos identidades
- **WHEN** se cambia entre «Rodillo por wifi (FTMS)» y «Todo por wifi (modo Zwift Ride)»
- **THEN** cada modo ofrece la serie de su propia identidad, y la de uno no aparece en el otro

#### Scenario: Con el simulador de KICKR BIKE
- **WHEN** «KICKR BIKE PRO» está elegido en «Nombre ante la app» y Zwift lee la información del dispositivo
- **THEN** Zwift recibe la serie de esa identidad, que es la misma en cada sesión, y el resto de lo observable no cambia

#### Scenario: Sin Neo conectado
- **WHEN** Zwift lee la información del dispositivo con el Neo desconectado
- **THEN** Zwift la recibe igual, porque no depende del Neo
