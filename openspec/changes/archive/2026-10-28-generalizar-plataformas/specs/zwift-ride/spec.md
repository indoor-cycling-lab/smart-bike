# Spec Delta

## Purpose

Modo «Todo por wifi (modo Zwift Ride)» de Zwift: la app se presenta ante Zwift por wifi como un solo dispositivo que es a la vez rodillo, fuente de potencia y mando, con el protocolo de rodillo propio de Zwift. Recoge lo que cambia respecto al modo de rodillo por wifi con FTMS.

## ADDED Requirements

### Requirement: Terreno de Zwift con «Todo por wifi»
Con el modo «Todo por wifi (modo Zwift Ride)», Zwift manda su simulación y la app SHALL contestarla, tomar de ella el terreno (pendiente y, si los manda, el coeficiente de rodadura y el aerodinámico) y simular en el Neo esa pendiente con la marcha de la app y el modelo de resistencia elegido (simple, tabla o físico). La marcha de Zwift SHALL ignorarse.

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

### Requirement: Mando por wifi dentro del rodillo
Con el modo «Todo por wifi (modo Zwift Ride)», la app SHALL ofrecer siempre el mando en el mismo dispositivo que el rodillo, de modo que Zwift vea uno solo, como potencia y como mando, sin anunciarse por Bluetooth ni ofrecer desactivarlo. Las funciones de Zwift de los botones del Square SHALL llegar y comportarse como por Bluetooth.

#### Scenario: Emparejar el mando por wifi
- **WHEN** el modo es «Todo por wifi», Zwift empareja el dispositivo por wifi como potencia y como mando y la app tiene wifi
- **THEN** Zwift muestra el dispositivo como rodillo y como mando a la vez, y Plataformas muestra el mando «Conectado» tras el saludo de Zwift

#### Scenario: Funciones desde el Square por wifi
- **WHEN** el mando está conectado por wifi y se pulsa la cruceta ↑ y después B
- **THEN** Zwift abre la barra de acciones y la cierra, como por Bluetooth

#### Scenario: Freno y giro por wifi
- **WHEN** el mando está conectado por wifi, se mantiene una maneta de freno y se suelta
- **THEN** el avatar frena mientras se mantiene y, al soltar, Zwift no conserva el freno

#### Scenario: Diez minutos sin pulsar nada por wifi
- **WHEN** el mando está conectado por wifi y pasan diez minutos sin pulsar nada
- **THEN** Zwift sigue con el mando conectado y el siguiente botón llega

#### Scenario: Sin Bluetooth
- **WHEN** el modo es «Todo por wifi» y el Bluetooth del móvil está apagado
- **THEN** el mando sigue disponible por wifi sin anunciarse por Bluetooth, mientras el Neo y el Square no se conectan hasta que vuelva

#### Scenario: Zwift perdido por wifi
- **WHEN** el mando está conectado por wifi con un botón mantenido y se pierde la conexión con Zwift
- **THEN** la app suelta todos los botones y el siguiente Zwift que se conecta empieza sin nada pulsado

#### Scenario: Mando desactivado por wifi
- **WHEN** el modo es «Todo por wifi» y se abre Plataformas
- **THEN** no hay interruptor «Mando de Zwift», no se puede desactivar el mando y los botones del Square llegan a Zwift por wifi

### Requirement: Información del mando completa por wifi
Con el modo «Todo por wifi (modo Zwift Ride)», la app SHALL contestar con datos, y no solo con el identificador, a las páginas de información que Zwift pide al mando al emparejar (números 0, 16, 1024 y 771). Lo que no sepa contestar SHALL contestarse con el identificador y anotarse en el registro de sesión, sin cambiar lo ya validado del mando.

#### Scenario: Zwift pide las páginas al emparejar
- **WHEN** el modo es «Todo por wifi» y Zwift empareja el dispositivo y saluda al mando
- **THEN** el registro de sesión muestra cada página pedida, con la respuesta enviada, y ninguna de las cuatro queda sin datos

#### Scenario: Botones tras las páginas nuevas
- **WHEN** Zwift ha pedido las páginas y se pulsa la cruceta ↓ y luego A en un menú de Zwift
- **THEN** Zwift baja una opción y la selecciona

#### Scenario: Freno y giro tras las páginas nuevas
- **WHEN** Zwift ha pedido las páginas, se rueda en Zwift, se mantiene una maneta de freno y después ✕
- **THEN** el avatar frena mientras se mantiene la maneta y se desplaza a la izquierda mientras se mantiene ✕

#### Scenario: Página que no se conoce
- **WHEN** Zwift pide una página que la app no sabe contestar
- **THEN** la app contesta con su identificador, lo anota en el registro con el número de la página y el mando sigue conectado

#### Scenario: Diez minutos sin pulsar nada
- **WHEN** el mando está conectado por wifi tras pedir las páginas y pasan diez minutos sin pulsar nada
- **THEN** Zwift sigue con el mando conectado y el siguiente botón llega

#### Scenario: El modo de rodillo por wifi no cambia
- **WHEN** el modo es «Rodillo por wifi (FTMS)» y Zwift empareja el mando
- **THEN** el mando por Bluetooth funciona como siempre y no se contestan páginas de información por wifi
