# Spec Delta

## Purpose

Modo ERG manual del Neo: el usuario fija una potencia que el rodillo mantiene sea cual sea la cadencia, la activa y la ajusta desde el Square, con o sin Zwift, sin tocar el móvil.

## ADDED Requirements

### Requirement: Alternar SIM ⇄ ERG manual
La acción «Alternar SIM ⇄ ERG» SHALL pasar de SIM (sin Zwift o en una ruta de Zwift) a ERG manual y, en ERG manual, volver a SIM. En ERG manual, el Neo SHALL mantener la potencia fijada sea cual sea la cadencia. Al volver a SIM, el Neo SHALL recibir al momento la pendiente que corresponde a la marcha puesta.

#### Scenario: Entrar en ERG manual
- **WHEN** se rueda sin Zwift en la marcha 14, la última potencia de ERG manual es 200 W y se mantiene △ hasta el umbral de pulsación larga
- **THEN** el Neo pasa a 200 W y lo mantiene aunque cambie la cadencia

#### Scenario: Volver a SIM
- **WHEN** en ERG manual, con la marcha 14 sin Zwift, se mantiene △ hasta el umbral de pulsación larga
- **THEN** el Neo pasa a simular +2 % (la pendiente de la marcha 14) y la marcha sigue siendo la 14

#### Scenario: Potencia mantenida a dos cadencias
- **WHEN** en ERG manual a 200 W se pedalea 30 s a unas 70 rpm y otros 30 s a unas 95 rpm
- **THEN** la potencia medida del Neo se queda cerca de 200 W en los dos tramos, tras unos segundos de estabilización al cambiar de cadencia

### Requirement: Potencia inicial recordada
Al entrar en ERG manual, la potencia SHALL ser la última que tuvo el ERG manual, también si fue en una sesión anterior; la primera vez SHALL ser 150 W. La app SHALL empezar siempre en SIM.

#### Scenario: Primera vez
- **WHEN** se entra en ERG manual por primera vez desde que se instaló la app
- **THEN** el Neo pasa a 150 W

#### Scenario: Entre sesiones
- **WHEN** se sale de ERG manual con 220 W, se termina la sesión, se vuelve a abrir la app y se entra en ERG manual
- **THEN** la app empieza en SIM y, al entrar, el Neo pasa a 220 W

### Requirement: Palancas en ERG manual
En ERG manual, las acciones de marcha SHALL ajustar la potencia en lugar de la marcha: 10 W por cada marcha que pedirían, en el mismo sentido (más dura, más potencia). Plato, marcha más dura y marcha más ligera SHALL NOT cambiar nada. Volver a la referencia SHALL devolver la potencia con la que se entró en ERG manual; fijar la referencia SHALL fijar la marcha puesta, sin tocar la potencia.

#### Scenario: Subir 10 W
- **WHEN** en ERG manual a 200 W se pulsa la palanca D1
- **THEN** el Neo pasa a 210 W y la marcha no cambia

#### Scenario: Salto con modificador
- **WHEN** en ERG manual a 200 W se mantiene Campag. I y se pulsa la palanca I1
- **THEN** el Neo pasa a 170 W

#### Scenario: Mantener la palanca
- **WHEN** en ERG manual a 200 W, con la autorrepetición activada, se mantiene la palanca D1 hasta que repite dos veces
- **THEN** el Neo pasa a 210 W al pulsar y después a 220 W y 230 W

#### Scenario: Volver a la potencia de entrada
- **WHEN** se entra en ERG manual a 200 W, se sube a 240 W y se pulsa □
- **THEN** el Neo vuelve a 200 W

#### Scenario: Plato en ERG manual
- **WHEN** la transmisión es la Shimano, se está en ERG manual a 200 W y se pulsa la palanca I1
- **THEN** ni la potencia ni la marcha cambian

#### Scenario: Palanca D1 con la Shimano
- **WHEN** la transmisión es la Shimano, se está en ERG manual a 200 W y se pulsa la palanca D1
- **THEN** el Neo pasa a 190 W, porque D1 pediría una marcha más ligera

### Requirement: Ajuste de ±10 W
Las acciones «+10 W» y «−10 W», si el usuario las asigna, SHALL subir o bajar 10 W la potencia en ERG manual y SHALL repetirse al mantener su botón, como las de marcha. En SIM SHALL NOT hacer nada. Por defecto no tienen botón.

#### Scenario: Botón asignado a +10 W
- **WHEN** «+10 W» está asignado a A «pulsar» y en ERG manual a 200 W se pulsa A
- **THEN** el Neo pasa a 210 W

#### Scenario: Mantener el botón
- **WHEN** «−10 W» está asignado a B «pulsar» y en ERG manual a 200 W, con la autorrepetición activada, se mantiene B hasta que repite una vez
- **THEN** el Neo pasa a 190 W y después a 180 W

