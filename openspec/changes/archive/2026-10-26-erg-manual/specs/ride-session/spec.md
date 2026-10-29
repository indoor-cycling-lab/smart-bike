# Spec Delta

## MODIFIED Requirements

### Requirement: Notificación de la sesión
Mientras dura la sesión, la app SHALL mostrar una notificación permanente con la marcha (o la potencia objetivo en ERG de Zwift o en ERG manual) y el estado de las conexiones, incluido Zwift si está conectado, que se actualiza al cambiar y dice qué dispositivo se está conectando, con la acción «Terminar»; al tocarla SHALL abrir la app en la pantalla de rodaje.

#### Scenario: Notificación al rodar
- **WHEN** la marcha es la 14, el Neo y el Square están conectados y Zwift no
- **THEN** la notificación dice «Marcha 14 de 24 · Neo y Square conectados»

#### Scenario: Rodando en Zwift
- **WHEN** la marcha es la 14 y el Neo, el Square y Zwift están conectados
- **THEN** la notificación dice «Marcha 14 de 24 · Neo, Square y Zwift conectados»

#### Scenario: Entrenamiento de Zwift
- **WHEN** Zwift pide 230 W, el ajuste de las palancas es +10 W y el Neo, el Square y Zwift están conectados
- **THEN** la notificación dice «ERG 240 W · Neo, Square y Zwift conectados»

#### Scenario: ERG manual
- **WHEN** se está en ERG manual a 200 W, el Neo y el Square están conectados y Zwift no
- **THEN** la notificación dice «ERG 200 W · Neo y Square conectados»

#### Scenario: Conexión perdida en segundo plano
- **WHEN** la app está en segundo plano con la marcha 14, Zwift no está conectado y se pierde la conexión con el Square
- **THEN** la notificación dice «Marcha 14 de 24 · Conectando el Square» y, al volver el Square, otra vez «Neo y Square conectados»

#### Scenario: Sin dispositivo que conectar
- **WHEN** no hay ningún Square recordado y el Neo está conectado
- **THEN** la notificación dice «Square sin conexión»
