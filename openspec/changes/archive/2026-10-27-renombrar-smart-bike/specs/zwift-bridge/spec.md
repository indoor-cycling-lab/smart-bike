## MODIFIED Requirements

### Requirement: Rodillo visible en Zwift por wifi
Mientras dura la sesión y el móvil está conectado a una red wifi, la app SHALL anunciarse en esa red como un rodillo llamado «Smart Bike» que Zwift lista como rodillo controlable, fuente de potencia y fuente de cadencia, sin que el usuario active nada en la app.

#### Scenario: Emparejar en Zwift para PC
- **WHEN** la app está abierta, el móvil y el PC de Zwift están en la misma red wifi y el usuario abre la pantalla de emparejamiento de Zwift
- **THEN** «Smart Bike» aparece por wifi en las listas de potencia, cadencia y rodillo controlable, y se puede elegir en las tres

#### Scenario: Zwift vuelve a otra sesión
- **WHEN** «Smart Bike» se emparejó en Zwift en una sesión anterior y se abren la app y Zwift otra vez
- **THEN** Zwift lo reconoce como el mismo dispositivo y se conecta sin volver a elegirlo

#### Scenario: Neo sin conexión
- **WHEN** Zwift está conectado a la app y el Neo todavía no se ha conectado
- **THEN** Zwift sigue viendo «Smart Bike» conectado, recibe potencia y cadencia 0 y, al conectarse el Neo, empieza a recibir las suyas

### Requirement: Pulso hacia Zwift
La app SHALL ofrecer a Zwift, además de rodillo, potencia y cadencia, una fuente de pulso que Zwift lista como «Smart Bike» en la lista de pulso. Con Zwift conectado y suscrito al pulso, la app SHALL enviarle los bpm del pulsómetro cada vez que la banda da una lectura válida. Mientras no haya pulso, la app SHALL NOT enviarle nada (ni un 0). El pulso SHALL NOT cambiar la potencia, la cadencia, la pendiente ni la marcha.

#### Scenario: Emparejar el pulso en Zwift para PC
- **WHEN** la app está abierta, el móvil y el PC están en la misma red wifi y el usuario abre la pantalla de emparejamiento de Zwift
- **THEN** «Smart Bike» aparece por wifi también en la lista de pulso y se puede elegir

#### Scenario: Pedaleando con la banda
- **WHEN** el pulsómetro da 140 bpm y Zwift está conectado y suscrito al pulso
- **THEN** Zwift muestra 140 bpm y los cambios que dé la banda llegan sin cortes

#### Scenario: Pulsómetro sin lectura
- **WHEN** Zwift está suscrito al pulso, la banda se ha desconectado y no hay pulso
- **THEN** la app no envía pulso a Zwift, que lo trata como sensor sin datos, y la potencia y la cadencia siguen llegando

#### Scenario: Banda de vuelta
- **WHEN** no había pulso, Zwift sigue suscrito y la banda vuelve a dar lecturas válidas
- **THEN** Zwift vuelve a recibir los bpm sin volver a emparejar

#### Scenario: Sin pulsómetro recordado
- **WHEN** no hay pulsómetro recordado y Zwift está conectado
- **THEN** la potencia, la cadencia y el rodillo funcionan como siempre, y la fuente de pulso de «Smart Bike» no envía nada

#### Scenario: Zwift sin suscribirse al pulso
- **WHEN** Zwift está conectado pero no ha elegido «Smart Bike» como fuente de pulso
- **THEN** la app no envía pulso, y no cambia nada de lo demás

#### Scenario: Zwift vuelve a otra sesión
- **WHEN** «Smart Bike» se eligió como fuente de pulso en una sesión anterior y se abren la app y Zwift otra vez
- **THEN** Zwift lo reconoce como la misma fuente y recibe el pulso sin volver a elegirla

#### Scenario: Otro equipo de Zwift
- **WHEN** Zwift está conectado desde un PC y se conecta Zwift desde otro equipo de la red
- **THEN** el pulso va al último que se ha conectado, como lo demás