#### Scenario: En SIM
- **WHEN** «+10 W» está asignado a A «pulsar», se rueda en SIM en la marcha 14 y se pulsa A
- **THEN** ni la marcha ni la resistencia cambian

### Requirement: Límites de potencia en ERG manual
La potencia de ERG manual SHALL quedar entre 50 W y 1000 W. Un ajuste que la saque de ese margen SHALL quedarse en el límite; uno pedido ya en el límite SHALL NOT cambiar nada, SHALL vibrar como un tope y SHALL parar la repetición hasta soltar.

#### Scenario: Mínimo
- **WHEN** en ERG manual a 60 W se pulsa la palanca I1 dos veces
- **THEN** la primera pulsación deja el Neo en 50 W y la segunda no cambia nada y vibra como en un tope

#### Scenario: Salto recortado
- **WHEN** en ERG manual a 980 W se pide +30 W
- **THEN** el Neo pasa a 1000 W

### Requirement: Marcha conservada en ERG manual
Mientras dura el ERG manual, la marcha SHALL conservarse sin enviarse al Neo, y al volver a SIM SHALL ser la misma que había al entrar, salvo que se haya fijado otra referencia y se vuelva a ella después.

#### Scenario: Ida y vuelta
- **WHEN** en la marcha 9 se entra en ERG manual, se ajusta la potencia varias veces con las palancas y se vuelve a SIM
- **THEN** la marcha sigue siendo la 9

### Requirement: ERG manual en una ruta de Zwift
En una ruta de Zwift, el ERG manual SHALL mandar sobre el terreno: mientras dura, el Neo SHALL mantener la potencia fijada aunque Zwift envíe pendientes, y al volver a SIM SHALL simular la última pendiente que Zwift haya enviado, con la marcha puesta. Zwift SHALL seguir recibiendo la potencia y la cadencia.

#### Scenario: Subida en ERG manual
- **WHEN** en una ruta de Zwift se entra en ERG manual a 200 W y Zwift envía una pendiente de 6 %
- **THEN** el Neo sigue a 200 W y Zwift recibe la potencia medida

#### Scenario: Vuelta a la ruta
- **WHEN** en ERG manual sobre una ruta de Zwift, con la marcha 14, Zwift ha enviado una pendiente de 3 % y se vuelve a SIM
- **THEN** el Neo simula +5 % (la pendiente de Zwift con la marcha 14)

#### Scenario: Zwift se desconecta en ERG manual
- **WHEN** en ERG manual sobre una ruta de Zwift se cierra Zwift y pasan más de 30 s
- **THEN** el Neo sigue en la potencia fijada y, al volver a SIM, simula la pendiente base con la marcha puesta

### Requirement: Entrenamiento de Zwift en ERG manual
Si Zwift pide una potencia en un entrenamiento, el ERG manual SHALL terminar y SHALL mandar Zwift, sin ajuste de las palancas; la potencia que tenía el ERG manual SHALL quedar recordada. En ERG de Zwift, «Alternar SIM ⇄ ERG» SHALL NOT cambiar nada y el pie SHALL decir «Entrenamiento de Zwift».

#### Scenario: Empieza un entrenamiento
- **WHEN** en ERG manual a 200 W sobre una ruta de Zwift, Zwift pide 230 W
- **THEN** el Neo pasa a 230 W y la pantalla dice «ERG ZWIFT»

#### Scenario: Alternar en un entrenamiento
- **WHEN** Zwift pide 230 W en un entrenamiento y se mantiene △ hasta el umbral de pulsación larga
- **THEN** el Neo sigue a 230 W, vibra como en un tope y el pie dice «Entrenamiento de Zwift» durante 1,5 s

#### Scenario: Fin del entrenamiento
- **WHEN** el ERG manual terminó al empezar un entrenamiento de Zwift y Zwift vuelve a enviar el terreno
- **THEN** la app queda en SIM con la marcha que había, no en ERG manual

### Requirement: ERG manual sin Neo o sin Square
Sin el Neo conectado, se SHALL poder entrar y salir de ERG manual y ajustar la potencia, y al conectarse el Neo SHALL recibir la potencia o la pendiente vigente. Perder el Square SHALL NOT cambiar el modo ni la potencia.

#### Scenario: Neo que vuelve
- **WHEN** en ERG manual a 200 W se pierde el Neo, se sube a 220 W y el Neo vuelve a conectarse
- **THEN** el Neo recibe 220 W

#### Scenario: Square perdido
- **WHEN** en ERG manual a 200 W se pierde la conexión con el Square
- **THEN** el Neo sigue a 200 W hasta que vuelva el Square y se cambie con él
