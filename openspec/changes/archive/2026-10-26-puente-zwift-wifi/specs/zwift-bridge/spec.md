## Purpose

Puente con Zwift por wifi: la app se presenta ante Zwift como un rodillo inteligente en la red local, le envía la potencia y la cadencia del Neo y aplica en el Neo el terreno o la potencia que pide Zwift, junto con la marcha de la app.

## ADDED Requirements

### Requirement: Rodillo visible en Zwift por wifi
Mientras dura la sesión y el móvil está conectado a una red wifi, la app SHALL anunciarse en esa red como un rodillo llamado «Square Shift» que Zwift lista como rodillo controlable, fuente de potencia y fuente de cadencia, sin que el usuario active nada en la app.

#### Scenario: Emparejar en Zwift para PC
- **WHEN** la app está abierta, el móvil y el PC de Zwift están en la misma red wifi y el usuario abre la pantalla de emparejamiento de Zwift
- **THEN** «Square Shift» aparece por wifi en las listas de potencia, cadencia y rodillo controlable, y se puede elegir en las tres

#### Scenario: Zwift vuelve a otra sesión
- **WHEN** «Square Shift» se emparejó en Zwift en una sesión anterior y se abren la app y Zwift otra vez
- **THEN** Zwift lo reconoce como el mismo dispositivo y se conecta sin volver a elegirlo

#### Scenario: Neo sin conexión
- **WHEN** Zwift está conectado a la app y el Neo todavía no se ha conectado
- **THEN** Zwift sigue viendo «Square Shift» conectado, recibe potencia y cadencia 0 y, al conectarse el Neo, empieza a recibir las suyas

### Requirement: Potencia y cadencia hacia Zwift
Con Zwift conectado, la app SHALL enviarle la potencia y la cadencia que mide el Neo, de forma continua mientras se pedalea, y SHALL enviar 0 al dejar de pedalear o si el Neo no está conectado.

#### Scenario: Pedaleando
- **WHEN** se pedalea a unos 200 W y 90 rpm con el Neo y Zwift conectados
- **THEN** Zwift muestra una potencia y una cadencia que siguen a las de la pantalla de rodaje, sin cortes

#### Scenario: Dejar de pedalear
- **WHEN** se deja de pedalear
- **THEN** en unos 2 s Zwift muestra potencia y cadencia 0

#### Scenario: Neo perdido
- **WHEN** se pierde la conexión con el Neo con Zwift conectado
- **THEN** Zwift sigue conectado a la app y recibe potencia y cadencia 0 hasta que el Neo vuelve

### Requirement: Pendiente de Zwift con la marcha
Cuando Zwift envía el terreno de la ruta, la app SHALL simular en el Neo la pendiente de Zwift en lugar de la pendiente base, más el ajuste de la marcha puesta (1 % por marcha respecto a la 12), y SHALL seguir enviando cada cambio de marcha al instante.

#### Scenario: Subida en marcha neutra
- **WHEN** Zwift envía una pendiente de 4 % y la marcha es la 12
- **THEN** el Neo simula +4 %

#### Scenario: Subida en marcha dura
- **WHEN** Zwift envía una pendiente de 4 % y se pasa de la marcha 12 a la 14
- **THEN** el Neo simula +6 % en cuanto se pulsa la palanca, sin esperar al siguiente terreno de Zwift

#### Scenario: Bajada
- **WHEN** Zwift envía una pendiente de −3 % y la marcha es la 10
- **THEN** el Neo simula −5 %

#### Scenario: Terreno que cambia muy seguido
- **WHEN** Zwift envía pendientes nuevas varias veces por segundo
- **THEN** el Neo recibe como mucho 4 pendientes por segundo, siempre con la última de Zwift, y no recibe las que difieren menos de 0,1 % de la anterior

### Requirement: Entrenamiento ERG de Zwift
Cuando Zwift pide una potencia en un entrenamiento, la app SHALL fijar esa potencia en el Neo, más el ajuste de las palancas, y SHALL conservar la marcha puesta sin enviarla mientras dure el ERG; al volver Zwift a enviar el terreno, SHALL volver a la pendiente con esa marcha.

#### Scenario: Bloque de 230 W
- **WHEN** Zwift pide 230 W y no se han tocado las palancas
- **THEN** el Neo mantiene unos 230 W aunque cambie la cadencia

#### Scenario: Cambio de bloque
- **WHEN** Zwift pasa de pedir 230 W a pedir 280 W
- **THEN** el Neo pasa a 280 W más el ajuste de las palancas

