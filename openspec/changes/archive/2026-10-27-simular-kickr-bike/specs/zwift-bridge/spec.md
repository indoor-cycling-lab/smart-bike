## ADDED Requirements

### Requirement: Nombre de KICKR BIKE ante Zwift
Con el ajuste «Simular un KICKR BIKE» activado, la app SHALL anunciarse ante Zwift por wifi como «KICKR BIKE PRO» seguido de cuatro caracteres hexadecimales, en vez de «Square Shift», en cualquiera de los dos modos de conexión con Zwift. Con el ajuste desactivado, que es el valor por defecto, la app SHALL anunciarse como «Square Shift». Lo que se ofrece y se envía a Zwift SHALL ser lo mismo en los dos casos.

#### Scenario: Ajuste desactivado
- **WHEN** el ajuste está desactivado y se abre la pantalla de emparejamiento de Zwift
- **THEN** Zwift lista «Square Shift», como hasta ahora

#### Scenario: Ajuste activado
- **WHEN** el ajuste está activado y se abre la pantalla de emparejamiento de Zwift
- **THEN** Zwift lista un dispositivo «KICKR BIKE PRO» con cuatro caracteres hexadecimales y no lista «Square Shift»

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
- **WHEN** «Square Shift» estaba emparejado en Zwift y se activa el ajuste
- **THEN** Zwift ve un dispositivo nuevo, sin el emparejamiento de «Square Shift», y hay que elegirlo en el emparejamiento

#### Scenario: Volver al dispositivo de siempre
- **WHEN** se desactiva el ajuste y «Square Shift» estaba emparejado en Zwift desde antes
- **THEN** Zwift vuelve a reconocer «Square Shift» como el mismo dispositivo de antes

### Requirement: Cambiar la simulación de KICKR BIKE con la sesión abierta
Cambiar el ajuste «Simular un KICKR BIKE» con la sesión abierta SHALL volver a anunciar la app con el nombre y la identidad nuevos, y SHALL mantener lo último que pidió Zwift durante los 30 s habituales sin cambiar la marcha ni lo que simula el Neo. Zwift SHALL tener que reconectar y emparejar el dispositivo nuevo.

#### Scenario: Activar rodando
- **WHEN** se rueda en una ruta de Zwift con el Neo en la marcha 12 y se activa el ajuste
- **THEN** Zwift pierde la conexión con «Square Shift», la marcha sigue siendo la 12, el Neo mantiene lo último que pidió Zwift y, al emparejar el dispositivo nuevo, vuelve a recibir su terreno

#### Scenario: Desactivar rodando
- **WHEN** se rueda con el ajuste activado y se desactiva
- **THEN** la app vuelve a anunciarse como «Square Shift», la marcha y lo que simula el Neo no cambian y Zwift reconecta con el dispositivo de siempre

#### Scenario: Sin wifi
- **WHEN** no hay conexión wifi y se cambia el ajuste
- **THEN** el ajuste queda guardado y se aplica al anunciarse la app en cuanto haya wifi

#### Scenario: Latencia de la palanca
- **WHEN** se cambia el ajuste y después se pulsan las palancas del Square
- **THEN** la latencia de la palanca sigue por debajo de 100 ms
