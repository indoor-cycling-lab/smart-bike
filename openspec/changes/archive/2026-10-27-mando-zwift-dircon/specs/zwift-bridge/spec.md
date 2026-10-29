## ADDED Requirements

### Requirement: Terreno de Zwift con «Todo por wifi»
Con el modo «Todo por wifi (modo Zwift Ride)», Zwift manda su simulación y la app SHALL contestarla, tomar de ella el terreno (pendiente y, si los manda, el coeficiente de rodadura y el aerodinámico) y simular en el Neo esa pendiente con la marcha de la app y el modelo de resistencia elegido (simple, tabla o físico), como si el terreno viniera del comando de simulación habitual. La marcha de Zwift SHALL ignorarse.

#### Scenario: Subida con el modelo simple
- **WHEN** Zwift envía una pendiente de 4 % y la marcha de la app es la 12, con el modelo simple
- **THEN** el Neo simula +4 %, igual que con el terreno de siempre

#### Scenario: Cambiar de marcha con las palancas
- **WHEN** Zwift envía una pendiente de 4 % y se pasa de la marcha 12 a la 14 con la palanca
- **THEN** el Neo simula +6 % en cuanto se pulsa, sin esperar a otra simulación de Zwift, y Zwift no avisa de nada

#### Scenario: Modelo físico y por tabla
- **WHEN** Zwift envía una pendiente y el modelo es el físico o el de tabla
- **THEN** el Neo recibe lo que ese modelo calcula con esa pendiente y la marcha de la app, como con el terreno de siempre

#### Scenario: Marcha de Zwift ignorada
- **WHEN** Zwift muestra su marcha y esta cambia
- **THEN** la marcha de la app y lo que simula el Neo no cambian

#### Scenario: Simulación a trozos
- **WHEN** Zwift envía primero el coeficiente de rodadura y las masas, y después solo pendientes sueltas
- **THEN** cada pendiente se aplica con el último coeficiente recibido, y una simulación que no trae pendiente no cambia lo que simula el Neo

#### Scenario: Zwift deja de repetir
- **WHEN** Zwift envía una simulación
- **THEN** la app la contesta de modo que Zwift no la repite una y otra vez

#### Scenario: Terreno que cambia muy seguido
- **WHEN** Zwift envía pendientes nuevas varias veces por segundo
- **THEN** el Neo recibe como mucho 4 pendientes por segundo, siempre con la última, y no recibe las que difieren menos de 0,1 % de la anterior

#### Scenario: Zwift desconectado
- **WHEN** se rueda en este modo y se cierra Zwift
- **THEN** el Neo mantiene lo último que pidió 30 s y después pasa a la pendiente base con la marcha puesta, como con el terreno de siempre

#### Scenario: Sin Neo
- **WHEN** Zwift envía su simulación y el Neo no está conectado
- **THEN** la app la guarda y la aplica cuando el Neo se conecta, como con el terreno de siempre

#### Scenario: Marcha fija en el HUD de Zwift
- **WHEN** se rueda en este modo y se cambia de marcha con las palancas
- **THEN** el HUD de Zwift sigue mostrando su misma marcha y la pantalla de Rodaje muestra la de la app

### Requirement: Potencia y cadencia hacia Zwift con «Todo por wifi»
Con el modo «Todo por wifi (modo Zwift Ride)» y el mando saludado por Zwift, la app SHALL mandarle la potencia y la cadencia del Neo con el mensaje de datos de pedaleo del protocolo de Zwift, unas 4 veces por segundo, con 0 si el Neo no está conectado. Sin este mensaje Zwift SHALL NOT mostrar potencia.

#### Scenario: Pedalear a 120 W
- **WHEN** se rueda en este modo y se pedalea a 120 W y 80 rpm
- **THEN** Zwift muestra 120 W y 80 rpm y el avatar avanza

#### Scenario: Dejar de pedalear
- **WHEN** se deja de pedalear
- **THEN** en unos 2 s Zwift muestra potencia y cadencia 0

#### Scenario: Neo perdido
- **WHEN** se pierde la conexión con el Neo con Zwift conectado en este modo
- **THEN** Zwift sigue conectado y recibe potencia y cadencia 0 hasta que el Neo vuelve

### Requirement: Entrenamiento ERG con «Todo por wifi»
Con el modo «Todo por wifi (modo Zwift Ride)», cuando Zwift pide una potencia en un entrenamiento, la app SHALL fijar esa potencia en el Neo, más el ajuste de las palancas, y SHALL conservar la marcha puesta sin enviarla mientras dure; al volver Zwift a mandar el terreno, SHALL volver a la pendiente con esa marcha.

#### Scenario: Entrenamiento a 55 W
- **WHEN** se rueda en este modo y Zwift pide 55 W en un entrenamiento
- **THEN** el Neo pasa a 55 W

#### Scenario: Cambio de intervalo
- **WHEN** Zwift pasa de 55 W a 200 W
- **THEN** el Neo pasa a 200 W

#### Scenario: Fin del entrenamiento
- **WHEN** Zwift termina el entrenamiento y vuelve a mandar la pendiente
- **THEN** el Neo vuelve a simular la pendiente con la marcha puesta

### Requirement: El modo de siempre no cambia
El modo «Rodillo por wifi y mando por Bluetooth» SHALL comportarse como antes de este change: Zwift controla el rodillo con FTMS, recibe la potencia y la cadencia por FTMS y Cycling Power, y no recibe ningún servicio de mando por wifi.

#### Scenario: Rodillo con FTMS
- **WHEN** el modo es el de siempre, Zwift envía una pendiente de 4 % y la marcha es la 12
- **THEN** el Neo simula +4 % y Zwift ve la potencia y la cadencia como siempre

#### Scenario: Sin mando por wifi
- **WHEN** el modo es el de siempre
- **THEN** Zwift ve el rodillo por wifi y el mando por Bluetooth, y el rodillo no ofrece el servicio del mando
