## MODIFIED Requirements

### Requirement: Notificación de la sesión
Mientras dura la sesión, la app SHALL mostrar una notificación permanente con la marcha (o la potencia objetivo en ERG de Zwift) y el estado de las conexiones, incluido Zwift si está conectado, que se actualiza al cambiar y dice qué dispositivo se está conectando, con la acción «Terminar»; al tocarla SHALL abrir la app en la pantalla de rodaje.

#### Scenario: Notificación al rodar
- **WHEN** la marcha es la 14, el Neo y el Square están conectados y Zwift no
- **THEN** la notificación dice «Marcha 14 de 24 · Neo y Square conectados»

#### Scenario: Rodando en Zwift
- **WHEN** la marcha es la 14 y el Neo, el Square y Zwift están conectados
- **THEN** la notificación dice «Marcha 14 de 24 · Neo, Square y Zwift conectados»

#### Scenario: Entrenamiento de Zwift
- **WHEN** Zwift pide 230 W, el ajuste de las palancas es +10 W y el Neo, el Square y Zwift están conectados
- **THEN** la notificación dice «ERG 240 W · Neo, Square y Zwift conectados»

#### Scenario: Conexión perdida en segundo plano
- **WHEN** la app está en segundo plano con la marcha 14, Zwift no está conectado y se pierde la conexión con el Square
- **THEN** la notificación dice «Marcha 14 de 24 · Conectando el Square» y, al volver el Square, otra vez «Neo y Square conectados»

#### Scenario: Sin dispositivo que conectar
- **WHEN** no hay ningún Square recordado y el Neo está conectado
- **THEN** la notificación dice «Square sin conexión»

### Requirement: Terminar la sesión
Al terminar la sesión, con Atrás desde la pantalla de rodaje o con «Terminar» en la notificación, la app SHALL guardar la marcha, enviar al Neo una pendiente de 0 % si está conectado, dejar de anunciarse a Zwift y cerrar la conexión con él, desconectarse del Neo y del Square, quitar la notificación y cerrarse.

#### Scenario: Atrás desde Rodaje
- **WHEN** la marcha es la 17 y el usuario pulsa Atrás en la pantalla de rodaje
- **THEN** el Neo vuelve a pendiente 0 %, el Neo y el Square quedan libres para otra app, la notificación desaparece y la próxima sesión empieza en la 17

#### Scenario: Terminar desde la notificación
- **WHEN** la app está en segundo plano y el usuario toca «Terminar» en la notificación
- **THEN** la sesión termina igual que con Atrás desde Rodaje

#### Scenario: Terminar rodando en Zwift
- **WHEN** Zwift está conectado a la app y el usuario termina la sesión
- **THEN** Zwift pierde la conexión con «Square Shift», que deja de aparecer en su pantalla de emparejamiento

### Requirement: Sesión larga con la pantalla apagada
La sesión SHALL durar 2 h o más con la pantalla apagada o la app en segundo plano sin que el sistema la corte: las conexiones con el Neo, el Square y Zwift, la marcha y los cambios con el Square SHALL seguir funcionando todo ese tiempo, y una desconexión durante ese tiempo SHALL recuperarse sola.

#### Scenario: Dos horas con la pantalla apagada
- **WHEN** se rueda 2 h en Zwift con el Neo y el Square conectados, la pantalla del móvil apagada y el móvil sin cargar, cambiando de marcha de vez en cuando
- **THEN** cada cambio llega al Neo durante las 2 h, Zwift recibe la potencia y la cadencia sin cortes, la notificación sigue presente y, al encender la pantalla, Rodaje muestra la marcha puesta sin haber perdido la sesión

#### Scenario: Corte a mitad con la pantalla apagada
- **WHEN** a la hora de rodar con la pantalla apagada se apaga y se enciende el Square
- **THEN** el Square se reconecta solo y las palancas vuelven a cambiar de marcha sin encender la pantalla