#### Scenario: Fin del entrenamiento
- **WHEN** la marcha era la 14 antes del entrenamiento, se ajusta la potencia con las palancas y Zwift vuelve a enviar una pendiente de 2 %
- **THEN** el Neo simula +4 %, la marcha sigue siendo la 14 y el ajuste de las palancas se pone a 0

### Requirement: Palancas en ERG de Zwift
En ERG de Zwift, las palancas SHALL ajustar la potencia en lugar de la marcha: 10 W por cada marcha que pedirían, en el mismo sentido, y □ SHALL quitar el ajuste. El ajuste SHALL mantenerse entre bloques del entrenamiento y la potencia resultante SHALL quedar entre 50 W y 1000 W.

#### Scenario: Subir 10 W
- **WHEN** Zwift pide 230 W y se pulsa la palanca D1
- **THEN** el Neo pasa a 240 W y la marcha no cambia

#### Scenario: Salto con modificador
- **WHEN** Zwift pide 230 W, el ajuste es 0 y se mantiene Campag. I y se pulsa la palanca I1
- **THEN** el Neo pasa a 200 W

#### Scenario: Quitar el ajuste
- **WHEN** Zwift pide 230 W, el ajuste es +20 W y se pulsa □
- **THEN** el Neo vuelve a 230 W

#### Scenario: Ajuste entre bloques
- **WHEN** el ajuste es +10 W y Zwift pasa de pedir 230 W a pedir 280 W
- **THEN** el Neo pasa a 290 W

#### Scenario: Potencia mínima
- **WHEN** Zwift pide 60 W y se pulsa la palanca I1 dos veces
- **THEN** la primera pulsación deja el Neo en 50 W y la segunda no cambia nada y vibra como en un tope

#### Scenario: Marcha más dura o más ligera en ERG
- **WHEN** en ERG se mantiene Campag. D y se pulsa la palanca D2
- **THEN** ni la potencia ni la marcha cambian

### Requirement: Zwift desconectado
Si Zwift se desconecta, la app SHALL mantener en el Neo lo último que pidió (terreno o potencia) durante 30 s y, si Zwift no vuelve, SHALL volver a la pendiente base con la marcha puesta; SHALL seguir anunciándose para que Zwift se reconecte solo.

#### Scenario: Corte breve
- **WHEN** se rueda en una subida de Zwift al 5 % con la marcha 12 y Zwift pierde la conexión durante 10 s
- **THEN** el Neo sigue simulando +5 % durante el corte y, al volver Zwift, la pendiente vuelve a seguir el terreno sin tocar el móvil

#### Scenario: Zwift cerrado
- **WHEN** se rueda con Zwift en una subida al 5 % con la marcha 14 y se cierra Zwift
- **THEN** a los 30 s el Neo pasa a simular +2 % (pendiente base 0 % y marcha 14) y las palancas siguen cambiando de marcha

#### Scenario: Zwift cerrado en un entrenamiento
- **WHEN** Zwift pide 250 W en un entrenamiento y se cierra Zwift
- **THEN** el Neo mantiene 250 W durante 30 s y después pasa a la pendiente de la marcha puesta

#### Scenario: Otro equipo de Zwift
- **WHEN** Zwift está conectado desde un PC y se conecta Zwift desde otro equipo de la red
- **THEN** la app atiende al último que se ha conectado y deja de atender al anterior

### Requirement: Sin wifi
Sin red wifi, la app SHALL seguir rodando sin Zwift y SHALL decir que Zwift necesita la misma red wifi; al recuperar el wifi, SHALL volver a anunciarse sin que el usuario haga nada.

#### Scenario: Wifi apagado al abrir
- **WHEN** se abre la app con el wifi del móvil apagado
- **THEN** las marchas funcionan con la pendiente base, el chip de Zwift dice «sin conexión» y Diagnóstico explica que hay que conectar el móvil a la misma red wifi que Zwift

#### Scenario: Wifi perdido rodando en Zwift
- **WHEN** se rueda en Zwift y el móvil pierde el wifi durante 1 minuto
- **THEN** tras 30 s el Neo vuelve a la pendiente base con la marcha puesta y, al volver el wifi, Zwift se reconecta a la app sin tocar el móvil

### Requirement: Latencia de la palanca con Zwift
Con Zwift conectado, un cambio de marcha SHALL llegar al Neo con la misma inmediatez que sin Zwift: el terreno o la telemetría de Zwift SHALL NOT retrasar el comando de la marcha.

#### Scenario: Cambio en una subida
- **WHEN** se rueda en Zwift en una subida y se pulsa la palanca D1
- **THEN** Diagnóstico muestra una latencia de la palanca del mismo orden que sin Zwift
