# Spec Delta

## Purpose

Sesión de rodaje de la app: mantiene las conexiones y la marcha mientras se rueda, también con la pantalla apagada o la app en segundo plano, la anuncia con una notificación y deja el Neo libre al terminar.

## ADDED Requirements

### Requirement: Sesión al abrir la app
Al abrir la app, SHALL empezar una sesión de rodaje que dure hasta que el usuario la termine, aunque la pantalla se apague o se cambie a otra app.

#### Scenario: Pantalla apagada
- **WHEN** el Neo y el Square están conectados, se apaga la pantalla del móvil y se pulsa la palanca D1
- **THEN** la marcha sube, el Neo cambia de resistencia y las dos conexiones siguen activas

#### Scenario: Otra app delante
- **WHEN** se pulsa el botón de inicio del móvil durante la sesión y se vuelve a la app al cabo de un minuto
- **THEN** la app sigue en la misma marcha, con las mismas conexiones, y los cambios hechos con el Square mientras tanto se ven aplicados

### Requirement: Notificación de la sesión
Mientras dura la sesión, la app SHALL mostrar una notificación permanente con la marcha y el estado de las conexiones, que se actualiza al cambiar, con la acción «Terminar»; al tocarla SHALL abrir la app en la pantalla de rodaje.

#### Scenario: Notificación al rodar
- **WHEN** la marcha es la 14 y el Neo y el Square están conectados
- **THEN** la notificación dice «Marcha 14 de 24 · Neo y Square conectados»

#### Scenario: Conexión perdida en segundo plano
- **WHEN** la app está en segundo plano y se pierde la conexión con el Square
- **THEN** la notificación dice que el Square no está conectado

### Requirement: Permiso de notificaciones
Si el sistema exige permiso para mostrar notificaciones, la app SHALL pedirlo al empezar la primera sesión y, si se deniega, SHALL seguir rodando igual sin mostrar la notificación.

#### Scenario: Permiso denegado
- **WHEN** el usuario deniega el permiso de notificaciones
- **THEN** la sesión funciona igual, también con la pantalla apagada, y no se vuelve a pedir el permiso en cada sesión

### Requirement: Terminar la sesión
Al terminar la sesión, con Atrás desde la pantalla de rodaje o con «Terminar» en la notificación, la app SHALL guardar la marcha, enviar al Neo una pendiente de 0 % si está conectado, desconectarse del Neo y del Square, quitar la notificación y cerrarse.

#### Scenario: Atrás desde Rodaje
- **WHEN** la marcha es la 17 y el usuario pulsa Atrás en la pantalla de rodaje
- **THEN** el Neo vuelve a pendiente 0 %, el Neo y el Square quedan libres para otra app, la notificación desaparece y la próxima sesión empieza en la 17

#### Scenario: Terminar desde la notificación
- **WHEN** la app está en segundo plano y el usuario toca «Terminar» en la notificación
- **THEN** la sesión termina igual que con Atrás desde Rodaje
