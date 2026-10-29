# Spec Delta

## MODIFIED Requirements

### Requirement: Maqueta de Plataformas
La maqueta de Plataformas SHALL mostrar Zwift elegida, sus tres modos con su ayuda, el interruptor «Mando de Zwift» solo con el de rodillo por wifi, el selector «Nombre ante la app» solo con los modos por wifi, el estado de Zwift y del mando con cómo emparejarlos y la navegación inferior, con estados para cada modo, mando desactivado y no disponible, KICKR BIKE PRO, Zwift no visible, sin wifi y Bluetooth apagado, y para el modo por Bluetooth, con el mando dentro del mismo dispositivo: esperando, conectado, Bluetooth apagado y móvil sin modo periférico.

#### Scenario: Estado inicial
- **WHEN** se abre `maquetas/plataformas.html#estado=inicio`
- **THEN** se ve Zwift con «Rodillo por wifi (FTMS)» elegido y recomendado, «Mando de Zwift» activado, «Smart Bike» elegido, Zwift y el mando «Esperando» y la pestaña Plataformas marcada

#### Scenario: Zwift Ride
- **WHEN** se elige el estado de Zwift Ride
- **THEN** «Todo por wifi (modo Zwift Ride)» está elegido, la ayuda dice que Zwift ve un solo dispositivo y mostrará su marcha, no hay interruptor «Mando de Zwift» y el mando se explica como parte del mismo dispositivo

#### Scenario: Mando desactivado y no disponible
- **WHEN** se eligen los estados de mando desactivado y mando no disponible
- **THEN** la fila del mando dice «Desactivado», con que se activa en esta pantalla, o «No disponible», con que este móvil no puede hacer de mando de Zwift

#### Scenario: KICKR BIKE PRO
- **WHEN** se elige el estado KICKR BIKE PRO
- **THEN** «KICKR BIKE PRO» está elegido, la ayuda dice que es una prueba y que hay que emparejar de nuevo, y la ayuda de emparejar nombra el «KICKR BIKE PRO» con sus cuatro caracteres

#### Scenario: Rodillo por Bluetooth
- **WHEN** se abre el estado de Plataformas con «Rodillo por Bluetooth (FTMS)» elegido
- **THEN** el modo está elegido y sin marca de recomendado, la ayuda dice que el mando va dentro del mismo dispositivo y que el Square no se empareja en Zwift, no hay interruptor «Mando de Zwift» ni «Nombre ante la app», y el estado de Zwift y la fila del mando («Dentro de Smart Bike · por Bluetooth») explican cómo emparejar el móvil por su nombre Bluetooth

#### Scenario: Estados del modo por Bluetooth
- **WHEN** se recorren los estados esperando, conectado, Bluetooth apagado y móvil sin modo periférico del modo por Bluetooth
- **THEN** el estado de Zwift y el del mando dicen «Esperando», «Conectado», «Sin conexión» y «No disponible», con la forma de cada uno, y ninguno menciona el wifi

#### Scenario: Rodaje con el modo por Bluetooth
- **WHEN** se abre `maquetas/rodaje.html` en el estado del modo por Bluetooth sin Bluetooth
- **THEN** el estado de la plataforma dice «Sin Bluetooth para Zwift» con el aro vacío y el borde de alerta, enlaza con Plataformas y el resto de la pantalla no se mueve

#### Scenario: Zwift no visible
- **WHEN** se elige el estado Zwift no visible
- **THEN** arriba se ve el aviso de error «Zwift no puede encontrar la app en esta red wifi. Apaga y vuelve a encender el wifi del móvil; si sigue igual, reinícialo.» y la fila de Zwift dice «No visible» con el anillo vacío y el borde de alerta, sin la ayuda de emparejar

#### Scenario: Sin wifi y Bluetooth apagado
- **WHEN** se eligen los estados sin wifi y Bluetooth apagado
- **THEN** Zwift dice «Sin conexión» con la ayuda de conectarse a la misma red, o el mando dice «Sin conexión» con el aviso de Bluetooth apagado, según el modo

#### Scenario: Nombre anunciado en Diagnóstico
- **WHEN** se abre la maqueta de Diagnóstico con el nombre «KICKR BIKE PRO» o «Smart Bike»
- **THEN** Conexiones muestra, bajo la fila de Zwift, ese nombre anunciado

#### Scenario: Acceso desde Rodaje
- **WHEN** en las maquetas de rodaje se toca el icono de Zwift o el del mando
- **THEN** se abre la maqueta de Plataformas, y volver abre Rodaje

## ADDED Requirements

### Requirement: Maqueta de Diagnóstico con el modo por Bluetooth
La maqueta de Diagnóstico SHALL tener estados del modo por Bluetooth, con Zwift conectado y con el Bluetooth apagado, que muestren la fila de Zwift («Por Bluetooth»), el nombre Bluetooth del móvil en lugar del nombre anunciado y la fila del mando dentro del mismo dispositivo, sin ningún aviso de wifi.

#### Scenario: Zwift conectado por Bluetooth
- **WHEN** se abre `maquetas/diagnostico.html#estado=bt-rodando`
- **THEN** Neo, Square, Zwift (por Bluetooth) y el mando (dentro de Smart Bike, por Bluetooth) constan como conectados, el nombre anunciado es el nombre Bluetooth del móvil y no hay aviso de wifi

#### Scenario: Bluetooth apagado
- **WHEN** se abre `maquetas/diagnostico.html#estado=bt-sin-bluetooth`
- **THEN** se ve el aviso de Bluetooth apagado y Zwift y el mando constan como «Sin conexión»