### Requirement: Nombre de KICKR BIKE ante Zwift
Con el ajuste «Simular un KICKR BIKE» activado, la app SHALL anunciarse ante Zwift por wifi como «KICKR BIKE PRO» seguido de cuatro caracteres hexadecimales, en vez de «Smart Bike», en cualquiera de los dos modos de conexión con Zwift. Con el ajuste desactivado, que es el valor por defecto, la app SHALL anunciarse como «Smart Bike». Lo que se ofrece y se envía a Zwift SHALL ser lo mismo en los dos casos.

#### Scenario: Ajuste desactivado
- **WHEN** el ajuste está desactivado y se abre la pantalla de emparejamiento de Zwift
- **THEN** Zwift lista «Smart Bike», como hasta ahora

#### Scenario: Ajuste activado
- **WHEN** el ajuste está activado y se abre la pantalla de emparejamiento de Zwift
- **THEN** Zwift lista un dispositivo «KICKR BIKE PRO» con cuatro caracteres hexadecimales y no lista «Smart Bike»

#### Scenario: Mismo nombre en cada sesión
- **WHEN** el ajuste está activado y se cierran y se vuelven a abrir la app y Zwift
- **THEN** Zwift lista el dispositivo con el mismo nombre y lo reconoce como el mismo, sin volver a elegirlo

#### Scenario: Cada modo con su nombre
- **WHEN** el ajuste está activado y se cambia entre «Rodillo por wifi y mando por Bluetooth» y «Todo por wifi»
- **THEN** cada modo se anuncia con su propio nombre y su propia identidad, y volver a un modo ya emparejado lo reconoce Zwift

#### Scenario: Mismo comportamiento
- **WHEN** el ajuste está activado, Zwift está conectado y se rueda con el Neo y el Square
- **THEN** Zwift recibe la potencia, la cadencia, el pulso y los botones, y el Neo simula el terreno con la marcha de la app, igual que con el ajuste desactivado

### Requirement: Identidad propia de la simulación de KICKR BIKE
Con el ajuste activado, la app SHALL anunciarse ante Zwift con una identidad distinta de la que usa con el ajuste desactivado, en cada modo, para que Zwift lo trate como un dispositivo nuevo. Al desactivar el ajuste, la app SHALL volver a la identidad de siempre de ese modo, y Zwift SHALL reconocerla sin volver a emparejarla si ya lo estaba.

#### Scenario: Dispositivo nuevo al activar
- **WHEN** «Smart Bike» estaba emparejado en Zwift y se activa el ajuste
- **THEN** Zwift ve un dispositivo nuevo, sin el emparejamiento de «Smart Bike», y hay que elegirlo en el emparejamiento

#### Scenario: Volver al dispositivo de siempre
- **WHEN** se desactiva el ajuste y «Smart Bike» estaba emparejado en Zwift desde antes
- **THEN** Zwift vuelve a reconocer «Smart Bike» como el mismo dispositivo de antes

### Requirement: Cambiar la simulación de KICKR BIKE con la sesión abierta
Cambiar el ajuste «Simular un KICKR BIKE» con la sesión abierta SHALL volver a anunciar la app con el nombre y la identidad nuevos, y SHALL mantener lo último que pidió Zwift durante los 30 s habituales sin cambiar la marcha ni lo que simula el Neo. Zwift SHALL tener que reconectar y emparejar el dispositivo nuevo.

#### Scenario: Activar rodando
- **WHEN** se rueda en una ruta de Zwift con el Neo en la marcha 12 y se activa el ajuste
- **THEN** Zwift pierde la conexión con «Smart Bike», la marcha sigue siendo la 12, el Neo mantiene lo último que pidió Zwift y, al emparejar el dispositivo nuevo, vuelve a recibir su terreno

#### Scenario: Desactivar rodando
- **WHEN** se rueda con el ajuste activado y se desactiva
- **THEN** la app vuelve a anunciarse como «Smart Bike», la marcha y lo que simula el Neo no cambian y Zwift reconecta con el dispositivo de siempre

#### Scenario: Sin wifi
- **WHEN** no hay conexión wifi y se cambia el ajuste
- **THEN** el ajuste queda guardado y se aplica al anunciarse la app en cuanto haya wifi

#### Scenario: Latencia de la palanca
- **WHEN** se cambia el ajuste y después se pulsan las palancas del Square
- **THEN** la latencia de la palanca sigue por debajo de 100 ms
