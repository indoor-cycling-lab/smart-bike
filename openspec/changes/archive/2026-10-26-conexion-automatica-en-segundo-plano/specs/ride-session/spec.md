## ADDED Requirements

### Requirement: Sesión larga con la pantalla apagada
La sesión SHALL durar 2 h o más con la pantalla apagada o la app en segundo plano sin que el sistema la corte: las conexiones, la marcha y los cambios con el Square SHALL seguir funcionando todo ese tiempo, y una desconexión durante ese tiempo SHALL recuperarse sola.

#### Scenario: Dos horas con la pantalla apagada
- **WHEN** se rueda 2 h con el Neo y el Square conectados, la pantalla apagada y el móvil sin cargar, cambiando de marcha de vez en cuando
- **THEN** cada cambio llega al Neo durante las 2 h, la notificación sigue presente y, al encender la pantalla, Rodaje muestra la marcha puesta sin haber perdido la sesión

#### Scenario: Corte a mitad con la pantalla apagada
- **WHEN** a la hora de rodar con la pantalla apagada se apaga y se enciende el Square
- **THEN** el Square se reconecta solo y las palancas vuelven a cambiar de marcha sin encender la pantalla

## MODIFIED Requirements

### Requirement: Notificación de la sesión
Mientras dura la sesión, la app SHALL mostrar una notificación permanente con la marcha y el estado de las conexiones, que se actualiza al cambiar y dice qué dispositivo se está conectando, con la acción «Terminar»; al tocarla SHALL abrir la app en la pantalla de rodaje.

#### Scenario: Notificación al rodar
- **WHEN** la marcha es la 14 y el Neo y el Square están conectados
- **THEN** la notificación dice «Marcha 14 de 24 · Neo y Square conectados»

#### Scenario: Conexión perdida en segundo plano
- **WHEN** la app está en segundo plano con la marcha 14 y se pierde la conexión con el Square
- **THEN** la notificación dice «Marcha 14 de 24 · Conectando el Square» y, al volver el Square, otra vez «Neo y Square conectados»

#### Scenario: Sin dispositivo que conectar
- **WHEN** no hay ningún Square recordado y el Neo está conectado
- **THEN** la notificación dice «Square sin conexión»
