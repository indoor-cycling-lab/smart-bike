# Spec Delta

## MODIFIED Requirements

### Requirement: Terminar la sesión
Al terminar la sesión, con Atrás dos veces seguidas desde la pantalla de rodaje o con «Terminar» en la notificación, la app SHALL guardar la marcha, enviar al Neo una pendiente de 0 % si está conectado, dejar de anunciarse a Zwift y cerrar la conexión con él, desconectarse del Neo y del Square, quitar la notificación y cerrarse. Un solo Atrás en Rodaje SHALL NOT terminar la sesión.

#### Scenario: Atrás desde Rodaje
- **WHEN** la marcha es la 17 y el usuario pulsa Atrás dos veces seguidas, con menos de 3 s entre ellas, en la pantalla de rodaje
- **THEN** el Neo vuelve a pendiente 0 %, el Neo y el Square quedan libres para otra app, la notificación desaparece y la próxima sesión empieza en la 17

#### Scenario: Un solo Atrás
- **WHEN** el usuario pulsa Atrás una vez en la pantalla de rodaje y no vuelve a pulsarlo
- **THEN** la sesión sigue en marcha: el Neo conserva su resistencia, las conexiones siguen activas y la notificación sigue visible

#### Scenario: Terminar desde la notificación
- **WHEN** la app está en segundo plano y el usuario toca «Terminar» en la notificación
- **THEN** la sesión termina igual que con Atrás dos veces desde Rodaje, sin pedir confirmación

#### Scenario: Terminar rodando en Zwift
- **WHEN** Zwift está conectado a la app y el usuario termina la sesión
- **THEN** Zwift pierde la conexión con «Smart Bike», que deja de aparecer en su pantalla de emparejamiento
